 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Information about bus stops. Real bus stops within the GMPTE area are
  * identified in a systematic way, but this is not the case for all the bus
  * stops within the scope of the the pilot IBMS as some of them are outside
  * the GMPTE area.<br><br>
  * 
  * Bus stops in the database are identified by an area and a name, 
  * e.g. "Hayfield, Bus Station" which is enough to uniqiely identify them.
  * Areas also have shorter codes, we.g. "HAY"<br><br>
  * 
  * Some bus stops are timing points, meaning that they appear on the timetable
  * associated with service times.<br><br>
  * 
  * None of the UCs in the pilot IBMS change bus stop information, so this
  * interface provides read-only access
  */
 public class BusStopInfo 
 {
   
   // This class is not intended to be instantiated
   private BusStopInfo()
   { 
   }
 
   /**
    * Get the IDs of all the routes within the scope of the pilot IBMS
    */
   public static int[] getRoutes()
   {
     return database.busDatabase.select_ids("route_id", "route", "name");
   }
 
   /**
    * Find a route with a given name. 
    * @param name, the name of a route within the scope of the IBMS.
    * These can be found using getRouteName of each of the route IDs given
    * by getAllRoutes
    */
   public static int findRoute(String name)
   {
     return database.busDatabase.find_id("route", "name", name);
   }
 
   /**
    * Get the name of a route
    * @return the name of one of the routes within the scope of the IBMS
    */
   public static String getRouteName(int route)
   {
     if (route == 0) throw new InvalidQueryException("Nonexistent route");
     return database.busDatabase.get_string("route", route, "name");
   }
 
   /**
    * Get the routes which visit a particular bus stop
    */
   public static int[] getRoutes(int busStop)
   {
     if (busStop == 0) throw new InvalidQueryException("Nonexistent bus stop");
     return database.busDatabase.select_ids("route", "path", "bus_stop", busStop, "");
   }
 
   /**
    * Get all the bus stops on the route which are in the database
    * This currently does not include intermediate stops between timing points
    */
   public static int[] getBusStops(int route)
   {
     if (route == 0) throw new InvalidQueryException("Nonexistent route");
     return database.busDatabase.select_ids("bus_stop", "path", "route", route, "sequence");
   }
 
   /**
    * Find a bus stop with a particular area code and name
    */
   public static int findBusStop(String areaCode, String name)
   {
     String source = database.join("bus_stop", "area", "area");
     return database.busDatabase.find_id("bus_stop_id", source, "area.code", areaCode, "bus_stop.name", name);
   }
 
   /**
    * Get the full name of a bus stop in the format 'areaname,stopname', eg
    * "Marple,Navigation"
    */
   public static String getFullName(int busStop)
   {
     if (busStop == 0) throw new InvalidQueryException("Nonexistent bus stop");
     database db = database.busDatabase;
     if (db.select_record("area.name, name", database.join("bus_stop", "area", "area"), "bus_stop_id", busStop))
       return (String)db.get_field("area.name") + ", " + (String)db.get_field("name");
     else
       return "";
   }
 
   /**
    * Get al the bus stops in a given area
    */
   public static int[] getBusStopsInArea(int area)
   {
     if (area == 0) throw new InvalidQueryException("Nonexistent area");
     return database.busDatabase.select_ids("bus_stop_id", "bus_stop", "area", area, "name");
   }
 
   /**
    * Get the next stop for a stop on a particular toute. Returns 0 if there
    * is no such stop.
    */
   public static int getNextStop(int busStop, int route)
   {
     if (busStop == 0) throw new InvalidQueryException("Nonexistent bus stop");
     if (route   == 0) throw new InvalidQueryException("Nonexistent route");
     database db       = database.busDatabase;
     String   source   = "path As path1 Inner Join path As path2 On (path1.sequence + 1 = path2.sequence)";
     String   criteria = "path1.route = " + route + " And path2.route = " + route + " And path1.bus_stop = " + busStop;
     db.select("path2.bus_stop", source, criteria, "");
     if (db.move_first()) return (Integer)db.get_field("path2.bus_stop"); else return 0;
   }
 
    /**
    * Get the previous stop for a stop on a particular toute. Returns 0 if there
    * is no such stop.
    */
   public static int getPreviousStop(int busStop, int route)
   {
     if (busStop == 0) throw new InvalidQueryException("Nonexistent bus stop");
     if (route   == 0) throw new InvalidQueryException("Nonexistent route");
     database db       = database.busDatabase;
     String   source   = "path As path1 Inner Join path As path2 On (path1.sequence - 1 = path2.sequence)";
     String   criteria = "path1.route = " + route + " And path2.route = " + route + " And path1.bus_stop = " + busStop;
     db.select("path2.bus_stop", source, criteria, "");
     if (db.move_first()) return (Integer)db.get_field("path2.bus_stop"); else return 0;
   }
 
   /**
    * Determine whethee a bus stop is a timing point somewhere on the timetable
    */
   public static boolean isTimingPoint(int busStop)
   {
     if (busStop == 0) throw new InvalidQueryException("Nonexistent bus stop");
     return database.busDatabase.record_count("*", "timetable_line", "timing_point", busStop) > 0;
   }
 
   /**
    * Determine whether a bus stop is a timeing point on the given route
    */
   public static boolean isTimingPointOnRoute(int busStop, int route)
   {
     if (busStop == 0) throw new InvalidQueryException("Nonexistent bus stop");
     if (route   == 0) throw new InvalidQueryException("Nonexistent route");
     String source = database.join("timetable_line", "daily_timetable", "daily_timetable");
     return database.busDatabase.record_count("*", source, "timetable_line.timing_point", busStop, "daily_timetable.route", route) > 0;
   }
 
   /**
    * Get the IDs of all the area codes
    */
   public static int[] getAreas()
   {
     return database.busDatabase.select_ids("area_id", "area", "name");
   }
 
   /**
    * Get the code for a given area, e.g. "MAR"
    */
   public static String getAreaCode(int area)
   {
     if (area == 0) throw new InvalidQueryException("Nonexistent area");
     return database.busDatabase.get_string("area", area, "code");
   }
 
    /**
    * Get the name for a given area, e.g. "Marple"
    */
   public static String getAreaName(int area)
   {
     if (area == 0) throw new InvalidQueryException("Nonexistent area");
     return database.busDatabase.get_string("area", area, "name");
   }
 
   /**
    * Get the ID of the area with the given code
    */
   public static int findArea(String code)
   {
     return database.busDatabase.find_id("area", "code", code);
   }
 
   /**
    * Get the ID of the area with the given name
    */
   public static int findAreaByName(String name)
   {
     return database.busDatabase.find_id("area", "name", name);
   }
   
   /**
    * Get all the bus stops ids and names.
    */
   public static String[] getBusStops()
   {
     int[] id = database.busDatabase.select_ids("bus_stop_id", "bus_stop", "bus_stop_id");
     String[] name = new String[id.length];
     String[] result = new String[id.length];
     for (int i = 0; i<id.length; i++)
     	name[i] = database.busDatabase.get_string("bus_stop", id[i], "name");
     for (int i = 0; i<id.length; i++)
     	result[i] = id[i] + " " + name[i];
     return result;
   }
   
   /* 
    * Show next 5 buses arriving at this bus stop. 
    */
   @SuppressWarnings("deprecation")
 	public static String[] display5buses(int stop)
   {
   	//database.busDatabase.select("service, time", "timetable_line", "", "time");
     int[] service = database.busDatabase.select_ids("service", "timetable_line", "timing_point", stop, "time");
     int[] time = database.busDatabase.select_ids("time", "timetable_line", "timing_point", stop, "time");
     
     DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
     Date date = new Date();
     int hours = date.getHours();
     int minutes = date.getMinutes();
     
     int timeAsInDatabase = hours * 60 + minutes + 1;
     //System.out.println(timeAsInDatabase);
     
     int currentIndex = 0;
     
     int counter = 0;
     while (counter < service.length)
     {
     	if (time[counter] < timeAsInDatabase)
     	{
     	  counter++;
     	}
     	else
     	{
     		currentIndex = counter;
     		counter = service.length; // to break
     	}//else
     
     }//while
     
     int min = service.length;
     if (5 < min)
     	min = 5;
     
     String[] result = new String[min];
     
     for (int j=0; j<min; j++){
       String  mins = "" + time[j+currentIndex]%60;
 
       if(mins.length() < 2) mins = "0" + mins;
 
   	  result[j] = service[j+currentIndex] + "           " + time[j+currentIndex]/60 + ":" 
                                          + mins;
     }
 
      
     return result;
     
   }//display5Buses
   
   /* 
    * Show next 5 buses arriving at this bus stop. 
    */
 	public static int[] displayNextBus(int stop, int theTime)
   {
   	//database.busDatabase.select("service, time", "timetable_line", "", "time");
     int[] service = database.busDatabase.select_ids("service", "timetable_line", "timing_point", stop, "time");
     int[] time = database.busDatabase.select_ids("time", "timetable_line", "timing_point", stop, "time");
     
     int timeAsInDatabase = theTime;
     //System.out.println(timeAsInDatabase);
     
     int currentIndex = 0;
     
     int counter = 0;
     while (counter < service.length)
     {
     	if (time[counter] < timeAsInDatabase)
     	{
     	  counter++;
     	}
     	else
     	{
     		currentIndex = counter;
     		counter = service.length; // to break
     	}//else
     
     }//while
     
     int min = service.length;
     if (5 < min)
     	min = 5;
     
     String[] result = new String[min];
     
     for (int j=0; j<min; j++)
   	  result[j] = service[j+currentIndex] + " " + time[j+currentIndex];
     
     if(result.length<1)
     	throw new InvalidQueryException("There is no buses available at that time.");
     String[] parts = result[0].split(" ");
 		int theService = Integer.parseInt(parts[0]);
 		int theTimeInt = Integer.parseInt(parts[1]);
 		
 		
 		int[] resultInt = {theService, theTimeInt};
     return resultInt;
     
   }//display5Buses
 
   /* 
    * Show next 5 buses arriving at this bus stop. 
    */
   @SuppressWarnings("deprecation")
 	public static int getService(int stop, int theTime)
   {
   	//database.busDatabase.select("service, time", "timetable_line", "", "time");
     int[] service = database.busDatabase.select_ids("service", "timetable_line", "timing_point", stop, "time");
     int[] time = database.busDatabase.select_ids("time", "timetable_line", "timing_point", stop, "time");
 
     
     int timeAsInDatabase = theTime;
     //System.out.println(timeAsInDatabase);
     
     int currentIndex = 0;
     
     int counter = 0;
     while (counter < service.length)
     {
     	if (time[counter] < timeAsInDatabase)
     	{
     	  counter++;
     	}
     	else
     	{
     		currentIndex = counter;
     		counter = service.length; // to break
     	}//else
     
     }//while
     
     return service[currentIndex];
   }//display5Buses
   
   public static int getTime(int service, int stop)
   {
   	int result = 0;
     int[] stops = database.busDatabase.select_ids("timing_point", "timetable_line", "service", service, "time");
     int[] time = database.busDatabase.select_ids("time", "timetable_line", "service", service, "time");
     for(int i = 0; i < stops.length; i++)
     {
     	if(stops[i]==stop)
     		result = time[i];
     }
     return result;
   }
 
 }//class
