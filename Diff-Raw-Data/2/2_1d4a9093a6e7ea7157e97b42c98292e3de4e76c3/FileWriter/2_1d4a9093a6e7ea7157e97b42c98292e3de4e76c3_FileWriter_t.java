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
 import java.util.Set;
 import java.util.logging.Logger;
 
 import com.treasure_data.bulk_import.Configuration;
 import com.treasure_data.bulk_import.model.AliasTimeColumnValue;
 import com.treasure_data.bulk_import.model.ColumnType;
 import com.treasure_data.bulk_import.model.DoubleColumnValue;
 import com.treasure_data.bulk_import.model.IntColumnValue;
 import com.treasure_data.bulk_import.model.LongColumnValue;
 import com.treasure_data.bulk_import.model.Row;
 import com.treasure_data.bulk_import.model.StringColumnValue;
 import com.treasure_data.bulk_import.model.TimeColumnValue;
 import com.treasure_data.bulk_import.model.TimeValueTimeColumnValue;
 import com.treasure_data.bulk_import.prepare_parts.PrepareConfiguration;
 import com.treasure_data.bulk_import.prepare_parts.PreparePartsException;
 import com.treasure_data.bulk_import.prepare_parts.Task;
 
 public abstract class FileWriter implements Closeable {
 
     private static final Logger LOG = Logger
             .getLogger(FileWriter.class.getName());
 
     protected PrepareConfiguration conf;
     protected Task task;
     protected long rowNum = 0;
 
     protected String[] columnNames;
     protected ColumnType[] columnTypes;
 
     protected Set<Integer> skipColumns;
     protected boolean needAdditionalTimeColumn = false;
     protected TimeColumnValue timeColumnValue;
     protected int timeColumnIndex = -1;
 
     protected FileWriter(PrepareConfiguration conf) {
         this.conf = conf;
     }
 
     public void setColumnNames(String[] columnNames) {
         this.columnNames = columnNames;
     }
 
     public void setColumnTypes(ColumnType[] columnTypes) {
         this.columnTypes = columnTypes;
     }
 
     public void setSkipColumns(Set<Integer> skipColumns) {
         this.skipColumns = skipColumns;
     }
 
     public void setTimeColumnValue(TimeColumnValue timeColumnValue) {
         needAdditionalTimeColumn =
                 timeColumnValue instanceof AliasTimeColumnValue ||
                 timeColumnValue instanceof TimeValueTimeColumnValue;
         if (!needAdditionalTimeColumn) {
             timeColumnIndex = ((TimeColumnValue) timeColumnValue).getIndex();
         }
         this.timeColumnValue = timeColumnValue;
     }
 
     public void configure(Task task)
             throws PreparePartsException {
         this.task = task;
     }
 
     public void next(Row row) throws PreparePartsException {
         int size = row.getValues().length;
 
         // begin writing
         if (needAdditionalTimeColumn) {
             // if the row doesn't have 'time' column, new 'time' column needs
             // to be appended to it.
            writeBeginRow(size - skipColumns.size() + 1);
         } else {
             writeBeginRow(size - skipColumns.size());
         }
 
         // write columns
         for (int i = 0; i < size; i++) {
             if (skipColumns.contains(i)) {
                 continue;
             }
 
             write(columnNames[i]);
             if (i == timeColumnIndex) {
                 timeColumnValue.write(row.getValue(i), this);
             } else {
                 row.getValue(i).write(this);
             }
         }
 
         if (needAdditionalTimeColumn) {
             write(Configuration.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE);
             TimeColumnValue tcValue = timeColumnValue;
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
     public abstract void write(TimeColumnValue filter, StringColumnValue v) throws PreparePartsException;
     public abstract void write(TimeColumnValue filter, IntColumnValue v) throws PreparePartsException;
     public abstract void write(TimeColumnValue filter, LongColumnValue v) throws PreparePartsException;
     public abstract void write(TimeColumnValue filter, DoubleColumnValue v) throws PreparePartsException;
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
 
     // Closeable#close()
     public abstract void close() throws IOException;
 }
