 package com.tjh.swivel.controller;
 
import com.sun.jersey.multipart.FormDataParam;
 import com.tjh.swivel.model.Configuration;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.io.IOException;
import java.io.InputStream;
 import java.io.StringWriter;
 import java.net.URI;
 import java.util.Map;
 
 @Path("config")
 public class ConfigurationResource {
     protected static Logger LOGGER = Logger.getLogger(ConfigurationResource.class);
     private ObjectMapper objectMapper = new ObjectMapper();
     private Configuration configuration;
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public Response getConfigurationResponse() throws IOException {
         StringWriter stringWriter = new StringWriter();
         objectMapper.writeValue(stringWriter, configuration.toMap());
 
         return Response.ok()
                 .type(MediaType.APPLICATION_JSON)
                 .header("Content-Disposition", "attachment; filename=\"swivelConfig.json\"")
                 .entity(stringWriter.toString())
                 .build();
     }
 
    @SuppressWarnings("unchecked")
     @PUT
     @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Map<String, Map<String, Object>> putConfiguration(@FormDataParam("swivelConfig") InputStream inputStream)
             throws IOException {
        Map<String, Map<String, Object>> configMap = objectMapper.readValue(inputStream, Map.class);

         LOGGER.debug("Loading configuration: " + configMap);
         configuration.load(configMap);
         return configuration.toMap();
     }
 
     @DELETE
     @Produces(MediaType.APPLICATION_JSON)
     public Map<String, Map<String, Object>> reset() {
         LOGGER.debug("Resetting Swivel");
         configuration.reset();
 
         return configuration.toMap();
     }
 
     @DELETE
     @Path("path/{localPath: .*}")
     @Produces(MediaType.APPLICATION_JSON)
     public Map<String, Map<String, Object>> deletePath(@PathParam("localPath") URI localUri) {
         LOGGER.debug(String.format("Deleting path %1$s", localUri));
         configuration.removePath(localUri);
 
         return configuration.toMap();
     }
 
     public void setConfiguration(Configuration configuration) {this.configuration = configuration;}
 
     public void setObjectMapper(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }
 }
