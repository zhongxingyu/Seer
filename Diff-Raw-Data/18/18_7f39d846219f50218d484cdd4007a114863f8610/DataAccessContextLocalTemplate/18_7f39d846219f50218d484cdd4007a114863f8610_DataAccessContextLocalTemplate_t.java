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
     Contact info: jose.sanleandro@acm-sl.com
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
  * Description: Is able to generate the configuration file for configuring
  *              DAOChooser.
  *
  */
 package org.acmsl.queryj.tools.templates.dao;
 
 /*
  * Importing some project-specific classes.
  */
 import org.acmsl.queryj.tools.customsql.CustomSqlProvider;
 import org.acmsl.queryj.tools.metadata.CachingTableDecorator;
 import org.acmsl.queryj.tools.metadata.DecorationUtils;
 import org.acmsl.queryj.tools.metadata.DecoratorFactory;
 import org.acmsl.queryj.tools.metadata.MetadataManager;
 import org.acmsl.queryj.tools.metadata.MetadataTypeManager;
 import org.acmsl.queryj.tools.metadata.TableDecorator;
 import org.acmsl.queryj.tools.PackageUtils;
 import org.acmsl.queryj.tools.templates.BasePerRepositoryTemplate;
 
 /*
  * Importing StringTemplate classes.
  */
 import org.antlr.stringtemplate.StringTemplateGroup;
 
 /*
  * Importing some ACM-SL Commons classes.
  */
 import org.acmsl.commons.utils.StringUtils;
 
 /*
  * Importing StringTemplate classes.
  */
 import org.antlr.stringtemplate.StringTemplateGroup;
 
 /*
  * Importing some JDK classes.
  */
 import java.util.Collection;
 import java.util.Map;
 
 /**
  * Is able to generate the configuration file for configuring
  * DAOChooser.
  * @author <a href="mailto:chous@acm-sl.org"
  *         >Jose San Leandro</a>
  */
 public class DataAccessContextLocalTemplate
     extends  BasePerRepositoryTemplate
 {
     /**
      * The JNDI location.
      */
     private String m__strJNDILocation;
 
     /**
      * Builds a <code>DataAccessContextLocalTemplate</code> using given
      * information.
      * @param metadataManager the database metadata manager.
      * @param header the header.
      * @param decoratorFactory the <code>DecoratorFactory</code> instance.
      * @param packageName the package name.
      * @param basePackageName the base package name.
      * @param repositoryName the repository name.
      * @param engineName the engine name.
      * @param jndiLocation the JNDI location.
      * @param tables the tables.
      */
     public DataAccessContextLocalTemplate(
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager,
         final CustomSqlProvider customSqlProvider,
         final String header,
         final DecoratorFactory decoratorFactory,
         final String packageName,
         final String basePackageName,
         final String repositoryName,
         final String engineName,
         final String jndiLocation,
         final Collection tables)
     {
         super(
             metadataManager,
             metadataTypeManager,
             customSqlProvider,
             header,
             decoratorFactory,
             packageName,
             basePackageName,
             repositoryName,
             engineName,
             tables);
         immutableSetJNDILocation(jndiLocation);
     }
 
     /**
      * Specifies the JNDI location.
      * @param location such value.
      */
     private void immutableSetJNDILocation(final String location)
     {
         m__strJNDILocation = location;
     }
 
     /**
      * Specifies the JNDI location.
      * @param location such value.
      */
     protected void setJNDILocation(final String location)
     {
         immutableSetJNDILocation(location);
     }
 
     /**
      * Retrieves the JNDI location.
      * @return such value.
      */
     public String getJNDILocation()
     {
         return m__strJNDILocation;
     }
 
     /**
      * Retrieves the string template group.
      * @return such instance.
      */
     protected StringTemplateGroup retrieveGroup()
     {
         return retrieveGroup("/org/acmsl/queryj/DataAccessContextLocal.stg");
     }
 
     /**
      * Retrieves the template name.
      * @return such information.
      */
     public String getTemplateName()
     {
         return "DataAccessContextLocal";
     }
 
     /**
      * Fills the core parameters.
      * @param input the input.
      * @param metadataManager the database metadata manager.
      * @param customSqlProvider the <code>CustomSqlProvider</code> instance.
      * @param decoratorFactory the <code>DecoratorFactory</code> instance.
      * @param basePackageName the base package name.
      * @param subpackageName the subpackage name.
      * @param tableRepositoryName the table repository name.
      * @param engineName the engine name.
      * @param tables the tables.
      * @param timestamp the timestamp.
      * @param stringUtils the <code>StringUtils</code> instance.
      * @precondition input != null
      * @precondition metadataManager != null
      * @precondition customSqlProvider != null
      * @precondition decoratorFactory != null
      * @precondition subpackageName != null
      * @precondition basePackageName != null
      * @precondition tableRepositoryName != null
      * @precondition tables != null
      * @precondition timestamp != null
      * @precondition stringUtils != null
      */
     protected void fillCoreParameters(
         final Map input,
         final MetadataManager metadataManager,
         final CustomSqlProvider customSqlProvider,
         final DecoratorFactory decoratorFactory,
         final String basePackageName,
         final String subpackageName,
         final String tableRepositoryName,
         final String engineName,
         final Collection tables,
         final String timestamp,
         final StringUtils stringUtils)
     {
         super.fillCoreParameters(
             input,
             metadataManager,
             customSqlProvider,
             decoratorFactory,
             basePackageName,
             subpackageName,
             tableRepositoryName,
             engineName,
             tables,
             timestamp,
             stringUtils);
 
         input.put("engine_name", engineName);
         input.put("engine_name_lowercased", engineName.toLowerCase());
        String t_strProcessedHeader = getProcessedHeader(input);
        if  (t_strProcessedHeader != null)
        {
            input.put("splitted_header", split(t_strProcessedHeader));
        }
         fillJndiLocation(input, getJNDILocation());
     }
 
     /**
      * Fills the JNDI location.
      * @param input the input map.
      * @param jndiLocation the JNDI location.
      * @precondition input != null
      * @precondition jndiLocation != null
      */
     protected void fillJndiLocation(final Map input, final String jndiLocation)
     {
         input.put("jndi_location", jndiLocation);
     }
 
     /**
      * Splits given value into multiple lines.
      * @param value the text.
      * @return the splitted text.
      */
     protected String[] split(final String value)
     {
         return split(value, DecorationUtils.getInstance());
     }
     
     /**
      * Splits given value into multiple lines.
      * @param value the text.
      * @param decorationUtils the <code>DecorationUtils</code> instance.
      * @return the splitted text.
      */
     protected String[] split(
         final String value, final DecorationUtils decorationUtils)
     {
         return decorationUtils.split(value);
     }
 }
