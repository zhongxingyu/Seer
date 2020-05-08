 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.Scanner;
 
 
 /**
 * A Runner that handles input
 */
 
 public class Runner
 {
 	private static Integer unusedPort = 9000;						
 	private static boolean scriptMode = false;												
 	private static HashMap<Integer, Integer> serverPorts = new HashMap<Integer, Integer>();	//maps sIDs to ports ie {0 : 9000}
 	private static HashMap<Integer, Client> clients = new HashMap<Integer, Client>();
 	private static HashMap<Integer, Server> servers = new HashMap<Integer, Server>();			//maps sIDs to Servers
 	private static boolean paused = false;
 	private static long delay_interval = 100;
 	private static Scanner scanner = null;
 	private static int primaryServer = -1;
 	
 	public static void main(String[] args)
 	{
 		System.out.println("Hello Chao.  Welcome to Bayou.");
 		
 		args = new String[1];	
		args[0] = "entropy_script";
 		
 		if(args == null)
 			scanner = new Scanner(System.in);
 		else
 		{
 			try 
 			{
 				scanner = new Scanner(new File(args[0]));
 				scriptMode = true;
 			} catch (FileNotFoundException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		run();
 	}
 	
 	private static void startClient(Integer cID, Integer sID)
 	{
 		Client client = new Client(cID, serverPorts.get(sID));
 		clients.put(cID, client);
 	}
 	
 	private static void join(Integer sID)
 	{
 		Server s = new Server(unusedPort, sID);
 		if(primaryServer == -1)
 		{
 			primaryServer = sID;
 			s.makePrimary();
 		}
 		servers.put(sID, s);
 		serverPorts.put(sID, unusedPort++);
 
 	}
 	
 	private static void clientDisconnect(Integer cID)
 	{
 		clients.get(cID).disconnect();
 	}
 	
 	private static void clientReconnect(Integer cID, Integer sID)
 	{
 		clients.get(cID).reconnect(serverPorts.get(sID));
 	}
 	
 	private static void printLog(Integer sID)
 	{
 		servers.get(sID).printLog();
 		System.out.println("-------------------------------------");
 	}
 	
 	private static void isolate(Integer sID)
 	{
 		servers.get(sID).isolate();
 	}
 	
 	private static void reconnect(Integer sID)
 	{
 		for(Integer otherID : servers.keySet())
 			recoverConnection(sID, otherID);
 	}
 	
 	private static void breakConnection(Integer sA, Integer sB)
 	{
 		servers.get(sA).breakConnectionWith(sB);
 	}
 	
 	private static void recoverConnection(Integer sIDA, Integer sIDB)
 	{
 		if(sIDA == sIDB)
 			return;
 		servers.get(sIDA).connectToServer(serverPorts.get(sIDB), sIDB);
 	}
 	
 	private static void testConnection(int sA, int sB) {
 		servers.get(sA).sendMessageToServer(sB, "test connection");
 	}
 	
 	private static void leave(Integer sID)
 	{
 		//TODO
 	}
 	
 
 	private static void testEntropy(int sID) {
 		servers.get(sID).startEntropySession();
 	}
 	
 	private static void delay()
 	{
 		long t = System.currentTimeMillis();
 		while(System.currentTimeMillis() - delay_interval < t);
 	}
 	
 	private static void printConnections()
 	{
 		for(Server s : servers.values())
 			s.printConnections();
 		for(Client c : clients.values())
 			c.printConnections();
 	}
 	
 	private static void quit()
 	{
 		System.exit(0);
 	}
 
 	private static void run()
 	{
 		while(true)
 		{
 			if(!scanner.hasNext())
 				break;
 			delay();
 			String command = scanner.nextLine();
 			String[] cmdArgs = command.split(" ");
 			if(scriptMode)
 				System.out.println("\nSCRIPT: " + command);
 			if(cmdArgs[0].equals("user"))
 				clients.get(Integer.parseInt(cmdArgs[1])).userRequest(cmdArgs);
 			else if(cmdArgs[0].equals("startClient"))
 				startClient(Integer.parseInt(cmdArgs[1]), Integer.parseInt(cmdArgs[2]));
 			else if(cmdArgs[0].equals("join"))
 				join(Integer.parseInt(cmdArgs[1]));
 			else if(cmdArgs[0].equals("clientDisconnect"))
 				clientDisconnect(Integer.parseInt(cmdArgs[1]));
 			else if(cmdArgs[0].equals("clientReconnect"))
 				clientReconnect(Integer.parseInt(cmdArgs[1]), Integer.parseInt(cmdArgs[2]));
 			else if(cmdArgs[0].equals("printLog"))
 				printLog(Integer.parseInt(cmdArgs[1]));
 			else if(cmdArgs[0].equals("printLogs"))
 				for(Integer sID : servers.keySet())
 					printLog(sID);
 			else if(cmdArgs[0].equals("isolate"))
 				isolate(Integer.parseInt(cmdArgs[1]));
 			else if(cmdArgs[0].equals("leave"))
 				leave(Integer.parseInt(cmdArgs[1]));
 			else if(cmdArgs[0].equals("reconnect"))
 				reconnect(Integer.parseInt(cmdArgs[1]));
 			else if(cmdArgs[0].equals("breakConnection"))
 				breakConnection(Integer.parseInt(cmdArgs[1]), Integer.parseInt(cmdArgs[2]));
 			else if(cmdArgs[0].equals("recoverConnection"))
 				recoverConnection(Integer.parseInt(cmdArgs[1]), Integer.parseInt(cmdArgs[2]));
 			else if(cmdArgs[0].equals("testConnection"))
 				testConnection(Integer.parseInt(cmdArgs[1]), Integer.parseInt(cmdArgs[2]));
 			else if(cmdArgs[0].equals("printConnections"))
 				printConnections();
 			else if(cmdArgs[0].equals("startEntropy"))
 				testEntropy(Integer.parseInt(cmdArgs[1]));
 			else if(cmdArgs[0].equals("pause"))
 				paused = true;
 			else if(cmdArgs[0].equals("continue"))
 				paused = false;
 			else if(cmdArgs[0].equals("quit"))
 				quit();
 			else if(cmdArgs[0].equals("switch to console mode"))
 				scanner = new Scanner(System.in);
 		}
 	}
 
 
 
 	
 }
