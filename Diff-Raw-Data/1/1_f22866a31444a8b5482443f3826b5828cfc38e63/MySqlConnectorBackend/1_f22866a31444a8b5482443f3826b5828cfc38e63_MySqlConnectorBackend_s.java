 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.connector.backend;
 
 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 import com.gooddata.connector.model.PdmColumn;
 import com.gooddata.connector.model.PdmSchema;
 import com.gooddata.connector.model.PdmTable;
 import com.gooddata.exception.InternalErrorException;
 import com.gooddata.naming.N;
 import com.gooddata.util.JdbcUtil;
 import com.gooddata.util.StringUtil;
 
 /**
  * GoodData  MySQL connector backend. This connector backend is the performance option. It provides reasonable
  * performance for large data files. This connector backend assumes that MySQL is installed on the computer where
  * it runs.
  * Connector backend handles communication with the specific SQL database. Specifically it handles the DB connection
  * and other communication specifics of the Derby SQL. It uses the SQL driver that generates appropriate SQL dialect.
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 
 public class MySqlConnectorBackend extends AbstractSqlConnectorBackend implements ConnectorBackend {
 
     private static Logger l = Logger.getLogger(MySqlConnectorBackend.class);
 
     //use memory tables for transformations
     private boolean useMemory = false;
 
     //use memory tables for transformations
     private int memoryAmountInMB = 60;
     
     /**
      * static initializer of the Derby SQL JDBC driver
      */
     static {
         l.debug("Loading MySQL driver.");
         String driver = "com.mysql.jdbc.Driver";
         try {
             Class.forName(driver).newInstance();
         } catch (InstantiationException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
         l.debug("Finished loading MySQL driver.");
     }
 
     /**
      * Constructor
      * @param username database backend username
      * @param password database backend password
      * @throws java.io.IOException in case of an IO issue
      */
     protected MySqlConnectorBackend(String username, String password) throws IOException {
         super(username, password);
         // autoincrement syntax
         SYNTAX_AUTOINCREMENT = "AUTO_INCREMENT";
         SYNTAX_CONCAT_FUNCTION_PREFIX = "CONCAT(";
         SYNTAX_CONCAT_FUNCTION_SUFFIX = ")";
         SYNTAX_CONCAT_OPERATOR = ",'" + HASH_SEPARATOR + "',";
         FETCH_SIZE = Integer.MIN_VALUE;
     }
 
     /**
      * Constructor
      * @param username database backend username
      * @param password database backend password
      * @param host database backend hostname
      * @throws java.io.IOException in case of an IO issue
      */
     protected MySqlConnectorBackend(String username, String password, String host) throws IOException {
         this(username, password);
         this.setHost(host);
     }
 
     /**
      * Constructor
      * @param username database backend username
      * @param password database backend password
      * @param host database backend hostname
      * @param mem amount of memory (in MB) to use for the fast in-memory processing
      * @throws java.io.IOException in case of an IO issue
      */
     protected MySqlConnectorBackend(String username, String password, String host, int mem) throws IOException {
         this(username, password,host);
         this.setUseMemory(true);
         this.setMemoryAmountInMB(mem);
     }
 
     /**
      * Create
      * @param username MySQL username
      * @param password MySQL password
      * @return a new instance of the MySQL connector backend
      * @throws java.io.IOException in case of an IO issue
      */
     public static MySqlConnectorBackend create(String username, String password) throws IOException {
         return new MySqlConnectorBackend(username, password);
     }
 
     /**
      * Create
      * @param username MySQL username
      * @param password MySQL password
      * @param host MySQL password hostname
      * @return a new instance of the MySQL connector backend
      * @throws java.io.IOException in case of an IO issue
      */
     public static MySqlConnectorBackend create(String username, String password, String host) throws IOException {
         return new MySqlConnectorBackend(username, password, host);
     }
 
     /**
      * Create
      * @param username database backend username
      * @param password database backend password
      * @param host database backend hostname
      * @param mem amount of memory (in MB) to use for the fast in-memory processing
      * @throws java.io.IOException in case of an IO issue
      */
     public static MySqlConnectorBackend create(String username, String password, String host, int mem) throws IOException {
         return new MySqlConnectorBackend(username, password, host, mem);        
     }
 
 
     /**
      * {@inheritDoc}
      */
     public Connection getConnection() throws SQLException {
         String dbName = N.DB_PREFIX+getProjectId()+N.DB_SUFFIX;
     	if (connection == null) {
 	        String protocol = "jdbc:mysql:";
             String hostName = "//localhost/";
             if(getHost() != null) {
                 hostName = "//"+getHost()+"/";
             }
 	        try {
 	        	connection = DriverManager.getConnection(protocol + hostName + dbName + "?jdbcCompliantTruncation=false", getUsername(), getPassword());
 	        }
 	        catch (SQLException e) {
 	        	connection = DriverManager.getConnection(protocol + hostName+"mysql", getUsername(), getPassword());
 	            JdbcUtil.executeUpdate(connection,
 	                "CREATE DATABASE IF NOT EXISTS " + dbName + " CHARACTER SET utf8"
 	            );
 	            connection.close();
 	            connection = DriverManager.getConnection(protocol + hostName + dbName + "?jdbcCompliantTruncation=false", getUsername(), getPassword());
 	        }
     	}
     	Properties props = new Properties();
     	props.setProperty("useUnicode", "true");
     	props.setProperty("characterEncoding", "utf-8");
     	connection.setClientInfo(props);
         return connection;
     }
 
     protected String makeCreateTempTableStatement(String tableName, String definition) {
     	return "CREATE TEMPORARY TABLE " + tableName + " " + definition;
     }
 
     /**
      * {@inheritDoc}
      */
     public void dropIntegrationDatabase() {
         String dbName = N.DB_PREFIX+getProjectId()+N.DB_SUFFIX;
         l.debug("Dropping MySQL snapshots "+dbName);
         Connection con = null;
         Statement s = null;
         try {
             con = getConnection();
             s = con.createStatement();
             s.execute("DROP DATABASE IF EXISTS " + dbName);
 
         } catch (SQLException e) {
             l.debug("Error dropping MySQL snapshots.", e);
             throw new InternalErrorException("Error dropping MySQL snapshots.",e);
         }
         finally {
             try  {
                 if(s != null)
                     s.close();
             }
             catch (SQLException e) {
                 l.error("Can't close MySQL connection.", e);
             }
         }
         l.debug("Finished dropping MySQL snapshots "+dbName);
     }
 
     /**
      * {@inheritDoc}
      */
     protected String decorateFactColumnForLoad(String cols, PdmColumn cl, String table) {
         if (cols.length() > 0)
             cols += ",ATOD(" + table + "." +
                     StringUtil.toIdentifier(cl.getName())+")";
         else
             cols +=  "ATOD(" + table + "." +
                     StringUtil.toIdentifier(cl.getName())+")";
         return cols;
     }
 
     /**
      * {@inheritDoc}
      */
     protected void insertFactsToFactTable(Connection c, PdmSchema schema) throws SQLException {
         PdmTable factTable = schema.getFactTable();
         PdmTable sourceTable = schema.getSourceTable();
         String fact = factTable.getName();
         String source = sourceTable.getName();
         String factColumns = "";
         String sourceColumns = "";
         for(PdmColumn column : factTable.getFactColumns()) {
             factColumns += "," + column.getName();
             sourceColumns += "," + column.getSourceColumn();
         }
 
         for(PdmColumn column : factTable.getDateColumns()) {
             factColumns += "," + column.getName();
             sourceColumns += ",IFNULL(DATEDIFF(STR_TO_DATE(" + column.getSourceColumn() + ",'" +
                     StringUtil.convertJavaDateFormatToMySql(column.getFormat())
                     + "'),'1900-01-01')+1," + getDefaultDateForeignKey() + ")";
         }
         String sql = "INSERT INTO "+fact+"("+N.ID+factColumns+") SELECT "+ N.SRC_ID + sourceColumns 
         		+ " FROM " + source + " WHERE "+N.SRC_ID+" > (SELECT MAX(lastid) FROM snapshots WHERE name='"
         		+fact+"')";
         JdbcUtil.executeUpdate(c, sql);
     }
 
     /**
      * {@inheritDoc}
      */
     protected void createFunctions(Connection c) throws SQLException {
         l.debug("Creating system functions.");
     	String sql = "CREATE FUNCTION ATOD(str varchar(255)) RETURNS DECIMAL(15,4) "
 			    + "RETURN CASE "
                 + "      WHEN TRIM(str) = '-' THEN 0"
 			    + "      WHEN TRIM(str) = '' THEN NULL "
 			    + "      ELSE CAST( ";
     	for (final String s : Constants.DISCARD_CHARS) {
     		sql += "REPLACE(";
     	}
     	sql += "str";
     	for (final String s : Constants.DISCARD_CHARS) {
     		sql += ", '" + s + "', '')";
     	}
 		sql +=  "           AS DECIMAL(15,4)) "
 			  + "   END";
 
         JdbcUtil.executeUpdate(c, sql);
         l.debug("System functions creation finished.");
     }
 
     /**
      * {@inheritDoc}
      */
     protected void updateFactTableFk(Connection c, PdmSchema schema) throws SQLException {
         String fact = schema.getFactTable().getName();
         String updateStatement = "";
         List<PdmTable> tables = new ArrayList<PdmTable>();
         tables.addAll(schema.getLookupTables());
         tables.addAll(schema.getConnectionPointTables());
         tables.addAll(schema.getReferenceTables());
         for(PdmTable tbl : tables) {
             if(updateStatement.length() > 0)
                 updateStatement += " , " + generateFactUpdateSetStatement(tbl, schema);
             else
                 updateStatement += generateFactUpdateSetStatement(tbl, schema);
     	}
 
         if(useMemory)
             copyLookupsToMemory(c,tables);
 
         if(updateStatement.length()>0) {
             updateStatement = "UPDATE " + fact + " SET " + updateStatement +
                 " WHERE "+N.ID+" > "+getLastId(c,fact);
             JdbcUtil.executeUpdate(c, updateStatement);
         }
 
         if(useMemory)
             dropMemoryLookups(c,tables);
     }
 
     /**
      * {@inheritDoc}
      */
     protected String generateFactUpdateSetStatement(PdmTable lookupTable, PdmSchema schema) {
         String lookup = lookupTable.getName();
         String fact = schema.getFactTable().getName();
         String source = schema.getSourceTable().getName();
         String associatedSourceColumns = concatAssociatedSourceColumns(lookupTable);
 
         return lookupTable.getAssociatedSourceColumn() + "_"+N.ID+" = (SELECT "+N.ID+" FROM " +
                 ((useMemory)?(N.MEM_TBL_PREFIX):("")) + lookup + " d," + source + " o WHERE " + associatedSourceColumns +
                 " = d."+N.HSH+" AND o."+N.SRC_ID+"= " +
               fact + "."+N.ID+") ";
     }
 
     protected void copyLookupsToMemory(Connection c, List<PdmTable> tables) throws SQLException {
         JdbcUtil.executeUpdate(c,"set max_heap_table_size="+(getMemoryAmountInMB()*1000000));
         for(PdmTable tbl : tables) {
             String tblName = tbl.getName();
             String memTblName = N.MEM_TBL_PREFIX + tbl.getName();
             int maxLength = getMaxHashidLength(tblName);
             JdbcUtil.executeUpdate(c,"CREATE TEMPORARY TABLE " + memTblName + " ("+N.ID+" INT, "+N.HSH+" CHAR("+maxLength+") UNIQUE) ENGINE MEMORY");
             JdbcUtil.executeUpdate(c,"INSERT INTO "+memTblName+" SELECT "+N.ID+","+N.HSH+" FROM "+tblName);
         }
     }
 
     protected void dropMemoryLookups(Connection c, List<PdmTable> tables) throws SQLException {
         for(PdmTable tbl : tables) {
             String tblName = tbl.getName();
             String memTblName = N.MEM_TBL_PREFIX + tbl.getName();
             JdbcUtil.executeUpdate(c,"DROP TABLE IF EXISTS " + memTblName);
         }
     }
 
     public int getMaxHashidLength(String tblName) throws SQLException {
         Connection con = null;
         Statement s = null;
         ResultSet r = null;
         try {
             con = getConnection();
             s = con.createStatement();
             r = s.executeQuery("SELECT MAX(LENGTH("+N.HSH+")) FROM "+tblName);
             for(boolean rc = r.next(); rc; rc = r.next()) {
                 int mx = r.getInt(1);
                 return mx;
             }
         }
         finally {
             try {
                 if(r != null)
                     r.close();
                 if(s != null)
                     s.close();
             }
             catch (SQLException ee) {
                 ee.printStackTrace();
             }
         }
         return Constants.HASHID_MAX_LENGTH;
     }
 
     public boolean isUseMemory() {
         return useMemory;
     }
 
     public void setUseMemory(boolean useMemory) {
         this.useMemory = useMemory;
     }
 
     public int getMemoryAmountInMB() {
         return memoryAmountInMB;
     }
 
     public void setMemoryAmountInMB(int memoryAmountInMB) {
         if(memoryAmountInMB > Constants.MIN_MYSQL_HEAP_MEMORY_IN_MB)
             this.memoryAmountInMB = memoryAmountInMB;
         else
             this.memoryAmountInMB = Constants.MIN_MYSQL_HEAP_MEMORY_IN_MB;            
     }
     
 
 }
