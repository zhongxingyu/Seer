 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package core;
 
 import edu.wpi.first.wpilibj.Solenoid;
 
 /**
  *
  * @author warren.elwood
  */
public class Ejector {

     public Ejector(myJoystick joy, int port1, int port2, int port3, int port4)// Joy will come later
     {
        Piston sol = new Piston(int port1,int port2, int port3, int port4);
        
           
          }
    public void run() {
        
            if (/*TODO*/)// replace with code that gets the joystick button state
            {
                sol.solenoidSwitch();
            }



         }



     }
 
