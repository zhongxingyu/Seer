 package com.ricardoborillo.test.services.rest;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response.Status;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.springframework.web.context.ContextLoaderListener;
 import org.springframework.web.context.request.RequestContextListener;
 import org.springframework.web.util.Log4jConfigListener;
 
 import com.ricardoborillo.test.services.common.RestResponse;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
 import com.sun.jersey.test.framework.JerseyTest;
 import com.sun.jersey.test.framework.WebAppDescriptor;
 import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
 import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;
 
 public class UsersResourceTest extends JerseyTest
 {
     private WebResource resource;
 
     public UsersResourceTest()
     {
        super(new WebAppDescriptor.Builder("com.ricardoborillo.test.services.rest.v2", "com.ricardoborillo.test.services.rest.v1")
                 .contextParam("contextConfigLocation", "classpath:applicationContext.xml")
                 .contextParam("log4jConfigLocation", "src/main/webapp/WEB-INF/log4j.properties")
                 .contextParam("webAppRootKey", "template-jersey-spring-jpa.root")
                 .contextListenerClass(Log4jConfigListener.class)
                 .contextListenerClass(ContextLoaderListener.class)
                 .requestListenerClass(RequestContextListener.class)
                 .servletClass(SpringServlet.class).build());
 
         this.resource = resource();
     }
 
     @Override
     protected TestContainerFactory getTestContainerFactory()
     {
         return new GrizzlyWebTestContainerFactory();
     }
 
     @Test
    public void current_users() throws FileNotFoundException, IOException
    {
        ClientResponse response = resource.path("/current/users").accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        RestResponse serviceResponse = response.getEntity(RestResponse.class);

        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        Assert.assertTrue(serviceResponse.getSuccess());
        Assert.assertTrue(serviceResponse.getData().isEmpty());
    }
    
    @Test
     public void v1_users() throws FileNotFoundException, IOException
     {
         ClientResponse response = resource.path("/v1/users").get(ClientResponse.class);
         RestResponse serviceResponse = response.getEntity(RestResponse.class);
 
         Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
         Assert.assertTrue(serviceResponse.getSuccess());
         Assert.assertTrue(serviceResponse.getData().isEmpty());
     }
     
     @Test
     public void v2_users() throws FileNotFoundException, IOException
     {
         ClientResponse response = resource.path("/v2/users").get(ClientResponse.class);
         RestResponse serviceResponse = response.getEntity(RestResponse.class);
 
         Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
         Assert.assertTrue(serviceResponse.getSuccess());
         Assert.assertTrue(serviceResponse.getData().isEmpty());
     }       
 }
