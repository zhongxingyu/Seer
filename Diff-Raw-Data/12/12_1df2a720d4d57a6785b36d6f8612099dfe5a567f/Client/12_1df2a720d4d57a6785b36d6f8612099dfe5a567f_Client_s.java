 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Serv
  * Date: 27.06.13
  * Time: 13:28
  * To change this template use File | Settings | File Templates.
  */
 public class Client {
 
     ConsoleController consoleController;
     protected volatile Socket serverSocket;
     private ConnectData connectData;
     PrintWriter output;
     volatile BufferedReader  input;
     MessageReceiver messageReceiver;
     public  Client(){
         consoleController = new ConsoleController(this);
 
     }
 
 
     public void connect(ConnectData connectData ) throws IOException {
         if (messageReceiver != null){output.println("quit");}
         this.connectData = connectData;
 
         serverSocket = new Socket(connectData.ip,connectData.port);
 
         messagingWithServer(connectData.userName) ;
         messageReceiver = new MessageReceiver();
         messageReceiver.start();
 
 
 
       /*  out.close();
         in.close();
         inu.close();
         serverSocket.close();*/
     }
 
     public List<String> messagingWithServer(String message) throws IOException {
         if(serverSocket == null)  {
             consoleController.write("At the beginning connect to server!");
             return null;
         }
         if (!serverSocket.isClosed())    {
         output = new PrintWriter(serverSocket.getOutputStream(), true);
         output.println(message);
          } else {
              consoleController.write("At the beginning connect to server!");
          }
         /*BufferedReader input = new
                 BufferedReader(new
                 InputStreamReader(serverSocket.getInputStream()));
         List<String> inputMessage = new ArrayList<String>();
 
         String messageString = "";
         while (input.ready()) {
             messageString = input.readLine();
             inputMessage.add("Server response: " + messageString);
         }
         consoleController.write(inputMessage);
         return inputMessage;*/
         return null  ;
 
     }
 
     public void disconnect() {
         if (serverSocket.isConnected()){
 
             output.println("quit");
 
         }    else {
             consoleController.write("At the beginning connect to server!");
         }
     }
 
 
     public class MessageReceiver extends Thread{
         @Override
         public void run() {
 
             try {
                 input = new
                         BufferedReader(new
                         InputStreamReader(serverSocket.getInputStream()));
                 String messageFromServer;
 
                 while (serverSocket.isConnected()) {
 
                     messageFromServer = input.readLine();
                     consoleController.write("Server say: " + messageFromServer);
 
                     if (messageFromServer.equalsIgnoreCase("quit")) break;
 
                 }
 
                 output.close();
                 input.close();
                 //serverSocket.close();
                 consoleController.write("You was disconnected from server");
            } catch (Exception e) {
                 e.printStackTrace();
             }
 

         }
     }
 
   /*  public static void main(String[] args) throws IOException {
 
         readInputData();
 
 
      *//*   if (args.length==0) {
             System.out.println("use: client hostname");
             System.exit(-1);
         }*//*
 
      //   System.out.println("Connecting to... "+args[0]);
 
         serverSocket = new Socket("192.168.0.47",5463);
         BufferedReader in  = new
                 BufferedReader(new
                 InputStreamReader(serverSocket.getInputStream()));
         PrintWriter out = new
                 PrintWriter(serverSocket.getOutputStream(),true);
         BufferedReader inu = new
                 BufferedReader(new InputStreamReader(System.in));
 
         String fuser,fserver;
 
         while ((fuser = inu.readLine())!=null) {
             out.println(fuser);
             fserver = in.readLine();
             System.out.println(fserver);
             if (fuser.equalsIgnoreCase("close")) break;
             if (fuser.equalsIgnoreCase("exit")) break;
         }
 
         out.close();
         in.close();
         inu.close();
         serverSocket.close();
     }*/
 
 
 }
