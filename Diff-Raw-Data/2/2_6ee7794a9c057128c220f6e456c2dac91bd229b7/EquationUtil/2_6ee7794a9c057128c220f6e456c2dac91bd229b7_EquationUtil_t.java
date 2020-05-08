 /*
   File: EquationUtil.java
 
   Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)
 
   The Cytoscape Consortium is:
   - Institute for Systems Biology
   - University of California San Diego
   - Memorial Sloan-Kettering Cancer Center
   - Institut Pasteur
   - Agilent Technologies
 
   This library is free software; you can redistribute it and/or modify it
   under the terms of the GNU Lesser General Public License as published
   by the Free Software Foundation; either version 2.1 of the License, or
   any later version.
 
   This library is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
   MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
   documentation provided hereunder is on an "as is" basis, and the
   Institute for Systems Biology and the Whitehead Institute
   have no obligations to provide maintenance, support,
   updates, enhancements or modifications.  In no event shall the
   Institute for Systems Biology and the Whitehead Institute
   be liable to any party for direct, indirect, special,
   incidental or consequential damages, including lost profits, arising
   out of the use of this software and its documentation, even if the
   Institute for Systems Biology and the Whitehead Institute
   have been advised of the possibility of such damage.  See
   the GNU Lesser General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License
   along with this library; if not, write to the Free Software Foundation,
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package org.cytoscape.equations;
 
 
 public class EquationUtil {
 	/**
 	 *  @return "attribName" written as am attribute reference with a leading $-sign
 	 */
 	public static String attribNameAsReference(final String attribName) {
 		if (isSimpleAttribName(attribName))
 			return "$" + attribName;
 		else
 			return "${" + escapeAttribName(attribName) + "}";
 	}
 
 	/**
 	 *  @return "d" converted to a Long using Excel™ rules
 	 *  @throws IllegalArgumentException if "d" is outside the range of a long
 	 */
 	public static long doubleToLong(final double d) {
 		if (d > Long.MAX_VALUE || d < Long.MIN_VALUE)
 			throw new IllegalArgumentException("floating point value is too large to be converted to a Long!");
 
 		double x = ((Double)d).longValue();
		if (x != d && d < 0.0)
 			--x;
 
 		return (long)x;
 	}
 
 	/**
 	 *  @param attribName the name to test
 	 *  @return true if "attribName" start with a letter and consists of only letters and digits, else false
 	 */
 	private static boolean isSimpleAttribName(final String attribName) {
 		final int length = attribName.length();
 		if (length == 0)
 			throw new IllegalStateException("empty attribute names should never happen!");
 
 		if (!Character.isLetter(attribName.charAt(0)))
 			return false;
 
 		for (int i = 1; i < length; ++i) {
 			final char ch = attribName.charAt(i);
 			if (!Character.isLetter(ch) && !Character.isDigit(ch))
 				return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 *  @return "attribName" with characters that need to be backslash-escaped when written as
 	 *           part of an attribute refernce, escaped
 	 */
 	private static String escapeAttribName(final String attribName) {
 		final int length = attribName.length();
 		final StringBuilder escapedAttribName = new StringBuilder(length * 2);
 		for (int i = 0; i < length; ++i) {
 			final char ch = attribName.charAt(i);
 			switch (ch) {
 			case ' ':
 			case '\\':
 			case '{':
 			case '}':
 			case ':':
 			case ',':
 			case '(':
 			case ')':
 				escapedAttribName.append('\\');
 			}
 			escapedAttribName.append(ch);
 		}
 
 		return escapedAttribName.toString();
 	}
 }
