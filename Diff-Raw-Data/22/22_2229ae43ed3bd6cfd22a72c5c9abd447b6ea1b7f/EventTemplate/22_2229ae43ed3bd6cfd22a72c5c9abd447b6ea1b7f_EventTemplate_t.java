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
     private EmailTemplates manuel = new EmailTemplates();
     private EmailTemplates spezial = new EmailTemplates();
     private EmailTemplates booking = new EmailTemplates();
     private EmailTemplates ticket = new EmailTemplates();
     private EmailTemplates followup = new EmailTemplates();
     private EmailTemplates storno = new EmailTemplates();
     
     private EmailTemplatesController templateController;
     
     
     /**
      * Creates a new instance of CreateEventTemplate
      */
     public EventTemplate() {
         
         // FacesContext facesContext = FacesContext.getCurrentInstance();
         //this.eventID = (Integer) Integer.parseInt(facesContext.getExternalContext().getRequestParameterMap().get("eventID"));
     
         
         
     }
     
     public void createTemplate(BmwEvent e){
         templateController = PersistenceService.getManagedBeanInstance(EmailTemplatesController.class);
       
         //Erstellen der Einladungsemail an den Journalisten
         einladung.setEid(e);
         einladung.setSubject("Einladung BMW Event");
         einladung.setType("invite");
         einladung.setEmailContent("Sie sind hiermit eingeladen!");
         
         templateController.prepareCreate(null);
         templateController.setSelected(einladung);
         templateController.saveNew(null);
     
         //Erstellung der Urgenzmail an den Journalisten
         urgenz.setEid(e);
         urgenz.setSubject("Urgenzmail");
         urgenz.setType("urgenz");
         urgenz.setEmailContent("Sie sind hiermit Erinnert");
         
         templateController.prepareCreate(null);
         templateController.setSelected(urgenz);
         templateController.saveNew(null);
         
         //Erstellung der manuellen Nachladungsmail an den BMW Mitarbeiter
         manuel.setEid(e);
         manuel.setSubject("Manuelle Nachladung");
         manuel.setType("manuel");
         manuel.setEmailContent("Bitte Manuel nachladen!");
         
         templateController.prepareCreate(null);
         templateController.setSelected(manuel);
         templateController.saveNew(null);
         
         //Erstellung der speziellen Flugbuchungsmail an den BMW Mitarbeiter
         spezial.setEid(e);
         spezial.setSubject("Spezieller Flug");
         spezial.setType("spezial");
         spezial.setEmailContent("Bitte speziellen Flug buchen!");
         
         templateController.prepareCreate(null);
         templateController.setSelected(spezial);
         templateController.saveNew(null);
         
         //Erstellung der Flugbuchungsmail an das Reisebüro
         booking.setEid(e);
         booking.setSubject("Flug buchen");
         booking.setType("booking");
         booking.setEmailContent("Bitte Flug buchen!");
         
         templateController.prepareCreate(null);
         templateController.setSelected(booking);
         templateController.saveNew(null);
         
         
         //Erstellung der Ticketabholungsmail an den Journalisten
         ticket.setEid(e);
         ticket.setSubject("Ticket zur abholung bereit!");
         ticket.setType("ticket");
         ticket.setEmailContent("Ticket zum Download bereit!");
         
         templateController.prepareCreate(null);
         templateController.setSelected(ticket);
         templateController.saveNew(null);
         
         
         //Erstellung der Follow-up mail an den BMW Mitarbeiter
         followup.setEid(e);
         followup.setSubject("Follow-up E-mail");
         followup.setType("followup");
         followup.setEmailContent("Anbei finden Sie Eventinformationen");
         
         templateController.prepareCreate(null);
         templateController.setSelected(followup);
         templateController.saveNew(null);
         
         //Erstellung der Storno mail an den Journalisten
         storno.setEid(e);
         storno.setSubject("Stornierung der Einladung");
         storno.setType("storno");
         storno.setEmailContent("Ihre Einladung wurde somit storniert!");
         
         templateController.prepareCreate(null);
         templateController.setSelected(storno);
         templateController.saveNew(null);
     }
     
     
 }
