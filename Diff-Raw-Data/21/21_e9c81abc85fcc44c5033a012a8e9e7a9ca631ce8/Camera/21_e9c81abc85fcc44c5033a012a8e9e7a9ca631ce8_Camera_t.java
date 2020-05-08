 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
import edu.wpi.first.wpilibj.camera.AxisCameraException;
 import edu.wpi.first.wpilibj.image.ColorImage;
import edu.wpi.first.wpilibj.image.NIVisionException;
 import edu.wpi.first.wpilibj.templates.Team1482Robot;
 /**
  *
  * @author student
  */
 
 public class Camera extends Team1482Robot{
     private ColorImage ciImage;

    
     public void image(){
        //Get the camera image
        try {
            ciImage = camera.getImage();
        } catch (AxisCameraException ex) {
            ex.printStackTrace();
        } catch (NIVisionException ex) {
            ex.printStackTrace();
        }          
     }
     
 }
