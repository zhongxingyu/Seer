 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ac.at.fhkufstein.bean.process;
 
 import ac.at.fhkufstein.activiti.InvitationProcess;
 import static ac.at.fhkufstein.activiti.InvitationProcess.ACTIVITI_SIGNAL_VARIABLES_DEFINED;
 import static ac.at.fhkufstein.activiti.InvitationProcess.signalEvent;
 import ac.at.fhkufstein.bean.BmwEventController;
 import ac.at.fhkufstein.entity.BmwEvent;
 import ac.at.fhkufstein.mailing.EventTemplate;
 import ac.at.fhkufstein.service.MessageService;
 import ac.at.fhkufstein.service.PersistenceService;
 import ac.at.fhkufstein.session.BmwEventFacade;
 import java.io.Serializable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.Resource;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.persistence.EntityManager;
 import javax.transaction.UserTransaction;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.transaction.HeuristicMixedException;
 import javax.transaction.HeuristicRollbackException;
 import javax.transaction.NotSupportedException;
 import javax.transaction.RollbackException;
 import javax.transaction.Status;
 import javax.transaction.SystemException;
 import javax.transaction.Transaction;
 
 /**
  *
  * @author mike
  */
 @ManagedBean(name = "processBmwEventController")
 @ViewScoped
 public class ProcessBmwEventController implements Serializable {
 
     private static final String ACTIVITI_RELEASE_ACTIVITY = "releaseEvent";
     @Resource
     UserTransaction ut;
 
     public void saveNew(ActionEvent event) {
         try {
 
             final BmwEventController eventController = PersistenceService.getManagedBeanInstance(BmwEventController.class);
 
             // to get the id of the inserted event immediately a transacion has to be executed
             UserTransaction transaction = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
             if(transaction.getStatus() == Status.STATUS_NO_TRANSACTION) {
                 transaction.begin();
             }
 
             try {
                 EntityManager em = ((BmwEventFacade) eventController.getFacade()).getEntityManager();
                 em.persist(eventController.getSelected());
                 transaction.commit();
             } catch (Exception ex) {
                 Logger.getLogger(ProcessBmwEventController.class.getName()).log(Level.SEVERE, null, ex);
                 transaction.rollback();
             }
             EventTemplate a = PersistenceService.getManagedBeanInstance(EventTemplate.class);
             a.createTemplate(eventController.getSelected());
 
 
             // starting activiti process
             startEventProcess(eventController.getSelected());
 
 
             FacesContext context = FacesContext.getCurrentInstance();
             context.addMessage(null, new FacesMessage("Event erstellen", "Event erfolgreich erstellt!"));
 
         } catch (NamingException ex) {
             Logger.getLogger(ProcessBmwEventController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (NotSupportedException ex) {
             Logger.getLogger(ProcessBmwEventController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SystemException ex) {
             Logger.getLogger(ProcessBmwEventController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SecurityException ex) {
             Logger.getLogger(ProcessBmwEventController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalStateException ex) {
             Logger.getLogger(ProcessBmwEventController.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     public void save(ActionEvent event) {
 
         BmwEventController eventController = PersistenceService.getManagedBeanInstance(BmwEventController.class);
 
         eventController.save(event);
 
         if (eventController.getSelected().getProcessId() == null) {
             startEventProcess(eventController.getSelected());
         }
 
     }
 
     private void startEventProcess(BmwEvent event) {
         try {
 
             if (event.getId() == null) {
                 throw new Exception("Das Event wurde noch nicht gespeichert.");
             }
 
             InvitationProcess process = new InvitationProcess(event, InvitationProcess.PROCESSES[0]);
             signalEvent(process.getProcessInstance(), ACTIVITI_SIGNAL_VARIABLES_DEFINED);
 
             MessageService.showInfo(FacesContext.getCurrentInstance(), "Der Prozess für dieses Event wurde gestartet.");
         } catch (Exception ex) {
             Logger.getLogger(ProcessBmwEventController.class.getName()).log(Level.SEVERE, null, ex);
 
             MessageService.showError(FacesContext.getCurrentInstance(), "Der Prozess für dieses Event konnte nicht gestartet werden.");
 
         }
     }
 
     public void release(BmwEvent event) {
 
 
         InvitationProcess process = new InvitationProcess(event, InvitationProcess.PROCESSES[0]);
        System.out.println("Current ProzessID: " + process.getCurrentActivity());
         if (process.getCurrentActivity() != null && process.getCurrentActivity().equals(ACTIVITI_RELEASE_ACTIVITY)) {
 
             event.setReleased(true);
             PersistenceService.save(BmwEventController.class, event);
 
             process.resumeProcess();
 
             MessageService.showInfo(FacesContext.getCurrentInstance(), "Das Event wurde freigegeben.");
             MessageService.showInfo(FacesContext.getCurrentInstance(), "Der Prozess wurde fortgefahren.");
         } else {
             MessageService.showError(FacesContext.getCurrentInstance(), "Das Event konnte nicht freigegeben werden.");
             MessageService.showError(FacesContext.getCurrentInstance(), "Der Prozess konnte nicht fortgesetzt werden.");
         }
     }
 }
