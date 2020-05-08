 
     package modules.oran;
 
     import edu.wpi.first.wpilibj.Jaguar;
     import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.Timer;
 
     /**
      *
      * @author luzono
      */
     public class DriveOran extends GyroOran {
         //Credit to Yoli, Marc, Cory, Lucas for helping to write code.
 
            Joystick left = new Joystick(1);
            Joystick right = new Joystick(2);
 
             Jaguar bl = new Jaguar(1);
             Jaguar br = new Jaguar(2);
             Jaguar fl = new Jaguar(3);
             Jaguar fr = new Jaguar(4);
             
             double delay = 1;
             
             
             public void startup(){
 
             }
 
 
             protected void iteration(){
                     boolean tRight = right.getRawButton(2);
                     boolean tLeft = right.getRawButton(3);
 
                     double leftvalue;
                     double rightvalue;
 
                     leftvalue = left.getY();
                     rightvalue = right.getY();
 
                     bl.set(leftvalue);
                     fl.set(leftvalue);
                     br.set(-1 * rightvalue);
                     fr.set(-1 * rightvalue);
                     
                     
                     
                     if(tLeft) {
                         double less = angle + 315;
                        while (less >= 360){
                             less -= 360;
                         }
                        if (angle < less){
                         leftvalue = -1;
                         rightvalue = 1;   
                         
                         bl.set(leftvalue);
                         fl.set(leftvalue);
                         br.set(-1 * rightvalue);
                         fr.set(-1 * rightvalue);   
                         }
                         
                         Timer.delay(delay);
                         if (tLeft) {
                             leftvalue = -0.5;
                             rightvalue = 0.5;
                         
                             bl.set(leftvalue);
                             fl.set(leftvalue);
                             br.set(-1 * rightvalue);
                             fr.set(-1 * rightvalue);
                         }
                     }
 
                     if(tRight) {
                         double less1 = angle + 45;
                         if (less1 >= 360){
                             less1 -= 360;
                         }
                         while (angle < less1){
                         leftvalue = 1;
                         rightvalue = -1;
                         
                         bl.set(leftvalue);
                         fl.set(leftvalue);
                         br.set(-1 * rightvalue);
                         fr.set(-1 * rightvalue);
                         }
                         
                         Timer.delay(delay);
                         if (tRight) {
                             leftvalue = 0.5;
                             rightvalue = -0.5;
 
                             bl.set(leftvalue);
                             fl.set(leftvalue);
                             br.set(-1 * rightvalue);
                             fr.set(-1 * rightvalue);
                         }
                     }
 
            }
 
             public void reset(){
 
             }
     } 
 
 
