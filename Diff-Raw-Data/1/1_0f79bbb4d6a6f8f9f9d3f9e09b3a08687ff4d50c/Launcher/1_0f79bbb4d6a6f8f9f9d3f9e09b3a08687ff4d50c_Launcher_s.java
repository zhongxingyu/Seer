 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Timer;
 
 /**
  *
  * @author Nick, Michael
  */
 public class Launcher {
     
     Jaguar launchMotor;
     Jaguar loadMotor;
     Encoder encoder;
     
     /**
      * Default constructor for the launcher
      */
     public Launcher() {
         this.launchMotor = new Jaguar(RoboMap.LAUNCH_MOTOR);
         this.loadMotor = new Jaguar(RoboMap.LOAD_MOTOR);
         this.encoder = new Encoder(RoboMap.LAUNCH_ENCODER1, RoboMap.LAUNCH_ENCODER2, false);
         this.encoder.setDistancePerPulse(1);
     }
     
     private double getRPM() {
         double encod1 = encoder.get();
         double deltaTime = .01;
         Timer.delay(deltaTime);
         double encod2 = encoder.get();
         double deltaEncod = encod2 - encod1;
         double RPM = deltaEncod / (deltaTime * 6);
         return RPM;
     }
     
     public void shoot(int pixel) {
         double speed = 0.0;
         int launchSpeed = pixel * /* this can change to tested values later */1000;
         encoder.start();
         while (MathX.abs(getRPM() - launchSpeed) >= 100) {
             launchMotor.set(speed);
             speed += .025;
             Timer.delay(.025);
         }
         encoder.stop();
         loadMotor.set(.25);
         Timer.delay(5);
         loadMotor.set(0);
     }
    
 }
