 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.imaginea.botbot.server.service;
 
 import com.imaginea.botbot.server.jpa.RecordSession;
 import java.util.Collection;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.Produces;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.WebApplicationException;
 import javax.persistence.NoResultException;
 import javax.persistence.EntityManager;
 import java.util.List;
 import com.imaginea.botbot.server.jpa.RecordEntry;
 import com.imaginea.botbot.server.converter.RecordSessionConverter;
 import com.sun.jersey.api.core.ResourceContext;
 import java.net.URI;
 
 /**
  *
  * @author rohit
  */
 public class RecordSessionResource {
     @Context
     protected com.sun.jersey.api.core.ResourceContext resourceContext;
     @Context
     protected javax.ws.rs.core.UriInfo uriInfo;
     
     protected Long id;
 
     /** Creates a new instance of RecordSessionResource */
     public RecordSessionResource() {
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     /**
      * Get method for retrieving an instance of RecordSession identified by id in XML format.
      *
      * @param id identifier for the entity
      * @return an instance of RecordSessionConverter
      */
     @GET
     @Produces({"application/xml", "application/json"})
     public RecordSessionConverter get(@QueryParam("expandLevel")
             @DefaultValue("1") int expandLevel) {
         PersistenceService persistenceSvc = PersistenceService.getInstance();
         try {
             persistenceSvc.beginTx();
             return new RecordSessionConverter(getEntity(), uriInfo.getAbsolutePath().resolve(URI.create(getEntity().getId().toString())), expandLevel);
         } finally {
             PersistenceService.getInstance().close();
         }
     }
 
     /**
      * Put method for updating an instance of RecordSession identified by id using XML as the input format.
      *
      * @param id identifier for the entity
      * @param data an RecordSessionConverter entity that is deserialized from a XML stream
      */
     @PUT
     @Consumes({"application/xml", "application/json"})
     public void put(RecordSessionConverter data) {
         PersistenceService persistenceSvc = PersistenceService.getInstance();
         try {
             persistenceSvc.beginTx();
             EntityManager em = persistenceSvc.getEntityManager();
             updateEntity(getEntity(), data.resolveEntity(em));
             persistenceSvc.commitTx();
         } finally {
             persistenceSvc.close();
         }
     }
 
     /**
      * Delete method for deleting an instance of RecordSession identified by id.
      *
      * @param id identifier for the entity
      */
     @DELETE
     public void delete() {
         PersistenceService persistenceSvc = PersistenceService.getInstance();
         try {
             persistenceSvc.beginTx();
             deleteEntity(getEntity());
             persistenceSvc.commitTx();
         } finally {
             persistenceSvc.close();
         }
     }
 
     /**
      * Returns an instance of RecordSession identified by id.
      *
      * @param id identifier for the entity
      * @return an instance of RecordSession
      */
     protected RecordSession getEntity() {
         EntityManager em = PersistenceService.getInstance().getEntityManager();
         try {
             return (RecordSession) em.createQuery("SELECT e FROM RecordSession e where e.id = :id").setParameter("id", id).getSingleResult();
         } catch (NoResultException ex) {
             throw new WebApplicationException(new Throwable("Resource for " + uriInfo.getAbsolutePath().resolve(URI.create("")) + " does not exist."), 404);
         }
     }
 
     /**
      * Updates entity using data from newEntity.
      *
      * @param entity the entity to update
      * @param newEntity the entity containing the new data
      * @return the updated entity
      */
     private RecordSession updateEntity(RecordSession entity, RecordSession newEntity) {
         EntityManager em = PersistenceService.getInstance().getEntityManager();
         List<RecordEntry> recordEntries = entity.getRecordEntries();
         List<RecordEntry> recordEntriesNew = newEntity.getRecordEntries();
         entity = em.merge(newEntity);
         for (RecordEntry value : recordEntries) {
             if (!recordEntriesNew.contains(value)) {
                 throw new WebApplicationException(new Throwable("Cannot remove items from records"));
             }
         }
         for (RecordEntry value : recordEntriesNew) {
             if (!recordEntries.contains(value)) {
                 RecordSession oldEntity = value.getRecordSession();
                 value.setRecordSession(entity);
                 if (oldEntity != null && !oldEntity.equals(entity)) {
                     oldEntity.getRecordEntries().remove(value);
                 }
             }
         }
         return entity;
     }
 
     /**
      * Deletes the entity.
      *
      * @param entity the entity to deletle
      */
     private void deleteEntity(RecordSession entity) {
         EntityManager em = PersistenceService.getInstance().getEntityManager();
        if (!entity.getRecordEntries().isEmpty()) {
            throw new WebApplicationException(new Throwable("Cannot delete entity because records is not empty."));
        }
         em.remove(entity);
     }
 
     /**
      * Returns a dynamic instance of RecordEntriesResource used for entity navigation.
      *
      * @param id identifier for the parent entity
      * @return an instance of RecordEntriesResource
      */
     @Path("recordentries/")
     public RecordEntriesResource getRecordEntriesResource() {
         RecordEntriesResourceSub recordEntriesResourceSub = resourceContext.getResource(RecordEntriesResourceSub.class);
         recordEntriesResourceSub.setParent(getEntity());
         return recordEntriesResourceSub;
     }
 
     public static class RecordEntriesResourceSub extends RecordEntriesResource {
 
         private RecordSession parent;
 
         public void setParent(RecordSession parent) {
             this.parent = parent;
         }
 
         @Override
         protected Collection<RecordEntry> getEntities(int start, int max, String query) {
             Collection<RecordEntry> result = new java.util.ArrayList<RecordEntry>();
             int index = 0;
             for (RecordEntry e : parent.getRecordEntries()) {
                 if (index >= start && (index - start) < max) {
                     result.add(e);
                 }
                 index++;
             }
             return result;
         }
     }
 }
