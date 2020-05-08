 package controllers;
 
 import play.mvc.Controller;
 import play.mvc.With;
 
 /**
  *
  * @author pierregaste
  */
 @With(Secure.class)
 public class WayParticipation extends Controller {
 
     public static void add(Long id) {
         models.Way way = models.Way.findById(id);
 
         if (way != null) {
             new models.WayParticipation(way, User.connected()).save();
         }
 
         redirect("/way/search");
     }
 
     public static void accept(Long id) {
         models.WayParticipation participation = models.WayParticipation.findById(id);
 
         if (participation != null && participation.status == 0 && participation.way.driver == User.connected()) {
             participation.status = 2;
             participation.save();
         }
 
         User.dashboard();
     }
 
     public static void refuse(Long id) {
         models.WayParticipation participation = models.WayParticipation.findById(id);
 
         if (participation != null && participation.status == 0 && participation.way.driver == User.connected()) {
             participation.status = 1;
             participation.save();
         }
 
         User.dashboard();
     }
 
     public static void cancel(Long id) {
         models.WayParticipation participation = models.WayParticipation.findById(id);
 
         if (participation != null && (participation.status == 2 || participation.status == 4) && participation.participant == User.connected()) {
             participation.status = 3;
             participation.save();
         }
 
        redirect("/way/search");
     }
 
     public static void validNotification(Long id) {
         models.WayParticipation participation = models.WayParticipation.findById(id);
 
         if (participation != null && participation.participant == User.connected()) {
             if (participation.status == 1) {
                 participation.delete();
             } else if (participation.status == 2) {
                 participation.status = 4;
                 participation.save();
             } else if (participation.status == 3) {
                 participation.delete();
             }
         }
 
         if (participation != null && participation.way.deleted == 1 && participation.status == 3 && participation.way.driver == User.connected()) {
             participation.delete();
         }
 
         User.dashboard();
     }
 }
