 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.DriverStationLCD.*;
 
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
   double stageOneScaler = .5;
   double PWMMax = 1;
   double shootSpeedScale = PWMMax/5300;
   double currentRPMT2 = StageTwoTalon.get()*shootSpeedScale;
   double currentRPMT1 = currentRPMT2*stageOneScaler;
   
 
      /**************
     * Shooter code *
     * *************/
    
    public Shooter() {
        dsLCD = DriverStationLCD.getInstance();
        dsLCD.updateLCD();
        
       dsLCD.println(Line.kUser1, 1, "Motors ");
       dsLCD.println(Line.kUser3, 1, "Stage One Speed: " + (currentRPMT1/currentRPMT2)*100 + "%");
       
        if (shootStart){
            StageTwoTalon.set(idle);
            StageOneTalon.set(idle * .5);

            //Stage one is set to be 50% of Stage 2
            dsLCD.println(Line.kUser1, 1, "Motors On");

        } else{
            StageTwoTalon.set(off);
            StageOneTalon.set(off *.5);

            //Stage one is set to be 50% of Stage 2
            dsLCD.println(Line.kMain6, 1, "Motors Off");

        }
        
        if (shootA){
            //This code is used to subtrack the current speed of Stage 2
            StageOneTalon.set((currentRPMT2 + 100) *.5);
            dsLCD.println(Line.kUser2, 1, "Adding 100 RPM");
        }
        
        if (shootB){
            //This code is used to subtrack the current speed of Stage 2
            StageTwoTalon.set(currentRPMT2 - 100);
            StageOneTalon.set((currentRPMT2 - 100) *.5);       
            dsLCD.println(Line.kUser2, 1, "Removing 100 RPM.");
            if (StageTwoTalon.get() < 0){
                StageTwoTalon.set(off);
                //if the speed is less than 0, turn off
            }
        }
         
     }
 }
