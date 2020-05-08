 package controllers;
 
 import java.util.*;
 import models.FriendShip;
 import play.mvc.*;
 import play.data.validation.*;
 
 @With(Secure.class)
 public class User extends Controller {
 
     /**
      * Action par défaut
      */
     public static void index() {
         redirect("/");
     }
 
     /**
      * Tableau de bord de l'utilisateur
      */
     public static void dashboard() {
         models.User user = User.connected();
         renderArgs.put("user", user);
         
         List<models.WayParticipation> participations_waiting = models.WayParticipation.find("way.driver = ? and status = ? and way.dateHourStart > ? order by date", user, 0, new Date()).fetch();
         renderArgs.put("participations_waiting", participations_waiting);
         
         List<models.WayParticipation> participations_refuse = models.WayParticipation.find("participant = ? and status = ? order by date DESC", user, 1).fetch();
         renderArgs.put("participations_refuse", participations_refuse);
         
         List<models.WayParticipation> participations_accept = models.WayParticipation.find("participant = ? and status = ? order by date", user, 2).fetch();
         renderArgs.put("participations_accept", participations_accept);
         
         List<models.WayParticipation> participations_cancel = models.WayParticipation.find("way.driver = ? and status = ? order by date", user, 3).fetch();
         renderArgs.put("participations_cancel", participations_cancel);
         
         List<models.WayParticipation> ways_cancel = models.WayParticipation.find("participant = ? and way.deleted = ? and status = ? order by date", user, 1, 3).fetch();
         renderArgs.put("ways_cancel", ways_cancel);        
         
         List<FriendShip> friends_waiting = FriendShip.find("friend = ? and status = ? order by date", user, 0).fetch();
         renderArgs.put("friends_waiting", friends_waiting);
 
         List<FriendShip> friends_refuse = FriendShip.find("user = ? and status = ? order by date", user, 1).fetch();
         renderArgs.put("friends_refuse", friends_refuse);
 
         List<FriendShip> friends_accept = FriendShip.find("user = ? and status = ? order by date", user, 2).fetch();
         renderArgs.put("friends_accept", friends_accept);
 
         int number_friend = (int) FriendShip.count("user = ? and status = ?", user, 2);
         renderArgs.put("number_friend", number_friend);
 
         int number_car = (int) models.Car.count("byOwner", user);
         renderArgs.put("number_car", number_car);
 
         int number_way_driver = (int) models.Way.count("byDriver", user);
         renderArgs.put("number_way_driver", number_way_driver);
 
         int number_way_passenger = models.WayParticipation.find("participant = ? and way.dateHourStart < ? and (status = 2 or status = 4) ", user, new Date()).fetch().size();
         renderArgs.put("number_way_passenger", number_way_passenger);
 
         int number_way_total = number_way_driver + number_way_passenger;
         renderArgs.put("number_way_total", number_way_total);
 
        List<models.Way> ways_driver = models.Way.find("driver = ? and datehourstart > ? and deleted = 0 order by datehourstart", user, new Date()).fetch();
         renderArgs.put("ways_driver", ways_driver);
 
        Double sum = 0.0;
        Integer nb = 0;
        
        for(models.Way way : ways_driver){
            sum += way.cost;
            nb++;
        }
        
        renderArgs.put("avg_price", (nb == 0) ? 0 : sum/nb);
         render();
     }
 
     /**
      * Action de déconnexion
      * @throws Throwable 
      */
     public static void logout() throws Throwable {
         Secure.logout();
     }
 
     /**
      * Page d'édition de profil
      * 
      * @view app/view/user/profile.html
      */
     public static void profile() {
         models.User user = User.connected();
         render(user);
     }
 
     public static void details(Long id) {
         models.User user = models.User.findById(id);
         List<models.Way> waysDriver = models.Way.find("byDriver", user).fetch();
         List<models.WayParticipation> wayPassengers = models.WayParticipation.find("participant = ? and way.dateHourStart < ? and (status = 2 or status = 4) ", user, new Date()).fetch();
 
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
