 /*-
  * Copyright © 2013 Diamond Light Source Ltd.
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
 
 package gda.device.zebra;
 
 import gda.device.DeviceException;
 import gda.device.continuouscontroller.ContinuousMoveController;
 import gda.device.scannable.ContinuouslyScannableViaController;
 import gda.device.scannable.PositionCallableProvider;
 import gda.device.scannable.PositionConvertorFunctions;
 import gda.device.scannable.ScannableMotor;
 
 import java.util.concurrent.Callable;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 
 public class ZebraScannableMotor extends ScannableMotor implements ContinuouslyScannableViaController, ZebraMotorInfoProvider, PositionCallableProvider<Double>, InitializingBean{
 	private static final Logger logger = LoggerFactory.getLogger(ZebraScannableMotor.class);
 	private ZebraConstantVelocityMoveController continuousMoveController;
 	private double constantVelocitySpeedFactor=0.8;
 	private double scurveTimeToVelocity=.03;//default set to rotation stage on I13
 	private int pcEnc=0;
 
 	@Override
 	public void setOperatingContinuously(boolean b) throws DeviceException {
 		//do nothing - always operating Continuously
 	}
 
 	@Override
 	public boolean isOperatingContinously() {
 		return true;
 	}
 
 	@Override
 	public ContinuousMoveController getContinuousMoveController() {
 		return continuousMoveController;
 	}
 
 	public void setZebraConstantVelocityMoveController(ZebraConstantVelocityMoveController continuousMoveController) {
 		this.continuousMoveController = continuousMoveController;
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		if( continuousMoveController == null){
 			throw new Exception("continuousMoveController == null");
 		}
 	}
 	
 	
 
 	// Scannable //
 	@Override
 	public void asynchronousMoveTo(Object position) throws DeviceException {
 		continuousMoveController.addPoint(PositionConvertorFunctions.toDouble(externalToInternal(position)));
 	}
 	@Override
 	public Object getPosition() throws DeviceException {
		//TODO this will not be called as we have getPositionCallable so getLastPointAdded is not required not addPoint
 		Object[] pos = (Object[]) internalToExternal(new Double[]{continuousMoveController.getLastPointAdded()});
 		if (pos == null) {
 			// First point is in process of being added
 			return super.getPosition();
 		}
 		return pos[0];
 	}
 
 	@Override
 	public Callable<Double> getPositionCallable() throws DeviceException {
 		return continuousMoveController.getPositionSteamIndexer(getPcEnc()).getNamedPositionCallable(getName(),1); 
 	}
 
 	@Override
 	public void waitWhileBusy() throws DeviceException, InterruptedException {
 		return; //this is never busy as it does not talk to hardware
 	}
 
 	@Override
 	public double getConstantVelocitySpeedFactor() {
 		return constantVelocitySpeedFactor;
 	}
 
 	public void setConstantVelocitySpeedFactor(double constantVelocitySpeedFactor) {
 		this.constantVelocitySpeedFactor = constantVelocitySpeedFactor;
 	}
 
 	/**
 	 * 
 	 * @param velocity  in units of motor units/second e.g. mm/s
 	 * @return distance in motor units e.g. mm
 	 */
 	@Override
 	public double distanceToAccToVelocity(double velocity) {
 		//for an S curve with time to velocity of 30ms
 		return scurveTimeToVelocity * velocity/2;
 	}
 
 	public double getScurveTimeToVelocity() {
 		return scurveTimeToVelocity;
 	}
 
 	/**
 	 * 
 	 * @param scurveTimeToVelocity - if using linear acceleration this is the ACCEL field of the 
 	 * motor in EPICS (Time to Velocity)
 	 */
 	public void setScurveTimeToVelocity(double scurveTimeToVelocity) {
 		this.scurveTimeToVelocity = scurveTimeToVelocity;
 	}
 
 	
 	@Override
 	public int getPcEnc() {
 		return pcEnc;
 	}
 
 	/**
 	 * 
 	 * @param pcEnc index of Posn Trig PV of Zebra for this motor Enc1 = 0
 	 */
 	public void setPcEnc(int pcEnc) {
 		this.pcEnc = pcEnc;
 	}
 
 }
