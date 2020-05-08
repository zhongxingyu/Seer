 package es.rchavarria.springmvc.rest;
 
 import static org.junit.Assert.fail;
 import static org.mockito.Mockito.when;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.springframework.http.MediaType;
 import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
 import org.springframework.test.web.servlet.MockMvc;
 
 import es.rchavarria.springmvc.core.services.PropertyService;
 
 public class PropertiesQueriesIntegrationTest {
   
     private MockMvc mockMvc;
 
     @InjectMocks
     PropertiesQueriesController controller;
 
     @Mock
     PropertyService propertyService;
 
     @Before
     public void setup() {
         MockitoAnnotations.initMocks(this);
 
         mockMvc = standaloneSetup(controller)
         		.setMessageConverters(new MappingJackson2HttpMessageConverter())
         		.build();
     }
 
     @Test
     public void testRequestAllPropertiesUsesHttpOK() throws Exception {
         when(propertyService.requestAllProperties()).thenReturn(allProperties());
 
         mockMvc.perform(get("/properties")
             .accept(MediaType.APPLICATION_JSON))
             .andDo(print())
             .andExpect(status().isOk());
     }
 
 	@Test
     public void testRequestAllPropertiesRendersOkAsJSON() throws Exception {
 		when(propertyService.requestAllProperties()).thenReturn(allProperties());
 
         mockMvc.perform(get("/properties")
             .accept(MediaType.APPLICATION_JSON))
             .andDo(print())
             .andExpect(jsonPath("$[0]").value("one"))
 	        .andExpect(jsonPath("$[1]").value("two"))
 	        .andExpect(jsonPath("$[2]").value("three"));
     }
 
     @Test
     public void testRequestAPropertyUsesHttpOK() throws Exception {
         when(propertyService.requestAllProperties()).thenReturn(allProperties());
 
         fail("requesting a single property will return an object with property info: city, address and price");
        
         mockMvc.perform(get("/properties/{id}", "an arbitrary id")
             .accept(MediaType.APPLICATION_JSON))
             .andDo(print())
             .andExpect(status().isOk());
     }
 
 	// fixture method
     private List<String> allProperties() {
 		return Arrays.asList("one", "two", "three");
 	}
 }
