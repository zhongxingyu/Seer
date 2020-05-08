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
 import org.lightmare.cache.DeploymentDirectory;
 import org.lightmare.jpa.datasource.PoolConfig;
 import org.lightmare.jpa.datasource.PoolConfig.PoolProviderType;
 import org.lightmare.utils.ObjectUtils;
 import org.yaml.snakeyaml.Yaml;
 
 /**
  * Easy way to retrieve configuration properties from configuration file
  * 
  * @author levan
  * 
  */
 public class Configuration implements Cloneable {
 
     // cache for all configuration passed programmatically or read from file
     private final Map<Object, Object> config = new HashMap<Object, Object>();
 
     // path where stored adminitrator users
     public static final String ADMIN_USERS_PATH_KEY = "adminUsersPath";
 
     /**
      * <a href="netty.io">Netty</a> server / client configuration properties for
      * RPC calls
      */
     public static final String IP_ADDRESS_KEY = "listeningIp";
 
     public static final String PORT_KEY = "listeningPort";
 
     public static final String BOSS_POOL_KEY = "bossPoolSize";
 
     public static final String WORKER_POOL_KEY = "workerPoolSize";
 
     public static final String CONNECTION_TIMEOUT_KEY = "timeout";
 
     // properties for datasource path and deployment path
     public static final String DEMPLOYMENT_PATH_KEY = "deploymentPath";
 
     public static final String DATA_SOURCE_PATH_KEY = "dataSourcePath";
 
     // runtime to get avaliable processors
     private static final Runtime RUNTIME = Runtime.getRuntime();
 
     /**
      * Default properties
      */
     public static final String ADMIN_USERS_PATH_DEF = "./config/admin/users.properties";
 
     public static final String IP_ADDRESS_DEF = "0.0.0.0";
 
     public static final String PORT_DEF = "1199";
 
     public static final String BOSS_POOL_DEF = "1";
 
     public static final int WORKER_POOL_DEF = 3;
 
     public static final String CONNECTION_TIMEOUT_DEF = "1000";
 
     public static final boolean SERVER_DEF = Boolean.TRUE;
 
     public static final String DATA_SOURCE_PATH_DEF = "./ds";
 
     /**
      * Properties which version of server is running remote it requires server
      * client RPC infrastructure or local (embeddable mode)
      */
     private static final String REMOTE_KEY = "remote";
 
     private static final String SERVER_KEY = "server";
 
     private static final String CLIENT_KEY = "client";
 
     public static final Set<DeploymentDirectory> DEPLOYMENT_PATHS_DEF = new HashSet<DeploymentDirectory>(
 	    Arrays.asList(new DeploymentDirectory("./deploy", Boolean.TRUE)));
 
     public static final Set<String> DATA_SOURCES_PATHS_DEF = new HashSet<String>(
 	    Arrays.asList("./ds"));
 
     private static final String CONFIG_FILE = "./config/configuration.yaml";
 
     // String prefixes for jndi names
     public static final String JPA_NAME = "java:comp/env/";
 
     public static final String EJB_NAME = "ejb:";
 
     public static final int EJB_NAME_LENGTH = 4;
 
     // Configuration keys properties for deployment
     private static final String DEPLOY_CONFIG_KEY = "deployConfiguration";
 
     private static final String ADMIN_USER_PATH_KEY = "adminPath";
 
     private static final String HOT_DEPLOYMENT_KEY = "hotDeployment";
 
     private static final String WATCH_STATUS_KEY = "watchStatus";
 
     private static final String LIBRARY_PATH_KEY = "libraryPaths";
 
     // Persistence provider property keys
     private static final String PERSISTENCE_CONFIG_KEY = "persistenceConfig";
 
     private static final String SCAN_FOR_ENTITIES_KEY = "scanForEntities";
 
     private static final String ANNOTATED_UNIT_NAME_KEY = "annotatedUnitName";
 
     private static final String PERSISTENCE_XML_PATH_KEY = "persistanceXmlPath";
 
     private static final String PERSISTENCE_XML_FROM_JAR_KEY = "persistenceXmlFromJar";
 
     private static final String SWAP_DATASOURCE_KEY = "swapDataSource";
 
     private static final String SCAN_ARCHIVES_KEY = "scanArchives";
 
     private static final String POOLED_DATA_SOURCE_KEY = "pooledDataSource";
 
     private static final String PERSISTENCE_PROPERTIES_KEY = "persistenceProperties";
 
     // Connection pool provider property keys
    private static final String POOL_CONFIG_KEY = "poolConfg";
 
     private static final String POOL_PROPERTIES_PATH_KEY = "poolPropertiesPath";
 
     private static final String POOL_PROVIDER_TYPE_KEY = "poolProviderType";
 
     private static final String POOL_PROPERTIES_KEY = "poolProperties";
 
     // Configuration properties for deployment
     private static String ADMIN_USERS_PATH;
 
     // Is configuration server or client (default is server)
     private static boolean server = SERVER_DEF;
 
     private static boolean remote;
 
     // Instance of pool configuration
     private static final PoolConfig POOL_CONFIG = new PoolConfig();
 
     private static final Logger LOG = Logger.getLogger(Configuration.class);
 
     public Configuration() {
     }
 
     @SuppressWarnings("unchecked")
     private <K, V> Map<K, V> getAsMap(Object key, Map<Object, Object> from) {
 
 	if (from == null) {
 	    from = config;
 	}
 	Map<K, V> value = (Map<K, V>) ObjectUtils.getAsMap(key, from);
 
 	return value;
     }
 
     private <K, V> Map<K, V> getAsMap(Object key) {
 
 	return getAsMap(key, null);
     }
 
     private <K, V> void setSubConfigValue(Object key, K subKey, V value) {
 
 	Map<K, V> subConfig = getAsMap(key);
 	if (subConfig == null) {
 	    subConfig = new HashMap<K, V>();
 	    config.put(key, subConfig);
 	}
 
 	subConfig.put(subKey, value);
     }
 
     private <K, V> V getSubConfigValue(Object key, K subKey, V defaultValue) {
 
 	V def;
 	Map<K, V> subConfig = getAsMap(key);
 	if (ObjectUtils.available(subConfig)) {
 	    def = subConfig.get(subKey);
 	    if (def == null) {
 		def = defaultValue;
 	    }
 	} else {
 	    def = defaultValue;
 	}
 
 	return def;
     }
 
     private <K> boolean containsSubConfigKey(Object key, K subKey) {
 
 	Map<K, ?> subConfig = getAsMap(key);
 	boolean valid = ObjectUtils.available(subConfig);
 	if (valid) {
 	    valid = subConfig.containsKey(subKey);
 	}
 
 	return valid;
     }
 
     private <K> boolean containsConfigKey(K key) {
 
 	return containsSubConfigKey(DEPLOY_CONFIG_KEY, key);
     }
 
     private <K, V> V getSubConfigValue(Object key, K subKey) {
 
 	return getSubConfigValue(key, subKey, null);
     }
 
     private <K, V> void setConfigValue(K subKey, V value) {
 
 	setSubConfigValue(DEPLOY_CONFIG_KEY, subKey, value);
     }
 
     private <K, V> V getConfigValue(K subKey, V defaultValue) {
 
 	return getSubConfigValue(DEPLOY_CONFIG_KEY, subKey, defaultValue);
     }
 
     private <K, V> V getConfigValue(K subKey) {
 
 	return getSubConfigValue(DEPLOY_CONFIG_KEY, subKey);
     }
 
     private <K, V> Map<K, V> getWithInitialization(Object key) {
 
 	Map<K, V> result = getConfigValue(key);
 	if (result == null) {
 	    result = new HashMap<K, V>();
 	    setConfigValue(key, result);
 	}
 
 	return result;
     }
 
     private <K, V> void setWithInitialization(Object key, K subKey, V value) {
 
 	Map<K, V> result = getWithInitialization(key);
 
 	result.put(subKey, value);
     }
 
     public <V> V getPersistenceConfigValue(Object key, V defaultValue) {
 
 	V value = ObjectUtils.getSubValue(config, DEPLOY_CONFIG_KEY,
 		PERSISTENCE_CONFIG_KEY, key);
 	if (value == null) {
 	    value = defaultValue;
 	}
 
 	return value;
     }
 
     public <V> V getPersistenceConfigValue(Object key) {
 
 	return getPersistenceConfigValue(key, null);
     }
 
     public void setPersistenceConfigValue(Object key, Object value) {
 
 	setWithInitialization(PERSISTENCE_CONFIG_KEY, key, value);
     }
 
     public <V> V getPoolConfigValue(Object key, V defaultValue) {
 
 	V value = ObjectUtils.getSubValue(config, DEPLOY_CONFIG_KEY,
 		POOL_CONFIG_KEY, key);
 	if (value == null) {
 	    value = defaultValue;
 	}
 
 	return value;
     }
 
     public <V> V getPoolConfigValue(Object key) {
 
 	V value = getPoolConfigValue(key, null);
 
 	return value;
     }
 
     public void setPoolConfigValue(Object key, Object value) {
 
 	setWithInitialization(POOL_CONFIG_KEY, key, value);
     }
 
     /**
      * Configuration for {@link PoolConfig} instance
      */
     private void configurePool() {
 
 	Map<Object, Object> poolProperties = getPoolConfigValue(POOL_PROPERTIES_KEY);
 	if (ObjectUtils.available(poolProperties)) {
 
 	    setPoolProperties(poolProperties);
 	}
 
 	String type = getPoolConfigValue(POOL_PROVIDER_TYPE_KEY);
 	if (ObjectUtils.available(type)) {
 	    getPoolConfig().setPoolProviderType(type);
 	}
 
 	String path = getPoolConfigValue(POOL_PROPERTIES_PATH_KEY);
 	if (ObjectUtils.available(path)) {
 	    setPoolPropertiesPath(path);
 	}
     }
 
     /**
      * Configures server from properties
      */
     private void configureServer() {
 
 	// Sets default values to remote server configuration
 	boolean contains = containsConfigKey(IP_ADDRESS_KEY);
 	if (ObjectUtils.notTrue(contains)) {
 	    setConfigValue(IP_ADDRESS_KEY, IP_ADDRESS_DEF);
 	}
 
 	contains = containsConfigKey(PORT_KEY);
 	if (ObjectUtils.notTrue(contains)) {
 	    setConfigValue(PORT_KEY, PORT_DEF);
 	}
 
 	contains = containsConfigKey(BOSS_POOL_KEY);
 	if (ObjectUtils.notTrue(contains)) {
 	    setConfigValue(BOSS_POOL_KEY, BOSS_POOL_DEF);
 	}
 
 	contains = containsConfigKey(WORKER_POOL_KEY);
 	if (ObjectUtils.notTrue(contains)) {
 
 	    int workers = RUNTIME.availableProcessors() * WORKER_POOL_DEF;
 	    String workerProperty = String.valueOf(workers);
 	    setConfigValue(WORKER_POOL_KEY, workerProperty);
 	}
 
 	contains = containsConfigKey(CONNECTION_TIMEOUT_KEY);
 	if (ObjectUtils.notTrue(contains)) {
 	    setConfigValue(CONNECTION_TIMEOUT_KEY, CONNECTION_TIMEOUT_DEF);
 	}
 
 	// Sets default values is application on server or client mode
 	Object serverValue = getConfigValue(SERVER_KEY);
 	if (ObjectUtils.notNull(serverValue)) {
 	    if (serverValue instanceof Boolean) {
 		server = (Boolean) serverValue;
 	    } else {
 		server = Boolean.valueOf(serverValue.toString());
 	    }
 	}
 
 	Object remoteValue = getConfigValue(REMOTE_KEY);
 	if (ObjectUtils.notNull(remoteValue)) {
 	    if (remoteValue instanceof Boolean) {
 		remote = (Boolean) remoteValue;
 	    } else {
 		remote = Boolean.valueOf(remoteValue.toString());
 	    }
 	}
     }
 
     /**
      * Merges configuration with default properties
      */
     public void configureDeployments() {
 
 	// Sets administrator user configuration file path
 	ADMIN_USERS_PATH = getConfigValue(ADMIN_USER_PATH_KEY,
 		ADMIN_USERS_PATH_DEF);
 
 	// Checks if application run in hot deployment mode
 	Boolean hotDeployment = getConfigValue(HOT_DEPLOYMENT_KEY);
 	if (hotDeployment == null) {
 	    setConfigValue(HOT_DEPLOYMENT_KEY, Boolean.FALSE);
 	    hotDeployment = getConfigValue(HOT_DEPLOYMENT_KEY);
 	}
 
 	// Check if application needs directory watch service
 	boolean watchStatus;
 	if (ObjectUtils.notTrue(hotDeployment)) {
 	    watchStatus = Boolean.TRUE;
 	} else {
 	    watchStatus = Boolean.FALSE;
 	}
 
 	setConfigValue(WATCH_STATUS_KEY, watchStatus);
 
 	// Sets deployments directories
 	Set<DeploymentDirectory> deploymentPaths = getConfigValue(DEMPLOYMENT_PATH_KEY);
 	if (deploymentPaths == null) {
 	    deploymentPaths = DEPLOYMENT_PATHS_DEF;
 	    setConfigValue(DEMPLOYMENT_PATH_KEY, deploymentPaths);
 	}
     }
 
     /**
      * Configures server and connection pooling
      */
     public void configure() {
 
 	configureServer();
 	configureDeployments();
 	configurePool();
     }
 
     /**
      * Merges two {@link Map}s and if second {@link Map}'s value is instance of
      * {@link Map} merges this value with first {@link Map}'s value recursively
      * 
      * @param map1
      * @param map2
      * @return {@link Map}<Object, Object>
      */
     @SuppressWarnings("unchecked")
     protected Map<Object, Object> deepMerge(Map<Object, Object> map1,
 	    Map<Object, Object> map2) {
 
 	if (map1 == null) {
 	    map1 = map2;
 	} else {
 	    Set<Map.Entry<Object, Object>> entries2 = map2.entrySet();
 	    Object key;
 	    Map<Object, Object> value1;
 	    Object value2;
 	    Object mergedValue;
 	    for (Map.Entry<Object, Object> entry2 : entries2) {
 		key = entry2.getKey();
 		value2 = entry2.getValue();
 		if (value2 instanceof Map) {
 		    value1 = ObjectUtils.getAsMap(key, map1);
 		    mergedValue = deepMerge(value1,
 			    (Map<Object, Object>) value2);
 		} else {
 		    mergedValue = value2;
 		}
 
 		if (ObjectUtils.notNull(mergedValue)) {
 		    map1.put(key, mergedValue);
 		}
 	    }
 	}
 
 	return map1;
     }
 
     /**
      * Reads configuration from passed properties
      * 
      * @param configuration
      */
     public void configure(Map<Object, Object> configuration) {
 
 	deepMerge(config, configuration);
     }
 
     /**
      * Reads configuration from passed file path
      * 
      * @param configuration
      */
     @SuppressWarnings("unchecked")
     public void configure(String path) throws IOException {
 
 	File yamlFile = new File(path);
 	if (yamlFile.exists()) {
 	    InputStream stream = new FileInputStream(yamlFile);
 	    try {
 		Yaml yaml = new Yaml();
 		Object configuration = yaml.load(stream);
 		if (configuration instanceof Map) {
 		    configure((Map<Object, Object>) configuration);
 		}
 	    } finally {
 		stream.close();
 	    }
 	}
     }
 
     /**
      * Gets value associated with particular key as {@link String} instance
      * 
      * @param key
      * @return {@link String}
      */
     public String getStringValue(String key) {
 
 	Object value = config.get(key);
 	String textValue;
 	if (value == null) {
 	    textValue = null;
 	} else {
 	    textValue = value.toString();
 	}
 
 	return textValue;
     }
 
     /**
      * Gets value associated with particular key as <code>int</code> instance
      * 
      * @param key
      * @return {@link String}
      */
     public int getIntValue(String key) {
 
 	String value = getStringValue(key);
 
 	return Integer.parseInt(value);
     }
 
     /**
      * Gets value associated with particular key as <code>long</code> instance
      * 
      * @param key
      * @return {@link String}
      */
     public long getLongValue(String key) {
 
 	String value = getStringValue(key);
 
 	return Long.parseLong(value);
     }
 
     /**
      * Gets value associated with particular key as <code>boolean</code>
      * instance
      * 
      * @param key
      * @return {@link String}
      */
     public boolean getBooleanValue(String key) {
 
 	String value = getStringValue(key);
 
 	return Boolean.parseBoolean(value);
     }
 
     public void putValue(String key, String value) {
 	config.put(key, value);
     }
 
     /**
      * Loads configuration form file
      * 
      * @throws IOException
      */
     public void loadFromFile() throws IOException {
 
 	InputStream propertiesStream = null;
 	try {
 	    File configFile = new File(CONFIG_FILE);
 	    if (configFile.exists()) {
 		propertiesStream = new FileInputStream(configFile);
 		loadFromStream(propertiesStream);
 	    } else {
 		configFile.mkdirs();
 	    }
 	} catch (IOException ex) {
 	    LOG.error("Could not open config file", ex);
 	} finally {
 	    if (ObjectUtils.notNull(propertiesStream)) {
 		propertiesStream.close();
 	    }
 	}
 
     }
 
     /**
      * Loads configuration form file by passed file path
      * 
      * @param configFilename
      * @throws IOException
      */
     public void loadFromFile(String configFilename) throws IOException {
 
 	InputStream propertiesStream = null;
 	try {
 	    propertiesStream = new FileInputStream(new File(configFilename));
 	    loadFromStream(propertiesStream);
 	} catch (IOException ex) {
 	    LOG.error("Could not open config file", ex);
 	} finally {
 	    if (ObjectUtils.notNull(propertiesStream)) {
 		propertiesStream.close();
 	    }
 	}
 
     }
 
     /**
      * Loads configuration from file contained in classpath
      * 
      * @param resourceName
      * @param loader
      */
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
 
     public static String getAdminUsersPath() {
 	return ADMIN_USERS_PATH;
     }
 
     public static void setAdminUsersPath(String aDMIN_USERS_PATH) {
 	ADMIN_USERS_PATH = aDMIN_USERS_PATH;
     }
 
     public boolean isRemote() {
 
 	return remote;
     }
 
     public void setRemote(boolean remoteValue) {
 	remote = remoteValue;
     }
 
     public static boolean isServer() {
 
 	return server;
     }
 
     public static void setServer(boolean serverValue) {
 
 	server = serverValue;
     }
 
     public boolean isClient() {
 
 	return getConfigValue(CLIENT_KEY, Boolean.FALSE);
     }
 
     public void setClient(boolean client) {
 	setConfigValue(CLIENT_KEY, client);
     }
 
     /**
      * Adds path for deployments file or directory
      * 
      * @param path
      * @param scan
      */
     public void addDeploymentPath(String path, boolean scan) {
 
 	Set<DeploymentDirectory> deploymentPaths = getConfigValue(DEMPLOYMENT_PATH_KEY);
 	if (deploymentPaths == null) {
 	    deploymentPaths = new HashSet<DeploymentDirectory>();
 	    setConfigValue(DEMPLOYMENT_PATH_KEY, deploymentPaths);
 	}
 
 	deploymentPaths.add(new DeploymentDirectory(path, scan));
     }
 
     /**
      * Adds path for data source file
      * 
      * @param path
      */
     public void addDataSourcePath(String path) {
 
 	Set<String> dataSourcePaths = getConfigValue(DATA_SOURCE_PATH_KEY);
 	if (dataSourcePaths == null) {
 	    dataSourcePaths = new HashSet<String>();
 	    setConfigValue(DATA_SOURCE_PATH_KEY, dataSourcePaths);
 	}
 
 	dataSourcePaths.add(path);
     }
 
     public Set<DeploymentDirectory> getDeploymentPath() {
 
 	return getConfigValue(DEMPLOYMENT_PATH_KEY);
     }
 
     public Set<String> getDataSourcePath() {
 
 	return getConfigValue(DATA_SOURCE_PATH_KEY);
     }
 
     public String[] getLibraryPaths() {
 	return getConfigValue(LIBRARY_PATH_KEY);
     }
 
     public void setLibraryPaths(String[] libraryPaths) {
 	setConfigValue(LIBRARY_PATH_KEY, libraryPaths);
     }
 
     public boolean isHotDeployment() {
 	return getConfigValue(HOT_DEPLOYMENT_KEY, Boolean.FALSE);
     }
 
     public void setHotDeployment(boolean hotDeployment) {
 	setConfigValue(HOT_DEPLOYMENT_KEY, hotDeployment);
     }
 
     public boolean isWatchStatus() {
 	return getConfigValue(WATCH_STATUS_KEY, Boolean.FALSE);
     }
 
     public void setWatchStatus(boolean watchStatus) {
 	setConfigValue(WATCH_STATUS_KEY, watchStatus);
     }
 
     // Persistence configuration
     public boolean isScanForEntities() {
 	return getPersistenceConfigValue(SCAN_FOR_ENTITIES_KEY, Boolean.FALSE);
     }
 
     public void setScanForEntities(boolean scanForEntities) {
 
 	setPersistenceConfigValue(SCAN_FOR_ENTITIES_KEY, scanForEntities);
     }
 
     public String getAnnotatedUnitName() {
 	return getPersistenceConfigValue(ANNOTATED_UNIT_NAME_KEY);
     }
 
     public void setAnnotatedUnitName(String annotatedUnitName) {
 	setPersistenceConfigValue(ANNOTATED_UNIT_NAME_KEY, annotatedUnitName);
     }
 
     public String getPersXmlPath() {
 	return getPersistenceConfigValue(PERSISTENCE_XML_PATH_KEY);
     }
 
     public void setPersXmlPath(String persXmlPath) {
 	setPersistenceConfigValue(PERSISTENCE_XML_PATH_KEY, persXmlPath);
     }
 
     public boolean isPersXmlFromJar() {
 	return getPersistenceConfigValue(PERSISTENCE_XML_FROM_JAR_KEY,
 		Boolean.FALSE);
     }
 
     public void setPersXmlFromJar(boolean persXmlFromJar) {
 	setPersistenceConfigValue(PERSISTENCE_XML_FROM_JAR_KEY, persXmlFromJar);
     }
 
     public boolean isSwapDataSource() {
 	return getPersistenceConfigValue(SWAP_DATASOURCE_KEY, Boolean.FALSE);
     }
 
     public void setSwapDataSource(boolean swapDataSource) {
 	setPersistenceConfigValue(SWAP_DATASOURCE_KEY, swapDataSource);
     }
 
     public boolean isScanArchives() {
 	return getPersistenceConfigValue(SCAN_ARCHIVES_KEY, Boolean.FALSE);
     }
 
     public void setScanArchives(boolean scanArchives) {
 	setPersistenceConfigValue(SCAN_ARCHIVES_KEY, scanArchives);
     }
 
     public boolean isPooledDataSource() {
 	return getPersistenceConfigValue(POOLED_DATA_SOURCE_KEY, Boolean.FALSE);
     }
 
     public void setPooledDataSource(boolean pooledDataSource) {
 	setPersistenceConfigValue(POOLED_DATA_SOURCE_KEY, pooledDataSource);
     }
 
     public Map<Object, Object> getPersistenceProperties() {
 	return getPersistenceConfigValue(PERSISTENCE_PROPERTIES_KEY);
     }
 
     public void setPersistenceProperties(
 	    Map<Object, Object> persistenceProperties) {
 	setPersistenceConfigValue(PERSISTENCE_PROPERTIES_KEY,
 		persistenceProperties);
     }
 
     // Pool configuration
     public static PoolConfig getPoolConfig() {
 
 	return POOL_CONFIG;
     }
 
     public void setDataSourcePooledType(boolean dsPooledType) {
 
 	PoolConfig poolConfig = getPoolConfig();
 	poolConfig.setPooledDataSource(dsPooledType);
     }
 
     public void setPoolPropertiesPath(String path) {
 
 	PoolConfig poolConfig = getPoolConfig();
 	poolConfig.setPoolPath(path);
     }
 
     public void setPoolProperties(
 	    Map<? extends Object, ? extends Object> properties) {
 
 	PoolConfig poolConfig = getPoolConfig();
 	poolConfig.getPoolProperties().putAll(properties);
     }
 
     public void addPoolProperty(Object key, Object value) {
 
 	PoolConfig poolConfig = getPoolConfig();
 	poolConfig.getPoolProperties().put(key, value);
     }
 
     public void setPoolProviderType(PoolProviderType poolProviderType) {
 
 	PoolConfig poolConfig = getPoolConfig();
 	poolConfig.setPoolProviderType(poolProviderType);
     }
 
     @Override
     public Object clone() throws CloneNotSupportedException {
 
 	Configuration cloneConfig = (Configuration) super.clone();
 	cloneConfig.config.clear();
 	cloneConfig.configure(this.config);
 
 	return cloneConfig;
     }
 }
