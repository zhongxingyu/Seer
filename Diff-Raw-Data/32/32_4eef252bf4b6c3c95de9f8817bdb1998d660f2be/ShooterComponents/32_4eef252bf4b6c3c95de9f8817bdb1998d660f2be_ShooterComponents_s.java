 package com.edinarobotics.zephyr.parts;
 
 import com.edinarobotics.utils.sensors.FilterDouble;
 import com.edinarobotics.utils.sensors.SimpleAverageFilter;
 import com.edinarobotics.zephyr.Zephyr;
 import edu.wpi.first.wpilibj.*;
 
 /**
  *The wrapper for the shooter components, contains the 2 jaguars driving the shooter
  * along with the rotating jaguar and the piston.
  */
 public class ShooterComponents{
     public static final int ROTATE_RIGHT_SIGN = 1;
     public static final int ROTATE_LEFT_SIGN = -1;
     public static final int ENCODER_TICKS_PER_REV = 180;
    public static final double MAX_SHOOTER_SPEED = 8000;
     public static final double MIN_SHOOTER_SPEED = 0;
     
     private CANJaguar shooterLeftJaguar;
     private CANJaguar shooterRightJaguar;
     private Jaguar shooterRotator;
     private Relay ballLoadPiston;
     private DigitalInput leftLimitSwitch;
     private DigitalInput rightLimitSwitch;
     private FilterDouble filter;
     private final double P = 1.5;
     private final double I = .015;
     private final double D = .25;
     /**
      * Constructs shooterLeftJaguar, shooterRightJaguar, shooterRotator and ballLoadPiston
      * with leftJaguar, rightJaguar, rotator and piston respectively.
      */
     public ShooterComponents(int leftJaguar, int rightJaguar, int rotator, int piston,
                              int leftLimitSwitch, int rightLimitSwitch){
         try{
             shooterLeftJaguar = new CANJaguar(leftJaguar, CANJaguar.ControlMode.kSpeed);
             shooterRightJaguar = new CANJaguar(rightJaguar, CANJaguar.ControlMode.kVoltage);
             System.out.println("Success!");
         }catch(Exception e){
             e.printStackTrace();
             Zephyr.exceptionProblem = true;
         }
         System.out.println("CAN setup done.");
         try{
             shooterLeftJaguar.configEncoderCodesPerRev(ENCODER_TICKS_PER_REV);
         }catch(Exception e){
             e.printStackTrace();
             Zephyr.exceptionProblem = true;
         }
         try{
             shooterLeftJaguar.setPID(P, I, D);
             shooterLeftJaguar.setSafetyEnabled(false);
             shooterLeftJaguar.setSpeedReference(CANJaguar.SpeedReference.kEncoder);
             shooterRightJaguar.setSafetyEnabled(false);
             shooterLeftJaguar.enableControl();
             shooterRightJaguar.enableControl();
         }catch(Exception e){
             e.printStackTrace();
             Zephyr.exceptionProblem = true;
         }
         shooterRotator = new Jaguar(rotator);
         ballLoadPiston = new Relay(piston);
         this.leftLimitSwitch = new DigitalInput(leftLimitSwitch);
         this.rightLimitSwitch = new DigitalInput(rightLimitSwitch);
     }
     
     /**
      * Initializes shooterLeftJaguar, shooterRightJaguar, shooterRotator, ballPistonLoader
      * leftLimitSwitch, rightLimitSwitch, and filter with leftJaguar, rightJaguar, 
      * rotator piston, and filterTaps respectively. 
      * 
      */
     public ShooterComponents(int leftJaguar, int rightJaguar, int rotator, int piston,
                              int leftLimitSwitch, int rightLimitSwitch, int filterTaps){
         filter = new SimpleAverageFilter(filterTaps);
         try{
             shooterLeftJaguar = new CANJaguar(leftJaguar, CANJaguar.ControlMode.kSpeed);
             shooterRightJaguar = new CANJaguar(rightJaguar, CANJaguar.ControlMode.kVoltage);
             System.out.println("Success!");
         }catch(Exception e){
             e.printStackTrace();
             Zephyr.exceptionProblem = true;
         }
         System.out.println("CAN setup done.");
         try{
             shooterLeftJaguar.configEncoderCodesPerRev(ENCODER_TICKS_PER_REV);
         }catch(Exception e){
             e.printStackTrace();
             Zephyr.exceptionProblem = true;
         }
         try{
             shooterLeftJaguar.setPID(P, I, D);
             shooterLeftJaguar.setSafetyEnabled(false);
             shooterLeftJaguar.setSpeedReference(CANJaguar.SpeedReference.kEncoder);
             shooterRightJaguar.setSafetyEnabled(false);
             shooterLeftJaguar.enableControl();
             shooterRightJaguar.enableControl();
         }catch(Exception e){
             e.printStackTrace();
             Zephyr.exceptionProblem = true;
         }
         shooterRotator = new Jaguar(rotator);
         ballLoadPiston = new Relay(piston);
         this.leftLimitSwitch = new DigitalInput(leftLimitSwitch);
         this.rightLimitSwitch = new DigitalInput(rightLimitSwitch);
         filter = new SimpleAverageFilter(300);
     }
     /**
      * Sets the current setpoint value for the shooter Jaguars. Scaling and
      * units depend on the current control mode of the Jaguars.
      */
     public void setSpeed(double speed){
         try{
             shooterLeftJaguar.setX(-1*speed);
             shooterRightJaguar.setX(-1*shooterLeftJaguar.getOutputVoltage());
             System.out.println("Speed set: "+shooterRightJaguar.getX()+"  -  "+shooterLeftJaguar.getOutputVoltage());
         }catch(Exception e){
             e.printStackTrace();
         }
     }
     
     /**
      * Changes the control mode of the shooter Jaguars to the given
      * CANJaguar.ControlMode value.
      * @param controlMode The new control mode for the shooter Jaguars.
      */
     public void setShooterControlMode(CANJaguar.ControlMode controlMode){
         try{
             shooterLeftJaguar.changeControlMode(controlMode);
             shooterRightJaguar.changeControlMode(CANJaguar.ControlMode.kVoltage);
         }catch(Exception e){
             e.printStackTrace();
             Zephyr.exceptionProblem = true;
         }
     }
     
     /**
      * Sets the rotator to speed
      */
     public void rotate(double speed){
         if((sgn(speed) == ROTATE_LEFT_SIGN && !(leftLimitSwitch.get())) || 
            (sgn(speed) == ROTATE_RIGHT_SIGN) && !(rightLimitSwitch.get())){
             //Limit switches are pressed, stop rotating.
             shooterRotator.set(0);
             return;
         }
         shooterRotator.set(speed);
     }
     /**
      * Sets the piston up if position is true, else it lowers it.
      */
     public void firePiston(boolean position){
         ballLoadPiston.set((position ? Relay.Value.kForward :Relay.Value.kReverse));
     }
     
     /**
      * Uses a function of battery voltage to convert a desired velocity into
      * a PWM value that can be used for the shooter.
      * @param desiredVelocity The velocity in rotations per second to target.
      * @return A PWM value that can be used to approximately reach the
      * desired velocity.
      */
     public static double getVoltagePWM(double desiredVelocity){
         double targetVoltage = 0.1377905889*desiredVelocity+0.1398396921;
         double batteryVoltage = DriverStation.getInstance().getBatteryVoltage();
         if(batteryVoltage == 0.0){
             return 1;
         }
         return targetVoltage/batteryVoltage;
     }
     
     /**
      * Returns the current, raw value from the encoder attached to the
      * shooter jaguar. If an error occurs {@code -1} is returned.
      * @return The raw speed given by the shooter encoder, or {@code 0} if an
      * error occurs.
      */
     public double getEncoderValue(){
         try{
             return shooterLeftJaguar.getSpeed();
         }catch(Exception e){
             e.printStackTrace();
             Zephyr.exceptionProblem = true;
         }
         return 0;
     }
     
     public double getFilteredEncoderValue(){
         try{
             return filter.filter(shooterLeftJaguar.getSpeed());
         }catch(Exception e){
             getEncoderValue();
             e.printStackTrace();
         }
         return 0;
     }
     
     /**
      * A very simple implementation of the sgn function. Returns
      * {@code 1},{@code -1} or {@code 0} representing the sign of the number.
      * @param num The number whose sign will be returned.
      * @return {@code -1} if the number is negative,
      * {@code 0} if the number is 0, or {@code 1} if the number is positive.
      */
     private int sgn(double num){
         if(num>0){
             return 1;
         }
         else if(num<0){
             return -1;
         }
         return 0;
     }
 }
