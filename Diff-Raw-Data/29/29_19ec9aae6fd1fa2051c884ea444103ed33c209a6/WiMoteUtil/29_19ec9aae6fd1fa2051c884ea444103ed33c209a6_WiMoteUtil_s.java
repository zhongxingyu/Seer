 package com.because.wimote;
 
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.because.wimote.R;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.preference.PreferenceManager;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ToggleButton;
 
 public class WiMoteUtil {
 	private boolean bGaming = false;
 	private String hostname;
 	private int pin;
 	private int port;
 
 	private Activity activity;
 	private ArrayList<String> depressedKeyStreams = new ArrayList<String>();
 	private HashMap<Integer, Point> pointers = new HashMap<Integer, Point>();
 
 	private float Accel_Sensitivity_Max = 10f;
 	private float AccelMaximum = 10f;
 	private float DetectionThreshold = 2f;
 	private int MouseSensitivityPercent = 50;
 
 	public static final int[] ACCEL_KEY_IDS = {R.id.LeftClick, R.id.RightClick};
 	public static final int[] KEY_IDS = {R.id.A, R.id.Apostrophe, R.id.B, R.id.Backslash, R.id.Backspace, R.id.C, R.id.CapsLock, R.id.Comma, R.id.D, R.id.Dash, R.id.Down, R.id.E, R.id.Eight, R.id.Enter, R.id.Equals, R.id.Esc, R.id.F, R.id.Five, R.id.Four, R.id.G, R.id.Grave, R.id.H, R.id.I, R.id.J, R.id.K, R.id.L, R.id.Left, R.id.LeftClick, R.id.LeftSquareBracket, R.id.M, R.id.N, R.id.Nine, R.id.O, R.id.One, R.id.P, R.id.Period, R.id.Q, R.id.R, R.id.Right, R.id.RightClick, R.id.RightSquareBracket, R.id.S, R.id.Semicolon, R.id.Seven, R.id.Six, R.id.Slash, R.id.Space, R.id.T, R.id.Tab, R.id.Three, R.id.Two, R.id.U, R.id.Up, R.id.V, R.id.W, R.id.X, R.id.Y, R.id.Z, R.id.Zero};
 	public static final String[] KEY_NAMES = {"a", "'", "b", "\\", "BACKSPACE", "c", "CAPS", ",", "d", "-", "DOWN", "e", "8", "ENTER", "=", "ESC", "f", "5", "4", "g", "`", "h", "i", "j", "k", "l", "LEFT", "MOUSE_LEFT_CLICK", "[", "m", "n", "9", "o", "1", "p", ".", "q", "r", "RIGHT", "MOUSE_RIGHT_CLICK", "]", "s", ";", "7", "6", "/", "SPACE", "t", "TAB", "3", "2", "u", "UP", "v", "w", "x", "y", "z", "0"};
 	public static final int[] GAMING_KEY_IDS = {R.id.Tab, R.id.W, R.id.A, R.id.S, R.id.D, R.id.E, R.id.R, R.id.F, R.id.G, R.id.Z, R.id.LeftClick, R.id.RightClick, R.id.Space, R.id.Esc, R.id.Left, R.id.Up, R.id.Down, R.id.Right, R.id.Enter, R.id.Alt, R.id.Ctrl};
 	public static final String[] GAMING_KEY_NAMES = {"TAB", "w", "a", "s", "d", "e", "r", "f", "g", "z", "MOUSE_LEFT_CLICK", "MOUSE_RIGHT_CLICK", "SPACE", "ESC", "LEFT", "UP", "DOWN", "RIGHT", "ENTER", "ALT", "CTRL"};
 	public static final int[] MODIFIER_KEY_IDS = {R.id.Alt, R.id.Ctrl, R.id.Shift, R.id.Win};
 	public static final String[] MODIFIER_KEY_NAMES = {"ALT", "CTRL", "SHIFT", "WIN"};
 
 	public static final String KEY_DOWN = "KEY_DOWN";
 	public static final String KEY_UP = "KEY_UP";
 
 	private OnClickListener modifierClickListener = new OnClickListener() {
 		public void onClick(View v) {
 			if (!bGaming && !((ToggleButton)activity.findViewById(R.id.ToggleModifiers)).isChecked()) {
 				((ToggleButton)activity.findViewById(R.id.Shift)).setChecked(false);
 				((ToggleButton)activity.findViewById(R.id.Ctrl)).setChecked(false);
 				((ToggleButton)activity.findViewById(R.id.Win)).setChecked(false);
 				((ToggleButton)activity.findViewById(R.id.Alt)).setChecked(false);
 			}
 		}
 	};
 
 	public String getKeyName(int keyId) {
 		int[] key_ids = bGaming ? GAMING_KEY_IDS : KEY_IDS;
 		String[] key_names = bGaming ? GAMING_KEY_NAMES : KEY_NAMES;
 		for (int i = 0; i < key_ids.length; i++)
 			if (key_ids[i] == keyId)
 				return key_names[i];
 		return "";
 	}
 
 	public String getModifierKeyName(int modifierKeyId) {
 		if (!bGaming) {
 			for (int i = 0; i < MODIFIER_KEY_IDS.length; i++)
 				if (MODIFIER_KEY_IDS[i] == modifierKeyId)
 					return MODIFIER_KEY_NAMES[i];
 		}
 		return "";
 	}
 
 	public void setKeyboard() {
 		activity.setContentView(R.layout.keyboard);
 		((ToggleButton)activity.findViewById(R.id.Shift)).setOnClickListener(modifierClickListener);
 		((ToggleButton)activity.findViewById(R.id.Ctrl)).setOnClickListener(modifierClickListener);
 		((ToggleButton)activity.findViewById(R.id.Win)).setOnClickListener(modifierClickListener);
 		((ToggleButton)activity.findViewById(R.id.Alt)).setOnClickListener(modifierClickListener);
 		((ToggleButton)activity.findViewById(R.id.ToggleModifiers)).setOnClickListener(modifierClickListener);
 		((ToggleButton)activity.findViewById(R.id.ToggleModifiers)).setChecked(true);
 	}
 
 	public boolean toggleGaming() {
 		bGaming = !bGaming;
 
 		if (bGaming) {
 			((ToggleButton)activity.findViewById(R.id.Shift)).setOnClickListener(null);
 			((ToggleButton)activity.findViewById(R.id.Ctrl)).setOnClickListener(null);
 			((ToggleButton)activity.findViewById(R.id.Win)).setOnClickListener(null);
 			((ToggleButton)activity.findViewById(R.id.Alt)).setOnClickListener(null);
 			((ToggleButton)activity.findViewById(R.id.ToggleModifiers)).setOnClickListener(null);
 			((ToggleButton)activity.findViewById(R.id.ToggleModifiers)).setChecked(false);
 			activity.setContentView(R.layout.keyboard_gaming);
 		}
 		else
 			setKeyboard();
 		return bGaming;
 	}
 
 	public Rect getViewRect(View view) {
 		Rect rectParent = new Rect();
 		if (bGaming)
 			((KeyboardGamingLinearLayout)activity.findViewById(R.id.Parent)).getGlobalVisibleRect(rectParent);
 		else
 			((KeyboardLinearLayout)activity.findViewById(R.id.Parent)).getGlobalVisibleRect(rectParent);
 
 		Rect rect = new Rect();
 		view.getGlobalVisibleRect(rect);
 		rect.offset(-rectParent.left, -rectParent.top);
 
 		return rect;
 	}
 
 	public void processButtons(MotionEvent event, boolean bPressed) {
 		int pointerIndex = event.getActionIndex();
 		int pointerId = event.getPointerId(pointerIndex);
 		Point point = bPressed ? (new Point((int)event.getX(pointerIndex), (int)event.getY(pointerIndex))) : pointers.get(pointerId);
 		if (point == null)
 			return;
 
 		String keyAction = bPressed ? KEY_DOWN : KEY_UP;
 
 		if (!bGaming && !((ToggleButton)activity.findViewById(R.id.ToggleModifiers)).isChecked()) {
 			// Allow multitouch of modifier keys only when they are not acting
 			// as toggle buttons.
 			for (int i = 0; i < MODIFIER_KEY_IDS.length; i++) {
 				if (getViewRect(activity.findViewById(MODIFIER_KEY_IDS[i])).contains(point.x, point.y)) {
 					activity.findViewById(MODIFIER_KEY_IDS[i]).setPressed(bPressed);
 					processKeys(keyAction, getModifierKeyName(MODIFIER_KEY_IDS[i]), true);
 					if (bPressed)
 						pointers.put(event.getPointerId(pointerIndex), point);
 					else
 						pointers.remove(pointerId);
 				}
 			}
 		}
 
 		int[] key_ids = bGaming ? GAMING_KEY_IDS : KEY_IDS;
 
 		for (int i = 0; i < key_ids.length; i++) {
 			if (getViewRect(activity.findViewById(key_ids[i])).contains(point.x, point.y)) {
 				activity.findViewById(key_ids[i]).setPressed(bPressed);
 				processKeys(keyAction, getKeyName(key_ids[i]), true);
 				if (bPressed)
 					pointers.put(event.getPointerId(pointerIndex), point);
 				else
 					pointers.remove(pointerId);
 			}
 		}
 	}
 
 	public void sendString(String s) {
 		try {
 			String string = Integer.toString(pin) + " " + s;
 			DatagramSocket socket = new DatagramSocket();
 			byte[] data = string.getBytes();
 			DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(hostname), port);
 			socket.send(packet);
 			socket.close();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public WiMoteUtil(Activity a) {
 		activity = a;
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
 		hostname = settings.getString("hostname", "192.168.1.101");
 		pin = Integer.parseInt(settings.getString("pinnumber", "1337"));
 		port = Integer.parseInt(settings.getString("portnumber", "27015"));	
 		DetectionThreshold = Float.parseFloat(settings.getString("aThreshold", "2f"));
 		MouseSensitivityPercent = settings.getInt("aMouseSensitivity", 50);
 	}
 
 	public WiMoteUtil(Activity a, boolean b) {
 		this(a);
 		bGaming = b;
 	}
 
 	public void processKeys(String szKeyAction, String szKeyName, boolean isKeyboard) {
 		if (szKeyAction.isEmpty() || szKeyName.isEmpty())
 			return;
 
 		if (szKeyName.length() == 1)
 			if (szKeyName.charAt(0) < 32 || szKeyName.charAt(0) > 126)
 				return;
 
 		// By default, modifier keys (e.g. SHIFT, CTRL) act as toggle buttons.
 		// This behavior can be disabled by turning off the "Mod" button. In
 		// that case, modifier keys can be pressed individually just like any
 		// other key, which is useful in certain applications such as games.
 		String szModifierKeys = new String();
 		if (isKeyboard && !bGaming) {
 			if (((ToggleButton)activity.findViewById(R.id.ToggleModifiers)).isChecked()) {
 				if (((ToggleButton)activity.findViewById(R.id.Shift)).isChecked())
 					szModifierKeys += (getModifierKeyName(R.id.Shift) + " ");
 				if (((ToggleButton)activity.findViewById(R.id.Ctrl)).isChecked())
 					szModifierKeys += (getModifierKeyName(R.id.Ctrl) + " ");
 				if (((ToggleButton)activity.findViewById(R.id.Win)).isChecked())
 					szModifierKeys += (getModifierKeyName(R.id.Win) + " ");
 				if (((ToggleButton)activity.findViewById(R.id.Alt)).isChecked())
 					szModifierKeys += (getModifierKeyName(R.id.Alt) + " ");
 			}
 		}
 
 		String szKeyStream = szModifierKeys + szKeyName;
 
 		if (szKeyStream.isEmpty())
 			return;
 
 		if (szKeyAction.equals(KEY_DOWN)) {
 			// We are asked to depress specific key(s) that is/are NOT
 			// currently depressed.
 			if (!depressedKeyStreams.contains(szKeyStream)) {
 				sendString(KEY_DOWN + " " + szKeyStream);
 				depressedKeyStreams.add(szKeyStream);
 			}
 		}
 		else if (szKeyAction.equals(KEY_UP)) {
 			// We are asked to release specific key(s) that is/are currently
 			// depressed.
 			if (depressedKeyStreams.contains(szKeyStream)) {
 				sendString(KEY_UP + " " + szKeyStream);
 				depressedKeyStreams.remove(szKeyStream);
 			}
 		}
 	}
 
 	public float[] processAccel(float x, float y, float z) {
 		float PercentX = 0f, PercentY = 0f, PercentZ = 0f;
 
 		//under normal operation of the accelerometer the tilting of the phone to a 90* angle will
 		//correspond with values in the range of 0->10, however if the phone is shaken or moved quickly
 		//the value may exceed 10, so the .min function will limit the value to 10, this is then scaled
 		//up to 30, representing 30% of the screen traveled in a unit of time (TBD)
 
 		//TODO: set once in beginning.
 		float PerS = Accel_Sensitivity_Max * (float)MouseSensitivityPercent / 100f;
 
 		//TODO: can we reduce multiplications
 		//Scale and limit the Accelerometer data to a range between 0->30 (representing percent value for mouse movement)
 		PercentX = Math.min(x, AccelMaximum) * PerS;
 		PercentY = Math.min(y, AccelMaximum) * PerS;
 		//PercentZ = Math.min(z, AccelMaximum)*Accel_Sensitivity_Max;
 
 		PercentX = Math.max(PercentX, -AccelMaximum) * PerS;
 		PercentY = Math.max(PercentY, -AccelMaximum) * PerS;
 		//PercentZ = -Math.max(z, -AccelMaximum)*Accel_Sensitivity_Max;
 
 		//to ignore noise while holding the phone still.
 		if (Math.abs(PercentX) < DetectionThreshold)
 			PercentX = 0;
 		else // the threshold is subtracted (or added) to the Percent value to scale it back for the threshold
 			PercentX = PercentX - DetectionThreshold * (PercentX / Math.abs(PercentX));
 
 		if (Math.abs(PercentY) < DetectionThreshold)
 			PercentY = 0;
 		else
 			PercentY = PercentY - DetectionThreshold * (PercentY / Math.abs(PercentY));
 
 		if (Math.abs(PercentZ) < DetectionThreshold)
 			PercentZ = 0;
 		else
 			PercentZ = PercentZ - DetectionThreshold * (PercentZ / Math.abs(PercentZ));
 
 		return (new float[] {-PercentX, PercentY});
 	}
 }
