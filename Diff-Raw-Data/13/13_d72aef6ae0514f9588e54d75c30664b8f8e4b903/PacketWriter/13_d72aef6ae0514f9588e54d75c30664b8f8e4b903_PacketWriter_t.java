 /**
  * $RCSfile$
  * $Revision$
  * $Date$
  *
  * Copyright 2003-2007 Jive Software.
  *
  * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jivesoftware.smack;
 
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.util.ThreadUtil;
 
 import java.io.IOException;
 import java.util.Vector;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 
 /**
  * Writes packets to a XMPP server. Packets are sent using a dedicated thread. Packet
  * interceptors can be registered to dynamically modify packets before they're actually
  * sent. Packet listeners can be registered to listen for all outgoing packets.
  *
  * @see Connection#addPacketInterceptor
  * @see Connection#addPacketSendingListener
  *
  * @author Matt Tucker
  */
 class PacketWriter {
 
     private Thread writerThread;
     private XMPPConnection connection;
     private final BlockingQueue<Packet> queue;
     private boolean done;
 
     /**
      * Creates a new packet writer with the specified connection.
      *
      * @param connection the connection.
      */
     protected PacketWriter(XMPPConnection connection) {
         this.queue = new ArrayBlockingQueue<Packet>(500, true);
         this.connection = connection;
         
         done = false;
 
         writerThread = new Thread() {
             public void run() { writePackets(this); }
         };
         writerThread.setName("Smack Packet Writer (" + connection.connectionCounterValue + ")");
         writerThread.setDaemon(true);
         writerThread.start();
     }
 
     /**
      * Sends the specified packet to the server.
      *
      * @param packet the packet to send.
      */
     public void sendPacket(Packet packet) {
         if (!done) {
             // Invoke interceptors for the new packet that is about to be sent. Interceptors
             // may modify the content of the packet.
             connection.firePacketInterceptors(packet);
 
             synchronized(this) {
                 if(!queue.offer(packet)) {
                     throw new RuntimeException("Queue overflow");
                 }
                 this.notifyAll();
             }
 
             // Process packet writer listeners. Note that we're using the sending
             // thread so it's expected that listeners are fast.
             connection.firePacketSendingListeners(packet);
         }
     }
 
     /**
      * Shuts down the packet writer. Once this method has been called, no further
      * packets will be written to the server.
      *
      * The caller must first shut down the data stream to ensure the thread will exit.
      */
     public void shutdown() {
         synchronized(this) {
             done = true;
             this.notifyAll();
         }
 
         if(writerThread != null) {
             ThreadUtil.uninterruptibleJoin(writerThread);
             writerThread = null;
         }
     }
 
     /**
      * Returns the next available packet from the queue for writing.
      *
      * @return the next packet for writing.
      */
     private synchronized Packet nextPacket() {
         // Wait until there's a packet or we're done.
         Packet packet = null;
         while (!done && (packet = queue.poll()) == null) {
             try {
                 this.wait();
             }
             catch (InterruptedException ie) {
                 Thread.currentThread().interrupt();
                 done = true;
                 this.notifyAll();
             }
         }
         return packet;
     }
 
     private void writePackets(Thread thisThread) {
         try {
             // Write out packets from the queue.
             while (!done && (writerThread == thisThread)) {
                 Packet packet = nextPacket();
                 if (packet != null) {
                     Vector<Packet> packets = new Vector<Packet>();
                     packets.add(packet);
                     connection.writePacket(packets);
                 }
             }
             // Flush out the rest of the queue. If the queue is extremely large, it's possible
             // we won't have time to entirely flush it before the socket is forced closed
             // by the shutdown process.
             try {
                 Vector<Packet> packets = new Vector<Packet>();
                 packets.addAll(queue);

                // Until we receive a packet, it's not valid to call writePacket.  Don't call
                // it at all if we have no packets to send.
                if(packets.size() > 0)
                    connection.writePacket(packets);
             }
             catch (XMPPException e) {
                 // e.printStackTrace();
             }
 
             // Delete the queue contents (hopefully nothing is left).
             queue.clear();
         }
         catch (XMPPException ioe){
             // Don't report write errors.  Instead, require that any write errors at the
             // socket layer cause reads to throw an error as well, so all error handling
             // is consolidated in PacketReader.
             new Exception(ioe).printStackTrace();
             done = true;
         }
     }
 }
