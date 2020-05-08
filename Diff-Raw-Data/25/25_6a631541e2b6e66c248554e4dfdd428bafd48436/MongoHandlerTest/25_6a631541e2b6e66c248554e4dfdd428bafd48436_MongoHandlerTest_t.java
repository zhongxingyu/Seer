 package org.robot.gtf.dblayer.mongodb;
 
 import static org.junit.Assert.*;
 
 import java.net.ConnectException;
 import java.net.UnknownHostException;
 
 import org.junit.Test;
import org.robot.gtf.service.impl.mongodb.MongoHandler;
 
 public class MongoHandlerTest {
 
 	@Test
 	public void testDefaultConnection() {
 		
 		MongoHandler handler = new MongoHandler();
 		try {
 			handler.connect("localhost", 27017, "test");
 		} catch (UnknownHostException e) {
 			fail("UnknownHostException-Exception caught " + e.getMessage());
 		} catch (ConnectException e) {
 			fail("ConnectException-Exception caught " + e.getMessage());
 		}
 	}
 }
