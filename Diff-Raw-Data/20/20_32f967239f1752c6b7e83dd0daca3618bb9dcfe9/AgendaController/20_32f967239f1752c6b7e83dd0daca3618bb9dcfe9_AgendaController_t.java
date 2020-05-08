 package web;
 
 import ejb.AgendaBean;
 import entity.AgendaItem;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.inject.Inject;
 import javax.inject.Named;
 import org.primefaces.event.DateSelectEvent;
 import org.primefaces.event.ScheduleEntryMoveEvent;
 import org.primefaces.event.ScheduleEntryResizeEvent;
 import org.primefaces.event.ScheduleEntrySelectEvent;
 import org.primefaces.model.LazyScheduleModel;
 import org.primefaces.model.ScheduleEvent;
 import org.primefaces.model.ScheduleModel;
 import regular.VeloScheduleEvent;
 
 @Named
 @SessionScoped
 public class AgendaController implements Serializable
 {
     @Inject
     private UserController userController;
     
     @EJB
     private AgendaBean bean;
     private ScheduleModel agenda;
     private ScheduleEvent event = new VeloScheduleEvent();
         
     /**sets up the agenda **/
     @PostConstruct
     public void agendaInit()
     {
         agenda = new LazyScheduleModel()
         {
             @Override
             public void loadEvents(Date start, Date end)
             {
                 super.loadEvents(start, end);
                 clear();
                 
                 List<AgendaItem> items = getAgendaItems(start, end);
                 for(AgendaItem i : items)
                 {
                     VeloScheduleEvent tempEvent = new VeloScheduleEvent();
                     tempEvent.setOwnID(i.getId());
                     tempEvent.setTitle(i.getItemText());
                     tempEvent.setStartDate(i.getStartDate());
                     tempEvent.setEndDate(i.getEndDate());
                     
                     agenda.addEvent(tempEvent);
                 }  
             }
         };
     }
     
     
     /** Gets the agendaitems for this month for the logged in user
      * @param start the start date
      * @param end the end date
      * @return a list of the agendaitems for the logged in user
      */
     public List<AgendaItem> getAgendaItems(Date start, Date end)
     {
         return bean.getAgendaItems(start, end, userController.getLoggedInUser());
     }
     
     /** Updates an existing agendaitem, or adds a new agendaitem.
      * @param agendaItem  The agendaitem that must be updated or added.
      * @return  returns the new (updated) AgendaItem-object.
      */
     public AgendaItem addOrUpdate(AgendaItem agendaItem)
     {
         return bean.addOrUpdate(agendaItem);
     }
     
     
     public void addAppointment(ActionEvent actionEvent) 
     {  
         if(event.getId() == null) 
         {
             agenda.addEvent(event);
             
             AgendaItem item = new AgendaItem();
             item.setOwner(userController.getLoggedInUser());
             item.setItemText(event.getTitle());
             item.setStartDate(event.getStartDate());
             item.setEndDate(event.getEndDate());
             addOrUpdate(item);
         }  
         else {
             agenda.updateEvent(event);
             
             AgendaItem item = new AgendaItem();
             item.setId(((VeloScheduleEvent) event).getOwnID());
             item.setOwner(userController.getLoggedInUser());
             item.setItemText(event.getTitle());
             item.setStartDate(event.getStartDate());
             item.setEndDate(event.getEndDate());
             addOrUpdate(item);
         }  
           
         event = new VeloScheduleEvent();  
     }  
       
     public void onEventSelect(ScheduleEntrySelectEvent selectEvent) {  
         event = selectEvent.getScheduleEvent();  
         System.out.println("ID selected event " + event.getId());
     }  
       
     public void onDateSelect(DateSelectEvent selectEvent) {  
         event = new VeloScheduleEvent("", selectEvent.getDate(), selectEvent.getDate());  
     }  
       
     public void onEventMove(ScheduleEntryMoveEvent event) {  
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event verplaatst", "Event verplaatst");  
           
         addMessage(message);  
     }  
       
     public void onEventResize(ScheduleEntryResizeEvent event) {  
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event veranderd", "Event veranderd");  
           
         addMessage(message);  
     }  
       
     private void addMessage(FacesMessage message) {  
         FacesContext.getCurrentInstance().addMessage(null, message);  
     }
 
     
     public ScheduleModel getAgenda() {
         return agenda;
     }
     public void setAgenda(ScheduleModel sm) {
         this.agenda = sm;
     }
     public ScheduleEvent getEvent() {
         return event;
     } 
    public List<AgendaItem> getNextWeek()
     {
         Calendar calendar = Calendar.getInstance();
         Date start = calendar.getTime();
         
        calendar.add(Calendar.HOUR, 24*7);
         Date end = calendar.getTime();
                 
         System.out.println("Start: " + start.toString());
         System.out.println("End: "+ end.toString());
         
         getAgendaItems(start, end);
         return getAgendaItems(start, end);
     }
     
     public void setEvent(ScheduleEvent event) {
         this.event = event;
     }
 }
