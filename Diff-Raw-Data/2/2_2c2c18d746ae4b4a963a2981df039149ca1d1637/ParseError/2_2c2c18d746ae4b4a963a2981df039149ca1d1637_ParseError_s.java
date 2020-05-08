 /*
  * Author: C.Williams
  * 
  * Copyright (c) 2004 RubyPeople.
  * 
  * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. You
  * can get copy of the GPL along with further information about RubyPeople and
  * third party software bundled with RDT in the file
  * org.rubypeople.rdt.core_0.4.0/RDT.license or otherwise at
  * http://www.rubypeople.org/RDT.license.
  * 
  * RDT is free software; you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  * 
  * RDT is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * RDT; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
  * Suite 330, Boston, MA 02111-1307 USA
  */
 package org.rubypeople.rdt.internal.core.parser;
 
 import org.eclipse.core.resources.IMarker;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyElement;
 
 public class ParseError {
 
 	private String error;
 	private int lineNum;
 	private int start;
 	private int end;
 	private int severity;
 
 	public static final int WARNING = IMarker.SEVERITY_WARNING;
 	public static final int INFO = IMarker.SEVERITY_INFO;
	public static final int ERROR = IMarker.SEVERITY_INFO;
 
 	/**
 	 * @param error
 	 * @param lineNum
 	 * @param start
 	 * @param end
 	 */
 	public ParseError(String error, int lineNum, int start, int end, int severity) {
 		this.error = error;
 		this.lineNum = lineNum;
 		this.start = start;
 		this.end = end;
 		this.severity = severity;
 	}
 
 	/**
 	 * @param string
 	 * @param element
 	 */
 	public ParseError(String string, RubyElement element, int severity) {
 		this(string, element.getStart().getLineNumber(), element.getStart().getOffset(), element.getStart().getOffset() + element.getName().length(), severity);
 	}
 
 	/**
 	 * @param error
 	 * @param line
 	 * @param token
 	 * @param severity
 	 */
 	public ParseError(String error, int line, RubyToken token, int severity) {
 		this(error, line, token.getOffset(), token.getText().length(), severity);
 	}
 
 	/**
 	 * @return
 	 */
 	public int getLine() {
 		return lineNum;
 	}
 
 	/**
 	 * @return
 	 */
 	public int getStart() {
 		return start;
 	}
 
 	/**
 	 * @return
 	 */
 	public int getEnd() {
 		return end;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getError() {
 		return error;
 	}
 
 	/**
 	 * @return
 	 */
 	public Integer getSeverity() {
 		return new Integer(severity);
 	}
 
 }
