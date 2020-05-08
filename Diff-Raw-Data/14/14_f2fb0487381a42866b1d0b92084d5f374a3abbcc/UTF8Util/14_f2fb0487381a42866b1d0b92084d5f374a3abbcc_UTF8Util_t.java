 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.common.ui.internal.utils;
 
 /**
 * TODO: this class doesn't seem to be in use
  * @author mengbo
  * @version 1.5
  */
public final class UTF8Util {
 	private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
 			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
 
	/**
	 * @param s
	 * @return the unwound string
	 */
 	public static String unwindEscapeChars(String s) {
 		StringBuffer sb = new StringBuffer(s.length());
 		int length = s.length();
 		for (int i = 0; i < length; i++) {
 			char c = s.charAt(i);
 			sb.append(getUnwoundString(c));
 		}
 		return sb.toString();
 	}
 
 	private static String getUnwoundString(char c) {
 		switch (c) {
 		case '\b':
 			return "\\b";//$NON-NLS-1$
 		case '\t':
 			return "\\t";//$NON-NLS-1$
 		case '\n':
 			return "\\n";//$NON-NLS-1$
 		case '\f':
 			return "\\f";//$NON-NLS-1$	
 		case '\r':
 			return "\\r";//$NON-NLS-1$
 
 			// These can be used unescaped in properties file:
 		case '\"':
 			return "\\\"";//$NON-NLS-1$
 		case '\'':
 			return "\\\'";//$NON-NLS-1$
 
 		case '\\':
 			return "\\\\";//$NON-NLS-1$
 
 		case '=':
 			return "\\=";//$NON-NLS-1$
 
 			// This is only done when writing to the .properties file in
 			// #unwindValue(String)
 			// case '!':
 			// return "\\!";//$NON-NLS-1$
 			// case '#':
 			// return "\\#";//$NON-NLS-1$
 
 		default:
 			if (((c < 0x0020) || (c > 0x007e))) {
 				return new StringBuffer().append('\\').append('u').append(
 						toHex((c >> 12) & 0xF)).append(toHex((c >> 8) & 0xF))
 						.append(toHex((c >> 4) & 0xF)).append(toHex(c & 0xF))
 						.toString();
 
 			}
 			return String.valueOf(c);
 		}
 	}
 
 	private static char toHex(int halfByte) {
 		return HEX_DIGITS[(halfByte & 0xF)];
 	}
 
	private UTF8Util()
	{
		// no instantiation
	}
 }
