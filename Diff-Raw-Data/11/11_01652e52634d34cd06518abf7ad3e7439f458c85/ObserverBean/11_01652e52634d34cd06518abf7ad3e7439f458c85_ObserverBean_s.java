 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.marcel.web.mavenjsfcdi;
 
 import java.util.logging.Level;
 import java.util.logging.Logger; 
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception; 
 import javax.inject.Inject;
 import javax.inject.Named;
 import org.primefaces.push.PushContext;
 import org.primefaces.push.PushContextFactory;
 
 /**
  *
  * @author Marcel
  */
 @Named
 @RequestScoped
 public class ObserverBean {
     
     @Inject
     private InfoBean bean;
      
     public void observer(@Observes MyEvent event) {
        System.out.println("observer run...");
         try {
            Thread.sleep(5000);
         } catch (InterruptedException ex) {
             Logger.getLogger(ObserverBean.class.getName()).log(Level.SEVERE, null, ex);
         }
         PushContext pushContext = PushContextFactory.getDefault()
                 .getPushContext();
          pushContext.push("/CHANNEL", "");
         bean.setValue("observed");
     }
 }
