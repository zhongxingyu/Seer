 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package longcatarmy.bb;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.ConversationScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import longcatarmy.src.AuctionObject;
 import longcatarmy.src.Customer;
 import longcatarmy.src.SuperSite;
 
 /**
  *
  * @author William Axhav Bratt
  */
 @ConversationScoped
 @Named("addAuction")
 public class AddAuctionBB implements Serializable{
    /*
     Customer cust;
     String name;
     double price;
     String info;
     
     AuctionBB test = AuctionBB.INSTANCE;
     //@Inject
     SuperSite site = test.createSuperSite();
     
     public AddAuctionBB()
     {
         
     }
     
     @PostConstruct
     public void post()
     { 
         cust = site.getCustomerByName("apa");
     }
     
     
     public String actOnSelected()
     {
         site.newAuction(cust,new AuctionObject(name,info,price,new Date()));
         return goToView();
     }  
     
     public String goToView()
     {
         return "profile";
     }
     public void setName(String name)
     {
         this.name = name;
     }
     
     public String getName()
     {
         return name;
     }
     public void setPrice(Double price)
     {
         this.price = price;
     }
     
     public Double getPrice()
     {
         return price;
     }
     public void setInfo(String info)
     {
         this.info = info;
     }
     
     public String getInfo()
     {
         return info;
     }
    */
    
 }
