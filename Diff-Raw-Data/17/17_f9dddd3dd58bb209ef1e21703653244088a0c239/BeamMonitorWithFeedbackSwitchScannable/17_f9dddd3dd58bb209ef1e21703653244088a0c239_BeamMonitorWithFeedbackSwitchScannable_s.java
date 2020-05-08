 /*-
  * Copyright © 2010 Diamond Light Source Ltd.
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
 
 package gda.device.scannable;
 
 import gda.device.DeviceException;
 import gda.epics.connection.EpicsChannelManager;
 import gda.epics.connection.EpicsController;
 import gda.epics.connection.InitializationListener;
 import gda.jython.commands.ScannableCommands;
 import gda.scan.ScanBase;
 import gov.aps.jca.CAException;
 import gov.aps.jca.Channel;
 import gov.aps.jca.Channel.ConnectionState;
 import gov.aps.jca.TimeoutException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class BeamMonitorWithFeedbackSwitchScannable extends TopupScannable implements InitializationListener{
 	private static final Logger logger = LoggerFactory.getLogger(BeamMonitorScannable.class);
 
 	private String machineModePV = "CS-CS-MSTAT-01:MODE";
 	private String ringCurrentPV = "SR21C-DI-DCCT-01:SIGNAL";
 	private String shutterPV = "FE18I-RS-ABSB-02:STA";
 	private String energyPV = "BL18I-OP-DCM-01:ENERGY";
 	private String[] feedbackPVs; 
 	private String[] modesToIgnore = new String[] { "Mach. Dev.","Shutdown" };
 	private boolean removeFromDefaultsAtScanEnd = false;
 
 	private EpicsController controller;
 
 	private EpicsChannelManager channelManager;
 
 	private Channel machineMode;
 	private Channel portShutter;
 	private Channel ringCurrent;
 	private Channel energy;
 	private Channel[] feedback;
 
 	private boolean override;
 
 	public boolean isOverride() {
 		return override;
 	}
 
 	public void setOverride(boolean override) {
 		this.override = override;
 	}
 
 	public BeamMonitorWithFeedbackSwitchScannable() {
 		super();
 	}
 	
 	public BeamMonitorWithFeedbackSwitchScannable(String shutterPV, String... feedBackPVs)
 	{
 		this.shutterPV = shutterPV;
 		if(feedBackPVs.length >0)
 			this.setFeedbackPVs(feedBackPVs);
 		
 	}
 
 	@Override
 	public void configure() {
 		this.level = 1;
 		if (machineModePV == null || machineModePV.isEmpty() || shutterPV == null || 
 				shutterPV.isEmpty() || ringCurrentPV == null || ringCurrentPV.isEmpty()) {
 			logger.error(getName() + " cannot configure as the PVs are not defined.");
 		}
 
 		controller = EpicsController.getInstance();
 		channelManager = new EpicsChannelManager(this);
 		try {
 			machineMode = channelManager.createChannel(machineModePV, false);
 			portShutter = channelManager.createChannel(shutterPV, false);
 			ringCurrent = channelManager.createChannel(ringCurrentPV, false);
 			energy = channelManager.createChannel(energyPV, false);
 			if(feedbackPVs != null && feedbackPVs.length > 0)
 			{
 				feedback = new Channel[feedbackPVs.length];
 				int pvIndex =0;
 				for(String fbPv : feedbackPVs)
 				{
 					if(fbPv!=null && !fbPv.equals(""))
 					feedback[pvIndex++] = channelManager.createChannel(fbPv, false);
 				}
 			}
 		} catch (Exception e) {
 			logger.error(getName() + " Beam monitor failed to configure.",
 					e);
 		}
 	}
 
 	@Override
 	public void atScanStart() throws DeviceException {
 		// if not connected then try to connect again at the start of every scan
 		if (!isConnected()) {
 			configure();
 		}
 	}
 
 	@Override
 	public void atScanEnd() throws DeviceException{
 		ScannableCommands.remove_default(this);
 		
 	}
 	
 	@Override
 	public void atCommandFailure() 
 	{
 		ScannableCommands.remove_default(this);
 	}
 	
 	private boolean isConnected() {
 		if(feedback != null && feedback.length > 0)
 		{
 			for(Channel fbPv : feedback)
 			{
 				if(fbPv!=null && !fbPv.getConnectionState().isEqualTo(ConnectionState.CONNECTED))
 					return false;
 			}
 		}
 		return machineMode.getConnectionState().isEqualTo(ConnectionState.CONNECTED)
 				&& portShutter.getConnectionState().isEqualTo(ConnectionState.CONNECTED)
 				&& ringCurrent.getConnectionState().isEqualTo(ConnectionState.CONNECTED);
 	}
 
 	private String getNotConnectedMessage() {
 		return getName()
 				+ " could not run its test as its channels to Epics are not connected. The scan is not protected by this scannable";
 	}
 
 	@Override
 	protected void testShouldPause() throws DeviceException {
 		
 		if(isOverride())
 			return;
 
 		if (!isConnected()) {
 			logger.error(getNotConnectedMessage());
 		}
 
 		if (!machineIsRunning()) {
 			return;
 		}
 
 		while(true){
 			if (beamWithFeedbackOn())
 				break;
 			//else switch the feedback off and sleep for 1 minute 
 			try {
 				ScanBase.checkForInterrupts();
 				sendAndPrintMessage("Beam lost : Pausing until resumed");				
 				Thread.sleep(60000);
 			} catch (InterruptedException e) {
 				// someone trying to kill the thread so re-throw to kill any scan
 				throw new DeviceException(e.getMessage(), e);
 			}
 		}
 		
 		// set energy to same value so idgap goes to correct position.
 		try {
 			if (energy.get().isDOUBLE()) {
 				Object value = energy.get().getValue();
 				double energyVal;
 				if (value.getClass().isArray())
 					energyVal =((double[])value)[0];
 				else 
 					energyVal = Double.parseDouble(value.toString());
 				energy.put(energyVal);
 			}
 		} catch (NumberFormatException e) {
			logger.error("Test pause - set energy error: cannot parse value for energy from string to double", e);
 		} catch (IllegalStateException e) {
			logger.error("Test pause - channel state error:  failed to put energy value on to energy channel", e);
 		} catch (CAException e) {
			logger.error("Test pause - channel error: channel failure occurred while putting energy value on to energy channel", e);
 		}
 	}
 
 	
 	public boolean beamWithFeedbackOn() throws DeviceException {
 		boolean beamOn = beamAvailable();
 		switchFeedback(beamOn);
 		return beamOn;
 	}
 	
 	private void switchFeedback(boolean onOff) throws DeviceException {
 		
 			//switch the feedback on 
 			if(feedback != null && feedback.length > 0 && feedback[0]!=null)
 			{
 				for(Channel fbPv : feedback){
 					try {
 						if(onOff){
 							if(controller.cagetInt(fbPv) == 0)
 								controller.caput(fbPv, 1);
 						}
 						else{
 							if(controller.cagetInt(fbPv) ==1)
 								controller.caput(fbPv, 0);
 						}
 					} catch (TimeoutException e) {
 						//timeout occurred during feedback switching
						logger.error("switchFeedback - timeout error: failed to read feedback from controller", e);
 						throw new DeviceException(e.getMessage(), e);
 					} catch (CAException e) {
						logger.error("switchFeedback - channel state error: channel failure occurred while getting OR putting value to controller channel", e);
 						throw new DeviceException(e.getMessage(), e);
 					} catch (InterruptedException e) {
						logger.error("switchFeedback - interrupt error: the process thread to get OR put a value to the controller channel was interrupted", e);
 						throw new DeviceException(e.getMessage(), e);
 					}
 					
 				}
 				
 			}
 		
 	}
 	
 	private boolean beamAvailable() {
 		String value;
 		try {
 			value = controller.cagetString(portShutter);
 			return value.equals("Open");
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			return true;
 		}
 	}
 
 	/**
 	 * Returns false if not in a real running mode.
 	 * 
 	 * @return true if user mode, so this object should operate.
 	 */
 	private boolean machineIsRunning() {
 
 		if (machineModePV == null || machineModePV.isEmpty()) {
 			return true;
 		}
 
 		String value;
 		try {
 			value = controller.cagetString(machineMode);
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			return true;
 		}
 
 		for (String modeToIgnore : modesToIgnore) {
 			if (modeToIgnore.equals(value)) {
 				return false;
 			}
 		}
 
 		// current mode not listed in modes to ignore
 		return true;
 	}
 
 	public String getMachineModePV() {
 		return machineModePV;
 	}
 
 	public void setMachineModePV(String machineModePV) {
 		this.machineModePV = machineModePV;
 	}
 
 	public String getShutterPV() {
 		return shutterPV;
 	}
 
 	public void setShutterPV(String shutterPV) {
 		this.shutterPV = shutterPV;
 	}
 
 	public String[] getModesToIgnore() {
 		return modesToIgnore;
 	}
 
 	public void setModesToIgnore(String[] modesToIgnore) {
 		this.modesToIgnore = modesToIgnore;
 	}
 
 	@Override
 	public void initializationCompleted() {
 	}
 
 	public void setFeedbackPVs(String[] feedbackPVs) {
 		this.feedbackPVs = feedbackPVs;
 	}
 
 	public String[] getFeedbackPVs() {
 		return feedbackPVs;
 	}
 
 	public void setRingCurrentPV(String ringCurrentPV) {
 		this.ringCurrentPV = ringCurrentPV;
 	}
 
 	public String getRingCurrentPV() {
 		return ringCurrentPV;
 	}
 
 	public void setRemoveFromDefaultsAtScanEnd(boolean removeFromDefaultsAtScanEnd) {
 		this.removeFromDefaultsAtScanEnd = removeFromDefaultsAtScanEnd;
 	}
 
 	public boolean isRemoveFromDefaultsAtScanEnd() {
 		return removeFromDefaultsAtScanEnd;
 	}
 
 }
