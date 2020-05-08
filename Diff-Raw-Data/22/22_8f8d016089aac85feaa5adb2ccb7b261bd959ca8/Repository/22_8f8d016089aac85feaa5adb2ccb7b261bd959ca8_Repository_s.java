 package org.wyona.yarep.core;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 
 import org.apache.log4j.Category;
 
 import org.wyona.util.FileUtil;
 
 /**
  *
  */
 public class Repository {
 
     private static Category log = Category.getInstance(Repository.class);
 
     protected String id;
     protected File configFile;
 
     protected String name;
 
     protected File pathsDir;
 
     protected Storage storage;
 
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
             pathsDir = new File(pathConfig.getAttribute("src"));
             if (!pathsDir.isAbsolute()) {
                 pathsDir = FileUtil.file(configFile.getParent(), pathConfig.getAttribute("src"));
             }
             log.debug(pathsDir.toString());
 
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
      *
      */
     public String getID() {
         return id;
     }
 
     /**
      *
      */
     public Writer getWriter(Path path) {
         UID uid = getUID(path);
         log.debug(uid.toString());
         return storage.getWriter(uid, path);
     }
 
     /**
      *
      */
     public OutputStream getOutputStream(Path path) {
         UID uid = getUID(path);
         log.debug(uid.toString());
         return storage.getOutputStream(uid, path);
     }
 
     /**
      *
      */
     public Reader getReader(Path path) throws NoSuchNodeException {
         if (!exists(path)) throw new NoSuchNodeException(path);
         UID uid = getUID(path);
         log.debug(uid.toString());
         return storage.getReader(uid, path);
     }
 
     /**
      *
      */
     public InputStream getInputStream(Path path) {
         // TODO: if (!exists(path)) throw new NoSuchNodeException(path);
         UID uid = getUID(path);
         log.debug(uid.toString());
         return storage.getInputStream(uid, path);
     }
 
     /**
      *
      */
    public boolean exists(Path path) {
         File uidFile = new File(pathsDir + path.toString() + File.separator + ".yarep-uid");
         return uidFile.exists();
     }
 
     /**
      * Get UID
      */
     public synchronized UID getUID(Path path) {
         log.debug(pathsDir.toString());
         File uidFile = new File(pathsDir + path.toString() + File.separator + ".yarep-uid");
         log.debug(uidFile.toString());
         if (uidFile.exists()) {
             try {
                 FileReader fr = new FileReader(uidFile);
                 BufferedReader br = new BufferedReader(fr);
                 String existingUID = br.readLine();
                 br.close();
                 fr.close();
                 return new UID(existingUID);
             } catch (Exception e) {
                 log.error(e);
                 return null;
             }
         }
 
         // TODO: Shouldn't the uid be written only if the writer is being closed successfully!
 	String uid = "" + System.currentTimeMillis();
         try {
             File parent = new File(uidFile.getParent());
             if (!parent.exists()) {
                 log.warn("Directory will be created: " + parent);
                 parent.mkdirs();
             }
             FileWriter fw = new FileWriter(uidFile);
             fw.write(uid);
             fw.close();
         } catch (Exception e) {
             log.error(e);
         }
         return new UID(uid);
     }
 }
