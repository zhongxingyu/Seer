 /*
  * Java CSV is a stream based library for reading and writing
  * CSV and other delimited data.
  *   
  * Copyright (C) Bruce Dunwiddie bruce@csvreader.com
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
  */
 package com.csvreader;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.nio.charset.Charset;
 import java.util.Hashtable;
 
 /**
  * A stream based parser for parsing delimited text data from a file or a
  * stream.
  */
 public class CsvReader {
     private static final String EMPTY = "";
 
     private static final char LF = '\n';
 
     private static final char CR = '\r';
 
     private static final char QUOTE = '"';
 
     private static final char COMMA = ',';
 
     private static final char SPACE = ' ';
 
     private static final char TAB = '\t';
 
     private static final char POUND = '#';
 
     private static final char BACKSLASH = '\\';
 
     private static final char NULL_CHAR = '\0';
 
     private static final char BACKSPACE = '\b';
 
     private static final char FORM_FEED = '\f';
 
     private static final char ESCAPE = (char) 27; // ASCII/ANSI escape
 
     private static final char VERTICAL_TAB = (char) 11;
 
     private static final char ALERT = (char) 7;
 
     private static final int INITIAL_COLUMN_BUFFER_SIZE = 100;
 
     private static final int MAX_BUFFER_SIZE = 1024;
 
     private static final int INITIAL_MAX_COLUMN_COUNT = 10;
 
     private Reader inputStream = null;
 
     private String fileName = null;
 
     // these all basically correspond to public properties
     // used during parsing
 
     private Charset charset = null;
 
     private char textQualifier = QUOTE;
 
     private boolean trimWhitespace = true;
 
     private boolean useTextQualifier = true;
 
     private char delimiter = COMMA;
 
     private char recordDelimiter = NULL_CHAR;
 
     private boolean useCustomRecordDelimiter = false;
 
     private char comment = POUND;
 
     private boolean useComments = false;
 
     private int escapeMode = ESCAPE_MODE_DOUBLED;
 
     private boolean safetySwitch = true;
 
     // create holders for the processed column data from the
     // first record in the file in case the first record is being
     // treated as column headers
 
     private String[] headers = new String[0];
 
     private int headersCount = 0;
 
     private Hashtable headerIndexByName = new Hashtable();
 
     private String[] values = null;
 
     // create a buffer to hold processed data, a variable
     // that keeps track of how much space is currently
     // reserved for the buffer, and a counter
     // that keeps track of how much data is in the buffer
 
     private char[] columnBuffer = new char[INITIAL_COLUMN_BUFFER_SIZE];
 
     private int columnBufferSize = INITIAL_COLUMN_BUFFER_SIZE;
 
     private int usedColumnLength = 0;
 
     private int columnStart = 0;
 
     // this will be our working buffer to hold data chunks
     // read in from the data file
 
     private char[] dataBuffer = new char[MAX_BUFFER_SIZE];
 
     private int bufferPosition = 0;
 
     private int bufferCount = 0;
 
     private int maxColumnCount = INITIAL_MAX_COLUMN_COUNT;
 
     private int[] columnStarts = new int[INITIAL_MAX_COLUMN_COUNT];
 
     private int[] columnLengths = new int[INITIAL_MAX_COLUMN_COUNT];
 
     private boolean[] columnQualified = new boolean[INITIAL_MAX_COLUMN_COUNT];
 
     private int columnsCount = 0;
 
     private long currentRecord = 0;
 
     // these are all more or less global loop variables
     // to keep from needing to pass them all into various
     // methods during parsing
 
     private boolean startedColumn = false;
 
     private boolean startedWithQualifier = false;
 
     private boolean hasMoreData = true;
 
     private char lastLetter = '\0';
 
     private boolean hasReadNextLine = false;
 
     private boolean readingHeaders = false;
 
     private boolean skippingRecord = false;
 
     private boolean initialized = false;
 
     private boolean closed = false;
 
     /**
      * Double up the text qualifier to represent an occurance of the text
      * qualifier.
      */
     public static final int ESCAPE_MODE_DOUBLED = 1;
 
     /**
      * Use a backslash character before the text qualifier to represent an
      * occurance of the text qualifier.
      */
     public static final int ESCAPE_MODE_BACKSLASH = 2;
 
     /**
      * Creates a {@link com.csvreader.CsvReader CsvReader} object using a file
      * as the data source.
      * 
      * @param fileName
      *            The path to the file to use as the data source.
      * @param delimiter
      *            The character to use as the column delimiter.
      * @param charset
      *            The {@link java.nio.charset.Charset Charset} to use while
      *            parsing the data.
      */
     public CsvReader(String fileName, char delimiter, Charset charset) {
         if (fileName == null) {
             throw new IllegalArgumentException(
                     "Parameter fileName can not be null.");
         }
 
         if (charset == null) {
             throw new IllegalArgumentException(
                     "Parameter charset can not be null.");
         }
 
         this.fileName = fileName;
         this.delimiter = delimiter;
         this.charset = charset;
     }
 
     /**
      * Creates a {@link com.csvreader.CsvReader CsvReader} object using a file
      * as the data source.&nbsp;Uses ISO-8859-1 as the
      * {@link java.nio.charset.Charset Charset}.
      * 
      * @param fileName
      *            The path to the file to use as the data source.
      * @param delimiter
      *            The character to use as the column delimiter.
      */
     public CsvReader(String fileName, char delimiter) {
         this(fileName, delimiter, Charset.forName("ISO-8859-1"));
     }
 
     /**
      * Creates a {@link com.csvreader.CsvReader CsvReader} object using a file
      * as the data source.&nbsp;Uses a comma as the column delimiter and
      * ISO-8859-1 as the {@link java.nio.charset.Charset Charset}.
      * 
      * @param fileName
      *            The path to the file to use as the data source.
      */
     public CsvReader(String fileName) {
         this(fileName, COMMA);
     }
 
     /**
      * Constructs a {@link com.csvreader.CsvReader CsvReader} object using a
      * {@link java.io.Reader Reader} object as the data source.
      * 
      * @param inputStream
      *            The stream to use as the data source.
      * @param delimiter
      *            The character to use as the column delimiter.
      */
     public CsvReader(Reader inputStream, char delimiter) {
         if (inputStream == null) {
             throw new IllegalArgumentException(
                     "Parameter inputStream can not be null.");
         }
 
         this.inputStream = inputStream;
         this.delimiter = delimiter;
         initialized = true;
     }
 
     /**
      * Constructs a {@link com.csvreader.CsvReader CsvReader} object using a
      * {@link java.io.Reader Reader} object as the data source.&nbsp;Uses a
      * comma as the column delimiter.
      * 
      * @param inputStream
      *            The stream to use as the data source.
      */
     public CsvReader(Reader inputStream) {
         this(inputStream, COMMA);
     }
 
     /**
      * Constructs a {@link com.csvreader.CsvReader CsvReader} object using an
      * {@link java.io.InputStream InputStream} object as the data source.
      * 
      * @param inputStream
      *            The stream to use as the data source.
      * @param delimiter
      *            The character to use as the column delimiter.
      * @param charset
      *            The {@link java.nio.charset.Charset Charset} to use while
      *            parsing the data.
      */
     public CsvReader(InputStream inputStream, char delimiter, Charset charset) {
         this(new InputStreamReader(inputStream, charset), delimiter);
     }
 
     /**
      * Constructs a {@link com.csvreader.CsvReader CsvReader} object using an
      * {@link java.io.InputStream InputStream} object as the data
      * source.&nbsp;Uses a comma as the column delimiter.
      * 
      * @param inputStream
      *            The stream to use as the data source.
      * @param charset
      *            The {@link java.nio.charset.Charset Charset} to use while
      *            parsing the data.
      */
     public CsvReader(InputStream inputStream, Charset charset) {
         this(new InputStreamReader(inputStream, charset));
     }
 
     /**
      * Gets the count of columns found in this record.
      * 
      * @return The count of columns found in this record.
      */
     public int getColumnCount() {
         return columnsCount;
     }
 
     /**
      * Gets the index of the current record.
      * 
      * @return The index of the current record.
      */
     public long getCurrentRecord() {
         return currentRecord - 1;
     }
 
     /**
      * Gets whether leading and trailing whitespace characters are being trimmed
      * from non-textqualified column data. Default is true.
      * 
      * @return Whether leading and trailing whitespace characters are being
      *         trimmed from non-textqualified column data.
      */
     public boolean getTrimWhitespace() {
         return trimWhitespace;
     }
 
     /**
      * Sets whether leading and trailing whitespace characters should be trimmed
      * from non-textqualified column data or not. Default is true.
      * 
      * @param trimWhitespace
      *            Whether leading and trailing whitespace characters should be
      *            trimmed from non-textqualified column data or not.
      */
     public void setTrimWhitespace(boolean trimWhitespace) {
         this.trimWhitespace = trimWhitespace;
     }
 
     /**
      * Gets the character being used as the column delimiter. Default is comma,
      * ','.
      * 
      * @return The character being used as the column delimiter.
      */
     public char getDelimiter() {
         return delimiter;
     }
 
     /**
      * Sets the character to use as the column delimiter. Default is comma, ','.
      * 
      * @param delimiter
      *            The character to use as the column delimiter.
      */
     public void setDelimiter(char delimiter) {
         this.delimiter = delimiter;
     }
 
     /**
      * Sets the character to use as the record delimiter.
      * 
      * @param recordDelimiter
      *            The character to use as the record delimiter. Default is
      *            combination of standard end of line characters for Windows,
      *            Unix, or Mac.
      */
     public void setRecordDelimiter(char recordDelimiter) {
         useCustomRecordDelimiter = true;
         this.recordDelimiter = recordDelimiter;
     }
 
     /**
      * Gets the current way to escape an occurance of the text qualifier inside
      * qualified data.
      * 
      * @return The current way to escape an occurance of the text qualifier
      *         inside qualified data.
      */
     public int getEscapeMode() {
         return escapeMode;
     }
 
     /**
      * Sets the current way to escape an occurance of the text qualifier inside
      * qualified data.
      * 
      * @param escapeMode
      *            The way to escape an occurance of the text qualifier inside
      *            qualified data.
      * @exception IllegalArgumentException
      *                When an illegal value is specified for escapeMode.
      */
     public void setEscapeMode(int escapeMode) throws IllegalArgumentException {
         if (escapeMode != ESCAPE_MODE_DOUBLED
                 && escapeMode != ESCAPE_MODE_BACKSLASH) {
             throw new IllegalArgumentException(
                     "Parameter escapeMode must be a valid value.");
         }
 
         this.escapeMode = escapeMode;
     }
 
     /**
      * Whether text qualifiers will be used while parsing or not.
      * 
      * @return Whether text qualifiers will be used while parsing or not.
      */
     public boolean getUseTextQualifier() {
         return useTextQualifier;
     }
 
     /**
      * Sets whether text qualifiers will be used while parsing or not.
      * 
      * @param useTextQualifier
      *            Whether to use a text qualifier while parsing or not.
      */
     public void setUseTextQualifier(boolean useTextQualifier) {
         this.useTextQualifier = useTextQualifier;
     }
 
     /**
      * Gets the character to use as a text qualifier in the data.
      * 
      * @return The character to use as a text qualifier in the data.
      */
     public char getTextQualifier() {
         return textQualifier;
     }
 
     /**
      * Sets the character to use as a text qualifier in the data.
      * 
      * @param textQualifier
      *            The character to use as a text qualifier in the data.
      */
     public void setTextQualifier(char textQualifier) {
         this.textQualifier = textQualifier;
     }
 
     /**
      * Gets the character being used as a comment signal.
      * 
      * @return The character being used as a comment signal.
      */
     public char getComment() {
         return comment;
     }
 
     /**
      * Sets the character to use as a comment signal.
      * 
      * @param comment
      *            The character to use as a comment signal.
      */
     public void setComment(char comment) {
         this.comment = comment;
     }
 
     /**
      * Gets whether comments are being looked for while parsing or not.
      * 
      * @return Whether comments are being looked for while parsing or not.
      */
     public boolean getUseComments() {
         return useComments;
     }
 
     /**
      * Sets whether comments are being looked for while parsing or not.
      * 
      * @param useComments
      *            Whether comments are being looked for while parsing or not.
      */
     public void setUseComments(boolean useComments) {
         this.useComments = useComments;
     }
 
     /**
      * Safety caution to prevent the parser from using large amounts of memory
      * in the case where parsing settings like file encodings don't end up
      * matching the actual format of a file. This switch can be turned off if
      * the file format is known and tested. With the switch off, the max column
      * lengths and max column count per record supported by the parser will
      * greatly increase. Default is true.
      * 
      * @return
      */
     public boolean getSafetySwitch() {
         return safetySwitch;
     }
 
     /**
      * Safety caution to prevent the parser from using large amounts of memory
      * in the case where parsing settings like file encodings don't end up
      * matching the actual format of a file. This switch can be turned off if
      * the file format is known and tested. With the switch off, the max column
      * lengths and max column count per record supported by the parser will
      * greatly increase. Default is true.
      * 
      * @param safetySwitch
      */
     public void setSafetySwitch(boolean safetySwitch) {
         this.safetySwitch = safetySwitch;
     }
 
     /**
      * Returns the header values as a string array.
      * 
      * @return The header values as a String array.
      * @exception IOException
      *                Thrown if this object has already been closed.
      */
     public String[] getHeaders() throws IOException {
         checkClosed();
 
         return headers;
     }
 
     public void setHeaders(String[] headers) {
         this.headers = headers;
 
         headerIndexByName.clear();
 
         if (headers != null) {
             headersCount = headers.length;
         } else {
             headersCount = 0;
         }
 
         for (int i = 0; i < headersCount; i++) {
             headerIndexByName.put(headers[i], new Integer(i));
         }
     }
 
     public String[] getValues() throws IOException {
         checkClosed();
 
         if (values == null) {
             if (initialized) {
                 values = new String[columnsCount];
 
                 for (int i = 0; i < columnsCount; i++) {
                     values[i] = get(i);
                 }
             }
         }
 
         return values;
     }
 
     /**
      * Gets the count of headers read in by a previous call to
      * {@link com.csvreader.CsvReader#readHeaders readHeaders()}.
      * 
      * @return The count of headers read in by a previous call to
      *         {@link com.csvreader.CsvReader#readHeaders readHeaders()}.
      */
     public int getHeaderCount() {
         return headersCount;
     }
 
     /**
      * Returns the current column value for a given column index.
      * 
      * @param columnIndex
      *            The index of the column.
      * @return The current column value.
      * @exception IOException
      *                Thrown if this object has already been closed.
      */
     public String get(int columnIndex) throws IOException {
         checkClosed();
 
         String returnVal;
 
         if (columnIndex > -1 && columnIndex < columnsCount) {
             int start = columnStarts[columnIndex];
             int length = columnLengths[columnIndex];
 
             if (length == 0) {
                 returnVal = EMPTY;
             } else {
                 returnVal = new String(columnBuffer, start, length);
             }
         } else {
             returnVal = EMPTY;
         }
 
         return returnVal;
     }
 
     /**
      * Returns the current column value for a given column header name.
      * 
      * @param headerName
      *            The header name of the column.
      * @return The current column value.
      * @exception IOException
      *                Thrown if this object has already been closed.
      */
     public String get(String headerName) throws IOException {
         checkClosed();
 
         return get(getIndex(headerName));
     }
 
     /**
      * Creates a {@link com.csvreader.CsvReader CsvReader} object using a string
      * of data as the source.&nbsp;Uses ISO-8859-1 as the
      * {@link java.nio.charset.Charset Charset}.
      * 
      * @param data
      *            The String of data to use as the source.
      * @return A {@link com.csvreader.CsvReader CsvReader} object using the
      *         String of data as the source.
      */
     public static CsvReader parse(String data) {
         if (data == null) {
             throw new IllegalArgumentException(
                     "Parameter data can not be null.");
         }
 
         return new CsvReader(new StringReader(data));
     }
 
     /**
      * Reads another record.
      * 
      * @return Whether another record was successfully read or not.
      * @exception IOException
      *                Thrown if an error occurs while reading data from the
      *                source stream.
      */
     public boolean readRecord() throws IOException {
         checkClosed();
 
         clearColumns();
 
         hasReadNextLine = false;
 
         // check to see if we've already found the end of data
 
         if (hasMoreData) {
             // loop over the data stream until the end of data is found
             // or the end of the record is found
 
             do {
 
                 // loop over the data buffer until the end of the buffer
                 // is reached or until the end of the record is found
 
                 while (!hasReadNextLine && bufferPosition < bufferCount) {
                     startedWithQualifier = false;
 
                     // grab the current letter as a byte
 
                     char currentLetter = dataBuffer[bufferPosition++];
 
                     if (useTextQualifier && currentLetter == textQualifier) {
                         // this will be a text qualified column, so
                         // we need to set startedWithQualifier to make it
                         // enter the seperate branch to handle text
                         // qualified columns
 
                         lastLetter = currentLetter;
 
                         // read qualified
                         startedColumn = true;
                         startedWithQualifier = true;
                         boolean lastLetterWasQualifier = false;
 
                         char escapeChar = QUOTE;
 
                         if (escapeMode == ESCAPE_MODE_BACKSLASH) {
                             escapeChar = BACKSLASH;
                         }
 
                         // read qualified
 
                         boolean eatingTrailingJunk = false;
                         boolean lastLetterWasEscape = false;
                         boolean readingComplexEscape = false;
                         int escape = ComplexEscape.UNICODE;
                         int escapeLength = 0;
                         char escapeValue = (char) 0;
 
                         do {
                             // loop over the data buffer until the end of the
                             // buffer
                             // is reached or until a delimiter is found
 
                             while (startedColumn
                                     && bufferPosition < bufferCount) {
                                 // grab the current letter as a byte
 
                                 currentLetter = dataBuffer[bufferPosition++];
 
                                 if (eatingTrailingJunk) {
                                     if (currentLetter == delimiter) {
                                         endColumn();
                                     } else if ((!useCustomRecordDelimiter && (currentLetter == CR || currentLetter == LF))
                                             || (useCustomRecordDelimiter && currentLetter == recordDelimiter)) {
                                         endColumn();
 
                                         endRecord();
                                     }
                                 } else if (readingComplexEscape) {
                                     escapeLength++;
 
                                     switch (escape) {
                                         case ComplexEscape.UNICODE:
                                             escapeValue *= (char) 16;
                                             escapeValue += hexToDec(currentLetter);
 
                                             if (escapeLength == 4) {
                                                 readingComplexEscape = false;
                                             }
 
                                             break;
                                         case ComplexEscape.OCTAL:
                                             escapeValue *= (char) 8;
                                             escapeValue += (char) (currentLetter - '0');
 
                                             if (escapeLength == 3) {
                                                 readingComplexEscape = false;
                                             }
 
                                             break;
                                         case ComplexEscape.DECIMAL:
                                             escapeValue *= (char) 10;
                                             escapeValue += (char) (currentLetter - '0');
 
                                             if (escapeLength == 3) {
                                                 readingComplexEscape = false;
                                             }
 
                                             break;
                                         case ComplexEscape.HEX:
                                             escapeValue *= (char) 16;
                                             escapeValue += hexToDec(currentLetter);
 
                                             if (escapeLength == 2) {
                                                 readingComplexEscape = false;
                                             }
 
                                             break;
                                     }
 
                                     if (!readingComplexEscape) {
                                         addLetter(escapeValue);
                                     }
                                 } else if (currentLetter == textQualifier) {
                                     if (lastLetterWasEscape) {
                                         lastLetterWasEscape = false;
                                         lastLetterWasQualifier = false;
 
                                         addLetter(textQualifier);
                                     } else {
                                         if (escapeMode == ESCAPE_MODE_DOUBLED) {
                                             lastLetterWasEscape = true;
                                         }
 
                                         lastLetterWasQualifier = true;
                                     }
                                 } else if (escapeMode == ESCAPE_MODE_BACKSLASH
                                         && lastLetterWasEscape) {
                                     switch (currentLetter) {
                                         case 'n':
                                             addLetter(LF);
                                             break;
                                         case 'r':
                                             addLetter(CR);
                                             break;
                                         case 't':
                                             addLetter(TAB);
                                             break;
                                         case 'b':
                                             addLetter(BACKSPACE);
                                             break;
                                         case 'f':
                                             addLetter(FORM_FEED);
                                             break;
                                         case 'e':
                                             addLetter(ESCAPE);
                                             break;
                                         case 'v':
                                             addLetter(VERTICAL_TAB);
                                             break;
                                         case 'a':
                                             addLetter(ALERT);
                                             break;
                                         case '0':
                                         case '1':
                                         case '2':
                                         case '3':
                                         case '4':
                                         case '5':
                                         case '6':
                                         case '7':
                                             escape = ComplexEscape.OCTAL;
                                             readingComplexEscape = true;
                                             escapeLength = 1;
                                             escapeValue = (char) (currentLetter - '0');
                                             break;
                                         case 'u':
                                         case 'x':
                                         case 'o':
                                         case 'd':
                                         case 'U':
                                         case 'X':
                                         case 'O':
                                         case 'D':
                                             switch (currentLetter) {
                                                 case 'u':
                                                 case 'U':
                                                     escape = ComplexEscape.UNICODE;
                                                     break;
                                                 case 'x':
                                                 case 'X':
                                                     escape = ComplexEscape.HEX;
                                                     break;
                                                 case 'o':
                                                 case 'O':
                                                     escape = ComplexEscape.OCTAL;
                                                     break;
                                                 case 'd':
                                                 case 'D':
                                                     escape = ComplexEscape.DECIMAL;
                                                     break;
                                             }
 
                                             readingComplexEscape = true;
                                             escapeLength = 0;
                                             escapeValue = (char) 0;
 
                                             break;
                                         default:
                                             addLetter(currentLetter);
                                             break;
                                     }
 
                                     lastLetterWasEscape = false;
                                 } else if (currentLetter == escapeChar) {
                                     // can only happen for EscapeMode.Backslash
                                     lastLetterWasEscape = true;
                                 } else {
                                     if (lastLetterWasQualifier) {
                                         if (currentLetter == delimiter) {
                                             endColumn();
                                         } else if ((!useCustomRecordDelimiter && (currentLetter == CR || currentLetter == LF))
                                                 || (useCustomRecordDelimiter && currentLetter == recordDelimiter)) {
                                             endColumn();
 
                                             endRecord();
                                         } else {
                                             eatingTrailingJunk = true;
                                         }
 
                                         // make sure to clear the flag for next
                                         // run of
                                         // the loop
 
                                         lastLetterWasQualifier = false;
                                     } else // if !lastLetterWasQualifier
                                     {
                                         addLetter(currentLetter);
                                     }
                                 }
 
                                 // keep track of the last letter because we need
                                 // it for several key decisions
 
                                 lastLetter = currentLetter;
 
                             } // end while
 
                             checkDataLength();
 
                         } while (hasMoreData && startedColumn);
                     } else if (currentLetter == delimiter) {
                         // we encountered a column with no data, so
                         // just send the end column
 
                         lastLetter = currentLetter;
 
                         endColumn();
                     } else if ((!useCustomRecordDelimiter && (currentLetter == CR || currentLetter == LF))
                             || (useCustomRecordDelimiter && currentLetter == recordDelimiter)) {
                         // this will skip blank lines
                         if (startedColumn || columnsCount > 0
                                 || columnStart != usedColumnLength) {
                             endColumn();
 
                             endRecord();
                         }
 
                         lastLetter = currentLetter;
                     } else if (useComments && columnsCount == 0
                             && currentLetter == comment) {
                         // encountered a comment character at the beginning of
                         // the
                         // line so just ignore the rest of the line
 
                         lastLetter = currentLetter;
 
                         skipLine();
                     } else if (trimWhitespace
                             && (currentLetter == SPACE || currentLetter == TAB)) {
                         // do nothing, this will trim leading whitespace
                         // for both text qualified columns and non
 
                         startedColumn = true;
                     } else {
                         // since the letter wasn't a special letter, this
                         // will be the first letter of our current column
 
                         startedColumn = true;
                         boolean lastLetterWasBackslash = false;
                         boolean readingComplexEscape = false;
                         int escape = ComplexEscape.UNICODE;
                         int escapeLength = 0;
                         char escapeValue = (char) 0;
 
                         boolean firstLoop = true;
 
                         // read until delimiter
 
                         do {
                             // loop over the data buffer until the end of the
                             // buffer
                             // is reached or until a delimiter is found
 
                             do {
                                 if (!firstLoop) {
                                     // grab the current letter as a byte
                                     currentLetter = dataBuffer[bufferPosition++];
                                 }
 
                                 if (!useTextQualifier
                                         && escapeMode == ESCAPE_MODE_BACKSLASH
                                         && currentLetter == BACKSLASH) {
                                     if (lastLetterWasBackslash) {
                                         addLetter(BACKSLASH);
                                         lastLetterWasBackslash = false;
                                     } else {
                                         lastLetterWasBackslash = true;
                                     }
                                 } else if (readingComplexEscape) {
                                     escapeLength++;
 
                                     switch (escape) {
                                         case ComplexEscape.UNICODE:
                                             escapeValue *= (char) 16;
                                             escapeValue += hexToDec(currentLetter);
 
                                             if (escapeLength == 4) {
                                                 readingComplexEscape = false;
                                             }
 
                                             break;
                                         case ComplexEscape.OCTAL:
                                             escapeValue *= (char) 8;
                                             escapeValue += (char) (currentLetter - '0');
 
                                             if (escapeLength == 3) {
                                                 readingComplexEscape = false;
                                             }
 
                                             break;
                                         case ComplexEscape.DECIMAL:
                                             escapeValue *= (char) 10;
                                             escapeValue += (char) (currentLetter - '0');
 
                                             if (escapeLength == 3) {
                                                 readingComplexEscape = false;
                                             }
 
                                             break;
                                         case ComplexEscape.HEX:
                                             escapeValue *= (char) 16;
                                             escapeValue += hexToDec(currentLetter);
 
                                             if (escapeLength == 2) {
                                                 readingComplexEscape = false;
                                             }
 
                                             break;
                                     }
 
                                     if (!readingComplexEscape) {
                                         addLetter(escapeValue);
                                     }
                                 } else if (!useTextQualifier
                                         && escapeMode == ESCAPE_MODE_BACKSLASH
                                         && lastLetterWasBackslash) {
                                     switch (currentLetter) {
                                         case 'n':
                                             addLetter(LF);
                                             break;
                                         case 'r':
                                             addLetter(CR);
                                             break;
                                         case 't':
                                             addLetter(TAB);
                                             break;
                                         case 'b':
                                             addLetter(BACKSPACE);
                                             break;
                                         case 'f':
                                             addLetter(FORM_FEED);
                                             break;
                                         case 'e':
                                             addLetter(ESCAPE);
                                             break;
                                         case 'v':
                                             addLetter(VERTICAL_TAB);
                                             break;
                                         case 'a':
                                             addLetter(ALERT);
                                             break;
                                         case '0':
                                         case '1':
                                         case '2':
                                         case '3':
                                         case '4':
                                         case '5':
                                         case '6':
                                         case '7':
                                             escape = ComplexEscape.OCTAL;
                                             readingComplexEscape = true;
                                             escapeLength = 1;
                                             escapeValue = (char) (currentLetter - '0');
                                             break;
                                         case 'u':
                                         case 'x':
                                         case 'o':
                                         case 'd':
                                         case 'U':
                                         case 'X':
                                         case 'O':
                                         case 'D':
                                             switch (currentLetter) {
                                                 case 'u':
                                                 case 'U':
                                                     escape = ComplexEscape.UNICODE;
                                                     break;
                                                 case 'x':
                                                 case 'X':
                                                     escape = ComplexEscape.HEX;
                                                     break;
                                                 case 'o':
                                                 case 'O':
                                                     escape = ComplexEscape.OCTAL;
                                                     break;
                                                 case 'd':
                                                 case 'D':
                                                     escape = ComplexEscape.DECIMAL;
                                                     break;
                                             }
 
                                             readingComplexEscape = true;
                                             escapeLength = 0;
                                             escapeValue = (char) 0;
 
                                             break;
                                         default:
                                             addLetter(currentLetter);
                                             break;
                                     }
 
                                     lastLetterWasBackslash = false;
                                 } else {
                                     if (currentLetter == delimiter) {
                                         endColumn();
                                     } else if ((!useCustomRecordDelimiter && (currentLetter == CR || currentLetter == LF))
                                             || (useCustomRecordDelimiter && currentLetter == recordDelimiter)) {
                                         endColumn();
 
                                         endRecord();
                                     } else {
                                         addLetter(currentLetter);
                                     }
                                 }
 
                                 // keep track of the last letter because we need
                                 // it for several key decisions
 
                                 lastLetter = currentLetter;
                                 firstLoop = false;
                             } while (startedColumn
                                     && bufferPosition < bufferCount);
 
                             checkDataLength();
 
                         } while (hasMoreData && startedColumn);
                     }
 
                 } // end while
 
                 checkDataLength();
 
             } while (hasMoreData && !hasReadNextLine);
 
             // check to see if we hit the end of the file
             // without processing the current record
 
             if (startedColumn || lastLetter == delimiter) {
                 endColumn();
 
                 endRecord();
             }
         }
 
         return hasReadNextLine;
     }
 
     /**
      * @exception IOException
      *                Thrown if an error occurs while reading data from the
      *                source stream.
      */
     private void checkDataLength() throws IOException {
         if (!initialized) {
             if (fileName != null) {
                 inputStream = new InputStreamReader(new FileInputStream(
                         fileName), charset);
             }
 
             initialized = true;
         }
 
         if (bufferPosition == bufferCount) {
             try {
                 bufferCount = inputStream.read(dataBuffer, 0, MAX_BUFFER_SIZE);
             } catch (IOException e) // An I/O error occurs.
             {
                 close();
 
                 throw e;
             }
 
             bufferPosition = 0;
 
             // if no more data could be found, set flag stating that
             // the end of the data was found
 
            if (bufferCount == 0) {
                 hasMoreData = false;
             }
         }
     }
 
     /**
      * 
      */
     private void clearColumns() {
         columnsCount = 0;
         columnStart = 0;
         usedColumnLength = 0;
     }
 
     /**
      * Read the first record of data as column headers.
      * 
      * @return Whether the header record was successfully read or not.
      * @exception IOException
      *                Thrown if an error occurs while reading data from the
      *                source stream.
      */
     public boolean readHeaders() throws IOException {
         readingHeaders = true;
 
         boolean result = readRecord();
 
         readingHeaders = false;
 
         clearColumns();
 
         return result;
     }
 
     /**
      * Returns the column header value for a given column index.
      * 
      * @param columnIndex
      *            The index of the header column being requested.
      * @return The value of the column header at the given column index.
      * @exception IOException
      *                Thrown if this object has already been closed.
      */
     public String getHeader(int columnIndex) throws IOException {
         checkClosed();
 
         // check to see if we have read the header record yet
 
         // check to see if the column index is within the bounds
         // of our header array
 
         if (columnIndex > -1 && columnIndex < headersCount) {
             // return the processed header data for this column
 
             return headers[columnIndex];
         } else {
             return EMPTY;
         }
     }
 
     /**
      * @exception IOException
      *                Thrown if a very rare extreme exception occurs during
      *                parsing, normally resulting from improper data format.
      */
     private void endColumn() throws IOException {
         startedColumn = false;
 
         if (!skippingRecord) {
             if (columnsCount > 100000 && safetySwitch) {
                 close();
 
                 throw new IOException(
                         "Max column count of 100000 exceeded in record "
                                 + currentRecord + ".");
             }
 
             // check to see if our current holder array for
             // column chunks is still big enough to handle another
             // column chunk
 
             if (columnsCount == maxColumnCount) {
                 // holder array needs to grow to be able to hold another column
                 // chunk
 
                 int newMaxColumnCount = maxColumnCount
                         + Math.max(1, (int) (maxColumnCount * 1.0 / 2.0));
 
                 int[] holderStarts = new int[newMaxColumnCount];
                 int[] holderLengths = new int[newMaxColumnCount];
                 boolean[] holderQualified = new boolean[newMaxColumnCount];
 
                 System.arraycopy(columnStarts, 0, holderStarts, 0,
                         maxColumnCount);
                 System.arraycopy(columnLengths, 0, holderLengths, 0,
                         maxColumnCount);
                 System.arraycopy(columnQualified, 0, holderQualified, 0,
                         maxColumnCount);
 
                 columnStarts = holderStarts;
                 columnLengths = holderLengths;
                 columnQualified = holderQualified;
 
                 maxColumnCount = newMaxColumnCount;
             }
 
             // if column length > 0
 
             if (usedColumnLength - columnStart > 0) {
                 if (trimWhitespace && !startedWithQualifier) {
                     // we need to trim off leading and trailing whitespace here
 
                     int startOfContent = columnStart;
                     int endOfContent = usedColumnLength - 1;
 
                     // find the beginning of non-whitespace characters
 
                     while (startOfContent < usedColumnLength
                             && (columnBuffer[startOfContent] == SPACE || columnBuffer[startOfContent] == TAB)) {
                         startOfContent++;
                     }
 
                     if (startOfContent < usedColumnLength - 1) {
                         // find the end of non-whitespace characters
 
                         while (endOfContent > startOfContent
                                 && (columnBuffer[endOfContent] == SPACE || columnBuffer[endOfContent] == TAB)) {
                             endOfContent--;
                         }
                     }
 
                     columnStarts[columnsCount] = startOfContent;
                     columnLengths[columnsCount] = endOfContent - startOfContent
                             + 1;
                 } else {
                     columnStarts[columnsCount] = columnStart;
                     columnLengths[columnsCount] = usedColumnLength
                             - columnStart;
                 }
 
                 columnStart = usedColumnLength;
 
             } // end if (usedColumnLength - columnStart > 0)
             else {
                 // store a column chunk with basically a column length of 0
 
                 columnStarts[columnsCount] = 0;
                 columnLengths[columnsCount] = 0;
             }
 
             columnsCount++;
         }
     }
 
     /**
      * @exception IOException
      *                Thrown if an error occurs while reading data from the
      *                source stream.
      */
     private void endRecord() throws IOException {
         if (!skippingRecord) {
             // check to see if there was any data on this line
 
             if (columnsCount > 1 || usedColumnLength > 0
                     || startedWithQualifier) {
                 // this flag is used as a loop exit condition
                 // during parsing
 
                 hasReadNextLine = true;
 
                 // check to see if this is the header record
 
                 if (readingHeaders) {
                     // switch the header data from the growable ArrayList
                     // to the string array
 
                     headersCount = columnsCount;
 
                     headers = new String[headersCount];
 
                     for (int i = 0; i < headersCount; i++) {
                         String columnValue = this.get(i);
                         headers[i] = columnValue;
                         headerIndexByName.put(columnValue, new Integer(i));
                     }
                 } else {
                     currentRecord++;
                 }
             } else {
                 // clear public column values for current line
 
                 clearColumns();
             }
         } else {
             hasReadNextLine = true;
         }
     }
 
     /**
      * @exception IOException
      *                Thrown if a very rare extreme exception occurs during
      *                parsing, normally resulting from improper data format.
      */
     private void addLetter(char letter) throws IOException {
         if (!skippingRecord) {
             if (usedColumnLength - columnStart >= 100000 && safetySwitch) {
                 close();
 
                 throw new IOException(
                         "Max column length of 100000 exceeded in column "
                                 + columnsCount + " in record " + currentRecord
                                 + ".");
             }
 
             // check to see if our current holder array for
             // column data is still big enough to handle another
             // letter
 
             if (usedColumnLength == columnBufferSize) {
                 // holder array needs to grow to be able to hold another letter
 
                 int newBufferSize = columnBufferSize
                         + Math.max(1, (int) (columnBufferSize * 1.0 / 2.0));
 
                 char[] holder = new char[newBufferSize];
 
                 System.arraycopy(columnBuffer, 0, holder, 0, columnBufferSize);
 
                 columnBuffer = holder;
                 columnBufferSize = newBufferSize;
             }
 
             columnBuffer[usedColumnLength++] = letter;
         }
     }
 
     /**
      * Gets the corresponding column index for a given column header name.
      * 
      * @param headerName
      *            The header name of the column.
      * @return The column index for the given column header name.&nbsp;Returns
      *         -1 if not found.
      * @exception IOException
      *                Thrown if this object has already been closed.
      */
     public int getIndex(String headerName) throws IOException {
         checkClosed();
 
         Object indexValue = headerIndexByName.get(headerName);
 
         if (indexValue != null) {
             return ((Integer) indexValue).intValue();
         } else {
             return -1;
         }
     }
 
     /**
      * Skips the next record of data by parsing each column.&nbsp;Does not
      * increment
      * {@link com.csvreader.CsvReader#getCurrentRecord getCurrentRecord()}.
      * 
      * @return Whether another record was successfully skipped or not.
      * @exception IOException
      *                Thrown if an error occurs while reading data from the
      *                source stream.
      */
     public boolean skipRecord() throws IOException {
         checkClosed();
 
         boolean recordRead = false;
 
         if (hasMoreData) {
             skippingRecord = true;
 
             recordRead = readRecord();
 
             skippingRecord = false;
         }
 
         return recordRead;
     }
 
     /**
      * Skips the next line of data using the standard end of line characters and
      * does not do any column delimited parsing.
      * 
      * @return Whether a line was successfully skipped or not.
      * @exception IOException
      *                Thrown if an error occurs while reading data from the
      *                source stream.
      */
     public boolean skipLine() throws IOException {
         checkClosed();
 
         // clear public column values for current line
 
         clearColumns();
 
         boolean skippedLine = false;
 
         if (hasMoreData) {
             boolean foundEol = false;
 
             do {
                 // loop over the data buffer until the end of the buffer
                 // is reached or until the end of the line is found
 
                 while (bufferPosition < bufferCount && !foundEol) {
                     skippedLine = true;
 
                     // grab the current letter as a byte
 
                     char currentLetter = dataBuffer[bufferPosition++];
 
                     if (currentLetter == CR || currentLetter == LF) {
                         foundEol = true;
                     }
 
                     // keep track of the last letter because we need
                     // it for several key decisions
 
                     lastLetter = currentLetter;
 
                 } // end while
 
                 checkDataLength();
 
             } while (hasMoreData && !foundEol);
         }
 
         return skippedLine;
     }
 
     /**
      * 
      * 
      * @param columnIndex
      * @return
      */
     public boolean isQualified(int columnIndex) throws IOException {
         checkClosed();
 
         if (columnIndex < columnsCount && columnIndex > -1) {
             return columnQualified[columnIndex];
         } else {
             return false;
         }
     }
 
     /**
      * Returns the length of a column without doing extra work to find the
      * column's actual value.
      * 
      * @param columnIndex
      *            The index of the header column being requested.
      * @return The length of the requested column.
      * @exception IOException
      *                Thrown if this object has already been closed.
      */
     public int getLength(int columnIndex) throws IOException {
         checkClosed();
 
         if (columnIndex < columnsCount && columnIndex > -1) {
             return columnLengths[columnIndex];
         } else {
             return 0;
         }
     }
 
     /**
      * Closes and releases all related resources.
      */
     public void close() {
         if (!closed) {
             close(true);
 
             closed = true;
         }
     }
 
     /**
      * 
      */
     private void close(boolean closing) {
         if (!closed) {
             if (closing) {
                 charset = null;
                 headers = null;
                 headerIndexByName = null;
                 columnBuffer = null;
                 dataBuffer = null;
                 columnStarts = null;
                 columnLengths = null;
                 columnQualified = null;
             }
 
             try {
                 if (initialized) {
                     inputStream.close();
                 }
             } catch (Exception e) {
                 // just eat the exception
             }
 
             inputStream = null;
 
             closed = true;
         }
     }
 
     /**
      * @exception IOException
      *                Thrown if this object has already been closed.
      */
     private void checkClosed() throws IOException {
         if (closed) {
             throw new IOException(
                     "This instance of the CsvReader class has already been closed.");
         }
     }
 
     /**
      * 
      */
     protected void finalize() {
         close(false);
     }
 
     private class ComplexEscape {
         private static final int UNICODE = 1;
 
         private static final int OCTAL = 2;
 
         private static final int DECIMAL = 3;
 
         private static final int HEX = 4;
     }
 
     private static char hexToDec(char hex) {
         char result;
 
         if (hex >= 'a') {
             result = (char) (hex - 'a' + 10);
         } else if (hex >= 'A') {
             result = (char) (hex - 'A' + 10);
         } else {
             result = (char) (hex - '0');
         }
 
         return result;
     }
 }
