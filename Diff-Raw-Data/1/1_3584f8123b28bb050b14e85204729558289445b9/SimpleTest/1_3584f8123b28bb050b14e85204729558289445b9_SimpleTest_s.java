 package edu.ucla.cens.test;
 
 //import java.io.ByteArrayInputStream;
 import java.io.IOException;
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
 import javax.microedition.media.MediaException;
 import javax.microedition.media.Player;
 import javax.microedition.media.PlayerListener;
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
 	 * A count of the number of samples taken since application start-up.
 	 */
 	public int samplesTaken = 0;
 
 	/** ****************************** */
 	// GPS Data
 	/**
 	 * Latitude and longitude
 	 */
 	private ChoiceGroup choiceGroup_enableLocation;
 
 	//private boolean bool_locationEnabled = true;
 
 	// private int locationStatus = LocationProvider.OUT_OF_SERVICE;
 
 	public double lat = 0;
 
 	public double lon = 0;
 
 	public LocationProvider lp = null;
 
 	public Location location = null;
 
 	public QualifiedCoordinates coordinates = null;
 
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
 
 	public TextField strItem_gpsState;
 
 	/** ************************************** */
 	// UI Form: Upload
 	public UploadScreen myUpload;
 
 	// UI Form: GPS Details
 	public GPSScreen gpsScreen;
 
 	/** ************************************** */
 	// UI Menu Commands
 	private Command uploadScreenCommand = new Command("-> Upload Screen",
 			Command.SCREEN, 1);
 
 	private Command recordScreenCommand = new Command("-> Record Screen",
 			Command.SCREEN, 1);
 
 	private Command gpsScreenCommand = new Command("-> GPS Screen",
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
 			// StringItem: GPS state
 			this.strItem_gpsState = new TextField("GPS State", "Not Started",
 					32, TextField.ANY);
 			this.myForm.append(this.strItem_gpsState);
 			//
 			// ChoiceGroup: Actions
 			this.myChoiceGroupActions = new ChoiceGroup("Actions:",
 					Choice.POPUP);
 			this.myChoiceGroupActions.append("Stop Recording", null);
 			this.myChoiceGroupActions.append("Start Recording", null);
 			this.myChoiceGroupActions.append("Clear Records", null);
 			this.myChoiceGroupActions.append("Show Location", null);
 			this.myForm.append(this.myChoiceGroupActions);
 			this.myForm.addCommand(this.uploadScreenCommand);
 			this.myForm.addCommand(this.gpsScreenCommand);
 			this.myForm.addCommand(this.exitCommand);
 
 			// Install Command and Item Listeners for Form.
 			this.myForm.setCommandListener(this);
 			this.myForm.setItemStateListener(this);
 
 			this.int_totalLength_ms = 10000;
 			this.textField_totalLength_ms = new TextField("Repeat every (ms)",
 					"10000", 6, TextField.NUMERIC);
 			this.myForm.append(this.textField_totalLength_ms);
 
 			/** ******************************* */
 			// Location
 			this.choiceGroup_enableLocation = new ChoiceGroup("GPS:",
 					Choice.MULTIPLE);
 			this.choiceGroup_enableLocation.append("Enable", null);
 			this.myForm.append(this.choiceGroup_enableLocation);
 
 			// Add commands.
 			this.startUploadScreen();
 			this.startGPSScreen();
 
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
 			this.myUpload = new UploadScreen(this, this.recordStore);
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
 
 	private void startGPSScreen() {
 		this.gpsScreen = new GPSScreen(this);
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
 		} else if (c == this.uploadScreenCommand) {
 			this.uploadScreenCallback();
 		} else if (c == this.gpsScreenCommand) {
 			this.gpsScreenCallback();
 		}
 	}
 
 	private void uploadScreenCallback() {
 		Display.getDisplay(this).setCurrent(this.myUpload.form);
 	}
 
 	private void gpsScreenCallback() {
 		Display.getDisplay(this).setCurrent(this.gpsScreen.textBox);
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
 		} 
 //		else if (item.equals(this.choiceGroup_enableLocation)) {
 //			this.choiceGroup_enableLocation_changed();
 //		}
 	}
 
 	/**
 	 * Called from this.itemStatechanged, this is triggered when
 	 * this.myChoiceGroupActions is the item that changed. Updates the stats.
 	 */
 	private void choiceGroupChanged() {
 		int selectedIndex = this.myChoiceGroupActions.getSelectedIndex();
 		String selectedStr = this.myChoiceGroupActions.getString(selectedIndex);
 		if (selectedStr.equals("Start Recording")) {
 			this.stopPlayer = false;
 			playerRecord();
 		} else if (selectedStr.equals("Stop Recording")) {
 			this.stopPlayer = true;
 			this.playerUpdate(null, "PAUSE", null);
 		} else if (selectedStr.equals("Clear Records")) {
 			synchronized (this.recordStore) {
 				try {
 					RecordEnumeration recIter = this.recordStore
 							.enumerateRecords(null, null, false);
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
 			}
 		} else if (selectedStr.equals("Show Location")) {
 			Coordinates c = getCoordinates();
 			if (c != null) {
 				// use coordinate information
 				this.alertError("lat:" + c.getLatitude() + " lon:"
 						+ c.getLongitude());
 			} else {
 				this.alertError("error getting location");
 			}
 		} else {
 			this.alertError("Bug: no match for" + selectedStr);
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
 
 //	private void choiceGroup_enableLocation_changed() {
 //		this.bool_locationEnabled = this.choiceGroup_enableLocation
 //				.isSelected(0);
 //	}
 
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
 		// while (true) {
 		try {
 			this.playerRecordHelper();
 			// break;
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
 		// }
 	}
 
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
 
 		if (this.int_totalLength_ms <= 0) {
 			this.int_totalLength_ms = 10000;
 		}
 
 //		if (this.bool_locationEnabled) {
 //			playerRecordSetupLocation();
 //		}
 		Coordinates c = getCoordinates();
 
 		if (c != null) {
 			// use coordinate information
 			this.lat = c.getLatitude();
 			this.lon = c.getLongitude();
 			// c.getAltitude();
 		} 
 		this.myThread = new Thread(new SimpleTestHelper(this,
 				this.int_totalLength_ms, "PAUSE"));
 		this.myThread.start();
 	}
 
 //	private void playerRecordSetupLocation() {
 //		try {
 //			if (this.lp == null) {
 //				Criteria cr = new Criteria();
 //				cr.setHorizontalAccuracy(500);
 //				this.lp = LocationProvider.getInstance(cr);
 //				this.lp.setLocationListener(this, -1, -1, -1);
 //			}
 //		} catch (LocationException e) {
 //			this.alertError("LocationException:" + e.getMessage());
 //		}
 //	}
 
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
 		this.playerUpdateStore();
 		this.playerUpdateReset();
 		this.playerUpdateMaybeRecordAgain();
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
 
 		// The Floats may be Float.NaN.
 
 		Boolean isValid = new Boolean(false);
 		Integer lpstate = new Integer(LocationProvider.OUT_OF_SERVICE);
 		Float alt = new Float(Float.NaN);
 		Float horizontal_accuracy = new Float(Float.NaN);
 		Float vertical_accuracy = new Float(Float.NaN);
 		Float course = new Float(Float.NaN);
 		Float speed = new Float(Float.NaN);
 		Long timestamp = new Long(0);
 
 		if (this.lp != null) {
 			lpstate = new Integer(this.lp.getState());
 		}
 		if (this.location != null) {
 			isValid = new Boolean(this.location.isValid());
 			course = new Float(this.location.getCourse());
 			speed = new Float(this.location.getSpeed());
 			timestamp = new Long(this.location.getTimestamp());
 		}
 		if (this.coordinates != null) {
 			alt = new Float(this.coordinates.getAltitude());
 			horizontal_accuracy = new Float(this.coordinates
 					.getHorizontalAccuracy());
 			vertical_accuracy = new Float(this.coordinates
 					.getVerticalAccuracy());
 		}
 
 		new SigSeg(this.recordStore, timeMS, isValid, lpstate, this.lat,
 				this.lon, alt, horizontal_accuracy, vertical_accuracy, course,
 				speed, timestamp);
 
 	}
 
 	private void playerUpdateReset() {
 		// this.lat = 0;
 		// this.lon = 0;
 	}
 
 	/**
 	 * If the stopPlayer flag is not set, then set another time to record again.
 	 */
 	private void playerUpdateMaybeRecordAgain() {
 		if (!this.stopPlayer) {
 			// Set a timer callback.
 			int sleepMS = 0;
 			if (sleepMS < 0) {
 				sleepMS = 0;
 			}
 			this.myThread = new Thread(new SimpleTestHelper(this, 0, "START"));
 			this.myThread.start();
 		}
 	}
 
 	/**
 	 * Commit and close the player.
 	 * 
 	 * @throws IOException
 	 */
 	private void playerUpdateCommitAndClose() throws IOException {
 
 	}
 
 	/**
 	 * This is called when the user selects the "Location" action. It's not
 	 * called by anything other than that.
 	 */
 	public Coordinates getCoordinates() {
 		try {
 			if ((this.lp != null)
 					&& ((this.lp.getState() == LocationProvider.OUT_OF_SERVICE)
 					|| (this.lp.getState() == LocationProvider.TEMPORARILY_UNAVAILABLE)))
 			{
 				this.lp.reset();
 				this.lp = null;
 			}
 			if (this.lp == null) {
 				Criteria cr = new Criteria();
 				cr.setHorizontalAccuracy(500);
 				this.lp = LocationProvider.getInstance(cr);
 				this.lp.setLocationListener(this, -1, -1, -1);
 			}
 			this.location = this.lp.getLocation(5);
 			this.coordinates = this.location.getQualifiedCoordinates();
 			this.gpsScreen.updateDisplayCB();
 			return this.coordinates;
 		} catch (LocationException e) {
 			// this.alertError("LocationException:" + e.getMessage());
 			this.gpsScreen.updateDisplayCB();
 			return null;
 		} catch (InterruptedException e) {
 			// this.alertError("location InterruptedException" +
 			// e.getMessage());
 			this.gpsScreen.updateDisplayCB();
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
 		synchronized (this.recordStore) {
 			try {
 				int totalSaved = this.recordStore.getNumRecords();
 				String numStr = String.valueOf(totalSaved);
 				this.strItem_recordsQueued.setText(numStr);
 			} catch (RecordStoreNotOpenException e) {
 			}
 		}
 		return;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
 	 */
 	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
 		synchronized (this.recordStore) {
 			try {
 				this.recordStore.closeRecordStore();
 			} catch (RecordStoreNotOpenException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (RecordStoreException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		synchronized (this.userInfo_rs) {
 			try {
 				this.userInfo_rs.closeRecordStore();
 			} catch (RecordStoreNotOpenException e) {
 			} catch (RecordStoreException e) {
 			}
 		}
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
 		this.gpsScreen.updateDisplayCB();
 	}
 
 	public void providerStateChanged(LocationProvider lp, int lp_status) {
 		switch (lp_status) {
 		case LocationProvider.AVAILABLE:
 			this.strItem_gpsState.setString("Available");
 			break;
 		case LocationProvider.TEMPORARILY_UNAVAILABLE:
 			this.strItem_gpsState.setString("Temporarily Unavailable");
 			break;
 		case LocationProvider.OUT_OF_SERVICE:
 			this.strItem_gpsState.setString("Out of Service");
 		}
 		this.gpsScreen.updateDisplayCB();
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
 
 }
