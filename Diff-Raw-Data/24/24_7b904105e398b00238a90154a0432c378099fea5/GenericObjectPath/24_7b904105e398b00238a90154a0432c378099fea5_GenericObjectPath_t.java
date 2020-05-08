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
 
 import com.flexive.core.search.cmis.impl.ResultColumnReference;
 import com.flexive.core.search.cmis.impl.CmisSqlQuery;
 import com.flexive.core.search.cmis.impl.sql.mapper.ResultColumnMapper;
 import com.flexive.core.search.cmis.impl.sql.SqlMapperFactory;
 import com.flexive.core.search.cmis.impl.sql.ColumnIndex;
 import com.flexive.core.search.cmis.impl.sql.generic.GenericSqlDialect;
 import com.flexive.core.search.cmis.model.TableReference;
 import com.flexive.core.search.PropertyEntry;
 import com.flexive.shared.structure.FxDataType;
 import com.flexive.shared.exceptions.FxSqlSearchException;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * Maps the @path PropertyEntry in a result set.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class GenericObjectPath implements ResultColumnMapper<ResultColumnReference> {
     private static final GenericObjectPath INSTANCE = new GenericObjectPath();
 
     public String selectColumn(SqlMapperFactory sqlMapperFactory, CmisSqlQuery query, ResultColumnReference column, long languageId, boolean xpath, boolean includeResultAlias, ColumnIndex index) {
         final TableReference tableRef = column.getSelectedObject().getTableReference();
         final long captionId = sqlMapperFactory.getSqlDialect().getEnvironment().getProperty("CAPTION").getId();
         index.increment();
         return "(SELECT TREE_FTEXT1024_PATHS("
                 + GenericSqlDialect.FILTER_ALIAS + "." + tableRef.getIdFilterColumn() + ","
                 + languageId + ","
                + captionId + ",false))" // TODO: LIVE/EDIT
                + (includeResultAlias ? " " + column.getResultSetAlias() : "");
     }
 
     public Object decodeResultValue(SqlMapperFactory factory, ResultSet rs, ResultColumnReference column, long languageId) throws SQLException {
         try {
             final PropertyEntry entry = column.getSelectedObject().getPropertyEntry();
             entry.setPositionInResultSet(column.getColumnStart());
             return entry.getResultValue(
                     rs, languageId, false, -1
             );
         } catch (FxSqlSearchException e) {
             throw e.asRuntimeException();
         }
     }
 
     public boolean isDirectSelectForMultivalued(SqlMapperFactory factory, ResultColumnReference column, FxDataType dataType) {
         return false;
     }
 
     public static GenericObjectPath getInstance() {
         return INSTANCE;
     }
 }
