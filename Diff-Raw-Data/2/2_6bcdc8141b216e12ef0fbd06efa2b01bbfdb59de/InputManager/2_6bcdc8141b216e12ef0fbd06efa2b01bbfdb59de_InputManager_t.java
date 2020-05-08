 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.ames.frc.robot;
 //We need to test this.
 import com.sun.squawk.util.MathUtils;
 import edu.wpi.first.wpilibj.Joystick;
 /* List of buttons/toggles needed
  * Manual pivot toggle: 2
  * Speed boost button: Active joystick push
  * Force shoot button: 4 
  * Pivot Left : 5
  * Pivot Right : 6
  * Force Realign button: 7
  * Stop auto-target toggle: 10
  * Activate frisbee grab button: 8
  * Launch climb procedure: Simultaneously &  5,6,7,8,9, and 10.
  *
  */
 
 public class InputManager {
 //Git is good
 
     //static RobotMap rm = new RobotMap();
     protected static Joystick ps2cont;
     protected static Joystick monoJoystick;
     // protected static boolean dzactive  = false; // In case we want to check for deadzoneing being active
     //  protected static double[] axisOC = new double[2]; // Stores the original copies of the axis reads, for use elsewhere.
     protected static button manpivot;
     protected static button fireButton;
     //protected static button pivotRight;//What are these two?
     //protected static button pivotLeft;//What are these two?
     protected static button realign;
     protected static button infrisbee;
     protected static button autotarg;
     protected static button speedBoost;
     protected static button climber;
 
     public void init() {
         ps2cont = new Joystick(1);
         monoJoystick = new Joystick(2);
         manpivot = new button(true, RobotMap.manpivotpin);
         fireButton = new button(false, RobotMap.forcefire);
         realign = new button(false, RobotMap.realignpin);
         autotarg = new button(true, RobotMap.autotarg);
         speedBoost = new button(false, RobotMap.speedboost);
         climber = new button(false, RobotMap.clmpin);
     }
 
     public void updateAll() {
         boolean voidBool;
         voidBool = manpivot.getState();
         voidBool = fireButton.getState();
         //voidBool = pivotRight.getState();
         //voidBool = pivotLeft.getState();
         voidBool = realign.getState();
         voidBool = infrisbee.getState();
         voidBool = autotarg.getState();
         voidBool = speedBoost.getState();
         voidBool = voidBool; // LUL
     }
 
     public static double[] getPureAxis() { // Gets, stores, and returns the status of the joysticks on the PS2 Controller
         /* We will use a double dimension arry to hold the joystick data so that everything can be sent to other functions.
          * Both of the first dimensions will hold 2 doulbes, the first is the x & y axis of the first (paning) joystick
          * The second dimension holds the x & y for the second (pivoting) joystick
          */
         // double[] axis = new double[2];// Variable for storing all that data
         double[] dir = new double[2];
         dir[0] = -ps2cont.getRawAxis(1);// X
         dir[1] = ps2cont.getRawAxis(2);// Y
         //  axis[2] = -ps2cont.getRawAxis(3);// X
         //      axisOC[0] = axis[0][0]; 
         //    axisOC[1] = axis[0][1];
         //       axis[1][1] = PS2Cont.getRawAxis(4);// Y We dont actually need this value
         dir = deadzone(dir);
         //dir = ramp(dir);
         dir = translate(dir);
         return (dir); // Returns axis data to the caller.
     }
 
     public static double getPivot() {
         double pivot = -ps2cont.getRawAxis(3);
         pivot = rampSingle(pivot);
         return (pivot);
     }
 
     public static double getClimb() {
        double joyinput = -ps2cont.getRawAxis(4);
         joyinput = rampClimb(joyinput);
         return joyinput;
     }
 
     protected static double[] deadzone(double[] axis) {// Checks for deadzone
         //This is a skeleton of the deadzone funtion. Mark should fill this in.
 
         // for(byte li = 0; li <= axis.length; li++){//Loops through first dimesion of array
         for (byte si = 0; si < axis.length; si++) {//loops through second dimension of array.
             if (axis[si] <= RobotMap.deadzone && axis[si] >= RobotMap.deadzone) {
                 axis[si] = 0;
             }
         }
         //  }
         return (axis);
     }
 
     protected static double[] ramp(double[] axis) {
         for (byte ri = 0; ri < axis.length; ri++) {
             //axis[ri] = MathUtils.pow(axis[ri], rm.expo_ramp);
             axis[ri] = ((.666) * MathUtils.pow(axis[ri], RobotMap.expo_ramp)) + ((.333) * axis[ri]);
         }
         return (axis);
     }
 
     protected static double rampSingle(double axis) {
 
         //axis = MathUtils.pow(axis, rm.expo_ramp);
         axis = ((.666) * MathUtils.pow(axis, RobotMap.expo_ramp)) + ((.333) * axis);
         return (axis);
     }
 
     public static double rampClimb(double raw) {
         raw = raw / 10;
         return raw;
     }
 
     protected static double[] translate(double[] axis) {// Translates final input values into a format for use by the rest of the code.
         double[] vect = new double[2];
         double speed = 0;
         double angle = 0;
         //Sets the speed to the length of the hypotenuse of the imaginary triangle between x & y directional values
         speed = Math.sqrt(MathUtils.pow(axis[0], 2) + MathUtils.pow(axis[1], 2));// Pythagorean theorem: The square root of ( (X^2) + (y^2) )
         //Sets the angle to the inverse tangent of x / y
         angle = MathUtils.atan2(axis[0], axis[1]);// Tan^-1(x/y) Example: Tan^-1(.7/.2)
 
         if (angle < 0) {
             angle = (2 * Math.PI) - Math.abs(angle);//Wut
         }
 
         vect[0] = angle;
         vect[1] = speed;
         return (vect);
     }
 
     public static class button {
 
         boolean state;
         int bpin;
         boolean toggle;
 
         public button(boolean isToggle, int pin) {
             toggle = isToggle;
             bpin = pin;
         }
 
         public boolean getState() {
             state = ps2cont.getRawButton(this.bpin);
             return state;
         }
     }
     //protected static double[] translate(double[][] axis){// Translates deadzoned and scaled inputs into whatever exact type of input MotorControl needs/wants.
     //This is a skeleton of the translate funtion. Mark should fill this in.
     //return (ABC);
     // }
 }
