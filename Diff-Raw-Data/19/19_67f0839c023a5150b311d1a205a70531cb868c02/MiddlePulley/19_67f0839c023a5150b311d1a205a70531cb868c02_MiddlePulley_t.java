 package com.team1160.feathers.subsystems;
 
 import com.team1160.feathers.OI;
 import com.team1160.feathers.api.Constants;
 import com.team1160.feathers.api.DigitalServo;
 import com.team1160.feathers.commands.pulleys.middle.MiddleNothing;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.command.Subsystem;
 
 public class MiddlePulley extends Subsystem{
 
 	protected DigitalServo angle;
 	protected Jaguar am;
 
 	protected Joystick stick;
 	
 	protected static MiddlePulley instance;
 	
 	public static MiddlePulley getInstance(){
 		if(instance == null){
 			instance = new MiddlePulley();
 		}
 		return instance;
 	}
 	
 	protected MiddlePulley(){
 		angle = new DigitalServo(Constants.P_TOP_SERVO_CAR, Constants.P_TOP_SERVO_CHAN);
 		am = new Jaguar(Constants.P_TOP_JAG_CAR, Constants.P_TOP_JAG_CHAN);
 	}
 	
 	
 	protected void initDefaultCommand() {
 		setDefaultCommand(new MiddleNothing());
 	}
 	
 	public void setAngle(double set){
 		this.angle.set(set);
 	}
 	
 	public void setVelocity(double set){
 		this.am.set(set);
 	}
 
 	public void joyAngle(){
 		if(stick == null){
 			stick = OI.getInstance().getDriveStick();
 		}
		angle.set((-stick.getY()+1)/2);
 	}
 	
 	public void joyVelocity(){
 		if(stick == null){
 			stick = OI.getInstance().getDriveStick();
 		}
 		am.set(stick.getY());
 	}
 
 	
 }
