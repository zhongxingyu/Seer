 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * An assortment of string utilities to close the gap to J2SE 1.4
  * 
  * @author kgilmer
  * 
  */
 public class StringUtil {
 
 	/**
 	 * custom string splitting function as CDC/Foundation does not include
 	 * String.split();
 	 * 
 	 * @param s
 	 *            Input String
 	 * @param seperator
 	 * @return
 	 */
 	public static String[] split(String s, String seperator) {
 		if (s == null || seperator == null || s.length() == 0 || seperator.length() == 0) {
 			return (new String[0]);
 		}
 
 		List tokens = new ArrayList();
 		String token;
 		int index_a = 0;
 		int index_b = 0;
 
 		while (true) {
 			index_b = s.indexOf(seperator, index_a);
 			if (index_b == -1) {
 				token = s.substring(index_a);
 
 				if (token.length() > 0) {
 					tokens.add(token);
 				}
 
 				break;
 			}
 			token = s.substring(index_a, index_b);
			token.trim();
 			if (token.length() >= 0) {
 				tokens.add(token);
 			}
 			index_a = index_b + seperator.length();
 		}
 		String[] str_array = new String[tokens.size()];
 		for (int i = 0; i < str_array.length; i++) {
 			str_array[i] = (String) (tokens.get(i));
 		}
 		return str_array;
 	}
 
 	/**
 	 * Replaces a {@link String} within a {@link String}
 	 * 
 	 * @param target
 	 *            {@link String} where replacement needs to be done
 	 * @param from
 	 *            {@link String} to replace from
 	 * @param to
 	 *            {@link String} to replace to
 	 * @return
 	 */
 	public static String replace(String target, String from, String to) {
 		int start = target.indexOf(from);
 		if (start == -1)
 			return target;
 		int lf = from.length();
 		char[] targetChars = target.toCharArray();
 		StringBuffer buffer = new StringBuffer();
 		int copyFrom = 0;
 		while (start != -1) {
 			buffer.append(targetChars, copyFrom, start - copyFrom);
 			buffer.append(to);
 			copyFrom = start + lf;
 			start = target.indexOf(from, copyFrom);
 		}
 		buffer.append(targetChars, copyFrom, targetChars.length - copyFrom);
 		return buffer.toString();
 	}
 
 	/**
 	 * Joins elements in {@link List} with a delimiter {@link String}
 	 * 
 	 * @param list
 	 * @param delimiter
 	 * @return
 	 */
 	public static String join(List list, String delimiter) {
 		String out = "";
 		for (int i = 0; i < list.size(); i++) {
 			out += list.get(i);
 			if (i < list.size() - 1) {
 				out += delimiter;
 			}
 		}
 		return out;
 	}
 
 	/**
 	 * Remove extra white spaces
 	 * 
 	 * @param source
 	 * @return
 	 */
 	public static String squeeze(String source) {
 		char[] arr = source.toCharArray();
 		String out = "";
 		char prev = ' ';
 		for (int i = 0; i < arr.length; i++) {
 			if (!(prev == ' ' && arr[i] == ' ')) {
 				out += arr[i];
 			}
 			prev = arr[i];
 		}
 		return out;
 	}
 }
