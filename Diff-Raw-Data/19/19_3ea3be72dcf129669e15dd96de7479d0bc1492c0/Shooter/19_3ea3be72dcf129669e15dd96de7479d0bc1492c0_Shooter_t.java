 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.northwestrobotics.frc2013.shooter;
 
 import edu.wpi.first.wpilibj.Compressor;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.MotorSafetyHelper;
 import edu.wpi.first.wpilibj.Solenoid;
 import edu.wpi.first.wpilibj.SpeedController;
 import edu.wpi.first.wpilibj.Talon;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import org.northwestrobotics.frc2013.RobotConstants;
 import org.northwestrobotics.frc2013.State;
 import org.northwestrobotics.frc2013.StateMachine;
 /*
  * Shooter
  * =======
  */
 /**
  * This class manages the shooting system.
  *
  * @author soggy.potato
  * @author SilverX
  * @author AgentOrange
  * @author Deven_Gosalia
  */
 public final class Shooter {
     /*
      * Aiming
      * ------
      */
     /**
      * The joystick for obtaining aiming and shooting input.
      */
     private final Joystick aimingStick;
     /**
      * The motor used to adjust the shooter's vertical aiming.
      *
      * @author soggy.potato
      * @author SilverX
      */
     private final SpeedController pitchMotor = new Talon(RobotConstants.Shooting.PITCH_MOTOR);
     /*
      * Shooting
      * --------
      */
     
     /**
      * The motor used to launch a frisbee at targets.
      *
      * @author soggy.potato
      * @author SilverX
      * @author AgentOrange
      */
     private final SpeedController shootMotor = new Talon(RobotConstants.Shooting.SHOOT_MOTOR);
     /*
      * Pneumatics
      * ----------
      */
     /**
      * Pneumatic arm to push frisbees into shooter
      *
      * @author AgentOrange
      */
     private final Solenoid feeder = new Solenoid(RobotConstants.Pneumatics.FEEDER_CHANNEL);
     private final Compressor airCompressor = new Compressor(RobotConstants.Pneumatics.PRESSURE_SWITCH_VALUE,
             RobotConstants.Pneumatics.COMPRESSOR_RELAY);
     /*
      * State machine
      * -------------
      */
     // ### States
     private final State awaitingUserInputState = new AwaitingUserInputState(this);
     private final State shootingState = new ShootingState(this);
     // ### Machine
     private final StateMachine shootingStateMachine = new StateMachine(awaitingUserInputState);
 
     public Shooter(Joystick aimingStick) {
         this.aimingStick = aimingStick;
         airCompressor.start();
     }
 
     State getAwaitingUserInputState() {
         return awaitingUserInputState;
     }
 
     State getShootingState() {
         return shootingState;
     }
 
     /**
      * Adjusts vertical aiming in accordance to user input.
      * @author AgentOrange
      * @author soggy.potato
      */
     public void adjustAim() {
         // User uses controller to aim. Read in this user input.
         // If the y is zero pitch arm stops moving
         /*
          * TODO: Find out the sign for the Y value, and adjust the pitch factor
          * from testing. Perform a range check before calling the set method.
          * May have to change sign.
          */
         /*
          * Read in the pitch asjustment the user has made with the joystick.
          * Scale the value so that it is not to fast for the user to control.
          */
         double pitchAdjustment = -aimingStick.getY() * RobotConstants.Shooting.PITCH_FACTOR;
         
         /* 
          * Adjust the adjustment we are about to make when it is going in certain 
          * directions to counteract weight differences on sides of the shooter
          * component.
          */
         //if (pitchAdjustment < 0) {
             pitchAdjustment *= 5;
        // } else {
            
        // }
         
         /*
          * Tell the motor to make the pitch asjustment.
          */
         pitchMotor.set(pitchAdjustment);
   
                 
 
     }
 
     /**
      * Determines whether the driver has commanded the robot to shoot a frisbee.
      * @author soggy.potato
      */
     boolean isShootButtonPressed() {
         // Determine whether the shoot button is pressed or not.
         return aimingStick.getRawButton(RobotConstants.Shooting.SHOOT_BUTTON);
     }
     /**
      * Updates the shooting state machine.
      * @author soggy.potato
      */
     public void updateShooting() {
         shootingStateMachine.update();
         // determine whether user wants to activate the shoot motor
         if (isActivateShootMotorButtonPressed()) {
             /*
              * Turn on the shoot motor. The value is negated because it has to
              * rotate in the inverse direction to correctly launch the frisbee.
              */
             shootMotor.set(-getShootMotorSpeed());
              
         } else {
             shootMotor.set(0); // the user
         }
     }
 
     public void updatePressure() {
         if (airCompressor.getPressureSwitchValue() && airCompressor.enabled()) {
             airCompressor.stop();
         }
        else if (!airCompressor.enabled() && !airCompressor.getPressureSwitchValue()) {
             airCompressor.start();
         }
 
 
     }
 
    
     // Util
     SpeedController getShootMotor() {
         return shootMotor;
     }
 
    public Solenoid getFeeder() {
         return feeder;
     }
     
     private double getShootMotorSpeed() {
         return Math.abs(aimingStick.getAxis(Joystick.AxisType.kZ));
     }
     
     private boolean isActivateShootMotorButtonPressed() {
         return aimingStick.getRawButton(RobotConstants.Shooting.TOGGLE_SHOOT_MOTOR_BUTTON);
     }
 
     public void deactivateShootMotorForAutonomous() {
        shootMotor.set(0);
     }
 
     public void activateShootMotorForAutonomous() {
        shootMotor.set(-1);
     }
 }
