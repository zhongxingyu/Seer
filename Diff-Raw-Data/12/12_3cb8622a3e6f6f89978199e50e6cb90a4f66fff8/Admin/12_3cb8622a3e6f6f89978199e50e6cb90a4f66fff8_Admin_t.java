 package controllers;
 
 import models.GameBet;
 import models.User;
 import play.Play;
 import play.mvc.Controller;
 import securesocial.provider.SocialUser;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.TimeZone;
 
//@With( SecureSocial.class )
 public class Admin extends Controller {
 
     public static void adminPanel() {
         makeSureUserIsAdmin();
 
         boolean hasTournamentStarted = Application.hasTournamentStarted();
         boolean hasTest1Started = hasTest1Started();
         boolean hasTest2Started = hasTest2Started();
         render(hasTournamentStarted, hasTest1Started, hasTest2Started);
     }
 
     public static void users() {
        //makeSureUserIsAdmin();
 
         List<User> users = User.getAllIdSorted();
         for (User user : users) {
             user.betsPlaced = GameBet.getNumberOfBetsPlaced(user);
         }
         render(users);
     }
 
     public static void topList() {
         makeSureUserIsAdmin();
 
         List<User> users = User.getAllPointSorted();
         render(users);
     }
 
 
     public static void setUserGroup(Long userId, String group) {
         System.out.println("UserId: " + userId + ", group: " + group);
        //makeSureUserIsAdmin();
         User user = User.findById(userId);
         if (user != null) {
             user.group = group;
             user.save();
             users();
         } else {
             error("User with given id not found!");
         }
     }
 
     public static void deleteResults() {
         makeSureUserIsAdmin();
 
         int deletedResults = GameBet.deleteResults();
         render(deletedResults);
     }
 
     public static void sumPoints() {
         makeSureUserIsAdmin();
         List<String> userPoints = new ArrayList<String>();
         List<User> users = User.findAll();
         for (User user : users) {
             user.points = GameBet.getPointsForUser(user);
             user.save();
             userPoints.add(String.format("UserId %d: %d points", user.id, user.points));
         }
         render(userPoints);
     }
 
     private static void makeSureUserIsAdmin() {
         if (Play.mode.isProd()) {
             SocialUser user = Application.getSocialUser();
             User emUser = User.getUser(user);
             if (!emUser.isResultUser()) {
                 error("Sorry, you are not admin!");
             }
         }
     }
 
 
 
     static boolean hasTest1Started() {
         Calendar tournamentStart = Calendar.getInstance(TimeZone.getTimeZone("CET"));
         tournamentStart.set(2012,Calendar.JUNE,30,21,50);
         Calendar now = Calendar.getInstance();
         return now.after(tournamentStart);
     }
 
     static boolean hasTest2Started() {
         Calendar tournamentStart = Calendar.getInstance(TimeZone.getTimeZone("CET"));
         tournamentStart.set(2012,Calendar.JUNE,1,22,30);
         Calendar now = Calendar.getInstance();
         return now.after(tournamentStart);
     }
 
 }
