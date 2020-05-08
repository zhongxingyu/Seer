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
 
 package gda.device.detector.addetector.filewriter;
 
 import gda.data.fileregistrar.FileRegistrarHelper;
 import gda.device.DeviceException;
 import gda.device.detector.NXDetectorData;
 import gda.device.detector.areadetector.v17.NDFile;
 import gda.device.detector.areadetector.v17.NDFile.FileWriteMode;
 import gda.device.detector.areadetector.v17.NDFileHDF5;
 import gda.device.detector.nxdata.NXDetectorDataAppender;
 import gda.device.detector.nxdata.NXDetectorDataFileLinkAppender;
 import gda.device.detector.nxdata.NXDetectorDataNullAppender;
 import gda.device.detector.nxdetector.NXPlugin;
 import gda.jython.InterfaceProvider;
 import gda.scan.ScanInformation;
 import gov.aps.jca.TimeoutException;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Vector;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.StringUtils;
 
 public class MultipleImagesPerHDF5FileWriter extends FileWriterBase implements NXPlugin{
 	
 	private static Logger logger = LoggerFactory.getLogger(MultipleImagesPerHDF5FileWriter.class);
 
 	private NDFileHDF5 ndFileHDF5;
 	
 	private boolean blocking = true;
 	
 	/*
 	 * default chunking is off so we get 1 image per chunk
 	 */
 	private int rowChunks = 0;
 	private int colChunks=0;
 	private int framesChunks=1;
 	private int framesFlush=1;
 
 	private boolean firstReadoutInScan;
 	
 	@Override
 	public String getName() {
 		return "hdfwriter"; // TODO: Multiple filewriters require different names.
 	}
 	
 	@Override
 	public void setNdFile(NDFile ndFile) {
 		throw new RuntimeException("Configure ndFileHDF5 instead of ndFile");
 	}
 	
 	public void setNdFileHDF5(NDFileHDF5 ndFileHDF5) {
 		this.ndFileHDF5 = ndFileHDF5;
 		super.setNdFile(ndFileHDF5.getFile());
 	}
 	
 	public NDFileHDF5 getNdFileHDF5() {
 		return ndFileHDF5;
 	}
 
 	public int getColChunks() {
 		return colChunks;
 	}
 
 	public void setColChunks(int colChunks) {
 		this.colChunks = colChunks;
 	}
 
 	public int getFramesChunks() {
 		return framesChunks;
 	}
 
 	public void setFramesChunks(int framesChunks) {
 		this.framesChunks = framesChunks;
 	}
 
 	public int getFramesFlush() {
 		return framesFlush;
 	}
 
 	public void setFramesFlush(int framesFlush) {
 		this.framesFlush = framesFlush;
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		if (ndFileHDF5 == null)
 			throw new IllegalStateException("ndFileHDF5 is null");
 		super.afterPropertiesSet();
 	}
 
 	public boolean isBlocking() {
 		return blocking;
 	}
 
 	/**
 	 * 
 	 * @param blocking If true(default) the file plugin is blocking. It is better to pause the scan someother way than rely on teh buffre which can overrun anyway
 	 */
 	public void setBlocking(boolean blocking) {
 		this.blocking = blocking;
 	}
 
 	public int getRowChunks() {
 		return rowChunks;
 	}
 
 	public void setRowChunks(int rowChunks) {
 		this.rowChunks = rowChunks;
 	}
 
 	boolean setChunking=true;
 
 	private boolean storeAttr=false;
 
 	private boolean storePerform=false;
 
 	private boolean alreadyPrepared=false;
 	
 	private boolean lazyOpen=false;
 
 	public boolean isLazyOpen() {
 		return lazyOpen;
 	}
 
 	/**
 	 * 
 	 * @param lazyOpen If true the HDF5 plugin supports LazyOpen and set it to 1
 	 */
 	public void setLazyOpen(boolean lazyOpen) {
 		this.lazyOpen = lazyOpen;
 	}
 
 	private Integer boundaryAlign=null;
 
 	protected String expectedFullFileName;
 
 	private int numToBeCaptured;
 
 	private int numCaptured;
 
 	private Double xPixelSize=null;
 
 	private Double yPixelSize=null;
 
 	private String xPixelSizeUnit=null;
 
 	private String yPixelSizeUnit=null;
 	
 	public Integer getBoundaryAlign() {
 		return boundaryAlign;
 	}
 
 	/**
 	 * 
 	 * @param boundaryAlign value for BounaryAlign PV. Default is null in which case it is not set.
 	 * This was added in version 1-9 of areaDetector
 	 */
 	public void setBoundaryAlign(Integer boundaryAlign) {
 		this.boundaryAlign = boundaryAlign;
 	}
 
 	public boolean isSetChunking() {
 		return setChunking;
 	}
 
 	public void setSetChunking(boolean setChunking) {
 		this.setChunking = setChunking;
 	}
 
 	public boolean isStoreAttr() {
 		return storeAttr;
 	}
 
 	/**
 	 * 
 	 * @param storeAttr if true the hdf5 plugin stores metadata in image file
 	 */
 	public void setStoreAttr(boolean storeAttr) {
 		this.storeAttr = storeAttr;
 	}
 
 	public boolean isStorePerform() {
 		return storePerform;
 	}
 
 	/**
 	 * 
 	 * @param storePerform if true the hdf5 plugin stores performance data in image file
 	 */
 	public void setStorePerform(boolean storePerform) {
 		this.storePerform = storePerform;
 	}
 
 	@Override
 	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
 		if(!isEnabled())
 			return;
 		if( alreadyPrepared)
 			return;
 		setNDArrayPortAndAddress();
 		getNdFile().getPluginBase().disableCallbacks();
 		getNdFile().getPluginBase().setBlockingCallbacks(blocking ? 1:0); //use camera memory 
 		getNdFileHDF5().setStoreAttr(storeAttr? 1:0);
 		getNdFileHDF5().setStorePerform(storePerform?1:0);
 		if( lazyOpen)
 			getNdFileHDF5().setLazyOpen(true);
 		if( boundaryAlign != null)
 			getNdFileHDF5().setBoundaryAlign(boundaryAlign);
 		getNdFile().setFileWriteMode(FileWriteMode.STREAM); 
 		ScanInformation scanInformation = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
 		//if not scan setup then act as if this is a 1 point scan
 		setScanDimensions(scanInformation == null ? new int []{1}: scanInformation.getDimensions(), numberImagesPerCollection);
 		if( isSetFileNameAndNumber()){
 			setupFilename();
 		}
 		deriveFullFileName();
 		clearWriteStatusErr();
 		resetCounters();
 		startRecording();
 		getNdFile().getPluginBase().enableCallbacks();
 		firstReadoutInScan = true;
 		alreadyPrepared=true;
 	}
 
 	protected void deriveFullFileName() throws Exception {
 		expectedFullFileName = String.format(getNdFile().getFileTemplate_RBV(), getNdFile().getFilePath_RBV(), getNdFile().getFileName_RBV(), getNdFile().getFileNumber_RBV());
 	}
 	
 	private void setScanDimensions(int[] dimensions, int numberImagesPerCollection) throws Exception {
 		int [] actualDims = dimensions;
 		if( numberImagesPerCollection > 1){
 			actualDims = Arrays.copyOf(dimensions, dimensions.length+1);
 			actualDims[dimensions.length] = numberImagesPerCollection;
 		}
 		if( actualDims.length > 3)
 			throw new Exception("Maximum dimensions for storing in hdf is currently 3. Value specified = " + actualDims.length);			
 		if( actualDims.length==1 ){
 			getNdFileHDF5().setNumExtraDims(0); 
 		} else	if( actualDims.length==2 ){
 			getNdFileHDF5().setNumExtraDims(1); 
 			getNdFileHDF5().setExtraDimSizeN(actualDims[1]);
 			getNdFileHDF5().setExtraDimSizeX(actualDims[0]);
 		} else	if( actualDims.length==3 ){
 			getNdFileHDF5().setNumExtraDims(2); 
 			getNdFileHDF5().setExtraDimSizeN(actualDims[2]);
 			getNdFileHDF5().setExtraDimSizeX(actualDims[1]);
 			getNdFileHDF5().setExtraDimSizeY(actualDims[0]);
 		}
 		int numberOfAcquires=1;
 		for( int dim : actualDims ){
 			numberOfAcquires *= dim;
 		}
 		getNdFileHDF5().setNumCapture(numberOfAcquires);
 		if( isSetChunking()){
 			getNdFileHDF5().setNumRowChunks(rowChunks);
 			getNdFileHDF5().setNumColChunks(colChunks);
 			getNdFileHDF5().setNumFramesChunks(framesChunks);
 			getNdFileHDF5().setNumFramesFlush(framesFlush);
 		}
 	}
 	
 	private void setupFilename() throws Exception {
 		getNdFile().setFileName(getFileName());
 		getNdFile().setFileTemplate(getFileTemplate());
 		String filePath = getFilePath();
 		
 		if (!filePath.endsWith(File.separator))
 			filePath += File.separator;
 		File f = new File(filePath);
 		if (!f.exists()) {
 			if (!f.mkdirs())
 				throw new Exception("Folder does not exist and cannot be made:" + filePath);
 		}		
 		
 		getNdFile().setFilePath(filePath);
 		if( !getNdFile().filePathExists())
 			if (isPathErrorSuppressed())
 				logger.warn("Ignoring Path does not exist on IOC '" + filePath + "'");
 			else
 				throw new Exception("Path does not exist on IOC '" + filePath + "'");
 		long scanNumber = getScanNumber();
 		
 		getNdFile().setFileNumber((int)scanNumber);	
 		getNdFile().setAutoSave((short) 0);
 		getNdFile().setAutoIncrement((short) 0);
 
 	}
 	
 	private void startRecording() throws Exception {
 		//if (getNdFileHDF5().getCapture() == 1) 
 			//	throw new DeviceException("detector found already saving data when it should not be");
 		
 		getNdFileHDF5().startCapture();
 		int totalmillis = 60 * 1000;
 		int grain = 25;
 		for (int i = 0; i < totalmillis/grain; i++) {
 			if (getNdFileHDF5().getCapture_RBV() == 1) return;
 			Thread.sleep(grain);
 		}
 		throw new TimeoutException("Timeout waiting for hdf file creation.");
 	}
 
 	
 	private void resetCounters() throws Exception {
 		getNdFile().getPluginBase().setDroppedArrays(0);
 		getNdFile().getPluginBase().setArrayCounter(0);
 	}
 
 	
 	
 	
 	@Override
 	public void disableFileWriting() throws Exception {
 		getNdFile().getPluginBase().disableCallbacks();
 		getNdFile().getPluginBase().setBlockingCallbacks((short) 0);
 //		getNdFile().setFileWriteMode(FileWriteMode.STREAM);
 	}
 	
 	
 	@Override
 	public void completeCollection() throws Exception{
 		alreadyPrepared=false;
 		if(!isEnabled())
 			return;
 		FileRegistrarHelper.registerFile(expectedFullFileName);
 		endRecording();
 		disableFileWriting();
 	}
 	
 	private void endRecording() throws Exception {
 		while (getNdFileHDF5().getFile().getCapture_RBV() != 0) {
 			Thread.sleep(1000);
 		}
 		getNdFileHDF5().stopCapture();
 		
 //		logger.warn("Waited very long for hdf writing to finish, still not done. Hope all we be ok in the end.");
 		if (getNdFileHDF5().getFile().getPluginBase().getDroppedArrays_RBV() > 0)
 			throw new DeviceException("sorry, we missed some frames");
 	}
 	
 	@Override
 	public boolean appendsFilepathStrings() {
 		return false;
 	}
 
 	@Override
 	public void stop() throws Exception {
 		alreadyPrepared=false;
 		if(!isEnabled())
 			return;
 		getNdFileHDF5().stopCapture();
 		
 	}
 
 	@Override
 	public void atCommandFailure() throws Exception {
 		alreadyPrepared=false;
 		if(!isEnabled())
 			return;
 		stop();
 	}
 	
 	@Override
 	public List<String> getInputStreamNames() {
 		return Arrays.asList();
 	}
 
 	@Override
 	public List<String> getInputStreamFormats() {
 		return Arrays.asList();
 	}
 
 	@Override
 	public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
 			DeviceException {
 		NXDetectorDataAppender dataAppender;
 		//wait until the NumCaptured_RBV is equal to or exceeds maxToRead.
 		if (isEnabled()) {
 			checkErrorStatus();
		}
 		try {
 			getNdFile().getPluginBase().checkDroppedFrames();
 		} catch (Exception e) {
 			throw new DeviceException("Error in " + getName(), e);
 		}
 		if (firstReadoutInScan) {
 			dataAppender = new NXDetectorDataFileLinkAppender(expectedFullFileName, getxPixelSize(), getyPixelSize(), getxPixelSizeUnit(), getyPixelSizeUnit());
 			numToBeCaptured=1;
 			numCaptured=0;
 		}
 		else {
 			dataAppender = new NXDetectorDataNullAppender();
 			numToBeCaptured++;
 		}
 		while( numCaptured< numToBeCaptured){
 			try {
 				getNdFile().getPluginBase().checkDroppedFrames();
 			} catch (Exception e) {
 				throw new DeviceException("Error in " + getName(), e);
 			}
 			try {
 				numCaptured = getNdFileHDF5().getNumCaptured_RBV();
 			} catch (Exception e) {
 				throw new DeviceException("Error in getCapture_RBV" + getName(), e);
 			}
 			Thread.sleep(50);
 		}
 		firstReadoutInScan = false;
 		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
 		appenders.add(dataAppender);
 		return appenders;
 	}
 
 	
 	 class NXDetectorDataFileLinkAppenderDelayed implements NXDetectorDataAppender {
 
 			@Override
 			public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
 
 				try{
 					String filename = "";
 					do{
 						Thread.sleep(1000);
 						filename=getFullFileName();
 					}
 					while(!StringUtils.hasLength(filename));
 
 					data.addScanFileLink(detectorName, "nxfile://" + filename + "#entry/instrument/detector/data");
 				}catch(Exception ex){
 					throw new DeviceException("Exception getting filename");
 				}
 
 			}
 
 		}
 
 
 	public Double getyPixelSize() {
 		return yPixelSize;
 	}
 
 	public void setyPixelSize(Double yPixelSize) {
 		this.yPixelSize = yPixelSize;
 	}
 
 	public Double getxPixelSize() {
 		return xPixelSize;
 	}
 
 	public void setxPixelSize(Double xPixelSize) {
 		this.xPixelSize = xPixelSize;
 	}
 
 	public String getxPixelSizeUnit() {
 		return xPixelSizeUnit;
 	}
 
 	public void setxPixelSizeUnit(String xPixelSizeUnit) {
 		this.xPixelSizeUnit = xPixelSizeUnit;
 	}
 
 	public void setyPixelSizeUnit(String yPixelSizeUnit) {
 		this.yPixelSizeUnit=yPixelSizeUnit;
 		
 	}
 
 	public String getyPixelSizeUnit() {
 		return yPixelSizeUnit;
 	}
 }
