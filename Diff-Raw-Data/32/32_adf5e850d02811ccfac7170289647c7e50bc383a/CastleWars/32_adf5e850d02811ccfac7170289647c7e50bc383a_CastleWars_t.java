 package server.model.minigames;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import server.model.players.Client;
 import server.util.Misc;
 import server.model.minigames.*;
 import server.Server;
 /**
  * @Author Sanity for base
  * @Edited by Satan666
  */
 public class CastleWars {
 
     /*
      * Game timers.
      */
     private static final int GAME_TIMER = 200; //1500 * 600 = 900000ms = 15 minutes
     private static final int GAME_START_TIMER = 30;
     /*
      * Hashmap for the waitingroom players
      */
     private static HashMap<Client, Integer> waitingRoom = new HashMap<Client, Integer>();
     /*
      * hashmap for the gameRoom players
      */
     private static HashMap<Client, Integer> gameRoom = new HashMap<Client, Integer>();
     /*
      * The coordinates for the waitingRoom both sara/zammy
      */
     private static final int[][] WAIT_ROOM = {
         {2377, 9485}, //sara
         {2421, 9524} //zammy
     };
     /*
      * The coordinates for the gameRoom both sara/zammy
      */
     private static final int[][] GAME_ROOM = {
         {2426, 3076}, //sara
         {2372, 3131} //zammy
     };
     private static final int[][] FLAG_STANDS = {
         {2429, 3074}, //sara {X-Coord, Y-Coord)
         {2370, 3133} //zammy
     };
     /*
      * Scores for saradomin and zamorak!
      */
     private static int[] scores = {0, 0};
     /*
      * Booleans to check if a team's flag is safe
      */
     private static int zammyFlag = 0;
     private static int saraFlag = 0;
     /*
      * Zamorak and saradomin banner/capes item ID's
      */
     public static final int SARA_BANNER = 4037;
     public static final int ZAMMY_BANNER = 4039;
     public static final int SARA_CAPE = 4041;
     public static final int ZAMMY_CAPE = 4042;
     /*
      * 
      */
     private static int properTimer = 0;
     private static int timeRemaining = -1;
     private static int gameStartTimer = GAME_START_TIMER;
     private static boolean gameStarted = false;
 
     /**
      * Method we use to add someone to the waitinroom in a different method, this will filter out some error messages
      * @param player the player that wants to join
      * @param team the team!
      */
     public static void addToWaitRoom(Client player, int team) {
         if (player == null) {
             return;
         } else if (gameStarted == true) {
             player.sendMessage("There's already a Castle Wars going. Please wait a few minutes before trying again.");
             return;
         } else if (player.playerEquipment[Client.playerHat] > 0 || player.playerEquipment[Client.playerCape] > 0) {
             player.sendMessage("You may not wear capes or helmets inside of castle wars.");
             return;
         }
         toWaitingRoom(player, team);
     }
 
     /**
      * Method we use to transfer to player from the outside to the waitingroom (:
      * @param player the player that wants to join
      * @param team team he wants to be in - team = 1 (saradomin), team = 2 (zamorak), team = 3 (random)
      */
     public static void toWaitingRoom(Client player, int team) {
         if (team == 1) {
             if (getSaraPlayers() > getZammyPlayers() && getSaraPlayers() > 0) {
                 player.sendMessage("The saradomin team is full, try again later!");
                 return;
             }
             if (getZammyPlayers() >= getSaraPlayers() || getSaraPlayers() == 0) {
                 player.sendMessage("You have been added to the @red@Saradomin@bla@ team.");
                 player.sendMessage("Next Game Begins In:@red@ " + ((gameStartTimer * 3) + (timeRemaining * 3)) + " @bla@seconds.");
                 addCapes(player, SARA_CAPE);
                 waitingRoom.put(player, team);
                 player.getPA().movePlayer(WAIT_ROOM[team - 1][0] + Misc.random(5), WAIT_ROOM[team - 1][1] + Misc.random(5), 0);
             }
         } else if (team == 2) {
             if (getZammyPlayers() > getSaraPlayers() && getZammyPlayers() > 0) {
                 player.sendMessage("The zamorak team is full, try again later!");
                 return;
             }
             if (getZammyPlayers() <= getSaraPlayers() || getZammyPlayers() == 0) {
                 player.sendMessage("[@red@RANDOM TEAM@bla@] You have been added to the @red@Zamorak@bla@ team.");
                 player.sendMessage("Next Game Begins In:@red@ " + ((gameStartTimer * 3) + (timeRemaining * 3)) + " @bla@seconds.");
                 addCapes(player, ZAMMY_CAPE);
                 waitingRoom.put(player, team);
                 player.getPA().movePlayer(WAIT_ROOM[team - 1][0] + Misc.random(5), WAIT_ROOM[team - 1][1] + Misc.random(5), 0);
             }
         } else if (team == 3) {
             toWaitingRoom(player, getZammyPlayers() > getSaraPlayers() ? 1 : 2);
             return;
         }
     }
 
     /**
      * Method to add score to scoring team
      * @param player the player who scored
      * @param banner banner id!
      */
     public static void returnFlag(Client player, int wearItem) {
         if (player == null) {
             return;
         }
         if (wearItem != SARA_BANNER && wearItem != ZAMMY_BANNER) {
             return;
         }
         int team = gameRoom.get(player);
         int objectId = -1;
         int objectTeam = -1;
         switch (team) {
             case 1:
                 if (wearItem == SARA_BANNER) {
                     setSaraFlag(0);
                     objectId = 4902;
                     objectTeam = 0;
                     player.sendMessage("returned the sara flag!");
                 } else {
                     objectId = 4903;
                     objectTeam = 1;
                     setZammyFlag(0);
                     scores[0]++; //upping the score of a team; team 0 = sara, team 1 = zammy
                     player.sendMessage("The team of Saradomin scores 1 point!");
                 }
                 break;
             case 2:
                 if (wearItem == ZAMMY_BANNER) {
                     setZammyFlag(0);
                     objectId = 4903;
                     objectTeam = 1;
                     player.sendMessage("returned the zammy flag!");
                 } else {
                     objectId = 4902;
                     objectTeam = 0;
                     setSaraFlag(0);
                     scores[1]++; //upping the score of a team; team 0 = sara, team 1 = zammy
                     player.sendMessage("The team of Zamorak scores 1 point!");
                     zammyFlag = 0;
                 }
                 break;
         }
         changeFlagObject(objectId, objectTeam);
         player.getPA().createPlayerHints(10, -1);
         player.playerEquipment[Client.playerWeapon] = -1;
         player.playerEquipmentN[Client.playerWeapon] = 0;
         player.getItems().updateSlot(3);
         player.appearanceUpdateRequired = true;
         player.updateRequired = true;
         player.getItems().resetItems(3214);
     }
 
     /**
      * Method that will capture a flag when being taken by the enemy team!
      * @param player the player who returned the flag
      * @param team
      */
     public static void captureFlag(Client player) {
         if (player.playerEquipment[Client.playerWeapon] > 0) {
             player.sendMessage("Please remove your weapon before attempting to get the flag again!");
             return;
         }
         int team = gameRoom.get(player);
         if (team == 2 && saraFlag == 0) { //sara flag
             setSaraFlag(1);
             addFlag(player, SARA_BANNER);
             createHintIcon(player, 1);
             changeFlagObject(4377, 0);
         }
         if (team == 1 && zammyFlag == 0) {
             setZammyFlag(1);
             addFlag(player, ZAMMY_BANNER);
             createHintIcon(player, 2);
             changeFlagObject(4378, 1);
         }
     }
 
     /**
      * Method that will add the flag to a player's weapon slot
      * @param player the player who's getting the flag
      * @param flagId the banner id.
      */
     public static void addFlag(Client player, int flagId) {
         player.playerEquipment[Client.playerWeapon] = flagId;
         player.playerEquipmentN[Client.playerWeapon] = 1;
         player.getItems().updateSlot(Client.playerWeapon);
         player.appearanceUpdateRequired = true;
         player.updateRequired = true;
     }
 
     /**
      * Method we use to handle the flag dropping
      * @param player the player who dropped the flag/died
      * @param flagId the flag item ID
      */
     public static void dropFlag(Client player, int flagId) {
         int object = -1;
         switch (flagId) {
             case SARA_BANNER: //sara
                 setSaraFlag(2);
                 object = 4900;
                 createFlagHintIcon(player.getX(), player.getY());
                 break;
             case ZAMMY_BANNER: //zammy
                 setZammyFlag(2);
                 object = 4901;
                 createFlagHintIcon(player.getX(), player.getY());
                 break;
         }
         Iterator iterator = gameRoom.keySet().iterator();
         while (iterator.hasNext()) {
             Client teamPlayer = (Client) iterator.next();
             teamPlayer.getPA().object(object, player.getX(), player.getY(), 0, 10);
         }
     }
 
     /**
      * Method we use to pickup the flag when it was dropped/lost
      * @param Player the player who's picking it up
      * @param objectId the flag object id.
      */
     public static void pickupFlag(Client player) {
         switch (player.objectId) {
             case 4900: //sara
                 setSaraFlag(1);
                 addFlag(player, 4037);
                 break;
             case 4901: //zammy
                 setZammyFlag(1);
                 addFlag(player, 4039);
                 break;
         }
         createHintIcon(player, (gameRoom.get(player) == 1) ? 2 : 1);
         Iterator iterator = gameRoom.keySet().iterator();
         while (iterator.hasNext()) {
             Client teamPlayer = (Client) iterator.next();
             teamPlayer.getPA().createObjectHints(player.objectX, player.objectY, 170, -1);
             teamPlayer.getPA().object(-1, player.objectX, player.objectY, 0, 10);
         }
         return;
     }
 
     /**
      * Hint icons appear to your team when a enemy steals flag
      * @param player the player who took the flag
      * @param t team of the opponent team. (: 
      */
     public static void createHintIcon(Client player, int t) {
         Iterator iterator = gameRoom.keySet().iterator();
         while (iterator.hasNext()) {
             Client teamPlayer = (Client) iterator.next();
             System.out.println(teamPlayer.playerName + " => Team => " + gameRoom.get(teamPlayer));
             System.out.println("desired team = " + t);
             teamPlayer.getPA().createPlayerHints(10, -1); 
             if (gameRoom.get(teamPlayer) == t) {
                 System.out.println("created hint icons for playername " + teamPlayer.playerName + " and team number: " + t);
                 teamPlayer.getPA().createPlayerHints(10, player.playerId);
                 teamPlayer.getPA().requestUpdates();
             }
         }
     }
 
     /**
      * Hint icons appear to your team when a enemy steals flag
      * @param player the player who took the flag
      * @param t team of the opponent team. (:
      */
     public static void createFlagHintIcon(int x, int y) {
         Iterator iterator = gameRoom.keySet().iterator();
         while (iterator.hasNext()) {
             Client teamPlayer = (Client) iterator.next();
             teamPlayer.getPA().createObjectHints(x, y, 170, 2);
         }
     }
 
     /**
      * This method is used to get the teamNumber of a certain player
      * @param player
      * @return
      */
     public static int getTeamNumber(Client player) {
         if (player == null) {
             return -1;
         }
         if (gameRoom.containsKey(player)) {
             return gameRoom.get(player);
         }
         return -1;
     }
 
     /**
      * The leaving method will be used on click object or log out
      * @param player player who wants to leave
      */
     public static void leaveWaitingRoom(Client player) {
         if (player == null) {
             System.out.println("player is null");
             return;
         }
         if (waitingRoom.containsKey(player)) {
             waitingRoom.remove(player);
             player.getPA().createPlayerHints(10, -1);
             player.sendMessage("You left your team!");
             deleteGameItems(player);
             player.getPA().movePlayer(2439 + Misc.random(4), 3085 + Misc.random(5), 0);
             return;
         }
         player.getPA().movePlayer(2439 + Misc.random(4), 3085 + Misc.random(5), 0);
         System.out.println("Waiting room map does not contain " + player.playerName);
     }
 
     public static void process() {
         if (properTimer > 0) {
             properTimer--;
             return;
         } else {
             properTimer = 4;
         }
         if (gameStartTimer > 0) {
             gameStartTimer--;
             updatePlayers();
         } else if (gameStartTimer == 0) {
             startGame();
         }
         if (timeRemaining > 0) {
             timeRemaining--;
             updateInGamePlayers();
         } else if (timeRemaining == 0) {
             endGame();
         }
     }
 
     /**
      * Method we use to update the player's interface in the waiting room
      */
     public static void updatePlayers() {
         Iterator iterator = waitingRoom.keySet().iterator();
         while (iterator.hasNext()) {
             Client player = (Client) iterator.next();
             if (player != null) {
                 player.getPA().sendFrame126("Next Game Begins In:@red@ " + ((gameStartTimer * 3) + (timeRemaining * 3)) + " @whi@seconds.", 6570);
                 player.getPA().sendFrame126("Zamorak Players: @red@" + getZammyPlayers() + "@whi@.", 6572);
                 player.getPA().sendFrame126("Saradomin Players: @red@" + getSaraPlayers() + "@whi@.", 6664);
                 player.getPA().walkableInterface(6673);
             }
         }
     }
 
     /**
      * Method we use the update the player's interface in the game room
      */
     public static void updateInGamePlayers() {
         if (getSaraPlayers() > 0 && getZammyPlayers() > 0) {
             Iterator iterator = gameRoom.keySet().iterator();
             while (iterator.hasNext()) {
                 Client player = (Client) iterator.next();
                 int config;
                 if (player == null) {
                     continue;
                 }
                 player.getPA().walkableInterface(11146);
                 player.getPA().sendFrame126("Zamorak = " + scores[1], 11147);
                 player.getPA().sendFrame126(scores[0] + " = Saradomin", 11148);
                 player.getPA().sendFrame126(timeRemaining * 3 + " secs", 11155);
                 config = (2097152 * saraFlag);
                 player.getPA().sendFrame87(378, config);
                 config = (2097152 * zammyFlag); //flags 0 = safe 1 = taken 2 = dropped
                 player.getPA().sendFrame87(377, config);
             }
         }
     }
 
     /*
      * Method that will start the game when there's enough players.
      */
     public static void startGame() {
         if (getSaraPlayers() < 1 || getZammyPlayers() < 1) {
             gameStartTimer = GAME_START_TIMER;
             return;
         }
         gameStartTimer = -1;
         System.out.println("Starting Castle Wars game.");
         gameStarted = true;
         timeRemaining = GAME_TIMER / 2;
         Iterator iterator = waitingRoom.keySet().iterator();
         while (iterator.hasNext()) {
             Client player = (Client) iterator.next();
             int team = waitingRoom.get(player);
             if (player == null) {
                 continue;
             }
             player.getPA().walkableInterface(-1);
             player.getPA().movePlayer(GAME_ROOM[team - 1][0] + Misc.random(3), GAME_ROOM[team - 1][1] - Misc.random(3), 1);
             player.getPA().movePlayer(GAME_ROOM[team - 1][0] + Misc.random(3), GAME_ROOM[team - 1][1] - Misc.random(3), 1);
             gameRoom.put(player, team);
         }
         waitingRoom.clear();
     }
 
     /*
      * Method we use to end an ongoing cw game.
      */
     public static void endGame() {
         Iterator iterator = gameRoom.keySet().iterator();
         while (iterator.hasNext()) {
             Client player = (Client) iterator.next();
             int team = gameRoom.get(player);
             if (player == null) {
                 continue;
             }
             player.cwGames++;
             player.getPA().movePlayer(2440 + Misc.random(3), 3089 - Misc.random(3), 0);
             player.sendMessage("[@red@CASTLE WARS@bla@] The Castle Wars Game has ended!");
             player.sendMessage("[@red@CASTLE WARS@bla@] Kills: @red@ " + player.cwKills + " @bla@Deaths:@red@ " + player.cwDeaths + "@bla@ Games Played: @red@" + player.cwGames + "@bla@.");
             player.getPA().createPlayerHints(10, -1);
             deleteGameItems(player);
             if (scores[0] == scores[1]) {
                 player.getItems().addItem(4067, 30);
                 player.sendMessage("Tie game! You gain 30 CastleWars tickets!");
             } else if (team == 1) {
                 if (scores[0] > scores[1]) {
                     player.getItems().addItem(4067, 50);
                     player.sendMessage("You won the CastleWars Game. You received 50 CastleWars Tickets!");
                 } else if (scores[0] < scores[1]) {
                     player.getItems().addItem(4067, 20);
                     player.sendMessage("You lost the CastleWars Game. You received 20 CastleWars Tickets!");
                 }
             } else if (team == 2) {
                 if (scores[1] > scores[0]) {
                     player.getItems().addItem(4067, 50);
                     player.sendMessage("You won the CastleWars Game. You received 50 CastleWars Tickets!");
                 } else if (scores[1] < scores[0]) {
                     player.getItems().addItem(4067, 20);
                     player.sendMessage("You lost the CastleWars Game. You received 20 CastleWars Tickets!");
                 }
             }
         }
         resetGame();
     }
 
     /**
      * reset the game variables
      */
     public static void resetGame() {
         changeFlagObject(4902, 0);
         changeFlagObject(4903, 1);
         setSaraFlag(0);
         setZammyFlag(0);
         timeRemaining = -1;
         System.out.println("Ending Castle Wars game.");
         gameStartTimer = GAME_START_TIMER;
         gameStarted = false;
         gameRoom.clear();
     }
 
     /**
      * Method we use to remove a player from the game
      * @param player the player we want to be removed
      */
     public static void removePlayerFromCw(Client player) {
         if (player == null) {
             System.out.println("Error removing player from castle wars [REASON = null]");
             return;
         }
         if (gameRoom.containsKey(player)) {
             /*
              * Logging/leaving with flag
              */
             if (player.getItems().playerHasEquipped(SARA_BANNER)) {
                 player.getItems().removeItem(player.playerEquipment[3], 3);
                 setSaraFlag(0); //safe flag
             } else if (player.getItems().playerHasEquipped(ZAMMY_BANNER)) {
                 player.getItems().removeItem(player.playerEquipment[3], 3);
                 setZammyFlag(0); //safe flag
             }
             deleteGameItems(player);
             player.getPA().movePlayer(2440, 3089, 0);
             System.out.println("Deleting playername test: " + player.playerName);
             player.sendMessage("[@red@CASTLE WARS@bla@] The Casle Wars Game has ended for you!");
             player.sendMessage("[@red@CASTLE WARS@bla@] Kills: @red@ " + player.cwKills + " @bla@Deaths:@red@ " + player.cwDeaths + "@bla@.");
             player.getPA().createPlayerHints(10, -1);
             gameRoom.remove(player);
         }
         if (getZammyPlayers() <= 0 || getSaraPlayers() <= 0) {
             endGame();
         }
     }
 
     /**
      * Will add a cape to a player's equip
      * @param player the player
      * @param capeId the capeId
      */
     public static void addCapes(Client player, int capeId) {
         player.playerEquipment[Client.playerCape] = capeId;
         player.playerEquipmentN[Client.playerCape] = 1;
         player.getItems().updateSlot(Client.playerCape);
         player.appearanceUpdateRequired = true;
         player.updateRequired = true;
     }
 
     /**
      * This method will delete all items received in game. Easy to add items to the array. (:
      * @param player the player who want the game items deleted from.
      */
     public static void deleteGameItems(Client player) {
         switch (player.playerEquipment[3]) {
             case 4037:
             case 4039:
                 player.getItems().removeItem(player.playerEquipment[3], 3);
                 System.out.println("removed weapon:" + player.playerEquipment[3]);
                 break;
         }
         switch (player.playerEquipment[1]) {
             case 4042:
             case 4041:
                 player.getItems().removeItem(player.playerEquipment[1], 1);
                 System.out.println("removed cape:" + player.playerEquipment[1]);
                 break;
         }
         int[] items = {4049, 1265, 4045, 4053, 4042, 4041, 4037, 4039};
         for (int i = 0; i < items.length; i++) {
             if (player.getItems().playerHasItem(items[i])) {
                 player.getItems().deleteItem2(items[i], player.getItems().getItemAmount(items[i]));
             }
         }
     }
 
     /**
      * Methode we use to get the zamorak players
      * @return the amount of players in the zamorakian team!
      */
     public static int getZammyPlayers() {
         int players = 0;
         Iterator iterator = (!waitingRoom.isEmpty()) ? waitingRoom.values().iterator() : gameRoom.values().iterator();
         while (iterator.hasNext()) {
             if ((Integer) iterator.next() == 2) {
                 players++;
             }
         }
         return players;
     }
 
     /**
      * Method we use to get the saradomin players!
      * @return the amount of players in the saradomin team!
      */
     public static int getSaraPlayers() {
         int players = 0;
         Iterator iterator = (!waitingRoom.isEmpty()) ? waitingRoom.values().iterator() : gameRoom.values().iterator();
         while (iterator.hasNext()) {
             if ((Integer) iterator.next() == 1) {
                 players++;
             }
         }
         return players;
     }
 
     /**
      * Method we use for checking if the player is in the gameRoom
      * @param player player who will be checking
      * @return
      */
     public static boolean isInCw(Client player) {
         return gameRoom.containsKey(player);
     }
 
     /**
      * Method we use for checking if the player is in the waitingRoom
      * @param player player who will be checking
      * @return
      */
     public static boolean isInCwWait(Client player) {
         return waitingRoom.containsKey(player);
     }
 
     /**
      * Method to make sara flag change status 0 = safe, 1 = taken, 2 = dropped
      * @param status
      */
     public static void setSaraFlag(int status) {
         saraFlag = status;
     }
 
     /**
      * Method to make zammy flag change status 0 = safe, 1 = taken, 2 = dropped
      * @param status
      */
     public static void setZammyFlag(int status) {
         zammyFlag = status;
     }
 
     /**
      * Method we use for the changing the object of the flag stands when capturing/returning flag
      * @param objectId the object
      * @param team the team of the player
      */
     public static void changeFlagObject(int objectId, int team) {
         Iterator iterator = gameRoom.keySet().iterator();
         while (iterator.hasNext()) {
             Client teamPlayer = (Client) iterator.next();
             teamPlayer.getPA().object(objectId, FLAG_STANDS[team][0], FLAG_STANDS[team][1], 0, 10);
         }
     }
 }
