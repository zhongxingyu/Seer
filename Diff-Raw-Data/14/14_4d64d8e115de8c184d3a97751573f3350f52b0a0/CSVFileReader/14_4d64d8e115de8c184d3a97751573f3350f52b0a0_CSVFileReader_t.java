 //
 // Treasure Data Bulk-Import Tool in Java
 //
 // Copyright (C) 2012 - 2013 Muga Nishizawa
 //
 //    Licensed under the Apache License, Version 2.0 (the "License");
 //    you may not use this file except in compliance with the License.
 //    You may obtain a copy of the License at
 //
 //        http://www.apache.org/licenses/LICENSE-2.0
 //
 //    Unless required by applicable law or agreed to in writing, software
 //    distributed under the License is distributed on an "AS IS" BASIS,
 //    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //    See the License for the specific language governing permissions and
 //    limitations under the License.
 //
 package com.treasure_data.td_import.reader;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.supercsv.comment.CommentMatcher;
 import org.supercsv.exception.SuperCsvException;
 import org.supercsv.prefs.CsvPreference;
 
 import com.treasure_data.td_import.model.TimeColumnSampling;
 import com.treasure_data.td_import.prepare.CSVPrepareConfiguration;
 import com.treasure_data.td_import.prepare.PreparePartsException;
 import com.treasure_data.td_import.prepare.Task;
 import com.treasure_data.td_import.writer.FileWriter;
 import com.treasure_data.td_import.writer.JSONFileWriter;
 
 public class CSVFileReader extends FixnumColumnsFileReader<CSVPrepareConfiguration> {
     private static final Logger LOG = Logger.getLogger(CSVFileReader.class.getName());
 
     static class Tokenizer extends org.supercsv.io.AbstractTokenizer {
         private static final char NEWLINE = '\n';
 
         private static final char SPACE = ' ';
 
         private final StringBuilder currentColumn = new StringBuilder();
 
         /* the raw, untokenized CSV row (may span multiple lines) */
         private final StringBuilder currentRow = new StringBuilder();
 
         private final int quoteChar;
 
         private final int delimeterChar;
 
         private final boolean surroundingSpacesNeedQuotes;
 
         private final CommentMatcher commentMatcher;
 
         /**
          * Enumeration of tokenizer states. QUOTE_MODE is activated between quotes.
          */
         private enum TokenizerState {
             NORMAL, QUOTE_MODE;
         }
         /**
          * Constructs a new <tt>Tokenizer</tt>, which reads the CSV file, line by line.
          *
          * @param reader
          *            the reader
          * @param preferences
          *            the CSV preferences
          * @throws NullPointerException
          *             if reader or preferences is null
          */
         public Tokenizer(final Reader reader, final CsvPreference preferences) {
             super(reader, preferences);
             this.quoteChar = preferences.getQuoteChar();
             this.delimeterChar = preferences.getDelimiterChar();
             this.surroundingSpacesNeedQuotes = preferences.isSurroundingSpacesNeedQuotes();
             this.commentMatcher = preferences.getCommentMatcher();
         }
 
         /**
          * {@inheritDoc}
          */
         public boolean readColumns(final List<String> columns) throws IOException {
             if (columns == null) {
                 throw new NullPointerException("columns should not be null");
             }
 
             // clear the reusable List and StringBuilders
             columns.clear();
             currentColumn.setLength(0);
             currentRow.setLength(0);
 
             // keep reading lines until data is found
             String line;
             do {
                 line = readLine();
                 if (line == null) {
                     return false; // EOF
                 }
             } while (line.length() == 0
                     || (commentMatcher != null && commentMatcher.isComment(line)));
 
             // update the untokenized CSV row
             currentRow.append(line);
 
             // add a newline to determine end of line (making parsing easier)
             line += NEWLINE;
 
             // process each character in the line, catering for surrounding quotes (QUOTE_MODE)
             TokenizerState state = TokenizerState.NORMAL;
             int quoteScopeStartingLine = -1; // the line number where a potential multi-line cell starts
             int potentialSpaces = 0; // keep track of spaces (so leading/trailing space can be removed if required)
             int charIndex = 0;
             while (true) {
 
                 final char c = line.charAt(charIndex);
 
                 if (TokenizerState.NORMAL.equals(state)) {
                     /*
                      * NORMAL mode (not within quotes).
                      */
 
                     if (c == delimeterChar) {
                         /*
                          * Delimiter. Save the column (trim trailing space if
                          * required) then continue to next character.
                          */
                         if (!surroundingSpacesNeedQuotes) {
                             appendSpaces(currentColumn, potentialSpaces);
                         }
                         columns.add(currentColumn.length() > 0 ? currentColumn.toString() : null); // "" -> null
                         potentialSpaces = 0;
                         currentColumn.setLength(0);
                     } else if (c == SPACE) {
                         /*
                          * Space. Remember it, then continue to next character.
                          */
                         potentialSpaces++;
                     } else if (c == NEWLINE) {
                         /*
                          * Newline. Add any required spaces (if surrounding
                          * spaces don't need quotes) and return (we've read a
                          * line!).
                          */
                         if (!surroundingSpacesNeedQuotes) {
                             appendSpaces(currentColumn, potentialSpaces);
                         }
                         columns.add(currentColumn.length() > 0 ? currentColumn.toString() : null); // "" -> null
                         return true;
                     } else if (c == quoteChar) {
                         /*
                          * A single quote ("). Update to QUOTESCOPE (but don't
                          * save quote), then continue to next character.
                          */
                         state = TokenizerState.QUOTE_MODE;
                         quoteScopeStartingLine = getLineNumber();
 
                         // cater for spaces before a quoted section (be
                         // lenient!)
                         if (!surroundingSpacesNeedQuotes
                                 || currentColumn.length() > 0) {
                             appendSpaces(currentColumn, potentialSpaces);
                         }
                         potentialSpaces = 0;
                     } else {
                         /*
                          * Just a normal character. Add any required spaces (but
                          * trim any leading spaces if surrounding spaces need
                          * quotes), add the character, then continue to next
                          * character.
                          */
                         if (!surroundingSpacesNeedQuotes || currentColumn.length() > 0) {
                             appendSpaces(currentColumn, potentialSpaces);
                         }
 
                         potentialSpaces = 0;
                         currentColumn.append(c);
                     }
 
                 } else {
                     /*
                      * QUOTE_MODE (within quotes).
                      */
 
                     if (c == NEWLINE) {
 
                         /*
                          * Newline. Doesn't count as newline while in QUOTESCOPE. Add the newline char, reset the charIndex
                          * (will update to 0 for next iteration), read in the next line, then then continue to next
                          * character. For a large file with an unterminated quoted section (no trailing quote), this could
                          * cause memory issues as it will keep reading lines looking for the trailing quote. Maybe there
                          * should be a configurable limit on max lines to read in quoted mode?
                          */
                         currentColumn.append(NEWLINE);
                         currentRow.append(NEWLINE); // specific line terminator lost, \n will have to suffice
 
                         charIndex = -1;
                         line = readLine();
                         if (line == null) {
                             throw new SuperCsvException(
                                     String.format(
                                             "unexpected end of file while reading quoted column beginning on line %d and ending on line %d",
                                             quoteScopeStartingLine,
                                             getLineNumber()));
                         }
 
                         currentRow.append(line); // update untokenized CSV row
                         line += NEWLINE; // add newline to simplify parsing
                     } else if (c == quoteChar) {
                        if (charIndex > 2 && line.charAt(charIndex - 2) == '\\'
                                && line.charAt(charIndex - 1) == '\\') {
                            // TODO FIXME it's monkey patch
                            state = TokenizerState.NORMAL;
                            quoteScopeStartingLine = -1;
                        } else if (charIndex > 1 && line.charAt(charIndex - 1) == '\\') {
                            currentColumn.append(c);
                            //charIndex++;
                        } else if (line.charAt(charIndex + 1) == quoteChar) {
                             /*
                              * An escaped quote (""). Add a single quote, then move the cursor so the next iteration of the
                              * loop will read the character following the escaped quote.
                              */
                             currentColumn.append(c);
                             charIndex++;
                         } else {
                             /*
                              * A single quote ("). Update to NORMAL (but don't save quote), then continue to next character.
                              */
                             state = TokenizerState.NORMAL;
                             quoteScopeStartingLine = -1; // reset ready for next multi-line cell
                         }
                     } else {
                         /*
                          * Just a normal character, delimiter (they don't count in QUOTESCOPE) or space. Add the character,
                          * then continue to next character.
                          */
                         currentColumn.append(c);
                     }
                 }
 
                 charIndex++; // read next char of the line
             }
         }
 
         /**
          * Appends the required number of spaces to the StringBuilder.
          *
          * @param sb
          *            the StringBuilder
          * @param spaces
          *            the required number of spaces to append
          */
         private static void appendSpaces(final StringBuilder sb, final int spaces) {
             for( int i = 0; i < spaces; i++ ) {
                 sb.append(SPACE);
             }
         }
 
         /**
          * {@inheritDoc}
          */
         public String getUntokenizedRow() {
             return currentRow.toString();
         }
     }
 
     protected CsvPreference csvPref;
     private Tokenizer tokenizer;
     protected List<String> row = new ArrayList<String>();
 
     public CSVFileReader(CSVPrepareConfiguration conf, FileWriter writer)
             throws PreparePartsException {
         super(conf, writer);
     }
 
     @Override
     public void configure(Task task) throws PreparePartsException {
         super.configure(task);
 
         // initialize csv preference
         CsvPreference.Builder b = new CsvPreference.Builder(
                 conf.getQuoteChar().quote(),
                 conf.getDelimiterChar(),
                 conf.getNewline().newline());
         csvPref = b.build();
 
         // if conf object doesn't have column names, types, etc,
         // sample method checks those values.
         sample(task);
 
         try {
             tokenizer = new Tokenizer(new InputStreamReader(
                     task.createInputStream(conf.getCompressionType()),
                     conf.getCharsetDecoder()), csvPref);
         } catch (IOException e) {
             LOG.log(Level.SEVERE,
                     String.format("Cannot create CSV file reader [] %s",
                             task.getSource()), e);
             throw new PreparePartsException(e);
         }
         if (conf.hasColumnHeader()) {
             try {
                 // header line is skipped
                 incrementLineNum();
                 tokenizer.readColumns(new ArrayList<String>());
             } catch (IOException e) {
                 LOG.log(Level.SEVERE,
                         String.format("Column header is not read or EOF [line: 1] %s",
                                 task.getSource()), e);
                 throw new PreparePartsException(e);
             }
         }
     }
 
     // TODO FIXME this method is bad design
     private void setColumnNamesWithColumnHeader(Tokenizer tokenizer) throws IOException {
         List<String> sampleRow = new ArrayList<String>();
         if (conf.hasColumnHeader()) {
             tokenizer.readColumns(sampleRow);
             if (columnNames == null || columnNames.length == 0) {
                 columnNames = sampleRow.toArray(new String[0]);
                 conf.setColumnNames(columnNames);
             }
         }
     }
 
     public void sample(Task task) throws PreparePartsException {
         Tokenizer sampleTokenizer = null;
 
         try {
             // create sample reader
             sampleTokenizer = new Tokenizer(new InputStreamReader(
                     task.createInputStream(conf.getCompressionType()),
                     conf.getCharsetDecoder()), csvPref);
 
             // extract column names
             // e.g. 
             // 1) [ "time", "name", "price" ]
             // 2) [ "timestamp", "name", "price" ]
             // 3) [ "name", "price" ]
             setColumnNamesWithColumnHeader(sampleTokenizer);
 
             // get index of 'time' column
             // [ "time", "name", "price" ] as all columns is given,
             // the index is zero.
             int timeColumnIndex = getTimeColumnIndex();
 
             // get index of specified alias time column
             // [ "timestamp", "name", "price" ] as all columns and
             // "timestamp" as alias time column are given, the index is zero.
             //
             // if 'time' column exists in row data, the specified alias
             // time column is ignore.
             int aliasTimeColumnIndex = getAliasTimeColumnIndex(timeColumnIndex);
 
             // if 'time' and the alias columns don't exist, ...
             if (timeColumnIndex < 0 && aliasTimeColumnIndex < 0) {
                 if (conf.getTimeValue() >= 0) {
                 } else {
                     throw new PreparePartsException(
                             "Time column not found. --time-column or --time-value option is required");
                 }
             }
 
             boolean isFirstRow = true;
             List<String> firstRow = new ArrayList<String>();
             final int sampleRowSize = conf.getSampleRowSize();
             TimeColumnSampling[] sampleColumnValues = new TimeColumnSampling[columnNames.length];
             for (int i = 0; i < sampleColumnValues.length; i++) {
                 sampleColumnValues[i] = new TimeColumnSampling(sampleRowSize);
             }
 
             // read some rows
             List<String> sampleRow = new ArrayList<String>();
             for (int i = 0; i < sampleRowSize; i++) {
                 int lineNum = i + 1;
                 if (!isFirstRow && (columnTypes == null || columnTypes.length == 0)) {
                     break;
                 }
 
                 try {
                     sampleTokenizer.readColumns(sampleRow);
                 } catch (IOException e) {
                     LOG.log(Level.SEVERE, String.format(
                             "Anything is not read or EOF [line: %d] %s",
                             lineNum, task.getSource()), e);
                     throw new PreparePartsException(e);
                 }
 
                 if (sampleRow == null || sampleRow.isEmpty()) {
                     break;
                 }
 
                 if (isFirstRow) {
                     firstRow.addAll(sampleRow);
                     isFirstRow = false;
                 }
 
                 if (sampleColumnValues.length != sampleRow.size()) {
                     throw new PreparePartsException(String.format(
                             "The number of columns to be processed (%d) must " +
                             "match the number of column types (%d): check that the " +
                             "number of column types you have defined matches the " +
                             "expected number of columns being read/written [line: %d] %s",
                             sampleRow.size(), sampleColumnValues.length, lineNum, sampleRow));
                 }
 
                 // sampling
                 for (int j = 0; j < sampleColumnValues.length; j++) {
                     sampleColumnValues[j].parse(sampleRow.get(j));
                 }
             }
 
             // initialize types of all columns
             initializeColumnTypes(sampleColumnValues);
 
             // initialize time column value
             setTimeColumnValue(sampleColumnValues, timeColumnIndex, aliasTimeColumnIndex);
 
             initializeConvertedRow();
 
             // check properties of exclude/only columns
             setSkipColumns();
 
             // print first sample row
             JSONFileWriter w = null;
             try {
                 w = new JSONFileWriter(conf);
                 w.setColumnNames(getColumnNames());
                 w.setColumnTypes(getColumnTypes());
                 w.setSkipColumns(getSkipColumns());
                 w.setTimeColumnValue(getTimeColumnValue());
 
                 this.row.addAll(firstRow);
 
                 // convert each column in row
                 convertTypesOfColumns();
                 // write each column value
                 w.next(convertedRow);
                 String ret = w.toJSONString();
                 String msg = null;
                 if (ret != null) {
                     msg = "sample row: " + ret;
                 } else  {
                     msg = "cannot get sample row";
                 }
                 System.out.println(msg);
                 LOG.info(msg);
             } finally {
                 if (w != null) {
                     w.close();
                 }
             }
         } catch (IOException e) {
             LOG.throwing(this.getClass().getName(), "sample", e);
             throw new PreparePartsException(e);
         } finally {
             if (sampleTokenizer != null) {
                 try {
                     sampleTokenizer.close();
                 } catch (IOException e) {
                     LOG.throwing(this.getClass().getName(), "sample", e);
                     throw new PreparePartsException(e);
                 }
             }
         }
     }
 
     @Override
     public boolean readRow() throws IOException, PreparePartsException {
         row.clear();
         try {
             if (!tokenizer.readColumns(row)) {
                 return false;
             }
         } catch (IOException e) {
             throw new PreparePartsException(e);
         }
 
         incrementLineNum();
 
         int rawRowSize = row.size();
         if (rawRowSize != columnTypes.length) {
             throw new PreparePartsException(String.format(
                     "The number of columns to be processed (%d) must " +
                     "match the number of column types (%d): check that the " +
                     "number of column types you have defined matches the " +
                     "expected number of columns being read/written [line: %d]",
                     rawRowSize, columnTypes.length, getLineNum()));
         }
 
         return true;
     }
 
     @Override
     public void convertTypesOfColumns() throws PreparePartsException {
         for (int i = 0; i < this.row.size(); i++) {
             columnTypes[i].convertType(this.row.get(i), convertedRow.getValue(i));
         }
     }
 
     @Override
     public String getCurrentRow() {
         return row.toString();
     }
 
     @Override
     public void close() throws IOException {
         super.close();
 
         if (tokenizer != null) {
             tokenizer.close();
         }
     }
 
 }
