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
 public class RobotTemplate extends IterativeRobot implements PIDSource, PIDOutput{
 
     RobotDrive drive;
     Messager msg;
     Joystick leftStick, rightStick, launchControlStick, testStick;
     Controls launchControls;
     AxisCamera camera;
     ImageProcessing imageProc;
     ParticleAnalysisReport target;
     Launcher launcher;
     Victor bridgeArm, collector;
     GyroX gyro;
     PIDController pid;
     boolean isManual = true;
     boolean isShooting = false;
     int shots = 0;
     double distanceFromTarget;
     DeadReckoning dead;
     ParticleFilters rajathFilter;
 
     public void robotInit() {
         pid = new PIDController(0.1, 0, 0, this, this);
         pid.setSetpoint(0);
         pid.setOutputRange(-.4, .4);
         msg = new Messager();
         msg.printLn("Loading Please Wait...");
         Timer.delay(10);
 
        
         drive = new RobotDrive(new Jaguar(1), new Jaguar(2));
        
         drive.setSafetyEnabled(false);
         getWatchdog().setEnabled(false);
         //leftStick = new Joystick(RoboMap.JOYSTICK1);
         //rightStick = new Joystick(RoboMap.JOYSTICK2);
         //launchControlStick = new Joystick(RoboMap.JOYSTICK3);
         //launchControls = new Controls(launchControlStick);
 
         camera = AxisCamera.getInstance();
         camera.writeBrightness(30);
         camera.writeResolution(AxisCamera.ResolutionT.k640x480);
         imageProc = new ImageProcessing();
         
         //bridgeArm = new Victor(RoboMap.BRIDGE_MOTOR);
         //collector = new Victor(RoboMap.COLLECT_MOTOR);
         //launcher = new Launcher();
 
         //dead = new DeadReckoning(drive,launcher.launchMotor,launcher.loadMotor, collector,bridgeArm);
 
         //gyro = new GyroX(RoboMap.GYRO, RoboMap.LAUNCH_TURN, drive);
         //rajathFilter = new ParticleFilters();
         msg.printLn("Done: FRC 2012");
     }
 
     public void autonomousInit() {
         isShooting = false;//change me!!!!!
         
         
     }
     
     
 
     public void autonomousPeriodic() { 
         
         //dead.driveToBridge();
         //dead.driveToBridge();
        // dead.shoot();
         
         if (camera.freshImage()) {
             try {
                 
                 imageProc.getTheParticles(camera);
                 pid.enable();
                 
 
                 //msg.printOnLn("Top:" + imageProc.isTopTarget(target), DriverStationLCD.Line.kMain6);
                 //msg.printOnLn("Bottom:" + imageProc.isBottomTarget(target), DriverStationLCD.Line.kUser2);
                 //msg.printOnLn("dist(midtop):" + imageProc.getDistance(imageProc.particles[0], ImageProcessing.topTargetHeight), DriverStationLCD.Line.kUser3);
                 //msg.printOnLn("Tilt:" + imageProc.getCameraTilt(), DriverStationLCD.Line.kUser4);
                                
                 // start gyro debug
                 //double angle = imageproc.getHorizontalAngle();
                 
                 //gyro.turnTurret(angle);
                 //end gyro debug
 
 
             } catch (Exception e) {
                 System.out.println("Exception:" + e.getMessage());
             }         
            
 
 
         } else {
             msg.printLn("No Camera Image");
         }
        
         
          /*
           * if(imageProc.isTopTarget(target)) { msg.printLn("Top"); }
           * if(imageProc.isBottomTarget(target)) { msg.printLn("Botton");
           * } if(!imageProc.isBottomTarget(target) &&
           * !imageProc.isTopTarget(target)) { msg.printLn("No target
           * found"); }
           *
           */
 
     }
     
     
 
     public void teleopInit() {
         pid.disable();
         pid.free();
         launcher.launchMotor.set(0);
         collector.set(0);
         launcher.loadMotor.set(0);
 
         msg.clearConsole();
     }
 
     public void teleopPeriodic() {
         System.out.println("Value: " + dead.potentiometer.getVoltage());
 
        // switch to control assisted teleop
        double power = (testStick.getZ() + 1)/2;
        msg.printLn(Double.toString(power));
        if(testStick.getRawButton(2)){            
            launcher.launchMotor.set(power);
            Timer.delay(3);
            collector.set(-1);
            launcher.loadMotor.set(-1);
            Timer.delay(7);
            launcher.launchMotor.set(0);
            collector.set(0);
            launcher.loadMotor.set(0);
        }
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
         /*
          * if (camera.freshImage() && isShooting) { try {
          *
          * imageProc.getTheParticles(camera); ParticleAnalysisReport topTarget =
          * imageProc.getTopTarget(); double angle =
          * ImageProcessing.getHorizontalAngle(topTarget);
          * gyro.turnTurret(angle); launcher.shootTopTarget();          *
          *
          * isShooting = false; } catch (Exception e) {
          * msg.printLn(e.getMessage()); isShooting = false; }
          *
          * }
          *
          */
 
 
     }
 
     public void pidWrite(double output) {
         //gyro.lazySusan.setRaw((int)output);
         drive.arcadeDrive(0, output);
         
     }
 
     public double pidGet() {
         double d = ImageProcessing.getHorizontalAngle(
                 ImageProcessing.getBottomMost(imageProc.particles));
         System.out.println(-d);
         return -d;
     }
 }
