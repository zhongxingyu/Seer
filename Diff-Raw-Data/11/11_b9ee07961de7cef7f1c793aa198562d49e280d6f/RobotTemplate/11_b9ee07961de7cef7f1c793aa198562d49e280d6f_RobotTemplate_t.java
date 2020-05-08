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
 public class RobotTemplate extends IterativeRobot {
 
     RobotDrive drive;
     Messager msg;
     Joystick leftStick, rightStick, launchControlStick;
     Controls launchControls;
     AxisCamera camera;
     ImageProcessing imageProc;
     ParticleAnalysisReport target;
     Launcher launcher;    
     Victor bridgeArm, collector;
     GyroX gyro;
     boolean isManual = true;
     boolean isShooting = false;
     int shots = 0;
     double distanceFromTarget;
     DeadReckoning dead;
     ParticleFilters rajathFilter;
     public void robotInit() {
         msg = new Messager();
         msg.printLn("Loading Please Wait...");
         Timer.delay(10);
 
         //drive = new RobotDrive(
         //        RoboMap.MOTOR1, RoboMap.MOTOR2, RoboMap.MOTOR3, RoboMap.MOTOR4);
         //drive = new RobotDrive(new Victor(RoboMap.MOTOR1), new Victor(RoboMap.MOTOR2), new Victor(RoboMap.MOTOR3), new Victor(RoboMap.MOTOR4));
         //drive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
         //drive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
         //drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
         //drive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
         //drive.setSafetyEnabled(false);
         //getWatchdog().setEnabled(false);
         //leftStick = new Joystick(RoboMap.JOYSTICK1);
         //rightStick = new Joystick(RoboMap.JOYSTICK2);
         //launchControlStick = new Joystick(RoboMap.JOYSTICK3);
         //launchControls = new Controls(launchControlStick);
 
         camera = AxisCamera.getInstance();
         camera.writeBrightness(30);
         camera.writeResolution(AxisCamera.ResolutionT.k640x480);
         imageProc = new ImageProcessing();
 
         //physics = new Physics();
         //bridgeArm = new Victor(RoboMap.BRIDGE_MOTOR);
         //collector = new Victor(RoboMap.COLLECT_MOTOR);
         //launcher = new Launcher();
 
         //dead = new DeadReckoning(drive,launcher.launchMotor,launcher.loadMotor, collector,bridgeArm);
 
         //gyro = new GyroX(RoboMap.GYRO, RoboMap.LAUNCH_TURN, drive);
         rajathFilter = new ParticleFilters();
         msg.printLn("Done: FRC 2012");
     }
 
     public void autonomousInit() {
         isShooting = false;//change me!!!!!
         if(camera.freshImage())
         {
                  try{
                     imageProc.getTheParticles(camera);
         }catch(Exception e)
                 {
                     e.printStackTrace();
                 }
         rajathFilter.getDistances(imageProc.particles);
         rajathFilter.setArray();   
         }
     }
 
     public void autonomousPeriodic() {
         if(camera.freshImage())
         {
                  try{
                     imageProc.getTheParticles(camera);
         }catch(Exception e)
         {
             e.printStackTrace();
         }
         rajathFilter.getDistances(imageProc.particles);
         rajathFilter.compare();
              
         }
         else
         {
             msg.printLn("CAN'T FIND TARGET");
         }
             //dead.driveToBridge();
             /*
              * if (camera.freshImage() && false) { try {
              * imageProc.getTheParticles(camera); target =
              * ImageProcessing.getTopMost(imageProc.particles);
              *
              *
              * double angle = ImageProcessing.getHorizontalAngle(target);
              * //msg.printLn("" + angle); /* while (MathX.abs(angle -
              * gyro.modulatedAngle) > 2) { gyro.turnToAngle(angle);
              * getWatchdog().feed(); }
              *
              *
              *
              *
              *
              *
              *
              * if (isShooting) { Timer.delay(3);
              *
              * launcher.shoot(target.boundingRectHeight, Physics.HOOP3);
              *
              * shots++; if (shots == 2) { isShooting = false; } } } catch
              * (Exception e) { e.printStackTrace(); System.out.println("ERROR!!!
              * Cannot Fetch Image"); } } getWatchdog().feed();
              */
         
 
 
 
 
 
         //dead.driveToBridge();
 
         /*
          * if (camera.freshImage() && false) { try {
          * imageProc.getTheParticles(camera); target =
          * ImageProcessing.getTopMost(imageProc.particles);
          *
          *
          * double angle = ImageProcessing.getHorizontalAngle(target);
          * //msg.printLn("" + angle); /* while (MathX.abs(angle -
          * gyro.modulatedAngle) > 2) { gyro.turnToAngle(angle);
          * getWatchdog().feed(); }
          *
          *
          *
          *
          *
          *
          *
          * if (isShooting) { Timer.delay(3);
          *
          * launcher.shoot(target.boundingRectHeight, Physics.HOOP3);
          *
          * shots++; if (shots == 2) { isShooting = false; } } } catch (Exception
          * e) { e.printStackTrace(); System.out.println("ERROR!!! Cannot Fetch
          * Image"); } } getWatchdog().feed();
          */
     }
 
     public void teleopInit() {
         launcher.launchMotor.set(0);
         collector.set(0);
         launcher.loadMotor.set(0);
         
         msg.clearConsole();
     }
 
     public void teleopPeriodic() {
 
         // switch to control assisted teleop
         if (launchControls.button11()) {
             isManual = true;
         } else if (launchControls.button12()) {
             isManual = false;
         }
 
         // drive system, independent of teleop assistance
         if (leftStick.getRawButton(2) || rightStick.getRawButton(2)) {
             drive.tankDrive(leftStick.getAxis(Joystick.AxisType.kY) * .5,
                     rightStick.getAxis(Joystick.AxisType.kY) * .5);
         } else {
             drive.tankDrive(leftStick, rightStick);
         }
 
         // motor to lower bridge arm, currently independent of teleop assitance
         if (leftStick.getRawButton(3)) {
             bridgeArm.set(.5);
         } else if (leftStick.getRawButton(2)) {
             bridgeArm.set(-1);
         } else {
             bridgeArm.set(0);
         }
 
         if (isManual) {
             msg.printOnLn("Mode: Manual", DriverStationLCD.Line.kMain6);
             if (launchControls.button7()) {
                 collector.set(-1);
             } else if (launchControls.button8()) {
                 collector.set(0);
             }
             double power = (launchControlStick.getThrottle() + 1) / 2;
             launcher.launchMotor.set(power);
             msg.printOnLn("Launch Power = " + power, DriverStationLCD.Line.kUser2);
             // control the firing mechanism
             if (launchControls.button1()) {
                 launcher.manualShoot();
             } else {
                 launcher.loadMotor.set(0);
             }
         } else if (!isManual) {
             msg.printOnLn("Mode: Auto", DriverStationLCD.Line.kMain6);
             collector.set((launchControlStick.getThrottle() + 1) / 2);
 
             if (launchControls.button2()) {
                 isShooting = true;
             }
         }
 
         if (camera.freshImage() && isShooting) {
             try {
                 imageProc.getTheParticles(camera);
                 ParticleAnalysisReport topTarget = imageProc.getTopTarget();
                 double angle = ImageProcessing.getHorizontalAngle(topTarget);
                 gyro.turnTurret(angle);
                 launcher.shootTopTarget();        
                 
                 
                 isShooting = false;               
             } catch (Exception e) {
                 msg.printLn(e.getMessage());
                 isShooting = false;
             }
 
        }
 
     }
 }
