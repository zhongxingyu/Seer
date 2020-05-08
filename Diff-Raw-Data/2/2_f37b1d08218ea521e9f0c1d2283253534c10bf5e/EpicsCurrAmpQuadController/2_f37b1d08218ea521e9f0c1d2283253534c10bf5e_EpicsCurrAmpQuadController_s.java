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
 import gda.device.Monitor;
 import gda.device.Scannable;
 import gda.epics.connection.EpicsChannelManager;
 import gda.epics.connection.EpicsController;
 import gda.epics.connection.EpicsController.MonitorType;
 import gda.epics.connection.InitializationListener;
 import gda.epics.interfaces.CurrAmpQuadType;
 import gda.epics.interfaces.ElementType;
 import gda.factory.FactoryException;
 import gov.aps.jca.CAException;
 import gov.aps.jca.Channel;
 import gov.aps.jca.TimeoutException;
 import gov.aps.jca.dbr.DBR;
 import gov.aps.jca.dbr.DBRType;
 import gov.aps.jca.dbr.DBR_CTRL_Double;
 import gov.aps.jca.dbr.DBR_Enum;
 import gov.aps.jca.event.MonitorEvent;
 import gov.aps.jca.event.MonitorListener;
 
 import org.python.core.PyException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * EpicsCurrAmpQuadController Class
  */
 public class EpicsCurrAmpQuadController extends EnumPositionerBase implements Monitor, InitializationListener,
 		Scannable, EnumPositioner {
 
 	private static final Logger logger = LoggerFactory.getLogger(EpicsCurrAmpQuadController.class);
 
 	private String deviceName = null;
 
 	private EpicsChannelManager channelManager;
 
 	private EpicsController controller;
 
 	private boolean poll = false;
 
 	private volatile double current1 = Double.NaN;
 
 	private volatile double current2 = Double.NaN;
 
 	private volatile double current3 = Double.NaN;
 
 	private volatile double current4 = Double.NaN;
 
 	private Channel range = null;
 
 	private Object lock = new Object();
 
 	private Current1MonitorListener i1MonitorListener;
 
 	private Current2MonitorListener i2MonitorListener;
 
 	private Current3MonitorListener i3MonitorListener;
 
 	private Current4MonitorListener i4MonitorListener;
 
 	private gov.aps.jca.Monitor i1Monitor;
 
 	private gov.aps.jca.Monitor i2Monitor;
 
 	private gov.aps.jca.Monitor i3Monitor;
 
 	private gov.aps.jca.Monitor i4Monitor;
 
 	private RangeMonitorListener rangeMonitor;
 
 	private Channel current1Ch = null;
 
 	private Channel current2Ch = null;
 
 	private Channel current3Ch = null;
 
 	private Channel current4Ch = null;
 
 	private Channel range_rbv;
 
 	/**
 	 * Constructor
 	 */
 	public EpicsCurrAmpQuadController() {
 		
 		controller = EpicsController.getInstance();
 		channelManager = new EpicsChannelManager(this);
 		i1MonitorListener = new Current1MonitorListener();
 		i2MonitorListener = new Current2MonitorListener();
 		i3MonitorListener = new Current3MonitorListener();
 		i4MonitorListener = new Current4MonitorListener();
 		rangeMonitor = new RangeMonitorListener();
 		
 		setInputNames(new String[]{"rangeValue"});
 		setExtraNames(new String[]{ "current1", "current2", "current3", "current4"});
 
 		outputFormat = new String[inputNames.length + extraNames.length];
 
		for (int i = 0; i < outputFormat.length + 1; i++) {
 			outputFormat[i] = "%4.10f";
 		}
 		setOutputFormat(outputFormat);
 	}
 
 	/**
 	 * Configures the class with the PV information from the gda-interface.xml file. Vendor and model are available
 	 * through EPICS but are currently not supported in GDA.
 	 * 
 	 * @see gda.device.DeviceBase#configure()
 	 */
 	@Override
 	public void configure() throws FactoryException {
 		//String rangeRec = null;
 		if (!configured) {
 			if (getDeviceName() != null) {
 				CurrAmpQuadType currAmpConfig;
 				try {
 					currAmpConfig = Configurator.getConfiguration(getDeviceName(),
 							gda.epics.interfaces.CurrAmpQuadType.class);
 					createChannelAccess(currAmpConfig);
 					channelManager.tryInitialize(100);
 				} catch (ConfigurationNotFoundException e) {
 					logger.error("Can NOT find EPICS configuration for current amplifier quad " + getDeviceName(), e);
 				}
 			} else {
 				logger.error("Missing EPICS interface configuration for the current amplifier quad " + getName());
 				throw new FactoryException("Missing EPICS interface configuration for the current amplifier quad "
 						+ getName());
 			}
 			configured = true;
 		}// end of if (!configured)
 	}
 
 	/**
 	 * @return device name
 	 */
 	public String getDeviceName() {
 		return deviceName;
 	}
 
 	/**
 	 * @param name
 	 */
 	public void setDeviceName(String name) {
 		this.deviceName = name;
 	}
 
 	private void createChannelAccess(CurrAmpQuadType currAmpConfig) throws FactoryException {
 		try {
 			range = channelManager.createChannel(currAmpConfig.getRANGE().getPv(), rangeMonitor, MonitorType.CTRL, false);
 			ElementType rangeReadback = currAmpConfig.getRANGE_READBACK();
 			if ((rangeReadback != null) && rangeReadback.getPv().isEmpty()) {
 				range_rbv = channelManager.createChannel(currAmpConfig.getRANGE_READBACK().getPv(), rangeMonitor, MonitorType.CTRL, true);
 			}
 			current1Ch = channelManager.createChannel(currAmpConfig.getI1().getPv(), false);
 			current2Ch = channelManager.createChannel(currAmpConfig.getI2().getPv(), false);
 			current3Ch = channelManager.createChannel(currAmpConfig.getI3().getPv(), false);
 			current4Ch = channelManager.createChannel(currAmpConfig.getI4().getPv(), false);
 			channelManager.creationPhaseCompleted();
 
 		} catch (Throwable th) {
 			// TODO take care of destruction
 			throw new FactoryException("failed to connect to all channels", th);
 		}
 	}
 
 	@Override
 	public void initializationCompleted() throws DeviceException {
 		// borrowed from EpicsPneumatic
 		String[] position = getPositions();
 		for (int i = 0; i < position.length; i++) {
 			if (position[i] != null || position[i] != "") {
 				super.positions.add(position[i]);
 			}
 		}
 		
 		if (isPoll()) {
 			disablePoll();
 		} else {
 			enablePoll();
 		}
 		logger.info("{} initialisation completed", getName());
 	}
 	
 	public void disablePoll() {
 		setPoll(false);
 		if (current1Ch != null && i1MonitorListener != null) {
 			try {
 				i1Monitor = current1Ch.addMonitor(DBRType.DOUBLE, 0, gov.aps.jca.Monitor.VALUE, i1MonitorListener);
 			} catch (IllegalStateException e) {
 				logger.error("Fail to add monitor to channel " + current1Ch.getName(), e);
 			} catch (CAException e) {
 				logger.error("Fail to add monitor to channel " + current1Ch.getName(), e);
 			}
 		}
 		if (current2Ch != null && i2MonitorListener != null) {
 			try {
 				i2Monitor = current2Ch.addMonitor(DBRType.DOUBLE, 0, gov.aps.jca.Monitor.VALUE, i2MonitorListener);
 			} catch (IllegalStateException e) {
 				logger.error("Fail to add monitor to channel " + current2Ch.getName(), e);
 			} catch (CAException e) {
 				logger.error("Fail to add monitor to channel " + current2Ch.getName(), e);
 			}
 		}
 		if (current3Ch != null && i3MonitorListener != null) {
 			try {
 				i3Monitor = current3Ch.addMonitor(DBRType.DOUBLE, 0, gov.aps.jca.Monitor.VALUE, i3MonitorListener);
 			} catch (IllegalStateException e) {
 				logger.error("Fail to add monitor to channel " + current3Ch.getName(), e);
 			} catch (CAException e) {
 				logger.error("Fail to add monitor to channel " + current3Ch.getName(), e);
 			}
 		}
 		if (current4Ch != null && i4MonitorListener != null) {
 			try {
 				i4Monitor = current4Ch.addMonitor(DBRType.DOUBLE, 0, gov.aps.jca.Monitor.VALUE, i4MonitorListener);
 			} catch (IllegalStateException e) {
 				logger.error("Fail to add monitor to channel " + current4Ch.getName(), e);
 			} catch (CAException e) {
 				logger.error("Fail to add monitor to channel " + current4Ch.getName(), e);
 			}
 		}
 	}
 
 	public void enablePoll() {
 		setPoll(true);
 		if (i1Monitor != null && i1MonitorListener != null) {
 			i1Monitor.removeMonitorListener(i1MonitorListener);
 		}
 		if (i2Monitor != null && i2MonitorListener != null) {
 			i2Monitor.removeMonitorListener(i2MonitorListener);
 		}
 		if (i3Monitor != null && i3MonitorListener != null) {
 			i3Monitor.removeMonitorListener(i3MonitorListener);
 		}
 		if (i4Monitor != null && i4MonitorListener != null) {
 			i4Monitor.removeMonitorListener(i4MonitorListener);
 		}
 	}
 
 	@Override
 	public String[] getPositions() throws DeviceException{
 		String[] positionLabels = new String[positions.size()];
 		try {
 			positionLabels = controller.cagetLabels(range);
 		} catch (Exception e) {
 			throw new DeviceException(getName() + " exception in getPositions",e);
 		}
 		return positionLabels;
 	}
 
 	@Override
 	public int getElementCount() throws DeviceException {
 		return 5;
 	}
 
 	@Override
 	public String getUnit() throws DeviceException {
 		return null;
 	}
 
 	@Override
 	public EnumPositionerStatus getStatus() throws DeviceException {
 		return positionerStatus;
 	}
 
 	@Override
 	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
 		// EpicsPositioner moveTo
 		// find in the positionNames array the index of the string
 		if (positions.contains(position.toString())) {
 			int target = positions.indexOf(position.toString());
 			try {
 				controller.caput(range, target, channelManager);
 			} catch (Throwable th) {
 				throw new DeviceException(range.getName() + " failed to moveTo " + position.toString(), th);
 			}
 			return;
 		}
 
 		// if get here then wrong position name supplied
 		throw new DeviceException("Position called: " + position.toString() + " not found.");
 	}
 	
 	// need independent monitors for each current value
 	private class Current1MonitorListener implements MonitorListener {
 		@Override
 		public void monitorChanged(MonitorEvent mev) {
 			DBR dbr = mev.getDBR();
 			if (dbr.isDOUBLE()) {
 				current1 = ((DBR_CTRL_Double) dbr).getDoubleValue()[0];
 				notifyIObservers(this, current1);
 			} else {
 				logger.error("Error: .RBV should return DOUBLE type value.");
 			}
 		}
 
 	}
 
 	private class Current2MonitorListener implements MonitorListener {
 		@Override
 		public void monitorChanged(MonitorEvent mev) {
 			DBR dbr = mev.getDBR();
 			if (dbr.isDOUBLE()) {
 				current2 = ((DBR_CTRL_Double) dbr).getDoubleValue()[0];
 				notifyIObservers(this, current2);
 			} else {
 				logger.error("Error: .RBV should return DOUBLE type value.");
 			}
 		}
 
 	}
 
 	private class Current3MonitorListener implements MonitorListener {
 		@Override
 		public void monitorChanged(MonitorEvent mev) {
 			DBR dbr = mev.getDBR();
 			if (dbr.isDOUBLE()) {
 				current3 = ((DBR_CTRL_Double) dbr).getDoubleValue()[0];
 				notifyIObservers(this, current3);
 			} else {
 				logger.error("Error: .RBV should return DOUBLE type value.");
 			}
 		}
 
 	}
 
 	private class Current4MonitorListener implements MonitorListener {
 		@Override
 		public void monitorChanged(MonitorEvent mev) {
 			DBR dbr = mev.getDBR();
 			if (dbr.isDOUBLE()) {
 				current4 = ((DBR_CTRL_Double) dbr).getDoubleValue()[0];
 
 				/*
 				 * synchronized (motorProperty) { motorProperty.put(MotorProperty.POSITION, new Double(
 				 * currentPosition)); notifyIObservers(this, motorProperty);
 				 * motorProperty.remove(MotorProperty.POSITION); }
 				 */
 				notifyIObservers(this, current4);
 			} else {
 				logger.error("Error: .RBV should return DOUBLE type value.");
 			}
 		}
 
 	}
 
 	private class RangeMonitorListener implements MonitorListener {
 		@Override
 		public void monitorChanged(MonitorEvent mev) {
 			short value = -1;
 			DBR dbr = mev.getDBR();
 			if (dbr.isENUM()) {
 				value = ((DBR_Enum) dbr).getEnumValue()[0];
 			}
 			if (value == 0) {
 				synchronized (lock) {
 					positionerStatus = EnumPositionerStatus.ERROR;
 				}
 			} else if (value == 1 || value == 3) {
 				synchronized (lock) {
 					positionerStatus = EnumPositionerStatus.IDLE;
 				}
 			} else if (value == 2 || value == 4) {
 				synchronized (lock) {
 					positionerStatus = EnumPositionerStatus.MOVING;
 				}
 			}
 		}
 	}
 
 	/**
 	 * @return range value
 	 * @throws DeviceException
 	 */
 	public String getRangeValue() throws DeviceException {
 		try {
 			short test = controller.cagetEnum(range);
 			return positions.get(test);
 		} catch (Throwable th) {
 			throw new DeviceException("failed to get position from " + range.getName(), th);
 		}
 	}
 
 	public String getRangeReadbackValue() throws DeviceException {
 		try {
 			short test = controller.cagetEnum(range_rbv);
 			return positions.get(test);
 		} catch (Throwable th) {
 			throw new DeviceException("failed to get position from " + range_rbv.getName(), th);
 		}
 	}
 	/**
 	 * @return current1
 	 * @throws DeviceException 
 	 */
 	public double getCurrent1() throws DeviceException {
 		if (isPoll()) {
 			try {
 				return controller.cagetDouble(current1Ch);
 			} catch (TimeoutException e) {
 				logger.error("Timeout Exception on get intensity from" + current1Ch.getName(), e);
 				throw new DeviceException("Timeout Exception on get intensity from" + current1Ch.getName(), e);
 			} catch (CAException e) {
 				logger.error("CAException on get intensity from" + current1Ch.getName(), e);
 				throw new DeviceException("CAException on get intensity from" + current1Ch.getName(), e);
 			} catch (InterruptedException e) {
 				logger.error("Interrupted Exception on get intensity from" + current1Ch.getName(), e);
 				throw new DeviceException("Interrupted Exception on get intensity from" + current1Ch.getName(), e);
 			}
 		}
 		return this.current1;
 	}
 
 	/**
 	 * @return current2
 	 * @throws DeviceException 
 	 */
 	public double getCurrent2() throws DeviceException {
 		if (isPoll()) {
 			try {
 				return controller.cagetDouble(current2Ch);
 			} catch (TimeoutException e) {
 				logger.error("Timeout Exception on get intensity from" + current2Ch.getName(), e);
 				throw new DeviceException("Timeout Exception on get intensity from" + current2Ch.getName(), e);
 			} catch (CAException e) {
 				logger.error("CAException on get intensity from" + current2Ch.getName(), e);
 				throw new DeviceException("CAException on get intensity from" + current2Ch.getName(), e);
 			} catch (InterruptedException e) {
 				logger.error("Interrupted Exception on get intensity from" + current2Ch.getName(), e);
 				throw new DeviceException("Interrupted Exception on get intensity from" + current2Ch.getName(), e);
 			}
 		}
 		return this.current2;
 	}
 
 	/**
 	 * @return current3
 	 * @throws DeviceException 
 	 */
 	public double getCurrent3() throws DeviceException {
 		if (isPoll()) {
 			try {
 				return controller.cagetDouble(current3Ch);
 			} catch (TimeoutException e) {
 				logger.error("Timeout Exception on get intensity from" + current3Ch.getName(), e);
 				throw new DeviceException("Timeout Exception on get intensity from" + current3Ch.getName(), e);
 			} catch (CAException e) {
 				logger.error("CAException on get intensity from" + current3Ch.getName(), e);
 				throw new DeviceException("CAException on get intensity from" + current3Ch.getName(), e);
 			} catch (InterruptedException e) {
 				logger.error("Interrupted Exception on get intensity from" + current3Ch.getName(), e);
 				throw new DeviceException("Interrupted Exception on get intensity from" + current3Ch.getName(), e);
 			}
 		}
 		return this.current3;
 	}
 
 	/**
 	 * @return current4
 	 * @throws DeviceException 
 	 */
 	public double getCurrent4() throws DeviceException {
 		if (isPoll()) {
 			try {
 				return controller.cagetDouble(current4Ch);
 			} catch (TimeoutException e) {
 				logger.error("Timeout Exception on get intensity from" + current4Ch.getName(), e);
 				throw new DeviceException("Timeout Exception on get intensity from" + current4Ch.getName(), e);
 			} catch (CAException e) {
 				logger.error("CAException on get intensity from" + current4Ch.getName(), e);
 				throw new DeviceException("CAException on get intensity from" + current4Ch.getName(), e);
 			} catch (InterruptedException e) {
 				logger.error("Interrupted Exception on get intensity from" + current4Ch.getName(), e);
 				throw new DeviceException("Interrupted Exception on get intensity from" + current4Ch.getName(), e);
 			}
 		}
 		return this.current4;
 	}
 
 	@Override
 	public Object rawGetPosition() throws DeviceException {
 		String[] value = new String[5];
 		value[0] = String.format(getOutputFormat()[0], getCurrent1());
 		value[1] = String.format(getOutputFormat()[1], getCurrent2());
 		value[2] = String.format(getOutputFormat()[2], getCurrent3());
 		value[3] = String.format(getOutputFormat()[3], getCurrent4());
 		value[4] = getRangeValue();
 		return value;
 	}
 
 	@Override
 	public String toString() {
 		try {
 
 			// get the current position as an array of doubles
 			Object position = getPosition();
 
 			// if position is null then simply return the name
 			if (position == null) {
 				logger.warn("getPosition() from " + getName() + " returns NULL.");
 				return getName() + " : NOT AVAILABLE";
 			}
 
 			String[] positionAsArray = getCurrentPositionArray(this);
 
 			// if cannot create array of doubles then use position's toString
 			// method
 			if (positionAsArray == null || positionAsArray.length == 1) {
 				return getName() + " : " + position.toString();
 			}
 
 			// else build a string of formatted positions
 			String output = getName() + " : ";
 			int i = 0;
 			for (; i < this.inputNames.length; i++) {
 				output += this.inputNames[i] + ": " + positionAsArray[i] + " ";
 			}
 
 			for (int j = 0; j < this.extraNames.length; j++) {
 				output += this.extraNames[j] + ": " + positionAsArray[i + j] + " ";
 			}
 			return output.trim();
 
 		} catch (PyException e) {
 			logger.info(getName() + ": jython exception while getting position. " + e.toString());
 			return getName();
 		} catch (Exception e) {
 			logger.info(getName() + ": exception while getting position. " + e.getMessage() + "; " + e.getCause(), e);
 			return getName();
 		}
 	}
 
 	/**
 	 * converts object to String array
 	 * 
 	 * @param scannable
 	 * @return position array
 	 * @throws Exception
 	 */
 	public static String[] getCurrentPositionArray(Scannable scannable) throws Exception {
 
 		// get object returned by getPosition
 		Object currentPositionObj = scannable.getPosition();
 
 		// if its null or were expecting it to be null from the arrays, return
 		// null
 		if (currentPositionObj == null
 				|| (scannable.getInputNames().length == 0 && scannable.getExtraNames().length == 0)) {
 			return null;
 		}
 
 		// else create an array of the expected size and fill it
 		String[] currentPosition = new String[scannable.getInputNames().length + scannable.getExtraNames().length];
 		currentPosition = (String[]) currentPositionObj;
 
 		return currentPosition;
 
 	}
 
 	public boolean isPoll() {
 		return poll;
 	}
 
 	public void setPoll(boolean poll) {
 		this.poll = poll;
 	}
 }
