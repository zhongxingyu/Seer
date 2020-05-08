 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.imaginea.botbot.server.service;
 
 import com.imaginea.botbot.server.jpa.RecordEntry;
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
 import com.imaginea.botbot.server.jpa.RecordSession;
 import com.imaginea.botbot.server.converter.RecordEntryConverter;
 import com.sun.jersey.api.core.ResourceContext;
 import java.net.URI;
 
 /**
  *
  * @author rohit
  */
 public class RecordEntryResource {
     @Context
     protected com.sun.jersey.api.core.ResourceContext resourceContext;
     @Context
     protected javax.ws.rs.core.UriInfo uriInfo;
     
     protected Long id;
 
     /** Creates a new instance of RecordEntryResource */
     public RecordEntryResource() {
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     /**
      * Get method for retrieving an instance of RecordEntry identified by id in XML format.
      *
      * @param id identifier for the entity
      * @return an instance of RecordEntryConverter
      */
     @GET
     @Produces({"application/xml", "application/json"})
     public RecordEntryConverter get(@QueryParam("expandLevel")
             @DefaultValue("1") int expandLevel) {
         PersistenceService persistenceSvc = PersistenceService.getInstance();
         try {
             persistenceSvc.beginTx();
             return new RecordEntryConverter(getEntity(), uriInfo.getAbsolutePath().resolve(URI.create(getEntity().getId().toString())), expandLevel);
         } finally {
             PersistenceService.getInstance().close();
         }
     }
 
     /**
      * Put method for updating an instance of RecordEntry identified by id using XML as the input format.
      *
      * @param id identifier for the entity
      * @param data an RecordEntryConverter entity that is deserialized from a XML stream
      */
     @PUT
     @Consumes({"application/xml", "application/json"})
     public void put(RecordEntryConverter data) {
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
      * Delete method for deleting an instance of RecordEntry identified by id.
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
      * Returns an instance of RecordEntry identified by id.
      *
      * @param id identifier for the entity
      * @return an instance of RecordEntry
      */
     protected RecordEntry getEntity() {
         EntityManager em = PersistenceService.getInstance().getEntityManager();
         try {
            return (RecordEntry) em.createQuery("SELECT e FROM RecordEntry e where e.id = :id").setParameter("id", id).getSingleResult();
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
     private RecordEntry updateEntity(RecordEntry entity, RecordEntry newEntity) {
         EntityManager em = PersistenceService.getInstance().getEntityManager();
         RecordSession recordSession = entity.getRecordSession();
         RecordSession recordSessionNew = newEntity.getRecordSession();
         entity = em.merge(newEntity);
         if (recordSession != null && !recordSession.equals(recordSessionNew)) {
             recordSession.getRecordEntries().remove(entity);
         }
         if (recordSessionNew != null && !recordSessionNew.equals(recordSession)) {
             recordSessionNew.getRecordEntries().add(entity);
         }
         return entity;
     }
 
     /**
      * Deletes the entity.
      *
      * @param entity the entity to deletle
      */
     private void deleteEntity(RecordEntry entity) {
         EntityManager em = PersistenceService.getInstance().getEntityManager();
         RecordSession recordSession = entity.getRecordSession();
         if (recordSession != null) {
             recordSession.getRecordEntries().remove(entity);
         }
         em.remove(entity);
     }
 
     /**
      * Returns a dynamic instance of RecordSessionResource used for entity navigation.
      *
      * @param id identifier for the parent entity
      * @return an instance of RecordSessionResource
      */
     @Path("recordsession/")
     public RecordSessionResource getRecordSessionResource() {
         RecordSessionResourceSub recordSessionResourceSub = resourceContext.getResource(RecordSessionResourceSub.class);
         recordSessionResourceSub.setParent(getEntity());
         return recordSessionResourceSub;
     }
 
     public static class RecordSessionResourceSub extends RecordSessionResource {
 
         private RecordEntry parent;
 
         public void setParent(RecordEntry parent) {
             this.parent = parent;
         }
 
         @Override
         protected RecordSession getEntity() {
             RecordSession entity = parent.getRecordSession();
             if (entity == null) {
                 throw new WebApplicationException(new Throwable("Resource for " + URI.create("") + " does not exist."), 404);
             }
             return entity;
         }
     }
 }
