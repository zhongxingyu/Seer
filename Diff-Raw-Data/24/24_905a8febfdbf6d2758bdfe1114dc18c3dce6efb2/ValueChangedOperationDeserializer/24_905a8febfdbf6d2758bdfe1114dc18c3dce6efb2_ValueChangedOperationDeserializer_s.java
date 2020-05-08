 package gx.realtime.serialize;
 
 import java.io.IOException;
 
 import com.fasterxml.jackson.core.JsonToken;
 import gx.realtime.operation.ValueChangedOperation;
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.DeserializationContext;
 import com.fasterxml.jackson.databind.JsonDeserializer;
 import com.fasterxml.jackson.databind.JsonNode;
 
 public class ValueChangedOperationDeserializer extends JsonDeserializer<ValueChangedOperation>
 {
     @Override
     public ValueChangedOperation deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException
     {
         String objectId = jp.nextTextValue();
         String key = jp.nextTextValue();
         jp.nextToken();
 
         if(jp.getCurrentToken().equals(JsonToken.END_ARRAY))
         {
             // This is a remove action
             return new ValueChangedOperation(objectId, key, null, null);
         }
 
         JsonNode valueNode = jp.readValueAsTree();
         int valueType = valueNode.get(0).asInt();
        String value = valueNode.get(1).toString();
         return new ValueChangedOperation(objectId, key, valueType, value);
     }
 }
