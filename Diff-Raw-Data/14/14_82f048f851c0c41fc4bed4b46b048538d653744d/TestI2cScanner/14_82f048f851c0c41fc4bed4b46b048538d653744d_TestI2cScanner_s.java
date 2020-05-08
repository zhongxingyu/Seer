 package com.neophob.lib.rainbowduino.test;
 
 import java.util.Arrays;
 
 import processing.core.PApplet;
 
 import com.neophob.lib.rainbowduino.Rainbowduino;
 
 /**
  * simply test class, only used to test the lib<br>
  * <br>
  * part of the neorainbowduino library
  * 
  * @author Michael Vogt / neophob.com
  *
  */
 public class TestI2cScanner extends PApplet {
 
 	Rainbowduino r;
 	
 	/**
 	 * 
 	 */
 	public void setup() {
 		System.out.println("Scan I2C bus: ");
		System.out.println("Found I2C devices: "+Arrays.toString(Rainbowduino.scanI2cBus(this).toArray()) );
 		
 		noLoop();
 	}
 	
 	public void draw() {  
 	}
 	
 	public static void main(String args[]) {
 		PApplet.main(new String[] { "com.neophob.lib.rainbowduino.test.TestI2cScanner" });
 	}
 
 }
