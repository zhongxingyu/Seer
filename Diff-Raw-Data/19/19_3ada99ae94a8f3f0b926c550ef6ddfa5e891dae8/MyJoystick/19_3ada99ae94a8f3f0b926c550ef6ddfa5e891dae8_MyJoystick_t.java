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
    boolean[]buttonState;
     public MyJoystick(int portNumber){
         super(portNumber);
         System.out.println("Joystick created with port:"+ portNumber);       
        buttonState = new boolean[15];
        //bigger number for buttons just in case
               
    }    
     //calls constructor
     public boolean getDebounce(int button){
        
        if(buttonState[button] == false && getRawButton(button) == true){              
               buttonState[button]=getRawButton(button);
                System.out.print("Button pressed");
                return true; 
            }  
            // If the the button was unpressed and now is pressed it is pressed
            else{
                buttonState[button] = getRawButton(button);
                return false;
            //If not the button is not pressed    
            }
                        
         
     }
         
    
     
 }
