 package net.ae97.teamstats.networking;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.util.Iterator;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.ae97.teamstats.ClientRequest;
 
 /**
  * @version 0.1
  * @author Lord_Ralex
  */
 public class PacketListener extends Thread {
 
     private final ConcurrentLinkedQueue<Packet> queue = new ConcurrentLinkedQueue<Packet>();
     private final WatchableObjectInputStream inputStream;
 
     public PacketListener(InputStream input) throws IOException {
         super();
         inputStream = new WatchableObjectInputStream(input);
     }
 
     public Packet getNextPacket(byte idOfPacket, boolean isReply) {
         Iterator<Packet> itt;
         synchronized (queue) {
             Packet pk = queue.peek();
             while (pk != null && !pk.isAlive()) {
                 queue.poll();
                 pk = queue.peek();
             }
             itt = queue.iterator();
         }
         while (itt.hasNext()) {
             Packet next = itt.next();
             if ((next.getID() == idOfPacket) && next.isAlive()) {
                 next.kill();
                 return next;
             }
         }
         //add a delay to the packet check
         DelayThread thread = new DelayThread();
         thread.start();
         while (thread.isAlive()) {
         };
         return getNextPacket(idOfPacket, isReply);
     }
 
     public Packet getNextPacket(byte idOfPacket) {
         return getNextPacket(idOfPacket, false);
     }
 
     public Packet getNextPacket(ClientRequest idOfPacket, boolean isReply) {
         return getNextPacket(idOfPacket.getID(), isReply);
     }
 
     public Packet getNextPacket(ClientRequest idOfPacket) {
         return getNextPacket(idOfPacket, false);
     }
 
     public void run() {
         boolean run = true;
         while (run) {
             try {
                 Object obj = inputStream.readObject();
             } catch (IOException ex) {
                 if (inputStream.isClosed()) {
                     Logger.getLogger(PacketListener.class.getName()).log(Level.SEVERE, "Stream is closed", ex);
                     run = true;
                 } else {
                     Logger.getLogger(PacketListener.class.getName()).log(Level.SEVERE, "IOException", ex);
                 }
             } catch (ClassNotFoundException ex) {
                 Logger.getLogger(PacketListener.class.getName()).log(Level.SEVERE, "Class read was incorrect", ex);
             }
         }
     }
 
     private class DelayThread extends Thread {
 
         public void run() {
             synchronized (DelayThread.this) {
                 try {
                     DelayThread.this.wait(1 * 1000);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(PacketListener.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
 }
