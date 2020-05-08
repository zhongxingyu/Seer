 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dblike.client.service;
 
 import dblike.api.ServerAPI;
 import dblike.client.ActiveServer;
 import dblike.client.Client;
 import dblike.client.ClientStart;
 import dblike.service.InternetUtil;
 import java.rmi.AccessException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author wenhanwu
  */
 public class ServerListenerClient implements Runnable {
 
     private boolean runningFlag = true;
 
     public void setRunningFlag(boolean flag) {
         this.runningFlag = flag;
     }
     private Vector<ActiveServer> ActiveServerList;
 
     public ServerListenerClient() {
         this.ActiveServerList = ActiveServerListClient.getActiveServerList();
     }
 
     public boolean checkCurrentServer() {
         boolean flag = true;
         int currentIndex = ClientConfig.getCurrentServerIndex();
         flag = true;
         ActiveServer aServer = ActiveServerList.get(currentIndex);
         if (aServer.getStatus() == InternetUtil.getOK()) {
             aServer.setStatus(aServer.getStatus() - 1);
         } else {
             aServer.setStatus(aServer.getStatus() - 1);
             if (aServer.getStatus() == 0) {
                //ActiveServerListClient.removeServer(aServer.getServerIP(), aServer.getPort());
                 System.out.println("Server down!!!-- " + aServer.getServerIP() + ":" + aServer.getPort());
                 ClientStart.aClient.pickupNewServer();
                 flag = false;
             } else {
                 //System.out.println("Connection problem, wait to see..."+ aServer.getServerIP() + ":" + aServer.getPort());
             }
         }
         return flag;
     }
 
 
     public void waitForAWhile(int timeOut) {
         try {
             Thread.sleep(timeOut * 1000);
 
 
         } catch (InterruptedException ex) {
             Logger.getLogger(ServerListenerClient.class
                     .getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     public void run() {
         while (runningFlag) {
             checkCurrentServer();
             waitForAWhile(InternetUtil.getTIMEOUT());
         }
 
     }
 }
