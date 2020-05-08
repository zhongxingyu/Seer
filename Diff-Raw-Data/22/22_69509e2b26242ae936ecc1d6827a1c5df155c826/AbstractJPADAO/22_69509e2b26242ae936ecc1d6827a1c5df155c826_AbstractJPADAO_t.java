 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.redshape.persistence.dao.jpa;
 
 import com.redshape.persistence.dao.DAOException;
 import com.redshape.persistence.dao.annotations.QueryHolder;
 import com.redshape.persistence.dao.jpa.executors.CriteriaExecutor;
 import com.redshape.persistence.dao.query.IQuery;
 import com.redshape.persistence.dao.query.IQueryHolder;
 import com.redshape.persistence.dao.query.QueryBuilderException;
 import com.redshape.persistence.dao.query.QueryExecutorException;
 import com.redshape.persistence.entities.IEntity;
 import org.apache.log4j.Logger;
 import org.springframework.beans.BeanUtils;
 import org.springframework.orm.jpa.support.JpaDaoSupport;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.*;
 import java.util.*;
 
 /**
  *
  * @author user
  */
 public abstract class AbstractJPADAO<T extends IEntity> extends JpaDaoSupport implements IJPADAO<T>{
     private static final Logger log = Logger.getLogger(AbstractJPADAO.class);
     private Class<T> entityClass;
     
     private Map<Class<T>, com.redshape.persistence.dao.query.IQueryHolder> queryHolders = new HashMap<Class<T>, IQueryHolder>();
 
     public AbstractJPADAO( Class<T> entityClass) {
         super();
 
         this.entityClass = entityClass;
     }
 
     @PersistenceContext( name = "persistenceManager", type = PersistenceContextType.TRANSACTION )
     protected EntityManager em;
 
 	@Override
     @Transactional
     public void save( Collection<T> list ) throws DAOException {
         for ( T record : list ) {
             this.save(record);
         }
     }
 	
     @Override
     @Transactional
     public T save(T object) throws DAOException {
         return this.em.merge(object);
     }
     
     @Override
     @Transactional
     public T update(T object) throws DAOException {
         if ( object.getId() == null ) {
             throw new DAOException("<null> id provided");
         }
 
         return (T) this.em.find( object.getClass(), object.getId() );
     }
 
     @Override
     @Transactional
     public void update(Collection<T> list ) throws DAOException {
         for ( T item : list ) {
             this.update( item );
         }
     }
     
     @Override
     @Transactional
     public void persist( T record ) throws DAOException {
     	this.em.persist(record);
     }
 
     @Override
     public void removeAll() throws DAOException {
         this.executeUpdate("delete from " + this.getEntityName());
     }
     
     @Override
     @Transactional
     public void remove( Collection<T> objects ) throws DAOException {
         for ( T record : objects ) {
             this.remove(record);
         }
     }
 
     @Transactional
 	public void remove( T object ) throws DAOException {
         if ( !this.em.contains( object ) ) {
             T objInDb = (T) this.em.find( object.getClass(), object.getId() );
             BeanUtils.copyProperties( object, objInDb );
             this.remove(objInDb);
         } else {
             this.em.remove(object);
         }
     }
 
     protected void executeUpdate(final String query) throws DAOException {
         this.executeUpdate(query, new HashMap<String, Object>());
     }
 
     protected void executeUpdate(final String queryString, final Map<String, Object> params) throws DAOException {
 		Query query = this.em.createQuery(queryString);
         for (String key : params.keySet()) {
             query.setParameter(key, params.get(key));
         }
 
         query.executeUpdate();
     }
 
     @Override
     public T findById( final Long id ) throws DAOException {
         try {
             return this.em.find( this.getEntityClass(), id );
        } catch ( EntityNotFoundException e ) {
			return null;
		} catch ( Throwable e ) {
             throw new DAOException( e.getMessage(), e );
         }
     }
 
     
     @Override
     public List<T> executeQuery( IQuery query ) throws DAOException {
         return this.executeQuery(query, new HashMap<String, Object>() );
     }
 
     @Override
     public List<T> executeQuery( IQuery query, Map<String, Object> parameters ) throws DAOException {
         return this.executeQuery( query, parameters, -1, -1 );
     }
 
     @SuppressWarnings("unchecked")
 	@Override
     public List<T> executeQuery( final IQuery query, 
     							 final Map<String, Object> parameters, 
     							 final int offset, 
     							 final int limit ) throws DAOException {
 		if ( parameters != null ) {
 			for ( String key : parameters.keySet() ) {
 				query.setAttribute( key, parameters.get(key) );
 			}
 		}
 
         CriteriaExecutor executor = new CriteriaExecutor( this.em, query);
         
         try {
 	        Query jpaQuery = executor.execute();
 	        if ( offset > 0 ) {
 	        	jpaQuery.setFirstResult( offset );
 	        }
 	        
 	        if ( limit > 0 ) {
 	        	jpaQuery.setMaxResults(limit);
 	        }
 
             try {
 	            return  jpaQuery.getResultList();
             } catch ( EntityNotFoundException e ) {
                 return new ArrayList<T>();
             }
         } catch ( QueryExecutorException e ) {
         	throw new DAOException( "Query execution failed", e );
         }
     }
 
     @Override
     public List<T> executeNamedQuery(String queryName, Map<String, Object> params) throws DAOException {
         return this.executeNamedQuery(queryName, params, -1);
     }
 
     @Override
     public List<T> executeNamedQuery(String queryName, Map<String, Object> params, int limit) throws DAOException {
         return this.executeNamedQuery( queryName, params, limit, -1 );
     }
 
     @Override
     public List<T> executeNamedQuery( final String queryName, final Map<String, Object> params, final int limit, final int offset ) throws DAOException {
         IQueryHolder queryHolder = this.getQueryHolder();
         if ( queryHolder != null && queryHolder.isQueryExists(queryName) ) {
             try {
                 return this.executeQuery( queryHolder.findQuery(queryName), params, limit, offset );
             } catch ( QueryBuilderException e ) {
                 throw new DAOException( e.getMessage(), e );
             }
         }
 
 		Query query = this.em.createNamedQuery(queryName);
         for (String key : params.keySet()) {
             query.setParameter(key, params.get(key));
         }
 
         if (limit > 0 ) {
             query.setMaxResults(limit);
         }
         
         if ( offset > 0 ) {
         	query.setFirstResult(offset);
         }
         
         return query.getResultList();
     }
 
     public IQueryHolder getQueryHolder() throws DAOException {
     	IQueryHolder concreteHolder = this.queryHolders.get( this.getEntityClass() );
     	if ( concreteHolder != null ) {
     		return concreteHolder;
     	}
 
 	    QueryHolder holder = this.getEntityClass().getAnnotation(QueryHolder.class);
 	    if ( holder == null ) {
 		    return null;
 	    }
 
 	    Class<? extends IQueryHolder> value = holder.value();
         try {
             concreteHolder = value.newInstance();
             concreteHolder.init();
             this.queryHolders.put( this.getEntityClass(), concreteHolder );
         } catch (Throwable e) {
         	log.error( e.getMessage(), e );
             throw new DAOException(e.getMessage(), e);
         }
 
         return concreteHolder;
     }
 
     @Override
     public Class<T> getEntityClass() {
         return this.entityClass;
     }
 
     @Override
     public List<T> findAll() throws DAOException {
     	return this.findAll( -1, -1 );
     }
 
     @Override
     public List<T> findAll( int offset ) throws DAOException {
         return this.findAll(offset, -1);
     }
 
     @Override
     public List<T> findAll( final int offset, final int count ) throws DAOException {
     	Query query = this.em.createQuery( String.format("from %s", getEntityName() ) );
 
         if ( offset > 0 ) {
             query.setFirstResult( offset );
         }
 
         if ( count > 0 ) {
             query.setMaxResults(count);
         }

		try {
        	return query.getResultList();
		} catch ( EntityNotFoundException e ) {
			return new ArrayList<T>();
		}
 	}
 
     public static String getEntityName(IEntity entity) {
         return getEntityName(entity.getClass());
     }
 
     public String getEntityName() {
         return getEntityName(this.getEntityClass());
     }
 
     public Long count() throws DAOException {
 		return (Long) this.em.createQuery("select count(*) from " + getEntityName() )
 				 	    .getSingleResult();
     }
 
     public static String getEntityName(Class<? extends IEntity> clazz) {
         javax.persistence.Entity annon = clazz.getAnnotation(javax.persistence.Entity.class);
         String result = clazz.getCanonicalName();
         if (annon != null && annon.name() != null && !annon.name().isEmpty()) {
             result = annon.name();
         } else {
             javax.persistence.Table tableAnnon = clazz.getAnnotation(Table.class);
             if (tableAnnon != null && tableAnnon.name() != null && !tableAnnon.name().isEmpty()) {
                 result = tableAnnon.name();
             }
         }
 
         return result;
     }
 
 }
