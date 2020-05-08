 package com.mycompany.template.api;
 
 import com.mycompany.template.beans.Property;
 import com.mycompany.template.beans.SomeBean;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.GenericType;
 import com.sun.jersey.api.client.WebResource;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.ws.rs.core.MediaType;
 
 import java.util.Date;
 import java.util.List;
 
 import static com.mongodb.util.MyAsserts.assertTrue;
 import static junit.framework.Assert.assertNotNull;
 
 /**
  * Created with IntelliJ IDEA.
  * User: azee
  * Date: 7/12/13
  * Time: 2:20 PM
  */
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath*:web-test-context.xml")
 public class SomeBeanRestTest{
     public static String SERVICE_PATH = "http://localhost:9009/template-api/some-bean";
     private static final String GET_ALL_PATH = "/all";
 
     private void createSomeBeansCollection(){
         Client client = Client.create();
         WebResource webResource = client.resource(SERVICE_PATH);
         webResource.path(SERVICE_PATH)
                 .accept(MediaType.APPLICATION_XML)
                 .put(buildSomeBean());
         webResource.path(SERVICE_PATH)
                 .accept(MediaType.APPLICATION_XML)
                 .put(buildSomeBean());
     }
 
     /**
      * Builds filled SomeBean
      * @return SomeBean
      */
     private SomeBean buildSomeBean(){
         SomeBean someBean = new SomeBean();
         someBean.setTime(new Date().getTime());
         someBean.setTitle("Some Bean Test Title");
         someBean.setDescription("Some Bean Test Description");
 
         Property property = new Property();
         property.setTitle("Property 1");
         property.setKey("prop1");
         property.setValue("p1");
         someBean.getProperties().add(property);
 
         property = new Property();
         property.setTitle("Property 2");
         property.setKey("prop2");
         property.setValue("p2");
         someBean.getProperties().add(property);
         return someBean;
     }
 
 
     @Test
     public void testGetSomeBeans() throws Exception {
         createSomeBeansCollection();
         Client client = Client.create();
         WebResource webResource = client.resource(SERVICE_PATH + GET_ALL_PATH);
        ClientResponse response = webResource.path(SERVICE_PATH + GET_ALL_PATH)
                .accept(MediaType.APPLICATION_XML)
                 .get(ClientResponse.class);
         List<SomeBean> someBeans = response.getEntity(new GenericType<List<SomeBean>>() {
         });
         assertNotNull(someBeans);
         assertTrue(someBeans.size() > 0);
 
         //Remove all created test some beans
         for (SomeBean someBean : someBeans){
             webResource.path(SERVICE_PATH).queryParam("id", someBean.getId()).delete();
         }
     }
 
 }
