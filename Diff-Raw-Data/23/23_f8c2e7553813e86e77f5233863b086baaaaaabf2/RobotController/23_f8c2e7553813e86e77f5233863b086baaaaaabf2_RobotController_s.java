 package grouppractical.robot;
 
 import grouppractical.client.commands.Command;
 import grouppractical.client.commands.RDistanceCommand;
 import grouppractical.client.commands.RRotateCommand;
 import grouppractical.client.commands.RStatusCommand;
 import grouppractical.server.CommandListener;
 import grouppractical.utils.map.Position;
 
 import java.util.concurrent.Semaphore;
 
 import javax.swing.event.EventListenerList;
 
 public class RobotController implements CommandListener {
 	private final EventListenerList listeners;
 	private final Semaphore sem;
 	
 	private Vector2D pos;
 	/** Direction in Radians */
 	private double angle;
 	/** Is the robot's position locked? */
 	private boolean locked;
 	private float voltage;
 
 	public RobotController() {
 		this.listeners = new EventListenerList();
 		this.sem = new Semaphore(1);
 		this.pos = new Vector2D(500, 500);
 		this.angle = 0.0;
 	}
 	
 	public boolean isLocked() {
 		return locked;
 	}
 	
 	public RStatusCommand getStatus() {
 		Position p = new Position((int)pos.getX(), (int)pos.getY(), false, Position.MAX_CERTAINTY);
 		return RStatusCommand.constructFromRadians(p, voltage, angle);
 	}
 	
 	public void setVoltage(float voltage) {
 		if (Math.abs(voltage - this.voltage) >= 0.1) {
 			this.voltage = voltage;
 			updateListeners();
 		}
 	}
 
 	@Override
 	public void enqueueCommand(Command cmd) {
 		sem.acquireUninterruptibly();
 		boolean changed = false;
 		switch (cmd.getCommandType()) {
 		case RDISTANCE:
 			if (locked) break;
 			RDistanceCommand distcmd = (RDistanceCommand) cmd;
 			double dist = (double) distcmd.getDistance();
 			Vector2D v = new Vector2D(0.0, dist);
 			v.rotate(angle);
 			pos.translate(v);
 			changed = true;
 			break;
 		case RROTATE:
 			if (locked) break;
 			RRotateCommand anglecmd = (RRotateCommand) cmd;
 			double turnAngle = (double) anglecmd.getAngleRadians();
			angle += turnAngle;
 			changed = true;
 			break;
 		case RLOCK:
 			locked = true;
 			break;
 		case RUNLOCK:
 			locked = false;
 			break;
 		default:
 			break;
 		}
 		sem.release();
 		if (changed) updateListeners();
 	}
 	
 	private void updateListeners() {
 		for (CommandListener l : listeners.getListeners(CommandListener.class))
 			l.enqueueCommand(getStatus());
 	}
 	
 	/**
 	 * Adds a CommandListener, which will be called when the robot's status changes
 	 * @param l CommandListener to add
 	 */
 	public void addCommandListener(CommandListener l) {
 		listeners.add(CommandListener.class, l);
 	}
 	/**
 	 * Removes a CommandListener
 	 * @param l CommandListener to remove
 	 */
 	public void removeCommandListener(CommandListener l) {
 		listeners.remove(CommandListener.class, l);
 	}
 }
