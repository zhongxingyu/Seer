 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common.encoder;
 
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetEncoder;
 
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.filters.IEncoder;
 
 /**
  * Implements IEncoder for properties-type format.
  */
 public class PropertiesEncoder implements IEncoder {
 	
 	private CharsetEncoder outputEncoder;
	private boolean escapeAll;
 
 	/**
 	 * Creates a new PropertiesEncoder object, with US-ASCII as the encoding, and
 	 * escaping all extended characters.
 	 */
 	public PropertiesEncoder () {
		escapeAll = true;
 		outputEncoder = Charset.forName("us-ascii").newEncoder();
 	}
 	
 	public void setOptions (IParameters params,
 		String encoding)
 	{
 		outputEncoder = Charset.forName(encoding).newEncoder();
 		//TODO: get escapeAll from params
 	}
 
 	public String encode (String text,
 		int context)
 	{
 		StringBuilder escaped = new StringBuilder();
 		for ( int i=0; i<text.length(); i++ ) {
 			if ( text.codePointAt(i) > 127 ) {
 				if ( escapeAll ) {
 					escaped.append(String.format("\\u%04x", text.codePointAt(i))); 
 				}
 				else {
 					if ( outputEncoder.canEncode(text.charAt(i)) )
 						escaped.append(text.charAt(i));
 					else
 						escaped.append(String.format("\\u%04x", text.codePointAt(i)));
 				}
 			}
 			else {
 				switch ( text.charAt(i) ) {
 				case '\n':
 					escaped.append("\\n");
 					break;
 				case '\t':
 					escaped.append("\\t");
 					break;
 				default:
 					escaped.append(text.charAt(i));
 					break;
 				}
 			}
 		}
 		return escaped.toString();
 	}
 
 	public String encode (char value,
 		int context)
 	{
 		if ( value > 127 ) {
 			if ( escapeAll ) {
 				return String.format("\\u%04x", value); 
 			}
 			else {
 				if ( outputEncoder.canEncode(value) )
 					return String.valueOf(value);
 				else
 					return String.format("\\u%04x", value);
 			}
 		}
 		else {
 			switch ( value ) {
 			case '\n':
 				return "\\n";
 			case '\t':
 				return "\\t";
 			default:
 				return String.valueOf(value);
 			}
 		}
 	}
 
 	public String toNative (String propertyName,
 		String value)
 	{
 		// PROP_ENCODING: Not applicable
 		// PROP_LANGUGE: Not applicable
 		
 		// No changes for the other values
 		return value;
 	}
 
 }
