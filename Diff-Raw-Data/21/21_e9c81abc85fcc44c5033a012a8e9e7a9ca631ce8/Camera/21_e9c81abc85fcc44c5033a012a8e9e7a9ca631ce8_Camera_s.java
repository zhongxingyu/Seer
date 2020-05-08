 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.image.ColorImage;
 import edu.wpi.first.wpilibj.templates.Team1482Robot;
 /**
  *
  * @author student
  */
 
 public class Camera extends Team1482Robot{
     private ColorImage ciImage;
    try {
    ciImage = camera.getImage();
}
    catch {
}
     public void image(){
        
     }
     
 }
