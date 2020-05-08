 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bt.main;
 
 /**
  *
  * @author Dlock
  */
 public interface Constants {
     public static final int XBOX_CONTROLLER_PORT = 1;
     public static final int XBOX_CONTROLLER_BUTTONS = 10;
     public static final int XBOX_CONTROLLER_AXES = 10;
     
     
     //B MAP
     public static final int A_BUTTON = 1;
     public static final int B_BUTTON = 2;
     public static final int X_BUTTON = 3;
     public static final int Y_BUTTON = 4;
     public static final int BACK_BUTTON = 7;
     public static final int START_BUTTON = 8;
     public static final int LEFT_BUMPER_BUTTON = 5;
     public static final int RIGHT_BUMPER_BUTTON = 6;
     public static final int LEFT_STICK_BUTTON = 9;
     public static final int RIGHT_STICK_BUTTON = 10;
     
     
     //A MAP
     public static final int LEFT_STICK_UP_DOWN_PORT = 2;
     public static final int LEFT_STICK_LEFT_RIGHT_PORT = 1;
     public static final int RIGHT_STICK_UP_DOWN_PORT = 5;
    public static final int RIGHT_STICK_LEFT_STICK_PORT = 4;
     public static final int TRIGGER_PORT = 3;
     public static final double LEFT_TRIGGER = 1.0;
     public static final double RIGHT_TRIGGER = -1.0;
     public static final int DPAD_LEFT_RIGHT_PORT = 6;
     public static final double LEFT_DPAD = -1.0;
     public static final double RIGHT_DPAD = 1.0;
 
     
     
     //Debugger Constants
     public static final int DEBUG_LOCATION_AMOUNT = 1;
     public static final int DEBUG_CAP_LIMIT = 1000;
     public static final String[] LOCATION_ARR = {"BTmain",
                                                  "ControlBoard",
                                                  "BTController",
                                                  "BTDebugger",
                                                  "BTMotor",
                                                  "BTSmartGUI",
                                                  "Drive Train",
                                                  "Collector",
                                                  "Low Climber"
                                                 };
     public static final String[] SEVERITY_ARR = {"Information","Warning","SEVERE"};
     public static final boolean DEBUGMODE = true;
     public static final boolean DEBUG_DURING_DISABLED = false;
     
     //DriveTrain
     public static final int LEFT_MOTOR_PORT = 1;
     public static final int RIGHT_MOTOR_PORT = 2;
     public static final int LEFT_2_MOTOR_PORT = 1;
     public static final int RIGHT_2_MOTOR_PORT = 2;
     public static final int VOLTAGE_RAMP_RATE = 50;
 }
