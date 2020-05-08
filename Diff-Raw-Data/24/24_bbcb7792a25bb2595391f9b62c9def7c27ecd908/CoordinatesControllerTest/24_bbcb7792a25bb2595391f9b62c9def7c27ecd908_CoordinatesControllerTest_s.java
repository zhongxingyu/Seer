 package net.canadensys.api.narwhal.controller;
 import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;
 
 import java.io.File;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.web.WebAppConfiguration;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.test.web.servlet.setup.MockMvcBuilders;
 import org.springframework.web.context.WebApplicationContext;
 
 /**
  * Tests for API functions regarding Coordinates processing.
  * This won't test the HTML page.
  * @author canadensys
  *
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @WebAppConfiguration
 @ContextConfiguration("classpath:test-dispatcher-servlet.xml")
 public class CoordinatesControllerTest {
 	
 	private static final File EXPECTED_GEOJSON_FILE = new File("src/test/resources/expectedResults/expected_coordinates.json");
 	private static final File EXPECTED_GML_FILE = new File("src/test/resources/expectedResults/expected_coordinates.xml");
 	
 	@Autowired
     private WebApplicationContext wac;
 
     private MockMvc mockMvc;
 
     @Before
     public void setup() {
         this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
     }
 
     @Test
     public void testCoordinatesJSON() throws Exception {
     	//test GET
         this.mockMvc.perform(get("/coordinates.json").param("data","1\t2:3:4N,5:6:7W"))
         	.andExpect(status().isOk())
         	.andExpect(content().encoding("UTF-8"))
         	.andExpect(content().contentType("application/json;charset=UTF-8")) //this is a bug in Spring 3.2, charset should be avoided  .andExpect(content().contentType(MediaType.APPLICATION_JSON))
         	.andExpect(jsonPath("$.features[0].geometry.coordinates[0]").exists());
         
     	//test GET with no ID and tab as coordinates separator. We give no hint about the presence or not of an id.
         this.mockMvc.perform(get("/coordinates.json").param("data","2:3:4N\t5:6:7W"))
         	.andExpect(status().isOk())
         	.andExpect(content().encoding("UTF-8"))
         	.andExpect(content().contentType("application/json;charset=UTF-8")) //this is a bug in Spring 3.2, charset should be avoided  .andExpect(content().contentType(MediaType.APPLICATION_JSON))
         	.andExpect(jsonPath("$.features[0].geometry.coordinates[0]").exists())
         	.andExpect(jsonPath("$.features[0].properties.originalValue").value("2:3:4N\t5:6:7W"));
         
     	//test GET with no ID and tab as coordinates separator but give a hint that an ID is(should be) provided
         //the expected behavior is to NOT return results since 2:3:4N is the id and 5:6:7W is the coordinates (according to what we ask)
         this.mockMvc.perform(get("/coordinates.json").param("data","2:3:4N\t5:6:7W").param("idprovided","true"))
         	.andExpect(status().isOk())
         	.andExpect(content().encoding("UTF-8"))
         	.andExpect(content().contentType("application/json;charset=UTF-8")) //this is a bug in Spring 3.2, charset should be avoided  .andExpect(content().contentType(MediaType.APPLICATION_JSON))
         	.andExpect(jsonPath("$.features[0].geometry.coordinates[0]").doesNotExist());
         
         //test POST
         this.mockMvc.perform(post("/coordinates.json").param("data","1\t2:3:4N,5:6:7W"))
         	.andExpect(status().isOk())
         	.andExpect(content().encoding("UTF-8"))
         	.andExpect(content().contentType("application/json;charset=UTF-8")) //this is a bug in Spring 3.2, charset should be avoided  .andExpect(content().contentType(MediaType.APPLICATION_JSON))
         	.andExpect(jsonPath("$.features[0].geometry.coordinates[0]").exists())
         	.andExpect(jsonPath("$.features[0].properties.originalValue").value("2:3:4N,5:6:7W"));
         
         //test with error
         this.mockMvc.perform(get("/coordinates.json").param("data","12,25"))
         	.andExpect(status().isOk())
         	.andExpect(jsonPath("$.features[0].properties.error").exists());
         
         //test full JSON content
         String expectedGeoJSONContent = FileUtils.readFileToString(EXPECTED_GEOJSON_FILE, Charset.forName("UTF-8"));
         this.mockMvc.perform(get("/coordinates.json").param("data","1\t45° 32' 25\"N, 129.6° W"))
         	.andExpect(content().string(equalToIgnoringWhiteSpace(expectedGeoJSONContent)));
     }
     
     @Test
     public void testCoordinatesXML() throws Exception {
         Map<String,String> namespaces = new HashMap<String, String>();
         namespaces.put("xs", "http://www.w3.org/2001/XMLSchema");
         namespaces.put("gml", "http://www.opengis.net/gml");
         
     	//test GET
         this.mockMvc.perform(get("/coordinates.xml").param("data","1\t2:3:4N,5:6:7W"))
         	.andExpect(status().isOk())
         	.andExpect(content().encoding("UTF-8"))
         	.andExpect(content().contentType("application/xml;charset=UTF-8"))
         	.andExpect(xpath("/gml:FeatureCollection/gml:featureMembers/xs:result/xs:coordinate/gml:Point/gml:pos",
     			namespaces).exists());
         
         //test POST
         this.mockMvc.perform(post("/coordinates.xml").param("data","1\t2:3:4N,5:6:7W"))
         	.andExpect(status().isOk())
         	.andExpect(content().encoding("UTF-8"))
         	.andExpect(content().contentType("application/xml;charset=UTF-8"))
         	.andExpect(xpath("/gml:FeatureCollection/gml:featureMembers/xs:result/xs:coordinate/gml:Point/gml:pos",
         			namespaces).exists());
         
         //test with error
         this.mockMvc.perform(get("/coordinates.xml").param("data","12,25"))
         	.andExpect(status().isOk())
         	.andExpect(xpath("/gml:FeatureCollection/gml:featureMembers/xs:result/xs:error",
     			namespaces).exists());
         
         //test full XML content
         String expectedGMLContent = FileUtils.readFileToString(EXPECTED_GML_FILE,Charset.forName("UTF-8"));
         this.mockMvc.perform(get("/coordinates.xml").param("data","1\t45.5° N, 129.6° W"))
         	.andExpect(content().string(equalToIgnoringWhiteSpace(expectedGMLContent)));
     }
     
     @Test
     public void testCoordinatesJSONP() throws Exception {
     	this.mockMvc.perform(get("/coordinates.json").param("data","1\t2:3:4N,5:6:7W").param("callback", "callme"))
     	.andExpect(status().isOk())
     	.andExpect(content().encoding("UTF-8"))
     	.andExpect(content().contentType("application/x-javascript"));
     }
 }
