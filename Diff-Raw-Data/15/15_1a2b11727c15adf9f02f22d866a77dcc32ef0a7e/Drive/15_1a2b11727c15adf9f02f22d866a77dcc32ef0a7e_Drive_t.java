 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Robot.FoximusPrime.UltimateAscent.RobotParts;
 
 import Robot.FoximusPrime.UltimateAscent.UltimateAscentRobot;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import edu.wpi.first.wpilibj.Relay;
 /**
  *
  * @author edward
  */
 public class Drive extends UltimateAscentRobotPart {
     
     private Jaguar        leftMotor;
     private Jaguar        rightMotor;
     private RobotDrive    drive;
     //private Relay         compressor;
     
     private double        KP = .05;
     private double        AUTOMAX = .3;
     private double        kYAW    = 0;
     
 
     public Drive(UltimateAscentRobot robot){
         super(robot);
         leftMotor  = new Jaguar(1); //PWM 1
         rightMotor = new Jaguar(4); //PWM 2
         drive      = new RobotDrive(leftMotor,rightMotor);
         //compressor = new Relay(1);
         //compressor.setDirection(Relay.Direction.kForward);
         SmartDashboard.putNumber("YAW", kYAW);
     }
     
     public void updateTeleop(){
         /*if(robot.getSensors().getLeftJoystick().getRawButton(4))
             compressor.set(Relay.Value.kOn);
         else
             compressor.set(Relay.Value.kOff); */
     
         kYAW = SmartDashboard.getNumber("YAW");
         if (robot.getSensors().getLeftJoystick().getRawButton(3))
             adjustAim(robot.getImageProcesser().getOffCenter());
         else
            drive.arcadeDrive(-robot.getSensors().getLeftJoystick().getX(), -robot.getSensors().getLeftJoystick().getY());
             //drive.arcadeDrive(robot.getSensors().getLeftJoystick().getY(), robot.getSensors().getLeftJoystick().getX());
         
 
     }
     
     public void updateAuto(){
         double power = -robot.getSensors().getGyro().getAngle() * KP;
         drive.arcadeDrive(0, power > AUTOMAX?power:AUTOMAX);
     }
     public void adjustAim(double offsets){
     if (offsets >= 3 || offsets <= 3)
         drive.arcadeDrive(0, offsets * .05);
     }
 }
