 package org.generationcp.ibpworkbench.datasource.helper;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.util.Properties;
 
 import org.generationcp.middleware.exceptions.ConfigException;
 import org.generationcp.middleware.manager.DatabaseConnectionParameters;
 import org.generationcp.middleware.manager.ManagerFactory;
 import org.generationcp.middleware.util.ResourceFinder;
 import org.hibernate.HibernateException;
 
 public class DatasourceConfig {
     private String host;
     private String port;
     private String dbname;
     private String username;
     private String password;
     private ManagerFactory managerFactory;
 
     public DatasourceConfig() {
 
         Properties prop = new Properties();
 
         try {
             InputStream in = null;
 
             try {
                 in = new FileInputStream(new File(ResourceFinder.locateFile("IBPDatasource.properties").toURI()));
             }
             catch (IllegalArgumentException ex) {
                 in = getClass().getClassLoader().getResourceAsStream("IBPDatasource.properties");
             }
             prop.load(in);
 
             host = prop.getProperty("ibpmiddleware.host");
             System.out.println(host);
             dbname = prop.getProperty("ibpmiddleware.dbname");
             System.out.println(dbname);
             port = prop.getProperty("ibpmiddleware.port");
             System.out.println(port);
             username = prop.getProperty("ibpmiddleware.username");
             System.out.println(username);
             password = prop.getProperty("ibpmiddleware.password");
             System.out.println(password);
             in.close();
 
             DatabaseConnectionParameters param = new DatabaseConnectionParameters(host, port, dbname, username, password);
            //the database connection parameters for central is set to null because the Workbench
            //does not need a connection to a central instance of IBDB
            //the Workbench currently uses the WorkbenchDataManager only which needs just a 
            //connection to a local instance
            managerFactory = new ManagerFactory(param, null);
         }
         catch (URISyntaxException e) {
             throw new RuntimeException("Cannot create data source", e);
         }
         catch (HibernateException e) {
             throw new RuntimeException("Cannot create data source", e);
         }
         catch (ConfigException e) {
             throw new RuntimeException("Cannot create data source", e);
         }
         catch (IOException e) {
             throw new RuntimeException("Cannot create data source", e);
         }
 
     }
 
     public ManagerFactory getManagerFactory() {
         return this.managerFactory;
     }
 
 }
