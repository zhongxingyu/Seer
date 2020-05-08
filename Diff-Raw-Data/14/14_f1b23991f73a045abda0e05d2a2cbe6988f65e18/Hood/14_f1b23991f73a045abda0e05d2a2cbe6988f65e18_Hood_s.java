 package edu.wpi.first.wpilibj.templates.subsystems;
 
 import edu.wpi.first.wpilibj.Utility;
 import edu.wpi.first.wpilibj.templates.RobotMap;
 import edu.wpi.first.wpilibj.templates.commands.CommandBase;
 //import edu.wpi.first.wpilibj.templates.commands.MoveHood;
 
 import edu.wpi.first.wpilibj.Victor;
 import edu.wpi.first.wpilibj.command.PIDSubsystem;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 public class Hood extends PIDSubsystem {
 
     Victor hood;
 
     public Hood() {
         super("HoodAngle", RobotMap.HOOD_P_GAIN, RobotMap.HOOD_I_GAIN, RobotMap.HOOD_D_GAIN, RobotMap.HOOD_PID_PERIOD);
        
        
         this.setSetpointRange(RobotMap.HOOD_ANGLE_MIN, RobotMap.HOOD_ANGLE_MAX);
         this.setSetpoint((RobotMap.HOOD_ANGLE_MIN + RobotMap.HOOD_ANGLE_MAX) / 2);
         this.getPIDController().setTolerance(RobotMap.HOOD_PID_TOLERANCE);
         hood = new Victor(RobotMap.MOTOR_HOOD);
     }
 
     public void initDefaultCommand() {
     }
 
     protected double returnPIDInput() {
         return CommandBase.globalState.getHoodAngle();
     }
 
     protected void usePIDOutput(double output) {
        SmartDashboard.putDouble("Hood Command Speed", output);
         SmartDashboard.putDouble("Hood Setpoint", this.getSetpoint());
        SmartDashboard.putDouble("Hood Update Time", Utility.getFPGATime());
 
         // Prevent hood overruns
         // The mistmatch bewteen MAX and command direction is intentional -- the hood runs backwards with respect to the pot
         if ((CommandBase.globalState.getHoodAngle() >= RobotMap.HOOD_ANGLE_MAX && output < 0)
                || (CommandBase.globalState.getHoodAngle() <= RobotMap.HOOD_ANGLE_MIN && output < 0)) {
             output = 0;
         }
         hood.set(output);
     }
 
     public void adjustSetpoint(double delta) {
         this.setSetpoint(this.getSetpoint() + delta);
         if (this.getSetpoint() > RobotMap.HOOD_ANGLE_MAX) {
             this.setSetpoint(RobotMap.HOOD_ANGLE_MAX);
         } else if (this.getSetpoint() < RobotMap.HOOD_ANGLE_MIN) {
             this.setSetpoint(RobotMap.HOOD_ANGLE_MIN);
         }
         start();
     }
 
     public void start() {
         this.enable();
     }
 
     public void stop() {
         this.disable();
         hood.stopMotor();
     }
 }
