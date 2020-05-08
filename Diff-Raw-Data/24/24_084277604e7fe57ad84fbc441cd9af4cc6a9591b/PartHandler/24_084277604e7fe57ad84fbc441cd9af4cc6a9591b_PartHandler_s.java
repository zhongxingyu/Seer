 package factory.masterControl;
 
 //	Server-Socket Team -- Devon, Mher & Ben
 //	CSCI-200 Factory Project Team 2
 //	Fall 2012
 // 	Prof. Crowley
 // Another test
 
package factory.masterControl;
 
 //	Server-Socket Team -- Devon, Mher & Ben
 //	CSCI-200 Factory Project Team 2
 //	Fall 2012
 // 	Prof. Crowley
 // Another test
 
 import java.io.BufferedReader;
import java.io.PrintWriter;
 import java.net.Socket;
 
 public class PartHandler {
 
 Socket mySocket = null;
 PrintWriter out = null;
 BufferedReader in = null;
 boolean haveCMD = false; 
 String cmd = null;
 
 	public PartHandler(Socket s, BufferedReader b, PrintWriter p, String me, MasterControl mc){
 		//need to add other initializations
 		Socket mySocket = s;
 	}
 	
 	public void run() {
 	    try {
 		//Create the 2 streams for talking to the client
 		//second one may be for server though
 		out = new PrintWriter( mySocket.getOutputStream(), true );
 		in = new BufferedReader( new InputStreamReader( mySocket.getInputStream() ) );
 	    } catch (Exception e) {
 		e.printStackTrace();
 		System.exit(0);
 	    }
 
 	    System.out.println("got streams");
 
 	    //This thread loops to get confirmations sent by clients 
 
	    while( /*condition goes here*/) {
 			cmd = gotCmd();
 			if(haveCMD)
 			{//if there was a command then call parseCmd and send the cmd to Server to assess
				parseCmd(cmd); 
 				//sets haveCMD to false because parseCmd notified server?
 				haveCMD = false;
 			}
 		}
 	}
 	
 	public boolean send(String cmd) {
 		boolean result = false;
 		String confirmation = null;
 		out.println(cmd);
 			try //check if client recived sent cmd, not too sure on how to update result yeto r what results is suposed to be
 			{
				confirmation =  in.readLine());
 				if(confirmation != null)
 					result = true;
 			}
 			catch(IOException e)
 			{
 				System.err.print("Something went wrong when sent");
 				System.exit(1);
 			}
 		return result;
 	}
 	
 	//this loops until it gets a cmd from client 
 	private String gotCmd()
 	{
		try {
 		    //Wait for the client to send a String 
		    String message = in.readLine();
 		    
 		    //Send back a response
 		    // maybe out.println( message );
 		} catch (Exception e) {
 		    e.printStackTrace();
 		}
 
 		//sets haveCMD to true is ther was a command sent so can call parseCmd
 		if(message != null)
 		{
 			haveCMD = true;
 		}
 		return message;
 	}
 	
 	
 }
