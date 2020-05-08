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
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import org.glom.libglom.Document;
 import org.glom.libglom.Field;
 import org.glom.libglom.Glom;
 import org.glom.libglom.LayoutFieldVector;
 import org.glom.libglom.SqlBuilder;
 import org.glom.libglom.Value;
 import org.glom.web.server.Log;
 import org.glom.web.server.Utils;
 import org.glom.web.shared.DataItem;
 import org.glom.web.shared.TypedDataItem;
 
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 
 /**
  * @author Ben Konrath <ben@bagu.org>
  * 
  */
 public class DetailsDBAccess extends DBAccess {
 
 	public DetailsDBAccess(Document document, String documentID, ComboPooledDataSource cpds, String tableName) {
 		super(document, documentID, cpds, tableName);
 		this.tableName = tableName;
 	}
 
 	public DataItem[] getData(TypedDataItem primaryKeyValue) {
 
 		LayoutFieldVector fieldsToGet = getFieldsToShowForSQLQuery(document
 				.get_data_layout_groups("details", tableName));
 
 		if (fieldsToGet == null || fieldsToGet.size() <= 0) {
 			Log.warn(documentID, tableName, "Didn't find any fields to show. Returning null.");
 			return null;
 		}
 
 		Field primaryKey = getPrimaryKeyField();
 
 		if (primaryKey == null) {
 			Log.error(documentID, tableName, "Couldn't find primary key in table. Returning null.");
 			return null;
 		}
 
 		ArrayList<DataItem[]> rowsList = new ArrayList<DataItem[]>();
 		Connection conn = null;
 		Statement st = null;
 		ResultSet rs = null;
		Value gdaPrimaryKeyValue = null;
 		try {
 			// Setup the JDBC driver and get the query.
 			conn = cpds.getConnection();
 			st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
 
			gdaPrimaryKeyValue = Utils.getGlomTypeGdaValueForTypedDataItem(documentID, tableName,
 					primaryKey.get_glom_type(), primaryKeyValue);
 
			// Only create the query if we've created a Gda Value from the TypedDataItem.
 			if (gdaPrimaryKeyValue != null) {
 
 				SqlBuilder builder = Glom.build_sql_select_with_key(tableName, fieldsToGet, primaryKey,
 						gdaPrimaryKeyValue);
 				String query = Glom.sqlbuilder_get_full_query(builder);
 
 				rs = st.executeQuery(query);
 
 				// get the results from the ResultSet
 				// using 2 as a length parameter so we can log a warning if appropriate
 				rowsList = convertResultSetToDTO(2, fieldsToGet, rs);
 			}
 
 		} catch (SQLException e) {
 			Log.error(documentID, tableName, "Error executing database query.", e);
 			// TODO: somehow notify user of problem
 			return null;
 		} finally {
 			// cleanup everything that has been used
 			try {
 				if (rs != null)
 					rs.close();
 				if (st != null)
 					st.close();
 				if (conn != null)
 					conn.close();
 			} catch (Exception e) {
 				Log.error(documentID, tableName,
 						"Error closing database resources. Subsequent database queries may not work.", e);
 			}
 		}
 
 		if (rowsList.size() == 0) {
 			Log.error(documentID, tableName, "The query returned an empty ResultSet. Returning null.");
 			return null;
		} else if (rowsList.size() > 1 && !gdaPrimaryKeyValue.is_null()) {
			// Only log a warning if the result size is greater than 1 and the gdaPrimaryKeyValue is not null. When
			// gdaPrimaryKeyValue.is_null() is true, the default query for the details view is being executed so we
			// expect a result set that is larger than one.
 			Log.warn(documentID, tableName,
 					"The query did not return the expected unique result. Returning the first result in the set.");
 		}
 
 		return rowsList.get(0);
 	}
 }
