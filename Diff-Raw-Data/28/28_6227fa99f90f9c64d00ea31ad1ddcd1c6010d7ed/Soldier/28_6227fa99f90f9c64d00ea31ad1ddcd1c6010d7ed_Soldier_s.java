 package squirtlesquad;
 
 import java.util.Arrays;
 
 import battlecode.common.*;
 
 public class Soldier extends Unit {
 
    public int[] dirOffsets = {4,-3,3,-2,2,-1,1,0};
     public int round = -1;
     public int strat = 0;
     public RobotController rc = null;
     public int id;
     public MapLocation rallyPoint, enemyHq, hq, currentLoc;
     public boolean engaged = false;
     public Team otherTeam, myTeam;
     public int genDir;
     public int width, height;
     public boolean nukeWarning = false;
     public boolean defend = false;
 
     public Soldier(RobotController passedRc) {
         rc = passedRc;
         //		strat = inStrat;
         //		strat=0;
 
         myTeam = rc.getTeam();
         otherTeam = myTeam.opponent();
         id = rc.getRobot().getID();
         hq = rc.senseHQLocation();
         enemyHq = rc.senseEnemyHQLocation();
         genDir=id%8;
         strat=id%3;
         //		strat = 1;
         run();
         width = rc.getMapWidth();
         height = rc.getMapHeight();
     }
 
     @Override
     public void run() {
         while (true) {
             try {
                 if (rc.isActive()) {
                     round = Clock.getRoundNum();
                     currentLoc = rc.getLocation();
                     if (rc.readBroadcast(Constants.nukeWarningChannel) == Constants.nukeWarningValue) {
                         nukeWarning = true;
                     }
                     defend = rc.readBroadcast(Constants.retreatChannel)==Constants.retreatWarningValue;
                     if (nukeWarning) {
                         strat = 2;
                     }
                     if (defend)
                         strat = 1;
                     switch(strat){
                     case 0:
                         runExplorer();
                         break;
                     case 1:
                         runDefender();
                         break;
                     case 2:
                         runAttacker();
                         break;
                     }
                 }
                 rc.yield();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     public void runExplorer() throws GameActionException{
         MapLocation[] encamps=rc.senseEncampmentSquares(currentLoc,100,Team.NEUTRAL);
         if(enemyHq.distanceSquaredTo(currentLoc)<100 && !(rc.senseEncampmentSquare(currentLoc) && enemyHq.distanceSquaredTo(currentLoc)<63)) {
             Direction dir=currentLoc.directionTo(enemyHq);
             if(rc.canMove(dir)) {
                 move(dir);
             }
             else {
                 Direction dirt;
                 for(int i=8;--i>=0;) {
                     dirt=Direction.values()[(dir.ordinal()+dirOffsets[i]+8)%8];
                     if(rc.canMove(dirt)) {
                         move(dirt);
                         break;
                     }
                 }
             }
         }
         else {
             if(rc.senseEncampmentSquare(currentLoc)) {
                 double pow = rc.getTeamPower();
                 double need = rc.senseCaptureCost();
                 if(pow > need) {
                     rc.captureEncampment(pickEncampment());
                 }
                 else if(pow-need > -10){
                     return;
                 }
                 else{
                     strat=1;
                 }
             }
             else {
                 Direction dir;
                 int minDist = 1000;
                 MapLocation target = currentLoc;
                 for(MapLocation camp : encamps){
                     int dist = camp.distanceSquaredTo(currentLoc);
                     if(dist < minDist){
                         target = camp;
                         minDist = dist;
                     }
                 }
                 dir = currentLoc.directionTo(target);
                 for(int i=8; --i>=0;) {
                     dir=Direction.values()[(dir.ordinal()+dirOffsets[i]+8)%8];
                     if(rc.canMove(dir)) {
                         move(dir);
                         return;
                     }
                 }
                 //				strat=1;
             }
         }
     }
 
 
     public void runDefender() throws GameActionException{
        if(Math.random()<.5 && (round%400)==200)
             {
                 strat=2;
             }
         else{
             if (rc.isActive()) {
                 if(RobotPlayer.debug)
                     System.out.println("defending");
                 MapLocation target = hq.add(hq.directionTo(enemyHq));
                 Direction dir = Direction.NONE;
                 if(rc.senseEncampmentSquare(currentLoc) && rc.getTeamPower() > rc.senseCaptureCost() && (rc.senseEncampmentSquares(hq, 2, null).length+rc.senseMineLocations(hq,  2, Team.NEUTRAL).length) < 8){
                     rc.captureEncampment(pickEncampment());
                     return;
                 }
                 else {
                     MapLocation[] someMyCamps = rc.senseEncampmentSquares(hq, 256, myTeam);
                     MapLocation[] myCamps = Arrays.copyOf(someMyCamps,someMyCamps.length+1);
                     myCamps[myCamps.length-1] = hq;
                     double max = -10000;
                     int maxdex = 0;
                     for(int i = 0; i < myCamps.length; i++){
                         double def = defensibility(myCamps[i]);
                         if(def == Math.max(def, max)){
                             max = def;
                             maxdex = i;
                         }
                     }
                     if(RobotPlayer.debug){
                         System.out.println(myCamps.length + " many camps found");
                     }
                     for(int i = 0; i < myCamps.length; i++){
                         if(i == maxdex){
                             target = myCamps[i].add(myCamps[i].directionTo(enemyHq));
                             if(RobotPlayer.debug){
                                 System.out.println("defending camp at " + myCamps[i]);
                             }
                             break;
                         }
                     }
                     dir = currentLoc.directionTo(target);
                    if (rc.senseMine(currentLoc) == null && currentLoc.distanceSquaredTo(target) <= 14 && Math.random()<0.5) {
                         // Lay a mine
                         if(RobotPlayer.debug)
                             System.out.println("mining");
                         rc.layMine();
                         return;
                     }
                     else if(rc.senseMineLocations(target, 2, myTeam).length < 8){
                         for(Direction d : Direction.values()){
                             if(rc.senseMine(target.add(d))==null){
                                 if(RobotPlayer.debug){
                                     System.out.println("mine found missing in direction " + dir);
                                 }
                                 dir = currentLoc.directionTo(target.add(d));
                                 break;
                             }
                         }
                     }
                 }
 
                 for (int i=8; --i>=0;) {
                     Direction trying = Direction.values()[(dirOffsets[i]+dir.ordinal()+8)%8];
                     if (rc.canMove(trying)) {
                         dir = trying;
                         break;
                     }
                 }
 
                 target = rc.getLocation().add(dir);
                 if(!dir.equals(Direction.NONE) && !dir.equals(Direction.OMNI) && rc.canMove(dir)){
                     if(rc.senseMine(target) == null || rc.senseMine(target).equals(rc.getTeam())) {
                         rc.move(dir);
                         rc.setIndicatorString(0, "Last direction moved: "+dir.toString());
                         return;
                     }
                     else if(!rc.senseMine(target).equals(rc.getTeam())){
                         rc.defuseMine(target);
                     }
                 }
                 else{
                     do{
                         dir = Direction.values()[(int)(Math.random()*8)];
                     }
                     while(!rc.canMove(dir) || (dir.equals(Direction.NONE) || dir.equals(Direction.OMNI)));
 
                     target = rc.getLocation().add(dir);
                     if(rc.canMove(dir) && (rc.senseMine(target) == null || rc.senseMine(target).equals(rc.getTeam()))) {
                         rc.move(dir);
                         rc.setIndicatorString(0, "Last direction moved: "+dir.toString());
                     }
                     else if(!rc.senseMine(target).equals(rc.getTeam())){
                         rc.defuseMine(target);
                     }
                 }
             }
         }
     }
 
     public void runAttacker() throws GameActionException{
         if(rc.senseEncampmentSquare(rc.getLocation())&&!nukeWarning) {
             if(rc.getTeamPower()>rc.senseCaptureCost()) {
                 rc.captureEncampment(pickEncampment());
             }
             else
                 strat=1;
 
         } else if(round%Constants.moduloForAttacks<Constants.attackAfterEvery&&!nukeWarning) {
             MapLocation base=hq;
 
             Direction dir=currentLoc.directionTo(base);
             if(rc.canMove(dir)) {
                 move(dir);
             } else {
                 Direction dirt;
                 for(int i=8; --i>=0;) {
                     dirt = Direction.values()[(dirOffsets[i]+dir.ordinal()+8)%8];
                     //dirt=Direction.values()[(dir.ordinal()+i+8)%8];
                     if(rc.canMove(dirt)) {
                         move(dirt);
                         break;
                     }
                 }
             }
         } else {
             MapLocation goal=enemyHq;
             Direction dir=currentLoc.directionTo(goal);
             if(rc.canMove(dir)) {
                 move(dir);
             } else {
                 Direction dirt;
                 for(int i=8;--i>=0;) {
                     dirt = Direction.values()[(dirOffsets[i]+dir.ordinal()+8)%8];
                     //dirt=Direction.values()[(dir.ordinal()+i+8)%8];
                     if(rc.canMove(dirt)) {
                         move(dirt);
                         break;
                     }
                 }
             }
             //strat=2;
         }
     }
 
 
     public double defensibility(MapLocation camp){
         int homeDist = camp.distanceSquaredTo(hq);
         int enemyDist = camp.distanceSquaredTo(enemyHq);
         int hypotenuse = hq.distanceSquaredTo(enemyHq);
         int targetDist = currentLoc.distanceSquaredTo(camp);
         int friendlyNum = rc.senseNearbyGameObjects(Robot.class, camp, 14, myTeam).length;
         int enemyNum = rc.senseNearbyGameObjects(Robot.class, camp, 64, otherTeam).length;
 
         int sqDiff = homeDist+enemyDist-hypotenuse;
        return enemyNum*5-4*friendlyNum - 0.25*sqDiff-0.1*targetDist;
     }
 
     public RobotType pickEncampment() throws GameActionException{
         int power = (int)rc.getTeamPower();
         int homeDist = currentLoc.distanceSquaredTo(hq);
         int targetDist = currentLoc.distanceSquaredTo(enemyHq);
         int hypotenuse = hq.distanceSquaredTo(enemyHq);
         MapLocation[] allyCamps = rc.senseAlliedEncampmentSquares();
         int numAlliedCamps = allyCamps.length;
         int numArtillery = 0;
         for(MapLocation loc : allyCamps){
             if(rc.senseRobotInfo((Robot)rc.senseObjectAtLocation(loc)).type.equals(RobotType.ARTILLERY)){
                 numArtillery++;
             }
         }
         if(homeDist + targetDist > .85*hypotenuse || homeDist < 0.9*targetDist){
             if((numAlliedCamps==0 && (id%7==1 || id%7==6 || id%7 == 4))|| (numAlliedCamps != 0&& (id%7==2 || id%7==3 || id%7 == 4) && numArtillery/numAlliedCamps < 0.35)){
                 System.out.println(numArtillery + " artilleries and " + numAlliedCamps + " camps total");
                 return RobotType.ARTILLERY;
             }
            else if(power < 160){
                 System.out.println("generator");
                 return RobotType.GENERATOR;
             }
             else{
                 System.out.println("supplier");
                 return RobotType.SUPPLIER;
             }
         }
         else{
             int nearbyArtillery = 0;
             for(MapLocation l : rc.senseEncampmentSquares(currentLoc, 64, Team.NEUTRAL)){
                 if(rc.canSenseSquare(l) && rc.senseObjectAtLocation(l) != null && rc.senseRobotInfo((Robot)rc.senseObjectAtLocation(l)).type.equals(RobotType.ARTILLERY)){
                     nearbyArtillery++;
                 }
             }
             int nearbyFriendly = rc.senseNearbyGameObjects(Robot.class, 14, myTeam).length;
             int nearbyEnemy = rc.senseNearbyGameObjects(Robot.class, 64, otherTeam).length;
             if(nearbyArtillery > 0){
                 return RobotType.SHIELDS;
             }
             else if(homeDist < hypotenuse/9){
                 if(nearbyFriendly > 3 && nearbyEnemy > 3){
                     return RobotType.MEDBAY;
                 }
                 else if(power < 50){
                     return RobotType.GENERATOR;
                 }
                 else
                     return RobotType.ARTILLERY;
             }
             else{
                 return RobotType.ARTILLERY;
             }
         }
     }
 
     public void move(Direction dir) throws GameActionException
     {
         MapLocation[] mines=rc.senseNonAlliedMineLocations(rc.getLocation(), 2);
         MapLocation loc=currentLoc.add(dir);
         boolean die=false;
         for(MapLocation m:mines) {
             if(m.equals(loc)) {
                 die=true;
             }
         }
         if(!(die)) {
             rc.move(dir);
             rc.setIndicatorString(0, "Last direction moved: "+dir.toString());
         }
         else {
             rc.defuseMine(loc);
         }
     }
 }
