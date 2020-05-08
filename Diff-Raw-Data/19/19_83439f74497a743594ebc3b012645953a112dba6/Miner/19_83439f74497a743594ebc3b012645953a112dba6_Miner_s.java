 package archpirates.castes;
 
 import archpirates.modules.*;
 import battlecode.common.*;
 
 public class Miner extends Caste {
     private static enum State {
         START,
         IDLE,
         BUILD,
         YIELD
     }
 
     private final Builder builder;
     private final MapLocation myLoc; // Buildings can't move.
 
     private boolean scout;
     private State state;
 
     public Miner(RobotProperties rp){
         super(rp);
 
         state = State.START;
         builder = new Builder(rp);
         myLoc = myRC.getLocation();
     }
 
     public void SM() {
         while(true) {
             try {
                 switch(state) {
                     case IDLE:
                         idle();
                         break;
                     case BUILD:
                         build();
                         break;
                     case YIELD:
                     default:
                         yield();
                         break;
                 }
             } catch (Exception e) {
                 System.out.println("caught exception:");
                 e.printStackTrace();
             }
             System.out.println(Clock.getBytecodeNum());
             myRC.yield();
         }
     }
 
     private void idle() throws GameActionException {
        GameObject obj = myRP.sensor.senseObjectAtLocation(loc, RobotLevel.IN_AIR);
         if(obj != null && obj.getTeam() == myRP.myTeam && !myRP.sensor.senseRobotInfo((Robot)obj).on) {
            builder.startBuild(true, loc, RobotLevel.IN_AIR, ComponentType.SIGHT, ComponentType.CONSTRUCTOR);
             builder.doBuild();
             state = State.BUILD;
         }
     }
 
     private void build_fighter() {
         Direction dir = Direction.NORTH;
        Location loc = null;
         for (int i = 8; --i > 0;) {
            if (motor.canMove(loc = myLoc.add(dir = Direction.rotateLeft())))
                 break;
         }
         if (loc == null)
            return
        switch(builder.startBuild(true, Chassis.LIGHT, ComponentType.SMG, ComponentType.ANTENNA, ComponentType.RADAR)) {
             case ACTIVE:
             case WAITING:
                 state = State.BUILD;
                 break;
             case FAIL:
             default:
                 state = State.IDLE;
                 break;
         }
     }
 
     private void build() throws GameActionException {
         switch (builder.doBuild()) {
             case DONE:
             case FAIL:
                 state = State.IDLE;
                 break;
             default:
                 break;
         }
     }
 }
