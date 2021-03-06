 /*
  * (C) Copyright 2008-2009 Nuxeo SA (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Florent Guillaume
  */
 
 package org.nuxeo.ecm.core.storage.sql.db.dialect;
 
 import java.io.Serializable;
 import java.net.SocketException;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.nuxeo.common.utils.StringUtils;
 import org.nuxeo.ecm.core.storage.StorageException;
 import org.nuxeo.ecm.core.storage.sql.Binary;
 import org.nuxeo.ecm.core.storage.sql.Model;
 import org.nuxeo.ecm.core.storage.sql.RepositoryDescriptor;
 import org.nuxeo.ecm.core.storage.sql.db.Column;
 import org.nuxeo.ecm.core.storage.sql.db.ColumnType;
 import org.nuxeo.ecm.core.storage.sql.db.Database;
 import org.nuxeo.ecm.core.storage.sql.db.Table;
import org.nuxeo.ecm.core.storage.sql.db.dialect.Dialect.FulltextMatchInfo;
 
 /**
  * MySQL-specific dialect.
  *
  * @author Florent Guillaume
  */
 public class DialectMySQL extends Dialect {
 
     public DialectMySQL(DatabaseMetaData metadata,
             RepositoryDescriptor repositoryDescriptor) throws StorageException {
         super(metadata, repositoryDescriptor);
     }
 
     @Override
     public char openQuote() {
         return '`';
     }
 
     @Override
     public char closeQuote() {
         return '`';
     }
 
     @Override
     public String getAddForeignKeyConstraintString(String constraintName,
             String[] foreignKeys, String referencedTable, String[] primaryKeys,
             boolean referencesPrimaryKey) {
         String cols = StringUtils.join(foreignKeys, ", ");
         String sql = String.format(
                 " ADD INDEX %s (%s), ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s (%s)",
                 constraintName, cols, constraintName, cols, referencedTable,
                 StringUtils.join(primaryKeys, ", "));
         return sql;
     }
 
     @Override
     public boolean qualifyIndexName() {
         return false;
     }
 
     @Override
     public boolean supportsIfExistsBeforeTableName() {
         return true;
     }
 
     @Override
     public JDBCInfo getJDBCTypeAndString(ColumnType type) {
         switch (type) {
         case VARCHAR:
             // don't use the max 65535 because this max is actually for the
             // total size of all columns of a given table, so allow several
             // varchar columns in the same table
             return jdbcInfo("VARCHAR(500)", Types.VARCHAR);
         case CLOB:
             return jdbcInfo("LONGTEXT", Types.LONGVARCHAR);
         case BOOLEAN:
             return jdbcInfo("BIT", Types.BIT);
         case LONG:
             return jdbcInfo("BIGINT", Types.BIGINT);
         case DOUBLE:
             return jdbcInfo("DOUBLE", Types.DOUBLE);
         case TIMESTAMP:
             return jdbcInfo("DATETIME", Types.TIMESTAMP);
         case BLOBID:
             return jdbcInfo("VARCHAR(40)", Types.VARCHAR);
             // -----
         case NODEID:
         case NODEIDFK:
         case NODEIDFKNP:
         case NODEIDFKMUL:
         case NODEIDFKNULL:
         case NODEVAL:
             return jdbcInfo("VARCHAR(36)", Types.VARCHAR);
         case SYSNAME:
             return jdbcInfo("VARCHAR(256)", Types.VARCHAR);
         case TINYINT:
             return jdbcInfo("TINYINT", Types.TINYINT);
         case INTEGER:
             return jdbcInfo("INTEGER", Types.INTEGER);
         case FTINDEXED:
             throw new AssertionError(type);
         case FTSTORED:
             return jdbcInfo("LONGTEXT", Types.LONGVARCHAR);
         case CLUSTERNODE:
             return jdbcInfo("BIGINT", Types.BIGINT);
         case CLUSTERFRAGS:
             return jdbcInfo("TEXT", Types.VARCHAR);
         }
         throw new AssertionError(type);
     }
 
     @Override
     public boolean isAllowedConversion(int expected, int actual,
             String actualName, int actualSize) {
         // LONGVARCHAR vs VARCHAR compatibility
         if (expected == Types.VARCHAR && actual == Types.LONGVARCHAR) {
             return true;
         }
         if (expected == Types.LONGVARCHAR && actual == Types.VARCHAR) {
             return true;
         }
         // INTEGER vs BIGINT compatibility
         if (expected == Types.BIGINT && actual == Types.INTEGER) {
             return true;
         }
         if (expected == Types.INTEGER && actual == Types.BIGINT) {
             return true;
         }
         return false;
     }
 
     @Override
     public void setToPreparedStatement(PreparedStatement ps, int index,
             Serializable value, Column column) throws SQLException {
         switch (column.getJdbcType()) {
         case Types.VARCHAR:
         case Types.LONGVARCHAR:
             String v;
             if (column.getType() == ColumnType.BLOBID) {
                 v = ((Binary) value).getDigest();
             } else {
                 v = (String) value;
             }
             ps.setString(index, v);
             break;
         case Types.BIT:
             ps.setBoolean(index, ((Boolean) value).booleanValue());
             return;
         case Types.TINYINT:
         case Types.INTEGER:
         case Types.BIGINT:
             ps.setLong(index, ((Long) value).longValue());
             return;
         case Types.DOUBLE:
             ps.setDouble(index, ((Double) value).doubleValue());
             return;
         case Types.TIMESTAMP:
             Calendar cal = (Calendar) value;
             Timestamp ts = new Timestamp(cal.getTimeInMillis());
             ps.setTimestamp(index, ts, cal); // cal passed for timezone
             return;
         default:
             throw new SQLException("Unhandled JDBC type: "
                     + column.getJdbcType());
         }
     }
 
     @Override
     @SuppressWarnings("boxing")
     public Serializable getFromResultSet(ResultSet rs, int index, Column column)
             throws SQLException {
         switch (column.getJdbcType()) {
         case Types.VARCHAR:
         case Types.LONGVARCHAR:
             String string = rs.getString(index);
             if (column.getType() == ColumnType.BLOBID && string != null) {
                 return column.getModel().getBinary(string);
             } else {
                 return string;
             }
         case Types.BIT:
             return rs.getBoolean(index);
         case Types.TINYINT:
         case Types.INTEGER:
         case Types.BIGINT:
             return rs.getLong(index);
         case Types.DOUBLE:
             return rs.getDouble(index);
         case Types.TIMESTAMP:
             Timestamp ts = rs.getTimestamp(index);
             if (ts == null) {
                 return null;
             } else {
                 Serializable cal = new GregorianCalendar(); // XXX timezone
                 ((Calendar) cal).setTimeInMillis(ts.getTime());
                 return cal;
             }
         }
         throw new SQLException("Unhandled JDBC type: " + column.getJdbcType());
     }
 
     @Override
     public String getCreateFulltextIndexSql(String indexName,
             String quotedIndexName, Table table, List<Column> columns,
             Model model) {
         List<String> columnNames = new ArrayList<String>(columns.size());
         for (Column col : columns) {
             columnNames.add(col.getQuotedName());
         }
         return String.format("CREATE FULLTEXT INDEX %s ON %s (%s)",
                 quotedIndexName, table.getQuotedName(), StringUtils.join(
                         columnNames, ", "));
     }
 
     @Override
     public String getDialectFulltextQuery(String query) {
         query = query.replaceAll(" +", " ");
         List<String> pos = new LinkedList<String>();
         List<String> neg = new LinkedList<String>();
         for (String word : StringUtils.split(query, ' ', false)) {
             if (word.startsWith("-")) {
                 neg.add(word);
             } else if (word.startsWith("+")) {
                 pos.add(word);
             } else {
                 pos.add("+" + word);
             }
         }
         if (pos.isEmpty()) {
             return "+DONTMATCHANYTHINGFOREMPTYQUERY";
         }
         String res = StringUtils.join(pos, " ");
         if (!neg.isEmpty()) {
             res += " " + StringUtils.join(neg, " ");
         }
         return res;
     }
 
     // SELECT ..., (MATCH(`fulltext`.`simpletext`, `fulltext`.`binarytext`)
     // .................. AGAINST (?) / 10) AS nxscore
     // FROM ... LEFT JOIN `fulltext` ON ``fulltext`.`id` = `hierarchy`.`id`
     // WHERE ... AND MATCH(`fulltext`.`simpletext`, `fulltext`.`binarytext`)
     // ................... AGAINST (? IN BOOLEAN MODE)
     // ORDER BY nxscore DESC
     @Override
     public FulltextMatchInfo getFulltextScoredMatchInfo(String fulltextQuery,
             String indexName, int nthMatch, Column mainColumn, Model model,
             Database database) {
         String nthSuffix = nthMatch == 1 ? "" : String.valueOf(nthMatch);
         String scoreAlias = "_nxscore" + nthSuffix;
         String indexSuffix = model.getFulltextIndexSuffix(indexName);
         Table ft = database.getTable(model.FULLTEXT_TABLE_NAME);
         Column ftMain = ft.getColumn(model.MAIN_KEY);
         Column stColumn = ft.getColumn(model.FULLTEXT_SIMPLETEXT_KEY
                 + indexSuffix);
         Column btColumn = ft.getColumn(model.FULLTEXT_BINARYTEXT_KEY
                 + indexSuffix);
         String match = String.format("MATCH (%s, %s)",
                 stColumn.getFullQuotedName(), btColumn.getFullQuotedName());
         FulltextMatchInfo info = new FulltextMatchInfo();
         info.leftJoin = String.format(
                 "%s ON %s = %s", //
                 ft.getQuotedName(), ftMain.getFullQuotedName(),
                 mainColumn.getFullQuotedName());
         info.whereExpr = String.format("%s AGAINST (? IN BOOLEAN MODE)", match);
         info.whereExprParam = fulltextQuery;
         // Note: using the boolean query in non-boolean mode gives approximate
         // results but it's the best we have as MySQL does not provide a score
         // in boolean mode.
         // Note: dividing by 10 is arbitrary, but MySQL cannot really
         // normalize scores.
         info.scoreExpr = String.format("(%s AGAINST (?) / 10) AS %s", match,
                 scoreAlias);
         info.scoreExprParam = fulltextQuery;
         info.scoreAlias = scoreAlias;
         info.scoreCol = new Column(mainColumn.getTable(), null,
                 ColumnType.DOUBLE, null, model);
         return info;
     }
 
     @Override
     public boolean getMaterializeFulltextSyntheticColumn() {
         return false;
     }
 
     @Override
     public int getFulltextIndexedColumns() {
         return 2;
     }
 
     @Override
     public String getTableTypeString(Table table) {
         if (table.hasFulltextIndex()) {
             return " ENGINE=MyISAM";
         } else {
             return " ENGINE=InnoDB";
         }
     }
 
     @Override
     public boolean supportsUpdateFrom() {
         return true;
     }
 
     @Override
     public boolean doesUpdateFromRepeatSelf() {
         return true;
     }
 
     @Override
     public boolean needsOrderByKeysAfterDistinct() {
         return false;
     }
 
     @Override
     public boolean needsAliasForDerivedTable() {
         return true;
     }
 
     @Override
     public String getSecurityCheckSql(String idColumnName) {
         return String.format("NX_ACCESS_ALLOWED(%s, ?, ?)", idColumnName);
     }
 
     @Override
     public String getInTreeSql(String idColumnName) {
         return String.format("NX_IN_TREE(%s, ?)", idColumnName);
     }
 
     @Override
     public Collection<ConditionalStatement> getConditionalStatements(
             Model model, Database database) {
         String idType;
         switch (model.idGenPolicy) {
         case APP_UUID:
             idType = "varchar(36)";
             break;
         case DB_IDENTITY:
             idType = "integer";
             break;
         default:
             throw new AssertionError(model.idGenPolicy);
         }
 
         List<ConditionalStatement> statements = new LinkedList<ConditionalStatement>();
 
         statements.add(new ConditionalStatement(
                 false, // late
                 Boolean.TRUE, // always drop
                 null, //
                 "DROP PROCEDURE IF EXISTS NX_CLUSTER_INVAL", //
                 String.format(
                         "CREATE PROCEDURE NX_CLUSTER_INVAL(i %s, f TEXT, k TINYINT) " //
                                 + "LANGUAGE SQL " //
                                 + "MODIFIES SQL DATA " //
                                 + "BEGIN" //
                                 + "  DECLARE n BIGINT;" //
                                 + "  DECLARE done BOOLEAN DEFAULT FALSE;" //
                                 + "  DECLARE cur CURSOR FOR " //
                                 + "    SELECT nodeid FROM cluster_nodes WHERE nodeid <> @@PSEUDO_THREAD_ID;" //
                                 + "  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;" //
                                 + "  OPEN cur;" //
                                 + "  REPEAT " //
                                 + "    FETCH cur INTO n;" //
                                 + "    IF NOT done THEN" //
                                 + "      INSERT INTO cluster_invals (nodeid, id, fragments, kind) VALUES (n, i, f, k);" //
                                 + "    END IF;" //
                                 + "  UNTIL done END REPEAT;" //
                                 + "  CLOSE cur; " //
                                 + "END" //
                         , idType)));
 
         statements.add(new ConditionalStatement(
                 true, // early
                 Boolean.TRUE, // always drop
                 null, //
                 "DROP FUNCTION IF EXISTS NX_IN_TREE", //
                 String.format(
                         "CREATE FUNCTION NX_IN_TREE(id %s, baseid %<s) " //
                                 + "RETURNS BOOLEAN " //
                                 + "LANGUAGE SQL " //
                                 + "READS SQL DATA " //
                                 + "BEGIN" //
                                 + "  DECLARE curid %<s DEFAULT id;" //
                                 + "  IF baseid IS NULL OR id IS NULL OR baseid = id THEN" //
                                 + "    RETURN FALSE;" //
                                 + "  END IF;" //
                                 + "  LOOP" //
                                 + "    SELECT parentid INTO curid FROM hierarchy WHERE hierarchy.id = curid;" //
                                 + "    IF curid IS NULL THEN" //
                                 + "      RETURN FALSE; " //
                                 + "    ELSEIF curid = baseid THEN" //
                                 + "      RETURN TRUE;" //
                                 + "    END IF;" //
                                 + "  END LOOP;" //
                                 + "END" //
                         , idType)));
 
         statements.add(new ConditionalStatement(
                 true, // early
                 Boolean.TRUE, // always drop
                 null, //
                 "DROP FUNCTION IF EXISTS NX_ACCESS_ALLOWED", //
                 String.format(
                         "CREATE FUNCTION NX_ACCESS_ALLOWED" //
                                 + "(id %s, users VARCHAR(10000), perms VARCHAR(10000)) " //
                                 + "RETURNS BOOLEAN " //
                                 + "LANGUAGE SQL " //
                                 + "READS SQL DATA " //
                                 + "BEGIN" //
                                 + "  DECLARE allusers VARCHAR(10000) DEFAULT CONCAT('|',users,'|');" //
                                 + "  DECLARE allperms VARCHAR(10000) DEFAULT CONCAT('|',perms,'|');" //
                                 + "  DECLARE first BOOLEAN DEFAULT TRUE;" //
                                 + "  DECLARE curid %<s DEFAULT id;" //
                                 + "  DECLARE newid %<s;" //
                                 + "  DECLARE gr BIT;" //
                                 + "  DECLARE pe VARCHAR(1000);" //
                                 + "  DECLARE us VARCHAR(1000);" //
                                 + "  WHILE curid IS NOT NULL DO" //
                                 + "    BEGIN" //
                                 + "      DECLARE done BOOLEAN DEFAULT FALSE;" //
                                 + "      DECLARE cur CURSOR FOR" //
                                 + "        SELECT `grant`, `permission`, `user` FROM `acls`" //
                                 + "        WHERE `acls`.`id` = curid ORDER BY `pos`;" //
                                 + "      DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;" //
                                 + "      OPEN cur;" //
                                 + "      REPEAT " //
                                 + "        FETCH cur INTO gr, pe, us;" //
                                 + "        IF NOT done THEN" //
                                 + "          IF LOCATE(CONCAT('|',us,'|'), allusers) <> 0 AND LOCATE(CONCAT('|',pe,'|'), allperms) <> 0 THEN" //
                                 + "            CLOSE cur;" //
                                 + "            RETURN gr;" //
                                 + "          END IF;" //
                                 + "        END IF;" //
                                 + "      UNTIL done END REPEAT;" //
                                 + "      CLOSE cur;" //
                                 + "    END;" //
                                 + "    SELECT parentid INTO newid FROM hierarchy WHERE hierarchy.id = curid;" //
                                 + "    IF first AND newid IS NULL THEN" //
                                 + "      SELECT versionableid INTO newid FROM versions WHERE versions.id = curid;" //
                                 + "    END IF;" //
                                 + "    SET first = FALSE;" //
                                 + "    SET curid = newid;" //
                                 + "  END WHILE;" //
                                 + "  RETURN FALSE; " //
                                 + "END" //
                         , idType)));
         return statements;
     }
 
     protected static class DebugStatements {
         public ConditionalStatement makeDebugTable() {
             return new ConditionalStatement(
                     true, // early
                     Boolean.TRUE, // always drop
                     null, //
                     "DROP TABLE IF EXISTS NX_DEBUG_TABLE", //
                     "CREATE TABLE NX_DEBUG_TABLE (id INTEGER AUTO_INCREMENT PRIMARY KEY, log VARCHAR(10000))");
         }
 
         public ConditionalStatement makeNxDebug() {
             return new ConditionalStatement(
                     true, // early
                     Boolean.TRUE, // always drop
                     null, //
                     "DROP PROCEDURE IF EXISTS NX_DEBUG", //
                     String.format("CREATE PROCEDURE NX_DEBUG(line VARCHAR(10000)) " //
                             + "LANGUAGE SQL " //
                             + "BEGIN " //
                             + "  INSERT INTO NX_DEBUG_TABLE (log) values (line);" //
                             + "END" //
                     ));
         }
     }
 
     @Override
     public Collection<ConditionalStatement> getTestConditionalStatements(
             Model model, Database database) {
         List<ConditionalStatement> statements = new LinkedList<ConditionalStatement>();
         statements.add(new ConditionalStatement(
                 true,
                 Boolean.FALSE,
                 null,
                 null,
                 // here use a NCLOB instead of a NVARCHAR2 to test compatibility
                 "CREATE TABLE `testschema2` (`id` VARCHAR(36) NOT NULL, `title` LONGTEXT) ENGINE=InnoDB"));
         statements.add(new ConditionalStatement(true, Boolean.FALSE, null,
                 null,
                 "ALTER TABLE `testschema2` ADD CONSTRAINT `testschema2_pk` PRIMARY KEY (`id`)"));
         return statements;
     }
 
     @Override
     public boolean connectionClosedByException(Throwable t) {
         while (t.getCause() != null) {
             t = t.getCause();
         }
         if (t instanceof SocketException) {
             return true;
         }
         // XAResource.start:
         // com.mysql.jdbc.jdbc2.optional.MysqlXAException
         // No operations allowed after connection closed. Connection was
         // implicitly closed due to underlying exception/error:
         // com.mysql.jdbc.exceptions.jdbc4.CommunicationsException:
         // Communications link failure
         String message = t.toString() + " " + t.getMessage();
         if (message.contains("Communications link failure")
                 || message.contains("CommunicationsException")) {
             return true;
         }
         return false;
     }
 
     @Override
     public boolean isClusteringSupported() {
         return true;
     }
 
     @Override
     public boolean isClusteringDeleteNeeded() {
         return true;
     }
 
     @Override
     public String getCleanupClusterNodesSql(Model model, Database database) {
         Table cln = database.getTable(model.CLUSTER_NODES_TABLE_NAME);
         Column clnid = cln.getColumn(model.CLUSTER_NODES_NODEID_KEY);
         // delete nodes for sessions that don't exist anymore
         return String.format(
                 "DELETE N FROM %s N WHERE NOT EXISTS("
                         + "SELECT 1 FROM INFORMATION_SCHEMA.PROCESSLIST P WHERE N.%s = P.id)",
                 cln.getQuotedName(), clnid.getQuotedName());
     }
 
     @Override
     public String getCreateClusterNodeSql(Model model, Database database) {
         Table cln = database.getTable(model.CLUSTER_NODES_TABLE_NAME);
         Column clnid = cln.getColumn(model.CLUSTER_NODES_NODEID_KEY);
         Column clncr = cln.getColumn(model.CLUSTER_NODES_CREATED_KEY);
         return String.format(
                 "INSERT INTO %s (%s, %s) VALUES (@@PSEUDO_THREAD_ID, CURRENT_TIMESTAMP)",
                 cln.getQuotedName(), clnid.getQuotedName(),
                 clncr.getQuotedName());
     }
 
     @Override
     public String getRemoveClusterNodeSql(Model model, Database database) {
         Table cln = database.getTable(model.CLUSTER_NODES_TABLE_NAME);
         Column clnid = cln.getColumn(model.CLUSTER_NODES_NODEID_KEY);
         return String.format("DELETE FROM %s WHERE %s = @@PSEUDO_THREAD_ID",
                 cln.getQuotedName(), clnid.getQuotedName());
     }
 
     @Override
     public String getClusterInsertInvalidations() {
         return "CALL NX_CLUSTER_INVAL(?, ?, ?)";
     }
 
     @Override
     public String getClusterGetInvalidations() {
         return "SELECT id, fragments, kind FROM cluster_invals WHERE nodeid = @@PSEUDO_THREAD_ID";
     }
 
     @Override
     public String getClusterDeleteInvalidations() {
         return "DELETE FROM cluster_invals WHERE nodeid = @@PSEUDO_THREAD_ID";
     }
 
 }
