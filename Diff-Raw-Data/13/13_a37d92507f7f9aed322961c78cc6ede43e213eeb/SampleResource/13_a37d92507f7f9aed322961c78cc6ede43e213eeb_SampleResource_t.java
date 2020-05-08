 package com.sample.resources;
 
 import java.util.concurrent.atomic.AtomicLong;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
 
 import com.google.common.base.Optional;
 import com.sample.api.Message;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;
 import com.yammer.metrics.annotation.Timed;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Path("/sample")
 @Produces(MediaType.APPLICATION_JSON)
 public class SampleResource {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(SampleResource.class);
 
     private final String template;
     private final String defaultName;
     private final AtomicLong counter;
 
     public SampleResource(String template, String defaultName) {
         this.template = template;
         this.defaultName = defaultName;
         this.counter = new AtomicLong();
     }
 
     @GET
     @Timed
     public Message sayHello(
         @QueryParam("name")
         Optional<String> name) {
        if (name.or(defaultName).equals("fail")) {
            Response resp = new ResponseBuilderImpl().status(500).entity("Error Scenario").build();
            throw new WebApplicationException(resp);
         }
        LOGGER.info("Everything looks good..responding sayHello to {}", name.or(defaultName));
        LOGGER.info("Just Spitting some more logs");
         return new Message(counter.incrementAndGet(), String.format(template, name.or(defaultName)));
     }
 }
