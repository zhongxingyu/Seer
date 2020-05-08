 package org.makumba.parade.listeners;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import net.contentobjects.jnotify.JNotify;
 import net.contentobjects.jnotify.JNotifyListener;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.makumba.aether.RelationComputationException;
 import org.makumba.parade.access.ActionLogDTO;
 import org.makumba.parade.aether.ActionTypes;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.init.ParadeProperties;
 import org.makumba.parade.init.RowProperties;
import org.makumba.parade.model.File;
 import org.makumba.parade.model.Parade;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.User;
 import org.makumba.parade.model.managers.FileManager;
 import org.makumba.parade.model.managers.MakumbaManager;
 import org.makumba.parade.tools.ParadeLogger;
 import org.makumba.parade.tools.SimpleFileFilter;
 import org.makumba.parade.tools.TriggerFilter;
 
 /**
  * Implementation of a JNotifyListener for ParaDe. See {@link http://jnotify.sourceforge.net/} for more information
  * about JNotify (note that ParaDe uses customised sources of JNotify so as to remove the warning messages in the logs)
  * 
  * @author Manuel Gay
  * 
  */
 public class ParadeJNotifyListener implements JNotifyListener {
 
     private static Logger logger = ParadeLogger.getParadeLogger(ParadeJNotifyListener.class.getName());
 
     private MakumbaManager makMgr = new MakumbaManager();
 
     public static final String LOCK = ".parade-lock~";
 
     public static Vector<String> lockedDirectories = new Vector<String>();
 
     private RowProperties rp = new RowProperties();
 
     private SimpleFileFilter sf = new SimpleFileFilter();
 
     public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
         logger
                 .fine("JNotifyTest.fileRenamed() : wd #" + wd + " root = " + rootPath + ", " + oldName + " -> "
                         + newName);
         // checkSpecialFile(rootPath, oldName);
         // checkSpecialFile(rootPath, newName);
 
         if (isLocked(rootPath, oldName, JNotify.FILE_RENAMED))
             return;
 
         if (InitServlet.aetherEnabled) {
             deleteRelations(rootPath, oldName);
             updateRelations(rootPath, newName);
         }
 
         cacheDeleted(rootPath, oldName);
         cacheNew(rootPath, newName);
 
     }
 
     public void fileModified(int wd, String rootPath, String name) {
         logger.fine("JNotifyTest.fileModified() : wd #" + wd + " root = " + rootPath + ", " + name);
         // checkSpecialFile(rootPath, name);
 
        if (isLocked(rootPath, name, JNotify.FILE_MODIFIED)){
             return;
        }
       
         if (InitServlet.aetherEnabled) {
             updateRelations(rootPath, name);
         }
 
         cacheModified(rootPath, name);
         logAction(rootPath, name, JNotify.FILE_MODIFIED);
     }
 
     public void fileDeleted(int wd, String rootPath, String name) {
         logger.fine("JNotifyTest.fileDeleted() : wd #" + wd + " root = " + rootPath + ", " + name);
         // checkSpecialFile(rootPath, name);
 
         if (InitServlet.aetherEnabled) {
             deleteRelations(rootPath, name);
         }
 
         if (isLocked(rootPath, name, JNotify.FILE_DELETED))
             return;
         cacheDeleted(rootPath, name);
         logAction(rootPath, name, JNotify.FILE_DELETED);
 
     }
 
     public void fileCreated(int wd, String rootPath, String name) {
         logger.fine("JNotifyTest.fileCreated() : wd #" + wd + " root = " + rootPath + ", " + name);
         // checkSpecialFile(rootPath, name);
 
         if (isLocked(rootPath, name, JNotify.FILE_CREATED))
             return;
 
         if (InitServlet.aetherEnabled) {
             updateRelations(rootPath, name);
         }
 
         cacheNew(rootPath, name);
         logAction(rootPath, name, JNotify.FILE_MODIFIED);
 
     }
 
     /**
      * Checks whether this is a special file and if something should be done, like refreshing a manager or so
      * 
      * @param rootPath
      *            the root path of the file
      * @param name
      *            the name of the file
      */
     private void checkSpecialFile(String rootPath, String name) {
 
         // TODO this should be available to all the model managers, via the interface
 
         // we check if this is the makumba jar, and if yes, we recompute the version number cache
         String filePath = rootPath + java.io.File.separator + name;
         if (filePath.indexOf("WEB-INF/lib/makumba") > -1 && filePath.endsWith(".jar")) {
 
             Session session = null;
             try {
                 session = InitServlet.getSessionFactory().openSession();
 
                 Parade p = (Parade) session.get(Parade.class, new Long(1));
                 Transaction tx = session.beginTransaction();
 
                 makMgr.hardRefresh(findRowFromContext(rootPath, p));
 
                 tx.commit();
 
             } finally {
                 session.close();
             }
         }
     }
 
     private synchronized void cacheNew(String rootPath, String fileName) {
         java.io.File f = new java.io.File(rootPath + java.io.File.separator + fileName);
 
         if (!f.exists())
             return;
 
         if (sf.accept(f)) {
             cacheFile(rootPath, fileName);
         }
     }
 
     private synchronized void cacheModified(String rootPath, String fileName) {
         java.io.File f = new java.io.File(rootPath + java.io.File.separator + fileName);
 
         if (!f.exists())
             return;
 
         // we don't refresh directories, since the modification of files in there are going to be
         // notified
         if (sf.accept(f) && !f.isDirectory()) {
             cacheFile(rootPath, fileName);
         }
     }
 
     private synchronized void cacheDeleted(String rootPath, String fileName) {
         java.io.File f = new java.io.File(rootPath + java.io.File.separator + fileName);
 
         if (sf.accept(f) && !f.isDirectory()) {
             deleteFile(rootPath, fileName);
         }
     }
 
     private void cacheFile(String rootPath, String fileName) {
         if (rootPath == null || fileName == null)
             return;
 
         logger.fine("Refreshing file cache for file " + fileName + " of directory " + rootPath);
 
         java.io.File f = new java.io.File(rootPath + java.io.File.separator + fileName);
 
         FileManager fileMgr = new FileManager();
         Session session = null;
 
         try {
             session = InitServlet.getSessionFactory().openSession();
 
             Parade p = (Parade) session.get(Parade.class, new Long(1));
             if(p == null) {
                 logger.warning("Could not acquire parade instance, jnotify event not logged. This may happen when parade build its cache the first time.");
                 return;
             }
             Row r = findRowFromContext(rootPath, p);
             Transaction tx = session.beginTransaction();
 
             // we cache the file
             fileMgr.cacheFile(r, f, true);
 
             tx.commit();
 
         } finally {
             session.close();
         }
 
         logger.fine("Finished refreshing file cache for file " + fileName + " of directory " + rootPath);
 
     }
 
     private void deleteFile(String rootPath, String fileName) {
         if (rootPath == null || fileName == null)
             return;
 
         logger.fine("Deleting file cache for file " + fileName + " of directory " + rootPath);
 
         java.io.File f = new java.io.File(rootPath + java.io.File.separator + fileName);
 
         FileManager fileMgr = new FileManager();
         Session session = null;
 
         try {
             session = InitServlet.getSessionFactory().openSession();
 
             Parade p = (Parade) session.get(Parade.class, new Long(1));
             if(p == null) {
                 logger.warning("Could not acquire parade instance, jnotify event not logged. This may happen when parade build its cache the first time.");
                 return;
             }
             Row r = findRowFromContext(rootPath, p);
             Transaction tx = session.beginTransaction();
 
             fileMgr.removeFileCache(r, rootPath, fileName);
 
             tx.commit();
 
         } finally {
             session.close();
         }
 
         logger.fine("Finished deleting file cache for file " + fileName + " of directory " + rootPath);
     }
 
     private Row findRowFromContext(String rowPath, Parade p) {
         
         Iterator<String> i = p.getRows().keySet().iterator();
 
         boolean row_found = false;
         Row contextRow = null;
         while (i.hasNext() && !row_found) {
             contextRow = p.getRows().get(i.next());
             row_found = rowPath.startsWith(contextRow.getRowpath());
         }
         return contextRow;
     }
 
     /**
      * Avoids conflicts with CVS Manager by checking whether there's a lock on the files to come in the directory /
      * subdirectories affected by the lock.
      * 
      */
     private boolean isLocked(String rootPath, String relativeFilePath, int mask) {
         if (rootPath == null || relativeFilePath == null)
             return false;
 
         String relativePath = relativeFilePath.indexOf(java.io.File.separator) > -1 ? relativeFilePath.substring(0,
                 relativeFilePath.lastIndexOf(java.io.File.separator)) : "";
         String fileName = relativeFilePath.indexOf(java.io.File.separator) > -1 ? relativeFilePath
                 .substring(relativeFilePath.lastIndexOf(java.io.File.separator) + 1) : relativeFilePath;
         String path = rootPath + (relativePath.length() > 0 ? java.io.File.separator : "") + relativePath;
         String filePath = path + java.io.File.separator + fileName;
         if (fileName.endsWith(LOCK) && mask == JNotify.FILE_CREATED) {
             // a lock was just created, we register it
             if (fileName.equals(LOCK)) {
                 lockedDirectories.add(path);
                 logger.fine("Adding lock for directory " + path);
             } else if (fileName.endsWith(LOCK) && fileName.length() > LOCK.length()) {
                 lockedDirectories.add(path + java.io.File.separator + fileName.substring(0, fileName.indexOf(LOCK)));
                 logger.fine("Adding lock for file " + path + java.io.File.separator
                         + fileName.substring(0, fileName.indexOf(LOCK)));
             }
             return true; // we don't want to cache this file anyway
 
         } else if (fileName.endsWith(LOCK) && mask == JNotify.FILE_DELETED) {
             // a lock was removed, we unregister the directory
             String lockPath = "";
             if (fileName.equals(LOCK))
                 lockPath = path;
             else if (fileName.endsWith(LOCK) && fileName.length() > LOCK.length())
                 lockPath = path + java.io.File.separator + fileName.substring(0, fileName.indexOf(LOCK));
 
             if (lockedDirectories.contains(lockPath)) {
                 lockedDirectories.remove(lockPath);
                 logger.fine("Removing lock " + lockPath);
             } else {
                 logger.severe("Tried to remove lock for directory " + path + " but there was no lock registered");
             }
             return true; // we don't want to cache this file anyway
         } else if (fileName.endsWith(LOCK) && mask == JNotify.FILE_MODIFIED) {
             // WTF?
             logger.warning("Lock of directory " + path + " modified, shouldn't happen.");
         }
 
         // does the actual check
         for (int i = 0; i < lockedDirectories.size(); i++) {
             if (path.startsWith(lockedDirectories.get(i)) || filePath.equals(lockedDirectories.get(i))) {
                 logger.fine("Lock detected for " + lockedDirectories.get(i));
                 return true;
             }
         }
 
         return false;
     }
 
     public static void updateRelations(String rootPath, String name) {
 
         if ((name.endsWith(".mdd") | name.endsWith(".java") | name.endsWith(".jsp")) && InitServlet.aetherEnabled) {
             logger.fine("Updating relations for file " + name + " in " + rootPath);
             if (!rootPath.startsWith(ParadeProperties.getParadeBase())) {
                 try {
                     InitServlet.getContextRelationComputer(rootPath).updateRelation(
                             rootPath
                                     + (rootPath.endsWith(java.io.File.separator)
                                             || name.startsWith(java.io.File.separator) ? "" : "/") + name);
                 } catch (RelationComputationException e) {
                     logger.warning("Failed updating relations for file " + name + " in " + rootPath + ": "
                             + e.getMessage());
                 }
                 logger.fine("Finished updating relations for file " + name + " in " + rootPath);
             }
         }
 
     }
 
     public static void deleteRelations(String rootPath, String name) {
 
         if ((name.endsWith(".mdd") | name.endsWith(".java") | name.endsWith(".jsp")) && InitServlet.aetherEnabled) {
             logger.fine("Deleting relations for file " + name + " in " + rootPath);
             try {
                 InitServlet.getContextRelationComputer(rootPath)
                         .deleteRelation(
                                 rootPath
                                         + (rootPath.endsWith(java.io.File.separator)
                                                 || name.startsWith(java.io.File.separator) ? "" : "/") + name);
             } catch (RelationComputationException e) {
                logger.warning("Failed deleting relations for file " + name + " in " + rootPath + ": "
                                 + e.getMessage());
             }
             logger.fine("Finished deleting relations for file " + name + " in " + rootPath);
         }
     }
 
     private void logAction(String root, String file, int action) {
 
         java.io.File f = new java.io.File(root + java.io.File.separator + file);
 
         if (!sf.accept(f))
             return;
 
         switch (action) {
 
         case JNotify.FILE_CREATED:
             logAction(root, file, ActionTypes.SAVE.action());
             break;
 
         case JNotify.FILE_MODIFIED:
             logAction(root, file, ActionTypes.SAVE.action());
             break;
 
         case JNotify.FILE_DELETED:
             logAction(root, file, ActionTypes.DELETE.action());
             break;
 
         default:
             break;
         }
 
     }
 
     private void logAction(String root, String file, String action) {
         Session s = null;
         try {
             s = InitServlet.getSessionFactory().openSession();
             Transaction tx = s.beginTransaction();
             Parade p = (Parade) s.get(Parade.class, new Long(1));
             if(p == null) {
                 logger.warning("Could not acquire parade instance, jnotify event not logged. This may happen when parade build its cache the first time.");
                 return;
             }
             Row r = findRowFromContext(root, p);
 
            
            if (r.getFiles().get(root + java.io.File.separator + file) == null) {
                logger.warning("File cannot be found, jnotify event nog logged");
                 return;
             }
 
             ActionLogDTO log = new ActionLogDTO();
             log.setAction(action);
             log.setDate(new Date());
             log.setParadecontext(r.getRowname());
             log.setFile(file);
 
             // TODO maybe we have to replace this with a more elaborate mechanism
             // such as trying to figure who does unison via a process listing
             User u = r.getUser();
             if (u != null) {
                 log.setUser(u.getLogin());
             } else {
                 // try to fetch it from rowstore
                 Map<String, String> defs = rp.getRowDefinitions().get(r.getRowname());
                 if (defs != null) {
                     String user = defs.get("user");
                     if (user != null) {
                         log.setUser(user);
                     } else {
                         log.setUser(User.getUnknownUser().getLogin());
                         // logger.severe("User for row "+r.getRowname() +
                         // " not set! Please go to the ParaDe admin interface and set it there!");
 
                     }
                 }
             }
 
             TriggerFilter.redirectToServlet("/servlet/org.makumba.parade.access.DatabaseLogServlet", log);
 
             tx.commit();
 
         } finally {
             if (s != null) {
                 s.close();
             }
         }
 
     }
 
     public static void removeDirectoryLock(String absoluteDirectoryPath) {
         java.io.File f = new java.io.File(absoluteDirectoryPath + java.io.File.separator + LOCK);
         f.delete();
     }
 
     public static void createDirectoryLock(String absoluteDirectoryPath) {
         java.io.File f = new java.io.File(absoluteDirectoryPath + java.io.File.separator + LOCK);
         try {
             f.createNewFile();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     public static void removeFileLock(String absoluteFilePath) {
         java.io.File lock = new java.io.File(absoluteFilePath + LOCK);
         lock.delete();
     }
 
     public static void createFileLock(String absoluteFilePath) {
         java.io.File lock = new java.io.File(absoluteFilePath + LOCK);
         try {
             lock.createNewFile();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 }
