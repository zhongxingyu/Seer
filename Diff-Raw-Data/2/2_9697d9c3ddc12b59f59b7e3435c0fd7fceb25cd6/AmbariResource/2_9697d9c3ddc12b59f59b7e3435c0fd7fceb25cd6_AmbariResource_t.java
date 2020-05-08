 package org.apache.ambari.resources;
 
 import org.apache.ambari.core.*;
 import com.google.common.base.Optional;
 import com.yammer.metrics.annotation.Timed;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import java.util.concurrent.atomic.AtomicLong;
@Path("/ambari")
 @Produces(MediaType.APPLICATION_JSON)
 public class AmbariResource {
   private final String template;
   private final String defaultName;
   private final AtomicLong counter;
 
   public AmbariResource(String template, String defaultName) {
     this.template = template;
     this.defaultName = defaultName;
     this.counter = new AtomicLong();
   }
 
   @GET
   @Timed
   public Saying sayTest(@QueryParam("name") Optional<String> name) {
     return new Saying(counter.incrementAndGet(),
         String.format(template, name.or(defaultName)));
   }
 }
