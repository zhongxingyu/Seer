 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pompe.crental.web;
 
 import cz.muni.fi.pompe.crental.dto.DTOEmployee;
import cz.muni.fi.pompe.crental.service.AbstractEmployeeService;
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
     
     private List<DTOEmployee> employees;
 
     @ValidateNestedProperties(value = {
             @Validate(on = {"add", "save"}, field = "name", required = true),
             @Validate(on = {"add", "save"}, field = "password", required = true),
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
     
     public Resolution add() {
         log.debug("add() employee={}", employee);
        employeeService.createEmployee(employee);
         log.debug("addded() employee={}", employee);
         getContext().getMessages().add(new LocalizableMessage("employee.add.message",escapeHTML(employee.getName())));
         return new RedirectResolution(this.getClass(), "list");
     }
 
     @Override
     public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
         //fill up the data for the table if validation errors occured
        employees = employeeService.getAllEmployees();
         //return null to let the event handling continue
         return null;
     }
 
     //--- part for deleting a book ----
 
     public Resolution delete() {
         //only id is filled by the form
        employee = employeeService.getEmployeeById(employee.getId());
        employeeService.deleteEmployee(employee);
         getContext().getMessages().add(new LocalizableMessage("employee.delete.message",escapeHTML(employee.getName())));
         return new RedirectResolution(this.getClass(), "list");
     }
 
     //--- part for editing a book ----
 
     @Before(stages = LifecycleStage.BindingAndValidation, on = {"edit", "save"})
     public void loadBookFromDatabase() {
         String ids = getContext().getRequest().getParameter("employee.id");
         if (ids == null) return;
        employee = employeeService.getEmployeeById(Long.parseLong(ids));
     }
 
     public Resolution edit() {
         return new ForwardResolution("/book/edit.jsp");
     }
 
     public Resolution save() {
        employeeService.updateEmployee(employee);
         return new RedirectResolution(this.getClass(), "list");
     }
     
     public Resolution cancel() {
         return new RedirectResolution(this.getClass(), "list");
     }
     
 }
