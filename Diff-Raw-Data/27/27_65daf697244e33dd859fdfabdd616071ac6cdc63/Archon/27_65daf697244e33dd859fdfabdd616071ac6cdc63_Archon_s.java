 package hax;
 
 import battlecode.common.*;
 
 import java.util.Random;
 
 public class Archon extends RobotBase {
     private MapLocation[] archons;
     private MapLocation flux_to_get;
     private int[] staticArchons = new int[6];    
    private int spawned_soldiers = 0;
 
     
 
     public Archon(RobotController rc) {
         super(rc);
         state = State.SEARCH;
     }
 
     public void init() {
         archons = rc.senseAlliedArchons();        
     }
 
     protected void go() throws GameActionException {
         rc.setIndicatorString(0, state.toString());
         while (rc.isMovementActive()) {
             rc.yield();
         }
         if (Clock.getRoundNum() % 2 == 0) {
             sense();
             sendTargets();
             transfer();
            if (e_nearby.size() > a_soldiers - 2 && rc.getEnergonLevel() > rc.getMaxEnergonLevel() / 2) {
                spawn(RobotType.SOLDIER);
                spawned_soldiers++;
            }
            if(spawned_soldiers % 3 == 0 && e_nearby.size() > a_soldiers - 2 && rc.getEnergonLevel() > rc.getMaxEnergonLevel() / 2)
             {
             	spawn(RobotType.CANNON); 
            }         
         }
         
         if(!e_nearby.isEmpty()) {
         	state = State.DEFENCE;
         	if(rc.canBurnFlux() && rc.getEnergonLevel() < 2 * rc.getMaxEnergonLevel() / 3){
         		rc.burnFlux();
         	}
         	return;
         } else if (state == State.DEFENCE){
         	if(rc.senseDirectionToOwnedFluxDeposit() == Direction.OMNI)
         		state = State.FOUND;
         	else
         		state = State.SEARCH;
         }
 
         switch (state) {
             case SEARCH:
                 search();
                 break;
             case GET_FLUX:
                 get_flux();
                 break;
             case GET_NEAREST:
                 nearest();
                 break;
             case FOUND:
                 found();
                 break;
             case GATHER:
                 gather();
                 break;
             case DONE:
                 return;
         }
     }
 
     private void updateArchons() {
         MapLocation[] new_archons = rc.senseAlliedArchons();
         for (int i = 0 ; i < new_archons.length ; ++i) {
             if (archons[i].equals(new_archons[i])) {
                 ++staticArchons[i];
             } else {
                 staticArchons[i] = 0;
             }
         }
 
         archons = new_archons;
     }
 
     private void get_flux() throws GameActionException {
         if (rc.getLocation().equals(flux_to_get)) {
             state = State.FOUND;
             return;
         }
 
         Direction uf = rc.getLocation().directionTo(flux_to_get);
 
         if (rc.senseAirRobotAtLocation(flux_to_get) != null) {
             state = State.SEARCH;
             return;
         }
 
         if (! rc.getDirection().equals(uf)) {
             rc.setDirection(uf);
         } else if (rc.canMove(uf)) {
             rc.moveForward();
         }
     }
 
     private void search() throws GameActionException {
         updateArchons();
 
         Direction uf = rc.senseDirectionToUnownedFluxDeposit();
         MapLocation ml = rc.getLocation();
 
         boolean go = true;
 
         for (MapLocation l : archons) {
             if (ml.directionTo(l).equals(uf)) {
                 go = false;
                 break;
             }
         }
 
         if (go) {
             state = State.GET_NEAREST;
             return;
         }
 
         wander(1, 10);
     }
 
     void found() throws GameActionException {
         spawn(RobotType.WORKER);
         state = State.GATHER;
     }
 
     private void spawn(RobotType rt) {
         boolean done = false;
         if (rt.isAirborne()) {
 
         } else {
             while (! done) {
                 try {
                     done = spawnGround(rt);
                 } catch (Exception e) {
                 }
             }
         }
         rc.yield();
     }
 
     private boolean spawnGround(RobotType rt) throws GameActionException {
         Direction d = rc.getDirection();
         MapLocation l = rc.getLocation();
 
         while (rc.senseTerrainTile(l.add(d)).getType() != TerrainTile.TerrainType.LAND || rc.senseGroundRobotAtLocation(l.add(d)) != null) {
             d = d.rotateRight();
         }
 
         if (! d.equals(rc.getDirection())) {
             rc.setDirection(d);
             rc.yield();
         }
         while (rt.spawnCost() > rc.getEnergonLevel()) {
             rc.yield();
         }
         rc.spawn(rt);
 
         return true;
     }
     
     private void gather() {
         if (Clock.getRoundNum() % 10 == 0) {
             if (a_workers < 3 && rc.getEnergonLevel() > rc.getMaxEnergonLevel() / 2) {
                 spawn(RobotType.WORKER);
             }
         }
     }
 
     void nearest() throws GameActionException {
         Direction uf = rc.senseDirectionToUnownedFluxDeposit();
 
         if (uf.equals(Direction.NONE)) {
             state = State.DONE;
         } else if (uf.equals(Direction.OMNI)) {
             state = State.FOUND;
         } else if (! rc.getDirection().equals(uf)) {
             rc.setDirection(uf);
         } else if (rc.canMove(uf)) {
             rc.moveForward();
         } else {
             state = State.SEARCH;
         }
     }
 }
