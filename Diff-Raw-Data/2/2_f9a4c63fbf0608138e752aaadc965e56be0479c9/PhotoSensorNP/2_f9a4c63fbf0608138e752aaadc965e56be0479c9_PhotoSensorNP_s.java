 
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 public class PhotoSensorNP {
     
     DigitalInput lights = new DigitalInput(1);
     
 
     public boolean get(){
         return lights.get();
     }
     
     public void getDashboard(){
        SmartDashboard.getBoolean("Photosensor value", photosensor.get());
     }
     
 }
