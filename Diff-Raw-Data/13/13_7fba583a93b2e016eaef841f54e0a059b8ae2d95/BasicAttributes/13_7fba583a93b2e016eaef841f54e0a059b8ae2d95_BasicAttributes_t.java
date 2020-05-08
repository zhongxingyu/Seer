 /*
 
 Copyright: 2010 Bindley Bioscience Center, Purdue University
 
 License: X11 license.
 
 	Permission is hereby granted, free of charge, to any person
 	obtaining a copy of this software and associated documentation
 	files (the "Software"), to deal in the Software without
 	restriction, including without limitation the rights to use,
 	copy, modify, merge, publish, distribute, sublicense, and/or sell
 	copies of the Software, and to permit persons to whom the
 	Software is furnished to do so, subject to the following
 	conditions:
 
 	The above copyright notice and this permission notice shall be
 	included in all copies or substantial portions of the Software.
 
 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 	EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 	OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 	NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 	HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 	WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 	FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 	OTHER DEALINGS IN THE SOFTWARE.
 
 */
 
 package edu.purdue.bbc.util.attributes;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * A class containing arbitrary attributes.
  */
 public class BasicAttributes<T> implements Attributes<T> {
 	protected Map<String,T> attributes = new HashMap<String,T>( );
 	protected boolean caseSensitive = true;
 
 	/**
 	 * Creates a new BasicAttributes object.
 	 */
 	public BasicAttributes( ) { }
 
 	/**
 	 * Creates a new Basicattributes object with the specified case sensitivity.
 	 * When case sensitivity is set to false, all attribute names are forced to
 	 * lower case.
 	 * 
 	 * @param caseSensitive false forces attribute names should be forced to
 	 *	lower case. 
 	 */
 	public BasicAttributes( boolean caseSensitive ) {
 		this.caseSensitive = caseSensitive;
 	}
 
 	/**
 	 * Gets an attribute for this object.
 	 * 
 	 * @param attribute The attribute to retrieve.
 	 * @return The value of the requested attribute, or null if it does not exist.
 	 */
 	public T getAttribute( String attribute ) {
 		if ( !this.caseSensitive ) {
 			attribute = attribute.toLowerCase( );
 		}
 		return this.attributes.get( attribute );
 	}
 
 	/**
 	 * Gets the attributes of this object as a Map.
 	 * 
 	 * @return A Map containing the attributes of this object.
 	 */
 	public Map<String,T> getAttributes( ) {
 		return this.attributes;
 	}
 
 	/**
 	 * Determines if this object has the requested attribute.
 	 * 
 	 * @param attribute The attribute to check for.
 	 * @return A boolean indicating whether or not the object has the requested
 	 *	attribute.
 	 */
 	public boolean hasAttribute( String attribute ) {
 		if ( !this.caseSensitive ) {
 			attribute = attribute.toLowerCase( );
 		}
 		return this.attributes.containsKey( attribute );
 	}
 
 	/**
 	 * Sets an attribute for this object.
 	 * 
 	 * @param attribute The attribute to set.
 	 * @param value The new value for the specified attribute.
 	 */
 	public T setAttribute( String attribute, T value ) {
 		if ( !this.caseSensitive ) {
 			attribute = attribute.toLowerCase( );
 		}
 		return this.attributes.put( attribute, value );
 	}
 
 	/**
 	 * Sets multiple Attributes for this object
 	 * 
 	 * @param map A map containing all of the attributes to be set.
 	 */
 	public void setAttributes( Map<String,T> map ) {
 		for ( Map.Entry<String,T> attribute : map.entrySet( )) {
 			if ( !this.caseSensitive ) {
 				this.attributes.put( attribute.getKey( ).toLowerCase( ), 
 				                     attribute.getValue( ));
 			} else {
 				this.attributes.put( attribute.getKey( ), attribute.getValue( ));
 			}
 		}
 	}
 
 	/**
 	 * Sets multiple Attributes for this object
 	 * 
	 * @param attributes An attributes object containing all of the attributes 
	 *	to be set.
 	 */
 	public void setAttributes( Attributes<T> attributes ) {
 		for ( Map.Entry<String,T> attribute : 
 			    attributes.getAttributes( ).entrySet( )) {
 			if ( !this.caseSensitive ) {
 				this.attributes.put( attribute.getKey( ).toLowerCase( ), 
 				                     attribute.getValue( ) );
 			} else {
 				this.attributes.put( attribute.getKey( ), attribute.getValue( ) );
 			}
 		}
 	}
 
 	/**
 	 * Removes the specified attribute and returns it's value.
 	 * 
 	 * @param attribute The attribute to remove
 	 * @return The value of the requested attribute, or null if it does not exist.
 	 */
 	public T removeAttribute( String attribute ) {
 		if ( !this.caseSensitive )
 			attribute = attribute.toLowerCase( );
 		return this.attributes.remove( attribute );
 	}
 }
 
