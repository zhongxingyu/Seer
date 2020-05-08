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
		String expected = "[{\"label\":\"MONTHLY\",\"id\":10},{\"label\":\"QUARTERLY\",\"id\":20},{\"label\":\"SEMI_ANNUALLY\",\"id\":30},{\"label\":\"ANNUALLY\",\"id\":40}]";
 		JSONAssert.assertEquals(expected, json, false);
 	}
 
 }
