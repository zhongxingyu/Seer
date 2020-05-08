 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package storm.logic;
 
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.RobotDrive;
 import storm.interfaces.IDriveTrain;
 import storm.interfaces.IRobotLogic;
 import storm.modules.TargetTracker;
 import storm.utility.Print;
 
 /**
  *
  * @author joe
  */
 public class TrackerTest implements IRobotLogic {
    TargetTracker tracker;
    Print print;

    public TrackerTest(IDriveTrain drive,Gyro gyro) {
        tracker = new TargetTracker(drive,gyro);
    }

     public void doInit() {
         tracker.startTracking();
     }
 
     public void doContinuous() {
        print = Print.getInstance();
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
