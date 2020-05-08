 package org.hackystat.dailyprojectdata.resource.codeissue;
 
 import static org.hackystat.dailyprojectdata.server.ServerProperties.SENSORBASE_FULLHOST_KEY;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.hackystat.dailyprojectdata.resource.codeissue.jaxb.CodeIssueDailyProjectData;
 import org.hackystat.dailyprojectdata.resource.codeissue.jaxb.MemberData;
 import org.hackystat.dailyprojectdata.resource.dailyprojectdata.DailyProjectDataResource;
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.Properties;
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
 import org.restlet.resource.Representation;
 import org.restlet.resource.Variant;
 import org.w3c.dom.Document;
 
 /**
  * Implements the Resource for processing GET {host}/codeissue/{user}/{project}/{timestamp}
  * requests. Requires the authenticated user to be {user} or else the Admin user for the
  * sensorbase connected to this service.
  * 
  * @author jsakuda
  */
 public class CodeIssueResource extends DailyProjectDataResource {
 
   /** The optional code issue tool. */
   private String tool;
 
   /** The optional type. */
   private String type;
 
   /**
    * The standard constructor.
    * 
    * @param context The context.
    * @param request The request object.
    * @param response The response object.
    */
   public CodeIssueResource(Context context, Request request, Response response) {
     super(context, request, response);
     this.tool = (String) request.getAttributes().get("Tool");
     // get the type and remove any spaces
     this.type = (String) request.getAttributes().get("Type");
     if (this.type != null) {
       this.type = type.replaceAll(" ", "");
     }
   }
 
   /**
    * Returns a CodeIssueDailyProjectData instance representing the CodeIssues associated with
    * the Project data, or null if not authorized.
    * 
    * @param variant The representational variant requested.
    * @return The representation.
    */
   @Override
   public Representation getRepresentation(Variant variant) {
     if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
       try {
         // [1] get the SensorBaseClient for the user making this request.
         SensorBaseClient client = super.getSensorBaseClient();
         // [2] get a SensorDataIndex of all sensor data for this Project on the requested day.
         XMLGregorianCalendar startTime = Tstamp.makeTimestamp(this.timestamp);
         XMLGregorianCalendar endTime = Tstamp.incrementDays(startTime, 1);
         SensorDataIndex index = client.getProjectSensorData(authUser, project, startTime,
             endTime);
         // [3] look through this index for CodeIssue sensor data
         CodeIssueRuntimeSorter sorter = new CodeIssueRuntimeSorter();
         for (SensorDataRef ref : index.getSensorDataRef()) {
           if (ref.getSensorDataType().equals("CodeIssue")) {
             // tell client to get the SensorData
             SensorData sensorData = client.getSensorData(ref);
             this.addSensorDataToSorter(sorter, sensorData);
           }
         }
         // [4] process all SensorData with the latest runtime
         MemberCodeIssueCounter counter = new MemberCodeIssueCounter();
         List<SensorData> lastestRuntimeCodeIssues = sorter.getLastCodeIssueBatch();
         for (SensorData sensorData : lastestRuntimeCodeIssues) {
           List<Property> typeProperties = this.getTypeProperties(sensorData);
           if (typeProperties.isEmpty()) {
             // no Type: properties, must be zero data, pass null type and 0 count
             counter.addMemberCodeIssue(sensorData.getOwner(), sensorData.getTool(), null, 0);
           }
           else {
             for (Property property : typeProperties) {
               counter.addMemberCodeIssue(sensorData.getOwner(), sensorData.getTool(), property
                   .getKey(), Integer.valueOf(property.getValue()));
             }
           }
         }
         // [5] create and return the CodeIssueDailyProjectData
         Set<String> members = counter.getMembers();
         CodeIssueDailyProjectData codeIssue = new CodeIssueDailyProjectData();
         String sensorBaseHost = this.server.getServerProperties().get(SENSORBASE_FULLHOST_KEY);
         for (String member : members) {
           ToolToTypeCounter toolToTypeCounter = counter.getMemberCodeIssueCounts(member);
 
           Set<String> tools = toolToTypeCounter.getTools();
           for (String tool : tools) {
             // create one MemberData instance per tool
             MemberData memberData = new MemberData();
             memberData.setTool(tool);
             memberData.setMemberUri(sensorBaseHost + "users/" + member);
 
             Map<String, Integer> typeCounts = toolToTypeCounter.getTypeCounts(tool);
             for (Entry<String, Integer> entry : typeCounts.entrySet()) {
               String type = entry.getKey();
               Integer count = entry.getValue();
 
               Map<QName, String> attributeMap = memberData.getOtherAttributes();
               attributeMap.put(new QName(type), count.toString());
             }
             codeIssue.getMemberData().add(memberData);
           }
 
           // Map<ToolTypePair, Integer> memeberCodeIssueCounts = counter
           // .getMemeberCodeIssueCounts(member);
           // for (Entry<ToolTypePair, Integer> entry : memeberCodeIssueCounts.entrySet()) {
           // MemberData memberData = new MemberData();
           // memberData.setTool(entry.getKey().getTool());
           // memberData.setCategory(entry.getKey().getType());
           // memberData.setCodeIssues(entry.getValue());
           // memberData.setMemberUri(sensorBaseHost + "users/" + member);
           // codeIssue.getMemberData().add(memberData);
           // }
         }
         codeIssue.setStartTime(sorter.getLastRuntime());
         codeIssue.setOwner(uriUser);
         codeIssue.setProject(project);
         codeIssue.setUriPattern("**"); // we don't support UriPatterns yet.
 
         String xmlData = this.makeCodeIssue(codeIssue);
         return super.getStringRepresentation(xmlData);
       }
       catch (Exception e) {
         server.getLogger().warning("Error processing CodeIssues: " + StackTrace.toString(e));
         return null;
       }
     }
     return null;
   }
 
   /**
    * Adds the sensor data to the given sorter only if the sensor data is determined to be valid
    * with the given tool and type.
    * 
    * @param sorter The sorter to add the issue to if it matches the given criteria.
    * @param sensorData The sensor data to test for validity and add to the sorter.
    */
   private void addSensorDataToSorter(CodeIssueRuntimeSorter sorter, SensorData sensorData) {
     // add code issue to the sorter if the issue matches the given tool and/or type
     if (isDataValid(sensorData)) {
       sorter.addCodeIssueData(sensorData);
     }
   }
 
   /**
    * Checks that the <code>SensorData</code> is valid for the Tool and Category pertaining to
    * the current request.
    * 
    * @param sensorData The <code>SensorData</code> instance to check against Tool and
    *          Category.
    * @return Returns true if the data should be processed otherwise false.
    */
   private boolean isDataValid(SensorData sensorData) {
 
     // neither tool nor type were specified
     if (this.tool == null && this.type == null) {
       return true;
     }
     // only tool given
     else if (this.type == null && this.tool != null) {
       String issueTool = sensorData.getTool();
       return this.tool.equals(issueTool);
     }
     // only type given
     else if (this.tool == null && this.type != null) {
       return this.hasTypeProperty(this.type, sensorData);
     }
     // both tool and type were specified
     else {
       String issueTool = sensorData.getTool();
       assert (issueTool != null);
       if (this.tool.equals(issueTool) && this.hasTypeProperty(this.type, sensorData)) {
         return true;
       }
     }
     return false;
   }
 
   /**
    * Determines if the property prefixed with "Type_" exists in the sensor data's properties.
    * 
   * @param propertyName The property prefixed with "Type:".
    * @param sensorData The sensor data instance to check.
    * @return Returns true if it exists, otherwise false.
    */
   private boolean hasTypeProperty(String propertyName, SensorData sensorData) {
     List<Property> typeProperties = this.getTypeProperties(sensorData);
     for (Property property : typeProperties) {
       if (property.getKey().equalsIgnoreCase(propertyName)) {
         return true;
       }
     }
     return false;
   }
 
   /**
    * Gets a list of all the property names prefixed with "Type_".
    * 
    * @param data The sensor data instance to get type properties from.
    * @return Returns all the property names prefixed with "Type_".
    */
   private List<Property> getTypeProperties(SensorData data) {
     ArrayList<Property> typePropertyList = new ArrayList<Property>();
 
     Properties properties = data.getProperties();
     List<Property> propertyList = properties.getProperty();
 
     for (Property property : propertyList) {
       if (property.getKey().startsWith("Type_")) {
         typePropertyList.add(property);
       }
     }
 
     return typePropertyList;
   }
 
   /**
    * Returns the passed SensorData instance as a String encoding of its XML representation.
    * 
    * @param data The SensorData instance.
    * @return The XML String representation.
    * @throws Exception If problems occur during translation.
    */
   private String makeCodeIssue(CodeIssueDailyProjectData data) throws Exception {
     JAXBContext codeIssueJAXB = (JAXBContext) this.server.getContext().getAttributes().get(
         "CodeIssueJAXB");
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
 }
