 package org.lastbamboo.common.ice;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import javax.net.ServerSocketFactory;
 
 import org.apache.commons.io.IOExceptionWithCause;
 import org.apache.commons.lang.math.RandomUtils;
 import org.lastbamboo.common.portmapping.NatPmpService;
 import org.lastbamboo.common.portmapping.PortMappingProtocol;
 import org.lastbamboo.common.portmapping.UpnpService;
 import org.lastbamboo.common.util.NetworkUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class is a single server socket for all ICE offerers. It works by
  * keeping track of which IP addresses it's expecting incoming sockets from
  * and then associating those with existing ICE negotiations. Users of this
  * class should first check if there are already associated mappings for
  * expected incoming addresses and use alternative means if there are, such
  * as creating a dynamically generated server socket.
  * 
  * This class is useful to avoid continually opening and closing server sockets
  * and to avoid constant UPnP and NAP-PMP churn.
  */
 public class MappedTcpOffererServerPool {
 
     private final Logger log = LoggerFactory.getLogger(getClass());
     
     private final Queue<PortMappedServerSocket> servers = 
         new ConcurrentLinkedQueue<PortMappedServerSocket>();
 
     private final NatPmpService natPmpService;
 
     private final UpnpService upnpService;
 
     private final ServerSocketFactory serverSocketFactory;
 
     /**
      * Creates a new mapped server for the answerer.
      * 
      * @param natPmpService The NAT PMP mapper.
      * @param upnpService The UPnP mapper.
      * @param serverAddress The address of the server.
      * @param serverSocketFactory The factory for creating server sockets --
      * could be SSL sockets, for example.
      * @throws IOException If there's an error starting the server.
      */
     public MappedTcpOffererServerPool(final NatPmpService natPmpService,
         final UpnpService upnpService, 
         final ServerSocketFactory serverSocketFactory) {
         this.natPmpService = natPmpService;
         this.upnpService = upnpService;
         this.serverSocketFactory = serverSocketFactory;
         
         // We add a couple of cached server sockets right at the beginning 
         // for later use.
         final Runnable runner = new Runnable() {
 
             public void run() {
                 try {
                     addServerSocket(serverSocket());
                 } catch (final IOException e) {
                     log.error("Could not create socket!");
                 }
                 try {
                     addServerSocket(serverSocket());
                 } catch (final IOException e) {
                     log.error("Could not create socket!");
                 }
             }
         };
         final Thread t = 
             new Thread(runner, "Mapped-Offerer-Server-Socket-Creation-Thread");
         t.setDaemon(true);
         t.start();
     }
 
     /**
      * Accessor for port-mapped server socket. If there are not cached sockets
      * around, this will create a new one and return it on demand.
      */
     public PortMappedServerSocket serverSocket() throws IOException {
         synchronized (servers) {
             if (servers.isEmpty()) {
                 return randomPortServer();
             }
             else {
                 return servers.remove();
             }
         }
     }
 
     public void addServerSocket(final PortMappedServerSocket ss) {
         synchronized (servers) {
             // This guards against bugs in external code that might add
             // the same server socket twice.
             if (servers.contains(ss)) {
                 log.warn("We already have this server socket -- " +
                     "bug in calling code?", ss);
             }
             else {
                 servers.add(ss);
             }
         }
     }
 
     private PortMappedServerSocket randomPortServer() throws IOException {
         IOException ioe = null;
         final InetAddress lh = NetworkUtils.getLocalHost();
         for (int i = 0; i < 20; i++) {
             try {
                final ServerSocket ss = 
                    this.serverSocketFactory.createServerSocket();
                 final InetSocketAddress endpoint = 
                     new InetSocketAddress(lh, randomPort());
                 ss.bind(endpoint);
                 
                 // With this set, calls to accept will timeout after the 
                 // specified interval.
                 ss.setSoTimeout(30*1000);
                 final PortMappedServerSocket pmss = 
                     new PortMappedServerSocket(ss);
                 final int port = endpoint.getPort();
                 upnpService.addUpnpMapping(PortMappingProtocol.TCP, port, 
                     port, pmss);
                 natPmpService.addNatPmpMapping(PortMappingProtocol.TCP, port,
                     port, pmss);
                 return pmss;
             } catch (final IOException e) {
                 log.info("Error binding?", e);
                 ioe = e;
             }
         }
         if (ioe == null) {
             throw new IOException(
                 "Could not create server socket after many tries");
         }
         throw new IOExceptionWithCause(
             "Could not create server socket after many tries", ioe);
     }
 
     private int randomPort() {
         return 1024 + (RandomUtils.nextInt() % 60000);
     }
 }
