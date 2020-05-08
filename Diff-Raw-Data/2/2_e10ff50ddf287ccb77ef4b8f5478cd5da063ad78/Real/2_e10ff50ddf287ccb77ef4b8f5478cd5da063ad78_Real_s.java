 package com.edinarobotics.scouting.definitions.database.types;
 
 /**
  * Defines a real data type for use in scouting data.
  * This data can store any real value that can be stored
  * in a Java primitive {@code double} value.
  * Many of its methods are implemented similarly to
  * the methods in {@link java.lang.Double}.<br/>
  * Serializing this class using Java's built-in serialization
  * ({@link java.io.Serializable}) is <em>not</em> recommended.
  * @see java.lang.Double
  */
 @SuppressWarnings("serial")
 public class Real extends Number implements Data, Comparable<Real>{
 	private double value;
 	
 	/**
 	 * Constructs a new {@link Real} object representing the
 	 * given {@code double} value.
 	 * @param value The value to store in this object.
 	 */
 	public Real(double value){
 		this.value = value;
 	}
 	
 	/**
 	 * Constructs a new {@link Real} object representing the
 	 * given {@code float} value.
 	 * @param value The value to store in this object.
 	 */
 	public Real(float value){
 		this.value = value;
 	}
 	
 	/**
 	 * Constructs a new {@link Real} object representing the
 	 * given {@code long} value. The type conversion
 	 * is performed with casting.
 	 * @param value The value to store in this object.
 	 */
 	public Real(long value){
 		this((double)value);
 	}
 	
 	/**
 	 * Constructs a new {@link Real} object representing the
 	 * given {@code long} value. The type conversion
 	 * is performed with casting.
 	 * @param value The value to store in this object.
 	 */
 	public Real(int value){
 		this((double)value);
 	}
 	
 	/**
 	 * Constructs a new {@link Real} object representing the
 	 * {@code double} value indicated by the given {@link String}.
 	 * @param s The {@code String} representing the value to store in this object.
 	 * @see Double#Double(String)
 	 */
 	public Real(String s){
 		this(new Double(s));
 	}
 	
 	/**
 	 * Returns the {@code double} value stored in this {@link Real} object.
 	 * @return The exact {@code double} value stored in this object.
 	 */
 	@Override
 	public double doubleValue(){
 		return this.value;
 	}
 	
 	/**
 	 * Returns the {@code double} value stored in this object as an
 	 * {@code long}. The conversion is performed by casting.
 	 * @return An {@code long} representation of the {@code double}
 	 * value stored in this object.
 	 */
 	@Override
 	public long longValue(){
 		return (long)doubleValue();
 	}
 	
 	/**
 	 * Returns the {@code double} value stored in this object as an
 	 * {@code long}. The conversion is performed by casting.
 	 * @return An {@code long} representation of the {@code double}
 	 * value stored in this object.
 	 */
 	@Override
 	public int intValue(){
 		return (int)doubleValue();
 	}
 	
 	/**
 	 * Returns the {@code double} value stored in this object as an
 	 * {@code float}. The conversion is performed by casting.
 	 * @return An {@code float} representation of the {@code double}
 	 * value stored in this object.
 	 */
 	@Override
 	public float floatValue(){
 		return (float)doubleValue();
 	}
 	
 	/**
 	 * Compares this object to another object. The result is
 	 * {@code true} if and only if the argument {@code obj}
 	 * is also an {@link Real} object that contains the
 	 * same {@code double} value as this object.
 	 * @param obj The object to be compared to this
 	 * {@link Real} object.
 	 * @return {@code true} if the objects are equal as
 	 * described above, {@code false} otherwise.
 	 * @see Double#equals(Object)
 	 */
 	@Override
 	public boolean equals(Object obj){
 		if(obj instanceof Real){
 			return ((Real)obj).doubleValue() == doubleValue();
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns a hash code value for this object as described
 	 * in {@link Object#hashCode()}. This method uses the
 	 * implementation in {@link Double#hashCode()}.
 	 * @return A hash code value for this object.
 	 * @see Double#hashCode()
 	 * @see Object#hashCode()
 	 */
 	@Override
 	public int hashCode(){
 		return new Double(this.doubleValue()).hashCode();
 	}
 		
 	/**
 	 * Implements comparisons between different {@link Real}
 	 * objects. This method uses the contract of
 	 * {@link Comparable#compareTo(Object)} where the values
 	 * compared are the results of calls to
 	 * {@link #doubleValue()}.
 	 * @param real The {@code Real} object to be
 	 * compared to this one.
 	 * @return The value {@code 0} if this {@code Real} is
 	 * equal to the argument {@code Real}, a value less than
 	 * {@code 0} if this {@code Real} is less than the
 	 * argument {@code Real} and a value greater than
 	 * {@code 0} if this {@code Real} is numerically greater
 	 * than the argument {@code Real}.
 	 * @see Comparable#compareTo(Object)
 	 */
 	public int compareTo(Real real){
 		return new Double(doubleValue()).compareTo(real.doubleValue());
 	}
 	
 	/**
 	 * Returns a {@link String} object representing this
 	 * {@link Real} object's value. The value is calculated
 	 * using {@link Double#toString()}.
 	 * @return A {@link String} object representing the
 	 * value of this object in base 10.
 	 * @see Double#toString()
 	 */
 	public String toString(){
 		return new Double(doubleValue()).toString();
 	}
 	
 	/**
 	 * Follows the contract of {@link Data#serializeToString()}.
 	 * Gives a {@link String} representation of this object
	 * suitable to be passed to {@link Real#Integer(String)}.
 	 * @return A {@link String} representing the value
 	 * stored in this {@link Real} object.
 	 * In this case it is an alias for {@link Real#toString()}.
 	 * @see Data#serializeToString()
 	 * @see Real#toString()
 	 */
 	public String serializeToString(){
 		return toString();
 	}
 }
