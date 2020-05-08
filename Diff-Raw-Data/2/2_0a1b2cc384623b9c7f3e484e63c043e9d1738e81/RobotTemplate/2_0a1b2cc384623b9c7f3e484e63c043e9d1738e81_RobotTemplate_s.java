 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.SimpleRobot;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.Compressor;
 //import edu.wpi.first.wpilibj.Jaguar;
 //import edu.wpi.first.wpilibj.DriverStationLCD;
 
 
 public class RobotTemplate extends SimpleRobot {
     /**
      * This function is called once each time the robot enters autonomous mode.
      */
     public RobotDrive drivetrain;
     public Relay spikeA;
     public Joystick leftStick;
     public Joystick rightStick;
     public String controlScheme = "twostick";
     public Compressor pnuematicA;
     //public DriverStationLCD lcd;
     //public Victor gearMotor;
 
 
     public RobotTemplate() {
         //Instantialize objects for RobotTemplate
         getWatchdog().setEnabled(false);
         leftStick = new Joystick(1);
         rightStick  = new Joystick(2);
         
         //gearMotor = new Victor(5); //initialize speed controller
        
         //2-Wheel tank drive
         spikeA = new Relay(1);
         pnuematicA = new Compressor(1,2);
         drivetrain = new RobotDrive(1,2);
         
         //4-Wheel tank drive
         //Motors must be set in the following order:
         //LeftFront=1; LeftRear=2; RightFront=3; RightRear=4;
         //drivetrain = new RobotDrive(1,2,3,4);
         //drivetrain.tankDrive(leftStick, rightStick);
     }
 
 
     public void autonomous() {
         for (int i = 0; i < 4; i++){
             drivetrain.drive(0.5, 0.0); // drive 50% fwd 0% turn
             Timer.delay(2.0);    // wait 2 seconds
             drivetrain.drive(0.0, 0.75); // drive 0% fwd, 75% turn
         }
         drivetrain.drive(0.0, 0.0);   // drive 0% forward, 0% turn
     }
 
 
     public void operatorControl() {
          
         drivetrain.setSafetyEnabled(true);
          
          while(isOperatorControl() && isEnabled()){
              drivetrain.tankDrive(leftStick, rightStick);
              Timer.delay(0.01);
              
              // Test spike relay code
              if(rightStick.getTrigger()){
                 spikeA.set(Relay.Value.kForward);
                 pnuematicA.start();
              }
                 else{
                 spikeA.set(Relay.Value.kOff);
                 pnuematicA.stop();
              }
              
              // Switches control scheme from "tank" to "arcade"
              // when left trigger is pressed
              if(controlScheme.equals("twostick")) {
                  drivetrain.tankDrive(leftStick, rightStick);
              }
              
              if(controlScheme.equals("onestick")) {
                  drivetrain.arcadeDrive(leftStick);
              }
              
              if(leftStick.getTrigger()) {
                  if (controlScheme.equals("twostick")) {
                      controlScheme = "onestick";
                  } else {
                      controlScheme = "twostick";
                  }
              }
              /*if(leftStick.getTrigger()){
                  lcd.println(DriverStationLCD.Line.kUser2, 1, motor.get());
                  Jaguar motor;
                  motor = new Jaguar(1);
                  if(motor.get() > -1){
                  motor.set(motor.get() - .1);
                  }
                  //gearMotor.set(.5);//if right stick trigger is pressed, set motor to 50% speed
              }
              if(rightStick.getTrigger()){
                  Jaguar motor;
                  motor = new Jaguar(2);
                  if(motor.get() > -1){
                  motor.set(motor.get() - .1);
                  }
                  //gearMotor.set(.5);//if right stick trigger is pressed, set motor to 50% speed
              }
              else{
                  //gearMotor.set(0);
              }*/
         }
     }
 }
 
 
