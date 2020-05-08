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
 import java.io.FilenameFilter;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Category;
 
 /**
  *
  */
 public class VFileSystemMapImpl implements Map {
 
     private static Category log = Category.getInstance(VFileSystemMapImpl.class);
 
     protected File pathsDir;
     protected Pattern[] ignorePatterns;
     protected ChildrenFilter childrenFilter = new ChildrenFilter();
     
 
     /**
      *
      */
     public void readConfig(Configuration mapConfig, File repoConfigFile) throws RepositoryException {
         try {
             setPathsDir(new File(mapConfig.getAttribute("src")), repoConfigFile);
             
             Configuration[] ignoreElements = mapConfig.getChildren("ignore");
             ignorePatterns = new Pattern[ignoreElements.length];
             for (int i=0; i<ignoreElements.length; i++) {
                 String patternString = ignoreElements[i].getAttribute("pattern");
                 ignorePatterns[i] = Pattern.compile(patternString);
                 log.debug("adding ignore pattern: " + ignorePatterns[i].pattern());
             }
 
         } catch(Exception e) {
             log.error(e);
             throw new RepositoryException("Could not read map configuration: " 
                     + repoConfigFile.getAbsolutePath() + e.getMessage(), e);
         }
     }
 
     /**
      * Set paths directory
      */
     public void setPathsDir(File src,  File repoConfigFile) throws RepositoryException {
         pathsDir = src;
         if (!pathsDir.isAbsolute()) {
             pathsDir = FileUtil.file(repoConfigFile.getParent(), pathsDir.toString());
         }
         log.info("Paths dir: " + pathsDir.toString());
 
         if (!pathsDir.exists()) {
             log.error("No such file or directory: " + pathsDir);
             throw new RepositoryException("No such file or directory: " + pathsDir);
         }
     }
     
     /**
      * Test if path should be ignored
      */
     protected boolean ignorePath(String path) {
         if (ignorePatterns != null) {
             for (int i=0; i<this.ignorePatterns.length; i++) {
                 Matcher matcher = this.ignorePatterns[i].matcher(path); 
                 if (matcher.matches()) {
                     if (log.isDebugEnabled()) {
                         log.debug(path + " matched ignore pattern " + ignorePatterns[i].pattern());
                     }
                     return true;
                 }
             }
         }
         if (log.isDebugEnabled()) {
             log.debug(path + " did not match any ignore patterns");
         }
         return false;
     }
 
     /**
      *
      */
     public boolean isResource(Path path) throws RepositoryException {
         File file = new File(pathsDir + path.toString());
         return file.isFile();
     }
 
     /**
      *
      */
     public boolean exists(Path path) throws RepositoryException {
         File file = new File(pathsDir + path.toString());
         // TODO: Get name of repository for debugging ...
         //log.debug("File: " + file);
         return file.exists() && !ignorePath(file.getPath());
     }
 
     /**
      *
      */
     public boolean delete(Path path) throws RepositoryException {
         // don't do anything because if we delete the file here, the delete
         // in the storage will fail later
         //File file = new File(pathsDir + path.toString());
         //return file.delete();
         return true;
     }
 
     /**
      *
      */
     public boolean isCollection(Path path) throws RepositoryException {
         File file = new File(pathsDir + path.toString());
         return file.isDirectory();
     }
 
     /**
      * Get children
      */
     public Path[] getChildren(Path path) throws RepositoryException {
         File file = new File(pathsDir + path.toString());
         if (!file.exists()) {
             log.warn("No such file or directory: " + file);
             return new Path[0];
         }
 
         String[] filenames = file.list(this.childrenFilter);
 
 	// NOTE: This situation should only occur if one is trying to get children for a file than a directory! One might want to consider to test first with isResource() or isCollection().
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
         // TODO: Check if leading slash should be removed ...
         return new UID(path.toString());
     }
 
     /**
      * Create UID
      */
     public synchronized UID create(Path path) throws RepositoryException {
         // TODO: Check if leading slash should be removed ...
         return new UID(path.toString());
     }
 
     /**
      *
      */
     public void addSymbolicLink(Path path, UID uid) throws RepositoryException {
         throw new RepositoryException("Symbolic links not implemented for virtual file system!");
     }
     
     /**
      * Ignore all children which are matched by an ignore pattern (see repository configuration, e.g. src/test/repository/node-fs-example/repository.xml)
      */
     protected class ChildrenFilter implements FilenameFilter {
         public boolean accept(File dir, String name) {
             
             if (VFileSystemMapImpl.this.ignorePath(name)) {
                 return false;
             } else {
                 return true;
             }
         }
     }
 }
