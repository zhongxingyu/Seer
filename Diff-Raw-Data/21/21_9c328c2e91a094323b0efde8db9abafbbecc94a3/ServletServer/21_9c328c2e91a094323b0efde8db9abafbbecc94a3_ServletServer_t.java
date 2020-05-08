 package ksw.servlet;
 
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.bio.SocketConnector;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 
 import javax.servlet.Servlet;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 /**
  * Create a simple webserver (built on top of jetty) that uses Servlets to process requests.
  */
 public class ServletServer
 {
     private Server _server = null;
     private ServletContextHandler _internalContext = null;
     private String _contextPath;
     private String _rootPath;
     private boolean _inited;
 
     public static final int DefaultPortNumber = 9001;
 
     private static ServletServer s_server;
 
     public static void main (String[] args)
     {
         System.out.println("Starting ServletServer");
 
         ServletServer server = ServletServer.getServer();
        String portS = System.getenv("PORT");
        int portNumber = DefaultPortNumber;
        if (portS != null && portS.length() > 0) {
            portNumber = Integer.valueOf(portS);
        }

         server.init(portNumber, "");
         server.addTestServlets();
         server.run();
 
         System.out.println("Finished ServletServer");
     }
 
     public static ServletServer getServer ()
     {
         if (s_server == null) {
             s_server = new ServletServer();
         }
 
         return s_server;
     }
 
     public String getAddress ()
     {
         if (_contextPath == null || _contextPath.length() < 1) {
             return _rootPath + "/";
         }
         else {
             return _rootPath + _contextPath + "/";
         }
     }
 
     private ServletServer ()
     {
         _inited = false;
     }
 
     /**
      * Initialize the server.
      * @param portNumber the port number to listen for http requests
      * @param contextPath context path of the servlet, usually "" or "/blah"
      */
     public void init (int portNumber, String contextPath)
     {
         if (_inited) {
            // we only allow initialization once
             return;
         }
         _server = new Server(portNumber);
         _rootPath = getLocalAddress();
         if (portNumber != 80) {
            // this is kind of funky
            // we want the root path to include the port number when running on a home server
            // but when running in heroku - we don't want the port number
            // so see if we're at home:
            String kevinVar = System.getenv("KEVIN");
            if (kevinVar != null && kevinVar.length() > 0) {
                _rootPath += ":" + portNumber;
            }

         }
         _contextPath = contextPath;
 
         // set up the connector
         //Connector connector = new SelectChannelConnector();
         Connector connector = new SocketConnector();
         connector.setPort(portNumber);
         _server.setConnectors(new Connector[]{connector});
 
         // set up the internal context, used for internal servlets
         _internalContext = new ServletContextHandler(_server, _contextPath, ServletContextHandler.SESSIONS);
 
         _inited = true;
     }
 
     public void run ()
     {
         if (!_inited) {
             // default initialization
             init(80, "");
         }
         
         try
         {
             _server.start();
             _server.join();
         }
         catch (Exception exc)
         {
             String exception = exc.toString();
             System.out.println("Servlet startup failed with " + exception);
             _server = null;
         }
 
     }
 
     /**
      * Add a servlet to the server.
      * This adds a servlet
      * <p/>
      * This method must be called before the server is started.
      *
      * @param srv  The servlet to be added
      * @param servletName the name of the servlet
      * @return an error string, or null if no error
      */
     public String addServlet (Servlet srv, String servletName)
     {
         //Log.servlet.info("Adding internal servlet with contextPath %s", contextPath);
         String contextPath = "/" + servletName + "/*";
         _internalContext.addServlet(new ServletHolder(srv), contextPath);
 
         return null;
     }
 
     public void addTestServlets ()
     {
         // add the echo test servlets
         addServlet(new EchoServlet(), EchoServlet.ServletName);
         addServlet(new EchoReply(), EchoReply.ServletName);
     }
 
     private static String getLocalAddress ()
     {
         String result;
         try {
             InetAddress localH = InetAddress.getLocalHost();
             String hostAddress = localH.toString();
             int indx = hostAddress.indexOf('/');
             if (indx >= 0) {
                 hostAddress = hostAddress.substring(indx + 1);
             }
             result = "http://" + hostAddress;
         }
         catch (UnknownHostException uhe) {
             // just use default & hope it works
             result = "http://localhost";
         }
         return result;
     }
 
     public static void redirect (HttpServletResponse response, String redirectTo)
             throws IOException
     {
         String addr = getServer().getAddress() + redirectTo;
         
         response.setContentType("text/plain");
         response.sendRedirect(addr);
     }
 }
