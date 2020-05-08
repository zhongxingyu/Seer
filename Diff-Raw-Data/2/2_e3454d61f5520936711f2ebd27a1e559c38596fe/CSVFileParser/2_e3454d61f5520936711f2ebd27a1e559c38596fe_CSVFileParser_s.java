 //
 // Java Extension to CUI for Treasure Data
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
 package com.treasure_data.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.catalina.util.Strftime;
 import org.supercsv.cellprocessor.CellProcessorAdaptor;
 import org.supercsv.cellprocessor.Optional;
 import org.supercsv.cellprocessor.ParseDate;
 import org.supercsv.cellprocessor.ParseDouble;
 import org.supercsv.cellprocessor.ParseInt;
 import org.supercsv.cellprocessor.ParseLong;
 import org.supercsv.cellprocessor.ift.CellProcessor;
 import org.supercsv.cellprocessor.ift.DateCellProcessor;
 import org.supercsv.cellprocessor.ift.StringCellProcessor;
 import org.supercsv.exception.SuperCsvCellProcessorException;
 import org.supercsv.io.CsvListReader;
 import org.supercsv.io.ICsvListReader;
 import org.supercsv.prefs.CsvPreference;
 import org.supercsv.util.CsvContext;
 
 import com.treasure_data.commands.CommandException;
 import com.treasure_data.commands.Config;
 import com.treasure_data.commands.bulk_import.PreparePartsRequest;
 
 public class CSVFileParser extends FileParser {
     private static final Logger LOG = Logger.getLogger(CSVFileParser.class
             .getName());
 
     private static class CellProcessorGen {
         public CellProcessor[] gen(String[] columnTypes)
                 throws CommandException {
             int len = columnTypes.length;
             List<CellProcessor> cprocs = new ArrayList<CellProcessor>(len);
             for (int i = 0; i < len; i++) {
                 // TODO any more types...
 
                 // TODO parser for time
                 CellProcessor cproc;
                 String type = columnTypes[i];
                 if (type.equals("string")) {
                     cproc = new Optional();
                 } else if (type.equals("int")) {
                     cproc = new ParseInt();
                 } else if (type.equals("long")) {
                     cproc = new ParseLong();
                 } else if (type.equals("double")) {
                     cproc = new ParseDouble();
                 } else {
                     throw new CommandException("Unsupported type: " + type);
                 }
                 cprocs.add(cproc);
             }
             return cprocs.toArray(new CellProcessor[0]);
         }
     }
 
     private static class ExtStrftime extends Strftime {
         public ExtStrftime(String origFormat) {
             super(origFormat);
         }
 
         public SimpleDateFormat getSimpleDateFormat() {
             return simpleDateFormat;
         }
     }
 
     private static class ParseStrftimeDate extends CellProcessorAdaptor
             implements StringCellProcessor {
         private static SimpleDateFormat simpleFormat;
 
         public ParseStrftimeDate(String dateFormat) {
             super();
             if (dateFormat == null) {
                 throw new NullPointerException("dateFormat should not be null");
             }
             simpleFormat = new ExtStrftime(dateFormat).getSimpleDateFormat();
         }
 
         /**
          * {@inheritDoc}
          *
          * @throws SuperCsvCellProcessorException
          *             if value is null, isn't a String, or can't be parsed to a Date
          */
         public Object execute(final Object value, final CsvContext context) {
             validateInputNotNull(value, context);
 
             if (!(value instanceof String)) {
                 throw new SuperCsvCellProcessorException(String.class, value,
                         context, this);
             }
 
             try {
                 Long result = simpleFormat.parse((String) value).getTime() / 1000;
                 return next.execute(result, context);
             } catch (final ParseException e) {
                 throw new SuperCsvCellProcessorException(String.format(
                         "'%s' could not be parsed as a Date", value), context,
                         this, e);
             }
         }
     }
 
     private ICsvListReader reader;
     private int timeIndex = -1;
     private Long timeValue = new Long(-1);
     private int aliasTimeIndex = -1;
     private String[] columnNames;
 
     private String[] columnTypes;
     private CellProcessor[] cprocessors;
 
     public CSVFileParser(PreparePartsRequest request, File file)
             throws CommandException {
         try {
             initReader(request, new FileInputStream(file));
         } catch (FileNotFoundException e) {
             throw new CommandException(e);
         }
     }
 
     public CSVFileParser(PreparePartsRequest request, InputStream in)
             throws CommandException {
         initReader(request, in);
     }
 
     @Override
     public void initReader(PreparePartsRequest request, InputStream in)
             throws CommandException {
         // create reader
         reader = new CsvListReader(new InputStreamReader(in),
                 CsvPreference.STANDARD_PREFERENCE);
 
         // column name e.g. "time,name,price"
         if (request.hasColumnHeader()) {
             try {
                 List<String> columnList = reader.read();
                 columnNames = columnList.toArray(new String[0]);
             } catch (IOException e) {
                 throw new CommandException(e);
             }
         } else {
             columnNames = request.getColumnNames();
         }
 
         String aliasTimeColumnName = request.getAliasTimeColumn();
         if (aliasTimeColumnName != null) {
             for (int i = 0; i < columnNames.length; i++) {
                 if (columnNames[i].equals(aliasTimeColumnName)) {
                     aliasTimeIndex = i;
                     break;
                 }
             }
         }
         for (int i = 0; i < columnNames.length; i++) {
             if (columnNames[i]
                     .equals(Config.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE)) {
                 timeIndex = i;
                 break;
             }
         }
         if (timeIndex < 0) {
             timeValue = request.getTimeValue();
            if (aliasTimeIndex > 0 || timeValue > 0) {
                 timeIndex = columnNames.length;
             } else {
                 throw new CommandException(
                         "Time column not found. --time-column or --time-value option is required");
             }
         }
 
         // "long,string,long"
         columnTypes = request.getColumnTypes();
 
         cprocessors = new CellProcessorGen().gen(columnTypes);
     }
 
     public boolean parseRow(MsgpackGZIPFileWriter w) throws CommandException {
         List<Object> row = null;
         try {
             row = reader.read(cprocessors);
         } catch (Exception e) {
             // catch IOException and SuperCsvCellProcessorException
             e.printStackTrace();
             LOG.severe("Skip row number: " + getRowNum());
             return true;
         }
 
         if (row == null || row.isEmpty()) {
             return false;
         }
 
         // increment row number
         incrRowNum();
 
         if (LOG.isLoggable(Level.FINE)) {
             LOG.fine(String.format("lineNo=%s, rowNo=%s, customerList=%s",
                     reader.getLineNumber(), reader.getRowNumber(),
                     row));
         }
 
         /** DEBUG
         System.out.println(String.format("lineNo=%s, rowNo=%s, customerList=%s",
                 reader.getLineNumber(), reader.getRowNumber(), row));
          */
 
         try {
             int size = row.size();
 
             if (size == timeIndex) {
                 w.writeBeginRow(size + 1);
             } else {
                 w.writeBeginRow(size);
             }
 
             long time = 0;
             for (int i = 0; i < size; i++) {
                 if (i == aliasTimeIndex) {
                     time = (Long) row.get(i);
                 }
 
                 w.write(columnNames[i]);
                 w.write(row.get(i));
             }
 
             if (size == timeIndex) {
                 w.write(Config.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE);
                 if (aliasTimeIndex > 0) {
                     w.write(time);
                 } else {
                     w.write(timeValue);
                 }
             }
 
             w.writeEndRow();
 
             w.incrRowNum();
             return true;
         } catch (Exception e) {
             throw new CommandException(e);
         }
     }
 
     public void close() throws CommandException {
         if (reader != null) {
             try {
                 reader.close();
             } catch (IOException e) {
                 throw new CommandException(e);
             }
         }
     }
 }
