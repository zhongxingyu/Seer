 package factory.masterControl;
 
 //	Server-Socket Team -- Devon, Mher & Ben
 //	CSCI-200 Factory Project Team 2
 //	Fall 2012
 // 	Prof. Crowley
 // Another test
 
 import java.io.BufferedReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 public class PartHandler implements Runnable {
 
     Socket mySocket = null;
     PrintWriter out = null;
     BufferedReader in = null;
     boolean haveCMD = false;
     boolean error = true;
     String cmd = null;
     String message;
     MasterControl master = null;
     String client_id = null;
     boolean factoryDone;
 
 	public PartHandler(Socket s, BufferedReader b, PrintWriter p, String me, MasterControl mc){
 		//Get all the required variables set so PartHandler can function correctly
 		mySocket = s;
 		out = p;
 		in = b;
 		master = mc;
 		client_id = me;
         factoryDone = false;
 		//Sets up thread for the partHandler
 		(new Thread(this)).start();
 	}
 
     public void endClient(){
         //Need to send a final message to the client
         String closeCmd = "mcs close";
         //Send closeCmd to the client
         //Then set the factoryDone flag to true, allowing the thread to exit.
 
         out.println(closeCmd);
 
         factoryDone = true;
 
     }
 
 	public void run() 
 	{
 	    //This thread loops to get confirmations sent by clients 
 
 	    for(;;) {
             cmd = gotCmd();
             if(haveCMD) {//if there was a command then call parseCmd and send the cmd to Server to assess
                     error = master.command(cmd);
                     //sets haveCMD to false because parseCmd notified server
                     haveCMD = false;
             }
             if(error == false)
             {
            	master.command("err");
             }
             if(factoryDone){
                 break;
             }
         }
 	}
 	
 	public boolean send(String cmd) {
 		boolean result = false;
 
 		out.println(cmd);	//output command
 		result = true;
 		if(out.checkError())
 		{
 			result = false;
 		}
 		return result;
 	}
 	
 	//this loops until it gets a cmd from client 
 	private String gotCmd()
 	{
 		try {
 		    //Wait for the client to send a String 
 		    message = in.readLine();
 		    
 		} catch (Exception e) {
 		    e.printStackTrace();
 		}
 
 		//sets haveCMD to true is there was a command sent so can call parseCmd
 		if(message != null)
 		{
 			haveCMD = true;
 		}
 		return message;
 	}
 	
 }
