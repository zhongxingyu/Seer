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
 
 import java.util.Map;
 
 /**
  * Interface for a class containing arbitrary attributes.
  */
 public interface Attributes<T> {
 
 	/**
 	 * Gets an attribute for this object.
 	 * 
 	 * @param attribute The attribute to retrieve.
 	 * @return The value of the requested attribute, or null if it does not exist.
 	 */
 	public T getAttribute( String attribute );
 
 	/**
 	 * Gets the attributes of this object as a Map.
 	 * 
 	 * @return A Map containing the attributes of this object.
 	 */
 	public Map<String,T> getAttributes( );
 
 	/**
	 * Determines if this object has the requested attribute.
	 * 
	 * @param attribute The attribute to check for.
	 * @return A boolean indicating whether or not the object has the requested
	 *	attribute.
	 */
	public boolean hasAttribute( String attribute );
	/**
 	 * Sets an attribute for this object.
 	 * 
 	 * @param attribute The attribute to set.
 	 * @param value The new value for the specified attribute.
 	 */
 	public void setAttribute( String attribute, T value );
 
 	/**
 	 * Sets multiple Attributes for this object
 	 * 
 	 * @param map A map containing all of the attributes to be set.
 	 */
 	public void setAttributes( Map<String,T> map );
 
 	/**
 	 * Removes the specified attribute and returns it's value.
 	 * 
 	 * @param attribute The attribute to remove
 	 * @return The value of the requested attribute, or null if it does not exist.
 	 */
 	public T removeAttribute( String attribute );
 
 }
 
