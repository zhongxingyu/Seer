 package org.pentaho.pac.server;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.HttpConnection;
 import org.mortbay.jetty.Request;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.bio.SocketConnector;
 import org.mortbay.jetty.handler.AbstractHandler;
 import org.mortbay.jetty.handler.ContextHandlerCollection;
 import org.mortbay.jetty.handler.ResourceHandler;
 import org.mortbay.jetty.servlet.Context;
 import org.mortbay.jetty.servlet.ServletHolder;
 
 public class JettyServer {
   private Server server;
   private int portNumber = 8099;
  private String hostname = "localhost"; //$NON-NLS-1$
   
   private static final Log logger = LogFactory.getLog(JettyServer.class);
   
   public JettyServer(){
     server = new Server();
     setupServer();
     startServer();
   }
   
   private void startServer(){
     SocketConnector connector = new SocketConnector();
     connector.setPort(portNumber);
     connector.setHost(hostname);
     connector.setName("Pentaho Console HTTP listener for ["+hostname+":" + portNumber + "]"); //$NON-NLS-2$ //$NON-NLS-3$
     logger.info("starting " + connector.getName()); //$NON-NLS-1$
     server.setConnectors( new Connector[] { connector });
     
     logger.info( "Console Starting" );
     
     try {
       server.start();
    } catch (RuntimeException e) {
      // let runtime exceptions leak, developer should handle them before release.
      logger.error("Error starting server", e); //$NON-NLS-1$
      throw e;
     } catch (Exception e) {
      logger.error("Error starting server", e); //$NON-NLS-1$
     }
   }
   
   
   private void setupServer(){
     server = new Server();
 
     ContextHandlerCollection contexts = new ContextHandlerCollection();
  
     // Start execution
     Context startExecution = new Context(contexts, "/", Context.SESSIONS);  //$NON-NLS-1$
     startExecution.setResourceBase("www/org.pentaho.pac.PentahoAdminConsole"); //$NON-NLS-1$
    startExecution.setWelcomeFiles(new String[]{"PentahoAdminConsole.html"}); //$NON-NLS-1$
     
     ServletHolder pacsvc = new ServletHolder(new org.pentaho.pac.server.PacServiceImpl() );
     startExecution.addServlet(pacsvc, "/pacsvc"); //$NON-NLS-1$
     
     //sample
     Handler hello = new HomeHandler();
     
     //resource handler
     ResourceHandler resources = new ResourceHandler();
    resources.setResourceBase("www/org.pentaho.pac.PentahoAdminConsole"); //$NON-NLS-1$
    resources.setWelcomeFiles(new String[]{"PentahoAdminConsole.html"}); //$NON-NLS-1$
     
     server.setHandlers(new Handler[] { resources, startExecution }); //, contexts });
     
   }
 
 
   public int getPortNumber() {
   
     return portNumber;
   }
 
 
   public void setPortNumber(int portNumber) {
   
     this.portNumber = portNumber;
   }
 
 
   public String getHostname() {
   
     return hostname;
   }
 
 
   public void setHostname(String hostname) {
   
     this.hostname = hostname;
   }
   
   public static void main(String[] args){
     JettyServer server = new JettyServer();
   }
   
   
   public static class HomeHandler extends AbstractHandler
   {
       public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
       {
           Request base_request = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
           base_request.setHandled(true);
           
           response.setStatus(HttpServletResponse.SC_OK);
          response.setContentType("text/html"); //$NON-NLS-1$
           response.getWriter().println("<h1>Hello OneContext</h1>");
           
       }
   }
 }
 
   
