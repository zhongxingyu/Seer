 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
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
 
 package gda.device.detector.countertimer;
 
 import gda.device.DeviceException;
 import gda.device.timer.Tfg;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Another extension to the class which, if given all the data point times in the scan in advance, will code up all the
  * time frames so that timing grousp are used across the scan instead of doing a tfg generate command at each point.
  */
 public class TfgScalerWithFrames extends TfgScalerWithLogValues {
 	
 	private static final Logger logger = LoggerFactory.getLogger(TfgScalerWithFrames.class);
 	
 	private Double[] times; // milliseconds
 
 	public Double[] getTimes() {
 		return times;
 	}
 
 	/**
 	 * @param times - array of times of the upcoming scan in seconds.
 	 */
 	public void setTimes(Double[] times) {
 		logger.debug("array of " + times.length + " frame times given to " + getName());
 		this.times = times;
 	}
 
 	@Override
 	public void atScanLineStart() throws DeviceException {
		super.clearFrameSets();
 		if (times != null && times.length > 0) {
 			// create the time frames here
 			for (int i = 0; i < times.length; i++) {
 				// convert times to milliseconds for da.server
 				addFrameSet(1, 0, times[i] * 1000, 0, 0, -1, 0);
 			}
 		}
 		timer.setAttribute(Tfg.SOFTWARE_START_AND_TRIG_ATTR_NAME, Boolean.TRUE);
 		super.atScanLineStart();
 	}
 
 	@Override
 	public void atScanLineEnd() throws DeviceException {
 		timer.setAttribute(Tfg.SOFTWARE_START_AND_TRIG_ATTR_NAME, Boolean.FALSE);
 		super.atScanLineEnd();
 	}
 
 	@Override
 	public void atScanEnd() throws DeviceException {
 		clearTimesArray();
 		super.atScanEnd();
 	}
 
 	@Override
 	public void atCommandFailure() throws DeviceException {
 		clearTimesArray();
 		super.atCommandFailure();
 		stop();
 	}
 	
 	@Override
 	public void stop() throws DeviceException {
 		clearTimesArray();
 		super.stop();
 	}
 	
 	@Override
 	public void clearFrameSets() throws DeviceException {
 		clearTimesArray();
 		super.clearFrameSets();
 	}
 	
 	private void clearTimesArray() {
 		times = null;
 		logger.debug("array of frame times cleared in " + getName());
 	}
 }
