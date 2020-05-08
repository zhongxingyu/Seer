/*
 * Copyright (c) 2001, 2008 Physikalisch Technische Bundesanstalt.
 * All rights reserved.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */
 package de.ptb.epics.eve.data.measuringstation;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import de.ptb.epics.eve.data.PluginDataType;
 
 /**
  * This class represents the parameter of a plug in. 
  * 
  * @author Stephan Rehfeld <stephan.rehfel( -at )ptb.de>
  *
  */
 public class PluginParameter {
 
 	/**
 	 * The name of the parameter.
 	 */
 	private String name;
 	
 	/**
 	 * The default value of the parameter.
 	 */
 	private String defaultValue;
 	
 	/**
 	 * A flag if this parameter is mandatory
 	 */
 	private boolean mandatory;
 	
 	/**
 	 * The type of the plug in parameter.
 	 */
 	private PluginDataType type;
 	
 	/**
 	 * The string that holds the limitations of the plug in Parameter.
 	 */
 	private String values;
 	
 	/**
 	 * Constructs a <code>PluginParameter</code>.
 	 * 
 	 * @param name the name of the plug in parameter.
 	 * @param type the type of the plug in parameter.
 	 * @param defaultValue the default value for the plug in parameter.
 	 * @param mandatory a flag if the parameter is mandatory.
 	 * @throws IllegalArgumentException if type or name is <code>null</code>
 	 */
 	public PluginParameter(final String name, final PluginDataType type, 
 						final String defaultValue, final boolean mandatory) {
 		if(type == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'type' must not be null!");
 		}
 		if(name == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'name' must not be null!");
 		}
 		this.type = type;
 		this.name = name;
 		this.defaultValue = defaultValue;
 		this.mandatory = mandatory;
 	}
 
 	/**
 	 * Returns the default value of the parameter or null if it has no default 
 	 * parameter.
 	 *
 	 * @return the default value or <code>null</code>, if no default value is 
 	 * 			set.
 	 */
 	public String getDefaultValue() {
 		// Wenn kein defaultValue gesetzt ist, einen erzeugen!
 		if (defaultValue != null) {
 			return defaultValue;
 		}
 		else {
 			if (this.isDiscrete()) {
 				// Value ist diskrete, ersten Eintrag als default setzen
 				String[] values = this.getDiscreteValues().toArray(new String[0]);
 				return values[0];
 			}
 			else {
 				// kein diskreter Wert vorhanden, default Wert über Typ ermitteln
 				return PluginDataType.getDefaultValue(type);
 			}
 		}
 	}
 
 	/**
 	 * Sets the default parameter.
 	 * 
 	 * @param defaultValue the default value or <code>null</code> if a default 
 	 * 			value is not required.
 	 */
 	public void setDefaultValue(final String defaultValue) {
 		this.defaultValue = defaultValue;
 	}
 
 	/**
 	 * Checks whether this parameter is mandatory for the plug in.
 	 * 
 	 * @return <code>true</code> if the parameter is mandatory, 
 	 * 			<code>false</code> otherwise
 	 */
 	public boolean isMandatory() {
 		return this.mandatory;
 	}
 
 	/**
 	 * Sets if the parameter is mandatory. 
 	 * 
 	 * @param mandatory <code>true</code> to set it mandatory, 
 	 * 					 <code>false</code> otherwise
 	 */
 	public void setMandatory(final boolean mandatory) {
 		this.mandatory = mandatory;
 	}
 
 	/**
 	 * Returns the name of parameter.
 	 * 
 	 * @return the name of the parameter.
 	 */
 	public String getName() {
 		return this.name;
 	}
 
 	/**
 	 * Sets the name of the parameter.
 	 * 
 	 * @param name the name of the parameter
 	 * @throws IllegalArgumentException if the argument is <code>null</code>
 	 */
 	public void setName(final String name) {
 		if(name == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'name' must not be null!");
 		}
 		this.name = name;
 	}
 
 	/**
 	 * Returns the type of the parameter.
 	 * 
 	 * @return the type of the parameter.
 	 */
 	public PluginDataType getType() {
 		return this.type;
 	}
 
 	/**
 	 * Sets the type of the parameter.
 	 * 
 	 * @param type the type of the parameter. Must not be 'null'
 	 * @throws IllegalArgumentException if the argument is <code>null</code>
 	 */
 	public void setType(final PluginDataType type) {
 		if(type == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'type' must not be null!");
 		}
 		this.type = type;
 	}
 
 	/**
 	 * Returns the <code>String</code> that describes value limits of the 
 	 * parameter.
 	 * 
 	 * @return the <code>String</code> that describes value limits of 
 	 * 			the parameter.
 	 */
 	public String getValues() {
 		return values;
 	}
 
 	/**
 	 * Sets the values <code>String</code> of the parameter.
 	 * 
 	 * @param values
 	 */
 	public void setValues(final String values) {
 		this.values = values;
 	}
 	
 	/**
 	 * Checks whether the parameter values must be discrete.
 	 * 
 	 * @return <code>true</code> if the parameter values must be discrete, 
 	 * 			<code>false</code> otherwise
 	 */
 	public boolean isDiscrete() {
 		if(values == null) {
 			return false;
 		}
 		StringBuffer buffer = new StringBuffer(this.values);
 		boolean escape = false;
 		escape = false;
 		for(int i=0; i<buffer.length(); ++i) {
 			if(buffer.charAt( i ) == '"') {
 				escape = !escape;
 			} else if(!escape && buffer.charAt( i ) == ',') {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns a <code>List</code> containing all valid values. (if the 
 	 * parameter only has discrete values)
 	 * 
 	 * @return a <code>List</code> of all possible values.
 	 */
 	public List<String> getDiscreteValues() {
 		if( this.isDiscrete() ) {
 			StringBuffer buffer = new StringBuffer(this.values);
 			boolean escape = false;
 			
 			for(int i=0; i<buffer.length(); ++i) {
 				if(buffer.charAt(i) == '"') {
 					escape = !escape;
 				} else if(!escape && buffer.charAt(i) == ' ') {
 					buffer.deleteCharAt(i);
 					--i;
 				}
 			}
 			
 			escape = false;
 			
 			int lastIndex = 0;
 			List<String> elements = new ArrayList<String>();
 			for(int i=0; i<buffer.length(); ++i) {
 				if(buffer.charAt( i ) == '"') {
 					escape = !escape;
 				} else if(!escape && buffer.charAt(i) == ',') {
 					StringBuffer buffer2 = new StringBuffer(buffer.substring(lastIndex, i));
 					if(buffer2.charAt(0) == '"') {
 						buffer2.deleteCharAt(0);
 					}
 					if(buffer2.charAt(buffer2.length() - 1) == '"') {
 						buffer2.deleteCharAt(buffer2.length() - 1);
 					}
 					elements.add( buffer2.toString());
 					lastIndex = i + 1;
 				}
 			}
 			StringBuffer buffer2 = new StringBuffer(buffer.substring(lastIndex, buffer.length()));
 			if(buffer2.charAt( 0 ) == '"') {
 				buffer2.deleteCharAt( 0 );
 			}
 			if(buffer2.charAt( buffer2.length() - 1) == '"') {
 				buffer2.deleteCharAt(buffer2.length() - 1);
 			}
 			elements.add(buffer2.toString());
 			
 			return elements;
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Checks whether a value is valid in regard of the constraints of the 
 	 * <code>TypeValue</code>.
 	 * 
 	 * @param value the value that should be checked
 	 * @return <code>true</code> if the value fits the constraints, 
 	 * 			<code>false</code> otherwise
 	 * @throws IllegalArgumentException if the argument is <code>null</code>
 	 */
 	public boolean isValuePossible(final String value) {
 		if(value == null) {
 			throw new IllegalArgumentException(
 					"The parameter value must not be null");
 		}
 		
 		if(!PluginDataType.isValuePossible( this.type, value)) {
 			return false;
 		}
 		
 		if(values == null) {
 			return true;
 		}
 		
 		
 		StringBuffer buffer = new StringBuffer(this.values);
 		
 		boolean escape = false;
 		for(int i=0; i<buffer.length(); ++i) {
 			if(buffer.charAt(i) == '"') {
 				escape = !escape;
 			} else if(!escape && buffer.charAt(i) == ' ') {
 				buffer.deleteCharAt(i);
 				--i;
 			}
 		}
 		
 		escape = false;
		
		// TODO some explanations/descriptions of these long code parts
		// would be nice
		
 		if(this.isDiscrete()) {
 			escape = false;
 			int lastIndex = 0;
 			List<String> elements = new ArrayList<String>();
 			for(int i=0; i<buffer.length(); ++i) {
 				if(buffer.charAt(i) == '"') {
 					escape = !escape;
 				} else if(!escape && buffer.charAt(i) == ',') {
 					StringBuffer buffer2 = new StringBuffer(buffer.substring(lastIndex, i));
 					if(buffer2.charAt(0) == '"') {
 						buffer2.deleteCharAt(0);
 					}
 					if(buffer2.charAt(buffer2.length() - 1) == '"') {
 						buffer2.deleteCharAt( buffer2.length() - 1);
 					}
 					elements.add(buffer2.toString());
 					lastIndex = i + 1;
 				}
 			}
 			StringBuffer buffer2 = new StringBuffer(
 					buffer.substring(lastIndex, buffer.length()));
 			if(buffer2.charAt(0) == '"') {
 				buffer2.deleteCharAt(0);
 			}
 			if(buffer2.charAt(buffer2.length() - 1) == '"') {
 				buffer2.deleteCharAt(buffer2.length() - 1);
 			}
 			elements.add(buffer2.toString());	
 			
 			Iterator<String> it = elements.iterator();
 			if(this.type == PluginDataType.INT) {
 				int val = Integer.parseInt(value);
 				while(it.hasNext()) {
 					if(Integer.parseInt(it.next()) == val)
 						return true;
 				}
 			} else if(this.type == PluginDataType.DOUBLE) {
 				double val = Double.parseDouble(value);
 				while(it.hasNext()) {
 					if(Double.parseDouble(it.next()) == val)
 						return true;
 				}
 			} else {
 				while(it.hasNext()) {
 					if(it.next().equals(value))
 						return true;
 				}
 			}
 			
 		} else {
 			String[] values = this.values.split("to");
 			if(values.length != 2) {
 				this.values = null;
 				return true;
 			}
 			System.err.println(this.values);
 			System.err.println(value);
 			if(this.type == PluginDataType.INT) {
 				if(Integer.parseInt(value) > Integer.parseInt(values[0]) && 
 				   Integer.parseInt(value) < Integer.parseInt(values[1])) {
 					return true;
 				}
 			} else if(this.type == PluginDataType.DOUBLE) {
 				if(Double.parseDouble(value) >= Double.parseDouble(values[0]) && 
 				   Double.parseDouble(value) <= Double.parseDouble(values[1])) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 
 	/**
	 * @return a hash
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((defaultValue == null) ? 0 : defaultValue.hashCode());
 		result = prime * result + (mandatory ? 1231 : 1237);
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((type == null) ? 0 : type.hashCode());
 		result = prime * result + ((values == null) ? 0 : values.hashCode());
 		return result;
 	}
 
 	/**
 	 * Checks whether the argument and calling object are equal.
 	 * 
 	 * @param obj the <code>Object</code> that should be checked
 	 * @return <code>true</code> if objects are equal, 
 	 * 			<code>false</code> otherwise
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		PluginParameter other = (PluginParameter) obj;
 		if (defaultValue == null) {
 			if (other.defaultValue != null) {
 				return false;
 			}
 		} else if (!defaultValue.equals(other.defaultValue)) {
 			return false;
 		}
 		if (mandatory != other.mandatory) {
 			return false;
 		}
 		if (name == null) {
 			if (other.name != null) {
 				return false;
 			}
 		} else if (!name.equals(other.name)) {
 			return false;
 		}
 		if (type == null) {
 			if (other.type != null) {
 				return false;
 			}
 		} else if (!type.equals(other.type)) {
 			return false;
 		}
 		if (values == null) {
 			if (other.values != null) {
 				return false;
 			}
 		} else if (!values.equals(other.values)) {
 			return false;
 		}
 		return true;
 	}	
 }
