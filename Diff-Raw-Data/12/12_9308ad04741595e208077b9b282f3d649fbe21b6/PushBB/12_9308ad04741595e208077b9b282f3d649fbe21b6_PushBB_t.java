 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package catchatbb;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
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
    @Inject
    private StatusManager statusManager;
     
     @PostConstruct
     public void postConstruct(){
         PushRenderer.addCurrentSession(PUSH_GROUP);
     }
      
     public String push() {
         PushRenderer.render(PUSH_GROUP);
         return null;
     }
    public String logInPush() {
        String stat = statusManager.login();
        if(stat.equals("LOGIN_SUCCESS")){
            PushRenderer.render(PUSH_GROUP);
        }
        return stat;
    }
     
 }
