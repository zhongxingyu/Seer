 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controllers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import javax.persistence.Query;
 import models.Activity;
 import models.User;
 import play.db.jpa.GenericModel;
 import play.db.jpa.JPA;
 import play.mvc.Controller;
 import play.mvc.With;
 import service.ShareService;
 
 /**
  *
  * @author Pluce
  */
 @Check("user")
 @With(Secure.class)
 public class Profil extends Controller {
     
     public static void index(){
         User current = Security.connectedUser();
         List<Activity> flux = getFluxVisiblePour(current.id);
         render(current,flux);
     }
     
     public static void fiche(Long id,String slugname){        
         User current = User.findById(id);
         if(current == null){
             notFound("User "+id+" not found.");
         }
        if(current.email.equals(Security.connected())) index();
         List<Activity> flux = getFluxVisibleDe(id);
         renderTemplate("Profil/index.html",current,flux);
     }
     
     public static void partager(String message, File image) throws IOException{
         User current = Security.connectedUser();
         if(message!=null && !message.isEmpty()){
             if(image == null){
                 ShareService.share(current, message);
             } else {
                 ShareService.shareWithPhoto(current, message, image);
             }    
         }
         Profil.index();
     }
     
     public static void follow(Long id,String slugname){
         User current = Security.connectedUser();
         User u = User.findById(id);
         if(u==null){
             notFound();
         }
         current.follows.add(u);
         u.leads.add(current);
         u.save();
         current.save();
         Profil.fiche(id,slugname);
     }
     
     public static void unfollow(Long id,String slugname){
         User current = Security.connectedUser();
         User u = User.findById(id);
         if(u==null){
             notFound();
         }
         current.follows.remove(u);
         u.leads.remove(current);
         u.save();
         current.save();
         Profil.fiche(id,slugname);
     }
     
     private static List<Activity> getFluxVisiblePour(Long id){
         Query query = JPA.em().createQuery("SELECT p FROM Activity p WHERE p.owner.id = ? OR p.owner IN(SELECT f FROM User u, IN(u.follows) f WHERE u.id = ?) ORDER BY p.timeShared DESC").setParameter(1, id).setParameter(2, id);
         return (List<Activity>)query.getResultList();
     }
     
     private static List<Activity> getFluxVisibleDe(Long id){
         User u = User.findById(id);
        // User current = User.find("byEmail",Security.connected()).first();
         Query query;
         //if(u.leads.contains(current)){
             query = JPA.em().createQuery("SELECT p FROM Activity p WHERE p.owner.id = ? ORDER BY p.timeShared DESC").setParameter(1, id);
         //} else {
           //  query = JPA.em().createQuery("SELECT p FROM Activity p WHERE p.owner.id = ? AND p.isPublic = true ORDER BY p.timeShared DESC").setParameter(1, id);
         //}
         
         return (List<Activity>)query.getResultList();
     }
 }
