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
     protected static Joystick ps2cont = new Joystick(1);
     // protected static boolean dzactive  = false; // In case we want to check for deadzoneing being active
     //  protected static double[] axisOC = new double[2]; // Stores the original copies of the axis reads, for use elsewhere.
     protected static button manpivot = new button(true, 2);
     protected static button fireButton = new button(false, 4);
     protected static button pivotRight = new button(false, 6);
     protected static button pivotLeft = new button(false, 5);
     protected static button realign = new button(false, 7);
     protected static button infrisbee = new button(false, 8);//Activates the frisbee retriever 
     protected static button autotarg = new button(true, 10);
     protected static button speedBoost = new button(false, 11);
 
     public static double[] getPureAxis() { // Gets, stores, and returns the status of the joysticks on the PS2 Controller
         /* We will use a double dimension arry to hold the joystick data so that everything can be sent to other functions.
          * Both of the first dimensions will hold 2 doulbes, the first is the x & y axis of the first (paning) joystick
          * The second dimension holds the x & y for the second (pivoting) joystick
          */
         double[] axis = new double[3];// Variable for storing all that data
         double[] dir = new double[3];
         axis[0] = ps2cont.getRawAxis(1);// X
         axis[1] = ps2cont.getRawAxis(2);// Y
         axis[2] = ps2cont.getRawAxis(3);// X
         //      axisOC[0] = axis[0][0]; 
         //    axisOC[1] = axis[0][1];
         //       axis[1][1] = PS2Cont.getRawAxis(4);// Y We dont actually need this value
         axis = deadzone(axis);
         axis = ramp(axis);
         dir = translate(axis);
         return (dir); // Returns axis data to the caller.
     }
 
     protected static double[] deadzone(double[] axis) {// Checks for deadzone
         //This is a skeleton of the deadzone funtion. Mark should fill this in.
         
        // for(byte li = 0; li <= axis.length; li++){//Loops through first dimesion of array
             for(byte si = 0; si < axis.length; si++){//loops through second dimension of array.
                if(axis[si] <= RobotMap.deadzone & axis[si] >= -RobotMap.deadzone){
                     axis[si] = 0;
                 }
             }
       //  }
         return (axis);
     }
 
     protected static double[] ramp(double[] axis) {
         for(byte ri = 0; ri < axis.length; ri++){
         axis[ri] = MathUtils.pow(axis[ri], RobotMap.expo_ramp);
         }
         return (axis);
     }
 
     protected static double[] translate(double[] axis) {// Translates final input values into a format for use by the rest of the code.
         //This is a skeleton of the ramp funtion. Mark should fill this in
         double[] vect = new double[3];
         double speed = 0;
         double angle = 0;
         //     double hypo = 0;
         speed = Math.sqrt(MathUtils.pow(axis[0], 2) + MathUtils.pow(axis[1], 2));
         angle = RobotArithmetic.arcTangent(axis[0], axis[1]);
         vect[0] = angle;
         vect[1] = speed;
         vect[3] = axis[2];
         return (vect);
     }
 
     public static class button {
 
         public button(boolean isToggle, int pin) {
             toggle = isToggle;
             bpin = pin;
         }
         boolean state;
         int bpin;
         boolean toggle;
     }
     //protected static double[] translate(double[][] axis){// Translates deadzoned and scaled inputs into whatever exact type of input MotorControl needs/wants.
     //This is a skeleton of the translate funtion. Mark should fill this in.
     //return (ABC);
     // }
 }
