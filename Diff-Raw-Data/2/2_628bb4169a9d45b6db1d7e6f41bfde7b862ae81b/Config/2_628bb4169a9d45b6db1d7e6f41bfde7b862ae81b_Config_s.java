 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
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
 
 package gda.gui.scanplot;
 
 import gda.scan.IScanDataPoint;
 import gda.scan.ScanPlotSettings;
 
 import java.util.Vector;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Config {
 	private static final Logger logger = LoggerFactory.getLogger(Config.class);
 	int xAxisIndex;
 	String xAxisHeader;
 	Vector<ConfigLine> linesToAdd;
 	String id = "";
 	ScanPlotSettings scanPlotSettings;
 	int numberOfScannables;
 	int numberOfDetectors;
 	int numberofChildScans;
 	Double[] initialDataAsDoubles;
 
 	boolean isValid(IScanDataPoint pt) {
 		return id.equals(pt.getUniqueName());
 	}
 
 	boolean UsePreviousScanLineSetting(Config prevConfig, int xAxisIndex, IScanDataPoint point,
 			Vector<String> namesOfVisibleLinesInPreviousScan, Vector<String> namesOfInVisibleLinesInPreviousScan) {
		if( !scanPlotSettings.isAllowUseOfPreviousScanSettings())
 			return false;
 		if (namesOfVisibleLinesInPreviousScan == null || namesOfVisibleLinesInPreviousScan.size() == 0
 				|| namesOfInVisibleLinesInPreviousScan == null) {
 			return false;
 		}
 		if (prevConfig == null)
 			return false;
 		if (prevConfig.xAxisIndex != xAxisIndex)
 			return false;
 		if (prevConfig.xAxisHeader == null)
 			return false;
 		if (!prevConfig.xAxisHeader.equals(xAxisHeader))
 			return false;
 		if (prevConfig.numberofChildScans != numberofChildScans)
 			return false;
 		if (prevConfig.numberOfScannables != numberOfScannables)
 			return false;
 		if (prevConfig.numberOfDetectors != numberOfDetectors)
 			return false;
 		if (prevConfig.scanPlotSettings != null) {
 			if (!prevConfig.scanPlotSettings.equals(scanPlotSettings)) {
 				return false;
 			}
 		}
 		Vector<String> positionHeader = point.getPositionHeader();
 		Vector<String> detectorHeader = point.getDetectorHeader();
 		for (String s : namesOfVisibleLinesInPreviousScan) {
 			if (!positionHeader.contains(s) && !detectorHeader.contains(s))
 				return false;
 		}
 		for (String s : namesOfInVisibleLinesInPreviousScan) {
 			if (!positionHeader.contains(s) && !detectorHeader.contains(s))
 				return false;
 		}
 		return true;
 	}
 
 	public Config(Config prevConfig, IScanDataPoint point, Vector<String> namesOfVisibleLinesInPreviousScan,
 			Vector<String> namesOfInVisibleLinesInPreviousScan) {
 		numberOfScannables = point.getPositionHeader().size();
 		numberofChildScans = point.getNumberOfChildScans();
 		numberOfDetectors = point.getDetectorNames().size();
 		scanPlotSettings = point.getScanPlotSettings();
 		String[] pointyAxesShown = null;
 		String[] pointyAxesNotShown = null;
 		if (scanPlotSettings != null) {
 			xAxisHeader = scanPlotSettings.getXAxisName();
 
 		}
 
 		xAxisIndex = 0;
 		if (xAxisHeader == null) {
 			if (point.getHasChild()) {
 				xAxisIndex = numberofChildScans;
 			}
 			Vector<String> positionHeader = point.getPositionHeader();
 			if (positionHeader.isEmpty()) {
 				id = point.getUniqueName();
 				return; // do not plot anything
 			}
 			xAxisHeader = positionHeader.get(xAxisIndex);
 		} else {
 			if (xAxisHeader.isEmpty()) {
 				id = point.getUniqueName();
 				return; // do not plot anything
 			}
 			xAxisIndex = point.getPositionHeader().indexOf(xAxisHeader);
 		}
 
 		if (UsePreviousScanLineSetting(prevConfig, xAxisIndex, point, namesOfVisibleLinesInPreviousScan,
 				namesOfInVisibleLinesInPreviousScan)) {
 			Vector<String> axesNotToShow = new Vector<String>();
 			for (String s : namesOfInVisibleLinesInPreviousScan) {
 				axesNotToShow.add(s);
 			}
 			if (scanPlotSettings != null) {
 				if (scanPlotSettings.getYAxesNotShown() != null) {
 					for (String name : scanPlotSettings.getYAxesNotShown()) {
 						if (!namesOfVisibleLinesInPreviousScan.contains(name))
 							axesNotToShow.add(name);
 					}
 				}
 				if (scanPlotSettings.getYAxesShown() != null) {
 					for (String name : scanPlotSettings.getYAxesShown()) {
 						if (!namesOfVisibleLinesInPreviousScan.contains(name))
 							axesNotToShow.add(name);
 					}
 				}
 			}
 			pointyAxesShown = namesOfVisibleLinesInPreviousScan.toArray(new String[] {});
 			pointyAxesNotShown = axesNotToShow.toArray(new String[] {});
 		} else {
 			if (scanPlotSettings != null) {
 				pointyAxesShown = scanPlotSettings.getYAxesShown();
 				pointyAxesNotShown = scanPlotSettings.getYAxesNotShown();
 			}
 		}
 
 		linesToAdd = new Vector<ConfigLine>();
 		int index = 0;
 		Vector<String> yAxesShown = null;
 		if (pointyAxesShown != null) {
 			yAxesShown = new Vector<String>();
 			for (String yAxis : pointyAxesShown) {
 				yAxesShown.add(yAxis);
 			}
 		}
 		Vector<String> yAxesNotShown = null;
 		if (pointyAxesNotShown != null) {
 			yAxesNotShown = new Vector<String>();
 			for (String yAxis : pointyAxesNotShown) {
 				yAxesNotShown.add(yAxis);
 			}
 		}
 
 		initialDataAsDoubles = point.getAllValuesAsDoubles();
 		if (initialDataAsDoubles[xAxisIndex] != null) {
 			for (int j = 0; j < numberOfScannables; j++, index++) {
 				addIfWanted(linesToAdd, initialDataAsDoubles[index], yAxesShown, yAxesNotShown, point
 						.getPositionHeader().get(j), index, xAxisIndex);
 			}
 			for (int j = 0; j < point.getDetectorHeader().size(); j++, index++) {
 				addIfWanted(linesToAdd, initialDataAsDoubles[index], yAxesShown, yAxesNotShown, point
 						.getDetectorHeader().get(j), index, xAxisIndex);
 			}
 		} else {
 			logger.warn("xAxis is not plottable for scan " + point.getUniqueName());
 			xAxisHeader = "";
 		}
 		id = point.getUniqueName();
 	}
 
 	private void addIfWanted(Vector<ConfigLine> linesToAdd, Double val, Vector<String> yAxesShown,
 			Vector<String> yAxesNotShown, String name, int index, int xAxisIndex) {
 		// do not add a line if we are unable to convert the string representation to a double
 		if (val == null)
 			return;
 		if (index != xAxisIndex) {
 			if (yAxesShown == null || yAxesShown.contains(name)) {
 				linesToAdd.add(new ConfigLine(index, name, true));
 			} else {
 				if (yAxesNotShown == null || yAxesNotShown.contains(name)) {
 					linesToAdd.add(new ConfigLine(index, name, false));
 				}
 			}
 		}
 
 	}
 }
