 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ESMS.managedBean;
 
 import ESMS.entity.ShowEntity;
 import ESMS.entity.ShowScheduleEntity;
 import ESMS.entity.ShowTicketEntity;
 import ESMS.session.ShowScheduleSessionBean;
 import ESMS.session.ShowSessionBean;
 import ESMS.session.ShowTicketSessionBean;
 import java.io.IOException;
 import java.util.Date;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  *
  * @author Ser3na
  */
 @ManagedBean
 @RequestScoped
 public class AddShowManagedBean {
 
     @EJB
     ShowSessionBean showSessionBean;
     @EJB
     ShowScheduleSessionBean showScheduleSessionBean;
     @EJB
     ShowTicketSessionBean showTicketSessionBean;
     private ShowEntity show;
     private ShowScheduleEntity showSchedule;
     private ShowTicketEntity showTicket;
     private Long showId;
     private Date currentDate = new Date();
 
     public AddShowManagedBean() {
         show = new ShowEntity();
         showSchedule = new ShowScheduleEntity();
         showTicket = new ShowTicketEntity();
     }
 
 //    public void handleFileUpload(FileUploadEvent event) {  
 //        FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");  
 //        FacesContext.getCurrentInstance().addMessage(null, msg);  
 //    }
     public void saveNewShow(ActionEvent event) throws IOException {
         System.err.println("Saving New Show...");
         showSessionBean.addShow(getShow());
 
         HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
         request.getSession().setAttribute("showId", Long.valueOf(getShow().getShowId()));
 
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "New Show saved.", ""));
         FacesContext.getCurrentInstance().getExternalContext().redirect("addShow.xhtml");
     }
 
     public void saveNewShowSchedule(ActionEvent event) throws IOException {
         System.err.println("Saving New Show Schedule...");
         showScheduleSessionBean.addShowSchedule(getShowSchedule());
         System.err.println(showId);
         System.err.println(showSchedule.getShowDate());
         showSessionBean.addShowSchedule(showId, showSchedule);
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "New Show Schedule saved.", ""));
         FacesContext.getCurrentInstance().getExternalContext().redirect("addShow.xhtml");
     }
 
     public void saveNewShowTicket(ActionEvent event) throws IOException {
         System.err.println("Saving New Show Ticket...");
         showTicketSessionBean.addShowTicket(getShowTicket());
         showSessionBean.addShowTicket(showId, showTicket);
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "New Show Ticket saved.", ""));
         FacesContext.getCurrentInstance().getExternalContext().redirect("addShow.xhtml");
     }
 
     public void oneMore(ActionEvent event) throws IOException {
         FacesContext.getCurrentInstance().getExternalContext().redirect("addShow.xhtml");
     }
 
     //Getter and Setters
     public Date getCurrentDate() {
         return currentDate;
     }
 
     public void setCurrentDate(Date currentDate) {
         this.currentDate = currentDate;
     }
 
     public Long getShowId() {
         return showId;
     }
 
     public void setShowId(Long showId) {
         this.showId = showId;
     }
 
     public ShowEntity getShow() {
         return show;
     }
 
     public void setShow(ShowEntity show) {
         this.show = show;
     }
 
     public ShowScheduleEntity getShowSchedule() {
         return showSchedule;
     }
 
     public void setShowSchedule(ShowScheduleEntity showSchedule) {
         this.showSchedule = showSchedule;
     }
 
     public ShowSessionBean getShowSessionBean() {
         return showSessionBean;
     }
 
     public void setShowSessionBean(ShowSessionBean showSessionBean) {
         this.showSessionBean = showSessionBean;
     }
 
     public ShowScheduleSessionBean getShowScheduleSessionBean() {
         return showScheduleSessionBean;
     }
 
     public void setShowScheduleSessionBean(ShowScheduleSessionBean showScheduleSessionBean) {
         this.showScheduleSessionBean = showScheduleSessionBean;
     }
 
     public ShowTicketSessionBean getShowTicketSessionBean() {
         return showTicketSessionBean;
     }
 
     public void setShowTicketSessionBean(ShowTicketSessionBean showTicketSessionBean) {
         this.showTicketSessionBean = showTicketSessionBean;
     }
 
     public ShowTicketEntity getShowTicket() {
         return showTicket;
     }
 
     public void setShowTicket(ShowTicketEntity showTicket) {
         this.showTicket = showTicket;
     }
 }
