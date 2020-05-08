 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pa165.web;
 
 import cz.muni.fi.pa165.library.dtos.CustomerTO;
 import cz.muni.fi.pa165.library.services.CustomerService;
 import java.util.List;
 import net.sourceforge.stripes.action.Before;
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import net.sourceforge.stripes.controller.LifecycleStage;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidationErrors;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 import net.sourceforge.stripes.validation.ValidationErrorHandler;
 
 /**
  *
  * @author Matej
  */
 public class CustomerEditBean extends BaseBean implements ValidationErrorHandler {
 
     @SpringBean
     private CustomerService custService;
     @ValidateNestedProperties(value = {
         @Validate(on = {"create", "save"}, field = "name", required = true),
         @Validate(on = {"create", "save"}, field = "address", required = true),})
     private CustomerTO customer;
     private List<CustomerTO> allCustomers;
 
     @DefaultHandler
     public Resolution printCustomers() {
         allCustomers = custService.findAllCustomers();
         return new ForwardResolution("/customer/main.jsp");
     }
 
     public Resolution create() {
         custService.createCustomer(customer);
         return new RedirectResolution(getClass(), "printCustomers");
     }
 
     @Before(stages = LifecycleStage.BindingAndValidation, on = {"edit", "delete"})
     public void loadCustomer() {
         customer = custService.findCustomerById(Long.parseLong(getContext().getRequest().getParameter("customer.id")));
     }
 
     public Resolution edit() {
         return new ForwardResolution("/customer/edit.jsp");
     }
 
     public Resolution save() {
         custService.updateCustomer(customer);
         return new RedirectResolution(getClass(), "printCustomers");
     }
 
     public Resolution delete() {
         custService.deleteCustomer(customer);
         return new ForwardResolution(getClass(), "printCustomers");
     }
 
     @Override
     public Resolution handleValidationErrors(ValidationErrors errors) {
         allCustomers = custService.findAllCustomers();
         return null;
     }
 
     public CustomerTO getCustomer() {
         return customer;
     }
 
     public void setCustomer(CustomerTO customer) {
         this.customer = customer;
     }
 
     public List<CustomerTO> getAllCustomers() {
         return allCustomers;
     }
 
     public void setAllCustomers(List<CustomerTO> allCustomers) {
         this.allCustomers = allCustomers;
     }
 }
