 package org.lightmare.config;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 /**
  * Easy way to retrieve configuration properties from configuration file
  * 
  * @author levan
  * 
  */
 public class Configuration {
 
     // cache for all configuration passed programmatically or read from file
     private final Map<String, String> config = new HashMap<String, String>();
 
     // Default semaphore capacity
     public static final int SEMAPHORE_SIZE = 1;
 
     /**
      * <a href="netty.io">Netty</a> server / client configuration properties for
      * RPC calls
      */
     public static final String IP_ADDRESS = "listening_ip";
 
     public static final String PORT = "listening_port";
 
     public static final String BOSS_POOL = "boss_pool_size";
 
     public static final String WORKER_POOL = "worker_pool_size";
 
     public static final String CONNECTION_TIMEOUT = "timeout";
 
     // properties for datasource path and deployment path
     public static final String DATA_SOURCE_PATH = "dspath";
 
     public static Set<String> DEPLOYMENT_PATH;
 
     // runtime to get avaliable processors
     private static final Runtime RUNTIME = Runtime.getRuntime();
 
     /**
      * Default properties
      */
     public static final String IP_ADDRESS_DEF = "0.0.0.0";
 
     public static final String PORT_DEF = "1199";
 
     public static final String BOSS_POOL_DEF = "1";
 
     public static final int WORKER_POOL_DEF = 3;
 
     public static final String CONNECTION_TIMEOUT_DEF = "1000";
 
     public static final boolean SERVER_DEF = Boolean.TRUE;
 
     public static final String DATA_SOURCE_PATH_DEF = "./ds";
 
     public static final Set<String> DEPLOYMENT_PATH_DEF = new HashSet<String>(
 	    Arrays.asList("./deploy"));
 
     /**
      * Properties which version of server is running remote it requires server
      * client RPC infrastructure or local (embeddable mode)
      */
     private boolean remote;
 
     private boolean server = SERVER_DEF;
 
     private boolean client;
 
     private static final String CONFIG_FILE = "./config/config.properties";
 
     // String prefixes for jndi names
     public static final String JPA_NAME = "java:comp/env/";
 
     public static final String EJB_NAME = "ejb:";
 
     public static final int EJB_NAME_LENGTH = 4;
 
     private static final Logger LOG = Logger.getLogger(Configuration.class);
 
     public Configuration() {
     }
 
     public void setDefaults() {
 	if (!config.containsKey(IP_ADDRESS)) {
 	    config.put(IP_ADDRESS, IP_ADDRESS_DEF);
 	}
 
 	if (!config.containsKey(PORT)) {
 	    config.put(PORT, PORT_DEF);
 	}
 
 	if (!config.containsKey(BOSS_POOL)) {
 	    config.put(BOSS_POOL, BOSS_POOL_DEF);
 	}
 
 	if (!config.containsKey(WORKER_POOL)) {
 
 	    int workers = RUNTIME.availableProcessors() * WORKER_POOL_DEF;
 	    String workerProperty = String.valueOf(workers);
 	    config.put(WORKER_POOL, workerProperty);
 	}
 
 	if (!config.containsKey(CONNECTION_TIMEOUT)) {
 	    config.put(CONNECTION_TIMEOUT, CONNECTION_TIMEOUT_DEF);
 	}
 
 	if (!config.containsKey(DATA_SOURCE_PATH)) {
 	    config.put(DATA_SOURCE_PATH, DATA_SOURCE_PATH_DEF);
 	}
 
 	if (DEPLOYMENT_PATH == null) {
 	    DEPLOYMENT_PATH = DEPLOYMENT_PATH_DEF;
 	}
     }
 
    public void configure() {
	setDefaults();
    }

     public String getStringValue(String key) {
 	return config.get(key);
     }
 
     public int getIntValue(String key) {
 	return Integer.parseInt(config.get(key));
     }
 
     public long getLongValue(String key) {
 	return Long.parseLong(config.get(key));
     }
 
     public boolean getBooleanValue(String key) {
 	return Boolean.parseBoolean(config.get(key));
     }
 
     public void putValue(String key, String value) {
 	config.put(key, value);
     }
 
     public void loadFromFile() {
 	try {
 	    File configFile = new File(CONFIG_FILE);
 	    if (configFile.exists()) {
 		InputStream propertiesStream = new FileInputStream(configFile);
 		loadFromStream(propertiesStream);
 		propertiesStream.close();
 	    } else {
 		configFile.mkdirs();
 	    }
 	} catch (IOException ex) {
 	    LOG.error("Could not open config file", ex);
 	}
 
     }
 
     public void loadFromFile(String configFilename) {
 	try {
 	    FileInputStream propertiesStream = new FileInputStream(new File(
 		    configFilename));
 	    loadFromStream(propertiesStream);
 	    propertiesStream.close();
 	} catch (IOException ex) {
 	    LOG.error("Could not open config file", ex);
 	}
 
     }
 
     public void loadFromResource(String resourceName, ClassLoader loader) {
 	InputStream resourceStream = loader
 		.getResourceAsStream(new StringBuilder("META-INF/").append(
 			resourceName).toString());
 	if (resourceStream == null) {
 	    LOG.error("Configuration resource doesn't exist");
 	    return;
 	}
 	loadFromStream(resourceStream);
 	try {
 	    resourceStream.close();
 	} catch (IOException ex) {
 	    LOG.error("Could not load resource", ex);
 	}
     }
 
     /**
      * Load {@link Configuration} in memory as {@link Map} of parameters
      * 
      * @throws IOException
      */
     public void loadFromStream(InputStream propertiesStream) {
 	try {
 	    Properties props = new Properties();
 	    props.load(propertiesStream);
 	    for (String propertyName : props.stringPropertyNames()) {
 		config.put(propertyName, props.getProperty(propertyName));
 	    }
 	    propertiesStream.close();
 	} catch (IOException ex) {
 	    LOG.error("Could not load configuration", ex);
 	}
     }
 
     public boolean isRemote() {
 	return remote;
     }
 
     public void setRemote(boolean remote) {
 	this.remote = remote;
     }
 
     public boolean isServer() {
 	return server;
     }
 
     public void setServer(boolean server) {
 	this.server = server;
     }
 
     public boolean isClient() {
 	return client;
     }
 
     public void setClient(boolean client) {
 	this.client = client;
     }
 
     public void addDeploymentPath(String path) {
 
 	synchronized (Configuration.class) {
 	    if (DEPLOYMENT_PATH == null) {
 		DEPLOYMENT_PATH = new HashSet<String>();
 	    }
 
 	    DEPLOYMENT_PATH.add(path);
 	}
     }
 
     public Set<String> getDeploymentPath() {
 
 	return DEPLOYMENT_PATH;
     }
 }
