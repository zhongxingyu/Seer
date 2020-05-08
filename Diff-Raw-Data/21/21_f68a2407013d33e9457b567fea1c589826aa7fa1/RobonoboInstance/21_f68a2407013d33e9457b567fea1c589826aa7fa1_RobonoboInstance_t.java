 package com.robonobo.core;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.PropertyConfigurator;
 
 import com.robonobo.common.concurrent.SafetyNet;
 import com.robonobo.common.serialization.ConfigBeanSerializer;
 import com.robonobo.common.serialization.PageBufferSerializer;
 import com.robonobo.common.serialization.SerializationManager;
 import com.robonobo.common.util.ExceptionEvent;
 import com.robonobo.common.util.ExceptionListener;
 import com.robonobo.core.api.Robonobo;
 import com.robonobo.core.api.RobonoboException;
 import com.robonobo.core.api.RobonoboStatus;
 import com.robonobo.core.api.config.RobonoboConfig;
 import com.robonobo.core.itunes.ITunesService;
 import com.robonobo.core.mina.MinaService;
 import com.robonobo.core.search.SearchService;
 import com.robonobo.core.service.*;
 import com.robonobo.core.storage.StorageService;
 import com.robonobo.core.update.Updater;
 import com.robonobo.core.wang.RobonoboWangConfig;
 import com.robonobo.core.wang.WangService;
 import com.robonobo.mina.external.Application;
 import com.robonobo.mina.external.MinaControl;
 import com.robonobo.spi.RuntimeService;
 
 /**
  * Central class that controls a robonobo instance; handles startup, and holds references to services that do the actual
  * work
  * 
  * @author macavity
  * 
  */
 public class RobonoboInstance implements Robonobo {
 	protected String version = "dev-unknown";
 	protected Log log;
 	ScheduledThreadPoolExecutor executor;
 	Map<String, Object> configs = new HashMap<String, Object>();
 	ServiceManager serviceMgr;
 	private SerializationManager serializationMgr;
 	Application thisApplication;
 	private File homeDir;
 	private RobonoboStatus status = RobonoboStatus.Stopped;
 
 	public RobonoboInstance(String[] args) throws Exception {
 		loadVersion();
 		setHomeDir();
 		initLogging();
 		updateHomeDir();
 		loadApplicationDetails();
 		initSerializers();
 		loadConfig();
 		overrideConfigWithEnv();
 		registerServices();
 		startExecutor();
 		Platform.getPlatform().initRobonobo(this);
 	}
 
 	public void start() throws RobonoboException {
 		setStatus(RobonoboStatus.Starting);
 		serviceMgr.startup();
 	}
 
 	protected void startExecutor() {
 		if (executor == null) {
 			int poolSz = Runtime.getRuntime().availableProcessors() + getConfig().getThreadPoolOverhead();
 			log.info("Starting thread pool with " + poolSz + " threads");
 			executor = new ScheduledThreadPoolExecutor(poolSz);
 		}
 	}
 
 	protected void registerServices() {
 		serviceMgr = new ServiceManager(RobonoboInstance.this);
 		serviceMgr.registerService(new EventService());
 		serviceMgr.registerService(new DbService());
 		serviceMgr.registerService(new FormatService());
 		serviceMgr.registerService(new SearchService());
 		serviceMgr.registerService(new StorageService());
 		serviceMgr.registerService(new MinaService());
 		serviceMgr.registerService(new GatewayService());
 		serviceMgr.registerService(new ShareService());
 		serviceMgr.registerService(new DownloadService());
 		serviceMgr.registerService(new TrackService());
 		serviceMgr.registerService(new MetadataService());
 		serviceMgr.registerService(new PlaybackService());
 		serviceMgr.registerService(new UserService());
 		serviceMgr.registerService(new TaskService());
 		serviceMgr.registerService(new LibraryService());
 
 		if (getConfig().isAgoric())
 			serviceMgr.registerService(new WangService());
 		if (Platform.getPlatform().iTunesAvailable())
 			serviceMgr.registerService(Platform.getPlatform().getITunesService());
 		// Extra services defined in config
 		String[] extraServices = getConfig().getExtraServices().split(",");
 		for (String serviceClass : extraServices) {
 			if (serviceClass.trim().length() > 0) {
 				log.info("Instantiating extra service " + serviceClass);
 				try {
 					Class<?> cl = Class.forName(serviceClass);
 					serviceMgr.registerService((RuntimeService) cl.newInstance());
 				} catch (ClassNotFoundException e) {
 					throw new RuntimeException(e);
 				} catch (InstantiationException e) {
 					throw new RuntimeException(e);
 				} catch (IllegalAccessException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		}
 	}
 
 	protected void loadVersion() throws IOException {
 		InputStream is = getClass().getResourceAsStream("/robonobo-build.properties");
 		if (is != null) {
 			Properties props = new Properties();
 			props.load(is);
 			version = props.getProperty("version");
 			is.close();
 		}
 	}
 
 	protected void initSerializers() {
 		serializationMgr = new SerializationManager();
 		serializationMgr.registerSerializer(new PageBufferSerializer());
 	}
 
 	protected void loadConfig() throws IOException {
 		// Load base robonobo config, that contains class names of any other
 		// configs we have
 		File configDir = new File(homeDir, "config");
 		if (!configDir.exists())
 			configDir.mkdirs();
 		File configFile = new File(configDir, "robo.cfg");
 		ConfigBeanSerializer cbs = new ConfigBeanSerializer();
 		if (configFile.exists()) {
 			log.warn("Loading config from " + configFile.getAbsolutePath());
 			configs.put("robo", cbs.deserializeConfig(RobonoboConfig.class, configFile));
 		} else {
 			log.warn("Config file " + configFile.getAbsolutePath() + " not found - creating new config");
 			configs.put("robo", new RobonoboConfig());
 		}
 		String[] extraCfgs = getConfig().getExtraConfigs().split(",");
 		Pattern cfgPat = Pattern.compile("^(.+):(.+)$");
 		for (String cfgStr : extraCfgs) {
 			Matcher m = cfgPat.matcher(cfgStr);
 			if (!m.matches())
 				throw new IOException("Invalid config string: " + cfgStr);
 			String cfgName = m.group(1);
 			String cfgClassName = m.group(2);
 			configFile = new File(configDir, cfgName + ".cfg");
 			try {
 				if (configFile.exists()) {
 					log.warn("Loading config from " + configFile.getAbsolutePath());
 					configs.put(cfgName, cbs.deserializeConfig(Class.forName(cfgClassName), configFile));
 				} else {
 					log.warn("Config file " + configFile.getAbsolutePath() + " not found - creating new config");
 					configs.put(cfgName, Class.forName(cfgClassName).newInstance());
 				}
 			} catch (ClassNotFoundException e) {
 				throw new IOException("Cannot find specified config class " + cfgClassName);
 			} catch (InstantiationException e) {
 				throw new IOException("Error loading config class " + cfgClassName);
 			} catch (IllegalAccessException e) {
 				throw new IOException("Error loading config class " + cfgClassName);
 			}
 		}
 
 		// First time through, set the default download dir
 		if (getConfig().getFinishedDownloadsDirectory() == null) {
 			File dd = Platform.getPlatform().getDefaultDownloadDirectory();
 			dd.mkdirs();
 			String ddPath = dd.getAbsolutePath();
 			getConfig().setFinishedDownloadsDirectory(ddPath);
 		}
 
 		saveConfig();
 	}
 
 	public boolean isDevVersion() {
 		return version.toLowerCase().startsWith("dev");
 	}
 	
 	private void setHomeDir() {
 		if (System.getenv().containsKey("ROBOHOME"))
 			homeDir = new File(System.getenv("ROBOHOME"));
 		else {
 			homeDir = Platform.getPlatform().getDefaultHomeDirectory();
 			// Use a different homedir for development versions, try to avoid fucking things up too badly
 			if(isDevVersion()) {
 				String dirName = homeDir.getName() + "-dev";
 				homeDir = new File(homeDir.getParentFile(), dirName);
 			}
 		}
 		homeDir.mkdirs();
 	}
 
 	public File getHomeDir() {
 		return homeDir;
 	}
 
 	/**
 	 * Overrides config properties with values specified in 'cfg_<cfgName>_<propName>' environment vars
 	 */
 	protected void overrideConfigWithEnv() {
 		ConfigBeanSerializer cbs = new ConfigBeanSerializer();
 		try {
 			for (String cfgName : configs.keySet()) {
 				cbs.overrideCfgFromEnv(configs.get(cfgName), cfgName);
 			}
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void shutdown() {
 		setStatus(RobonoboStatus.Stopping);
 		serviceMgr.shutdown();
 		setStatus(RobonoboStatus.Stopped);
 	}
 
 	protected void loadApplicationDetails() {
 		thisApplication = new Application();
 		thisApplication.setVersion(version);
 		thisApplication.setName("robonobo");
 		thisApplication.setPublisher("The robonobo project");
 		thisApplication.setHomeUri("http://robonobo.com");
 		log.info(thisApplication.getName() + "/" + thisApplication.getVersion() + " (" + thisApplication.getPublisher()
 				+ ") " + thisApplication.getHomeUri());
 	}
 
 	public Application getApplication() {
 		return thisApplication;
 	}
 
 	public ShareService getShareService() {
 		return (ShareService) serviceMgr.getService("core.shares");
 	}
 
 	public TrackService getTrackService() {
 		return (TrackService) serviceMgr.getService("core.tracks");
 	}
 
 	public TaskService getTaskService() {
 		return (TaskService) serviceMgr.getService("core.tasks");
 	}
 
 	public LibraryService getLibraryService() {
 		return (LibraryService) serviceMgr.getService("core.library");
 	}
 
 	public ITunesService getITunesService() {
 		return (ITunesService) serviceMgr.getService("core.itunes");
 	}
 
 	public UserService getUsersService() {
 		return (UserService) serviceMgr.getService("core.users");
 	}
 
 	public PlaybackService getPlaybackService() {
 		return (PlaybackService) serviceMgr.getService("core.playback");
 	}
 
 	public DownloadService getDownloadService() {
 		return (DownloadService) serviceMgr.getService("core.downloads");
 	}
 
 	public WangService getWangService() {
 		return (WangService) serviceMgr.getService("core.wang");
 	}
 
 	public RobonoboConfig getConfig() {
 		return (RobonoboConfig) getConfig("robo");
 	}
 
 	public Object getConfig(String cfgName) {
 		return configs.get(cfgName);
 	}
 
 	public ScheduledThreadPoolExecutor getExecutor() {
 		return executor;
 	}
 
 	public DbService getDbService() {
 		return (DbService) serviceMgr.getService("core.db");
 	}
 
 	public MetadataService getMetadataService() {
 		return (MetadataService) serviceMgr.getService("core.metadata");
 	}
 
 	public MinaControl getMina() {
 		return ((MinaService) getServiceMgr().getService("core.mina")).getMina();
 	}
 
 	public SerializationManager getSerializationManager() {
 		return serializationMgr;
 	}
 
 	public ServiceManager getServiceMgr() {
 		return serviceMgr;
 	}
 
 	public StorageService getStorageService() {
 		return (StorageService) serviceMgr.getService("core.storage");
 	}
 
 	public void addConfig(String name, Object cfg) {
 		configs.put(name, cfg);
 		saveConfig();
 	}
 
 	public void saveConfig() {
 		File configDir = new File(homeDir, "config");
 		ConfigBeanSerializer cbs = new ConfigBeanSerializer();
 		for (String cfgName : configs.keySet()) {
 			File configFile = new File(configDir, cfgName + ".cfg");
 			try {
 				log.info("Saving config '" + cfgName + "' to " + configFile.getAbsolutePath());
 				cbs.serializeConfig(configs.get(cfgName), configFile);
 			} catch (Exception e) {
 				log.warn("NOT saving config back to filesystem: " + e.getMessage());
 			}
 		}
 	}
 
 	public void setExecutor(ScheduledThreadPoolExecutor executor) {
 		this.executor = executor;
 	}
 
 	protected void initLogging() {
 		// This property gets picked up by the log4j config
 		File logDir = new File(homeDir, "logs");
 		if (!logDir.exists())
 			logDir.mkdir();
 		System.setProperty("robo.log.dir", logDir.getAbsolutePath());
 
 		// If there isn't a log4j properties file in our homedir, copy one from
 		// the jar
 		try {
 			File log4jCfgFile = new File(homeDir, "log4j.properties");
 			if (!log4jCfgFile.exists()) {
 				// Note, if we call the props file in the jar "log4j.properties", then log4j will detect and use it,
 				// ignoring what we have on the filesystem
 				InputStream is = getClass().getResourceAsStream("/log4j.props.skel");
 				OutputStream os = new FileOutputStream(log4jCfgFile);
 				byte[] arr = new byte[1024];
 				int numRead;
 				while ((numRead = is.read(arr)) > 0)
 					os.write(arr, 0, numRead);
 				is.close();
 				os.close();
 			}
 
 			PropertyConfigurator.configureAndWatch(log4jCfgFile.getAbsolutePath());
 			log = LogFactory.getLog(getClass());
 			log.fatal("O HAI!  robonobo starting using homedir " + homeDir.getAbsolutePath());
 			// Log uncaught exceptions in other threads
 			SafetyNet.addListener(new ExceptionListener() {
 				public void onException(ExceptionEvent e) {
 					log.error("Uncaught exception", e.getException());
 				}
 			});
 		} catch (Exception e) {
 			System.err.println("Error: Unable to initialize log4j logging (caught " + e.getClass().getName() + ": "
 					+ e.getMessage() + ")");
 		}
 	}
 
 	protected void updateHomeDir() throws IOException {
 		Updater updater = new Updater(homeDir);
 		updater.runUpdate();
 	}
 
 	public FormatService getFormatService() {
 		return (FormatService) serviceMgr.getService("core.format");
 	}
 
 	public SearchService getSearchService() {
 		return (SearchService) serviceMgr.getService("core.search");
 	}
 
 	public String getVersion() {
 		return version;
 	}
 
 	public EventService getEventService() {
 		return (EventService) serviceMgr.getService("core.event");
 	}
 
 	public RobonoboStatus getStatus() {
 		return status;
 	}
 
 	public void setStatus(RobonoboStatus status) {
 		log.debug("New status: " + status);
 		this.status = status;
 	}
 }
