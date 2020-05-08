 /*
                         QueryJ
 
     Copyright (C) 2002-today  Jose San Leandro Armendariz
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
     Contact info: jose.sanleandro@acm-sl.com
 
  ******************************************************************************
  *
  * Filename: TableAttributeTypeImportsHandler.java
  *
  * Author: Jose San Leandro Armendariz (chous)
  *
  * Description: Resolves "column_type_imports" placeholders, to avoid declaring
  * unused imports
  *
  * Date: 6/20/12
  * Time: 2:54 AM
  *
  */
 package org.acmsl.queryj.templates.handlers.fillhandlers;
 
 /*
  * Importing some project classes.
  */
 import org.acmsl.queryj.metadata.MetadataManager;
 import org.acmsl.queryj.metadata.MetadataTypeManager;
 import org.acmsl.queryj.metadata.TableDAO;
 import org.acmsl.queryj.metadata.vo.Attribute;
 import org.acmsl.queryj.metadata.vo.Table;
 import org.acmsl.queryj.templates.BasePerTableTemplateContext;
 
 /*
  * Importing some JetBrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /*
  * Importing some JDK classes.
  */
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /*
  * Importing checkthread.org annotations.
  */
 import org.checkthread.annotations.ThreadSafe;
 
 /**
  * Resolves "column_type_imports" placeholders, to avoid declaring
  * unused imports.
  * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro</a>
  * @since 2012/06/20
  */
 @ThreadSafe
 public class TableAttributeTypeImportsHandler
     extends AbstractTemplateContextFillHandler<BasePerTableTemplateContext, List<String>>
 {
     /**
      * Creates a {@link TableAttributeTypeImportsHandler} using given {@link BasePerTableTemplateContext context}
      * @param context the context.
      */
     public TableAttributeTypeImportsHandler(@NotNull final BasePerTableTemplateContext context)
     {
         super(context);
     }
 
     /**
      * Retrieves "column_type_imports".
      * @return such value.
      */
     @NotNull
     @Override
     public String getPlaceHolder()
     {
         return "column_type_imports";
     }
 
     /**
      * Retrieves the list of JDK imports associated to the table attributes.
      * @return such list.
      */
     @NotNull
     @Override
     protected List<String> getValue(@NotNull final BasePerTableTemplateContext context)
     {
         return retrieveImports(context.getTableName(), context.getMetadataManager());
     }
 
     /**
      * Retrieves the list of JDK imports associated to the table attributes.
      * @param tableName the name of the table.
      * @param metadataManager the {@link MetadataManager} instance.
      * @return such list.
      */
     @NotNull
     protected List<String> retrieveImports(
         @NotNull final String tableName, @NotNull final MetadataManager metadataManager)
     {
         return retrieveImports(tableName,  metadataManager.getTableDAO(), metadataManager.getMetadataTypeManager());
     }
 
     /**
      * Retrieves the list of JDK imports associated to the table attributes.
      * @param tableName the name of the table.
      * @param tableDAO the {@link TableDAO} instance.
      * @param metadataTypeManager the {@link MetadataTypeManager} instance.
      * @return such list.
      */
     @NotNull
     public static List<String> retrieveImports(
         @NotNull final String tableName,
         @NotNull final TableDAO tableDAO,
         @NotNull final MetadataTypeManager metadataTypeManager)
     {
         @Nullable List<String> result = null;
 
         @Nullable Table t_Table = tableDAO.findByName(tableName);
 
         if (t_Table != null)
         {
             result = retrieveImports(t_Table.getAttributes(), metadataTypeManager);
         }
 
         if (result == null)
         {
             result = new ArrayList<String>(0);
         }
 
         return result;
     }
 
     /**
      * Retrieves the list of JDK imports associated to the table attributes.
      * @param attributes the name of the table.
      * @param metadataTypeManager the {@link MetadataTypeManager} instance.
      * @return such list.
      */
     @NotNull
     protected static List<String> retrieveImports(
         @NotNull final List<Attribute> attributes, @NotNull final MetadataTypeManager metadataTypeManager)
     {
         List<String> result = new ArrayList<String>();
 
         String t_strImport;
 
         for (@Nullable Attribute t_Attribute : attributes)
         {
             if (t_Attribute != null)
             {
                 t_strImport =
                     metadataTypeManager.getFullyQualifiedType(
                         t_Attribute.getTypeId(), t_Attribute.isBoolean());
 
                 if (   (!metadataTypeManager.inJavaLang(t_strImport))
                    && (!metadataTypeManager.isPrimitive(t_strImport))
                     && (!result.contains(t_strImport)))
                 {
                     result.add(t_strImport);
 
                     if (Timestamp.class.getName().equals(t_strImport))
                     {
                         result.add(Date.class.getName());
                     }
                 }
             }
         }
 
         return result;
     }
 }
