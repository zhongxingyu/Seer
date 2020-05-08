 package com.mns.mojoinvest.server.util;
 
 import com.google.common.base.Joiner;
 import com.mns.mojoinvest.server.engine.model.Quote;
 import org.joda.time.LocalDate;
 
 import java.math.BigDecimal;
 import java.util.*;
 
 import static com.mns.mojoinvest.server.util.DatastoreUtils.forDatastore;
 
 public class QuoteUtils {
 
     public static String quoteId(String symbol, LocalDate date) {
         return forDatastore(date) + "|" + symbol;
     }
 
     public static List<Quote> rollMissingQuotes(List<Quote> quotes) {
 
         sortByDateAsc(quotes);
         List<LocalDate> dates = TradingDayUtils.getDailySeries(quotes.get(0).getDate(),
                 quotes.get(quotes.size() - 1).getDate(), true);
 
         List<Quote> missingQuotes = new ArrayList<Quote>();
 
         Iterator<Quote> quoteIter = quotes.iterator();
         Quote quote = quoteIter.next();
         Quote previousQuote = null;
 
         for (LocalDate date : dates) {
             while (date.isAfter(quote.getDate())) {
                 previousQuote = quote;
                 if (quoteIter.hasNext()) {
                     quote = quoteIter.next();
                 } else {
                     break;
                 }
             }
 
             if (date.equals(quote.getDate())) {
                 previousQuote = quote;
                 if (quoteIter.hasNext()) {
                     quote = quoteIter.next();
                 }
             } else {
                 missingQuotes.add(rollQuote(previousQuote, date));
             }
 
         }
         return missingQuotes;
 
 
     }
 
     private static Quote rollQuote(Quote quote, LocalDate date) {
        return new Quote(quote.getSymbol(), date, quote.getIndex(), quote.getNav(),
                 quote.getTrNav(),
                 quote.getDividend(), true);
     }
 
 
     public static void sortByDateAsc(List<Quote> quotes) {
         Collections.sort(quotes, new Comparator<Quote>() {
             @Override
             public int compare(Quote q1, Quote q2) {
                 return q1.getDate().compareTo(q2.getDate());
             }
         });
     }
 
     public static void sortByDateDesc(List<Quote> quotes) {
         Collections.sort(quotes, new Comparator<Quote>() {
             @Override
             public int compare(Quote q1, Quote q2) {
                 return q2.getDate().compareTo(q1.getDate());
             }
         });
     }
 
     public static Quote fromStringArray(String[] row) {
         return new Quote(row[0],
                 new LocalDate(row[1]),
                 row[2].isEmpty() ? null : new BigDecimal(row[2]),
                 row[3].isEmpty() ? null : new BigDecimal(row[3]),
                 row[4].isEmpty() ? null : new BigDecimal(row[4]),
                 row[5].isEmpty() ? null : new BigDecimal(row[5]),
                 Boolean.parseBoolean(row[6]));
     }
 
 
     public static String[] toStringArray(Quote quote) {
         String[] arr = new String[7];
         arr[0] = quote.getSymbol();
         arr[1] = quote.getDate().toString();
         arr[2] = quote.getIndex() == null ? "" : quote.getIndex().toString();
         arr[3] = quote.getNav() == null ? "" : quote.getNav().toString();
         arr[4] = quote.getTrNav() == null ? "" : quote.getTrNav().toString();
         arr[5] = quote.getDividend() == null ? "" : quote.getDividend().toString();
         arr[6] = quote.isRolled() + "";
         return arr;
     }
 
 
     public static Quote fromString(String line) {
         line = line.replaceAll("\"", "");
         String[] row = line.split(",");
         return fromStringArray(row);
     }
 
     public static String toString(Quote quote) {
         return "\"" + Joiner.on("\",\"").join(toStringArray(quote)) + "\"";
     }
 }
