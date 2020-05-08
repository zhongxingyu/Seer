 package teamwork.goodVibrations;
 
 import java.util.Calendar;
 
 public class Utils
 {  
   public static long getTimeOfDayInMillis()
   {
     Calendar c = Calendar.getInstance();
     
     return //c.get(Calendar.HOUR_OF_DAY)*3600000 +
            //c.get(Calendar.MINUTE)*60000 +
            (c.get(Calendar.SECOND)*1000 +
            c.get(Calendar.MILLISECOND))%30000;
   }
   
   public static long calculateTimeInMillis(int hour, int minute)
   {
     return hour*3600000 + minute*60000;
   }
   
   public static byte getDayOfWeekBitMask(int dayOfWeek)
   {
     int res = 1;
    for(int i = 1; i < dayOfWeek - 1; i++)
     {
       res = res * 2;
     }
     return (byte)res;
   }
   
   
 }
