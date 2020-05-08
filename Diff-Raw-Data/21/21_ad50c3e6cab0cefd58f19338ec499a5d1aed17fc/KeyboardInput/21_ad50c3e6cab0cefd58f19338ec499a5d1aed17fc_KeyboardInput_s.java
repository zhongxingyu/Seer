 package input;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 
 /**
  * KeyboardModule gets input info from keyboard and send appropriate input info to the Input object
  * @author Ying Chen, Gavin Ovsak, aaronkrolik
  *
  */
 @SuppressWarnings("unchecked")
 public class KeyboardInput extends InputDevice{
 
 	public static final String myDevice = "KEYBOARD";
 	ArrayList<KeyState> myKeys;
 	Input myInput;
 	long lastClickTime = 0;
 	
 	public KeyboardInput(JComponent component, Input input) {
 		super(myDevice,input);
 		myKeys = new ArrayList<KeyState>();
 		initialize(component);
 		myInput = input;
 	}
 
 	private void recursivePermutation(String accumulatedKeys, ArrayList<KeyState> keyArray, int maxSize, long time) {
 		if(maxSize == 0) {
 			notifyInputAction("Keyboard_" + accumulatedKeys + "_KeyDown", new AlertObject(time));
 		} else {
 			for(KeyState key : keyArray) {
 				ArrayList<KeyState> modArray = (ArrayList<KeyState>) keyArray.clone();
 				modArray.remove(key);
 				recursivePermutation(key.toString() + accumulatedKeys, modArray, maxSize - 1, time);
 			}
 		}
 	}

 	private void initialize(JComponent component) {
 		component.addKeyListener(new KeyListener() {
 			
 			@Override
 			public void keyPressed(KeyEvent e) {
 				String keyName = KeyboardMappings.getKeyName(e.getKeyCode());
 				KeyState downKey = new KeyState( keyName, e.getWhen());
 				if (!myKeys.contains(downKey)){
 					myKeys.add(downKey);
 				}
 				ArrayList<KeyState> buttonArray = (ArrayList<KeyState>) myKeys.clone();
 				if(buttonArray.size() > 1)
 					notifyInputAction("Keyboard_" + keyName + "_KeyDown", new AlertObject(e.getWhen()));
 				recursivePermutation("", buttonArray, buttonArray.size(), e.getWhen());
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				String keyName = KeyboardMappings.getKeyName(e.getKeyCode());
 				KeyState temp = new KeyState(keyName, e.getWhen());
 				long timeDifference = temp.getTime() - myKeys.remove(myKeys.indexOf(temp)).getTime();
 				notifyInputAction("Keyboard_" + keyName + "_KeyUp",
 							new AlertObject(e.getWhen()));
 				if (timeDifference < Integer.parseInt(myInput.getSetting("ShortClickTimeThreshold")) ) {
 					notifyInputAction("Keyboard_" + keyName + "_ShortClick",
 							new AlertObject(e.getWhen()));
 				}
 				if (timeDifference >= Integer.parseInt(myInput.getSetting("LongClickTimeThreshold"))) {
 					notifyInputAction("Keyboard_" + keyName + "_LongClick",
 							new AlertObject(e.getWhen()));
 				}
 				if (e.getWhen() - lastClickTime < Integer.parseInt(myInput.getSetting("DoubleClickTimeThreshold"))) {
 					notifyInputAction("Keyboard_" + keyName + "_DoubleClick", new AlertObject(e.getWhen()));
 				}
 				lastClickTime = e.getWhen();
 			}
 
 			@Override
 			public void keyTyped(KeyEvent e) {
 			}
 		});
 	}
 	
 	private class KeyState{
 		private final String myName;
 		private final long myTime;
 		
 		public KeyState(String name, long time)  {
 			myName = name;
 			myTime = time;
 		}
 		
 		public String toString(){
 			return myName;
 		}
 		
 		public long getTime() {
 			return myTime;
 		}
 	
 		public boolean equals(Object in){
 			if(in instanceof KeyState){
 				return ( myName.equals(((KeyState) in).myName)  );
 			}
 			return false;
 		}
 	
 	}
 }
