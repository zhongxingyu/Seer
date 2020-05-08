 package com.twock.swappricer.test.fpml;
 
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.*;
 
 import com.twock.swappricer.HolidayCalendar;
 import com.twock.swappricer.HolidayCalendarContainer;
 import com.twock.swappricer.fpml.FpmlParser;
 import com.twock.swappricer.fpml.SwapStreamDateCalculator;
 import com.twock.swappricer.fpml.model.*;
 import com.twock.swappricer.fpml.model.enumeration.*;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import static com.twock.swappricer.fpml.model.enumeration.BusinessDayConventionEnum.*;
 import static com.twock.swappricer.fpml.model.enumeration.DayCountFractionEnum.*;
 import static com.twock.swappricer.fpml.model.enumeration.RollConventionEnum.*;
 
 /**
  * @author Chris Pearson (chris@twock.com)
  */
 public class SwapStreamDateCalculatorTest {
   private static SwapStreamDateCalculator calculator;
   private static FpmlParser fpmlParser;
   private static HolidayCalendarContainer allCalendars;
   private static HolidayCalendarContainer london;
   private static final double DCF_DELTA = 0.00001;
 
   @BeforeClass
   public static void setUp() throws UnsupportedEncodingException {
     calculator = new SwapStreamDateCalculator();
     fpmlParser = FpmlParserTest.createFpmlParser();
     allCalendars = new HolidayCalendarContainer();
     allCalendars.loadFromTsv(new InputStreamReader(SwapStreamDateCalculatorTest.class.getResourceAsStream("/calendars.tsv"), "UTF8"));
     london = new HolidayCalendarContainer(allCalendars, "GBLO");
   }
 
   @Test
   public void checkImmDate() {
     DateWithDayCount JAN18_2012 = new DateWithDayCount(2012, 1, 18);
     Assert.assertTrue(calculator.matchesRollConvention(JAN18_2012, IMM));
     Assert.assertFalse(calculator.matchesRollConvention(JAN18_2012, DAY17));
     Assert.assertTrue(calculator.matchesRollConvention(JAN18_2012, DAY18));
     Assert.assertFalse(calculator.matchesRollConvention(JAN18_2012, DAY19));
     Assert.assertFalse(calculator.matchesRollConvention(JAN18_2012, EOM));
   }
 
   @Test
   public void checkImmSecondWeds() {
     DateWithDayCount JAN11_2012 = new DateWithDayCount(2012, 1, 11);
     Assert.assertFalse(calculator.matchesRollConvention(JAN11_2012, IMM));
     Assert.assertFalse(calculator.matchesRollConvention(JAN11_2012, DAY10));
     Assert.assertTrue(calculator.matchesRollConvention(JAN11_2012, DAY11));
     Assert.assertFalse(calculator.matchesRollConvention(JAN11_2012, DAY12));
     Assert.assertFalse(calculator.matchesRollConvention(JAN11_2012, EOM));
   }
 
   @Test
   public void checkImmFourthWeds() {
     DateWithDayCount JAN25_2012 = new DateWithDayCount(2012, 1, 25);
     Assert.assertFalse(calculator.matchesRollConvention(JAN25_2012, IMM));
     Assert.assertFalse(calculator.matchesRollConvention(JAN25_2012, DAY24));
     Assert.assertTrue(calculator.matchesRollConvention(JAN25_2012, DAY25));
     Assert.assertFalse(calculator.matchesRollConvention(JAN25_2012, DAY26));
     Assert.assertFalse(calculator.matchesRollConvention(JAN25_2012, EOM));
   }
 
   @Test
   public void checkMidMonth() {
     DateWithDayCount JAN19_2012 = new DateWithDayCount(2012, 1, 19);
     Assert.assertFalse(calculator.matchesRollConvention(JAN19_2012, IMM));
     Assert.assertFalse(calculator.matchesRollConvention(JAN19_2012, DAY17));
     Assert.assertFalse(calculator.matchesRollConvention(JAN19_2012, DAY18));
     Assert.assertTrue(calculator.matchesRollConvention(JAN19_2012, DAY19));
     Assert.assertFalse(calculator.matchesRollConvention(JAN19_2012, EOM));
   }
 
   @Test
   public void checkLeapYear() {
     DateWithDayCount FEB29_2012 = new DateWithDayCount(2012, 2, 29);
     Assert.assertFalse(calculator.matchesRollConvention(FEB29_2012, IMM));
     Assert.assertFalse(calculator.matchesRollConvention(FEB29_2012, DAY28));
     Assert.assertTrue(calculator.matchesRollConvention(FEB29_2012, DAY29));
     Assert.assertTrue(calculator.matchesRollConvention(FEB29_2012, DAY30));
     Assert.assertTrue(calculator.matchesRollConvention(FEB29_2012, EOM));
   }
 
   @Test
   public void checkLeapYear28Feb() {
     DateWithDayCount FEB28_2012 = new DateWithDayCount(2012, 2, 28);
     Assert.assertFalse(calculator.matchesRollConvention(FEB28_2012, IMM));
     Assert.assertTrue(calculator.matchesRollConvention(FEB28_2012, DAY28));
     Assert.assertFalse(calculator.matchesRollConvention(FEB28_2012, DAY29));
     Assert.assertFalse(calculator.matchesRollConvention(FEB28_2012, DAY30));
     Assert.assertFalse(calculator.matchesRollConvention(FEB28_2012, EOM));
   }
 
   @Test
   public void checkNonLeapYear() {
     DateWithDayCount FEB28_2011 = new DateWithDayCount(2011, 2, 28);
     Assert.assertFalse(calculator.matchesRollConvention(FEB28_2011, IMM));
     Assert.assertTrue(calculator.matchesRollConvention(FEB28_2011, DAY28));
     Assert.assertTrue(calculator.matchesRollConvention(FEB28_2011, DAY29));
     Assert.assertTrue(calculator.matchesRollConvention(FEB28_2011, DAY30));
     Assert.assertTrue(calculator.matchesRollConvention(FEB28_2011, EOM));
   }
 
   @Test
   public void testNoAdjustmentNeeded() {
     HolidayCalendarContainer calendars = new HolidayCalendarContainer();
     Assert.assertEquals(new DateWithDayCount(2012, 1, 11), calculator.adjustDate(new DateWithDayCount(2012, 1, 11), FOLLOWING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 11), calculator.adjustDate(new DateWithDayCount(2012, 1, 11), PRECEDING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 11), calculator.adjustDate(new DateWithDayCount(2012, 1, 11), NO_ADJUST, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 11), calculator.adjustDate(new DateWithDayCount(2012, 1, 11), MODFOLLOWING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 11), calculator.adjustDate(new DateWithDayCount(2012, 1, 11), MODPRECEDING, calendars));
   }
 
   @Test
   public void testWeekendModFol() {
     HolidayCalendarContainer calendars = new HolidayCalendarContainer();
     Assert.assertEquals(new DateWithDayCount(2012, 4, 2), calculator.adjustDate(new DateWithDayCount(2012, 3, 31), FOLLOWING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 3, 30), calculator.adjustDate(new DateWithDayCount(2012, 3, 31), PRECEDING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 3, 31), calculator.adjustDate(new DateWithDayCount(2012, 3, 31), NO_ADJUST, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 3, 30), calculator.adjustDate(new DateWithDayCount(2012, 3, 31), MODFOLLOWING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 3, 30), calculator.adjustDate(new DateWithDayCount(2012, 3, 31), MODPRECEDING, calendars));
   }
 
   @Test
   public void testWeekendModPrec() {
     HolidayCalendarContainer calendars = new HolidayCalendarContainer();
     Assert.assertEquals(new DateWithDayCount(2012, 1, 2), calculator.adjustDate(new DateWithDayCount(2012, 1, 1), FOLLOWING, calendars));
     Assert.assertEquals(new DateWithDayCount(2011, 12, 30), calculator.adjustDate(new DateWithDayCount(2012, 1, 1), PRECEDING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 1), calculator.adjustDate(new DateWithDayCount(2012, 1, 1), NO_ADJUST, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 2), calculator.adjustDate(new DateWithDayCount(2012, 1, 1), MODFOLLOWING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 2), calculator.adjustDate(new DateWithDayCount(2012, 1, 1), MODPRECEDING, calendars));
   }
 
   @Test
   public void testAdjustmentForHolidayInOnlyNeeded() {
     HolidayCalendarContainer calendars = new HolidayCalendarContainer();
     TreeMap<String, HolidayCalendar> calendarMap = new TreeMap<String, HolidayCalendar>();
     calendarMap.put("GBLO", new HolidayCalendar("GBLO", Arrays.asList(new DateWithDayCount(2012, 1, 11))));
     calendars.setHolidayCalendars(calendarMap);
 
     Assert.assertEquals(new DateWithDayCount(2012, 1, 12), calculator.adjustDate(new DateWithDayCount(2012, 1, 11), FOLLOWING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 10), calculator.adjustDate(new DateWithDayCount(2012, 1, 11), PRECEDING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 11), calculator.adjustDate(new DateWithDayCount(2012, 1, 11), NO_ADJUST, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 12), calculator.adjustDate(new DateWithDayCount(2012, 1, 11), MODFOLLOWING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 10), calculator.adjustDate(new DateWithDayCount(2012, 1, 11), MODPRECEDING, calendars));
   }
 
   @Test
   public void testAdjustmentForMondayHolidayInOnlyNeeded() {
     HolidayCalendarContainer calendars = new HolidayCalendarContainer();
     TreeMap<String, HolidayCalendar> calendarMap = new TreeMap<String, HolidayCalendar>();
     calendarMap.put("GBLO", new HolidayCalendar("GBLO", Arrays.asList(new DateWithDayCount(2012, 1, 9))));
     calendars.setHolidayCalendars(calendarMap);
 
     Assert.assertEquals(new DateWithDayCount(2012, 1, 10), calculator.adjustDate(new DateWithDayCount(2012, 1, 9), FOLLOWING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 6), calculator.adjustDate(new DateWithDayCount(2012, 1, 9), PRECEDING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 9), calculator.adjustDate(new DateWithDayCount(2012, 1, 9), NO_ADJUST, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 10), calculator.adjustDate(new DateWithDayCount(2012, 1, 9), MODFOLLOWING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 6), calculator.adjustDate(new DateWithDayCount(2012, 1, 9), MODPRECEDING, calendars));
   }
 
   @Test
   public void testAdjustmentForMondayHolidayInSecondNeeded() {
     HolidayCalendarContainer calendars = new HolidayCalendarContainer();
     TreeMap<String, HolidayCalendar> calendarMap = new TreeMap<String, HolidayCalendar>();
     calendarMap.put("GBLO", new HolidayCalendar("GBLO", Collections.<DateWithDayCount>emptyList()));
     calendarMap.put("USNY", new HolidayCalendar("USNY", Arrays.asList(new DateWithDayCount(2012, 1, 9))));
     calendars.setHolidayCalendars(calendarMap);
 
     Assert.assertEquals(new DateWithDayCount(2012, 1, 10), calculator.adjustDate(new DateWithDayCount(2012, 1, 9), FOLLOWING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 6), calculator.adjustDate(new DateWithDayCount(2012, 1, 9), PRECEDING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 9), calculator.adjustDate(new DateWithDayCount(2012, 1, 9), NO_ADJUST, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 10), calculator.adjustDate(new DateWithDayCount(2012, 1, 9), MODFOLLOWING, calendars));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 6), calculator.adjustDate(new DateWithDayCount(2012, 1, 9), MODPRECEDING, calendars));
   }
 
   @Test
   public void singleExactPeriod() {
     Assert.assertEquals(Arrays.asList(
       new DateWithDayCount(2012, 1, 11),
       new DateWithDayCount(2012, 7, 11)
     ), calculator.calculateUnadjustedPeriodDates(new DateWithDayCount(2012, 1, 11), null, null, new DateWithDayCount(2012, 7, 11), new CalculationPeriodFrequency(6, PeriodEnum.M, DAY11)));
   }
 
   @Test
   public void singleShortPeriod() {
     Assert.assertEquals(Arrays.asList(
       new DateWithDayCount(2012, 1, 11),
       new DateWithDayCount(2012, 7, 11)
     ), calculator.calculateUnadjustedPeriodDates(new DateWithDayCount(2012, 1, 11), null, null, new DateWithDayCount(2012, 7, 11), new CalculationPeriodFrequency(1, PeriodEnum.Y, DAY11)));
   }
 
   @Test
   public void imm3mDates() {
     Assert.assertEquals(Arrays.asList(
       new DateWithDayCount(2012, 1, 18),
       new DateWithDayCount(2012, 4, 18),
       new DateWithDayCount(2012, 7, 18),
       new DateWithDayCount(2012, 10, 17),
       new DateWithDayCount(2013, 1, 16)
     ), calculator.calculateUnadjustedPeriodDates(new DateWithDayCount(2012, 1, 18), null, null, new DateWithDayCount(2013, 1, 16), new CalculationPeriodFrequency(3, PeriodEnum.M, IMM)));
   }
 
   @Test
   public void imm1mDates() {
     Assert.assertEquals(Arrays.asList(
       new DateWithDayCount(2012, 1, 18),
       new DateWithDayCount(2012, 2, 15),
       new DateWithDayCount(2012, 3, 21),
       new DateWithDayCount(2012, 4, 18),
       new DateWithDayCount(2012, 5, 16),
       new DateWithDayCount(2012, 6, 20),
       new DateWithDayCount(2012, 7, 18)
     ), calculator.calculateUnadjustedPeriodDates(new DateWithDayCount(2012, 1, 18), null, null, new DateWithDayCount(2012, 7, 18), new CalculationPeriodFrequency(1, PeriodEnum.M, IMM)));
   }
 
   @Test
   public void immStubsGiven() {
     DateWithDayCount JAN18 = new DateWithDayCount(2012, 1, 18);
     DateWithDayCount FEB15 = new DateWithDayCount(2012, 2, 15);
     DateWithDayCount JUN20 = new DateWithDayCount(2012, 6, 20);
     DateWithDayCount JUL18 = new DateWithDayCount(2012, 7, 18);
     List<DateWithDayCount> expected = Arrays.asList(JAN18, FEB15, new DateWithDayCount(2012, 3, 21), new DateWithDayCount(2012, 4, 18), new DateWithDayCount(2012, 5, 16), JUN20, JUL18);
     Assert.assertEquals(expected, calculator.calculateUnadjustedPeriodDates(JAN18, FEB15, null, JUL18, new CalculationPeriodFrequency(1, PeriodEnum.M, IMM)));
     Assert.assertEquals(expected, calculator.calculateUnadjustedPeriodDates(JAN18, FEB15, JUN20, JUL18, new CalculationPeriodFrequency(1, PeriodEnum.M, IMM)));
     Assert.assertEquals(expected, calculator.calculateUnadjustedPeriodDates(JAN18, null, JUN20, JUL18, new CalculationPeriodFrequency(1, PeriodEnum.M, IMM)));
   }
 
   @Test
   public void shortInitialStubPeriod() {
     Assert.assertEquals(Arrays.asList(
       new DateWithDayCount(2012, 1, 11),
       new DateWithDayCount(2012, 1, 17),
       new DateWithDayCount(2012, 7, 17)
     ), calculator.calculateUnadjustedPeriodDates(new DateWithDayCount(2012, 1, 11), new DateWithDayCount(2012, 1, 17), null, new DateWithDayCount(2012, 7, 17), new CalculationPeriodFrequency(6, PeriodEnum.M, DAY17)));
     Assert.assertEquals(Arrays.asList(
       new DateWithDayCount(2012, 1, 11),
       new DateWithDayCount(2012, 2, 1),
       new DateWithDayCount(2012, 8, 1)
     ), calculator.calculateUnadjustedPeriodDates(new DateWithDayCount(2012, 1, 11), new DateWithDayCount(2012, 2, 1), null, new DateWithDayCount(2012, 8, 1), new CalculationPeriodFrequency(6, PeriodEnum.M, DAY17)));
   }
 
   @Test
   public void shortFinalStubPeriod() {
     Assert.assertEquals(Arrays.asList(
       new DateWithDayCount(2012, 1, 11),
       new DateWithDayCount(2012, 7, 11),
       new DateWithDayCount(2012, 7, 17)
     ), calculator.calculateUnadjustedPeriodDates(new DateWithDayCount(2012, 1, 11), null, new DateWithDayCount(2012, 7, 11), new DateWithDayCount(2012, 7, 17), new CalculationPeriodFrequency(6, PeriodEnum.M, DAY17)));
     Assert.assertEquals(Arrays.asList(
       new DateWithDayCount(2012, 1, 11),
       new DateWithDayCount(2012, 7, 11),
       new DateWithDayCount(2012, 8, 1)
     ), calculator.calculateUnadjustedPeriodDates(new DateWithDayCount(2012, 1, 11), null, new DateWithDayCount(2012, 7, 11), new DateWithDayCount(2012, 8, 1), new CalculationPeriodFrequency(6, PeriodEnum.M, DAY17)));
   }
 
   @Test
   public void testNormalPeriod() {
     Assert.assertEquals(Arrays.asList(
       new DateWithDayCount(2012, 1, 11),
       new DateWithDayCount(2012, 2, 11),
       new DateWithDayCount(2012, 3, 11),
       new DateWithDayCount(2012, 4, 11),
       new DateWithDayCount(2012, 5, 11),
       new DateWithDayCount(2012, 6, 11),
       new DateWithDayCount(2012, 7, 11),
       new DateWithDayCount(2012, 8, 11)
     ), calculator.calculateUnadjustedPeriodDates(new DateWithDayCount(2012, 1, 11), null, null, new DateWithDayCount(2012, 8, 11), new CalculationPeriodFrequency(1, PeriodEnum.M, DAY11)));
   }
 
   @Test
   public void singleTerm() {
     Assert.assertEquals(Arrays.asList(
       new DateWithDayCount(2012, 1, 11),
       new DateWithDayCount(2012, 8, 11)
     ), calculator.calculateUnadjustedPeriodDates(new DateWithDayCount(2012, 1, 11), null, null, new DateWithDayCount(2012, 8, 11), new CalculationPeriodFrequency(1, PeriodEnum.T, DAY11)));
     Assert.assertEquals(Arrays.asList(
       new DateWithDayCount(2012, 1, 11),
       new DateWithDayCount(2014, 8, 11)
     ), calculator.calculateUnadjustedPeriodDates(new DateWithDayCount(2012, 1, 11), null, null, new DateWithDayCount(2014, 8, 11), new CalculationPeriodFrequency(1, PeriodEnum.T, DAY11)));
   }
 
   @Test
   public void calculateFpmlPeriodDates() {
     List<SwapStream> streams = fpmlParser.parse(getClass().getResourceAsStream("/LCH00000513426.xml"));
     SwapStream s1 = streams.get(0);
     SwapStream s2 = streams.get(1);
     Assert.assertEquals(Arrays.asList(
       new DateWithDayCount(2008, 6, 13),
       new DateWithDayCount(2009, 6, 13),
       new DateWithDayCount(2010, 6, 13),
       new DateWithDayCount(2011, 6, 13),
       new DateWithDayCount(2012, 6, 13),
       new DateWithDayCount(2013, 6, 13),
       new DateWithDayCount(2014, 6, 13),
       new DateWithDayCount(2015, 6, 13),
       new DateWithDayCount(2016, 6, 13),
       new DateWithDayCount(2017, 6, 13),
       new DateWithDayCount(2018, 6, 13)
     ), calculator.calculateUnadjustedPeriodDates(s1.getEffectiveDate().getUnadjustedDate(), s1.getFirstRegularPeriodStartDate(), s1.getLastRegularPeriodEndDate(), s1.getTerminationDate().getUnadjustedDate(), s1.getCalculationPeriodFrequency()));
     Assert.assertEquals(Arrays.asList(
       new DateWithDayCount(2008, 6, 13),
       new DateWithDayCount(2008, 12, 13),
       new DateWithDayCount(2009, 6, 13),
       new DateWithDayCount(2009, 12, 13),
       new DateWithDayCount(2010, 6, 13),
       new DateWithDayCount(2010, 12, 13),
       new DateWithDayCount(2011, 6, 13),
       new DateWithDayCount(2011, 12, 13),
       new DateWithDayCount(2012, 6, 13),
       new DateWithDayCount(2012, 12, 13),
       new DateWithDayCount(2013, 6, 13),
       new DateWithDayCount(2013, 12, 13),
       new DateWithDayCount(2014, 6, 13),
       new DateWithDayCount(2014, 12, 13),
       new DateWithDayCount(2015, 6, 13),
       new DateWithDayCount(2015, 12, 13),
       new DateWithDayCount(2016, 6, 13),
       new DateWithDayCount(2016, 12, 13),
       new DateWithDayCount(2017, 6, 13),
       new DateWithDayCount(2017, 12, 13),
       new DateWithDayCount(2018, 6, 13)
     ), calculator.calculateUnadjustedPeriodDates(s2.getEffectiveDate().getUnadjustedDate(), s2.getFirstRegularPeriodStartDate(), s2.getLastRegularPeriodEndDate(), s2.getTerminationDate().getUnadjustedDate(), s2.getCalculationPeriodFrequency()));
   }
 
   @Test
   public void calculateAdjustedFpmlPeriodDates() {
     List<SwapStream> streams = fpmlParser.parse(getClass().getResourceAsStream("/LCH00000513426.xml"));
     SwapStream s1 = streams.get(0);
     SwapStream s2 = streams.get(1);
     List<DateWithDayCount> unadjustedSide1Dates = calculator.calculateUnadjustedPeriodDates(s1.getEffectiveDate().getUnadjustedDate(), s1.getFirstRegularPeriodStartDate(), s1.getLastRegularPeriodEndDate(), s1.getTerminationDate().getUnadjustedDate(), s1.getCalculationPeriodFrequency());
     List<DateWithDayCount> unadjustedSide2Dates = calculator.calculateUnadjustedPeriodDates(s2.getEffectiveDate().getUnadjustedDate(), s2.getFirstRegularPeriodStartDate(), s2.getLastRegularPeriodEndDate(), s2.getTerminationDate().getUnadjustedDate(), s2.getCalculationPeriodFrequency());
     List<DateWithDayCount> adjustedSide1Dates = calculator.calculateAdjustedPeriodDates(unadjustedSide1Dates, s1.getEffectiveDate().getBusinessDayAdjustments(), s1.getCalculationPeriodDatesAdjustments(), s1.getTerminationDate().getBusinessDayAdjustments(), allCalendars);
     List<DateWithDayCount> adjustedSide2Dates = calculator.calculateAdjustedPeriodDates(unadjustedSide2Dates, s2.getEffectiveDate().getBusinessDayAdjustments(), s2.getCalculationPeriodDatesAdjustments(), s2.getTerminationDate().getBusinessDayAdjustments(), allCalendars);
     Assert.assertEquals(Arrays.asList(
       new DateWithDayCount(2008, 6, 13),
       new DateWithDayCount(2009, 6, 15),
       new DateWithDayCount(2010, 6, 14),
       new DateWithDayCount(2011, 6, 13),
       new DateWithDayCount(2012, 6, 13),
       new DateWithDayCount(2013, 6, 13),
       new DateWithDayCount(2014, 6, 13),
       new DateWithDayCount(2015, 6, 15),
       new DateWithDayCount(2016, 6, 13),
       new DateWithDayCount(2017, 6, 13),
       new DateWithDayCount(2018, 6, 13)
     ), adjustedSide1Dates);
     Assert.assertTrue(unadjustedSide1Dates.get(0) == adjustedSide1Dates.get(0));
     Assert.assertTrue(unadjustedSide1Dates.get(3) == adjustedSide1Dates.get(3));
     Assert.assertTrue(unadjustedSide1Dates.get(4) == adjustedSide1Dates.get(4));
     Assert.assertTrue(unadjustedSide1Dates.get(10) == adjustedSide1Dates.get(10));
     Assert.assertEquals(Arrays.asList(
       new DateWithDayCount(2008, 6, 13),
       new DateWithDayCount(2008, 12, 15),
       new DateWithDayCount(2009, 6, 15),
       new DateWithDayCount(2009, 12, 14),
       new DateWithDayCount(2010, 6, 14),
       new DateWithDayCount(2010, 12, 13),
       new DateWithDayCount(2011, 6, 13),
       new DateWithDayCount(2011, 12, 13),
       new DateWithDayCount(2012, 6, 13),
       new DateWithDayCount(2012, 12, 13),
       new DateWithDayCount(2013, 6, 13),
       new DateWithDayCount(2013, 12, 13),
       new DateWithDayCount(2014, 6, 13),
       new DateWithDayCount(2014, 12, 15),
       new DateWithDayCount(2015, 6, 15),
       new DateWithDayCount(2015, 12, 14),
       new DateWithDayCount(2016, 6, 13),
       new DateWithDayCount(2016, 12, 13),
       new DateWithDayCount(2017, 6, 13),
       new DateWithDayCount(2017, 12, 13),
       new DateWithDayCount(2018, 6, 13)
     ), adjustedSide2Dates);
   }
 
   @Test
   public void shiftNoBusinessDay() {
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 13), 0, PeriodEnum.D, DayTypeEnum.BUSINESS, BusinessDayConventionEnum.NO_ADJUST, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 13), 0, PeriodEnum.D, DayTypeEnum.BUSINESS, BusinessDayConventionEnum.FOLLOWING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 13), 0, PeriodEnum.D, DayTypeEnum.BUSINESS, BusinessDayConventionEnum.PRECEDING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 13), 0, PeriodEnum.D, DayTypeEnum.BUSINESS, BusinessDayConventionEnum.MODFOLLOWING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 13), 0, PeriodEnum.D, DayTypeEnum.BUSINESS, BusinessDayConventionEnum.MODPRECEDING, london));
   }
 
   @Test
   public void shiftNoCalendarDay() {
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 13), 0, PeriodEnum.D, DayTypeEnum.CALENDAR, BusinessDayConventionEnum.NO_ADJUST, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 13), 0, PeriodEnum.D, DayTypeEnum.CALENDAR, BusinessDayConventionEnum.FOLLOWING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 13), 0, PeriodEnum.D, DayTypeEnum.CALENDAR, BusinessDayConventionEnum.PRECEDING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 13), 0, PeriodEnum.D, DayTypeEnum.CALENDAR, BusinessDayConventionEnum.MODFOLLOWING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 13), 0, PeriodEnum.D, DayTypeEnum.CALENDAR, BusinessDayConventionEnum.MODPRECEDING, london));
   }
 
   @Test
   public void shiftPositiveBusinessDay() {
     Assert.assertEquals(new DateWithDayCount(2012, 1, 16), calculator.shift(new DateWithDayCount(2012, 1, 13), 1, PeriodEnum.D, DayTypeEnum.BUSINESS, BusinessDayConventionEnum.NO_ADJUST, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 16), calculator.shift(new DateWithDayCount(2012, 1, 13), 1, PeriodEnum.D, DayTypeEnum.BUSINESS, BusinessDayConventionEnum.FOLLOWING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 16), calculator.shift(new DateWithDayCount(2012, 1, 13), 1, PeriodEnum.D, DayTypeEnum.BUSINESS, BusinessDayConventionEnum.PRECEDING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 16), calculator.shift(new DateWithDayCount(2012, 1, 13), 1, PeriodEnum.D, DayTypeEnum.BUSINESS, BusinessDayConventionEnum.MODFOLLOWING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 16), calculator.shift(new DateWithDayCount(2012, 1, 13), 1, PeriodEnum.D, DayTypeEnum.BUSINESS, BusinessDayConventionEnum.MODPRECEDING, london));
   }
 
   @Test
   public void shiftPositiveCalendarDay() {
     Assert.assertEquals(new DateWithDayCount(2012, 1, 14), calculator.shift(new DateWithDayCount(2012, 1, 13), 1, PeriodEnum.D, DayTypeEnum.CALENDAR, BusinessDayConventionEnum.NO_ADJUST, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 16), calculator.shift(new DateWithDayCount(2012, 1, 13), 1, PeriodEnum.D, DayTypeEnum.CALENDAR, BusinessDayConventionEnum.FOLLOWING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 13), 1, PeriodEnum.D, DayTypeEnum.CALENDAR, BusinessDayConventionEnum.PRECEDING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 16), calculator.shift(new DateWithDayCount(2012, 1, 13), 1, PeriodEnum.D, DayTypeEnum.CALENDAR, BusinessDayConventionEnum.MODFOLLOWING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 13), 1, PeriodEnum.D, DayTypeEnum.CALENDAR, BusinessDayConventionEnum.MODPRECEDING, london));
   }
 
   @Test
   public void shiftNegativeBusinessDay() {
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 16), -1, PeriodEnum.D, DayTypeEnum.BUSINESS, BusinessDayConventionEnum.NO_ADJUST, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 16), -1, PeriodEnum.D, DayTypeEnum.BUSINESS, BusinessDayConventionEnum.FOLLOWING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 16), -1, PeriodEnum.D, DayTypeEnum.BUSINESS, BusinessDayConventionEnum.PRECEDING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 16), -1, PeriodEnum.D, DayTypeEnum.BUSINESS, BusinessDayConventionEnum.MODFOLLOWING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 16), -1, PeriodEnum.D, DayTypeEnum.BUSINESS, BusinessDayConventionEnum.MODPRECEDING, london));
   }
 
   @Test
   public void shiftNegativeCalendarDay() {
     Assert.assertEquals(new DateWithDayCount(2012, 1, 15), calculator.shift(new DateWithDayCount(2012, 1, 16), -1, PeriodEnum.D, DayTypeEnum.CALENDAR, BusinessDayConventionEnum.NO_ADJUST, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 16), calculator.shift(new DateWithDayCount(2012, 1, 16), -1, PeriodEnum.D, DayTypeEnum.CALENDAR, BusinessDayConventionEnum.FOLLOWING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 16), -1, PeriodEnum.D, DayTypeEnum.CALENDAR, BusinessDayConventionEnum.PRECEDING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 16), calculator.shift(new DateWithDayCount(2012, 1, 16), -1, PeriodEnum.D, DayTypeEnum.CALENDAR, BusinessDayConventionEnum.MODFOLLOWING, london));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 13), calculator.shift(new DateWithDayCount(2012, 1, 16), -1, PeriodEnum.D, DayTypeEnum.CALENDAR, BusinessDayConventionEnum.MODPRECEDING, london));
   }
 
   @Test
   public void noInitialStub() {
     calculator.hasInitialStub(new DateWithDayCount(2012, 1, 18), new DateWithDayCount(2012, 2, 15), new CalculationPeriodFrequency(1, PeriodEnum.M, IMM));
     calculator.hasInitialStub(new DateWithDayCount(2012, 1, 18), new DateWithDayCount(2012, 2, 18), new CalculationPeriodFrequency(1, PeriodEnum.M, DAY18));
     calculator.hasInitialStub(new DateWithDayCount(2012, 1, 31), new DateWithDayCount(2012, 2, 29), new CalculationPeriodFrequency(1, PeriodEnum.M, EOM));
     calculator.hasInitialStub(new DateWithDayCount(2012, 1, 30), new DateWithDayCount(2012, 2, 29), new CalculationPeriodFrequency(1, PeriodEnum.M, DAY30));
     calculator.hasInitialStub(new DateWithDayCount(2012, 2, 29), new DateWithDayCount(2012, 3, 30), new CalculationPeriodFrequency(1, PeriodEnum.M, DAY30));
     calculator.hasInitialStub(new DateWithDayCount(2012, 2, 29), new DateWithDayCount(2012, 3, 30), new CalculationPeriodFrequency(1, PeriodEnum.M, EOM));
   }
 
   @Test
   public void initialStub() {
     calculator.hasInitialStub(new DateWithDayCount(2012, 1, 17), new DateWithDayCount(2012, 2, 15), new CalculationPeriodFrequency(1, PeriodEnum.M, IMM));
     calculator.hasInitialStub(new DateWithDayCount(2012, 1, 19), new DateWithDayCount(2012, 2, 15), new CalculationPeriodFrequency(1, PeriodEnum.M, IMM));
     calculator.hasInitialStub(new DateWithDayCount(2012, 1, 18), new DateWithDayCount(2012, 2, 14), new CalculationPeriodFrequency(1, PeriodEnum.M, IMM));
     calculator.hasInitialStub(new DateWithDayCount(2012, 1, 18), new DateWithDayCount(2012, 2, 16), new CalculationPeriodFrequency(1, PeriodEnum.M, IMM));
     calculator.hasInitialStub(new DateWithDayCount(2012, 1, 17), new DateWithDayCount(2012, 2, 18), new CalculationPeriodFrequency(1, PeriodEnum.M, DAY18));
     calculator.hasInitialStub(new DateWithDayCount(2012, 1, 19), new DateWithDayCount(2012, 2, 18), new CalculationPeriodFrequency(1, PeriodEnum.M, DAY18));
     calculator.hasInitialStub(new DateWithDayCount(2012, 1, 18), new DateWithDayCount(2012, 2, 17), new CalculationPeriodFrequency(1, PeriodEnum.M, DAY18));
     calculator.hasInitialStub(new DateWithDayCount(2012, 1, 18), new DateWithDayCount(2012, 2, 19), new CalculationPeriodFrequency(1, PeriodEnum.M, DAY18));
     calculator.hasInitialStub(new DateWithDayCount(2012, 1, 30), new DateWithDayCount(2012, 2, 29), new CalculationPeriodFrequency(1, PeriodEnum.M, EOM));
     calculator.hasInitialStub(new DateWithDayCount(2012, 2, 29), new DateWithDayCount(2012, 3, 31), new CalculationPeriodFrequency(1, PeriodEnum.M, DAY30));
     calculator.hasInitialStub(new DateWithDayCount(2012, 1, 18), new DateWithDayCount(2012, 2, 18), new CalculationPeriodFrequency(1, PeriodEnum.M, DAY1));
   }
 
   @Test
   public void noStubPaymentDates() {
     PaymentDates ONEMONTH_MODFOL = new PaymentDates(1, PeriodEnum.M, PayRelativeToEnum.CALCULATION_PERIOD_END_DATE, null, new BusinessDayAdjustments(BusinessDayConventionEnum.MODFOLLOWING, "GBLO"));
     PaymentDates ONEMONTH_MODFOL_UPFRONT = new PaymentDates(1, PeriodEnum.M, PayRelativeToEnum.CALCULATION_PERIOD_START_DATE, null, new BusinessDayAdjustments(BusinessDayConventionEnum.MODFOLLOWING, "GBLO"));
     Assert.assertEquals(Arrays.asList(new DateWithDayCount(2012, 2, 1)),
       calculator.calculatePaymentDates(Arrays.asList(new DateWithDayCount(2012, 1, 1), new DateWithDayCount(2012, 2, 1)), ONEMONTH_MODFOL, allCalendars));
     Assert.assertEquals(Arrays.asList(new DateWithDayCount(2012, 1, 3)),
       calculator.calculatePaymentDates(Arrays.asList(new DateWithDayCount(2012, 1, 1), new DateWithDayCount(2012, 2, 1)), ONEMONTH_MODFOL_UPFRONT, allCalendars));
     Assert.assertEquals(Arrays.asList(new DateWithDayCount(2012, 2, 1), new DateWithDayCount(2012, 3, 1), new DateWithDayCount(2012, 4, 2)),
       calculator.calculatePaymentDates(Arrays.asList(new DateWithDayCount(2012, 1, 1), new DateWithDayCount(2012, 2, 1), new DateWithDayCount(2012, 3, 1), new DateWithDayCount(2012, 4, 1)), ONEMONTH_MODFOL, allCalendars));
     Assert.assertEquals(Arrays.asList(new DateWithDayCount(2012, 1, 3), new DateWithDayCount(2012, 2, 1), new DateWithDayCount(2012, 3, 1)),
       calculator.calculatePaymentDates(Arrays.asList(new DateWithDayCount(2012, 1, 1), new DateWithDayCount(2012, 2, 1), new DateWithDayCount(2012, 3, 1), new DateWithDayCount(2012, 4, 1)), ONEMONTH_MODFOL_UPFRONT, allCalendars));
   }
 
   @Test
   public void testDayCountSingle() {
     Assert.assertArrayEquals(new double[]{1}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2012, 1, 1), new DateWithDayCount(2012, 2, 1)), SINGLE, null, false, false), DCF_DELTA);
     Assert.assertArrayEquals(new double[]{1}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2012, 1, 1), new DateWithDayCount(2013, 1, 1)), SINGLE, null, false, false), DCF_DELTA);
   }
 
   @Test
   public void testDayCountActActIsda() {
     // actual number of days divided by 365 (or 366 on leap years)
     Assert.assertArrayEquals(new double[]{31.0 / 366}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2012, 1, 1), new DateWithDayCount(2012, 2, 1)), ACT_ACT_ISDA, null, false, false), DCF_DELTA);
     Assert.assertArrayEquals(new double[]{1.0}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2011, 1, 1), new DateWithDayCount(2012, 1, 1)), ACT_ACT_ISDA, null, false, false), DCF_DELTA);
     Assert.assertArrayEquals(new double[]{1.0 + 31.0 / 366}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2011, 1, 1), new DateWithDayCount(2012, 2, 1)), ACT_ACT_ISDA, null, false, false), DCF_DELTA);
     Assert.assertArrayEquals(new double[]{1.0 + 31.0 / 365}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2012, 1, 1), new DateWithDayCount(2013, 2, 1)), ACT_ACT_ISDA, null, false, false), DCF_DELTA);
     Assert.assertArrayEquals(new double[]{17.0 / 365 + 31.0 / 366}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2011, 12, 15), new DateWithDayCount(2012, 2, 1)), ACT_ACT_ISDA, null, false, false), DCF_DELTA);
     Assert.assertArrayEquals(new double[]{17.0 / 366 + 31.0 / 365}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2012, 12, 15), new DateWithDayCount(2013, 2, 1)), ACT_ACT_ISDA, null, false, false), DCF_DELTA);
     // examples from http://www.isda.org/c_and_a/pdf/ACT-ACT-ISDA-1999.pdf
     Assert.assertArrayEquals(new double[]{61.0 / 365 + 121.0 / 366}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2003, 11, 1), new DateWithDayCount(2004, 5, 1)), ACT_ACT_ISDA, null, false, false), DCF_DELTA);
     Assert.assertArrayEquals(new double[]{150.0 / 365, 184.0 / 365 + 182.0 / 366}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(1999, 2, 1), new DateWithDayCount(1999, 7, 1), new DateWithDayCount(2000, 7, 1)), ACT_ACT_ISDA, null, true, false), DCF_DELTA);
     Assert.assertArrayEquals(new double[]{334.0 / 365, 170.0 / 365 + 14.0 / 366}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2002, 8, 15), new DateWithDayCount(2003, 7, 15), new DateWithDayCount(2004, 1, 15)), ACT_ACT_ISDA, null, true, false), DCF_DELTA);
     Assert.assertArrayEquals(new double[]{155.0 / 365 + 29.0 / 366, 152.0 / 366}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(1999, 7, 30), new DateWithDayCount(2000, 1, 30), new DateWithDayCount(2000, 6, 30)), ACT_ACT_ISDA, null, false, true), DCF_DELTA);
     Assert.assertArrayEquals(new double[]{32.0 / 365 + 120.0 / 366}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(1999, 11, 30), new DateWithDayCount(2000, 4, 30)), ACT_ACT_ISDA, null, false, true), DCF_DELTA);
 
   }
 
   @Test
   public void testDayCountActActIcmaIsma() {
     // ICMA and ISMA are identical: http://www.isda.org/c_and_a/trading_practice.html
     EnumSet<DayCountFractionEnum> identical = EnumSet.of(ACT_ACT_ISMA, ACT_ACT_ICMA);
     for(DayCountFractionEnum dcf : identical) {
       // examples from http://www.isda.org/c_and_a/pdf/ACT-ACT-ISDA-1999.pdf
       Assert.assertArrayEquals(new double[]{0.5}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2003, 11, 1), new DateWithDayCount(2004, 5, 1)), dcf, new CalculationPeriodFrequency(6, PeriodEnum.M, DAY1), false, false), DCF_DELTA);
       Assert.assertArrayEquals(new double[]{150.0 / 365, 1}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(1999, 2, 1), new DateWithDayCount(1999, 7, 1), new DateWithDayCount(2000, 7, 1)), dcf, new CalculationPeriodFrequency(1, PeriodEnum.Y, DAY1), true, false), DCF_DELTA);
       Assert.assertArrayEquals(new double[]{0.5 + 153.0 / (184 * 2), 0.5}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2002, 8, 15), new DateWithDayCount(2003, 7, 15), new DateWithDayCount(2004, 1, 15)), dcf, new CalculationPeriodFrequency(6, PeriodEnum.M, DAY15), true, false), DCF_DELTA);
       Assert.assertArrayEquals(new double[]{0.5, 152.0 / (182 * 2)}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(1999, 7, 30), new DateWithDayCount(2000, 1, 30), new DateWithDayCount(2000, 6, 30)), dcf, new CalculationPeriodFrequency(6, PeriodEnum.M, DAY30), false, true), DCF_DELTA);
       Assert.assertArrayEquals(new double[]{0.25 + 61.0 / (92 * 4)}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(1999, 11, 30), new DateWithDayCount(2000, 4, 30)), dcf, new CalculationPeriodFrequency(3, PeriodEnum.M, EOM), false, true), DCF_DELTA);
     }
   }
 
  @Test
  public void testDayCountAct365Fixed() {
    Assert.assertArrayEquals(new double[]{31.0 / 365}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2012, 1, 1), new DateWithDayCount(2012, 2, 1)), ACT_365_FIXED, null, false, false), DCF_DELTA);
    Assert.assertArrayEquals(new double[]{48.0 / 365}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2011, 12, 15), new DateWithDayCount(2012, 2, 1)), ACT_365_FIXED, null, false, false), DCF_DELTA);
    Assert.assertArrayEquals(new double[]{695.0 / 365}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2010, 6, 15), new DateWithDayCount(2012, 5, 10)), ACT_365_FIXED, null, false, false), DCF_DELTA);
  }

  @Test
  public void testDayCountAct360() {
    Assert.assertArrayEquals(new double[]{31.0 / 360}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2012, 1, 1), new DateWithDayCount(2012, 2, 1)), ACT_360, null, false, false), DCF_DELTA);
    Assert.assertArrayEquals(new double[]{48.0 / 360}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2011, 12, 15), new DateWithDayCount(2012, 2, 1)), ACT_360, null, false, false), DCF_DELTA);
    Assert.assertArrayEquals(new double[]{695.0 / 360}, calculator.getDayCountFractions(Arrays.asList(new DateWithDayCount(2010, 6, 15), new DateWithDayCount(2012, 5, 10)), ACT_360, null, false, false), DCF_DELTA);
  }

   /*
   THIRTY_360("30/360"),
   THIRTY_E_360("30E/360"),
   THIRTY_E_360_ISDA("30E/360.ISDA"),
 */
 
 }
