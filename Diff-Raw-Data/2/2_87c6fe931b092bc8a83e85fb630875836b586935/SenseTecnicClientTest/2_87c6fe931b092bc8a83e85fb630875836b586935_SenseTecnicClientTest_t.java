 package com.sensetecnic.client;
 
 import java.io.IOException;
 import java.io.StringReader;
 
 import junit.framework.TestCase;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.csvreader.CsvReader;
 
 public class SenseTecnicClientTest extends TestCase {
 
 	private SenseTecnicClient client;
 	@Before
 	protected void setUp() throws Exception {
 		super.setUp();
 		client = new SenseTecnicClient();
 		client.setStsBaseUrl("http://demo.sensetecnic.com/SenseTecnic/api");
 	}
 	
 	@After
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 	
 	@Test
 	public void testGetData() throws SenseTecnicException, IOException {
 
 
 		String csvData = client.getSensorDataCsv("sensetecnic.mule1", 10, "mike", "aMUSEment2");
 		
 		CsvReader reader = new CsvReader(new StringReader(csvData));
 		// populating the lists
 		while (reader.readRecord()) {
 			
 			for (int i=0; i<reader.getColumnCount(); i++) {
 				System.out.print(reader.get(i)+" ");
 
 			}
 			System.out.println();
 		}
 		
 	}
 	
 	@Test
 	public void testGetDataBadSensor() throws SenseTecnicException, IOException {
 		try {
 			String csvData = client.getSensorDataCsv("random.mule333", 10, "mike", "spidey7");
		} catch (Exception e) {
 			// success - should be a 404
 			return;
 		}
 		fail("exception not thrown");
 	}
 	
 	@Test
 	public void testRegisterSensor() throws SenseTecnicException, IOException {
 		//
 	}
 	
 	@Test
 	public void testDeleteSensor() throws SenseTecnicException, IOException {
 		//TODO: test deleting a sensor
 	}
 	
 	@Test
 	public void testSendReceiveData() throws SenseTecnicException, IOException {
 		//TODO: send it and receive it to check it goes in and out.
 	}
 	
 	@Test
 	public void testActuator() throws SenseTecnicException, IOException {
 		//TODO: create a listening actuator, send messages to it and compare
 	}
 	
 	@Test
 	public void testBadSubscriber() throws SenseTecnicException, IOException {
 		//TODO: check to see we receive correct error when subscription does not exist
 	
 	}
 	
 	@Test
 	public void testUnsubscribe() throws SenseTecnicException, IOException {
 		//TODO: check to see we receive correct error when subscription does not exist
 	}
 
 }
