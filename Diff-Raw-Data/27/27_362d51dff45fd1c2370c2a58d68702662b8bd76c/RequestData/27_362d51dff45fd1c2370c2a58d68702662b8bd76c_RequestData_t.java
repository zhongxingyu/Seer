 package fr.cg95.cvq.business.request;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 
 import net.sf.oval.constraint.NotNull;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
 
 import fr.cg95.cvq.business.users.MeansOfContact;
 import fr.cg95.cvq.service.request.SubjectId;
 import fr.cg95.cvq.service.request.condition.IConditionChecker;
 
 /**
  * Real persisted class containing request data
  */
 @Entity
 @Table(name="request")
 public class RequestData implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
 
     public static final Map<String, IConditionChecker> conditions =
         new HashMap<String, IConditionChecker>();
 
     @Id
     @GeneratedValue(strategy=GenerationType.SEQUENCE)
     private Long id;
 
     @Column(name="creation_date")
     private Date creationDate;
 
     @Column(name="last_modification_date")
     private Date lastModificationDate;
 
     @Column(name="validation_date")
     private Date validationDate;
 
     @Column(name="last_intervening_user_id")
     private Long lastInterveningUserId;
 
     @Enumerated(EnumType.STRING)
     @Column(name="state",length=16,nullable=false)
     private RequestState state;
 
     @Enumerated(EnumType.STRING)
     @Column(name="data_state",length=16,nullable=false)
     private DataState dataState;
 
     @NotNull(profiles = {"validation"}, message = "meansOfContact")
     @ManyToOne(fetch=FetchType.EAGER)
     @JoinColumn(name="means_of_contact_id")
     private MeansOfContact meansOfContact;
 
     @Enumerated(EnumType.STRING)
     @Column(name="request_step",length=16)
     private RequestStep step;
 
     @ManyToOne(fetch=FetchType.EAGER)
     @JoinColumn(name="request_type_id")
     private RequestType requestType;
 
     @ManyToOne(fetch=FetchType.EAGER)
     @JoinColumn(name="request_season_id")
     private RequestSeason requestSeason;
 
     /** QoS level 1 : instruction delay is expiring soon. */
     @Column(name="orange_alert")
     private Boolean orangeAlert;
 
     /** QoS level 2 : instruction delay has expired. */
     @Column(name="red_alert")
     private Boolean redAlert;
 
     @Column(name="home_folder_id")
     private Long homeFolderId;
 
     @Column(name="requester_id")
     private Long requesterId;
 
     @Column(name="requester_last_name")
     private String requesterLastName;
 
     @Column(name="requester_first_name")
     private String requesterFirstName;
 
     @SubjectId
     @Column(name="subject_id")
     private Long subjectId;
 
     @Column(name="subject_last_name")
     private String subjectLastName;
 
     @Column(name="subject_first_name")
     private String subjectFirstName;
 
     @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY)
     @OrderBy("id asc")
     @JoinColumn(name="request_id")
     private Set<RequestDocument> documents;
 
     @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY)
     @OrderBy("date desc")
     @JoinColumn(name="request_id")
     private Set<RequestAction> actions;
 
     @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY)
     @OrderBy("id asc")
     @JoinColumn(name="request_id")
     private Set<RequestNote> notes;
 
     @Column(name="step_states")
    @Type(type="serializable")
    private Map<String, Map<String, Object>> stepStates;
 
     @Column(name="specific_data_class")
     private Class<?> specificDataClass;
 
     @Column(name="specific_data_id")
     private Long specificDataId;
 
     /**
      * Hack to store PDF chunk with documents as they are when the request is validated;
      * to be deleted when we have document versioning
      */
     @Column(name="documents_archive")
     private byte[] documentsArchive;
 
     public RequestData() {
        this.stepStates = new LinkedHashMap<String, Map<String, Object>>();
     }
 
     public Long getId() {
         return this.id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Long getHomeFolderId() {
         return this.homeFolderId;
     }
 
     public void setHomeFolderId(Long homeFolderId) {
         this.homeFolderId = homeFolderId;
     }
 
     public Date getCreationDate() {
         return this.creationDate;
     }
 
     public void setCreationDate(Date creationDate) {
         this.creationDate = creationDate;
     }
 
     public Date getLastModificationDate() {
         return this.lastModificationDate;
     }
 
     public void setLastModificationDate(Date lastModificationDate) {
         this.lastModificationDate = lastModificationDate;
     }
 
     public Long getLastInterveningUserId() {
         return this.lastInterveningUserId;
     }
 
     public void setLastInterveningUserId(Long lastInterveningUserId) {
         this.lastInterveningUserId = lastInterveningUserId;
     }
 
     public RequestState getState() {
         return this.state;
     }
 
     public void setState(RequestState state) {
         this.state = state;
     }
 
     public DataState getDataState() {
         return this.dataState;
     }
 
     public void setDataState(DataState dataState) {
         this.dataState = dataState;
     }
 
     public MeansOfContact getMeansOfContact() {
         return this.meansOfContact;
     }
 
     public void setMeansOfContact(MeansOfContact meansOfContact) {
         this.meansOfContact = meansOfContact;
     }
 
     public RequestStep getStep() {
         return this.step;
     }
 
     public void setStep(RequestStep step) {
         this.step = step;
     }
 
     public Long getRequesterId() {
         return this.requesterId;
     }
 
     public void setRequesterId(Long requesterId) {
         this.requesterId = requesterId;
     }
 
     public String getRequesterLastName() {
         return requesterLastName;
     }
 
     public void setRequesterLastName(String requesterLastName) {
         this.requesterLastName = requesterLastName;
     }
 
     public String getRequesterFirstName() {
         return requesterFirstName;
     }
 
     public void setRequesterFirstName(String requesterFirstName) {
         this.requesterFirstName = requesterFirstName;
     }
 
     public RequestType getRequestType() {
         return this.requestType;
     }
 
     public void setRequestType(RequestType requestType) {
         this.requestType = requestType;
     }
 
     public RequestSeason getRequestSeason() {
         return requestSeason;
     }
 
     public void setRequestSeason(RequestSeason requestSeason) {
         this.requestSeason = requestSeason;
     }
 
     public Set<RequestDocument> getDocuments() {
         if (documents == null)
             documents = new HashSet<RequestDocument>();
         return this.documents;
     }
 
     public void setDocuments(Set<RequestDocument> documents) {
         this.documents = documents;
     }
 
     public Set<RequestAction> getActions() {
         return this.actions;
     }
 
     public void setActions(Set<RequestAction> actions) {
         this.actions = actions;
     }
 
     public Set<RequestNote> getNotes() {
         return this.notes;
     }
 
     public void setNotes(Set<RequestNote> notes) {
         this.notes = notes;
     }
 
     @Override
     public String toString() {
         return new ToStringBuilder(this).append("id", getId()).toString();
     }
 
     public Boolean getOrangeAlert() {
         return orangeAlert;
     }
 
     public void setOrangeAlert(Boolean orangeAlert) {
         this.orangeAlert = orangeAlert;
     }
 
     public Boolean getRedAlert() {
         return redAlert;
     }
 
     public void setRedAlert(Boolean redAlert) {
         this.redAlert = redAlert;
     }
 
     public Date getValidationDate() {
         return validationDate;
     }
 
     public void setValidationDate(Date validationDate) {
         this.validationDate = validationDate;
     }
 
     public Long getSubjectId() {
         return subjectId;
     }
 
     public void setSubjectId(Long subjectId) {
         this.subjectId = subjectId;
     }
 
     public String getSubjectLastName() {
         return subjectLastName;
     }
 
     public void setSubjectLastName(String subjectLastName) {
         this.subjectLastName = subjectLastName;
     }
 
     public String getSubjectFirstName() {
         return subjectFirstName;
     }
 
     public void setSubjectFirstName(String subjectFirstName) {
         this.subjectFirstName = subjectFirstName;
     }
 
     public Map<String, Map<String, Object>> getStepStates() {
        return stepStates;
     }
 
     public void setStepStates(Map<String, Map<String, Object>> stepStates) {
        this.stepStates = stepStates;
     }
 
     public Class<?> getSpecificDataClass() {
         return specificDataClass;
     }
 
     public void setSpecificDataClass(Class<?> specificDataClass) {
         this.specificDataClass = specificDataClass;
     }
 
     public Long getSpecificDataId() {
         return specificDataId;
     }
 
     public void setSpecificDataId(Long specificDataId) {
         this.specificDataId = specificDataId;
     }
 
     public byte[] getDocumentsArchive() {
         return documentsArchive;
     }
 
     public void setDocumentsArchive(byte[] documentsArchive) {
         this.documentsArchive = documentsArchive;
     }
 
     @Override
     public RequestData clone() {
         RequestData result = new RequestData();
         result.setHomeFolderId(getHomeFolderId());
         result.setMeansOfContact(getMeansOfContact());
         result.setRequestType(getRequestType());
         result.setSubjectId(getSubjectId());
         return result;
     }
 }
