 /*
  *  Copyright 2011 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package cx.ath.mancel01.webframework.data;
 
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 import cx.ath.mancel01.webframework.WebFramework;
 import java.io.File;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.FlushModeType;
 import javax.sql.DataSource;
 import org.hibernate.ejb.Ejb3Configuration;
 
 /**
  *
  * @author mathieuancelin
  */
 public class JPAService {
 
     private static JPAService INSTANCE;
 
 //    private Server hsqlServer = null;
 
     private boolean started = false;
 
     private DataSource dataSource;
 
     private EntityManagerFactory emf;
 
     private TxManager txManager;
 
     public static ThreadLocal<EntityManager> currentEm =
             new ThreadLocal<EntityManager>() {
 
         @Override
         protected EntityManager initialValue() {
             return null;
         }
     };
 
     private static final ThreadLocal<Boolean> rollbackFlag =
             new ThreadLocal<Boolean>() {
 
         @Override
         protected Boolean initialValue() {
             return false;
         }
     };
 
     private JPAService() {
         this.txManager = new TxManager();
     }
 
     public static synchronized JPAService getInstance() {
         if (INSTANCE == null) {
             INSTANCE = new JPAService();
         }
         return INSTANCE;
     }
 
     public static synchronized void start() {
         JPAService service = getInstance();
 //        //if (WebFramework.dev) {
 //            String db =  WebFramework.config.getProperty("db.dev");
 //            if (db != null) {
 //                if (db.equals("true")) {
 //                    //service.launchDevelopementServer();
 //                    service.started = true;
 //                }
 //            }
 //        //}
         try {
             service.launchJPA();
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         service.started = true;
     }
 
     public static synchronized void stop() {
         JPAService service = getInstance();
 //        service.stopDevelopementServer();
         if (service.emf != null)
             service.emf.close();
         if (service.dataSource instanceof ComboPooledDataSource) {
             try {
             ((ComboPooledDataSource) service.dataSource).close();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         service.started = false;
     }
 
     public void startTx() {
         EntityManager manager = emf.createEntityManager();
         manager.setFlushMode(FlushModeType.COMMIT);
         manager.getTransaction().begin();
         JPAService.currentEm.set(manager);
         JPAService.rollbackFlag.set(false);
     }
 
     public void stopTx(boolean rollback) {
         EntityManager manager = JPAService.currentEm.get();
         try {
             if (rollback) {
                 manager.getTransaction().rollback();
             } else {
                 if (JPAService.rollbackFlag.get()) {
                     manager.getTransaction().rollback();
                 } else {
                     manager.getTransaction().commit();
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         JPAService.rollbackFlag.remove();
         JPAService.currentEm.remove();
     }
 
 //    public void launchDevelopementServer() {
 //        try {
 //            hsqlServer = new Server();
 //            hsqlServer.setLogWriter(null);
 //            hsqlServer.setSilent(true);
 //            hsqlServer.setDatabaseName(0, "webframeworkdb");
 //            hsqlServer.setDatabasePath(0, "file:target/db/webframeworkdb");
 //            hsqlServer.start();
 //        } catch (Exception e) {
 //            e.printStackTrace();
 //        }
 //    }
 //
 //    public void stopDevelopementServer() {
 //        if (hsqlServer != null) {
 //            hsqlServer.shutdown();
 //            hsqlServer.stop();
 //        }
 //    }
 
     public void launchJPA() throws Exception {
         System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
         System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF");
         Ejb3Configuration cfg = new Ejb3Configuration();
         if (WebFramework.config.getProperty("db.dataSource") == null) {
             ComboPooledDataSource intDataSource = new ComboPooledDataSource();
             if ("true".equals(WebFramework.config.getProperty("db.dev", "true"))) {
             //if (hsqlServer != null) {
                 intDataSource.setDriverClass("org.hsqldb.jdbcDriver");
                 //intDataSource.setJdbcUrl("jdbc:hsqldb:hsql://localhost/webframeworkdb");
                 intDataSource.setJdbcUrl("jdbc:hsqldb:file:" + (new File(WebFramework.DB, "webframeworkdb").getAbsolutePath()));
                 intDataSource.setUser("sa");
                 intDataSource.setPassword("");
                 cfg.setProperty("hibernate.hbm2ddl.auto", "create-drop");          
                 cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
             } else {
                 intDataSource.setDriverClass(
                        WebFramework.config.getProperty("db.url"));
                intDataSource.setJdbcUrl(
                         WebFramework.config.getProperty("db.driver"));
                 intDataSource.setUser(
                         WebFramework.config.getProperty("db.user"));
                 intDataSource.setPassword(
                         WebFramework.config.getProperty("db.pass"));
                cfg.setProperty("hibernate.hbm2ddl.auto", WebFramework.config.getProperty("jpa.dialect"));
                cfg.setProperty("hibernate.dialect", WebFramework.config.getProperty("jpa.ddl"));
             }
             intDataSource.setAcquireRetryAttempts(10);
             intDataSource.setCheckoutTimeout(5000);
             intDataSource.setBreakAfterAcquireFailure(false);
             intDataSource.setMaxPoolSize(30);
             intDataSource.setMinPoolSize(1);
             intDataSource.setIdleConnectionTestPeriod(10);
             intDataSource.setTestConnectionOnCheckin(true);
             dataSource = intDataSource;
         } else {
             Context ctx = new InitialContext();
             dataSource = (DataSource) ctx.lookup(WebFramework.config.getProperty("db.dataSource"));
         }
         cfg.setProperty("javax.persistence.transactionType", "RESOURCE_LOCAL");
         Collection<Class<?>> classes = WebFramework.getApplicationClasses();
         for (Class<?> clazz : classes) {
             if (clazz.isAnnotationPresent(Entity.class)) {
                 cfg.addAnnotatedClass(clazz);
             }
         }
         cfg.setDataSource(dataSource);
         this.emf = cfg.buildEntityManagerFactory();
     }
 
     public TxManager getTxManager() {
         return txManager;
     }
 
     public class TxManager {
         public void rollboackCurrentTx() {
             JPAService.rollbackFlag.set(true);
         }
     }
 
     public DataSource getDataSource() {
         return dataSource;
     }
 
     public Connection getConnection() {
         try {
             return dataSource.getConnection();
         } catch (SQLException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     private static void findEntities(ArrayList<String> builder, File file) {
         final File[] children = file.listFiles();
         if (children != null) {
             for (File f : children) {
                 if (f.isDirectory()) {
                     findEntities(builder, f);
                 }
                 if (f.isFile()) {
                     if (f.getName().endsWith(".java")) {
                         builder.add(f.getAbsolutePath());
                     }
                 }
             }
         }
     }
 
 //    public void launchJPA() throws Exception {
 //        System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
 //        System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF");
 //        Ejb3Configuration cfg = new Ejb3Configuration();
 //        cfg.setProperty("hibernate.hbm2ddl.auto", "create-drop");
 //        cfg.setProperty("javax.persistence.transactionType", "RESOURCE_LOCAL");
 //        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
 //        cfg.addAnnotatedClass(Person.class);
 //        ComboPooledDataSource source = new ComboPooledDataSource();
 //        source.setDriverClass("org.hsqldb.jdbcDriver");
 //        source.setJdbcUrl("jdbc:hsqldb:hsql://localhost/webframeworkdb");
 //        source.setUser("sa");
 //        source.setPassword("");
 //        source.setAcquireRetryAttempts(10);
 //        source.setCheckoutTimeout(5000);
 //        source.setBreakAfterAcquireFailure(false);
 //        source.setMaxPoolSize(30);
 //        source.setMinPoolSize(1);
 //        source.setIdleConnectionTestPeriod(10);
 //        source.setTestConnectionOnCheckin(true);
 //        cfg.setDataSource(source);
 //        EntityManagerFactory programmaticEmf = cfg.buildEntityManagerFactory();
 //        EntityManager manager = programmaticEmf.createEntityManager();
 //        Person person = new Person("john", "doe", "null");
 //        EntityTransaction tx = manager.getTransaction();
 //        tx.begin();
 //        manager.persist(person);
 //        manager.flush();
 //        tx.commit();
 //        manager.close();
 //        programmaticEmf.close();
 //        source.close();
 //    }
 
 //    public void testConnection() {
 //        Connection connection = null;
 //        try {
 //            Class.forName("org.hsqldb.jdbcDriver");
 //            connection = DriverManager.getConnection(
 //                    "jdbc:hsqldb:hsql://localhost/webframeworkdb", "sa", "");
 //            try {
 //                connection.prepareStatement("drop table testtable;").execute();
 //            } catch (Exception e) {
 //                e.printStackTrace();
 //            }
 //            connection.prepareStatement(
 //                    "create table testtable ( id INTEGER, "
 //                    + "name VARCHAR);").execute();
 //            connection.prepareStatement(
 //                    "insert into testtable(id, name) "
 //                    + "values (1, 'testvalue');").execute();
 //            ResultSet rs = connection.prepareStatement(
 //                    "select * from testtable;").executeQuery();
 //            rs.next();
 //            System.out.println("Id: " + rs.getInt(1) + " Name: "
 //                    + rs.getString(2));
 //        } catch (Exception e) {
 //            e.printStackTrace();
 //        } finally {
 //            // Closing the connection
 //            if (connection != null) {
 //                try {
 //                    connection.close();
 //                } catch (SQLException ex) {
 //                    ex.printStackTrace();
 //                }
 //            }
 //        }
 //    }
 }
