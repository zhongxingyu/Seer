 /*
  * Copyright (C) 2009  Lars Pötter <Lars_Poetter@gmx.de>
  * All Rights Reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 
 package org.FriendsUnited.NetworkLayer;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.nio.channels.ClosedChannelException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.nio.channels.spi.SelectorProvider;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.FriendsUnited.FriendWorker;
 import org.FriendsUnited.Protocol;
 import org.FriendsUnited.Packets.Packet;
 import org.FriendsUnited.Util.ByteConverter;
 import org.FriendsUnited.Util.Tool;
 import org.FriendsUnited.Util.TrippleMap;
 import org.FriendsUnited.Util.Option.IntegerOption;
 import org.FriendsUnited.Util.Option.OptionCollection;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  *
  * @author James Greenfield nio@flat502.com
  * see http://rox-xmlrpc.sourceforge.net/niotut/
  *
  */
 public class PacketTransmitter extends FriendWorker
 {
     private final Logger log = Logger.getLogger(this.getClass().getName());
     private OptionCollection cfg;
     private Router myRouter;
     private int myPort;
     private final InetAddress IpAddr;
     private final OptionCollection status;
 
     private Selector socketSelector;
 
     private final TrippleMap<SocketChannel, Location, NIOPacketQueue> connections;
 
     // The buffer into which we'll read data when it's available
     private final ByteBuffer readBuffer = ByteBuffer.allocate(8192);
 
     // A list of PendingChange instances
     private List<ChangeRequest> pendingChanges;
 
     // Status Information
     private final IntegerOption sendPackets;
     private final IntegerOption recievedPackets;
 
 
     public PacketTransmitter(final InetAddress IpAddr,
                              final OptionCollection cfg,
                              final Router myRouter,
                              final OptionCollection status)
     {
         super("PacketTransmitter");
         this.cfg = cfg;
         this.myRouter = myRouter;
         this.IpAddr = IpAddr;
         this.status = status;
         pendingChanges = new LinkedList<ChangeRequest>();
         myPort = 0;
         sendPackets = new IntegerOption("send Packets", 0);
         status.add(sendPackets);
         recievedPackets = new IntegerOption("recieved Packets", 0);
         status.add(recievedPackets);
         connections = new TrippleMap<SocketChannel, Location, NIOPacketQueue>();
 
         // Create a new selector
         try
         {
             socketSelector = SelectorProvider.provider().openSelector();
 
             // Create a new non-blocking server socket channel
             final ServerSocketChannel serverChannel = ServerSocketChannel.open();
             serverChannel.configureBlocking(false);
 
             // Bind the server socket to the specified address, use the first available Port
             final InetSocketAddress isa = new InetSocketAddress(IpAddr, 0);
             serverChannel.socket().bind(isa);
             myPort = serverChannel.socket().getLocalPort();
 
             // Register the server socket channel, indicating an interest in
             // accepting new connections
             serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
         }
         catch(final IOException e)
         {
             log.error(Tool.fromExceptionToString(e));
         }
     }
 
     @Override
     public final FriendWorker getDuplicate()
     {
         final PacketTransmitter res = new PacketTransmitter(IpAddr, cfg, myRouter, status);
         return res;
     }
 
     public final int getPort()
     {
         return myPort;
     }
 
 
     private void accept(final SelectionKey key)
     {
         try
         {
             // For an accept to be pending the channel must be a server socket channel.
             final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
 
             // Accept the connection and make it non-blocking
             final SocketChannel socketChannel = serverSocketChannel.accept();
             final Socket socket = socketChannel.socket();
             socketChannel.configureBlocking(false);
             final Location remloc = new Location(socket.getInetAddress(), socket.getPort());
             final NIOPacketQueue pq = new NIOPacketQueue(socketChannel, remloc, this);
             pq.setActive();
             myRouter.registerSendQueue(remloc, pq);
             connections.put(socketChannel, remloc, pq);
             // Register the new SocketChannel with our Selector, indicating
             // we'd like to be notified when there's data waiting to be read
             socketChannel.register(socketSelector, SelectionKey.OP_READ);
         }
         catch(final ClosedChannelException e)
         {
             log.error(Tool.fromExceptionToString(e));
         }
         catch(final IOException e)
         {
             log.error(Tool.fromExceptionToString(e));
         }
     }
 
     private void read(final SelectionKey key)
     {
         final SocketChannel socketChannel = (SocketChannel) key.channel();
         final NIOPacketQueue pq = connections.getByIndex(socketChannel);
 
         // Clear out our read buffer so it's ready for new data
         readBuffer.clear();
 
         // Attempt to read off the channel
         int numRead;
         try
         {
             numRead = socketChannel.read(readBuffer);
         }
         catch (final IOException e)
         {
             // The remote forcedly closed the connection, cancel
             // the selection key and close the channel.
             key.cancel();
 
             pq.setPassive();
 
             try
             {
                 socketChannel.close();
             }
             catch (final IOException e1)
             {
                 // I don't care
             }
             return;
         }
 
         if (numRead == -1)
         {
             // Remote entity shut the socket down cleanly. Do the
             // same from our end and cancel the channel.
             key.cancel();
 
             pq.setPassive();
 
             try
             {
                 socketChannel.close();
             }
             catch (final IOException e)
             {
                 // I don't care
             }
             return;
         }
 
         final ByteConverter bc = pq.getRecievedDataBuffer();
         bc.add(readBuffer.array(), numRead);
         // Check if we have a complete Packet in the buffer
         boolean mightHaveAnotherPacket = true;
         while(true == mightHaveAnotherPacket) // There might be two or more packets in there already
         {
             if(Packet.ENVELOPE_SIZE >= bc.size())
             {
                 // not enough bytes for another packet
                 // at least Envelope + one data byte is needed
                 mightHaveAnotherPacket = false;
             }
             else
             {
                 // Enough Bytes to be a complete packet
                 if(Protocol.MAGIC == bc.readShort(0))
                 {
                     // Is a valid Friends United Packet
                     final short lengthOfPacket = bc.readShort(2);
                     if(bc.size() <= lengthOfPacket)
                     {
                         if(true == myRouter.removePacketFromBuffer(bc,
                                                                    lengthOfPacket,
                                                                    pq.getremoteLocation(),
                                                                    pq) )
                         {
                             recievedPackets.inc();
                         }
                         // there might be another packet still in the buffer, ...
                     }
                     else
                     {
                         // This packet isn't completed so no further packets in Buffer
                         mightHaveAnotherPacket = false;
                     }
                 }
                 else
                 {
                     log.error("Recieve Magic Error !");
                     log.error(Tool.fromByteBufferToHexString(bc.toByteArray()));
                     // drop bytes until Magic found ? - NO !
                     // Remote entity is not a Friend United Entity
                     // Close the channel !
                     key.cancel();
 
                     pq.setPassive();
 
                     mightHaveAnotherPacket = false;
                     try
                     {
                         socketChannel.close();
                     }
                     catch (final IOException e)
                     {
                         // I don't care
                     }
                     return;
                 }
             }
         }
         // else we got to wait for some more bytes
     }
 
     private void write(final SelectionKey key)
     {
         final SocketChannel socketChannel = (SocketChannel) key.channel();
         final NIOPacketQueue pq = connections.getByIndex(socketChannel);
         final List<ByteBuffer> queue = pq.getSendQueue();
 
         if(true == queue.isEmpty())
         {
             // We wrote away all data, so we're no longer interested
             // in writing on this socket. Switch back to waiting for
             // data.
             key.interestOps(SelectionKey.OP_READ);
             return;
         }
         // Write until there's not more data ...
         while (false == queue.isEmpty())
         {
             final ByteBuffer buf = queue.get(0);
             try
             {
                 socketChannel.write(buf);
             }
             catch (final IOException e)
             {
                 log.error(Tool.fromExceptionToString(e));
             }
             if (buf.remaining() > 0)
             {
                 // ... or the socket's buffer fills up
                 return;
             }
             else
             {
                 queue.remove(0);
             }
         }
 
         if(true == queue.isEmpty())
         {
               // We wrote away all data, so we're no longer interested
               // in writing on this socket. Switch back to waiting for
               // data.
               key.interestOps(SelectionKey.OP_READ);
         }
 
     }
 
     private void finishConnection(final SelectionKey key)
     {
         final SocketChannel socketChannel = (SocketChannel) key.channel();
 
         // Finish the Outgoing connection. If the connection operation failed
         // this will raise an IOException.
         try
         {
             socketChannel.finishConnect();
         }
         catch (final IOException e)
         {
             // Cancel the channel's registration with our selector
             log.error(Tool.fromExceptionToString(e));
             key.cancel();
             final NIOPacketQueue pq = connections.getByIndex(socketChannel);
            final Location loc = pq.getremoteLocation();
            log.error("Could not connect to " + loc.getIP() + " Port " + loc.getPort());
             pq.setPassive();
             return;
         }
 
         // Register an interest in writing on this channel
         key.interestOps(SelectionKey.OP_WRITE);
     }
 
     public final void run()
     {
         while(true == isActive())
         {
             try
             {
                 // Process any pending changes
                 synchronized(pendingChanges)
                 {
                     final Iterator<ChangeRequest> changes = pendingChanges.iterator();
                     while(true == changes.hasNext())
                     {
                         final ChangeRequest change = changes.next();
                         switch (change.getType())
                         {
                         case ChangeRequest.CHANGEOPS:
                             final SelectionKey key = (change.getSocket()).keyFor(socketSelector);
                             key.interestOps(change.getOps());
                             break;
                         case ChangeRequest.REGISTER:
                             (change.getSocket()).register(socketSelector, change.getOps());
                             break;
                         default:
                             break;
                         }
                     }
                     this.pendingChanges.clear();
                 }
 
                 // Wait for an event one of the registered channels
                 socketSelector.select();
                 // Iterate over the set of keys for which events are available
                 final Iterator<SelectionKey> selectedKeys = socketSelector.selectedKeys().iterator();
                 while (selectedKeys.hasNext())
                 {
                     final SelectionKey key = selectedKeys.next();
                     selectedKeys.remove();
 
                     if (false == key.isValid())
                     {
                         continue;
                     }
                     // Check what event is available and deal with it
                     if (key.isConnectable())
                     {   // Outgoing connection is in creation
                         finishConnection(key);
                     }
                     else if (key.isAcceptable())
                     {   // Incoming Connection is in creation
                         accept(key);
                     }
                     else if(key.isReadable())
                     {   // Incoming Data
                         read(key);
                     }
                     else if(key.isWritable())
                     {   // ready to send outgoing Data
                         write(key);
                     }
                 }
             }
             catch (final IOException e)
             {
                 log.error(Tool.fromExceptionToString(e));
             }
         } // While()
         // Shutdown
         try
         {
             socketSelector.close();
         }
         catch (final IOException e)
         {
             log.error(Tool.fromExceptionToString(e));
         }
     }
 
     public final PacketQueue getSendQueueForLocation(final Location loc)
     {
         NIOPacketQueue res = connections.getByKey(loc);
         if(null == res)
         {
             // We don't have a connection open to that Location.
             // But we can open a connection to every Location,
             // so lets try that.
             log.info("Channel is not open - Connect to location");
             // Create a new non-blocking socket channel
             SocketChannel channel;
             try
             {
                 channel = SocketChannel.open();
                 channel.configureBlocking(false);
 
                 res = new NIOPacketQueue(channel, loc, this);
                 connections.put(channel, loc, res);
                 // Bind the server socket to the specified address and port
                 final InetSocketAddress isa = new InetSocketAddress(loc.getIP(), loc.getPort());
                 channel.connect(isa);
 
                 // Queue a channel registration since the caller is not the
                 // selecting thread. As part of the registration we'll register
                 // an interest in connection events. These are raised when a channel
                 // is ready to complete connection establishment.
                 synchronized(this.pendingChanges)
                 {
                     pendingChanges.add(new ChangeRequest(channel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
                 }
             }
             catch (final IOException e)
             {
                 log.error(Tool.fromExceptionToString(e));
             }
         }
         return res;
     }
 
     public final void issuePacketsendRequest(final NIOPacketQueue pq)
     {
         // packet Queue is interested in writing
         final SocketChannel channel = pq.getChannel();
         pendingChanges.add(new ChangeRequest(channel, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
         socketSelector.wakeup();
         sendPackets.inc();
     }
 }
