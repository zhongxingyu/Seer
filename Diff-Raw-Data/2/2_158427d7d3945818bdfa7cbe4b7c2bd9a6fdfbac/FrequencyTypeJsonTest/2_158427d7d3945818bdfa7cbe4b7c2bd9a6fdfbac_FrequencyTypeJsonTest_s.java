 package org.gsoft.openserv.util.time;
 
 import org.json.JSONException;
 import org.junit.Test;
 import org.skyscreamer.jsonassert.JSONAssert;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class FrequencyTypeJsonTest {
 
 	@Test
 	public void testSerialize() throws JsonProcessingException, JSONException {
 		ObjectMapper mapper = new ObjectMapper();
 		String json = mapper.writeValueAsString(FrequencyType.values());
		String expected = "[{\"name\":\"MONTHLY\",\"id\":10},{\"name\":\"QUARTERLY\",\"id\":20},{\"name\":\"SEMI_ANNUALLY\",\"id\":30},{\"name\":\"ANNUALLY\",\"id\":40}]";
 		JSONAssert.assertEquals(expected, json, false);
 	}
 
 }
