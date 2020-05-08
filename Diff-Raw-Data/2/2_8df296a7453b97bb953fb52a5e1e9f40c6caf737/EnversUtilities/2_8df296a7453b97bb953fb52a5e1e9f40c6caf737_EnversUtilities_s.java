 package org.jboss.pressgang.ccms.model.utils;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 
 import org.hibernate.envers.AuditReader;
 import org.hibernate.envers.AuditReaderFactory;
 import org.jboss.pressgang.ccms.model.base.AuditedEntity;
 
 public class EnversUtilities {
 
     /**
      * When returning a collection of entity revisions, the lastModified property is set automatically (in the getRevision
      * method). For entities returned from a database query, the last modified date needs to be found manually.
      * 
      * @return either the date saved in the lastModified property, or the latest revision date if lastModified is null
      */
     public static <T extends AuditedEntity<T>> Date getFixedLastModifiedDate(final EntityManager entityManager, final T entity) {
         return entity.getLastModifiedDate() != null ? entity.getLastModifiedDate() : getLatestRevisionDate(entityManager,
                 entity);
     }
 
     /**
      * @return Returns the latest Envers revision number
      */
     public static <T extends AuditedEntity<T>> Date getLatestRevisionDate(final EntityManager entityManager, final T entity) {
         final AuditReader reader = AuditReaderFactory.get(entityManager);
         return reader.getRevisionDate(getLatestRevision(entityManager, entity));
     }
 
     /**
      * @return Returns a collection of revisions
      */
     public static <T extends AuditedEntity<T>> Map<Number, T> getRevisionEntities(final EntityManager entityManager,
             final T entity) {
         final AuditReader reader = AuditReaderFactory.get(entityManager);
         final List<Number> revisions = reader.getRevisions(entity.getClass(), entity.getId());
         Collections.sort(revisions, Collections.reverseOrder());
 
         /* Use a LinkedHashMap to preserver the order */
         final Map<Number, T> retValue = new LinkedHashMap<Number, T>();
         for (final Number revision : revisions)
             retValue.put(revision, getRevision(reader, entity, revision));
 
         return retValue;
     }
 
     /**
      * @return Returns the list of revision numbers for this entity, as maintained by Envers
      */
     public static <T extends AuditedEntity<T>> List<Number> getRevisions(final EntityManager entityManager, final T entity) {
         final AuditReader reader = AuditReaderFactory.get(entityManager);
         final List<Number> retValue = reader.getRevisions(entity.getClass(), entity.getId());
         Collections.sort(retValue, Collections.reverseOrder());
         return retValue;
     }
 
     /**
      * 
      * @param entityManager
      * @param revision
      * @return
      */
     public static <T extends AuditedEntity<T>> T getRevision(final EntityManager entityManager, final T entity,
             final Number revision) {
         final AuditReader reader = AuditReaderFactory.get(entityManager);
         return getRevision(reader, entity, revision);
     }
 
     @SuppressWarnings("unchecked")
     private static <T extends AuditedEntity<T>> T getRevision(final AuditReader reader, final T entity, final Number revision) {
         final T revEntity = (T) reader.find(entity.getClass(), entity.getId(), revision);
         if (revEntity == null)
             return null;
 
         final Date revisionLastModified = reader.getRevisionDate(revision);
         revEntity.setLastModifiedDate(revisionLastModified);
 
         revEntity.setRevision(revision);
 
        return entity;
     }
 
     public static <T extends AuditedEntity<T>> Number getLatestRevision(final EntityManager entityManager, final T entity) {
         final AuditReader reader = AuditReaderFactory.get(entityManager);
         final List<Number> retValue = reader.getRevisions(entity.getClass(), entity.getId());
         Collections.sort(retValue, Collections.reverseOrder());
         return retValue.size() != 0 ? retValue.get(0) : -1;
     }
 }
