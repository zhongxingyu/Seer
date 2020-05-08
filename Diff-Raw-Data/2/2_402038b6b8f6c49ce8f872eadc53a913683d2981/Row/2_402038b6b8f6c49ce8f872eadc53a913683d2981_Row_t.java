 package org.makumba.parade.model;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Row {
 
     private Long id;
 
     private String rowname;
 
     private String rowpath;
 
     private String description;
 
     private Map files = new HashMap();
 
     private Map rowdata = new HashMap();
 
     private Parade parade;
 
     public void addManagerData(RowData data) {
 
         data.setRow(this);
         getRowdata().put(data.getDataType(), data);
     }
     
     public static Row getRow(Parade p, String context) {
         
         Row r = (Row) p.getRows().get(context);
         if (r == null)
             return null;
         else
             return r;
     }
     
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
     
     public Map getFiles() {
         return this.files;
     }
 
     public void setFiles(Map files) {
         this.files = files;
     }
 
     public String getRowname() {
         return rowname;
     }
 
     public void setRowname(String rowname) {
         this.rowname = rowname;
     }
 
     public String getRowpath() {
         return rowpath.replace('/', java.io.File.separatorChar);
     }
 
     public void setRowpath(String rowpath) {
         this.rowpath = rowpath.replace(java.io.File.separatorChar, '/');
     }
 
     public Parade getParade() {
         return parade;
     }
 
     public void setParade(Parade parade) {
         this.parade = parade;
     }
 
     public Map getRowdata() {
         return rowdata;
     }
 
     public void setRowdata(Map rowdata) {
         this.rowdata = rowdata;
     }
 
 }
