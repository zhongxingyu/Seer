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
 package com.treasure_data.bulk_import.writer;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import com.treasure_data.bulk_import.Row;
 import com.treasure_data.bulk_import.ColumnType;
 import com.treasure_data.bulk_import.Row.TimeColumnValue;
 import com.treasure_data.bulk_import.prepare_parts.PrepareConfiguration;
 import com.treasure_data.bulk_import.prepare_parts.PreparePartsException;
 import com.treasure_data.bulk_import.prepare_parts.PrepareProcessor;
import com.treasure_data.commands.Config;
 
 public abstract class FileWriter implements Closeable {
 
     private static final Logger LOG = Logger
             .getLogger(FileWriter.class.getName());
 
     protected PrepareConfiguration conf;
     protected PrepareProcessor.Task task;
     protected long rowNum = 0;
 
     protected String[] columnNames;
     protected ColumnType[] columnTypes;
 
     protected FileWriter(PrepareConfiguration conf) {
         this.conf = conf;
     }
 
     public void setColumnNames(String[] columnNames) {
         this.columnNames = columnNames;
     }
 
     public void setColumnTypes(ColumnType[] columnTypes) {
         this.columnTypes = columnTypes;
     }
 
     public void configure(PrepareProcessor.Task task)
             throws PreparePartsException {
         this.task = task;
     }
 
     public void next(Row row) throws PreparePartsException {
         int size = row.getValues().length;
 
         // begin writing
         if (row.needAdditionalTimeColumn()) {
             // if the row doesn't have 'time' column, new 'time' column needs
             // to be appended to it.
             writeBeginRow(size + 1);
         } else {
             writeBeginRow(size);
         }
 
         // write columns
         for (int i = 0; i < size; i++) {
             write(columnNames[i]);
             if (i == row.getTimeColumnIndex()) {
                 row.getTimeColumnValue().write(row.getValue(i), this);
             } else {
                 row.getValue(i).write(this);
             }
         }
 
         if (row.needAdditionalTimeColumn()) {
            write(Config.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE);
             TimeColumnValue tcValue = row.getTimeColumnValue();
             tcValue.write(row.getValue(tcValue.getIndex()), this);
         }
 
         // end
         writeEndRow();
     }
 
     public abstract void writeBeginRow(int size) throws PreparePartsException;
     public abstract void writeNil() throws PreparePartsException;
     public abstract void write(String v) throws PreparePartsException;
     public abstract void write(int v) throws PreparePartsException;
     public abstract void write(long v) throws PreparePartsException;
     public abstract void write(double v) throws PreparePartsException;
     public abstract void write(Row.TimeColumnValue filter, Row.StringColumnValue v) throws PreparePartsException;
     public abstract void write(Row.TimeColumnValue filter, Row.IntColumnValue v) throws PreparePartsException;
     public abstract void write(Row.TimeColumnValue filter, Row.LongColumnValue v) throws PreparePartsException;
     public abstract void write(Row.TimeColumnValue filter, Row.DoubleColumnValue v) throws PreparePartsException;
     public abstract void writeEndRow() throws PreparePartsException;
 
     public void resetRowNum() {
         rowNum = 0;
     }
 
     public void incrementRowNum() {
         rowNum++;
     }
 
     public long getRowNum() {
         return rowNum;
     }
 
     public abstract void close() throws IOException;
 
     public void closeSilently() {
         try {
             close();
         } catch (IOException e) {
             LOG.severe(e.getMessage());
         }
     }
 }
