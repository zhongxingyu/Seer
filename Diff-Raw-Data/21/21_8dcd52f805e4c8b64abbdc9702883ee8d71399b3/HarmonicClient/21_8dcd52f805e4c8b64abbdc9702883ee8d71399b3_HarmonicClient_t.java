 package br.unb.tr2.harmonic.client;
 
 import br.unb.tr2.harmonic.entity.CalculationInterval;
 import br.unb.tr2.harmonic.entity.Server;
 import br.unb.tr2.harmonic.exceptions.ConnectionFailedException;
 import br.unb.tr2.zeroconf.DiscoveryService;
 import br.unb.tr2.zeroconf.ServiceAnnouncement;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.util.logging.Logger;
 
 /**
  * Copyright (C) 2013 Loop EC - All Rights Reserved
  * Created by sandoval for harmonic-server
  */
 public class HarmonicClient {
 
     private static HarmonicClient instance;
 
     private DiscoveryService discoveryService;
 
     private Server server = null;
 
     private Logger logger = Logger.getLogger("mainClient");
 
     private HarmonicClient() {
 
         discoveryService = DiscoveryService.getInstance();
 
         while (true) {
             if (server == null)
                 findServer();
 
             try {
                 server.connect();
                 while (true) {
                     CalculationInterval interval = server.getCalculationInterval();
                     interval.calculate();
                     server.sendCalculationInterval(interval);
                 }
             } catch (ConnectionFailedException e) {
                 logger.severe(e.getMessage());
                server = null;
             }
         }
 
     }
 
     public static void main(String[] args) throws IOException {
         HarmonicClient.getInstance();
     }
 
     public static HarmonicClient getInstance() {
         if (instance == null)
             instance = new HarmonicClient();
         return instance;
     }
 
     private void findServer() {
         while (server == null) {
             /**
              * The code in this loop searches for an available Server when there isn't any.
              */
 
             Integer leastRunningClients = Integer.MAX_VALUE;
             try {
                 ServerSocket serverSocket = new ServerSocket(0);
                 serverSocket.setSoTimeout(1000);
                 discoveryService.broadcastServiceAnnouncement(new ServiceAnnouncement("Harmonic Series Calculation Client._tcp.local",
                         (long) serverSocket.getLocalPort(), serverSocket.getInetAddress()));
                 try {
                     while (true) {
                         Socket socket = serverSocket.accept();
                         ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                         try {
                             ServiceAnnouncement serviceAnnouncement = (ServiceAnnouncement)ois.readObject();
                             ois.close();
                             socket.close();
                             if (!"Harmonic Series Calculation Server._tcp.local".equals(serviceAnnouncement.getService()))
                                 continue;
                             if ((Integer)serviceAnnouncement.getParameters().get("clientInstances") < leastRunningClients) {
                                 leastRunningClients = (Integer)serviceAnnouncement.getParameters().get("clientInstances");
                                 server = (Server)serviceAnnouncement.getParameters().get("server");
                             }
                         } catch (ClassNotFoundException e) {
                             e.printStackTrace();
                         }
                     }
                 } catch (SocketTimeoutException e) {
                     serverSocket.close();
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
 }
