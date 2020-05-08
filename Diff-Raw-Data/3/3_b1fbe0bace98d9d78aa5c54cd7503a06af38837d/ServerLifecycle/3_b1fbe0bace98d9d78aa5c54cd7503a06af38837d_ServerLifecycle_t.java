 package org.apache.ode.embed;
 
 import org.apache.log4j.Logger;
 import org.apache.ode.EmbeddedServer;
 import org.apache.ode.Options;
 import org.apache.ode.rest.EngineWebResource;
 import org.apache.ode.embed.messaging.BindingContextImpl;
 import org.apache.ode.embed.messaging.MessageExchangeContextImpl;
 import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
 import org.apache.ode.bpel.engine.BpelServerImpl;
 import org.apache.ode.bpel.engine.CountLRUDehydrationPolicy;
 import org.apache.ode.bpel.evtproc.DebugBpelEventListener;
 import org.apache.ode.bpel.iapi.*;
 import org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl;
 import org.apache.ode.il.dbutil.Database;
 import org.apache.ode.scheduler.simple.JdbcDelegate;
 import org.apache.ode.scheduler.simple.SimpleScheduler;
 import org.apache.ode.utils.GUID;
 import org.hsqldb.jdbc.jdbcDataSource;
 
 import javax.sql.DataSource;
 import javax.transaction.TransactionManager;
 import java.sql.*;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 /**
  * @author Matthieu Riou <mriou@apache.org>
  */
 public class ServerLifecycle {
     private static final Logger __log = Logger.getLogger(EmbeddedServer.class);
 
     protected Options _options;
     protected TransactionManager _txMgr;
     protected BpelServerImpl _server;
     protected Database _db;
     protected DataSource _ds;
     protected BpelDAOConnectionFactory _daoCF;
     protected ExecutorService _executorService;
     protected Scheduler _scheduler;
     protected EmbeddedStore _store;
     protected EngineWebResource _webEngine;
 
     public ServerLifecycle(Options options) {
         _options = options;
         if (_options.getThreadPoolMaxSize() <= 0) _executorService = Executors.newCachedThreadPool();
         else _executorService = Executors.newFixedThreadPool(_options.getThreadPoolMaxSize());
     }
 
     public void start() {
         __log.debug("Initializing transaction manager");
         initTxMgr();
         __log.debug("Creating data source.");
         initDataSource();
         __log.debug("Starting DAO.");
         initDAO();
         __log.debug("Initializing BPEL process store.");
         initProcessStore();
 
         if (_options.isRestful()) initRestfulServer();
 
         __log.debug("Initializing BPEL server.");
         initBpelServer();
 
         // Register BPEL event listeners configured in axis2.properties file.
         registerEventListeners();
 
         _server.start();
         _store.start();
         __log.info("Up and ready to serve.");
     }
 
     public void clean() {
         EngineWebResource.stopRestfulServer();
         if (_db != null) _db.shutdown();
        _db = null;
         _server = null;
         _txMgr = null;
         _executorService = null;
         _store = null;
         _ds = null;
         _scheduler = null;
         _scheduler = null;
         _webEngine = null;
     }
 
     private void initBpelServer() {
         _server = new BpelServerImpl();
         _scheduler = createScheduler();
         _scheduler.setJobProcessor(_server);
 
         _server.setDaoConnectionFactory(_daoCF);
 //        _server.setEndpointReferenceContext(new EndpointReferenceContextImpl(this));
         _server.setMessageExchangeContext(new MessageExchangeContextImpl(_options.getMessageSender()));
         
         BindingContextImpl bc = new BindingContextImpl(_options);
         _server.setBindingContext(bc);
 
         _server.setScheduler(_scheduler);
         _server.setTransactionManager(_txMgr);
         if (_options.isDehydrationEnabled()) {
             CountLRUDehydrationPolicy dehy = new CountLRUDehydrationPolicy();
             _server.setDehydrationPolicy(dehy);
         }
         _server.setConfigProperties(_options.getProperties());
         _server.init();
     }
 
     protected void initDAO() {
         // TODO supporting only in memory for now, extend to datasource usage
         _daoCF = new BpelDAOConnectionFactoryImpl(_txMgr);
     }
 
     protected void initTxMgr() {
         if(_txMgr == null) {
             try {
                 GeronimoTxFactory txFactory = new GeronimoTxFactory();
                 _txMgr = txFactory.getTransactionManager();
             } catch (Exception e) {
                 __log.fatal("Couldn't initialize a transaction manager using Geronimo's transaction factory.", e);
                 throw new RuntimeException("Couldn't initialize a transaction manager using " + "Geronimo's transaction factory.", e);
             }
         }
     }
 
     protected void initDataSource() {
         jdbcDataSource hsqlds = new jdbcDataSource();
         hsqlds.setDatabase("jdbc:hsqldb:mem:" + new GUID().toString());
         hsqlds.setUser("sa");
         hsqlds.setPassword("");
         _ds = hsqlds;
     }
 
     protected Scheduler createScheduler() {
         // TODO as long as we're using HSQL that's fine, afterward...
         Connection conn = null;
         Statement stmt = null;
         ResultSet result = null;
         try {
             conn = _ds.getConnection();
             DatabaseMetaData metaData = conn.getMetaData();
             if (metaData != null) {
                 result = metaData.getTables("APP", null, "ODE_JOB", null);
 
                 if (!result.next()) {
                     String dbProductName = metaData.getDatabaseProductName();
                     if (dbProductName.indexOf("Derby") >= 0) {
                         stmt = conn.createStatement();
                         stmt.execute(DERBY_SCHEDULER_DDL1);
                         stmt.close();
                         stmt = conn.createStatement();
                         stmt.execute(DERBY_SCHEDULER_DDL2);
                         stmt.close();
                         stmt = conn.createStatement();
                         stmt.execute(DERBY_SCHEDULER_DDL3);
                         stmt.close();
                     }
                     if (dbProductName.indexOf("HSQL") >= 0) {
                         stmt = conn.createStatement();
                         stmt.execute(HSQL_SCHEDULER_DDL);
                     }
                 }
             }
 
         } catch (SQLException e) {
             // Swallowing it, either it already exists in which case we don't care or
             // creation failed and we'll find out soon enough
         } finally {
             try {
                 if (result != null) result.close();
                 if (stmt != null) stmt.close();
                 if (conn != null) conn.close();
             } catch (SQLException se) {
                 __log.info(se);
             }
         }
 
         SimpleScheduler scheduler = new SimpleScheduler(new GUID().toString(),new JdbcDelegate(_ds), new Properties());
         scheduler.setTransactionManager(_txMgr);
         _scheduler = scheduler;
         return scheduler;
     }
 
     protected void initProcessStore() {
         // TODO Support persistent store as well
         _store = new EmbeddedStore();
         _store.registerListener(new ProcessStoreListenerImpl());
     }
 
     private void initRestfulServer() {
         EngineWebResource.startRestfulServer(this);
     }
     
     public void setEngineWebResource(EngineWebResource webEngine) {
         _webEngine = webEngine;
     }
 
     /**
      * Register event listeners configured in the configuration.
      *
      */
     private void registerEventListeners() {
         // let's always register the debugging listener....
         _server.registerBpelEventListener(new DebugBpelEventListener());
 
         // then, whatever else they want.
         List<BpelEventListener> listeners = _options.getBpelEventListeners();
         if (listeners != null) {
             for (BpelEventListener listener : listeners) {
                _server.registerBpelEventListener(listener);
             }
         }
     }
 
     public class ProcessStoreListenerImpl implements ProcessStoreListener {
         public void onProcessStoreEvent(ProcessStoreEvent event) {
             handleEvent(event);
         }
     }
 
     private void handleEvent(ProcessStoreEvent pse) {
         __log.debug("Process store event: " + pse);
         switch (pse.type) {
             case ACTIVATED:
             case RETIRED:
                 // bounce the process
                 _server.unregister(pse.pid);
                 ProcessConf pconf = _store.getProcessConfiguration(pse.pid);
                 if (pconf != null) _server.register(pconf);
                 else __log.debug("slighly odd: received event " + pse + " for process not in store!");
                 break;
             case DISABLED:
             case UNDEPLOYED:
                 _server.unregister(pse.pid);
                 break;
             default:
                 __log.debug("Ignoring store event: " + pse);
         }
     }
 
     public EmbeddedStore getStore() {
         return _store;
     }
 
     public BpelServerImpl getServer() {
         return _server;
     }
 
     private static final String HSQL_SCHEDULER_DDL =
             "CREATE TABLE ODE_JOB (" +
                     " jobid CHAR(64) DEFAULT '' NOT NULL," +
                     " ts BIGINT DEFAULT 0 NOT NULL ," +
                     " nodeid char(64)  NULL," +
                     " scheduled int DEFAULT 0 NOT NULL," +
                     " transacted int DEFAULT 0 NOT NULL," +
                     " details LONGVARBINARY NULL," +
                     " PRIMARY KEY(jobid));\n" +
                     "CREATE INDEX IDX_ODE_JOB_TS ON ODE_JOB (ts);\n" +
                     "CREATE INDEX IDX_ODE_JOB_NODEID ON ODE_JOB (nodeid);";
 
     private static final String DERBY_SCHEDULER_DDL1 =
             "CREATE TABLE ODE_JOB (" +
                     " jobid CHAR(64) DEFAULT '' NOT NULL," +
                     " ts BIGINT DEFAULT 0 NOT NULL ," +
                     " nodeid char(64)," +
                     " scheduled int DEFAULT 0 NOT NULL," +
                     " transacted int DEFAULT 0 NOT NULL," +
                     " details BLOB(50K)," +
                     " PRIMARY KEY (jobid))";
     private static final String DERBY_SCHEDULER_DDL2 = "CREATE INDEX IDX_ODE_JOB_TS ON ODE_JOB (ts)";
     private static final String DERBY_SCHEDULER_DDL3 = "CREATE INDEX IDX_ODE_JOB_NODEID ON ODE_JOB (nodeid)";
 
 }
