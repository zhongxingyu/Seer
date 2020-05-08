 // The class for piston exceptions, thrown by the Piston class
 package org.usfirst.frc3780.components;
 
 public class PistonException extends RuntimeException {
	private String cause;
 	public PistonException(String s) {
		cause = s;
 	}
 	
	public String toString() {
		return super.toString() + cause;
	}
 	
 }
