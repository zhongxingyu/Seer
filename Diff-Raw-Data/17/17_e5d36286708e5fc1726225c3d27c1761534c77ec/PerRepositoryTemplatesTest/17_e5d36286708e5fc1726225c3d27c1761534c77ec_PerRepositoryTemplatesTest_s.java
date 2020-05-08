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
  * Filename: PerRepositoryTemplatesTest.java
  *
  * Author: Jose San Leandro Armendariz (chous)
  *
  * Description: Maps steps in Cucumber scenarios for testing per-repository
  *              templates.
  *
  * Date: 2013/05/05
  * Time: 17:44
  *
  */
 package cucumber.templates;
 
 /*
  * Importing project classes.
  */
 import org.acmsl.commons.utils.EnglishGrammarUtils;
 import org.acmsl.commons.utils.StringUtils;
 import org.acmsl.queryj.customsql.xml.UntrimmedCallMethodRule;
 import org.acmsl.queryj.metadata.DecoratorFactory;
 import org.acmsl.queryj.metadata.vo.Table;
 import org.acmsl.queryj.metadata.vo.TableIncompleteValueObject;
 import org.acmsl.queryj.templates.BasePerRepositoryTemplate;
 import org.acmsl.queryj.templates.BasePerRepositoryTemplateFactory;
 import org.acmsl.queryj.templates.BasePerRepositoryTemplateGenerator;
 import org.acmsl.queryj.templates.RepositoryDAOFactoryTemplateFactory;
 import org.acmsl.queryj.templates.RepositoryDAOFactoryTemplateGenerator;
 import org.acmsl.queryj.templates.RepositoryDAOTemplateFactory;
 import org.acmsl.queryj.templates.RepositoryDAOTemplateGenerator;
 import org.acmsl.queryj.templates.dao.BasePreparedStatementCreatorTemplateFactory;
 import org.acmsl.queryj.templates.dao.BasePreparedStatementCreatorTemplateGenerator;
 import org.acmsl.queryj.templates.dao.BaseRepositoryDAOFactoryTemplateFactory;
 import org.acmsl.queryj.templates.dao.BaseRepositoryDAOFactoryTemplateGenerator;
 import org.acmsl.queryj.templates.dao.BaseRepositoryDAOTemplateFactory;
 import org.acmsl.queryj.templates.dao.BaseRepositoryDAOTemplateGenerator;
 import org.acmsl.queryj.templates.dao.BaseResultSetExtractorTemplateFactory;
 import org.acmsl.queryj.templates.dao.BaseResultSetExtractorTemplateGenerator;
 import org.acmsl.queryj.templates.dao.ConfigurationPropertiesTemplateFactory;
 import org.acmsl.queryj.templates.dao.ConfigurationPropertiesTemplateGenerator;
 import org.acmsl.queryj.templates.dao.DAOChooserTemplateFactory;
 import org.acmsl.queryj.templates.dao.DAOChooserTemplateGenerator;
 import org.acmsl.queryj.templates.dao.DAOListenerImplTemplateFactory;
 import org.acmsl.queryj.templates.dao.DAOListenerImplTemplateGenerator;
 import org.acmsl.queryj.templates.dao.DAOListenerTemplateFactory;
 import org.acmsl.queryj.templates.dao.DAOListenerTemplateGenerator;
 import org.acmsl.queryj.templates.dao.DataAccessContextLocalTemplateFactory;
 import org.acmsl.queryj.templates.dao.DataAccessContextLocalTemplateGenerator;
 import org.acmsl.queryj.templates.dao.DataAccessManagerTemplateFactory;
 import org.acmsl.queryj.templates.dao.DataAccessManagerTemplateGenerator;
 import org.acmsl.queryj.templates.dao.JdbcTemplateTemplateFactory;
 import org.acmsl.queryj.templates.dao.JdbcTemplateTemplateGenerator;
 import org.acmsl.queryj.templates.dao.JndiUtilsTemplateFactory;
 import org.acmsl.queryj.templates.dao.JndiUtilsTemplateGenerator;
 import org.acmsl.queryj.templates.dao.StatisticsProviderTemplateFactory;
 import org.acmsl.queryj.templates.dao.StatisticsProviderTemplateGenerator;
 import org.acmsl.queryj.templates.dao.ThreadLocalBagTemplateFactory;
 import org.acmsl.queryj.templates.dao.ThreadLocalBagTemplateGenerator;
 import org.acmsl.queryj.tools.QueryJBuildException;
 
 /*
  * Importing ACM S.L. Commons classes.
  */
 import org.acmsl.commons.logging.UniqueLogFactory;
 
 /*
  * Importing Apache Commons Logging classes.
  */
 import org.apache.commons.digester.Digester;
 import org.apache.commons.digester.ObjectCreationFactory;
 import org.apache.commons.logging.LogFactory;
 
 /*
  * Importing Cucumber classes.
  */
 import cucumber.api.DataTable;
 import cucumber.api.java.en.Given;
 import cucumber.api.java.en.Then;
 import cucumber.api.java.en.When;
 
 /*
  * Importing Jetbrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 
 /*
  * Importing JUnit classes.
  */
 import org.jetbrains.annotations.Nullable;
 import org.junit.Assert;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 /*
  * Importing JDK classes.
  */
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * Maps steps in Cucumber scenarios for testing per-repository templates.
  * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro</a>
  * @since 2013/05/05
  */
 @SuppressWarnings("unused")
 public class PerRepositoryTemplatesTest
     extends AbstractTemplatesTest<BasePerRepositoryTemplateGenerator, BasePerRepositoryTemplateFactory>
 {
     /**
      * The package name.
      */
     public static final String PACKAGE_NAME = "com.foo.bar";
 
     /**
      * The DAO package name.
      */
     public static final String DAO_PACKAGE_NAME = "com.foo.bar.dao";
 
     /**
      * The repository.
      */
     private String m__strRepository;
 
     /**
      * The db user.
      */
     private String m__strDbUser;
 
     /**
      * The vendor.
      */
     private String m__strVendor;
 
     /**
      * The table names.
      */
     private List<String> m__lTableNames;
 
     /**
      * Creates an instance.
      */
     public PerRepositoryTemplatesTest()
     {
         immutableSetTableNames(new ArrayList<String>());
 
         GENERATOR_MAPPINGS.put("DataAccessManager", new DataAccessManagerTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("DataAccessManager", DataAccessManagerTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("BasePreparedStatementCreator", new BasePreparedStatementCreatorTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("BasePreparedStatementCreator", BasePreparedStatementCreatorTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("BaseRepositoryDAO", new BaseRepositoryDAOTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("BaseRepositoryDAO", BaseRepositoryDAOTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("BaseRepositoryDAOFactory", new BaseRepositoryDAOFactoryTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("BaseRepositoryDAOFactory", BaseRepositoryDAOFactoryTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("BaseResultSetExtractor", new BaseResultSetExtractorTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("BaseResultSetExtractor", BaseResultSetExtractorTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("DAOChooser", new DAOChooserTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("DAOChooser", DAOChooserTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("DAOListener", new DAOListenerTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("DAOListener", DAOListenerTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("DAOListenerImpl", new DAOListenerImplTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("DAOListenerImpl", DAOListenerImplTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("JdbcTemplate", new JdbcTemplateTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("JdbcTemplate", JdbcTemplateTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("JndiUtils", new JndiUtilsTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("JndiUtils", JndiUtilsTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("RepositoryDAO", new RepositoryDAOTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("RepositoryDAO", RepositoryDAOTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("RepositoryDAOFactory", new RepositoryDAOFactoryTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("RepositoryDAOFactory", RepositoryDAOFactoryTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("StatisticsProvider", new StatisticsProviderTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("StatisticsProvider", StatisticsProviderTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("ThreadLocalBag", new ThreadLocalBagTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("ThreadLocalBag", ThreadLocalBagTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("ConfigurationProperties", new ConfigurationPropertiesTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("ConfigurationProperties", ConfigurationPropertiesTemplateFactory.getInstance());
         GENERATOR_MAPPINGS.put("DataAccessContextLocal", new DataAccessContextLocalTemplateGenerator(false, 1));
         FACTORY_MAPPINGS.put("DataAccessContextLocal", DataAccessContextLocalTemplateFactory.getInstance());
     }
 
     /**
      * Specifies the repository name.
      * @param name such name.
      */
     protected final void immutableSetRepositoryName(@NotNull final String name)
     {
         this.m__strRepository = name;
     }
 
     /**
      * Specifies the repository name.
      * @param name such name.
      */
     protected void setRepositoryName(@NotNull final String name)
     {
         immutableSetRepositoryName(name);
     }
 
     /**
      * Retrieves the repository name.
      * @return such name.
      */
     @NotNull
     public String getRepositoryName()
     {
         return m__strRepository;
     }
 
     /**
      * Specifies the db user.
      * @param user the database username.
      */
     protected final void immutableSetDbUser(@NotNull final String user)
     {
         this.m__strDbUser = user;
     }
 
     /**
      * Specifies the db user.
      * @param user the database username.
      */
     protected void setDbUser(@NotNull final String user)
     {
         immutableSetDbUser(user);
     }
 
     /**
      * Retrieves the db user.
      * @return the database username.
      */
     public String getDbUser()
     {
         return this.m__strDbUser;
     }
 
     /**
      * Specifies the vendor.
      * @param vendor the database vendor.
      */
     protected final void immutableSetVendor(@NotNull final String vendor)
     {
         this.m__strVendor = vendor;
     }
 
     /**
      * Specifies the vendor.
      * @param vendor the database vendor.
      */
     protected void setVendor(@NotNull final String vendor)
     {
         immutableSetVendor(vendor);
     }
 
     /**
      * Specifies the vendor.
      * @return the database vendor.
      */
     public String getVendor()
     {
         return this.m__strVendor;
     }
 
     /**
      * Specifies the table names.
      * @param tableNames such names.
      */
     protected final void immutableSetTableNames(@NotNull final List<String> tableNames)
     {
         m__lTableNames = tableNames;
     }
 
     /**
      * Specifies the table names.
      * @param tableNames such names.
      */
     protected void setTableNames(@NotNull final List<String> tableNames)
     {
         immutableSetTableNames(tableNames);
     }
 
     /**
      * Retrieves the table names.
      * @return such names.
      */
     protected final List<String> immutableGetTableNames()
     {
         return m__lTableNames;
     }
 
     /**
      * Retrieves the table names.
      * @return such names.
      */
     public List<String> getTableNames()
     {
         return new ArrayList<String>(immutableGetTableNames());
     }
 
     /**
      * Retrieves the {@link DecoratorFactory} instance using given generator.
      * @param generator the generator to use.
      * @return the decorator factory.
      */
     @NotNull
     @Override
     protected DecoratorFactory retrieveDecoratorFactory(@NotNull final BasePerRepositoryTemplateGenerator generator)
     {
         return generator.getDecoratorFactory();
     }
 
     /**
      * Gathers the repository information from the scenario.
      * @param repositoryTable the repository information in tabular format.
      */
     @Given("^a repository with the following information:$")
     public void defineRepository(@NotNull final DataTable repositoryTable)
     {
         @NotNull final List<Map<String,String>> entries = repositoryTable.asMaps();
 
         Assert.assertTrue("Missing repository information", entries.size() > 0);
         Assert.assertTrue("Too many repositories defined", entries.size() == 1);
 
         @Nullable final Map<String, String> repositoryInfo = entries.get(0);
 
         Assert.assertNotNull(repositoryInfo);
 
         @Nullable final String name = repositoryInfo.get("name");
         Assert.assertNotNull("Missing repository name", name);
         setRepositoryName(name);
 
         @Nullable final String user = repositoryInfo.get("user");
         Assert.assertNotNull("Missing repository user", user);
         setDbUser(user);
 
         @Nullable final String vendor = repositoryInfo.get("vendor");
         Assert.assertNotNull("Missing repository vendor", vendor);
         setVendor(vendor);
     }
 
     /**
      * Gathers information about the tables defined in the repository.
      * @param dataTable the tabular data about the repository tables.
      */
     @Given("^a repository with the following tables:$")
     public void defineTables(@NotNull final DataTable dataTable)
     {
         defineTables(dataTable.asMaps(), immutableGetTableNames());
     }
 
     /**
      * Gathers information about the tables defined in the repository.
      * @param entries the tabular data about the repository tables.
      * @param tableList the table collection.
      */
     protected void defineTables(
         @NotNull final List<Map<String, String>> entries, @NotNull final List<String> tableList)
     {
         Assert.assertTrue("Missing table information", entries.size() > 0);
 
         for (@NotNull final Map<String, String> entry : entries)
         {
             @Nullable final String tableName = entry.get("table");
             Assert.assertNotNull("Missing table name", tableName);
             tableList.add(tableName);
         }
     }
 
     /**
      * Generates a file with the information from the feature.
      * @param template the template.
      */
     @SuppressWarnings("unused")
     @When("^I generate with per-repository (.*)\\.stg$")
     public void generateFile(@NotNull final String template)
     {
         generateFile(template, getRepositoryName(), getVendor(), getTableNames(), getOutputFiles());
     }
 
     /**
      * Generates a file with the information from the feature.
      * @param templateName the template.
      * @param repository the repository.
      * @param tables the tables.
      * @param outputFiles the output files.
      */
     @SuppressWarnings("unchecked")
     protected void generateFile(
         @NotNull final String templateName,
         @NotNull final String repository,
         @NotNull final String engine,
         @NotNull final List<String> tables,
         @NotNull final Map<String, File> outputFiles)
     {
         final BasePerRepositoryTemplateGenerator generator =
             retrieveTemplateGenerator(templateName);
 
         Assert.assertNotNull("No template generator found for " + templateName, generator);
 
         final BasePerRepositoryTemplateFactory templateFactory = retrieveTemplateFactory(templateName);
 
         Assert.assertNotNull("No template factory found for " + templateName, templateFactory);
 
         final BasePerRepositoryTemplate template =
             templateFactory.createTemplate(
                 retrieveMetadataManager(engine, tables, wrapTables(tables)),
                 retrieveCustomSqlProvider(),
                 retrieveDecoratorFactory(generator),
                 DAO_PACKAGE_NAME,
                 PACKAGE_NAME,
                 repository,
                 "", // header
                 false, // marker
                 false, // jmx
                 tables,
                 "java:comp/env/db",
                 false, // disable generation timestamps
                 false, // disable NotNull annotations
                 true); // disable checkThread.org annotations
 
         Assert.assertNotNull("No template found for " + templateName, template);
 
         File outputDir = null;
 
         try
         {
             rootFolder.create();
             outputDir = rootFolder.newFolder("dao");
         }
         catch (@NotNull final IOException ioException)
         {
             Assert.fail(ioException.getMessage());
         }
 
 //            Assert.assertTrue("Cannot create folder: " + outputDir.getAbsolutePath(), outputDir.mkdirs());
 
         UniqueLogFactory.initializeInstance(LogFactory.getLog(PerTableTemplatesTest.class));
 
         try
         {
             generator.write(
                 template,
                 outputDir,
                 rootFolder.getRoot(),
                 Charset.defaultCharset());
         }
         catch (@NotNull final IOException ioException)
         {
             Assert.fail(ioException.getMessage());
         }
         catch (@NotNull final QueryJBuildException queryjBuildException)
         {
             Assert.fail(queryjBuildException.getMessage());
         }
 
         outputFiles.put(
             repository,
             new File(outputDir, generator.retrieveTemplateFileName(template.getTemplateContext())));
     }
 
     /**
      * Wraps a list of {@link Table}s using given names.
      * @param tableNames the table names.
      * @return the table list.
      */
     protected List<Table> wrapTables(final List<String> tableNames)
     {
         @NotNull final List<Table> result = new ArrayList<Table>(tableNames.size());
 
         for (@Nullable final String tableName : tableNames)
         {
             if (tableName != null)
             {
                 result.add(
                     new TableIncompleteValueObject(tableName, null));
             }
         }
 
         return result;
     }
 
     /**
      * Checks the generated file compiles.
      * @param outputName the name of the output file.
      */
     @SuppressWarnings("unused")
     @Then("^the generated per-repository (.*) file compiles successfully")
     public void checkGeneratedFileCompiles(@NotNull final String outputName)
     {
         checkGeneratedFilesCompile(outputName, getOutputFiles());
     }
 
     /**
      * Checks whether the generated properties file is valid.
      * @param outputName the output name.
      */
     @Then("^the generated properties (.*)-queryj.properties is valid$")
     public void checkPropertiesIsValid(@NotNull final String outputName)
     {
         checkPropertiesIsValid(
             outputName,
             getOutputFiles(),
             getTableNames(),
             getVendor(),
             EnglishGrammarUtils.getInstance(),
             StringUtils.getInstance());
     }
 
     /**
      * Checks whether the generated properties file is valid.
      * @param outputName the output name.
      * @param outputFiles the output files.
      * @param tableNames the table names.
      * @param vendor the vendor.
      * @param grammarUtils the {@link EnglishGrammarUtils} instance.
      */
     protected void checkPropertiesIsValid(
         @NotNull final String outputName,
         @NotNull final Map<String,File> outputFiles,
         @NotNull final List<String> tableNames,
         @NotNull final String vendor,
         @NotNull final EnglishGrammarUtils grammarUtils,
         @NotNull final StringUtils stringUtils)
     {
         checkPropertiesFiles(outputName, outputFiles);
         @Nullable final File file = retrieveOutputFile(outputName.concat("-queryj.properties"));
 
         if (file != null)
         {
             @Nullable final Properties properties = readPropertiesFile(file);
 
             Assert.assertNotNull("Invalid properties file : " + file.getAbsolutePath(), properties);
 
             Assert.assertEquals(
                 "Invalid number of entries in " + file.getAbsolutePath(),
                 tableNames.size(),
                 properties.size());
 
             for (@Nullable final String tableName : tableNames)
             {
                 Assert.assertNotNull(tableName);
 
                 @NotNull final String singularTableName = toSingular(tableName, grammarUtils);
 
                 @NotNull final String key =
                     getRepositoryName() + "." + singularTableName + ".dao";
 
                 Assert.assertTrue(
                     "Missing entry in " + file.getAbsolutePath(),
                     properties.containsKey(key));
 
                 Assert.assertEquals(
                     "Invalid entry " + key + " in " + file.getAbsolutePath(),
                       DAO_PACKAGE_NAME
                     + ".rdb."
                     + vendor.toLowerCase(Locale.getDefault())
                     + "." + vendor
                     + capitalize(singularTableName, stringUtils)
                     + "DAOFactory",
                     properties.getProperty(key));
             }
         }
     }
 
     /**
      * Converts given word to singular.
      * @param value the word.
      * @param grammarUtils the {@link EnglishGrammarUtils} instance.
      * @return the singular word.
      */
     @NotNull
     protected String toSingular(
         @NotNull final String value, @NotNull final EnglishGrammarUtils grammarUtils)
     {
         return grammarUtils.getSingular(value);
     }
 
     /**
      * Capitalizes given value.
      * @param value the value.
      * @param stringUtils the {@link StringUtils} instance.
      * @return the capitalized value.
      */
     @NotNull
     protected String capitalize(
         @NotNull final String value, @NotNull final StringUtils stringUtils)
     {
         return stringUtils.capitalize(value);
     }
 
     /**
      * Checks whether the generated properties file is valid.
      * @param outputName the output name.
      */
     @Then("^the generated Spring file (.*) is valid$")
     public void checkDataAccessContextLocalIsValid(@NotNull final String outputName)
     {
         @Nullable final File file = retrieveOutputFile(outputName);
 
         Assert.assertNotNull("Invalid file : " + outputName, file);
 
         @NotNull final List<BeanElement> beans = parseFile(file);
 
 
     }
 
     /**
      * Parses given file.
      * @param file the file to parse.
      * @return the list of {@link BeanElement}s in the file.
      */
     @NotNull
     protected List<BeanElement> parseFile(@NotNull final File file)
     {
         return parseFile(file, buildDataAccessContextLocalDigester());
     }
 
     /**
      * Parses given file.
      * @param file the file to parse.
      * @param digester the {@link Digester} instance.
      * @return the list of {@link BeanElement}s in the file.
      */
     @NotNull
     protected List<BeanElement> parseFile(@NotNull final File file, @NotNull final Digester digester)
     {
         @NotNull final List<BeanElement> result = new ArrayList<BeanElement>();
 
         digester.push(result);
 
         try
         {
             digester.parse(file);
         }
         catch (IOException e)
         {
             Assert.fail(e.getMessage());
         }
         catch (SAXException e)
         {
             Assert.fail(e.getMessage());
         }
 
         return result;
     }
 
     /**
      * Creates and configures a new Digester instance.
      * @return such instance.
      */
     @NotNull
     protected Digester buildDataAccessContextLocalDigester()
     {
         @NotNull final Digester result = new Digester();
 
         // <beans>
 
         //   <bean>
         result.addFactoryCreate(
             "beans/bean",
             new BeanElementFactory());
 
         //     <property>
         result.addFactoryCreate(
             "beans/bean/property",
             new PropertyElementFactory());
 
         //     <value>
         result.addRule(
             "beans/bean/property/value",
             new UntrimmedCallMethodRule("setValue", 0));
         //     </value>
 
         //     <ref>
         result.addFactoryCreate(
             "beans/bean/property/ref",
             new RefElementFactory());
         //     </ref>
 
         result.addSetNext("beans/bean/property", "add");
 
         //   </property>
 
         // <sql-list>
 
         return result;
     }
 
     /**
      * Represents &lt;bean&gt; information in XML files.
      */
     protected static class BeanElement
     {
         /**
          * The id.
          */
         private String m__strId;
 
         /**
          * The Class.
          */
         private String m__strClass;
 
         /**
          * The properties.
          */
         private List<PropertyElement> m__lProperties;
 
         /**
          * Creates a new BeanElement.
          * @param id the id.
          * @param clazz the class value.
          */
         public BeanElement(@NotNull final String id, @NotNull final String clazz)
         {
             immutableSetId(id);
             immutableSetClassValue(clazz);
             immutableSetProperties(new ArrayList<PropertyElement>(1));
         }
 
         /**
          * Specifies the id.
          * @param value the id value.
          */
         protected final void immutableSetId(@NotNull final String value)
         {
             this.m__strId = value;
         }
 
         /**
          * Specifies the id.
          * @param value the id value.
          */
         @SuppressWarnings("unused")
         protected void setId(@NotNull final String value)
         {
             immutableSetId(value);
         }
 
         /**
          * Retrieves the id.
          * @return such value.
          */
         @NotNull
         public String getId()
         {
             return this.m__strId;
         }
 
         /**
          * Specifies the Class.
          * @param value the Class value.
          */
         protected final void immutableSetClassValue(@NotNull final String value)
         {
             this.m__strClass = value;
         }
 
         /**
          * Specifies the Class.
          * @param value the Class value.
          */
         @SuppressWarnings("unused")
         protected void setClassValue(@NotNull final String value)
         {
             immutableSetClassValue(value);
         }
 
         /**
          * Retrieves the Class.
          * @return such value.
          */
         @NotNull
         public String getClassValue()
         {
             return this.m__strClass;
         }
 
         /**
          * Specifies the properties.
          * @param properties the properties.
          */
         protected final void immutableSetProperties(@NotNull final List<PropertyElement> properties)
         {
             this.m__lProperties = properties;
         }
 
         /**
          * Specifies the properties.
          * @param properties the properties.
          */
         @SuppressWarnings("unused")
         protected void setProperties(@NotNull final List<PropertyElement> properties)
         {
             immutableSetProperties(properties);
         }
 
         /**
          * Retrieves the properties.
          * @return such information.
          */
         @NotNull
         protected final List<PropertyElement> immutableGetProperties()
         {
             return this.m__lProperties;
         }
 
         /**
          * Retrieves the properties.
          * @return such information.
          */
         @NotNull
         public List<PropertyElement> getProperties()
         {
             return new ArrayList<PropertyElement>(immutableGetProperties());
         }
 
         /**
          * Adds a new property.
          * @param property the property.
          */
         public void add(@NotNull final PropertyElement property)
         {
             immutableGetProperties().add(property);
         }
 
         @Override
         public boolean equals(final Object o)
         {
             boolean result = false;
 
             if (this == o)
             {
                 result = true;
             }
             else if (o instanceof BeanElement)
             {
                 @NotNull final BeanElement that = (BeanElement) o;
 
                 if (   (immutableGetProperties().equals(that.immutableGetProperties()))
                     && (getClassValue().equals(that.getClassValue()))
                     && (getId().equals(that.getId())))
                 {
                     result = true;
                 }
             }
 
             return result;
         }
 
         @Override
         public int hashCode()
         {
             int result = m__strId.hashCode();
             result = 31 * result + m__strClass.hashCode();
             result = 31 * result + m__lProperties.hashCode();
             return result;
         }
 
         @Override
         public String toString()
         {
             return
                   "BeanElement{"
                 + " id='" + getId()
                 + "', class='" + getClassValue() + "'"
                 + "', properties=" + immutableGetProperties()
                 + "'}";
         }
     }
 
     /**
      * Creates BeanElements.
      */
     protected static class BeanElementFactory
         implements ObjectCreationFactory
     {
         /**
          * The digester instance.
          */
         private Digester m__Digester;
 
         /**
          * <p>Factory method called by {@link org.apache.commons.digester.FactoryCreateRule} to supply an
          * object based on the element's attributes.
          *
          * @param attributes the element's attributes
          * @throws Exception any exception thrown will be propagated upwards
          */
         @NotNull
         @Override
         public Object createObject(@NotNull final Attributes attributes)
             throws Exception
         {
             final @Nullable String t_strId = attributes.getValue("id");
             final @Nullable String t_strClass = attributes.getValue("class");
 
             return new BeanElement(t_strId, t_strClass);
         }
 
         /**
          * <p>Returns the {@link org.apache.commons.digester.Digester} that was set by the
          * {@link org.apache.commons.digester.FactoryCreateRule} upon initialization.
          */
         @Override
         @Nullable
         public Digester getDigester()
         {
             return m__Digester;
         }
 
         /**
          * <p>Set the {@link org.apache.commons.digester.Digester} to allow the implementation to do logging,
          * classloading based on the digester's classloader, etc.
          *
          * @param digester parent Digester object.
          */
         @Override
         public void setDigester(@NotNull final Digester digester)
         {
             this.m__Digester = digester;
         }
     }
 
     /**
      * Represents &lt;property&gt; information in XML files.
      */
     protected static class PropertyElement
     {
         /**
          * The name.
          */
         private String m__strName;
 
         /**
          * The value.
          */
         private String m__strValue;
 
         /**
          * Creates a PropertyElement with the following name.
          * @param name the name.
          */
         public PropertyElement(@NotNull final String name)
         {
             immutableSetName(name);
         }
 
         /**
          * Specifies the name.
          * @param name the name.
          */
         protected final void immutableSetName(@NotNull final String name)
         {
             this.m__strName = name;
         }
 
         /**
          * Specifies the name.
          * @param name the name.
          */
         @SuppressWarnings("unused")
         protected void setName(@NotNull final String name)
         {
             immutableSetName(name);
         }
 
         /**
          * Retrieves the name.
          * @return the name.
          */
         @NotNull
         public String getName()
         {
             return this.m__strName;
         }
 
         /**
          * Specifies the value.
          * @param value the value.
          */
         protected final void immutableSetValue(@NotNull final String value)
         {
             this.m__strValue = value;
         }
 
         /**
          * Specifies the value.
          * @param value the value.
          */
         @SuppressWarnings("unused")
         public void setValue(@NotNull final String value)
         {
             immutableSetValue(value);
         }
 
         /**
          * Retrieves the value.
          * @return the value.
          */
         @NotNull
         public String getValue()
         {
             return this.m__strValue;
         }
 
         @Override
         public boolean equals(final Object o)
         {
             boolean result = false;
 
             if (this == o)
             {
                 result = true;
             }
             else if (o instanceof PropertyElement)
             {
                 PropertyElement that = (PropertyElement) o;
 
                 if (   (getName().equals(that.getName()))
                     && (getName().equals(that.getValue())))
                 {
                     result = true;
                 }
             }
 
             return result;
         }
 
         @Override
         public int hashCode()
         {
             int result = getName().hashCode();
             String t_strValue = getValue();
             if (t_strValue != null)
             {
                 result = 31 * result + t_strValue.hashCode();
             }
 
             return result;
         }
 
         @Override
         public String toString()
         {
             StringBuilder result = new StringBuilder("PropertyElement{name='");
             result.append(getName());
             String t_strValue = getValue();
             if (t_strValue != null)
             {
                 result.append(", value='");
                 result.append(t_strValue);
                 result.append("'");
             }
             result.append("'}");
 
             return result.toString();
         }
     }
 
     /**
      * Creates PropertyElements.
      */
     protected static class PropertyElementFactory
         implements ObjectCreationFactory
     {
         /**
          * The digester instance.
          */
         private Digester m__Digester;
 
         /**
          * <p>Factory method called by {@link org.apache.commons.digester.FactoryCreateRule} to supply an
          * object based on the element's attributes.
          *
          * @param attributes the element's attributes
          * @throws Exception any exception thrown will be propagated upwards
          */
         @Override
         public Object createObject(@NotNull final Attributes attributes)
             throws Exception
         {
             final @Nullable String t_strName = attributes.getValue("name");
 
             return new PropertyElement(t_strName);
         }
 
         /**
          * <p>Returns the {@link org.apache.commons.digester.Digester} that was set by the
          * {@link org.apache.commons.digester.FactoryCreateRule} upon initialization.
          */
         @Override
         public Digester getDigester()
         {
             return m__Digester;
         }
 
         /**
          * <p>Set the {@link org.apache.commons.digester.Digester} to allow the implementation to do logging,
          * classloading based on the digester's classloader, etc.
          *
          * @param digester parent Digester object.
          */
         @Override
         public void setDigester(@NotNull final Digester digester)
         {
             this.m__Digester = digester;
         }
     }
 
 
     /**
      * Represents &lt;ref&gt; information in XML files.
      */
     protected static class RefElement
     {
         /**
          * The local information.
          */
         private String m__strLocal;
 
         /**
          * Creates a RefElement with the following local reference name.
          * @param local the reference name.
          */
         public RefElement(@NotNull final String local)
         {
             immutableSetLocal(local);
         }
 
         /**
          * Specifies the local reference name.
          * @param local the reference name.
          */
         protected final void immutableSetLocal(@NotNull final String local)
         {
             this.m__strLocal = local;
         }
 
         /**
          * Specifies the local reference name.
          * @param local the reference name.
          */
         @SuppressWarnings("unused")
         protected void setLocal(@NotNull final String local)
         {
             immutableSetLocal(local);
         }
 
         /**
          * Retrieves the local reference name.
          * @return the reference name.
          */
         @NotNull
         public String getLocal()
         {
             return this.m__strLocal;
         }
 
         @Override
         public String toString()
         {
             return "RefElement{local='" + getLocal() + "'}";
         }
     }
 
     /**
      * Creates RefElements.
      */
     protected static class RefElementFactory
         implements ObjectCreationFactory
     {
         /**
          * The digester instance.
          */
         private Digester m__Digester;
 
         /**
          * <p>Factory method called by {@link org.apache.commons.digester.FactoryCreateRule} to supply an
          * object based on the element's attributes.
          *
          * @param attributes the element's attributes
          * @throws Exception any exception thrown will be propagated upwards
          */
         @Override
         public Object createObject(@NotNull final Attributes attributes)
             throws Exception
         {
             final @Nullable String t_strLocal = attributes.getValue("local");
 
             return new RefElement(t_strLocal);
         }
 
         /**
          * <p>Returns the {@link org.apache.commons.digester.Digester} that was set by the
          * {@link org.apache.commons.digester.FactoryCreateRule} upon initialization.
          */
         @Override
         public Digester getDigester()
         {
             return m__Digester;
         }
 
         /**
          * <p>Set the {@link org.apache.commons.digester.Digester} to allow the implementation to do logging,
          * classloading based on the digester's classloader, etc.
          *
          * @param digester parent Digester object
          */
         @Override
         public void setDigester(final Digester digester)
         {
             this.m__Digester = digester;
         }
     }
 }
