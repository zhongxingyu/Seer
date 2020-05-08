 package com.technosophos.rhizome.document;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Arrays;
 
 /** 
  * This class describes an individual metadatum. Each metadatum is 
  * stored in the metadata portion of a Rhizome document. A Metadatum has
  * one name and one or more values.
  * 
  * @author mbutcher
  * @see com.technosophos.rhizome.document.RhizomeDocument
  *
  */
 public class Metadatum {
 	/** The default name of a metadatum. */
 	public static String METADATUM_DEFAULT_NAME = "default";
 	public static String METADATUM_DEFAULT_TYPE = "string";
 	
 	private String name = null;
 	private List<String> values = null;
 	private String dataType = METADATUM_DEFAULT_TYPE;
 
 	/**
 	 * Construct a simple metadatum.
 	 * This is the constructor that ought to be used if the metadatum is
 	 * likely to have only one value (it is, or may be, optimized for storage
 	 * of short lists).
 	 * @param name
 	 * @param value
 	 */
 	public Metadatum(String name, String value) {
 		this.name = name;
 		this.values = new ArrayList<String>();
 		this.values.add(value);
 	}
 
 	/**
 	 * Create a new metadatum with a list of values.
 	 * This constructor is useful if you already have a list of values.
 	 * @param name
 	 * @param value
 	 */
 	public Metadatum(String name, List<String> value) {
 		this.name = name;
 		this.values = value;
 	}
 	
 	public Metadatum(String name, String[] values) {
 		this(name, new ArrayList<String>(Arrays.asList(values)));
 	}
 
 	/**
 	 * Create a metadatum with a name, but an empty list of values.
 	 * Note that Rhizome can store empty metadatum items (that is, an item
 	 * where there is a name but no values at all). However, different 
 	 * applications of the Rhizome library may handle this situation
 	 * differently (though all should handle it gracefully).
 	 * @param name The name of the metadatum.
 	 */
 	public Metadatum(String name) {
 		this(name, new ArrayList<String>());
 	}
 
 	/** 
 	 * A metadatum needs to have a name to be meaningful. Thus, this one
 	 * assigns the default name 'default' to the metadatum.
 	 * Use another constructor.
 	 */
 	private Metadatum() {
 		this(METADATUM_DEFAULT_NAME,new ArrayList<String>());
 	}
 
 	/**
 	 * Get the name of the metadatum.
 	 * If not metadatum name is set, this will return null. However, this
 	 * should not happen.
 	 * @return The name of the metadatum.
 	 */
 	public String getName() {
 		return this.name;
 	}
 
 	/**
 	 * Get the "type" of the data stored in the metadatum value list.
 	 * Rhizome allows for a weak form of typing of the list of metadatum
 	 * values. All values have to be of the same type. 
 	 * <p>Note that type is not an enforced property of values. All values 
 	 * must be converted to strings before they are stored. However, type
 	 * information may be used by an implementing application as a means
 	 * of figuring out how best to handle the values.</p>
 	 * <p>Example: If the name is "date" and the values are strings 
 	 * containing timestampls, one might choose to set the "type" to 
 	 * "timestamp" or something similar. An implementing application may 
 	 * then decide to convert the timestampe string to a long.</p>
 	 * <p>The default data type is "string" (note lower case).</p>
 	 * @return A String indicating what the (loose and unenforced) type is.
 	 */
 	public String getDataType() {
 		return this.dataType;
 	}
 
 	/**
 	 * Get the list of values stored in this metadatum.
 	 * This will return an empty array list if no values have been set.
 	 * @return List of values (or null if no list exists).
 	 */
 	public List<String> getValues() {
 		if(this.values == null)
 			return new ArrayList<String>();
 		return this.values;
 	}
 	
 	/**
 	 * Get the first value for this metadatum.
 	 * <p>This is a convenience method for quickly getting the first value
 	 * in a metadatum. This will return null under the two following conditions: (a)
 	 * there are no values at all in the metadatum, or (b) the value of this metadatum has
 	 * been explicitly assigned the null value.</p>
 	 * @return String value or null if no value is found.
 	 */
 	public String getFirstValue() {
 		if(this.values == null || this.values.size() == 0) return null;
 		return this.values.get(0);
 	}
 	
 	/**
 	 * Check whether there are any values.
 	 * This returns true if there is at least one value
 	 * for this metadatum item.
 	 * @return true if this metadatum has values.
 	 */
 	public boolean hasValues() {
 		if(this.values.size() > 0) return true;
 		return false;
 	}
 	
 	/**
 	 * See if value exists.
 	 * This does a case-insensitive (equalsIgnoreCase) search through values, returning
 	 * true if at least one matches the passed-in value. If <code>null</code> is passed in,
 	 * it will always return <code>false</code>.
 	 * @param value See if this value exists in Metadatum.
 	 * @return true if value exists, false otherwise.
 	 */
 	public boolean hasValue(String value) {
 		if(this.values == null || value == null) return false;
 		for(String v: this.values) {
			//System.err.format("### Does :%s: match :%s:?\n", v, value);
 			if(value.equalsIgnoreCase(v)) return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Set the data type that will be used for all values. 
 	 * See the discussion on getDataType.
 	 * @see #getDataType()
 	 * @param dataType
 	 */
 	public void setDataType(String dataType) {
 		this.dataType = dataType;
 	}
 	
 	/**
 	 * Set the values for this metadatum.
 	 * This sets the current values to the values in the list passed in.
 	 * That means that any existing values will be lost.
 	 * @param vals
 	 */
 	public void setValues(List<String> vals) {
 		this.values = vals;
 	}
 	
 	/**
 	 * Add a new value to the end of the values list.
 	 * @param val
 	 */
 	public void addValue(String val) {
 		this.values.add(val);
 	}
 	
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		sb.append(this.name);
 		sb.append("(");
 		sb.append(this.values.size());
 		sb.append("): ");
 		for(int i = 0; i < this.values.size(); ++i) {
 			if(i > 0) sb.append(", ");
 			sb.append(this.values.get(i));
 		}
 		return sb.toString();
 	}
 	
 	/*
 	public String toXML() {
 		
 	}
 	*/
 }
