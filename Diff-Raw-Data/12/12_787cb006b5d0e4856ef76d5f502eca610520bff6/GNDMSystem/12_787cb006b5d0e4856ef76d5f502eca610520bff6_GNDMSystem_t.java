 package de.zib.gndms.infra.system;
 
 import de.zib.gndms.infra.GridConfig;
 import de.zib.gndms.infra.monitor.ActionCaller;
 import de.zib.gndms.infra.monitor.GroovyBindingFactory;
 import de.zib.gndms.infra.monitor.GroovyMoniServer;
 import de.zib.gndms.infra.service.GNDMServiceHome;
 import de.zib.gndms.infra.service.GNDMSingletonServiceHome;
 import de.zib.gndms.infra.service.ServiceInfo;
 import de.zib.gndms.infra.sys.EMFactoryProvider;
 import de.zib.gndms.infra.sys.RestrictedEMFactory;
 import de.zib.gndms.logic.action.Action;
 import de.zib.gndms.logic.action.CommandAction;
 import de.zib.gndms.logic.action.SkipActionInitializationException;
 import de.zib.gndms.logic.model.DefaultBatchUpdateAction;
 import de.zib.gndms.logic.model.DelegatingEntityUpdateListener;
 import de.zib.gndms.logic.model.EntityUpdateListener;
 import de.zib.gndms.logic.model.config.ConfigAction;
 import de.zib.gndms.model.common.GridResource;
 import de.zib.gndms.model.common.ModelUUIDGen;
 import de.zib.gndms.model.common.VEPRef;
 import de.zib.gndms.model.dspace.DSpaceRef;
 import groovy.lang.Binding;
 import groovy.lang.GroovyShell;
 import org.apache.axis.components.uuid.UUIDGen;
 import org.apache.axis.components.uuid.UUIDGenFactory;
 import org.apache.axis.message.MessageElement;
 import org.apache.axis.message.addressing.EndpointReferenceType;
 import org.apache.axis.message.addressing.ReferencePropertiesType;
 import org.apache.axis.types.URI;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.globus.wsrf.ResourceException;
 import org.globus.wsrf.impl.SimpleResourceKey;
 import org.globus.wsrf.jndi.Initializable;
 import org.globus.wsrf.utils.AddressingUtils;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import javax.naming.Context;
 import javax.naming.Name;
 import javax.naming.NameAlreadyBoundException;
 import javax.naming.NamingException;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import static javax.persistence.Persistence.createEntityManagerFactory;
 import javax.persistence.Query;
 import javax.xml.namespace.QName;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.security.Principal;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 
 /**
  * This sets up the configuration and database storage area shared between
  * GNDMS services.
  *
  * @author Stefan Plantikow <plantikow@zib.de>
  * @version $Id$
  *
  *          User: stepn Date: 17.06.2008 Time: 23:09:00
  */
 @SuppressWarnings({
         "OverloadedMethodsWithSameNumberOfParameters", "NestedAssignment",
         "ClassWithTooManyMethods" })
 public final class GNDMSystem
 	  implements Initializable, SystemHolder,
 	  EMFactoryProvider, ModelUUIDGen, EntityUpdateListener<GridResource>, ActionCaller {
     private static final int INITIAL_CAPACITY = 32;
     private static final int INSTANCE_RETRIEVAL_INTERVAL = 250;
 
 	private final UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
 
 
 	private final Log logger = createLogger();
 
     @NotNull
 	private static Log createLogger()
 		{ return LogFactory.getLog(GNDMSystem.class); }
 
 
 	@NotNull
 	private final Map<String, Object> instances;
     private final Map<Class<? extends GridResource>, GNDMServiceHome<?>> homes;
 
 
     @NotNull
 	private final GridConfig sharedConfig;
 
 	@NotNull
 	private File sharedDir;
 
 	@NotNull
 	private File logDir;
 
 	@NotNull
 	private File dbDir;
 
 	@NotNull
 	private File dbLogFile;
 
 	@NotNull
 	private EntityManagerFactory emf;
 
 	@NotNull
 	private EntityManagerFactory restrictedEmf;
 
 	@Nullable
 	private GroovyMoniServer groovyMonitor;
 
     private final @NotNull ModelUUIDGen actionUUIDGen = new ModelUUIDGen() {
             @NotNull
             public String nextUUID() {
                 return uuidGen.nextUUID();
             }
         };
 
 	/**
 	 * Retrieves a GNDMSSystem using context.lookup(name).
 	 *
 	 * A lightweight factory facade is either atomically retrieved from context or bound under name
 	 * iff name is unbound in context. The factory acts as an intermediary and ensures that at most
 	 * one DbSetupFacade ever gets instantiated and initialized.
 	 *
 	 * This instance is returned by this call from the factory facade.
 	 *
 	 * @param sharedContext
 	 * @param facadeName
 	 * @return GNDMSSystem singleton
 	 * @throws NamingException
 	 */
 	@NotNull
 	public static GNDMSystem lookupSystem(@NotNull Context sharedContext,
 	                                        @NotNull Name facadeName,
 	                                        @NotNull GridConfig anySharedConfig)
 		  throws NamingException {
 		try {
 			final SysFactory theFactory = new SysFactory(createLogger(), anySharedConfig);
 			sharedContext.bind(facadeName, theFactory);
 			return theFactory.getInstance();
 		}
 		catch (NameAlreadyBoundException n) {
 			return ((SysFactory) sharedContext.lookup(facadeName)).getInstance();
 		}
 	}
 
 	@SuppressWarnings({"StaticMethodOnlyUsedInOneClass"})
 	@NotNull
 	public static GNDMSystem lookupSystem(
 		  @NotNull Context sharedContext, @NotNull String facadeName,
 		  @NotNull GridConfig anySharedConfig)
 		  throws NamingException {
 		try {
 			final SysFactory theFactory = new SysFactory(createLogger(), anySharedConfig);
 			sharedContext.bind(facadeName, theFactory);
 			return theFactory.getInstance();
 		}
 		catch (NameAlreadyBoundException n) {
 			return ((SysFactory) sharedContext.lookup(facadeName)).getInstance();
 		}
 	}
 
 	private GNDMSystem(@NotNull GridConfig anySharedConfig)  {
 		sharedConfig = anySharedConfig;
 		instances = new HashMap<String, Object>(INITIAL_CAPACITY);
         homes = new HashMap<Class<? extends GridResource>, GNDMServiceHome<?>>(INITIAL_CAPACITY);
 		addInstance("sys", this);
 		// initialization intentionally deferred to initialize
 	}
 
 	public void initialize() throws RuntimeException {
 		try {
 			// Q: Think about how to correct UNIX directory/file permissions from java-land
 			// A: External script during deployment
             initSharedDir();
 			createDirectories();
 			emf = createEMF();
 			restrictedEmf = new RestrictedEMFactory(emf);
 			tryTxExecution();
 		}
 		catch (Exception e) {
 			logger.error("Initialization failed", e);
 			throw new RuntimeException(e);
 		}
 	}
 
 
     private synchronized void initSharedDir() throws Exception {
         sharedDir = new File(sharedConfig.getGridPath()).getCanonicalFile();
         final String gridName = sharedConfig.getGridName();
 
         logger.info("Initializing for grid: '" + gridName
               + "' (shared dir: '" + sharedDir.getPath() + "')");
 
     }
 
 
     @SuppressWarnings({ "MethodOnlyUsedFromInnerClass" })
 	private synchronized void shutdown() throws Exception {
 		emf.close();
 		final GroovyMoniServer moniServer = getMonitor();
 		if (moniServer != null)
 			moniServer.stopServer();
 	}
 
 	private void createDirectories() throws IOException {
         File curSharedDir = getSharedDir();
 
 		doCheckOrCreateDir(curSharedDir);
 
 		logDir = new File(curSharedDir, "log");
 		doCheckOrCreateDir(logDir);
 
 		prepareDbStorage();
 	}
 
 	private void prepareDbStorage() throws IOException {
         File curSharedDir = getSharedDir();
 		dbDir = new File(curSharedDir, "db");
 		doCheckOrCreateDir(dbDir);
 
 		System.setProperty("derby.system.home", dbDir.getCanonicalPath());
        System.setProperty("derby.infolog.append", "true");
        System.setProperty("derby.language.logStatementText", "true");
        System.setProperty("derby.stream.error.logSeverityLevel", "20000");
 
 		dbLogFile = new File(logDir, "derby.log");
 		if (!dbLogFile.exists())
 			dbLogFile.createNewFile();
 		System.setProperty("derby.stream.error.file",
 				  dbLogFile.getCanonicalPath());
 	}
 
 	@NotNull
 	public EntityManagerFactory createEMF() throws Exception {
 		final String gridName = sharedConfig.getGridName();
 		final Properties map = new Properties();
 
 		map.put("openjpa.Id", gridName);
 		map.put("openjpa.ConnectionURL", "jdbc:derby:" + gridName+";create=true");
 		logger.info("Opening JPA Store: " + map.toString());
 
 		return createEntityManagerFactory(gridName, map);
 	}
 
 	private void tryTxExecution() {
 		final EntityManager em = emf.createEntityManager();
 		try {
 			em.getTransaction().begin();
 			em.getTransaction().commit();
 		}
 		catch (RuntimeException re)
 			{ em.getTransaction().rollback(); }
 		finally
 			{ em.close(); }
 	}
 
 	@SuppressWarnings({ "MethodOnlyUsedFromInnerClass" })
 	private synchronized void setupShellService() throws Exception {
 		File monitorConfig = new File(sharedDir, "monitor.properties");
 		groovyMonitor = new GroovyMoniServer(getGridName(),
 		                                     monitorConfig, new GNDMSBindingFactory(), this);
 		groovyMonitor.startConfigRefreshThread(true);
 	}
 
     public synchronized void addHome(final @NotNull GNDMServiceHome<?> home)
             throws ResourceException {
         addHome(home.getModelClass(), home);
     }
 
     @SuppressWarnings({ "HardcodedFileSeparator", "RawUseOfParameterizedType" })
     private synchronized <K extends GridResource> void addHome(final @NotNull Class<K> modelClazz,
                                                                final @NotNull GNDMServiceHome<?> home)
             throws ResourceException {
         if (homes.containsKey(modelClazz))
             throw new IllegalStateException("Name clash in home registration");
         else {
             final String homeName = home.getNickName() + "Home";
             addInstance_(homeName, home);
             try {
                 if (home instanceof GNDMSingletonServiceHome) {
                     Object instance = home.find(null);
                     final String resourceName = home.getNickName() + "Resource";
                     addInstance_(resourceName, instance);
                     logger.debug(getSystemName() + " addSingletonResource: '" + resourceName + "' = '" + modelClazz.getName() + '/' + ((GNDMSingletonServiceHome)home).getSingletonID() + '\'');
                 }
             }
             catch (RuntimeException e) {
                 instances.remove(homeName);
                 throw e;
             }
             catch (ResourceException e) {
                 instances.remove(homeName);
                 throw e;                
             }
             homes.put(modelClazz, home);
         }
 
         logger.debug(getSystemName() + " addHome: '" + modelClazz.getName() + '\'');
     }
 
     @SuppressWarnings({ "unchecked" })
     public synchronized <M extends GridResource> GNDMServiceHome<M> getHome(Class<M> modelClazz) {
         final GNDMServiceHome<M> home = (GNDMServiceHome<M>) homes.get(modelClazz);
         if (home == null)
             throw new IllegalStateException("Unknown home");
         return home;
     }
 
 	public synchronized void addInstance(@NotNull String name, @NotNull Object obj) {
         if (name.endsWith("Home") || name.endsWith("Resource"))
             throw new IllegalArgumentException("Reserved instance name");
 
         addInstance_(name, obj);
 
 		logger.debug(getSystemName() + " addInstance: '" + name + '\'');
 	}
 
 
     private void addInstance_(final String name, final Object obj) {
         if ("out".equals(name) || "err".equals(name) || "args".equals(name) || "em".equals(name)
             || "emg".equals(name))
             throw new IllegalArgumentException("Reserved instance name");
 
         if (instances.containsKey(name))
             throw new IllegalStateException("Name clash in instance registration");
         else
             instances.put(name, obj);
     }
 
 
     @NotNull
 	public synchronized <T> T getInstance(@NotNull Class<? extends T> clazz, @NotNull String name)
 	{
 		final Object obj = instances.get(name);
 		if (obj == null)
 			throw new
 				  IllegalStateException("Null instance retrieved or invalid or unregistered name");
 		return clazz.cast(obj);
 	}
 
 	public synchronized ServiceInfo lookupServiceInfo(@NotNull String instancePrefix) {
 		return getInstance(ServiceInfo.class, instancePrefix+"Home");
 	}
 
 
 	private void doCheckOrCreateDir(File mainDir) {
 		if (!mainDir.exists()) {
 			logger.info("Creating " + mainDir.getPath());
 			mainDir.mkdir();
 		}
 		if (!mainDir.isDirectory() || !mainDir.canRead())
 			throw new IllegalStateException(mainDir + " is not accessible");
 	}
 
 	@NotNull
 	public String getSystemName() {
 		try {
 			return '\'' + sharedConfig.getGridName() + "' system";
 		}
 		catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@NotNull
 	public GridConfig getSharedConfig() {
 		return sharedConfig;
 	}
 
 	@NotNull
 	public File getLogDir() {
 		return logDir;
 	}
 
 	@NotNull
 	public File getDbDir() {
 		return dbDir;
 	}
 
 	@NotNull
 	public File getDbLogFile() {
 		return dbLogFile;
 	}
 
 	@Nullable
 	public synchronized GroovyMoniServer getMonitor() {
 		return groovyMonitor;
 	}
 
 	@SuppressWarnings({"ReturnOfThis"})
 	@NotNull
 	public GNDMSystem getSystem() {
 		return this;
 	}
 
 	public void setSystem(@NotNull GNDMSystem system) throws IllegalStateException {
 		throw new IllegalStateException("Cant set this system");
 	}
 
 	@NotNull
 	public String nextUUID() {
 		return uuidGen.nextUUID();
 	}
 
 	@NotNull
 	public static EndpointReferenceType serviceEPRType(@NotNull URI defAddr, @NotNull VEPRef dSpaceRef)
 		  throws URI.MalformedURIException {
 		if (dSpaceRef.getGridSiteId() != null)
 			throw new IllegalArgumentException("Non-local EPRTs currently unsupported");
 
 		try {
 			return AddressingUtils.createEndpointReference(
 				  defAddr.toString(), null);
 		}
 		catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@NotNull
 	public EndpointReferenceType serviceEPRType(@NotNull String instPrefix,
 	                                           @NotNull VEPRef dSpaceRef)
 		  throws URI.MalformedURIException {
 		return serviceEPRType(lookupServiceInfo(instPrefix).getServiceAddress(), dSpaceRef);
 	}
 
 	@NotNull
 	public static VEPRef modelEPRT(@NotNull QName keyTypeName, @NotNull EndpointReferenceType epr) {
 		@NotNull ReferencePropertiesType props = epr.getProperties();
 		@NotNull MessageElement msgElem = props.get(keyTypeName);
 		SimpleResourceKey key = new SimpleResourceKey(keyTypeName, msgElem.getObjectValue());
 
 		// theVEPREF.setSite("");
 		// theVEPREF.setRk(key);
 		return new DSpaceRef();
 	}
 
 	@NotNull
 	public VEPRef modelEPRT(@NotNull String instPrefix, @NotNull EndpointReferenceType epr) {
 		return modelEPRT(lookupServiceInfo(instPrefix).getResourceKeyTypeName(), epr);
 	}
 
 	@NotNull
 	public <T> T retrieveInstance(@NotNull Class<T> clazz, @NotNull String name) {
 		T instance;
 		try { instance = getInstance(clazz, name); }
 		catch (IllegalStateException e) { instance = null; }
 		while (instance != null) {
 			try {
 				Thread.sleep(INSTANCE_RETRIEVAL_INTERVAL);
 			}
 			catch (InterruptedException e) {
 				// inteded
 			}
 			try { instance = getInstance(clazz, name); }
 			catch (IllegalStateException e) { instance = null; }
 		}
 		return instance;
 	}
 
 	public String getGridName() {
 		try {
 			return sharedConfig.getGridName();
 		}
 		catch (Exception e) {
 			// Dont know what to do here
 			throw new RuntimeException(e);
 		}
 	}
 
 
 	@NotNull
 	public EntityManagerFactory getEntityManagerFactory() {
 		return restrictedEmf;
 	}
 
 
     public void onModelChange( GridResource model ) {
         try {
             onModelChange_(model);
         } catch ( ResourceException e ) {
             logger.warn(e);
         }
     }
 
 
     @SuppressWarnings({ "unchecked" })
     private <M extends GridResource> void onModelChange_(final M model) throws ResourceException {
         final Class<M> modelClazz = (Class<M>) model.getClass();
         GNDMServiceHome<M> home = getHome(modelClazz);
         home.refresh(model);
     }
 
 
     @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
     public static ConfigAction<?> instantiateConfigAction(final @NotNull String name,
                                                      final @NotNull String params)
             throws ClassNotFoundException, IllegalAccessException, InstantiationException,
             CommandAction.ParameterTools.ParameterParseException {
         final Class<ConfigAction> clazz;
         if (name.length() > 0 && name.charAt(0) == '.')
             clazz = (Class<ConfigAction>) Class.forName("de.zib.gndms.logic.model" + name +
                     "Action");
         else
             clazz = (Class<ConfigAction>) Class.forName(name);
         if (ConfigAction.class.isAssignableFrom(clazz)) {
             final ConfigAction configAction = clazz.newInstance();
             configAction.parseLocalOptions(params);
             return configAction;
         }
         else
             throw new IllegalArgumentException("Not a ConfigAction");
     }
 
     @SuppressWarnings({ "FeatureEnvy" })
     public Object callAction(
             final @NotNull String className, final @NotNull String opts,
             final @NotNull PrintWriter writer) throws Exception {
         ConfigAction<?> action = instantiateConfigAction(className, opts.trim());
         action.setEntityManager(getEntityManagerFactory().createEntityManager());
         action.setPrintWriter(writer);
         action.setClosingWriterOnCleanUp(false);
         action.setWriteResult(true);
         action.setUUIDGen(actionUUIDGen);
         action.setPostponedActions(new DefaultBatchUpdateAction());
         action.getPostponedActions().setListener(DelegatingEntityUpdateListener.getInstance(this));
 
         logger.info("Running " + className + ' ' + opts);
         try {
             Object retVal = action.call();
             Action<?> postAction = action.getPostponedActions();
             postAction.call();
             return retVal;
         }
         catch (SkipActionInitializationException sa) {
             // Intentionally not logged
             throw sa;
         }
         catch (Exception e) {
             logger.warn("Failure during " + className + ' ' + opts, e);
             throw e;
         }
 
     }
 
 
     @NotNull
     public synchronized File getSharedDir() {
         return sharedDir;
     }
 
 
     @SuppressWarnings({ "unchecked" })
     public @NotNull <M extends GridResource> List<String> listAllResources(
             final @NotNull GNDMServiceHome<M> home, final @NotNull EntityManager em) {
         Query query = home.getListAllQuery(em);
         return query.getResultList();
     }
 
 
     public final @NotNull  <M extends GridResource> List<String> listAllResources(
             final @NotNull EntityManagerFactory emg, final @NotNull Class<M> clazz) {
         final EntityManager manager = emf.createEntityManager();
         List<String> retList = null;
         try {
             try {
                 manager.getTransaction().begin();
                 retList = listAllResources(getHome(clazz), manager);
                 manager.getTransaction().commit();
             }
             finally {
                 if (manager.getTransaction().isActive())
                     manager.getTransaction().rollback();
             }
         }
         finally {
             if (manager.isOpen())
                 manager.close();
         }
         return retList;
     }
 
 
     public final <M extends GridResource> void refreshAllResources(
             final @NotNull GNDMServiceHome<M> home) {
         final EntityManager manager = home.getEntityManagerFactory().createEntityManager();
         try {
             try {
                 manager.getTransaction().begin();
                 for (String id : listAllResources(home, manager)) {
                     try {
                         logger.debug("Restoring " + home.getNickName() + ':' + id);
                         home.find(home.getKeyForId(id));
                     }
                     catch (ResourceException e) {
                         logger.warn(e);
                     }
                 }
                 manager.getTransaction().commit();
             }
             finally {
                 if (manager.getTransaction().isActive())
                     manager.getTransaction().rollback();
             }
         }
         finally {
             if (manager.isOpen())
                 manager.close();
         }
     }
 
 
     public static final class SysFactory {
 		private final Log logger;
 
 		private GNDMSystem instance;
 		private RuntimeException cachedException;
 		private GridConfig sharedConfig;
 
 		public SysFactory(@NotNull Log theLogger, @NotNull GridConfig anySharedConfig) {
 			logger = theLogger;
 			sharedConfig = anySharedConfig;
 		}
 
 		public synchronized GNDMSystem getInstance() {
 			return getInstance(true);
 		}
 		
 		public synchronized GNDMSystem getInstance(boolean setupShellService) {
 			if (cachedException != null)
 				throw cachedException;
 			if (instance == null) {
 				try {
 					GNDMSystem newInstance = new GNDMSystem(sharedConfig);
 					newInstance.initialize();
 					try {
 						if (setupShellService)
 							newInstance.setupShellService();
 					}
 					catch (Exception e) {
 						throw new RuntimeException(e);
 					}
 					try { logger.info(sharedConfig.getGridName() + " initialized"); }
 					catch (Exception e) { logger.error(e); }
 					instance = newInstance;
 				}
 				catch (RuntimeException e) {
 					cachedException = e;
                     throw cachedException;
 				}
 				finally {
 					// not required after this point
 					sharedConfig = null;
 				}
 			}
 			return instance;
 		}
 
 		@SuppressWarnings({ "MethodOnlyUsedFromInnerClass" })
 		private synchronized void shutdown() throws Exception {
 			if (instance == null)
 				return;
 			else
 				getInstance().shutdown();
 		}
 
 		public @NotNull Runnable createShutdownAction() {
 			return new Runnable() {
 				public void run() {
 					try {
 						shutdown();
 					}
 					catch (Exception e) {
 						throw new RuntimeException(e);
 					}
 				}
 			};
 		}
 
 	}
 
 	private final class GNDMSBindingFactory implements GroovyBindingFactory {
 		@NotNull
 		public Binding createBinding(
 			  final @NotNull GroovyMoniServer moniServer,
 		      final @NotNull Principal principal, final @NotNull String args) {
 			final Binding binding = new Binding();
 			for (Map.Entry<String, Object> entry : instances.entrySet())
 				binding.setProperty(entry.getKey(), entry.getValue());
 			return binding;
 		}
 
 		@SuppressWarnings({"StringBufferWithoutInitialCapacity"})
 		public void initShell(@NotNull GroovyShell shell, @NotNull Binding binding) {
 			StringBuilder builder = new StringBuilder();
 			for (Map.Entry<String, Object> entry : instances.entrySet()) {
 				final String key = entry.getKey();
 				builder.append("Object.metaClass.");
 				builder.append(key);
 				builder.append('=');
 				builder.append(key);
 				builder.append(';');
 			}
 			shell.evaluate(builder.toString());
 		}
 
 
 		public void destroyBinding(final @NotNull GroovyMoniServer moniServer,
 		                           final @NotNull Binding binding) {
 			// intended
 		}
 
 	}
 }
