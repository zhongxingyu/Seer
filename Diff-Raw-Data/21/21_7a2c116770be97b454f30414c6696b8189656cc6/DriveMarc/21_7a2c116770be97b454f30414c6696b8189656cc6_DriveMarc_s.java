 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package modules;
 
 import mainframe.AvianceThread;
 import edu.wpi.first.wpilibj.DoubleSolenoid;
 import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Talon;
 
 /**
  *
  * @author zitellim
  */
 public class DriveMarc extends AvianceThread {
     
     Joystick rightjoy = new Joystick(1);
     Joystick leftjoy = new Joystick(2);
     
    Talon FL = new Talon(1);
    Talon BL = new Talon(2);
    Talon FR = new Talon(3);
    Talon BR = new Talon(4);
     
     DoubleSolenoid piston1 = new DoubleSolenoid(1,2);
     
     double leftTank;
     double rightTank;
     
     protected void iteration(){
     
         rightTank = rightjoy.getY();
         leftTank = leftjoy.getY();
         
        FL.set(leftTank);
        BL.set(leftTank);
        FR.set(rightTank);
        BR.set(rightTank);
         
         piston1.set(DoubleSolenoid.Value.kForward);
         
 }
 }
