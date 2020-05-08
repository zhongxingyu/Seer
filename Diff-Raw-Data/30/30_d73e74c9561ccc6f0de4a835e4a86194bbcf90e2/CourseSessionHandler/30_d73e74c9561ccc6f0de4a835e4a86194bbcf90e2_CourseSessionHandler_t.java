 package com.lo54project.webservice.handler;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.lo54project.webservice.model.CourseSession;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 public class CourseSessionHandler {
         
         private static ObjectMapper mapper = new ObjectMapper(); 
         private static ClientConfig cConfig = new DefaultClientConfig();
         private static com.sun.jersey.api.client.Client client = com.sun.jersey.api.client.Client.create(cConfig);
         private static WebResource service = client.resource(UriBuilder.fromUri("http://localhost:8080/LO54_Project").build());
         
         public static List<CourseSession> parseCourseSessions() throws JsonProcessingException, UniformInterfaceException, ClientHandlerException, IOException, ParseException{
                 
                 List<CourseSession> courseSession = new ArrayList<CourseSession>();
 
                 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                 
                 JsonNode rootNode = mapper.readTree(service.path("rest").path("coursesessions").accept(MediaType.APPLICATION_JSON).get(String.class));
                 for(JsonNode n : rootNode.path("courseSession")) {
                	//Need the set
                         courseSession.add(new CourseSession(n.path("id").asInt(), formatter.parse(n.path("start").asText()), formatter.parse(n.path("end").asText())));
                 }
                 
                 return courseSession;
         }
         
         public static List<CourseSession> parseCourseSessionsByLocation(String location) throws JsonProcessingException, UniformInterfaceException, ClientHandlerException, IOException, ParseException{
                 
                 List<CourseSession> courseSession = new ArrayList<CourseSession>();
 
                 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                 
                 JsonNode rootNode = mapper.readTree(service.path("rest").path("coursesessions").path("location").path(location).accept(MediaType.APPLICATION_JSON).get(String.class));
                 for(JsonNode n : rootNode.path("courseSession")) {
                         courseSession.add(new CourseSession(n.path("id").asInt(), formatter.parse(n.path("start").asText()), formatter.parse(n.path("end").asText())));
                 }
                 
                 return courseSession;
         }
 
 }
