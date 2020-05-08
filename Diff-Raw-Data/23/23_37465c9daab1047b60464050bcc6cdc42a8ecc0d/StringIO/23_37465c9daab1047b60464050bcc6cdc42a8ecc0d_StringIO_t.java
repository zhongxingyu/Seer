 /*******************************************************************************
  * Copyright 2011 Lance Feagan
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package net.vectorcomputing.base.string;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 
 import net.vectorcomputing.base.Assert;
 
 /**
  * Utility class providing static methods to read strings from input streams and
  * convert strings to an input stream.
  */
 public final class StringIO {
 
 	private static final int DEFAULT_BUFFER_SIZE = 1 << 14;
 	
 	private static final String DEFAULT_FILE_ENCODING = System.getProperty("file.encoding"); //$NON-NLS-1$
 
 	/**
 	 * Reads content from an input stream until there are no more bytes to be
 	 * read. Returns the content read as a string.
 	 * 
 	 * @param input
 	 *            the input stream to read from
 	 * @return the data read from the input stream as a string
 	 * @throws IOException
 	 */
 	public static final String read(final InputStream input) throws IOException {
 		Assert.isNotNull(input, "input"); //$NON-NLS-1$
 		return read(input, DEFAULT_FILE_ENCODING, DEFAULT_BUFFER_SIZE);
 	}
 
 	/**
 	 * Reads content from an input stream until there are no more bytes to be
 	 * read and returns the content read as a string
 	 * 
 	 * @param input
 	 *            the input stream to read from
 	 * @param fileEncoding
 	 *            the file encoding to use when converting the bytes in the
 	 *            input stream to characters
 	 * @return the data read from the input stream as a string
 	 * @throws IOException
 	 */
 	public static final String read(final InputStream input, final String fileEncoding, final int bufferSize) throws IOException {
 		Assert.isNotNull(input, "input"); //$NON-NLS-1$
 		assertIsBufferSizeValid(bufferSize);

		final StringBuffer contents = new StringBuffer();
 		final char[] buffer = new char[bufferSize];
 		final InputStreamReader isr = new InputStreamReader(input, fileEncoding);
 		final BufferedReader br = new BufferedReader(isr, bufferSize);
 
 		try {			
 			int bytesRead;
 			while ((bytesRead = br.read(buffer)) != -1) {
				contents.append(buffer, 0, bytesRead);
 			}
			return contents.toString();
 		} finally {
 			br.close();
 			isr.close();
 			input.close();
 		}
 	}
 
 	private static void assertIsBufferSizeValid(final int bufferSize) {
 		if (bufferSize <= 0) {
 			throw new AssertionError("buffer size > 0"); //$NON-NLS-1$
 		}
 	}
 	
 	public static final String read(final File file) throws IOException {
 		Assert.isNotNull(file, "file"); //$NON-NLS-1$
 		return read(file, DEFAULT_BUFFER_SIZE);
 	}
 	
 	public static final String read(final File file, final int bufferSize) throws IOException {
 		Assert.isNotNull(file, "file"); //$NON-NLS-1$
 		assertIsBufferSizeValid(bufferSize);
 		
 		if (file.length() != 0) {
 			final StringBuffer contents = new StringBuffer();
 			final char[] buffer = new char[bufferSize];
 			final FileReader reader = new FileReader(file);
 			int charsRead;
 			while ((charsRead = reader.read(buffer)) > 0) {
 				contents.append(buffer, 0, charsRead);
 			}
 			reader.close();
 			return contents.toString();
 		} else {
 			return Strings.EMPTY_STRING;
 		}	
 	}
 	
 
 	/**
 	 * Converts a string to a byte representation using the default file
 	 * encoding and returns an input stream that will read this encoded form.
 	 * 
 	 * @param string
 	 *            the string to be encoded
 	 */
 	public static final void write(final String string, final OutputStream outputStream, final boolean closeStream) throws IOException {
 		if (DEFAULT_FILE_ENCODING != null) {
 			write(string, outputStream, closeStream, DEFAULT_FILE_ENCODING);
 		} else {
 			throw new RuntimeException(StringMessages.getString("StringInputStreams_NoDefaultFileEncoding")); //$NON-NLS-1$
 		}
 	}
 	
 	/**
 	 * Converts a string to a byte representation using the specified file
 	 * encoding and returns an input stream that will read this encoded form.
 	 * 
 	 * @param str
 	 *            the string to encoding
 	 * @param fileEncoding
 	 *            the encoding to use when converting the string to its byte
 	 *            representation
 	 */
 	public static final void write(final String str, final OutputStream outputStream, final boolean closeStream, final String fileEncoding) throws IOException {
 		Assert.isNotNull(str, "str"); //$NON-NLS-1$
 		Assert.isNotNull(outputStream, "outputStream"); //$NON-NLS-1$
 		
 		final ByteBuffer encodedString = Charset.forName(fileEncoding).encode(str);
 		
 		try {
 			outputStream.write(encodedString.array());
 		} finally {
 			if (closeStream) {
 				try {
 					outputStream.close();
 				} catch (IOException e) { // $codepro.audit.disable emptyCatchClause
 					// do nothing
 				}
 			}
 		}
 	}
 	
 }
