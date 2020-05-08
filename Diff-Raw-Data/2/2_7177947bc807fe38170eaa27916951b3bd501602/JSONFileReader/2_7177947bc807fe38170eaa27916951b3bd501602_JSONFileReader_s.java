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
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import com.treasure_data.bulk_import.Configuration;
 import com.treasure_data.bulk_import.model.AliasTimeColumnValue;
 import com.treasure_data.bulk_import.model.ColumnType;
 import com.treasure_data.bulk_import.model.ColumnValue;
 import com.treasure_data.bulk_import.model.Row;
 import com.treasure_data.bulk_import.model.TimeColumnValue;
 import com.treasure_data.bulk_import.model.TimeValueTimeColumnValue;
 import com.treasure_data.bulk_import.prepare_parts.PrepareConfiguration;
 import com.treasure_data.bulk_import.prepare_parts.PreparePartsException;
 import com.treasure_data.bulk_import.prepare_parts.Task;
 import com.treasure_data.bulk_import.writer.FileWriter;
 
 public class JSONFileReader extends FileReader {
     // TODO need data split
 
     private static final Logger LOG = Logger.getLogger(JSONFileReader.class.getName());
 
     protected BufferedReader reader;
     protected JSONParser parser;
     protected Map<String, Object> row;
 
     protected String aliasTimeColumnName = null;
     protected long timeValue = -1;
 
     public JSONFileReader(PrepareConfiguration conf, FileWriter writer) {
         super(conf, writer);
     }
 
     @Override
     public void configure(Task task) throws PreparePartsException {
         super.configure(task);
 
         // check compression type of the file
         conf.checkCompressionType(task.fileName);
 
         if (conf.getAliasTimeColumn() != null &&
                 !conf.getAliasTimeColumn().equals(Configuration.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE)) {
             aliasTimeColumnName = conf.getAliasTimeColumn();
         }
 
         timeValue = conf.getTimeValue();
 
         try {
             reader = new BufferedReader(new InputStreamReader(
                     task.createInputStream(conf.getCompressionType())));
         } catch (IOException e) {
             throw new PreparePartsException(e);
         }
 
         columnNames = new String[0];
         columnTypes = new ColumnType[0];
         skipColumns = new HashSet<String>();
         timeColumnValue = new TimeColumnValue(-1, null);
 
         // create parser
         parser = new JSONParser();
     }
 
     public void setColumnNames() {
         columnNames = row.keySet().toArray(new String[0]);
     }
 
     public void setColumnTypes() {
         columnTypes = new ColumnType[columnNames.length];
         for (int i = 0; i < columnNames.length; i++) {
             Object v = row.get(columnNames[i]);
             columnTypes[i] = toColumnType(v);
         }
     }
 
     private ColumnType toColumnType(Object value) {
         if (value instanceof Integer) {
             return ColumnType.INT;
         } else if (value instanceof Double) {
             return ColumnType.DOUBLE;
         } else if (value instanceof String) {
             return ColumnType.STRING;
         } else if (value instanceof Long) {
             return ColumnType.LONG;
         } else {
             throw new UnsupportedOperationException("During toColumnType() execution");
         }
     }
 
     @Override
     public void setSkipColumns() {
         // FIXME need refactoring
         skipColumns.clear();
         super.setSkipColumns();
     }
 
     public void setTimeColumnValue() throws PreparePartsException {
         int timeColumnIndex = -1;
         int aliasTimeColumnIndex = -1;
         for (int i = 0; i < columnNames.length; i++) {
             if (columnNames[i].equals(Configuration.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE)) {
                 timeColumnIndex = i;
                 break;
             }
             if (aliasTimeColumnName != null && columnNames[i].equals(aliasTimeColumnName)) {
                 aliasTimeColumnIndex = i;
             }
         }
 
         if (timeColumnIndex >= 0) {
             timeColumnValue = new TimeColumnValue(timeColumnIndex, null);
         } else if (aliasTimeColumnIndex >= 0) {
            timeColumnValue = new AliasTimeColumnValue(timeColumnIndex, null);
         } else if (timeValue >= 0) {
             timeColumnValue = new TimeValueTimeColumnValue(timeValue);
         } else {
             // TODO should change message more user-friendly
             throw new PreparePartsException("the row doesn't have time column");
         }
     }
 
     @Override
     public boolean next() throws PreparePartsException {
         incrementLineNum();
         try {
             if (!readRow()) {
                 return false;
             }
 
             setColumnNames();
             writer.setColumnNames(getColumnNames());
             setColumnTypes();
             writer.setColumnTypes(getColumnTypes());
             setSkipColumns();
             writer.setColumnTypes(getColumnTypes());
             setTimeColumnValue();
             writer.setTimeColumnValue(getTimeColumnValue());
 
             // convert each column in row
             convertTypesOfColumns();
 
             // write each column value
             writer.next(convertedRow);
 
             writer.incrementRowNum();
         } catch (IOException e) {
             // if reader throw I/O error, parseRow throws PreparePartsException.
             LOG.throwing("JSONFileParser", "parseRow", e);
             throw new PreparePartsException(e);
         } catch (PreparePartsException e) {
             // TODO the row data should be written to error rows file
             LOG.warning(e.getMessage());
         }
         return true;
     }
 
     @Override
     public boolean readRow() throws IOException {
         try {
             String line = reader.readLine();
             if (line == null) {
                 return false;
             }
             row = (Map<String, Object>) parser.parse(line);
             return row != null;
         } catch (ParseException e) {
             throw new IOException(e);
         }
     }
 
     @Override
     public void convertTypesOfColumns() throws PreparePartsException {
         ColumnValue[] columnValues = new ColumnValue[columnNames.length];
         for (int i = 0; i < columnNames.length; i++) {
             columnValues[i] = columnTypes[i].createColumnValue();
             columnTypes[i].setColumnValue(row.get(columnNames[i]), columnValues[i]);
         }
 
         convertedRow = new Row(columnValues);
     }
 
 }
