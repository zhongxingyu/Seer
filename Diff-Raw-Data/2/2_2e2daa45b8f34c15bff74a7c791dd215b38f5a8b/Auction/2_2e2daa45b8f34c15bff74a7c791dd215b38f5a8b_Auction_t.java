 package uy.com.elsubonline.web.user;
 
 import java.util.ResourceBundle;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import org.apache.log4j.Logger;
 import uy.com.elsubonline.api.IAuctionService;
 import uy.com.elsubonline.api.exceptions.ServiceException;
 
 @ManagedBean
 @RequestScoped
 public class Auction {
 
     private static final Logger logger = Logger.getLogger(Auction.class);
 
     private String title;
     private String short_description;
     private String long_description;
     private double base_price;
 
     @EJB
     private IAuctionService auctionService;
 
     public String create() {
 
         logger.info("Trying to create new aution: " + title);
 
         FacesMessage msg;
         FacesContext facesContext = FacesContext.getCurrentInstance();
         ResourceBundle bundle = facesContext.getApplication().getResourceBundle(facesContext, "msg");
         try {
             auctionService.create(title, short_description, long_description, base_price);
             msg = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("msg_auction_created"), title);
         } catch (ServiceException ex) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, bundle.getString("err_auction_aborted"), title);
         }
 
         facesContext.addMessage(null, msg);
         return "/index.xhtml";
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getShort_description() {
         return short_description;
     }
 
     public void setShort_description(String short_description) {
         this.short_description = short_description;
     }
 
     public String getLong_description() {
         return long_description;
     }
 
     public void setLong_description(String long_description) {
         this.long_description = long_description;
     }
 
     public double getBase_price() {
         return base_price;
     }
 
     public void setBase_price(double base_price) {
         this.base_price = base_price;
     }
 
 }
