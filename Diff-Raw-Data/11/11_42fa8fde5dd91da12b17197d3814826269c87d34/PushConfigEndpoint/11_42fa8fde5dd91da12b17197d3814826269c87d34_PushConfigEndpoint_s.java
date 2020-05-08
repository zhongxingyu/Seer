 package org.jboss.aerogear.aerodoc.rest;
 
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
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
 
 import org.jboss.aerogear.aerodoc.model.Lead;
 import org.jboss.aerogear.aerodoc.model.PushConfig;
 
 @Stateless
 @Path("/pushconfig")
 public class PushConfigEndpoint {
 
 	@PersistenceContext(unitName = "forge-default")
 	private EntityManager em;
 
 	@POST
 	@Consumes("application/json")
 	public Response create(PushConfig entity) {
 		updateActiveState(entity);
 		em.persist(entity);
 		return Response.created(
 				UriBuilder.fromResource(LeadEndpoint.class)
 						.path(String.valueOf(entity.getId())).build()).build();
 	}
 
 	private void updateActiveState(PushConfig entity) {
 		// if the entity is set to active, make sure first to set all other
 		// config to false
 		if (entity.isActive()) {
 			Query query = em
 					.createQuery("update PushConfig p set p.active = false");
 			query.executeUpdate();
 		}
 	}
 
 	@DELETE
 	@Path("/{id:[0-9][0-9]*}")
 	public Response deleteById(@PathParam("id") Long id) {
		Lead entity = em.find(Lead.class, id);
 		if (entity == null) {
 			return Response.status(Status.NOT_FOUND).build();
 		}
 		em.remove(entity);
 		return Response.noContent().build();
 	}
 
 	@GET
 	@Path("/{id:[0-9][0-9]*}")
 	@Produces("application/json")
 	public Response findById(@PathParam("id") Long id) {
 		TypedQuery<PushConfig> findByIdQuery = em.createQuery(
 				"SELECT p FROM PushConfig p WHERE p.id = :entityId",
 				PushConfig.class);
 		findByIdQuery.setParameter("entityId", id);
 		PushConfig entity = findByIdQuery.getSingleResult();
 		if (entity == null) {
 			return Response.status(Status.NOT_FOUND).build();
 		}
 		return Response.ok(entity).build();
 	}
 
 	public PushConfig findActiveConfig() {
 		PushConfig entity = null;
 		TypedQuery<PushConfig> findByIdQuery = em.createQuery(
 				"SELECT p FROM PushConfig p WHERE p.active = true",
 				PushConfig.class);
 		List results = findByIdQuery.getResultList();
 		if (!results.isEmpty()) {
 			entity = findByIdQuery.getSingleResult();
 		}
 		return entity;
 	}
 
 	@GET
 	@Produces("application/json")
 	public List<PushConfig> listAll() {
 		final List<PushConfig> results = em.createQuery(
 				"SELECT p FROM PushConfig p", PushConfig.class).getResultList();
 		return results;
 	}
 
 	@PUT
 	@Path("/{id:[0-9][0-9]*}")
 	@Consumes("application/json")
 	public Response update(@PathParam("id") Long id, PushConfig entity) {
 		updateActiveState(entity);
 
 		entity.setId(id);
 		entity = em.merge(entity);
 		return Response.noContent().build();
 	}
 
 }
