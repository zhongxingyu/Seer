 package com.network.monitor.service;
 
 import com.network.monitor.domain.Server;
 import com.network.monitor.view.MainForm;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.List;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author
  */
 class MonitoringInfoServer implements Runnable {
 
     ServerSocket socket;
     MainForm mainForm;
     HealthCheckService healthCheckService;
     private static final Logger LOGGER = Logger.getLogger(MonitoringInfoServer.class);
 
     public MonitoringInfoServer(ServerSocket ds, MainForm mainForm) {
         socket = ds;
         this.mainForm = mainForm;
         healthCheckService = new HealthCheckService(mainForm);
 
     }
 
     @Override
     public void run() {
 
         LOGGER.info("MonitoringInfoServer started...");
 
         while (true) {
             try {
 
                 Socket s = socket.accept();
                 InputStream is = s.getInputStream();
                 ObjectInputStream ois = new ObjectInputStream(is);
                Server server = (Server) ois.readObject();;
 
                 LOGGER.info("Received info from Server: " + server.getServerInfo().getGeneralInfo().getComputerName());
 
                 List<Server> servers = mainForm.getServersList();
                 if (servers.isEmpty()) {
                     mainForm.addServer(server);
                 } else {
                     boolean found = false;
                     int index = -1;
                     for (Server existingServer : servers) {
                         index++;
                         if (server.getServerInfo().getGeneralInfo().getComputerName()
                                 .equals(existingServer.getServerInfo().getGeneralInfo().getComputerName())) {
                             found = true;
                         }
                     }
                     if (!found) {
                         mainForm.addServer(server);
                     } else {
                         mainForm.updateServerInfo(index, server);
                     }
                 }
 
                 is.close();
                 s.close();
 
                 healthCheckService.checkServer(server);
 
             } catch (IOException ex) {
                 ex.printStackTrace();
             } catch (ClassNotFoundException ex) {
                 ex.printStackTrace();
             }
 
         }
     }
 }
