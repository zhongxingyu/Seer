 /*
  * Copyright (c) 2001-2010, Inversoft Inc., All Rights Reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific
  * language governing permissions and limitations under the License.
  */
 package org.primeframework.persistence.test;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.persistence.Table;
 import javax.sql.DataSource;
 import javax.sql.RowSet;
 import javax.sql.rowset.CachedRowSet;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Logger;
 
 import org.primeframework.persistence.service.DatabaseType;
 import org.primeframework.persistence.service.DatabaseType.Database;
import org.primeframework.persistence.service.jdbc.ConnectionContext;
 
 import com.sun.rowset.CachedRowSetImpl;
 
 /**
  * This class provides test helper methods for JDBC.
  *
  * @author Brian Pontarelli
  */
 public class JDBCTestHelper {
   public static final Logger logger = Logger.getLogger(JDBCTestHelper.class.getName());
   public static DataSource dataSource;
 
   /**
    * Constructs the DataSource and places it in the JNDI tree for Hibernate.
    *
    * @param type         The database type to setup the DataSource for.
    * @param databaseName The database name to connect to.
    * @param jndiName     The JDNI name to put the DataSource under.
    * @throws NamingException If setting the data source into the JNDI tree fails.
    */
   public static void initialize(Database type, String databaseName, String jndiName) throws NamingException {
     InitialContext jndi = new InitialContext();
     if (dataSource == null) {
       if (type == Database.MYSQL) {
         logger.info("+++++++++++++++++++++++++++++++ Setting up MySQL data source for testing +++++++++++++++++++++++++++++++");
         dataSource = MySQLTools.setup(jndi, databaseName, jndiName);
         DatabaseType.database = Database.MYSQL;
       } else if (type == Database.POSTGRESQL) {
         logger.info("+++++++++++++++++++++++++++++++ Setting up PostgreSQL data source for testing +++++++++++++++++++++++++++++++");
         dataSource = PostgreSQLTools.setup(jndi, databaseName, jndiName);
         DatabaseType.database = Database.POSTGRESQL;
       } else {
         throw new RuntimeException("Invalid database type for testing [" + type + "]");
       }
     }
   }
 
   /**
   * Sets up a connection into the context for testing.
   */
  public static void setupForTest() {
    try {
      ConnectionContext.set(dataSource.getConnection());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Tears down the connection from the context and closes it after the test has run.
   */
  public static void tearDownFromTest() {
    try {
      ConnectionContext.get().close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      ConnectionContext.remove();
    }
  } 

  /**
    * Executes the given SQL statement via plain old JDBC. This will be committed to the database. This SQL statement
    * should be an insert, update or delete statement because it uses the executeUpdate method on Statement.
    *
    * @param sql The SQL to execute.
    * @throws java.sql.SQLException If the execute failed.
    */
   public static void executeSQL(String sql) throws SQLException {
     Connection c = dataSource.getConnection();
     Statement s = c.createStatement();
     s.executeUpdate(sql);
     s.close();
     c.close();
   }
 
   /**
    * Clears all the data from the given table using JDBC.
    *
    * @param table The Table to clear.
    * @throws java.sql.SQLException If the clear failed.
    */
   public static void clearTable(String table) throws SQLException {
     executeSQL("delete from " + table);
   }
 
   /**
    * Clears all the data from the table that the given Class is mapped to using JPA.
    *
    * @param klass The JPA class to get the table name from. This first checks if the Class has a JPA Table annotation
    *              and uses that. Otherwise, it uses the Class's simple name.
    * @throws java.sql.SQLException If the clear failed.
    */
   public static void clearTable(Class<?> klass) throws SQLException {
     Table table = klass.getAnnotation(Table.class);
     String tableName;
     if (table == null || table.name().equals("")) {
       tableName = klass.getSimpleName();
     } else {
       tableName = table.name();
     }
 
     clearTable(tableName);
   }
 
   /**
    * Runs the given query and returns the results in a detached RowSet.
    *
    * @param query The query to run.
    * @return The results of the query.
    * @throws java.sql.SQLException If the query failed.
    */
   public static RowSet executeQuery(String query) throws SQLException {
     Connection c = dataSource.getConnection();
     Statement s = c.createStatement();
     ResultSet rs = s.executeQuery(query);
     CachedRowSet rowSet = new CachedRowSetImpl();
     rowSet.populate(rs);
     rs.close();
     s.close();
     c.close();
     return rowSet;
   }
 
   /**
    * @return A connection.
    * @throws java.sql.SQLException on sql exception
    */
   public static Connection getConnection() throws SQLException {
     return dataSource.getConnection();
   }
 }
