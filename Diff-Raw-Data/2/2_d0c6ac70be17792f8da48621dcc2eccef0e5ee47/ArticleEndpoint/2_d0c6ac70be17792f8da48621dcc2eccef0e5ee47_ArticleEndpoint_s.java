 package com.abjon.rest;
 
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceContextType;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriBuilder;
 import com.abjon.model.Article;
 
 /**
  * 
  */
 @Stateless
 @Path("/articles")
 public class ArticleEndpoint
 {
    @PersistenceContext
    private EntityManager em;
 
    @POST
    @Consumes("application/json")
    public Response create(Article entity)
    {
 	  System.out.println("Created called!");
       em.persist(entity);
       return Response.created(UriBuilder.fromResource(ArticleEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
    }
 
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id")
    Long id)
    {
 	   System.out.println("Delete called!");
       Article entity = em.find(Article.class, id);
       if (entity == null)
       {
          return Response.status(Status.NOT_FOUND).build();
       }
       em.remove(entity);
       return Response.noContent().build();
    }
 
    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces("application/json")
    public Response findById(@PathParam("id")
    Long id)
    {
 	  System.out.println("Get one called!");
       Article entity = em.find(Article.class, id);
       if (entity == null)
       {
          return Response.status(Status.NOT_FOUND).build();
       }
       return Response.ok(entity).build();
    }
 
    @GET
    @Produces("application/json")
    public List<Article> listAll()
    {
 	  System.out.println("List all called!");
       final List<Article> results = em.createQuery("FROM Article", Article.class).getResultList();
       return results;
    }
 
    @PUT
    @Path("/{id:[0-9][0-9]*}")
    @Consumes("application/json")
    public Response update(@PathParam("id")
    Long id, Article entity)
    {
 	  System.out.println("Update called!"); 
       entity.setId(id);
       entity = em.merge(entity);
       return Response.noContent().build();
    }
 }
