 /*
  * Copyright (C) 2010, 2011 Openismus GmbH
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
 
 package org.glom.web.client;
 
 import java.util.ArrayList;
 
 import org.glom.web.shared.DataItem;
 import org.glom.web.shared.DetailsLayoutAndData;
 import org.glom.web.shared.DocumentInfo;
 import org.glom.web.shared.Documents;
 import org.glom.web.shared.NavigationRecord;
 import org.glom.web.shared.TypedDataItem;
 import org.glom.web.shared.layout.LayoutGroup;
 
 import com.google.gwt.user.client.rpc.RemoteService;
 import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
 
 @RemoteServiceRelativePath("libGlom")
 public interface OnlineGlomService extends RemoteService {
 
 	DocumentInfo getDocumentInfo(String documentID);
 
 	/**
 	 * Gets a {@link LayoutGroup} for the given Glom document and table name.
 	 * 
 	 * @param documentID
 	 *            identifier for the Glom document
 	 * @param tableName
 	 *            name of the table in the Glom document or an empty {@link String} ("") to get the layout for the
 	 *            default table
 	 * @return filled in {@link LayoutGroup}
 	 */
 	LayoutGroup getListViewLayout(String documentID, String tableName);
 
 	/**
 	 * Retrieves data for a list view table.
 	 * 
 	 * @param documentID
 	 *            identifier for the Glom document
 	 * @param tableName
 	 *            name of the table in the Glom document or an empty {@link String} ("") to get the layout for the
 	 *            default table
 	 * @param start
 	 *            the start index in the data result set from the SQL query that should be retrieved
 	 * @param length
 	 *            the number of rows of data to retrieve
 	 * @return an {@link ArrayList} of {@link DataItem} arrays that represents the requested data
 	 */
 	ArrayList<DataItem[]> getListViewData(String documentID, String tableName, int start, int length);
 
 	/**
 	 * Retrieves sorted data for a list view table.
 	 * 
 	 * @param documentID
 	 *            identifier for the Glom document
 	 * @param tableName
 	 *            name of the table in the Glom document or an empty {@link String} ("") to get the layout for the
 	 *            default table
 	 * @param start
 	 *            the start index in the data result set from the SQL query that should be retrieved
 	 * @param length
 	 *            the number of rows of data to retrieve
 	 * @param sortColumnIndex
 	 *            the index of the column to sort
 	 * @param ascending
 	 *            <code>true</code> if the column should be sorted in ascending order, <code>false</code> if the column
 	 *            should be sorted in descending order
 	 * @return an {@link ArrayList} of {@link DataItem} arrays that represents the requested data
 	 */
 	ArrayList<DataItem[]> getSortedListViewData(String documentID, String tableName, int start, int length,
 			int sortColumnIndex, boolean isAscending);
 
 	/**
 	 * Gets a list of Glom documents found in the configured directory.
 	 * 
 	 * @return an {@link ArrayList<String>} of Glom document titles. If the list is empty, no glom documents were found
 	 *         in the configured directory because it's empty or the directory is not configured correctly.
 	 */
 	Documents getDocuments();
 
 	/**
 	 * Checks if the PostgreSQL authentication has been set for this document.
 	 * 
 	 * @param documentID
 	 *            identifier for the Glom document
 	 * @return true if the authentication has been set, false if it hasn't
 	 */
 	boolean isAuthenticated(String documentID);
 
 	/**
 	 * Checks if the provided PostgreSQL username and password are correct for the specified glom document. If the
 	 * information is correct it is saved for future access.
 	 * 
 	 * @param documentID
 	 *            identifier for the Glom document
 	 * @param username
 	 *            the PostgreSQL username
 	 * @param password
 	 *            the POstgreSQL password
 	 * @return true if username and password are correct, false otherwise
 	 */
 	boolean checkAuthentication(String documentID, String username, String password);
 
 	/**
 	 * Gets data for the details view.
 	 * 
	 * Note: This method is not currently being used. It will be useful when navigation through the details view is
	 * added by creating a data provider for the details view.
	 * 
	 * TODO: Remove above comment when this method is being used.
	 * 
 	 * @param documentID
 	 *            identifier for the Glom document
 	 * @param tableName
 	 *            name of the table in the Glom document or an empty {@link String} ("") to get the layout for the
 	 *            default table
 	 * @param primaryKeyValue
 	 *            value of the primary key in the specified Glom table to use in the query
 	 * @return the result of the SQL query as an array of {@link DataItem}s
 	 */
 	DataItem[] getDetailsData(String documentID, String tableName, TypedDataItem primaryKeyValue);
 
 	/**
 	 * Gets a {@link DetailsLayoutAndData} object that contains the layout and data of the details view.
 	 * 
 	 * @param documentID
 	 *            identifier for the Glom document
 	 * @param tableName
 	 *            name of the table in the Glom document or an empty {@link String} ("") to get the layout for the
 	 *            default table
 	 * @param primaryKeyValue
 	 *            value of the primary key in the specified Glom table to use in the query
 	 * @return a {@link DetailsLayoutAndData} object for the layout and initial data of the details view.
 	 */
 	DetailsLayoutAndData getDetailsLayoutAndData(String documentID, String tableName, TypedDataItem primaryKeyValue);
 
 	/**
 	 * Retrieves data for the related list table with the specified relationship name and foreign key value.
 	 * 
 	 * @param documentID
 	 *            identifier for the Glom document
 	 * @param tableName
 	 *            name of the table in the Glom document or an empty {@link String} ("") to get the layout for the
 	 *            default table
 	 * @param relationshipName
 	 *            name of the relationship to use for setting up the SQL query
 	 * @param start
 	 *            the start index in the data result set from the SQL query that should be retrieved
 	 * @param length
 	 *            the number of rows of data to retrieve
 	 * @param foreignKeyValue
 	 *            the value of the foreign key
 	 * @return an {@link ArrayList} of {@link DataItem} arrays that represents the requested data
 	 */
 	ArrayList<DataItem[]> getRelatedListData(String documentID, String tableName, String relationshipName,
 			TypedDataItem foreignKeyValue, int start, int length);
 
 	/**
 	 * Retrieves sorted data for the related list table with the specified relationship name and foreign key value.
 	 * 
 	 * @param documentID
 	 *            identifier for the Glom document
 	 * @param tableName
 	 *            name of the table in the Glom document or an empty {@link String} ("") to get the layout for the
 	 *            default table
 	 * @param relationshipName
 	 *            name of the relationship to use for setting up the SQL query
 	 * @param foreignKeyValue
 	 *            the value of the foreign key
 	 * @param start
 	 *            the start index in the data result set from the SQL query that should be retrieved
 	 * @param length
 	 *            the number of rows of data to retrieve
 	 * @param sortColumnIndex
 	 *            the index of the column to sort
 	 * @param ascending
 	 *            <code>true</code> if the column should be sorted in ascending order, <code>false</code> if the column
 	 *            should be sorted in descending order
 	 * @return an {@link ArrayList} of {@link DataItem} arrays that represents the requested data
 	 */
 	ArrayList<DataItem[]> getSortedRelatedListData(String documentID, String tableName, String relationshipName,
 			TypedDataItem foreignKeyValue, int start, int length, int sortColumnIndex, boolean ascending);
 
 	/**
 	 * Gets the expected row count for the related list table with the specified relationship name and foreign key
 	 * value.
 	 * 
 	 * @param documentID
 	 *            identifier for the Glom document
 	 * @param tableName
 	 *            name of the table in the Glom document or an empty {@link String} ("") to get the layout for the
 	 *            default table
 	 * @param relationshipName
 	 *            name of the relationship to use for setting up the SQL query
 	 * @param foreignKeyValue
 	 *            the value of the foreign key
 	 * @return the expected row count
 	 */
 	int getRelatedListRowCount(String documentID, String tableName, String relationshipName,
 			TypedDataItem foreignKeyValue);
 
 	NavigationRecord getSuitableRecordToViewDetails(String documentID, String tableName, String relationshipName,
 			TypedDataItem primaryKeyValue);
 
 	String getConfigurationErrorMessage();
 
 }
