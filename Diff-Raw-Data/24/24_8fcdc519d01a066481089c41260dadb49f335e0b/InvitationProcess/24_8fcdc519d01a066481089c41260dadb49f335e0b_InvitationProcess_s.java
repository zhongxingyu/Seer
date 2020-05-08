 package ac.at.fhkufstein.activiti;
 
 import ac.at.fhkufstein.bean.BmwEventController;
 import ac.at.fhkufstein.bean.BmwParticipantsController;
 import ac.at.fhkufstein.entity.ActivitiProcessHolder;
 import ac.at.fhkufstein.entity.BmwEvent;
 import ac.at.fhkufstein.entity.BmwParticipants;
 import ac.at.fhkufstein.entity.BmwUser;
 import ac.at.fhkufstein.service.PersistenceService;
 import ac.at.fhkufstein.session.BmwParticipantsFacade;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.activiti.engine.runtime.ProcessInstance;
 import java.util.Iterator;
 import java.util.List;
 import javax.faces.context.FacesContext;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import org.activiti.engine.runtime.Execution;
 
 public class InvitationProcess {
 
     final static public String DATABASE_EVENTID = "eventID";
     final static public String DATABASE_PARTICIPANTID = "participantId";
     final static public String ACTIVITI_CANCEL_INVITATION_TIME = "cancelInvitationTime";
     final static public String ACTIVITI_FOLLOW_UP_TIME = "followUpTime";
     final static public String ACTIVITI_INVITATION_SENT = "invitationSent";
     final static public String ACTIVITI_REMINDER_SENT = "reminderSent";
     final static public String ACTIVITI_EVENT_IS_OPEN = "eventIsOpen";
     final static public String ACTIVITI_INVITATION_ACCEPTED = "invitationAccepted";
     final static public String ACTIVITI_WILL_BE_SUBSTITUTED = "willBeSubstituted";
     final static public String ACTIVITI_TAKES_FLIGHT = "takesFlight";
     final static public String ACTIVITI_TAKES_PREDEFINED_FLIGHT = "takesPredefinedFlight";
     final static public String ACTIVITI_SIGNAL_VARIABLES_DEFINED = "variablesDefined";
     final static public String ACTIVITI_INVITATION_STARTED = "invitationStarted";
     final static public String DATABASE_NEXT_PARTICIPANTID = "nextParticipantId";
     final static public String[] PROCESSES = {"InvitationProcess", "Journalist_Invitation_Response"};
     final static public String PROCESS_FILE_LOCATION = "diagrams/";
     final static public String SUFFIX = ".bpmn";
     private String processDefinition;
     private String processDefinitionId;
     private ProcessInstance processInstance;
     private ActivitiProcessHolder processHolder;
 
     public InvitationProcess(ActivitiProcessHolder processHolder, String processDefinition) {
         this.processHolder = processHolder;
         this.processDefinition = processDefinition;
 
         if (processHolder.getProcessId() != null) {
            setProcessInstance(
                    // problem returns null
                    Services.getRuntimeService().createProcessInstanceQuery().processInstanceId(processHolder.getProcessId().toString()).singleResult());
             System.out.println(processHolder.getProcessId().toString());
             setProcessDefinitionId(getProcessInstance().getProcessDefinitionId());
 
         } else {
             startProcess();
         }
     }
 
     public void startProcess() {
 
         setProcessInstance(Services.getRuntimeService().startProcessInstanceByKey(processDefinition));
 
 
         processHolder.setProcessId(Integer.valueOf(getProcessInstance().getProcessInstanceId()));
         //Progress wird verwendet um den Status des Prozesses zu kontrollieren
         processHolder.setProgress(10);
         // nur bei Hauptprozess
         if (processDefinition.equals(PROCESSES[0])) {
             setVariable(ACTIVITI_INVITATION_STARTED, false);
             PersistenceService.save(BmwEventController.class, processHolder);
         }
 
         if (processHolder instanceof BmwEvent) {
             BmwEvent event = (BmwEvent) processHolder;
             setVariable(DATABASE_EVENTID, processHolder.getId());
             setVariable(ACTIVITI_FOLLOW_UP_TIME, formatActivitiDate(event.getEndEventdate().getTime() + getDaysInMilliSeconds(event.getSendFollowup())));
         } else if (processHolder instanceof BmwParticipants) {
             setVariable(DATABASE_PARTICIPANTID, processHolder.getId());
         }
 
         setProcessDefinitionId(getProcessInstance().getProcessDefinitionId());
 
         System.out.println("Proccess Instance # " + processHolder.getProcessId() + " started");
     }
 
     public void setVariable(String name, Object value) {
         Services.getRuntimeService().setVariable(getProcessInstance().getId(), name, value);
     }
 
     public Object getVariable(String name) {
         return Services.getRuntimeService().getVariable(getProcessInstance().getId(), name);
     }
 
     public String getStartFormKey() {
 
         String processDefinitionId = Services.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(processDefinition).latestVersion().singleResult().getId();
         String formKey = Services.getFormService().getStartFormKey(processDefinitionId);
 
         System.out.println("start task with form " + formKey);
 
         return formKey;
     }
 
     public String getNextFormKey() {
 
 
         resumeProcess();
 
         String formKey;
 
         try {
             formKey = Services.getFormService().getTaskFormKey(processDefinitionId, getCurrentActivity());
 
             System.out.println("task with form " + formKey);
         } catch (org.activiti.engine.ActivitiIllegalArgumentException ex) {
             formKey = "noformkey";
         }
 
         return formKey;
     }
 
     public void resumeProcess() {
         System.out.println("Resume Process with Id " + processHolder.getProcessId());
         Services.getRuntimeService().signal(String.valueOf(processHolder.getProcessId()));
     }
 
     public boolean processStarted() {
         return processHolder.getProcessId() != null;
     }
 
     /**
      * @return the processDefinitionId
      */
     public String getProcessDefinitionId() {
         return processDefinitionId;
     }
 
     /**
      * @param processDefinitionId the processDefinitionId to set
      */
     public void setProcessDefinitionId(String processDefinitionId) {
         this.processDefinitionId = processDefinitionId;
     }
 
     /**
      * @return the currentActivity
      */
     public String getCurrentActivity() {
         try {
             return Services.getRuntimeService().createProcessInstanceQuery().processInstanceId(String.valueOf(processHolder.getProcessId())).singleResult().getActivityId();
 
         } catch (NullPointerException ex) {
             ex.printStackTrace();
             return null;
         }
     }
 
     /**
      * @return the processInstance
      */
     public ProcessInstance getProcessInstance() {
         return processInstance;
     }
 
     /**
      * @param processInstance the processInstance to set
      */
     public void setProcessInstance(ProcessInstance processInstance) {
         this.processInstance = processInstance;
     }
 
     public static InvitationProcess startSingleProcess(BmwEvent event, BmwParticipants participant) {
 
         InvitationProcess process = new InvitationProcess(participant, InvitationProcess.PROCESSES[1]);
         process.setVariable(DATABASE_EVENTID, event.getId());
 
         process.setVariable(InvitationProcess.ACTIVITI_REMINDER_SENT, false);
         process.setVariable(InvitationProcess.ACTIVITI_EVENT_IS_OPEN, true);
 
         process.setVariable(ACTIVITI_CANCEL_INVITATION_TIME, new Date());
         process.setVariable(ACTIVITI_INVITATION_SENT, false);
 
         participant.setProcessId(Integer.valueOf(process.getProcessInstance().getProcessInstanceId()));
         PersistenceService.save(BmwParticipantsController.class, participant);
 
         if(!signalEvent(process.getProcessInstance(), ACTIVITI_SIGNAL_VARIABLES_DEFINED)) {
 
             Services.getRuntimeService().signal(process.getProcessInstance().getId());
 
         }
 
         System.out.println("Start Single Process");
 
         return process;
     }
 
     public static boolean signalEvent(ProcessInstance processInstance, String reference) {
         System.out.println("searching signal event subscription " + reference);
         //System.out.println("Prozess-Instanz: " + processInstance.getId());
         boolean found = false;
         for (Execution exec : Services.getRuntimeService().createExecutionQuery().signalEventSubscriptionName(reference).list()) {
             if (exec.getProcessInstanceId().equals(processInstance.getProcessInstanceId())) {
                 Services.getRuntimeService().signalEventReceived(reference, exec.getId());
                 System.out.println("Event with signal event subscription \"" + reference + "\" fortgesetzt");
                 found = true;
             }
         }
 
         return found;
     }
 
     public static long getDueTime(BmwEvent event, BmwParticipants participant, int days) throws Exception {
         Long dueTime;
 
         if ((dueTime = participant.getInvitationDate().getTime() + getDaysInMilliSeconds(days)) > event.getCloseInvitation().getTime()) {
             dueTime = event.getCloseInvitation().getTime();
         }
 
         if (dueTime <= new Date().getTime()) {
             throw new Exception("Zeitpunkt bereits vorüber.");
         }
 
         return dueTime;
     }
 
     public static int getDaysInMilliSeconds(int days) {
         return days * 24 * 3600 * 1000;
     }
 
     public static String formatActivitiDate(Long time) {
         return new SimpleDateFormat("yyyyy-MM-dd HH:mm:ss").format(new Date(time)).replace(" ", "T"); // example: "2011-03-11T12:13:14"
     }
 
     public static BmwUser getNextParticipant(BmwEvent event) {
 
         EntityManager em = ((BmwParticipantsFacade) PersistenceService.getManagedBeanInstance(BmwParticipantsController.class).getFacade()).getEntityManager();
 
         Query userQuery = em.createQuery("select u from BmwUser ORDER BY u.rating DESC");
         userQuery.setParameter("eventId", event.getId());
 
         Query participantsQuery = em.createNamedQuery("BmwParticipants.findByEventId");
         List<BmwParticipants> participantsList = participantsQuery.getResultList();
 
         Iterator iter = userQuery.getResultList().iterator();
         while (iter.hasNext()) {
             BmwUser user = (BmwUser) iter.next();
             boolean alreadyInvited = false;
             for (BmwParticipants participant : participantsList) {
                 if (user.getUid() == participant.getUserId().getUid()) {
                     alreadyInvited = true;
                     break;
                 }
             }
             if (!alreadyInvited) {
                 return user;
             }
         }
 
         return null;
     }
 
     /**
      * @return the processHolder
      */
     public ActivitiProcessHolder getProcessHolder() {
         return processHolder;
     }
 
     /**
      * @param processHolder the processHolder to set
      */
     public void setProcessHolder(ActivitiProcessHolder processHolder) {
         this.processHolder = processHolder;
     }
 }
