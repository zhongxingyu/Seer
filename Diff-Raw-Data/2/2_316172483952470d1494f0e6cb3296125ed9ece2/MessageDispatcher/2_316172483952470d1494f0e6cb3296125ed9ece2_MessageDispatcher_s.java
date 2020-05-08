 /*
  * Copyright (C) 2012  Pauli Kauppinen
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see <http://www.gnu.org/licenses/>.
  */
 package org.javnce.vnc.common;
 
 import java.io.IOException;
 import java.net.StandardSocketOptions;
 import java.nio.channels.*;
 import org.javnce.eventing.ChannelSubscriber;
 import org.javnce.eventing.EventLoop;
 import org.javnce.rfb.messages.Message;
 
 /**
  * The Class MessageDispatcher handles sending and receiving messages to
  * non-blocking channel. The {@link ReceivedMsgEvent} event is published when
  * message is received. The {@link SocketClosedEvent} event is published if
  * socket error occurs for example when host disconnects.
  */
 public class MessageDispatcher implements ChannelSubscriber {
     //private static Logger logger = Logger.getLogger(MessageDispatcher.class.getName());
 
     /**
      * The channel.
      */
     private SocketChannel channel;
     /**
      * The message writer.
      */
     final private SocketWriter writer;
     /**
      * The message reader.
      */
     final private SocketReader reader;
     /**
      * The socket ops.
      */
     private int socketOps;
     final private EventLoop eventLoop;
 
     /**
      * Instantiates a new message dispatcher.
      *
      * @param eventLoop the event loop to which dispatcher attaches
      * @param channel the channel
      * @param factory the incoming message factory
      *
      */
     public MessageDispatcher(EventLoop eventLoop, SocketChannel channel, ReceiveMessageFactory factory) {
         this.channel = channel;
         this.eventLoop = eventLoop;
         this.writer = new SocketWriter();
         this.reader = new SocketReader(factory);
         socketOps = 0;
 
         try {
             channel.configureBlocking(false);
             channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
             updateSocketState();
         } catch (Throwable ex) {
             EventLoop.fatalError(this, ex);
         }
     }
 
     /**
      * Reads the socket.
      */
     private void read() {
         try {
             while (true) {
                 Message msg = reader.read(channel);
 
                 if (null != msg) {
                     eventLoop.publish(new ReceivedMsgEvent(msg));
                 } else {
                     break;
                 }
             }
         } catch (Throwable e) {
             close();
         }
     }
 
     /* (non-Javadoc)
      * @see org.javnce.eventing.ChannelSubscriber#channel(java.nio.channels.SelectionKey)
      */
     @Override
     public void channel(SelectionKey key) {
         try {
             if (key.isReadable()) {
                 read();
             } else if (key.isWritable()) {
                 writer.write(channel);
             } else {
                 close();
             }
             updateSocketState();
         } catch (Throwable e) {
             close();
         }
     }
 
     /**
      * Send the messages to socket.
      *
      * @param msg the msg
      */
     public void send(Message msg) {
         //logger.info("" + msg);
         writer.add(msg);
         try {
             writer.write(channel);
         } catch (IOException e) {
             close();
         }
         updateSocketState();
     }
 
     /**
      * Update socket state.
      */
     private void updateSocketState() {
 
         if (channel.isConnected() && channel.isOpen()) {
            int newOps = SelectionKey.OP_READ | SelectionKey.OP_CONNECT;
 
             if (!writer.isEmpty()) {
                 newOps |= SelectionKey.OP_WRITE;
             }
 
             if (newOps != socketOps) {
                 socketOps = newOps;
                 eventLoop.subscribe(channel, this, socketOps);
             }
         } else {
             close();
         }
     }
 
     /**
      * Closes the socket.
      */
     public void close() {
         try {
             this.channel.close();
         } catch (Throwable e) {
         }
 
         eventLoop.publish(new SocketClosedEvent());
     }
 }
