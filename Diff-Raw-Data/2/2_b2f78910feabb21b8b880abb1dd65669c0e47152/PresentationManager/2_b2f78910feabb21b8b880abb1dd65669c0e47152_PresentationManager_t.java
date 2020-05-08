 package jpaoletti.jpm.core;
 
 import java.util.*;
 import jpaoletti.jpm.converter.ClassConverterList;
 import jpaoletti.jpm.converter.Converter;
 import jpaoletti.jpm.converter.ConverterWrapper;
 import jpaoletti.jpm.converter.ExternalConverters;
 import jpaoletti.jpm.core.audit.AuditService;
 import jpaoletti.jpm.core.log.JPMLogger;
 import jpaoletti.jpm.core.monitor.Monitor;
 import jpaoletti.jpm.menu.MenuItemLocation;
 import jpaoletti.jpm.menu.MenuItemLocationsParser;
 import jpaoletti.jpm.parser.*;
 import jpaoletti.jpm.security.core.PMSecurityConnector;
 import jpaoletti.jpm.util.Properties;
 import org.apache.commons.beanutils.NestedNullException;
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.log4j.Logger;
 
 /**
  * Main engine class.
  *
  * @author jpaoletti
  */
 public class PresentationManager extends Observable {
 
     private static PresentationManager instance; // Singleton
     private static final String DEFAULT_CONVERTER = "default-converter";
     private static final String PERSISTENCE_MANAGER = "persistence-manager";
     private static final String SECURITY_CONNECTOR = "security-connector";
     private ResourceBundle bundle;
     private static Long sessionIdSeed = 0L;
     private Properties cfg;
     private JPMLogger logger;
     private Map<Object, Entity> entities;
     private Map<String, MenuItemLocation> locations;
     private Map<Object, Monitor> monitors;
     private List<ExternalConverters> externalConverters;
     private ClassConverterList classConverters;
     private String persistenceManager;
     private boolean error;
     private final Map<String, PMSession> sessions = new HashMap<String, PMSession>();
     private Timer sessionChecker;
     private PMSecurityConnector securityConnector;
     private AuditService auditService;
     private String cfgFilename;
 
     /**
      * Default constructor with default configuration name "jpm-config.xml".
      */
     public PresentationManager() {
         this("jpm-config.xml");
     }
 
     /**
      * Constructor with configuration file name.
      */
     public PresentationManager(String cfgFilename) {
         this.cfgFilename = cfgFilename;
         if (instance == null) {
             if (initialize()) {
                 instance = this;
             } else {
                 instance = null;
             }
         } else {
             instance.warn("Trying to initialize the already initialized PM. Ignoring");
         }
     }
 
     /**
      * Initialize the Presentation Manager singleton
      *
      * @param configurationFilename File name for a configuration file
      * @param log a PrintStream for logs
      *
      * @return true if initialization was success, false otherwies
      */
     public static boolean start(final String configurationFilename) {
         try {
             if (getPm() == null) {
                 instance = new PresentationManager(configurationFilename);
             }
             return isActive();
         } catch (Exception ex) {
             instance = null;
             Logger.getRootLogger().fatal("Unable to initialize jPM", ex);
             return false;
         }
     }
 
     /**
      * @return true if jpm is active
      */
     public static boolean isActive() {
         return getPm() != null;
     }
 
     /**
      * Initialize the Presentation Manager.
      *
      * @param log a PrintStream for logs
      *
      * @return true if initialization was success, false otherwies
      */
     public final boolean initialize() {
         notifyObservers();
         try {
             this.cfg = (Properties) new MainParser(this).parseFile(getCfgFilename());
         } catch (Exception ex) {
             ex.printStackTrace(); //Deep trouble
             error = true;
             return false;
         }
         logger = (JPMLogger) newInstance(getCfg().getProperty("logger-class", "jpaoletti.jpm.core.log.Log4jLogger"));
         logger.setName(getCfg().getProperty("logger-name", "jPM"));
         error = false;
         try {
             try {
                 Class.forName(getDefaultDataAccess());
                 logItem("Default Data Access", getDefaultDataAccess(), "*");
             } catch (Exception e) {
                 logItem("Default Data Access", getDefaultDataAccess(), "?");
             }
 
             logItem("Template", getTemplate(), "*");
             logItem("Menu", getMenu(), "*");
             logItem("Application version", getAppversion(), "*");
             logItem("Title", getTitle(), "*");
             logItem("Subtitle", getSubtitle(), "*");
             logItem("Contact", getContact(), "*");
             logItem("Default Converter", getDefaultConverterClass(), "*");
 
             final String _securityConnector = getCfg().getProperty(SECURITY_CONNECTOR);
             if (_securityConnector != null) {
                 try {
                     securityConnector = (PMSecurityConnector) newInstance(_securityConnector);
                     logItem("Security Connector", _securityConnector, "*");
                 } catch (Exception e) {
                     error = true;
                     logItem("Security Connector", _securityConnector, "?");
                 }
             } else {
                 securityConnector = null;
             }
 
             persistenceManager = cfg.getProperty(PERSISTENCE_MANAGER, "jpaoletti.jpm.core.PersistenceManagerVoid");
             try {
                 newInstance(persistenceManager);
                 logItem("Persistance Manager", persistenceManager, "*");
             } catch (Exception e) {
                 error = true;
                 logItem("Persistance Manager", persistenceManager, "?");
             }
             loadEntities();
             loadMonitors();
             loadConverters();
             loadClassConverters();
             loadLocations();
             createSessionChecker();
             customLoad();
         } catch (Exception exception) {
             error(exception);
             error = true;
         }
         if (error) {
             error("error: One or more errors were found. Unable to start jPM");
         }
         return !error;
     }
 
     public String getTitle() {
         return cfg.getProperty("title", "pm.title");
     }
 
     public String getTemplate() {
         return cfg.getProperty("template", "default");
     }
 
     public boolean isDebug() {
         return "true".equals(cfg.getProperty("debug"));
     }
 
     public String getAppversion() {
         return cfg.getProperty("appversion", "1.0.0");
     }
 
     public String getDefaultConverterClass() {
         return getCfg().getProperty(DEFAULT_CONVERTER);
     }
 
     private void loadMonitors() {
         PMParser parser = new MonitorParser(this);
         final Map<Object, Monitor> result = new HashMap<Object, Monitor>();
         final List<String> monitorNames = getAll("monitor");
         for (String monitorName : monitorNames) {
             try {
                 final Monitor m = (Monitor) parser.parseFile(monitorName);
                 result.put(m.getId(), m);
                 result.put(monitorNames.indexOf(monitorName), m);
                 m.getSource().init();
                 Thread thread = new Thread(m);
                 m.setThread(thread);
                 thread.start();
                 logItem("[Monitor] " + m.getId(), m.getSource().getClass().getName(), "*");
             } catch (Exception exception) {
                 error(exception);
                 logItem("[Monitor] " + monitorName, null, "!");
             }
         }
         monitors = result;
     }
 
     /**
      * Formatting helper for startup
      *
      * @param evt The event
      * @param s1 Text
      * @param s2 Extra description
      * @param symbol Status symbol
      */
     public void logItem(String s1, String s2, String symbol) {
         info(String.format("(%s) %-25s %s", symbol, s1, (s2 != null) ? s2 : ""));
     }
 
     private void loadLocations() {
         final MenuItemLocationsParser parser = new MenuItemLocationsParser(this, getCfg().getProperty("locations", "jpm-locations.xml"));
         locations = parser.getLocations();
         if (locations == null || locations.isEmpty()) {
             warn("No locations defined!");
         }
         if (parser.hasError()) {
             error = true;
         }
     }
 
     private void loadEntities() {
         EntityParser parser = new EntityParser(this);
         if (entities == null) {
             entities = new HashMap<Object, Entity>();
         } else {
             entities.clear();
         }
         final List<String> ss = getAll("entity");
         for (String s : ss) {
             try {
                 final Entity e = (Entity) parser.parseFile(s);
                 if (e.hasSelectedScopeOperations() && !e.isIdentified()) {
                     error("Entity " + e.getId() + " has selected scope operations and idField is not defined");
                     logItem("[Entity] " + e.getId(), e.getClazz(), "!");
                     error = true;
                 } else {
                     if (e.getFields() != null) {
                         for (Field field : e.getFields()) {
                             field.setEntity(e);
                         }
                     }
                     try {
                         Class.forName(e.getClazz());
                         entities.put(e.getId(), e);
                         entities.put(ss.indexOf(s), e);
                         if (e.isWeak()) {
                             logItem("[Entity] " + e.getId(), e.getClazz(), "\u00b7");
                         } else {
                             logItem("[Entity] " + e.getId(), e.getClazz(), "*");
                         }
 
                     } catch (ClassNotFoundException cnte) {
                         logItem("[Entity] " + e.getId(), e.getClazz(), "?");
                         error = true;
                     }
                 }
             } catch (Exception exception) {
                 error(exception);
                 logItem("[Entity] " + s, "???", "!");
                 error = true;
             }
         }
     }
 
     /**
      * Return the list of weak entities of the given entity.
      *
      * @param e The strong entity
      * @return The list of weak entities
      */
     public List<Entity> weakEntities(Entity e) {
         final List<Entity> res = new ArrayList<Entity>();
         for (Entity entity : getEntities().values()) {
             if (entity.getOwner() != null && entity.getOwner().getEntityId().compareTo(e.getId()) == 0) {
                 res.add(entity);
             }
         }
         if (res.isEmpty()) {
             return null;
         } else {
             return res;
         }
     }
 
     /**
      * Return the entity of the given id
      *
      * @param id Entity id
      * @return The entity
      */
     public Entity getEntity(String id) {
         Entity e = getEntities().get(id);
         if (e == null) {
             return null;
         }
         return e;
     }
 
     /**
      * Return the location of the given id
      *
      * @param id The location id
      * @return The MenuItemLocation
      */
     public MenuItemLocation getLocation(String id) {
         return locations.get(id);
     }
 
     /**
      * Return the monitor of the given id
      *
      * @param id The monitor id
      * @return The monitor
      */
     public Monitor getMonitor(String id) {
         return getMonitors().get(id);
     }
 
     /**
      * Create and fill a new Entity Container
      *
      * @param id Entity id
      * @return The container
      */
     public EntityContainer newEntityContainer(String id) {
         final Entity e = lookupEntity(id);
         if (e == null) {
             return null;
         }
         e.setWeaks(weakEntities(e));
         return new EntityContainer(e);
     }
 
     /**
      * Looks for an Entity with the given id
      */
     private Entity lookupEntity(String sid) {
         for (Integer i = 0; i < getEntities().size(); i++) {
             Entity e = getEntities().get(i);
             if (e != null && sid.compareTo(EntityContainer.buildId(e.getId())) == 0) {
                 return getEntity(e.getId());
             }
         }
         return null;
     }
 
 
     /*
      * Getters
      */
     /**
      * Getter for contact
      *
      * @return
      */
     public String getContact() {
         return cfg.getProperty("contact", "");
     }
 
     /**
      * Getter for default data access
      *
      * @return
      */
     public String getDefaultDataAccess() {
         return cfg.getProperty("default-data-access", "jpaoletti.jpm.core.DataAccessVoid");
     }
 
     /**
      * Getter for entities map
      *
      * @return
      */
     public Map<Object, Entity> getEntities() {
         return entities;
     }
 
     /**
      * Getter for location map
      *
      * @return
      */
     public Map<String, MenuItemLocation> getLocations() {
         return locations;
     }
 
     /**
      * Getter for login required
      *
      * @return
      */
     public boolean isLoginRequired() {
         return securityConnector != null;
     }
 
     /**
      * Getter for monitor map
      *
      * @return
      */
     public Map<Object, Monitor> getMonitors() {
         return monitors;
     }
 
     /**
      * Create a new instance of the persistance manager
      *
      * @return
      */
     public PersistenceManager newPersistenceManager() {
         return (PersistenceManager) newInstance(persistenceManager);
     }
 
     /**
      * Getter for singleton pm
      *
      * @return
      */
     public static PresentationManager getPm() {
         return instance;
     }
 
     /*
      * Loggin helpers
      */
     /**
      * If debug flag is active, create a debug information log
      *
      * @param invoquer The invoquer of the debug
      * @param o Object to log
      */
     public void debug(Object invoquer, Object o) {
         if (logger.isDebugEnabled()) {
             logger.debug("[" + invoquer.getClass().getName() + "] " + o);
         }
     }
 
     /**
      * Generate an info entry on the local logger
      *
      * @param o Object to log
      */
     public void info(Object o) {
         logger.info(o);
     }
 
     /**
      * Generate a warn entry on the local logger
      *
      * @param o Object to log
      */
     public void warn(Object o) {
         if (o instanceof Throwable) {
             logger.warn(o, (Throwable) o);
         } else {
             logger.warn(o);
         }
     }
 
     /**
      * Generate an error entry on the local logger
      *
      * @param o Object to log
      */
     public void error(Object o) {
         if (o instanceof Throwable) {
             logger.error(o, (Throwable) o);
         } else {
             logger.error(o);
         }
     }
 
     /*
      * Helpers for bean management
      */
     /**
      * Getter for an object property value as String
      *
      * @param obj The object
      * @param propertyName The property
      * @return The value of the property of the object as string
      *
      */
     public String getAsString(Object obj, String propertyName) {
         Object o = get(obj, propertyName);
         if (o != null) {
             return o.toString();
         } else {
             return "";
         }
     }
 
     /**
      * Getter for an object property value
      *
      * @param obj The object
      * @param propertyName The property
      * @return The value of the property of the object
      *
      */
     public Object get(Object obj, String propertyName) {
         try {
             if (obj != null && propertyName != null) {
                 return PropertyUtils.getNestedProperty(obj, propertyName);
             }
         } catch (NullPointerException e) {
         } catch (NestedNullException e) {
         } catch (Exception e) {
             // Now I don't like it.
             error(e);
             return "-undefined-";
         }
         return null;
     }
 
     /**
      * Setter for an object property value
      *
      * @param obj The object
      * @param name The property name
      * @param value The value to set
      *
      */
     public void set(Object obj, String name, Object value) {
         try {
             PropertyUtils.setNestedProperty(obj, name, value);
         } catch (Exception e) {
             error(e);
         }
     }
 
     /**
      * Creates a new instance object of the given class.
      *
      * @param clazz The Class of the new Object
      * @return The new Object or null on any error.
      */
     public Object newInstance(String clazz) {
         try {
             return Class.forName(clazz).newInstance();
         } catch (Exception e) {
             error(e);
             return null;
         }
     }
 
     private void loadConverters() {
         final PMParser parser = new ExternalConverterParser(this);
         externalConverters = new ArrayList<ExternalConverters>();
         final List<String> ss = getAll("external-converters");
         for (String s : ss) {
             try {
                 final ExternalConverters ec = (ExternalConverters) parser.parseFile(s);
                 externalConverters.add(ec);
                 logItem("[ExternalConverter] " + s, null, "*");
             } catch (Exception exception) {
                 error(exception);
                 logItem("[ExternalConverter] " + s, null, "!");
             }
         }
         //Check repeated ids
         final List<String> ids = new ArrayList<String>();
         for (ExternalConverters ec : externalConverters) {
             for (ConverterWrapper cw : ec.getConverters()) {
                 if (ids.contains(cw.getId())) {
                     logItem("[ExternalConverter] Repeated id: " + cw.getId(), null, "!");
                     error = true;
                 } else {
                     ids.add(cw.getId());
                 }
             }
         }
     }
 
     private void loadClassConverters() {
         final PMParser parser = new ClassConverterParser(this);
         final String classConvertersFile = getCfg().getProperty("class-converters");
         if (classConvertersFile != null) {
             try {
                 classConverters = (ClassConverterList) parser.parseFile(classConvertersFile);
                 logItem("[ClassConverters] " + classConvertersFile, null, "*");
             } catch (Exception exception) {
                 error(exception);
                 logItem("[ClassConverters] " + classConvertersFile, null, "!");
             }
         } else {
             logItem("[ClassConverters] - ", null, "*");
             classConverters = new ClassConverterList(this);
         }
     }
 
     public Converter findExternalConverter(String id) {
         for (ExternalConverters ecs : getExternalConverters()) {
             ConverterWrapper w = ecs.getWrapper(id);
             if (w != null) {
                 return w.getConverter();
             }
         }
         return null;
     }
 
     /**
      * Getter for the list of external converters
      */
     public List<ExternalConverters> getExternalConverters() {
         return externalConverters;
     }
 
     /**
      * Creates a new session with the given id. If null is used, an automatic
      * session id will be generated
      *
      * @param sessionId The new session id. Must be unique.
      * @throws PMException on already defined session
      * @return New session
      */
     public PMSession registerSession(String sessionId) {
         synchronized (sessions) {
             if (sessionId != null) {
                 if (!sessions.containsKey(sessionId)) {
                     sessions.put(sessionId, new PMSession(sessionId));
                 }
                 return getSession(sessionId);
             } else {
                 return registerSession(newSessionId());
             }
         }
     }
 
     /**
      * Return the session for the given id
      *
      * @param sessionId The id of the wanted session
      * @return The session
      */
     public PMSession getSession(String sessionId) {
         final PMSession s = sessions.get(sessionId);
         if (s != null) {
             s.setLastAccess(new Date());
         }
         return s;
     }
 
     /**
      * Getter for the session map.
      *
      * @return Sessions
      */
     public Map<String, PMSession> getSessions() {
         return sessions;
     }
 
     /**
      * Removes the given id session
      */
     public void removeSession(String sessionId) {
         sessions.remove(sessionId);
     }
 
     public Properties getCfg() {
         return cfg;
     }
 
     public String getSubtitle() {
         return cfg.getProperty("subtitle", "pm.subtitle");
     }
 
     private void createSessionChecker() {
         final Long timeout = Long.parseLong(cfg.getProperty("session-timeout", "3600")) * 1000;
         final int interval = Integer.parseInt(cfg.getProperty("session-check-interval", "300")) * 1000;
         if (sessionChecker != null) {
             sessionChecker.cancel();
         }
         sessionChecker = new Timer();
         sessionChecker.scheduleAtFixedRate(new TimerTask() {
             @Override
             public void run() {
                 synchronized (sessions) {
                     final List<String> toRemove = new ArrayList<String>();
                     for (Map.Entry<String, PMSession> entry : sessions.entrySet()) {
                         if (entry.getValue().getLastAccess().getTime() + timeout < System.currentTimeMillis()) {
                             toRemove.add(entry.getKey());
                         }
                     }
                     for (String session : toRemove) {
                         removeSession(session);
                     }
                 }
             }
         }, 0, interval);
     }
 
     public String getCopyright() {
         return cfg.getProperty("copyright", "jpaoletti");
     }
 
     public String getMenu() {
         return cfg.getProperty("menu", "jpm-menu.xml");
     }
 
     public boolean isHideableHeader() {
         return "true".equalsIgnoreCase(cfg.getProperty("hideable-header", "false"));
     }
 
     public static synchronized String newSessionId() {
         sessionIdSeed++;
         final MD5 md5 = new MD5();
         return md5.calcMD5(sessionIdSeed.toString());
     }
 
     /**
      * Returns the internacionalized string for the given key
      */
     public String message(String key, Object... params) {
         if (key == null) {
             return null;
         }
         try {
            String string = (getResourceBundle().containsKey(key)) ? getResourceBundle().getString(key) : key;
             if (params != null) {
                 for (int i = 0; i < params.length; i++) {
                     String param = (params[i] == null) ? "" : params[i].toString();
                     string = string.replaceAll("\\{" + i + "\\}", param);
                 }
             }
             return string;
         } catch (Exception e) {
             return key;
         }
     }
 
     /**
      * Returns the internacionalized string for the given key
      */
     public static String getMessage(String key, Object... params) {
         return getPm().message(key, params);
     }
 
     public Converter getDefaultConverter() {
         try {
             if (getDefaultConverterClass() != null && !"".equals(getDefaultConverterClass().trim())) {
                 return (Converter) newInstance(getDefaultConverterClass());
             }
         } catch (Exception e) {
         }
         return null;
     }
 
     public PMSession getSessionByUser(final String username) {
         for (PMSession s : sessions.values()) {
             if (s.getUser() != null && s.getUser().getUsername().equals(username)) {
                 return s;
             }
         }
         return null;
     }
 
     //TODO Check security connector and its context
     public PMSecurityConnector getSecurityConnector(PMContext ctx) {
         securityConnector.setContext(ctx);
         return securityConnector;
     }
 
     public List<String> getAll(String name) {
         return cfg.getAll(name);
     }
 
     public boolean allowMultipleLogin() {
         return "true".equalsIgnoreCase(cfg.getProperty("multi-login", "true"));
     }
 
     /**
      * Returns local resource bundle for internationalization
      */
     public ResourceBundle getResourceBundle() {
         if (bundle == null) {
             String lang = "";
             String country = "";
             final String _locale = getPm().getCfg().getProperty("locale", "");
             if (_locale != null && !"".equals(_locale.trim())) {
                 final String[] __locale = _locale.split("[_]");
                 lang = __locale[0];
                 if (__locale.length > 1) {
                     country = __locale[1];
                 }
             }
             final Locale locale = new Locale(lang, country);
             bundle = ResourceBundle.getBundle("ApplicationResource", locale);
         }
         return bundle;
     }
 
     /**
      * @return the cfgFilename
      */
     public String getCfgFilename() {
         return cfgFilename;
     }
 
     /**
      * @param cfgFilename the cfgFilename to set
      */
     protected final void setCfgFilename(String cfgFilename) {
         this.cfgFilename = cfgFilename;
     }
 
     public ClassConverterList getClassConverters() {
         return classConverters;
     }
 
     /**
      * Search external converter by id
      */
     public Converter getExternalConverter(String id) {
         for (ExternalConverters ec : getExternalConverters()) {
             for (ConverterWrapper cw : ec.getConverters()) {
                 if (cw.getId().equals(id)) {
                     return cw.getConverter();
                 }
             }
         }
         return null;
     }
 
     /**
      * Returns audit service
      */
     public AuditService getAuditService() {
         if (auditService == null) {
             auditService = (AuditService) newInstance(cfg.getProperty("audit-service", "jpaoletti.jpm.core.audit.SimpleAudit"));
             auditService.setLevel(cfg.getInt("audit-level", -1));
         }
         return auditService;
     }
 
     /**
      * Executes a custom loader.
      */
     private void customLoad() {
         final String s = getCfg().getProperty("custom-loader");
         if (s != null) {
             try {
                 final CustomLoader customLoader = (CustomLoader) Class.forName(s).newInstance();
                 logItem("Custom Loader", s, "*");
                 customLoader.execute(this);
             } catch (ClassNotFoundException e) {
                 error = true;
                 logItem("Custom Loader", s, "?");
             } catch (Exception e) {
                 error = true;
                 error(e);
                 logItem("Custom Loader Failed", s, "!");
             }
         }
     }
 }
