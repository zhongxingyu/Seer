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
 
 package org.glom.web.server;
 
 import java.beans.PropertyVetoException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import org.glom.libglom.Document;
 import org.glom.libglom.Field;
 import org.glom.libglom.FieldFormatting;
 import org.glom.libglom.FieldVector;
 import org.glom.libglom.Glom;
 import org.glom.libglom.LayoutGroupVector;
 import org.glom.libglom.LayoutItemVector;
 import org.glom.libglom.LayoutItem_Field;
 import org.glom.libglom.LayoutItem_Notebook;
 import org.glom.libglom.LayoutItem_Portal;
 import org.glom.libglom.NumericFormat;
 import org.glom.libglom.Relationship;
 import org.glom.libglom.StringVector;
 import org.glom.web.server.database.DetailsDBAccess;
 import org.glom.web.server.database.ListViewDBAccess;
 import org.glom.web.server.database.RelatedListDBAccess;
 import org.glom.web.server.database.RelatedListNavigation;
 import org.glom.web.shared.DataItem;
 import org.glom.web.shared.DocumentInfo;
 import org.glom.web.shared.GlomNumericFormat;
 import org.glom.web.shared.NavigationRecord;
 import org.glom.web.shared.layout.Formatting;
 import org.glom.web.shared.layout.LayoutGroup;
 import org.glom.web.shared.layout.LayoutItem;
 import org.glom.web.shared.layout.LayoutItemField;
 import org.glom.web.shared.layout.LayoutItemNotebook;
 import org.glom.web.shared.layout.LayoutItemPortal;
 
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 
 /**
  * A class to hold configuration information for a given Glom document. This class is used to retrieve layout
  * information from libglom and data from the underlying PostgreSQL database.
  * 
  * @author Ben Konrath <ben@bagu.org>
  * 
  */
 final class ConfiguredDocument {
 
 	private Document document;
 	private ComboPooledDataSource cpds;
 	private boolean authenticated = false;
 	private String documentID;
 
 	@SuppressWarnings("unused")
 	private ConfiguredDocument() {
 		// disable default constructor
 	}
 
 	ConfiguredDocument(Document document) throws PropertyVetoException {
 
 		// load the jdbc driver
 		cpds = new ComboPooledDataSource();
 
 		// We don't support sqlite or self-hosting yet.
 		if (document.get_hosting_mode() != Document.HostingMode.HOSTING_MODE_POSTGRES_CENTRAL) {
 			Log.fatal("Error configuring the database connection." + " Only central PostgreSQL hosting is supported.");
 			// FIXME: Throw exception?
 		}
 
 		try {
 			cpds.setDriverClass("org.postgresql.Driver");
 		} catch (PropertyVetoException e) {
 			Log.fatal("Error loading the PostgreSQL JDBC driver."
 					+ " Is the PostgreSQL JDBC jar available to the servlet?", e);
 			throw e;
 		}
 
 		// setup the JDBC driver for the current glom document
 		cpds.setJdbcUrl("jdbc:postgresql://" + document.get_connection_server() + ":" + document.get_connection_port()
 				+ "/" + document.get_connection_database());
 
 		this.document = document;
 	}
 
 	/**
 	 * Sets the username and password for the database associated with the Glom document.
 	 * 
 	 * @return true if the username and password works, false otherwise
 	 */
 	boolean setUsernameAndPassword(String username, String password) throws SQLException {
 		cpds.setUser(username);
 		cpds.setPassword(password);
 
 		int acquireRetryAttempts = cpds.getAcquireRetryAttempts();
 		cpds.setAcquireRetryAttempts(1);
 		Connection conn = null;
 		try {
 			// FIXME find a better way to check authentication
 			// it's possible that the connection could be failing for another reason
 			conn = cpds.getConnection();
 			authenticated = true;
 		} catch (SQLException e) {
 			Log.info(Utils.getFileName(document.get_file_uri()), e.getMessage());
 			Log.info(Utils.getFileName(document.get_file_uri()),
 					"Connection Failed. Maybe the username or password is not correct.");
 			authenticated = false;
 		} finally {
 			if (conn != null)
 				conn.close();
 			cpds.setAcquireRetryAttempts(acquireRetryAttempts);
 		}
 		return authenticated;
 	}
 
 	Document getDocument() {
 		return document;
 	}
 
 	ComboPooledDataSource getCpds() {
 		return cpds;
 	}
 
 	boolean isAuthenticated() {
 		return authenticated;
 	}
 
 	String getDocumentID() {
 		return documentID;
 	}
 
 	void setDocumentID(String documentID) {
 		this.documentID = documentID;
 	}
 
 	/**
 	 * @return
 	 */
 	DocumentInfo getDocumentInfo() {
 		DocumentInfo documentInfo = new DocumentInfo();
 
 		// get arrays of table names and titles, and find the default table index
 		StringVector tablesVec = document.get_table_names();
 
 		int numTables = Utils.safeLongToInt(tablesVec.size());
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
 					documentInfo.setDefaultTableIndex(visibleIndex);
 					foundDefaultTable = true;
 				}
 				tableTitles.add(document.get_table_title(tableName));
 				visibleIndex++;
 			}
 		}
 
 		// set everything we need
 		documentInfo.setTableNames(tableNames);
 		documentInfo.setTableTitles(tableTitles);
 		documentInfo.setTitle(document.get_database_title());
 
 		return documentInfo;
 	}
 
 	/*
 	 * Gets the layout group for the list view using the defined layout list in the document or the table fields if
 	 * there's no defined layout group for the list view.
 	 */
 	private org.glom.libglom.LayoutGroup getValidListViewLayoutGroup(String tableName) {
 
 		LayoutGroupVector layoutGroupVec = document.get_data_layout_groups("list", tableName);
 
 		int listViewLayoutGroupSize = Utils.safeLongToInt(layoutGroupVec.size());
 		org.glom.libglom.LayoutGroup libglomLayoutGroup = null;
 		if (listViewLayoutGroupSize > 0) {
 			// a list layout group is defined; we can use the first group as the list
 			if (listViewLayoutGroupSize > 1)
 				Log.warn(documentID, tableName, "The size of the list layout group is greater than 1. "
 						+ "Attempting to use the first item for the layout list view.");
 
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
 
 		return libglomLayoutGroup;
 	}
 
 	ArrayList<DataItem[]> getListViewData(String tableName, int start, int length, boolean useSortClause,
 			int sortColumnIndex, boolean isAscending) {
 		// Validate the table name.
 		tableName = getTableNameToUse(tableName);
 
 		// Get the libglom LayoutGroup that represents the list view.
 		org.glom.libglom.LayoutGroup libglomLayoutGroup = getValidListViewLayoutGroup(tableName);
 
 		// Create a database access object for the list view.
 		ListViewDBAccess listViewDBAccess = new ListViewDBAccess(document, documentID, cpds, tableName,
 				libglomLayoutGroup);
 
 		// Return the data.
 		return listViewDBAccess.getData(start, length, useSortClause, sortColumnIndex, isAscending);
 	}
 
 	DataItem[] getDetailsData(String tableName, String primaryKeyValue) {
 		// Validate the table name.
 		tableName = getTableNameToUse(tableName);
 
 		DetailsDBAccess detailsDBAccess = new DetailsDBAccess(document, documentID, cpds, tableName);
 
 		return detailsDBAccess.getData(primaryKeyValue);
 	}
 
 	ArrayList<DataItem[]> getRelatedListData(String tableName, String relationshipName, String foreignKeyValue,
 			int start, int length, boolean useSortClause, int sortColumnIndex, boolean isAscending) {
 		// Validate the table name.
 		tableName = getTableNameToUse(tableName);
 
 		// Create a database access object for the related list
 		RelatedListDBAccess relatedListDBAccess = new RelatedListDBAccess(document, documentID, cpds, tableName,
 				relationshipName);
 
 		// Return the data
 		return relatedListDBAccess.getData(start, length, foreignKeyValue, useSortClause, sortColumnIndex, isAscending);
 	}
 
 	ArrayList<LayoutGroup> getDetailsLayoutGroup(String tableName) {
 		// Validate the table name.
 		tableName = getTableNameToUse(tableName);
 
 		// Get the details layout group information for each LayoutGroup in the LayoutGroupVector
 		LayoutGroupVector layoutGroupVec = document.get_data_layout_groups("details", tableName);
 		ArrayList<LayoutGroup> layoutGroups = new ArrayList<LayoutGroup>();
 		for (int i = 0; i < layoutGroupVec.size(); i++) {
 			org.glom.libglom.LayoutGroup libglomLayoutGroup = layoutGroupVec.get(i);
 
 			// satisfy the precondition of getDetailsLayoutGroup(String, org.glom.libglom.LayoutGroup)
 			if (libglomLayoutGroup == null)
 				continue;
 
 			layoutGroups.add(getDetailsLayoutGroup(tableName, libglomLayoutGroup));
 		}
 
 		return layoutGroups;
 	}
 
 	LayoutGroup getListViewLayoutGroup(String tableName) {
 		// Validate the table name.
 		tableName = getTableNameToUse(tableName);
 
 		org.glom.libglom.LayoutGroup libglomLayoutGroup = getValidListViewLayoutGroup(tableName);
 
 		return getListLayoutGroup(tableName, libglomLayoutGroup);
 	}
 
 	/*
 	 * Gets the expected row count for a related list.
 	 */
 	int getRelatedListRowCount(String tableName, String relationshipName, String foreignKeyValue) {
 		// Validate the table name.
 		tableName = getTableNameToUse(tableName);
 
 		// Create a database access object for the related list
 		RelatedListDBAccess relatedListDBAccess = new RelatedListDBAccess(document, documentID, cpds, tableName,
 				relationshipName);
 
 		// Return the row count
 		return relatedListDBAccess.getExpectedResultSize(foreignKeyValue);
 	}
 
 	NavigationRecord getSuitableRecordToViewDetails(String tableName, String relationshipName, String primaryKeyValue) {
 		// Validate the table name.
 		tableName = getTableNameToUse(tableName);
 
 		RelatedListNavigation relatedListNavigation = new RelatedListNavigation(document, documentID, cpds, tableName,
 				relationshipName);
 
 		return relatedListNavigation.getNavigationRecord(primaryKeyValue);
 	}
 
 	/*
 	 * Gets a LayoutGroup DTO for the given table name and libglom LayoutGroup. This method can be used for the main
 	 * list view table and for the related list table.
 	 */
 	private LayoutGroup getListLayoutGroup(String tableName, org.glom.libglom.LayoutGroup libglomLayoutGroup) {
 		LayoutGroup layoutGroup = new LayoutGroup();
 		int primaryKeyIndex = -1;
 
 		// look at each child item
 		LayoutItemVector layoutItemsVec = libglomLayoutGroup.get_items();
 		int numItems = Utils.safeLongToInt(layoutItemsVec.size());
 		for (int i = 0; i < numItems; i++) {
 			org.glom.libglom.LayoutItem libglomLayoutItem = layoutItemsVec.get(i);
 
 			// TODO add support for other LayoutItems (Text, Image, Button etc.)
 			LayoutItem_Field libglomLayoutItemField = LayoutItem_Field.cast_dynamic(libglomLayoutItem);
 			if (libglomLayoutItemField != null) {
 				layoutGroup.addItem(convertToGWTGlomLayoutItemField(libglomLayoutItemField, true));
 				Field field = libglomLayoutItemField.get_full_field_details();
 				if (field.get_primary_key())
 					primaryKeyIndex = i;
 			} else {
 				Log.info(documentID, tableName,
 						"Ignoring unknown list LayoutItem of type " + libglomLayoutItem.get_part_type_name() + ".");
 				continue;
 			}
 		}
 
 		// set the expected result size for list view tables
 		LayoutItem_Portal libglomLayoutItemPortal = LayoutItem_Portal.cast_dynamic(libglomLayoutGroup);
 		if (libglomLayoutItemPortal == null) {
 			// libglomLayoutGroup is a list view
 			ListViewDBAccess listViewDBAccess = new ListViewDBAccess(document, documentID, cpds, tableName,
 					libglomLayoutGroup);
 			layoutGroup.setExpectedResultSize(listViewDBAccess.getExpectedResultSize());
 		}
 
 		// Set the primary key index for the table
 		if (primaryKeyIndex < 0) {
 			// Add a LayoutItemField for the primary key to the end of the item list in the LayoutGroup because it
 			// doesn't already contain a primary key.
 			Field primaryKey = null;
 			FieldVector fieldsVec = document.get_table_fields(tableName);
 			for (int i = 0; i < Utils.safeLongToInt(fieldsVec.size()); i++) {
 				Field field = fieldsVec.get(i);
 				if (field.get_primary_key()) {
 					primaryKey = field;
 					break;
 				}
 			}
 			if (primaryKey != null) {
 				LayoutItem_Field libglomLayoutItemField = new LayoutItem_Field();
 				libglomLayoutItemField.set_full_field_details(primaryKey);
 				layoutGroup.addItem(convertToGWTGlomLayoutItemField(libglomLayoutItemField, false));
 				layoutGroup.setPrimaryKeyIndex(layoutGroup.getItems().size() - 1);
 				layoutGroup.setHiddenPrimaryKey(true);
 			} else {
 				Log.error(document.get_database_title(), tableName,
 						"A primary key was not found in the FieldVector for this table. Navigation buttons will not work.");
 			}
 
 		} else {
 			layoutGroup.setPrimaryKeyIndex(primaryKeyIndex);
 		}
 
 		layoutGroup.setTableName(tableName);
 
 		return layoutGroup;
 	}
 
 	/*
 	 * Gets a recursively defined Details LayoutGroup DTO for the specified libglom LayoutGroup object. This is used for
 	 * getting layout information for the details view.
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
 	private LayoutGroup getDetailsLayoutGroup(String tableName, org.glom.libglom.LayoutGroup libglomLayoutGroup) {
 		LayoutGroup layoutGroup = new LayoutGroup();
 		layoutGroup.setColumnCount(Utils.safeLongToInt(libglomLayoutGroup.get_columns_count()));
		layoutGroup.setTitle(libglomLayoutGroup.get_title_or_name());
 
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
 				// libglomLayoutItem is a LayoutGroup
 				LayoutItem_Portal libglomLayoutItemPortal = LayoutItem_Portal.cast_dynamic(group);
 				if (libglomLayoutItemPortal != null) {
 					// group is a LayoutItem_Portal
 					LayoutItemPortal layoutItemPortal = new LayoutItemPortal();
 					Relationship relationship = libglomLayoutItemPortal.get_relationship();
 					if (relationship != null) {
 						layoutItemPortal.setNavigationType(convertToGWTGlomNavigationType(libglomLayoutItemPortal
 								.get_navigation_type()));
 
 						layoutItemPortal.setTitle(libglomLayoutItemPortal.get_title_used("")); // parent title not
 																								// relevant
 						LayoutGroup tempLayoutGroup = getListLayoutGroup(tableName, libglomLayoutItemPortal);
 						for (org.glom.web.shared.layout.LayoutItem item : tempLayoutGroup.getItems()) {
 							// TODO EDITING If the relationship does not allow editing, then mark all these fields as
 							// non-editable. Check relationship.get_allow_edit() to see if it's editable.
 							layoutItemPortal.addItem(item);
 						}
 						layoutItemPortal.setPrimaryKeyIndex(tempLayoutGroup.getPrimaryKeyIndex());
 						layoutItemPortal.setHiddenPrimaryKey(tempLayoutGroup.hasHiddenPrimaryKey());
 						layoutItemPortal.setName(libglomLayoutItemPortal.get_relationship_name_used());
 						layoutItemPortal.setTableName(relationship.get_from_table());
 						layoutItemPortal.setFromField(relationship.get_from_field());
 
 						// Set whether or not the related list will need to show the navigation buttons.
 						// This was ported from Glom: Box_Data_Portal::get_has_suitable_record_to_view_details()
 						StringBuffer navigationTableName = new StringBuffer();
 						LayoutItem_Field navigationRelationship = new LayoutItem_Field(); // Ignored.
 						libglomLayoutItemPortal.get_suitable_table_to_view_details(navigationTableName,
 								navigationRelationship, document);
 						layoutItemPortal.setAddNavigation(!(navigationTableName.toString().isEmpty()));
 					}
 
 					// Note: empty layoutItemPortal used if relationship is null
 					layoutItem = layoutItemPortal;
 
 				} else {
 					// libglomLayoutItem is a LayoutGroup
 					LayoutItem_Notebook libglomLayoutItemNotebook = LayoutItem_Notebook.cast_dynamic(group);
 					if (libglomLayoutItemNotebook != null) {
 						// group is a LayoutItem_Notebook
 						LayoutGroup tempLayoutGroup = getDetailsLayoutGroup(tableName, libglomLayoutItemNotebook);
 						LayoutItemNotebook layoutItemNotebook = new LayoutItemNotebook();
 						for (LayoutItem item : tempLayoutGroup.getItems()) {
 							layoutItemNotebook.addItem(item);
 						}
 						layoutItemNotebook.setName(tableName);
 						layoutItem = layoutItemNotebook;
 					} else {
 						// group is *not* a LayoutItem_Portal or a LayoutItem_Notebook
 						// recurse into child groups
 						layoutItem = getDetailsLayoutGroup(tableName, group);
 					}
 				}
 			} else {
 				// libglomLayoutItem is *not* a LayoutGroup
 				// create LayoutItem DTOs based on the the libglom type
 				// TODO add support for other LayoutItems (Text, Image, Button etc.)
 				LayoutItem_Field libglomLayoutItemField = LayoutItem_Field.cast_dynamic(libglomLayoutItem);
 				if (libglomLayoutItemField != null) {
 
 					LayoutItemField layoutItemField = convertToGWTGlomLayoutItemField(libglomLayoutItemField, true);
 
 					// Set the full field details with updated field details from the document.
 					libglomLayoutItemField.set_full_field_details(document.get_field(
 							libglomLayoutItemField.get_table_used(tableName), libglomLayoutItemField.get_name()));
 
 					// Determine if the field should have a navigation button and set this in the DTO.
 					Relationship fieldUsedInRelationshipToOne = new Relationship();
 					boolean addNavigation = Glom.layout_field_should_have_navigation(tableName, libglomLayoutItemField,
 							document, fieldUsedInRelationshipToOne);
 					layoutItemField.setAddNavigation(addNavigation);
 
 					// Set the the name of the table to navigate to if navigation should be enabled.
 					if (addNavigation) {
 						// It's not possible to directly check if fieldUsedInRelationshipToOne is
 						// null because of the way that the glom_sharedptr macro works. This workaround accomplishes the
 						// same task.
 						String tableNameUsed;
 						try {
 							Relationship temp = new Relationship();
 							temp.equals(fieldUsedInRelationshipToOne); // this will throw an NPE if
 																		// fieldUsedInRelationshipToOne is null
 							// fieldUsedInRelationshipToOne is *not* null
 							tableNameUsed = fieldUsedInRelationshipToOne.get_to_table();
 
 						} catch (NullPointerException e) {
 							// fieldUsedInRelationshipToOne is null
 							tableNameUsed = libglomLayoutItemField.get_table_used(tableName);
 						}
 
 						// Set the navigation table name only if it's not different than the current table name.
 						if (!tableName.equals(tableNameUsed)) {
 							layoutItemField.setNavigationTableName(tableNameUsed);
 						}
 					}
 
 					layoutItem = layoutItemField;
 
 				} else {
 					Log.info(documentID, tableName,
 							"Ignoring unknown details LayoutItem of type " + libglomLayoutItem.get_part_type_name()
 									+ ".");
 					continue;
 				}
 			}
 
 			layoutGroup.addItem(layoutItem);
 		}
 
 		return layoutGroup;
 	}
 
 	private GlomNumericFormat convertNumbericFormat(NumericFormat libglomNumericFormat) {
 		GlomNumericFormat gnf = new GlomNumericFormat();
 		gnf.setUseAltForegroundColourForNegatives(libglomNumericFormat.getM_alt_foreground_color_for_negatives());
 		gnf.setCurrencyCode(libglomNumericFormat.getM_currency_symbol());
 		gnf.setDecimalPlaces(Utils.safeLongToInt(libglomNumericFormat.getM_decimal_places()));
 		gnf.setDecimalPlacesRestricted(libglomNumericFormat.getM_decimal_places_restricted());
 		gnf.setUseThousandsSeparator(libglomNumericFormat.getM_use_thousands_separator());
 		return gnf;
 	}
 
 	private Formatting convertFormatting(FieldFormatting libglomFormatting) {
 		Formatting formatting = new Formatting();
 
 		// horizontal alignment
 		Formatting.HorizontalAlignment horizontalAlignment;
 		switch (libglomFormatting.get_horizontal_alignment()) {
 		case HORIZONTAL_ALIGNMENT_LEFT:
 			horizontalAlignment = Formatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_LEFT;
 		case HORIZONTAL_ALIGNMENT_RIGHT:
 			horizontalAlignment = Formatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_RIGHT;
 		case HORIZONTAL_ALIGNMENT_AUTO:
 		default:
 			horizontalAlignment = Formatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_AUTO;
 		}
 		formatting.setHorizontalAlignment(horizontalAlignment);
 
 		// text colour
 		String foregroundColour = libglomFormatting.get_text_format_color_foreground();
 		if (foregroundColour != null && !foregroundColour.isEmpty())
 			formatting.setTextFormatColourForeground(convertGdkColorToHtmlColour(foregroundColour));
 		String backgroundColour = libglomFormatting.get_text_format_color_background();
 		if (backgroundColour != null && !backgroundColour.isEmpty())
 			formatting.setTextFormatColourBackground(convertGdkColorToHtmlColour(backgroundColour));
 
 		// multiline
 		if (libglomFormatting.get_text_format_multiline()) {
 			formatting.setTextFormatMultilineHeightLines(Utils.safeLongToInt(libglomFormatting
 					.get_text_format_multiline_height_lines()));
 		}
 
 		return formatting;
 	}
 
 	private LayoutItemField convertToGWTGlomLayoutItemField(LayoutItem_Field libglomLayoutItemField,
 			boolean includeFormatting) {
 		LayoutItemField layoutItemField = new LayoutItemField();
 
 		// set type
 		layoutItemField.setType(convertToGWTGlomFieldType(libglomLayoutItemField.get_glom_type()));
 
 		// set title and name
 		layoutItemField.setTitle(libglomLayoutItemField.get_title_or_name());
 		layoutItemField.setName(libglomLayoutItemField.get_name());
 
 		if (includeFormatting) {
 			FieldFormatting glomFormatting = libglomLayoutItemField.get_formatting_used();
 			Formatting formatting = convertFormatting(glomFormatting);
 
 			// create a GlomNumericFormat DTO for numeric values
 			if (libglomLayoutItemField.get_glom_type() == org.glom.libglom.Field.glom_field_type.TYPE_NUMERIC) {
 				formatting.setGlomNumericFormat(convertNumbericFormat(glomFormatting.getM_numeric_format()));
 			}
 			layoutItemField.setFormatting(formatting);
 		}
 
 		return layoutItemField;
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
 	 * Converts a Gdk::Color (16-bits per channel) to an HTML colour (8-bits per channel) by discarding the least
 	 * significant 8-bits in each channel.
 	 */
 	private String convertGdkColorToHtmlColour(String gdkColor) {
 		if (gdkColor.length() == 13)
 			return gdkColor.substring(0, 3) + gdkColor.substring(5, 7) + gdkColor.substring(9, 11);
 		else if (gdkColor.length() == 7) {
 			// This shouldn't happen but let's deal with it if it does.
 			Log.warn(documentID,
 					"Expected a 13 character string but received a 7 character string. Returning received string.");
 			return gdkColor;
 		} else {
 			Log.error("Did not receive a 13 or 7 character string. Returning black HTML colour code.");
 			return "#000000";
 		}
 	}
 
 	/*
 	 * This method converts a LayoutItem_Portal.navigation_type from java-libglom to the equivalent
 	 * LayoutItemPortal.NavigationType from Online Glom. This conversion is required because the LayoutItem_Portal class
 	 * from java-libglom can't be used with GWT-RPC. An enum identical to LayoutItem_Portal.navigation_type from
 	 * java-libglom is included in the LayoutItemPortal data transfer object.
 	 */
 	private LayoutItemPortal.NavigationType convertToGWTGlomNavigationType(
 			LayoutItem_Portal.navigation_type navigationType) {
 		switch (navigationType) {
 		case NAVIGATION_NONE:
 			return LayoutItemPortal.NavigationType.NAVIGATION_NONE;
 		case NAVIGATION_AUTOMATIC:
 			return LayoutItemPortal.NavigationType.NAVIGATION_AUTOMATIC;
 		case NAVIGATION_SPECIFIC:
 			return LayoutItemPortal.NavigationType.NAVIGATION_SPECIFIC;
 		default:
 			Log.error("Recieved an unknown NavigationType: " + LayoutItem_Portal.navigation_type.class.getName() + "."
 					+ navigationType.toString() + ". Returning " + LayoutItemPortal.NavigationType.NAVIGATION_AUTOMATIC
 					+ ".");
 			return LayoutItemPortal.NavigationType.NAVIGATION_AUTOMATIC;
 		}
 	}
 
 	/**
 	 * Gets the table name to use when accessing the database and the document. This method guards against SQL injection
 	 * attacks by returning the default table if the requested table is not in the database or if the table name has not
 	 * been set.
 	 * 
 	 * @param tableName
 	 *            The table name to validate.
 	 * @return The table name to use.
 	 */
 	private String getTableNameToUse(String tableName) {
 		if (tableName == null || tableName.isEmpty() || !document.get_table_is_known(tableName)) {
 			return document.get_default_table();
 		}
 		return tableName;
 	}
 
 }
