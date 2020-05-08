 /*
  * Copyright 2010-2013, CloudBees Inc.
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
 package localdomain.localhost;
 
 import sun.misc.BASE64Encoder;
 
 import javax.annotation.Resource;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.annotation.WebListener;
 import javax.sql.DataSource;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
  */
 @WebListener
 public class JdbcRealmInitializerWebListener implements ServletContextListener {
 
     private final Logger logger = Logger.getLogger(getClass().getName());
     @Resource(lookup = "jdbc/mydb")
     private DataSource dataSource;
 
     @Override
     public void contextInitialized(ServletContextEvent sce) {
         logger.info("Initialize JdbcRealm database");
         Connection cnn = null;
         try {
             if (dataSource == null) {
                 throw new IllegalStateException("DataSource not injected, verify in web.xml that 'metadata-complete' is not set to 'true'");
             }
 
             cnn = dataSource.getConnection();
 
             testDatabaseConnectivity(cnn);
             createTablesIfNotExist(cnn);
 
             insertUserIfNotExists("john", "doe", "user", cnn);
 
             insertUserIfNotExists("cloudbees", "beescloud", "user", cnn);
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Exception starting app", e);
         } finally {
             closeQuietly(cnn);
         }
     }
 
     private void insertUserIfNotExists(String username, String password, String group, Connection cnn) throws NoSuchAlgorithmException, UnsupportedEncodingException, SQLException {
 
         if (userExists(username, cnn)) {
             logger.info("User '" + username + "' already exists. Skip creation.");
             return;
         }
         logger.info("Create user '" + username + "' in group '" + group + "'");
         {
             // insert user
             String encodedPasswordDigest = hashAndEncodePassword(password);
             PreparedStatement stmt = null;
             try {
                 stmt = cnn.prepareStatement("insert into `cb_users`(`username`, `password`) values (?,?)");
                 stmt.setString(1, username);
                 stmt.setString(2, encodedPasswordDigest);
                 stmt.execute();
             } finally {
                 closeQuietly(stmt);
             }
         }
 
         {
             // insert user_group
             PreparedStatement stmt = null;
             try {
                 stmt = cnn.prepareStatement("insert into `cb_groups`(`groupname`, `username`) values (?,?)");
                 stmt.setString(1, group);
                 stmt.setString(2, username);
                 stmt.execute();
             } finally {
                 closeQuietly(stmt);
             }
         }
     }
 
     private boolean userExists(String username, Connection cnn) throws SQLException {
         boolean userExists;
         PreparedStatement stmt = null;
         ResultSet rst = null;
         try {
             stmt = cnn.prepareStatement("select count(*) from `cb_users` where `username` = ?");
             stmt.setString(1, username);
             rst = stmt.executeQuery();
             rst.next();
             int count = rst.getInt(1);
             userExists = count > 0;
         } finally {
             closeQuietly(stmt);
         }
         return userExists;
     }
 
     private String hashAndEncodePassword(String password) {
         try {
             MessageDigest md = MessageDigest.getInstance("SHA-256");
             md.update(password.getBytes("UTF-8"));
             byte[] passwordDigest = md.digest();
             return new BASE64Encoder().encode(passwordDigest);
         } catch (Exception e) {
             throw new RuntimeException("Exception encoding password", e);
         }
     }
 
     private void createTablesIfNotExist(Connection cnn) {
        if (tableExists("cb_users", cnn)) {
            logger.info("table cb_users already exists");
        } else {
            logger.info("create table cb_users");
             String sql =
                     "CREATE TABLE `cb_users` (                 \n" +
                             "  `username` varchar(255) NOT NULL,       \n" +
                             "  `password` varchar(255) DEFAULT NULL,   \n" +
                             "  PRIMARY KEY (`username`)                \n" +
                             ")                                         \n";
             executeStatement(sql, cnn);
         }
 
 
        if (tableExists("cb_groups", cnn)) {
            logger.info("table cb_groups already exists");
        } else {
            logger.info("create table cb_groups");
             String sql =
                     "CREATE TABLE `cb_groups` (                        \n" +
                             "  `groupname` varchar(255) NOT NULL,      \n" +
                             "  `username` varchar(255) NOT NULL,       \n" +
                             "  `ID` int(11) NOT NULL AUTO_INCREMENT,   \n" +
                             "  PRIMARY KEY (`ID`),                     \n" +
                             "  UNIQUE KEY `ID_UNIQUE` (`ID`)           \n" +
                             ")                                         \n";
             executeStatement(sql, cnn);
 
         }
     }
 
     private void executeStatement(String sql, Connection cnn) {
         Statement stmt = null;
         try {
             stmt = cnn.createStatement();
             stmt.execute(sql);
         } catch (SQLException e) {
             throw new RuntimeException("Exception executing '" + sql + "'", e);
         } finally {
             closeQuietly(stmt);
         }
     }
 
     private void testDatabaseConnectivity(Connection cnn) {
         try {
             executeStatement("select 1", cnn);
         } catch (RuntimeException e) {
             throw new RuntimeException("Database connectivity failure", e);
         }
     }
 
     private boolean tableExists(String tableName, Connection cnn) {
         Statement stmt = null;
         try {
             stmt = cnn.createStatement();
             stmt.execute("select * from " + tableName + " where 0=1");
             return true;
         } catch (SQLException e) {
             return false;
         } finally {
             closeQuietly(stmt);
         }
     }
 
     private void closeQuietly(Statement stmt) {
         if (stmt == null) {
             return;
         }
         try {
             stmt.close();
         } catch (Exception e) {
             logger.log(Level.WARNING, "Ignore exception closing quietly statement", e);
         }
     }
 
     private void closeQuietly(Connection cnn) {
         if (cnn == null) {
             return;
         }
         try {
             cnn.close();
         } catch (Exception e) {
             logger.log(Level.WARNING, "Ignore exception closing quietly connection", e);
         }
     }
 
     @Override
     public void contextDestroyed(ServletContextEvent sce) {
     }
 }
