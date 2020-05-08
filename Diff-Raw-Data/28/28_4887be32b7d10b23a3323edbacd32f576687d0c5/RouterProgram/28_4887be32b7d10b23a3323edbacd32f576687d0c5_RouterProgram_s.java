 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package routerprogram;
 import java.net.*;  
 import java.io.*; 
 import java.util.*;
 /**
  *
  * @author bmulvihill
  */
 public class RouterProgram {
     public static int SEQNUM;
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         Config c = Config.getInstance();
         c.setConfig();
         RoutingServer rs = new RoutingServer();
         rs.start();
         Timer t = new Timer();
         RoutingTick rt = new RoutingTick();
         //schedule routing updater to execute every 30 seconds or whatever the tick_time is set to
         t.scheduleAtFixedRate(rt, 3 * (c.tick_time * 1000), c.tick_time * 1000);
     }
 }
 
 class RoutingTick extends TimerTask{
     @Override
     public void run() {
         RoutingUpdater ru = new RoutingUpdater();
     }
 }
 
 class RoutingUpdater {
     Config c = Config.getInstance();
     // Packet List Contructor
     public RoutingUpdater(LinkStatePacket lsp, String originIP){
        for (Map.Entry<String, String> entry : c.routerNeighbors.entrySet()){
            if(!entry.getKey().equals(lsp.ownerIP) && !entry.getKey().equals(originIP)){
             forwardPacket(lsp, entry.getKey());
            }
         }
     }
     
     //Timer Task Constructor
     public RoutingUpdater() { 
         PacketList pl = PacketList.getInstance();
         pl.decrement();
         RouterProgram.SEQNUM += 1;
         System.out.println("in timer task");
         for (Map.Entry<String, String> entry : c.routerNeighbors.entrySet())
         {   
             LinkStatePacket lsp = new LinkStatePacket();
             lsp.setOwnerIP(c.ROUTER);
             lsp.setSeqNum(RouterProgram.SEQNUM);
             lsp.setTTL(5);
             lsp.setNeighbors(c.routerNeighbors);
             forwardPacket(lsp, entry.getKey());
             Logger.log("Packet: " + + lsp.seqNum + " sent to Router " + entry.getKey());
         }
        
     }
     
     private void forwardPacket(LinkStatePacket p, String destIP){
         try{
           System.out.println("Forwaring Packet: " + p.ownerIP + " | " + p.seqNum + " to Router " + destIP);
           Logger.log("Sending Packet: " + p.ownerIP + " | " + p.seqNum + " to Router " + destIP);
            Socket s = new Socket(destIP, Config.getInstance().serverPort);  
            OutputStream os = s.getOutputStream();  
            ObjectOutputStream oos = new ObjectOutputStream(os);  
            oos.writeObject(p);  
            oos.close();  
            os.close();  
            s.close();  
         }
         catch(Exception e) {
             System.out.println("An error occurred while attempted to forward packet " + p.seqNum + " to " + destIP);
             Logger.log("An error occurred while attempted to forward packet " + p.seqNum + " to " + destIP);
         }
     }
 }
