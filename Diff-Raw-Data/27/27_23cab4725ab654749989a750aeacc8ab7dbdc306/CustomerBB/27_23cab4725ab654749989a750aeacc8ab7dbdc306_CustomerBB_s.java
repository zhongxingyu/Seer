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
 }
