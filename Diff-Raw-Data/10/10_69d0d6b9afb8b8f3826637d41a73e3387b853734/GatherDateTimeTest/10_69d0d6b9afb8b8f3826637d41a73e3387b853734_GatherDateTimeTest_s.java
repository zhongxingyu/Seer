 /**
  * Copyright (C) 2009 AED <info@gatherdata.org>
  *
  * OSI compliant license pending.
  *
  * http://www.opensource.org/licenses
  */
 package org.gatherdata.commons.util;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.junit.Assert.assertNotNull;
 
 import java.text.ParseException;
 import java.util.Calendar;
 
 import org.gatherdata.commons.util.GatherDateTimeParser;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.junit.Test;
 
 public class GatherDateTimeTest {
 
 	private static final String YEAR_ONLY_VALID_STRING = "1920";
 	private static final String YEAR_ONLY_INVALID_STRING = "19Twenty";
 	private static final String YEAR_ONLY_OUTOFBOUNDS_STRING = "-30208";
 
 	private static final String YEAR_MONTH_DAY_VALID_STRING = "1969-02-07";
 	private static final String YEAR_MONTH_DAY_INVALID_STRING = "19690207";
 
 	private static final String MONTH_DAY_YEAR_VALID_STRING = "05/11/1985";
 	private static final String MONTH_DAY_YEAR_INVALID_STRING = "301-07-1969";
 	
 	private static final String YEAR_MONTH_DAY_TIME_VALID_STRING = "2009-03-22-05:14";
 	private static final String YEAR_MONTH_DAY_TIME_INVALID_STRING = "19690107:05:32";
 	
	private static final String ISO_VALID_STRING = "2009-10-11T14:23:59.343-04:00";
	private static final String ISO_INVALID_STRING = "2009-10-11X14:23:59.343-04:00";
 
 	@Test
 	public void shouldParseAsYearOnly() throws Exception {
 		LocalDate parsedDate = GatherDateTimeParser.parseAsYearOnly(YEAR_ONLY_VALID_STRING);
 		assertThat(parsedDate, notNullValue());
 		assertThat(parsedDate.getYear(), is(1920));
         assertThat(parsedDate.getMonthOfYear(), is (1));
 		assertThat(parsedDate.getDayOfMonth(), is (1));
 	}
 	
 	@Test (expected = NumberFormatException.class)
 	public void shouldNotParseInvalidAsYearOnly() throws Exception {
 		GatherDateTimeParser.parseAsYearOnly(YEAR_ONLY_INVALID_STRING);
 	}
 	
 	@Test (expected = NumberFormatException.class)
 	public void shouldNotParseOutOfBoundsAsYearOnly() throws Exception {
 		GatherDateTimeParser.parseAsYearOnly(YEAR_ONLY_OUTOFBOUNDS_STRING);
 	}
 	
     @Test
     public void shouldParseAsIsoStandard() throws Exception {
        DateTime parsedDateTime = GatherDateTimeParser.parseDateTime(ISO_VALID_STRING);
         assertThat(parsedDateTime.getYear(), is (2009));
         assertThat(parsedDateTime.getMonthOfYear(), is(10));
         assertThat(parsedDateTime.getDayOfMonth(), is(11));
         assertThat(parsedDateTime.getHourOfDay(), is(14));
         assertThat(parsedDateTime.getMinuteOfHour(), is(23));
         assertThat(parsedDateTime.getSecondOfMinute(), is(59));
     }
     
     @Test
     public void shouldNotParseInvalidAsIsoStandard() throws Exception {
         assertThat(GatherDateTimeParser.parseDateTime(ISO_INVALID_STRING), nullValue());
     }
 
 	@Test
 	public void shouldParseAsYearMonthDay() throws Exception {
 		LocalDate parsedDate = GatherDateTimeParser.parseDate(YEAR_MONTH_DAY_VALID_STRING);
         assertThat(parsedDate, notNullValue());
         assertThat(parsedDate.getYear(), is(1969));
         assertThat(parsedDate.getMonthOfYear(), is (2));
         assertThat(parsedDate.getDayOfMonth(), is (7));
 	}
 	
 	@Test
 	public void shouldNotParseInvalidAsYearMonthDay() throws Exception {
 		assertThat(GatherDateTimeParser.parseDate(YEAR_MONTH_DAY_INVALID_STRING), nullValue());
 	}
 
 	@Test
 	public void shouldParseAsMonthDayYear() throws Exception {
 	    LocalDate parsedDate = GatherDateTimeParser.parseDate(MONTH_DAY_YEAR_VALID_STRING);
         assertThat(parsedDate, notNullValue());
         assertThat(parsedDate.getYear(), is(1985));
         assertThat(parsedDate.getMonthOfYear(), is (5));
         assertThat(parsedDate.getDayOfMonth(), is (11));
 	}
 	
 	@Test
 	public void shouldNotParseInvalidAsMonthDayYear() throws Exception {
 		assertThat(GatherDateTimeParser.parseDate(MONTH_DAY_YEAR_INVALID_STRING), nullValue());
 	}
 
 	@Test
 	public void shouldParseAsYearMonthDayTime() throws Exception {
         DateTime parsedDateTime = GatherDateTimeParser.parseDateTime(YEAR_MONTH_DAY_TIME_VALID_STRING);
         assertThat(parsedDateTime.getYear(), is (2009));
         assertThat(parsedDateTime.getMonthOfYear(), is(3));
         assertThat(parsedDateTime.getDayOfMonth(), is(22));
         assertThat(parsedDateTime.getHourOfDay(), is(5));
         assertThat(parsedDateTime.getMinuteOfHour(), is(14));
         assertThat(parsedDateTime.getSecondOfMinute(), is(0));
 	}
 	
 	@Test
 	public void shouldNotParseInvalidAsYearMonthDayTime() throws Exception {
 		assertThat(GatherDateTimeParser.parseDateTime(YEAR_MONTH_DAY_TIME_INVALID_STRING), nullValue());
 	}
 
 	@Test
 	public void shouldParseAnySupportedFormat() throws Exception {
 		assertNotNull(GatherDateTimeParser.parseDate(YEAR_ONLY_VALID_STRING));
 		assertNotNull(GatherDateTimeParser.parseDate(YEAR_MONTH_DAY_VALID_STRING));
 		assertNotNull(GatherDateTimeParser.parseDate(MONTH_DAY_YEAR_VALID_STRING));
 		assertNotNull(GatherDateTimeParser.parseDateTime(YEAR_MONTH_DAY_TIME_VALID_STRING));
 		assertNotNull(GatherDateTimeParser.parseDateTime(ISO_VALID_STRING));
 	}
 
 	private void testRoundTripDateTime(String originalDateTimeString) throws ParseException {
         DateTime fromStringToDateTime = GatherDateTimeParser.parseDateTime(originalDateTimeString);
         String backToString = GatherDateTimeParser.format(fromStringToDateTime);
         DateTime backToDateTimeAgain = GatherDateTimeParser.parseDateTime(backToString);
         assertThat(fromStringToDateTime, is(backToDateTimeAgain));
 	}
 
     private void testRoundTripDate(String originalDateTimeString) throws ParseException {
         LocalDate fromStringToDateTime = GatherDateTimeParser.parseDate(originalDateTimeString);
         String backToString = GatherDateTimeParser.format(fromStringToDateTime);
         LocalDate backToDateTimeAgain = GatherDateTimeParser.parseDate(backToString);
         assertThat(fromStringToDateTime, is(backToDateTimeAgain));
     }
     
 	@Test
 	public void shouldParseYearOnlyRoundtrip() throws ParseException {
 	    testRoundTripDate(YEAR_ONLY_VALID_STRING);
 	}
 	
 	@Test
     public void shouldParseYearMonthDayRoundtrip() throws ParseException {
         testRoundTripDate(YEAR_MONTH_DAY_VALID_STRING);
     }
 
     @Test
     public void shouldParseMonthDayYearRoundtrip() throws ParseException {
         testRoundTripDate(YEAR_MONTH_DAY_VALID_STRING);
     }
     
     @Test
     public void shouldParseYearMonthDayTimeRoundtrip() throws ParseException {
         testRoundTripDateTime(YEAR_MONTH_DAY_TIME_VALID_STRING);
     }
 
     @Test
     public void shouldParseIsoDateTimeRoundtrip() throws ParseException {
         testRoundTripDateTime(ISO_VALID_STRING);
     }
     
 }
