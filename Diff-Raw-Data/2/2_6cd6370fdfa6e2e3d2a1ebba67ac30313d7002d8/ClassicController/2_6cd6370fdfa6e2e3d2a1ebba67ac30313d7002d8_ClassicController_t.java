 package org.cvpcs.android.cwiidconfig.config;
 
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 public class ClassicController extends Device {
 	public static final String NAME = "Classic";
 	private static String PLUGIN_STICK2BTN = "Plugin.classic_stick2btn";
 	private static final Pattern PATTERN = Pattern.compile(
			"((classic|plugin.classic_stick2btn)\\.[a-z0-9_]+)[ \t]*=[ \t]*([a-z0-9_]+)",
 			Pattern.CASE_INSENSITIVE);
 
 	public static final ArrayList<String> BUTTONS = new ArrayList<String>();
 	private static final ArrayList<String> CONFIG_BUTTONS = new ArrayList<String>();
 	
 	public static final String BUTTON_UP 		= "Up";
 	public static final String BUTTON_LEFT		= "Left";
 	public static final String BUTTON_RIGHT		= "Right";
 	public static final String BUTTON_DOWN		= "Down";
 	public static final String BUTTON_A			= "A";
 	public static final String BUTTON_B			= "B";
 	public static final String BUTTON_X			= "X";
 	public static final String BUTTON_Y			= "Y";
 	public static final String BUTTON_MINUS		= "Minus";
 	public static final String BUTTON_HOME		= "Home";
 	public static final String BUTTON_PLUS		= "Plus";
 	public static final String BUTTON_L			= "L";
 	public static final String BUTTON_R			= "R";
 	public static final String BUTTON_ZL		= "ZL";
 	public static final String BUTTON_ZR		= "ZR";
 	public static final String BUTTON_LS_UP		= "Left Stick, Up";
 	public static final String BUTTON_LS_LEFT	= "Left Stick, Left";
 	public static final String BUTTON_LS_RIGHT	= "Left Stick, Right";
 	public static final String BUTTON_LS_DOWN	= "Left Stick, Down";
 	public static final String BUTTON_RS_UP		= "Right Stick, Up";
 	public static final String BUTTON_RS_LEFT	= "Right Stick, Left";
 	public static final String BUTTON_RS_RIGHT	= "Right Stick, Right";
 	public static final String BUTTON_RS_DOWN	= "Right Stick, Down";
 	
 	static {
 		BUTTONS.add(BUTTON_UP);
 		BUTTONS.add(BUTTON_LEFT);
 		BUTTONS.add(BUTTON_RIGHT);
 		BUTTONS.add(BUTTON_DOWN);
 		BUTTONS.add(BUTTON_A);
 		BUTTONS.add(BUTTON_B);
 		BUTTONS.add(BUTTON_X);
 		BUTTONS.add(BUTTON_Y);
 		BUTTONS.add(BUTTON_MINUS);
 		BUTTONS.add(BUTTON_HOME);
 		BUTTONS.add(BUTTON_PLUS);
 		BUTTONS.add(BUTTON_L);
 		BUTTONS.add(BUTTON_R);
 		BUTTONS.add(BUTTON_ZL);
 		BUTTONS.add(BUTTON_ZR);
 		BUTTONS.add(BUTTON_LS_UP);
 		BUTTONS.add(BUTTON_LS_LEFT);
 		BUTTONS.add(BUTTON_LS_RIGHT);
 		BUTTONS.add(BUTTON_LS_DOWN);
 		BUTTONS.add(BUTTON_RS_UP);
 		BUTTONS.add(BUTTON_RS_LEFT);
 		BUTTONS.add(BUTTON_RS_RIGHT);
 		BUTTONS.add(BUTTON_RS_DOWN);
 
 		CONFIG_BUTTONS.add(NAME + "." + BUTTON_UP);
 		CONFIG_BUTTONS.add(NAME + "." + BUTTON_LEFT);
 		CONFIG_BUTTONS.add(NAME + "." + BUTTON_RIGHT);
 		CONFIG_BUTTONS.add(NAME + "." + BUTTON_DOWN);
 		CONFIG_BUTTONS.add(NAME + "." + BUTTON_A);
 		CONFIG_BUTTONS.add(NAME + "." + BUTTON_B);
 		CONFIG_BUTTONS.add(NAME + "." + BUTTON_X);
 		CONFIG_BUTTONS.add(NAME + "." + BUTTON_Y);
 		CONFIG_BUTTONS.add(NAME + "." + BUTTON_MINUS);
 		CONFIG_BUTTONS.add(NAME + "." + BUTTON_HOME);
 		CONFIG_BUTTONS.add(NAME + "." + BUTTON_PLUS);
 		CONFIG_BUTTONS.add(NAME + "." + BUTTON_L);
 		CONFIG_BUTTONS.add(NAME + "." + BUTTON_R);
 		CONFIG_BUTTONS.add(NAME + "." + BUTTON_ZL);
 		CONFIG_BUTTONS.add(NAME + "." + BUTTON_ZR);
 		CONFIG_BUTTONS.add(PLUGIN_STICK2BTN + ".LStick_Up");
 		CONFIG_BUTTONS.add(PLUGIN_STICK2BTN + ".LStick_Left");
 		CONFIG_BUTTONS.add(PLUGIN_STICK2BTN + ".LStick_Right");
 		CONFIG_BUTTONS.add(PLUGIN_STICK2BTN + ".LStick_Down");
 		CONFIG_BUTTONS.add(PLUGIN_STICK2BTN + ".RStick_Up");
 		CONFIG_BUTTONS.add(PLUGIN_STICK2BTN + ".RStick_Left");
 		CONFIG_BUTTONS.add(PLUGIN_STICK2BTN + ".RStick_Right");
 		CONFIG_BUTTONS.add(PLUGIN_STICK2BTN + ".RStick_Down");
 	}
 	
 	public ClassicController() {
 		super();
 		mName = NAME;
 		mButtons = BUTTONS;
 		mConfigButtons = CONFIG_BUTTONS;
 		mPattern = PATTERN;
 	}
 }
