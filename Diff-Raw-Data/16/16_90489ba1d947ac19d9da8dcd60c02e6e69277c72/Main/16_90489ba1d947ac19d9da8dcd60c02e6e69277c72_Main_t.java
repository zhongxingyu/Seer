 import java.io.IOException;
 import java.lang.Math;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.leapmotion.leap.*;
 import com.leapmotion.leap.Controller.PolicyFlag;
 import com.leapmotion.leap.Gesture.State;
 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.event.KeyEvent;
 
 class GestureInfo {
 	Pointable p;
 	Vector direction;
 	Vector position;
 
 	public GestureInfo(Pointable p, Vector direction, Vector position) {
 		super();
 		this.p = p;
 		this.direction = direction;
 		this.position = position;
 	}
 
 }
 
 public class Main extends Listener {
 	Robot _robot;
 
 	Map<Integer, GestureInfo> boundFingers = new HashMap();
 	List<Integer> circles = new ArrayList();
 	List<Integer> lrSwipes = new ArrayList();
 	Mappings _mappings;
 
 	public Main() {
 		try {
 			_robot = new Robot();
 		} catch (AWTException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		_robot.delay(2000);
 		_mappings = new Mappings(_robot);
 		// example: mappings.doAction(Actions.SWIPE_LEFT);
 	}
 
 	private void keyPress(String action) {
 		_mappings.doAction(action);
 	}
 
 	public void onInit(Controller controller) {
 	}
 
 	public void onConnect(Controller controller) {
 		controller.enableGesture(Gesture.Type.TYPE_SWIPE);
 		controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
 		controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
 		controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
 
 		Config config = controller.config();
 
 		config.setFloat("Gesture.Swipe.MinLength", 80);
 		config.setFloat("Gesture.Swipe.MinVelocity", 200);
 
 		config.setFloat("Gesture.Circle.MinArc", (float) Math.PI * 1.8f);
 		config.setFloat("Gesture.Circle.MinRadius", 25);
 
 		config.save();
 	}
 
 	public void onDisconnect(Controller controller) {
 		// Note: not dispatched when running in a debugger.
 		System.out.println("Disconnected");
 	}
 
 	public void onExit(Controller controller) {
 		System.out.println("Exited");
 	}
 
 	public void goDown(Pointable p, Vector position) {
 		if (!boundFingers.containsKey(p.id())) {
 			GestureInfo gi = new GestureInfo(p, Vector.down(), position);
 			boundFingers.put(p.id(), gi);
 		}
 		if (boundFingers.get(p.id()).direction.equals(Vector.down())) {
 			boundFingers.get(p.id()).position = position;
 			System.out.println("going down " + p.id());
 			keyPress(Actions.SWIPE_DOWN);
 		}
 	}
 
 	public void goUp(Pointable p, Vector position) {
 		if (!boundFingers.containsKey(p.id())) {
 			GestureInfo gi = new GestureInfo(p, Vector.up(), position);
 			boundFingers.put(p.id(), gi);
 		}
 		if (boundFingers.get(p.id()).direction.equals(Vector.up())) {
 			boundFingers.get(p.id()).position = position;
 			System.out.println("going up " + p.id());
 			keyPress(Actions.SWIPE_UP);
 		}
 	}
 
 	public void goDirection(Pointable f) {
 		GestureInfo gi = boundFingers.get(f.id());
 		Vector v = gi.direction;
 		if (v.equals(Vector.up())) {
 			goUp(f, gi.position);
 		}
 		if (v.equals(Vector.down())) {
 			goDown(f, gi.position);
 		}
 	}
 
 	public void onFrame(Controller controller) {
 		Frame frame = controller.frame();
 		System.out.println(frame.hands().count());
 		Hand hand = frame.hands().frontmost();
 		// if(hand != null) {
 		// Frame lastFrame = controller.frame(5);
 		// Hand lastHand = lastFrame.hand(hand.id());
 		// System.out.println("hands: "+hand.pointables().count()+", "+hand.pointables().count());
 		// if(hand.pointables().count() >= 2 && lastHand.pointables().count() >=
 		// 2 ) {
 		// float lastRadius = lastHand.sphereRadius();
 		// float currRadius = hand.sphereRadius();
 		//
 		// float radiusDifference = currRadius - lastRadius;
 		// float threshold = 1.5f;
 		//
 		// System.out.println("radiusDifference: " + radiusDifference);
 		// if( radiusDifference > threshold ) {
 		// keyPress(Actions.ZOOM_OUT);
 		// } else if (radiusDifference < -threshold) {
 		// keyPress(Actions.ZOOM_IN);
 		// }
 		//
 		// }
 		// return;
 		// }
 		PointableList fingers = frame.pointables();
 		System.out.println(fingers.count());
 
 		if (fingers.count() != 1)
 			return;
 
 		for (Pointable finger : fingers) {
 			if (boundFingers.containsKey(finger.id())) {
 				GestureInfo gi = boundFingers.get(finger.id());
 				goDirection(finger);
 				if (finger.tipPosition().distanceTo(gi.position) > 70) {
 					boundFingers.remove(finger.id());
 				}
 			}
 		}
 		GestureList gestures = frame.gestures();
 		for (int i = 0; i < gestures.count(); i++) {
 			Gesture gesture = gestures.get(i);
 
 			switch (gesture.type()) {
 			// Scroll up/down
 			case TYPE_SWIPE:
 				float CONE_ANGLE = (float) Math.PI / 3;
 				SwipeGesture swipe = new SwipeGesture(gesture);
 				if (swipe.direction().angleTo(Vector.down()) < CONE_ANGLE) {
 					goDown(swipe.pointable(), swipe.position());
 				}
 				if (swipe.direction().angleTo(Vector.up()) < CONE_ANGLE) {
 					goUp(swipe.pointable(), swipe.position());
 				}
 
 				if (!lrSwipes.contains(swipe.id())
 						&& swipe.state().equals(Gesture.State.STATE_STOP)) {
 					if (swipe.direction().angleTo(Vector.right()) < CONE_ANGLE
 							&& swipe.position().getX() > 0) {
 						lrSwipes.add(swipe.id());
 						System.out.println("go right");
 						keyPress(Actions.SWIPE_RIGHT);
 					}
 					if (swipe.direction().angleTo(Vector.left()) < CONE_ANGLE
 							&& swipe.position().getX() < 0) {
 						lrSwipes.add(swipe.id());
 						System.out.println("go right");
 						keyPress(Actions.SWIPE_LEFT);
 					}
 				}
 				System.out.println("Swipe id: " + swipe.id() + ", "
 						+ swipe.state() + ", position: " + swipe.position()
 						+ ", direction: " + swipe.direction() + ", speed: "
 						+ swipe.speed());
 				break;
 			// Type "f"
 			case TYPE_SCREEN_TAP:
 				ScreenTapGesture screenTap = new ScreenTapGesture(gesture);
 				System.out.println("Screen Tap id: " + screenTap.id() + ", "
 						+ screenTap.state() + ", position: "
 						+ screenTap.position() + ", direction: "
 						+ screenTap.direction());
 				keyPress(Actions.SCREEN_TAP);
 				break;
 			// Type 1-2-3-4
 			case TYPE_KEY_TAP:
 				KeyTapGesture keyTap = new KeyTapGesture(gesture);
 				System.out.println("Key Tap id: " + keyTap.id() + ", "
 						+ keyTap.state() + ", position: " + keyTap.position()
 						+ ", direction: " + keyTap.direction());
				String numFromLeft = null;
 				float x = keyTap.position().getX();
 				if (x < -70)
					numFromLeft = Actions.KEY_TAP_ONE;
 				else if (x < 0)
					numFromLeft = Actions.KEY_TAP_TWO;
 				else if (x < 70)
					numFromLeft = Actions.KEY_TAP_THREE;
 				else
					numFromLeft = Actions.KEY_TAP_FOUR;
				keyPress(numFromLeft);
 				break;
 			// Go forward/back
 			case TYPE_CIRCLE:
 				CircleGesture circle = new CircleGesture(gesture);
 
 				if (circle.progress() == 0 || circles.contains(circle.id()))
 					break;
 				if (circle.pointable().direction().angleTo(circle.normal()) <= Math.PI / 4) {
 					// clockwise
 					keyPress(Actions.CLOCKWISE);
 				} else {
 					// counterclockwise
 					keyPress(Actions.COUNTER_CLOCKWISE);
 				}
 
 				circles.add(circle.id());
 
 				// Calculate clock direction using the angle between circle
 				// normal and pointable
 				String clockwiseness;
 				if (circle.pointable().direction().angleTo(circle.normal()) <= Math.PI / 4) {
 					// Clockwise if angle is less than 90 degrees
 					clockwiseness = "clockwise";
 				} else {
 					clockwiseness = "counterclockwise";
 				}
 
 				// Calculate angle swept since last frame
 				double sweptAngle = 0;
 				if (circle.state() != State.STATE_START) {
 					CircleGesture previousUpdate = new CircleGesture(controller
 							.frame(1).gesture(circle.id()));
 					sweptAngle = (circle.progress() - previousUpdate.progress())
 							* 2 * Math.PI;
 				}
 
 				System.out.println("Circle id: " + circle.id() + ", "
 						+ circle.state() + ", progress: " + circle.progress()
 						+ ", radius: " + circle.radius() + ", angle: "
 						+ Math.toDegrees(sweptAngle) + ", " + clockwiseness);
 				break;
 
 			default:
 				System.out.println("Unknown gesture type.");
 				break;
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 		Controller controller = new Controller();
 		controller.setPolicyFlags(PolicyFlag.POLICY_BACKGROUND_FRAMES);
 		Main main = new Main();
 
 		controller.addListener(main);
 
 		// Keep this process running until Enter is pressed
 		System.out.println("Press Enter to quit...");
 		try {
 			System.in.read();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// Remove the sample listener when done
 		controller.removeListener(main);
 	}
 
 }
