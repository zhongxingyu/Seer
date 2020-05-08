 package wielwijk;
 import java.util.*;
 
 /**
  *
  * @author jeroen
  */
 public class ActivityContainer {
     private static ArrayList<Activity> activities = new ArrayList<Activity>();
     
     /**
      *
      * @param hike
      */
    public static void addActivity(Hike hike) {
        activities.add(hike);
     }
 
     /**
      *
      * @param activity
      */
     public static void removeActivity(Activity activity) {
         Wielwijk.db.exec("REMOVE FROM activities WHERE id = " + activity.getId());
     }
 
     /**
      *
      * @param activity
      */
     public static List findActivity(Activity activity) {
         List l = Wielwijk.db.query("SELECT * FROM activities WHERE id = " + activity.getId());
 
         return l;
     }
 
     /**
      * 
      * @param id
      */
     public static Activity getActivityById(int id) {
         List res = Wielwijk.db.query("SELECT * FROM activities WHERE id = " + id);
         if (res.size() == 0) {
             return null;
         }
 
         java.util.Map<String, Object> map = (HashMap<String, Object>) res.get(0);
 
         Date datetimebegin = new Date((Long) map.get("datetime_begin"));
         Date datetimeend = new Date((Long) map.get("datetime_end"));
         
         return new Activity((String) map.get("name"), (String) map.get("location"), (String) map.get("destination"), datetimebegin, datetimeend,
                 (Integer) map.get("fee"), (Integer) map.get("lower_user_limit"), (Integer) map.get("upper_user_limit"),
                map.get("lower_limit_date").toString(), (Boolean) map.get("cancelled"), (Long) map.get("id"));
     }
 
     public static Activity getActivityByDay(int year, int month, int day) {
         List res = Wielwijk.db.query("SELECT * FROM activities WHERE "
                 + "FROM_UNIXTIME(datetime_begin, '%Y') = '" + year
                 + "' AND FROM_UNIXTIME(datetime_begin, '%c') = '" + month
                 + "' AND (FROM_UNIXTIME(datetime_begin, '%d') = '" + day 
                 + "' OR " + "FROM_UNIXTIME(datetime_begin, '%d') = '0" + day 
                 + "')");
         if (res.size() == 0) {
             return null;
         }
 
         java.util.Map<String, Object> map = (HashMap<String, Object>) res.get(0);
 
         Date datetimebegin = new Date((Long) map.get("datetime_begin"));
         Date datetimeend = new Date((Long) map.get("datetime_end"));
 
         return new Activity((String) map.get("name"), (String) map.get("location"), (String) map.get("destination"), datetimebegin, datetimeend,
                 (Integer) map.get("fee"), (Integer) map.get("lower_user_limit"), (Integer) map.get("upper_user_limit"),
                map.get("lower_limit_date").toString(), (Boolean) map.get("cancelled"), (Long) map.get("id"));
     }
 }
