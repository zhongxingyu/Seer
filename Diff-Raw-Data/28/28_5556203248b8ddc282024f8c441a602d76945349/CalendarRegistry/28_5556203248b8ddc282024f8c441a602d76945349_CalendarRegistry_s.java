 package gps.tasks.task3663;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.joda.time.Days;
 import org.joda.time.LocalDate;
 
 
 /**
  *  .
  *  Contract
  *
 *  1. All database access must go through registry.
  *  2. Registry needn't know every object in database.
  *  3. Registry must always have the latest info for objects it knows about.
  *
  *  Implications
  *
  *  1. Reg doesn't know every object in database.
  *  2. Reg always has latest info for objects it knows.
  *  3. Reg pulls from database when it doesn't know a requested object.
  *  4. Reg must pull from database to get list of all objects matching a pattern.
  *      - needn't pull full object
  *  5. When it pushes to the database, reg must also update itself.
  */
 public class CalendarRegistry
 {
   static QueryDb q = new QueryDb();
   static ModifyDb mod = new ModifyDb();
 
   static Map<Integer, Map<String, String>> eventBeans = new HashMap<Integer, Map<String, String>>();
   static Map<Integer, Map<String, String>> guestBeans = new HashMap<Integer, Map<String, String>>();
 
 
 //------------------------------------------------------------------------------
 //  cache helpers
 //------------------------------------------------------------------------------
 
 
 // {{{ auto generated with genCacheHelpers.py
   /**
    * Populate map of event beans from db.
    *
    *  @param ids   event ids to fetch from database
    *  @throws SQLException
    */
   public static void fetchEvents(List<Integer> ids) throws SQLException//{{{
   {
     Map<Integer, Map<String, String>> beanMom = q.getRows("events", ids);
 
     eventBeans.putAll(beanMom);
   }//}}}
 
   /**
    *  Returns a list of "unregistered" event ids.
    *
    *  @param ids  List of event ids to check
    *  @throws SQLException
    */
   public static List<Integer> unregEvents(List<Integer> ids) throws SQLException//{{{
   {
     List<Integer> missingIds = new ArrayList<Integer>();
 
     //  if not in beanlist store in list
     for (Integer id : ids)
     {
       if (!eventBeans.containsKey(id))
       {
         missingIds.add(id);
       }
     }
 
     return missingIds;
   }//}}}
 
   /**
    *  Updates list of event beans with any it doesn't have.
    *
    *  @param ids   list of event ids
    *  @throws SQLException
    */
   public static void updateEventBeans(List<Integer> ids) throws SQLException//{{{
   {
     // find any "unregistered" ids
     List<Integer> missing = unregEvents(ids);
 
     //  if there were any missing, hit the db and add them
     if (!missing.isEmpty())
     {
       fetchEvents(missing);
     }
   }//}}}
 
   /**
    * Populate map of guest beans from db.
    *
    *  @param ids   guest ids to fetch from database
    *  @throws SQLException
    */
   public static void fetchGuests(List<Integer> ids) throws SQLException//{{{
   {
     Map<Integer, Map<String, String>> beanMom = q.getRows("guests", ids);
 
     guestBeans.putAll(beanMom);
   }//}}}
 
   /**
    *  Returns a list of "unregistered" guest ids.
    *
    *  @param ids  List of guest ids to check
    *  @throws SQLException
    */
   public static List<Integer> unregGuests(List<Integer> ids) throws SQLException//{{{
   {
     List<Integer> missingIds = new ArrayList<Integer>();
 
     //  if not in beanlist store in list
     for (Integer id : ids)
     {
       if (!guestBeans.containsKey(id))
       {
         missingIds.add(id);
       }
     }
 
     return missingIds;
   }//}}}
 
   /**
    *  Updates list of guest beans with any it doesn't have.
    *
    *  @param ids   list of guest ids
    *  @throws SQLException
    */
   public static void updateGuestBeans(List<Integer> ids) throws SQLException//{{{
   {
     // find any "unregistered" ids
     List<Integer> missing = unregGuests(ids);
 
     //  if there were any missing, hit the db and add them
     if (!missing.isEmpty())
     {
       fetchGuests(missing);
     }
   }//}}}
 
 // }}} END auto generated with genCacheHelpers.py
 
 
 //------------------------------------------------------------------------------
 //  misc
 //------------------------------------------------------------------------------
 
   /**
    *  Sends bean to db.
    *
    *  @param type   type of bean: guest || event
   *  @param bean   bean to save
    *  @throws SQLException
    */
   public static void send(String type, Map<String, String> map) throws SQLException//{{{
   {
     if      (type.equals("event"))  mod.modRow("events",  map);
     else if (type.equals("guest"))  mod.modRow("guests",  map);
     else                            mod.modRow(type,      map);
   }//}}}
 
   /**
    *  Composite method to send() bean to db, then fetchEvents() or fetchGuests()
   *  to update cache
    *
    *  @param type   type of bean: guest || event
    *  @param bean   bean to save
    *  @throws SQLException
    */
  public static void save(String type, Map<String, String> map) throws SQLException//{{{
   {
     Integer id = Integer.valueOf(map.get("id"));
     if (id < 0)
     {
       id = q.nextId(type);
     }
     send(type, map);
     if (type.equals("events"))  fetchEvents(Arrays.asList(id));
     else                        fetchGuests(Arrays.asList(id));
   }//}}}
 
 
 
 //------------------------------------------------------------------------------
 //  event methods for ui
 //------------------------------------------------------------------------------
 
   /**
    *  Returns a map of (eid, 50-char description).
    *
    *  Also updates registry with any unknown eids.
    *
    *  @param eids   List of event ids to make taglines from
    *  @throws SQLException
    */
   public static Map<Integer, String> getEventTaglines(List<Integer> eids) throws SQLException//{{{
   {
     Map<Integer, String> taglines = new HashMap<Integer, String>();
 
     updateEventBeans(eids);
 
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
    *  Returns an event bean for specific event id.
    *
    *  Also updates registry if eid is unknown.
    *
    *  @throws SQLException
    */
   public static Event getEvent(Integer eid) throws SQLException//{{{
   {
     updateEventBeans(Arrays.asList(eid));
 
     Map<String, String> m = eventBeans.get(eid);
 
     Event event = new Event(eid);
     event.setDay(         m.get("day"));
     event.setDescription( m.get("description"));
     event.setGuests(      m.get("guests"));
     event.setKind(        m.get("kind"));
     event.setTimeEnd(     m.get("timeEnd"));
     event.setTimeStart(   m.get("timeStart"));
 
     return event;
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
    *  Returns a list of eventful days for date range.
    *
    *  Also caches any unknown beans
    *
    *  @throws SQLException
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
 
   /**
    *  Returns a list of event ids for a date.
    *
    *  Also updates event bean list as needed.
    *
    *  @throws SQLException
    */
   public static List<Integer> getEventsFor(LocalDate ld) throws SQLException//{{{
   {
     // hit database for list of ids
     List<Integer> ids = q.getEvents_these(Arrays.asList(ld.toString()));
 
     // update event bean list as needed
     updateEventBeans(ids);
 
     return ids;
   }//}}}
 
 
 
 //------------------------------------------------------------------------------
 //  guest methods for ui
 //------------------------------------------------------------------------------
 
   /**
    *
    *  @param gids
    *  @return
    *  @throws SQLException
    */
   public static Map<Integer, String> getGuestNames(List<Integer> ids) throws SQLException
   {
     Map<Integer, String> names = new HashMap<Integer, String>();
 
     updateGuestBeans(ids);
 
     for (Integer id : ids)
     {
       String s = guestBeans.get(id).get("name");
       names.put(id, s);
     }
 
     return names;
   }
 
   /**
    *
    *  @return
    *  @throws SQLException
    */
   public static List<Integer> getGuests() throws SQLException
   {
     // hit database for list of ids
     List<Integer> ids = q.getGuests();
 
     // update guest bean list as needed
     updateGuestBeans(ids);
 
     return ids;
   }
 }
