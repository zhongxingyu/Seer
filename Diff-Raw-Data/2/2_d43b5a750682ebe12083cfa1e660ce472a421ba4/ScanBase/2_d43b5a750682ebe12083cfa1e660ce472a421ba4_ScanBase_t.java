 /*-
  * Copyright © 2012 Diamond Light Source Ltd., Science and Technology
  * Facilities Council Daresbury Laboratory
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.scan;
 
 import static gda.jython.InterfaceProvider.getCurrentScanInformationHolder;
 import static gda.jython.InterfaceProvider.getDefaultScannableProvider;
 import static gda.jython.InterfaceProvider.getJythonServerNotifer;
 import static gda.jython.InterfaceProvider.getScanStatusHolder;
 import static gda.jython.InterfaceProvider.getTerminalPrinter;
 import static gda.scan.ScanDataPoint.handleZeroInputExtraNameDevice;
 import gda.configuration.properties.LocalProperties;
 import gda.data.NumTracker;
 import gda.data.scan.datawriter.DataWriter;
 import gda.data.scan.datawriter.DefaultDataWriterFactory;
 import gda.device.Detector;
 import gda.device.DeviceException;
 import gda.device.Scannable;
 import gda.device.scannable.PositionCallableProvider;
 import gda.device.scannable.ScannableBase;
 import gda.device.scannable.ScannableUtils;
 import gda.jython.InterfaceProvider;
 import gda.jython.Jython;
 import gda.jython.JythonServer.JythonServerThread;
 import gda.util.OSCommandRunner;
 import gda.util.ScannableLevelComparator;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.UUID;
 import java.util.Vector;
 import java.util.concurrent.Callable;
 
 import org.python.core.PyException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.util.ThreadManager;
 
 /**
  * Base class for objects using the Scan interface.
  */
 public abstract class ScanBase implements Scan {
 
 	public static final String GDA_SCANBASE_FIRST_SCAN_NUMBER_FOR_TEST = "gda.scanbase.firstScanNumber";
 
 	static public volatile boolean explicitlyHalted = false;
 
 	/**
 	 * set true by multiscans to stop any other scans from creating their own datahandlers.
 	 */
 	static public volatile boolean insideMultiScan = false;
 
 	/**
 	 * Variable used to kill running scans The value of this variable may be double checked by calling
 	 * checkForInterrupts within a running thread
 	 */
 	static public volatile boolean interrupted = false;
 
 	private static final Logger logger = LoggerFactory.getLogger(ScanBase.class);
 
 	/**
 	 * Variable used to pause running scans The value of this variable may be double checked by calling
 	 * checkForInterrupts within a running thread
 	 */
 	static public volatile boolean paused = false;
 
 	/**
 	 * Return a string representation of an error in the form 'ExceptionTypeName:message'. Useful for
 	 * e.g. logging. Works for exceptions thrown from Jython code, which would otherwise always contain
 	 * a null message.
 	 */
 	static public String representThrowable(Throwable e) {
 		String message = (e instanceof PyException) ? e.toString() : e.getMessage();
 		return e.getClass().getSimpleName() + ":" + message;
 	}
 	
 	/**
 	 * all the detectors being operated in this scan. This vector is generated from detectors in this.allScannables and
 	 * DetetcorBase.activeDetectors This list is to be used by DataHandlers when writing out the data.
 	 */
 	protected Vector<Detector> allDetectors = new Vector<Detector>();
 
 	/**
 	 * all the scannables being operated in this scan, but *not* Detectors. for some scan types this may be a single
 	 * scannable object.
 	 */
 	protected Vector<Scannable> allScannables = new Vector<Scannable>();
 
 	protected Scan child = null;
 
 	/**
 	 * Command line.
 	 */
 	protected String command = "";
 
 	/**
 	 * Counter to get the current point number. 0 based as for ScanDataPoint
 	 */
 	int currentPointCount = -1;
 
 	/**
 	 * instrument name.
 	 */
 	protected String instrument = "";
 
 	/**
 	 * to allow nested scans to ignore the baton (as it will have already been taken)
 	 */
 	protected boolean isChild = false;
 
 	private boolean lineScanNeedsDoing;
 
 	private DataWriter manuallySetDataWriter = null;
 
 	/**
 	 * unique identifier for this scan
 	 */
 	protected String name = "";
 
 	protected int numberOfChildScans = 0;
 
 	protected Scan parent = null;
 
 	// attributes relating to the thread which started this scan.
 	protected int permissionLevel = 0;
 
 	ScanDataPoint point = null;
 
 	private int pointNumberAtLineBeginning;
 
 	private int positionCallableThreadPoolSize = 3;
 
 	/**
 	 * Used to broadcast points and to write them to a DataWriter. Created before a scan is run.
 	 */
 	protected ScanDataPointPipeline scanDataPointPipeline = null;
 
 	private int scanDataPointQueueLength = 3;
 
 	ScanPlotSettings scanPlotSettings;
 
 	protected IScanStepId stepId = null;
 
 	protected boolean threadHasBeenAuthorised = false;
 
 	protected int TotalNumberOfPoints = 0;
 
 	/**
 	 * The unique number for this scan. Set in direct call to prepareScanNumber and
 	 * in prepareScanForCollection.
 	 */
 	private Long _scanNumber=null;
 	
 	@Override
 	public Long getScanNumber() {
 		return _scanNumber;
 	}
 
 	/**
 	 *
 	 */
 	public ScanBase() {
 		// randomly create the name
 		name = generateRandomName();
 
 		instrument = LocalProperties.get("gda.instrument", "unknown");
 
 		// rbac: you must be the baton holder to be able to create scans. Scan should also run within a Thread which
 		// has the same properties as a thread from the Command server so the rbac system works.
 
 		if (Thread.currentThread() instanceof JythonServerThread) {
 			JythonServerThread currentThread = (JythonServerThread) Thread.currentThread();
 			permissionLevel = currentThread.authorisationLevel;
 			threadHasBeenAuthorised = currentThread.hasBeenAuthorised;
 		} else {
 			permissionLevel = InterfaceProvider.getAuthorisationHolder().getAuthorisationLevel();
 			threadHasBeenAuthorised = false;
 		}
 
 		explicitlyHalted = false;
 	}
 
 	/**
 	 * This method should be called before every task in the run method of a concrete scan class which takes a long
 	 * period of time (e.g. collecting data, moving a motor).
 	 * <P>
 	 * If, since the last time this method was called, interrupted was set to true, an interrupted exception is thrown
 	 * which should be used by the scan to end its run method.
 	 * <P>
 	 * If pause was set to true, then this method will loop endlessly until paused has been set to false.
 	 * <P>
 	 * As these variable are in the base class, these interrupts will effect all scans running.
 	 * 
 	 * @throws InterruptedException
 	 */
 	public static void checkForInterrupts() throws InterruptedException {
 		
 		if (InterfaceProvider.getScanStatusHolder().getScanStatus() == Jython.IDLE) {
 			//do not reset as if the scan thread detects this and so goes idle other threads related to the scan will not get the interruption
 			//we should clear the interruption at the start of the scan instead
 //			paused = false;
 //			interrupted = false; 
 			return;
 		}
 
 		
 		try {
 			if (paused & !interrupted) {
 				InterfaceProvider.getScanStatusHolder().setScanStatus(Jython.PAUSED);
 				while (paused) {
 					Thread.sleep(1000);
 				}
 				InterfaceProvider.getScanStatusHolder().setScanStatus(Jython.RUNNING);
 			}
 		} catch (InterruptedException ex) {
 			interrupted = true;
 		}
 
 		if (interrupted) {
 			InterfaceProvider.getScanStatusHolder().setScanStatus(Jython.IDLE);
 			throw new InterruptedException();
 		}
 	}
 
 	/**
 	 * Returns true if the scan baton has been claimed by a scan that has already started.
 	 * 
 	 * @return boolean
 	 */
 	public static boolean scanRunning() {
 		return getScanStatusHolder().getScanStatus() == Jython.RUNNING;
 	}
 
 	private static double sortArgs(double start, double stop, double step) {
 		// if start > stop then step should be negative
 		if (start > stop && step > 0) {
 			step = (-step);
 			return step;
 		}
 		// if stop is larger then step must be positive
 		else if (start < stop && step < 0) {
 			step = (-step);
 			return step;
 		} else {
 			return step;
 		}
 	}
 
 	private static Object listWrapArray(Object foo) {
 		if (foo.getClass().isArray()) {
 			List<Object> list = Arrays.asList((Object[]) foo);
 			return list;
 		}
 		return foo;
 	}
 
 	/**
 	 * Makes sure the step has the correct sign.
 	 * 
 	 * @param _start
 	 *            Object
 	 * @param _stop
 	 *            Object
 	 * @param _step
 	 *            Object
 	 * @return The correct step value
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public static Object sortArguments(Object _start, Object _stop, Object _step) {
 
 		try {
 			_start = listWrapArray(_start);
 			_stop = listWrapArray(_stop);
 			_step = listWrapArray(_step);
 
 			if (_start instanceof List) {
 				// start, top and step must be of the same size
 
 				int size = ((List) _start).size();
 				int stosize = ((List) _stop).size();
 				int stesize = ((List) _step).size();
 
 				if (!((size == stosize) && (stosize == stesize))) {
 					throw new IllegalArgumentException("start, stop and step need to be of same length");
 				}
 
 				for (int i = 0; i < size; i++) {
 					Object startElement = ((List) _start).get(i);
 					Object stopElement = ((List) _stop).get(i);
 					Object stepElement = ((List) _step).get(i);
 
 					Double start = Double.valueOf(startElement.toString()).doubleValue();
 					Double stop = Double.valueOf(stopElement.toString()).doubleValue();
 					Double step = Double.valueOf(stepElement.toString()).doubleValue();
 
 					step = sortArgs(start, stop, step);
 					((List) _step).set(i, step);
 				}
 
 				return _step;
 			}
 
 			// otherwise assume these are single numbers
 			double start = 0.0;
 			double stop = 0.0;
 			double step = 0.0;
 			// only can do this if we can create doubles
 
 			start = Double.valueOf(_start.toString()).doubleValue();
 			stop = Double.valueOf(_stop.toString()).doubleValue();
 			step = Double.valueOf(_step.toString()).doubleValue();
 
 			return sortArgs(start, stop, step);
 		} catch (NumberFormatException nfe) {
 			throw new IllegalArgumentException("start, stop and step need to be numeric values. " + nfe.getMessage());
 		}
 	}
 
 	/**
 	 * Runs a loop until the scan baton is returned by the currently running scan. Useful for scripts where you do not
 	 * want to start a new scan until the last one has finished.
 	 * 
 	 * @throws InterruptedException
 	 */
 	public static void waitForScanEnd() throws InterruptedException {
 		try {
 			while (InterfaceProvider.getScanStatusHolder().getScanStatus() != Jython.IDLE) {
 				Thread.sleep(1000);
 			}
 			checkForInterrupts();
 		} catch (InterruptedException ex) {
 			// any calling routine should handle when an interrupt is called
 			throw ex;
 		}
 	}
 
 	protected void callAtCommandFailureHooks() {
 		for (Scannable scannable : this.allScannables) {
 			try {
 				scannable.atCommandFailure();
 			} catch (DeviceException e) {
 				String message = "Catching " + e.getClass().getSimpleName() + " during call of " + getName()
 						+ ".atCommandFailure() hook:";
 				logger.error(message, e);
 				getTerminalPrinter().print(message);
 			}
 		}
 	}
 
 	/**
 	 * This should be called at each node of the scan. The collectData method is called for all detectors in the
 	 * DetectorBase.ActiveDetectors static arraylist. Throws two types of errors as scans may want to handle these
 	 * differently.
 	 * 
 	 * @throws Exception
 	 */
 	protected void collectData() throws Exception {
 		triggerDetectorsAndWait();
 		checkForInterrupts();
 		readDevicesAndPublishScanDataPoint();
 	}
 	
 	protected void setScanIdentifierInScanDataPoint(IScanDataPoint point) {
 		//the scanIdentifier returned by getDataWriter().getCurrentScanIdentifier() should match this.scanNumber
 		if(LocalProperties.isScanSetsScanNumber() ){
 			//the scan number is setup in the outermost scan
 			point.setScanIdentifier(Long.toString(getOuterMostScan().getScanNumber()));
 		} else {
 			//otherwise leave to the first datawriter to set the scanIdentifer as it determines the scan number
 			point.setScanIdentifier(getDataWriter().getCurrentScanIdentifier());
 			//TODO only do this if not already set
 		}
 	}
 	
 	/**
 	 * Samples the position of Scannables (via getPosition()), readouts detectors (via readout) and creates a ScanDataPoint
 	 * @throws Exception
 	 */
 	protected void readDevicesAndPublishScanDataPoint() throws Exception {
 		// now can collate the data by creating a DataPoint
 		ScanDataPoint point = new ScanDataPoint();
 		point.setUniqueName(name);
 		point.setCurrentFilename(getDataWriter().getCurrentFileName());
 		point.setHasChild(isChild());
 		point.setNumberOfChildScans(getNumberOfChildScans());
 		point.setStepIds(getStepIds());
 		point.setScanPlotSettings(getScanPlotSettings());
 		point.setScanDimensions(getDimensions());
 		point.setCurrentPointNumber(currentPointCount);
 		point.setNumberOfPoints(TotalNumberOfPoints);
 		point.setInstrument(instrument);
 		point.setCommand(command);
 		setScanIdentifierInScanDataPoint(point);
 
 		for (Scannable scannable : allScannables) {
 			if (scannable.getOutputFormat().length == 0) {
 				handleZeroInputExtraNameDevice(scannable);
 			} else {
 				point.addScannable(scannable);
 			}
 		}
 		for (Detector scannable : allDetectors) {
 			point.addDetector(scannable);
 		}
 		
 		try {
 			populateScannablePositions(point);
 		} catch (Exception e) {
 			throw wrappedException(e);
 		}
 
 		readoutDetectorsAndPublish(point);
 	}
 	
 	/**
 	 * Readout detectors into ScanDataPoint and add to pipeline for possible completion and publishing.
 	 * @param point
 	 * @throws Exception
 	 */
 	protected void readoutDetectorsAndPublish(final ScanDataPoint point) throws Exception {
 		try {
 			populateDetectorData(point);
 		} catch (Exception e) {
 			throw wrappedException(e);
 		}
 		checkForInterrupts();
 		scanDataPointPipeline.put(point);
 		checkForInterrupts();
 	}
 
 	
 	/**
 	 * Blocks while detectors are readout and point is added to pipeline (for the previous point).
 	 */
 	@SuppressWarnings("unused") // subclasses may throw Exceptions
 	public void waitForDetectorReadoutAndPublishCompletion() throws Exception {
 		// Do nothing as readoutDetectorsAndPublish blocks until complete.
 	}
 	
 	public void cancelReadoutAndPublishCompletion () {
 		// Do nothing as readoutDetectorsAndPublish blocks until complete.
 	}
 	
 	protected static DeviceException wrappedException(Throwable e) {
 		String message = (e instanceof PyException) ? e.toString() : e.getMessage();
 		if (message == null) {
 			message = e.getClass().getSimpleName();
 		}
 		return new DeviceException(message , e);
 	}
 
 	static void populateScannablePositions(IScanDataPoint point) throws DeviceException {
 		for (Scannable scannable : point.getScannables()) {
 			Object position;
 			if (scannable instanceof PositionCallableProvider) {
 				Callable<?> positionCallable = ((PositionCallableProvider<?>) scannable).getPositionCallable();
 				position = positionCallable;
 			} else {
 				position = scannable.getPosition();
 			}
 			point.addScannablePosition(position, scannable.getOutputFormat());
 		}
 	}
 
 	static void populateDetectorData(IScanDataPoint point) throws DeviceException, InterruptedException {
 		for (Detector detector : point.getDetectors()) {
 			if (Thread.interrupted()) {
 				throw new InterruptedException(); // in case a device will ignore or has ignored an interrupt request
 			}
 			Object data;
 			if (detector instanceof PositionCallableProvider) {
 				Callable<?> positionCallable = ((PositionCallableProvider<?>) detector).getPositionCallable();
 				data = positionCallable;
 			} else {
 				data = detector.readout();
 			}
 			point.addDetectorData(data, ScannableUtils.getExtraNamesFormats(detector));
 		}
 	}
 	
 	protected void createScanDataPointPipeline() throws Exception {
 		DataWriter dataWriter = (manuallySetDataWriter == null) ? DefaultDataWriterFactory
 				.createDataWriterFromFactory() : manuallySetDataWriter;
 		createScanDataPointPipeline(dataWriter);
 	}
 	
 	protected void createScanDataPointPipeline(DataWriter dataWriter) {
 
 		float estimatedPointsToComputeSimultaneousely;
 		if ((getPositionCallableThreadPoolSize() == 0) | (numberOfScannablesThatCanProvidePositionCallables() == 0)) {
 			estimatedPointsToComputeSimultaneousely = 0;
 		} else {
 			estimatedPointsToComputeSimultaneousely = (float) getPositionCallableThreadPoolSize()
 					/ (float) numberOfScannablesThatCanProvidePositionCallables();
 		}
 		logger.info(String.format(
 				"Creating MultithreadedScanDataPointPipeline which can hold %d points before blocking"
						+ ", and that will on average process %.1f points simultaneously using %d threads.",
 				getScanDataPointQueueLength(), estimatedPointsToComputeSimultaneousely,
 				getPositionCallableThreadPoolSize()));
 
 		scanDataPointPipeline = new MultithreadedScanDataPointPipeline(
 				new ScanDataPointPublisher(dataWriter, this), getPositionCallableThreadPoolSize(),
 				getScanDataPointQueueLength(), getName());
 	}
 
 	@Override
 	public abstract void doCollection() throws Exception;
 
 	/**
 	 * This should be called by all scans when they have finished, including when an exception has been raised.
 	 * 
 	 * @throws DeviceException
 	 */
 	protected void endScan() throws DeviceException {
 
 		// if the interrupt was set
 		if (interrupted) {
 			// stop all scannables
 			try {
 				logger.info("ScanBase stopping " + allScannables.size() + " Scannables involved in interupted Scan");
 				for (Scannable scannable : allScannables) {
 					scannable.stop();
 				}
 				logger.info("ScanBase stopping " + allDetectors.size() + " Detectors involved in interupted Scan");
 				for (Scannable scannable : allDetectors) {
 					scannable.stop();
 				}
 			} finally  {
 				// disengage with the data handler, in case this scan is
 				// restarted
 				scanDataPointPipeline.shutdownNow();
 			}
 
 
 		} else {
 			if (getChild() == null) {
 				// wait for the last point to readout
 				try {
 					waitForDetectorReadoutAndPublishCompletion();
 				} catch (Exception e) {
 					throw new DeviceException(e);
 				}
 				// call the atEnd method of all the scannables
 
 				for (Scannable scannable : this.allScannables) {
 					scannable.atScanLineEnd();
 				}
 
 				for (Scannable scannable : this.allDetectors) {
 					scannable.atScanLineEnd();
 				}
 			}
 
 			// if a standalone scan, or the top-level scan in a nest of scans
 			if (!isChild()) { // FIXME: Move all !isChild() logic up into runScan
 
 				// call the atScanGroupEnd method of all the scannables
 				for (Scannable scannable : this.allScannables) {
 					scannable.atScanEnd();
 				}
 				for (Scannable scannable : this.allDetectors) {
 					scannable.atScanEnd();
 				}
 
 				// tell detectors that collection is over
 				for (Detector detector : allDetectors) {
 					try {
 						detector.endCollection();
 					} catch (DeviceException ex) {
 						logger.error("endScan(): Device Exception: {} ", ex.getMessage());
 						throw ex;
 					}
 				}
 
 				// shutdown the ScanDataPointPipeline (will close DataWriter)
 				try {
 					this.scanDataPointPipeline.shutdown(Long.MAX_VALUE); // no timeout
 				} catch (InterruptedException e) {
 					throw new DeviceException("Interupted while shutting down ScanDataPointPipeline from scan thread", e);
 
 				}
 
 				logger.info("Scan '{}' complete: {}", getName(), getDataWriter().getCurrentFileName());
 
 				getTerminalPrinter().print("Scan complete.");
 			}
 		}
 		if (!isChild()) {  // FIXME: Move all !isChild() logic up into runScan
 
 			// See if we want to kick-off an end-of-scan process
 			String endOfScanName = LocalProperties.get("gda.scan.executeAtEnd");
 			if (endOfScanName != null) {
 				final String command = endOfScanName + " " + getDataWriter().getCurrentFileName();
 				logger.info("running gda.scan.executeAtEnd {}", command);
 
 				final String[] commands = command.split(" ");
 				Thread commandThread = ThreadManager.getThread(new Runnable() {
 					@Override
 					public void run() {
 						logger.debug("Running command (scan end) - \'" + command + "\'.");
 						OSCommandRunner os = new OSCommandRunner(commands, true, null, null);
 						os.logOutput();
 					}
 				}, command);
 				commandThread.start();
 			}
 		}
 	}
 
 	protected String generateRandomName() {
 		return UUID.randomUUID().toString();
 	}
 
 	@Override
 	public Scan getChild() {
 		return child;
 	}
 
 	/**
 	 * Gets the reference to the dataHandler object which this scan uses.
 	 * 
 	 * @return DataWriter
 	 */
 	@Override
 	public DataWriter getDataWriter() {
 		if (scanDataPointPipeline == null) {
 			if (manuallySetDataWriter == null) {
 				throw new IllegalStateException(
 						"Could not get datawriter from data pipeline as there is no pipeline or"
 								+ "manually set datawriter");
 			}
 			return manuallySetDataWriter;
 		}
 		return scanDataPointPipeline.getDataWriter();
 	}
 
 	@Override
 	public Vector<Detector> getDetectors() {
 		return this.allDetectors;
 	}
 
 	/**
 	 * default implementation. Classes that derive from ScanBase which want to support the reporting of scan dimensions
 	 * -@see getDimensions need to override this method
 	 * 
 	 * @see ConcurrentScan
 	 * @return the number of points of this scan object - the whole scan execution can be a hierarchy of parent scan
 	 *         objects and layers of child scan objects
 	 */
 	@Override
 	public int getDimension() {
 		return -1;
 	}
 
 	/**
 	 * @return the dimensions of the hierarchy of scan and child scans that together constitute an individual scan
 	 *         execution For a 1d scan of 10 points the return value is new int[]{10} For a 2d scan of 10 x 20 points
 	 *         the return value is new int[]{10,20}
 	 */
 	// if one of the child scans does not support the reporting of scan dimensions then simply return
 	// as if a 1d scan
 	int[] getDimensions() {
 		Vector<Integer> dim = new Vector<Integer>();
 		Scan scan = this;
 		while (scan != null) {
 			int numberPoints = scan.getDimension();
 			if (numberPoints == -1) {
 				return new int[] { -1 }; // escape if child does not support this concept
 			}
 			// order is parent->child so insert at the front
 			dim.add(0, numberPoints);
 			scan = scan.getParent();
 		}
 		int[] dims = new int[dim.size()];
 		for (int i = 0; i < dim.size(); i++) {
 			dims[i] = dim.get(i);
 		}
 		return dims;
 	}
 
 	Scan getInnerMostScan() {
 		Scan scan = this;
 		while (scan.getChild() != null) {
 			scan = scan.getChild();
 		}
 		return scan;
 	}
 
 	/**
 	 * Returns the unique identifier for this scan. Nested (child) scans share the same identifier as their parents.
 	 * 
 	 * @return String
 	 */
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return numberOfChildScans
 	 */
 	public int getNumberOfChildScans() {
 		return numberOfChildScans;
 	}
 
 	Scan getOuterMostScan() {
 		Scan scan = this;
 		while (scan.getParent() != null) {
 			scan = scan.getParent();
 		}
 		return scan;
 	}
 
 	@Override
 	public Scan getParent() {
 		return parent;
 	}
 
 	public int getPositionCallableThreadPoolSize() {
 		return positionCallableThreadPoolSize;
 	}
 
 	@Override
 	public ScanDataPointPipeline getScanDataPointPipeline() {
 		return scanDataPointPipeline;
 	}
 
 	public int getScanDataPointQueueLength() {
 		return scanDataPointQueueLength;
 	}
 
 	@Override
 	public Vector<Scannable> getScannables() {
 		return this.allScannables;
 	}
 
 	/**
 	 * @return Settings for plotting
 	 */
 	@Override
 	public ScanPlotSettings getScanPlotSettings() {
 		Scan scan = getInnerMostScan();
 		return (scan == this) ? scanPlotSettings : scan.getScanPlotSettings();
 	}
 
 	@Override
 	public IScanStepId getStepId() {
 		return stepId;
 	}
 
 	/**
 	 * Gets the stepIds of scan
 	 */
 	List<IScanStepId> getStepIds() {
 		Vector<IScanStepId> stepsIds = new Vector<IScanStepId>();
 		Scan scan = this;
 		while (scan != null) {
 			IScanStepId stepId = scan.getStepId();
 			// order is parent->child so insert at the front
 			stepsIds.add(0, stepId);
 			scan = scan.getParent();
 		}
 		return stepsIds;
 	}
 
 	/**
 	 * @return Returns the totalNumberOfPoints.
 	 */
 	@Override
 	public int getTotalNumberOfPoints() {
 		return TotalNumberOfPoints;
 	}
 	
 	@Override
 	public boolean isChild() {
 		return isChild;
 	}
 
 	public boolean isLineScanNeedsDoing() {
 		return lineScanNeedsDoing;
 	}
 
 	/**
 	 * Give the command server the latest data object to fan out to its observers.
 	 * 
 	 * @param data
 	 * @deprecated Behaviour now in {@link ScanDataPointPipeline} implementations
 	 */
 	@Deprecated
 	public void notifyServer(Object data) {
 		getJythonServerNotifer().notifyServer(this, data);
 	}
 
 	/**
 	 * A better way to notify the observer which allows users to specify source of the data, not like the one above.
 	 * 
 	 * @param source
 	 * @param data
 	 * @deprecated Behaviour now in {@link ScanDataPointPipeline} implementations
 	 */
 	@Deprecated
 	public void notifyServer(Object source, Object data) {
 		getJythonServerNotifer().notifyServer(source, data);
 	}
 
 	public int numberOfScannablesThatCanProvidePositionCallables() {
 		int n = 0;
 		for (Scannable scn : allScannables) {
 			if (scn instanceof PositionCallableProvider) {
 				n++;
 			}
 		}
 		for (Detector det : allDetectors) {
 			if (det instanceof PositionCallableProvider) {
 				n++;
 			}
 		}
 
 		return n;
 	}
 
 	@Override
 	public void pause() {
 		paused = true;
 	}
 
 	protected void prepareDevicesForCollection() throws Exception {
 		// prepare to collect data
 		for (Detector detector : allDetectors) {
 			checkForInterrupts();
 			detector.prepareForCollection();
 		}
 
 		// then loop through all the Scannables and call their atStart method
 		if (!isChild()) {
 			for (Scannable scannable : this.allScannables) {
 				scannable.atScanStart();
 			}
 			for (Scannable scannable : this.allDetectors) {
 				scannable.atScanStart();
 			}
 		}
 		if (getChild() == null) {
 			
 			for (Scannable scannable : this.allScannables) {
 				scannable.atScanLineStart();
 			}
 			
 			for (Scannable scannable : this.allDetectors) {
 				scannable.atScanLineStart();
 			}
 		}
 	}
 
 	/**
 	 * This should called by all scans just before they start to collect data. It resets the static variable which the
 	 * scan classes use and creates a dataHandler if one has not been created yet.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public void prepareForCollection() throws Exception {
 		try {
 
 			prepareScanForCollection();
 			checkForInterrupts();
 
 			prepareDevicesForCollection();
 		} catch (Exception e) {
 			String message = createMessage(e) + " during prepare for collection";
 			logger.info(message);
 			getTerminalPrinter().print(message);
 			throw e;
 		}
 	}
 
 	protected void prepareScanForCollection() throws Exception {
 		// ignore the baton if the scan is a nested scan inside another
 		// scan object
 		if (getScanStatusHolder().getScanStatus() == Jython.RUNNING && !isChild() && !isLineScanNeedsDoing()) {
 			waitForScanEnd();
 		}
 		prepareScanNumber();
 		prepareStaticVariables();
 
 		// unless it has already been defined, create a new datahandler
 		// for this scan
 		if (scanDataPointPipeline == null) {
 			createScanDataPointPipeline();
 		}
 		getDataWriter().configureScanNumber(getScanNumber());
 	}
 
 
 	protected void prepareScanNumber() throws IOException{
 		if( getScanNumber() == null && !isChild()){
 			if(LocalProperties.isScanSetsScanNumber()){
 				NumTracker runNumber = new NumTracker("scanbase_numtracker");
 				//Allow tests to set the scanNumber
 				int int1 = LocalProperties.getInt(GDA_SCANBASE_FIRST_SCAN_NUMBER_FOR_TEST, -1);
 				if( int1 != -1){
 					runNumber.setFileNumber(int1-1);
 				}
 				_scanNumber = runNumber.incrementNumber();
 			}
 		}
 	}
 	
 	protected synchronized void prepareStaticVariables() {
 		try {
 			// to prevent other scans from starting
 			getScanStatusHolder().setScanStatus(Jython.RUNNING);
 			getCurrentScanInformationHolder().setCurrentScan(this);
 			// cannot have had any interrupts already, so reset values just
 			// in case of GUI logic error
 			ScanBase.interrupted = false;
 			ScanBase.paused = false;
 			
 		} catch (Exception ex) {
 			logger.error("Error starting scan", ex);
 		}
 	}
 
 	private void removeDuplicateScannables() {
 		Vector<Scannable> newAllScannables = new Vector<Scannable>();
 
 		for (Scannable thisScannable : allScannables) {
 			if (!newAllScannables.contains(thisScannable)) {
 				newAllScannables.add(thisScannable);
 			}
 		}
 
 		allScannables = newAllScannables;
 	}
 
 	/**
 	 * Order the allScannables vector using the 'level' attribute.
 	 */
 	protected void reorderScannables() {
 		Collections.sort(allScannables, new ScannableLevelComparator());
 	}
 
 	@Override
 	public void resume() {
 		paused = false;
 	}
 
 	@Override
 	public void run() throws Exception {
 		// lineScanNeedsDoing = false;
 		logger.debug("ScanBase.run() for scan: '" + getName() + "'");
 		do {
 			lineScanNeedsDoing = false;
 			pointNumberAtLineBeginning = currentPointCount;
 			try {
 				// validate scannables
 				for (Scannable scannable : getScannables()) {
 					ScannableBase.validateScannable(scannable);
 				}
 
 				// run the child scan, based on innerscanstatus
 				try {
 					prepareForCollection();
 				} catch (Exception e) {
 					throw new Exception("Exception preparing for scan collection: " + createMessage(e), e);
 				}
 				if (getScannables().size() == 0 && getDetectors().size() == 0) {
 					throw new IllegalStateException("ScanBase: No scannables, detectors or monitors to be scanned!");
 				}
 				
 				try {
 					doCollection();
 				} catch (InterruptedException e) {
 					// need the correct exception type so wrapping code know its an interrupt
 					String message = "Scan interrupted";
 					if (interrupted){
 						message = "Scan aborted on request.";
 					}
 					throw new ScanInterruptedException(message,e.getStackTrace());
 				} catch (Exception e) {
 					throw new Exception("Exception during scan collection: " + createMessage(e), e);
 				}
 			} catch (Exception e) {
 				logger.error(createMessage(e) + " during scan: calling atCommandFailure hooks and then interrupting scan.");
 				cancelReadoutAndPublishCompletion();
 				callAtCommandFailureHooks();
 				throw e;
 			} finally {
 				try {
 					endScan();
 				} catch (DeviceException e) {
 					if ((e instanceof RedoScanLineThrowable) && (getChild() == null)) {
 						logger.info("Redoing scan line because: ", e.getMessage());
 						lineScanNeedsDoing = true;
 						currentPointCount = pointNumberAtLineBeginning;
 					} else {
 						logger.error(createMessage(e) + " Calling atCommandFailure hooks.");
 						callAtCommandFailureHooks();
 						throw e;
 					}
 				}
 			}
 		} while (lineScanNeedsDoing);
 	}
 
 	
 	
 	@Override
 	public void runScan() throws InterruptedException, Exception {
 		if (getScanStatusHolder().getScanStatus() != Jython.IDLE) {
 			throw new Exception("Scan not started as there is already a scan running (could be paused).");
 		}
 		try {
 			// check if a scan or script is currently running.
 			if (this.isChild()) {
 				return;
 			}
 			// Note: some subclasses override the run method so its code cannot
 			// be simply pulled into this method
 			run();
 		} finally {
 			
 			// Leaving interrupted true (if it is) allows jython scripts running
 			// Scans to checkForInterrupts and so end 'for' loops sensibly.
 			// (It is set to false at the start of a scan anyway).
 			paused = false;
 			insideMultiScan = false;
 			
 			// inform observers that scan is complete by sending a Boolean false
 			// nb moved this until after batonTaken is false as we use the flag
 			// to check for scan end.
 			getScanStatusHolder().setScanStatus(Jython.IDLE);
 		}
 
 		if (interrupted) {
 			logger.info("Scan interupted ScanBase.interupted flag");
 			throw new InterruptedException("Scan interrupted");
 		}
 	}
 
 	@Override
 	public void setChild(Scan child) {
 		this.child = child;
 	}
 
 	/**
 	 * Gives the scan a dataHandler reference.
 	 * 
 	 * @param dataWriter
 	 *            DataWriter
 	 */
 	@Override
 	public void setDataWriter(DataWriter dataWriter) {
 		this.manuallySetDataWriter = dataWriter;
 	}
 
 	@Override
 	public void setDetectors(Vector<Detector> allDetectors) {
 		this.allDetectors = allDetectors;
 	}
 
 	@Override
 	public void setIsChild(boolean child) {
 		this.isChild = child;
 	}
 
 	public void setLineScanNeedsDoing(boolean lineScanNeedsDoing) {
 		this.lineScanNeedsDoing = lineScanNeedsDoing;
 	}
 
 	/**
 	 * @param numberOfChildScans
 	 */
 	public void setNumberOfChildScans(int numberOfChildScans) {
 		this.numberOfChildScans = numberOfChildScans;
 	}
 
 	@Override
 	public void setParent(Scan parent) {
 		this.parent = parent;
 	}
 
 	public void setPositionCallableThreadPoolSize(int positionCallableThreadPoolSize) {
 		this.positionCallableThreadPoolSize = positionCallableThreadPoolSize;
 	}
 
 	@Override
 	public void setScanDataPointPipeline(ScanDataPointPipeline scanDataPointPipeline) {
 		this.scanDataPointPipeline = scanDataPointPipeline;
 	}
 
 	public void setScanDataPointQueueLength(int scanDataPointQueueLength) {
 		this.scanDataPointQueueLength = scanDataPointQueueLength;
 	}
 
 	@Override
 	public void setScannables(Vector<Scannable> allScannables) {
 		this.allScannables = allScannables;
 	}
 
 	@Override
 	public void setScanPlotSettings(ScanPlotSettings scanPlotSettings) {
 		Scan scan = getInnerMostScan();
 		if (scan == this) {
 			this.scanPlotSettings = scanPlotSettings;
 		} else {
 			scan.setScanPlotSettings(scanPlotSettings);
 		}
 	}
 
 	@Override
 	public void setStepId(IScanStepId stepId) {
 		this.stepId = stepId;
 	}
 
 	/**
 	 * This should be called by all scans during their constructor. In this method the objects to scan over and the
 	 * detectors to use are identified, and the data handlers objects are created and setup.
 	 */
 	protected synchronized void setUp() {
 		// first add to the list of scannables all those items which are
 		// in the list of defaults
 		Vector<Scannable> defaultScannables = getDefaultScannableProvider().getDefaultScannables();
 		for (Scannable scannable : defaultScannables) {
 			if (!allScannables.contains(scannable)) {
 				allScannables.add(scannable);
 			}
 		}
 		// look to see if any of the scannables was a detector
 		// and add it to the list of detectors
 		/*
 		 * A detector may be specified as a scannable in the constructor to ScanBase class. e.g. within a Jython
 		 * script we may want to execute a scan passing in a non-default detector as: gda.scan.ConcurrentScan( [
 		 * dof, parameters.start.getValue(), parameters.end.getValue(), parameters.step.getValue(), detector
 		 * ]).runScan(); Such a detector would be an instance of Scannable and also DetectorAdapter. Such objects
 		 * need to be added to the list of detectors to be removed.
 		 */
 		ArrayList<Scannable> detectorsToRemove = new ArrayList<Scannable>();
 		for (Scannable scannable : allScannables) {
 			if (scannable instanceof Detector) {
 				// recast
 				Detector det = (Detector) scannable;
 				// add the detector to the list of detectors.
 				if (!allDetectors.contains(det)) {
 					this.allDetectors.add(det);
 					detectorsToRemove.add(scannable);
 				}
 			}
 		}
 
 		// detectors are to be treated differently from scannables,
 		// so remove anything just added to the list of detectors from
 		// the list of scannables
 		for (Object detector : detectorsToRemove) {
 			allScannables.remove(detector);
 		}
 
 		// ensure that there are no duplications in the list of scannables
 		removeDuplicateScannables();
 
 	}
 
 	@Override
 	public void stop() {
 		interrupted = true;
 		paused = false;
 	}
 
 	protected void triggerDetectorsAndWait() throws InterruptedException, DeviceException {
 		// collect data
 		for (Detector detector : allDetectors) {
 			checkForInterrupts();
 			detector.collectData();
 		}
 		checkForInterrupts();
 
 		// check that all detectors have completed data collection
 		for (Detector detector : allDetectors) {
 			detector.waitWhileBusy();
 		}
 		checkForInterrupts();
 	}
 
 	public boolean wasScanExplicitlyHalted() {
 		return explicitlyHalted;
 	}
 	
 	public static void setPaused(boolean paused){
 		if (paused == ScanBase.paused)
 			return;
 
 		if (InterfaceProvider.getScanStatusHolder().getScanStatus() == Jython.IDLE) {
 			logger.info("paused flag set from " + ScanBase.paused + " to " + paused + " by thread :'" + Thread.currentThread().getName() + "' while idle -- ignored");
 			ScanBase.paused = paused;
 			return;
 		}
 
 		logger.info("paused flag set from " + ScanBase.paused + " to " + paused + " by thread :'" + Thread.currentThread().getName() + "'");
 		ScanBase.paused = paused;
 	}
 	/**
 	 * @param interrupted - allows scripts to be stopped at a convenient point
 	 */
 	public static void setInterrupted(boolean interrupted) {
 		if (interrupted == ScanBase.interrupted) 
 			return;
 		
 		if (InterfaceProvider.getScanStatusHolder().getScanStatus() == Jython.IDLE) {
 			String msg = MessageFormat.format("interrupted flag set from {0} to {1} by thread :''{2}'' while idle -- ignored",
 					ScanBase.interrupted, interrupted, Thread.currentThread().getName());
 			logger.info(msg);
 			logger.debug(msg + " from:\n" + generateStackTrace());
 			return;
 		}
 		
 		String msg = MessageFormat.format("interrupted flag set from {0} to {1} by thread :''{2}''",
 				ScanBase.interrupted, interrupted, Thread.currentThread().getName());
 		logger.info(msg);
 		logger.debug(msg + " from:\n" + generateStackTrace());
 		ScanBase.interrupted = interrupted;
 	}
 	
 	
 	public static boolean isInterrupted(){
 		return ScanBase.interrupted;
 	}
 	
 	private static String generateStackTrace() {
 		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
 		String trace = "";
 		for (int i = 2; i < stackTrace.length; i++) {
 			trace =trace + "    " + stackTrace[i].toString() + "\n";
 		}
 		return "    " + trace.trim();
 	}
 	
 	/**
 	 * Returns for example "ErrorType: message" 
 	 * @param e
 	 * @return message
 	 */
 	private String createMessage(Throwable e) {
 		if (e.getMessage() != null) {
 			return e.getClass().getSimpleName() + ": " + e.getMessage();
 		} else if (e instanceof PyException) {
 			return e.getClass().getSimpleName() + ": " + e.toString();
 		} else {
 			return e.getClass().getSimpleName();
 		}
 	}
 
 }
