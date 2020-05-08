 /*
  *  Copyright Mattias Liljeson Sep 14, 2011
  */
 package gameserver;
 
 import common.*;
 import java.awt.Color;
 import java.io.*;
 import java.util.*;
 
 /**
  *
  * @author Mattias Liljeson <mattiasliljeson.gmail.com>
  */
 public class GameServer {
     private int pollingRate = 60;
     private Map<Integer,Car> cars;
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
        System.out.println("Game Server started");
         
         GameServer gameServ = new GameServer();
         gameServ.run(); 
     }
     
     public GameServer(){
         cars = new HashMap<Integer,Car>();
     }
     
     public void run(){
         //Create clientHandlerThread thread to handle new map requests
         ClientHandler clientHandler = new ClientHandler(this);
         Thread clientHandlerThread = new Thread(clientHandler);
         clientHandlerThread.start();
         
         //Enter main loop
         boolean done = false;
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         String msg = "";
         long prevTime = System.currentTimeMillis();
         
         while(!done){
             // Handle exit msg
             try{
                 msg = br.readLine();
             }catch(IOException ignore){}
             if(msg.equals("exit")){
                System.out.println("Exiting from gs");
                 done = true;
             }
             
             //Poll clients, update, send new positions to clients
             if((prevTime - System.currentTimeMillis()) > (1000/pollingRate)){
                 clientHandler.pollClients();
                 
                 RaceUpdate update = new RaceUpdate(cars.size());
                 for(Map.Entry<Integer, Car> entry : cars.entrySet()){
                     Car car = entry.getValue();
                     car.update(pollingRate);
                     update.addCar(car.getPosition(), car.getColor());
                 }
                 clientHandler.sendRaceUpdate(update);
             }
             
         }
         System.exit(0);
     }
     
     public void updateKeyStates(int id, KeyStates keyStates){
         cars.get(id).setKeyStates(keyStates);
     }
     
 }
