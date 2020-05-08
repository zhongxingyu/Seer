 package org.wyona.yarep.impl;
 
 import org.apache.avalon.framework.configuration.Configuration;
 
 import org.wyona.commons.io.FileUtil;
 import org.wyona.yarep.core.Map;
 import org.wyona.yarep.core.Path;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.core.UID;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.BufferedReader;
 
 import org.apache.log4j.Category;
 
 /**
  *
  */
 public class DefaultMapImpl implements Map {
 
     private static Category log = Category.getInstance(DefaultMapImpl.class);
 
     protected File pathsDir;
 
     /**
      *
      */
     public void readConfig(Configuration mapConfig, File repoConfigFile) throws RepositoryException {
         try {
             pathsDir = new File(mapConfig.getAttribute("src"));
             if (!pathsDir.isAbsolute()) {
                 pathsDir = FileUtil.file(repoConfigFile.getParent(), pathsDir.toString());
             }
             log.debug(pathsDir.toString());
             // TODO: Throw Exception
             if (!pathsDir.exists()) log.error("No such file or directory: " + pathsDir);
         } catch(Exception e) {
             log.error(e);
             throw new RepositoryException("Could not read map configuration: " 
                     + repoConfigFile.getAbsolutePath() + e.getMessage(), e);
         }
     }
 
     /**
      *
      */
     public boolean isResource(Path path) throws RepositoryException {
         File file = new File(pathsDir + path.toString());
         File uidFile = new File(pathsDir + path.toString() + File.separator + ".yarep-uid");
         return uidFile.exists() || file.isFile();
     }
 
     /**
      *
      */
     public boolean exists(Path path) throws RepositoryException {
         File file = new File(pathsDir + path.toString());
         // TODO: Get name of repository for debugging ...
         //log.debug("Path (" + getName() + "): " + file);
         return file.exists();
     }
 
     /**
      *
      */
     public boolean delete(Path path) throws RepositoryException {
 /*
         File uidFile = new File(pathsDir + path.toString() + File.separator + ".yarep-uid");
         if (uidFile.isFile()) uidFile.delete();
 */
         File file = new File(pathsDir + path.toString());
         return org.wyona.commons.io.FileUtil.deleteDirectory(file);
     }
 
     /**
      *
      */
     public boolean isCollection(Path path) throws RepositoryException {
         File file = new File(pathsDir + path.toString());
         return (file.exists() && !isResource(path));
     }
 
     /**
      *
      */
     public Path[] getChildren(Path path) throws RepositoryException {
         File file = new File(pathsDir + path.toString());
         String[] filenames = file.list();
 
 	// NOTE: This situation should only occur if isResource(Path) didn't work properly!
         if (filenames == null) {
             log.warn("No children: " + path + " (" + file + ")");
             return new Path[0];
         }
 
         log.debug("Number of children: " + filenames.length + " (" + file + ")");
         Path[] children = new Path[filenames.length];
         for (int i = 0;i < children.length; i++) {
             if (path.toString().endsWith(File.separator)) {
                 children[i] = new Path(path + filenames[i]);
             } else {
                 // NOTE: Do not use File.separator here, because it's the repository path and not the Operating System File System path
                 children[i] = new Path(path + "/" + filenames[i]);
             }
             log.debug("Child: " + children[i]);
         }
         return children;
     }
 
     /**
      * Get UID
      */
     public synchronized UID getUID(Path path) throws RepositoryException {
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
                 throw new RepositoryException("Error reading uid of path: " + path.toString() 
                         + ": " + e.getMessage(), e);
             }
         }
 
         return null;
     }
 
     /**
      * Get UID
      */
    public synchronized UID create(Path path) throws RepositoryException {
         log.debug(pathsDir.toString());
         File uidFile = new File(pathsDir + path.toString() + File.separator + ".yarep-uid");
         log.debug(uidFile.toString());
 
         // TODO: Shouldn't the uid be written only if the writer is being closed successfully!
 	//String uid = "" + System.currentTimeMillis();
 	String uuid = org.apache.commons.id.uuid.UUID.randomUUID().toString();
 	//String uid = java.util.UUID.randomUUID().toString(); // Java 1.5.x
         try {
             File parent = new File(uidFile.getParent());
             if (!parent.exists()) {
                 log.warn("Directory will be created: " + parent);
                 parent.mkdirs();
             }
             // TODO: ...
             if (parent.isFile()) {
                 log.warn("Parent is a file and not a directory: " + parent);
             }
             FileWriter fw = new FileWriter(uidFile);
             fw.write(uuid);
             fw.close();
         } catch (Exception e) {
             log.error(e);
             throw new RepositoryException("Error creating uid for path: " + path.toString() 
                     + ": " + e.getMessage(), e);
         }
         return new UID(uuid);
     }
 
     /**
      *
      */
     public void addSymbolicLink(Path link, UID uid) throws RepositoryException {
         File uidFile = new File(pathsDir + link.toString() + File.separator + ".yarep-uid");
 
         String uuid = uid.toString();
         try {
             File parent = new File(uidFile.getParent());
             if (!parent.exists()) {
                 log.warn("Directory will be created: " + parent);
                 parent.mkdirs();
             }
             // TODO: ...
             if (parent.isFile()) {
                 log.warn("Parent is a file and not a directory: " + parent);
             }
             FileWriter fw = new FileWriter(uidFile);
             fw.write(uuid);
             fw.close();
         } catch (Exception e) {
             log.error(e);
             throw new RepositoryException("Error creating uid for path: " + link.toString() 
                     + ": " + e.getMessage(), e);
         }
     }
 }
