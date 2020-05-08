 package fm.audiobox.configurations;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fm.audiobox.interfaces.ICacheManager;
 import fm.audiobox.interfaces.IConfiguration;
 import fm.audiobox.interfaces.IConnector.IConnectionMethod;
 import fm.audiobox.interfaces.IFactory;
 import fm.audiobox.interfaces.ILoginExceptionHandler;
 import fm.audiobox.interfaces.IServiceExceptionHandler;
 
 public class DefaultConfiguration implements IConfiguration {
 
   private static Logger log = LoggerFactory.getLogger(DefaultConfiguration.class);
 
   private static final String APP_NAME_PLACEHOLDER = "${APP_NAME}";
   private static final String VERSION_PLACEHOLDER = "${VERSION}";
 
   /** Prefix for properties keys */
   private static final String PROP_PREFIX = "libaudioboxfm-core.";
 
   /** Keyword used to check if libs have GA */
   private static final String SNAPSHOT = "-SNAPSHOT";
 
   public static final String APPLICATION_NAME = "Java libs";
   public static final int MAJOR = 2;
   public static final int MINOR = 1;
   public static final int REVISION = 1;
   public static final String VERSION = MAJOR + "." + MINOR + "." + REVISION;
   public static final String PROTOCOL = "http";
   public static final String HOST = "audiobox.fm";
   public static final int PORT = 80;
   public static final String PATH = "api";
 
   /** Properties descriptor reader */
   private static Properties sProperties = new Properties();
 
 
   private ContentFormat requestFormat = ContentFormat.XML;
   private boolean useCache = false;
   private IFactory factory = new DefaultFactory();
   private ILoginExceptionHandler loginHandler;
   private IServiceExceptionHandler serviceHandler;
   private String appName = APPLICATION_NAME;
   private String version = VERSION;
   private Class<? extends IConnectionMethod> connMethodClass = DefaultRequestMethod.class;
   private ExecutorService executor;
   private ICacheManager cacheManager;
 
   private String mUserAgent;
 
   public DefaultConfiguration(String appName, int major, int minor, int revision, ContentFormat requestFormat){
     this.setApplicationName(appName);
     this.setVersion(major, minor, revision);
     this.setRequestFormat(requestFormat);
     log.debug("Application name: " + appName);
     log.debug("Application version: " + this.getVersion() );
     log.debug("Request format: " + requestFormat.toString() );
     log.info("Configuration loaded");
     this.executor = Executors.newSingleThreadExecutor();
     this.setCacheManager( new DefaultCacheManager() );
 
     String version = "unattended";
     String ga_flag = "S";
     try {
      version = DefaultConfiguration.getProperty("version");
       ga_flag = version.contains(SNAPSHOT) ? ga_flag : "GA";
       version = version.replace(SNAPSHOT, "");
     } catch (FileNotFoundException e) {
       log.error("Environment properties file not found: " + e.getMessage());
     } catch (IOException e) {
       log.error("Unable to access the environment properties file: " + e.getMessage());
     }
 
     mUserAgent = "AudioBox.fm/" + version + " (Java; " + ga_flag + "; " +
         System.getProperty("os.name") + " " +
         System.getProperty("os.arch") + "; " + 
         System.getProperty("user.language") + "; " +
         System.getProperty("java.runtime.version") +  ") " +
         System.getProperty("java.vm.name") + "/" + 
         System.getProperty("java.vm.version") + 
         " " + APP_NAME_PLACEHOLDER + "/" + VERSION_PLACEHOLDER;
   }
 
 
   public DefaultConfiguration(String appName, int major, int minor, int revision){
     this(appName, major, minor, revision, ContentFormat.XML);
   }
 
   public DefaultConfiguration(String appName){
     this(appName, MAJOR, MINOR, REVISION);
   }
 
 
   @Override
   public void setRequestFormat(ContentFormat requestFormat) {
     this.requestFormat = requestFormat;
   }
 
   @Override
   public ContentFormat getRequestFormat() {
     return this.requestFormat;
   }
 
   @Override
   public IFactory getFactory() {
     return this.factory;
   }
 
   @Override
   public void setApplicationName(String appName) {
     this.appName = appName;
   }
 
   @Override
   public String getApplicationName() {
     return this.appName;
   }
 
 
   @Override
   public void setVersion(int major, int minor, int revision) {
     this.version = major + "." + minor + "." + revision;
   }
 
 
   @Override
   public String getVersion() {
     return this.version;
   }
 
   @Override
   public String getUserAgent(){
     mUserAgent = mUserAgent
         .replace(APP_NAME_PLACEHOLDER, getApplicationName() )
         .replace(VERSION_PLACEHOLDER, getVersion() );
     log.debug("Computed UA: " + mUserAgent);
     return mUserAgent;
   }
 
 
   @Override
   public String getProtocol(IConfiguration.Connectors server) {
     String prop = safelyGetProperty(server, "protocol");
     return prop != null ? prop : PROTOCOL;
   }
 
   @Override
   public String getHost(IConfiguration.Connectors server) {
     String prop = safelyGetProperty(server, "host");
     return prop != null ? prop : HOST;
   }
 
   
   @Override
   public int getPort(IConfiguration.Connectors server){
     String prop = safelyGetProperty(server, "port");
     return prop != null ? Integer.parseInt(prop, 10) : PORT;
   }
 
   @Override
   public String getPath(IConfiguration.Connectors server) {
     String prop = safelyGetProperty(server, "apiPath");
     return prop != null ? prop : PATH;
   }
 
   
   @Override
   public void setUseCache(boolean useCache) {
     this.useCache = useCache;
   }
 
   @Override
   public boolean isCacheEnabled() {
     return this.useCache;
   }
 
 
   @Override
   public void setDefaultLoginExceptionHandler(ILoginExceptionHandler handler) {
     this.loginHandler = handler;
   }
 
   @Override
   public ILoginExceptionHandler getDefaultLoginExceptionHandler() {
     return this.loginHandler;
   }
 
 
   @Override
   public void setDefaultServiceExceptionHandler(IServiceExceptionHandler handler) {
     this.serviceHandler = handler;
   }
 
 
   @Override
   public IServiceExceptionHandler getDefaultServiceExceptionHandler() {
     return this.serviceHandler;
   }
 
 
   @Override
   public void setHttpMethodType(Class<? extends IConnectionMethod> method) {
     this.connMethodClass = method; 
   }
 
 
   @Override
   public Class<? extends IConnectionMethod> getHttpMethodType() {
     return this.connMethodClass;
   }
 
 
   @Override
   public ExecutorService getExecutor() {
     return executor;
   }
 
 
   @Override
   public void setCacheManager(ICacheManager manager) {
     this.cacheManager = manager;
   }
 
 
   @Override
   public ICacheManager getCacheManager() {
     return this.cacheManager;
   }
 
 
 
   public String getEnvProperty(){
     String env = safelyGetProperty("env");
     return env != null ? env : "default";
   }
 
 
   /* 
    * Following code manages internal cache of {@link IEntities}
    */
 
   // ---------------------------------------------------
 
   /* --------------- */
   /* Private methods */
   /* --------------- */
 
 
   /**
    * This method returns the AudioBox.fm properties file reader.
    * 
    * @param key the property you are looking for
    * 
    * @return the value of the property
    * @throws IOException if property files is not accessible or does not exists
    */
   private static String getProperty(String key) throws IOException {
     if (sProperties == null || sProperties.isEmpty()) {
       sProperties.load(DefaultConfiguration.class.getResourceAsStream("/fm/audiobox/core/config/env.properties"));
     }
 
    return sProperties.getProperty(PROP_PREFIX + key);
   }
   
   
   /**
    * This method returns the AudioBox.fm properties file reader.
    * 
    * @param server the server scope of the property
    * @param key the property you are looking for
    * 
    * @return the value of the property
    * @throws IOException if property files is not accessible or does not exists
    */
   private static String getProperty(IConfiguration.Connectors server, String key) throws IOException {
     if (sProperties == null || sProperties.isEmpty()) {
       sProperties.load(DefaultConfiguration.class.getResourceAsStream("/fm/audiobox/core/config/env.properties"));
     }
 
     return getProperty(PROP_PREFIX + server.toString().toLowerCase() + "." + key);
   }
 
 
   /**
    * Use this method to get the property in a safe way.<br/>
    * This method will return null if the property file is not accessible for any reason.
    * 
    * @param key the property you are looking for
    * 
    * @return the value of the property
    */
   private static String safelyGetProperty(IConfiguration.Connectors server, String key) {
     try {
       return getProperty(server, key);
     } catch (IOException e) {
       log.warn("Error accessing environment properties file. Default values will be used");
     }
     return null;
   }
   
   /**
    * Use this method to get the property in a safe way.<br/>
    * This method will return null if the property file is not accessible for any reason.
    * 
    * @param key the property you are looking for
    * 
    * @return the value of the property
    */
   private static String safelyGetProperty(String key) {
     try {
      return getProperty(key);
     } catch (IOException e) {
       log.warn("Error accessing environment properties file. Default values will be used");
     }
     return null;
   }
 
 }
