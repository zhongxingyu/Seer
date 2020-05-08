 /*******************************************************************************
  * Copyright (c) 2013 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Edgar Mueller
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.common.model.util;
 
 import java.io.IOException;
 import java.io.StringWriter;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * A {@link StringWriter} implementation that ignores any whitespace.
  * 
  * @author emueller
  * 
  */
 public class IgnoreWhitespaceStringWriter extends StringWriter {
 
 	public IgnoreWhitespaceStringWriter(int initialSize) {
 		super(initialSize);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see java.io.Writer#write(char[])
 	 */
 	@Override
 	public void write(char[] cbuf) throws IOException {
 		super.write(removeWhitespaceChars(cbuf));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see java.io.StringWriter#write(char[], int, int)
 	 */
 	@Override
 	public void write(char[] cbuf, int off, int len) {
		super.write(removeWhitespaceChars(cbuf), off, len);
 	}
 
 	private char[] removeWhitespaceChars(char[] cbuf) {
 		return new String(cbuf).replaceAll("\\s", StringUtils.EMPTY).toCharArray();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see java.io.StringWriter#write(int)
 	 */
 	@Override
 	public void write(int c) {
 		if (Character.isWhitespace(c)) {
 			return;
 		}
 		super.write(c);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see java.io.StringWriter#write(java.lang.String)
 	 */
 	@Override
 	public void write(String str) {
 		super.write(str.replaceAll("\\s", StringUtils.EMPTY));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see java.io.StringWriter#write(java.lang.String, int, int)
 	 */
 	@Override
 	public void write(String str, int off, int len) {
 		super.write(str.replaceAll("\\s", StringUtils.EMPTY), off, len);
 	}
 }
