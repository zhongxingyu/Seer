 package AP2DX.reflex;
 
 import java.util.ArrayList;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import AP2DX.AP2DXBase;
 import AP2DX.AP2DXMessage;
 import AP2DX.ConnectionHandler;
 import AP2DX.Message;
 import AP2DX.Module;
 import AP2DX.usarsim.specialized.USonarSensorMessage;
 
 public class Program extends AP2DXBase {
 	private double[] currentDistances;
 	private double[] previousDistances;
 	private double[] approaching;
 	private boolean isBotBlocked = false;
 	private ArrayBlockingQueue<AP2DXMessage> motorBacklog = new ArrayBlockingQueue<AP2DXMessage>(
 			256);
 
 	/**
 	 * Entrypoint of reflex
 	 */
 	public static void main(String[] args) {
 		new Program();
 	}
 
 	/**
 	 * constructor
 	 */
 	public Program() {
 		super(Module.REFLEX); // explicitly calls base constructor
 		System.out.println(" Running Reflex... ");
 
 	}
 
 	/**
 	 * Does the logic for the Reflex module. That is: <br/>
 	 * - check if there is something in front of the vehicle and stop the motor.<br/>
 	 * - forward messages from the Planner to the motor if there is nothing.
 	 * 
 	 * @param message
 	 *            The AP2DX Message that is received and read from the queue.
 	 */
 	@Override
 	public ArrayList<AP2DXMessage> componentLogic(Message message) {
 		ArrayList<AP2DXMessage> messageList = new ArrayList<AP2DXMessage>();
 
 		switch (message.getMsgType()) {
 		case AP2DX_MOTOR_ACTION:
 
 			if (isBotBlocked) {
 				ArrayBlockingQueue<AP2DXMessage> backlog = getMotorBacklog();
 				backlog.add((AP2DXMessage) message);
 				setMotorBacklog(backlog);
 			} else {
 				message.setDestinationModuleId(Module.MOTOR);
 				messageList.add((AP2DXMessage) message);
 			}
 
 			break;
 		case AP2DX_SENSOR_SONAR:
 			USonarSensorMessage msg = (USonarSensorMessage) message;
 
 			setPreviousDistances(getCurrentDistances());
 			setCurrentDistances(msg.getData());
 
 			double[] approaching = new double[getCurrentDistances().length];
 			for (int i = 0; i < getCurrentDistances().length; i++) {
 				if (getCurrentDistances()[i] < getPreviousDistances()[i]) {
 					approaching[i] = 1;
 				} else if (getCurrentDistances()[i] == getPreviousDistances()[i]) {
 					approaching[i] = 0;
 				} else {
 					approaching[i] = -1;
 				}
 			}
 
 			boolean toggleBlocked = false;
 			for (int i = 0; i < getCurrentDistances().length; i++) {
 				if (getCurrentDistances()[i] < 0.75 && approaching[i] == 1) {
 					setBotBlocked(true);
 					toggleBlocked = true;
 				}
 			}
 			if (!toggleBlocked) {
 				setBotBlocked(false);
 			}
 
 			break;
 		default:
 			System.out
 					.println("Error in AP2DX.reflex.Program.componentLogic(Message message) Couldn't deal with message: "
 							+ message.getMsgType());
 		}
 		return messageList;
 	}
 
 	/**
 	 * Here a new thread is started that monitors the state of the bot. If there
 	 * is something in front of it, isBotBlocked will be true. Then all messages
 	 * from the planner will go to in a blocking queue. As soon as the bot is no
 	 * longer blocked, start sending the messages from the planner again.
 	 */
 	@Override
 	public void doOverride() {
 		Thread t1 = new Thread(new MotorPassthrough(this));
		t1.start();
 	}
 
 	/**
 	 * This class represents the thread that will send backlogged messages to
 	 * the motor.
 	 * 
 	 * @author Jasper Timmer
 	 * 
 	 */
 	private class MotorPassthrough implements Runnable {
 		AP2DXBase base;
 
 		public MotorPassthrough(AP2DXBase base) {
 			this.base = base;
 		}
 
 		@Override
 		public void run() {
 			ConnectionHandler conn = null;
 			boolean interrupted = false;
 
 			try {
 				while (conn == null && !interrupted)
 					conn = this.base.getSendConnection(Module.MOTOR);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				interrupted = true;
 			}
 
 			while (!interrupted) {
 
 				Message msg = null;
 
 				try {
 					while (msg == null) {
 						while (isBotBlocked) {
 							Thread.sleep(10);
 						}
 
 						msg = getMotorBacklog().poll(50, TimeUnit.MILLISECONDS);
 					}
 				} catch (InterruptedException e) {
 					interrupted = true;
 				}
 
 				if (!interrupted && msg != null) {
 					msg.setDestinationModuleId(Module.MOTOR);
 					conn.sendMessage(msg);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param currentDistances
 	 *            the currentDistances to set
 	 */
 	public void setCurrentDistances(double[] currentDistances) {
 		this.currentDistances = currentDistances;
 	}
 
 	/**
 	 * @return the currentDistances
 	 */
 	public double[] getCurrentDistances() {
 		return currentDistances;
 	}
 
 	/**
 	 * @param previousDistances
 	 *            the previousDistances to set
 	 */
 	public void setPreviousDistances(double[] previousDistances) {
 		this.previousDistances = previousDistances;
 	}
 
 	/**
 	 * @return the previousDistances
 	 */
 	public double[] getPreviousDistances() {
 		return previousDistances;
 	}
 
 	/**
 	 * @param approaching
 	 *            the approaching to set
 	 */
 	public void setApproaching(double[] approaching) {
 		this.approaching = approaching;
 	}
 
 	/**
 	 * @return the approaching
 	 */
 	public double[] getApproaching() {
 		return approaching;
 	}
 
 	/**
 	 * @param isBotBlocked
 	 *            the isBotBlocked to set
 	 */
 	public void setBotBlocked(boolean isBotBlocked) {
 		this.isBotBlocked = isBotBlocked;
 	}
 
 	/**
 	 * @return the isBotBlocked
 	 */
 	public boolean isBotBlocked() {
 		return isBotBlocked;
 	}
 
 	/**
 	 * @param motorBacklog
 	 *            the motorBacklog to set
 	 */
 	public void setMotorBacklog(ArrayBlockingQueue<AP2DXMessage> motorBacklog) {
 		this.motorBacklog = motorBacklog;
 	}
 
 	/**
 	 * @return the motorBacklog
 	 */
 	public ArrayBlockingQueue<AP2DXMessage> getMotorBacklog() {
 		return motorBacklog;
 	}
 }
