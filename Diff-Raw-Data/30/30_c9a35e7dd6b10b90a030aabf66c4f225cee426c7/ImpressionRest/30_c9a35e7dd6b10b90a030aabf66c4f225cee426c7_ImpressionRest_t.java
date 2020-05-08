 package cz.muni.fi.pa165.rest;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import cz.muni.fi.pa165.library.dtos.ImpressionTO;
 import cz.muni.fi.pa165.library.services.ImpressionService;
 import java.net.URI;
 import java.util.List;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import org.springframework.beans.factory.annotation.Autowired;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import org.joda.time.LocalDate;
 
 /**
  *
  * @author Michal
  */
@Path("/")
 public class ImpressionRest {
 
     @Context
     private UriInfo context;
     @Autowired
     private ImpressionService service;
 
     public ImpressionRest() {
     }
 
     @GET
     @Path("impressions/{id}")
     @Produces({MediaType.APPLICATION_JSON})
     public String getById(@PathParam("id") Long id) {
         ImpressionTO i = service.findImpressionById(id);
         Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new JodaTimeSerializer()).create();
         return gson.toJson(i);
     }
 
     @GET
    @Path("/impressions")
     @Produces({MediaType.APPLICATION_JSON})
     public String getAll() {
         List<ImpressionTO> i = service.findAllImpressions();
         Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new JodaTimeSerializer()).create();
         return gson.toJson(i);
     }
 
     @POST
     @Path("impressions")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public Response post(String s) {
         Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new JodaTimeSerializer()).create();
         ImpressionTO i = gson.fromJson(s, ImpressionTO.class);
         i.setId(null);
         service.createImpression(i);
         return Response.created(URI.create(context.getAbsolutePath() + "/" + i.getId())).build();
     }
 
     @PUT
     @Path("impressions")
     @Consumes(MediaType.APPLICATION_JSON)
     public Response putJson(String s) {
         URI uri = context.getAbsolutePath();
         Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new JodaTimeSerializer()).create();
         ImpressionTO i = gson.fromJson(s, ImpressionTO.class);
         service.updateImpression(i);
         Response response = Response.created(uri).build();
         return response;
     }
 
     @DELETE
     @Path("impressions/{id}")
     public void delete(@PathParam("id") Long id) {
         ImpressionTO i = service.findImpressionById(id);
         service.deleteImpression(i);
     }
 }
