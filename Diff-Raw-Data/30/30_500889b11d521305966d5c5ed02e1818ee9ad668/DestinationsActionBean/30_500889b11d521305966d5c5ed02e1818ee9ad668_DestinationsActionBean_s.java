 package cz.muni.fi.pa165.airportmanager.web.beans;
 
 import cz.muni.fi.pa165.airportmanager.services.DestinationService;
 import cz.muni.fi.pa165.airportmanager.transferobjects.DestinationTO;
 import java.util.List;
 import net.sourceforge.stripes.action.Before;
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.HandlesEvent;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import net.sourceforge.stripes.controller.LifecycleStage;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 import net.sourceforge.stripes.validation.ValidationErrorHandler;
 import net.sourceforge.stripes.validation.ValidationErrors;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Filip
  */
 @UrlBinding("/destinations/{$event}/{destination.id}")
 public class DestinationsActionBean extends BaseActionBean implements ValidationErrorHandler {
     
     final static Logger log = LoggerFactory.getLogger(DestinationsActionBean.class);
     
     @SpringBean
     protected DestinationService destinationService;
     
     private List<DestinationTO> destinations;
     
     private DestinationTO destination;
 
     public List<DestinationTO> getDestinations() {
         return destinations;
     }
     
     public DestinationTO getDestination() {
         return destination;
     }
 
     public void setDestination(DestinationTO destination) {
         this.destination = destination;
     }
     
    public Resolution createTest() {
        destination = new DestinationTO();
        destination.setCity("Brno");
        destination.setCode("BR");
        destination.setCountry("CZECH");
        destinationService.createDestination(destination);
        destinations = destinationService.getAllDestinations();
        return new ForwardResolution("/destination/list.jsp");
    }
    
     @DefaultHandler
     public Resolution list() {
         log.debug("list()");
         destinations = destinationService.getAllDestinations();
         return new ForwardResolution("/destination/list.jsp");
     }
 
     @Override
     public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
         //fill up the data for the table if validation errors occured
         destinations = destinationService.getAllDestinations();
         //return null to let the event handling continue
         return null;
     }
     
     public Resolution delete() {
         log.debug("delete({})", destination.getId());
         destinationService.removeDestination(destination);
         return new RedirectResolution(this.getClass(), "list");
     }
 
     @Before(stages = LifecycleStage.BindingAndValidation, on = {"save", "edit", "delete"})
     public void loadDestinationFromDatabase() {
         String ids = getContext().getRequest().getParameter("destination.id");
         if (ids == null) return;
         destination = destinationService.getDestination(Long.parseLong(ids));
     }
 
     public Resolution create() {
         log.debug("create() destination={}", destination);
         return new ForwardResolution("/destination/create.jsp");
     }
     
    @ValidateNestedProperties(value = {
            @Validate(on = {"add", "save"}, field = "country", required = true),
            @Validate(on = {"add", "save"}, field = "city", required = true),
            @Validate(on = {"add", "save"}, field = "code", required = true)
    })
     public Resolution add() {
         log.debug("add() destination={}", destination);
         destinationService.createDestination(destination);
         return new RedirectResolution(this.getClass(), "list");
     }
 
     public Resolution edit() {
         log.debug("edit() destination={}", destination);
         return new ForwardResolution("/destination/edit.jsp");
     }
     
     public Resolution save() {
         log.debug("save() destination={}", destination);
         destinationService.updateDestination(destination);
         return new RedirectResolution(this.getClass(), "list");
     }
 }
