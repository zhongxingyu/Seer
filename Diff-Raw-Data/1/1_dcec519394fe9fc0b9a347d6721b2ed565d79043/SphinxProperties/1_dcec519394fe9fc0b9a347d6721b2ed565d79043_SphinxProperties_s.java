 /*
  * Copyright 1999-2002 Carnegie Mellon University.  
  * Portions Copyright 2002 Sun Microsystems, Inc.  
  * Portions Copyright 2002 Mitsubishi Electronic Research Laboratories.
  * All Rights Reserved.  Use is subject to license terms.
  * 
  * See the file "license.terms" for information on usage and
  * redistribution of this file, and for a DISCLAIMER OF ALL 
  * WARRANTIES.
  *
  */
 
 package edu.cmu.sphinx.util;
 
 import java.net.URL;
 import java.util.Properties;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Map;
 import java.util.HashMap;
 
 
 /**
  * Manages configuration data for sphinx.  Configuration data in
  * Sphinx is managed via property files. Objects that need
  * configuration data should get the SphinxProperties object for their
  * context and extract properties. Property names have the form:       
  *
  *	objectName.propertyName
  *
  *  There is a hierarchy of Configuration  data. When a getProperty
  *  method is called on a SphinxProperties object, the System
  *  properties are checked first to see if there is a property with
  *  the given name, if so, the system property is used, otherwise the
  *  properties associated with the URL are checked and if found that
  *  is used, otherwise the default value for the property is used.
  */
 public class SphinxProperties {
 
     /**
      * The context pool contains a mapping from the context to either
      * the URL for the context or once the properties has been loaded
      * from the URL, the properties object itself
      */
     private static Map contextPool = new HashMap();
 
     // This empty SphinxProperties is used when a request is made for
     // a SphinxProperties from a context that does not exist.
     // It allows the application to continue with getXXX calls (which
     // will supply the inline default values).
     private static SphinxProperties EMPTY_SPHINX_PROPERTIES = 
 		new SphinxProperties();
 
     private URL url;		// URL associated with the properties
     private Properties props;	// the actual properties
     private Properties shadowProps; // actuall requested properties
     private String contextName;	// name of the associated context
     private boolean warnIfNoProperty = false;
 
     private final static String PROP_WARN_NO_PROPERTY =
         "edu.cmu.sphinx.util.SphinxProperties.warnIfPropertyNotDefined";
 
 
     /**
      * Initialize a particular context. There can be multiple contexts
      * in a Sphinx system. Each context can have its own
      * SphinxProperties. This method associates a URL with a
      * particular context.
      *
      * @param context the name of the context
      * @param url the location of the property sheet, or null if there
      * is no property sheet.
      *
      * @throws IOException if the property list cannot be loaded from
      * the given url
      */
     static public void initContext(String context, URL url) 	
 				throws IOException {
 	if (contextPool.containsKey(context)) {
 	    throw new Error("Cannot init SphinxProperties with same context: "+
 			    context);
 	} else {
 	    contextPool.put(context, new SphinxProperties(context, url));
 	}
     }
 
 
     /**
      * Initialize a new SphinxProperty with the given Properties object and
      * context.
      *
      * @param context the name of the context
      * @param properties the Properties object where our Properties reside
      */
     static public void initContext(String context, Properties properties) {
 	if (contextPool.containsKey(context)) {
 	    throw new Error("Cannot init SphinxProperties with same context: "+
 			    context);
 	} else {
 	    contextPool.put(context, 
 			    new SphinxProperties(context, properties));
 	}
     }
 
 
     /**
      * Retrieves the SphinxProperties for the particular context. 
      *
      * @param context  the context of interest
      *
      * @return the SphinxProperties associated with the context. If
      * the given context does not exist an empty SphinxProperties 
      * is returned
      *
      */
     static public SphinxProperties getSphinxProperties(String context) {
 	SphinxProperties sp = (SphinxProperties) contextPool.get(context);
 	if (sp == null) {
 	    sp = EMPTY_SPHINX_PROPERTIES;
 	}
 
         sp.warnIfNoProperty = sp.getBoolean(PROP_WARN_NO_PROPERTY, false);
 	return sp;
     }
 
 
     /**
      * Contructs a new SphinxProperties from the given URL and
      * contextName. 
      *
      * @param contextName the name of the context
      * @param url the url associated with the context (or null)
      *
      * @throws IOException if there was an error loading the property
      * sheet.
      */
     private SphinxProperties(String contextName, URL url) throws IOException {
 	this.contextName = contextName;
 	this.url = url;
 	props = new Properties();
 	shadowProps = new Properties();
 	if (url != null) {
 	    props.load(url.openStream());
 	}
     }
 
 
     /**
      * Constructs a new SphinxProperties from the given context and Properties.
      *
      * @param context the name of the context
      * @param properties the Properties object
      */
     private SphinxProperties(String context, Properties properties) {
 	this.contextName = context;
 	this.url = null;
 	props = properties;
 	shadowProps = new Properties();
     }
 
 
     /**
      * Creates an empty SphinxProperties
      */
     private SphinxProperties() {
 	this.contextName = null;
 	this.url = null;
 	props = new Properties();
 	shadowProps = new Properties();
     }
 
 
     /**
      * Retrieves the context name for this SphinxProperties
      *
      * @return the context name
      */
     public String getContext() {
 	return contextName;
     }
 
     /**
      * Prints this property list out to the specified output stream
      *
      * @param out an output stream.
      */
     public void list(PrintStream out) {
 	shadowProps.list(out);
     }
 
     /**
      * Returns a new Property object that contains all the properties of
      * this SphinxProperties.
      *
      * @return a new Property object of all the properties of 
      *    this SphinxProperties
      */
     public Properties getProperties() {
 	Properties properties = new Properties();
 	properties.putAll(props);
 	return properties;
     }
 
     /**
      * Returns true if this SphinxProperties contains the given
      * property.
      *
      * @return true if it has the given property, false otherwise
      */
     public boolean contains(String propertyName) {
 	String value = System.getProperty(propertyName);
 	if (value == null) {
 	    return props.contains(propertyName);
 	} else {
 	    return true;
 	}
     }
 
 
     /**
      * Searches for and returns the value of the given property.
      *
      * @param propertyName the property to search for
      *
      * @return the value of the property, or null if the property
      *    does not exists
      */
     private String getString(String propertyName) {
 	String value = System.getProperty(propertyName);
 	if (value == null) {
 	    value = props.getProperty(propertyName);
 	}
 	return value;
     }
 
 
     /**
      * Searches for the property with the specified name.
      *
      * @param propertyName the name of the property to search for
      * @param defaultValue the value to return if the property is not
      *    found
      *
      * @return the value of the property
      */
     public String getString(String propertyName, String defaultValue) {
 	String value = getString(propertyName);
 	if (value == null) {
 	    warnNoProperty(propertyName, defaultValue);
 	    value = defaultValue;
 	}
 	if (value != null) {
 	    shadowProps.setProperty(propertyName, value);
 	}
 	return value;
     }
 
     /**
      * Searches for the property with the specified name of a
      * particular instance.  This is a conveniance method, since many 
      * property accesses are built by concatenating the instance name
      * and the property name, this method is provided to make it
      * easier.  
      *
      * @param instanceName the name of the particular instance
      * @param propertyName the name of the property to search for
      * @param defaultValue the value to return if the property is not
      *    found
      *
      * @return the value of the property
      */
     public String getString(String instanceName,
 			    String propertyName, String defaultValue) {
 	return getString(instanceName + "." + propertyName,
 			defaultValue);
     }
 
 
     /**
      * Searches for the property with the specified name.
      *
      * @param propertyName the name of the property to search for
      * @param defaultValue the value to return if the property is not
      *    found
      *
      * @return the value of the property
      *
      * @throws NumberFormatException if the property cannot be
      * 	converted to the number format
      */
     public float getFloat(String propertyName, float defaultValue) 
     		throws NumberFormatException {
 	float value = defaultValue;
 	String svalue = getString(propertyName, String.valueOf(defaultValue));
        	if (svalue != null) {
 	    value = Float.parseFloat(svalue);
 	}
 	return value;
     }
 
     /**
      * Searches for the property with the specified name of a
      * particular instance.  This is a conveniance method, since many 
      * property accesses are built by concatenating the instance name
      * and the property name, this method is provided to make it
      * easier.  
      *
      * @param instanceName the name of the particular instance
      * @param propertyName the name of the property to search for
      * @param defaultValue the value to return if the property is not
      *    found
      *
      * @return the value of the property
      * @throws NumberFormatException if the property cannot be
      * 	converted to the number format
      */
     public float getFloat(String instanceName, 
 	    		String propertyName, float defaultValue) 
     		throws NumberFormatException {
 	return getFloat(instanceName + "." + propertyName, defaultValue);
     }
 
     /**
      * Searches for the property with the specified name.
      *
      * @param propertyName the name of the property to search for
      * @param defaultValue the value to return if the property is not
      *    found
      *
      * @return the value of the property
      *
      * @throws NumberFormatException if the property cannot be
      * 	converted to the number format
      */
     public double getDouble(String propertyName, double defaultValue) 
     		throws NumberFormatException {
 	double value = defaultValue;
 	String svalue = getString(propertyName, String.valueOf(defaultValue));
 	if (svalue != null) {
 	    value = Double.parseDouble(svalue);
 	}
 	return value;
     }
 
     /**
      * Searches for the property with the specified name of a
      * particular instance.  This is a conveniance method, since many 
      * property accesses are built by concatenating the instance name
      * and the property name, this method is provided to make it
      * easier.  
      *
      * @param instanceName the name of the particular instance
      * @param propertyName the name of the property to search for
      * @param defaultValue the value to return if the property is not
      *    found
      *
      * @return the value of the property
      * @throws NumberFormatException if the property cannot be
      * 	converted to the number format
      */
     public double getDouble(String instanceName, 
 	    		String propertyName, double defaultValue) 
     		throws NumberFormatException {
 	return getDouble(instanceName + "." + propertyName, defaultValue);
     }
 
     /**
      * Searches for the property with the specified name.
      *
      * @param propertyName the name of the property to search for
      * @param defaultValue the value to return if the property is not
      *    found
      *
      * @return the value of the property
      *
      * @throws NumberFormatException if the property cannot be
      * 	converted to the number format
      */
     public int getInt(String propertyName, int defaultValue) 
     		throws NumberFormatException {
 	int value = defaultValue;
 	String svalue = getString(propertyName, String.valueOf(defaultValue));
 	if (svalue != null) {
 	    value = Integer.parseInt(svalue);
 	}
 	return value;
     }
 
     /**
      * Searches for the property with the specified name of a
      * particular instance.  This is a conveniance method, since many 
      * property accesses are built by concatenating the instance name
      * and the property name, this method is provided to make it
      * easier.  
      *
      * @param instanceName the name of the particular instance
      * @param propertyName the name of the property to search for
      * @param defaultValue the value to return if the property is not
      *    found
      *
      * @return the value of the property
      * @throws NumberFormatException if the property cannot be
      * 	converted to the number format
      */
     public int getInt(String instanceName, 
 	    		String propertyName, int defaultValue) 
     		throws NumberFormatException {
 	return getInt(instanceName + "." + propertyName, defaultValue);
     }
 
     /**
      * Searches for the property with the specified name. The string
      * values: true, on, 1 and enabled all correspond to true,
      * anything else is considered false.
      *
      * @param propertyName the name of the property to search for
      * @param defaultValue the value to return if the property is not
      *    found
      *
      * @return the value of the property
      *
      */
     public boolean getBoolean(String propertyName, boolean defaultValue) {
 	boolean value = defaultValue;
 	String svalue = getString(propertyName, String.valueOf(defaultValue));
 	if (svalue != null) {
 	    value = 	svalue.equalsIgnoreCase("true") ||
 	     		svalue.equalsIgnoreCase("on") ||
 	     		svalue.equalsIgnoreCase("1") ||
 	     		svalue.equalsIgnoreCase("enabled");
 	}
 	return value;
     }
 
     /**
      * Searches for the property with the specified name of a
      * particular instance.  This is a conveniance method, since many 
      * property accesses are built by concatenating the instance name
      * and the property name, this method is provided to make it
      * easier.  
      *
      * @param instanceName the name of the particular instance
      * @param propertyName the name of the property to search for
      * @param defaultValue the value to return if the property is not
      *    found
      *
      * @return the value of the property
      * @throws NumberFormatException if the property cannot be
      * 	converted to the number format
      */
     public boolean getBoolean(String instanceName, 
 	    		String propertyName, boolean defaultValue)  {
 	return getBoolean(instanceName + "." + propertyName, defaultValue);
     }
 
 
     /**
      * Prints out a warning to System.out saying that the given
      * property does not exist and is using the given default value.
      *
      * @param propertyName the name of the property
      * @param defaultValue the default value used
      */
     private void warnNoProperty(String propertyName, String defaultValue) {
 	// print out the warning only if the property has never been
 	// searched for
 	if (warnIfNoProperty && !shadowProps.containsKey(propertyName)) {
 	    System.out.println("WARNING: no property, " 
                     + propertyName + "\n" 
                     + "         using the default value " 
                     + defaultValue);
 	}
     }
 
 
     /**
      * A test program
      */
     public static void main(String[] args) {
 
 	try {
 	    // an empty context
 	    URL emptyURL = null;
 	    SphinxProperties.initContext("sun", emptyURL);
 	    // an populated context
 	    SphinxProperties.initContext("moon", new URL("file:./test.props"));
     	} catch (IOException ioe) {
 	    System.out.println("ioe " + ioe);
 	}
 
 	SphinxProperties sun = SphinxProperties.getSphinxProperties("sun");
 	SphinxProperties moon = SphinxProperties.getSphinxProperties("moon");
 	SphinxProperties star = SphinxProperties.getSphinxProperties("star");
 
 	System.out.println("sun flare " +sun.getString("flare", "high"));
 	System.out.println("moon flare " + moon.getString("flare", "high"));
 	System.out.println("moon grav " + moon.getFloat("gravity", 9.8f));
 	System.out.println("moon lgrav " + moon.getDouble("lgravity", 19.8));
 	System.out.println("moon int " + moon.getInt("craters", 5000));
 	System.out.println("moon bool " + moon.getBoolean("tracing", false));
 
 	System.out.println("io flare " + moon.getString("io.flare", "high"));
 	System.out.println("io grav " + moon.getFloat("io.gravity", 9.8f));
 	System.out.println("io lgrav " + moon.getDouble("io.lgravity", 19.8));
 	System.out.println("io int " + moon.getInt("io.craters", 5000));
 	System.out.println("io bool " + moon.getBoolean("io.tracing", false));
 
 	System.out.println("p flare " + moon.getString("phobus", "flare", "high"));
 	System.out.println("p grav " + moon.getFloat("phobus", "gravity", 9.8f));
 	System.out.println("p lgrav " + moon.getDouble("phobus", "lgravity", 19.8));
 	System.out.println("p int " + moon.getInt("phobus", "craters", 5000));
 	System.out.println("p bool " + moon.getBoolean("phobus", "tracing", false));
     }
 }
