 package cz.muni.fi.pompe.crental.web;
 
 import cz.muni.fi.pompe.crental.dto.DTOEmployee;
 import cz.muni.fi.pompe.crental.dto.DTORequest;
 import cz.muni.fi.pompe.crental.service.AbstractEmployeeService;
 import cz.muni.fi.pompe.crental.service.AbstractRequestService;
 import java.util.Date;
 import java.util.List;
 import net.sourceforge.stripes.action.Before;
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.HandlesEvent;
 import net.sourceforge.stripes.action.LocalizableMessage;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import net.sourceforge.stripes.controller.LifecycleStage;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.DateTypeConverter;
 import net.sourceforge.stripes.validation.LocalizableError;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 import net.sourceforge.stripes.validation.ValidationErrorHandler;
 import net.sourceforge.stripes.validation.ValidationErrors;
 import net.sourceforge.stripes.validation.ValidationMethod;
 import net.sourceforge.stripes.validation.ValidationState;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author jozef
  */
 @UrlBinding("/request/{$event}/{request.id}")
 public class RequestActionBean extends BaseActionBean implements ValidationErrorHandler{
     final static Logger log = LoggerFactory.getLogger(RequestActionBean.class);
     
     @SpringBean
     private AbstractRequestService requestService;
     
     @SpringBean
     private AbstractEmployeeService employeeService;
     
     private List<DTORequest> requests;
     
     private List<DTOEmployee> employees;
     
     @ValidateNestedProperties(value = {
         @Validate(on = {"add", "save"}, field = "dateFrom", required = true, converter = DateTypeConverter.class),
         @Validate(on = {"add", "save"}, field = "dateTo", required = true, converter = DateTypeConverter.class),
         @Validate(on = {"add", "save"}, field = "employeeId", required = true),
         @Validate(on = {"add", "save"}, field = "description", required = true)
     })
     private DTORequest request;
     
     public DTORequest getRequest() {
         return request;
     }
 
     public void setRequest(DTORequest request) {
         this.request = request;
     }
     
     public List<DTORequest> getRequests() {
         return requests;
     }
 
     public List<DTOEmployee> getEmployees() {
         return employees;
     }
 
     public void setEmployees(List<DTOEmployee> employees) {
         this.employees = employees;
     }
     
     @DefaultHandler
     public Resolution list() {
         return new ForwardResolution("/request/list.jsp");
     }
     
     @HandlesEvent("add")
     public Resolution add() {
         requestService.createRequest(request);
         getContext().getMessages().add(new LocalizableMessage("request.add.message" ));
         return new RedirectResolution(this.getClass(), "list");
     }
 
     @Override
     public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
         //fill up the data for the table if validation errors occured
         requests = requestService.getAllRequests();
         //return null to let the event handling continue
         return null;
     }
 
     //--- part for deleting a book ----
 
     @HandlesEvent("delete")
     public Resolution delete() {
         request = requestService.getRequestById(request.getId());
         requestService.deleteRequest(request);
         getContext().getMessages().add(new LocalizableMessage("request.delete.message"));
         return new RedirectResolution(this.getClass(), "list");
     }
     
    @Before(stages = LifecycleStage.BindingAndValidation, on = {"list", "edit", "add", "save"})
     public void loadRequestsEmployees() {
         employees = employeeService.getAllEmployees();
         requests = requestService.getAllRequests();
         String ids = getContext().getRequest().getParameter("request.id");
         if (ids == null) return;
         request = requestService.getRequestById(Long.parseLong(ids));
     }
     
     @HandlesEvent("edit")
     public Resolution edit() {
         return new ForwardResolution("/request/edit.jsp");
     }
 
     @HandlesEvent("save")
     public Resolution save() {
         requestService.updateRequest(request);
         return new RedirectResolution(this.getClass(), "list");
     }
     
     @HandlesEvent("cancel")
     public Resolution cancel() {
         return new RedirectResolution(this.getClass(), "list");
     }
     
     @ValidationMethod(when=ValidationState.NO_ERRORS, on={"add", "save"})
     public void validateDates() {
         if (request.getDateFrom().after(request.getDateTo())) {
             getContext().getValidationErrors().add("dateFrom", new LocalizableError("request.validate.datesMissMatch"));
         }
     }
     
     @ValidationMethod(when=ValidationState.NO_ERRORS, on={"save"})
     public void validateDateFrom() {
         Date today = new Date(); // TODO dnes 00
    
         if (request.getDateFrom().after(today)) {
             getContext().getValidationErrors().add("dateFrom", new LocalizableError("request.validate.dateFromPast"));
         }
     }
 }
