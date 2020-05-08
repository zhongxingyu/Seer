 /*-
  * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
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
 
 import gda.data.DetectorDataWrapper;
 import gda.data.PlottableDetectorData;
 import gda.data.PlottableDetectorDataClone;
 import gda.data.nexus.tree.NexusTreeProvider;
 import gda.data.scan.datawriter.DataWriter;
 import gda.data.scan.datawriter.DataWriterBase;
 import gda.device.Detector;
 import gda.device.DeviceException;
 import gda.device.Scannable;
 import gda.device.scannable.ScannableUtils;
 import gda.util.Serializer;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.util.map.MapUtils;
 
 /**
  * This class holds information about the data collected at a single point on a scan. It is to be passed around between
  * objects for display \ recording of data.
  */
 public class ScanDataPoint implements Serializable, IScanDataPoint {
 
 	private static final Logger logger = LoggerFactory.getLogger(ScanDataPoint.class);
 
 	/**
 	 * The delimiter used by toString() method.
 	 */
 	public static String delimiter = "\t";
 
 	/**
 	 * The command typed to start the scan that this point is part of.
 	 */
 	private String command = "";
 
 	/**
 	 * The name of the gui panel which initiated this scan. This is used by the local JythonFacade to know which local
 	 * object to distribute this data point to for display.
 	 */
 	private String creatorPanelName = "";
 
 	/**
 	 * The result of calling {@link DataWriter#getCurrentFileName() getCurrentFilename()} on the {@link DataWriter}
 	 * owned by the scan which created this point. Could be used to uniquely identify where this data is being recorded
 	 * by the {@link DataWriter}.
 	 */
 	private String currentFilename = "";
 
 	/**
 	 * The current point number.
 	 */
 	private int currentPointNumber = -1;
 
 	/**
 	 * Vector<Object> of data from the detectors. Each element represents the data from the detector in the
 	 * corresponding element of the 'detectors' vector;
 	 */
 	private Vector<Object> detectorData = new Vector<Object>();
 
 	/**
 	 * The expanded header names for detectors in which the names are composed as detector name plus its element name.
 	 */
 	private String[] detectorHeader = new String[0];
 
 	/**
 	 * The detectors used in the scan.
 	 */
 	private String[] detectorNames = new String[0];
 
 	/**
 	 * The {@link gda.device.Detector} detectors that participate in the scan.
 	 */
 	private transient Vector<Detector> detectors = new Vector<Detector>();
 
 	/**
 	 * Formatting information for the scannable positions - used in the toString method. If an element is "" or null
 	 * then do not format that in the output.
 	 */
 	private String[][] detectorFormats = new String[0][];
 
 	/**
 	 * beamline name
 	 */
 	private String instrument = "";
 
 	/**
 	 * total number of points in the scan
 	 */
 	private int numberOfPoints = -1;
 
 	/**
 	 * The expanded header names for scannable position in which the names are composed of the scannable name plus its
 	 * element name, i.e. scannable's InputNames and ExtraNames
 	 */
 	private String[] scannableHeader = new String[0];
 
 	/**
 	 * The current positions of the scannables. Each element represents the scannable in the corresponding element of
 	 * the 'scannables' vector;
 	 */
 	private Vector<Object> scannablePositions = new Vector<Object>();
 
 	/**
 	 * Formatting information for the scannable positions - used in the toString method.
 	 */
 	private String[][] scannableFormats = new String[0][];
 
 	/**
 	 * The scan identifier, such as the scan number.
 	 */
 	private String scanIdentifier = "";
 
 	/**
 	 * The scannables which are part of the scan.
 	 */
 	private String[] scannableNames = new String[0];
 
 	/**
 	 * The {@link gda.device.Scannable} scannables that participate in the scan.
 	 */
 	private transient Vector<Scannable> scannables = new Vector<Scannable>();
 
 	/**
 	 * Unique identifier for the scan.
 	 */
 	private String uniqueName = "";
 
 	// values useful for plotting tools
 	private List<IScanStepId> stepIds = new Vector<IScanStepId>();
 	private int[] scanDimensions = new int[0];
 	private int numberOfChildScans = 0;
 	private ScanPlotSettings scanPlotSettings;
 	/**
 	 * Is this data point contain child scan
 	 */
 	private boolean hasChild = false;
 
 	/**
 	 * 
 	 */
 	public ScanDataPoint() {
 	}
 
 	/**
 	 * Unpack the ScanData object to fill this sdp rather than using accessor functions.
 	 * <p>
 	 * protected as only intended for use by the scandatapointserver
 	 * 
 	 * @param point
 	 * @param sdpt
 	 */
 	protected ScanDataPoint(ScanData point, ScanDataPointVar sdpt) {
 		uniqueName = point.uniqueName;
 		scannableNames = point.scannableNames;
 		detectorNames = point.detectorNames;
 		scannableHeader = point.scannableHeader;
 		detectorHeader = point.detectorHeader;
 		hasChild = point.hasChild;
 		scanIdentifier = point.scanIdentifier;
 		creatorPanelName = point.creatorPanelName;
 		currentFilename = point.currentFilename;
 		numberOfChildScans = point.numberOfChildScans;
 		instrument = point.instrument;
 		numberOfPoints = point.numberOfPoints;
 		command = point.command;
 		scanPlotSettings = point.scanPlotSettings;
 		scanDimensions = point.scanDimensions;
 
 		currentPointNumber = sdpt.getCurrentPointNumber();
 		detectorFormats = point.detectorFormats;
 		scannableFormats = point.scannableFormats;
 
 		scannablePositions = sdpt.getPositions();
 		detectorData = sdpt.getDetectorData();
 
 		Vector<IScanStepId> stepIds = new Vector<IScanStepId>();
 		if (sdpt.getStepIds() != null) {
 			Object[] dt = (Object[]) Serializer.toObject(sdpt.getStepIds());
 			for (Object obj : dt) {
 				stepIds.add((IScanStepId) obj);
 			}
 		}
 		this.stepIds = stepIds;
 	}
 
 	/**
 	 * An alternative for populating this object. Can be used instead of repeated calls to
 	 * addScannable,addScannablePosition,addDetector,addDetectorData.
 	 * <p>
 	 * Note this makes calls to getPosition() in the scannables and readout() in the detectors.
 	 * 
 	 * @param allScannables
 	 * @param allDetectors
 	 * @throws DeviceException
 	 */
 	@Override
 	public void addScannablesAndDetectors(Vector<Scannable> allScannables, Vector<Detector> allDetectors)
 			throws DeviceException {
 
 		for (Scannable scannable : allScannables) {
 			if (scannable.getOutputFormat().length == 0) {
 				handleZeroInputExtraNameDevice(scannable);
 			} else {
 				this.addScannable(scannable);
 				addPositionFromScannable(scannable);
 			}
 		}
 		for (Detector scannable : allDetectors) {
 			this.addDetector(scannable);
 			addDataFromDetector(scannable);
 		}
 	}
 
 	/**
 	 * Gets a Scannables position and adds it to the data point. Does not ass the Scannable itself.
 	 * 
 	 * @param scannable
 	 * @throws DeviceException
 	 */
 	@Override
 	public void addPositionFromScannable(Scannable scannable) throws DeviceException {
 		this.addScannablePosition(scannable.getPosition(), scannable.getOutputFormat());
 	}
 
 	/**
 	 * Reads data from a detector and adds it to the point. Does not add the detector itself.
 	 * 
 	 * @param detector
 	 * @throws DeviceException
 	 */
 	@Override
 	public void addDataFromDetector(Detector detector) throws DeviceException {
 		Object data = detector.readout();
 		this.addDetectorData(data, ScannableUtils.getExtraNamesFormats(detector));
 	}
 
 	/**
 	 * Call getPosition on the zero-input-extra-names scannable. The zie must return null. This hook is used by some
 	 * Scannables to perform a task in a scan but return nothing.
 	 * 
 	 * @param zie
 	 * @throws DeviceException
 	 */
 	static void handleZeroInputExtraNameDevice(Scannable zie) throws DeviceException {
 		Object zeroInputExtraNameDevicePosition = zie.getPosition();
 		if (zeroInputExtraNameDevicePosition != null) {
 			final String msg = String
 					.format(
 							"Scannable %s has no input or extra names defined. Its getPosition method should return null/None but returned: '%s'.",
 							zie.getName(), zeroInputExtraNameDevicePosition.toString());
 			throw new DeviceException(msg);
 		}
 	}
 
 	@Override
 	public void addScannableWithPosition(Scannable scannable, Object position, String[] format) {
 		this.addScannable(scannable);
 		this.addScannablePosition(position, format);
 	}
 
 	/**
 	 * Add a piece of data to this object. Calls to this method must be made in the same order as calls to addDetector
 	 * to associate the data with the detector.
 	 * <p>
 	 * 
 	 * @param data
 	 */
 	@Override
 	public void addDetectorData(Object data, String[] format) {
		this.detectorData.add(data);
		if (format == null) {
			format = new String[] { "%s" };
 		}
		detectorFormats = (String[][]) ArrayUtils.add(detectorFormats, format);
 	}
 
 	/**
 	 * Replaces the detector data held by the object. The replacement vector must be the same length as the previous for
 	 * this sdp to be self-consistent.
 	 * <p>
 	 * protected as only intended for use by the scandatapointserver
 	 * 
 	 * @param newdata
 	 */
 	protected void setDetectorData(Vector<Object> newdata, String[][] format) {
 		this.detectorData = newdata;
 		detectorFormats = format;
 	}
 
 	/**
 	 * Add a position to the array of positions. Calls to this method must be made in the same order as calls to
 	 * addScannable to associate the array of numbers with the scannable.
 	 * <p>
 	 * It is recommended to call setScannables instead.
 	 * 
 	 * @param data
 	 */
 	@Override
 	public void addScannablePosition(Object data, String[] format) {
 		if (data != null) {
 			scannablePositions.add(data);
 			scannableFormats = (String[][]) ArrayUtils.add(scannableFormats, format);
 		}
 	}
 
 	/**
 	 * Replaces the scannable positions data held by the object. The replacement array must be the same length as the
 	 * previous for this sdp to be self-consistent.
 	 * <p>
 	 * protected as only for use by the scandatapointserver
 	 * 
 	 * @param positions
 	 * @param formats
 	 */
 	protected void setScannablePositions(Vector<Object> positions, String[][] formats) {
 		this.scannablePositions = positions;
 		this.scannableFormats = formats;
 	}
 
 	private String convertStringArrayToString(String[] array) {
 		// Use StringBuilder rather than String addition for speed
 		StringBuilder sb = new StringBuilder();
 		sb.append("");
 
 		boolean first = true;
 		for (String s : array) {
 			if (!first) {
 				sb.append(delimiter);
 			}
 			sb.append(s);
 			first = false;
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Add a detector to the list of detectors this object holds data from. This stores the name in the detectorHeader
 	 * array and detectorNames array. If its a countertimer is stored in the boolean array. The contents of the
 	 * detectorHeader and detectorNames arrays will be different if the detector is a countertimer.
 	 * <p>
 	 * Note this does not readout the detector! Data must be added by using the addData method.
 	 * 
 	 * @param det
 	 */
 	@Override
 	public void addDetector(Detector det) {
 		detectorNames = (String[]) ArrayUtils.add(detectorNames, det.getName());
 		String[] extraNames = det.getExtraNames();
 		if (extraNames != null && extraNames.length > 0) {
 			detectorHeader = (String[]) ArrayUtils.addAll(detectorHeader, extraNames);
 		} else {
 			detectorHeader = (String[]) ArrayUtils.add(detectorHeader, det.getName());
 		}
 		detectors.add(det);
 	}
 
 	/**
 	 * Add a scannable to the list of scannables this object holds data on.
 	 * <p>
 	 * Note that this does not read the current position of the scannable.
 	 * 
 	 * @param scannable
 	 */
 	@Override
 	public void addScannable(Scannable scannable) {
 		scannableNames = (String[]) ArrayUtils.add(scannableNames, scannable.getName());
 		scannableHeader = (String[]) ArrayUtils.addAll(scannableHeader, scannable.getInputNames());
 		scannableHeader = (String[]) ArrayUtils.addAll(scannableHeader, scannable.getExtraNames());
 		scannables.add(scannable);
 	}
 
 	/**
 	 * @return the gda command entered
 	 */
 	@Override
 	public String getCommand() {
 		return command;
 	}
 
 	/**
 	 * Set the name of the panel which requested the scan which created this data point.
 	 * 
 	 * @return String
 	 */
 	@Override
 	public String getCreatorPanelName() {
 		return creatorPanelName;
 	}
 
 	/**
 	 * @return String
 	 */
 	@Override
 	public String getCurrentFilename() {
 		return currentFilename;
 	}
 
 	/**
 	 * @return the current point number
 	 */
 	@Override
 	public int getCurrentPointNumber() {
 		return currentPointNumber;
 	}
 
 	/**
 	 * Return the vector of detector data which this object is a carrier of.
 	 * 
 	 * @return Vector<Object>
 	 */
 	@Override
 	public Vector<Object> getDetectorData() {
 		return detectorData;
 	}
 
 	Double [] allValuesAsDoubles=null;
 	/**
 	 * Returns the values held by this ScanDataPoint of Scannables, Monitors and Detectors.
 	 * 
 	 * @return an array of Double of length getMonitorHeader().size() + getPositionHeader().size() +
 	 *         getDetectorHeader().size() if the conversion of a field to Double is not possible then the element of the
 	 *         array will be null
 	 * @throws IllegalArgumentException
 	 *             if the fields convert to too few values
 	 * @throws IndexOutOfBoundsException
 	 *             if the fields convert to too many values
 	 */
 	@Override
 	public Double[] getAllValuesAsDoubles() throws IllegalArgumentException, IndexOutOfBoundsException {
 		if( allValuesAsDoubles == null){
 			Double[] scannablePosAsDoubles = getPositionsAsDoubles();
 			Double[] detectorDataAsDoubles = getDetectorDataAsDoubles();
 
 			Vector<Double> output = new Vector<Double>();
 			output.addAll(Arrays.asList(scannablePosAsDoubles));
 			output.addAll(Arrays.asList(detectorDataAsDoubles));
 			allValuesAsDoubles = output.toArray(new Double[] {});
 		}
 		return allValuesAsDoubles;
 	}
 
 	/**
 	 * Just returns array of detector data.
 	 * 
 	 * @return all detector data.
 	 */
 	@Override
 	public Double[] getDetectorDataAsDoubles() {
 		Vector<Double> vals = new Vector<Double>();
 		if (getDetectorData() != null) {
 			for (Object data : getDetectorData()) {
 				PlottableDetectorData wrapper = (data instanceof PlottableDetectorData) ? (PlottableDetectorData) data
 						: new DetectorDataWrapper(data);
 				Double[] dvals = wrapper.getDoubleVals();
 				vals.addAll(Arrays.asList(dvals));
 			}
 		}
 		int expectedSize = getDetectorHeader().size();
 		int actualSize = vals.size();
 		if (actualSize != expectedSize) {
 			throw new IllegalArgumentException("Detector data does not hold the expected number of fields actual:" + actualSize + " expected:" + expectedSize);
 		}
 		return vals.toArray(new Double[] {});
 	}
 
 	/**
 	 * returns a vector of expanded detector header string for each data point.
 	 * 
 	 * @return a vector of expanded detector header string for each data point.
 	 */
 	@Override
 	public Vector<String> getDetectorHeader() {
 		return new Vector<String>(Arrays.asList(detectorHeader));
 	}
 
 	/**
 	 * Return the list of names of detectors which this object holds data from.
 	 * 
 	 * @return Vector<String>
 	 */
 	@Override
 	public Vector<String> getDetectorNames() {
 		return new Vector<String>(Arrays.asList(detectorNames));
 	}
 
 	/**
 	 * The list of detectors this object refers to.
 	 * 
 	 * @return list of detectors this object refers to.
 	 */
 	@Override
 	public Vector<Detector> getDetectors() {
 		return detectors;
 	}
 
 	/**
 	 * @return hasChild
 	 */
 	@Override
 	public boolean getHasChild() {
 		return hasChild;
 	}
 
 	/**
 	 * Returns a string whose elements are separated by a mixture of tabs and spaces so that the columns are aligned
 	 * with the output from toString()
 	 * 
 	 * @return String - which could be used in an ascii print out of all the scan points from the same scan
 	 */
 	@Override
 	public String getHeaderString() {
 		return getHeaderString(null);
 	}
 
 	@Override
 	public String getHeaderString(ScanDataPointFormatter dataPointFormatter) {
 		// work out the lengths of the header string and the lengths of each element from the toString method
 		// and pad each to adjust
 
 		String header = getDelimitedHeaderString();
 
 		String data = toDelimitedString();
 
 		String[] headerElements = header.split(delimiter);
 		String[] dataElements = data.split(delimiter);
 
 		if(headerElements.length != dataElements.length )
 			throw new IllegalArgumentException("Number of parts in header '" + headerElements.length + "' != number of parts in data '" + dataElements.length + "'");
 		for (int i = 0; i < headerElements.length; i++) {
 			int headerLength = headerElements[i].trim().length();
 			int dataLength = dataElements[i].trim().length();
 
 			int maxLength = dataLength > headerLength ? dataLength : headerLength;
 			String format = "%" + maxLength + "s";
 
 			headerElements[i] = String.format(format, headerElements[i].trim());
 			dataElements[i] = String.format(format, dataElements[i].trim());
 		}
 
 		if (dataPointFormatter == null || !dataPointFormatter.isValid(this)) {
 			return convertStringArrayToString(headerElements);
 		}
 
 		// Optionally a data formatter may be set on Scan data points.
 		// This formats them in a custom format.
 		return dataPointFormatter.getHeader(this, MapUtils.createLinkedMap(headerElements, dataElements));
 	}
 
 	@Override
 	public String getDelimitedHeaderString() {
 		String header = convertStringArrayToString(scannableHeader);
 		if (detectorHeader.length > 0) {
 			header += delimiter + convertStringArrayToString(detectorHeader);
 		}
 		return header.trim();
 	}
 
 	private String createStringFromPositions() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("");
 		int i = 0;
 		for (Object position : scannablePositions) {
 			String[] thisPosition;
 			try {
 				thisPosition = ScannableUtils.getFormattedCurrentPositionArray(position, scannableFormats[i].length,
 						scannableFormats[i]);
 				for (String part : thisPosition) {
 					sb.append(part);
 					sb.append(delimiter);
 				}
 			} catch (Exception e) {
 				// ignore so will get a truncated string
 			}
 			i++;
 		}
 		return sb.toString().trim();
 	}
 
 	private String createStringFromDetectorData() {
 		StringBuilder sb = new StringBuilder("");
 		int i = 0;
 		for (Object dataItem : this.detectorData) {
 
 			if (dataItem instanceof String || dataItem instanceof NexusTreeProvider
 					|| dataItem instanceof PlottableDetectorDataClone) {
 				sb.append(dataItem.toString());
 				sb.append(delimiter);
 			}
 			/*
 			 * else use the first format if it is not empty
 			 */
 			else if (this.detectorFormats[i] != null && this.detectorFormats[i].length > 0
 					&& this.detectorFormats[i][0] != null && !this.detectorFormats[i][0].isEmpty()) {
 				String[] thisPosition;
 				try {
 					thisPosition = ScannableUtils.getFormattedCurrentPositionArray(dataItem, detectorFormats[i].length,
 							detectorFormats[i]);
 					for (String part : thisPosition) {
 						sb.append(part);
 						sb.append(delimiter);
 					}
 				} catch (Exception e) {
 					logger.error("Error getting position", e);
 				}
 			}
 			/*
 			 * else give up and get detector to return the string
 			 */
 			else {
 				sb.append(DataWriterBase.getDetectorData(dataItem, false));
 				sb.append(delimiter);
 			}
 			i++;
 		}
 		return sb.toString().trim();
 
 	}
 
 	/**
 	 * @return String
 	 */
 	@Override
 	public String getInstrument() {
 		return instrument;
 	}
 
 	/**
 	 * @return Vector<String>
 	 */
 	@Override
 	public Vector<String> getNames() {
 		Vector<String> allNames = new Vector<String>();
 		allNames.addAll(getScannableNames());
 		allNames.addAll(getDetectorNames());
 		return allNames;
 	}
 
 	/**
 	 * @return int
 	 */
 	@Override
 	public int getNumberOfChildScans() {
 		return numberOfChildScans;
 	}
 
 	/**
 	 * @return int
 	 */
 	@Override
 	public int getNumberOfPoints() {
 		return numberOfPoints;
 	}
 
 	/**
 	 * @return Vector<String>
 	 */
 	@Override
 	public Vector<String> getPositionHeader() {
 		return new Vector<String>(Arrays.asList(scannableHeader));
 	}
 
 	/**
 	 * @return Vector<String>
 	 */
 	@Override
 	public Vector<Object> getPositions() {
 		return scannablePositions;
 	}
 
 	/**
 	 * @return String
 	 */
 	@Override
 	public String getScanIdentifier() {
 		return scanIdentifier;
 	}
 
 	/**
 	 * @return Vector<String>
 	 */
 	@Override
 	public Vector<String> getScannableNames() {
 		return new Vector<String>(Arrays.asList(scannableNames));
 	}
 
 	/**
 	 * Just returns array of positions. Strings will be an empty element.
 	 * 
 	 * @return all scannable positions.
 	 */
 	@Override
 	public Double[] getPositionsAsDoubles() {
 		Vector<Double> vals = new Vector<Double>();
 		if (getPositions() != null) {
 			for (Object data : getPositions()) {
 				PlottableDetectorData wrapper = new DetectorDataWrapper(data);
 				Double[] dvals = wrapper.getDoubleVals();
 				vals.addAll(Arrays.asList(dvals));
 			}
 		}
 		if (vals.size() != getPositionHeader().size()) {
 			throw new IllegalArgumentException("Position data does not hold the expected number of fields");
 		}
 		return vals.toArray(new Double[] {});
 	}
 
 	/**
 	 * @return all Scannable positions as strings using the given format
 	 */
 	@Override
 	public String[] getPositionsAsFormattedStrings() {
 
 		String[] strings = new String[0];
 		if (getPositions() != null) {
 			int index = 0;
 			for (Object data : getPositions()) {
 				PlottableDetectorData wrapper = new DetectorDataWrapper(data);
 				Double[] dvals = wrapper.getDoubleVals();
 				String[] formattedVals = new String[dvals.length];
 				String[] formats = this.scannableFormats[index];
 				for (int j = 0; j < dvals.length; j++) {
 					formattedVals[j] = String.format(formats[j], dvals[j]);
 				}
 				strings = (String[]) ArrayUtils.addAll(strings, formattedVals);
 				index++;
 			}
 		}
 		if (strings.length != getPositionHeader().size()) {
 			throw new IllegalArgumentException("Position data does not hold the expected number of fields");
 		}
 		return strings;
 	}
 
 	/**
 	 * The list of scannables this object refers to.
 	 * 
 	 * @return list of scannables this object refers to.
 	 */
 	@Override
 	public Vector<Scannable> getScannables() {
 		return scannables;
 	}
 
 	/**
 	 * @return ScanPlotSettings
 	 */
 	@Override
 	public ScanPlotSettings getScanPlotSettings() {
 		return scanPlotSettings;
 	}
 
 	/**
 	 * @param scanPlotSettings
 	 *            The scanPlotSettings to set.
 	 */
 	@Override
 	public void setScanPlotSettings(ScanPlotSettings scanPlotSettings) {
 		this.scanPlotSettings = scanPlotSettings;
 	}
 
 	/**
 	 * @return Returns the scanDimensions.
 	 */
 	@Override
 	public int[] getScanDimensions() {
 		return scanDimensions;
 	}
 
 	/**
 	 * @param scanDimensions
 	 *            The scanDimensions to set.
 	 */
 	@Override
 	public void setScanDimensions(int[] scanDimensions) {
 		this.scanDimensions = scanDimensions;
 	}
 
 	/**
 	 * Unique information about this point.
 	 */
 	@Override
 	public String toString() {
 		
 		String identifier = getCurrentFilename() != null ?  getCurrentFilename() : uniqueName;
 		return "point " + (currentPointNumber + 1) + " of " + numberOfPoints + " for scan " + identifier;
 	}
 
 	/**
 	 * Returns a string whose elements are separated by a mixture of tabs and spaces so that the columns are aligned
 	 * with the output from getHeaderString().
 	 * <p>
 	 * To be used to create an ascii version of the data held by this object for priting to terminals or to ascii files.
 	 */
 	@Override
 	public String toFormattedString() {
 		return toFormattedString(null);
 	}
 
 	@Override
 	public String toFormattedString(ScanDataPointFormatter dataPointFormatter) {
 		// work out the lengths of the header string and the lengths of each element from the toString method
 		// and pad each to adjust
 
 		String header = getDelimitedHeaderString();
 
 		String data = toDelimitedString();
 
 		String[] headerElements = header.split(delimiter);
 		String[] dataElements = data.split(delimiter);
 
 		for (int i = 0; i < headerElements.length; i++) {
 			int headerLength = headerElements[i].trim().length();
 			int dataLength = dataElements[i].trim().length();
 
 			int maxLength = dataLength > headerLength ? dataLength : headerLength;
 			String format = "%" + maxLength + "s";
 
 			headerElements[i] = String.format(format, headerElements[i].trim());
 			dataElements[i] = String.format(format, dataElements[i].trim());
 		}
 
 		if (dataPointFormatter == null || !dataPointFormatter.isValid(this)) {
 			return convertStringArrayToString(dataElements);
 		}
 
 		// Optionally a data formatter may be set on Scan data points.
 		// This formats them in a custom format.
 		return dataPointFormatter.getData(this, MapUtils.createLinkedMap(headerElements, dataElements));
 	}
 
 	String delimitedString=null;
 
 	/**
 	 * Returns a string of the information held by this object delimited by the static variable.
 	 * 
 	 * @return String
 	 */
 	@Override
 	public String toDelimitedString() {
 		if( delimitedString == null){
 			StringBuilder sb = new StringBuilder(createStringFromPositions());
 			sb.append(delimiter);
 			sb.append(createStringFromDetectorData());
 			delimitedString = sb.toString().trim();
 		}
 		return delimitedString;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + currentPointNumber;
 		result = prime * result + ((stepIds == null) ? 0 : stepIds.hashCode());
 		result = prime * result + ((uniqueName == null) ? 0 : uniqueName.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ScanDataPoint other = (ScanDataPoint) obj;
 		if (currentPointNumber != other.currentPointNumber)
 			return false;
 		if (stepIds == null) {
 			if (other.stepIds != null)
 				return false;
 		} else if (!stepIds.equals(other.stepIds))
 			return false;
 		if (uniqueName == null) {
 			if (other.uniqueName != null)
 				return false;
 		} else if (!uniqueName.equals(other.uniqueName))
 			return false;
 		return true;
 	}
 
 	/**
 	 * @return Returns the stepIds.
 	 */
 	@Override
 	public List<IScanStepId> getStepIds() {
 		return stepIds;
 	}
 
 	/**
 	 * @param stepIds
 	 *            The stepIds to set.
 	 */
 	@Override
 	public void setStepIds(List<IScanStepId> stepIds) {
 		this.stepIds = stepIds;
 	}
 
 	/**
 	 * @return Returns the uniqueName.
 	 */
 	@Override
 	public String getUniqueName() {
 		return uniqueName;
 	}
 
 	/**
 	 * @param uniqueName
 	 *            The uniqueName to set.
 	 */
 	@Override
 	public void setUniqueName(String uniqueName) {
 		this.uniqueName = uniqueName;
 	}
 
 	/**
 	 * @param command
 	 *            The command to set.
 	 */
 	@Override
 	public void setCommand(String command) {
 		this.command = command;
 	}
 
 	/**
 	 * @param creatorPanelName
 	 *            The creatorPanelName to set.
 	 */
 	@Override
 	public void setCreatorPanelName(String creatorPanelName) {
 		this.creatorPanelName = creatorPanelName;
 	}
 
 	/**
 	 * @param currentFilename
 	 *            The currentFilename to set.
 	 */
 	@Override
 	public void setCurrentFilename(String currentFilename) {
 		this.currentFilename = currentFilename;
 	}
 
 	/**
 	 * @param currentPointNumber
 	 *            The currentPointNumber to set.
 	 */
 	@Override
 	public void setCurrentPointNumber(int currentPointNumber) {
 		this.currentPointNumber = currentPointNumber;
 	}
 
 	/**
 	 * @param hasChild
 	 *            The hasChild to set.
 	 */
 	@Override
 	public void setHasChild(boolean hasChild) {
 		this.hasChild = hasChild;
 	}
 
 	/**
 	 * @param instrument
 	 *            The instrument to set.
 	 */
 	@Override
 	public void setInstrument(String instrument) {
 		this.instrument = instrument;
 	}
 
 	/**
 	 * @param numberOfChildScans
 	 *            The numberOfChildScans to set.
 	 */
 	@Override
 	public void setNumberOfChildScans(int numberOfChildScans) {
 		this.numberOfChildScans = numberOfChildScans;
 	}
 
 	/**
 	 * @param numberOfPoints
 	 *            The numberOfPoints to set.
 	 */
 	@Override
 	public void setNumberOfPoints(int numberOfPoints) {
 		this.numberOfPoints = numberOfPoints;
 	}
 
 	/**
 	 * @param scanIdentifier
 	 *            The scanIdentifier to set.
 	 */
 	@Override
 	public void setScanIdentifier(String scanIdentifier) {
 		this.scanIdentifier = scanIdentifier;
 	}
 
 	@Override
 	public String[][] getScannableFormats() {
 		return scannableFormats;
 	}
 
 	@Override
 	public void setScannableFormats(String[][] scannableFormats) {
 		this.scannableFormats = scannableFormats;
 	}
 
 	@Override
 	public void setDetectorHeader(String[] detectorHeader) {
 		this.detectorHeader = detectorHeader;
 	}
 
 	@Override
 	public String[] getScannableHeader() {
 		return scannableHeader;
 	}
 
 	@Override
 	public void setScannableHeader(String[] scannableHeader) {
 		this.scannableHeader = scannableHeader;
 	}
 
 	@Override
 	public Vector<Object> getScannablePositions() {
 		return scannablePositions;
 	}
 
 	@Override
 	public void setScannablePositions(Vector<Object> scannablePositions) {
 		this.scannablePositions = scannablePositions;
 	}
 
 	@Override
 	public String[][] getDetectorFormats() {
 		return detectorFormats;
 	}
 
 	@Override
 	public void setDetectorFormats(String[][] detectorFormats) {
 		this.detectorFormats = detectorFormats;
 	}
 
 	/**
 	 * Searches the scannables for one of a given name. Used to avoid searches being in many places.
 	 * 
 	 * @param name
 	 * @return Scannable if it exists or null
 	 */
 	@Override
 	public Scannable getScannable(final String name) {
 		final Iterator<Scannable> it = getScannables().iterator();
 		while (it.hasNext()) {
 			final Scannable s = it.next();
 			if (s.getName().equals(name))
 				return s;
 		}
 		return null;
 	}
 
 	/**
 	 * Searches the scannables for one of a given name. Works for scannables where name is declared and the actual
 	 * scannable is not sent over.
 	 * 
 	 * @param name
 	 * @return true if it exists or false
 	 */
 	@Override
 	public boolean isScannable(final String name) {
 		final Iterator<String> it = getScannableNames().iterator();
 		while (it.hasNext()) {
 			final String n = it.next();
 			if (n.equals(name))
 				return true;
 		}
 		return getScannable(name) != null;
 	}
 
 	/**
 	 * Searches the detectors for one of a given name. Used to avoid searches being in many places.
 	 * 
 	 * @param name
 	 * @return Detector if it exists or null
 	 */
 	@Override
 	public Detector getDetector(final String name) {
 		final Iterator<Detector> it = getDetectors().iterator();
 		while (it.hasNext()) {
 			final Detector s = it.next();
 			if (s.getName().equals(name))
 				return s;
 		}
 		return null;
 	}
 
 	/**
 	 * Searches the detectors for one of a given name. Works for detectors where name is declared and the actual
 	 * detector is not sent over.
 	 * 
 	 * @param name
 	 * @return true if it exists or false
 	 */
 	@Override
 	public boolean isDetector(String name) {
 		final Iterator<String> it = getDetectorNames().iterator();
 		while (it.hasNext()) {
 			final String n = it.next();
 			if (n.equals(name))
 				return true;
 		}
 		return getDetector(name) != null;
 	}
 }
