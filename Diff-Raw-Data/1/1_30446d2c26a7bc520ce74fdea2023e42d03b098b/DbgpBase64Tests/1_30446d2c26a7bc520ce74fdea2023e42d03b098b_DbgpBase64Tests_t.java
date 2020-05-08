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
 package org.eclipse.dltk.debug.dbgp.tests;
 
 import org.eclipse.dltk.dbgp.internal.utils.Base64Helper;
 
 import junit.framework.TestCase;
 
 public class DbgpBase64Tests extends TestCase {
 
 	public void testNullAndEmpty() {
 		assertEquals("", Base64Helper.encodeString(null));
 		assertEquals("", Base64Helper.encodeString(""));
 		assertEquals("", Base64Helper.decodeString(null));
 		assertEquals("", Base64Helper.decodeString(""));
 		assertEquals("", Base64Helper.decodeString("\r\n"));
 	}
 
 	public void testEndode() {
 		assertEquals("MTIz", Base64Helper.encodeString("123"));
 		assertEquals("MTIzNDU2Nzg5", Base64Helper.encodeString("123456789"));
 	}
 
 	public void testDecode() {
 		assertEquals("123", Base64Helper.decodeString("MTIz"));
 		assertEquals("123456789", Base64Helper.decodeString("MTIzNDU2Nzg5"));
 	}
 
 	public void testDecodeChunked() {
 		assertEquals("123", Base64Helper.decodeString("MTIz"));
 		assertEquals("123456789", Base64Helper.decodeString("MTIz" + "\n"
 				+ "NDU2" + "\n" + "Nzg5"));
 	}
 
 	public void testLongString() {
 		char[] c = new char[256];
 		for (int i = 0; i < c.length; ++i) {
 			c[i] = (char) i;
 		}
 		final String input = new String(c);
 		final String encoded = Base64Helper.encodeString(input);
 		final int chunkSize = 64;
 		final int chunkCount = (encoded.length() + chunkSize - 1) / chunkSize;
 		final StringBuffer chunked = new StringBuffer();
 		for (int i = 0; i < chunkCount; ++i) {
 			final int beginIndex = i * chunkSize;
 			final int endIndex = Math.min(encoded.length(), beginIndex
 					+ chunkSize);
 			chunked.append(encoded.substring(beginIndex, endIndex));
			chunked.append("\r\n");
 		}
 		final String output = Base64Helper.decodeString(chunked.toString());
 		assertEquals(input, output);
 	}
 }
