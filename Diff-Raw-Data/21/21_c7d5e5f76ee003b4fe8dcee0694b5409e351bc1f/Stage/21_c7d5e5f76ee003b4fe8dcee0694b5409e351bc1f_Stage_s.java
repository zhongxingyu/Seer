 package edu.ucsb.deepspace;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Properties;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import edu.ucsb.deepspace.MoveCommand.MoveMode;
 import edu.ucsb.deepspace.MoveCommand.MoveType;
 import edu.ucsb.deepspace.gui.MainWindow;
 
 public class Stage {
 	
 	private static final Stage INSTANCE = new Stage();
 	public static Stage getInstance() {return INSTANCE;}
 	
 	public static enum StageTypes {
 		GALIL, FTDI;
 	}
 	private StageTypes stageType = StageTypes.GALIL;
 	public StageTypes getType() {
 		return this.stageType;
 	}
 
 	private double minAz, maxAz, minEl, maxEl;
 	private double maxVelAz, maxAccAz, maxVelEl, maxAccEl;
 	private double maxMoveRelAz, maxMoveRelEl;
 	private int encTol = 10;
 	
 	private TelescopeInterface scope;
 
 	private Timer raDecTracker, lstUpdater;
 	private final ExecutorService exec = Executors.newFixedThreadPool(2);
 	private MainWindow window;
 	private boolean commStatus = false;
 	private ReaderInterface reader;
 	private boolean readerState = false;
 	private DataInterface position = DataGalil.blank();
 	private final Properties actSettings = new Properties();
 	private final Properties settings = new Properties();
 	private LatLongAlt baseLocation, balloonLocation;
 	private double azToBalloon = 0, elToBalloon = 0;
 	private CommGalil stageProtocol, scopeProtocol, readerProtocol;
 	private Boolean continousScanOn = false;
 	private Boolean raOn = false;
 
 	ScriptLoader sl;
 
 
 	/**
 	 * Creates a new stage object and loads all the preset settings into it.
 	 */
 	public Stage() {
 		try {
 			loadSettings();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Creates all the CommGalil, ActGalil, etc. objects and starts the connection. 
 	 * @param window
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	public void initialize(MainWindow window) throws FileNotFoundException, IOException {
 		this.window = window;
 		switch (stageType) {
 		case GALIL:
 			stageProtocol = new CommGalil(2222);
 			scopeProtocol = new CommGalil(23);
 			readerProtocol = new CommGalil(4444);
 			scope = new TelescopeGalil(this, scopeProtocol);
 			reader = new ReaderGalil(this, readerProtocol);
 			sl = new ScriptLoader();
 			loadGalil();
 			break;
 		case FTDI:
 			//Really tired of FTDI stuff.
 			//Reed, 5/19/2012
 			break;
 		}
 		updateLst();
 		if (commStatus) {
 			refreshScripts();
 			reader.start();
 			if (sl.readerReady()) {
 				reader.readerOnOff(true);
 			}
 		}
 	}
 	
 	/**
 	 * Loads the settings.ini file and saves all its variables.
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	private void loadSettings() throws FileNotFoundException, IOException {
 		//read settings from file
 		settings.load(new FileInputStream("Settings.ini"));
 
 		//populate fields with information from file
 		azToBalloon = Double.parseDouble(settings.getProperty("azToBalloon", "0"));
 		elToBalloon = Double.parseDouble(settings.getProperty("elToBalloon", "0"));
 
 		double baseLatitude = Double.parseDouble(settings.getProperty("baseLatitude", "0"));
 		double baseLongitude = Double.parseDouble(settings.getProperty("baseLongitude", "0"));
 		double baseAltitude = Double.parseDouble(settings.getProperty("baseAltitude", "0"));
 		baseLocation =  new LatLongAlt(baseLatitude, baseLongitude, baseAltitude);
 
 		double balloonLatitude = Double.parseDouble(settings.getProperty("balloonLatitude", "0"));
 		double balloonLongitude = Double.parseDouble(settings.getProperty("balloonLongitude", "0"));
 		double balloonAltitude = Double.parseDouble(settings.getProperty("balloonAltitude", "0"));
 		balloonLocation =  new LatLongAlt(balloonLatitude, balloonLongitude, balloonAltitude);
 	}
 	
 	/**
 	 * Saves new settings to settings.ini.
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	private void saveSettings() throws FileNotFoundException, IOException {
 		//prep settings to be stored
 		settings.setProperty("baseLatitude", String.valueOf(baseLocation.getLatitude()));
 		settings.setProperty("baseLongitude", String.valueOf(baseLocation.getLongitude()));
 		settings.setProperty("baseAltitude", String.valueOf(baseLocation.getAltitude()));
 
 		settings.setProperty("balloonLatitude", String.valueOf(balloonLocation.getLatitude()));
 		settings.setProperty("balloonLongitude", String.valueOf(balloonLocation.getLongitude()));
 		settings.setProperty("balloonAltitude", String.valueOf(balloonLocation.getAltitude()));
 
 		settings.setProperty("azToBalloon", String.valueOf(azToBalloon));
 		settings.setProperty("elToBalloon", String.valueOf(elToBalloon));
 
 		//store settings
 		settings.store(new FileOutputStream("Settings.ini"), "");
 	}
 	
 	/**
 	 * Loads the galil.ini file and reads all its variables.
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	private void loadGalil() throws FileNotFoundException, IOException {
 		actSettings.load(new FileInputStream("Galil.ini"));
 		double azOffset = Double.parseDouble(actSettings.getProperty("azOffset", "0"));
 		double elOffset = Double.parseDouble(actSettings.getProperty("elOffset", "0"));
 		minAz = Double.parseDouble(actSettings.getProperty("minAz", "0"));
 		maxAz = Double.parseDouble(actSettings.getProperty("maxAz", "0"));
 		minEl = Double.parseDouble(actSettings.getProperty("minEl", "0"));
 		maxEl = Double.parseDouble(actSettings.getProperty("maxEl", "0"));
 		maxVelAz = Double.parseDouble(actSettings.getProperty("maxVelAz", "0"));
 		maxAccAz = Double.parseDouble(actSettings.getProperty("maxAccAz", "0"));
 		maxVelEl = Double.parseDouble(actSettings.getProperty("maxVelEl", "0"));
 		maxAccEl = Double.parseDouble(actSettings.getProperty("maxAccEl", "0"));
 		maxMoveRelAz = Double.parseDouble(actSettings.getProperty("maxMoveRelAz", "360"));
 		maxMoveRelEl = Double.parseDouble(actSettings.getProperty("maxMoveRelEl", "360"));
 		encTol = Integer.parseInt(actSettings.getProperty("encTol", "10"));
 		scope.setOffsets(azOffset, elOffset);
 
 		window.setMinMaxAzEl(minAz, maxAz, minEl, maxEl);
 		window.setVelAccAzEl(maxVelAz, maxAccAz, maxVelEl, maxAccEl);
 		window.setMaxMoveRel(maxMoveRelAz, maxMoveRelEl);
 	}
 	
 	/**
 	 * Saves the settings of Galil back into galil.ini.
 	 */
 	private void closeGalil() {
 		actSettings.setProperty("azOffset", String.valueOf(scope.getOffset(Axis.AZ)));
 		actSettings.setProperty("elOffset", String.valueOf(scope.getOffset(Axis.EL)));
 		actSettings.setProperty("minAz", String.valueOf(minAz));
 		actSettings.setProperty("maxAz", String.valueOf(maxAz));
 		actSettings.setProperty("minEl", String.valueOf(minEl));
 		actSettings.setProperty("maxEl", String.valueOf(maxEl));
 		actSettings.setProperty("maxVelAz", String.valueOf(maxVelAz));
 		actSettings.setProperty("maxAccAz", String.valueOf(maxAccAz));
 		actSettings.setProperty("maxVelEl", String.valueOf(maxVelEl));
 		actSettings.setProperty("maxAccEl", String.valueOf(maxAccEl));
 		actSettings.setProperty("maxMoveRelAz", String.valueOf(maxMoveRelAz));
 		actSettings.setProperty("maxMoveRelEl", String.valueOf(maxMoveRelEl));
 		actSettings.setProperty("encTol", String.valueOf(encTol));
 		try {
 			actSettings.store(new FileOutputStream("Galil.ini"), "");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private void loadFTDI() throws FileNotFoundException, IOException {
 		actSettings.load(new FileInputStream("FTDI.ini"));
 	}
 
 	public void confirmCommConnection() {
 		commStatus = true;
 	}
 	
 	/**
 	 * Toggles the reader on or off.
 	 */
 	public void toggleReader() {
 		readerState = !readerState;
 		reader.readerOnOff(readerState);
 	}
 	
 	public void loadScripts() {
 		sl.load();
 		window.updateScriptArea("loaded", sl.findLoaded());
 		sl.close();
 	}
 	
 	public void refreshScripts() {
 		window.updateScriptArea("expected", sl.findExpected());
 		window.updateScriptArea("loaded", sl.findLoaded());
 	}
 	
 	/**
 	 * Points to a ra dec position every amount of time specified until told to stop.
 	 * @param ra
 	 * @param dec
 	 */
 	public void startRaDecTracking(final double ra, final double dec) {
 		if (!motorCheck(Axis.AZ)) {
 			window.raDecTrackingButtonUpdater(true, false);
 			return;
 		}
 		if (!motorCheck(Axis.EL)) {
 			window.raDecTrackingButtonUpdater(true, false);
 			return;
 		}
 		long period = 10000;
 		raDecTracker = new Timer("RA/Dec Tracker", true);
 		raDecTracker.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				double az = baseLocation.radecToAz(ra, dec);
 				double el = baseLocation.radecToEl(ra, dec);
 				System.out.println("az:  " + az);
 				System.out.println("el:  " + el);
 				System.out.println();
 				//for FTDI only: velocity of 0 means no motion has occurred
 				//vel=1 currently at rest, but was moving forward
 				//vel=-1 currently at rest, but was moving backwards
 				moveAbsolute(az, el);
 			}
 		}, 0, period);
 	}
 	/**
 	 * Stops ra dec tracking.
 	 */
 	public void stopRaDecTracking() {
 		if (raDecTracker == null) return;
 		raDecTracker.cancel();
 	}
 	/**
 	 * Updates information on the Galil position, times, and ra dec coordinates every second.
 	 */
 	private void updateLst() {
 		lstUpdater = new Timer("LST Updater", true);
 		lstUpdater.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {		
 				Calendar local = Calendar.getInstance();
 
 				double azPos = 0, elPos = 0;
 				if (position != null ) {
 					azPos = scope.getUserPos(Axis.AZ);
 					elPos = scope.getUserPos(Axis.EL);
 				}
 				
 				double ra = baseLocation.azelToRa(azPos, elPos);
 				double dec = baseLocation.azelToDec(azPos, elPos);
 				double lst = baseLocation.lst();
 				String gmt = baseLocation.gmt();
 
 				String sLst = Formatters.formatLst(lst);
 				
 				String out = "Az:  " + Formatters.DEGREE_POS.format(azPos);
 
 				out += "\nEl:  " + Formatters.DEGREE_POS.format(elPos);
 				out += "\nRA:  " + Formatters.FOUR_POINTS.format(ra);
 				out += "\nDec:  " + Formatters.THREE_POINTS.format(dec);
 				out += "\nLST:  " + sLst;
 				out += "\nUTC:  " + gmt;
 				out += "\nLocal:  " + Formatters.HOUR_MIN_SEC.format(local.getTime());
 				
 				window.updateTxtAzElRaDec(out);
 			}
 		}, 0, 1000);
 	}
 	
 	/**
 	 * updates scan command min and max using ra/dec conversions
 	 * @param sc
 	 * @param axis
 	 */
 	public void updateScanCommand(ScanCommand sc, Axis axis) {
 		
 	}
 	/**
 	 * Moves over a specified min and max az and el a specified or continuous number of times. 
 	 * 
 	 * @param azSc
 	 * @param elSc
 	 */
 	
 	//TODO FIGURE OUT A WAY TO MAKE THIS PRETTY
 	public boolean canScan(ScanCommand azSc, ScanCommand elSc, double time){
 		
 		
 			
 			
 		double axAz;
 		double axEl;
 		boolean moveable = true;
 		
 	if (azSc == null){
 			
 
 			double acVelEl = 2*scope.getDistance(elSc.getMin(), elSc.getMax(), Axis.EL)/time;
 			double acTimeEl =acVelEl/maxAccEl;
 
 			 
 			axAz = 0;
 			axEl = .5*maxAccEl*acTimeEl*acTimeEl;
 			axEl = scope.convEncToDeg(axEl, Axis.EL);
 			MoveCommand mcMinEl = new MoveCommand(MoveMode.ABSOLUTE, MoveType.DEGREE, null ,elSc.getMin()-axEl);
 			MoveCommand mcMaxEl = new MoveCommand(MoveMode.ABSOLUTE, MoveType.DEGREE, null ,elSc.getMax()+axEl);
 			moveable = moveable && canMove(mcMinEl);
 			moveable = moveable && canMove(mcMaxEl);
 			
 		}
 		else{
 			double acVelAz = 2*scope.getDistance(azSc.getMin(), azSc.getMax(), Axis.AZ)/time;
 			double acTimeAz =acVelAz/maxAccAz;
 			
 			
 
 			axAz = .5*maxAccAz*acTimeAz*acTimeAz;
 			axEl = 0;
 			axAz = scope.convEncToDeg(axAz, Axis.AZ);
 			System.out.println(axAz);
 
 			MoveCommand mcMinAz = new MoveCommand(MoveMode.ABSOLUTE, MoveType.DEGREE, azSc.getMin()-axAz, null);
 			MoveCommand mcMaxAz = new MoveCommand(MoveMode.ABSOLUTE, MoveType.DEGREE, azSc.getMax()+axAz, null);
 			moveable = moveable && canMove(mcMinAz);
 			moveable = moveable && canMove(mcMaxAz);
 		}
 		
 		
 		
 		
 		
 		
 		
 
 		System.out.println(moveable);
 
 			return moveable;
 	}
 	
 	public boolean test(){
 		return true;
 	}
 	
 	public void startScanning(final ScanCommand azSc, final ScanCommand elSc, final boolean fraster) {
 		if (azSc == null && elSc == null) {
 			window.updateStatusArea("Fatal error.  The ScanCommands associated with Az and El are both null.\n");
 			return;
 		}
 		//I changed the size of the thread pool executor to 2.  This may have unintended consequences
 		//for other pieces of the code.  However, this is the only way I could get both to scan at
 		//the same time AND have the scanning done from inside the ActGalil class.
 		exec.submit(new Runnable() {
 			@Override
 			public void run() {
				
 				//If user presses scan both
 				if (azSc != null && elSc!=null && raOn) {
 					double minAz = azSc.getMin();
 					double maxAz = azSc.getMax();
 					double minEl = azSc.getMin();
 					double maxEl = azSc.getMax();
 					goToPos(new Coordinate(minEl, maxEl));
 					double roundPlace = 1000;
 					//convert cords to time dependent RADEC
 					double minDec = baseLocation.azelToDec(azSc.getMin(), elSc.getMin());
 					double maxDec =baseLocation.azelToDec(azSc.getMax(), elSc.getMax());
 					double minRa = baseLocation.azelToRa(azSc.getMin(), elSc.getMin());
 					double maxRa = baseLocation.azelToRa(azSc.getMax(), elSc.getMax());
 					double maxScanTime = 0;
 
 					do {
 
 
 						waitWhileExecuting(1);
 						//scan uses Az/El coord not ra dec so make scan commands with az/el
						ScanCommand aSc= new ScanCommand(minAz, maxAz, azSc.getTime(), (int)azSc.getReps());
						ScanCommand eSc =new ScanCommand(minEl, maxEl, elSc.getTime(), (int)elSc.getReps());
 						if(canScan(azSc, eSc, elSc.getTime()))
 							scope.scan(aSc,eSc, fraster);		
 						waitWhileExecuting(1);
 						//convert ra coords, to az el this changes with base location time
 						minAz = GalilCalc.round(roundPlace*baseLocation.radecToAz(minRa, minDec), 4);
 						minEl = GalilCalc.round(roundPlace*baseLocation.radecToEl(minRa, minDec),4);
 						maxAz = GalilCalc.round(roundPlace*baseLocation.radecToAz(maxRa, maxDec),4);
 						maxEl = GalilCalc.round(roundPlace*baseLocation.radecToEl(maxRa, maxDec),4);
 						
 						
 					}while(continousScanOn);
 				}
 
 				else {
 					do {
 						waitWhileExecuting(1);
						if (canScan(azSc, elSc, azSc.getTime()))
 							scope.scan(azSc, elSc, fraster);
 						waitWhileExecuting(1);
 					}while(continousScanOn);
 					
 					
 				}
 				
 				window.enableScanButtons();
 
 				
 				
 				
 				
 				
 
 				
 				
 			}});
 		
 	}
 	/**
 	 * Stops movement caused by scanning.
 	 * 
 	 */
 	public void stopScanning() {
 		scope.stopScanning();
 	}
 	
 	
 	//TODO figure out another place for this
 	public double getScanTime(ScanCommand sc, Axis axis){
 		double d;
 		switch(axis){
 		case AZ:
 			d = Math.abs(scope.getDistance(maxAz, minAz, axis));
 			return scope.getScanTime(sc,maxVelAz, maxAccAz,2*d, axis);
 			
 		case EL:
 			d=Math.abs(scope.getDistance(maxEl, minEl, axis));
			return scope.getScanTime(sc, maxVelEl, maxAccEl,2*d, axis);
 
 		default:
 			return 0;
 			
 		}
 		
 	}
 	
 	boolean canMove( final MoveCommand mc){
 		if (mc.getAzAmount() != null) {
 			if (!motorCheck(Axis.AZ) && !motorCheck(Axis.EL)) {
 				window.controlMoveButtons(true);
 				statusArea("The motors must be on before moving.\n");
 				return false;
 			}
 		}
 		
 		if (mc.getMode() == MoveMode.RELATIVE && mc.getType() == MoveType.DEGREE) {
 			if (mc.getAmount(Axis.AZ) == null) {
 				if (Math.abs(mc.getAmount(Axis.EL)) > maxMoveRelEl) {
 					window.controlMoveButtons(true);
 					statusArea("Maximum allowed relative move limit exceeded.\n");
 					return false;
 				}
 			}
 			else if (mc.getAmount(Axis.EL) == null) {
 				if (Math.abs(mc.getAmount(Axis.AZ)) > maxMoveRelAz) {
 					window.controlMoveButtons(true);
 					statusArea("Maximum allowed relative move limit exceeded.\n");
 					return false;
 				}
 			}
 		}
 		
 	
 		
 		if (!scope.validMove(mc, minAz, maxAz, minEl, maxEl)) {
 			System.out.println("this is an invalid move");
 			statusArea("The desired position falls outside the allowed moving angles.\n");
 			window.controlMoveButtons(true);
 			return false;
 		}
 		
 		return true;
 		
 	}
 
 	public void move(final MoveCommand mc) {
 		exec.submit(new Runnable() {
 			public void run() {
 				if (!canMove(mc))
 					return;
 				scope.move(mc);
 				
 				System.out.println("done stage.move\n");
 				window.controlMoveButtons(true);
 			}
 		});
 	}
 	
 	public void setVelocity(double vel, Axis axis) {
 		scope.setVelocity(vel, axis);
 	}
 	
 	public void moveRelative(Double amount, Axis axis, MoveType type) {
 		MoveCommand mc = new MoveCommand(MoveMode.RELATIVE, type, null, null);
 		
 		switch (axis) {
 			case AZ:
 				mc = new MoveCommand(MoveMode.RELATIVE, type, amount, null);
 				setVelocity( this.maxVelAz, axis);
 				break;
 			case EL:
 				mc = new MoveCommand(MoveMode.RELATIVE, type, null, amount);
 				setVelocity(this.maxVelEl, axis);
 				break;
 		}
 		move(mc);
 	}
 	
 	/**
 	 * Convenience method that moves the az and el axis to an absolute position in degrees in two separate 
 	 * threads.
 	 * @param azDeg
 	 * @param elDeg
 	 */
 	private void moveAbsolute(double azDeg, double elDeg) {
 		MoveCommand mc = new MoveCommand(MoveMode.ABSOLUTE, MoveType.DEGREE, azDeg, elDeg);
 		move(mc);
 	}
 	
 	
 
 	/**
 	 * Moves an axis to its default position.
 	 * @param type of axis
 	 */
 	public void index(final Axis axis) {
 		if (scope.isIndexing(axis)) {
 			buttonEnabler("indexAz");
 			buttonEnabler("indexEl");
 			return;
 		}
 		
 		if (!motorCheck(axis)) {
 			buttonEnabler("indexAz");
 			buttonEnabler("indexEl");
 			return;
 		}
 		exec.submit(new Runnable() {
 			@Override
 			public void run() {
 				reader.readerOnOff(false);
 				scope.index(axis);
 				buttonEnabler("indexAz");
 				buttonEnabler("indexEl");
 				reader.readerOnOff(true);
 			}
 		});
 		window.controlMoveButtons(true);
 	}
 
 	
 	
 
 
 	/**
 	 * Returns true if something is moving, false if not.
 	 * @return boolean
 	 */
 	public boolean isMoving() {
 		switch (stageType) {
 			case FTDI:
 				return true; //don't care about FTDI right now (5/5/2012, reed)
 				//return Math.abs(velocity) <= 1;
 			case GALIL:
 				return scope.isMoving();
 			default:
 				return true;
 		}
 	}
 
 	/**
 	 * While isMoving() is true, sleeps for 100ms.
 	 */
 	public void waitWhileExecuting(int thread) {
 		
 		while (position.isThreadEx(thread)) {
 			//statusArea("thread" +thread + "is executing\n");
 			;
 		}
 		//statusArea("Thread" + thread +" is  done executing\n");
 		//pause(50);
 	}
 	/**
 	 * Sleeps for the waitTime.
 	 * @param waitTimeInMS
 	 */
 	private void pause(long waitTimeInMS) {
 		try {
 			Thread.sleep(waitTimeInMS);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	/**
 	 * Sets Galil's min and max az value.
 	 * @param minAz
 	 * @param maxAz
 	 */
 	public void setMinMaxAz(double minAz, double maxAz) {
 		this.minAz = minAz;
 		this.maxAz = maxAz;
 	}
 	/**
 	 * Sets Galil's min and max el value.
 	 * @param minEl
 	 * @param maxEl
 	 */
 	public void setMinMaxEl(double minEl, double maxEl) {
 		this.minEl = minEl;
 		this.maxEl = maxEl;
 	}
 	
 	public void setMaxVelAccAz(double maxVelAz, double maxAccAz) {
 		this.maxVelAz = maxVelAz;
 		this.maxAccAz = maxAccAz;
 		scope.setAccel(maxAccAz, Axis.AZ);
 		scope.setVelocity(maxVelAz, Axis.AZ);
 	}
 	
 	public void setMaxVelAccEl(double maxVelEl, double maxAccEl) {
 		this.maxVelEl = maxVelEl;
 		this.maxAccEl = maxAccEl;
 		scope.setAccel(this.maxEl, Axis.EL);
 		scope.setVelocity(maxVelEl, Axis.EL);
 	}
 	
 	public void setMaxMoveRel(double maxMoveRelAz, double maxMoveRelEl) {
 		this.maxMoveRelAz = maxMoveRelAz;
 		this.maxMoveRelEl = maxMoveRelEl;
 	}
 	
 	public void setRpm(double azRpm, double elRpm) {
 		scope.setSpeedByRpm(azRpm, Axis.AZ);
 		scope.setSpeedByRpm(elRpm, Axis.EL);
 	}
 
 	public int getEncTol() {return encTol;}
 	/**
 	 * Sets some value of Galil, I'm not sure which.
 	 * @param encTol
 	 */
 	public void setEncTol(int encTol) {
 		this.encTol = encTol;
 	}
 	/** 
 	 * Gets and updates the velocity and position of the az axis.
 	 */
 	public void status() {
 		stageProtocol.initialize();
 		System.out.println(stageProtocol.sendRead("XQ #READERI"));
 		System.out.println(stageProtocol.read());
 	}
 	/**
 	 * Sends a message to Galil and prints its reply.
 	 * @param command to send
 	 */
 	public void sendCommand(String command) {
 		System.out.println(stageProtocol.sendRead(command));
 	}
 	/**
 	 * Gets and prints the amount of bytes waiting to be read from Galil.
 	 */
 	public void queueSize() {
 		System.out.println("stage protocol queue size: " + stageProtocol.queueSize());
 		System.out.println("scope protocol queue size: " + scopeProtocol.queueSize());
 		System.out.println(readerProtocol.port + " reader protocol queue size: " + readerProtocol.queueSize());
 	}
 	/**
 	 * Reads off what Galil is waiting to send.
 	 */
 	public void readQueue() {
 		stageProtocol.test();
 		System.out.println("--------");
 		scopeProtocol.test();
 	}
 	
 	/**
 	 * Stops the desired axis.
 	 * @param axis az or el
 	 */
 	public void stop(Axis axis) {
 		scope.stop(axis);
 	}
 	
 	/**
 	 * Toggles the motor on or off.
 	 * @param axis az or el
 	 */
 	public void motorToggle(final Axis axis) {
 		exec.submit(new Runnable() {
 			public void run() {
 				scope.motorToggle(axis);
 			}
 		});
 	}
 
 	/**
 	 * Gets the position of a certain axis.
 	 * @param axisType
 	 * @return
 	 */
 	double encPos(Axis axis) {
 		if (position == null) return 0;
 		switch (axis) {
 			case AZ:
 				return position.azPos();
 			case EL:
 				return position.elPos();
 			default:
 				return 0;
 		}
 	}
 
 	/**
 	 * Moves Galil to a specified coordinate.
 	 * @param c coordinate
 	 */
 	public void goToPos(Coordinate c) {
 		moveAbsolute(c.getAz(), c.getEl());
 	}
 	/**
 	 * Sets the coordinate for ra dec tracking.
 	 * @param ra
 	 * @param dec
 	 */
 	public void setRaDecTracking(double ra, double dec) {
 		window.setRaDec(ra, dec);
 	}
 	/**
 	 * Sets the base location.
 	 * @param pos in latitude, longitude, and position
 	 */
 	public void setBaseLocation(LatLongAlt pos) {
 		baseLocation = pos;
 		calcAzElToBalloon();
 		window.updateBaseBalloonLoc();
 	}
 	/**
 	 * Sets the balloon location.
 	 * @param pos in latitude, longitude, and position
 	 */
 	public void setBalloonLocation(LatLongAlt pos) {
 		balloonLocation = pos;
 		calcAzElToBalloon();
 		window.updateBaseBalloonLoc();
 	}
 	/**
 	 * Converts the longitude and latitude position of the balloon to an az and el coordinate.
 	 */
 	private void calcAzElToBalloon() {
 		Coordinate base = new Coordinate(baseLocation);
 		Coordinate balloon = new Coordinate(balloonLocation);
 
 		double deltaX = balloon.getX() - base.getX();
 		double deltaY = balloon.getY() - base.getY();
 		double deltaZ = balloon.getZ() - base.getZ();
 
 		Coordinate R = new Coordinate(deltaX, deltaY, deltaZ, true);
 
 		Coordinate rHat = base.rHat();
 		Coordinate thetaHat = base.thetaHat();
 		Coordinate phiHat = base.phiHat();
 		thetaHat = thetaHat.negate();
 
 		double xRel = R.dot(phiHat);
 		double yRel = R.dot(thetaHat);
 		double zRel = R.dot(rHat);
 		double xyRel = Math.sqrt(xRel*xRel + yRel*yRel);
 
 		elToBalloon = Math.toDegrees(Math.atan2(zRel, xyRel));
 		azToBalloon = Math.toDegrees(Math.atan2(xRel, yRel));
 		if (azToBalloon < 0) azToBalloon = azToBalloon + 360;
 	}
 	/**
 	 * Returns the balloon location.
 	 * @return
 	 */
 	public LatLongAlt getBalloonLocation() {
 		return balloonLocation;
 	}
 	/**
 	 * Return the base location.
 	 * @return
 	 */
 	public LatLongAlt getBaseLocation() {
 		return baseLocation;
 	}
 	/**
 	 * The string that displays the base location in the program.
 	 * @return
 	 */
 	public String baseLocDisplay() {
 		return "Base Location\n" + baseLocation.guiString();
 	}
 	/**
 	 * The string that displays the balloon location in the program.
 	 * @return
 	 */
 	public String balloonLocDisplay() {
 		String out = "BalloonLocation\n" + balloonLocation.guiString();
 		out += "\nAz to balloon:  " + Formatters.TWO_POINTS_FORCE.format(azToBalloon) + "\n";
 		out += "El to balloon:  " + Formatters.TWO_POINTS_FORCE.format(elToBalloon);
 		return out;
 	}
 	/**
 	 * Moves Galil to the balloon position.
 	 */
 	public void goToBalloon() {
 		moveAbsolute(azToBalloon, elToBalloon);
 	}
 	/**
 	 * Sets a new coordinate as the relative (0,0).
 	 * @param c coordinate
 	 */
 	public void calibrate(Coordinate c) {
 		scope.calibrate(c);
 		System.out.println("az: " + c.getAz());
 		System.out.println("el: " + c.getEl());
 	}
 	/**
 	 * Convenience method for enabling a method.
 	 * @param name
 	 */
 	public void buttonEnabler(String name) {
 		window.buttonEnabler(name);
 	}
 	/**
 	 * Updates Galil's position for the user.
 	 * @param data to update with
 	 */
 	void updatePosition(DataInterface data) {
 		if (data.motorState(Axis.AZ) != position.motorState(Axis.AZ) ||
 		data.motorState(Axis.EL) != position.motorState(Axis.EL)) {
 			window.updateMotorButton(data.motorState(Axis.AZ), Axis.AZ);
 			window.updateMotorButton(data.motorState(Axis.EL), Axis.EL);
 			
 		}
 		
 		position = data;
 		String info = position.info();
 		double azRpm = scope.rpm(data.azMaxVel(), Axis.AZ);
 		double elRpm = scope.rpm(data.elMaxVel(), Axis.EL);
 		info += String.format(Formatters.ACTINFO_FORMAT, "RPM", azRpm, elRpm) + "\n";
 		if (window.debug) {
 			double azAbsPos = scope.getAbsolutePos(Axis.AZ);
 			double elAbsPos = scope.getAbsolutePos(Axis.EL);
 			info += String.format(Formatters.ACTINFO_FORMAT, "Abs Pos", azAbsPos, elAbsPos);
 		}
 		window.updateTxtPosInfo(info);
 	}
 	
 //	void updateVelAcc(String azVel, String azAcc, String elVel, String elAcc) {
 //		window.updateVelAcc(azVel, azAcc, elVel, elAcc);
 //	}
 	
 
 	/**
 	 * Sets where an axis wants to go if it were to complete a successful move.
 	 * @param deg absolute position it will end up
 	 * @param axis
 	 */
 	void setGoalPos(double deg, Axis axis) {
 		window.setGoalPos(Formatters.TWO_POINTS.format(deg), axis);
 	}
 	
 	/**
 	 * Closes the program and saves all settings.
 	 */
 	public void shutdown() {
 		exec.shutdown();
 		reader.stop2();
 		try {
 			saveSettings();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		switch (stageType) {
 			case GALIL:
 				closeGalil();
 				stageProtocol.close();
 				break;
 			case FTDI:
 				break;
 		}
 	}
 	
 	public void statusArea(String message) {
 		window.updateStatusArea(message);
 	}
 	
 	/**
 	 * Returns true if the motor is on.
 	 * @param axis
 	 * @return
 	 */
 	private boolean motorCheck(Axis axis) {
 		if (!position.motorState(axis)) {
 			statusArea(axis.getFullName() + " motor is off.  Please turn motor on before proceeding.\n");
 			return false;
 		}
 		return true;
 	}
 	
 	boolean validateSpeed(double proposedVel, Axis axis) {
 		switch (axis) {
 			case AZ:
 				if (Math.abs(proposedVel) <= maxVelAz) {
 					return true;
 				}
 				break;
 			case EL:
 				if (Math.abs(proposedVel) <= maxVelEl) {
 					return true;
 				}
 				break;
 			default:
 				assert false; //This is only reached if a new axis is added.
 		}
 		return false;
 	}
 	
 	boolean validateAccel(double proposedAcc, Axis axis) {
 		switch (axis) {
 			case AZ:
 				if (Math.abs(proposedAcc) <= maxAccAz) {
 					return true;
 				}
 				break;
 			case EL:
 				if (Math.abs(proposedAcc) <= maxAccEl) {
 					return true;
 				}
 				break;
 			default:
 				assert false; //This is only reached if a new axis is added.
 		}
 		return false;
 	}
 
 	public Boolean getContinousScanOn() {
 		return continousScanOn;
 	}
 
 	public void setContinousScanOn(Boolean continousScanOn) {
 		this.continousScanOn = continousScanOn;
 	}
 	public Boolean RaOn() {
 		return raOn;
 	}
 	public void setRaOn(Boolean raOn){
 		this.raOn = raOn;
 	}
 	
 	
 
 
 
 }
