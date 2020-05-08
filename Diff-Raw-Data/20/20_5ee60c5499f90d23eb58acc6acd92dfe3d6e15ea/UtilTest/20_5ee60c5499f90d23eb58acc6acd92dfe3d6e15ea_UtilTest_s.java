 package ru.develbureau.mrtesting.util;
 
 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.Matchers.*;
 
 import org.testng.annotations.Test;
 import ru.develbureau.mrtesting.parser.ApacheLogParser;
 import ru.develbureau.mrtesting.parser.ParserException;
 
 import java.util.Calendar;
 
 /**
  * User: sergey.sheypak
  * Date: 23.03.13
  * Time: 16:35
  */
 public class UtilTest {
 
     private final ApacheLogParser  parser = new ApacheLogParser();
 
     @Test
     public void trimTimestamps() throws ParserException {
        verifyResult(Util.trimMinutes(parser.parseTimestamp("03/Jul/1995:10:49:40")), 1995, 06, 03, 10);
        verifyResult(Util.trimMinutes(parser.parseTimestamp("03/Jul/1995:01:00:40")), 1995, 06, 03, 01);
        verifyResult(Util.trimMinutes(parser.parseTimestamp("31/Dec/1995:00:00:00")), 1995, 11, 31, 00);
     }
 
     private void verifyResult(long result, int year, int month, int day, int hour){
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeInMillis(result);
 
         assertThat(calendar.get(Calendar.YEAR), equalTo(year));
         assertThat(calendar.get(Calendar.MONTH), equalTo(month));
         assertThat(calendar.get(Calendar.DAY_OF_MONTH), equalTo(day));
         assertThat(calendar.get(Calendar.HOUR_OF_DAY), equalTo(hour));
 
         assertThat(calendar.get(Calendar.MINUTE), equalTo(0));
         assertThat(calendar.get(Calendar.SECOND), equalTo(0));
         assertThat(calendar.get(Calendar.MILLISECOND), equalTo(0));
     }
 }
