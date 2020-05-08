 package cs420.buySell.client;
 
 import java.io.IOException;
 import java.net.*;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import cs420.buySell.gui.*;
 
 /**
  * ClientSocket.java
  *
  * This class will handle communication between the different clients. It will send the update messages
  * when necessary and call an update method when a message is received.
  */
 public class ClientSocket implements Runnable{
 
     private DatagramSocket socket;
     private BuySellUI ui;
     private static int port;
 
     /**
      * Constructor method that creates the Datagram socket to send and receive messages.
      */
     public ClientSocket(BuySellUI ui) {
         try {
             Random rand = new Random();
             port = 3000;
             socket = new DatagramSocket(port);
 
         } catch (SocketException e) {
             e.printStackTrace();
         }
         
         this.ui = ui;
 
     }
 
     public void sendUpdateBuy(LinkedList<Client> clients) {
         for(Client client : clients) {
             InetAddress address = client.getAddress();
             int port = client.getPort();
             System.out.println("Sending to: " + address);
 
             String output = "Update_want_to_buy";
 
             sendData(output, address, port);
         }
     }
 
     public void sendUpdateSell(LinkedList<Client> clients) {
         for(Client client : clients) {
             InetAddress address = client.getAddress();
             int port = client.getPort();
             System.out.println("Sending to: " + address);
 
             String output = "Update_want_to_sell";
 
             sendData(output, address, port);
         }
     }
 
     public void sendData(String sendMessage, InetAddress client, int port) {
         try {
             byte[] data = sendMessage.getBytes();
 
             DatagramPacket sendPacket = new DatagramPacket(data, data.length, client, port);
             System.out.println("Sending packet");
             socket.send(sendPacket);
 
 
         } catch(IOException ioException) {
             ioException.printStackTrace();
         }
 
     }
 
     public void waitForPackets() {
         try{
             byte[] data = new byte[1024];
             DatagramPacket received = new DatagramPacket(data, data.length);
             while(true) {
 
 
 
                socket.receive(received);
                String output = new String(received.getData());
 

                interpret(output);
 
 
 
             }
         } catch(IOException e) {
             e.printStackTrace();
         }
 
     }
 
 
 
     public void interpret(String message) {
        if (message.contains("Update_want_to_sell")) {
             ui.refreshWTS();
 
        } else if (message.contains("Update_want_to_buy")) {
             ui.refreshWTB();
 
         }
     }
 
 
 
     public List<Client> parseClients(ResultSet rs) throws SQLException, UnknownHostException {
         List<Client> clients = new ArrayList<Client>();
         while(rs.next()) {
             String address = rs.getString(1);
             String port = rs.getString(2);
 
             int outPort = Integer.parseInt(port);
             InetAddress outAddress = InetAddress.getByName(address);
 
             Client client = new Client(outAddress, outPort);
             clients.add(client);
         }
 
         return clients;
     }
 
     public static int getPort(){
         return port;
     }
 
 
     @Override
     public void run() {
         this.waitForPackets();
     }
 }
