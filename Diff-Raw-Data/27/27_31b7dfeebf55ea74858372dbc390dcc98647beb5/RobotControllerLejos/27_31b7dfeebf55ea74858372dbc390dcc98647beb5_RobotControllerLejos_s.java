 
 package edu.msoe.se2800.h4;
 
 import java.awt.Point;
 
 import lejos.nxt.Motor;
 import lejos.nxt.remote.RemoteMotor;
 import lejos.robotics.localization.PoseProvider;
 import lejos.robotics.navigation.DifferentialPilot;
 import lejos.robotics.navigation.Navigator;
 import lejos.robotics.navigation.Pose;
 import lejos.robotics.navigation.Waypoint;
 import lejos.robotics.pathfinding.Path;
 
 /**
  * Provides the basic functions for controlling the lejos robot.
  *
  * @author koenigj
  */
 public class RobotControllerLejos implements IRobotController {
 
     /**
      * The pilot class is set to 2 cm wheel diameter and 7 cm between the wheels
      */
     private DifferentialPilot pilot;
     /**
      * The Navigator is used to follow a path or by point.
      */
     private static Navigator nav;
     /**
      * The path to be followed by the robot.
      */
     private static Path path;
 
     private boolean stopRequested = false;
 
     /**
      * The constructor instantiates the pilot and navigator. Single step is false.
      */
     public RobotControllerLejos() {
         path = new Path();
         pilot = new DifferentialPilot(2, 7, Motor.A, Motor.B);
         nav = new Navigator(pilot);
         nav.singleStep(false);
     }
 
     /**
      * Set the path to be followed.
      */
     @Override
     public void setPath(Path path) {
         this.path = path;
         nav.setPath(path);
         System.out.println("here");
     }
 
     /**
      * Follow the route
      */
     @Override
     public void followRoute() {
         this.stopRequested = false;
         Thread t = new Thread(new Runnable() {
             @Override
             public void run() {
                 if (!stopRequested) {
                     forward();
                     reverse();
                     nav.waitForStop();
                     // This has to be reset since path is reset in navigator after the followPath is called
                     for (int i = 0; i < path.size(); i++) {
                         nav.addWaypoint(path.get(i));
                     }
                     System.out.println(nav.getPath().size());
                     System.out.println(path.size());
                    // test a loop
                    this.run();
                 }
             }
         });
         t.start();
     }
 
     @Override
     public void addWaypoint(Waypoint wp) {
         nav.addWaypoint(wp);
         path.add(wp);
     }
 
     @Override
     public Path getPath() {
         return path;
     }
 
     public static void forward() {
         nav.followPath();
 
     }
 
     public static void reverse() {
         for (int i = path.size() - 2; i > -1; i--) {
             nav.goTo(path.get(i));
 
         }
         nav.goTo(0, 0);
     }
 
     @Override
     public void goToImmediate(Waypoint wp) {
         if (path != null) {
             path.clear();
             nav.clearPath();
         }
         nav.goTo(wp);
         nav.waitForStop();
         path.clear();
         nav.clearPath();
     }
 
     @Override
     public void singleStep(boolean setting) {
         nav.singleStep(setting);
     }
 
     @Override
     public Waypoint getLocation() {
         PoseProvider provider = nav.getPoseProvider();
         Pose pose = provider.getPose();
         return (Waypoint) pose.getLocation();
 
     }
 
     @Override
     public void setVelocity(double speed) {
         pilot.setTravelSpeed(speed);
     }
 
     @Override
     public boolean isMoving() {
         return pilot.isMoving();
     }
 
     @Override
     public void setReverse(boolean isReverse) {
         pilot = new DifferentialPilot(2, 7, Motor.A, Motor.B, isReverse);
         nav = new Navigator(pilot);
     }
 
 
     @Override
     public void stop() {
         stopRequested = true;
         nav.singleStep(true);


     }
 
     @Override
     public void stopImmediate() {
         stopRequested = true;
         nav.stop();
     }
 
     @Override
     public double getVelocity() {
         return pilot.getTravelSpeed();
     }
 }
