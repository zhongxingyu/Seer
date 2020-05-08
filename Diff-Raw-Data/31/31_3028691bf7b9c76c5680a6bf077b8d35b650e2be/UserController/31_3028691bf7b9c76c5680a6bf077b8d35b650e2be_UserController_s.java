 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fr.gphy.piotrgui.j2eged.controllers;
 
 import fr.gphy.piotrgui.j2eged.helpers.UserHelper;
 import fr.gphy.piotrgui.j2eged.model.Document;
 import fr.gphy.piotrgui.j2eged.model.User;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Named;
 
 /**
  *
  * @author Piotr
  */
 @Named("UserController")
 @SessionScoped
 public class UserController implements Serializable {
 
     private User user;
     private UserHelper helper;
 
     private List<Document> documents;
     
     public UserController() {
         this.user = new User();
         this.helper = new UserHelper();
         this.documents  = new ArrayList<Document>();
     }
 
     public String affiche() {
         return "etudiant";
     }
 
     public String retour() {
         return "login";
     }
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     public String logOut() {
         user = new User();
         return "login";
     }
     
     public void checkLogged() throws IOException {
         FacesContext context = FacesContext.getCurrentInstance();
         
         String currentUrl = context.getExternalContext().getRequestServletPath();
         if (getUser().getLogin() == null) {
             if(!currentUrl.equals("/login.xhtml")) {
                 context.getExternalContext().redirect("login.xhtml");
             }
         } else {
             if(currentUrl.equals("/login.xhtml")) {
                 context.getExternalContext().redirect("browser.xhtml");
             }
         }
     }
     
     public String checkDB() {
         FacesContext context = FacesContext.getCurrentInstance();
         
         
         if (! this.helper.getConnection(user.getLogin(), user.getPassword())) {
             context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Access denied", "Login: "+user.getLogin()));
             return "login";
         }
         
         context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Welcome on GOD", "Login: "+user.getLogin()));
         return "browser";
         
     }
     
     
 }
