 package com.metal.webservice.test;
 
 import static org.junit.Assert.*;
 
 import java.net.MalformedURLException;
 import java.net.URL;
import java.util.List;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.Endpoint;
 import javax.xml.ws.Service;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.metal.model.Participant;
 import com.metal.webservice.Statistics;
 import com.metal.webservice.StatisticsWs;
 
 public class StatisticsIT {
 
 	private Endpoint endpoint;
 	private URL wsdlDocumentLocation;
 	private QName serviceQN;
 	private QName portQN;
 
 	@Before
 	public void setUp() throws MalformedURLException {
 		endpoint = Endpoint.publish("http://localhost:1234/Statistics", new Statistics());
 		assertTrue(endpoint.isPublished());
 		assertEquals("http://schemas.xmlsoap.org/wsdl/soap/http", endpoint.getBinding().getBindingID());
 
 		wsdlDocumentLocation = new URL("http://localhost:8080/StatisticsService/Statistics?wsdl");
 		String namespaceURI = "http://webservice.metal.com/";
 		String serviceName = "StatisticsService";
 		String portName = "StatisticsPort";
 		serviceQN = new QName(namespaceURI, serviceName);
 		portQN = new QName(namespaceURI, portName);
 	}
 
 	@After
 	public void tearDown() {
 		endpoint.stop();
 		assertFalse(endpoint.isPublished());
 	}
 
 	@Test
 	@Ignore
 	public void testGetCadena() {
 		Service service = Service.create(wsdlDocumentLocation, serviceQN);
 		StatisticsWs statisticsWs = service.getPort(portQN, StatisticsWs.class);
 		String cadena = statisticsWs.getCadena();
 		assertEquals("Cadena Invlida", "TEST", cadena);
 	}
 
 	@Test
 	public void testGetRatingParticipants() {
 		Service service = Service.create(wsdlDocumentLocation, serviceQN);
 		StatisticsWs statisticsWs = service.getPort(portQN, StatisticsWs.class);
 
 		// List<Participant> participants =
 		// statisticsWs.getRatingParticipants();
 		Participant participant = statisticsWs.getRatingParticipants();
 		// assertTrue("Invalid resultList", participants.size() > 0);
 
 		System.out.println("NAME: " + participant.getName());
 	}
 
 	@Test
 	@Ignore
 	public void testGetRatingSongs() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	@Ignore
 	public void testGetRatingPerParticipant() {
 		fail("Not yet implemented");
 	}
 
 }
