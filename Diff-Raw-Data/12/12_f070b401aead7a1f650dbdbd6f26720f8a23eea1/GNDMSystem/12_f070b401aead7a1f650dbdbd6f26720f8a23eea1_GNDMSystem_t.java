 package de.zib.gndms.infra.system;
 
 import de.zib.gndms.infra.GridConfig;
 import de.zib.gndms.infra.monitor.ActionCaller;
 import de.zib.gndms.infra.monitor.GroovyMoniServer;
 import de.zib.gndms.infra.service.GNDMServiceHome;
 import de.zib.gndms.logic.model.DefaultBatchUpdateAction;
 import de.zib.gndms.logic.model.EntityAction;
 import de.zib.gndms.logic.model.EntityUpdateListener;
 import de.zib.gndms.logic.util.LogicTools;
 import de.zib.gndms.model.common.GridResource;
 import de.zib.gndms.model.common.ModelUUIDGen;
 import de.zib.gndms.model.common.VEPRef;
 import de.zib.gndms.model.dspace.DSpaceRef;
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
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 
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
 	  implements Initializable, SystemHolder, EMFactoryProvider, ModelUUIDGen,
         EntityUpdateListener<GridResource> {
 
     private static @NotNull Log createLogger() { return LogFactory.getLog(GNDMSystem.class); }
 
     private final boolean debugMode;
 
     private @NotNull final UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
 	private @NotNull final Log logger = createLogger();
     private @NotNull final InstanceDirectory instanceDir;
     private @NotNull final ConfigActionCaller actionCaller;
 	private @NotNull final GridConfig sharedConfig;
 	private @NotNull File sharedDir;
     private @NotNull File dbDir;
     private @NotNull File logDir;
 	private @NotNull File dbLogFile;
 	private @NotNull EntityManagerFactory emf;
 	private @NotNull EntityManagerFactory restrictedEmf;
 	private @NotNull GroovyMoniServer groovyMonitor;
 
     private ExecutorService executorService;
 
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
 	                                      @NotNull GridConfig anySharedConfig,
                                           boolean debugModeParam)
 		  throws NamingException {
 		try {
 			final SysFactory theFactory =
                     new SysFactory(createLogger(), anySharedConfig, debugModeParam);
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
 		  @NotNull GridConfig anySharedConfig,
           boolean debugModeParam)
 		  throws NamingException {
 		try {
 			final SysFactory theFactory =
                     new SysFactory(createLogger(), anySharedConfig, debugModeParam);
 			sharedContext.bind(facadeName, theFactory);
 			return theFactory.getInstance();
 		}
 		catch (NameAlreadyBoundException n) {
 			return ((SysFactory) sharedContext.lookup(facadeName)).getInstance();
 		}
 	}
 
 	@SuppressWarnings({ "ThisEscapedInObjectConstruction" })
     private GNDMSystem(@NotNull GridConfig anySharedConfig, boolean debugModeParam)  {
 		sharedConfig = anySharedConfig;
         debugMode = debugModeParam;
         instanceDir = new InstanceDirectory(getSystemName());
         instanceDir.addInstance("sys", this);
         // Bad style, usually would be an inner class but
         // removed it from this source file to reduce source file size
         actionCaller = new ConfigActionCaller(this);
 
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
 
         if (isDebugging()) {
             LogicTools.setDerbyToDebugMode();
         }
 
 		dbLogFile = new File(logDir, "derby.log");
 		if (!dbLogFile.exists())
 			dbLogFile.createNewFile();
 		System.setProperty("derby.stream.error.file",
 				  dbLogFile.getCanonicalPath());
 	}
 
 
 	public @NotNull EntityManagerFactory createEMF() throws Exception {
 		final String gridName = sharedConfig.getGridName();
 		final Properties map = new Properties();
 
 		map.put("openjpa.Id", gridName);
 		map.put("openjpa.ConnectionURL", "jdbc:derby:" + gridName+";create=true");
 
         if (isDebugging()) {
             File jpaLogFile = new File(getLogDir(), "jpa.log");
             if (! jpaLogFile.exists())
                 jpaLogFile.createNewFile();
             map.put("openjpa.Log", "File=" + jpaLogFile +
                     ", DefaultLevel=INFO, Runtime=TRACE, Tool=INFO");
         }
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
 		groovyMonitor = new GroovyMoniServer(getGridName(), monitorConfig,
                                              getInstanceDir().createBindingFactory(),
                                              getActionCaller());
 		groovyMonitor.startConfigRefreshThread(true);
 	}
 
 
 
 
 	private void doCheckOrCreateDir(File mainDir) {
 		if (!mainDir.exists()) {
 			logger.info("Creating " + mainDir.getPath());
 			mainDir.mkdir();
 		}
 		if (!mainDir.isDirectory() || !mainDir.canRead())
 			throw new IllegalStateException(mainDir + " is not accessible");
 	}
 
 
     @SuppressWarnings({ "MethodOnlyUsedFromInnerClass" })
 	private synchronized void shutdown() throws Exception {
 		emf.close();
 		final GroovyMoniServer moniServer = getMonitor();
 		if (moniServer != null)
 			moniServer.stopServer();
 	}
 
 
    private synchronized @NotNull ExecutorService getExecutorService() {
         if (executorService == null)
             executorService = createExecutorService();
         return executorService;
     }
 
     public final @NotNull <R> Future<R> submitAction(final @NotNull EntityAction<R> action) {
         final @NotNull EntityManager em = getEntityManagerFactory().createEntityManager();
         try {
             return submitAction(em, action);
         }
         finally {
             if (em.isOpen())
                 em.close();
         }
     }
 
     @SuppressWarnings({ "FeatureEnvy" })
     public @NotNull <R> Future<R> submitAction(final @NotNull EntityManager em,
                                                final @NotNull EntityAction<R> action) {
         action.setOwnEntityManager(em);
         if (action instanceof SystemHolder)
             ((SystemHolder)action).setSystem(this);
         if (action.getPostponedActions() == null)
             action.setOwnPostponedActions(new DefaultBatchUpdateAction<GridResource>());
         if (action.getPostponedActions().getListener() == null)
             action.getPostponedActions().setListener(getEntityUpdateListener());
         action.initialize();
         return getExecutorService().submit(action);
     }
 
 
     @SuppressWarnings({ "MethodMayBeStatic" })
     private @NotNull ExecutorService createExecutorService() {
         return Executors.newCachedThreadPool();
     }
 
 
     public boolean isDebugging() {
         return debugMode;
     }
 
 
     public synchronized @NotNull File getSharedDir() {
         return sharedDir;
     }
 
 	public @NotNull String getSystemName() {
 		try { return '\'' + sharedConfig.getGridName() + "' system"; }
 		catch (Exception e) { throw new RuntimeException(e); }
 	}
 
     public @NotNull String getGridName() {
         try { return sharedConfig.getGridName(); }
         catch (Exception e) { throw new RuntimeException(e); }
     }
 
 
     public @NotNull EntityManagerFactory getEntityManagerFactory() {
         return restrictedEmf;
     }
 
 
     public @NotNull InstanceDirectory getInstanceDir() {
         return instanceDir;
     }
 
     public @NotNull ActionCaller getActionCaller() {
         return actionCaller;
     }
 
 
 	public @NotNull GridConfig getSharedConfig() {
 		return sharedConfig;
 	}
 
 
 	public @NotNull File getLogDir() {
 		return logDir;
 	}
 
 
 	public @NotNull File getDbDir() {
 		return dbDir;
 	}
 
 
 	public @NotNull File getDbLogFile() {
 		return dbLogFile;
 	}
 
 
 	public synchronized @Nullable GroovyMoniServer getMonitor() {
 		return groovyMonitor;
 	}
 
 	@SuppressWarnings({"ReturnOfThis"})
 	public @NotNull GNDMSystem getSystem() {
 		return this;
 	}
 
 	public void setSystem(@NotNull GNDMSystem system) throws IllegalStateException {
 		throw new IllegalStateException("Cant set this system");
 	}
 
 	public @NotNull String nextUUID() {
 		return uuidGen.nextUUID();
 	}
 
 
 	public static @NotNull EndpointReferenceType serviceEPRType(@NotNull URI defAddr, @NotNull VEPRef dSpaceRef)
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
 
 
 	public @NotNull EndpointReferenceType serviceEPRType(@NotNull String instPrefix,
 	                                           @NotNull VEPRef dSpaceRef)
 		  throws URI.MalformedURIException {
 		return serviceEPRType(
                 getInstanceDir().lookupServiceHome(instPrefix).getServiceAddress(), dSpaceRef);
 	}
 
 
 	public static @NotNull VEPRef modelEPRT(@NotNull QName keyTypeName,
                                             @NotNull EndpointReferenceType epr) {
 		@NotNull ReferencePropertiesType props = epr.getProperties();
 		@NotNull MessageElement msgElem = props.get(keyTypeName);
 		SimpleResourceKey key = new SimpleResourceKey(keyTypeName, msgElem.getObjectValue());
 
 		// theVEPREF.setSite("");
 		// theVEPREF.setRk(key);
 		return new DSpaceRef();
 	}
 
 
 	public @NotNull VEPRef modelEPRT(@NotNull String instPrefix,
                                      @NotNull EndpointReferenceType epr) {
 		return modelEPRT(getInstanceDir().lookupServiceHome(instPrefix).getKeyTypeName(), epr);
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
         GNDMServiceHome<M> home = getInstanceDir().getHome(modelClazz);
         home.refresh(model);
     }
 
 
 
     @SuppressWarnings({ "unchecked", "MethodMayBeStatic" })
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
                 retList = listAllResources(getInstanceDir().getHome(clazz), manager);
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
                         if (isDebugging())
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
 
 
     @SuppressWarnings({ "ReturnOfThis" })
     public EntityUpdateListener<GridResource> getEntityUpdateListener() {
         return this;
     }
 
 
     public static final class SysFactory {
 		private final Log logger;
 
 		private GNDMSystem instance;
 		private RuntimeException cachedException;
 		private GridConfig sharedConfig;
         private boolean debugMode;
 
 		public SysFactory(
                 @NotNull Log theLogger, @NotNull GridConfig anySharedConfig,
                 final boolean debugModeParam) {
 			logger = theLogger;
 			sharedConfig = anySharedConfig;
             debugMode = debugModeParam;
 		}
 
 		public synchronized GNDMSystem getInstance() {
 			return getInstance(true);
 		}
 		
 		public synchronized GNDMSystem getInstance(boolean setupShellService) {
 			if (cachedException != null)
 				throw cachedException;
 			if (instance == null) {
 				try {
 					GNDMSystem newInstance = new GNDMSystem(sharedConfig, debugMode);
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
 }
