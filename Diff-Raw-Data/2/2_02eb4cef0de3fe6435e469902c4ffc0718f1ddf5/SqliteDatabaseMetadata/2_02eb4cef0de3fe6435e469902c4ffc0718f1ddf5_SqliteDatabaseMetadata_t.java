 /*
  * Copyright (c) 2013, Timothy Stack
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *  Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer.
  *  Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ''AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.sqlitejdbcng;
 
 import org.sqlitejdbcng.bridj.Sqlite3;
 import org.sqlitejdbcng.internal.ColumnData;
 import org.sqlitejdbcng.internal.SQLKeywords;
 
 import java.sql.*;
 import java.util.*;
 
 public class SqliteDatabaseMetadata implements DatabaseMetaData {
     private static final String KEYWORD_LIST;
 
     static {
         SQLKeywords keywords = new SQLKeywords();
         List<String> sqliteList = new ArrayList<>(Arrays.asList(keywords.getSqliteKeywords()));
 
         sqliteList.removeAll(Arrays.asList(keywords.getSqlKeywords()));
 
         KEYWORD_LIST = Sqlite3.join(sqliteList.toArray(), ",");
     }
 
     private final SqliteConnection conn;
 
     public SqliteDatabaseMetadata(SqliteConnection conn) {
         this.conn = conn;
     }
 
     @Override
     public boolean allProceduresAreCallable() throws SQLException {
         return false;
     }
 
     @Override
     public boolean allTablesAreSelectable() throws SQLException {
         return true;
     }
 
     @Override
     public String getURL() throws SQLException {
         return this.conn.getURL();
     }
 
     @Override
     public String getUserName() throws SQLException {
         return "";
     }
 
     @Override
     public boolean isReadOnly() throws SQLException {
         return this.conn.isReadOnly();
     }
 
     @Override
     public boolean nullsAreSortedHigh() throws SQLException {
         return false;
     }
 
     @Override
     public boolean nullsAreSortedLow() throws SQLException {
         return true;
     }
 
     @Override
     public boolean nullsAreSortedAtStart() throws SQLException {
         return false;
     }
 
     @Override
     public boolean nullsAreSortedAtEnd() throws SQLException {
         return false;
     }
 
     @Override
     public String getDatabaseProductName() throws SQLException {
         return "SQLite";
     }
 
     @Override
     public String getDatabaseProductVersion() throws SQLException {
         return Sqlite3.sqlite3_libversion().getCString();
     }
 
     @Override
     public String getDriverName() throws SQLException {
         return SqliteDriver.class.getPackage().getName();
     }
 
     @Override
     public String getDriverVersion() throws SQLException {
         return "" + SqliteDriver.VERSION[0] + "." + SqliteDriver.VERSION[1];
     }
 
     @Override
     public int getDriverMajorVersion() {
         return SqliteDriver.VERSION[0];
     }
 
     @Override
     public int getDriverMinorVersion() {
         return SqliteDriver.VERSION[1];
     }
 
     @Override
     public boolean usesLocalFiles() throws SQLException {
         return true;
     }
 
     @Override
     public boolean usesLocalFilePerTable() throws SQLException {
         return false;
     }
 
     @Override
     public boolean supportsMixedCaseIdentifiers() throws SQLException {
         return false;
     }
 
     @Override
     public boolean storesUpperCaseIdentifiers() throws SQLException {
         return false;
     }
 
     @Override
     public boolean storesLowerCaseIdentifiers() throws SQLException {
         return false;
     }
 
     @Override
     public boolean storesMixedCaseIdentifiers() throws SQLException {
         return true;
     }
 
     @Override
     public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getIdentifierQuoteString() throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getSQLKeywords() throws SQLException {
         return KEYWORD_LIST;
     }
 
     @Override
     public String getNumericFunctions() throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getStringFunctions() throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getSystemFunctions() throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getTimeDateFunctions() throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getSearchStringEscape() throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getExtraNameCharacters() throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsAlterTableWithAddColumn() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsAlterTableWithDropColumn() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsColumnAliasing() throws SQLException {
         return true;
     }
 
     @Override
     public boolean nullPlusNonNullIsNull() throws SQLException {
         return true;
     }
 
     @Override
     public boolean supportsConvert() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsConvert(int i, int i2) throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsTableCorrelationNames() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsDifferentTableCorrelationNames() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsExpressionsInOrderBy() throws SQLException {
         return true;
     }
 
     @Override
     public boolean supportsOrderByUnrelated() throws SQLException {
         return true;
     }
 
     @Override
     public boolean supportsGroupBy() throws SQLException {
         return true;
     }
 
     @Override
     public boolean supportsGroupByUnrelated() throws SQLException {
         return true;
     }
 
     @Override
     public boolean supportsGroupByBeyondSelect() throws SQLException {
         return true;
     }
 
     @Override
     public boolean supportsLikeEscapeClause() throws SQLException {
         return true;
     }
 
     @Override
     public boolean supportsMultipleResultSets() throws SQLException {
         return false;
     }
 
     @Override
     public boolean supportsMultipleTransactions() throws SQLException {
         return true;
     }
 
     @Override
     public boolean supportsNonNullableColumns() throws SQLException {
         return true;
     }
 
     @Override
     public boolean supportsMinimumSQLGrammar() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsCoreSQLGrammar() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsExtendedSQLGrammar() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsANSI92EntryLevelSQL() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsANSI92IntermediateSQL() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsANSI92FullSQL() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsIntegrityEnhancementFacility() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsOuterJoins() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsFullOuterJoins() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsLimitedOuterJoins() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getSchemaTerm() throws SQLException {
         return "";
     }
 
     @Override
     public String getProcedureTerm() throws SQLException {
         return "";
     }
 
     @Override
     public String getCatalogTerm() throws SQLException {
         return "database";
     }
 
     @Override
     public boolean isCatalogAtStart() throws SQLException {
         return true;
     }
 
     @Override
     public String getCatalogSeparator() throws SQLException {
         return ".";
     }
 
     @Override
     public boolean supportsSchemasInDataManipulation() throws SQLException {
         return false;
     }
 
     @Override
     public boolean supportsSchemasInProcedureCalls() throws SQLException {
         return false;
     }
 
     @Override
     public boolean supportsSchemasInTableDefinitions() throws SQLException {
         return false;
     }
 
     @Override
     public boolean supportsSchemasInIndexDefinitions() throws SQLException {
         return false;
     }
 
     @Override
     public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
         return false;
     }
 
     @Override
     public boolean supportsCatalogsInDataManipulation() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsCatalogsInProcedureCalls() throws SQLException {
         return false;
     }
 
     @Override
     public boolean supportsCatalogsInTableDefinitions() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsPositionedDelete() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsPositionedUpdate() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsSelectForUpdate() throws SQLException {
         return false;
     }
 
     @Override
     public boolean supportsStoredProcedures() throws SQLException {
         return false;
     }
 
     @Override
     public boolean supportsSubqueriesInComparisons() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsSubqueriesInExists() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsSubqueriesInIns() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsSubqueriesInQuantifieds() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsCorrelatedSubqueries() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsUnion() throws SQLException {
         return true;
     }
 
     @Override
     public boolean supportsUnionAll() throws SQLException {
         return true;
     }
 
     @Override
     public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public int getMaxBinaryLiteralLength() throws SQLException {
         return Sqlite3.sqlite3_limit(this.conn.getHandle(), Sqlite3.Limit.SQLITE_LIMIT_LENGTH.value(), -1);
     }
 
     @Override
     public int getMaxCharLiteralLength() throws SQLException {
         return Sqlite3.sqlite3_limit(this.conn.getHandle(), Sqlite3.Limit.SQLITE_LIMIT_LENGTH.value(), -1);
     }
 
     @Override
     public int getMaxColumnNameLength() throws SQLException {
         return 0;
     }
 
     @Override
     public int getMaxColumnsInGroupBy() throws SQLException {
         return Sqlite3.sqlite3_limit(this.conn.getHandle(), Sqlite3.Limit.SQLITE_LIMIT_COLUMN.value(), -1);
     }
 
     @Override
     public int getMaxColumnsInIndex() throws SQLException {
         return Sqlite3.sqlite3_limit(this.conn.getHandle(), Sqlite3.Limit.SQLITE_LIMIT_COLUMN.value(), -1);
     }
 
     @Override
     public int getMaxColumnsInOrderBy() throws SQLException {
         return Sqlite3.sqlite3_limit(this.conn.getHandle(), Sqlite3.Limit.SQLITE_LIMIT_COLUMN.value(), -1);
     }
 
     @Override
     public int getMaxColumnsInSelect() throws SQLException {
         return Sqlite3.sqlite3_limit(this.conn.getHandle(), Sqlite3.Limit.SQLITE_LIMIT_COLUMN.value(), -1);
     }
 
     @Override
     public int getMaxColumnsInTable() throws SQLException {
         return Sqlite3.sqlite3_limit(this.conn.getHandle(), Sqlite3.Limit.SQLITE_LIMIT_COLUMN.value(), -1);
     }
 
     @Override
     public int getMaxConnections() throws SQLException {
         return 0;
     }
 
     @Override
     public int getMaxCursorNameLength() throws SQLException {
         return 0;
     }
 
     @Override
     public int getMaxIndexLength() throws SQLException {
         return 0;
     }
 
     @Override
     public int getMaxSchemaNameLength() throws SQLException {
         return 0;
     }
 
     @Override
     public int getMaxProcedureNameLength() throws SQLException {
         return 0;
     }
 
     @Override
     public int getMaxCatalogNameLength() throws SQLException {
         return 0;
     }
 
     @Override
     public int getMaxRowSize() throws SQLException {
         return Sqlite3.sqlite3_limit(this.conn.getHandle(), Sqlite3.Limit.SQLITE_LIMIT_LENGTH.value(), -1);
     }
 
     @Override
     public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
         return true;
     }
 
     @Override
     public int getMaxStatementLength() throws SQLException {
         return Sqlite3.sqlite3_limit(this.conn.getHandle(), Sqlite3.Limit.SQLITE_LIMIT_SQL_LENGTH.value(), -1);
     }
 
     @Override
     public int getMaxStatements() throws SQLException {
         return 0;
     }
 
     @Override
     public int getMaxTableNameLength() throws SQLException {
         return 0;
     }
 
     @Override
     public int getMaxTablesInSelect() throws SQLException {
         return 64;
     }
 
     @Override
     public int getMaxUserNameLength() throws SQLException {
         return 0;
     }
 
     @Override
     public int getDefaultTransactionIsolation() throws SQLException {
         return 0;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsTransactions() throws SQLException {
         return true;
     }
 
     @Override
     public boolean supportsTransactionIsolationLevel(int i) throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     private ResultSet executeConstantQuery(String constantQuery) throws SQLException {
         Statement stmt = this.conn.createStatement();
 
         try {
             stmt.closeOnCompletion();
             return stmt.executeQuery(constantQuery);
         }
         catch (SQLException e) {
             stmt.close();
 
             throw e;
         }
     }
 
     @Override
     public ResultSet getProcedures(String s, String s2, String s3) throws SQLException {
         return this.executeConstantQuery(
                 "SELECT null as PROCEDURE_CAT, null as PROCEDURE_SCHEM, null as PROCEDURE_NAME, " +
                         "null as RES1, null as RES2, null as RES3, null as REMARKS, " +
                         "null as PROCEDURE_TYPE, null as SPECIFIC_NAME LIMIT 0");
     }
 
     @Override
     public ResultSet getProcedureColumns(String s, String s2, String s3, String s4) throws SQLException {
         return this.executeConstantQuery(
                 "SELECT null as PROCEDURE_CAT, null as PROCEDURE_SCHEM, null as PROCEDURE_NAME, " +
                         "null as COLUMN_NAME, null as COLUMN_TYPE, null as DATA_TYPE, " +
                         "null as TYPE_NAME, null as PRECISION, null as LENGTH, null as SCALE, " +
                         "null as RADIX, null as NULLABLE, null as REMARKS, null as COLUMN_DEF, " +
                         "null as SQL_DATA_TYPE, null as SQL_DATETIME_SUB, null as CHAR_OCTET_LENGTH, " +
                         "null as ORDINAL_POSITION, null as IS_NULLABLE, null as SPECIFIC_NAME " +
                         "LIMIT 0");
     }
 
     private static final String[] DEFAULT_TABLE_TYPES = { "TABLE", "VIEW" };
 
     @Override
     public ResultSet getTables(String catalog,
                                String schemaPattern,
                                String tableNamePattern,
                                String[] types) throws SQLException {
         if (schemaPattern != null && !schemaPattern.isEmpty())
             throw new SQLFeatureNotSupportedException("SQLite does not support schemas");
 
         if (catalog == null || catalog.isEmpty())
             catalog = "main";
 
         if (types == null) {
             types = DEFAULT_TABLE_TYPES;
         }
 
         String sql = Sqlite3.mprintf(
                 "SELECT ? as TABLE_CAT, null as TABLE_SCHEM, name as TABLE_NAME, " +
                         "upper(type) as TABLE_TYPE, sql as REMARKS, null as TYPE_CAT, " +
                         "null as TYPE_SCHEM, null as TYPE_NAME, " +
                         "\"row_id\" as SELF_REFERENCING_COL_NAME, " +
                         "\"SYSTEM\" as REF_GENERATION FROM %Q.sqlite_master " +
                         "WHERE name LIKE ? and upper(type) in (%s) " +
                         "ORDER BY TABLE_TYPE, TABLE_CAT, TABLE_SCHEM, TABLE_NAME",
                 catalog,
                 Sqlite3.join(Collections.nCopies(types.length, "?").toArray(), ", "));
 
         PreparedStatement ps = this.conn.prepareStatement(sql);
 
         ps.closeOnCompletion();
         try {
             ps.setString(1, catalog);
 
             if (tableNamePattern == null)
                 tableNamePattern = "%";
             ps.setString(2, tableNamePattern);
             for (int lpc = 0; lpc < types.length; lpc++) {
                 ps.setString(3 + lpc, types[lpc]);
             }
 
             return ps.executeQuery();
         }
         catch (SQLException e) {
             ps.close();
 
             throw e;
         }
     }
 
     @Override
     public ResultSet getSchemas() throws SQLException {
         return this.executeConstantQuery(
                 "SELECT null as TABLE_SCHEM, null as TABLE_CATALOG LIMIT 0");
     }
 
     @Override
     public ResultSet getCatalogs() throws SQLException {
         try (Statement stmt = this.conn.createStatement()) {
             List<String> dbNames = new ArrayList<>();
 
             try (ResultSet rs = stmt.executeQuery("PRAGMA database_list")) {
                 while (rs.next()) {
                     dbNames.add(rs.getString(2));
                 }
             }
 
             String query = Sqlite3.join(
                     Collections.nCopies(dbNames.size(), "SELECT ? as TABLE_CAT").toArray(),
                     " UNION ALL ");
 
             PreparedStatement preparedStatement = this.conn.prepareStatement(query);
 
             preparedStatement.closeOnCompletion();
             try {
                 for (int lpc = 0; lpc < dbNames.size(); lpc++) {
                     preparedStatement.setString(lpc + 1, dbNames.get(lpc));
                 }
 
                 return preparedStatement.executeQuery();
             }
             catch (SQLException e) {
                 preparedStatement.close();
 
                 throw e;
             }
         }
     }
 
     @Override
     public ResultSet getTableTypes() throws SQLException {
         return this.executeConstantQuery(
                 "SELECT 'TABLE' as TABLE_TYPE UNION ALL " +
                         "SELECT 'VIEW' as TABLE_TYPE");
     }
 
     private static final String COLUMN_QUERY =
             "SELECT ? AS TABLE_CAT, null AS TABLE_SCHEM, ? AS TABLE_NAME, " +
                     "? AS COLUMN_NAME, ? AS DATA_TYPE, ? AS TYPE_NAME, ? AS COLUMN_SIZE, " +
                     "null AS BUFFER_LENGTH, ? AS DECIMAL_DIGITS, 10 AS NUM_PREC_RADIX, " +
                     "? AS NULLABLE, '' AS REMARKS, ? AS COLUMN_DEF, null AS SQL_DATA_TYPE, " +
                     "null AS SQL_DATETIME_SUB, ? AS ORDINAL_POSITION, ? AS IS_NULLABLE, " +
                     "null AS SCOPE_CATALOG, null AS SCOPE_SCHEMA, null AS SCOPE_TABLE, " +
                     "null AS SOURCE_DATA_TYPE, ? AS IS_AUTOINCREMENT, ? AS IS_GENERATEDCOLUMN ";
 
     @Override
     public ResultSet getColumns(String catalog,
                                 String schemaPattern,
                                 String tableNamePattern,
                                 String columnNamePattern) throws SQLException {
         List<String> tableList = new ArrayList<>();
         String query;
 
         /* XXX We should iterate over the catalogs instead of just defaulting to "main" */
        if (catalog == null || catalog.isEmpty())
             catalog = "main";
 
         if (tableNamePattern == null)
             tableNamePattern = "%";
         if (columnNamePattern == null)
             columnNamePattern = "%";
 
         query = Sqlite3.mprintf("SELECT tbl_name FROM %Q.sqlite_master WHERE type='table' AND tbl_name LIKE ?",
                 catalog);
         try (PreparedStatement ps = this.conn.prepareStatement(query)) {
             ps.setString(1, tableNamePattern);
             try (ResultSet rs = ps.executeQuery()) {
                 while (rs.next()) {
                     tableList.add(rs.getString(1));
                 }
             }
         }
 
         List<ColumnData> columnList = new ArrayList<>();
 
         columnNamePattern = columnNamePattern.replaceAll("%", ".*");
         try (Statement stmt = this.conn.createStatement()) {
             for (String tableName : tableList) {
 
                 query = Sqlite3.mprintf("PRAGMA %Q.table_info(%Q)", catalog, tableName);
 
                 try (ResultSet rs = stmt.executeQuery(query)) {
                     while (rs.next()) {
                         ColumnData cd = new ColumnData(this.conn.getHandle(), catalog, tableName, rs);
 
                         if (!cd.name.matches(columnNamePattern))
                             continue;
                         columnList.add(cd);
                     }
                 }
             }
         }
 
         String constantQuery = "", limit = "";
 
         for (int lpc = 0; lpc < columnList.size(); lpc++) {
             if (!constantQuery.isEmpty())
                 constantQuery += " UNION ALL ";
             constantQuery += COLUMN_QUERY;
         }
         if (constantQuery.isEmpty()) {
             constantQuery = COLUMN_QUERY;
             limit = " LIMIT 0";
         }
         constantQuery += " ORDER BY TABLE_CAT, TABLE_SCHEM, TABLE_NAME, ORDINAL_POSITION";
         constantQuery += limit;
 
         PreparedStatement ps = this.conn.prepareStatement(constantQuery);
 
         ps.closeOnCompletion();
 
         int index = 1;
 
         for (ColumnData column : columnList) {
             ps.setString(index++, catalog);
             ps.setString(index++, column.tableName);
             ps.setString(index++, column.name);
             ps.setInt(index++, column.sqlType);
             ps.setString(index++, column.type);
             ps.setInt(index++, 0);
             ps.setInt(index++, 0);
             ps.setInt(index++, column.notNull);
             ps.setString(index++, column.defaultValue);
             ps.setInt(index++, column.index);
             ps.setString(index++, column.notNull == columnNoNulls ? "NO" : "YES");
             ps.setInt(index++, 0);
             ps.setInt(index++, 0);
         }
 
         return ps.executeQuery();
     }
 
     @Override
     public ResultSet getColumnPrivileges(String s, String s2, String s3, String s4) throws SQLException {
         return this.executeConstantQuery(
                 "SELECT NULL AS TABLE_CAT, NULL AS TABLE_SCHEM, NULL AS TABLE_NAME, " +
                         "NULL AS COLUMN_NAME, NULL AS GRANTOR, NULL AS GRANTEE, " +
                         "NULL AS PRIVILEGE, NULL AS IS_GRANTABLE LIMIT 0"
         );
     }
 
     @Override
     public ResultSet getTablePrivileges(String s, String s2, String s3) throws SQLException {
         return this.executeConstantQuery(
                 "SELECT NULL AS TABLE_CAT, NULL AS TABLE_SCHEM, NULL AS TABLE_NAME, " +
                         "NULL AS GRANTOR, NULL AS GRANTEE, " +
                         "NULL AS PRIVILEGE, NULL AS IS_GRANTABLE LIMIT 0"
         );
     }
 
     @Override
     public ResultSet getBestRowIdentifier(String s, String s2, String s3, int i, boolean b) throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public ResultSet getVersionColumns(String s, String s2, String s3) throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     private static final String PRIMARY_KEY_QUERY =
             "SELECT ? AS TABLE_CAT, null AS TABLE_SCHEM, ? AS TABLE_NAME, ? AS COLUMN_NAME, " +
                     "? AS KEY_SEQ, null AS PK_NAME ";
 
     @Override
     public ResultSet getPrimaryKeys(String catalog, String schema, String tableName) throws SQLException {
         List<ColumnData> columnList = new ArrayList<>();
         String query, limit = "";
 
         try (Statement stmt = this.conn.createStatement()) {
             if (catalog != null)
                 query = Sqlite3.mprintf("PRAGMA %Q.table_info(%Q)", catalog, tableName);
             else
                 query = Sqlite3.mprintf("PRAGMA table_info(%Q)", tableName);
 
             try (ResultSet rs = stmt.executeQuery(query)) {
                 while (rs.next()) {
                     ColumnData cd = new ColumnData(this.conn.getHandle(), catalog, tableName, rs);
 
                     if (cd.primaryKey == 0)
                         continue;
                     columnList.add(cd);
                 }
             }
         }
 
         String constantQuery = "";
 
         for (int lpc = 0; lpc < columnList.size(); lpc++) {
             if (!constantQuery.isEmpty())
                 constantQuery += " UNION ALL ";
             constantQuery += PRIMARY_KEY_QUERY;
         }
         if (constantQuery.isEmpty()) {
             constantQuery = PRIMARY_KEY_QUERY;
             limit = " LIMIT 0";
         }
         constantQuery += " ORDER BY COLUMN_NAME";
         constantQuery += limit;
 
         PreparedStatement ps = this.conn.prepareStatement(constantQuery);
 
         ps.closeOnCompletion();
 
         int index = 1;
 
         for (ColumnData column : columnList) {
             ps.setString(index++, catalog);
             ps.setString(index++, tableName);
             ps.setString(index++, column.name);
             ps.setInt(index++, column.primaryKey);
         }
 
         return ps.executeQuery();
     }
 
     public static class ForeignKeyData {
         private static final Map<String, Integer> ACTION_MAP = new HashMap<>();
 
         static {
             ACTION_MAP.put("SET NULL", DatabaseMetaData.importedKeySetNull);
             ACTION_MAP.put("SET DEFAULT", DatabaseMetaData.importedKeySetDefault);
             ACTION_MAP.put("CASCADE", DatabaseMetaData.importedKeyCascade);
             ACTION_MAP.put("RESTRICT", DatabaseMetaData.importedKeyRestrict);
             ACTION_MAP.put("NO ACTION", DatabaseMetaData.importedKeyNoAction);
         }
 
         private static final int actionStringToInt(String actionStr) {
             Integer actionInt = ACTION_MAP.get(actionStr);
 
             if (actionInt == null)
                 throw new RuntimeException("Unknown sqlite action string " + actionStr);
 
             return actionInt;
         }
 
         public final int id;
         public final int seq;
         public final String fromTable;
         public final String fromColumn;
         public final String toTable;
         public final String toColumn;
         public final int onUpdate;
         public final int onDelete;
         public final String match;
 
         public ForeignKeyData(String fromTable, ResultSet rs) throws SQLException {
             this.fromTable = fromTable;
             this.id = rs.getInt("id");
             this.seq = rs.getInt("seq");
             this.toTable = rs.getString("table");
             this.fromColumn = rs.getString("from");
             this.toColumn = rs.getString("to");
             this.onUpdate = actionStringToInt(rs.getString("on_update"));
             this.onDelete = actionStringToInt(rs.getString("on_delete"));
             this.match = rs.getString("match");
         }
 
         public ForeignKeyData(String fromTable, String toTable) {
             this.id = -1;
             this.seq = 0;
             this.fromTable = fromTable;
             this.fromColumn = null;
             this.toTable = toTable;
             this.toColumn = null;
             this.onUpdate = -1;
             this.onDelete = -1;
             this.match = null;
         }
     }
 
     private Map<String, List<ForeignKeyData>> getForeignKeyData(String catalog) throws SQLException {
         Map<String, List<ForeignKeyData>> table2Key = new HashMap<>();
 
         try (Statement stmt = this.conn.createStatement()) {
             List<String> allTables = new ArrayList<>();
             String tableQuery;
 
             /* XXX We need to iterate over all of the catalogs */
             if (catalog != null) {
                 tableQuery = Sqlite3.mprintf(
                     "SELECT name FROM %Q.sqlite_master WHERE type='table'", catalog);
             }
             else {
                 tableQuery = "SELECT name FROM sqlite_master WHERE type='table'";
             }
 
             try (ResultSet rs = stmt.executeQuery(tableQuery)) {
                 while (rs.next()) {
                     allTables.add(rs.getString(1));
                 }
             }
 
             for (String catalogTable : allTables) {
                 try (ResultSet rs = stmt.executeQuery(Sqlite3.mprintf("PRAGMA %Q.foreign_key_list(%Q)",
                         catalog, catalogTable))) {
                     while (rs.next()) {
                         ForeignKeyData fkd = new ForeignKeyData(catalogTable, rs);
 
                         if (!table2Key.containsKey(fkd.fromTable))
                             table2Key.put(fkd.fromTable, new ArrayList<ForeignKeyData>());
                         if (!table2Key.containsKey(fkd.toTable))
                             table2Key.put(fkd.toTable, new ArrayList<ForeignKeyData>());
 
                         table2Key.get(fkd.fromTable).add(fkd);
                         table2Key.get(fkd.toTable).add(fkd);
                     }
                 }
             }
         }
 
         return table2Key;
     }
 
     private static final String FOREIGN_KEY_QUERY = "SELECT ? AS PKTABLE_CAT, " +
             "NULL AS PKTABLE_SCHEM, ? AS PKTABLE_NAME, " +
             "? AS PKCOLUMN_NAME, ? AS FKTABLE_CAT, NULL AS FKTABLE_SCHEM, " +
             "? AS FKTABLE_NAME, ? AS FKCOLUMN_NAME, ? AS KEY_SEQ, ? AS UPDATE_RULE, " +
             "? AS DELETE_RULE, NULL AS FK_NAME, NULL AS PK_NAME, ? AS DEFERRABILITY ";
 
     private ResultSet getForeignKeys(String catalog, String fromTable, String toTable) throws SQLException {
         Map<String, List<ForeignKeyData>> table2Key = getForeignKeyData(catalog);
         List<ForeignKeyData> columnList = new ArrayList<>();
         String limit = "";
 
         if (table2Key.containsKey(fromTable)) {
             columnList.addAll(table2Key.get(fromTable));
         }
         if (table2Key.containsKey(toTable)) {
             columnList.addAll(table2Key.get(toTable));
         }
 
         String constantQuery = "";
 
         for (ForeignKeyData fkd : columnList) {
             if (fromTable != null && !fromTable.equals(fkd.fromTable))
                 continue;
             if (toTable != null && !toTable.equals(fkd.toTable))
                 continue;
 
             if (!constantQuery.isEmpty())
                 constantQuery += " UNION ALL ";
             constantQuery += FOREIGN_KEY_QUERY;
         }
         if (constantQuery.isEmpty()) {
             constantQuery = FOREIGN_KEY_QUERY;
             limit = " LIMIT 0";
         }
         constantQuery += " ORDER BY PKTABLE_CAT, PKTABLE_SCHEM, PKTABLE_NAME, KEY_SEQ";
         constantQuery += limit;
 
         PreparedStatement ps = this.conn.prepareStatement(constantQuery);
 
         ps.closeOnCompletion();
 
         int index = 1;
 
         for (ForeignKeyData fkd : columnList) {
             if (fromTable != null && !fromTable.equals(fkd.fromTable))
                 continue;
             if (toTable != null && !toTable.equals(fkd.toTable))
                 continue;
 
             ps.setString(index++, catalog);
             ps.setString(index++, fkd.toTable);
             ps.setString(index++, fkd.toColumn);
             ps.setString(index++, catalog);
             ps.setString(index++, fkd.fromTable);
             ps.setString(index++, fkd.fromColumn);
             ps.setInt(index++, fkd.seq + 1);
             ps.setInt(index++, fkd.onUpdate);
             ps.setInt(index++, fkd.onDelete);
             ps.setInt(index++, importedKeyInitiallyImmediate); // XXX
         }
 
         return ps.executeQuery();
     }
 
     /**
      * The DEFERRABILITY is always set to the value of 'importedKeyInitiallyImmediate'
      * since there is no way to tell at run time whether it is set to immediate or
      * deferred.
      *
      * {@inheritDoc}
      */
     @Override
     public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
         return this.getForeignKeys(catalog, table, null);
     }
 
     @Override
     public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
         return this.getForeignKeys(catalog, null, table);
     }
 
     @Override
     public ResultSet getCrossReference(String parentCatalog,
                                        String parentSchema,
                                        String parentTable,
                                        String foreignCatalog,
                                        String foreignSchema,
                                        String foreignTable) throws SQLException {
         if (parentCatalog != foreignCatalog)
             throw new SQLNonTransientException("Catalog names must be the same");
 
         return this.getForeignKeys(parentCatalog, foreignTable, parentTable);
     }
 
     @Override
     public ResultSet getTypeInfo() throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public ResultSet getIndexInfo(String s, String s2, String s3, boolean b, boolean b2) throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsResultSetType(int i) throws SQLException {
         return (i == ResultSet.TYPE_FORWARD_ONLY);
     }
 
     @Override
     public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
         return (type == ResultSet.TYPE_FORWARD_ONLY && concurrency == ResultSet.CONCUR_READ_ONLY);
     }
 
     @Override
     public boolean ownUpdatesAreVisible(int i) throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean ownDeletesAreVisible(int i) throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean ownInsertsAreVisible(int i) throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean othersUpdatesAreVisible(int i) throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean othersDeletesAreVisible(int i) throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean othersInsertsAreVisible(int i) throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean updatesAreDetected(int i) throws SQLException {
         return false;
     }
 
     @Override
     public boolean deletesAreDetected(int i) throws SQLException {
         return false;
     }
 
     @Override
     public boolean insertsAreDetected(int i) throws SQLException {
         return false;
     }
 
     @Override
     public boolean supportsBatchUpdates() throws SQLException {
         return true;
     }
 
     @Override
     public ResultSet getUDTs(String s, String s2, String s3, int[] ints) throws SQLException {
         return this.executeConstantQuery(
                 "SELECT null as TYPE_CAT, null as TYPE_SCHEM, null as TYPE_NAME, " +
                         "null as CLASS_NAME, null as DATA_TYPE, null as REMARKS, null as BASE_TYPE " +
                         "LIMIT 0"
         );
     }
 
     @Override
     public Connection getConnection() throws SQLException {
         return this.conn;
     }
 
     @Override
     public boolean supportsSavepoints() throws SQLException {
         return true;
     }
 
     @Override
     public boolean supportsNamedParameters() throws SQLException {
         return false;
     }
 
     @Override
     public boolean supportsMultipleOpenResults() throws SQLException {
         return false;
     }
 
     @Override
     public boolean supportsGetGeneratedKeys() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public ResultSet getSuperTypes(String s, String s2, String s3) throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public ResultSet getSuperTables(String s, String s2, String s3) throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public ResultSet getAttributes(String s, String s2, String s3, String s4) throws SQLException {
         return this.executeConstantQuery(
                 "SELECT null as TYPE_CAT, NULL AS TYPE_SCHEM, NULL AS TYPE_NAME, " +
                         "NULL AS ATTR_NAME, NULL AS DATA_TYPE, NULL AS ATTR_TYPE_NAME, " +
                         "NULL AS ATTR_SIZE, NULL AS DECIMAL_DIGITS, NULL AS NUM_PREC_RADIX, " +
                         "NULL AS NULLABLE, NULL AS REMARKS, NULL AS ATTR_DEF, " +
                         "NULL AS SQL_DATA_TYPE, NULL AS SQL_DATETIME_SUB, " +
                         "NULL AS CHAR_OCTET_LENGTH, NULL AS ORDINAL_POSITION, " +
                         "NULL AS IS_NULLABLE, NULL AS SCOPE_CATALOG, NULL AS SCOPE_SCHEMA, " +
                         "NULL AS SCOPE_TABLE, NULL AS SOURCE_DATA_TYPE LIMIT 0"
         );
     }
 
     @Override
     public boolean supportsResultSetHoldability(int i) throws SQLException {
         return (i == ResultSet.CLOSE_CURSORS_AT_COMMIT);
     }
 
     @Override
     public int getResultSetHoldability() throws SQLException {
         return ResultSet.CLOSE_CURSORS_AT_COMMIT;
     }
 
     @Override
     public int getDatabaseMajorVersion() throws SQLException {
         int version = Sqlite3.sqlite3_libversion_number();
 
         return version / 1000000;
     }
 
     @Override
     public int getDatabaseMinorVersion() throws SQLException {
         int version = Sqlite3.sqlite3_libversion_number();
 
         return (version / 1000) % 1000;
     }
 
     @Override
     public int getJDBCMajorVersion() throws SQLException {
         return 4;
     }
 
     @Override
     public int getJDBCMinorVersion() throws SQLException {
         return 0;
     }
 
     @Override
     public int getSQLStateType() throws SQLException {
         return 0;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean locatorsUpdateCopy() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsStatementPooling() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public RowIdLifetime getRowIdLifetime() throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public ResultSet getSchemas(String s, String s2) throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
         return false;
     }
 
     @Override
     public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public ResultSet getClientInfoProperties() throws SQLException {
         return this.executeConstantQuery(
                 "SELECT '' AS NAME, 0 as MAX_LEN, '' as DEFAULT_VALUE, '' as DESCRIPTION LIMIT 0");
     }
 
     @Override
     public ResultSet getFunctions(String s, String s2, String s3) throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public ResultSet getFunctionColumns(String s, String s2, String s3, String s4) throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean generatedKeyAlwaysReturned() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public <T> T unwrap(Class<T> tClass) throws SQLException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean isWrapperFor(Class<?> aClass) throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
