 package ws.cogito.auditing.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.ValidationEvent;
 import javax.xml.bind.ValidationEventHandler;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ws.cogito.auditing.model.AuditEvent;
 import ws.cogito.auditing.model.AuditEvents;
 
 /**
  * Unit test the XML Transreption
  * @author jeremydeane
  */
 
 public class XMLTransreptionTest {
 	
 	private static final Logger logger = LoggerFactory.getLogger
 			(XMLTransreptionTest.class);	
 	
 	@Test
 	public void toXMLAuditEventTest() throws Exception {
 		
 		//Test AuditEvent Transreption
 		AuditEvent auditEvent = new AuditEvent
 				("Claims", "201110201650", "Bodily Injury");
 
 		JAXBContext context = JAXBContext.newInstance(AuditEvent.class);
 		Marshaller marsheller = context.createMarshaller();
 		
 		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 		
 		marsheller.marshal(auditEvent, outStream);
 		
 		String xml = outStream.toString();
 		
 		logger.debug(xml);
 
 		assertTrue(xml.contains("Claims"));
 	}
 	
 	@Test
 	public void toXMLAuditEventsTest() throws Exception {		
 
 		//Test AuditEvent Transreption
 		AuditEvent auditEvent = new AuditEvent
 				("Claims", "201110201650", "Bodily Injury");
 		
 		AuditEvent auditEvent2 = new AuditEvent
 				("Claims", "201210201650", "Commercial Vehicle");			
 		
 		List<URL> auditEventLocations = new ArrayList<URL>();
 		
		auditEventLocations.add (auditEvent.getAuditEventLocation("localhost",8080, "/spring-auditor"));
		auditEventLocations.add (auditEvent2.getAuditEventLocation("localhost",8080, "/spring-auditor"));
 		
 		AuditEvents auditEvents = new AuditEvents ("Claims", auditEventLocations);		
 
 		JAXBContext context = JAXBContext.newInstance(AuditEvents.class);
 		Marshaller marsheller = context.createMarshaller();
 		
 		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 		
 		marsheller.marshal(auditEvents, outStream);
 		
 		String xml = outStream.toString();
 		
 		logger.debug(xml);
 		
 		assertTrue(xml.contains ("<audit-events application=\"Claims\"><event>"));
 	}	
 	
 	@Test
 	public void toPOJOAuditEventTest() throws Exception {
 		
 		String xml = "<audit-event><application>Billing</application><time>201210201650</time><message>Late Payment</message></audit-event>";
 		
 		//Unmarshell to Java
 		JAXBContext context = JAXBContext.newInstance(AuditEvent.class);			
 		Unmarshaller unmarshaller = context.createUnmarshaller();
 		unmarshaller.setEventHandler(new ValidationEventHandler() {
 
             @Override
             public boolean handleEvent(ValidationEvent event) {
                 logger.error((event.getMessage()));
                 return true;
             }}
 
         );
 		
 		InputStream stream = IOUtils.toInputStream (xml);	
 
 		//Unmarshell to Java		
 		AuditEvent auditEvent  = (AuditEvent) unmarshaller.unmarshal(stream);
 	
 		logger.debug(auditEvent.toString());
 		
 		assertEquals(auditEvent.getApplication(), "Billing");	
 	}
 	
 	@Test
 	public void toPOJOAuditEventsTest() throws Exception {
 		
 		StringBuffer xml = new StringBuffer();
 		xml.append("<audit-events application=\"Claims\">");
		xml.append("<event>http://localhost:8080/spring-auditor/audit/event/Claims-201110201650</event>");
		xml.append("<event>http://localhost:8080/spring-auditor/audit/event/Claims-201210201650</event>");
 		xml.append("</audit-events>");
 		
 		//Unmarshell to Java
 		JAXBContext context = JAXBContext.newInstance(AuditEvents.class);			
 		Unmarshaller unmarshaller = context.createUnmarshaller();
 		unmarshaller.setEventHandler(new ValidationEventHandler() {
 
             @Override
             public boolean handleEvent(ValidationEvent event) {
                 logger.error((event.getMessage()));
                 return true;
             }}
 
         );
 		
 		InputStream stream = IOUtils.toInputStream (xml.toString());	
 
 		//Unmarshell to Java
 		AuditEvents auditEvents  = (AuditEvents) unmarshaller.unmarshal(stream);
 	
 		logger.debug(auditEvents.toString());
 		
 		assertEquals(auditEvents.getEvents().size(), 2);	
 	}	
 }
