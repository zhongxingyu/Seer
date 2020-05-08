 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 
 /**
  *
  * @author 01965
  */
 
 
 public class camera implements IRobot
 {

    private AxisCamera camera;
     public void getCamera()
     {     
        camera.getInstance().writeCompression(0);
        camera.getInstance().writeResolution(AxisCamera.ResolutionT.k320x240);
        camera.getInstance().writeBrightness(10);
         DriverStationLCD.getInstance().updateLCD();
     }
 }
