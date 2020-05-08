 /*	Client
 
 	Handles the client-side of communications - display messages sent from the server and accept messages
 	sent from the user and forward them to the server. 
 
 	Still needs:
 		
 	-	Exception handling
 
 	-	More, I'm sure, but I can't think of it right now. 
 
 */
 
 
 import java.io.*;  // Provides for system input and output through data 
 		   // streams, serialization and the file system
 import java.net.*; // Provides the classes for implementing networking 
 		   // applications Lemons
 
 // TCP Client class
 class Client 
 {
 	public static void main(String argv[])  
 	{
  		int lisPort;
 		String hostname; 
 			
 		if(argv.length == 2)
 		{	
 			hostname = argv[0];
 			lisPort = Integer.parseInt(argv[1]);
 			connect(hostname, lisPort);
 		} else
 		{
 			System.out.println("Please specify a hostname and port number.");
 		}
 		
 	}
 	public static void connect(String hostname, int port) 
 	{
 		BufferedReader servIn;
 		PrintWriter servOut;
 		BufferedReader userIn;  
 	
 		Protocol protocol = new Protocol();
 		Socket clientSocket; 
 		String userPkt = "";
 		String serverPkt = ""; 
 		String serverString = "";
 		String userString = "";	
 		char[] data;	
 		int dataLength;
 		String cmd_type; 
 			
 		userIn = new BufferedReader(new InputStreamReader(System.in));
 		
 		try
 		{
 			// Create the socket and connect.
 			clientSocket = new Socket(hostname, port);
 		
 			// Set up communication channels. 
 			servOut = new PrintWriter(clientSocket.getOutputStream(), true); 	
 			servIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
 			
 // PROTOCOL
 			while(serverString != "END")
 			{
 				while(userString.equals("") && serverPkt.equals(""))
 				{
 					// Poll for input from either the user or the server continually.
 					if(userIn.ready())
 						userString = userIn.readLine();
 					else if(servIn.ready())
 						serverPkt = servIn.readLine();
 				}
 				if(userString.equals(""))
 				{
 					// The server sent us something ; display it.
 					dataLength = protocol.getLength(serverPkt);
 					data = new char[dataLength];	
 					servIn.read(data, 0, dataLength);
 					serverString = new String(data);
 					System.out.println(serverString);
 				} else if(userString != null)
 				{	
 					if(userString.indexOf(" ") != -1 && userString.indexOf(" ") != userString.length())
 					{			
 						cmd_type = userString.substring(0, userString.indexOf(" "));	
 						userString = userString.substring(userString.indexOf(" ") + 1);
 					} else
 					{
						cmd_type = userString;
 					}
 					userPkt = protocol.makePacket(userString, cmd_type);
 					// The user typed something - send it to the server. 
 					servOut.println(userPkt);
 				}		
 				serverPkt = "";
 				userPkt = "";
 				userString = "";
 				serverString = "";
 			}
 // PROTOCOL
 
 			clientSocket.close();
 		} catch(SocketException se)
 		{
 			System.out.println("There has been an issue with the socket. Please check your hostname and port #, and try again.");
 		} catch(Exception e)
 		{
 			System.out.println("Something has gone terribly wrong...");
 		}
 	}
 }
 
