 package com.thoughtworks.socket;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class SocketClients {
     private int expectedCount;
     private List<SocketClient> clients = new ArrayList<SocketClient>();
     private int intervalInSeconds;
 
     public SocketClients(int expectedCount, int intervalInSeconds) {
         this.expectedCount = expectedCount;
         this.intervalInSeconds = intervalInSeconds;
     }
 
     public void tryToConnect(String host, int port) {
         for (int i = 0; i < expectedCount; i++) {
             if (isErrorOccurred()) {
                 break;
             }
 
             Utils.log("Start to open socket %d", i);
             SocketClient client = new SocketClient(i).connect(host, port);
             clients.add(client);
             Utils.sleepInSeconds(intervalInSeconds);
         }
     }
 
     public boolean isErrorOccurred() {
         for (SocketClient client : clients) {
             if (client.isErrorOccurred()) {
                 Utils.log("Error occurred when connecting socket %d", client.getIndex());
                 return true;
             }
         }
         return false;
     }
 
     public void closeAll() {
         for (SocketClient client : clients) {
             client.closeQuietly();
         }
     }
 }
