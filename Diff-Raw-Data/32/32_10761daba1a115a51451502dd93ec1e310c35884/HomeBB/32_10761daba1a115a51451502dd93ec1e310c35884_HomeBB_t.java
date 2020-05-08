 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package longcatarmy.bb;
 
 import longcatarmy.src.SuperSiteBean;
 import java.io.Serializable;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.PostConstruct;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import longcat.auction.src.AuctionObject;
 
 /**
  *
  * @author William Axhav Bratt
  */
 @RequestScoped
 @Named("home")
 public class HomeBB implements Serializable{
     
     
     @Inject
     SuperSiteBean site;
     
     private Long id;
     
     public HomeBB()
     {
     }
     
     @PostConstruct
     public void post()
     { 
     }
     
     public List<AuctionObject> getAllAuctions()
     {
         Logger.getAnonymousLogger().log(Level.INFO,"auction ={0}", site.getAuctionCatalogue().getAllAuctions().isEmpty());//Never remove this
         return site.getAuctionCatalogue().getAllAuctions();
     }
     
     public void setId (Long id) {
         this.id = id;
     }
     
     //TODO fixa till returnen till nåt vettigt (till viewAuction)
     //fixa även till viewAuction och index xhtml
     public String goToAuction(Long id) {
         this.id = id;
        //return "success?faces-redirect=true&includeViewParams=true"; //ev bara return success
        return "success";
     }
 }
