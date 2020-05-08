 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.DriverStationLCD.Line;
 
 /**
  *
  * @author Philip2
  */
 public class Shooter {
    int StageOneMotorPWM = 1; //Declares channel of StageOne PWM
    int StageTwoMotorPWM = 2; //Declares channel of StageTwo PWM
    Talon StageOneTalon = new Talon(StageOneMotorPWM); //Creates instance of StageOne PWM
    Talon StageTwoTalon = new Talon(StageTwoMotorPWM); //Creates instance of StageTwo PWM 
    DriverStationLCD dsLCD;
    Joystick shootStick = new Joystick(2);
    
    /************************
     * XBOX Shooter Buttons *
     * *********************/
    
    boolean shootA = shootStick.getRawButton(1);
    boolean shootB = shootStick.getRawButton(2);
    boolean shootX = shootStick.getRawButton(3);
    boolean shootY = shootStick.getRawButton(4);
    boolean shootRB = shootStick.getRawButton(5);
    boolean shootLB = shootStick.getRawButton(6);
    boolean shootBack = shootStick.getRawButton(7); 
    boolean shootStart = shootStick.getRawButton(8);
    
    /************************
     * XBOX Shooter Axes *
     * *********************/
    
    double shootLX = shootStick.getRawAxis(1);
    double shootLY = shootStick.getRawAxis(2);
    double shootTriggers = shootStick.getRawAxis(3);
    double shootRX = shootStick.getRawAxis(4);
    double shootRY = shootStick.getRawAxis(5);
    double shootDP = shootStick.getRawAxis(6);
    
    int idle = 1;
    int off = 0;
    double currentRPMT2 = StageTwoTalon.get();
 
 
  
    
    public Shooter() {
        dsLCD = DriverStationLCD.getInstance();
        dsLCD.updateLCD();
        
        if (shootStart){
            StageTwoTalon.set(idle);
            StageOneTalon.set(idle * .5);
            dsLCD.println(Line.kUser1, 1, "Motors On");
        } else{
            StageTwoTalon.set(off);
            StageOneTalon.set(off *.5);
            dsLCD.println(Line.kMain6, 1, "Motors Off");
        }
        
        if (shootA){
            StageOneTalon.set((currentRPMT2 + 100) *.5);
            dsLCD.println(Line.kUser2, 1, "Adding 100 RPM");
        }
        
        if (shootB){
            StageTwoTalon.set(currentRPMT2 - 100);
            StageOneTalon.set((currentRPMT2 - 100) *.5);       
            dsLCD.println(Line.kUser2, 1, "Removing 100 RPM.");
        }
         
     }
 }
