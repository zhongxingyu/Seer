 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.server;
 
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.Enumeration;
 
 import org.joe_e.array.ByteArray;
 import org.ref_send.promise.Receiver;
 import org.waterken.dns.Resource;
 import org.waterken.net.TCPDaemon;
 import org.waterken.thread.Yield;
 
 /**
  * A TCP daemon.
  */
 final class
 TCP implements Runnable {
     
     private final TCPDaemon daemon;
     private final String hostname;
     private final ServerSocket port;
     private final Receiver<ByteArray> updateDNS;
     
     private       InetAddress lastKnownAddress = null;
     
     TCP(final TCPDaemon daemon, final String hostname,
         final ServerSocket port, final Receiver<ByteArray> updateDNS) {
         this.daemon = daemon;
         this.hostname = hostname;
         this.port = port;
         this.updateDNS = updateDNS;
     }
 
     public void
     run() {
         final Thread thread = Thread.currentThread();
         System.out.println(thread + ": " + "running at <" +
                            port.getLocalSocketAddress() + ">...");
         
         final ThreadGroup threads = new ThreadGroup(thread.getName());
         if (null != updateDNS) { updateHostAddress(thread); }
         while (true) {
             try {
                 final Socket socket = port.accept();
                 new Thread(threads, "" + socket.getRemoteSocketAddress()) {
                     public void
                     run() {
                         try {
                             System.out.println(this + ": open...");
                             daemon.accept(hostname, socket, new Yield()).run();
                         } catch (final SocketTimeoutException e) {
                             // normal end to a TCP connection
                         } catch (final Throwable e) {
                            System.err.println(this + ": " + e);
                             e.printStackTrace(System.err);
                         } finally {
                             try { socket.close(); } catch (final Exception e) {}
                         }
                         System.out.println(this + ": closed");
                     }
                 }.start();
             } catch (final SocketTimeoutException e) {
                 if (null != updateDNS) { updateHostAddress(thread); }
             } catch (final Throwable e) {
                System.err.println(thread + ": " + e);
                 e.printStackTrace(System.err);
             }
         }
     }
     
     private void
     updateHostAddress(final Thread thread) {
         try {
             final InetAddress a = dynip();
             if (!a.equals(lastKnownAddress)) {
                 System.out.println(thread + ": updating DNS to: " +
                                    a.getHostAddress() + "...");
                 updateDNS.run(Resource.rr(
                         Resource.A, Resource.IN, 60, a.getAddress()));
                 lastKnownAddress = a;
             }
         } catch (final Throwable e) {
            System.err.println(thread + ": " + e);
             e.printStackTrace(System.err);
         }
     }
     
     static private InetAddress
     dynip() throws SocketException {
         InetAddress r = Loopback.addr;
         for (final Enumeration<NetworkInterface> j =
                                 NetworkInterface.getNetworkInterfaces();
                                                      j.hasMoreElements();) {
             for (final Enumeration<InetAddress> k =
                                 j.nextElement().getInetAddresses();
                                                      k.hasMoreElements();) {
                 final InetAddress a = k.nextElement();
                 if (a instanceof Inet4Address && !a.isLoopbackAddress()) {
                     if (!a.isSiteLocalAddress()) { return a; }
                     r = a;
                 }
             }
         }
         return r;
     }
 }
