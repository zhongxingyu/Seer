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
 import java.io.Writer;
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
 
     private Writer getWriter() { return connection.getWriter(); }
     
     /**
      * Creates a new packet writer with the specified connection.
      *
      * @param connection the connection.
      */
     protected PacketWriter(XMPPConnection connection) {
         this.queue = new ArrayBlockingQueue<Packet>(500, true);
         this.connection = connection;
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
 
             try {
                 queue.put(packet);
             }
             catch (InterruptedException ie) {
                 ie.printStackTrace();
                 return;
             }
             synchronized (queue) {
                 queue.notifyAll();
             }
 
             // Process packet writer listeners. Note that we're using the sending
             // thread so it's expected that listeners are fast.
             connection.firePacketSendingListeners(packet);
         }
     }
 
     /**
      * Starts the packet writer thread and opens a connection to the server. The
      * packet writer will continue writing packets until {@link #shutdown} or an
      * error occurs.
      */
     public void startup() {
         if(writerThread != null)
             throw new RuntimeException("WriterThread.startup called while already running");
 
         done = false;
 
         writerThread = new Thread() {
             public void run() {
                 writePackets(this);
             }
         };
         writerThread.setName("Smack Packet Writer (" + connection.connectionCounterValue + ")");
         writerThread.setDaemon(true);
         writerThread.start();
     }
 
     /**
      * Shuts down the packet writer. Once this method has been called, no further
      * packets will be written to the server.
      *
      * The caller must first shut down the data stream to ensure the thread will exit.
      */
     public void shutdown() {
         done = true;
         synchronized (queue) {
             queue.notifyAll();
         }
 
         connection.interceptors.clear();
         connection.sendListeners.clear();
 
         ThreadUtil.uninterruptibleJoin(writerThread);
         writerThread = null;
     }
 
     /**
      * Returns the next available packet from the queue for writing.
      *
      * @return the next packet for writing.
      */
     private Packet nextPacket() {
         Packet packet = null;
         // Wait until there's a packet or we're done.
         while (!done && (packet = queue.poll()) == null) {
             try {
                 synchronized (queue) {
                     queue.wait();
                 }
             }
             catch (InterruptedException ie) {
                 // Do nothing
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
                     Writer writer = getWriter();
                     if(writer == null)
                        throw new IOException("Wrote a packet while the connection was closed");
                     synchronized (writer) {
                         writer.write(packet.toXML());
                         writer.flush();
                     }
                 }
             }
             // Flush out the rest of the queue. If the queue is extremely large, it's possible
             // we won't have time to entirely flush it before the socket is forced closed
             // by the shutdown process.
             try {
                 Writer writer = getWriter();
 
                 // This happens normally when the connection is shut down abruptly
                 // due to an error.
                 if(writer == null)
                     throw new IOException("Wrote a packet while the connection was closed");
 
                 synchronized (writer) {
                     while (!queue.isEmpty()) {
                         Packet packet = queue.remove();
                         writer.write(packet.toXML());
                     }
                     writer.flush();
                 }
             }
             catch (IOException e) {
                 e.printStackTrace();
             }
 
             // Delete the queue contents (hopefully nothing is left).
             queue.clear();
 
             try {
                 Writer writer = getWriter();
                 if(writer != null)
                     writer.close();
             }
             catch (IOException e) {
                 // Do nothing
             }
         }
         catch (IOException ioe){
             // Don't report write errors.  Instead, require that any write errors at the
             // socket layer cause reads to throw an error as well, so all error handling
             // is consolidated in PacketReader.
             new Exception(ioe).printStackTrace();
             done = true;
         }
     }
 }
