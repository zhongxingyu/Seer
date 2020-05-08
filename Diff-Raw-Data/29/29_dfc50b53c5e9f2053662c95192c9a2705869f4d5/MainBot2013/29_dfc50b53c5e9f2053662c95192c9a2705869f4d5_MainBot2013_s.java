 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
     
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;
 //import edu.wpi.first.wpilibj.Preferences;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.Servo;
 import edu.wpi.first.wpilibj.SimpleRobot;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Victor;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the SimpleRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 
 public class MainBot2013 extends SimpleRobot {
     /**
      * This function is called once each time the robot enters autonomous mode.
      */
     //Preferences prefs;
     
     //Relays
     Relay banana2 = new Relay(6);
     
     //Digital inputs
     DigitalInput firingmech = new DigitalInput(1);
     DigitalInput tilttop = new DigitalInput(2);
     DigitalInput tiltbot = new DigitalInput(3);
     DigitalInput Rotleft = new DigitalInput(4);
     DigitalInput Rotright = new DigitalInput(5);
     
     //Motor Control
     Jaguar leftdrive1 = new Jaguar(1);
     Jaguar leftdrive2 = new Jaguar(2);
     Jaguar rightdrive1 = new Jaguar(3);
     Jaguar rightdrive2 = new Jaguar(4);
     Jaguar shooter = new Jaguar(5);
     Victor tilter = new Victor(7);
    Victor rotator = new Victor(6);
     Servo leftgearbox = new Servo(10);
     Servo rightgearbox = new Servo(9);
    
     
     //Digitaledness
     Joystick leftStick = new Joystick(1);
     Joystick rightStick = new Joystick(2);
     Joystick actionJoy = new Joystick(3);
     JoystickButton trigger = new JoystickButton(actionJoy, 1);
     JoystickButton lefttrig = new JoystickButton(leftStick,1);
     JoystickButton righttrig = new JoystickButton(rightStick,1);
     JoystickButton tiltAllow = new JoystickButton(actionJoy,3);
     JoystickButton rotAllow = new JoystickButton(actionJoy,4);
  
     double diskNumber;
     
     
     public void autonomous() {
         while (isAutonomous () && isEnabled()) {
                 if (diskNumber == 2){
                     
                 }
                 else if (diskNumber == 3){
                     
                 }
         }
     }
 
     /**
      * This function is called once each time the robot enters operator control.
      */
     
     public double rampmotor(double req, double cur){
         double error = Math.abs(req - cur);
         double output = 0;
         
         if(error >= 0.1){
             output = ((0.1)*(req-cur)+ cur);
             
         }
         else{
             output = req;
         }
         return output;
     }
     
     
     public void operatorControl() {
         boolean fire = false, gearboxstate = false, b, prevalue = false;        
         double Tiltvalue = 0.0, Rotationvalue = 0;                              
         double shooterspeed=0;
         long Currenttime, Presstime = 0;
         SmartDashboard.putString("Teleop:", " Enabled");
         while (isOperatorControl() && isEnabled())                              //Runs while enagled 
         {
             Timer.delay(0.1);
             Currenttime = System.currentTimeMillis();
             
             leftdrive1.set((leftStick.getY()*.75));
             leftdrive2.set((leftStick.getY()*.75));
             rightdrive1.set((rightStick.getY()*.75)*(-1));
             rightdrive2.set((rightStick.getY()*.75)*(-1));                                  
           
 
                 
             //Gearbox
             
             if (lefttrig.get() && righttrig.get()){
                 b = true;                                                       // b if both buttons pressed
             } else {
                 b = false;
             }
             if (!prevalue && b){
                 gearboxstate=!gearboxstate;
             }
             if (gearboxstate){
                 SmartDashboard.putString("Gear:", " HIGH");
                 leftgearbox.set(0.23);
                 rightgearbox.set(0.23);
             } else {
                 SmartDashboard.putString("Gear:", " LOW");
                 leftgearbox.set(0.76);
                 rightgearbox.set(0.76);
             }
             if (b){
                 prevalue = true;
             } else {
                 prevalue = false;
             }
             
             //Shooter
             shooterspeed=rampmotor((((actionJoy.getThrottle()-1)/-2)*(-1)),shooterspeed);
             shooter.set(shooterspeed);
             SmartDashboard.putNumber("Shooter Speed: ", shooterspeed * (-100)); //happy kyle? indeed
             
             
             //Kicker
             if(firingmech.get()){                                               //limitswitch is pressed
                 if(trigger.get()){                                              //the firing button is pressed
                     fire = true;                                                //motor should be turned on
                     Presstime = System.currentTimeMillis();
                 }else{                                                          
                     fire = false;                                               //motor should be turned off
                 }                                                               
             }else{                                                              //limitswitch is not pressed
                 fire = true;                                                    //motor should be turned on
             }
             if ((Currenttime-Presstime) > 50000){
                 fire = false;
             }
             if(fire){                                                           //if motor should be on
                 banana2.set(Relay.Value.kForward);                              //set it to on
             }else{                                                              //if motor should be off
                 banana2.set(Relay.Value.kOff);                                  //set it to off
             }
             
             //Tilt Aimer
             Tiltvalue = actionJoy.getY(); //(-1);
             
             if (tiltAllow.get()){ 
                  if (tilttop.get() && tiltbot.get()){                           //if both are pressed, this shouldnt happen
                     SmartDashboard.putString("Tilt limit:", "both limits pressed");
                     tilter.set(0.0);
                 } else if (tilttop.get()){                                      //is top limit switch pressed
                     SmartDashboard.putString("Tilt limit:", " you have reached the upper limit");
                     if (Tiltvalue < 0.0){                                       //if the suggested value is down
                         tilter.set(Tiltvalue);                                  //allow set of motor
                     } else {                                                    //if the suggested value is up
                         tilter.set(0);                                          //do not set motor
                     }
                 } else if (tiltbot.get()){                                      //is bottom limit switch pressed
                     SmartDashboard.putString("Tilt limit:", " you have reached the bottom limit");
                     if (Tiltvalue > 0.0){                                       //if the suggested value is up
                         tilter.set(Tiltvalue);                                  //allow set of motor
                     } else {                                                    //if the suggested value is down
                         tilter.set(0);                                          //do not set motor
                     }
                 } else {
                    SmartDashboard.putString("Limitswitch:","neither switch is pressed");
                    tilter.set(Tiltvalue);                                      //
                } 
             }
             
             //Rotator Aimer
             
            Rotationvalue = actionJoy.getY()*(-1);
             
             if (rotAllow.get()){
                 if (Rotleft.get() && Rotleft.get()){                            //if both are pressed, this shouldnt happen
                     SmartDashboard.putString("Turn Limit", "this instance should never be reached expect physical problem");
                     rotator.set(0.0);
                 } else if (Rotright.get()){                                     //is top limit switch pressed
                     SmartDashboard.putString("Turn Limit", "you have reached the right limit");
                     if (Rotationvalue < 0.0){                                       //if the suggested value is down
                        rotator.set(Tiltvalue);                                 //allow set of motor
                     } else {                                                    //if the suggested value is up
                         rotator.set(0);                                         //do not set motor
                     }
                 } else if (Rotleft.get()){                                      //is bottom limit switch pressed
                     SmartDashboard.putString("Turn Limit", "you have reached the left limit");
                     if (Rotationvalue > 0.0){                                       //if the suggested value is up
                        rotator.set(Tiltvalue);                                 //allow set of motor
                     } else {                                                    //if the suggested value is down
                         rotator.set(0);                                         //do not set motor
                     }
                 } else {                                                        //
                     rotator.set(Tiltvalue);                                     //
                     SmartDashboard.putString("Turn Limit", "neither pressed");
                 }
             }
             
             //rob's version of tilt aimer program
             /*
             int position = 0;
             if (limitSwitchtop.get()){ 
                 position = 1;
             }
             if (limitSwitchbot.get()) {
                 position = -1;
             }
             
             if (position == 1 && Tiltvalue > 0) {
                 Tiltvalue = 0;
             }
             if (position == -1 && Tiltvalue < 0) {
                 Tiltvalue = 0;
             }
             tilter.set(Tiltvalue);
             */
             
         }
     }
     public void disabled(){
         SmartDashboard.putString("Teleop:", " Disabled");
     }
     public void robotInit() {
       //  diskNumber = prefs.getDouble("Number of auto disks", 2.0);
     }
 }
