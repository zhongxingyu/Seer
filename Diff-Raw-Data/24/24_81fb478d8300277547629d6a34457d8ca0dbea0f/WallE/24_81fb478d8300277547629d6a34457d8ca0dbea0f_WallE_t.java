 /* All FRC 3780 code follows the following License:
  * Copyright 2013 FRC 3780 (Brian Fogarty, Oliver Graff) Licensed under the
  * Educational Community License, Version 2.0 (the "License"); you may not use 
  * this file except in compliance with the License. You may obtain a copy of the
  * License at 
  *      http://www.osedu.org/licenses/ECL-2.0 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 
 package org.usfirst.frc3780.subsystems;
 
 import edu.wpi.first.wpilibj.Servo;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import org.usfirst.frc3780.robot.RobotMap;
 
 /**
  * The subsystem for the servo.
  * @author Brian + Oliver
  */
 public class WallE extends Subsystem {
 
     private static WallE __instance;
     
     public static WallE getInstance() {
         if(__instance == null) {
             __instance = new WallE();
         }
         return __instance;
     }
     
     
     private Servo _servo;
     
     //  Values to calibrate servo.
    private int ServoOpenValue = 0;
    private int ServoClosedValue = 1;
     
     private WallE() {
         _servo = new Servo(RobotMap.walleServoSlot, RobotMap.walleServoChannel);
     }
     
     public void open() {
         _servo.set(ServoOpenValue);
     }
     
     public void close() {
         _servo.set(ServoClosedValue);
     }
     
     /**
      * Sees if the servo is open.
      * @return If the servo is open or closed.
      */
     public boolean getOpen() {
         if (Math.abs(_servo.get()-ServoOpenValue) < .1) {
             return true;
         } else {
             return false;
         }
     }
     
     public void initDefaultCommand() {
     }
     
 }
