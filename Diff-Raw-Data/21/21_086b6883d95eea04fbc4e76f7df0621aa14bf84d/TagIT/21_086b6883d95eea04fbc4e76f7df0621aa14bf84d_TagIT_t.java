 package com.github.t1.webresource;
 
 import static org.junit.Assert.*;
 
 import javax.ws.rs.*;
 
 import org.jboss.resteasy.client.*;
 import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
 import org.jboss.resteasy.spi.ResteasyProviderFactory;
 import org.junit.Test;
 
 public class TagIT {
     private static final String BASE_URL = PersonWebResourceIT.BASE_URL;
     private static final String XML_HEADER = PersonWebResourceIT.XML_HEADER;
 
     private static final String XML = XML_HEADER + "\n" //
             + "<tag name=\"X\">hiho</tag>\n";
 
     public interface TagService {
         @GET
         @Path("tags")
         @Produces("text/xml")
        public String getTags();
 
         @GET
         @Path("tag/{id}")
         @Produces("text/xml")
        public String getTag(@PathParam("id") long id);
 
         @GET
         @Path("tag/{id}")
         @Produces("text/xml")
        public Tag getTagObject(@PathParam("id") long id);
 
         @POST
         @Path("tags")
         @Consumes("text/xml")
         public ClientResponse<String> createTag(String tag);
 
         @PUT
         @Path("tag/{id}")
         @Consumes("text/xml")
         @Produces("text/xml")
         public String updateTag(@PathParam("id") long id, String tag);
 
         @DELETE
         @Path("tag/{id}")
         @Produces("text/xml")
         public String deleteTag(@PathParam("id") long id);
     }
 
     static {
         RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
     }
 
     private static final TagService client = ProxyFactory.create(TagService.class, BASE_URL);
 
     @Test
     public void integrationTest() {
         String initial = client.getTags();
         long created = create();
         assertFalse(initial.equals(client.getTags()));
         assertEquals(XML_HEADER + "<tag id=\"" + created + "\" name=\"X\">hiho</tag>", client.getTag(created));
         client.deleteTag(created);
         assertEquals(initial, client.getTags());
     }
 
     private static long create() {
         ClientResponse<String> result = client.createTag(XML);
         result.getEntity();
         String location = result.getHeaders().getFirst("Location");
         assert location.startsWith(BASE_URL);
         location = location.substring(BASE_URL.length());
         assert location.startsWith("tag/");
         location = location.substring(4);
         return Long.valueOf(location);
     }
 }
