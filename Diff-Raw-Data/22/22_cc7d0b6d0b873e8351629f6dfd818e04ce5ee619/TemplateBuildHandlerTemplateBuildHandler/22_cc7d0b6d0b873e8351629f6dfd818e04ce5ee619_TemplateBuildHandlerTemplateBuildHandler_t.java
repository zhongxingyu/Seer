 /*
                         queryj
 
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
  * Filename: TemplateBuildHandlerTemplateBuildHandler.java
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Build handler to build template build handler templates.
  *
  * Date: 2013/08/20
  * Time: 06:15
  *
  */
 package org.acmsl.queryj.templates.packaging.handlers;
 
 /*
  * Importing QueryJ-Core classes.
  */
 import org.acmsl.queryj.QueryJCommand;
 import org.acmsl.queryj.QueryJCommandWrapper;
 import org.acmsl.queryj.templates.packaging.DefaultTemplatePackagingContext;
 import org.acmsl.queryj.templates.packaging.TemplateBuildHandlerTemplate;
 import org.acmsl.queryj.templates.packaging.TemplateBuildHandlerTemplateFactory;
 import org.acmsl.queryj.templates.packaging.TemplateDef;
 
 /*
  * Importing JetBrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 
 /*
  * Importing checkthread.org annotations.
  */
 import org.checkthread.annotations.ThreadSafe;
 
 /*
  * Importing JDK classes.
  */
 import java.util.List;
 
 /**
  * Build handler to build template build handler templates.
  * @author <a href="mailto:queryj@acm-sl.org">Jose San Leandro</a>
  * @since 3.0
  * Created: 2013/08/20 06:15
  */
 @ThreadSafe
 public class TemplateBuildHandlerTemplateBuildHandler
     extends TemplatePackagingBuildHandler
                 <TemplateBuildHandlerTemplate<DefaultTemplatePackagingContext>,
                  TemplateBuildHandlerTemplateFactory,
                  DefaultTemplatePackagingContext>
 {
     /**
      * Retrieves the template factory.
      * @return such instance.
      */
     @NotNull
     @Override
     protected TemplateBuildHandlerTemplateFactory retrieveTemplateFactory()
     {
         return TemplateBuildHandlerTemplateFactory.getInstance();
     }
 
     /**
      * Builds the context from given parameters.
      * @param templateDef the template def.
      * @param parameters the command with the parameters.
      * @return the template context.
      */
     @NotNull
     @Override
     protected DefaultTemplatePackagingContext buildContext(
         @NotNull final TemplateDef<String> templateDef, @NotNull final QueryJCommand parameters)
     {
         return buildDefaultContext(templateDef, parameters);
     }
 
     /**
      * Stores the templates in given attribute map.
      * @param templates the templates.
      * @param parameters the parameter map.
      */
     @Override
     protected void storeTemplates(
         @NotNull final List<TemplateBuildHandlerTemplate<DefaultTemplatePackagingContext>> templates,
         @NotNull final QueryJCommand parameters)
     {
         new QueryJCommandWrapper<List<TemplateBuildHandlerTemplate<DefaultTemplatePackagingContext>>>(parameters)
             .setSetting(TEMPLATE_BUILD_HANDLER_TEMPLATES, templates);
     }
 
     /**
      * Retrieves the template name, using the parameters if necessary.
      * @param parameters the parameters.
      * @return the template name.
      */
     @NotNull
     @Override
     protected String retrieveTemplateName(@NotNull final QueryJCommand parameters)
     {
        return "TemplateBuildHandler";
     }
 
     /**
      * Retrieves the output package for the generated file.
      * @param parameters the parameters.
      * @return such package.
      */
     @NotNull
     @Override
     protected String retrieveOutputPackage(@NotNull final QueryJCommand parameters)
     {
         return OUTPUT_PACKAGE + ".handlers";
     }
 }
