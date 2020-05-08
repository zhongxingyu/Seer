 package edu.wpi.first.wpilibj.templates.variablestores;
 
 /**
  * This is the main variable store.
  */
 public class VstM {
 
     /*
      * Debug controls.
      */
     public class Debug {
 
         /**
          * This is the Dashboard debug variable. If DEBUG isn't true then this
          * will not work.
          */
         public static final boolean DASHBOARD = true;
         /**
          * This is the Console debug variable. If DEBUG isn't true then this
          * will not work.
          */
         public static final boolean CONSOLE = true;
         /**
          * This is the main debug variable.
          */
         public static final boolean DEBUG = DASHBOARD || CONSOLE;
     }
 
     /*
      * Digital input ports
      */
     public class Digital {
 
         public final static int PRESSURE_SWITCH = 1;
         public static final int CLIMBER_TOP = 2;
         public static final int CLIMBER_BOTTOM = 3;
     }
 
     /*
      * Analog input ports
      */
     public class Analog {
     }
 
     /*
      * PWM ports
      */
     public class PWM {
 
         public static final int LEFT_MOTOR_PORT = 1;
         public static final int RIGHT_MOTOR_PORT = 2;
         public static final int CLIMBER_MOTOR = 3;
         public static final int TEST_MOTOR_1_PORT = 4;
         public static final int TEST_MOTOR_2_PORT = 5;
     }
 
     /*
      * Relay ports
      */
     public class Relays {
 
         public final static int COMPRESSOR = 1;
         public final static int LOWER_LIMIT_SWITCH = 2;
         public final static int UPPER_LIMIT_SWITCH = 3;
     }
 
     /*
      * Joystick axes/buttons
      */
     public class Joysticks {
 
         public class Xbox {
 
             public final static int PORT = 1;
             public final static int LEFT_X = 1;
             public final static int LEFT_Y = 2;
             public final static int TRIGGERS = 3;
             public final static int RIGHT_X = 4;
             public final static int RIGHT_Y = 5;
         }
     }
 
     /**
      * This Class Holds Variables For The Climber.
      */
     public static class Climber {
 
         /**
          * This variable should represent the state of the Climber Motors.
          *
          * 0 for off.
          *
          * -1 for retracting
          *
          * 1 for extending.
          */
         private static int climberState = 0;
 
         /**
          * This should get the state of the Climber Motors. This method may not
          * return the exact state, as this is set by other methods besides the
          * RunClimber to tell the RunClimber what to do.
          *
          * 0 for off.
          *
          * -1 for retracting
          *
          * 1 for un retracting.
          */
         public static int climberState() {
             return climberState;
         }
 
         /**
          * This sets the value returned by climberState(). This should be called
          * by the RunClimber every refresh and by other methods wanting to set
          * what the RunClimber is doing.
          *
          * 0 for off.
          *
          * -1 for retracting
          *
          * 1 for extending.
          */
         public static void setClimberState(int climberStateV) {
             if (climberStateV > 1) {
                 throw new IllegalArgumentException("To High A Value in setClimberState");
             }
             if (climberStateV < -1) {
                 throw new IllegalArgumentException("To Low A Value in setClimberState");
             }
             climberState = climberStateV;
         }
 
         /**
         * This is the equevilent to: climberState()==-1
          */
         public static boolean isRetracting() {
             return climberState < 0;
         }
     }
 }
