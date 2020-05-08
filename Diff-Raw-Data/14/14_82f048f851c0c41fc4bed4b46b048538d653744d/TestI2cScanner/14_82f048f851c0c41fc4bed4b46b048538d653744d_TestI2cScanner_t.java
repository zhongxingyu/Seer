 package com.neophob.lib.rainbowduino.test;
 
 import java.util.Arrays;
import java.util.List;
 
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
		List<Integer> list = Rainbowduino.scanI2cBus(this,"/dev/tty.usbserial-A9007QOH");
		System.out.println("Found I2C devices: "+Arrays.toString(list.toArray()) );
 		
 		noLoop();
 	}
 	
 	public void draw() {  
 	}
 	
 	public static void main(String args[]) {
 		PApplet.main(new String[] { "com.neophob.lib.rainbowduino.test.TestI2cScanner" });
 	}
 
 }
