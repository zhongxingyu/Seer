 package cz.datalite.dao.impl;
 
 import cz.datalite.dao.DLResponse;
 import cz.datalite.dao.DLSearch;
 import cz.datalite.dao.DLSort;
 import cz.datalite.dao.GenericDAO;
 import org.hibernate.Criteria;
import org.hibernate.LockOptions;
 import org.hibernate.Session;
 import org.hibernate.TransientObjectException;
 import org.hibernate.criterion.*;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.io.Serializable;
 import java.lang.reflect.ParameterizedType;
 import java.util.Iterator;
 import java.util.List;
 
 /*
  * Generic DAO design pattern.
  *
  * Based on Hibernate generic DAO recommendation, additionaly contains methods for DLSearch processing.
  *
  * @author Jiri Bubnik
  */
 @SuppressWarnings( "unchecked" )
 public class GenericDAOImpl<T, ID extends Serializable> implements GenericDAO<T, ID> {
 
     public static final String INVALID_ARGUMENT_ID_MISSING = "Invalid argument in GenericDAO. Id is missing";
     private static final String INVALID_ARGUMENT_ENTITY_MISSING = "Invalid argument in GenericDAO. Entity is missing";
     private static final String INVALID_ARGUMENT_EXAMPLE_MISSING = "Invalid argument in GenericDAO. Example is missing";
     private static final String INVALID_ARGUMENT_SEARCH_OBJECT_MISSING = "Invalid argument in GenericDAO. Search object is missing";
     private final Class<T> persistentClass;
     private EntityManager entityManager;
 
 
     /**
      * Default Constructor - resolves persistent class from generics.
      * 
      * @throws IllegalStateException if no generics is found
      */
     public GenericDAOImpl() {
         ParameterizedType parametrizedType;
 
         if (getClass().getGenericSuperclass() instanceof ParameterizedType) // class
             parametrizedType = (ParameterizedType) getClass().getGenericSuperclass();
         else if (getClass().getGenericSuperclass() instanceof Class) // in case of CGLIB proxy
             parametrizedType = (ParameterizedType) ((Class)getClass().getGenericSuperclass()).getGenericSuperclass();
         else
             throw new IllegalStateException("GenericDAOImpl - class " + getClass() + " is not subtype of ParametrizedType.");
 
 
         this.persistentClass = ( Class<T> ) parametrizedType.getActualTypeArguments()[0];
     }
 
     public GenericDAOImpl( final Class<T> persistentClass ) {
         this.persistentClass = persistentClass;
     }
 
     @PersistenceContext
     public void setEntityManager( final EntityManager entityManager ) {
         this.entityManager = entityManager;
     }
 
     public EntityManager getEntityManager() {
         return entityManager;
     }
 
     public Session getSession() {
         return ( Session ) getEntityManager().getDelegate();
     }
 
     public Class<T> getPersistentClass() {
         return persistentClass;
     }
 
     public T get( final ID id ) {
         assert id != null : INVALID_ARGUMENT_ID_MISSING;
         return ( T ) getSession().get( getPersistentClass(), id );
     }
 
     public T findById( final ID id ) {
         assert id != null : INVALID_ARGUMENT_ID_MISSING;
         return findById( id, false );
     }
 
     public T findById( final ID id, final boolean lock ) {
         assert id != null : INVALID_ARGUMENT_ID_MISSING;
         T entity;
         if ( lock ) {
            entity = ( T ) getSession().load( getPersistentClass(), id, LockOptions.UPGRADE);
         } else {
             entity = ( T ) getSession().load( getPersistentClass(), id );
         }
 
         return entity;
     }
 
     public List<T> findAll() {
         return findByCriteria();
     }
 
     public List<T> findByExample( final T exampleInstance ) {
         assert exampleInstance != null : INVALID_ARGUMENT_EXAMPLE_MISSING;
         return findByExample( exampleInstance, new String[]{} );
     }
 
     public List<T> findByExample( final T exampleInstance, final String[] excludeProperty ) {
         assert exampleInstance != null : INVALID_ARGUMENT_EXAMPLE_MISSING;
         assert excludeProperty != null : "Invalid argument in GenericDAO. PropertyArray is missing";
         final Criteria crit = getSession().createCriteria( getPersistentClass() );
         final Example example = Example.create( exampleInstance );
         for ( String exclude : excludeProperty ) {
             example.excludeProperty( exclude );
         }
         crit.add( example );
         return crit.list();
     }
 
     public T makePersistent( final T entity ) {
         assert entity != null : INVALID_ARGUMENT_ENTITY_MISSING;
         getSession().saveOrUpdate( entity );
         return entity;
     }
 
     public void makeTransient( final T entity ) {
         assert entity != null : INVALID_ARGUMENT_ENTITY_MISSING;
 
         getSession().delete( entity );
     }
 
     public void flush() {
         getSession().flush();
     }
 
     public void clear() {
         getSession().clear();
     }
 
     /**
      * Use this inside subclasses as a convenience method.
      *
      * @param criterion list of criterions
      * @return found entity list
      */
     protected List<T> findByCriteria( final Criterion... criterion ) {
         final Criteria crit = getSession().createCriteria( getPersistentClass() );
         for ( Criterion c : criterion ) {
             crit.add( c );
         }
         return crit.list();
     }
 
     public void reattach( final T entity ) {
         assert entity != null : INVALID_ARGUMENT_ENTITY_MISSING;
        getSession().buildLockRequest(LockOptions.NONE).lock(entity);
     }
 
     public T merge( final T entity ) {
         assert entity != null : INVALID_ARGUMENT_ENTITY_MISSING;
         return ( T ) getSession().merge( entity );
     }
 
     public List<T> search( final DLSearch<T> search ) {
         assert search != null : INVALID_ARGUMENT_SEARCH_OBJECT_MISSING;
 
         // prepare executable criteria
         final Criteria executable = DetachedCriteria.forClass( persistentClass ).getExecutableCriteria( getSession() );
         addSearchCriteria( executable, search, true );
 
         return search( executable, search );
     }
 
     /**
      * Add firstResult/maxResults and distinct flags and executes the query.
      *
      * @param executable
      * @param search
      * @return
      */
     protected List<T> search( final Criteria executable, final DLSearch<T> search ) {
 
         // if row count is defined write it
         if ( search.getRowCount() != 0 ) {
             executable.setMaxResults( search.getRowCount() );
             executable.setFirstResult( search.getFirstRow() );
         }
 
         if ( search.isDistinct() ) {
             executable.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
         }
 
         //execute
         return executable.list();
     }
 
     public Integer count( final DLSearch<T> search ) {
         assert search != null : INVALID_ARGUMENT_SEARCH_OBJECT_MISSING;
 
         // prepare executable criteria
         final Criteria executable = DetachedCriteria.forClass( persistentClass ).getExecutableCriteria( getSession() );
         addSearchCriteria( executable, search, false );
 
         // execute and get row count number
         return count( executable );
     }
 
     /**
      * Adds projection of rowCount and executes the criteria query. Returns count from database or 0.
      *
      * <b>This method modifies the criteria object (adds projection to rowcount).
      * Use after all other criteria operations (e.g. search).</b>
      *
      * @param executable criteria initialized criteria query.
      * @return count from database or 0.
      */
     protected Integer count( final Criteria executable ) {
         // set projection for row count only
         executable.setProjection( Projections.rowCount() );
 
         // clear distinct and pagination flags
         executable.setResultTransformer( Criteria.ROOT_ENTITY );
         executable.setMaxResults( 1 );
         executable.setFirstResult( 0 );
 
 
         final List cnt = executable.list();
 
         // hibernate doesn't throw exception when something goes wrong
         if ( cnt.isEmpty() ) {
             return 0;
         }
         // converts row count number from long to int
         Object result = cnt.get( 0 );
         // result type of count depends on Hibernate version
         if (result instanceof Long)
             return (( Long ) cnt.get( 0 )).intValue();
         else
             return (( Integer ) cnt.get( 0 )).intValue();
     }
 
     /**
      * Method preparse DetachedCriteria from DLSearch using all aliasses,
      * projections, criterions and if writeOrderBy is set then also with
      * all sorts.
      *
      * @param criteria criteria object to add aliases and restrictions
      * @param search search object with data
      * @param writeOrderBy use sorts
      */
     protected void addSearchCriteria( final Criteria criteria, final DLSearch<T> search, final boolean writeOrderBy ) {
         assert search != null : INVALID_ARGUMENT_SEARCH_OBJECT_MISSING;
 
         // add sort aliases
         for ( final Iterator<DLSort> it = search.getSorts(); it.hasNext(); ) {
             final DLSort sort = it.next();
             search.addAliases( sort.getColumn(), Criteria.LEFT_JOIN );
         }
 
         // write aliasses
         for ( DLSearch.Alias alias : search.getAliases() ) {
             criteria.createAlias( alias.getPath(), alias.getAlias(), alias.getJoinType() );
         }
 
         // write projections
         if ( !search.getProjections().isEmpty() ) { // writes list of projections
             final ProjectionList projectionList = Projections.projectionList();
             for ( Projection projection : search.getProjections() ) {
                 projectionList.add( projection );
             }
             criteria.setProjection( projectionList );
         }
 
         // write criterions
         for ( Criterion criterion : search.getCriterions() ) {
             if ( criterion != null ) {
                 criteria.add( criterion );
             }
         }
 
         // write order by
         if ( writeOrderBy ) {
             for ( final Iterator<DLSort> it = search.getSorts(); it.hasNext(); ) {
                 final DLSort sort = it.next();
                 switch ( sort.getSortType() ) {
                     case ASCENDING:
                         criteria.addOrder( Order.asc( search.getAliasForPath( sort.getColumn() ) ) );
                         break;
                     case DESCENDING:
                         criteria.addOrder( Order.desc( search.getAliasForPath( sort.getColumn() ) ) );
                         break;
                     default:
                 }
 
             }
         }
     }
 
     public DLResponse<T> searchAndCount( final DLSearch<T> search ) {
         assert search != null : INVALID_ARGUMENT_SEARCH_OBJECT_MISSING;
         return new DLResponse<T>( search( search ), count( search ) );
     }
 
     /**
      * Execute criteria query - get result list (pagination from search object) and count results).
      *
      * <b>This methods modifies criteria object. Projections and paginations are set. Don't use this object after returning.</b>
      *
      * @param criteria criteria query specification
      * @param search search object (used for pagination and distinct flag)
      *
      * @return result list and count in DLResponse object
      */
     protected DLResponse<T> searchAndCount( final Criteria criteria, final DLSearch<T> search ) {
         return new DLResponse<T>( search( criteria, search ), count( criteria ) );
     }
 
     public boolean isNew( T entity ) {
         assert entity != null : INVALID_ARGUMENT_ENTITY_MISSING;
 
         try {
             getSession().getIdentifier( entity );
             return true;
         } catch ( TransientObjectException ex ) {
             return false;
         }
     }
 
     public T reload( T entity ) {
         assert entity != null : "Invalid argument in GenericDAO reload(). Entity is missing";
         assert !isNew( entity ) : "Invalid argument in GenericDAO reload(). Entity is transient";
 
         return ( T ) getSession().load( entity.getClass(), getSession().getIdentifier( entity ) );
     }
 }
