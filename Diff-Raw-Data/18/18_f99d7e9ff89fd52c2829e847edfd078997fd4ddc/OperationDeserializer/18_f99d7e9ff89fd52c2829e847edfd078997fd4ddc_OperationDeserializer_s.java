 package gx.realtime.serialize;
 
 import java.io.IOException;
 
 import gx.realtime.operation.*;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.core.JsonToken;
 import com.fasterxml.jackson.databind.DeserializationContext;
 import com.fasterxml.jackson.databind.JsonDeserializer;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class OperationDeserializer extends JsonDeserializer<Operation>
 {
     private final String INVALID_FORMAT = "Invalid operation format";
     private final String UNKNOWN_TYPE = "Unknown operation type";
     
     @Override
     public Operation deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonParseException
     {
         Operation operation = null;
         int operationType = -1;
 
         // Add mapper
         if(jp.getCodec() == null)
             jp.setCodec(new ObjectMapper());
         
         // Check if current token is array start
         if(!jp.getCurrentToken().equals(JsonToken.START_ARRAY))
             throw new JsonParseException(INVALID_FORMAT, jp.getCurrentLocation());
 
         // Read type
         operationType = jp.nextIntValue(-1);
         
         // Read event
         operation = readOperation(jp, operationType);
         
         // AbstractMessage type not recognized
         if(operation == null)
             throw new JsonParseException(UNKNOWN_TYPE, jp.getCurrentLocation());
         
         // Check if next token is array end token
        if(!jp.nextToken().equals(JsonToken.END_ARRAY))
             throw new JsonParseException(INVALID_FORMAT, jp.getCurrentLocation());
         
         return operation;
     }
 
     private Operation readOperation(JsonParser jp, int eventType) throws JsonProcessingException, IOException
     {
         // TODO: Recognize other operation types
         switch(eventType) {
         case 4:
             return jp.readValueAs(CompoundOperation.class);
         case 7:
             return jp.readValueAs(ObjectAddedOperation.class);
         case 8:
             return jp.readValueAs(ValueChangedOperation.class);
         case 9:
             return new AcknowledgementOperation();
         }
         return null;
     }
 }
