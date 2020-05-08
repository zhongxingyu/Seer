 //;-*- mode: java -*-
 /*
                         QueryJ
 
     Copyright (C) 2002-2006  Jose San Leandro Armendariz
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
  * Description: Generates QueryJ classes using Ant.
  *
  */
 package org.acmsl.queryj.tools.ant;
 
 /*
  * Importing some project classes.
  */
 import org.acmsl.queryj.tools.ant.AntExternallyManagedFieldsElement;
 import org.acmsl.queryj.tools.ant.AntTablesElement;
 import org.acmsl.queryj.tools.handlers.ParameterValidationHandler;
 import org.acmsl.queryj.tools.QueryJBuildException;
 import org.acmsl.queryj.tools.QueryJCommand;
 import org.acmsl.queryj.tools.QueryJSettings;
 import org.acmsl.queryj.tools.QueryJChain;
 import org.acmsl.queryj.tools.logging.QueryJAntLog;
 
 /*
  * Importing some ACM-SL classes.
  */
 import org.acmsl.commons.logging.UniqueLogFactory;
 
 /*
  * Importing some JDK classes.
  */
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 /*
  * Importing some Ant classes.
  */
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.DynamicConfigurator;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.types.Path;
 import org.apache.tools.ant.types.Reference;
 
 /*
  * Importing some Apache Commons Logging classes.
  */
 import org.apache.commons.logging.Log;
 
 /**
  * Generates QueryJ classes using Ant.
  * @author <a href="mailto:chous@acm-sl.org"
            >Jose San Leandro</a>
  */
 public class QueryJTask
     extends     Task
     implements  QueryJSettings,
                 DynamicConfigurator
 {
     /**
      * The <code>QueryJChain</code> delegee.
      */
     private QueryJChain m__QueryJChain;
 
     /**
      * The classpath.
      */
     private Path m__Classpath;
 
     /**
      * The nested tables.
      */
     private AntTablesElement m__Tables;
 
     /**
      * The externally-managed fields.
      */
     private AntExternallyManagedFieldsElement m__ExternallyManagedFields;
 
     /**
      * Creates a <code>QueryJTask</code> instance.
      */
     public QueryJTask()
     {
         // AntQueryJChain is an inner class defined below.
         immutableSetQueryJChain(new AntQueryJChain());
     }
 
     /**
      * Specifies the <code>QueryJChain</code> to delegate.
      * @param delegee such instance.
      */
     protected final void immutableSetQueryJChain(final QueryJChain delegee)
     {
         m__QueryJChain = delegee;
     }
 
     /**
      * Specifies the <code>QueryJChain</code> to delegate.
      * @param delegee such instance.
      */
     protected void setQueryJChain(final QueryJChain delegee)
     {
         immutableSetQueryJChain(delegee);
     }
 
     /**
      * Retrieves the delegating <code>QueryJChain</code> instance.
      * @return such instance.
      */
     protected QueryJChain getQueryJChain()
     {
         return m__QueryJChain;
     }
 
     /**
      * Specifies the properties file.
      * @param file such file.
      */
     public void setSettingsFile(final File file)
     {
         setSettingsFile(file, getQueryJChain());
     }
 
     /**
      * Specifies the properties file.
      * @param file such file.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setSettingsFile(final File file, final QueryJChain delegee)
     {
         delegee.setSettingsFile(file);
     }
 
     /**
      * Retrieves the properties file.
      * @return such file.
      */
     public File getSettingsFile()
     {
         return getSettingsFile(getQueryJChain());
     }
 
     /**
      * Retrieves the properties file.
      * @param delegee the delegee.
      * @return such file.
      * @precondition delegee != null
      */
     protected File getSettingsFile(final QueryJChain delegee)
     {
         return delegee.getSettingsFile();
     }
 
     /**
      * Specifies the driver.
      * @param driver the new driver.
      */
     public void setDriver(final String driver)
     {
         setDriver(driver, getQueryJChain());
     }
 
     /**
      * Specifies the driver.
      * @param driver the new driver.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setDriver(final String driver, final QueryJChain delegee)
     {
         delegee.setDriver(driver);
     }
 
     /**
      * Retrieves the driver.
      * @return such information.
      */
     public String getDriver()
     {
         return getDriver(getQueryJChain());
     }
 
     /**
      * Retrieves the driver.
      * @param delegee the delegee.
      * @return such information.
      * @precondition delegee != null
      */
     protected String getDriver(final QueryJChain delegee)
     {
         return delegee.getDriver();
     }
 
     /**
      * Specifies the url.
      * @param url the new url.
      */
     public void setUrl(final String url)
     {
         setUrl(url, getQueryJChain());
     }
 
     /**
      * Specifies the url.
      * @param url the new url.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setUrl(final String url, final QueryJChain delegee)
     {
         delegee.setUrl(url);
     }
 
     /**
      * Retrieves the url.
      * @return such information.
      */
     public String getUrl()
     {
         return getUrl(getQueryJChain());
     }
 
     /**
      * Retrieves the url.
      * @param delegee the delegee.
      * @return such information.
      * @precondition delegee != null
      */
     protected String getUrl(final QueryJChain delegee)
     {
         return delegee.getUrl();
     }
 
     /**
      * Specifies the username.
      * @param username the new username.
      */
     public void setUsername(final String username)
     {
         setUsername(username, getQueryJChain());
     }
 
     /**
      * Specifies the username.
      * @param username the new username.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setUsername(
         final String username, final QueryJChain delegee)
     {
         delegee.setUsername(username);
     }
 
     /**
      * Retrieves the username.
      * @return such information.
      */
     public String getUsername()
     {
         return getUsername(getQueryJChain());
     }
 
     /**
      * Retrieves the username.
      * @param delegee the delegee.
      * @return such information.
      * @precondition delegee != null
      */
     protected String getUsername(final QueryJChain delegee)
     {
         return delegee.getUsername();
     }
 
     /**
      * Specifies the password.
      * @param password the new password.
      */
     public void setPassword(final String password)
     {
         setPassword(password, getQueryJChain());
     }
 
     /**
      * Specifies the password.
      * @param password the new password.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setPassword(
         final String password, final QueryJChain delegee)
     {
         delegee.setPassword(password);
     }
 
     /**
      * Retrieves the password.
      * @return such information.
      */
     public String getPassword()
     {
         return getPassword(getQueryJChain());
     }
 
     /**
      * Retrieves the password.
      * @param delegee the delegee.
      * @return such information.
      * @precondition delegee != null
      */
     protected String getPassword(final QueryJChain delegee)
     {
         return delegee.getPassword();
     }
 
     /**
      * Specifies the catalog.
      * @param catalog the new catalog.
      */
     public void setCatalog(final String catalog)
     {
         setCatalog(catalog, getQueryJChain());
     }
 
     /**
      * Specifies the catalog.
      * @param catalog the new catalog.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setCatalog(
         final String catalog, final QueryJChain delegee)
     {
         delegee.setCatalog(catalog);
     }
 
     /**
      * Retrieves the catalog.
      * @return such information.
      */
     public String getCatalog()
     {
         return getCatalog(getQueryJChain());
     }
 
     /**
      * Retrieves the catalog.
      * @param delegee the delegee.
      * @return such information.
      * @precondition delegee != null
      */
     protected String getCatalog(final QueryJChain delegee)
     {
         return delegee.getCatalog();
     }
 
     /**
      * Specifies the schema.
      * @param schema the new schema.
      */
     public void setSchema(final String schema)
     {
         setSchema(schema, getQueryJChain());
     }
 
     /**
      * Specifies the schema.
      * @param schema the new schema.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setSchema(
         final String schema, final QueryJChain delegee)
     {
         delegee.setSchema(schema);
     }
 
     /**
      * Retrieves the schema.
      * @return such information.
      */
     public String getSchema()
     {
         return getSchema(getQueryJChain());
     }
 
     /**
      * Retrieves the schema.
      * @param delegee the delegee.
      * @return such information.
      * @precondition delegee != null
      */
     protected String getSchema(final QueryJChain delegee)
     {
         return delegee.getSchema();
     }
 
     /**
      * Specifies the repository.
      * @param repository the new repository.
      */
     public void setRepository(final String repository)
     {
         setRepository(repository, getQueryJChain());
     }
 
     /**
      * Specifies the repository.
      * @param repository the new repository.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setRepository(
         final String repository, final QueryJChain delegee)
     {
         delegee.setRepository(repository);
     }
 
     /**
      * Retrieves the repository.
      * @return such information.
      */
     public String getRepository()
     {
         return getRepository(getQueryJChain());
     }
 
     /**
      * Retrieves the repository.
      * @param delegee the delegee.
      * @return such information.
      * @precondition delegee != null
      */
     protected String getRepository(final QueryJChain delegee)
     {
         return delegee.getRepository();
     }
 
     /**
      * Specifies the package.
      * @param packageName the new package.
      */
     public void setPackage(final String packageName)
     {
         setPackage(packageName, getQueryJChain());
     }
 
     /**
      * Specifies the package.
      * @param packageName the new package.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setPackage(
         final String packageName, final QueryJChain delegee)
     {
         delegee.setPackage(packageName);
     }
 
     /**
      * Retrieves the package.
      * @return such information.
      */
     public String getPackage()
     {
         return getPackage(getQueryJChain());
     }
 
     /**
      * Retrieves the package.
      * @param delegee the delegee.
      * @return such information.
      * @precondition delegee != null
      */
     protected String getPackage(final QueryJChain delegee)
     {
         return delegee.getPackage();
     }
 
     /**
      * Specifies the output dir.
      * @param outputdir the new output dir.
      */
     public void setOutputdir(final File outputdir)
     {
         setOutputdir(outputdir, getQueryJChain());
     }
 
     /**
      * Specifies the output dir.
      * @param outputdir the new output dir.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setOutputdir(
         final File outputdir, final QueryJChain delegee)
     {
         delegee.setOutputdir(outputdir);
     }
 
     /**
      * Retrieves the output dir.
      * @return such information.
      */
     public File getOutputdir()
     {
         return getOutputdir(getQueryJChain());
     }
 
     /**
      * Retrieves the output dir.
      * @param delegee the delegee.
      * @return such information.
      * @precondition delegee != null
      */
     protected File getOutputdir(final QueryJChain delegee)
     {
         return delegee.getOutputdir();
     }
 
     /**
      * Specifies the header file.
      * @param headerfile the new header file.
      */
     public void setHeaderfile(final File headerfile)
     {
         setHeaderfile(headerfile, getQueryJChain());
     }
 
     /**
      * Specifies the header file.
      * @param headerfile the new header file.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setHeaderfile(
         final File headerfile, final QueryJChain delegee)
     {
         delegee.setHeaderfile(headerfile);
     }
 
     /**
      * Retrieves the header file.
      * @return such information.
      */
     public File getHeaderfile()
     {
         return getHeaderfile(getQueryJChain());
     }
 
     /**
      * Retrieves the header file.
      * @param delegee the delegee.
      * @return such information.
      * @precondition delegee != null
      */
     protected File getHeaderfile(final QueryJChain delegee)
     {
         return delegee.getHeaderfile();
     }
 
     /**
      * Specifies whether to use subfolders.
      * @param outputDirSubFolders such setting.
      */
     public void setOutputdirsubfolders(final String outputDirSubFolders)
     {
         setOutputdirsubfolders(outputDirSubFolders, getQueryJChain());
     }
 
     /**
      * Specifies whether to use subfolders.
      * @param outputDirSubFolders such setting.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setOutputdirsubfolders(
         final String outputDirSubFolders, final QueryJChain delegee)
     {
         delegee.setOutputdirsubfolders(outputDirSubFolders);
     }
 
     /**
      * Retrieves whether to use subfolders.
      * @return such setting.
      */
     public String getOutputdirsubfolders()
     {
         return getOutputdirsubfolders(getQueryJChain());
     }
 
     /**
      * Retrieves whether to use subfolders.
      * @param delegee the delegee.
      * @return such setting.
      * @precondition delegee != null
      */
     protected String getOutputdirsubfolders(final QueryJChain delegee)
     {
         return delegee.getOutputdirsubfolders();
     }
 
     /**
      * Specifies whether to extract the tables.
      * @param extractTables the procedure extraction setting.
      */
     public void setExtractTables(final String extractTables)
     {
         setExtractTables(extractTables, getQueryJChain());
     }
 
     /**
      * Specifies whether to extract the tables.
      * @param extractTables the procedure extraction setting.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setExtractTables(
         final String extractTables, final QueryJChain delegee)
     {
         delegee.setExtractTables(extractTables);
     }
 
     /**
      * Retrieves whether to extract the tables.
      * @return such setting.
      */
     public String getExtractTables()
     {
         return getExtractTables(getQueryJChain());
     }
 
     /**
      * Retrieves whether to extract the tables.
      * @param delegee the delegee.
      * @return such setting.
      * @precondition delegee != null
      */
     protected String getExtractTables(final QueryJChain delegee)
     {
         return delegee.getExtractTables();
     }
 
     /**
      * Retrieves the "extract-tables" flag.
      * @return such flag.
      */
     protected boolean getExtractTablesFlag()
     {
         return getExtractTablesFlag(getQueryJChain());
     }
 
     /**
      * Retrieves the "extract-tables" flag.
      * @return such flag.
      */
     protected boolean getExtractTablesFlag(final QueryJChain delegee)
     {
         return delegee.getExtractTablesFlag();
     }
 
     /**
      * Specifies whether to extract the procedures.
      * @param extractProcedures the procedure extraction setting.
      */
     public void setExtractProcedures(final String extractProcedures)
     {
         setExtractProcedures(extractProcedures, getQueryJChain());
     }
 
     /**
      * Specifies whether to extract the procedures.
      * @param extractProcedures the procedure extraction setting.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setExtractProcedures(
         final String extractProcedures, final QueryJChain delegee)
     {
         delegee.setExtractProcedures(extractProcedures);
     }
 
     /**
      * Retrieves whether to extract the procedures.
      * @return such setting.
      */
     public String getExtractProcedures()
     {
         return getExtractProcedures(getQueryJChain());
     }
 
     /**
      * Retrieves whether to extract the procedures.
      * @param delegee the delegee.
      * @return such setting.
      * @precondition delegee != null
      */
     protected String getExtractProcedures(final QueryJChain delegee)
     {
         return delegee.getExtractProcedures();
     }
 
     /**
      * Specifies whether to extract the functions.
      * @param extractFunctions the procedure extraction setting.
      */
     public void setExtractFunctions(final String extractFunctions)
     {
         setExtractFunctions(extractFunctions, getQueryJChain());
     }
 
     /**
      * Specifies whether to extract the functions.
      * @param extractFunctions the procedure extraction setting.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setExtractFunctions(
         final String extractFunctions, final QueryJChain delegee)
     {
         delegee.setExtractFunctions(extractFunctions);
     }
 
     /**
      * Retrieves whether to extract the functions.
      * @return such setting.
      */
     public String getExtractFunctions()
     {
         return getExtractFunctions(getQueryJChain());
     }
 
     /**
      * Retrieves whether to extract the functions.
      * @param delegee the delegee.
      * @return such setting.
      * @precondition delegee != null
      */
     protected String getExtractFunctions(final QueryJChain delegee)
     {
         return delegee.getExtractFunctions();
     }
 
     /**
      * Specifices the JNDI location for the data sources.
      * @param jndiLocation the JNDI location.
      */
     public void setJndiDataSource(final String jndiLocation)
     {
         setJndiDataSource(jndiLocation, getQueryJChain());
     }
 
     /**
      * Specifices the JNDI location for the data sources.
      * @param jndiLocation the JNDI location.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setJndiDataSource(
         final String jndiLocation, final QueryJChain delegee)
     {
         delegee.setJndiDataSource(jndiLocation);
     }
 
     /**
      * Retrieves the JNDI location for the data sources.
      * @return such information.
      */
     public String getJndiDataSource()
     {
         return getJndiDataSource(getQueryJChain());
     }
 
     /**
      * Retrieves the JNDI location for the data sources.
      * @param delegee the delegee.
      * @return such information.
      * @precondition delegee != null
      */
     protected String getJndiDataSource(final QueryJChain delegee)
     {
         return delegee.getJndiDataSource();
     }
 
     /**
      * Specifies whether to generate Mock DAO implementations.
      * @param generate such setting.
      */
     public void setGenerateMockDAOImplementation(final String generate)
     {
         setGenerateMockDAOImplementation(generate, getQueryJChain());
     }
 
     /**
      * Specifies whether to generate Mock DAO implementations.
      * @param generate such setting.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setGenerateMockDAOImplementation(
         final String generate, final QueryJChain delegee)
     {
         delegee.setGenerateMockDAOImplementation(generate);
     }
 
     /**
      * Retrieves whether to generate Mock DAO implementations.
      * @return such setting.
      */
     public String getGenerateMockDAOImplementation()
     {
         return getGenerateMockDAOImplementation(getQueryJChain());
     }
 
     /**
      * Retrieves whether to generate Mock DAO implementations.
      * @return such setting.
      */
     protected String getGenerateMockDAOImplementation(
         final QueryJChain delegee)
     {
         return delegee.getGenerateMockDAOImplementation();
     }
 
     /**
      * Retrieves whether to generate XML DAO implementations.
      * @return such setting.
      */
     public String getGenerateXMLDAOImplementation()
     {
         return getGenerateXMLDAOImplementation(getQueryJChain());
     }
 
     /**
      * Retrieves whether to generate XML DAO implementations.
      * @param delegee the delegee.
      * @return such setting.
      * @precondition delegee != null
      */
     protected String getGenerateXMLDAOImplementation(
         final QueryJChain delegee)
     {
         return delegee.getGenerateXMLDAOImplementation();
     }
 
     /**
      * Specifies whether to allow empty repository DAO.
      * @param allow such setting.
      */
     public void setAllowEmptyRepositoryDAO(final String allow)
     {
         setAllowEmptyRepositoryDAO(allow, getQueryJChain());
     }
 
     /**
      * Specifies whether to allow empty repository DAO.
      * @param allow such setting.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setAllowEmptyRepositoryDAO(
         final String allow, final QueryJChain delegee)
     {
         delegee.setAllowEmptyRepositoryDAO(allow);
     }
 
     /**
      * Retrieves whether to allow empty repository DAO.
      * @return such setting.
      */
     public String getAllowEmptyRepositoryDAO()
     {
         return getAllowEmptyRepositoryDAO(getQueryJChain());
     }
 
     /**
      * Retrieves whether to allow empty repository DAO.
      * @param delegee the delegee.
      * @return such setting.
      * @precondition delegee != null
      */
     protected String getAllowEmptyRepositoryDAO(final QueryJChain delegee)
     {
         return delegee.getAllowEmptyRepositoryDAO();
     }
 
     /**
      * Specifies whether to implement marker interfaces.
      * @param implement such setting.
      */
     public void setImplementMarkerInterfaces(final String implement)
     {
         setImplementMarkerInterfaces(implement, getQueryJChain());
     }
 
     /**
      * Specifies whether to implement marker interfaces.
      * @param implement such setting.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setImplementMarkerInterfaces(
         final String implement, final QueryJChain delegee)
     {
         delegee.setImplementMarkerInterfaces(implement);
     }
 
     /**
      * Retrieves whether to implement marker interfaces.
      * @return such setting.
      */
     public String getImplementMarkerInterfaces()
     {
         return getImplementMarkerInterfaces(getQueryJChain());
     }
 
     /**
      * Retrieves whether to implement marker interfaces.
      * @param delegee the delegee.
      * @return such setting.
      * @precondition delegee != null
      */
     protected String getImplementMarkerInterfaces(final QueryJChain delegee)
     {
         return delegee.getImplementMarkerInterfaces();
     }
 
     /**
      * Specifies the custom-sql model.
      * @param model the model.
      */
     public void setCustomSqlModel(final String model)
     {
         setCustomSqlModel(model, getQueryJChain());
     }
 
     /**
      * Specifies the custom-sql model.
      * @param model the model.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setCustomSqlModel(
         final String model, final QueryJChain delegee)
     {
         delegee.setCustomSqlModel(model);
     }
 
     /**
      * Retrieves the custom-sql model.
      * @return such model.
      */
     public String getCustomSqlModel()
     {
         return getCustomSqlModel(getQueryJChain());
     }
 
     /**
      * Retrieves the custom-sql model.
      * @param delegee the delegee.
      * @return such model.
      * @precondition delegee != null
      */
     protected String getCustomSqlModel(final QueryJChain delegee)
     {
         return delegee.getCustomSqlModel();
     }
 
     /**
      * Specifies the sql.xml file.
      * @param file the new file.
      */
     public void setSqlXmlFile(final File file)
     {
         setSqlXmlFile(file, getQueryJChain());
     }
 
     /**
      * Specifies the sql.xml file.
      * @param file the new file.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setSqlXmlFile(final File file, final QueryJChain delegee)
     {
         delegee.setSqlXmlFile(file);
     }
 
     /**
      * Retrieves the sql.xml file.
      * @return such information.
      */
     public File getSqlXmlFile()
     {
         return getSqlXmlFile(getQueryJChain());
     }
 
     /**
      * Retrieves the sql.xml file.
      * @param delegee the delegee.
      * @return such information.
      * @precondition delegee != null
      */
     protected File getSqlXmlFile(final QueryJChain delegee)
     {
         return delegee.getSqlXmlFile();
     }
 
     /**
      * Specifies the grammar bundle.
      * @param grammarBundle the new grammar bundle.
      */
     public void setGrammarbundle(final String grammarBundle)
     {
         setGrammarbundle(grammarBundle, getQueryJChain());
     }
 
     /**
      * Specifies the grammar bundle.
      * @param grammarBundle the new grammar bundle.
      * @param delegee the delegee.
      * @precondition delegee != null
      */
     protected void setGrammarbundle(
         final String grammarBundle, final QueryJChain delegee)
     {
         delegee.setGrammarbundle(grammarBundle);
     }
 
     /**
      * Retrieves the grammar bundle.
      * @return such information.
      */
     public String getGrammarbundle()
     {
         return getGrammarbundle(getQueryJChain());
     }
 
     /**
      * Retrieves the grammar bundle.
      * @param delegee the delegee.
      * @return such information.
      * @precondition delegee != null
      */
     protected String getGrammarbundle(final QueryJChain delegee)
     {
         return delegee.getGrammarbundle();
     }
 
     /**
      * Specifies the classpath.
      * @param classpath the new classpath.
      */
     protected final void immutableSetClasspath(final Path classpath)
     {
         m__Classpath = classpath;
     }
 
     /**
      * Specifies the classpath.
      * @param classpath the new classpath.
      */
     public void setClasspath(final Path classpath)
     {
         immutableSetClasspath(classpath);
     }
 
     /**
      * Creates the classpath structure.
      * @return such path.
      */
     public Path createClasspath()
     {
         Path t_Classpath = getClasspath();
 
         if  (t_Classpath == null)
         {
             t_Classpath = new Path(getProject());
 
             setClasspath(t_Classpath);
         }
 
         return t_Classpath.createPath();
     }
 
     /**
      * Reference to the classpath.
      * @param classpathReference the reference to the class path.
      */
     public void setClasspathRef(final Reference classpathReference)
     {
         Path t_Path = createClasspath();
 
         if  (t_Path != null)
         {
             t_Path.setRefid(classpathReference);
         }
     }
 
     /**
      * Retrieves the classpath.
      * @return such information.
      */
     public Path getClasspath()
     {
         return m__Classpath;
     }
 
     /**
      * Specifies the "tables" nested element.
      * @param tables the tables xml element.
      */
     protected void setTables(final AntTablesElement tables)
     {
         m__Tables = tables;
     }
 
     /**
      * Retrieves the "tables" nested element.
      * @return such information.
      */
     public AntTablesElement getTables()
     {
         return m__Tables;
     }
 
     /**
      * Requests the chained logic to be performed.
      * @throws BuildException whenever the required
      * parameters are not present or valid.
      */
     public void execute()
         throws  BuildException
     {
         execute(getQueryJChain());
     }
 
     /**
      * Requests the chained logic to be performed.
      * @param delegee the delegee.
      * @throws BuildException whenever the required
      * parameters are not present or valid.
      */
     protected void execute(final QueryJChain delegee)
         throws  BuildException
     {
         try
         {
             delegee.process();
         }
         catch  (final QueryJBuildException buildException)
         {
             throw
                 new BuildException(
                     buildException.getMessage(), buildException);
         }
     }
 
     /**
      * Specifies the "externally-managed-fields" nested element.
      * @param externallyManagedFields the externally-managed-fields xml
      * element.
      */
     protected void setExternallyManagedFields(
         final AntExternallyManagedFieldsElement externallyManagedFields)
     {
         m__ExternallyManagedFields = externallyManagedFields;
     }
 
     /**
      * Retrieves the "externally-managed-fields" nested element.
      * @return such information.
      */
     public AntExternallyManagedFieldsElement getExternallyManagedFields()
     {
         return m__ExternallyManagedFields;
     }
 
     /**
      * Customizes <code>QueryJChain</code> to get parameters from Ant.
      * @author <a href="mailto:chous@acm-sl.org"
                >Jose San Leandro</a>
      */
     protected class AntQueryJChain
         extends  QueryJChain
     {
         /**
          * Creates an <code>AntQueryJChain</code> instance.
          */
         public AntQueryJChain() {};
                 
         /**
          * Builds the command.
          * @return the initialized command.
          */
         protected QueryJCommand buildCommand()
         {
             return
                 buildCommand(
                     new QueryJCommand(new QueryJAntLog(getProject())));
         }
 
         /**
          * Maps the attributes in the command map.
          * @param attributes the command attributes.
          * @param driver the JDBC driver.
          * @param url the JDBC url.
          * @param username the JDBC username.
          * @param password the JDBC password.
          * @param catalog the JDBC catalog.
          * @param schema the JDBC schema.
          * @param repository the repository.
          * @param packageName the base package of the generated sources.
          * @param outputDir the output folder.
          * @param header the copyright header.
          * @param outputDirSubfolders whether to use main/ and test/ as
          * subfolders.
          * @param extractTables whether to extract tables or not.
          * @param extractProcedures whether to extract the procedures or not.
          * @param extractFunctions whether to extract the functions or not.
          * @param jndiDataSource the location in JNDI of the
          * <code>DataSource</code>.
          * @param generateMockDAOImplementation whether to generate Mock DAOs.
          * @param generateXmlDAOImplementation whether to generate XML DAOs.
          * @param allowEmptyRepositoryDAO whether to generate a repository
          * DAO even tough it'll contain no custom queries..
          * @param implementMarkerInterfaces whether to make some generated 
          * sources implement <code>org.acmsl.commons.patterns</code> <i>Marker</i>
          * interfaces.
          * @param customSqlModel the format of the custom SQL file.
          * @param sqlXmlFile the file containing the custom SQL.
          * @param grammarBundle the grammar with irregular singular and plural
          * forms of the table names.
          * @param tables the custom tables.
          * @param externallyManagedFields the externally-managed fields.
          * @param classpath the classpath.
          * @precondition attributes != null
          */
         protected void mapAttributes(
             final Map attributes,
             final String driver,
             final String url,
             final String username,
             final String password,
             final String catalog,
             final String schema,
             final String repository,
             final String packageName,
             final File outputdir,
             final File header,
             final boolean outputdirsubfolders,
             final boolean extractTables,
             final boolean extractProcedures,
             final boolean extractFunctions,
             final String jndiDataSource,
             final boolean generateMockDAOImplementation,
             final boolean generateXmlDAOImplementation,
             final boolean allowEmptyRepositoryDAO,
             final boolean implementMarkerInterfaces,
             final String customSqlModel,
             final File sqlXmlFile,
             final String grammarBundle)
         {
             super.mapAttributes(
                 attributes,
                 driver,
                 url,
                 username,
                 password,
                 catalog,
                 schema,
                 repository,
                 packageName,
                 outputdir,
                 header,
                 outputdirsubfolders,
                 extractTables,
                 extractProcedures,
                 extractFunctions,
                 jndiDataSource,
                 generateMockDAOImplementation,
                 generateXmlDAOImplementation,
                 allowEmptyRepositoryDAO,
                 implementMarkerInterfaces,
                 customSqlModel,
                 sqlXmlFile,
                 grammarBundle);
 
             mapAttributes(
                 attributes,
                 getTables(),
                 getExternallyManagedFields(),
                 getClasspath());
         }
 
         /**
          * Maps the attributes in the command map.
          * @param attributes the command attributes.
          * @param tables the custom tables.
          * @param externallyManagedFields the externally-managed fields.
          * @param classpath the classpath.
          * @precondition attributes != null
          */
         protected void mapAttributes(
             final Map attributes,
             final AntTablesElement tables,
             final AntExternallyManagedFieldsElement externallyManagedFields,
             final Path classpath)
         {
             if  (attributes != null)
             {
                 if  (classpath != null)
                 {
                     attributes.put(
                         ParameterValidationHandler.CLASSPATH,
                         classpath);
                 }
 
                 if  (tables != null)
                 {
                     attributes.put(
                         ParameterValidationHandler.TABLES,
                         tables);
                 }
 
                 if  (externallyManagedFields != null)
                 {
                     attributes.put(
                         ParameterValidationHandler.EXTERNALLY_MANAGED_FIELDS,
                         externallyManagedFields);
                 }
             }
         }
     }
 
     /**
      * Specifies a dynamic attribute.
      * @param name the attribute name.
      * @param value the attribute value.
      */
     public void setDynamicAttribute(
         final String name, final String value)
     {
         throw
             new BuildException(
                   "No dynamic attributes are supported ("
                 + name + "=" + value + ")");
     }
 
     /**
      * Creates a dynamic element.
      * @param name the element's name.
      * @return the object.
      * @throws BuildException if the element is not supported.
      */
     public Object createDynamicElement(final String name)
     {
         Object result = null;
 
         if  ("tables".equals(name))
         {
             AntTablesElement t_ateResult = null;
 
             if  (!getExtractTablesFlag())
             {
                 throw new BuildException("Table extraction is disabled.");
             }
             else
             {
                 t_ateResult = new AntTablesElement();
             }
 
             setTables(t_ateResult);
 
             result = t_ateResult;
         }
         else if  ("externally-managed-fields".equals(name))
         {
             AntExternallyManagedFieldsElement t_aemfeResult =
                 new AntExternallyManagedFieldsElement();
 
             setExternallyManagedFields(t_aemfeResult);
 
             result = t_aemfeResult;
         }
         else
         {
             throw new BuildException(name + " elements are not supported");
         }
 
         return result;
     }
 }
