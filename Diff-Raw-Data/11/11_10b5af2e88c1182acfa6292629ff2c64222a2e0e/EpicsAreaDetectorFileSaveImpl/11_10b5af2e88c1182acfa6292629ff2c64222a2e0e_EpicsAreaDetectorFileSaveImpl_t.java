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
 
 package gda.device.detector.areadetector.impl;
 
 import gda.device.detector.areadetector.EpicsAreaDetectorFileSave;
 import gda.epics.connection.EpicsChannelManager;
 import gda.epics.connection.EpicsController;
 import gda.factory.FactoryException;
 import gov.aps.jca.CAException;
 import gov.aps.jca.Channel;
 import gov.aps.jca.TimeoutException;
 
 public class EpicsAreaDetectorFileSaveImpl implements EpicsAreaDetectorFileSave{
 
 	// Localizable variables
 	private boolean local = true;
 
 
 	// Values to be set by Spring
 	private String  basePVName            = null;
 	private String  initialFileName       = null;
 	private String  initialFileTemplate   = null;
 	private String  initialAutoIncrement  = null;
 	private String  initialAutoSave       = null;
 	private String  initialWriteMode      = null;
 	private Integer initialNumCapture     = null;
 	private String  initialArrayPort      = null;
 	private Integer initialArrayAddress   = null;
 	private Boolean initialBlockingCallback= null;
 
 
 	// Values internal to the object for Channel Access
 	private EpicsChannelManager ecm = new EpicsChannelManager();
 	private EpicsController ecl = EpicsController.getInstance();
 
 
 	// Channels
 	private Channel channelEnable;
 	private Channel channelFilePath;
 	private Channel channelFileName;
 	private Channel channelFileName_RBV;
 	private Channel channelFileTemplate;
 	private Channel channelFileTemplate_RBV;
 	private Channel channelAutoIncrement;
 	private Channel channelFileWriteMode;
 	private Channel channelAutoSave;
 	private Channel channelCapture;
 	private Channel channelNumCapture;
 	private Channel channelFileNumber;
 	private Channel channelFileNumber_RBV;
 	private Channel channelArrayPort;
 	private Channel channelArrayAddress;
 	private Channel channelBlockingCallbacks;
 
 
 	private Channel channelFullFileName_RBV;
 	private Channel channelTimeStamp_RBV;
 
 	// Methods for Localizable interface
 	@Override
 	public boolean isLocal() {
 		return local;
 	}
 
 	@Override
 	public void setLocal(boolean local) {
 		this.local = local;
 	}
 
 
 	// Getters and Setters for spring
 	@Override
 	public void setBasePVName(String basePVName) {
 		this.basePVName = basePVName;
 	}
 
 	@Override
 	public String getBasePVName() {
 		return basePVName;
 	}
 
 	@Override
 	public String getInitialFileName() {
 		return initialFileName;
 	}
 
 	@Override
 	public void setInitialFileName(String initialFileName) {
 		this.initialFileName = initialFileName;
 	}
 
 	@Override
 	public String getInitialFileTemplate() {
 		return initialFileTemplate;
 	}
 
 	@Override
 	public void setInitialFileTemplate(String initialFileTemplate) {
 		this.initialFileTemplate = initialFileTemplate;
 	}
 
 	@Override
 	public String getInitialAutoIncrement() {
 		return initialAutoIncrement;
 	}
 
 	@Override
 	public void setInitialAutoIncrement(String initialAutoIncrement) {
 		this.initialAutoIncrement = initialAutoIncrement;
 	}
 
 	@Override
 	public String getInitialAutoSave() {
 		return initialAutoSave;
 	}
 
 	@Override
 	public void setInitialAutoSave(String initialAutoSave) {
 		this.initialAutoSave = initialAutoSave;
 	}
 
 	@Override
 	public String getInitialWriteMode() {
 		return initialWriteMode;
 	}
 
 	@Override
 	public void setInitialWriteMode(String initialWriteMode) {
 		this.initialWriteMode = initialWriteMode;
 	}
 
 	@Override
 	public Integer getInitialNumCapture() {
 		return initialNumCapture;
 	}
 
 	@Override
 	public void setInitialNumCapture(Integer initialNumCapture) {
 		this.initialNumCapture = initialNumCapture;
 	}	
 
 	@Override
 	public String getInitialArrayPort() {
 		return initialArrayPort;
 	}
 
 	@Override
 	public void setInitialArrayPort(String initialArrayPort) {
 		this.initialArrayPort = initialArrayPort;
 	}
 
 	@Override
 	public Integer getInitialArrayAddress() {
 		return initialArrayAddress;
 	}
 
 	@Override
 	public void setInitialArrayAddress(Integer initialArrayAddress) {
 		this.initialArrayAddress = initialArrayAddress;
 	}
 	@Override
 	public Boolean getInitialBlockingCallbacks() {
 		return initialBlockingCallback;
 	}
 
 	@Override
 	public void setInitialBlockingCallbacks(Boolean initialBlockingCallback) {
 		this.initialBlockingCallback = initialBlockingCallback;
 	}
 
 
 	// Methods for the configurable interface and the reset method
 	@Override
 	public void configure() throws FactoryException {
 		try {
 			channelEnable = ecl.createChannel(basePVName+"EnableCallbacks");
 			channelFilePath = ecl.createChannel(basePVName+"FilePath");
 			channelFileName = ecl.createChannel(basePVName+"FileName");
 			channelFileName_RBV = ecl.createChannel(basePVName+"FileName_RBV");
 			channelFullFileName_RBV = ecl.createChannel(basePVName+"FullFileName_RBV");
 			channelFileTemplate = ecl.createChannel(basePVName+"FileTemplate");
 			channelFileTemplate_RBV = ecl.createChannel(basePVName+"FileTemplate_RBV");
 			channelAutoIncrement = ecl.createChannel(basePVName+"AutoIncrement");
 			channelFileWriteMode = ecl.createChannel(basePVName+"FileWriteMode");
 			channelAutoSave = ecl.createChannel(basePVName+"AutoSave");
 			channelCapture = ecl.createChannel(basePVName+"Capture");
 			channelNumCapture = ecl.createChannel(basePVName+"NumCapture");
 			channelFileNumber = ecl.createChannel(basePVName+"FileNumber");
 			channelFileNumber_RBV = ecl.createChannel(basePVName+"FileNumber_RBV");
 			channelArrayPort = ecl.createChannel(basePVName+"NDArrayPort");
 			channelArrayAddress = ecl.createChannel(basePVName+"NDArrayAddress");
 			channelBlockingCallbacks = ecl.createChannel(basePVName+"BlockingCallbacks");
 			channelTimeStamp_RBV = ecl.createChannel(basePVName+"TimeStamp_RBV");
 			// acknowledge that creation phase is completed
 			ecm.creationPhaseCompleted();
 
 			// now set any values which are appropriate
 			reset();
 
 
 		} catch (Exception e) {
 			throw new FactoryException("Failure to initialise AreaDetector",e);
 		}
 
 	}
 
 	@Override
 	public void reset() throws CAException, InterruptedException {
 		if (initialAutoIncrement != null) setAutoIncrement(initialAutoIncrement);
 		if (initialAutoSave != null) setAutoSave(initialAutoSave);
 		if (initialFileName != null) setFileName(initialFileName);
 		if (initialFileTemplate != null) setFileTemplate(initialFileTemplate);
 		if (initialNumCapture != null) setNumCapture(initialNumCapture);
 		if (initialWriteMode != null) setWriteMode(initialWriteMode);
 		if (initialArrayAddress != null) setArrayAddress(initialArrayAddress);
 		if (initialArrayPort != null) setArrayPort(initialArrayPort);
 		if (initialBlockingCallback != null) setBlockingCallback(initialBlockingCallback);
 	}
 
 
 	private void setBlockingCallback(Boolean enable) throws CAException, InterruptedException {
 		if(enable) {
 			ecl.caput(channelBlockingCallbacks, "Yes");
 		} else {
 			ecl.caput(channelBlockingCallbacks, "No");
 		}
 	}
 
 	// Methods for manipulating the underlying channels	
 	@Override
 	public void setEnable(boolean enable) throws CAException, InterruptedException {
 		if(enable) {
 			ecl.caput(channelEnable, "Yes");
 		} else {
 			ecl.caput(channelEnable, "No");
 		}
 	}
 
 	@Override
 	public void setFilePath(String filePath) throws CAException, InterruptedException {
		String nullterm = filePath + '\0';
		ecl.caput(channelFilePath, nullterm.getBytes());
 	}
 
 	@Override
 	public void setFileName(String fileName) throws CAException, InterruptedException {
		String nullterm = fileName + '\0';
		ecl.caput(channelFileName, nullterm.getBytes());
 	}
 	@Override
 	public String getFullFileName() throws TimeoutException, CAException, InterruptedException {
 		String result = new String(ecl.cagetByteArray(channelFullFileName_RBV));
 		result = result.trim();
 		return result;
 	}	
 	@Override
 	public String getFileName() throws TimeoutException, CAException, InterruptedException {
 		String result = new String(ecl.cagetByteArray(channelFileName_RBV));
 		result = result.trim();
 		return result;
 	}	
 	@Override
 	public String getFilePath() throws TimeoutException, CAException, InterruptedException {
 		String result = new String(ecl.cagetByteArray(channelFilePath));
 		result = result.trim();
 		return result;
 	}
 	@Override
 	public void setFileTemplate(String fileTemplate) throws CAException, InterruptedException {
		String nullterm = fileTemplate + '\0';
		ecl.caput(channelFileTemplate, nullterm.getBytes());
 	}
 	
 	@Override
 	public String getFileTemplate() throws TimeoutException, CAException, InterruptedException {
 		String result = new String(ecl.cagetByteArray(channelFileTemplate_RBV));
 		result = result.trim();
 		return result;
 	}	
 	@Override
 	public double getTimeStamp() throws TimeoutException, CAException, InterruptedException {
 		return ecl.cagetDouble(channelTimeStamp_RBV);
 	}
 
 	@Override
 	public void setWriteMode(String writeMode) throws CAException, InterruptedException {
 		ecl.caput(channelFileWriteMode, writeMode);		
 	}
 
 	@Override
 	public void startCapture() throws CAException, InterruptedException {
 		ecl.caput(channelCapture, 1);
 	}
 
 	@Override
 	public void setNumCapture(int numberOfFramesToCapture) throws CAException, InterruptedException {
 		ecl.caput(channelNumCapture, numberOfFramesToCapture);	
 	}
 
 	@Override
 	public void setframeCounter(int numberOfFrameToSetCounterTo) throws CAException, InterruptedException {
 		ecl.caput(channelFileNumber, numberOfFrameToSetCounterTo);	
 	}	
 
 	@Override
 	public void setAutoIncrement(String increment) throws CAException, InterruptedException {
 		ecl.caput(channelAutoIncrement, increment);	
 	}
 
 	@Override
 	public void setAutoSave(String save) throws CAException, InterruptedException {
 		ecl.caput(channelAutoSave, save);	
 	}	
 
 	@Override
 	public int getFileNumber() throws TimeoutException, CAException, InterruptedException {
 		return ecl.cagetInt(channelFileNumber_RBV);		
 	}
 
 	@Override
 	public void setArrayPort(String channelArrayPortName) throws CAException, InterruptedException {
 		ecl.caput(channelArrayPort, channelArrayPortName);	
 	}
 
 	@Override
 	public void setArrayAddress(int channelArrayAddressToUse) throws CAException, InterruptedException {
 		ecl.caput(channelArrayAddress, channelArrayAddressToUse);	
 	}
 
 }
