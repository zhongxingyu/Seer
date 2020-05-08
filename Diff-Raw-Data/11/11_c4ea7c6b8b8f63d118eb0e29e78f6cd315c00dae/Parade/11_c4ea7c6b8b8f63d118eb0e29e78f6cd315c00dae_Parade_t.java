 package org.makumba.parade.model;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
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
      * 1. Calls create row for the new/to be updated rows 2. Calls for each row: - rowRefresh() - directoryRefresh()
      */
     public void refresh() {
 
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
     }
 
     /* Creates/updates rows */
     private void createRows(Map rowstore) {
 
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
                 if (!canonicalPath.equals(storedRow.getRowpath())) {
                     storedRow.setRowpath((String) rowDefinition.get("path"));
                     logger.warn("The path of row " + rowname + " was updated to " + (String) rowDefinition.get("path"));
                 }
                 if (!((String) rowDefinition.get("desc")).trim().equals(storedRow.getDescription())) {
                     storedRow.setDescription((String) rowDefinition.get("desc"));
                     logger.warn("The description of row " + rowname + " was updated to "
                             + (String) rowDefinition.get("desc"));
                 }
 
                 newRow(storedRow, rowDefinition);
 
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
     }
 
     public void newRow(Row r, Map rowDefinition) {
         r.setParade(this);
         rows.put(r.getRowname(), r);
 
         fileMgr.newRow(r.getRowname(), r, rowDefinition);
         CVSMgr.newRow(r.getRowname(), r, rowDefinition);
         antMgr.newRow(r.getRowname(), r, rowDefinition);
         webappMgr.newRow(r.getRowname(), r, rowDefinition);
         makMgr.newRow(r.getRowname(), r, rowDefinition);
 
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
 
 }
