 // COMP 429 Virtual router class project 
 // Ursula, Moe, and Kash
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 // import java.util.zip.CRC32;
 import java.util.Arrays;
 
 public class Router {
 
 	// class level variables
 	
 
 	// reader for user input from console
 	static BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
 	
 	
  	// constructor
  	public Router() {
  		
  	}
 	/*----------------------------------------------------------------------------------------*/
 	// program entry point
 	public static void main(String[] args) {
 
 		// local variables
 		String[] command;
 		
 		// say hello
 		print("Virtual router 1.0\n");
 		print("type help for list of commands\n\n");
 		
 		ListenerPort port1 = new ListenerPort(8000);
 		port1.start();
 		try {Thread.sleep(100);} catch (InterruptedException e) {}
 	
 		// main loop
 		while(true){
 			
 			command = getCommand();
 			doCommand(command);
 		}
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// output string to console
 	private static void print(String s) {
 		
 		System.out.print(s);
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// get command from console
 	private static String[] getCommand() {
 		
 		String inputString = null;
 		
 		System.out.print("> ");
 		
 		try { inputString = console.readLine();} 
 		catch (IOException e) {
 			e.printStackTrace();
 			System.out.println(e.getMessage());
 		}
 		
 		String[] ret = inputString.split(" ");
 		return ret;
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// process command
 	private static void doCommand(String[] command) {
 		
 		if(command.length == 0)								// empty, do nothing 
 			return;
 		
 		switch(command[0]){
 		
 		case "help" 	: showHelp();				break;
 		case "config"	: showSettings();			break;
 		case "route"	: route(command);			break;
 		case "port"		: port(command);			break;
 		case "connect"	: connect(command);			break;
 		case "send"		: send(command);			break;
 		case "include"	: loadSettings(command);	break;
 		case "t"		: testSomething();			break;
 		case "quit" 	: appQuit();				break;
 		case "q" 		: appQuit();				break;
 		default     	: System.out.println("unknown command (type help for list)");
 		}
 	}
 	/*----------------------------------------------------------------------------------------*/
 	private static void showHelp(){
 		
 		 System.out.println("help                                                   show this");
 		 System.out.println("config                                                 show router settings");
		 System.out.println("include <file>                                         load commands from <file>");
 		 System.out.println("port add <port number> <virtual IP/bits> <mtu>         add port");
 		 System.out.println("port del <port number>                                 delete port");
 		 System.out.println("connect add <local real port> <remote Real IP:port>    add connection");
 		 System.out.println("connect del <port number>                              delete connection");
 		 System.out.println("route add <network ID/subnet bits> <virtual IP>        add to routing table");
 		 System.out.println("route del <network ID/subnet bits> <virtual IP>        delete from routing table");
 		 System.out.println("send <SRC Virtual IP> <DST Virtual IP> <ID> <N bytes>  send test packet\n" +
 		                    "                                                       creates a packet with Identification = ID\n" +
 		 		            "                                                       sent to the virtual IP. Data portion consists of\n" +
 		 		            "                                                       N bytes of 'ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGH...\n");
 		 System.out.println("quit                                                   release resources and quit program");
 	}
 	/*----------------------------------------------------------------------------------------*/
 	private static void port(String[] command) {
 		
 		try {
 			switch(command[1]){
 			case "add" :    System.out.println("command: " + command[0] + " " + command[1] + " " +
 								command[2] + " " + command[3] + " " + command[4] + " " );
 						    break;
 			case "del" : System.out.println("command: " + command[0] + " " + command[1] + " " +
 						        command[2]);
 						    break;
 		    default    : System.out.println("usage: port add <port number> <virtual IP/bits> <mtu>");
 			             System.out.println("usage: port del <port number>");
 			}
 		}
 		catch (Exception e){
 			System.out.println("usage: port add <port number> <virtual IP/bits> <mtu>");
 			System.out.println("usage: port del <port number>");
 		}
 	}
 	/*----------------------------------------------------------------------------------------*/
 	private static void connect(String[] command) {
 		
 		try {
 			switch(command[1]){
 			case "add" :    System.out.println("command: " + command[0] + " " + command[1] + " " +
 								command[2] + " " + command[3]);
 						    break;
 			case "del" : System.out.println("command: " + command[0] + " " + command[1] + " " +
 						    command[2]);
 						    break;
 			default    : System.out.println("usage: connect add <port number> <virtual IP/bits> <mtu>");
 			             System.out.println("usage: connect del <port number>");
 			}
 		}
 		catch (Exception e){
 			System.out.println("usage: connect add <port number> <virtual IP/bits> <mtu>");
 			System.out.println("usage: connect del <port number>");
 		}
 	}
 	/*----------------------------------------------------------------------------------------*/
 	private static void send(String[] command) {
 		
 		try {
 			System.out.println("command: " + command[0] + " " + command[1] + " " +
 								command[2] + " " + command[3] + " " + command[4]);
 		}
 		catch (Exception e){
 			System.out.println("usage: send <SRC Virtual IP> <DST Virtual IP> <ID> <N bytes>");
 		}
 	}
 	/*----------------------------------------------------------------------------------------*/
 	private static void route(String[] command) {
 		
 		try {
 			switch(command[1]){
 			case "add" :    System.out.println("command: " + command[0] + " " + command[1] + " " +
 								command[2] + " " + command[3]);
 						    break;
 			case "del" : System.out.println("command: " + command[0] + " " + command[1] + " " +
 						    command[2] + " " + command[3]);
 						    break;
 			default    : System.out.println("usage: route add <network ID/subnet bits> <virtual IP>");
 			             System.out.println("usage: route del <network ID/subnet bits> <virtual IP>");
 			}
 		}
 		catch (Exception e){
 			System.out.println("usage: route add <network ID/subnet bits> <virtual IP>");
 			System.out.println("usage: route del <network ID/subnet bits> <virtual IP>");
 		}
 	}	/*----------------------------------------------------------------------------------------*/
 	/*----------------------------------------------------------------------------------------*/
 	/*----------------------------------------------------------------------------------------*/
 	// print router settings 
 	private static void showSettings() {
 	
 		// system info
 		String nameOS = "os.name";  
 		String versionOS = "os.version";  
 		String architectureOS = "os.arch";
 
 		// print some OS info
 		System.out.println("\nName of the OS: " + 
 		System.getProperty(nameOS));
 		System.out.println("Version of the OS: " + 
 		System.getProperty(versionOS));
 		System.out.println("Architecture of THe OS: " + 
 		System.getProperty(architectureOS));
 		
 		// router settings
 
 	}
 	/*----------------------------------------------------------------------------------------*/
 	/*----------------------------------------------------------------------------------------*/
 	// load router settings 
 	private static void loadSettings(String[] command) {
 	
 		// open file
 		// process commands
 		try {
 			System.out.println("command: " + command[0] + " " + command[1]);
 		}
 		catch (Exception e){
 			System.out.println("usage: load <filename>");
 		}
 	}
 	/*----------------------------------------------------------------------------------------*/
 	/*----------------------------------------------------------------------------------------*/
 	/*----------------------------------------------------------------------------------------*/
 	/*----------------------------------------------------------------------------------------*/
 	// exit application properly
 	private static void appQuit() {
 	
 		print("\nreleasing resources\n");
 		print("good bye\n");
 		try {console.close();} 
 		catch (IOException e)
 			{print("IO error: " + e.getMessage() + "\n");}		// nothing we can do here
 		System.exit(0);											// exit application
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// use this method to test code 
 	private static void testSomething() {
 	
 		//String s = "Whenever digital data is stored or interfaced, data corruption might occur. Since the beginning of computer science";
 	
 
 		try {
 			
 //			byte[] ip = {(byte) 192, (byte) 168, 1, 0};
 //			byte[] ip0 = {(byte) 192, (byte) 168, 1, 1};
 		
 			byte a1[] = {1,2,3,4,5,6};
 			byte a2[] = {6,5,4,3,2,1};
 			
 			print("setup 2 macAddress classes\n");
 			MacAddress m1 = new MacAddress(a1);
 			MacAddress m2 = new MacAddress(a2);
 			System.out.println(m1.toHexString());
 			System.out.println(m2.toHexString());
 			
 
 			
 			print("\nmaking ethernet frame\n");
 			byte[] b = new byte[45];
 			Arrays.fill(b, (byte)97);
 			EthernetFrame ef1 = new EthernetFrame(m1,m2,(short)b.length,b);
 			System.out.println(ef1.toString() + "\n");
 			System.out.println(ef1.toHexString(ef1.toByteArray()));
 			
 			print("testing Ethernet Frame byte array constructor\n");
 			EthernetFrame ef2 = new EthernetFrame(ef1.toByteArray());
 			System.out.println(ef2.toString() + "\n");
 			System.out.println(ef2.toHexString(ef1.toByteArray()) + "\n");
 			System.out.println(VRMUtil.frameValid(ef2.toByteArray()) + "\n");	
 			
 //			print("test crc32 class\n");
 //			print("enter a string: ");
 //			String s = console.readLine();
 //		
 //			long t = 0;
 //			t = VRMUtil.getCRC(s.getBytes());
 //			System.out.println(t);
 //			System.out.println(VRMUtil.getCRCStr(s.getBytes()));
 
 
 			
 		}
 		catch(Throwable e) {
 			print("something went wrong with the test: " + e.getMessage() + "\n"); 
 		}
 	}
 	/*----------------------------------------------------------------------------------------*/
 
 } 
