 package org.hackystat.sensorbase.resource.sensordatatypes;
 
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.Property;
 import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.RequiredField;
 import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
 import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
 import org.junit.Test;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Response;
 import org.restlet.resource.DomRepresentation;
 import org.restlet.resource.Representation;
 import org.restlet.resource.XmlRepresentation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 /**
  * Tests the SensorBase REST API for both the SensorDataTypes and SensorDataType resources.
  * @author Philip M. Johnson
  */
 public class TestSdtRestApi extends SensorBaseRestApiHelper {
 
 
   /**
    * Test that GET host/sensorbase/sensordatatypes returns an index containing SampleSDT.
    * @throws Exception If problems occur.
    */
   @Test public void getSdtIndex() throws Exception {
     Response response = makeRequest(Method.GET, "sensordatatypes");
 
     // Test that the request was received and processed by the server OK. 
     assertTrue("Testing for successful GET index", response.getStatus().isSuccess());
 
     // Ensure that we can find the SampleSdt definition.
     XmlRepresentation data = response.getEntityAsSax();
     Node node = data.getNode("//SensorDataTypeRef[@Name='SampleSdt']");
     assertNotNull("Checking that we found the SampleSdt", node);
     }
   
   /**
    * Test that GET host/sensorbase/sensordatatypes/SampleSdt returns the SampleSdt SDT.
    * @throws Exception If problems occur.
    */
   @Test public void getIndividualSdt() throws Exception {
     Response response = makeRequest(Method.GET, "sensordatatypes/SampleSdt");
 
     // Test that the request was received and processed by the server OK. 
     assertTrue("Testing for successful GET SampleSdt", response.getStatus().isSuccess());
     DomRepresentation data = response.getEntityAsDom();
     assertEquals("Checking SDT", "SampleSdt", data.getText("SensorDataType/@Name"));
     
     //Make it into a Java SDT and ensure the fields are there as expected. 
     SensorDataType sdt = SdtManager.unmarshallSdt(data.getDocument());
     assertEquals("Checking name", "SampleSdt", sdt.getName());
     assertTrue("Checking description", sdt.getDescription().startsWith("Sample SDT"));
     RequiredField reqField = sdt.getRequiredFields().getRequiredField().get(0);
     assertEquals("Checking required field name", "SampleField", reqField.getName());
     assertEquals("Checking required field value", "Sample Field Value", reqField.getDescription());
     Property property = sdt.getProperties().getProperty().get(0);
     assertEquals("Checking property key", "SampleProperty", property.getKey());
     assertEquals("Checking property value", "Sample Property Value", property.getValue());
     }
   
   /**
    * Test that PUT host/sensorbase/sensordatatypes/TestSdt works.
    * @throws Exception If problems occur.
    */
   @Test public void putSdt() throws Exception {
     // First, create a sample SDT. Note that our XmlSchema is too lenient right now. 
     SensorDataType sdt = new SensorDataType();
     sdt.setName("TestSdt");
     Document doc = SdtManager.marshallSdt(sdt);
     Representation representation = new DomRepresentation(MediaType.TEXT_XML, doc);
     String uri = "sensordatatypes/TestSdt";
     Response response = makeRequest(Method.PUT, uri, representation);
 
     // Test that the PUT request was received and processed by the server OK. 
     assertTrue("Testing for successful PUT TestSdt", response.getStatus().isSuccess());
     
     // Test to see that we can now retrieve it. 
     response = makeRequest(Method.GET, uri);
     assertTrue("Testing for successful GET TestSdt", response.getStatus().isSuccess());
     XmlRepresentation data = response.getEntityAsSax();
     assertEquals("Checking SDT", "TestSdt", data.getText("SensorDataType/@Name"));
     
    // Test that PUTting it again is an error. 
     response = makeRequest(Method.PUT, uri, representation);
    assertFalse("Testing for unsuccessful PUT TestSdt", response.getStatus().isSuccess());
     
     // Test that DELETE gets rid of this SDT.
     response = makeRequest(Method.DELETE, uri);
     assertTrue("Testing for successful DELETE TestSdt", response.getStatus().isSuccess());
     
     // Test that a second DELETE succeeds even though its no longer there. 
     response = makeRequest(Method.DELETE, uri);
     assertTrue("Testing for second DELETE TestSdt", response.getStatus().isSuccess());
   }
 }
