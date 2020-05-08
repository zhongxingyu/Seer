 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.imaginea.botbot.server.service;
 
 import com.imaginea.botbot.server.jpa.RecordSession;
 import java.util.Collection;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Produces;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import javax.persistence.EntityManager;
 import com.imaginea.botbot.server.jpa.RecordEntry;
 import com.imaginea.botbot.server.converter.RecordSessionsConverter;
 import com.imaginea.botbot.server.converter.RecordSessionConverter;
 import com.sun.jersey.api.core.ResourceContext;
 import java.net.URI;
 
 /**
  *
  * @author rohit
  */
 @Path("/recordsessions/")
 public class RecordSessionsResource {
     @Context
     protected com.sun.jersey.api.core.ResourceContext resourceContext;
     @Context
     protected javax.ws.rs.core.UriInfo uriInfo;
 
     /** Creates a new instance of RecordSessionsResource */
     public RecordSessionsResource() {
     }
 
     /**
      * Get method for retrieving a collection of RecordSession instance in XML format.
      *
      * @return an instance of RecordSessionsConverter
      */
     @GET
     @Produces({"application/xml", "application/json"})
     public RecordSessionsConverter get(@QueryParam("start")
             @DefaultValue("0") int start, @QueryParam("max")
            @DefaultValue("10") int max, @QueryParam("expandLevel")
             @DefaultValue("1") int expandLevel, @QueryParam("query")
             @DefaultValue("SELECT e FROM RecordSession e") String query) {
         PersistenceService persistenceSvc = PersistenceService.getInstance();
         try {
             persistenceSvc.beginTx();
             return new RecordSessionsConverter(getEntities(start, max, query), uriInfo.getAbsolutePath().resolve(URI.create("")), expandLevel);
         } finally {
             persistenceSvc.commitTx();
             persistenceSvc.close();
         }
     }
 
     /**
      * Post method for creating an instance of RecordSession using XML as the input format.
      *
      * @param data an RecordSessionConverter entity that is deserialized from an XML stream
      * @return an instance of RecordSessionConverter
      */
     @POST
     @Consumes({"application/xml", "application/json"})
     public javax.ws.rs.core.Response post(RecordSessionConverter data) {
         PersistenceService persistenceSvc = PersistenceService.getInstance();
         try {
             persistenceSvc.beginTx();
             EntityManager em = persistenceSvc.getEntityManager();
             RecordSession entity = data.resolveEntity(em);
             createEntity(data.resolveEntity(em));
             persistenceSvc.commitTx();
             //return Response.created(uriInfo.getAbsolutePath().resolve(entity.getId() + "/")).build();
             return Response.created(URI.create(entity.getId().toString())).build();
         } finally {
             persistenceSvc.close();
         }
     }
 
     /**
      * Returns a dynamic instance of RecordSessionResource used for entity navigation.
      *
      * @return an instance of RecordSessionResource
      */
     @Path("{id}/")
     public RecordSessionResource getRecordSessionResource(@PathParam("id") Long id) {
         RecordSessionResource recordSessionResource = resourceContext.getResource(RecordSessionResource.class);
         recordSessionResource.setId(id);
         return recordSessionResource;
     }
 
     /**
      * Returns all the entities associated with this resource.
      *
      * @return a collection of RecordSession instances
      */
     protected Collection<RecordSession> getEntities(int start, int max, String query) {
         EntityManager em = PersistenceService.getInstance().getEntityManager();
         return em.createQuery(query).setFirstResult(start).setMaxResults(max).getResultList();
     }
 
     /**
      * Persist the given entity.
      *
      * @param entity the entity to persist
      */
     protected void createEntity(RecordSession entity) {
         entity.setId(null);
         EntityManager em = PersistenceService.getInstance().getEntityManager();
         em.persist(entity);
         for (RecordEntry value : entity.getRecordEntries()) {
             RecordSession oldEntity = value.getRecordSession();
             value.setRecordSession(entity);
             if (oldEntity != null) {
                 oldEntity.getRecordEntries().remove(value);
             }
         }
     }
 }
