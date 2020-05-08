 package ch.hsr.bieridee.serializer;
 
 import java.io.IOException;
 
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.map.JsonSerializer;
 import org.codehaus.jackson.map.SerializerProvider;
 
 import ch.hsr.bieridee.config.Res;
 import ch.hsr.bieridee.models.ConsumptionModel;
 
 /**
  * JSON serializer for action models.
  * 
  */
 public class ConsumptionSerializer extends JsonSerializer<ConsumptionModel> {
 	@Override
 	public void serialize(ConsumptionModel consumption, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
 
 		jsonGenerator.writeStartObject();
 		jsonGenerator.writeStringField("type", consumption.getType());
 		jsonGenerator.writeStringField("date", consumption.getDate().toString());
 		jsonGenerator.writeNumberField("timestamp", consumption.getDate().getTime());
 
 		jsonGenerator.writeObjectFieldStart("beer");
 		jsonGenerator.writeStringField("name", consumption.getBeer().getName());
		jsonGenerator.writeStringField("uri", Res.getResourceUri(consumption));
 		jsonGenerator.writeEndObject();
 
 		jsonGenerator.writeObjectFieldStart("user");
 		jsonGenerator.writeStringField("user", consumption.getUser().getUsername());
		jsonGenerator.writeStringField("uri", Res.getResourceUri(consumption));
 		jsonGenerator.writeEndObject();
 
 		jsonGenerator.writeEndObject();
 
 	}
 
 	@Override
 	public Class<ConsumptionModel> handledType() {
 		return ConsumptionModel.class;
 	}
 }
