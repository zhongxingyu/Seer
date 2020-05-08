 package client;
 import java.io.*;
 import java.nio.*;
 import java.nio.channels.*;
 import java.net.*;
 import java.util.*;
 import tools.*;
 
 public class MultiClient{
 
 
     private final String SERVER_HOST = "localhost";//change address before submitting!
     private final int SERVER_PORT = 40302;
     private ServerSocket ss = null;
     private int port;
     private TimeInfo timeinfo;
     private String clientId = null;
 
     public static void main(String []args){
 
 
         new MultiClient().run(); //runing client
 
     }
 
 
     private void unsubscribe() throws Exception{
 
         if ( clientId == null ){ //haven't subscribed time
      
             System.err.println("You haven't subscribe time yet!");
             return;
 
         }
 
         Socket csocket = new Socket( SERVER_HOST, SERVER_PORT );
       
         PrintWriter out = new PrintWriter( csocket.getOutputStream(), true);
         BufferedReader in = new BufferedReader( new InputStreamReader( csocket.getInputStream() ) );
 
         out.println("unsubscribe  " + clientId); //send request to unsubscribe format: unsubscribe  clientId
 
         String line;
         if( ( line = in.readLine() ) != null &&
               line.equals("OK") ){ // get confirmation of server
              
             out.close();
             in.close();
             csocket.close();
 
             System.out.println("Time unsubscribed");
 
         }else{
 
             System.err.println("Failed to unsubscribe");
         }
 
     }
 
 
     private void subscribe() throws Exception{
 
             
            Socket csocket = new Socket( SERVER_HOST, SERVER_PORT );
            this.port = csocket.getLocalPort(); //get local port, it will be used in the future 
 
            PrintWriter out = new PrintWriter( csocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader( new InputStreamReader( csocket.getInputStream() ) );
 
            out.println("subscribe"); //send request to server
 
            String line;
            if( ( line = in.readLine() ) != null ){
                   
                     clientId = line; //get client id
 
                     out.close();
                     in.close();
                     csocket.close();
                     
                     this.ss = new ServerSocket( this.port ); //create new server socket to listen from server 
                     System.out.println("Time subscribed, client id is " + clientId);
 
  
            }else{
 
                     throw new Exception("Failed to subscribe");
            }
 
     }
 
 
     class SocketProcess implements Runnable{
 
 
         private void getTime() throws Exception{
           
 
                 Socket s = ss.accept();
     
                 ObjectInputStream in = new ObjectInputStream( s.getInputStream() );
                 timeinfo = (TimeInfo)in.readObject(); //get time infomation from server
                 System.out.println( timeinfo.time );
                 
                 in.close();
                 s.close();
                 
 
         }
     
         public void cast(){
              
               if( timeinfo.point + 1 < timeinfo.addresses.size() )
                    timeinfo.point++; // incrument 'point' 
               else
                    timeinfo.point = -1; // no more client
     
     
               if( timeinfo.addresses.size() > 0 && timeinfo.point != -1 ){ // there is client I need cast time to it
            
                   clientInfo ci;
                   InetAddress ip;
                   int port;
     
                   
                   for(int i=timeinfo.point; i<timeinfo.addresses.size(); i++ ){
     
                         try{
     
                             ci = timeinfo.addresses.get(i); // get first client
                             ip = ci.getAddress();
                             port = ci.getPort();
                             Socket s = new Socket( ip, port );
                             ObjectOutputStream out = new ObjectOutputStream( s.getOutputStream() );
                             out.writeObject( timeinfo ); //send time to client
                             out.close();
                             s.close();
                             System.out.println("Time is sent to " + i + "th(st/nd) client "  + ip.getHostAddress() 
                                                + ":" + port + "\n");
                             return;
                         }
                         catch(Exception e)
                         {  
                             System.err.println("Fail to send time to " + i + "th(st/nd) client "); 
     
                             if( timeinfo.point + 1 < timeinfo.addresses.size() ) //try next client 
                                timeinfo.point++;
     
                             continue;
                         }
     
                    }
 
                    System.err.println("Failed to send time to clients, will resend after");
     
                 }
                 else{ // no subscription
     
                     if( timeinfo.point == -1 ) 
                       System.out.println("I am last client");
     
                 }
                 System.out.println("");
     
     
         }
     
         public void run(){
           
           try{
              
             System.out.println("I am started...");
     
              while(true){
     
                this.getTime(); //get time from server
                this.cast();//cast to next client 
     
              }
     
           }catch(Exception e){
     
                 System.err.println("Client error: " + e.getMessage() );
                 System.exit(-1); 
     
           }
     
     
         }
     
     } //end of Socket Process
     
     public void run(){
 
       try{
 
          /*
          InputStream in = System.in;
          FileInputStream fi = (FileInputStream) in;
          FileChannel fc = fi.getChannel(); 
          */
 
          this.subscribe();//send subscribe to server 
          Thread t = new Thread( new SocketProcess() );  
          t.start(); // start a thread to get time from server
           
          while(true){
 
            Console cons;
            String cmd;
            if( ( cons = System.console() ) != null &&
                  ( cmd = cons.readLine() ) != null  ){ //waiting user's command
 
                if ( cmd.equals("unssb") ){ //if user ask to unsubscribe 
                  this.unsubscribe();
                  System.out.println("Client is exiting...");
                  System.exit(0);
                }
                else{
 
                  System.out.println("Unknown cmd");
                }
 
            }
 
          }
 
       }catch(Exception e){
 
             System.err.println("Client error: " + e.getMessage() );
             System.exit(-1); 
 
       }
 
 
 
     }
 
 }
