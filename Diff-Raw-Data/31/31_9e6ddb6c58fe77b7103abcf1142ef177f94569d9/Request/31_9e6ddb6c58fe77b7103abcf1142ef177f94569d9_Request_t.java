 package fr.cg95.cvq.business.request;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.xmlbeans.XmlObject;
 
 
 /**
  * Represent a request issued by an e-citizen.
  *
  * @hibernate.class
  *  table="request"
  *  lazy="false"
  *
  * @author bor@zenexity.fr
  */
 public abstract class Request implements Serializable {
 
     private static final long serialVersionUID = 1L;
 	
     public static final String SEARCH_BY_REQUEST_ID = "id";
     public static final String SEARCH_BY_HOME_FOLDER_ID = "homeFolderId";
     public static final String SEARCH_BY_REQUESTER_LASTNAME = "requesterLastName";
     public static final String SEARCH_BY_SUBJECT_LASTNAME = "subjectLastName";
     public static final String SEARCH_BY_SUBJECT_ID = "subjectId";
     public static final String SEARCH_BY_CATEGORY_NAME = "categoryName";
     public static final String SEARCH_BY_CATEGORY_ID = "categoryId";
     public static final String SEARCH_BY_REQUEST_TYPE_ID = "requestTypeId";
     public static final String SEARCH_BY_REQUEST_TYPE_LABEL = "requestTypeLabel";
     public static final String SEARCH_BY_STATE = "state";    
     public static final String SEARCH_BY_CREATION_DATE = "creationDate";
    public static final String SEARCH_BY_VALIDATION_DATE = "validationDate";
     public static final String SEARCH_BY_LAST_MODIFICATION_DATE = "lastModificationDate";
     public static final String SEARCH_BY_LAST_INTERVENING_USER_ID = "lastInterveningUserId";
     public static final String SEARCH_BY_QUALITY_TYPE = "qualityType";
 	
     public static final String SEARCH_BY_RESULTING_STATE = "resultingState";
     public static final String SEARCH_BY_MODIFICATION_DATE = "modificationDate";
 
     public static final String QUALITY_TYPE_OK = "qualityTypeOk";
     public static final String QUALITY_TYPE_ORANGE = "qualityTypeOrange";
     public static final String QUALITY_TYPE_RED = "qualityTypeRed";
     public static final String DRAFT = "draft";
 
     
     /** identifier field */
     private Long id;
     private Date creationDate;
     private Date lastModificationDate;
     private Date validationDate;
     private Long lastInterveningUserId;
     private RequestState state;
     private DataState dataState;
     private MeansOfContact meansOfContact;
     /** the request's step, instruction or delivery, set by the model */
     private RequestStep step;
     private RequestType requestType;
     /** for request types that have seasons, keep the season uuid */
     private String seasonUuid;
     /** QoS level 1 : instruction delay is expiring soon. */
     private Boolean orangeAlert;
     /** QoS level 2 : instruction delay has expired. */
     private Boolean redAlert;
 
     private Long homeFolderId;
     private Long requesterId;
     private String requesterLastName;
     private String requesterFirstName;
     private Long subjectId;
     private String subjectLastName;
     private String subjectFirstName;
     private Boolean draft;
     
     private Set<RequestDocument> documents;
     private Set<RequestAction> actions;
     private Set<RequestNote> notes;
     
     private Map<String,Map<String,String>> stepStates;
 
     public Request() {
         this.draft = false;
         this.stepStates = new HashMap<String, Map<String, String>>();
     }
 
     public abstract String modelToXmlString() ;
 
     public abstract XmlObject modelToXml() ;
 
     public abstract fr.cg95.cvq.xml.common.RequestType modelToXmlRequest();
     
     public void fillCommonXmlInfo(fr.cg95.cvq.xml.common.RequestType requestType) {
         Calendar calendar = Calendar.getInstance();
 
         requestType.setRequestTypeLabel(this.getRequestType().getLabel());
         if (this.id != null)
             requestType.setId(this.id.longValue());
         // creation date can't be null, this is certified by the model :-)
         // hmm ... it can in fact be null when we generate test empty PDFs ...
         if (this.creationDate != null) {
             calendar.setTime(this.creationDate);
             requestType.setCreationDate(calendar);
         }
         if (this.lastModificationDate != null) {
             calendar.setTime(this.lastModificationDate);
             requestType.setLastModificationDate(calendar);
         }
         if (this.validationDate != null) {
             calendar.setTime(this.validationDate);
             requestType.setValidationDate(calendar);
         }
         if (this.lastInterveningUserId != null)
             requestType.setLastInterveningUserId(this.lastInterveningUserId.longValue());
 
         if (this.state != null)
             requestType.setState(fr.cg95.cvq.xml.common.RequestStateType.Enum.forString(this.state.toString()));
         if (this.dataState != null)
             requestType.setDataState(fr.cg95.cvq.xml.common.DataStateType.Enum.forString(this.dataState.toString()));
         
         if (meansOfContact != null)
             requestType.setMeansOfContact(MeansOfContact.modelToXml(meansOfContact));
         
         if (this.step != null)
             requestType.setStep(fr.cg95.cvq.xml.common.RequestType.Step.Enum.forString(this.step.toString()));
     }
 
     public void fillCommonModelInfo(Request request,
             fr.cg95.cvq.xml.common.RequestType requestType) {
         
         Calendar calendar = Calendar.getInstance();
 
         request.setId(new Long(requestType.getId()));
 
         // creation date can be null, this is certified by the model :-)
         calendar = requestType.getCreationDate();
         if (calendar != null)
             request.setCreationDate(calendar.getTime());
         calendar = requestType.getLastModificationDate();
         if (calendar != null)
             request.setLastModificationDate(calendar.getTime());
         calendar = requestType.getValidationDate();
         if (calendar != null)
             request.setValidationDate(calendar.getTime());
         request.setLastInterveningUserId(new Long(requestType.getLastInterveningUserId()));
         if (requestType.getState() != null)
             request.setState(RequestState.forString(requestType.getState().toString()));
         
         if (requestType.getMeansOfContact() != null)
             request.setMeansOfContact(MeansOfContact.xmlToModel(requestType.getMeansOfContact()));
         
         if (requestType.getStep() != null)
             request.setStep(RequestStep.forString(requestType.getStep().toString()));
         // TODO REFACTORING
         /*
         if (requestType.getRequester() != null)
             request.setRequester(Adult.xmlToModel(requestType.getRequester()));
         if (requestType.getHomeFolder() != null)
             request.setHomeFolder(HomeFolder.xmlToModel(requestType.getHomeFolder()));
         if (requestType.getSubject() != null) {
             if (requestType.getSubject().isSetIndividual()) {
                 request.setSubject(Individual.xmlToModel(requestType.getSubject().getIndividual()));
             } else if (requestType.getSubject().isSetAdult()) {
                 request.setSubject(Adult.xmlToModel(requestType.getSubject().getAdult()));
             } else if (requestType.getSubject().isSetChild()) {
                 request.setSubject(Child.xmlToModel(requestType.getSubject().getChild()));
             }
         }
         */
     }
 
     /**
      * @hibernate.id
      *  generator-class="sequence"
      *  column="id"
      */
     public Long getId() {
         return this.id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     /**
      * @hibernate.property
      *  column="home_folder_id"
      */
     public Long getHomeFolderId() {
         return this.homeFolderId;
     }
 
     public void setHomeFolderId(Long homeFolderId) {
         this.homeFolderId = homeFolderId;
     }
 
     /**
      * @hibernate.property
      *  column="creation_date"
      */
     public Date getCreationDate() {
         return this.creationDate;
     }
 
     public void setCreationDate(Date creationDate) {
         this.creationDate = creationDate;
     }
 
     /**
      * @hibernate.property
      *  column="last_modification_date"
      */
     public Date getLastModificationDate() {
         return this.lastModificationDate;
     }
 
     public void setLastModificationDate(Date lastModificationDate) {
         this.lastModificationDate = lastModificationDate;
     }
 
     /**
      * @hibernate.property
      *  column="last_intervening_user_id"
      */
     public Long getLastInterveningUserId() {
         return this.lastInterveningUserId;
     }
 
     public void setLastInterveningUserId(Long lastInterveningUserId) {
         this.lastInterveningUserId = lastInterveningUserId;
     }
 
     /**
      * @hibernate.property
      *  column="state"
      *  not-null="true"
      *  length="16"
      */
     public RequestState getState() {
         return this.state;
     }
 
     public void setState(RequestState state) {
         this.state = state;
     }
 
     /**
      * @hibernate.property
      *  column="data_state"
      *  not-null="true"
      *  length="16"
      */
     public DataState getDataState() {
         return this.dataState;
     }
 
     public void setDataState(DataState dataState) {
         this.dataState = dataState;
     }
 
     /** 
      * @hibernate.many-to-one
      *  class="fr.cg95.cvq.business.request.MeansOfContact"
      *  column="means_of_contact_id"
      */
     public MeansOfContact getMeansOfContact() {
         return this.meansOfContact;
     }
 
     public void setMeansOfContact(MeansOfContact meansOfContact) {
         this.meansOfContact = meansOfContact;
     }
 
     /**
      * @hibernate.property
      *  column="request_step"
      *  length="16"
      */
     public RequestStep getStep() {
         return this.step;
     }
 
     public void setStep(RequestStep step) {
         this.step = step;
     }
 
     /**
      * @hibernate.property
      *  column="requester_id"
      */
     public Long getRequesterId() {
         return this.requesterId;
     }
 
     public void setRequesterId(Long requesterId) {
         this.requesterId = requesterId;
     }
 
     /**
      * @hibernate.property
      *  column="requester_last_name"
      */
     public String getRequesterLastName() {
         return requesterLastName;
     }
 
     public void setRequesterLastName(String requesterLastName) {
         this.requesterLastName = requesterLastName;
     }
 
     /**
      * @hibernate.property
      *  column="requester_first_name"
      */
     public String getRequesterFirstName() {
         return requesterFirstName;
     }
 
     public void setRequesterFirstName(String requesterFirstName) {
         this.requesterFirstName = requesterFirstName;
     }
 
     /**
      * @hibernate.many-to-one
      *  class="fr.cg95.cvq.business.request.RequestType"
      *  column="request_type_id"
      */
     public RequestType getRequestType() {
         return this.requestType;
     }
 
     public void setRequestType(RequestType requestType) {
         this.requestType = requestType;
     }
 
     /**
      * @hibernate.property
      *  column="season_uuid"
      */
     public String getSeasonUuid() {
         return seasonUuid;
     }
 
     public void setSeasonUuid(String seasonUuid) {
         this.seasonUuid = seasonUuid;
     }
 
     /**
      * @hibernate.set
      *  lazy="true"
      *  cascade="all"
      *  order-by="id asc"
      * @hibernate.key
      *  column="request_id"
      * @hibernate.one-to-many
      *  class="fr.cg95.cvq.business.request.RequestDocument"
      */
     public Set<RequestDocument> getDocuments() {
         return this.documents;
     }
 
     public void setDocuments(Set<RequestDocument> documents) {
         this.documents = documents;
     }
 
     /**
      * @hibernate.set
      *  lazy="true"
      *  cascade="all"
      *  order-by="id asc"
      * @hibernate.key
      *  column="request_id"
      * @hibernate.one-to-many
      *  class="fr.cg95.cvq.business.request.RequestAction"
      */
     public Set<RequestAction> getActions() {
         return this.actions;
     }
 
     public void setActions(Set<RequestAction> actions) {
         this.actions = actions;
     }
 
     /**
      * @hibernate.set
      *  lazy="true"
      *  cascade="all"
      *  order-by="id asc"
      * @hibernate.key
      *  column="request_id"
      * @hibernate.one-to-many
      *  class="fr.cg95.cvq.business.request.RequestNote"
      */
     public Set<RequestNote> getNotes() {
         return this.notes;
     }
 
     public void setNotes(Set<RequestNote> notes) {
         this.notes = notes;
     }
 
     @Override
     public String toString() {
         return new ToStringBuilder(this)
             .append("id", getId())
             .toString();
     }
 
     /**
      * @hibernate.property
      *  column="orange_alert"
      */
 	public Boolean getOrangeAlert() {
 		return orangeAlert;
 	}
 
 	public void setOrangeAlert(Boolean orangeAlert) {
 		this.orangeAlert = orangeAlert;
 	}
 
     /**
      * @hibernate.property
      *  column="red_alert"
      */
 	public Boolean getRedAlert() {
 		return redAlert;
 	}
 
 	public void setRedAlert(Boolean redAlert) {
 		this.redAlert = redAlert;
 	}
 
     /**
      * @hibernate.property
      *  column="validation_date"
      */
     public Date getValidationDate() {
         return validationDate;
     }
 
     public void setValidationDate(Date validationDate) {
         this.validationDate = validationDate;
     }
 
     /**
      * @hibernate.property
      *  column="subject_id"
      *  update="false"
      */
     public Long getSubjectId() {
         return subjectId;
     }
 
     public void setSubjectId(Long subjectId) {
         this.subjectId = subjectId;
     }
 
     /**
      * @hibernate.property
      *  column="subject_last_name"
      *  update="false"
      */
     public String getSubjectLastName() {
         return subjectLastName;
     }
 
     public void setSubjectLastName(String subjectLastName) {
         this.subjectLastName = subjectLastName;
     }
 
     /**
      * @hibernate.property
      *  column="subject_first_name"
      *  update="false"
      */
     public String getSubjectFirstName() {
         return subjectFirstName;
     }
 
     public void setSubjectFirstName(String subjectFirstName) {
         this.subjectFirstName = subjectFirstName;
     }
 
     /**
      * @hibernate.property
      *  column="draft"
      *  not-null="true"
      */
     public Boolean getDraft() {
         return draft;
     }
 
     public void setDraft(Boolean draft) {
         this.draft = draft;
     }
     
     /**
      * @hibernate.property
      *  column="step_states"
      *  type="serializable"
      */
     public Map<String, Map<String, String>> getStepStates() {
         return stepStates;
     }
 
     public void setStepStates(Map<String, Map<String, String>> stepStates) {
         this.stepStates = stepStates;
     }
 }
