 package org.obiba.magma.datasource.healthcanada;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Map;
 
 import javax.annotation.Nonnull;
 
 import org.obiba.magma.Datasource;
 import org.obiba.magma.MagmaDate;
 import org.obiba.magma.MagmaRuntimeException;
 import org.obiba.magma.NoSuchValueSetException;
 import org.obiba.magma.Timestamps;
 import org.obiba.magma.Value;
 import org.obiba.magma.ValueSet;
 import org.obiba.magma.ValueTable;
 import org.obiba.magma.ValueType;
 import org.obiba.magma.Variable;
 import org.obiba.magma.VariableEntity;
 import org.obiba.magma.support.AbstractValueTable;
 import org.obiba.magma.support.VariableEntityBean;
 import org.obiba.magma.type.DateTimeType;
 import org.obiba.magma.type.DateType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.Maps;
 
 import au.com.bytecode.opencsv.CSVReader;
 import de.schlichtherle.io.File;
 import de.schlichtherle.io.FileInputStream;
 
 public class HCDrugsValueTable extends AbstractValueTable {
 
   private static final Logger log = LoggerFactory.getLogger(HCDrugsValueTable.class);
 
   private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
 
   static final String DRUG_ENTITY_TYPE = "Drug";
 
   static final String ALL_FILES_ZIP_URL
       = "http://www.hc-sc.gc.ca/dhp-mps/alt_formats/zip/prodpharma/databasdon/allfiles.zip";
 
   private static final Charset WESTERN_EUROPE = Charsets.ISO_8859_1;
 
   private static final int BUFFER_SIZE = 153600;
 
   private File zsource;
 
   // source file name / entity id / value set
   private final Map<String, Map<String, String[]>> valueSets = Maps.newHashMap();
 
   public HCDrugsValueTable(Datasource datasource) {
     super(datasource, "Drugs");
     setVariableEntityProvider(new HCDrugsVariableEntityProvider(this));
     addVariableValueSources(new HCDrugsVariableValueSourceFactory());
   }
 
   @Override
   public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
     return new HCDrugsValueSet(entity);
   }
 
   @Override
   public Timestamps getTimestamps() {
     return new Timestamps() {
 
       private File drugEntry;
 
       @Nonnull
       @Override
       public Value getLastUpdate() {
         return DateTimeType.get().valueOf(new Date(getDrugEntry().lastModified()));
       }
 
       @Nonnull
       @Override
       public Value getCreated() {
         return DateTimeType.get().nullValue();
       }
 
       private File getDrugEntry() {
         if(drugEntry == null) {
           drugEntry = getFileEntry("drug.txt");
         }
         return drugEntry;
       }
     };
   }
 
   private void downloadLatestAllFiles() throws IOException {
     log.info("Download from: {} ...", ALL_FILES_ZIP_URL);
 
     // Get a connection to the URL and start up a buffered reader.
     URL url = new URL(ALL_FILES_ZIP_URL);
     url.openConnection();
 
     // Setup a buffered file writer to write out what we read from the website.
     java.io.File allfiles = java.io.File.createTempFile("healthcanada-drugs-", ".zip");
     allfiles.deleteOnExit();
 
     try(InputStream reader = url.openStream();
         FileOutputStream writer = new FileOutputStream(allfiles)) {
       byte[] buffer = new byte[BUFFER_SIZE];
       int bytesRead = 0;
 
       while((bytesRead = reader.read(buffer)) > 0) {
         writer.write(buffer, 0, bytesRead);
         buffer = new byte[BUFFER_SIZE];
       }
     }
 
     zsource = new File(allfiles);
   }
 
   public File getFileEntry(String fileName) {
     if(zsource == null) {
       try {
         downloadLatestAllFiles();
       } catch(IOException e) {
         throw new MagmaRuntimeException("Unable to download Health Canada Drugs from: " + ALL_FILES_ZIP_URL, e);
       }
     }
     return new File(zsource, fileName);
   }
 
   CSVReader getEntryReader(String fileName) {
     try {
       return new CSVReader(new InputStreamReader(new FileInputStream(getFileEntry(fileName)), WESTERN_EUROPE));
     } catch(FileNotFoundException e) {
       throw new MagmaRuntimeException("Unable to read Health Canada Drug file: " + fileName, e);
     }
   }
 
  String[] normalize(String... line) {
    for(int i = 0; i < line.length; i++) {
      line[i] = normalize(line[i]);
     }
    return line;
   }
 
   private String normalize(String str) {
     return str.trim().replaceAll("\\s{2,}", " ");
   }
 
   class HCDrugsValueSet implements ValueSet {
 
     private final VariableEntity entity;
 
     private HCDrugsValueSet(VariableEntity entity) {
       this.entity = entity;
     }
 
     Value getValue(Variable variable) {
       String sourceFile = variable.getAttributeStringValue("file");
       int column = Integer.parseInt(variable.getAttributeStringValue("column"));
 
       String[] valueSet = getValueSet(sourceFile);
       if(valueSet == null) {
         return variable.isRepeatable() ? variable.getValueType().nullSequence() : variable.getValueType().nullValue();
       }
 
       return variable.isRepeatable()
           ? getValueSequence(variable.getValueType(), valueSet[column])
           : getValue(variable.getValueType(), valueSet[column]);
     }
 
     /**
      * Load the data in memory if not already done.
      *
      * @param sourceFile
      * @return
      */
     private String[] getValueSet(String sourceFile) {
       Map<String, String[]> sourceValueSets = valueSets.get(sourceFile);
       try {
         if(sourceValueSets == null) {
           sourceValueSets = Maps.newHashMap();
           valueSets.put(sourceFile, sourceValueSets);
           CSVReader reader = getEntryReader(sourceFile);
           String[] nextLine;
           while((nextLine = reader.readNext()) != null) {
             nextLine = normalize(nextLine);
             VariableEntity nextEntity = new VariableEntityBean(DRUG_ENTITY_TYPE, nextLine[0]);
             if(getVariableEntityProvider().getVariableEntities().contains(nextEntity)) {
               if(sourceValueSets.containsKey(nextLine[0])) {
                 // merge lines
                 sourceValueSets.put(nextLine[0], merge(sourceValueSets.get(nextLine[0]), nextLine));
               } else {
                 sourceValueSets.put(nextLine[0], nextLine);
               }
             }
           }
           reader.close();
         }
       } catch(IOException e) {
         throw new MagmaRuntimeException("Unable to read source file: " + sourceFile, e);
       }
 
       return sourceValueSets.get(entity.getIdentifier());
     }
 
     /**
      * Handle date format and deparse lines that were abusively merged.
      *
      * @param type
      * @param value
      * @return
      */
     private Value getValue(ValueType type, String value) {
       if(value == null || value.isEmpty()) return type.nullValue();
 
       if(type.equals(DateType.get())) {
         try {
           return DateType.get().valueOf(dateFormat.parse(value));
         } catch(ParseException e) {
           log.warn("ParseException", e);
           return DateType.get().nullValue();
         }
 
       } else {
         // case of abusive merging of lines
         if(value.indexOf('|') != -1) {
           String[] values = value.split("\\|");
           return values.length > 0 ? getValue(type, values[0]) : type.nullValue();
         }
         return type.valueOf(value);
       }
 
     }
 
     /**
      * Deparse multiple values resulting from the merge.
      *
      * @param type
      * @param value
      * @return
      */
     private Value getValueSequence(ValueType type, String value) {
       Collection<Value> values = new ArrayList<>();
       for(String val : value.split("\\|")) {
         values.add(getValue(type, val));
       }
       return type.sequenceOf(values);
     }
 
     /**
      * When there are multiple values, it is on multiple lines (therefore some data are abusively repeated).
      *
      * @param previousLine
      * @param nextLine
      * @return
      */
     private String[] merge(String[] previousLine, String... nextLine) {
       for(int i = 0; i < nextLine.length; i++) {
         previousLine[i] = previousLine[i] + "|" + nextLine[i];
       }
       return previousLine;
     }
 
     @Override
     public ValueTable getValueTable() {
       return HCDrugsValueTable.this;
     }
 
     @Override
     public VariableEntity getVariableEntity() {
       return entity;
     }
 
     @Override
     public Timestamps getTimestamps() {
       return new Timestamps() {
         @Nonnull
         @Override
         public Value getLastUpdate() {
           Value updated = getValue(getVariable("LAST_UPDATE_DATE"));
           if(updated.isNull()) return HCDrugsValueTable.this.getTimestamps().getLastUpdate();
           return updated;
         }
 
         @Nonnull
         @Override
         public Value getCreated() {
           Value created = getLastUpdate();
           Value history = getValue(getVariable("HISTORY_DATE"));
           if(history.isNull()) return created;
 
           for(Value val : history.asSequence().getValues()) {
             if(((MagmaDate) val.getValue()).asDate().before(((MagmaDate) created.getValue()).asDate())) {
               created = val;
             }
           }
           return created;
         }
       };
     }
   }
 }
