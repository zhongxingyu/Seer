 package br.unb.tr2.harmonic.entity;
 
 import br.unb.tr2.harmonic.exceptions.ConnectionFailedException;
 
 import java.io.*;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.UUID;
 
 /**
  * Copyright (C) 2013 Loop EC - All Rights Reserved
  * Created by sandoval for harmonic-server
  */
 public class Server implements Serializable {
 
     private InetAddress address;
 
     private Long port;
 
     private UUID uuid;
 
     private Socket socket = null;
 
     public Server(InetAddress address, Long port) {
         this(address, port, UUID.randomUUID());
     }
 
     public Server(InetAddress address, Long port, UUID uuid) {
         this.address = address;
         this.port = port;
         this.uuid = uuid;
     }
 
     public InetAddress getAddress() {
         return address;
     }
 
     public Long getPort() {
         return port;
     }
 
     public UUID getUuid() {
         return uuid;
     }
 
     public void connect() throws ConnectionFailedException {
         socket = new Socket();
         try {
             System.out.println("Trying to connect to server " + address.getHostAddress() + ":" + port.intValue());
             socket.connect(new InetSocketAddress(address, port.intValue()), 5000);
         } catch (Exception e) {
             socket = null;
             e.printStackTrace();
             throw new ConnectionFailedException(e.getMessage());
         }
     }
 
 
     public CalculationInterval getCalculationInterval() throws ConnectionFailedException {
         CalculationInterval interval = null;
         try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
             oos.writeObject("CALCULATION INTERVAL REQUEST");
             oos.flush();
             interval = (CalculationInterval)ois.readObject();
         } catch (IOException e) {
             if (!socket.isConnected()) {
                 this.connect(); // May throw ConnectionFailedException while trying to connect
                 return getCalculationInterval();
             } else {
                 e.printStackTrace();
                 throw new ConnectionFailedException(e.getMessage());
             }
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
         return interval;
     }
 
     public void sendCalculationInterval(CalculationInterval interval) throws ConnectionFailedException {
         try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             oos.writeObject(interval);
             oos.flush();
         } catch (IOException e) {
             if (!socket.isConnected()) {
                 this.connect(); // May throw ConnectionFailedException while trying to connect
                 sendCalculationInterval(interval);
             } else {
                 e.printStackTrace();
                 throw new ConnectionFailedException(e.getMessage());
             }
         }
     }
 }
