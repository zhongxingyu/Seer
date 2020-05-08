 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
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
 package com.flexive.cmis.spi;
 
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.structure.FxProperty;
 import com.flexive.shared.structure.FxDataType;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.exceptions.FxRuntimeException;
 import com.flexive.shared.value.FxValue;
 import org.apache.chemistry.PropertyType;
 import org.apache.chemistry.BaseType;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Set;
 import java.util.HashSet;
 
 /**
  * Utility methods for the SPI implementation.
  * 
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class SPIUtils {
 
     private SPIUtils() {
     }
 
     public static boolean isFolderId(String objectId) {
         return objectId != null && objectId.indexOf('.') == -1;
     }
 
     public static boolean isDocumentId(String objectId) {
         return !isFolderId(objectId);
     }
 
     public static long getNodeId(String objectId) {
         return Long.parseLong(objectId);
     }
 
     public static FxPK getDocumentId(String objectId) {
         return FxPK.fromString(objectId);
     }
 
     public static Calendar convertDate(Object value) {
         if (value instanceof Calendar) {
             return (Calendar) value;
         }
         final Calendar cal = Calendar.getInstance();
         if (value instanceof FxValue) {
             final Object translation = ((FxValue) value).getBestTranslation();
             if (!(translation instanceof Date)) {
                 throw new IllegalArgumentException(
                         "Value '" + value + "' has type " + value.getClass() + " and cannot be converted to a Date."
                 );
             }
             cal.setTime((Date) translation);
         } else {
             cal.setTime((Date) value);
         }
         return cal;
     }
 
     public static Set<Long> getFolderTypeIds() {
        return new HashSet<Long>(
                FxSharedUtils.getSelectableObjectIdList(
                        CacheAdmin.getEnvironment().getType(FxType.FOLDER).getDerivedTypes(true, true)
                )
         );
     }
 
     public static PropertyType mapPropertyType(FxProperty property) {
         return mapFxDataType(property.getDataType());
     }
 
     private static PropertyType mapFxDataType(FxDataType dataType) {
         switch (dataType) {
             case String1024:
             case Text:
                 return PropertyType.STRING;
             case HTML:
                 return PropertyType.HTML;
             case Number:
             case LargeNumber:
                 return PropertyType.INTEGER;
             case Float:
             case Double:
                 return PropertyType.DECIMAL;
             case Boolean:
                 return PropertyType.BOOLEAN;
             case Date:
             case DateTime:
                 return PropertyType.DATETIME;
             case Reference:
                 return PropertyType.ID;
             case Binary:
                 return PropertyType.ID;
             case SelectOne:
             case SelectMany:
             case DateRange:
             case DateTimeRange:
                 return PropertyType.STRING; // TODO: ?
             default:
                 return PropertyType.STRING; // TODO
         }
     }
 
     public static Serializable convertValue(FxValue value, FxProperty property) {
         return convertValue(value, mapPropertyType(property));
     }
 
     public static Serializable convertValue(FxValue value, FlexivePropertyDefinition definition) {
         return convertValue(
                 (Serializable) value.getBestTranslation(),
                 mapPropertyType(definition.getAssignment().getProperty())
         );
     }
 
     public static Serializable convertValue(Object value, FxDataType type) {
         return convertValue(value, mapFxDataType(type));
     }
 
     public static Serializable convertValue(Object value, FxProperty property) {
         return convertValue(value, mapPropertyType(property));
     }
 
     public static Serializable convertValue(Object value, PropertyType type) {
         if (value == null) {
             return null;
         } else if (type == PropertyType.STRING || type == PropertyType.HTML
                 || type == PropertyType.XML || type == PropertyType.ID || type == PropertyType.URI) {
             return value.toString();
         } else if (type == PropertyType.DATETIME) {
             return convertDate(value);
         } else if (type == PropertyType.INTEGER) {
             return value instanceof Number ? ((Number) value).intValue() : Integer.valueOf(value.toString());
         } else if (type == PropertyType.DECIMAL) {
             return value instanceof Number ? ((Number) value).doubleValue() : Double.valueOf(value.toString());
         } else if (type == PropertyType.BOOLEAN) {
             return value instanceof Boolean ? (Boolean) value : Boolean.valueOf(value.toString());
         } else {
             throw new IllegalArgumentException("No conversion available for value '" + value
                     + "' of class " + value.getClass()
                     + " for property " + type);
         } 
     }
 
     public static boolean typeContainsAssignment(FxType type, String xpath) {
         try {
             type.getPropertyAssignment(xpath);
             return true;
         } catch (FxRuntimeException e) {
             return false;
         }
     }
 
     /**
      * Map a CMIS type name to the flexive type name.
      *
      * @param typeId    the CMIS type ID
      * @return          the corresponding flexive type
      */
     public static String getFxTypeName(String typeId) {
         if (BaseType.DOCUMENT.toString().equalsIgnoreCase(typeId)) {
             // TODO: this should actually the root type, but the root type isn't really usable here
             return FxType.DOCUMENT;
         } else if (BaseType.FOLDER.toString().equalsIgnoreCase(typeId)) {
             return FxType.FOLDER;
         } else {
             return typeId;
         }
     }
 }
