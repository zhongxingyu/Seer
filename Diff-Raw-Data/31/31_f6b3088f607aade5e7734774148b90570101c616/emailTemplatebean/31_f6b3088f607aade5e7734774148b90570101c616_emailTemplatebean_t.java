 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ac.at.fhkufstein.EventFunc;
 
 import ac.at.fhkufstein.bean.BmwEventController;
 import ac.at.fhkufstein.bean.EmailTemplatesController;
 import ac.at.fhkufstein.entity.BmwEvent;
 import ac.at.fhkufstein.entity.EmailTemplates;
 import ac.at.fhkufstein.session.EmailTemplatesFacade;
import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.persistence.EntityManager;
 
 /**
  *
  * @author Philipp
  */
 @ManagedBean(name="emailTemplateBean")
@ViewScoped
public class emailTemplatebean implements Serializable{
 
     private EmailTemplatesController emailController;
     private BmwEventController bmweventController;
     private BmwEvent bmwevent;
     private Integer eventID;
     private List<EmailTemplates> list;
     private String subject;
 
     public Integer getEventID() {
         return eventID;
     }
 
     public void setEventID(Integer eventID) {
         this.eventID = eventID;
     }
 
     public List<EmailTemplates> getList() {
         return list;
     }
 
     public void setList(List<EmailTemplates> list) {
         this.list = list;
     }
 
     public String getSubject() {
         return subject;
     }
 
     public void setSubject(String subject) {
         this.subject = subject;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public String getEmailContent() {
         return emailContent;
     }
 
     public void setEmailContent(String emailContent) {
         this.emailContent = emailContent;
     }
     private String type;
     private String emailContent;
     
     /**
      * Creates a new instance of emailTemplatebean
      */
     public emailTemplatebean() {
         
         emailController = FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{emailTemplatesController}", EmailTemplatesController.class);
         bmweventController = FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{bmwEventController}", BmwEventController.class);
         
         FacesContext facesContext = FacesContext.getCurrentInstance();
         this.eventID = (Integer) Integer.parseInt(facesContext.getExternalContext().getRequestParameterMap().get("eventID"));
 
         bmwevent = bmweventController.getFacade().find(eventID);
         
         try {
 
 			EntityManager em = ((EmailTemplatesFacade) emailController.getFacade()).getEntityManager();
                         list = em.createNamedQuery("EmailTemplates.findByEventId")
 					.setParameter("eid", bmwevent)
 					.getResultList();
 			
 		} catch (Exception ex) {
 			System.out.println(ex.getMessage());
 		}
         
     }
     
     public void saveNew(){
 		
 				EmailTemplates el = emailController.prepareCreate(null);//Save new Flight
 				el.setEid(bmwevent);//Correct Event ID
 				el.setEmailContent(emailContent);
                                 el.setSubject(subject);
                                 el.setType(type);
                                
                                 
                                 
 				emailController.setSelected(el);
 				emailController.saveNew(null);//save to database
 				
 				FacesContext context = FacesContext.getCurrentInstance();
 
 				context.addMessage(null, new FacesMessage("Email Template hinzugefügt", "Aktualisieren sie die Seite um die Änderungen zu sehen")); //Send message 
 				
 	}
     
     
     
     
     
   
 }
