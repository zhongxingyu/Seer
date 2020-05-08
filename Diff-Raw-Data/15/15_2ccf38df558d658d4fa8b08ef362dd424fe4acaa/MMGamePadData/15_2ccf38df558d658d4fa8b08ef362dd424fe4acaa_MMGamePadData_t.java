 package org.usfirst.frc1318.minimike.shared;
 
 public class MMGamePadData {
 		
	private boolean leftButton = false;
	private boolean rightButton = false;
	private boolean autonomousButton = false;
 	
 	public boolean getLeftButton(){
 		return leftButton;
 	}
 	
 	public void setLeftButton(boolean newValue){
 		leftButton = newValue;
 	}
 	
 	public boolean getRightButton(){
 		return rightButton;
 	}
 	
 	public void setRightButton(boolean newValue){
 		rightButton = newValue;
 	}
 	
 	public boolean getAutonomousButton(){
 		return autonomousButton;
 	}
 	
 	public void setAutonomousButton(boolean newValue){
 		autonomousButton = newValue;
 	}
 }
