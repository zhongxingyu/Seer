 package Beans;
 
 import java.io.IOException;
 import java.io.Serializable;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.bean.ManagedBean;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.inject.Named;
 import javax.servlet.http.HttpSession;
 
 @ManagedBean
 @SessionScoped
 @Named("Frontpage")
 public class LoginBean implements Serializable {
 
     public void redirect() {
         ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
         try {
            if (externalContext.getUserPrincipal().getName().equals("customer")) {
                 externalContext.redirect("faces/protected/customer.xhtml");
             }
            if (externalContext.getUserPrincipal().getName().equals("chef")) {
                 externalContext.redirect("faces/protected/chef.xhtml");
             }
            if (externalContext.getUserPrincipal().getName().equals("salesman")) {
                 externalContext.redirect("faces/protected/salesman.xhtml");
             }
            if (externalContext.getUserPrincipal().getName().equals("driver")) {
                 externalContext.redirect("faces/protected/driver.xhtml");
             }
            if (externalContext.getUserPrincipal().getName().equals("admin")) {
                 externalContext.redirect("faces/protected/admin.xhtml");
             }
         } catch (IOException e) {
             System.out.println("IOException");
         }
 
     }
 
     public void logout() {
         HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
         httpSession.invalidate();
         ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
         try {
             externalContext.redirect("../faces/index.xhtml");
         } catch (IOException e) {
             System.out.println("IOException");
         }
     }
 
     public boolean isLoggedIn() {
         ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
         if (externalContext.getRemoteUser() != null) {
             return true;
         }
         return false;
     }
 }
