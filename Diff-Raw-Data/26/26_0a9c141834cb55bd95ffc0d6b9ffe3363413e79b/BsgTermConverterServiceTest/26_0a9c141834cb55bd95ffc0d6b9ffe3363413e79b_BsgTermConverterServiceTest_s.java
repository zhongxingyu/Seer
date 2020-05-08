 package uk.ac.ox.oucs.termdates;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeConstants;
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * Test BSG term converter
 * <p/>
  * Yes, methods names and variables are unconventional, but it easier to read and it's only for unit tests
  *
  * @author Colin Hebert
  */
 public class BsgTermConverterServiceTest extends AbstractTermConverterServiceTest {
     BsgTermConverterService bsgTermConverterService = new BsgTermConverterService();
 
     @Override
     public BsgTermConverterService getTermConverterService() {
         return bsgTermConverterService;
     }
 
     @Test
     public void test_2012_10_30() {
         DateTime _2012_10_30 = new DateTime(2012, 10, 30, 0, 0);
         int expectedWeekNumber = 6;
         String expectedWeekName = "Michaelmas 4";
         DateTime expectedStartOfWeek = _2012_10_30.withDayOfWeek(DateTimeConstants.MONDAY);
         DateTime expectedEndOfWeek = _2012_10_30.withDayOfWeek(DateTimeConstants.SUNDAY);
 
         assertEquals(expectedWeekNumber, getTermConverterService().getTermWeekNumber(_2012_10_30.toDate()));
         assertEquals(expectedWeekName, getTermConverterService().getWeekName(_2012_10_30.toDate()));
         assertEquals(expectedStartOfWeek.toDate(), getTermConverterService().getWeekStartDate(expectedWeekNumber, _2012_10_30.toDate()));
         assertEquals(expectedEndOfWeek.toDate(), getTermConverterService().getWeekEndDate(expectedWeekNumber, _2012_10_30.toDate()));
     }
 
     @Test
     public void test_2012_9_10() {
         DateTime _2012_9_10 = new DateTime(2012, 9, 10, 0, 0);
         int expectedWeekNumber = 52;
         String expectedWeekName = "Summer 10";
         DateTime expectedStartOfWeek = _2012_9_10.withDayOfWeek(DateTimeConstants.MONDAY);
         DateTime expectedEndOfWeek = _2012_9_10.withDayOfWeek(DateTimeConstants.SUNDAY);
 
         assertEquals(expectedWeekNumber, getTermConverterService().getTermWeekNumber(_2012_9_10.toDate()));
         assertEquals(expectedWeekName, getTermConverterService().getWeekName(_2012_9_10.toDate()));
         assertEquals(expectedStartOfWeek.toDate(), getTermConverterService().getWeekStartDate(expectedWeekNumber, _2012_9_10.toDate()));
         assertEquals(expectedEndOfWeek.toDate(), getTermConverterService().getWeekEndDate(expectedWeekNumber, _2012_9_10.toDate()));
     }
 
     @Test
     public void test_2012_9_17() {
         DateTime _2012_9_17 = new DateTime(2012, 9, 17, 0, 0);
         int expectedWeekNumber = 0;
         String expectedWeekName = "Presession 1/Michaelmas -2";
         DateTime expectedStartOfWeek = _2012_9_17.withDayOfWeek(DateTimeConstants.MONDAY);
         DateTime expectedEndOfWeek = _2012_9_17.withDayOfWeek(DateTimeConstants.SUNDAY);
 
         assertEquals(expectedWeekNumber, getTermConverterService().getTermWeekNumber(_2012_9_17.toDate()));
         assertEquals(expectedWeekName, getTermConverterService().getWeekName(_2012_9_17.toDate()));
         assertEquals(expectedStartOfWeek.toDate(), getTermConverterService().getWeekStartDate(expectedWeekNumber, _2012_9_17.toDate()));
         assertEquals(expectedEndOfWeek.toDate(), getTermConverterService().getWeekEndDate(expectedWeekNumber, _2012_9_17.toDate()));
     }
 
     @Test
     public void test_2012_12_17() {
         DateTime _2012_12_17 = new DateTime(2012, 12, 17, 0, 0);
         int expectedWeekNumber = 13;
         String expectedWeekName = "Michaelmas 11";
         DateTime expectedStartOfWeek = _2012_12_17.withDayOfWeek(DateTimeConstants.MONDAY);
         DateTime expectedEndOfWeek = _2012_12_17.withDayOfWeek(DateTimeConstants.SUNDAY);
 
         assertEquals(expectedWeekNumber, getTermConverterService().getTermWeekNumber(_2012_12_17.toDate()));
         assertEquals(expectedWeekName, getTermConverterService().getWeekName(_2012_12_17.toDate()));
         assertEquals(expectedStartOfWeek.toDate(), getTermConverterService().getWeekStartDate(expectedWeekNumber, _2012_12_17.toDate()));
         assertEquals(expectedEndOfWeek.toDate(), getTermConverterService().getWeekEndDate(expectedWeekNumber, _2012_12_17.toDate()));
     }
 
     @Test
     public void test_2012_12_31() {
         DateTime _2012_12_31 = new DateTime(2012, 12, 31, 0, 0);
         int expectedWeekNumber = 15;
         String expectedWeekName = "Hilary -1";
         DateTime expectedStartOfWeek = _2012_12_31.withDayOfWeek(DateTimeConstants.MONDAY);
         DateTime expectedEndOfWeek = _2012_12_31.withDayOfWeek(DateTimeConstants.SUNDAY);
 
         assertEquals(expectedWeekNumber, getTermConverterService().getTermWeekNumber(_2012_12_31.toDate()));
         assertEquals(expectedWeekName, getTermConverterService().getWeekName(_2012_12_31.toDate()));
         assertEquals(expectedStartOfWeek.toDate(), getTermConverterService().getWeekStartDate(expectedWeekNumber, _2012_12_31.toDate()));
         assertEquals(expectedEndOfWeek.toDate(), getTermConverterService().getWeekEndDate(expectedWeekNumber, _2012_12_31.toDate()));
     }
 
     @Test
     public void test_2013_1_1() {
         DateTime _2013_1_1 = new DateTime(2013, 1, 1, 0, 0);
         int expectedWeekNumber = 15;
         String expectedWeekName = "Hilary -1";
         DateTime expectedStartOfWeek = _2013_1_1.withDayOfWeek(DateTimeConstants.MONDAY);
         DateTime expectedEndOfWeek = _2013_1_1.withDayOfWeek(DateTimeConstants.SUNDAY);
 
         assertEquals(expectedWeekNumber, getTermConverterService().getTermWeekNumber(_2013_1_1.toDate()));
         assertEquals(expectedWeekName, getTermConverterService().getWeekName(_2013_1_1.toDate()));
         assertEquals(expectedStartOfWeek.toDate(), getTermConverterService().getWeekStartDate(expectedWeekNumber, _2013_1_1.toDate()));
         assertEquals(expectedEndOfWeek.toDate(), getTermConverterService().getWeekEndDate(expectedWeekNumber, _2013_1_1.toDate()));
     }
 
     @Test
     public void test_2013_9_16() {
         DateTime _2013_9_16 = new DateTime(2013, 9, 16, 0, 0);
         int expectedWeekNumber = 52;
         String expectedWeekName = "Summer 10";
         DateTime expectedStartOfWeek = _2013_9_16.withDayOfWeek(DateTimeConstants.MONDAY);
         DateTime expectedEndOfWeek = _2013_9_16.withDayOfWeek(DateTimeConstants.SUNDAY);
 
         assertEquals(expectedWeekNumber, getTermConverterService().getTermWeekNumber(_2013_9_16.toDate()));
         assertEquals(expectedWeekName, getTermConverterService().getWeekName(_2013_9_16.toDate()));
         assertEquals(expectedStartOfWeek.toDate(), getTermConverterService().getWeekStartDate(expectedWeekNumber, _2013_9_16.toDate()));
         assertEquals(expectedEndOfWeek.toDate(), getTermConverterService().getWeekEndDate(expectedWeekNumber, _2013_9_16.toDate()));
     }
 
     @Test
     public void test_2013_9_9() {
         DateTime _2013_9_9 = new DateTime(2013, 9, 9, 0, 0);
         int expectedWeekNumber = 51;
         String expectedWeekName = "Summer 9";
         DateTime expectedStartOfWeek = _2013_9_9.withDayOfWeek(DateTimeConstants.MONDAY);
         DateTime expectedEndOfWeek = _2013_9_9.withDayOfWeek(DateTimeConstants.SUNDAY);
 
         assertEquals(expectedWeekNumber, getTermConverterService().getTermWeekNumber(_2013_9_9.toDate()));
         assertEquals(expectedWeekName, getTermConverterService().getWeekName(_2013_9_9.toDate()));
         assertEquals(expectedStartOfWeek.toDate(), getTermConverterService().getWeekStartDate(expectedWeekNumber, _2013_9_9.toDate()));
         assertEquals(expectedEndOfWeek.toDate(), getTermConverterService().getWeekEndDate(expectedWeekNumber, _2013_9_9.toDate()));
     }
 }
