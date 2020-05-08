 package Grasping;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.ros.node.Node;
 import org.ros.node.topic.Publisher;
 import org.ros.node.topic.Subscriber;
 import org.apache.commons.logging.Log;
 import org.ros.message.MessageListener;
 import org.ros.message.rss_msgs.ArmMsg;
 //
 //520 - min for wrist/gripper
 //910 - max for wrist/gripper
 //
 //for a block
 //650-660 - slides out
 //600 - locked in but bad for servo. 640 is best
 //
 //elbow
 //900 - min
 //1800 - max
 //
 //1400 - flat
 //
 //2100 - 0deg
 //1250 - 90deg
 //
 //
 //shoulder 
 //1975 - 90deg
 //1200 - 0deg
 //
 //max 2100
 //min 550
 
 public class RosArmDriver {
 	private final Publisher<ArmMsg> armPub;
 	private Subscriber<ArmMsg> armSub;
 
 	private static Log log;
 	
 	public RosArmDriver(Node node) {
 		log = node.getLog();
 		armPub = node.newPublisher("command/Arm", "rss_msgs/ArmMsg");
 		
 		log.info("Waiting for subscribers");
 		while (armPub.getNumberOfSubscribers() == 0) {
 			// log.info("num sub is 0");
 		}
 		log.info("More than 0 subscribers");
 		
 		armSub = node.newSubscriber("rss/ArmStatus", "rss_msgs/ArmMsg");
 		armSub.addMessageListener(new ArmListener());			
 
 		log.info("Arm is ready");
 	}
 
 	private void sendArmPWM(Arm toArm) {
 		ArmMsg msg = new ArmMsg();
 		msg.pwms = toArm.getPwms();
 		log.info("publishing " + Arrays.toString(msg.pwms));
 		armPub.publish(msg);
 	}
 
 	public void doMovement(Arm toArm) {
 		// set the desired theta of the joints
 		while (toArm.step()) {
 			// just keep calling step
 			log.info("driver: moving arm");
 			sendArmPWM(toArm);
 			try {
 				Thread.sleep(250);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
		log.info("driver: arm movement");
 	}
 
 	public class ArmListener implements MessageListener<ArmMsg> {
 		@Override
 		public void onNewMessage(ArmMsg msg) {
 			//log.info("arm says " + Arrays.toString(msg.pwms));
 		}
 	}
 
 
 }
