 package org.hackystat.sensorbase.resource.sensordatatypes;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 import org.restlet.Client;
 import org.restlet.data.Method;
 import org.restlet.data.Protocol;
 import org.restlet.data.Reference;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.resource.XmlRepresentation;
 
 /**
  * Tests the SensorBase REST API for the SensorDataType resource.
  *
  * @author Philip M. Johnson
  */
 public class TestSdtRestApi {
 
   /**
    * Test that GET host/hackystat/sensordatatypes returns an appropriate value.
    */
   @Test public void getSdtIndex() {
     // Set up the call.
     Method method = Method.GET;
     Reference reference = new Reference("http://localhost:9090/hackystat/sensordatatypes");
     Request request = new Request(method, reference);
 
     // Make the call.
     Client client = new Client(Protocol.HTTP);
     Response response = client.handle(request);
 
     // Test that the request was received and processed by the server OK. 
     assertTrue("Testing for successful status", response.getStatus().isSuccess());
 
     // Now test that the response is OK.
     XmlRepresentation data = response.getEntityAsSax();
     assertEquals("Checking SDT", "SampleSDT", data.getText("SensorDataTypes/SensorDataType/@Name"));
     }
 }
