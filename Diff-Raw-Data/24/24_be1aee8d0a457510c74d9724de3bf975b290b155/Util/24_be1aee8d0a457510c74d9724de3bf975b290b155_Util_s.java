 /*===========================================================================*/
 /* Copyright (C) 2008 Yves Savourel (at ENLASO Corporation)                  */
 /*---------------------------------------------------------------------------*/
 /* This library is free software; you can redistribute it and/or modify it   */
 /* under the terms of the GNU Lesser General Public License as published by  */
 /* the Free Software Foundation; either version 2.1 of the License, or (at   */
 /* your option) any later version.                                           */
 /*                                                                           */
 /* This library is distributed in the hope that it will be useful, but       */
 /* WITHOUT ANY WARRANTY; without even the implied warranty of                */
 /* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   */
 /* General Public License for more details.                                  */
 /*                                                                           */
 /* You should have received a copy of the GNU Lesser General Public License  */
 /* along with this library; if not, write to the Free Software Foundation,   */
 /* Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA               */
 /*                                                                           */
 /* See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html */
 /*===========================================================================*/
 
 package net.sf.okapi.common;
 
 /**
  * Collection of various all-purpose helper functions.
  */
 public class Util {
 
 	/**
 	 * Removes from the from of a string any of the specified characters. 
	 * @param p_sText String to trim.
	 * @param p_sChars List of the characters to trim.
 	 * @return The trimmed string.
 	 */
	static public String trimStart (String p_sText,
		String p_sChars)
 	{
		if ( p_sText == null ) return p_sText;
 		int n = 0;
		while ( n < p_sText.length() ) {
			if ( p_sChars.indexOf(p_sText.charAt(n)) == -1 ) break;
 			n++;
 		}
		if ( n >= p_sText.length() ) return "";
		if ( n > 0 ) return p_sText.substring(n);
		return p_sText;
 	}
 
 }
