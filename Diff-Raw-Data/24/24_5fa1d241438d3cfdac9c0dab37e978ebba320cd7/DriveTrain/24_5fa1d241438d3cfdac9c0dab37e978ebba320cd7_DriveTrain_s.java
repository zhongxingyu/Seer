 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package storm.modules;
 import edu.wpi.first.wpilibj.*;
 import storm.interfaces.IDriveTrain;
 import storm.utility.Queue;
 
 /**
  *
  * @author Storm
  */
 public class DriveTrain implements IDriveTrain {
 
     public static final int STOP_QUEUE = 0;
 
     SpeedController leftMotor1, leftMotor2, rightMotor1, rightMotor2;
     RobotDrive highDrive;
     RobotDrive lowDrive;
     boolean highgear = true;
     Queue queue = new Queue();
     Encoder leftEncoder = new Encoder(1, 2);
    //Encoder rightEncoder = new Encoder(3, 2);
 
     public DriveTrain (int motorChannelL1,int motorChannelL2,int motorChannelR1,int motorChannelR2 ){
         leftMotor1 = new Victor(motorChannelL1);
         leftMotor2 = new Victor(motorChannelL2);
         rightMotor1 = new Victor(motorChannelR1);
         rightMotor2 = new Victor(motorChannelR2);
         highDrive = new RobotDrive(motorChannelL1, motorChannelR1);
         lowDrive = new RobotDrive(motorChannelL2, motorChannelR2);
 
     }
     
     public void drive(double leftSpeed, double rightSpeed) {
 
         if (highgear){
             highDrive.tankDrive(leftSpeed, rightSpeed);
             lowDrive.tankDrive(0.0, 0.0);
         }else{
             lowDrive.tankDrive(leftSpeed, rightSpeed);
             highDrive.tankDrive(0.0, 0.0);
         }
 
 
         /*
         DEPRECATED
         leftMotor1.set(leftSpeed);
         leftMotor2.set(leftSpeed);
         rightMotor1.set(rightSpeed);
         rightMotor2.set(rightSpeed);
         */
     }
 
     public void addToQueue(int type, double speed, double distance) {
         queue.add(type, speed, distance);
     }
 
     public void resetQueue() {
         queue.clear();
     }
 
     public void executeQueue() {
         switch ((int)queue.getType()) {
             case 1:
                 //Forward/Backward
                 drive(queue.getSpeed(), queue.getSpeed());
                 break;
             case 2:
                 //Left/Right
                 drive(queue.getSpeed(), -queue.getSpeed());
                 break;
             case 3:
                 //Hook Left
                 drive(0, queue.getSpeed());
                 break;
             case 4:
                 //Hook Right
                 drive(queue.getSpeed(), 0);
                 break;
             default:
                 System.out.println("Error: Invalid Type");
                 drive(0,0);
         }
     }
 
     public boolean isQueueFinished() {
         return queue.isRunning();
     }
 
     public void resetDistance() {
        leftEncoder.reset();
     }
 
     public double getDistance() {
         return leftEncoder.getDistance();
     }
 
     public void switchGear() {
         if (highgear) highgear = false;
         else highgear = true;
     }
 
     public void setHighGear() {
         highgear = true;
     }
 
     public void setLowGear() {
         highgear = false;
     }
     
 }
