    import java.io.*; 
    import java.net.*; 
 	  
 public class TCPServerRouter
    {
 
        //This file is run with these args: <up-to-5-IPs-of-each-server>
        //
       static ServerSocket incomingSocket = null;
       static Socket outgoingServerSocket = null; //Sends request to server and receives file
       static Socket clientSocket = null; //Received request from client and send file to client
       int portInt;
       static int bytesRead = 0;
 	  static int sizeOfArgs = 0;
 	  static int timesRun = 0;
 	  static long start = 0;
 	  static long elapsedTime = 0;
 	  static long avgLookup = 0;
       static String ipAddressOfClient = null;
 	  static String ipAddressOfServer = null;
 	  static String[] messages = null;
       static String ipArray [][] = new String[5][3];
       static InputStream inFromServer = null; 
       static OutputStream fileStreamOut = null;
       static InputStream inFromClient = null;
 
       static byte[] receiveData = new byte[10240]; 
       static byte[] sendData  = new byte[10240]; 
 	  static byte[] receiveFile = new byte[10240];
    	
       public TCPServerRouter()
       {
          try
          {
             incomingSocket = new ServerSocket(2222);
          }
             catch(IOException e)
             {
                System.out.println("Could not listen on port: " + portInt);
             }
       }
    	
    	    //This method receives the string, splits it into necessary parts, and 
    	    //loops up the IP in the routing table
       public void getAndBuildDataToServer() throws Exception {
             inFromClient = new DataInputStream(clientSocket.getInputStream());
             inFromClient.read(receiveData);
             String message = new String (receiveData);
             message = message.trim();
 			messages = message.split(":");
 			message = messages[0];
 			ipAddressOfClient = messages[1];
 			ipAddressOfServer = messages[2];
 			sendData = messages[0].getBytes();
 			
 			start = System.currentTimeMillis();
             for( int i = 0 ; i < sizeOfArgs; i++ ) {
                if( ipArray[i][0].equals(ipAddressOfServer)) 
                {
 					elapsedTime = System.currentTimeMillis()-start;
 					this.Router(sendData, ipAddressOfServer, ipArray[i][2] );
                }		   
             }    
          }                     
       
 
         public static void main(String args[]){
 			sizeOfArgs = args.length;
 		
 		        //ipArray build depending on the number of IPs given in args
             for( int i = 0 ; i < sizeOfArgs; i++ ) {
                 ipArray[i][0] = args[i];
                 ipArray[i][2] = "2222";
             }
             
             TCPServerRouter myServerRouter = null;
             while(true){
                 try{
                     myServerRouter = new TCPServerRouter();
                     System.out.println("Waiting to receive message");
                     clientSocket = incomingSocket.accept();
                     System.out.println("Message received...getting data.");
                     
                     myServerRouter.getAndBuildDataToServer();
                     
                     System.out.println("Data built and sent");
                     
                     inFromServer = new DataInputStream(outgoingServerSocket.getInputStream());
                     fileStreamOut = new DataOutputStream(clientSocket.getOutputStream());
                     
                     System.out.println("Receiving data");
     
                     //received file from server and writes it directly to the client byte-by-byte
                    do{
                         bytesRead = inFromServer.read(receiveFile);
                         System.out.println("There are " + inFromServer.available() + " remaining bytes");
                         fileStreamOut.write(receiveFile, 0, bytesRead);    
                    }while(inFromServer.available() != 0);
                    
                    outgoingServerSocket.close();
                     clientSocket.close();
                     
                     //Routing table lookup is always 0
                     timesRun++;
                     avgLookup = elapsedTime/timesRun;
                     System.out.println("Current routing table lookup time(sec) = " + elapsedTime/1000);
                     System.out.println("Average routing table lookup time(sec) = " + avgLookup/1000);
                 }
                 catch(Exception e){
                     System.out.println(e);
                 }
 			}
         }
 
       //Not really a useful method
       //Could clean this out
       //TODO: clean this out
       public void Router(byte[] message, String ipAddress,
         String ports ) throws Exception {
         
          int port = Integer.parseInt( ports );
       
         this.sendMessage( message, ipAddress, port );
          }
 
    	    //Connects to the server and send the message
       public void sendMessage( byte[] message,  String ipAddress, int port ) {
           try{
           outgoingServerSocket = new Socket(ipAddress, port);
           OutputStream out = new DataOutputStream(outgoingServerSocket.getOutputStream());
           out.write(sendData);
		  	System.out.println("check");
           }
           catch(Exception e){
               System.out.println(e);
           }
       }
    }
