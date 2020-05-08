 package com.frc4343.robot;
 
 import java.util.Vector;
 
 public final class Constants {
     // Prevents instantiation of the class.
     private Constants() {
     }
     // CAMERA
     public static final int CAMERA_BRIGHTNESS = 50;
     public static final int CAMERA_COMPRESSION = 80;
     public static final int CAMERA_COLOUR_LEVEL = 100;
     // Tape Reflection HSL values
     public static final int HMIN = 5;
     public static final int HMAX = 50;
     public static final int SMIN = 120;
     public static final int SMAX = 255;
     public static final int LMIN = 120;
     public static final int LMAX = 255;
     // Minumum camera target area.
     public static final int PARTICLE_AREA = 500;
     public static final double CAMERA_TARGET_DEADZONE = 0.1;
     public static final double CAMERA_TURN_TIMEOUT = 0.1;
     // TIMEOUTS
     public static final double WINDING_TIME = 0.3;
     public static final double TRIGGER_TIME = 0.575;
     public static final double BRIDGE_TIME = 1;
     // GYRO
     public static final float Kp = 200;
     // MOTOR VARIABLES
     public static final boolean SAFETY_ENABLED = false; // If this is true, it throws errors but the trigger works.
     public static final double EXPIRATION = 0.01;
     // JOYSTICKS
     public static final int JOYSTICK_PORT = 1;
     // SOLENOIDS
     public static final int SOLENOID_ONE = 0;
     public static final int SOLENOID_TWO = 1;
     // BUTTON MAPPINGS
     public static final int CATAPULT_WIND_BUTTON = 2;
     public static final int CATAPULT_UNWIND_BUTTON = 3;
     public static final int UNPICKUP_BUTTON = 4;
     public static final int PICKUP_BUTTON = 5;
     public static final int BRIDGE_LOWER_BUTTON = 6;
     public static final int BRIDGE_RAISE_BUTTON = 7;
     public static final int CAMERA_LIGHT_BUTTON = 8;
     public static final int CAMERA_TARGET_BUTTON = 9;
     public static final int AUTO_FIRE_BUTTON = 10;
     //public static final UINT32 SOLENOID_ONE_BUTTON = 10;
     public static final int SOLENOID_TWO_BUTTON = 11;
 
     public static String combineStringWithDouble(String string, double doubleVal) {
        return string.concat(Double.toString(doubleVal));
     }
 
     public static double getDoubleFromVector(Vector vector, int index) {
        return ((Double) vector.elementAt(index)).doubleValue();
     }
 }
