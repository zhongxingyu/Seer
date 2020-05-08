 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.Gyro;
 import edu.wpi.first.wpilibj.RobotDrive;
 
 /**
  * This class adds swerve drive functionality
  * to the RobotDrive class.
  * 
  * Wheel 1: right forward wheel (1st quadrant)
  * Wheel 2: left forward wheel (2nd quadrant)
  * Wheel 3: left rear wheel (3rd quadrant)
  * Wheel 4: right rear wheel (4th quadrant)
  * 
  * Explanation of Relative Steering Mode:
  * The objective of relative steering mode is to use
  * the angle read from the gyro to maintain the robot's
  * coordinate system relative to the driver. The effect
  * is that the robot will travel in the same direction,
  * given an input vector from the gamepad, regardless of
  * its current heading. Relative steering is accomplished
  * by rotating the input vector by -gyro degrees, essentially
  * "unrotating" the robot before running the swerve algorithm.
  * 
  * Credit for the swerve drive algorithm goes to Ether on ChiefDelphi.
  * @author Connor
  */
 public class RobotDriveSwerve extends RobotDrive{
 
     //objects
     private SwerveWheel wheel1,wheel2,wheel3,wheel4;
     private Gyro relstrGyro;
     
     //constants
     
     /*
      * must verify measurements on robot:
      * used to determine directions of rotational vectors.
      */
     private final double halfRobotWidthIn = 18;
     private final double halfRobotLengthIn = 30;
     private final double radiusLength = 
             Math.sqrt(halfRobotWidthIn * halfRobotWidthIn
             + halfRobotLengthIn * halfRobotLengthIn);
     
     //normalize the vector (divide by its length)
     private final double hw = halfRobotWidthIn/radiusLength;
     private final double hl = halfRobotLengthIn/radiusLength;
     
     /*
      * rotational vectors - notation:
      * r(rotation)x(x component)1(first wheel)
      */
     private final double rx1 = -hl;
     private final double ry1 = hw;
     
     private final double rx2 = -hl;
     private final double ry2 = -hw;
     
     private final double rx3 = hl;
     private final double ry3 = -hw;
     
     private final double rx4 = hl;
     private final double ry4 = hw;
     
     //vars
     private double sumx1;
     private double sumy1;
     private double sumx2;
     private double sumy2;
     private double sumx3;
     private double sumy3;
     private double sumx4;
     private double sumy4;
     
     private double length = 0.0;
     private double maxLength = 0.0;
     
     private boolean relstrMode = false;
     
     public RobotDriveSwerve(SwerveWheel wheel1,SwerveWheel wheel2,
             SwerveWheel wheel3, SwerveWheel wheel4,Gyro relstrGyro) {
         super(wheel1.getMotor(),wheel2.getMotor(),wheel3.getMotor(),wheel4.getMotor());
         this.relstrGyro = relstrGyro;
     }
     
     public RobotDriveSwerve(SwerveWheel wheel1,SwerveWheel wheel2,
             SwerveWheel wheel3, SwerveWheel wheel4) {
         this(wheel1,wheel2,wheel3,wheel4,null);
     }
     
     /**
      * Controls all 4 SwerveWheels according to the following parameters:
      * @param vx The horizontal translational component of the robot's movement.
      * @param vy The vertical translational component.
      * @param rotation The rotational component.
      */
     public void swerveDrive(double vx,double vy,double rotation)
     {
         if(relstrMode && relstrGyro != null)
         {
             //perform a clockwise rotation of <gyro> degrees on input vector
             double angle = relstrGyro.getAngle();
             double cosa = Math.cos(angle);
             double sina = Math.sin(angle);
             
             double newvx = vx * cosa + vy * sina;
             double newvy = -vx * sina + vy * cosa;
             
             vx = newvx;
             vy = newvy;
         }
         
         sumx1 = vx + rotation * rx1;
         sumy1 = vy + rotation * ry1;
         
         sumx2 = vx + rotation * rx2;
         sumy2 = vy + rotation * ry2;
         
         sumx3 = vx + rotation * rx3;
         sumy3 = vy + rotation * ry3;
         
         sumx4 = vx + rotation * rx4;
         sumy4 = vy + rotation * ry4;
         
         checkMaxLength(sumx1,sumy1);
         checkMaxLength(sumx2,sumy2);
         checkMaxLength(sumx3,sumy3);
         checkMaxLength(sumx4,sumy4);
         
         maxLength = Math.sqrt(maxLength);
         
         sumx1 /= maxLength;
         sumy1 /= maxLength;
         sumx2 /= maxLength;
         sumy2 /= maxLength;
         sumx3 /= maxLength;
         sumy3 /= maxLength;
         sumx4 /= maxLength;
         sumy4 /= maxLength;
         
         wheel1.setVector(sumx1,sumy1);
         wheel1.update();
         
         wheel2.setVector(sumx2,sumy2);
         wheel2.update();
         
         wheel3.setVector(sumx3,sumy3);
         wheel3.update();
         
         wheel4.setVector(sumx4,sumy4);
         wheel4.update();
     }
     
     private void checkMaxLength(double x,double y)
     {
         length = Math708.lengthSquared(x,y);
         if(length > maxLength) maxLength = length;
     }
     
     /*
      * The following two methods are used to switch relative
      * steering mode on or off.
      */
     
     public void setRelStrMode(boolean b)
     {
         relstrMode = b;
     }
     
     public boolean isRelStrMode()
     {
         return relstrMode;
     }
     
     public double getGyroAngle()
     {
         if(relstrGyro != null)
             return relstrGyro.getAngle();
         else return 0.0;
     }
     
     public void resetGyro()
     {
         if(relstrGyro != null)
             relstrGyro.reset();
     }
 }
 
