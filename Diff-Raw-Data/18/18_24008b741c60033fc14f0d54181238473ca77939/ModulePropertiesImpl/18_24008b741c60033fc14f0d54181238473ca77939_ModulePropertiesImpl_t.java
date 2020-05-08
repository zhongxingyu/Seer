 
 /*
  Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 
 package cytoscape.util;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import cytoscape.CytoscapeInit;
 import cytoscape.layout.Tunable;
 
 
 /**
  *
  */
 public class ModulePropertiesImpl implements ModuleProperties {
 	protected HashMap<String, String> propertyMap = null;
 	protected HashMap<String, String> savedPropertyMap = null;
 	protected HashMap<String, Tunable> tunablesMap = null;
 	protected List<Tunable> tunablesList = null;
 	protected String propertyPrefix = null;
 	
 	protected String moduleType; 
 
 	/**
 	 * Constructor.
 	 *
 	 * @param propertyPrefix String representing the prefix to be used
 	 *                       when pulling properties from the property
 	 *                       list.
 	 */
 	public ModulePropertiesImpl(String propertyPrefix, String moduleType) {
 		this.moduleType = moduleType;
 		this.propertyPrefix = propertyPrefix;
 		this.tunablesMap = new HashMap<String, Tunable>();
 		this.tunablesList = new ArrayList<Tunable>();
 	}
 
 	/**
 	 * This method is used to add a new Tunable to the LayoutProperties
 	 * list.  The Tunable can later be retrieved by name using the
 	 * <tt>get</tt> method.
 	 *
 	 * @param tunable The Tunable to add to this LayoutProperties
 	 */
 	public void add(Tunable tunable) {
 		tunablesMap.put(tunable.getName(), tunable);
 		tunablesList.add(tunable);
 	}
 
 	/**
 	 * This method is used to get the Tunable named <tt>name</tt>
 	 * from this LayoutProperties.  In general, the name of a
 	 * Tunable should correspond to the last component of the
 	 * property that it is associated with.
 	 *
 	 * @param name The name of the Tunable to retrieve.
 	 * @return Tunable associated with <tt>name</tt> or null if
 	 *         there is no Tunable with that name.
 	 */
 	public Tunable get(String name) {
 		if (tunablesMap.containsKey(name))
 			return (Tunable) tunablesMap.get(name);
 
 		return null;
 	}
 
 	/**
 	  * This method is used to get the list of available Tunables for
 	  * this algorithm.
 	  *
 	  * @return the list of the names of Tunables for this algorithm
 	  */
 	public List<String> getTunableList() {
 		return new ArrayList<String>(tunablesMap.keySet());
 	}
 
 	/**
 	 * This method is used to get the value from the Tunable
 	 * named <tt>name</tt> from this LayoutProperties.  The
 	 * value is always returned as a String.
 	 *
 	 * @param name The name of the Tunable whose value you
 	 *             want to retrieve.
 	 * @return String value from the Tunable or null if
 	 *         there is no Tunable with that name.
 	 */
 	public String getValue(String name) {
 		if (tunablesMap.containsKey(name)) {
 			Tunable t = (Tunable) tunablesMap.get(name);
 
 			return t.getValue().toString();
 		}
 
 		return null;
 	}
 
 	/**
 	 * This method calls the <tt>updateValues</tt> method of each
 	 * Tunable that is part of this LayoutProperty.
 	 */
 	public void updateValues() {
 		for (Tunable tunable : tunablesList)
 			tunable.updateValue();
 	}
 
 	/**
 	 * These methods provide some simple convenience methods for property
 	 * handling.  They are intended to be used as a mechanism to track
 	 * settings and tuneables.
 	 */
 
 	/**
 	 * getProperties is used to extract properties from the Cytoscape properties
 	 * file.  getProperties should always be called first to initialize the property
 	 * maps.
 	 *
 	 * @return HashMap containing the resulting properties
 	 */
 	public HashMap getProperties() {
 		String prefix = getPrefix();
 		Properties props = CytoscapeInit.getProperties();
 		propertyMap = new HashMap();
 		savedPropertyMap = new HashMap();
 
 		// Find all properties with this prefix
 		Enumeration iter = props.propertyNames();
 
 		while (iter.hasMoreElements()) {
 			String property = (String) iter.nextElement();
 
 			if (property.startsWith(prefix)) {
				int start = prefix.length();
				propertyMap.put(property.substring(start), props.getProperty(property));
				savedPropertyMap.put(property.substring(start), props.getProperty(property));
 			}
 		}
 
 		return propertyMap;
 	}
 
 	/**
 	 * saveProperties is used to add modified properties to the Cytoscape properties
 	 * so they can be saved in the properties file.
 	 *
 	 */
 	public void saveProperties() {
 		String prefix = getPrefix();
 		Properties props = CytoscapeInit.getProperties();
 
 		for (String key : propertyMap.keySet()) {
 			props.setProperty(prefix + key, propertyMap.get(key));
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param property DOCUMENT ME!
 	 * @param value DOCUMENT ME!
 	 */
 	public void setProperty(String property, String value) {
 		propertyMap.put(property, value);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param property DOCUMENT ME!
 	 * @param value DOCUMENT ME!
 	 */
 	public void setSavedProperty(String property, String value) {
 		savedPropertyMap.put(property, value);
 	}
 
 	/**
 	 * revertProperties is used primarily by the settings dialog mechanism when
 	 * the user does a "Cancel".
 	 */
 	public void revertProperties() {
 		propertyMap = new HashMap();
 		if (savedPropertyMap == null)
 			return;
 
 		Set keys = savedPropertyMap.keySet();
 
 		for (Iterator iter = keys.iterator(); iter.hasNext();) {
 			String key = (String) iter.next();
 			propertyMap.put(new String(key), new String((String) savedPropertyMap.get(key)));
 
 			Tunable t = (Tunable) tunablesMap.get(key);
 
 			if (t != null)
 				t.setValue((String) savedPropertyMap.get(key));
 		}
 	}
 
 	/**
 	 * This method is used to read the properties from the Cytoscape properties
 	 * file and set the values for that property in the appropriate Tunable.  If
 	 * there is no value for the property, then the default value in the Tunable
 	 * is used to initialize the property.
 	 */
 	public void initializeProperties() {
 		getProperties();
 
 		for (Iterator iter = tunablesList.iterator(); iter.hasNext();) {
 			Tunable tunable = (Tunable) iter.next();
 			String property = tunable.getName();
 
 			// Do we have this property?
 			if (propertyMap.containsKey(property)) {
 				// Yes -- set it in our array
 				tunable.setValue(propertyMap.get(property));
 			} else {
 				// No, set the default
 				setProperty(property, tunable.getValue().toString());
 				setSavedProperty(property, tunable.getValue().toString());
 			}
 		}
 	}
 	
 	public List<Tunable> getTunables() {
 		return tunablesList;
 	}
 
 	protected void setModuleType(String type) {
 		moduleType = type;
 	}
 
 	protected String getPrefix() {
 		String prefix = moduleType + "." + propertyPrefix;
 
 		if (prefix.lastIndexOf('.') != prefix.length())
 			prefix = prefix + ".";
 
 		return prefix;
 	}
 }
