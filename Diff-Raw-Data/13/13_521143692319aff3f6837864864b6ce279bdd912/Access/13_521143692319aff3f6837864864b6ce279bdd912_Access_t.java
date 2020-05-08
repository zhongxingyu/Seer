 package de.ptb.epics.eve.data.measuringstation;
 
 import de.ptb.epics.eve.data.DataTypes;
 import de.ptb.epics.eve.data.MethodTypes;
 import de.ptb.epics.eve.data.TransportTypes;
 
 /**
  * Represents an access description through a mediated layer like EPICS.
  * 
  * @author Stephan Rehfeld <stephan.rehfeld( -at -) ptb.de>
  * @author Marcus Michalsky
  */
 public class Access {
 	
 	// the method type
 	private MethodTypes method;
 	
 	// The data type
 	private DataTypes type;
 	
 	/*
 	 * If this access leads to an array type of data, the array size is in this 
 	 * attribute.
 	 */
 	private int count;
 	
 	// The variable id
 	private String variableID;
 	
 	// The transport type
 	private TransportTypes transport;
 	
 	// The timeout
 	private double timeout;
 	
 	// may be monitored
 	private boolean monitor;
 	
 	/**
 	 * Constructs an <code>Access</code> with the given method type.
 	 * 
 	 * @param method A value of MethodTypes. Must not be null.
 	 * @throws IllegalArgumentException if the argument is <code>null</code> 
 	 */
 	public Access(final MethodTypes method) {
 		this("", null, 0, method, null, 0.0);
 	}
 	
 	/**
 	 * Constructs an <code>Access</code> with specific values.
 	 * 
 	 * @param variableID
 	 * @param type
 	 * @param count
 	 * @param method
 	 * @param transport
 	 * @param timeout
 	 * @throws IllegalArgumentException if <code>variableID</code> or 
 	 * 			<code>method<code> is <code>null</code>
 	 */
 	public Access(final String variableID, final DataTypes type, 
 				   final int count, final MethodTypes method, 
 				   final TransportTypes transport, final double timeout) {
 		if(variableID == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'variableID' must not be null!");
 		}
 		if(method == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'method' must not be null!");
 		}
 		this.variableID = variableID;
 		this.type = type;
 		this.count = count;
 		this.method = method;
 		this.transport = transport;
 		this.timeout = timeout;
 	}
 	
 	/**
 	 * Returns the method type as defined in 
 	 * {@link de.ptb.epics.eve.data.MethodTypes}.
 	 * 
 	 * @return the method type as defined in 
 	 * 			{@link de.ptb.epics.eve.data.MethodTypes}
 	 */
 	public MethodTypes getMethod() {
 		return this.method;
 	}
 	
 	/**
 	 * Returns the data type as defined in 
 	 * {@link de.ptb.epics.eve.data.DataTypes}
 	 * 
 	 * @return the data type as defined in 
 	 * 			{@link de.ptb.epics.eve.data.DataTypes}
 	 */
 	public DataTypes getType() {
 		return this.type;
 	}
 	
 	/**
 	 * Returns the count.
 	 * 
 	 * @return 0 or a positive integer
 	 */
 	public int getCount() {
 		return this.count;
 	}
 	
 	/**
 	 * Returns the id.
 	 * 
 	 * @return a <code>String</code> containing the variable id.
 	 */
 	public String getVariableID() {
 		return this.variableID;
 	}
 	
 	/**
 	 * Sets the count.
 	 * 
 	 * @param count the new count
 	 */
 	public void setCount(final int count) {
 		this.count = count;
 	}
 
 	/**
 	 * Sets the access method.
 	 * 
 	 * @param method the method type
 	 * @throws IllegalArgumentException if the argument is <code>null</code>
 	 */
 	public void setMethod(final MethodTypes method) {
 		if(method == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'method' must not be null!");
 		}
 		this.method = method;
 	}
 
 	/**
 	 * Sets the data type.
 	 * 
 	 * @param type the new data type
 	 */
 	public void setType(final DataTypes type) {
 		this.type = type;
 	}
 
 	/**
 	 * Sets the ID.
 	 * This is the id that is used in the transport system to access it.
 	 * 
 	 * @param variableID the ID 
 	 * @throws IllegalArgumentException if the argument is <code>null</code>
 	 */
 	public void setVariableID(final String variableID) {
 		if(variableID == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'variableID' must not be null!");
 		}
 		this.variableID = variableID;
 	}
 
 	
 	/**
 	 * Returns the timeout.
 	 * 
 	 * @return the timeout
 	 */
 	public double getTimeout() {
 		return this.timeout;
 	}
 
 	/**
 	 * Sets the timeout.
 	 * 
 	 * @param timeout the timeout
 	 */
 	public void setTimeout(final double timeout) {
 		this.timeout = timeout;
 	}
 	
 	/**
 	 * Returns the monitor
 	 * 
 	 * @return monitor flag
 	 */
 	public boolean getMonitor() {
 		return this.monitor;
 	}
 	/**
 	 * Sets or set back the monitor flag.
 	 * 
 	 * @param hasMonitor <code>true</code> to set, 
 	 * 					  <code>false</code> to set back
 	 */
 	public void setMonitor(boolean hasMonitor) {
 		this.monitor = hasMonitor;
 	}
 
 
 	/**
 	 * Returns the transport type. 
 	 *
 	 * @return the transport type
 	 */
 	public TransportTypes getTransport() {
 		return this.transport;
 	}
 
 	/**
 	 * Sets the transport type.
 	 * 
 	 * @param transport the new transport type
 	 */
 	public void setTransport(final TransportTypes transport) {
 		this.transport = transport;
 	}
 
 	/**
 	 * checks whether the given value is valid for this <code>Access</code>.
 	 * 
 	 * @param value the value that should be checked.
 	 * @return <code>true</code> if the value is valid,
 	 * 			<code>false</code> otherwise
 	 */
 	public boolean isValuePossible(final String value) {
 		return DataTypes.isValuePossible(this.type, value);
 	}
 
 	/**
 	 * Returns a <code>String</code> with a valid value for the data type.
 	 * If the value cannot be converted, returns a default value.
 	 * 
 	 * @param value the value that should be formatted.
 	 * @return the formatted <code>String</code>
 	 */
 	public String formatValueDefault(final String value) {
 		return DataTypes.formatValueDefault(this.type, value);
 	}
 	
 	/**
 	 * Returns a <code>String</code> with a valid value for the data type.
 	 * If the value cannot be converted, returns <code>null</code>.
 	 * 
 	 * @param value the value that should be formatted.
 	 * @return the formatted <code>String</code> or <code>null</code> if
 	 * 			not convertible
 	 */
 	public String formatValue(final String value) {
 		return DataTypes.formatValue(this.type, value);
 	}
 
 	/**
 	 * Returns a <code>String</code> with a default value for the data type.
 	 * 
 	 * @return a <code>String</code> with a default value
 	 */
 	public String getDefaultValue() {
 		return DataTypes.getDefaultValue(this.type);
 	}
 
 	/**
 	 * Checks whether the <code>Access</code> is read only.
 	 * 
 	 * @return <code>true</code> if the method is read only,
 	 * 			<code>false</code> otherwise
 	 */
 	public boolean isReadOnly() {
 		switch (method) {
 		case GETPUT:
 		case GETPUTCB:
 		case PUT:
 		case PUTCB:
 			return false;
 		default:
 			return true;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + count;
 		result = prime * result + ((method == null) ? 0 : method.hashCode());
 		result = prime * result + (monitor ? 1231 : 1237);
 		long temp;
 		temp = Double.doubleToLongBits(timeout);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		result = prime * result
 				+ ((transport == null) ? 0 : transport.hashCode());
 		result = prime * result + ((type == null) ? 0 : type.hashCode());
 		result = prime * result
 				+ ((variableID == null) ? 0 : variableID.hashCode());
 		return result;
 	}
 
 	/**
 	 * Checks whether the argument and calling object are equal.
 	 * 
 	 * @param obj the <code>Object</code> to be checked
 	 * @return <code>true</code> if objects are equal,
 	 * 			<code>false</code> otherwise
 	 */
 	@Override
 	public boolean equals(final Object obj) {
 		if(this == obj) {
 			return true;
 		}
 		if(obj == null) {
 			return false;
 		}
 		if(getClass() != obj.getClass()) {
 			return false;
 		}
 		final Access other = (Access)obj;
 		if(count != other.count) {
 			return false;
 		}
 		if(method == null) {
 			if(other.method != null) {
 				return false;
 			}
 		} else if(!method.equals(other.method)) {
 			return false;
 		}
 		if(monitor != other.monitor) {
 			return false;
 		}
 		if(Double.doubleToLongBits(timeout) != Double
 				.doubleToLongBits(other.timeout)) {
 			return false;
 		}
 		if(transport == null) {
 			if(other.transport != null) {
 				return false;
 			}
 		} else if(!transport.equals(other.transport)) {
 			return false;
 		}
 		if(type == null) {
 			if(other.type != null) {
 				return false;
 			}
 		} else if(!type.equals(other.type)) {
 			return false;
 		}
 		if(variableID == null) {
 			if(other.variableID != null) {
 				return false;
 			}
 		} else if(!variableID.equals( other.variableID)) {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Clones the calling <code>Object</code>.
 	 * 
 	 * @return a copy of the current <code>Object</code>
 	 */
 	@Override
 	public Object clone() {
		
		// TODO monitor property was not cloned !!!! is now, but not used 
		// in the constructor...
		/*
 		return new Access(this.variableID, this.type, this.count, 
							this.method, this.transport, this.timeout);*/
		Access access = new Access(this.variableID, this.type, this.count, 
 							this.method, this.transport, this.timeout);
		access.setMonitor(this.getMonitor());
		return access;
		
 	}	
 }
