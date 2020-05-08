 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package storm.logic;
 
 import edu.wpi.first.wpilibj.AnalogChannel;
 import edu.wpi.first.wpilibj.Preferences;
 import storm.RobotState;
 import storm.interfaces.IBallCollector;
 import storm.interfaces.IBridgeManipulator;
 import storm.interfaces.IDriveTrain;
 import storm.interfaces.IRobotLogic;
 import storm.interfaces.IShooter;
 import storm.interfaces.ITargetTracker;
 import storm.utility.Print;
 import storm.utility.Queue;
 
 /**
  *
  * @author Awesome
  */
 public class Hybrid implements IRobotLogic {
 
     IDriveTrain driveTrain;
     IShooter shooter;
     ITargetTracker targetTracker;
     IBallCollector ballCollector;
     IBridgeManipulator manipulator;
 
     AnalogChannel autoType;
 
     Queue Q = new Queue();
 
     boolean isLoading = false;
     boolean isManipulating = false;
 
     double endTime = 0;
     double startTime = 0;
 
     int autoNum = 0;
 
     public Hybrid() {
 
         driveTrain = RobotState.driveTrain;
         shooter = RobotState.shooter;
         targetTracker = RobotState.targetTracker;
         ballCollector = RobotState.ballCollector;
         manipulator = RobotState.bridgeManipulator;
 
         autoType = new AnalogChannel(RobotState.PORT_SWITCH_HYBRID_TYPE);
 
     }
 
     public void doInit() {
 
         Q.clear();
         endTime = 0;
         startTime = System.currentTimeMillis();
         autoNum = 0;
 	driveTrain.setLowGear();
         isManipulating = false;
         isLoading = false;
 
         //1 - Straight     \\
         //2 - Left         \\
         //3 - Manipulate   \\
         //4 - Shoot        \\
         //5 - Start Load   \\
         //6 - Stop Load    \\
         //7 - Wait         \\
 
         //System.currentTimeMillis();
 
         autoNum = (int) Math.floor(autoType.getVoltage() + 0.5) + 1;
 
         switch (autoNum) {//autoType.getValue()) {
             case 1: //Super Auto Mode
 
                 Q.add(4, 0, 0); //Shoot
                 Q.add(1, 60, -.5); //Move back
                 Q.add(5, 0, 0); //Start Loading
                 Q.add(7, 3, 0); //Wait
                 Q.add(3, 0, 0); //Manipulate
 
                 break;
             case 2: //Quick ninja
 
                 Q.add(1, 60, -.5);
                 Q.add(5, 25, 1);
                 //Possibly do a shoot sequence
 
                 break;
             case 3: //Shoot
                 
                 Q.add(4, 0, 0); //Shoot
                 Q.add(7, 2, 0); //Wait
                Q.add(4, 0, 0); //Start Loading
 
                 break;
             case 4: //Flee in terror to the left
 
                 Q.add(2, 50, .8);
                 Q.add(1, 50, .5);
 
                 break;
             case 5: //Flee in terror to the right
 
                 Q.add(2, 50, -.8);
                 Q.add(1, 50, .5);
 
                 break;
             case 6: //Sitting duck
                 break;
             default:
                 break;
         }
 
         Q.start();
 
     }
 
     public void doContinuous() {
         Print.getInstance().setLine(0, "Queue Part: " + Q.getPart());
         Print.getInstance().setLine(2, "End Time: " + endTime);
         Print.getInstance().setLine(3, "Current Time: " + (System.currentTimeMillis()-startTime));
         shooter.doShoot();
         //Eat a muffin.  please :)
     }
 
     public void doPeriodic() {
 
         if (Q.isRunning()) runQueue();
 
         if (Q.getType() == 1 || Q.getType() == 2) {
             if (Math.abs(driveTrain.getDistance()) >= Q.getDistance()) {
                 Q.next();
             }
         } else if (Q.getType() == 3) {
             //Manipulate
             Q.next();
         } else if (Q.getType() == 4) {
             //if (!shooter.isShooting()) Q.next();
             Q.next();
         } else if (Q.getType() == 5 || Q.getType() == 6) {
             Q.next();
         } else if (Q.getType() == 7) {
             if ((System.currentTimeMillis()-startTime) >= endTime) {
                 endTime = 0;
                 Q.next();
             }
         }
 
         //if (isLoading) ballCollector.start(IBallCollector.DIRECTION_UP);
         if (isManipulating) manipulator.lower();
 
     }
 
     public void doEnd() {
         shooter.endShoot();
         Q.clear();
     }
 
     public void runQueue() {
 
         switch ((int)Q.getType()) {
             case 1: //Forward/Backward
                 driveTrain.drive(Q.getSpeed(), Q.getSpeed());
                 break;
             case 2: //Turning
                 driveTrain.drive(Q.getSpeed(), -Q.getSpeed());
                 break;
             case 3: //Manipulate
                 isManipulating = true;
                 break;
             case 4: //Shoot
                 shooter.setContinuousShoot(true);
                 if (!shooter.isShooting()) shooter.startShoot(Preferences.getInstance().getDouble("Distance", 1.0));
                 break;
             case 5:
                 isLoading = true;
                 ballCollector.start(IBallCollector.DIRECTION_UP);
                 break;
             case 6:
                 isLoading = false;
                 ballCollector.stop();
                 break;
             case 7:
                 if (endTime == 0) {
                     endTime = System.currentTimeMillis() - startTime + Q.getDistance() * 1000;
                 }
                 break;
             default:
                 break;
         }
 
     }
 
 }
