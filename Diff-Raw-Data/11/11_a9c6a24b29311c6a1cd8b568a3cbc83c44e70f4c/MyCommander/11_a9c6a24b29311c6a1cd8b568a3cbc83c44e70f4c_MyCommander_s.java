 
 package commander;
 
 import java.util.*;
 
 import com.aisandbox.cmd.*;
 import com.aisandbox.cmd.info.*;
 import com.aisandbox.cmd.cmds.*;
 import com.aisandbox.util.*;
 
 
 public class MyCommander extends SandboxCommander {
     private String myTeam;
     private String enemyTeam;
 
     private int myTeamSize;
 
     private Vector2 middle;
     private Vector2 left;
     private Vector2 right;
     private Vector2 front;
 
     private Vector2 myFlagSpawnLocation;
     private Vector2 enemyFlagSpawnLocation;
 
     private Vector2 myScoreLocation;
     private Vector2 enemyScoreLocation;
 
     private BotInfo attacker;
     private BotInfo defender;
 
 
     private ArrayList<BotInfo> attackers;
     private ArrayList<BotInfo> defenders;
     private ArrayList<BotInfo> scouts;
     private ArrayList<BotInfo> avengers;
 
 
     /**
      * Custom commander class construtor.
      */
     public MyCommander() {
         name = "JavaCmd";
 
         attacker = null;
         defender = null;
 
 
     }
 
     /**
      * Called when the server sends the "initialize" message to the commander.
      * Use this function to setup your bot before the game starts.
      * You can also set this.verbose = true to get more information about each bot visually.
      */
     @Override
     public void initialize() {
         // set the name of our and the enemy teams
         myTeam = gameInfo.getTeam();
         enemyTeam = gameInfo.getEnemyTeam();
 
         myTeamSize = gameInfo.getMyTeamInfo().getMembers().size();
 
 
         attackers = new ArrayList<BotInfo>();
         defenders = new ArrayList<BotInfo>();
         scouts = new ArrayList<BotInfo>();
         avengers = new ArrayList<BotInfo>();
         // Calculate flag positions and store the middle.
         Vector2 ours = gameInfo.getMyFlagInfo().getPosition();
         Vector2 theirs = gameInfo.getEnemyFlagInfo().getPosition();
         middle = new Vector2((ours.getX() + theirs.getX()) / 2, (ours.getY() + theirs.getY()) / 2);
 
         //get flag spawn locations
         myFlagSpawnLocation = levelInfo.getFlagSpawnLocations().get(myTeam);
         enemyFlagSpawnLocation = levelInfo.getFlagSpawnLocations().get(enemyTeam);
 
         //get flag score locations
         myScoreLocation = levelInfo.getFlagScoreLocations().get(myTeam);
         enemyScoreLocation = levelInfo.getFlagScoreLocations().get(enemyTeam);
 
         // Now figure out the flaking directions, assumed perpendicular.
         Vector2 d = new Vector2(ours);
         d.sub(theirs);
         left = new Vector2(-d.getY(), d.getX()).normalize();
         right = new Vector2(d.getY(), -d.getX()).normalize();
         front = new Vector2(d.getX(), d.getY()).normalize();
 
         // display the command descriptions next to the bot labels
         verbose = true;
     }
 
     /**
      * Called when the server sends a "tick" message to the commander.
      * Override this function for your own bots.  Here you can access all the information in this.gameInfo,
      * which includes game information, and this.levelInfo which includes information about the level.
      * You can send commands to your bots using the issue() method in this class.
      */
     @Override
     public void tick() {
           System.out.println("list size: " + attackers.size());
 
         if (attacker != null && attacker.getHealth() <= 0)
             attacker = null; // the attacker is dead we'll pick another when available
 
         if (defender != null && defender.getHealth() <= 0)
             defender = null; // the defender is dead we'll pick another when available
 
         for (int i = 0; i < attackers.size(); i++) {
             if (attackers.get(i) != null && attackers.get(i).getHealth() <= 0) {
                 System.out.println(attackers.get(i).getHealth());
                 attackers.remove(attackers.get(i));
             }
         }
 
         // loop through all living bots without orders
         for (BotInfo bot : gameInfo.botsAvailable()) {
 
             if (!isBotAssigned(bot)) {
                 attackers.add(bot);
                 System.out.println(bot.getName());
             } else {
                 removeFromList(attackers, bot);
                 attackers.add(bot);
                 int botIndex = attackers.indexOf(bot);
                 System.out.println(botIndex);
 
                 if (attackers.get(botIndex).hasFlag()) {
                     // tell the flag carrier to run home
                     System.out.println((attackers.get(botIndex)) + " has the flag");
                     Vector2 target = levelInfo.getFlagScoreLocations().get(myTeam);
                     issue(new MoveCmd(attackers.get(botIndex).getName(), target, "running home"));
                 } else {
                     if (attackers.get(botIndex).getVisibleEnemies().size() > 0) {
                         List<String> visibleEnemies = bot.getVisibleEnemies();
                         Vector2 target = gameInfo.getEnemyFlagInfo().getPosition();
 
                         issue(new AttackCmd(attackers.get(botIndex).getName(), target, null, "enemies seen"));
                     } else {
 
                         System.out.println("Attacker does not have flag");
                         Vector2 target = gameInfo.getEnemyFlagInfo().getPosition();
                         Vector2 flank = getFlankingPosition(bot, target);
                         System.out.println("Set target and flank position to " + target + " and " + flank);
                         if (target.sub(flank).length() > attackers.get(botIndex).getPosition().sub(target).length()) {
                             issue(new AttackCmd(attackers.get(botIndex).getName(), target, null, "attack from flank"));
                             System.out.println(attackers.get(botIndex).getName() + " attacking flag from flank");
                         } else {
                             flank = levelInfo.findNearestFreePosition(flank);
                             issue(new ChargeCmd(attackers.get(botIndex).getName(), flank, "running to flank"));
                             System.out.println(attackers.get(botIndex).getName() + " running to flank");
                         }
                         }
                     }
                 }
             }
     }
 
 
     private Vector2 getFlankingPosition(BotInfo bot, Vector2 target) {
         List<Vector2> options = new ArrayList<Vector2>();
         Vector2 l = levelInfo.findNearestFreePosition(left.scale(16f).add(target));
         if (l != null) options.add(l);
         Vector2 r = levelInfo.findNearestFreePosition(right.scale(16f).add(target));
         if (r != null) options.add(r);
 
         if (options.size() > 0) {
             Collections.sort(options);
             return options.get(0);
         } else
             return target;
     }
 
     //check if bot is assigned to a list
     private boolean isBotAssigned(BotInfo bot) {
         String botName = bot.getName();
         if (attackers != null) {
             for (BotInfo bot2 : attackers)
                 if (bot2.getName().equals(botName)) {
                     System.out.println("Bot is assigned");
                     return true;
                 }
         }
         System.out.println("Bot is not assigned");
         return false;
     }
 
     private void removeFromList(ArrayList<BotInfo> list, BotInfo bot) {
         String botName = bot.getName();
         for (BotInfo botInList : list) {
             if (botInList.getName().equals(botName)) {
                 System.out.println("Match found for removal");
                int i = list.indexOf(botInList);
                list.remove(bot);
             }
         }
     }
 
     //add bots to lists
     private void determineRoles(BotInfo bot) {
         if ((attackers.size() >= 0) && (attackers.size() < (myTeamSize / 4)))
             addAttackers(bot);
         else if ((defenders.size() >= 0) && (defenders.size() < (myTeamSize / 4)))
             addDefenders(bot);
         else if ((scouts.size() >= 0) && (scouts.size() < (myTeamSize / 4)))
             addScouts(bot);
         else
             addAvengers(bot);
     }
 
     //adds to attacker list
     private void addAttackers(BotInfo bot) {
 
         if (!isBotAssigned(bot)) {
             String botName = bot.getName();
             if (botName != null) {
                 attackers.add(bot);
                 System.out.println("attacker " + botName + " added to list");
             }
         } else {
         }
     }
 
     //adds to defender list
     private void addDefenders(BotInfo bot) {
         if (!isBotAssigned(bot)) {
             String botName = bot.getName();
             if (botName != null) {
                 defenders.add(bot);
                 System.out.println("defender " + botName + " added to list");
             }
         } else {
         }
     }
 
     //adds to scout list
     private void addScouts(BotInfo bot) {
         if (!isBotAssigned(bot)) {
             String botName = bot.getName();
             if (botName != null) {
                 scouts.add(bot);
                 System.out.println("scout " + botName + " added to list");
             }
         } else {
         }
     }
 
     //adds to avenger list
     private void addAvengers(BotInfo bot) {
         if (!isBotAssigned(bot))
             avengers.add(bot);
         else {
         }
     }
 
     private void attackerBehavior(BotInfo bot) {
         System.out.println("attack method called");
         if (attackers.contains(bot)) {
             System.out.println("bot is in attackers list");
             if (bot.hasFlag()) {
                 // tell the flag carrier to run home
                 Vector2 target = levelInfo.getFlagScoreLocations().get(myTeam);
                 issue(new MoveCmd(bot.getName(), target, "running home"));
             } else {
                 Vector2 target = gameInfo.getEnemyFlagInfo().getPosition();
                 Vector2 flank = getFlankingPosition(bot, target);
                 if (target.sub(flank).length() > bot.getPosition().sub(target).length())
                     issue(new AttackCmd(bot.getName(), target, null, "attack from flank"));
                 else {
                     flank = levelInfo.findNearestFreePosition(flank);
                     issue(new MoveCmd(bot.getName(), flank, "running to flank"));
                 }
             }
         }
     }
 
 
     private void defenderBehavior(BotInfo bot) {
         if (defenders.contains(bot)) {
             Vector2 targetPosition = levelInfo.getFlagSpawnLocations().get(myTeam);
             Vector2 targetMin = targetPosition.sub(new Vector2(2f, 2f));
             Vector2 targetMax = targetPosition.add(new Vector2(2f, 2f));
             Vector2 goal = levelInfo.findRandomFreePositionInBox(new Area(targetMin, targetMax));
 
             if (goal.sub(bot.getPosition()).length() > 8f)
                 issue(new ChargeCmd(bot.getName(), goal, "running to defend"));
             else {
                 List<FacingDirection> dirs = new ArrayList<FacingDirection>();
                 dirs.add(new FacingDirection(left.sub(bot.getPosition()), 1));
                 dirs.add(new FacingDirection(middle.sub(bot.getPosition()), 1));
                 issue(new DefendCmd(bot.getName(), dirs, "turning to defend"));
             }
         }
     }
 
     private void scoutBehavior(BotInfo bot) {
         if (scouts.contains(bot)) {
             Vector2 target = levelInfo.findRandomFreePositionInBox(levelInfo.getLevelArea());
             if (target != null)
                 issue(new AttackCmd(bot.getName(), target, null, "random patrol"));
         }
 
     }
 
     private void avengerBehavior(BotInfo bot) {
         if (avengers.contains(bot)) {
             issue(new AttackCmd(bot.getName(), enemyScoreLocation, null, "Avenging flag"));
         }
 
     }
 
     /**
      * Called when the server sends the "shutdown" message to the commander.
      * Use this function to teardown your bot after the game is over.
      */
     @Override
     public void shutdown() {
         System.out.println("<shutdown> message received from server");
     }
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
