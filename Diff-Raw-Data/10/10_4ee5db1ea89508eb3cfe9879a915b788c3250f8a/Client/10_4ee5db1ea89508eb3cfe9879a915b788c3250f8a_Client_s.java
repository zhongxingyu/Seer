 import java.io.*;
 import java.net.*;
 
 public class Client {
 
   public static void main(String[] args) throws Exception {
     Socket socket = null;
     PrintWriter out = null;
     BufferedReader in = null;
  
     String tag = args[0];
     /*** my code **/
     Player player = new Player(tag);
 
     try {
       socket = new Socket("localhost", 4445);
       out = new PrintWriter(socket.getOutputStream(), true);
       in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
     } catch (UnknownHostException e) {
       System.err.println("Don't know about host: localhost.");
       System.exit(1);
     } catch (IOException e) {
       System.err.println("Couldn't get I/O for the connection to: localhost.");
       System.exit(1);
     }
 
     BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
     String fromServer;
     String fromUser;
 
     out.println(tag);
 
     while ((fromServer = in.readLine()) != null) {
       System.out.println("Server: " + fromServer);
       if (fromServer.equals("Bye"))
         break;
       if (((fromServer.startsWith("ADD")) || (fromServer.startsWith("REMOVE"))))
       {
         //replace here with an output from my code
         player.setStatus(fromServer);
        System.out.println("final result " + player.play());
         //fromUser = stdIn.readLine();
         fromUser = player.play();
         if (fromUser != null) {
           //System.out.println("Client: " + fromUser);
           
           out.println(fromUser);
         }        
       }
     }
 
     out.close();
     in.close();
     stdIn.close();
     socket.close();
   }
 
   
 }
