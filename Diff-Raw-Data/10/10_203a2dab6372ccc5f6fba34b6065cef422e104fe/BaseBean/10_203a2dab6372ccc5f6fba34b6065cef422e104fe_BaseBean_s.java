 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package roman.cwk.managedbeans;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpSession;
 
 /**
  * Abstract class that contains method which are common for both managed beans.
  *
  * @author Terrorhunt
  */
 public abstract class BaseBean {
 
     /**
      * Adds error message view.
      *
      * @param message The message to be added.
      */
     public void addErrorMessage(String message) {
         FacesContext context = FacesContext.getCurrentInstance();
         FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message);
         context.addMessage(null, facesMessage);
     }
 
     /**
      * Get the name of currently logged in user.
      *
      * @return The name of user that is logged it.
      */
     public String getLogedUsername() {
         String userName = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
         return userName;
     }
 
     /**
      * Logout user.
      *
      * @return String which navigates to home page.
      */
     public String logout() {
         HttpSession session;
         session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
         session.invalidate();
        return "/index";
     }
 
     /**
      * Finds out whether any user is logged in.
      *
      * @return true if a user is logged in
      */
     public boolean isLoggedIn() {
         String userName = this.getLogedUsername();
         if (userName == null) {
             return false;
         } else {
             return true;
         }
     }
 }
