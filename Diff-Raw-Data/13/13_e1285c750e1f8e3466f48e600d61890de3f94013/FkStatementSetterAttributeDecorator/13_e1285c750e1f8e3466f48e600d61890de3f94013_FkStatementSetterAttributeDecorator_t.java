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
 
  *****************************************************************************
  *
  * Filename: FkStatementSetterAttributeDecorator.java
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Decorates attributes for FkStatementSetter template.
  *
  */
 package org.acmsl.queryj.tools.templates.dao;
 
 /*
  * Importing project classes.
  */
 import org.acmsl.queryj.tools.metadata.CachingAttributeDecorator;
 import org.acmsl.queryj.tools.metadata.MetadataManager;
 import org.acmsl.queryj.tools.metadata.MetadataTypeManager;
 import org.acmsl.queryj.tools.metadata.vo.Attribute;
 
 /*
  * Importing some JetBrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /**
  * Decorates attributes for FkStatementSetter template.
  * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro Armendariz</a>
  */
 public class FkStatementSetterAttributeDecorator
     extends  CachingAttributeDecorator
 {
     /**
      * Creates a <code>FkStatementSetterAttributeDecorator</code> with the
      * <code>Attribute</code> to decorate.
      * @param attribute the attribute.
      * @param metadataManager the metadata manager.
      */
     public FkStatementSetterAttributeDecorator(
         final Attribute attribute,
         final MetadataManager metadataManager)
     {
         super(attribute, metadataManager);
     }
 
     /**
      * Retrieves the Java type of the attribute, which would be
      * only a primitive Java type if the attribute type matches,
      * and the column allows nulls.
      * @return such information.
      */
     @NotNull
     public String getType()
     {
         String result = getCachedType();
 
         if  (result == null)
         {
             result = retrieveType();
             setCachedType(result);
         }
 
         return result;
     }
 
     /**
      * Retrieves the object type.
      * @return such information.
      */
     @Nullable
     public String getObjectType()
     {
         return getObjectType(getTypeId(), getMetadataTypeManager());
     }
 
     /**
      * Retrieves the attribute's object type.
      * @param type the attribute type.
      * @param metadataTypeManager the metadata type manager.
      * @return such type.
      */
     @Nullable
     protected String getObjectType(
         final int type, @NotNull final MetadataTypeManager metadataTypeManager)
     {
         return metadataTypeManager.getStatementSetterFieldType(type);
     }
 }
