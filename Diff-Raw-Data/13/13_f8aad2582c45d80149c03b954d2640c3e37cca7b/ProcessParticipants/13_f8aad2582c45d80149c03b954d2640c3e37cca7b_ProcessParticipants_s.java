 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ac.at.fhkufstein.bean.process;
 
 import ac.at.fhkufstein.EventFunc.Participants;
 import ac.at.fhkufstein.activiti.InvitationProcess;
 import ac.at.fhkufstein.entity.BmwEvent;
 import ac.at.fhkufstein.entity.BmwParticipants;
 import ac.at.fhkufstein.service.MessageService;
 import ac.at.fhkufstein.service.PersistenceService;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 
 /**
  *
  * @author mike
  */
 @ManagedBean(name = "processParticipants")
 @ViewScoped
public class ProcessParticipants {
 
     private final String ACTIVITI_ADD_ACTIVITY = "addJournalists";
     private final String ACTIVITI_SELECTIVE_INVITATION = "selectiveInvitation";
 
     public void confirmInvitedParticipants(BmwEvent event) {
 
         // @todo Invite Participants which have been invited after Start
 
         InvitationProcess process = new InvitationProcess(event, InvitationProcess.PROCESSES[0]);
 
         if(process.getCurrentActivity() != null && process.getCurrentActivity().equals(ACTIVITI_ADD_ACTIVITY)) {
 
             process.resumeProcess();
 
             MessageService.showInfo(FacesContext.getCurrentInstance(), "Der Prozess wird fortgefahren.");
         } else {
             MessageService.showError(FacesContext.getCurrentInstance(), "Der Prozess kann nicht fortgesetzt werden." + (process.getCurrentActivity() != null ? " current Activity: "+process.getCurrentActivity()  : ""));
         }
     }
 
     public void inviteParticpantManually(BmwEvent event, BmwParticipants participant) {
 
         InvitationProcess process = new InvitationProcess(event, InvitationProcess.PROCESSES[0]);
         if((Boolean) process.getVariable(InvitationProcess.ACTIVITI_INVITATION_STARTED)) {
             // Teilnehmer wird eingeladen
             InvitationProcess.startSingleProcess(event, participant);
 
             MessageService.showError(FacesContext.getCurrentInstance(), "Der Teilnehmer " + participant + " wurde nachgeladen.");
 
         }
     }
 
     public void confirmSelectiveInvitation(BmwParticipants participant, BmwParticipants invitedParticipant) {
 
         InvitationProcess process = new InvitationProcess(participant, InvitationProcess.PROCESSES[1]);
         if(process.getCurrentActivity() != null && process.getCurrentActivity().equals(ACTIVITI_SELECTIVE_INVITATION)) {
 
 
             process.setVariable(InvitationProcess.DATABASE_NEXT_PARTICIPANTID, invitedParticipant.getId());
 
             process.resumeProcess();
 
             MessageService.showInfo(FacesContext.getCurrentInstance(), "Der Prozess wird fortgefahren.");
         } else {
             MessageService.showError(FacesContext.getCurrentInstance(), "Der Prozess kann nicht fortgesetzt werden." + (process.getCurrentActivity() != null ? " current Activity: "+process.getCurrentActivity()  : ""));
         }
 
     }
 }
