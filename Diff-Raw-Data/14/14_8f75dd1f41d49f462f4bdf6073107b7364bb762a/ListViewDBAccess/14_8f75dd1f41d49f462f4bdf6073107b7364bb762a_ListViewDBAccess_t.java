 /*
  * Copyright (C) 2011 Openismus GmbH
  *
  * This file is part of GWT-Glom.
  *
  * GWT-Glom is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.glom.web.server.database;
 
 import java.util.ArrayList;
 
import org.apache.commons.lang.StringUtils;
 import org.glom.libglom.Document;
 import org.glom.libglom.Field;
 import org.glom.libglom.Glom;
 import org.glom.libglom.LayoutGroupVector;
 import org.glom.libglom.LayoutItem_Field;
 import org.glom.libglom.Relationship;
 import org.glom.libglom.SortClause;
 import org.glom.libglom.SqlBuilder;
 import org.glom.libglom.SqlExpr;
 import org.glom.libglom.Value;
 import org.glom.web.shared.DataItem;
 
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 
 /**
  *
  */
 public class ListViewDBAccess extends ListDBAccess {
 
 	public ListViewDBAccess(final Document document, final String documentID, final ComboPooledDataSource cpds,
 			final String tableName, final org.glom.libglom.LayoutGroup libglomLayoutGroup) {
 		super(document, documentID, cpds, tableName);
 
 		// Convert the libglom LayoutGroup object into a LayoutFieldVector suitable for SQL queries.
 		final LayoutGroupVector tempLayoutGroupVec = new LayoutGroupVector();
 		tempLayoutGroupVec.add(libglomLayoutGroup);
 		fieldsToGet = getFieldsToShowForSQLQuery(tempLayoutGroupVec);
 
 		// Add a LayoutItem_Field for the primary key to the end of the LayoutFieldVector if it doesn't already contain
 		// a primary key.
 		if (getPrimaryKeyIndex() < 0) {
 			fieldsToGet.add(getPrimaryKeyLayoutItemField(tableName));
 		}
 	}
 
 	public ArrayList<DataItem[]> getData(final String quickFind, final int start, final int length,
 			final boolean useSortClause, final int sortColumnIndex, final boolean isAscending) {
 
 		return getListData(quickFind, start, length, useSortClause, sortColumnIndex, isAscending);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.server.ListDBAccess#getExpectedResultSize()
 	 */
 	public int getExpectedResultSize() {
 
 		if (fieldsToGet == null || fieldsToGet.size() <= 0)
 			return -1;
 
 		return getResultSizeOfSQLQuery();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.server.ListDBAccess#getSQLQuery(org.glom.libglom.LayoutFieldVector,
 	 * org.glom.libglom.SortClause)
 	 */
 	@Override
 	protected String getSelectQuery(final String quickFind, final SortClause sortClause) {
		// Later versions of libglom actually return an empty SqlExpr when quickFindValue is empty,
		// but let's be sure:
		SqlExpr whereClause;
		if (StringUtils.isEmpty(quickFind)) {
			whereClause = new SqlExpr();
		} else {
			final Value quickFindValue = new Value(quickFind);
			whereClause = Glom.get_find_where_clause_quick(document, tableName, quickFindValue);
		}

 		final Relationship extraJoin = new Relationship(); // Ignored.
 		final SqlBuilder builder = Glom.build_sql_select_with_where_clause(tableName, fieldsToGet, whereClause,
 				extraJoin, sortClause);
 		return Glom.sqlbuilder_get_full_query(builder);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.server.ListDBAccess#getCountQuery(org.glom.libglom.LayoutFieldVector)
 	 */
 	@Override
 	protected String getCountQuery() {
 		final SqlBuilder builder = Glom.build_sql_select_with_where_clause(tableName, fieldsToGet);
 		final SqlBuilder countBuilder = Glom.build_sql_select_count_rows(builder);
 		return Glom.sqlbuilder_get_full_query(countBuilder);
 	}
 
 	/**
 	 * Gets the primary key index of this list layout.
 	 * 
 	 * @return index of primary key or -1 if a primary key was not found
 	 */
 	private int getPrimaryKeyIndex() {
 		for (int i = 0; i < fieldsToGet.size(); i++) {
 			final LayoutItem_Field layoutItemField = fieldsToGet.get(i);
 			final Field field = layoutItemField.get_full_field_details();
 			if (tableName.equals(layoutItemField.get_table_used(tableName)) && field != null && field.get_primary_key())
 				return i;
 		}
 		return -1;
 	}
 }
