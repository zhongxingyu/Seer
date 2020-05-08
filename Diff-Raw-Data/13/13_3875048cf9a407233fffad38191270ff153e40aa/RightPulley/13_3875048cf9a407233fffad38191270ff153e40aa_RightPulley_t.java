 package com.team1160.feathers.subsystems;
 
 import com.team1160.feathers.OI;
 import com.team1160.feathers.api.Constants;
 import com.team1160.feathers.api.DigitalServo;
 import com.team1160.feathers.commands.pulleys.right.RightPulleyAngle;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.command.Subsystem;
 
 public class RightPulley extends Subsystem{
 
 	protected DigitalServo angle;
 	protected Jaguar am;
 	
 	protected Joystick stick;
 	
 	protected double lastSet;
 	
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
 			stick = OI.getInstance().getRightStick();
 		}
 		setAngle((-stick.getY()+1)/2);
 	}
 	// Used for fine tuning the rod angle
 	public void joyAdjust(){
 		if(stick == null){
 			stick = OI.getInstance().getLeftStick();
 		}
 		//z axis is untested just a hunch as of now
		setDeltaAngle(-stick.getZ()/4);
		lastSet = lastSet-(-stick.getZ()/4); //This stops the servo from moving for ever by reseting last to its previous value
 	}
 	
 	public void joyVelocity(){
 		if(stick == null){
 			stick = OI.getInstance().getRightStick();
 		}
 		setVelocity(-stick.getY());
 	}
 }
