 // Copyright 2005-2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.server;
 
 import static org.joe_e.file.Filesystem.file;
 import static org.waterken.io.MediaType.MIME;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.lang.reflect.Type;
 import java.net.DatagramSocket;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.ServerSocket;
 import java.security.SecureRandom;
 import java.util.Enumeration;
 
 import org.joe_e.array.ByteArray;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.file.Filesystem;
 import org.ref_send.Variable;
 import org.ref_send.promise.eventual.Eventual;
 import org.ref_send.promise.eventual.Task;
 import org.waterken.dns.Resource;
 import org.waterken.dns.udp.NameServer;
 import org.waterken.http.Server;
 import org.waterken.http.mirror.Mirror;
 import org.waterken.http.trace.Trace;
 import org.waterken.jos.JODB;
 import org.waterken.net.http.HTTPD;
 import org.waterken.remote.http.AMP;
 import org.waterken.remote.http.Browser;
 import org.waterken.remote.mux.Mux;
 import org.waterken.syntax.json.JSONDeserializer;
 import org.waterken.thread.Concurrent;
 
 /**
  * Starts the server.
  */
 final class
 Serve {
 
     private
     Serve() {}
     
     /**
      * @param args  command line arguments
      */
     static public void
     main(String[] args) throws Exception {
         
         // Initialize the static state.
         final File home = new File("").getAbsoluteFile();
         final File www = file(home, "www");
         final File db = file(home, JODB.dbDirName);
         final File keys = new File(home, "keys.jks");
 
         // Extract the arguments.
         int i = 0;
         int backlog = 100;
         int maxAge = 0;
         int soTimeout = 60 * 1000;
         if (args.length == 0) {
             // the default arguments if none are specified
             if (keys.isFile()) {
                 args = new String[] { "http=80", "https=443" };
             } else {
                 args = new String[] { "http=8080" };
             }
         } else {
             // Pop any server arguments.
             for (; i != args.length; ++i) {
                 if (args[i].startsWith("backlog=")) {
                     backlog = Integer.parseInt(args[i].substring(9));
                 } else if (args[i].startsWith("max-age=")) {
                     maxAge = Integer.parseInt(args[i].substring(8));
                 } else if (args[i].startsWith("so-timeout=")) {
                     soTimeout = Integer.parseInt(args[i].substring(11));
                 } else {
                     // Assume this is the start of the service list.
                     break;
                 }
             }
         }
 
         // Summarize the configuration information.
         final PrintStream err = System.err;
         err.println("Home directory: <" + home + ">");
         err.println("Files served with Cache-Control: max-age=" + maxAge);
         err.println("Using server socket backlog: " + backlog);
         err.println("Using connection socket timeout: " + soTimeout + " ms");
 
         // Configure the server.
         final Server server = Trace.make(Mux.make(db, new AMP(),
                                            Mirror.make(maxAge, www, MIME)));
 
         // Start the inbound network services.
         for (; i != args.length; ++i) {
 
             final String service = args[i];
             final int eq = service.indexOf('=');
             final String protocol = service.substring(0, eq);
             final int port = Integer.parseInt(service.substring(eq + 1));
 
             final Runnable listener;
             if ("http".equals(protocol)) {
                 Proxy.protocols.put("http", Loopback.client(80));
                 listener = new TCP(err, new ThreadGroup(service), "http",
                     HTTPD.make("http", server, Proxy.thread), soTimeout,
                     new ServerSocket(port, backlog, Loopback.addr));
             } else if ("https".equals(protocol)) {
                 final Credentials credentials=SSL.keystore("TLS",keys,"nopass");
                 Proxy.protocols.put("https", SSL.client(443, credentials));
                 final ServerSocket listen = credentials.getContext().
                     getServerSocketFactory().createServerSocket(port, backlog);
                 listener = new TCP(err, new ThreadGroup(service), "https",
                     HTTPD.make("https",server,Proxy.thread), soTimeout, listen);
             } else if ("dns".equals(protocol)) {
                 listener = new UDP(err, "dns",
                     NameServer.make(file(db, "dns")),
                     new DatagramSocket(port));
             } else {
                 err.println("Unrecognized protocol: " + protocol);
                 return;
             }
 
             // run the corresponding daemon
             new Thread(listener, service).start();
         }
         
         // update the DNS
         final File ip = new File("ip.json");
         if (ip.isFile()) {
             
             // find the ip address.
             Inet4Address addr = null;
             for (final Enumeration<NetworkInterface> j =
                     NetworkInterface.getNetworkInterfaces();
                  j.hasMoreElements();) {
                 final NetworkInterface x = j.nextElement();
                 for (final Enumeration<InetAddress> k = x.getInetAddresses();
                        k.hasMoreElements();) {
                     final InetAddress a = k.nextElement();
                     if (a instanceof Inet4Address && !a.isLoopbackAddress()) {
                         addr = (Inet4Address)a;
                         break;
                     }
                 }
             }
            final InetAddress outer = null != addr ? addr : Loopback.addr;
             
             // notify the DNS nameserver
             final ClassLoader code = GenKey.class.getClassLoader();
             final Browser browser = Browser.make(
                     new Proxy(), new SecureRandom(), code,
                     Concurrent.loop(Thread.currentThread().getThreadGroup(),
                                     "enqueue"));
             final Eventual _ = browser._;
             _.enqueue.run(new Task() {
                 @SuppressWarnings("unchecked") public void
                 run() throws Exception {
                     final InputStream in = Filesystem.read(ip);
                     final Type type = Variable.class;
                     final Variable update = (Variable)new JSONDeserializer().
                         run("", browser.connect, code,
                             in, PowerlessArray.array(type)).get(0);
                     in.close();
                    System.err.println("Updating DNS to: " +
                                       outer.getHostAddress() + "...");
                     final ByteArray a = ByteArray.array(outer.getAddress());
                     update.put(new Resource(Resource.A, Resource.IN, 0, a));
                 }
             });
         }
     }
 }
