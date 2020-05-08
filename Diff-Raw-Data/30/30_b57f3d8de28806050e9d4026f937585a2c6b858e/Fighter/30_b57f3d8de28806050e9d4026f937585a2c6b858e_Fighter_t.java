 package archpirates.castes;
 
 import archpirates.modules.*;
 import battlecode.common.*;
 
 public class Fighter extends Caste {
     private static enum State {
         ATTACK,
         DEFEND,
         WANDER,
         INIT,
         YIELD,
         GOTO
     }
     private State state;
 
     private final Attacker attacker;
     private final int bitmask;
 
     public Fighter(RobotProperties rp){
         super(rp);
 
         state = State.INIT;
 
         attacker = new Attacker(rp);
 
         bitmask = ( Communicator.ATTACK | Communicator.DEFEND );
     }
     
 
     @SuppressWarnings("fallthrough")
     public void SM() {
         MapLocation [] path = null;
         MapLocation location = null;
         while(true) {
             try {
                 switch(state) {
                     case INIT:
                         // Wait until we stop firing and the gun is ready
                         nav.setDestination(new MapLocation(0,0));
                         nav.bugNavigate();
                         state = State.WANDER;
                         break;
                     case WANDER:
                        //if ((path = com.getCommand(bitmask)) != null) {
                        //    nav.setDestination(path[path.length-1], 4);
                        //    state = State.GOTO;
                        //} else ...
                        if ((location = attacker.autoFire()) != null) {
                             nav.setDestination(location, 4);
                             state = State.ATTACK;
                         }
                         nav.bugNavigate();
                         break;
                     case DEFEND:
                         attacker.autoFire();
                         break;
                     case GOTO:
                         attacker.autoFire();
                         if (path != null)
                             com.sendCommand(Communicator.ATTACK, path);
                         if (!nav.bugNavigate())
                             state = State.ATTACK;
                         break;
                     case ATTACK:
                        //if (path != null)
                        //    com.sendCommand(Communicator.ATTACK, path);
                        
                        if ((location = attacker.autoFire()) != null) {
                             nav.setDestination(location, 4);
                            nav.bugNavigate();
                        } else {
                            state = State.INIT;
                        }
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
 }
