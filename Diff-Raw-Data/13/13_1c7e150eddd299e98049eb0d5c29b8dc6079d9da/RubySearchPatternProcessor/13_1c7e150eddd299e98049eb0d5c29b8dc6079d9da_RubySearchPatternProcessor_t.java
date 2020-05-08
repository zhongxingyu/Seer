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
 package org.eclipse.dltk.ruby.internal.core.search;
 
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.core.ISearchPatternProcessor;
 
 public class RubySearchPatternProcessor implements ISearchPatternProcessor {
 
 	private static final String TYPE_DELIMITER = "::"; //$NON-NLS-1$
 
 	private static final String METHOD_DELIMITER = "::"; //$NON-NLS-1$
 
 	public char[] extractDeclaringTypeQualification(String pattern) {
 		final int pos = pattern.lastIndexOf(METHOD_DELIMITER);
 		if (pos != -1) {
 			final String type = pattern.substring(0, pos);
 			return extractTypeQualification(type);
 		}
 		return null;
 	}
 
 	public char[] extractDeclaringTypeSimpleName(String pattern) {
 		final int pos = pattern.lastIndexOf(METHOD_DELIMITER);
 		if (pos != -1) {
 			final String type = pattern.substring(0, pos);
			return extractTypeChars(type).toCharArray();
 		}
 		return null;
 	}
 
 	public char[] extractSelector(String pattern) {
 		final int pos = pattern.lastIndexOf(METHOD_DELIMITER);
 		if (pos != -1) {
 			final int begin = pos + METHOD_DELIMITER.length();
 			if (begin < pattern.length()) {
 				final char[] result = new char[pattern.length() - begin];
 				pattern.getChars(begin, pattern.length(), result, 0);
 				return result;
 			}
 		}
 		return pattern.toCharArray();
 	}
 
	public String extractTypeChars(String pattern) {
 		final int pos = pattern.lastIndexOf(TYPE_DELIMITER);
 		if (pos != -1) {
 			final int begin = pos + TYPE_DELIMITER.length();
 			if (begin < pattern.length()) {
				return pattern.substring(begin);
 			}
 		}
		return pattern;
 	}
 
 	public char[] extractTypeQualification(String pattern) {
 		final int pos = pattern.lastIndexOf(TYPE_DELIMITER);
 		if (pos != -1) {
 			final char[] result = new char[pos];
 			pattern.getChars(0, pos, result, 0);
 			return CharOperation.replace(result, TYPE_DELIMITER.toCharArray(),
 					new char[] { '$' });
 		}
 		return null;
 	}
 
 	public String getDelimiterReplacementString() {
 		return TYPE_DELIMITER;
 	}
 
 }
