 package org.otherobjects.cms.bootstrap;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.commons.lang.StringUtils;
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.config.OtherObjectsConfigurator;
 import org.otherobjects.cms.model.User;
 import org.otherobjects.cms.model.UserDao;
 import org.otherobjects.cms.util.ResourceScanner;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.security.Authentication;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
 
 /**
  * Ensures that the OTHERobjects datastores are correctly initialised
  * and populated.
  * 
  * FIXME Needs support for multiple schema files
  * 
  * @author rich
  */
 public class OoBootstrapper implements InitializingBean
 {
     private OtherObjectsConfigurator otherObjectsConfigurator;
     private DbSchemaInitialiser dbSchemaInitialiser;
     private JackrabbitInitialiser jackrabbitInitialiser;
     private OtherObjectsAdminUserCreator otherObjectsAdminUserCreator;
     private JackrabbitPopulater jackrabbitPopulater;
     private ResourceScanner resourceScanner;
     private UserDao userDao;
 
     private static final String BOOTSTRAP_PROPERTIES_FILENAME = "/bootstrap.properties";
     private static final String DB_SCHEMA_VERSION_KEY = "db.schema.version";
     private static final String JCR_SCHEMA_VERSION_KEY = "jcr.schema.version";
 
     private Properties boostrapProperties;
 
     public void setUserDao(UserDao userDao)
     {
         this.userDao = userDao;
     }
 
     public void afterPropertiesSet() throws Exception
     {
         bootstrap();
     }
 
     public void bootstrap() throws Exception
     {
         // Load bootstrap properties containing schema versions
         loadProperties();
 
         //initialise jcr repository
         jackrabbitInitialiser.initialise();
 
         // initialise db schema
         if (schemaUpdateRequired())
         {
             dbSchemaInitialiser.initialise(false);
             boostrapProperties.setProperty(DB_SCHEMA_VERSION_KEY, "1");
         }
         else
         {
             // FIXME We should not do this on every startup
             dbSchemaInitialiser.update();
         }
 
         try
         {
             // create admin user if not yet existing
             User adminUser = getAdminUser();
             if (adminUser == null)
                 adminUser = otherObjectsAdminUserCreator.createAdminUser();
             
             else if (StringUtils.isEmpty(adminUser.getEmail()))
             {
                 // If email is empty then the admin user has not been configured,
                 // so we reset the password for safety.
                 otherObjectsAdminUserCreator.resetPassword(adminUser);
             }
             // Authenticate as new Admin user
             Authentication authentication = new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities());
             SecurityContextHolder.getContext().setAuthentication(authentication);
 
             // populate repository with default infrastructure (folders, welcome page etc.)
             // FIXME Always run so that Types are created
             if (true || repositoryPopulationRequired())
             {
                 jackrabbitPopulater.populateRepository();
                 boostrapProperties.setProperty(JCR_SCHEMA_VERSION_KEY, "1");
 
                 if (repositoryPopulationRequired())
                 {
                 // Scan for resources in file system
                //resourceScanner.updateResources();
                 }
             }
         }
         finally
         {
             SecurityContextHolder.clearContext();
         }
 
         // Save properties
         storeProperties();
 
         // Copy properties to main configurator
         otherObjectsConfigurator.setProperty(DB_SCHEMA_VERSION_KEY, boostrapProperties.getProperty(DB_SCHEMA_VERSION_KEY));
         otherObjectsConfigurator.setProperty(JCR_SCHEMA_VERSION_KEY, boostrapProperties.getProperty(JCR_SCHEMA_VERSION_KEY));
     }
 
     private void loadProperties() throws IOException
     {
         boostrapProperties = new Properties();
         FileInputStream fis = null;
         try
         {
             String privateDataPath = otherObjectsConfigurator.getProperty("site.private.data.path");
             File f = new File(privateDataPath + BOOTSTRAP_PROPERTIES_FILENAME);
             fis = new FileInputStream(f);
             boostrapProperties.load(fis);
         }
         catch (FileNotFoundException e)
         {
             // No properties so this is first startup
         }
         catch (IOException e)
         {
             throw new OtherObjectsException("Error reading bootstrap properties file.", e);
         }
         finally
         {
             if (fis != null)
                 fis.close();
         }
 
     }
 
     private void storeProperties() throws IOException
     {
         FileOutputStream fis = null;
         try
         {
             String privateDataPath = otherObjectsConfigurator.getProperty("site.private.data.path");
             File f = new File(privateDataPath + BOOTSTRAP_PROPERTIES_FILENAME);
             fis = new FileOutputStream(f);
             boostrapProperties.store(fis, "OO Bootstrap Properties");
         }
         catch (FileNotFoundException e)
         {
             // No properties so this is first startup
         }
         catch (IOException e)
         {
             throw new OtherObjectsException("Error writing bootstrap properties file.", e);
         }
         finally
         {
             if (fis != null)
                 fis.close();
         }
     }
 
     private boolean repositoryPopulationRequired()
     {
         String version = boostrapProperties.getProperty(JCR_SCHEMA_VERSION_KEY);
         if (version == null)
             return true;
         else
             return false;
     }
 
     private boolean schemaUpdateRequired()
     {
         String version = boostrapProperties.getProperty(DB_SCHEMA_VERSION_KEY);
         if (version == null)
             return true;
         else
             return false;
     }
 
     private User getAdminUser()
     {
         return (User) userDao.loadUserByUsername(OtherObjectsAdminUserCreator.DEFAULT_ADMIN_USER_NAME);
     }
 
     public void setDbSchemaInitialiser(DbSchemaInitialiser dbSchemaInitialiser)
     {
         this.dbSchemaInitialiser = dbSchemaInitialiser;
     }
 
     public void setJackrabbitInitialiser(JackrabbitInitialiser jackrabbitInitialiser)
     {
         this.jackrabbitInitialiser = jackrabbitInitialiser;
     }
 
     public void setOtherObjectsAdminUserCreator(OtherObjectsAdminUserCreator otherObjectsAdminUserCreator)
     {
         this.otherObjectsAdminUserCreator = otherObjectsAdminUserCreator;
     }
 
     public void setJackrabbitPopulater(JackrabbitPopulater jackrabbitPopulater)
     {
         this.jackrabbitPopulater = jackrabbitPopulater;
     }
 
     public void setOtherObjectsConfigurator(OtherObjectsConfigurator otherObjectsConfigurator)
     {
         this.otherObjectsConfigurator = otherObjectsConfigurator;
     }
 
     public void setResourceScanner(ResourceScanner resourceScanner)
     {
         this.resourceScanner = resourceScanner;
     }
 }
