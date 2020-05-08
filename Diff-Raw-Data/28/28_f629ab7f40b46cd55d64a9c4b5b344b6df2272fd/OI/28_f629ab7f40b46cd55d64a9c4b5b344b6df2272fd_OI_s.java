 package edu.wpi.first.wpilibj.technobots;
 
 import com.sun.squawk.util.MathUtils;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.KinectStick;
 import edu.wpi.first.wpilibj.buttons.Button;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 import edu.wpi.first.wpilibj.technobots.commands.*;
 
 /**
  * This class is the glue that binds the controls on the physical operator
  * interface to the commands and command groups that allow control of the robot.
  */
 public class OI {
 
     Joystick xbox;
     Button button1, button2, button3, button5, button6, button8;
     ReleaseBall releaseBall;
     KinectStick leftArm;
     KinectStick rightArm;
 
     public OI() {
 
         xbox = new Joystick(1);
         button1 = new JoystickButton(xbox, 1);
         button2 = new JoystickButton(xbox, 2);
         button3 = new JoystickButton(xbox, 3);
         button5 = new JoystickButton(xbox, 5);
         button6 = new JoystickButton(xbox, 6);
         button8 = new JoystickButton(xbox, 8);
 
         button1.whileHeld(new SetElbowSetPoint(0.04));
         button5.whileHeld(new ShooterOn(-1));
         button2.whileHeld(new ShooterOn(-.6));
         button3.whileHeld(new ShooterOn(-.2));
         button6.whileHeld(new SweeperOn());
 
         button8.whileHeld(new ReleaseBall());
 
         leftArm = new KinectStick(1);
         rightArm = new KinectStick(2);
 
 
     }
 
     public double getMagnitude() {
 
        double y = Buffer(xbox.getY());
        double x = Buffer(xbox.getX());

        double magnitude = Math.sqrt(MathUtils.pow(x, 2) + MathUtils.pow(y, 2));

        return magnitude;
     }
 
     public double getDirection() {
 
        double y = Buffer(xbox.getY());
        double x = Buffer(xbox.getX());

        double direction = Math.toDegrees(MathUtils.atan2(x, y));

        if (direction < 180) {

            direction = 0;
        }
        return direction;
     }
 
     public double getRotation() {
 
         return Buffer(xbox.getRawAxis(4));
     }
 
     public double KinectRotation() {
         return rightArm.getX();
     }
 
     public double KinectDirection() {
 
         double y = Buffer(leftArm.getY());
         double x = Buffer(leftArm.getX());
 
         double direction = Math.toDegrees(MathUtils.atan2(x, y));
 
         if (direction < 180) {
 
             direction = 0;
         }
         return direction;
     }
 
     public double KinectMagnitude() {
         double y = Buffer(leftArm.getY());
         double x = Buffer(leftArm.getX());
 
         double magnitude = Math.sqrt(MathUtils.pow(x, 2) + MathUtils.pow(y, 2));
 
         return magnitude;
     }
 
 
 
     private double Buffer(double n) {
 
         double buffers = 0.3;
 
         if (Math.abs(n) < buffers) {
 
             n = 0.000000000000000;
         }
 
         return n;
     }
 }
