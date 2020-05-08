 package Jordan.bau5.FRC2115.subsystems;
 
 import Jordan.bau5.FRC2115.RobotMap;
 import Jordan.bau5.FRC2115.commands.ShootWithJoyStick;
 import edu.wpi.first.wpilibj.AnalogChannel;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.command.Subsystem;
 
 public class Shooter extends Subsystem 
 {
     Jaguar j = new Jaguar(RobotMap.shooterMotor);
     private AnalogChannel us = new AnalogChannel(1, 1);
    private int step = 0;
 
     
     public void initDefaultCommand() 
     {
        //setDefaultCommand(new ShootWithJoyStick());
     }
     
     public void spinWithJoystick(Joystick jSet, int axis)
     {
         double joyStickValue = jSet.getRawAxis(axis);
         //Reverse axis because our joystick makes the top -1
         //Then change the range from -1 -> 1 to 0 -> 1
         j.set(((-1 * joyStickValue) + 1) / 2);
     }
     
     //for testing distance v. shooter speed
     public void spinWithButton(int button)
     {
        if(step < 20 && button == 11)
            step++;
        if(step > -20 && button == 10)
            step--;
        j.set(.05 * step);
        System.out.println(j.get());
     }
     
     public void actualShoot()
     {
         range();
     }
     
     public void range()
     {
        int usRange = us.getValue() / 2;
         System.out.println("Range: " + usRange);
     }
 }
