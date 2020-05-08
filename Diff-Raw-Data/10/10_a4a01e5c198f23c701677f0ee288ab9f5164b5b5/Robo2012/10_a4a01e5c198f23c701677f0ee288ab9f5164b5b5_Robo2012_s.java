 /*
  * FRC Team 53:  The Alien Cow Abductors
  * 2012 FRC Competition "Rebound Rumble"
  * Released under GNU GPL v. 3 or later
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
 
 /**
  *
  * @author Team53
  */
 public class Robo2012 extends IterativeRobot {
 
     RobotDrive drive;
     Joystick stick;
     AxisCamera camera;
     ImageProcessing imageProc;
     ParticleAnalysisReport target;
     Physics physics;
     Launcher launcher;
     Jaguar bridgeArm;    
     Jaguar collectMotor;
     //AnalogChannel autoPot;
     //AnalogChannel telePot;
     GyroX gyro;
     Messager msg;
     Controls controls;
 
     boolean isManual = false;
     String idTarget = "top";
 
     boolean isShooting = false;
     int shots = 0;
     double distanceFromTarget;
     double hoopHeight = Physics.HOOP1;
 
     public void robotInit() {
         msg = new Messager();
         msg.printLn("Loading Please Wait...");
         Timer.delay(10);
         //left front, left back, right front, right back
         drive = new RobotDrive(
                 RoboMap.MOTOR1, RoboMap.MOTOR3, RoboMap.MOTOR2, RoboMap.MOTOR4);
         drive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
         drive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
 
         stick = new Joystick(RoboMap.JOYSTICK1);
         controls = new Controls(stick);
 
         camera = AxisCamera.getInstance();
         camera.writeBrightness(30);
         camera.writeResolution(AxisCamera.ResolutionT.k640x480);
         imageProc = new ImageProcessing();
         physics = new Physics(false);
         bridgeArm = new Jaguar(RoboMap.BRIDGE_MOTOR);        
         collectMotor = new Jaguar(RoboMap.COLLECT_MOTOR);
         launcher = new Launcher();
         gyro = new GyroX(RoboMap.GYRO, RoboMap.LAUNCH_TURN, drive);
         //autoPot = new AnalogChannel(RoboMap.AUTO_POT);
         //telePot = new AnalogChannel(RoboMap.TELO_POT);
         msg.printLn("FRC 2012");
     }
 
     public void autonomousInit() {
         isShooting = true;
     }
 
     public void autonomousPeriodic() {
         if (camera.freshImage()) {
             try {
                 imageProc.getTheParticles(camera);
 
                 target = ImageProcessing.getTopMost(imageProc.particles);
 
                 double p = (Physics.MAXWIDTH / 2) - target.center_mass_x;
 
                 double angle = p / physics.LAMBDA;
                 msg.printLn("" + angle);
                 
                 while (MathX.abs(angle - gyro.modulatedAngle) > 2) {
                     gyro.turnToAngle(angle);
                     getWatchdog().feed();
                 }
 
                 int img = 640;
                 //double p = (640 / 2) - target.center_mass_x;
                 //double angle = p / physics.LAMBDA;
                 msg.printLn("" + angle);
                 //gyro.turnToAngle(angle);
 // reformat to 80 characters, remove unused imports
                 if (isShooting) {
                     Timer.delay(3);
 
                     launcher.shoot(target.boundingRectHeight, Physics.HOOP3);
 
                     shots++;
                     if (shots == 2) {
                         isShooting = false;
                     }
                 }
             } catch (Exception e) {
                 e.printStackTrace();
                 msg.printLn("ERROR!!! Cannot Fetch Image");
             }
         }
         getWatchdog().feed();
     }   
 
     public void teleopPeriodic() {
         if (controls.button8()) {
             isManual = true;
             
         } else if (controls.button7()) {
             isManual = false;
             
         }
         if (controls.button1()) {//trigger reverses drive
            drive.mecanumDrive_Cartesian(-stick.getX(), -stick.getY(), -MathX.pow(stick.getTwist(), 3), 0);
             
         } else {
            drive.mecanumDrive_Cartesian(stick.getX(), stick.getY(), MathX.pow(stick.getTwist(), 3), 0);
             
         }
 
         if (!isManual) {
             //motor to collect the balls off the ground
             collectMotor.set((stick.getThrottle() + 1) / 2);
             if (controls.FOV_Left()) {
                 target = imageProc.middleTarget;
                 hoopHeight = Physics.HOOP2;
                 
             } else if (controls.FOV_Right()) {
                 target = imageProc.middleTarget;
                 hoopHeight = Physics.HOOP2;
                 
             } else if (controls.FOV_Top()) {
                 target = imageProc.topTarget;
                 hoopHeight = Physics.HOOP3;
                 
             } else if (controls.FOV_Bottom()) {
                 target = imageProc.bottomTarget;
                 hoopHeight = Physics.HOOP1;
                 
             }
             if (controls.button2()) {
                 isShooting = true;
             }
         } else {
             collectMotor.set(0);
             double power = (stick.getThrottle() + 1) / 2;
             launcher.launchMotor.set(power);
             
             if (controls.button2()) {
                 launcher.manualShoot();
                 
             }
         }
 
         if (controls.button3()) {
             gyro.turnRobotToAngle(0);
             
         } else if (controls.button4()) {
             gyro.turnRobotToAngle(180);
             
         } else if (controls.button5()) {
             gyro.turnRobotToAngle(-90);
             
         } else if (controls.button6()) {
             gyro.turnRobotToAngle(90);
             
         }
 
         //motor to control lazy susan for launcher
         if (controls.button9()) {
             gyro.turnAngle(5);
         } else if (controls.button10()) {
             gyro.turnAngle(-5);
         }
 
         // motor to lower bridge arm
         if (controls.button11()) {
             bridgeArm.set(1);
         } else if (controls.button12()) {
             bridgeArm.set(-.75);
 
          //reformat to 80 characters, remove unused imports
 
         // Have the camera scan for targets
 
 
         if (camera.freshImage()) {
             try {
                 imageProc.getTheParticles(camera);

                 /*TODO don't set a variable in the class (particles) and
                  * then call a method of the class passing in that same
                  * variable.  instead put that logic into a method in the
                  * class itself and call that method from here.
                  */
               // imageProc.idTopTarget(imageProc.getBottomMost(imageProc.particles));
              double Distance =  imageProc.CameraCorrection(target,idTarget);
              System.out.println("Look at Top Target,  Distance (inches) = " 
                      + Distance );
                 if (isShooting) {
                   launcher.shoot(target.boundingRectHeight, hoopHeight);
                   isShooting = false;
                 }
             } catch (Exception e) {
                 e.printStackTrace();
                 msg.printLn("ERROR!!! Cannot Fetch Image");
             }
         }
     }
  }
 }
