 package com.team1160.feathers.subsystems;
 
 import com.team1160.feathers.OI;
 import com.team1160.feathers.api.Constants;
 import com.team1160.feathers.api.DigitalServo;
 import com.team1160.feathers.commands.pulleys.right.RightPulleyAngle;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.Servo;
 import edu.wpi.first.wpilibj.command.Subsystem;
 
 public class RightPulley extends Subsystem{
 
 	protected Servo lock;
 	protected DigitalServo angle;
 	protected Jaguar am;
 	
 	protected Joystick stick;
 	
 	protected static RightPulley instance;
 	
 	public static RightPulley getInstance(){
 		if(instance == null){
 			instance = new RightPulley();
 		}
 		return instance;
 	}
 	
 	protected RightPulley(){
 		angle = new DigitalServo(Constants.P_RIGHT_SERVO_CAR, Constants.P_RIGHT_SERVO_CHAN);
 		am = new Jaguar(Constants.P_RIGHT_JAG_CAR, Constants.P_RIGHT_JAG_CHAN);
 	}	
 	
 	protected void initDefaultCommand() {
 		setDefaultCommand(new RightPulleyAngle());
 	}
 	public void setAngle(double set){
 		this.angle.set(set);
 	}
 	
 	public void setVelocity(double set){
 		this.am.set(set);
 	}
 
 	public void joyAngle(){
 		if(stick == null){
 			stick = OI.getInstance().getRightStick();
 		}
		angle.set((stick.getY()+1)/2);
 	}
 	
 	public void joyVelocity(){
 		if(stick == null){
 			stick = OI.getInstance().getRightStick();
 		}
		am.set(stick.getY());
 	}
 }
