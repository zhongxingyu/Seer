 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
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
  * @date 4th January 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.server.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.handler.ContextHandlerCollection;
 import org.mortbay.jetty.nio.SelectChannelConnector;
 import org.mortbay.jetty.servlet.Context;
 import org.mortbay.jetty.servlet.ServletHolder;
 import org.mortbay.thread.QueuedThreadPool;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 
 import au.edu.uts.eng.remotelabs.schedserver.config.Config;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.server.ServletContainer;
 import au.edu.uts.eng.remotelabs.schedserver.server.ServletContainerService;
 
 /**
  * The server implementation. Listens for service events filter by the 
  * <tt>ServletContainerService<tt> object classes to dynamically add and
  * remove request handling servlets.
  * <br />
  * Added or removing a servlet requires the serverContext to be reset, thus
  * requires the server to briefly stop listening.
  */
 public class ServerImpl
 {
     /** Default listening port for HTTP connections. */
     public static final int DEFAULT_HTTP_PORT = 8080;
     
     /** Default listening port for HTTPS connections. */
     public static final int DEFAULT_HTTPS_PORT = 8081;
     
     /** Bundle serverContext of the Server bundle. */
     private final BundleContext bundleContext;
     
     /** Jetty server. */
     private Server server;
     
     /** Contexts keyed by service id. */
     private final Map<Object, Context> contexts;
 
     /** The handler collection. */
     private ContextHandlerCollection contextCollection;
     
     /** Request thread pool. */
     private QueuedThreadPool threadPool;
 
     /** Logger. */
     private final Logger logger;
     
     public ServerImpl(final BundleContext context)
     {
         this.logger = LoggerActivator.getLogger();
         this.bundleContext = context;
         
         this.contexts = new HashMap<Object, Context>();
     }
 
     /**
      * Initialize the embedded Jetty server. This sets up the server and adds 
      * the connectors and a thread pool. 
      * 
      * @throws Exception error occurs initializing server
      */
     @SuppressWarnings("serial")
     public synchronized void init() throws Exception
 	{
 	    this.logger.debug("Starting the Scheduling Server server up.");
 	    
 	    final List<Connector> connectors = new ArrayList<Connector>();
 
 	    /* Get the configuration service. */
 	    final ServiceReference ref = this.bundleContext.getServiceReference(Config.class.getName());
 	    Config config = null;
 	    if (ref == null || (config = (Config)this.bundleContext.getService(ref)) == null)
 	    {
 	        this.logger.error("Unable to get configuration service reference so unable " +
 	        "to load server configuration.");
 	        throw new Exception("Unable to load server configuration.");
 	    }
 
 	    /* --------------------------------------------------------------------
 	     * ---- 1. Create the server. -----------------------------------------
 	     * ----------------------------------------------------------------- */
 	    /* The server is the main class for the Jetty HTTP server. It uses a 
 	     * connectors to receive requests, a serverContext to handle requests and 
 	     * a thread pool to manage concurrent requests.
 	     */
 	    this.server = new Server();
 
 	    /* --------------------------------------------------------------------
 	     * ---- 2. Create and configure the connectors. ------------------------
 	     * ----------------------------------------------------------------- */
 	    /* The connectors receives requests and calls handle on handler object
 	     * to handle a request. */
 	    final Connector http = new SelectChannelConnector();
 	    String tmp = config.getProperty("Listening_Port", String.valueOf(ServerImpl.DEFAULT_HTTP_PORT));
 	    try
 	    {
 	        http.setPort(Integer.parseInt(tmp));
 	        this.logger.info("Listening on port (HTTP) " + tmp + '.');
 	    }
 	    catch  (NumberFormatException nfe)
 	    {
 	        http.setPort(ServerImpl.DEFAULT_HTTP_PORT);
 	        this.logger.error("Invalid configuration for the Scheduling Server HTTP listening port. " + tmp + " is " +
 	                "not a valid port number. Using the default of " + ServerImpl.DEFAULT_HTTP_PORT + '.');
 	    }
 	    connectors.add(http);
 
 	    /* HTTPS connector. */
 //	    final SslSelectChannelConnector https = new SslSelectChannelConnector();
 //	    tmp = config.getProperty("Listening_Port_HTTPS", String.valueOf(ServerImpl.DEFAULT_HTTPS_PORT));
 //	    try
 //	    {
 //	        https.setPort(Integer.parseInt(tmp));
 //	    }
 //	    catch (NumberFormatException nfe)
 //	    {
 //	        https.setPort(ServerImpl.DEFAULT_HTTPS_PORT);
 //	        this.logger.info("Invalid configuration for the Scheduling Server HTTPS listening port." + tmp + " is " +
 //	                "not a valid port number. Using the default of " + ServerImpl.DEFAULT_HTTPS_PORT + '.');
 //	    }
 //	    /* TODO Set up SSL engine. */
 //	    connectors.add(https);
 
 	    this.server.setConnectors(connectors.toArray(new Connector[connectors.size()]));
 
 	    /* --------------------------------------------------------------------
 	     * ---- 3. Create and configure the request thread pool. -------------- 
 	     * ----------------------------------------------------------------- */
 	    int concurrentReqs = 100;
 	    tmp = config.getProperty("Concurrent_Requests", "100");
 	    try
 	    {
 	        concurrentReqs = Integer.parseInt(tmp);
 	        this.logger.info("Allowable concurrent requests is " + concurrentReqs + ".");
 	    }
 	    catch (NumberFormatException nfe)
 	    {
 	        this.logger.warn(tmp + " is not a valid number of concurrent requests. Using the default of " +
 	                concurrentReqs + '.');
 	    }
 	    this.threadPool = new QueuedThreadPool(concurrentReqs);
 	    this.server.setThreadPool(this.threadPool);
 	    
 	    /* --------------------------------------------------------------------
 	     * ---- 4. Set up the content container and the primoridal  -----------
 	     * ----    context for the root path. ---------------------------------
 	     * ----------------------------------------------------------------- */
 	    /* The context container is used to keep each context in isolation 
 	     * from each other, stopping classes leaking across servlets and 
 	     * causing problems. */
 	    this.contextCollection = new ContextHandlerCollection();
 	    this.server.addHandler(this.contextCollection);
 	    
 	    final Context context = new Context(Context.SESSIONS);
 	    context.setContextPath("/");
 	    this.contextCollection.addHandler(context);
 	    this.contexts.put(new Object(), context);
 
 	    final ServletHolder holder =  new ServletHolder(new HttpServlet()
 	    {
 	        @Override
 	        public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
 	        {
 	            /* Setting response code to 404 Not Found because this response
 	             * occurs where an address is unbound to a servlet. */
 	            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
 	            
 	            if (req.getRequestURI().endsWith("team.jpg"))
 	            {
 	                res.setContentType("image/jpeg");
 	                URL img = this.getClass().getResource("/META-INF/images/team.jpg");
 	                if (img != null)
 	                {
 	                    ServletOutputStream output = res.getOutputStream();
 	                    InputStream imgInput = img.openStream();
 	                    int b = -1;
 	                    byte buf[] = new byte[1024];
 	                    while (imgInput.available() > 0 && (b = imgInput.read(buf)) != -1) output.write(buf, 0, b);
 	                }
 	            }
 	            else
 	            {
 	                PrintWriter writer = res.getWriter();
 	                writer.println("<html>");
 	                writer.println("   <head>");
 	                writer.println("       <title>Sahara R2: A New Hope</title>");
 	                writer.println("   <head>");
 	                writer.println("   <body>");
 	                writer.println("       <div align=\"center\">");
 	                writer.println("           <h1>Sahara R2: A New Hope</h1>");
 	                writer.println("           <img src=\"team.jpg\" />");
 	                writer.println("           <p style=\"font-size:0.8em\"><strong>The Sahara team:</strong> " +
 	                		"Michael Diponio (Developer), Tania Machet (Software Engineer), " +
 	                		"Michel de la Villefromoy (Project Manager), Tejaswini Deshpande (Tester).<br />");
 	                writer.println("           &copy;University of Technology, Sydney 2009 - 2010</p>");
 	                writer.println("       </div>");
 	                writer.println("   </body>");
 	                writer.println("</html>");
  	            }
 	        }
 	    });
 	    context.addServlet(holder, "/");
 	}
     
     /**
      * Adds a servlet to hosted on server based on the provided service reference.
      * If the server is started, it is briefly stopped to add the servlet and is 
      * then restarted.
      * 
      * @param ref service reference pointing to a ServletContainerService service
      */
     public synchronized void addService(final ServiceReference ref)
     {
         boolean wasRunning = false;
         try
         {
             final ServletContainerService serv = (ServletContainerService)this.bundleContext.getService(ref);
             ServletContainer containers[] = serv.getServlets();
             if (containers.length == 0)
             {
                 this.logger.error("Server registration from bundle " + ref.getBundle().getSymbolicName() + 
                 " does not contain a servlet so it cannot be hosted. This is a bug.");
                 throw new IllegalArgumentException("Servlet is empty.");
             }
 
             /* If running, stop the server. */
             wasRunning = this.server.isStarted() || this.server.isStarting();
             if (wasRunning) this.server.stop();
             
             /* Create the context. */
             final String contextPath = serv.getOverriddingPathSpec() == null ? '/' + ref.getBundle().getSymbolicName() :
                 serv.getOverriddingPathSpec();
             this.logger.info("The servlets for bundle " + ref.getBundle().getSymbolicName() + " will be hosted on " +
             		"path " + contextPath + '.');
             final Context context = new Context(this.server, contextPath, Context.SESSIONS);
             this.contexts.put(ref.getProperty(Constants.SERVICE_ID), context);
             
             /* Populate a context with all the servlets to run. */
             for (ServletContainer cont : containers)
             {
                 final ServletHolder holder = new ServletHolder(cont.getServlet());
                 if (cont.isAxis())
                 {
                     URL repoUrl = cont.getServlet().getClass().getResource("/META-INF/repo");
                     if (repoUrl != null)
                     {
                         this.logger.debug("Axis repository for bundle " + ref.getBundle().getSymbolicName() + 
                                 " has URI " + repoUrl.toURI().toString() + '.');
                         holder.setInitParameter("axis2.repository.url", repoUrl.toURI().toString());
                     }
                     else
                     {
                         this.logger.error("Unable to find the repository resource from the " + 
                                 ref.getBundle().getSymbolicName() + " bundle. There must be a 'repo' folder in the " +
                                 "bundle META-INF folder containing the services list (services.list) and a service " +
                                 "archive file with the service WSDL and service descriptor (services.xml).");
                         continue;
                      }
                 }
                 this.logger.debug("Deploying servlet from the " + ref.getBundle().getSymbolicName() + 
                         " bundle with service ID: " + ref.getProperty(Constants.SERVICE_ID));
                 context.addServlet(holder, cont.getPath());
             }
 
             this.contextCollection.addHandler(context);
         }
         catch (Exception ex)
         {
             ex.printStackTrace();
             this.logger.error("Failed adding server service from bundle " + ref.getBundle().getSymbolicName() +
                     " because of exception with message: " + ex.getMessage() + '.');
         }
         finally
         {
             /* Restore the server state. */
             if (wasRunning && this.server.isStopped())
             {
                 try
                 {
                     this.logger.debug("Restarting Scheduling server servlet server.");
                     this.server.start();
                 }
                 catch (Exception e)
                 { 
                     this.logger.error("Failed starting Jetty server because of exception with message: " 
                             + e.getMessage() + '.');
                 }
             }
         }
     }
     
     /**
      * Removes a servlet from being hosted on the server based on the provided 
      * service reference which provides a ServletContainerService object. 
      * If the server is started, it is briefly stopped to remove the servlet and is 
      * then restarted.
      * 
      * @param ref service reference pointing to a ServletContainerService service
      */
     public synchronized void removeService(final ServiceReference ref)
     {
         boolean wasRunning = false;
         try
         {
             if (!this.contexts.containsKey(ref.getProperty(Constants.SERVICE_ID)))
             {
                 this.logger.warn("The server servlet for bundle " + ref.getBundle().getSymbolicName() + " is not " + 
                         " currently registered, so nothing to remove.");
                 return;
             }
             
             /* If running, stop the server. */
             wasRunning = this.server.isStarted() || this.server.isStarting();
             if (wasRunning) this.server.stop();
             
             Context con = this.contexts.remove(ref.getProperty(Constants.SERVICE_ID));
             
             this.contextCollection.stop();
             this.contextCollection.destroy();
             this.contextCollection.removeHandler(con);
             this.contextCollection.setHandlers(this.contexts.values().toArray(new Handler[this.contexts.size()]));
         }
         catch (Exception ex)
         {
             this.logger.error("Failed removing server service from bundle " + ref.getBundle().getSymbolicName() +
                 " because of exception with message: " + ex.getMessage() + '.');
         }
         finally
         {
             /* Restore the server state. */
             if (wasRunning && this.server.isStopped())
             {
                 try
                 {
                     this.logger.debug("Restarting Scheduling server servlet server.");
                     this.server.start();
                 }
                 catch (Exception ex)
                 {
                     this.logger.error("Failed starting Jetty server because of exception with message: " 
                             + ex.getMessage() + '.');
                 }
             }
         }
     }
     
     /**
      * Starts the listening server.
      * 
      * @throws Exception
      */
     public synchronized void start() throws Exception
     {
         try
         {
             this.logger.debug("Starting the Scheduling Server server now.");
             this.server.start();
         }
         catch (Exception e)
         {
             this.logger.error("Exception thrown when starting the listening server with message: " +
                     e.getMessage() + '.');
             throw e;
         }
     }
     
 
     /**
      * Stops the server listening.
      * 
      * @throws Exception
      */
 	public synchronized void stop() throws Exception 
 	{
 	    if (this.server != null)
 	    {
 	        try
 	        {
	            this.logger.debug("Stopping the Scheduling Server server now.");
 	            this.server.stop();
 	        }
 	        catch (Exception e)
 	        {
 	            this.logger.error("Exception thrown when stopping the listening server with message: " + 
 	                    e.getMessage() + '.');
 	            throw e;
 	        }
 	    }
 	}
 }
