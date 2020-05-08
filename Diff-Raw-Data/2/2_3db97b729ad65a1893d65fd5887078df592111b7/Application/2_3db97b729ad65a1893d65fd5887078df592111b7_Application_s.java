 package controllers;
 
 import controllers.securesocial.*;
 import models.*;
 import play.i18n.*;
 import play.mvc.*;
 import securesocial.provider.*;
 
 import java.util.*;
 
 @With(SecureSocial.class)
 public class Application extends Controller {
 
   // List of rewards for flowers
   public static Map<Integer,Reward> rewards = new HashMap<Integer, Reward>();
 
   static {
     rewards.put(1, new Reward("Väike lill", 5));
     rewards.put(2, new Reward("Keskmine lill", 10));
     rewards.put(3, new Reward("Suur lill", 15));
   }
 
 
 
   public static User getUser() {
     return (User)renderArgs.get("fanUser");
   }
 
   @Before
   public static void before() {
     FanUser fanUser = (FanUser)SecureSocial.getCurrentUser();
     if (fanUser != null) renderArgs.put("fanUser", User.findById(fanUser.userId));
   }
 
   public static void index() {
     render();
   }
 
   /**
    * AJAX query endpoint for liking / disliking productions
    *
    * @param productionId
    * @param rating
    */
   public static void rateProduction(Long productionId, String rating) {
     EventRating eventRating = EventRating.find("web_event_id = ? and user_id = ?", productionId, getUser().id).first();
 
     if (eventRating != null) {
       eventRating.rating = EventRatings.valueOf(rating);
     }
     else {
       eventRating = new EventRating(getUser(), Production.<Production>findById(productionId), EventRatings.valueOf(rating));
     }
     eventRating.save();
     renderText("");
   }
 
   public static void buyTicket(Long eventId, Integer count) {
     Event event = Event.findById(eventId);
     try {
       TicketPurchase ticket = TicketPurchase.register(event, getUser(), count == null ? 1 : count);
       renderTemplate("Application/ticket.html", ticket);
     }
     catch (OutOfTicketsException e) {
       error(400, Messages.get("Pole piisavalt pileteid!"));
     }
     catch (OutOfPointsException e) {
       error(400, Messages.get("Pole piisavalt punkte!"));
     }
     error(500, "Pileti ostmine ebaõnnestus");
   }
 
   public static void patActor(int actorId, int rewardId, String description) {
     try {
       Actor a = createActors().get(actorId);
       Reward r = rewards.get(rewardId);
       if(a == null || r == null) {
         flash.error("Palun täida kõik väljad");
       } else {
         try {
           ActorPatting.register(getUser(), r.points, Messages.get("Saaja") + ": " + a.name + " / " + Messages.get("Teade") + ": " + description);
          flash.success("Aitäh! ...hea meel");
         }
         catch (OutOfPointsException e) {
           flash.error("Pole piisavalt punkte!");
         }
       }
     }
     catch (Exception e) {
       flash.error("Tehniline probleem");
       System.out.println(e);
     }
     flowers();
   }
 
   public static void supportTheatre() {
     try {
       PointTransaction.register(getUser(), 1, Messages.get("Teatri toetamine"));
     }
     catch (OutOfPointsException e) {
       error(Messages.get("Pole piisavalt punkte!"));
     }
     renderText("OK");
   }
 
   public static void kava() {
     SocialUser user = SecureSocial.getCurrentUser();
     List<Event> events = Event.find("time > ? and event_id != 0 order by time asc", new Date()).fetch();
     render(user, events);
   }
 
   public static void kava_ext(long eventId) {
     Event event = Event.findById(eventId);
     Production production = event.production;
     render(event, production);
   }
 
   public static void account() {
     render();
   }
 
   public static void support() {
     render();
   }
 
   public static void flowers() {
     Map<Integer, Reward> rewards = Application.rewards;
     Map<Integer, Actor> actors = createActors();
     render(rewards,actors);
   }
 
   private static Map<Integer, Actor> createActors() {
     Map<Integer, Actor> actors = new HashMap<Integer, Actor>();
     actors.put(1,new Actor("Rasmus Kaljujärv"));
     actors.put(2,new Actor("Eve Klemets"));
     actors.put(3,new Actor("Risto Kübar"));
     actors.put(4,new Actor("Mirtel Pohla"));
     actors.put(5,new Actor("Jaak Prints"));
     actors.put(6,new Actor("Gert Raudsepp"));
     actors.put(7,new Actor("Stig Rästa"));
     actors.put(8,new Actor("Inga Salurand"));
     actors.put(9,new Actor("Tambet Tuisk"));
     actors.put(10,new Actor("Marika Vaarik"));
     actors.put(11,new Actor("Sergo Vares"));
     return actors;
   }
 }
