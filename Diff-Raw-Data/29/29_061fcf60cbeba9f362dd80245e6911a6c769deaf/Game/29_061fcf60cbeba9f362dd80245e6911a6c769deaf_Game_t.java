 package controllers;
 
 import com.google.gson.JsonObject;
 import events.Event;
 import models.Authentication;
 import models.Job;
 import models.Queues;
 import models.User;
 import play.Logger;
 import play.db.jpa.JPA;
 import play.libs.WS;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.WebSocketController;
 
 import javax.persistence.Query;
 import java.util.ArrayList;
import java.util.Date;
 import java.util.List;
 
 /**
  * @author <a href="mailto:alexander.hanschke@gmail.com">Alexander Hanschke</a>
  */
 public class Game extends Controller {
 
     @Before(unless = {"index", "login", "signup"})
     static void before() {
         if (!Security.isConnected()) {
             index();
         }
 
         if (Game.player().logout) {
             Game.player().logout = Boolean.FALSE;
             Game.player().save();
 
             Security.logout();
         }
 
         renderArgs.put("player", Game.player());
     }
 
     public static void index() {
         Logger.info("index access - ip: %s", request.remoteAddress);
         render();
     }
 
     public static void home() {
         render();
     }
 
     public static void vault() {
         render();
     }
 
     public static void armory() {
         render();
     }
 
     public static void jobs() {
         User player = player();
         List<Job> jobs = Job.find("level <= ? order by level asc", player.level).fetch();
         render(jobs, player);
     }
 
     public static void fight() {
         List<User> users = User.find("name != ? and health > 0", player().name).fetch();
         List<User> enemies = new ArrayList<User>();
         for (User user : users) {
             if (Game.player().canSee(user)) {
                 enemies.add(user);
             }
         }
 
         render(enemies);
     }
 
     public static void tribe() {
         render();
     }
 
     public static void leaderboard() {
         List<User> players = User.find("order by level desc").fetch();
         render(players);
     }
 
     public static void login() {
         render();
     }
 
     public static void signup() {
         render();
     }
 
     public static void profile() {
         if (Game.player().isFacebookConnected()) {
             Authentication auth = Authentication.find("byUser", Game.player()).first();
             JsonObject json = WS.url("https://graph.facebook.com/me?access_token=%s", WS.encode(auth.token)).get().getJson().getAsJsonObject();
             String image = "http://graph.facebook.com/" + json.get("username").getAsString() + "/picture";
             render(json, image);
         }
 
         render();
     }
 
     public static User player() {
         return User.find("byName", session.get("player")).first();
     }
 
 	public static class News extends WebSocketController {
 
 		public static void listen() {
 			while (inbound.isOpen()) {
 				Event event = await(Queues.instance.events.nextEvent());
 
 				if (outbound.isOpen()) {
 					outbound.send(event.toJSON());
 				}
 			}
 		}
 	}
 }
