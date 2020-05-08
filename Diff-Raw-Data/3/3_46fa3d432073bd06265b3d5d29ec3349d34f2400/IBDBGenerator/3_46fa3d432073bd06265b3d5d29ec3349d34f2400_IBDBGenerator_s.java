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
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.generationcp.commons.exceptions.InternationalizableException;
 import org.generationcp.commons.util.ResourceFinder;
 import org.generationcp.ibpworkbench.Message;
 import org.generationcp.ibpworkbench.model.BreedingMethodModel;
 import org.generationcp.ibpworkbench.model.LocationModel;
 import org.generationcp.middleware.pojos.workbench.CropType;
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
     
     private static final String DEFAULT_INSERT_LOCATIONS = "INSERT location VALUES(?,?,?,?,?,?,?,?,?,?,?)";
     private static final String DEFAULT_INSERT_BREEDING_METHODS = "INSERT methods VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
     private static final String DEFAULT_INSERT_INSTALLATION = "INSERT instln VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
 
     private String workbenchHost;
     private String workbenchPort;
     private String workbenchUsername;
     private String workbenchPassword;
     private String workbenchURL;
     
     private String generatedDatabaseName;
 
     private CropType cropType;
     private Long projectId;
 
     private Connection connection = null;
 
     public IBDBGenerator(CropType cropType, Long projectId) {
         this.cropType = cropType;
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
 
         databaseName.append(cropType.getCropName().toLowerCase()).append("_").append(projectId).append(DB_LOCAL_NAME_SUFFIX);
 
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
             
             generatedDatabaseName = databaseName.toString();
             
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
 
             executeSQLFile(new File(ResourceFinder.locateFile(WORKBENCH_DMS_SQL).toURI()));
             executeSQLFile(new File(ResourceFinder.locateFile(WORKBENCH_GDMS_SQL).toURI()));
             executeSQLFile(new File(ResourceFinder.locateFile(WORKBENCH_GMS_LOCAL_SQL).toURI()));
             executeSQLFile(new File(ResourceFinder.locateFile(WORKBENCH_IMS_SQL).toURI()));
             
             LOG.info("IB Local Database Generation Successful");
 
         } catch (FileNotFoundException e) {
             handleConfigurationError(e);
         } catch (URISyntaxException e) {
             handleConfigurationError(e);
         }
 
     }
 
     private void executeSQLFile(File sqlFile) throws InternationalizableException {
 
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
     
     public boolean addCachedLocations(Map<Integer, LocationModel> cachedLocations) {
     	
     	boolean areLocationsAdded = false;
     	
     	try {
     		
 		    connection = DriverManager.getConnection(workbenchURL, workbenchUsername, workbenchPassword);
     	
 		    connection.setCatalog(generatedDatabaseName);
     	
     	    Set<Integer> keySet = cachedLocations.keySet();
     	
     	    Iterator<Integer> keyIter = keySet.iterator();
     	
     	    LocationModel location;
     	
     	    PreparedStatement preparedStatement = null;
     	
     	    while(keyIter.hasNext()) {
     		
     	    	location = cachedLocations.get(keyIter.next());
     		
     		    preparedStatement = connection.prepareStatement(DEFAULT_INSERT_LOCATIONS);
     		    
     		    preparedStatement.setInt(1, location.getLocationId());
     		    preparedStatement.setInt(2, 0);
     		    preparedStatement.setInt(3, 0);
     		    preparedStatement.setString(4, location.getLocationName());
     		    preparedStatement.setString(5, location.getLocationAbbreviation());
     		    preparedStatement.setInt(6, 0);
     		    preparedStatement.setInt(7, 0);
     		    preparedStatement.setInt(8, 0);
     		    preparedStatement.setInt(9, 0);
     		    preparedStatement.setInt(10, 0);
     		    preparedStatement.setInt(11, 0);
     		    
     		    preparedStatement.executeUpdate();
     		    
     		    preparedStatement = null;
     		
     	    }
     	    
     	    areLocationsAdded = true;
     	    
     	    closeConnection();
 
 		} catch (SQLException e) {
 		    handleDatabaseError(e);
 		}
     	
     	return areLocationsAdded;
     	
     }
     
     public boolean addCachedBreedingMethods(Map<Integer, BreedingMethodModel> cachedBreedingMethods) {
         
         boolean areBreedingMethodsAdded = false;
         
         try {
             
             connection = DriverManager.getConnection(workbenchURL, workbenchUsername, workbenchPassword);
         
             connection.setCatalog(generatedDatabaseName);
         
             Set<Integer> keySet = cachedBreedingMethods.keySet();
         
             Iterator<Integer> keyIter = keySet.iterator();
         
             BreedingMethodModel breedingMethod;
         
             PreparedStatement preparedStatement = null;
         
             while(keyIter.hasNext()) {
             
                 breedingMethod = cachedBreedingMethods.get(keyIter.next());
             
                 preparedStatement = connection.prepareStatement(DEFAULT_INSERT_BREEDING_METHODS);
                 
    /*             mid int
                 mtype combo string
                 mgrp string 3  -
                 mcode string 8
                 mname string 50
                 mdesc string 255
                 mref int 0
                 mprgn int 0
                 mfprg int 0
                 mattr int 0
                 geneq int 0
                 muid int 0
                 lmid int 0
                 mdate int*/
                 
                 preparedStatement.setInt(1, breedingMethod.getMethodId());
                 preparedStatement.setString(2, breedingMethod.getMethodType());
                 preparedStatement.setString(3, breedingMethod.getMethodGroup());
                 preparedStatement.setString(4, breedingMethod.getMethodCode());
                 preparedStatement.setString(5, breedingMethod.getMethodName());
                 preparedStatement.setString(6, breedingMethod.getMethodDescription());
                 preparedStatement.setInt(7, 0);
                 preparedStatement.setInt(8, 0);
                 preparedStatement.setInt(9, 0);
                 preparedStatement.setInt(10, 0);
                 preparedStatement.setInt(11, 0);
                 preparedStatement.setInt(12, 0);
                 preparedStatement.setInt(13, 0);
                 
                 Calendar currentDate = Calendar.getInstance();
                 SimpleDateFormat formatter= 
                 new SimpleDateFormat("yyyy/MMM/dd");
                 String dateNow = formatter.format(currentDate.getTime());
                 System.out.println(dateNow);
                 
                 preparedStatement.setInt(14, 0);
                 
                 preparedStatement.executeUpdate();
                 
                 preparedStatement = null;
             
             }
             
             areBreedingMethodsAdded = true;
             
             closeConnection();
 
         } catch (SQLException e) {
             handleDatabaseError(e);
         }
         
         return areBreedingMethodsAdded;
         
     }
     
     public boolean addLocalInstallationRecord(String projectName) {
         
         boolean isInstallationInserted = false;
 
         PreparedStatement preparedStatement = null;
         
         try {
             
             connection = DriverManager.getConnection(workbenchURL, workbenchUsername, workbenchPassword);
         
             connection.setCatalog(generatedDatabaseName);
             
             preparedStatement = connection.prepareStatement(DEFAULT_INSERT_INSTALLATION);
             
             DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
             Date date = new Date();
             dateFormat.format(date);
             
             preparedStatement.setInt(1, -1);
             preparedStatement.setInt(2, -1);
             preparedStatement.setInt(3, Integer.parseInt(dateFormat.format(date)));
             preparedStatement.setInt(4, 0);
             preparedStatement.setInt(5, 0);
             preparedStatement.setInt(6, 0);
             preparedStatement.setInt(7, 0);
             preparedStatement.setInt(8, 0);
             preparedStatement.setInt(9, 0);
             preparedStatement.setInt(10, 0);
             preparedStatement.setInt(11, 0);
             preparedStatement.setInt(12, 0);
             preparedStatement.setInt(13, 0);
             preparedStatement.setString(14, projectName);
             preparedStatement.setInt(15, 0);
             preparedStatement.setInt(16, 0);
             preparedStatement.setInt(17, 0);
             
             preparedStatement.executeUpdate();
             
             preparedStatement = null;
 
             isInstallationInserted = true;
             
             closeConnection();
 
         } catch (SQLException e) {
             handleDatabaseError(e);
         }
         
         return isInstallationInserted;
         
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
