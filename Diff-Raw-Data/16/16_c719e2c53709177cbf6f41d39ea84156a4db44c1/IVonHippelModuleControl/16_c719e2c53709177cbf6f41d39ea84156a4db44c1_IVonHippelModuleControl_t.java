 package com.buglabs.bug.module.vonhippel.pub;
 
 import java.io.IOException;
 
 public interface IVonHippelModuleControl {
 	public int LEDRedOff() throws IOException;		// Turn off red LED
 	public int LEDRedOn() throws IOException;
 	public int LEDGreenOff() throws IOException;
 	public int LEDGreenOn() throws IOException;
 	
	public int getStatus();
	public void makeGPIOOut(int pin);
	public void makeGPIOIn(int pin);
	public void setGPIO(int pin);
	public void clearGPIO(int pin);
	public void makeIOXOut(int pin);
	public void makeIOXIn(int pin);
	public void setIOX(int pin);
	public void clearIOX(int pin);
	public void setRDACResistance(int resistance);
	public int getRDACResistance();
	public void doADC();
	public int readADC();
	public void doDAC();
	public void readDAC();
 }
