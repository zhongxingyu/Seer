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
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.treasure_data.td_import.Configuration;
 import com.treasure_data.td_import.model.AliasTimeColumnValue;
 import com.treasure_data.td_import.model.ColumnType;
 import com.treasure_data.td_import.model.TimeColumnValue;
 import com.treasure_data.td_import.model.TimeValueTimeColumnValue;
 import com.treasure_data.td_import.prepare.PrepareConfiguration;
 import com.treasure_data.td_import.prepare.PreparePartsException;
 import com.treasure_data.td_import.prepare.Task;
 import com.treasure_data.td_import.writer.FileWriter;
 
 public abstract class NonFixnumColumnsFileReader<T extends PrepareConfiguration> extends FileReader<T> {
     private static final Logger LOG = Logger.getLogger(NonFixnumColumnsFileReader.class.getName());
 
     protected String aliasTimeColumnName = null;
 
     public NonFixnumColumnsFileReader(T conf, FileWriter writer) {
         super(conf, writer);
     }
 
     @Override
     public void configure(Task task) throws PreparePartsException {
         super.configure(task);
 
         if (conf.getAliasTimeColumn() != null &&
                 !conf.getAliasTimeColumn().equals(Configuration.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE)) {
             aliasTimeColumnName = conf.getAliasTimeColumn();
         }
 
         columnNames = new String[0];
         columnTypes = new ColumnType[0];
         skipColumns = new HashSet<String>();
         timeColumnValue = new TimeColumnValue(-1, null);
     }
 
     public void setColumnNames() {
         throw new UnsupportedOperationException();
     }
 
     public void setColumnTypes() {
         throw new UnsupportedOperationException();
     }
 
     public ColumnType toColumnType(Object value) {
         if (value instanceof Integer) {
             return ColumnType.INT;
         } else if (value instanceof Double) {
             return ColumnType.DOUBLE;
         } else if (value instanceof String) {
             return ColumnType.STRING;
         } else if (value instanceof Long) {
             return ColumnType.LONG;
         } else if (value instanceof List) {
             return ColumnType.ARRAY;
         } else if (value instanceof Map) {
             return ColumnType.MAP;
         } else {
             throw new UnsupportedOperationException("During toColumnType() execution");
         }
     }
 
     @Override
     public void setSkipColumns() {
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
             timeColumnValue = new TimeColumnValue(timeColumnIndex, conf.getTimeFormat());
         } else if (aliasTimeColumnIndex >= 0) {
             timeColumnValue = new AliasTimeColumnValue(aliasTimeColumnIndex, conf.getTimeFormat());
         } else if (conf.getTimeValue() >= 0) {
             timeColumnValue = new TimeValueTimeColumnValue(conf.getTimeValue());
         } else {
             // TODO should change message more user-friendly
             throw new PreparePartsException("the row doesn't have time column");
         }
     }
 
     @Override
     public boolean next() throws PreparePartsException {
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
             String msg = String.format("Cannot read raw data: line %d in %s", lineNum, source);
             LOG.log(Level.WARNING, msg, e);
             throw new PreparePartsException(e);
         } catch (PreparePartsException e) {
             writer.incrementErrorRowNum();
 
            // the untokenized raw row is written to error rows file
            writeErrorRecord(getCurrentRow());
             // the row data should be written to error rows file
             String msg = String.format("line %d in %s: %s", lineNum, source, getCurrentRow());
             LOG.log(Level.WARNING, msg, e);
             handleError(e);
         }
         return true;
     }
 
     @Override
     public boolean readRow() throws IOException {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void convertTypesOfColumns() throws PreparePartsException {
         throw new UnsupportedOperationException();
     }
 
 }
