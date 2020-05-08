 /*
  *  Copyright Mattias Liljeson Sep 14, 2011
  */
 package gameserver;
 
 import common.*;
 import java.awt.Color;
 import java.lang.reflect.Field;
 import java.net.*;
 
 /**
  *
  * @author Mattias Liljeson <mattiasliljeson.gmail.com>
  */
 public class ClientConnection implements Runnable{
     private Channel channel;
     private ClientHandler clientHandler;
     private int id;
     private boolean done = false;
 
     public ClientConnection(Socket socket, ClientHandler clientHandler, int id){
         channel = new Channel(socket);
         channel.openStreams();
         this.clientHandler = clientHandler;
         this.id = id;
 
         System.out.println("ClientConnection started");
     }
 
     @Override
     public void run(){
         // Fetch info about the car color etc and create a car and add the
         // client to the clienthandler before entering the game loop.
        done = !initClientData();
         
         // Enter main loop.
         KeyStates keyStates = null;
         while(!done){
             //Fetch keystates
             try{
                 keyStates = (KeyStates)channel.readObject();
             }catch(Channel.ConnectionLostException ex){
                 clientHandler.removeClient(id);
                 close();
                 done = true;
             }
 
             if(keyStates != null){
                 //Send em' up to the top
                 clientHandler.updateKeyStates(id, keyStates);
             }
         }
     }
     
     public boolean close(){
         boolean success = true;
         done = true;
 
         // HACK: Wait for the objects own thread to end its while-loop.
         // TODO: Use join instead?
         try{
             Thread.sleep(1000); 
         }catch(InterruptedException ignore){}
         channel.closeStreams();
 
         // Leave time for the client to close its streams
         try{
             Thread.sleep(1000); 
         }catch(InterruptedException ignore){}
         channel.closeSockets();
 
         return success;
     }
 
     public boolean initClientData(){
         boolean success = true;
         ClientData clientData = null;
         try{
             clientData = (ClientData)channel.readObject();
         }catch(Channel.ConnectionLostException ex){
             close();
             success = false;
         }
         
         if(success){
             //Turn string to actual color
             Color color = null;
             try {
                 Field field = Class.forName("java.awt.Color").getField(clientData.carColor);
                 color = (Color)field.get(null);
             } catch (Exception e) {
                 color = new Color(0); // Not defined. Use black as color
             }
             
             Car car = new Car(color);
             clientHandler.addClient(id, this, car);
         }
         return success;
     }
     
     public boolean poll(){
         boolean result = false;
         try{
             result = channel.sendObject(new KeyStatesReq());
         }catch(Channel.ConnectionLostException ex){
             clientHandler.removeClient(id);
             close();
         }
         return result;
     }
 
     public boolean sendRaceUpdate(RaceUpdate update){
         boolean result = false;
         try{
             return channel.sendObject(update);
         }catch(Channel.ConnectionLostException ex){
             clientHandler.removeClient(id);
             close();
         }
         return result;
     }
 }
