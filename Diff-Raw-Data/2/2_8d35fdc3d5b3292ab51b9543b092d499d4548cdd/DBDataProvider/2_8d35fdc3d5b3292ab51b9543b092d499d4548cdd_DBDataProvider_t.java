 package org.jboss.pressgang.ccms.provider;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityNotFoundException;
 import javax.persistence.PersistenceException;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.validation.ValidationException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.jboss.pressgang.ccms.model.base.AuditedEntity;
 import org.jboss.pressgang.ccms.model.exceptions.CustomConstraintViolationException;
 import org.jboss.pressgang.ccms.model.utils.EnversUtilities;
 import org.jboss.pressgang.ccms.provider.exception.BadRequestException;
 import org.jboss.pressgang.ccms.provider.exception.InternalServerErrorException;
 import org.jboss.pressgang.ccms.provider.exception.NotFoundException;
 import org.jboss.pressgang.ccms.provider.exception.ProviderException;
 import org.jboss.pressgang.ccms.provider.listener.ProviderListener;
 import org.jboss.pressgang.ccms.wrapper.DBWrapperFactory;
 
 public class DBDataProvider extends DataProvider {
     private final EntityManager entityManager;
 
     protected DBDataProvider(final EntityManager entityManager, final DBWrapperFactory wrapperFactory, List<ProviderListener> listeners) {
         super(wrapperFactory, listeners);
         this.entityManager = entityManager;
     }
 
     protected EntityManager getEntityManager() {
         return entityManager;
     }
 
     @Override
     protected DBWrapperFactory getWrapperFactory() {
         return (DBWrapperFactory) super.getWrapperFactory();
     }
 
     protected <T> T getEntity(Class<T> clazz, Integer id) {
         try {
             final T entity = getEntityManager().find(clazz, id);
             if (entity == null) {
                throw new NotFoundException("Unable to find " + clazz.getSimpleName()  + " with id " + id);
             } else {
                 return entity;
             }
         } catch (Exception e) {
             throw handleException(e);
         }
     }
 
     protected <T extends AuditedEntity> T getRevisionEntity(T entity, Integer revision) {
         return getRevisionEntity((Class<T>) entity.getClass(), entity.getId(), revision);
     }
 
     protected <T extends AuditedEntity> T getRevisionEntity(Class<T> entityClass, Integer id, Integer revision) {
         try {
             final T entity =  EnversUtilities.getRevision(getEntityManager(), entityClass, id, revision);
             if (entity == null) {
                 throw new NotFoundException();
             } else {
                 return entity;
             }
         } catch (Exception e) {
             throw handleException(e);
         }
     }
 
     protected <T extends AuditedEntity> List<T> getRevisionList(Class<T> entityClass, Integer id) {
         try {
             final Map<Number, T> revisionMapping = EnversUtilities.getRevisionEntities(getEntityManager(), entityClass, id);
 
             final List<T> revisions = new ArrayList<T>();
             for (final Map.Entry<Number, T> entry : revisionMapping.entrySet()) {
                 revisions.add(entry.getValue());
             }
 
             return revisions;
         } catch (Exception e) {
             throw handleException(e);
         }
     }
 
     protected <T> List<T> executeQuery(final CriteriaQuery<T> query) {
         try {
             return getEntityManager().createQuery(query).getResultList();
         } catch (Exception e) {
             throw handleException(e);
         }
     }
 
     protected RuntimeException handleException(final Exception e) {
         if (e instanceof EntityNotFoundException) {
             return new NotFoundException(e);
         } else if (e instanceof ProviderException) {
             return (ProviderException) e;
         } else if (e instanceof PersistenceException || e instanceof ValidationException || e instanceof
                 CustomConstraintViolationException) {
             return new BadRequestException(e);
         } else {
             return new InternalServerErrorException(e);
         }
     }
 }
