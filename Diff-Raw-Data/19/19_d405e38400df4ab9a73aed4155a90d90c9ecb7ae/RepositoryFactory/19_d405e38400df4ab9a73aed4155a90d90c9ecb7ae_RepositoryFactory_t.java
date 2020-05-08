 package org.wyona.yarep.core;
 
 import java.io.File;
 import java.net.URL;
 import java.net.URI;
 import java.net.URLDecoder;
 import java.util.Properties;
 import java.util.Vector;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 import org.apache.log4j.Category;
 
 import org.wyona.commons.io.FileUtil;
 
 /**
  *
  */
 public class RepositoryFactory {
 
     private static Category log = Category.getInstance(RepositoryFactory.class);
 
     public static final String DEFAULT_CONFIGURATION_FILE = "yarep.properties";
     public static String CONFIGURATION_FILE = DEFAULT_CONFIGURATION_FILE;
 
     private Vector repositories;
 
     private URL propertiesURL;
 
     /**
      *
      */
     public RepositoryFactory() throws RepositoryException {
         this(DEFAULT_CONFIGURATION_FILE);
     }
 
     /**
      * TODO: Make CONFIGURATION_FILE loadable from absolute path
      */
     public RepositoryFactory(String configurationFile) throws RepositoryException {
         CONFIGURATION_FILE = configurationFile;
 
         propertiesURL = RepositoryFactory.class.getClassLoader().getResource(CONFIGURATION_FILE);
         if (propertiesURL == null) {
             log.warn("No such resource: " + CONFIGURATION_FILE);
             repositories = new Vector(0);
             return;
         }
 
         Properties props = new Properties();
         log.debug("Properties URL: " + propertiesURL);
         // use URLDecoder to avoid problems when the filename contains spaces
         File propsFile = new File(URLDecoder.decode(propertiesURL.getFile()));
 
         try {
             props.load(propertiesURL.openStream());
 
             String separator = ",";
             String[] tokens = props.getProperty("configurations").split(separator);
             if (tokens.length % 2 != 0) {
                 // NOTE: An exception is being trown and hence the repo factory instance will be null anyway
                 //repositories = new Vector(0);
                 throw new Exception("Wrong number of config parameters: " + CONFIGURATION_FILE);
             }
 
            repositories = new Vector();
             for (int i = 0;i < tokens.length / 2; i++) {
                 String repoID = tokens[2 * i];
                 String configFilename = tokens[2 * i + 1];

                 log.debug("Property File: " + propsFile.getAbsolutePath());
                 log.debug("PARENT: " + propsFile.getParentFile());
                 log.debug("Filename: " + configFilename);

                 File configFile;
                 if (new File(configFilename).isAbsolute()) {
                     configFile = new File(configFilename);
                 } else {
                     configFile = FileUtil.file(propsFile.getParentFile().getAbsolutePath(), new File(configFilename).toString());
                 }
                 if (log.isDebugEnabled()) log.debug("Configuration File: " + configFile.getAbsolutePath());
                 Repository rt = newRepository(repoID, configFile);
             }
 
             // see src/java/org/wyona/meguni/parser/Parser.java
         } catch (Exception e) {
             log.error(e.toString(), e);
             throw new RepositoryException("Could not create RepositoryFactory with file " 
                     + configurationFile + ": " + e.getMessage(), e);
         }
     }
 
     /**
      * Resets the repository factory and removes all registered repositories.
      */
     public void reset() {
         this.repositories = new Vector();
     }
 
     /**
      * Get properties URL
      */
     public URL getPropertiesURL() {
         return propertiesURL;
     }
 
     /**
      * Get repository IDs
      */
     public String[] getRepositoryIDs() {
         String[] ids = new String[repositories.size()];
         for (int i = 0;i < repositories.size(); i++) {
             ids[i] = ((Repository) repositories.elementAt(i)).getID();
         }
         return ids;
     }
 
     /**
      * List all registered repositories
      */
     public String toString() {
        String s = "\n\nShow all (" + repositories.size() + ") repositories listed within " + CONFIGURATION_FILE + " respectively set during runtime:";
         for (int i = 0;i < repositories.size(); i++) {
            Repository repo = (Repository) repositories.elementAt(i);
            s = s + "\nRepository (id=" + repo.getID() + "): " + (Repository) repositories.elementAt(i);
         }
         return s;
     }
 
     /**
      * Get repository from yarep.properties
      *
      * @param rid Repository ID
      */
     public Repository newRepository(String rid) throws RepositoryException {
         for (int i = 0;i < repositories.size(); i++) {
             if (((Repository) repositories.elementAt(i)).getID().equals(rid)) return (Repository) repositories.elementAt(i);
         }
         log.warn("No such repository: " + rid + " (" + getPropertiesURL() + ")");
         if (repositories.size() == 0) {
             log.error("No repositories (" + getPropertiesURL() + ")! Maybe properties file is misconfigured!");
         }
         return null;
     }
 
     /**
      * Get first repository from yarep.properties
      *
      */
     public Repository firstRepository() throws RepositoryException {
         if (repositories.size() > 0) return (Repository) repositories.elementAt(0);
         log.error("No repositories (" + getPropertiesURL() + ")! Maybe properties file is misconfigured!");
         return null;
     }
 
     /**
      * Get repository from specified config, whereas config is being resolved relative to classpath
      */
     public Repository newRepository(String rid, File configFile) throws RepositoryException {
         if (exists(rid)) {
             log.warn("Repository ID already exists: " + rid + " Repository will not be added to list of Repository Factory!");
             return null;
         }
 
         try {
             if (!configFile.isAbsolute()) {
                 URL configURL = RepositoryFactory.class.getClassLoader().getResource(configFile.toString());
                 configFile = new File(configURL.getFile());
             }
             log.debug("Config file: " + configFile);
             
             DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
             Configuration config;
 
             config = builder.buildFromFile(configFile);
 
             String className = config.getAttribute("class", null);
             Repository repository;
             if (className != null) {
                 log.debug("create repository instance: " + className);
                 Class repoClass = Class.forName(className);
                 repository = (Repository) repoClass.newInstance();
             } else {
                 repository = (Repository) Class.forName("org.wyona.yarep.impl.DefaultRepository").newInstance();
             }
             repository.setID(rid);
             repository.readConfiguration(configFile);
             
             repositories.addElement(repository);
             return repository;
         } catch (Exception e) {
             log.error(e);
             throw new RepositoryException("Could not create repository: " + rid + " " 
                     + configFile + " " + e.getMessage(), e);
         }
     }
 
     /**
      * Check if repository exists
      *
      * @param rid Repository ID
      */
     public boolean exists(String rid) {
         for (int i = 0;i < repositories.size(); i++) {
             if (((Repository) repositories.elementAt(i)).getID().equals(rid)) return true;
         }
         log.debug("No such repository: " + rid + " (" + getPropertiesURL() + ")");
         return false;
     }
 }
