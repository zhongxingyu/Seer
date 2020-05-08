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
 
 package gda.device.enumpositioner;
 
 import gda.configuration.epics.ConfigurationNotFoundException;
 import gda.configuration.epics.Configurator;
 import gda.device.DeviceException;
 import gda.device.EnumPositioner;
 import gda.device.EnumPositionerStatus;
 import gda.epics.connection.EpicsChannelManager;
 import gda.epics.connection.EpicsController;
 import gda.epics.connection.InitializationListener;
 import gda.epics.interfaces.PositionerType;
 import gda.epics.xml.EpicsRecord;
 import gda.factory.FactoryException;
 import gda.factory.Finder;
 import gda.util.exceptionUtils;
 import gov.aps.jca.CAStatus;
 import gov.aps.jca.Channel;
 import gov.aps.jca.dbr.DBR;
 import gov.aps.jca.dbr.DBR_Double;
 import gov.aps.jca.dbr.Severity;
 import gov.aps.jca.dbr.Status;
 import gov.aps.jca.event.MonitorEvent;
 import gov.aps.jca.event.MonitorListener;
 import gov.aps.jca.event.PutEvent;
 import gov.aps.jca.event.PutListener;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class maps to EPICS positioner template.
  * 
  * @see <a href="http://serv0002.cs.diamond.ac.uk/cgi-bin/wiki.cgi/positioner">positioner module documentation</a>
  */
 public class EpicsPositioner extends EnumPositionerBase implements EnumPositioner, InitializationListener {
 	/**
 	 * logger
 	 */
 	private static final Logger logger = LoggerFactory.getLogger(EpicsPositioner.class);
 
 	private String epicsRecordName;
 
 	private String deviceName;
 	
 	protected EpicsController controller;
 
 	protected EpicsChannelManager channelManager;
 	
 	private String recordName;
 	
 	private String selectRecordName;
 	
 	private String inPosRecordName;
 	
 	private String dMovRecordName;
 	
 	private String stopRecordName;
 	
 	private String errorRecordName;
 	
 	protected Channel select;
 
 	private Channel inPos;
 
 	private Channel dmov;
 
 	private Channel stop;
 
 	private Channel error;
 
 	private InposMonitorListener inposMonitor;
 
 	protected DmovMonitorListener dmovMonitor;
 
 	private ErrorMonitorListener errorMonitor;
 
 	private PutCallbackListener putCallbackListener;
 	private Status status = Status.NO_ALARM;
 	private Severity severity = Severity.NO_ALARM;
 
 	private Object lock = new Object();
 	
 	protected PositionerType configuration;
 	
 	/**
 	 * Constructor
 	 */
 	public EpicsPositioner() {
 		super();
 		channelManager = new EpicsChannelManager(this);
 		controller = EpicsController.getInstance();
 		inposMonitor = new InposMonitorListener();
 		dmovMonitor = new DmovMonitorListener();
 		errorMonitor = new ErrorMonitorListener();
 		putCallbackListener = new PutCallbackListener();
 	}
 	
 	/**
 	 * Sets the record name that this positioner will link to.
 	 * 
 	 * @param recordName the record name
 	 */
 	public void setRecordName(String recordName) {
 		this.recordName = recordName;
 	}
 	
 	/**
 	 * Sets the configuration for this positioner.
 	 */
 	public void setConfiguration(PositionerType configuration) {
 		this.configuration = configuration;
 	}
 	
 	@Override
 	public void configure() throws FactoryException {
 		if (!configured) {
 			
 			if (recordName != null) {
 				setRecordNamesUsingBasePv(recordName);
 			}
 			
 			else if (configuration != null) {
 				setRecordNamesUsingEpicsConfiguration(configuration);
 			}
 			
 			// EPICS interface version 2 for phase I beamlines + I22
 			else if (getEpicsRecordName() != null) {
 				EpicsRecord epicsRecord;
 				
 				if ((epicsRecord = (EpicsRecord) Finder.getInstance().find(epicsRecordName)) != null) {
 					String recordName = epicsRecord.getFullRecordName();
 					setRecordNamesUsingBasePv(recordName);
 				} else {
 					logger.error("Epics Record " + epicsRecordName + " not found");
 					throw new FactoryException("Epics Record " + epicsRecordName + " not found");
 				}
 			}
 			
 			// EPICS interface version 3 for phase II beamlines (excluding I22).
 			else if (getDeviceName() != null) {
 				PositionerType pnrConfig;
 				try {
 					pnrConfig = Configurator.getConfiguration(getDeviceName(), PositionerType.class);
 					setRecordNamesUsingEpicsConfiguration(pnrConfig);
 				} catch (ConfigurationNotFoundException e) {
 					logger.error("Can NOT find EPICS configuration for motor " + getDeviceName(), e);
 				}
 			}
 			
 			// Nothing specified in Server XML file
 			else {
 				logger.error("Missing EPICS interface configuration for the motor " + getName());
 				throw new FactoryException("Missing EPICS interface configuration for the motor " + getName());
 			}
 			
 			createChannelAccess();
 			channelManager.tryInitialize(100);
 			
 			configured = true;
 		}
 	}
 	
 	/**
 	 * Retrieves the five PVs for this {@link EpicsPositioner} from the specified {@link PositionerType} object.
 	 */
 	private void setRecordNamesUsingEpicsConfiguration(PositionerType config) {
 		selectRecordName = config.getSELECT().getPv();
 		inPosRecordName = config.getINPOS().getPv();
 		dMovRecordName = config.getDMOV().getPv();
 		stopRecordName = config.getSTOP().getPv();
 		errorRecordName = config.getERROR().getPv();
 	}
 	
 	/**
 	 * Builds the five PVs for this {@link EpicsPositioner} by appending suffixes to a base record name.
 	 */
 	protected void setRecordNamesUsingBasePv(String recordName) {
 		selectRecordName = recordName + ":SELECT";
 		inPosRecordName = recordName + ":INPOS";
 		dMovRecordName = recordName + ":DMOV";
 		stopRecordName = recordName + ":STOP.PROC";
 		errorRecordName = recordName + ":ERROR.K";
 	}
 	
 	protected void createChannelAccess() throws FactoryException {
 		try {
 			select = channelManager.createChannel(selectRecordName, false);
 			inPos = channelManager.createChannel(inPosRecordName, inposMonitor, false);
 			dmov = channelManager.createChannel(dMovRecordName, dmovMonitor, false);
 			stop = channelManager.createChannel(stopRecordName, false);
 			error = channelManager.createChannel(errorRecordName, errorMonitor, false);
 
 			// acknowledge that creation phase is completed
 			channelManager.creationPhaseCompleted();
 
 		} catch (Throwable th) {
 			// TODO take care of destruction
 			throw new FactoryException("failed to connect to all channels", th);
 		}
 	}
 
 	@Override
 	public String getPosition() throws DeviceException {
 		try {
 			short test = controller.cagetEnum(select);
 			return positions.get(test);
 		} catch (Throwable th) {
 			throw new DeviceException("failed to get position from " + select.getName(), th);
 		}
 	}
 
 	/**
 	 * @return EnumPositionerStatus
 	 * @throws DeviceException
 	 */
 	@Deprecated
 	public EnumPositionerStatus getPositionerStatus() throws DeviceException {
 		try {
 			// first check if its moving
 			if (controller.cagetDouble(dmov) == 0.0) {
 				return EnumPositionerStatus.MOVING;
 			}
 			// check if in position
 			if (controller.cagetDouble(inPos) == 1.0) {
 				// and status is NO_ALARM
 				if (controller.cagetDouble(error) == 0) {
 					return EnumPositionerStatus.IDLE;
 				}
 				logger.error("EpicsPositioner: " + getName() + " completed move but has error status.");
 				notifyIObservers(this, EnumPositionerStatus.ERROR);
 				return EnumPositionerStatus.ERROR;
 			}
 			// else its an error
 
 			logger.error("EpicsPositioner: " + getName() + " failed to successfully move to required location.");
 			return EnumPositionerStatus.ERROR;
 
 		} catch (Throwable e) {
 			throw new DeviceException("while updating EpicsPositioner " + getName() + " : " + e.getMessage(), e);
 		}
 
 	}
 
 	@Override
 	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
 		// find in the positionNames array the index of the string
 		if (positions.contains(position.toString())) {
 				int target = positions.indexOf(position.toString());
 				try {
 					if (getStatus() == EnumPositionerStatus.MOVING) {
 						logger.warn("{} is busy", getName());
 						return;
 					}
 					positionerStatus = EnumPositionerStatus.MOVING;
 					controller.caput(select, target, putCallbackListener);
 				} catch (Throwable th) {
 					positionerStatus = EnumPositionerStatus.ERROR;
 					throw new DeviceException(select.getName() + " failed to moveTo " + position.toString(), th);
 				}
 			return;
 		}
 
 		// if get here then wrong position name supplied
 		throw new DeviceException("Position called: " + position.toString()+ " not found.");
 
 	}
 
 	@Override
 	public void stop() throws DeviceException {
 		try {
 			controller.caput(stop, 1, putCallbackListener);
 		} catch (Throwable th) {
 			throw new DeviceException("failed to stop " + stop.getName(), th);
 		}
 
 	}
 
 	@Override
 	public String[] getPositions() throws DeviceException  {
 		try {
 			return controller.cagetLabels(select);
 		} catch (Exception e) {
 			throw new DeviceException("Error gettings labels for " + getName(),e);
 		}
 	}
 
 	@Override
 	public void initializationCompleted() throws DeviceException  {
 		String[] position = getPositions();
 		for (int i = 0; i < position.length; i++) {
 			if (position[i] != null || position[i] != "") {
 				super.positions.add(position[i]);
 			}
 		}
 		logger.info("EpicsPositioner " + getName() + " is initialised");
 	}
 
 	/**
 	 * InPos monitor listener
 	 */
 	private class InposMonitorListener implements MonitorListener {
 		@Override
 		public void monitorChanged(MonitorEvent arg0) {
 			double value = -1.0;
 			DBR dbr = arg0.getDBR();
 			if (dbr.isDOUBLE()) {
 				value = ((DBR_Double) dbr).getDoubleValue()[0];
 			}
 			if (value == 1.0) {
 				synchronized (lock) {
 					positionerStatus = EnumPositionerStatus.IDLE;
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * DMOV monitor
 	 */
 	private class DmovMonitorListener implements MonitorListener {
 		@Override
 		public void monitorChanged(MonitorEvent arg0) {
 			double value = -1.0;
 			DBR dbr = arg0.getDBR();
 			if (dbr.isDOUBLE()) {
 				value = ((DBR_Double) dbr).getDoubleValue()[0];
 			}
 			if (value == 0.0) {
 				synchronized (lock) {
 					positionerStatus = EnumPositionerStatus.MOVING;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Error Monitor
 	 */
 	private class ErrorMonitorListener implements MonitorListener {
 		@Override
 		public void monitorChanged(MonitorEvent arg0) {
 			double value = -1.0;
 			DBR dbr = arg0.getDBR();
 			if (dbr.isDOUBLE()) {
 				value = ((DBR_Double) dbr).getDoubleValue()[0];
 			}
 			if (value != 0.0) {
 				synchronized (lock) {
 					positionerStatus = EnumPositionerStatus.ERROR;
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * @return epicsRecordName
 	 */
 	public String getEpicsRecordName() {
 		return epicsRecordName;
 	}
 
 	/**
 	 * @param epicsRecordName
 	 */
 	public void setEpicsRecordName(String epicsRecordName) {
 		this.epicsRecordName = epicsRecordName;
 	}
 
 	/**
 	 * @return deviceName
 	 */
 	public String getDeviceName() {
 		return deviceName;
 	}
 
 	/**
 	 * @param deviceName
 	 */
 	public void setDeviceName(String deviceName) {
 		this.deviceName = deviceName;
 	}
 
 	private class PutCallbackListener implements PutListener {
 		volatile PutEvent event = null;
 
 		@Override
 		public void putCompleted(PutEvent ev) {
 			try {
 				logger.debug("caputCallback complete for {}", getName());
 				event = ev;
 
 				if (event.getStatus() != CAStatus.NORMAL) {
 					logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
 							.getStatus());
 					positionerStatus=EnumPositionerStatus.ERROR;
 				} else {
 					logger.info("{} move done", getName());
 					positionerStatus=EnumPositionerStatus.IDLE;
 				}
 
 				if (status == Status.NO_ALARM && severity == Severity.NO_ALARM) {
 					logger.info("{} moves OK", getName());
 					positionerStatus=EnumPositionerStatus.IDLE;				
 				} else {
 					// if Alarmed, check and report MSTA status
 					logger.error("{} reports Alarm: {}", getName(), status);
 					positionerStatus=EnumPositionerStatus.ERROR;			
 				}
 
 			} catch (Exception ex) {
 				exceptionUtils.logException(logger, "Error in putCompleted for " + getName(), ex);
 			}
 		}
 	}
 }
