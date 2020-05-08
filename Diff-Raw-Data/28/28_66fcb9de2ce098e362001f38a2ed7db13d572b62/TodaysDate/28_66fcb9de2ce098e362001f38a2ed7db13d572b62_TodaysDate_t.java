 package streamfish;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import org.joda.time.*;
 
 /**
  *
  * @author NorC
  */
 public class TodaysDate {
 
     public TodaysDate() {
     }
 
     public static String getDate() {
         TimeZone tz = TimeZone.getTimeZone("Europe/Oslo");
         Calendar currentDate = Calendar.getInstance(tz, Locale.ENGLISH); //Get the current date
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); //format it as per your requirement
         String dateNow = formatter.format(currentDate.getTime());
 
         return dateNow;
     }
     
     public int isDateValid(String date) {
         String today = getDate();
         
         today = today.replaceAll("-", "");
         date = date.replaceAll("-", "");
       
         String todayYear = today.substring(0, 4);
         String todayMonth = today.substring(4, 6);
         String todayDay = today.substring(6, today.length());
         String deliveryYear = date.substring(0, 4);
         String deliveryMonth = date.substring(4, 6);
         String deliveryDay = date.substring(6, date.length());
         
         if (todayMonth.startsWith("0")) {
             todayMonth = todayMonth.substring(1);
         } else if (todayDay.startsWith("0")) {
             todayDay = todayDay.substring(1);
         } else if (deliveryMonth.startsWith("0")) {
             deliveryMonth = deliveryMonth.substring(1);
         } else if (deliveryDay.startsWith("0")) {
             deliveryDay = deliveryDay.substring(1);
         }
         
         today = todayYear + todayMonth + todayDay;
         date = deliveryYear + deliveryMonth + deliveryDay;
         
         int todayInt = Integer.parseInt(today);
         int dateInt = Integer.parseInt(date);
         
         if (todayInt > dateInt) {
             return -1;      // Not acceptable
         }
         if (today.equals(date)) {
             return 0;       // Can be accepted by user-verification
         }
         return 1;           // Accepted
     }
     
     public int getCurrentYear() {
         TimeZone tz = TimeZone.getTimeZone("Europe/Oslo");
         Calendar currentDate = Calendar.getInstance(tz, Locale.ENGLISH); //Get the current date
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy"); //format it as per your requirement
         String dateNow = formatter.format(currentDate.getTime());
         int year = Integer.parseInt(dateNow);
         return year;
     }
     
     public static DateTime createDateTime(String date){
         int year = Integer.parseInt(date.substring(0, 4));
         System.out.println(year);
         int month = Integer.parseInt(date.substring(5, 7));
         System.out.println(month);
         int day = Integer.parseInt(date.substring(8, 10));
         System.out.println(day);
         DateTime dateTime = new DateTime(year,month,day,0,0);
         return dateTime;
         
     }
     public static int diffDates(DateTime dateStart, DateTime dateStop){
         
         
         Days d = Days.daysBetween(dateStart, dateStop);
         int result = d.getDays();
         return result;
     }
 	
 	public static String getADate(int days){
 		TimeZone tz = TimeZone.getTimeZone("Europe/Oslo");
         Calendar currentDate = Calendar.getInstance(tz, Locale.ENGLISH); //Get the current date
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); //format it as per your requirement
         currentDate.add(Calendar.DATE, days);
 		String date = formatter.format(currentDate.getTime());
 
         return date;
 	}
 
     public String getDateDB() {
         TimeZone tz = TimeZone.getTimeZone("Europe/Oslo");
         Calendar currentDate = Calendar.getInstance(tz, Locale.ENGLISH);
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
         String dateNow = formatter.format(currentDate.getTime());
 
         return dateNow;
     }
 	
 	public static String getADateAddMonth(int month){
 		TimeZone tz = TimeZone.getTimeZone("Europe/Oslo");
         Calendar currentDate = Calendar.getInstance(tz, Locale.ENGLISH); //Get the current date
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); //format it as per your requirement
         currentDate.add(Calendar.MONTH, month);
 		String date = formatter.format(currentDate.getTime());
 
         return date;
 	}
 
     public static int getDay() {
         Calendar currentDate = Calendar.getInstance(Locale.ENGLISH);
         SimpleDateFormat formatter = new SimpleDateFormat("EEE");
         String day = formatter.format(currentDate.getTime());
         int today;
         if (day.equals("ma") || (day.equals("mo"))) {
             today = 0;
         } else if (day.equals("ti") || (day.equals("ty") || day.equals("tu"))) {
             today = 1;
         } else if (day.equals("on") || (day.equals("we"))) {
             today = 2;
         } else if (day.equals("to") || (day.equals("th"))) {
             today = 3;
         } else if (day.equals("fr")) {
             today = 4;
         } else if (day.equals("lø") || (day.equals("la") || day.equals("sa"))) {
             today = 5;
         } else if (day.equals("sø") || (day.equals("su"))) {
             today = 6;
         } else {
             return -1;
         }
         return today;
     }
 
     public static void main(String[] args) {
 //        String[] timezones = TimeZone.getAvailableIDs();
 //        for (String tz : timezones) {
 //            System.out.println(tz);
 //        }
         TodaysDate td = new TodaysDate();
         System.out.println(Calendar.DAY_OF_WEEK_IN_MONTH);
         System.out.println(td.getDateDB());
         Calendar cal = Calendar.getInstance();
         System.out.println(cal.getTime());
         System.out.println(cal.get(Calendar.DAY_OF_WEEK_IN_MONTH));
         System.out.println(td.getDay());
         DateTime date1 = new DateTime(2013, 04, 29,0,0);
         DateTime date2 = new DateTime(2013, 05, 10,0,0);
         System.out.println("Tester noe:");
         System.out.println(createDateTime("2013-04-29"));
         td.isDateValid("2013-4-30");
     }
 }
