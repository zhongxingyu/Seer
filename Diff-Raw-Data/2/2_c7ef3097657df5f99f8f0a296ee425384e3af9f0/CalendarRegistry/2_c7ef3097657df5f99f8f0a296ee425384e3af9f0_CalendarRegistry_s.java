 package gps.tasks.task3663;
 
 import java.util.Collection;
 import java.sql.SQLException;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.joda.time.Days;
 import org.joda.time.LocalDate;
 
 public class CalendarRegistry
 {
   static QueryDb q = new QueryDb();
   static ModifyDb mod = new ModifyDb();
 
   static Map<Integer, Map<String, String>> eventBeans = new HashMap<Integer, Map<String, String>>();
   static Map<Integer, Map<String, String>> guestBeans = new HashMap<Integer, Map<String, String>>();
 
   /**
    * Populate map of event beans from db
    *
    *  @param eids   event ids to fetch from database
    *  @throws SQLException
    */
   public static void fetchEvents(List<Integer> eids) throws SQLException//{{{
   {
     Map<Integer, Map<String, String>> beanMom = q.getRows("events", eids);
 
     eventBeans.putAll(beanMom);
   }//}}}
 
   /**
    * Populate map of guest beans from db
    *
    *  @param gids   guest ids to fetch from database
    *  @throws SQLException
    */
   public static void fetchGuests(List<Integer> gids) throws SQLException//{{{
   {
     Map<Integer, Map<String, String>> beanMom = q.getRows("guests", gids);
 
     guestBeans.putAll(beanMom);
   }//}}}
 
   /**
    *  Sends bean to db.
    *
    *  @param type   type of bean: guest || event
    *  @param bean   bean to save
    *  @throws SQLException
    */
   public static void send(String type, Map<String, String> bean) throws SQLException//{{{
   {
     if      (type.equals("event"))  mod.modRow("events",  bean);
     else if (type.equals("guest"))  mod.modRow("guests",  bean);
     else                            mod.modRow(type,      bean);
   }//}}}
 
   /**
    *  Composite method to save() bean to db, then fetchEvents() to update cache
    *
    *  @param type   type of bean: guest || event
    *  @param bean   bean to save
    *  @throws SQLException
    */
   public static void save(String type, Map<String, String> bean) throws SQLException//{{{
   {
     send(type, bean);
     fetchEvents(Arrays.asList(Integer.valueOf(bean.get("id"))));
   }//}}}
 
   /**
    *  Returns a list of "unregistered" event ids.
    *
    *  @param eids  List of event ids to check
    *  @throws SQLException
    */
   public static List<Integer> unregEvents(List<Integer> eids) throws SQLException//{{{
   {
     List<Integer> missingEids = new ArrayList<Integer>();
 
     //  if not in beanlist store in list
     for (Integer id : eids)
     {
       if (!eventBeans.containsKey(id))
       {
         missingEids.add(id);
       }
     }
 
     return missingEids;
   }//}}}
 
   /**
    *  Updates list of event beans with any it doesn't have.
    *
    *  @param eids   list of event ids
    * @throws SQLException
    */
   public static void updateEventBeans(List<Integer> eids) throws SQLException//{{{
   {
     // find any "unregistered" eids
     List<Integer> missing = unregEvents(eids);
 
     //  if there were any missing, hit the db and add them
     if (!missing.isEmpty())
     {
       fetchEvents(missing);
     }
   }//}}}
 
   /**
    *  Returns a map of (eid, 50-char description).
    *
    *  @param eids   List of event ids to make taglines from
    *  @throws SQLException
    */
   public static Map<Integer, String> getEventTaglines(List<Integer> eids) throws SQLException//{{{
   {
     Map<Integer, String> taglines = new HashMap<Integer, String>();
 
     // find any "unregistered" eids
     List<Integer> missing = unregEvents(eids);
 
     //  if there were any missing, hit the db and add them
     if (!missing.isEmpty())
     {
       fetchEvents(missing);
     }
 
     for (Integer id : eids)
     {
       // ensure tagline is less than 50 chars
       String description = eventBeans.get(id).get("description");
       if (description.length() > 50)
       {
         description = description.substring(0,50);
       }
 
       taglines.put(id, description);
     }
 
     return taglines;
   }//}}}
 
   /**
   *  Returns an event bean for specific day
    *
    *  @throws SQLException
    */
   public static Map<String, String> getEvent(Integer eid) throws SQLException//{{{
   {
     // find any "unregistered" eids
     List<Integer> missing = unregEvents(Arrays.asList(eid));
 
     //  if there were any missing, hit the db and add them
     if (!missing.isEmpty())
     {
       fetchEvents(missing);
     }
 
     return eventBeans.get(eid);
   }//}}}
 
   /**
    *  Returns a list of days for date range. Inclusive of start and end.
    *
    *  @param start  in form yyyy-mm-dd
    *  @param end    in form yyyy-mm-dd
    */
   public static List<LocalDate> getDatesInRange(LocalDate start, LocalDate end)//{{{
   {
     List<LocalDate> dates = new ArrayList<LocalDate>();
 
     Integer days = Days.daysBetween(start, end).getDays();
 
     for (Integer i = 0; i <= days; i++)
     {
         LocalDate d = start.plusDays(i);
         dates.add(d);
     }
 
     return dates;
   }//}}}
 
   /**
    *  Returns a list of eventful days for date range
    * @throws SQLException
    */
   public static List<LocalDate> eventDatesFor(LocalDate start, LocalDate end) throws SQLException//{{{
   {
     // return this eventually
     List<LocalDate> eventDays = new ArrayList<LocalDate>();
 
     // get a list of the dates from start to end
     List<LocalDate> range = getDatesInRange(start, end);
 
     // convert list of joda dates to list of string dates
     List<String> stringRange = new ArrayList<String>();
     for (LocalDate d : range)
     {
       stringRange.add(d.toString());
     }
 
     // hit db to get list of event ids
     List<Integer> eids = q.getEvents_these(stringRange);
 
     // update cache as needed
     updateEventBeans(eids);
 
     // loop through event bean list looking for events within range
     // dupe positives are okay
     for (Integer k : eventBeans.keySet())
       for (LocalDate d : range)
       {
         if (eventBeans.get(k).containsValue(d.toString()))
         {
           eventDays.add(d);
         }
       }
 
     return eventDays;
   }//}}}
 }
