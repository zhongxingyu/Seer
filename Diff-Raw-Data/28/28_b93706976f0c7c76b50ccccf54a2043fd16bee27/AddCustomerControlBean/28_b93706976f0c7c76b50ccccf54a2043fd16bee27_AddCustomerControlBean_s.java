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
 import backingbeans.AddCustomerBackingBean;
 import backingbeans.CustomersBackingBean;
 import core.Address;
 import core.Customer;
 import core.Group;
 import core.Product;
 import java.io.Serializable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.enterprise.context.*;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import modelbeans.CustomerRegistryBean;
 
 @Named
 @ConversationScoped
 public class AddCustomerControlBean implements Serializable{
     @Inject // Handled by system, don't need to create class.
     private Conversation conv;
     @Inject
     private AddAddressBackingBean addAddressBackingBean;
     @Inject
     private AddCustomerBackingBean addCustomerBackingBean;
     @Inject
     private CustomerRegistryBean customerRegistryBean;
     
     public AddCustomerControlBean(){}
         
     public String action() {
         if (conv.isTransient()) {
             conv.begin();
              Logger.getAnonymousLogger().log(Level.INFO, "CONVERSATION BEGINS: Got pnumb {0}");
         }else{
             
         }
         String town = addAddressBackingBean.getTown();
         String street = addAddressBackingBean.getStreet();
         int nbr = addAddressBackingBean.getNbr();
         Address address = new Address(street, nbr, town);
         addCustomerBackingBean.setAddress(address);
         String fname = addCustomerBackingBean.getFname();
         String lname = addCustomerBackingBean.getLname();
         String email = addCustomerBackingBean.getEmail();
         String userName =addCustomerBackingBean.getUserName();
         String password = addCustomerBackingBean.getPassword();
         
         if(customerRegistryBean.isUserNameTaken(userName))
         {
             FacesContext.getCurrentInstance().addMessage("regForm:username", new FacesMessage("Username is already in use"));
             
        
             
             //FacesContext context = FacesContext.getCurrentInstance();
            // context.addMessage( null, new FacesMessage( "The message to display in client" ));
             return null;
            // return "newCustomer?faces-redirect=true";
         }
         
          if (!conv.isTransient()) {
             conv.end();
              Logger.getAnonymousLogger().log(Level.INFO, "CONVERSATION ENDS");
         }
         
         Customer customer = new Customer(address, fname, lname, email, userName, password);
        customer.addGroup(Group.ADMIN);
         customerRegistryBean.add(customer);
         return "login5?faces-redirect=true";
     }
     
     
 
 
     
     
     
 }
