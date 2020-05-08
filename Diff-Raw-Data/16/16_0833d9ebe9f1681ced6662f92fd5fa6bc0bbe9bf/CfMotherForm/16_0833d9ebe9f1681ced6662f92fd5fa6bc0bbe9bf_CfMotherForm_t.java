 package org.motechproject.care.reporting.domain.measure;
 
 import org.hibernate.annotations.*;
 import org.hibernate.annotations.CascadeType;
 import org.motechproject.care.reporting.domain.dimension.Flw;
 import org.motechproject.care.reporting.domain.dimension.MotherCase;
 import org.motechproject.care.reporting.utils.FormToString;
 
 import javax.persistence.*;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import java.util.Date;
 
 
 @Entity
 @Table(name = "cf_mother_form", uniqueConstraints = @UniqueConstraint(columnNames = "instance_id"))
 public class CfMotherForm extends Form {
 
 	private int id;
 	private Flw flw;
 	private MotherCase motherCase;
 	private Date timeEnd;
 	private Date timeStart;
 	private Date dateModified;
 	private Date dateCf1;
 	private Date dateCf2;
 	private Date dateCf3;
 	private Date dateCf4;
 	private Date dateCf5;
 	private Date dateCf6;
 	private Date dateLastVisit;
 	private Date dateNextCf;
 	private String lastVisitType;
 	private Short cfVisitNum;
     private Short numChildren;
 	private String playCompFeedingVid;
 	private String lastvisit;
 	private Date dateCf7;
 	private String confirmClose;
     private Boolean close;
     private Date creationTime = new Date();
     private String ownerId;
     private String invalidGroupsTransfer;
     private String newOwner;
 
     public CfMotherForm() {
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
 	@JoinColumn(name = "user_id")
    @Cascade({	CascadeType.SAVE_UPDATE, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REPLICATE, CascadeType.LOCK, CascadeType.EVICT })
 	public Flw getFlw() {
 		return this.flw;
 	}
 
 	public void setFlw(Flw flw) {
 		this.flw = flw;
 	}
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "case_id")
    @Cascade({	CascadeType.SAVE_UPDATE, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REPLICATE, CascadeType.LOCK, CascadeType.EVICT })
 	public MotherCase getMotherCase() {
 		return this.motherCase;
 	}
 
 	public void setMotherCase(MotherCase motherCase) {
 		this.motherCase = motherCase;
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
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_cf_1")
 	public Date getDateCf1() {
 		return this.dateCf1;
 	}
 
 	public void setDateCf1(Date dateCf1) {
 		this.dateCf1 = dateCf1;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_cf_2")
 	public Date getDateCf2() {
 		return this.dateCf2;
 	}
 
 	public void setDateCf2(Date dateCf2) {
 		this.dateCf2 = dateCf2;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_cf_3")
 	public Date getDateCf3() {
 		return this.dateCf3;
 	}
 
 	public void setDateCf3(Date dateCf3) {
 		this.dateCf3 = dateCf3;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_cf_4")
 	public Date getDateCf4() {
 		return this.dateCf4;
 	}
 
 	public void setDateCf4(Date dateCf4) {
 		this.dateCf4 = dateCf4;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_cf_5")
 	public Date getDateCf5() {
 		return this.dateCf5;
 	}
 
 	public void setDateCf5(Date dateCf5) {
 		this.dateCf5 = dateCf5;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_cf_6")
 	public Date getDateCf6() {
 		return this.dateCf6;
 	}
 
 	public void setDateCf6(Date dateCf6) {
 		this.dateCf6 = dateCf6;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_last_visit")
 	public Date getDateLastVisit() {
 		return this.dateLastVisit;
 	}
 
 	public void setDateLastVisit(Date dateLastVisit) {
 		this.dateLastVisit = dateLastVisit;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_next_cf")
 	public Date getDateNextCf() {
 		return this.dateNextCf;
 	}
 
 	public void setDateNextCf(Date dateNextCf) {
 		this.dateNextCf = dateNextCf;
 	}
 
 	@Column(name = "last_visit_type")
 	public String getLastVisitType() {
 		return this.lastVisitType;
 	}
 
 	public void setLastVisitType(String lastVisitType) {
 		this.lastVisitType = lastVisitType;
 	}
 
 	@Column(name = "cf_visit_num")
 	public Short getCfVisitNum() {
 		return this.cfVisitNum;
 	}
 
 	public void setCfVisitNum(Short cfVisitNum) {
 		this.cfVisitNum = cfVisitNum;
 	}
 
     @Column(name = "num_children")
 	public Short getNumChildren() {
 		return this.numChildren;
 	}
 
 	public void setNumChildren(Short numChildren) {
 		this.numChildren = numChildren;
 	}
 
 	@Column(name = "play_comp_feeding_vid")
 	public String getPlayCompFeedingVid() {
 		return this.playCompFeedingVid;
 	}
 
 	public void setPlayCompFeedingVid(String playCompFeedingVid) {
 		this.playCompFeedingVid = playCompFeedingVid;
 	}
 
 	@Column(name = "lastvisit")
 	public String getLastvisit() {
 		return this.lastvisit;
 	}
 
 	public void setLastvisit(String lastvisit) {
 		this.lastvisit = lastvisit;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "date_cf_7")
 	public Date getDateCf7() {
 		return this.dateCf7;
 	}
 
 	public void setDateCf7(Date dateCf7) {
 		this.dateCf7 = dateCf7;
 	}
 
 	@Column(name = "confirm_close")
 	public String getConfirmClose() {
 		return this.confirmClose;
 	}
 
 	public void setConfirmClose(String confirmClose) {
 		this.confirmClose = confirmClose;
 	}
 
     @Column(name = "close")
     public Boolean getClose() {
         return this.close;
     }
 
     public void setClose(Boolean close) {
         this.close = close;
     }
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "creation_time")
     public Date getCreationTime() {
         return creationTime;
     }
 
     public void setCreationTime(Date creationTime) {
         this.creationTime = creationTime;
     }
 
     @Override
     public String toString() {
         return FormToString.toString(this);
     }
 
     @Column(name = "owner_id")
     public String getOwnerId() {
         return ownerId;
     }
 
     public void setOwnerId(String ownerId) {
         this.ownerId = ownerId;
     }
 
    @Column(name = "invalid_groups_transfer")
     public String getInvalidGroupsTransfer() {
         return invalidGroupsTransfer;
     }
 
     public void setInvalidGroupsTransfer(String invalidGroupsTransfer) {
         this.invalidGroupsTransfer = invalidGroupsTransfer;
     }
 
     @Column(name = "new_owner")
     public String getNewOwner() {
         return newOwner;
     }
 
     public void setNewOwner(String newOwner) {
         this.newOwner = newOwner;
     }
 }
