 package game;
 
 
 import ui.render.StringRender;
 
 import java.util.Random;
 
 /**
  *
  * The RandomEvent class is called every Developement round;   It contains a static method that will decide
  * whether or not an event should happen then call the appopriate methods if a random event should happen
  *
  * @author Matt Gruchacz, Nick Teissler
  *
  */
 public class RandomEvent
 {
     private static Random random = new Random();
     private static Session session;
     private static int[] multiplier = {25,25,25,50,50,50,50,75,75,75,75,100};
     private static final String[] goodEvents = {
             "YOU JUST RECEIVED A PACKAGE FROM THE GT ALUMNI CONTAINING 3 FOOD AND 2 ENERGY UNITS.",
             "A WANDERING TECH STUDENT REPAID YOUR HOSPITALITY BY LEAVING TWO BARS OF ORE.",
             "THE MUSEUM BOUGHT YOUR ANTIQUE PERSONAL COMPUTER FOR $%d.", //8*m
             "YOU FOUND A DEAD MOOSE RAT AND SOLD THE HIDE FOR $%d" //2*m
     };
     private static final String[] badEvents = {
             "FLYING CAT-BUGS ATE THE ROOF OFF YOUR HOUSE. REPAIRS COST $%d.",//4*m
             "MISCHIEVOUS UGA STUDENTS BROKE INTO YOUR STORAGE SHED AND STOLE HALF YOUR FOOD.",
             "YOUR SPACE GYPSY INLAWS MADE A MESS OF THE TOWN. IT COST YOU $%d TO CLEAN IT UP."//6*m
     };
 
 
     /**
      * The generateEvent method decides whether or not an event should happen then randomly calls a good or bad
      * event
      *
      * @param sess - the session of the game
      * @param needsBadEvent - whether or not it is possible for their to be a bad event
      * @param eventString1 The eventString object on the first line, passed by reference from the TownScreen class, added
      *                    to the RenderableStrings arrayList in TownScreen.
      * @param eventString2 See above, second line though.
      */
 
     public static void generateEvent(Session sess, boolean needsBadEvent, StringRender eventString1,StringRender eventString2)
     {
          session = sess;
          if (!needsBadEvent)
          {
              int randInt = random.nextInt(100);
              if (randInt <= 26)
              {
                  generateGoodEvent(eventString1,eventString2);
              }
          }
         else
          {
             int firstRand = random.nextInt(100);
             int secondRand = random.nextInt(7);
             if (firstRand <= 26)
             {
                 if (secondRand <= 4)
                 {
                     generateGoodEvent(eventString1,eventString2);
                 }
                 else
                 {
                       generateBadEvent(eventString1,eventString2);
                 }
             }
          }
     }
 
     /**
      * generateGood event is a method that randomly picks a good event for the current player!
      *
      */
     private static void generateGoodEvent(StringRender eventString1,StringRender eventString2)
     {
         int randInt = random.nextInt(4);
         String event = goodEvents[randInt];
         String currentPlayerID = session.getCurrentPlayerId();
         switch (randInt)
         {
             case 0:
                 session.incrementFood(currentPlayerID,3);
                 session.incrementEnergy(currentPlayerID,2);
                 eventString1.setText(event.substring(0,47));
                 eventString2.setText(event.substring(47));
                 break;
             case 1:
                 session.incrementOre(currentPlayerID,2);
                 eventString1.setText(event.substring(0,37));
                 eventString2.setText(event.substring(37));
                 break;
             case 2:
                event = String.format(event,8*multiplier[session.getCurrentRound()]) ;
                session.incrementMoney(currentPlayerID,8*multiplier[session.getCurrentRound()]);
                 eventString1.setText(event.substring(0,40));
                 eventString2.setText(event.substring(40));
                 break;
             case 3:
                event = String.format(event,2*multiplier[session.getCurrentRound()]);
                session.incrementMoney(currentPlayerID, 2*multiplier[session.getCurrentRound()]);
                 eventString1.setText(event);
                 eventString2.setText("");
                 break;
             default:
                 System.out.println("Error in random event generation");
                 System.exit(0);
         }
     }
 
     /**
      * generateBadEvent is a method that randomly picks a bad event for the current player!
      */
     private static void generateBadEvent(StringRender eventString1, StringRender eventString2)
     {
         int randInt = random.nextInt(3);
         String event = badEvents[randInt];
         String currentPlayerID = session.getCurrentPlayerId();
         switch (randInt)
         {
             case 0:
                event = String.format(event,4*multiplier[session.getCurrentRound()]);
                session.incrementMoney(currentPlayerID,  -4*multiplier[session.getCurrentRound()]);
                 eventString1.setText(event.substring(0,38));
                 eventString2.setText(event.substring(38));
                 break;
             case 1:
                 int food = session.getPlayerFood(currentPlayerID);
                 int halfFood = food/2;
                 session.incrementFood(currentPlayerID,session.getPlayerFood(currentPlayerID)-halfFood);
                 eventString1.setText(event.substring(0,41));
                 eventString2.setText(event.substring(41));
                 break;
             case 2:
                event = String.format(event,6*multiplier[session.getCurrentRound()]);
                session.incrementMoney(currentPlayerID, -6*multiplier[session.getCurrentRound()]);
                 eventString1.setText(event.substring(0,34));
                 eventString2.setText(event.substring(34));
 
                 break;
             default:
                 System.out.println("Error in random event generation");
                 System.exit(0);
         }
 
     }
 
 }
