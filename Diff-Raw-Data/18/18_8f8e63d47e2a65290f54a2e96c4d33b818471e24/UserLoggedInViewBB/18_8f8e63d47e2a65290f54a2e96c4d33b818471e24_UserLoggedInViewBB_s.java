 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package longcatarmy.bb;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.annotation.PostConstruct;
 import javax.faces.bean.SessionScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import longcatarmy.src.AuctionObject;
 import longcatarmy.src.Customer;
 
 /**
  *
  * @author William Axhav Bratt
  */
 
 @SessionScoped
 @Named("userLIV") //UserLoggedInView
 public class UserLoggedInViewBB implements Serializable {
     
     private List<AuctionObject> expAuctions;
     //private AuctionBB test = AuctionBB.INSTANCE;
     Customer testCust;
     
     @Inject
     SuperSiteBean site;
     
     public UserLoggedInViewBB()
     {
         
         
        testCust = site.getCustomerByName("apa");
        
        expAuctions = site.getAllAuctionsForUser(testCust);   
     }
     
     @PostConstruct
     public void post()
     {
     }
     
     public List<AuctionObject> getAuctions()
     {
         return expAuctions;
     }
     
     public List<AuctionObject> getBids()
     {
         return testCust.getMyBuyAuctionList();
     }
     
 }
