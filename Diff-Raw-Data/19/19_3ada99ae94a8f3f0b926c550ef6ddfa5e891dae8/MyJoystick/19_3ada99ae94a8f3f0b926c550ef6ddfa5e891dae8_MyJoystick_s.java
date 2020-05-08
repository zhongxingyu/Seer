 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package util;
 import edu.wpi.first.wpilibj.Joystick;
 
 /**
  *
  * @author seraj.bonakdar
  */
 public class MyJoystick extends Joystick {
     public MyJoystick(int portNumber){
         super(portNumber);
         System.out.println("Joystick created with port:"+ portNumber);       
    }
     //calls constructor
     public boolean getDebounce(int button){
           boolean lastState = getRawButton(button);
           //make sure the button is set to false? 
           if(lastState == false && lastState ==! getRawButton(button)){
               
                System.out.print("Button pressed");
                return true; 
            }  
            // If the the button was unpressed and now is pressed it is pressed
            else{
               return false;
            //If not the button is not pressed    
            }
                        
         
     }
         
    
     
 }
