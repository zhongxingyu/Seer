 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package core;
 import util.MyTalon;
 import util.Config;
 import util.MyJoystick;
 import edu.wpi.first.wpilibj.DigitalInput;
 import util.Output;
 
 /**
 * @author Warren
  * Controls the ejector using limit switches.
  */
 public class LimitEjector 
 
 {
     MyJoystick joy;
     MyTalon ejectorMotor;
     DigitalInput limitSwitchFar;
     DigitalInput limitSwitchClose;
     boolean retractMode = false;
     double speed;
     
     /**
      * Constructor takes a joystick while creating the limit switches.
      * @param joystick 
      */
     public LimitEjector(MyJoystick joystick)
     {
         limitSwitchFar= new DigitalInput(Config.chnLsFar);
         limitSwitchClose =new DigitalInput(Config.chnLsClose);
         ejectorMotor= new MyTalon(Config.chnEjector);
         joy = joystick;   
     }
     
     /**
      * Extends and retracts the ejector when a button is pressed.
      */
     public void run()
     {
         if(joy.getDebounce(Config.btEjector))     //TODO get button
             ejectorMotor.set(speed);
             
         if(retractMode == false && limitSwitchFar.get())
         {
             ejectorMotor.set(-speed);
             retractMode = true;
         }
         
         if(limitSwitchClose.get())
         {
            ejectorMotor.set(0);
             retractMode = false;
         }
     }
 }
