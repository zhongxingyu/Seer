 /*
  * Copyright (C) 2013 MineStar.de 
  * 
  * This file is part of ConAir.
  * 
  * ConAir is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * ConAir is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with ConAir.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.conair.network;
 
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import de.minestar.conair.core.Core;
 
 public class ChatServer implements Runnable {
 
     private final ByteBuffer networkBuffer;
 
     private Selector selector;
 
     private ServerSocketChannel serverSocket;
 
     private boolean isRunning;
 
     private PacketHandler packetHandler;
 
     private List<String> addressWhitelist;
 
     public ChatServer(int port, List<String> addressWhitelist) throws Exception {
 
         this.networkBuffer = ByteBuffer.allocateDirect(4096);
 
         this.packetHandler = new PacketHandler(networkBuffer);
 
         this.selector = Selector.open();
 
         // Listening on the port
         serverSocket = ServerSocketChannel.open();
         serverSocket.socket().bind(new InetSocketAddress(port));
 
         // Non-Blocking for Selector activity
         serverSocket.configureBlocking(false);
         serverSocket.register(selector, SelectionKey.OP_ACCEPT);
 
         isRunning = true;
 
         if (addressWhitelist == null) {
             addressWhitelist = new ArrayList<String>();
         }
         if (addressWhitelist.isEmpty()) {
             addressWhitelist.add("127.0.0.1");
         }
         this.addressWhitelist = addressWhitelist;
     }
 
     @Override
     public void run() {
         try {
             while (isRunning) {
                 int rdyChannels = selector.select();
                 // No channel want something
                 if (rdyChannels == 0) {
                     continue;
                 }
 
                 // Iterate over all channel which want something
                 Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                 while (it.hasNext()) {
                     SelectionKey key = it.next();
                     // New client wants to connect
                     if (key.isAcceptable()) {
                         // accept new client
                         onClientAccept();
                     }
                     // client want to send something
                     if (key.isReadable()) {
                         onClientRead(key);
                     }
                     // client can receive something
                     if (key.isWritable()) {
                         onClientWrite(key);
                     }
                     it.remove();
                 }
 
             }
         } catch (Exception e) {
             e.printStackTrace();
             Logger.getLogger(Core.NAME).throwing("de.minestar.conair.core.network.ChatServer", "run", e);
             isRunning = false;
         }
     }
 
     public void stop() {
         this.isRunning = false;
     }
 
     /*
      * ACCEPTING
      */
 
     public void onClientAccept() throws Exception {
         // accept new client
         SocketChannel clientSocket = serverSocket.accept();
 
         // Is client allowed to connect?
         String address = clientSocket.getRemoteAddress().toString();
 
         // Remove the port number
        int i = address.indexOf(':');
         if (i != -1)
             address = address.substring(0, i);
 
        if (address.startsWith("/")) {
            address = address.substring(1, address.length());
        }

         // Client is not allowed to connect - refuse connection
         if (!addressWhitelist.contains(address)) {
             clientSocket.close();
            System.out.println("Client is not whitelisted: " + address);
            return;
         }
 
         clientSocket.configureBlocking(false);
         clientSocket.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE).attach(new ConnectedClient(clientSocket.getRemoteAddress().toString()));
     }
 
     /*
      * READING
      */
     public void onClientRead(SelectionKey key) throws Exception {
         if (!(key.channel() instanceof SocketChannel)) {
             return;
         }
         // Read into the clients specific buffer
         SocketChannel channel = (SocketChannel) key.channel();
         ConnectedClient client = (ConnectedClient) key.attachment();
         // When readfrom fails the client has disconnected
         if (!client.readFrom(channel)) {
             key.cancel();
             System.out.println("Client is disconnected!");
         }
 
         if (packetHandler.isPacketComplete(client.getClientBuffer())) {
             // extract the packet
             NetworkPacket packet = packetHandler.extractPacket(client.getClientBuffer());
 
             // if we have found a packet, we handle it...
             if (packet != null) {
                 handlePacket(client, packet);
             }
 
             // clear the clientBuffer
             client.getClientBuffer().clear();
         }
     }
 
     // Handle a single packet
     private void handlePacket(ConnectedClient src, NetworkPacket packet) {
         // We have a broadcast server - broadcast all packages
         broadcastPacket(src, packet);
     }
 
     // Deliver the packet the all other clients
     private void broadcastPacket(ConnectedClient src, NetworkPacket packet) {
         Set<SelectionKey> keys = selector.keys();
 
         // pack the packet
         this.packetHandler.packPacket(packet);
 
         for (SelectionKey key : keys) {
             if (!(key.channel() instanceof SocketChannel))
                 continue;
 
             ConnectedClient client = (ConnectedClient) key.attachment();
 
             // ignore if the client is the sender
 //            if (client.equals(src))
 //                continue;
 
             // add packetdata to clientbuffer
             client.addPacket(networkBuffer);
         }
 
         // clear the networkBuffer
         networkBuffer.clear();
     }
 
     /*
      * WRITING
      */
     private void onClientWrite(SelectionKey key) throws Exception {
         if (!(key.channel() instanceof SocketChannel)) {
             return;
         }
 
         ConnectedClient client = (ConnectedClient) key.attachment();
         if (client.hasDataToSend()) {
             // If write fails the client has disconnected
             if (!client.write((SocketChannel) key.channel())) {
                 key.cancel();
             }
         }
     }
 }
