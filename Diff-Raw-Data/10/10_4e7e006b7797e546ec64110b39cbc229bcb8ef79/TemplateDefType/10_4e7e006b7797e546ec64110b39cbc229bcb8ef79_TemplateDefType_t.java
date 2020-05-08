 /*
                         QueryJ Template Packaging
 
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
  * Filename: TemplateDefType.java
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Allowed values for template def types.
  *
  * Date: 2013/08/14
  * Time: 09:03
  *
  */
 package org.acmsl.queryj.templates.packaging;
 
 /*
 * Importing ACM-SL Commons classes.
 */
import org.acmsl.commons.utils.EnumUtils;

/*
  * Importing checkthread.org annotations.
  */
 import org.checkthread.annotations.ThreadSafe;

/*
 * Importing Jetbrains annotations.
 */
 import org.jetbrains.annotations.NotNull;
 
 /**
  * Allowed values for template def types.
  * @author <a href="mailto:queryj@acm-sl.org">Jose San Leandro</a>
  * @since 3.0
  * Created: 2013/08/14 09:03
  */
 @ThreadSafe
 public enum TemplateDefType
 {
     PER_TABLE("per-table"),
     PER_REPOSITORY("per-repository"),
     PER_CUSTOM_RESULT("per-custom-result"),
     PER_FOREIGN_KEY("per-foreign-key"),
     PER_SQL("per-sql");
 
     /**
      * The type.
      */
     @NotNull
     private final String m__strType;
 
     /**
      * Creates a new type.
      * @param type the name of the type.
      */
     TemplateDefType(@NotNull final String type)
     {
         this.m__strType = type;
     }
 
     /**
      * Retrieves the name.
      * @return such name.
      */
     @NotNull
     public String getType()
     {
         return this.m__strType;
     }
 
     /**
      * Retrieves the template def type for given value.
      * @param type the value.
      * @return the enum.
      */
     public static TemplateDefType getEnumFromString(@NotNull final String type)
     {
         return EnumUtils.getInstance().getEnumFromString(TemplateDefType.class, type);
     }
 }
