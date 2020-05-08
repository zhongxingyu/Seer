 package org.jcandystore.services;
 
 import org.jcandystore.db.PersistenceService;
 import org.jcandystore.model.Orders;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 
 @Path("/orders")
 public class OrdersService {
     private EntityManager em;
     private PersistenceService ps;
     
     public OrdersService() {
         em = PersistenceService.getInstance().getEntityManager();
         ps = PersistenceService.getInstance();
     }
 
     @POST
     @Consumes({"application/xml", "application/json"})
     public void create(Orders entity) {
         try {
         	ps.beginTx();
             em.persist(entity);
             ps.commitTx();
         } catch (PersistenceException pe) {
             pe.printStackTrace();
             ps.rollbackTx();
         }
     }
 
     @PUT
     @Consumes({"application/xml", "application/json"})
     public void edit(Orders entity) {
         try {
         	ps.beginTx();
         	em.merge(entity);
         	ps.commitTx();
         } catch (PersistenceException pe) {
             pe.printStackTrace();
             ps.rollbackTx();
         }
     }
 
     @DELETE
     @Path("{id}")
     public void remove(@PathParam("id") String id) {
     	try {
     		ps.beginTx();
     		em.remove(em.merge(em.find(Orders.class, id)));
     		ps.commitTx();
     	} catch (Exception e) {
     		e.printStackTrace();
     		ps.rollbackTx();
     	}
     }
 
     @GET
     @Path("{id}")
     @Produces({"application/xml", "application/json"})
     public Orders find(@PathParam("id") String id) {
         Query q = em.createNamedQuery("Orders.findByOrderId")
                .setParameter("orderId", id);
         return (Orders) q.getSingleResult();
     }
 
     @GET
     @Produces({"application/xml", "application/json"})
     public List<Orders> findAll() {
         Query query = em.createNamedQuery("Orders.findAll");
         return query.getResultList();
     }
 
     protected EntityManager getEntityManager() {
         return em;
     }    
     
 }
