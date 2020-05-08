 package edu.wpi.first.wpilibj.templates.debugging;
 
 /**
  * These are different debugging levels.
  *
  * LOW should be for things that are displaying what a motor/solenoid "should
  * be".
  *
  * MID should be for things that are displaying what a motor/solenoid is.
  *
  * HIGH+ will almost always be displayed.
  *
  * @author daboross
  */
 public class DebugLevel {
 
     /**
      * Messages with this level will always be displayed.
      */
     public static final int ALWAYS = 6;
     /**
      * Messages with this level will pretty much always be displayed.
      */
     public static final int HIGHEST = 5;
     /**
     * 
      */
     public static final int HIGH = 4;
     /**
     * 
      */
     public static final int MID = 3;
     /**
     * 
      */
     public static final int LOW = 2;
     /**
     * 
      */
     public static final int LOWEST = 1;
     /**
     * 
      */
     public static final int NEVER = 0;
     /**
     * 
      */
     public static int CURRENT = MID;
 }
