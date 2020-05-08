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
 
 import gda.device.DeviceException;
 import gda.epics.connection.EpicsController;
 import gda.epics.connection.STSHandler;
 import gda.epics.util.JCAUtils;
 import gda.factory.FactoryException;
 import gov.aps.jca.CAStatus;
 import gov.aps.jca.Channel;
 import gov.aps.jca.Monitor;
 import gov.aps.jca.event.ConnectionEvent;
 import gov.aps.jca.event.ConnectionListener;
 import gov.aps.jca.event.MonitorEvent;
 import gov.aps.jca.event.MonitorListener;
 import gov.aps.jca.event.PutEvent;
 import gov.aps.jca.event.PutListener;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class can be used when a pv can be set which is a choice of several values. For instance d1_gain on I20 uses
  * this kind of control. The choices are hard coded and must be defined with the values map. This map allows EPICs value
  * and command line GDA value to be different to save typing.
  */
 public class EpicsSimplePositioner extends EnumPositionerBase implements ConnectionListener, MonitorListener {
 
 	private static final Logger logger = LoggerFactory.getLogger(EpicsSimplePositioner.class);
 
 	protected String pvName;
 	protected EpicsController controller;
 	protected Channel currentPositionChnl;
 	private PutCallbackListener pcbl;
 	
 	private Map<String, String> values;
 	private Map<String, String> reverseValues;
 	protected HashSet<Channel> monitorInstalledSet;
 
 	public EpicsSimplePositioner(){
 		controller = EpicsController.getInstance();
 		pcbl = new PutCallbackListener();
 	}
 	@Override
 	public void configure() throws FactoryException {
 		
 		// cf. ScannableMotor.configure
 		this.inputNames = new String[] { getName() };
 		
 		this.outputFormat = new String[]{"%s"};
 
 		try {
 			if (!configured) {
 				monitorInstalledSet = new HashSet<Channel>(3);
 
 				currentPositionChnl = controller.createChannel(pvName, this);
 
 				configured = true;
 			} // end of if(!configured)
 
 		} catch (Throwable th) {
 			throw new FactoryException("failed to create channel " + pvName, th);
 		}
 	}
 
 	@Override
 	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
 
 		String positionString = position.toString();
 
 		// find in the positionNames array the index of the string
 		if (values.containsKey(positionString)) {
 			final String value = values.get(positionString);
 			try {
 				controller.caput(currentPositionChnl, value, pcbl);
 			} catch (Throwable th) {
 				throw new DeviceException("failed to moveTo", th);
 			}
 		} else {
 			// if get here then wrong position name supplied
 			throw new DeviceException("Position called: " + positionString + " not found.");
 		}
 	}
 	
 	@Override
 	public String getPosition() throws DeviceException {
 		try {
 			// int position = controller.cagetInt(currentPositionChnl);
 			String value = controller.cagetString(currentPositionChnl);
 			return reverseValues.get(value);
 		} catch (Throwable th) {
 			throw new DeviceException("failed to get position", th);
 		}
 	}
 
 	public String getPvName() {
 		return pvName;
 	}
 
 	@Override
 	public String checkPositionValid(Object position) {
 		if (!values.containsKey(position.toString())){
 			return position.toString() + "not in list of acceptable values";
 		}
 		return null;
 	}
 
 	public void setPvName(String pvName) {
 		this.pvName = pvName;
 	}
 
 	@Override
 	public void connectionChanged(ConnectionEvent ev) {
 		Channel ch = (Channel) ev.getSource();
 		boolean installMonitor = false;
 
 		if (ev.isConnected()) {
 			synchronized (monitorInstalledSet) {
 				installMonitor = !monitorInstalledSet.contains(ch);
 			}
 		}
 
 		// start a monitor on the first connection
 		if (installMonitor) {
 			try {
 				// Print some information
 				logger.info(JCAUtils.timeStamp() + " Search successful for: " + ch.getName());
 				// ch.printInfo();
 				// Add a monitor listener on every successful connection
 				// The following is commented out to solve scan pyException
 				// problem
 				// - need to track down the real cause.
				controller.setMonitor(ch, STSHandler.getSTSType(ch), Monitor.VALUE | Monitor.ALARM, this);
 
 				synchronized (monitorInstalledSet) {
 					monitorInstalledSet.add(ch);
 				}
 
 			} catch (Throwable ex) {
 				logger.error("Add Monitor failed for Channel: " + ch.getName() + " : " + ex);
 				return;
 			}
 		}
 
 		// print connection state
 		logger.info(JCAUtils.timeStamp() + " ");
 		if (ch.getConnectionState() == Channel.CONNECTED) {
 			logger.info(ch.getName() + " is connected");
 		} else if (ch.getConnectionState() == Channel.DISCONNECTED) {
 			logger.info(ch.getName() + " is disconnected");
 		} else if (ch.getConnectionState() == Channel.CLOSED) {
 			logger.info(ch.getName() + " is closed");
 		}
 	}
 
 	@Override
 	public void monitorChanged(MonitorEvent arg0) {
 		// no matter what message is received, send observers latest status
 		try {
 			if (currentPositionChnl != null && positions != null) {
				notifyIObservers(getPosition(), getStatus());
 			} else {
 				notifyIObservers(null, getStatus());
 			}
 		} catch (DeviceException e) {
 			logger.debug(e.getClass() + " while updating EpicsPositioner " + getName() + " : " + e.getMessage());
 		}
 
 	}
 
 	@Override
 	public String toFormattedString() {
 		try {
 			return getName() + " : " + getPosition() + " " + createFormattedListAcceptablePositions();
 		} catch (DeviceException e) {
 			return super.toString();
 		}
 	}
 
 	/**
 	 * @return Returns the values.
 	 */
 	public Map<String, String> getValues() {
 		return values;
 	}
 
 	/**
 	 * Map<String, String> - means <GDA name, EPICS name>
 	 * @param values
 	 *            The values to set.
 	 */
 	public void setValues(Map<String, String> values) {
 		this.values = values;
 		positions.addAll(values.keySet());
 		reverseValues = new HashMap<String, String>(3);
 		for (String key : values.keySet()) {
 			final String value = values.get(key);
 			reverseValues.put(value, key);
 		}
 	}
 	private class PutCallbackListener implements PutListener {
 		volatile PutEvent event = null;
 		@Override
 		public void putCompleted(PutEvent ev) {
 			logger.debug("caputCallback complete for {}", getName());
 			event = ev;
 
 			if (event.getStatus() != CAStatus.NORMAL) {
 				logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
 						.getStatus());
 			} else {
 				logger.info("{} move done", getName());
 			}
 		}
 		
 	}
 
 }
