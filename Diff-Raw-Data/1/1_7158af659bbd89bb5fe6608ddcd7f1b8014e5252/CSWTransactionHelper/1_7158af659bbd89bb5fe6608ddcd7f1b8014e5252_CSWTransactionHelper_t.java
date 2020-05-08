 package gov.usgs.cida.watersmart.csw;
 
 import gov.usgs.cida.config.DynamicReadOnlyProperties;
 import gov.usgs.cida.watersmart.parse.RunMetadata;
 import gov.usgs.cida.watersmart.util.JNDISingleton;
import java.util.Map;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class CSWTransactionHelper {
     
     private static DynamicReadOnlyProperties props = JNDISingleton.getInstance();
     
     private RunMetadata metadataBean;
     
     public CSWTransactionHelper(RunMetadata runMeta) {
         this.metadataBean = runMeta;
     }
     
     public void insert() {
         XMLStreamReader recordsCall = getRecordsCall();
         String sosRepo = props.getProperty("watersmart.sos.model.repo");
         if (sosRepo == null) {
             throw new RuntimeException("Record not inserted, must specify thredds location");
         }
         // pull out gmd:MD_Metadata
     }
     
     public void update(RunMetadata oldInfo, RunMetadata newInfo) {
         
     }
     
     public void delete(RunMetadata info) {
         
     }
     
     private XMLStreamReader getRecordsCall() {
         //String identifer = metadataBean.modelId;
         // GET or POST to get a record, create XMLStream
         return null;
     }
     
     private void fullRecordTransaction(XMLStreamWriter record) {
         
     }
     
     private void updateFieldsTransaction(Map<String, String> propValueMap) {
         
     }
     
 }
