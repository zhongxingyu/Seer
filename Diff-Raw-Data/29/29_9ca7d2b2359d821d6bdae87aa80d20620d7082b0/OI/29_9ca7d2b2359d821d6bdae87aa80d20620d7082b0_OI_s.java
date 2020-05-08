 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.buttons.Button;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 import edu.wpi.first.wpilibj.templates.commands.ClimbDownCommand;
 import edu.wpi.first.wpilibj.templates.commands.ClimbUpCommand;
 import edu.wpi.first.wpilibj.templates.commands.FireCycle;
 import edu.wpi.first.wpilibj.templates.commands.ReleaseArm;
 import edu.wpi.first.wpilibj.templates.commands.StartLaunchMotor;
 import edu.wpi.first.wpilibj.templates.commands.StopLaunchMotor;
 
 /**
  * This class is the glue that binds the controls on the physical operator
  * interface to the commands and command groups that allow control of the robot.
  */
 public class OI {
     //// CREATING BUTTONS
     // One type of button is a joystick button which is any button on a joystick.
     // You create one by telling it which joystick it's on and which button
     // number it is.
     // Joystick stick = new Joystick(port);
     // Button button = new JoystickButton(stick, buttonNumber);
 
     // Another type of button you can create is a DigitalIOButton, which is
     // a button or switch hooked up to the cypress module. These are useful if
     // you want to build a customized operator interface.
     // Button button = new DigitalIOButton(1);
     // There are a few additional built in buttons you can use. Additionally,
     // by subclassing Button you can create custom triggers and bind those to
     // commands the same as any other Button.
     //// TRIGGERING COMMANDS WITH BUTTONS
     // Once you have a button, it's trivial to bind it to a button in one of
     // three ways:
     // Start the command when the button is pressed and let it run the command
     // until it is finished as determined by it's isFinished method.
     // button.whenPressed(new ExampleCommand());
     // Run the command while the button is being held down and interrupt it once
     // the button is released.
     // button.whileHeld(new ExampleCommand());
     // Start the command when the button is released  and let it run the command
     // until it is finished as determined by it's isFinished method.
     // button.whenReleased(new ExampleCommand());
     public Joystick joy1 = new Joystick(1);
     public Joystick joy2 = new Joystick(2);
     public Joystick joy3 = new Joystick(3);
     public Joystick joy4 = new Joystick(4);
     public Button climbUpButton = new JoystickButton(joy1, 11);
     public Button climbDownButton = new JoystickButton(joy1, 10);
     public Button releaseArmButton1 = new JoystickButton(joy3, 8);
     public Button releaseArmButton2 = new JoystickButton(joy4, 9);
     public Button releaseArmButton = new Button() {
         public boolean get() {
             return releaseArmButton1.get() && releaseArmButton2.get();
         }
     };
     public Button lockonButton = new JoystickButton(joy1, 1);
     public Button startShooterButton1 = new JoystickButton(joy2, 2);
     public Button startShooterButton2 = new JoystickButton(joy4, 2);
     public Button startShooterButton = new Button() {
         public boolean get() {
             return startShooterButton1.get() || startShooterButton2.get();
         }
     };
     public Button fireButton1 = new JoystickButton(joy2, 1);
     public Button fireButton2 = new JoystickButton(joy4, 1);
    public JoystickAxis drivex = new JoystickAxis(joy2, Joystick.AxisType.kX.value);
    public JoystickAxis drivey = new JoystickAxis(joy2, Joystick.AxisType.kY.value);
    public JoystickAxis driveTheta = new JoystickAxis(joy1, Joystick.AxisType.kX.value);
    public JoystickAxis shoulderControl = new JoystickAxis(joy4, Joystick.AxisType.kY.value);
 
     {
         climbUpButton.whenPressed(new ClimbUpCommand());
         climbDownButton.whenPressed(new ClimbDownCommand());
         fireButton1.whenPressed(new FireCycle());
         fireButton2.whenPressed(new FireCycle());
         startShooterButton.whenPressed(new StartLaunchMotor());
         startShooterButton.whenReleased(new StopLaunchMotor());
         releaseArmButton.whenPressed(new ReleaseArm());
     }
 }
