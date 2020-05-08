 package au.edu.uq.cmm.paul.status;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.hibernate.annotations.GenericGenerator;
 
 import au.edu.uq.cmm.aclslib.config.DatafileTemplateConfig;
 import au.edu.uq.cmm.aclslib.config.FacilityConfig;
 import au.edu.uq.cmm.paul.grabber.FileGrabber;
 
 /**
  * The Paul implementation of FacilityConfig persists the configuration data
  * using Hibernate.  It also tracks the login sessions for a facility.
  * 
  * @author scrawley
  */
 @Entity
 @Table(name = "facilities",
        uniqueConstraints={
             @UniqueConstraint(columnNames={"facilityName"}),
             @UniqueConstraint(columnNames={"address"})})
 @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
 public class Facility implements FacilityConfig {
     public enum Status {
         ON, DISABLED, OFF, DUMMY
     }
 
     private List<FacilitySession> sessions = new ArrayList<FacilitySession>();
     private Long id;
     
     private boolean useFullScreen;
     private String driveName;
     private String accessPassword;
     private String accessName;
     private String folderName;
     private String facilityName;
     private String localHostId;
     private boolean useTimer;
     private String facilityDescription;
     private boolean dummy;
     private boolean useFileLocks = true;
     private boolean caseInsensitive;
     private int fileSettlingTime;
     private String address;
     private List<DatafileTemplate> datafileTemplates;
     private Status status = Status.OFF;
     private String message = "";
     private FileGrabber fileGrabber;
     private File localDirectory;
     
 
     public Facility() {
         super();
     }
 
     public Facility(FacilityConfig facilityConfig) {
         useFullScreen = facilityConfig.isUseFullScreen();
         driveName = facilityConfig.getDriveName();
         accessPassword = facilityConfig.getAccessPassword();
         accessName = facilityConfig.getAccessName();
         facilityName = facilityConfig.getFacilityName();
         localHostId = facilityConfig.getLocalHostId();
         folderName = facilityConfig.getFolderName();
         useTimer = facilityConfig.isUseTimer();
         facilityDescription = facilityConfig.getFacilityDescription();
         dummy = facilityConfig.isDummy();
         useFileLocks = facilityConfig.isUseFileLocks();
         fileSettlingTime = facilityConfig.getFileSettlingTime();
         caseInsensitive = facilityConfig.isCaseInsensitive();
         address = facilityConfig.getAddress();
         datafileTemplates = new ArrayList<DatafileTemplate>();
         for (DatafileTemplateConfig template : facilityConfig.getDatafileTemplates()) {
             datafileTemplates.add(new DatafileTemplate(template));
         }
     }
     
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
 
     public String getAccessName() {
         return accessName;
     }
 
     public String getAccessPassword() {
         return accessPassword;
     }
 
     public String getDriveName() {
         return driveName;
     }
 
     public String getFacilityName() {
         return facilityName;
     }
 
     public String getFacilityDescription() {
         return facilityDescription;
     }
 
     public String getFolderName() {
         return folderName;
     }
 
     public boolean isUseFullScreen() {
         return useFullScreen;
     }
 
     @JsonIgnore
     @Transient
     public boolean isUseNetDrive() {
         return driveName != null;
     }
 
     public boolean isUseTimer() {
         return useTimer;
     }
 
     public boolean isDummy() {
         return dummy;
     }
 
     public void setAccessName(String accessName) {
         this.accessName = accessName;
     }
 
     public void setAccessPassword(String accessPassword) {
         this.accessPassword = accessPassword;
     }
 
     public void setDriveName(String driveName) {
         this.driveName = driveName;
     }
 
     public void setFacilityName(String name) {
         this.facilityName = name;
     }
 
     public String getLocalHostId() {
         return localHostId;
     }
 
     public void setLocalHostId(String localHostId) {
         this.localHostId = localHostId;
     }
 
     public void setFacilityDescription(String desc) {
         this.facilityDescription = desc;
     }
 
     public void setFolderName(String folderName) {
         this.folderName = folderName;
     }
 
     public void setUseFullScreen(boolean useFullScreen) {
         this.useFullScreen = useFullScreen;
     }
 
     public void setUseTimer(boolean useTimer) {
         this.useTimer = useTimer;
     }
 
     public void setDummy(boolean dummy) {
         this.dummy = dummy;
     }
 
     public boolean isUseFileLocks() {
         return this.useFileLocks;
     }
 
     public void setUseFileLocks(boolean useFileLocks) {
         this.useFileLocks = useFileLocks;
     }
     
     public int getFileSettlingTime() {
         return this.fileSettlingTime;
     }
 
     public void setFileSettlingTime(int fileSettlingTime) {
         this.fileSettlingTime = fileSettlingTime;
     }
 
     @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
     @JoinColumn(name="datafile_id")
     public List<DatafileTemplate> getDatafileTemplates() {
         return datafileTemplates;
     }
 
     public void setDatafileTemplates(List<DatafileTemplate> templates) {
         this.datafileTemplates = templates;
     }
     
     public Status getStatus() {
         return status;
     }
 
     public void setStatus(Status status) {
         this.status = status;
     }
 
     @JsonIgnore
     @Transient
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     @Id
     @GeneratedValue(generator="increment")
     @GenericGenerator(name="increment", strategy = "increment")
     public Long getId() {
         return id;
     }
     
     public synchronized void addSession(FacilitySession session) {
         sessions.add(session);
     }
 
     @JsonIgnore
     @Transient
     public synchronized FacilitySession getCurrentSession() {
         // FIXME - this needs tuning
         return (sessions.size() == 0) ? null : sessions.get(sessions.size() - 1);
     }
     
     @JsonIgnore
     @Transient
     public synchronized boolean isInUse() {
         // FIXME - this needs tuning
         return sessions.size() > 0 && getCurrentSession().getLogoutTime() == null;
     }
 
     public synchronized FacilitySession getLoginDetails(long timestamp) {
         // FIXME - this needs tuning
         for (int i = sessions.size() - 1; i >= 0; i--) {
             FacilitySession session = sessions.get(i);
             if (session.getLoginTime().getTime() <= timestamp && 
                    (session.getLogoutTime() == null || 
                     session.getLogoutTime().getTime() >= timestamp)) {
                 return session;
             }
         }
         return null;
     }
 
     @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
     @OrderBy(value="id")
     @JoinColumn(name="session_id")
     public List<FacilitySession> getSessions() {
         // FIXME - this needs tuning
         return sessions;
     }
 
     public void setSessions(List<FacilitySession> sessions) {
         this.sessions = sessions;
     }
 
     public boolean isCaseInsensitive() {
         return caseInsensitive;
     }
 
     public void setCaseInsensitive(boolean caseInsensitive) {
         this.caseInsensitive = caseInsensitive;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @JsonIgnore
     @Transient
     public FileGrabber getFileGrabber() {
         return this.fileGrabber;
     }
 
     public void setFileGrabber(FileGrabber fileGrabber) {
         this.fileGrabber = fileGrabber;
     }
 
     @JsonIgnore
     @Transient
     public File getLocalDirectory() {
         return localDirectory;
     }
     
     public void setLocalDirectory(File localDirectory) {
         this.localDirectory = localDirectory;
     }
 }
