 package net.brtly.monkeyboard;
 
 import javax.swing.AbstractAction;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JButton;
 import javax.swing.DefaultListModel;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JOptionPane;
 import javax.swing.JSeparator;
 import javax.swing.KeyStroke;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.ImageIcon;
 import javax.swing.SwingWorker;
 import javax.swing.Timer;
 
 import com.android.chimpchat.ChimpChat;
 import com.android.chimpchat.core.IChimpDevice;
 import com.android.chimpchat.core.IChimpImage;
 import com.android.chimpchat.core.TouchPressType;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.border.BevelBorder;
 import javax.swing.text.Document;
 import javax.swing.text.SimpleAttributeSet;
 
 import java.awt.Cursor;
 import java.awt.Event;
 import java.awt.FileDialog;
 import java.awt.Rectangle;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JTextPane;
 import java.awt.Dimension;
 import java.awt.Component;
 import javax.swing.SwingConstants;
 import java.awt.Font;
 import javax.swing.JScrollPane;
 import java.awt.event.InputEvent;
 import java.awt.Color;
 
 public class MonkeyBoard {
 	private DefaultListModel listModel = new DefaultListModel();
 	private JList listView = null;
 	private JButton btnMonkeyBoard = null;
 	private JTextPane textConsole = null;
 	private ArrayList<JMenuItem> deviceMenuItems = new ArrayList<JMenuItem>(); //stores a reference to all menu items that need to be
 		//disabled when there isn't a device connected.
 	private Timer tmrRefresh = null;
 	
 	JFrame frmMonkeyboard;
 
     private ChimpChat mChimpChat;
     private IChimpDevice mDevice; 
     private String connectedDeviceId = null;
     private String desktopPath;
     private static final String ANDROID_SDK = "/Users/obartley/Library/android-sdk-macosx/";
     private static final String ADB = ANDROID_SDK + "platform-tools/adb";
     private static final String EMULATOR = ANDROID_SDK + "tools/emulator";
     private static final long TIMEOUT = 5000;
     private static final int REFRESH_DELAY = 1000;
     // ddms default filename = "device-2011-12-23-160423.png"
     public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd-HHmmss";
     
     
     
     // lookup table to translate from Java keycodes to Android
     private Map<Integer, String> keyCodeMap = new TreeMap<Integer, String>();
     
     // Set to track which android keycodes are currently in a down state,
     // so that they can be quickly matched with a keyup in the event of a focus lost event
     private Set<String> keysPressed = new HashSet<String>();
     
 	/**
 	 * Create the application.
 	 */
 	public MonkeyBoard() {
 		// warmup!
 		initialize(); // this method is only for GUI elements manipulated on Design tab
 		initializeKeyCodeMap();
 		refreshDeviceList();
 		
 		// create the adb backend
 		TreeMap<String, String> options = new TreeMap<String, String>();
         options.put("backend", "adb");
         options.put("adbLocation", ADB);
 		mChimpChat = ChimpChat.getInstance(options);
 		desktopPath = System.getProperty("user.home") + "/Desktop";
 		
 		// create the timer that refreshes the device list
 		@SuppressWarnings("serial")
 		AbstractAction timerAction = new AbstractAction() {
 		    public void actionPerformed(ActionEvent e) {
 		    	refreshDeviceList();
 		    }
 		};
 		tmrRefresh = new Timer(REFRESH_DELAY, timerAction);
 		tmrRefresh.start();
 	}
 	
 	/**
 	 *  Append a String to the text in the console and force scrolling to the end of the doc
 	 *  basically a Log
 	 */
 	public void toConsole(String arg0) {
 		try {
 			// get document from console and append arg0
 			Document d = textConsole.getDocument();
 			SimpleAttributeSet attributes = new SimpleAttributeSet();
 			d.insertString(d.getLength(), '\n' + arg0, attributes);
 			// force scrolling to end of output
 			textConsole.scrollRectToVisible(new Rectangle(0, textConsole.getHeight()-2, 1, 1));
 		} catch (Exception e) {
 			System.err.println("Error instering:" + arg0);
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Capture output from `adb devices`, parses it and returns data in a Map in the form of
 	 * deviceId:key::status:value
 	 * @return
 	 */
 	private Map<String, String> getAdbStatus() {
 		Map<String, String> rv = new HashMap<String, String>();
 		// Capture output from `adb devices` and map connected deviceIds to their status
 		// TODO: remove absolute path to adb or make dynamic
 		String cmd = ADB + " devices";
 		Runtime run = Runtime.getRuntime();
 		Process pr = null;
 		
 		// execute cmd
 		try {
 			pr = run.exec(cmd);
 			pr.waitFor();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		// parse output
 		BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
 		String line = "";
 		try {
 			while ((line=buf.readLine())!=null) {
 				if ( ! (line.startsWith("List") || line.length() <= 1) ) {
 					String[] s = line.split("\\s+"); //it's a tab separated list, dude!
 					rv.put(s[0], s[1]); // add deviceId, status to Map
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} 
 		return rv;
 	}	
 
 	/**
 	 * execute an adb command via
 	 * adb -s connectedDeviceId cmd
 	 * useful for uninstalling packages, etc
 	 */
 	private void execAdbCommand(final String args) {
 		// threaded subprocess
 		SwingWorker <Object, Void> worker = new SwingWorker<Object, Void>() {
 		    @Override
 		    public Object doInBackground() {
 				Runtime run = Runtime.getRuntime();
 				Process pr = null;
 				
 				if (args == null) return null;
 				if (connectedDeviceId == null) return null;
 				
 				String cmd = ADB + " -s " + connectedDeviceId + " " + args;
 				toConsole(">>> " + cmd);
 				
 				// execute cmd
 				try {
 					pr = run.exec(cmd);
 					pr.waitFor();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
 				// parse output
 				BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
 				String line = "";
 				try {
 					while ((line=buf.readLine())!=null) {
 						toConsole(".   " + line);
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				return null;
 		    }
 		};
 		worker.execute();
 	}
 	/**
 	 * the only reason this one is used exclusively for install is because
 	 * the default process runner used in execAdbCommand treats whitespace as delimiting arguments, regardless of
 	 * escape chars or quotes. Because some filepaths use spaces O_o, we need to pass the args as an array instead
 	 * @param apkPath
 	 */
 	private void execAdbInstallCommand(final String apkPath) {
 		SwingWorker <Object, Void> worker = new SwingWorker<Object, Void>() {
 		    @Override
 		    public Object doInBackground() {
 				Runtime run = Runtime.getRuntime();
 				Process pr = null;
 				
 				if (apkPath == null) return null;
 				if (connectedDeviceId == null) return null;
 				
 				String[] cmd = {ADB, "-s", connectedDeviceId, "install", apkPath};
 		
 				toConsole(">>> " + ADB + " -s " + connectedDeviceId + " install " + '"' + apkPath + '"');
 				
 				// execute cmd
 				try {
 					pr = run.exec(cmd);
 					pr.waitFor();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
 				// parse output
 				BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
 				String line = "";
 				try {
 					while ((line=buf.readLine())!=null) {
 						toConsole(".   " + line);
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				return null;
 		    }
 		};
 		worker.execute();
 	}
 
 	/**
 	 * Given the name of an AVD, launch an emulator instance of it in a worker thread
 	 * @param name
 	 */
 	private void startAvd(final String name) {
 		SwingWorker <Object, Void> worker = new SwingWorker<Object, Void>() {
 		    @Override
 		    public Object doInBackground() {
 				Runtime run = Runtime.getRuntime();
 				Process pr = null;
 				
 				String cmd = EMULATOR + " -avd " + name;
 
 				toConsole(">>> " + cmd);
 				
 				// execute cmd
 				try {
 					pr = run.exec(cmd);
 					pr.waitFor();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				return null;
 		    }
 		};
 		// now run the thread
 		worker.execute();
 	}
 	
 	
 	/**
 	 * refreshes data in listModel to reflect the current status of devices connected to adb
 	 */
 	private void refreshDeviceList() {
 		Map<String, String> adb = getAdbStatus();
 		Iterator<Entry<String, String>> adbDevices = adb.entrySet().iterator();
 		Boolean foundConnectedDevice = false;
 		String selectedElement = null;
 		int selectedIndex = -1;
 		int i = 0;
 		
 		// save a reference to the currently selected item
 		// so we can reselect it after the list is rebuilt
 		try {
 			selectedElement = listModel.getElementAt(listView.getSelectedIndex()).toString();
 		} catch (Exception e) {
 			selectedElement = "";
 		}
 		
 		// iterate over the items in adb
 		listModel.clear();
 		while (adbDevices.hasNext()) {
 			Entry<String, String> dev =  (Entry<String, String>) adbDevices.next();
 
 			String devId = dev.getKey().trim();
 			String devStatus = dev.getValue().trim();
 			Map <String, String> elem = new HashMap<String, String>();
 
 			// build list element
 			if ( devId.equals(connectedDeviceId) ) {
 				// some special treatment for the device matching connectedDeviceId
 				devStatus = "connected";
 				foundConnectedDevice = true;
 			}
 			elem.put("deviceId", devId);
 			elem.put("deviceStatus", devStatus);
 			listModel.addElement(elem);
 			
 			if (selectedElement.contains(devId))
 				// we found a match to the previously selected device
 				// save an index to it
 				selectedIndex = i;
 			i++;
 		}
 		
 		if ( ! foundConnectedDevice) {
 			// a deviceId matching connectedDevcieId was not found, reset connection
 			disconnectFromDevice();
 		}
 		
 		// if a match was found in the above loop, then this will be true
 		if (selectedIndex > -1)
 			listView.setSelectedIndex(selectedIndex);
 		
 		// now we scrub the list for stuff that isn't in the deviceList anymore
 		if ( listModel.getSize() > 0) {
 			for (i = 0; i == listModel.getSize(); i++) {
 				if (adb.keySet().contains(listModel.getElementAt(i)))
 					listModel.remove(i);
 			}
 		}
 	}
 	
 	/**
 	 * define a background thread to connect to the device selected in the list
 	 */
 	@SuppressWarnings("unchecked")
 	private void connectToDevice() {
 		// define the worker
 		SwingWorker <Object, Void> worker = new SwingWorker<Object, Void>() {
 		    @Override
 		    public Object doInBackground() {
 			HashMap<String, String> v;
 			String deviceId = null;
 			
 			// get the device id from the selected list element
 			try {
 				v = (HashMap<String, String>) listView.getSelectedValue();	
 				deviceId = v.get("deviceId");
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				disconnectFromDevice();
 				refreshDeviceList();
 				return null;
 			}
 	
 			// if the device is already selected, disconnect instead
 			if (deviceId.equals(connectedDeviceId)) {
 				disconnectFromDevice();
 				refreshDeviceList();
 				return null;
 			}
 	
 			//get a connection to the device
 			try {
 		        mDevice = mChimpChat.waitForConnection(TIMEOUT, deviceId);
 		        if ( mDevice == null ) throw new RuntimeException("Couldn't connect.");
 		        mDevice.wake();
 		        connectedDeviceId = deviceId;
 		        toConsole("connected to device: " + deviceId);
 		        setDeviceMenuItemsEnabled(true);
 			} catch (Exception e) {
 				e.printStackTrace();
 				disconnectFromDevice();
 	        	toConsole("couldn't connect to device: " + deviceId);    
 			}
 			refreshDeviceList();
 		  	return null;
 		    }
 		};
 		// now run the thread
 		worker.execute();
 	}
 	
 	/**
 	 * handles disposing of connection object and disabling menu items that require a connected device
 	 */
 	private void disconnectFromDevice() {
 		connectedDeviceId = null;
 		if (mDevice != null) {
 			mDevice.dispose();
 			mDevice = null;
 		}
 		setDeviceMenuItemsEnabled(false);
 	}
 	
 	/**
 	 * iterate over items in deviceMenuItems and call .setEnabled(b)
 	 * this is used when connecting/disconnecting to a device
 	 * @param b
 	 */
 	private void setDeviceMenuItemsEnabled(Boolean b) {
 		Iterator<JMenuItem> iter = deviceMenuItems.iterator();		
 		while (iter.hasNext()) {
 			iter.next().setEnabled(b);
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	private void getDeviceProperties() {
 		String[] props = {"build.board",
 		     	"build.brand",
 		     	"build.device",
 		     	"build.fingerprint",
 		     	"build.host",
 		     	"build.ID",
 		     	"build.model",
 		     	"build.product",
 		     	"build.tags",
 		     	"build.type",
 		     	"build.user",
 		     	"build.CPU_ABI",
 		     	"build.manufacturer",
 		     	"build.version.incremental",
 		     	"build.version.release",
 		     	"build.version.codename",
 		     	"display.width",
 		     	"display.height",
 		     	"display.density"};
 		toConsole("Device properties for " + connectedDeviceId);
 		try {
 			for (int i = 0; i < props.length; i++ ) {
 				toConsole(String.format("%1$-" + 30 + "s", props[i]) + mDevice.getProperty(props[i]));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Handler for all screenshot functions. If a filename is passed, a screenshot will be saved there
 	 * if "" or null is passed, the na default filename will be generated and the file will be saved to the user's desktop
 	 * @param filename
 	 */
 	private void screenshotHandler(String filename) {
 		// if there was no filename passed, give it a default and save to desktop
 		if ((filename == "") || (filename == null)) {
 			// ddms default filename = "device-2011-12-23-160423.png"
 			Calendar cal = Calendar.getInstance();
 		    SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMAT);
		    filename = desktopPath + "/device-" + sdf.format(cal.getTime() + ".png");	    			
 		}
 		// now do the saving...
 		try {
 			toConsole("Saving snapshot to " + filename);
 			IChimpImage img = mDevice.takeSnapshot();
 			img.writeToFile(filename, "png");
 		} catch (Exception e) {
 			toConsole("there was an error saving " + filename);
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * Handles keyPress and keyRelease events to be sent to connected device
 	 * 
 	 * @param keyCode
 	 * @param modifiers
 	 * @param type
 	 */
 	private void keyEventHandler(int keyCode, int modifiers, TouchPressType type) {
 		String code = null;
 		String stype = (type == TouchPressType.DOWN)?"PRESS":"RELEASE";
 		//Boolean isShift = ((modifiers & 0x01) == 1);
 		Boolean isCtrl = ((modifiers & 0x02) == 2);
 		Boolean isMeta = ((modifiers & 0x04) == 4);
 		//Boolean isAlt = ((modifiers & 0x08) == 8);
 		
 		
 		// ignore all meta + keydown (Such as Command + S)
 		if (isMeta && (type == TouchPressType.DOWN)) return;		
 		
 		// manually map some special ctrl+keyevents
 		// TODO: make this not so brittle. incorporate this into a keymap?
 		switch (keyCode) {
 			case KeyEvent.VK_ENTER:
 				// if the special mapping is already pressed, it's a keyup
 				// the reason we don't care about isCtrl is it's possible the user
 				// can release ctrl before the key in question, and then a release event will never be sent
 				if ((isCtrl && (type == TouchPressType.DOWN)) || 
 						(keysPressed.contains("KEYCODE_DPAD_CENTER")))
 					code = "KEYCODE_DPAD_CENTER"; 
 				break;
 			
 			// emulator parity
 			case KeyEvent.VK_F3:
 				if ((isCtrl && (type == TouchPressType.DOWN)) || 
 						(keysPressed.contains("KEYCODE_CAMERA")))
 					code = "KEYCODE_CAMERA"; 
 				break;				
 			case KeyEvent.VK_F5:
 				if ((isCtrl && (type == TouchPressType.DOWN)) || 
 						(keysPressed.contains("KEYCODE_VOLUME_UP")))
 					code = "KEYCODE_VOLUME_UP"; 
 				break;
 			case KeyEvent.VK_F6:
 				if ((isCtrl && (type == TouchPressType.DOWN)) || 
 						(keysPressed.contains("KEYCODE_VOLUME_DOWN")))
 					code = "KEYCODE_VOLUME_DOWN"; 
 				break;
 			
 			// these ones make more sense than the emulator defaults
 			case KeyEvent.VK_M:
 				if ((isCtrl && (type == TouchPressType.DOWN)) || 
 						(keysPressed.contains("KEYCODE_MENU")))
 					code = "KEYCODE_MENU"; 
 				break;
 			case KeyEvent.VK_S:
 				if ((isCtrl && (type == TouchPressType.DOWN)) || 
 						(keysPressed.contains("KEYCODE_SEARCH")))
 					code = "KEYCODE_SEARCH"; 
 				break;
 			case KeyEvent.VK_H:
 				if ((isCtrl && (type == TouchPressType.DOWN)) || 
 						(keysPressed.contains("KEYCODE_HOME")))
 					code = "KEYCODE_HOME"; 
 				break;
 			case KeyEvent.VK_P:
 				if ((isCtrl && (type == TouchPressType.DOWN)) || 
 						(keysPressed.contains("KEYCODE_POWER")))
 					code = "KEYCODE_POWER"; 
 				break;
 			case KeyEvent.VK_C:
 				if ((isCtrl && (type == TouchPressType.DOWN)) || 
 						(keysPressed.contains("KEYCODE_CAMERA")))
 					code = "KEYCODE_CAMERA"; 
 				break;
 			case KeyEvent.VK_MINUS:
 				if ((isCtrl && (type == TouchPressType.DOWN)) || 
 						(keysPressed.contains("KEYCODE_VOLUME_DOWN")))
 					code = "KEYCODE_VOLUME_DOWN"; 
 				break;
 			case KeyEvent.VK_EQUALS:
 				if ((isCtrl && (type == TouchPressType.DOWN)) || 
 						(keysPressed.contains("KEYCODE_VOLUME_UP")))
 					code = "KEYCODE_VOLUME_UP"; 
 				break;
 		}
 		
 		// if code is still null, then do a regular lookup in the map
 		if ( code == null ) code = keyCodeMap.get(keyCode);
 		
 		// still null? nothing to do here
 		if ( code == null ) return;
 		
 		// if it's a keydown and there's already a reference in keysPressed, don't send another keydown 
 		if ((! code.contains("DPAD")) &&
 				(keysPressed.contains(code)) && // only allow spamming trackball commands
 				(type == TouchPressType.DOWN))
 			return;
 		
 		// now we focus on sending it to the device, log it
 		toConsole("[" + Integer.toString(keyCode) + ":" + Integer.toString(modifiers) + "] " + code + " " + stype);
 		
 		// make sure the state of the key is properly stored
 		switch(type) {
 		case DOWN:keysPressed.add(code); break;
 		case UP:keysPressed.remove(code); break;
 		}
 		
 		// actually send the key event if we're connected to a device
 		if (connectedDeviceId != null)  {
 			mDevice.press(code, type);
 		}
 	}
 
 	/**
 	 * iterate over items in keysPressed to return all keys to an unpressed state
 	 * this is useful as a deadfall switch to quickly return all keys issued a down command
 	 * a matching up command in the event of lost focus
 	 */
 	private void resetKeysPressed() {
 		Iterator<String> iter = keysPressed.iterator();
 		String code;
 	    while (iter.hasNext()) {
 	    	code = iter.next();
 	    	toConsole("[-:-] " + code + " RELEASE");
 			if (connectedDeviceId != null) {
 				mDevice.press(code, TouchPressType.UP);
 			} 
 	    }
 	    keysPressed.clear();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmMonkeyboard = new JFrame();
 		frmMonkeyboard.setMinimumSize(new Dimension(512, 360));
 		frmMonkeyboard.setTitle("MonkeyBoard");
 		frmMonkeyboard.setBounds(100, 100, 512, 360);
 		frmMonkeyboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		// moved JList declaration to class-level declarartions
 		listView = new JList(listModel);
 		listView.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				// double click to connect
 				if(e.getClickCount() == 2) {
 					int index = listView.locationToIndex(e.getPoint());
 					//Object item = listModel.getElementAt(index);;
 					listView.ensureIndexIsVisible(index);
 					connectToDevice();
 				}
 			}
 		});
 		listView.setAlignmentY(Component.TOP_ALIGNMENT);
 		listView.setAlignmentX(Component.LEFT_ALIGNMENT);
 		listView.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		listView.setCellRenderer(new DeviceListRenderer());
 		JButton btnRefresh = new JButton("Refresh");
 		btnRefresh.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				refreshDeviceList();
 			}
 		});
 		
 		JButton btnConnect = new JButton("Connect");
 		btnConnect.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				connectToDevice();
 			}
 		});		
 		btnMonkeyBoard = new JButton("");
 		btnMonkeyBoard.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				// if the button has focus, then clicking it will give focus to something else, like a toggle switch.
 				if ( btnMonkeyBoard.hasFocus() ) {
 					textConsole.requestFocus();
 				}
 			}
 		});
 		btnMonkeyBoard.setHorizontalTextPosition(SwingConstants.CENTER);
 		btnMonkeyBoard.setFocusTraversalKeysEnabled(false);
 		btnMonkeyBoard.setAlignmentX(Component.CENTER_ALIGNMENT);
 		btnMonkeyBoard.setBorderPainted(false);
 		btnMonkeyBoard.setIconTextGap(0);
 		btnMonkeyBoard.setPressedIcon(new ImageIcon(MonkeyBoard.class.getResource("/res/android_large_sel.png")));
 		btnMonkeyBoard.setSelectedIcon(new ImageIcon(MonkeyBoard.class.getResource("/res/android_large_sel.png")));
 		btnMonkeyBoard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		btnMonkeyBoard.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				keyEventHandler(e.getKeyCode(), e.getModifiers(), TouchPressType.DOWN);
 			}
 			@Override
 			public void keyReleased(KeyEvent e) {
 				keyEventHandler(e.getKeyCode(), e.getModifiers(), TouchPressType.UP);
 			}
 		});
 		btnMonkeyBoard.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent arg0) {
 				toConsole("key events released");
 				btnMonkeyBoard.setSelected(false);
 				resetKeysPressed();
 			}
 			@Override
 			public void focusGained(FocusEvent arg0) {
 				toConsole("key events trapped");
 				btnMonkeyBoard.setSelected(true);
 			}
 		});	
 		btnMonkeyBoard.setBorder(null);
 		btnMonkeyBoard.setIcon(new ImageIcon(MonkeyBoard.class.getResource("/res/android_large.png")));
 		
 		JScrollPane consoleScrollPane = new JScrollPane();
 
 		GroupLayout groupLayout = new GroupLayout(frmMonkeyboard.getContentPane());
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addComponent(consoleScrollPane, GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
 							.addContainerGap())
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addComponent(btnRefresh)
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(btnConnect)
 									.addGap(0, 0, Short.MAX_VALUE))
 								.addComponent(listView, GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE))
 							.addGap(3)
 							.addComponent(btnMonkeyBoard, GroupLayout.PREFERRED_SIZE, 246, Short.MAX_VALUE)
 							.addGap(2))))
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addComponent(listView, GroupLayout.PREFERRED_SIZE, 244, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(btnConnect)
 								.addComponent(btnRefresh)))
 						.addComponent(btnMonkeyBoard, GroupLayout.PREFERRED_SIZE, 279, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(consoleScrollPane, GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
 					.addContainerGap())
 		);
 		
 		textConsole = new JTextPane();
 		textConsole.setText("ready");
 		textConsole.setForeground(new Color(255, 255, 255));
 		textConsole.setFont(new Font("Monospaced", Font.PLAIN, 15));
 		textConsole.setEditable(false);
 		textConsole.setBackground(new Color(0, 0, 0));
 		consoleScrollPane.setViewportView(textConsole);
 		frmMonkeyboard.getContentPane().setLayout(groupLayout);
 		
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.setBorder(null);
 		frmMonkeyboard.setJMenuBar(menuBar);
 		
 		JMenu mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 		
 		JMenuItem mntmRestartAdbServer = new JMenuItem("Restart adb server");
 		mntmRestartAdbServer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.META_MASK));
 		mnFile.add(mntmRestartAdbServer);		
 		
 		JMenuItem mntmRefreshDeviceList = new JMenuItem("Refresh Device List");
 		mntmRefreshDeviceList.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.META_MASK));
 		mntmRefreshDeviceList.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			toConsole("refreshing device list...");
 			refreshDeviceList();
 			}
 		});
 		mnFile.add(mntmRefreshDeviceList);
 				
 		JMenuItem mntmConnectToDevice = new JMenuItem("Connect To Device");
 		mntmConnectToDevice.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				connectToDevice();
 			}
 		});
 		mntmConnectToDevice.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_MASK));
 		mnFile.add(mntmConnectToDevice);
 		
 		JSeparator separator = new JSeparator();
 		mnFile.add(separator);
 		
 		JMenuItem mntmLaunchEmulator = new JMenuItem("Launch Emulator...");
 		mntmLaunchEmulator.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Component source = (Component) arg0.getSource();
 				String avd = JOptionPane.showInputDialog(source,
 		                "Enter the name of the AVD you want to lauch:");
 				startAvd(avd);
 			}
 		});
 		mntmLaunchEmulator.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.META_MASK));
 		mnFile.add(mntmLaunchEmulator);
 		
 		mnFile.add(new JSeparator());
 		
 		JMenuItem mntmInstallapk = new JMenuItem("Install *.apk...");
 		mntmInstallapk.setEnabled(false);
 		mntmInstallapk.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.META_MASK));
 		mntmInstallapk.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				// Show a file chooser dialog and
 				// issue an adb install command if the filepath
 				// return contains '.apk'
 				FileDialog fd = new FileDialog(frmMonkeyboard, "Select an .apk package to install");
 				fd.show();
 				String apk = fd.getDirectory() + fd.getFile();
 				if (apk.contains(".apk"))
 					execAdbInstallCommand(apk);
 				
 				
 			}
 		});
 		
 		mnFile.add(mntmInstallapk);
 		deviceMenuItems.add(mntmInstallapk);
 		
 		JMenuItem mntmExecuteShellCommand = new JMenuItem("Execute adb Command...");
 		mntmExecuteShellCommand.setEnabled(false);
 		mntmExecuteShellCommand.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.META_MASK));
 		mntmExecuteShellCommand.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Component source = (Component) arg0.getSource();
 				String cmd = JOptionPane.showInputDialog(source,
 		                "Enter adb command:");
 				execAdbCommand(cmd);
 			}
 		});
 		mnFile.add(mntmExecuteShellCommand);
 		deviceMenuItems.add(mntmExecuteShellCommand);
 		
 		JMenuItem mntmGetDeviceProperties = new JMenuItem("Get Device Properties");
 		mntmGetDeviceProperties.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				getDeviceProperties();
 			}
 		});
 		mntmGetDeviceProperties.setEnabled(false);
 		mntmGetDeviceProperties.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.META_MASK));
 		mnFile.add(mntmGetDeviceProperties);
 		deviceMenuItems.add(mntmGetDeviceProperties);
 		
 		mnFile.add(new JSeparator());
 		
 		JMenuItem mntmSaveScreenshot = new JMenuItem("Save Screenshot");
 		mntmSaveScreenshot.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				screenshotHandler(null);
 			}
 		});
 		mntmSaveScreenshot.setEnabled(false);
 		mntmSaveScreenshot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.META_MASK));
 		mnFile.add(mntmSaveScreenshot);
 		deviceMenuItems.add(mntmSaveScreenshot);
 		
 		JMenuItem mntmSaveScreenshotAs = new JMenuItem("Save Screenshot As...");
 		mntmSaveScreenshotAs.setVisible(false);
 		mntmSaveScreenshotAs.setEnabled(false);
 		mntmSaveScreenshotAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.META_MASK));
 		mnFile.add(mntmSaveScreenshotAs);
 		deviceMenuItems.add(mntmSaveScreenshotAs);
 		
 		JMenuItem mntmDisplayScreenshot = new JMenuItem("Display Screenshot...");
 		mntmDisplayScreenshot.setVisible(false);
 		mntmDisplayScreenshot.setEnabled(false);
 		mntmDisplayScreenshot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.META_MASK));
 		mnFile.add(mntmDisplayScreenshot);
 		deviceMenuItems.add(mntmDisplayScreenshot);
 	}
 	
 	public void initializeKeyCodeMap() {
 		// modifiers
 		keyCodeMap.put(KeyEvent.VK_SHIFT, "KEYCODE_SHIFT_LEFT");
 		//keyCodeMap.put(KeyEvent.VK_CONTROL, "KEYCODE_CTRL_LEFT");					
 		keyCodeMap.put(KeyEvent.VK_ALT, "KEYCODE_ALT_LEFT");
 	
 		// alphanumeric
 		keyCodeMap.put(KeyEvent.VK_A, "KEYCODE_A");
 		keyCodeMap.put(KeyEvent.VK_B, "KEYCODE_B");
 		keyCodeMap.put(KeyEvent.VK_C, "KEYCODE_C");
 		keyCodeMap.put(KeyEvent.VK_D, "KEYCODE_D");
 		keyCodeMap.put(KeyEvent.VK_E, "KEYCODE_E");
 		keyCodeMap.put(KeyEvent.VK_F, "KEYCODE_F");
 		keyCodeMap.put(KeyEvent.VK_G, "KEYCODE_G");
 		keyCodeMap.put(KeyEvent.VK_H, "KEYCODE_H");
 		keyCodeMap.put(KeyEvent.VK_I, "KEYCODE_I");
 		keyCodeMap.put(KeyEvent.VK_J, "KEYCODE_J");
 		keyCodeMap.put(KeyEvent.VK_K, "KEYCODE_K");
 		keyCodeMap.put(KeyEvent.VK_L, "KEYCODE_L");
 		keyCodeMap.put(KeyEvent.VK_M, "KEYCODE_M");
 		keyCodeMap.put(KeyEvent.VK_N, "KEYCODE_N");
 		keyCodeMap.put(KeyEvent.VK_O, "KEYCODE_O");
 		keyCodeMap.put(KeyEvent.VK_P, "KEYCODE_P");
 		keyCodeMap.put(KeyEvent.VK_Q, "KEYCODE_Q");
 		keyCodeMap.put(KeyEvent.VK_R, "KEYCODE_R");
 		keyCodeMap.put(KeyEvent.VK_S, "KEYCODE_S");
 		keyCodeMap.put(KeyEvent.VK_T, "KEYCODE_T");
 		keyCodeMap.put(KeyEvent.VK_U, "KEYCODE_U");
 		keyCodeMap.put(KeyEvent.VK_V, "KEYCODE_V");
 		keyCodeMap.put(KeyEvent.VK_W, "KEYCODE_W");
 		keyCodeMap.put(KeyEvent.VK_X, "KEYCODE_X");
 		keyCodeMap.put(KeyEvent.VK_Y, "KEYCODE_Y");
 		keyCodeMap.put(KeyEvent.VK_Z, "KEYCODE_Z");
 		keyCodeMap.put(KeyEvent.VK_0, "KEYCODE_0");
 		keyCodeMap.put(KeyEvent.VK_1, "KEYCODE_1");
 		keyCodeMap.put(KeyEvent.VK_2, "KEYCODE_2");
 		keyCodeMap.put(KeyEvent.VK_3, "KEYCODE_3");
 		keyCodeMap.put(KeyEvent.VK_4, "KEYCODE_4");
 		keyCodeMap.put(KeyEvent.VK_5, "KEYCODE_5");
 		keyCodeMap.put(KeyEvent.VK_6, "KEYCODE_6");
 		keyCodeMap.put(KeyEvent.VK_7, "KEYCODE_7");
 		keyCodeMap.put(KeyEvent.VK_8, "KEYCODE_8");
 		keyCodeMap.put(KeyEvent.VK_9, "KEYCODE_9");
 		
 		// dpad
 		keyCodeMap.put(KeyEvent.VK_UP, "KEYCODE_DPAD_UP");
 		keyCodeMap.put(KeyEvent.VK_DOWN, "KEYCODE_DPAD_DOWN");
 		keyCodeMap.put(KeyEvent.VK_LEFT, "KEYCODE_DPAD_LEFT");
 		keyCodeMap.put(KeyEvent.VK_RIGHT, "KEYCODE_DPAD_RIGHT");
 		
 		keyCodeMap.put(KeyEvent.VK_HOME, "KEYCODE_HOME");
 		keyCodeMap.put(KeyEvent.VK_END, "KEYCODE_END");
 		keyCodeMap.put(KeyEvent.VK_PAGE_UP, "KEYCODE_PAGE_UP");
 		keyCodeMap.put(KeyEvent.VK_PAGE_DOWN, "KEYCODE_PAGE_DOWN");
 		keyCodeMap.put(KeyEvent.VK_ESCAPE, "KEYCODE_BACK");
 		
 		// parity with android emulator
 		keyCodeMap.put(KeyEvent.VK_F3, "KEYCODE_CALL");
 		keyCodeMap.put(KeyEvent.VK_F4, "KEYCODE_ENDCALL");
 		keyCodeMap.put(KeyEvent.VK_F5, "KEYCODE_SEARCH");
 		keyCodeMap.put(KeyEvent.VK_F7, "KEYCODE_POWER");		
 		
 		// errata
 		keyCodeMap.put(KeyEvent.VK_CLEAR, "KEYCODE_CLEAR");
 		keyCodeMap.put(KeyEvent.VK_COMMA, "KEYCODE_COMMA");
 		keyCodeMap.put(KeyEvent.VK_PERIOD, "KEYCODE_PERIOD");
 		keyCodeMap.put(KeyEvent.VK_TAB, "KEYCODE_TAB");
 		keyCodeMap.put(KeyEvent.VK_SPACE, "KEYCODE_SPACE");
 		keyCodeMap.put(KeyEvent.VK_ENTER, "KEYCODE_ENTER");
 		keyCodeMap.put(KeyEvent.VK_DELETE, "KEYCODE_DEL");
 		keyCodeMap.put(KeyEvent.VK_BACK_SPACE, "KEYCODE_DEL");
 		keyCodeMap.put(KeyEvent.VK_BACK_QUOTE, "KEYCODE_GRAVE");
 		keyCodeMap.put(KeyEvent.VK_MINUS, "KEYCODE_MINUS");
 		keyCodeMap.put(KeyEvent.VK_EQUALS, "KEYCODE_EQUALS");
 		keyCodeMap.put(KeyEvent.VK_OPEN_BRACKET, "KEYCODE_LEFT_BRACKET");
 		keyCodeMap.put(KeyEvent.VK_CLOSE_BRACKET, "KEYCODE_RIGHT_BRACKET");
 		keyCodeMap.put(KeyEvent.VK_BACK_SLASH, "KEYCODE_BACKSLASH");
 		keyCodeMap.put(KeyEvent.VK_SEMICOLON, "KEYCODE_SEMICOLON");
 		keyCodeMap.put(KeyEvent.VK_SLASH, "KEYCODE_SLASH");
 		keyCodeMap.put(KeyEvent.VK_AT, "KEYCODE_AT");
 		keyCodeMap.put(KeyEvent.VK_PLUS, "KEYCODE_PLUS");
 
 	}
 }
