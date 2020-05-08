 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class Someclass 
 {
 
	private HashMap<Service, Driver> rosterDrivers = new HashMap<Service, Driver>();
	private HashMap<Service, Bus> rosterBusses = new HashMap<Service, Bus>();
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
 
 
 	public static int getTotalDuration(TimetableInfo.timetableKind dayType)
 	{
 		Route[] routes = Route.getAll();
 		int thisDuration = 0;
 		for(Route route: routes)
 		{
 			Service[] services = route.getServices(dayType);
 			for(Service service: services)
 				thisDuration += service.getDuration();
 		}
 		return thisDuration;
 	}
 
 	public static void printFullTimetable()
 	{
 		Route[] routes = Route.getAll();
 		for(TimetableInfo.timetableKind dayType: TimetableInfo.timetableKind.values())
 		{
 			for(Route route: routes)
 			{
 				System.out.println();
 				System.out.println("Route: " + route.getName() + ", " + dayType);
 				System.out.println("--------------------------------------------");
 				Service[] services = route.getServices(dayType);
 				for(Service service: services)
 				{
 					System.out.println("Service number: " + service.getID());
 					TimingPoint[] stops = service.getTimingPoints();
 					for(int j=0; j< stops.length; j++)
						System.out.println(BusStopInfo.getFullName(stops[i].getStop()) + ": "
							               + simpleDateFormat.format(stops[i].getTime()));
 					System.out.println("Duration: " + service.getDuration() + " minutes");
 					System.out.println();
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void generateDriverRoster()
 	{
 		Driver drivers[] = Driver.getAll();
 		Bus busses[] = Bus.getAll();
 		Route routes[] = Route.getAll();
 		
 		for(TimetableInfo.timetableKind dayType: TimetableInfo.timetableKind.values())
 		{
 			HashMap<Driver, Integer> minutesWorked = new HashMap<Driver, Integer>();
 			for(Driver driver: drivers)
 				minutesWorked.put(driver, 0);
 			if(dayType == TimetableInfo.timetableKind.weekday)
 			{
 				rosterDrivers.put(new Service(0, routes[0], dayType), drivers[0]);
 				minutesWorked.put(drivers[0], minutesWorked.get(drivers[0]) + 9);
 			}
 			System.out.println(dayType);
 			System.out.println("-----------------------------------------");
 			double workPerDay = (getTotalDuration(dayType)*1.0)/(drivers.length);
 			workPerDay = workPerDay * 0.9;
 			// System.out.println(workPerDay);
 			for (Driver driver: drivers)
 			{
 				Date nextAvailable = null;
 				System.out.print(driver.getID() + ":");
 				Service[] services = routes[0].getServices(dayType);
 				for(Service service: services)
 					{
 						Service serviceback;
 						if(dayType == TimetableInfo.timetableKind.weekday)
 							serviceback = new Service(service.getIndex()+1, routes[1], dayType);
 						else
 							serviceback = new Service(service.getIndex(), routes[1], dayType);
 						// System.out.println("        Trying Service " + service.getID());
 						if((nextAvailable == null) || (service.getTime(0).after(nextAvailable)))
 							if(!rosterDrivers.containsKey(service))
 							{
 								rosterDrivers.put(service, driver);
 								rosterDrivers.put(serviceback, driver);
 								if(nextAvailable == null)
 								{
 									long difference = serviceback.getTime(serviceback.getNumberOfTimingPoints()-1).getTime() - service.getTime(0).getTime();
 									minutesWorked.put(driver, minutesWorked.get(driver) + (int)(difference/(1000*60)));
 								}
 								else
 								{
 									long difference = serviceback.getTime(service.getNumberOfTimingPoints()-1).getTime() - nextAvailable.getTime();
 									minutesWorked.put(driver, minutesWorked.get(driver) + (int)(difference/(1000*60)));
 								}
 								nextAvailable = serviceback.getTime(serviceback.getNumberOfTimingPoints()-1);
 								System.out.print(" " + service.getID() + "(" + simpleDateFormat.format(service.getTime(0)) + ")");
 								System.out.print(" " + serviceback.getID() + "(" + simpleDateFormat.format(serviceback.getTime(0)) + ")" + minutesWorked.get(driver));
 								// System.out.print("            Driver is assigned to " + service.getID());
 								// System.out.println(" next available is " + simpleDateFormat.format(nextAvailable));
 								if(minutesWorked.get(driver) > workPerDay)
 									break;
 							}
 							// else
 								// System.out.println("            Service is already assigned");
 						// else
 							// System.out.println("            Driver is not available for this service");
 					}
 				if(minutesWorked.get(driver) != 0)
 				{
 					System.out.println(" ");
 					continue;
 				}
 				for(int routeIndex = 2; routeIndex < routes.length; routeIndex++)
 				{
 					Route route = routes[routeIndex];
 					// System.out.println("    Trying route " + routeID);
 					services = route.getServices(dayType);
 					for(Service service: services)
 					{
 						// System.out.println("        Trying Service " + service.getID());
 						if((nextAvailable == null)||(service.getTime(0).after(nextAvailable)))
 							if(!rosterDrivers.containsKey(service))
 							{
 								rosterDrivers.put(service, driver);
 								if(nextAvailable == null)
 									minutesWorked.put(driver, minutesWorked.get(driver) + service.getDuration());
 								else
 								{
 									long difference = service.getTime(service.getNumberOfTimingPoints()-1).getTime() - nextAvailable.getTime();
 									minutesWorked.put(driver, minutesWorked.get(driver) + (int)(difference/(1000*60)));
 								}
 								nextAvailable = service.getTime(service.getNumberOfTimingPoints()-1);
 								System.out.print(" " + service.getID() + "(" + simpleDateFormat.format(service.getTime(0)) + ")" + minutesWorked.get(driver));
 								// System.out.print("            Driver is assigned to " + service.getID());
 								// System.out.println(" next available is " + simpleDateFormat.format(nextAvailable));
 								if(minutesWorked.get(driver) > workPerDay)
 									break;
 							}
 							// else
 								// System.out.println("            Service is already assigned");
 						// else
 							// System.out.println("            Driver is not available for this service");
 					}					
 					if(minutesWorked.get(driver) > 0)
 						break;
 				}
 				System.out.println(" ");
 			}
 		}
 		for (Map.Entry entry : rosterDrivers.entrySet())
 		{
     		System.out.println(((Service)entry.getKey()).getID() + ": " + ((Driver)entry.getValue()).getID());
 		}
 	}
 
 	public static void main(String[] args)
 	{
 		database.openBusDatabase();
 
 		// printFullTimetable();
 		generateDriverRoster();
 	}
 }
