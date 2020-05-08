 /*
  * Copyright 2010-2011 Ning, Inc.
  *
  * Ning licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 
 package com.ning.metrics.serialization.event;
 
 import org.codehaus.jackson.*;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.smile.SmileFactory;
 import org.codehaus.jackson.smile.SmileGenerator;
 import org.codehaus.jackson.smile.SmileParser;
 import org.joda.time.DateTime;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 
 /**
  * Event representation of a single Smile event. This class is useful to send Json trees
  * to the collector via the eventtracker library.
  * Under the cover though, we use SmileBucketEvent on the wire (when sending to the collector) and in
  * Hadoop sequencefiles (to leverage Smile back-references).
  *
  * @see com.ning.metrics.serialization.event.SmileBucketEvent
  */
 public class SmileEnvelopeEvent implements Event
 {
     // UTF-8 won't work!
     public static final Charset CHARSET = Charset.forName("ISO-8859-1");
 
     public static final Charset NAME_CHARSET = Charset.forName("UTF-8");
 
     protected final static SmileFactory smileFactory = new SmileFactory();
     protected final static JsonFactory jsonFactory = new JsonFactory();
 
     static {
         // yes, full 'compression' by checking for repeating names, short string values:
         smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_NAMES, true);
         smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES, true);
         // and for now let's not mandate header for input
         smileFactory.configure(SmileParser.Feature.REQUIRE_HEADER, false);
     }
 
     private static final ObjectMapper smileObjectMapper = new ObjectMapper(smileFactory);
     private static final ObjectMapper jsonObjectMapper = new ObjectMapper(jsonFactory);
 
     public static final String SMILE_EVENT_DATETIME_TOKEN_NAME = "eventDate";
     public static final String SMILE_EVENT_GRANULARITY_TOKEN_NAME = "eventGranularity";
 
     protected DateTime eventDateTime = null;
     protected String eventName;
     protected Granularity granularity = null;
     protected JsonNode root;
 
     private boolean isPlainJson = false;
 
     @Deprecated
     public SmileEnvelopeEvent()
     {
     }
 
     /**
      * Given a map ("json-like"), create an event with hourly granularity
      *
      * @param eventName     name of the event
      * @param eventDateTime event timestamp
      * @param map           event data
      * @throws IOException generic serialization exception
      */
     public SmileEnvelopeEvent(String eventName, DateTime eventDateTime, HashMap<String, Object> map) throws IOException
     {
         this.eventName = eventName;
         this.eventDateTime = eventDateTime;
         this.granularity = Granularity.HOURLY;
 
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
        JsonGenerator g = factory.createJsonGenerator(stream);
 
         g.writeStartObject();
         g.writeNumberField(SmileEnvelopeEvent.SMILE_EVENT_DATETIME_TOKEN_NAME, eventDateTime.getMillis());
         g.writeStringField(SmileEnvelopeEvent.SMILE_EVENT_GRANULARITY_TOKEN_NAME, granularity.toString());
         for (String key : map.keySet()) {
             g.writeObjectField(key, map.get(key)); // will hopefully do the right thing (e.g. take care of numerics)
         }
         g.writeEndObject();
         g.close(); // important: will force flushing of output, close underlying output stream
 
         setPayloadFromByteArray(stream.toByteArray());
     }
 
     public SmileEnvelopeEvent(String eventName, JsonNode node)
     {
         this(eventName, null, node);
     }
 
     public SmileEnvelopeEvent(String eventName, Granularity granularity, JsonNode node)
     {
         this.eventName = eventName;
         this.root = node;
         this.granularity = granularity;
 
         setEventPropertiesFromNode(node);
     }
 
     public SmileEnvelopeEvent(String eventName, byte[] inputBytes, DateTime eventDateTime, Granularity granularity) throws IOException
     {
         this.eventName = eventName;
         this.eventDateTime = eventDateTime;
         this.granularity = granularity;
         setPayloadFromByteArray(inputBytes);
     }
 
     @Override
     public DateTime getEventDateTime()
     {
         return eventDateTime;
     }
 
     @Override
     public String getName()
     {
         return eventName;
     }
 
     @Override
     public Granularity getGranularity()
     {
         return granularity;
     }
 
     @Override
     public String getVersion()
     {
         // TODO Not sure how to version these schemata. Need more thinking here.
         return "1";
     }
 
     @Override
     public String getOutputDir(String prefix)
     {
         GranularityPathMapper pathMapper = new GranularityPathMapper(String.format("%s/%s", prefix, eventName), granularity);
 
         return pathMapper.getPathForDateTime(getEventDateTime());
     }
 
     /**
      * @return a JsonNode representation of a SMILE event (json)
      */
     @Override
     public Object getData()
     {
         return root;
     }
 
     public boolean isPlainJson()
     {
         return isPlainJson;
     }
 
     public void setPlainJson(boolean plainJson)
     {
         isPlainJson = plainJson;
     }
 
     public ObjectMapper getObjectMapper()
     {
         if (isPlainJson()) {
             return jsonObjectMapper;
         }
         else {
             return smileObjectMapper;
         }
     }
 
     @Override
     public byte[] getSerializedEvent()
     {
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 
         try {
             JsonGenerator gen = getObjectMapper().getJsonFactory().createJsonGenerator(outStream, JsonEncoding.UTF8);
             getObjectMapper().writeTree(gen, root);
             gen.close();
         }
         catch (IOException e) {
             return null;
         }
 
         return outStream.toByteArray();
     }
 
     /**
      * The object implements the writeExternal method to save its contents
      * by calling the methods of DataOutput for its primitive values or
      * calling the writeObject method of ObjectOutput for objects, strings,
      * and arrays.
      *
      * @param out the stream to write the object to
      * @throws java.io.IOException Includes any I/O exceptions that may occur
      * @serialData Overriding methods should use this tag to describe
      * the data layout of this Externalizable object.
      * List the sequence of element types and, if possible,
      * relate the element to a public/protected field and/or
      * method of this Externalizable class.
      */
     @Override
     public void writeExternal(ObjectOutput out) throws IOException
     {
         // Name of the event
         byte[] eventNameBytes = eventName.getBytes(NAME_CHARSET);
         out.writeInt(eventNameBytes.length);
         out.write(eventNameBytes);
 
         byte[] payloadBytes = getSerializedEvent();
 
         // Size of Smile payload. Needed for deserialization, see below
         out.writeInt(payloadBytes.length);
 
         out.write(payloadBytes);
     }
 
     /**
      * The object implements the readExternal method to restore its
      * contents by calling the methods of DataInput for primitive
      * types and readObject for objects, strings and arrays.  The
      * readExternal method must read the values in the same sequence
      * and with the same types as were written by writeExternal.
      *
      * @param in the stream to read data from in order to restore the object
      * @throws java.io.IOException    if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     *                                restored cannot be found.
      */
     @Override
     public void readExternal(ObjectInput in) throws IOException
     {
         // Name of the event first
         int smileEventNameBytesSize = in.readInt();
         byte[] eventNameBytes = new byte[smileEventNameBytesSize];
         in.readFully(eventNameBytes);
         eventName = new String(eventNameBytes, NAME_CHARSET);
 
         // Then payload
         int smilePayloadSize = in.readInt();
         byte[] smilePayload = new byte[smilePayloadSize];
         in.readFully(smilePayload);
 
         setPayloadFromByteArray(smilePayload);
 
         setEventPropertiesFromNode(root);
     }
 
     private void setEventPropertiesFromNode(JsonNode node)
     {
         eventDateTime = getEventDateTimeFromJson(node);
 
         if (granularity == null) {
             granularity = getGranularityFromJson(node);
         }
     }
 
     public static DateTime getEventDateTimeFromJson(JsonNode node)
     {
         JsonNode eventDateTimeNode = node.path(SMILE_EVENT_DATETIME_TOKEN_NAME);
 
         DateTime nodeDateTime = new DateTime();
         if (!eventDateTimeNode.isMissingNode()) {
             nodeDateTime = new DateTime(eventDateTimeNode.getLongValue());
         }
 
         return nodeDateTime;
     }
 
     public static Granularity getGranularityFromJson(JsonNode node)
     {
         JsonNode granularityNode = node.path(SMILE_EVENT_GRANULARITY_TOKEN_NAME);
 
         Granularity nodeGranularity = Granularity.HOURLY;
         if (!granularityNode.isMissingNode()) {
             try {
                 nodeGranularity = Granularity.valueOf(granularityNode.getValueAsText());
             }
             catch (IllegalArgumentException e) {
                 nodeGranularity = null;
             }
         }
 
         return nodeGranularity;
     }
 
     private void setPayloadFromByteArray(byte[] smilePayload) throws IOException
     {
         JsonParser jp = getObjectMapper().getJsonFactory().createJsonParser(smilePayload);
         root = getObjectMapper().readTree(jp);
         jp.close();
     }
 
     @Override
     public String toString()
     {
         return root.toString();
     }
 }
