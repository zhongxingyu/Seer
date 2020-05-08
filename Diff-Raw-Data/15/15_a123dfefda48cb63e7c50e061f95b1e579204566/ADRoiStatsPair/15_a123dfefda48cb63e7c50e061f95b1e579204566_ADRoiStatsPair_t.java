 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
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
 
 package gda.device.detector.nxdetector.plugin.areadetector;
 
 import gda.device.DeviceException;
 import gda.device.detector.NXDetectorData;
 import gda.device.detector.areadetector.v18.NDStatsPVs.BasicStat;
 import gda.device.detector.areadetector.v18.NDStatsPVs.CentroidStat;
 import gda.device.detector.areadetector.v18.NDStatsPVs.Stat;
 import gda.device.detector.nxdata.NXDetectorDataAppender;
 import gda.device.detector.nxdetector.NXPlugin;
 import gda.device.detector.nxdetector.roi.ImutableRectangularIntegerROI;
 import gda.device.detector.nxdetector.roi.RectangularROI;
 import gda.device.detector.nxdetector.roi.RectangularROIProvider;
 import gda.device.detector.nxdetector.roi.SimpleRectangularROIProvider;
 import gda.device.scannable.PositionInputStream;
 import gda.device.scannable.PositionInputStreamCombiner;
 import gda.scan.ScanInformation;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 /**
  * Enabled if any stats are enabled. If stats are enabled an ROI must have been set. The names of the specidfied roi and
  * stats plugins are ignored in favour of this plugins name (could be revisited--RobW Feb2013).
  */
 public class ADRoiStatsPair implements NXPlugin, PositionInputStream<NXDetectorDataAppender>, RectangularROIProvider<Integer> {
 
 	final private ADRectangularROIPlugin roiPlugin;
 
 	final private ADTimeSeriesStatsPlugin statsPlugin;
 
 	final private String name;
 
 	private final String roiInputPort;
 	
 	private final RectangularROIProvider<Integer> roiProvider;
 
 	private PositionInputStream<List<NXDetectorDataAppender>> combinedInputStream;
 
 
 	public ADRoiStatsPair(String name, ADRectangularROIPlugin roiPlugin, ADTimeSeriesStatsPlugin statsPlugin,
 			String roiInputPort, RectangularROIProvider<Integer> roiProvider) {
 		super();
 		this.name = name;
 		this.roiPlugin = roiPlugin; 
 		this.statsPlugin = statsPlugin;
 		this.roiInputPort = roiInputPort;
 		this.roiProvider = roiProvider;
 	}
 	
 	@Override
 	public String toString() {
 		String str = "";
 		RectangularROI<Integer> roi = getRoi();
 		ArrayList<String> names = new ArrayList<String>();
 
 		for (BasicStat stat : getEnabledBasicStats()) {
 			names.add(stat.name().toLowerCase());
 		}
 		for (CentroidStat stat : getEnabledCentroidStats()) {
 			names.add(stat.name().toLowerCase());
 		} 
 		
 		str += Arrays.toString(names.toArray());
 		if (roi == null) {
 			str += " *disabled*";
 		} else {
 			str += " " + roi;
 		}
 		return str;
 	}
 	
 	public ADRectangularROIPlugin getRoiPlugin() {
 		return roiPlugin;
 	}
 
 	public ADTimeSeriesStatsPlugin getStatsPlugin() {
 		return statsPlugin;
 	}
 
 	public List<BasicStat> getEnabledBasicStats() {
 		return getStatsPlugin().getEnabledBasicStats();
 	}
 
 	public void setEnabledBasicStats(List<BasicStat> enabledBasicStats) {
 		getStatsPlugin().setEnabledBasicStats(enabledBasicStats);
 	}
 
 	public List<CentroidStat> getEnabledCentroidStats() {
 		return getStatsPlugin().getEnabledCentroidStats();
 	}
 
 	public void setEnabledCentroidStats(List<CentroidStat> enabledCentroidStats) {
 		getStatsPlugin().setEnabledCentroidStats(enabledCentroidStats);
 	}
 
 	private List<Stat> getEnabledStats() {
 		return getStatsPlugin().getEnabledStats();
 	}
 
 	/**
 	 * Set the roi to configure.
 	 * @param roi null to disable
 	 * @throws IllegalStateException if called with an ROI provider set
 	 */
 	public void setRoi(RectangularROI<Integer> roi) throws IllegalStateException{
 		if (roiProvider instanceof SimpleRectangularROIProvider) {
 			((SimpleRectangularROIProvider) roiProvider).setRoi(roi);
 		} else {
 			throw new IllegalStateException("Not configured to have a manually configurable roi");
 		}
 	}	
 
 	@Override
 	public RectangularROI<Integer> getRoi() {
 		try {
 			return roiProvider.getRoi();
 		} catch (Exception e) {
			throw new RuntimeException("Problem querying the configured roiProvider: " + e.getMessage(), e);
 		}
 	}
 	
 	public void setRoi(Integer xstart, Integer ystart, Integer xsize, Integer ysize, String name) {
 		setRoi(new ImutableRectangularIntegerROI(xstart, ystart, xsize, ysize, name));
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public boolean willRequireCallbacks() {
 		// Will only work if both are set
 		return ((!getEnabledStats().isEmpty()) && (getRoi()!=null));
 	}
 
 	@Override
 	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
 //		if (!getEnabledStats().isEmpty() && (getRoi()==null)) {
 //			throw new IllegalStateException("Stats where enabled, but no ROI configured");
 //		}
 //		if (!getEnabledStats().isEmpty() && (getRoi()!=null)) {
 //			throw new IllegalStateException("An ROI was configured, but no stats enabled");
 //		}
 		
 		boolean enabled = (!getEnabledStats().isEmpty() && (getRoi()!=null));
 
 		if (enabled) {
 			getRoiPlugin().setInputNDArrayPort(roiInputPort);
 			String roiPortName = getRoiPlugin().getPortName();
 			getStatsPlugin().setInputNDArrayPort(roiPortName);
 		}
 		
 		// prepare even if disabled as preparation will in this case disable the plugins
 		
 		getRoiPlugin().prepareForCollection(numberImagesPerCollection, scanInfo);
 		getStatsPlugin().prepareForCollection(numberImagesPerCollection, scanInfo);
 		List<PositionInputStream<NXDetectorDataAppender>> streams = new ArrayList<PositionInputStream<NXDetectorDataAppender>>();
 		streams.add(getRoiPlugin());
 		streams.add(getStatsPlugin());
 		combinedInputStream = new PositionInputStreamCombiner<NXDetectorDataAppender>(streams);
 	}
 
 	@Override
 	public void prepareForLine() throws Exception {
 		getRoiPlugin().prepareForLine();
 		getStatsPlugin().prepareForLine();
 	}
 
 	@Override
 	public void completeLine() throws Exception {
 		getRoiPlugin().completeLine();
 		getStatsPlugin().completeLine();
 	}
 
 	@Override
 	public void completeCollection() throws Exception {
 		getRoiPlugin().completeCollection();
 		getStatsPlugin().completeCollection();
 	}
 
 	@Override
 	public void atCommandFailure() throws Exception {
 		getRoiPlugin().atCommandFailure();
 		getStatsPlugin().atCommandFailure();
 	}
 
 	@Override
 	public void stop() throws Exception {
 		getRoiPlugin().stop();
 		getStatsPlugin().stop();
 	}
 
 	/**
 	 * Returns the roii and stats plugins names prefixed by the roi plugins name. (roi plugins names will probably be empty).
 	 */
 	@Override
 	public List<String> getInputStreamNames() {
 		return getStatsPlugin().getInputStreamNames();
 	}
 
 	@Override
 	public List<String> getInputStreamFormats() {
 		return getStatsPlugin().getInputStreamFormats();
 	}
 	
 	@Override
 	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
 			DeviceException {
 		List<List<NXDetectorDataAppender>> newAppenderPairs = combinedInputStream.read(maxToRead);
 		
 		List<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
 		for (List<NXDetectorDataAppender> appenderPair : newAppenderPairs) {
 			appenders.add(new SerialAppender(appenderPair));
 		}
 		return appenders;
 	}
 
 }
 
 class SerialAppender implements NXDetectorDataAppender {
 	
 	final private List<NXDetectorDataAppender> appenders;
 
 	public SerialAppender(List<NXDetectorDataAppender> appenders) {
 		this.appenders = appenders;
 	}
 
 	@Override
 	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
 		for (NXDetectorDataAppender appender : appenders) {
 			appender.appendTo(data, detectorName);
 		}
 	}
 
 }
