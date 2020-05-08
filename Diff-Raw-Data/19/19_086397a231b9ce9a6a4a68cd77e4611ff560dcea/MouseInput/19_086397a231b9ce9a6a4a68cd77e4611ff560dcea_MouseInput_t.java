 package util.input;
 
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JComponent;
 
 /**
  * MouseInput catches events from the Mouse Input Device and passes them to Input.
  * @author Gavin Ovsak, Ying Chen
  */
 public class MouseInput extends InputDevice {
 	JComponent myComponent;
 	public static final String myDevice = "Mouse";
 
 	private Point lastPosition;
 	private Point lastClickPosition;
 	private long lastClickTime = 0;
 	private String lastClickButton = "";
 	private Input myInput;
 	private InputDevice inDev = this;
 	private Map<Integer, String> mouseNameMap;
 	private List<ButtonState> downButtons = new ArrayList<ButtonState>();
 	private boolean gestureAlreadyNotified = false;
 
 	public MouseInput(JComponent component, Input input) {
 		super(myDevice, input);
 		myComponent = component;
 		mouseNameMap = new HashMap<Integer, String>();
 		mouseNameMap.put(MouseEvent.BUTTON1, "Left");
 		mouseNameMap.put(MouseEvent.BUTTON2, "Center");
 		mouseNameMap.put(MouseEvent.BUTTON3, "Right");
 		initialize();
 		myInput = input;
 	}
 
 	private PositionObject makePositionObject(MouseEvent e) {
 		return new PositionObject(e.getX(), myComponent.getWidth(), e.getY(), myComponent.getHeight(), e.getWhen());
 	}
 
 	private RollObject makeWheelObject(MouseWheelEvent e) {
 		return new RollObject(e.getWhen(), e.getWheelRotation());
 	}
 
 	private String getClosestWall() {
 		int[] wallDistances = new int[5];
 		String[] walls = { "Left", "Right", "Top", "Bottom" };
 		wallDistances[0] = lastPosition.x;
 		wallDistances[1] = myComponent.getWidth() - lastPosition.x;
 		wallDistances[2] = lastPosition.y;
 		wallDistances[3] = myComponent.getHeight() - lastPosition.y;
 		int min = 9999;
 		int minIndex = 0;
 		for (int i = 0; i < 4; i++) {
 			int newMin = Math.min(wallDistances[i], min);
 			if (newMin != min) {
 				minIndex = i;
 			}
 			min = newMin;
 		}
 
 		return walls[minIndex];
 	}
 
 	private String getDirection(Point pt) {
 		String[] walls = { "Right", "Left", "Down", "Up" };
 		int index = (Math.abs(pt.x) - Math.abs(pt.y) > 0) ? ((int) Math
 				.signum(pt.x) + 1) / 2 : ((int) Math.signum(pt.y) + 1) / 2 + 2;
 		return walls[index];
 	}
 
 	/**
 	 * Sets up single mouse listener. implements MouseMotionAdapter with
 	 * mouseMove and MouseDrag
 	 */
 	private void initialize() {
 		myComponent.addMouseMotionListener(new MouseMotionAdapter() {
 			public void mouseMoved(MouseEvent e) {
 				lastPosition = e.getPoint();
 				notifyInputAction("Mouse_Move", makePositionObject(e));
 			}
 
 			public void mouseDragged(MouseEvent e) {
 				lastPosition = e.getPoint();
 				notifyInputAction("Mouse_Drag", makePositionObject(e));
 				for (ButtonState downButton : downButtons) {
 					if (!gestureAlreadyNotified
 							&& downButton.getPosition() != null
 							&& lastPosition.distance(downButton.getPosition()) > Integer.parseInt(myInput
 									.getSetting("DoubleClickDistanceThreshold"))) {
 						notifyInputAction(
 								downButton.getFullName()
 										+ "_Drag"
 										+ getDirection(new Point(downButton
 												.getPosition().x
 												- lastPosition.x, downButton
 												.getPosition().y
 												- lastPosition.y)),
 								makePositionObject(e));
 						gestureAlreadyNotified = true;
 					}
 				}
 			}
 		});
 
 		myComponent.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				notifyInputAction("Mouse_MoveIn", makePositionObject(e));
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 				if (lastPosition != null) {
 					notifyInputAction("Mouse_MoveOut", makePositionObject(e));
 					notifyInputAction("Mouse_MoveOut" + getClosestWall(),
 							makePositionObject(e));
 					for (ButtonState downButton : downButtons) {
 						notifyInputAction(downButton.getFullName() + "_DragOut"
 								+ getClosestWall(), makePositionObject(e));
 					}
 					if (!downButtons.isEmpty())
 						notifyInputAction("Mouse_DragOut",
 								makePositionObject(e));
 				}
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				String buttonName = mouseNameMap.get(e.getButton());
 				ButtonState downButton = new ButtonState(myDevice, buttonName,
 						e.getWhen(), inDev, e.getPoint());
 				downButtons.add(downButton);
 				notifyInputAction("Mouse_" + buttonName + "_Down",
						makePositionObject(e));
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				String buttonName = mouseNameMap.get(e.getButton());
 				ButtonState temp = new ButtonState(myDevice, buttonName, e
 						.getWhen(), inDev);
 				notifyInputAction(temp.getFullName() + "_Up",
						makePositionObject(e));
 				notifyInputAction(temp.getFullName() + "_KeyUp",
						makePositionObject(e)); // Legacy Support
 				notifyInputAction(temp.getFullName() + "_Click",
						makePositionObject(e)); // Legacy Support
 				long timeDifference = temp.getTime()
 						- downButtons.remove(downButtons.indexOf(temp))
 								.getTime();
 
 				if (timeDifference < Integer.parseInt(myInput
 						.getSetting("ShortClickTimeThreshold"))) {
 					notifyInputAction(temp.getFullName() + "_ShortClick",
 							makePositionObject(e));
 				}
 				if (timeDifference > Integer.parseInt(myInput
 						.getSetting("LongClickTimeThreshold"))) {
 					notifyInputAction(temp.getFullName() + "_LongClick",
 							makePositionObject(e));
 				}
 				if (lastClickPosition != null
 						&& lastClickButton.equals(buttonName)
 						&& lastClickPosition.distance(e.getPoint()) < Integer.parseInt(myInput
 								.getSetting("DoubleClickDistanceThreshold"))
 						&& e.getWhen() - lastClickTime < Integer.parseInt(myInput
 								.getSetting("DoubleClickTimeThreshold"))) {
 					notifyInputAction(temp.getFullName() + "_DoubleClick",
 							makePositionObject(e));
 				}
 				lastClickPosition = e.getPoint();
 				lastClickTime = e.getWhen();
 				lastClickButton = buttonName;
 				gestureAlreadyNotified = false;
 			}
 		});
 		myComponent.addMouseWheelListener(new MouseWheelListener() {
 			/**
 			 * This will send two types of signals. It will send
 			 * "Mouse_Wheel_Down" signal if the user scrolls the wheel towards
 			 * him and it will send "Mouse_Wheel_Up" signal if the user scrolls
 			 * the wheel away from him.
 			 */
 			@Override
 			public void mouseWheelMoved(MouseWheelEvent e) {
 				if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
 					if (e.getWheelRotation() > 0)
 						notifyInputAction(myDevice + "_Wheel" + "_Down",
 								makeWheelObject(e));
 					else
 						notifyInputAction(myDevice + "_Wheel" + "_Up",
 								makeWheelObject(e));
 				}
 			}
 		});
 
 	}
 
 	public void notifyInput(String keyInfo, MouseEvent e) {
 		notifyInputAction(keyInfo, makePositionObject(e));
 	}
 }
