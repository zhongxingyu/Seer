 package com.bluebananagames.gametemplate.input;
 
 import java.util.HashMap;
 
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.controllers.mappings.Ouya;
 
 public class InputMapping {
 
 	private HashMap<Control, Integer> controlMapping = new HashMap<Control, Integer>();
 	private HashMap<Integer, Control> buttonMapping = new HashMap<Integer, Control>();
 
 	public void mapControl(Control control, int button) {
 		controlMapping.put(control, button);
 		buttonMapping.put(button, control);
 	}
 
 	public Control getMappedControl(int button) {
 		return buttonMapping.get(button);
 	}
 
 	public int getMappedButton(Control control) {
 		return controlMapping.get(control);
 	}
 
 	// FACTORY METHODS
 
 	public static InputMapping OUYAMapping() {
 		InputMapping ouyaMapping = new InputMapping();
 		ouyaMapping.mapControl(Control.DOWN, Ouya.BUTTON_DPAD_DOWN);
 		ouyaMapping.mapControl(Control.UP, Ouya.BUTTON_DPAD_UP);
 		ouyaMapping.mapControl(Control.LEFT, Ouya.BUTTON_DPAD_LEFT);
 		ouyaMapping.mapControl(Control.RIGHT, Ouya.BUTTON_DPAD_RIGHT);
 		ouyaMapping.mapControl(Control.FIRE, Ouya.BUTTON_O);
 		return ouyaMapping;
 	}
 
 	public static InputMapping XBOX360Mapping() {
 		InputMapping xboxMapping = new InputMapping();
		xboxMapping.mapControl(Control.DOWN, 6);
		xboxMapping.mapControl(Control.UP, 5);
		xboxMapping.mapControl(Control.LEFT, 7);
		xboxMapping.mapControl(Control.RIGHT, 8);
		xboxMapping.mapControl(Control.FIRE, 16);
 		return xboxMapping;
 	}
 
 	public static InputMapping KeyboardArrowsMapping() {
 		InputMapping keyboardMapping = new InputMapping();
 		keyboardMapping.mapControl(Control.DOWN, Keys.DOWN);
 		keyboardMapping.mapControl(Control.UP, Keys.UP);
 		keyboardMapping.mapControl(Control.LEFT, Keys.LEFT);
 		keyboardMapping.mapControl(Control.RIGHT, Keys.RIGHT);
 		keyboardMapping.mapControl(Control.FIRE, Keys.SPACE);
 		return keyboardMapping;
 	}
 
 	public static InputMapping KeyboardWASDMapping() {
 		InputMapping keyboardMapping = new InputMapping();
 		keyboardMapping.mapControl(Control.DOWN, Keys.S);
 		keyboardMapping.mapControl(Control.UP, Keys.W);
 		keyboardMapping.mapControl(Control.LEFT, Keys.A);
 		keyboardMapping.mapControl(Control.RIGHT, Keys.D);
 		keyboardMapping.mapControl(Control.FIRE, Keys.Y);
 		return keyboardMapping;
 	}
 }
