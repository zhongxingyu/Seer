 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cs.wintoosa.controller;
 
 import cs.wintoosa.AbstractTest;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.test.context.web.WebAppConfiguration;
 import org.springframework.test.web.servlet.MockMvc;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
 import org.springframework.web.context.WebApplicationContext;
 
 
 /**
  *
  * @author vkukkola
  */
 @WebAppConfiguration
 public class NetworkControllerTest extends AbstractTest{
     @Autowired
     private WebApplicationContext wac;
     private MockMvc mockMvc;
 
     @Before
     public void setup() {
         this.mockMvc = webAppContextSetup(this.wac).build();
         
     }
     
     @Test
     public void testPutNetworkLog() throws Exception {
         
         String jsondata = "{\"operator\":\"test\",\"phoneId\":\"123456789012345\",\"timestamp\":1361264436365,\"checksum\":\"foo\"}";
 
        this.mockMvc.perform(put("/log/network").contentType(MediaType.APPLICATION_JSON).content(jsondata))
                 .andExpect(status().isOk())
                 .andExpect(content().string("true"))
                 .andReturn();
     }
     
     
 }
