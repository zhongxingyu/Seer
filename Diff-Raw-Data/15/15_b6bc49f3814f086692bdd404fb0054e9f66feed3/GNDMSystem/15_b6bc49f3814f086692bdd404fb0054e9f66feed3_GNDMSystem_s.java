 package de.zib.gndms.infra.system;
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 
 import de.zib.gndms.GNDMSVerInfo;
 import de.zib.gndms.infra.GridConfig;
 import de.zib.gndms.kit.access.EMFactoryProvider;
 import de.zib.gndms.logic.action.ActionCaller;
 import de.zib.gndms.logic.model.*;
 import de.zib.gndms.logic.model.gorfx.DefaultWrapper;
 import de.zib.gndms.logic.util.LogicTools;
 import de.zib.gndms.model.common.*;
 import de.zib.gndms.neomodel.common.Dao;
 import de.zib.gndms.neomodel.gorfx.Taskling;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.jetbrains.annotations.NotNull;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 import org.slf4j.MDC;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceUnit;
 
 import static javax.persistence.Persistence.createEntityManagerFactory;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import static java.lang.Thread.sleep;
 
 import java.util.UUID;
 import java.util.concurrent.*;
 
 
 /**
  * This sets up the configuration and database storage area shared between
  * GNDMS services.
  *
  * @author  try ste fan pla nti kow zib
  * @version $Id$
  *
  *          User: stepn Date: 17.06.2008 Time: 23:09:00
  */
 @SuppressWarnings({
         "OverloadedMethodsWithSameNumberOfParameters", "NestedAssignment",
         "ClassWithTooManyMethods" })
 public final class GNDMSystem
 	  implements SystemHolder, EMFactoryProvider, BeanFactoryAware,
         ModelUpdateListener<GridResource> {
     private static final long EXECUTOR_SHUTDOWN_TIME = 5000L;
     private ConfigActionCaller actionCaller;
     private AutowireCapableBeanFactory beanFactory;
 
 
     private static @NotNull Logger createLogger() { return LoggerFactory.getLogger(GNDMSystem.class); }
 
     private boolean shutdown;
 
     private final boolean debugMode;
 
     private @NotNull final ModelUUIDGen uuidGenDelegate;
 	private @NotNull final Logger logger = createLogger();
 	private @NotNull final GNDMSVerInfo verInfo = new GNDMSVerInfo();
     /**
      * the GNDMSystemDirectory connected with this GNDMSystem
      */
     private @NotNull GNDMSystemDirectory instanceDir;
 	private @NotNull final GridConfig sharedConfig;
 	private @NotNull File sharedDir;
     private @NotNull File dbDir;
     private @NotNull File neoDir;
     private @NotNull File logDir;
 	private @NotNull File dbLoggerFile;
     private @NotNull File containerHome;
 	private @NotNull EntityManagerFactory emf;
     private @NotNull GraphDatabaseService neo = null;
     private @NotNull Dao dao;
 //	private NetworkAuxiliariesProvider netAux;
 
 
 	// Outside injector
     private @NotNull TaskExecutionService executionService; // accessible only via system
 
 
 
 
 
 	@SuppressWarnings({ "ThisEscapedInObjectConstruction" })
     private GNDMSystem(@NotNull GridConfig anySharedConfig, boolean debugModeParam)  {
 		sharedConfig = anySharedConfig;
         debugMode = debugModeParam;
 		uuidGenDelegate = new ModelUUIDGen() {
 		    public @NotNull String nextUUID() {
 		        return GNDMSystem.this.nextUUID();
 		    }
 		};
 	}
 
 
     @PostConstruct
 	public void initialize() throws RuntimeException {
 		try {
 			printVersion();
             containerHome = new File(System.getenv("GLOBUS_LOCATION")).getAbsoluteFile();
             logger.info("Container home directory is '" + containerHome.toString() + '\'');
             initSharedDir();
 			createDirectories();
 			prepareDbStorage();
             dao = new Dao(getGridName(), neo);
 			tryTxExecution();
 			// initialization intentionally deferred to initialize
 	        instanceDir = ( GNDMSystemDirectory ) beanFactory.configureBean(
                 new GNDMSystemDirectory(getSystemName(),
                     new DefaultWrapper<SystemHolder, Object>(SystemHolder.class) {
 
 		        @Override
 		        protected <Y> Y wrapInterfaceInstance(final Class<Y> wrapClass, @NotNull final SystemHolder wrappedParam) {
 			        wrappedParam.setSystem(GNDMSystem.this);
 			        return wrapClass.cast(wrappedParam);
 		        }
 	        } ), "instanceDir" );
 	        instanceDir.addInstance("sys", this);
 			instanceDir.reloadConfiglets(emf);
 			// Bad style, usually would be an inner class but
 			// removed it from this source file to reduce source file size
 			actionCaller.init( this );
             logger.info("getSubGridName() /* gridconfig subGridName */ is '" + getInstanceDir().getSubGridName() + '\'');
 		}
 		catch (Exception e) {
 			logger.error("Initialization failed", e);
 			throw new RuntimeException(e);
 		}
 	}
 
     private GraphDatabaseService loadNeo() {
         logger.info("Loading neo4j graph database");
         return new EmbeddedGraphDatabase(getNeoDir().getAbsolutePath());
     }
 
 
     /**
      * Writes information about the GNDMS release and GNDMS build (using {@link #verInfo}) to the logger.
      */
 	@SuppressWarnings({ "ValueOfIncrementOrDecrementUsed", "MagicNumber" })
 	private void printVersion() {
 		final String releaseInfo = verInfo.readRelease();
 		final String curBuildInfo = verInfo.readBuildInfo();
 		int maxSize = Math.max(releaseInfo.length(), curBuildInfo.length()) + 14;
 		final StringBuilder builder = new StringBuilder(maxSize);
 		while (maxSize-- >= 0)
 			builder.append('=');
 		final String hrString = builder.toString();
 		logger.warn(hrString);
 		logger.warn("GNDMS RELEASE: " + releaseInfo);
 		logger.warn("GNDMS BUILD: " + curBuildInfo);
 		logger.warn(hrString);
 	}
 
 
 //  todo remove when spring injection is sufficent..
 //   /**
 //     * Binds several classes with {@code this} or other corresponding fields
 //     *
 //     * @param binder binds several class with certain fields.
 //     */
 //   public void configure(final @NotNull Binder binder) {
 //       //binder.bind(GNDMSystem.class).toInstance(this);
 //       //binder.bind(EntityManagerFactory.class).toInstance(emf);
 //       //binder.bind(EMFactoryProvider.class).toInstance(this);
 //       //binder.bind(GridConfig.class).toInstance(sharedConfig);
 //       //binder.bind(NetworkAuxiliariesProvider.class).toInstance(getNetAux());
 //       binder.bind(ModelUpdateListener.class).toInstance( this );
 //       binder.bind(BatchUpdateAction.class).to( DefaultBatchUpdateAction.class );
 //       binder.bind(GNDMSVerInfo.class).toInstance(verInfo);
 //       binder.bind(Logger.class).toInstance(logger);
 //       binder.bind( DirectoryAux.class).toInstance( new LinuxDirectoryAux() );
 //       // TODO later: binder.bind(TxFrame.class).to(TxFrame.class);
 //   }
 
     /**
      * Retrieves the grid path using {@code sharedConfig} and stores the corresponding file in {@code sharedDir}.
      *
      * @throws Exception if an error occurs while creating the {@link File} instance.
      */
 	private synchronized void initSharedDir() throws Exception {
         sharedDir = new File(sharedConfig.getGridPath()).getCanonicalFile();
         final String gridName = sharedConfig.getGridName();
 
         logger.info("Initializing for grid: '" + gridName
               + "' (shared dir: '" + sharedDir.getPath() + "')");
 
     }
 
     /**
      * Creates the path {@link #sharedDir} and the file {@link #logDir} on the file system.
      */
 	private void createDirectories() {
         File curSharedDir = getSharedDir();
 
 		doCheckOrCreateDir(curSharedDir);
 
 		logDir = new File(curSharedDir, "log");
 		doCheckOrCreateDir(logDir);
 	}
 
     /**
      * Create the files {@link #dbDir} and {@link #dbLoggerFile} on the file system and stores the paths
      * in the system property "derby.system.home" respectively "derby.stream.error.file".
      *
      * @throws IOException if an error occurs while accessing the file system
      */
 	@SuppressWarnings({ "ResultOfMethodCallIgnored" })
     private void prepareDbStorage() throws IOException {
         File curSharedDir = getSharedDir();
 		dbDir = new File(curSharedDir, "db");
         neoDir = new File(curSharedDir, "neo");
 		doCheckOrCreateDir(dbDir);
 
 		System.setProperty("derby.system.home", dbDir.getCanonicalPath());
 
         if (isDebugging()) {
             LogicTools.setDerbyToDebugMode();
         }
 
 		dbLoggerFile = new File(logDir, "derby.log");
 		if (!dbLoggerFile.exists())
 			dbLoggerFile.createNewFile();
 		System.setProperty("derby.stream.error.file",
 				  dbLoggerFile.getCanonicalPath());
 	}
 
 // Moved to system.xml
 //    /**
 //     * Creates an EntityManagerFactory.
 //     *
 //     * @return an EntityManagerFactory
 //     * @throws Exception
 //     */
 //	@SuppressWarnings({ "ResultOfMethodCallIgnored" })
 //    public @NotNull EntityManagerFactory createEMF() throws Exception {
 //
 //
 //		final String gridName = sharedConfig.getGridName();
 //		final Properties map = new Properties();
 //
 //		map.put("openjpa.Id", gridName);
 //		map.put("openjpa.ConnectionURL", "jdbc:derby:" + gridName+";create=true");
 //
 //        if (isDebugging()) {
 //            File jpaLoggerFile = new File(getLogDir(), "jpa.log");
 //            if (! jpaLoggerFile.exists())
 //                jpaLoggerFile.createNewFile();
 //            map.put("openjpa.Logger", "File=" + jpaLoggerFile +
 //                    ", DefaultLevel=INFO, Runtime=TRACE, Tool=INFO");
 //        }
 //		logger.info("Opening JPA Store: " + map.toString());
 //
 //		return createEntityManagerFactory(gridName, map);
 //	}
 
     /**
      * Checks if a commit can be done on the database
      *
      * @throws RuntimeException if an error occured while commiting on the database
      */
 	private void tryTxExecution() throws RuntimeException{
 		final EntityManager em = emf.createEntityManager();
 		try {
 			em.getTransaction().begin();
 			em.getTransaction().commit();
 		}
 		catch (RuntimeException re)
 			{ em.getTransaction().rollback();
               em.close();
               throw re;
         }
 		finally
 			{ em.close(); }
 	}
 
     
     /**
      * Checks if the path denoted by <tt>mainDir</tt> exists and may create the path on the filesystem,
      * if not already done.
      *
      * @param mainDir a <tt>File</tt> instance denoting a path
      */
 	@SuppressWarnings({ "ResultOfMethodCallIgnored" })
     private void doCheckOrCreateDir(File mainDir) {
 		if (!mainDir.exists()) {
 			logger.info("Creating " + mainDir.getPath());
 			mainDir.mkdir();
 		}
 		if (!mainDir.isDirectory() || !mainDir.canRead())
 			throw new IllegalStateException(mainDir + " is not accessible");
 	}
 
 
     /**
      * Invokes a shutdown on the ExecutorService {@link #executionService}, on the configlets of the <tt>GNDMSystemDirectory</tt>,
      * and closes the EntityManagerFactory {@link #emf}.
      *
      * @throws Exception if an error occurs while stopping the monitorserver
      */
     @SuppressWarnings({
             "MethodOnlyUsedFromInnerClass", "SleepWhileHoldingLock",
             "CallToNativeMethodWhileLocked" })
 	private synchronized void shutdown() throws Exception {
         // todo put this in spring context
         if (! shutdown) {
             shutdown = true;
             executionService.shutdown();
             instanceDir.shutdownConfiglets();
             emf.close();
             neo.shutdown();
         }
 	}
 
     /**
      * Returns the {@code TaskExecutionService}, which is used by this system. If it does not exist, 
      * a new instance will be created.
      *
      * @return the TaskExecutionService instance, which is uses by this system.
      */
     private synchronized @NotNull TaskExecutionService getExecutionService() {
         return executionService;
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
         return emf;
     }
 
 
     public @NotNull GNDMSystemDirectory getInstanceDir() {
         return instanceDir;
     }
 
     public @NotNull
     ActionCaller getActionCaller() {
         return actionCaller;
     }
 
 
 	public @NotNull GridConfig getSharedConfig() {
 		return sharedConfig;
 	}
 
 
 	public @NotNull File getLogDir() {
 		return logDir;
 	}
 
 
     public @NotNull File getNeoDir() {
         return neoDir;
     }
 
 	public @NotNull File getDbDir() {
 		return dbDir;
 	}
 
 
 	public @NotNull File getDbLoggerFile() {
 		return dbLoggerFile;
 	}
 
 
 	@SuppressWarnings({"ReturnOfThis"})
 	public @NotNull GNDMSystem getSystem() {
 		return this;
 	}
 
 	public void setSystem(@NotNull GNDMSystem system) throws IllegalStateException {
 		throw new IllegalStateException("Cant set this system");
 	}
 
     /**
      * Returns the next UUID using
      * 
      * @return the next UUID
      */
 	public @NotNull String nextUUID() {
 		return UUID.randomUUID().toString();
 	}
 
 
 
 
     public void onModelChange(GridResource model) {
         // implement if required
     }
 
 
     @Inject
     public void setActionCaller( ConfigActionCaller actionCaller ) {
         this.actionCaller = actionCaller;
     }
 
 
 
     /**
      * Returns {@code this}
      *
      * @return {@code this}
      */
     @SuppressWarnings({ "ReturnOfThis" })
     public ModelUpdateListener<GridResource> getEntityUpdateListener() {
         return this;
     }
 
 
     /**
      * Returns {@link #uuidGenDelegate}
      *
      * @return {@link #uuidGenDelegate}
      */
     public @NotNull ModelUUIDGen getModelUUIDGen() {
         return uuidGenDelegate;
     }
 
 
     /**
      * Calls {@code sumitAction(action,logParam)} on {@code getExecutionService()}.
      *
      * @see SysTaskExecutionService#submitAction(de.zib.gndms.logic.model.EntityAction, org.slf4j.Logger)
      */
     public @NotNull <R> Future<R> submitAction(final @NotNull EntityAction<R> action, final @NotNull Logger logParam) {
         return getExecutionService().submitAction(action, logParam);
     }
 
     /**
      * Calls {@code sumitAction(em,action,logParam)} on {@code getExecutionService()}
      * 
      * @see SysTaskExecutionService#submitAction(javax.persistence.EntityManager, de.zib.gndms.logic.model.EntityAction, org.slf4j.Logger) 
      *
      */
     public @NotNull <R> Future<R> submitAction(final @NotNull EntityManager em,
                                                final @NotNull EntityAction<R> action,
                                                final @NotNull Logger logParam) {
         return getExecutionService().submitAction(em, action, logParam);
     }
 
 
     @NotNull
     public <R> Future<R> submitDaoAction(@NotNull EntityManager em, @NotNull Dao dao, @NotNull ModelDaoAction<?, R> action, @NotNull Logger log) {
         return executionService.submitDaoAction(em, dao, action, log);
     }
 
     @NotNull
     public <R> Future<R> submitDaoAction(@NotNull ModelDaoAction<?, R> action, @NotNull Logger log) {
         return executionService.submitDaoAction(action, log);
     }
 
     public void reloadConfiglets() {
 		instanceDir.reloadConfiglets(getEntityManagerFactory());
 	}
 
 
     @Override
     public void setBeanFactory( BeanFactory beanFactory ) throws BeansException {
         this.beanFactory = ( AutowireCapableBeanFactory ) beanFactory;
     }
 
 
     @Inject
     public void setExecutionService( @NotNull TaskExecutionService executionService ) {
         this.executionService = executionService;
     }
 
 
     /**
      * A factory class for the <tt>GNDMSystem</tt>.
      *
      *
      * 
      * @see de.zib.gndms.infra.system.GNDMSystem
      */
     public static final class SysFactory {
 		protected final Logger logger = LoggerFactory.getLogger( this.getClass() );
 
 		private GNDMSystem instance;
 		private RuntimeException cachedException;
 		private GridConfig sharedConfig;
         private final boolean debugMode;
 
 		public SysFactory(
                 @NotNull GridConfig anySharedConfig,
                 final boolean debugModeParam) {
 			sharedConfig = anySharedConfig;
             debugMode = debugModeParam;
 		}
 
 
         /**
          * Returns the current GNDMSystem if it has already been created.
          * Otherwise a new instance will be created.
          *
          * <p>The system will be loaded with the values set in the fields {@link #sharedConfig} and {@link #debugMode}.
          * If <tt>setupShellService</tt> is set to <tt>true</tt>, {@code setupShellService()} will be invoked on the new
          * system. The new created system will be stored at {@link #instance}.
          *
          * @return the current used GNDMS system
          */
 		public synchronized GNDMSystem getInstance() {
 			if (cachedException != null)
 				throw cachedException;
 			if (instance == null) {
 				try {
 					GNDMSystem newInstance = new GNDMSystem(sharedConfig, debugMode);
					newInstance.initialize();
 					try { logger.info(sharedConfig.getGridName() + " initialized"); }
 					catch (Exception e) { logger.error( "", e ); }
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
 
         /**
          * Shuts down the current GNDM system by invoking {@code shutdown()} on {@link #instance}.
          *
          * @throws Exception if an error occurs during the initialization or setup of the shell service of the GNDM system
          */
 		@SuppressWarnings({ "MethodOnlyUsedFromInnerClass" })
 		private synchronized void shutdown() throws Exception {
 			if (instance != null)
 				getInstance().shutdown();
 		}
 
 
         /**
          * Returns a <tt>Runnable</tt> which calls {@link #shutdown()}, when it is executed
          * 
          * @return a <tt>Runnable</tt> which calls {@link #shutdown()}, when it is executed
          */
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
 
     @NotNull
     public File getContainerHome() {
         return containerHome;
     }
 
     public boolean isGridAdmin(final String tag, final String dn) {
         final boolean ret = isGridAdmin_(dn == null ? "null" : dn);
         MDC.put( tag, "" );
         try {
             if (ret)
                 logger.info("Authenticated Grid Admin " + dn);
             else
                 logger.info("Denied Access to Non-Grid Admin " + dn);
 
             return ret;
         }
         finally { MDC.remove( tag ); }
     }
 
     private boolean isGridAdmin_(final String dn) {
         // This only works on UNIX
         for (final File gridAdminFile : new File[] {
                 new File(File.separatorChar + "etc" + File.separatorChar + "grid-security" + File.separatorChar + getInstanceDir().getSubGridName() + "-support-staff"),
                 new File(getSharedDir(), getInstanceDir().getSubGridName() + "-support-staff")
         })
             if (isDNInFile(dn, gridAdminFile))
                 return true;
 
         return false;
     }
 
     private boolean isDNInFile(final String dn, final File file) {
         logger.debug("Checking if '" + dn + "' is in '" + file.getAbsolutePath() + '\'');
         if (! (file.isFile() && file.exists() && file.canRead()))
             return false;
 
         BufferedReader rd;
         try {
             rd = new BufferedReader(new FileReader(file));
             try {
                 String line;
 
                 while ((line = rd.readLine()) != null) {
                     String trLine = line.trim();
                     if (trLine.length() > 0 && trLine.charAt(0) == '\"') {
                         int endIndex = trLine.lastIndexOf('\"');
                         if (endIndex > 0)
                             trLine = trLine.substring(1, endIndex).trim();
                     }
                     if (trLine.equals(dn))
                         return true;
                 }
             }
             finally { rd.close(); }
         }
         catch (final IOException e) {
             throw new RuntimeException(e);
         }
 
         return false;
     }
 
     @NotNull
     public GraphDatabaseService getNeo() {
         return neo;
     }
 
 
     @Inject
     public void setNeo( @NotNull GraphDatabaseService neo ) {
 
        if( neo != null )
             throw new IllegalStateException( "Graph DB already set" );
 
         this.neo = neo;
     }
 
 
     public @NotNull
     Dao getDao() {
         return dao;
     }
     /*
     public NetworkAuxiliariesProvider getNetAux( ) {
 
         if( netAux == null )
             netAux = new NetworkAuxiliariesProvider( );
 
         return netAux;
     }
     */
 
     private final ModelUpdateListener<Taskling> tasklingUpdater = new ModelUpdateListener<Taskling>() {
         public void onModelChange(Taskling model) {
             // TODO IMPLEMENT
         }
     };
 
 
     @PersistenceUnit
     public void setEmf( @NotNull EntityManagerFactory emf ) {
         logger.debug( "setEmf called "  );
         this.emf = emf;
     }
 }
