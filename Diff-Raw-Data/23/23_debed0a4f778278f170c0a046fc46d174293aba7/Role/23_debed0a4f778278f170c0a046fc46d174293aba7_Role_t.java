 package com.epam.lab.intouch.model.member.enums;
 
 public enum Role {
 	DEVELOPER, TESTER, MANAGER;
 
 	public static Role fromString(String string) {
 		if (string != null) {
			for (Role role : values()) {
				if (string.equalsIgnoreCase(role.toString())) {
					return role;
 				}
 			}
 		}
 		return null;
 	}
 }
