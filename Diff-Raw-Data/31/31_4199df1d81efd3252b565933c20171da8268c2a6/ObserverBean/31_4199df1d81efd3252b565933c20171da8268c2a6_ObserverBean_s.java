 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.marcel.web.mavenjsfcdi;
 
 import java.util.logging.Level;
 import java.util.logging.Logger; 
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.event.Observes; 
 import javax.ejb.Asynchronous;
 import javax.enterprise.event.TransactionPhase;
 import javax.inject.Inject;
 import javax.inject.Named;
 import org.marcel.web.asynch.FutureSupport;
 import org.primefaces.push.PushContext;
 import org.primefaces.push.PushContextFactory;
 
 /**
  *
  * @author Marcel
  */ 
 public class ObserverBean {
     
     @Inject
     private InfoBean bean;
       
     @Inject
     private ChannelBean channel;
     
    
     public void observer(@Observes MyEvent event) {
         System.out.println("observer run...waiting 10sec");
         try {
             Thread.sleep(1000);
         } catch (InterruptedException ex) {
             Logger.getLogger(ObserverBean.class.getName()).log(Level.SEVERE, null, ex);
         }
         PushContext pushContext = PushContextFactory.getDefault()
                 .getPushContext();
          pushContext.push(channel.getEventchannel(), "");
         bean.setValue("observed by "+this.hashCode());
     }
 }
