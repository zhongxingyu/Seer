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
 
 package gda.scan;
 
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
 import gda.jython.InterfaceProvider;
 import gda.jython.JythonServerFacade;
 import gda.util.OutOfRangeException;
 import gov.aps.jca.CAException;
 import gov.aps.jca.CAStatus;
 import gov.aps.jca.Channel;
 import gov.aps.jca.TimeoutException;
 import gov.aps.jca.dbr.DBR;
 import gov.aps.jca.dbr.DBR_Enum;
 import gov.aps.jca.dbr.DBR_String;
 import gov.aps.jca.event.MonitorEvent;
 import gov.aps.jca.event.MonitorListener;
 import gov.aps.jca.event.PutEvent;
 import gov.aps.jca.event.PutListener;
 
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
 public class EpicsTrajectoryScanController extends DeviceBase implements TrajectoryScanController,InitializationListener, Configurable, Findable {
 
 	private static final Logger logger = LoggerFactory.getLogger(EpicsTrajectoryScanController.class);
 
 	private String name = null;
 
 	// GDA names map to EPICS trajectory move axis for CASTOR config
 	
 	private String maxis[] = new String[MAX_TRAJECTORY];
 	
 	//TODO This is not used!
 	/**
 	 * the acceleration time for the motor
 	 */
 	private double accelerationTime = 1.0;
 
 	/**
 	 * /** Maximum array size of the defined trajectory path
 	 */
 	public static  int MAXIMUM_ELEMENT_NUMBER = 1500;
 
 	
 	public static int getMAXIMUM_ELEMENT_NUMBER() {
 		return MAXIMUM_ELEMENT_NUMBER;
 	}
 
 	public static void setMAXIMUM_ELEMENT_NUMBER(int mAXIMUMELEMENTNUMBER) {
 		MAXIMUM_ELEMENT_NUMBER = mAXIMUMELEMENTNUMBER;
 	}
 
 	/**
 	 * Maximum array size of the output pulses or the data points collected during the trajectory scan.
 	 */
 	public static int MAXIMUM_PULSE_NUMBER = 60000;
 
 	public static int getMAXIMUM_PULSE_NUMBER() {
 		return MAXIMUM_PULSE_NUMBER;
 	}
 
 	public static void setMAXIMUM_PULSE_NUMBER(int mAXIMUMPULSENUMBER) {
 		MAXIMUM_PULSE_NUMBER = mAXIMUMPULSENUMBER;
 	}
 
 	/**
 	 * Maximum number of motor permitted to participate in this trajectory scan
 	 */
 	public static final int MAXIMUM_MOTOR_NUMBER = 8;
 
 	private Channel nelm = null; // Number of element in trajectory pv
 
 	private Channel npulses = null; // number of output pulses pv
 
 	private Channel spulses = null; // element number to start pulse pv
 
 	private Channel epulses = null; // element number to end pulses pv
 
 	private Channel apulses = null; // actual number of output pulses pv
 
 	private Channel time = null; // trajectory time mbbinary
 	
 	private Channel mmove[]= new Channel[MAX_TRAJECTORY];
 	private Channel mtraj[] = new Channel[MAX_TRAJECTORY];	
 
 	private Channel build = null; // build and check trajectory PV
 
 	private Channel bstate = null; // trajectory build state mbbinary
 
 	private Channel bstatus = null; // trajectory build status mbbinary
 
 	private Channel bmess = null; // trajectory build message mbbinary
 
 	private Channel execute = null; // start trajectory motion PV
 
 	private Channel estate = null; // trajectory execute state mbbinary
 
 	private Channel estatus = null; // trajectory execute status mbbinary
 
 	private Channel emess = null; // trajectory execute message mbbinary
 
 	private Channel abort = null; // abort trajectory motion PV
 
 	private Channel read = null; // read back actual positions PV
 
 	private Channel rstate = null; // read back state mbbinary 
 
 	private Channel rstatus = null; // read back status mbbinary
 
 	private Channel rmess = null; // read back message mbbinary
 
 	private Channel mactual[] = new Channel[MAX_TRAJECTORY];
 
 	private Channel mname[] = new Channel[MAX_TRAJECTORY]; //actual positions array
 	
 	private Channel m2error = null; // M2 actual positions array
 
 	/**
 	 * GDA device Name
 	 */
 	private String deviceName = null;
 
 	/**
 	 * EPICS controller
 	 */
 	private EpicsController controller;
 
 	/**
 	 * EPICS Channel Manager
 	 */
 	private EpicsChannelManager channelManager;
 
 	private BuildStatusListener bsl;
 
 	private ExecuteStatusListener esl;
 
 	private ReadStatusListener rsl;
 
 	private BuildStateListener bstatel;
 
 	private ExecuteStateListener estatel;
 
 	private ReadStateListener rstatel;
 
 	private BuildMessageListener bml;
 
 	private ExecuteMessageListener eml;
 
 	private ReadMessageListener rml;
 
 	/**
 	 * EPICS Put call back handler
 	 */
 	private BuildCallbackListener bcbl;
 
 	protected boolean buildDone = false;
 
 	private ExecuteCallbackListener ecbl;
 
 	protected boolean executeDone = false;
 
 	private ReadCallbackListener rcbl;
 
 	protected boolean readDone = false;
 
 	private BuildStatus buildStatus = BuildStatus.UNDEFINED;
 
 	private String buildState = null;
 
 	private String buildMessage = "not set in EPICS";
 
 	
 
 	private ExecuteStatus executeStatus = ExecuteStatus.UNDEFINED;
 
 	private String executeState = null;
 
 	private String executeMessage = "not set in EPICS";
 
 	
 	private ReadStatus readStatus = ReadStatus.UNDEFINED;
 
 	private String readState = null;
 
 	private String readMessage = "not set in EPICS";
 
 	
 	/*
 	 * private static EpicsTrajectoryScanController instance = null; public static EpicsTrajectoryScanController
 	 * getInstance() { if(instance == null) { instance = new EpicsTrajectoryScanController(); } return instance; }
 	 */
 
 	/**
 	 * default constructor
 	 */
 	public EpicsTrajectoryScanController() {
 		controller = EpicsController.getInstance();
 		channelManager = new EpicsChannelManager(this);
 		bsl = new BuildStatusListener();
 		esl = new ExecuteStatusListener();
 		rsl = new ReadStatusListener();
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
 			bstatus = channelManager.createChannel(tsConfig.getBSTATUS().getPv(), bsl, false);
 			bmess = channelManager.createChannel(tsConfig.getBMESS().getPv(), bml, false);
 			execute = channelManager.createChannel(tsConfig.getEXECUTE().getPv(), false);
 			estate = channelManager.createChannel(tsConfig.getESTATE().getPv(), estatel, false);
 			estatus = channelManager.createChannel(tsConfig.getESTATUS().getPv(), esl, false);
 			emess = channelManager.createChannel(tsConfig.getEMESS().getPv(), eml, false);
 			abort = channelManager.createChannel(tsConfig.getABORT().getPv(), false);
 			read = channelManager.createChannel(tsConfig.getREAD().getPv(), rstatel, false);
 			rstate = channelManager.createChannel(tsConfig.getRSTATE().getPv(), false);
 			rstatus = channelManager.createChannel(tsConfig.getRSTATUS().getPv(), rsl, false);
 			rmess = channelManager.createChannel(tsConfig.getRMESS().getPv(), rml, false);
 			mactual[0] = channelManager.createChannel(tsConfig.getM1ACTUAL().getPv(), false);
 			mactual[1] = channelManager.createChannel(tsConfig.getM2ACTUAL().getPv(), false);
 			mactual[2] = channelManager.createChannel(tsConfig.getM3ACTUAL().getPv(), false);
 			mactual[3] = channelManager.createChannel(tsConfig.getM4ACTUAL().getPv(), false);
 			mactual[4] = channelManager.createChannel(tsConfig.getM5ACTUAL().getPv(), false);
 			mactual[5] = channelManager.createChannel(tsConfig.getM6ACTUAL().getPv(), false);
 			mactual[6] = channelManager.createChannel(tsConfig.getM7ACTUAL().getPv(), false);
 			mactual[7] = channelManager.createChannel(tsConfig.getM8ACTUAL().getPv(), false);
 			m2error = channelManager.createChannel("BL11I-MO-DIFF-01:TRAJ1:M2Error", false);
 
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
 			// acknowledge that creation phase is completed
 			channelManager.creationPhaseCompleted();
 			configured = true;
 		} catch (Throwable th) {
 			throw new FactoryException("failed to create reuqired channels", th);
 		}
 	}
 	
 	/**
 	 * sets trajectory path for motor 1.
 	 * 
 	 * @param value
 	 * @throws DeviceException
 	 */
 	@Override
 	public void setMTraj(int motorIndex, double[] value) throws DeviceException, InterruptedException{
 		try{
 		controller.caput(mtraj[motorIndex -1], value);
 		} catch (CAException e) {
 			logger.error("Error setting the M Traj " , e);
 			throw new DeviceException("Error setting the M Traj", e);
 		}
 	}
 
 	/**
 	 * sets trajectory path for motor 1.
 	 * 
 	 * @param value
 	 * @throws DeviceException
 	 */
 	public void setM1Traj(double[] value) throws DeviceException, InterruptedException {
 		try{
 		controller.caput(mtraj[0], value);
 		} catch (CAException e) {
 			logger.error("Error setting the M3 Traj " , e);
 			throw new DeviceException("Error setting the M3 Traj", e);
 		}
 	}
 
 	/**
 	 * sets trajectory path for motor 2.
 	 * 
 	 * @param value
 	 * @throws CAException
 	 * @throws InterruptedException 
 	 */
 	public void setM2Traj(double[] value) throws CAException, InterruptedException {
 		controller.caput(mtraj[1], value);
 	}
 
 	/**
 	 * sets trajectory path for motor 3.
 	 * 
 	 * @param value
 	 * @throws InterruptedException 
 	 */
 	
 	public void setM3Traj(double[] value) throws DeviceException, InterruptedException {
 		try {
 			controller.caput(mtraj[2], value);
 		} catch (CAException e) {
 			logger.error("Error setting the M3 Traj " , e);
 			throw new DeviceException("Error setting the M3 Traj", e);
 		}
 	}
 
 	/**
 	 * sets trajectory path for motor 4.
 	 * 
 	 * @param value
 	 * @throws CAException
 	 */
 	public void setM4Traj(double[] value) throws CAException, InterruptedException {
 		controller.caput(mtraj[3], value);
 	}
 
 	/**
 	 * sets trajectory path for motor 5.
 	 * 
 	 * @param value
 	 * @throws CAException
 	 */
 	public void setM5Traj(double[] value) throws CAException, InterruptedException {
 		controller.caput(mtraj[4], value);
 	}
 
 	/**
 	 * sets trajectory path for motor 6.
 	 * 
 	 * @param value
 	 * @throws CAException
 	 */
 	public void setM6Traj(double[] value) throws CAException, InterruptedException {
 		controller.caput(mtraj[5], value);
 	}
 
 	/**
 	 * sets trajectory path for motor 7.
 	 * 
 	 * @param value
 	 * @throws CAException
 	 */
 	public void setM7Traj(double[] value) throws CAException, InterruptedException {
 		controller.caput(mtraj[6], value);
 	}
 
 	/**
 	 * sets trajectory path for motor 8.
 	 * 
 	 * @param value
 	 * @throws CAException
 	 */
 	public void setM8Traj(double[] value) throws CAException, InterruptedException {
 		controller.caput(mtraj[7], value);
 	}
 
 	/**
 	 * gets the specified trajectory path for motor 1.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double[] getM1Traj() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(mtraj[0]);
 	}
 
 	/**
 	 * gets the specified trajectory path for motor 2.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 * @throws InterruptedException 
 	 */
 	public double[] getM2Traj() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(mtraj[1]);
 	}
 
 	/**
 	 * gets the specified trajectory path for motor 3.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double[] getM3Traj() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(mtraj[2]);
 	}
 
 	/**
 	 * gets the specified trajectory path for motor 4.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double[] getM4Traj() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(mtraj[3]);
 	}
 
 	/**
 	 * gets the specified trajectory path for motor 5.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double[] getM5Traj() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(mtraj[4]);
 	}
 
 	/**
 	 * gets the specified trajectory path for motor 6.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double[] getM6Traj() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(mtraj[5]);
 	}
 
 	/**
 	 * gets the specified trajectory path for motor 7.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double[] getM7Traj() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(mtraj[6]);
 	}
 
 	/**
 	 * gets the specified trajectory path for motor 8.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double[] getM8Traj() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(mtraj[7]);
 	}
 	/**
 	 * enable (true) or disable (false) trajectory move for motor .
 	 * 
 	 * @param b
 	 * @throws DeviceException
 	 * @throws InterruptedException 
 	 */
 	@Override
 	public void setMMove(int motorIndex, boolean b) throws DeviceException, InterruptedException {
 		try {if (b == true) {
 			
 				controller.caput(mmove[motorIndex - 1], 1);
 			
 		} else {
 			controller.caput(mmove[0], 0);
 		}
 		} catch (CAException e) {
 			logger.error("Error setting m1Move " , e);
 			throw new DeviceException("Error setting m1Move " , e);
 		}
 	}
 
 	/**
 	 * enable (true) or disable (false) trajectory move for motor 1.
 	 * 
 	 * @param b
 	 * @throws DeviceException
 	 * @throws InterruptedException 
 	 */
 	public void setM1Move(boolean b) throws DeviceException, InterruptedException {
 		try {if (b == true) {
 			
 				controller.caput(mmove[0], 1);
 			
 		} else {
 			controller.caput(mmove[0], 0);
 		}
 		} catch (CAException e) {
 			logger.error("Error setting m1Move " , e);
 			throw new DeviceException("Error setting m1Move " , e);
 		}
 	}
 
 	/**
 	 * enable (true) or disable (false) trajectory move for motor 2.
 	 * 
 	 * @param b
 	 * @throws DeviceException
 	 * @throws InterruptedException 
 	 */
 	public void setM2Move(boolean b) throws DeviceException, InterruptedException {
 		
 		try{
 			if (b == true) {
 			controller.caput(mmove[1], 1);
 		} else {
 			controller.caput(mmove[1], 0);
 		}
 	} catch (CAException e) {
 		logger.error("Error setting m1Move " , e);
 		throw new DeviceException("Error setting m1Move " , e);
 	}
 	}
 
 	/**
 	 * enable (true) or disable (false) trajectory move for motor 3.
 	 * 
 	 * @param b
 	 * @throws DeviceException
 	 * @throws InterruptedException 
 	 */
 	public void setM3Move(boolean b) throws DeviceException, InterruptedException {
 		try{
 		if (b == true) {
 			controller.caput(mmove[2], 1);
 		} else {
 			controller.caput(mmove[2], 0);
 		}
 	} catch (CAException e) {
 		logger.error("Error setting m1Move " , e);
 		throw new DeviceException("Error setting m1Move " , e);
 	}
 	}
 
 	/**
 	 * enable (true) or disable (false) trajectory move for motor 4.
 	 * 
 	 * @param b
 	 * @throws DeviceException
 	 * @throws InterruptedException 
 	 */
 	public void setM4Move(boolean b) throws DeviceException, InterruptedException {
 		try{
 		if (b == true) {
 			controller.caput(mmove[3], 1);
 		} else {
 			controller.caput(mmove[3], 0);
 		}
 	} catch (CAException e) {
 		logger.error("Error setting m1Move " , e);
 		throw new DeviceException("Error setting m1Move " , e);
 	}
 	}
 
 	/**
 	 * enable (true) or disable (false) trajectory move for motor 5.
 	 * 
 	 * @param b
 	 * @throws DeviceException
 	 * @throws InterruptedException 
 	 */
 	public void setM5Move(boolean b) throws DeviceException, InterruptedException {
 		try{
 		if (b == true) {
 			controller.caput(mmove[4], 1);
 		} else {
 			controller.caput(mmove[4], 0);
 		}
 	} catch (CAException e) {
 		logger.error("Error setting m1Move " , e);
 		throw new DeviceException("Error setting m1Move " , e);
 	}
 	}
 
 	/**
 	 * enable (true) or disable (false) trajectory move for motor 6.
 	 * 
 	 * @param b
 	 * @throws DeviceException
 	 * @throws InterruptedException 
 	 */
 	public void setM6Move(boolean b) throws DeviceException, InterruptedException {
 		
 		try{
 			if (b == true) {
 			controller.caput(mmove[5], 1);
 		} else {
 			controller.caput(mmove[5], 0);
 		}
 	} catch (CAException e) {
 		logger.error("Error setting m1Move " , e);
 		throw new DeviceException("Error setting m1Move " , e);
 	}
 	}
 
 	/**
 	 * enable (true) or disable (false) motor 7 move.
 	 * 
 	 * @param b
 	 * @throws DeviceException
 	 * @throws InterruptedException 
 	 */
 	public void setM7Move(boolean b) throws DeviceException, InterruptedException {
 		try{
 			if (b == true) {
 			controller.caput(mmove[6], 1);
 		} else {
 			controller.caput(mmove[6], 0);
 		}
 	} catch (CAException e) {
 		logger.error("Error setting m1Move " , e);
 		throw new DeviceException("Error setting m1Move " , e);
 	}
 	}
 
 	/**
 	 * enable (true) or disable (false) motor 8 move.
 	 * 
 	 * @param b
 	 * @throws DeviceException
 	 * @throws InterruptedException 
 	 */
 	public void setM8Move(boolean b) throws DeviceException, InterruptedException {
 		try{
 		if (b == true) {
 			controller.caput(mmove[7], 1);
 		} else {
 			controller.caput(mmove[7], 0);
 		}
 		} catch (CAException e) {
 			logger.error("Error setting m1Move " , e);
 			throw new DeviceException("Error setting m1Move " , e);
 		}
 	}
 
 	/**
 	 * query if motor 1 moves to its trajectory or no, return Yes or No.
 	 * 
 	 * @return true or false
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public boolean isM1Move() throws CAException, TimeoutException, InterruptedException {
 		String isMove = controller.cagetLabels(mmove[0])[0];
 		if (isMove.equalsIgnoreCase("Yes")) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * query if motor 2 moves to its trajectory or no, return Yes or No.
 	 * 
 	 * @return true or false
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public boolean isM2Move() throws CAException, TimeoutException, InterruptedException {
 		String isMove = controller.cagetLabels(mmove[1])[0];
 		if (isMove.equalsIgnoreCase("Yes")) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * query if motor 3 moves to its trajectory or no, return Yes or No.
 	 * 
 	 * @return true or false
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public boolean isM3Move() throws CAException, TimeoutException, InterruptedException {
 		String isMove = controller.cagetLabels(mmove[2])[0];
 		if (isMove.equalsIgnoreCase("Yes")) {
 			return true;
 		}
 		return false;
 
 	}
 
 	/**
 	 * query if motor 4 moves to its trajectory or no, return Yes or No.
 	 * 
 	 * @return true or false
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public boolean isM4Move() throws CAException, TimeoutException, InterruptedException {
 		String isMove = controller.cagetLabels(mmove[3])[0];
 		if (isMove.equalsIgnoreCase("Yes")) {
 			return true;
 		}
 		return false;
 
 	}
 
 	/**
 	 * query if motor 5 moves to its trajectory or no, return Yes or No.
 	 * 
 	 * @return true or false
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public boolean isM5Move() throws CAException, TimeoutException, InterruptedException {
 		String isMove = controller.cagetLabels(mmove[4])[0];
 		if (isMove.equalsIgnoreCase("Yes")) {
 			return true;
 		}
 		return false;
 
 	}
 
 	/**
 	 * query if motor 6 moves to its trajectory or no, return Yes or No.
 	 * 
 	 * @return true or false
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public boolean isM6Move() throws CAException, TimeoutException, InterruptedException {
 		String isMove = controller.cagetLabels(mmove[5])[0];
 		if (isMove.equalsIgnoreCase("Yes")) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * query if motor 7 moves to its trajectory or no, return Yes or No.
 	 * 
 	 * @return true or false
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public boolean isM7Move() throws CAException, TimeoutException, InterruptedException {
 		String isMove = controller.cagetLabels(mmove[6])[0];
 		if (isMove.equalsIgnoreCase("Yes")) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * query if motor 8 moves to its trajectory or no, return Yes or No.
 	 * 
 	 * @return true or false
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public boolean isM8Move() throws CAException, TimeoutException, InterruptedException {
 		String isMove = controller.cagetLabels(mmove[7])[0];
 		if (isMove.equalsIgnoreCase("Yes")) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * sets the number of elements in the trajectory to look at for all motors (maximum is 2000).
 	 * 
 	 * @param value
 	 * @throws DeviceException
 	 * @throws OutOfRangeException
 	 * @throws InterruptedException 
 	 */
 	@Override
 	public void setNumberOfElements(int value) throws DeviceException, OutOfRangeException, InterruptedException {
 		if (value > MAXIMUM_ELEMENT_NUMBER || value <= 0) {
 			throw new OutOfRangeException("Input value " + value + " is out of range 0 - " + MAXIMUM_ELEMENT_NUMBER);
 		}
 		try{
 		controller.caput(nelm, value);
 	} catch (CAException e) {
 		logger.error("Error setting m1Move " , e);
 		throw new DeviceException("Error setting m1Move " , e);
 	}
 	}
 
 	/**
 	 * gets number of elements in the defined trajectory from EPICS
 	 * 
 	 * @return total element number
 	 * @throws TimeoutException
 	 * @throws CAException
 	 * @throws InterruptedException 
 	 */
 	public int getNumberOfElements() throws TimeoutException, CAException, InterruptedException {
 		return controller.cagetInt(nelm);
 	}
 
 	/**
 	 * sets the number of output pulses for triggering the detector.
 	 * 
 	 * @param value
 	 * @throws DeviceException
 	 * @throws OutOfRangeException
 	 * @throws InterruptedException 
 	 */
 	@Override
 	public void setNumberOfPulses(int value) throws DeviceException, OutOfRangeException, InterruptedException {
 		if (value > MAXIMUM_PULSE_NUMBER || value <= 0) {
 			throw new OutOfRangeException("Input value " + value + " is out of range 0 - " + MAXIMUM_PULSE_NUMBER);
 		}
 		try{
 		controller.caput(npulses, value);
 	} catch (CAException e) {
 		logger.error("Error setting m1Move " , e);
 		throw new DeviceException("Error setting m1Move " , e);
 	}
 	}
 
 	/**
 	 * gets the number of pulses in the trajectory
 	 * 
 	 * @return number of pulses
 	 * @throws TimeoutException
 	 * @throws CAException
 	 * @throws InterruptedException 
 	 */
 	public int getNumberOfPulses() throws TimeoutException, CAException, InterruptedException {
 		return controller.cagetInt(npulses);
 	}
 
 	/**
 	 * sets the element number at which output pulses starts, i.e. the element that starts the triggering pulses.
 	 * 
 	 * @param value
 	 * @throws DeviceException
 	 * @throws InterruptedException 
 	 */
 	@Override
 	public void setStartPulseElement(int value) throws DeviceException, InterruptedException {
 		
 		try{controller.caput(spulses, value);
 	} catch (CAException e) {
 		logger.error("Error setting m1Move " , e);
 		throw new DeviceException("Error setting m1Move " , e);
 	}
 	}
 
 	/**
 	 * gets the elements number that starts the output pulses.
 	 * 
 	 * @return pulse-starting element
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public int getStartPulseElement() throws TimeoutException, CAException, InterruptedException {
 		return controller.cagetInt(spulses);
 	}
 
 	/**
 	 * sets the element number at which output pulses stops, i.e. the element that stops the triggering pulses.
 	 * 
 	 * @param value
 	 * @throws DeviceException
 	 * @throws InterruptedException 
 	 */
 	@Override
 	public void setStopPulseElement(int value) throws DeviceException, InterruptedException {
 		try{
 		controller.caput(epulses, value);
 	} catch (CAException e) {
 		logger.error("Error setting m1Move " , e);
 		throw new DeviceException("Error setting m1Move " , e);
 	}
 	}
 
 	/**
 	 * gets the element number that stops the ouptput pulses.
 	 * 
 	 * @return element where pulse stops
 	 * @throws TimeoutException
 	 * @throws DeviceException
 	 */
 	@Override
 	public int getStopPulseElement() throws TimeoutException, DeviceException {
 		try{
 		return controller.cagetInt(epulses);
 	} catch (CAException e) {
 		logger.error("Error getting Stop pulse element " , e);
 		throw new DeviceException("Error getting stop pulse element " , e);
 	} catch (InterruptedException e) {
 		throw new DeviceException("Error getting stop pulse element " , e);
 	}
 	}
 
 	/**
 	 * sets the trajectory time, i.e. the time to execute the trajectory.
 	 * 
 	 * @param value
 	 * @throws DeviceException
 	 * @throws InterruptedException 
 	 */
 	@Override
 	public void setTime(double value) throws DeviceException, InterruptedException {
 		try{
		controller.caput(time, value);
 	} catch (CAException e) {
 		logger.error("Error setting m1Move " , e);
 		throw new DeviceException("Error setting m1Move " , e);
 	}
 	}
 
 	/**
 	 * gets the total time for the trajectory.
 	 * 
 	 * @return total trajectory time
 	 * @throws TimeoutException
 	 * @throws CAException
 	 * @throws InterruptedException 
 	 */
 	public double getTime() throws TimeoutException, CAException, InterruptedException {
 		return controller.cagetDouble(time);
 	}
 
 	/**
 	 * starts the build process in EPICS.
 	 * @throws DeviceException 
 	 * @throws InterruptedException 
 	 */
 	@Override
 	public void build() throws DeviceException, InterruptedException {
 		buildDone = false;
 		InterfaceProvider.getTerminalPrinter().print("Trajectory Build is called.");
 		try {
 			controller.caput(build, 1, bcbl);
 		} catch (CAException e) {
 			throw new DeviceException("Exception in build",e);
 		}
 	}
 
 	/**
 	 * @return short
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public short getBuild() throws TimeoutException, CAException, InterruptedException {
 		short num = controller.cagetEnum(build);
 		return num;
 	}
 
 	/**
 	 * starts the execute process in EPICS.
 	 * @throws DeviceException 
 	 * @throws InterruptedException 
 	 */
 	@Override
 	public void execute() throws DeviceException, InterruptedException {
 		/*
 		 * if (!buildDone || getBuildStatus() != BuildStatus.SUCCESS) { throw new IllegalStateException( "Success build
 		 * is required before execution."); }
 		 */
 		executeDone = false;
 		JythonServerFacade.getInstance().print("Trajectory Execute is called.");
 		try {
 			controller.caput(execute, 1, ecbl);
 		} catch (CAException e) {
 			throw new DeviceException("Exception in execute",e);
 		}
 	}
 
 	/**
 	 * @return short
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public short getExecute() throws TimeoutException, CAException, InterruptedException {
 		short num =  controller.cagetEnum(execute);
 		return num;
 	}
 
 	/**
 	 * starts the read process in EPICS.
 	 * @throws DeviceException 
 	 * @throws InterruptedException 
 	 */
 	@Override
 	public void read() throws DeviceException, InterruptedException {
 		/*
 		 * if (!buildDone || getBuildStatus() != BuildStatus.SUCCESS) { throw new IllegalStateException( "Success build
 		 * is required before execution."); } if (!executeDone || getExecuteStatus() != ExecuteStatus.SUCCESS) { throw
 		 * new IllegalStateException( "Success execution is required before read data back."); }
 		 */
 		readDone = false;
 		JythonServerFacade.getInstance().print("Trajectory Read is called.");
 		try {
 			controller.caput(read, 1, rcbl);
 		} catch (CAException e) {
 			throw new DeviceException("Exception in read",e);
 		}
 	}
 
 	/**
 	 * @return short
 	 * @throws TimeoutException
 	 * @throws CAException
 	 * @throws InterruptedException 
 	 */
 	public short getRead() throws TimeoutException, CAException, InterruptedException {
 		short num =  controller.cagetEnum(read);
 		return num;
 	}
 
 	/**
 	 * gets the build message
 	 * 
 	 * @return String the message
 	 */
 	public String getBuildMessage() {
 		return buildMessage;
 	}
 
 	/**
 	 * @return build message
 	 * @throws TimeoutException
 	 * @throws CAException
 	 * @throws InterruptedException 
 	 */
 	public String getBuildMessageFromEpics() throws TimeoutException, CAException, InterruptedException {
 		return controller.caget(bmess);
 	}
 
 	/**
 	 * returns the build status
 	 * 
 	 * @return build status
 	 */
 	@Override
 	public BuildStatus getBuildStatus() {
 		short value;
 		try {
 			value = controller.cagetEnum(bstatus);
 		
 			if (value == 1) {
 				buildStatus = BuildStatus.SUCCESS;
 			} else if (value == 2) {
 				buildStatus = BuildStatus.FAILURE;
 			} else if (value == 0) {
 				buildStatus = BuildStatus.UNDEFINED;
 			} else {
 				logger.error("Trajectory build reports UNKNOWN status value: {}", value);
 		}
 		} catch (TimeoutException e) {
 			// TODO Auto-generated catch block
 			logger.error("Could not get the build status ", e);
 		} catch (CAException e) {
 			// TODO Auto-generated catch block
 			logger.error("Could not get the build status", e);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			logger.error("Could not get the build status", e);
 		}
 		return buildStatus;
 	}
 
 	/**
 	 * @return build status
 	 * @throws CAException
 	 * @throws TimeoutException
 	 * @throws InterruptedException 
 	 */
 	public short getBuildStatusFromEpics() throws TimeoutException, CAException, InterruptedException {
 		return controller.cagetEnum(bstatus);
 	}
 
 	/**
 	 * gets the build state
 	 * 
 	 * @return build state
 	 */
 	public String getBuildState() {
 		return buildState;
 	}
 
 	/**
 	 * @return build state
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public short getBuildStateFromEpics() throws TimeoutException, CAException, InterruptedException {
 		return controller.cagetEnum(bstate);
 	}
 
 	/**
 	 * gets the execute message
 	 * 
 	 * @return String the message
 	 */
 	public String getExecuteMessage() {
 		return executeMessage;
 	}
 
 	/**
 	 * @return execute message
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public String getExecuteMessageFromEpics() throws TimeoutException, CAException, InterruptedException {
 		return controller.caget(emess);
 	}
 
 	/**
 	 * returns the Execute status
 	 * 
 	 * @return Execute status
 	 */
 	public ExecuteStatus getExecuteStatus() {
 		return executeStatus;
 	}
 
 	/**
 	 * @return execute status
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public short getExecuteStatusFromEpics() throws TimeoutException, CAException, InterruptedException {
 		return controller.cagetEnum(estatus);
 	}
 
 	/**
 	 * gets the Execute state
 	 * 
 	 * @return Execute state
 	 */
 	public String getExecuteState() {
 		return executeState;
 	}
 
 	/**
 	 * @return execute state
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public short getExecuteStateFromEpics() throws TimeoutException, CAException, InterruptedException {
 		return controller.cagetEnum(estate);
 	}
 
 	/**
 	 * gets the Read message
 	 * 
 	 * @return String the message
 	 */
 	public String getReadMessage() {
 		return readMessage;
 	}
 
 	/**
 	 * @return read message
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public String getReadMessageFromEpics() throws TimeoutException, CAException, InterruptedException {
 		return controller.caget(rmess);
 	}
 
 	/**
 	 * returns the Read status
 	 * 
 	 * @return Read status
 	 */
 	@Override
 	public ReadStatus getReadStatus() {
 		short value;
 		try {
 			value = controller.cagetEnum(rstatus);
 		
 			if (value == 1) {
 				readStatus = ReadStatus.SUCCESS;
 			} else if (value == 2) {
 				readStatus = ReadStatus.FAILURE;
 			} else if (value == 0) {
 				readStatus = ReadStatus.UNDEFINED;
 			} else {
 				logger.error("Trajectory Read reports UNKNOWN status value: {}", value);
 		}
 		} catch (TimeoutException e) {
 			// TODO Auto-generated catch block
 			logger.error("Could not get the read status ", e);
 		} catch (CAException e) {
 			// TODO Auto-generated catch block
 			logger.error("Could not get the read status", e);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			logger.error("Could not get the read status", e);
 		}
 		return readStatus;
 	}
 
 	/**
 	 * @return read status
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public short getReadStatusFromEpics() throws TimeoutException, CAException, InterruptedException {
 		return controller.cagetEnum(rstatus);
 	}
 
 	/**
 	 * gets the Read state
 	 * 
 	 * @return Read state
 	 */
 	public String getReadState() {
 		return readState;
 	}
 
 	/**
 	 * @return read state
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public short getReadStateFromEpics() throws TimeoutException, CAException, InterruptedException {
 		return controller.cagetEnum(rstate);
 	}
 
 	/**
 	 * gets the actual output pulses
 	 * 
 	 * @return number of actual pulses
 	 * @throws TimeoutException
 	 * @throws DeviceException
 	 */
 	@Override
 	public int getActualPulses() throws TimeoutException, DeviceException {
 		try{
 			return controller.cagetInt(apulses);
 	} catch (CAException e) {
 		logger.error("Error setting m1Move " , e);
 		throw new DeviceException("Error setting m1Move " , e);
 	} catch (InterruptedException e) {
 		throw new DeviceException("Error setting m1Move " , e);
 	}
 	}
 	/**
 	 * gets the Actual trajectory path for motor .
 	 * 
 	 * @return double value array
 	 * @throws DeviceException
 	 * @throws TimeoutException
 	 */
 	@Override
 	public double[] getMActual(int motorIndex) throws DeviceException, TimeoutException, InterruptedException {
 		try{
 		return controller.cagetDoubleArray(mactual[motorIndex - 1]);
 		} catch (CAException e) {
 			logger.error("Error setting m1Move " , e);
 			throw new DeviceException("Error setting m1Move " , e);
 		} catch (InterruptedException e) {
 			throw new DeviceException("Error setting m1Move " , e);
 		}
 	}
 
 	/**
 	 * gets the Actual trajectory path for motor 1.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double[] getM1Actual() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(mactual[0]);
 	}
 
 	/**
 	 * gets the Actual trajectory path for motor 2.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double[] getM2Actual() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(mactual[1]);
 	}
 
 	/**
 	 * gets the Actual trajectory path for motor 2.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double[] getM2Error() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(m2error);
 	}
 
 	/**
 	 * gets the Actual trajectory path for motor 3.
 	 * 
 	 * @return double value array
 	 * @throws DeviceException
 	 * @throws TimeoutException
 	 * @throws InterruptedException 
 	 */	
 	public double[] getM3Actual() throws DeviceException, TimeoutException, InterruptedException {
 		
 		try{
 			return controller.cagetDoubleArray(mactual[2]);
 		}
 	catch (CAException e) {
 		logger.error("Error setting m1Move " , e);
 		throw new DeviceException("Error setting m1Move " , e);
 	}
 	}
 
 	/**
 	 * gets the Actual trajectory path for motor 4.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double[] getM4Actual() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(mactual[3]);
 	}
 
 	/**
 	 * gets the Actual trajectory path for motor 5.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double[] getM5Actual() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(mactual[4]);
 	}
 
 	/**
 	 * gets the Actual trajectory path for motor 6.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double[] getM6Actual() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(mactual[5]);
 	}
 
 	/**
 	 * gets the Actual trajectory path for motor 7.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double[] getM7Actual() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(mactual[6]);
 	}
 
 	/**
 	 * gets the Actual trajectory path for motor 8.
 	 * 
 	 * @return double value array
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double[] getM8Actual() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetDoubleArray(mactual[7]);
 	}
 
 	/**
 	 * gets the motor name for motor .
 	 * 
 	 * @return name
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public String getMName(int motorIndex) throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetString(mname[motorIndex -1]);
 	}
 
 	/**
 	 * gets the motor name for motor 1.
 	 * 
 	 * @return name
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public String getM1Name() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetString(mname[0]);
 	}
 
 	/**
 	 * gets the motor name for motor 2.
 	 * 
 	 * @return name
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public String getM2Name() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetString(mname[1]);
 	}
 
 	/**
 	 * gets the motor name for motor 3.
 	 * 
 	 * @return name
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public String getM3Name() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetString(mname[2]);
 	}
 
 	/**
 	 * gets the motor name for motor 4.
 	 * 
 	 * @return name
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public String getM4Name() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetString(mname[3]);
 	}
 
 	/**
 	 * gets the motor name for motor 5.
 	 * 
 	 * @return name
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public String getM5Name() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetString(mname[4]);
 	}
 
 	/**
 	 * gets the motor name for motor 6.
 	 * 
 	 * @return name
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public String getM6Name() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetString(mname[5]);
 	}
 
 	/**
 	 * gets the motor name for motor 7.
 	 * 
 	 * @return name
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public String getM7Name() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetString(mname[6]);
 	}
 
 	/**
 	 * gets the motor name for motor 8.
 	 * 
 	 * @return name
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public String getM8Name() throws CAException, TimeoutException, InterruptedException {
 		return controller.cagetString(mname[7]);
 	}
 
 	/**
 	 * stop or abort the trajectory scan.
 	 * 
 	 * @throws DeviceException
 	 * @throws InterruptedException 
 	 */
 	@Override
 	public void stop() throws DeviceException, InterruptedException {
 		
 		try{controller.caput(abort, 1);
 	} catch (CAException e) {
 		logger.error("Error setting m1Move " , e);
 		throw new DeviceException("Error setting m1Move " , e);
 	}
 	}
 
 	/**
 	 * The build call back handler
 	 */
 	public class BuildCallbackListener implements PutListener {
 		@Override
 		public synchronized void putCompleted(PutEvent ev) {
 			JythonServerFacade.getInstance().print("Build call back now." + ev.getStatus());
 			if (ev.getStatus() != CAStatus.NORMAL) {
 				logger
 						.error("Put failed. Channel {} : Status {}", ((Channel) ev.getSource()).getName(), ev
 								.getStatus());
 			} else {
 				JythonServerFacade.getInstance().print(
 						"Build state: " + getBuildState() + "\n" + "Build Status: " + getBuildStatus() + "\n"
 								+ "Build Message: " + getBuildMessage());
 				if (getBuildStatus() != BuildStatus.SUCCESS) {
 					logger.error("Trajectory scan {} build process failed: {}", getName(), getBuildMessage());
 				}
 			}
 			// terminate build calling thread
 			buildDone = true;
 			this.notifyAll();
 			//notifyIObservers(TrajectoryScanProperty.BUILD, buildDone);
 		}
 	}
 
 	/**
 	 * The execute call back handler
 	 */
 	public class ExecuteCallbackListener implements PutListener {
 		volatile PutEvent event = null;
 
 		@Override
 		public synchronized void putCompleted(PutEvent ev) {
 			event = ev;
 			if (event.getStatus() != CAStatus.NORMAL) {
 				logger.error("Execution failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
 						.getStatus());
 			} else {
 				JythonServerFacade.getInstance().print(
 						"Execute state: " + getExecuteState() + "\n" + "Execute Status: " + getExecuteStatus() + "\n"
 								+ "Execute Message: " + getExecuteMessage());
 
 				if (getExecuteStatus() != ExecuteStatus.SUCCESS) {
 					logger.error("Trajectory scan {} build process failed: {}", getName(), getExecuteMessage());
 				}
 			}
 			// terminate build calling thread
 			executeDone = true;
 			//notifyIObservers(TrajectoryScanProperty.EXECUTE, executeDone);
 		}
 	}
 
 	/**
 	 * The read callback handler
 	 */
 	public class ReadCallbackListener implements PutListener {
 		volatile PutEvent event = null;
 
 		@Override
 		public synchronized void putCompleted(PutEvent ev) {
 			event = ev;
 			if (event.getStatus() != CAStatus.NORMAL) {
 				logger.error("Read failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
 						.getStatus());
 			} else {
 				JythonServerFacade.getInstance().print(
 						"Read state: " + getReadState() + "\n" + "Read Status: " + getReadStatus() + "\n"
 								+ "Read Message: " + getReadMessage());
 				if (getReadStatus() != ReadStatus.SUCCESS) {
 					logger.error("Trajectory scan {} build process failed: {}", getName(), getReadMessage());
 				}
 			}
 			// terminate build calling thread
 			readDone = true;
 			//notifyIObservers(TrajectoryScanProperty.READ, readDone);
 		}
 	}
 
 	/**
 	 * Build status Listener
 	 */
 	private class BuildStatusListener implements MonitorListener {
 
 		@Override
 		public void monitorChanged(MonitorEvent arg0) {
 			short value = -1;
 			DBR dbr = arg0.getDBR();
 			if (dbr.isENUM()) {
 				value = ((DBR_Enum) dbr).getEnumValue()[0];
 
 			} else {
 				logger.error("Error: expecting ENUM type but got " + dbr.getType() + " type.");
 			}
 			if (value == 1) {
 				buildStatus = BuildStatus.SUCCESS;
 			} else if (value == 2) {
 				buildStatus = BuildStatus.FAILURE;
 			} else if (value == 0) {
 				buildStatus = BuildStatus.UNDEFINED;
 			} else {
 				logger.error("Trajectory Build report UNKNOWN staus value: {}", value);
 			}
 			logger.debug("Build status updated to: {}", buildStatus.name());
 		}
 	}
 
 	/**
 	 * Execute Status Listener
 	 */
 	private class ExecuteStatusListener implements MonitorListener {
 
 		@Override
 		public void monitorChanged(MonitorEvent arg0) {
 			short value = -1;
 			DBR dbr = arg0.getDBR();
 			if (dbr.isENUM()) {
 				value = ((DBR_Enum) dbr).getEnumValue()[0];
 
 			} else {
 				logger.error("Error: expecting ENUM type but got " + dbr.getType() + " type.");
 			}
 			if (value == 1) {
 				executeStatus = ExecuteStatus.SUCCESS;
 			} else if (value == 2) {
 				executeStatus = ExecuteStatus.FAILURE;
 			} else if (value == 3) {
 				executeStatus = ExecuteStatus.ABORT;
 			} else if (value == 4) {
 				executeStatus = ExecuteStatus.TIMEOUT;
 			} else if (value == 0) {
 				executeStatus = ExecuteStatus.UNDEFINED;
 			} else {
 				logger.error("Trajectory execute reports UNKNOWN status value: {}", value);
 			}
 			notifyIObservers(TrajectoryScanProperty.EXECUTE, executeStatus);
 			logger.debug("Execute status updated to: {}", executeStatus.name());
 		}
 	}
 
 	/**
 	 * Read Status Listener
 	 */
 	private class ReadStatusListener implements MonitorListener {
 
 		@Override
 		public void monitorChanged(MonitorEvent arg0) {
 			short value = -1;
 			DBR dbr = arg0.getDBR();
 			if (dbr.isENUM()) {
 				value = ((DBR_Enum) dbr).getEnumValue()[0];
 
 			} else {
 				logger.error("Error: expecting ENUM type but got " + dbr.getType() + " type.");
 			}
 			if (value == 1) {
 				readStatus = ReadStatus.SUCCESS;
 			} else if (value == 2) {
 				readStatus = ReadStatus.FAILURE;
 			} else if (value == 0) {
 				readStatus = ReadStatus.UNDEFINED;
 			} else {
 				logger.error("Trajectory Read reports UNKNOWN status value: {}", value);
 			}
 			logger.debug("Read status updated to: {}", readStatus.name());
 		}
 	}
 
 	/**
 	 * Build State listener
 	 */
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
 
 	/**
 	 * Execute state listener
 	 */
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
 
 	/**
 	 * Read state listener
 	 */
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
 
 	/**
 	 * Build Message Listener
 	 */
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
 
 	/**
 	 * Execute Message Listener
 	 */
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
 
 	/**
 	 * Read Message Listener
 	 */
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
 
 	/**
 	 * sets acceleration for motor 1.
 	 * 
 	 * @param value
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void setM1Acceleration(double[] value) throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[0]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		controller.caput(ch, value);
 	}
 
 	/**
 	 * sets acceleration for motor 2.
 	 * 
 	 * @param value
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void setM2Acceleration(double[] value) throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[1]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		controller.caput(ch, value);
 	}
 
 	/**
 	 * sets acceleration for motor 3.
 	 * 
 	 * @param value
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void setM3Acceleration(double[] value) throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[2]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		controller.caput(ch, value);
 	}
 
 	/**
 	 * sets acceleration for motor 4.
 	 * 
 	 * @param value
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void setM4Acceleration(double[] value) throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[3]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		controller.caput(ch, value);
 	}
 
 	/**
 	 * sets acceleration for motor 5.
 	 * 
 	 * @param value
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void setM5Acceleration(double[] value) throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[4]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		controller.caput(ch, value);
 	}
 
 	/**
 	 * sets acceleration for motor 6.
 	 * 
 	 * @param value
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void setM6Acceleration(double[] value) throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[5]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		controller.caput(ch, value);
 	}
 
 	/**
 	 * sets acceleration for motor 7.
 	 * 
 	 * @param value
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void setM7Acceleration(double[] value) throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[6]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		controller.caput(ch, value);
 	}
 
 	/**
 	 * sets acceleration for motor 8.
 	 * 
 	 * @param value
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void setM8Acceleration(double[] value) throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[7]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		controller.caput(ch, value);
 	}
 	/**
 	 * sets acceleration for motor.
 	 * 
 	 * @param value
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void setMAcceleration(int motorIndex, double[] value) throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[motorIndex -1]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		controller.caput(ch, value);
 	}
 
 	/**
 	 * gets the acceleration of motor .
 	 * 
 	 * @return acceleration
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double getMAcceleration(int motorIndex) throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[motorIndex -1]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		return controller.cagetDouble(ch);
 	}
 
 	/**
 	 * gets the acceleration of motor 1.
 	 * 
 	 * @return acceleration
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double getM1Acceleration() throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[0]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		return controller.cagetDouble(ch);
 	}
 
 	/**
 	 * gets the acceleration of motor 2.
 	 * 
 	 * @return acceleration
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double getM2Acceleration() throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[1]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		return controller.cagetDouble(ch);
 	}
 
 	/**
 	 * gets the acceleration of motor 3.
 	 * 
 	 * @return acceleration
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double getM3Acceleration() throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[2]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		return controller.cagetDouble(ch);
 	}
 
 	/**
 	 * gets the acceleration of motor 4.
 	 * 
 	 * @return acceleration
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double getM4Acceleration() throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[3]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		return controller.cagetDouble(ch);
 	}
 
 	/**
 	 * gets the acceleration of motor 5.
 	 * 
 	 * @return acceleration
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double getM5Acceleration() throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[4]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		return controller.cagetDouble(ch);
 	}
 
 	/**
 	 * gets the acceleration of motor 6.
 	 * 
 	 * @return acceleration
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double getM6Acceleration() throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[5]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		return controller.cagetDouble(ch);
 	}
 
 	/**
 	 * gets the acceleration of motor 7.
 	 * 
 	 * @return acceleration
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double getM7Acceleration() throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[6]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		return controller.cagetDouble(ch);
 	}
 
 	/**
 	 * gets the acceleration of motor 8.
 	 * 
 	 * @return acceleration
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public double getM8Acceleration() throws CAException, TimeoutException, InterruptedException {
 		String motor = controller.cagetString(mname[7]) + ".ACCL";
 		Channel ch;
 		if ((ch = channelManager.getChannel(motor)) == null) {
 			ch = channelManager.createChannel(motor);
 		}
 		return controller.cagetDouble(ch);
 	}
 
 	/**
 	 * returns the device name
 	 * 
 	 * @return deviceName
 	 */
 	public String getDeviceName() {
 
 		return deviceName;
 	}
 
 	/**
 	 * sets device name
 	 * 
 	 * @param name
 	 */
 	public void setDeviceName(String name) {
 
 		this.deviceName = name;
 	}
 
 	@Override
 	public void initializationCompleted() {
 
 		logger.info("EPICS trajectory Scan Controller {} is initialised", getName());
 
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public void setName(String name) {
 		this.name = name;
 
 	}
 
 	public String getMaxis(int motorIndex)
 	{
 		return maxis[motorIndex -1];
 	}
 	
 	/**
 	 * @param maxis
 	 */
 	public void setMaxis(int motorIndex, String maxis) {
 		this.maxis[motorIndex -1] = maxis;
 	}
 	/**
 	 * @return m1axis
 	 */
 	public String getM1axis() {
 		return maxis[0];
 	}
 
 	/**
 	 * @param m1axis
 	 */
 	public void setM1axis(String m1axis) {
 		this.maxis[0] = m1axis;
 	}
 
 	/**
 	 * @return m2axis
 	 */
 	public String getM2axis() {
 		return maxis[1];
 	}
 
 	/**
 	 * @param m2axis
 	 */
 	public void setM2axis(String m2axis) {
 		this.maxis[1]= m2axis;
 	}
 
 	/**
 	 * @return m3axis
 	 */
 	public String getM3axis() {
 		return maxis[2];
 	}
 
 	/**
 	 * @param m3axis
 	 */
 	public void setM3axis(String m3axis) {
 		this.maxis[2] = m3axis;
 	}
 
 	/**
 	 * @return m4axis
 	 */
 	public String getM4axis() {
 		return maxis[3];
 	}
 
 	/**
 	 * @param m4axis
 	 */
 	public void setM4axis(String m4axis) {
 		this.maxis[3] = m4axis;
 	}
 
 	/**
 	 * @return m5axis
 	 */
 	public String getM5axis() {
 		return maxis[4];
 	}
 
 	/**
 	 * @param m5axis
 	 */
 	public void setM5axis(String m5axis) {
 		this.maxis[4] = m5axis;
 	}
 
 	/**
 	 * @return m6axis
 	 */
 	public String getM6axis() {
 		return maxis[5];
 	}
 
 	/**
 	 * @param m6axis
 	 */
 	public void setM6axis(String m6axis) {
 		this.maxis[5] = m6axis;
 	}
 
 	/**
 	 * @return m7axis
 	 */
 	public String getM7axis() {
 		return maxis[6];
 	}
 
 	/**
 	 * @param m7axis
 	 */
 	public void setM7axis(String m7axis) {
 		this.maxis[6] = m7axis;
 	}
 
 	/**
 	 * @return m8axis
 	 */
 	public String getM8axis() {
 		return maxis[7];
 	}
 
 	/**
 	 * @param m8axis
 	 */
 	public void setM8axis(String m8axis) {
 		this.maxis[7] = m8axis;
 	}
 
 	/**
 	 * @return accelerationTime
 	 */
 	@Override
 	public double getAccelerationTime() {
 		return accelerationTime;
 	}
 
 	/**
 	 * @param accelerationTime
 	 */
 	@Override
 	public void setAccelerationTime(double accelerationTime) {
 		this.accelerationTime = accelerationTime;
 	}
 
 	@Override
 	public boolean isBuilding() throws DeviceException, TimeoutException, InterruptedException {
 		try {
 			return getBuild() == 1?true:false;
 		} catch (CAException e) {
 			throw new DeviceException(getName() + " exception in isBuilding ",e);
 		}
 	}
 
 	@Override
 	public boolean isBusy() throws TimeoutException, InterruptedException, DeviceException {
 		try {
 			return getRead() == 1 ||getExecute() == 1||getBuild() ==1 ? true:false;
 			//return (getBuildStateFromEpics().equals("Busy") || getExecuteState().equals("Busy") || getReadState().equals("isBusy"));
 		} catch (CAException e) {
 			throw new DeviceException(getName() + "Exception in isBusy ",e);
 		}
 	}
 
 	@Override
 	public boolean isReading() throws InterruptedException, DeviceException, TimeoutException {
 		try {
 			return getRead() == 1?true:false;
 		} catch (CAException e) {
 			throw new DeviceException(getName() + " exception in isReading ",e);
 		}
 	}
 }
