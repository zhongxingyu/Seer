 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 5th December 2009
  *
  * Changelog:
  * - 05/12/2009 - mdiponio - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import org.apache.axis2.transport.http.AxisServlet;
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.nio.SelectChannelConnector;
 import org.mortbay.jetty.servlet.Context;
 import org.mortbay.jetty.servlet.ServletHolder;
 import org.mortbay.thread.QueuedThreadPool;
 
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Embedded Jetty Server set up to handle SOAP requests.
  */
 public class EmbeddedJettyServer implements IServer
 {
     /** Address post-fix for the service URL. */
     public static final String URL_POSTFIX = "/services/RigClientService";
     
     /** Jetty server. */
     private Server server;
     
     /** Connector which receives requests. */
     private Connector connector;
     
     /** Context which handles a request using a servlet. */
     private Context context;
     
     /** Request thread pool. */
     private QueuedThreadPool threadPool;
     
     /** The addresses this server is listening on. */
     private final List<String> addresses;
     
     /** Configuration. */
     private final IConfig config;
     
     /** Logger. */
     private final ILogger logger;
     
     /**
      * Constructor.
      */
     public EmbeddedJettyServer()
     {
         this.logger = LoggerFactory.getLoggerInstance();
         this.logger.debug("Creating a new embedded Jetty server.");
                         
         this.config = ConfigFactory.getInstance();
         this.addresses = new ArrayList<String>();
     }
     
     /**
      * Initialise the server.
      * 
      * @throws ServerException error setting up server
      * @throws IOException 
      */
     private void init() throws ServerException, IOException
     {
         /* --------------------------------------------------------------------
          * ---- 1. Create the server. -----------------------------------------
          * ----------------------------------------------------------------- */
         /* The server is the main class for the Jetty HTTP server. It uses a 
          * connector to receive requests, a context to handle requests and 
          * a thread pool to manage concurrent requests.
          */
         this.server = new Server();
         
         /* --------------------------------------------------------------------
          * ---- 2. Create and configure the connector. ------------------------
          * ----------------------------------------------------------------- */
         /* The connector receives requests and calls handle on handler object
          * to handle a request. */
         try
         {
             final int portNumber = Integer.parseInt(this.config.getProperty("Listening_Port"));
             if (portNumber < 1)
             {
                 this.logger.fatal("Invalid listening server port number loaded from configuration. Check the " +
                 		"configuration property 'Listening_Port' has a valid port number set.");
                 throw new ServerException("Invalid port number configuration.");
             }
             this.logger.info("Rig client request listening port is " + portNumber + ".");
             
             // DODGY The maxIdleTimeout may need to set on the connector in case long
             // requests are truncated.
             this.connector = new SelectChannelConnector();
             this.connector.setPort(portNumber);
             final String addr = this.generateAddress("http", portNumber, EmbeddedJettyServer.URL_POSTFIX);
             this.addresses.add(addr);
             this.logger.priority("Rig client connection address is " + addr + ".");
         }
         catch (NumberFormatException e)
         {
             this.logger.fatal("Failed to load the port number to listen for requests on. Check the configuration" +
             		" property 'Listening_Port' has a valid port number set.");
             throw new ServerException("Unable to load server port number.");
         }
         // TODO Add a HTTPS connector (SslSelectSocketConnector)
         this.server.setConnectors(new Connector[]{this.connector});
         
         /* --------------------------------------------------------------------
          * ---- 3. Create and configure the request thread pool. -------------- 
          * ----------------------------------------------------------------- */
         try
         {
             final int concurrentRequests = Integer.parseInt(this.config.getProperty("Concurrent_Requests"));
             if (concurrentRequests < 1)
             {
                 this.logger.fatal("Invalid number of allowable concurrent requests. This must be greater then 1." +
                 		" Check the configuration property 'Concurrent_Requests' has a valid number of concurrent" +
                 		" requests.");
                 throw new ServerException("Unable to load allowable concurrent request number.");
             }
             this.logger.info("Allowable concurrent requests is " + concurrentRequests + ".");
             this.threadPool = new QueuedThreadPool(concurrentRequests);
         }
         catch (NumberFormatException e)
         {
             this.logger.fatal("Failed to load the number of allowable concurrent requests. Check the configuration" +
             		" property 'Concurrent_Requests' hads a valid number of concurrent requests.");
             throw new ServerException("Unable to load allowable concurrent request number.");
         }
         this.server.setThreadPool(this.threadPool);
         
         /* --------------------------------------------------------------------
          * --- 4. Create and configure the handler. ---------------------------
          * ------------------------------------------------------------------*/
         /* The handler routes the requests to the Apache Axis 2 servlet. */
         this.context = new Context(this.server, "/", Context.SESSIONS);        
         ServletHolder holder = new ServletHolder(new AxisServlet());
         
         File repoPath = new File("./interface");
         this.logger.debug("Axis repository " + repoPath.getCanonicalPath() + ".");
         holder.setInitParameter("axis2.repository.path", repoPath.getCanonicalPath());
         this.context.addServlet(holder, "/");
     }
     
     /* 
      * @see au.edu.uts.eng.remotelabs.rigclient.server.IServer#startListening()
      */
     @Override
     public boolean startListening()
     {
         try
         {
             this.init();
             this.server.start();
             return true;
         }
         catch (ServerException e)
         {
             this.logger.fatal("Unable to initalise server with error message: " + e.getMessage() + ".");
             return false;
         }
         catch (Exception e)
         {
             this.logger.fatal("Unable to start server because of exception: " + e.getClass().getSimpleName() + ", " +
             		"with error message: " + e.getMessage() + ".");
             return false;
         }
     }
 
     /* 
      * @see au.edu.uts.eng.remotelabs.rigclient.server.IServer#shutdownServer()
      */
     @Override
     public boolean stopListening()
     {
         if (this.server == null)
         {
             this.logger.error("Unable to stop server which has not previously been started.");
             return false;
         }
         
         try
         {
             this.server.stop();
             return true;
         }
         catch (Exception e)
         {
             this.logger.fatal("Unable to stop server because of exception: " + e.getClass().getSimpleName() + ", " +
             		"with error message: " + e.getMessage() + ".");
             return false;
         }
     }
     
     /* 
      * @see au.edu.uts.eng.remotelabs.rigclient.server.IServer#isServerListening()
      */
     @Override
     public boolean isListening()
     {
         return this.server.isRunning();
     }
     
     /*
      * @see au.edu.uts.eng.remotelabs.rigclient.server.IServer#getAddress() 
      */
     @Override
     public String[] getAddress()
     {
         return this.addresses.toArray(new String[this.addresses.size()]);
     }
     
     /**
      * Builds the address of the rig client from the passed parameters and 
      * from the determined rig client server IP address.
      * 
      * @param proto protocol string e.g. 'http' or 'https'
      * @param port port number 
      * @param post resource name
      * @return generated address
      * @throws UnknownHostException 
      * @throws UnknownHostException error finding local IP.
      */
     private String generateAddress(final String proto, final int port, final String post) throws SocketException, UnknownHostException
     {
         StringBuilder builder = new StringBuilder();
         
         /* Protocol. */
         builder.append(proto);
         builder.append("://");
         
         /* IP address. */
         final String configIP = this.config.getProperty("Rig_Client_IP_Address");
         if (configIP == null || configIP.isEmpty())
         {
             String configNetworkIf = this.config.getProperty("Listening_Network_Interface");
             configNetworkIf = (configNetworkIf == null || configNetworkIf.isEmpty()) ? null : configNetworkIf;
             
             /* Detect and use the first iterated NIC. */
             Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
             boolean found = false;
             while (nics.hasMoreElements() && !found)
             {
                 NetworkInterface nic = nics.nextElement();
                 
                 /* If a network name is specified, check if the nic has the same name. */
                 if (configNetworkIf != null && !nic.getName().equals(configNetworkIf))
                 {
                     continue;
                 }
                 
                 /* If the network device is the loopback address, continue searching. */
                 if (nic.isLoopback())
                 {
                 	continue;
                 }
 
                 Enumeration<InetAddress> boundAddrs = nic.getInetAddresses();
                 InetAddress addr = null;
                 /* Iterate through the bound address to find a IPv4 address. */
                 while (boundAddrs.hasMoreElements())
                 {
                     if ((addr = boundAddrs.nextElement()) instanceof Inet4Address)
                     {
                         builder.append(addr.getCanonicalHostName());
                         found = true;
                     }
                 }
             }
             
             /* Check to see if an address was found. */ 
             if (!found)
             {
             	/* No external addresses were found so fall back to the listening loopback address, provided it is not
             	 * the configured nic. */
            	builder.append(InetAddress.getLocalHost().getCanonicalHostName());
             }
         }
         else
         {
             builder.append(configIP);
         }
         
         /* Port number. */
         builder.append(':');
         builder.append(port);
         
         /* Resource string. */
         builder.append(post);
         
         return builder.toString();
     }
 }
