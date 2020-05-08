 package org.hackystat.dailyprojectdata.resource.coverage;
 
 import java.io.StringWriter;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.hackystat.dailyprojectdata.resource.coverage.jaxb.ConstructData;
 import org.hackystat.dailyprojectdata.resource.coverage.jaxb.CoverageDailyProjectData;
 import org.hackystat.dailyprojectdata.resource.dailyprojectdata.DailyProjectDataResource;
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.Property;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataRef;
 import org.hackystat.utilities.stacktrace.StackTrace;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.resource.Representation;
 import org.restlet.resource.Variant;
 import org.w3c.dom.Document;
 
 /**
  * Implements the Resource for processing GET
  * {host}/coverage/{user}/{project}/{timestamp}/{type} requests. 
  * 
  * Authenticated user must be the uriUser, or Admin, or project member. 
  * 
  * @author jsakuda
  * @author austen
  */
 public class CoverageResource extends DailyProjectDataResource {
   private final String granularity;
 
   /**
    * The standard constructor.
    * 
    * @param context The context.
    * @param request The request object.
    * @param response The response object.
    */
   public CoverageResource(Context context, Request request, Response response) {
     super(context, request, response);
     this.granularity = (String) request.getAttributes().get("granularity");
   }
 
   /**
    * Returns an CoverageDailyProjectData instance representing the Coverage
    * associated with the Project data for the given day and granularity.
    * @param variant The representational variant requested.
    * @return The representation.
    */
   @Override
   public Representation getRepresentation(Variant variant) {
     Logger logger = this.server.getLogger();
     logger.fine("Coverage DPD: Starting");
     if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
       try {
         // [1] get the SensorBaseClient for the user making this request.
         SensorBaseClient client = super.getSensorBaseClient();
 
         // [2] Get the latest snapshot of Coverage data for this Project on the requested day.
         XMLGregorianCalendar startTime = Tstamp.makeTimestamp(this.timestamp);
         XMLGregorianCalendar endTime = Tstamp.incrementDays(startTime, 1);
         logger.fine("Coverage DPD: Requesting index: " + uriUser + " " + project);
         SensorDataIndex snapshot = 
           client.getProjectSensorDataSnapshot(this.uriUser, this.project, startTime, endTime, 
               "Coverage");
         logger.fine("Coverage DPD: Got index: " + snapshot.getSensorDataRef().size() 
             + " instances");
         // [3] Create the Coverage DPD.
         CoverageDailyProjectData coverageData = new CoverageDailyProjectData();
         coverageData.setProject(this.project);
         coverageData.setStartTime(startTime);
         coverageData.setGranularity(this.granularity);
         
         // [4] If data, then add ConstructData instances for required granularity.
         if (!snapshot.getSensorDataRef().isEmpty()) {
           logger.fine("There is data to process");
           // Add a ConstructData instance if this sensor data contains the appropriate granularity.
           for (SensorDataRef ref : snapshot.getSensorDataRef()) {
             SensorData data = client.getSensorData(ref);
             logger.fine ("Sensor Data: " + data);
             coverageData.setOwner(data.getOwner()); 
             coverageData.setTool(data.getTool()); 
             String coveredKey = this.granularity.toLowerCase() + "_Covered";
             String uncoveredKey = this.granularity.toLowerCase() + "_Uncovered";
             logger.fine("Covered: " + coveredKey + " " + this.getProperty(data, coveredKey));
             logger.fine("Uncovered: " + uncoveredKey + " " + this.getProperty(data, coveredKey));
             Integer covered = this.getCoverageValue(data, coveredKey);
             Integer uncovered = this.getCoverageValue(data, uncoveredKey);
             logger.fine("Covered num: " + covered);
             logger.fine("Uncovered num: " + uncovered);
             if ((covered != null) && (uncovered != null)) { //NOPMD
               ConstructData construct = new ConstructData();
               construct.setName(data.getResource());
               construct.setNumCovered(covered);
               construct.setNumUncovered(uncovered);
               coverageData.getConstructData().add(construct);
             }
           }
         }
         logger.fine("Coverage DPD: Finished processing instances.");
         // Now return the CoverageDPD instance. 
         String xmlData = this.makeCoverage(coverageData);
         logRequest("Coverage");
         return super.getStringRepresentation(xmlData);
       }
       catch (Exception e) {
         server.getLogger().warning("Error processing Coverage DPD: " + StackTrace.toString(e));
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
         return null;
       }
     }
     return null;
   }
 
   /**
    * Returns the passed SensorData instance as a String encoding of its XML
    * representation.
    * 
    * @param data The SensorData instance.
    * @return The XML String representation.
    * @throws Exception If problems occur during translation.
    */
   private String makeCoverage(CoverageDailyProjectData data) throws Exception {
     JAXBContext codeIssueJAXB = (JAXBContext) this.server.getContext().getAttributes().get(
         "CoverageJAXB");
     Marshaller marshaller = codeIssueJAXB.createMarshaller();
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     dbf.setNamespaceAware(true);
     DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
     Document doc = documentBuilder.newDocument();
     marshaller.marshal(data, doc);
     DOMSource domSource = new DOMSource(doc);
     StringWriter writer = new StringWriter();
     StreamResult result = new StreamResult(writer);
     TransformerFactory tf = TransformerFactory.newInstance();
     Transformer transformer = tf.newTransformer();
     transformer.transform(domSource, result);
     return writer.toString();
   }
   
   /**
    * Returns the string value associated with the specified property key. If no
    * property with that key exists, null is returned.
    * @param data The sensor data instance whose property list will be searched.
    * @param key the property key to search for.
    * @return The property value with the specified key or null.
    */
   private String getProperty(SensorData data, String key) {
     List<Property> propertyList = data.getProperties().getProperty();
     for (Property property : propertyList) {
       if (key.equals(property.getKey())) {
         return property.getValue();
       }
     }
     return null;
   }
   
   /**
    * Returns the Coverage value at the given Granularity, or null if not present or not 
    * parsable to an Integer. 
    * @param data The SensorData instance to inspect for the given granularity.
    * @param granularityKey One of line_Covered, line_Uncovered, method_Covered, etc.
    * @return The coverage integer, or null if not found.
    */
   private Integer getCoverageValue(SensorData data, String granularityKey) {
     String prop = getProperty(data, granularityKey);
     Double coverageNum = null;
     try {
       coverageNum = Double.parseDouble(prop);
     }
     catch (Exception e) { //NOPMD
       // Don't do anything, ok to drop through.
     }
    return coverageNum.intValue();
   }
 }
