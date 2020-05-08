 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jsnative.global;
 
 import org.eclipse.vjet.dsf.jsnative.anno.Constructor;
 import org.eclipse.vjet.dsf.jsnative.anno.Function;
 import org.eclipse.vjet.dsf.jsnative.anno.JsSupport;
 import org.eclipse.vjet.dsf.jsnative.anno.JsVersion;
 import org.eclipse.vjet.dsf.jsnative.anno.OverLoadFunc;
 import org.eclipse.vjet.dsf.jsnative.anno.Property;
 import org.eclipse.vjet.dsf.jsnative.anno.Static;
 
 /**
  * 
  * An object representing a series of characters in a string. 
  *
  */
 @JsSupport( {JsVersion.MOZILLA_ONE_DOT_ZERO, JsVersion.JSCRIPT_ONE_DOT_ZERO})
 public interface String extends Object {
 	
 	/**
 	 * The String constructor: new String(string)
 	 * @param value
 	 */
 	@Constructor void String(Object value);
 	
 	@Constructor void String();
 	
 	/**
 	 * Number of characters in the string.
 	 */
 	@Property Number getLength();
 	
 	/**
 	 * Creates an HTML anchor that is used as a hypertext target.
 	 * @param anchorname name attribure of anchor
 	 */
 	@Function String anchor(String anchorname);
 	
 	/**
 	 * Returns a copy of a string surrounded by BIG HTML tags.
 	 */
 	@Function String big();
 	
 	/**
 	 * Returns a copy of a string surrounded by BLINK HTML tags.
 	 */
 	@Function String blink();
 	
 	/**
 	 * Returns a copy of a string surrounded by BOLD HTML tags.
 	 */
 	@Function String bold();
 	
 	/**
 	 * Returns the specified character from a string.
 	 * @param index An integer between 0 and 1 less than the length of the string. 
 	 */
 	@Function String charAt(Number index);
 	
 	/**
 	 * Returns a number indicating the Unicode value of the character at the given index.
 	 * @param index An integer between 0 and 1 less than the length of the string. 
 	 */
 	@JsSupport( {JsVersion.MOZILLA_ONE_DOT_TWO})
 	@Function Number charCodeAt(Number index);
 	
 	/**
 	 * Combines the text of two or more strings and returns a new string.
 	 * @param Strings to concatenate to this string.
 	 */
 	@JsSupport( {JsVersion.MOZILLA_ONE_DOT_TWO})
	@Function String concat(String ...strings);
 	
 	/**
 	 * Returns a copy of a string surrounded by TT HTML tags.
 	 */
 	@Function String fixed();
 	
 	/**
 	 * Returns a copy of a string surrounded by FONT COLOR tags.
 	 * @param color A string expressing the color as a hexadecimal RGB triplet or as a string literal. 
 	 */
 	@Function String fontcolor(String color);
 	
 	/**
 	 * Returns a copy of a string surrounded by FONT SIZE tags.
 	 * @param size An integer between 1 and 7, a string representing a signed integer between 1 and 7.
 	 */
 	@Function String fontsize(Number size);
 	
 	/**
 	 * Returns a string created by using the specified sequence of Unicode values.
 	 * @param numbers A sequence of numbers that are Unicode values.
 	 */
 	@Function @Static String fromCharCode(Number ...numbers);
 	
 	/**
 	 * Returns the index within the calling String object of the 
 	 * first occurrence of the specified value, starting the search 
 	 * at fromIndex, or -1 if the value is not found.
 	 * @param searchvalue A string representing the value to search for.
 	 * @param fromindex The location within the calling string to start the search 
 	 * from. It can be any integer between 0 and the length of the string. 
 	 * The default value is 0.
 	 */
 	@OverLoadFunc Number indexOf(String searchvalue);
 	
 	/**
 	 * Returns the index within the calling String object of the 
 	 * first occurrence of the specified value, starting the search 
 	 * at fromIndex, or -1 if the value is not found.
 	 * @param searchvalue A string representing the value to search for.
 	 * @param fromindex The location within the calling string to start the search 
 	 * from. It can be any integer between 0 and the length of the string. 
 	 * The default value is 0.
 	 */
 	@OverLoadFunc Number indexOf(String searchvalue, Number fromindex);
 	
 	/**
 	 * Returns a copy of a string surrounded by I tags.
 	 */
 	@Function String italics();
 	
 	/**
 	 * Returns the index within the calling String object of the 
 	 * last occurrence of the specified value, or -1 if not found. 
 	 * The calling string is searched backward, starting at fromIndex.
 	 * @param searchvalue A string representing the value to search for.
 	 * @param fromindex The location within the calling string to start the search 
 	 * from. It can be any integer between 0 and the length of the string. 
 	 * The default value is the length of the string. 
 	 */
 	@OverLoadFunc Number lastIndexOf(String searchvalue);
 	
 	/**
 	 * Returns the index within the calling String object of the 
 	 * last occurrence of the specified value, or -1 if not found. 
 	 * The calling string is searched backward, starting at fromIndex.
 	 * @param searchvalue A string representing the value to search for.
 	 * @param fromindex The location within the calling string to start the search 
 	 * from. It can be any integer between 0 and the length of the string. 
 	 * The default value is the length of the string. 
 	 */
 	@OverLoadFunc Number lastIndexOf(String searchvalue, Number fromindex);
 	
 	/**
 	 * Returns a copy of a string surrounded by HTML hypertext link tags.
 	 * @param href Any string that specifies the HREF attribute of the A tag; 
 	 * it should be a valid URL (relative or absolute).
 	 */
 	@Function String link(String href);
 	
 	/**
 	 * Uses locale-specific ordering to compare two strings.
 	 * @param target A string to be compared
 	 */
 	@Function Number localeCompare(String target);
 	
 	/**
 	 * Used to match a regular expression against a string.
 	 * @param regexp Name of the regular expression. 
 	 * It can be a variable name or a literal. 
 	 */
 	@OverLoadFunc String[] match(RegExp regexp);
 	
 	/**
 	 * Used to match a regular expression against a string.
 	 * @param regexp Name of the regular expression. 
 	 * It can be a variable name or a literal. 
 	 */
 	@OverLoadFunc String[] match(String regexp);
 	
 	/**
 	 * Finds a match between a regular expression and a string, 
 	 * and replaces the matched substring with a new substring.
 	 * @param regexp The name of the regular expression. It can be a variable name or a literal. 
 	 * @param newSubStr The string to put in place of the string found with regexp. 
 	 */
 	@JsSupport( {JsVersion.MOZILLA_ONE_DOT_TWO})
 	@OverLoadFunc String replace(String regexp, String newSubStr);
 	
 	/**
 	 * Finds a match between a regular expression and a string, 
 	 * and replaces the matched substring with a new substring.
 	 * @param regexp A RegExp object 
 	 * @param newSubStr The string to put in place of the string found with regexp. 
 	 */
 	@JsSupport( {JsVersion.MOZILLA_ONE_DOT_TWO})
 	@OverLoadFunc String replace(RegExp regexp, String newSubStr);
 	
 	/**
 	 * Finds a match between a regular expression and a string, 
 	 * and replaces the matched substring with a new substring.
 	 * @param regexp The name of the regular expression. It can be a variable name or a literal. 
 	 * @param function A function to be invoked to create the new substring. 
 	 */
 	@JsSupport( {JsVersion.MOZILLA_ONE_DOT_THREE})
 	@OverLoadFunc String replace(String regexp, Function function);
 	
 	/**
 	 * Finds a match between a regular expression and a string, 
 	 * and replaces the matched substring with a new substring.
 	 * @param regexp A RegExp object 
 	 * @param function A function to be invoked to create the new substring. 
 	 */
 	@JsSupport( {JsVersion.MOZILLA_ONE_DOT_TWO})
 	@OverLoadFunc String replace(RegExp regexp, Function function);
 	
 	/**
 	 * Executes the search for a match between a regular expression 
 	 * and this String object.
 	 * @param regexp The name of the regular expression. It can be a variable name or a literal. 
 	 */
 	@Function Number search(Object regexp);
 	
 	/**
 	 * Extracts a section of a string and returns a new string.
 	 * @param start The zero-based index at which to begin extraction. 
 	 * @param end The zero-based index at which to end extraction. 
 	 * If omitted, slice extracts to the end of the string. 
 	 */
 	@OverLoadFunc String slice(Number start);
 
 	/**
 	 * Extracts a section of a string and returns a new string.
 	 * @param start The zero-based index at which to begin extraction. 
 	 * @param end The zero-based index at which to end extraction. 
 	 * If omitted, slice extracts to the end of the string. 
 	 */
 	@OverLoadFunc String slice(Number start, Number end);
 	
 	/**
 	 * Returns a copy of a string surrounded by SMALL tags.
 	 */
 	@Function String small();
 	
 	/**
 	 * Splits a String object into an array of strings by 
 	 * separating the string into substrings.
 	 * @param separator Specifies the character to use for separating the string. 
 	 * The separator is treated as a string. If separator is omitted, 
 	 * the array returned contains one element consisting of the entire string.
 	 * @param howmany Integer specifying a limit on the number of splits to be found.
 	 */
 	@OverLoadFunc Array split(String separator);
 	/**
 	 * Splits a String object into an array of strings by 
 	 * separating the string into substrings.
 	 * @param separator Specifies the character to use for separating the string. 
 	 * The separator is treated as a string. If separator is omitted, 
 	 * the array returned contains one element consisting of the entire string.
 	 * @param howmany Integer specifying a limit on the number of splits to be found.
 	 */
 	@OverLoadFunc Array split(String separator, Number howmany);
 	
 	/**
 	 * Returns a copy of a string surrounded by STRIKE tags.
 	 */
 	@Function String strike();
 	
 	/**
 	 * Returns a copy of a string surrounded by SUB tags.
 	 */
 	@Function String sub();
 	
 	/**
 	 * Returns the characters in a string beginning at the specified 
 	 * location through the specified number of characters.
 	 * @param start Location at which to begin extracting characters.
 	 * @param length The number of characters to extract.
 	 */
 	@OverLoadFunc String substr(Number start);
 	
 	/**
 	 * Returns the characters in a string beginning at the specified 
 	 * location through the specified number of characters.
 	 * @param start Location at which to begin extracting characters.
 	 * @param length The number of characters to extract.
 	 */
 	@OverLoadFunc String substr(Number start, Number length);
 	
 	/**
 	 * Returns a subset of a String object.
 	 * @param start An integer between 0 and 1 less than the length of the string.
 	 * @param end An integer between 0 and 1 less than the length of the string.
 	 */
 	@OverLoadFunc String substring(Number start);
 	/**
 	 * Returns a subset of a String object.
 	 * @param start An integer between 0 and 1 less than the length of the string.
 	 * @param end An integer between 0 and 1 less than the length of the string.
 	 */
 	@OverLoadFunc String substring(Number start, Number stop);
 	
 	/**
 	 * Returns a copy of a string surrounded by SUP tags.
 	 */
 	@Function String sup();
 	
 	/**
 	 * Returns the string value converted to lowercase. 
 	 */
 	@Function String toLowerCase();
 	
 	/**
 	 * Returns a copy of a string in lowercase letters in 
 	 * a locale-specific format.
 	 */
 	@Function String toLocaleLowerCase();
 	
 	/**
 	 * Returns the string value converted to all uppercase.
 	 */
 	@Function String toUpperCase();
 	
 	/**
 	 * Returns a copy of a string in uppercase letters in 
 	 * a locale-specific format.
 	 */
 	@Function String toLocaleUpperCase();
 	
 	/**
 	 * Returns the primitive value of the specified object.
 	 */
 	@Function String valueOf();
 }
