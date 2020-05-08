 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
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
 
 import gda.configuration.properties.LocalProperties;
 import gda.device.Detector;
 import gda.device.Scannable;
 import gda.device.detector.DetectorWithReadout;
 import gda.device.scannable.ScannableUtils;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 import java.util.Vector;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.FutureTask;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Base class for scan classes which can act as a dimension in a multi-dimensional concurrentscan
  */
 public abstract class ConcurrentScanChild extends ScanBase implements IConcurrentScanChild {
 	
 	private static final Logger logger = LoggerFactory.getLogger(ConcurrentScanChild.class);
 	
 	protected TreeMap<Integer, Scannable[]> scannableLevels;
 	// the list of movements that this scan will perform in the context of the a multi-dimensional set of nested scans
 	protected Vector<ScanObject> allScanObjects = new Vector<ScanObject>();
 	// the list of child scans belonging to this parent
 	protected Vector<IConcurrentScanChild> allChildScans = new Vector<IConcurrentScanChild>();
 
 	private boolean mustBeFinal = false;
 
 	protected FutureTask<Void> detectorReadoutTask;
 	
 	private Boolean readoutConcurrently; // readout detectors in concurrent threads while the next point is started
 
 	protected enum PointPositionInLine {
 		FIRST, MIDDLE, LAST
 	}
 	
 	private PointPositionInLine pointPositionInLine;
 
 	private boolean detectorWithReadoutDeprecationWarningGiven = false;
 	
 	final protected PointPositionInLine getPointPositionInLine() {
 		return pointPositionInLine;
 	}
 
 	final protected void setPointPositionInLine(PointPositionInLine pointPositionInLine) {
 		this.pointPositionInLine = pointPositionInLine;
 	}
 
 	public boolean isReadoutConcurrent() {
 		if (readoutConcurrently == null) {
 			// cache value to prevent change during scan
 			String propertyName = LocalProperties.GDA_SCAN_CONCURRENTSCAN_READOUT_CONCURRENTLY;
 			readoutConcurrently = LocalProperties.check(propertyName, false);
 			if (!isReadoutConcurrent()) {
 				logger.info("This gda installation is configured not to move motors to the next point while detectors are readout in parallel threads");
 			}
 		}
 		return readoutConcurrently;
 	}
 	
 	@Override
 	public IConcurrentScanChild getParent() {
 		return (IConcurrentScanChild) parent;
 	}
 
 	/**
 	 * @param parent
 	 */
 	@Override
 	public void setParent(IConcurrentScanChild parent) {
 		this.parent = parent;
 	}
 
 	@Override
 	public IConcurrentScanChild getChild() {
 		return (IConcurrentScanChild) super.child;
 	}
 
 	/**
 	 * @param child
 	 */
 	@Override
 	public void setChild(IConcurrentScanChild child) {
 		this.child = child;
 	}
 	
 	
 	/**
 	 * @return Returns the scannableLevels.
 	 */
 	@Override
 	public TreeMap<Integer, Scannable[]> getScannableLevels() {
 		return scannableLevels;
 	}
 
 	/**
 	 * @param scannableLevels The scannableLevels to set.
 	 */
 	@Override
 	public void setScannableLevels(TreeMap<Integer, Scannable[]> scannableLevels) {
 		this.scannableLevels = scannableLevels;
 	}
 
 	/**
 	 * @return Returns the allScanObjects.
 	 */
 	@Override
 	public Vector<ScanObject> getAllScanObjects() {
 		return allScanObjects;
 	}
 
 	/**
 	 * @param allScanObjects The allScanObjects to set.
 	 */
 	@Override
 	public void setAllScanObjects(Vector<ScanObject> allScanObjects) {
 		this.allScanObjects = allScanObjects;
 	}
 
 
 	/**
 	 * @return Returns the allChildScans.
 	 */
 	@Override
 	public Vector<IConcurrentScanChild> getAllChildScans() {
 		return allChildScans;
 	}
 
 	/**
 	 * @param allChildScans The allChildScans to set.
 	 */
 	@Override
 	public void setAllChildScans(Vector<IConcurrentScanChild> allChildScans) {
 		this.allChildScans = allChildScans;
 	}
 	
 	/**
 	 * @return Returns the insideMultiScan.
 	 */
 	public static boolean isInsideMultiScan() {
 		return insideMultiScan;
 	}
 
 	/**
 	 * @param insideMultiScan The insideMultiScan to set.
 	 */
 	public static void setInsideMultiScan(boolean insideMultiScan) {
 		ScanBase.insideMultiScan = insideMultiScan;
 	}
 
 	/**
 	 * @return Returns the allScannables.
 	 */
 	@Override
 	public Vector<Scannable> getAllScannables() {
 		return allScannables;
 	}
 
 	/**
 	 * @param allScannables The allScannables to set.
 	 */
 	@Override
 	public void setAllScannables(Vector<Scannable> allScannables) {
 		this.allScannables = allScannables;
 	}
 
 	/**
 	 * @return Returns the allDetectors.
 	 */
 	@Override
 	public Vector<Detector> getAllDetectors() {
 		return allDetectors;
 	}
 
 	/**
 	 * @param allDetectors The allDetectors to set.
 	 */
 	@Override
 	public void setAllDetectors(Vector<Detector> allDetectors) {
 		this.allDetectors = allDetectors;
 	}
 
 	/**
 	 * @return Returns the command.
 	 */
 	@Override
 	public String getCommand() {
 		return command;
 	}
 	
 	@Override
 	public void setCommand(String command) {
 		this.command = command;		
 	}
 
 	public void setMustBeFinal(boolean mustBeFinal) {
 		this.mustBeFinal = mustBeFinal;
 	}
 
 	@Override
 	public boolean isMustBeFinal() {
 		return mustBeFinal;
 	}
 	
 	@Override
 	public void setTotalNumberOfPoints(int totalNumberOfPoints) {
 		TotalNumberOfPoints = totalNumberOfPoints;
 	}
 
 
 	
 	/**
 	 * Moves to the next step unless start is true, then moves to the start of the current (possibly child) scan.
 	 * @throws Exception
 	 */
 	protected void acquirePoint(boolean start, boolean collectDetectors) throws Exception {
 		
 		TreeMap<Integer, Scannable[]> devicesToMoveByLevel;
 		if(collectDetectors) {
 			devicesToMoveByLevel = generateDevicesToMoveByLevel(scannableLevels, allDetectors);
 		} else {
 			devicesToMoveByLevel = scannableLevels;
 		}
 		
 		for (Integer thisLevel : devicesToMoveByLevel.keySet()) {
 
 			Scannable[] scannablesAtThisLevel = devicesToMoveByLevel.get(thisLevel);
 
 			// If there is a detector at this level then wait for detector readout thread to complete
 			for (Scannable scannable : scannablesAtThisLevel) {
 				if (scannable instanceof Detector) {
 					waitForDetectorReadoutAndPublishCompletion();
 					break;
 				}
 			}
 			
 			// trigger at level move start on all Scannables
 			for (Scannable scannable : scannablesAtThisLevel) {
 				if (isScannableToBeMoved(scannable) != null) {
 					if (isScannableToBeMoved(scannable).hasStart()) {
 						scannable.atLevelMoveStart();
 					}
 				}
 			}
 			
 			// on detectors (technically scannables) that implement DetectorWithReadout call waitForReadoutComplete
 			for (Scannable scannable : scannablesAtThisLevel) {
 				if (scannable instanceof DetectorWithReadout) {
 					if (!detectorWithReadoutDeprecationWarningGiven ) {
 						logger.warn("The DetectorWithReadout interface is deprecated. Set gda.scan.concurrentScan.readoutConcurrently to true instead (after reading the 8.24 release note");
 						detectorWithReadoutDeprecationWarningGiven = true;
 					}
					((DetectorWithReadout) scannable).waitForReadoutCompletion();
 				}
 			}
 			
 			for (Scannable device : scannablesAtThisLevel) {
 				if (!(device instanceof Detector)) {
 					// does this scan (is a hierarchy of nested scans) operate this scannable?
 					ScanObject scanObject = isScannableToBeMoved(device);
 					if (scanObject != null) {
 						if (start) {
 							scanObject.moveToStart();
 						} else {
 							scanObject.moveStep();
 						}
 					}
 				} else {
 					((Detector) device).collectData();
 				}
 			}
 
 			// pause here until all the scannables at this level have finished moving
 			for (Entry<Integer, Scannable[]> entriesByLevel : devicesToMoveByLevel.entrySet()) {
 				Scannable[] scannablesAtLevel = entriesByLevel.getValue();
 				for (int i = 0; i < scannablesAtLevel.length; i++) {
 					Scannable scn = scannablesAtLevel[i];
 					scn.waitWhileBusy();
 				}
 			}
 		}
 		
 	}
 	
 	TreeMap<Integer, Scannable[]> generateDevicesToMoveByLevel(TreeMap<Integer, Scannable[]> scannableLevels,
 			Vector<Detector> detectors) {
 
 		TreeMap<Integer, Scannable[]> devicesToMoveByLevel = new TreeMap<Integer, Scannable[]>();
 		devicesToMoveByLevel.putAll(scannableLevels);
 
 		for (Scannable detector : detectors) {
 
 			Integer level = detector.getLevel();
 
 			if (devicesToMoveByLevel.containsKey(level)) {
 				Scannable[] levelArray = devicesToMoveByLevel.get(level);
 				levelArray = (Scannable[]) ArrayUtils.add(levelArray, detector);
 				devicesToMoveByLevel.put(level, levelArray);
 			} else {
 				Scannable[] levelArray = new Scannable[] { detector };
 				devicesToMoveByLevel.put(level, levelArray);
 			}
 		}
 		return devicesToMoveByLevel;
 	}
 
 
 	/**
 	 * Asks if the given scannable is part of the array of scannables which this scan is to operate in its moveToStarts
 	 * and moveBySteps methods. If true returns the ScanObject else returns null.
 	 * 
 	 * @param scannable
 	 * @return the ScanObject
 	 */
 	protected ScanObject isScannableToBeMoved(Scannable scannable) {
 		for (ScanObject scanObject : allScanObjects) {
 			if (scanObject.scannable == scannable) {
 				return scanObject;
 			}
 		}
 		return null;
 	}
 	
 	final protected boolean isScannableActuallyToBeMoved(Scannable scannable) {
 		ScanObject scannableToBeMoved = isScannableToBeMoved(scannable);
 		return (scannableToBeMoved) == null ? false : scannableToBeMoved.hasStart();
 	}
 	
 	/*
 	 * Waits until all the scannables of this scan are no longer moving. @throws InterruptedException
 	 */
 	protected void checkAllMovesComplete() throws Exception {
 		for (ScanObject scanObject : allScanObjects) {
 			checkForInterrupts();
 			// only check those objects which we have moved are no longer busy
 			if (scanObject.hasStart()) {
 				scanObject.scannable.waitWhileBusy();
 			}
 		}
 	}
 	
 	@Override
 	protected synchronized void setUp() {
 		super.setUp();
 		// setUp may have removed objects using the Detector interface.
 		// This scan needs its allScanObjects vector to keep to the same order as allScannables // TODO: very dangerous!
 		reorderAllScanObjects();
 	}
 	
 	/*
 	 * Called during instantiation but after the setUp method has been called. The setUp method will have edited and
 	 * reordered the allScannables vector. The allScanObjects vector must now be reordered to keep track of this. In
 	 * doing this, any objects using the Detector interface in the allScanObjects vector will be removed.
 	 */
 	protected void reorderAllScanObjects() {
 		Vector<ScanObject> sortedAllScanObjects = new Vector<ScanObject>();
 		int i = 0;
 		for (Object nextObject : allScannables) {
 			for (ScanObject nextScanObject : allScanObjects) {
 				if (nextScanObject.scannable.equals(nextObject)) {
 					sortedAllScanObjects.add(i, nextScanObject);
 					i++;
 				}
 			}
 		}
 		allScanObjects = sortedAllScanObjects;
 
 		// now save information about which scannables are at each level
 		scannableLevels = new TreeMap<Integer, Scannable[]>();
 
 		// loop through all levels saving the amount of scannables at each level
 		for (Scannable scannable : allScannables) {
 
 			Integer thisLevel = scannable.getLevel();
 
 			if (scannableLevels.containsKey(thisLevel)){
 				Scannable[] levelArray = scannableLevels.get(thisLevel);
 				levelArray = (Scannable[]) ArrayUtils.add(levelArray,scannable);
 				scannableLevels.put(thisLevel, levelArray);
 			} else {
 				Scannable[] levelArray = new Scannable[]{scannable};
 				scannableLevels.put(thisLevel, levelArray);
 			}
 		}
 		
 	}
 
 	private class ReadoutDetector implements Callable<Object> {
 
 		private final Detector detector;
 
 		public ReadoutDetector(Detector detector) {
 			this.detector = detector;
 		}
 
 		@Override
 		public Object call() throws Exception {
 
 			try {
 				return detector.readout();
 			} catch (Exception e) {
 				logger.info("Exeption reading out detector '" + detector.getName() + "': " + representThrowable(e)
 						+ "(first readout exception will be thrown soon from scan thread)");
 				throw e;
 			}
 		}
 
 	}
 
 	/**
 	 * Asynchronously, readout detectors using parallel threads into ScanDataPoint and add to pipeline for possible
 	 * completion and publishing. Call {@link ConcurrentScanChild#waitForDetectorReadoutAndPublishCompletion()} to wait
 	 * for this task to complete, or {@link #cancelReadoutAndPublishCompletion()} to cancel and interrupt it.
 	 * <p>
 	 * If the property {@link LocalProperties#GDA_SCAN_CONCURRENTSCAN_READOUT_CONCURRENTLY} is its default false value
 	 * then simply block while reading out each detector in series and then adding the ScanDataPoint to the pipeline.
 	 * 
 	 * @param point
 	 * @throws Exception
 	 */
 	@Override
 	protected void readoutDetectorsAndPublish(final ScanDataPoint point) throws Exception {
 		
 		final boolean lastPointInLine = (getPointPositionInLine() == PointPositionInLine.LAST); // latch value
 		
 		if (!isReadoutConcurrent()) {
 			super.readoutDetectorsAndPublish(point);
 			return;
 		}
 
 		// Make sure the previous point has read been published
 		// (If the scan contains a detector this method will already have been called)
 		waitForDetectorReadoutAndPublishCompletion();
 
 
 		detectorReadoutTask = new FutureTask<Void>(new Callable<Void>() {
 
 			List<Future<Object>> readoutTasks;
 
 			/**
 			 * Readout each detector in a thread, add the resulting data to the ScanDataPoint and publish.
 			 */
 			@Override
 			public Void call() throws Exception {
 
 				try {
 					Vector<Detector> detectors = point.getDetectors();
 
 					// if there are detectors then readout in parallel threads
 					if (detectors.size() != 0) {
 
 						ExecutorService threadPool = Executors.newFixedThreadPool(detectors.size());
 
 						// Start readout tasks
 						readoutTasks = new ArrayList<Future<Object>>(detectors.size());
 						for (Detector detector : point.getDetectors()) {
 							readoutTasks.add(threadPool.submit(new ReadoutDetector(detector)));
 						}
 
 						// Wait for readout results and put into point
 						for (int i = 0; i < detectors.size(); i++) {
 							if (Thread.interrupted()) {
 								throw new InterruptedException(); // can't trust devices to look for these
 							}
 							checkForInterrupts(); // checks only ScanBase.interupted and may pause
 							Object data = readoutTasks.get(i).get();
 							point.addDetectorData(data, ScannableUtils.getExtraNamesFormats(detectors.get(i)));
 						}
 
 					}
 
 					// Put point onto pipeline
 					checkForInterrupts();
 					scanDataPointPipeline.put(point); // may block
 					checkForInterrupts();
 					
 					// The main scan thread cannot call atPointEnd (and subsequently atPointStart) in the correct order
 					// with respect to readout so call these here instead.
 					
 					for (Detector detector : detectors) {
 						detector.atPointEnd();
 					}
 					
 					
 					// unless this is the last point in the line, call atPointStart hooks for the next point (the one
 					// that main scan thread is now working on.
 					if (! lastPointInLine) {
 						for (Detector detector : detectors) {
 							detector.atPointStart();
 						}
 					}
 
 				} catch (Exception e) {
 					// could be the normal result of cancelling this task
 					// (detector.readout() unfortunately doesn't distinguish InteruptedException from DeviceException
 					logger.info("'" + representThrowable(e)
 							+ "' --- while reading out detectors. *Canceling any remaining readout tasks.*");
 					for (Future<Object> task : readoutTasks) {
 						task.cancel(true);
 					}
 					throw e;
 				}
 				return null;
 
 			}
 
 		});
 		
 		String threadName = "ConcurrentScanChild.readoutDetectorsAndPublish(point '" + point.toString() + "')";
 		new Thread(detectorReadoutTask, threadName).start();
 	}
 
 	/**
 	 * Blocks while detectors are readout and point is added to pipeline. Throws an exception if one was
 	 * thrown while reading out the detectors or adding the point to the pipeline.
 	 */
 	@Override
 	public void waitForDetectorReadoutAndPublishCompletion() throws InterruptedException, ExecutionException {
 		try {
 			if (detectorReadoutTask != null) {
 				detectorReadoutTask.get(); // exceptions, for example from readout, will be thrown here
 			}
 		} catch (InterruptedException e) {
 			cancelReadoutAndPublishCompletion();
 			throw e;
 		} catch (ExecutionException e) {
 			cancelReadoutAndPublishCompletion();
 			throw e;
 		}
 	}
 	
 	/**
 	 * Cancels readout and publish completion task.
 	 */
 	@Override
 	public void cancelReadoutAndPublishCompletion () {
 		if (detectorReadoutTask != null) {
 			detectorReadoutTask.cancel(true);
 		}
 	}
 	
 }
