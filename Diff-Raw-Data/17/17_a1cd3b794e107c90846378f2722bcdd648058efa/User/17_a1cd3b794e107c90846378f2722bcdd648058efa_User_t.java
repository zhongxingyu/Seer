 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 import models.FriendShip;
 import play.mvc.*;
 import play.data.validation.*;
 
 @With(Secure.class)
 public class User extends Controller {
 
     /**
      * Action par défaut
      */
     public static void index()
     {
         redirect("/");
     }
 
     /**
      * Tableau de bord de l'utilisateur
      */
     public static void dashboard()
     {
         models.User user = User.connected();
         renderArgs.put("user", user);
         
         List<FriendShip> friends_waiting = FriendShip.find("friend = ? and status = ?", user, 0).fetch();
         renderArgs.put("friends_waiting", friends_waiting);
         
         List<FriendShip> friends_refuse = FriendShip.find("user = ? and status = ?", user, 1).fetch();
         renderArgs.put("friends_refuse", friends_refuse);
         
         List<FriendShip> friends_accept = FriendShip.find("user = ? and status = ?", user, 2).fetch();
         renderArgs.put("friends_accept", friends_accept);
         
         render();
     }
 
     /**
      * Action de déconnexion
      * @throws Throwable 
      */
     public static void logout() throws Throwable
     {
         Secure.logout();
     }
 
     /**
      * Page d'édition de profil
      * 
      * @view app/view/user/profile.html
      */
     public static void profile()
     {
         models.User user = User.connected();
         render(user);
     }
     
     public static void detail(Long id){   
         models.User user = models.User.findById(id);
         List<models.Way> waysDriver = models.Way.find("byDriver", user).fetch();
         List<models.Way> wayPassengers = new ArrayList<models.Way>();
         render(user, waysDriver, wayPassengers);
     }
 
    public static void editProfile(@Required String password, @Required String firstname, @Required String lastname) {
         models.User userEdited = User.connected();
 
         if (validation.hasErrors()) {
             params.flash(); // add http parameters to the flash scope
             validation.keep(); // keep the errors for the next request
             redirect("/user/profile");
         }
         
        userEdited.password = password;
        userEdited.firstname = firstname;
        userEdited.lastname = lastname;
         userEdited.save();
 
         redirect("/user/profile");
     }
 
     public static models.User connected() {
         models.User user = models.User.find("byEmail", Security.connected()).first();
         return user;
     }
 }
