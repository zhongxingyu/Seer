 package org.kjkoster.zapcat.zabbix;
 
 /* This file is part of Zapcat.
  *
  * Zapcat is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  * Zapcat is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Zapcat.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.nio.channels.spi.SelectorProvider;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.management.ObjectName;
 
 import org.apache.log4j.Logger;
 import org.kjkoster.zapcat.Agent;
 
 /**
  * A passive Zabbix agent. This agent starts and manages a daemon thread.
  * <p>
  * The agent uses an executor service to handle the JMX queries that come in.
  * This allows us to handle a few queries concurrently.
  * 
  * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
  */
 public final class ZabbixAgent implements Agent, Runnable {
     private static final Logger log = Logger.getLogger(ZabbixAgent.class);
 
     // the address to bind to (or 'null' to bind to any available interface).
     private final InetAddress address;
 
     // the port to bind to.
     private final int port;
 
     private final Thread daemon;
 
     private Selector listener = null;
 
     private ServerSocketChannel serverSocketChannel = null;
 
     private final ExecutorService handlers;
 
     private final ObjectName mbeanName;
 
     /**
      * Configure a new Zabbix agent. Each agent needs the local port number to
      * run. This constructor configures the port number by checking for a system
      * property named "org.kjkoster.zapcat.zabbix.port". If it is not there, the
      * port number defaults to 10052.
      * <p>
      * An optional address on which the agent will listen can be specified by
      * setting a property named "org.kjkoster.zapcat.zabbix.address". If this
      * property is not set, this Zabbix agent will listen on any available
      * address.
      * 
      * @throws UnknownHostException
      *             Thrown by invoking {@link InetAddress#getByName(String)} on
      *             the value of "org.kjkoster.zapcat.zabbix.address", if that
      *             value was set.
      */
     public ZabbixAgent() throws UnknownHostException {
         this(findAddress(), Integer.parseInt(System.getProperty(
                "org.kjkoster.zapcat.zabbix.port", "10052")));
     }
 
     private static InetAddress findAddress() throws UnknownHostException {
         final String hostname = System
                 .getProperty("org.kjkoster.zapcat.zabbix.address");
         return hostname == null ? null : InetAddress.getByName(hostname);
     }
 
     /**
      * Configure a new Zabbix agent to listen on a specific address and port
      * number.
      * <p>
      * Use of this method is discouraged, since it places address and port
      * number configuration in the hands of developers. That is a task that
      * should normally be left to system administrators and done through system
      * properties.
      * 
      * @param address
      *            The address to listen on, or 'null' to listen on any available
      *            address.
      * @param port
      *            The port number to listen on.
      */
     public ZabbixAgent(final InetAddress address, final int port) {
         this.address = address;
         this.port = port;
         handlers = new ThreadPoolExecutor(1, 5, 60L, TimeUnit.SECONDS,
                 new LinkedBlockingQueue<Runnable>());
 
         daemon = new Thread(this, "Zabbix-agent");
         daemon.setDaemon(true);
         daemon.start();
 
         mbeanName = JMXHelper.register(new Agent(),
                 "org.kjkoster.zapcat:type=Agent,port=" + port);
     }
 
     /**
      * Stop and clean up the used resources.
      * 
      * @see org.kjkoster.zapcat.Agent#stop()
      */
     public void stop() {
         try {
             if (listener != null) {
                 listener.close();
             }
             if (serverSocketChannel != null) {
                 serverSocketChannel.close();
             }
         } catch (IOException e) {
             // ignore, we're going down anyway...
         }
         try {
             daemon.join();
         } catch (InterruptedException e) {
             // ignore, we're going down anyway...
         }
 
         try {
             handlers.shutdown();
             handlers.awaitTermination(10, TimeUnit.SECONDS);
         } catch (InterruptedException e) {
             // ignore, we're going down anyway...
         }
     }
 
     /**
      * @see java.lang.Runnable#run()
      */
     public void run() {
         try {
            listener = initNIOListener(address, port);
 
             for (;;) {
                 listener.select();
 
                 if (!listener.isOpen()) {
                     log.debug("stopped listening for connections.");
                     break;
                 }
 
                 for (final SelectionKey key : listener.keys()) {
                     if (!key.isValid()) {
                         continue;
                     }
 
                     if (key.isAcceptable()) {
                        final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
                                 .channel();
                        final SocketChannel socketChannel = serverSocketChannel
                                .accept();
 
                         // This will ensure backwards compatibility. Should we
                         // change this to non-blocking sockets as well?
                         socketChannel.configureBlocking(true);
                         final Socket accepted = socketChannel.socket();
                         log.debug("accepted connection from "
                                 + accepted.getInetAddress().getHostAddress());
 
                         handlers.execute(new QueryHandler(accepted));
                     }
                 }
             }
         } catch (IOException e) {
             log.error("caught exception, exiting", e);
         } finally {
             if (listener != null) {
                 try {
                     listener.close();
                 } catch (IOException e) {
                     // ignore, we're going down anyway...
                 }
             }
             if (serverSocketChannel != null) {
                 try {
                     serverSocketChannel.close();
                 } catch (IOException e) {
                     // ignore, we're going down anyway...
                 }
             }
 
             JMXHelper.unregister(mbeanName);
 
             log.debug("zabbix agent exits");
         }
     }
 
     /**
      * Initializes a non-blocking server socket, by binding it to the specified
      * address (use 'null' to bind to any available interface) and port.
      * 
     * @param address
     *            The address to bind the server socket to. Bind to any
     *            available address by assigning 'null'.
     * @param port
     *            The port to bind the server socket to.
      * @return The selector object that has been registered to the channel.
      */
    private Selector initNIOListener(final InetAddress address, final int port)
            throws IOException {
         serverSocketChannel = ServerSocketChannel.open();
         // We'd like to perform non-blocking operations.
         serverSocketChannel.configureBlocking(false);
 
         final ServerSocket serverSocket = serverSocketChannel.socket();
         serverSocket.bind(new InetSocketAddress(address, port));
         log.info("listening on " + serverSocket.toString());
 
         // create, register and return the Selector instance that we'll use.
         final Selector selector = SelectorProvider.provider().openSelector();
         serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
         return selector;
     }
 
     /**
      * The interface to our JMX representation.
      * 
      * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
      */
     public interface AgentMBean {
         /**
          * Read the port number that the current agent is listening to.
          * 
          * @return The configured port number.
          */
         int getPort();
 
         /**
          * Read the bind address of the current agent.
          * 
          * @return The configured bind address, or '*' to indicate any address.
          */
         String getBindAddress();
     }
 
     /**
      * Our JMX representation.
      * 
      * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
      */
     private class Agent implements AgentMBean {
         /**
          * @see org.kjkoster.zapcat.zabbix.AgentMBean#getPort()
          */
         public int getPort() {
             return port;
         }
 
         /**
          * @see org.kjkoster.zapcat.zabbix.ZabbixAgent.AgentMBean#getBindAddress()
          */
         public String getBindAddress() {
             return address == null ? "*" : address.getHostAddress();
         }
     }
 }
