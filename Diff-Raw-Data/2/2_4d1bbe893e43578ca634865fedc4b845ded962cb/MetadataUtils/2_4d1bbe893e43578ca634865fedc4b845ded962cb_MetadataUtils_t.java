 //;-*- mode: java -*-
 /*
                         QueryJ
 
     Copyright (C) 2002-2005  Jose San Leandro Armendariz
                         chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: chous@acm-sl.org
     Postal Address: c/Playa de Lagoa, 1
                     Urb. Valdecabanas
                     Boadilla del monte
                     28660 Madrid
                     Spain
 
  ******************************************************************************
  *
  * Filename: $RCSfile$
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Provides some methods commonly-reused when working with
  *              metadata.
  */
 package org.acmsl.queryj.tools.metadata;
 
 /*
  * Importing some project-specific classes.
  */
 import org.acmsl.queryj.tools.metadata.AttributeDecorator;
 import org.acmsl.queryj.tools.metadata.DecorationUtils;
 import org.acmsl.queryj.tools.metadata.MetadataManager;
 import org.acmsl.queryj.tools.metadata.MetadataTypeManager;
 import org.acmsl.queryj.tools.metadata.vo.ForeignKey;
 
 /*
  * Importing ACM-SL Commons classes.
  */
 import org.acmsl.commons.patterns.Utils;
 
 /*
  * Importing some JDK classes.
  */
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Provides some methods commonly-used when working with metadata.
  * @author <a href="mailto:chous@acm-sl.org"
  *         >Jose San Leandro</a>
  */
 public class MetadataUtils
     implements Utils
 {
     /**
      * An empty <code>String</code> array.
      */
     public static final String[] EMPTY_STRING_ARRAY = new String[0];
 
     /**
      * An empty <code>ForeignKey</code> array.
      */
     public static final ForeignKey[] EMPTY_FOREIGNKEY_ARRAY = new ForeignKey[0];
 
     /**
      * Singleton implemented as a weak reference.
      */
     private static WeakReference singleton;
 
     /**
      * Protected constructor to avoid accidental instantiation.
      */
     protected MetadataUtils() {};
 
     /**
      * Specifies a new weak reference.
      * @param utils the utils instance to use.
      */
     protected static void setReference(final MetadataUtils utils)
     {
         singleton = new WeakReference(utils);
     }
 
     /**
      * Retrieves the weak reference.
      * @return such reference.
      */
     protected static WeakReference getReference()
     {
         return singleton;
     }
 
     /**
      * Retrieves a <code>MetadataUtils</code> instance.
      * @return such instance.
      */
     public static MetadataUtils getInstance()
     {
         MetadataUtils result = null;
 
         WeakReference reference = getReference();
 
         if  (reference != null) 
         {
             result = (MetadataUtils) reference.get();
         }
 
         if  (result == null) 
         {
             result = new MetadataUtils();
 
             setReference(result);
         }
 
         return result;
     }
 
     /**
      * Retrieves the primary key attributes.
      * @param tableName the table name.
      * @param metadataManager the metadata manager.
      * @param metadataTypeManager the metadata type manager.
      * @return the collection of attributes participating in the primary key.
      * @precondition tableName != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      */
     public Collection retrievePrimaryKeyAttributes(
         final String tableName,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager)
     {
         return
             buildAttributes(
                 metadataManager.getPrimaryKey(tableName),
                 tableName,
                 metadataManager,
                 metadataTypeManager);
     }
     
     /**
      * Retrieves the non-primary key attributes.
      * @param tableName the table name.
      * @param metadataManager the <code>MetadataManager</code>
      * instance.
      * @param metadataTypeManager the <code>MetadataTypeManager</code> instance.
      * @return the collection of attributes not participating in the primary
      * key.
      * @precondition tableName != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      */
     public Collection retrieveNonPrimaryKeyAttributes(
         final String tableName,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager)
     {
         Collection t_cNonPkNames = new ArrayList();
         
         String[] t_astrColumnNames =
             metadataManager.getColumnNames(tableName);
 
         int t_iLength =
             (t_astrColumnNames != null) ? t_astrColumnNames.length : 0;
 
         for  (int t_iIndex = 0; t_iIndex < t_iLength; t_iIndex++)
         {
             if  (!metadataManager.isPartOfPrimaryKey(
                      tableName, t_astrColumnNames[t_iIndex]))
             {
                 t_cNonPkNames.add(t_astrColumnNames[t_iIndex]);
             }
         }
 
         return
             buildAttributes(
                 (String[]) t_cNonPkNames.toArray(EMPTY_STRING_ARRAY),
                 tableName,
                 metadataManager,
                 metadataTypeManager);
     }
 
     /**
      * Retrieves the foreign key attributes.
      * @param tableName the table name.
      * @param metadataManager the <code>MetadataManager</code>
      * instance.
      * @param metadataTypeManager the <code>MetadataTypeManager</code> instance.
      * @return the foreign key attributes.
      * @precondition tableName != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      */
     public Collection retrieveForeignKeyAttributes(
         final String tableName,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager)
     {
         Collection result = new ArrayList();
 
         String[][] t_aastrForeignKeys =
             metadataManager.getForeignKeys(tableName);
 
         int t_iLength =
             (t_aastrForeignKeys != null) ? t_aastrForeignKeys.length : 0;
 
         for  (int t_iIndex = 0; t_iIndex < t_iLength; t_iIndex++)
         {
             result.add(
                 buildAttributes(
                     t_aastrForeignKeys[t_iIndex],
                     tableName,
                     metadataManager,
                     metadataTypeManager));
         }
 
         return result;
     }
 
     /**
      * Retrieves the complete attribute list.
      * @param tableName the table name.
      * @param metadataManager the <code>MetadataManager</code>
      * instance.
      * @param metadataTypeManager the <code>MetadataTypeManager</code> instance.
      * @return the attributes.
      * @precondition tableName != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      */
     public Collection retrieveAttributes(
         final String tableName,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager)
     {
         return
             buildAttributes(
                 metadataManager.getColumnNames(tableName),
                 tableName,
                 metadataManager,
                 metadataTypeManager);
     }
 
     /**
      * Retrieves the externally-managed attributes.
      * @param tableName the table name.
      * @param metadataManager the <code>MetadataManager</code>
      * instance.
      * @param metadataTypeManager the <code>MetadataTypeManager</code> instance.
      * @return the externally-managed attributes.
      * @precondition tableName != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      */
     public Collection retrieveExternallyManagedAttributes(
         final String tableName,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager)
     {
         Collection t_cExternallyManagedAttributeNames = new ArrayList();
         
         String[] t_astrColumnNames =
             metadataManager.getColumnNames(tableName);
 
         int t_iLength =
             (t_astrColumnNames != null) ? t_astrColumnNames.length : 0;
 
         for  (int t_iIndex = 0; t_iIndex < t_iLength; t_iIndex++)
         {
             if  (metadataManager.isManagedExternally(
                      tableName, t_astrColumnNames[t_iIndex]))
             {
                 t_cExternallyManagedAttributeNames.add(
                     t_astrColumnNames[t_iIndex]);
             }
         }
 
         return
             buildAttributes(
                 (String[])
                     t_cExternallyManagedAttributeNames.toArray(
                         EMPTY_STRING_ARRAY),
                 tableName,
                 metadataManager,
                 metadataTypeManager);
     }
 
     /**
      * Retrieves all but the externally-managed attributes.
      * @param tableName the table name.
      * @param metadataManager the <code>MetadataManager</code>
      * instance.
      * @param metadataTypeManager the <code>MetadataTypeManager</code> instance.
      * @return all but the externally-managed attributes.
      * @precondition tableName != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      */
     public Collection retrieveAllButExternallyManagedAttributes(
         final String tableName,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager)
     {
         Collection t_cNonExternallyManagedAttributeNames = new ArrayList();
         
         String[] t_astrColumnNames =
             metadataManager.getColumnNames(tableName);
 
         int t_iLength =
             (t_astrColumnNames != null) ? t_astrColumnNames.length : 0;
 
         for  (int t_iIndex = 0; t_iIndex < t_iLength; t_iIndex++)
         {
             if  (!metadataManager.isManagedExternally(
                      tableName, t_astrColumnNames[t_iIndex]))
             {
                 t_cNonExternallyManagedAttributeNames.add(
                     t_astrColumnNames[t_iIndex]);
             }
         }
 
         return
             buildAttributes(
                 (String[])
                     t_cNonExternallyManagedAttributeNames.toArray(
                         EMPTY_STRING_ARRAY),
                 tableName,
                 metadataManager,
                 metadataTypeManager);
     }
 
     /**
      * Retrieves the foreign key attributes.
      * @param tableName the table name.
      * @param metadataManager the <code>MetadataManager</code>
      * instance.
      * @param metadataTypeManager the <code>MetadataTypeManager</code> instance.
      * @return the foreign key attributes (a list of attribute lists,
      * grouped by referred tables.
      * @precondition tableName != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      */
     public Collection retrieveForeignKeys(
         final String tableName,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager)
     {
         Collection result = new ArrayList();
 
         String[] t_astrReferredTables =
             metadataManager.getReferredTables(tableName);
 
         String[] t_astrReferredColumns = null;
 
         int t_iLength =
             (t_astrReferredTables != null) ? t_astrReferredTables.length : 0;
 
         Collection t_cCurrentForeignKey = null;
 
         String t_strReferredTable = null;
 
         for  (int t_iRefTableIndex = 0;
                   t_iRefTableIndex < t_iLength;
                   t_iRefTableIndex++)
         {
             t_strReferredTable =
                 t_astrReferredTables[t_iRefTableIndex];
 
             String[][] t_aastrForeignKeys =
                 metadataManager.getForeignKeys(
                     t_strReferredTable, tableName);
 
             int t_iFkLength =
                 (t_aastrForeignKeys != null) ? t_aastrForeignKeys.length : 0;
 
             for  (int t_iIndex = 0; t_iIndex < t_iFkLength; t_iIndex++)
             {
                 t_cCurrentForeignKey =
                     buildAttributes(
                         t_aastrForeignKeys[t_iIndex],
                         t_strReferredTable,
                     (metadataManager.allowsNull(
                         t_strReferredTable, t_astrReferredColumns)
                      ?  Boolean.TRUE : Boolean.FALSE),
                     metadataManager,
                     metadataTypeManager);
 
                 // Note: 'result' contains a list of lists.
                 result.add(t_cCurrentForeignKey);
 
                 t_cCurrentForeignKey = null;
             }
         }
 
         return result;
     }
 
     /**
      * Retrieves the refering keys.
      * @param tableName the table name.
      * @param metadataManager the <code>MetadataManager</code>
      * instance.
      * @param metadataTypeManager the <code>MetadataTypeManager</code> instance.
      * @return the foreign keys of other tables pointing
      * to this one: 
      * a map of "fk_"referringTableName -> foreign_keys (list of attribute
      * lists).
      * @precondition tableName != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      */
     public Map retrieveReferingKeys(
         final String tableName,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager)
     {
         Map result = new HashMap();
 
         String[] t_astrReferingTables =
             metadataManager.getReferingTables(tableName);
 
         String[][] t_aastrReferingColumns = null;
 
         int t_iLength =
             (t_astrReferingTables != null) ? t_astrReferingTables.length : 0;
 
         Collection t_cReferingFks = null;
 
         Collection t_cCurrentForeignKey = null;
 
         String t_strReferingTable = null;
 
         for  (int t_iRefTableIndex = 0;
                   t_iRefTableIndex < t_iLength;
                   t_iRefTableIndex++)
         {
             t_cReferingFks = new ArrayList();
 
             t_strReferingTable =
                 t_astrReferingTables[t_iRefTableIndex];
 
             t_aastrReferingColumns =
                 metadataManager.getForeignKeys(
                     t_strReferingTable, tableName);
 
             int t_iFkCount =
                 (t_aastrReferingColumns != null)
                 ?  t_aastrReferingColumns.length
                 :  0;
 
             for  (int t_iFk = 0; t_iFk < t_iFkCount; t_iFk++)
             {
                 t_cCurrentForeignKey =
                     buildAttributes(
                         t_aastrReferingColumns[t_iFk],
                         t_strReferingTable,
                         (metadataManager.allowsNull(
                             t_strReferingTable,
                             t_aastrReferingColumns[t_iFk])
                          ?  Boolean.TRUE : Boolean.FALSE),
                         metadataManager,
                         metadataTypeManager);
 
                 // Note: 't_cReferingFks' contains a list of lists.
                 t_cReferingFks.add(t_cCurrentForeignKey);
 
                 t_cCurrentForeignKey = null;
             }
 
             result.put(t_strReferingTable, t_cReferingFks);
         }
 
         return result;
     }
 
     /**
      * Builds the attributes associated to given column names.
      * @param columnNames the column names.
      * @param tableName the table name.
      * @param metadataManager the <code>MetadataManager</code>
      * instance.
      * @param metadataTypeManager the <code>MetadataTypeManager</code> instance.
      * @return the attribute collection.
      * @precondition columnNames != null
      * @precondition tableName != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      */
     public Collection buildAttributes(
         final String[] columnNames,
         final String tableName,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager)
     {
         return
             buildAttributes(
                 columnNames,
                 new String[columnNames.length],
                 tableName,
                 metadataManager,
                 metadataTypeManager);
     }
 
     /**
      * Builds the attributes associated to given column names.
      * @param columnNames the column names.
      * @param columnValues the column values.
      * @param tableName the table name.
      * @param metadataManager the <code>MetadataManager</code>
      * instance.
      * @param metadataTypeManager the <code>MetadataTypeManager</code> instance.
      * @return the attribute collection.
      * @precondition columnNames != null
      * @precondition columnValues != null
      * @precondition tableName != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      */
     public Collection buildAttributes(
         final String[] columnNames,
         final String[] columnValues,
         final String tableName,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager)
     {
         return
             buildAttributes(
                 columnNames,
                 columnValues,
                 tableName,
                 null,
                 metadataManager,
                 metadataTypeManager);
     }
 
     /**
      * Builds the attributes associated to given column names.
      * @param columnNames the column names.
      * @param tableName the table name.
      * @param allowsNullAsAWhole whether given column names can be null
      * as a whole or not.
      * @param metadataManager the <code>MetadataManager</code>
      * instance.
      * @param metadataTypeManager the <code>MetadataTypeManager</code>
      * instance.
      * @return the attribute collection.
      * @precondition columnNames != null
      * @precondition tableName != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      */
     public Collection buildAttributes(
         final String[] columnNames,
         final String tableName,
         final Boolean allowsNullAsAWhole,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager)
     {
         return
             buildAttributes(
                 columnNames,
                 new String[columnNames.length],
                 tableName,
                 allowsNullAsAWhole,
                 metadataManager,
                 metadataTypeManager);
     }
 
     /**
      * Builds the attributes associated to given column names.
      * @param columnNames the column names.
      * @param columnValues the column values.
      * @param tableName the table name.
      * @param allowsNullAsAWhole whether given column names can be null
      * as a whole or not.
      * @param metadataManager the <code>MetadataManager</code>
      * instance.
      * @param metadataTypeManager the <code>MetadataTypeManager</code>
      * instance.
      * @return the attribute collection.
      * @precondition columnNames != null
      * @precondition tableName != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      */
     public Collection buildAttributes(
         final String[] columnNames,
         final String[] columnValues,
         final String tableName,
         final Boolean allowsNullAsAWhole,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager)
     {
         Collection result = new ArrayList();
         
         int t_iLength = (columnNames != null) ? columnNames.length : 0;
 
         for  (int t_iIndex = 0; t_iIndex < t_iLength; t_iIndex++)
         {
             int t_iType =
                 metadataManager.getColumnType(
                     tableName, columnNames[t_iIndex]);
 
             String t_strNativeType =
                 metadataTypeManager.getNativeType(t_iType);
 
             boolean t_bAllowsNull = false;
 
             if  (allowsNullAsAWhole != null)
             {
                 t_bAllowsNull = allowsNullAsAWhole.booleanValue();
             }
             else
             {
                 t_bAllowsNull =
                     metadataManager.allowsNull(
                         tableName, columnNames[t_iIndex]);
             }
 
             String t_strFieldType =
                 metadataTypeManager.getFieldType(t_iType, t_bAllowsNull);
 
             boolean t_bManagedExternally =
                 metadataManager.isManagedExternally(
                     tableName, columnNames[t_iIndex]);
 
             result.add(
                 new AttributeDecorator(
                     columnNames[t_iIndex],
                     t_iType,
                     t_strNativeType,
                     t_strFieldType,
                     tableName,
                     t_bManagedExternally,
                     t_bAllowsNull,
                     columnValues[t_iIndex],
                     metadataManager,
                     metadataTypeManager));
         }
         
         return result;
     }
 
     /**
      * Retrieves the foreign keys starting at given table.
      * @param tableName the table name.
      * @param metadataManager the <code>MetadataManager</code> instance.
      * @return the foreign keys.
      * @precondition tableName != null
      * @precondition metadataManager != null
      */
     public ForeignKey[] retrieveForeignKeys(
         final String tableName, final MetadataManager metadataManager)
     {
         return
             retrieveFks(
                 tableName,
                 metadataManager,
                 metadataManager.getMetadataTypeManager());
     }
 
     /**
      * Retrieves the foreign keys starting at given table.
      * @param tableName the table name.
      * @param metadataManager the <code>MetadataManager</code> instance.
      * @param metadataTypeManager the <code>MetadataTypeManager</code> instance.
      * @return the foreign keys.
      * @precondition tableName != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      */
     protected ForeignKey[] retrieveFks(
         final String tableName,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager)
     {
         Collection result = new ArrayList();
         
         String[] t_astrReferredTables =
             metadataManager.getReferredTables(tableName);
 
         int t_iLength =
             (t_astrReferredTables != null) ? t_astrReferredTables.length : 0;
 
         for  (int t_iIndex = 0; t_iIndex < t_iLength; t_iIndex++)
         {
             String[][] t_aastrForeignKeys =
                 metadataManager.getForeignKeys(
                     tableName, t_astrReferredTables[t_iIndex]);
 
             int t_iFkCount =
                 (t_aastrForeignKeys != null) ? t_aastrForeignKeys.length : 0;
 
             for  (int t_iFkIndex = 0; t_iFkIndex < t_iFkCount; t_iFkIndex++)
             {
                 result.add(
                     new ForeignKey(
                         tableName,
                         buildAttributes(
                             t_aastrForeignKeys[t_iFkIndex],
                             tableName,
                             metadataManager,
                             metadataTypeManager),
                         t_astrReferredTables[t_iIndex]));
             }
         }
 
         return (ForeignKey[]) result.toArray(EMPTY_FOREIGNKEY_ARRAY);
     }
 
     /**
      * Retrieve all but the LOB attributes.
      * @param tableName the table name.
      * @param metadataManager the metadata manager.
      * @param metadataTypeManager the metadata type manager.
      * @return such attributes.
      * @precondition tableName != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      */
     public Collection retrieveAllButLobAttributes(
         final String tableName,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager)
     {
         Collection t_cLobAttributeNames = new ArrayList();
         
         String[] t_astrColumnNames =
             metadataManager.getColumnNames(tableName);
 
         int t_iLength =
             (t_astrColumnNames != null) ? t_astrColumnNames.length : 0;
 
         for  (int t_iIndex = 0; t_iIndex < t_iLength; t_iIndex++)
         {
            if  (!metadataTypeManager.isClob(
                      metadataManager.getColumnType(
                          tableName, t_astrColumnNames[t_iIndex])))
             {
                 t_cLobAttributeNames.add(t_astrColumnNames[t_iIndex]);
             }
         }
 
         return
             buildAttributes(
                 (String[]) t_cLobAttributeNames.toArray(EMPTY_STRING_ARRAY),
                 tableName,
                 metadataManager,
                 metadataTypeManager);
     }
 }
