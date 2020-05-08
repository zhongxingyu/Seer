 package com.github.Holyvirus.Blacksmith.core.Tools.Sign;
 
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.SignChangeEvent;
 
 public class SignValidator {
 	
 	private static boolean isBlackSmithSign(String s) {
 		return s.equalsIgnoreCase("[BlackSmith]");
 	}
 	
 	public static SignType getType(String s) {
		if(isBlackSmithSign(s)) {
			if(s.equalsIgnoreCase("VALUE")) {
				return SignType.VALUE;
			}else if(s.equalsIgnoreCase("REPAIR")) {
				return SignType.REPAIR;
			}else if(s.equalsIgnoreCase("KILL")) {
				return SignType.KILL;
			}
 		}
 		
 		return SignType.INVALID;
 	}
 	
 	public static boolean isBlackSmithSign(Sign s) {
 		return isBlackSmithSign(s.getLine(0));
 	}
 	
 	public static boolean isBlackSmithSign(SignChangeEvent s) {
 		return isBlackSmithSign(s.getLine(0));
 	}
 	
 	public static SignType getType(Sign s) {
		return getType(s.getLine(1));
 	}
 	
 	public static SignType getType(SignChangeEvent s) {
		return getType(s.getLine(1));
 	}
 }
