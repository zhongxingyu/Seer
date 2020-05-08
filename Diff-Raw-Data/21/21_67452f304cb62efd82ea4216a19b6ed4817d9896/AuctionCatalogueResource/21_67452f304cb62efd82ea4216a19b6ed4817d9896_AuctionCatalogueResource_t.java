 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package admin.src;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.GenericEntity;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import longcat.auction.src.AuctionObject;
 
 /**
  *
  * @author Alexander Lissenko
  */
 
 @Path("auctionobjects")
 public class AuctionCatalogueResource {
     
      private final static Site site = Site.INSTANCE;
     
     private UriInfo uriInfo;
     
     @GET
     @Path("/count")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCount() {
         int a = site.getAuctionCatalogue().getCount();
         PrimitiveJSONWrapper<Integer> wa = new PrimitiveJSONWrapper<Integer>(a);
         return Response.ok(wa).build();
     }
     
     @GET
     @Path("/range") 
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response getRange( @QueryParam("first") String first, 
                                     @QueryParam ("last")String last) {
         List<AuctionObject> al = site.getAuctionCatalogue().getRange(Integer.parseInt(first), Integer.parseInt(last));
         List<AuctionProxy> proxList = new ArrayList<AuctionProxy>();
         for(AuctionObject a: al) {
             AuctionProxy ap = new AuctionProxy(a);
             proxList.add(ap);
         }
         GenericEntity<List<AuctionProxy>> ge = new GenericEntity<List<AuctionProxy>>(proxList){};
         return Response.ok(ge).build();
     }
     
     @PUT
     @Path("{id}")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response editAuction(@FormParam("name") String name, 
             @FormParam("info") String info, @FormParam("price") Double price) {
         try {
             AuctionObject obj = new AuctionObject(name, info, price);
             //AuctionObject obj = new AuctionObject(title, info, price);
             site.getAuctionCatalogue().update(obj);
             AuctionProxy ax = new AuctionProxy(obj);
             return Response.ok(ax).build();
         } catch (IllegalArgumentException e) {
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
     }
     
     @DELETE
     @Path("{id}")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response deleteAuction(@PathParam("id") Long id) {
         try {
             site.getAuctionCatalogue().remove(id);
             return Response.ok().build();
         } catch (IllegalArgumentException e) {
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 }
