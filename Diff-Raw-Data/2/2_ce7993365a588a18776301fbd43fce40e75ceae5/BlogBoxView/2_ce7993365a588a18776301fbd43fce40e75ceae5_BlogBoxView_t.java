 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package my.presentation;
 
 import boundary.BlogBoxUserFacade;
 import entities.BlogBoxUser;
 import java.io.Serializable;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Named;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Patrik Larsson
  */
 @Named
 @SessionScoped
 public class BlogBoxView implements Serializable{
 
     @EJB
     private BlogBoxUserFacade blogBoxUserFacade;
     private BlogBoxUser user;
 
     /**
      * Creates a new instance of MessageView
      */
     public BlogBoxView() {
         this.user = new BlogBoxUser();
     }
 
     public BlogBoxUser getUser() {
         return user;
     }
 
     public void setUser(BlogBoxUser user) {
         this.user = user;
     }
 
     public boolean hasUser() {
         return user.isNotNull();
     }
 
     public int getNumberOfUsers() {
         return blogBoxUserFacade.findAll().size();
     }
 
     public List<BlogBoxUser> getUsers() {
         return blogBoxUserFacade.findAll();
     }
 
     public String loginUser(BlogBoxUser tempuser) {
         this.user = tempuser;
         System.out.println(" 1  " + user.getName() + " 2  " + user.getPassword() + " TTT  " + user.getId());
         List<BlogBoxUser> users = blogBoxUserFacade.findAll();
         for (BlogBoxUser temp : users) {
             if (temp.getName().equals(user.getName()) && temp.getPassword().equals(user.getPassword())) {
                 user = temp;
                 return "user";
             }
         }
         user.setName(null);
         user.setPassword(null);
         return "index";
     }
 
     public String logoutUser() {
         HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
         httpSession.invalidate();
        user.setName(null);
        user.setPassword(null);
         return "index";
     }
 }
