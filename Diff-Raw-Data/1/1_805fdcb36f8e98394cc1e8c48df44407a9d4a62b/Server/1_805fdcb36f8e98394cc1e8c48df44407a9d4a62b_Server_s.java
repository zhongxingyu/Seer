 package first_distributed_system.Server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Reignos
  * Date: 7/9/13
  * Time: 6:14 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Server {
 
     private class ClientRun extends Thread{
         public Socket clientSocket;
         public ClientRun(Socket cSocket){
             clientSocket = cSocket;
         }
         @Override
         public void run() {
             try{
                 MathLogic mathLogic = new MathLogic();
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in =
                         new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 String inputLine;
 
                 while ((inputLine = in.readLine()) != null) {
                     if (inputLine.toLowerCase().equals("bye")){
                         break;
                     }
                     String[] spaceSplit = inputLine.split(" ");
                     int a = Integer.parseInt(spaceSplit[1]);
                     int b = Integer.parseInt(spaceSplit[2]);
                     String print = "";
                     if(spaceSplit[0].toLowerCase().equals("add")){
                         print = a + " + " + b + " = " + mathLogic.add(a,b);
                         out.println(print);
                     }

                     else if(spaceSplit[0].toLowerCase().equals("sub")){
                         print = a + " - " + b + " = " + mathLogic.subtract(a,b);
                         out.println(print);
                     }
                     System.out.println(print + " sent to Client");
                 }
 
                 out.close();
                 in.close();
                 clientSocket.close();
             }
             catch (IOException e) {
                 System.out.println("Accept failed: 4444");
                 System.exit(-1);
             }
         }
     }
 
     public Server(){
         System.out.println("Server Running");
 
         ServerSocket serverSocket = null;
         try {
             serverSocket = new ServerSocket(4444);
         }
         catch (IOException e) {
             System.out.println("Could not listen on port: 4444");
             System.exit(-1);
         }
 
         boolean running = true;
         do{
         try {
             ClientRun clientRun = new ClientRun(serverSocket.accept());
             clientRun.start();
         }
         catch (IOException e) {
             System.out.println("Accept failed: 4444");
             System.exit(-1);
         }
         } while(running);
     }
 
     public static void main(String [ ] args){
         new Server();
     }
 }
