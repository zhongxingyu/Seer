 package org.usfirst.frc1148.modules;
 
import edu.wpi.first.wpilibj.Timer;
 import org.usfirst.frc1148.data.MoveData;
 import org.usfirst.frc1148.interfaces.RobotModule;
 
 public class AutonomousModule implements RobotModule {
 
     RobotDriver driver;
     DrawbridgeModule drawbridge;
     int state = 0;
     Timer timer;
     Timer secondTimer;
     int wiggle = 0;
     boolean autonomousEnabled = true;
     AutoDriveModule autoDrive;
 
     public AutonomousModule(RobotDriver drive, DrawbridgeModule box, AutoDriveModule autoDrive) {
         driver = drive;
         drawbridge = box;
         this.autoDrive = autoDrive;
     }
 
     public void initModule() {
         timer = new Timer();
         secondTimer = new Timer();
 
     }
 
     public void activateModule() {
         //drawbridge.Close();
         //	driver.Relative();
     }
 //
 
     public void deactivateModule() {
     }
 
     public void updateTick(int mode) {
 
         if (mode == 1 && autonomousEnabled) {
             MoveData moveData = driver.getMoveData();
             drawbridge.Enable();
             driver.NotRelative();
             switch (state) {
                 case 0:
                     timer.reset();
                     timer.start();
                     state++;
                     System.out.println("Moving to state " + state);
                     break;
                 case 1:
                     moveData.angle = 90;
                     moveData.speed = .5;
                     autoDrive.OrientTo(360-45);
                     drawbridge.Close();
                    if (timer.get() > 5) {
                         timer.stop();
                         timer.reset();
                         moveData.angle = 270;
                         moveData.speed = 0;
                         //stop auto-orienting incase door is damaged
                         autoDrive.Disable();
                         state++; 
                         System.out.println("Moving to state " + state);
                     }
                     break;
                 case 2:
                     drawbridge.Open();
                     timer.reset();
                     timer.start();
                     state++;
                     System.out.println("Moving to state " + state);
                     break;
                 case 3:
                     moveData.speed = 0;
                     moveData.angle = 270;
                    if (timer.get() > 2) {
                         state++;
                         System.out.println("Moving to state " + state);
                         timer.stop();
                         timer.reset();
                         timer.start();
                     }
                     break;
                 case 4:
                     moveData.angle = 270;
                     moveData.speed = 1;
                    if (timer.get() > 1) {
                         timer.stop();
                         timer.reset();
                         state++;
                         System.out.println("Moving to state " + state);
                     }
                     break;
                 case 5:
                     moveData.angle = 0;
                     moveData.speed = 0;
                     drawbridge.Close();
                     autoDrive.Disable();
                     driver.Relative();
                     state++;
                     System.out.println("Moving to state " + state);
                     break;
                 default:
                     System.out.println("Autonomous finished.");
                     break;
             }
         }
     }
 }
