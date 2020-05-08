 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.compiler.util;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IDLTKLanguageToolkitExtension;
 import org.eclipse.dltk.core.RuntimePerformanceMonitor;
 import org.eclipse.dltk.core.RuntimePerformanceMonitor.PerformanceNode;
 
 public class Util {
 
 	/**
 	 * Some input streams return available() as zero, so we need this value.
 	 */
 	private static final int DEFAULT_READING_SIZE = 8192;
 	public final static String UTF_8 = "UTF-8"; //$NON-NLS-1$			
 	public static String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
 	public static final String EMPTY_STRING = ""; //$NON-NLS-1$
 
 	/**
 	 * Returns the given input stream's contents as a byte array. If a length is
 	 * specified (ie. if length != -1), only length bytes are returned.
 	 * Otherwise all bytes in the stream are returned. Note this doesn't close
 	 * the stream.
 	 * 
 	 * @throws IOException
 	 *             if a problem occured reading the stream.
 	 */
 	public static byte[] getInputStreamAsByteArray(InputStream stream,
 			int length) throws IOException {
 		byte[] contents;
 		if (length == -1) {
 			contents = new byte[0];
 			int contentsLength = 0;
 			int amountRead = -1;
 			do {
 				int amountRequested = Math.max(stream.available(),
 						DEFAULT_READING_SIZE); // read at least 8K
 
 				// resize contents if needed
 				if (contentsLength + amountRequested > contents.length) {
 					System.arraycopy(contents, 0,
 							contents = new byte[contentsLength
 									+ amountRequested], 0, contentsLength);
 				}
 
 				// read as many bytes as possible
 				amountRead = stream.read(contents, contentsLength,
 						amountRequested);
 
 				if (amountRead > 0) {
 					// remember length of contents
 					contentsLength += amountRead;
 				}
 			} while (amountRead != -1);
 
 			// resize contents if necessary
 			if (contentsLength < contents.length) {
 				System.arraycopy(contents, 0,
 						contents = new byte[contentsLength], 0, contentsLength);
 			}
 		} else {
 			contents = new byte[length];
 			int len = 0;
 			int readSize = 0;
 			while ((readSize != -1) && (len != length)) {
 				// See PR 1FMS89U
 				// We record first the read size. In this case len is the actual
 				// read size.
 				len += readSize;
 				readSize = stream.read(contents, len, length - len);
 			}
 		}
 
 		return contents;
 	}
 
 	/**
 	 * /** Returns the contents of the given file as a byte array.
 	 * 
 	 * @throws IOException
 	 *             if a problem occured reading the file.
 	 */
 	public static byte[] getFileByteContent(File file) throws IOException {
 		InputStream stream = null;
 		PerformanceNode p = RuntimePerformanceMonitor.begin();
 		try {
 			stream = new FileInputStream(file);
 			byte[] data = getInputStreamAsByteArray(stream, (int) file.length());
 			p.done("#", RuntimePerformanceMonitor.IOREAD, data.length);
 			return data;
 		} finally {
 			if (stream != null) {
 				try {
 					stream.close();
 				} catch (IOException e) {
 					// ignore
 				}
 			}
 		}
 	}
 
 	/*
 	 * a character array. If a length is specified (ie. if length != -1), this
 	 * represents the number of bytes in the stream. Note this doesn't close the
 	 * stream. @throws IOException if a problem occured reading the stream.
 	 */
 	public static char[] getInputStreamAsCharArray(InputStream stream,
 			int length, String encoding) throws IOException {
 		InputStreamReader reader = null;
 		try {
 			try {
 				reader = encoding == null ? new InputStreamReader(
 						toBufferedInputStream(stream)) : new InputStreamReader(
 						stream, encoding);
 			} catch (UnsupportedEncodingException e) {
 				// encoding is not supported
 				reader = new InputStreamReader(toBufferedInputStream(stream));
 			}
 			char[] contents;
 			int totalRead = 0;
 			if (length == -1) {
 				contents = CharOperation.NO_CHAR;
 			} else {
 				// length is a good guess when the encoding produces less or the
 				// same amount of characters than the file length
 				contents = new char[length]; // best guess
 			}
 
 			while (true) {
 				int amountRequested;
 				if (totalRead < length) {
 					// until known length is met, reuse same array sized eagerly
 					amountRequested = length - totalRead;
 				} else {
 					// reading beyond known length
 					int current = reader.read();
 					if (current < 0)
 						break;
 
 					amountRequested = Math.max(stream.available(),
 							DEFAULT_READING_SIZE); // read at least 8K
 
 					// resize contents if needed
 					if (totalRead + 1 + amountRequested > contents.length)
 						System.arraycopy(contents, 0,
 								contents = new char[totalRead + 1
 										+ amountRequested], 0, totalRead);
 
 					// add current character
 					contents[totalRead++] = (char) current; // coming from
 					// totalRead==length
 				}
 				// read as many chars as possible
 				int amountRead = reader.read(contents, totalRead,
 						amountRequested);
 				if (amountRead < 0)
 					break;
 				totalRead += amountRead;
 			}
 
 			// Do not keep first character for UTF-8 BOM encoding
 			int start = 0;
 			if (totalRead > 0 && UTF_8.equals(encoding)) {
 				if (contents[0] == 0xFEFF) { // if BOM char then skip
 					totalRead--;
 					start = 1;
 				}
 			}
 
 			// resize contents if necessary
 			if (totalRead < contents.length)
 				System.arraycopy(contents, start,
 						contents = new char[totalRead], 0, totalRead);
 
 			return contents;
 		} finally {
 			if (reader != null) {
 				reader.close();
 			}
 		}
 	}
 
 	private static InputStream toBufferedInputStream(InputStream stream) {
 		if (stream instanceof BufferedInputStream) {
 			return stream;
 		} else {
 			return new BufferedInputStream(stream, DEFAULT_READING_SIZE);
 		}
 	}
 
 	private final static char[] SUFFIX_zip = new char[] { '.', 'z', 'i', 'p' };
 	private final static char[] SUFFIX_ZIP = new char[] { '.', 'Z', 'I', 'P' };
 
 	/**
 	 * Returns <code>true</code> if str.toLowerCase().endsWith(".zip")
 	 * implementation is not creating extra strings.
 	 */
 	public final static boolean isArchiveFileName(String name) {
 		if (name == null) {
 			return false;
 		}
 		final int nameLength = name.length();
 		final int suffixLength = SUFFIX_ZIP.length;
 		if (nameLength < suffixLength)
 			return false;
 		for (int i = 0; i < suffixLength; i++) {
 			final char c = name.charAt(nameLength - i - 1);
 			final int suffixIndex = suffixLength - i - 1;
 			if (c != SUFFIX_zip[suffixIndex] && c != SUFFIX_ZIP[suffixIndex]) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public final static boolean isArchiveFileName(IDLTKLanguageToolkit toolkit,
 			String name) {
 		if (name == null) {
 			return false;
 		}
		if (toolkit instanceof IDLTKLanguageToolkitExtension) {
			IDLTKLanguageToolkitExtension ext = (IDLTKLanguageToolkitExtension) toolkit;
			if (ext.isArchiveFileName(name)) {
				return true;
			}
		}
 		final int nameLength = name.length();
 		final int suffixLength = SUFFIX_ZIP.length;
 		if (nameLength < suffixLength)
 			return false;
 		for (int i = 0; i < suffixLength; i++) {
 			final char c = name.charAt(nameLength - i - 1);
 			final int suffixIndex = suffixLength - i - 1;
 			if (c != SUFFIX_zip[suffixIndex] && c != SUFFIX_ZIP[suffixIndex]) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/*
 	 * TODO (philippe) should consider promoting it to CharOperation Returns
 	 * whether the given resource path matches one of the inclusion/exclusion
 	 * patterns. NOTE: should not be asked directly using pkg root pathes
 	 */
 	public final static boolean isExcluded(char[] path,
 			char[][] inclusionPatterns, char[][] exclusionPatterns,
 			boolean isFolderPath) {
 		if (inclusionPatterns == null && exclusionPatterns == null)
 			return false;
 
 		inclusionCheck: if (inclusionPatterns != null) {
 			for (int i = 0, length = inclusionPatterns.length; i < length; i++) {
 				char[] pattern = inclusionPatterns[i];
 				char[] folderPattern = pattern;
 				if (isFolderPath) {
 					int lastSlash = CharOperation.lastIndexOf('/', pattern);
 					if (lastSlash != -1 && lastSlash != pattern.length - 1) { // trailing
 						// slash
 						// ->
 						// adds
 						// ' **'
 						// for
 						// free
 						// (see
 						// http://ant.apache.org/manual/dirtasks.html)
 						int star = CharOperation.indexOf('*', pattern,
 								lastSlash);
 						if ((star == -1 || star >= pattern.length - 1 || pattern[star + 1] != '*')) {
 							folderPattern = CharOperation.subarray(pattern, 0,
 									lastSlash);
 						}
 					}
 				}
 				if (CharOperation.pathMatch(folderPattern, path, true, '/')) {
 					break inclusionCheck;
 				}
 			}
 			return true; // never included
 		}
 		if (isFolderPath) {
 			path = CharOperation.concat(path, new char[] { '*' }, '/');
 		}
 		if (exclusionPatterns != null) {
 			for (int i = 0, length = exclusionPatterns.length; i < length; i++) {
 				if (CharOperation.pathMatch(exclusionPatterns[i], path, true,
 						'/')) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the contents of the given file as a char array. When encoding is
 	 * null, then the platform default one is used
 	 * 
 	 * @throws IOException
 	 *             if a problem occured reading the file.
 	 */
 	public static char[] getFileCharContent(File file, String encoding)
 			throws IOException {
 		InputStream stream = null;
 		PerformanceNode p = RuntimePerformanceMonitor.begin();
 		try {
 			stream = new FileInputStream(file);
 			char[] data = getInputStreamAsCharArray(stream,
 					(int) file.length(), encoding);
 			p.done("#", RuntimePerformanceMonitor.IOREAD, data.length);
 			return data;
 		} finally {
 			if (stream != null) {
 				try {
 					stream.close();
 				} catch (IOException e) {
 					// ignore
 				}
 			}
 		}
 	}
 
 	public static void copy(File file, InputStream input) throws IOException {
 		PerformanceNode p = RuntimePerformanceMonitor.begin();
 		OutputStream fos = new BufferedOutputStream(new FileOutputStream(file),
 				4096);
 		copy(input, fos);
 		fos.close();
 		p.done("#", RuntimePerformanceMonitor.IOWRITE, file.length());
 	}
 
 	public static void copy(InputStream input, OutputStream fos)
 			throws IOException {
 		byte[] buf = new byte[8192];
 		int n = 0;
 		while ((n = input.read(buf)) >= 0) {
 			fos.write(buf, 0, n);
 		}
 	}
 }
