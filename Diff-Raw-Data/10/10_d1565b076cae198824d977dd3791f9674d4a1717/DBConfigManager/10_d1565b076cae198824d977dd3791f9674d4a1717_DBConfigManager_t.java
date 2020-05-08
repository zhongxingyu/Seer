 package ro.gagarin.config;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import ro.gagarin.BasicManagerFactory;
 import ro.gagarin.ConfigDAO;
 import ro.gagarin.ConfigurationManager;
 import ro.gagarin.ManagerFactory;
 import ro.gagarin.application.objects.AppConfig;
 import ro.gagarin.exceptions.DataConstraintException;
 import ro.gagarin.exceptions.ErrorCodes;
 import ro.gagarin.exceptions.OperationException;
 import ro.gagarin.jdbc.objects.DBConfig;
 import ro.gagarin.log.AppLog;
 import ro.gagarin.scheduler.JobController;
 import ro.gagarin.scheduler.ScheduledJob;
 import ro.gagarin.session.Session;
 
 public class DBConfigManager extends ConfigHolder implements ConfigurationManager, SettingsChangeObserver {
 
     private static final transient Logger LOG = Logger.getLogger(DBConfigManager.class);
 
     // TODO: there is a problem with the configuration implementation in the
     // following scenario:
     // the config is only in the DB
     // runtime, the config is added to the file
     // Problem: DBConfig observers are not notified
 
     private static ManagerFactory FACTORY = BasicManagerFactory.getInstance();
     private static final DBConfigManager INSTANCE = new DBConfigManager(FileConfigurationManager.getInstance());
     private ConfigImportJob configImportJob;
 
     static {
 	ConfigurationManager cfgManager = FACTORY.getConfigurationManager();
 	long period = cfgManager.getLong(Config.DB_CONFIG_CHECK_PERIOD);
 
 	INSTANCE.registerForChange(INSTANCE);
 	INSTANCE.configImportJob = new DBConfigManager.ConfigImportJob("DB_CONFIG_IMPORT", period, period);
 	FACTORY.getScheduleManager().scheduleJob(INSTANCE.configImportJob, true);
     }
 
     public static class ConfigImportJob extends ScheduledJob {
 	public ConfigImportJob(String name, long initialWait, long period) {
 	    super(name, initialWait, period);
 	}
 
 	@Override
 	public void execute(Session session, AppLog log, JobController jc) throws Exception {
 	    ConfigDAO configDAO = FACTORY.getDAOManager().getConfigDAO(session);
 	    long lastUpdateTime = configDAO.getLastUpdateTime();
 	    log.debug("DBLUT = " + lastUpdateTime + " CacheLUT=" + INSTANCE.getLastUpdateTime());
 	    if (lastUpdateTime > INSTANCE.getLastUpdateTime()) {

		// TODO: use TB generated timestamp
 		synchronized (INSTANCE) {
		    long lastQuery = System.currentTimeMillis();
		    ArrayList<ConfigEntry> cfgValues = configDAO.listConfigurations();
 		    INSTANCE.importConfigMap(cfgValues, log);
 		    INSTANCE.setLastUpdateTime(lastQuery);
 		    INSTANCE.notify();
 		    LOG.debug("DB import done");
 		}
 	    }
 	}
     }
 
     private final ConfigurationManager localConfig;
     private long lastUpdateTime = 0;
    private long lastChangeRequest = System.currentTimeMillis();
 
     private DBConfigManager(ConfigurationManager localCfg) {
 	this.localConfig = localCfg;
     }
 
     private void importConfigMap(ArrayList<ConfigEntry> cfgValues, AppLog log) {
 	String cfgs[] = new String[Config.values().length];
 
 	for (ConfigEntry configEntry : cfgValues) {
 
 	    try {
 		Config cfg = Config.valueOf(configEntry.getConfigName());
 
 		// don't import entries defined locally
 		if (!localConfig.isDefined(cfg)) {
 		    cfgs[cfg.ordinal()] = configEntry.getConfigValue();
 		}
 	    } catch (Exception e) {
 		log.error("Could not interpret config " + configEntry.getConfigName() + "="
 			+ configEntry.getConfigValue(), e);
 	    }
 	}
 	importConfig(cfgs);
     }
 
     public void setLastUpdateTime(long lastQuery) {
 	this.lastUpdateTime = lastQuery;
 
     }
 
     public long getLastUpdateTime() {
 	return this.lastUpdateTime;
     }
 
     @Override
     public String getString(Config config) {
 	// local config has precedence
 	if (localConfig.isDefined(config)) {
 	    return localConfig.getString(config);
 	}
 	if (super.isDefined(config)) {
 	    return super.getString(config);
 	}
 
 	// this will return the default value
 	return localConfig.getString(config);
     }
 
     @Override
     public InputStream getConfigFileStream(Config file) throws OperationException {
 	return localConfig.getConfigFileStream(file);
     }
 
     @Override
     public InputStream getConfigFileStream(String file) throws OperationException {
 	return localConfig.getConfigFileStream(file);
     }
 
     public static DBConfigManager getInstance() {
 	return INSTANCE;
     }
 
     @Override
     public synchronized void setConfigValue(Session session, Config config, String value) throws OperationException {
 
 	// local config has precedence...
 	if (localConfig.isDefined(config)) {
 	    AppLog log = session.getManagerFactory().getLogManager(session, DBConfigManager.class);
 	    log.error("Changing the local config will not be persisted! " + config.name() + "=" + value);
 	    localConfig.setConfigValue(session, config, value);
 	    return;
 	}
 
 	if (value.equalsIgnoreCase(getString(config)))
 	    return;
 
 	this.lastChangeRequest = System.currentTimeMillis();
 
 	ConfigDAO configDAO = FACTORY.getDAOManager().getConfigDAO(session);
 	DBConfig cfg = new DBConfig();
 	cfg.setConfigName(config.name());
 	cfg.setConfigValue(value);
 	try {
 	    configDAO.setConfigValue(cfg);
 	} catch (DataConstraintException e) {
 	    LOG.error("Error setting config " + config + "=" + value, e);
 	    throw new OperationException(ErrorCodes.DB_OP_ERROR, e);
 	}
 	FACTORY.getScheduleManager().triggerExecution(configImportJob);
     }
 
     @Override
     public boolean configChanged(Config config, String value) {
 	switch (config) {
 	case DB_CONFIG_CHECK_PERIOD:
 	    FACTORY.getScheduleManager().updateJobRate(configImportJob.getId(), Long.valueOf(value));
 	    return true;
 	default:
 	    break;
 	}
 	return false;
     }
 
     public List<ConfigEntry> getConfigValues() {
 	ArrayList<ConfigEntry> cfgList = new ArrayList<ConfigEntry>();
 	for (Config cfg : Config.values()) {
 	    // do not export internal config controls
 	    if (cfg.name().startsWith("_"))
 		continue;
 	    AppConfig cfgObj = new AppConfig();
 	    cfgObj.setConfigName(cfg.name());
 	    cfgObj.setConfigValue(getString(cfg));
 	    if (isDefined(cfg)) {
 		cfgObj.setConfigScope(ConfigScope.DB);
 	    } else if (localConfig.isDefined(cfg)) {
 		cfgObj.setConfigScope(ConfigScope.LOCAL);
 	    } else {
 		cfgObj.setConfigScope(ConfigScope.DEFAULT);
 	    }
 
 	    cfgList.add(cfgObj);
 	}
 	return cfgList;
     }
 
     /**
      * Wait until the last requested change to actually happen. For testing
      * purposes only.
      * 
      * @throws InterruptedException
      */
     public void waitForDBImport() throws InterruptedException {
 	LOG.info("Waiting for DB import...");
 	synchronized (this) {
 	    while (lastChangeRequest > lastUpdateTime) {
 		this.wait();
 	    }
 	}
 	LOG.info("DB import done.");
     }
 }
