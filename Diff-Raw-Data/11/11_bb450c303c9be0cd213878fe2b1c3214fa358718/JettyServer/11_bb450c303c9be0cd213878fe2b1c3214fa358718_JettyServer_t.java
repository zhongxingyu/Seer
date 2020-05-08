 package org.pentaho.pac.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.BindException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.StringTokenizer;
 
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
 import org.mortbay.jetty.plus.jaas.JAASUserRealm;
 import org.mortbay.jetty.security.Constraint;
 import org.mortbay.jetty.security.ConstraintMapping;
 import org.mortbay.jetty.security.SecurityHandler;
 import org.mortbay.jetty.security.SslSocketConnector;
 import org.mortbay.jetty.servlet.Context;
 import org.mortbay.jetty.servlet.ServletHolder;
 import org.pentaho.pac.server.common.ConsoleProperties;
 import org.pentaho.pac.server.i18n.Messages;
 
 public class JettyServer implements Halter, IJettyServer {
   protected Server server;
   String delimeter = null;
   String consoleHome = null;
   String callbackHandler = null;
   boolean securityEnabled = false;
   private int portNumber;
   String roles = null;
   String authLoginConfigPath = null;
   String realmName = null;
   String loginModuleName = null;
   String securitEnabledValue = null;
   private String hostname;
   private static final Log logger = LogFactory.getLog(JettyServer.class);
   public int stopPort = 0;
   private boolean running = false;
   public static JettyServer jettyServer;
   
   public static final int DEFAULT_PORT_NUMBER = 8099;
   public static final int DEFAULT_SSL_PORT_NUMBER = 8043;
   public static final int DEFAULT_STOP_PORT_NUMBER = 8011;
   public static final String DEFAULT_DELIMETER = ","; //$NON-NLS-1$
   public static final String DEFAULT_HOSTNAME = "localhost"; //$NON-NLS-1$
   public static final String CURRENT_DIR = "."; //$NON-NLS-1$
   public static final String JETTY_HOME = "jetty.home"; //$NON-NLS-1$
   public static final String AUTH_LOGIN_CONFIG_ENV_VAR = "java.security.auth.login.config"; //$NON-NLS-1$
   public static final String DEFAULT_CALLBACK_HANDLER = "org.mortbay.jetty.plus.jaas.callback.DefaultCallbackHandler"; //$NON-NLS-1$
 
   public JettyServer() {
     // Get the CONSOLE_HOME Environment variable. This is required as it is needed to cofigure Jetty
     consoleHome = System.getProperty("CONSOLE_HOME", CURRENT_DIR);
     // Set the jetty.home to PEHTAHO_HOME
     System.setProperty(JETTY_HOME, consoleHome);
     readConfiguration();
     server = new Server();
     setupServer();
     startServer();
     stopHandler(this, stopPort);
   }
 
   public JettyServer(String hostname, int port, int stop) {
     // Get the CONSOLE_HOME Environment variable. This is required as it is needed to cofigure Jetty
     consoleHome = System.getProperty("CONSOLE_HOME", CURRENT_DIR);
     // Set the jetty.home to PEHTAHO_HOME
     System.setProperty(JETTY_HOME, consoleHome);
     readConfiguration();
     this.portNumber = port;
     this.hostname = hostname;
     stopPort = stop;
     server = new Server();
     setupServer();
     startServer();
     stopHandler(this, stopPort);
   }
 
   public static JettyServer getInstance() {
     return jettyServer;
   }
 
   public boolean isRunning() {
     return running;
   }
 
   public void stop() {
 
     Halter halter = new Halter(this);
     // Create the thread supplying it with the runnable object
     Thread thread = new Thread(halter);
 
     // Start the thread
     thread.start();
   }
 
   public void haltNow() {
     try {
       server.stop();
       running = false;
     } catch (Exception e) {
       logger.error("error starting server", e); //$NON-NLS-1$
     }
   }
 
   private void startServer() {
     Connector connector = null;
     // Check whether ssl needs to be enabled or not
     String value = ConsoleProperties.getInstance().getProperty(ConsoleProperties.SSLENABLED);
     boolean sslEnable =  (value != null && value.length() > 0) ? Boolean.parseBoolean(value) : false;
     SslParameters sslParameters = new SslParameters(ConsoleProperties.getInstance());
     if(sslEnable) {
       connector = setupSslConnector(sslParameters);
       connector.setPort(sslParameters.getSslPort());
     }  else {
       connector = new SocketConnector();
       connector.setPort(portNumber);
     }
 
     connector.setHost(hostname);
     logger.info("starting " + connector.getName()); //$NON-NLS-1$
     server.setConnectors(new Connector[] { connector });
     server.setStopAtShutdown(true);
     logger.info("Console Starting"); //$NON-NLS-1$
 
     try {
       server.start();
     } catch (BindException e) {
       haltNow();
     } catch (RuntimeException e) {
       // let runtime exceptions leak, developer should handle them before release.
       logger.error("Error starting server", e); //$NON-NLS-1$
       throw e;
     } catch (Exception e) {
       logger.error("Error starting server", e); //$NON-NLS-1$
     }
   }
 
   public String getResourceBaseName() {
     return "www/org.pentaho.pac.PentahoAdminConsole"; //$NON-NLS-1$
   }
   
   public String[] getWelcomeFiles() {
     return new String[] { "PentahoAdminConsole.html" };
   }
   
   protected Context createServletContext() {
     ContextHandlerCollection contextHandlers = new ContextHandlerCollection();
     Context servletContext = new Context(contextHandlers, "/", Context.SESSIONS); //$NON-NLS-1$
     servletContext.setResourceBase( getResourceBaseName() ); //$NON-NLS-1$
     servletContext.setWelcomeFiles( getWelcomeFiles() ); //$NON-NLS-1$
     
     return servletContext;
   }
   
   public void configureResourceHandlers( Context servletContext, SecurityHandler securityHandler ) {
     ResourceHandler resources = new ResourceHandler();
     resources.setResourceBase( getResourceBaseName() );
     resources.setWelcomeFiles( getWelcomeFiles() ); //$NON-NLS-1$
 
     if (securityHandler != null) {
       server.setHandlers(new Handler[] { securityHandler, resources, servletContext });
     } else {
       server.setHandlers(new Handler[] { resources, servletContext });
     }
   }
 
   public SecurityHandler configureSecurityHandler() {
     SecurityHandler securityHandler = null;
     // This is required for the jetty security to locate the security configuration file
     System.setProperty(AUTH_LOGIN_CONFIG_ENV_VAR, authLoginConfigPath); 
     
     // configure security if necessary
     if (securityEnabled) {
         Constraint constraint = new Constraint();
         constraint.setName(Constraint.__BASIC_AUTH);
 
         // Creating the roles list
         StringTokenizer token = new StringTokenizer(roles, delimeter);
         String[] rolesList = new String[token.countTokens()];
         int i =0; 
         while(token.hasMoreTokens()) {
           rolesList[i++] = token.nextToken();
         }
         
         constraint.setRoles(rolesList);
         constraint.setAuthenticate(true);
         ConstraintMapping constraintMapping = new ConstraintMapping();
         constraintMapping.setConstraint(constraint);
         constraintMapping.setPathSpec("/*"); //$NON-NLS-1$
         JAASUserRealm realm = new JAASUserRealm(realmName);
         realm.setLoginModuleName(loginModuleName);
         realm.setCallbackHandlerClass(callbackHandler);
         securityHandler = new SecurityHandler();
         securityHandler.setUserRealm(realm); //$NON-NLS-1$ //$NON-NLS-2$        
         securityHandler.setConstraintMappings(new ConstraintMapping[] { constraintMapping });
     }
     return securityHandler;
   }
   
   public void configureServlets( Context servletContext ) {
     // add servlets
     ServletHolder defaultServlet = new ServletHolder(new DefaultConsoleServlet("/", this)); //$NON-NLS-1$
     servletContext.addServlet(defaultServlet, "/*"); //$NON-NLS-1$
     servletContext.addServlet(defaultServlet, "/halt"); //$NON-NLS-1$
 
     ServletHolder pacsvc = new ServletHolder(new org.pentaho.pac.server.PacServiceImpl());
     servletContext.addServlet(pacsvc, "/pacsvc"); //$NON-NLS-1$
 
     ServletHolder schedulersvc = new ServletHolder(new org.pentaho.pac.server.SchedulerServiceImpl());
     servletContext.addServlet(schedulersvc, "/schedulersvc"); //$NON-NLS-1$
 
     ServletHolder subscriptionsvc = new ServletHolder(new org.pentaho.pac.server.SubscriptionServiceImpl());
     servletContext.addServlet(subscriptionsvc, "/subscriptionsvc"); //$NON-NLS-1$
 
     ServletHolder solutionrepositorysvc = new ServletHolder(new org.pentaho.pac.server.SolutionRepositoryServiceImpl());
     servletContext.addServlet(solutionrepositorysvc, "/solutionrepositorysvc"); //$NON-NLS-1$
     
     ServletHolder jdbcdriverdiscoveryservice = new ServletHolder(new org.pentaho.pac.server.common.JdbcDriverDiscoveryServiceImpl());
     servletContext.addServlet(jdbcdriverdiscoveryservice, "/jdbcdriverdiscoverysvc"); //$NON-NLS-1$
 
   }
 
   public void configureEventListeners( Context servletContext ) {
     // no-op for now
   }
   
   protected final void setupServer() {
     server = new Server();
     SecurityHandler securityHandler = configureSecurityHandler();
     Context servletContext = createServletContext();
     configureServlets( servletContext );
     configureEventListeners( servletContext );
     configureResourceHandlers( servletContext, securityHandler );
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
 
   public static void main(String[] args) {
     jettyServer = new JettyServer();
   }
 
   // TODO sbarkdull, can this be deleted?
   public static class HomeHandler extends AbstractHandler {
     public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
         throws IOException, ServletException {
       Request base_request = (request instanceof Request) ? (Request) request : HttpConnection.getCurrentConnection()
           .getRequest();
       base_request.setHandled(true);
 
       response.setStatus(HttpServletResponse.SC_OK);
       response.setContentType("text/html"); //$NON-NLS-1$
       response.getWriter().println("<h1>Hello OneContext</h1>"); //$NON-NLS-1$
 
     }
   }
 
   private class Halter implements Runnable {
     // This method is called when the thread runs
 
     private JettyServer jettyServer;
 
     public Halter(JettyServer jettyServer) {
       this.jettyServer = jettyServer;
     }
 
     public void run() {
       System.out.println(Messages.getString("CONSOLE.WAITING_TO_HALT")); //$NON-NLS-1$
       try {
         Thread.sleep(3000);
       } catch (Exception e) {
         // ignore this
       }
       System.out.println(Messages.getString("CONSOLE.HALTING")); //$NON-NLS-1$
       jettyServer.haltNow();
     }
   }
   
   public void readConfiguration() {
       String port = ConsoleProperties.getInstance().getProperty(ConsoleProperties.CONSOLE_PORT_NUMBER);
       String hostname = ConsoleProperties.getInstance().getProperty(ConsoleProperties.CONSOLE_HOST_NAME);
       String stopPortNumber = ConsoleProperties.getInstance().getProperty(ConsoleProperties.STOP_PORT);
 
       if (port != null && port.length() > 0) {
         this.portNumber = Integer.parseInt(port);
       } else {
         this.portNumber = DEFAULT_PORT_NUMBER;
       }
 
       if (stopPortNumber != null && stopPortNumber.length() > 0) {
         stopPort = Integer.parseInt(stopPortNumber);
       } else {
         stopPort = DEFAULT_STOP_PORT_NUMBER;
       }
 
       
       if (hostname != null && hostname.length() > 0) {
         this.hostname = hostname;
       } else {
         this.hostname = DEFAULT_HOSTNAME;
       }
       
       roles = ConsoleProperties.getInstance().getProperty(ConsoleProperties.CONSOLE_SECURITY_ROLES_ALLOWED);
      String delimeterValue = ConsoleProperties.getInstance().getProperty(ConsoleProperties.CONSOLE_SECURITY_ROLE_DELIMITER);
 
       if (delimeterValue != null && delimeterValue.length() > 0) {
         this.delimeter = delimeterValue;
       } else {
         this.delimeter = DEFAULT_DELIMETER;
       }
       
       authLoginConfigPath = ConsoleProperties.getInstance().getProperty(ConsoleProperties.CONSOLE_SECURITY_AUTH_CONFIG_PATH);
       realmName = ConsoleProperties.getInstance().getProperty(ConsoleProperties.CONSOLE_SECURITY_REALM_NAME);
       loginModuleName = ConsoleProperties.getInstance().getProperty(ConsoleProperties.CONSOLE_SECURITY_LOGIN_MODULE_NAME);
       String securitEnabledValue = ConsoleProperties.getInstance().getProperty(ConsoleProperties.CONSOLE_SECURITY_ENABLED);
       if(securitEnabledValue != null && securitEnabledValue.length() > 0) {
         securityEnabled =  Boolean.parseBoolean(securitEnabledValue);
       }
       String callbackHandlerValue = ConsoleProperties.getInstance().getProperty(ConsoleProperties.CONSOLE_SECURITY_CALLBACK_HANDLER);
       if(callbackHandlerValue != null && callbackHandlerValue.length() > 0) {
         callbackHandler =  callbackHandlerValue;
       } else  {
         callbackHandler =  DEFAULT_CALLBACK_HANDLER;
       }
       
   }
   public void stopHandler(JettyServer jServer, int stopPort) {
     ServerSocket server = null;
     try {
       server = new ServerSocket(stopPort);
     } catch (IOException ioe) {
       System.out.println("IO Error: " + ioe.getLocalizedMessage());//$NON-NLS-1$
     }
     try {
       Socket s = server.accept();
       Thread t = new Thread(new RequestHandler(jServer, s));
       t.start();
     } catch (Exception e) {
       System.out.println("IO Error: " + e.getLocalizedMessage());//$NON-NLS-1$
     }
   }
   
   private Connector setupSslConnector(SslParameters ssl)  {
     Connector connector;
     String keyStore = ssl.getKeyStore();
     if (keyStore == null) {
         keyStore = System.getProperty("javax.net.ssl.keyStore", "");
         if (keyStore == null) {
             throw new IllegalArgumentException(
                            "keyStore or system property javax.net.ssl.keyStore must be set");
         }
     }
 
     String keyStorePassword = ssl.getKeyStorePassword();
     if (keyStorePassword == null) {
         keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
         if (keyStorePassword == null) {
             throw new IllegalArgumentException(
                 "keyStorePassword or system property javax.net.ssl.keyStorePassword must be set");
         }
     }
     SslSocketConnector sslConnector = new SslSocketConnector();
     sslConnector.setConfidentialPort(ssl.getSslPort());
     sslConnector.setPassword(ssl.getKeyStorePassword());
     sslConnector.setKeyPassword(ssl.getKeyPassword() != null ? ssl.getKeyPassword() : keyStorePassword);
     sslConnector.setKeystore(keyStore);
     sslConnector.setKeystoreType(ssl.getKeyStoreType());
     sslConnector.setNeedClientAuth(ssl.isNeedClientAuth());
     sslConnector.setWantClientAuth(ssl.isWantClientAuth());
     // important to set this values for selfsigned keys
     // otherwise the standard truststore of the jre is used
     sslConnector.setTruststore(ssl.getTrustStore());
     if (ssl.getTrustStorePassword() != null) {
         // check is necessary because if a null password is set
         // jetty would ask for a password on the comandline
         sslConnector.setTrustPassword(ssl.getTrustStorePassword());
     }
     sslConnector.setTruststoreType(ssl.getTrustStoreType());
     connector = sslConnector;
     return connector;
 }
   private class RequestHandler implements Runnable {
     // This method is called when the thread runs
 
     private JettyServer jettyServer;
 
     private Socket socket;
 
     public RequestHandler(JettyServer jettyServer, Socket socket) {
       this.jettyServer = jettyServer;
       this.socket = socket;
     }
 
     public void run() {
       try {
         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         String inputLine;
         while ((inputLine = in.readLine()) != null) {
           if (inputLine != null && inputLine.length() > 0) {
             inputLine = inputLine.trim();
             if (inputLine.equalsIgnoreCase(ConsoleProperties.STOP_ARG)) {
               System.out.println("Waiting to halt console"); //$NON-NLS-1$
               try {
                 Thread.sleep(3000);
               } catch (Exception e) {
                 // ignore this
               }
               System.out.println("Console is halting"); //$NON-NLS-1$
               jettyServer.haltNow();
             }
           }
         }
       } catch (IOException ioe) {
         System.out.println("IO Error: " + ioe.getLocalizedMessage());//$NON-NLS-1$
       }
     }
   }
 }
 
