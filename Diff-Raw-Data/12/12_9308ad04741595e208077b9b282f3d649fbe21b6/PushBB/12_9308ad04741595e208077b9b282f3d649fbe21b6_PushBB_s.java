 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package catchatbb;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Named;
 import org.icefaces.application.PushRenderer;
 
 /**
  *
  * @author Elin
  */
 @Named("pushBean")
 @RequestScoped
 public class PushBB {
     
     private static final String PUSH_GROUP = "chatPage";
     
     @PostConstruct
     public void postConstruct(){
         PushRenderer.addCurrentSession(PUSH_GROUP);
     }
      
     public String push() {
         PushRenderer.render(PUSH_GROUP);
         return null;
     }
     
 }
