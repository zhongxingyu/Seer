 package com.buglabs.bug.module.vonhippel.pub;
 
 import java.io.IOException;
 
 public interface IVonHippelModuleControl {
 	public int LEDRedOff() throws IOException;		// Turn off red LED
 	public int LEDRedOn() throws IOException;
 	public int LEDGreenOff() throws IOException;
 	public int LEDGreenOn() throws IOException;
 	
	//this is where the abstraction happens
 }
