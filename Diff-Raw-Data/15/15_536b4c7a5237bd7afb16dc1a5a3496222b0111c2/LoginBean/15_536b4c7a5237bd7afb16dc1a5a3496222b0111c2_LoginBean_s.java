 package edu.wctc.bean.demo6;
 
 import java.io.IOException;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 /**
  *
  * @author jlombardo
  */
 @ManagedBean
 @RequestScoped
public class LoginBean {
 
     private String username = "";
     private String password = "";
     private boolean loggedIn = false;
 
     /**
      * @return
      * @throws IOException
      * @throws ServletException
      */
     public String doLogin() throws IOException, ServletException {
         ExternalContext context = FacesContext.getCurrentInstance()
                 .getExternalContext();
         RequestDispatcher dispatcher = ((ServletRequest) context.getRequest())
                 .getRequestDispatcher("/j_spring_security_check?j_username=" + username
                                 + "&j_password=" + password);
         dispatcher.forward((ServletRequest) context.getRequest(),
                 (ServletResponse) context.getResponse());
         FacesContext.getCurrentInstance().responseComplete();
         // It's OK to return null here because Faces is just going to exit.
         return null;
     }
 
     /**
      * @return
      */
     public String getUsername() {
         return this.username;
     }
 
     /**
      * @param username
      */
     public void setUsername(final String username) {
         this.username = username;
     }
 
     /**
      * @return
      */
     public String getPassword() {
         return this.password;
     }
 
     /**
      * @param password
      */
     public void setPassword(final String password) {
         this.password = password;
     }
 
     /**
      * @return
      */
     public boolean isLoggedIn() {
         return this.loggedIn;
     }
 
     /**
      * @param loggedIn
      */
     public void setLoggedIn(final boolean loggedIn) {
         this.loggedIn = loggedIn;
     }
 }
