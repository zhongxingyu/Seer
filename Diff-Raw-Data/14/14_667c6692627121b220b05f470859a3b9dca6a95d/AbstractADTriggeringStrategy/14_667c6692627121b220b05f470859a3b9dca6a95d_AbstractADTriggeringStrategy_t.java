 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
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
 
 package gda.device.detector.addetector.triggering;
 
 import org.springframework.beans.factory.InitializingBean;
 
 import gda.device.detector.areadetector.v17.ADBase;
 
 abstract public class AbstractADTriggeringStrategy implements ADTriggeringStrategy, InitializingBean{
 
 	private final ADBase adBase;
 
 	private double readoutTime = 0.1; // TODO: Should default to 0
 	
 	AbstractADTriggeringStrategy(ADBase adBase) {
 		this.adBase = adBase;
 	}
 	
 	/**
 	 * Sets the required readout/dwell time (t_period - t_acquire).
 	 * <p>
 	 * Defaults to 0.
 	 * 
 	 * @param readoutTime
 	 */
 	public void setReadoutTime(double readoutTime) {
 		this.readoutTime = readoutTime;
 	}
 
 	/**
 	 * Get the required readout/dwell time (t_period - t_acquire).
 	 */
 	public double getReadoutTime() {
 		return readoutTime;
 	}
 	
 	protected ADBase getAdBase() {
 		return adBase;
 	}
 
 	@Override
 	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
 		if (getReadoutTime() < 0) {
 			getAdBase().setAcquirePeriod(0.0);
 		} else {
 			getAdBase().setAcquirePeriod(collectionTime + getReadoutTime());
 		}
		getAdBase().setAcquireTime(collectionTime);
 	}
 	
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		if( adBase == null)
 			throw new RuntimeException("adBase is not set");
 	}
 
 }
