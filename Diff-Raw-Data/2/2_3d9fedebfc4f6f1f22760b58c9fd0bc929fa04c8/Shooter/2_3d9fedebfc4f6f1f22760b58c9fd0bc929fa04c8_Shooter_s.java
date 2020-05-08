 package com.saintsrobotics.frc;
 
 import edu.wpi.first.wpilibj.Counter;
 import edu.wpi.first.wpilibj.CounterBase;
 import edu.wpi.first.wpilibj.CounterBase.EncodingType;
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.SmartDashboard;
 import edu.wpi.first.wpilibj.Timer;
 
 /**
  * The shooter for the robot.
  *
  * @author Saints Robotics
  */
 public class Shooter implements IRobotComponent {
 
     private final int FEEDER_RELAY_CHANNEL = 1;
     private final int FEEDER_DIGITAL_SIDECAR_SLOT = 1;
     private final int FEEDER_DIGITAL_CHANNEL = 1;
     private Relay feeder;
     private DigitalInput feederSwitch;
     private Vision vision;
     private JoystickControl controller;
     private final int ENCODER_DIGITAL_SIDECAR_SLOT = 1;
     private final int ENCODER_DIGITAL_CHANNEL = 2;
     private final double ENCODER_PULSE_DISTANCE = 1.0 / 3;
     private final int ENCODER_AVERAGE_SAMPLES = 25;
     private DigitalInput encoderInput;
     private Counter shooterEncoder;
     private static final int SHOOTER_JAGUAR_CHANNEL = 10;
     private static final boolean SHOOTER_JAGUAR_INVERTED = false;
     private Motor shooterMotor;
     private MovingAverage averageSpeed;
     private boolean lastSwitched;
     private int cycleCounts;
     private double rateCount;
     private double prevTime;
     private double currentSpeed;
 
     public Shooter(Vision vision, JoystickControl controller) {
         this.vision = vision;
         this.controller = controller;
 
         averageSpeed = new MovingAverage(ENCODER_AVERAGE_SAMPLES);
 
         feeder = new Relay(FEEDER_RELAY_CHANNEL);
         feeder.setDirection(Relay.Direction.kForward);
         feederSwitch = new DigitalInput(FEEDER_DIGITAL_SIDECAR_SLOT, FEEDER_DIGITAL_CHANNEL);
 
         encoderInput = new DigitalInput(ENCODER_DIGITAL_SIDECAR_SLOT, ENCODER_DIGITAL_CHANNEL);
         shooterEncoder = new Counter(encoderInput);
         shooterEncoder.setSemiPeriodMode(true);
         //shooterEncoder = new Encoder(encoderInput, encoderInput, false, EncodingType.k2X);
         //shooterEncoder.setDistancePerPulse(ENCODER_PULSE_DISTANCE);
 
         shooterMotor = new Motor(SHOOTER_JAGUAR_CHANNEL, SHOOTER_JAGUAR_INVERTED);
 
         cycleCounts = 0;
         rateCount = 0.0;
         prevTime = 0.0;
         currentSpeed = 0.0;
     }
 
     public void robotDisable() {
         shooterEncoder.stop();
         shooterMotor.motor.disable();
     }
 
     public void robotEnable() {
         shooterEncoder.reset();
         shooterEncoder.start();
         lastSwitched = feederSwitch.get();
         SmartDashboard.putBoolean("Limit", lastSwitched);
     }
 
     public void act() {
         shooterMotor.motor.set(controller.getShooterSpeed());
 
         /*
          * if(shooterEncoder.getRate() * 60 > controller.getShooterSpeed() *
          * 5000) { shooterMotor.motor.set(0); } else {
          * shooterMotor.motor.set(1); }
          */
 
         if (cycleCounts == 5) {
             currentSpeed = 10 * (shooterEncoder.get() - rateCount) / (Timer.getFPGATimestamp() - prevTime);
             rateCount = shooterEncoder.get();
             prevTime = Timer.getFPGATimestamp();
             cycleCounts = 0;
 
         }
         cycleCounts++;
         //averageSpeed.add(shooterEncoder.getRate() * 60);
         //System.out.println((shooterEncoder.getRate()* 60) + " : " + (controller.getShooterSpeed() * 5000));
         if (feederSwitch.get() && !lastSwitched) {
             feeder.set(Relay.Value.kOff);
         } else if (controller.getFeederButton() && currentSpeed > 4000) {
             feeder.set(Relay.Value.kOn);
         }
 
         lastSwitched = feederSwitch.get();
         SmartDashboard.putBoolean("Limit", lastSwitched);
         report();
     }
 
     private void report() {
         DriverStationComm.printMessage(DriverStationLCD.Line.kUser2, 1, "Shoot Spd: " + Double.valueOf(currentSpeed).toString());
         DriverStationComm.printMessage(DriverStationLCD.Line.kUser3, 1, "Shoot Pwr: " + Double.valueOf(controller.getShooterSpeed() * 5000).toString());
         SmartDashboard.putNumber("Shooter Speed", currentSpeed);
         SmartDashboard.putNumber("Shooter Power", controller.getShooterSpeed() * 5000);
     }
 }
