 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.rmr662.frc2012.controller;
 
import com.rmr662.frc2012.RMRRobot;
 import com.rmr662.frc2012.controller.function.DriveFunction;
import com.rmr662.frc2012.controller.function.DrivePIDTuningFunction;
 import com.rmr662.frc2012.generic.Controller;
 import com.rmr662.frc2012.generic.Function;
 import edu.wpi.first.wpilibj.Joystick;
 
 /**
  *
  * @author RMR Programming
  */
 public class TeleopController implements Controller {
 
     private static final int[] JOYSTICKS = {1, 2};
     private Joystick[] joysticks = new Joystick[2];
     private Function[] functions;
     private static TeleopController instance = null;
 
     public TeleopController() {
         for (int i = 0; i < joysticks.length; i++) {
             joysticks[i] = new Joystick(JOYSTICKS[i]);
         }
        functions = new Function[2];
         functions[0] = new DriveFunction(joysticks);
        functions[1] = new DrivePIDTuningFunction(joysticks);
     }
 
     public void run() {
        while (RMRRobot.robot.isOperatorControl() && !RMRRobot.robot.isDisabled()) {
             for (int i = 0; i < functions.length; i++) {
                 functions[i].run();
             }
         }
     }
 
     public static TeleopController getInstance() {
         if (instance == null) {
             instance = new TeleopController();
         }
         return instance;
     }
 
     public String getRMRName() {
         return "Teleop Controller";
     }
 }
