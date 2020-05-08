 package Jordan.bau5.FRC2115;
 
 import Jordan.bau5.FRC2115.commands.DeployPlunger;
 import Jordan.bau5.FRC2115.commands.ExtendArm;
 import Jordan.bau5.FRC2115.commands.RetractArm;
 import Jordan.bau5.FRC2115.commands.RollRoller;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 
 public class OI 
 {   
     private Joystick jGamepad = new Joystick(1),
             jAux = new Joystick(2);
     
     public Joystick jLeftWheel = jGamepad;
     public int leftAxis = 2;
     public Joystick jRightWheel = jGamepad;
    public int rightAxis = 4;
     public Joystick jShootSetter = jAux;
     public int shootSetAxis = 3;
     
     public JoystickButton plungerButton = new JoystickButton(jGamepad, 6),
             bridgeButton = new JoystickButton(jGamepad, 9),
             rollerButton = new JoystickButton(jGamepad, 10);
     
     public OI()
     {
         plungerButton.whenPressed(new DeployPlunger(0.5));
         bridgeButton.whenPressed(new ExtendArm(2.5, 2.5));
         bridgeButton.whenReleased(new RetractArm(2.5, 2.5));
         rollerButton.whileHeld(new RollRoller());
     }
 }
