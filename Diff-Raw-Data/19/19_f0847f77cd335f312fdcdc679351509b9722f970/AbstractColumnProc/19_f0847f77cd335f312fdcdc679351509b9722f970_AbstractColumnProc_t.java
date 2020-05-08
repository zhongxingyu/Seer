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
 package com.treasure_data.bulk_import.prepare_parts.proc;
 
 import java.util.logging.Logger;
 
 import org.supercsv.cellprocessor.ift.CellProcessor;
 import org.supercsv.util.CsvContext;
 
 import com.treasure_data.bulk_import.prepare_parts.PreparePartsException;
 
 public abstract class AbstractColumnProc implements ColumnProc, CellProcessor {
 
     private static final Logger LOG = Logger.getLogger(
             AbstractColumnProc.class.getName());
 
     protected int index;
     protected String columnName;
     protected com.treasure_data.bulk_import.prepare_parts.writer.FileWriter writer;
 
     protected AbstractColumnProc(int index, String columnName,
             com.treasure_data.bulk_import.prepare_parts.writer.FileWriter writer) {
         this.index = index;
         if (columnName == null) {
             throw new NullPointerException("column name is null.");
         }
         this.columnName = columnName;
         this.writer = writer;
     }
 
     public int getIndex() {
         return index;
     }
 
     public String getColumnName() {
         return columnName;
     }
 
     public Object execute(final Object value) {
         return executeKeyValue(value);
     }
 
     public Object execute(final Object value, final CsvContext context) {
         return executeKeyValue(value);
     }
 
     public Object executeKeyValue(final Object value) {
         LOG.finer(String.format("index=%d, column=%s, value=%s",
                 index, columnName, value));
         try {
             executeKey();
 
             if (value == null) {
                 writer.writeNil();
                 return null;
             }
 
             return executeValue(value);
         } catch (PreparePartsException e) {
            throw new RuntimeException(e);
         }
     }
 
     protected void executeKey() throws PreparePartsException {
         writer.writeString(columnName);
     }
 
     protected abstract Object executeValue(final Object value)
             throws PreparePartsException;
 }
