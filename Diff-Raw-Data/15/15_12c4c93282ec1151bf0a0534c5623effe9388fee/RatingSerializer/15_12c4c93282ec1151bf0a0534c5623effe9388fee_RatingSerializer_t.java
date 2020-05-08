 package ch.hsr.bieridee.serializer;
 
 import java.io.IOException;
 
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.map.JsonSerializer;
 import org.codehaus.jackson.map.SerializerProvider;
 
 import ch.hsr.bieridee.config.Res;
 import ch.hsr.bieridee.models.RatingModel;
 
 /**
  * JSON serializer for action models.
  * 
  */
 public class RatingSerializer extends JsonSerializer<RatingModel> {
 	@Override
 	public void serialize(RatingModel rating, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
 
 		jsonGenerator.writeStartObject();
 		jsonGenerator.writeStringField("type", rating.getType());
 		jsonGenerator.writeStringField("date", rating.getDate().toString());
 		jsonGenerator.writeNumberField("timestamp", rating.getDate().getTime());
 		
 		jsonGenerator.writeObjectFieldStart("beer");
 		jsonGenerator.writeStringField("name", rating.getBeer().getName());
		jsonGenerator.writeStringField("uri", Res.getResourceUri(rating.getBeer()));
 		jsonGenerator.writeEndObject();
 
 		jsonGenerator.writeObjectFieldStart("user");
 		jsonGenerator.writeStringField("user", rating.getUser().getUsername());
		jsonGenerator.writeStringField("uri", Res.getResourceUri(rating.getUser()));
 		jsonGenerator.writeEndObject();
 
 		jsonGenerator.writeNumberField("rating", rating.getRating());
 
 		jsonGenerator.writeEndObject();
 
 	}
 
 	@Override
 	public Class<RatingModel> handledType() {
 		return RatingModel.class;
 	}
 }
