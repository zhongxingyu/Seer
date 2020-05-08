 package com.wishlistery.rs;
 
 import java.net.URI;
 
 import javax.inject.Inject;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriBuilder;
 
 import org.jboss.resteasy.spi.NoLogWebApplicationException;
import org.springframework.beans.BeanUtils;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import com.wishlistery.domain.Wishlist;
 import com.wishlistery.domain.WishlistItem;
 import com.wishlistery.persistence.WishlistRepository;
 
 @Scope("prototype")
 @Component
 @Path("/wishlist")
 public class WishlistWebService {
             
     @Inject
     WishlistRepository wishlistRepo;
     
     @GET
     @Path("{id}")
     @Produces(MediaType.APPLICATION_JSON)
     public Wishlist getWishlistById(@PathParam("id") String id) {
         return findWishlist(id);
     }
     
     @PUT
     @Path("{id}")
     @Produces(MediaType.APPLICATION_JSON)
     public void updateWishlistById(@PathParam("id") String id, Wishlist updated) {
         Wishlist wishlist = findWishlist(id);
         wishlist.setName(updated.getName());
         wishlist.setDescription(updated.getDescription());
         wishlistRepo.save(wishlist);
     }
     
     @POST
     @Path("{id}/view/{viewName}")
     public Response addView(@PathParam("id") String id, @PathParam("viewName") String viewName) {
         Wishlist wishlist = findWishlist(id);
         wishlist.addView(viewName);
         wishlistRepo.save(wishlist);
         return Response.created(null).build();
     }
     
     @DELETE
     @Path("{id}/view/{viewName}")
     public void removeView(@PathParam("id") String id, @PathParam("viewName") String viewName) {
         Wishlist wishlist = findWishlist(id);
         try {
             wishlist.removeView(viewName);
         } catch (IllegalStateException e) {
             throwError(Status.BAD_REQUEST, e.getMessage());
         }
         wishlistRepo.save(wishlist);
     }
     
     @POST
     @Path("{id}/category/{categoryName}")
     public Response addCategory(@PathParam("id") String id, @PathParam("categoryName") String categoryName) {
         Wishlist wishlist = findWishlist(id);
         wishlist.addCategory(categoryName);
         wishlistRepo.save(wishlist);
         return Response.created(null).build();
     }
     
     @DELETE
     @Path("{id}/category/{categoryName}")
     public void removeCategory(@PathParam("id") String id, @PathParam("categoryName") String categoryName) {
         Wishlist wishlist = findWishlist(id);
         try {
             wishlist.removeCategory(categoryName);
         } catch (IllegalStateException e) {
             throwError(Status.BAD_REQUEST, e.getMessage());
         }
         wishlistRepo.save(wishlist);
     }
     
     @POST    
     @Consumes(MediaType.APPLICATION_JSON)
     public Response createWishlist(Wishlist wishlist) {
         wishlist.setId(null); //ensure new
         wishlist.setCategories(null); //must be managed separately
         wishlist.setViews(null); //must be managed separately
         wishlistRepo.save(wishlist);
         return Response.created(URI.create("/wishlist/" + wishlist.getId())).build();
     }
     
     @DELETE
     @Path("{id}")
     public void deleteWishlistById(@PathParam("id") String id) {
         wishlistRepo.delete(id);
     }
     
     @POST
     @Path("{id}/item")
     @Consumes(MediaType.APPLICATION_JSON)
     public Response addWishlistItem(@PathParam("id") String id, WishlistItem item) {
         Wishlist wishlist = findWishlist(id);
         wishlist.addItem(item);
         wishlistRepo.save(wishlist);
         return created("/wishlist/{id}/item/{itemId}", wishlist.getId(), item.getId());
     }
     
     @PUT
     @Path("{id}/item/{itemId}")
     @Consumes(MediaType.APPLICATION_JSON)
     public void updateWishlistItem(@PathParam("id") String id, @PathParam("itemId") int itemId, WishlistItem item) {
         Wishlist wishlist = findWishlist(id);
         WishlistItem savedItem = wishlist.getItem(itemId);
         if (savedItem == null) {
              throwError(Status.NOT_FOUND, "no wishlist item with specified id");
         }
        BeanUtils.copyProperties(item, savedItem, new String[] {"id"});
         wishlistRepo.save(wishlist);        
     }
     
     @DELETE
     @Path("{id}/item/{itemId}")
     public void deleteWishlistItem(@PathParam("id") String id, @PathParam("itemId") int itemId) {
         Wishlist wishlist = findWishlist(id);
         WishlistItem savedItem = wishlist.getItem(itemId);
         if (savedItem != null) {
              wishlist.removeItem(savedItem);
         }
         wishlistRepo.save(wishlist);        
     }
 
     private Wishlist findWishlist(String id) {
         Wishlist wishlist = wishlistRepo.findOne(id);
         if (wishlist == null) {
             throwError(Status.NOT_FOUND, "No wishlist exists with specified id");
         }
         return wishlist;
     }
     
     private void throwError(Status status, String message) {
         throw new NoLogWebApplicationException(Response.status(status).entity(new ErrorResponse(message)).build());
     }
     
     private Response created(String location, Object... pathParams) {
         return Response.created(UriBuilder.fromPath(location).build(pathParams)).build();
     }
 }
