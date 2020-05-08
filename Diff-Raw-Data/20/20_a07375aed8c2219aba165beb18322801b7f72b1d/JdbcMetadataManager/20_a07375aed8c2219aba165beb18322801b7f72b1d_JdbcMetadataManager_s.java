 //;-*- mode: java -*-
 /*
                         QueryJ
 
     Copyright (C) 2001-2006  Jose San Leandro Armendariz
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
 package org.acmsl.queryj.tools.metadata.engines;
 
 /*
  * Importing some ACM-SL classes.
  */
 import org.acmsl.queryj.Field;
 import org.acmsl.queryj.QueryJException;
 import org.acmsl.queryj.tools.metadata.MetadataManager;
 import org.acmsl.queryj.tools.metadata.MetadataTypeManager;
 import org.acmsl.queryj.tools.metadata.ProcedureMetadata;
 import org.acmsl.queryj.tools.metadata.ProcedureParameterMetadata;
 
 /*
  * Importing some JDK classes.
  */
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Manages the information provided by database metadata, using plain JDBC.
  * @author <a href="mailto:chous@acm-sl.org"
            >Jose San Leandro</a>
  */
 public class JdbcMetadataManager
     extends AbstractJdbcMetadataManager
     implements MetadataManager
 {
     /**
      * Creates an empty <code>JdbcMetadataManager</code> instance..
      */
     public JdbcMetadataManager()
     {
         super();
     }
 
     /**
      * Creates a <code>JdbcMetadataManager</code> using given information.
      * @param tableNames explicitly specified table names.
      * @param procedureNames explicitly specified procedure names.
      * @param disableTableExtraction <code>true</code> to disable table
      * extraction.
      * @param lazyTableExtraction if the table metadata should not be
      * retrieved from the database until the tables to extract are
      * specified.
      * @param disableProcedureExtraction <code>true</code> to disable
      * procedure extraction.
      * @param lazyProcedureExtraction if the procedure metadata should not
      * be retrieved from the database until the procedures to extract are
      * specified.
      * @param metaData the database meta data.
      * @param catalog the database catalog.
      * @param schema the database schema.
      * @throws SQLException if the database operation fails.
      * @throws QueryJException if an error, which is identified by QueryJ,
      * occurs.
      * @precondition tableNames != null
      * @precondition procedureNames != null
      * @precondition metaData != null
      */
     public JdbcMetadataManager(
         final String[] tableNames,
         final String[] procedureNames,
         final boolean disableTableExtraction,
         final boolean lazyTableExtraction,
         final boolean disableProcedureExtraction,
         final boolean lazyProcedureExtraction,
         final DatabaseMetaData metaData,
         final String catalog,
         final String schema)
       throws  SQLException,
               QueryJException
     {
         super(
             tableNames,
             procedureNames,
             disableTableExtraction,
             lazyTableExtraction,
             disableProcedureExtraction,
             lazyProcedureExtraction,
             metaData,
             catalog,
             schema);
     }
 
     /**
      * Creates a <code>JdbcMetadataManager</code> using given information.
      * @param tableNames explicitly specified table names.
      * @param procedureNames explicitly specified procedure names.
      * @param metaData the database meta data.
      * @param catalog the database catalog.
      * @param schema the database schema.
      * @throws SQLException if the database operation fails.
      * @throws QueryJException if an error, which is identified by QueryJ,
      * occurs.
      * @precondition tableNames != null
      * @precondition procedureNames != null
      * @precondition metaData != null
      */
     protected JdbcMetadataManager(
         final String[] tableNames,
         final String[] procedureNames,
         final DatabaseMetaData metaData,
         final String catalog,
         final String schema)
       throws  SQLException,
               QueryJException
     {
         this(
             tableNames,
             procedureNames,
             false,
             false,
             false,
             false,
             metaData,
             catalog,
             schema);
     }
 
     /**
      * Retrieves the metadata.
      * @throws SQLException if the database operation fails.
      * @throws QueryJException if an error, which is identified by QueryJ,
      * occurs.
      */
     public void retrieveMetadata()
       throws  SQLException,
               QueryJException
     {
         retrieveMetadata(
             getTableNames(),
             getProcedureNames(),
             getDisableTableExtraction(),
             getLazyTableExtraction(),
             getDisableProcedureExtraction(),
             getLazyProcedureExtraction(),
             getMetaData(),
             getCatalog(),
             getSchema());
     }
 
 
     /**
      * Retrieves the type manager.
      * @return such instance.
      */
     public MetadataTypeManager getMetadataTypeManager()
     {
         return JdbcMetadataTypeManager.getInstance();
     }
 
     /**
      * Retrieves the name identifying the manager instance.
      * @return such name.
      */
     public String getName()
     {
         return "jdbc";
     }
 
     /**
      * Checks whether the engine requires specific BLOB handling.
      * @return <code>true</code> in such case.
      */
     public boolean requiresCustomBlobHandling()
     {
         return false;
     }
 
     /**
      * Checks whether the engine requires specific CLOB handling.
      * @return <code>true</code> in such case.
      */
     public boolean requiresCustomClobHandling()
     {
         return false;
     }
 }
