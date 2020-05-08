 package com.gooddata.util;
 
 /**
  Copyright 2005 Bytecode Pty Ltd.
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * A very simple CSV reader released under a commercial-friendly license.
  * 
  * @author Glen Smith
  * 
  */
 public class CSVReader implements Closeable {
 	
 	private static char DEFAULT_SEPARATOR = ',';
 	private static char DEFAULT_QUOTE_CHARACTER = '"';
 	private static char DEFAULT_ESCAPE_CHARACTER = '"';
 	
 	private static int CHUNK_SIZE = 4096;
 
     private Reader r;
     
     // configuration
     private char separator;
     private char quote;
     private char escape;
     private boolean hasCommentSupport = false;
     private char commentChar;
 
     // status variables
     private List<String> openRecord = new ArrayList<String>();
     private StringBuffer openField  = new StringBuffer();
     private char lastChar = 0; 
     private boolean quotedField = false;
     private boolean commentedLine = false;
     private LinkedList<String[]> recordsQueue = new LinkedList<String[]>();
     private boolean eof = false;
     
     private int row = 0;
     private int col = 0;
 
     /**
      * Constructs CSVReader using a comma for the separator.
      * 
      * @param reader
      *            the reader to an underlying CSV source.
      */
     public CSVReader(Reader r) {
         this(r, DEFAULT_SEPARATOR, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
     }
 
     /**
      * Constructs CSVReader with supplied separator.
      * 
      * @param reader
      *            the reader to an underlying CSV source.
      * @param separator
      *            the delimiter to use for separating entries.
      */
     public CSVReader(Reader r, char separator) {
         this(r, separator, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
     }
 
     /**
      * Constructs CSVReader with supplied separator and quote char.
      * 
      * @param reader
      *            the reader to an underlying CSV source.
      * @param separator
      *            the delimiter to use for separating entries
      * @param quotechar
      *            the character to use for quoted elements
      */
     public CSVReader(Reader r, char separator, char quotechar) {
         this(r, separator, quotechar, DEFAULT_ESCAPE_CHARACTER);
     }
 
    /**
      * Constructs CSVReader with supplied separator and quote char.
      *
      * @param reader
      *            the reader to an underlying CSV source.
      * @param separator
      *            the delimiter to use for separating entries
      * @param quotechar
      *            the character to use for quoted elements
      * @param escape
      *            the character to use for escaping a separator or quote
      */
 
     public CSVReader(Reader r, char separator, char quotechar, char escape) {
         this.r = r;
         this.separator = separator;
         this.quote = quotechar;
         this.escape = escape;
 	}
 
 	/**
      * Reads the entire file into a List with each element being a String[] of
      * tokens.
      * 
      * @return a List of String[], with each String[] representing a line of the
      *         file.
      * 
      * @throws IOException
      *             if bad things happen during the read
      */
     public List<String[]> readAll() throws IOException {
 
         List<String[]> allElements = new ArrayList<String[]>();
         String line[];
         while ((line = readNext()) != null) {
             allElements.add(line);
         }
         return allElements;
     }
 
     /**
      * Reads the next line from the buffer and converts to a string array.
      * 
      * @return a string array with each comma-separated element as a separate
      *         entry.
      * 
      * @throws IOException
      *             if bad things happen during the read
      */
     public String[] readNext() throws IOException {
     	while (recordsQueue.isEmpty() && !eof) {
 	    	char[] data = new char[CHUNK_SIZE];
 	    	int size = r.read(data);
 	    	if (size == -1) {
 	    		break;
 	    	}
 	    	
 	    	for (int i = 0; i < size; i++) {
 	    		col++;
 	    		final char c = data[i];
 	    		if (c == escape || c == quote) {
	    			i = handleEscapeOrQuote(data, size, i);
 	    		} else if (c == separator) {
 	    			handleSeparator(c);
 	    		} else if (c == '\n' || c == '\r') {
 	    			// handle CRLF sequence
 	                if (c == '\n' && !quotedField && (lastChar == '\r')) {
 	                	break;
 	                }
 	                handleEndOfLine(c);
 	    		} else if (hasCommentSupport && (c == commentChar)) {
 	    			handleComment(c);
 	    		} else {
 	    			if (commentedLine) 
 	    				break;
 	    			addCharacter(c);
 	    		}
 	    		lastChar = c;
 	    	}	
     	}
     	if (recordsQueue.isEmpty()) {
     		if (openRecord.isEmpty()) {
     			return null;
     		} else {
     			String[] result = openRecord.toArray(new String[]{});
     			openRecord.clear();
     			return result;
     		}
     	}
     	return recordsQueue.removeFirst();
     }
     
     private void handleComment(final char c) {
     	if (commentedLine)
 			return;
     	if (openRecord.isEmpty() && (openField.length() == 0) && !quotedField) {
     		commentedLine = true;
     	} else {
     		addCharacter(c);
     	}
     }
     
     private void handleEndOfLine(final char c) {
     	if (commentedLine) {
     		commentedLine = false;
     	} else if (quotedField) {
     		addCharacter(c);
     	} else {
     		addField();
     		addRecord();
     	}
     	row++;
     	col = 0;
     }
     
     private void addRecord() {
     	recordsQueue.add(openRecord.toArray(new String[]{}));
     	openRecord.clear();
     	openField.delete(0, openField.length());
     	quotedField = false;
     }
     
     private void handleSeparator(final char c) {
     	if (commentedLine)
     		return;
     	if (quotedField) {
     		this.addCharacter(c);
     	} else {
     		this.addField();
     	}
     }
     	
     private int handleEscapeOrQuote(final char[] data, final int size, int i) {
     	if (commentedLine)
 			return i;
     	final char c = data[i];
 		final char nextChar;
 		final boolean hasNextChar;
 		if (i + 1 < size) {
 			nextChar = data[i + 1];
 			hasNextChar = true;
 		} else {
 			nextChar = 0;
 			hasNextChar = false;
 		}
 		boolean isEscape = false;
 		if (c == escape && hasNextChar) {
 			if (isEscapableCharacter(nextChar)) {
 				addCharacter(nextChar);
 				i++;
 				isEscape = true;
 			}
 		}
 		if (!isEscape && (c == quote)) {
 			if (quotedField) { // closing quote should be followed by separator
 				if (hasNextChar && nextChar != '\r' && nextChar != '\n' && nextChar != separator) {
 					throw new IllegalStateException(
 							"separator expected after a closing quote; found " + nextChar + getPositionString());
 				}
 				quotedField = false;
 			} else if (openField.length() == 0) {
 				quotedField = true;
 			}
 		}
 		return i;
 	}
 
     private void addField() {
     	openRecord.add(openField.toString());
     	openField.delete(0, openField.length());
     	quotedField = false;
     }
     
     private void addCharacter(final char c) {
     	openField.append(c);
     }
     
     private boolean isEscapableCharacter(final char c) {
     	return (c == escape || c == quote);
     }
     
     /**
      * Closes the underlying reader.
      * 
      * @throws IOException if the close fails
      */
     public void close() throws IOException{
     	r.close();
     }
     
     private String getPositionString() {
     	return " [" + row + "," + col + "]";
     }
     
     public void setCommentChar(char c) {
     	hasCommentSupport = true;
     	commentChar = c;
     }
 }
