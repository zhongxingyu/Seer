 package edu.uw.zookeeper.clients.trace;
 
 import java.io.IOException;
 import java.util.Map;
 
 import com.fasterxml.jackson.core.JsonGenerationException;
 import com.fasterxml.jackson.core.JsonGenerator;
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.core.JsonToken;
 import com.fasterxml.jackson.databind.DeserializationContext;
 import com.fasterxml.jackson.databind.SerializerProvider;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 
 import edu.uw.zookeeper.common.Factories;
 
 public final class TraceEventHeader extends Factories.Holder<TraceEvent> {
 
     public static TraceEventHeader create(TraceEvent value) {
         return new TraceEventHeader(value);
     }
 
     public static Serializer serializer() {
         return Serializer.create();
     }
 
     public static Deserializer deserializer() {
         return Deserializer.create();
     }
 
     public static Map<TraceEventTag, Class<? extends TraceEvent>> types() {
         ImmutableMap.Builder<TraceEventTag, Class<? extends TraceEvent>> builder = ImmutableMap.builder();
         ImmutableList<Class<? extends TraceEvent>> types = ImmutableList.of(
                 TimestampEvent.class, 
                 ProtocolRequestEvent.class, 
                 ProtocolResponseEvent.class, 
                 OperationEvent.class, 
                LatencyMeasurementEvent.class,
                ThroughputMeasurementEvent.class);
         for (Class<? extends TraceEvent> type: types) {
             builder.put(type.getAnnotation(TraceEventType.class).value(), type);
         }
         return builder.build();
     }
     
     public TraceEventHeader(TraceEvent value) {
         super(value);
     }
 
     public static class Serializer extends ListSerializer<TraceEventHeader> {
     
         public static Serializer create() {
             return new Serializer();
         }
         
         public Serializer() {
             super(TraceEventHeader.class);
         }
     
         @Override
         protected void serializeValue(TraceEventHeader value, JsonGenerator json,
                 SerializerProvider provider) throws IOException,
                 JsonGenerationException {
             TraceEvent event = value.get();
             TraceEventTag tag = event.getTag();
             json.writeNumber(tag.ordinal());
             provider.findValueSerializer(event.getClass(), null).serialize(event, json, provider);
         }
     }
     
     public static class Deserializer extends ListDeserializer<TraceEventHeader> {
 
         public static Deserializer create() {
             return create(types());
         }
         
         public static Deserializer create(Map<TraceEventTag, Class<? extends TraceEvent>> types) {
             return new Deserializer(types);
         }
         
         protected static final TraceEventTag[] EVENT_TAGS = TraceEventTag.values();
         
         private static final long serialVersionUID = -1819939360080426783L;
 
         protected final Map<TraceEventTag, Class<? extends TraceEvent>> types;
 
         public Deserializer(Map<TraceEventTag, Class<? extends TraceEvent>> types) {
             super(TraceEventHeader.class);
             this.types = types;
         }
         
         @Override
         protected TraceEventHeader deserializeValue(JsonParser json,
                 DeserializationContext ctxt) throws IOException {
             JsonToken token = json.getCurrentToken();
             if (token == null) {
                 token = json.nextToken();
                 if (token == null) {
                     return null;
                 }
             }
             
             if (token != JsonToken.VALUE_NUMBER_INT) {
                 throw ctxt.wrongTokenException(json, JsonToken.VALUE_NUMBER_INT, "");
             }
             TraceEventTag tag = EVENT_TAGS[json.getIntValue()];
             json.clearCurrentToken();
             
             Class<? extends TraceEvent> type = types.get(tag);
             if (type == null) {
                 throw ctxt.instantiationException(TraceEventHeader.class, String.valueOf(tag));
             }
             TraceEvent value = (TraceEvent) ctxt.findContextualValueDeserializer(ctxt.constructType(type), null).deserialize(json, ctxt);
 
             return new TraceEventHeader(value);
         }
     }
 }
