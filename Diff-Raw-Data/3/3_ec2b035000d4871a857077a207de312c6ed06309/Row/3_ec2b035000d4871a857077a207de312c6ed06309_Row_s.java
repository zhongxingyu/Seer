 package org.makumba.parade.model;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.MapKeyColumn;
 import javax.persistence.OneToMany;
 import javax.persistence.Transient;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.Index;
 import org.makumba.parade.aether.ObjectTypes;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.init.ParadeProperties;
 
 /**
  * Representation of a ParaDe row, with loads of data.
  * 
  * @author Manuel Gay
  * 
  */
 @Entity
 @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
 public class Row {
 
     public static final int AUTO_CVS_UPDATE_DISABLED = 10;
 
     public static final int AUTO_CVS_UPDATE_ENABLED = 20;
 
     public static String getModule(String fileURL) {
         // fetch cvs module of row
         Session s = null;
         String module = "";
         try {
             s = InitServlet.getSessionFactory().openSession();
             Transaction tx = s.beginTransaction();
 
             module = (String) s.createQuery("select module from Row where rowname = :context").setString("context",
                     ObjectTypes.rowNameFromURL(fileURL)).uniqueResult();
 
             tx.commit();
         } finally {
             if (s != null)
                 s.close();
         }
         return module;
     }
 
     private Application application;
 
     // 10 = No, 20 = Yes
     private int automaticCvsUpdate = AUTO_CVS_UPDATE_DISABLED;
 
     private String branch;
 
     private String buildfile = "";
 
     private String contextname;
 
     private String cvsuser;
 
     private String db;
 
     private String description;
 
     private User externalUser;
 
     private Map<String, File> files = new HashMap<String, File>();
 
     private boolean hasMakumba;
 
     private Long id;
 
     private String module;
 
     private boolean moduleRow = false;
 
     private Parade parade;
 
     private String rowname;
 
     private String rowpath;
 
     private Integer status;
 
     private List<AntTarget> targets = new LinkedList<AntTarget>();
 
     private User user;
 
     private String version;
 
     private boolean watchedByJNotify = false;
 
     private String webappPath;
     
     /**
      * @author Joao Andrade
      * @param path
      * @return
      */
     public Boolean isInside(String path) {
         String rowPath = getRowpath();
 
         Integer pathLength = 0;
         Integer rowPathLength = 0;
         try {
             pathLength = new java.io.File(path).getCanonicalPath().length();
             rowPathLength = new java.io.File(rowPath).getCanonicalPath().length();
         } catch (IOException e) {
             e.printStackTrace();
         }
         // FIXME Joao - should do the check in a better way
        return pathLength >= rowPathLength;
     }
 
     @Transient
     public List<String> getAllowedOperations() {
         List<String> allowedTargets = new LinkedList<String>();
 
         for (String allowed : ParadeProperties.getElements("ant.displayedOps")) {
             for (AntTarget target : getTargets()) {
                 if (target.getTarget().startsWith("#"))
                     target.setTarget(target.getTarget().substring(1));
                 if (target.getTarget().equals(allowed)) {
                     allowedTargets.add(target.getTarget());
                 }
             }
         }
         return allowedTargets;
     }
 
     @ManyToOne
     public Application getApplication() {
         return application;
     }
 
     @Column(insertable = false, columnDefinition = "int(11) default 10")
     public int getAutomaticCvsUpdate() {
         return automaticCvsUpdate;
     }
 
     @Column
     public String getBranch() {
         return branch;
     }
 
     @Column
     public String getBuildfile() {
         return buildfile;
     }
 
     @Column
     public String getContextname() {
         return contextname;
     }
 
     @Column
     public String getCvsuser() {
         return cvsuser;
     }
 
     @Column
     public String getDb() {
         return db;
     }
 
     @Column
     public String getDescription() {
         return description;
     }
 
     @ManyToOne
     @JoinColumn(name = "externalUser")
     public User getExternalUser() {
         return externalUser;
     }
 
     @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = org.makumba.parade.model.File.class)
     @JoinColumn(name = "row")
     @MapKeyColumn(name = "path", columnDefinition = "longtext")
     public Map<String, File> getFiles() {
         return this.files;
     }
 
     @Column
     public boolean getHasMakumba() {
         return hasMakumba;
     }
 
     @Id
     @GeneratedValue
     @Column(name = "row")
     public Long getId() {
         return id;
     }
 
     @Column
     public String getModule() {
         return module;
     }
 
     @Column
     public boolean getModuleRow() {
         return moduleRow;
     }
 
     @ManyToOne
     @JoinColumn(name = "id_parade", insertable = false, updatable = false)
     public Parade getParade() {
         return parade;
     }
 
     @Column
     @Index(name = "IDX_ROWNAME")
     public String getRowname() {
         return rowname;
     }
 
     @Column
     public String getRowpath() {
         return rowpath.replace('/', java.io.File.separatorChar);
     }
 
     @Column
     public Integer getStatus() {
         return status;
     }
 
     @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = org.makumba.parade.model.AntTarget.class)
     @MapKeyColumn(name = "row_id")
     public List<AntTarget> getTargets() {
         return targets;
     }
 
     @ManyToOne
     @JoinColumn(name = "user")
     public User getUser() {
         return user;
     }
 
     @Column
     public String getVersion() {
         return version;
     }
 
     @Column(insertable = false, columnDefinition = "bit(1) default '\0'")
     public boolean getWatchedByJNotify() {
         return this.watchedByJNotify;
     }
 
     @Column
     public String getWebappPath() {
         return webappPath;
     }
 
     public void setApplication(Application application) {
         this.application = application;
     }
 
     public void setAutomaticCvsUpdate(int automaticCvsUpdate) {
         this.automaticCvsUpdate = automaticCvsUpdate;
     }
 
     public void setBranch(String branch) {
         this.branch = branch;
     }
 
     public void setBuildfile(String buildfile) {
         this.buildfile = buildfile;
     }
 
     public void setContextname(String name) {
         this.contextname = name;
     }
 
     public void setCvsuser(String cvsuser) {
         this.cvsuser = cvsuser;
     }
 
     public void setDb(String database) {
         this.db = database;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public void setExternalUser(User externalUser) {
         this.externalUser = externalUser;
     }
 
     public void setFiles(Map<String, File> files) {
         this.files = files;
     }
 
     public void setHasMakumba(boolean hasMakumba) {
         this.hasMakumba = hasMakumba;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public void setModule(String module) {
         this.module = module;
     }
 
     public void setModuleRow(boolean moduleRow) {
         this.moduleRow = moduleRow;
     }
 
     public void setParade(Parade parade) {
         this.parade = parade;
     }
 
     public void setRowname(String rowname) {
         this.rowname = rowname;
     }
 
     public void setRowpath(String rowpath) {
         this.rowpath = rowpath.replace(java.io.File.separatorChar, '/');
     }
 
     public void setStatus(Integer status) {
         this.status = status;
     }
 
     public void setTargets(List<AntTarget> targets) {
         this.targets = targets;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     public void setVersion(String version) {
         this.version = version;
     }
 
     public void setWatchedByJNotify(boolean watchedByJNotify) {
         this.watchedByJNotify = watchedByJNotify;
     }
 
     public void setWebappPath(String webappPath) {
         this.webappPath = webappPath;
     }
 
     @Override
     public String toString() {
         return getRowname() + " - " + getDescription();
     }
 }
