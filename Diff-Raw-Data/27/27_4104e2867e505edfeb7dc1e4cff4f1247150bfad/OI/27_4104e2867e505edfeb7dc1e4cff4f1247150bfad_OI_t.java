 package Jordan.bau5.FRC2115;
 
 import Jordan.bau5.FRC2115.commands.*;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 
 public class OI 
 {   
     private Joystick jGamepad = new Joystick(1),
             jAux = new Joystick(2);
     
     public Joystick jLeftWheel = jGamepad;
     public int leftAxis = 2;
     public Joystick jRightWheel = jGamepad;
     public int rightAxis = 3;
     public Joystick jShootSetter = jAux;
     public int shootSetAxis = 3;
     
     public JoystickButton plungerButton = new JoystickButton(jGamepad, 6),
             bridgeButton = new JoystickButton(jGamepad, 9),
             rollerButton = new JoystickButton(jGamepad, 10),
             autoAim = new JoystickButton(jAux, 3),
             fasterButton = new JoystickButton(jAux, 11),
             slowerButton = new JoystickButton(jAux, 10),
             shootButton = new JoystickButton(jGamepad, 8);
     
     public OI()
     {
         plungerButton.whenPressed(new DeployPlunger(0.5));
         bridgeButton.whenPressed(new ExtendArm(2.5, 2.5));
         bridgeButton.whenReleased(new RetractArm(2.5, 2.5));
         rollerButton.whileHeld(new RollRoller());
         autoAim.whileHeld(new AutoAlign());
        fasterButton.whileHeld(new IncreaseShooterSpeed());
        slowerButton.whileHeld(new DecreaseShooterSpeed());
         shootButton.whileHeld(new Shoot());
     }
 }
