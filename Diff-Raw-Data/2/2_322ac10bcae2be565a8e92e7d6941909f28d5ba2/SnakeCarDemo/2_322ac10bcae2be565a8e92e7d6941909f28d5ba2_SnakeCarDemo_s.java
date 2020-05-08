 /**
  * Unified Simulator for Self-Reconfigurable Robots (USSR)
  * (C) University of Southern Denmark 2008
  * This software is distributed under the BSD open-source license.
  * For licensing see the file LICENCE.txt included in the root of the USSR distribution.
  */
 package ussr.samples.atron.simulations;
 
 import ussr.description.Robot;
 import ussr.description.geometry.RotationDescription;
 import ussr.description.geometry.VectorDescription;
 import ussr.description.setup.BoxDescription;
 import ussr.description.setup.WorldDescription;
 import ussr.model.Controller;
 import ussr.samples.ObstacleGenerator;
 import ussr.samples.atron.ATRON;
 
 /**
  * Extension of the {@link EightToCarSimulationJ} example to use snake and car modes of locomotion.
  * 
  * @author ups
  */
 public class SnakeCarDemo extends EightToCarSimulationJ {
 
     public static void main(String argv[]) {
        new CopyOfSnakeCarDemo().main();
     }
 
     protected void changeWorldHook(WorldDescription world) {
         world.setPlaneTexture(WorldDescription.WHITE_GRID_TEXTURE);
         world.setHasBackgroundScenery(false);
         world.setHeavyObstacles(true);
         ObstacleGenerator generator = new ObstacleGenerator();
         generator.setNumberOfCircleObstacles(54);
         generator.setNumberOfCircleLayers(1);
         generator.setCircleObstacleRadius(0.4f);
         generator.setObstacleY(-0.4f);
         generator.setObstacleIncY(0.15f);
         generator.setObstacleSize(0.015f);
         generator.obstacalize(ObstacleGenerator.ObstacleType.CIRCLE, world);
     }
     @Override
     protected Robot getRobot() {
         ATRON atron = new ATRON() {
             public Controller createController() {
                 return new CarStuffController();
             }
         };
         atron.setRubberRing();
         return atron;
     }
 
     public static final byte NO_CONNECTOR = 8;
     public static final byte ALL_MODULES = 8;
 
     protected class CarStuffController extends EightController {
 
         volatile private int state = 0;
         int snake_counter = 2;
 
         @Override
         public void activate_before_eight2car() {
             int id = this.getMyID();
             if(id==3) this.state = 1;
             while(true) {
                 super.yield();
                 if(state!=0) System.out.println("@ Pre-controller "+id+" in state "+state);
                 switch(state) {
                 case 1: /* module 3 */
                     this.disconnect(0);
                     this.disconnect(6);
                     nextState(2,0);
                     break;
                 case 2: /* module 0 */
                     this.rotate(1);
                     this.rotate(1);
                     nextState(3,6);
                     break; 
                 case 3: /* module 6 */
                     this.rotate(1);
                     this.rotate(1);
                     nextState(9,2);
                     //nextState(4,3);
                     break;
                 case 4: /* module 3 */ /* NOT USED */
                     for(int i=0; i<10; i++) {
                         this.rotateDegrees(30);
                         this.rotateDegrees(-30);
                         delay(1000);
                     }
                     nextState(5,6);
                     break;
                 case 5: /* module 6 */
                     this.rotate(-1);
                     this.rotate(-1);
                     nextState(6,0);
                     break;
                 case 6: /* module 0 */ 
                     this.rotate(-1);
                     this.rotate(-1);
                     nextState(7,3);
                     break; 
                 case 7: /* module 3 */
                     this.connect(0);
                     this.connect(6);
                     nextState(8,ALL_MODULES);
                     break;
                 case 8: /* all modules */
                     super.delay(3000);
                     return;
                 case 9: /* module 2 */
                     //this.rotate(1);
                     this.rotateContinuous(1);
                     this.delay(6000);
                     //this.centerStop();
                     if(this.snake_counter-->0) {
                         nextState(10,4);
                     } else {
                         this.centerStop();
                         super.home();
                         nextState(11,4);
                     }
                     break;
                 case 10: /* module 4 */
                     //this.rotate(-1);
                     this.rotateContinuous(-1);
                     this.delay(6000);
                     //this.centerStop();
                     nextState(9,2);
                     break;
                 case 11: /* module 4 */
                     this.centerStop();
                     super.home();
                     nextState(5,6);
                     break;
                 default:
                     state = 0;
                 }
             }
         }
 
         @Override
         public void activate_after_eight2car() {
             // First execute "getup" operation
             int id = this.getMyID();
             if(id==0) reportTilt();
             if(id==0) 
                 this.state = 1;
             else
                 this.state = 0;
             control: while(true) {
                 delay(10000);
                 super.yield();
                 if(state!=0) System.out.println("@ Post-controller "+id+" in state "+state);
                 switch(state) {
                 case 1: /* module 0 */
                     this.rotate(1);
                     this.rotate(1);
                     delay(20000);
                     reportTilt();
                     nextState(2,6);
                     break;
                 case 2: /* module 6 */
                     this.rotateDegrees(45);
                     nextState(3,0);
                     break;
                 case 3: /* module 0 */
                     for(int i=0; i<4; i++) {
                         this.rotateDegrees(20);
                         delay(1000);
                     }
                     nextState(4,1);
                     break;
                 case 4: /* module 1 */
                     this.rotateDegrees(20);
                     nextState(5,0);
                     break;
                 case 5: /* module 0 */
                     for(int i=0; i<5; i++) {
                         this.rotateDegrees(20);
                         delay(1000);
                     }
                     nextState(6,6);
                     break;
                 case 6: /* module 6 */
                     this.rotateDegrees(-45);
                     nextState(10,ALL_MODULES);
                     break;
                 case 10:
                     delay(5000);
                     break control;
                 default:
                     state = 0;
                 }
             }
             // Then start driving 
             switch(id) {
             case 5: this.rotateContinuous(1f); break;
             case 2: this.rotateContinuous(-1f); break;
             case 3: this.rotateContinuous(1f); break;
             case 4: this.rotateContinuous(-1f); break;
             case 0:
                 int i;
                 while(true) {
                     this.delay(1000);
                     reportTilt();
                 }
             }
             delay(500000);
             this.centerStop();
             while(true) this.yield();
         }
 
         private void reportTilt() {
             System.out.println("Tilt sensor in module 0: ["+this.getTiltX()+","+this.getTiltY()+","+this.getTiltZ()+"]");
         }
         
         private void nextState(int nextState, int module_id) {
             this.state = 0;
             this.sendNextState(nextState, NO_CONNECTOR, (byte)module_id, (byte)0);
             if(module_id==ALL_MODULES) this.state = nextState;
         }
 
         private void sendNextState(int nextState, byte omit, byte destination, byte seen) {
             seen |= 1<<this.getMyID();
             byte[] message = new byte[] { 0, (byte)(nextState), 42, destination, seen };
             for(byte c=0; c<8; c++)
                 if(c!=omit && super.isConnected(c)) super.sendMessage(message, (byte)4, c);
         }
 
         @Override
         protected boolean canHandleMessage(byte[] incoming, int messageSize, int channel) {
             if(incoming[2]==42) {
                 byte destination = incoming[3];
                 byte seen = incoming[4];
                 byte flag = (byte) (seen>>this.getMyID());
                 byte nextState = incoming[1];
                 if((flag&1)==1) return true;
                 if(destination==this.getMyID() || destination==ALL_MODULES) state = nextState;
                 if(destination!=this.getMyID() || destination==ALL_MODULES)
                     sendNextState(nextState,(byte)channel,destination,seen);
                 return true;
             }
             return false;
         }
     }
 }
