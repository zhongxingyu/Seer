 package com.neutrino.profiling;
 
 import com.avaje.ebean.EbeanServer;
 import com.avaje.ebean.EbeanServerFactory;
 import com.avaje.ebean.Transaction;
 import com.avaje.ebean.config.DataSourceConfig;
 import com.avaje.ebean.config.ServerConfig;
 
 import javax.persistence.PersistenceException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 public class EbeanServerManager {
     private final static EbeanServerManager INSTANCE = new EbeanServerManager();
     private static final ConcurrentMap<String, EbeanServer> SERVERS = new ConcurrentHashMap<>();
 
     private EbeanServerManager() {
 
     }
     public static EbeanServerManager getManager() {
         return INSTANCE;
     }
 
     public EbeanServer getMysqlServer() {
         return getServer("mysql", null, false);
     }
 
     public boolean isCreated(final String dbName) {
         return executeQuery(getMysqlServer(), new QueryCallable<Boolean>() {
             @Override
             public Boolean call(PreparedStatement pstmt) throws SQLException {
                 ResultSet r = pstmt.executeQuery();
                 return r.next();
             }
 
             @Override
             public String getQuery() {
                 return "show databases like ?";
             }
 
             @Override
             public void setup(PreparedStatement pstmt) throws SQLException {
                 pstmt.setString(1, dbName);
             }
         });
 
     }
 
     public boolean createDatabase(final String dbName) {
         return executeQuery(getMysqlServer(), new QueryCallable<Boolean>() {
             @Override
             public Boolean call(PreparedStatement pstmt) throws SQLException {
                 return pstmt.execute();
             }
 
             @Override
             public String getQuery() {
                 return "create database " + dbName;
             }
 
             @Override
             public void setup(PreparedStatement pstmt) throws SQLException {
             }
         });
 
     }
 
     public <V> V executeQuery(EbeanServer s, QueryCallable<V> query) {
         PreparedStatement pstmt = null;
         Connection conn = null;
         Transaction t = s.createTransaction();
         try {
             conn = t.getConnection();
             pstmt = conn.prepareStatement(query.getQuery());
             query.setup(pstmt);
             return query.call(pstmt);
         } catch (SQLException e) {
             throw new PersistenceException("Failed to execute SQL " + query, e);
         } finally {
             if (pstmt != null) {
                 try {
                     pstmt.close();
                 } catch (SQLException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
             t.commit();
             t.end();
         }
     }
 
     private String serverName(String dbName) {
         return "server_" + dbName;
     }
 
     public EbeanServer getServer(String dbName, List<Class<?>> classes, boolean runDdl) {
         if (runDdl || !SERVERS.containsKey(dbName)) {
             ServerConfig config = new ServerConfig();
             config.setName(serverName(dbName));
             config.setDatabaseBooleanTrue("T");
             config.setDatabaseBooleanFalse("F");
             DataSourceConfig db = new DataSourceConfig();
             db.setDriver("com.mysql.jdbc.Driver");
             db.setUsername("root");
             db.setPassword("");
            db.setUrl("jdbc:mysql://localhost:3306/" + dbName);
             db.setHeartbeatSql("select 1");
             config.setDefaultServer(false);
             config.setRegister(true);
             config.setDataSourceConfig(db);
             config.setEnhanceLogLevel(10);
             if (classes != null) {
                 config.setClasses(classes);
             }
             if (runDdl) {
                 config.setDdlGenerate(true);
                 config.setDdlRun(true);
                 config.setRegister(false);
                 return EbeanServerFactory.create(config);
             }
             SERVERS.putIfAbsent(dbName, EbeanServerFactory.create(config));
         }
         return SERVERS.get(dbName);
     }
 
 }
