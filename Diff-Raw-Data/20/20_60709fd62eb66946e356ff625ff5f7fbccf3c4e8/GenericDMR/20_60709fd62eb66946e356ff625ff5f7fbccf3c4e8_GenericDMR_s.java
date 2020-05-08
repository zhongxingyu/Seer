 /*    Copyright 2014 Certifi Inc.
  *
  *    This file is part of AuditTableGen.
  *
  *        AuditTableGen is free software: you can redistribute it and/or modify
  *        it under the terms of the GNU General Public License as published by
  *        the Free Software Foundation, either version 3 of the License, or
  *        (at your option) any later version.
  *
  *        AuditTableGen is distributed in the hope that it will be useful,
  *        but WITHOUT ANY WARRANTY; without even the implied warranty of
  *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *        GNU General Public License for more details.
  *
  *        You should have received a copy of the GNU General Public License
  *        along with AuditTableGen.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.certifi.audittablegen;
 
 import com.google.common.base.Throwables;
 import java.sql.*;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import javax.sql.DataSource;
 import liquibase.Liquibase;
 import liquibase.database.Database;
 import liquibase.database.DatabaseFactory;
 import liquibase.database.jvm.JdbcConnection;
 import liquibase.exception.DatabaseException;
 import liquibase.exception.LiquibaseException;
 import liquibase.resource.FileSystemResourceAccessor;
 import org.apache.commons.collections.map.CaseInsensitiveMap;
 import org.apache.commons.dbcp.BasicDataSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  *
  * @author Glenn Sacks
  */
 class GenericDMR implements DataSourceDMR {
     private static final Logger logger = LoggerFactory.getLogger(GenericDMR.class);
     
     DataSource dataSource;
     String databaseProduct;
     String unverifiedSchema;
     String verifiedSchema;
     String unverifiedAuditConfigTable = "auditconfig";
     String verifiedAuditConfigTable;
     String lastSQL; //adding this for testing
     String sessionUserSQL;
     Queue<List<DBChangeUnit>> operations = new ArrayDeque<>();
     Map<String, DataTypeDef> dataTypes = null;
     //IdentifierMetaData idMetaData;
    
     
     /**
      * 
      * @param ds A DataSource. Unless set elsewhere,
      * the default database/schema will be targeted.
      * 
      * @throws SQLException 
      */
     GenericDMR (DataSource ds) throws SQLException{
         
         this (ds, null);
         
     }
     /**
      *
      * @param ds A DataSource
      *
      * @param schema Name of schema to perform operations upon.
      * @throws SQLException
      */
     GenericDMR(DataSource ds, String schema) throws SQLException {
 
         dataSource = ds;
         Connection conn = ds.getConnection();
         DatabaseMetaData dmd = conn.getMetaData();
         databaseProduct = dmd.getDatabaseProductName();
         //idMetaData = new IdentifierMetaData();
 
         //storing this data for potential future use.
         //not using it for anything currently
         //idMetaData.setStoresLowerCaseIds(dmd.storesLowerCaseIdentifiers());
         //idMetaData.setStoresMixedCaseIds(dmd.storesMixedCaseIdentifiers());
         //idMetaData.setStoresUpperCaseIds(dmd.storesUpperCaseIdentifiers());
 
         unverifiedSchema = schema;
 
         conn.close();
 
     }
     
     /**
      * Generate a DataSource from Properties 
      * @param props
      * @return BasicDataSource as DataSource
      */
     static DataSource getRunTimeDataSource(Properties props){
         
         BasicDataSource dataSource = new BasicDataSource();
         
         dataSource.setDriverClassName(props.getProperty("driver", ""));
         dataSource.setUsername(props.getProperty("username"));
         dataSource.setPassword(props.getProperty("password"));
         dataSource.setUrl(props.getProperty("url"));
         dataSource.setMaxActive(10);
         dataSource.setMaxIdle(5);
         dataSource.setInitialSize(5);
         
         //dataSource.setValidationQuery("SELECT 1");
         
         return dataSource;
     }
     
     /**
      * Return true of the audit configuration source is
      * avaliable.  Only one source is currently supported, and
      * that is a table in the target database/schema named
      * auditconfig.
      * 
      * @return 
      */
     @Override
     public Boolean hasAuditConfigTable (){
         
         return ( (getAuditConfigTableName() != null) ? true : false );
         
     }
    
     
     @Override
     public void createAuditConfigTable() {
         
         try {
             Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()));
             String schema = getSchema();
             if (null != schema) {
                 database.setDefaultSchemaName(schema);
             }
             Liquibase liquibase = new Liquibase("src/main/resources/changesets/changeset-init-config.xml", new FileSystemResourceAccessor(), database);
             liquibase.update(null);
             database.close();
         } catch (SQLException ex) {
             logger.error("Error genereating audit configuration tables", ex);
         } catch (DatabaseException ex) {
             logger.error("Error genereating audit configuration tables", ex);
         } catch (LiquibaseException ex) {
             logger.error("Error genereating audit configuration tables", ex);
         }
   
     }
     
 //    public void createAuditConfigTable2() {
 //        
 //        StringBuilder builder  = new StringBuilder();
 //        
 //        builder.append("create table ").append(this.unverifiedAuditConfigTable).append("(").append(System.lineSeparator());
 //        builder.append("...the rest of theh create script...this will generate an error");
 //        
 //        try (Connection conn = dataSource.getConnection()) {
 //            String schema = getSchema();
 //            if (null != schema) {
 //                conn.setSchema(schema);
 //            }
 //
 //            Statement stmt = conn.createStatement();
 //            stmt.executeUpdate(builder.toString());
 //            
 //            stmt.close();
 //        } catch (SQLException ex) {
 //            logger.error("Error genereating audit configuration tables", ex);
 //        }
 //        
 //    }
 
     
     /**
      * Read the configuration attributes from the audit configuration
      * table in the target database/schema and return as a list
      *
      * @return A list of ConfigAttribute objects or an empty list if none are found.
      */
     @Override
     public List getConfigAttributes(){
         
         StringBuilder builder = new StringBuilder();
         String schema;
         
         if (verifiedSchema != null){
             schema = verifiedSchema + ".";
         }
         else {
             schema = "";
         }
         
         builder.append("select attribute, tablename, columnname, value from ").append(schema).append(verifiedAuditConfigTable);
                 
         List<ConfigAttribute> attributes = new ArrayList();
         
         try {
  
             Connection conn = dataSource.getConnection();
             //String defaultSchema = conn.getSchema();
 
             //if ( schema != null){
             //    conn.setSchema(schema);
             //}
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(builder.toString());
             
             while (rs.next()){
                 //load attributes into configSource
                 ConfigAttribute attrib = new ConfigAttribute();
                 attrib.setAttribute(rs.getString("attribute"));
                 attrib.setTableName(rs.getString("tablename"));
                 attrib.setColumnName(rs.getString("columnname"));
                 attrib.setValue(rs.getString("value"));
                 
                 attributes.add(attrib);
                 
             }     
             
             //conn.setSchema(defaultSchema);
             
             rs.close();
             stmt.close();
             conn.close();
             
         } catch (SQLException ex) {
             logger.error("Error retrieving audit configuration " + ex.getMessage());
         }
         
         return attributes;
              
     }
     
     /**
      * Get List of TableDef objects for all tables
      * in the targeted database/schema
      * 
      * @return ArrayList of TableDef objects or an empty list if none are found.
      */
     @Override
     public List getTables (){
      
         List<TableDef> tables = new ArrayList<>();
         
         try (Connection conn = dataSource.getConnection()){
             
             DatabaseMetaData dmd = conn.getMetaData();
             ResultSet rs = dmd.getTables(null, verifiedSchema, null, new String[]{"TABLE"});
             
             while (rs.next()){
                 TableDef tableDef = new TableDef();
                 tableDef.setName(rs.getString("TABLE_NAME").trim());
                 tables.add(tableDef);
                 
 //                //ToDo: handle case where table full name matches the prefix or postfi
 //                if ( table.toUpperCase().startsWith(configSource.getTablePrefix().toUpperCase())
 //                     && table.toUpperCase().endsWith(configSource.getTablePostfix().toUpperCase())){
 //                    configSource.addExistingAuditTable(table);
 //                }
 //                else {
 //                    configSource.ensureTableConfig(table);
 //                    
 //                    //just in case audit config has set up the table with the
 //                    //wrong case sensitivity, update the table name with the
 //                    //value returned from the db
 //                    TableConfig tc = configSource.getTableConfig(table);
 //                    tc.setTableName(table);
 //                }
             }
             
             rs.close();
             
         } catch (SQLException e){
             logger.error("SQL error retrieving table list: ", e);
             return null;
         }
         
         for ( TableDef tableDef : tables){
             tableDef.setColumns(getColumns(tableDef.getName()));
         }
         
         return tables;
  
     }
     
     /**
      * Get List of ColumnDef objects for all tables
      * in the targeted database/schema
      * 
      * @param tableName
      * @return ArrayList of ColumnDef objects or an empty list if none are found.
      */
     @Override
     public List getColumns (String tableName){
         
         //getDataTypes will initialize the map if it isn't already loaded
         Map<String, DataTypeDef> dtds = getDataTypes();
         
         List columns = new ArrayList<>();
         
         try {
             Connection conn = dataSource.getConnection();
             DatabaseMetaData dmd = conn.getMetaData();
             ResultSet rs = dmd.getColumns(null, verifiedSchema, tableName, null);
             
             //load all of the metadata in the result set into a map for each column
             
             ResultSetMetaData rsmd = rs.getMetaData();
             int metaDataColumnCount = rsmd.getColumnCount();
             if (! rs.isBeforeFirst()) {
                 throw new RuntimeException("No results for DatabaseMetaData.getColumns(" + verifiedSchema + "." + tableName + ")");
             }
             while (rs.next()){
                 ColumnDef columnDef = new ColumnDef();
                 Map columnMetaData = new CaseInsensitiveMap();
                 for (int i = 1; i <= metaDataColumnCount; i++){
                     columnMetaData.put(rsmd.getColumnName(i), rs.getString(i));
                 }
                 columnDef.setName(rs.getString("COLUMN_NAME"));
                 columnDef.setTypeName(rs.getString("TYPE_NAME"));
                 columnDef.setSqlType(rs.getInt("DATA_TYPE"));
                 columnDef.setSize(rs.getInt("COLUMN_SIZE"));
                 columnDef.setDecimalSize(rs.getInt("DECIMAL_DIGITS"));
                 columnDef.setSourceMeta(columnMetaData);
                 
                 if (dtds.containsKey(columnDef.getTypeName())){
                     columnDef.setDataTypeDef(dtds.get(columnDef.getTypeName()));
                 }
                 else {
                     throw new RuntimeException("Missing DATA_TYPE definition for data type " + columnDef.getTypeName());
                 }
                 columns.add(columnDef);
             }
             
         }
         catch (SQLException e) {
             throw Throwables.propagate(e);
         }
         
         return columns;
         
     }
 
     public Map<String, DataTypeDef> getDataTypes (){
         
         if (this.dataTypes != null){
             return this.dataTypes;
         }
            
         //this is kind of ugly.  Some databases (postgres) return the VALUES of
         //metadata in lowercase, while others (hsqldb) return the VALUES of
         //metadata in uppercase.  This is why the key values are stored as
         //case insensitive - so it will work with default types for 
         Map<String, DataTypeDef> types = new CaseInsensitiveMap();
 
         try {
             Connection conn = dataSource.getConnection();
             DatabaseMetaData dmd = conn.getMetaData();
             ResultSet rs = dmd.getTypeInfo();
 
             if (!rs.isBeforeFirst()) {
                 throw new RuntimeException("No results for DatabaseMetaData.getTypeInfo()");
             }
             while (rs.next()) {
 
                 DataTypeDef dtd = new DataTypeDef();
 
                 dtd.type_name = rs.getString("TYPE_NAME");
                 dtd.data_type = rs.getInt("DATA_TYPE");
                 dtd.precision = rs.getInt("PRECISION");
                 dtd.literal_prefix = rs.getString("LITERAL_PREFIX");
                 dtd.literal_suffix = rs.getString("LITERAL_SUFFIX");
                 dtd.create_params = rs.getString("CREATE_PARAMS");
                 dtd.nullable = rs.getShort("NULLABLE");
                 dtd.case_sensitive = rs.getBoolean("CASE_SENSITIVE");
                 dtd.searchable = rs.getShort("SEARCHABLE");
                 dtd.unsigned_attribute = rs.getBoolean("UNSIGNED_ATTRIBUTE");
                 dtd.fixed_prec_scale = rs.getBoolean("FIXED_PREC_SCALE");
                 dtd.auto_increment = rs.getBoolean("AUTO_INCREMENT");
                 dtd.local_type_name = rs.getString("LOCAL_TYPE_NAME");
                 dtd.minimum_scale = rs.getShort("MINIMUM_SCALE");
                 dtd.maximum_scale = rs.getShort("MAXIMUM_SCALE");
                 dtd.sql_data_type = rs.getInt("SQL_DATA_TYPE"); //not used
                 dtd.sql_datetime_sub = rs.getInt("SQL_DATETIME_SUB"); //not used
                 dtd.num_prec_radix = rs.getInt("NUM_PREC_RADIX");
 
                 types.put(dtd.type_name, dtd);
 
             }
 
         } catch (SQLException e) {
             throw Throwables.propagate(e);
         }
 
         this.dataTypes = types;
         
         Collection<String> debugValues = types.keySet();
         for (String debugValue : debugValues) {
             logger.debug("DB has type value {}", debugValue);
 
         }
         
         return types;
 
     }
     
    @Override
     public void setSchema(String unverifiedSchema) {
 
         this.unverifiedSchema = unverifiedSchema;
         this.verifiedSchema = null;
         
         if(unverifiedSchema == null){
             return;
         }
         
         try (Connection conn = dataSource.getConnection()){
 
             DatabaseMetaData dmd = conn.getMetaData();
             ResultSet rs = dmd.getSchemas();
             while (rs.next()) {
                 if (rs.getString("TABLE_SCHEM").trim().equalsIgnoreCase(unverifiedSchema)) {
                     //store value with whatever case sensitivity it is returned as
                     verifiedSchema = rs.getString("TABLE_SCHEM").trim();
                 }
             }
             rs.close();
         } catch (SQLException e) {
             logger.error("error verifying schema", e);
         }
     }
     
     @Override
     public String getSchema() {
 
         if (verifiedSchema == null
                 && unverifiedSchema != null) {
             setSchema(unverifiedSchema);
         }
 
         return verifiedSchema;
     }
     
     @Override
     public void setAuditConfigTableName (String unverifiedTable){
         
         this.unverifiedAuditConfigTable = unverifiedTable;
         this.verifiedAuditConfigTable = null;
         String candidate = null;
         boolean multiMatch = false;
         
         if(unverifiedAuditConfigTable == null){
             return;
         }
         
         if (null == verifiedSchema){
             logger.error("attempting to verify auditConfigTable with unverified schema");
         }
         
         try (Connection conn = dataSource.getConnection()){
 
             DatabaseMetaData dmd = conn.getMetaData();
             ResultSet rs = dmd.getTables(null, null == verifiedSchema ? null : verifiedSchema, null, null);
             while (rs.next()) {
                 if (rs.getString("TABLE_NAME").trim().equalsIgnoreCase(unverifiedTable)) {
                     //store value with whatever case sensitivity it is returned as
                     if (candidate == null){
                     candidate = rs.getString("TABLE_NAME").trim();
                     }
                     else{
                         multiMatch = true;
                     }
                 }
             }
             rs.close();
         } catch (SQLException e) {
             logger.error("error verifying auditConfigTable", e);
         }
         
         /** Fails to set verified value if more than one match.
          * This can occur if schema is not set and there are multiple
          * tables in different schemas matching the table name.
          */
         if (!multiMatch){
             this.verifiedAuditConfigTable = candidate;
         }
         
     }
     
     @Override
     public String getAuditConfigTableName(){
          if (verifiedAuditConfigTable == null
                 && unverifiedAuditConfigTable != null) {
             setAuditConfigTableName(unverifiedAuditConfigTable);
         }
 
         return verifiedAuditConfigTable;
     }
 
     @Override
     public void readDBChangeList(List<DBChangeUnit> units) {
         
         //The change list should be valid, and any code in this method
         //which is checking errors should not be required.  It is here
         //for early development sake, but the list really should be validated
         //before it is submitted here.
         
         List<DBChangeUnit> workList = null;
         Boolean beginTag = false;
         DBChangeType workListType = DBChangeType.notSet;
         
         if (!DBChangeUnit.validateUnitList(units)){
             logger.error("Invalid DBChangeUnitList submitted.  Not processing");
             return;
         }
         
         //pull apart each <begin-stuff-end> and submit into queue.
         for ( DBChangeUnit unit : units) {
             switch (unit.getChangeType()){
                 case begin:
                     //start 'work unit list'
                     beginTag = true;
                     workList = new ArrayList<>();
                     workList.add(unit);
                     break;
                 case end:
                     beginTag = false;
                     workList.add(unit);
                     //add to work queue
                     operations.add(workList);
                     break;
                 case createTable:
                 case alterTable:
                 case createTriggers:
                 case dropTriggers:
                     workListType = unit.getChangeType();
                     workList.add(unit);
                     break;
                 case addColumn:                    
                 case alterColumnName:
                 case alterColumnSize:
                 case alterColumnType:                    
                 case addTriggerColumn:
                 case fireOnInsert:
                 case fireOnUpdate:
                 case fireOnDelete:
                 case addTriggerAction:
                 case addTriggerTimeStamp:
                 case addTriggerUser:
                 case addTriggerSessionUser:
                     workList.add(unit);
                     break;
                 case notSet:
                 default:
                     //should not get here if the list is valid, unless a new changetype
                     //was added that this DMR does not know about.  If which case - fail.
                     logger.error ("unimplemented DBChangeUnit {}", unit.getChangeType().toString());
                     return;
                     
             }
             
         }
 
     }
 
     @Override
     public void executeChanges() {
 
         List<DBChangeUnit> op;
         String query;
 
         while (!operations.isEmpty()) {
             op = operations.poll();
 
             //validate it one more time, totally not necessary :)
             if (!DBChangeUnit.validateUnitList(op)) {
                 logger.error("Invalid DBChangeUnitList submitted.  Not processing");
             }
             
             switch (op.get(1).changeType) {
                 case createTable:
                     query = getCreateTableSQL(op);
                     break;
                 case alterTable:
                     query = getAlterTableSQL(op);
                     break;
                 case createTriggers:
                     query = getCreateTriggerSQL(op);
                     break;
                 case dropTriggers:
                     query = getDropTriggerSQL(op);
                 default:
                     //should not get here if the list is valid, unless a new changetype
                     //was added that this DMR does not know about.  If which case - fail.
                     logger.error("unimplemented DBChangeUnit {}", op.get(1).getChangeType().toString());
                     return;
             }
             
             if (query == null){
                 logger.error("Error generating update SQL for changeList: {}", DBChangeUnit.ListToString(op));
                 return;
             }
             else {
                 executeUpdate(query);
             }
             
         }
     }
 
     String getCreateTableSQL(List<DBChangeUnit> op) {
         
         StringBuilder builder = new StringBuilder();
         StringBuilder constraints = new StringBuilder();
         DataTypeDef dataTypeDef = null;
         boolean firstCol = true;
         String schema;
         
         if (verifiedSchema != null){
             schema = verifiedSchema + ".";
         }
         else {
             schema = "";
         }
 
         for (DBChangeUnit unit : op) {
             switch (unit.changeType) {
                 case begin:
                     //nothinig
                     break;
                 case end:
                     builder.append(constraints);
                     builder.append(")").append(System.lineSeparator());
                     //execute SQL here...
                     break;
                 case createTable:
                     builder.append("CREATE TABLE ").append(schema).append(unit.tableName).append(" (").append(System.lineSeparator());
                     break;
                 case addColumn:
                     if (!firstCol){
                         builder.append(", ");
                     }
                     else {
                         firstCol = false;
                     }
                     
                     dataTypeDef = getDataType(unit.typeName);
                     
                     if (unit.identity){
                         builder.append(unit.columnName).append(" ").append("serial PRIMARY KEY").append(System.lineSeparator());
                     }
                     else {
                         builder.append(unit.columnName).append(" ").append(unit.typeName);
                        if (dataTypeDef.create_params != null &&  unit.size > 0){
                             builder.append(" (").append(unit.size);
                         
                             if (unit.decimalSize > 0){
                                 builder.append(",").append(unit.decimalSize);
                             }
                             builder.append(") ");
                         }
                         if (!unit.foreignTable.isEmpty()){
                             builder.append("REFERENCES ").append(unit.foreignTable).append(" (").append(unit.columnName).append(")");
                             //constraints.append("CONSTRAINT ").append(unit.columnName).append(" REFERENCES ").append(unit.foreignTable);
                         }
                         builder.append(System.lineSeparator());
                     }
                     break;
                 default:
                     //should not get here if the list is valid, unless a new changetype
                     //was added that this DMR does not know about.  If which case - fail.
                     logger.error("unimplemented DBChangeUnit '{}' for create table operation", unit.getChangeType().toString());
                     return null;
             }
         }
         
         return builder.toString();
         
     }
 
     String getAlterTableSQL(List<DBChangeUnit> op) {
         
         StringBuilder builder = new StringBuilder();
         StringBuilder constraints = new StringBuilder();
         DataTypeDef dataTypeDef = null;
         boolean firstCol = true;
         String schema;
         
         if (verifiedSchema != null){
             schema = verifiedSchema + ".";
         }
         else {
             schema = "";
         }
 
         for (DBChangeUnit unit : op) {
             switch (unit.changeType) {
                 case begin:
                     //nothinig
                     break;
                 case end:
                     builder.append(constraints);
                     break;
                 case alterTable:
                     builder.append("ALTER TABLE ").append(schema).append(unit.tableName).append(System.lineSeparator());
                     break;
                 case addColumn:
                     if (!firstCol){
                         builder.append(", ");
                     }
                     else {
                         firstCol = false;
                     }
                     builder.append("ADD COLUMN ");
                     
                     dataTypeDef = getDataType(unit.typeName);
                     
                     if (unit.identity){
                         builder.append(unit.columnName).append(" ").append("serial PRIMARY KEY").append(System.lineSeparator());
                     }
                     else {
                         builder.append(unit.columnName).append(" ").append(unit.typeName);
                        if (dataTypeDef.create_params != null &&  unit.size > 0){
                             builder.append(" (").append(unit.size);
                         
                             if (unit.decimalSize > 0){
                                 builder.append(",").append(unit.decimalSize);
                             }
                             builder.append(") ");
                         }
                         if (!unit.foreignTable.isEmpty()){
                             builder.append("REFERENCES ").append(unit.foreignTable).append(" (").append(unit.columnName).append(")");
                             //constraints.append("CONSTRAINT ").append(unit.columnName).append(" REFERENCES ").append(unit.foreignTable);
                         }
                         builder.append(System.lineSeparator());
                     }
                     break;
                 case alterColumnSize:
                 case alterColumnType:
                     if (!firstCol){
                         builder.append(", ");
                     }
                     else {
                         firstCol = false;
                     }
                     builder.append("ALTER COLUMN ").append(unit.columnName).append(" TYPE ").append(unit.typeName);
                     if (unit.size > 0) {
                         builder.append(" (").append(unit.size);
 
                         if (unit.decimalSize > 0) {
                             builder.append(",").append(unit.decimalSize);
                         }
                         builder.append(") ");
                     }
                     builder.append(System.lineSeparator());
                     break;
                 case alterColumnName:
                     if (!firstCol){
                         builder.append(", ");
                     }
                     else {
                         firstCol = false;
                     }
                     builder.append("RENAME COLUMN ").append(unit.columnName).append(" TO ").append(unit.newColumnName);
                     builder.append(System.lineSeparator());
                     
                     break;
                 default:
                     //should not get here if the list is valid, unless a new changetype
                     //was added that this DMR does not know about.  If which case - fail.
                     logger.error("unimplemented DBChangeUnit {} for alter table operation", unit.getChangeType().toString());
                     return null;
             }
         }
         
         return builder.toString();
 
     }
 
     String getCreateTriggerSQL(List<DBChangeUnit> op) {
         
         StringBuilder builder = new StringBuilder();
         StringBuilder insertDetail = new StringBuilder();
         StringBuilder deleteDetail = new StringBuilder();
         StringBuilder updateDetail = new StringBuilder();
         StringBuilder updateConditional = new StringBuilder();
         String functionName = null;
         String triggerName = null;
         String triggerReference = null;
         String tableName = null;
         String auditTableName = null;
         String actionColumn = null;
         String userColumn = null;
         String sessionUserColumn = null;
         String timeStampColumn = null;
         boolean firstTrig = true;
         boolean onDelete = true;
         boolean onInsert = true;
         boolean onUpdate = true;
         List<String> columns = new ArrayList<>();
         List<String> whenColumns = new ArrayList<>();
         String schema;
         
         if (verifiedSchema != null){
             schema = verifiedSchema + ".";
         }
         else {
             schema = "";
         }
 
         for (DBChangeUnit unit : op) {
             switch (unit.changeType) {
                 case begin:
                     //nothinig
                     break;
                 case end:
                     if (actionColumn == null || timeStampColumn == null || userColumn == null){
                         logger.error("Trigger info for table %s missing audit columns for: %s %s %s",
                                 tableName, actionColumn == null ? "action " : "",
                                 timeStampColumn == null ? "timeStamp " : "",
                                 userColumn == null ? "user" : "");
                         return null;
                     }
                     
                     //////////////////////
                     //generate the when clause for the update trigger
                     if (columns.size() > whenColumns.size() ){
                         //some columns excluded from update
                         updateConditional.append("AND (");
                         boolean firstCol = true;
                         for (String col : whenColumns){
                             if (!firstCol){
                                 updateConditional.append("            OR ");
                              
                             }
                             firstCol = false;
                             updateConditional.append("OLD.").append(col).append(" IS DISTINCT FROM NEW.").append(col).append(System.lineSeparator());
                         }
                         updateConditional.append("            )) THEN").append(System.lineSeparator());                       
                     }
                     else {
                         //no column conditions.  Alwasy insert audit row on update
                         updateConditional.append(") THEN").append(System.lineSeparator());
                     }
                     
                     //////////////////////                     
                     //generate the detail insert column list for the trigger(s)
                     insertDetail.append(String.format("        INSERT INTO %s%s (%s, %s, %s", schema, auditTableName, actionColumn, userColumn, timeStampColumn));
                     updateDetail.append(String.format("        INSERT INTO %s%s (%s, %s, %s", schema, auditTableName, actionColumn, userColumn, timeStampColumn));
                     deleteDetail.append(String.format("        INSERT INTO %s%s (%s, %s, %s", schema, auditTableName, actionColumn, userColumn, timeStampColumn));
     
                     if (sessionUserColumn != null){
                         insertDetail.append(", ").append(sessionUserColumn);
                         updateDetail.append(", ").append(sessionUserColumn);
                         deleteDetail.append(", ").append(sessionUserColumn);
                     }
                     for (String col : columns){
                         insertDetail.append(", ").append(col);
                         updateDetail.append(", ").append(col);
                         deleteDetail.append(", ").append(col);
                     }
                     insertDetail.append(")").append(System.lineSeparator());
                     updateDetail.append(")").append(System.lineSeparator());
                     deleteDetail.append(")").append(System.lineSeparator());
                     
                     //////////////////////
                     //generate the insert column valuues for the trigger(s)
                     insertDetail.append("        SELECT 'insert', user, now()");
                     updateDetail.append("        SELECT 'update', user, now()");
                     deleteDetail.append("        SELECT 'delete', user, now()");
                     if (sessionUserColumn != null){
                         insertDetail.append(", ").append(this.getSessionUserSQL());
                         updateDetail.append(", ").append(this.getSessionUserSQL());
                         deleteDetail.append(", ").append(this.getSessionUserSQL());
                     }
                     for (String col : columns){
                         insertDetail.append(", NEW.").append(col);
                         updateDetail.append(", NEW.").append(col);
                         deleteDetail.append(", OLD.").append(col);
                     }
                     insertDetail.append(";").append(System.lineSeparator());
                     updateDetail.append(";").append(System.lineSeparator());
                     deleteDetail.append(";").append(System.lineSeparator());
                     insertDetail.append("        RETURN NEW;").append(System.lineSeparator());
                     updateDetail.append("        RETURN NEW;").append(System.lineSeparator());
                     deleteDetail.append("        RETURN OLD;").append(System.lineSeparator());
                     
                     //////////////////////
                     //creat the function that the trigger calls
                     builder.append("CREATE OR REPLACE FUNCTION ").append(schema).append(functionName).append(" RETURNS TRIGGER AS ")
                             .append(triggerReference).append(System.lineSeparator());
                     builder.append("BEGIN").append(System.lineSeparator());
                     builder.append("    IF (TG_OP = 'DELETE') THEN").append(System.lineSeparator());
                     builder.append(deleteDetail);                    
                     builder.append("    ELSEIF (TG_OP = 'INSERT') THEN").append(System.lineSeparator());
                     builder.append(insertDetail);
                     builder.append("    ELSEIF (TG_OP = 'UPDATE' ").append(updateConditional).append(System.lineSeparator());
                     builder.append(updateDetail);
                     builder.append("    END IF;").append(System.lineSeparator());
                     builder.append("END").append(System.lineSeparator());
                     builder.append(triggerReference).append(" LANGUAGE plpgsql;").append(System.lineSeparator());
                     
                     ///////////////////////                    
                     //create the trigger
                     builder.append("DROP TRIGGER IF EXISTS ").append(triggerName).append(" ON ").append(schema).append(tableName).append(";").append(System.lineSeparator());
                     builder.append("CREATE TRIGGER ").append(triggerName).append(System.lineSeparator());
                     builder.append("AFTER ");
                     if (onInsert){
                         builder.append("INSERT ");
                         firstTrig = false;
                     }
                     if (onUpdate){
                         if (!firstTrig){
                             builder.append("OR UPDATE ");
                         }
                         else {
                             builder.append("UPDATE ");
                             firstTrig = false;
                         }
                     }
                     if (onDelete){
                         if (!firstTrig){
                             builder.append("OR DELETE ");
                         }
                         else {
                             builder.append("DELETE ");
                         }
                     }
                     builder.append("ON ").append(schema).append(tableName).append(System.lineSeparator());
                     builder.append("FOR EACH ROW EXECUTE PROCEDURE ").append(schema).append(functionName).append(";").append(System.lineSeparator());
                     //run the sql...
                     break;
                 case createTriggers:
                     tableName = unit.getTableName();
                     triggerName = unit.getTableName() + "_audit";
                     functionName = "process_" + triggerName + "()";
                     triggerReference = "$" + triggerName + "$";
                     auditTableName = unit.getAuditTableName();
                     break;
                 case fireOnDelete:
                     onDelete = unit.getFiresTrigger();
                     break;
                 case fireOnInsert:
                     onInsert = unit.getFiresTrigger();
                     break;
                 case fireOnUpdate:
                     onUpdate = unit.getFiresTrigger();
                     break;
                 case addTriggerColumn:
                     if (unit.firesTrigger){
                         whenColumns.add(unit.getColumnName());
                     }
                     columns.add(unit.columnName);
                     break;
                 case addTriggerAction:
                     actionColumn = unit.getColumnName();
                     break;
                 case addTriggerUser:
                     userColumn = unit.getColumnName();
                     break;
                 case addTriggerTimeStamp:
                     timeStampColumn = unit.getColumnName();
                     break;
                 case addTriggerSessionUser:
                     sessionUserColumn = unit.getColumnName();
                     break;
                 default:
                     //should not get here if the list is valid, unless a new changetype
                     //was added that this DMR does not know about.  If which case - fail.
                     logger.error("unimplemented DBChangeUnit {} for create trigger operation", unit.getChangeType().toString());
                     return null;
             }
         }
         
         return builder.toString();
         
     }
 
     String getDropTriggerSQL(List<DBChangeUnit> op) {
         
         StringBuilder builder = new StringBuilder();
         String triggerName;
         String schema;
         
         if (verifiedSchema != null){
             schema = verifiedSchema + ".";
         }
         else {
             schema = "";
         }
 
         for (DBChangeUnit unit : op) {
             switch (unit.changeType) {
                 case begin:
                     //nothing
                     break;
                 case end:
                     //run the sql...
                     break;
                 case dropTriggers:
                     triggerName = unit.tableName + "_audit";
                     builder.append("DROP TRIGGER IF EXISTS ").append(triggerName).append(" ON ").append(schema).append(unit.tableName).append(";").append(System.lineSeparator());
                     break;
                 default:
                     //should not get here if the list is valid, unless a new changetype
                     //was added that this DMR does not know about.  If which case - fail.
                     logger.error("unimplemented DBChangeUnit {} for drop trigger operation", unit.getChangeType().toString());
                     return null;
             }
         }
         
         return builder.toString();
         
     }
         
     public void executeUpdate (String query){
 
         String schema = this.getSchema();
         
         logger.debug("running SQL");
         logger.debug(query);
         
         try (Connection conn = dataSource.getConnection()) {
             String defaultSchema = null;
             
 //            try {
 //                defaultSchema = conn.getSchema();
 //            }
 //            catch (Exception e) {
 //                logger.error("Connection.getSchema not implemented", e);
 //            }
 //            if (null != schema) {
 //                conn.setSchema(schema);
 //            }
             Statement stmt = conn.createStatement();
             stmt.executeUpdate(query);
             stmt.close();
 
             //just in case this code is called with a pooled dataSource
 //            conn.setSchema(defaultSchema);
             
             
         } catch (SQLException ex) {
             logger.error("Update failed...", ex);
         }
     }
 
     @Override
     public void executeDBChangeList(List<DBChangeUnit> units) {
         readDBChangeList(units);
         executeChanges();
     }
 
     @Override
     public void purgeDBChanges() {
         operations.clear();
     }
 
     @Override
     public int getMaxUserNameLength() {
         
         Integer length = -1;
          try (Connection conn = dataSource.getConnection()){
 
             DatabaseMetaData dmd = conn.getMetaData();
             length = dmd.getMaxUserNameLength();
             
         } catch (SQLException e) {
             logger.error("error getting maxUserNameLength", e);
         }
 
          return length;
     }
     
     @Override
     public DataTypeDef getDataType (String typeName){
         
         Map<String, DataTypeDef> dtds = this.getDataTypes();
         
         if (dtds.containsKey(typeName)){
             return dtds.get(typeName);
         }
         else {
             return null;
         }
                   
     }
     
     @Override
     public DataTypeDef getDataType (int dataType){
         
         Map<String, DataTypeDef> dtds = this.getDataTypes();
         
         for (DataTypeDef dtd : dtds.values()) {
             if (dtd.data_type == dataType){
                 return dtd;
             }
         }
         
         return null;
     }
 
     @Override
     public void setSessionUserSQL(String sql) {
         
         this.sessionUserSQL = "(" + sql + ")";
         
     }
 
     @Override
     public String getSessionUserSQL() {
         
         return sessionUserSQL;
     }
     
 }
