 package cz.muni.fi.pa165.library;
 
 import static cz.muni.fi.pa165.library.BooksActionBean.log;
 import cz.muni.fi.pa165.library.service.CustomerService;
 import cz.muni.fi.pa165.library.to.CustomerTo;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.List;
 import net.sourceforge.stripes.action.*;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
@UrlBinding("/customer/{$event}/{customer.id}")
 public class CustomersActionBean extends BaseActionBean {
 
     static final Logger log = LoggerFactory.getLogger(BooksActionBean.class);
 
     @SpringBean
     protected CustomerService customerService;
     
     private boolean validationError;
 
     @ValidateNestedProperties(value = {
             @Validate(on = {"add","save"}, field = "firstName", required = true),
             @Validate(on = {"add","save"}, field = "lastName", required = true),
             @Validate(on = {"add","save"}, field = "address", required = true),
             @Validate(on = {"add","save"}, field = "dateOfBirth", required = true),
             @Validate(on = {"add","save"}, field = "pid", required = true)
     })
     private CustomerTo customer;
     
     private List<CustomerTo> customers;
 
     private Long findId;
     private String findFirstName;
     private String findLastName;
     private String findAddress;
     private String findPid;
     private Date findDateOfBirth;
     
     public CustomerTo getCustomer() {
         return customer;
     }
     public void setCustomer(CustomerTo customer) {
         this.customer = customer;
     }
     public List<CustomerTo> getCustomers() {
         return customers;
     }
     
     @DefaultHandler
     public Resolution list() {
         return new ForwardResolution("/customers.jsp");
     }
 
     public Resolution edit() {
         return new ForwardResolution("/customer/edit.jsp");
     }
 
     /*
     public Resolution findBy(){
         log.debug("findBy() ");
         customers = customerService.find();
         if(customers == null || customers.isEmpty()){
             DateFormat f = DateFormat.getDateInstance(DateFormat.LONG, getContext().getRequest().getLocale());
             getContext().getMessages().add(new LocalizableMessage("book.findPublishDate.message",(findDateFrom==null?"neomezeno":f.format(findDateFrom)),(findDateTo==null?"neomezeno":f.format(findDateTo))));  
             return getContext().getSourcePageResolution();
         }
         return new ForwardResolution("/customer/list.jsp");       
     }
     */
     public Resolution add() {
         log.debug("add() customer={}", customer);
         customerService.addCustomer(customer);
         getContext().getMessages().add(new LocalizableMessage("customer.add.message",escapeHTML(customer.getFirstName()),escapeHTML(customer.getLastName()),customer.getId()));
         return new RedirectResolution(this.getClass());
     }
  
     public Resolution delete() {
         return new ForwardResolution("/customers.jsp");
     }
 
     public boolean isValidationError() {
         return validationError;
     }
 
     public void setValidationError(boolean validationError) {
         this.validationError = validationError;
     }
 
     public Long getFindId() {
         return findId;
     }
 
     public void setFindId(Long findId) {
         this.findId = findId;
     }
 
     public String getFindFirstName() {
         return findFirstName;
     }
 
     public void setFindFirstName(String findFirstName) {
         this.findFirstName = findFirstName;
     }
 
     public String getFindLastName() {
         return findLastName;
     }
 
     public void setFindLastName(String findLastName) {
         this.findLastName = findLastName;
     }
 
     public String getFindAddress() {
         return findAddress;
     }
 
     public void setFindAddress(String findAddress) {
         this.findAddress = findAddress;
     }
 
     public String getFindPid() {
         return findPid;
     }
 
     public void setFindPid(String findPid) {
         this.findPid = findPid;
     }
 
     public Date getFindDateOfBirth() {
         return findDateOfBirth;
     }
 
     public void setFindDateOfBirth(Date findDateOfBirth) {
         this.findDateOfBirth = findDateOfBirth;
     }
     
 }
