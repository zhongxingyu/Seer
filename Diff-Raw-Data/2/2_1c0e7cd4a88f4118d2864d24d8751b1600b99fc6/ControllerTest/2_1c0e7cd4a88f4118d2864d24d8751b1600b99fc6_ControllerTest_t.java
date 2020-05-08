 package org.mailoverlord.server.controllers;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mailoverlord.server.config.EmbeddedDataSourceConfig;
 import org.mailoverlord.server.config.JpaConfig;
 import org.mailoverlord.server.config.TestMailSessionConfig;
 import org.mailoverlord.server.config.WebConfig;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.web.WebAppConfiguration;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.test.web.servlet.setup.MockMvcBuilders;
 import org.springframework.web.context.WebApplicationContext;
 
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
 /**
  * Index Controller Test
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @WebAppConfiguration
 @ContextConfiguration(classes={WebConfig.class, EmbeddedDataSourceConfig.class, JpaConfig.class, TestMailSessionConfig.class})
 public class ControllerTest {
 
     @Autowired
     private WebApplicationContext wac;
 
     private MockMvc mockMvc;
 
     @Before
     public void setup() {
         //this.mockMvc = MockMvcBuilders.standaloneSetup(new IndexController()).build();
         this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
     }
 
     @Test
     public void testIndex() throws Exception {
         mockMvc.perform(get("/")).andExpect(status().isOk());
     }
 
     @Test
     public void testTable() throws Exception {
        mockMvc.perform(get("/messages/list").accept(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType("application/json"));
     }
 
 
 }
