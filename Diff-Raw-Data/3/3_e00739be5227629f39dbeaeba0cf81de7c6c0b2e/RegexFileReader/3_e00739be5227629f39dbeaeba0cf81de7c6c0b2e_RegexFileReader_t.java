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
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.treasure_data.bulk_import.prepare.PrepareConfiguration;
 import com.treasure_data.bulk_import.prepare.PreparePartsException;
 import com.treasure_data.bulk_import.prepare.Task;
 import com.treasure_data.bulk_import.writer.FileWriter;
 
 public class RegexFileReader<T extends PrepareConfiguration> extends FixnumColumnsFileReader<T> {
     private static final Logger LOG = Logger.getLogger(RegexFileReader.class
             .getName());
 
     protected String regex;
 
     protected BufferedReader reader;
     protected Pattern pat;
 
     protected String line;
     protected List<String> row = new ArrayList<String>();
 
     public RegexFileReader(T conf, FileWriter writer, String regex)
             throws PreparePartsException {
         super(conf, writer);
         this.regex = regex;
     }
 
     protected void updateColumnNames() {
         throw new UnsupportedOperationException();
     }
 
     protected void updateColumnTypes() {
         throw new UnsupportedOperationException();
     }
 
     protected void updateTimeColumnValue() {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void configure(Task task) throws PreparePartsException {
         super.configure(task);
 
         // column names
         updateColumnNames();
 
         // column types
         updateColumnTypes();
 
         // time column
         updateTimeColumnValue();
 
         initializeConvertedRow();
 
         // check properties of exclude/only columns
         setSkipColumns();
 
         try {
             reader = new BufferedReader(new InputStreamReader(
                    task.createInputStream(conf.getCompressionType()),
                    conf.getCharsetDecoder()));
         } catch (IOException e) {
             throw new PreparePartsException(e);
         }
 
         pat = Pattern.compile(regex);
     }
 
     @Override
     public boolean readRow() throws IOException, PreparePartsException {
         row.clear();
         if ((line = reader.readLine()) == null) {
             return false;
         }
 
         incrementLineNum();
 
         Matcher commonLogMatcher = pat.matcher(line);
 
         if (!commonLogMatcher.matches()) {
             writer.incrementErrorRowNum();
             throw new PreparePartsException(String.format(
                     "line is not matched at apache common log format [line: %d]",
                     getLineNum()));
         }
 
         // extract groups
         for (int i = 1; i < (columnNames.length + 1); i++) {
             row.add(commonLogMatcher.group(i));
         }
 
         int rawRowSize = row.size();
         if (rawRowSize != columnTypes.length) {
             writer.incrementErrorRowNum();
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
