 package loyalitymonitor;
 
 import org.apache.pig.EvalFunc;
 import org.apache.pig.data.Tuple;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 /**
  * Roundup to minutes, i.e. 10:22 will be round up to 10:30 with roundup value 15
  */
 public class TimestampRoundUp extends EvalFunc<String> {
     final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
 
     private SimpleDateFormat inputDateFormat;
     private SimpleDateFormat outDateFormat;
 
     private int roundUpTo;
 
    public TimestampRoundUp(String minutes) {
        this.roundUpTo = Integer.parseInt(minutes);
 
         this.inputDateFormat = new SimpleDateFormat(TWITTER);
         this.inputDateFormat.setLenient(true);
         this.inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
         this.outDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
         this.outDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     }
 
     @Override
     public String exec(Tuple input) throws IOException {
         if (input == null || input.size() == 0) {
             return null;
         }
 
         Date dt = null;
         String inputLine = (String)input.get(0);
         try {
             dt = inputDateFormat.parse(inputLine);
         } catch (ParseException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return null;
         }
         Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
         calendar.setTime(dt);   // assigns calendar to given date
 
 
         int minutes = calendar.get(Calendar.MINUTE);
         int interval = 60/roundUpTo;
         for(int i = 0; i <= interval; i++) {
             if( i*roundUpTo > minutes ) {
                 interval = (i-1)*roundUpTo;
                 break;
             }
         }
         calendar.set(Calendar.MINUTE, interval);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
 
         return outDateFormat.format( calendar.getTime() );
     }
 }
