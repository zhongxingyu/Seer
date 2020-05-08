 package de.enwida.web.utils;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import de.enwida.web.db.model.CalendarRange;
 
 public class CalendarRangeTest {
 	
 	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 	
 	@Test
 	public void testConstructors() throws Exception {
 		final Calendar cStart = Calendar.getInstance();
 		final Calendar cEnd = Calendar.getInstance();
 		final Date dStart = dateFormat.parse("1967-05-21");
 		final Date dEnd = dateFormat.parse("2016-11-19");
 		
 		cStart.setTime(dStart);
 		cEnd.setTime(dEnd);
 		
 		final CalendarRange cTestee = new CalendarRange(cStart, cEnd);
 		Assert.assertEquals(cStart, cTestee.getFrom());
 		Assert.assertEquals(cEnd, cTestee.getTo());
 		
 		final CalendarRange dTestee = new CalendarRange(dStart, dEnd);
 		Assert.assertEquals(cStart, dTestee.getFrom());
 		Assert.assertEquals(cEnd, dTestee.getTo());
 		
 		try {
 			new CalendarRange(cEnd, cStart);
 		} catch (IllegalArgumentException e) {
 			// Expected
 			return;
 		}
 		throw new Exception("Should not be reachable; IllegalArgumentException expected.");
 	}
 	
 	@Test
 	public void testSetters() throws Exception {
 		final Calendar cStart = Calendar.getInstance();
 		final Calendar cEnd = Calendar.getInstance();
 		final Date dStart = dateFormat.parse("1988-01-21");
 		final Date dEnd = dateFormat.parse("2019-05-02");
 		
 		cStart.setTime(dStart);
 		cEnd.setTime(dEnd);
 
 		final CalendarRange testee = new CalendarRange();
 		Assert.assertNull(testee.getFrom());
 		Assert.assertNull(testee.getTo());
 		
 		testee.setFrom(cStart);
 		testee.setTo(cEnd);
 		
 		Assert.assertEquals(cStart, testee.getFrom());
 		Assert.assertEquals(cEnd, testee.getTo());
 
 		testee.setTo(cStart);
 
 		try {
 			testee.setFrom(cEnd);
 		} catch (IllegalArgumentException e) {
 			// Expected
 			return;
 		}
 		throw new Exception("Should not be reachable; IllegalArgumentException expected.");
 	}
 	
 	@Test
 	public void testMaximumRange() throws ParseException {
 		final List<CalendarRange> ranges = Arrays.asList(
 				getRange("2010-01-01", "2012-01-01"),
 				getRange("1988-05-28", "2011-05-01"),
 				getRange("2015-05-28", "2018-12-01")
 		);
 		final CalendarRange maximum = CalendarRange.getMaximum(ranges);
 		Assert.assertEquals(dateFormat.parse("1988-05-28"), maximum.getFrom().getTime());
 		Assert.assertEquals(dateFormat.parse("2018-12-01"), maximum.getTo().getTime());
 	}
 	
 	@Test
 	public void testMinimumRange() throws ParseException {
 		final List<CalendarRange> ranges = Arrays.asList(
 				getRange("2010-01-01", "2012-01-01"),
 				getRange("1988-05-28", "2011-05-01"),
 				getRange("2010-05-28", "2018-12-01")
 		);
 		final CalendarRange minimum = CalendarRange.getMinimum(ranges);
 		Assert.assertEquals(dateFormat.parse("2010-05-28"), minimum.getFrom().getTime());
 		Assert.assertEquals(dateFormat.parse("2011-05-01"), minimum.getTo().getTime());
 		
 	}
 	
 	@Test
 	public void testMinimumRangeNoOverlap() throws Exception {
 		final List<CalendarRange> ranges = Arrays.asList(
 				getRange("2010-01-01", "2012-01-01"),
 				getRange("1988-05-28", "2011-05-01"),
 				getRange("2016-05-28", "2018-12-01")
 		);
		try {
			// Should construct a negative time range
			CalendarRange.getMinimum(ranges);
		} catch (IllegalArgumentException e) {
			// Expected
			return;
		}
		throw new Exception("Should not be reachable; IllegalArgumentException expected.");
 	}
 	
 	@Test
 	public void testConnectedTimeRanges() throws ParseException {
 		final List<CalendarRange> ranges = new ArrayList<>(Arrays.asList(
 				getRange("2010-01-01", "2012-01-01"),
 				getRange("1988-05-28", "2011-05-01"),
 				getRange("2010-05-28", "2018-12-01")
 		));
 		// Check for fully connected range
 		List<CalendarRange> testee = CalendarRange.getConnectedRanges(ranges);
 		Assert.assertEquals(1, testee.size());
 		Assert.assertEquals(dateFormat.parse("1988-05-28"), testee.get(0).getFrom().getTime());
 		Assert.assertEquals(dateFormat.parse("2018-12-01"), testee.get(0).getTo().getTime());
 		
 		// Check for partially connected ranges
 		ranges.add(getRange("2020-01-01", "2025-01-01"));
 		ranges.add(getRange("2025-01-01", "2030-01-01"));
 		testee = CalendarRange.getConnectedRanges(ranges);
 		Assert.assertEquals(2, testee.size());
 		Assert.assertEquals(dateFormat.parse("1988-05-28"), testee.get(0).getFrom().getTime());
 		Assert.assertEquals(dateFormat.parse("2018-12-01"), testee.get(0).getTo().getTime());
 		Assert.assertEquals(dateFormat.parse("2020-01-01"), testee.get(1).getFrom().getTime());
 		Assert.assertEquals(dateFormat.parse("2030-01-01"), testee.get(1).getTo().getTime());
 		
 		// Add a disconnected range
 		ranges.add(getRange("2050-01-01", "2060-01-01"));
 		testee = CalendarRange.getConnectedRanges(ranges);
 		Assert.assertEquals(3, testee.size());
 		Assert.assertEquals(dateFormat.parse("2050-01-01"), testee.get(2).getFrom().getTime());
 		Assert.assertEquals(dateFormat.parse("2060-01-01"), testee.get(2).getTo().getTime());
 	}
 	
 	private CalendarRange getRange(String start, String end) throws ParseException {
 		return new CalendarRange(dateFormat.parse(start), dateFormat.parse(end));
 	}
 
 }
