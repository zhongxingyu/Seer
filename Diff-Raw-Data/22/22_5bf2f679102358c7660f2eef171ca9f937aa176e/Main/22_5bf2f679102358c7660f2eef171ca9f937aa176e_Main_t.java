 package com.thoughtworks.socket;
 
 public class Main {
 
     private void run(String host, int port, int clientCount, int connectIntervalInSeconds) {
 
         SocketClients clients = new SocketClients(clientCount, connectIntervalInSeconds);
         clients.tryToConnect(host, port);
 
        if (keepThisProcessForAWhile(clients)) {
             int sleepTime = 5 * 60 * 60; // will sleep 5 hours
             Utils.log("Will sleep for %d seconds before close all sockets", sleepTime);
             Utils.sleepInSeconds(sleepTime);
         }
         else{
             Utils.log("Error occurred, or connected socket clients count is less than 10, we will close this process.");
         }
         clients.closeAll();
     }
 
    private boolean keepThisProcessForAWhile(SocketClients clients) {
        return !clients.isErrorOccurred() || clients.size() >= 10; // We will
     }
 
     public static void main(String[] args) {
         if(args.length < 3){
            Utils.log("Usage: java -jar java-socket-test.jar host:port clientCount connectIntervalInSeconds");
             Utils.log("Params: clientCount is the total count of socket clients");
             Utils.log("Params: connectIntervalInSeconds defines the interval before opening a new socket client");
            Utils.log("Example: java -jar java-socket-test.jar 10.18.2.163:7222 120 15\n");
         }
 
         String host = "10.18.2.163";
         int port = 7222;
 
         int clientCount = 120;
         int connectIntervalInSeconds = 15;
 
         new Main().run(host, port, clientCount, connectIntervalInSeconds);
     }
 
 }
