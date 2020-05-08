 package border.model;
 
 import java.text.ParseException;
 import java.util.*;
 
 import javax.persistence.*;
 import javax.validation.constraints.*;
 
 import org.hibernate.validator.constraints.NotEmpty;
 
 import border.helper.DateHelper;
 
 /*
  * JavaBean is this, says the Yoda!
  */
 @Entity
 @Table(name = "AdminUnitType")
 public class AdminUnitType {
 
 	@Id
 	@Column(name = "id")
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private Long adminUnitTypeID; 
 	@Size(min = 1, max = 16)
 	private String code;
 	@Size(min = 1, max = 64)
 	private String name;
 	@Column(nullable = false)
 	private String comment;
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(nullable = false)
 	private Date fromDate;
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(nullable = false)
 	private Date toDate;
 	@NotNull
 	private String openedBy;
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(nullable = false)
 	private Date openedDate;
 	@NotNull
 	private String changedBy;
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(nullable = false)
 	private Date changedDate;
 	@NotNull
 	private String closedBy;
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(nullable = false)
 	private Date closedDate;
 	
 	// one-to-many definitions into AdminUnitTypeSubordination
 	// masters of this unit
 	@OneToMany(mappedBy="adminUnitTypeSubordinate", fetch = FetchType.EAGER)
     private Set<AdminUnitTypeSubordination> adminUnitTypeSubordinationMasters;
 	// subordinates of this unit
 	@OneToMany(mappedBy="adminUnitTypeMaster",  fetch = FetchType.EAGER)
     private Set<AdminUnitTypeSubordination> adminUnitTypeSubordinationSubordinates;
 
 	
 	public AdminUnitType() {
 		// TODO: find out real username
 		// TODO: do not overwrite exsisting dates
 		this.fromDate = DateHelper.getNow();
 		this.toDate = DateHelper.getFutureDate();
 
 		this.openedDate =  DateHelper.getNow();
 		this.changedDate =  DateHelper.getNow();
 		this.closedDate = DateHelper.getFutureDate();
 		
 		this.openedBy = "admin";
 		this.changedBy = "admin";
 		this.closedBy = "admin";
 
 	}
 
 	public AdminUnitType(String code, String name, String comment,
 			String fromDate, String toDate, String openedBy, String openedDate,
 			String changedBy, String changedDate, String closedBy,
 			String closedDate) throws ParseException {
 
 		this.code = code;
 		this.name = name;
 		
 		if (comment == null || comment.trim().isEmpty()) {
 			this.comment = "Add here extra information";
 		} else {
 			this.comment = comment;
 		}
 		
 		this.fromDate = DateHelper.getParsedDate(fromDate);
 		this.toDate = DateHelper.getParsedDate(toDate);
 		this.openedBy = openedBy;
 		this.openedDate = DateHelper.getParsedDate(openedDate);
 		this.changedBy = changedBy;
 		this.changedDate = DateHelper.getParsedDate(changedDate);
 		this.closedBy = closedBy;
 		this.closedDate = DateHelper.getParsedDate(closedDate);
 	}
 
 	public AdminUnitType(String code, String name, String comment) {
 		this.code = code;
 		this.name = name;
 		
 		if (comment == null || comment.trim().isEmpty()) {
 			this.comment = "Add here extra information";
 		} else {
 			this.comment = comment;
 		}
 
 		this.fromDate = DateHelper.getNow();
 		this.toDate = DateHelper.getFutureDate();
 
 		this.openedBy = "admin";
 		this.openedDate = DateHelper.getNow();
 		this.changedBy = "admin";
 		this.changedDate = DateHelper.getNow();
 		this.closedBy = "admin";
 		this.closedDate = DateHelper.getFutureDate();
 		
 	}
 
 	@PreUpdate
 	public void preUpdate() {
 		changedDate = new Date();
 	}
 
 	@PrePersist
 	public void prePersist() {
 		// TODO: find out real username
 		// TODO: do not overwrite exsisting dates
		
		if (this.comment == null || this.comment.trim().isEmpty()) {
			this.comment = "Add here extra information";
		}
		
 		this.fromDate = DateHelper.getNow();
 		this.toDate = DateHelper.getFutureDate();
 
 		this.openedDate =  DateHelper.getNow();
 		this.changedDate =  DateHelper.getNow();
 		this.closedDate = DateHelper.getFutureDate();
 		
 		this.openedBy = "admin";
 		this.changedBy = "admin";
 		this.closedBy = "admin";
 		
 	}
 
 
 	public String getCode() {
 		return code;
 	}
 
 	public void setCode(String code) {
 		this.code = code;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	public Date getFromDate() {
 		return fromDate;
 	}
 
 	public void setFromDate(Date fromDate) {
 		this.fromDate = fromDate;
 	}
 
 	public Date getToDate() {
 		return toDate;
 	}
 
 	public void setToDate(Date toDate) {
 		this.toDate = toDate;
 	}
 
 	public String getOpenedBy() {
 		return openedBy;
 	}
 
 	public void setOpenedBy(String openedBy) {
 		this.openedBy = openedBy;
 	}
 
 	public Date getOpenedDate() {
 		return openedDate;
 	}
 
 	public void setOpenedDate(Date openedDate) {
 		this.openedDate = openedDate;
 	}
 
 	public String getChangedBy() {
 		return changedBy;
 	}
 
 	public void setChangedBy(String changedBy) {
 		this.changedBy = changedBy;
 	}
 
 	public Date getChangedDate() {
 		return changedDate;
 	}
 
 	public void setChangedDate(Date changedDate) {
 		this.changedDate = changedDate;
 	}
 
 	public String getClosedBy() {
 		return closedBy;
 	}
 
 	public void setClosedBy(String closedBy) {
 		this.closedBy = closedBy;
 	}
 
 	public Date getClosedDate() {
 		return closedDate;
 	}
 
 	public void setClosedDate(Date closedDate) {
 		this.closedDate = closedDate;
 	}
 	
 	public Set<AdminUnitTypeSubordination> getAdminUnitTypeSubordinationMasters() {
 		return adminUnitTypeSubordinationMasters;
 	}
 
 	public void setAdminUnitTypeSubordinationMasters(
 			Set<AdminUnitTypeSubordination> adminUnitTypeSubordinationMasters) {
 		this.adminUnitTypeSubordinationMasters = adminUnitTypeSubordinationMasters;
 	}
 
 	public Set<AdminUnitTypeSubordination> getAdminUnitTypeSubordinationSubordinates() {
 		return adminUnitTypeSubordinationSubordinates;
 	}
 
 	public void setAdminUnitTypeSubordinationSubordinates(
 			Set<AdminUnitTypeSubordination> adminUnitTypeSubordinationSubordinates) {
 		this.adminUnitTypeSubordinationSubordinates = adminUnitTypeSubordinationSubordinates;
 	}
 
 	@Override
 	public String toString() {
 		return "AdminUnitType [id=" + adminUnitTypeID + ", name=" + name + ", code=" + code + "]";
 	}
 
 	public Long getAdminUnitTypeID() {
 		return adminUnitTypeID;
 	}
 
 	public void setAdminUnitTypeID(Long adminUnitTypeID) {
 		this.adminUnitTypeID = adminUnitTypeID;
 	}
 
 
 
 }
