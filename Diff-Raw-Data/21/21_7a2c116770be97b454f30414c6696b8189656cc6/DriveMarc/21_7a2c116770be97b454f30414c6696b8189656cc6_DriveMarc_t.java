 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package modules;
 
 import mainframe.AvianceThread;
 import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;

 
 /**
  *
  * @author zitellim
  */
 public class DriveMarc extends AvianceThread {
     
     Joystick rightjoy = new Joystick(1);
     Joystick leftjoy = new Joystick(2);
     
    Jaguar LJ = new Jaguar(1);
    Jaguar RJ = new Jaguar(2);
     
     DoubleSolenoid piston1 = new DoubleSolenoid(1,2);
     
     double leftTank;
     double rightTank;
     
     protected void iteration(){
     
         rightTank = rightjoy.getY();
         leftTank = leftjoy.getY();
         
        LJ.set(leftTank);
        RJ.set(rightTank * -1);
        
         
         piston1.set(DoubleSolenoid.Value.kForward);
         
         
 }
 }
