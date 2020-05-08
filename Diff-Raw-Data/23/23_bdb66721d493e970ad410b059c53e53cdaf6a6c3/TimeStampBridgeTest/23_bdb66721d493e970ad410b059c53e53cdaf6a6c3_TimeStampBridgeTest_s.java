 package nl.surfnet.bod.util;
 
 import java.sql.Timestamp;
 import java.util.Date;
 
 import org.joda.time.LocalDateTime;
 import org.junit.Before;
 import org.junit.Test;
 
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

 public class TimeStampBridgeTest {
 
   private TimeStampBridge timeStampBridge;
 
   private Timestamp sqlTimeStamp;
 
   private LocalDateTime localDateTime;
 
   @Before
   public void onSetup() {
     localDateTime = new LocalDateTime().withDate(2012, 9, 17).withTime(16, 40, 0, 0);
     sqlTimeStamp = new Timestamp(localDateTime.toDate().getTime());
     timeStampBridge = new TimeStampBridge();
   }
 
   @Test
   public void shouldHandleJodaLocalDateTime() {
     assertThat(timeStampBridge.objectToString(sqlTimeStamp), is("2012-09-17 16:40:00.0"));
   }
 
   @Test
   public void shouldHandleSqlDateTime() {
     assertThat(timeStampBridge.objectToString(localDateTime), is("2012-09-17T16:40:00.000"));
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowWhenOtherClass() {
     timeStampBridge.objectToString(new Date());
   }
 }
