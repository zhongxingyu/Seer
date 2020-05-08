 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package CEMS.managedbean;
 
 import CEMS.entity.EventBookingEntity;
 import CEMS.session.EventBookingSessionBean;
 import CEMS.session.EventSessionBean;
 import Exception.ExistException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  *
  * @author Ser3na
  */
 @ManagedBean
 @ViewScoped
 public class EventServiceManagedBean {
 
     @EJB
     EventBookingSessionBean eventBookingSessionBean;
     @EJB
     EventSessionBean eventSessionBean;
     
     EventBookingEntity selectedEventBooking;
     List<EventBookingEntity> eventBookingList;
 
     /**
      * Creates a new instance of EventServiceManagedBean
      */
     public EventServiceManagedBean() {
         selectedEventBooking = new EventBookingEntity();
     }
 
     public List<EventBookingEntity> getAllEventBookings() throws ExistException {
         HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
         String userId = (String) request.getSession().getAttribute("userId");
 //        System.err.println("userId: "+userId);
         eventBookingList = new ArrayList<EventBookingEntity>();
         eventBookingList = eventBookingSessionBean.getManagerEvent(userId);
         return eventBookingList;
     }
     
     public void addService(ActionEvent event) throws IOException{
         FacesContext.getCurrentInstance().getExternalContext().getFlash().put("eventService", selectedEventBooking);
 //        System.err.println("add service: "+selectedEventBooking.getId());
         FacesContext.getCurrentInstance().getExternalContext().redirect("addEventService.xhtml");
     }
 
     public EventBookingSessionBean getEventBookingSessionBean() {
         return eventBookingSessionBean;
     }
 
     public void setEventBookingSessionBean(EventBookingSessionBean eventBookingSessionBean) {
         this.eventBookingSessionBean = eventBookingSessionBean;
     }
 
     public EventSessionBean getEventSessionBean() {
         return eventSessionBean;
     }
 
     public void setEventSessionBean(EventSessionBean eventSessionBean) {
         this.eventSessionBean = eventSessionBean;
     }
 
     public EventBookingEntity getSelectedEventBooking() {
         return selectedEventBooking;
     }
 
     public void setSelectedEventBooking(EventBookingEntity selectedEventBooking) {
         this.selectedEventBooking = selectedEventBooking;
     }
 
     public List<EventBookingEntity> getEventBookingList() {
         return eventBookingList;
     }
 
     public void setEventBookingList(List<EventBookingEntity> eventBookingList) {
         this.eventBookingList = eventBookingList;
     }
 }
