 package battlesys;
 
 import battlesys.exception.*;
 import java.util.*;
 import java.io.*;
 
 /**
  * Class for storing general data and moves for a player
  * TODO implements Serializable...
  * @author Peter
  */
 public abstract class Player {
 
     /**
      * Total point allowed to allocate
      */
     public static final int TOTAL_POINTS = loadTournamentData();
     private static final int DEFAULT_PLAYER_POINTS = 30;
     /**
      * Whether the player has been placed into any team
      */
     boolean placed = false;
     /**
      * representing infinite move time
      */
     public static final int MOVE_TIME_INF = 1000;
     //Player data concerning battle system
     //Initial points at the beginning of every battle
     int initHp, initAtk, initDef, initSpd, initMor, maxHp;
     //With ability-changing moves
     int btHp, btAtk, btDef, btSpd, btMor;
     //Actual values, also affected by Random events
     int atk, def, spd, mor;
     int hp;
     //Status affections
     boolean[] status;
     //Is the status affection due to Random Events?
     boolean[] reStatus;
     //attacks bought. This is a list as a player can own the same move twice, one being temp move of MoveRandom
     List<Move> boughtAtk;
 
     /**
      * Id for uniquely identifying all the statuses, used to tell whether a player has a certain status inflicted, etc...
      * Also used to accessing their name etc...
      */
     public static enum statusId {
 
         /**
          * Indicating no status, used by Random Event and some parts in MoveStatus
          */
         NO_EFFECT(-1),
         /**
          * Paralysis: Player has a probability that cannot move
          */
         PARALYSIS(0),
         /**
          * Pin: Player must be hit when attacked
          */
         PIN(1),
         /**
          * Poison: Lose some hp every turn
          */
         POISON(2),
         /**
          * Dizzy: Player has a probability to attack friendly players
          */
         DIZZY(3),
         /**
          * Disable: Player only able to use basic attacks
          */
         DISABLE(4),
         /**
          * Freeze: Player cannot move, and is always be hit
          */
         FREEZE(5),
         /**
          * Shield: When HP drops to 0 the HP of the player becomes 1, and immediately gone after this
          */
         SHIELD(6),
         /**
          * Weakening: Player's attack and defense drops every turn
          */
         WEAKENING(7),
         /**
          * Charge: For charge attack - the player is charging
          */
         CHARGE(8),
         /**
          * Reflect: Reflect an attack done on the player to the user, and immediately gone after this
          */
         REFLECT(9),
         /**
          * Shield 2: When HP drops to 0 the HP of the player becomes 1, and gone after the turn
          */
         SHIELD_2(10),
         /**
          * Weakening: Player's ability drops every turn
          */
         WEAKENING_2(11),
         /**
          * Absorbing: Player is subject to losing hp to others every turn
          */
         ABSORBING(12),
         /**
          * Destroy Link: When the player's hp drops to zero, it destroys an opponent indicated by destroyLinkTarget, instantly.
          */
         DESTROY_LINK(13),
         /**
          * Protect: Prevent damage of an attack done on the player, and immediately gone after this.
          */
         PROTECT(14),
         /**
          * Freeze 3: Player cannot move, and is always be hit. Effect same as Freeze except it is more difficult to recover from Freeze 3
          */
         FREEZE_3(15);
         private int id;
 
         private statusId(int x) {
             id = x;
         }
 
         /**
          *
          * @return
          */
         public int getId() {
             return id;
         }
     }
     //Probability of the player unable to move when begin paralysis
     static final int PARALYSIS_EFFECT_PROB = 80;
     //Probability of the player attacking himself when being dizzy
     static final int DIZZY_EFFECT_PROB = 80;
     /**
      * For MoveStatus and RandomEvent, Applying a random status in BAD_STATUS_LIST
      */
     public static final int RANDOM_BAD = 1000;
     /**
      * list of bad statuses
      */
     public static final int[] BAD_STATUS_LIST = {0, 1, 2, 3, 4, 5, 7, 11, 12};
     /**
      * Name of each status
      */
     public static final String[] STATUS_STRING = {
         "麻痺", "定身", "中毒", "暈眩", "禁咒", "凍結", "鐵壁", "衰弱", "儲氣", "反射",
         "鋼壁", "衰弱彈", "吸力咒", "同亡", "保護盾", "寒凍結"};
     /**
      * Number of statuses
      */
     public static final int STATUS_COUNT = STATUS_STRING.length;
     //Damage rate of poisoning
     private static final double POISON_RATE = 0.1;
     //probability of recovery from the status, without addition by morale
     private static final double[] RECOVER_RATE = {5, 5, 5, 5, 5, 3, 0, 5, 0, 0, 0, 3, 5, 0, 33, 10};
     //probability addition of morale to recover from status
     private static final int[] RECOVER_MOR_EFFECT = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0};
     private static final String[] STATUS_COLOR = {
         "#777700", "#0000FF", "#007700", "#FF7700", "#770000", "#00FFFF", "#AAAAAA", "#333333", "#FF0000", "#777777",
         "#AAAAAA", "#333333", "#770000", "#AAAAAA", "#000000", "#00FFFF"};
     //Flags for player
     boolean moved;
     private int pointRemaining;
     //Player names and speeches
     private String name;
     //Team Members
     private String teamName;
     /**
      * String indicating the player does not belong to any team.
      */
     public static final String NO_TEAM = "NO_TEAM";
     //Player random effect time remaining
     int reAffect[];
     //statistical data for player
     //For one battle. Attempts: number of attempts to make a move.
     private long roundTotalHits, roundTotalCriticals, roundTotalDamage, roundMaxDamage, roundAttempts;
     //For whole run
     private long totalHits, totalCriticals, totalDamage, maxDamage, attempts;
     private long beTotalHits, beCriticals, beTotalDamage, beMaxDamage, beAttempts;
     //npc flag
     boolean npc;
     //Stored player lists - for charged attacks
     Player playerToAtk;
     //Misc.
     //Whether the player has spoken in this round
     private boolean spoken;
     //for shield2
     boolean shield2Finishing = false;
     //absorbing to whom
     PlayerList absorbTo = new PlayerList();
     //destroy_link: The target to destroy when this player hp drops to 0
     Player destroyLinkTarget = null;
 
     /**
      * Load the allowed points to allocate from the file "tournamentPoints.txt".
      * If loading fails, a file will be created, and the default value (DEFAULT_PLAYER_POINTS) will be returned
      * @return
      */
     public static final int loadTournamentData() {
         try {
             BufferedReader br = new BufferedReader(new FileReader("tournamentPoints.txt"));
             String line;
             int result = 0;
             
             line = br.readLine();
             line = br.readLine();
             result = Integer.parseInt(line);
             
             line = br.readLine();
             line = br.readLine();
             Main.gametime = Integer.parseInt(line);
             
             line = br.readLine();
             line = br.readLine();
             Main.hf = line;
             
             br.close();
             return result;
         } catch (IOException ex) {
             //unable to read file
             try {
                 BufferedWriter bw = new BufferedWriter(new FileWriter("tournamentPoints.txt"));
                 bw.write(Integer.toString(DEFAULT_PLAYER_POINTS));
                 bw.close();
             } catch (IOException ex2) {
                 //unable to create file - ignore
             }
             return DEFAULT_PLAYER_POINTS;
         }
     }
 
     /**
      * Constructor initializing the player data
      * It should be noted that boughtAtk 0 - 2 must store the 3 basic attacks, BasicAtk, AimedAtk and StrongAtk, respectively.
      * All players must first call this constructor before giving their own point allocation
      */
     public Player() {
         maxHp = hp = initHp = 2000;
         atk = initAtk = 0;
         def = initDef = 0;
 
         name = "玩家";
 
         reAffect = new int[Move.AFFECT_COUNT];
         for (int i = 0; i < reAffect.length; ++i) {
             reAffect[i] = 0;
         }
 
         //get all basic attacks
         boughtAtk = new ArrayList<Move>();
         Collection<Move> moves = Move.getAllMoves();
         for (Move m : moves) {
             if (m.basic) {
                 boughtAtk.add(m);
             }
         }
 
         status = new boolean[STATUS_COUNT];
         reStatus = new boolean[STATUS_COUNT];
         for (int i = 0; i < STATUS_COUNT; ++i) {
             status[i] = false;
             reStatus[i] = false;
         }
 
         moved = false;
         npc = false;
         pointRemaining = TOTAL_POINTS;
 
         spoken = false;
         
         teamName = NO_TEAM;
 
         totalHits = roundTotalHits = totalCriticals = roundTotalCriticals = roundAttempts =
                 totalDamage = roundTotalDamage = roundMaxDamage = maxDamage = attempts =
                 beTotalHits = beCriticals = beTotalDamage = beMaxDamage = beAttempts = 0;
 
     }
 
     /**
      * Give n HP points to the player, adding to its HP
      * @param n Number of points
      */
     protected final void giveHpPoint(int n) throws NotEnoughPointsException {
         if (pointRemaining >= n || npc) {
             initHp += 800 * n;
             pointRemaining -= n;
         } else {
             throw new NotEnoughPointsException(this, "get " + n + " points on HP");
         }
     }
 
     /**
      * Convert the HP value to point value that one could allocate to give the specifed hp
      * @param hp The HP value 
      * @return The point value.
      */
     public static final int hpToPoint(int hp) {
         return (hp - 2000) / 800;
     }
 
     /**
      * Give n Attack points to the player, adding to its Attack
      * @param n Number of points
      */
     protected final void giveAtkPoint(int n) throws NotEnoughPointsException {
         if (pointRemaining >= n || npc) {
             initAtk += n;
             pointRemaining -= n;
         } else {
             throw new NotEnoughPointsException(this, "get " + n + " points on attack");
         }
     }
 
     /**
      * Give n Defense points to the player, adding to its defense
      * @param n Number of points
      */
     protected final void giveDefPoint(int n) throws NotEnoughPointsException {
         if (pointRemaining >= n || npc) {
             initDef += n;
             pointRemaining -= n;
         } else {
             throw new NotEnoughPointsException(this, "get " + n + " points on defense");
         }
     }
 
     /**
      * Give n Speed points to the player, adding to its speed
      * @param n Number of points
      */
     protected final void giveSpdPoint(int n) throws NotEnoughPointsException {
         if (pointRemaining >= n || npc) {
             initSpd += n;
             pointRemaining -= n;
         } else {
             throw new NotEnoughPointsException(this, "get " + n + " points on speed");
         }
     }
 
     /**
      * Give n Morale points to the player, adding to its Morale
      * @param n Number of points
      */
     protected final void giveMorPoint(int n) throws NotEnoughPointsException {
         if (pointRemaining >= n || npc) {
             initMor += n;
             pointRemaining -= n;
         } else {
             throw new NotEnoughPointsException(this, "get " + n + " points on morale");
         }
     }
 
     /**
      * Purchase a certain attack
      * @param name The name (short class name) of the move
      * @throws InvalidMoveNameException Thrown when the name for the move is invalid.
      */
     protected final void buyAtk(String name) throws InvalidMoveNameException, NotEnoughPointsException, UnbuyableMoveException {
         Move move = Move.getMove(name);
         buyAtk(move);
     }
 
     /**
      * Purchase a certain attack
      * @param name The name (short class name) of the move
      * @throws InvalidMoveNameException Thrown when the name for the move is invalid.
      */
     protected final void buyAtk(Move move) throws InvalidMoveNameException, NotEnoughPointsException, UnbuyableMoveException {
         if (!move.isBuyable()) {
             throw new UnbuyableMoveException(move.getClass().getSimpleName());
         }
         if ((pointRemaining >= move.cost || npc) && move.buyable) {
             move.owner = this;
             boughtAtk.add(move);
             pointRemaining -= move.cost;
             for (String s : move.alsoGetMove) {
                 Move m = Move.getMove(s);
                 m.owner = this;
                 boughtAtk.add(m);
             }
         } else {
             throw new NotEnoughPointsException(this, "buy a move named " + move.getName() + "( simple class name " + move.getClass().getSimpleName() + ")");
         }
     }
 
     /**
      * Whether an attack is bought
      * @param move The move object.
      * @return
      */
     public final boolean isAtkBought(Move move) {
         for (Move m : boughtAtk) {
             if (move.getClass().getSimpleName().equals(m.getClass().getSimpleName())) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Whether an attack is bought
      * @param name The simple class name of the move wanted
      * @return
      */
     public final boolean isAtkBought(String name) {
         for (Move m : boughtAtk) {
             if (name.equals(m.getClass().getSimpleName())) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Get a list of bought atks
      * @return
      */
     public final Collection<Move> getBoughtAtk() {
         return boughtAtk;
     }
 
     /**
      * Tell whether a move is usable for this Player. Note the basic moves are always usable
      * @param name the Short Class Name for the move concerned
      * @return
      * @throws BattleSysException Thrown if the code is invalid or does not own the move
      */
     protected final boolean usable(String name) throws BattleSysException {
         return (getMoveTime(name) > 0 && !statusInflicted(statusId.DISABLE.getId())) || (Move.getMove(name).basic);
     }
 
     /**
      * Do a checking about disable status. If the player is "disabled" he can only use Basic Attack.
      * @param move The move the player is going to use
      * @return true if disable affect the player (only basic attack can be used), false otherwise)
      */
     boolean checkDisable(Move m) {
         if (status[statusId.DISABLE.getId()] && !m.basic) {
             return true;
         }
         return false;
     }
 
     /**
      * Do a checking about paralysis status. Player under paralysis may not be able to move.
      * @return true if paralysis affect the player (cannot move), false otherwise
      */
     boolean checkParalysis() {
         if (!status[statusId.PARALYSIS.getId()] || !Utility.probTest(PARALYSIS_EFFECT_PROB)) {
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Do a checking about freeze status. Player under freeze may not be able to move.
      * @return true if paralysis affect the player (cannot move), false otherwise
      * */
     boolean checkFreeze() {
         if (!status[statusId.FREEZE.getId()] || !status[statusId.FREEZE_3.getId()]) {
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Do a checking about dizzy status. Player under dizzy will attack itself
      * @return true if dizzy affect the player, false otherwise
      */
     boolean checkDizzy() {
         if (!status[statusId.DIZZY.getId()] || !Utility.probTest(DIZZY_EFFECT_PROB)) {
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Do a checking about reflect status. If the opponent is in reflect, this player will attack itself
      * Also remove the status
      * @return true if reflect affect the player, false otherwise
      */
     boolean checkReflect(Player defender) {
         if (!defender.status[statusId.REFLECT.getId()]) {
             return false;
         } else {
             defender.status[statusId.REFLECT.getId()] = false;
             return true;
         }
     }
 
     /**
      * Do a checking about protect status. If the opponent is in reflect, this player cannot attack the opponent
      * Also remove the status
      * @return true if reflect affect the player, false otherwise
      */
     boolean checkProtect(Player defender) {
         if (!defender.status[statusId.PROTECT.getId()]) {
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Inflict poison damage to the player if it is poisoned. Poison damage do not cause the one who is poisoned to faint.
      */
     private String inflictPoison() {
         String s = "";
         if (status[statusId.POISON.getId()]) {
             int hpLost = (int) (Math.max(getInitHp(), getHp()) * POISON_RATE * Utility.randBetween(0.9, 1.1));
             s = getName() + "受毒害所傷，失去體力" + hpLost + '\n';
             hp -= hpLost;
             //Does not allow faint due to poison
             if (hp <= 0) {
                 hp = 1;
             }
         }
         return s;
     }
 
     /**
      * Inflict weakening status damage to the player if it is poisoned
      */
     private String inflictWeakening() {
         String s = "";
         if (status[statusId.WEAKENING.getId()]) {
             int abyLost = Utility.randBetween(2, 4);
             s = getName() + "受衰弱所傷，失去攻擊力及防禦力" + abyLost + '\n';
             atk -= abyLost;
             def -= abyLost;
         }
         return s;
     }
 
     /**
      * Inflict weakening 2 status damage to the player if it is poisoned
      */
     private String inflictWeakening2() {
         String s = "";
         if (status[statusId.WEAKENING_2.getId()]) {
             int abyLost = Utility.randBetween(2, 4);
             int hpLost = (int) (Math.max(getInitHp(), getHp()) * POISON_RATE * Utility.randBetween(0.9, 1.1));
             s = getName() + "受衰弱所傷，失去能力" + abyLost + "，體力" + hpLost + '\n';
             atk -= abyLost;
             def -= abyLost;
             spd -= abyLost;
             mor -= abyLost;
             hp -= hpLost;
         }
         return s;
     }
 
     /**
      * Inflict absorbing status damage to the player if it is being absorbed
      */
     private String inflictAbsorbing() {
         String s = "";
         if (status[statusId.ABSORBING.getId()]) {
             for (Player p : absorbTo) {
                 int hpLost = Utility.randBetween(500, 1000);
                 if (p.getHp() <= 0) {
                     continue;
                 }
                 s = getName() + "受吸力咒所影響，其體力" + hpLost + "被" + p.getName() + "吸去了！\n";
                 p.hp += hpLost;
                 this.hp -= hpLost;
             }
         }
         return s;
     }
 
     /**
      * Recover the player's status for every round automatically, if it happens
      */
     private String autoRecoverStatus() {
         String s = "";
         for (int i = 0; i < STATUS_COUNT; ++i) {
             //Skip statuses that are suppose to never be recovered (RECOVER_RATE[i] == 0)
             if (RECOVER_RATE[i] == 0) {
                 continue;
             }
             if (status[i] && Utility.probTest(RECOVER_RATE[i] + getMor() * RECOVER_MOR_EFFECT[i])) {
                 s += getName() + "從" + STATUS_STRING[i] + "狀態中恢復了。\n";
                 status[i] = false;
             }
         }
         return s;
     }
 
     /**
      * Do attack when the player is forced to use basic attack
      * @param opponent
      * @return MoveResult: damage dealt, if critical, if hit
      */
     final CompleteMoveResult forcedBasicAtk(Player opponent, PlayerList attackers, PlayerList defenders) throws BattleSysException {
         if (this.getHp() <= 0) {
             return new CompleteMoveResult(Collections.singletonList(new SingleMoveResult(this, opponent)));
         }
         return useMove("BasicAtk", this, attackers, opponent, defenders);
     }
 
     /**
      * Upon no hp, some moves/statuses will be triggered.
      * @param p Player who has no hp concerned
      */
     final String noHpTriggers(PlayerList thisSide, PlayerList opposingSide) {
         //status related
         String s = "";
         if (hp <= 0) {
             if (statusInflicted(statusId.SHIELD.getId())) {
                 hp = 1;
                 status[statusId.SHIELD.getId()] = false;
                 s += getName() + "受" + STATUS_STRING[statusId.SHIELD.getId()] + "影響，沒有因體力歸零而變得衰弱！";
             } else if (statusInflicted(statusId.SHIELD_2.getId())) {
                 hp = 1;
                 shield2Finishing = true;
                 s += getName() + "受" + STATUS_STRING[statusId.SHIELD_2.getId()] + "影響，沒有因體力歸零而變得衰弱！";
             } else {
                 //The player no longer alive
                 s += getName() + "無法再戰鬥了。";
                 deadSpeech();
                 if (statusInflicted(statusId.DESTROY_LINK.getId())) {
                     s += getName() + "發動同歸於盡，" + destroyLinkTarget.getName() + "的體力一下子變成了零！";
                     destroyLinkTarget.hp = 0;
                 }
             }
 
             //moves related
             for (Move m : boughtAtk) {
                 s += m.noHpTriggers(thisSide, opposingSide);
             }
         }
 
         /*if (hp <= 0) {
         if (isAtkBought("Revive2")) {
         Move m = getMoveByName("Revive2");
         if (m.getMoveTime() > 0) {
         moved = false;
         useMove("Revive2", this, thisSide, opposingSide.randomPick(), opposingSide);
         moved = true;
         }
         }
         }*/
 
         return s;
     }
 
     /**
      * Recover all bad status for this player
      */
     final String recoverStatus() {
         for (int i = 0; i < STATUS_COUNT; ++i) {
             if (!Utility.inArray(i, BAD_STATUS_LIST)) {
                 continue;
             }
             status[i] = false;
             reStatus[i] = false;
         }
         return getName() + "從所有不良狀態中回復了！";
     }
 
     /**
      * Keep statistics for results of a certain move
      * @param result The moveresult records
      */
     static final void keepStat(List<SingleMoveResult> result) {
         for (SingleMoveResult r : result) {
             Player attacker = r.getAttacker();
             Player defender = r.getDefender();
 
             attacker.roundAttempts++;
             attacker.attempts++;
             attacker.roundTotalDamage += r.getDamage();
             attacker.totalDamage += r.getDamage();
             if (r.getDamage() > attacker.getRoundMaxDamage()) {
                 attacker.roundMaxDamage = r.getDamage();
             }
             if (r.getDamage() > attacker.getMaxDamage()) {
                 attacker.maxDamage = r.getDamage();
             }
 
             attacker.roundTotalHits += r.isHit() ? 1 : 0;
             attacker.totalHits += r.isHit() ? 1 : 0;
             attacker.roundTotalCriticals += r.isCritical() ? 1 : 0;
             attacker.totalCriticals += r.isCritical() ? 1 : 0;
 
             if (defender != null) {
                 defender.beTotalDamage += r.getDamage();
                 if (r.getDamage() > defender.getBeMaxDamage()) {
                     defender.beMaxDamage = r.getDamage();
                 }
                 defender.beTotalHits += r.isHit() ? 1 : 0;
                 defender.beCriticals += r.isCritical() ? 1 : 0;
                 defender.beAttempts++;
             }
         }
     }
 
     /**
      * Return the move object named by the given name owned by this player. If no such move exists (e.g. the player did not buy it)
      * an exception is thrown.
      * @param name
      * @return
      */
     private final Move getMoveByName(String name) throws BattleSysException {
         for (Move m : boughtAtk) {
             if (m.getClass().getSimpleName().equalsIgnoreCase(name) && m.owner == this) {
                 return m;
             }
         }
         throw new BattleSysException("Move " + name + " does not exist or is not owned by player " + this.getName() + "(" + this.getClassName() + ")");
     }
 
     /**
      * Use a move with a given name. The target will be chosen randomly.
      * @param name The name (short class name) of the attack
      * @param thisSide The players in own side
      * @param opposingSide The players in other side
      * @return MoveResult for the move actually used
      * @throws BattleSysException Thrown when the code is invalid or the move is not owned by player
      */
     protected final CompleteMoveResult useMove(String name, PlayerList thisSide, PlayerList opposingSide) throws BattleSysException {
         return useMove(name, thisSide.randomPick(), thisSide, opposingSide.randomPick(), opposingSide);
     }
 
     /**
      * Use a move with a given name to a given target.
      * @param name The name (short class name) of the attack
      * @param target The target of player
      * @param thisSide The players in own side
      * @param opposingSide The players in other side
      * @return MoveResult for the move actually used
      * @throws BattleSysException Thrown when the code is invalid or the move is not owned by player
      */
     protected final CompleteMoveResult useMove(String name, Player target, PlayerList thisSide, PlayerList opposingSide) throws BattleSysException {
         return useMove(name, target, thisSide, target, opposingSide);
     }
 
     /**
      * Use a move with a given name
      * @param name The name (short class name) of the attack
      * @param thisSideTarget The target of player in own sides
      * @param thisSide The players in own side
      * @param opposingSideTarget The target of player in other side
      * @param opposingSide The players in other side
      * @return MoveResult for the move actually used
      * @throws BattleSysException Thrown when the code is invalid or the move is not owned by player
      */
     protected final CompleteMoveResult useMove(String name, Player thisSideTarget, PlayerList thisSide, Player opposingSideTarget, PlayerList opposingSide) throws BattleSysException {
         Move m = Move.getMove(name);
         m.owner = this;
         return m.useMove(m.onOpposing ? opposingSideTarget : thisSideTarget, thisSide, opposingSide);
     }
 
     /**
      * Use the given move
      * @param move The move object given, to be used against.
      * @param thisSideTarget The target of player in own sides
      * @param thisSide The players in own side
      * @param opposingSideTarget The target of player in other side
      * @param opposingSide The players in other side
      * @return MoveResult for the move actually used
      * @throws BattleSysException Thrown when the move is not owned by player
      */
     final CompleteMoveResult useMove(Move move, Player thisSideTarget, PlayerList thisSide, Player opposingSideTarget, PlayerList opposingSide) throws BattleSysException {
         return move.useMove(move.onOpposing ? opposingSideTarget : thisSideTarget, thisSide, opposingSide);
     }
 
     /**
      * Before starting a battle, reset data and do some moves before starting a battle
      */
     void preBattle() {
         hp = btHp = initHp;
         atk = btAtk = initAtk;
         def = btDef = initDef;
         spd = btSpd = initSpd;
         mor = btMor = initMor;
         for (int i = 0; i < reAffect.length; ++i) {
             reAffect[i] = 0;
         }
         for (Move m : boughtAtk) {
             m.setMoveTime(m.initialMoveTime);
         }
         for (int i = 0; i < STATUS_COUNT; ++i) {
             status[i] = false;
             reStatus[i] = false;
         }
         roundTotalHits = roundTotalDamage = roundTotalCriticals = roundMaxDamage = roundAttempts = 0;
         moved = true;
     }
 
     /**
      * Set the team name. Players with same team name are considered the same team.
      * @param name The team name to set.
      */
     protected final void setTeamName(String name) {
         if (name.equals(NO_TEAM)) {
             System.err.println("Team name cannot be " + NO_TEAM);
         } else {
             teamName = name;
         }
     }
 
     /**
      * Get the team number of this player
      * @return The team number of this player
      */
     public final String getTeamName() {
         return teamName;
     }
 
     /**
      * Get the name list of the team members this player have, including the player itself
      * @param p Array consists of information of all the players
      * @return String containing all information of all players.
      */
     public final String teamMemberNames(PlayerList p) {
         String[] names = null;
         names = Utility.objArr2StrArr(p.invokeForPlayers("getName", new Class[0], new Object[0], false));
         return Utility.listString(names, "、");
     }
 
     /**
      * Whether this player is of same team of the other player
      * @param t The player to be checked
      * @return true if this player is same team of t, false otherwise
      */
     public final boolean inSameTeam(Player t) {
         return t.teamName.equals(this.teamName);
     }
 
     /**
      * Get Players of the same team from the array of all players, including the caller itself
      * @param allPlayers Array of all players
      * @return Array of players in the same team
      */
     public final PlayerList getPlayersOfSameTeam(PlayerList allPlayers) {
         //If the player have no team, only return the caller itself
         if (teamName.equals(NO_TEAM)) {
             PlayerList r = new PlayerList();
             r.add(this);
             return r;
         }
 
         PlayerList r = new PlayerList();
         for (Player thisPlayer : allPlayers) {
             if (thisPlayer.getTeamName().equals(teamName)) {
                 r.add(thisPlayer);
             }
         }
 
         return r;
 
     }
 
     /**
      * Do speeches before a battle.
      * @param thisPlayers array of data of players in same team
      * @param opposingPlayers array of data of opposingPlayers
      */
     protected void preBattleSpeech(PlayerList thisPlayers, PlayerList opposingPlayers) {
         return;
     }
 
     /**
      * Do speeches after a battle.
      * @param thisPlayers array of data of players in same team
      * @param opposingPlayers array of data of opposingPlayers
      * @param win Boolean value whether this player has won
      */
     protected void postBattleSpeech(PlayerList thisPlayers, PlayerList opposingPlayers, boolean win) {
         return;
     }
 
     /**
      * Do speeches when this player hp less than zero.
      * To be overriden.
      */
     protected void deadSpeech() {
         return;
     }
 
     /**
      * Do a player move.
      * @param thisPlayers Player data of all players in the same team
      * @param opposingPlayers Player data of all the opposing players
      * @param round The number of round it has been
      * @param battleNo How many battles have been played for this player from the beginning of this replay
      * @param reResult Random Event Result of the last Random event
      * @param mResult The result of the opponent's last move
      * @return List of Move Results returned by the useMove method
      * @throws BattleSysException
      */
     protected CompleteMoveResult move(PlayerList thisPlayers, PlayerList opposingPlayers, int round, int battleNo, RandomEventResult reResult, List<SingleMoveResult> mResult) throws BattleSysException {
         return useMove("BasicAtk", this, thisPlayers, opposingPlayers.randomPick(), opposingPlayers);
     }
 
     /**
      * Do action or bookmarking before a player move, in each round.
      * It may also make actual moves that are forced to use.
      */
     final String preRound(PlayerList thisSide, PlayerList opposingSide) {
         String s = "";
         moved = false;
         spoken = false;
         s += inflictPoison();
         s += inflictWeakening();
         s += inflictWeakening2();
         s += inflictAbsorbing();
         s += autoRecoverStatus();
 
         //Now, for forced moves
         if (statusInflicted(statusId.CHARGE.getId())) {
             try {
                 s += getMoveByName("ChargeAtk_Real").useMove(playerToAtk, thisSide, opposingSide).getResultString();
             } catch (BattleSysException ex) {
                 assert false;
             }
             status[statusId.CHARGE.getId()] = false;
         }
         return s;
     }
 
     /**
      * Do action or bookmarking after a player move, in each round.
      * It may also make actual moves that are forced to use.
      */
     final void postRound() throws BattleSysException {
         //remove some status before every turn
         if (this.statusInflicted(statusId.SHIELD_2.getId())) {
             if (shield2Finishing) {
                 shield2Finishing = false;
                 this.status[statusId.SHIELD_2.getId()] = false;
             }
         }
     }
 
     /**
      * Have a player say something (Return formatted message)
      * @param msg The message to be spoken
      */
     protected final String speak(String msg) {
         spoken = true;
         return "「" + msg + "」" + getName() + "道\n";
     }
 
     /**
      * Get HP of the player
      * @return HP of the player
      */
     public final int getHp() {
         return hp;
     }
 
     /**
      * Get max HP of the player
      * @return max HP of the player
      */
     public final int getMaxHp() {
         return maxHp;
     }
 
     /**
      * Get Attack of the player
      * @return Attack of the player
      */
     public final int getAtk() {
         return atk;
     }
 
     /**
      * Get Defense of the player
      * @return Defense of the player
      */
     public final int getDef() {
         return def;
     }
 
     /**
      * Get Speed of the player
      * @return Speed of the player
      */
     public final int getSpd() {
         return spd;
     }
 
     /**
      * Get Morale of the player
      * @return Morale of the player
      */
     public final int getMor() {
         return mor;
     }
 
     /**
      * Get Initial HP of the player
      * @return Initial HP of the player
      */
     public final int getInitHp() {
         return initHp;
     }
 
     /**
      * Get Initial Attack of the player
      * @return Initial Attack of the player
      */
     public final int getInitAtk() {
         return initAtk;
     }
 
     /**
      * Get Initial Defense of the player
      * @return Initial Defense of the player
      */
     public final int getInitDef() {
         return initDef;
     }
 
     /**
      * Get Initial Speed of the player
      * @return Initial Speed of the player
      */
     public final int getInitSpd() {
         return initSpd;
     }
 
     /**
      * Get Initial Morale of the player
      * @return Initial Morale of the player
      */
     public final int getInitMor() {
         return initMor;
     }
 
     /**
      * Whether the player has a certain status inflicted
      * @param statusId ID of status to test, i.e. statusId.POISON.getId(), statusId.PIN.getId(), etc...
      * @return true if it has the specific status inflicted, false otherwise
      */
     public final boolean statusInflicted(int statusId) {
         if (statusId < 0 || statusId > STATUS_COUNT) {
             return false;
         }
         return this.status[statusId];
     }
 
     /**
      * Whether the player has any bad status inflicted
      * @return true if it has any status inflicted, false otherwise
      */
     public final boolean statusInflicted() {
         for (int i = 0; i < STATUS_COUNT; ++i) {
             if (!Utility.inArray(i, BAD_STATUS_LIST)) {
                 continue;
             }
             if (statusInflicted(i)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Set name for the player
      * @param setName The name to be set
      */
     protected final void setName(String setName) {
         name = setName;
     }
 
     /**
      * Get name of the player
      * @return Name of player
      */
     public final String getName() {
         return name;
     }
 
     /**
      * Set the name of the attack specific to the player. Set the same name of the critical, so if
      * you want to set the special critical name, call this first before setting the critical name! Otherwise the
      * name you set will be overwritten.
      * @param className The short class name for the move
      * @param name Name to be set. This name will be shown in the reports
      * @throws BattleSysException Thrown when the move specified by className does not exist, or the player does not own the move
      */
     protected final void setAtkName(String className, String name) throws BattleSysException {
         getMoveByName(className).name = name;
         getMoveByName(className).critName = name;
     }
 
     /**
      * Set the name of the critical attack specific to the player
      * @param className The short class name for the move
      * @param name Name to be set. This name will be shown in the reports
      * @throws BattleSysException Thrown when the move specified by className does not exist, or the player does not own the move
      */
     protected final void setCritName(String className, String name) throws BattleSysException {
         getMoveByName(className).critName = name;
     }
 
     /**
      *
      * @return
      */
     public final String getClassName() {
         return this.getClass().getName();
     }
 
     /**
      * rate of hit within a round
      * @return rate of hit within a round
      */
     public final double getRoundHitRate() {
         return roundTotalHits / (roundAttempts + 0.0);
     }
 
     /**
      * rate of hit throughout the battle
      * @return rate of hit throughout the run
      */
     public final double getHitRate() {
         return totalHits / (attempts + 0.0);
     }
 
     /**
      * rate of criticals within a round
      * @return rate of criticals within a round
      */
     public final double getRoundCriticalRate() {
         return roundTotalCriticals / (roundAttempts + 0.0);
     }
 
     /**
      * rate of criticals throughout the battle
      * @return rate of criticals throughout the run
      */
     public final double getCriticalRate() {
         return totalCriticals / (attempts + 0.0);
     }
 
     /**
      * The average damage dealt in this round
      * @return the roundAvgDamage
      */
     public final double getRoundAvgDamage() {
         return roundTotalDamage / (roundAttempts + 0.0);
     }
 
     /**
      * The maximum damage dealt in this round
      * @return the roundMaxDamage
      */
     public final long getRoundMaxDamage() {
         return roundMaxDamage;
     }
 
     /**
      * The average damage throughout the battle
      * @return the totalDamage
      */
     public final double getAvgDamage() {
         return totalDamage / (attempts + 0.0);
     }
 
     /**
      * The maximum damage throughout the battle
      * @return the maxDamage
      */
     public final long getMaxDamage() {
         return maxDamage;
     }
 
     /**
      * Number of attacks attempted throughout the battle
      * @return the attempts
      */
     public final long getAttempts() {
         return attempts;
     }
 
     /**
      * Number of attacks attempted for the last round
      * @return the roundAttempts
      */
     public final long getRoundAttempts() {
         return roundAttempts;
     }
 
     /**
      * Get a textual statistical description of the whole round
      * @return textual statistical description of the whole round
      */
     public final String getRoundStatDescription() {
         return name + "命中率" + Utility.percentageForm(getRoundHitRate())
                 + "%；暴擊率" + Utility.percentageForm(getRoundCriticalRate())
                 + "%；平均傷害" + Math.round(getRoundAvgDamage())
                 + "；最大傷害" + getRoundMaxDamage();
     }
 
     /**
      * Get a textual statistical description of the whole run. Also shows the points given to each of the ability
      * @return textual statistical description of the whole run
      */
     public final String getTotalStatDescription() {
         return name + "(" + initHp + "," + initAtk + "," + initDef + "," + initSpd + "," + initMor
                 + ")：\n攻擊次數" + getAttempts()
                 + "；命中率" + Utility.percentageForm(getHitRate())
                 + "%；暴擊率" + Utility.percentageForm(getCriticalRate())
                 + "%；平均傷害" + Math.round(getAvgDamage())
                 + "；最大傷害" + getMaxDamage()
                 + "\n被攻擊次數" + getBeAttempts()
                 + "；被命中率" + Utility.percentageForm(getBeHitRate())
                 + "%；被暴擊率" + Utility.percentageForm(getBeCriticalRate())
                 + "%；被打平均傷害" + Math.round(getBeAvgDamage())
                 + "；被打最大傷害" + getBeMaxDamage() + "\n";
     }
 
     /**
      * Get a textual ability description of the player
      * @return textual ability description of the player
      */
     public final String getAbyDescription() {
         StringBuilder s = new StringBuilder("(" + initHp + "," + initAtk + "," + initDef + "," + initSpd + "," + initMor + "; ");
         for (Move m : boughtAtk) {
             s.append(m.getClass().getSimpleName());
             s.append(", ");
         }
         s.append(")");
         return s.toString();
     }
 
     /**
      * Get a textual string for showing status of the player
      * @return The string
      */
     public final String getStatusString() {
         StringBuilder result = new StringBuilder();
         for (int i = 0; i < STATUS_COUNT; ++i) {
             if (status[i]) {
                 if (!result.toString().equals("")) {
                     result.append("、");
                 }
                 result.append("[color=");
                 result.append(STATUS_COLOR[i]);
                 result.append("]");
                 result.append(STATUS_STRING[i]);
                 result.append("[/color]");
             }
         }
         if (result.toString().equals("")) {
             result.append("無");
         }
         return result.toString();
     }
 
     /**
      * Get the string for report before every round
      * @return The string
      */
     public final String getReportString() {
         return getName() + "體力" + Utility.colorText(getHp(), 0, 5000, 255, 0, 0, 0, 255, 0)
                 + "，狀態：" + getStatusString();
     }
 
     /**
      * Get the string for reporting the remaining HP after battle
      * @return The String
      */
     public final String getHpLeftStr() {
         return getName() + "剩下體力" + getHp();
     }
 
     /**
      * @return the rate of being hit, throughout the battle
      */
     public final double getBeHitRate() {
         return beTotalHits / (beAttempts + 0.0);
     }
 
     /**
      * @return the rate of being hit critically, throughout the battle
      */
     public final double getBeCriticalRate() {
         return beCriticals / (beAttempts + 0.0);
     }
 
     /**
      * @return the average damage taken, throughout the battle
      */
     public final double getBeAvgDamage() {
         return beTotalDamage / (beAttempts + 0.0);
     }
 
     /**
      * @return the maximum damage taken, throughout the battle
      */
     public final long getBeMaxDamage() {
         return beMaxDamage;
     }
 
     /**
      * @return the number of attacks taken, throughout the battle
      */
     public final long getBeAttempts() {
         return beAttempts;
     }
 
     /**
      * Obtain number of times a move can be used, left. 
      * @param name the Short Class Name for the move concerned
      * @return the number of times
      * @throws MoveNotBoughtException Thrown when the move is not bought.
      */
     public final int getMoveTime(String name) throws BattleSysException {
         return getMoveByName(name).getMoveTime();
     }
 
     /**
      * Obtain number of times a move can be used, left.
      * @param move The move object of the move
      * @return the number of times
      */
     public final int getMoveTime(Move move) {
         return move.getMoveTime();
     }
 
     /**
      * Obtain how many times a move has been used
      * @param name the Short Class Name for the move concerned
      * @return the number of times
      * @throws MoveNotBoughtException Thrown when the move is not bought.
      */
     public final int getUsedTime(String name) throws BattleSysException {
         return getMoveByName(name).usedTime;
     }
 
     /**
      * Obtain how many times a move has been used
      * @param move
      * @return The number of times
      */
     public final int getUsedTime(Move move) {
         return move.usedTime;
     }
 
     /**
      * Whether the player has spoken in this round.
      * @return Whether the player has spoken in this round.
      */
     public final boolean getSpoken() {
         return spoken;
     }
 
     /**
      * point used by this player
      * @return point used by this player
      */
     public final int getPlayerPoint() {
         return TOTAL_POINTS - pointRemaining;
     }
 
     /**
      * String representation of the player, which is the name of the player with its class name, and its team name
      * @return The name of the player
      */
     @Override
     public final String toString() {
         return getName() + "(" + getClassName() + ")" + ", team " + getTeamName();
     }
 
     /**
      * Get the value difference between the current value and actual value (including ability-changing moves).
      * The difference due to random events.
      * @param quality Quality of the player affected, one of the AFFECT_ATK, AFFECT_DEF, AFFECT_SPD, AFFECT_MOR.
      * @return positive value difference if the player has been positively affected by random events, negative
      * in contrast. If no such effect zero is returned.
      */
     public final int getREEffect(int quality) {
         if (reAffect[quality] > 0) {
             switch (quality) {
                 case Move.AFFECT_ATK:
                     return atk - btAtk;
                 case Move.AFFECT_DEF:
                     return def - btDef;
                 case Move.AFFECT_SPD:
                     return spd - btSpd;
                 case Move.AFFECT_MOR:
                     return mor - btMor;
                 default:
                     return 0;
             }
         }
         return 0;
     }
 
     /**
      * Execute a battle, and shows the procedure and result
      * @param t1 Player list of team 1
      * @param t2 Player list of team 2
      * @param teamName1 Name of team 1
      * @param teamName2 Name of team 2
      * @param battleNo Battle number (non-R1 only)
      * @param graphName The name of the graph generated
      * @return Battle result storing the winner, number of round played and the battle record
      * @throws BattleSysException
      */
     public static final SingleBattleResult battle(PlayerList t1, PlayerList t2, String teamName1, String teamName2, int battleNo, String graphName) throws BattleSysException {
 
         //Array of all players on the battlefield
         PlayerList allPlayers = PlayerList.fromLists(t1, t2);
 
         StringBuilder result = new StringBuilder();
 
         //ordering
         int[] ordering = new int[allPlayers.size()];
         for (int i = 0; i < allPlayers.size(); ++i) {
             ordering[i] = i;
         }
 
         t1.preBattle();
         t2.preBattle();
 
         result.append("[quote][b]").append(teamName1).append("對").append(teamName2).append("的戰鬥[/b]\n");
 
         t1.preBattleSpeech(t1, t2);
         t2.preBattleSpeech(t2, t1);
 
         int round = 1;
 
         BattleRecord br = new BattleRecord(graphName + ".Graph", allPlayers, teamName1 + "對" + teamName2 + "的戰鬥");
 
         //Winning condition - get one player HP down to zero
         try {
             while (!t1.allDead() && !t2.allDead()) {
                 result.append("[color=#770000]－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－[/color]\n" + "[color=#007777]").
                         append(Utility.listString(Utility.objArr2StrArr(t1.invokeForPlayers("getReportString", new Class[0], new Object[0], true)), "\n")).append('\n').
                         append(" [/color][color=#777700]").append(Utility.listString(Utility.objArr2StrArr(t2.invokeForPlayers("getReportString", new Class[0], new Object[0], true)), "\n")).append('\n').
                         append(" [/color]" + "[b]第").append(Utility.colorText(round, 1, 100, 0, 255, 0, 0, 0, 255)).append("輪攻勢[/b]\n");
 
                 RandomEventResult re;
                 re = RandomEvent.randomEvent(allPlayers);
 
                 result.append(t1.preRound(t1, t2));
                 result.append(t2.preRound(t2, t1));
 
                 //shuffle the int array
                 ordering = Utility.shuffle(ordering);
 
                 CompleteMoveResult m = new CompleteMoveResult(ordering.length);
 
                 for (int i = 0; i < ordering.length; ++i) {
                     //initial move result
                     m.add(new SingleMoveResult(0, false, false, allPlayers.get(ordering[i]), allPlayers.get(ordering[i]), ""));
                 }
 
                 for (int i = 0; i < ordering.length; ++i) {
 
                     PlayerList thisSide, opposingSide;
 
                     if (t1.contains(allPlayers.get(ordering[i]))) {
                         thisSide = t1;
                         opposingSide = t2;
                     } else {
                         thisSide = t2;
                         opposingSide = t1;
                     }
 
                     if (allPlayers.get(ordering[i]).hp > 0) {
                         result.append(thisSide == t1 ? "[color=#007777]" : "[color=#777700]");
                         m = allPlayers.get(ordering[i]).move(thisSide, opposingSide, round, battleNo, re, m);
                         result.append(m.getResultString()).append("[/color]");
                     }
                 }
 
                 t1.postRound();
                 t2.postRound();
 
                 br.setBattleRecord(round);
 
                 ++round;
             }
         } catch (BattleTooLongException ex) {
             ex.printStackTrace();
         }
 
         int winner = 0;
         if (t1.allDead() && !t2.allDead()) {
             winner = 2;
             result.append(teamName2).append("勝出。").
                     append(Utility.listString(Utility.objArr2StrArr(t2.invokeForPlayers("getHpLeftStr", new Class[0], new Object[0], true)), "；")).append('\n');
             t2.incWin();
             t1.incLose();
             //ct.setValue(k, j, false);
         } else if (!t1.allDead() && t2.allDead()) {
             winner = 1;
             result.append(teamName1).append("勝出。").
                     append(Utility.listString(Utility.objArr2StrArr(t1.invokeForPlayers("getHpLeftStr", new Class[0], new Object[0], true)), "；")).append('\n');
             t1.incWin();
             t2.incLose();
             //ct.setValue(j, k, false);
         } else {
             result.append("打和\n");
             winner = 0;
             t1.incDraw();
             t2.incDraw();
             //ct.setValue(j, k, true);
         }
 
         t1.postBattleSpeech(t1, t2, winner == 1);
         t2.postBattleSpeech(t2, t1, winner == 2);
 
         result.append("[color=#007777]").append(Utility.listString(Utility.objArr2StrArr(t1.invokeForPlayers("getRoundStatDescription", new Class[0], new Object[0], false)), "\n")).append('\n').
                 append(" [/color][color=#777700]").append(Utility.listString(Utility.objArr2StrArr(t2.invokeForPlayers("getRoundStatDescription", new Class[0], new Object[0], false)), "\n")).
                 append("[/color][/quote]");
 
         return new SingleBattleResult(round, winner, br, result.toString());
     }
 }
