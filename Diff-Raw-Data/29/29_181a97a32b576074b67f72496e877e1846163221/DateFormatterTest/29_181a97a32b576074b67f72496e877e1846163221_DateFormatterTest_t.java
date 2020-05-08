 package uk.co.eelpieconsulting.common.dates;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Date;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 public class DateFormatterTest {
     
 	private final DateFormatter dateFormatter = new DateFormatter();
	private final Date onceUponATime = new DateTime(2009, 10, 2, 14, 23, 10, 0).toDate();
 	
 	@Test
     public void shouldBeAbleToOutputNiceTimeDeltas() throws Exception {    	
     	DateTime now = new DateTime();
     	
     	DateTime lessThanOneMinuteAgo = now.minusSeconds(30);
     	assertEquals("just now", dateFormatter.timeSince(lessThanOneMinuteAgo.toDate()));
     	
     	DateTime fiveMinutesBefore = now.minusMinutes(5);
         assertEquals("5 minutes ago", dateFormatter.timeSince(fiveMinutesBefore.toDate()));   
        
         DateTime anHourAgo = now.minusHours(1);
         assertEquals("1 hour ago", dateFormatter.timeSince(anHourAgo.toDate()));
         
         DateTime twoHoursAgo = now.minusHours(2);
         assertEquals("2 hours ago", dateFormatter.timeSince(twoHoursAgo.toDate()));
                 
         DateTime oneDayAgo = now.minusDays(1);
         assertEquals("1 day ago", dateFormatter.timeSince(oneDayAgo.toDate()));
         
         DateTime oneWeekAgo = now.minusWeeks(1);
         assertEquals("1 week ago", dateFormatter.timeSince(oneWeekAgo.toDate()));                        
     }
 	
 	@Test
 	public void canGenerateYearFormat() throws Exception {
 		assertEquals("2009", dateFormatter.year(onceUponATime));
 	}
 	
 	@Test
 	public void canGenerateShortDayMonthYearFormat() throws Exception {
 		assertEquals("2 Oct 2009", dateFormatter.dayMonthYear(onceUponATime));
 	}
 	
 	@Test
 	public void canGenerateFullMonthYearFormat() throws Exception {
 		assertEquals("October 2009", dateFormatter.fullMonthYear(onceUponATime));
 	}
 	
 	@Test
 	public void canGenerateDayMonthYearTimeFormat() throws Exception {
 		assertEquals("2 Oct 2009 14:23", dateFormatter.dayMonthYearTime(onceUponATime));
 	}
 	
 	@Test
	public void canGenerateDayMonthYearTimeWithSecondsFormat() throws Exception {
		assertEquals("2 Oct 2009 14:23:10", dateFormatter.dayMonthYearTimeWithSeconds(onceUponATime));
	}
	
	@Test
 	public void canGenerateW3CDateTimeFormattedDate() throws Exception {		
		assertEquals("2009-10-02T14:23:10+0100", dateFormatter.w3cDateTime(onceUponATime));
 	}
 	
 	@Test
 	public void canGenerateYearMonthDayUrlStub() throws Exception {		
 		assertEquals("2009/oct/2", dateFormatter.yearMonthDayUrlStub(onceUponATime));
 	}
 	
 	@Test
 	public void canGenerateYearMonthUrlStub() throws Exception {		
 		assertEquals("2009/oct", dateFormatter.yearMonthUrlStub(onceUponATime));
 	}
 	
 }
