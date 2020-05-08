 package net.kjonigsen.silenthours;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 import java.util.Date;
 
 /**
  * Created by jostein on 22/09/13.
  */
 public class ApplicationStatusProvider {
 
     private ApplicationStatusProvider()
     {
     }
 
     public static ApplicationStatus getFor(Context context)
     {
        SharedPreferences prefs = context.getSharedPreferences("silentHours", context.MODE_PRIVATE);
 
         boolean enableService = prefs.getBoolean("enable_service", true);
         Date nextDate = getNextApplicationEventDateFor(context);
 
         ApplicationStatus result = new ApplicationStatus();
         result.SilentHoursEnabled = enableService;
         result.NextApplicationEvent = nextDate;
 
         return result;
     }
 
     private static Date getNextApplicationEventDateFor(Context context)
     {
         Date result = new Date();
        result.setTime(result .getTime() + 1);
         return result;
     }
 
 }
