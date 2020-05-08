 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package storm.logic;
 
import storm.RobotState;
 import storm.interfaces.IDriveTrain;
 import storm.interfaces.IRobotLogic;
import storm.modules.DriveTrain;
 import storm.modules.TargetTracker;
 import storm.utility.Print;
 
 /**
  *
  * @author joe
  */
 public class TrackerTest implements IRobotLogic {
    Print print = Print.getInstance();
    IDriveTrain drive = new DriveTrain(RobotState.PORT_MOTOR_DRIVE_LEFT,RobotState.PORT_MOTOR_DRIVE_RIGHT);
    TargetTracker tracker = new TargetTracker(drive,RobotState.PORT_GYRO_ROBOT_ROTATION);
    
     public void doInit() {
         tracker.startTracking();
     }
 
     public void doContinuous() {
         boolean aimed = tracker.isAimed();
         print.println(aimed ? "Aimed" : "Not Aimed");
         if(aimed)
             print.println("Z: " + tracker.getDistance());
     }
 
     public void doPeriodic() {
     }
 
     public void doEnd() {
         tracker.stopTracking();
     }
 }
