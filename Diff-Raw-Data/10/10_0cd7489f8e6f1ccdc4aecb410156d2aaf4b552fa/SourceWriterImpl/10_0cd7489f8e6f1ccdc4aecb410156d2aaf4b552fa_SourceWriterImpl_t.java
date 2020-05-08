 /**
  * SoCoFo Source Code Formatter
  * Copyright (C) 2009 Dirk Strauss <lexxy23@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /**
  * 
  */
 package com.googlecode.socofo.core.impl;
 
 import java.util.logging.Logger;
 
 import com.google.inject.Inject;
 import com.googlecode.socofo.core.api.LineHandler;
 import com.googlecode.socofo.core.api.SourceWriter;
 import com.googlecode.socofo.core.exceptions.TranslationException;
 import com.googlecode.socofo.rules.api.CommonAttributes;
 
 /**
  * The basic impl of the SourceWriter.
  * 
  * @author Dirk Strauss
  * @version 1.0
  */
 public class SourceWriterImpl implements SourceWriter {
 	/**
 	 * The string buffer to store content in
 	 */
 	private StringBuffer sb = null;
 	/**
 	 * the line buffer
 	 */
 	private StringBuffer currentLine = null;
 	/**
 	 * The common attributes for NEWLINE, maxLineLength and indentSequence
 	 */
 	private CommonAttributes ca = null;
 	/**
 	 * the NewLine terminator
 	 */
 	private String newline = "\n";
 	/**
 	 * A logger
 	 */
 	private static final transient Logger log = Logger
 			.getLogger(SourceWriterImpl.class.getName());
 	/**
 	 * The line handler.
 	 */
 	@Inject
 	private LineHandler lh = null;
 
 	/**
 	 * Inits the source buffer
 	 */
 	public SourceWriterImpl() {
 		sb = new StringBuffer();
 		currentLine = new StringBuffer();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean addLine(int indents, String s) throws TranslationException {
 		commitLine(false);
 		if (indents < 0) {
 			log.severe("Indents of " + indents + " are impossible!");
 			return false;
 		}
 		addIndents(currentLine, indents);
 		currentLine.append(s);
 		return commitLine(false);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean addToLine(String s) {
 		if (s == null) {
 			return false;
 		}
 		currentLine.append(s);
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getResult() {
 		return sb.toString();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean addToLine(int currentIndent, String s) {
 		if (s == null) {
 			log.warning("No content given!");
 			return false;
 		}
 		final StringBuffer tmpBuffer = new StringBuffer();
 		addIndents(tmpBuffer, currentIndent);
 		tmpBuffer.append(s);
 		final String insertStr = tmpBuffer.toString();
 		tmpBuffer.insert(0, currentLine);
 		final int lineLength = getLineLength(tmpBuffer.toString());
 		boolean rc = true;
 		if (lineLength <= ca.getMaxLinewidth()) {
 			// ok
 			currentLine.append(insertStr);
 		} else {
 			log.warning("Line becomes too long: " + currentLine + insertStr);
 			rc = false;
 		}
 		return rc;
 	}
 
 	/**
 	 * Adds a number of indents to the given string buffer.
 	 * 
 	 * @param s
 	 *            the string buffer to use
 	 * @param count
 	 *            the count of indents to add
 	 */
 	private void addIndents(final StringBuffer s, final int count) {
 		if (count < 0) {
 			log.warning("Count is too low: " + count);
 			return;
 		}
 		if (s == null) {
 			log.warning("No buffer given!");
 			return;
 		}
 		synchronized (s) {
 			s.append(lh.getSequence(count, ca.getIndentSequence()));
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void finish() throws TranslationException {
 		commitLine(false);
 		clearBuffer(currentLine);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void prepare() {
 		clearBuffer(sb);
 		clearBuffer(currentLine);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setCommonAttributes(CommonAttributes c) {
 		if (c == null) {
 			log.warning("No common attributes given!");
 			return;
 		}
 		ca = c;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 */
 	@Override
 	public boolean commitLine(boolean ignoreLineLength)
 			throws TranslationException {
 		if (!ignoreLineLength && currentLine.length() > ca.getMaxLinewidth()) {
 			log.warning("line too long to commit: " + currentLine);
 			if (ca.getStopOnLongline()) {
 				throw new TranslationException("Line too long to commit: "
 						+ currentLine);
 			}
 			return false;
 		}
 		if (currentLine.length() <= 0) {
 			log.finer("nothing to commit: line is empty already");
 			return true;
 		}
 		sb.append(currentLine.toString());
 		log.finest("commiting this content: " + currentLine.toString());
 		sb.append(newline);
 		clearBuffer(currentLine);
 		return true;
 	}
 
 	/**
 	 * Clears the given string buffer
 	 * 
 	 * @param s
 	 *            the buffer to clear
 	 */
 	private void clearBuffer(StringBuffer s) {
 		if (s == null) {
 			log.warning("No buffer given!");
 			return;
 		}
 		if (s.length() > 0) {
 			log.finest("This content will be cleared now: " + s.toString());
 		}
 		s.delete(0, s.length());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getCurrentLine() {
 		return currentLine.toString();
 	}
 
 	public int getLineLength(String line) {
 		if (lh == null) {
 			throw new IllegalStateException("No line handler has been setup!");
 		}
 		int tabSize = ca.getTabSize();
 		int rc = lh.getLineWidth(tabSize, line);
 		return rc;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getCurrentLineLength() {
 		int rc = 0;
 		String currentLineStr = currentLine.toString();
 		rc = getLineLength(currentLineStr);
 		return rc;
 	}
 
	public void setTestLineHandler(LineHandlerImpl lineHandlerImpl) {
		lh = lineHandlerImpl;
	}

 }
