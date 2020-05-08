 package controllers;
 
 import models.domain.Statistiques;
 import play.mvc.Controller;
 
 import java.util.List;
 
 /**
  * .
  *
  * @author fsznajderman
  *         date :  09/06/12
  */
 public class FreeDatas extends Controller {
 
 
     public static void freeStatGplus() {
 
 
         final List<Statistiques> statistiques = Statistiques.q().filter("current", true).asList();
 
 
        Statistiques stats = statistiques.get(statistiques.size() - 1);
 
 
         renderJSON(stats);
     }
 }
