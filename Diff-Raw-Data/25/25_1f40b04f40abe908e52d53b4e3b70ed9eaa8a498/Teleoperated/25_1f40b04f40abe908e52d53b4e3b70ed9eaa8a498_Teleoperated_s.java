/*  ______ ______ ______ ______
  * |__    |      |__    |  __  |
  * |__    |_     |    __|__    |
  * |______| |____|______|______|
  */
 package edu.first3729.frc2012;
 
 /**
  * @file Teleoperated.java
  */
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.RobotDrive;
 
 /**
  *
  * @class Teleoperated @brief Class that runs robot during Teleoperated period
  *
  * Manages instances of Drive, Input, and Manipulator classes. Gets appropriate
  * input based on controller layout mode and sets outputs accordingly.
  */
 public class Teleoperated {

     private Input _input_manager;
     private RobotDrive _drive;
     private Manipulator _manip;
     private DigitalInput bridge_limit, intake_sensor;
     private double x = 0.0, y = 0.0, z = 0.0, left = 0.0, right = 0.0, scale_factor = 0.0;
     private boolean polarity = false, net_down = false, net_up = false, intake = false, bridge_down = false, bridge_up = false;
     private boolean shoot_on = false, shoot_off = false, lift_on = false, lift_off = false;
 
     /**
      * @param imanager instance of Input class passed from Robot
      * @param drv instance of Drive class passed from Robot
      * @param manip instance of Manipulator class passed from Robot
      * @brief Constructor - takes subsystem classes passed from Robot
      */
     public Teleoperated(Input imanager, RobotDrive drv, Manipulator manip) {
         this._input_manager = imanager;
         this._manip = manip;
         this._drive = drv;
         this.intake_sensor = new DigitalInput(Params.intake_sensor_digin_port);
         this.bridge_limit = new DigitalInput(Params.bridge_limit_digin_port);
     }
 
     /**
      * @brief Initializes manipulator, locks drive, locks input
      */
     public void init() {
         this._drive.tankDrive(0, 0);
         this._manip.init();
     }
 
     /**
      * @brief Runs Teleoperated period Runs Teleoperated period
      */
     public void run() {
         // Update input fields
         this.getInput();
         // Drive teh robot
         this._drive.arcadeDrive(this.x, this.y);
 
         /* If intake limit switch is hit, turn off intake relay
         if (!this.intake_limit.get()) {
             for (int i = 0; i < 10000; i++) { continue; }
             this._manip.intake(Relay.Value.kOff);
         }
         // Intake control   
         if (this.intake) {
             this._manip.intake(Relay.Value.kForward);
         }
         */
         if (!this.intake_sensor.get()) {
             this._manip.intake(false);
         }
         if (this.intake) {
             if (!this.shoot_on)
                 this._manip.shoot(true);
             this._manip.intake(true);
         }
 
         // Gist of the above 2 'if' statements: when intake button pressed,
         // keep relay running until limit switch is pressed.
 
         // Toggle elevator
         if (lift_on) {
             this._manip.lift(true);
         } else if (lift_off) {
             this._manip.lift(false);
         }
 
         // Toggle shooter
         if (shoot_on) {
             this._manip.shoot(true);
         } else if (shoot_off) {
             this._manip.shoot(false);
         }
 
         // If up and down both requested, do nothing
         if (bridge_up && bridge_down) {
             this._manip.bridge(Relay.Value.kOff);
         }
 
         // Bridge relay control
         if (bridge_up && !this.bridge_limit.get()) {
             this._manip.bridge(Relay.Value.kForward);
         } else if (bridge_down) {
             this._manip.bridge(Relay.Value.kReverse);
         } else {
             this._manip.bridge(Relay.Value.kOff);
         }
 
         // If up and down both requested, do nothing
         if (bridge_up && bridge_down) {
             this._manip.bridge(Relay.Value.kOff);
         }
 
         // Ditto for net
         if (net_up) {
             this._manip.lift_net(Relay.Value.kForward);
         } else if (net_down) {
             this._manip.lift_net(Relay.Value.kReverse);
         } else {
             this._manip.lift_net(Relay.Value.kOff);
         }
 
         if (net_up && net_down) {
             this._manip.lift_net(Relay.Value.kOff);
         }
     }
 
     /**
      * @brief Updates local input fields with values read from input devices
      */
     public void getInput() {
         if (this._input_manager.get_twist(1) > 0) {
             this.scale_factor = Params.drive_creep_scale_factor;
         } else {
             this.scale_factor = 1.0;
         }
 
         this.x = this._input_manager.get_x() * scale_factor;
         if (polarity)
             this.y = this._input_manager.get_y() * scale_factor * -1.0;
         else
             this.y = this._input_manager.get_y() * scale_factor;
         this.z = this._input_manager.get_z() * scale_factor;
 
         this.intake = this._input_manager.check_button(0, 1);
         this.lift_on = this._input_manager.check_button(0, 3);
         this.lift_off = this._input_manager.check_button(0, 2);
         this.shoot_on = this._input_manager.check_button(0, 4);
         this.shoot_off = this._input_manager.check_button(0, 5);
         this.bridge_down = this._input_manager.check_button(1, 4);
         this.bridge_up = this._input_manager.check_button(1, 5);
         this.net_up = this._input_manager.check_button(1, 3);
         this.net_down = this._input_manager.check_button(1, 2);
         this.polarity = this._input_manager.check_button(2, 2);
     }
 }
