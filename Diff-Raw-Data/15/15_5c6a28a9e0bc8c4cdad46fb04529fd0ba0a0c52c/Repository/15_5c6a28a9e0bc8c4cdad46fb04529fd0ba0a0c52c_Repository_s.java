 package org.wyona.yarep.core;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 
 import org.apache.log4j.Category;
 
 import org.wyona.commons.io.FileUtil;
 
 /**
  *
  */
 public class Repository {
 
     private static Category log = Category.getInstance(Repository.class);
 
     protected String id;
     protected File configFile;
 
     protected String name;
 
     protected Map map;
     protected Storage storage;
 
     private boolean fallback = false;
 
     /**
      *
      */
     public Repository(String id, File configFile) {
         this.id = id;
         this.configFile = configFile;
 
         readConfiguration();
     }
 
     /**
      * Read respectively load repository configuration
      */
     private void readConfiguration() {
         DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
         Configuration config;
 
         try {
             config = builder.buildFromFile(configFile);
 
 	    name = config.getChild("name", false).getValue();
 
             Configuration pathConfig = config.getChild("paths", false);
 
             fallback = pathConfig.getAttributeAsBoolean("fallback", false);
             String pathsClassname = pathConfig.getAttribute("class", null);
             if (pathsClassname != null) {
                 log.debug(pathsClassname);
                 Class pathsClass = Class.forName(pathsClassname);
                 map = (Map) pathsClass.newInstance();
             } else {
                 map = new org.wyona.yarep.impl.DefaultMapImpl();
             }
             map.readConfig(pathConfig, configFile);
 
             Configuration storageConfig = config.getChild("storage", false);
 
             String storageClassname = storageConfig.getAttribute("class", null);
             log.debug(storageClassname);
             Class storageClass = Class.forName(storageClassname);
             storage = (Storage) storageClass.newInstance();
             storage.readConfig(storageConfig, configFile);
             log.debug(storage.getClass().getName());
         } catch (Exception e) {
             log.error(e.toString());
         }
     }
 
     /**
      *
      */
     public String toString() {
         return "Repository: ID = " + id + ", Configuration-File = " + configFile + ", Name = " + name;
     }
 
     /**
      * Get repository ID
      */
     public String getID() {
         return id;
     }
 
     /**
      * Get repository name
      */
     public String getName() {
         return name;
     }
 
     /**
      * Get repository configuration file
      */
     public File getConfigFile() {
         return configFile;
     }
 
     /**
      *
      */
     public Writer getWriter(Path path) throws IOException {
         OutputStream out = getOutputStream(path);
         if (out != null) {
             return new OutputStreamWriter(getOutputStream(path), "UTF-8");
         } else {
             return null;
         }
     }
 
     /**
      *
      */
     public OutputStream getOutputStream(Path path) throws IOException  {
         UID uid = getUID(path);
        // TODO: Implement fallback!
        if (uid == null) uid = map.createUID(path);
	/*
         if (uid == null) {
             if (fallback) {
                 log.warn("No path to get UID from! Fallback to : " + path);
                 uid = new UID(path.toString());
             } else {
                 uid = map.createUID(path);
             }
         }
	*/
         log.debug(uid.toString());
         return storage.getOutputStream(uid, path);
     }
 
     /**
      *
      */
     public Reader getReader(Path path) throws IOException {
         return new InputStreamReader(getInputStream(path), "UTF-8");
     }
 
     /**
      *
      */
     public InputStream getInputStream(Path path) throws IOException {
         UID uid = null;
         if (!exists(path)) {
             if (fallback) {
                 log.warn("No UID! Fallback to : " + path);
                 uid = new UID(path.toString());
             } else {
                 throw new NoSuchNodeException(path, this);
             }
         } else {
             uid = getUID(path);
         }
         if (uid == null) {
             log.error("No UID: " + path);
             return null;
         }
         log.debug(uid.toString());
         return storage.getInputStream(uid, path);
     }
 
     /**
      *
      */
     public long getLastModified(Path path) {
         UID uid = getUID(path);
         if (uid == null) {
             log.error("No UID: " + path);
             return -1;
         }
         return storage.getLastModified(uid, path);
     }
 
     /**
      * @return true if node has been deleted, otherwise false
      */
     public boolean delete(Path path) {
         if(map.isCollection(path)) {
             log.warn("Node is a collection and hence cannot be deleted: " + path);
             return false;
         }
         UID uid = getUID(path);
         if (uid == null) {
            log.error("No UID: " + path);
            return false;
         }
         return map.delete(path) && storage.delete(uid, path);
     }
 
     /**
      * Not implemented yet
      * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/impl/FileSource.html#getValidity()
      * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/SourceValidity.html
      */
     public void getValidity(Path path) {
         log.error("TODO: No implemented yet!");
     }
 
     /**
      * Not implemented yet
      * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/impl/FileSource.html#getContentLength()
      */
     public void getContentLength(Path path) {
         log.error("TODO: No implemented yet!");
     }
 
     /**
      * Not implemented yet
      * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/impl/FileSource.html#getURI()
      */
     public void getURI(Path path) {
         log.error("TODO: No implemented yet!");
     }
 
     /**
      *
      */
     public boolean isResource(Path path) {
         return map.isResource(path);
     }
 
     /**
      * One might want to discuss what is a collection. A resource for instance could
      * also be a collection, but a collection with some default content.
      * In the case of JCR there are only nodes and properties!
      */
     public boolean isCollection(Path path) {
         return map.isCollection(path);
     }
 
     /**
      *
      */
     public boolean exists(Path path) {
        return map.exists(path);
     }
 
     /**
      *
      */
     public Path[] getChildren(Path path) {
         // TODO: Order by last modified resp. alphabetical resp. ...
         return map.getChildren(path);
     }
 
     /**
      * Get UID
      *
      * http://www.ietf.org/rfc/rfc4122.txt
      * http://incubator.apache.org/jackrabbit/apidocs/org/apache/jackrabbit/uuid/UUID.html
      * http://www.webdav.org/specs/draft-leach-uuids-guids-01.txt
      */
     public synchronized UID getUID(Path path) {
         return map.getUID(path);
     }
 }
