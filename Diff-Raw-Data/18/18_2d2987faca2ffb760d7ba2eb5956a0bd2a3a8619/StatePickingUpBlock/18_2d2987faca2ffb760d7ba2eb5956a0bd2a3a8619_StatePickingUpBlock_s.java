 package Robot;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.geom.Point2D.Double;
 import java.io.File;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.ros.message.lab5_msgs.GUIEraseMsg;
 import org.ros.message.lab5_msgs.GUISegmentMsg;
 import org.ros.message.lab6_msgs.GUIRectMsg;
 import org.ros.node.parameter.ParameterTree;
 
 import Controller.AngleController;
 import Grasping.Arm;
 
 public class StatePickingUpBlock extends RobotState {
 
 	public StatePickingUpBlock(Robot ri) {
 		super(ri);
 	}
 
 	enum State {
 		APPROACHING_BLOCK, MOVING_ARM, STORING_BLOCK, CHECK_HOLDING_PEN
 	}
 
 	@Override
 	public void perform() {
 
 		State state = State.APPROACHING_BLOCK;
 		boolean done = false;
 
 		// robot.arm.prepareToPickup();
 
 		while (!done) {
 			switch (state) {
 			case APPROACHING_BLOCK: {
 				robot.stopMoving();
 				state = State.MOVING_ARM;
 				break;
 			}
 			case MOVING_ARM: {
 				if (robot.vision.canSeeBlock() == false) {
 					robot.arm = new Arm(); // reset
 					// blocking arm controller movement
 					robot.armDriver.doMovement(robot.arm);
 					// robot.failedToPickBlock(b);
 					// exitState =
 					// iDontKnowWhatShouldBeTheStateIfWeFail;
 					done = true;
 				} else if (robot.vision.canSeeBlock() == true) {
 					robot.arm.openGripper();
 					robot.arm.lowerArm();
 					// blocking arm controller movement, TODO replace with
 					// non-blocking
 					robot.armDriver.doMovement(robot.arm);
 					state = State.STORING_BLOCK;
 				}
 				break;
 			}
 			case STORING_BLOCK: {
 				robot.arm.closeGripper();
 				// blocking arm controller movement
 				robot.armDriver.doMovement(robot.arm);
 				// do arm movements here
 				robot.arm.raiseArm();
 				break;
 			}
 			case CHECK_HOLDING_PEN: {
 				// wait a little bit for the block to be acknowledged
 				// this state shouldn't really be necessary though...
 
 				break;
 			}
 			} // end switch
 		} // end while
 
 		// state transition
 		this.robot.setStateObject(new StateLookingForBlocks(this.robot));
 
 	}
 
 }
