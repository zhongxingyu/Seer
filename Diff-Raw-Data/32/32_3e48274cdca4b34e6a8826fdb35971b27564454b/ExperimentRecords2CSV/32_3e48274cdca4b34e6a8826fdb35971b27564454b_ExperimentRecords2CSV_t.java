 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.wsnusbcollect.experiment;
 
 import com.csvreader.CsvWriter;
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 import fi.wsnusbcollect.App;
 import fi.wsnusbcollect.db.DataCSVWritable;
 import fi.wsnusbcollect.db.ExperimentDataGenericMessage;
 import fi.wsnusbcollect.db.ExperimentDataLog;
 import fi.wsnusbcollect.db.ExperimentDataParameters;
 import fi.wsnusbcollect.db.ExperimentMetadata;
 import fi.wsnusbcollect.db.FileWritable;
 import fi.wsnusbcollect.db.FileWritableTypes;
 import fi.wsnusbcollect.db.USBconfiguration;
 import fi.wsnusbcollect.db.USBdevice;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentHashMap;
 import javax.annotation.PostConstruct;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author ph4r05
  */
 public class ExperimentRecords2CSV implements ExperimentRecords2DB{
     private static final Logger log = LoggerFactory.getLogger(ExperimentRecords2CSV.class);
     
     // 
     public static final String DATADIR="RECORDS_DATA_DIR";
     
     // from properties
     protected static String realDataDir=null;
     
     // xstream writer, global
     protected static XStream xstreamWriter=null;
     
     /**
      * Map of opened files to write data, for XML files
      */
     protected static ConcurrentHashMap<String, BufferedWriter> fileBuffers = new ConcurrentHashMap<String, BufferedWriter>();
     
     /**
      * For CSV writers
      */
     protected static ConcurrentHashMap<String, CsvWriter> fileCSVWriters = new ConcurrentHashMap<String, CsvWriter>();
     
     /**
      * Main experiment meta data
      */
     protected ExperimentMetadata expMetaMain;
     
     @PostConstruct
     public synchronized void postConstruct(){
         // load data directory from properties file
         if (realDataDir==null){
             realDataDir = App.getRunningInstance().getProps().getProperty(DATADIR, "expData");
         
             // assure existence
             File dataDir = new File(realDataDir);
             if (dataDir.exists()==false){
                 dataDir.mkdirs();
             }
             
             try {
                 // cannonical name
                 String cannonDir = dataDir.getCanonicalPath();
                 if (cannonDir!=null && cannonDir.isEmpty()==false){
                     realDataDir = cannonDir;
                 }
             } catch (IOException ex) {
                 log.error("Cannot determine cannonical path for data dir", ex);
             }
         }
         
         // xstream construction
         if (xstreamWriter==null){
             xstreamWriter = getXstream();
         }
        
        log.info("Data file writer initialized, datadir="+realDataDir);
     }
     
     /**
      * initialize Xstream writer
      * @return 
      */
     public static XStream getXstream(){
         XStream xstream = new XStream(new DomDriver());
 //        xstream.alias("SimpleGenericNode", SimpleGenericNode.class);
 //        xstream.alias("GenericPlatform", rssi_graph.nodeRegister.NodePlatformGeneric.class);
 //        xstream.alias("TelosBPlatform", rssi_graph.nodeRegister.NodePlatformTelosb.class);
 //        xstream.alias("IRISPlatform", rssi_graph.nodeRegister.NodePlatformIris.class);
 //        xstream.alias("LogNormalShadowing", rssi_graph.rssi.RSSI2DistLogNormalShadowing.class);
         xstream.processAnnotations(ExperimentMetadata.class);
         xstream.processAnnotations(USBconfiguration.class);
         xstream.processAnnotations(USBdevice.class);
         xstream.processAnnotations(ExperimentDataGenericMessage.class);
         xstream.processAnnotations(ExperimentDataLog.class);
         xstream.processAnnotations(ExperimentDataParameters.class);
         return xstream;
     }
     
     /**
      * Store entity as CSV file - has to have CSV writable implemented
      * @param entity 
      */
     public void storeEntityCSV(DataCSVWritable entity) {
         String id = entity.getCSVname();
         CsvWriter csvOutput = null;
         
        boolean writerCreated = fileCSVWriters.containsKey(id);
        
         // check existence in map
        if (writerCreated) {
            csvOutput = fileCSVWriters.get(id);
        } else {
            log.info("Entity ID was not found in register: [" + id + "]; id of structure: " + fileBuffers.toString());
            // dump all key ids
            for(String curKey:  fileCSVWriters.keySet()){
                log.info("KeyInWRITERS: [" + curKey + "]");
            }
            
            synchronized(fileCSVWriters){
                 // this file does not yet exist
                 String path = realDataDir + App.pathSeparator + id + ".csv";
                 File tmpFile = new File(path);
 
                 // does file itself exists?
                 boolean empty=false;
                 if (tmpFile.exists() == false) {
                     try {
                         // no -> create new empty and init it with header record
                         tmpFile.createNewFile();
                     } catch (IOException ex) {
                         log.error("cannot create new CSV file: " + path, ex);
                         return;
                     }
 
                     empty=true;
                 }
 
                 // create new writer
                 try {
                     // create new CSVwriter
                     csvOutput = new CsvWriter(new FileWriter(path, true), ';');
 
                     // if empty -> add header
                     if (empty){
                        log.info("Entity file does not exist: " + id);
                         entity.writeCSVheader(csvOutput);
                         csvOutput.endRecord();
                         csvOutput.flush();
                     }
 
                     // add to map
                     fileCSVWriters.put(id, csvOutput);
                    log.info("Initialized new entity file: " + id + "; SizeOfWriters: " + fileCSVWriters.size());
                 } catch (IOException ex) {
                     log.error("Cannot initialize new CSV writer", ex);
                     return;
                 }
             }
         }
         
         synchronized(csvOutput){
             try {
                 // dump to CSV and end record
                 entity.writeCSVdata(csvOutput);
                 csvOutput.endRecord();
             } catch (IOException ex) {
                 log.error("Cannot save record 2 CSV file - IOException occurred", ex);
             }
         }
     }
     
     /**
      * Store entity as XML file - use XStream
      * @param entity 
      */
     public void storeEntityXML(Object entity){
         String id = entity.getClass().getCanonicalName();
         BufferedWriter bw = null;
         
         // check existence in map
        if (fileBuffers.containsKey(id) == false) {
            synchronized(fileBuffers){
                 // this file does not yet exist
                 String path = realDataDir + App.pathSeparator + id + ".xml";
                 File tmpFile = new File(path);
 
                 // does file itself exists?
                 if (tmpFile.exists() == false) {
                     try {
                         // no -> create new empty and init it with header record
                         tmpFile.createNewFile();
                     } catch (IOException ex) {
                         log.error("cannot create new XML file: " + path, ex);
                         return;
                     }
                 }
 
                 // create new writer
                 try {
                     // create new CSVwriter
                     bw = new BufferedWriter(new FileWriter(tmpFile));
 
                     // add to map
                     fileBuffers.put(id, bw);
                 } catch (IOException ex) {
                     log.error("Cannot initialize new XML writer", ex);
                     return;
                 }
             }
         } else {
             bw = fileBuffers.get(id);
         }
         
         synchronized(bw){
             try {
                 bw.write(xstreamWriter.toXML(entity));
                 bw.write("\n");
                bw.flush();
             } catch (IOException ex) {
                 log.error("Cannot save record 2 XML file - IOException occurred", ex);
             }
         }
     }
     
     /**
      * Store entity to CSV file, has opened in register?
      * @param entity 
      */
     @Override
     public void storeEntity(Object entity) {
         // nulltest
         if (entity==null){
             throw new NullPointerException("Cannot store null value");
         }
         
         // decide what to do according to preffered way of serialization
         if (!(entity instanceof FileWritable)){
             // cannot continue with serialization
             throw new IllegalArgumentException("Class " + entity.getClass().getCanonicalName() 
                     + " does not implements FileWritable interface");
         }
         
         final FileWritable fw = (FileWritable) entity;
         if (fw.getPrefferedWriteFormat()==FileWritableTypes.CSV){
             if (entity instanceof DataCSVWritable){
                 final DataCSVWritable dcsv = (DataCSVWritable) entity;
                 this.storeEntityCSV(dcsv);
             } else {
                 throw new IllegalArgumentException("Class " + entity.getClass().getCanonicalName()
                         + " preffers CSV format but does not implement DataCSVWritable interface");
             }
         } else {
             this.storeEntityXML(entity);
         }
     }
     
     /**
      * To be able to determine meaningful files prefix
      * @param meta 
      */
     @Override
     public void setMainExperiment(ExperimentMetadata meta){
         this.expMetaMain = meta;
     }
     
     @Override
     public void storeExperimentMeta(ExperimentMetadata meta) {
         this.storeEntity(meta);
     }
 
     @Override
     public void updateExperimentStart(ExperimentMetadata meta, long mili) {
         meta.setMiliStart(mili);
         this.storeEntity(meta);
     }
 
     @Override
     public void closeExperiment(ExperimentMetadata meta) {
         meta.setDatestop(new Date());
         this.storeEntity(meta);
     }
 
     @Override
     public synchronized void flush() {
         // iterate over saved writers and flush them all
         Collection<CsvWriter> values = fileCSVWriters.values();
         for(CsvWriter writer : values){
             if (writer==null) continue;
             
             writer.flush();
         }
         
         // now iterate over XML buffered writer and do the same thing
         Collection<BufferedWriter> bufWriters = fileBuffers.values();
         for(BufferedWriter bw : bufWriters){
             if (bw==null) continue;
             
             
             try {
                 bw.flush();
             } catch (IOException ex) {
                 log.error("Cannot flush particular buffer writer", ex);
             }
         }
     }
 
     @Override
     public synchronized void close() {
         // iterate and remove
         Collection<CsvWriter> values = fileCSVWriters.values();
         
         for(Iterator<CsvWriter> iterator = values.iterator(); iterator.hasNext(); ){
             CsvWriter writer = iterator.next();
             if (writer==null) continue;
             
             writer.flush();
             writer.close();
             
             // remove from map
             iterator.remove();
         }
         
         // now iterate over XML buffered writer and do the same thing
         Collection<BufferedWriter> bufWriters = fileBuffers.values();
         for(Iterator<BufferedWriter> iterator = bufWriters.iterator(); iterator.hasNext(); ){
             BufferedWriter bw = iterator.next();
             if (bw==null) continue;
             
             try {
                 bw.flush();
                 bw.close();
                 iterator.remove();
             } catch (IOException ex) {
                 log.error("Cannot flush particular buffer writer", ex);
             }
         }
     }
 }
