 package de.thm.ateam.memory.network;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 import de.thm.ateam.memory.engine.type.*;
 
 /**
  * 
  * Service which starts a Task, responsible for handling connecting Clients
  *
  */
 public class HostService extends Service{
 
   private static final String TAG = HostService.class.getSimpleName();
 
   public static ArrayList<NetworkPlayer> clients = null;
   public static int current = 0;
   /** Clients can join the game or not */
   public static boolean gameAvailable = false;
   private static ServerSocket servSock = null;
   private static Socket clientSock = null;
 
   @Override
   public IBinder onBind(Intent arg0) {
     return null;
   }
 
   @Override
   public int onStartCommand(Intent intent, int flags, int startId){
     Log.i(TAG, "Start Host Service!");
     clients = new ArrayList<NetworkPlayer>();
     gameAvailable = true;
     Thread t = new Thread(new ServerTask());
     t.start();
 
     return START_NOT_STICKY;
   }
 
   /**
    * Finds a player from the client list by a given socket
    * @param sock Socket, which is compared to each client
    * @return null if no Player was found, the found NetworkPlayer otherwise
    */
   public static NetworkPlayer findPlayerBySocket(Socket sock){
     if(clients.size() == 0) return null;
     for(NetworkPlayer p : clients){
      if(p.sock != null && p.sock.getLocalAddress().equals(sock.getLocalAddress())){
         return p;
       }
     }
     return null;
   }
 
   @Override
   public void onDestroy(){
     super.onDestroy();
     Log.i(TAG, "SERVICE WAS STOPPED!");
     try {
       servSock.close();
       clientSock.shutdownInput();
       clientSock.shutdownOutput();
     } catch (IOException e) {
       Log.e(TAG, "Socket could not be closed");
       e.printStackTrace();
     }
     stopSelf();
   }
 
   private class ServerTask implements Runnable{
 
     public void run() {
       try {
         servSock = new ServerSocket(6666);
 
         Log.i(TAG, "Server is waiting for clients...");
 
         while(true){
           clientSock = servSock.accept();
           if(!gameAvailable){
             Log.i(TAG, "Client tried to connect, but Host doesn't accep connections anymore.");
           }else{
             Log.i(TAG, "A Client has connected!");
             // add this client to the client List
             clients.add(new NetworkPlayer(clientSock));
             // create a new thread for this client
             Thread t = new Thread(new ClientConnection(clientSock));
             t.start();
           }
         }
 
       } catch (IOException e) {
         Log.e(TAG, "Server: Port 6666 could not be used ");
       }
 
     }
 
   }
 
   /**
    * Compute the winner of  a network game
    * @return the Player who has won if he/she has the single most pairs
    *          if there is more than one player with the highest amount of pairs
    *          it is a draw and null is returned 
    */
   public static Player computeWinner() {
     if(clients.size() == 0) return null;
     int highscore = 0;
     Player winner = null;
     int numberOfWinners = 0;
     for(Player p : clients){
       if(p.roundHits > highscore){
         highscore = p.roundHits;
         numberOfWinners = 1;
         winner = p;
       }else if(p.roundHits == highscore){
         numberOfWinners++;
       }
       //reset the hits of each player for the next round
       p.roundHits = 0;
     }
     if(numberOfWinners == 1){
       return winner;
     }
     return null;
 
   }
 
 }
