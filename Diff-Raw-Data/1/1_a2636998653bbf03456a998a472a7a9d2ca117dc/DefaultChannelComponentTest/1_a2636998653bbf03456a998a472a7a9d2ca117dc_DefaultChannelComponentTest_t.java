 package org.jembi.openhim;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 
 import org.jembi.openhim.DefaultChannelComponent.URLMapping;
 import org.junit.Test;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 
 public class DefaultChannelComponentTest {
 
 	@Test
 	public void test_findURLMapping_match() throws JsonParseException, JsonMappingException, IOException {
 		DefaultChannelComponent dcc = new DefaultChannelComponent();
 		dcc.readMappings();
 		
 		URLMapping mapping = dcc.findURLMapping("test/sample/123");
 		assertNotNull(mapping);
 		assertEquals("localhost", mapping.getHost());
 		assertEquals("8080", mapping.getPort());
 		
 		mapping = dcc.findURLMapping("test/sample2/123abc/test2");
 		assertNotNull(mapping);
 		assertEquals("localhost", mapping.getHost());
 		assertEquals("8080", mapping.getPort());
 	}
 	
 	@Test
 	public void test_findURLMapping_noMatch() throws JsonParseException, JsonMappingException, IOException {
 		DefaultChannelComponent dcc = new DefaultChannelComponent();
 		dcc.readMappings();
 		
 		URLMapping mapping = dcc.findURLMapping("not/a/match");
 		assertEquals(null, mapping);
 	}
 	
 	@Test
 	public void test_readMappings() throws JsonParseException, JsonMappingException, IOException {
 		DefaultChannelComponent dcc = new DefaultChannelComponent();
 		dcc.readMappings();
 		
 		assertNotNull(DefaultChannelComponent.mappings);
 		
 		URLMapping mapping1 = new URLMapping();
 		mapping1.setHost("localhost");
 		mapping1.setPort("8080");
 		mapping1.setUrlPattern("test/sample/.+");
 		
 		URLMapping mapping2 = new URLMapping();
 		mapping2.setHost("localhost");
 		mapping2.setPort("8080");
 		mapping2.setUrlPattern("test/sample2/.+/test2");
 		
 		URLMapping mapping3 = new URLMapping();
 		mapping3.setHost("localhost");
 		mapping3.setPort("8080");
 		mapping3.setUrlPattern("test/sample/protected");
 		mapping3.setAuthType("basic");
 		mapping3.setUsername("test");
 		mapping3.setPassword("password");
 		
 		URLMapping mapping4 = new URLMapping();
 		mapping4.setHost("localhost");
 		mapping4.setPort("8080");
 		mapping4.setUrlPattern("test/post");
 		
 		assertEquals(true, DefaultChannelComponent.mappings.contains(mapping1));
 		assertEquals(true, DefaultChannelComponent.mappings.contains(mapping2));
 	}
 
 }
