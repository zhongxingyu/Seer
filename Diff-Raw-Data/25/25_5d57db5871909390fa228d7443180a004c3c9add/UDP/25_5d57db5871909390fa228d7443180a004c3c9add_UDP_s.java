 // Copyright 2007-2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.server;
 
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketAddress;
 
 import org.joe_e.Struct;
 import org.joe_e.array.ByteArray;
 import org.ref_send.promise.Do;
 import org.waterken.udp.UDPDaemon;
 
 /**
  * A UDP daemon.
  */
 final class
 UDP extends Struct implements Runnable {
 
     private final UDPDaemon daemon;
     private final DatagramSocket port;
     
     UDP(final UDPDaemon daemon, final DatagramSocket port) {
         this.daemon = daemon;
         this.port = port;
     }
     
     public void
     run() {
         final Thread thread = Thread.currentThread();
         System.out.println(thread + ": " + "running at <" +
                            port.getLocalSocketAddress() + ">...");
         while (true) {
             try {
                 final DatagramPacket in = new DatagramPacket(new byte[512],512);
                 port.receive(in);
                 final ByteArray.Builder g = ByteArray.builder(in.getLength());
                 g.append(in.getData(), in.getOffset(), in.getLength());
                 final ByteArray msg = g.snapshot();
                 final SocketAddress from = in.getSocketAddress(); 
                 daemon.accept(from, msg, new Do<ByteArray,Void>() {
                     public Void
                     fulfill(final ByteArray out) throws Exception {
                         port.send(new DatagramPacket(
                             out.toByteArray(), out.length(), from));
                         return null;
                     }
                 });
             } catch (final Throwable e) {
                System.err.println(thread + ": " + e);
                 e.printStackTrace(System.err);
             }
         }
     }
 }
