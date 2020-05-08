 package com.mns.alphaposition.server.util;
 
 import org.joda.time.DateTimeConstants;
 import org.joda.time.LocalDate;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class TradingDayUtils {
 
 
 //    Actual: paid on the actual day, even if it is a non-business day.
 //    Following business day: the payment date is rolled to the next business day.
 //    Modified following business day: the payment date is rolled to the next business day, unless doing so would
 //      cause the payment to be in the next calendar month, in which case the payment date is rolled to the previous
 //      business day. Many institutions have month-end accounting procedures that necessitate this.
 //    Previous business day: the payment date is rolled to the previous business day.
 //    Modified previous business day: the payment date is rolled to the previous business day, unless doing so
 //      would cause the payment to be in the previous calendar month, in which case the payment date is rolled to
 //      the next business day. Many institutions have month-end accounting procedures that necessitate this.
 
 
    public static List<LocalDate> getMonthlySeries(LocalDate fromDate, LocalDate toDate, int frequency, boolean forwards) {
         List<LocalDate> dates = new ArrayList<LocalDate>();
         while (toDate.isAfter(fromDate)) {
             dates.add(rollIfRequired(toDate));
            toDate = toDate.minusMonths(frequency);
         }
         if (forwards) {
             //Little bit back to front the logic here
             Collections.reverse(dates);
         }
         return dates;
     }
 
     public static List<LocalDate> getDailySeries(LocalDate fromDate, LocalDate toDate, boolean forwards) {
         List<LocalDate> dates = new ArrayList<LocalDate>();
         while (toDate.isAfter(fromDate) || toDate.isEqual(fromDate)) {
             toDate = rollIfRequired(toDate);
             dates.add(toDate);
             toDate = toDate.minusDays(1);
         }
         if (forwards) {
             //Little bit back to front the logic here
             Collections.reverse(dates);
         }
         return dates;
     }
 
     public static List<LocalDate> getPreviousDays(LocalDate date, int numDays, boolean inclusive) {
         List<LocalDate> dates = new ArrayList<LocalDate>();
         int count = 0;
         if (inclusive) {
             date = rollIfRequired(date);
             dates.add(date);
             count++;
         }
         while (count < numDays) {
             date = rollIfRequired(date.minusDays(1));
             dates.add(date);
             count++;
         }
         return dates;
     }
 
     public static LocalDate rollIfRequired(LocalDate date) {
         if (date.dayOfWeek().get() == DateTimeConstants.SATURDAY)
             return date.minusDays(1);
         else if (date.dayOfWeek().get() == DateTimeConstants.SUNDAY)
             return date.minusDays(2);
         else
             return date;
     }
 }
