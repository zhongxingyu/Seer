 package com.buglabs.bug.module.vonhippel.pub;
 
 import java.io.IOException;
 
 public interface IVonHippelModuleControl {
 	public int LEDRedOff() throws IOException;		// Turn off red LED
 	public int LEDRedOn() throws IOException;
 	public int LEDGreenOff() throws IOException;
 	public int LEDGreenOn() throws IOException;
 	
	public int getStatus() throws IOException;
	public void makeGPIOOut(int pin) throws IOException;
	public void makeGPIOIn(int pin) throws IOException;
	public void setGPIO(int pin) throws IOException;
	public void clearGPIO(int pin) throws IOException;
	public void makeIOXOut(int pin) throws IOException;
	public void makeIOXIn(int pin) throws IOException;
	public void setIOX(int pin) throws IOException;
	public void clearIOX(int pin) throws IOException;
	public void setRDACResistance(int resistance) throws IOException;
	public int getRDACResistance() throws IOException;
	public void doADC() throws IOException;
	public int readADC() throws IOException;
	public void doDAC() throws IOException;
	public void readDAC() throws IOException;
 }
