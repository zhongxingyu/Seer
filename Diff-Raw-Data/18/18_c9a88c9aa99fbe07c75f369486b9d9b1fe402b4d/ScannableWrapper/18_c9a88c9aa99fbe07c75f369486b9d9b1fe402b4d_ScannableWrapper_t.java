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
 
 package uk.ac.gda.client.observablemodels;
 
 import gda.device.DeviceException;
 import gda.device.Scannable;
 import gda.device.ScannableMotionUnits;
 import gda.device.scannable.ScannableStatus;
 import gda.observable.IObserver;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.client.UIObservableModel;
 import uk.ac.gda.common.rcp.UIHelper;
 
 public class ScannableWrapper extends UIObservableModel implements IObserver {
 	private static final Logger logger = LoggerFactory.getLogger(ScannableWrapper.class);
 	
 	// TODO this should be declare at the source
 	private static final String LOWER_LIMIT_ATTRIBUTE_NAME = "lowerMotorLimit";
 	private static final String UPPER_LIMIT_ATTRIBUTE_NAME = "upperMotorLimit";
 	
 	private static final int CHECK_BUSY_STATUS_IN_MS = 50;
 	private static final int WAIT_FOR_MSG_UPDATE_IN_MS = 500;
 	private final Scannable scannable;
 
 	public static final String POSITION_PROP_NAME = "position";
 	public static final String BUSY_PROP_NAME = "busy";
 
 	public static final String TARGET_POSITION_PROP_NAME = "targetPosition";
 	private Double targetPosition;
 	
 	public static final String LOWER_LIMIT_PROP_NAME = "lowerLimit";
 	private Double lowerLimit;
 	
 	public static final String UPPER_LIMIT_PROP_NAME = "upperLimit";
 	private Double upperLimit;
 
 	
 	private PositionChecker scannablePositionChecker;
 
 	public ScannableWrapper(Scannable scannable) {
 		this.scannable = scannable;
 		this.scannable.addIObserver(this);
 		updateLimits(scannable);
 	}
 
 	private void updateLimits(Scannable scannable) {
 		try {
 			if( scannable instanceof ScannableMotionUnits) {
 				Double[] upperGdaLimits = ((ScannableMotionUnits)scannable).getUpperGdaLimits();
 				Double[] lowerGdaLimits = ((ScannableMotionUnits)scannable).getLowerGdaLimits();
 				if( (upperGdaLimits != null) && (upperGdaLimits.length==1) && (lowerGdaLimits != null) && (lowerGdaLimits.length==1)) {
 					upperLimit = upperGdaLimits[0];
					lowerLimit = lowerGdaLimits[0];
 					return;
 				}
 			}
 			Object lowerLimitObj = scannable.getAttribute(LOWER_LIMIT_ATTRIBUTE_NAME);
 			Object upperLimitObj = scannable.getAttribute(UPPER_LIMIT_ATTRIBUTE_NAME);
 			if (lowerLimitObj != null && upperLimitObj != null) {
 				lowerLimit = (double) lowerLimitObj;
 				upperLimit = (double) upperLimitObj;
 			}
 		} catch (DeviceException e) {
 			logger.error("Error getting limits for " + scannable.getName(), e);
 		}
 	}
 
 	public Scannable getScannable() {
 		return scannable;
 	}
 
 	public void setPosition(final double position) throws DeviceException {
 		if (scannable.isBusy()) {
 			throw new DeviceException(scannable.getName() +" motor is busy");
 		}
 		final Job job = new Job("Moving " + scannable.getName() + " to " + position + ".") {
 			@Override
 			protected void canceling() {
 				boolean isBusy;
 				try {
 					scannable.stop();
 					// TODO add timeouts!
 					while (scannable.isBusy()) {
 						Thread.sleep(CHECK_BUSY_STATUS_IN_MS);
 					}
 					isBusy = scannable.isBusy();
 					updatePosition();
 				} catch (final Exception e) {
 					UIHelper.showError("Error while stopping the motor", e.getMessage());
 					logger.error("Error while stopping the motor", e);
 					isBusy = false;
 				}
 				firePropertyChange(BUSY_PROP_NAME, true, isBusy);
 				super.canceling();
 			}
 
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				IStatus status;
 				try {
 					firePropertyChange(TARGET_POSITION_PROP_NAME, targetPosition, targetPosition = new Double(position));
 					// TODO add progress monitor and timeouts!
 					scannable.asynchronousMoveTo(position);
 					while (!scannable.isBusy()) {
 						Thread.sleep(CHECK_BUSY_STATUS_IN_MS);
 					}
 					firePropertyChange(BUSY_PROP_NAME, false, scannable.isBusy());
 					while (scannable.isBusy()) {
 						firePropertyChange(POSITION_PROP_NAME, null, scannable.getPosition());
 						Thread.sleep(WAIT_FOR_MSG_UPDATE_IN_MS);
 					}
 					status = Status.OK_STATUS;
 					firePropertyChange(BUSY_PROP_NAME, true, scannable.isBusy());
 					updatePosition();
 				} catch (final Exception e) {
 					UIHelper.showError("Error while moving the motor", e.getMessage());
 					logger.error("Error while moving the motor", e);
 					status = Status.CANCEL_STATUS;
 					firePropertyChange(BUSY_PROP_NAME, true, false);
 				}
 				return status;
 			}
 		};
 		job.schedule();
 	}
 
 	public double getPosition() throws DeviceException {
 		return Double.parseDouble(scannable.getPosition().toString());
 	}
 
 	public Double getTargetPosition() {
 		return targetPosition;
 	}
 
 	public boolean isBusy() throws DeviceException {
 		return scannable.isBusy();
 	}
 
 	@Override
 	public void update(Object source, Object arg) {
 		if (arg instanceof ScannableStatus) {
 			ScannableStatus status = (ScannableStatus) arg;
 			try {
 				if (status.getStatus() == ScannableStatus.BUSY) {
 					if (scannablePositionChecker == null) {
 						synchronized(this) { // Make sure there is only one scannablePositionChecker
 							if (scannablePositionChecker == null) {
 								scannablePositionChecker = new PositionChecker();
 								Thread t = new Thread(scannablePositionChecker);
 								t.start();
 							}
 						}
 					}
 				} else {
 					if (scannablePositionChecker != null) {
 						synchronized(this) {
 							if (scannablePositionChecker != null) {
 								scannablePositionChecker.stop();
 								scannablePositionChecker = null;
 							}
 						}
 					}
 					updatePosition();
 				}
 			} catch (DeviceException e) {
 				logger.error("Error updating scannable motor position", e);
 			}
 		}
 	}
 
 	private void updatePosition() throws DeviceException {
 		final Object object = scannable.getPosition();
 		if (((Double) object).equals(targetPosition) & targetPosition != null) {
 			firePropertyChange(TARGET_POSITION_PROP_NAME, targetPosition, targetPosition = null);
 		}
 		firePropertyChange(POSITION_PROP_NAME, null, (double) object);
 	}
 
 	public Double getLowerLimit() {
 		return lowerLimit;
 	}
 
 	public Double getUpperLimit() {
 		return upperLimit;
 	}
 
 	private class PositionChecker implements Runnable {
 		private boolean stopped = false;
 
 		public void stop() {
 			stopped = true;
 		}
 
 		@Override
 		public void run() {
 			while (!stopped) {
 				try {
 					Thread.sleep(CHECK_BUSY_STATUS_IN_MS);
 					updatePosition();
 				} catch (InterruptedException | DeviceException e) {
 					break;
 				}
 			}
 		}
 	}
 }
