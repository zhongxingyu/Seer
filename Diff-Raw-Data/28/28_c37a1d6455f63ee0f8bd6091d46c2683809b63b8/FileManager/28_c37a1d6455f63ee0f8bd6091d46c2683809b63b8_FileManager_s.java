 package org.makumba.parade.model.managers;
 
 import java.io.BufferedWriter;
 import java.io.FileFilter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.model.File;
 import org.makumba.parade.model.Parade;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.interfaces.FileRefresher;
 import org.makumba.parade.model.interfaces.ParadeManager;
 import org.makumba.parade.model.interfaces.RowRefresher;
 import org.makumba.parade.tools.ParadeLogger;
 import org.makumba.parade.tools.SimpleFileFilter;
 import org.makumba.parade.tools.WordCount;
 
 /**
  * This class manages the cache for the files in ParaDe.
  * 
  * TODO clean it up and document
  * 
  * @author Manuel Gay
  * 
  */
 public class FileManager implements RowRefresher, FileRefresher, ParadeManager {
     static Logger logger = ParadeLogger.getParadeLogger(FileManager.class.getName());
 
     private FileFilter filter = new SimpleFileFilter();
 
     public void softRefresh(Row row) {
         // TODO Auto-generated method stub
 
     }
 
     /*
      * Creates a first File for the row which is its root dir and invokes its refresh() method
      */
     public void hardRefresh(Row row) {
         logger.fine("Refreshing row information for row " + row.getRowname());
 
         File root = new File();
 
         try {
             java.io.File rootPath = new java.io.File(row.getRowpath());
             root.setName("_root_");
             root.setPath(rootPath.getCanonicalPath());
             root.setParentPath("");
             root.setRow(row);
             root.setDate(new Long(new java.util.Date().getTime()));
             root.setSize(new Long(0));
             root.setOnDisk(false);
             row.getFiles().clear();
             root.setIsDir(true);
             row.getFiles().put(root.getPath(), root);
 
         } catch (Throwable t) {
             logger.severe("Couldn't access row path of row " + row.getRowname());
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
 
         try {
 
             if (currDir.isDirectory()) {
 
                 java.io.File[] dir = currDir.listFiles();
 
                 Set<String> dirContent = new HashSet<String>();
                 for (java.io.File d : dir) {
 
                     if (filter.accept(d) && !(d.getName() == null)) {
                         dirContent.add(d.getCanonicalPath());
                         cacheFile(row, d, local);
                     }
                 }
 
                 // now we clear zombie entries from the cache
                 File fileInCache = row.getFiles().get(path);
                 if (fileInCache != null) {
                     for (File child : fileInCache.getChildren(null)) {
 
                         if (dirContent.contains(child.getPath())) {
                             continue;
                             // if the file is not on disk but it was scheduled for CVS deletion, we keep it
                         } else if (!dirContent.contains(child.getPath()) && child.getCvsStatus() != null
                                 && child.getCvsStatus() == CVSManager.DELETED) {
                             child.setOnDisk(false);
                             continue;
                         } else {
                             row.getFiles().remove(child.getPath());
                         }
                     }
                 }
             }
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
     }
 
     /**
      * Creates / updates file cache entries
      * 
      * @param row
      *            the row this file belongs to
      * @param file
      *            the java.io.File to extract metadata from
      * @param local
      *            whether this is a local or a recursive cache update
      */
     public void cacheFile(Row row, java.io.File file, boolean local) {
         if (!file.exists() || file == null) {
             logger.warning("Trying to add non-existing/null file");
             return;
         }
         if (row == null) {
             logger.severe("Row was null while trying to add file with path " + file.getAbsolutePath());
             return;
         }
 
         if (file.isDirectory()) {
             File dirData = setFileData(row, file, true);
             addFile(row, dirData);
 
             if (!local) {
                 directoryRefresh(row, dirData.getPath(), false);
             }
 
         } else if (file.isFile()) {
             File fileData = setFileData(row, file, false);
             makeParentDirs(row, fileData);
             addFile(row, fileData);
         }
     }
 
     private void makeParentDirs(Row row, File f) {
         File parent = row.getFiles().get(f.getParentPath().replace('/', java.io.File.separatorChar));
         if (parent == null) {
             logger.warning("Creating new parent directory cache " + f.getParentPath() + " for file " + f.getPath());
             parent = setFileData(row, new java.io.File(f.getParentPath()), true);
             addFile(row, parent);
             makeParentDirs(row, parent);
         }
     }
 
     /* adding file to Row files */
     private void addFile(Row row, File fileData) {
         // +++
         if (fileData.getCrawled() == null) {
             // set to 0 when you insert the file into the database so the crawler will crawl the file for the first time
             fileData.setCrawled(new Long(0));
         }
         // ---
         row.getFiles().put(fileData.getPath(), fileData);
 
         // logger.warning("Added file: "+fileData.getName());
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
         if ((fileData = row.getFiles().get(path)) != null) {
             fileData.setDate(new Long(file.lastModified()));
             fileData.setSize(new Long(file.length()));
             fileData.setOnDisk(true);
             if (!isDir) {
                 fileData.setPreviousChars(fileData.getCurrentChars());
                 fileData.setCurrentChars(WordCount.count(file.getAbsolutePath()));
             }
 
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
             if (!isDir) {
                 fileData.setCurrentChars(WordCount.count(file.getAbsolutePath()));
                 fileData.setPreviousChars(WordCount.count(file.getAbsolutePath()));
             }
             fileData.setOnDisk(true);
         }
         return fileData;
     }
 
     public static File setVirtualFileData(Row r, File path, String name, boolean dir) {
         File virtualFile = new File();
         virtualFile.setName(name);
         virtualFile.setPath(path.getPath() + java.io.File.separator + name);
         virtualFile.setParentPath(path.getPath().replace(java.io.File.separatorChar, '/'));
         virtualFile.setOnDisk(false);
         virtualFile.setIsDir(dir);
         virtualFile.setRow(r);
         virtualFile.setDate(new Long((new Date()).getTime()));
         virtualFile.setSize(new Long(0));
         virtualFile.setCurrentChars(0);
         virtualFile.setPreviousChars(0);
         return virtualFile;
     }
 
     public void newRow(String name, Row r, Map<String, String> m) {
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
         logger.severe("Error while trying to delete file " + f.getAbsolutePath() + " " + "\n" + "Reason: exists: "
                 + f.exists() + ", canRead: " + f.canRead() + ", canWrite: " + f.canWrite());
         return "Error while trying to delete file " + f.getName();
     }
 
     public void fileRefresh(Row row, String absolutePath) {
         java.io.File f = new java.io.File(absolutePath);
         File cacheFile = null;
         if (!f.exists() && (cacheFile = row.getFiles().get(absolutePath)) != null) {
             // file was deleted but cache still exists
             removeFileCache(cacheFile);
             return;
         } else if (!f.exists() && row.getFiles().get(absolutePath) == null) {
             return;
         }
         cacheFile(row, f, true);
 
     }
 
     public void removeFileCache(Row r, String path, String entry) {
         File cacheFile = r.getFiles().get(path + java.io.File.separator + entry);
         if (cacheFile == null)
             return;
 
         removeFileCache(cacheFile);
     }
 
     public void removeFileCache(File file) {
         // if there is CVS data for this file we keep it and set is as virtual
         if (file.getCvsStatus() != null) {
             file.setOnDisk(false);
         } else
             file.getRow().getFiles().remove(file.getPath());
     }
 
     public static void updateSimpleFileCache(String context, String path, String filename) {
         logger.fine("Refreshing file cache for file " + filename + " in path " + path + " of row " + context);
         Session s = InitServlet.getSessionFactory().openSession();
         Parade p = (Parade) s.get(Parade.class, new Long(1));
         Row r = Row.getRow(p, context);
         Transaction tx = s.beginTransaction();
         FileManager fileMgr = new FileManager();
         fileMgr.fileRefresh(r, path + java.io.File.separator + filename);
         tx.commit();
         s.close();
         logger.fine("Finished refreshing file cache for file " + filename + " in path " + path + " of row " + context);
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
     public static void checkShouldCache(String context, Collection<File> files) {
         for (File file : files) {
             java.io.File f = new java.io.File(file.getPath());
             if (!f.exists()) {
                 if (file.getCvsStatus() != null && file.getCvsStatus().equals(CVSManager.DELETED)) {
                     file.getRow().getFiles().remove(file.getPath());
                 }
             }
         }
     }
 }
