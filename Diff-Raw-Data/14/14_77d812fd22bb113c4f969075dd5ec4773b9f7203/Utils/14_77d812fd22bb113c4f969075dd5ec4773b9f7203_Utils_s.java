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
 
 import java.io.File;
 
 import org.glom.libglom.Field.glom_field_type;
 import org.glom.libglom.Value;
 import org.glom.web.shared.TypedDataItem;
 import org.glom.web.shared.layout.LayoutItemField.GlomFieldType;
 
 /**
  * @author Ben Konrath <ben@bagu.org>
  * 
  */
 public class Utils {
 
 	/*
 	 * This method safely converts longs from libglom into ints. This method was taken from stackoverflow:
 	 * 
 	 * http://stackoverflow.com/questions/1590831/safely-casting-long-to-int-in-java
 	 */
 	public static int safeLongToInt(long value) {
 		if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
 			throw new IllegalArgumentException(value + " cannot be cast to int without changing its value.");
 		}
 		return (int) value;
 	}
 
 	public static String getFileName(String fileURI) {
 		String[] splitURI = fileURI.split(File.separator);
 		return splitURI[splitURI.length - 1];
 	}
 
 	public static Value getGlomTypeGdaValueForTypedDataItem(String documentID, String tableName,
 			glom_field_type glomType, TypedDataItem dataItem) {
 		Value gdaValue = null;
 
 		switch (glomType) {
 		case TYPE_NUMERIC:
 
 			if (dataItem.isEmpty()) {
 				// No data has been set on the TypedDataItem. Use an empty value.
 				gdaValue = new Value();
 
 			} else if (dataItem.getType() == GlomFieldType.TYPE_NUMERIC) {
 				// non-empty data, numeric type:
 				// Trust the data in the TypedDataItem because the types match.
 				gdaValue = new Value(dataItem.getNumber());
 
 			} else if (dataItem.getType() == GlomFieldType.TYPE_INVALID) {
 				// non-empty data, invalid type:
 				// An invalid type that's not empty indicates that the TypeDataItem has been created from a URL string.
 				// The string will be converted into the Glom type (numeric).
 				try {
 					// non-locale specific string-to-number conversion:
 					// http://docs.oracle.com/javase/6/docs/api/java/lang/Double.html#valueOf%28java.lang.String%29
 					gdaValue = new Value(Double.parseDouble(dataItem.getUnknown()));
 				} catch (Exception e) {
 					// Use an empty Value when the number conversion doesn't work.
 					gdaValue = new Value();
 				}
 
 			} else {
 				// non-empty data, mis-matched types:
 				// Don't use the data when the type doesn't match the type from the Glom document.
				Log.warn(documentID, tableName, "The data type: " + dataItem.getType()
 						+ " doesn't match the type from the Glom document: " + glomType + ".");
				Log.warn(documentID, tableName, "The data item is being ignored.");
 
 				gdaValue = new Value(); // an empty Value
 			}
 			break;
 
 		case TYPE_TEXT:
 
 			if (dataItem.isEmpty()) {
 				// No data has been set on the TypedDataItem. Use an empty string value.
 				gdaValue = new Value("");
 
 			} else if (dataItem.getType() == GlomFieldType.TYPE_TEXT) {
 				// non-empty data, text type:
 				// Trust the data in the TypedDataItem because the types match.
 				gdaValue = new Value(dataItem.getText());
 
 			} else if (dataItem.getType() == GlomFieldType.TYPE_INVALID) {
 				// non-empty data, invalid type:
 				// An invalid type that's not empty indicates that primary key value has been created from a URL string.
 				// The string will be converted into the Glom type (text).
 				gdaValue = new Value(dataItem.getUnknown());
 
 			} else {
 				// non-empty data, mis-matched types:
 				// Don't use the primary key value when the type doesn't match the type from the Glom document.
				Log.warn(documentID, tableName, "The data type: " + dataItem.getType()
 						+ " doesn't match the expected type from the Glom document: " + glomType + ".");
				Log.warn(documentID, tableName, "The data item is being ignored.");
 				gdaValue = new Value(""); // an emtpy string Value
 			}
 			break;
 
 		default:
 			Log.error(documentID, tableName, "Unable to create a Gda Value of type: " + glomType
 					+ " based on data of type: " + dataItem.getType() + ".");
			Log.warn(documentID, tableName, "The data item is being ignored.");
 			gdaValue = new Value(); // an empty Value
 			break;
 		}
 
 		return gdaValue;
 	}
 }
