 //;-*- mode: java -*-
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
  * Filename: BaseRepositoryDAOTemplateGenerator.java
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Is able to generate the DAO repository interface.
  *
  */
 package org.acmsl.queryj.tools.templates;
 
 /*
  * Importing some project-specific classes.
  */
 import org.acmsl.queryj.tools.customsql.CustomSqlProvider;
 import org.acmsl.queryj.tools.metadata.CachingDecoratorFactory;
 import org.acmsl.queryj.tools.metadata.DecorationUtils;
 import org.acmsl.queryj.tools.metadata.DecoratorFactory;
 import org.acmsl.queryj.tools.metadata.MetadataManager;
 import org.acmsl.queryj.tools.metadata.MetadataTypeManager;
 import org.acmsl.queryj.tools.templates.RepositoryDAOTemplate;
 
 /*
  * Importing some ACM-SL classes.
  */
 import org.acmsl.commons.patterns.Singleton;
 import org.acmsl.commons.utils.io.FileUtils;
 import org.acmsl.commons.utils.StringUtils;
 
 /*
  * Importing some JDK classes.
  */
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.Collection;
 
 /**
  * Is able to generate the repository DAO interface.
  * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro Armendariz</a>
  */
 public class BaseRepositoryDAOTemplateGenerator
     implements  DefaultBasePerRepositoryTemplateFactory,
                 BasePerRepositoryTemplateGenerator,
                 Singleton
 {
     /**
      * Singleton implemented to avoid the double-checked locking.
      */
     private static class BaseRepositoryDAOTemplateGeneratorSingletonContainer
     {
         /**
          * The actual singleton.
          */
         public static final BaseRepositoryDAOTemplateGenerator SINGLETON =
             new BaseRepositoryDAOTemplateGenerator();
     }
 
     /**
      * Protected constructor to avoid accidental instantiation.
      */
     protected BaseRepositoryDAOTemplateGenerator() {};
 
     /**
      * Retrieves a <code>RepositoryDAOTemplateGenerator</code> instance.
      * @return such instance.
      */
     public static BaseRepositoryDAOTemplateGenerator getInstance()
     {
         return BaseRepositoryDAOTemplateGeneratorSingletonContainer.SINGLETON;
     }
 
     /**
      * Generates a <i>per-repository</i> template.
      * @param metadataManager the metadata manager.
      * @param metadataTypeManager the metadata type manager.
      * @param customSqlProvider the <code>CustomSqlProvider</code> instance.
      * @param packageName the package name.
      * @param basePackageName the base package name.
      * @param engineName the engine name.
      * @param repositoryName the name of the repository.
      * @param tables the tables.
      * @param header the header.
      * @return a template.
      */
     public BasePerRepositoryTemplate createTemplate(
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager,
         final CustomSqlProvider customSqlProvider,
         final String packageName,
         final String basePackageName,
         final String repositoryName,
         final String engineName,
         final Collection tables,
         final String header)
     {
         return
             new BaseRepositoryDAOTemplate(
                 metadataManager,
                 metadataTypeManager,
                 customSqlProvider,
                 header,
                 getDecoratorFactory(),
                 packageName,
                 basePackageName,
                 repositoryName,
                 engineName,
                 tables);
     }
 
     /**
      * Retrieves the decorator factory.
      * @return such instance.
      */
     public DecoratorFactory getDecoratorFactory()
     {
         return CachingDecoratorFactory.getInstance();
     }
 
     /**
      * Writes a table repository template to disk.
      * @param template the table repository template to write.
      * @param outputDir the output folder.
      * @param charset the file encoding.
      * @throws IOException if the file cannot be created.
      */
     public void write(
         final BasePerRepositoryTemplate template,
         final File outputDir,
         final Charset charset)
       throws  IOException
     {
         write(
             template,
             outputDir,
             charset,
             DecorationUtils.getInstance(),
             FileUtils.getInstance());
     }
             
     /**
      * Writes a table repository template to disk.
      * @param template the template to write.
      * @param outputDir the output folder.
      * @param charset the file encoding.
      * @param decorationUtils the <code>DecorationUtils</code> instance.
      * @param fileUtils the <code>FileUtils</code> instance.
      * @throws IOException if the file cannot be created.
      * @precondition template != null
      * @precondition outputDir != null
      * @precondition fileUtils != null
      */
     public void write(
         final BasePerRepositoryTemplate template,
         final File outputDir,
         final Charset charset,
         final DecorationUtils decorationUtils,
         final FileUtils fileUtils)
       throws  IOException
     {
        if (outputDir.mkdirs())
         {
             fileUtils.writeFile(
                   outputDir.getAbsolutePath()
                 + File.separator
                 + decorationUtils.capitalize(template.getRepositoryName())
                 + "DAO.java",
                 template.generate(),
                 charset);
         }
         else
         {
             throw
                 new IOException("Cannot create output dir: " + outputDir);
         }
     }
 }
