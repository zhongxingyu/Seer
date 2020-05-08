 package com.mns.alphaposition.server.util;
 
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 
 public class TradingDayUtilsTests {
 
     public static final DateTimeFormatter fmt = DateTimeFormat.forPattern("E yyyy-MM-dd");
 
 
     @Test
     public void testGetMonthlySeries() {
         String[] dtStrs = {
                 "2011-07-29",
                 "2011-06-30",
                 "2011-05-30",
                 "2011-04-29",
                 "2011-03-30",
                 "2011-02-28",
                 "2011-01-28",
                 "2010-12-28",
                 "2010-11-26",
                 "2010-10-28",
                 "2010-09-28",
                 "2010-08-27"
         };
         List<LocalDate> sl = loadAList(dtStrs);
         LocalDate queryDate = new LocalDate(2011, 7, 30);
        List<LocalDate> dates = TradingDayUtils.getMonthlySeries(queryDate.minusYears(1), queryDate, 1, false);
         assertEquals(dates, sl);
     }
 
 
     @Test
     public void testGetDailySeries() {
         String[] dtStrs = {
                 "2011-07-20",
                 "2011-07-19",
                 "2011-07-18",
                 "2011-07-15",
                 "2011-07-14",
                 "2011-07-13",
                 "2011-07-12",
                 "2011-07-11",
                 "2011-07-08",
                 "2011-07-07",
                 "2011-07-06",
                 "2011-07-05",
                 "2011-07-04",
                 "2011-07-01"
         };
         List<LocalDate> sl = loadAList(dtStrs);
         List<LocalDate> dates = TradingDayUtils.getDailySeries(new LocalDate(2011, 7, 1), new LocalDate(2011, 7, 20), false);
         assertEquals(dates, sl);
     }
 
     @Test
     public void testGetPreviousDaysExclusive() {
         String[] dtStrs = {
                 "2011-07-18",
                 "2011-07-15",
                 "2011-07-14",
                 "2011-07-13",
                 "2011-07-12",
                 "2011-07-11",
                 "2011-07-08",
                 "2011-07-07",
                 "2011-07-06",
                 "2011-07-05"
         };
         List<LocalDate> sl = loadAList(dtStrs);
         LocalDate queryDate = new LocalDate(2011, 7, 19);
         int numDays = 10;
         boolean includeToday = false;
         List<LocalDate> dates = TradingDayUtils.getPreviousDays(queryDate, numDays, includeToday);
         assertEquals(dates, sl);
     }
 
     @Test
     public void testGetPreviousDaysInclusive() {
         String[] dtStrs = {
                 "2011-07-19",
                 "2011-07-18",
                 "2011-07-15",
                 "2011-07-14",
                 "2011-07-13",
                 "2011-07-12",
                 "2011-07-11",
                 "2011-07-08",
                 "2011-07-07",
                 "2011-07-06"
         };
         List<LocalDate> sl = loadAList(dtStrs);
         LocalDate queryDate = new LocalDate(2011, 7, 19);
         int numDays = 10;
         boolean includeToday = true;
         List<LocalDate> dates = TradingDayUtils.getPreviousDays(queryDate, numDays, includeToday);
         assertEquals(dates, sl);
     }
 
     private List<LocalDate> loadAList(String[] someStrs) {
         List<LocalDate> newList = new ArrayList<LocalDate>();
         try {
             for (int i = 0; i < someStrs.length; ++i) {
                 newList.add(new LocalDate(someStrs[i]));
             } // end of the for
         } catch (IllegalArgumentException pe) {
             pe.printStackTrace();
         }
         return newList;
     }
 
 }
