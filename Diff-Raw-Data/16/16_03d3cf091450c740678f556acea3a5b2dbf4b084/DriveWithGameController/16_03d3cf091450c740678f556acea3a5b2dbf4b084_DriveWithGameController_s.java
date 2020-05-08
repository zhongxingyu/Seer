 package frc.t4069.robots.commands;
 
 import edu.wpi.first.wpilibj.DriverStation;
 import frc.t4069.utils.GameController;
 import frc.t4069.utils.GameController.EventHandler;
 import frc.t4069.utils.math.Point;
 
 public class DriveWithGameController extends CommandBase {
 
 	private GameController m_gc;
 	private DriverStation m_ds;
 	private double m_speedlimit = 1;
 
 	public DriveWithGameController() {
 
 	}
 
 	protected void initialize() {
 		// TODO Auto-generated method stub
 		m_gc = oi.getController();
 		m_ds = DriverStation.getInstance();
 	}
 
 	protected void execute() {
 		double sensitivity = m_ds.getAnalogIn(2) / 5.0;
 		m_gc.tick();
 
 		// Roller Handler
 		m_gc.addButtonHandler(GameController.BTN_X, new EventHandler() {
 			public void buttonUp() {
 				m_rollerRunning = !m_rollerRunning;
 			}
 		});
 
 		// Shooter
 		m_gc.addButtonHandler(GameController.BTN_B, new EventHandler() {
 			public void buttonUp() {
 				shooter.shoot();
 			}
 		});
 
		m_gc.addButtonHandler(GameController.BTN_START, new EventHandler(){
			public void buttonUp(){
				if (m_speedlimit == 1) m_speedlimit = 0.4;
 				else
 					m_speedlimit = 1;
 			}
		})
 		processCamera(m_gc);
 		processRoller();
 		processDriveTrain(m_gc, sensitivity);
 		processArm(m_gc, m_ds.getAnalogIn(1) / 5.0);
 		processConveyor();
 	}
 
 	private boolean m_rollerRunning = false;
 
 	public boolean rollerRunning() {
 		return m_rollerRunning;
 	}
 
 	protected void processRoller() {
 		if (m_rollerRunning)
 			pickupArm.runRoller(m_ds.getAnalogIn(3) / 5.0);
 		else
 			pickupArm.runRoller(0);
 	}
 
 	protected void processArm(GameController gc, double sensitivity) {
 		if (gc.getButton(GameController.BTN_Y))
			pickupArm.setArm(sensitivity);
 		else if (gc.getButton(GameController.BTN_A))
			pickupArm.setArm(-sensitivity);
 		else
 			pickupArm.stop();
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
 		Point rightStick = gc.getRightStick();
 		double y = (rightStick.y + 1) / 4.0 + 0.25;
 		double x = (rightStick.x + 1) / 2.0;
 		cameraMount.setTilt(y);
 		cameraMount.setPan(x);
 	}
 
 	protected void processConveyor() {
 		if (shooter.isShooting())
 			conveyor.reverse();
 		else if (!shooter.isBallThere())
 			conveyor.reverse();
 		else
 			conveyor.stop();
 	}
 
 	public void stopRoller() {
 		m_rollerRunning = false;
 	}
 
 	protected void interrupted() {
 		stopRoller();
 	}
 
 	protected boolean isFinished() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	protected void end() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
