 package dit126.group4.group4shop_app.controller;
 
 import dit126.group4.group4shop_app.model.CurrentUserBackingBean;
 import dit126.group4.group4shop_app.view.LoginBackingBean;
 import java.io.IOException;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  *
  * @author Group4
  */
 @Named("loginController")
 public class LoginController {
     
     @Inject
     private LoginBackingBean loginBackingBean;
     
     @Inject 
     private CurrentUserBackingBean currentUserBackingBean;
     
     public String login() throws IOException{
         FacesContext context = FacesContext.getCurrentInstance();
         ExternalContext externalContext = context.getExternalContext();
         HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
         try {
             request.login(loginBackingBean.getUsername(), loginBackingBean.getPassword());
             
             currentUserBackingBean.initUser();
             externalContext.redirect("../user/customerhome.xhtml");
             
             return "success";
            
         } catch(ServletException e){
             context.addMessage(null, new FacesMessage("Login Failed"));
             //return navigationController.loginFailed();
             //externalContext.redirect("loginerror.xhtml");
             return "failed";
         }
         //return "";
         //null;
     }
     
     public void logout() throws IOException{
         ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
         externalContext.invalidateSession();
         //externalContext.redirect("/content/home.xhtml");
         externalContext.redirect("../home.xhtml");
     }
     
 }
