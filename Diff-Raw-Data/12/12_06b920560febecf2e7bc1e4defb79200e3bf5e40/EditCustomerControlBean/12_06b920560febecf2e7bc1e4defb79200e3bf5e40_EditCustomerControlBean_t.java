 package controlbeans;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author thituson
  */
 import backingbeans.AddAddressBackingBean;
 import backingbeans.EditCustomerBackingBean;
 import core.Address;
 import core.Customer;
 import java.io.Serializable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.enterprise.context.*;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.inject.Inject;
 import javax.inject.Named;
 import modelbeans.CustomerRegistryBean;
 
 @Named
 @ConversationScoped
 public class EditCustomerControlBean implements Serializable{
     
     @Inject // Handled by system, don't need to create class.
     private Conversation conv;
     @Inject
     private AddAddressBackingBean addAddressBackingBean;
     @Inject
     private EditCustomerBackingBean editCustomerBackingBean;
     @Inject
     private CustomerRegistryBean customerRegistryBean;
        
     public EditCustomerControlBean(){}
         
 
 
      public String action() {
         if (!conv.isTransient()) {
             conv.end();
              Logger.getAnonymousLogger().log(Level.INFO, "CONVERSATION ENDS");
         }
         try 
         {
             Long id = editCustomerBackingBean.getId();
             Address address = editCustomerBackingBean.getAddress();
             String fname = editCustomerBackingBean.getFname();
             String lname = editCustomerBackingBean.getLname();
             String email = editCustomerBackingBean.getEmail();
             String userName =editCustomerBackingBean.getUserName();
             String password = editCustomerBackingBean.getPassword();
             Customer customer = new Customer(id, address, fname, lname, email, userName, password);
             customerRegistryBean.update(customer);
            
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Profile saved"));
             return null;
         } catch (Exception e) {
             // Not implemented
             //return "error?faces-redirect=true&amp;cause=" + e.getMessage();
             return null;
         }
     }   
      
     public void actionListener(ActionEvent ae) 
     { 
         Customer customer = (Customer) ae.getComponent().getAttributes().get("customer");
         if (conv.isTransient()) {
             conv.begin();
              Logger.getAnonymousLogger().log(Level.INFO, "CONVERSATION BEGINS: Got pnumb {0}", customer);
         }else{
 
         }
         editCustomerBackingBean.setId(customer.getId());
         editCustomerBackingBean.setFname(customer.getFname());
         editCustomerBackingBean.setLname(customer.getLname());
         editCustomerBackingBean.setEmail(customer.getEmail());
         Address address;
         address = customer.getAddress();
         editCustomerBackingBean.setAddress(address);
     }
         
 }
