 /**
  * 
  */
 package com.arguments;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import com.arguments.functional.datamodel.ArgumentsException;
 
 /**
  * @author mirleau
  * 
  */
 public class Deployment
 {
     private static Deployment theInstance = new Deployment();
 
     public final String dbUrl;
     public final String dbName;
     public final String dbDriver;
     public final String dbUserName;
     public final String dbPassword;
     public final String foreignTestId = "0";
     public final String foreignAdminId = "10001";
     
     public final String webappPath;
     public final String webinfPath;
 
     public static Deployment i()
     {
         return theInstance;
     }
 
     public Deployment()
     {
         final URL myClassRoot = Deployment.class.getResource("/");
         final String myClassRootPath = myClassRoot.getFile();
         System.out.println("Class root path: " + myClassRootPath);
         // From eclipse: /mnt/bigspace/opt/linux/i386/liferay/liferay-sdk/portlets/argumentation-portlet/docroot/WEB-INF/classes/
         // From tomcat: /mnt/bigspace/opt/linux/i386/liferay/liferay-portal-6.1.0-ce-ga1.2/tomcat-7.0.23/webapps/argumentation-portlet/WEB-INF/classes/
 
         //String myWebappToClassRootPath = "argumentation-portlet/docroot/WEB-INF/classes/";
         //assertTrue(myClassRootPath.endsWith(myWebappToClassRootPath));
 
         File myWebinfFile = new File(myClassRootPath).getParentFile();
 
         File myWebappFile = new File(myClassRootPath).
                 getParentFile().getParentFile().getParentFile().getParentFile();
         
         
         webappPath = myWebappFile.getAbsolutePath()+"/"; 
         System.out.println("Webapp path: " + webappPath);
         webinfPath = myWebinfFile.getAbsolutePath()+"/";
         
         ArrayList<String> myPropertyLocations = new ArrayList<String>()
                 {{
                     add(webappPath + "webapps-conf/arguments.deployment.properties");
                     add(myClassRootPath + "com/arguments/testdeployment.properties");
                 }};
         
         Properties myDeploymentProperties = new Properties();
 
         try
         {
             
             InputStream myPropertiesStream = null;
             String myLocation = null;
             while(myPropertiesStream == null && !myPropertyLocations.isEmpty())
             {
                 myLocation = myPropertyLocations.remove(0);
                 File myPropertiesFile = new File(myLocation);
                 if (!myPropertiesFile.exists())
                 {
                     System.out.println("Didn't find arguments properties at " + myLocation);
                 }
                 else
                 {
                     myPropertiesStream = new FileInputStream(myPropertiesFile);
                 }
             }
 
            assertNotNull(myPropertiesStream);
             
             System.out.println("Found aruments properties at " + myLocation);
             
             myDeploymentProperties.load(myPropertiesStream);
         } catch (IOException anException)
         {
             throw new ArgumentsException(anException);
         }
 
         dbUrl = myDeploymentProperties.getProperty("dbUrl");
         dbName = myDeploymentProperties.getProperty("dbName");
         dbDriver = myDeploymentProperties.getProperty("dbDriver");
         dbUserName = myDeploymentProperties.getProperty("dbUserName");
         dbPassword = myDeploymentProperties.getProperty("dbPassword");
         assertNotNull(dbUrl);
     }
 
     static String getClassPath()
     {
         String myClassPath =  System.getProperty("java.class.path");
         return myClassPath.replace(":", "\n");
     }
 
 }
