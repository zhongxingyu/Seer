 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.sm.business.persistence.sql;
 
 import de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.sm.business.Constants;
 import de.escidoc.core.sm.business.persistence.DirectDatabaseAccessorInterface;
 import de.escidoc.core.sm.business.vo.database.record.DatabaseRecordFieldVo;
 import de.escidoc.core.sm.business.vo.database.record.DatabaseRecordVo;
 import de.escidoc.core.sm.business.vo.database.select.AdditionalWhereFieldVo;
 import de.escidoc.core.sm.business.vo.database.select.AdditionalWhereGroupVo;
 import de.escidoc.core.sm.business.vo.database.select.DatabaseSelectVo;
 import de.escidoc.core.sm.business.vo.database.select.RootWhereFieldVo;
 import de.escidoc.core.sm.business.vo.database.select.RootWhereGroupVo;
 import de.escidoc.core.sm.business.vo.database.select.SelectFieldVo;
 import de.escidoc.core.sm.business.vo.database.table.DatabaseIndexVo;
 import de.escidoc.core.sm.business.vo.database.table.DatabaseTableFieldVo;
 import de.escidoc.core.sm.business.vo.database.table.DatabaseTableVo;
 import org.springframework.jdbc.core.support.JdbcDaoSupport;
 
 import javax.sql.DataSource;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Class for direct JDBC Database access via Hibernate.
  * 
  * @author MIH
  * @sm
  */
 public class DirectPostgresDatabaseAccessor extends JdbcDaoSupport
     implements DirectDatabaseAccessorInterface {
     
     //Check CREATE INDEX and DROP INDEX Statement
     //Method getCreateStatements + get DropStatements
     
     //Check Where-Clause <tablename>.<fieldname> allowed?
     //Method handleFieldTypeWhere
     
     //Eventually convert xml:date-format to database-specific format
     //with Method convertDate()
     //for Method createRecord()
     
     //Check xPath-Methods(getXpathBoolean, getXpathString, getXpathNumneric)
 
     private static AppLogger log =
         new AppLogger(DirectPostgresDatabaseAccessor.class.getName());
 
     private static final String TIMESTAMP_FIELD_TYPE = "timestamp";
 
     private static final String TEXT_FIELD_TYPE = "text";
 
     private static final String NUMERIC_FIELD_TYPE = "numeric";
     
     private static final String SYSDATE = "'now'";
 
     private static final String DAY_OF_MONTH_FUNCTION = 
                         "date_trunc('day',${FIELD_NAME})";
 
     private static final Pattern FIELD_NAME_PATTERN = 
         Pattern.compile("\\$\\{FIELD_NAME\\}");
 
     private static Matcher FIELD_NAME_MATCHER = FIELD_NAME_PATTERN.matcher("");
 
     private static final String XPATH_BOOLEAN_FUNCTION = 
         "(xpath('${XPATH}', XMLPARSE(DOCUMENT ${FIELD_NAME})))[1]::text IS NOT NULL";
     
     private static final String XPATH_STRING_FUNCTION = 
         "(xpath('${XPATH}', XMLPARSE(DOCUMENT ${FIELD_NAME})))[1]::text";
     
     private static final String XPATH_NUMBER_FUNCTION = 
         "(xpath('${XPATH}', XMLPARSE(DOCUMENT ${FIELD_NAME})))[1]::text";
     
     private static final Pattern XPATH_PATTERN = 
                     Pattern.compile("\\$\\{XPATH\\}(.*?)\\$\\{FIELD_NAME\\}");
     
     private static Matcher XPATH_MATCHER = XPATH_PATTERN.matcher("");
 
     private static final Map<String, String> RESERVED_EXPRESSIONS = 
         new HashMap<String, String>() {
         {
             put("user", "");
             put("timestamp", "");
         }
     };
 
     /**
      * Converts xmldate into database-specific format.
      * Method is synchronized because 
      * SimpleDateFormatter is not Thread-Safe!
      * 
      * @param xmldate
      *            date in xml-format
      * @return String date in database-specific format
      * @throws SqlDatabaseSystemException e
      * 
      */
     private synchronized String convertDate(final String xmldate) 
                     throws SqlDatabaseSystemException {
         //do nothing
         return xmldate;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.sm.business.persistence.DirectDatabaseAccessorInterface
      *      #createTable(DatabaseTableVo)
      * 
      * @param databaseTableVo
      *            databaseTableVo with information about tablename, fieldnames
      *            and indexnames.
      * 
      * @throws SqlDatabaseSystemException
      *             If an error occurs accessing the database.
      * 
      */
     public void createTable(final DatabaseTableVo databaseTableVo)
         throws SqlDatabaseSystemException {
         checkDatabaseTableVo(databaseTableVo);
         Collection<String> sqls = getCreateStatements(databaseTableVo);
         try {
             for (String sql : sqls) {
                 getJdbcTemplate().execute(sql);
             }
         }
         catch (Exception e) {
             log.error(e);
             throw new SqlDatabaseSystemException(e);
         }
     }
 
     /**
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.sm.business.persistence.DirectDatabaseAccessorInterface
      *      #dropTable(DatabaseTableVo)
      * 
      * @param databaseTableVo
      *            databaseTableVo.
      * @throws SqlDatabaseSystemException
      *             If an error occurs accessing the database.
      * 
      */
     public void dropTable(final DatabaseTableVo databaseTableVo)
         throws SqlDatabaseSystemException {
         checkDatabaseTableVo(databaseTableVo);
         Collection<String> sqls = getDropStatements(databaseTableVo);
         try {
             for (String sql : sqls) {
                 getJdbcTemplate().execute(sql);
             }
         }
         catch (Exception e) {
             log.error(e);
             throw new SqlDatabaseSystemException(e);
         }
     }
 
     /**
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.sm.business.persistence.DirectDatabaseAccessorInterface
      *      #createRecord(DatabaseRecordVo)
      * 
      * @param databaseRecordVo
      *            databaseRecordVo with information about tablename and
      *            fieldnames + values.
      * @throws SqlDatabaseSystemException
      *             If an error occurs accessing the database.
      * 
      */
     public void createRecord(final DatabaseRecordVo databaseRecordVo)
         throws SqlDatabaseSystemException {
         checkDatabaseRecordVo(databaseRecordVo);
         try {
             String tablename = handleTableName(databaseRecordVo.getTableName());
             StringBuffer sql = new StringBuffer("");
             StringBuffer fieldsSql = new StringBuffer("");
             StringBuffer valuesSql = new StringBuffer(" VALUES (");
             fieldsSql.append("INSERT INTO ").append(tablename).append(" (");
             Collection<DatabaseRecordFieldVo> fields = 
                 databaseRecordVo.getDatabaseRecordFieldVos();
             int i = 0;
             for (DatabaseRecordFieldVo field : fields) {
                 if (i > 0) {
                     fieldsSql.append(",");
                     valuesSql.append(",");
                 }
                 fieldsSql.append(field.getFieldName());
 
                 String value = field.getFieldValue();
 
                 // Case type=date and value=sysdate => sysdate
                 // (is 'now' in postgres)
                 if (field.getFieldType().equalsIgnoreCase(
                     Constants.DATABASE_FIELD_TYPE_DATE)) {
                     if (value.equalsIgnoreCase("sysdate")) {
                         value = SYSDATE;
                     }
                     else {
                         value = "'" + convertDate(value) + "'";
                     }
                 } else if (field.getFieldType().equalsIgnoreCase(
                     Constants.DATABASE_FIELD_TYPE_TEXT)) {
                     value = value.replaceAll("'", "''");
                     value = "'" + value + "'";
                 }
 
                 valuesSql.append(value);
                 i++;
             }
             fieldsSql.append(") ");
             valuesSql.append(");");
             sql.append(fieldsSql).append(valuesSql);
             getJdbcTemplate().execute(sql.toString());
         }
         catch (Exception e) {
             log.error(e);
             throw new SqlDatabaseSystemException(e);
         }
 
     }
 
     /**
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.sm.business.persistence.DirectDatabaseAccessorInterface
      *      #deleteRecord(DatabaseRecordVo)
      * 
      * @param databaseSelectVo
      *            databaseSelectVo.
      * @throws SqlDatabaseSystemException
      *             If an error occurs accessing the database.
      * 
      */
     public void deleteRecord(final DatabaseSelectVo databaseSelectVo)
         throws SqlDatabaseSystemException {
         checkDatabaseSelectVo(databaseSelectVo);
         try {
             String tablename =
                 handleTableName(databaseSelectVo
                     .getTableNames().iterator().next());
             StringBuffer sql = new StringBuffer("");
             sql.append("DELETE FROM ").append(tablename);
             if (databaseSelectVo.getRootWhereGroupVo() != null) {
                 sql.append(" WHERE ");
                 sql.append(handleWhereClause(databaseSelectVo));
             }
             sql.append(";");
 
             getJdbcTemplate().execute(sql.toString());
         }
         catch (Exception e) {
             log.error(e);
             throw new SqlDatabaseSystemException(e);
         }
     }
 
     /**
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.sm.business.persistence.DirectDatabaseAccessorInterface
      *      #updateRecord(DatabaseSelectVo)
      * 
      * @param databaseSelectVo
      *            databaseSelectVo.
      * @throws SqlDatabaseSystemException
      *             If an error occurs accessing the database.
      * 
      */
     public void updateRecord(final DatabaseSelectVo databaseSelectVo)
         throws SqlDatabaseSystemException {
         checkDatabaseSelectVo(databaseSelectVo);
         try {
             String tablename =
                 handleTableName(databaseSelectVo
                     .getTableNames().iterator().next());
             StringBuffer sql = new StringBuffer("");
             sql.append("UPDATE ").append(tablename).append(" SET ");
             int i = 0;
             for (SelectFieldVo selectFieldVo
                 : databaseSelectVo.getSelectFieldVos()) {
                 if (i > 0) {
                     sql.append(",");
                 }
                 sql.append(handleFieldTypeWhere(null, selectFieldVo
                     .getFieldName(), selectFieldVo.getFieldType(),
                     selectFieldVo.getFieldValue(), "=", null));
                 i++;
             }
             if (databaseSelectVo.getRootWhereGroupVo() != null) {
                 sql.append(" WHERE ");
                 sql.append(handleWhereClause(databaseSelectVo));
             }
             sql.append(";");
 
             getJdbcTemplate().execute(sql.toString());
         }
         catch (Exception e) {
             log.error(e);
             throw new SqlDatabaseSystemException(e);
         }
     }
 
     /**
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.sm.business.persistence.DirectDatabaseAccessorInterface
      *      #executeSql(DatabaseSelectVo)
      * 
      * @param databaseSelectVo
      *            databaseSelectVo with information about sql.
      * @return List of Maps with data (one Map per db-record)
      * @throws SqlDatabaseSystemException
      *             If an error occurs accessing the database.
      * 
      */
     public List executeSql(final DatabaseSelectVo databaseSelectVo)
         throws SqlDatabaseSystemException {
         checkDatabaseSelectVo(databaseSelectVo);
         try {
             StringBuffer sql = new StringBuffer("");
             sql.append(databaseSelectVo.getSelectType()).append(" ");
             if (databaseSelectVo.getSelectType().equalsIgnoreCase(
                 Constants.DATABASE_SELECT_TYPE_UPDATE)) {
                 String tablename =
                     handleTableName(databaseSelectVo
                         .getTableNames().iterator().next());
                 sql.append(tablename).append(" SET ");
             }
             else if (databaseSelectVo.getSelectType().equalsIgnoreCase(
                 Constants.DATABASE_SELECT_TYPE_DELETE)) {
                 String tablename =
                     handleTableName(databaseSelectVo
                         .getTableNames().iterator().next());
                 sql.append(" FROM ").append(tablename).append(" ");
             }
             else if (databaseSelectVo.getSelectType().equalsIgnoreCase(
                 Constants.DATABASE_SELECT_TYPE_SELECT)) {
                 sql.append(handleSelectFields(databaseSelectVo
                     .getSelectFieldVos()));
                 sql.append(" FROM ");
                 int i = 0;
                 for (String tabname
                     : databaseSelectVo.getTableNames()) {
                     String tablename = handleTableName(tabname);
                     if (i > 0) {
                         sql.append(",");
                     }
                     sql.append(tablename).append(" AS ");
                     sql
                         .append(tablename.replaceFirst(".*?\\.", "")).append(
                             " ");
                     i++;
                 }
                 if (databaseSelectVo.getRootWhereGroupVo() != null) {
                     sql.append(" WHERE ");
                     sql.append(handleWhereClause(databaseSelectVo));
                 }
             }
 
             sql.append(";");
 
            List resultList = getJdbcTemplate().queryForList(sql.toString());
            return resultList;
         }
         catch (Exception e) {
             throw new SqlDatabaseSystemException(e);
         }
     }
 
     /**
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.sm.business.persistence.DirectDatabaseAccessorInterface
      *      #executeSql(java.lang.String)
      * 
      * @param sql
      *            sql-String
      * @return List of Maps with data
      * @throws SqlDatabaseSystemException
      *             If an error occurs accessing the database.
      * 
      */
     public List executeReadOnlySql(final String sql) throws SqlDatabaseSystemException {
         boolean condition = false;
         String executionSql = sql;
         executionSql = executionSql.replaceAll("\\s+", " ");
         if (executionSql.matches("(?i).* (where|order by|group by) .*")) {
             condition = true;
         }
         String fromClause;
         if (condition) {
             fromClause =
                 executionSql.replaceFirst(
                     "(?i).*?from(.*?)(where|order by|group by).*", "$1");
         }
         else {
             fromClause = executionSql.replaceFirst("(?i).*?from(.*)", "$1");
         }
         String[] tables = fromClause.split(",");
         StringBuffer replacedFromClause = new StringBuffer(" ");
         for (int i = 0; i < tables.length; i++) {
             if (i > 0) {
                 replacedFromClause.append(",");
             }
             replacedFromClause.append(handleTableName(tables[i].trim()));
         }
         replacedFromClause.append(" ");
         if (condition) {
             executionSql =
                 executionSql.replaceFirst(
                     "(?i)(.*?from).*?((where|order by|group by).*)", "$1"
                         + Matcher
                         .quoteReplacement(replacedFromClause.toString()) 
                         + "$2");
         }
         else {
             executionSql =
                 executionSql.replaceFirst("(?i)(.*?from).*", "$1"
                     + Matcher
                     .quoteReplacement(replacedFromClause.toString()));
         }
 
         try {
             return getJdbcTemplate().queryForList(executionSql);
         }
         catch (Exception e) {
             throw new SqlDatabaseSystemException(e);
         }
     }
 
     /**
      * Create create sql-statement from databaseTableVo.
      * 
      * @param databaseTableVo
      *            databaseTableVo with information about tablename, fieldnames
      *            and indexnames.
      * @return Collection with sql-statements
      * 
      */
     private Collection<String> getCreateStatements(
             final DatabaseTableVo databaseTableVo) {
 
         Collection<String> sqlStatements = new ArrayList<String>();
         // Get Create-Statement for Table
         String tablename = handleTableName(databaseTableVo.getTableName());
         StringBuffer createSql = new StringBuffer("CREATE TABLE ");
         createSql.append(tablename).append(" (");
         int i = 0;
         for (DatabaseTableFieldVo databaseTableFieldVo 
                 : databaseTableVo.getDatabaseFieldVos()) {
             if (i > 0) {
                 createSql.append(",");
             }
             String dbDataType = "";
             if (databaseTableFieldVo.getFieldType().equals(
                 Constants.DATABASE_FIELD_TYPE_DATE)) {
                 dbDataType = TIMESTAMP_FIELD_TYPE;
             } else if (databaseTableFieldVo.getFieldType().equals(
                     Constants.DATABASE_FIELD_TYPE_NUMERIC)) {
                 dbDataType = NUMERIC_FIELD_TYPE;
             } else if (databaseTableFieldVo.getFieldType().equals(
                     Constants.DATABASE_FIELD_TYPE_TEXT)) {
                 dbDataType = TEXT_FIELD_TYPE;
             }
             createSql
                 .append(databaseTableFieldVo.getFieldName()).append(" ")
                 .append(dbDataType).append("");
             i++;
         }
         createSql.append(");");
         sqlStatements.add(createSql.toString());
 
         // Get Create-Statements for Indexes
         Collection<DatabaseIndexVo> databaseIndexVos =
             databaseTableVo.getDatabaseIndexVos();
         if (databaseIndexVos != null) {
             for (DatabaseIndexVo databaseIndexVo : databaseIndexVos) {
                 StringBuffer indexSql = new StringBuffer("CREATE INDEX ");
                 String indexName = databaseIndexVo.getIndexName();
                 indexSql
                     .append(indexName).append(" ON ").append(tablename).append(
                         " (");
                 Collection<String> indexFields = databaseIndexVo.getFields();
                 int j = 0;
                 for (String indexField : indexFields) {
                     if (j > 0) {
                         indexSql.append(",");
                     }
                     indexSql.append(indexField);
                     j++;
                 }
                 indexSql.append(");");
                 sqlStatements.add(indexSql.toString());
             }
         }
         return sqlStatements;
 
     }
 
     /**
      * Create drop sql-statements from databaseTableVo.
      * 
      * @param databaseTableVo
      *            databaseTableVo with information about tablename and
      *            indexnames.
      * @return Collection with sql-statements
      * 
      */
     private Collection<String> getDropStatements(
             final DatabaseTableVo databaseTableVo) {
         Collection<String> sqlStatements = new ArrayList<String>();
         String tablename = handleTableName(databaseTableVo.getTableName());
         // Get Drop-Statements for Indexes
         Collection<DatabaseIndexVo> databaseIndexVos =
             databaseTableVo.getDatabaseIndexVos();
         if (databaseIndexVos != null) {
             for (DatabaseIndexVo databaseIndexVo : databaseIndexVos) {
                 StringBuffer indexSql = new StringBuffer("DROP INDEX ");
                 String indexName = databaseIndexVo.getIndexName();
                 if (!indexName.matches(".*\\..*")) {
                     indexName = Constants.SM_SCHEMA_NAME + "." + indexName;
                 }
                 indexSql.append(indexName).append(";");
                 sqlStatements.add(indexSql.toString());
             }
         }
 
         // Get Drop-Statement for Table
         StringBuffer dropSql = new StringBuffer("DROP TABLE ");
         dropSql.append(tablename);
         sqlStatements.add(dropSql.toString());
 
         return sqlStatements;
 
     }
 
     /**
      * makes string for where-clause out of databaseSelectVo.
      * 
      * @param databaseSelectVo
      *            databaseSelectVo.
      * @return String string for where clause
      * @throws SqlDatabaseSystemException
      *             If an error occurs accessing the database.
      * 
      */
     private String handleWhereClause(final DatabaseSelectVo databaseSelectVo)
         throws SqlDatabaseSystemException {
         StringBuffer whereClause = new StringBuffer(" ");
         boolean additionalWhereGroups = false;
         if (databaseSelectVo.getAdditionalWhereGroupVos() != null
             && !databaseSelectVo.getAdditionalWhereGroupVos().isEmpty()) {
             additionalWhereGroups = true;
         }
         if (additionalWhereGroups) {
             whereClause.append("(");
         }
         RootWhereGroupVo rootWhereGroupVo =
             databaseSelectVo.getRootWhereGroupVo();
         RootWhereFieldVo rootWhereFieldVo =
             rootWhereGroupVo.getRootWhereFieldVo();
         if (databaseSelectVo.getSelectType().equals(
             Constants.DATABASE_SELECT_TYPE_UPDATE)
             || databaseSelectVo.getSelectType().equals(
                 Constants.DATABASE_SELECT_TYPE_DELETE)) {
             rootWhereFieldVo.setTableName(null);
         }
         whereClause.append(handleFieldTypeWhere(
             rootWhereFieldVo.getTableName(), rootWhereFieldVo.getFieldName(),
             rootWhereFieldVo.getFieldType(), rootWhereFieldVo.getFieldValue(),
             rootWhereFieldVo.getOperator(), rootWhereFieldVo.getXpath()));
         if (rootWhereGroupVo.getAdditionalWhereFieldVos() != null
             && !rootWhereGroupVo.getAdditionalWhereFieldVos().isEmpty()) {
             for (AdditionalWhereFieldVo additionalWhereFieldVo
                 : rootWhereGroupVo.getAdditionalWhereFieldVos()) {
                 whereClause.append(additionalWhereFieldVo.getAlliance());
                 if (databaseSelectVo.getSelectType().equals(
                     Constants.DATABASE_SELECT_TYPE_UPDATE)
                     || databaseSelectVo.getSelectType().equals(
                         Constants.DATABASE_SELECT_TYPE_DELETE)) {
                     additionalWhereFieldVo.setTableName(null);
                 }
                 whereClause.append(handleFieldTypeWhere(additionalWhereFieldVo
                     .getTableName(), additionalWhereFieldVo.getFieldName(),
                     additionalWhereFieldVo.getFieldType(),
                     additionalWhereFieldVo.getFieldValue(),
                     additionalWhereFieldVo.getOperator(),
                     additionalWhereFieldVo.getXpath()));
             }
         }
         if (additionalWhereGroups) {
             whereClause.append(") ");
             for (AdditionalWhereGroupVo additionalWhereGroupVo
                 : databaseSelectVo.getAdditionalWhereGroupVos()) {
                 whereClause.append(additionalWhereGroupVo.getAlliance());
                 whereClause.append(" (");
                 rootWhereFieldVo = additionalWhereGroupVo.getRootWhereFieldVo();
                 if (databaseSelectVo.getSelectType().equals(
                     Constants.DATABASE_SELECT_TYPE_UPDATE)
                     || databaseSelectVo.getSelectType().equals(
                         Constants.DATABASE_SELECT_TYPE_DELETE)) {
                     rootWhereFieldVo.setTableName(null);
                 }
                 whereClause.append(handleFieldTypeWhere(rootWhereFieldVo
                     .getTableName(), rootWhereFieldVo.getFieldName(),
                     rootWhereFieldVo.getFieldType(), rootWhereFieldVo
                         .getFieldValue(), rootWhereFieldVo.getOperator(),
                     rootWhereFieldVo.getXpath()));
                 if (additionalWhereGroupVo.getAdditionalWhereFieldVos() != null
                     && !additionalWhereGroupVo
                         .getAdditionalWhereFieldVos().isEmpty()) {
                     for (AdditionalWhereFieldVo additionalWhereFieldVo
                         : additionalWhereGroupVo.getAdditionalWhereFieldVos()) {
                         whereClause
                             .append(additionalWhereFieldVo.getAlliance());
                         if (databaseSelectVo.getSelectType().equals(
                             Constants.DATABASE_SELECT_TYPE_UPDATE)
                             || databaseSelectVo.getSelectType().equals(
                                 Constants.DATABASE_SELECT_TYPE_DELETE)) {
                             additionalWhereFieldVo.setTableName(null);
                         }
                         whereClause.append(handleFieldTypeWhere(
                             additionalWhereFieldVo.getTableName(),
                             additionalWhereFieldVo.getFieldName(),
                             additionalWhereFieldVo.getFieldType(),
                             additionalWhereFieldVo.getFieldValue(),
                             additionalWhereFieldVo.getOperator(),
                             additionalWhereFieldVo.getXpath()));
                     }
                 }
                 whereClause.append(") ");
             }
         }
 
         return whereClause.append(" ").toString();
     }
 
     /**
      * makes string for where-clause out of databaseSelectVo.
      * 
      * @param tableName
      *            tableName.
      * @param fieldName
      *            fieldName.
      * @param fieldType
      *            fieldType.
      * @param fieldValue
      *            fieldValue.
      * @param operator
      *            operator.
      * @param xpath
      *            xpath.
      * @return String string for where clause
      * @throws SqlDatabaseSystemException
      *             If an error occurs accessing the database.
      * 
      */
     private String handleFieldTypeWhere(
         final String tableName, final String fieldName, final String fieldType,
         final String fieldValue, final String operator, final String xpath)
         throws SqlDatabaseSystemException {
         StringBuffer whereClause = new StringBuffer(" ");
         StringBuffer longFieldName = new StringBuffer("");
         if (tableName != null && !tableName.equals("")) {
             longFieldName.append(
                 handleTableName(tableName).replaceFirst(".*?\\.", "")).append(
                 ".");
         }
         longFieldName.append(fieldName);
         if (fieldType.equalsIgnoreCase(Constants.DATABASE_FIELD_TYPE_TEXT)) {
             String value = fieldValue.replaceAll("'", "''");
             whereClause
                 .append(longFieldName).append(" ").append(operator)
                 .append(" '").append(value).append("' ");
         }
         else if (fieldType.endsWith(Constants.DATABASE_FIELD_TYPE_DATE)) {
             if (fieldType
                 .equalsIgnoreCase(Constants.DATABASE_FIELD_TYPE_DAYDATE)) {
                 String dayOfMonthFunction = FIELD_NAME_MATCHER
                     .reset(DAY_OF_MONTH_FUNCTION)
                     .replaceAll(Matcher.quoteReplacement(
                             longFieldName.toString()));
 
                 whereClause
                     .append(dayOfMonthFunction).append(operator).append(" ");
             }
             else {
                 whereClause.append(longFieldName).append(operator).append(" ");
             }
             String value = "";
             if (fieldValue.equalsIgnoreCase("sysdate")) {
                 value = SYSDATE;
             }
             else {
                 value = "'" + convertDate(fieldValue) + "'";
             }
             whereClause.append(value).append(" ");
         }
         else if (fieldType
             .equalsIgnoreCase(Constants.DATABASE_FIELD_TYPE_NUMERIC)) {
             whereClause
                 .append(longFieldName).append(" ").append(operator).append(
                     fieldValue).append(" ");
         }
         else if (fieldType
             .equalsIgnoreCase(Constants.DATABASE_FIELD_TYPE_XPATH_BOOLEAN)) {
             whereClause.append(getXpathBoolean(xpath, fieldName)).append(" ");
         }
         else if (fieldType
             .equalsIgnoreCase(Constants.DATABASE_FIELD_TYPE_XPATH_STRING)) {
             whereClause
                 .append(getXpathString(xpath, fieldName)).append(" ").append(
                     operator).append(" '").append(fieldValue.trim()).append("' ");
         }
         else if (fieldType
             .equalsIgnoreCase(Constants.DATABASE_FIELD_TYPE_XPATH_NUMERIC)) {
             whereClause
                 .append(getXpathNumeric(xpath, fieldName)).append(" ").append(
                     operator).append(" '").append(fieldValue.trim()).append("' ");
         }
         else if (fieldType
             .equalsIgnoreCase(Constants.DATABASE_FIELD_TYPE_FREE_SQL)) {
             whereClause.append(fieldValue);
         }
         else {
             log.error("wrong fieldType given");
             throw new SqlDatabaseSystemException("wrong fieldType given");
         }
         return whereClause.append(" ").toString();
     }
 
     /**
      * Handles select-fields for sql-query.
      * 
      * @param selectFieldVos
      *            Collection of SelectFieldVo.
      * @return String string with fields to select
      * @throws SqlDatabaseSystemException
      *             If an error occurs accessing the database.
      * 
      */
     private String handleSelectFields(
         final Collection<SelectFieldVo> selectFieldVos)
         throws SqlDatabaseSystemException {
         StringBuffer selectFields = new StringBuffer(" ");
         int i = 0;
         for (SelectFieldVo selectFieldVo : selectFieldVos) {
             if (i > 0) {
                 selectFields.append(",");
             }
             if (selectFieldVo.getFieldName().equals("*")) {
                 selectFields.append("*");
                 break;
             }
             if (selectFieldVo.getTableName() != null
                 && !selectFieldVo.getTableName().equals("")) {
                 String tablename =
                     handleTableName(selectFieldVo.getTableName()).replaceFirst(
                         ".*?\\.", "");
                 selectFields.append(tablename).append(".");
             }
             selectFields.append(selectFieldVo.getFieldName());
             i++;
         }
 
         return selectFields.append(" ").toString();
     }
 
     /**
      * Get database-dependant sql-query-part for an xpath-boolean request.
      * 
      * @param xpath
      *            xpath-expression.
      * @param field
      *            db-field.
      * @return String database-dependant query for an xpath-boolean request.
      * 
      */
     public String getXpathBoolean(final String xpath, final String field) {
         String xpathBol = XPATH_MATCHER
             .reset(XPATH_BOOLEAN_FUNCTION)
             .replaceAll(Matcher.quoteReplacement(xpath) 
                     + "$1" 
                     + Matcher.quoteReplacement(field));
         return xpathBol;
     }
 
     /**
      * Get database-dependant sql-query-part for an xpath-string request.
      * 
      * @param xpath
      *            xpath-expression.
      * @param field
      *            db-field.
      * @return String database-dependant query for an xpath-string request.
      * 
      */
     public String getXpathString(final String xpath, final String field) {
         StringBuffer replacedXpath = new StringBuffer(xpath.trim());
         if (!replacedXpath.toString().endsWith("text()")) {
             if (!replacedXpath.toString().endsWith("/")) {
                 replacedXpath.append("/");
             }
             replacedXpath.append("text()");
         }
         String xpathString = XPATH_MATCHER
             .reset(XPATH_STRING_FUNCTION)
             .replaceAll(Matcher.quoteReplacement(replacedXpath.toString()) 
                     + "$1" 
                     + Matcher.quoteReplacement(field));
         return xpathString;
     }
 
     /**
      * Get database-dependant sql-query-part for an xpath-numeric request.
      * 
      * @param xpath
      *            xpath-expression.
      * @param field
      *            db-field.
      * @return String database-dependant query for an xpath-string request.
      * 
      */
     public String getXpathNumeric(final String xpath, final String field) {
         StringBuffer replacedXpath = new StringBuffer(xpath.trim());
         if (!replacedXpath.toString().endsWith("text()")) {
             if (!replacedXpath.toString().endsWith("/")) {
                 replacedXpath.append("/");
             }
             replacedXpath.append("text()");
         }
         String xpathNumber = XPATH_MATCHER
             .reset(XPATH_NUMBER_FUNCTION)
             .replaceAll(Matcher.quoteReplacement(replacedXpath.toString()) 
                     + "$1" 
                     + Matcher.quoteReplacement(field));
         return xpathNumber;
     }
 
     /**
      * checks DatabaseTableVo for validity.
      * 
      * @param databaseTableVo
      *            databaseTableVo.
      * @throws SqlDatabaseSystemException
      *             SqlDatabaseSystemException
      * 
      */
     public void checkDatabaseTableVo(final DatabaseTableVo databaseTableVo)
         throws SqlDatabaseSystemException {
         if (databaseTableVo.getTableName() == null
             || databaseTableVo.getTableName().equals("")) {
             log.error("tablename may not be empty");
             throw new SqlDatabaseSystemException("tablename may not be empty");
         }
         if (databaseTableVo.getDatabaseFieldVos() == null
             || databaseTableVo.getDatabaseFieldVos().isEmpty()) {
             log.error("database-fields may not be empty");
             throw new SqlDatabaseSystemException(
                 "database-fields may not be empty");
         }
         for (DatabaseTableFieldVo databaseTableFieldVo 
                 : databaseTableVo.getDatabaseFieldVos()) {
             if (databaseTableFieldVo.getFieldName() == null
                 || databaseTableFieldVo.getFieldName().equals("")) {
                 log.error("fieldname may not be empty");
                 throw new SqlDatabaseSystemException(
                     "fieldname may not be empty");
             }
             if (databaseTableFieldVo.getFieldType() == null
                 || databaseTableFieldVo.getFieldType().equals("")) {
                 log.error("fieldtype may not be empty");
                 throw new SqlDatabaseSystemException(
                     "fieldtype may not be empty");
             }
 
         }
         if (databaseTableVo.getDatabaseIndexVos() != null) {
             for (DatabaseIndexVo databaseIndexVo
                 : databaseTableVo.getDatabaseIndexVos()) {
                 if (databaseIndexVo.getIndexName() == null
                     || databaseIndexVo.getIndexName().equals("")) {
                     log.error("indexname may not be empty");
                     throw new SqlDatabaseSystemException(
                         "indexname may not be empty");
                 }
                 if (databaseIndexVo.getFields() == null) {
                     log.error("indexfields may not be empty");
                     throw new SqlDatabaseSystemException(
                         "indexfields may not be empty");
                 }
                 for (String field : databaseIndexVo.getFields()) {
                     if (field == null || field.equals("")) {
                         log.error("indexfield may not be null");
                         throw new SqlDatabaseSystemException(
                             "indexfield may not be null");
                     }
                 }
 
             }
         }
     }
 
     /**
      * checks DatabaseRecordVo for validity.
      * 
      * @param databaseRecordVo
      *            databaseRecordVo.
      * @throws SqlDatabaseSystemException
      *             SqlDatabaseSystemException
      * 
      */
     public void checkDatabaseRecordVo(final DatabaseRecordVo databaseRecordVo)
         throws SqlDatabaseSystemException {
         if (databaseRecordVo.getTableName() == null
             || databaseRecordVo.getTableName().equals("")) {
             log.error("tablename may not be empty");
             throw new SqlDatabaseSystemException("tablename may not be empty");
         }
         if (databaseRecordVo.getDatabaseRecordFieldVos() == null
             || databaseRecordVo.getDatabaseRecordFieldVos().isEmpty()) {
             log.error("database-fields may not be empty");
             throw new SqlDatabaseSystemException(
                 "database-fields may not be empty");
         }
         for (DatabaseRecordFieldVo databaseRecordFieldVo
             : databaseRecordVo.getDatabaseRecordFieldVos()) {
             if (databaseRecordFieldVo.getFieldName() == null
                 || databaseRecordFieldVo.getFieldName().equals("")) {
                 log.error("fieldname may not be empty");
                 throw new SqlDatabaseSystemException(
                     "fieldname may not be empty");
             }
             if (databaseRecordFieldVo.getFieldType() == null
                 || databaseRecordFieldVo.getFieldType().equals("")) {
                 log.error("fieldtype may not be empty");
                 throw new SqlDatabaseSystemException(
                     "fieldtype may not be empty");
             }
             if (databaseRecordFieldVo.getFieldValue() == null) {
                 log.error("fieldvalue may not be empty");
                 throw new SqlDatabaseSystemException(
                     "fieldvalue may not be empty");
             }
         }
     }
 
     /**
      * checks DatabaseSelectVo for validity.
      * 
      * @param databaseSelectVo
      *            databaseSelectVo.
      * @throws SqlDatabaseSystemException
      *             SqlDatabaseSystemException
      * 
      */
     public void checkDatabaseSelectVo(final DatabaseSelectVo databaseSelectVo)
         throws SqlDatabaseSystemException {
         if (databaseSelectVo.getTableNames() == null
             || databaseSelectVo.getTableNames().isEmpty()) {
             log.error("tablenames may not be empty");
             throw new SqlDatabaseSystemException("tablenames may not be empty");
         }
         for (String tablename : databaseSelectVo.getTableNames()) {
             if (tablename == null || tablename.equals("")) {
                 log.error("tablename may not be null");
                 throw new SqlDatabaseSystemException(
                     "tablename may not be null");
             }
         }
         if (databaseSelectVo.getSelectType() == null
             || databaseSelectVo.getSelectType().equals("")) {
             log.error("database-fields may not be empty");
             throw new SqlDatabaseSystemException(
                 "database-fields may not be empty");
         }
         if (databaseSelectVo.getSelectType().equals(
             Constants.DATABASE_SELECT_TYPE_SELECT)
             || databaseSelectVo.getSelectType().equals(
                 Constants.DATABASE_SELECT_TYPE_UPDATE)) {
             if (databaseSelectVo.getSelectFieldVos() == null
                 || databaseSelectVo.getSelectFieldVos().isEmpty()) {
                 log.error("select-fields may not be empty");
                 throw new SqlDatabaseSystemException(
                     "select-fields may not be empty");
             }
             for (SelectFieldVo selectFieldVo
                 : databaseSelectVo.getSelectFieldVos()) {
                 if (selectFieldVo.getFieldName() == null
                     || selectFieldVo.getFieldName().equals("")) {
                     log.error("select-fieldname may not be empty");
                     throw new SqlDatabaseSystemException(
                         "select-fieldname may not be empty");
                 }
                 if (databaseSelectVo.getTableNames().size() > 1
                     && (selectFieldVo.getTableName() == null || selectFieldVo
                         .getTableName().equals(""))) {
                     log.error("select-field-tablename may not be empty");
                     throw new SqlDatabaseSystemException(
                         "select-field-tablename may not be empty");
                 }
                 if (databaseSelectVo.getSelectType().equals(
                     Constants.DATABASE_SELECT_TYPE_UPDATE)) {
                     if (selectFieldVo.getFieldValue() == null) {
                         log.error("select-field-value may not be null");
                         throw new SqlDatabaseSystemException(
                             "select-field-value may not be null");
                     }
                     if (selectFieldVo.getFieldType() == null
                         || selectFieldVo.getFieldType().equals("")) {
                         log.error("select-field-type may not be empty");
                         throw new SqlDatabaseSystemException(
                             "select-field-type may not be empty");
                     }
                 }
             }
         }
         if (databaseSelectVo.getAdditionalWhereGroupVos() != null
             && !databaseSelectVo.getAdditionalWhereGroupVos().isEmpty()
             && databaseSelectVo.getRootWhereGroupVo() == null) {
             log.error("root where group may not be empty");
             throw new SqlDatabaseSystemException(
                 "root where group may not be empty");
         }
         if (databaseSelectVo.getRootWhereGroupVo() != null) {
             RootWhereGroupVo rootWhereGroupVo =
                 databaseSelectVo.getRootWhereGroupVo();
             if (rootWhereGroupVo.getRootWhereFieldVo() == null) {
                 log.error("root where field may not be empty");
                 throw new SqlDatabaseSystemException(
                     "root where field may not be empty");
             }
             RootWhereFieldVo rootWhereFieldVo =
                 rootWhereGroupVo.getRootWhereFieldVo();
             checkWhereFieldVo("root", rootWhereFieldVo.getFieldName(),
                 rootWhereFieldVo.getFieldType(), rootWhereFieldVo
                     .getFieldValue(), rootWhereFieldVo.getOperator(),
                 rootWhereFieldVo.getXpath());
             if (rootWhereGroupVo.getAdditionalWhereFieldVos() != null) {
                 for (AdditionalWhereFieldVo additionalWhereFieldVo
                     : rootWhereGroupVo.getAdditionalWhereFieldVos()) {
                     checkWhereFieldVo("additional", additionalWhereFieldVo
                         .getFieldName(), additionalWhereFieldVo.getFieldType(),
                         additionalWhereFieldVo.getFieldValue(),
                         additionalWhereFieldVo.getOperator(),
                         additionalWhereFieldVo.getXpath());
                 }
             }
 
         }
         if (databaseSelectVo.getAdditionalWhereGroupVos() != null) {
             for (AdditionalWhereGroupVo additionalWhereGroupVo
                 : databaseSelectVo.getAdditionalWhereGroupVos()) {
                 if (additionalWhereGroupVo.getRootWhereFieldVo() == null) {
                     log.error("root where field may not be empty");
                     throw new SqlDatabaseSystemException(
                         "root where field may not be empty");
                 }
                 RootWhereFieldVo rootWhereFieldVo =
                     additionalWhereGroupVo.getRootWhereFieldVo();
                 checkWhereFieldVo("root", rootWhereFieldVo.getFieldName(),
                     rootWhereFieldVo.getFieldType(), rootWhereFieldVo
                         .getFieldValue(), rootWhereFieldVo.getOperator(),
                     rootWhereFieldVo.getXpath());
                 if (additionalWhereGroupVo
                         .getAdditionalWhereFieldVos() != null) {
                     for (AdditionalWhereFieldVo additionalWhereFieldVo
                         : additionalWhereGroupVo.getAdditionalWhereFieldVos()) {
                         checkWhereFieldVo("additional", additionalWhereFieldVo
                             .getFieldName(), additionalWhereFieldVo
                             .getFieldType(), additionalWhereFieldVo
                             .getFieldValue(), additionalWhereFieldVo
                             .getOperator(), additionalWhereFieldVo.getXpath());
                     }
 
                 }
             }
         }
     }
 
     /**
      * checks WhereFieldVo for validity.
      * 
      * @param type
      *            type (root or additional).
      * @param fieldName
      *            fieldName.
      * @param fieldType
      *            fieldType.
      * @param fieldValue
      *            fieldValue.
      * @param operator
      *            operator.
      * @param xpath
      *            xpath.
      * @throws SqlDatabaseSystemException
      *             SqlDatabaseSystemException
      * 
      */
     private void checkWhereFieldVo(
         final String type, final String fieldName, final String fieldType,
         final String fieldValue, final String operator, final String xpath)
         throws SqlDatabaseSystemException {
         if (type == null || type.equals("")
             || (!type.equals("root") && !type.equals("additional"))) {
             log.error("wrong type given");
             throw new SqlDatabaseSystemException("wrong type given");
         }
         if (type.equals("additional")
             && (operator == null || operator.equals(""))) {
             log.error("operator may not be null");
             throw new SqlDatabaseSystemException("operator may not be null");
         }
         if (fieldType == null || fieldType.equals("")) {
             log.error("fieldtype may not be null");
             throw new SqlDatabaseSystemException("fieldtype may not be null");
         }
         if (fieldValue == null) {
             log.error("fieldvalue may not be null");
             throw new SqlDatabaseSystemException("fieldvalue may not be null");
         }
         if (!fieldType.equals(Constants.DATABASE_FIELD_TYPE_FREE_SQL)) {
             if (fieldName == null || fieldName.equals("")) {
                 log.error("fieldname may not be null");
                 throw new SqlDatabaseSystemException(
                     "fieldname may not be null");
             }
             if (operator == null || operator.equals("")) {
                 log.error("operator may not be null");
                 throw new SqlDatabaseSystemException(
                             "operator may not be null");
             }
             if (fieldType.startsWith("xpath")) {
                 if (xpath == null || xpath.equals("")) {
                     log.error("xpath may not be null");
                     throw new SqlDatabaseSystemException(
                         "xpath may not be null");
                 }
             }
         }
     }
 
     /**
      * extends tablename with schemaname and quotes.
      * 
      * @param tablename
      *            name of table
      * @return String replaced tablename
      * 
      */
     public String handleTableName(final String tablename) {
         if (!tablename.matches(".*\\..*")) {
             String extendedTablename =
                 Constants.SM_SCHEMA_NAME + "." + tablename;
             return extendedTablename;
         }
         else {
             return tablename;
         }
 
     }
 
     /**
      * checks if given fieldname is a reserved expression in the database.
      * 
      * @param fieldname
      *            name of field
      * @throws SqlDatabaseSystemException
      *             e
      * 
      */
     public void checkReservedExpressions(final String fieldname)
         throws SqlDatabaseSystemException {
         if (RESERVED_EXPRESSIONS.get(fieldname) != null) {
             throw new SqlDatabaseSystemException(fieldname
                 + " must not be used as fieldname");
         }
 
     }
 
     /**
      * Wrapper of setDataSource to enable bean stuff generation for this
      * handler.
      * 
      * @spring.property ref="escidoc-core.DataSource"
      * @param myDataSource
      *            ds
      * 
      */
     public void setMyDataSource(final DataSource myDataSource) {
         super.setDataSource(myDataSource);
     }
 
 }
