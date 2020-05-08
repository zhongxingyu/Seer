 package server.rooms;
 
 import java.io.IOException;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import server.lists.*;
 import server.*;
 
 public class ChatRoom implements Runnable {
     public final String name;
     private final RoomList rooms;
     private final ChatUserList connectedClients;
     private LinkedBlockingQueue<String> messageBuffer = new LinkedBlockingQueue<String>();
     private Thread self;
 
     public ChatRoom(String name, RoomList rooms, ConnectionHandler connection)
             throws IOException {
         this.name = name;
         this.rooms = rooms;
         this.connectedClients = new ChatUserList(name);
         rooms.add(this);
         connectedClients.add(connection);
         self = new Thread(this);
         System.out.println("  Room: " + name + " - " + "Created");
         self.start();
     }
 
     public void run() {
         System.out.println("  Room: " + name + " - " + "Input Thread Started");
         while (true)
             try {
                 connectedClients.informAll("message " + messageBuffer.take());
                 System.out.println("  Room: " + name + " - " + "Message Sent");
             } catch (InterruptedException e) {
                 System.out.println("  Room: " + name + " - "
                         + "Stopping Input Thread");
                 break;
             }
 
         System.out.println("  Room: " + name + " - " + "Stopped Input Thread");
         cleanup();
         System.out.println("  Room: " + name + " - " + "Cleanup complete");
     }
 
    public synchronized void addUser(ConnectionHandler connection)
             throws IOException {
         if (self.isAlive())
             connectedClients.add(connection);
         else
             throw new IOException("Room no longer exists");
     }
 
     public synchronized void removeUser(ConnectionHandler connection) {
         connectedClients.remove(connection);
         if (connectedClients.size() <= 0)
             self.interrupt();
     }
 
     private void cleanup() {
         System.out.println("  Room: " + name + " - "
                 + "Removing from server listing");
         rooms.remove(this);
     }
 
     public void updateQueue(String info) {
         messageBuffer.add(info);
     }
 }
