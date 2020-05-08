 /*
  * Copyright (c) 2006, 2008 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.xpand.util;
 
 import java.util.Collection;
 
 public class ParserException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	private final ErrorLocationInfo[] errors;
 	private final String qualifiedResourceName;
 
	private static String getMessage(ErrorLocationInfo[] errors) {
 		assert errors != null && errors.length > 0;
		StringBuilder result = new StringBuilder();
 		for (ErrorLocationInfo errorLocationInfo : errors) {
 			result.append(errorLocationInfo.toString());
 			result.append("\n");
 		}
 		return result.toString();
 	}
 	
 	public ParserException(String qualifiedName, Collection<? extends ErrorLocationInfo> errors) {
 		this(qualifiedName, errors.toArray(new ErrorLocationInfo[errors.size()]));
 	}
 
 	public ParserException(String qualifiedName, ErrorLocationInfo... errors) {
		super(getMessage(errors));
 		assert errors != null && errors.length > 0;
 		this.errors = errors;
 		this.qualifiedResourceName = qualifiedName;
 	}
 
 	public ErrorLocationInfo[] getParsingErrors() {
 		return errors;
 	}
 	
 	public String getResourceName() {
 		return qualifiedResourceName;
 	}
 
 	public static class ErrorLocationInfo {
 		public final int startLine;
 		public final int startColumn;
 		public final int endLine;
 		public final int endColumn;
 		public final String message;
 
 		public ErrorLocationInfo(String message) {
 			this(message, -1, -1, -1, -1);
 		}
 
 		public ErrorLocationInfo(String message, int startLine, int startColumn, int endLine, int endColumn) {
 			this.message = message;
 			this.startLine = startLine;
 			this.startColumn = startColumn;
 			this.endLine = endLine;
 			this.endColumn = endColumn;
 		}
 		
 		public String toString() {
 			return startLine + ":" + startColumn + "-" + endLine + ":" + endColumn + " - " + message;
 		}
 	}
 }
