 package bakedbeans.builder.test;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 
 import java.io.IOException;
 
 import org.junit.Test;
 
import bakedbeans.example.BakedConfiguration;
 import bakedbeans.example.PersonDetails;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
 import com.google.common.io.Resources;
 
 public class ConfigurationBuilderYAMLTest {
 
 	private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
 	private static final String EXAMPLE_YAML = "yaml/example.yaml";
 	
 	@Test
 	public void shouldMapPersonDetails() throws JsonParseException, JsonMappingException, IOException {
		BakedConfiguration config= mapper.readValue(Resources.getResource(EXAMPLE_YAML), BakedConfiguration.class);
		PersonDetails person = config.getNick();
 		assertThat(person.getFirst()).isEqualTo("Nick");
 		assertThat(person.getDescription()).isEqualTo("An Amazing Guy");
 		assertThat(person.getLast()).isEqualTo("Klauer");
 	}
	
 }
