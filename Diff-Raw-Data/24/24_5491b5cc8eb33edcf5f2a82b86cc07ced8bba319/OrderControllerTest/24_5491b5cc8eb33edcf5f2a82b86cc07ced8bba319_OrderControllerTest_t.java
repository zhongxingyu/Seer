 package com.abudko.reseller.huuto.mvc.order;
 
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @WebAppConfiguration
 @ContextConfiguration(locations = { "classpath:/spring/test-webapp-config.xml" })
 public class OrderControllerTest {
 
     private static final String ORDER_PATH = "/order";
 
     private static final String FORWARDED_URL_PATH = "/WEB-INF/views/order/orderSuccess.jsp";
 
     @Autowired
     private WebApplicationContext wac;
 
     private MockMvc mockMvc;
 
     @Before
     public void setup() {
         this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
     }
 
     @Test
     public void testOrder() throws Exception {
        String customerName = "customerName";
        String customerEmail = "customerEmail";
        String text = "text";
        String itemUrl = "itemUrl";

        this.mockMvc
                .perform(
                        post(ORDER_PATH).param("customerName", customerName).param("customerEmail", customerEmail)
                                .param("text", text).param("itemUrl", itemUrl)).andExpect(status().isOk())
                /*.andExpect(model().attribute("items", queryListResponse))*/.andExpect(forwardedUrl(FORWARDED_URL_PATH));
     }
 }
