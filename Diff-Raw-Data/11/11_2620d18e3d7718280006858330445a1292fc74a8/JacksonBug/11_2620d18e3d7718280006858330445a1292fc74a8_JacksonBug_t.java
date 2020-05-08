 package test;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.fasterxml.jackson.annotation.JsonAnySetter;
 import com.fasterxml.jackson.annotation.JsonCreator;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.fasterxml.jackson.annotation.JsonUnwrapped;
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import static org.apache.commons.lang3.Validate.notNull;
 
 public class JacksonBug {
 
 	@JsonUnwrapped
 	private final Name name;
 	private final Map<String, String> data;
 	
 	@JsonCreator
 	public JacksonBug(@JsonProperty("first") String first, @JsonProperty("last") String last) {
 		notNull(first);
 		notNull(last);
 		this.name = new Name(first, last);
 		this.data = new HashMap<String, String>();
 	}
 
 	public Name getName() {
 		return name;
 	}
 	
 	public Map<String, String> getData() {
 		return data;
 	}
 	
 	@JsonAnySetter
 	public void addMetadata(String key, String value) {
 		data.put(key, value);
 	}
 
 	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
 		String json = 
 			"{" +
                    "\"one\":\"one\"" +
                    ", \"first\":\"Bob\"" +
                    ", \"two\":\"two\"" +
                    ", \"last\":\"Builder\"" +
			        ", \"three\":\"three\"" +
 			"}";
 		
 		ObjectMapper mapper = new ObjectMapper();
 		JacksonBug bug = mapper.readValue(json, JacksonBug.class);
 		System.out.println(bug.getData());
		if (bug.getData().size() == 3) {
 			System.out.println("SUCCESS!!!");
 		} else {
 			System.out.println("back to the drawing board...");
 		}
 	}
 
 }
