 package com.saintsrobotics.frc;
 
 import edu.wpi.first.wpilibj.CANJaguar;
 import edu.wpi.first.wpilibj.CounterBase;
 import edu.wpi.first.wpilibj.CounterBase.EncodingType;
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.can.CANTimeoutException;
 
 /**
  * The drive system for the robot.
  * @author Saints Robotics
  */
 public class Drive implements IRobotComponent {
     // Constants
     /*
     private static final int JAGUAR_FRONT_LEFT_ID = 1;
     private static final int JAGUAR_FRONT_RIGHT_ID = 1;
     private static final int JAGUAR_BACK_LEFT_ID = 2;
     private static final int JAGUAR_BACK_RIGHT_ID = 2;
     
     private static final boolean JAGUAR_FRONT_LEFT_INVERTED = false;
     private static final boolean JAGUAR_FRONT_RIGHT_INVERTED = false;
     private static final boolean JAGUAR_BACK_LEFT_INVERTED = false;
     private static final boolean JAGUAR_BACK_RIGHT_INVERTED = false;
     
     private static final CANJaguar.ControlMode CANJAGUAR_CONTROL_MODE =
             CANJaguar.ControlMode.kPercentVbus;
     private static final CANJaguar.NeutralMode CANJAGUAR_NEUTRAL_MODE =
             CANJaguar.NeutralMode.kBrake;
     */
     
     private static final int JAGUAR_LEFT_ID = 1;
     private static final int JAGUAR_RIGHT_ID = 2;
     
     private static final boolean JAGUAR_LEFT_INVERTED = true;
     private static final boolean JAGUAR_RIGHT_INVERTED = false;
     
     private static final int ENCODER_DIGITAL_SIDECAR_CHANNEL = 1;
     private static final int ENCODER_LEFT_CHANNEL = 3;
     private static final int ENCODER_RIGHT_CHANNEL = 4;
     
     private static final double ENCODER_CODES_PER_REV = 10;
     private static final double ENCODER_GEARING_RATIO = 10;
     
     private JoystickControl controller;
     
     // Instance variables
     /*
     private Motor frontLeftMotor;
     private Motor frontRightMotor;
     private Motor backLeftMotor;
     private Motor backRightMotor;
     */
     
     private Motor leftMotor;
     private Motor rightMotor;
     
     private Encoder leftEncoder;
     private DigitalInput leftEncoderInput;
     private Encoder rightEncoder;
     private DigitalInput rightEncoderInput;
     
     public Drive(JoystickControl controller) {
         /*
         frontLeftMotor = new Motor(JAGUAR_FRONT_LEFT_ID,
                 JAGUAR_FRONT_LEFT_INVERTED);
         frontRightMotor = new Motor(JAGUAR_FRONT_RIGHT_ID,
                 JAGUAR_FRONT_RIGHT_INVERTED);
         backLeftMotor = new Motor(JAGUAR_BACK_LEFT_ID,
                 JAGUAR_BACK_LEFT_INVERTED);
         backRightMotor = new Motor(JAGUAR_BACK_RIGHT_ID,
                 JAGUAR_BACK_RIGHT_INVERTED);
         */
         
         leftMotor = new Motor(JAGUAR_LEFT_ID, JAGUAR_LEFT_INVERTED);
         rightMotor = new Motor(JAGUAR_RIGHT_ID, JAGUAR_RIGHT_INVERTED);
         
         leftEncoderInput = new DigitalInput(ENCODER_DIGITAL_SIDECAR_CHANNEL, ENCODER_LEFT_CHANNEL);
         rightEncoderInput = new DigitalInput(ENCODER_DIGITAL_SIDECAR_CHANNEL, ENCODER_RIGHT_CHANNEL);
         
         leftEncoder = new Encoder(leftEncoderInput, leftEncoderInput, false, EncodingType.k1X);
         rightEncoder = new Encoder(rightEncoderInput, rightEncoderInput, false, EncodingType.k1X);
         
         this.controller = controller;
         
         //init();
     }
     
     /*
     private void init() {
         try {
             // Set all motors to have the same modes
             frontLeftMotor.motor.changeControlMode(CANJAGUAR_CONTROL_MODE);
             frontRightMotor.motor.changeControlMode(CANJAGUAR_CONTROL_MODE);
             backLeftMotor.motor.changeControlMode(CANJAGUAR_CONTROL_MODE);
             backRightMotor.motor.changeControlMode(CANJAGUAR_CONTROL_MODE);
             
             frontLeftMotor.motor.configNeutralMode(CANJAGUAR_NEUTRAL_MODE);
             frontRightMotor.motor.configNeutralMode(CANJAGUAR_NEUTRAL_MODE);
             backLeftMotor.motor.configNeutralMode(CANJAGUAR_NEUTRAL_MODE);
             backRightMotor.motor.configNeutralMode(CANJAGUAR_NEUTRAL_MODE);
         }
         catch (Exception exception) {
             Logger.log(exception);
         }
     }
     */
     
     public void act()
     {
         if(controller.getControlMode().value == JoystickControl.ControlMode.arcadeDrive.value)
         {
             arcadeDrive(controller.getArcadeValues());
         }
         else if(controller.getControlMode().value == JoystickControl.ControlMode.tankDrive.value)
         {
             tankDrive(controller.getTankValues());
         }
         
         else
         {
             arcadeDrive(controller.getArcade1Values());
         }
                 
     }
     
     //Index 0: left side motor value
     //Index 1: right side motor value
     public void tankDrive(double[] motorValues)
     {
         tankDrive(motorValues[0], motorValues[1]);
     }
     
     public void tankDrive(double leftValue, double rightValue) {
         leftValue = limit(leftValue);
         rightValue = limit(rightValue);
         
         try {
             /*
             frontLeftMotor.motor.set(frontLeftMotor.invert() * leftValue);
             frontRightMotor.motor.set(frontRightMotor.invert() * rightValue);
             backLeftMotor.motor.set(backLeftMotor.invert() * leftValue);
             backRightMotor.motor.set(backRightMotor.invert() * rightValue);
             */
             
             leftMotor.motor.set(leftMotor.invert() * leftValue);
             rightMotor.motor.set(rightMotor.invert() * rightValue);
         }
         catch (Exception exception) {
             Logger.log(exception);
         }
     }
     
     //Index 0: throttle value
     //Index 1: turning value
     public void arcadeDrive(double[] motorValues)
     {
         arcadeDrive(motorValues[0], motorValues[1]);
     }
     
     public void arcadeDrive(double moveValue, double rotateValue) {
        double leftValue = moveValue - rotateValue;
        double rightValue = moveValue + rotateValue;
         double[] motorValues = scale( new double[]{ leftValue, rightValue } );
         
         System.out.println(leftValue + " : " + rightValue + " :: " + motorValues);
         
         try
         {
             /*
             frontLeftMotor.motor.set(frontLeftMotor.invert() * motorValues[0]);
             frontRightMotor.motor.set(frontRightMotor.invert() * motorValues[1]);
             backLeftMotor.motor.set(backLeftMotor.invert() * motorValues[0]);
             backRightMotor.motor.set(backRightMotor.invert() * motorValues[1]);
             */
             
             leftMotor.motor.set(leftMotor.invert() * motorValues[0]);
             rightMotor.motor.set(rightMotor.invert() * motorValues[1]);
         }
         catch (Exception e)
         {
             Logger.log(e);
         }
     }
     
     public void stopDrive() {
         try {
             /*
             frontLeftMotor.motor.set(0);
             frontRightMotor.motor.set(0);
             backLeftMotor.motor.set(0);
             backRightMotor.motor.set(0);
             */
             
             leftMotor.motor.set(0);
             rightMotor.motor.set(0);
         }
         catch (Exception exception) {
             Logger.log(exception);
         }
     }
     
     public double limit(double value) {
         if (value > 1) {
             return 1;
         }
         if (value < -1) {
             return -1;
         }
         
         return value;
     }
     
     public double[] scale (double[] values)
     {
         double scale = 1.0;
         double newValues[] = new double[values.length];
         
         for(int i = 0; i < values.length; i++)
         {
             double currentScale = 1 / Math.abs(values[i]);
             scale = scale > currentScale ? currentScale : scale;
         }
         
         if(scale < 1.0)
         {
             for(int i = 0; i < values.length; i++)
             {
                 newValues[i] = scale * values[i];
             }
             
             return newValues;
         }
         else
         {
             return values;
         }
     }
 
     public void robotDisable() {
         leftEncoder.stop();
         rightEncoder.stop();
         
         /*
         frontLeftMotor.motor.disable();
         frontRightMotor.motor.disable();
         backLeftMotor.motor.disable();
         backRightMotor.motor.disable();
         */
         
         leftMotor.motor.disable();
         rightMotor.motor.disable();
         
         /*
         try
         {
             frontLeftMotor.motor.disableControl();
             frontRightMotor.motor.disableControl();
             backLeftMotor.motor.disableControl();
             backRightMotor.motor.disableControl();
         }
         catch (CANTimeoutException e)
         {
             Logger.log(e);
         }
         */
     }
 
     public void robotEnable() {
         leftEncoder.reset();
         leftEncoder.start();
         
         rightEncoder.reset();
         rightEncoder.start();
         
         /*
         try
         {
             frontLeftMotor.motor.enableControl();
             frontRightMotor.motor.enableControl();
             backLeftMotor.motor.enableControl();
             backRightMotor.motor.enableControl();
         }
         catch (CANTimeoutException e)
         {
             Logger.log(e);
         }
         */
     }
 }
