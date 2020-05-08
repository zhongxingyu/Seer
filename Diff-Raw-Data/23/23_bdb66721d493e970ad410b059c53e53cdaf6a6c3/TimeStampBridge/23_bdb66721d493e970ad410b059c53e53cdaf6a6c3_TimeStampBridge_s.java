 package nl.surfnet.bod.util;
 
 import java.sql.Timestamp;
 
 import org.hibernate.search.bridge.StringBridge;
 import org.joda.time.LocalDateTime;
 
 /**
  * Handles parsing of timestamps to a String so it can be searched. Needed since
  * BoD uses joda timestamps and sql time stamps.
  * 
  */
 public class TimeStampBridge implements StringBridge {
 
   @Override
   public String objectToString(Object object) {
     String result = null;
 
    if (LocalDateTime.class.isAssignableFrom(object.getClass())) {
       LocalDateTime localDateTime = (LocalDateTime) object;
       result = localDateTime.toString();
     }
     else if (Timestamp.class.isAssignableFrom(object.getClass())) {
       Timestamp timestamp = (Timestamp) object;
       result = timestamp.toString();
     }
     else {
       throw new IllegalArgumentException("Bridge is not suitable for handling objects of type: " + object);
     }
 
     return result;
   }
 
 }
