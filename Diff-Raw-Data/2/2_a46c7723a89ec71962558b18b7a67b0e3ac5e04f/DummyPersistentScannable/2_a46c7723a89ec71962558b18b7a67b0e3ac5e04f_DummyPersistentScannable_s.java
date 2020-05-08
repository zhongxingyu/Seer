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
 
 package gda.device.scannable;
 
 import gda.device.DeviceException;
 import gda.util.persistence.LocalParameters;
 
 import java.io.IOException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.FileConfiguration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Dummy object that is able to save its position into a local parameter
  */
 public class DummyPersistentScannable extends DummyScannable {
 	private static final Logger mylogger = LoggerFactory.getLogger(DummyPersistentScannable.class);
 
 	protected FileConfiguration configuration;
 	/**
 	 * Constructor
 	 */
 	public DummyPersistentScannable() {
 		super();
 		try {
 			configuration = LocalParameters.getThreadSafeXmlConfiguration("UserConfiguration");
 		} catch (ConfigurationException e) {
 			mylogger.error("Configuration exception in constructor for DummyPersistentScannable",e);
 		} catch (IOException e) {
 			mylogger.error("IO exception for DummyPersistentScannable", e);
 		}
 	}
 
 	@Override
 	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
 		Double[] positionArray = ScannableUtils.objectToArray(position);
		configuration.setProperty(getName()+"PersistentPosition",position);
 		try {
 			configuration.save();
 			final Double newPosition = positionArray[0];
 			notifyIObservers(getName(), newPosition);
 			notifyIObservers(getName(), new ScannablePositionChangeEvent(newPosition));
 			notifyIObservers(getName(), new ScannableStatus(getName(), ScannableStatus.IDLE));
 		} catch (ConfigurationException e) {
 			mylogger.error("Configuration exception in rawAsynchronousMoveTo for DummyPersistentScannable",e);
 		}
 	}
 	
 	@Override
 	public Object rawGetPosition() throws DeviceException {
 		String propertyName = getName() + "PersistentPosition";
 		if (configuration.getProperty(propertyName)== null) {
 			mylogger.warn("Value "+propertyName + " does not exist, initializing to 0.0");
 			configuration.setProperty(propertyName, "0.0");
 			try {
 				configuration.save();
 			} catch (ConfigurationException e) {
 				mylogger.error("configuration error when saving to UserConfiguration", e);
 			}
 		}
 		return configuration.getProperty(getName()+"PersistentPosition");
 	}
 	
 	
 }
