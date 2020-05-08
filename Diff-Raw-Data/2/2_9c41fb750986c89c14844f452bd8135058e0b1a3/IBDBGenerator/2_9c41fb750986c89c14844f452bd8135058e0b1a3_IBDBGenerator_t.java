 
 package org.generationcp.ibpworkbench.database;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.sql.BatchUpdateException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Properties;
 
 import org.generationcp.commons.exceptions.InternationalizableException;
 import org.generationcp.commons.util.ResourceFinder;
 import org.generationcp.ibpworkbench.Message;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  * @author Jeffrey Morales
  */
 public class IBDBGenerator{
 
     private static final Logger LOG = LoggerFactory.getLogger(IBDBGenerator.class);
 	private static final String WORKBENCH_PROP = "workbench.properties";
 	private static final String WORKBENCH_DMS_SQL = "IBDBv1_DMS.sql";
 	private static final String WORKBENCH_GDMS_SQL = "IBDBv1_GDMS.sql";
 	private static final String WORKBENCH_GMS_LOCAL_SQL = "IBDBv1_GMS-LOCAL.sql";
 	private static final String WORKBENCH_IMS_SQL = "IBDBv1_IMS.sql";
 	
 	private static final String WORKBENCH_PROP_HOST = "workbench.host";
 	private static final String WORKBENCH_PROP_PORT = "workbench.port";
 	private static final String WORKBENCH_PROP_USER = "workbench.username";
 	private static final String WORKBENCH_PROP_PASSWORD = "workbench.password";
 	
 	private static final String DB_LOCAL_NAME_SUFFIX = "_local";
 	
 	private static final String SQL_CREATE_DATABASE = "CREATE DATABASE ";
 	private static final String SQL_CHAR_SET = " CHARACTER SET ";
 	private static final String SQL_COLLATE = " COLLATE ";
 	private static final String SQL_GRANT_ALL = "GRANT ALL ON ";
 	private static final String SQL_TO = " TO ";
 	private static final String SQL_IDENTIFIED_BY = " IDENTIFIED BY ";
 	private static final String SQL_FLUSH_PRIVILEGES = "FLUSH PRIVILEGES ";
 	private static final String SQL_LINE_COMMENT = "--";
 	private static final String SQL_BEGIN_COMMENT = "/*";
 	private static final String SQL_END_COMMENT = "*/";
 	private static final String SQL_SINGLE_QUOTE = "'";
 	private static final String SQL_AT_SIGN = "@";
 	private static final String SQL_PERIOD = ".";
 	private static final String SQL_END = ";";
 	
 	private static final String DEFAULT_LOCAL_USER = "local";
	private static final String DEFAULT_LOCAL_HOST = "localhost";
 	private static final String DEFAULT_LOCAL_PASSWORD = "local";
 	private static final String DEFAULT_ALL = "*";
 	private static final String DEFAULT_CHAR_SET = "utf8";
 	private static final String DEFAULT_COLLATE = "utf8_general_ci";
 
     private String workbenchHost;
     private String workbenchPort;
     private String workbenchUsername;
     private String workbenchPassword;
     private String workbenchURL;
 
     private String crop;
     private Long projectId;
 
     private Connection connection = null;
 
     public IBDBGenerator(String crop, Long projectId) {
 
         this.crop = crop;
         this.projectId = projectId;
 
     }
 
     public boolean generateDatabase() throws InternationalizableException {
         
         boolean isGenerationSuccess = false;
         
         try {
             createLocalConnection();
             createLocalDatabase();
             createManagementSystems();
             closeConnection();
             
             isGenerationSuccess = true;
         } catch (InternationalizableException e) {
             isGenerationSuccess = false;            
             throw e;
         } 
 
         return isGenerationSuccess;
     }
 
     private void createLocalConnection() throws InternationalizableException {
 
         if (this.connection == null) {
 
             Properties prop = new Properties();
 
             try {
                 InputStream in = null;
 
                 try {
                     in = new FileInputStream(new File(ResourceFinder.locateFile(WORKBENCH_PROP).toURI()));
                 } catch (IllegalArgumentException ex) {
                     in = Thread.currentThread().getContextClassLoader().getResourceAsStream(WORKBENCH_PROP);
                 }
                 prop.load(in);
 
                 workbenchHost = prop.getProperty(WORKBENCH_PROP_HOST);
                 workbenchPort = prop.getProperty(WORKBENCH_PROP_PORT);
                 workbenchUsername = prop.getProperty(WORKBENCH_PROP_USER);
                 workbenchPassword = prop.getProperty(WORKBENCH_PROP_PASSWORD);
                 workbenchURL = "jdbc:mysql://" + workbenchHost + ":" + workbenchPort;
 
                 in.close();
 
             } catch (URISyntaxException e) {
                 handleConfigurationError(e);
             } catch (IOException e) {
                 handleConfigurationError(e);
             }
 
             try {
                 connection = DriverManager.getConnection(workbenchURL, workbenchUsername, workbenchPassword);
             } catch (SQLException e) {
                 handleDatabaseError(e);
             }
 
         }
 
     }
 
     private void createLocalDatabase() throws InternationalizableException {
 
         StringBuffer databaseName = new StringBuffer();
         StringBuffer createDatabaseSyntax = new StringBuffer();
     	StringBuffer createGrantSyntax = new StringBuffer();
     	StringBuffer createFlushSyntax = new StringBuffer();
 
         databaseName.append(crop).append("_").append(projectId).append(DB_LOCAL_NAME_SUFFIX);
 
         Statement statement = null;
 
         try {
 
 	    	statement = connection.createStatement();
 	    	
 	    	createDatabaseSyntax.append(SQL_CREATE_DATABASE).append(databaseName).append(SQL_CHAR_SET).append(DEFAULT_CHAR_SET).append(SQL_COLLATE).append(DEFAULT_COLLATE);
 	    	
 	    	statement.addBatch(createDatabaseSyntax.toString());
 	    	
 	    	createGrantSyntax.append(SQL_GRANT_ALL).append(databaseName).append(SQL_PERIOD).append(DEFAULT_ALL).append(SQL_TO)
 	    		.append(SQL_SINGLE_QUOTE).append(DEFAULT_LOCAL_USER).append(SQL_SINGLE_QUOTE).append(SQL_AT_SIGN).append(SQL_SINGLE_QUOTE).append(DEFAULT_LOCAL_HOST)
 	    		.append(SQL_SINGLE_QUOTE).append(SQL_IDENTIFIED_BY).append(SQL_SINGLE_QUOTE).append(DEFAULT_LOCAL_PASSWORD).append(SQL_SINGLE_QUOTE);
 	    	
 	    	statement.addBatch(createGrantSyntax.toString());
 	    	
 	    	createFlushSyntax.append(SQL_FLUSH_PRIVILEGES);
 	    	
 	    	statement.addBatch(createFlushSyntax.toString());
 	    	
 	    	statement.executeBatch();
 	    	
 	    	connection.setCatalog(databaseName.toString());
 
         } catch (SQLException e) {
             handleDatabaseError(e);
         } finally {
             if (statement != null) {
                 try {
                     statement.close();
                 } catch (SQLException e) {
                     handleDatabaseError(e);
                 }
             }
         }
     }
 
     private void createManagementSystems() throws InternationalizableException {
 
         try {
 
             createTables(new File(ResourceFinder.locateFile(WORKBENCH_DMS_SQL).toURI()));
             createTables(new File(ResourceFinder.locateFile(WORKBENCH_GDMS_SQL).toURI()));
             createTables(new File(ResourceFinder.locateFile(WORKBENCH_GMS_LOCAL_SQL).toURI()));
             createTables(new File(ResourceFinder.locateFile(WORKBENCH_IMS_SQL).toURI()));
 
             LOG.info("IB Local Database Generation Successful");
 
         } catch (FileNotFoundException e) {
             handleConfigurationError(e);
         } catch (URISyntaxException e) {
             handleConfigurationError(e);
         }
 
     }
 
     private void createTables(File sqlFile) throws InternationalizableException {
 
         /*
          * if (!sqlFile.toString().endsWith(".sql")) { throw new
          * IllegalArgumentException("Wrong file type."); }
          */
 
         StringBuffer batch = null;
         Statement statement = null;
 
         boolean isEndCommentFound = true;
 
         try {
 
             statement = this.connection.createStatement();
 
         } catch (SQLException sqle) {
             handleDatabaseError(sqle);
         }
 
         BufferedReader in = null;
 
         try {
 
             try {
 
                 in = new BufferedReader(new FileReader(sqlFile));
 
             } catch (IllegalArgumentException ex) {
                 handleConfigurationError(ex);
             }
 
             String inputLine;
 
             while ((inputLine = in.readLine()) != null) {
 
                 if (inputLine.startsWith(SQL_LINE_COMMENT)) {
 
                     continue;
 
                 } else if (inputLine.startsWith(SQL_BEGIN_COMMENT) && inputLine.endsWith(SQL_END_COMMENT)) {
 
                     isEndCommentFound = true;
 
                     continue;
 
                 } else if (inputLine.startsWith(SQL_BEGIN_COMMENT)) {
 
                     isEndCommentFound = false;
 
                     continue;
 
                 } else if (inputLine.endsWith(SQL_END_COMMENT)) {
 
                     isEndCommentFound = true;
 
                     continue;
 
                 } else if (isEndCommentFound) {
 
                     if (inputLine.contains(SQL_LINE_COMMENT)) {
 
                         inputLine = inputLine.substring(0, inputLine.indexOf(SQL_LINE_COMMENT));
 
                     }
 
                     if (inputLine.contains(SQL_BEGIN_COMMENT) && inputLine.contains(SQL_END_COMMENT)) {
 
                         inputLine = inputLine.substring(0, inputLine.indexOf(SQL_BEGIN_COMMENT));
 
                         isEndCommentFound = true;
 
                     }
 
                     if (batch != null) {
 
                         batch.append(inputLine);
 
                         if (batch.toString().contains(SQL_END)) {
 
                             statement.addBatch(batch.toString().replace(SQL_END, ""));
 
                             batch = null;
 
                         }
 
                     } else {
 
                         batch = new StringBuffer(inputLine);
 
                         if (batch.toString().contains(SQL_END)) {
 
                             statement.addBatch(batch.toString().replace(SQL_END, ""));
 
                             batch = null;
 
                         }
 
                     }
 
                 }
 
             }
 
             in.close();
 
             statement.executeBatch();
 
             LOG.info("Tables in " + sqlFile.getName() + " Generated.");
 
         } catch (IOException e) {
             handleConfigurationError(e);
         } catch (BatchUpdateException e) {
             handleDatabaseError(e);
         } catch (SQLException e) {
             handleDatabaseError(e);
         }  finally {
             if (statement != null) {
                 try {
                     statement.close();
                 } catch (SQLException e) {
                     handleDatabaseError(e);
                 }
             }
         }
     }
 
     private void closeConnection() throws InternationalizableException {
 
         if (this.connection != null) {
 
             try {
 
                 connection.close();
 
                 connection = null;
 
             } catch (SQLException e) {
                 handleDatabaseError(e);
             }
 
         }
 
     }
 
     @Override
     protected void finalize() throws Throwable {
         super.finalize();
         closeConnection();
     }
     
     private void handleDatabaseError(Exception e) throws InternationalizableException {
         LOG.error(e.toString(), e);
         throw new InternationalizableException(e, 
                 Message.DATABASE_ERROR, Message.CONTACT_ADMIN_ERROR_DESC);
     }
     
     private void handleConfigurationError(Exception e) throws InternationalizableException {
         LOG.error(e.toString(), e);
         throw new InternationalizableException(e, 
                 Message.CONFIG_ERROR, Message.CONTACT_ADMIN_ERROR_DESC);
     }
 }
