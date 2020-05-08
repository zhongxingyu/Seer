 /**
  * 
  */
 package ussr.samples.atron;
 
 import ussr.model.Controller;
 import ussr.robotbuildingblocks.Robot;
 
 /**
  * @author ups
  *
  * TODO Write a nice and user-friendly comment here
  * 
  */
public class ATRONSimulationNative1 extends ATRONCarSimulation {
     public static void main(String argv[]) {
         new ATRONSimulationNative1().main();
     }
     
     @Override
     protected Robot getRobot() {
         return new ATRON() {
             public Controller createController() {
                 return new ATRONNativeController("controller1");
             }
         };
     }
 }
