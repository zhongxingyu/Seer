 package net.aequologica.parimagine.jaxrs;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 
 import net.aequologica.parimagine.model.Photo;
 import net.aequologica.parimagine.model.Photos;
 import net.aequologica.parimagine.utils.RequestUtils;
 
 import org.apache.lucene.queryparser.classic.ParseException;
 import org.glassfish.jersey.server.mvc.Viewable;
 
 @javax.ws.rs.Path("/")
 public class Resource {
 	
     @GET
     @javax.ws.rs.Path("/data")
     @Produces(MediaType.APPLICATION_JSON)
     public List<Photo> getPhotos() throws IOException {
         return Photos.getInstance().getPhotos();
     }
 
     @GET
     @javax.ws.rs.Path("/datum/{index: \\d+}")
     @Produces(MediaType.APPLICATION_JSON)
     public Photo getPhoto(@PathParam("index") int index) throws IOException {
         int max = Photos.getInstance().getSize();
         if (index < 0 || max <= index ) {
             throw new WebApplicationException(Response.noContent().build());
         }
         Photo photo = Photos.getInstance().getPhoto(index);
         if (photo == null) {
             throw new WebApplicationException(Response.noContent().build());
         }
         return photo;
     }
 
     @GET
     @javax.ws.rs.Path("{index: \\d+}")
     @Produces(MediaType.APPLICATION_JSON)
     public Viewable getPhotoJsp(@PathParam("index") int index) throws IOException {
     	URI location = UriBuilder
     			.fromUri("../photo.jsp")
                .queryParam("index", index)
     			.build();
     	throw new WebApplicationException(Response.temporaryRedirect(location).build());
     }
 
     @GET
     @javax.ws.rs.Path("/district/{district}/page/{page}/count/{count}")
     @Produces(MediaType.APPLICATION_JSON)
     public List<Photo> getDistrictSlice(
             @PathParam("district") Integer district,
             @PathParam("page") Integer page,
             @PathParam("count") Integer count ) throws IOException {
         Photos photos = Photos.getInstance(); 
         return photos.getDistrictSlice(district, page, count);
     }
 
     @GET
     @javax.ws.rs.Path("/theme/{theme}/page/{page}/count/{count}")
     @Produces(MediaType.APPLICATION_JSON)
     public List<Photo> getThemeSlice( 
     		@PathParam("theme") String theme,
             @PathParam("page") Integer page,
             @PathParam("count") Integer count  ) throws IOException {
         Photos photos = Photos.getInstance(); 
         return photos.getThemeSlice(theme, page, count);
     }
 
     @GET
     @javax.ws.rs.Path("/random/page/{page}/count/{count}")
     @Produces(MediaType.APPLICATION_JSON)
     public List<Photo> getRandomSlice(
     		@PathParam("page") Integer page,
     		@PathParam("count") Integer count  ) throws IOException {
         Photos photos = Photos.getInstance(); 
         return photos.getRandomSlice(page, count);
     }
 
     @GET
     @javax.ws.rs.Path("/random")
     @Produces(MediaType.TEXT_PLAIN)
     public String getRandom(@Context HttpServletRequest request) throws IOException {
         Photos photos = Photos.getInstance(); 
         List<Photo> singleton = photos.getRandomSlice(0, 1);
         return photos.toURL(request, singleton.get(0).getImage());
     }
     
     @GET
     @javax.ws.rs.Path("/search")
     @Produces(MediaType.APPLICATION_JSON)
     public List<Photo> search(
     		@QueryParam("for") String  searchString) throws IOException, ParseException {
         Photos photos = Photos.getInstance(); 
         return photos.search("\""+searchString+"\"", 32);
     }
 
 }
