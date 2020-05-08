 package jacs.server;
 
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import javax.swing.JFrame;
 
 public class Server
 {
     private ServerSocket myServer;
     private Client myClient;
 
     public Server()
 	{
 		try
 		{
 			showStatus("Starting Server.");
 			myServer = new ServerSocket(9999);
 			while(true)
 			{
 				Socket mySocket = myServer.accept();
 				myClient = new Client(this,mySocket);
 				showStatus("["+myClient.CLIENT_IP+"] Client Connected.");
 				myClient.start();
 			}
 		}
 		catch(Exception e)
 		{
 			killServer("ERROR->Server() : "+e.toString());
 		}
 		finally
 		{
 			killServer("ERROR->Server Error.");
 		}
     }
     
     
     public void showStatus(String str)
     {
         try
         {
             str = "->"+str;
             System.out.println(str);
         }
         catch(Exception e)
         {
             System.out.println(e.toString());
         }
     }
     
     public void killServer(String str)
     {
         try
         {
             myServer.close();
             showStatus("Server Stoped.");
             showStatus(str);
         }
         catch(Exception e)
         {
             showStatus("Server Error.");
         }
     }
     
     public void sendData(String msg){
     	myClient.sendData(msg);
     }
     
    public String getMsg(){
    	return myClient.getMsg();
    }
 }
