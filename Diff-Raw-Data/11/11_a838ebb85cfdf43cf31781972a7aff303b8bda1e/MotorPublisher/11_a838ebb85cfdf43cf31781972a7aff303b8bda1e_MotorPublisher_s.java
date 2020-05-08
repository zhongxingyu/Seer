 package robot.lcm;
 import java.awt.*;
 import java.io.*;
 import static java.lang.System.out;
 
 import javax.swing.*;
 
 import lcm.lcm.*;
 import april.jmat.*;
 import april.util.*;
 import april.vis.*;
 import lcmtypes.*;
 import april.*;
 import robot.lcm.*;
 
 public class MotorPublisher
 {
 
     LCM lcm;
 
     public MotorPublisher(){
         try{
             lcm = new LCM();
         } catch (Exception e){
 
         }
     }
 
    void publish(MotorSpeed motorSpeed){
 	    long now = TimeUtil.utime();
         drive_t cmd = new drive_t();
         cmd.timestamp = now;
         cmd.front_motor = motorSpeed.frontMotor;
         cmd.back_motor = motorSpeed.backMotor;
         cmd.right_motor = motorSpeed.rightMotor;
         cmd.left_motor = motorSpeed.leftMotor;
 
         lcm.publish("MOTOR", cmd);
     }
 
 }
