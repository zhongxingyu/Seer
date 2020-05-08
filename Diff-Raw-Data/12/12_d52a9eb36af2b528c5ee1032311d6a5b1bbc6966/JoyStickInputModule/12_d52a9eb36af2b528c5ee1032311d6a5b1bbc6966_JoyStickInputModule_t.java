 package org.usfirst.frc1148.modules;
 
 import org.usfirst.frc1148.HWRobot;
 import org.usfirst.frc1148.data.MoveData;
 import org.usfirst.frc1148.interfaces.RobotModule;
 
 import edu.wpi.first.wpilibj.Joystick;
 
 public class JoyStickInputModule implements RobotModule {
 
     Joystick drive;
     Joystick secControl;
     Joystick thirdControl;
     double driveY;
     double driveX;
     //Talon testTalon;
     double rotSpeed;
     boolean isResetButtonPressed = false;
     boolean isRelativeTogglePressed = false;
     MoveData data;
     RobotDriver driver;
     DrawbridgeModule drawbridge;
     AutoDriveModule autoDrive;
     ClimberModule climber;
 
     public JoyStickInputModule(HWRobot robot, AutoDriveModule autoDriver, RobotDriver driver, DrawbridgeModule drawbridge, ClimberModule climb) {
         this.autoDrive = autoDriver;
         this.driver = driver;
         this.drawbridge = drawbridge;
         data = new MoveData();
         climber = climb;
     }
 
     public void initModule() {
         System.out.println("Initializing JoyStickInputModule!");
         //testTalon = new Talon(5);
         drive = new Joystick(1);
         secControl = new Joystick(2);
         thirdControl = new Joystick(3);
     }
 
     public void activateModule() {
         // TODO Auto-generated method stub
         driver.setMoveData(data);
         drawbridge.Enable();
         climber.Stop();
     }
 
     public void deactivateModule() {
         // TODO Auto-generated method stub
     }
 
     public void updateTick(int mode) {
         if (mode != 2) {
             return;
         }
        rotSpeed = drive.getZ()/1.5; 
         if (drive.getRawButton(2)) {
             rotSpeed = 0;
         }
         //exponential
        rotSpeed = rotSpeed * Math.abs(rotSpeed);
         driveY = drive.getY() * Math.abs(drive.getY());
         driveX = drive.getX() * Math.abs(drive.getX());
         //System.out.println("Z: "+rotSpeed+" Y: "+driveY+" X: "+driveX);
 
         if (driveY < .01 && driveY > -.01) {
             driveY = 0;
         }
         if (driveX < .01 && driveX > -.01) {
             driveX = 0;
         }
         if (rotSpeed < .01 && rotSpeed > -.01) {
             rotSpeed = 0;
         }
         if (drive.getRawButton(8) && !isResetButtonPressed) {
             driver.resetGyro();
             isResetButtonPressed = true;
             System.out.println("Gyro reset!");
         } else if (!drive.getRawButton(8)) {
             isResetButtonPressed = false;
         }
 
         if (drive.getRawButton(7) && !isRelativeTogglePressed) {
             driver.ToggleRelative();
             isRelativeTogglePressed = true;
             System.out.println("toggled relative!");
         } else if (!drive.getRawButton(7)) {
             isRelativeTogglePressed = false;
         }
 
         if (drive.getRawButton(6)) {
             drawbridge.Open();
         } else if (drive.getRawButton(4)) {
             drawbridge.Close();
         }
 
         if (secControl.getTrigger()) {
             climber.SetArmSpeed(secControl.getY());
         } else {
             climber.SetArmSpeed(0);
         }
 
 
         if (thirdControl.getTrigger()) {
             climber.SetWinchSpeed(thirdControl.getY());
         } else {
             climber.SetWinchSpeed(0);
         }
 
         if (drive.getTrigger()) {
             driver.TemporaryCamRel();
         }
         if (drive.getRawButton(9)) {
             drawbridge.Enable();
         }
         if (drive.getRawButton(10)) {
             drawbridge.Disable();
         }
         if (drive.getRawButton(12)) {
             drawbridge.Disable();
         }
         if (drive.getRawButton(11)) {
             drawbridge.Enable();
         }
         data.speed = Math.max(Math.abs(drive.getX()), Math.abs(drive.getY()));
         data.angle = drive.getDirectionDegrees();
         data.rotationSpeed = rotSpeed;
 
         //AUTO ORIENT
         if (drive.getRawButton(11)) {
             autoDrive.OrientTo(45);
         } else if (drive.getRawButton(12)) {
             autoDrive.OrientTo(360 - 45);
         } else {
             autoDrive.Disable();
         }
         
         if(drive.getRawButton(3)){
             drawbridge.Half();
         }
         if(secControl.getRawButton(7)){
             climber.LiftBox();
         }else if(secControl.getRawButton(6)) {
             climber.LowerBox();
         }
         else{
             climber.StopBox();
         }
         
     }
 }
