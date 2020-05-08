 package com.jamezp.example.rest;
 
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriBuilder;
 
 import com.jamezp.example.model.Vendor;
 
 /**
  *
  */
 @Stateless
 @Path("/vendors")
 public class VendorEndpoint {
     @PersistenceContext
     private EntityManager em;
 
     @POST
    @Consumes("application/xml")
     public Response create(Vendor entity) {
         em.persist(entity);
         return Response.created(UriBuilder.fromResource(VendorEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
     }
 
     @DELETE
     @Path("/{id:[0-9][0-9]*}")
     public Response deleteById(@PathParam("id") Long id) {
         Vendor entity = em.find(Vendor.class, id);
         if (entity == null) {
             return Response.status(Status.NOT_FOUND).build();
         }
         em.remove(entity);
         return Response.noContent().build();
     }
 
     @GET
     @Path("/{id:[0-9][0-9]*}")
    @Produces("application/xml")
     public Response findById(@PathParam("id") Long id) {
         Vendor entity = em.find(Vendor.class, id);
         if (entity == null) {
             return Response.status(Status.NOT_FOUND).build();
         }
         return Response.ok(entity).build();
     }
 
     @GET
    @Produces("application/xml")
     public List<Vendor> listAll() {
         final List<Vendor> results = em.createQuery("FROM Vendor", Vendor.class).getResultList();
         return results;
     }
 
     @PUT
     @Path("/{id:[0-9][0-9]*}")
    @Consumes("application/xml")
     public Response update(@PathParam("id") Long id, Vendor entity) {
         entity.setId(id);
         entity = em.merge(entity);
         return Response.noContent().build();
     }
 }
