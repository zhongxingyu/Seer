 // The Grinder
 // Copyright (C) 2000  Paco Gomez
 // Copyright (C) 2000  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.util;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.Properties;
 
 /**
  * @author Philip Aston
  * @version $Revision$
  */
 public class GrinderProperties extends Properties
 {
     private static final String PROPERTIES_FILENAME = "grinder.properties";
 
     private static GrinderProperties s_singleton;
 
     /**
      *Package method that sets global GrinderProperties for use by
      *various unit tests
      */
     static void setProperties(GrinderProperties properties)
     {
 	synchronized (GrinderProperties.class)
 	{
 	    s_singleton = properties;
 	}
     }
 
     /**
      * Default constructor is public for use by the unit tests.
     * @see GrinderProperties#setGrinderProperties
      */
     public GrinderProperties()
     {
     }
 
     public static GrinderProperties getProperties()
     {
 	if (s_singleton == null) {
 	    synchronized (GrinderProperties.class) {
 		if (s_singleton == null) { // Double checked locking.
 
 		    s_singleton = new GrinderProperties();
 
 		    try {
 			final InputStream propertiesInputStream =
 			    new FileInputStream(PROPERTIES_FILENAME);
 			s_singleton.load(propertiesInputStream);
 		    }
 		    catch (Exception e) {
 			System.err.println(
 			    "Error loading properties file '" +
 			    PROPERTIES_FILENAME + "'");
 
 			return null;
 		    }
 
 		    // Allow overriding on command line.
 		    s_singleton.putAll(System.getProperties());
 		}
 	    }
 	}
 
 	return s_singleton;
     }
 
     public GrinderProperties getPropertySubset(String prefix)
     {
 	final GrinderProperties result = new GrinderProperties();
 
 	final Enumeration propertyNames = propertyNames();
 
 	while (propertyNames.hasMoreElements()) {
 	    final String name = (String)propertyNames.nextElement();
 
 	    if (name.startsWith(prefix)) {
 		result.setProperty(name.substring(prefix.length()),
 				   getProperty(name));
 	    }
 	}
 
 	return result;	
     }
 
     public String getMandatoryProperty(String propertyName)
 	throws GrinderException
     {
 	final String s = getProperty(propertyName);
 
 	if (s == null) {
 	    throw new GrinderException("Mandatory property '" + propertyName +
 				       "' not specified");
 	}
 
 	return s;	
     }
 
     public int getInt(String propertyName, int defaultValue)
     {
 	final String s = getProperty(propertyName);
 
 	if (s != null) {
 	    try {
 		return Integer.parseInt(s);
 	    }
 	    catch (NumberFormatException e) {
 		System.err.println("Warning, property '" + propertyName +
 				   "' does not specify an integer value");
 	    }
 	}
 
 	return defaultValue;
     }
 
     public int getMandatoryInt(String propertyName)
 	throws GrinderException
     {
 	final String s = getProperty(propertyName);
 
 	if (s == null) {
 	    throw new GrinderException("Mandatory property '" + propertyName +
 				       "' not specified");
 	}
 	
 	try {
 	    return Integer.parseInt(s);
 	}
 	catch (NumberFormatException e) {
 	    throw new GrinderException("Mandatory property '" + propertyName +
 				       "' does not specify an integer value");
 	}
     }
 
     public long getLong(String propertyName, long defaultValue)
     {
 	final String s = getProperty(propertyName);
 
 	if (s != null) {
 	    try {
 		return Long.parseLong(s);
 	    }
 	    catch (NumberFormatException e) {
 		System.err.println("Warning, property '" + propertyName +
 				   "' does not specify an integer value");
 	    }
 	}
 
 	return defaultValue;
     }
 
     public long getMandatoryLong(String propertyName)
 	throws GrinderException
     {
 	final String s = getProperty(propertyName);
 
 	if (s == null) {
 	    throw new GrinderException("Mandatory property '" + propertyName +
 				       "' not specified");
 	}
 	
 	try {
 	    return Long.parseLong(s);
 	}
 	catch (NumberFormatException e) {
 	    throw new GrinderException("Mandatory property '" + propertyName +
 				       "' does not specify a long value");
 	}
     }
 
     public short getShort(String propertyName, short defaultValue)
     {
 	final String s = getProperty(propertyName);
 
 	if (s != null) {
 	    try {
 		return Short.parseShort(s);
 	    }
 	    catch (NumberFormatException e) {
 		System.err.println("Warning, property '" + propertyName +
 				   "' does not specify a short value");
 	    }
 	}
 
 	return defaultValue;
     }
 
     public short getMandatoryShort(String propertyName)
 	throws GrinderException
     {
 	final String s = getProperty(propertyName);
 
 	if (s == null) {
 	    throw new GrinderException("Mandatory property '" + propertyName +
 				       "' not specified");
 	}
 	
 	try {
 	    return Short.parseShort(s);
 	}
 	catch (NumberFormatException e) {
 	    throw new GrinderException("Mandatory property '" + propertyName +
 				       "' does not specify a short value");
 	}
     }
 
     public double getMandatoryDouble(String propertyName)
 	throws GrinderException
     {
 	final String s = getProperty(propertyName);
 
 	if (s == null) {
 	    throw new GrinderException("Mandatory property '" + propertyName +
 				       "' not specified");
 	}
 	
 	try {
 	    return Double.parseDouble(s);
 	}
 	catch (NumberFormatException e) {
 	    throw new GrinderException("Mandatory property '" + propertyName +
 				       "' does not specify a double value");
 	}
     }
 
     public double getDouble(String propertyName, double defaultValue)
     {
 	final String s = getProperty(propertyName);
 
 	if (s != null) {
 	    try {
 		return Double.parseDouble(s);
 	    }
 	    catch (NumberFormatException e) {
 		System.err.println("Warning, property '" + propertyName +
 				   "' does not specify a double value");
 	    }
 	}
 
 	return defaultValue;
     }
 
     public boolean getBoolean(String propertyName, boolean defaultValue)
     {
 	final String s = getProperty(propertyName);
 
 	if (s != null) {
 	    return Boolean.valueOf(s).booleanValue();
 	}
 
 	return defaultValue;
     }
 
     public boolean getMandatoryBoolean(String propertyName)
 	throws GrinderException
     {
 	final String s = getProperty(propertyName);
 
 	if (s == null) {
 	    throw new GrinderException("Mandatory property '" + propertyName +
 				       "' not specified");
 	}
 
 	return Boolean.valueOf(s).booleanValue();
     }
 }
