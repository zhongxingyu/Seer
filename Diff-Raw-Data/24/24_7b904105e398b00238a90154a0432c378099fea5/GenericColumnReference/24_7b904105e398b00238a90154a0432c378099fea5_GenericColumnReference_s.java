 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.core.search.cmis.impl.sql.generic.mapper.select;
 
 import com.flexive.core.DatabaseConst;
 import com.flexive.core.search.PropertyResolver;
 import com.flexive.core.search.DataSelector;
 import com.flexive.core.search.PropertyEntry;
 import com.flexive.core.search.cmis.impl.CmisSqlQuery;
 import com.flexive.core.search.cmis.impl.ResultColumnReference;
 import com.flexive.core.search.cmis.impl.sql.ColumnIndex;
 import com.flexive.core.search.cmis.impl.sql.SqlDialect;
 import com.flexive.core.search.cmis.impl.sql.SqlMapperFactory;
 import static com.flexive.core.search.cmis.impl.sql.generic.GenericSqlDialect.FILTER_ALIAS;
 import com.flexive.core.search.cmis.impl.sql.mapper.ConditionColumnMapper;
 import com.flexive.core.search.cmis.impl.sql.mapper.ResultColumnMapper;
 import com.flexive.core.search.cmis.model.ColumnReference;
 import com.flexive.core.search.cmis.model.TableReference;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxSqlSearchException;
 import com.flexive.shared.structure.FxDataType;
 import com.flexive.shared.structure.FxFlatStorageMapping;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.value.FxNoAccess;
 import com.flexive.shared.value.FxValue;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Select and condition column mapper for column references (= assignments of instances).
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  * @since 3.1
  */
 public class GenericColumnReference implements ResultColumnMapper<ResultColumnReference>, ConditionColumnMapper<ColumnReference> {
     private static final Log LOG = LogFactory.getLog(GenericColumnReference.class);
     private static final String SUBSEL_ALIAS = "sub";
     private static final GenericColumnReference INSTANCE = new GenericColumnReference();
 
     /**
      * {@inheritDoc}
      */
     public String selectColumn(SqlMapperFactory sqlMapperFactory, CmisSqlQuery query, ResultColumnReference column, long languageId, boolean xpath, boolean includeResultAlias, ColumnIndex index) {
         // this is a modified version of GenericSQLDataSelector#getContentDataSubSelect
 
         // select data fields
         final PropertyEntry entry = column.getPropertyEntry();
         if (entry == null) {
             return "null";  // don't select anything
         }
         final FxDataType dataType = entry.getProperty() == null ? null : entry.getProperty().getDataType();
         final String[] readColumns = entry.getReadColumns();
         final List<String> columns = new ArrayList<String>(readColumns.length);
         int ctr = 0;
         if (column.getSelectedObject().isMultivalued() && !isDirectSelectForMultivalued(sqlMapperFactory, column, dataType)) {
             // multivalued select, but database does not support direct select - select only ID and version
             final TableReference table = column.getSelectedObject().getTableReference();
             columns.add(FILTER_ALIAS + "." + table.getIdFilterColumn());
             columns.add(FILTER_ALIAS + "." + table.getVersionFilterColumn());
             index.increment(2);
         } else {
             // select all read columns
             for (String readColumn : readColumns) {
                 columns.add(
                         getSelect(sqlMapperFactory.getSqlDialect(), readColumn,
                                 column.getSelectedObject(),
                                 column.getSelectedObject().getReferencedAssignments(), languageId)
                                 + (includeResultAlias  && dataType != FxDataType.Binary ?
                                 // add unique suffix if more than one column is selected,
                                 // but first column is exposed as the "official" value (for sorting)
                                 // (don't add the alias for binaries yet, because the select will be wrapped)
                                 " " + column.getResultSetAlias()
                                         + (readColumns.length > 1 && ctr++ > 0 ? "_" + ctr : "")
 
                                 : ""    // skip alias
                         )
 
                 );
                 index.increment();
             }
             // type uses property permissions, we have to select the XPath
             if (sqlMapperFactory.getSqlDialect().isPropertyPermissionsEnabled(column)
                     && PropertyResolver.Table.T_CONTENT_DATA.equals(entry.getTableType())) {
                 columns.add(
                         getSelect(sqlMapperFactory.getSqlDialect(), "xpath",
                                 column.getSelectedObject(),
                                 column.getSelectedObject().getReferencedAssignments(),
                                 languageId)
                 );
                 index.increment();
             }
         }
         // select XPath
         // currently does not return a xpath prefix, because one row may match more than one content instance
         //columns.add("concat(filter.xpathPref," + getContentDataSelect("XPATHMULT", null, assignment, languageId, true) + ")");
 
         // disabled because we don't gain much by returning xpaths to CMIS queries
         //columns.add(getContentDataSelect("XPATHMULT", null, assignment, languageId, true));
         //index.increment();
 
         String select = StringUtils.join(columns, ",\n");
 
         // use base assignment for datatype checks
         final FxPropertyAssignment assignment = column.getSelectedObject().getBaseAssignment();
 
         if (!xpath && assignment != null && assignment.getProperty().getDataType() == FxDataType.Binary) {
             // select string-coded form of the BLOB properties
             select = DataSelector.selectBinary(columns.get(0))
                     + (includeResultAlias ? " " + column.getResultSetAlias() : "");
         }
         return select;
 
     }
 
     protected String getSelect(SqlDialect dialect, String readColumn, ColumnReference column, List<FxPropertyAssignment> assignments, long languageId) {
         switch (column.getPropertyEntry().getTableType()) {
             case T_CONTENT:
                 return getContentSelect(readColumn, column);
             case T_CONTENT_DATA:
                 return getContentDataSelect(readColumn, column, assignments, languageId);
             case T_CONTENT_DATA_FLAT:
                 return getContentDataFlatSelect(dialect, column, assignments, languageId);
             default:
                 throw new IllegalArgumentException("Invalid table type: " + column.getPropertyEntry().getTableType());
         }
     }
 
     protected String getContentSelect(String readColumn, ColumnReference column) {
         if ("id".equalsIgnoreCase(readColumn)) {
             return FILTER_ALIAS + "." + column.getTableReference().getIdFilterColumn();
         } else if ("ver".equalsIgnoreCase(readColumn)) {
             return FILTER_ALIAS + "." + column.getTableReference().getVersionFilterColumn();
         }
         return "(" + selectUsingTableFilter(readColumn, column, DatabaseConst.TBL_CONTENT) + ")";
 
     }
 
     protected String getContentDataSelect(String readColumn, ColumnReference column, List<FxPropertyAssignment> assignments, long languageId) {
         return "(" + selectUsingTableFilter(readColumn, column, DatabaseConst.TBL_CONTENT_DATA) + " AND " +
                 "ASSIGN IN (" + StringUtils.join(FxSharedUtils.getSelectableObjectIdList(assignments), ',') + ")" +
                 " AND " + "(" + SUBSEL_ALIAS + ".lang=" + languageId +
                 " OR " + SUBSEL_ALIAS + ".ismldef=true)" +
                 // fetch exact language match before default
                 " ORDER BY " + SUBSEL_ALIAS + ".ismldef " +
                 " LIMIT 1)";
     }
 
     protected String getContentDataFlatSelect(SqlDialect dialect, ColumnReference column, List<FxPropertyAssignment> assignments, long languageId) {
         // the mapping information must be the same for all derived assignments
         final FxPropertyAssignment assignment = assignments.get(0);
         final FxFlatStorageMapping mapping = assignment.getFlatStorageMapping();
         return "(" + selectUsingTableFilter(mapping.getColumn(), column, mapping.getStorage()) +
                 " AND " + dialect.getAssignmentFilter(column.getFilterTableType(), "", column.getReferencedAssignments()) +
                 (assignment.isMultiLang()
                         ? " AND " + mapping.getColumn() + " IS NOT NULL"
                                             + " AND (lang=" + languageId
                                             + " OR " + mapping.getColumn() + "_mld=true)"
                                             + " ORDER BY " + mapping.getColumn() + "_mld"
                 : "") +
                 " LIMIT 1)";
     }
 
     protected String selectUsingTableFilter(String readColumn, ColumnReference column, String tableName) {
         final String select;
         if (column.isMultivalued()) {
             select = "SELECT " + getMultivaluedConcatFunction(SUBSEL_ALIAS + "." + readColumn);
         } else {
             select = "SELECT " + SUBSEL_ALIAS + "." + readColumn;
         }
         return select +
                 " FROM " + tableName + " " + SUBSEL_ALIAS +
                 " WHERE " +
                 SUBSEL_ALIAS + ".id=" +
                 FILTER_ALIAS + "." + column.getTableReference().getIdFilterColumn() + " AND " +
                 SUBSEL_ALIAS + ".ver=" +
                 FILTER_ALIAS + "." + column.getTableReference().getVersionFilterColumn() + " ";
     }
 
 
     /**
      * {@inheritDoc}
      */
     public Object decodeResultValue(SqlMapperFactory factory, ResultSet rs, ResultColumnReference column, long languageId) {
         if (column.getPropertyEntry() == null) {
             return null;    // null selected
         }
         column.getPropertyEntry().setPositionInResultSet(column.getColumnStart());
         try {
             if (column.getSelectedObject().isMultivalued()) {
                 if (isDirectSelectForMultivalued(factory, column, column.getPropertyEntry().getProperty().getDataType())) {
                     final String result = rs.getString(column.getColumnStart());
                     return decodeMultivaluedResult(
                             factory,
                             column.getPropertyEntry().getProperty().getDataType(),
                             result
                     );
                 } else {
                     // return reference, which will then be used by postprocessing to fill out the multivalued property
                     assert column.getColumnEnd() == column.getColumnStart() + 1;
                     return new FxPK(
                             rs.getLong(column.getColumnStart()),
                             rs.getInt(column.getColumnStart() + 1)
                     );
                 }
             } else {
                 final SqlDialect sqlDialect = factory.getSqlDialect();
                 final boolean propertyPerms = sqlDialect.isPropertyPermissionsEnabled(column);
                 Object value = column.getPropertyEntry().getResultValue(
                         rs,
                         languageId,
                         propertyPerms,
                         // get type ID from result set (only when property permissions are enabled or FxValues should be returned)
                         propertyPerms || !sqlDialect.isReturnPrimitives() ?
                             rs.getLong(
                                     sqlDialect.getTypeIdColumn(column.getSelectedObject().getTableReference()).getColumnStart()
                             )
                         : -1
                 );
                 if (propertyPerms) {
                     if (!(value instanceof FxValue)) {
                         throw new UnsupportedOperationException("Property permissions can only be set on structure " +
                                 "properties (column: " + column.getAlias() + ")");
                     }
                     // check permissions by selected XPath 
                     final FxValue fxValue = (FxValue) value;
                     if (!fxValue.isEmpty() && !sqlDialect.isAllowedByPropertyPermissions(rs, column, fxValue.getXPath())) {
                         value = new FxNoAccess(FxLanguage.SYSTEM_ID, false);
                     }
                 }
                 return (sqlDialect.isReturnPrimitives() && value instanceof FxValue && !(value instanceof FxNoAccess))
                         ? ((FxValue) value).getBestTranslation()
                         : value;
             }
         } catch (FxSqlSearchException e) {
             throw e.asRuntimeException();
         } catch (SQLException e) {
             throw new FxSqlSearchException(LOG, e).asRuntimeException();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public String getConditionColumn(SqlMapperFactory sqlMapperFactory, ColumnReference expression, String tableAlias) {
         return (tableAlias != null ? tableAlias + "." : "")
                 + expression.getPropertyEntry().getFilterColumn();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isDirectSelectForMultivalued(SqlMapperFactory factory, ResultColumnReference column, FxDataType dataType) {
         return false;
     }
 
     protected String getMultivaluedConcatFunction(String column) {
         throw new UnsupportedOperationException("Direct SELECT for multivalued properties not supported by generic SQL dialect.");
     }
 
     private List decodeMultivaluedResult(SqlMapperFactory factory, FxDataType dataType, String encodedValues) {
         final List<String> values = splitMultivaluedResult(encodedValues);
         final List<Object> result = new ArrayList<Object>(values.size());
         for (String value : values) {
             result.add(decodeMultivaluedRowValue(factory, dataType, value));
         }
         return result;
     }
 
     protected Object decodeMultivaluedRowValue(SqlMapperFactory factory, FxDataType dataType, String encodedValue) {
         throw new UnsupportedOperationException("SELECT for multivalued properties not supported by generic SQL dialect.");
     }
 
     protected List<String> splitMultivaluedResult(String result) {
         throw new UnsupportedOperationException("SELECT for multivalued properties not supported by generic SQL dialect.");
     }
 
     public static GenericColumnReference getInstance() {
         return INSTANCE;
     }
 }
