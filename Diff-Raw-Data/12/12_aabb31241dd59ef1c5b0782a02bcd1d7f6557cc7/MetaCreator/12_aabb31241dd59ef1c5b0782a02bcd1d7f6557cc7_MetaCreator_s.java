 package org.lightmare.deploy;
 
 import static org.lightmare.cache.MetaContainer.closeConnections;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import javax.ejb.Local;
 import javax.ejb.Remote;
 import javax.ejb.Stateless;
 import javax.persistence.Entity;
 
 import org.apache.log4j.Logger;
 import org.lightmare.annotations.UnitName;
 import org.lightmare.cache.ArchiveData;
 import org.lightmare.cache.DeployData;
 import org.lightmare.cache.MetaData;
 import org.lightmare.cache.TmpResources;
 import org.lightmare.config.Configuration;
 import org.lightmare.deploy.fs.Watcher;
 import org.lightmare.jpa.JPAManager;
 import org.lightmare.jpa.datasource.DataSourceInitializer;
 import org.lightmare.jpa.datasource.PoolConfig;
 import org.lightmare.jpa.datasource.PoolConfig.PoolProviderType;
 import org.lightmare.libraries.LibraryLoader;
 import org.lightmare.remote.rpc.RpcListener;
 import org.lightmare.scannotation.AnnotationDB;
 import org.lightmare.utils.AbstractIOUtils;
 import org.lightmare.utils.ObjectUtils;
 import org.lightmare.utils.reflect.MetaUtils;
 import org.lightmare.utils.shutdown.ShutDown;
 
 /**
  * Determines and saves in cache ejb beans {@link MetaData} on startup
  * 
  * @author Levan
  * 
  */
 public class MetaCreator {
 
     private static AnnotationDB annotationDB;
 
     private Map<Object, Object> prop;
 
     private boolean scanForEntities;
 
     private String annotatedUnitName;
 
     private String persXmlPath;
 
     private String[] libraryPaths;
 
     private boolean persXmlFromJar;
 
     private boolean swapDataSource;
 
     private String dataSourcePath;
 
     private boolean scanArchives;
 
     private TmpResources tmpResources;
 
     private boolean await;
 
     private CountDownLatch conn;
 
     /**
      * {@link Configuration} container class for server
      */
     public static final Configuration CONFIG = new Configuration();
 
     // Data for cache at deploy time
     private Map<String, AbstractIOUtils> aggregateds = new HashMap<String, AbstractIOUtils>();
 
     private Map<URL, ArchiveData> archivesURLs;
 
     private Map<String, URL> classOwnersURL;
 
     private Map<URL, DeployData> realURL;
 
     private boolean hotDeployment;
 
     private boolean watchStatus;
 
     private static final Logger LOG = Logger.getLogger(MetaCreator.class);
 
     private MetaCreator() {
 	tmpResources = new TmpResources();
 	ShutDown.setHook(tmpResources);
     }
 
     public AnnotationDB getAnnotationDB() {
 	return annotationDB;
     }
 
     /**
      * Checks weather {@link javax.persistence.Entity} annotated classes is need
      * to be filtered by {@link org.lightmare.annotations.UnitName} value
      * 
      * @param className
      * @return boolean
      * @throws IOException
      */
     private boolean checkForUnitName(String className) throws IOException {
 	boolean isValid = Boolean.FALSE;
 	Class<?> entityClass;
 	entityClass = MetaUtils.initClassForName(className);
 	UnitName annotation = entityClass.getAnnotation(UnitName.class);
 	isValid = annotation.value().equals(annotatedUnitName);
 
 	return isValid;
     }
 
     private List<String> translateToList(Set<String> classSet) {
 
 	String[] classArray = new String[classSet.size()];
 	classArray = ObjectUtils.toArray(classSet, String.class);
 	List<String> classList = Arrays.asList(classArray);
 
 	return classList;
     }
 
     /**
      * Defines belonginess of {@link javax.persistence.Entity} annotated classes
      * to jar file
      * 
      * @param classSet
      * @return {@link List}<String>
      */
     private void filterEntitiesForJar(Set<String> classSet,
 	    String fileNameForBean) {
 
 	Map<String, String> classOwnersFiles = annotationDB
 		.getClassOwnersFiles();
 
 	String fileNameForEntity;
 	for (String entityName : classSet) {
 	    fileNameForEntity = classOwnersFiles.get(entityName);
 	    if (ObjectUtils.notNull(fileNameForEntity)
 		    && ObjectUtils.notNull(fileNameForBean)
 		    && !fileNameForEntity.equals(fileNameForBean)) {
 		classSet.remove(entityName);
 	    }
 	}
     }
 
     /**
      * Filters {@link javax.persistence.Entity} annotated classes by name or by
      * {@link org.lightmare.annotations.UnitName} by configuration
      * 
      * @param classSet
      * @return {@link List}<String>
      * @throws IOException
      */
     private List<String> filterEntities(Set<String> classSet)
 	    throws IOException {
 	List<String> classes;
 	if (annotatedUnitName == null) {
 	    classes = translateToList(classSet);
 	} else {
 	    Set<String> filtereds = new HashSet<String>();
 	    for (String className : classSet) {
 		if (checkForUnitName(className)) {
 		    filtereds.add(className);
 		}
 	    }
 	    classes = translateToList(filtereds);
 	}
 
 	return classes;
     }
 
     /**
      * Creates connection associated with unit name if such does not exists
      * 
      * @param unitName
      * @param beanName
      * @throws IOException
      */
     protected void configureConnection(String unitName, String beanName)
 	    throws IOException {
 
 	JPAManager.Builder builder = new JPAManager.Builder();
 	Map<String, String> classOwnersFiles = annotationDB
 		.getClassOwnersFiles();
 	AbstractIOUtils ioUtils = aggregateds.get(beanName);
 
 	if (ObjectUtils.notNull(ioUtils)) {
 	    URL jarURL = ioUtils.getAppropriatedURL(classOwnersFiles, beanName);
 	    builder.setURL(jarURL);
 	}
 	if (scanForEntities) {
 	    Set<String> classSet;
 	    Map<String, Set<String>> annotationIndex = annotationDB
 		    .getAnnotationIndex();
 	    classSet = annotationIndex.get(Entity.class.getName());
 	    if (annotatedUnitName == null) {
 		classSet = annotationIndex.get(Entity.class.getName());
 	    } else if (annotatedUnitName.equals(unitName)) {
 		Set<String> unitNamedSet = annotationIndex.get(UnitName.class
 			.getName());
 		classSet.retainAll(unitNamedSet);
 	    }
 	    if (ObjectUtils.notNull(ioUtils)) {
 		String fileNameForBean = classOwnersFiles.get(beanName);
 		filterEntitiesForJar(classSet, fileNameForBean);
 	    }
 	    List<String> classes = filterEntities(classSet);
 	    builder.setClasses(classes);
 	}
 	builder.setPath(persXmlPath).setProperties(prop)
 		.setSwapDataSource(swapDataSource)
 		.setScanArchives(scanArchives).build().setConnection(unitName);
     }
 
     /**
      * Caches each archive by it's {@link URL} for deployment
      * 
      * @param ejbURLs
      * @param archiveData
      */
     private void fillArchiveURLs(Collection<URL> ejbURLs,
 	    ArchiveData archiveData, DeployData deployData) {
 
 	for (URL ejbURL : ejbURLs) {
 	    archivesURLs.put(ejbURL, archiveData);
 	    realURL.put(ejbURL, deployData);
 	}
     }
 
     /**
      * Caches each archive by it's {@link URL} for deployment and creates fill
      * {@link URL} array for scanning and finding {@link javax.ejb.Stateless}
      * annotated classes
      * 
      * @param archive
      * @param modifiedArchives
      * @throws IOException
      */
     private void fillArchiveURLs(URL archive, List<URL> modifiedArchives)
 	    throws IOException {
 
 	AbstractIOUtils ioUtils = AbstractIOUtils.getAppropriatedType(archive);
 	if (ObjectUtils.notNull(ioUtils)) {
 	    ioUtils.scan(persXmlFromJar);
 	    List<URL> ejbURLs = ioUtils.getEjbURLs();
 	    modifiedArchives.addAll(ejbURLs);
 	    ArchiveData archiveData = new ArchiveData();
 	    archiveData.setIoUtils(ioUtils);
 	    DeployData deployData = new DeployData();
 	    deployData.setType(ioUtils.getType());
 	    deployData.setUrl(archive);
 	    if (ejbURLs.isEmpty()) {
 		archivesURLs.put(archive, archiveData);
 		realURL.put(archive, deployData);
 	    } else {
 		fillArchiveURLs(ejbURLs, archiveData, deployData);
 	    }
 	}
     }
 
     /**
      * Gets {@link URL} array for all classes and jar libraries within archive
      * file for class loading policy
      * 
      * @param archives
      * @return {@link URL}[]
      * @throws IOException
      */
     private URL[] getFullArchives(URL[] archives) throws IOException {
 
 	List<URL> modifiedArchives = new ArrayList<URL>();
 	for (URL archive : archives) {
 	    fillArchiveURLs(archive, modifiedArchives);
 	}
 
 	return ObjectUtils.toArray(modifiedArchives, URL.class);
     }
 
     /**
      * Awaits for {@link Future} tasks if it set so by configuration
      * 
      * @param future
      */
     private void awaitDeployment(Future<String> future) {
 
 	if (await) {
 	    try {
 		String nameFromFuture = future.get();
 		LOG.info(String.format("Deploy processing of %s finished",
 			nameFromFuture));
 	    } catch (InterruptedException ex) {
 		LOG.error(ex.getMessage(), ex);
 	    } catch (ExecutionException ex) {
 		LOG.error(ex.getMessage(), ex);
 	    }
 	}
     }
 
     /**
      * Awaits for {@link CountDownLatch} of deployments
      */
     private void awaitDeployments() {
 	try {
 	    conn.await();
 	} catch (InterruptedException ex) {
 	    LOG.error(ex);
 	}
     }
 
     /**
      * Starts bean deployment process for bean name
      * 
      * @param beanName
      * @throws IOException
      */
     private void deployBean(String beanName) throws IOException {
 	URL currentURL = classOwnersURL.get(beanName);
 	ArchiveData archiveData = archivesURLs.get(currentURL);
 	if (archiveData == null) {
 	    archiveData = new ArchiveData();
 	}
 	AbstractIOUtils ioUtils = archiveData.getIoUtils();
 	if (ioUtils == null) {
 	    ioUtils = AbstractIOUtils.getAppropriatedType(currentURL);
 	    archiveData.setIoUtils(ioUtils);
 	}
 	ClassLoader loader = archiveData.getLoader();
 
 	// Finds appropriated ClassLoader if needed and or creates new one
 	List<File> tmpFiles = null;
 
 	if (ObjectUtils.notNull(ioUtils)) {
 	    if (loader == null) {
 		if (!ioUtils.isExecuted()) {
 		    ioUtils.scan(persXmlFromJar);
 		}
 		URL[] libURLs = ioUtils.getURLs();
 		loader = LibraryLoader.getEnrichedLoader(libURLs);
 		archiveData.setLoader(loader);
 	    }
 	    tmpFiles = ioUtils.getTmpFiles();
 	    aggregateds.put(beanName, ioUtils);
 	}
 
 	// Archive file url which contains this bean
 	DeployData deployData;
 	if (ObjectUtils.available(realURL)) {
 	    deployData = realURL.get(currentURL);
 	} else {
 	    deployData = null;
 	}
 	// Initializes and fills BeanLoader.BeanParameters class to deploy
 	// stateless ejb bean
 	BeanLoader.BeanParameters parameters = new BeanLoader.BeanParameters();
 	parameters.creator = this;
 	parameters.className = beanName;
 	parameters.loader = loader;
 	parameters.tmpFiles = tmpFiles;
 	parameters.conn = conn;
 	parameters.deployData = deployData;
 
 	Future<String> future = BeanLoader.loadBean(parameters);
 	awaitDeployment(future);
 	if (ObjectUtils.available(tmpFiles)) {
 	    tmpResources.addFile(tmpFiles);
 	}
     }
 
     /**
      * Deploys single bean by class name
      * 
      * @param beanNames
      */
     private void deployBeans(Set<String> beanNames) {
 	conn = new CountDownLatch(beanNames.size());
 	for (String beanName : beanNames) {
 	    LOG.info(String.format("deploing bean %s", beanName));
 	    try {
 		deployBean(beanName);
 	    } catch (IOException ex) {
 		LOG.error(String.format("Could not deploy bean %s", beanName),
 			ex);
 	    }
 	}
 	awaitDeployments();
 	if (hotDeployment && !watchStatus) {
 	    Watcher.startWatch(this);
 	    watchStatus = true;
 	}
     }
 
     /**
      * Scan application for find all {@link javax.ejb.Stateless} beans and
      * {@link Remote} or {@link Local} proxy interfaces
      * 
      * @param archives
      * @throws IOException
      * @throws ClassNotFoundException
      */
     public void scanForBeans(URL[] archives) throws IOException {
 
 	synchronized (this) {
 	    try {
 		// starts RPC server if configured as remote and server
 		if (CONFIG.isRemote() && CONFIG.isServer()) {
 		    RpcListener.startServer();
 		}
 		// Loads libraries from specified path
 		if (ObjectUtils.notNull(libraryPaths)) {
 		    LibraryLoader.loadLibraries(libraryPaths);
 		}
 		archivesURLs = new HashMap<URL, ArchiveData>();
 		if (ObjectUtils.available(archives)) {
 		    realURL = new HashMap<URL, DeployData>();
 		}
 		URL[] fullArchives = getFullArchives(archives);
 		annotationDB = new AnnotationDB();
 		annotationDB.setScanFieldAnnotations(Boolean.FALSE);
 		annotationDB.setScanParameterAnnotations(Boolean.FALSE);
 		annotationDB.setScanMethodAnnotations(Boolean.FALSE);
 		annotationDB.scanArchives(fullArchives);
 		Set<String> beanNames = annotationDB.getAnnotationIndex().get(
 			Stateless.class.getName());
 		classOwnersURL = annotationDB.getClassOwnersURLs();
 		DataSourceInitializer.initializeDataSource(dataSourcePath);
 		if (ObjectUtils.available(beanNames)) {
 		    deployBeans(beanNames);
 		}
 	    } finally {
 		// gets rid from all created temporary files
 		tmpResources.removeTempFiles();
 	    }
 	}
     }
 
     /**
      * Scan application for find all {@link javax.ejb.Stateless} beans and
      * {@link Remote} or {@link Local} proxy interfaces
      * 
      * @throws ClassNotFoundException
      * @throws IOException
      */
     public void scanForBeans(File[] jars) throws IOException {
 	List<URL> urlList = new ArrayList<URL>();
 	URL url;
 	for (File file : jars) {
 	    url = file.toURI().toURL();
 	    urlList.add(url);
 	}
 	URL[] archives = ObjectUtils.toArray(urlList, URL.class);
 	scanForBeans(archives);
     }
 
     /**
      * Scan application for find all {@link javax.ejb.Stateless} beans and
      * {@link Remote} or {@link Local} proxy interfaces
      * 
      * @throws ClassNotFoundException
      * @throws IOException
      */
     public void scanForBeans(String... paths) throws IOException {
 
 	if (ObjectUtils.notAvailable(paths)
 		&& ObjectUtils.available(CONFIG.getDeploymentPath())) {
 
 	    Set<String> deployments = CONFIG.getDeploymentPath();
 	    List<String> pathList = new ArrayList<String>();
 	    File deployFile;
 	    for (String deployment : deployments) {
 		deployFile = new File(deployment);
 		String[] subDeployments = deployFile.list();
 		if (ObjectUtils.available(subDeployments)) {
 		    pathList.addAll(Arrays.asList(subDeployments));
 		}
 	    }
 	    paths = ObjectUtils.toArray(pathList, String.class);
 	}
 	List<URL> urlList = new ArrayList<URL>();
 	URL archive;
 	File file;
 	for (String path : paths) {
 	    file = new File(path);
 	    archive = file.toURI().toURL();
 	    urlList.add(archive);
 	}
 	URL[] archives = ObjectUtils.toArray(urlList, URL.class);
 	scanForBeans(archives);
     }
 
     /**
      * Closes all existing connections
      */
     public static void closeAllConnections() {
 	closeConnections();
     }
 
     public void clear() {
 
 	if (ObjectUtils.available(realURL)) {
 	    realURL.clear();
 	    realURL = null;
 	}
 
 	if (ObjectUtils.available(aggregateds)) {
 	    aggregateds.clear();
 	}
 
 	if (ObjectUtils.available(archivesURLs)) {
 	    archivesURLs.clear();
 	    archivesURLs = null;
 	}
 
 	if (ObjectUtils.available(classOwnersURL)) {
 	    classOwnersURL.clear();
 	    classOwnersURL = null;
 	}
     }
 
     /**
      * Builder class to provide properties for lightmare
      * 
      * @author levan
      * 
      */
     public static class Builder {
 
 	private MetaCreator creator;
 
 	public Builder() {
 	    creator = new MetaCreator();
 	}
 
 	private void initPoolProperties() {
 
 	    if (ObjectUtils.notAvailable(PoolConfig.poolProperties)) {
 		PoolConfig.poolProperties = new HashMap<Object, Object>();
 	    }
 	}
 
 	public Builder setPersistenceProperties(Map<String, String> properties) {
 	    creator.prop = new HashMap<Object, Object>();
 	    creator.prop.putAll(properties);
 	    return this;
 	}
 
 	public Builder setScanForEntities(boolean scanForEnt) {
 	    creator.scanForEntities = scanForEnt;
 	    return this;
 	}
 
 	public Builder setUnitName(String unitName) {
 	    creator.annotatedUnitName = unitName;
 	    return this;
 	}
 
 	public Builder setPersXmlPath(String path) {
 	    creator.persXmlPath = path;
 	    creator.scanArchives = Boolean.FALSE;
 	    return this;
 	}
 
 	public Builder setLibraryPath(String... libPaths) {
 	    creator.libraryPaths = libPaths;
 	    return this;
 	}
 
 	public Builder setXmlFromJar(boolean xmlFromJar) {
 	    creator.persXmlFromJar = xmlFromJar;
 	    return this;
 	}
 
 	public Builder setSwapDataSource(boolean swapDataSource) {
 	    creator.swapDataSource = swapDataSource;
 	    return this;
 	}
 
 	public Builder setDataSourcePath(String dataSourcePath) {
 	    creator.dataSourcePath = dataSourcePath;
 	    CONFIG.putValue(Configuration.DATA_SOURCE_PATH, dataSourcePath);
 	    return this;
 	}
 
 	public Builder setScanArchives(boolean scanArchives) {
 	    creator.scanArchives = scanArchives;
 	    return this;
 	}
 
 	public Builder setAwaitSeploiment(boolean await) {
 	    creator.await = await;
 	    return this;
 	}
 
 	public Builder setRemote(boolean remote) {
 	    CONFIG.setRemote(remote);
 	    return this;
 	}
 
 	public Builder setServer(boolean server) {
 	    CONFIG.setServer(server);
 	    CONFIG.setClient(!server);
 	    return this;
 	}
 
 	public Builder setClient(boolean client) {
 	    CONFIG.setClient(client);
 	    CONFIG.setServer(!client);
 	    return this;
 	}
 
 	public Builder setProperty(String key, String property) {
 	    CONFIG.putValue(key, property);
 	    return this;
 	}
 
 	public Builder setIpAddress(String property) {
 	    CONFIG.putValue(Configuration.IP_ADDRESS, property);
 	    return this;
 	}
 
 	public Builder setPort(String property) {
 	    CONFIG.putValue(Configuration.PORT, property);
 	    return this;
 	}
 
 	public Builder setMasterThreads(String property) {
 	    CONFIG.putValue(Configuration.BOSS_POOL, property);
 	    return this;
 	}
 
 	public Builder setWorkerThreads(String property) {
 	    CONFIG.putValue(Configuration.WORKER_POOL, property);
 	    return this;
 	}
 
 	public Builder addDeploymentPath(String deploymentPath) {
 
 	    CONFIG.addDeploymentPath(deploymentPath);
 	    return this;
 	}
 
 	public Builder setTimeout(String property) {
 	    CONFIG.putValue(Configuration.CONNECTION_TIMEOUT, property);
 	    return this;
 	}
 
 	public Builder setDataSourcePooledType(boolean dsPooledType) {
 	    JPAManager.pooledDataSource = dsPooledType;
 	    return this;
 	}
 
 	public Builder setPoolProviderType(PoolProviderType poolProviderType) {
 	    PoolConfig.poolProviderType = poolProviderType;
 	    return this;
 	}
 
 	public Builder setPoolPropertiesPath(String path) {
 	    PoolConfig.poolPath = path;
 	    return this;
 	}
 
 	public Builder setPoolProperties(Properties properties) {
 	    initPoolProperties();
 	    PoolConfig.poolProperties.putAll(properties);
 	    return this;
 	}
 
 	public Builder addPoolProperty(Object key, Object value) {
 	    initPoolProperties();
 	    PoolConfig.poolProperties.put(key, value);
 	    return this;
 	}
 
 	public Builder setHotDeployment(boolean hotDeployment) {
 	    creator.hotDeployment = hotDeployment;
 	    return this;
 	}
 
 	public MetaCreator build() {
 	    LOG.info("Lightmare application starts working");
 	    return creator;
 	}
 
     }
 }
