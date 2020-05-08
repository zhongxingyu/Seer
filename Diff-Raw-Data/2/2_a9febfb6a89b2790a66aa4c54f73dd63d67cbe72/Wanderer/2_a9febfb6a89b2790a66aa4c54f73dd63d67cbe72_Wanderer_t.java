 /**
  * Wanderer.java
  *
  * Executes a safe wander for a given PlayerClient robot interface.
  *
  * @author Karl Berger
  * @author jmd  12 May 2011
  */
 
 import javaclient3.*;
 import javaclient3.structures.PlayerConstants;
 import javaclient3.structures.ranger.*;
 
 public class Wanderer extends Thread {
 
 	private PlayerClient pc;
 	private Position2DInterface pos;
 	private RangerInterface ranger;
 	private Localizer loc;
 
 	private double x;
 	private double y;
 	private double yaw;
 
 	public static double dx, dy, dyaw;
 	public static boolean updateReady = false;
 	public static double[] ranges;
 
 	public Wanderer( PlayerClient pc, Position2DInterface pos, RangerInterface ranger, Localizer loc ) {
 		this.pc = pc;
 		this.pos = pos;
 		this.ranger = ranger;
 		this.loc = loc;
 
 		x = pos.getX();
 		y = pos.getY();
 		yaw = pos.getYaw();
 
 	}
 
 	public synchronized void updateDataz(double x, double y, double yaw, double[] ranges) {
 	    boolean compound = Wanderer.updateReady;
 	    Wanderer.dx = compound ? Wanderer.dx + x - this.x : x - this.x; 
 	    Wanderer.dy = compound ? Wanderer.dy + y - this.y : y - this.y;
	    Wanderer.dyaw = compound ? Wanderer.dyaw + yaw - this.yaw: yaw - this.yaw;
 	    Wanderer.ranges = ranges;
 	    Wanderer.updateReady = true;
 	    
 	}
 
 	public static synchronized boolean updateReady() {
 		return Wanderer.updateReady;
 	}
 
 	public static void sendUpdate(Localizer l) {
 		synchronized (l) {
 			l.receiveUpdate(Wanderer.dx,
 					Wanderer.dy,
 					Wanderer.dyaw,
 					Wanderer.ranges);
 			Wanderer.updateReady = false;
 		}
 	}
 
 	public void run() {
 		while( loc.isAlive() && !loc.isLocalized() ) {
 
 			double turnrate = 0, fwd = 0;
 			double omega = 20 * Math.PI / 180;
 
 			pc.readAll();
 
 			if (!ranger.isDataReady() || !pos.isDataReady()) {
 				continue;
 			}
 
 			double[] ranges = ranger.getData().getRanges();
 
 			updateDataz(pos.getX(),pos.getY(),pos.getYaw(),ranges);
 
 			x = pos.getX();
 			y = pos.getY();
 			yaw = pos.getYaw();
 
 
 			// do simple collision avoidance
 			double rightval = (ranges[113] + ranges[118]) / 2.0;
 			double leftval = (ranges[569] + ranges[574]) / 2.0;
 			double frontval = (ranges[340] + ranges[345]) / 2.0;
 
 			if (frontval < 0.05) {
 				fwd = 0;
 				if (Math.abs(leftval - rightval) < .025) {
 					turnrate = omega;
 				} else if (leftval > rightval) {
 					turnrate = omega;
 				} else {
 					turnrate = -1 * omega;
 				}
 			} else {
 				fwd = 0.1;
 				if (leftval < 0.05) {
 					fwd = 0.0;
 					turnrate = -1 * omega;
 				} else if (rightval < 0.05) {
 					fwd = 0.0;
 					turnrate = omega;
 				}
 			}
 			pos.setSpeed(fwd, turnrate);
 		}
 	}// run()
 }// Wanderer.java
