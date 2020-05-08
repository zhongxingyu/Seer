 package org.makumba.parade.model;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Transient;
 
 import org.hibernate.Hibernate;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.annotations.Index;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.model.managers.CVSManager;
 import org.makumba.parade.model.managers.FileManager;
 
 @Entity
 public class File {
 
     private Long id;
 
     private boolean isDir;
 
     private boolean onDisk;
 
     private String name;
 
     private Long date;
 
     private Long size;
 
     private Integer currentChars;
 
     private Integer previousChars;
 
     private Row row;
 
     private String path;
 
     private String parentPath;
 
     private String cvsURL;
 
     private Integer cvsStatus;
 
     private String cvsRevision;
 
     private String cvsCheckedOutRevision;
 
     private Date cvsDate;
 
     private Long crawled;
 
     /* Calls the refresh() directoryRefresh() on the directory managers */
     public void refresh() {
        FileManager fileMgr = new FileManager();
        CVSManager CVSMgr = new CVSManager();

         fileMgr.directoryRefresh(row, this.getPath(), false);
         CVSMgr.directoryRefresh(row, this.getPath(), false);
     }
 
     public void setPath(String p) {
         this.path = p;
     }
 
    @ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
     public Row getRow() {
         return row;
     }
 
     public void setRow(Row row) {
         this.row = row;
     }
 
    @Id
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Transient
     public Long getAge() {
         return new Long(new Date().getTime() - this.date.longValue());
     }
     
 
     @Column
     public Long getDate() {
         return date;
     }
 
     public void setDate(Long date) {
         this.date = date;
     }
     
     @Column
     public Long getSize() {
         return size;
     }
 
     public void setSize(Long size) {
         this.size = size;
     }
 
     @Column
     @Index(name="nameIndex")
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @Column
     public boolean getIsDir() {
         return isDir;
     }
 
     public void setIsDir(boolean isDir) {
         this.isDir = isDir;
     }
 
     @Column
     public String getPath() {
         return this.path;
     }
 
     @Column
     public String getParentPath() {
         return parentPath;
     }
 
     public void setParentPath(String parentPath) {
         this.parentPath = parentPath;
     }
 
     @Column
     public boolean getOnDisk() {
         return onDisk;
     }
 
     public void setOnDisk(boolean onDisk) {
         this.onDisk = onDisk;
     }
 
     @Column
     public String getCvsURL() {
         return cvsURL;
     }
 
     public void setCvsURL(String cvsURL) {
         this.cvsURL = cvsURL;
     }
 
     @Column
     public Integer getCvsStatus() {
         return cvsStatus;
     }
 
     public void setCvsStatus(Integer cvsStatus) {
         this.cvsStatus = cvsStatus;
     }
 
     @Column
     public String getCvsRevision() {
         return cvsRevision;
     }
 
     public void setCvsRevision(String cvsRevision) {
         this.cvsRevision = cvsRevision;
     }
 
     @Column
     public String getCvsCheckedOutRevision() {
         return cvsCheckedOutRevision;
     }
 
     public void setCvsCheckedOutRevision(String cvsCheckedOutRevision) {
         this.cvsCheckedOutRevision = cvsCheckedOutRevision;
     }
 
     @Column
     public Date getCvsDate() {
         return cvsDate;
     }
 
     public void setCvsDate(Date cvsDate) {
         this.cvsDate = cvsDate;
     }
 
     @Column
     public Long getCrawled() {
         return crawled;
     }
 
     public void setCrawled(Long crawled) {
         this.crawled = crawled;
     }
 
     @Transient
     public String getFileURI() {
 
         if (path.startsWith(row.getRowpath() + "/" + row.getWebappPath())) {
             return "file://"
                     + row.getRowname()
                     + path.substring((row.getRowpath() + "/" + row.getWebappPath()).length()).replace(
                             java.io.File.separator, "/");
         } else if (path.startsWith(row.getRowpath())) {
             return "file://" + row.getRowname()
                     + path.substring((row.getRowpath()).length()).replace(java.io.File.separator, "/");
         }
 
         return "file://"
                 + row.getRowname()
                 + path.substring((row.getRowpath() + "/" + row.getWebappPath()).length()).replace(
                         java.io.File.separator, "/");
     }
 
     @Transient
     public String getCvsPath() {
         return row.getApplication().getName() + path.substring(row.getRowpath().length());
     }
 
     public void emptyCvsData() {
         setCvsDate(null);
         setCvsRevision(null);
         setCvsCheckedOutRevision(null);
         setCvsStatus(null);
         setCvsURL(null);
     }
 
     /* returns a List of the direct children (files, dirs) of a given Path */
     @Transient
     public List<File> getChildren(String orderBy) {
 
         if (orderBy == null) {
             orderBy = new String("f.isDir desc, f.name asc");
         } else if (orderBy.equals("name")) {
             orderBy = "f.isDir desc, f.name asc";
         } else if (orderBy.equals("age")) {
             orderBy = "f.isDir desc, f.date asc";
         } else if (orderBy.equals("size")) {
             orderBy = "f.isDir desc, f.size asc";
         }
 
         String keyPath = this.getPath().replace(java.io.File.separatorChar, '/');
 
         String absoulteRowPath = (new java.io.File(row.getRowpath()).getAbsolutePath());
         if (keyPath == null || keyPath == "")
             keyPath = absoulteRowPath.replace(java.io.File.separatorChar, '/');
 
         List<File> children = null;
 
         Session s = InitServlet.getSessionFactory().openSession();
         Transaction tx = s.beginTransaction();
 
         Query q = s.createQuery("from File f where f.parentPath = :keyPath and f.row.rowname = :rowname order by "
                 + orderBy);
         q.setCacheable(true);
         q.setString("keyPath", keyPath);
         q.setString("rowname", row.getRowname());
 
         children = q.list();
 
         // we need to initialise the file data of this file
         Iterator<File> i = q.iterate();
         while (i.hasNext()) {
             File f = i.next();
             if (f.getRow().getApplication() != null) {
                 Hibernate.initialize(f.getRow().getApplication().getCvsfiles());
             }
         }
 
         tx.commit();
         s.close();
 
         return children;
     }
 
     /* returns a List of the direct children (files, dirs) of a given Path */
     @Transient
     public List<String> getChildrenPaths() {
         String keyPath = this.getPath().replace(java.io.File.separatorChar, '/');
 
         String absoulteRowPath = (new java.io.File(row.getRowpath()).getAbsolutePath());
         if (keyPath == null || keyPath == "")
             keyPath = absoulteRowPath.replace(java.io.File.separatorChar, '/');
 
         List<String> children = null;
 
         Session s = InitServlet.getSessionFactory().openSession();
         Transaction tx = s.beginTransaction();
 
         Query q = s
                 .createSQLQuery(
                         "SELECT path FROM File f JOIN Row r WHERE f.row = r.row AND f.parentPath = ? AND r.rowname = ? ORDER BY f.isDir DESC, f.path ASC")
                 .addScalar("path", Hibernate.STRING);
         q.setCacheable(true);
         q.setString(0, keyPath);
         q.setString(1, row.getRowname());
 
         children = q.list();
 
         tx.commit();
         s.close();
 
         return children;
     }
 
     @Column
     public Integer getCurrentChars() {
         return currentChars;
     }
 
     public void setCurrentChars(Integer chars) {
         this.currentChars = chars;
     }
 
     @Column
     public Integer getPreviousChars() {
         return previousChars;
     }
 
     public void setPreviousChars(Integer previousChars) {
         this.previousChars = previousChars;
     }
 
     @Override
     public String toString() {
         return this.path + "(" + this.row.getRowname() + "), " + (onDisk ? "on disk" : "virtual") + ", cvs revision"
                 + this.cvsRevision + " cvs status " + this.cvsStatus;
     }
 
 }
