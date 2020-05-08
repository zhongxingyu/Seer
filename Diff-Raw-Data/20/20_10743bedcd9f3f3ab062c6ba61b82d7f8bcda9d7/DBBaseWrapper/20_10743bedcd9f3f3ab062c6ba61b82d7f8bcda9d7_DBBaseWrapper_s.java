 package org.jboss.pressgang.ccms.wrapper.base;
 
 import javax.persistence.EntityManager;
 
 import org.jboss.pressgang.ccms.model.base.AuditedEntity;
 import org.jboss.pressgang.ccms.model.utils.EnversUtilities;
 import org.jboss.pressgang.ccms.provider.DBProviderFactory;
 import org.jboss.pressgang.ccms.wrapper.DBWrapperFactory;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 
 public abstract class DBBaseWrapper<T extends EntityWrapper<T>, U extends AuditedEntity> implements EntityWrapper<T> {
 
     private final DBProviderFactory providerFactory;
     private final DBWrapperFactory wrapperFactory;
     private final boolean isRevision;
     private final Class<U> clazz;
     private final Class<T> wrapperClazz;
 
     public DBBaseWrapper(final DBProviderFactory providerFactory, boolean isRevision, Class<U> clazz) {
         this(providerFactory, isRevision, null, clazz);
     }
 
     public DBBaseWrapper(final DBProviderFactory providerFactory, boolean isRevision, Class<T> wrapperClazz, Class<U> clazz) {
         this.providerFactory = providerFactory;
         this.isRevision = isRevision;
         this.clazz = clazz;
         this.wrapperClazz = wrapperClazz;
         wrapperFactory = providerFactory.getWrapperFactory();
     }
 
     protected DBProviderFactory getDatabaseProvider() {
         return providerFactory;
     }
 
     protected DBWrapperFactory getWrapperFactory() {
         return wrapperFactory;
     }
 
     protected EntityManager getEntityManager() {
         return getDatabaseProvider().getEntityManager();
     }
 
     @Override
     public T clone(boolean deepCopy) {
         throw new UnsupportedOperationException("The clone method has no implementation.");
     }
 
     @Override
     public boolean isRevisionEntity() {
         return isRevision;
     }
 
     protected abstract U getEntity();
 
     @Override
     public Integer getId() {
         return getEntity().getId();
     }
 
     @Override
     public Integer getRevision() {
         return getEntity().getRevision() == null ? EnversUtilities.getLatestRevision(getDatabaseProvider().getEntityManager(),
                 getEntity()).intValue() : getEntity().getRevision().intValue();
     }
 
     @Override
     public CollectionWrapper<T> getRevisions() {
         if (wrapperClazz != null) {
             return getWrapperFactory().createCollection(EnversUtilities.getRevisionEntities(getEntityManager(), getEntity()),
                     clazz, true, wrapperClazz);
         } else {
             return getWrapperFactory().createCollection(EnversUtilities.getRevisionEntities(getEntityManager(), getEntity()),
                     clazz, true);
         }
     }
 
     @Override
     public boolean equals(final Object o) {
         if (o instanceof DBBaseWrapper && getEntity() != null) {
            return getEntity().equals(o);
         } else {
             return super.equals(o);
         }
     }
 
     @Override
     public int hashCode() {
         if (getEntity() != null) {
             return getEntity().hashCode();
         } else {
             return super.hashCode();
         }
     }
 }
