package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private Invoice invoice;
    private Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        final StringBuilder result;
        result = new StringBuilder("Statement for " + invoice.getCustomer() + System.lineSeparator());

        result.append(String.format("Amount owed is %s%n", usd(getTotalAmount(result))));
        result.append(String.format("You earned %s credits%n", getTotalVolumeCredits()));
        return result.toString();
    }

    private int getTotalAmount(StringBuilder result) {
        int totalAmount = 0;
        for (Performance p : invoice.getPerformances()) {
            // print line for this order
            result.append(String.format("  %s: %s (%s seats)%n", getPlay(p).getName(),
                    usd(getAmount(p)), p.getAudience()));
            totalAmount += getAmount(p);
        }
        return totalAmount;
    }

    private int getTotalVolumeCredits() {
        int result = 0;
        for (Performance p : invoice.getPerformances()) {
            result += getVolumeCredits(p);
        }
        return result;
    }

    private static String usd(int totalAmount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(totalAmount / Constants.PERCENT_FACTOR);
    }

    private int getVolumeCredits(Performance performance) {
        // add volume credits
        int result = 0;
        result += Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        // add extra credit for every five comedy attendees
        if ("comedy".equals(getPlay(performance).getType())) {
            result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return result;
    }

    private Play getPlay(Performance performance) {
        final Play play = plays.get(performance.getPlayID());
        return play;
    }

    private int getAmount(Performance performance) {
        int thisAmount = 0;
        switch (getPlay(performance).getType()) {
            case "tragedy":
                thisAmount = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    final int val = performance.getAudience()
                            - Constants.TRAGEDY_AUDIENCE_THRESHOLD;
                    thisAmount += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON * val;
                }
                break;
            case "comedy":
                thisAmount = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                thisAmount += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s",
                        getPlay(performance).getType()));
        }
        return thisAmount;
    }
}
