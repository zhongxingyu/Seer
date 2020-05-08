 package com.ning.metrics.serialization.event;
 
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.smile.SmileFactory;
 import org.codehaus.jackson.smile.SmileGenerator;
 import org.codehaus.jackson.smile.SmileParser;
 import org.joda.time.DateTime;
 import org.testng.Assert;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 
 public class TestSmileEnvelopeEvent
 {
     private static Granularity eventGranularity = Granularity.MONTHLY;
     private static final DateTime eventDateTime = new DateTime();
     private static final String SCHEMA_NAME = "mySmile";
 
     private byte[] serializedBytes;
 
     private String serializedString;
    private static final String CHARSET_ISO_1 = "ISO-8859-1";
 
     @BeforeTest
     public void setUp() throws IOException
     {
         // Use same configuration as SmileEnvelopeEvent
         SmileFactory f = new SmileFactory();
         f.configure(SmileGenerator.Feature.CHECK_SHARED_NAMES, true);
         f.configure(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES, true);
         f.configure(SmileParser.Feature.REQUIRE_HEADER, false);
 
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         JsonGenerator g = f.createJsonGenerator(stream);
 
         g.writeStartObject();
         g.writeStringField(SmileEnvelopeEvent.SMILE_EVENT_GRANULARITY_TOKEN_NAME, eventGranularity.toString());
         g.writeObjectFieldStart("name");
         g.writeStringField("first", "Joe");
         g.writeStringField("last", "Sixpack");
         g.writeEndObject(); // for field 'name'
         g.writeStringField("gender", "MALE");
         g.writeNumberField(SmileEnvelopeEvent.SMILE_EVENT_DATETIME_TOKEN_NAME, eventDateTime.getMillis());
         g.writeBooleanField("verified", false);
         g.writeEndObject();
         g.close(); // important: will force flushing of output, close underlying output stream
 
         serializedBytes = stream.toByteArray();
         // one sanity check; should be able to round-trip via String (iff using latin-1!)
        serializedString = stream.toString(CHARSET_ISO_1);
     }
 
     /*
     ///////////////////////////////////////////////////////////////////////
     // Unit tests, char-to-byte conversions
     ///////////////////////////////////////////////////////////////////////
      */
 
     /**
      * Unit test that verifies that messy conversions between byte[] and String do not totally
      * break contents
      *
      * @throws Exception generic serialization exception
      */
     @Test(groups = "fast")
     public void testBytesVsString() throws Exception
     {
        byte[] fromString = serializedString.getBytes(CHARSET_ISO_1);
         Assert.assertEquals(fromString, serializedBytes);
     }
 
     /*
    ///////////////////////////////////////////////////////////////////////
    // Unit tests, metadata access
    ///////////////////////////////////////////////////////////////////////
     */
 
     @Test(groups = "fast")
     public void testGetEventDateTime() throws Exception
     {
         SmileEnvelopeEvent event = createEvent();
         Assert.assertEquals(event.getEventDateTime(), eventDateTime);
     }
 
     @Test(groups = "fast")
     public void testGetName() throws Exception
     {
         SmileEnvelopeEvent event = createEvent();
         Assert.assertEquals(event.getName(), SCHEMA_NAME);
     }
 
     /*
     ///////////////////////////////////////////////////////////////////////
     // Unit tests, externalization (readObject/writeObject)
     ///////////////////////////////////////////////////////////////////////
      */
 
     @Test(groups = "fast")
     public void testExternalization() throws Exception
     {
         SmileEnvelopeEvent envelope = createEvent();
 
         byte[] inputBytes = serializedBytes;
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         ObjectOutputStream out = new ObjectOutputStream(bytes);
         out.writeObject(envelope);
         out.close();
         byte[] data = bytes.toByteArray();
         ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
         Object ob = in.readObject();
 
         Assert.assertNotNull(ob);
         Assert.assertSame(SmileEnvelopeEvent.class, ob.getClass());
 
         SmileEnvelopeEvent result = (SmileEnvelopeEvent) ob;
         // name is not automatically set, but can check other metadata
         Assert.assertSame(result.getGranularity(), eventGranularity);
         Assert.assertEquals(result.getEventDateTime(), eventDateTime);
 
         Assert.assertNotNull(result.getData());
         // TODO Looks like there are multiple JsonNode classes?
         //Assert.assertSame(result.getData().getClass(), JsonNode.class);
         Assert.assertEquals(result.getSerializedEvent(), inputBytes);
     }
 
     @Test
     public void testReadWriteExternal() throws Exception
     {
         SmileEnvelopeEvent event = createEvent();
 
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();
         ObjectOutput out = new ObjectOutputStream(outStream);
         event.writeExternal(out);
         out.close();
 
         SmileEnvelopeEvent event2 = new SmileEnvelopeEvent();
         event2.readExternal(new ObjectInputStream(new ByteArrayInputStream(outStream.toByteArray())));
 
         Assert.assertEquals(event2.getName(), event.getName());
         Assert.assertEquals(event2.getGranularity(), event.getGranularity());
         Assert.assertEquals(event2.getName(), event.getName());
     }
 
     /*
     ///////////////////////////////////////////////////////////////////////
     // Helper methods
     ///////////////////////////////////////////////////////////////////////
      */
 
     private SmileEnvelopeEvent createEvent() throws IOException
     {
         return new SmileEnvelopeEvent(SCHEMA_NAME, serializedBytes, eventDateTime, eventGranularity);
     }
 }
