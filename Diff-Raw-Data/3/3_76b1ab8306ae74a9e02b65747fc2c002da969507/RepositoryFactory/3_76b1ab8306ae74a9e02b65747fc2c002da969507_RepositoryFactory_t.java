 package org.wyona.yarep.core;
 
 import java.io.File;
 import java.net.URL;
 import java.net.URI;
 import java.util.Properties;
 
 import org.apache.log4j.Category;
 
 import org.wyona.util.FileUtil;
 
 /**
  *
  */
 public class RepositoryFactory {
 
     private static Category log = Category.getInstance(RepositoryFactory.class);
 
     public static final String DEFAULT_CONFIGURATION_FILE = "yarep.properties";
 
     private Repository[] repositories;
 
     /**
      *
      */
     public RepositoryFactory() throws Exception {
         URL propertiesURL = RepositoryFactory.class.getClassLoader().getResource(DEFAULT_CONFIGURATION_FILE);
         Properties props = new Properties();
         try {
             props.load(propertiesURL.openStream());
             File propsFile = new File(propertiesURL.getFile());
             //File propsFile = new File(new URI(propertiesURL.toString()));
 
 	    String separator = ",";
             String[] tokens = props.getProperty("configurations").split(separator);
             if (tokens.length % 2 != 0) {
                 throw new Exception("Wrong number of config parameters: " + DEFAULT_CONFIGURATION_FILE);
             }
             repositories = new Repository[tokens.length / 2];
             for (int i = 0;i < tokens.length / 2; i++) {
                 String repoID = tokens[2 * i];
                 String configFilename = tokens[2 * i + 1];
                 log.debug("PARENT: " + propsFile.getParent());
                 log.debug("Filename: " + configFilename);
                 File configFile;
                 if (new File(configFilename).isAbsolute()) {
                     configFile = new File(configFilename);
                 } else {
                     configFile = FileUtil.file(propsFile.getParent(), configFilename);
                 }
                 log.debug("File: " + configFile.getAbsolutePath());
                 Repository rt = new Repository(repoID, configFile);
                 log.info(rt.toString());
                 repositories[i] = rt;
             }
 
             // see src/java/org/wyona/meguni/parser/Parser.java
         } catch (Exception e) {
             log.error(e.toString());
         }
     }
 
     /**
      *
      */
     public String toString() {
         String s = "Show all repositories listed within yarep.properties:";
         for (int i = 0;i < repositories.length; i++) {
             s = s + "\n" + repositories[i];
         }
         return s;
     }
 
     /**
      * Get repository from yarep.properties
      *
      * @param rid Repository ID
      */
     public Repository newRepository(String rid) {
         for (int i = 0;i < repositories.length; i++) {
             if (repositories[i].getID().equals(rid)) return repositories[i];
         }
         log.error("No such repository: " + rid);
         return null;
     }
 
     /**
      * Get repository from specified config, whereas config is being resolved relative to classpath
      */
     public Repository newRepository(File config) {
         if (!config.isAbsolute()) {
             URL configURL = RepositoryFactory.class.getClassLoader().getResource(config.toString());
             try {
                File configFile = new File(configURL.getFile());
                //File configFile = new File(new URI(configURL.toString()));
                 log.debug("Config file: " + configFile);
                 // TODO: what about the repository ID?
                 return new Repository("null", configFile);
             } catch (Exception e) {
                 log.error(e);
                 return null;
             }
         }
         // TODO: what about the repository ID?
         return new Repository("null", config);
     }
 }
