 /*-
  * Copyright © 2014 Diamond Light Source Ltd.
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
 
 import gda.device.DeviceBase;
 import gda.device.DeviceException;
 import gda.device.continuouscontroller.ConstantVelocityRasterMoveController;
 import gda.device.scannable.scannablegroup.ScannableMotionWithScannableFieldsBase;
 import gda.epics.LazyPVFactory;
 import gda.epics.PV;
 import gda.epics.PVWithSeparateReadback;
 import gda.factory.FactoryException;
 import gda.jython.JythonServerFacade;
 import gda.scan.ConstantVelocityRasterScan;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A Scannable suitable for use in {@link ConstantVelocityRasterScan}s inorder to control a PIE725 2d piezo controller.
  * <p>
  * WFrom Jython two fields will be made, name.nameX and name.nameY which represent the x and y dimensions of stage
  * respectively. Us e.g.:
  * <p>
  * ConstantVelocityRasterScan([name.nameY, s, s, s, name.nameX, s, s, s, deta, time ...])
  */
 public class PIE725ConstantVelocityRasterScannable extends ScannableMotionWithScannableFieldsBase implements
 		ContinuouslyScannableViaController {
 
 	private static final Logger logger = LoggerFactory.getLogger(PIE725ConstantVelocityRasterScannable.class);
 
 	protected PVWithSeparateReadback<Double> pvXpair;
 
 	protected PVWithSeparateReadback<Double> pvYpair;
 
 	private Double[] lastRasterTarget = new Double[2];
 
 	/**
 	 * @param name
 	 * @param pvName
 	 *            e.g. BL16I-EA-PIEZO-01:C1:
 	 */
 	public PIE725ConstantVelocityRasterScannable(String name, String pvName) {
 		setName(name);
 		setInputNames(new String[] { name + 'X', name + 'Y' });
 		setExtraNames(new String[] {});
 		setOutputFormat(new String[] { "%.4f", "%.4f" });
 
 		pvXpair = new PVWithSeparateReadback<Double>(LazyPVFactory.newDoublePV(pvName + "X:MOV:WR"),
 				LazyPVFactory.newReadOnlyDoublePV(pvName + "X:POS:RD"));
 
 		pvYpair = new PVWithSeparateReadback<Double>(LazyPVFactory.newDoublePV(pvName + "Y:MOV:WR"),
 				LazyPVFactory.newReadOnlyDoublePV(pvName + "Y:POS:RD"));
 
 		setContinuousMoveController(new PIE725ConstantVelocityRasterMoveController(pvName));
 	}
 
 	@Override
 	public void rawAsynchronousMoveTo(Object position) throws gda.device.DeviceException {
 		Double[] xytarget = PositionConvertorFunctions.toDoubleArray(position);
 		if (xytarget.length != 2) {
 			throw new AssertionError("Target position must have 2 fields, not " + xytarget.length);
 		}
 		if (isOperatingContinously()) {
 			// Record position for the subsequent getPosition() call
 			if (xytarget[0] != null) {
 				lastRasterTarget[0] = xytarget[0];
 			}
 			if (xytarget[1] != null) {
 				lastRasterTarget[1] = xytarget[1];
 			}
 		} else {
 			try {
 				if (xytarget[0] != null) {
 					pvXpair.putWait(xytarget[0]);
 				}
 				if (xytarget[1] != null) {
 					pvYpair.putWait(xytarget[1]);
 				}
 			} catch (IOException e) {
 				throw new DeviceException(e);
 			}
 		}
 	}
 
 	@Override
 	public Object rawGetPosition() throws DeviceException {
 		if (isOperatingContinously()) {
 			if (lastRasterTarget == null) {
 				throw new NullPointerException("lastRasterTargetNotSet	");
 			}
 			return lastRasterTarget;
 		} // else
 		try {
 			return new Double[] { pvXpair.get(), pvYpair.get() };
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 	}
 
 	@Override
 	public boolean isBusy() throws DeviceException {
 		// when operating continuously this needs to return false. When not operating continuously, then
 		// we will have blocked in asynchMoveto already.
 		return false;
 	}
 
 	@Override
 	public void stop() throws DeviceException {
 		try {
 			getContinuousMoveController().stopAndReset();
 		} catch (InterruptedException e) {
 			throw new DeviceException(e);
 		}
 	}
 	
 	@Override
 	public void atCommandFailure() throws DeviceException {
 		stop();
 	}
 	
 	class PIE725ConstantVelocityRasterMoveController extends DeviceBase implements ConstantVelocityRasterMoveController {
 
 		private PV<Boolean> sendStartCommandPv;
 
 		private PV<Boolean> sendStopCommandPv;
 
 		private PV<Boolean> sendWaveformSetupCommand;
 
 		private PV<String> startCommandStringPv;
 		
 		private PV<String> stopCommandStringPv;
 		
 		private PV<String> waveformSetupCommandString;
 
 		private boolean hasBeenStarted = false;
 
 		private double xmin;
 
 		private double xmax;
 
 		private double xstep;
 
 		private double ymin;
 
 		private double ymax;
 
 		private double ystep;
 
 		private double periodS;
 		
 		public PIE725ConstantVelocityRasterMoveController(String pvName) {
 			sendStartCommandPv = LazyPVFactory.newBooleanFromEnumPV(pvName + "WFSTART:GO");
 			sendStopCommandPv = LazyPVFactory.newBooleanFromEnumPV(pvName + "WFSTOP:GO");
 			sendWaveformSetupCommand = LazyPVFactory.newBooleanFromEnumPV(pvName + "WFSETUP:GO");
 			
 			startCommandStringPv = LazyPVFactory.newStringFromWaveformPV(pvName + "WFSTART:WR");
 			stopCommandStringPv = LazyPVFactory.newStringFromWaveformPV(pvName + "WFSTOP:WR");
 			waveformSetupCommandString = LazyPVFactory.newStringFromWaveformPV(pvName + "WFSETUP:WR");
 		}
 
 		@Override
 		public String getName() {
 			return "pie725_controller";
 		}
 		
 		@Override
 		public void setTriggerPeriod(double seconds) throws DeviceException {
 			periodS = seconds;
 			log(".setTriggerPeriod(" + seconds + ")");
 		}
 
 		@Override
 		public void setStart(double startExternal) throws DeviceException {
 			xmin = toInternalX(startExternal);
 		}
 
 		@Override
 		public void setEnd(double endExternal) throws DeviceException {
 			xmax = toInternalX(endExternal);
 		}
 
 		@Override
 		public void setStep(double step) throws DeviceException {
 			assertAgainstForScalingOrUnitChange();
 			this.xstep = step;
 		}
 
 		@Override
 		public void setOuterStart(double startExternal) throws DeviceException {
 			ymin = toInternalY(startExternal);
 		}
 
 		@Override
 		public void setOuterEnd(double endExternal) throws DeviceException {
 			ymax = toInternalY(endExternal);
 		}
 
 		@Override
 		public void setOuterStep(double step) throws DeviceException {
 			assertAgainstForScalingOrUnitChange();
 			ystep = step;
 		}
 
 		@Override
 		public void prepareForMove() throws DeviceException, InterruptedException {
 
 			double cols = ScannableUtils.getNumberSteps(xmin, xmax, xstep) + 1;
 			double rows = ScannableUtils.getNumberSteps(ymin, ymax, ystep) + 1;
 			double rate = 1 / periodS;
 
 			JythonServerFacade jsf = JythonServerFacade.getInstance();
 
 			String instantiateRasterGenerator = MessageFormat.format(
					"RasterGenerator(_rate={0} , _xmin={1}, _xmax={2}, _ymin={3}, _ymax={4}, _rows={5}, _cols={6})",
 					rate, xmin, xmax, ymin, ymax, rows, cols);
 
 			// TODO: create constant for epics_scripts.device.scannable.pie725.generateRaster
 			jsf.exec(logAndReturn("from epics_scripts.device.scannable.pie725.generateRaster import RasterGenerator"));
 			jsf.exec(logAndReturn("_rg = " + instantiateRasterGenerator));
 			jsf.exec(logAndReturn("_rg.createCommands()"));
 
 			String startCommand = jsf.eval(logAndReturn("_rg.startCmd")).toString();
 			logger.info(startCommand);
 
 			String stopCommand = jsf.eval(logAndReturn("_rg.stopCmd")).toString();
 			logger.info(stopCommand);
 
 			String waveformSetupCommand = jsf.eval(logAndReturn("_rg.commands")).toString();
 			logger.info(waveformSetupCommand);
 			
 			
 			try {
 				logger.info("putting string commands");
 				startCommandStringPv.putWait(startCommand);
 //				Thread.sleep(1);
 				stopCommandStringPv.putWait(stopCommand);
 //				Thread.sleep(1);
 				waveformSetupCommandString.putWait(waveformSetupCommand);
 //				Thread.sleep(1);
 				
 				logger.info("sending waveform string commands to controller");
 				sendWaveformSetupCommand.putWait(true);
 //				Thread.sleep(1);
 				
 				logger.info("complete");
 				
 			} catch (IOException e) {
 				throw new DeviceException(e);
 			}
 		}
 		
 		private String logAndReturn(String cmd) {
 			logger.info(">>> " + cmd);
 			return cmd;
 			
 		}
 
 		@Override
 		public void startMove() throws DeviceException {
 			log(".startMove()");
 			hasBeenStarted = true;
 			try {
 				sendStartCommandPv.putWait(true);
 			} catch (IOException e) {
 				throw new DeviceException(e);
 			}
 			log(".startMove() complete");
 		}
 
 		@Override
 		public boolean isMoving() throws DeviceException {
 			log(".isMoving() *Just returning false as EPICS gives no feedback*");
 			if (Thread.interrupted())
 				throw new DeviceException("Thread interrupted during isMoving()");
 			if (hasBeenStarted) {
 				try {
 					Thread.sleep(1000);  // Bodge!
 				} catch (InterruptedException e) {
 					throw new DeviceException(e);
 				}
 			}
 			return false;
 		}
 
 		@Override
 		public void waitWhileMoving() throws DeviceException, InterruptedException {
 			if (hasBeenStarted) {
 				log(".waitWhileMoving() *Just sleeping 1s as EPICS gives no feedback*");
 				Thread.sleep(1000);
 			}
 		}
 
 		@Override
 		public void stopAndReset() throws DeviceException, InterruptedException {
 			log(".stopAndReset()");
 			hasBeenStarted = false;
 			try {
 				sendStopCommandPv.putWait(true);
 			} catch (IOException e) {
 				throw new DeviceException(e);
 			}
 			log(".stopAndReset() complete");
 		}
 
 		// //
 
 		@Override
 		public int getNumberTriggers() {
 			throw new AssertionError("Assumed unused");
 		}
 
 		@Override
 		public double getTotalTime() throws DeviceException {
 			throw new AssertionError("Assumed unused");
 		}
 
 		private void log(String msg) {
 			logger.info(getName() + msg);
 		}
 
 		@Override
 		public void configure() throws FactoryException {
 			// do nothing
 		}
 
 		private double toInternalY(double startExternal) {
 			// outer is y in [y, x]
 			Double[] paddedInternal = (Double[]) externalToInternal(new Double[] { startExternal, null });
 			return paddedInternal[0];
 		}
 
 		private double toInternalX(double endExternal) {
 			// inner is x in [y, x]
 			Double[] paddedInternal = (Double[]) externalToInternal(new Double[] { null, endExternal });
 			return paddedInternal[1];
 		}
 
 		private void assertAgainstForScalingOrUnitChange() throws AssertionError {
 			if (isNonNullOrZero(getScalingFactor())) {
 				throw new AssertionError("Scaling factor not supported");
 			}
 
 		}
 
 		private boolean isNonNullOrZero(Double[] a) {
 			if (a == null) {
 				return false;
 			}
 			for (int i = 0; i < a.length; i++) {
 				if (a[i] != 0) {
 					return true;
 				}
 			}
 			return false;
 		}
 
 	}
 
 }
