 package controllers;
 
 import models.Shout;
 import play.Play;
 import play.cache.Cache;
 import play.data.validation.Valid;
 import play.mvc.Controller;
 import play.mvc.Router;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 public class Application extends Controller {
 
     public static void index() {
         long shoutCount = Shout.count();
         if (shoutCount > 0) {
             int random = new Random().nextInt((int) shoutCount);
             List<Shout> shouts = Shout.all().from(random).fetch(1);
             if (shouts.size() > 0) {
                 Shout shout = shouts.get(0);
                 String shareUrl = getShoutUrl(shout);
                 render(shout, shareUrl);
             }
         }
         create();
     }
 
     private static String getShoutUrl(Shout shout) {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("id", shout.id);
         String baseUrl = Play.configuration.getProperty("application.baseUrl");
         if (baseUrl != null && baseUrl.length() > 0) {
            return baseUrl + Router.reverse("Application.show", map).url;
         }
         return Router.getFullUrl("Application.show", map);
     }
 
     public static void show(long id) {
         Shout shout = Shout.<Shout>findById(id);
         if (shout != null) {
             String shareUrl = getShoutUrl(shout);
             render("/Application/index.html", shout, shareUrl);
         }
         index();
     }
 
     public static void update(long id, Shout shout) {
         Shout orgShout = Shout.<Shout>findById(id);
         if (orgShout != null && orgShout.echo == null) {
             orgShout.echo = shout.echo;
             orgShout.save();
         }
         show(id);
     }
 
     public static void create() {
         render();
     }
 
     public static void add(@Valid Shout shout) {
         if (validation.hasErrors()) {
             validation.keep();
             params.flash();
             create();
         }
         if (shout.id == null) {
             shout.save();
         }
         added(shout.id);
     }
 
     public static void added(long id) {
         render(id);
     }
 }
