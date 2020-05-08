 /*
  *  Copyright Mattias Liljeson Sep 14, 2011
  */
 package gameserver;
 
 import common.*;
 import java.io.*;
 import java.net.*;
 
 /**
  *
  * @author Mattias Liljeson <mattiasliljeson.gmail.com>
  */
 public class ClientConnection implements Runnable{
         private Socket socket;
         private ObjectInputStream inStream;
         private ObjectOutputStream outStream = null;
         private ClientHandler clientHandler;
         private int id;
         private boolean done = false;
         
         public ClientConnection(Socket socket, ClientHandler clientHandler, int id){
             this.socket = socket;
             this.clientHandler = clientHandler;
             this.id = id;
             
             try{
                 outStream = new ObjectOutputStream(socket.getOutputStream());
                 inStream = new ObjectInputStream(socket.getInputStream());
             }catch(IOException ex){
                 System.out.println("Error in stream creation");
             }
            System.out.println("ClientConnection started");
         }
         
         @Override
         public void run(){
             KeyStates keyStates = null;
             while(!done){
                 //Fetch keystates
                 try{
                     keyStates = (KeyStates)inStream.readObject();
                 }
                 catch(ClassNotFoundException ex)
                 {
                     System.out.println("Other Class than KeyStates received, " + ex);
                 }
                 catch(IOException ex){
                     System.out.println("Problem with socket input");
                    close();
                 }
 
                 if(keyStates != null){
                     System.out.println("Received payload:");
                     System.out.println(keyStates.toString());
                    //Send em' up to the top
                    clientHandler.updateKeyStates(id, keyStates);
                 }
             }
         }
         
         public boolean close(){
             boolean success = true;
             done = true;
             
             // Wait for the objects own thread to end its while-loop.
             // TODO: use join instead?
             try{
                 Thread.sleep(1000); 
             }catch(InterruptedException ignore){}
             
             // Close streams
             try{
                 if(outStream != null)
                     outStream.close();
                 if(inStream != null)
                     inStream.close();
             }catch(IOException ex){
                 System.out.println("Error when closing streams");
                 success = false;
             }
             
             // Leave time for the client to close its streams
             try{
                 Thread.sleep(1000); 
             }catch(InterruptedException ignore){}
             
             return success;
         }
         
         public boolean poll(){
             return sendObject(new KeyStatesReq());
         }
         
         public boolean sendObject(Object obj){
             boolean success = false;
             try{
                 if(outStream != null){
                     outStream.writeObject(obj);  // send serilized payload
                     outStream.flush();
                     success = true;
                 }
             }catch(IOException ex){
                 System.out.println("Error when sending object through stream");
                 success = false;
             }
             return success;
         }
         
         public boolean sendRaceUpdate(RaceUpdate update){
             return sendObject(update);
         }
     }
