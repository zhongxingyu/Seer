 package daniel.web.http;
 
 import daniel.data.unit.Instant;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.TimeZone;
 
 public final class DateUtils {
   private static final DateFormat rfc1123Format;
   private static final DateFormat iso8601Format;
 
   static {
    rfc1123Format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'");
     rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
 
     iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
     iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
   }
 
   private DateUtils() {}
 
   public static String formatRfc1123(Instant instant) {
     synchronized (rfc1123Format) {
       return rfc1123Format.format(instant.toDate());
     }
   }
 
   public static Instant parseRfc1123(String dateString) throws ParseException {
     synchronized (rfc1123Format) {
       return Instant.fromDate(rfc1123Format.parse(dateString));
     }
   }
 
   public static String formatIso8601(Instant instant) {
     synchronized (iso8601Format) {
       return iso8601Format.format(instant.toDate());
     }
   }
 
   public static Instant parseIso8601(String dateString) throws ParseException {
     synchronized (iso8601Format) {
       return Instant.fromDate(iso8601Format.parse(dateString));
     }
   }
 }
