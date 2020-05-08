 package battlesys;
 
 import battlesys.exception.BattleSysException;
 import java.util.*;
 
 /**
  * A List of Players, with numerous more functions specific for a list of players
  * @author Peter
  */
 public class PlayerList extends ArrayList<Player>{
     
     /**
      * Battle counts
      */
     private int win, lose, draw;
 
     /**
      * Constructs an empty player list
      */
     public PlayerList(){
         super();
     }
 
     /**
      * Constructs an empty list with the specified initial capacity.
      * @param n
      */
     public PlayerList(int n){
         super(n);
     }
 
     /**
      * Constructs a list containing the elements of the specified collection, in the order they are returned by the collection's iterator.
      * @param c
      */
     public PlayerList(Collection<? extends Player> c){
         super(c);
     }
 
     /**
      * Combine a list of Players to get a new PlayerList
      * @param p List of players concerned
      * @return Combined list
      */
     public static final PlayerList fromLists(PlayerList ... p){
        PlayerList c = new PlayerList();
        for (int i = 0; i < p.length; ++i){
            for (Player pl : p[i]){
                c.add(pl);
            }
        }
        return c;
     }
 
     /**
      * get a subset of players. It returns a swallow copy of the players.
      * If the size of player is less than n, a swallow copy of original player is returned.
      * @param n How many player to pick
      * @return The picked players
      * @throws IllegalArgumentException If n &lt; 0
      */
     public final PlayerList playerSubset(int n){
         if (n < 0){
             throw new IllegalArgumentException();
         }
 
         //If the player array is shorter than the number of element wanted, clone the whole array and return it
         if (this.size() <= n){
             return (PlayerList) this.clone();
         }
 
         PlayerList r = new PlayerList(n);
         int[] chooseIndex = Utility.intSubset(this.size(), n);
 
         for (int i = 0; i < chooseIndex.length; ++i){
             r.add(this.get(chooseIndex[i]));
         }
 
         return r;
     }
 
     /**
      * Given a whole list of player, return a new list of player whose are living (hp > 0)
      * @return reference to new array
      */
     public final PlayerList livingPlayer(){
         PlayerList r = new PlayerList();
         for (Player p : this){
             if (p.getHp() > 0){
                 r.add(p);
             }
         }
         return r;
     }
 
     /**
      * Count the number of living players in an array of player
      * @return Number of living players
      */
     public final int livingPlayerCount(){
         int r = 0;
         for (Player p : this){
             if (p.getHp() > 0){
                 r++;
             }
         }
         return r;
     }
 
     /**
      * randomly pick a player from this list of players that is living. If no such player a player is randomly picked.
      * @return
      */
     public final Player randomPick(){
         return this.randomPick(false);
     }
 
     /**
      * Randomly pick a player from this list of players that is living
      * @param giveNull Whether null should be given if no such player. If false, a random player will be given
      * @return
      */
     public final Player randomPick(boolean giveNull){
         PlayerList living = this.livingPlayer();
         if (living.size() == 0){
             return giveNull?null:this.get(Utility.randBetween(0, this.size() - 1));
         }
         return living.get(Utility.randBetween(0, living.size() - 1));
     }
 
     /**
      * Invoke a particular function for all the team members in this list, including the player itself
      * @param methodName The name of the function to be invoked.
      * @param argType Types of the arguments of the function to be invoked.
      * @param args Arguments to be sent into the function invoked.
      * @param liveOnly If true only players with HP greater than zero are considered
      * @return Array of objects each function call by each player returns.<br>
      * It is guaranteed that the position of objects always correspond to the position of players as in the array
      */
     public final Object[] invokeForPlayers(String methodName, Class[] argType, Object[] args, boolean liveOnly) {
         Object[] res = new Object[this.size()];
         int resLength = 0;
         for (Player p : this) {
             if (!liveOnly || p.getHp() > 0) {
                 try {
                     res[resLength] = p.getClass().getMethod(methodName, argType).invoke(p, args);
                 } catch (Exception ex) {
                     ex.printStackTrace();
                     System.exit(1);
                 }
                 ++resLength;
             }
         }
         return Utility.shrinkArray(res, resLength);
     }
 
     /**
      * Find the player with the extreme value of given quality. If no such player exists a random player from pList is returned.
      * If more than one have the same extreme value, the smaller index one will returned.
      * @param quality The quantity requested, either HP, Atk, Def, Spd, Mor, specified in String form
      * @param liveOnly Whether only Players living are searched
      * @param minimal Whether the minimal or the maximal of the quality should be returned,
      * if true, minimal will be returned, otherwise maximal will be returned
      * @return Player
      */
     public final Player getPlayerOfMinQuality(String quality, boolean liveOnly, boolean minimal) {
         Player result = null;
         int min = Integer.MAX_VALUE;
         int max = Integer.MIN_VALUE;
         int v;
 
         for (Player p : this){
            if (liveOnly && p.getHp() < 0){
                 continue;
             }
             if (quality.equalsIgnoreCase("HP")) {
                 v = p.getHp();
             } else if (quality.equalsIgnoreCase("Atk")) {
                 v = p.getAtk();
             } else if (quality.equalsIgnoreCase("Def")) {
                 v = p.getDef();
             } else if (quality.equalsIgnoreCase("Spd")) {
                 v = p.getSpd();
             } else if (quality.equalsIgnoreCase("Mor")) {
                 v = p.getMor();
             } else {
                 throw new IllegalArgumentException("Wrong quality.");
             }
             if (v < min && minimal) {
                 min = v;
                 result = p;
             } else if (v > max && !minimal) {
                 max = v;
                 result = p;
             }
         }
 
         return result == null ? this.randomPick() : result;
     }
 
     /**
      * Return the player who have a certain status inflicted. If more than one is in such status
      * anyone of them is returned. If there is no such player then null is returned. Only players living are concerned
      * @param statusId The id of the status concerned
      * @return the player data who have a certain status inflicted. If no such player null is returned.
      */
     public final Player getStatusInflictedPlayer(int statusId) {
         return getStatusInflictedPlayer(statusId, false);
     }
 
     /**
      * Return the player who have a certain status inflicted. If more than one is in such status
      * anyone of them is selected. If there is no such player then null is returned. Only players living are concerned
      * @param statusId The id of the status concerned
      * @param not Whether the opposite, that is, players not inflicted, should be returned.
      * @return player data who have a certain status inflicted. If no such player null is returned.
      */
     public final Player getStatusInflictedPlayer(int statusId, boolean not) {
         PlayerList inflicted = new PlayerList();
 
         for (Player p : this){
             if (((not && !p.statusInflicted(statusId)) || (!not && p.statusInflicted(statusId))) && p.getHp() > 0) {
                 inflicted.add(p);
             }
         }
 
         return inflicted.size() == 0 ? null : inflicted.randomPick(true);
     }
 
     /**
      * Return the player who have a certain status inflicted. If more than one is in such status
      * the one with minimal quality is returned. If there is no such player then null is returned. Only players living are concerned
      * @param statusId The id of the status concerned
      * @param not Whether the opposite, that is, players not inflicted, should be returned.
      * @param quality The quality concerned. either HP, Atk, Def, Spd, Mor, specified in String form. Caps sensitive
      * @param minimal Whether the minimal or the maximal of the quality should be returned
      * @return player data who have a certain status inflicted. If no such player null is returned
      */
     public final Player getStatusInflictedPlayer(int statusId, boolean not, String quality, boolean minimal) {
         PlayerList inflicted = new PlayerList();
 
         for (Player p : this){
             if (((not && !p.statusInflicted(statusId)) || (!not && p.statusInflicted(statusId))) && p.getHp() > 0) {
                 inflicted.add(p);
             }
         }
 
         return inflicted.size() == 0 ? null : inflicted.getPlayerOfMinQuality(quality, true, minimal);
     }
 
     /**
      * Return list of names from a list of player
      * @return list of names from a list of player, full-sized stroke delimited
      */
     public final String nameList() {
         return Utility.listString(Utility.objArr2StrArr(this.invokeForPlayers("getName", new Class[0], new Object[0], false)), "、");
     }
 
     /**
      * Search a player by a name from a list, and return the object reference to this player object
      * @param name Name of the player to be found
      * @return object reference to the player given by the name, null is returned if search failed
      */
     public final Player findPlayer(String name){
         for (Player p : this){
             if (p.getName().equals(name)){
                 return p;
             }
         }
         return null;
     }
 
     /**
      * Return whether all players in this player's team have 0 or below HP
      * @return true if all players have neg HP ("dead"), false otherwise
      */
     public final boolean allDead() {
         boolean res = true;
         for (Player thisPlayer : this) {
             if (thisPlayer.getHp() > 0) {
                 res = false;
             }
         }
         return res;
     }
 
     /**
      * Do preBattle for everyone in this playerList
      */
     void preBattle() {
         for (Player thisPlayer : this) {
             thisPlayer.preBattle();
         }
     }
 
     /**
      * Do preBattleSpeech for everyone in this playerList
      */
     void preBattleSpeech(PlayerList thisPlayers, PlayerList opposingPlayers) {
         for (Player thisPlayer : this) {
             thisPlayer.preBattleSpeech(thisPlayers, opposingPlayers);
         }
     }
 
     /**
      * Do postBattleSpeech for everyone in this playerList
      */
     void postBattleSpeech(PlayerList thisPlayers, PlayerList opposingPlayers, boolean win) {
         for (Player thisPlayer : this) {
             thisPlayer.postBattleSpeech(thisPlayers, opposingPlayers, win);
         }
     }
 
     /**
      * Do preRound stage for everyone in this playerList
      * @param thisSide Players of the side same as the player being processed
      * @param opposingSide Player of the side opposing to the player being processed
      * @throws BattleSysException
      */
     String preRound(PlayerList thisSide, PlayerList opposingSide) throws BattleSysException {
         StringBuilder s = new StringBuilder();
         for (Player thisPlayer : this) {
             if (thisPlayer.hp > 0) {
                 s.append(thisPlayer.preRound(thisSide, opposingSide));
             }
         }
         return s.toString();
     }
 
     /**
      * Do postRound stage for everyone in this playerList
      * @param thisSide Players of the side same as the player being processed
      * @param opposingSide Player of the side opposing to the player being processed
      * @throws BattleSysException
      */
     void postRound() throws BattleSysException {
         for (Player thisPlayer : this) {
             if (thisPlayer.hp > 0) {
                 thisPlayer.postRound();
             }
         }
     }
 
     /**
      * If anyone in this list of player have a certain status inflicted
      * @param statusId statusId ID of status to test, i.e. statusId.POISON.getId(), statusId.PIN.getId(), etc...
      * @return
      */
     public final boolean statusInflicted(int statusId) {
         if (statusId < 0 || statusId > Player.STATUS_COUNT) {
             return false;
         }
         for (Player thisPlayer : this) {
             if (thisPlayer.statusInflicted(statusId)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * If anyone in the list of player have any status inflicted
      * @return
      */
     public final boolean statusInflicted() {
         for (Player thisPlayer : this) {
             if (thisPlayer.statusInflicted()) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Get the score of this player, i.e. 3 * Win + Draw
      * @return Score of player
      */
     public final long getScore() {
         return getWin() * 3 + getDraw();
     }
 
     /**
      * Increment Win Count
      */
     final void incWin() {
         win += 1;
     }
 
     /**
      * Increment Lose Count
      */
     final void incLose() {
         lose += 1;
     }
 
     /**
      * Increment Draw Count
      */
     final void incDraw() {
         draw += 1;
     }
 
     /**
      * Reset all battle counts to 0.
      */
     final void resetCompeteResult() {
         win = lose = draw = 0;
     }
 
     /**
      * Get how many wins the player get
      * @return the win
      */
     public final long getWin() {
         return win;
     }
 
     /**
      * Get how many loses the player get
      * @return the lose
      */
     public final long getLose() {
         return lose;
     }
 
     /**
      * Get how many draws the player get
      * @return the draw
      */
     public final long getDraw() {
         return draw;
     }
 
 }
