 /*
  Part of the ReplicatorG project - http://www.replicat.org
  Copyright (c) 2008 Zach Smith
 
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package replicatorg.app;
 
 import java.util.EnumMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.JOptionPane;
 
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import replicatorg.app.exceptions.BuildFailureException;
 import replicatorg.app.exceptions.GCodeException;
 import replicatorg.app.exceptions.JobCancelledException;
 import replicatorg.app.exceptions.JobEndException;
 import replicatorg.app.exceptions.JobRewindException;
 import replicatorg.app.tools.XML;
 import replicatorg.app.ui.MainWindow;
 import replicatorg.drivers.Driver;
 import replicatorg.drivers.DriverFactory;
 import replicatorg.drivers.EstimationDriver;
 import replicatorg.drivers.SDCardCapture;
 import replicatorg.drivers.SimulationDriver;
 import replicatorg.machine.MachineListener;
 import replicatorg.machine.MachineProgressEvent;
 import replicatorg.machine.MachineState;
 import replicatorg.machine.MachineStateChangeEvent;
 import replicatorg.machine.MachineToolStatusEvent;
 import replicatorg.machine.model.MachineModel;
 import replicatorg.machine.model.ToolModel;
 import replicatorg.model.GCodeSource;
 import replicatorg.model.StringListSource;
 
 /**
  * The MachineController object controls a single machine. It contains a single
  * machine driver object. All machine operations (building, stopping, pausing)
  * are performed asynchronously by a thread maintained by the MachineController;
  * calls to MachineController ordinarily trigger an operation and return
  * immediately.
  * 
  * @author phooky
  * 
  */
 public class MachineController {
 
 	/**
 	 * The MachineThread is responsible for communicating with the machine.
 	 * 
 	 * @author phooky
 	 * 
 	 */
 	class MachineThread extends Thread {
 		private MachineState state = MachineState.NOT_ATTACHED;
 		private boolean paused = false;
 		public MachineState getMachineState() { return state; }
 		public boolean isPaused() { return paused; }
 		
 		// Build statistics
 		int linesProcessed = -1;
 		int linesTotal = -1;
 		double startTimeMillis = -1;
 		boolean needsStatusPoll = false;
 	
 		class StatusPollThread extends Thread {
 			public void run() {
 				while (true) {
 					try {
 						Thread.sleep(1000);
 						needsStatusPoll = true;
 					} catch (InterruptedException e) {
 						break;
 					}
 				}
 			}
 		}
 
 		private StatusPollThread pollingThread;
 
 		synchronized void startStatusPolling() {
 			if (pollingThread == null) {
 				pollingThread = new StatusPollThread();
 				pollingThread.start();
 			}
 		}
 		
 		synchronized void stopStatusPolling() {
 			if (pollingThread != null) {
 				pollingThread.interrupt();
 				pollingThread = null;
 			}
 		}
 		
 		/**
 		 * Run the warmup commands.
 		 * 
 		 * @throws BuildFailureException
 		 * @throws InterruptedException
 		 */
 		private void runWarmupCommands() throws BuildFailureException, InterruptedException {
 			System.out.println("Running warmup commands.");
 			buildCodesInternal(new StringListSource(warmupCommands));
 		}			
 
 		private void runCooldownCommands() throws BuildFailureException, InterruptedException {
 			System.out.println("Running cooldown commands.");
 			buildCodesInternal(new StringListSource(cooldownCommands));
 		}
 		
 		private synchronized void setState(MachineState state) {
 			MachineState prev = this.state;
 			this.state = state;
 			emitStateChange(prev, state);
 			notifyAll();
 		}
 
 		private boolean buildCodesInternal(GCodeSource source) throws BuildFailureException, InterruptedException {
 			if (!state.isRunning()) {
 				// Do not continue build if the machine is not building or paused
 				return false;
 			}
 
 			Iterator<String> i = source.iterator();
 			while (i.hasNext()) {
 				String line = i.next();
 				linesProcessed++;
 				if (Thread.interrupted()) {
 					System.err.println("build thread interrupted");
 					return false;
 				}
 				
 				// use our parser to handle the stuff.
 				if (simulator.isSimulating())
 					simulator.parse(line);
 				if (!state.isSimulating()) { driver.parse(line); }
 				
 				try {
 					driver.getParser().handleStops();
 				} catch (JobEndException e) {
 					return false;
 				} catch (JobCancelledException e) {
 					return false;
 				} catch (JobRewindException e) {
 					i = source.iterator();
 					continue;
 				}
 				
 				// simulate the command.
 				if (simulator.isSimulating())
 					simulator.execute();
 				
 				try {
 					if (!state.isSimulating()) {
 						driver.execute();
 					}
 				} catch (GCodeException e) {
 					// TODO: prompt the user to continue.
 					System.out.println("Error: " + e.getMessage());
 				}
 				
 				// did we get any errors?
 				if (!state.isSimulating()) {
 					driver.checkErrors();
 				}
 				
 				// are we paused?
 				if (state.isPaused()) {
 					if (!state.isSimulating()) driver.pause();
 					while (state.isPaused()) {
 						synchronized(this) { wait(); }
 					}
 					if (!state.isSimulating()) driver.unpause();
 				}
 				
 				// bail if we got interrupted.
 				if (state == MachineState.STOPPING) {
 					driver.stop();
 					return false;
 				}
 				// send out updates
 				if (needsStatusPoll) {
 					pollStatus();
 				}
 				MachineProgressEvent progress = 
 					new MachineProgressEvent((double)System.currentTimeMillis()-startTimeMillis,
 							estimatedBuildTime,
 							linesProcessed,
 							linesTotal);
 				emitProgress(progress);
 			}
 			
 			// wait for driver to finish up.
 			if (!state.isSimulating()) while (!driver.isFinished()) {
 				Thread.sleep(100);
 			}
 			return true;
 		}
 
 		/**
 		 * Reset machine to its basic state.
 		 * 
 		 */
 		private synchronized void resetInternal() {
 			driver.reset();
 		}
 
 		public boolean isReady() { return state == MachineState.READY; }
 
 		public void pollStatus() {
 			needsStatusPoll = false;
 			if (state.isRunning()) {
 				if (Base.preferences.getBoolean("build.monitor_temp",false)) {
 					driver.readTemperature();
 					emitToolStatus(driver.getMachine().currentTool());
 				}
 			}
 		}
 		
 		public void forceReset() {
 			interrupt();
 			resetInternal();
 		}
 		
 		public void reset() {
 			setState(MachineState.CONNECTING);
 		}
 
 		GCodeSource currentSource;
 		
 		private void buildInternal(GCodeSource source) {
 			startTimeMillis = System.currentTimeMillis();
 			linesProcessed = 0;
 			linesTotal = warmupCommands.size() + 
 				cooldownCommands.size() +
 				source.getLineCount();
 			startStatusPolling();
 			try {
 				runWarmupCommands();
 				System.out.println("Running build.");
 				buildCodesInternal(source);
 				runCooldownCommands();
 				reset();
 			} catch (BuildFailureException e) {
 				JOptionPane.showMessageDialog(null, e.getMessage(),
 						"Build Failure", JOptionPane.ERROR_MESSAGE);
 
 			} catch (InterruptedException e) {
 				System.out.println("MachineController interrupted");
 			} finally {
 				stopStatusPolling();
 			}
 		}
 
 		String remoteName = null;
 		
 		private void buildRemoteInternal(String remoteName) {
 			if (remoteName == null || !(driver instanceof SDCardCapture)) return;
 			SDCardCapture sdcc = (SDCardCapture)driver;
 			if (!processSDResponse(sdcc.playback(remoteName))) {
 				state = MachineState.STOPPING;
 				return;
 			}
 			while (!driver.isFinished()) {
 				try {
 					// are we paused?
 					if (state.isPaused()) {
 						driver.pause();
 						while (state.isPaused()) {
 							synchronized(this) { wait(); }
 						}
 						driver.unpause();
 					}
 					
 					// bail if we got interrupted.
 					if (state == MachineState.STOPPING) {
 						driver.stop();
 						return;
 					}
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 				}
 			}
 			reset();
 		}
 		
 		public void build(GCodeSource source) {
 			currentSource = source;
 			setState(MachineState.BUILDING);
 		}
 		
 		public void simulate(GCodeSource source) {
 			currentSource = source;
 			setState(MachineState.SIMULATING);
 		}
 
 		public void upload(GCodeSource source, String remoteName) {
 			currentSource = source;
 			this.remoteName = remoteName;
 			setState(MachineState.UPLOADING);
 		}
 
 		public void buildRemote(String remoteName) {
 			this.remoteName = remoteName;
 			setState(MachineState.PLAYBACK_BUILDING);
 		}
 		
 		public void pauseBuild() {
 			if (state.isRunning() && !state.isPaused())
 				setState(state.getPausedState());
 		}
 		
 		public void resumeBuild() {
 			if (state.isRunning() && state.isPaused())
 				setState(state.getUnpausedState());
 		}
 		
 		public void stopBuild() {
 			if (state.isRunning()) {
 				System.err.println("Stopping.");
 				setState(MachineState.STOPPING);				
 			}
 		}
 		
 		public void autoscan() {
 			if (state == MachineState.CONNECTING ||
 					state == MachineState.NOT_ATTACHED) {
 				setState(MachineState.AUTO_SCAN);
 			}
 		}
 		
 		public void run() {
 			while (true) {
 				try {
 					if (state == MachineState.BUILDING || state == MachineState.SIMULATING) {
 						buildInternal(currentSource);
 					} else if (state == MachineState.UPLOADING) {
 						if (driver instanceof SDCardCapture) {
 							SDCardCapture sdcc = (SDCardCapture)driver;
 							if (processSDResponse(sdcc.beginCapture(remoteName))) { 
 								buildInternal(currentSource);
 								System.err.println("Captured bytes: " +Integer.toString(sdcc.endCapture()));
 							} else { state = MachineState.STOPPING; }
 						}
 					} else if (state == MachineState.PLAYBACK_BUILDING) {
 						buildRemoteInternal(remoteName);
 					} else if (state == MachineState.AUTO_SCAN) {
 						driver.autoscan();
 						if (driver.isInitialized()) {
 							setState(MachineState.READY);
 						} else {
 							setState(MachineState.NOT_ATTACHED);
 						}
 					} else if (state == MachineState.CONNECTING) {
 						resetInternal();
 						if (driver.isInitialized()) {
 							setState(MachineState.READY);
 						} else {
 							setState(MachineState.NOT_ATTACHED);
 						}
 					} else if (state == MachineState.STOPPING) {
 						setState(MachineState.READY);						
 					} else {
 						synchronized(this) {
 							if (state == MachineState.READY ||
 								state == MachineState.PLAYBACK_BUILDING ||
 								state.isPaused()) {
 								wait();
 							}
 						}
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	MachineThread machineThread = new MachineThread();
 	
 	// The GCode source of the current build source.
 	protected GCodeSource source;
 	
 	// this is the xml config for this machine.
 	protected Node machineNode;
 
 	// The name of our machine.
 	protected String name;
 
 	public String getName() { return name; }
 	
 	// Our driver object. Null when no driver is selected.
 	public Driver driver = null;
 	
 	// the simulator driver
 	protected SimulationDriver simulator;
 
 	// our current thread.
 	protected Thread thread;
 	
 	// estimated build time in millis
 	protected double estimatedBuildTime = 0;
 
 	// our warmup/cooldown commands
 	protected Vector<String> warmupCommands;
 
 	protected Vector<String> cooldownCommands;
 
 	/**
 	 * Creates the machine object.
 	 */
 	public MachineController(Node mNode) {
 		// save our XML
 		machineNode = mNode;
 
 		parseName();
 		System.out.println("Loading machine: " + name);
 
 		// load our various objects
 		loadDriver();
 		loadExtraPrefs();
 		machineThread = new MachineThread();
 		machineThread.start();
 	}
 
 	public void setCodeSource(GCodeSource source) {
 		this.source = source;
 	}
 
 	// TODO: hide this behind an API
 	private MainWindow window; // for responses to errors, etc.
 	public void setMainWindow(MainWindow window) { this.window = window; }
 
 	static Map<SDCardCapture.ResponseCode,String> sdErrorMap =
 		new EnumMap<SDCardCapture.ResponseCode,String>(SDCardCapture.ResponseCode.class);
 	{
 		sdErrorMap.put(SDCardCapture.ResponseCode.FAIL_NO_CARD,
				"No SD card was detected.  Please make sure you have a working, formatted\n" +
 				"SD card in the motherboard's SD slot and try again.");
 		sdErrorMap.put(SDCardCapture.ResponseCode.FAIL_INIT,
				"ReplicatorG was unable to initialize the SD card.  Please make sure that\n" +
 				"the SD card works properly.");
 		sdErrorMap.put(SDCardCapture.ResponseCode.FAIL_PARTITION,
				"ReplicatorG was unable to read the SD card's partition table.  Please check\n" +
				"that the card is partitioned properly.\n" +
				"If you believe your SD card is OK, try resetting your device and restarting\n" +
				"ReplicatorG."
				);
 		sdErrorMap.put(SDCardCapture.ResponseCode.FAIL_FS,
				"ReplicatorG was unable to open the filesystem on the SD card.  Please make sure\n" +
 				"that the SD card has a single partition formatted with a FAT16 filesystem.");
 		sdErrorMap.put(SDCardCapture.ResponseCode.FAIL_ROOT_DIR,
				"ReplicatorG was unable to read the root directory on the SD card.  Please\n"+
 				"check to see if the SD card was formatted properly.");
 		sdErrorMap.put(SDCardCapture.ResponseCode.FAIL_LOCKED,
				"The SD card cannot be written to because it is locked.  Remove the card,\n" +
 				"switch the lock off, and try again.");
 		sdErrorMap.put(SDCardCapture.ResponseCode.FAIL_NO_FILE,
 				"ReplicatorG could not find the build file on the SD card.");
 		sdErrorMap.put(SDCardCapture.ResponseCode.FAIL_GENERIC,"Unknown SD card error.");
 	}
 	
 	/**
 	 * Process an SD response code and throw up an appropriate dialog for the user.
 	 * @param code the response from the SD request
 	 * @return true if the code indicates success; false if the operation should be aborted
 	 */
 	public boolean processSDResponse(SDCardCapture.ResponseCode code) {
 		if (code == SDCardCapture.ResponseCode.SUCCESS) return true;
 		String message = sdErrorMap.get(code);
 		JOptionPane.showMessageDialog(
 				window,
 				message,
 				"SD card error",
 				JOptionPane.ERROR_MESSAGE);
 		return false;
 	}
 
 	
 	private void parseName() {
 		NodeList kids = machineNode.getChildNodes();
 
 		for (int j = 0; j < kids.getLength(); j++) {
 			Node kid = kids.item(j);
 
 			if (kid.getNodeName().equals("name")) {
 				name = kid.getFirstChild().getNodeValue().trim();
 				return;
 			}
 		}
 
 		name = "Unknown";
 	}
 
 	public boolean buildRemote(String remoteName) {
 		machineThread.buildRemote(remoteName);
 		return true;
 	}
 	
 	/**
 	 * Begin running a job.
 	 */
 	public boolean execute() {
 		// start simulator
 		if (simulator != null)
 			simulator.createWindow();
 
 		// estimate build time.
 		System.out.println("Estimating build time...");
 		estimate();
 
 		// do that build!
 		System.out.println("Running GCode...");
 		machineThread.build(source);
 		return true;
 	}
 
 	public boolean simulate() {
 		// start simulator
 		if (simulator != null)
 			simulator.createWindow();
 
 		// estimate build time.
 		System.out.println("Estimating build time...");
 		estimate();
 
 		// do that build!
 		System.out.println("Running GCode...");
 		machineThread.simulate(source);
 		return true;
 	}
 
 
 	public void estimate() {
 		if (source == null) { return; }
 		try {
 			EstimationDriver estimator = new EstimationDriver();
 			estimator.setMachine(loadModel());
 
 			// run each line through the estimator
 			for (String line : source) {
 				// use our parser to handle the stuff.
 				estimator.parse(line);
 				estimator.execute();
 			}
 
 			if (simulator != null) {
 				System.err.println("setting sim bounds on simulator");
 				simulator.setSimulationBounds(estimator.getBounds());
 			}
 			// oh, how this needs to be cleaned up...
 			if (driver instanceof SimulationDriver) {
 				System.err.println("setting sim bounds on driver");
 				((SimulationDriver)driver).setSimulationBounds(estimator.getBounds());
 			}
 			estimatedBuildTime = estimator.getBuildTime();
 			System.out.println("Estimated build time is: "
 					+ EstimationDriver.getBuildTimeString(estimatedBuildTime));
 		} catch (InterruptedException e) {
 			assert (false);
 			// Should never happen
 		}
 	}
 
 	private MachineModel loadModel() {
 		MachineModel model = new MachineModel();
 		model.loadXML(machineNode);
 		return model;
 	}
 	
 	public MachineState getState() { return machineThread.state; }
 	
 	private void loadDriver() {
 		// load our utility drivers
 		if (Base.preferences.getBoolean("machinecontroller.simulator",true)) {
 			System.err.println("loading simulator");
 			simulator = new SimulationDriver();
 			simulator.setMachine(loadModel());
 		}
 		// load our actual driver
 		NodeList kids = machineNode.getChildNodes();
 		for (int j = 0; j < kids.getLength(); j++) {
 			Node kid = kids.item(j);
 
 			if (kid.getNodeName().equals("driver")) {
 				driver = DriverFactory.factory(kid);
 				driver.setMachine(loadModel());
 				// We begin the initialization process here in a seperate
 				// thread.
 				// The rest of the system should check that the machine is
 				// initialized
 				// before proceeding with prints, etc.
 				Thread initThread = new Thread() {
 					public void run() {
 						synchronized(driver) {
 							System.err.println("Attempting to initialize driver "+driver);
 							driver.initialize();
 						}
 					}
 				};
 				initThread.start();
 				return;
 			}
 		}
 
 		System.out.println("No driver config found.");
 
 		driver = DriverFactory.factory();
 		driver.setMachine(loadModel());
 		driver.initialize();
 	}
 
 	private void loadExtraPrefs() {
 		String[] commands = null;
 		String command = null;
 
 		warmupCommands = new Vector<String>();
 		if (XML.hasChildNode(machineNode, "warmup")) {
 			String warmup = XML.getChildNodeValue(machineNode, "warmup");
 			commands = warmup.split("\n");
 
 			for (int i = 0; i < commands.length; i++) {
 				command = commands[i].trim();
 				warmupCommands.add(new String(command));
 				// System.out.println("Added warmup: " + command);
 			}
 		}
 
 		cooldownCommands = new Vector<String>();
 		if (XML.hasChildNode(machineNode, "cooldown")) {
 			String cooldown = XML.getChildNodeValue(machineNode, "cooldown");
 			commands = cooldown.split("\n");
 
 			for (int i = 0; i < commands.length; i++) {
 				command = commands[i].trim();
 				cooldownCommands.add(new String(command));
 				// System.out.println("Added cooldown: " + command);
 			}
 		}
 	}
 
 	public Driver getDriver() {
 		return driver;
 	}
 
 	public SimulationDriver getSimulatorDriver() {
 		return simulator;
 	}
 
 	public MachineModel getModel() {
 		return loadModel();
 	}
 
 	synchronized public void stop() {
 		machineThread.stopBuild();
 	}
 
 	synchronized public boolean isInitialized() {
 		return (driver != null && driver.isInitialized());
 	}
 
 	synchronized public void pause() {
 		machineThread.pauseBuild();
 	}
 
 	synchronized public void upload(String remoteName) {
 		machineThread.upload(source, remoteName);
 	}
 	synchronized public void unpause() {
 		machineThread.resumeBuild();
 	}
 
 	synchronized public void reset() {
 		machineThread.reset();
 	}
 	
 	public void autoscan() {
 		assert machineThread != null;
 		machineThread.autoscan();
 	}
 	
 	synchronized public boolean isPaused() {
 		return getState().isPaused();
 	}
 	
 	public void dispose() {
 		machineThread.stopBuild();
 		driver.dispose();
 	}
 	
 	private Vector<MachineListener> listeners = new Vector<MachineListener>();
 	
 	public void addMachineStateListener(MachineListener listener) {
 		listeners.add(listener);
 		listener.machineStateChanged(new MachineStateChangeEvent(this,getState()));
 	}
 	
 	protected void emitStateChange(MachineState prev, MachineState current) {
 		MachineStateChangeEvent e = new MachineStateChangeEvent(this, current, prev);
 		for (MachineListener l : listeners) {
 			l.machineStateChanged(e);
 		}
 	}
 
 	protected void emitProgress(MachineProgressEvent progress) {
 		for (MachineListener l : listeners) {
 			l.machineProgress(progress);
 		}
 	}
 
 	protected void emitToolStatus(ToolModel tool) {
 		MachineToolStatusEvent e = new MachineToolStatusEvent(this, tool);
 		for (MachineListener l : listeners) {
 			l.toolStatusChanged(e);
 		}
 	}
 }
