 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ac.at.fhkufstein.mailing;
 
 import ac.at.fhkufstein.bean.EmailTemplatesController;
 import ac.at.fhkufstein.entity.BmwEvent;
 import ac.at.fhkufstein.entity.EmailTemplates;
 import ac.at.fhkufstein.service.PersistenceService;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.persistence.Persistence;
 
 /**
  *
  * @author Philipp
  */
 @ManagedBean(name="eventTamplate")
 @RequestScoped
 public class EventTemplate {
 
     private EmailTemplates einladung = new EmailTemplates();
     private EmailTemplates urgenz = new EmailTemplates();
     private EmailTemplates followup = new EmailTemplates();
     
     private EmailTemplatesController templateController;
     
     
     /**
      * Creates a new instance of CreateEventTemplate
      */
     public EventTemplate() {
         
           FacesContext facesContext = FacesContext.getCurrentInstance();
         //this.eventID = (Integer) Integer.parseInt(facesContext.getExternalContext().getRequestParameterMap().get("eventID"));
     
         
         
     }
     
     public void createTemplate(BmwEvent e){
         templateController = PersistenceService.getManagedBeanInstance(EmailTemplatesController.class);
       
         
         einladung.setEid(e);
         einladung.setSubject("Einladung BMW Event");
         einladung.setType("invite");
         einladung.setEmailContent("Sie sind hiermit eingeladen!");
         
         templateController.prepareCreate(null);
         templateController.setSelected(einladung);
         templateController.saveNew(null);
     
         urgenz.setEid(e);
         urgenz.setSubject("Urgenzmail");
         urgenz.setType("urgenz");
         urgenz.setEmailContent("Sie sind hiermit Erinnert");
         
         templateController.prepareCreate(null);
         templateController.setSelected(urgenz);
         templateController.saveNew(null);
         
         followup.setEid(e);
         followup.setSubject("Follow-up");
         followup.setType("followup");
         followup.setEmailContent("Anbei finden Sie Eventinformationen");
         
         templateController.prepareCreate(null);
        templateController.setSelected(followup);
         templateController.saveNew(null);
     }
     
     
 }
