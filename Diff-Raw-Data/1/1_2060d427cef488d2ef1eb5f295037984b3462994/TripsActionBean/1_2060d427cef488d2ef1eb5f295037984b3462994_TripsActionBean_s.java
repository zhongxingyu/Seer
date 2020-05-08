 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pa165.jtravelagency.web;
 
 import cz.muni.fi.pa165.jtravelagency.dto.TripDTO;
 import cz.muni.fi.pa165.jtravelagency.facade.ServiceFacade;
 import static cz.muni.fi.pa165.jtravelagency.web.ExcursionsActionBean.pattern;
 import java.util.List;
 import net.sourceforge.stripes.action.Before;
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.LocalizableMessage;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import net.sourceforge.stripes.controller.LifecycleStage;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 import net.sourceforge.stripes.validation.ValidationErrorHandler;
 import net.sourceforge.stripes.validation.ValidationErrors;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Jakub Marecek
  */
 @UrlBinding("/trips/{$event}/{trip.id}")
 public class TripsActionBean extends BaseActionBean implements ValidationErrorHandler {
 
     final static Logger log = LoggerFactory.getLogger(TripsActionBean.class);
     
     final static String pattern = "dd.MM.yyyy HH:mm";
     
     @SpringBean
     protected ServiceFacade facade;
 
     private List<TripDTO> trips;
 
     @DefaultHandler
     public Resolution list() {
         log.debug("list()");
         trips = facade.getAllTrips();
         return new ForwardResolution("/trip/list.jsp");
     }
 
     public List<TripDTO> getTrips() {
         return trips;
     }
 
     @ValidateNestedProperties(value = {
             @Validate(on = {"add", "save"}, field = "destination", required = true),
             @Validate(on = {"add", "save"}, field = "availableTrips", required = true)
     })
     private TripDTO trip;
     @Validate(on = {"add", "save"}, required = true)
     private String dateFrom;
     @Validate(on = {"add", "save"}, required = true)
     private String dateTo;    
 
     public Resolution add() {
         log.debug("add() trip={}", trip);
         trip.setDateFrom(DateTime.parse(dateFrom, DateTimeFormat.forPattern(pattern)));
         trip.setDateTo(DateTime.parse(dateTo, DateTimeFormat.forPattern(pattern)));        
         facade.createTrip(trip);
         getContext().getMessages().add(new LocalizableMessage("trip.add.message",escapeHTML(trip.getDestination())));
         return new RedirectResolution(this.getClass(), "list");
     }
 
     @Override
     public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
         trips = facade.getAllTrips();
         return null;
     }
 
     public TripDTO getTrip() {
         return trip;
     }
 
     public void setTrip(TripDTO trip) {
         this.trip = trip;
     }
     
     public String getDateFrom() {
         return dateFrom;
     }
 
     public void setDateFrom(String dateFrom) {
         this.dateFrom = dateFrom;
     }
 
     public String getDateTo() {
         return dateTo;
     }
 
     public void setDateTo(String dateTo) {
         this.dateTo = dateTo;
     }
     
 
     public Resolution delete() {
         log.debug("delete({})", trip.getId());
         //only id is filled by the form
         trip = facade.getTrip(trip.getId());
         facade.deleteTrip(trip);
         getContext().getMessages().add(new LocalizableMessage("trip.delete.message",escapeHTML(trip.getDestination())));
         return new RedirectResolution(this.getClass(), "list");
     }
 
     @Before(stages = LifecycleStage.BindingAndValidation, on = {"edit", "save"})
     public void loadTripFromDatabase() {
         String ids = getContext().getRequest().getParameter("trip.id");
         if (ids == null) return;
         trip = facade.getTrip(Long.parseLong(ids));        
         dateFrom = trip.getDateFrom().toString(DateTimeFormat.forPattern(pattern));
         dateTo = trip.getDateTo().toString(DateTimeFormat.forPattern(pattern));
     }
 
     public Resolution edit() {
         log.debug("edit() trip={}", trip);
         return new ForwardResolution("/trip/edit.jsp");
     }
 
     public Resolution save() {
         log.debug("save() trip={}", trip);
         trip.setDateFrom(DateTime.parse(dateFrom, DateTimeFormat.forPattern(pattern)));
         trip.setDateTo(DateTime.parse(dateTo, DateTimeFormat.forPattern(pattern)));
         facade.updateTrip(trip);
         return new RedirectResolution(this.getClass(), "list");
     }
     
     public Resolution cancel() {
         return new RedirectResolution(this.getClass(), "list");
     }
     
 }
