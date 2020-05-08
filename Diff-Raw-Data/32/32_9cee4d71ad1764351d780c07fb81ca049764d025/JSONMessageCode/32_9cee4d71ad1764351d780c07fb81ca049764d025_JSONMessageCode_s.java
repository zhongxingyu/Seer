 package wad.tukki.models;
 
 import java.io.IOException;
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.map.JsonSerializableWithType;
 import org.codehaus.jackson.map.SerializerProvider;
 import org.codehaus.jackson.map.TypeSerializer;
 
 public enum JSONMessageCode implements JsonSerializableWithType {
     
     // Status
     OK (1),
     NOT_FOUND (2),
     
     // Errors
     GENERAL_ERROR (3),
     INTERNAL_ERROR (4),
    PARSING_ERROR (5),
     VALIDATION_ERROR (6),
     
     // Authentication
     AUTHENTICATION_REQUIRED (7),
     BAD_CREDENTIALS (8),
     AUTHENTICATED (9);
     
     private int code;
     
     JSONMessageCode(int code) {
         this.code = code;
     }
 
     @Override
     public void serializeWithType(JsonGenerator generator, SerializerProvider provider, TypeSerializer serializer) 
                                   throws IOException, JsonProcessingException {
         serialize(generator, provider);
     }
 
     @Override
     public void serialize(JsonGenerator generator, SerializerProvider provider) 
                           throws IOException, JsonProcessingException {
         generator.writeNumber(code);
     }
 }
