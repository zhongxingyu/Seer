/*  ______ ______ ______ ______
 * |__    |      |__    |  __  |
 * |__    |_     |    __|__    |
 * |______| |____|______|______|
  */
 package edu.first3729.frc2012;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Relay;
 
 /**
  *
  * @author Teddy
  */
 public class ManipulatorPractice extends Manipulator {
     
     private Jaguar shooter1;
     private Jaguar shooter2;
 
 
     public ManipulatorPractice()
     {
         shooter1 = new Jaguar(Params.shooter1_Jaguar_port);
         shooter2 = new Jaguar(Params.shooter2_Jaguar_port);
         elevator = new Relay(Params.elevator_relay_port);
         bridge = new Relay(Params.bridge_relay_port);
         intake = new Relay(Params.intake_relay_port);
         net = new Relay(Params.net_relay_port);
     }
 
     public void shoot(boolean state) {
         if (state) {
             shooter1.set(Params.shooter1_speed);
             shooter2.set(Params.shooter2_speed);
         } else {
             shooter1.set(0.0);
             shooter2.set(0.0);
         }
 
     }
 }
