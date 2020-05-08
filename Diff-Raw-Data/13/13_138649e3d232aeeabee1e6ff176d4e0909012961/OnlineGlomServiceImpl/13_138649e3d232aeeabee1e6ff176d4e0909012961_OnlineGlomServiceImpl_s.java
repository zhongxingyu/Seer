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
 
 package org.glom.web.server;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Time;
 import java.text.DateFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Currency;
 import java.util.Hashtable;
 import java.util.Locale;
 import java.util.Properties;
 
 import org.glom.libglom.BakeryDocument.LoadFailureCodes;
 import org.glom.libglom.Document;
 import org.glom.libglom.Field;
 import org.glom.libglom.FieldFormatting;
 import org.glom.libglom.FieldVector;
 import org.glom.libglom.Glom;
 import org.glom.libglom.LayoutFieldVector;
 import org.glom.libglom.LayoutGroupVector;
 import org.glom.libglom.LayoutItem;
 import org.glom.libglom.LayoutItemVector;
 import org.glom.libglom.LayoutItem_Field;
 import org.glom.libglom.LayoutItem_Portal;
 import org.glom.libglom.NumericFormat;
 import org.glom.libglom.SortClause;
 import org.glom.libglom.SortFieldPair;
 import org.glom.libglom.StringVector;
 import org.glom.web.client.OnlineGlomService;
 import org.glom.web.shared.Documents;
 import org.glom.web.shared.GlomDocument;
 import org.glom.web.shared.GlomField;
 import org.glom.web.shared.layout.Formatting;
 import org.glom.web.shared.layout.LayoutGroup;
 import org.glom.web.shared.layout.LayoutItemField;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 import com.mchange.v2.c3p0.DataSources;
 
 /**
  * The servlet for retrieving layout information from libglom and data from the underlying PostgreSQL database.
  * 
  * TODO: move methods that that require a glom document object to the ConfiguredDocument class.
  * 
  * @author Ben Konrath <ben@bagu.org>
  */
 @SuppressWarnings("serial")
 public class OnlineGlomServiceImpl extends RemoteServiceServlet implements OnlineGlomService {
 
 	// convenience class to for dealing with the Online Glom configuration file
 	private class OnlineGlomProperties extends Properties {
 		public String getKey(String value) {
 			for (String key : stringPropertyNames()) {
 				if (getProperty(key).trim().equals(value))
 					return key;
 			}
 			return null;
 		}
 	}
 
 	private final Hashtable<String, ConfiguredDocument> documentMapping = new Hashtable<String, ConfiguredDocument>();
 	// TODO implement locale
 	private final Locale locale = Locale.ROOT;
 
 	/*
 	 * This is called when the servlet is started or restarted.
 	 */
 	public OnlineGlomServiceImpl() throws Exception {
 
 		// Find the configuration file. See this thread for background info:
 		// http://stackoverflow.com/questions/2161054/where-to-place-properties-files-in-a-jsp-servlet-web-application
 		OnlineGlomProperties config = new OnlineGlomProperties();
 		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("onlineglom.properties");
 		if (is == null) {
 			Log.fatal("onlineglom.properties not found.");
 			throw new IOException();
 		}
 		config.load(is);
 
 		// check if we can read the configured glom file directory
 		String documentDirName = config.getProperty("glom.document.directory");
 		File documentDir = new File(documentDirName);
 		if (!documentDir.isDirectory()) {
 			Log.fatal(documentDirName + " is not a directory.");
 			throw new IOException();
 		}
 		if (!documentDir.canRead()) {
 			Log.fatal("Can't read the files in : " + documentDirName);
 			throw new IOException();
 		}
 
 		// get and check the glom files in the specified directory
 		final String glomFileExtension = ".glom";
 		File[] glomFiles = documentDir.listFiles(new FilenameFilter() {
 			@Override
 			public boolean accept(File dir, String name) {
 				return name.endsWith(glomFileExtension);
 			}
 		});
 		Glom.libglom_init();
 		for (File glomFile : glomFiles) {
 			Document document = new Document();
 			document.set_file_uri("file://" + glomFile.getAbsolutePath());
 			int error = 0;
 			boolean retval = document.load(error);
 			if (retval == false) {
 				String message;
 				if (LoadFailureCodes.LOAD_FAILURE_CODE_NOT_FOUND == LoadFailureCodes.swigToEnum(error)) {
 					message = "Could not find file: " + glomFile.getAbsolutePath();
 				} else {
 					message = "An unknown error occurred when trying to load file: " + glomFile.getAbsolutePath();
 				}
 				Log.error(message);
 				// continue with for loop because there may be other documents in the directory
 				continue;
 			}
 
 			ConfiguredDocument configuredDocument = new ConfiguredDocument(document);
 			// check if a username and password have been set and work for the current document
 			String filename = glomFile.getName();
 			String key = config.getKey(filename);
 			if (key != null) {
 				String[] keyArray = key.split("\\.");
 				if (keyArray.length == 3 && "filename".equals(keyArray[2])) {
 					// username/password could be set, let's check to see if it works
 					String usernameKey = key.replaceAll(keyArray[2], "username");
 					String passwordKey = key.replaceAll(keyArray[2], "password");
 					configuredDocument.setUsernameAndPassword(config.getProperty(usernameKey),
 							config.getProperty(passwordKey));
 				}
 			}
 
 			// check the if the global username and password have been set and work with this document
 			if (!configuredDocument.isAuthenticated()) {
 				configuredDocument.setUsernameAndPassword(config.getProperty("glom.document.username"),
 						config.getProperty("glom.document.password"));
 			}
 
 			// The key for the hash table is the file name without the .glom extension and with spaces ( ) replaced with
 			// pluses (+). The space/plus replacement makes the key more friendly for URLs.
 			String hashKey = filename.substring(0, glomFile.getName().length() - glomFileExtension.length()).replace(
 					' ', '+');
 			documentMapping.put(hashKey, configuredDocument);
 
 		}
 	}
 
 	/*
 	 * This is called when the servlet is stopped or restarted.
 	 * 
 	 * @see javax.servlet.GenericServlet#destroy()
 	 */
 	@Override
 	public void destroy() {
 		Glom.libglom_deinit();
 
 		for (String documenTitle : documentMapping.keySet()) {
 			ConfiguredDocument configuredDoc = documentMapping.get(documenTitle);
 			try {
 				DataSources.destroy(configuredDoc.getCpds());
 			} catch (SQLException e) {
 				Log.error(documenTitle, "Error cleaning up the ComboPooledDataSource.", e);
 			}
 		}
 
 	}
 
 	public GlomDocument getGlomDocument(String documentID) {
 
 		Document document = documentMapping.get(documentID).getDocument();
 		GlomDocument glomDocument = new GlomDocument();
 
 		// get arrays of table names and titles, and find the default table index
 		StringVector tablesVec = document.get_table_names();
 
 		int numTables = safeLongToInt(tablesVec.size());
 		// we don't know how many tables will be hidden so we'll use half of the number of tables for the default size
 		// of the ArrayList
 		ArrayList<String> tableNames = new ArrayList<String>(numTables / 2);
 		ArrayList<String> tableTitles = new ArrayList<String>(numTables / 2);
 		boolean foundDefaultTable = false;
 		int visibleIndex = 0;
 		for (int i = 0; i < numTables; i++) {
 			String tableName = tablesVec.get(i);
 			if (!document.get_table_is_hidden(tableName)) {
 				tableNames.add(tableName);
 				// JNI is "expensive", the comparison will only be called if we haven't already found the default table
 				if (!foundDefaultTable && tableName.equals(document.get_default_table())) {
 					glomDocument.setDefaultTableIndex(visibleIndex);
 					foundDefaultTable = true;
 				}
 				tableTitles.add(document.get_table_title(tableName));
 				visibleIndex++;
 			}
 		}
 
 		// set everything we need
 		glomDocument.setTableNames(tableNames);
 		glomDocument.setTableTitles(tableTitles);
 
 		return glomDocument;
 	}
 
 	public LayoutGroup getListLayout(String documentID, String tableName) {
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		Document document = configuredDoc.getDocument();
 
 		// Use the default table if the table name hasn't been set or if the table name isn't in the glom document.
 		// The last check guards against SQL injection attacks since the table name could come from the URL.
 		if (tableName == null || tableName.isEmpty() || !document.get_table_is_known(tableName)) {
 			tableName = document.get_default_table();
 		}
 
 		// access the layout list
 		LayoutGroupVector layoutGroupVec = document.get_data_layout_groups("list", tableName);
 		int listViewLayoutGroupSize = safeLongToInt(layoutGroupVec.size());
 		org.glom.libglom.LayoutGroup libglomLayoutGroup = null;
 		if (listViewLayoutGroupSize > 0) {
 			// a list layout group is defined; we can use the first group as the list
 			if (listViewLayoutGroupSize > 1)
 				Log.warn(documentID, tableName,
 						"The size of the list layout group is greater than 1. Attempting to use the first item for the layout list view.");
 
 			libglomLayoutGroup = layoutGroupVec.get(0);
 		} else {
 			// a list layout group is *not* defined; we are going make a libglom layout group from the list of fields
 			Log.info(documentID, tableName,
 					"A list layout is not defined for this table. Displaying a list layout based on the field list.");
 
 			FieldVector fieldsVec = document.get_table_fields(tableName);
 			libglomLayoutGroup = new org.glom.libglom.LayoutGroup();
 			for (int i = 0; i < fieldsVec.size(); i++) {
 				Field field = fieldsVec.get(i);
 				LayoutItem_Field layoutItemField = new LayoutItem_Field();
 				layoutItemField.set_full_field_details(field);
 				libglomLayoutGroup.add_item(layoutItemField);
 			}
 		}
 
 		// confirm the libglom LayoutGroup is not null as per the getLayoutGroup method precondition
 		if (libglomLayoutGroup == null) {
 			Log.error(documentID, tableName, "A LayoutGroup was not found. Returning null.");
 			return null;
 		}
 
 		LayoutGroup layoutGroup = getLayoutGroup(documentID, tableName, libglomLayoutGroup);
 
 		// use the same fields list as will be used for the query
 		LayoutFieldVector fieldsToGet = getFieldsToShowForSQLQuery(document, tableName, "list");
 		layoutGroup.setExpectedResultSize(getResultSizeOfSQLQuery(documentID, tableName, fieldsToGet));
 
 		// Set the primary key index for the table
 		int primaryKeyIndex = getPrimaryKeyIndex(fieldsToGet);
 		if (primaryKeyIndex < 0) {
 			// Add a LayoutItemField for the primary key to the end of the item list in the LayoutGroup because it
 			// doesn't already contain a primary key.
 			LayoutItem_Field libglomLayoutItemField = getPrimaryKeyLayoutItemFromFields(document, tableName);
 			layoutGroup.addItem(convertToGWTGlomLayoutItemField(libglomLayoutItemField));
 			layoutGroup.setPrimaryKeyIndex(layoutGroup.getItems().size() - 1);
 			layoutGroup.setHiddenPrimaryKey(true);
 
 		} else {
 			layoutGroup.setPrimaryKeyIndex(primaryKeyIndex);
 		}
 
 		layoutGroup.setTableName(tableName);
 
 		return layoutGroup;
 	}
 
 	private LayoutItem_Field getPrimaryKeyLayoutItemFromFields(Document document, String tableName) {
 		Field primaryKey = null;
 		FieldVector fieldVec = document.get_table_fields(tableName);
 		for (int i = 0; i < fieldVec.size(); i++) {
 			Field field = fieldVec.get(i);
 			if (field != null && field.get_primary_key()) {
 				primaryKey = field;
 				break;
 			}
 		}
 		if (primaryKey == null) {
 			Log.fatal(document.get_database_title(), tableName,
 					"A primary key was not found in the FieldVector for this table.");
 			// TODO throw exception
 		}
 
 		LayoutItem_Field libglomLayoutItemField = new LayoutItem_Field();
 		libglomLayoutItemField.set_full_field_details(primaryKey);
 		return libglomLayoutItemField;
 	}
 
 	/*
 	 * Gets the primary key index of the LayoutFieldVector.
 	 */
 	private int getPrimaryKeyIndex(LayoutFieldVector layoutFieldVec) {
 		for (int i = 0; i < layoutFieldVec.size(); i++) {
 			LayoutItem_Field layoutItemField = layoutFieldVec.get(i);
 			Field field = layoutItemField.get_full_field_details();
 			if (field != null && field.get_primary_key())
 				return i;
 		}
 		return -1;
 	}
 
 	/*
 	 * Get the number of rows a query with the table name and layout fields would return. This is needed for the /* list
 	 * view pager.
 	 */
 	private int getResultSizeOfSQLQuery(String documentID, String tableName, LayoutFieldVector fieldsToGet) {
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		if (!configuredDoc.isAuthenticated())
 			return -1;
 		Connection conn = null;
 		Statement st = null;
 		ResultSet rs = null;
 		try {
 			// Setup and execute the count query. Special care needs to be take to ensure that the results will be based
 			// on a cursor so that large amounts of memory are not consumed when the query retrieve a large amount of
 			// data. Here's the relevant PostgreSQL documentation:
 			// http://jdbc.postgresql.org/documentation/83/query.html#query-with-cursor
 			ComboPooledDataSource cpds = configuredDoc.getCpds();
 			conn = cpds.getConnection();
 			conn.setAutoCommit(false);
 			st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
 			String query = Glom.build_sql_select_count_simple(tableName, fieldsToGet);
 			// TODO Test execution time of this query with when the number of rows in the table is large (say >
 			// 1,000,000). Test memory usage at the same time (see the todo item in getTableData()).
 			rs = st.executeQuery(query);
 
 			// get the number of rows in the query
 			rs.next();
 			return rs.getInt(1);
 
 		} catch (SQLException e) {
 			Log.error(documentID, tableName, "Error calculating number of rows in the query.", e);
 			return -1;
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
 	}
 
 	// FIXME Check if we can use getFieldsToShowForSQLQuery() in these methods
 	public ArrayList<GlomField[]> getListData(String documentID, String tableName, int start, int length) {
 		return getListData(documentID, tableName, start, length, false, 0, false);
 	}
 
 	public ArrayList<GlomField[]> getSortedListData(String documentID, String tableName, int start, int length,
 			int sortColumnIndex, boolean isAscending) {
 		return getListData(documentID, tableName, start, length, true, sortColumnIndex, isAscending);
 	}
 
 	private ArrayList<GlomField[]> getListData(String documentID, String tableName, int start, int length,
 			boolean useSortClause, int sortColumnIndex, boolean isAscending) {
 
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		if (!configuredDoc.isAuthenticated()) {
 			return new ArrayList<GlomField[]>();
 		}
 
 		Document document = configuredDoc.getDocument();
 
 		// Use the default table if the table name hasn't been set or if the table name isn't in the glom document.
 		// The last check guards against SQL injection attacks since the table name could come from the URL.
 		if (tableName == null || tableName.isEmpty() || !document.get_table_is_known(tableName)) {
 			tableName = document.get_default_table();
 		}
 
 		// access the layout list using the defined layout list or the table fields if there's no layout list
 		LayoutGroupVector layoutListVec = document.get_data_layout_groups("list", tableName);
 		LayoutFieldVector layoutFields = new LayoutFieldVector();
 		SortClause sortClause = new SortClause();
 		int listViewLayoutGroupSize = safeLongToInt(layoutListVec.size());
 		if (layoutListVec.size() > 0) {
 			// a layout list is defined, we can use it to for the LayoutListTable
 			if (listViewLayoutGroupSize > 1)
 				Log.warn(documentID, tableName, "The size of the list view layout group for table is greater than 1. "
 						+ "Attempting to use the first item for the layout list view.");
 			LayoutItemVector layoutItemsVec = layoutListVec.get(0).get_items();
 
 			// find the defined layout list fields
 			int numItems = safeLongToInt(layoutItemsVec.size());
 			for (int i = 0; i < numItems; i++) {
 				// TODO add support for other LayoutItems (Text, Image, Button)
 				LayoutItem item = layoutItemsVec.get(i);
 				LayoutItem_Field layoutItemfield = LayoutItem_Field.cast_dynamic(item);
 				if (layoutItemfield != null) {
 					// use this field in the layout
 					layoutFields.add(layoutItemfield);
 
 					// create a sort clause if it's a primary key and we're not asked to sort a specific column
 					if (!useSortClause) {
 						Field details = layoutItemfield.get_full_field_details();
 						if (details != null && details.get_primary_key()) {
 							sortClause.addLast(new SortFieldPair(layoutItemfield, true)); // ascending
 						}
 					}
 				}
 			}
 		} else {
 			// no layout list is defined, use the table fields as the layout list
 			FieldVector fieldsVec = document.get_table_fields(tableName);
 
 			// find the fields to display in the layout list
 			int numItems = safeLongToInt(fieldsVec.size());
 			for (int i = 0; i < numItems; i++) {
 				Field field = fieldsVec.get(i);
 				LayoutItem_Field layoutItemField = new LayoutItem_Field();
 				layoutItemField.set_full_field_details(field);
 				layoutFields.add(layoutItemField);
 
 				// create a sort clause if it's a primary key and we're not asked to sort a specific column
 				if (!useSortClause) {
 					if (field.get_primary_key()) {
 						sortClause.addLast(new SortFieldPair(layoutItemField, true)); // ascending
 					}
 				}
 			}
 		}
 
 		// create a sort clause for the column we've been asked to sort
 		if (useSortClause) {
 			LayoutItem item = layoutFields.get(sortColumnIndex);
 			LayoutItem_Field field = LayoutItem_Field.cast_dynamic(item);
 			if (field != null)
 				sortClause.addLast(new SortFieldPair(field, isAscending));
 			else {
 				Log.error(documentID, tableName, "Error getting LayoutItem_Field for column index " + sortColumnIndex
 						+ ". Cannot create a sort clause for this column.");
 			}
 		}
 
 		// Add a LayoutItem_Field for the primary key to the end of the LayoutFieldVector if it doesn't already contain
 		// a primary key.
 		// TODO Can we use a cached LayoutGroup object to find out if we need to add a LayoutItem_Field object for the
 		// primary key field?
 		if (getPrimaryKeyIndex(layoutFields) < 0) {
 			layoutFields.add(getPrimaryKeyLayoutItemFromFields(document, tableName));
 		}
 
 		ArrayList<GlomField[]> rowsList = new ArrayList<GlomField[]>();
 		Connection conn = null;
 		Statement st = null;
 		ResultSet rs = null;
 		try {
 			// Setup the JDBC driver and get the query. Special care needs to be take to ensure that the results will be
 			// based on a cursor so that large amounts of memory are not consumed when the query retrieve a large amount
 			// of data. Here's the relevant PostgreSQL documentation:
 			// http://jdbc.postgresql.org/documentation/83/query.html#query-with-cursor
 			ComboPooledDataSource cpds = configuredDoc.getCpds();
 			conn = cpds.getConnection();
 			conn.setAutoCommit(false);
 			st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
 			st.setFetchSize(length);
 			String query = Glom.build_sql_select_simple(tableName, layoutFields, sortClause) + " OFFSET " + start;
 			// TODO Test memory usage before and after we execute the query that would result in a large ResultSet.
 			// We need to ensure that the JDBC driver is in fact returning a cursor based result set that has a low
 			// memory footprint. Check the difference between this value before and after the query:
 			// Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
 			// Test the execution time at the same time (see the todo item in getLayoutListTable()).
 			rs = st.executeQuery(query);
 
 			// get the results from the ResultSet
 			rowsList = getData(documentID, tableName, length, layoutFields, rs);
 		} catch (SQLException e) {
 			Log.error(documentID, tableName, "Error executing database query.", e);
 			// TODO: somehow notify user of problem
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
 		return rowsList;
 	}
 
 	private ArrayList<GlomField[]> getData(String documentID, String tableName, int length,
 			LayoutFieldVector layoutFields, ResultSet rs) throws SQLException {
 
 		// get the data we've been asked for
 		int rowCount = 0;
 		ArrayList<GlomField[]> rowsList = new ArrayList<GlomField[]>();
 		while (rs.next() && rowCount <= length) {
 			int layoutFieldsSize = safeLongToInt(layoutFields.size());
 			GlomField[] rowArray = new GlomField[layoutFieldsSize];
 			for (int i = 0; i < layoutFieldsSize; i++) {
 				// make a new GlomField to set the text and colours
 				rowArray[i] = new GlomField();
 
 				// get foreground and background colours
 				LayoutItem_Field field = layoutFields.get(i);
 				FieldFormatting formatting = field.get_formatting_used();
 				String fgcolour = formatting.get_text_format_color_foreground();
 				if (!fgcolour.isEmpty())
 					rowArray[i].setFGColour(convertGdkColorToHtmlColour(fgcolour));
 				String bgcolour = formatting.get_text_format_color_background();
 				if (!bgcolour.isEmpty())
 					rowArray[i].setBGColour(convertGdkColorToHtmlColour(bgcolour));
 
 				// Convert the field value to a string based on the glom type. We're doing the formatting on the
 				// server side for now but it might be useful to move this to the client side.
 				switch (field.get_glom_type()) {
 				case TYPE_TEXT:
 					String text = rs.getString(i + 1);
 					rowArray[i].setText(text != null ? text : "");
 					break;
 				case TYPE_BOOLEAN:
 					rowArray[i].setBoolean(rs.getBoolean(i + 1));
 					break;
 				case TYPE_NUMERIC:
 					// Take care of the numeric formatting before converting the number to a string.
 					NumericFormat numFormatGlom = formatting.getM_numeric_format();
 					// There's no isCurrency() method in the glom NumericFormat class so we're assuming that the
 					// number should be formatted as a currency if the currency code string is not empty.
 					String currencyCode = numFormatGlom.getM_currency_symbol();
 					NumberFormat numFormatJava = null;
 					boolean useGlomCurrencyCode = false;
 					if (currencyCode.length() == 3) {
 						// Try to format the currency using the Java Locales system.
 						try {
 							Currency currency = Currency.getInstance(currencyCode);
 							Log.info(documentID, tableName, "A valid ISO 4217 currency code is being used."
 									+ " Overriding the numeric formatting with information from the locale.");
 							int digits = currency.getDefaultFractionDigits();
 							numFormatJava = NumberFormat.getCurrencyInstance(locale);
 							numFormatJava.setCurrency(currency);
 							numFormatJava.setMinimumFractionDigits(digits);
 							numFormatJava.setMaximumFractionDigits(digits);
 						} catch (IllegalArgumentException e) {
 							Log.warn(documentID, tableName, currencyCode + " is not a valid ISO 4217 code."
 									+ " Manually setting currency code with this value.");
 							// The currency code is not this is not an ISO 4217 currency code.
 							// We're going to manually set the currency code and use the glom numeric formatting.
 							useGlomCurrencyCode = true;
 							numFormatJava = convertToJavaNumberFormat(numFormatGlom);
 						}
 					} else if (currencyCode.length() > 0) {
 						Log.warn(documentID, tableName, currencyCode + " is not a valid ISO 4217 code."
 								+ " Manually setting currency code with this value.");
 						// The length of the currency code is > 0 and != 3; this is not an ISO 4217 currency code.
 						// We're going to manually set the currency code and use the glom numeric formatting.
 						useGlomCurrencyCode = true;
 						numFormatJava = convertToJavaNumberFormat(numFormatGlom);
 					} else {
 						// The length of the currency code is 0; the number is not a currency.
 						numFormatJava = convertToJavaNumberFormat(numFormatGlom);
 					}
 
 					// TODO: Do I need to do something with NumericFormat.get_default_precision() from libglom?
 
 					double number = rs.getDouble(i + 1);
 					if (number < 0) {
 						if (formatting.getM_numeric_format().getM_alt_foreground_color_for_negatives())
 							// overrides the set foreground colour
 							rowArray[i].setFGColour(convertGdkColorToHtmlColour(NumericFormat
 									.get_alternative_color_for_negatives()));
 					}
 
 					// Finally convert the number to text using the glom currency string if required.
 					if (useGlomCurrencyCode) {
 						rowArray[i].setText(currencyCode + " " + numFormatJava.format(number));
 					} else {
 						rowArray[i].setText(numFormatJava.format(number));
 					}
 					break;
 				case TYPE_DATE:
 					Date date = rs.getDate(i + 1);
 					if (date != null) {
 						DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
 						rowArray[i].setText(dateFormat.format(date));
 					} else {
 						rowArray[i].setText("");
 					}
 					break;
 				case TYPE_TIME:
 					Time time = rs.getTime(i + 1);
 					if (time != null) {
 						DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
 						rowArray[i].setText(timeFormat.format(time));
 					} else {
 						rowArray[i].setText("");
 					}
 					break;
 				case TYPE_IMAGE:
 					byte[] image = rs.getBytes(i + 1);
 					if (image != null) {
 						// TODO implement field TYPE_IMAGE
 						rowArray[i].setText("Image (FIXME)");
 					} else {
 						rowArray[i].setText("");
 					}
 					break;
 				case TYPE_INVALID:
 				default:
 					Log.warn(documentID, tableName, "Invalid LayoutItem Field type. Using empty string for value.");
 					rowArray[i].setText("");
 					break;
 				}
 			}
 
 			// add the row of GlomFields to the ArrayList we're going to return and update the row count
 			rowsList.add(rowArray);
 			rowCount++;
 		}
 
 		return rowsList;
 	}
 
 	public Documents getDocuments() {
 		Documents documents = new Documents();
 		for (String documentID : documentMapping.keySet()) {
 			ConfiguredDocument configuredDoc = documentMapping.get(documentID);
			documents.addDocuemnt(documentID, configuredDoc.getDocument().get_database_title());
 		}
 		return documents;
 	}
 
 	/*
 	 * This method safely converts longs from libglom into ints. This method was taken from stackoverflow:
 	 * 
 	 * http://stackoverflow.com/questions/1590831/safely-casting-long-to-int-in-java
 	 */
 	private int safeLongToInt(long value) {
 		if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
 			throw new IllegalArgumentException(value + " cannot be cast to int without changing its value.");
 		}
 		return (int) value;
 	}
 
 	private NumberFormat convertToJavaNumberFormat(NumericFormat numFormatGlom) {
 		NumberFormat numFormatJava = NumberFormat.getInstance(locale);
 		if (numFormatGlom.getM_decimal_places_restricted()) {
 			int digits = safeLongToInt(numFormatGlom.getM_decimal_places());
 			numFormatJava.setMinimumFractionDigits(digits);
 			numFormatJava.setMaximumFractionDigits(digits);
 		}
 		numFormatJava.setGroupingUsed(numFormatGlom.getM_use_thousands_separator());
 		return numFormatJava;
 	}
 
 	/*
 	 * Converts a Gdk::Color (16-bits per channel) to an HTML colour (8-bits per channel) by discarding the least
 	 * significant 8-bits in each channel.
 	 */
 	private String convertGdkColorToHtmlColour(String gdkColor) {
 		if (gdkColor.length() == 13)
 			return gdkColor.substring(0, 3) + gdkColor.substring(5, 7) + gdkColor.substring(9, 11);
 		else if (gdkColor.length() == 7) {
 			// This shouldn't happen but let's deal with it if it does.
 			Log.warn("Expected a 13 character string but received a 7 character string. Returning received string.");
 			return gdkColor;
 		} else {
 			Log.error("Did not receive a 13 or 7 character string. Returning black HTML colour code.");
 			return "#000000";
 		}
 	}
 
 	/*
 	 * This method converts a FieldFormatting.HorizontalAlignment to the equivalent ColumnInfo.HorizontalAlignment. The
 	 * need for this comes from the fact that the GWT HorizontalAlignment classes can't be used with RPC and there's no
 	 * easy way to use the java-libglom FieldFormatting.HorizontalAlignment enum with RPC. An enum identical to
 	 * FieldFormatting.HorizontalAlignment is included in the ColumnInfo class.
 	 */
 	private Formatting.HorizontalAlignment convertToGWTGlomHorizonalAlignment(
 			FieldFormatting.HorizontalAlignment alignment) {
 		switch (alignment) {
 		case HORIZONTAL_ALIGNMENT_AUTO:
 			return Formatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_AUTO;
 		case HORIZONTAL_ALIGNMENT_LEFT:
 			return Formatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_LEFT;
 		case HORIZONTAL_ALIGNMENT_RIGHT:
 			return Formatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_RIGHT;
 		default:
 			Log.error("Recieved an alignment that I don't know about: "
 					+ FieldFormatting.HorizontalAlignment.class.getName() + "." + alignment.toString() + ". Returning "
 					+ Formatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_RIGHT.toString() + ".");
 			return Formatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_RIGHT;
 		}
 	}
 
 	/*
 	 * This method converts a Field.glom_field_type to the equivalent ColumnInfo.FieldType. The need for this comes from
 	 * the fact that the GWT FieldType classes can't be used with RPC and there's no easy way to use the java-libglom
 	 * Field.glom_field_type enum with RPC. An enum identical to FieldFormatting.glom_field_type is included in the
 	 * ColumnInfo class.
 	 */
 	private LayoutItemField.GlomFieldType convertToGWTGlomFieldType(Field.glom_field_type type) {
 		switch (type) {
 		case TYPE_BOOLEAN:
 			return LayoutItemField.GlomFieldType.TYPE_BOOLEAN;
 		case TYPE_DATE:
 			return LayoutItemField.GlomFieldType.TYPE_DATE;
 		case TYPE_IMAGE:
 			return LayoutItemField.GlomFieldType.TYPE_IMAGE;
 		case TYPE_NUMERIC:
 			return LayoutItemField.GlomFieldType.TYPE_NUMERIC;
 		case TYPE_TEXT:
 			return LayoutItemField.GlomFieldType.TYPE_TEXT;
 		case TYPE_TIME:
 			return LayoutItemField.GlomFieldType.TYPE_TIME;
 		case TYPE_INVALID:
 			Log.info("Returning TYPE_INVALID.");
 			return LayoutItemField.GlomFieldType.TYPE_INVALID;
 		default:
 			Log.error("Recieved a type that I don't know about: " + Field.glom_field_type.class.getName() + "."
 					+ type.toString() + ". Returning " + LayoutItemField.GlomFieldType.TYPE_INVALID.toString() + ".");
 			return LayoutItemField.GlomFieldType.TYPE_INVALID;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#isAuthenticated(java.lang.String)
 	 */
 	public boolean isAuthenticated(String documentID) {
 		return documentMapping.get(documentID).isAuthenticated();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#checkAuthentication(java.lang.String, java.lang.String,
 	 * java.lang.String)
 	 */
 	public boolean checkAuthentication(String documentID, String username, String password) {
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		try {
 			return configuredDoc.setUsernameAndPassword(username, password);
 		} catch (SQLException e) {
 			Log.error(documentID, "Unknown SQL Error checking the database authentication.", e);
 			return false;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getDetailsLayoutGroup(java.lang.String, java.lang.String)
 	 */
 	public ArrayList<LayoutGroup> getDetailsLayout(String documentID, String tableName) {
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		Document document = configuredDoc.getDocument();
 
 		// Use the default table if the table name hasn't been set or if the table name isn't in the glom document.
 		// The last check guards against SQL injection attacks since the table name could come from the URL.
 		if (tableName == null || tableName.isEmpty() || !document.get_table_is_known(tableName)) {
 			tableName = document.get_default_table();
 		}
 
 		LayoutGroupVector layoutGroupVec = document.get_data_layout_groups("details", tableName);
 
 		ArrayList<LayoutGroup> layoutGroups = new ArrayList<LayoutGroup>();
 		for (int i = 0; i < layoutGroupVec.size(); i++) {
 			org.glom.libglom.LayoutGroup libglomLayoutGroup = layoutGroupVec.get(i);
 
 			if (libglomLayoutGroup == null)
 				continue;
 
 			layoutGroups.add(getLayoutGroup(documentID, tableName, libglomLayoutGroup));
 		}
 
 		return layoutGroups;
 	}
 
 	/*
 	 * Gets a GWT-Glom LayoutGroup object for the specified libglom LayoutGroup object. This is used for getting layout
 	 * information for the list and details views.
 	 * 
 	 * @param documentID Glom document identifier
 	 * 
 	 * @param tableName table name in the specified Glom document
 	 * 
 	 * @param libglomLayoutGroup libglom LayoutGroup to convert
 	 * 
 	 * @precondition libglomLayoutGroup must not be null
 	 * 
 	 * @return {@link LayoutGroup} object that represents the layout for the specified {@link
 	 * org.glom.libglom.LayoutGroup}
 	 */
 	private LayoutGroup getLayoutGroup(String documentID, String tableName,
 			org.glom.libglom.LayoutGroup libglomLayoutGroup) {
 		LayoutGroup layoutGroup = new LayoutGroup();
 		layoutGroup.setColumnCount(safeLongToInt(libglomLayoutGroup.get_columns_count()));
 
 		// look at each child item
 		LayoutItemVector layoutItemsVec = libglomLayoutGroup.get_items();
 		for (int i = 0; i < layoutItemsVec.size(); i++) {
 			org.glom.libglom.LayoutItem libglomLayoutItem = layoutItemsVec.get(i);
 
 			// just a safety check
 			if (libglomLayoutItem == null)
 				continue;
 
 			org.glom.web.shared.layout.LayoutItem layoutItem = null;
 			org.glom.libglom.LayoutGroup group = org.glom.libglom.LayoutGroup.cast_dynamic(libglomLayoutItem);
 			if (group != null) {
 				// recurse into child groups
 				layoutItem = getLayoutGroup(documentID, tableName, group);
 			} else {
 				// create GWT-Glom LayoutItem types based on the the libglom type
 				// TODO add support for other LayoutItems (Text, Image, Button etc.)
 				LayoutItem_Field libglomLayoutField = LayoutItem_Field.cast_dynamic(libglomLayoutItem);
 				if (libglomLayoutField != null) {
 					layoutItem = convertToGWTGlomLayoutItemField(libglomLayoutField);
 				} else {
 					Log.info(documentID, tableName,
 							"Ignoring unknown LayoutItem of type " + libglomLayoutItem.get_part_type_name() + ".");
 					continue;
 				}
 
 			}
 
 			layoutItem.setTitle(libglomLayoutItem.get_title_or_name());
 			layoutGroup.addItem(layoutItem);
 		}
 
 		return layoutGroup;
 	}
 
 	private LayoutItemField convertToGWTGlomLayoutItemField(LayoutItem_Field libglomLayoutField) {
 		LayoutItemField layoutItemField = new LayoutItemField();
 
 		// set type
 		layoutItemField.setType(convertToGWTGlomFieldType(libglomLayoutField.get_glom_type()));
 
 		// set formatting
 		Formatting formatting = new Formatting();
 		formatting.setHorizontalAlignment(convertToGWTGlomHorizonalAlignment(libglomLayoutField
 				.get_formatting_used_horizontal_alignment()));
 		layoutItemField.setFormatting(formatting);
 
 		return layoutItemField;
 	}
 
 	public GlomField[] getDetailsData(String documentID, String tableName, String primaryKeyValue) {
 
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		Document document = configuredDoc.getDocument();
 
 		// Use the default table if the table name hasn't been set or if the table name isn't in the glom document.
 		// The last check guards against SQL injection attacks since the table name could come from the URL.
 		if (tableName == null || tableName.isEmpty() || !document.get_table_is_known(tableName)) {
 			tableName = document.get_default_table();
 		}
 
 		LayoutFieldVector fieldsToGet = getFieldsToShowForSQLQuery(document, tableName, "details");
 
 		if (fieldsToGet == null || fieldsToGet.size() <= 0) {
 			Log.warn(documentID, tableName, "Didn't find any fields to show. Returning null.");
 			return null;
 		}
 
 		// get primary key for the table to use in the SQL query
 		Field primaryKey = null;
 		FieldVector fieldsVec = document.get_table_fields(tableName);
 		for (int i = 0; i < safeLongToInt(fieldsVec.size()); i++) {
 			Field field = fieldsVec.get(i);
 			if (field.get_primary_key()) {
 				primaryKey = field;
 				break;
 			}
 		}
 
 		if (primaryKey == null) {
 			Log.error(documentID, tableName, "Couldn't find primary key in table. Returning null.");
 			return null;
 		}
 
 		ArrayList<GlomField[]> rowsList = new ArrayList<GlomField[]>();
 		Connection conn = null;
 		Statement st = null;
 		ResultSet rs = null;
 		try {
 			// Setup the JDBC driver and get the query.
 			ComboPooledDataSource cpds = configuredDoc.getCpds();
 			conn = cpds.getConnection();
 			st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
 			String query = Glom.build_sql_select_with_key(tableName, fieldsToGet, primaryKey, primaryKeyValue);
 			rs = st.executeQuery(query);
 
 			// get the results from the ResultSet
 			// using 2 as a length parameter so we can log a warning if appropriate
 			rowsList = getData(documentID, tableName, 2, fieldsToGet, rs);
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
 		} else if (rowsList.size() > 1 && primaryKeyValue != null && !primaryKeyValue.isEmpty()) {
 			// only log a warning if the result size is greater than 1 and the primaryKeyValue was set
 			Log.warn(documentID, tableName,
 					"The query did not return the expected unique result. Returning the first result in the set.");
 		}
 
 		return rowsList.get(0);
 
 	}
 
 	/*
 	 * Gets a LayoutFieldVector to use when generating an SQL query.
 	 */
 	private LayoutFieldVector getFieldsToShowForSQLQuery(Document document, String tableName, String layoutName) {
 		LayoutGroupVector layoutGroupVec = document.get_data_layout_groups(layoutName, tableName);
 		LayoutFieldVector layoutFieldVector = new LayoutFieldVector();
 
 		// special case for list layouts that don't have a defined layout group
 		if ("list".equals(layoutName) && layoutGroupVec.size() == 0) {
 			FieldVector fieldsVec = document.get_table_fields(tableName);
 			for (int i = 0; i < fieldsVec.size(); i++) {
 				Field field = fieldsVec.get(i);
 				LayoutItem_Field layoutItemField = new LayoutItem_Field();
 				layoutItemField.set_full_field_details(field);
 				layoutFieldVector.add(layoutItemField);
 			}
 			return layoutFieldVector;
 		}
 
 		// We will show the fields that the document says we should:
 		for (int i = 0; i < layoutGroupVec.size(); i++) {
 			org.glom.libglom.LayoutGroup layoutGroup = layoutGroupVec.get(i);
 
 			// Get the fields:
 			ArrayList<LayoutItem_Field> layoutItemsFields = getFieldsToShowForSQLQueryAddGroup(document, tableName,
 					layoutGroup);
 			for (LayoutItem_Field layoutItem_Field : layoutItemsFields) {
 				layoutFieldVector.add(layoutItem_Field);
 			}
 		}
 		return layoutFieldVector;
 	}
 
 	private ArrayList<LayoutItem_Field> getFieldsToShowForSQLQueryAddGroup(Document document, String tableName,
 			org.glom.libglom.LayoutGroup layoutGroup) {
 
 		ArrayList<LayoutItem_Field> layoutItemFields = new ArrayList<LayoutItem_Field>();
 		LayoutItemVector items = layoutGroup.get_items();
 		for (int i = 0; i < items.size(); i++) {
 			LayoutItem layoutItem = items.get(i);
 
 			LayoutItem_Field layoutItemField = LayoutItem_Field.cast_dynamic(layoutItem);
 			if (layoutItemField != null) {
 				// the layoutItem is a LayoutItem_Field
 				FieldVector fields;
 				if (layoutItemField.get_has_relationship_name()) {
 					// layoutItemField is a field in a related table
 					fields = document.get_table_fields(layoutItemField.get_table_used(tableName));
 				} else {
 					// layoutItemField is a field in this table
 					fields = document.get_table_fields(tableName);
 				}
 
 				// set the layoutItemFeild with details from its Field in the document and
 				// add it to the list to be returned
 				for (int j = 0; j < fields.size(); j++) {
 					// check the names to see if they're the same
 					// this works because we're using the field list from the related table if necessary
 					if (layoutItemField.get_name().equals(fields.get(j).get_name())) {
 						Field field = fields.get(j);
 						if (field != null) {
 							layoutItemField.set_full_field_details(field);
 							layoutItemFields.add(layoutItemField);
 						} else {
 							Log.warn(document.get_database_title(), tableName,
 									"LayoutItem_Field " + layoutItemField.get_layout_display_name()
 											+ " not found in document field list.");
 						}
 						break;
 					}
 				}
 
 			} else {
 				// the layoutItem is not a LayoutItem_Field
 				org.glom.libglom.LayoutGroup subLayoutGroup = org.glom.libglom.LayoutGroup.cast_dynamic(layoutItem);
 				if (subLayoutGroup != null) {
 					// the layoutItem is a LayoutGroup
 					LayoutItem_Portal layoutItemPortal = LayoutItem_Portal.cast_dynamic(layoutItem);
 					if (layoutItemPortal == null) {
 						// The subGroup is not a LayoutItem_Portal.
 						// We're ignoring portals because they are filled by means of a separate SQL query.
 						layoutItemFields
 								.addAll(getFieldsToShowForSQLQueryAddGroup(document, tableName, subLayoutGroup));
 					}
 				}
 			}
 		}
 		return layoutItemFields;
 	}
 
}
