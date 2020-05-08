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
 
 package gda.device.detector.areadetector.v17.impl;
 
 import gda.configuration.epics.ConfigurationNotFoundException;
 import gda.configuration.epics.Configurator;
 import gda.configuration.epics.EpicsConfiguration;
 import gda.device.Detector;
 import gda.device.DeviceException;
 import gda.device.detector.areadetector.IPVProvider;
 import gda.device.detector.areadetector.v17.NDFile;
 import gda.epics.LazyPVFactory;
 import gda.epics.connection.EpicsController;
 import gda.epics.interfaces.NDFileType;
 import gda.factory.FactoryException;
 import gda.observable.Observable;
 import gda.util.Sleep;
 import gov.aps.jca.CAException;
 import gov.aps.jca.CAStatus;
 import gov.aps.jca.Channel;
 import gov.aps.jca.TimeoutException;
 import gov.aps.jca.event.PutEvent;
 import gov.aps.jca.event.PutListener;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 
 import uk.ac.gda.util.FilePathConverter;
 
 public class NDFileImpl extends NDBaseImpl implements InitializingBean, NDFile {
 
 	private volatile int status = Detector.IDLE;
 
 	public class StartPutListener implements PutListener {
 		@Override
 		public void putCompleted(PutEvent event) {
 			if (event.getStatus() != CAStatus.NORMAL) {
 				logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(),
 						event.getStatus());
 				setStatus(Detector.FAULT);
 				return;
 			}
 			logger.info("Acquisition request completed: {} called back.", ((Channel) event.getSource()).getName());
 			setStatus(Detector.IDLE);
 		}
 	}
 
 	protected final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
 
 	private String basePVName;
 
 	private IPVProvider pvProvider;
 	
 
 	/**
 	 * Map that stores the channel against the PV name
 	 */
 	private Map<String, Channel> channelMap = new HashMap<String, Channel>();
 
 	/**
 	 * List all the PVs
 	 */
 
 	private static final String FilePath = "FilePath";
 
 	private static final String FilePath_RBV = "FilePath_RBV";
 
 	private static final String FileName = "FileName";
 
 	private static final String FileName_RBV = "FileName_RBV";
 
 	private static final String FileNumber = "FileNumber";
 
 	private static final String FileNumber_RBV = "FileNumber_RBV";
 
 	private static final String AutoIncrement = "AutoIncrement";
 
 	private static final String AutoIncrement_RBV = "AutoIncrement_RBV";
 
 	private static final String FileTemplate = "FileTemplate";
 
 	private static final String FileTemplate_RBV = "FileTemplate_RBV";
 
 	private static final String FullFileName_RBV = "FullFileName_RBV";
 
 	private static final String AutoSave = "AutoSave";
 
 	private static final String AutoSave_RBV = "AutoSave_RBV";
 
 	private static final String WriteFile = "WriteFile";
 
 	private static final String WriteFile_RBV = "WriteFile_RBV";
 
 	private static final String ReadFile = "ReadFile";
 
 	private static final String ReadFile_RBV = "ReadFile_RBV";
 
 	private static final String FileFormat = "FileFormat";
 
 	private static final String FileFormat_RBV = "FileFormat_RBV";
 
 	private static final String FileWriteMode = "FileWriteMode";
 
 	private static final String FileWriteMode_RBV = "FileWriteMode_RBV";
 
 	private static final String Capture = "Capture";
 
 	private static final String Capture_RBV = "Capture_RBV";
 
 	private static final String NumCapture = "NumCapture";
 
 	private static final String NumCapture_RBV = "NumCapture_RBV";
 
 	private static final String NumCaptured_RBV = "NumCaptured_RBV";
 
 	private boolean resetToInitialValues=true;
 	private String initialAutoSave;
 	private String initialAutoIncrement;
 	private Integer initialWriteMode;
 	private Integer initialNumCapture;
 	private String initialFileName;
 	private String initialFileTemplate;
 	private Integer initialFileNumber;
 
 	private NDFileType config;
 	private String deviceName;
 
 	private StartPutListener startCallback = new StartPutListener();
 
 	private FilePathConverter filePathConverter;
 	// Setup the logging facilities
 	static final Logger logger = LoggerFactory.getLogger(NDFileImpl.class);
 
 	protected EpicsConfiguration epicsConfiguration;
 
 	/**
 	 * Sets the EpicsConfiguration to use when looking up PV from deviceName.
 	 * 
 	 * @param epicsConfiguration the EpicsConfiguration
 	 */
 	public void setEpicsConfiguration(EpicsConfiguration epicsConfiguration) {
 		this.epicsConfiguration = epicsConfiguration;
 	}
 
 	public String getDeviceName() {
 		return deviceName;
 	}
 
 	public void setDeviceName(String deviceName) throws FactoryException {
 		this.deviceName = deviceName;
 		initializeConfig();
 	}
 
 	private void initializeConfig() throws FactoryException {
 		if (deviceName != null) {
 			try {
 				if (epicsConfiguration != null) {
 					config = epicsConfiguration.getConfiguration(getDeviceName(), NDFileType.class);
 				} else {
 					config = Configurator.getConfiguration(getDeviceName(), NDFileType.class);
 				}
 			} catch (ConfigurationNotFoundException e) {
 				logger.error("EPICS configuration for device {} not found", getDeviceName());
 				throw new FactoryException("EPICS configuration for device " + getDeviceName() + " not found.", e);
 			}
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getFilePath() throws Exception {
 		try {
 			if (config != null) {
 				return new String(EPICS_CONTROLLER.cagetByteArray(createChannel(config.getFilePath().getPv()))).trim();
 			}
 			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(FilePath))).trim();
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getFilePath", ex);
 			throw ex;
 		}
 	}
 
 	protected String getInternalPath(String filepath){
 		return filePathConverter != null ? filePathConverter.converttoInternal(filepath)  : filepath;
 	}
 	/**
 	*
 	*/
 	@Override
 	public void setFilePath(String filepath) throws Exception {
 		try {
 			Channel channel = config != null ? createChannel(config.getFilePath().getPv()) : getChannel(FilePath);
 			byte[] bytes = (getInternalPath(filepath) + '\0').getBytes();
 			EPICS_CONTROLLER.caputWait(channel, bytes);
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.FfmpegStreamImpl-> Cannot setFilePath", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getFilePath_RBV() throws Exception {
 		try {
 				Channel channel = config != null ? createChannel(config.getFilePath_RBV().getPv()) : getChannel(FilePath_RBV);
 				String filepath = new String(EPICS_CONTROLLER.cagetByteArray(channel)).trim();
 				return filePathConverter != null ? filePathConverter.converttoExternal(filepath)  : filepath;
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getFilePath_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getFileName() throws Exception {
 		try {
 			if (config != null) {
 				return new String(EPICS_CONTROLLER.cagetByteArray(createChannel(config.getFileName().getPv()))).trim();
 			}
 			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(FileName))).trim();
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getFileName", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setFileName(String filename) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getFileName().getPv()), (filename + '\0').getBytes());
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(FileName), (filename + '\0').getBytes());
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.FfmpegStreamImpl-> Cannot setFileName", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getFileName_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return new String(EPICS_CONTROLLER.cagetByteArray(createChannel(config.getFileName_RBV().getPv())))
 						.trim();
 			}
 			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(FileName_RBV))).trim();
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getFileName_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getFileNumber() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getFileNumber().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(FileNumber));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getFileNumber", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setFileNumber(int filenumber) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getFileNumber().getPv()), filenumber);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(FileNumber), filenumber);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.FfmpegStreamImpl-> Cannot setFileNumber", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getFileNumber_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getFileNumber_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(FileNumber_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getFileNumber_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getAutoIncrement() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getAutoIncrement().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(AutoIncrement));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getAutoIncrement", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setAutoIncrement(int autoincrement) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getAutoIncrement().getPv()), autoincrement);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(AutoIncrement), autoincrement);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.FfmpegStreamImpl-> Cannot setAutoIncrement", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getAutoIncrement_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getAutoIncrement_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(AutoIncrement_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getAutoIncrement_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getFileTemplate() throws Exception {
 		try {
 			if (config != null) {
 				return new String(EPICS_CONTROLLER.cagetByteArray(createChannel(config.getFileTemplate().getPv())))
 						.trim();
 			}
 			return new String(EPICS_CONTROLLER.cagetByteArray((getChannel(FileTemplate)))).trim();
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getFileTemplate", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setFileTemplate(String filetemplate) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getFileTemplate().getPv()),
 						(filetemplate + '\0').getBytes());
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(FileTemplate), (filetemplate + '\0').getBytes());
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.FfmpegStreamImpl-> Cannot setFileTemplate", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getFileTemplate_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return new String(EPICS_CONTROLLER.cagetByteArray(createChannel(config.getFileTemplate_RBV().getPv())))
 						.trim();
 			}
 			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(FileTemplate_RBV))).trim();
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getFileTemplate_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getFullFileName_RBV() throws Exception {
 		try {
 			Channel channel = config != null ? createChannel(config.getFullFileName_RBV().getPv()) : getChannel(FullFileName_RBV);
 			String filepath = new String(EPICS_CONTROLLER.cagetByteArray(channel)).trim();
 			return filePathConverter != null ? filePathConverter.converttoExternal(filepath)  : filepath;
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getFullFileName_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getAutoSave() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getAutoSave().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(AutoSave));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getAutoSave", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setAutoSave(int autosave) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getAutoSave().getPv()), autosave);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(AutoSave), autosave);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.FfmpegStreamImpl-> Cannot setAutoSave", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getAutoSave_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getAutoSave_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(AutoSave_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getAutoSave_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getWriteFile() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getWriteFile().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(WriteFile));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getWriteFile", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setWriteFile(int writefile) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getWriteFile().getPv()), writefile);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(WriteFile), writefile);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.FfmpegStreamImpl-> Cannot setWriteFile", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getWriteFile_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getWriteFile_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(WriteFile_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getWriteFile_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getReadFile() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getReadFile().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ReadFile));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getReadFile", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setReadFile(int readfile) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getReadFile().getPv()), readfile);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ReadFile), readfile);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.FfmpegStreamImpl-> Cannot setReadFile", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getReadFile_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getReadFile_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ReadFile_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getReadFile_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getFileFormat() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getFileFormat().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(FileFormat));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getFileFormat", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setFileFormat(int fileformat) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getFileFormat().getPv()), fileformat);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(FileFormat), fileformat);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.FfmpegStreamImpl-> Cannot setFileFormat", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getFileFormat_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getFileFormat_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(FileFormat_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getFileFormat_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getFileWriteMode() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getFileWriteMode().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(FileWriteMode));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getFileWriteMode", ex);
 			throw ex;
 		}
 	}
 	
 	@Override
 	public void setFileWriteMode(FileWriteMode mode) throws Exception {
 		setFileWriteMode((short) mode.ordinal());
 	}
 	
 	@Override
 	public void setFileWriteMode(int filewritemode) throws Exception {
 		logger.info("Setting file-write-mode to: " + filewritemode);
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getFileWriteMode().getPv()), filewritemode);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(FileWriteMode), filewritemode);
 			}
 		} catch (Exception ex) {
 			logger.warn("Cannot setFileWriteMode", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getFileWriteMode_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getFileWriteMode_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(FileWriteMode_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getFileWriteMode_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getCapture() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getCapture().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(Capture));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getCapture", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	 * @throws Exception
 	 */
 	@Override
 	public void startCapture() throws Exception {
 		try {
 			if (getCapture_RBV() == 1) {
 				return; // File capture already waiting, so it will not respond to another caput callback.
 			}
 			setStatus(Detector.BUSY);
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getCapture().getPv()), 1, startCallback);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(Capture), 1, startCallback);
 			}
 			int counter=0;
 			while(getCapture_RBV() != 1) {
 				Thread.sleep(50);
 				counter++;
 				if( counter > 100) // more than 5 seconds
 					throw new DeviceException("Capture failed");
 			}
 		} catch (Exception e) {
 			setStatus(Detector.IDLE);
 			logger.error("Exception caught on start Capture", e);
 			throw e;
 		}
 	}
 
 	@Override
 	public void startCaptureSynchronously() throws Exception {
 		startCapture();
 		while (getStatus() != Detector.IDLE) {
 			Sleep.sleep(100);
 		}
 	}
 
 	@Override
 	public void stopCapture() throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getCapture().getPv()), 0);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(Capture), 0);
 			}
 			// If the acquisition state is busy then wait for it to complete.
 			while (getCapture() == 1) {
 				logger.info("sleeping for 25");
 				Sleep.sleep(25);
 			}
 			setStatus(Detector.IDLE);
 		} catch (Exception e) {
 			logger.error("Exception caught on stop Capture", e);
 			throw e;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getCapture_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getCapture_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(Capture_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getCapture_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getNumCapture() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumCapture().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(NumCapture));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getNumCapture", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setNumCapture(int numcapture) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getNumCapture().getPv()), numcapture);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(NumCapture), numcapture);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.FfmpegStreamImpl-> Cannot setNumCapture", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getNumCapture_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumCapture_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(NumCapture_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getNumCapture_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getNumCaptured_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumCaptured_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(NumCaptured_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDFileImpl-> Cannot getNumCaptured_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	 * @return Returns the basePVName.
 	 */
 	public String getBasePVName() {
 		return basePVName;
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		if (deviceName == null && basePVName == null && pvProvider == null) {
 			throw new IllegalArgumentException("'deviceName','basePVName' or 'pvProvider' needs to be declared");
 		}
 		if (getPluginBase() == null) {
			logger.warn(getIdentifier() + " : 'ndPluginBase' not declared");
 			// TODO: The pilatus driver contains an NDFile with no associated NDPluginBase
 			// throw new IllegalArgumentException("'ndPluginBase' needs to be declared");
 		}
 		if( resetToInitialValues){
 			if (initialAutoSave == null) {
 				throw new IllegalArgumentException("'initialAutoSave' needs to be declared");
 			}
 			if (initialAutoIncrement == null) {
 				throw new IllegalArgumentException("'initialAutoIncrement' needs to be declared");
 			}
 			if (initialNumCapture == null) {
 				throw new IllegalArgumentException("'initialNumCapture' needs to be declared");
 			}
 			if (initialFileName == null) {
 				throw new IllegalArgumentException("'initialFileName' needs to be declared");
 			}
 			if (initialFileTemplate == null) {
 				throw new IllegalArgumentException("'initialFileTemplate' needs to be declared");
 			}
 		}
 
 	}
 
 	/**
 	 * @param basePVName
 	 *            The basePVName to set.
 	 */
 	public void setBasePVName(String basePVName) {
 		this.basePVName = basePVName;
 	}
 
 	/**
 	 * @return Returns the pvProvider.
 	 */
 	public IPVProvider getPvProvider() {
 		return pvProvider;
 	}
 
 	/**
 	 * @param pvProvider
 	 *            The pvProvider to set.
 	 */
 	public void setPvProvider(IPVProvider pvProvider) {
 		this.pvProvider = pvProvider;
 	}
 
 	/**
 	 * @return Returns the initialFileName. getInitialFileName
 	 */
 	@Override
 	public String getInitialFileName() {
 		return initialFileName;
 	}
 
 	/**
 	 * @param initialFileName
 	 *            The initialFileName to set.
 	 */
 	public void setInitialFileName(String initialFileName) {
 		this.initialFileName = initialFileName;
 	}
 
 	@Override
 	public void reset() throws Exception {
 		// Reset the plugin base variables.
 		if (getPluginBase() != null) {
 			getPluginBase().reset();
 		}
 		if( resetToInitialValues){
 			// Reset local variables.
 			setInitialAutoSave(initialAutoSave);
 			setInitialAutoIncrement(initialAutoIncrement);
 			if (initialWriteMode != null) {
 				setFileWriteMode(initialWriteMode);
 			}
 			setNumCapture(initialNumCapture);
 			resetFileTemplate();
 			setFileName(initialFileName);
 			if (initialFileNumber != null) {
 				setFileNumber(initialFileNumber);
 			}
 		}
 		setStatus(Detector.IDLE);
 	}
 
 	@Override
 	public void resetFileTemplate() throws Exception {
 		setFileTemplate(initialFileTemplate);
 	}
 
 	/**
 	 * @return Returns the initialAutoSave. getInitialAutoSave
 	 */
 	public String getInitialAutoSave() {
 		return initialAutoSave;
 	}
 
 	/**
 	 * @param initialAutoSave
 	 *            The initialAutoSave to set.
 	 */
 	public void setInitialAutoSave(String initialAutoSave) {
 		this.initialAutoSave = initialAutoSave;
 	}
 
 	/**
 	 * @return Returns the initialAutoIncrement. getInitialAutoIncrement
 	 */
 	public String getInitialAutoIncrement() {
 		return initialAutoIncrement;
 	}
 
 	/**
 	 * @param initialAutoIncrement
 	 *            The initialAutoIncrement to set.
 	 */
 	public void setInitialAutoIncrement(String initialAutoIncrement) {
 		this.initialAutoIncrement = initialAutoIncrement;
 	}
 
 	/**
 	 * @return Returns the initialWriteMode. getInitialWriteMode
 	 */
 	public Integer getInitialWriteMode() {
 		return initialWriteMode;
 	}
 
 	/**
 	 * @param initialWriteMode
 	 *            The initialWriteMode to set.
 	 */
 	public void setInitialWriteMode(Integer initialWriteMode) {
 		this.initialWriteMode = initialWriteMode;
 	}
 
 	/**
 	 * @return Returns the initialNumCapture. getInitialNumCapture
 	 */
 	public Integer getInitialNumCapture() {
 		return initialNumCapture;
 	}
 
 	/**
 	 * @param initialNumCapture
 	 *            The initialNumCapture to set.
 	 */
 	public void setInitialNumCapture(Integer initialNumCapture) {
 		this.initialNumCapture = initialNumCapture;
 	}
 
 	/**
 	 * @return Returns the initialFileTemplate. getInitialFileTemplate
 	 */
 	@Override
 	public String getInitialFileTemplate() {
 		return initialFileTemplate;
 	}
 
 	/**
 	 * @param initialFileTemplate
 	 *            The initialFileTemplate to set.
 	 */
 	public void setInitialFileTemplate(String initialFileTemplate) {
 		this.initialFileTemplate = initialFileTemplate;
 	}
 
 	private String getChannelName(String pvElementName, String... args)throws Exception{
 		String pvPostFix = null;
 		if (args.length > 0) {
 			// PV element name is different from the pvPostFix
 			pvPostFix = args[0];
 		} else {
 			pvPostFix = pvElementName;
 		}
 
 		String fullPvName;
 		if (pvProvider != null) {
 			fullPvName = pvProvider.getPV(pvElementName);
 		} else {
 			fullPvName = basePVName + pvPostFix;
 		}
 		return fullPvName;
 	}
 	/**
 	 * This method allows to toggle between the method in which the PV is acquired.
 	 * 
 	 * @param pvElementName
 	 * @param args
 	 * @return {@link Channel} to talk to the relevant PV.
 	 * @throws Exception
 	 */
 	private Channel getChannel(String pvElementName, String... args) throws Exception {
 		return createChannel(getChannelName(pvElementName, args));
 	}
 
 	public Channel createChannel(String fullPvName) throws CAException, TimeoutException {
 		Channel channel = channelMap.get(fullPvName);
 		if (channel == null) {
 			channel = EPICS_CONTROLLER.createChannel(fullPvName);
 			channelMap.put(fullPvName, channel);
 		}
 		return channel;
 	}
 
 	private Object statusMonitor = new Object();
 	@Override
 	public void setStatus(int status) {
 		synchronized (statusMonitor) {
 			this.status = status;
 			this.statusMonitor.notifyAll();
 		}
 	}
 	
 	@Override
 	public void waitWhileStatusBusy() throws InterruptedException {
 		synchronized (statusMonitor) {
 			while (status == Detector.BUSY) { 
 				try {
 					statusMonitor.wait(1000);
 				} catch (InterruptedException e) {
 					setStatus(0);
 					throw e;
 				}
 			}
 		}
 	}
 
 	@Override
 	public int getStatus() {
 		return status;
 	}
 
 	@Override
 	public void getEPICSStatus() throws Exception {
 		this.status = getCapture_RBV();
 	}
 
 	public Integer getInitialFileNumber() {
 		return initialFileNumber;
 	}
 
 	public void setInitialFileNumber(Integer initialFileNumber) {
 		this.initialFileNumber = initialFileNumber;
 	}
 
 	public FilePathConverter getFilePathConverter() {
 		return filePathConverter;
 	}
 
 	public void setFilePathConverter(FilePathConverter filePathConverter) {
 		this.filePathConverter = filePathConverter;
 	}
 
 	public boolean isResetToInitialValues() {
 		return resetToInitialValues;
 	}
 
 	public void setResetToInitialValues(boolean resetToInitialValues) {
 		this.resetToInitialValues = resetToInitialValues;
 	}
 
 	@Override
 	public Boolean filePathExists() throws Exception {
 		Channel channel = config != null ? createChannel(config.getFilePathExists().getPv()) : getChannel(FilePathExists_RBV);
 		return EPICS_CONTROLLER.cagetInt(channel)!=0;
 	}
 
 	@Override
 	public Observable<String> createWriteMessageObservable() throws Exception {
 		return LazyPVFactory.newNoCallbackStringFromWaveformPV(getChannelName(WriteMessage));
 	}
 
 	@Override
 	public Observable<Short> createWriteStatusObservable() throws Exception {
 		return LazyPVFactory.newNoCallbackShortPV(getChannelName(WriteStatus));
 	}
 
 	@Override
 	public String getWriteMessage() throws Exception {
 		Channel channel = config != null ? createChannel(config.getWriteMessage().getPv()) : getChannel(WriteMessage);
 		return new String(EPICS_CONTROLLER.cagetByteArray(channel)).trim();
 	}
 
 	@Override
 	public Boolean isWriteStatusErr() throws Exception {
 		Channel channel = config != null ? createChannel(config.getWriteStatus().getPv()) : getChannel(WriteStatus);
 		return EPICS_CONTROLLER.cagetShort(channel)!=0;
 	}

	private String getIdentifier() {
		// Since this can be configured with either a deviceName, pvProvider or basePVname, return an identifier based on
		// whichever is currently in use. If multiple are available, return in this order, which matches usage in the class.
		String id = getDeviceName();
		if (id == null && pvProvider != null)
			try {
				id = pvProvider.getPV("");
			} catch (Exception e) {
				logger.error("pvProvider.getPV('') threw exception: ", e);
			}
		if (id == null) id = basePVName;
		return id;
	}
 }
