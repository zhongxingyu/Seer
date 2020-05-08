 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.server;
 
 import java.io.PrintStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketAddress;
 
 import org.joe_e.Struct;
 import org.joe_e.array.ByteArray;
 import org.ref_send.promise.eventual.Do;
 import org.waterken.udp.Daemon;
 
 /**
  * A UDP daemon.
  */
 final class
 UDP extends Struct implements Runnable {
 
     private final PrintStream err;
     private final String protocol;
     private final Daemon daemon;
     private final DatagramSocket port;
     
     UDP(final PrintStream err, final String protocol,
         final Daemon daemon, final DatagramSocket port) {
         this.err = err;
         this.protocol = protocol;
         this.daemon = daemon;
         this.port = port;
     }
     
     public void
     run() {
         err.println("Running " + protocol +
                     " at " + port.getLocalSocketAddress() + " ...");
         while (true) {
             try {
                 final DatagramPacket in = new DatagramPacket(new byte[512],512);
                 port.receive(in);
                 final ByteArray.Generator g =
                     new ByteArray.Generator(in.getLength());
                 g.write(in.getData(), in.getOffset(), in.getLength());
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
                 e.printStackTrace();
             }
         }
     }
 }
