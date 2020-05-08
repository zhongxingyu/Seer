 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ac.at.fhkufstein.EventFunc;
 
 import ac.at.fhkufstein.bean.BmwEventController;
 import ac.at.fhkufstein.entity.BmwEvent;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 
 /**
  *
  * @author Philipp
  */
 @ManagedBean(name = "status")
 @ViewScoped
public class Eventstatus {
 
     final static public Integer EVENT_STATUS_CREATED = 10;
     final static public Integer EVENT_STATUS_JOURNALIST_INVITE = 20;
     final static public Integer EVENT_STATUS_RELEASE = 40;
     final static public Integer EVENT_STATUS_EVENT_END = 60;
     final static public Integer EVENT_STATUS_FOLLOWUP = 80;
     final static public Integer EVENT_STATUS_FINISHED = 100;
     private BmwEventController bmwEventController;
     private Integer eventID;
     private BmwEvent current;
     private boolean created = Boolean.FALSE;
     private boolean invited = Boolean.FALSE;
     private boolean released = Boolean.FALSE;
     private boolean end = Boolean.FALSE;
     private boolean followup = Boolean.FALSE;
     private boolean finished = Boolean.FALSE;
     
     
 
     /**
      * Creates a new instance of Eventstatus
      */
     public Eventstatus() {
 
         try {
             //Instanciate Controllers
             bmwEventController = FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{bmwEventController}", BmwEventController.class);
             //Get eventID from URL
             FacesContext facesContext = FacesContext.getCurrentInstance();
             this.eventID = (Integer) Integer.parseInt(facesContext.getExternalContext().getRequestParameterMap().get("eventID"));
 
             //Get Current Event for eventID
             current = bmwEventController.getFacade().find(eventID);
 
         } catch (Exception ex) {
             System.out.println(ex.getMessage());
         }
 
 
     }
 
     public boolean isCreated() {
         if(current.getProgress() == EVENT_STATUS_CREATED){
             created = Boolean.TRUE;
         }
         return created;
     }
 
     public boolean isInvited() {
         if(current.getProgress() == EVENT_STATUS_JOURNALIST_INVITE){
             created = Boolean.TRUE;
         }
         return invited;
     }
 
     public boolean isReleased() {
         if(current.getProgress() == EVENT_STATUS_RELEASE){
             created = Boolean.TRUE;
         }
         return released;
     }
 
     public boolean isEnd() {
         if(current.getProgress() == EVENT_STATUS_EVENT_END){
             created = Boolean.TRUE;
         }
         return end;
     }
 
     public boolean isFollowup() {
         if(current.getProgress() == EVENT_STATUS_FOLLOWUP){
             created = Boolean.TRUE;
         }
         return followup;
     }
 
     public boolean isFinished() {
         if(current.getProgress() == EVENT_STATUS_FINISHED){
             created = Boolean.TRUE;
         }
         return finished;
     }
     
     
     
     
 }
