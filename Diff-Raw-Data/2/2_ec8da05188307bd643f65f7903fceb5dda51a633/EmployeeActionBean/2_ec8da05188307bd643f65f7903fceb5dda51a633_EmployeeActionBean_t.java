 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pompe.crental.web;
 
 import cz.muni.fi.pompe.crental.dto.DTOEmployee;
 import cz.muni.fi.pompe.crental.dto.DTORent;
 import cz.muni.fi.pompe.crental.dto.DTORequest;
 import cz.muni.fi.pompe.crental.service.AbstractEmployeeService;
 import cz.muni.fi.pompe.crental.service.AbstractRentService;
 import cz.muni.fi.pompe.crental.service.AbstractRequestService;
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
 @UrlBinding("/employee/{$event}/{employee.id}")
 public class EmployeeActionBean extends BaseActionBean implements ValidationErrorHandler{
 
     final static Logger log = LoggerFactory.getLogger(EmployeeActionBean.class);
     
     @SpringBean
     private AbstractEmployeeService employeeService;
     
     @SpringBean
     private AbstractRequestService requestService;
     
     @SpringBean
     private AbstractRentService rentService;
     
     private List<DTOEmployee> employees;
 
     @ValidateNestedProperties(value = {
             @Validate(on = {"add", "save"}, field = "name", required = true, label = "Name"),
             @Validate(on = {"add", "save"}, field = "password", required = true, label = "Password"),
             @Validate(on = {"add", "save"}, field = "accessRight", required = true)
     })
     private DTOEmployee employee;
 
     public DTOEmployee getEmployee() {
         return employee;
     }
 
     public void setEmployee(DTOEmployee employee) {
         this.employee = employee;
     }
     
     public List<DTOEmployee> getEmployees() {
         return employees;
     }
     
     @DefaultHandler
     public Resolution list() {
         employees = employeeService.getAllEmployees();
         return new ForwardResolution("/employee/list.jsp");
     }
     
     @HandlesEvent("add")
     public Resolution add() {
         log.debug("add() employee={}", employee);
         employeeService.createEmployee(employee);
         log.debug("addded() employee={}", employee);
         getContext().getMessages().add(new LocalizableMessage("employee.add.message",escapeHTML(employee.getName())));
         return new RedirectResolution(this.getClass(), "list");
     }
     
     @ValidationMethod(when=ValidationState.NO_ERRORS, on={"add"})
     public void validateUniqueName() {
         
         if (employeeService.getEmployeeByName(employee.getName()) != null) {
             getContext().getValidationErrors().add("name", new LocalizableError("employee.name.unique"));
         }
     }
     
     @ValidationMethod(when=ValidationState.NO_ERRORS, on={"save"})
     public void validateUniqueNameOnSave() {
         DTOEmployee oldEmployee = employeeService.getEmployeeByName(employee.getName());
         
        if (oldEmployee != null && !oldEmployee.getId().equals(employee.getId())) {
             getContext().getValidationErrors().add("name", new LocalizableError("employee.name.unique"));
         }
     }
 
     @Override
     public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
         //fill up the data for the table if validation errors occured
         employees = employeeService.getAllEmployees();
         //return null to let the event handling continue
         return null;
     }
 
     //--- part for deleting a book ----
 
     @HandlesEvent("delete")
     public Resolution delete() {
         employee = employeeService.getEmployeeById(employee.getId());
         boolean findedDependency = false;
         for(DTORequest r : requestService.getAllRequests()){
             if(r.getEmployeeId().equals(employee.getId())){
                 getContext().getMessages().add(new LocalizableMessage("employee.validate.dependency"));       
                 findedDependency = true;
                 break;
             }
         }
         for(DTORent r : rentService.getAllRents()){
             if(r.getConfirmedById().equals(employee.getId())){
                 getContext().getMessages().add(new LocalizableMessage("employee.validate.dependency"));       
                 findedDependency = true;
                 break;
             }
         }
         if(!findedDependency){
             employeeService.deleteEmployee(employee);
             getContext().getMessages().add(new LocalizableMessage("employee.delete.message",escapeHTML(employee.getName())));
         }
         return new RedirectResolution(this.getClass(), "list");
     }
 
     @Before(stages = LifecycleStage.BindingAndValidation, on = {"edit", "save"})
     public void loadEmployeeFromDatabase() {
         String ids = getContext().getRequest().getParameter("employee.id");
         if (ids == null) return;
         employee = employeeService.getEmployeeById(Long.parseLong(ids));
     }
 
     @HandlesEvent("edit")
     public Resolution edit() {
         return new ForwardResolution("/employee/edit.jsp");
     }
 
     @HandlesEvent("save")
     public Resolution save() {
         employeeService.updateEmployee(employee);
         return new RedirectResolution(this.getClass(), "list");
     }
     
     @HandlesEvent("cancel")
     public Resolution cancel() {
         return new RedirectResolution(this.getClass(), "list");
     }
     
 }
