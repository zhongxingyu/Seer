 package FirstManda;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.UnknownHostException;
 
 public class Tester {
 	InputStreamReader isr = new InputStreamReader(System.in);
 	BufferedReader br = new BufferedReader(isr);
 	String role = "null";
 	
 	public static void main(String[] args) {
 		new Tester();
 	}
 	
 	public Tester() {
 		while(true) {
 			if(role.equalsIgnoreCase("server") || role.equalsIgnoreCase("client")) {
 				break;
 			}
 			chooseRole();
 		}
 		
 		if(role.equalsIgnoreCase("server")) {
 			createServer();
 		}
 		else if(role.equalsIgnoreCase("client")) {
 			createClient();
 		}
 	}
 	
 	public void chooseRole() {
 		if(role.equals("null")) {
 			System.out.println("Please choose a role (server/client):");
 		}
 		else {
 			System.out.println("Please type either 'client' or 'server':");
 		}
 		try {
 			role = br.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void createServer() {
 		TCPServer TCPs = new TCPServer();
 		
 		try {
 			System.out.println("Enter port number for local server: ");
 			int port = Integer.parseInt(br.readLine());
 			
 			System.out.println("Starting server...");
 			TCPs.startServer(port);
 		}
 		catch (IOException e) {
 			System.out.println("Server could not be started.");
 			e.printStackTrace();
 		}
 	}
 	
 	public void createClient() {
 		TCPClient TCPc = new TCPClient();
 		
 		try {
 			System.out.println("Enter IP address of remote host: ");
 			String ip = br.readLine();
 			System.out.println("Enter port of remote host: ");
 			int port = Integer.parseInt(br.readLine());
 			
 			System.out.println("Starting client...");
 			TCPc.initialize(ip, port);
 			System.out.println("Client started!");
 			
 			System.out.println("Enter message to server: ");
 			String message = br.readLine();
 			
			TCPc.sendMessage(message);
 		}
 		catch (UnknownHostException e) {
 			System.out.println("Client could not be started.");
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			System.out.println("Client could not be started.");
 			e.printStackTrace();
 		}
 	}
 }
