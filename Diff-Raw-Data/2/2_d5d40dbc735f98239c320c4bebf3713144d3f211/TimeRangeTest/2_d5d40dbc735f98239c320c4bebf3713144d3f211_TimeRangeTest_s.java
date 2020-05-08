 package jp.gr.java_conf.afterthesunrise.commons.time;
 
 import static junit.framework.Assert.assertFalse;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.TimeZone;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author takanori.takase
  */
 public class TimeRangeTest {
 
 	private TimeRange target;
 
 	private Time start;
 
 	private Time end;
 
 	private TimeZone timeZone;
 
 	private DateFormat df;
 
 	@Before
 	public void setUp() throws Exception {
 
 		timeZone = TimeZone.getTimeZone("Asia/Tokyo");
 
 		df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 
 		df.setTimeZone(timeZone);
 
 		start = new Time(9, 0, 0, 0);
 
 		end = new Time(15, 0, 0, 0);
 
 		target = new TimeRange(timeZone, start, end);
 
 	}
 
 	@Test(expected = NullPointerException.class)
 	public void testTimeRange_NullTimeZone() {
 		target = new TimeRange(null, start, end);
 	}
 
 	@Test(expected = NullPointerException.class)
 	public void testTimeRange_NullStart() {
 		target = new TimeRange(timeZone, null, end);
 	}
 
 	@Test(expected = NullPointerException.class)
 	public void testTimeRange_NullEnd() {
 		target = new TimeRange(timeZone, start, null);
 	}
 
 	@Test
 	public void testToString() {
 		assertEquals("09:00:00.000 Asia/Tokyo-15:00:00.000 Asia/Tokyo",
 				target.toString());
 	}
 
 	@Test
 	public void testHashCode() {
 		assertEquals(target.hashCode(), target.hashCode());
 	}
 
 	@Test
 	public void testEquals() {
 		TimeZone tz = TimeZone.getTimeZone("America/New_York");
 		assertTrue(target.equals(target));
 		assertTrue(target.equals(new TimeRange(timeZone, start, end)));
 		assertFalse(target.equals(new TimeRange(tz, start, end)));
 		assertFalse(target.equals(new TimeRange(timeZone, new Time(), end)));
 		assertFalse(target.equals(new TimeRange(timeZone, start, new Time())));
 		assertFalse(target.equals(new Object()));
 		assertFalse(target.equals(null));
 	}
 
 	@Test
 	public void testCompareTo() {
 		TimeZone tz = TimeZone.getTimeZone("America/New_York");
 		assertEquals(+0, target.compareTo(target));
 		assertEquals(+0, target.compareTo(new TimeRange(timeZone, start, end)));
		assertEquals(-1, target.compareTo(new TimeRange(tz, start, end)));
 		assertEquals(+1,
 				target.compareTo(new TimeRange(timeZone, new Time(), end)));
 		assertEquals(+1,
 				target.compareTo(new TimeRange(timeZone, start, new Time())));
 	}
 
 	@Test
 	public void testGetTimeZone() {
 		assertEquals(timeZone, target.getTimeZone());
 		assertNotSame(timeZone, target.getTimeZone());
 		assertNotSame(target.getTimeZone(), target.getTimeZone());
 	}
 
 	@Test
 	public void testGetTimeZoneId() {
 		assertEquals("Asia/Tokyo", target.getTimeZoneId());
 	}
 
 	@Test
 	public void testGetStart() {
 		assertSame(start, target.getStart());
 	}
 
 	@Test
 	public void testGetEnd() {
 		assertSame(end, target.getEnd());
 	}
 
 	private long parse(String value) throws ParseException {
 		return df.parse(value).getTime();
 	}
 
 	@Test
 	public void testInRange_Default() throws Exception {
 
 		assertFalse(target.inRange(parse("2012-01-01 08:59")));
 		assertTrue(target.inRange(parse("2012-01-01 09:00")));
 		assertTrue(target.inRange(parse("2012-01-01 09:01")));
 
 		assertTrue(target.inRange(parse("2012-01-01 14:59")));
 		assertFalse(target.inRange(parse("2012-01-01 15:00")));
 		assertFalse(target.inRange(parse("2012-01-01 15:01")));
 
 		// End time past midnight
 		target = new TimeRange(timeZone, new Time(16, 0), new Time(3, 0));
 
 		assertFalse(target.inRange(parse("2012-01-01 15:59")));
 		assertTrue(target.inRange(parse("2012-01-01 16:00")));
 		assertTrue(target.inRange(parse("2012-01-01 16:01")));
 
 		assertTrue(target.inRange(parse("2012-01-01 02:59")));
 		assertFalse(target.inRange(parse("2012-01-01 03:00")));
 		assertFalse(target.inRange(parse("2012-01-01 03:01")));
 
 	}
 
 	@Test
 	public void testInRange_Include_Include() throws Exception {
 
 		assertFalse(target.inRange(parse("2012-01-01 08:59"), true, true));
 		assertTrue(target.inRange(parse("2012-01-01 09:00"), true, true));
 		assertTrue(target.inRange(parse("2012-01-01 09:01"), true, true));
 
 		assertTrue(target.inRange(parse("2012-01-01 14:59"), true, true));
 		assertTrue(target.inRange(parse("2012-01-01 15:00"), true, true));
 		assertFalse(target.inRange(parse("2012-01-01 15:01"), true, true));
 
 		// End time past midnight
 		target = new TimeRange(timeZone, new Time(16, 0), new Time(3, 0));
 
 		assertFalse(target.inRange(parse("2012-01-01 15:59"), true, true));
 		assertTrue(target.inRange(parse("2012-01-01 16:00"), true, true));
 		assertTrue(target.inRange(parse("2012-01-01 16:01"), true, true));
 
 		assertTrue(target.inRange(parse("2012-01-01 02:59"), true, true));
 		assertTrue(target.inRange(parse("2012-01-01 03:00"), true, true));
 		assertFalse(target.inRange(parse("2012-01-01 03:01"), true, true));
 
 	}
 
 	@Test
 	public void testInRange_Include_Exclude() throws Exception {
 
 		assertFalse(target.inRange(parse("2012-01-01 08:59"), true, false));
 		assertTrue(target.inRange(parse("2012-01-01 09:00"), true, false));
 		assertTrue(target.inRange(parse("2012-01-01 09:01"), true, false));
 
 		assertTrue(target.inRange(parse("2012-01-01 14:59"), true, false));
 		assertFalse(target.inRange(parse("2012-01-01 15:00"), true, false));
 		assertFalse(target.inRange(parse("2012-01-01 15:01"), true, false));
 
 		// End time past midnight
 		target = new TimeRange(timeZone, new Time(16, 0), new Time(3, 0));
 
 		assertFalse(target.inRange(parse("2012-01-01 15:59"), true, false));
 		assertTrue(target.inRange(parse("2012-01-01 16:00"), true, false));
 		assertTrue(target.inRange(parse("2012-01-01 16:01"), true, false));
 
 		assertTrue(target.inRange(parse("2012-01-01 02:59"), true, false));
 		assertFalse(target.inRange(parse("2012-01-01 03:00"), true, false));
 		assertFalse(target.inRange(parse("2012-01-01 03:01"), true, false));
 
 	}
 
 	@Test
 	public void testInRange_Exclude_Include() throws Exception {
 
 		assertFalse(target.inRange(parse("2012-01-01 08:59"), false, true));
 		assertFalse(target.inRange(parse("2012-01-01 09:00"), false, true));
 		assertTrue(target.inRange(parse("2012-01-01 09:01"), false, true));
 
 		assertTrue(target.inRange(parse("2012-01-01 14:59"), false, true));
 		assertTrue(target.inRange(parse("2012-01-01 15:00"), false, true));
 		assertFalse(target.inRange(parse("2012-01-01 15:01"), false, true));
 
 		// End time past midnight
 		target = new TimeRange(timeZone, new Time(16, 0), new Time(3, 0));
 
 		assertFalse(target.inRange(parse("2012-01-01 15:59"), false, true));
 		assertFalse(target.inRange(parse("2012-01-01 16:00"), false, true));
 		assertTrue(target.inRange(parse("2012-01-01 16:01"), false, true));
 
 		assertTrue(target.inRange(parse("2012-01-01 02:59"), false, true));
 		assertTrue(target.inRange(parse("2012-01-01 03:00"), false, true));
 		assertFalse(target.inRange(parse("2012-01-01 03:01"), false, true));
 
 	}
 
 	@Test
 	public void testInRange_Exclude_Exclude() throws Exception {
 
 		assertFalse(target.inRange(parse("2012-01-01 08:59"), false, false));
 		assertFalse(target.inRange(parse("2012-01-01 09:00"), false, false));
 		assertTrue(target.inRange(parse("2012-01-01 09:01"), false, false));
 
 		assertTrue(target.inRange(parse("2012-01-01 14:59"), false, false));
 		assertFalse(target.inRange(parse("2012-01-01 15:00"), false, false));
 		assertFalse(target.inRange(parse("2012-01-01 15:01"), false, false));
 
 		// End time past midnight
 		target = new TimeRange(timeZone, new Time(16, 0), new Time(3, 0));
 
 		assertFalse(target.inRange(parse("2012-01-01 15:59"), false, false));
 		assertFalse(target.inRange(parse("2012-01-01 16:00"), false, false));
 		assertTrue(target.inRange(parse("2012-01-01 16:01"), false, false));
 
 		assertTrue(target.inRange(parse("2012-01-01 02:59"), false, false));
 		assertFalse(target.inRange(parse("2012-01-01 03:00"), false, false));
 		assertFalse(target.inRange(parse("2012-01-01 03:01"), false, false));
 
 	}
 
 }
