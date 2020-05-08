 package com.thoughtworks.socket;
 
 public class Main {
 
     private void run(String host, int port, int clientCount, int connectIntervalInSeconds) {
 
         SocketClients clients = new SocketClients(clientCount, connectIntervalInSeconds);
         clients.tryToConnect(host, port);
 
         if (keepThisProcessForAWhile(clientCount, clients)) {
            int sleepTime = clientCount * connectIntervalInSeconds; // 240 will spend 1 hour(240 * 15=3600s)
             Utils.log("Will sleep for %d seconds before close all sockets", sleepTime);
             Utils.sleepInSeconds(sleepTime);
         }
         clients.closeAll();
     }
 
     private boolean keepThisProcessForAWhile(int count, SocketClients clients) {
        return !clients.isErrorOccurred() || count > 10; // We will
     }
 
     public static void main(String[] args) {
         String host = "10.18.2.163";
         int port = 7222;
 
        int clientCount = 240;
         int connectIntervalInSeconds = 15;
 
         new Main().run(host, port, clientCount, connectIntervalInSeconds);
     }
 
 }
