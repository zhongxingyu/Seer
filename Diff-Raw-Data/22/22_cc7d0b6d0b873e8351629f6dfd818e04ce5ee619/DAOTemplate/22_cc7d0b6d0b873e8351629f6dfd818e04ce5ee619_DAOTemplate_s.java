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
  * Filename: DAOTemplate.java
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Is able to create engine-specific DAO interfaces for each
  *              table in the persistence model.
  *
  */
 package org.acmsl.queryj.templates.dao;
 
 /*
  * Importing some project-specific classes.
  */
 import org.acmsl.queryj.api.AbstractBasePerTableTemplate;
 import org.acmsl.queryj.api.PerTableTemplateContext;
 
 /*
  * Importing StringTemplate classes.
  */
 import org.stringtemplate.v4.STGroup;
 
 /*
  * Importing JetBrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /*
  * Importing checkthread.org annotations.
  */
 import org.checkthread.annotations.ThreadSafe;
 
 /**
  * Is able to create engine-specific DAO interfaces for each
  * table in the persistence model.
  * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro Armendariz</a>
  */
 @ThreadSafe
 public class DAOTemplate
     extends AbstractBasePerTableTemplate<PerTableTemplateContext>
 {
 
     private static final long serialVersionUID = 3085645762313515830L;
 
     /**
      * Builds a {@link DAOTemplate} using given context.
      * @param context the {@link org.acmsl.queryj.api.PerTableTemplateContext} instance.
      */
     public DAOTemplate(@NotNull final PerTableTemplateContext context)
     {
         super(context);
     }
 
     /**
      * Retrieves the string template group.
      * @return such instance.
      */
     @Nullable
     @Override
     public STGroup retrieveGroup()
     {
        return retrieveGroup(DAO_GROUP + getTemplateName() + ".stg");
     }
 
     /**
      * Retrieves the template name.
      * @return such information.
      */
     @NotNull
     public String getTemplateName()
     {
         return "DAO";
     }
 }
             
