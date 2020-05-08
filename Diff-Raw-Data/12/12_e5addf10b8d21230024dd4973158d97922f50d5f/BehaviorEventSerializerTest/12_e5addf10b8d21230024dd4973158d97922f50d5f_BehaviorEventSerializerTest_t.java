 package com.mtgi.analytics;
 
 import static org.junit.Assert.*;
 
 import java.io.StringWriter;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class BehaviorEventSerializerTest {
 
 	private BehaviorEventSerializer inst;
 	private TimeZone tz;
 	
 	@Before
 	public void setUp() {
 		tz = TimeZone.getDefault();
 		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
 		inst = new BehaviorEventSerializer();
 	}
 	
 	@After
 	public void tearDown() {
 		inst = null;
 		TimeZone.setDefault(tz);
 	}
 	
 	/** test serialization of an event with all fields populated */
 	@Test
 	public void testAllFields() throws Exception {
 		
 		BehaviorEvent parent = new BehaviorEvent(null, "test-event", "root", "test", "user", "session");
 		parent.setId("a");
 		parent.start();
 
 		Calendar date = Calendar.getInstance();
 		date.clear();
 		date.set(2006, 6, 1, 13, 59, 7);
 		
 		BehaviorEvent evt = new FixedEvent(parent, "test-event", "child", "test", "user", "session", date.getTime(), 17L);
 		evt.setId("b");
 		evt.start();
 		
 		RuntimeException re = new RuntimeException("boom");
 		evt.setError(re);
 		evt.addData().add("hello", "world");
 		
 		evt.stop();
 		parent.stop();
 
 		StringWriter buffer = new StringWriter();
 		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(buffer);
 		
 		inst.serialize(writer, evt);
 		writer.flush();
 		writer.close();
 		
 		assertEquals("<event id=\"b\" parent-id=\"a\">" +
 					"<type>test-event</type><name>child</name><application>test</application>" +
 					"<start>2006-07-01T13:59:07.000+00:00</start><duration-ms>17</duration-ms>" +
 					"<user-id>user</user-id><session-id>session</session-id>" +
 					"<error>java.lang.RuntimeException: boom</error>" +
 					"<event-data hello=\"world\"></event-data></event>", 
 					buffer.toString());
 		
 	}
 	
 	/** test elision of optional fields from output */
 	@Test
 	public void testOptionalFields() throws Exception {
 		Calendar date = Calendar.getInstance();
 		date.clear();
 		date.set(2006, 6, 1, 13, 59, 7);
 		
 		BehaviorEvent evt = new FixedEvent(null, "test-event", "child", "test", null, null, date.getTime(), 17L);
 		evt.setId("b");
 
 		evt.start();
 		evt.stop();
 
 		StringWriter buffer = new StringWriter();
 		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(buffer);
 		
 		inst.serialize(writer, evt);
 		writer.flush();
 		writer.close();
 		
 		assertEquals("<event id=\"b\"><type>test-event</type><name>child</name><application>test</application>" +
 					"<start>2006-07-01T13:59:07.000+00:00</start><duration-ms>17</duration-ms></event>", 
 					buffer.toString());
 		
 	}
 	
 	/** test formatting of the timezone field in date output */
 	@Test
 	public void testTimezoneFormatting() throws Exception {
 		TimeZone.setDefault(TimeZone.getTimeZone("PST"));
 		inst = new BehaviorEventSerializer();
 		
 		Calendar date = Calendar.getInstance();
 		date.clear();
 		date.set(2006, 6, 1, 13, 59, 7);
 		
 		BehaviorEvent evt = new FixedEvent(null, "test-event", "child", "test", null, null, date.getTime(), 17L);
 		evt.setId("b");
 
 		evt.start();
 		evt.stop();
 
 		StringWriter buffer = new StringWriter();
 		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(buffer);
 		
 		inst.serialize(writer, evt);
 		writer.flush();
 		writer.close();
 		
 		assertEquals("<event id=\"b\"><type>test-event</type><name>child</name><application>test</application>" +
 					"<start>2006-07-01T13:59:07.000-07:00</start><duration-ms>17</duration-ms></event>", 
 					buffer.toString());
 	}
 	
 	private static class FixedEvent extends BehaviorEvent {
 		
		private static final long serialVersionUID = -2851079725765893506L;

 		private Date date;
 		private Long duration;
		
 		public FixedEvent(BehaviorEvent parent, String type, String name, String application, String userId, String sessionId, Date start, Long duration) {
 			super(parent, type, name, application, userId, sessionId);
 			this.date = start;
 			this.duration = duration;
 		}
 		@Override
 		public Date getStart() {
 			return date;
 		}
 		@Override
 		public Long getDuration() {
 			return duration;
 		}
 		
 	}
 	
 }
