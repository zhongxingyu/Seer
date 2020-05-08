 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import org.yaml.snakeyaml.Yaml;
 
 import models.*;
 
 public class WeightOff extends Controller {
 
     @Before
     static void checkAuthenticated() {
         if (!(session.contains("user") && User.exists(session.get("user")))) {
             Authentication.login();
         }
     }
      
     // home screen
     public static void index() {
         User user = User.find("byOpenid", session.get("user")).first();
         render(user);
     }
     
     // add a new entry - form
     public static void add() {
         render();
     }
     
     // process new entry
     public static void addWeight(String weight, String type) {
     
         // get user 
         User user = User.find("byOpenid", session.get("user")).first();
         
        int wInt = 0;
        try {
            // get input weight amount as int
            double wD = Double.parseDouble(weight);
            wInt = (int)Math.round(wD);
        } catch (NumberFormatException e) {
            flash.put("numError", "A real number, please");
            add();
        }
         
         // determine type
         String t = "kg";
         if (type.equals("lb")) {
             t = "lb";
         }
         
         Weight w = new Weight(user, wInt, t);
         w.save();
         
         index();
     }
     
     
     /*
      * TODO note currently we support 1x 2 player games, 
      * although we don't enforce that in the schema
      */
     // show results of the current weights
     public static void current() {
         User player1 = User.find("byOpenid", session.get("user")).first();
         int p1Change = player1.getLastWeight().kg - player1.getFirstWeight().kg;
         
         if (player1.inGame()) {
             // player 1's games, get 1st, get list of opponents, get 1st
             User player2 = player1.games.get(0).getOpponents(player1).get(0);
             int p2Change = player2.getLastWeight().kg - player2.getFirstWeight().kg;
             
             String winner = "";
             if (p1Change < p2Change) {
                 winner = "player1";
             } else if (p1Change > p2Change) {
                 winner = "player2";
             } else {
                 winner = "draw";
             }
              
             // if in a game, use the game template (default)
             render(player1, p1Change, player2, p2Change, winner);
         } else {
             // else use the single player template
             render("WeightOff/single.html", player1, p1Change);
         }
         
     }
     
     // show results of the previous week
     public static void lastWeek() {
         index();
     }
     
     // results over time
     public static void overTime() {
     }
          
 }
