 package edu.nau.spring2012.cs386.DEADBEEF;
 
 import lejos.nxt.*;
 import lejos.nxt.addon.ColorSensorHT;
 
 public class RobotState {
 
 	private static LightSensor      light      = new LightSensor(SensorPort.S2);
 	private static UltrasonicSensor ultrasonic = new UltrasonicSensor(SensorPort.S3);
 	private static ColorSensorHT    color      = new ColorSensorHT(SensorPort.S4);
 
 	public static double trackWidth = 13.65;
 	//
 	// measured to outside = 14.0 mm
 	// measure from inside = 13.3 mm
 	
 	public static boolean touched;
 	public static int     lightLevel;
 	public static float   range;
	public static int     colorId;
 
 	public static int     totalItrs = 0;
 	public static boolean      hunt = false;
 	public static int     lineLevel;
 	public static int  nonLineLevel;
 	public static int  lineLevelErr;
 	public static int      itrsLost = 0;
 
 	public static CompassDifferentialPilot pilot =
 			  new CompassDifferentialPilot(4.0f, 4.0f, (float)trackWidth, Motor.A, Motor.B, false);
 	//
 	// actual gear diameter to edge of tooth is 42 mm
 
 	public static void preCalibrateLight() {
 		light.setFloodlight(true);
 	}
 
 	public static void calibrateLight() {
 		lightLevel = light.getNormalizedLightValue();
 	}
 
 	public static void setContinuousMode() {
 		ultrasonic.continuous();
 	}
 
 	public static void zeroCompass() {
 		pilot.zeroCompass();
 	}
 	
 	public static boolean poll() {
 
 		totalItrs++;
 		
 		LCD.drawString("L        R      ",0,1);
 		LCD.drawInt(Motor.A.getSpeed(),5,2,1);
 		LCD.drawInt(Motor.B.getSpeed(),5,11,1);
 		
 		LCD.drawString("LineLvl:   ",0,2);
 		LCD.drawInt(lineLevel,5,11,2);
 
 		LCD.drawString("LostItrs:  ",0,3);
 		LCD.drawInt(itrsLost,5,11,3);
 		
 		// TouchSensor
 		//
 		touched = Button.ENTER.isPressed();
 		LCD.drawString("Touched:   ",0,5);
 		if ( touched ) {
 			LCD.drawString(" true",11,5);
 		} else {
 			LCD.drawString("false",11,5);
 		}
 		
 		// LightSensor
 		//
 		lightLevel = light.getNormalizedLightValue();
 		LCD.drawString("LightLvl:   ",0,6);
 		LCD.drawInt(lightLevel,5,11,6);
 		lineLevelErr = Math.abs(lightLevel - lineLevel);
 		LCD.drawInt(lineLevelErr,4,12,4);
 				
 		// UltrasonicSensor
 		//
 		range = ultrasonic.getRange();
 		LCD.drawString("Range:      ",0,7);
 		LCD.drawInt((int)range,5,11,7);
 		
 		// ColorSensor
 		//
		colorId = color.getColorID();
 		
 		if ( !Button.ESCAPE.isPressed() ) {
 			return true;
 		} else {
 			return false;
 		}
 		
 	}
 
 }
