 package Jordan.bau5.FRC2115;
 
 import edu.wpi.first.wpilibj.Joystick;
 
 public class OI 
 {
     public static int JOYSTICK_ONE_PORT = 1, JOYSTICK_TWO_PORT = 2;
     Joystick j1, j2;
     
     public OI()
     {
         j1 = new Joystick(JOYSTICK_ONE_PORT);
         j2 = new Joystick(JOYSTICK_TWO_PORT);
     }
     
     public Joystick getJ1()
     {
         return j1;
     }
     
     public Joystick getJ2()
     {
         return j2;
     }
 }
