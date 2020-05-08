 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package es.cediant.abacus;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
import org.primefaces.event.TabChangeEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author mafernandez
  */
 @ManagedBean
 @RequestScoped
 public class TabBean {  
     
     private String effect;
     private final Logger logger;
 
     public TabBean(){
         this.logger = LoggerFactory.getLogger(LoginBean.class);
         this.setEffect("fade");
     }
     
     public String getEffect() {
         return effect;
     }
 
     public void setEffect(String effect) {
         this.effect = effect;
     }
     
    public void onTabChange(TabChangeEvent event) {  
        logger.info("New tab: {}.", event.getTab().getTitle());        
     }  
 }  
 
 
 
