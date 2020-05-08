 package dit126.group4.group4shop_app.client;
 
 import dit126.group4.group4shop.core.Product;
 import java.util.ArrayList;
 import java.util.List;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.GenericEntity;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 
 @Path ("products")
 public class ProductCatalogueResource {
     
     @GET
     @Path("all")
     @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
     public Response getAll(){
         List<Product> allP = Group4Shop.INSTANCE.getProductCatalogue().getRange(0,
                 Group4Shop.INSTANCE.getProductCatalogue().getCount());
         List<ProductProxy> ppList = new ArrayList<>();
         for(Product p : allP){
             ppList.add(new ProductProxy(p));
         }
         GenericEntity<List<ProductProxy>> ge = new GenericEntity<List<ProductProxy>>(ppList){};
         return Response.ok(ge).build();
     }
     
     @GET
     @Path("{id}")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response find(@PathParam("id") Long id){
         try{
             Product prod = Group4Shop.INSTANCE.getProductCatalogue().find(id);
             ProductProxy prox = new ProductProxy(prod);
             return Response.ok(prox).status(Status.OK).build();
         }catch(Exception e) {
             return Response.status(Status.BAD_REQUEST).build();
         }
     }
     
     @GET
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response getByName(@QueryParam("name") String name) {
         try{
             List<Product> rtn = Group4Shop.INSTANCE.getProductCatalogue().getByName(name);
             List<ProductProxy> ppList = new ArrayList<>();
             for(Product p : rtn){
                ProductProxy pp = new ProductProxy(p);
                ppList.add(pp);
             }
             GenericEntity<List<ProductProxy>> ge = new GenericEntity<List<ProductProxy>>(ppList){};
             return Response.ok(ge).build();
         } catch(Exception e) {
            return Response.status(Status.GONE).build();
         }
     }
     /*
     @GET
     //@Path("name")
     @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
     public Response getByCategory(@QueryParam("category") String category) {
         try{
             List<Product> rtn = Group4Shop.INSTANCE.getProductCatalogue().getByCategory(category);
             List<ProductProxy> ppList = new ArrayList<>();
             for(Product p : rtn){
                 ppList.add(new ProductProxy(p));
             }
             GenericEntity<List<ProductProxy>> ge = new GenericEntity<List<ProductProxy>>(ppList){};
             return Response.ok(ge).build();
         } catch(Exception e) {
             return Response.status(Status.INTERNAL_SERVER_ERROR).build();
         }
     }*/
     
     @GET
     @Path("range")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response getRange(@QueryParam("fst") int first, @QueryParam("max") int nItems) {
         try{
             List<Product> products = Group4Shop.INSTANCE.getProductCatalogue().getRange(first, nItems);
             ArrayList<ProductProxy> proxyList = new ArrayList<>();
             for (Product p : products){
                 ProductProxy pp = new ProductProxy(p);
                 proxyList.add(pp);
             }
             GenericEntity<List<ProductProxy>> genEnt = new GenericEntity<List<ProductProxy>>(proxyList){};
             return Response.ok(genEnt).build();
             
         }catch(Exception e) {
             return Response.status(Status.BAD_REQUEST).build();
         }
     }
     
     @GET
     @Path("count")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response getCount() {
         try{
             Integer rtn = Group4Shop.INSTANCE.getProductCatalogue().getCount();
             PrimitiveJSONWrapper pjw = new PrimitiveJSONWrapper(rtn);
             return Response.ok(pjw).build();
         } catch (Exception e){
             return Response.status(Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 }
