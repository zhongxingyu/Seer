 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.validators.core;
 
 import java.util.List;
 
 import java.util.ArrayList;
 
 public class CommandLine {
 
 	private final List args = new ArrayList();
 
 	public CommandLine() {
 		// default constructor
 	}
 
 	public CommandLine(String value) {
 		final String[] parts = value.split("\\s+"); //$NON-NLS-1$
 		add(parts);
 	}
 
 	public void add(String arg) {
 		args.add(arg);
 	}
 
 	public void add(String[] parts) {
 		for (int i = 0; i < parts.length; ++i) {
			args.add(parts[i]);
 		}
 	}
 
 	public void add(CommandLine other) {
 		args.addAll(other.args);
 	}
 
 	public void add(int index, String arg) {
 		args.add(index, arg);
 	}
 
 	public void replaceSequence(char pattern, String value) {
 		for (int i = 0, size = args.size(); i < size; ++i) {
 			final String arg = (String) args.get(i);
 			final String replaced = replace(arg, pattern, value);
 			if (!arg.equals(replaced)) {
 				args.set(i, replaced);
 			}
 		}
 	}
 
 	public void clear() {
 		args.clear();
 	}
 
 	/**
 	 * @param arg
 	 * @param pattern
 	 * @param value
 	 * @return
 	 */
 	private static String replace(String arg, char pattern, String value) {
 		StringBuffer buffer = new StringBuffer();
 		for (int i = 0; i < arg.length(); ++i) {
 			char c = arg.charAt(i);
 			if (c == '%' && i < arg.length() - 1
 					&& arg.charAt(i + 1) == pattern) {
 				buffer.append(value);
 				i++;
 			} else {
 				buffer.append(c);
 			}
 		}
 		return buffer.toString();
 	}
 
 	public String[] toArray() {
 		return (String[]) args.toArray(new String[args.size()]);
 	}
 
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < args.size(); ++i) {
			if (i != 0) {
				sb.append(' ');
			}
			sb.append(args.get(i));
		}
		return sb.toString();
	}

 	/**
 	 * @param string
 	 * @return
 	 */
 	public boolean contains(String string) {
 		return args.contains(string);
 	}
 }
