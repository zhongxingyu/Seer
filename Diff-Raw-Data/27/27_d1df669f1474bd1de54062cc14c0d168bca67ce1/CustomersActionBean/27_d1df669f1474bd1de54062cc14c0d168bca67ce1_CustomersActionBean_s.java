 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pa165.jtravelagency.web;
 
 import cz.muni.fi.pa165.jtravelagency.dto.CustomerDTO;
 import cz.muni.fi.pa165.jtravelagency.facade.ServiceFacade;
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
  * @author xvaraga
  */
 @UrlBinding("/customers/{$event}/{customer.id}")
 public class CustomersActionBean extends BaseActionBean implements ValidationErrorHandler {
 final static Logger log = LoggerFactory.getLogger(CustomersActionBean.class);
 
     @SpringBean
     protected ServiceFacade facade;
 
     private List<CustomerDTO> customers;
 
     @DefaultHandler
     public Resolution list() {
         log.debug("list()");
         customers = facade.getAllCustomers();
         return new ForwardResolution("/customer/list.jsp");
     }
 
     public List<CustomerDTO> getCustomers() {
         return customers;
     }
 
     @ValidateNestedProperties(value = {
             @Validate(on = {"add", "save"}, field = "firstName", required = true),
             @Validate(on = {"add", "save"}, field = "lastName", required = true)
     })
     private CustomerDTO customer;
 
     public Resolution add() {
         log.debug("add() customer={}", customer);
         facade.createCustomer(customer);
         getContext().getMessages().add(new LocalizableMessage("customer.add.message",escapeHTML(customer.getFirstName()),escapeHTML(customer.getLastName())));
         return new RedirectResolution(this.getClass(), "list");
     }
 
     @Override
     public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
         customers = facade.getAllCustomers();
         return null;
     }
 
     public CustomerDTO getCustomer() {
         return customer;
     }
 
     public void setCustomer(CustomerDTO customer) {
         this.customer = customer;
     }
 
     public Resolution delete() {
         log.debug("delete({})", customer.getId());
         customer = facade.getCustomer(customer.getId());
         facade.deleteCustomer(customer);
         getContext().getMessages().add(new LocalizableMessage("customer.delete.message",escapeHTML(customer.getFirstName()),escapeHTML(customer.getLastName())));
         return new RedirectResolution(this.getClass(), "list");
     }
 
     @Before(stages = LifecycleStage.BindingAndValidation, on = {"edit", "save"})
     public void loadCustomerFromDatabase() {
         String ids = getContext().getRequest().getParameter("customer.id");
         if (ids == null) return;
         customer = facade.getCustomer(Long.parseLong(ids));
     }
 
     public Resolution edit() {
         log.debug("edit() customer={}", customer);
         return new ForwardResolution("/customer/edit.jsp");
     }
 
     public Resolution save() {
         log.debug("save() customer={}", customer);
         facade.updateCustomer(customer);
         return new RedirectResolution(this.getClass(), "list");
     }
     
     public Resolution cancel() {
         return new RedirectResolution(this.getClass(), "list");
     }
 }
 
     
     
 
