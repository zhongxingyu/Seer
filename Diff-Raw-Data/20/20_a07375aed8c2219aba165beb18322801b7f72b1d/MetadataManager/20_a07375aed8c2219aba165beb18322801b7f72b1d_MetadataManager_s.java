 //;-*- mode: java -*-
 /*
                         QueryJ
 
     Copyright (C) 2001-2010  Jose San Leandro Armendariz
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
  * Filename: $RCSfile: $
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Manages the information provided by database metadata,
  *              using plain JDBC.
  *
  */
 package org.acmsl.queryj.tools.metadata;
 
 /*
  * Importing some ACM-SL classes.
  */
 import org.acmsl.queryj.tools.metadata.MetadataTypeManager;
 import org.acmsl.queryj.tools.metadata.ProcedureMetadata;
 import org.acmsl.queryj.tools.metadata.ProcedureParameterMetadata;
 import org.acmsl.queryj.QueryJException;
 
 /*
  * Importing some JDK classes.
  */
 import java.sql.DatabaseMetaData;
 import java.sql.SQLException;
 
 /**
  * Manages the information provided by database metadata, using plain JDBC.
  * @author <a href="mailto:chous@acm-sl.org"
            >Jose San Leandro</a>
  */
 public interface MetadataManager
 {
     /**
      * Retrieves the name identifying the manager instance.
      * @return such name.
      */
     public String getName();
 
     /**
      * Retrieves the metadata.
      * @throws SQLException if the database operation fails.
      * @throws QueryJException if an error, which is identified by QueryJ,
      * occurs.
      */
     public void retrieveMetadata()
       throws  SQLException,
               QueryJException;
 
     /**
      * Retrieves the database meta data.
      * @return such information.
      */
     public DatabaseMetaData getMetaData();
 
     /**
      * Retrieves the table names.
      * @return such names.
      */
     public String[] getTableNames();
 
     /**
      * Adds the comments of given table.
      * @param tableName the table name.
      * @param tableComment the table comment.
      */
     public void addTableComment(
         final String tableName, final String tableComment);
 
     /**
      * Retrieves the table comments.
      * @param tableName the table name.
      * @return the table comment.
      */
     public String getTableComment(final String tableName);
 
     /**
      * Adds the column names of given table.
      * @param tableName the table name.
      * @param columnNames the column names.
      */
     public void addColumnNames(
         final String tableName, final String[] columnNames);
 
     /**
      * Retrieves the column names.
      * @param tableName the table name.
      * @return the column names.
      */
     public String[] getColumnNames(final String tableName);
 
     /**
      * Retrieves the column type.
      * @param tableName the table name.
      * @param columnName the column name.
      * @return the column type.
      * @see java.sql.Types
      */
     public int getColumnType(
         final String tableName, final String columnName);
     
     /**
      * Adds a column type.
      * @param tableName the table name.
      * @param columnName the column name.
      * @param columnType the column type.
      */
     public void addColumnType(
         final String tableName,
         final String columnName,
         final int columnType);
 
     /**
      * Retrieves whether the attribute allows null values.
      * @param tableName the table name.
      * @param columnName the column name.
      * @return such condition.
      */
     public boolean allowsNull(final String tableName, final String columnName);
     
     /**
      * Retrieves the allow null.
      * @param tableName the table name.
      * @param allowNames the allow names.
      * @return the allow null.
      */
     public boolean allowsNull(
         final String tableName, final String[] allowNames);
 
     /**
      * Retrieves whether given attribute allows nulls.
      * @param tableName the table name.
      * @param attributeName the attribute name.
      * @return such information.
      */
     public boolean getAllowNull(
         final String tableName, final String attributeName);
 
     /**
      * Retrieves whether given attributes allow nulls.
      * @param tableName the table name.
      * @param attributeNames the attribute names.
      * @return such information.
      */
     public boolean getAllowNull(
         final String tableName, final String[] attributeNames);
 
     /**
      * Adds a null flag.
      * @param tableName the table name.
      * @param columnName the column name.
      * @param flag the flag.
      */
     public void addAllowNull(
         final String tableName,
         final String columnName,
         final boolean flag);
 
     /**
      * Adds a primary key.
      * @param tableName the table name.
      * @param columnName the column name.
      */
     public void addPrimaryKey(final String tableName, final String columnName);
 
     /**
      * Retrieves the number of columns building the primary key.
      * @param tableName the table name.
      * @return the primary keys.
      */
     public int getPrimaryKeyColumnCount(final String tableName);
     
     /**
      * Retrieves the primary key.
      * @param tableName the table name.
      * @return the primary key.
      */
     public String[] getPrimaryKey(final String tableName);
 
     /**
      * Checks whether given field belongs to the primary key or not.
      * @param tableName the table name.
      * @param fieldName the field name.
      * @return <code>true</code> if such field is part of what dentifies a
      * concrete row.
      */
     public boolean isPartOfPrimaryKey(
         final String tableName, final String fieldName);
 
     /**
      * Adds a foreign key.
      * @param tableName the table name.
      * @param columnNames the column names.
      * @param refTableName the referred table name.
      * @param refColumnNames the referred column names.
      */
     public void addForeignKey(
         final String tableName,
         final String[] columnNames,
         final String refTableName,
         final String[] refColumnNames);
 
     /**
      * Retrieves the tables referring to given table's.
      * @param tableName the table name.
      * @return such tables.
      */
     public String[] getReferringTables(final String tableName);
     
     /**
      * Retrieves the foreign keys of given table.
      * @param tableName the table name.
      * @return its foreign keys, a structure of<br/>
      * <pre>
      * [referredTableName1]
      *   {foreign-key1-attribute1,..foreign-key1-attributeN}
      * [referredTableName1]
      *   {foreign-key2-attribute1,..foreign-key2-attributeN}
      * ..
      * [referredTableName1]
      *   {foreign-keyN-attribute1,..foreign-keyN-attributeN}
      * ..
      * [referredTableNameN]
      *   {foreign-keyM-attribute1,..foreign-keyM-attributeN}
      * ..
      * [referredTableNameN]
      *   {foreign-keyM-attribute1,..foreign-keyM-attributeN}
      * </pre>
      */
     public String[][] getForeignKeys(final String tableName);
 
     /**
      * Checks whether given table contains foreign keys.
      * @param tableName the table name.
      * @return <code>true</code> in such case.
      */
     public boolean containsForeignKeys(final String tableName);
 
     /**
      * Retrieves the tables referred by given table's foreign keys.
      * @param tableName the table name.
      * @return such tables.
      */
     public String[] getReferredTables(final String tableName);
 
     /**
      * Retrieves the field of given table that points to a field on the referred one.
      * @param tableName the table name.
      * @param refTableName the referred table name.
      * @return such field.
      */
     public String[][] getForeignKeys(
         final String tableName, String refTableName);
 
     /**
      * Retrieves the table referred by given foreign key.
      * @param tableName the table name.
      * @param foreignKey the foreign key.
      * @return the referred table name.
      */
     public String getReferredTable(
         final String tableName, final String[] foreignKey);
     
     /**
      * Retrieves the field of referred table pointed by a field on the
      * original one.
      * @param tableName the table name.
      * @param refTableName the referred table name.
      * @return such field.
      */
     public String[][] getReferredKeys(
         final String tableName, String refTableName);
 
     /**
      * Annotates a externally-managed field.
      * @param tableName the table name.
      * @param columnName the column name.
      */
     public void addExternallyManagedField(
         final String tableName, final String columnName);
 
     /**
      * Annotates a externally-managed field.
      * @param tableName the table name.
      * @param columnName the column name.
      * @param keyword the keyword.
      */
     public void addExternallyManagedField(
         final String tableName,
         final String columnName,
         final String keyword);
 
     /**
      * Annotates a externally-managed field.
      * @param tableName the table name.
      * @param columnName the column name.
      * @param keyword the keyword.
      * @param query the retrieval query.
      */
     public void addExternallyManagedField(
         final String tableName,
         final String columnName,
         final String keyword,
         final String query);
     
     /**
      * Retrieves the externally-managed fields.
      * @param tableName the table name.
      * @return the externally-managed fields of such table.
      */
     public String[] getExternallyManagedFields(final String tableName);
 
     /**
      * Checks whether given field is externally managed or not.
      * @param tableName the table name.
      * @param fieldName the field name.
      * @return <code>true</code> if such field is managed externally.
      */
     public boolean isManagedExternally(
         final String tableName, String fieldName);
 
     /**
      * Retrieves the keyword used to create new values of given field.
      * @param tableName the table name.
      * @param fieldName the field name.
      * @return such keyword, or <code>null</code> if such information is
      * unknown.
      */
     public String getKeyword(final String tableName, String fieldName);
 
     /**
      * Retrieves the keyword used to create new values of given field.
      * @param tableName the table name.
      * @param fieldName the field name.
      * @return such keyword, or <code>null</code> if such information is
      * unknown.
      */
     public String getExternallyManagedFieldRetrievalQuery(
         final String tableName, String fieldName);
     
     /**
      * Retrieves the procedures metadata.
      * @return such metadata.
      */
     public ProcedureMetadata[] getProceduresMetadata();
 
     /**
      * Retrieves the parameters of given procedure.
      * @param procedure the procedure.
      * @return the parameters metadata.
      */
     public ProcedureParameterMetadata[] getProcedureParametersMetadata(
         final ProcedureMetadata procedure);
 
     /**
      * Retrieves the parameters of given procedure.
      * @param procedureName the procedure name.
      * @return the parameters metadata.
      */
     public ProcedureParameterMetadata[] getProcedureParametersMetadata(
         final String procedureName);
 
     /**
      * Retrieves the type manager.
      * @return such instance.
      */
     public MetadataTypeManager getMetadataTypeManager();
 
     /**
      * Checks whether the engine requires specific CLOB handling.
      * @return <code>true</code> in such case.
      */
     public boolean requiresCustomClobHandling();
 
     /**
      * Checks whether the engine requires specific BLOB handling.
      * @return <code>true</code> in such case.
      */
     public boolean requiresCustomBlobHandling();
 }
