 package edu.wpi.first.wpilibj.templates.vstj;
 
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 
 /**
  * This class holds variables about JoySticks.
  *
  * It will give different output (by using different mapping) depending on
  * whether the user has selected to use the XBox controller or the logitech
  * controller.
  */
 public class VstJ {
 
     private static Joystick[] joySticks;
 
     protected static void joyStickInit() {
         joySticks = new Joystick[3];
         for (int i = 1; i < joySticks.length; i++) {
             joySticks[i] = new Joystick(i);
         }
     }
 
     private static Joystick getJoystick(int number) {
         if (joySticks == null) {
             joyStickInit();
         }
         if (number < 1 || number > 2) {
             throw new IllegalArgumentException("To High/Low number for getJoystick()");
         }
         return joySticks[number];
     }
 
     private static JoystickButton getJoystickButton(int joystickNumber, int buttonNumber) {
         return new JoystickButton(getJoystick(joystickNumber), buttonNumber);
     }
 
     private static double getJoystickAxis(int joyStick, int axisNumber) {
         return getJoystick(joyStick).getRawAxis(axisNumber);
     }
 
     public static Joystick getDriveJoystick() {
         return getJoystick(FV.DRIVE_JOYSTICK);
     }
 
     public static Joystick getShooterJoystick() {
         return getJoystick(FV.SHOOTER_JOYSTICK);
     }
 
     public static double getClimberWedgeSolenoidControlAxisValue() {
         return getJoystickAxis(Mappings.climberWedgeSolenoidControlAxisJoystickNumber, Mappings.climberWedgeSolenoidControlNumber);
     }
 
     public static double getLadderControlAxisValue() {
         return getJoystickAxis(Mappings.ladderControlAxisJoystickNumber, Mappings.ladderControlAxisNumber);
     }
 
     public static JoystickButton getShooterSolenoidPushButton() {
         return getJoystickButton(Mappings.shooterSolenoidButtonJoystickNumber, Mappings.shooterSolenoidButtonNumber);
     }
 
     public static boolean getShooterSolenoidPushButtonValue() {
         return getShooterSolenoidPushButton().get();
     }
 
     public static JoystickButton getShooterMotorSpeedUpButton() {
         return getJoystickButton(Mappings.shooterMotorSpeedUpButtonJoystickNumber, Mappings.shooterMotorSpeedUpButtonNumber);
     }
 
     public static boolean getShooterMotorSpeedUpButtonValue() {
         return getShooterMotorSpeedUpButton().get();
     }
 
     public static JoystickButton getShooterMotorSpeedDownButton() {
         return getJoystickButton(Mappings.shooterMotorSpeedDownButtonJoystickNumber, Mappings.shooterMotorSpeedDownButtonNumber);
     }
 
     public static boolean getShooterMotorSpeedDownButtonValue() {
         return getShooterMotorSpeedDownButton().get();
     }
 
     public static JoystickButton getDriveSpeedToggleButton() {
         return getJoystickButton(Mappings.driveSpeedToggleButtonJoystickNumber, Mappings.driveSpeedToggleButtonNumber);
     }
 
     public static boolean getDriveSpeedToggleButtonValue() {
         return getDriveSpeedToggleButton().get();
     }
 
     public static JoystickButton getClimberArmStartToggleButton() {
         return getJoystickButton(Mappings.climberArmSolenoidStartButtonJoystickNumber, Mappings.climberArmSolenoidStartButtonNumber);
     }
 
     public static boolean getClimberArmStartToggleButtonValue() {
         return getClimberArmStartToggleButton().get();
     }
 
     public static JoystickButton getDriveControlReverseButton() {
         return getJoystickButton(Mappings.driveControlReverseButtonJoystickNumber, Mappings.driveControlReverseButtonNumber);
     }
 
     public static boolean getDriveControlReverseButtonValue() {
         return getDriveControlReverseButton().get();
     }
 
     public static JoystickButton getFrisbeeDumpButton() {
        return getJoystickButton(Mappings.frisbeeDumpButtonJoystickNumber, Mappings.frisbeeDumpButtonNumber);
     }
 
     public static boolean getFrisbeeDumpButtonValue() {
         return getFrisbeeDumpButton().get();
     }
 
     /**
      * Mappings for joysticks.
      */
     private static class Mappings {
 
         protected static final int climberArmSolenoidStartButtonJoystickNumber = FV.SHOOTER_JOYSTICK;
         protected static final int climberArmSolenoidStartButtonNumber = FV.BUTTON.STAND_OF_JOYSTICK.BOTTOM_LEFT;
         //
         protected static final int shooterSolenoidButtonJoystickNumber = FV.SHOOTER_JOYSTICK;
         protected static final int shooterSolenoidButtonNumber = FV.BUTTON.TOP_OF_JOYSTICK.TRIGGER;
         //
         protected static final int shooterMotorSpeedUpButtonJoystickNumber = FV.SHOOTER_JOYSTICK;
         protected static final int shooterMotorSpeedUpButtonNumber = FV.BUTTON.TOP_OF_JOYSTICK.RIGHT;
         //
         protected static final int shooterMotorSpeedDownButtonJoystickNumber = FV.SHOOTER_JOYSTICK;
         protected static final int shooterMotorSpeedDownButtonNumber = FV.BUTTON.TOP_OF_JOYSTICK.LEFT;
         //
         protected static final int driveSpeedToggleButtonJoystickNumber = FV.DRIVE_JOYSTICK;
         protected static final int driveSpeedToggleButtonNumber = FV.BUTTON.TOP_OF_JOYSTICK.MIDDLE;
         //
         protected static final int ladderControlAxisJoystickNumber = FV.SHOOTER_JOYSTICK;
         protected static final int ladderControlAxisNumber = FV.AXIS.Y;
         //
         protected static final int driveControlReverseButtonJoystickNumber = FV.DRIVE_JOYSTICK;
         protected static final int driveControlReverseButtonNumber = FV.BUTTON.TOP_OF_JOYSTICK.BOTTOM;
         //
         protected static final int climberWedgeSolenoidControlAxisJoystickNumber = FV.DRIVE_JOYSTICK;
         protected static final int climberWedgeSolenoidControlNumber = FV.AXIS.Y;
         //
         protected static final int frisbeeDumpButtonJoystickNumber = FV.SHOOTER_JOYSTICK;
         protected static final int frisbeeDumpButtonNumber = FV.BUTTON.STAND_OF_JOYSTICK.RIGHT_TOP;
     }
 }
