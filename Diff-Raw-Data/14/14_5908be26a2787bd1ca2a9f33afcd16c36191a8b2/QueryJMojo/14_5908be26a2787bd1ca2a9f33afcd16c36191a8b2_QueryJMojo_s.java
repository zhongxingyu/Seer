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
  * Filename: QueryJMojo.java
  *
  * Author: Jose San Leandro Armendariz/Jose Juan.
  *
  * Description: Executes QueryJ.
  */
 package org.acmsl.queryj.tools.maven;
 
 /*
  * Importing some JDK classes.
  */
 import java.io.File;
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Properties;
 
 /*
  * Importing some ACM-SL classes.
  */
 import org.acmsl.queryj.tools.ant.QueryJTask;
 import org.acmsl.queryj.tools.ant.AntExternallyManagedFieldsElement;
 import org.acmsl.queryj.tools.ant.AntFieldElement;
 import org.acmsl.queryj.tools.ant.AntTableElement;
 import org.acmsl.queryj.tools.ant.AntTablesElement;
 
 /*
  * Importing some Maven classes.
  */
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.Mojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 
 /*
  * Importing some Ant classes.
  */
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.types.Path;
 
 /**
  * Executes QueryJ.
  * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro Armendariz</a>
  * @goal queryj
  */
 public class QueryJMojo
     extends AbstractMojo
     implements Mojo
 {
     /**
      * The location of pom.properties within the jar file.
      */
     protected static final String POM_PROPERTIES_LOCATION =
         "META-INF/maven/org.acmsl/queryj/pom.properties";
 
     /**
      * The driver.
      * @parameter property="driver" @required
      */
     private String m__strDriver;
     
     /**
      * The url.
      * @parameter property="url" @required
      */
     private String m__strUrl;
     
     /**
      * The user name.
      * @parameter property="username" @required
      */
     private String m__strUsername;
     
     /**
      * The password.
      * @parameter property="password" @required
      */
     private String m__strPassword;
     
     /**
      * The catalog.
      * @parameter property="catalog"
      */
     private String catalog;
     
     /**
      * The schema.
      * @parameter property="schema" @required
      */
     private String schema;
     
     /**
      * The repository.
      * @parameter property="repository" @required
      */
     private String m__strRepository;
     
     /**
      * The package name.
      * @parameter property="packageName" @required
      */
     private String m__strPackageName;
     
     /**
      * The output directory.
      * @parameter property="outputDir" 
      */
     private File m__OutputDir;
     
     /**
      * The data source.
      * @parameter property="jndiDataSource"
      */
     private String m__strJndiDataSource;
     
     /**
      * The sql xml file.
      * @parameter property="sqlXmlFile"
      */
     private File m__SqlXmlFile;
     
     /**
      * The customized sql model.
      * @parameter property="customSqlModel"
      */
     private String m__strCustomSqlModel;
     
     /**
      * The xml dao implementation flag.
      * @parameter property="generateXmlDAOImplementation"
      */
     private Boolean m__bGenerateXmlDAOImplementation = Boolean.FALSE;
     
     /**
      * The mock dao implementation flag.
      * @parameter property="generateMockDAOImplementation"
      */
     private Boolean m__bGenerateMockDAOImplementation = Boolean.FALSE;
     
     /**
      * The test generation flag.
      * @parameter property="generateTests"
      */
     private Boolean m__bGenerateTests = Boolean.FALSE;
     
     /**
      * The header file.
      * @parameter property="headerFile"
      */
     private File m__HeaderFile;
     
     /**
      * The extract functions flag.
      * @parameter property="extractFunctions"
      */
     private Boolean m__bExtractFunctions = Boolean.FALSE;
     
     /**
      * The extract procedures flag.
      * @parameter property="extractProcedures"
      */
     private Boolean m__bExtractProcedures = Boolean.FALSE;
     
     /**
      * The list of external managed fields
      */
     private ExternallyManagedField[] m__aExternallyManagedFields;
     // @&& parameter property="externallyManagedFields"
     
     /**
      * @parameter
      */
     private ExternallyManagedField[] externallyManagedFields;
 
     /**
      * The grammar bundle file.
      */
     private String m__strGrammarbundle;
     //* @&& parameter property="grammarbundle"
 
     /**
      * Whether to use <code>main</code> and <code>test</code> subfolders
      * within <code>outputDir</code>.
      */
     private Boolean m__bOutputSubfolders = Boolean.FALSE;
 //     * @parameter property="useOutputSubfolders"
 
     /**
      * The list of tables.
      */
     private Table[] m__aTables;
     // @*** parameter property="tables"
 
     /**
      * The file encoding.
      * @parameter property="encoding" default-value="${project.build.sourceEncoding}"
      */
     private String m__strEncoding;
 
     /**
      * Specifies the driver.
      * @param driver such value.
      */
     protected final void immutableSetDriver(final String driver)
     {
         m__strDriver = driver;
     }
 
     /**
      * Specifies the driver.
      * @param driver such value.
      */
     public void setDriver(final String driver)
     {
         immutableSetDriver(driver);
     }
 
     /**
      * Returns the driver.
      * @return such value.
      */
     protected final String immutableGetDriver()
     {
         return m__strDriver;
     }
 
     /**
      * Returns the driver.
      * @return such value.
      */
     protected String getDriver()
     {
         return immutableGetDriver();
     }
 
     /**
      * Specifies the url.
      * @param url the url.
      */
     protected final void immutableSetUrl(final String url)
     {
         m__strUrl = url;
     }
 
     /**
      * Specifies the url.
      * @param newUrl the url.
      */
     public void setUrl(final String url)
     {
         immutableSetUrl(url);
     }
 
     /**
      * Returns the url.
      * @return such value.
      */
     protected final String immutableGetUrl()
     {
         return m__strUrl;
     }
     
     /**
      * Returns the url.
      * @return such value.
      */
     protected String getUrl()
     {
         return immutableGetUrl();
     }
 
     /**
      * Specifies the username.
      * @param username the new value.
      */
     protected final void immutableSetUsername(final String username)
     {
         m__strUsername = username;
     }
 
     /**
      * Specifies the username.
      * @param username the new value.
      */
     public void setUsername(final String username)
     {
         immutableSetUsername(username);
     }
 
     /**
      * Returns the user name.
      * @return such value.
      */
     protected final String immutableGetUsername() 
     {
         return m__strUsername;
     }
     
     /**
      * Returns the user name.
      * @return such value.
      */
     protected String getUsername() 
     {
         return immutableGetUsername();
     }
 
     /**
      * Specifies the password.
      * @param password the password.
      */
     protected final void immutableSetPassword(final String password)
     {
         m__strPassword = password;
     }
 
     /**
      * Specifies the password.
      * @param password the password.
      */
     public void setPassword(final String password)
     {
         immutableSetPassword(password);
     }
 
     /**
      * Returns the password.
      * @return such value.
      */
     protected final String immutableGetPassword()
     {
         return m__strPassword;
     }
     
     /**
      * Returns the password.
      * @return such value.
      */
     protected String getPassword()
     {
         return immutableGetPassword();
     }
 
     /**
      * Specifies the catalog.
      * @param catalog the catalog.
      */
     protected final void immutableSetCatalog(final String catalog)
     {
         this.catalog = catalog;
     }
 
     /**
      * Specifies the catalog.
      * @param catalog the catalog.
      */
     public void setCatalog(final String catalog)
     {
         immutableSetCatalog(catalog);
     }
 
     /**
      * Returns the catalog.
      * @return such value.
      */
     protected final String immutableGetCatalog()
     {
         return catalog;
     }
     
     /**
      * Returns the catalog.
      * @return such value.
      */
     protected String getCatalog()
     {
         return immutableGetCatalog();
     }
 
     /**
      * Specifies the schema.
      * @param schema the schema.
      */
     protected final void immutableSetSchema(final String schema)
     {
         this.schema = schema;
     }
 
     /**
      * Specifies the schema.
      * @param schema the schema.
      */
     public void setSchema(final String schema)
     {
         immutableSetSchema(schema);
     }
 
     /**
      * Returns the schema.
      * @return such value.
      */
     protected final String immutableGetSchema()
     {
         return schema;
     }
     
     /**
      * Returns the schema.
      * @return such value, or an empty string if not initialized.
      */
     protected String getSchema()
     {
         String result = immutableGetSchema();
 
         if  (result == null)
         {
             result = "";
         }
 
         return result;
     }
 
     /**
      * Specifies the repository.
      * @param repository the repository.
      */
     protected final void immutableSetRepository(final String repository)
     {
         m__strRepository = repository;
     }
 
     /**
      * Specifies the repository.
      * @param repository the repository.
      */
     public void setRepository(final String repository)
     {
         immutableSetRepository(repository);
     }
 
     /**
      * Returns the repository.
      * @return such value.
      */
     protected final String immutableGetRepository()
     {
         return m__strRepository;
     }
     
     /**
      * Returns the repository.
      * @return such value, or an empty string if not initialized.
      */
     protected String getRepository()
     {
         String result = immutableGetRepository();
 
         if  (result == null)
         {
             result = "";
         }
 
         return result;
     }
 
     /**
      * Specifies the package name.
      * @param packageName the package name.
      */
     protected final void immutableSetPackageName(final String packageName)
     {
         m__strPackageName = packageName;
     }
 
     /**
      * Specifies the package name.
      * @param packageName the package name.
      */
     public void setPackageName(final String packageName)
     {
         immutableSetPackageName(packageName);
     }
 
     /**
      * Returns the package name.
      * @return such value.
      */
     protected final String immutableGetPackageName()
     {
         return m__strPackageName;
     }
     
     /**
      * Returns the package name.
      * @return such value.
      */
     protected String getPackageName()
     {
         return immutableGetPackageName();
     }
 
     /**
      * Specifies the output directory.
      * @param outputDir such directory.
      */
     protected final void immutableSetOutputDir(final File outputDir)
     {
         m__OutputDir = outputDir;
     }
 
     /**
      * Specifies the output directory.
      * @param outputDir such directory.
      */
     public void setOutputDir(final File outputDir)
     {
         immutableSetOutputDir(outputDir);
     }
 
     /**
      * Returns the output directory.
      * @return such directory.
      */
     protected final File immutableGetOutputDir()
     {
         return m__OutputDir;
     }
     
     /**
      * Returns the output directory.
      * @return such directory.
      */
     protected File getOutputDir()
     {
         return immutableGetOutputDir();
     }
 
     /**
      * Specifies the JNDI path to the data source.
      * @param jndiPath such path.
      */
     protected final void immutableSetJndiDataSource(final String jndiPath)
     {
         m__strJndiDataSource = jndiPath;
     }
 
     /**
      * Specifies the JNDI path to the data source.
      * @param jndiPath such path.
      */
     public void setJndiDataSource(final String jndiPath)
     {
         immutableSetJndiDataSource(jndiPath);
     }
 
     /**
      * Returns the JNDI location of the data source.
      * @return such value.
      */
     protected final String immutableGetJndiDataSource()
     {
         return m__strJndiDataSource;
     }
     
     /**
      * Returns the JNDI location of the data source.
      * @return such value.
      */
     protected String getJndiDataSource()
     {
         return immutableGetJndiDataSource();
     }
 
     /**
      * Specifies the XML file where the SQL queries are defined.
      * @param sqlFile such file.
      */
     protected final void immutableSetSqlXmlFile(final File sqlFile)
     {
         m__SqlXmlFile = sqlFile;
     }
 
     /**
      * Specifies the XML file where the SQL queries are defined.
      * @param sqlFile such file.
      */
     public void setSqlXmlFile(final File sqlFile)
     {
         immutableSetSqlXmlFile(sqlFile);
     }
 
     /**
      * Return the sql xml file.
      * @return such file.
      */
     protected final File immutableGetSqlXmlFile()
     {
         return m__SqlXmlFile;
     }
     
     /**
      * Return the sql xml file.
      * @return such file.
      */
     protected File getSqlXmlFile()
     {
         return immutableGetSqlXmlFile();
     }
 
     /**
      * Specifies the custom SQL model.
      * @param model the model.
      */
     protected final void immutableSetCustomSqlModel(final String model)
     {
         m__strCustomSqlModel = model;
     }
 
     /**
      * Specifies the custom SQL model.
      * @param model the model.
      */
     public final void setCustomSqlModel(final String model)
     {
         immutableSetCustomSqlModel(model);
     }
 
     /**
      * Returns the custom sql model.
      * @return such value.
      */
     protected final String immutableGetCustomSqlModel()
     {
         return m__strCustomSqlModel;
     }
     
     /**
      * Returns the custom sql model.
      * @return such value.
      */
     protected String getCustomSqlModel()
     {
         return immutableGetCustomSqlModel();
     }
 
     /**
      * Specifies whether to generate a XML-based DAO implementation.
      * @param flag such condition.
      */
     protected final void immutableSetGenerateXmlDAOImplementation(final Boolean flag)
     {
         m__bGenerateXmlDAOImplementation = flag;
     }
 
     /**
      * Specifies whether to generate a XML-based DAO implementation.
      * @param flag such condition.
      */
     public void setGenerateXmlDAOImplementation(final Boolean flag)
     {
         immutableSetGenerateXmlDAOImplementation(flag);
     }
 
     /**
      * Indicates if xml dao must be generated.
      * @return such condition.
      */
     protected final Boolean immutableGetGenerateXmlDAOImplementation()
     {
         return m__bGenerateXmlDAOImplementation;
     }
     
     /**
      * Indicates if xml dao must be generated.
      * @return such condition.
      */
     protected Boolean getGenerateXmlDAOImplementation()
     {
         return immutableGetGenerateXmlDAOImplementation();
     }
     
     /**
      * Specifies whether to generate a Mock-based DAO implementation.
      * @param flag such condition.
      */
     protected final void immutableSetGenerateMockDAOImplementation(final Boolean flag)
     {
         m__bGenerateMockDAOImplementation = flag;
     }
 
     /**
      * Specifies whether to generate a Mock-based DAO implementation.
      * @param flag such condition.
      */
     public void setGenerateMockDAOImplementation(final Boolean flag)
     {
         immutableSetGenerateMockDAOImplementation(flag);
     }
 
     /**
      * Indicates if mock dao must be generated.
      * @return such condition.
      */
     protected final Boolean immutableGetGenerateMockDAOImplementation()
     {
         return m__bGenerateMockDAOImplementation;
     }
     
     /**
      * Indicates if mock dao must be generated.
      * @return such condition.
      */
     protected Boolean getGenerateMockDAOImplementation()
     {
         return immutableGetGenerateMockDAOImplementation();
     }
 
     /**
      * Specifies whether the tests should be generated.
      * @param flag such condition.
      */
     protected final void immutableSetGenerateTests(final Boolean flag)
     {
         m__bGenerateTests = flag;
     }
 
     /**
      * Specifies whether the tests should be generated.
      * @param flag such condition.
      */
     public void setGenerateTests(final Boolean flag)
     {
         immutableSetGenerateTests(flag);
     }
 
     /**
      * Indicates if tests must be generated.
      * @return such condition.
      */
     protected final Boolean immutableGetGenerateTests()
     {
         return m__bGenerateTests;
     }
     
     /**
      * Indicates if tests must be generated.
      * @return such condition.
      */
     protected Boolean getGenerateTests()
     {
         return immutableGetGenerateTests();
     }
 
     /**
      * Specifies the header file.
      * @param file such file.
      */
     protected final void immutableSetHeaderFile(final File file)
     {
         m__HeaderFile = file;
     }
 
     /**
      * Specifies the header file.
      * @param file such file.
      */
     public void setHeaderFile(final File file)
     {
         immutableSetHeaderFile(file);
     }
 
     /**
      * Returns the header file.
      * @return such file.
      */
     protected final File immutableGetHeaderFile()
     {
         return m__HeaderFile;
     }
     
     /**
      * Returns the header file.
      * @return such file.
      */
     protected File getHeaderFile()
     {
         return immutableGetHeaderFile();
     }
 
     /**
      * Specifies whether database functions must be generated.
      * @param flag such condition.
      */
     protected final void immutableSetExtractFunctions(final Boolean flag)
     {
         m__bExtractFunctions = flag;
     }
 
     /**
      * Specifies whether database functions must be generated.
      * @param flag such condition.
      */
     public void setExtractFunctions(final Boolean flag)
     {
         immutableSetExtractFunctions(flag);
     }
 
     /**
      * Indicates if functions must be extracted.
      * @return such condition.
      */
     protected final Boolean immutableGetExtractFunctions()
     {
         return m__bExtractFunctions;
     }
     
     /**
      * Indicates if functions must be extracted.
      * @return such condition.
      */
     protected Boolean getExtractFunctions()
     {
         return immutableGetExtractFunctions();
     }
     
     /**
      * Specifies whether database procedures must be generated.
      * @param flag such condition.
      */
     protected final void immutableSetExtractProcedures(final Boolean flag)
     {
         m__bExtractProcedures = flag;
     }
 
     /**
      * Specifies whether database procedures must be generated.
      * @param flag such condition.
      */
     public void setExtractProcedures(final Boolean flag)
     {
         immutableSetExtractProcedures(flag);
     }
 
     /**
      * Indicates if procedures must be extracted.
      * @return such condition.
      */
     protected final Boolean immutableGetExtractProcedures()
     {
         return m__bExtractProcedures;
     }
     
     /**
      * Indicates if procedures must be extracted.
      * @return such condition.
      */
     protected Boolean getExtractProcedures()
     {
         return immutableGetExtractProcedures();
     }
 
     /**
      * Specifies the externally managed fields.
      * @param fields such fields.
      */
     protected final void immutableSetExternallyManagedFields(
         final ExternallyManagedField[] fields)
     {
         m__aExternallyManagedFields = fields;
         externallyManagedFields = fields;
     }
 
     /**
      * Specifies the externally managed fields.
      * @param fields such fields.
      */
     public void setExternallyManagedFields(final ExternallyManagedField[] fields)
     {
         immutableSetExternallyManagedFields(fields);
     }
 
     /**
      * Returns the externally managed fields.
      * @return such fields.
      */
     protected final ExternallyManagedField[] immutableGetExternallyManagedFields()
     {
 //        return m__aExternallyManagedFields;
         return externallyManagedFields;
     }
     
     /**
      * Returns the externally managed fields.
      * @return such fields.
      */
     protected ExternallyManagedField[] getExternallyManagedFields()
     {
         return immutableGetExternallyManagedFields();
     }
 
     /**
      * Specifies the grammar bundle.
      * @param bundle such bundle.
      */
     protected final void immutableSetGrammarbundle(final String bundle)
     {
         m__strGrammarbundle = bundle;
     }
 
     /**
      * Specifies the grammar bundle.
      * @param bundle such bundle.
      */
     public void setGrammarbundle(final String bundle)
     {
         immutableSetGrammarbundle(bundle);
     }
 
     /**
      * Returns the grammar bundle.
      * @return such resource.
      */
     protected final String immutableGetGrammarbundle()
     {
         return m__strGrammarbundle;
     }
     
     /**
      * Returns the grammar bundle.
      * @return such resource.
      */
     protected String getGrammarbundle()
     {
         return immutableGetGrammarbundle();
     }
 
     /**
      * Specifies whether to create <code>main</code> and <code>test</code>
      * subfolders withing <code>outputDir</code>. Defaults to false.
      * @param flag such condition.
      */
     protected final void immutableSetUseOutputSubfolders(final Boolean flag)
     {
         m__bOutputSubfolders = flag;
     }
 
     /**
      * Specifies whether to create <code>main</code> and <code>test</code>
      * subfolders withing <code>outputDir</code>. Defaults to false.
      * @param flag such condition.
      */
     public void setUseOutputSubfolders(final Boolean flag)
     {
         immutableSetUseOutputSubfolders(flag);
     }
 
     /**
      * Retrieves whether to create <code>main</code> and <code>test</code>
      * subfolders withing <code>outputDir</code>. Defaults to false.
      * @param flag such condition.
      */
     protected final Boolean immutableGetUseOutputSubfolders()
     {
         return m__bOutputSubfolders;
     }
 
     /**
      * Retrieves whether to create <code>main</code> and <code>test</code>
      * subfolders withing <code>outputDir</code>. Defaults to false.
      * @param flag such condition.
      */
     public Boolean getUseOutputSubfolders()
     {
         return immutableGetUseOutputSubfolders();
     }
 
     /**
      * Specifies the tables.
      * @param tables such information.
      */
     protected final void immutableSetTables(final Table[] tables)
     {
         m__aTables = tables;
     }
 
     /**
      * Specifies the tables.
      * @param tables such information.
      */
     public void setTables(final Table[] tables)
     {
         immutableSetTables(tables);
     }
 
     /**
      * Returns the tables.
      * @return such information.
      */
     protected final Table[] immutableGetTables()
     {
         return m__aTables;
     }
     
     /**
      * Returns the tables.
      * @return such information.
      */
     protected Table[] getTables()
     {
         return immutableGetTables();
     }
 
     /**
      * Specifies the encoding.
      * @param encoding the encoding.
      */
     protected final void immutableSetEncoding(final String encoding)
     {
         m__strEncoding = encoding;
     }
 
     /**
      * Specifies the encoding.
      * @param encoding the encoding.
      */
     public void setEncoding(final String encoding)
     {
         immutableSetEncoding(encoding);
     }
 
     /**
      * Retrieves the encoding.
      * @return such information.
      */
     protected final String immutableGetEncoding()
     {
         return m__strEncoding;
     }
 
     /**
      * Retrieves the encoding.
      * @return such information.
      */
     public String getEncoding()
     {
         return immutableGetEncoding();
     }
 
     /**
      * Executes QueryJ via Maven2.
      * @throws MojoExecutionException if something goes wrong.
      */
     public void execute()
         throws MojoExecutionException
     {
         execute(getLog());
     }
 
     /**
      * Executes QueryJ via Maven2.
      * @param log the Maven log.
      * @throws MojoExecutionException if something goes wrong.
      * @precondition log != null
      */
     protected void execute(final Log log)
         throws MojoExecutionException
     {
         execute(log, retrieveVersion(retrievePomProperties(log)));
     }
 
     /**
      * Retrieves the version of QueryJ currently running.
      * @param properties the pom.properties information.
      * @return the version entry.
      */
     protected String retrieveVersion(final Properties properties)
     {
         String result = "(unknown)";
 
         if (   (properties != null)
             && (properties.containsKey("version")))
         {
             result = properties.getProperty("version");
         }
 
         return result;
     }
 
     /**
      * Executes QueryJ via Maven2.
      * @param log the Maven log.
      * @param version the QueryJ version.
      * @throws MojoExecutionException if something goes wrong.
      * @precondition log != null
      */
     protected void execute(final Log log, final String version)
         throws MojoExecutionException
     {
         boolean running = false;
 
         File outputDirPath = getOutputDir();
 
         QueryJTask task = null;
 
         if  (outputDirPath != null)
         {
             //initialize directories
             File outputDir = outputDirPath.getAbsoluteFile();
 
 	    if (   (!outputDir.exists())
 		&& (!outputDir.mkdirs()))
             {
                 log.warn("Cannot create output folder: " + outputDir);
             }
         
             //execute task  
             task = buildTask(log);
 
             if  (task != null)
             {
                 log.info("Running QueryJ " + version);
         
                 task.execute();
 
                 running = true;
             }
             else
             {
                 log.warn("task is null");
             }
         }
         else
         {
             log.error("outputDir is null");
         }
 
         if (!running)
         {
             log.error("NOT running QueryJ " + version);
             throw new MojoExecutionException("QueryJ could not start");
         }
     }
 
     /**
      * Retrieves the pom.properties bundled within the QueryJ jar.
      * @param log the Maven log.
      * @return such information.
      * @precondition log != null
      */
     protected Properties retrievePomProperties(final Log log)
     {
         Properties result = null;
 
         try
         {
             InputStream pomProperties =
                 getClass().getClassLoader().getResourceAsStream(POM_PROPERTIES_LOCATION);
 
             result = new Properties();
 
             result.load(pomProperties);
         }
         catch (final IOException ioException)
         {
             log.warn(
                 "Strange... Cannot read my own " + POM_PROPERTIES_LOCATION,
                 ioException);
         }
 
         return result;
     }
 
     /**
      * Builds the QueryJ task.
      * @param log the Maven log.
      * @return such info.
      * @precondition log != null
      */
     protected QueryJTask buildTask(final Log log)
     {
         QueryJTask result = new QueryJTask();
 
         Project project = new AntProjectAdapter(new Project(), log);
 
         result.setProject(project);
 
         Path path = new Path(project);
         result.setClasspath(path);
         
         log.debug("Catalog: " + getCatalog());
         result.setCatalog(getCatalog());
 
         log.debug("Driver: " + getDriver());
         result.setDriver(getDriver());
 
         log.debug("JNDI DataSource: " + getJndiDataSource());
         result.setJndiDataSource(getJndiDataSource());
 
         log.debug("Output dir: " + getOutputDir());
         result.setOutputdir(getOutputDir());
 
         log.debug("Package name: " + getPackageName());
         result.setPackage(getPackageName());
 
         log.debug("Repository: " + getRepository());
         result.setRepository(getRepository());
 
         log.debug("Schema: " + getSchema());
         result.setSchema(getSchema());
 
         log.debug("Url: " + getUrl());
         result.setUrl(getUrl());
 
         log.debug("Username: " + getUsername());
         result.setUsername(getUsername());
 
        log.debug("Password: " + getPassword());
         result.setPassword(getPassword());
 
         log.debug("Generate Mock DAO implementation? " + getGenerateMockDAOImplementation());
         result.setGenerateMockDAOImplementation(
             "" + getGenerateMockDAOImplementation());
 
         log.debug("Generate XML DAO implementation? " + getGenerateXmlDAOImplementation());
         result.setGenerateXMLDAOImplementation(
             "" + getGenerateXmlDAOImplementation());
 
         log.debug("Generate tests? " + getGenerateTests());
         result.setGenerateTests(
             "" + getGenerateTests());
         
         log.debug("Custom SQL model: " + getCustomSqlModel());
         result.setCustomSqlModel(getCustomSqlModel());
 
         log.debug("SQL XML file: " + getSqlXmlFile());
         result.setSqlXmlFile(getSqlXmlFile());
 
         log.debug("Header file: " + getHeaderFile());
         result.setHeaderfile(getHeaderFile());
         
         log.debug("Extract functions? " + getExtractFunctions());
         result.setExtractFunctions(
             "" + getExtractFunctions());
 
         log.debug("Extract procedures? " + getExtractProcedures());
         result.setExtractProcedures(
             "" + getExtractProcedures());
         
         log.debug("Grammar bundle: " + getGrammarbundle());
         result.setGrammarbundle(getGrammarbundle());
 
         log.debug("Output subfolders: " + getUseOutputSubfolders());
         result.setOutputdirsubfolders("" + getUseOutputSubfolders());
 
         buildExternallyManagedFields(result);
         buildTables(result);
 
         String encoding = getEncoding();
 
         if (encoding == null)
         {
             log.warn("Using default (platform-dependent) encoding to generate QueryJ sources");
         }
         else
         {
             log.info("Using encoding: '" + encoding + "' to generate QueryJ sources");
         }
         result.setEncoding(encoding);
 
         return result;
     }
     
     /**
      * Builds the external managed fields list.
      * @param task the task.
      */
     protected void buildExternallyManagedFields(
         final QueryJTask task)
     {
         ExternallyManagedField[] array = getExternallyManagedFields();
 
         int count = (array == null) ? 0 : array.length;
         AntExternallyManagedFieldsElement element;
         ExternallyManagedField field;
         AntFieldElement fieldElement;
 
         if  (count > 0)
         {
             element = 
                 (AntExternallyManagedFieldsElement) task.createDynamicElement(
                     "externally-managed-fields");
             
             for (int index = 0; index < count; index++)
             {
                 field = array[index];
 
                 if  (field != null)
                 {
                     fieldElement = 
                         (AntFieldElement) element.createDynamicElement("field");
                 
                     fieldElement.setDynamicAttribute("name", field.getName());
 
                     fieldElement.setDynamicAttribute(
                         "table-name", field.getTableName());
 
                     fieldElement.setDynamicAttribute(
                         "keyword", field.getKeyword());
 
                     fieldElement.setDynamicAttribute(
                         "retrieval-query", field.getRetrievalQuery());
                 }
             }
         }
     }
     
     /**
      * Builds the table list.
      * @param task the task.
      */
     protected void buildTables(final QueryJTask task)
     {
         Table[] array = getTables();
         Table table;
         AntTablesElement element;
         AntTableElement tableElement;
         List<Field> fields;
         int fieldCount;
         Field field;
         AntFieldElement fieldElement;
 
         int count = (array == null) ? 0 : array.length;
 
         if  (count > 0)
         {
             element = 
                 (AntTablesElement) task.createDynamicElement("tables");
             
             for (int index = 0; index < count; index++)
             {
                 table = array[index];
 
                 tableElement = 
                     (AntTableElement) element.createDynamicElement("table");
                 
                 tableElement.setDynamicAttribute("name", table.getName());
                 
                 fields = table.getFields();
 
                 fieldCount = (fields == null) ? 0 : fields.size();
 
                 for (int fieldIndex = 0; fieldIndex < fieldCount; fieldIndex++)
                 {
                     field = fields.get(fieldIndex);
 
                     if  (field != null)
                     {
                         fieldElement = 
                             (AntFieldElement) tableElement.createDynamicElement("field");
                         fieldElement.setDynamicAttribute(
                             "name", field.getName());
                         fieldElement.setDynamicAttribute(
                             "type", field.getType());
                         fieldElement.setDynamicAttribute(
                             "pk", field.getPk());
                     }
                 }
             }
         }
     }
 }
