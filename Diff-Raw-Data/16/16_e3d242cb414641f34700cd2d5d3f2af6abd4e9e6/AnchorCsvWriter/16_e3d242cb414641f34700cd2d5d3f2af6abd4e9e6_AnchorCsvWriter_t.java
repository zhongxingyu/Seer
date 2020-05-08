 /**
  * Copyright (c) 2010, Anchor Intelligence. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * - Redistributions of source code must retain the above copyright
  *   notice, this list of conditions and the following disclaimer.
  *
  * - Redistributions in binary form must reproduce the above copyright
  *   notice, this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the
  *   distribution.
  *
  * - Neither the name of Anchor Intelligence nor the names of its
  *   contributors may be used to endorse or promote products derived
  *   from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
  */
 package com.fraudwall.util.io;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 
 import org.apache.commons.lang.ArrayUtils;
 
 import com.fraudwall.util.AnchorFatalError;
 import com.fraudwall.util.ArgCheck;
 
 /**
  * Simple class for writing to CSV files based on the CsvWriter implementation,
  * but without the bug that causes IOExceptions to be silently ignored. There is
  * a code style test to ensure that this class is used instead of CsvWriter.
  * <p>
  * All constructors use the {@link IOUtils#UTF8} character set to convert from
  * characters to bytes. All constructors of this class guarantee that the output
  * is buffered for better performance.
  * <p>
  * Lines in the output file are separated by the value of the "line.separator"
  * property.
  * <p>
  * The individual fields on each output line are separated by a delimiter. The
  * default delimiter is the "," (comma) character, but some of the constructors
  * allow a different delimiter to be supplied, and the {@link #setDelimiter(char)}
  * method can be called even after the writer has been constructed.
  *
  * @author Allan Heydon
  */
 public class AnchorCsvWriter {
 	private static final char DEFAULT_DELIMITER = ',';
 	private static final char TEXT_QUALIFIER = '"';
 	private static final String TEXT_QUALIFIER_STR = String.valueOf(TEXT_QUALIFIER);
 
 	private /*final*/ Writer wr; // set to 'null' by close()
 	private final String lineSeparator;
 
 	private char delimiter;
 	private boolean closed = false;
 	private boolean firstColumn = true;
 
 	/**
 	 * Constructs a CSV writer that writes to a file with the given {@code fileName} using the
 	 * {@link IOUtils#UTF8 UTF8} character set and comma for the field delimiter.
 	 */
 	public AnchorCsvWriter(String fileName) {
 		this(new File(fileName));
 	}
 
 	/**
 	 * Constructs a CSV writer that writes to the given {@code outFile} using the
 	 * {@link IOUtils#UTF8 UTF8} character set and comma for the field delimiter.
 	 */
 	public AnchorCsvWriter(File outFile) {
 		this(outFile, DEFAULT_DELIMITER);
 	}
 
 	/**
 	 * Constructs a CSV writer that writes to the given {@code outFile} using the
 	 * {@link IOUtils#UTF8 UTF8} character set and {@code delimiter} for the field delimiter.
 	 */
 	public AnchorCsvWriter(File outFile, char delimeter) {
 		this(createBufferedWriter(outFile), delimeter);
 	}
 
 	private static Writer createBufferedWriter(File outFile) {
 		try {
 			return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), IOUtils.UTF8));
 		} catch (FileNotFoundException ex) {
 			throw new AnchorFatalError("File could not be opened for writing", ex);
 		}
 	}
 
 	/**
 	 * Constructs a CSV writer that writes to standard output using the {@link IOUtils#UTF8 UTF8} character
 	 * set and {@code delimiter} for the field delimiter.
 	 */
 	public AnchorCsvWriter(char delimiter) {
 		// System.out is already buffered, so there's no need to wrap it in a BufferedWriter.
 		this(new OutputStreamWriter(System.out, IOUtils.UTF8), delimiter);
 	}
 
 	/**
 	 * Constructs a CSV writer that writes to the Writer {@code wr} using the
 	 * {@link IOUtils#UTF8 UTF8} character set and {@code delimiter} for the field delimiter.
 	 */
 	/*test*/ AnchorCsvWriter(Writer wr, char delimiter) {
 		ArgCheck.isNotNull(wr, "wr");
 		this.wr = wr;
 		setDelimiter(delimiter);
 		this.lineSeparator = System.getProperty("line.separator");
 	}
 
 	/**
 	 * Sets the character to use as the column delimiter. This can be used to override the default (namely,
 	 * the comma character) or an explicit delimiter passed to the constructor.
 	 */
 	public void setDelimiter(char delimiter) {
 		this.delimiter = delimiter;
 	}
 
 	private static final char NEWLINE = '\n';
 	private static final char CARRIAGE_RETURN = '\r';
 	private static final char COMMENT_CHAR = '#';
 
 	/**
 	 * Appends the single field {@code content} to the current line without
 	 * trimming leading or trailing spaces.
 	 */
 	public void write(String content) throws IOException {
 		checkClosed();
 
 		if (content == null) {
 			content = "";
 		}
 
 		if (!firstColumn) {
 			wr.write(delimiter);
 		}
 
 		boolean textQualify =
 			content.indexOf(delimiter) > -1 || content.indexOf(TEXT_QUALIFIER) > -1
 			|| content.indexOf(NEWLINE) > -1 || content.indexOf(CARRIAGE_RETURN) > -1
 			// check for empty first column, which if on its own line must be qualified or the line will be skipped
 			|| (firstColumn && content.isEmpty());
 
 		if (!textQualify && !content.isEmpty()) {
 			char firstLetter = content.charAt(0);
 			if (Character.isWhitespace(firstLetter) || (firstColumn && firstLetter == COMMENT_CHAR)) {
 				textQualify = true;
 			}
 
 			if (!textQualify) {
 				char lastLetter = content.charAt(content.length() - 1);
 				if (Character.isWhitespace(lastLetter)) {
 					textQualify = true;
 				}
 			}
 		}
 
 		if (textQualify) {
 			wr.write(TEXT_QUALIFIER);
 			content = content.replace(TEXT_QUALIFIER_STR, TEXT_QUALIFIER_STR + TEXT_QUALIFIER_STR);
 		}
 		wr.write(content);
 		if (textQualify) {
 			wr.write(TEXT_QUALIFIER);
 		}
 
 		firstColumn = false;
 	}
 
 	/**
 	 * Appends the given {@code values} as separate fields to the current line without
 	 * trimming leading or trailing spaces, and ends the current line.
 	 */
 	public void writeRecord(String... values) throws IOException {
 		if (! ArrayUtils.isEmpty(values)) {
 			for (String value: values) {
 				write(value);
 			}
 			endRecord();
 		}
 	}
 
 	/**
 	 * Ends the current record by sending the record delimiter.
 	 *
 	 * @exception IOException
 	 *                Thrown if an error occurs while writing data to the
 	 *                destination stream.
 	 */
 	public void endRecord() throws IOException {
 		checkClosed();
 		wr.write(lineSeparator);
 		firstColumn = true;
 	}
 
 	/** Throws IOException if this writer has already been closed. */
 	private void checkClosed() throws IOException {
 		if (closed) {
 			throw new IOException("This instance of the AnchorCsvWriter class has already been closed.");
 		}
 	}
 
 	/**
 	 * Clears all buffers for the current writer and causes any buffered data to
 	 * be written to the underlying device.
 	 */
 	public void flush() {
 		try {
 			wr.flush();
 		} catch (IOException ex) {
 			throw new AnchorFatalError("Unexpected I/O exception", ex);
 		}
 	}
 
 	/**
 	 * Closes and releases all related resources.
 	 */
 	public void close() {
 		if (!closed) {
 			try {
 				wr.close();
 			} catch (IOException ex) {
 				throw new AnchorFatalError("Unexpected I/O exception", ex);
 			}
 			wr = null;
 			closed = true;
 		}
 	}
 
 	@Override
 	protected void finalize() {
 		close();
 	}
 }
