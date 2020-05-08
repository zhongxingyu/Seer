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
 
 package gda.device.continuouscontroller;
 
 import gda.configuration.epics.ConfigurationNotFoundException;
 import gda.configuration.epics.Configurator;
 import gda.device.DeviceBase;
 import gda.device.DeviceException;
 import gda.epics.connection.EpicsChannelManager;
 import gda.epics.connection.EpicsController;
 import gda.epics.connection.InitializationListener;
 import gda.epics.interfaces.TrajectoryScanType;
 import gda.factory.Configurable;
 import gda.factory.FactoryException;
 import gda.factory.Findable;
 import gda.util.OutOfRangeException;
 import gov.aps.jca.CAException;
 import gov.aps.jca.Channel;
 import gov.aps.jca.TimeoutException;
 import gov.aps.jca.dbr.DBR;
 import gov.aps.jca.dbr.DBR_Enum;
 import gov.aps.jca.dbr.DBR_String;
 import gov.aps.jca.event.MonitorEvent;
 import gov.aps.jca.event.MonitorListener;
 
 import java.text.MessageFormat;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class provides support for EPICS EpicsTrajectoryScanController. A trajectory scan allows fine control of
  * movement using an XPS motor controller. It allows a given number of pulses to be output from the controller, spaced
  * evenly over a set time frame. These pulses are used to control MCA data acquisition module. Operations in trajectory
  * scan involves four steps:
  * <ol>
  * <li>Setup or configure</li>
  * <li>Build</li>
  * <li>Execute</li>
  * <li>Read</li>
  * </ol>
  * Detector data collection will be handled by MCA object.
  */
 public class EpicsTrajectoryScanControllerDev812 extends DeviceBase implements TrajectoryScanControllerDev812, InitializationListener, Configurable, Findable {
 
 	private static final Logger logger = LoggerFactory.getLogger(EpicsTrajectoryScanControllerDev812.class);
 
 	/**
 	 * /** Maximum array size of the defined trajectory path
 	 */
 	public int MAXIMUM_ELEMENT_NUMBER = 3500;
 
 	/**
 	 * Maximum array size of the output pulses or the data points collected during the trajectory scan.
 	 */
 	public int MAXIMUM_PULSE_NUMBER = 60000;
 	
 	/**
 	 * Maximum number of motor permitted to participate in this trajectory scan
 	 */
 	public static int MAXIMUM_MOTOR_NUMBER = 8;
 
 	private static double BUILD_TIMEOUT = 60;//s
 
 	/**
 	 * Time to move to start, run up, run down and other execute overhead
 	 */
 	private static double EXECUTE_OVERHEAD_TIMEOUT = 120;// s
 
 	private static double READ_TIMEOUT = 60; //s
 
 	
 	private Channel nelm; // Number of element in trajectory pv
 
 	private Channel npulses; // number of output pulses pv
 
 	private Channel spulses; // element number to start pulse pv
 
 	private Channel epulses; // element number to end pulses pv
 
 	private Channel apulses; // actual number of output pulses pv
 
 	private Channel time; // trajectory time mbbinary
 
 	private Channel[] mmove = new Channel[MAXIMUM_MOTOR_NUMBER]; // move Mx motor? binary
 
 	private Channel[] mtraj= new Channel[MAXIMUM_MOTOR_NUMBER]; // Mx increments array
 
 	private Channel build; // build and check trajectory PV
 
 	@SuppressWarnings("unused")
 	private Channel bstate; // trajectory build state mbbinary
 
 	private Channel bstatus; // trajectory build status mbbinary
 
 	@SuppressWarnings("unused")
 	private Channel bmess; // trajectory build message mbbinary
 
 	private Channel execute; // start trajectory motion PV
 
 	@SuppressWarnings("unused")
 	private Channel estate; // trajectory execute state mbbinary
 
 	private Channel estatus; // trajectory execute status mbbinary
 
 	@SuppressWarnings("unused")
 	private Channel emess; // trajectory execute message mbbinary
 
 	private Channel abort; // abort trajectory motion PV
 
 	private Channel read; // read back actual positions PV
 
 	@SuppressWarnings("unused")
 	private Channel rstate; // read back state mbbinary 
 
 	private Channel rstatus; // read back status mbbinary
 
 	@SuppressWarnings("unused")
 	private Channel rmess; // read back message mbbinary
 
 	
 	private Channel[] mactual= new Channel[MAXIMUM_MOTOR_NUMBER]; // Mx actual positions array
 	
 	private Channel[] mname= new Channel[MAXIMUM_MOTOR_NUMBER]; // Mx name
 
 	private String deviceName;
 
 	private EpicsController controller;
 
 	/**
 	 * EPICS Channel Manager
 	 */
 	private EpicsChannelManager channelManager;
 
 	//private BuildStatusListener bsl;
 
 	//private ExecuteStatusListener esl;
 
 	//private ReadStatusListener rsl;
 
 	private BuildStateListener bstatel;
 
 	private ExecuteStateListener estatel;
 
 	private ReadStateListener rstatel;
 
 	private BuildMessageListener bml;
 
 	private ExecuteMessageListener eml;
 
 	private ReadMessageListener rml;
 
 	private String buildState = "UNDEFINED";
 
 	private String buildMessage = "not set in EPICS";
 
 	private String executeState = "UNDEFINED";
 
 	private String executeMessage = "not set in EPICS";
 
 	private String readState = "UNDEFINED";
 
 	private String readMessage = "not set in EPICS";
 
 	
 	/*
 	 * private static EpicsTrajectoryScanController instance = null; public static EpicsTrajectoryScanController
 	 * getInstance() { if(instance == null) { instance = new EpicsTrajectoryScanController(); } return instance; }
 	 */
 
 	/**
 	 * default constructor
 	 */
 	public EpicsTrajectoryScanControllerDev812() {
 		controller = EpicsController.getInstance();
 		channelManager = new EpicsChannelManager(this);
 		bstatel = new BuildStateListener();
 		estatel = new ExecuteStateListener();
 		rstatel = new ReadStateListener();
 		bml = new BuildMessageListener();
 		eml = new ExecuteMessageListener();
 		rml = new ReadMessageListener();
 	}
 
 	/**
 	 * Initialise the trajectory scan object.
 	 * 
 	 * @throws FactoryException
 	 */
 	@Override
 	public void configure() throws FactoryException {
 		if (!configured) {
 			// EPICS interface version 2 for phase II beamlines.
 			if (getDeviceName() != null) {
 				TrajectoryScanType tsConfig;
 				try {
 					tsConfig = Configurator
 							.getConfiguration(getDeviceName(), gda.epics.interfaces.TrajectoryScanType.class);
 					createChannelAccess(tsConfig);
 					channelManager.tryInitialize(100);
 				} catch (ConfigurationNotFoundException e) {
 					logger.error("Can NOT find EPICS configuration for motor " + getDeviceName(), e);
 				}
 			}
 			// Nothing specified in Server XML file
 			else {
 				logger.error("Missing EPICS configuration for trajectory scan {}", getName());
 				throw new FactoryException("Missing EPICS interface configuration for the scan: " + getName());
 			}
 			configured = true;
 		}// end of if (!configured)
 	}
 
 	/**
 	 * create channel access implementing phase II beamline EPICS interfaces.
 	 * 
 	 * @param tsConfig
 	 * @throws FactoryException
 	 */
 	private void createChannelAccess(TrajectoryScanType tsConfig) throws FactoryException {
 		try {
 			nelm = channelManager.createChannel(tsConfig.getNELM().getPv(), false);
 			npulses = channelManager.createChannel(tsConfig.getNPULSES().getPv(), false);
 			spulses = channelManager.createChannel(tsConfig.getSPULSES().getPv(), false);
 			epulses = channelManager.createChannel(tsConfig.getEPULSES().getPv(), false);
 			apulses = channelManager.createChannel(tsConfig.getAPULSES().getPv(), false);
 			time = channelManager.createChannel(tsConfig.getTIME().getPv(), false);
 
 			
 			mmove[0] = channelManager.createChannel(tsConfig.getM1MOVE().getPv(), false);
 			mmove[1] = channelManager.createChannel(tsConfig.getM2MOVE().getPv(), false);
 			mmove[2] = channelManager.createChannel(tsConfig.getM3MOVE().getPv(), false);
 			mmove[3] = channelManager.createChannel(tsConfig.getM4MOVE().getPv(), false);
 			mmove[4] = channelManager.createChannel(tsConfig.getM5MOVE().getPv(), false);
 			mmove[5] = channelManager.createChannel(tsConfig.getM6MOVE().getPv(), false);
 			mmove[6] = channelManager.createChannel(tsConfig.getM7MOVE().getPv(), false);
 			mmove[7] = channelManager.createChannel(tsConfig.getM8MOVE().getPv(), false);
 			
 			build = channelManager.createChannel(tsConfig.getBUILD().getPv(), false);
 			bstate = channelManager.createChannel(tsConfig.getBSTATE().getPv(), bstatel, false);
 			bstatus = channelManager.createChannel(tsConfig.getBSTATUS().getPv(), false);
 			bmess = channelManager.createChannel(tsConfig.getBMESS().getPv(), bml, false);
 			
 			execute = channelManager.createChannel(tsConfig.getEXECUTE().getPv(), false);
 			estate = channelManager.createChannel(tsConfig.getESTATE().getPv(), estatel, false);
 			estatus = channelManager.createChannel(tsConfig.getESTATUS().getPv(), false);
 			emess = channelManager.createChannel(tsConfig.getEMESS().getPv(), eml, false);
 			
 			abort = channelManager.createChannel(tsConfig.getABORT().getPv(), false);
 			
 			read = channelManager.createChannel(tsConfig.getREAD().getPv(), rstatel, false);
 			rstate = channelManager.createChannel(tsConfig.getRSTATE().getPv(), false);
 			rstatus = channelManager.createChannel(tsConfig.getRSTATUS().getPv(), false);
 			rmess = channelManager.createChannel(tsConfig.getRMESS().getPv(), rml, false);
 			
 			mactual[0] = channelManager.createChannel(tsConfig.getM1ACTUAL().getPv(), false);
 			mactual[1] = channelManager.createChannel(tsConfig.getM2ACTUAL().getPv(), false);
 			mactual[2] = channelManager.createChannel(tsConfig.getM3ACTUAL().getPv(), false);
 			mactual[3] = channelManager.createChannel(tsConfig.getM4ACTUAL().getPv(), false);
 			mactual[4] = channelManager.createChannel(tsConfig.getM5ACTUAL().getPv(), false);
 			mactual[5] = channelManager.createChannel(tsConfig.getM6ACTUAL().getPv(), false);
 			mactual[6] = channelManager.createChannel(tsConfig.getM7ACTUAL().getPv(), false);
 			mactual[7] = channelManager.createChannel(tsConfig.getM8ACTUAL().getPv(), false);
 
 			mtraj[0] = channelManager.createChannel(tsConfig.getM1TRAJ().getPv(), false);
 			mtraj[1] = channelManager.createChannel(tsConfig.getM2TRAJ().getPv(), false);
 			mtraj[2] = channelManager.createChannel(tsConfig.getM3TRAJ().getPv(), false);
 			mtraj[3] = channelManager.createChannel(tsConfig.getM4TRAJ().getPv(), false);
 			mtraj[4] = channelManager.createChannel(tsConfig.getM5TRAJ().getPv(), false);
 			mtraj[5] = channelManager.createChannel(tsConfig.getM6TRAJ().getPv(), false);
 			mtraj[6] = channelManager.createChannel(tsConfig.getM7TRAJ().getPv(), false);
 			mtraj[7] = channelManager.createChannel(tsConfig.getM8TRAJ().getPv(), false);
 			
 			mname[0] = channelManager.createChannel(tsConfig.getM1NAME().getPv(), false);
 			mname[1] = channelManager.createChannel(tsConfig.getM2NAME().getPv(), false);
 			mname[2] = channelManager.createChannel(tsConfig.getM3NAME().getPv(), false);
 			mname[3] = channelManager.createChannel(tsConfig.getM4NAME().getPv(), false);
 			mname[4] = channelManager.createChannel(tsConfig.getM5NAME().getPv(), false);
 			mname[5] = channelManager.createChannel(tsConfig.getM6NAME().getPv(), false);
 			mname[6] = channelManager.createChannel(tsConfig.getM7NAME().getPv(), false);
 			mname[7] = channelManager.createChannel(tsConfig.getM8NAME().getPv(), false);
 			
 			channelManager.creationPhaseCompleted();
 			configured = true;
 		} catch (Throwable th) {
 			throw new FactoryException(getName() + " failed to create required Epics channels", th);
 		}
 	}
 
 	public int getMaximumNumberMotors() {
 		return MAXIMUM_MOTOR_NUMBER;
 	}
 	
 	@Override
 	public void setMTraj(int motor, double[] path) throws DeviceException, InterruptedException {
 		try {
 			controller.caputWait(mtraj[motor-1], path);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem setting motor " + motor + " path", e);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics CA problem setting motor " + motor + " path", e);
 		}
 	}
 
 
 	@Override
 	public double[] getMTraj(int motor) throws DeviceException, InterruptedException {
 		try {
 			return controller.cagetDoubleArray(mtraj[motor-1]);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics timeout getting motor " + motor + " path", e);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem getting motor " + motor + " path", e);
 		}
 	}
 
 
 	@Override
 	public void setMMove(int motor, boolean b) throws DeviceException, InterruptedException {
 		try {
 			controller.caputWait(mmove[motor-1], b ? 1 : 0);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem enabling motor " + motor, e);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics CA problem enabling motor " + motor, e);
 		}
 	}
 
 
 	@Override
 	public boolean isMMove(int motor) throws CAException, TimeoutException, InterruptedException {
		short ordinate = controller.cagetEnum(mmove[motor-1]);
		return ordinate == 1;
 	}
 
 	@Override
 	public void setNumberOfElements(int value) throws DeviceException, OutOfRangeException, InterruptedException {
 		if (value > MAXIMUM_ELEMENT_NUMBER || value <= 0) {
 			throw new OutOfRangeException("Input value " + value + " is out of range 0 - " + MAXIMUM_ELEMENT_NUMBER);
 		}
 		try {
 			controller.caputWait(nelm, value);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem setting number of elements to use", e);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics CA problem setting number of elements to use", e);
 		}
 	}
 
 	@Override
 	public int getNumberOfElements() throws DeviceException, InterruptedException {
 		try {
 			return controller.cagetInt(nelm);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics timeout getting number of elements to use", e);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem getting number of elements to use", e);
 		}
 	}
 
 	@Override
 	public void setNumberOfPulses(int value) throws DeviceException, OutOfRangeException, InterruptedException {
 		if (value > MAXIMUM_PULSE_NUMBER || value <= 0) {
 			throw new OutOfRangeException("Input value " + value + " is out of range 0 - " + MAXIMUM_PULSE_NUMBER);
 		}
 		try {
 			controller.caputWait(npulses, value);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem setting number of output pulses", e);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics CA problem setting number of output pulses", e);
 		} 
 	}
 
 	@Override
 	public int getNumberOfPulses() throws DeviceException, InterruptedException {
 		try {
 			return controller.cagetInt(npulses);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics timeout getting number of output pulses", e);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem getting number of output pulses", e);
 		}
 	}
 
 	@Override
 	public void setStartPulseElement(int n) throws DeviceException, InterruptedException {
 		try {
 			controller.caputWait(spulses, n);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem setting start pulse element ", e);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics CA problem setting start pulse element ", e);
 		}
 	}
 
 	@Override
 	public int getStartPulseElement() throws DeviceException, InterruptedException {
 		try {
 			return controller.cagetInt(spulses);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics timeout getting start pulse element ", e);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem getting start pulse element ", e);
 		}
 	}
 
 	@Override
 	public void setStopPulseElement(int n) throws DeviceException, InterruptedException {
 		try {
 			controller.caputWait(epulses, n);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem setting stop pulse element ", e);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics CA problem getting start pulse element ", e);
 		}
 	}
 	
 	@Override
 	public int getStopPulseElement() throws DeviceException, InterruptedException {
 		try {
 			return controller.cagetInt(epulses);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics timeout getting stop pulse element ", e);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem getting stop pulse element ", e);
 		}
 	}
 
 	
 	@Override
 	public void setTrajectoryTime(double seconds) throws DeviceException, InterruptedException {
 		try {
 			controller.caputWait(time, seconds);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem setting the trajectory time ", e);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics CA problem setting the trajectory time ", e);
 		}
 	}
 
 	@Override
 	public double getTrajectoryTime() throws DeviceException, InterruptedException {
 		try {
 			return controller.cagetDouble(time);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics time out getting the trajectory time ", e);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem getting the trajectory time ", e);
 		}
 	}
 
 	public void build() throws DeviceException, InterruptedException{
 		
 		try {
 			logger.debug("{} building trajectory", getName());
 			controller.caput(build, 1, BUILD_TIMEOUT);
 			logger.debug("{} building trajectory complete", getName());
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem while building the trajectory", e);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics time out while building the trajectory", e);
 		}
 		BuildStatus initialbuildStatus = getBuildStatus();
 		if (initialbuildStatus == BuildStatus.SUCCESS) {
 			logger.info("{} build complete, state='{}', status='{}', message='{}'", new Object[] { getName(),
 					getBuildState(), getBuildStatus(), getBuildMessage() });
 		} else {
 			String msg = MessageFormat.format(
 					"Trajectory build *failed* in Epics: state=''{0}'', status=''{1}'', message=''{2}''",
 					getBuildState(), getBuildStatus(), getBuildMessage());
 			logger.error(msg);
 			throw new DeviceException(msg);
 		}
 	}
 
 		
 	@Override
 	public String checkBuildOkay() {
 		return "deprecated";
 	}
 
 	@Override
 	public void execute() throws DeviceException, InterruptedException{
 		logger.info("{} executing trajectory move", getName());
 		try {
 			controller.caput(execute, 1, getTrajectoryTime() + EXECUTE_OVERHEAD_TIMEOUT);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem while executing the trajectory move", e);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Time out while executing the trajectory move", e);
 		}
 		if (getExecuteStatus() == ExecuteStatus.SUCCESS) {
 			logger.info("{} move complete, state=''{}'', status=''{}'', message=''{}''", new Object[] { getName(),
 					getExecuteState(), getExecuteStatus(), getExecuteMessage() });
 		} else {
 			String msg = MessageFormat.format(
 					"Trajectory execute move *failed*: state=''{0}'', status=''{1}'', message=''{2}''",
 					getExecuteState(), getExecuteStatus(), getExecuteMessage());
 			logger.error(msg);
 			throw new DeviceException(msg);
 		}
 	}
 	
 	@Override
 	public String checkExecuteOkay() {
 		return "Deprecated";
 	}
 	
 	@Override
 	public void read() throws DeviceException, InterruptedException {
 		try {
 			if (controller.cagetEnum(read) == 1) {
 				logger.warn("{} has already read actual positions up and is greyed out", getName());
 				return;
 			}
 			logger.info("{} reading actual positions", getName());
 			controller.caput(read, 1, READ_TIMEOUT);
 			logger.debug("{} reading actual positions complete", getName());
 			
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem while reading up actual positions", e);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics timeout while reading up actual positions", e);
 		}
 		if (getReadStatus() == ReadStatus.SUCCESS) {
 			logger.info("{} read complete, state='{}', status='{}', message='{}'", new Object[] { getName(),
 					getReadState(), getReadStatus(), getReadMessage() });
 		} else {
 			logger.info("{} initial read status was:''{0}''", getName(), getReadStatus());
 			String msg = MessageFormat.format(
 					"Read of real trajectory positions *failed*: state=''{0}'', status=''{1}'', message=''{2}''",
 					getReadState(), getReadStatus(), getReadMessage());
 			logger.error(msg);
 			throw new DeviceException(msg);
 		}
 	}
 
 	@Override
 	public String checkReadOkay() {
 		return "deprecated";
 	}
 	
 	@Override
 	public int getActualPulses() throws DeviceException, InterruptedException {
 		try {
 			return controller.cagetInt(apulses);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem getting actual pulses ", e);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics timeout getting actual pulses ", e);
 		}
 	}
 
 	@Override
 	public double[] getMActual(int motor) throws DeviceException, InterruptedException {
 		try {
 			return controller.cagetDoubleArray(mactual[motor-1]);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics timeout getting actual path ", e);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem getting actual path ", e);
 		}
 	}
 
 	@Override
 	public String getMName(int motor) throws DeviceException, InterruptedException {
 		try {
 			return controller.cagetString(mname[motor-1]);
 		} catch (TimeoutException e) {
 			throw new DeviceException("Epics timeout getting motor name ", e);
 		} catch (CAException e) {
 			throw new DeviceException("Epics CA problem getting motor name ", e);
 		}
 	}
 
 	@Override
 	public void stop() throws DeviceException, InterruptedException {
 		try {
 			controller.caputWait(abort, 1);
 		} catch (CAException e) {
 			logger.error("Epics CA problem stopping trajectory move", e);
 			throw new DeviceException("Epics CA problem stopping trajectory move", e);
 		} catch (TimeoutException e) {
 			logger.error("Epics CA problem stopping trajectory move", e);
 			throw new DeviceException("Epics CA problem stopping trajectory move", e);
 		}
 	}
 
 	public String getDeviceName() {
 
 		return deviceName;
 	}
 
 	public void setDeviceName(String name) {
 		this.deviceName = name;
 	}
 
 	@Override
 	public void initializationCompleted() {
 		logger.info("EPICS trajectory Scan Controller {} is initialised", getName());
 	}
 
 	@Override
 	public int getMaximumNumberElements() {
 		return MAXIMUM_ELEMENT_NUMBER;
 	}
 
 	public void setMaximumNumberElements(int max) {
 		MAXIMUM_ELEMENT_NUMBER = max;
 	}
 
 
 	@Override
 	public int getMaximumNumberPulses() {
 		return MAXIMUM_PULSE_NUMBER;
 	}
 
 	public void setMaximumNumberPulses(int max) {
 		MAXIMUM_PULSE_NUMBER = max;
 	}
 	
 	
 	
 	public String getBuildMessage() {
 		return buildMessage;
 	}
 
 	public BuildStatus getBuildStatus() throws DeviceException, InterruptedException {
 		short value;
 		try {
 			value = controller.cagetEnum(bstatus);
 		} catch (TimeoutException e) {
 			throw new DeviceException("EpicsTimeout getting build status: " + e.getMessage(), e);
 		} catch (CAException e) {
 			throw new DeviceException("Problem getting build status: " + e.getMessage(), e);
 		}
 		logger.info("getBuildStatus got state: " + value);
 		return (new BuildStatus[] { BuildStatus.UNDEFINED, BuildStatus.SUCCESS, BuildStatus.FAILURE })[value];
 	}
 
 	public String getBuildState() {
 		return buildState;
 	}
 
 	public String getExecuteMessage() {
 		return executeMessage;
 	}
 
 	public ExecuteStatus getExecuteStatus() throws DeviceException, InterruptedException {
 		short value;
 		try {
 			value = controller.cagetEnum(estatus);
 		} catch (TimeoutException e) {
 			throw new DeviceException("EpicsTimeout getting execute status: " + e.getMessage(), e);
 		} catch (CAException e) {
 			throw new DeviceException("Problem getting execute status: " + e.getMessage(), e);
 		}
 		return (new ExecuteStatus[] {ExecuteStatus.UNDEFINED, ExecuteStatus.SUCCESS, ExecuteStatus.FAILURE, ExecuteStatus.ABORT, ExecuteStatus.TIMEOUT})[value];
 	}
 
 	public String getExecuteState() {
 		return executeState;
 	}
 
 	public String getReadMessage() {
 		return readMessage;
 	}
 
 	public ReadStatus getReadStatus() throws DeviceException, InterruptedException {
 		short value;
 		try {
 			value = controller.cagetEnum(rstatus);
 		} catch (TimeoutException e) {
 			throw new DeviceException("EpicsTimeout getting read status: " + e.getMessage(), e);
 		} catch (CAException e) {
 			throw new DeviceException("Problem getting read status: " + e.getMessage(), e);
 		}
 		return (new ReadStatus[] { ReadStatus.UNDEFINED, ReadStatus.SUCCESS, ReadStatus.FAILURE })[value];
 	}
 
 	public String getReadState() {
 		return readState;
 	}
 
 	private class BuildStateListener implements MonitorListener {
 
 		@Override
 		public void monitorChanged(MonitorEvent arg0) {
 			DBR dbr = arg0.getDBR();
 			int value = -1;
 			if (dbr.isENUM()) {
 				value = ((DBR_Enum) dbr).getEnumValue()[0];
 
 			} else {
 				logger.error("Error: expecting ENUM type but got " + dbr.getType() + " type.");
 			}
 			if (value == 0) {
 				buildState = "Done";
 			} else if (value == 1) {
 				buildState = "Busy";
 			} else {
 				logger.error("Trajectory Build reports UNKNOWN state value: {}", value);
 			}
 			logger.debug("Build state updated to: {}", buildState);
 		}
 	}
 
 	private class ExecuteStateListener implements MonitorListener {
 
 		@Override
 		public void monitorChanged(MonitorEvent arg0) {
 			DBR dbr = arg0.getDBR();
 			int value = -1;
 			if (dbr.isENUM()) {
 				value = ((DBR_Enum) dbr).getEnumValue()[0];
 
 			} else {
 				logger.error("Error: expecting ENUM type but got " + dbr.getType() + " type.");
 			}
 			if (value == 0) {
 				executeState = "Done";
 			} else if (value == 1) {
 				executeState = "Move start";
 			} else if (value == 2) {
 				executeState = "Executing";
 			} else if (value == 3) {
 				executeState = "Flyback";
 			} else {
 				logger.error("Trajectory Execute reports UNKNOWN state value: {}", value);
 			}
 			logger.debug("Execute state updated to: {}", executeState);
 		}
 	}
 
 	private class ReadStateListener implements MonitorListener {
 
 		@Override
 		public void monitorChanged(MonitorEvent arg0) {
 			DBR dbr = arg0.getDBR();
 			int value = -1;
 			if (dbr.isENUM()) {
 				value = ((DBR_Enum) dbr).getEnumValue()[0];
 
 			} else {
 				logger.error("Error: expecting ENUM type but got " + dbr.getType() + " type.");
 			}
 			if (value == 0) {
 				readState = "Done";
 			} else if (value == 1) {
 				readState = "Busy";
 			} else {
 				logger.error("Trajectory Read reports UNKNOWN state value: {}", value);
 			}
 			logger.debug("Read state updated to: {}", readState);
 		}
 	}
 
 	private class BuildMessageListener implements MonitorListener {
 
 		@Override
 		public void monitorChanged(MonitorEvent arg0) {
 			DBR dbr = arg0.getDBR();
 			if (dbr.isSTRING()) {
 				buildMessage = ((DBR_String) dbr).getStringValue()[0];
 
 			} else {
 				logger.error("Error: expecting ENUM type but got " + dbr.getType() + " type.");
 			}
 			logger.debug("Build message updated to: {}", buildMessage);
 		}
 	}
 
 	private class ExecuteMessageListener implements MonitorListener {
 
 		@Override
 		public void monitorChanged(MonitorEvent arg0) {
 			DBR dbr = arg0.getDBR();
 			if (dbr.isSTRING()) {
 				executeMessage = ((DBR_String) dbr).getStringValue()[0];
 			} else {
 				logger.error("Error: expecting ENUM type but got " + dbr.getType() + " type.");
 			}
 			logger.debug("Execute message updated to: {}", executeMessage);
 		}
 	}
 
 	private class ReadMessageListener implements MonitorListener {
 		
 		@Override
 		public void monitorChanged(MonitorEvent arg0) {
 			DBR dbr = arg0.getDBR();
 			if (dbr.isSTRING()) {
 				readMessage = ((DBR_String) dbr).getStringValue()[0];
 			} else {
 				logger.error("Error: expecting ENUM type but got " + dbr.getType() + " type.");
 			}
 			logger.debug("Read message updated to: {}", readMessage);
 		}
 	}
 
 	@Override
 	public boolean isBusy() {
 		return (getBuildState().equals("Busy") || getExecuteState().equals("Busy") || getReadState().equals("isBusy"));
 	}
 }
