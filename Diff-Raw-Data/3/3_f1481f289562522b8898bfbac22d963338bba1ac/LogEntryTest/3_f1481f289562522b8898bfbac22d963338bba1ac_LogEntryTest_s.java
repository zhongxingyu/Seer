 package org.logparser;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.not;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Unit tests for {@link LogEntry}.
  * 
  * @author jorge.decastro
  * 
  */
 public class LogEntryTest {
 	private LogEntry underTest;
 	private static final String ENTRY = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /statusCheck.do HTTP/1.1\" 200 1779 2073";
 	private static final String ACTION = "statusCheck.do";
 	private static final String DURATION = "2073.0";
 	private Calendar calendar;
 
 	@Before
 	public void setUp() {
 		calendar = Calendar.getInstance();
 		calendar.set(2008, 11, 15, 17, 15, 00);
 		Date date = calendar.getTime();
 		underTest = new LogEntry(ENTRY, date, ACTION, DURATION);
 	}
 
 	@After
 	public void tearDown() {
 		calendar = null;
 		underTest = null;
 	}
 
 	@Test
 	@SuppressWarnings("deprecation")
 	public void testLogEntryImmutability() {
 		// only need to test mutability w/ {@link Dates} since the other arguments are immutables
 		Date d = underTest.getDate();
 		d.setMinutes(30);
 		assertThat(underTest.getDate(), is(not(equalTo(d))));
 		assertThat(underTest.getDate().getMinutes(), is(15));
 	}
 
 	@Test
 	public void testEqualityOfIncompatibleTypeIsFalse() {
 		assertThat(underTest.equals("string"), is(false));
 	}
 
 	@Test
 	public void testToCsvString() {
 		String expected = String.format("%s, %s, %s", 
 				StringEscapeUtils.escapeCsv(underTest.getDate().toString()), 
 				StringEscapeUtils.escapeCsv(underTest.getAction()), 
 				StringEscapeUtils.escapeCsv(Double.toString(underTest.getDuration())));
 		assertThat(underTest.toCsvString(), is(equalTo(expected)));
 	}
 
 	@Test
 	public void testToJsonString() {
 		Date date = new Date();
 		underTest = new LogEntry("", date, ACTION, DURATION);
 		String expected = String.format("{\"timestamp\":%s,\"action\":\"%s\",\"duration\":%s,\"text\":\"%s\"}", date.getTime(), ACTION, DURATION, "");
 		assertThat(underTest.toJsonString(), is(equalTo(expected)));
 	}
 
 	@Test
 	public void testNullActionArgumentDoesNotCauseHashcodeException() {
 		underTest = new LogEntry(ENTRY, new Date(), null, DURATION);
 		underTest.hashCode();
 	}
 
 	@Test
 	public void testNullDurationArgumentDoesNotCauseHashcodeException() {
 		underTest = new LogEntry(ENTRY, new Date(), ACTION, null);
 		underTest.hashCode();
 	}
 
 	@Test
 	public void testNullMessageArgumentDoesNotCauseHashcodeException() {
 		underTest = new LogEntry(null, new Date(), ACTION, DURATION);
 		underTest.hashCode();
 	}
 }
