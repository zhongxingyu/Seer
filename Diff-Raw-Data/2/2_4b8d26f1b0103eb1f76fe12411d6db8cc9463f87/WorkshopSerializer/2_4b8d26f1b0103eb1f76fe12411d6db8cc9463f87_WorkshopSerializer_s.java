 package warsjawa;
 
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.SerializerProvider;
 import org.codehaus.jackson.map.ser.std.SerializerBase;
 import softwareart.booking.Workshop;
 
 import java.io.IOException;
 import java.lang.reflect.Type;
 
 public class WorkshopSerializer extends SerializerBase<Workshop> {
     protected WorkshopSerializer() {
         super(Workshop.class);
     }
 
     @Override
     public void serialize(Workshop workshop, JsonGenerator jgen, SerializerProvider provider) throws IOException {
         jgen.writeStartObject();
         jgen.writeStringField("title", workshop.getTitle());
         jgen.writeNumberField("length", calculateLength(workshop));
         jgen.writeNumberField("id", workshop.getId());
        jgen.writeBooleanField("disabled", workshop.hasFreePlaces());
         jgen.writeEndObject();
     }
 
     private int calculateLength(Workshop workshop) {
         return workshop.getEnd() - workshop.getStart() + 1;
     }
 
     @Override
     public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
         throw new UnsupportedOperationException();
     }
 }
