 package net.fourbytes.shadow;
 
 import net.fourbytes.shadow.Input.Key;
 import net.fourbytes.shadow.Input.Key.Triggerer;
 import net.fourbytes.shadow.MenuLevel.MenuItem;
 
 import com.badlogic.gdx.controllers.Controller;
 import com.badlogic.gdx.controllers.ControllerListener;
 import com.badlogic.gdx.controllers.Controllers;
 import com.badlogic.gdx.controllers.PovDirection;
 import com.badlogic.gdx.controllers.mappings.Ouya;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.ObjectMap;
 import com.badlogic.gdx.utils.ObjectMap.Entry;
 
 public final class ControllerHelper implements ControllerListener {
 	
 	public static abstract class ControllerInput {
 		public Controller controller;
 		
 		protected ControllerInput(Controller controller) {
 			this.controller = controller;
 		}
 		
 		public abstract String getLabel();
 	}
 	
 	public static class ControllerButton extends ControllerInput {
 		
 		public int buttonCode;
 		
 		public ControllerButton(Controller controller, int buttonCode) {
 			super(controller);
 			this.buttonCode = buttonCode;
 		}
 		
 		@Override
 		public String getLabel() {
 			return "Button "+buttonCode;
 		}
 		
 		@Override
 		public int hashCode() {
 			return controller.hashCode()*buttonCode;
 		}
 		
 		@Override
 		public boolean equals(Object o) {
 			if (o instanceof ControllerButton) {
 				ControllerButton button = (ControllerButton) o;
 				return button.controller.equals(controller) && button.buttonCode == buttonCode;
 			}
 			return false;
 		}
 	}
 	
 	public static class ControllerAxis extends ControllerInput {
 		
 		public int axisCode;
 		public boolean negative;
 		
 		public ControllerAxis(Controller controller, int axisCode, boolean negative) {
 			super(controller);
 			this.axisCode = axisCode;
 			this.negative = negative;
 		}
 		
 		@Override
 		public String getLabel() {
 			return "Axis "+axisCode;
 		}
 		
 		@Override
 		public int hashCode() {
 			int hash = controller.hashCode()*axisCode;
 			if (negative) {
 				hash = -hash;
 			}
 			return hash;
 		}
 		
 		@Override
 		public boolean equals(Object o) {
 			if (o instanceof ControllerAxis) {
 				ControllerAxis axis = (ControllerAxis) o;
 				return axis.controller.equals(controller) && axis.axisCode == axisCode && axis.negative == negative;
 			}
 			return false;
 		}
 	}
 	
 	public static float deadzone = 0.15f;
 
 	public ObjectMap<Key, Array<ControllerInput>> mapping = new ObjectMap<Key, Array<ControllerInput>>();
 	public ObjectMap<Key, Array<ControllerInput>> tmpmap = new ObjectMap<Key, Array<ControllerInput>>();
 	
 	private Key tmpkey;
 	public Key assignKey;
 	public MenuItem assignKeyHelper;
 	
 	public ControllerHelper() {
 		for (Controller controller : Controllers.getControllers()) {
 			connected(controller);
 		}
 	}
 	
 	public void refreshMapping() {
 		tmpmap.clear();
 		Array<Controller> controllers = Controllers.getControllers();
 		for (Entry<Key, Array<ControllerInput>> entry : mapping.entries()) {
 			Array<ControllerInput> inputs = entry.value;
 			Key key = entry.key;
 			
 			for (ControllerInput input : inputs) {
 				if (!controllers.contains(input.controller, true)) {
 					inputs.removeValue(input, false);
 				}
 			}
 			
 			if (inputs.size == 0) {
 				mapping.remove(key);
 			}
 		}
 	}
 	
 	public Array<Key> getKeysForInput(ControllerInput input) {
 		Array<Key> keys = new Array<Key>();
 		for (Entry<Key, Array<ControllerInput>> entry : mapping.entries()) {
 			Array<ControllerInput> einputs = entry.value;
 			Key ekey = entry.key;
 			if (einputs.contains(input, false)) {
 				keys.add(ekey);
 			}
 		}
 		return keys;
 	}
 	
 	public String getInputLabelForKey(Key key) {
 		Array<ControllerInput> inputs = mapping.get(key);
 		if (inputs == null) {
 			return "NONE";
 		}
 		if (inputs.size == 1) {
 			ControllerInput input = inputs.get(0);
 			if (input == null) {
 				return "NONE";
 			}
 			return input.getLabel();
 		}
 		return "AUTO";
 	}
 	
 	public void map(Key key, ControllerInput input) {
 		Array<ControllerInput> inputs = mapping.get(key);
 		if (inputs == null) {
 			inputs = new Array<ControllerInput>(2);
 			mapping.put(key, inputs);
 		}
 		inputs.add(input);
 	}
 	
 	public void tick() {
 		if (assignKey != null) {
 			if (assignKeyHelper != null) {
 				assignKeyHelper.text = assignKey.name+" ("+Shadow.controllerHelper.getInputLabelForKey(assignKey)+") ...";
 			}
 			if (tmpkey != null && tmpkey != assignKey) {
 				throw new Error("Switching key to assign while assigning key not supported!");
 			}
 			tmpkey = assignKey;
 		} else {
 			tmpkey = null;
 		}
 		
 		//"Unstick" axes.
 		for (Entry<Key, Array<ControllerInput>> entry : mapping.entries()) {
 			Array<ControllerInput> inputs = entry.value;
 			Key key = entry.key;
 			
 			for (ControllerInput input : inputs) {
 				if (input instanceof ControllerAxis) {
 					ControllerAxis axis = (ControllerAxis) input;
 					float value = axis.controller.getAxis(axis.axisCode);
 					if ((value < -deadzone && axis.negative) || (value > deadzone && !axis.negative)) {
 						//Do nothing - the listener should handle this.
 					} else {
						if (key.triggerer == Triggerer.CONTROLLER && key.isDown) {
 							key.nextState = false;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void connected(Controller controller) {
 		String os = System.getProperty("os.name").toLowerCase();
 		if ((os.equals("linux") || os.equals("unix")) && controller.getName().contains("PLAYSTATION(R)3")) {
 			//Sidenote: Third-Party bluetooth PS3 controllers should use the same button mapping as the original controller. 
 			//Otherwise, the PS3 itself would even have problems with the button IDs being different.
 			System.out.println("Automapping PS3 controller on Linux...");
 			//TODO
 			map(Input.up, new ControllerButton(controller, 4));
 			map(Input.down, new ControllerButton(controller, 6));
 			map(Input.left, new ControllerButton(controller, 7));
 			map(Input.right, new ControllerButton(controller, 5));
 			map(Input.up, new ControllerAxis(controller, 1, true));
 			map(Input.down, new ControllerAxis(controller, 1, false));
 			map(Input.left, new ControllerAxis(controller, 0, true));
 			map(Input.right, new ControllerAxis(controller, 0, false));
 			map(Input.jump, new ControllerButton(controller, 14));
 			map(Input.pause, new ControllerButton(controller, 3));
 			map(Input.enter, new ControllerButton(controller, 14));
 			map(Input.androidBack, new ControllerButton(controller, 13));
 		}
 		if (Ouya.ID.equals(controller.getName())) {
 			System.out.println("Automapping Ouya controller...");
 			//TODO
 			map(Input.up, new ControllerButton(controller, Ouya.BUTTON_DPAD_UP));
 			map(Input.down, new ControllerButton(controller, Ouya.BUTTON_DPAD_DOWN));
 			map(Input.left, new ControllerButton(controller, Ouya.BUTTON_DPAD_LEFT));
 			map(Input.right, new ControllerButton(controller, Ouya.BUTTON_DPAD_RIGHT));
 			map(Input.up, new ControllerAxis(controller, Ouya.AXIS_LEFT_Y, true));
 			map(Input.down, new ControllerAxis(controller, Ouya.AXIS_LEFT_Y, false));
 			map(Input.left, new ControllerAxis(controller, Ouya.AXIS_LEFT_X, true));
 			map(Input.right, new ControllerAxis(controller, Ouya.AXIS_LEFT_X, false));
 			map(Input.jump, new ControllerButton(controller, Ouya.BUTTON_O));
 			map(Input.pause, new ControllerButton(controller, Ouya.BUTTON_MENU));
 			map(Input.enter, new ControllerButton(controller, Ouya.BUTTON_O));
 			map(Input.androidBack, new ControllerButton(controller, Ouya.BUTTON_A));
 		}
 		
 		refreshMapping();
 	}
 	
 	@Override
 	public void disconnected(Controller controller) {
 	}
 	
 	@Override
 	public boolean buttonDown(Controller controller, int buttonCode) {
 		//System.out.println("Pressed button "+buttonCode+" on controller "+controller);
 		ControllerButton button = new ControllerButton(controller, buttonCode);
 		for (Key key : getKeysForInput(button)) {
 			//System.out.println("ControllerHelper triggered key \""+key.name+"\"'s nextstate to true");
			key.triggerer = Triggerer.CONTROLLER;
 			key.nextState = true;
 		}
 		if (assignKey != null) {
 			System.out.println("Mapped in-game key \""+assignKey.name+"\" to button "+buttonCode+" on controller "+controller);
 			map(assignKey, button);
 			refreshMapping();
 			if (assignKeyHelper != null) {
 				assignKeyHelper.text = assignKey.name+" ("+Shadow.controllerHelper.getInputLabelForKey(assignKey)+")";
 			}
 			assignKey = null;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean buttonUp(Controller controller, int buttonCode) {
 		//System.out.println("Released button "+buttonCode+" on controller "+controller);
 		ControllerButton button = new ControllerButton(controller, buttonCode);
 		for (Key key : getKeysForInput(button)) {
 			//System.out.println("ControllerHelper triggered key \""+key.name+"\"'s nextstate to false");
			key.triggerer = Triggerer.CONTROLLER;
 			key.nextState = false;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean axisMoved(Controller controller, int axisCode, float value) {
 		//TODO
 		float pvalue = value;
 		boolean negative = false;
 		if (value < 0f) {
 			pvalue = -value;
 			negative = true;
 		}
 		ControllerAxis axis = new ControllerAxis(controller, axisCode, negative);
 		if (pvalue >= deadzone) {//Internal deadzone.
 			//System.out.println("Moved axis "+axisCode+" with current value "+value+" on controller "+controller);
 			for (Key key : getKeysForInput(axis)) {
 				//System.out.println("ControllerHelper triggered key \""+key.name+"\"'s nextstate to true");
				key.triggerer = Triggerer.CONTROLLER;
 				key.nextState = true;
 			}
 			if (pvalue >= 0.25f && assignKey != null) {//To eliminate minor accidental movements while assigning
 				System.out.println("Mapped in-game key \""+assignKey.name+"\" to axis "+axisCode+" on controller "+controller);
 				map(assignKey, axis);
 				refreshMapping();
 				if (assignKeyHelper != null) {
 					assignKeyHelper.text = assignKey.name+" ("+Shadow.controllerHelper.getInputLabelForKey(assignKey)+")";
 				}
 				assignKey = null;
 			}
 		} else {
 			for (Key key : getKeysForInput(axis)) {
 				//System.out.println("ControllerHelper triggered key \""+key.name+"\"'s nextstate to true");
				key.triggerer = Triggerer.CONTROLLER;
 				key.nextState = false;
 			}
 			//System.out.println("Moved axis "+axisCode+" with current value "+value+" on controller "+controller+" inside \"internal deadzone\"");
 		}
 		return false;
 	}
 
 	@Override
 	public boolean povMoved(Controller controller, int povCode,
 			PovDirection value) {
 		//TODO
 		//System.out.println("Moved pov "+povCode+" with current value "+value+" on controller "+controller);
 		return false;
 	}
 
 	@Override
 	public boolean xSliderMoved(Controller controller, int sliderCode,
 			boolean value) {
 		//TODO
 		//System.out.println("Moved X slider "+sliderCode+" with current value "+value+" on controller "+controller);
 		return false;
 	}
 
 	@Override
 	public boolean ySliderMoved(Controller controller, int sliderCode,
 			boolean value) {
 		//TODO
 		//System.out.println("Moved Y slider "+sliderCode+" with current value "+value+" on controller "+controller);
 		return false;
 	}
 
 	@Override
 	public boolean accelerometerMoved(Controller controller,
 			int accelerometerCode, Vector3 value) {
 		//TODO
 		//System.out.println("Moved accelerometer "+accelerometerCode+" with current value "+value+" on controller "+controller);
 		return false;
 	}
 	
 }
