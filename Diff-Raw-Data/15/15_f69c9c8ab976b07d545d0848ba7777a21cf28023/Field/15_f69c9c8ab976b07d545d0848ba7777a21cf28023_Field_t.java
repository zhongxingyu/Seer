 package edu.wheaton.simulator.datastructure;
 
 /**
  * Field class for modeling variables. The name and type of the field will not
  * be changeable throughout the life of the field. The value of the field will
  * be changeable.
  * 
  * @author Simon Swenson
  * 
  */
 public class Field {
 
 	/**
 	 * The name for this field.
 	 */
 	private String name;
 	private String value;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param type
 	 *            Type of variable. Should be int, double, char, or String.
 	 * @param value
 	 *            Value of variable. Should match the type; an exception will
 	 *            be thrown if not.
 	 * 
 	 */
 	public Field(Object name, Object value) {
 		this.name = name.toString();
 		this.value = value.toString();
 	}
 
 	public Field(String strRepresentation) {
 		String[] params = strRepresentation.split(":");
 		this.name = params[1];
 		this.value = params[2];
 	}
 
 	/**
 	 * Clone constructor
 	 * 
 	 * @param parent
 	 *            Parent field
 	 */
 	public Field(Field parent) {
 		this(parent.name, parent.value);
 	}
 
 	/**
 	 * @return The string representation of this field. Note: easily parsed
 	 *         with toString().split(":")
 	 */
 	@Override
 	public String toString() {
 		return "Field:" + getName() + ":" + getValue();
 	}
 
 	/**
 	 * 
 	 * @return This field's name.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return This field's value
 	 */
 	public String getValue() {
 		return value;
 	}
 
 	/**
 	 * @return Integer.valueOf(getValue())
 	 */
 	public Integer getIntValue() {
		return getDoubleValue().intValue();
 	}
 
 	/**
 	 * @return Integer.valueOf(getValue())
 	 */
 	public Double getDoubleValue() {
 		return Double.valueOf(getValue());
 	}
 
 	/**
 	 * @return Boolean.valueOf(getValue())
 	 */
 	public Boolean getBoolValue() {
 		return Boolean.valueOf(getValue());
 	}
 }
