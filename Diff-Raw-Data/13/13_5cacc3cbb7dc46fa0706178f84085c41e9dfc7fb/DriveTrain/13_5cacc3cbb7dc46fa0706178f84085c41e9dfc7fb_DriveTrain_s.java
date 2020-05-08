 
 package edu.wpi.first.wpilibj.templates.subsystems;
 
 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.templates.RobotMap;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.templates.commands.JoystickDrive;
 import edu.wpi.first.wpilibj.Encoder;
 /**
  *contains methods for controlling the DriveTrain
  * @author Jessie and Emma
  */
 public class DriveTrain extends Subsystem {
     // Put methods for controlling this subsystem
     // here. Call these from Commands.
 
     Jaguar leftFront = new Jaguar(RobotMap.leftFront);
     Jaguar leftRear = new Jaguar(RobotMap.leftRear);
     Jaguar rightFront = new Jaguar(RobotMap.rightFront);
     Jaguar rightRear = new Jaguar(RobotMap.rightRear);
     Encoder leftEncoder = new Encoder(RobotMap.leftEncoderA, RobotMap.leftEncoderB);
     Encoder rightEncoder = new Encoder(RobotMap.rightEncoderA, RobotMap.rightEncoderB);
     //which motors we are using to drive the robot
     public RobotDrive drive = new RobotDrive(leftFront, leftRear, rightFront, rightRear);
     
      
     
     public DriveTrain(){
         double distPerPulse = 0.101;
         
         leftEncoder.start();
         rightEncoder.start();
         
         leftEncoder.setDistancePerPulse(distPerPulse);
         rightEncoder.setDistancePerPulse(distPerPulse);
     }
     /**
      * Default: DriveTrain subsystem controlled by inputs from the joysticks if no other 
      * commands are using the subsystem
      */
     public void initDefaultCommand() {
         // Set the default command for a subsystem here.
         //setDefaultCommand(new MySpecialCommand());
         setDefaultCommand(new JoystickDrive());
         
     }
     public void updateStatus(){
 
     }
     /**
      * sets the speed of the left motor to a double between -1 and 1
      * @param speed the speed of the left motor
      */
     public void setSpeed(double leftSpeed, double rightSpeed){
        drive.setLeftRightMotorOutputs(leftSpeed, rightSpeed);
        System.out.println("the left speed is:" + leftSpeed);
        System.out.println("the right speed is;" + rightSpeed);
     }
     
     /**
      * gives the value of the right encoder
      * @return the number of ticks in the right encoder
      */
     public double getRightEncoder(){
         return rightEncoder.getDistance();
     }
     /**
      * gives the value of the left encoder
      * @return the number of ticks in the left encoder
      */
     public double getLeftEncoder(){
         return leftEncoder.getDistance();
     }
     
 }
 
