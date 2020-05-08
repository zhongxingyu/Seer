 package com.jboss.examples.eap6.management.nativeapi;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

 import java.io.IOException;
import java.net.InetAddress;
 
import org.jboss.as.controller.client.ModelControllerClient;
 import org.jboss.dmr.ModelNode;
 import org.junit.Test;
 
 public class RemovingMessagingSubsystemTest extends AbstractLocalManagerTest {
 
 
     @Test
     public void testRemovingMessagingSubsystemUsingTheNativeAPI() throws IOException, InterruptedException{
 	
 	/* Start with the full profile so that messaging is active */
	mgr.startServerWithStandaloneFullProfile();

	ModelControllerClient client = null;

	client = ModelControllerClient.Factory.create(
		InetAddress.getByName("localhost"), 9999);
 
 	// Create an "add" operation
 	ModelNode op = new ModelNode();
 	op.get("operation").set("remove");
 
 	// get the appropriate address in the subsystem
 	ModelNode address = op.get("address");
 	address.add("subsystem", "messaging");
 	
 	// execute
	ModelNode result = client.execute(op);
 	assertNotNull("The result should not be null", result);
 	System.out.println(result.toString());
 	assertEquals("The outcome should be 'success'", "\"success\"", result.get("outcome").toString());
     }
 }
