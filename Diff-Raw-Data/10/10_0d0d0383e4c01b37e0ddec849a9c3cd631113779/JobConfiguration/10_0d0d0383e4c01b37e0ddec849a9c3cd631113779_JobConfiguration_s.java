 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.cmd;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.client.core.businessprocess.ClientTestStrings;
 import org.eclipse.jubula.client.core.businessprocess.db.TestJobBP;
 import org.eclipse.jubula.client.core.businessprocess.db.TestSuiteBP;
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.model.IAUTConfigPO;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.ITestJobPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.persistence.DatabaseConnectionInfo;
 import org.eclipse.jubula.client.core.preferences.database.DatabaseConnection;
 import org.eclipse.jubula.client.core.preferences.database.DatabaseConnectionConverter;
 import org.eclipse.jubula.client.core.preferences.database.H2ConnectionInfo;
 import org.eclipse.jubula.client.core.preferences.database.MySQLConnectionInfo;
 import org.eclipse.jubula.client.core.preferences.database.OracleConnectionInfo;
 import org.eclipse.jubula.client.core.preferences.database.PostGreSQLConnectionInfo;
 import org.eclipse.jubula.client.core.utils.LocaleUtil;
 import org.eclipse.jubula.tools.registration.AutIdentifier;
 import org.eclipse.osgi.util.NLS;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.converters.Converter;
 import com.thoughtworks.xstream.converters.MarshallingContext;
 import com.thoughtworks.xstream.converters.UnmarshallingContext;
 import com.thoughtworks.xstream.io.HierarchicalStreamReader;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 
 /**
  * One batchJob represents n Test Suite in 1 Project that should be executed.
  * @author BREDEX GmbH
  * @created Mar 29, 2006
  */
 @SuppressWarnings("synthetic-access")
 public class JobConfiguration {
     /** Separator between major and minor version numbers */
     public static final char VERSION_SEPARATOR = '.';
 
     /** String */
     private static final String CONFIGURATION = "configuration";  //$NON-NLS-1$
     
     /** configuration detail */
     private String m_projectName;
     /** configuration detail */
     private Integer m_projectMajor;
     /** configuration detail */
     private Integer m_projectMinor;
     /** configuration detail */
     private String m_db;
     /** configuration detail */
     private DatabaseConnectionInfo m_dbConnectionInfo;
     /** configuration detail */
     private String m_dbConnectionName;
     /** configuration detail */
     private String m_dbuser;
     /** configuration detail */
     private String m_dbpw;
     /** configuration detail */
     private String m_server;
     /** configuration detail */
     private String m_port;
     /** port of the server */
     private String m_serverport;
     /** configuration detail */
     private String m_resultDir;
     /** configuration detail */
     private String m_autConfigName;
     /** configuration detail */
     private List<String> m_testSuiteNames = new ArrayList<String>();
     /** the name of the Test Job to execute */
     private String m_testJobName;
     /** configuration detail */
     private Locale m_language;
     /** list for Test Suites */
     private List<ITestSuitePO> m_testSuites = new ArrayList<ITestSuitePO>();
     /** the Test Job to execute */
     private ITestJobPO m_testJob;
     /** list for Test Suites */
     private IAUTConfigPO m_autConfig;
     /** ID of Running AUT to test */
     private AutIdentifier m_autId;
     /** actual project */
     private IProjectPO m_project;
     /** actual testSuite */
     private int m_actualTestSuite = 0;
     /** where are the external data files */
     private String m_dataDir;
     /** timeout for this run */
     private int m_timeout = 0;
     /** flag to automatically take screenshots */
     private boolean m_autoScreenshot = true;
     /** flag to mark test execution as relevant or not */
     private boolean m_relevant = true;
     
     /**
      * constructor
      */
     public JobConfiguration() {
         super();
     }
 
     /**
      * @return String
      */
     public String getDb() {
         return m_db;
     }
 
     /**
      * @param db String
      */
     private void setDb(String db) {
         m_db = db;
     }
 
     /**
      * @return String
      */
     public String getDbpw() {
         return m_dbpw;
     }
 
     /**
      * @param dbpw String
      */
     private void setDbpw(String dbpw) {
         m_dbpw = dbpw;
     }
 
     /**
      * @return String
      */
     public String getDbuser() {
         return m_dbuser;
     }
 
     /**
      * @param dbuser String
      */
     private void setDbuser(String dbuser) {
         m_dbuser = dbuser;
     }
     
     /**
      * @return String
      */
     public DatabaseConnectionInfo getDbscheme() {
         return m_dbConnectionInfo;
     }
 
     /**
      * @param connectionInfo The connection information to use.
      */
     public void setDbscheme(DatabaseConnectionInfo connectionInfo) {
         m_dbConnectionInfo = connectionInfo;
     }
 
     /**
      * @return String
      */
     public String getDbConnectionName() {
         return m_dbConnectionName;
     }
 
     /**
      * @param connectionName The name of the connection information to use.
      */
     private void setDbConnectionName(String connectionName) {
         m_dbConnectionName = connectionName;
         setDbscheme(JobConfiguration.getConnectionInfoForName(connectionName));
     }
 
     /**
      * @return String
      */
     public String getProjectName() {
         return m_projectName;
 
     }
 
     /**
      * @return IProjectPO
      */
     public IProjectPO getProject() {
         return m_project;
     }
 
     /**
      * @param actualProject IProjectPO
      */
     public void setProject(IProjectPO actualProject) {
         m_project = actualProject;
     }
 
     /**
      * @return ITestSuitePO
      */
     public ITestSuitePO getActualTestSuite() {
         if (m_testSuites.size() > m_actualTestSuite) {
             return m_testSuites.get(m_actualTestSuite);
         }
         return null;
     }
 
     /**
      * @return int
      */
     public int getJobSize() {
         return m_testSuites.size();
     }
 
     /**
      * @return ITestSuitePO
      */
     public ITestSuitePO getNextTestSuite() {
         m_actualTestSuite++;
         if (m_testSuites.size() > m_actualTestSuite) {
             return m_testSuites.get(m_actualTestSuite);
         }
         return null;
     }
 
     /**
      * @return String
      */
     public String getPort() {
         return m_port;
     }
     
     /**
      * @return String
      */
     public String getServerPort() {
         return m_serverport;
     }
 
     /**
      * @return String
      */
     public String getServer() {
         return m_server;
     }
     
 
     /**
      * initializes the job configuration object after loading project
      * validates if choosen configuration is valid
      */
     public void initAndValidate() {
         // all needed properties set correctly?
         Validate.notNull(m_project, NLS.bind(
                 Messages.JobConfigurationValidateProjectExist,
                 new Object[] {String.valueOf(m_projectName), 
                     String.valueOf(m_projectMajor), 
                     String.valueOf(m_projectMinor)}));
         // searching for testsuites with the given names
         for (String name : m_testSuiteNames) {
             for (ITestSuitePO ts : TestSuiteBP.getListOfTestSuites()) {
                 if (ts.getName().equals(name)) {
                     m_testSuites.add(ts);
                     break;
                 }
             }
         }
         Validate.isTrue((m_testSuiteNames.size() == m_testSuites.size()), 
             Messages.JobConfigurationValidateTestSuiteExist);
 
         for (ITestJobPO tj : TestJobBP.getListOfTestJobs()) {
             if (tj.getName().equals(m_testJobName)) {
                 m_testJob = tj;
             }
         }
         
         if (!m_testSuites.isEmpty()) {
             // checking that all Test Suites are assigned to an AUT
             for (ITestSuitePO ts : m_testSuites) {
                 Validate.notNull(ts.getAut(), 
                         Messages.JobConfigurationValidateAnyAut);
             }
             
             // checking if specified AUT Config exists
             IAUTMainPO aut = getActualTestSuite().getAut();
             if (m_autConfigName != null) {
                 for (IAUTConfigPO config : aut.getAutConfigSet()) {
                     if (m_autConfigName.equals(config.getName())) {
                         m_autConfig = config;
                     }
                 }
                 Validate.notNull(m_autConfig, 
                         Messages.JobConfigurationValidateAutConf);
             }
 
             // LanguageCheck
             List <Locale> autLocales = aut.getLangHelper().getLanguageList();
             Validate.isTrue(autLocales.size() != 0, 
                 Messages.NoLanguageConfiguredInChoosenAUT);
             if (getLanguage() == null) {
                 if (autLocales.size() == 1) {
                     setLanguage(autLocales.get(0));
                 } else {
                     setLanguage(getProject().getDefaultLanguage());
                 }
             }
             Validate.isTrue(autLocales.contains(getLanguage()), 
                 Messages.SpecifiedLanguageNotSupported);
         }
     }
 
     /**
      * creates the job passend to command Line client
      * @param configFile File
      * @throws IOException Error
      * @return Jobconfiguration
      */
     public static JobConfiguration initJob(File configFile) throws IOException {
         JobConfiguration job;
         if (configFile != null) {
             // Create JobConfiguration from xml
             BufferedReader in = null;
             StringWriter writer = new StringWriter();
             try {
                 in = new BufferedReader(new FileReader(configFile));
                 String line = null;
                 while ((line = in.readLine()) != null) {
                     writer.write(line);
                 }
             } finally {
                 if (in != null) {
                     in.close();
                 }
             }
             String xml = writer.toString();    
             job = JobConfiguration.readFromXML(xml);
         } else {
             // or create an emty JobConfiguration
             job = new JobConfiguration();
         }
         return job;
     }
     
     /**
      * writes a job configuration to xml file using XStream
      * @param xml String
      * @return JobConfiguration
      * @throws IOException Error
      */
     public static JobConfiguration readFromXML(String xml) 
         throws IOException {
         XStream xstream = new XStream();
         xstream.setClassLoader(JobConfiguration.class.getClassLoader());
         xstream.alias(CONFIGURATION, JobConfiguration.class);
         xstream.registerConverter(new XMLConverter());
         JobConfiguration job;
         try {
             job = (JobConfiguration) xstream.fromXML(xml);
         } catch (Exception e) {
             throw new IOException(); 
         }
         return job;
     }
 
 
     /**
      * parses command line parameter and set them into job object
      * @param cmd CommandLine
      */
     public void parseJobOptions(CommandLine cmd) {
         if (cmd.hasOption(ClientTestStrings.PROJECT)) { 
             setProjectName(cmd.getOptionValue(ClientTestStrings.PROJECT)); 
         }
         if (cmd.hasOption(ClientTestStrings.PROJECT_VERSION)) { 
             // The format must be [majNum].[minNum]
             String [] numbers = 
                 cmd.getOptionValue(ClientTestStrings.PROJECT_VERSION).split("\\."); //$NON-NLS-1$
             if (numbers.length == 2) {
                 try {
                     setProjectMajor(Integer.parseInt(numbers[0]));
                     setProjectMinor(Integer.parseInt(numbers[1])); 
                 } catch (NumberFormatException nfe) {
                     // Do nothing. The version values will not be set and 
                     // this will be noticed during pre-validation.
                 } 
             }
         }
         if (cmd.hasOption(ClientTestStrings.SERVER)) { 
             setServer(cmd.getOptionValue(ClientTestStrings.SERVER)); 
         }
         if (cmd.hasOption(ClientTestStrings.PORT)) { 
             setPort(cmd.getOptionValue(ClientTestStrings.PORT)); 
         }
         parseDBOptions(cmd);
         if (cmd.hasOption(ClientTestStrings.RESULTDIR)) { 
             setResultDir(cmd.getOptionValue(ClientTestStrings.RESULTDIR));
         }
         if (cmd.hasOption(ClientTestStrings.AUT_CONFIG)) { 
             setAutConfigName(cmd.getOptionValue(ClientTestStrings.AUT_CONFIG)); 
         }
         if (cmd.hasOption(ClientTestStrings.AUT_ID)) {
             String autIdString = cmd.getOptionValue(ClientTestStrings.AUT_ID);
             if (autIdString != null) {
                 setAutId(new AutIdentifier(autIdString)); 
             }
         }
         if (cmd.hasOption(ClientTestStrings.DATA_DIR)) { 
             setDataDir(cmd.getOptionValue(ClientTestStrings.DATA_DIR)); 
         }
         if (cmd.hasOption(ClientTestStrings.LANGUAGE)) { 
             setLanguage(LocaleUtil.convertStrToLocale(
                     cmd.getOptionValue(ClientTestStrings.LANGUAGE))); 
         }
         if (cmd.hasOption(ClientTestStrings.TESTSUITE)) { 
             String tsName = cmd.getOptionValue(ClientTestStrings.TESTSUITE); 
             List<String> tsNamesList = new ArrayList<String>();
             tsNamesList.add(tsName);
             setTestSuiteNames(tsNamesList);
         }
         if (cmd.hasOption(ClientTestStrings.TESTJOB)) { 
             setTestJobName(cmd.getOptionValue(ClientTestStrings.TESTJOB));
         }
         if (cmd.hasOption(ClientTestStrings.AUTO_SCREENSHOT)) { 
             setAutoScreenshot(false);
         }
         if (cmd.hasOption(ClientTestStrings.TEST_EXECUTION_RELEVANT)) { 
             setRelevant(false);
         }
         if (cmd.hasOption(ClientTestStrings.TIMEOUT)) {
             try {
                 setTimeout(Integer.parseInt(cmd
                         .getOptionValue(ClientTestStrings.TIMEOUT)));
             } catch (NumberFormatException e) {
                 setTimeout(-1); // will be reported during validate
             }
         }
     }
 
     /**
      * parses the command line parameter when the parameter startserver was set
      * set the parsed parameter into a job object
      * @param cmd CommandLine
      */
     public void parseOptionsWithServer(CommandLine cmd) {
         parseDBOptions(cmd);
         if (cmd.hasOption(ClientTestStrings.STARTSERVER)) {
             setServerPort(cmd.getOptionValue(ClientTestStrings.STARTSERVER));
         }
         if (cmd.hasOption(ClientTestStrings.PROJECT)) { 
             setProjectName(cmd.getOptionValue(ClientTestStrings.PROJECT)); 
         }
         if (cmd.hasOption(ClientTestStrings.PROJECT_VERSION)) { 
             // The format must be [majNum].[minNum]
             String [] numbers = 
                 cmd.getOptionValue(ClientTestStrings.PROJECT_VERSION).split("\\."); //$NON-NLS-1$
             if (numbers.length == 2) {
                 try {
                     setProjectMajor(Integer.parseInt(numbers[0]));
                     setProjectMinor(Integer.parseInt(numbers[1])); 
                 } catch (NumberFormatException nfe) {
                     // Do nothing. The version values will not be set and 
                     // this will be noticed during pre-validation.
                 } 
             }
         }
         if (cmd.hasOption(ClientTestStrings.SERVER)) { 
             setServer(cmd.getOptionValue(ClientTestStrings.SERVER)); 
         }
         if (cmd.hasOption(ClientTestStrings.PORT)) { 
             setPort(cmd.getOptionValue(ClientTestStrings.PORT)); 
         }
         if (cmd.hasOption(ClientTestStrings.AUTO_SCREENSHOT)) { 
             setAutoScreenshot(false);
         }
         if (cmd.hasOption(ClientTestStrings.TEST_EXECUTION_RELEVANT)) { 
             setRelevant(false);
         }
         if (cmd.hasOption(ClientTestStrings.RESULTDIR)) { 
             setResultDir(cmd.getOptionValue(ClientTestStrings.RESULTDIR));
         }
         if (cmd.hasOption(ClientTestStrings.AUT_CONFIG)) { 
             setAutConfigName(cmd.getOptionValue(ClientTestStrings.AUT_CONFIG)); 
         }
         if (cmd.hasOption(ClientTestStrings.AUT_ID)) {
             String autIdString = cmd.getOptionValue(ClientTestStrings.AUT_ID);
             if (autIdString != null) {
                 setAutId(new AutIdentifier(autIdString)); 
             }
         }
         if (cmd.hasOption(ClientTestStrings.DATA_DIR)) { 
             setDataDir(cmd.getOptionValue(ClientTestStrings.DATA_DIR)); 
         }
         if (cmd.hasOption(ClientTestStrings.LANGUAGE)) { 
             setLanguage(LocaleUtil.convertStrToLocale(
                     cmd.getOptionValue(ClientTestStrings.LANGUAGE))); 
         }
 
     }
     
     /**
      * @param cmd CommandLine
      */
     private void parseDBOptions(CommandLine cmd) {
         
         if (cmd.hasOption(ClientTestStrings.DBURL)) { 
             final String dbURL = cmd.getOptionValue(ClientTestStrings.DBURL);
             setDb(dbURL);
             DatabaseConnectionInfo connectionInfo = getConnectionInfo(dbURL);
             
             setDbscheme(connectionInfo);
         }
         if (cmd.hasOption(ClientTestStrings.DB_SCHEME)) {
             setDbConnectionName(
                     cmd.getOptionValue(ClientTestStrings.DB_SCHEME));
         }
         if (cmd.hasOption(ClientTestStrings.DB_USER)) { 
             setDbuser(cmd.getOptionValue(ClientTestStrings.DB_USER)); 
         }
         if (cmd.hasOption(ClientTestStrings.DB_PW)) { 
             setDbpw(cmd.getOptionValue(ClientTestStrings.DB_PW)); 
         }
     }
  
     /**
      * @param dbURL
      *            the dbURL string to get a database connection information for
      * @return The corresponding database connection information or
      *         <code>null</code> if no connection information available for the
      *         given dbURL.
      */
    private DatabaseConnectionInfo getConnectionInfo(final String dbURL) {
         DatabaseConnectionInfo connectionInfo = null;
         if (dbURL.startsWith(OracleConnectionInfo.JDBC_PRE)) {
             connectionInfo = new OracleConnectionInfo() {
                 @Override
                 public String getConnectionUrl() {
                     return dbURL;
                 }
             };
         } else if (dbURL
             .startsWith(PostGreSQLConnectionInfo.JDBC_PRE)) {
             connectionInfo = new PostGreSQLConnectionInfo() {
                 @Override
                 public String getConnectionUrl() {
                     return dbURL;
                 }
             };
         } else if (dbURL
                 .startsWith(MySQLConnectionInfo.JDBC_PRE)) {
             connectionInfo = new MySQLConnectionInfo() {
                 @Override
                 public String getConnectionUrl() {
                     return dbURL;
                 }
             };
         } else if (dbURL
                 .startsWith(H2ConnectionInfo.JDBC_PRE)) {
             connectionInfo = new H2ConnectionInfo() {
                 @Override
                 public String getConnectionUrl() {
                     return dbURL;
                 }
             };
         }
         return connectionInfo;
     }
 
     /**
      * @return List<String>
      */
     public List<String> getTestSuiteNames() {
         return m_testSuiteNames;
     }
 
     /**
      * @return the name of the Test Job to execute, or <code>null</code> if no
      *         Test Job should be executed.
      */
     public String getTestJobName() {
         return m_testJobName;
     }
     
     /**
      * @return the name of the Test suite to execute, or <code>null</code> if no
      *         Test suite should be executed.
      */
     private String getTestSuiteName() {
         return getTestSuiteNames().get(m_actualTestSuite);
     }
 
     /**
      * @param port String
      */
     private void setPort(String port) {
         m_port = port;
     }
 
     /**
      * @param port String
      */
     private void setServerPort(String port) {
         m_serverport = port;
     }
     
     /**
      * @param projectName String
      */
     private void setProjectName(String projectName) {
         m_projectName = projectName;
     }
 
     /**
      * @param server String
      */
     private void setServer(String server) {
         m_server = server;
     }
 
     /**
      * @param testSuiteNames List<String>
      */
     private void setTestSuiteNames(List<String> testSuiteNames) {
         m_testSuiteNames = testSuiteNames;
     }
 
     /**
      * 
      * @param testJobName   The name of the Test Job to execute. 
      */
     private void setTestJobName(String testJobName) {
         m_testJobName = testJobName;
     }
 
     /**
      * 
      * @param testSuiteName   The name of the Test Suite to execute. 
      */
     private void setTestSuiteName(String testSuiteName) {
         List<String> tsNames = new ArrayList<String>(1);
         tsNames.add(testSuiteName);
         setTestSuiteNames(tsNames);
     }
     
     /**
      * @return String
      */
     public String getResultDir() {
         return m_resultDir;
     }
 
     /**
      * @param resultDir String
      */
     private void setResultDir(String resultDir) {
         m_resultDir = resultDir;
     }
 
     /**
      * @return String
      */
     public String getAutConfigName() {
         return m_autConfigName;
     }
 
     /**
      * 
      * @return the ID of the Running AUT to test, or <code>null</code> if no
      *         ID was provided.
      */
     public AutIdentifier getAutId() {
         return m_autId;
     }
 
     /**
      * @param autConfigName String
      */
     public void setAutConfigName(String autConfigName) {
         m_autConfigName = autConfigName;
     }
 
     /**
      * 
      * @param autId The ID of the Running AUT to test.
      */
     public void setAutId(AutIdentifier autId) {
         m_autId = autId;
     }
 
     /**
      * @return IAUTConfigPO
      */
     public IAUTConfigPO getAutConfig() {
         return m_autConfig;
     }
 
     /** 
      * @return List <ITestSuitePO>
      */
     public List<ITestSuitePO> getTestSuites() {
         return Collections.unmodifiableList(m_testSuites);
     }
 
     /** 
      * @return the Test Job to be executed, or <code>null</code> if no Test Job
      *         should be executed.
      */
     public ITestJobPO getTestJob() {
         return m_testJob;
     }
 
     /**
      * @return Locale
      */
     public Locale getLanguage() {
         return m_language;
     }
 
     /**
      * @param language Locale
      */
     private void setLanguage(Locale language) {
         m_language = language;
     }
     
     /**
      * Converter class to marshal/unmarshal job to xml
      * @author BREDEX GmbH
      * @created Apr 11, 2006
      */
     private static final class XMLConverter implements Converter {
 
         /**
          * {@inheritDoc}
          */
         public boolean canConvert(Class arg0) {
             return true;
         }
 
         /**
          * {@inheritDoc}
          */
         public void marshal(Object arg0, HierarchicalStreamWriter arg1, 
             MarshallingContext arg2) {
             
             JobConfiguration job = (JobConfiguration)arg0;
 
             arg1.startNode(ClientTestStrings.PROJECT);
             arg1.setValue(job.getProjectName());
             arg1.endNode();
         
             arg1.startNode(ClientTestStrings.PROJECT_VERSION);
             arg1.setValue(job.getProjectVersion());
             arg1.endNode();
 
             arg1.startNode(ClientTestStrings.SERVER);
             arg1.setValue(job.getServer());
             arg1.endNode();
             
             arg1.startNode(ClientTestStrings.PORT);
             arg1.setValue(job.getPort());
             arg1.endNode();
         
             arg1.startNode(ClientTestStrings.DBURL);
             arg1.setValue(job.getDb());
             arg1.endNode();
         
             arg1.startNode(ClientTestStrings.DB_USER);
             arg1.setValue(job.getDbuser());
             arg1.endNode();
         
             arg1.startNode(ClientTestStrings.DB_PW);
             arg1.setValue(job.getDbpw());
             arg1.endNode();
 
             arg1.startNode(ClientTestStrings.RESULTDIR);
             arg1.setValue(job.getResultDir());
             arg1.endNode();
 
             arg1.startNode(ClientTestStrings.TESTSUITE);
             arg1.setValue(job.getTestSuiteName());
             arg1.endNode();
 
             arg1.startNode(ClientTestStrings.AUT_CONFIG);
             arg1.setValue(job.getAutConfigName());
             arg1.endNode();
 
             arg1.startNode(ClientTestStrings.LANGUAGE);
             arg1.setValue(job.getLanguage().toString());
             arg1.endNode();
 
         }
 
         /**
          * {@inheritDoc}
          * @throws IllegalArgumentException 
          *              if no suitable Database Connection can be found.
          */
         public Object unmarshal(HierarchicalStreamReader arg0, 
                 UnmarshallingContext arg1) throws IllegalArgumentException {
             
             JobConfiguration job = new JobConfiguration();
             while (arg0.hasMoreChildren()) {
                 arg0.moveDown();
                 if (arg0.getNodeName().equals(ClientTestStrings.PROJECT)) {
                     job.setProjectName(arg0.getValue());
                 } else if (arg0.getNodeName().equals(
                         ClientTestStrings.PROJECT_VERSION)) {
                     job.setProjectVersion(arg0.getValue());
                 } else if (arg0.getNodeName().
                         equals(ClientTestStrings.SERVER)) {
                     job.setServer(arg0.getValue());
                 } else if (arg0.getNodeName().
                         equals(ClientTestStrings.PORT)) {
                     job.setPort(arg0.getValue());
                 } else if (arg0.getNodeName().
                         equals(ClientTestStrings.RESULTDIR)) {
                     job.setResultDir(arg0.getValue());
                 } else if (arg0.getNodeName().
                         equals(ClientTestStrings.DBURL)) {
                    job.setDb(arg0.getValue());
                 } else if (arg0.getNodeName().
                         equals(ClientTestStrings.DB_SCHEME)) {
                     job.setDbConnectionName(arg0.getValue());
                 } else if (arg0.getNodeName().
                         equals(ClientTestStrings.DB_USER)) {
                     job.setDbuser(arg0.getValue());
                 } else if (arg0.getNodeName().
                         equals(ClientTestStrings.DB_PW)) {
                     job.setDbpw(arg0.getValue());
                 } else if (arg0.getNodeName().
                         equals(ClientTestStrings.LANGUAGE)) {
                     job.setLanguage(LocaleUtil.
                         convertStrToLocale(arg0.getValue()));
                 } else if (arg0.getNodeName().
                         equals(ClientTestStrings.AUT_CONFIG)) {
                     job.setAutConfigName(arg0.getValue());
                 } else if (arg0.getNodeName().
                         equals(ClientTestStrings.AUT_ID)) {
                     job.setAutId(new AutIdentifier(arg0.getValue()));
                 } else if (arg0.getNodeName().
                         equals(ClientTestStrings.DATA_DIR)) {
                     job.setDataDir(arg0.getValue());
                 } else if (arg0.getNodeName().
                         equals(ClientTestStrings.TESTSUITE)) {
                     job.setTestSuiteName(arg0.getValue());
                 } else if (arg0.getNodeName().
                         equals(ClientTestStrings.TESTJOB)) {
                     job.setTestJobName(arg0.getValue());
                 }
                 arg0.moveUp();
             }
             return job;
         }
     }
 
     /**
      * Sets the project version for this job.
      * @param version Version number in the format 
      *              [majorNumber][<code>VERSION_SEPARATOR</code>][minorNumber].
      */
     private void setProjectVersion(String version) {
         String [] tokens = StringUtils.split(version, VERSION_SEPARATOR);
         if (tokens.length == 2) {
             try {
                 int majorVersion = Integer.parseInt(tokens[0]); 
                 int minorVersion = Integer.parseInt(tokens[1]);
                 setProjectMajor(majorVersion);
                 setProjectMinor(minorVersion);
             } catch (NumberFormatException nfe) {
                 // Do nothing. The version values will not be set and 
                 // this will be noticed during pre-validation.
             }
         }
     }
 
     /**
      * 
      * @return a <code>String</code> representing the project version number
      *         for this job.
      */
     public String getProjectVersion() {
         StringBuilder sb = new StringBuilder();
         sb.append(getProjectMajor()).append(VERSION_SEPARATOR)
             .append(getProjectMinor());
         return sb.toString();
     }
     
     /**
      * @return the projectMinor
      */
     public Integer getProjectMinor() {
         return m_projectMinor;
     }
 
     /**
      * @param projectMinor the projectMinor to set
      */
     public void setProjectMinor(Integer projectMinor) {
         m_projectMinor = projectMinor;
     }
 
     /**
      * @return the projectMajor
      */
     public Integer getProjectMajor() {
         return m_projectMajor;
     }
 
     /**
      * @param projectMajor the projectMajor to set
      */
     public void setProjectMajor(Integer projectMajor) {
         m_projectMajor = projectMajor;
     }
 
     /**
      * @return the dataDir
      */
     public String getDataDir() {
         return m_dataDir;
     }
 
     /**
      * @param dataDir the dataDir to set
      */
     public void setDataDir(String dataDir) {
         m_dataDir = dataDir;
     }
 
     /**
      * @return the timeout
      */
     public int getTimeout() {
         return m_timeout;
     }
 
     /**
      * @param timeout the timeout to set
      */
     public void setTimeout(int timeout) {
         m_timeout = timeout;
     }
 
     /**
      * @param autoScreenshot the autoScreenshot to set
      */
     public void setAutoScreenshot(boolean autoScreenshot) {
         m_autoScreenshot = autoScreenshot;
     }
 
     /**
      * @return the autoScreenshot
      */
     public boolean isAutoScreenshot() {
         return m_autoScreenshot;
     }
 
     /**
      * @param relevant the relevant to set
      */
     public void setRelevant(boolean relevant) {
         m_relevant = relevant;
     }
 
     /**
      * @return the relevant
      */
     public boolean isRelevant() {
         return m_relevant;
     }
     
     /**
      * 
      * @param name The name of the info to find.
      * @return the DatabaseConnectionInfo (from the Preferences) that matches 
      *         the provided name, or <code>null</code> if no such 
      *         DatabaseConnectionInfo can be found.
      */
     private static DatabaseConnectionInfo getConnectionInfoForName(
             String name) {
         
         List<DatabaseConnection> availableConnections = 
             DatabaseConnectionConverter.computeAvailableConnections();
         for (DatabaseConnection conn : availableConnections) {
             if (ObjectUtils.equals(conn.getName(), name)) {
                 return conn.getConnectionInfo();
             }
         }
 
         return null;
     }
 }
