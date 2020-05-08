 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 
 public class ScheduleServer {
 
 	private static int port = 8080;
 	private static int maxConnections = 0;
 	private static HashMap<CurrentDate, Day> calStorage;
 
 	public static void main(String[] args) {
 
 		ServerSocket myService = null;
 		Socket clientSocket = null;
 		int i = 0;
 
 		CalParser cp = null;
 		try {
 			cp = new CalParser("AllPeriods.txt");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		HashMap<CurrentDate, Integer> sDays = new HashMap<>();
 		sDays.put(new CurrentDate(12, 11, 2012), 0);
 		sDays.put(new CurrentDate(12, 12, 2012), 1);
 		sDays.put(new CurrentDate(12, 13, 2012), 0);
 		sDays.put(new CurrentDate(12, 14, 2012), 0);
 		sDays.put(new CurrentDate(3, 1, 2013), 2);
		cp.getCalStorage().remove(new CurrentDate(3, 5, 2013));
 		sDays.put(new CurrentDate(3, 5, 2013), 3);
 		cp.addSpecialDays(sDays);
 		calStorage = cp.getCalStorage();
 
 		try {
 			myService = new ServerSocket(port, 0, InetAddress.getByName("localhost"));
 			System.out.println("Started Schedule Server!");
 		} catch (IOException e1) {
 			System.out.println(e1);
 		}
 
 		try {
 			while ((i++ < maxConnections) || (maxConnections == 0)) {
 				ScheduleClientConnection connection;
 				clientSocket = myService.accept();
 				connection = new ScheduleClientConnection(clientSocket, calStorage);
 				Thread t = new Thread(connection);
 				t.start();
 				System.out.println("Got client!");
 			}
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 	}
 }
