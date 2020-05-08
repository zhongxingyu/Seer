 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
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
 
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.resource.Property;
 
 /**
  * Implements IEncoder for TS file format.
  */
 public class TSEncoder implements IEncoder {
 	
 //	private String lineBreak;
 	
 	public void setOptions (IParameters params,
 		String encoding,
 		String lineBreak)
 	{
 		//this.lineBreak = lineBreak;
 	}
 
 	public String encode (String text,
 		int context)
 	{
 		StringBuilder escaped = new StringBuilder();
 		char ch;
 		for ( int i=0; i<text.length(); i++ ) {
 			ch = text.charAt(i);
 			if((ch==0x9) || (ch==0xA)|| (ch==0xD) || ((ch >= 0x20) && (ch<=0xD7FF)) || ((ch >= 0xE000) && (ch<=0xFFFD)) || ((ch >= 0x10000) && (ch<=0x10FFFF))){
 				escaped.append(ch);
 			}else{
 				escaped.append("<byte value=\"x"+Integer.toHexString(ch)+"\">");
 			}
 		}
 		return escaped.toString();
 	}
 
 	public String encode(int codePoint, int context) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String encode(char value, int context) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String toNative(String propertyName, String value) {
 		// PROP_LANGUAGE: Not applicable
 		// PROP_ENCODING: No change
 		
 		// No changes for the other values
 		if ( propertyName.equals(Property.APPROVED) ) {
 			if (( value != null ) && ( value.equals("yes") )) {
 				return "";
 			}else{
				return "type=\"unfinished\"";
 			}
 		}
 		return value;		
 	}
 }
