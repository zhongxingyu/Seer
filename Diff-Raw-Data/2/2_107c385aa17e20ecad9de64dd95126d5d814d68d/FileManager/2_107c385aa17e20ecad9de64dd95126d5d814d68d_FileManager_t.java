 package org.makumba.parade.model.managers;
 
 import java.io.BufferedWriter;
 import java.io.FileFilter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.model.File;
 import org.makumba.parade.model.FileCVS;
 import org.makumba.parade.model.Parade;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.interfaces.CacheRefresher;
 import org.makumba.parade.model.interfaces.ParadeManager;
 import org.makumba.parade.model.interfaces.RowRefresher;
 import org.makumba.parade.tools.SimpleFileFilter;
 
 /**
  * This class manages the cache for the files in ParaDe.
  * 
  * TODO clean it up and document
  * 
  * @author Manuel Gay
  *
  */
 public class FileManager implements RowRefresher, CacheRefresher, ParadeManager {
 
     static Logger logger = Logger.getLogger(FileManager.class.getName());
 
     private FileFilter filter = new SimpleFileFilter();
 
     /*
      * Creates a first File for the row which is its root dir and invokes its refresh() method
      */
     public void rowRefresh(Row row) {
         logger.debug("Refreshing row information for row " + row.getRowname());
 
         File root = new File();
 
         try {
             java.io.File rootPath = new java.io.File(row.getRowpath());
             root.setName("_root_");
             root.setPath(rootPath.getCanonicalPath());
             root.setParentPath("");
             root.setRow(row);
             root.setDate(new Long(new java.util.Date().getTime()));
             root.setFiledata(new HashMap());
             root.setSize(new Long(0));
             root.setOnDisk(false);
             row.getFiles().clear();
             root.setIsDir(true);
             row.getFiles().put(root.getPath(), root);
 
         } catch (Throwable t) {
             logger.error("Couldn't access row path of row " + row.getRowname(), t);
         }
 
         root.refresh();
 
     }
 
     /**
      * Refreshes the cache state of a directory
      * 
      * @param row
      *            the row we work with
      * @param path
      *            the path to the directory
      * @param local
      *            indicates whether this should be a local refresh or if it should propagate also to subdirs
      * 
      * 
      */
     public synchronized void directoryRefresh(Row row, String path, boolean local) {
         java.io.File currDir = new java.io.File(path);
 
         if (currDir.isDirectory()) {
             
             java.io.File[] dir = currDir.listFiles();
             
             Set dirContent = new HashSet();
             for(int i = 0; i < dir.length; i++) {
                 dirContent.add(dir[i].getAbsolutePath());
             }
             
             // clear the cache of the entries of this directory
             File fileInCache = (File) row.getFiles().get(path);
             if(fileInCache != null) {
                 List children = fileInCache.getChildren(null);
                 for(int i = 0; i<children.size(); i++) {
                     File child = (File) children.get(i);
 
                     // if we do a local update only, we keep the subdirectories
                     if (local && child.getIsDir() && dirContent.contains(child.getPath()))
                         continue;
                     
                     // otherwise, we trash
                     row.getFiles().remove(child.getPath());
                 }
             }
             
             for (int i = 0; i < dir.length; i++) {
                 if (filter.accept(dir[i]) && !(dir[i].getName() == null)) {
 
                     java.io.File file = dir[i];
 
                     cacheFile(row, file, local);
                 }
             }
         }
     }
     
     /**
     
     public synchronized void directoryRefresh(Row row, String path, boolean local) {
         java.io.File currDir = new java.io.File(path);
 
         if (currDir.isDirectory()) {
 
             java.io.File[] dir = currDir.listFiles();
 
             Set dirContent = new HashSet();
             for (int i = 0; i < dir.length; i++) {
                 dirContent.add(dir[i].getAbsolutePath());
             }
             
             HashSet<String> cachedFiles = new HashSet<String>();
             
             
             logger.debug("Starting to populate cache entries of directory " + path + " of row " + row.getRowname());
             for (int i = 0; i < dir.length; i++) {
                 if (filter.accept(dir[i]) && !(dir[i].getName() == null)) {
 
                     java.io.File file = dir[i];
 
                     cacheFile(row, file, local, cachedFiles);
                 }
             }
             logger.debug("Finished updating cache");
 
             // clear the cache of the entries of this directory
             logger.debug("Starting to clear the invalid cache entries of directory " + path + " of row " + row.getRowname());
             File fileInCache = (File) row.getFiles().get(path);
             if (fileInCache != null) {
                 List<String> children = fileInCache.getChildrenPaths();
                 for (int i = 0; i < children.size(); i++) {
                     String childPath = children.get(i);
                     
                     if(!cachedFiles.contains(childPath)) {
                         // if we do a local update only, we keep the subdirectories
                         if (local && (new java.io.File(childPath).isDirectory() && dirContent.contains(childPath)))
                             continue;
                         
                         // otherwise, we trash
                         else
                             row.getFiles().remove(childPath);
                     }
                 }
             }
             logger.debug("Finished clearing cache");
         }
     }
     **/
 
 
     public void cacheFile(Row row, java.io.File file, boolean local) {
         if(!file.exists() || file == null) {
            logger.warn("Trying to add non-existing/null file");
             return;
         }
         if(row == null) {
             logger.error("Row was null while trying to add file with path "+file.getAbsolutePath());
             return;
         }
         
         if (file.isDirectory()) {
             File dirData = setFileData(row, file, true);
             addFile(row, dirData);
 
             if (!local)
                 dirData.refresh();
 
         } else if (file.isFile()) {
             File fileData = setFileData(row, file, false);
             addFile(row, fileData);
         }
     }
 
     /* adding file to Row files */
     private void addFile(Row row, File fileData) {
 
         row.getFiles().put(fileData.getPath(), fileData);
         
         // logger.warn("Added file: "+fileData.getName());
     }
 
     /* setting File informations */
     private File setFileData(Row row, java.io.File file, boolean isDir) {
         File fileData = null;
         String path = null;
         try {
             path = (file.getCanonicalPath());
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         // if we already had a file in cache we simple update it
         if((fileData = row.getFiles().get(path)) != null) {
             fileData.setDate(new Long(file.lastModified()));
             fileData.setSize(new Long(file.length()));
             fileData.setOnDisk(true);
         
         // otherwise we make a new file
         } else {
             fileData = new File();
             fileData.setIsDir(isDir);
             fileData.setRow(row);
             try {
                 fileData.setPath(file.getCanonicalPath());
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             fileData.setParentPath(file.getParent().replace(java.io.File.separatorChar, '/'));
             fileData.setName(file.getName());
             fileData.setDate(new Long(file.lastModified()));
             fileData.setSize(new Long(file.length()));
             fileData.setOnDisk(true);
         }
         return fileData;
     }
 
     public static File setVirtualFileData(Row r, File path, String name, boolean dir) {
         File cvsfile = new File();
         cvsfile.setName(name);
         cvsfile.setPath(path.getPath() + java.io.File.separator + name);
         cvsfile.setParentPath(path.getPath().replace(java.io.File.separatorChar, '/'));
         cvsfile.setOnDisk(false);
         cvsfile.setIsDir(dir);
         cvsfile.setRow(r);
         cvsfile.setDate(new Long((new Date()).getTime()));
         cvsfile.setSize(new Long(0));
         return cvsfile;
     }
 
     public void newRow(String name, Row r, Map m) {
         // TODO Auto-generated method stub
 
     }
 
     public String newFile(Row r, String path, String entry) {
         java.io.File f = new java.io.File((path + "/" + entry).replace('/', java.io.File.separatorChar));
         if (f.exists() && f.isFile())
             return "This file already exists";
         boolean success = false;
         try {
             success = f.createNewFile();
         } catch (IOException e) {
             e.printStackTrace();
             return ("Error while trying to create file " + entry);
         }
         if (success) {
             return "OK#" + f.getName();
         }
         return "Error while trying to create file " + entry;
     }
 
     public String newDir(Row r, String path, String entry) {
         String absolutePath = (path + "/" + entry + "/").replace('/', java.io.File.separatorChar);
         java.io.File f = new java.io.File(absolutePath);
         if (f.exists() && f.isDirectory())
             return "This directory already exists";
 
         boolean success = f.mkdir();
 
         if (success) {
             return "OK#" + f.getName();
         }
         return "Error while trying to create directory " + absolutePath
                 + ". Make sure ParaDe has the security rights to write on the filesystem.";
 
     }
 
     public String deleteFile(Row r, String path, String entry) {
 
         java.io.File f = new java.io.File(path + java.io.File.separator + entry);
         boolean success = f.delete();
         if (success) {
             return "OK#" + f.getName();
         }
         logger.error("Error while trying to delete file " + f.getAbsolutePath() + " " + "\n" + "Reason: exists: "
                 + f.exists() + ", canRead: " + f.canRead() + ", canWrite: " + f.canWrite());
         return "Error while trying to delete file " + f.getName();
     }
 
     public void fileRefresh(Row row, String path) {
         java.io.File f = new java.io.File(path);
         File cacheFile = null;
         if (!f.exists() && (cacheFile = row.getFiles().get(path)) != null) {
             // file was deleted but cache still exists
             removeFileCache(cacheFile);
             return;
         } else if(!f.exists() && row.getFiles().get(path) == null) {
             return;
         }
         cacheFile(row, f, false);
 
     }
     
     public void removeFileCache(Row r, String path, String entry) {
         File cacheFile = (File) r.getFiles().get(path + java.io.File.separator + entry);
         if(cacheFile == null)
             return;
         
         removeFileCache(cacheFile);
     }
     
     public void removeFileCache(File file) {
         
         Object cvsData = file.getFiledata().get("cvs");
 
         // if there is CVS data for this file
         // TODO do this check for Tracker as well once it will be done
         if (cvsData != null) {
             file.setOnDisk(false);
         } else
             file.getRow().getFiles().remove(file.getPath());
     }
     
     public static void updateSimpleFileCache(String context, String path, String filename) {
         logger.debug("Refreshing file cache for file "+filename+" in path "+path+" of row "+context);
         Session s = InitServlet.getSessionFactory().openSession();
         Parade p = (Parade) s.get(Parade.class, new Long(1));
         Row r = Row.getRow(p, context);
         Transaction tx = s.beginTransaction();
         FileManager fileMgr = new FileManager();
         fileMgr.fileRefresh(r, path + java.io.File.separator + filename);
         tx.commit();
         s.close();
         logger.debug("Finished refreshing file cache for file "+filename+" in path "+path+" of row "+context);
     }
 
     /* updates the File cache of a directory */
     public static void updateDirectoryCache(String context, String path, boolean local) {
         FileManager fileMgr = new FileManager();
         Session s = InitServlet.getSessionFactory().openSession();
         Parade p = (Parade) s.get(Parade.class, new Long(1));
         Row r = Row.getRow(p, context);
         Transaction tx = s.beginTransaction();
 
         fileMgr.directoryRefresh(r, path, local);
 
         tx.commit();
         s.close();
     }
 
     public static void fileWrite(java.io.File file, String content) throws IOException {
         PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
         pw.print(content);
     }
 
     // checks if a file should still be cached or if it's a zombie
     // FIXME should probably be in a more general CacheManager or so
     public static void checkShouldCache(String context, String absolutePath, String absoluteFilePath) {
         Session s = InitServlet.getSessionFactory().openSession();
         Parade p = (Parade) s.get(Parade.class, new Long(1));
         Row r = Row.getRow(p, context);
         Transaction tx = s.beginTransaction();
 
         java.io.File f = new java.io.File(absoluteFilePath);
         if(!f.exists()) {
             File cachedFile = (File) r.getFiles().get(absoluteFilePath);
             if(cachedFile != null) {
                 boolean hasCvsData = (cachedFile.getFiledata().get("cvs") != null);
                 if(!hasCvsData) {
                     r.getFiles().remove(absoluteFilePath);
                 }
             }
         }
 
         tx.commit();
         s.close();
         
     }
 
 }
