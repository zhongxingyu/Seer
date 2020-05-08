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
 package com.treasure_data.file;
 
 import static com.treasure_data.commands.bulk_import.CSVPreparePartsRequest.ColumnType.INT;
 import static com.treasure_data.commands.bulk_import.CSVPreparePartsRequest.ColumnType.LONG;
 import static com.treasure_data.commands.bulk_import.CSVPreparePartsRequest.ColumnType.DOUBLE;
 import static com.treasure_data.commands.bulk_import.CSVPreparePartsRequest.ColumnType.STRING;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.CharsetDecoder;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.catalina.util.Strftime;
 import org.json.simple.JSONValue;
 import org.supercsv.cellprocessor.CellProcessorAdaptor;
 import org.supercsv.cellprocessor.ConvertNullTo;
 import org.supercsv.cellprocessor.Optional;
 import org.supercsv.cellprocessor.ParseDouble;
 import org.supercsv.cellprocessor.ParseInt;
 import org.supercsv.cellprocessor.ParseLong;
 import org.supercsv.cellprocessor.ift.CellProcessor;
 import org.supercsv.cellprocessor.ift.StringCellProcessor;
 import org.supercsv.exception.SuperCsvCellProcessorException;
 import org.supercsv.io.CsvListReader;
 import org.supercsv.io.ICsvListReader;
 import org.supercsv.prefs.CsvPreference;
 import org.supercsv.util.CsvContext;
 
 import com.treasure_data.commands.CommandException;
 import com.treasure_data.commands.Config;
 import com.treasure_data.commands.bulk_import.CSVPreparePartsRequest;
 import com.treasure_data.commands.bulk_import.CSVPreparePartsRequest.ColumnType;
 import com.treasure_data.commands.bulk_import.PreparePartsResult;
 
 public class CSVFileParser extends
         FileParser<CSVPreparePartsRequest, PreparePartsResult> {
     private static final Logger LOG = Logger.getLogger(CSVFileParser.class.getName());
 
     static class CellProcessorGen {
         public CellProcessor[] genForSampleReader(String[] columnNames, String[] typeHints,
                 int sampleRowSize, int sampleHintScore) throws CommandException {
             TypeSuggestionProcessor[] cprocs = new TypeSuggestionProcessor[columnNames.length];
             for (int i = 0; i < cprocs.length; i++) {
                 cprocs[i] = new TypeSuggestionProcessor(sampleRowSize, sampleHintScore);
                 if (typeHints.length != 0) {
                     cprocs[i].addHint(typeHints[i]);
                 }
             }
             return cprocs;
         }
 
         public CellProcessor[] gen(CSVPreparePartsRequest.ColumnType[] columnTypes)
                 throws CommandException {
             int len = columnTypes.length;
             List<CellProcessor> cprocs = new ArrayList<CellProcessor>(len);
             for (int i = 0; i < len; i++) {
                 CellProcessor cproc;
                 switch (columnTypes[i]) { // override 'optional' ?
                 case INT:
                     // TODO optimizable as new converter
                     cproc = new ConvertNullTo(null, new ParseInt());
                     break;
                 case LONG:
                     // TODO optimizable as new converter
                     cproc = new ConvertNullTo(null, new ParseLong());
                     break;
                 case DOUBLE:
                     // TODO optimizable as new converter
                     cproc = new ConvertNullTo(null, new ParseDouble());
                     break;
                 case STRING:
                     // TODO optimizable as new converter
                     cproc = new Optional();
                     break;
                 default:
                     String msg = String.format("unsupported type: %s",
                             columnTypes[i]);
                     throw new CommandException(msg);
                 }
                 cprocs.add(cproc);
             }
             return cprocs.toArray(new CellProcessor[0]);
         }
     }
 
     static class TypeSuggestionProcessor extends CellProcessorAdaptor {
         private int[] scores = new int[] { 0, 0, 0, 0 };
         private int rowSize;
         private int hintScore;
 
         TypeSuggestionProcessor(int rowSize, int hintScore) {
             this.rowSize = rowSize;
             this.hintScore = hintScore;
         }
 
         void addHint(String typeHint) throws CommandException {
             if (typeHint == null) {
                 throw new NullPointerException("type hint is null.");
             }
 
             CSVPreparePartsRequest.ColumnType type = ColumnType.fromString(typeHint);
             if (type == null) { // fatal error
                 throw new CommandException("unsupported type: " + typeHint);
             }
 
             switch (type) {
             case INT:
                 scores[INT.index()] += hintScore;
                 break;
             case LONG:
                 scores[LONG.index()] += hintScore;
                 break;
             case DOUBLE:
                 scores[DOUBLE.index()] += hintScore;
                 break;
             case STRING:
                 scores[STRING.index()] += hintScore;
                 break;
             default:
                 throw new CommandException("fatal error");
             }
         }
 
         ColumnType getSuggestedType() {
             int max = -rowSize;
             int maxIndex = 0;
             for (int i = 0; i < scores.length; i++) {
                 if (max < scores[i]) {
                     max = scores[i];
                     maxIndex = i;
                 }
             }
             return ColumnType.fromInt(maxIndex);
         }
 
         void printScores() {
             for (int i = 0; i < scores.length; i++) {
                 System.out.println(scores[i]);
             }
         }
 
         int getScore(ColumnType type) {
             int i = type.index();
             if (i < 0 || i >= 4) {
                 throw new ArrayIndexOutOfBoundsException(i);
             }
             return scores[i];
         }
 
         @Override
         public Object execute(Object value, CsvContext context) {
             if (value == null) {
                 // any score are not changed
                 return null;
             }
 
             Object result = null;
 
             // value looks like String object?
             if (value instanceof String) {
                 scores[STRING.index()] += 1;
                 result = (String) value;
             }
 
             // value looks like Double object?
             if (value instanceof Double) {
                 result = (Double) value;
                 scores[DOUBLE.index()] += 1;
             } else if (value instanceof String) {
                 try {
                     result = Double.parseDouble((String) value);
                     scores[DOUBLE.index()] += 1;
                 } catch (NumberFormatException e) {
                     // ignore
                 }
             }
 
 
             if (value instanceof Long) {
                 result = (Long) value;
                 scores[LONG.index()] += 1;
             } else if (value instanceof String) {
                 try {
                     result = Long.parseLong((String) value);
                     scores[LONG.index()] += 1;
                 } catch (NumberFormatException e) {
                     // ignore
                 }
             }
 
             // value looks like Integer object?
             if (value instanceof Integer) {
                 result = (Integer) value;
                 scores[INT.index()] += 1;
             } else if (value instanceof String) {
                 try {
                     result = Integer.parseInt((String) value);
                     scores[INT.index()] += 1;
                 } catch (NumberFormatException e) {
                     // ignore
                 }
             }
 
             return next.execute(result, context);
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
     private CsvPreference csvPref;
 
     private int timeIndex = -1;
     private Long timeValue = new Long(-1);
     private int aliasTimeIndex = -1;
     private String[] allColumnNames;
     private List<Integer> extractedColumnIndexes;
 
     private ColumnType[] allSuggestedColumnTypes;
     private CellProcessor[] cprocessors;
 
     public CSVFileParser(CSVPreparePartsRequest request) throws CommandException {
         super(request);
     }
 
     @Override
     public void initParser(final CharsetDecoder decoder, InputStream in)
             throws CommandException {
         // CSV preference
         csvPref = new CsvPreference.Builder('"', request.getDelimiterChar(),
                 request.getNewline().newline()).build();
 
         // create sample reader
         CsvListReader sampleReader = new CsvListReader(new InputStreamReader(
                 in, decoder), csvPref);
 
         try {
             // extract all column names
             // e.g. new String[] { "time", "name", "price" }
             // e.g. new String[] { "timestamp", "name", "price" }
             if (request.hasColumnHeader()) {
                 List<String> columnList = sampleReader.read();
                 allColumnNames = columnList.toArray(new String[0]);
             } else {
                 allColumnNames = request.getColumnNames();
             }
 
             // get index of specified alias time column
             // new String[] { "timestamp", "name", "price" } as all columns and
             // "timestamp" as alias time column are given, the index is zero.
             String aliasTimeColumnName = request.getAliasTimeColumn();
             if (aliasTimeColumnName != null) {
                 for (int i = 0; i < allColumnNames.length; i++) {
                     if (allColumnNames[i].equals(aliasTimeColumnName)) {
                         aliasTimeIndex = i;
                         break;
                     }
                 }
             }
             // get index of 'time' column
             // new String[] { "time", "name", "price" } as all columns is given,
             // the index is zero.
             for (int i = 0; i < allColumnNames.length; i++) {
                 if (allColumnNames[i].equals(
                         Config.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE)) {
                     timeIndex = i;
                     break;
                 }
             }
             if (timeIndex < 0) {
                 // if 'time' column is not included in all columns, then...
                 timeValue = request.getTimeValue();
                 if (aliasTimeIndex >= 0 || timeValue > 0) {
                     // 'time' column is appended to all columns (last elem) 
                     timeIndex = allColumnNames.length;
                 } else {
                     throw new CommandException(
                             "Time column not found. --time-column or --time-value option is required");
                 }
             }
 
             extractedColumnIndexes = new ArrayList<Integer>();
             String[] onlyColumns = request.getOnlyColumns();
             String[] excludeColumns = request.getExcludeColumns();
             for (int i = 0; i < allColumnNames.length; i++) {
                 String cname = allColumnNames[i];
 
                 // column is included in exclude-columns?
                 if (excludeColumns.length != 0) {
                     boolean isExcludeColumn = false;
                     for (int j = 0; j < excludeColumns.length; j++) {
                         if (cname.equals(excludeColumns[j])) {
                             isExcludeColumn = true;
                             break;
                         }
                     }
                     if (isExcludeColumn) {
                         continue;
                     }
                 }
 
                 // column is included in only-columns?
                 if (onlyColumns.length == 0) {
                     extractedColumnIndexes.add(i);
                     continue;
                 } else {
                     boolean isOnlyColumn = false;
                     for (int j = 0; j < onlyColumns.length; j++) {
                         if (cname.equals(onlyColumns[j])) {
                             isOnlyColumn = true;
                             break;
                         }
                     }
                     if (isOnlyColumn) {
                         extractedColumnIndexes.add(i);
                     }
                 }
             }
 
             // new String[] { "long", "string", "long" }
             String[] columnTypeHints = request.getColumnTypeHints();
             int columnTypeHintSize = columnTypeHints.length;
             if (columnTypeHintSize != 0 && columnTypeHintSize != allColumnNames.length) {
                 throw new CommandException(
                         "mismatched between size of specified column types and size of columns");
             }
 
             CellProcessor[] cprocs = new CellProcessorGen().genForSampleReader(
                     allColumnNames, columnTypeHints, request.getSampleRowSize(),
                     request.getSampleHintScore());
 
             List<Object> firstRow = null;
             boolean isFirstRow = false;
             for (int i = 0; i < request.getSampleRowSize(); i++) {
                 List<Object> row = sampleReader.read(cprocs);
                 if (!isFirstRow) {
                     firstRow = row;
                     isFirstRow = true;
                 }
 
                 if (row == null || row.isEmpty()) {
                     break;
                 }
             }
 
             allSuggestedColumnTypes = new ColumnType[cprocs.length];
             for (int i = 0; i < cprocs.length; i++) {
                 allSuggestedColumnTypes[i] = ((TypeSuggestionProcessor) cprocs[i])
                         .getSuggestedType();
             }
 
             // print sample row
             if (firstRow != null) {
                 JSONFileWriter w = new JSONFileWriter(request);
                 parseList(w, firstRow);
                 String ret = JSONValue.toJSONString(w.getRecord());
                 LOG.info("sample row: " + ret);
             } else {
                 LOG.info("sample row is null");
             }
         } catch (IOException e) {
             throw new CommandException(e);
         } finally {
             if (sampleReader != null) {
                 try {
                     sampleReader.close();
                 } catch (IOException e) {
                     // ignore
                 }
             }
         }
     }
 
     @Override
     public void startParsing(final CharsetDecoder decoder, InputStream in)
             throws CommandException {
         // create reader
         reader = new CsvListReader(new InputStreamReader(in, decoder), csvPref);
         if (request.hasColumnHeader()) {
             // header line is skipped
             try {
                 reader.read();
             } catch (IOException e) {
                 throw new CommandException(e);
             }
         }
 
         // create cell processors
         cprocessors = new CellProcessorGen().gen(allSuggestedColumnTypes);
     }
 
     @Override
     public boolean parseRow(
             @SuppressWarnings("rawtypes") com.treasure_data.file.FileWriter w)
             throws CommandException {
         List<Object> row = null;
         try {
             row = reader.read(cprocessors);
         } catch (Exception e) {
             // catch IOException and SuperCsvCellProcessorException
             e.printStackTrace();
 
             // TODO
             // TODO
             // TODO and parsent-encoded row?
             String msg = String.format("reason: %s, line: %d",
                     e.getMessage(), getRowNum());
             writeErrorRecord(msg);
 
             LOG.warning("Skip row number: " + getRowNum());
             return true;
         }
 
         if (row == null || row.isEmpty()) {
             return false;
         }
 
         // increment row number
         incrRowNum();
 
         return parseList(w, row);
     }
 
     private boolean parseList(
             @SuppressWarnings("rawtypes") com.treasure_data.file.FileWriter w,
             List<Object> row) throws CommandException {
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
             int allSize = row.size();
 
             if (allSize == timeIndex) {
                 w.writeBeginRow(extractedColumnIndexes.size() + 1);
             } else {
                 w.writeBeginRow(extractedColumnIndexes.size());
             }
 
             long time = 0;
             for (int i = 0; i < allSize; i++) {
                 if (i == aliasTimeIndex) {
                    time = ((Number) row.get(i)).longValue();
                 }
 
                 // i is included in extractedColumnIndexes?
                 boolean included = false;
                 for (Integer j : extractedColumnIndexes) {
                     if (i == j) { // TODO optimize
                         included = true;
                         break;
                     }
                 }
 
                 // write extracted data with writer
                 if (included) {
                     w.write(allColumnNames[i]);
                     w.write(row.get(i));
                 }
             }
 
             if (allSize == timeIndex) {
                 w.write(Config.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE);
                 if (aliasTimeIndex >= 0) {
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
