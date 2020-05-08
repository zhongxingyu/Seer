 /*
  * Copyright 2008 The Kuali Foundation
  * 
  * Licensed under the Educational Community License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.opensource.org/licenses/ecl2.php
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.kualigan.tools.liquibase.change.ext;
 
 import java.math.BigInteger;
 
 import liquibase.change.AbstractChange;
 import liquibase.change.Change;
 import liquibase.database.Database;
 import liquibase.database.jvm.JdbcConnection;
 import liquibase.exception.DatabaseException;
 import liquibase.exception.ValidationErrors;
 import liquibase.executor.ExecutorService;
 import liquibase.logging.LogFactory;
 import liquibase.logging.Logger;
 import liquibase.sql.Sql;
 import liquibase.sql.UnparsedSql;
 import liquibase.statement.SqlStatement;
 import liquibase.statement.core.InsertStatement;
 import liquibase.statement.core.RuntimeStatement;
 
 import liquibase.change.core.DeleteDataChange;
 
 import java.io.PrintStream;
 
 import java.sql.Clob;
 import java.sql.DatabaseMetaData;
 import java.sql.SQLException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.Statement;
 import java.sql.Types;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Observer;
 import java.util.Observable;
 
 import static liquibase.ext.Constants.EXTENSION_PRIORITY;
 
 /**
  * Copies data from one database to another
  *
  * @author Leo Przybylski
  */
 public class MigrateData extends AbstractChange {
     private static final String[] carr = new String[] {"|", "\\", "-", "/"};
     private static final String RECORD_COUNT_QUERY = "select count(*) as \"COUNT\" from %s";
     private static final String SELECT_ALL_QUERY   = "select * from %s";
     private static final String INSERT_STATEMENT   = "insert into %s (%s) values (%s)";
     private static final String DATE_CONVERSION    = "TO_DATE('%s', 'YYYYMMDDHH24MISS')";
     private static final String COUNT_FIELD        = "COUNT";
     private static final String LIQUIBASE_TABLE    = "DATABASECHANGELOG";
     private static final int[]  QUOTED_TYPES       = 
         new int[] {Types.CHAR, Types.VARCHAR, Types.TIME, Types.LONGVARCHAR, Types.DATE, Types.TIMESTAMP};
 
     private static final String HSQLDB_PUBLIC      = "PUBLIC";
     private static final int    MAX_THREADS        = 3;
 
     private Database source;
     private Database target;
     private String sourceUrl;    
     private String sourceUser;
     private String sourcePass;
     private String sourceSchema;
     private Class  sourceDriverClass;
     
     public MigrateData() {
         super("MigrateData", "Migrating data from sourceUrl", EXTENSION_PRIORITY);
 	setSourceUrl(System.getProperty("lb.copy.source.url"));
 	setSourceUser(System.getProperty("lb.copy.source.user"));
 	setSourcePass(System.getProperty("lb.copy.source.password"));
 	setSourceSchema(System.getProperty("lb.copy.source.schema"));
     }
     
     /**
      * Supports all databases 
      */
     @Override
     public boolean supports(Database database) {
         return true;
     }
 
     /**
      *
      */
     @Override
     public ValidationErrors validate(Database database) {
         return super.validate(database);
     }
 
     /**
      * Normally returns sql statements, but we're not going to return any. Just going fake it.
      */
     public SqlStatement[] generateStatements(Database database) {
 	final Database target = null;
 	final Database source = null;
 
 	migrate(source, target);
         return new SqlStatement[]{};
     }
 
     public void migrate(final Database source, final Database target) throws DatabaseException {
         setTarget(target);
         setSource(source);
         migrate();
     }
     
     public void migrate() throws DatabaseException {
         getLog().debug("Migrating data from " + source.getConnection().getURL() + " to " + target.getConnection().getURL());
 
         final Incrementor recordCountIncrementor = new Incrementor();
         final Map<String, Integer> tableData = getTableData(recordCountIncrementor);
 
         getLog().debug("Copying " + tableData.size() + " tables");
 
         float recordVisitor = 0;
         final ProgressObserver progressObserver = new ProgressObserver(recordCountIncrementor.getValue(),
                                                                        48f, 48f/100,
                                                                        "\r|%s[%s] %3d%% (%d/%d) records");
         final ProgressObservable observable = new ProgressObservable();
         observable.addObserver(progressObserver);
 
         final ThreadGroup tgroup = new ThreadGroup("Migration Threads");
 
         for (final String tableName : tableData.keySet()) {
             final Map<String,Integer> columns = new HashMap<String, Integer>();
             migrate(tableName, observable);
         }
 
         // Wait for other threads to finish
         try {
             while(tgroup.activeCount() > 0) {
                 Thread.sleep(5000);
             }
         }
         catch (InterruptedException e) {
         }
 
     
         try {
             final JdbcConnection targetDb = target.getConnection();
             if (targetDb.getMetaData().getDriverName().toLowerCase().contains("hsqldb")) {
                 Statement st = targetDb.createStatement();
                 st.execute("CHECKPOINT"); 
                 st.close();
             }
         }
         catch (Exception e) {
             throw new DatabaseException(e.getMessage(), e);
         }        
     }
 
     protected void migrate(final String tableName, 
                            final ProgressObservable observable) throws DatabaseException {
         final JdbcConnection sourceDb = (JdbcConnection) getSource().getConnection();
         final JdbcConnection targetDb = (JdbcConnection) getTarget().getConnection();
 
         final Map<String, Integer> columns = getColumnMap(tableName);
 
         if (columns.size() < 1) {
             getLog().debug("Columns are empty for " + tableName);
             return;
         }
 
         final PreparedStatement toStatement = prepareStatement(targetDb, tableName, columns);
         Statement fromStatement = null;
 
         final boolean hasClob = columns.values().contains(Types.CLOB);
         int recordsLost = 0;
         
         try {
             fromStatement = sourceDb.createStatement();
             final ResultSet results = fromStatement.executeQuery(String.format(SELECT_ALL_QUERY, tableName));
             
             try {
                 while (results.next()) {
                     try {
                         toStatement.clearParameters();
                         
                         int i = 1;
                         for (String columnName : columns.keySet()) {
                             final Object value = results.getObject(columnName);
                             
                             if (value != null) {
                                 try {
                                     handleLob(toStatement, value, i);
                                 }
                                 catch (Exception e) {
                                     if (isDebugEnabled()) {
 					// getLog().warning(String.format("Error processing %s.%s %s", tableName, columnName, columns.get(columnName)));
 					if (Clob.class.isAssignableFrom(value.getClass())) {
 					    // getLog().warning("Got exception trying to insert CLOB with length" + ((Clob) value).length());
 					}
                                         // e.printStackTrace();
                                     }
                                 }
                             }
                             else {
                                 toStatement.setObject(i,value);
                             } 
                             i++;
                         }
                         
                         boolean retry = true;
                         int retry_count = 0;
                         while(retry) {
                             try {
                                 toStatement.execute();
                                 retry = false;
                             }
                             catch (SQLException sqle) {
                                 retry = false;
                                 if (sqle.getMessage().contains("ORA-00942")) {
                                     getLog().debug("Couldn't find " + tableName);
                                     if (isDebugEnabled()) {
                                         getLog().debug("Tried insert statement " + getStatementBuffer(tableName, columns), sqle);
                                     }
                                 }
                                 else if (sqle.getMessage().contains("ORA-12519")) {
                                     retry = true;
                                     if (isDebugEnabled()) {
                                         getLog().debug("Tried insert statement " + getStatementBuffer(tableName, columns), sqle);
                                     }
                                 }
                                 else if (sqle.getMessage().contains("IN or OUT")) {
                                     if (isDebugEnabled()) {
                                         getLog().debug("Column count was " + columns.keySet().size(), sqle);
                                     }
                                 }
                                 else if (sqle.getMessage().contains("Error reading")) {
                                     if (retry_count > 5) {
                                         if (isDebugEnabled()) {
                                             getLog().debug("Tried insert statement " + getStatementBuffer(tableName, columns), sqle);
                                         }
                                         retry = false;
                                     }
                                     retry_count++;
                                 }
                                 else {
                                     if (isDebugEnabled()) {
                                         // getLog().warning("Error executing: " + getStatementBuffer(tableName, columns), sqle);
 				    }
                                 }
                             }
                         }
                     }
                     catch (Exception e) {
                         recordsLost++;
                         throw e;
                     }
                     finally {
                         observable.incrementRecord();
                     }
                 }
             }
             finally {
                 if (results != null) {
                     try {
                         results.close();
                     }
                     catch(Exception e) {
                     }
                 }
             }
         }
         catch (Exception e) {
             throw new DatabaseException(e.getMessage(), e);
         }
         finally {
             if (sourceDb != null) {
                 try {
                     if (sourceDb.getMetaData().getDriverName().toLowerCase().contains("hsqldb")) {
                         Statement st = sourceDb.createStatement();
                         st.execute("CHECKPOINT"); 
                         st.close();
                     }
                     fromStatement.close();
                     //sourceDb.close();
                 }
                 catch (Exception e) {
                 }
             }
 
             if (targetDb != null) {
                 try {
                     targetDb.commit();
                     if (targetDb.getMetaData().getDriverName().toLowerCase().contains("hsql")) {
                         Statement st = targetDb.createStatement();
                         st.execute("CHECKPOINT"); 
                         st.close();
                     }
                     toStatement.close();
                     // targetDb.close();
                 }
                 catch (Exception e) {
                     getLog().debug("Error closing database connection");
                     e.printStackTrace();
                 }
             }
             // debug("Lost " +recordsLost + " records");
             columns.clear();
         }
     }
 
     protected void handleLob(final PreparedStatement toStatement, final Object value, final int i) throws SQLException {
         if (Clob.class.isAssignableFrom(value.getClass())) {
             toStatement.setAsciiStream(i, ((Clob) value).getAsciiStream(), ((Clob) value).length());
         }
         else if (Blob.class.isAssignableFrom(value.getClass())) {
             toStatement.setBinaryStream(i, ((Blob) value).getBinaryStream(), ((Blob) value).length());
         }
         else {
             toStatement.setObject(i,value);
         } 
     }
 
     protected PreparedStatement prepareStatement(final JdbcConnection conn, 
                                                  final String tableName, 
                                                  final Map<String, Integer> columns) throws DatabaseException {
         final String statement = getStatementBuffer(tableName, columns);
         
         try {
             return conn.prepareStatement(statement);
         }
         catch (Exception e) {
             throw new DatabaseException(e.getMessage(), e);
         }
     }
 
     protected String getStatementBuffer(final String tableName, final Map<String,Integer> columns) {
         String retval = null;
 
         final StringBuilder names  = new StringBuilder();
         final StringBuilder values = new StringBuilder();
         for (String columnName : columns.keySet()) {
             names.append(columnName).append(",");
             values.append("?,");
         }
 
         names.setLength(names.length() - 1);
         values.setLength(values.length() - 1);
         retval = String.format(INSERT_STATEMENT, tableName, names, values);
         
 
         return retval;
     }
 
     protected boolean isValidTable(final DatabaseMetaData metadata, final String tableName) {
         return !(tableName.startsWith("BIN$") || tableName.toUpperCase().startsWith(LIQUIBASE_TABLE) || isSequence(metadata, tableName));
     }
 
     protected boolean isSequence(final DatabaseMetaData metadata, final String tableName) {
         final JdbcConnection source = (JdbcConnection) getSource().getConnection();
         try {
             final ResultSet rs = source.getMetaData().getColumns(null, getSource().getDefaultSchemaName(), tableName, null);
             int columnCount = 0;
             boolean hasId = false;
             try {
                 while (rs.next()) {
                     columnCount++;
                     if ("yes".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT"))) {
                         hasId = true;
                     }
                 }
             }
             finally {
                 if (rs != null) {
                     try {
                         rs.close();
                     }
                     catch (Exception e) {
                     }
                 }
                 return (columnCount == 1 && hasId);
             }
         }
         catch (Exception e) {
             return false;
         }
     }
 
     /**
      * Get a list of table names available mapped to row counts
      */
     protected Map<String, Integer> getTableData(final Incrementor incrementor) throws DatabaseException {
         JdbcConnection sourceConn = (JdbcConnection) getSource().getConnection();
         JdbcConnection targetConn = (JdbcConnection) getTarget().getConnection();
         final Map<String, Integer> retval = new HashMap<String, Integer>();
         final Collection<String> toRemove = new LinkedList<String>();
 
         getLog().debug("Looking up table names in schema " + getSource().getDefaultSchemaName());
         try {
             final DatabaseMetaData metadata = sourceConn.getMetaData();
             final ResultSet tableResults = metadata.getTables(sourceConn.getCatalog(), getSource().getDefaultSchemaName(), null, new String[] { "TABLE" });
             while (tableResults.next()) {
                 final String tableName = tableResults.getString("TABLE_NAME");
                 if (!isValidTable(metadata, tableName)) {
                     continue;
                 }
                 if (tableName.toUpperCase().startsWith(LIQUIBASE_TABLE)) continue;
                 final int rowCount = getTableRecordCount(sourceConn, tableName);
                 if (rowCount < 1) { // no point in going through tables with no data
                     
                 }
                 incrementor.increment(rowCount);
                 // debug("Adding table " + tableName);
                 retval.put(tableName, rowCount);
             }
             tableResults.close();
         }
         catch (Exception e) {
             throw new DatabaseException(e.getMessage(), e);
         }
 
         try {
             for (String tableName : retval.keySet()) {
                 final ResultSet tableResults = targetConn.getMetaData().getTables(targetConn.getCatalog(), getTarget().getDefaultSchemaName(), null, new String[] { "TABLE" });
                 if (!tableResults.next()) {
                     getLog().debug("Removing " + tableName);
                     toRemove.add(tableName);
                 }
                 tableResults.close();
             }
         }
         catch (Exception e) {
             throw new DatabaseException(e.getMessage(), e);
         }
 
         for (String tableName : toRemove) {
             retval.remove(tableName);
         }
         
         return retval;
     }
 
     protected Map<String, Integer> getColumnMap(final String tableName) throws DatabaseException {
         final JdbcConnection targetDb = (JdbcConnection) target.getConnection();
         final JdbcConnection sourceDb = (JdbcConnection) source.getConnection();
         final Map<String,Integer> retval = new HashMap<String,Integer>();
         final Collection<String> toRemove = new LinkedList<String>();
         try {
             final Statement state = targetDb.createStatement();                
             final ResultSet altResults = state.executeQuery("select * from " + tableName + " where 1 = 0");
             final ResultSetMetaData metadata = altResults.getMetaData();
             
             for (int i = 1; i <= metadata.getColumnCount(); i++) {
                 retval.put(metadata.getColumnName(i),
                            metadata.getColumnType(i));
             }
             altResults.close();
             state.close();
         }
         catch (Exception e) {
             throw new DatabaseException(e.getMessage(), e);
         }
 
         for (final String column : retval.keySet()) {
             try {
                 final Statement state = targetDb.createStatement();                
                 final ResultSet altResults = state.executeQuery("select * from " + tableName + " where 1 = 0");
                 final ResultSetMetaData metadata = altResults.getMetaData();
 
                 for (int i = 1; i <= metadata.getColumnCount(); i++) {
                     retval.put(metadata.getColumnName(i),
                                metadata.getColumnType(i));
                 }
                 altResults.close();
                 state.close();
             }
             catch (Exception e) {
                 throw new DatabaseException(e.getMessage(), e);
             }
         }
 
         for (final String column : toRemove) {
             retval.remove(column);
         }
         
         return retval;
     }
 
     protected int getTableRecordCount(final JdbcConnection conn, final String tableName) throws DatabaseException {
         final String query = String.format(RECORD_COUNT_QUERY, tableName);
         Statement statement = null;
         try {
             statement = conn.createStatement();
             final ResultSet results = statement.executeQuery(query);
             results.next();
             final int retval = results.getInt(COUNT_FIELD);
             results.close();
             return retval;
         }
         catch (Exception e) {
             if (e.getMessage().contains("ORA-00942")) {
                 getLog().debug("Couldn't find " + tableName);
                 getLog().debug("Tried insert statement " + query);
             }
             getLog().debug("Exception executing " + query);
             throw new DatabaseException(e.getMessage(), e);
         }
         finally {
             try {
                 if (statement != null) {
                     statement.close();
                     statement = null;
                 }
             }
             catch (Exception e) {
             }
         }
     }
 
     /**
      * Helper class for incrementing values
      */
     private class Incrementor {
         private int value;
         
         public Incrementor() {
             value = 0;
         }
         
         public int getValue() {
             return value;
         }
 
         public void increment() {
             value++;
         }
 
         public void increment(int by) {
             value += by;
         }
     }
 
     private class ProgressObservable extends Observable {
         public void incrementRecord() {
             setChanged();
             notifyObservers();
             clearChanged();
         }
     }
 
     /**
      * Observer for handling progress
      * 
      */
     private class ProgressObserver implements Observer {
 
         private float total;
         private float progress;
         private float length;
         private float ratio;
         private String template;
         private float count;
         private PrintStream out;
         
         public ProgressObserver(final float total,
                                 final float length,
                                 final float ratio,
                                 final String template) {
             this.total    = total;
             this.template = template;
             this.ratio    = ratio;
             this.length   = length;
             this.count    = 0;
             
             out = System.out;
             /*
             try {
                 final Field field = Main.class.getDeclaredField("out");
                 field.setAccessible(true);
                 out = (PrintStream) field.get(null);
             }
             catch (Exception e) {
                 e.printStackTrace();
             }
             */
         }
 
         public synchronized void update(Observable o, Object arg) {
             count++;
 
             final int percent = (int) ((count / total) * 100f);
             final int progress = (int) ((count / total) * (100f * ratio));
             final StringBuilder progressBuffer = new StringBuilder();
                 
             for (int x = 0; x < progress; x++) {
                 progressBuffer.append('=');
             }
             
             for (int x = progress; x < length; x++) {
                 progressBuffer.append(' ');
             }
             int roll = (int) (count / (total / 1000));
 
 	    out.print(String.format(template, progressBuffer, carr[roll % carr.length], percent, (int) count, (int) total));
         }
     }
 
     /**
      * Get the sourceDriverClass attribute on this object
      *
      * @return sourceDriverClass value
      */
    public String getTemplate() {
         return this.sourceDriverClass;
     }
 
     /**
      * Set the sourceDriverClass attribute on this object
      *
      * @param sourceDriverClass value to set
      */
     public void setSourceDriverClass(final Class sourceDriverClass) {
         this.sourceDriverClass = sourceDriverClass;
     }
 
     /**
      * Get the sourceSchema attribute on this object
      *
      * @return sourceSchema value
      */
     public String getSourceSchema() {
         return this.sourceSchema;
     }
 
     /**
      * Set the sourceSchema attribute on this object
      *
      * @param sourceSchema value to set
      */
     public void setSourceSchema(final String sourceSchema) {
         this.sourceSchema = sourceSchema;
     }
 
     /**
      * Get the sourcePass attribute on this object
      *
      * @return sourcePass value
      */
     public String getSourcePass() {
         return this.sourcePass;
     }
 
     /**
      * Set the sourcePass attribute on this object
      *
      * @param sourcePass value to set
      */
     public void setSourcePass(final String sourcePass) {
         this.sourcePass = sourcePass;
     }
 
     /**
      * Get the sourceUser attribute on this object
      *
      * @return sourceUser value
      */
     public String getSourceUser() {
         return this.sourceUser;
     }
 
     /**
      * Set the sourceUser attribute on this object
      *
      * @param sourceUser value to set
      */
    public void sourceUser(final String sourceUser) {
         this.sourceUser = sourceUser;
     }
 
     /**
      * Get the sourceUrl attribute on this object
      *
      * @return sourceUrl value
      */
     public String getSourceUrl() {
         return this.sourceUrl;
     }
 
     /**
      * Set the sourceUrl attribute on this object
      *
      * @param sourceUrl value to set
      */
     public void setSourceUrl(final String sourceUrl) {
         this.sourceUrl = sourceUrl;
     }
 
     /**
      * Get the source attribute on this object
      *
      * @return source value
      */
     public Database getSource() {
         return this.source;
     }
 
     /**
      * Set the source attribute on this object
      *
      * @param source value to set
      */
     public void setSource(final Database source) {
         this.source = source;
     }
 
     /**
      * Get the target attribute on this object
      *
      * @return target value
      */
     public Database getTarget() {
         return this.target;
     }
 
     /**
      * Set the target attribute on this object
      *
      * @param target value to set
      */
     public void setTarget(final Database target) {
         this.source = source;
     }
 
     protected boolean isDebugEnabled() {
 	return getLog().getLogLevel() == LogLevel.DEBUG;
     }
 
     protected Logger getLog() {
 	return LogFactory.getLogger();
     }
 }
