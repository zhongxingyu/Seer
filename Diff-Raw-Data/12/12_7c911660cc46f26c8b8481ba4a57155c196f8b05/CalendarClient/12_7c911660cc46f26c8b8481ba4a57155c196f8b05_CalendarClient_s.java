 package alpv.calendar;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 
 public class CalendarClient implements EventCallback {
 
 	CalendarServer _calendarServer;
 
 	public CalendarClient(String host, int port) throws RemoteException,
 			NotBoundException {
 		Registry registry = LocateRegistry.getRegistry(host, port);
 		_calendarServer = (CalendarServer) (registry.lookup("calendarServer"));
 	}
 
 	public void run() {
 		System.out.print("Welcome to calendar client.\n"
 				+ "Possible commands:\n"
 				+ "add: <name>;<users>;<date> - to add an event\n"
 				+ "remove: <id> - to remove an event\n"
 				+ "update: <id>;<name>;<users>;<date> - to modifiy an event\n"
 				+ "list: <user> - show all events for a user\n"
 				+ "next: <user> - get next event for user\n"
 				+ "register: <user> - register for upcoming events\n"
 				+ "unregister - unregister from event notifications\n"
 				+ "quite - to close the client\n");
 
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		try {
 			while (true) {
 
 				String line = br.readLine();
 
 				try {
 					if (line.startsWith("add")) {
 						// Add a new event
 						String params[] = getParameters(line);
 						long id = _calendarServer.addEvent(parseEvent(
 								params[0], params[1], params[2]));
 						System.out.println("Event created with id: " + id);
 					} else if (line.startsWith("update")) {
 						// Update an event
 						String params[] = getParameters(line);
 						boolean success = _calendarServer.updateEvent(
 								(new Long(params[0])).longValue(),
 								parseEvent(params[1], params[2], params[3]));
 
 						if (success) {
 							System.out.println("Event updated.");
 						} else {
 							System.out.println("Can't update the event.");
 						}
 
 					} else if (line.startsWith("remove")) {
 						// Remove an event
 						String params[] = getParameters(line);
 						boolean success = _calendarServer
 								.removeEvent((new Long(params[0])).longValue());
 
 						if (success) {
 							System.out.println("Event removed.");
 						} else {
 							System.out.println("Can't remove the event.");
 						}
 
 					} else if (line.startsWith("list")) {
 						String params[] = getParameters(line);
 						List<Event> events = _calendarServer
 								.listEvents(params[0]);
 						if (events.isEmpty()) {
 							System.out.println("No entries.");
 						} else {
 							for (Event event : events) {
 								System.out.println(event);
 							}
 						}
 					} else if (line.startsWith("register")) {
 						String params[] = getParameters(line);
 						_calendarServer.RegisterCallback(this, params[0]);
 					} else if (line.startsWith("unregister")) {
 						_calendarServer.UnregisterCallback(this);
 					} else if (line.equals("quite")) {
 						break;
 					}
 				} catch (ParseException e) {
 					System.err.println("Invalid date format.");
 				}
 			}
 			br.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		System.out.println("Bye.");
 	}
 
 	public Event parseEvent(String name, String user, String date)
 			throws ParseException {
 
 		String[] users = user.split(",");
 
 		Calendar calendar = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
 		calendar.setTime(sdf.parse(date));
 
 		return new Event(name, users, calendar.getTime());
 
 	}
 
	public String[] getParameters(String line) {
 		String[] parts = line.split(" ");
 		if (parts.length == 2) {
 			return parts[1].split(";");
 		}
 
 		throw new IllegalArgumentException();
 	}
 
 	@Override
 	public void call(Event e) throws RemoteException {
 		System.out.println("Notification from server");
 		System.out.println(e);
 	}
 
 }
