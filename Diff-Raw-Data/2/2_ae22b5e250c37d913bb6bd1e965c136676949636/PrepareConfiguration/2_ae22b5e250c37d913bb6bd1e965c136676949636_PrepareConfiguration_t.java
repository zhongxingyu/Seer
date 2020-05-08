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
 package com.treasure_data.bulk_import.prepare_parts;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CodingErrorAction;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Logger;
 import java.util.zip.GZIPInputStream;
 
 import com.treasure_data.bulk_import.Configuration;
 import com.treasure_data.bulk_import.model.ColumnType;
 import com.treasure_data.bulk_import.reader.CSVFileReader;
 import com.treasure_data.bulk_import.reader.FileReader;
 import com.treasure_data.bulk_import.reader.JSONFileReader;
 import com.treasure_data.bulk_import.reader.MessagePackFileReader;
 import com.treasure_data.bulk_import.reader.MySQLTableReader;
 import com.treasure_data.bulk_import.writer.FileWriter;
 import com.treasure_data.bulk_import.writer.MsgpackGZIPFileWriter;
 
 public class PrepareConfiguration extends Configuration {
 
     public static class Factory {
         public PrepareConfiguration newPrepareConfiguration(Properties props) {
             String formatStr = props.getProperty(
                     Configuration.BI_PREPARE_PARTS_FORMAT,
                     Configuration.BI_PREPARE_PARTS_FORMAT_DEFAULTVALUE);
             PrepareConfiguration.Format format = Format.fromString(formatStr);
             if (format == null) {
                 throw new IllegalArgumentException(String.format(
                         "unsupported format '%s'", formatStr));
             }
             return format.createPrepareConfiguration();
         }
     }
 
     public static enum Format {
         CSV("csv") {
             @Override
             public FileReader<CSVPrepareConfiguration> createFileReader(
                     PrepareConfiguration conf, FileWriter writer)
                     throws PreparePartsException {
                 return new CSVFileReader((CSVPrepareConfiguration) conf, writer);
             }
 
             @Override
             public PrepareConfiguration createPrepareConfiguration() {
                 return new CSVPrepareConfiguration();
             }
         },
         TSV("tsv") {
             @Override
             public FileReader<CSVPrepareConfiguration> createFileReader(
                     PrepareConfiguration conf, FileWriter writer)
                     throws PreparePartsException {
                 return new CSVFileReader((CSVPrepareConfiguration) conf, writer);
             }
 
             @Override
             public PrepareConfiguration createPrepareConfiguration() {
                return new CSVPrepareConfiguration();
             }
         },
         MYSQL("mysql") {
             @Override
             public FileReader<MySQLPrepareConfiguration> createFileReader(
                     PrepareConfiguration conf, FileWriter writer)
                     throws PreparePartsException {
                 return new MySQLTableReader((MySQLPrepareConfiguration) conf,
                         writer);
             }
 
             @Override
             public PrepareConfiguration createPrepareConfiguration() {
                 return new MySQLPrepareConfiguration();
             }
         },
         JSON("json") {
             @Override
             public FileReader<JSONPrepareConfiguration> createFileReader(
                     PrepareConfiguration conf, FileWriter writer)
                     throws PreparePartsException {
                 return new JSONFileReader((JSONPrepareConfiguration) conf,
                         writer);
             }
 
             @Override
             public PrepareConfiguration createPrepareConfiguration() {
                 return new JSONPrepareConfiguration();
             }
         },
         MSGPACK("msgpack") {
             @Override
             public FileReader<MessagePackPrepareConfiguration> createFileReader(
                     PrepareConfiguration conf, FileWriter writer)
                     throws PreparePartsException {
                 return new MessagePackFileReader(
                         (MessagePackPrepareConfiguration) conf, writer);
             }
 
             @Override
             public PrepareConfiguration createPrepareConfiguration() {
                 return new MessagePackPrepareConfiguration();
             }
         };
 
         private String format;
 
         Format(String format) {
             this.format = format;
         }
 
         public String format() {
             return format;
         }
 
         public abstract PrepareConfiguration createPrepareConfiguration();
 
         public FileReader<? extends PrepareConfiguration> createFileReader(
                 PrepareConfiguration conf, FileWriter writer)
                 throws PreparePartsException {
             throw new PreparePartsException(
                     new UnsupportedOperationException("format: " + this));
         }
 
         public static Format fromString(String format) {
             return StringToFormat.get(format);
         }
 
         private static class StringToFormat {
             private static final Map<String, Format> REVERSE_DICTIONARY;
 
             static {
                 Map<String, Format> map = new HashMap<String, Format>();
                 for (Format elem : Format.values()) {
                     map.put(elem.format(), elem);
                 }
                 REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
             }
 
             static Format get(String key) {
                 return REVERSE_DICTIONARY.get(key);
             }
         }
     }
 
     public static enum OutputFormat {
         MSGPACKGZ("msgpackgz") {
             @Override
             public FileWriter createFileWriter(PrepareConfiguration conf) throws PreparePartsException {
                 return new MsgpackGZIPFileWriter(conf);
             }
         };
 
         private String outputFormat;
 
         OutputFormat(String outputFormat) {
             this.outputFormat = outputFormat;
         }
 
         public String outputFormat() {
             return outputFormat;
         }
 
         public FileWriter createFileWriter(PrepareConfiguration conf) throws PreparePartsException {
             throw new PreparePartsException(
                     new UnsupportedOperationException("output format: " + this));
         }
 
         public static OutputFormat fromString(String outputFormat) {
             return StringToOutputFormat.get(outputFormat);
         }
 
         private static class StringToOutputFormat {
             private static final Map<String, OutputFormat> REVERSE_DICTIONARY;
 
             static {
                 Map<String, OutputFormat> map = new HashMap<String, OutputFormat>();
                 for (OutputFormat elem : OutputFormat.values()) {
                     map.put(elem.outputFormat(), elem);
                 }
                 REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
             }
 
             static OutputFormat get(String key) {
                 return REVERSE_DICTIONARY.get(key);
             }
         }
     }
 
     public static enum CompressionType {
         GZIP("gzip") {
             @Override
             public InputStream createInputStream(InputStream in) throws IOException {
                 return new BufferedInputStream(new GZIPInputStream(in));
             }
         }, AUTO("auto") {
             @Override
             public InputStream createInputStream(InputStream in) throws IOException {
                 throw new IOException("unsupported compress type");
             }
         }, NONE("none") {
             @Override
             public InputStream createInputStream(InputStream in) throws IOException {
                 return new BufferedInputStream(in);
             }
         };
 
         private String type;
 
         CompressionType(String type) {
             this.type = type;
         }
 
         public String type() {
             return type;
         }
 
         public abstract InputStream createInputStream(InputStream in) throws IOException;
 
         public static CompressionType fromString(String type) {
             return StringToCompressionType.get(type);
         }
 
         private static class StringToCompressionType {
             private static final Map<String, CompressionType> REVERSE_DICTIONARY;
 
             static {
                 Map<String, CompressionType> map = new HashMap<String, CompressionType>();
                 for (CompressionType elem : CompressionType.values()) {
                     map.put(elem.type(), elem);
                 }
                 REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
             }
 
             static CompressionType get(String key) {
                 return REVERSE_DICTIONARY.get(key);
             }
         }
     }
 
     public static enum ErrorHandling {
         SKIP(Configuration.BI_PREPARE_PARTS_ERROR_HANDLING_DEFAULTVALUE) {
             @Override
             public void handleError(PreparePartsException e)
                     throws PreparePartsException {
                 // ignore
             }
         },
         ABORT("abort") {
             @Override
             public void handleError(PreparePartsException e)
                     throws PreparePartsException {
                 throw e;
             }
         };
 
         private String mode;
 
         ErrorHandling(String mode) {
             this.mode = mode;
         }
 
         public String mode() {
             return mode;
         }
 
         public abstract void handleError(PreparePartsException e)
                 throws PreparePartsException;
 
         public static ErrorHandling fromString(String mode) {
             return StringToErrorHandling.get(mode);
         }
 
         private static class StringToErrorHandling {
             private static final Map<String, ErrorHandling> REVERSE_DICTIONARY;
 
             static {
                 Map<String, ErrorHandling> map = new HashMap<String, ErrorHandling>();
                 for (ErrorHandling elem : ErrorHandling.values()) {
                     map.put(elem.mode(), elem);
                 }
                 REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
             }
 
             static ErrorHandling get(String key) {
                 return REVERSE_DICTIONARY.get(key);
             }
         }
     }
 
     private static final Logger LOG = Logger
             .getLogger(PrepareConfiguration.class.getName());
 
     // FIXME this field is also declared in td-client.Config.
     protected Properties props;
 
     protected Format format;
     protected OutputFormat outputFormat = OutputFormat.MSGPACKGZ;
     protected CompressionType compressionType;
     protected CharsetDecoder charsetDecoder;
     protected int numOfPrepareThreads;
     protected String aliasTimeColumn;
     protected long timeValue = -1;
     protected String timeFormat;
     protected String errorRecordOutputDirName;
     protected ErrorHandling errorHandling;
     protected boolean dryRun = false;
     protected String outputDirName;
     protected int splitSize;
     protected String[] columnNames;
     protected ColumnType[] columnTypes;
     protected String[] excludeColumns;
     protected String[] onlyColumns;
 
     public PrepareConfiguration() {
     }
 
     public void configure(Properties props) {
         this.props = props;
 
         // format
         setFormat();
 
         // output format
         setOutputFormat();
 
         // compression type
         setCompressionType();
 
         // parallel
         setPrepareThreadNum();
 
         // encoding
         setEncoding();
 
         // alias time column
         setAliasTimeColumn();
 
         // time value
         setTimeValue();
 
         // time format
         setTimeFormat();
 
         // output DIR
         setOutputDirName();
 
         // error record output DIR
         setErrorRecordOutputDirName();
 
         // error handling
         setErrorHandling();
 
         // exclude-columns
         setExcludeColumns();
 
         // only-columns
         setOnlyColumns();
 
         // dry-run mode
         setDryRun();
 
         // split size
         setSplitSize();
     }
 
     public void setFormat() {
         String formatStr = props.getProperty(
                 Configuration.BI_PREPARE_PARTS_FORMAT,
                 Configuration.BI_PREPARE_PARTS_FORMAT_DEFAULTVALUE);
         format = Format.fromString(formatStr);
         if (format == null) {
             throw new IllegalArgumentException(String.format(
                     "unsupported format '%s'", formatStr));
         }
     }
 
     public Format getFormat() {
         return format;
     }
 
     public void setOutputFormat() {
         String outputFormatStr = props.getProperty(
                 Configuration.BI_PREPARE_PARTS_OUTPUTFORMAT,
                 Configuration.BI_PREPARE_PARTS_OUTPUTFORMAT_DEFAULTVALUE);
         outputFormat = OutputFormat.fromString(outputFormatStr);
         if (outputFormat == null) {
             throw new IllegalArgumentException(String.format(
                     "unsupported format '%s'", outputFormatStr));
         }
     }
 
     public OutputFormat getOutputFormat() {
         return outputFormat;
     }
 
     public void setCompressionType() {
         String compType = props.getProperty(
                 Configuration.BI_PREPARE_PARTS_COMPRESSION,
                 Configuration.BI_PREPARE_PARTS_COMPRESSION_DEFAULTVALUE);
         compressionType = CompressionType.fromString(compType);
         if (compressionType == null) {
             throw new IllegalArgumentException(String.format(
                     "unsupported compression type: %s", compressionType));
         }
     }
 
     public CompressionType getCompressionType() {
         return compressionType;
     }
 
     public CompressionType checkCompressionType(String fileName) throws PreparePartsException {
         if (getCompressionType() != CompressionType.AUTO) {
             return getCompressionType();
         }
 
         CompressionType[] candidateCompressTypes = new CompressionType[] {
                 CompressionType.GZIP, CompressionType.NONE,
         };
 
         CompressionType compressionType = null;
         for (int i = 0; i < candidateCompressTypes.length; i++) {
             InputStream in = null;
             try {
                 if (candidateCompressTypes[i].equals(CompressionType.GZIP)) {
                     in = CompressionType.GZIP.createInputStream(new FileInputStream(fileName));
                 } else if (candidateCompressTypes[i].equals(CompressionType.NONE)) {
                     in = CompressionType.NONE.createInputStream(new FileInputStream(fileName));
                 } else {
                     throw new PreparePartsException("fatal error");
                 }
                 byte[] b = new byte[2];
                 in.read(b);
 
                 compressionType = candidateCompressTypes[i];
                 break;
             } catch (IOException e) {
                 LOG.fine(String.format("file %s is %s", fileName,
                         e.getMessage()));
             } finally {
                 if (in != null) {
                     try {
                         in.close();
                     } catch (IOException e) {
                         // ignore
                     }
                 }
             }
         }
 
         this.compressionType = compressionType;
         return compressionType;
     }
 
     public void setPrepareThreadNum() {
         String pthreadNum = props.getProperty(BI_PREPARE_PARTS_PARALLEL,
                 BI_PREPARE_PARTS_PARALLEL_DEFAULTVALUE);
         try {
             int n = Integer.parseInt(pthreadNum);
             if (n < 0) {
                 numOfPrepareThreads = 2;
             } else if (n > 9){
                 numOfPrepareThreads = 8;
             } else {
                 numOfPrepareThreads = n;
             }
         } catch (NumberFormatException e) {
             String msg = String.format(
                     "'int' value is required as 'parallel' option e.g. -D%s=5",
                     BI_UPLOAD_PARTS_PARALLEL);
             throw new IllegalArgumentException(msg, e);
         }
     }
 
     public int getNumOfPrepareThreads() {
         return numOfPrepareThreads;
     }
 
     public void setEncoding() {
         String encoding = props.getProperty(Configuration.BI_PREPARE_PARTS_ENCODING,
                 Configuration.BI_PREPARE_PARTS_ENCODING_DEFAULTVALUE);
         try {
             createCharsetDecoder(encoding);
         } catch (Exception e) {
             throw new IllegalArgumentException(e.getMessage());
         }
     }
 
     public void createCharsetDecoder(String encoding) throws Exception {
         charsetDecoder = Charset.forName(encoding).newDecoder()
                 .onMalformedInput(CodingErrorAction.REPORT)
                 .onUnmappableCharacter(CodingErrorAction.REPORT);
     }
 
     public CharsetDecoder getCharsetDecoder() throws PreparePartsException {
         return charsetDecoder;
     }
 
     public void setAliasTimeColumn() {
         aliasTimeColumn = props.getProperty(Configuration.BI_PREPARE_PARTS_TIMECOLUMN);
     }
 
     public String getAliasTimeColumn() {
         return aliasTimeColumn;
     }
 
     public void setTimeValue() {
         String tValue = props.getProperty(Configuration.BI_PREPARE_PARTS_TIMEVALUE);
         if (tValue != null) {
             try {
                 timeValue = Long.parseLong(tValue);
             } catch (NumberFormatException e) {
                 String msg = String.format(
                         "time value is required as long type (unix timestamp) e.g. -D%s=1360141200",
                         Configuration.BI_PREPARE_PARTS_TIMEVALUE);
                 throw new IllegalArgumentException(msg, e);
             }
         }
     }
 
     public long getTimeValue() {
         return timeValue;
     }
 
     public void setTimeFormat() {
         timeFormat = props.getProperty(Configuration.BI_PREPARE_PARTS_TIMEFORMAT);
     }
 
     public ExtStrftime getTimeFormat() {
         return timeFormat == null ? null : new ExtStrftime(timeFormat);
     }
 
     public void setOutputDirName() {
         outputDirName = props.getProperty(Configuration.BI_PREPARE_PARTS_OUTPUTDIR);
 
         File outputDir = null;
         if (outputDirName == null || outputDirName.isEmpty()) {
             outputDir = new File(new File("."), Configuration.BI_PREPARE_PARTS_OUTPUTDIR_DEFAULTVALUE);
             outputDirName = outputDir.getName();
         } else {
             outputDir = new File(outputDirName);
         }
 
         // validate output dir
         if (!outputDir.isDirectory()) {
             if (!outputDir.mkdir()) {
                 throw new IllegalArgumentException(String.format(
                         "Cannot create output directory '%s'", outputDirName));
             }
         }
     }
 
     public void setErrorRecordOutputDirName() {
         errorRecordOutputDirName = props
                 .getProperty(Configuration.BI_PREPARE_PARTS_ERROR_RECORD_OUTPUT);
 
         if (errorRecordOutputDirName == null) {
             return;
         }
 
         // validate output dir
         File errorRecordOutputDir = new File(errorRecordOutputDirName);
         if (!errorRecordOutputDir.isDirectory()) {
             if (!errorRecordOutputDir.mkdir()) {
                 throw new IllegalArgumentException(String.format(
                         "Cannot create error record output directory '%s'",
                         errorRecordOutputDirName));
             }
         }
     }
 
     public String getErrorRecordOutputDirName() {
         return errorRecordOutputDirName;
     }
 
     public void setErrorHandling() {
         String modeStr = props.getProperty(
                 Configuration.BI_PREPARE_PARTS_ERROR_HANDLING,
                 Configuration.BI_PREPARE_PARTS_ERROR_HANDLING_DEFAULTVALUE);
         errorHandling = ErrorHandling.fromString(modeStr);
         if (errorHandling == null) {
             throw new IllegalArgumentException(String.format(
                     "unsupported errorHandling mode '%s'", modeStr));
         }
     }
 
     public ErrorHandling getErrorHandling() {
         return errorHandling;
     }
 
     public void setDryRun() {
         String drun = props.getProperty(Configuration.BI_PREPARE_PARTS_DRYRUN,
                 Configuration.BI_PREPARE_PARTS_DRYRUN_DEFAULTVALUE);
         dryRun = drun != null && drun.equals("true");
     }
 
     public boolean dryRun() {
         return dryRun;
     }
 
     public String getOutputDirName() {
         return outputDirName;
     }
 
     public void setSplitSize() {
         String sSize = props.getProperty(
                 Configuration.BI_PREPARE_PARTS_SPLIT_SIZE,
                 Configuration.BI_PREPARE_PARTS_SPLIT_SIZE_DEFAULTVALUE);
         try {
             splitSize = Integer.parseInt(sSize);
         } catch (NumberFormatException e) {
             String msg = String.format(
                     "split size is required as int type e.g. -D%s=%s",
                     Configuration.BI_PREPARE_PARTS_SPLIT_SIZE,
                     Configuration.BI_PREPARE_PARTS_SPLIT_SIZE_DEFAULTVALUE);
             throw new IllegalArgumentException(msg, e);
         }
     }
 
     public int getSplitSize() {
         return splitSize;
     }
 
     public void setColumnNames() {
         String columns = props.getProperty(
                 Configuration.BI_PREPARE_PARTS_COLUMNS);
         if (columns != null && !columns.isEmpty()) {
             columnNames = columns.split(",");
         } else {
             columnNames = new String[0];
         }
     }
 
     public String[] getColumnNames() {
         return columnNames;
     }
 
     public void setColumnTypes() {
         String types = props.getProperty(Configuration.BI_PREPARE_PARTS_COLUMNTYPES);
         if (types != null && !types.isEmpty()) {
             String[] splited = types.split(",");
             columnTypes = new ColumnType[splited.length];
             for (int i = 0; i < columnTypes.length; i++) {
                 columnTypes[i] = ColumnType.fromString(splited[i].toLowerCase());
             }
         } else {
             columnTypes = new ColumnType[0];
         }
     }
 
     public void setColumnTypes(ColumnType[] columnTypes) {
         this.columnTypes = columnTypes;
     }
 
     public ColumnType[] getColumnTypes() {
         return columnTypes;
     }
 
     public void setExcludeColumns() {
         String excludeColumns = props.getProperty(
                 Configuration.BI_PREPARE_PARTS_EXCLUDE_COLUMNS);
         if (excludeColumns != null && !excludeColumns.isEmpty()) {
             this.excludeColumns = excludeColumns.split(",");
             for (String c : this.excludeColumns) {
                 if (c.equals(Configuration.BI_PREPARE_PARTS_TIMECOLUMN)) {
                     throw new IllegalArgumentException(
                             "'time' column cannot be included in excluded columns");
                 }
             }
         } else {
             this.excludeColumns = new String[0];
         }
     }
 
     public String[] getExcludeColumns() {
         return excludeColumns;
     }
 
     public void setOnlyColumns() {
         String onlyColumns = props.getProperty(Configuration.BI_PREPARE_PARTS_ONLY_COLUMNS);
         if (onlyColumns != null && !onlyColumns.isEmpty()) {
             this.onlyColumns = onlyColumns.split(",");
             for (String oc : this.onlyColumns) {
                 for (String ec : this.excludeColumns) {
                     if (oc.equals(ec)) {
                         throw new IllegalArgumentException(
                                 "'exclude' columns include specified 'only' columns");
                     }
                 }
             }
         } else {
             this.onlyColumns = new String[0];
         }
     }
 
     public String[] getOnlyColumns() {
         return onlyColumns;
     }
 
 }
