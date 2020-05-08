 package velo.entity;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "VL_REC_PROCESS_SUMMARY_EVENT")
@SequenceGenerator(name="ReconcileProcessSummaryEventIdSeq",sequenceName="REC_PROCESS_SUMMARY_EVENT_ID_SEQ")
 public class ReconcileProcessSummaryEvent {
 	public enum ReconcileProcessSummaryEvents {
 		IDENTITY_CONFIRMED, IDENTITY_REMOVED, IDENTITY_UNASSIGNED, IDENTITY_UNMATCHED, IDENTITY_MODIFIED, 
 		IDENTITY_ATTRIBUTE_MODIFIED,GROUP_CREATED, GROUP_MODIFIED, GROUP_REMOVED, 
 		GROUP_MEMBER_ASSOCIATED, GROUP_MEMBER_DISSOCIATED,GROUP_MEMBERSHIP_MODIFY
 	}
 	
 	public enum ReconcileProcessSummaryEventSeverities {
 		INFO, WARN
 	}
 	
 	public enum ReconcileProcessSummaryEventEntityType {
 		IDENTITY, GROUP, ACCOUNT_ATTRIBUTE
 	}
 	
 	private Long reconcileProcessSummaryEventId;
 	private String message;
 	private ReconcileProcessSummaryEvents event;
 	private ReconcileProcessSummaryEventSeverities severity;
 	private ReconcileProcessSummary reconcileProcessSummary;
 	private ReconcileProcessSummaryEventEntityType entityType;
 	private String entityName;
 	
 	
 	public ReconcileProcessSummaryEvent() {
 		
 	}
 	
 	public ReconcileProcessSummaryEvent(
 			ReconcileProcessSummary reconcileProcessSummary,
 			ReconcileProcessSummaryEvents event,
 			ReconcileProcessSummaryEventSeverities severity,
 			ReconcileProcessSummaryEventEntityType entityType, String entityName, String message) {
 		this.message = message;
 		this.event = event;
 		this.severity = severity;
 		this.reconcileProcessSummary = reconcileProcessSummary;
 		this.entityType = entityType;
 		this.entityName = entityName;
 	}
 
 	@Id
     @GeneratedValue(strategy=GenerationType.AUTO,generator="ReconcileProcessSummaryEventIdSeq")
    @Column(name="REC_PROCESS_SUMMARY_EVENT_ID")
 	public Long getReconcileProcessSummaryEventId() {
 		return reconcileProcessSummaryEventId;
 	}
 	
 	public void setReconcileProcessSummaryEventId(
 			Long reconcileProcessSummaryEventId) {
 		this.reconcileProcessSummaryEventId = reconcileProcessSummaryEventId;
 	}
 	
 	@Lob
 	@Column(name="MESSAGE")
 	public String getMessage() {
 		return message;
 	}
 	
 	public void setMessage(String message) {
 		this.message = message;
 	}
 	
 	@Column(name="EVENT")
     @Enumerated(EnumType.STRING)
 	public ReconcileProcessSummaryEvents getEvent() {
 		return event;
 	}
 	
 	public void setEvent(ReconcileProcessSummaryEvents event) {
 		this.event = event;
 	}
 	
 	@Column(name="SEVERITY")
     @Enumerated(EnumType.STRING)
 	public ReconcileProcessSummaryEventSeverities getSeverity() {
 		return severity;
 	}
 	
 	public void setSeverity(ReconcileProcessSummaryEventSeverities severity) {
 		this.severity = severity;
 	}
 
 	@ManyToOne(optional=false)
     @JoinColumn(name="REC_PROCESS_SUMMARY_ID", nullable=false)
 	public ReconcileProcessSummary getReconcileProcessSummary() {
 		return reconcileProcessSummary;
 	}
 
 	public void setReconcileProcessSummary(
 			ReconcileProcessSummary reconcileProcessSummary) {
 		this.reconcileProcessSummary = reconcileProcessSummary;
 	}
 
 	@Column(name="ENTITY_TYPE")
 	@Enumerated(EnumType.STRING)
 	public ReconcileProcessSummaryEventEntityType getEntityType() {
 		return entityType;
 	}
 
 	public void setEntityType(ReconcileProcessSummaryEventEntityType entityType) {
 		this.entityType = entityType;
 	}
 
 	@Column(name="ENTITY_NAME")
 	public String getEntityName() {
 		return entityName;
 	}
 
 	public void setEntityName(String entityName) {
 		this.entityName = entityName;
 	}
 
 	
 	public String toString() {
 		return "[Event ID '" + getReconcileProcessSummaryEventId() + "'], [Event: '" + getEvent() + "'], [Entity Type: '" + getEntityType() + "'], [Entity Name: '" + getEntityName() + "'], [Message: '" + getMessage()  +"']";
 	}
 	
 }
