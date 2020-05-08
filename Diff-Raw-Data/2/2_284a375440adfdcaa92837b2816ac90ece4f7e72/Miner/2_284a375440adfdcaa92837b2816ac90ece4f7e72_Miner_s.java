 package archpirates.castes;
 
 import archpirates.modules.*;
 import battlecode.common.*;
 
 public class Miner extends Caste {
     private static enum State {
         START,
         BUILD,
         FINISH,
         FAIL,
         YIELD
     }
     private State state;
 
     private final Builder builder;
     int c;
 
     public Miner(RobotProperties rp){
         super(rp);
 
         state = State.YIELD;
         c = 0;
         builder = new Builder(rp);
     }
 
     public void SM() {
         while(true) {
             try {
                 switch(state) {
                     case START:
                         start();
                         break;
                     case BUILD:
                         build();
                         break;
                     case FINISH:
                         if (++c == 1)
                             state = State.YIELD;
                         else
                             state = State.START;
                         break;
                     case FAIL:
                         state = State.START;
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
     private void start() {
        switch(builder.startBuild(Chassis.LIGHT, ComponentType.CONSTRUCTOR, ComponentType.SIGHT)) {
             case ACTIVE:
             case WAITING:
                 state = State.BUILD;
                 break;
             case FAIL:
             default:
                 state = State.FAIL;
                 break;
         }
     }
     private void build() {
         switch (builder.doBuild()) {
             case ACTIVE:
             case WAITING:
                 break;
             case DONE:
                 state = State.FINISH;
                 break;
             case FAIL:
             default:
                 state = State.FAIL;
                 break;
         }
     }
 
 }
