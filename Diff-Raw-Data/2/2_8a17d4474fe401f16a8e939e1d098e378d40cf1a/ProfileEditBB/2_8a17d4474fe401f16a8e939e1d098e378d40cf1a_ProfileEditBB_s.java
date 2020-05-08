 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package longcatarmy.bb;
 
 import longcatarmy.src.SuperSiteBean;
 import java.io.Serializable;
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.ConversationScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import longcat.auction.src.Customer;
 
 /**
  *
  * @author William Axhav Bratt
  */
 @ConversationScoped
 @Named("profileEdit")
 public class ProfileEditBB implements Serializable{
     
     Customer cust;
     String email;
     String password;
     String phone;
     String secquest;
     String address;
     
     @Inject
     SuperSiteBean site;
     
     public ProfileEditBB()
     {
         
     }
     
     @PostConstruct
     public void post()
     { 
        //cust = site.getCustomerCatalogue().getCustomerByName("apa");
     }
     
     
     public String actOnSelected()
     {
         //site.getCustomerCatalogue().updateCustomer("apa", cust);
         site.getCustomerCatalogue().update(cust);
         return goToView();
     }  
     
     public String goToView()
     {
         return "profile";
     }
     public void setName(String name)
     {
         //incase of buggs does not do anything
     }
     
     public void setEmail(String eMail)
     {
         cust.setEmail(eMail);
     }
     
     public void setPassword(String password)
     {
         cust.setPassword(password);
     }
     
     public void setPhone(String phone)
     {
         cust.setPhoneNr(phone);
     }
     
     public void setSecQuest(String secQuest)
     {
         cust.setSeqQuest(secQuest);
     }
     
     public void setAddress(String address)
     {
         cust.setAddress(address);
     }
     
     public String getName()
     {
         return cust.getName();
     }
     
     public String getEmail()
     {
         return cust.getEmail();
     }
     
     public String getPassword()
     {
         return cust.getPassword();
     }
     
     public String getPhone()
     {
         return cust.getPhoneNr();
     }
     
     public String getSecQuest()
     {
         return cust.getSeqQuest();
     }
     
     public String getAddress()
     {
         return cust.getAddress();
     }
 }
