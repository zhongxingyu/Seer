package org.usfirst.frc1318.shared;
 
 public class TBGamepadData 
 {
 	static boolean modeToggle;
 	static boolean kickButton;
 	
 	public static boolean isKickButton() {
 		return kickButton;
 	}
 	public static void setKickButton(boolean _kickButton) {
 		kickButton = _kickButton;
 	}
 	
 	public static boolean isModeToggle() {
 		return modeToggle;
 	}
 	public static void setModeToggle(boolean _modeToggle) {
 		modeToggle = _modeToggle;
 	}
 	
 }
