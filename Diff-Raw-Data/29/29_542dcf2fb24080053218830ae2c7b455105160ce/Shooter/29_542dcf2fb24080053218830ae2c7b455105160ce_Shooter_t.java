 package com.saintsrobotics.frc;
 
 import edu.wpi.first.wpilibj.CounterBase;
 import edu.wpi.first.wpilibj.CounterBase.EncodingType;
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Relay;
 
 /**
  * The shooter for the robot.
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
     private final double ENCODER_PULSE_DISTANCE = 1.0/3;
     private final int ENCODER_AVERAGE_SAMPLES = 25;
     
     private DigitalInput encoderInput;
     private Encoder shooterEncoder;
     
     private static final int SHOOTER_JAGUAR_CHANNEL = 10;
     private static final boolean SHOOTER_JAGUAR_INVERTED = false;
     
     private Motor shooterMotor;
     
     private MovingAverage averageSpeed;
     
     private boolean lastSwitched;
     
     public Shooter(Vision vision, JoystickControl controller) {
         this.vision = vision;
         this.controller = controller;
         
         averageSpeed = new MovingAverage(ENCODER_AVERAGE_SAMPLES);
         
         feeder = new Relay(FEEDER_RELAY_CHANNEL);
         feeder.setDirection(Relay.Direction.kForward);
         feederSwitch = new DigitalInput(FEEDER_DIGITAL_SIDECAR_SLOT, FEEDER_DIGITAL_CHANNEL);
         
         encoderInput = new DigitalInput(ENCODER_DIGITAL_SIDECAR_SLOT, ENCODER_DIGITAL_CHANNEL);
         shooterEncoder = new Encoder(encoderInput, encoderInput, false, EncodingType.k1X);
         shooterEncoder.setDistancePerPulse(ENCODER_PULSE_DISTANCE);
         
         shooterMotor = new Motor(SHOOTER_JAGUAR_CHANNEL, SHOOTER_JAGUAR_INVERTED);
     }
 
     public void robotDisable() {
         shooterEncoder.stop();
         shooterMotor.motor.disable();
     }
 
     public void robotEnable() {
         shooterEncoder.reset();
         shooterEncoder.start();
         lastSwitched = feederSwitch.get();
     }
 
     public void act() {
         shooterMotor.motor.set(controller.getShooterSpeed());
         
        /*
        if(controller.getShooterSpeed() * 5000 > averageSpeed.getAverage())
        {
            shooterMotor.motor.set(0);
        }
        else
        {
            shooterMotor.motor.set(1);
        }
        */
        
         averageSpeed.add(shooterEncoder.getRate() * 60);
         System.out.println(shooterEncoder.getRate()* 60);
         if(feederSwitch.get() && !lastSwitched)
         {
             feeder.set(Relay.Value.kOff);
         }
         else if(controller.getFeederButton())
         {
             feeder.set(Relay.Value.kOn);
         }
         
         lastSwitched = feederSwitch.get();
         
         report();
     }
     
     private void report()
     {
         DriverStationComm.printMessage(DriverStationLCD.Line.kUser2, 1, "Shooter Speed: " + Double.valueOf(averageSpeed.getAverage()).toString());
        DriverStationComm.printMessage(DriverStationLCD.Line.kUser3, 1, "Shooter Pwr Spd: " + Double.valueOf(controller.getShooterSpeed() * 5000).toString());
     }
 }
