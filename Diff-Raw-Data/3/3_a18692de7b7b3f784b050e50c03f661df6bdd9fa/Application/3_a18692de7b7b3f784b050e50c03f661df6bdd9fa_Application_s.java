 package controllers;
 
 import models.Bruker;
 import models.Lag;
 import models.TabellTips;
 import play.Logger;
 import play.mvc.Controller;
 
 import java.util.List;
 
 public class Application extends Controller {
 
   public static void index() {
     List<Lag> lag = Lag.findAll();
    render(lag);
   }
 
   public static void visBruker(Long id) {
     Bruker bruker = Bruker.findById(id);
     render(bruker);
   }
 
   public static void brukere() {
     List<Bruker> brukere = Bruker.getResultatliste();
     render(brukere);
   }
 
   public static void tipp(String navn, List<Long> tips) {
 
     Bruker bruker = new Bruker(navn);
     bruker.save();
 
     int plassering = 1;
     for (Long lagId : tips) {
       Logger.info("Bruker: %s, lagId: %s, posisjon: %s", bruker.navn, lagId, plassering);
       TabellTips t = new TabellTips(bruker, finnLag(lagId), plassering++);
       t.save();
     }
 
     visBruker(bruker.id);
   }
 
   private static Lag finnLag(Long lagId) {
     return (Lag) Lag.findById(lagId);
   }
 
 }
