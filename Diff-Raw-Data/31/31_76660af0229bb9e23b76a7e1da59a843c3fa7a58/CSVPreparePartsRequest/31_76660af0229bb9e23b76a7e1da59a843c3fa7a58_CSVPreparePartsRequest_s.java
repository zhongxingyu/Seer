 package com.treasure_data.commands.bulk_import;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import com.treasure_data.commands.CommandException;
 import com.treasure_data.commands.Config;
 
 public class CSVPreparePartsRequest extends PreparePartsRequest {
     public static enum NewLine {
         CR("\r"), LF("\n"), CRLF("\r\n");
 
         private String newline;
 
         NewLine(String newline) {
             this.newline = newline;
         }
 
         public String newline() {
             return newline;
         }
     }
 
     public static enum ColumnType {
         INT("int", 0), LONG("long", 1), DOUBLE("double", 2), STRING("string", 3);
 
         private String type;
 
         private int index;
 
         ColumnType(String type, int index) {
             this.type = type;
             this.index = index;
         }
 
         public String type() {
             return type;
         }
 
         public int index() {
             return index;
         }
 
         public static ColumnType fromString(String type) {
             return StringToColumnType.get(type);
         }
 
         public static ColumnType fromInt(int index) {
             return IntToColumnType.get(index);
         }
 
         private static class StringToColumnType {
             private static final Map<String, ColumnType> REVERSE_DICTIONARY;
 
             static {
                 Map<String, ColumnType> map = new HashMap<String, ColumnType>();
                 for (ColumnType elem : ColumnType.values()) {
                     map.put(elem.type, elem);
                 }
                 REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
             }
 
             static ColumnType get(String key) {
                 return REVERSE_DICTIONARY.get(key);
             }
         }
 
         private static class IntToColumnType {
             private static final Map<Integer, ColumnType> REVERSE_DICTIONARY;
 
             static {
                 Map<Integer, ColumnType> map = new HashMap<Integer, ColumnType>();
                 for (ColumnType elem : ColumnType.values()) {
                     map.put(elem.index, elem);
                 }
                 REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
             }
 
             static ColumnType get(Integer index) {
                 return REVERSE_DICTIONARY.get(index);
             }
         }
     }
 
     private static final Logger LOG = Logger
             .getLogger(CSVPreparePartsRequest.class.getName());
 
     protected char delimiterChar;
     protected NewLine newline;
     protected String[] columnNames;
     protected String[] columnTypeHints;
     protected boolean hasColumnHeader;
     protected String typeErrorMode;
     protected String[] excludeColumns;
     protected String[] onlyColumns;
 
     public CSVPreparePartsRequest() throws CommandException {
         super();
     }
 
     public CSVPreparePartsRequest(Format format, String[] fileNames,
             Properties props) throws CommandException {
         super(format, fileNames, props);
     }
 
     @Override
     public void setOptions(Properties props) throws CommandException {
         super.setOptions(props);
 
         // delimiter
         if (format.equals(PreparePartsRequest.Format.CSV)) {
             delimiterChar = props.getProperty(
                     Config.BI_PREPARE_PARTS_DELIMITER,
                     Config.BI_PREPARE_PARTS_DELIMITER_CSV_DEFAULTVALUE).charAt(
                     0);
         } else if (format.equals(PreparePartsRequest.Format.TSV)) {
             delimiterChar = props.getProperty(
                     Config.BI_PREPARE_PARTS_DELIMITER,
                     Config.BI_PREPARE_PARTS_DELIMITER_TSV_DEFAULTVALUE).charAt(
                     0);
         } else {
             // fatal error. i mean here might be not executed
             throw new CommandException("unsupported format: " + format);
         }
 
         // newline
         String nLine = props.getProperty(Config.BI_PREPARE_PARTS_NEWLINE,
                 Config.BI_PREPARE_PARTS_NEWLINE_DEFAULTVALUE);
        newline = NewLine.valueOf(nLine);
        if (newline == null) {
            throw new CommandException("unsupported newline char: " + nLine);
         }
 
         // column header
         String columnHeader = props.getProperty(
                 Config.BI_PREPARE_PARTS_COLUMNHEADER);
         if (columnHeader == null || !columnHeader.equals("true")) {
             // columns
             String columns = props.getProperty(
                     Config.BI_PREPARE_PARTS_COLUMNS);
             if (columns != null && !columns.isEmpty()) {
                 columnNames = columns.split(",");
             } else {
                 throw new CommandException("Column names not set");
             }
             hasColumnHeader = false;
         } else {
             hasColumnHeader = true;
         }
 
         // column types
         String cTypeHints = props.getProperty(
                 Config.BI_PREPARE_PARTS_COLUMNTYPES);
         if (cTypeHints != null && !cTypeHints.isEmpty()) {
             columnTypeHints = cTypeHints.split(",");
         } else {
             columnTypeHints = new String[0];
         }
 
         // type-conversion-error
         typeErrorMode = props.getProperty(
                 Config.BI_PREPARE_PARTS_TYPE_CONVERSION_ERROR,
                 Config.BI_PREPARE_PARTS_TYPE_CONVERSION_ERROR_DEFAULTVALUE);
 
         // exclude-columns
         String eColumns = props.getProperty(
                 Config.BI_PREPARE_PARTS_EXCLUDE_COLUMNS);
         if (eColumns != null && !eColumns.isEmpty()) {
             excludeColumns = eColumns.split(",");
         } else {
             excludeColumns = new String[0];
         }
 
         // only-columns
         String oColumns = props.getProperty(
                 Config.BI_PREPARE_PARTS_ONLY_COLUMNS);
         if (oColumns != null && !oColumns.isEmpty()) {
             onlyColumns = oColumns.split(",");
         } else {
             onlyColumns = new String[0];
         }
     }
 
    void setDelimiterChar(char c) {
         this.delimiterChar = c;
     }
 
     public char getDelimiterChar() {
         return delimiterChar;
     }
 
    void setNewLine(NewLine newline) {
         this.newline = newline;
     }
 
     public NewLine getNewline() {
         return newline;
     }
 
    void setColumnNames(String[] columnNames) {
         this.columnNames = columnNames;
     }
 
     public String[] getColumnNames() {
         return columnNames;
     }
 
    void setColumnTypeHints(String[] hints) {
         this.columnTypeHints = hints;
     }
 
     public String[] getColumnTypeHints() {
         return columnTypeHints;
     }
 
    void setHasColumnHeader(boolean flag) {
         hasColumnHeader = flag;
     }
 
     public boolean hasColumnHeader() {
         return hasColumnHeader;
     }
 
    void setTypeErrorMode(String mode) {
         typeErrorMode = mode;
     }
 
     public String getTypeErrorMode() {
         return typeErrorMode;
     }
 
    void setExcludeColumns(String[] columns) {
         this.excludeColumns = columns;
     }
 
     public String[] getExcludeColumns() {
         return excludeColumns;
     }
 
    void setOnlyColumns(String[] columns) {
         this.onlyColumns = columns;
     }
 
     public String[] getOnlyColumns() {
         return onlyColumns;
     }
 }
