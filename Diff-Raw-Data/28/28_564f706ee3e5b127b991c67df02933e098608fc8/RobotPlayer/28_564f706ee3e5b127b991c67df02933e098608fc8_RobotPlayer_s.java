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
         while (true)
         {
             try
             {
                myRC.setIndicatorString(0, myRC.getChassis().toString());
                String components = "";
                for (ComponentController comp : myRC.components())
                {
                    components += comp.type().toString() + " ";
                }
                myRC.setIndicatorString(1, components);
                myRC.setIndicatorString(2, String.valueOf(Clock.getBytecodeNum()));
                myRC.yield();                
             }
             catch (Exception e)
             {
                 System.err.println("Exception:");
                 e.printStackTrace();
             }
         }
     }
 }
