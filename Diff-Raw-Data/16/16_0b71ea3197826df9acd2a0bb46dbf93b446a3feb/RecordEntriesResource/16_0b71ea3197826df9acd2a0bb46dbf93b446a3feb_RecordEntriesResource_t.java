 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.imaginea.botbot.server.service;
 
 import com.imaginea.botbot.server.jpa.RecordEntry;
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
 import com.imaginea.botbot.server.jpa.RecordSession;
 import com.imaginea.botbot.server.converter.RecordEntriesConverter;
 import com.imaginea.botbot.server.converter.RecordEntryConverter;
 import com.sun.jersey.api.core.ResourceContext;
 import java.net.URI;
 
 /**
  *
  * @author rohit
  */
 @Path("/recordentries/")
 public class RecordEntriesResource {
     @Context
     protected com.sun.jersey.api.core.ResourceContext resourceContext;
     @Context
     protected javax.ws.rs.core.UriInfo uriInfo;
 
     /** Creates a new instance of RecordEntriesResource */
     public RecordEntriesResource() {
     }
 
     /**
      * Get method for retrieving a collection of RecordEntry instance in XML format.
      *
      * @return an instance of RecordEntriesConverter
      */
     @GET
     @Produces({"application/xml", "application/json"})
     public RecordEntriesConverter get(@QueryParam("start")
             @DefaultValue("0") int start, @QueryParam("max")
             @DefaultValue("10") int max, @QueryParam("expandLevel")
             @DefaultValue("1") int expandLevel, @QueryParam("query")
            @DefaultValue("SELECT e FROM RecordEntry e") String query) {
         PersistenceService persistenceSvc = PersistenceService.getInstance();
         try {
             persistenceSvc.beginTx();
             return new RecordEntriesConverter(getEntities(start, max, query), uriInfo.getAbsolutePath().resolve(URI.create("")), expandLevel);
         } finally {
             persistenceSvc.commitTx();
             persistenceSvc.close();
         }
     }
 
     /**
      * Post method for creating an instance of RecordEntry using XML as the input format.
      *
      * @param data an RecordEntryConverter entity that is deserialized from an XML stream
      * @return an instance of RecordEntryConverter
      */
     @POST
     @Consumes({"application/xml", "application/json"})
     public javax.ws.rs.core.Response post(RecordEntryConverter data) {
         PersistenceService persistenceSvc = PersistenceService.getInstance();
         try {
             persistenceSvc.beginTx();
             EntityManager em = persistenceSvc.getEntityManager();
             RecordEntry entity = data.resolveEntity(em);
             createEntity(data.resolveEntity(em));
             persistenceSvc.commitTx();
             //return Response.created(uriInfo.getAbsolutePath().resolve(entity.getId() + "/")).build();
             return Response.created(URI.create(entity.getId().toString())).build();
         } finally {
             persistenceSvc.close();
         }
     }
 
     /**
      * Returns a dynamic instance of RecordEntryResource used for entity navigation.
      *
      * @return an instance of RecordEntryResource
      */
     @Path("{id}/")
     public RecordEntryResource getRecordResource(@PathParam("id") Long id) {
         RecordEntryResource recordResource = resourceContext.getResource(RecordEntryResource.class);
         recordResource.setId(id);
         return recordResource;
     }
 
     /**
      * Returns all the entities associated with this resource.
      *
      * @return a collection of RecordEntry instances
      */
     protected Collection<RecordEntry> getEntities(int start, int max, String query) {
         EntityManager em = PersistenceService.getInstance().getEntityManager();
         return em.createQuery(query).setFirstResult(start).setMaxResults(max).getResultList();
     }
 
     /**
      * Persist the given entity.
      *
      * @param entity the entity to persist
      */
     protected void createEntity(RecordEntry entity) {
         entity.setId(null);
         EntityManager em = PersistenceService.getInstance().getEntityManager();
         em.persist(entity);
         RecordSession recordSession = entity.getRecordSession();
         if (recordSession != null) {
             recordSession.getRecordEntries().add(entity);
         }
     }
 }
