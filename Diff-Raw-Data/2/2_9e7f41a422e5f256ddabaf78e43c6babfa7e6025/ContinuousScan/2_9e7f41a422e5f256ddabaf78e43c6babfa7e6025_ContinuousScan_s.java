 /*-
  * Copyright © 2010 Diamond Light Source Ltd.
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
 
 import static gda.jython.InterfaceProvider.getJythonServerNotifer;
 import static gda.scan.ScanDataPoint.handleZeroInputExtraNameDevice;
 import gda.device.ContinuousParameters;
 import gda.device.Detector;
 import gda.device.DeviceException;
 import gda.device.Scannable;
 import gda.device.detector.BufferedDetector;
 import gda.device.scannable.ContinuouslyScannable;
 import gda.device.scannable.ScannableUtils;
 import gda.jython.InterfaceProvider;
 
 import java.util.HashMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Performs a continuous scan using Histogram detectors. NOTE: bypasses the ScanDataPointPipeline mechanism.
  * <p>
  * This extends ConcurrentScan so it can be a child scan of a ConcurrentScan and effectively be another dimension in a
  * multi-dimensional scan.
  * <p>
  * This will not operate any child scans, so must be the lowest dimension in a n-dimensional scan.
  */
 public class ContinuousScan extends ConcurrentScanChild {
 
 	private static final Logger logger = LoggerFactory.getLogger(ContinuousScan.class);
 	private ContinuouslyScannable qscanAxis;
 	private Double start;
 	private Double stop;
 	private Double time;
 	private Integer numberScanpoints;
 	private BufferedDetector[] qscanDetectors;
 
 	public ContinuousScan() {
 		super();
 		setMustBeFinal(true);
 	}
 
 	public ContinuousScan(ContinuouslyScannable energyScannable, Double start, Double stop, Integer numberPoints,
 			Double time, BufferedDetector[] detectors) {
 		setMustBeFinal(true);
 		allScannables.add(energyScannable);
 		double step = (stop - start) / (numberPoints - 1);
 		ImplicitScanObject firstScanObject = new ImplicitScanObject(energyScannable, start, stop, step);
 		firstScanObject.calculateScanPoints();
 		allScanObjects.add(firstScanObject);
 		qscanAxis = energyScannable;
 		this.start = start;
 		this.stop = stop;
 		this.numberScanpoints = numberPoints;
 		this.time = time;
 		qscanDetectors = detectors;
 		for (Detector det : detectors)
 			allDetectors.add(det);
 		super.setUp();
 	}
 
 	@Override
 	public boolean isReadoutConcurrent() {
 		return false; // should be false even if enabled for beamline
 	}
 
 	@Override
 	public int getDimension() {
 		return numberScanpoints;
 	}
 
 	/**
 	 * This method is used by the scan base class when preparing for the start of the scan.
 	 * <p>
 	 * This needs to be overriden by this class to prevent operation of the continuous scannable, but still enable other
 	 * Scannables in the scan to be setup for the scan. This is especially useful for when a continuous scan is part of
 	 * a multi-dimensional scan.
 	 */
 	@Override
 	protected ScanObject isScannableToBeMoved(Scannable scannable) {
 		if (scannable == qscanAxis)
 			return null;
 		return super.isScannableToBeMoved(scannable);
 	}
 
 	@Override
 	public void doCollection() throws Exception {
 		InterfaceProvider.getTerminalPrinter().print("Continuous Scan doCollection");
 		acquirePoint(true, false);
 		ContinuousParameters params = new ContinuousParameters();
 		params.setStartPosition(start);
 		params.setEndPosition(stop);
 		params.setNumberDataPoints(numberScanpoints);
 		params.setTotalTime(time);
 		params.setContinuouslyScannableName(qscanAxis.getName());
 		qscanAxis.setContinuousParameters(params);
 
 		// prepare the hardware for the continuous move and revise the number of scans points to the actual number which
 		// the hardware will do.
 		// I18 had a negative number of pulses in epics. Though theres no such thing, It is working and should be
 		// handled
 		qscanAxis.prepareForContinuousMove();
 		numberScanpoints = Math.abs(qscanAxis.getNumberOfDataPoints());
 
 		params.setNumberDataPoints(numberScanpoints);
 		super.setTotalNumberOfPoints(numberScanpoints);
 
 		// prep the detectors
 		for (BufferedDetector detector : qscanDetectors) {
 			detector.clearMemory();
 			detector.setContinuousParameters(params);
 			detector.setContinuousMode(true);
 		}
 
 		// for performance, see how many frames to read at any one time
 		int maxFrameRead = getMaxFrameRead();
 
 		// wait for the scannable to lined up the move to stop in another thread
 		qscanAxis.waitWhileBusy();
 		checkForInterruptsIgnoreIdle();
 		if (!isChild())
 			currentPointCount = -1;
 		qscanAxis.performContinuousMove();
 
 		// now readout and convert each point to a regular sdp to give it to the datahandler
 		int highestFrameNumberRead = -1;
 
 		try {
 			while (qscanAxis.isBusy() && highestFrameNumberRead < numberScanpoints - 1) {
 				// sleep for a second. For what reason?
 				Thread.sleep(1000);
 				checkForInterruptsIgnoreIdle();
 				// get lowest number of frames from all detectors
 				int framesReachedArray[] = new int[qscanDetectors.length];
 				fillArray(framesReachedArray, highestFrameNumberRead);
 				int frameNumberReached = highestFrameNumberRead;
 				for (int k = 0; k < qscanDetectors.length; k++) {
 					try {
 						int thisNumberFrames = qscanDetectors[k].getNumberFrames();
 						if (thisNumberFrames - 1 > framesReachedArray[k]) {
 							framesReachedArray[k] = thisNumberFrames - 1;
 						}
 						logger.debug("Frame number for  " + qscanDetectors[k].getName() + " " + framesReachedArray[k]);
 					} catch (DeviceException e) {
 						logger.warn("Problem getting number of frames from TFG.");
 					}
 				}
 				frameNumberReached = findLowest(framesReachedArray);
 				logger.debug("the lowest frame of all the detectors is " + frameNumberReached);
 				// do not collect more than 20 frames at any one time
 				if (frameNumberReached - highestFrameNumberRead > maxFrameRead) {
 					frameNumberReached = highestFrameNumberRead + maxFrameRead;
 				}
 				// get data from detectors for that frame and create an sdp and send it out
 				if (frameNumberReached > -1 && frameNumberReached > highestFrameNumberRead) {
 					logger.info("about to createDataPoints " + (highestFrameNumberRead + 1) + " " + frameNumberReached
 							+ " " + qscanAxis.isBusy());
 					createDataPoints(highestFrameNumberRead + 1, frameNumberReached);
 				}
 
 				highestFrameNumberRead = frameNumberReached;
 				logger.info("number of frames completed:" + new Integer(frameNumberReached + 1));
 
 			}
 
 		} catch (InterruptedException e) {
 			// scan has been aborted, so stop the motion and let the scan write out the rest of the data point which
 			// have been collected so far
 			qscanAxis.stop();
 			qscanAxis.atCommandFailure();
 			throw e;
 
 		}
 
 		// make sure axis has stopped. otherwise next repetition will set things while the axis is moving
 		while (qscanAxis.isBusy())
 			Thread.sleep(100);
 
 		// have we read all the frames?
 		if (highestFrameNumberRead == numberScanpoints - 2)
 			return;
 
 		// collect the rest of the frames and send the resulting sdp's out
 		while (highestFrameNumberRead < numberScanpoints - 1) {
 			int nextFramesetEnd = highestFrameNumberRead + maxFrameRead;
 			if (nextFramesetEnd > numberScanpoints - 1)
 				nextFramesetEnd = numberScanpoints - 1;
 			createDataPoints(highestFrameNumberRead + 1, nextFramesetEnd);
 			highestFrameNumberRead = nextFramesetEnd;
 		}
 	}
 
 	private int findLowest(int[] framesReachedArray) {
 		int lowest = framesReachedArray[0];
 		for (int i = 0; i < framesReachedArray.length; i++) {
 			if (framesReachedArray[i] < lowest)
 				lowest = framesReachedArray[i];
 		}
 		return lowest;
 	}
 
 	private void fillArray(int[] framesReachedArray, int highestFrameNumberRead) {
 		for (int i = 0; i < framesReachedArray.length; i++)
 			framesReachedArray[i] = highestFrameNumberRead;
 	}
 
 	private int getMaxFrameRead() throws DeviceException {
 		int smallestFrameLimit = Integer.MAX_VALUE;
 		for (BufferedDetector detector : qscanDetectors) {
 			int thisDetMax = detector.maximumReadFrames();
 			if (thisDetMax < smallestFrameLimit)
 				smallestFrameLimit = thisDetMax;
 		}
 		return smallestFrameLimit;
 	}
 
 	@Override
 	protected void endScan() throws DeviceException {
 		try {
 			qscanAxis.continuousMoveComplete();
 		} finally {
 			// always stop the detectors and end the scan (which will stop the qscanAxis too)
 			qscanAxis.stop();
 			for (BufferedDetector detector : qscanDetectors) {
 				logger.info("Stopping detector: " + detector.getName());
 				detector.stop();
 				detector.setContinuousMode(false);
 			}
 			super.endScan();
 		}
 	}
 
 	@Override
 	public String getCommand() {
 		if (command == null || command.equals("")) {
 			command = qscanAxis.getName() + " " + start + " " + stop + " " + numberScanpoints + " " + time;
 			for (BufferedDetector detector : qscanDetectors)
 				command += " " + detector.getName();
 		}
 		return command;
 	}
 
 	@Override
 	public int getTotalNumberOfPoints() {
 		if (!isChild())
 			return numberScanpoints;
 		return getParent().getTotalNumberOfPoints();
 	}
 
 	/**
 	 * @param lowFrame
 	 *            - where 0 is the first frame
 	 * @param highFrame
 	 *            - where number scan points -1 is the last frame
 	 * @throws Exception
 	 */
 	private void createDataPoints(int lowFrame, int highFrame) throws Exception {
 		// readout the correct frame from the detectors
 		HashMap<String, Object[]> detData = new HashMap<String, Object[]>();
 		logger.info("reading data from detectors from frames " + lowFrame + " to " + highFrame);
 		try {
 			for (BufferedDetector detector : qscanDetectors) {
 				Object[] data = detector.readFrames(lowFrame, highFrame);
 				detData.put(detector.getName(), data);
 			}
 		} catch (DeviceException e1) {
 			throw new DeviceException("Exception while reading out frames " + lowFrame + " to " + highFrame + ": "
 					+ e1.getMessage(), e1);
 		}
 		logger.info("data read successfully");
 
 		// thisFrame <= highFrame. this was thisFrame < highFrame which caused each frame to lose a point at the end
 		for (int thisFrame = lowFrame; thisFrame <= highFrame; thisFrame++) {
 			checkForInterruptsIgnoreIdle();
 			currentPointCount++;
 			this.stepId = new ScanStepId(qscanAxis.getName(), currentPointCount);
 			ScanDataPoint thisPoint = new ScanDataPoint();
 			thisPoint.setUniqueName(name);
 			thisPoint.setCurrentFilename(getDataWriter().getCurrentFileName());
 			thisPoint.setStepIds(getStepIds());
 			thisPoint.setScanPlotSettings(getScanPlotSettings());
 			thisPoint.setScanDimensions(getDimensions());
 			thisPoint.setNumberOfPoints(numberScanpoints);
 
 			// add the scannables. For the qscanAxis scannable calculate the position.
 			double stepSize = (stop - start) / (numberScanpoints - 1);
 
 			for (Scannable scannable : allScannables) {
 				if (scannable.equals(qscanAxis)) {
 					thisPoint.addScannable(qscanAxis);
 					try {
 						thisPoint.addScannablePosition(qscanAxis.calculateEnergy(thisFrame),
 								qscanAxis.getOutputFormat());
 					} catch (DeviceException e) {
 						thisPoint.addScannablePosition(start + (thisFrame - 1) * stepSize, qscanAxis.getOutputFormat());
 					}
 				} else {
 					if (scannable.getOutputFormat().length == 0)
 						handleZeroInputExtraNameDevice(scannable);
 					else {
 						thisPoint.addScannable(scannable);
 						thisPoint.addScannablePosition(scannable.getPosition(), scannable.getOutputFormat());
 					}
 				}
 
 			}
 			// readout the correct frame from the detectors
 			for (BufferedDetector detector : qscanDetectors) {
 				Object data = detData.get(detector.getName())[thisFrame - lowFrame];
 				if (data != null) {
 					thisPoint.addDetector(detector);
 					thisPoint.addDetectorData(data, ScannableUtils.getExtraNamesFormats(detector));
 				}
 			}
 
 			// Set some parameters in the data point.
 			// (This is implemented as setters at the moment, as I didn't want to risk changing the constructor
 			// statement above and risk breaking the scanning system!)
 			thisPoint.setCurrentPointNumber(this.currentPointCount);
 			thisPoint.setInstrument(instrument);
 			thisPoint.setCommand(getCommand());
 			setScanIdentifierInScanDataPoint(thisPoint);
 
 			// then write data to data handler
 			getDataWriter().addData(thisPoint);
 
 			checkForInterruptsIgnoreIdle();
 
 			// update the filename (if this was the first data point and so
 			// filename would never be defined until first data added
 			thisPoint.setCurrentFilename(getDataWriter().getCurrentFileName());
 
 			// then notify IObservers of this scan (e.g. GUI panels)
			getJythonServerNotifer().notifyServer(this, point);
 			// TODO might want to allow for PositionCallableProviders and add points to the ScanDataPointPipeline
 			// instead of simply calling notifyServer here
 
 		}
 	}
 
 }
