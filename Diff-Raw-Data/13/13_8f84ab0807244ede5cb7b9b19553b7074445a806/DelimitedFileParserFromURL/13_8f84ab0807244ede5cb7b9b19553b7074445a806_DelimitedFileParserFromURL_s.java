 /**
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, Board of Trustees-University of Illinois.
  * All rights reserved.
  *
  * Developed by:
  *
  * Automated Learning Group
  * National Center for Supercomputing Applications
  * http://www.seasr.org
  *
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal with the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimers.
  *
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimers in the
  *    documentation and/or other materials provided with the distribution.
  *
  *  * Neither the names of Automated Learning Group, The National Center for
  *    Supercomputing Applications, or University of Illinois, nor the names of
  *    its contributors may be used to endorse or promote products derived from
  *    this Software without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
  * WITH THE SOFTWARE.
  */
 
 package org.seasr.meandre.support.components.io.file.input;
 
 
import java.io.*;
import java.util.*;
 
 import org.meandre.tools.webdav.WebdavClient;
 import org.seasr.datatypes.datamining.table.ColumnTypes;
import org.seasr.datatypes.table.*;
 
 /**
  * Reads data from a delimited file. The delimiter is found automatically, or
  * can be set.
  * <p><b>Note:</b>  This module is the same as deprecated module
  * <i>DelimitedFileParser</i>, extended to access the data through
  * <i>DataObjectProxy</i>.</p>
  *
  * @author  $Author: dfleming $
  * @version $Revision: 1.2 $, $Date: 2007/01/23 23:09:35 $
  */
 public class DelimitedFileParserFromURL implements FlatFileParser {
 
    //~ Static fields/initializers **********************************************
 
    /** Possible delimiters. */
    static private final char TAB = '\t';
 
    /** Description of field SPACE. */
    static private final char SPACE = ' ';
 
    /** Description of field COMMA. */
    static protected final char COMMA = ',';
 
    /** Description of field PIPE. */
    static private final char PIPE = '|';
 
    /** Description of field EQUALS. */
    static private final char EQUALS = '=';
 
    //~ Instance fields *********************************************************
 
    /** Description of field blankRows. */
    private int blankRows = 0;
 
    /** the data (in/out) types of the columns. */
    private int[] dataTypes;
 
    /** the feature (nom/scalar) types of the columns. */
    private int[] featureTypes;
 
    /** the index, from 0, of the in out row. */
    private int inOutRow;
 
    /** the index, from 0, of the labels row. */
    private int labelsRow;
 
    /** the index, from 0, of the nominal/scalar row. */
    private int nomScalarRow;
 
    /** the index, from 0, of the types row. */
    private int typesRow;
 
    /** the labels of the columns. */
    protected String[] columnLabels;
 
    /** the types of the columns. */
    protected int[] columnTypes;
 
    /** the delimter for this file. */
    protected char delimiter;
 
    /** the file reader. */
    protected LineNumberReader lineReader;
 
    /** The theClient to read. */
    //protected DataObjectProxy mDataObj;
    protected WebdavClient client;
 
    /** the resource location. */
    protected String url;
 
    /** the number of columns in the file. */
    protected int numColumns;
 
    /** the number of data rows in the file (does not include meta rows). */
    protected int numRows;
 
    //~ Constructors ************************************************************
 
    /**
     * Creates a new DelimitedFileParser object.
     */
    protected DelimitedFileParserFromURL() { }
 
    /**
     * Create a new DelimitedFileReader with no types row, no labels row, no in
     * out row, no nominal scalar row.
     *
     * @param  theClient the file to read
     * @param  theURL    the resource location
     *
     * @throws Exception Description of exception Exception.
     */
    public DelimitedFileParserFromURL(WebdavClient theClient,
                                      String theURL) throws Exception {
       this(theClient, theURL, -1, -1, -1, -1);
    }
 
    /**
     * Create a new DelimitedFileReader with the specified labels row.
     *
     * @param  theClient  the file to read
     * @param  theURL     the resource location
     * @param  _labelsRow the index of the labels row
     *
     * @throws Exception Description of exception Exception.
     */
    public DelimitedFileParserFromURL(WebdavClient theClient,
                                      String theURL,
                                      int _labelsRow) throws Exception {
        this(theClient, theURL, _labelsRow, -1, -1, -1);
    }
 
    /**
     * Create a new DelimitedFileReader with the specified labels and types rows.
     *
     * @param  theClient  the file to read
     * @param  theURL     the resource location
     * @param  _labelsRow the index of the labels row
     * @param  _typesRow  the index of the types row
     *
     * @throws Exception Description of exception Exception.
     */
    public DelimitedFileParserFromURL(WebdavClient theClient,
                                      String theURL,
                                      int _labelsRow,
                                      int _typesRow) throws Exception {
        this(theClient, theURL, _labelsRow, _typesRow, -1, -1);
    }
 
    /**
     * Creates a new DelimitedFileParser object.
     *
     * @param  theClient  Description of parameter theClient.
     * @param  theURL     Description of parameter theURL.
     * @param  _labelsRow Description of parameter _labelsRow.
     * @param  _typesRow  Description of parameter _typesRow.
     * @param  delim      Description of parameter delim.
     *
     * @throws Exception Description of exception Exception.
     */
    public DelimitedFileParserFromURL(WebdavClient theClient,
                                      String theURL,
                                      int _labelsRow,
                                      int _typesRow,
                                      char delim) throws Exception {
        this(theClient, theURL, _labelsRow, _typesRow, -1, -1, delim); }
 
 
    /**
     * Create a new DelimitedFileReader with the specified labels, types, inout,
     * and nominal/scalar rows.
     *
     * @param  theClient     the file to read
     * @param  theURL        the resource location
     * @param  _labelsRow    the index of the labels row
     * @param  _typesRow     the index of the types row
     * @param  _inOutRow     the index of the in-out row
     * @param  _nomScalarRow the index of the nominal-scalar row
     *
     * @throws Exception Description of exception Exception.
     */
    public DelimitedFileParserFromURL(WebdavClient theClient,
                                      String theURL,
                                      int _labelsRow,
                                      int _typesRow,
                                      int _inOutRow,
                                      int _nomScalarRow) throws Exception {
       client = theClient;
       url = theURL;
       typesRow = _typesRow;
       labelsRow = _labelsRow;
       inOutRow = _inOutRow;
       nomScalarRow = _nomScalarRow;
 
 
       // read through the file to count the number of rows, columns, and find
       // the delimiter
       scanFile();
 
       lineReader =
          new LineNumberReader(new InputStreamReader(client.getResourceAsStream(url)));
                  //dataobj.getInputStream()));
 
 
       // now read in the types, scalar, in out rows, labels
       if (typesRow > -1) {
          numRows--;
 
          // now parse the line and get the types
          ArrayList row = getLineElements(typesRow);
 
          if (row != null) {
             createColumnTypes(row);
          } else {
             throw new Exception("Delimited File Parser: types' row number " +
                                 typesRow + " does not exist in the file");
          }
 
       } else {
          columnTypes = null;
       }
 
       if (labelsRow > -1) {
          numRows--;
 
          // now parse the line and the the labels
          ArrayList row = getLineElements(labelsRow);
 
          if (row != null) {
             createColumnLabels(row);
          } else {
             throw new Exception("Delimited File Parser: labels' row number " +
                                 labelsRow + " does not exist in the file");
          }
 
       } else {
          columnLabels = null;
       }
 
       lineReader.setLineNumber(0);
 
    }
 
    /**
     * Create a new DelimitedFileReader with the specified labels, types, inout,
     * and nominal/scalar rows.
     *
     * @param  theClient     the file to read
     * @param  theURL        the resource location
     * @param  _labelsRow    the index of the labels row
     * @param  _typesRow     the index of the types row
     * @param  _inOutRow     the index of the in-out row
     * @param  _nomScalarRow the index of the nominal-scalar row
     * @param  delim         Description of parameter delim.
     *
     * @throws Exception Description of exception Exception.
     */
    public DelimitedFileParserFromURL(WebdavClient theClient,
                                      String theURL,
                                      int _labelsRow,
                                      int _typesRow,
                                      int _inOutRow,
                                      int _nomScalarRow,
                                      char delim) throws Exception {
       client = theClient;
       url = theURL;
       typesRow = _typesRow;
       labelsRow = _labelsRow;
       inOutRow = _inOutRow;
       nomScalarRow = _nomScalarRow;
 
       setDelimiter(delim);
       this.scanRowsCols();
 
       lineReader =
          new LineNumberReader(new InputStreamReader(client.getResourceAsStream(url)));
 
       // now read in the types, scalar, in out rows, labels
       if (typesRow > -1) {
          numRows--;
 
          // now parse the line and get the types
          ArrayList row = getLineElements(typesRow);
          createColumnTypes(row);
       } else {
          columnTypes = null;
       }
 
       if (labelsRow > -1) {
          numRows--;
 
          // now parse the line and the the labels
          ArrayList row = getLineElements(labelsRow);
          createColumnLabels(row);
       } else {
          columnLabels = null;
       }
 
       lineReader.setLineNumber(0);
    }
 
    //~ Methods *****************************************************************
 
    /**
     * Count the number of tokens in a row.
     *
     * @param  row the row to count
     *
     * @return the number of tokens in the row
     */
    private int countRowElements(String row) {
       int current = 0;
 
       char[] bytes = row.toCharArray();
       int len = bytes.length;
 
       int numToks = 0;
 
       for (int i = 0; i < len; i++) {
 
          if (bytes[i] == getDelimiter()) {
             current = i + 1;
             numToks++;
          }
       }
 
       if ((len - current) > 0) {
          numToks++;
       }
 
       return numToks;
    }
 
    /**
     * Description of method createColumnLabels.
     *
     * @param row Description of parameter row.
     */
    private void createColumnLabels(ArrayList row) {
       columnLabels = new String[row.size()];
 
       for (int i = 0; i < row.size(); i++) {
          columnLabels[i] = new String((char[]) row.get(i));
       }
    }
 
    /**
     * Create the columns types.
     *
     * @param row Description of parameter row.
     */
    private void createColumnTypes(ArrayList row) {
 
       columnTypes = new int[row.size()];
 
       for (int i = 0; i < row.size(); i++) {
          char[] ty = (char[]) row.get(i);
          String type = new String(ty).trim();
 
          if (type.equalsIgnoreCase(STRING_TYPE)) {
             columnTypes[i] = ColumnTypes.STRING;
          } else if (type.equalsIgnoreCase(DOUBLE_TYPE)) {
             columnTypes[i] = ColumnTypes.DOUBLE;
          } else if (type.equalsIgnoreCase(INT_TYPE)) {
             columnTypes[i] = ColumnTypes.INTEGER;
          } else if (type.equalsIgnoreCase(FLOAT_TYPE)) {
             columnTypes[i] = ColumnTypes.FLOAT;
          } else if (type.equalsIgnoreCase(SHORT_TYPE)) {
             columnTypes[i] = ColumnTypes.SHORT;
          } else if (type.equalsIgnoreCase(LONG_TYPE)) {
             columnTypes[i] = ColumnTypes.LONG;
          } else if (type.equalsIgnoreCase(BYTE_TYPE)) {
             columnTypes[i] = ColumnTypes.BYTE;
          } else if (type.equalsIgnoreCase(CHAR_TYPE)) {
             columnTypes[i] = ColumnTypes.CHAR;
          } else if (type.equalsIgnoreCase(BYTE_ARRAY_TYPE)) {
             columnTypes[i] = ColumnTypes.BYTE_ARRAY;
          } else if (type.equalsIgnoreCase(CHAR_ARRAY_TYPE)) {
             columnTypes[i] = ColumnTypes.CHAR_ARRAY;
          } else if (type.equalsIgnoreCase(BOOLEAN_TYPE)) {
             columnTypes[i] = ColumnTypes.BOOLEAN;
          } else if (type.equalsIgnoreCase(NOMINAL_TYPE)) {
             columnTypes[i] = ColumnTypes.NOMINAL;
          } else {
             columnTypes[i] = ColumnTypes.STRING;
          }
       } // end for
    } // end method createColumnTypes
 
    /**
     * Read in a row and put its elements into an ArrayList.
     *
     * @param  rowNum the row to tokenize
     *
     * @return an ArrayList containing each of the elements in the row
     */
    private ArrayList getLineElements(int rowNum) {
 
       try {
          skipToLine(rowNum);
 
          String row = lineReader.readLine();
          int current = 0;
          ArrayList thisRow = new ArrayList();
          char[] bytes = row.toCharArray();
          char del = getDelimiter();
          int len = bytes.length;
 
          for (int i = 0; i < len; i++) {
 
             if (bytes[i] == del) {
 
                if ((i - current) > 0) {
                   char[] newBytes = new char[i - current];
                   System.arraycopy(bytes, current, newBytes, 0, i - current);
                   thisRow.add(newBytes);
                } else {
                   thisRow.add(new char[0]);
                }
 
                current = i + 1;
             }
          }
 
          if ((len - current) > 0) {
             char[] newBytes = new char[len - current];
             System.arraycopy(bytes, current, newBytes, 0, len - current);
             thisRow.add(newBytes);
          }
 
          return thisRow;
       } catch (Exception e) {
          return null;
       }
    } // end method getLineElements
 
    /**
     * This method will search the document, counting the number of each possible
     * delimiter per line to identify the delimiter to use. If in the first pass
     * we can not find a single delimiter that that can be found the same number
     * of times in each line, we will strip all the whitespace off the start and
     * end of the lines, and try again. If then we still can not find the
     * delimiter, we will fail.
     *
     * <p>This method also counts the number of rows and columns in the file.</p>
     *
     * @throws Exception   Description of exception Exception.
     * @throws IOException Description of exception IOException.
     */
    private void scanFile() throws Exception {
       int[] counters = new int[4];
       final int tabIndex = 0;
       final int spaceIndex = 1;
       final int commaIndex = 2;
       final int pipeIndex = 3;
 
       // Now just count them.
       int commaCount = -1;
       int spaceCount = -1;
       int tabCount = -1;
       int pipeCount = -1;
       boolean isComma = true;
       boolean isSpace = true;
       boolean isTab = true;
       boolean isPipe = true;
 
       String line;
       final int NUM_ROWS_TO_COUNT = 10;
       ArrayList lines = new ArrayList();
 
       BufferedReader reader =
          new BufferedReader(new InputStreamReader(client.getResourceAsStream(url)));
 
       // read the file in one row at a time
       int currentRow = 0;
 
       while (
              ((line = reader.readLine()) != null) &&
                 (currentRow < NUM_ROWS_TO_COUNT)) {
          lines.add(line);
 
          char[] bytes = line.toCharArray();
 
          // In this row, count instances of each delimiter
          for (int i = 0; i < bytes.length; i++) {
 
             switch (bytes[i]) {
 
                case TAB:
                   counters[tabIndex]++;
 
                   break;
 
                case SPACE:
                   counters[spaceIndex]++;
 
                   break;
 
                case COMMA:
                   counters[commaIndex]++;
 
                   break;
 
                case PIPE:
                   counters[pipeIndex]++;
 
                   break;
             }
          }
 
          // If first row, just init the counts...
          if (currentRow == 0) {
             commaCount = counters[commaIndex] == 0 ? -1 : counters[commaIndex];
             spaceCount = counters[spaceIndex] == 0 ? -1 : counters[spaceIndex];
             tabCount = counters[tabIndex] == 0 ? -1 : counters[tabIndex];
             pipeCount = counters[pipeIndex] == 0 ? -1 : counters[pipeIndex];
          } else {
 
             // Check that the counts remain the same.
             if (counters[commaIndex] != commaCount) {
                isComma = false;
             }
 
             if (counters[spaceIndex] != spaceCount) {
                isSpace = false;
             }
 
             if (counters[tabIndex] != tabCount) {
                isTab = false;
             }
 
             if (counters[pipeIndex] != pipeCount) {
                isPipe = false;
             }
          }
 
          counters[tabIndex] =
             counters[spaceIndex] =
                counters[commaIndex] = counters[pipeIndex] = 0;
          currentRow++;
       } // end while
 
       if (lines.size() < 2) {
          throw new Exception("The input file must have at least 2 rows for" +
                              " the delimiter to be identified automatically. \n" +
                              url +
                              " has only " +
                              lines.size() +
                              " row(s). ");
       }
 
       boolean delimiterFound = false;
 
       if (
           (commaCount <= 0) &&
              (spaceCount <= 0) &&
              (tabCount >= 0) &&
              (pipeCount <= 0)) {
          isTab = true;
       } else if (
                  (commaCount <= 0) &&
                     (spaceCount >= 0) &&
                     (tabCount <= 0) &&
                     (pipeCount <= 0)) {
          isSpace = true;
       } else if (
                  (commaCount >= 0) &&
                     (spaceCount <= 0) &&
                     (tabCount <= 0) &&
                     (pipeCount <= 0)) {
          isComma = true;
       } else if (
                  (commaCount <= 0) &&
                     (spaceCount <= 0) &&
                     (tabCount <= 0) &&
                     (pipeCount >= 0)) {
          isPipe = true;
       }
 
       // Did one of the possible delimiters come up a winner?
       if (isComma && !isSpace && !isTab && !isPipe) {
          setDelimiter(COMMA);
          delimiterFound = true;
       } else if (!isComma && isSpace && !isTab && !isPipe) {
          setDelimiter(SPACE);
          delimiterFound = true;
       } else if (!isComma && !isSpace && isTab && !isPipe) {
          setDelimiter(TAB);
          delimiterFound = true;
       } else if (!isComma && !isSpace && !isTab && isPipe) {
          setDelimiter(PIPE);
          delimiterFound = true;
       }
 
       if (!delimiterFound) {
 
          // OK, that didn't work. Lets trim the strings and see if it will work
          // the. read the file in one row at a time
          isComma = true;
          isSpace = true;
          isTab = true;
          isPipe = false;
 
          for (currentRow = 0; currentRow < lines.size(); currentRow++) {
             String tmp = ((String) lines.get(currentRow)).trim();
             char[] bytes = tmp.toCharArray();
             counters[tabIndex] =
                counters[spaceIndex] = counters[commaIndex] = 0;
 
             // In this row, count instances of each delimiter
             for (int i = 0; i < bytes.length; i++) {
 
                switch (bytes[i]) {
 
                   case TAB:
                      counters[tabIndex]++;
 
                      break;
 
                   case SPACE:
                      counters[spaceIndex]++;
 
                      break;
 
                   case COMMA:
                      counters[commaIndex]++;
 
                      break;
 
                   case PIPE:
                      counters[pipeIndex]++;
 
                      break;
                }
             }
 
             // If first row, just init the counts...
             if (currentRow == 0) {
                commaCount =
                   counters[commaIndex] == 0 ? -1 : counters[commaIndex];
                spaceCount =
                   counters[spaceIndex] == 0 ? -1 : counters[spaceIndex];
                tabCount = counters[tabIndex] == 0 ? -1 : counters[tabIndex];
                pipeCount = counters[pipeIndex] == 0 ? -1 : counters[pipeIndex];
             } else {
 
                // Check that the counts remain the same.
                if (counters[commaIndex] != commaCount) {
                   isComma = false;
                }
 
                if (counters[spaceIndex] != spaceCount) {
                   isSpace = false;
                }
 
                if (counters[tabIndex] != tabCount) {
                   isTab = false;
                }
 
                if (counters[pipeIndex] != pipeCount) {
                   isPipe = false;
                }
             }
          } // end for
 
          if (
              (commaCount <= 0) &&
                 (spaceCount <= 0) &&
                 (tabCount > 0) &&
                 (pipeCount <= 0)) {
             isTab = true;
          } else if (
                     (commaCount <= 0) &&
                        (spaceCount >= 0) &&
                        (tabCount <= 0) &&
                        (pipeCount <= 0)) {
             isSpace = true;
          } else if (
                     (commaCount >= 0) &&
                        (spaceCount <= 0) &&
                        (tabCount <= 0) &&
                        (pipeCount <= 0)) {
             isComma = true;
          } else if (
                     (commaCount <= 0) &&
                        (spaceCount <= 0) &&
                        (tabCount <= 0) &&
                        (pipeCount >= 0)) {
             isPipe = true;
          }
 
          // Did one of the possible delimiters come up a winner?
          if (isComma && !isSpace && !isTab && !isPipe) {
             setDelimiter(COMMA);
             delimiterFound = true;
          } else if (!isComma && isSpace && !isTab && !isPipe) {
             setDelimiter(SPACE);
             delimiterFound = true;
          } else if (!isComma && !isSpace && isTab && !isPipe) {
             setDelimiter(TAB);
             delimiterFound = true;
          } else if (!isComma && !isSpace && !isTab && isPipe) {
             setDelimiter(PIPE);
             delimiterFound = true;
          }
 
          if (!delimiterFound) {
             throw new IOException("No delimiter could be found in "+url);
          }
       } // end if
 
       scanRowsCols();
    } // end method scanFile
 
    /**
     * Description of method scanRowsCols.
     *
     * @throws Exception Description of exception Exception.
     */
    private void scanRowsCols() throws Exception {
       int nr = 0;
       int nc = 0;
 
       BufferedReader reader =
          new BufferedReader(new InputStreamReader(client.getResourceAsStream(url)));
       String line;
 
       // read the file in one row at a time
       while ((line = reader.readLine()) != null) {
 
          if (line.trim().length() > 0) {
             nr++;
 
             int ct = countRowElements(line);
 
             if (ct > nc) {
                nc = ct;
             }
          }
       }
 
       numRows = nr;
       numColumns = nc;
 
       reader.close();
    } // end method scanRowsCols
 
 
    /**
     * Skip to a specific line in the file.
     *
     * @param lineNum the line number to skip to
     */
    private void skipToLine(int lineNum) {
 
       try {
 
          if (lineNum < lineReader.getLineNumber()) {
             lineReader =
                new LineNumberReader(new InputStreamReader(client.getResourceAsStream(url)));
          }
 
          int ctr = 0;
 
          while (ctr < lineNum) {
             lineReader.readLine();
             ctr++;
          }
       } catch (Exception e) { }
    }
 
    /**
     * Skip to a specific row in the file. Rows are lines of data in the file,
     * not including the optional meta data rows.
     *
     * @param  rowNum the row number to skip to
     *
     * @return skip to a specific row in the file. Rows are lines of data in the
     *         file, not including the optional meta data rows.
     */
    protected String skipToRow(int rowNum) {
 
       if (labelsRow > -1) {
          rowNum++;
       }
 
       if (typesRow > -1) {
          rowNum++;
       }
 
       if (inOutRow > -1) {
          rowNum++;
       }
 
       if (nomScalarRow > -1) {
          rowNum++;
       }
 
       rowNum += blankRows;
 
       try {
 
          if (rowNum < lineReader.getLineNumber()) {
             lineReader =
                new LineNumberReader(new InputStreamReader(client.getResourceAsStream(url)));
          }
 
          int current = lineReader.getLineNumber();
 
          while (current < rowNum - 1) {
             lineReader.readLine();
             current++;
          }
 
          String line;
 
          while (
                 ((line = lineReader.readLine()) != null) &&
                    (line.trim().length() == 0)) {
             blankRows++;
          }
 
          return line;
       } catch (Exception e) {
          return null;
       }
    } // end method skipToRow
 
    /**
     * Get the column label at column i.
     *
     * @param  i Description of parameter i.
     *
     * @return the column label at column i, or null if no column labels were
     *         specified
     *
     * @throws ArrayIndexOutOfBoundsException Description of exception
     *                                        ArrayIndexOutOfBoundsException.
     */
    public String getColumnLabel(int i) {
 
       if (columnLabels == null) {
          return "column_" + i;
       }
 
       try {
          if (columnLabels[i] != null && columnLabels[i].length() != 0) {
             return columnLabels[i];
          } else {
             return "column_" + i;
          }
       } catch (ArrayIndexOutOfBoundsException e) {
          throw new ArrayIndexOutOfBoundsException("DelimitedFileParser: The number of column " +
                                                   "labels does not match the number of columns.");
       }
    }
 
    /**
     * Get the column type at column i.
     *
     * @param  i Description of parameter i.
     *
     * @return the column type at column i, or -1 if no column types were
     *         specified
     *
     * @throws ArrayIndexOutOfBoundsException Description of exception
     *                                        ArrayIndexOutOfBoundsException.
     */
    public int getColumnType(int i) {
 
       if (columnTypes == null) {
          return -1;
       }
 
       try {
          return columnTypes[i];
       } catch (ArrayIndexOutOfBoundsException e) {
          throw new ArrayIndexOutOfBoundsException("DelimitedFileParser: Types row = " +
                                                   typesRow + " labels row = " +
                                                   labelsRow +
                                                   " - The number of column " +
                                                   "types does not match the number of column labels.");
       }
    }
 
    /**
     * Get the data type (in-out) at column i.
     *
     * @param  i Description of parameter i.
     *
     * @return the data type (in-out) at column i, or -1 if no in-out types were
     *         specified
     */
    public int getDataType(int i) {
 
       if (dataTypes == null) {
          return -1;
       }
 
       return dataTypes[i];
    }
 
    /**
     * Description of method getDelimiter.
     *
     * @return Description of return value.
     */
    public char getDelimiter() { return delimiter; }
 
    /**
     * Get the feature type (nominal-scalar) at column i.
     *
     * @param  i Description of parameter i.
     *
     * @return the feature type at column i, or -1 if no nominal-scalar types
     *         were specified.
     */
    public int getFeatureType(int i) {
 
       if (featureTypes == null) {
          return -1;
       }
 
       return featureTypes[i];
    }
 
 
    /**
     * Get the number of columns.
     *
     * @return the number of columns
     */
    public int getNumColumns() { return numColumns; }
 
    /**
     * Get the number of rows.
     *
     * @return the number of rows
     */
    public int getNumRows() { return numRows; }
 
    /**
     * Read in a row and put its elements into an ArrayList.
     *
     * @param  rowNum the row to tokenize
     *
     * @return an ArrayList containing each of the elements in the row
     */
    public ParsedLine getRowElements(int rowNum) {
 
       try {
          ParsedLine pl = new ParsedLine();
          String row = skipToRow(rowNum);
 
          if (row == null) {
             return null;
          }
 
          int current = 0;
          char[][] thisRow = new char[numColumns][];
          boolean[] bl = new boolean[numColumns];
          int counter = 0;
          char[] bytes = row.toCharArray();
          char del = getDelimiter();
          int len = bytes.length;
 
          for (int i = 0; i < len; i++) {
 
             if (bytes[i] == del) {
 
                if ((i - current) > 0) {
                   char[] newBytes = new char[i - current];
                   System.arraycopy(bytes, current, newBytes, 0, i - current);
                   thisRow[counter] = newBytes;
                   counter++;
                } else {
                   bl[counter] = true;
                   thisRow[counter] = new char[0];
                   counter++;
                }
 
                current = i + 1;
             }
          }
 
          if ((len - current) > 0) {
             char[] newBytes = new char[len - current];
             System.arraycopy(bytes, current, newBytes, 0, len - current);
             thisRow[counter] = newBytes;
             counter++;
          }
 
          for (int i = counter; i < thisRow.length; i++) {
             thisRow[i] = new char[0];
             bl[i] = true;
          }
 
          pl.elements = thisRow;
          pl.blanks = bl;
 
          return pl;
       } catch (Exception e) {
          return null;
       }
    } // end method getRowElements
 
 
    /**
     * Description of method setDelimiter.
     *
     * @param d Description of parameter d.
     */
    public void setDelimiter(char d) { delimiter = d; }
 
   public void finalize() {}
 
    /**
     *
     * @return text read from WebDAV.
     */
    public String toText() {
        StringBuffer sb = new StringBuffer();
        String line = null;
        try {
            while((line = lineReader.readLine()) != null)
                sb.append(line).append("\n");
        }catch(IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
 
 } // end class DelimitedFileParser
