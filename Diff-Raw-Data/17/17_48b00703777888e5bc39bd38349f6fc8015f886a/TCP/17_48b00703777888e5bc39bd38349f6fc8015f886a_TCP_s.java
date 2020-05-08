 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.server;
 
 import java.io.PrintStream;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.Enumeration;
 
 import org.joe_e.array.ByteArray;
 import org.ref_send.var.Setter;
 import org.ref_send.var.Variable;
 import org.waterken.dns.Resource;
 import org.waterken.net.TCPDaemon;
 
 /**
  * A TCP daemon.
  */
 final class
 TCP implements Runnable {
     
     private final String service;
     private final PrintStream err;
     private final TCPDaemon daemon;
     private final String hostname;
     private final ServerSocket port;
     
     private       long count = 0;
     
     TCP(final String service, final PrintStream err,  
         final TCPDaemon daemon, final String hostname, final ServerSocket port){
         this.service = service;
         this.err = err;
         this.daemon = daemon;
         this.hostname = hostname;
         this.port = port;
     }
 
     public void
     run() {
         err.println(service + ": " + "running at " +
                     port.getLocalSocketAddress() + " ...");
         
         final ThreadGroup threads = new ThreadGroup(service);
         final Setter<Resource> updater_;
         if (daemon.SSL) {
             final Variable<Resource> ip = Config.read(Variable.class, "ip");
             if (null != ip) {
                 updater_ = Config.browser._._(ip.setter);
                 try {
                     port.setSoTimeout(60 * 1000);
                 } catch (final Exception e) {
                     err.println(service + ": " + e);
                 }
             } else {
                 updater_ = null;
             }
         } else {
             updater_ = null;
         }
         InetAddress address = null;
         boolean recheck = null != updater_;
         while (true) {
             final Socket socket;
             try {
                 if (recheck) {
                     final InetAddress a = dynip();
                     if (!a.equals(address)) {
                         err.println(
                             "Updating DNS to: " + a.getHostAddress() + "...");
                         updater_.run(new Resource(
                             Resource.A, Resource.IN, 60,
                             ByteArray.array(a.getAddress())));
                         address = a;
                     }
                     recheck = false;
                 }
 
                 socket = port.accept();
             } catch (final SocketTimeoutException e) {
                 recheck = true;
                 continue;
             } catch (final Exception e) {
                 // something strange happened
                 e.printStackTrace();
                 continue;
             }
             final String name = service + "-" + count++;
             new Thread(threads, new Runnable() {
                 public void
                 run() {
                     try {
                         err.println(name + ": processing...");
                         daemon.accept(Config.exe, hostname, socket).run();
                     } catch (final Throwable e) {
                        err.println(name + ": " + e);
                     } finally {
                         try { socket.close(); } catch (final Exception e) {}
                     }
                 }
             }, name).start();
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
