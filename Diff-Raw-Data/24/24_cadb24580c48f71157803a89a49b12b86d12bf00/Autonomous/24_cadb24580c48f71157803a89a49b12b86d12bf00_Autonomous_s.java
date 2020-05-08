 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy;
 
import edu.wpi.first.wpilibj.*;

 /**
  * This is the location of the autonomous routines.
  * @author Eric
  */
public class Autonomous {  
    
     public static int getAutonSetting(){
         int setting = 0;
        return setting;    
     }
     
    public static void runAuton() {
         switch (getAutonSetting()) {
             case 1:
                 break;
             default:
                 break;
         }
     }
 }
