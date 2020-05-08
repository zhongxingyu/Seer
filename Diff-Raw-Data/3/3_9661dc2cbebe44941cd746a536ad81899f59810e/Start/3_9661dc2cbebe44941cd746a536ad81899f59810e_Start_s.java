 import org.eclipse.jetty.security.SpnegoLoginService;
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.bio.SocketConnector;
 import org.eclipse.jetty.server.handler.DefaultHandler;
 import org.eclipse.jetty.server.handler.HandlerCollection;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.server.ssl.SslSocketConnector;
 import org.eclipse.jetty.webapp.WebAppContext;
 import se.su.it.svc.FilterHandler;
 import se.su.it.svc.SuCxfAuthenticator;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.JarURLConnection;
 import java.net.URL;
 import java.util.*;
 
 
 public class Start {
   private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Start.class);
   public static void main(String[] args) {
 
     Properties properties = new Properties();
     // Begin Check if properties file is defined as define argument
     String definedConfigFileName = System.getProperty("config.properties");
     if(definedConfigFileName != null) {
       logger.info("The configuration variable config.properties was set to <" + definedConfigFileName.trim() + ">.\r\n Checking properties in file...");
       try {
         File file=new File(definedConfigFileName.trim());
         if(!file.exists()) {
           logger.error("<" + definedConfigFileName.trim() + "> the file was not found. Quitting....");
           System.exit(10);
         }
         FileInputStream is = new FileInputStream(definedConfigFileName.trim());
         properties.load(is);
         is.close();
         if(!checkDefinedConfigFileProperties(properties)) {
           System.exit(10);
         }
       } catch (Exception e) {
         logger.error("<" + definedConfigFileName.trim() + ">, got an exception <" + e.getMessage() + "> trying to access file. Quitting....");
         System.exit(10);
       }
     } else {
       // Begin Default properties
       logger.warn("No config.properties file set in system environment, using defaults.");
 
       logger.warn("database.url=jdbc:mysql://localhost/gormtest");
       logger.warn("database.driver=com.mysql.jdbc.Driver");
       logger.warn("database.user=gormtest");
       logger.warn("database.password=gormtest");
       //Ldap
       logger.warn("ldap.serverro=ldap://ldap-test.su.se");
       logger.warn("ldap.serverrw=ldap://sukat-test-ldaprw02.it.su.se");
       //Ssl
       logger.warn("http.port=443");
       logger.warn("ssl.enabled=true");
       logger.warn("ssl.keystore=cxf-svc-server.keystore");
       logger.warn("ssl.password=changeit");
       //Spnego
       logger.warn("spnego.conf=/etc/spnego.conf");
       logger.warn("spnego.properties=spnego.properties");
       logger.warn("spnego.realm=SU.SE");
       //Ehcache
       logger.warn("ehcache.maxElementsInMemory=10000");
       logger.warn("ehcache.eternal=false");
       logger.warn("ehcache.timeToIdleSeconds=120");
       logger.warn("ehcache.timeToLiveSeconds=600");
       logger.warn("ehcache.overflowToDisk=false");
       logger.warn("ehcache.diskPersistent=false");
       logger.warn("ehcache.diskExpiryThreadIntervalSeconds=120");
       logger.warn("ehcache.memoryStoreEvictionPolicy=LRU");
 
       properties.put("database.url", "jdbc:mysql://localhost/gormtest");
       properties.put("database.driver", "com.mysql.jdbc.Driver");
       properties.put("database.user", "gormtest");
       properties.put("database.password", "gormtest");
       //Ldap
       properties.put("ldap.serverro", "ldap://ldap-test.su.se");
       properties.put("ldap.serverrw", "ldap://sukat-test-ldaprw02.it.su.se");
       //Ssl
       properties.put("http.port", "443");
       properties.put("ssl.enabled", "true");
       properties.put("ssl.keystore", "cxf-svc-server.keystore");
       properties.put("ssl.password", "changeit");
       //Spnego
       properties.put("spnego.conf","/etc/spnego.conf");
       properties.put("spnego.properties", "spnego.properties");
       properties.put("spnego.realm", "SU.SE");
       //Ehcache
       properties.put("ehcache.maxElementsInMemory", "10000");
       properties.put("ehcache.eternal", "false");
       properties.put("ehcache.timeToIdleSeconds", "120");
       properties.put("ehcache.timeToLiveSeconds", "600");
       properties.put("ehcache.overflowToDisk", "false");
       properties.put("ehcache.diskPersistent", "false");
       properties.put("ehcache.diskExpiryThreadIntervalSeconds", "120");
       properties.put("ehcache.memoryStoreEvictionPolicy", "LRU");
       // End Default properties
     }
     // End Check if properties file is defined as define argument
 
     int httpPort        = Integer.parseInt(properties.getProperty("http.port").trim());
     // extracting the config properties for ssl setup
     boolean sslEnabled  = Boolean.parseBoolean(properties.getProperty("ssl.enabled"));
     String sslKeystore  = properties.getProperty("ssl.keystore");
     String sslPassword  = properties.getProperty("ssl.password");
 
     //extracting the config for the spnegp setup
     String spnegoConfigFileName     = properties.getProperty("spnego.conf");
     String spnegoRealm              = properties.getProperty("spnego.realm");
     String spnegoPropertiesFileName = properties.getProperty("spnego.properties");
 
     try {
 
       org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server();
 
       if(sslEnabled) {
         SslSocketConnector connector = new SslSocketConnector();
 
         connector.setPort(httpPort);
         connector.setKeystore(sslKeystore);
         connector.setPassword(sslPassword);
 
         server.setConnectors(new Connector[]{connector});
       } else {
         SocketConnector connector = new SocketConnector();
         connector.setPort(httpPort);
 
         server.setConnectors(new Connector[]{connector});
       }
 
       URL url = Start.class.getClassLoader().getResource("Start.class");
       File warFile = new File(((JarURLConnection) url.openConnection()).getJarFile().getName());
       WebAppContext context = new WebAppContext();
       File webbAppFp = new File("webapp");
       webbAppFp.mkdir();
       context.setTempDirectory(webbAppFp);
       context.setContextPath("/");
       context.setWar(warFile.getAbsolutePath());
 
       FilterHandler fh = new FilterHandler(context.getTempDirectory().toString());
 
       HandlerList handlers = new HandlerList();
       handlers.setHandlers(new Handler[] {fh, context, new DefaultHandler() });
 
       server.setHandler(handlers);
 
       System.setProperty("java.security.auth.login.config", "=file:" + spnegoConfigFileName);
       SpnegoLoginService sLoginService = new SpnegoLoginService(spnegoRealm);
       sLoginService.setConfig(spnegoPropertiesFileName);
       context.getSecurityHandler().setLoginService(sLoginService);
       context.getSecurityHandler().setAuthenticator(new SuCxfAuthenticator(context));
 

       server.start();
       logger.info("Server ready...");
       server.join();
 
     } catch (Exception ex) {
       ex.printStackTrace();
     }
   }
 
   private static boolean checkDefinedConfigFileProperties(Properties properties) {
     // Begin check for mandatory properties
     List<String> notFoundList = new ArrayList<String>();
     if(properties.get("ldap.serverro") == null) {notFoundList.add("ldap.serverro");}
     if(properties.get("ldap.serverrw") == null) {notFoundList.add("ldap.serverrw");}
     if(properties.get("http.port") == null) {notFoundList.add("http.port");}
     if(properties.get("ssl.enabled") == null) {notFoundList.add("ssl.enabled");}
     if(properties.get("ssl.enabled") != null && Boolean.parseBoolean(properties.getProperty("ssl.enabled"))) {
       if(properties.get("ssl.keystore") == null) {notFoundList.add("ssl.keystore");}
       if(properties.get("ssl.password") == null) {notFoundList.add("ssl.password");}
     }
     if(properties.get("spnego.conf") == null) {notFoundList.add("spnego.conf");}
     if(properties.get("spnego.properties") == null) {notFoundList.add("spnego.properties");}
     if(properties.get("spnego.realm") == null) {notFoundList.add("spnego.realm");}
     if(properties.get("ehcache.maxElementsInMemory") == null) {notFoundList.add("ehcache.maxElementsInMemory");}
     if(properties.get("ehcache.eternal") == null) {notFoundList.add("ehcache.eternal");}
     if(properties.get("ehcache.timeToIdleSeconds") == null) {notFoundList.add("ehcache.timeToIdleSeconds");}
     if(properties.get("ehcache.timeToLiveSeconds") == null) {notFoundList.add("ehcache.timeToLiveSeconds");}
     if(properties.get("ehcache.overflowToDisk") == null) {notFoundList.add("ehcache.overflowToDisk");}
     if(properties.get("ehcache.diskPersistent") == null) {notFoundList.add("ehcache.diskPersistent");}
     if(properties.get("ehcache.diskExpiryThreadIntervalSeconds") == null) {notFoundList.add("ehcache.diskExpiryThreadIntervalSeconds");}
     if(properties.get("ehcache.memoryStoreEvictionPolicy") == null) {notFoundList.add("ehcache.memoryStoreEvictionPolicy");}
 
     if(notFoundList.size() <= 0) {
       return true;
     }
 
     for(int i=0;i < notFoundList.size();i++) {
       logger.error("Property <" + notFoundList.get(i) + ">   ...not found");
     }
     // End check for mandatory properties
     logger.error("Quitting because mandatory properties was missing...");
     return false;  //To change body of created methods use File | Settings | File Templates.
   }
 }
