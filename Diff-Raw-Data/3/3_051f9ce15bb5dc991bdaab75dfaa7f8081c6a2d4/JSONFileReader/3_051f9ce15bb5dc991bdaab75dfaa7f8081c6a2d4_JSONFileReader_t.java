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
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import com.treasure_data.bulk_import.model.ColumnType;
 import com.treasure_data.bulk_import.model.ColumnValue;
 import com.treasure_data.bulk_import.model.Row;
 import com.treasure_data.bulk_import.prepare.JSONPrepareConfiguration;
 import com.treasure_data.bulk_import.prepare.PreparePartsException;
 import com.treasure_data.bulk_import.prepare.Task;
 import com.treasure_data.bulk_import.writer.FileWriter;
 
 public class JSONFileReader extends NonFixnumColumnsFileReader<JSONPrepareConfiguration> {
     private static final Logger LOG = Logger.getLogger(JSONFileReader.class.getName());
 
     protected BufferedReader reader;
     protected JSONParser parser;
 
     protected String line;
     protected Map<String, Object> row;
 
     public JSONFileReader(JSONPrepareConfiguration conf, FileWriter writer) {
         super(conf, writer);
     }
 
     @Override
     public void configure(Task task) throws PreparePartsException {
         super.configure(task);
 
         try {
             reader = new BufferedReader(new InputStreamReader(
                    task.createInputStream(conf.getCompressionType()),
                    conf.getCharsetDecoder()));
         } catch (IOException e) {
             throw new PreparePartsException(e);
         }
 
         // create parser
         parser = new JSONParser();
     }
 
     @Override
     public void setColumnNames() {
         columnNames = row.keySet().toArray(new String[0]);
     }
 
     @Override
     public void setColumnTypes() {
         columnTypes = new ColumnType[columnNames.length];
         for (int i = 0; i < columnNames.length; i++) {
             Object v = row.get(columnNames[i]);
             columnTypes[i] = toColumnType(v);
         }
     }
 
     @Override
     public void setSkipColumns() {
         super.setSkipColumns();
     }
 
     @Override
     public boolean readRow() throws IOException {
         try {
             line = reader.readLine();
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
 
     @Override
     public String getCurrentRow() {
         return line;
     }
 
     @Override
     public void close() throws IOException {
         super.close();
 
         if (reader != null) {
             reader.close();
         }
     }
 }
