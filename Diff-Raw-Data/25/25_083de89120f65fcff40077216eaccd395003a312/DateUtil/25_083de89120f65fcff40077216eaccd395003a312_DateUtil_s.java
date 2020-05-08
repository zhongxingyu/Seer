 package by.bsu.courseproject.util;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * User: Artyom Strok
  * Date: 25.03.13
  * Time: 22:49
  */
 public class DateUtil {
  public static final String DATE_FORMAT = "yyyy-MM-dd";
   public static final int DEFAULT_OFFSET = 30;
 
   public static Date stringToDate(String date) {
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
     Date result = null;
     try {
       result = dateFormat.parse(date);
     } catch (ParseException e) {
       System.out.println("Parse date error");
     }
     return result;
   }
 
   public static String dateToString(Date date) {
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
     return dateFormat.format(date);
   }
 
   public static Date getCurrentDateWithDefaultOffset() {
     Calendar calendar = Calendar.getInstance();
     calendar.add(Calendar.DAY_OF_MONTH, DEFAULT_OFFSET);
     return new Date(calendar.getTimeInMillis());
   }
 
 }
