 package hax;
 
 import battlecode.common.*;
 
 import java.util.*;
 
 public abstract class RobotBase {
     protected final RobotController rc;
     protected final MapLocation startLocation;
     protected State state;
     protected Random rand;
 
     protected final HashSet<Robot> allies = new HashSet<Robot>();
     protected final HashSet<Robot> a_nearby = new HashSet<Robot>();
     protected int a_archons = 0;
     protected int a_workers = 0;
     protected int a_soldiers = 0;
 
     protected final HashSet<Robot> enemies = new HashSet<Robot>();
     protected final HashSet<Robot> e_nearby = new HashSet<Robot>();
     protected final HashSet<MapLocation> targets_g = new HashSet<MapLocation>();
     protected final HashSet<MapLocation> targets_a = new HashSet<MapLocation>();
     protected int e_archons = 0;
     protected int e_workers = 0;
     protected int e_soldiers = 0;
         
     protected final HashMap<Robot, RobotInfo> r_info = new HashMap<Robot, RobotInfo>();
 
     protected enum MoveState {
         MOVING,
         DONE,
         BLOCKED
     }
     
     enum Position {
     	AIR, //0
     	GROUND, //1
     	NONE,
     	BOTH
     }
     
     protected enum State {
         GET_NEAREST,
         GET_FLUX,        
         SEARCH,
         FOUND,
         GATHER,
         DONE,
         DEFENCE,
         ATTACK_G,
         ATTACK_A,
         GET_BLOCK,
         GET_BACK,
         UNLOAD
     }
 
     public RobotBase(RobotController rc) {
         this.rc = rc;
         this.startLocation = rc.getLocation();
         rand = new Random(rc.getLocation().hashCode());
         //System.out.println(rc.getRobotType().toString() + " @ " + rc.getLocation());
     }
 
     public void play() throws GameActionException {
         init();
         while (true) {
             try {
                 go();
             } catch(Exception e) {
                 System.out.println("caught exception:");
                 e.printStackTrace();
             }
             rc.yield();
         }
     }
 
     protected abstract void init() throws GameActionException;
 
     protected abstract void go() throws GameActionException;
 
     protected final void sense() throws GameActionException {
         a_nearby.clear();
         a_archons = 0;
         a_workers = 0;
         a_soldiers = 0;
 
         e_nearby.clear();
         e_archons = 0;
         e_workers = 0;
         e_soldiers = 0;
         targets_a.clear();
         targets_g.clear();
 
         doSense(rc.senseNearbyGroundRobots());
         doSense(rc.senseNearbyAirRobots());
     }
 
     protected final boolean is_air(RobotType rt){
     	return rt == RobotType.ARCHON || rt == RobotType.SCOUT;
     }
     
     protected final void doSense(Robot[] robots) throws GameActionException {
         for (Robot r : robots) {
             if (rc.canSenseObject(r)) {
                 RobotInfo ri = rc.senseRobotInfo(r);
                 r_info.put(r, ri);
                 if (ri.team != rc.getTeam()) {
                     enemies.add(r);
                     e_nearby.add(r);
                     if(is_air(ri.type)) {
                     	targets_a.add(ri.location);
                     } else {
                     	targets_g.add(ri.location);
                     }
                     switch (ri.type) {
                         case ARCHON:
                             ++e_archons;
                             break;
                         case CANNON:
                             break;
                         case CHANNELER:
                             break;
                         case SCOUT:
                             break;
                         case SOLDIER:
                             ++e_soldiers;
                             break;
                         case WORKER:
                             break;
                     }
                 } else {
                     allies.add(r);
                     a_nearby.add(r);
                     switch (ri.type) {
                         case ARCHON:
                             ++a_archons;
                             break;
                         case CANNON:
                             break;
                         case CHANNELER:
                             break;
                         case SCOUT:
                             break;
                         case SOLDIER:
                             ++a_soldiers;
                             break;
                         case WORKER:
                             ++a_workers;
                             break;
                     }
                 }
             }
         }
     }
 
     protected final void transfer() throws GameActionException {
         if (rc.getEnergonLevel() < rc.getMaxEnergonLevel() / 3) {
             return;
         }
 
         double min = Double.MAX_VALUE;
         Robot minr = null;
         RobotInfo minri = null;
 
         for (Robot r : a_nearby) {
             RobotInfo ri = r_info.get(r);
             if(state == State.DEFENCE && ri.type == RobotType.WORKER)//if defence => dont charge workers
             	continue;
             if (ri.location.isAdjacentTo(rc.getLocation()) || ri.location.equals(rc.getLocation())) {
                 if (min > ri.energonLevel + ri.energonReserve) {
                     min = ri.energonLevel + ri.energonReserve;
                     minr = r;
                     minri = ri;
                 }
             }
         }
         if (minr != null) {
             double e = GameConstants.ENERGON_RESERVE_SIZE - minri.energonReserve;
             e = Math.min(e, rc.getEnergonLevel() / 2);
             if (e > GameConstants.ENERGON_RESERVE_SIZE / 2 
             	&& rc.senseGroundRobotAtLocation(minri.location) != null)
             {
                 rc.transferEnergon(e, minri.location, minr.getRobotLevel());
             }
         }
     }
 
     protected void wander(int chance, int all) throws GameActionException {
         Direction d = rc.getDirection();
 
         int r = rand.nextInt(all);
 
         if (rc.canMove(d)) {
             if (r < chance) {
                 do {
                     d = d.rotateLeft();
                 } while (! rc.canMove(d));
             } else if (r > all - chance - 1) {
                 do {
                     d = d.rotateRight();
                 } while (! rc.canMove(d));
             }
         } else if (r < all/2) {
             do {
                 d = d.rotateLeft();
             } while (! rc.canMove(d));
         } else {
             do {
                 d = d.rotateRight();
             } while (! rc.canMove(d));
         }
 
         if (! rc.getDirection().equals(d)) {
             rc.setDirection(d);
         } else {
             rc.moveForward();
         }
     }
 
     protected MoveState moveToAdjacent(MapLocation l) throws GameActionException {
         if (rc.getLocation().isAdjacentTo(l)) {
             return MoveState.DONE;
         }
         if (! rc.getLocation().equals(l)) {
             if (! rc.getDirection().equals(rc.getLocation().directionTo(l))) {
                 rc.setDirection(rc.getLocation().directionTo(l));
                 return MoveState.MOVING;
             }
         } else {
             Direction d = rc.getDirection();
             while (! rc.canMove(d)) {
                 d = d.rotateRight();
                 if (rc.getDirection() == d) {
                     return MoveState.BLOCKED;
                 }
             }
             if (! rc.getDirection().equals(d)) {
                 rc.setDirection(d);
                 return MoveState.MOVING;
             }
         }
 
         if (rc.canMove(rc.getDirection())) {
             rc.moveForward();
             return MoveState.MOVING;
         } else {
             return MoveState.BLOCKED;
         }
     }
     
     protected void attack(Position pos, HashSet<MapLocation> _targets) throws GameActionException {
         int dist = Integer.MAX_VALUE;
         MapLocation target = null;
         //find close target
         for (MapLocation l : _targets) {
             if (dist > l.distanceSquaredTo(rc.getLocation())) {
                 dist = l.distanceSquaredTo(rc.getLocation());
                 target = l;
             }
         }
 
         if (target == null) {
             wander(2, 10);
         } else if (rc.canAttackSquare(target)) {
             if (rc.isAttackActive()) {
                 return;
             }
             if (pos == Position.AIR) {
                 rc.attackAir(target);
             } else {
                 rc.attackGround(target);
             }
         } else if (rc.getLocation().directionTo(target) != rc.getDirection()) {
             rc.setDirection(rc.getLocation().directionTo(target));
         } else if (rc.canMove(rc.getDirection())) {
             rc.moveForward();
         }
     }
     
     //communication
     private void doSendTargets(Position pos) throws GameActionException {
     	Message m = new Message();
     	m.locations = new MapLocation[0];
     	m.ints = new int[2];
 		m.ints[0] = rc.getTeam().hashCode();
 		if(pos == Position.AIR) {
 			m.ints[1] = 0;
 			m.locations = targets_a.toArray(m.locations);
 		} else {
 			m.ints[1] = 1;
 			m.locations = targets_g.toArray(m.locations);
 		}
 		rc.broadcast(m);
 		rc.yield();
     }
     
     protected void sendTargets() throws GameActionException {
     	if(!targets_a.isEmpty())
     		doSendTargets(Position.AIR);
     	if(!targets_g.isEmpty())
     		doSendTargets(Position.GROUND);
 
 	}
     
     protected Position receiveTargets() throws GameActionException {
     	Message[] messages = rc.getAllMessages();
     	boolean air = false, ground = false;
     	if (messages.length == 0)
     		return Position.NONE;
     	targets_a.clear();
     	targets_g.clear();
 		for (Message m : messages) {
 			if(m.ints.length == 2 && m.ints[0] == rc.getTeam().hashCode()) {
 				for (MapLocation l : m.locations) {
 					if(m.ints[1] == 0) {
 						air = true;					
 						targets_a.add(l);
 					} else {
 						ground = true;
 						targets_g.add(l);
 					}
 				}
 			}
 		}
 		if(ground && !air) return Position.GROUND;
 		else if(!ground && air) return Position.AIR;
 		else return Position.BOTH;
     }
     
 }
