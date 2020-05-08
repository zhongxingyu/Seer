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
 
 package gda.device.scannable;
 
 import gda.device.DeviceException;
 import gda.util.persistence.LocalParameters;
 
 import java.io.IOException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.FileConfiguration;
 import org.apache.commons.lang.ArrayUtils;
 import org.python.core.PyString;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Accepts and returns a String from a list of Strings.
  * <p>
  * If given integer, uses the index from the list of acceptable Strings
  */
 public class DummyPersistentEnumScannable extends ScannableBase {
 
 	private static final Logger mylogger = LoggerFactory.getLogger(DummyPersistentScannable.class);
 
 	private FileConfiguration configuration;
 	private String[] acceptableStrings = new String[0];
 
 	/**
 	 * Constructor
 	 */
 	public DummyPersistentEnumScannable() {
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
 		configuration.setProperty(getName()+"PersistentPosition",positionArray[0]);
 		try {
 			configuration.save();
 			final Double newPosition = positionArray[0];
 			notifyIObservers(getName(), newPosition);
 			notifyIObservers(getName(), new ScannablePositionChangeEvent(newPosition));
 		} catch (ConfigurationException e) {
 			mylogger.error("Configuration exception in rawAsynchronousMoveTo for DummyPersistentScannable",e);
 		}
 	}
 	
 	@Override
 	public Object rawGetPosition() throws DeviceException {
 		return configuration.getProperty(getName()+"PersistentPosition");
 	}
 	
 
 	@Override
 	public void asynchronousMoveTo(Object position) throws DeviceException {
 
 		if (position instanceof PyString) {
 			position = position.toString();
 		}
 		
 		if (position instanceof String) {
 			if (ArrayUtils.contains(acceptableStrings, position)) {
 				configuration.setProperty(getName() + "PersistentPosition", position);
				try {
					configuration.save();
				} catch (ConfigurationException e) {
					mylogger.error("configuration error when saving to UserConfiguration", e);
				}
 				notifyIObservers(getName(), position);
 				notifyIObservers(getName(), new ScannablePositionChangeEvent(position.toString()));
 				return;
 			}
 		} else {
 			int pos = Integer.parseInt(position.toString());
 			if (acceptableStrings.length > pos) {
 				String newpos = acceptableStrings[pos];
 				configuration.setProperty(getName() + "PersistentPosition", newpos);
				try {
					configuration.save();
				} catch (ConfigurationException e) {
					mylogger.error("configuration error when saving to UserConfiguration", e);
				}
 				notifyIObservers(getName(), newpos);
 				notifyIObservers(getName(), new ScannablePositionChangeEvent(newpos));
 				return;
 			}
 		}
 
 		// if get here then value unacceptable
 
 		throw new DeviceException("Target position " + position + " unacceptable for DummyPersistentEnumScannable "
 				+ getName());
 	}
 
 	@Override
 	public boolean isBusy() {
 		return false;
 	}
 
 	public String[] getAcceptableStrings() {
 		return acceptableStrings;
 	}
 
 	public void setAcceptableStrings(String[] acceptableStrings) {
 		this.acceptableStrings = acceptableStrings;
 	}
 	
 
 }
