 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package longcatarmy.bb;
 
 import java.io.Serializable;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 /**
  *
  * @author emesven
  */
 
 @SessionScoped
 @Named("customer")
 public class CustomerBB implements Serializable {
     
     private AuctionBB auction;
     @Inject
    private void setAuctiom(AuctionBB ab){
         auction = ab;
     }
     
     public void getProfile(){
         /*
          * tillfälligt void för att kunna kompilera, 
          * ska troligtvis returnera nåt i stil med
          * 
          * return customer;
          */
     }
     
     public void getMyBids(){
         /*
          * tillfälligt void för att kunna kompilera, 
          * ska troligtvis returnera nåt i stil med
          * 
          * return List<AuctionObject>??;
          */
     }
     
     public void getExpiring(){
         /*
          * tillfälligt void för att kunna kompilera, 
          * ska troligtvis returnera nåt i stil med
          * 
          * return List<AuctionObject>;
          */
     }
 }
