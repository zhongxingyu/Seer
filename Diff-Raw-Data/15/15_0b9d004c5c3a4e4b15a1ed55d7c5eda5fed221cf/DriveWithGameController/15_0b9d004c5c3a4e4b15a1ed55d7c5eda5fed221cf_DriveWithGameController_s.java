 package frc.t4069.robots.commands;
 
 import edu.wpi.first.wpilibj.DriverStation;
 import edu.wpi.first.wpilibj.Joystick;
 import frc.t4069.utils.GameController;
 import frc.t4069.utils.GameController.EventHandler;
 
 public class DriveWithGameController extends CommandBase {
 
 	private GameController m_gc;
 
 	private Joystick m_joystickRight;
 	private Joystick m_joystickLeft;
 	private DriverStation m_ds;
 	private double m_speedlimit = 1;
 
 	public DriveWithGameController() {
 
 	}
 
 	protected void initialize() {
 		// TODO Auto-generated method stub
 		m_gc = oi.getController();
 		m_joystickLeft = oi.getLeftJoystick();
 		m_joystickRight = oi.getRightJoystick();
 		m_ds = DriverStation.getInstance();
 	}
 
 	protected void execute() {
 		double sensitivity = m_ds.getAnalogIn(2) / 5.0;
 		m_gc.tick();
 
 		m_gc.addButtonHandler(GameController.BTN_START, new EventHandler() {
 			public void buttonUp() {
 				if (m_speedlimit == 1)
 					m_speedlimit = 0.5;
 				else
 					m_speedlimit = 1;
 			}
 		});
 
 		processCamera(m_gc);
 		processRoller();
 		processDriveTrain(m_gc, sensitivity);
 		processArm(m_gc);
 		processConveyorShooter();
 	}
 
 	protected void processRoller() {
 		if (m_joystickLeft.getRawButton(1))
 			pickupArm.runRoller(m_ds.getAnalogIn(3) / 5.0);
 		else
 			pickupArm.runRoller(0);
 	}
 
 	protected void processArm(GameController gc) {
		double speed = m_joystickLeft.getY();
		speed = (speed < 0.000001) ? gc.getRightStick().y : speed;
 		pickupArm.setArm(-speed / 2.0);
 	}
 
 	protected void processDriveTrain(GameController gc, double turnSensitivity) {
 		if (gc.getButton(GameController.BTN_RB)
 				|| gc.getButton(GameController.BTN_LB))
 			drivetrain.hardBreak();
 		else
 			drivetrain.arcadeDrive(m_speedlimit * gc.getTrigger(), m_speedlimit
 					* gc.getLeftStick().x * turnSensitivity);
 	}
 
 	protected void processCamera(GameController gc) {
 		// TODO: Sensititivty..g
		double y = (m_joystickRight.getX() + 1) / 4.0 + 0.25;
		double x = (m_joystickLeft.getY() + 1) / 2.0;
		cameraMount.setTilt(-y);
 		cameraMount.setPan(x);
 	}
 
 	protected void processConveyorShooter() {
 		double shooterSpeed = 0;
 		if (m_gc.getButton(GameController.BTN_X))
 			shooterSpeed = 0.4;
 		else if (m_gc.getButton(GameController.BTN_A))
 			shooterSpeed = 0.7;
 		else if (m_gc.getButton(GameController.BTN_B)) shooterSpeed = 1;
 
		shooter.set(shooterSpeed);
 
 		double voltage = shooter.getVoltage();
 		System.out.println("Shooter Speed: " + voltage);
 		if (shooterSpeed > 0.1 && shooter.isShooterReady())
 			conveyor.reverse();
 		else if (!shooter.isBallThere())
 			conveyor.reverse();
 		else
 			conveyor.stop();
 
 	}
 
 	protected void interrupted() {
 
 	}
 
 	protected boolean isFinished() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	protected void end() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
