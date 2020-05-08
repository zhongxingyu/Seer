 package hu.sztaki.ilab.longneck.process.access;
 
 import com.csvreader.CsvReader;
 import hu.sztaki.ilab.longneck.Field;
 import hu.sztaki.ilab.longneck.Record;
 import hu.sztaki.ilab.longneck.RecordImpl;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import org.apache.log4j.Logger;
 
 /**
  * A data source reading CSV files.
  *
  * Built on OpenCSV.
  *
  * @author Molnar Peter <molnarp@sztaki.mta.hu>
  */
 public class CsvSource implements Source {
 
     /** Logger. */
     private final static Logger log = Logger.getLogger(CsvSource.class);
 
     /** The name of this source from property. */
     private String name;
     /** The name of the source file. */
     private String path;
 	/** The delimiter used to separate the records. */
 	private Character delimiter = ';';
     /** The character set to use when reading the file. */
     private String characterSet = "UTF-8";
 	/** Column names. */
 	private String[] columns = null;
     /** The input files have headers. */
     private boolean hasHeaders = true;
     /** The CSV reader. */
     private CsvReader csvReader;
     /** The runtime properties. */
     protected Properties runtimeProperties;
     /** The source path to read csv files from. */
     private String sourcePath;
     /** The list of source files that are read. */
     private List<String> sourceFiles;
     /** The current file in the list. */
     private int currentFileIndex = -1;
 
     @Override
     public Record getRecord() throws NoMoreRecordsException {
 		Record result = new RecordImpl();
 
         try {
             String[] values = getNextRecordValues() ;
 
             while(values.length != columns.length ) {
                 log.warn("Skipping input line, number of columns differs: " + columns.length +
                          " (process definition) vs. " + values.length + " (CSV file line): \n" +
                          csvReader.getRawRecord() );
                 values = getNextRecordValues() ;
             }
             for (int i = 0; i < values.length; ++i) {
                 result.add(new Field(columns[i], values[i]));
             }
 
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         }
 
         return result;
 
     }
     
     private String[] getNextRecordValues() throws IOException, NoMoreRecordsException {
         while (csvReader == null || ! csvReader.readRecord()) {
             try {
                 nextFile();
             } catch (CsvHeaderException ex) {
                 log.warn(ex);
                 continue;
             }
         }
         return csvReader.getValues();
     }
 
     private void nextFile() throws NoMoreRecordsException, FileNotFoundException,
             CsvHeaderException, IOException {
         // Close current reader if available
         if (csvReader != null) {
             csvReader.close();
         }
 
         // Set current source file
         ++currentFileIndex;
         if (sourceFiles.size() <= currentFileIndex) {
             throw new NoMoreRecordsException("No more records.");
         }
 
         log.info(String.format("Processing file: %1$s", sourceFiles.get(currentFileIndex)));
 
         // Create new reader
         csvReader = new CsvReader(sourceFiles.get(currentFileIndex),
                 delimiter, Charset.forName(characterSet));
 
         // Try to set column names from the first file
         if (hasHeaders) {
             if (columns == null) {
                 if (csvReader.readHeaders()) {
                     columns = csvReader.getHeaders();
                 } else {
                     throw new CsvHeaderException(
                             String.format("Cannot read headers from file: %1$s",
                             sourceFiles.get(currentFileIndex)));
                 }
             } else {
                 csvReader.skipRecord();
             }
         }
     }
 
     @Override
     public void init() {
         // Check correct configuration
         if (hasHeaders == false && columns == null) {
             throw new RuntimeException(
                     "Input files must have headers or column names must be defined.");
         }
 
         if ((sourcePath = path) == null) {
             // Read source path from runtime properties
             sourcePath = runtimeProperties.getProperty(String.format("csvSource.%1$s.Path", name));
         }
 
         // Check source path is set
         if (sourcePath == null || "".equals(sourcePath)) {
            throw new RuntimeException(name!= null?String.format("csvSource.%1$s.sourcePath is undefined.", name):"Path is undefined");
         }
 
 
         // Create a list of files from the source path
         sourceFiles = new ArrayList<String>();
         for (String path : sourcePath.split(File.pathSeparator)) {
 
             if (path == null || "".equals(path)) {
                 continue;
             }
 
             File source = new File(path);
             if (source.isFile() && source.length() != 0) {
                 sourceFiles.add(source.getAbsolutePath());
             }
             else if (source.isDirectory()) {
                 for (File f : source.listFiles()) {
                     if (f.isFile() && f.length() != 0) {
                         sourceFiles.add(f.getAbsolutePath());
                     }
                 }
             }
         }
 
         // Check that at least 1 file is defined
         if (sourceFiles.isEmpty()) {
             throw new RuntimeException(
                     String.format("No files found at the specified location: %1$s", sourcePath));
         }
     }
 
     @Override
     public void close() {
         // Close current reader if available
         if (csvReader != null) {
             csvReader.close();
         }
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getPath() {
         return path;
     }
 
     public void setPath(String path) {
         this.path = path;
     }
     
     
 
     public Character getDelimiter() {
         return delimiter;
     }
 
     public void setDelimiter(Character delimiter) {
         this.delimiter = delimiter;
     }
 
     public String getCharacterSet() {
         return characterSet;
     }
 
     public void setCharacterSet(String characterSet) {
         this.characterSet = characterSet;
     }
 
     public void setColumns(String columns) {
         this.columns = columns.split("\\s+");
 
         if (this.columns != null && this.columns.length <= 0) {
             this.columns = null;
         }
     }
 
     public boolean isHasHeaders() {
         return hasHeaders;
     }
 
     public void setHasHeaders(boolean hasHeaders) {
         this.hasHeaders = hasHeaders;
     }
 
     public Properties getRuntimeProperties() {
         return runtimeProperties;
     }
 
     public void setRuntimeProperties(Properties runtimeProperties) {
         this.runtimeProperties = runtimeProperties;
     }
 
 }
