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
 
 package com.gooddata.connector.driver;
 
 import com.gooddata.connector.model.PdmColumn;
 import com.gooddata.connector.model.PdmLookupReplication;
 import com.gooddata.connector.model.PdmSchema;
 import com.gooddata.connector.model.PdmTable;
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.DLIPart;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.naming.N;
 import com.gooddata.util.JdbcUtil;
 import com.gooddata.util.JdbcUtil.StatementHandler;
 import com.gooddata.util.StringUtil;
 import org.apache.log4j.Logger;
 
 import java.sql.*;
 import java.util.*;
 import java.util.Date;
 
 /**
  * GoodData abstract SQL driver. Generates the DDL (tables and indexes), DML (transformation SQL) and other
  * SQL statements necessary for the data normalization (lookup generation)
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public abstract class AbstractSqlDriver implements SqlDriver {
 
     private static Logger l = Logger.getLogger(AbstractSqlDriver.class);
 
     // autoincrement syntax
     protected String SYNTAX_AUTOINCREMENT = "";
 
     // SQL concat function prefix and suffix
     protected String SYNTAX_CONCAT_FUNCTION_PREFIX = "";
     protected String SYNTAX_CONCAT_FUNCTION_SUFFIX = "";
     protected String SYNTAX_CONCAT_OPERATOR = "";
 
     // separates the different LABELs when we concatenate them to create an unique identifier out of them
     protected String HASH_SEPARATOR = "%";
 
 
     /**
      * {@inheritDoc}
      */
     public void executeSystemDdlSql(Connection c) throws SQLException {
         l.debug("Executing system DDL SQL.");
         createSnapshotTable(c);
         createFunctions(c);
         l.debug("System DDL SQL execution finished.");
     }
 
     /**
      * {@inheritDoc}
      */
     public void executeDdlSql(Connection c, PdmSchema schema) throws SQLException {
         l.debug("Executing DDL SQL.");
         for(PdmTable table : schema.getTables()) {
         	if (!exists(c, table.getName())) {
         		createTable(c, table);
         		if (PdmTable.PDM_TABLE_TYPE_LOOKUP.equals(table.getType())) {
         			prepopulateLookupTable(c, table);
         		} else if (PdmTable.PDM_TABLE_TYPE_CONNECTION_POINT.equals(table.getType())) {
         			final List<Map<String,String>> rows = prepareInitialTableLoad(table);
         			if (!rows.isEmpty()) {
         				l.warn("Prepopulating of connection point tables is not suppported (table = " + table.getName() + ")");
         			}
         		}
 	            if(PdmTable.PDM_TABLE_TYPE_SOURCE.equals(table.getType()))
 	                indexAllTableColumns(c, table);
         	} else {
         		for (PdmColumn column : table.getColumns()) {
         			if (!exists(c, table.getName(), column.getName())) {
         				addColumn(c, table, column);
         				if (PdmTable.PDM_TABLE_TYPE_SOURCE.equals(table.getType()))
         					indexTableColumn(c, table, column);
         			}
         		}
         	}
         }
         JdbcUtil.executeUpdate(c,
             "INSERT INTO snapshots(name,firstid,lastid,tmstmp) VALUES ('" + schema.getFactTable().getName() + "',0,0,0)"
         );
         l.debug("DDL SQL Execution finished.");
     }
 
     /**
      * {@inheritDoc}
      */
     public void executeNormalizeSql(Connection c, PdmSchema schema) throws SQLException {
         l.debug("Executing data normalization SQL.");
         //populate REFERENCEs lookups from the referenced lookups
         l.debug("Executing referenced lookups replication.");
         executeLookupReplicationSql(c, schema);
         l.debug("Finished referenced lookups replication.");
         l.debug("Executing lookup tables population.");
         populateLookupTables(c, schema);
         l.debug("Finished lookup tables population.");
         l.debug("Executing connection point tables population.");
         populateConnectionPointTables(c, schema);
         l.debug("FInished connection point tables population.");
         // nothing for the reference columns
         l.debug("Inserting partial snapshot record.");
         insertSnapshotsRecord(c, schema);
         l.debug("Executing fact table population.");
         insertFactsToFactTable(c, schema);
         l.debug("FInished fact table population.");
 
         l.debug("Executing fact table FK generation.");
         updateFactTableFk(c, schema);
         l.debug("Finished fact table FK generation.");
 
         updateSnapshotsRecord(c, schema);
         l.debug("Snapshot record updated.");
         l.debug("Finished data normalization SQL.");
     }
 
 
 
     /**
      * {@inheritDoc}
      */
     public void executeLookupReplicationSql(Connection c, PdmSchema schema) throws SQLException {
         for (PdmLookupReplication lr : schema.getLookupReplications()) {
             JdbcUtil.executeUpdate(c,
                 "DELETE FROM " + lr.getReferencingLookup()
             );
             JdbcUtil.executeUpdate(c,
                 "INSERT INTO " + lr.getReferencingLookup() + "("+N.ID+"," + lr.getReferencingColumn() +","+N.HSH+")" +
                 " SELECT "+ N.ID+"," + lr.getReferencedColumn() + "," + lr.getReferencedColumn() + " FROM " +
                 lr.getReferencedLookup()
             );
         }
     }
     
     /**
      * {@inheritDoc}
      * @throws SQLException 
      */
     public boolean exists(Connection c, String tbl) throws SQLException {
     	DatabaseMetaData md = c.getMetaData();
     	ResultSet rs = md.getTables(null, null, tbl, null);
     	try {
 	    	return rs.next();
     	} finally {
     		if (rs != null)
     			rs.close();
     	}
     }
 
     /**
      * Returns true if the specified column of the specified table exists in the DB. Case sensitive!
      * @param tbl table name
      * @param col column name
      * @return true if the table exists, false otherwise
      * @throws IllegalArgumentException if the required table does not exist
      * @throws SQLException if other database related problem occures 
      */
     public boolean exists(Connection c, String tbl, String col) throws SQLException {
     	if (!exists(c, tbl))
     		throw new IllegalArgumentException("Table '" + tbl + "' does not exist.");
     	String sql = "SELECT * FROM " + tbl + " WHERE 1=0";
 		Statement st = c.createStatement();
 		try {
             ResultSet rs = st.executeQuery(sql);
             try {
 	            ResultSetMetaData md = rs.getMetaData();
 	            int cols = md.getColumnCount();
	            for (int i = 1; i <= cols; i++) {
 	            	if (col.equals(md.getColumnName(i)))
 	            		return true;
 	            }
 	            return false;
     		} finally {
     			if (rs != null)
     				rs.close();
     		}
 		} finally {
 			if (st != null)
 				st.close();
 		}
     }
 
 
     /**
      * Indexes all table columns
      * @param c JDBC connection
      * @param table target table
      * @throws SQLException in case of SQL issues
      */
     protected void indexAllTableColumns(Connection c, PdmTable table) throws SQLException {
         for( PdmColumn column : table.getColumns()) {
             indexTableColumn(c, table, column);
         }
     }
 
     /**
      * Indexes table's column
      * @param c JDBC connection
      * @param table target table
      * @param column target table's columns
      * @throws SQLException in case of SQL issues
      */
     private void indexTableColumn(Connection c, PdmTable table, PdmColumn column) throws SQLException {
     	if(!column.isPrimaryKey() && !column.isUnique()) {
             JdbcUtil.executeUpdate(c,"CREATE INDEX idx_" + table.getName()
             		+ "_" + column.getName()
             		+ " ON " + table.getName() + "("+column.getName()+")");
     	}
     }
 
     /**
      * Creates a new table
      * @param c JDBC connection
      * @param table target table
      * @throws SQLException in case of SQL issues
      */
     protected void createTable(Connection c, PdmTable table) throws SQLException {
         String pk = "";
         String sql = "CREATE TABLE " + table.getName() + " (\n";
         for( PdmColumn column : table.getColumns()) {
             sql += " "+ column.getName() + " " + column.getType();
             if(column.isUnique())
                 sql += " UNIQUE";
             if(column.isAutoIncrement())
                 sql += " " + SYNTAX_AUTOINCREMENT;
             if(column.isPrimaryKey())
                 if(pk != null && pk.length() > 0)
                     pk += "," + column.getName();
                 else
                     pk += column.getName();
             sql += ",";
         }
         sql += " PRIMARY KEY (" + pk + "))";
 
         JdbcUtil.executeUpdate(c, sql);
     }
 
     /**
      * Fills the lookup table with the DISTINCT values from the source table
      * @param c JDBC connection
      * @param table target lookup table
      * @throws SQLException in case of SQL issues
      */
     private void prepopulateLookupTable(Connection c, PdmTable table) throws SQLException {
     	final List<Map<String,String>> rows = prepareInitialTableLoad(table);
     	if (rows.isEmpty())
     		return;
     	
     	// create the list to make sure consistent keys order in the following loop
 		final List<String> columns = new ArrayList<String>(rows.get(0).keySet());
     	final String placeholders = StringUtil.join(", ", columns, "?");
 
     	for (final Map<String,String> row : rows) {
     		
     		final String sql = "INSERT INTO " + table.getName() + " ("
     						 + N.HSH + ", " + StringUtil.join(", ", columns)
     						 + ") VALUES (?, " + placeholders + ")";
     		
     		JdbcUtil.executeUpdate(c, sql, new StatementHandler() {
 				public void prepare(PreparedStatement stmt) throws SQLException {
 					boolean first = true;
 					final StringBuffer hashbf = new StringBuffer();
 					int index = 2;
 					for (final String col : columns) {
 						if (first)
 							first = false;
 						else
 							hashbf.append(HASH_SEPARATOR);
 						hashbf.append(row.get(col));
 						stmt.setString(index++, row.get(col));
 					}
 					stmt.setString(1, hashbf.toString());
 				}
 			});
     	}
     }
 
 
     /**
      * TODO: PK to document
      * @param table target table
      * @return
      */
     private List<Map<String,String>> prepareInitialTableLoad(PdmTable table) {
     	final List<Map<String,String>> result = new ArrayList<Map<String,String>>();
     	final List<PdmColumn> toLoad = new ArrayList<PdmColumn>();
     	int max = 0;
     	for (final PdmColumn col : table.getColumns()) {
     		if (col.getElements() != null && !col.getElements().isEmpty()) {
     			int size = col.getElements().size();
     			if (max == 0)
     				max = size;
     			else if (size != max)
     				throw new IllegalStateException(
     						"Column " + col.getName() + " of table " + table.getName()
     						+ " has a different number of elements than: " + toLoad.toString());
     			toLoad.add(col);
     		}
     	}
     	if (!toLoad.isEmpty()) {    	
 	    	for (int i = 0; i < toLoad.get(0).getElements().size(); i++) {
 	    		final Map<String,String> row = new HashMap<String, String>();
 	    		for (final PdmColumn col : toLoad) {
 	    			row.put(col.getName(), col.getElements().get(i));
 	    		}
 	    		result.add(row);
 	    	}
     	}
     	return result;
     }
 
     /**
      * Add column to the table (ALTER TABLE)
      * @param c JDBC connection
      * @param table target table
      * @param column target column
      * @throws SQLException in case of SQL issues
      */
     private void addColumn(Connection c, PdmTable table, PdmColumn column) throws SQLException {
     	String sql = "ALTER TABLE " + table.getName() + " ADD COLUMN "
     			   + column.getName() + " " + column.getType();
     	if (column.isUnique())
     		sql += " UNIQUE";
     	JdbcUtil.executeUpdate(c, sql);
     }
 
     /**
      * Creates the system snapshots table
      * @param c JDBC connection
      * @throws SQLException in case of a DB issue
      */
     protected void createSnapshotTable(Connection c) throws SQLException {
         JdbcUtil.executeUpdate(c,
             "CREATE TABLE snapshots (" +
                 " id INT " + SYNTAX_AUTOINCREMENT + "," +
                 " name VARCHAR(255)," +
                 " tmstmp BIGINT," +
                 " firstid INT," +
                 " lastid INT," +
                 " PRIMARY KEY (id)" +
                 ")"
         );
     }
 
     /**
      * Inserts new records to the snapshots table before the load
      * @param c JDBC connection
      * @param schema PDM schema
      * @throws SQLException in case of a DB issue
      */
     protected void insertSnapshotsRecord(Connection c, PdmSchema schema) throws SQLException {
         PdmTable factTable = schema.getFactTable();
         String fact = factTable.getName();
         Date dt = new Date();
         JdbcUtil.executeUpdate(c,
             "INSERT INTO snapshots(name,tmstmp,firstid) SELECT '"+fact+"',"+dt.getTime()+",MAX("+N.ID+")+1 FROM " + fact
         );
         // compensate for the fact that MAX returns NULL when there are no rows in the SELECT
         JdbcUtil.executeUpdate(c,
             "UPDATE snapshots SET firstid = 0 WHERE name = '"+fact+"' AND firstid IS NULL"
         );
     }
 
     /**
      * Updates the snapshots table after load
      * @param c JDBC connection
      * @param schema PDM schema
      * @throws SQLException in case of a DB issue
      */
     protected void updateSnapshotsRecord(Connection c, PdmSchema schema) throws SQLException {
         PdmTable factTable = schema.getFactTable();
         String fact = factTable.getName();
         JdbcUtil.executeUpdate(c,
             "UPDATE snapshots SET lastid = (SELECT MAX("+N.ID+") FROM " + fact + ") WHERE name = '" +
             fact + "' AND lastid IS NULL"
         );
         // compensate for the fact that MAX returns NULL when there are no rows in the SELECT
         JdbcUtil.executeUpdate(c,
             "UPDATE snapshots SET lastid = 0 WHERE name = '" + fact + "' AND lastid IS NULL"
         );
     }
 
     /**
      * Inserts rows from the source table to the fact table
      * @param c JDBC connection
      * @param schema PDM schema
      * @throws SQLException in case of a DB issue
      */
     protected abstract void insertFactsToFactTable(Connection c, PdmSchema schema) throws SQLException;
 
     protected void populateLookupTables(Connection c, PdmSchema schema) throws SQLException {
         for(PdmTable lookupTable : schema.getLookupTables()) {
             populateLookupTable(c, lookupTable, schema);
         }
     }
 
     /**
      * Populates the connection point table
      * @param c JDBC connection
      * @param schema PDM schema
      * @throws SQLException in case of a DB issue
      */
     protected void populateConnectionPointTables(Connection c, PdmSchema schema) throws SQLException {
         for(PdmTable cpTable : schema.getConnectionPointTables())
             populateConnectionPointTable(c, cpTable, schema);
     }
 
     private void updateFactTableFk(Connection c, PdmSchema schema) throws SQLException {
         String fact = schema.getFactTable().getName();
         String updateStatement = "";
         for(PdmTable tbl : schema.getLookupTables())
             if(updateStatement.length() > 0)
                 updateStatement += " , " + generateFactUpdateSetStatement(tbl, schema);
             else
                 updateStatement += generateFactUpdateSetStatement(tbl, schema);
         for(PdmTable tbl : schema.getReferenceTables())
             if(updateStatement.length() > 0)
                 updateStatement += " , " + generateFactUpdateSetStatement(tbl, schema);
             else
                 updateStatement += generateFactUpdateSetStatement(tbl, schema);
         if(updateStatement.length()>0) {
             updateStatement = "UPDATE " + fact + " SET " + updateStatement +
                 " WHERE "+N.ID+" > (SELECT MAX(lastid) FROM snapshots WHERE name = '" + fact+"')";
             JdbcUtil.executeUpdate(c, updateStatement);
         }
     }
 
 
     /**
      * Generates the UPDATE SET statement for individual lookup FK
      * @param lookupTable lookup table
      * @param schema PDM schema
      * @return the column update clause
      */
     protected String generateFactUpdateSetStatement(PdmTable lookupTable, PdmSchema schema) {
         String lookup = lookupTable.getName();
         String fact = schema.getFactTable().getName();
         String source = schema.getSourceTable().getName();
         String associatedSourceColumns = concatAssociatedSourceColumns(lookupTable);
 
         return lookupTable.getAssociatedSourceColumn() + "_"+N.ID+" = (SELECT "+N.ID+" FROM " +
               lookup + " d," + source + " o WHERE " + associatedSourceColumns + " = d."+N.HSH+" AND o."+N.SRC_ID+"= " +
               fact + "."+N.ID+") ";
     }
 
     /**
      * Populates lookup table
      * @param c JDBC connection
      * @param lookupTable lookup table
      * @param schema PDM schema
      * @throws SQLException in case of a DB issue
      */
     protected void populateLookupTable(Connection c, PdmTable lookupTable, PdmSchema schema) throws SQLException {
         String lookup = lookupTable.getName();
         String fact = schema.getFactTable().getName();
         String source = schema.getSourceTable().getName();
         String insertColumns = N.HSH+"," + getInsertColumns(lookupTable);
         String associatedSourceColumns = getAssociatedSourceColumns(lookupTable);
         String concatAssociatedSourceColumns = concatAssociatedSourceColumns(lookupTable);
         String nestedSelectColumns = concatAssociatedSourceColumns+","+associatedSourceColumns;
         JdbcUtil.executeUpdate(c,
             "INSERT INTO " + lookup + "(" + insertColumns +
             ") SELECT DISTINCT " + nestedSelectColumns + " FROM " + source +
             " WHERE "+N.SRC_ID+" > (SELECT MAX(lastid) FROM snapshots WHERE name='" + fact +
             "') AND " + concatAssociatedSourceColumns + " NOT IN (SELECT "+N.HSH+" FROM " +
             lookupTable.getName() + ")"
         );
     }
 
     /**
      * Populates connection point table
      * @param c JDBC connection
      * @param lookupTable connection point table
      * @param schema PDM schema
      * @throws SQLException in case of a DB issue
      */
     protected void populateConnectionPointTable(Connection c, PdmTable lookupTable, PdmSchema schema) throws SQLException {
         String lookup = lookupTable.getName();
         String fact = schema.getFactTable().getName();
         String source = schema.getSourceTable().getName();
         String insertColumns = N.ID+","+N.HSH+"," + getInsertColumns(lookupTable);
         String associatedSourceColumns = getAssociatedSourceColumns(lookupTable);
         String concatAssociatedSourceColumns = concatAssociatedSourceColumns(lookupTable);
         String nestedSelectColumns = N.SRC_ID+","+concatAssociatedSourceColumns+","+associatedSourceColumns;
         /*
         JdbcUtil.executeUpdate(c,
             "INSERT INTO " + lookup + "(" + insertColumns + ") SELECT DISTINCT " + nestedSelectColumns +
             " FROM " + source + " WHERE "+N.SRC_ID+" > (SELECT MAX(lastid) FROM snapshots WHERE name='" + fact +
             "') AND " + associatedSourceColumns + " NOT IN (SELECT "+N.HSH+" FROM " + lookup + ")"
         );
         */
         // TODO: when snapshotting, there are duplicate CONNECTION POINT VALUES
         // we need to decide if we want to accumultae the connection point lookup or not
         /*
         JdbcUtil.executeUpdate(c,
             "INSERT INTO " + lookup + "(" + insertColumns + ") SELECT DISTINCT " + nestedSelectColumns +
             " FROM " + source + " WHERE "+N.SRC_ID+" > (SELECT MAX(lastid) FROM snapshots WHERE name='" + fact +"')"
         );
         */
         String sql = "INSERT INTO " + lookup + "(" + insertColumns +
 	        ") SELECT DISTINCT " + nestedSelectColumns + " FROM " + source +
 	        " WHERE "+N.SRC_ID+" > (SELECT MAX(lastid) FROM snapshots WHERE name='" + fact +
 	        "') AND " + concatAssociatedSourceColumns + " NOT IN (SELECT "+N.HSH+" FROM " +
 	        lookupTable.getName() + ")";
         JdbcUtil.executeUpdate(c, sql);
     }
 
     /**
      * Concats associated source columns with a DB specific concat method
      * The concatenated columns are used as a unique ke (hash id) of each lookup row
      * @param lookupTable lookup table
      * @return the concatenated columns as String
      */
     protected String concatAssociatedSourceColumns(PdmTable lookupTable) {
         String associatedColumns = "";
         for(PdmColumn column : lookupTable.getAssociatedColumns()) {
             // if there are LABELS, the lookup can't be added twice to the FROM clause
             if(associatedColumns.length() > 0)
                 associatedColumns += SYNTAX_CONCAT_OPERATOR +  column.getSourceColumn();
             else
                 associatedColumns = column.getSourceColumn();
         }
         associatedColumns = SYNTAX_CONCAT_FUNCTION_PREFIX + associatedColumns + SYNTAX_CONCAT_FUNCTION_SUFFIX;
         return associatedColumns;
     }
 
     /**
      * Get all columns that will be inserted (exclude autoincrements)
      * @param lookupTable lookup table
      * @return all columns eglibile for insert
      */
     protected String getInsertColumns(PdmTable lookupTable) {
         String insertColumns = "";
         for(PdmColumn column : lookupTable.getAssociatedColumns()) {
             if(insertColumns.length() > 0)
                 insertColumns += "," + column.getName();
             else
                 insertColumns += column.getName();
         }
         return insertColumns;
     }
 
     /**
      * Returns associted columns in the source table
      * @param lookupTable lookup table
      * @return list of associated source columns
      */
     protected String getAssociatedSourceColumns(PdmTable lookupTable) {
         String sourceColumns = "";
         for(PdmColumn column : lookupTable.getAssociatedColumns()) {
             if(sourceColumns.length() > 0)
                 sourceColumns += "," + column.getSourceColumn();
             else
                 sourceColumns += column.getSourceColumn();
         }
         return sourceColumns;
     }
 
     /**
      * Returns non-autoincrement columns
      * @param tbl table
      * @return non-autoincrement columns
      */
     protected String getNonAutoincrementColumns(PdmTable tbl) {
         String cols = "";
         for (PdmColumn col : tbl.getColumns()) {
             String cn = col.getName();
             if(!col.isAutoIncrement())
                 if (cols != null && cols.length() > 0)
                     cols += "," + cn;
                 else
                     cols += cn;
         }
         return cols;
     }
 
     /**
      * Generates the where clause for unloading data to CSVs in the data loading package
      * @param part DLI part
      * @param schema PDM schema
      * @param snapshotIds ids of snapshots to unload
      * @return SQL where clause
      */
     protected String getLoadWhereClause(DLIPart part, PdmSchema schema, int[] snapshotIds) {
         String dliTable = getTableNameFromPart(part);
         PdmTable pdmTable = schema.getTableByName(dliTable);
         String whereClause = "";
         if(PdmTable.PDM_TABLE_TYPE_FACT.equals(pdmTable.getType()) && snapshotIds != null && snapshotIds.length > 0) {
             String inClause = "";
             for(int i : snapshotIds) {
                 if(inClause.length()>0)
                     inClause += ","+i;
                 else
                     inClause = "" + i;
             }
             whereClause = ",SNAPSHOTS WHERE " + dliTable.toUpperCase() +
                     ".ID BETWEEN SNAPSHOTS.FIRSTID and SNAPSHOTS.LASTID AND SNAPSHOTS.ID IN (" + inClause + ")";
         }
         return whereClause;
     }
 
     /**
      * Generates the list of columns for unloading data to CSVs in the data loading package
      * @param part DLI part
      * @param schema PDM schema
      * @return list of columns
      */
     protected String getLoadColumns(DLIPart part, PdmSchema schema)  {
         String dliTable = getTableNameFromPart(part);
         PdmTable pdmTable = schema.getTableByName(dliTable);
         List<Column> columns = part.getColumns();
         String cols = "";
         for (Column cl : columns) {
             PdmColumn col = pdmTable.getColumnByName(cl.getName());
             // fact table fact columns
             if(PdmTable.PDM_TABLE_TYPE_FACT.equals(pdmTable.getType()) &&
                     SourceColumn.LDM_TYPE_FACT.equals(col.getLdmTypeReference()))
                 cols = decorateFactColumnForLoad(cols, cl, dliTable);
             // lookup table name column
             else if (PdmTable.PDM_TABLE_TYPE_LOOKUP.equals(pdmTable.getType()) &&
                     SourceColumn.LDM_TYPE_ATTRIBUTE.equals(col.getLdmTypeReference()))
                 cols = decorateLookupColumnForLoad(cols, cl, dliTable);
             else
                 cols = decorateOtherColumnForLoad(cols, cl, dliTable);
         }
         return cols;
     }
 
     /**
      * Uses DBMS specific functions for decorating fact columns for unloading from DB to CSV
      * @param cols column list
      * @param cl column to add to cols
      * @param table table name
      * @return the amended list
      */
     protected String decorateFactColumnForLoad(String cols, Column cl, String table) {
         return decorateOtherColumnForLoad(cols, cl, table);
     }
 
     /**
      * Uses DBMS specific functions for decorating lookup columns for unloading from DB to CSV
      * @param cols column list
      * @param cl column to add to cols
      * @param table table name
      * @return the amended list
      */
     protected String decorateLookupColumnForLoad(String cols, Column cl, String table) {
         return decorateOtherColumnForLoad(cols, cl, table);
     }
 
     /**
      * Uses DBMS specific functions for decorating generic columns for unloading from DB to CSV
      * @param cols column list
      * @param cl column to add to cols
      * @param table table name
      * @return the amended list
      */
     protected String decorateOtherColumnForLoad(String cols, Column cl, String table) {
         if (cols != null && cols.length() > 0)
             cols += "," + table.toUpperCase() + "." + StringUtil.formatShortName(cl.getName());
         else
             cols +=  table.toUpperCase() + "." + StringUtil.formatShortName(cl.getName());
         return cols;
     }
 
     /**
      * Get tab,e name from DLI part
      * @param part DLI part
      * @return table name
      */
     protected String getTableNameFromPart(DLIPart part) {
         return StringUtil.formatShortName(part.getFileName().split("\\.")[0]);
     }
 
     /**
      * Creates the DBMS specific system functions
      * @param c JDBC connection
      * @throws SQLException in case of DB issues
      */
     protected abstract void createFunctions(Connection c) throws SQLException;
 
 }
