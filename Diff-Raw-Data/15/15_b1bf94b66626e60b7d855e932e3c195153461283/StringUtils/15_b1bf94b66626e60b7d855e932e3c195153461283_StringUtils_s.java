 /**
  * Copyright (c) 2004-2007 Rensselaer Polytechnic Institute
  * Copyright (c) 2007 NEES Cyberinfrastructure Center
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * For more information: http://nees.rpi.edu/3dviewer/
  */
 
 package org.nees.rpi.util;
 
 import java.io.File;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 /**
  * A class that serves several common helper functions
  * for string manipulation and parsing.
  */
 public class StringUtils
 {
 	/**
 	 * Converts the first letter of the passed argument to uppercase,
 	 * and makes the rest lowercase.
 	 * @param str	the string to convert
 	 * @return		the converted string
 	 */
 	public static String convertToProperCase(String str)
 	{
 		char[] chars	= str.trim().toLowerCase().toCharArray();
 		boolean found	= false;
 
 		for (int i=0; i<chars.length; i++)
 		{
 			 if (!found && Character.isLetter(chars[i]))
 			 {
 	         	chars[i]	= Character.toUpperCase(chars[i]);
 	         	found		= true;
	         } else if (Character.isWhitespace(chars[i]))
 			 {
 	         	found		= false;
 	         }
 	    }
 
 		return String.valueOf(chars);
 	}
 
 	/**
 	 * Gets the extension of a file.
 	 * @param filename		the filename containing the extension
 	 * @return				file extension, or null if no extension is found
 	 */
 	public static String getFileExtension(String filename)
 	{
 		String ext = null;
 		int i = filename.lastIndexOf('.');
 
 		if (i > 0 &&  i < filename.length() - 1)
				ext = filename.substring(i+1).toLowerCase();
 
 		return ext;
 	}
 
 	/**
 	 * Gets the name without the extension for the passed file name
 	 * @param filename		the filename with the extension
 	 * @return				the filename withot the extension, if no extension is found
 	 * 						the same file name is returned
 	 */
 	public static String getFileNameWithoutExtension(String filename)
 	{
 		int i = filename.lastIndexOf('.');
 
 		if (i > 0 &&  i < filename.length() - 1)
				return filename.substring(0, i);
 
 		return filename;
 	}
 
 	/**
 	 * Replaces the extension on a filename to the new one
 	 * @param filename		the original file name
 	 * @param newExtension	the new extension to use
 	 * @return				the new file name with the replaced extension
 	 */
 	public static String replaceFileExtension(String filename, String newExtension)
 	{
 		String oldExtension = getFileExtension(filename);
		return oldExtension == null ?
				filename + "." + newExtension :
				getFileNameWithoutExtension(filename) + "." + newExtension;
 	}
 
 	/**
 	 * Checks if the passed string is a number.
 	 * @return
 	 * 		Returns true if the string is a number, false otherwise.
 	 */
 	public static boolean isNumber(String s)
 	{
 		try
 		{
 			Float.parseFloat(s);
 		}
 		catch (NumberFormatException e) //not a number, hence the exception
 		{
 			return false;
 		}
 
 		return true;
 	}
 }
