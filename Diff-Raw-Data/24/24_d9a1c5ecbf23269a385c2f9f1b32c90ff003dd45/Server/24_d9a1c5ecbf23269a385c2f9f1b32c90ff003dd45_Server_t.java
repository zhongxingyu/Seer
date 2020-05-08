 import java.io.IOException;
 import java.io.*;
 import java.net.*;
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: 8thlight
  * Date: 2/20/12
  * Time: 2:03 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Server
 extends Thread{
     private int _port;
     public String lastRequestType;
     private ServerSocket _serverSocket;
 
     public Server (int port){
         _port=port;
     }
 
     public void run()
     {
         try{
             _serverSocket = new ServerSocket(_port);
             while (true){
                 Socket connection=_serverSocket.accept();
                 RequestHandler handler=new RequestHandler(connection);
                 handler.start();
                 connection=null;
             }
         }
        catch (BindException e){
            System.out.println("Address In Use!");
        }
         catch (SocketException e){
             System.out.println("swallowing expected shutdown exception");
         }
         catch (IOException e){
            System.out.println("Unknown Exception!");
             e.printStackTrace();
         }
         finally{
             closeServerSocket();
         }
 
         }
 
 
     public void kill(){
         closeServerSocket();
     }
     
 
     private void closeServerSocket(){
         try{
             if(_serverSocket != null && !_serverSocket.isClosed()){
                 _serverSocket.close();
             }
         }
         catch (IOException e){
             System.out.println("IOException when closing server socket");
             e.printStackTrace();
         }
 
     }
     
 
 
 
 }
