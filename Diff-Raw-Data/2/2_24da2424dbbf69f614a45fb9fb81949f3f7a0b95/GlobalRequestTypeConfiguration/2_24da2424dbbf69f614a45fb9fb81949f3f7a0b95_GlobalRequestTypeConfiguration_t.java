 package fr.cg95.cvq.business.request;
 
 /**
  * @hibernate.class
  *  table="global_request_type_configuration"
  *  lazy="false"
  *
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public class GlobalRequestTypeConfiguration {
 
     private Long id;
 
     private int draftLiveDuration = 20;
 
     private int draftNotificationBeforeDelete = 7;
 
     /**
      * Whether an email alert is sent to notify of requests whose instruction is late,
      * defaults to false.
      */
     private boolean instructionAlertsEnabled = false;
 
     /**
      * Whether, if instruction alerts are enabled, the email sent displays a detailed resume of
      * requests to instruct, defaults to false.
      */
     private boolean instructionAlertsDetailed = false;
 
     private int instructionAlertDelay = 3;
 
     private int instructionMaxDelay = 10;
 
     /**
      * The max lifetime of a request modification lock before it can be discarded (in minutes)
      */
     private int requestLockMaxDelay = 30;
 
     /**
      * Whether an email alert is sent to notify of newly created requests, defaults to false.
      */
     private boolean requestsCreationNotificationEnabled = false;
 
     /**
      * Number of months before requests are filed (between 1 and 36 months)
      */
    private int filingDelay = 6;
 
     /**
      * @hibernate.id
      *  generator-class="sequence"
      *  column="id"
      */
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     /**
      * @hibernate.property
      *  column="draft_live_duration"
      *  not-null="true"
      */
     public int getDraftLiveDuration() {
         return draftLiveDuration;
     }
 
     public void setDraftLiveDuration(int draftLiveDuration) {
         this.draftLiveDuration = draftLiveDuration;
     }
 
     /**
      * @hibernate.property
      *  column="draft_notification_before_delete"
      *  not-null="true"
      */
     public int getDraftNotificationBeforeDelete() {
         return draftNotificationBeforeDelete;
     }
 
     public void setDraftNotificationBeforeDelete(int draftNotificationBeforeDelete) {
         this.draftNotificationBeforeDelete = draftNotificationBeforeDelete;
     }
 
     /**
      * @hibernate.property
      *  column="requests_creation_notification_enabled"
      *  not-null="true"
      */
     public boolean isRequestsCreationNotificationEnabled() {
         return requestsCreationNotificationEnabled;
     }
 
     public void setRequestsCreationNotificationEnabled(boolean requestsCreationNotificationEnabled) {
         this.requestsCreationNotificationEnabled = requestsCreationNotificationEnabled;
     }
 
     /**
      * @hibernate.property
      *  column="instruction_alerts_enabled"
      *  not-null="true"
      */
     public boolean isInstructionAlertsEnabled() {
         return instructionAlertsEnabled;
     }
 
     public void setInstructionAlertsEnabled(boolean instructionAlertsEnabled) {
         this.instructionAlertsEnabled = instructionAlertsEnabled;
     }
 
     /**
      * @hibernate.property
      *  column="instruction_alerts_detailed"
      *  not-null="true"
      */
     public boolean isInstructionAlertsDetailed() {
         return instructionAlertsDetailed;
     }
 
     public void setInstructionAlertsDetailed(boolean instructionAlertsDetailed) {
         this.instructionAlertsDetailed = instructionAlertsDetailed;
     }
 
     /**
      * @hibernate.property
      *  column="instruction_max_delay"
      *  not-null="true"
      */
     public int getInstructionMaxDelay() {
         return instructionMaxDelay;
     }
 
     public void setInstructionMaxDelay(int instructionMaxDelay) {
         this.instructionMaxDelay = instructionMaxDelay;
     }
 
     /**
      * @hibernate.property
      *  column="instruction_alert_delay"
      *  not-null="true"
      */
     public int getInstructionAlertDelay() {
         return instructionAlertDelay;
     }
 
     public void setInstructionAlertDelay(int instructionAlertDelay) {
         this.instructionAlertDelay = instructionAlertDelay;
     }
 
     /**
      * @hibernate.property
      *  column="request_lock_max_delay"
      *  not-null="true"
      */
     public int getRequestLockMaxDelay() {
         return requestLockMaxDelay;
     }
 
     public void setRequestLockMaxDelay(int requestLockMaxDelay) {
         this.requestLockMaxDelay = requestLockMaxDelay;
     }
 
     /**
      * @hibernate.property
      *  column="filing_delay"
      *  not-null="true"
      */
     public int getFilingDelay() {
         return filingDelay;
     }
 
     public void setFilingDelay(int filingDelay) {
         this.filingDelay = filingDelay;
     }
 }
