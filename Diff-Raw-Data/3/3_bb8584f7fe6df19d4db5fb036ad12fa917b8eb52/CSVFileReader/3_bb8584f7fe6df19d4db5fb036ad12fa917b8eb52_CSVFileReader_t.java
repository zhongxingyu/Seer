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
 package com.treasure_data.bulk_import.reader;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.supercsv.io.Tokenizer;
 import org.supercsv.prefs.CsvPreference;
 
 import com.treasure_data.bulk_import.Configuration;
 import com.treasure_data.bulk_import.model.AliasTimeColumnValue;
 import com.treasure_data.bulk_import.model.ColumnType;
 import com.treasure_data.bulk_import.model.ColumnSampling;
 import com.treasure_data.bulk_import.model.TimeColumnValue;
 import com.treasure_data.bulk_import.model.TimeValueTimeColumnValue;
 import com.treasure_data.bulk_import.prepare_parts.PrepareConfiguration;
 import com.treasure_data.bulk_import.prepare_parts.PreparePartsException;
 import com.treasure_data.bulk_import.prepare_parts.Task;
 import com.treasure_data.bulk_import.writer.FileWriter;
 import com.treasure_data.bulk_import.writer.JSONFileWriter;
 
 public class CSVFileReader extends FileReader {
     private static final Logger LOG = Logger.getLogger(CSVFileReader.class.getName());
 
     protected CsvPreference csvPref;
     private Tokenizer tokenizer;
 
     public CSVFileReader(PrepareConfiguration conf, FileWriter writer) throws PreparePartsException {
         super(conf, writer);
     }
 
     @Override
     public void configure(Task task) throws PreparePartsException {
         super.configure(task);
 
        // check compression type of the file
        conf.checkCompressionType(task.fileName);

         // initialize csv preference
         csvPref = new CsvPreference.Builder(conf.getQuoteChar(),
                 conf.getDelimiterChar(), conf.getNewline().newline()).build();
 
         // if conf object doesn't have column names, types, etc,
         // sample method checks those values.
         sample(task);
 
         try {
             tokenizer = new Tokenizer(new InputStreamReader(
                     task.createInputStream(conf.getCompressionType()),
                     conf.getCharsetDecoder()), csvPref);
             if (conf.hasColumnHeader()) {
                 // header line is skipped
                 tokenizer.readColumns(new ArrayList<String>());
                 incrementLineNum();
             }
         } catch (IOException e) {
             throw new PreparePartsException(e);
         }
     }
 
     private void sample(Task task) throws PreparePartsException {
         Tokenizer sampleTokenizer = null;
 
         int timeColumnIndex = -1;
         int aliasTimeColumnIndex = -1;
         List<String> row = new ArrayList<String>();
 
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
             if (conf.hasColumnHeader()) {
                 sampleTokenizer.readColumns(row);
                 if (columnNames == null || columnNames.length == 0) {
                     columnNames = row.toArray(new String[0]);
                     conf.setColumnNames(columnNames);
                 }
             }
 
             // get index of 'time' column
             // [ "time", "name", "price" ] as all columns is given,
             // the index is zero.
             for (int i = 0; i < columnNames.length; i++) {
                 if (columnNames[i].equals(
                         Configuration.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE)) {
                     timeColumnIndex = i;
                     break;
                 }
             }
 
             // get index of specified alias time column
             // [ "timestamp", "name", "price" ] as all columns and
             // "timestamp" as alias time column are given, the index is zero.
             //
             // if 'time' column exists in row data, the specified alias
             // time column is ignore.
             if (timeColumnIndex < 0 && conf.getAliasTimeColumn() != null) {
                 for (int i = 0; i < columnNames.length; i++) {
                     if (columnNames[i].equals(conf.getAliasTimeColumn())) {
                         aliasTimeColumnIndex = i;
                         break;
                     }
                 }
             }
 
             // if 'time' and the alias columns don't exist, ...
             if (timeColumnIndex < 0 && aliasTimeColumnIndex < 0) {
                 if (conf.getTimeValue() >= 0) {
                 } else {
                     throw new PreparePartsException(
                             "Time column not found. --time-column or --time-value option is required");
                 }
             }
 
             boolean isFirstRow = false;
             List<String> firstRow = new ArrayList<String>();
             final int sampleRowSize = conf.getSampleRowSize();
             ColumnSampling[] sampleColumnValues = new ColumnSampling[columnNames.length];
             for (int i = 0; i < sampleColumnValues.length; i++) {
                 sampleColumnValues[i] = new ColumnSampling(sampleRowSize);
             }
 
             // read some rows
             for (int i = 0; i < sampleRowSize; i++) {
                 sampleTokenizer.readColumns(row);
 
                 if (row == null || row.isEmpty()) {
                     break;
                 }
 
                 if (!isFirstRow) {
                     firstRow.addAll(row);
                     isFirstRow = true;
                 }
 
                 if (sampleColumnValues.length != row.size()) {
                     throw new PreparePartsException(String.format(
                             "The number of columns to be processed (%d) must " +
                             "match the number of column types (%d): check that the " +
                             "number of column types you have defined matches the " +
                             "expected number of columns being read/written [line: %d]",
                             row.size(), columnTypes.length, i));
                 }
 
                 // sampling
                 for (int j = 0; j < sampleColumnValues.length; j++) {
                     sampleColumnValues[j].parse(row.get(j));
                 }
             }
 
             // initialize types of all columns
             if (columnTypes == null || columnTypes.length == 0) {
                 columnTypes = new ColumnType[columnNames.length];
                 for (int i = 0; i < columnTypes.length; i++) {
                     columnTypes[i] = sampleColumnValues[i].getRank();
                 }
                 conf.setColumnTypes(columnTypes);
             }
 
             // initialize time column value
             if (timeColumnIndex >= 0) {
                 timeColumnValue = new TimeColumnValue(timeColumnIndex,
                         conf.getTimeFormat());
             } else if (aliasTimeColumnIndex >= 0) {
                 timeColumnValue = new AliasTimeColumnValue(
                         aliasTimeColumnIndex, conf.getTimeFormat());
             } else {
                 timeColumnValue = new TimeValueTimeColumnValue(
                         conf.getTimeValue());
             }
 
             initializeConvertedRow(timeColumnValue);
 
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
 
                 rawRow.addAll(firstRow);
 
                 // convert each column in row
                 convertTypesOfColumns();
                 // write each column value
                 w.next(convertedRow);
                 String ret = w.toJSONString();
                 if (ret != null) {
                     LOG.info("sample row: " + ret);
                 } else  {
                     LOG.info("cannot get sample row");
                 }
             } finally {
                 if (w != null) {
                     w.close();
                 }
             }
         } catch (IOException e) {
             throw new PreparePartsException(e);
         } finally {
             if (sampleTokenizer != null) {
                 try {
                     sampleTokenizer.close();
                 } catch (IOException e) {
                     throw new PreparePartsException(e);
                 }
             }
         }
     }
 
     @Override
     public boolean readRow() throws IOException {
         return tokenizer.readColumns(rawRow);
     }
 
     @Override
     public void close() throws IOException {
         super.close();
 
         if (tokenizer != null) {
             tokenizer.close();
         }
     }
 
     void setSkipColumns() {
         String[] excludeColumns = conf.getExcludeColumns();
         String[] onlyColumns = conf.getOnlyColumns();
         for (int i = 0; i < columnNames.length; i++) {
             // check exclude columns
             boolean isExcluded = false;
             for (String excludeColumn : excludeColumns) {
                 if (columnNames[i].equals(excludeColumn)) {
                     isExcluded = true;
                     break;
                 }
             }
 
             if (isExcluded) {
                 skipColumns.add(i);
                 continue;
             }
 
             // check only columns
             if (onlyColumns.length == 0) {
                 continue;
             }
 
             boolean isOnly = false;
             for (String onlyColumn : onlyColumns) {
                 if (columnNames[i].equals(onlyColumn)) {
                     isOnly = true;
                     break;
                 }
             }
 
             if (!isOnly) {
                 skipColumns.add(i);
                 continue; // not needed though,..
             }
         }
     }
 
 }
