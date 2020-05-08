 package org.usfirst.frc1318.minimike.shared;
 
 public class MMLimitSwitchData {
 		
	private static boolean rightSwitch = false;
	private static boolean leftSwitch = false;
 
 	public boolean getRightState(){
 		return rightSwitch;
 	}
 	
 	public void setRightState(boolean state){
 		//System.out.println("in right switch");
 		rightSwitch = state;
 		//System.out.println("right limit switch " + state + " hi");
 	}
 	
 	public boolean getLeftState(){
 		return leftSwitch;
 	}
 	
 	public void setLeftState(boolean state){
 		//System.out.println("in left switch");
 		leftSwitch = state;
 		//System.out.println("left limit switch " + state);
 	}
 	
 }
