 package org.hackystat.dailyprojectdata.resource.devtime;
 
 import java.io.StringWriter;
 import java.math.BigInteger;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.hackystat.dailyprojectdata.resource.dailyprojectdata.DailyProjectDataResource;
 import org.hackystat.dailyprojectdata.resource.devtime.jaxb.DevTimeDailyProjectData;
 import org.hackystat.dailyprojectdata.resource.devtime.jaxb.MemberData;
 import org.hackystat.sensorbase.client.SensorBaseClient;
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
  * Implements the Resource for processing GET {host}/devtime/{user}/{project}/{starttime} requests.
  * Requires the authenticated user to be {user} or else the Admin user for the sensorbase 
  * connected to this service. 
  * @author Philip Johnson
  */
 public class DevTimeResource extends DailyProjectDataResource {
   
  private boolean[] devEventArray = new boolean[288];
  
   /**
    * The standard constructor.
    * @param context The context.
    * @param request The request object.
    * @param response The response object.
    */
   public DevTimeResource(Context context, Request request, Response response) {
     super(context, request, response);
   }
   
   /**
    * Returns an DevTimeDailyProjectData instance representing the DevTime associated with the 
    * Project data, or null if not authorized. 
    * @param variant The representational variant requested.
    * @return The representation. 
    */
   @Override
   public Representation getRepresentation(Variant variant) {
     if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
       try {
         SensorBaseClient client = super.getSensorBaseClient();
         XMLGregorianCalendar startTime = Tstamp.makeTimestamp(this.timestamp);
         XMLGregorianCalendar endTime = Tstamp.incrementDays(startTime, 1);
         SensorDataIndex index = client.getProjectSensorData(authUser, project, startTime, endTime);
         Set<SensorData> data = new HashSet<SensorData>();
         for (SensorDataRef ref : index.getSensorDataRef()) {
           if (ref.getSensorDataType().equals("DevEvent")) {
             data.add(client.getSensorData(ref));
           }
         }
         // For now, each DevEvent represents five minutes of time.
         BigInteger devTimeMinutes = BigInteger.valueOf(data.size() * 5);
         DevTimeDailyProjectData devTime = new DevTimeDailyProjectData();
         devTime.setOwner(uriUser);
         devTime.setProject(project);
         devTime.setUriPattern("**");
         devTime.setTotalDevTime(devTimeMinutes);
         MemberData memberData = new MemberData();
         memberData.setMemberUri(uriUser);
         memberData.setDevTime(devTimeMinutes);
         devTime.getMemberData().add(memberData);
         String xmlData = makeDevTime(devTime);
         return super.getStringRepresentation(xmlData);
       }
       catch (Exception e) {
         server.getLogger().warning("Error processing devTime: " + StackTrace.toString(e));
         return null;
       }
     }
     return null;
   }
   
   /**
    * Returns the passed SensorData instance as a String encoding of its XML representation.
    * @param data The SensorData instance. 
    * @return The XML String representation.
    * @throws Exception If problems occur during translation. 
    */
   private String makeDevTime (DevTimeDailyProjectData data) throws Exception {
     JAXBContext devTimeJAXB = 
       (JAXBContext)this.server.getContext().getAttributes().get("DevTimeJAXB");
     Marshaller marshaller = devTimeJAXB.createMarshaller(); 
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
   * Takes a timestamp and returns an index between 0 and 287 indicating which five minute chunk
   * this timestamp falls within. 
   *
   * @param timeStamp  The date used to determine the five minute period.
   * @return           The index in the day array corresponding to the timestamp.
   */
  private void stampArray(XMLGregorianCalendar tstamp) {
    int hours = tstamp.getHour();
    int minutes = tstamp.getMinute();
    int index = (int) ((hours * 60 + minutes) / 288);
    devEventArray[index] = true;
  }
 }
 
