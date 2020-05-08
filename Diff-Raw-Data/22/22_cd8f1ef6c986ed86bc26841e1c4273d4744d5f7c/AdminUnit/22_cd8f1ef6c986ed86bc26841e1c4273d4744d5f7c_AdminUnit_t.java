 package border.model;
 
 import java.text.ParseException;
 import java.util.Date;
 import java.util.Set;
 
 import javax.persistence.*;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.hibernate.validator.constraints.NotEmpty;
 
 import border.helper.DateHelper;
 
 @Entity
 public class AdminUnit {
 
 	@Id
 	@Column(name = "id")
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private Long AdminUnitID;
 	@NotNull
 	@Size(min = 2, max = 16)
 	private String code;
 	@NotNull
 	@Size(min = 2, max = 64)
 	private String name;
	@Column(nullable = false)
 	private String comment;
 	@NotNull
 	@Min(1)
 	private Long adminUnitTypeID;
 	@Column(nullable = false)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date fromDate;
 	@Column(nullable = false)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date toDate;
 	@Column(nullable = false)
 	private String openedBy;
 	@Column(nullable = false)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date openedDate;
 	@Column(nullable = false)
 	private String changedBy;
 	@Column(nullable = false)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date changedDate;
 	private String closedBy; // only value from DB schema with NULL allowed
 	@Column(nullable = false)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date closedDate;
 
 	// one-to-many definitions into AdminUnitSubordination
 	
 	// subordinations where this unit is master
 	@OneToMany(mappedBy = "adminUnitMaster", fetch = FetchType.EAGER)
 	private Set<AdminUnitSubordination> adminUnitSubordinationSubordinates;
 	// subordinations where this unit is subordinate
 	@OneToMany(mappedBy = "adminUnitSubordinate", fetch = FetchType.EAGER)
 	private Set<AdminUnitSubordination> adminUnitSubordinationMasters;
 	
 
 	public AdminUnit() {
 	}
 
 	public AdminUnit(String code, String name, String comment,
 			Long adminUnitTypeID, String fromDate, String toDate,
 			String openedBy, String openedDate, String changedBy,
 			String changedDate, String closedBy, String closedDate)
 			throws ParseException {
 
 		this.code = code;
 		this.name = name;
 
 		if (comment == null || comment.trim().isEmpty()) {
 			this.comment = "Add here extra information";
 		} else {
 			this.comment = comment;
 		}
 
 		this.adminUnitTypeID = adminUnitTypeID;
 		this.fromDate = DateHelper.getParsedDate(fromDate);
 		this.toDate = DateHelper.getParsedDate(toDate);
 		this.openedBy = openedBy;
 		this.openedDate = DateHelper.getParsedDate(openedDate);
 		this.changedBy = changedBy;
 		this.changedDate = DateHelper.getParsedDate(changedDate);
 		this.closedBy = closedBy;
 		this.closedDate = DateHelper.getParsedDate(closedDate);
 	}
 
 	public AdminUnit(String code, String name, String comment,
 			Long adminUnitTypeID) {
 		this.code = code;
 		this.name = name;
 		this.comment = comment;
 		if (this.comment == null || this.comment.trim().isEmpty()) {
 			this.comment = "Add here extra information";
 		}
 		this.adminUnitTypeID = adminUnitTypeID;
 
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
 		// TODO: do not overwrite existing dates
		
		if (this.comment == null || this.comment.trim().isEmpty()) {
			this.comment = "Add here extra information";
		}
		
 		this.fromDate = DateHelper.getNow();
 		this.toDate = DateHelper.getFutureDate();
 
 		this.openedDate = DateHelper.getNow();
 		this.changedDate = DateHelper.getNow();
 		this.closedDate = DateHelper.getFutureDate();
 
 		this.openedBy = "admin";
 		this.changedBy = "admin";
 		this.closedBy = "admin";
 	}
 
 	public Long getAdminUnitID() {
 		return AdminUnitID;
 	}
 
 	public void setAdminUnitID(Long adminUnitID) {
 		AdminUnitID = adminUnitID;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getCode() {
 		return code;
 	}
 
 	public void setCode(String code) {
 		this.code = code;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	public Long getAdminUnitTypeID() {
 		return adminUnitTypeID;
 	}
 
 	public void setAdminUnitTypeID(Long adminUnitTypeID) {
 		this.adminUnitTypeID = adminUnitTypeID;
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
 
 	@Override
 	public String toString() {
 		return "AdminUnit [id=" + AdminUnitID + ", name=" + name + "]";
 	}
 
 	public Set<AdminUnitSubordination> getAdminUnitSubordinationMasters() {
 		return adminUnitSubordinationMasters;
 	}
 
 	public void setAdminUnitSubordinationMasters(
 			Set<AdminUnitSubordination> adminUnitSubordinationMasters) {
 		this.adminUnitSubordinationMasters = adminUnitSubordinationMasters;
 	}
 
 	public Set<AdminUnitSubordination> getAdminUnitSubordinationSubordinates() {
 		return adminUnitSubordinationSubordinates;
 	}
 
 	public void setAdminUnitSubordinationSubordinates(
 			Set<AdminUnitSubordination> adminUnitSubordinationSubordinates) {
 		this.adminUnitSubordinationSubordinates = adminUnitSubordinationSubordinates;
 	}
 }
