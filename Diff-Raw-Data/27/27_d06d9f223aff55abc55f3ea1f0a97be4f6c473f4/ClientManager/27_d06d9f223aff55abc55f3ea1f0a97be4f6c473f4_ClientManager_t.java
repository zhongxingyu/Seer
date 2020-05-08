 package aigilas.net;
 
 import aigilas.Config;
 import sps.core.Logger;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class ClientManager {
     final List<MessageHandler> clients = new ArrayList<MessageHandler>();
     private final HashMap<Integer, Integer> addressToIndex = new HashMap<Integer, Integer>();
     ServerSocket server;
 
     Thread clientListener;
     private boolean __otherServerExists = false;
 
     public ClientManager() {
         try {
             this.server = new ServerSocket(Config.get().port());
             clientListener = new Thread(new Runnable() {
                 public void run() {
                     while (!Thread.interrupted()) {
                         try {
                             Socket client = server.accept();
 
                             clients.add(new MessageHandler(client));
                             clients.get(clients.size() - 1).owner = "SERVER";
                             addressToIndex.put(client.getPort(), clients.size() - 1);
                         }
                         catch (IOException e) {
                             Logger.exception(e);
                         }
                     }
                 }
             }, "ClientManager");
             clientListener.start();
         }
         catch (IOException e1) {
             __otherServerExists = true;
             Logger.info("SERVER: Failure to start. If this isn't the host machine, then this message is harmless.");
         }
     }
 
    private int clientIndex = 0;

     public Message readMessage() {
         Message result;
        for (clientIndex = 0; clientIndex < clients.size(); clientIndex++) {
            result = clients.get(clientIndex).readInboundMessage();
             if (result != null) {
                 return result;
             }
         }
         return null;
     }
 
     public int size() {
         return clients.size();
     }
 
     public void announce(Message contents) {
        for (clientIndex = 0; clientIndex < clients.size(); clientIndex++) {
            clients.get(clientIndex).sendOutboundMessage(contents);
         }
     }
 
     public void send(Message contents) {
         clients.get(addressToIndex.get(contents.LocalPort)).sendOutboundMessage(contents);
     }
 }
