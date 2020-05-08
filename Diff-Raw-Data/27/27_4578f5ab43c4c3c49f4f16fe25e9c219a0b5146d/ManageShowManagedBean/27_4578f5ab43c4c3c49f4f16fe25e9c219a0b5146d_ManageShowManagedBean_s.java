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
 import Exception.ExistException;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import org.primefaces.event.RowEditEvent;
 
 /**
  *
  * @author Ser3na
  */
 @ManagedBean
 @ViewScoped
 public class ManageShowManagedBean {
 
     @EJB
     private ShowSessionBean showSessionBean;
     @EJB
     private ShowScheduleSessionBean showScheduleSessionBean;
     @EJB
     private ShowTicketSessionBean showTicketSessionBean;
     private ShowEntity selectedShow;
     private ShowScheduleEntity selectedShowSchedule;
     private ShowTicketEntity selectedShowTicket;
     private boolean editMode;
     private boolean editSchedule;
     private Long id;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     /**
      * Creates a new instance of ManageShowManagedBean
      */
     public ManageShowManagedBean() {
         selectedShow = new ShowEntity();
     }
    
     public void onEdit(RowEditEvent event) {
         FacesMessage msg = new FacesMessage("Schedule Edited", null);
         showScheduleSessionBean.updateShowSchedule(selectedShowSchedule);
         FacesContext.getCurrentInstance().addMessage(null, msg);
     }
 
     public void onCancel(RowEditEvent event) {
         FacesMessage msg = new FacesMessage("Schedule Edit Cancelled", null);
 
         FacesContext.getCurrentInstance().addMessage(null, msg);
     }
 
     public boolean isEditMode() {
         return editMode;
     }
 
     public List<ShowEntity> getShows() throws ExistException {
         return showSessionBean.getAllShows();
     }
 
     public void deleteShow(ActionEvent event) {
         setId((Long) event.getComponent().getAttributes().get("code1"));
         showSessionBean.deleteShow(getId());
     }
 
     public void deleteShowSchedule(ActionEvent event) {
         setId((Long) event.getComponent().getAttributes().get("code2"));
         showScheduleSessionBean.deleteShowSchedule(getId());
     }
 
     public void deleteShowTicket(ActionEvent event) {
         setId((Long) event.getComponent().getAttributes().get("code3"));
         showTicketSessionBean.deleteShowTicket(getId());
     }
 
     public void saveChanges(ActionEvent event) {
         showSessionBean.updateShow(getSelectedShow());
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Changes saved.", ""));
     }
 
     /**
      * @return the selectedShow
      */
     public ShowEntity getSelectedShow() {
         return selectedShow;
     }
 
     /**
      * @param selectedShow the selectedShow to set
      */
     public void setSelectedShow(ShowEntity selectedShow) {
         this.selectedShow = selectedShow;
     }
 
     /**
      * @param editMode the editMode to set
      */
     public void setEditMode(boolean editMode) {
         this.editMode = editMode;
     }
 
     public ShowSessionBean getShowSessionBean() {
         return showSessionBean;
     }
 
     public void setShowSessionBean(ShowSessionBean showSessionBean) {
         this.showSessionBean = showSessionBean;
     }
 
     public ShowScheduleEntity getSelectedShowSchedule() {
         return selectedShowSchedule;
     }
 
     public void setSelectedShowSchedule(ShowScheduleEntity selectedShowSchedule) {
         this.selectedShowSchedule = selectedShowSchedule;
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
 
     public ShowTicketEntity getSelectedShowTicket() {
         return selectedShowTicket;
     }
 
     public void setSelectedShowTicket(ShowTicketEntity selectedShowTicket) {
         this.selectedShowTicket = selectedShowTicket;
     }
 
     public boolean isEditSchedule() {
         System.err.println(editSchedule);
         return editSchedule;
     }
 
     public void setEditSchedule(boolean editSchedule) {
         this.editSchedule = editSchedule;
     }
 }
