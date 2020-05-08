 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package es.cediant.abacus;
 
 import es.cediant.database.User;
 import es.cediant.database.UserHelper;
 import es.cediant.service.IUserService;
 import java.io.Serializable;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import org.primefaces.context.RequestContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author mafernandez
  */
 @ManagedBean(name="userBean")
 @SessionScoped
 public final class UserBean implements Serializable {
     private static final long serialVersionUID = 1L;
     private static final String SUCCESS = "success";
     private static final String ERROR   = "error";    
 
     private boolean loggedin = false;
     private String username;
     private String password;
     private String pic;
     private final Logger logger;
     
     //Spring User Service is injected...
     @ManagedProperty(value="#{UserService}")
     IUserService userService;
     
     public UserBean() {      
         this.logger = LoggerFactory.getLogger(LoginBean.class);
         this.setPic("defaultPic.png");
         this.setLoggedin(false);
         logger.info("loggedin= {}", this.loggedin);
     }
     
     /**
      * Get User Service
      *
      * @return IUserService - User Service
      */
     public IUserService getUserService() {
         return userService;
     }
     
     public void setUserService(IUserService userService) {
         this.userService = userService;
     }
     
     public boolean isLoggedin() {
         return loggedin;
     }
 
     public void setLoggedin(boolean loggedin) {
         logger.info("Loggedin = "+loggedin);
         this.loggedin = loggedin;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         logger.info("Username = "+username);
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         logger.info("Password = "+password);
         this.password = password;
     }
     
     public String getPic() {
         return pic;
     }
 
     public void setPic(String pic) {
         logger.info("New Pic");   
         this.pic = pic;
     }        
     
     public void login(ActionEvent actionEvent){
        //RequestContext context = RequestContext.getCurrentInstance();  
         if(username != null && password != null) {  
            User user = getUserByUsername(username);
             if(user != null && user.getPassword().equals(password)){
                 this.setLoggedin(true);  
                 logger.info("Loggedin");
             } else {                      
                 this.setLoggedin(false);  
                 logger.info("Incorrect login");
             }  
         }
         logger.info("Username: {}", username);
         logger.info("Password: {}", password);
        //context.addCallbackParam("loggedin", this.isLoggedin());  
     }
     
     public void logout(){
         logger.info("Loggedout");
         FacesContext.getCurrentInstance().getExternalContext().invalidateSession();        
     }
 }
