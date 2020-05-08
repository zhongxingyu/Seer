 package edu.ucla.cens.test;
 
 //import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Vector;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 import javax.microedition.lcdui.Alert;
 import javax.microedition.lcdui.AlertType;
 import javax.microedition.lcdui.Choice;
 import javax.microedition.lcdui.ChoiceGroup;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.Form;
 import javax.microedition.lcdui.Item;
 import javax.microedition.lcdui.ItemStateListener;
 import javax.microedition.lcdui.StringItem;
 import javax.microedition.lcdui.TextField;
 import javax.microedition.media.Manager;
 import javax.microedition.media.MediaException;
 import javax.microedition.media.Player;
 import javax.microedition.media.PlayerListener;
 import javax.microedition.media.control.RecordControl;
 import javax.microedition.media.control.VideoControl;
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
 import javax.microedition.rms.InvalidRecordIDException;
 import javax.microedition.rms.RecordEnumeration;
 import javax.microedition.rms.RecordListener;
 import javax.microedition.rms.RecordStore;
 import javax.microedition.rms.RecordStoreException;
 import javax.microedition.rms.RecordStoreFullException;
 import javax.microedition.rms.RecordStoreNotFoundException;
 import javax.microedition.rms.RecordStoreNotOpenException;
 import javax.microedition.location.*;
 
 /**
  * @author adparker
  * 
  */
 public class SimpleTest extends MIDlet implements CommandListener,
 		PlayerListener, ItemStateListener, RecordListener, LocationListener /* ,LocationListener */{
 
 	private class SimpleTestHelper implements Runnable {
 		/**
 		 * Handle to our parent midlet.
 		 */
 		private SimpleTest midlet = null;
 
 		/**
 		 * Number of miliseconds to sleep.
 		 */
 		private int sleepMS = 0;
 		
 		/**
 		 * The string to deliver to the listener.
 		 */
 		String msg = null;
 
 		/**
 		 * Constructor.
 		 * 
 		 * @param midlet
 		 *            Handle to our parent midlet.
 		 * @param sleepMS
 		 *            Number of seconds to sleep.
 		 * @param msg
 		 *            Message to deliver.
 		 */
 		SimpleTestHelper(SimpleTest midlet, int sleepMS, String msg) {
 			this.midlet = midlet;
 			this.sleepMS = sleepMS;
 			this.msg = msg;
 		}
 
 		/*
 		 * Sleeps for a specified amount of time before triggering
 		 * this.midlet.playerUpdate callback.
 		 */
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Runnable#run()
 		 */
 		public void run() {
 			try {
 				Thread.sleep(this.sleepMS);
 			} catch (InterruptedException ie) {
 				this.midlet.alertError("InterruptedException while sleeping:"
 						+ ie.getMessage());
 			}
 			this.midlet.playerUpdate(null, this.msg, null);
 		}
 	}
 
 	/**
 	 * Convert the byte array to an int starting from the given offset.
 	 * 
 	 * @param b
 	 *            The byte array
 	 * @param offset
 	 *            The array offset
 	 * @return The integer
 	 */
 	public static int byteArrayToInt(byte[] b, int offset) {
 		int value = 0;
 		for (int i = 0; i < 4; i++) {
 			// int shift = (bytewidth - 1 - i) * 8;
 			int shift = i * 8;
 			value += (b[i + offset] & 0x000000FF) << shift;
 		}
 		return value;
 	}
 
 	/**
 	 * Convert a byte[] to a Short.
 	 * 
 	 * @param b
 	 *            The byte array.
 	 * @param offset
 	 *            The offset into the byte array.
 	 * @return The value of the Short located at the specified offset in the
 	 *         byte array.
 	 */
 	public static short byteArrayToShort(byte[] b, int offset) {
 		short value = 0;
 		for (int i = 0; i < 2; i++) {
 			int shift = i * 8;
 			value += (b[i + offset] & 0x000000FF) << shift;
 		}
 		return value;
 	}
 
 	/** ***************************** */
 	// Control
 	/**
 	 * The thread that implements the timer callback.
 	 */
 	private Thread myThread = null;
 
 	/**
 	 * A flag that tells this whether or not stop the player, or to start the
 	 * player.
 	 */
 	private boolean stopPlayer = true;
 
 	private TextField textField_totalLength_ms;
 
 	private int int_totalLength_ms;
 
 	/** ******************************** */
 	// Persistent Store
 	/**
 	 * This is the storage used by this object's SigSeg.
 	 */
 	public RecordStore recordStore = null;
 
 	/**
 	 * This is the storage used to hold user-specific information.
 	 */
 	public RecordStore userInfo_rs = null;
 
 	/**
 	 * A count of the number of acoust samples taken since application start-up.
 	 */
 	public int samplesTaken = 0;
 
 	/** ********************************** */
 	// Acoustic Data
 	private TextField textField_sharedLength_ms;
 
 	private ChoiceGroup choiceGroup_enableMicrophone;
 
 	private boolean bool_microphoneEnabled = false;
 
 	private int int_sharedLength_ms;
 
 	/**
 	 * The byte[] representation of the acoustic data recorded.
 	 */
 	public byte[] output = null;
 
 	/**
 	 * A vector containing power levels of the last several acoustic readings,
 	 * in order from oldest to most recent.
 	 */
 	public Vector power = new Vector();
 
 	/**
 	 * The player for the Recording.
 	 */
 	private Player audioPlayer = null;
 
 	/**
 	 * The player for the camera.
 	 */
 	private Player cameraPlayer = null;
 
 	/**
 	 * Where the player streams the audio data.
 	 */
 	private ByteArrayOutputStream tempoutput = null;
 
 	/**
 	 * The controller for this.audioPlayer;
 	 */
 	private RecordControl rc = null;
 
 	/** ****************************** */
 	// Camera Data
 	/**
 	 * The byte[] representation of the image data captured.
 	 */
 	private ChoiceGroup choiceGroup_enableCamera;
 
 	private boolean bool_cameraEnabled = false;
 
 	private ChoiceGroup choiceGroup_cameraResolution;
 
 	private int cameraWidth = 80;
 
 	private int cameraHeight = 60;
 
 	public byte[] cameraOutput = null;
 
 	private VideoControl vc = null;
 
 	/** ****************************** */
 	// GPS Data
 	/**
 	 * Latitude and longitude
 	 */
 	private ChoiceGroup choiceGroup_enableLocation;
 
 	private boolean bool_locationEnabled = false;
 
 	//private int locationStatus = LocationProvider.OUT_OF_SERVICE;
 
 	public double lat = 0;
 
 	public double lon = 0;
 
 	public LocationProvider lp = null;
 
 	public Location location = null;
 
 	public Coordinates coordinates = null;
 
 	/** ************************************ */
 	// UI Form: Records
 	/**
 	 * The Form object that contains information about records and EOS.
 	 */
 	public Form myForm;
 
 	/**
 	 * Belongs to this.myForm. A combo box for recording/stopping/deleting.
 	 */
 	private ChoiceGroup myChoiceGroupActions;
 
 	/**
 	 * Belongs to this.myForm. A text box that shows some stats on the number of
 	 * recordings made and kept.
 	 */
 	private StringItem strItem_recordsQueued;
 
 	/**
 	 * Belongs to this.myForm. A text box that shows the user name.
 	 */
 	public TextField strItem_userName;
 
 	/** *********************************** */
 	// UI Canvas: Sound Meter
 	/**
 	 * The Canvas object that draws the graph.
 	 */
 	public HelloCanvas myCanvas;
 
 	/** ************************************** */
 	// UI Form: Upload
 	public UploadScreen myUpload;
 
 	/** ************************************** */
 	// UI Menu Commands
 	private Command meterScreenCommand = new Command("-> Meter Screen",
 			Command.SCREEN, 1);
 
 	private Command uploadScreenCommand = new Command("-> Upload Screen",
 			Command.SCREEN, 1);
 
 	private Command recordScreenCommand = new Command("-> Record Screen",
 			Command.SCREEN, 1);
 
 	private Command exitCommand = new Command("Exit", Command.EXIT, 1);
 
 	/**
 	 * Default constructor. It creates a Canvas and a Form object.
 	 */
 	public SimpleTest() {
 		try {
 			// ////////////////////////////////////
 			// Open the record store.
 			try {
 				this.recordStore = RecordStore.openRecordStore("data", true);
 				this.recordStore.addRecordListener(this);
 			} catch (RecordStoreNotFoundException e) {
 				this.alertError("Error: RecordStore not found:"
 						+ e.getMessage());
 			} catch (RecordStoreFullException e) {
 				this.alertError("Error: RecordStore full:" + e.getMessage());
 			} catch (RecordStoreException e) {
 				this.alertError("Error: RecordStore Exception:"
 						+ e.getMessage());
 			}
 
 			/**
 			 * Open the user info record store
 			 */
 			this.userInfo_rs = RecordStore.openRecordStore("userInfo", true);
 
 			// /////////////////////////////////////////////////
 			// UI Record Form - Record Info
 			this.myForm = new Form("Record Info");
 			//
 			// StringItem: # of Saved Samples
 			this.strItem_recordsQueued = new StringItem("Records Queued:",
 					String.valueOf(-1), Item.PLAIN);
 			this.updateStringItem(this.recordStore, -1);
 			this.myForm.append(this.strItem_recordsQueued);
 			//
 			// StringItem: user name
 			String _userName = this.getUserInfoRecord(this.userInfo_rs);
 			this.strItem_userName = new TextField("User name", _userName, 32,
 					TextField.ANY);
 			this.myForm.append(this.strItem_userName);
 			//
 			// ChoiceGroup: Actions
 			this.myChoiceGroupActions = new ChoiceGroup("Actions:",
 					Choice.POPUP);
 			this.myChoiceGroupActions.append("Stop", null);
 			this.myChoiceGroupActions.append("Record", null);
 			this.myChoiceGroupActions.append("Clear", null);
 			this.myChoiceGroupActions.append("Location", null);
 			this.myForm.append(this.myChoiceGroupActions);
 			this.myForm.addCommand(this.meterScreenCommand);
 			this.myForm.addCommand(this.uploadScreenCommand);
 			this.myForm.addCommand(this.exitCommand);
 			// Install Command and Item Listeners for Form.
 			this.myForm.setCommandListener(this);
 			this.myForm.setItemStateListener(this);
 
 			this.int_totalLength_ms = 0;
 			this.textField_totalLength_ms = new TextField("Repeat every (ms)",
 					"10000", 6, TextField.NUMERIC);
 			this.myForm.append(this.textField_totalLength_ms);
 
 			/** ******************************* */
 			// Location
 			this.choiceGroup_enableLocation = new ChoiceGroup("GPS:",
 					Choice.MULTIPLE);
 			this.choiceGroup_enableLocation.append("Enable", null);
 			this.myForm.append(this.choiceGroup_enableLocation);
 
 			/** ****************************** */
 			// Acoustic
 			this.choiceGroup_enableMicrophone = new ChoiceGroup("Microphone:",
 					Choice.MULTIPLE);
 			this.choiceGroup_enableMicrophone.append("Enable", null);
 			this.myForm.append(this.choiceGroup_enableMicrophone);
 			this.int_sharedLength_ms = 0;
 			this.textField_sharedLength_ms = new TextField("Record for (ms)",
 					"1000", 6, TextField.NUMERIC);
 			this.myForm.append(this.textField_sharedLength_ms);
 
 			/** ****************************** */
 			// Camera
 			this.choiceGroup_enableCamera = new ChoiceGroup("Camera:",
 					Choice.MULTIPLE);
 			this.choiceGroup_enableCamera.append("Enable", null);
 			this.myForm.append(this.choiceGroup_enableCamera);
 
 			this.choiceGroup_cameraResolution = new ChoiceGroup("Picture Size",
 					Choice.POPUP);
 			this.choiceGroup_cameraResolution.append("80x60", null);
 			this.choiceGroup_cameraResolution.append("160x120", null);
 			this.choiceGroup_cameraResolution.append("320x240", null);
 			//this.choiceGroup_cameraResolution.append("640x480", null);
 			this.myForm.append(this.choiceGroup_cameraResolution);
 
 			// /////////////////////////////////////////////////
 			// UI Canvas (for the sound meter)
 			this.myCanvas = new HelloCanvas(this);
 			// Add commands.
 			// Install a Command Listener for the Canvas.
 			this.myCanvas.setCommandListener(this);
 			this.myCanvas.addCommand(this.recordScreenCommand);
 			this.myCanvas.addCommand(this.uploadScreenCommand);
 			this.myCanvas.addCommand(this.exitCommand);
 			// this.myForm.addCommand(this.startUploadScreenCommand);
 			// this.myForm.addCommand(this.uploadScreenCommand);
 			this.startUploadScreen();
 
 		} catch (Exception e) {
 			this.alertError("Hi Exception " + e.getMessage());
 			e.printStackTrace();
 		}
 
 	}
 
 	// requires that user record exists, otherwise returns "Default"
 	private String getUserInfoRecord(RecordStore userInfo) {
 		RecordEnumeration rs_enum = null;
 		byte[] recData_ba = null;
 		String recData_str = new String("Default");
 
 		try {
 			rs_enum = userInfo.enumerateRecords(null, null, true);
 			recData_ba = rs_enum.nextRecord();
 			recData_str = new String(recData_ba);
 		} catch (RecordStoreNotOpenException e) {
 			e.printStackTrace();
 		} catch (InvalidRecordIDException e) {
 			e.printStackTrace();
 		} catch (RecordStoreException e) {
 			e.printStackTrace();
 		}
 		return recData_str;
 	}
 
 	/**
 	 * 
 	 */
 	private void startUploadScreen() {
 		// ////////////////////////////////////////
 		// UI Upload form
 		try {
 			this.myUpload = new UploadScreen(this);
 		} catch (RecordStoreNotOpenException e) {
 			this.alertError("Error starting upload screen: " + e.getMessage());
 		} catch (NullPointerException e) {
 			this.alertError("Hi Null Exception: " + e.getMessage());
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			this.alertError("Hi IllegalArg: " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * Callback for the softkey menu
 	 * 
 	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command,
 	 *      javax.microedition.lcdui.Displayable)
 	 */
 	public void commandAction(Command c, Displayable d) {
 		if (c == this.exitCommand) {
 			this.notifyDestroyed();
 		} else if (c == this.recordScreenCommand) {
 			Display.getDisplay(this).setCurrent(this.myForm);
 		} else if (c == this.meterScreenCommand) {
 			Display.getDisplay(this).setCurrent(this.myCanvas);
 			this.myCanvas.start();
 		} else if (c == this.uploadScreenCommand) {
 			this.uploadScreenCallback();
 		}
 	}
 
 	private void uploadScreenCallback() {
 		Display.getDisplay(this).setCurrent(this.myUpload.form);
 
 	}
 
 	/**
 	 * Callback for ChoiceGroup when it has a state change.
 	 * 
 	 * @see javax.microedition.lcdui.ItemStateListener#itemStateChanged(javax.microedition.lcdui.Item)
 	 */
 	public void itemStateChanged(Item item) {
 		if (item.equals(this.myChoiceGroupActions)) {
 			this.choiceGroupChanged();
 		} else if (item.equals(this.strItem_userName)) {
 			String _userName = this.getUserInfoTextField();
 			this.setUserInfoRecord(this.userInfo_rs, _userName);
 		} else if (item.equals(this.textField_totalLength_ms)) {
 			this.int_totalLength_ms = Integer
 					.parseInt(this.textField_totalLength_ms.getString());
 		} else if (item.equals(this.textField_sharedLength_ms)) {
 			this.int_sharedLength_ms = Integer
 					.parseInt(this.textField_sharedLength_ms.getString());
 		} else if (item.equals(this.choiceGroup_enableLocation)) {
 			this.choiceGroup_enableLocation_changed();
 		} else if (item.equals(this.choiceGroup_enableMicrophone)) {
 			this.choiceGroup_enableMicrophone_changed();
 		} else if (item.equals(this.choiceGroup_enableCamera)) {
 			this.choiceGroup_enableCamera_changed();
 		} else if (item.equals(this.choiceGroup_cameraResolution)) {
 			this.choiceGroup_cameraResolution_changed();
 		}
 	}
 
 	/**
 	 * Called from this.itemStatechanged, this is triggered when
 	 * this.myChoiceGroupActions is the item that changed. Updates the stats.
 	 */
 	private void choiceGroupChanged() {
 		int selectedIndex = this.myChoiceGroupActions.getSelectedIndex();
 		String selectedStr = this.myChoiceGroupActions.getString(selectedIndex);
 		if (selectedStr.equals("Record")) {
 			playerRecord();
 		} else if (selectedStr.equals("Stop")) {
 			this.stopPlayer = true;
 			this.playerUpdate(null, "PAUSE", null);
 		} else if (selectedStr.equals("Clear")) {
 			try {
 				RecordEnumeration recIter = this.recordStore.enumerateRecords(
 						null, null, false);
 				while (recIter.hasNextElement()) {
 					int recId = recIter.nextRecordId();
 					this.recordStore.deleteRecord(recId);
 				}
 			} catch (RecordStoreNotOpenException e) {
 				e.printStackTrace();
 			} catch (InvalidRecordIDException e) {
 				e.printStackTrace();
 			} catch (RecordStoreException e) {
 				e.printStackTrace();
 			}
 		} else if (selectedStr.equals("Location")) {
 			Coordinates c = getCoordinates();
 			if (c != null) {
 				// use coordinate information
 				this.alertError("lat:" + c.getLatitude() + "\nlon:"
 						+ c.getLongitude());
 			} else {
 				this.alertError("error getting location");
 			}
 		}
 	}
 
 	private String getUserInfoTextField() {
 		return this.strItem_userName.getString();
 	}
 
 	// If user record exists, it'll update it.
 	// If user record doesn't exist, it'll add one.
 	private void setUserInfoRecord(RecordStore userInfo, String data) {
 		RecordEnumeration rs_enum = null;
 		byte[] ba = data.getBytes();
 		int recID = -1;
 		try {
 			if (userInfo.getNumRecords() == 0) {
 				userInfo.addRecord(ba, 0, ba.length);
 			} else {
 				rs_enum = this.userInfo_rs.enumerateRecords(null, null, true);
 				recID = rs_enum.nextRecordId();
 				userInfo.setRecord(recID, ba, 0, ba.length);
 			}
 		} catch (RecordStoreNotOpenException e) {
 			e.printStackTrace();
 		} catch (RecordStoreFullException e) {
 			e.printStackTrace();
 		} catch (InvalidRecordIDException e) {
 			e.printStackTrace();
 		} catch (RecordStoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void choiceGroup_enableLocation_changed() {
 		this.bool_locationEnabled = this.choiceGroup_enableLocation
 				.isSelected(0);
 	}
 
 	private void choiceGroup_enableCamera_changed() {
 		this.bool_cameraEnabled = this.choiceGroup_enableCamera.isSelected(0);
 	}
 
 	private void choiceGroup_enableMicrophone_changed() {
 		// Check "enable" option
 		this.bool_microphoneEnabled = this.choiceGroup_enableMicrophone
 				.isSelected(0);
 	}
 
 	private void choiceGroup_cameraResolution_changed() {
 		switch (this.choiceGroup_cameraResolution.getSelectedIndex()) {
 		case 3:
 			this.cameraWidth = 640;
 			this.cameraHeight = 480;
 			break;
 		case 2:
 			this.cameraWidth = 320;
 			this.cameraHeight = 240;
 			break;
 		case 1:
 			this.cameraWidth = 160;
 			this.cameraHeight = 120;
 			break;
 		case 0:
 		default:
 			this.cameraWidth = 80;
 			this.cameraHeight = 60;
 			break;
 		}
 	}
 
 	/**
 	 * Callback for PlayerListener.
 	 * 
 	 * @see javax.microedition.media.PlayerListener#playerUpdate(javax.microedition.media.Player,
 	 *      java.lang.String, java.lang.Object)
 	 */
 	public void playerUpdate(Player p, String event, Object eventData) {
 		try {
 			if (event.compareTo("PAUSE") == 0) {
 				playerUpdatePause();
 			} else if (event.compareTo("START") == 0) {
 				playerRecord();
 			}
 		} catch (Exception e) {
 			this.alertError("Exception in handing event:" + event + ":"
 					+ e.getMessage());
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void playerRecord() {
 		while (true) {
 			try {
 				this.playerRecordHelper();
 				break;
 			} catch (MediaException e) {
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e1) {
 				}
 			} catch (IOException e) {
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e1) {
 				}
 			}
 		}
 	}
 
 	/**
 	 * A helper of this.commandAction. This plays the data contained in
 	 * this.output.
 	 */
 	// private void playCallback2() {
 	// try {
 	// ByteArrayInputStream is = new ByteArrayInputStream(this.output);
 	// Player p = Manager.createPlayer(is, "audio/x-wav");
 	// p.start();
 	// } catch (MediaException me) {
 	// } catch (IOException io) {
 	// }
 	// }
 	/**
 	 * This is the callback for when the user selects "Record", or when the
 	 * timer goes off and it's time to record again. It creates a player...
 	 * starts recording... and creates a thread that will later stop the
 	 * recording.
 	 * 
 	 * @throws MediaException
 	 * @throws IOException
 	 */
 	private void playerRecordHelper() throws MediaException, IOException {
 		if (this.int_totalLength_ms < 0) {
 			this.int_totalLength_ms = 10000;
 		}
 		if (this.int_sharedLength_ms < 0) {
 			this.int_sharedLength_ms = 0;
 		} else if (this.int_sharedLength_ms >= this.int_totalLength_ms) {
 			this.int_sharedLength_ms = this.int_totalLength_ms;
 		}
 
 		if (this.bool_locationEnabled) {
 			playerRecordSetupLocation();
 		}
 		if (this.bool_microphoneEnabled) {
 			playerRecordSetupAudio();
 		}
 		if (this.bool_cameraEnabled) {
 			playerRecordSetupVideo();
 		}
 		this.myThread = new Thread(new SimpleTestHelper(this,
 				this.int_sharedLength_ms, "PAUSE"));
 		this.myThread.start();
 	}
 
 	private void playerRecordSetupLocation() {
 		try {
 			if (this.lp == null) {
 				Criteria cr = new Criteria();
 				cr.setHorizontalAccuracy(500);
 				this.lp = LocationProvider.getInstance(cr);
 				this.lp.setLocationListener(this, -1, -1, -1);
 			}
 		} catch (LocationException e) {
 			this.alertError("LocationException:" + e.getMessage());
 		}
 	}
 
 	private void playerRecordSetupVideo() {
 		try {
 			this.cameraPlayer = Manager.createPlayer("capture://video");
 		} catch (MediaException me) {
 			this
 					.alertError("MediaException playerRecordSetupVideo createPlayer():"
 							+ me.getMessage());
 			return;
 		} catch (IOException ioe) {
 			this
 					.alertError("IOException in playerRecordSetupVideo createPlayer():"
 							+ ioe.getMessage());
 			return;
 		}
 		try {
 			this.cameraPlayer.realize();
 		} catch (MediaException e1) {
 			this.alertError("MediaException recordCallback2 realize():"
 					+ e1.getMessage());
 			return;
 		}
 		this.vc = (VideoControl) this.cameraPlayer.getControl("VideoControl");
 		this.vc.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);
 		try {
 			this.cameraPlayer.start();
 		} catch (MediaException e) {
 			this.alertError("MediaException in recordCallback2 this.p.start:");
 			return;
 		}
 	}
 
 	private void playerRecordSetupAudio() {
		this.stopPlayer = false;
 		this.tempoutput = new ByteArrayOutputStream();
 		try {
 			this.audioPlayer = Manager
 					.createPlayer("capture://audio?encoding=pcm");
 		} catch (MediaException me) {
 			this
 					.alertError("MediaException playerRecordSetupAudio createPlayer():"
 							+ me.getMessage());
 			return;
 		} catch (IOException ioe) {
 			this
 					.alertError("IOException in playerRecordSetupAudio createPlayer():"
 							+ ioe.getMessage());
 			return;
 		}
 		try {
 			this.audioPlayer.realize();
 		} catch (MediaException e1) {
 			this.alertError("MediaException playerRecordSetupAudio realize():"
 					+ e1.getMessage());
 			return;
 		}
 		this.audioPlayer.addPlayerListener(this);
 		this.rc = (RecordControl) this.audioPlayer.getControl("RecordControl");
 		this.rc.setRecordStream(this.tempoutput);
 		this.rc.startRecord();
 		try {
 			this.audioPlayer.start();
 		} catch (MediaException e) {
 			this
 					.alertError("MediaException in playerRecordSetupAudio this.p.start:");
 			return;
 		}
 	}
 
 	/**
 	 * Helper function for playerUpdate, dispatched to if the event is "PAUSE".
 	 * 
 	 * @throws IOException
 	 * @throws RecordStoreNotOpenException
 	 * @throws RecordStoreException
 	 * @throws RecordStoreFullException
 	 */
 	private void playerUpdatePause() throws IOException,
 			RecordStoreNotOpenException, RecordStoreException,
 			RecordStoreFullException {
 		++this.samplesTaken;
 		this.playerUpdateCommitAndClose();
 		double noiseLevel = this.getNoiseLevel();
 		playerUpdateCanvas(noiseLevel);
 		playerUpdateStore();
 		playerUpdateReset();
 		playerUpdateMaybeRecordAgain();
 	}
 
 	/**
 	 * Skip the first 44 bytes of the (WAV header), Loop through every byte-pair
 	 * (Short) in this.output. For each Short, accumulate the sum of val^2.
 	 * Return sum/(this.output.length/2).
 	 * 
 	 * @return The noise level.
 	 */
 	private double getNoiseLevel() {
 		long sum = 0;
 		try {
 			for (int i = 44; i < this.output.length; i += 2) {
 				short val = SimpleTest.byteArrayToShort(this.output, i);
 				sum += val * val;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return (2.0 * sum) / this.output.length;
 	}
 
 	/**
 	 * Given a noiseLevel, update the canvas-relate state and repaint.
 	 * 
 	 * @param noiseLevel
 	 */
 	private void playerUpdateCanvas(double noiseLevel) {
 		if (this.power.size() >= 30) {
 			this.power.removeElementAt(0);
 		}
 		this.power.addElement(new Double(noiseLevel));
 		this.myCanvas.repaint();
 	}
 
 	/**
 	 * Based on the noiseLevel, decide to save or drop the sample.
 	 * 
 	 * @param noiseLevel
 	 * @throws RecordStoreNotOpenException
 	 * @throws RecordStoreException
 	 * @throws RecordStoreFullException
 	 * @throws IOException
 	 */
 	private void playerUpdateStore() throws RecordStoreNotOpenException,
 			RecordStoreException, RecordStoreFullException, IOException {
 		long timeMS = java.util.Calendar.getInstance().getTime().getTime();
 		new SigSeg(this.recordStore, timeMS, this.output, this.cameraOutput,
 				this.lat, this.lon);
 		
 
 	}
 
 	private void playerUpdateReset() {
 		this.output = null;
 		this.cameraOutput = null;
 		this.lat = 0;
 		this.lon = 0;
 	}
 	
 	/**
 	 * If the stopPlayer flag is not set, then set another time to record again.
 	 */
 	private void playerUpdateMaybeRecordAgain() {
 		if (!this.stopPlayer) {
 			// Set a timer callback.
 			int sleepMS = this.int_totalLength_ms - this.int_sharedLength_ms;
 			if (sleepMS < 0) {
 				sleepMS = 0;
 			}
 			this.myThread = new Thread(new SimpleTestHelper(this, sleepMS,
 					"START"));
 			this.myThread.start();
 		}
 	}
 
 	/**
 	 * Commit and close the player.
 	 * 
 	 * @throws IOException
 	 */
 	private void playerUpdateCommitAndClose() throws IOException {
 		if (this.rc != null) {
 			try {
 				this.playerUpdateCommitAndCloseAudio();
 			} catch (IOException e) {
 			}
 			this.rc = null;
 			this.audioPlayer = null;
 			this.tempoutput = null;
 		}
 		if (this.vc != null) {
 			try {
 				playerUpdateCommitAndCloseVideo();
 			} catch (IOException e) {
 			}
 			this.cameraPlayer = null;
 			this.vc = null;
 		}
 
 	}
 
 	private void playerUpdateCommitAndCloseAudio() throws IOException {
 		// Close down the recorder.
 		this.rc.commit();
 		this.audioPlayer.close();
 		this.output = this.tempoutput.toByteArray();
 		this.tempoutput.close();
 	}
 
 	private void playerUpdateCommitAndCloseVideo() throws IOException {
 		// Close the camera.
 		try {
 			String arg = new String("encoding=png&width=");
 			arg += String.valueOf(this.cameraWidth) + new String("&height=")
 					+ String.valueOf(this.cameraHeight);
 			//this.alertError("cam: " + arg);
 			this.cameraOutput = this.vc.getSnapshot(arg);
 		} catch (MediaException e) {
 			this.alertError("MediaException getSnapshot: " + e.getMessage());
 			this.cameraOutput = null;
 		} catch (IllegalStateException e) {
 			this.alertError("IllegalStateException getSnapshot: "
 					+ e.getMessage());
 			this.cameraOutput = null;
 		} catch (SecurityException e) {
 			this.alertError("SecurityException getSnapshot: " + e.getMessage());
 			this.cameraOutput = null;
 		}
 		this.cameraPlayer.close();
 		this.vc = null;
 
 	}
 
 	/**
 	 * 
 	 */
 	private Coordinates getCoordinates() {
 		try {
 			if (this.lp == null) {
 				Criteria cr = new Criteria();
 				cr.setHorizontalAccuracy(500);
 				this.lp = LocationProvider.getInstance(cr);
 
 			}
 			this.location = this.lp.getLocation(30);
 			this.coordinates = this.location.getQualifiedCoordinates();
 			return this.coordinates;
 		} catch (LocationException e) {
 			this.alertError("LocationException:" + e.getMessage());
 			return null;
 		} catch (InterruptedException e) {
 			this.alertError("location InterruptedException" + e.getMessage());
 			return null;
 		}
 	}
 
 	/**
 	 * Callback for RecordStore. It updates the text box with new stats.
 	 * 
 	 * @see javax.microedition.rms.RecordListener#recordAdded(javax.microedition.rms.RecordStore,
 	 *      int)
 	 */
 	public void recordAdded(RecordStore recordStore, int recordID) {
 		this.updateStringItem(recordStore, recordID);
 	}
 
 	/**
 	 * Callback for RecordStore
 	 * 
 	 * @see javax.microedition.rms.RecordListener#recordChanged(javax.microedition.rms.RecordStore,
 	 *      int)
 	 */
 	public void recordChanged(RecordStore recordStore, int recordID) {
 		return;
 	}
 
 	/**
 	 * Callback for RecordStore
 	 * 
 	 * @see javax.microedition.rms.RecordListener#recordDeleted(javax.microedition.rms.RecordStore,
 	 *      int)
 	 */
 	public void recordDeleted(RecordStore recordStore, int recordID) {
 		this.updateStringItem(recordStore, recordID);
 	}
 
 	/**
 	 * This is a helper for RecordListener callbacks. It updates the state of
 	 * this.myStringItem.
 	 * 
 	 * @param recordStore
 	 *            This is the record store to use for the update.
 	 * @param recordID
 	 *            This is ignored.
 	 */
 	private void updateStringItem(RecordStore recordStore, int recordID) {
 		try {
 			int totalSaved = this.recordStore.getNumRecords();
 			String numStr = String.valueOf(totalSaved);
 			this.strItem_recordsQueued.setText(numStr);
 		} catch (RecordStoreNotOpenException e) {
 		}
 		return;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
 	 */
 	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
 		if (this.lp != null) {
 			this.lp.reset();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.microedition.midlet.MIDlet#pauseApp()
 	 */
 	protected void pauseApp() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.microedition.midlet.MIDlet#startApp()
 	 */
 	protected void startApp() throws MIDletStateChangeException {
 		Display.getDisplay(this).setCurrent(this.myForm);
 	}
 
 	public void locationUpdated(LocationProvider lp, Location loc) {
 		// Get Coordinates
 		Coordinates c = null;
 		if (loc.isValid()) {
 			c = loc.getQualifiedCoordinates();
 		}
 		if (c != null) {
 			this.lat = c.getLatitude();
 			this.lon = c.getLongitude();
 		} else {
 			this.lat = 0;
 			this.lon = 0;
 		}
 	}
 
 	public void providerStateChanged(LocationProvider lp, int lp_status) {
 		if (lp_status == LocationProvider.OUT_OF_SERVICE) {
 			this.lp.reset();
 			this.lp = null;
 		}
 	}
 
 	/**
 	 * Creates an alert message on the phone.
 	 * 
 	 * @param message
 	 *            The message to display.
 	 */
 	public void alertError(String message) {
 		Alert alert = new Alert("Error", message, null, AlertType.ERROR);
 		Display display = Display.getDisplay(this);
 		Displayable current = display.getCurrent();
 		if (!(current instanceof Alert)) {
 			// This next call can't be done when current is an Alert
 			display.setCurrent(alert, current);
 		}
 	}
 
 	/**
 	 * Checks whether Location API is supported.
 	 * 
 	 * @return a boolean indicating is Location API supported.
 	 */
 	public static boolean isLocationApiSupported() {
 		String version = System.getProperty("microedition.location.version");
 		return (version != null && !version.equals("")) ? true : false;
 	}
 
 	/**
 	 * This UNUSED function is an example of how to playback an audio file.
 	 */
 	public void playCallback() {
 		String SNDFILE = "file:///E:/audio.wav";
 		FileConnection fconn = null;
 		InputStream inStream = null;
 		Player p = null;
 		fconn = this.createFC(SNDFILE, false);
 		if (fconn == null) {
 			this.alertError("fconn was null");
 			return;
 		}
 		try {
 			inStream = fconn.openDataInputStream();
 		} catch (IOException e) {
 			this
 					.alertError("Can't open input stream for:" + SNDFILE + "/n"
 							+ e);
 			return;
 		}
 		try {
 			// create a datasource that captures live audio
 			p = Manager.createPlayer(inStream, "audio/X-wav");
 			p.start();
 		} catch (IOException e) {
 			this.alertError("IOException in createPlayer:" + e.getMessage());
 		} catch (MediaException e) {
 			this.alertError("MediaException in createPlayer:" + e.getMessage());
 		}
 	}
 
 	/**
 	 * All this code is to create a FileConnection. :P
 	 * 
 	 * @param arg
 	 *            The name of the file to open.
 	 * @param write
 	 *            A boolean that indicates whether or not to open the file for
 	 *            writing.
 	 * @return A FileConnection object tied to the specified file.
 	 */
 	private FileConnection createFC(String arg, boolean write) {
 		FileConnection fconn = null;
 		try {
 			fconn = (FileConnection) Connector.open(arg);
 		} catch (IOException e) {
 			this.alertError("Can't open file (bad URL):" + arg + "/n" + e);
 			return null;
 		}
 		if (write) {
 			if (fconn.exists()) {
 				if (!fconn.canWrite()) {
 					this.alertError("Can't write to existing file:" + arg);
 				}
 			} else {
 				try {
 					fconn.create();
 				} catch (IOException e) {
 					this.alertError("Can't create file:" + arg + "/n"
 							+ e.getMessage());
 					return null;
 				}
 			}
 		}
 		return fconn;
 	}
 }
