 package com.todo.controller;
 
 import static org.junit.Assert.assertNotNull;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
 import java.util.Arrays;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
 import org.springframework.http.MediaType;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.web.WebAppConfiguration;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.test.web.servlet.MvcResult;
 import org.springframework.test.web.servlet.setup.MockMvcBuilders;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 
 import com.todo.config.DevConfig;
import com.todo.controller.TodoControllerTest.Config;
 import com.todo.domain.Todo;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @WebAppConfiguration
@ContextConfiguration(classes=Config.class)
 @ActiveProfiles("dev")
 @Transactional
 public class TodoControllerTest {
	
	@Configuration
	@EnableWebMvc
	@ComponentScan(basePackageClasses=TodoController.class)
	@Import(DevConfig.class)
	public static class Config{
		
	}
 
 	@Autowired
     private WebApplicationContext wac;
 
     private MockMvc mockMvc;
 
     @Before
     public void setup() {
         this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
     }
     
     @Test
     public void shouldCheckWacIsNotNull(){
     	assertNotNull(wac);
     }
     
 	@Test
 	public void shouldCreateAndReadTheTodo() throws Exception {
 		Todo todo = new Todo("Learning OpenShift",Arrays.asList("cloud"));
 		String json = todo.toJson();
 		MvcResult mvcResult = mockMvc.perform(post("/todos").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isCreated()).andReturn();
 		
 		MvcResult mvcResult2 = mockMvc.perform(get("/todos/{id}", 1).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isFound()).andReturn();
 	}
 
 
 }
