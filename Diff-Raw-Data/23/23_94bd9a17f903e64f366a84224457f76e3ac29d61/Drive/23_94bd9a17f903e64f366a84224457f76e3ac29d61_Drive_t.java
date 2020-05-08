 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package core;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.Talon;
 import util.MyJoystick;
 import util.Config;
 
 /**
  *
  * @author Merfoo
  */
 public class Drive 
 {
     private MyJoystick joy;
     private double motorLeftSpeed;
     private double motorRightSpeed;
     private Talon motorLeft1;
     private Talon motorLeft2;
     private Talon motorLeft3;
     private Talon motorRight1;
     private Talon motorRight2;
     private Talon motorRight3;
     
     Encoder enc = new Encoder(8,9);
     
     public Drive(MyJoystick stick)
     {
         joy = stick;
         motorLeft1 = new Talon(Config.chanDriveLeft1);
         motorLeft2 = new Talon(Config.chanDriveLeft2);
         motorLeft3 = new Talon(Config.chanDriveLeft3);
         motorRight1 = new Talon(Config.chanDriveRight1);
         motorRight2 = new Talon(Config.chanDriveRight2);
         motorRight3 = new Talon(Config.chanDriveRight3);
         enc.reset();
         enc.start();
     }
     
     public void runArcade()
     {
         double joyX = joy.getMyX()*Math.abs(joy.getMyX());
         double joyY = joy.getMyY()*Math.abs(joy.getMyY());
         
         motorLeftSpeed = joyY+joyX;
         motorRightSpeed = joyY-joyX;
         setSpeed(motorLeftSpeed, motorRightSpeed);
        
        if(joy.gotPressed(Config.btTest))
            System.out.println("Button got Pressed");
     }
     
     private void setSpeed(double left, double right)
     {
         right = -right;
         
         motorLeft1.set(left);
         motorLeft2.set(left);
         motorLeft3.set(left);
         motorRight1.set(right);
         motorRight2.set(right);
         motorRight3.set(right);
     }
     
     public double getLeftSpeed()
     {
         return motorLeft1.get();
     }
     
     public double getRightSpeed()
     {
         return -motorRight1.get();
     }
 }
