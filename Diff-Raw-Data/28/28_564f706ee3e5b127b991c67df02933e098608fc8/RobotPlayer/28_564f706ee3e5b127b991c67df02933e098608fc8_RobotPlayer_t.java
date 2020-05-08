 package team158;
 
 import battlecode.common.*;
 import static battlecode.common.GameConstants.*;
 
 @SuppressWarnings("unused")
 public class RobotPlayer implements Runnable
 {
 
     private final RobotController myRC;
 
     public RobotPlayer(RobotController rc)
     {
         myRC = rc;
     }
 
     public void run()
     {
        // Loop indefinitely
         while (true)
         {
            // Prevent errors from crashing program
             try
             {
                // Debug Information
                /*
                 * myRC.setIndicatorString(0, myRC.getChassis().toString());
                 * String components = ""; for (ComponentController comp :
                 * myRC.components()) { components += comp.type().toString() +
                 * " "; } myRC.setIndicatorString(1, components);
                 * myRC.setIndicatorString(2,
                 * String.valueOf(Clock.getBytecodeNum()));
                 */   
                myRC.addMatchObservation("I am stupid.");
                myRC.yield();     
             }
             catch (Exception e)
             {
                // Show any errors in debug info 
                myRC.setIndicatorString(0, e.getMessage());
                // Print errors to std err
                 System.err.println("Exception:");
                 e.printStackTrace();
             }
         }
     }
 }
