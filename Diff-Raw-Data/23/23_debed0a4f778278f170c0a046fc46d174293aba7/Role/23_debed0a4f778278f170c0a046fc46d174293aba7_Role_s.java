 package com.epam.lab.intouch.model.member.enums;
 
 public enum Role {
 	DEVELOPER, TESTER, MANAGER;
 
 	public static Role fromString(String string) {
 		if (string != null) {
			for (QualificationLevel level : values()) {
				if (string.equalsIgnoreCase(level.toString())) {
					return level;
 				}
 			}
 		}
 		return null;
 	}
 }
