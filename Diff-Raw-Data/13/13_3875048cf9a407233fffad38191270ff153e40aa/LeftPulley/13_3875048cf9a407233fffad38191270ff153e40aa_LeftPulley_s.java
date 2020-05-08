 package com.team1160.feathers.subsystems;
 
 import com.team1160.feathers.OI;
 import com.team1160.feathers.api.Constants;
 import com.team1160.feathers.api.DigitalServo;
 import com.team1160.feathers.commands.pulleys.left.LeftPulleyAngle;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.command.Subsystem;
 
 public class LeftPulley extends Subsystem{
 
 	protected Joystick stick;
 	
 	protected DigitalServo angle;
 	protected Jaguar am;
 	
 	protected double lastSet;
 	
 	protected static LeftPulley instance;
 	
 	public static LeftPulley getInstance(){
 		if(instance == null){
 			instance = new LeftPulley();
 		}
 		return instance;
 	}
 	
 	protected LeftPulley(){
 		angle = new DigitalServo(Constants.P_LEFT_SERVO_CAR, Constants.P_LEFT_SERVO_CHAN);
 		am = new Jaguar(Constants.P_LEFT_JAG_CAR, Constants.P_LEFT_JAG_CHAN);
 	}
 	
 	protected void initDefaultCommand() {
 		setDefaultCommand(new LeftPulleyAngle());
 	}
 	
 	/* 
 	 * Set angle and set velocity are our final output destitation 
 	 * for every command, this allows us to do stuff like final range
 	 * checks with out having to repeat our selfs
 	 */
 	
 	public void setAngle(double set){
 		this.angle.set(set);
 		this.lastSet = set;
 	}
 	
 	public void setVelocity(double set){
 		this.am.set(set);
 	}
 	
 	public void setDeltaAngle(double delta){
 		setAngle(lastSet+delta);
 	}
 	
 	public void joyAngle(){
 		if(stick == null){
 			stick = OI.getInstance().getLeftStick();
 		}
 		setAngle((stick.getY()+1)/2);
 	}
 	
 	// Used for fine tuning the rod angle
 	public void joyAdjust(){
 		if(stick == null){
 			stick = OI.getInstance().getLeftStick();
 		}
 		//z axis is untested just a hunch as of now
		setDeltaAngle(stick.getZ()/3);
		lastSet = lastSet -(stick.getZ()/3); //This stops the servo from moving for ever by reseting last to its previous value
 	}
 	
 	public void joyVelocity(){
 		if(stick == null){
 			stick = OI.getInstance().getLeftStick();
 		}
 		setVelocity(-stick.getY());
 	}
 }
