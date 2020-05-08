 package com.github.detro.rps;
 
 import org.apache.log4j.Logger;
 
 import java.security.MessageDigest;
 import java.util.Formatter;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * This is a logical representation of a match between 2 players.
  * It keeps track of the number of players, maintaining a useful
  * status to know at which "point" in the game we are.
  *
  * It will be useful to represent the concept of "Room" where max 2 players are allowed
  * to join and play.
  *
  * TODO Map error cases to different Exceptions: having all "RuntimeException" is very limiting in terms of error handling
  */
 public class Match {
     private static final Logger LOG = Logger.getLogger(Match.class);
 
     private final String id;
 
     public static final int NO_PLAYERS_YET             = 0x0001;
     public static final int WAITING_FOR_ANOTHER_PLAYER = 0x0003;
     public static final int WAITING_PLAYERS_WEAPONS    = 0x0004;
     public static final int PLAYED                     = 0x0008;
 
     private int status = NO_PLAYERS_YET;
 
     private Map<String, Integer> playersAndWeapons = new HashMap<String, Integer>(2);
 
     private static final int NO_WEAPON_YET = -1;
 
     public Match(String seed) {
         id = sha1(seed + System.currentTimeMillis());
 
         LOG.debug(String.format("Created new Match '%s'", id));
     }
 
     public String getId() {
         return id;
     }
 
     public int getStatus() {
         return status;
     }
 
     public int playersAmount() {
         return playersAndWeapons.keySet().size();
     }
 
     public boolean containsPlayer(String playerId) {
         return playersAndWeapons.keySet().contains(playerId);
     }
 
     private boolean areWeaponsReady() {
         if (playersAmount() != 2) {
             return false;
         }
 
         for (Map.Entry<String, Integer> playerAndWeapon : playersAndWeapons.entrySet()) {
             if (playerAndWeapon.getValue() == NO_WEAPON_YET) {
                 return false;
             }
         }
 
         return true;
     }
 
    public void addPlayer(String playerId) {
         // Add player to the match if we can
         if ((status & WAITING_FOR_ANOTHER_PLAYER) > 0) {
             if (!containsPlayer(playerId)) {
                 playersAndWeapons.put(playerId, NO_WEAPON_YET);    //< new player has not provided hiw weapon of choice yet
             } else {
                 String errMsg = String.format("Can't add the Player '%s' twice to Match '%s'", playerId, id);
                 LOG.error(errMsg);
                 throw new RuntimeException(errMsg);
             }
         } else {
             String errMsg = "Too many Players in this Match: " + id;
             LOG.error(errMsg);
             throw new RuntimeException(errMsg);
         }
 
         // Update Match status
         if (playersAmount() == 2) {
             status = WAITING_PLAYERS_WEAPONS;
         } else {
             status = WAITING_FOR_ANOTHER_PLAYER;
         }
 
         LOG.debug(String.format("Added Player '%s' to Match '%s', now in status '%s'", playerId, id, status));
     }
 
    public void setPlayerWeapon(String playerId, int weaponIdx) {
         // Check player is part of this match
         if (!containsPlayer(playerId)) {
             String errMsg = String.format("Player '%s' is not part of Match '%s'", playerId, id);
             LOG.error(errMsg);
             throw new RuntimeException(errMsg);
         }
 
         // Is the Match "ready to begin"?
         if (status != WAITING_PLAYERS_WEAPONS) {
             String errMsg = String.format("Match '%s' has not started yet", id);
             LOG.error(errMsg);
             throw new RuntimeException(errMsg);
         }
 
         // Check weapon is valid
         Weapons.validateWeaponIdx(weaponIdx);
 
         // Register Player's weapon of choice
         playersAndWeapons.put(playerId, weaponIdx);
         LOG.debug(String.format("Added weapon '%d' for Player '%s' to Match '%s'", weaponIdx, playerId, id));
 
         if (areWeaponsReady()) {
             status = PLAYED;
             LOG.debug(String.format("Match '%s' has been played", id));
         }
     }
 
     public String getWinner() {
         if (status != PLAYED) {
             String errMsg = String.format("Match '%s' not ready to play yet", id);
             LOG.error(errMsg);
             throw new RuntimeException(errMsg);
         }
 
         // Play the match, using player's weapons
         String[] players = playersAndWeapons.keySet().toArray(new String[2]);
         int winningWeapon = Weapons.vs(playersAndWeapons.get(players[0]), playersAndWeapons.get(players[1]));
 
         // Report the result
         if (playersAndWeapons.get(players[0]) == winningWeapon) {
             LOG.debug(String.format("Match '%s' won by Player '%s'", id, players[0]));
             return players[0];
         } else if (playersAndWeapons.get(players[1]) == winningWeapon) {
             LOG.debug(String.format("Match '%s' won by Player '%s'", id, players[1]));
             return players[1];
         } else {
             LOG.debug(String.format("Match '%s' finished in a draw", id));
             return "draw";
         }
     }
 
     public void reset() {
         // Reset match only if both players are there
         if ((status & (WAITING_PLAYERS_WEAPONS | PLAYED)) > 0) {
             // Clear Player's weapons
             for (String playerId : playersAndWeapons.keySet()) {
                 playersAndWeapons.put(playerId, NO_WEAPON_YET);
             }
 
             // Set the status back to WAITING_PLAYERS_WEAPONS
             status = WAITING_PLAYERS_WEAPONS;
 
             LOG.debug(String.format("Match '%s' was reset: ready for new weapons", id));
         }
     }
 
     private static String sha1(String input) {
         String result = "";
 
         try {
             MessageDigest digester = MessageDigest.getInstance("SHA-1");
             digester.reset();
             digester.update(input.getBytes("UTF-8"));
 
             byte[] digest = digester.digest();
 
             Formatter f = new Formatter();
             for (int i = 0, ilen = digest.length; i < ilen; ++i) {
                 f.format("%02X", digest[i]);
             }
 
             result = f.toString();
             f.close();
         } catch (Exception e) {
             LOG.fatal("Couldn't Hash a String with SHA-1");
             System.exit(1);
         }
 
         return result;
     }
 }
