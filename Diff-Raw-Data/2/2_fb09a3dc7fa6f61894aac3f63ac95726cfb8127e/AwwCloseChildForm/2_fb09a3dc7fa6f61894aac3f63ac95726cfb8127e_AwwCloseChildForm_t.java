 package org.motechproject.care.reporting.domain.measure;
 
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 import org.motechproject.care.reporting.domain.dimension.ChildCase;
 import org.motechproject.care.reporting.domain.dimension.Flw;
 import org.motechproject.care.reporting.utils.FormToString;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.UniqueConstraint;
 import java.util.Date;
 
 
 @Entity
@Table(name = "aww_close_child_form", uniqueConstraints = @UniqueConstraint(columnNames = {"instance_id","case_id"}))
 public class AwwCloseChildForm extends Form {
 
     private int id;
     private ChildCase childCase;
     private Flw flw;
     private Date timeEnd;
     private Date timeStart;
     private Date dateModified;
     private Date creationTime;
     private String closeChild;
     private String childOverSix;
     private String dupeReg;
     private String died;
     private Date dateDeath;
     private String siteDeath;
     private String diedVillage;
     private String placeDeath;
     private String confirmClose;
     private String yesClosedMessage;
     private String noClosedMessage;
     private String childAlive;
     private String success;
     private String childName;
     private Date dob;
     private String closeChildCase;
 
     public AwwCloseChildForm() {
 
     }
 
     @Id
     @Column(name = "id", unique = true, nullable = false)
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     public int getId() {
         return this.id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "case_id")
     @Cascade({	CascadeType.SAVE_UPDATE, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REPLICATE, CascadeType.LOCK, CascadeType.EVICT })
     public ChildCase getChildCase() {
         return this.childCase;
     }
 
     public void setChildCase(ChildCase childCase) {
         this.childCase = childCase;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id")
     @Cascade({	CascadeType.SAVE_UPDATE, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REPLICATE, CascadeType.LOCK, CascadeType.EVICT })
     public Flw getFlw() {
         return this.flw;
     }
 
     public void setFlw(Flw flw) {
         this.flw = flw;
     }
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "time_end")
     public Date getTimeEnd() {
         return this.timeEnd;
     }
 
     public void setTimeEnd(Date timeEnd) {
         this.timeEnd = timeEnd;
     }
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "time_start")
     public Date getTimeStart() {
         return this.timeStart;
     }
 
     public void setTimeStart(Date timeStart) {
         this.timeStart = timeStart;
     }
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "date_modified")
     public Date getDateModified() {
         return this.dateModified;
     }
 
     public void setDateModified(Date dateModified) {
         this.dateModified = dateModified;
     }
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "creation_time")
     public Date getCreationTime() {
         return creationTime;
     }
 
     public void setCreationTime(Date creationTime) {
         this.creationTime = creationTime;
     }
 
     @Column(name = "close_child")
     public String getCloseChild() {
         return closeChild;
     }
 
     public void setCloseChild(String closeChild) {
         this.closeChild = closeChild;
     }
 
     @Column(name = "child_over_six")
     public String getChildOverSix() {
         return childOverSix;
     }
 
     public void setChildOverSix(String childOverSix) {
         this.childOverSix = childOverSix;
     }
 
     @Column(name = "dupe_reg")
     public String getDupeReg() {
         return dupeReg;
     }
 
     public void setDupeReg(String dupeReg) {
         this.dupeReg = dupeReg;
     }
 
     @Column(name = "died")
     public String getDied() {
         return died;
     }
 
     public void setDied(String died) {
         this.died = died;
     }
 
     @Temporal(value = TemporalType.TIMESTAMP)
     @Column(name = "date_death")
     public Date getDateDeath() {
         return dateDeath;
     }
 
     public void setDateDeath(Date dateDeath) {
         this.dateDeath = dateDeath;
     }
 
     @Column(name = "site_death")
     public String getSiteDeath() {
         return siteDeath;
     }
 
     public void setSiteDeath(String siteDeath) {
         this.siteDeath = siteDeath;
     }
 
     @Column(name = "died_village")
     public String getDiedVillage() {
         return diedVillage;
     }
 
     public void setDiedVillage(String diedVillage) {
         this.diedVillage = diedVillage;
     }
 
     @Column(name = "place_death")
     public String getPlaceDeath() {
         return placeDeath;
     }
 
     public void setPlaceDeath(String placeDeath) {
         this.placeDeath = placeDeath;
     }
 
     @Column(name = "confirm_close")
     public String getConfirmClose() {
         return confirmClose;
     }
 
     public void setConfirmClose(String confirmClose) {
         this.confirmClose = confirmClose;
     }
 
     @Column(name = "yes_closed_message")
     public String getYesClosedMessage() {
         return yesClosedMessage;
     }
 
     public void setYesClosedMessage(String yesClosedMessage) {
         this.yesClosedMessage = yesClosedMessage;
     }
 
     @Column(name = "no_closed_message")
     public String getNoClosedMessage() {
         return noClosedMessage;
     }
 
     public void setNoClosedMessage(String noClosedMessage) {
         this.noClosedMessage = noClosedMessage;
     }
 
     @Column(name = "child_alive")
     public String getChildAlive() {
         return childAlive;
     }
 
     public void setChildAlive(String childAlive) {
         this.childAlive = childAlive;
     }
 
     @Column(name = "success")
     public String getSuccess() {
         return success;
     }
 
     public void setSuccess(String success) {
         this.success = success;
     }
 
     @Column(name = "child_name")
     public String getChildName() {
         return childName;
     }
 
     public void setChildName(String childName) {
         this.childName = childName;
     }
 
     @Temporal(value = TemporalType.DATE)
     @Column(name = "dob")
     public Date getDob() {
         return dob;
     }
 
     public void setDob(Date dob) {
         this.dob = dob;
     }
 
     @Column(name = "close_child_case")
     public String getCloseChildCase() {
         return closeChildCase;
     }
 
     public void setCloseChildCase(String closeChildCase) {
         this.closeChildCase = closeChildCase;
     }
 }
