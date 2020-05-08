 package com.anasoft.os.daofusion;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceContext;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Projections;
 import org.hibernate.ejb.HibernateEntityManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.anasoft.os.daofusion.criteria.PersistentEntityCriteria;
 import com.anasoft.os.daofusion.entity.Persistable;
 
 /**
  * Base class for persistent entity DAO implementations
  * providing data access through JPA / Hibernate persistence
  * APIs.
  * 
  * @see Persistable
  * 
  * @author vojtech.szocs
  */
 public abstract class BaseHibernateDataAccessor {
 
     private static final Logger LOG = LoggerFactory.getLogger(BaseHibernateDataAccessor.class);
     
 	/**
 	 * Returns an open {@link HibernateEntityManager} instance
 	 * providing access to the Hibernate {@link Session}.
 	 * 
 	 * <p>
 	 * 
 	 * The most convenient method implementation pattern is to rely
 	 * on entity manager instance injection via the {@link PersistenceContext}
 	 * annotation within a JPA persistence context. Alternatively,
 	 * the entity manager instance can be created directly via the
 	 * {@link EntityManagerFactory}.
 	 * 
 	 * @return Open {@link HibernateEntityManager} instance.
 	 */
 	protected abstract HibernateEntityManager getHibernateEntityManager();
 	
 	/**
 	 * Convenience method for retrieving the current {@link Session}
 	 * from the entity manager.
 	 * 
 	 * @return {@link Session} obtained from the entity manager.
 	 */
	protected final Session getSession() {
 	    return getHibernateEntityManager().getSession();
 	}
 	
 	/**
      * Convenience method for retrieving a new {@link Criteria}
      * instance bound to the current {@link Session}.
      * 
      * @param entityClass Persistent entity class for which
      * to create the {@link Criteria} instance.
      * @return New {@link Criteria} instance obtained from
      * the session.
      */
    protected final Criteria getHibernateCriteria(Class<? extends Persistable<? extends Serializable>> entityClass) {
         return getSession().createCriteria(entityClass);
     }
     
     /**
      * Convenience method for counting the number of rows
      * returned by the given <tt>criteria</tt>, based on
      * Hibernate {@link Projections#rowCount rowCount}
      * projection.
      * 
      * <p>
      * 
      * Note that the <tt>criteria</tt> shouldn't contain
      * any paging constraints since the method relies on
      * a result set with its "shape" defined by the
      * projection itself.
      * 
      * @param criteria {@link Criteria} instance for which
      * to perform the row count.
      * @return Number of rows returned by the given
      * <tt>criteria</tt>.
      */
    protected final int rowCount(Criteria criteria) {
         criteria.setProjection(Projections.rowCount());
         
         List<?> projectionResults = criteria.list();
         int rowCount = 0;
         
         Object firstResult = projectionResults.get(0);
        if (projectionResults.size() != 1 || !Integer.class.isAssignableFrom(firstResult.getClass())) {
            LOG.warn("rowCount projection for the given criteria did not result a single integer value, returning zero - did you add unnecessary paging constraints to the criteria?");
         } else {
            rowCount = Integer.class.cast(firstResult).intValue();
         }
         
         return rowCount;
     }
     
     /**
      * Returns the {@link Criteria} instance corresponding to query
      * constraints defined within the <tt>entityCriteria</tt>.
      * 
      * @param entityCriteria {@link PersistentEntityCriteria}
      * instance defining persistent entity query constraints.
      * @param entityClass Persistent entity class for which
      * to create the {@link Criteria} instance.
      * @return {@link Criteria} instance corresponding to query
      * constraints.
      */
     protected Criteria getCriteria(PersistentEntityCriteria entityCriteria, Class<? extends Persistable<? extends Serializable>> entityClass) {
         Criteria criteria = getHibernateCriteria(entityClass);
         entityCriteria.apply(criteria);
         
         return criteria;
     }
 	
 }
