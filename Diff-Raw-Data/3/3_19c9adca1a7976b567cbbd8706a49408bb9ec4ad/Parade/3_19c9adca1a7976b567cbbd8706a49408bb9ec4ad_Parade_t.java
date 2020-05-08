 package org.makumba.parade.model;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 import net.contentobjects.jnotify.JNotify;
 import net.contentobjects.jnotify.JNotifyException;
 import net.contentobjects.jnotify.JNotifyListener;
 
 import org.apache.log4j.Logger;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.init.ParadeProperties;
 import org.makumba.parade.init.RowProperties;
 import org.makumba.parade.model.managers.AntManager;
 import org.makumba.parade.model.managers.CVSManager;
 import org.makumba.parade.model.managers.FileManager;
 import org.makumba.parade.model.managers.MakumbaManager;
 import org.makumba.parade.model.managers.TrackerManager;
 import org.makumba.parade.model.managers.WebappManager;
 import org.makumba.parade.view.managers.RowStoreViewManager;
 
 public class Parade {
 
     private Long id;
 
     private String baseDir = new String();
 
     private Map rows = new HashMap();
 
     private static Logger logger = Logger.getLogger(Parade.class.getName());
 
     // ParaDe managers
     // TODO these should be injected using Spring
 
     public FileManager fileMgr = new FileManager();
 
     public CVSManager CVSMgr = new CVSManager();
 
     public AntManager antMgr = new AntManager();
 
     public WebappManager webappMgr = new WebappManager();
 
     public MakumbaManager makMgr = new MakumbaManager();
 
     /*
      * 1. Calls create row for the new/to be updated rows 2. Calls for each row: - rowRefresh() - directoryRefresh() 3.
      * Add listener to trigger refresh if needed
      */
     public void refresh() {
         logger.info("Starting ParaDe-wide refresh...");
 
         this.baseDir = ParadeProperties.getParadeBase();
 
         /* Reads the row definitions and perfoms update/creation */
         Map rowstore = (new RowProperties()).getRowDefinitions();
         if (rowstore.isEmpty()) {
             logger.warn("No row definitions found, check RowProperties");
         }
 
         createRows(rowstore);
 
         /*
          * TODO: read in config class/file which managers are row managers and and launch rowRefresh(row)) for all of
          * them
          */
 
         Iterator i = rows.keySet().iterator();
         while (i.hasNext()) {
 
             Row r = (Row) rows.get((String) i.next());
 
             fileMgr.rowRefresh(r);
             CVSMgr.rowRefresh(r);
             antMgr.rowRefresh(r);
             webappMgr.rowRefresh(r);
             makMgr.rowRefresh(r);
         }
 
         addJNotifyListeners();
 
         logger.info("ParaDe-wide refresh finished");
 
     }
 
     /* Creates/updates rows */
     private void createRows(Map rowstore) {
         logger.info("Updating rowstore cache...");
 
         Iterator i = rowstore.keySet().iterator();
         Map rowDefinition = new HashMap();
         String rowname = "";
 
         while (i.hasNext()) {
             rowDefinition = (Map) rowstore.get((String) i.next());
             rowname = ((String) rowDefinition.get("name")).trim();
 
             // looks if the row with the same name already exists and updates if necessary
             if (this.getRows().containsKey(rowname)) {
 
                 Row storedRow = (Row) this.getRows().get(rowname);
 
                 String path = ((String) rowDefinition.get("path")).trim();
                 String canonicalPath = path;
                 try {
                     canonicalPath = new java.io.File(path).getCanonicalPath();
                 } catch (IOException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
 
                 // the path is modified
                 if (!canonicalPath.equals(storedRow.getRowpath())) {
                     storedRow.setRowpath((String) rowDefinition.get("path"));
                     logger.warn("The path of row " + rowname + " was updated to " + (String) rowDefinition.get("path"));
                 }
 
                 // the description is modified
                 if (!((String) rowDefinition.get("desc")).trim().equals(storedRow.getDescription())) {
                     storedRow.setDescription((String) rowDefinition.get("desc"));
                     logger.warn("The description of row " + rowname + " was updated to "
                             + (String) rowDefinition.get("desc"));
                 }
 
                 // updating the specific row data
                 newRow(storedRow, rowDefinition);
 
                 // this is a new row
             } else {
 
                 // creating Row object and passing the information
                 Row r = new Row();
                 String name = ((String) rowDefinition.get("name")).trim();
                 r.setRowname(name);
                 String path = ((String) rowDefinition.get("path")).trim();
                 String canonicalPath = path;
                 try {
                     canonicalPath = new java.io.File(path).getCanonicalPath();
                 } catch (IOException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 r.setRowpath(canonicalPath);
                 r.setDescription((String) rowDefinition.get("desc"));
 
                 newRow(r, rowDefinition);
 
             }
         }
 
         // removing deleted rows from cache
         Iterator j = this.getRows().keySet().iterator();
         while (j.hasNext()) {
             String key = (String) j.next();
 
             // if the new rowstore definition doesn't contain the row, we trash it
             if (!rowstore.containsKey(key)) {
                 logger.info("Dropping row " + key + " from cache.");
                 this.getRows().remove(key);
             }
         }
     }
 
     public void newRow(Row r, Map rowDefinition) {
         logger.info("Registering new row " + r.getRowname());
 
         r.setParade(this);
         rows.put(r.getRowname(), r);
 
         fileMgr.newRow(r.getRowname(), r, rowDefinition);
         CVSMgr.newRow(r.getRowname(), r, rowDefinition);
         antMgr.newRow(r.getRowname(), r, rowDefinition);
         webappMgr.newRow(r.getRowname(), r, rowDefinition);
         makMgr.newRow(r.getRowname(), r, rowDefinition);
 
     }
 
     /**
      * Initalises the file system monitoring using JNotify ({@link http://jnotify.sourceforge.net/})
      * 
      * FIXME the watches should call much more fine-grained methods than they do now, or this may impact on the
      * performance
      * 
      */
     private void addJNotifyListeners() {
 
         // for each row we create listeners that will inform us whenever a change occurs in the filesystem
         Iterator i = this.getRows().keySet().iterator();
         while (i.hasNext()) {
 
             // let's get the path to where the interesting data is
             Row r = (Row) getRows().get(i.next());
 
             // we're not interested in the ParaDe row
             if (r.getRowpath().equals(getBaseDir()))
                 continue;
 
             //String webappPath = ((RowWebapp) r.getRowdata().get("webapp")).getWebappPath();
             String path = r.getRowpath(); //+ java.io.File.separator + webappPath;
 
             // what kind of changes do we want to watch
             int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED | JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;
 
             // we also want to know what happens in the subdirectories
             boolean watchSubtree = true;
 
             // now we start watching
             // FIXME the updates here should be much more fine-grained
             try {
                 int watchID = JNotify.addWatch(path, mask, watchSubtree, new JNotifyListener() {
                     public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
                         logger.info("JNotifyTest.fileRenamed() : wd #" + wd + " root = " + rootPath + ", "
                         + oldName + " -> " + newName);
                         directoryRefresh(rootPath);
                     }
 
                     public void fileModified(int wd, String rootPath, String name) {
                         logger.info("JNotifyTest.fileModified() : wd #" + wd + " root = " + rootPath + ", " + name);
                         directoryRefresh(rootPath);
                     }
 
                     public void fileDeleted(int wd, String rootPath, String name) {
                         logger.info("JNotifyTest.fileDeleted() : wd #" + wd + " root = " + rootPath + ", "
                         + name);
                         directoryRefresh(rootPath);
                     }
 
                     public void fileCreated(int wd, String rootPath, String name) {
                         logger.info("JNotifyTest.fileCreated() : wd #" + wd + " root = " + rootPath + ", "
                         + name);
                         directoryRefresh(rootPath);
                     }
 
                     private void directoryRefresh(String rootPath) {
                        if(rootPath == null)
                            return;
                        
                         logger.info("Refreshing cache for directory " + rootPath);
                         boolean row_found = false;
                         Row r = null;
 
                         Session s = InitServlet.getSessionFactory().openSession();
                         Transaction tx = s.beginTransaction();
 
                         Parade p = (Parade) s.get(Parade.class, new Long(1));
                         Iterator i = p.getRows().keySet().iterator();
 
                         while (i.hasNext() && !row_found) {
                             r = (Row) p.getRows().get(i.next());
                             row_found = rootPath.startsWith(r.getRowpath());
                         }
 
                         File modifiedDir = (File) r.getFiles().get(rootPath.replace(java.io.File.separatorChar, '/'));
                         modifiedDir.localRefresh();
 
                         tx.commit();
                         s.close();
                         logger.info("Finished refreshing cache for directory " + rootPath);
                     }
                 });
                 logger.debug("Adding filesystem watch to row " + r.getRowname());
             } catch (JNotifyException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 
     /* Model related fields and methods */
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Map getRows() {
         return rows;
     }
 
     public void setRows(Map rows) {
         this.rows = rows;
     }
 
     public Parade() {
 
     }
 
     public String getBaseDir() {
         return baseDir;
     }
 
     public void setBaseDir(String paradeBase) {
         this.baseDir = paradeBase;
     }
 
     public static String constructAbsolutePath(String context, String relativePath) {
         Session s = InitServlet.getSessionFactory().openSession();
         Transaction tx = s.beginTransaction();
         
         Row entryRow = null;
 
         if (context != null) {
             Query q = s.createQuery("from Row r where r.rowname = :context");
             q.setString("context", context);
             entryRow = (Row) q.list().get(0);
         }
 
         String absolutePath = entryRow.getRowpath();
 
         tx.commit();
         s.close();
 
         if (relativePath == null || relativePath == "")
             return absolutePath;
 
         if (relativePath.equals("/"))
             return absolutePath;
 
         if (relativePath.endsWith("/"))
             relativePath = relativePath.substring(0, relativePath.length() - 1);
         absolutePath = entryRow.getRowpath() + java.io.File.separator
                 + relativePath.replace('/', java.io.File.separatorChar);
 
         return absolutePath;
 
     }
 }
