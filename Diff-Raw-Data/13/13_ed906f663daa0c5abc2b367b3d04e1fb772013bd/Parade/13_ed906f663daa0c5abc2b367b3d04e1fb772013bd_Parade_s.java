 package org.makumba.parade.model;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import net.contentobjects.jnotify.JNotify;
 import net.contentobjects.jnotify.JNotifyException;
 
 import org.apache.log4j.Logger;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.init.ParadeProperties;
 import org.makumba.parade.init.RowProperties;
 import org.makumba.parade.model.managers.AntManager;
 import org.makumba.parade.model.managers.ApplicationManager;
 import org.makumba.parade.model.managers.CVSManager;
 import org.makumba.parade.model.managers.FileManager;
 import org.makumba.parade.model.managers.MakumbaManager;
 import org.makumba.parade.model.managers.WebappManager;
 import org.makumba.parade.tools.ParadeException;
 import org.makumba.parade.tools.ParadeJNotifyListener;
 
 /**
  * This class holds the methods for handling general ParaDe operations. It also is the starting point of the ParaDe
  * cache model, and only one instance exists within ParaDe (which is loaded on startup).<br>
  * 
  * @author Manuel Gay
  * 
  */
 public class Parade {
 
     private static Logger logger = Logger.getLogger(Parade.class.getName());
 
     private Long id;
 
     private String baseDir = new String();
 
     private Map<String, Row> rows = new HashMap<String, Row>();
 
     private Map<String, Application> applications = new HashMap<String, Application>();
 
     private Map<String, User> users = new HashMap<String, User>();
 
     private RowProperties rowproperties = new RowProperties();
 
     private Map<String, Integer> JNotifyWatches = new HashMap<String, Integer>();
 
     // ParaDe managers
 
     public FileManager fileMgr = new FileManager();
 
     public CVSManager CVSMgr = new CVSManager();
 
     public AntManager antMgr = new AntManager();
 
     public WebappManager webappMgr = new WebappManager();
 
     public MakumbaManager makMgr = new MakumbaManager();
 
     /**
      * Performs the overall ParaDe cache refresh. This is called on startup, when the cache is empty. It should
      * theoretically also be possible to do it without empty database cache, but it looks like Hibernate doesn't handle
      * this very well.<br>
      * The refresh happens in 3 steps:
      * <ol>
      * <li>application reading and configuration</li>
      * <li>row creation for the new / to be updated rows</li>
      * <li>row refresh for each row</li>
      * <li>JNotify trigger creation for each row</li>
      * </ol>
      * 
      */
     public void refresh() {
         logger.info("Starting ParaDe-wide refresh...");
 
         this.baseDir = ParadeProperties.getParadeBase();
 
         // we read the configured applications in parade.properties and ask the ApplicationManager to handle their
         // creation
         ApplicationManager applMgr = new ApplicationManager();
         applMgr.paradeRefresh(this);
 
         // we read the row definitions and perform update/creation
         Map<String, Map<String, String>> rowstore = rowproperties.getRowDefinitions();
         if (rowstore.isEmpty()) {
             logger.warn("No row definitions found, check RowProperties");
         }
 
         createOrUpdateRows(rowstore);
 
         Iterator<String> i = rows.keySet().iterator();
         while (i.hasNext()) {
 
             Row r = (Row) rows.get(i.next());
             refreshRow(r);
         }
 
         logger.info("ParaDe-wide refresh finished");
 
     }
 
     /**
      * Creates / updates row
      * 
      * @param rowstore
      *            the Map containing all the row store information
      */
     private void createOrUpdateRows(Map<String, Map<String, String>> rowstore) {
         logger.info("Updating rowstore cache...");
 
         Iterator<String> i = rowstore.keySet().iterator();
         Map<String, String> rowDefinition = new HashMap<String, String>();
 
         while (i.hasNext()) {
             rowDefinition = rowstore.get(i.next());
             buildRow(rowDefinition);
         }
 
         // removing deleted rows from cache
         Iterator<String> j = this.getRows().keySet().iterator();
         while (j.hasNext()) {
             String key = j.next();
 
             // if the new row store definition doesn't contain the row, we trash it
             if (!rowstore.containsKey(key)) {
                 logger.info("Dropping row " + key + " from cache.");
                 this.getRows().remove(key);
             }
         }
     }
 
     /**
      * Builds a row from its parsed configuration data
      * 
      * @param rowDefinition
      *            the Map containing the row definition
      * @return a new Row object
      */
     private Row buildRow(Map<String, String> rowDefinition) {
 
         String rowname;
         rowname = rowDefinition.get("name").trim();
 
         // looks if the row with the same name already exists and updates if necessary
         if (this.getRows().containsKey(rowname)) {
 
             return updateRow(rowDefinition, rowname);
         } else {
 
             return createRow(rowDefinition);
         }
     }
 
     /**
      * Creates a new Row object from its configuration data and adds it to the ParaDe rows map.
      * 
      * @param rowDefinition
      *            the Map containing the configuration data of the row
      * @return a new populated Row object
      */
     private Row createRow(Map<String, String> rowDefinition) {
         Row r = new Row();
         String name = (rowDefinition.get("name")).trim();
         r.setRowname(name);
         logger.info("Registering row " + r.getRowname());
         String path = (rowDefinition.get("path")).trim();
         String canonicalPath = path;
         try {
             canonicalPath = new java.io.File(path).getCanonicalPath();
         } catch (IOException e) {
             logger.error("Could not get the canonical path for row " + name + " with path " + path);
         }
         r.setRowpath(canonicalPath);
         r.setDescription(rowDefinition.get("desc"));
         String appName = rowDefinition.get("application");
         if (r.getRowpath().equals(this.baseDir))
             appName = "parade";
         
         if(appName.length() == 0) {
             appName = ParadeProperties.getProperty("parade.applications.default");
         }
         
         Application a = this.getApplications().get(appName);
         if (a == null) {
             logger.error("Application " + appName + " of row " + r.getRowname()
                     + " is not configured. Check your applications.properties file.");
         }
         r.setApplication(a);
 
         return registerRow(r, rowDefinition);
     }
 
     /**
      * Updates the cache of an already existing row
      * 
      * @param rowDefinition
      *            the new row configuration data
      * @param rowname
      *            the name of the row to update
      * @return an updated Row object
      */
     private Row updateRow(Map<String, String> rowDefinition, String rowname) {
 
         Row storedRow = this.getRows().get(rowname);
 
         String path = rowDefinition.get("path").trim();
         String canonicalPath = path;
         try {
             canonicalPath = new java.io.File(path).getCanonicalPath();
         } catch (IOException e) {
             logger.error("Could not get the canonical path for row " + rowname + " with path " + path);
         }
 
         // the path is modified
         if (!canonicalPath.equals(storedRow.getRowpath())) {
             storedRow.setRowpath(rowDefinition.get("path"));
             logger.warn("The path of row " + rowname + " was updated to " + rowDefinition.get("path"));
         }
 
         // the description is modified
         if (!rowDefinition.get("desc").trim().equals(storedRow.getDescription())) {
             storedRow.setDescription(rowDefinition.get("desc"));
             logger.warn("The description of row " + rowname + " was updated to " + rowDefinition.get("desc"));
         }
 
         // the application is modified
         String application = rowDefinition.get("application").trim();
         if (storedRow.getApplication() != null && !application.equals(storedRow.getApplication().getName())) {
             
             String appName = rowDefinition.get("application");
             
             if(appName.length() == 0) {
                 appName = ParadeProperties.getProperty("parade.applications.default");
             }
 
             
             Application a = this.getApplications().get(appName);
             if (a == null) {
                 logger.error("Application " + application + " is not configured. Check your parade.properties file.");
             }
             storedRow.setApplication(a);
             logger.warn("The application of row " + rowname + " was updated to " + application);
         }
 
         // updating the specific row data
         return registerRow(storedRow, rowDefinition);
     }
 
     /**
      * Registers a row, i.e. calls the newRow() method for all the row managers and adds it to ParaDe's row Map
      * 
      * @param r
      *            the row object
      * @param rowDefinition
      *            the rowDefinition of the row
      * @return the registered Row object
      */
     public Row registerRow(Row r, Map<String, String> rowDefinition) {
         r.setParade(this);
         rows.put(r.getRowname(), r);
 
         fileMgr.newRow(r.getRowname(), r, rowDefinition);
         CVSMgr.newRow(r.getRowname(), r, rowDefinition);
         antMgr.newRow(r.getRowname(), r, rowDefinition);
         webappMgr.newRow(r.getRowname(), r, rowDefinition);
         makMgr.newRow(r.getRowname(), r, rowDefinition);
 
         return r;
     }
 
     /**
      * Refreshes a row, i.e. calls the refreshRow() method for all the row managers
      * 
      * @param r
      *            the Row to refresh
      */
     public void refreshRow(Row r) {
         fileMgr.rowRefresh(r);
         CVSMgr.rowRefresh(r);
         antMgr.rowRefresh(r);
         webappMgr.rowRefresh(r);
         makMgr.rowRefresh(r);
     }
 
     /**
      * Rebuilds the cache of a specific row
      * 
      * TODO this should also unregister and re-register the JNotify listener, in case files changed
      * 
      * @param r
      *            the Row to rebuild
      */
     public void rebuildRowCache(Row r) {
 
         long start = new Date().getTime();
         logger.info("Starting to rebuild cache of row " + r.getRowname());
 
         String rowName = r.getRowname();
 
         // first we trash the existing row
         logger.info("Emptying cache of row " + r.getRowname());
         // FIXME this doesn't work!
         this.getRows().remove(r);
 
         // then we read the row information from the rowstore
         Map<String, String> rowDefinition = rowproperties.getRowDefinitions().get(rowName);
 
         if (rowDefinition == null) {
             // the row was removed
             logger.warn("Row " + rowName + " was removed from the rowstore.");
 
         } else {
 
             // then we build it again
             Row r1 = buildRow(rowDefinition);
 
             // we refresh it
             logger.info("Populating cache of row " + r.getRowname());
             refreshRow(r1);
 
             // finally, we re-register the JNotify listener
             try {
                 JNotify.removeWatch(JNotifyWatches.get(r.getRowname()));
                 addJNotifyListener(r);
             } catch (JNotifyException jne) {
                 logger.error("Exception while trying to refresh JNotify listener of row " + r.getRowname());
                 jne.printStackTrace();
             }
 
             long end = new Date().getTime();
 
             logger.info("Finished rebuilding cache of row " + rowName + ". Operation took " + (end - start) + " ms");
 
         }
     }
 
     /**
      * Reads the rows.properties file and caches not registered rows on the fly.
      */
     public void createNewRows() {
 
         Map<String, Map<String, String>> rowstore = rowproperties.getRowDefinitions();
         if (rowstore.isEmpty()) {
             logger.warn("No row definitions found, check RowProperties");
         }
 
         Iterator<String> i = rowstore.keySet().iterator();
         while (i.hasNext()) {
             String name = i.next();
             if (rows.get(rowstore.get(name).get("name").trim()) == null) {
                 // let's make the new row
                 Row r = buildRow(rowstore.get(name));
 
                 logger.info("Populating cache of row " + r.getRowname());
                 refreshRow(r);
                 addJNotifyListener(r);
             }
         }
     }
 
     /**
      * Initialises the file system monitoring using JNotify ({@link http://jnotify.sourceforge.net/})
      */
     public void addJNotifyListeners() {
 
         // for each row we create listeners that will inform us whenever a change occurs in the filesystem
         Iterator<String> i = this.getRows().keySet().iterator();
         while (i.hasNext()) {
 
             // let's get the path to where the interesting data is
             Row r = getRows().get(i.next());
 
             // we're not interested in the ParaDe row
             if (r.getRowpath().equals(getBaseDir()))
                 continue;
 
             addJNotifyListener(r);
         }
     }
 
     /**
      * Adds a JNotify listener to row r
      * 
      * @param r
      *            the row to which to add a JNotify listener
      */
     private void addJNotifyListener(Row r) {
 
         String path = r.getRowpath();
 
         // what kind of changes do we want to watch
         int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED | JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;
 
         // we also want to know what happens in the subdirectories
         boolean watchSubtree = true;
 
        int watchID = 0;
 
         // now we start watching
         try {
             logger.info("Adding filesystem watch to row " + r.getRowname());
             watchID = JNotify.addWatch(path, mask, watchSubtree, new ParadeJNotifyListener());
            JNotifyWatches.put(r.getRowname(), watchID);
            if (watchID == 0) {
                 throw new ParadeException("Row " + r.getRowname()
                         + " not properly watched by JNotify! Are you having two rows that use the same directory?");
             }
 
         } catch (JNotifyException e) {
             e.printStackTrace();
         } catch (NullPointerException npe) {
             // do nothing. JNotify returns plenty of those.
         }
     }
 
     /**
      * Builds the absolute path of a file, based on its context
      * 
      * @param context
      *            the name of the ParaDe context
      * @param relativePath
      *            the relative path to the file within the context
      * @return the absolute path of the file on the file system
      */
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
 
     /** Model related fields and methods * */
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Map<String, Row> getRows() {
         return rows;
     }
 
     public void setRows(Map<String, Row> rows) {
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
 
     public Map<String, Application> getApplications() {
         return applications;
     }
 
     public void setApplications(Map<String, Application> applications) {
         this.applications = applications;
     }
 
     public Map<String, User> getUsers() {
         return users;
     }
 
     public void setUsers(Map<String, User> users) {
         this.users = users;
     }
 
     public Map<String, Integer> getJNotifyWatches() {
         return JNotifyWatches;
     }
 
     public void setJNotifyWatches(Map<String, Integer> notifyWatches) {
         JNotifyWatches = notifyWatches;
     }
 }
