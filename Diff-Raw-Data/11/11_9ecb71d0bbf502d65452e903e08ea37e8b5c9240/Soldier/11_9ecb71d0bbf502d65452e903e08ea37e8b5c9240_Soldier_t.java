 package fieldbot;
 
 import battlecode.common.*;
 
 public class Soldier
 {
 
     private static final int GL_RADIUS = 100 * 100;
     private static final int LC_RADIUS =
         RobotType.ARTILLERY.attackRadiusMaxSquared;
 
     private static final int MAX_MINES  = 10;
     private static final int MAX_ROBOTS = 20;
     private static final int MAX_BASES = 10;
     private static final double MAX_SHIELD = 40;
 
     private static void strengthen(
             double[] strength, Direction dir, double force)
     {
         if (dir == Direction.OMNI) return;
 
         int ord = dir.ordinal();
         final int mask = 8 - 1;
 
         strength[ord] += force;
 
         double dropoff = 1.0;
 
         for (int i = 1; i < 4 && dropoff > 0.0; ++i) {
             dropoff -= Weights.DROPOFF;
             strength[(ord - i) & mask] += force * dropoff;
             strength[(ord + i) & mask] += force * dropoff;
         }
     }
 
     private static void strengthen(
             double[] strength, Direction dir, double weight, double dist)
     {
         strengthen(strength, dir, weight * (1 / dist));
     }
 
 
     /**
      */
     private static void mines(
             RobotController rc, MapLocation coord, double strength[],
             double w, int radius)
         throws GameActionException
     {
         MapLocation mines[] = rc.senseNonAlliedMineLocations(coord, radius);
         int steps = Math.max(1, Utils.ceilDiv(mines.length, MAX_MINES));
         int count = 0;
 
         for (int i = 0; i < mines.length; i += steps) {
             Direction dir = coord.directionTo(mines[i]);
             strengthen(strength, dir, w, coord.distanceSquaredTo(mines[i]));
             count++;
         }
 
         rc.setIndicatorString(1, "mines=" + count + "/" + mines.length);
     }
 
     /**
      */
     private static void battleFormation(
             RobotController rc, MapLocation coord, double strength[],
             double w, Team team)
         throws GameActionException
     {
         // TODO: finish this method
 
         // when enemies are nearby, group into a tight formation
 
         Robot robots[] = rc.senseNearbyGameObjects(Robot.class, 3, team);
         Robot enemyRobots[] = rc.senseNearbyGameObjects(
                 Robot.class, GL_RADIUS, team.opponent());
 
         MapLocation closestEnemy = findClosest(rc, enemyRobots);
         Direction toward = coord.directionTo(closestEnemy);
 
         // check if we're already in formation
         boolean grouped = false;
         GameObject oneLeft = rc.senseObjectAtLocation(coord.add(leftOf(toward)));
         if (oneLeft != null) {
             if (oneLeft.getTeam().equals(team))
                 grouped = true;
         }
         GameObject oneRight = rc.senseObjectAtLocation(coord.add(rightOf(toward)));
         if (oneRight != null) {
             if (oneRight.getTeam().equals(team))
                 grouped = true;
         }
 
         // if we're already grouped, attack!
         if (grouped)
             strengthen(strength, toward, Weights.GROUP_ATTACK);
         // otherwise, group up
         else {
             MapLocation closestAlly = findClosest(rc, robots);
             strengthen(strength, toward, Weights.GROUP_UP);
         }
     }
 
     public static Direction rightOf(Direction from){
         return from.rotateRight().rotateRight();
     }
     public static Direction leftOf(Direction from){
         return from.rotateLeft().rotateLeft();
     }
 
     private static MapLocation findClosest(
             RobotController rc, Robot[] otherRobots) 
         throws GameActionException {
         int closestDist = Integer.MAX_VALUE;
         MapLocation closestEnemy=null;
         for (int i=0;i<otherRobots.length;i++){
             Robot robot = otherRobots[i];
             RobotInfo robotInfo = rc.senseRobotInfo(robot);
             int dist = robotInfo.location.distanceSquaredTo(rc.getLocation());
             if (dist<closestDist){
                 closestDist = dist;
                 closestEnemy = robotInfo.location;
             }
         }
         return closestEnemy;
     }
 
     /**
      */
     private static void globalRobots(
             RobotController rc, MapLocation coord,
             double strength[], double w, Team team)
         throws GameActionException
     {
         Robot robots[] = rc.senseNearbyGameObjects(
                 Robot.class, GL_RADIUS, team);
 
         int steps = Math.max(1, Utils.ceilDiv(robots.length, MAX_ROBOTS));
 
         for (int i = 0; i < robots.length; i += steps) {
             MapLocation robotCoord = rc.senseRobotInfo(robots[i]).location;
             Direction dir = coord.directionTo(robotCoord);
 
             strengthen(strength, dir, w, coord.distanceSquaredTo(robotCoord));
         }
 
         rc.setIndicatorString(0, "global=" + robots.length);
     }
     
     /**
      */
     private static boolean localRobots(
             RobotController rc, MapLocation coord, double strength[], Team team)
         throws GameActionException
     {
         Robot enemies[] = rc.senseNearbyGameObjects(
                 Robot.class, LC_RADIUS, team.opponent());
 
         if (enemies.length == 0) return false;
 
         int x = 0, y = 0;
         int numEnemies = 0;
         int steps = Math.max(1, Utils.ceilDiv(enemies.length, MAX_ROBOTS));
 
         for (int i = 0; i < enemies.length; i += steps) {
             RobotInfo info = rc.senseRobotInfo(enemies[i]);
             if (info.type != RobotType.SOLDIER &&
                     info.type != RobotType.ARTILLERY)
             {
                 continue;
             }
 
             x += info.location.x;
             y += info.location.y;
             numEnemies++;
         }
 
         if (numEnemies == 0) return false;
 
         MapLocation enemyCenter = new MapLocation(x/numEnemies, y/numEnemies);
 
         Robot allies[] = rc.senseNearbyGameObjects(Robot.class, LC_RADIUS, team);
         steps = Math.max(1, Utils.ceilDiv(enemies.length, MAX_ROBOTS));
         int numAllies = 0;
 
         for (int i = 0; i < allies.length; i += steps) {
             RobotInfo info = rc.senseRobotInfo(allies[i]);
             if (info.type != RobotType.SOLDIER &&
                     info.type != RobotType.ARTILLERY)
             {
                 continue;
             }
             numAllies++;
         }
 
         double force = (
                 numAllies * Weights.LC_ALLY_SD -
                 numEnemies * Weights.LC_ENEMY_SD) *
             Weights.LC_MUL;
 
         rc.setIndicatorString(0,
                 "enemies=" + numEnemies + "/" + enemies.length +
                 ", allies=" + numAllies + "/" + allies.length +
                 ", enemyCenter=" + enemyCenter.toString());
 
         Direction chargeDir = coord.directionTo(enemyCenter);
         strengthen(strength, chargeDir, force);
 
         return true;
     }
 
 
     /**
      */
     private static void neutralBases(
             RobotController rc, MapLocation coord, double strength[])
         throws GameActionException
     {
         if (GameConstants.CAPTURE_POWER_COST >= rc.getTeamPower()) return;
 
         MapLocation bases[] =
             rc.senseEncampmentSquares(coord, LC_RADIUS, Team.NEUTRAL);
 
         int steps = Math.max(1, Utils.ceilDiv(bases.length, MAX_BASES));
         int count = 0, taken = 0;
 
         for (int i = 0; i < bases.length; i += steps) {
             if (rc.canSenseSquare(bases[i])) {
                 if (rc.senseObjectAtLocation(bases[i]) != null) {
                     taken++;
                     continue;
                 }
             }
 
             Direction dir = coord.directionTo(bases[i]);
             strengthen(
                     strength, dir, Weights.CAPTURE,
                     coord.distanceSquaredTo(bases[i]));
 
             count++;
         }
 
         rc.setIndicatorString(2, "neutral=" + taken + "/" + count);
     }
 
 
     private static void allyBases(
             RobotController rc, MapLocation coord, double strength[], Team team)
         throws GameActionException
     {
         double energon = rc.getEnergon();
         double shield = rc.getShields();
 
         MapLocation bases[] = rc.senseEncampmentSquares(coord, LC_RADIUS, team);
         int steps = Math.max(1, Utils.ceilDiv(bases.length, MAX_BASES));
 
         int count = 0;
         double shields = 0;
         double med = 0;
 
         for (int i = 0; i < bases.length; i += steps) {
             Robot base = (Robot) rc.senseObjectAtLocation(bases[i]);
             RobotInfo info = rc.senseRobotInfo(base);
 
             double force;
             count++;
 
             if (info.type == RobotType.MEDBAY) {
                 force = ((RobotType.SOLDIER.maxEnergon - energon) /
                         RobotType.SOLDIER.maxEnergon) * Weights.HEAL;
                 med += force;
             }
 
             else if (info.type == RobotType.SHIELDS) {
                 force = (MAX_SHIELD - shield) * Weights.SHIELD;
                 shields += force;
             }
 
             else continue;
 
             strengthen(strength, coord.directionTo(info.location), force);
         }
 
         rc.setIndicatorString(2,
                 "ally=" + count + ", meds=" + med + ", shields=" + shields);
     }
 
     private static int numAlliedBases(RobotController rc, RobotType type)
         throws GameActionException {
             //TODO: fix this method
         MapLocation bases[] = rc.senseAlliedEncampmentSquares();
         int n = 0;
         for (MapLocation baseLoc : bases) {
             Robot base = (Robot) rc.senseObjectAtLocation(baseLoc);
             RobotInfo baseType = rc.senseRobotInfo(base);
             if (baseType.equals(type))
                 n++;
         }
         return n;
     }
 
     public static boolean capture(RobotController rc, MapLocation coord)
         throws GameActionException
     {
         if (!rc.senseEncampmentSquare(coord)) return false;
 
         double cost = rc.senseCaptureCost();
         if (cost <= 0.0) return false;
         if (cost >= rc.getTeamPower()) return false;
 
         double rnd = Math.random();
 
         double distEnemyHQ = Math.sqrt(coord.distanceSquaredTo(rc.senseEnemyHQLocation()));
         double distHQ = Math.sqrt(coord.distanceSquaredTo(rc.senseHQLocation()));
         double ratioToHQ = distHQ / (distEnemyHQ + distHQ);
 
         double distBetween = Math.sqrt(rc.senseHQLocation().distanceSquaredTo(rc.senseEnemyHQLocation()));
         double onPathRatio = distBetween / (distHQ + distEnemyHQ);
 
         // prioritize artillery on the path between the HQs, and closer to enemy HQ
         double artilleryWeight = Weights.ARTILLERY * 
         (Weights.PATH * onPathRatio + Weights.TO_HQ * ratioToHQ);
 
         rc.setIndicatorString(1, "distHQ=" + distHQ + ", distEnemy=" + distEnemyHQ + 
             ", onPath="+onPathRatio + ", toHQ="+ratioToHQ + 
             ", w="+artilleryWeight + ", rnd=" + rnd/* + 
             ", suppliers=" + numAlliedBases(rc, RobotType.SUPPLIER)*/);
 
 
         if (rnd < artilleryWeight)
             rc.captureEncampment(RobotType.ARTILLERY);
         else {
             // TODO: improve supplier/generator decision
            if (rc.getTeamPower() > Weights.MIN_POWER)
                 rc.captureEncampment(RobotType.SUPPLIER);
             else
                 rc.captureEncampment(RobotType.GENERATOR);
         }
 /*
         if (rnd < Weights.MEDBAY_SUM)
             rc.captureEncampment(RobotType.MEDBAY);
 
         else if (rnd < Weights.SHIELDS_SUM)
             rc.captureEncampment(RobotType.SHIELDS);
 
         else if (rnd < Weights.ARTILLERY_SUM)
             rc.captureEncampment(RobotType.ARTILLERY);
 
         else if (rnd < Weights.GENERATOR_SUM)
             rc.captureEncampment(RobotType.GENERATOR);
 
         else if (rnd < Weights.SUPPLIER_SUM)
             rc.captureEncampment(RobotType.SUPPLIER);
 
         else {
             System.out.println("Bad capture sums");
             rc.breakpoint();
         }
 */
         return true;
 
     }
 
 
     /**
      */
     public static void run(RobotController rc) throws GameActionException
     {
         final Team team = rc.getTeam();
         final Robot robot = rc.getRobot();
 
         rc.wearHat();
 
         while (true) {
 
             Math.random();
 
             if (!rc.isActive()) {
                 rc.yield();
                 continue;
             }
 
             debug_resetBc();
 
             MapLocation coord = rc.getLocation();
             double strength[] = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
 
 
             // Enemy HQ
             {
                 MapLocation hq = rc.senseEnemyHQLocation();
                 Direction dir = coord.directionTo(hq);
                 strengthen(
                         strength, dir, Weights.ENEMY_HQ,
                         hq.distanceSquaredTo(coord));
             }
 
             debug_checkBc(rc, "HQ");
 
 
             boolean enemiesNearby = localRobots(rc, coord, strength, team);
             debug_checkBc(rc, "local-robot");
 
             if (!enemiesNearby) {
                 mines(rc, coord, strength, Weights.EXPLORE_MINE, GL_RADIUS);
                 debug_checkBc(rc, "explore-mine");
 
                 neutralBases(rc, coord, strength);
                 debug_checkBc(rc, "neutral-base");
 
                 globalRobots(rc, coord, strength, Weights.GL_ENEMY_SD, team.opponent());
                 // globalRobots(rc, coord, strength, Weights.GL_ALLY_SD, team);
 
                 //defensiveMines(rc, coord, strength);
 
                 debug_checkBc(rc, "global-robot");
             }
             else {
                 mines(rc, coord, strength, Weights.BATTLE_MINE, LC_RADIUS);
                 debug_checkBc(rc, "battle-mine");
 
                 allyBases(rc, coord, strength, team);
                 debug_checkBc(rc, "ally-base");
 
                 //battleFormation(rc, coord, strength, Weights.FORMATION, team);
             }
 
             // Compute the final direction.
 
             double maxStrength = Double.NEGATIVE_INFINITY;
             Direction finalDir = null;
 
             for (int i = 0; i < 8; ++i) {
                 if (maxStrength > strength[i]) continue;
                 if (!rc.canMove(Utils.dirByOrd[i])) continue;
 
                 maxStrength = strength[i];
                 finalDir = Utils.dirByOrd[i];
             }
 
             // debug_dumpStrength(rc, strength);
             debug_checkBc(rc, "select-strength");
 
             if (finalDir == null) { rc.yield(); continue; }
 
 
             // Execute the move safely.
             if (enemiesNearby || !capture(rc, coord)) {
 
                 debug_checkBc(rc, "capture");
 
                 MapLocation target = coord.add(finalDir);
                 Team mine = rc.senseMine(target);
 
                 if (mine == null || mine == team) {
                     if (rc.canMove(finalDir))
                         rc.move(finalDir);
                 }
                 else rc.defuseMine(target);
 
             }
 
             debug_checkBc(rc, "end");
 
             rc.yield();
         }
     }
 
 
     private static int lastBcCounter;
 
     private static void debug_resetBc()
     {
         lastBcCounter = Clock.getBytecodeNum();
     }
 
     private static void debug_checkBc(RobotController rc, String where)
     {
         int bcCounter = Clock.getBytecodeNum();
         if (bcCounter < lastBcCounter) {
             System.err.println(
                     "BC EXCEEDED: " + where + ", " +
                     lastBcCounter +  " -> " + bcCounter);
             rc.breakpoint();
         }
         lastBcCounter = bcCounter;
     }
 
     private static int debug_countBc(boolean reset)
     {
         int bcCounter = Clock.getBytecodeNum();
         int delta = bcCounter - lastBcCounter;
         if (reset) lastBcCounter = bcCounter;
         return delta;
     }
 
     private static void debug_dumpStrength(
             RobotController rc, double[] strength)
     {
         String str = "{ ";
         for (int i = 0; i < 8; ++i) {
             str += "(" + Utils.dirByOrd[i].name() +
                 "=" + ((int)(strength[i] * 10000)) + ") ";
         }
         str += "}";
 
         rc.setIndicatorString(0, str);
     }
 
 }
