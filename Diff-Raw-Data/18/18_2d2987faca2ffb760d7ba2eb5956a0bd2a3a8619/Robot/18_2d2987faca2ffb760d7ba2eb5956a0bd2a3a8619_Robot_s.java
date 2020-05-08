 package Robot;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Point2D.Double;
 
 import org.apache.commons.logging.Log;
 import org.ros.message.MessageListener;
 import org.ros.message.rss_msgs.BreakBeamMsg;
 import org.ros.message.rss_msgs.MotionMsg;
 import org.ros.message.rss_msgs.OdometryMsg;
 import org.ros.message.rss_msgs.VisionMsg;
 import org.ros.node.topic.Publisher;
 import org.ros.node.topic.Subscriber;
 import org.ros.node.Node;
 
 import Controller.Utility;
 import Grasping.Arm;
 import Grasping.RosArmDriver;
 import Navigation.NavigationMain;
 import VisualServoSolution.VisionMsgWrapper;
 import WaypointDriver.Odometer;
import WaypointDriver.OdometryListener;
 
 public class Robot {
 	Arm arm;
 	RosArmDriver armDriver;
 	// Wheels wheels;
 	// BreakBeamSensor handBreakBeam;
 	// SonarSensor sideSonars[4];
 	
 	private RobotState robotState;
 	NavigationMain navigationMain;
 	Odometer odom;
 	VisionMsgWrapper vision;
 	private boolean doneMoving;
 
 	private Subscriber<VisionMsg> visionSub;
 	private Subscriber<BreakBeamMsg> doneMovingSub;
 
 	private Publisher<OdometryMsg> waypointCommandPub;
 	private Publisher<OdometryMsg> angleCommandPub;
 	private Publisher<MotionMsg> movePub;
 	private Publisher<BreakBeamMsg> stopPub;
 	public Log log;
 
 	public Robot(Node node) {
 		this.log = node.getLog();
 		this.navigationMain = new NavigationMain(node);
 		this.arm = new Arm();
 		this.armDriver = new RosArmDriver(node);
 
 		this.visionSub = node.newSubscriber("rss/VisionMain", "rss_msgs/VisionMsg");
 
 		this.doneMovingSub = node.newSubscriber("rss/waypointcomplete", "rss_msgs/BreakBeamMsg");
 		this.waypointCommandPub = node.newPublisher("rss/waypointcommand","rss_msgs/OdometryMsg");
 		this.angleCommandPub = node.newPublisher("rss/anglecommand", "rss_msgs/OdometryMsg");
 
 		this.stopPub = node.newPublisher("rss/stopcommand",
 				"rss_msgs/BreakBeamMsg");
 		this.movePub = node
 				.newPublisher("command/Motors", "rss_msgs/MotionMsg");
 
 		this.odom = new Odometer(node);
 
 		log.info("Waiting for movePub");
		while (movePub.getNumberOfSubscribers() == 0) {
 			// block
 		}
 		log.info("Done waiting for movePub");
 		this.vision = new VisionMsgWrapper();
 		this.visionSub.addMessageListener(new VisionMessageListener());
 		this.doneMovingSub.addMessageListener(new DoneMovingListener());
 	}
 
 	public class VisionMessageListener implements
 			MessageListener<org.ros.message.rss_msgs.VisionMsg> {
 		public void onNewMessage(org.ros.message.rss_msgs.VisionMsg vm) {
 			vision = new VisionMsgWrapper(vm);
 		}
 	}
 
 	public class DoneMovingListener implements
 			MessageListener<org.ros.message.rss_msgs.BreakBeamMsg> {
 		public void onNewMessage(org.ros.message.rss_msgs.BreakBeamMsg bb) {
 			log.info("Done moving message received");
 			doneMoving = bb.beamBroken; // probably always true
 		}
 	}
 
 	public void setStateObject(RobotState newRobotState) {
 		log.info("Setting new state" + newRobotState.getClass());
 		robotState = newRobotState;
 	}
 
 	public RobotState getRobotState() {
 		return robotState;
 	}
 
 	public boolean doneMoving() {
 		return doneMoving;
 	}
 
 	public void stopMoving() {
 		BreakBeamMsg stop = new BreakBeamMsg();
 		stop.beamBroken = true;
 		stopPub.publish(stop);
 	}
 
 	public void driveToLocation(Point2D.Double loc) {
 		log.info("go to location" + loc);
 		OdometryMsg dest = new OdometryMsg();
 		dest.x = loc.x;
 		dest.y = loc.y;
 		waypointCommandPub.publish(dest);
 		doneMoving = false;
 	}
 
 	public void rotateToLocation(Point2D.Double loc) {
 		log.info("setting angle based on point2d" + loc);
 		OdometryMsg dest = new OdometryMsg();
 		dest.x = loc.x;
 		dest.y = loc.y;
 		angleCommandPub.publish(dest);
 		doneMoving = false;
 	}
 
 	public void rotateToTheta(double theta) {
 		// make an arbitrary point that is theta away
 		// TODO sign ok?
 		Point2D.Double p = new Point2D.Double(Math.cos(theta), Math.sin(theta));
 		rotateToLocation(p);
 	}
 
 	public Double getCurrentLocation() {
 		return this.odom.getPosition();
 	}
 
 	public Double getBlockLocation() {
 		Double us = this.odom.getPosition();
 		return new Double(us.x + this.vision.getBlockDistance()
 				* Math.cos(this.vision.getBlockTheta()), us.y
 				+ this.vision.getBlockDistance()
 				* Math.sin(this.vision.getBlockTheta()));
 	}
 
 	// NO MORE METHODS HERE!!!! NO MORE METHODS HERE!!!!
 	// instead of doing robot.resetArm()
 	// put the methods where they logically belong in the hierarchy
 	// eg., robot.arm.reset()
 	// instead of writing robot.getBlockDistance()
 	// use robot.vision.getBlockDistance()
 }
