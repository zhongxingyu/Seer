 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package CEMS.managedbean;
 
 import CEMS.entity.EventBookingEntity;
 import CEMS.entity.EventServiceBookingEntity;
 import CEMS.entity.EventServiceEntity;
 import CEMS.session.EventServiceBookingSessionBean;
 import CEMS.session.EventServiceSessionBean;
 import Exception.ExistException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.PhaseEvent;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  *
  * @author Ser3na
  */
 @ManagedBean
 @ViewScoped
 public class AddEventServiceManagedBean {
 
     @EJB
     EventServiceBookingSessionBean eventServiceBookingSessionBean;
     @EJB
     EventServiceSessionBean eventServiceSessionBean;
     EventBookingEntity eventBooking;
     EventServiceBookingEntity eventServiceBooking;
     EventServiceEntity eventService;
     List<EventServiceBookingEntity> eventServiceBookingList;
     Long serviceId;
     int serviceQuantity;
     
     public AddEventServiceManagedBean() {
         eventBooking = new EventBookingEntity();
         eventServiceBooking = new EventServiceBookingEntity();
         eventService = new EventServiceEntity();
     }
 
     public void init(PhaseEvent event) {
         eventBooking = new EventBookingEntity();
         eventBooking = (EventBookingEntity) FacesContext.getCurrentInstance().getExternalContext().getFlash().get("eventService");
        System.err.println("eventBooking: ...: "+eventBooking.getId());
         doThis();
     }
 
     public void doThis() {
         HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
         eventServiceBookingList = new ArrayList<EventServiceBookingEntity>();
         eventServiceBookingList = eventServiceBookingSessionBean.getBookingById(eventBooking.getId());
         request.getSession().setAttribute("eventServiceBooking", eventServiceBookingList);
         request.getSession().setAttribute("eventBooking", eventBooking);
     }
 
     public List<EventServiceBookingEntity> getEventServiceBookings() throws ExistException {
         HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
         eventServiceBookingList = new ArrayList<EventServiceBookingEntity>();
         eventServiceBookingList = (List<EventServiceBookingEntity>) request.getSession().getAttribute("eventServiceBooking");
         return eventServiceBookingList;
     }
     
     public void saveNew(ActionEvent event) throws IOException{
         HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
         eventBooking = new EventBookingEntity();
         eventBooking = (EventBookingEntity) request.getSession().getAttribute("eventBooking");
        System.err.println("eventBooking: "+eventBooking.getId());
         eventServiceBooking =  new EventServiceBookingEntity();
         eventServiceBooking.setEventBooking(eventBooking);
         eventServiceBooking.setEventService(eventServiceSessionBean.getEventServiceById(serviceId));
        System.err.println("eventService: "+eventServiceSessionBean.getEventServiceById(serviceId).getServiceName());
         eventServiceBooking.setEventServiceQuantity(serviceQuantity);
        System.err.println("serviceQuantity: "+serviceQuantity);
         eventServiceBookingSessionBean.addEventServiceBooking(eventServiceBooking);
         FacesContext.getCurrentInstance().getExternalContext().redirect("eventService.xhtml");
     }
 
     public List<EventServiceEntity> getAllEventServices() {
         return eventServiceSessionBean.getAllEventServices();
         
     }
 
     public EventServiceBookingSessionBean getEventServiceBookingSessionBean() {
         return eventServiceBookingSessionBean;
     }
 
     public void setEventServiceBookingSessionBean(EventServiceBookingSessionBean eventServiceBookingSessionBean) {
         this.eventServiceBookingSessionBean = eventServiceBookingSessionBean;
     }
 
     public EventBookingEntity getEventBooking() {
         return eventBooking;
     }
 
     public void setEventBooking(EventBookingEntity eventBooking) {
         this.eventBooking = eventBooking;
     }
 
     public EventServiceBookingEntity getEventServiceBooking() {
         return eventServiceBooking;
     }
 
     public void setEventServiceBooking(EventServiceBookingEntity eventServiceBooking) {
         this.eventServiceBooking = eventServiceBooking;
     }
 
     public EventServiceEntity getEventService() {
         return eventService;
     }
 
     public void setEventService(EventServiceEntity eventService) {
         this.eventService = eventService;
     }
 
     public List<EventServiceBookingEntity> getEventServiceBookingList() {
         return eventServiceBookingList;
     }
 
     public void setEventServiceBookingList(List<EventServiceBookingEntity> eventServiceBookingList) {
         this.eventServiceBookingList = eventServiceBookingList;
     }
 
     public Long getServiceId() {
         return serviceId;
     }
 
     public void setServiceId(Long serviceId) {
         this.serviceId = serviceId;
     }
 
     public EventServiceSessionBean getEventServiceSessionBean() {
         return eventServiceSessionBean;
     }
 
     public void setEventServiceSessionBean(EventServiceSessionBean eventServiceSessionBean) {
         this.eventServiceSessionBean = eventServiceSessionBean;
     }
 
     public int getServiceQuantity() {
         return serviceQuantity;
     }
 
     public void setServiceQuantity(int serviceQuantity) {
         this.serviceQuantity = serviceQuantity;
     }
 }
