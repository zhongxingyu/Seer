 package com.github.jcloudburst;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.SQLException;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 public class DelimitedFileHandler implements ImportDataSource {
   protected CSVReader reader;
 
   protected ColumnMapper mapper;
 
   protected String[] nextRow;
 
   public DelimitedFileHandler(File file, ConfigurationType config, ColumnMapper mapper) throws IOException {
     this.mapper = mapper;
 
     String sep = config.getCsv().getSeparatorChar();
     if (sep == null || sep.isEmpty()) {
       sep = ",";
     }
 
     reader = new CSVReader(new FileReader(file), sep.charAt(0));
 
     initializeMapper(config);
   }
 
   protected void initializeMapper(ConfigurationType config) throws IOException {
     String[] row = null;
     if (config.getCsv().isHasHeaderRow()) {
       row = reader.readNext();
     }
 
     for (int colId = 0; colId < mapper.numColumns(); colId++) {
       if (!mapper.isColumnDefined(colId)) {
         if (row == null) {
           throw new IllegalArgumentException("No header row specified and no file column index specified for column " + colId);
         }
 
         int fileColIndex = -1;
         String fileColName = mapper.getFileColumnName(colId);
 
         for (int i = 0; i < row.length; i++) {
           String value = row[i];
           if (value != null) {
             if (fileColName.equalsIgnoreCase(value)) {
               fileColIndex = i;
               break;
             }
           }
         }
 
         if (fileColIndex < 0) {
           throw new IllegalArgumentException("No file column index specified for column " + colId +
               " and no corresponding column named '" + fileColName + "'");
         } else {
           mapper.setFileColumnIndex(colId, fileColIndex);
         }
       }
     }
 
     nextRow = reader.readNext();
   }
 
   @Override
   public boolean hasNextRow() {
     return nextRow != null;
   }
 
   @Override
   public void fillRow(RowHandler handler) throws SQLException, IOException {
     int totalColumns = mapper.numColumns();
 
     for (int colId = 0; colId < totalColumns; colId++) {
       int fileColIndex = mapper.getFileColumnIndex(colId);
       if (fileColIndex >= 0) {
         String value = nextRow[fileColIndex];
 
         // if cell is null
         if (value == null || value.isEmpty()) {
           handler.setValue(colId, (String) null);
         } else {
           handler.setValue(colId, value);
         }
       }
     }

    nextRow = reader.readNext();
   }
 }
