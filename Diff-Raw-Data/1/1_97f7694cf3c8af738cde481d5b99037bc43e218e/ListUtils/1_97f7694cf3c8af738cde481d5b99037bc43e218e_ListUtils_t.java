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
 
 package net.sf.okapi.common;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Collection of helper functions for manipulating lists.
  * 
  * @version 0.1, 09.06.2009
  */
 public class ListUtils {
 	
 	/**
 	 * Splits up a string of comma-separated substrings into a string list of those substrings.
 	 * @param st string of comma-separated substrings 
 	 * @return a list of substrings 
 	 */
 	public static List<String> stringAsList(String st) {
 
 		return listTrimValues(stringAsList(st, ","));		
 	}
 	
 	/**
 	 * Splits up a string of comma-separated substrings into a string list of those substrings.
 	 * @param st string of comma-separated substrings
 	 * @param delimiter a string delimiting substrings in the string 
 	 * @return a list of substrings
 	 */
 	public static List<String> stringAsList(String st, String delimiter) {
 
 		if (Util.isEmpty(st)) return new ArrayList<String>();
 		
 		ArrayList<String> res = new ArrayList<String>();
 		if (res == null) 
 			return res;
 		
 		if (Util.isEmpty(delimiter)) {
 						
 			res.add(st);
 			return res;
 		}
 	
 //		String[] parts = st.split(delimiter);
 //		for (String part : parts) 
 //			part = part.trim();
 //		
 //		return Arrays.asList(parts);
 		
 		int start = 0;
 		int len = delimiter.length();
 		
 		while (true) {
 			
 			int index = st.substring(start).indexOf(delimiter);
 			if (index == -1) break;
 			
 			res.add(st.substring(start, start + index));
 			start += index + len;
 		}
 		
 		if (start <= st.length())
 			res.add(st.substring(start, st.length()));
 		
 		return res;
 	}
 	
 	/**
 	 * Splits up a string of comma-separated substrings into an array of those substrings.
 	 * @param st string of comma-separated substrings
 	 * @return the generated array of strings
 	 */
 	public static String[] stringAsArray(String st) {
 		
 		List<String> list = stringAsList(st);
 		
 		if (Util.isEmpty(list))
 			return new String[] {};
 		
 		return (String[]) list.toArray(new String[] {});
 	}
 	
 	/**
 	 * Splits up a string of comma-separated substrings into an array of those substrings.
 	 * @param st string of comma-separated substrings
 	 * @param delimiter a string delimiting substrings in the string
 	 * @return
 	 */
 	public static String[] stringAsArray(String st, String delimiter) {
 		
 		List<String> list = stringAsList(st, delimiter);
 		
 		if (Util.isEmpty(list))
 			return new String[] {};
 
 		return (String[]) list.toArray(new String[] {});
 	}
 	
 	/**
 	 * Converts a string of comma-separated numbers into a list of integers.
 	 * @param st string of comma-separated numbers 
 	 * @return a list of integers 
 	 */
 	public static List<Integer> stringAsIntList (String st) {
 		
 		return stringAsIntList(st, ",");
 	}
 	
 	/**
 	 * Converts a string of comma-separated numbers into a list of integers and sorts the list ascendantly.
 	 * @param st string of comma-separated numbers 
 	 * @param delimiter a string delimiting numbers in the string
 	 * @return a list of integers 
 	 */
 	public static List<Integer> stringAsIntList(String st, String delimiter) {
 		
 		return stringAsIntList(st, delimiter, false);
 	}		
 	
 	/**
 	 * Converts a string of comma-separated numbers into a list of integers.
 	 * @param st string of comma-separated numbers 
 	 * @param delimiter a string delimiting numbers in the string
 	 * @param sortList if the numbers in the resulting list should be sorted (ascendantly)
 	 * @return a list of integers 
 	 */
 	public static List<Integer> stringAsIntList(String st, String delimiter, boolean sortList) {
 					
 		List<Integer> res = new ArrayList<Integer>(); // Always create the list event if input string is empty
 		if (Util.isEmpty(st)) return res;
 		
 		String[] parts = st.split(delimiter);
 		for (String part : parts) {
 			
 			if (Util.isEmpty(part.trim()))
 				res.add(0);
 			else
 				res.add(Integer.valueOf(part.trim()));
 		}
 		if (sortList) Collections.sort(res);
 		return res;
 	}
 	
 	/**
 	 * Remove empty trailing elements of the given list.
 	 * Possible empty elements in the head and middle of the list remain if located before a non-empty element.
 	 * @param list the list to be trimmed
 	 */
 	public static void listTrimTrail(List<String> list) {
 	
 		if (list == null) return;
 
 		for (int i = list.size() -1; i >= 0; i--) 
 			if (Util.isEmpty(list.get(i))) 
 				list.remove(i);
 			else
 				break;
 	}
 	
 	/**
 	 * Trim all values of the given list. Empty elements remain on the list, non-empty are trimmed from both sides.
 	 * @param list
 	 * @return the list with trimmed elements.
 	 */
 	public static List<String> listTrimValues(List<String> list) {
 		
 		if (list == null) return list;
 
 		List<String> res = new ArrayList<String>();
 		
 		for (String st : list)
 			if (Util.isEmpty(st)) 
 				res.add(st);
 			else
 				res.add(st.trim());
 			
 		return res;
 	}
 	
 	public static String listAsString(List<String> list) {
 		
 		return listAsString(list, ",");
 	}
 	
 	public static String listAsString(List<String> list, String delimiter) {
 		
 		if (list == null) return "";
 		String res = "";
 		
 		for (int i = 0; i < list.size(); i++) {
 			if (i > 0) 
 				res = res + delimiter + list.get(i);
 			else
 				res = list.get(i);			
 		}
 		
 		return res;
 	}
 
 	public static <E> void remove(List<E> list, int start, int end) {
 		
 		if (list == null) return;
 		if (Util.isEmpty(list)) return;
 		
 		//for (int i = start; (i >= 0 && i < end && i < list.size()); i++)
 		for (int i = start; (i < end); i++)
 			list.remove(start);
 			
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <E> List<E> copyItems(List<E> buffer, int start, int end) {
 	
 		if (buffer == null) return null;
 		if (Util.isEmpty(buffer)) return null;
 		if (buffer.getClass() == null) return null;
 		
 		List<E> res = null;
 		try {
 			res = buffer.getClass().newInstance();
 			
 			res.addAll(buffer.subList(start, end + 1));		
 			
 		} catch (InstantiationException e) {
 			//TODO: do something with the exception, or comment why it's swallowed
 		} catch (IllegalAccessException e) {
 			//TODO: do something with the exception, or comment why it's swallowed
 		}
 		
 		return res;
 	}
 	
 	public static <E> List<E> moveItems(List<E> buffer, int start, int end) {
 	
 		List<E> res = copyItems(buffer, start, end);
 		if (res == null) return null;
 		
 		buffer.subList(start, end + 1).clear();
 		
 		return res;
 	}
 	
 	public static <E> List<E> moveItems(List<E> buffer) {
 		
 		List<E> res = copyItems(buffer, 0, buffer.size() - 1);
 		if (res == null) return null;
 		
 		buffer.clear();
 		
 		return res;
 	}
 	
 	
 }
