 package org.gesis.persistence.relational;
 
 import java.lang.reflect.ParameterizedType;
 import java.util.Collections;
 import java.util.List;
 
 import org.gesis.persistence.GenericDAO;
 import org.gesis.persistence.PersistableResource;
 import org.gesis.rdf.LangString;
 import org.hibernate.LockMode;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Example;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.StringUtils;
 
 /**
  * This is an abstract class which defines some necessary attributes and methods
  * that will be used by all Data Access Object-classes implemented in the
  * Hibernate context.
  * 
  * @author matthaeus
  * 
  * @param <T>
  */
 public abstract class GenericHibernateDAO<T> implements GenericDAO<T>
 {
 
 	private static Logger log = LoggerFactory.getLogger( Class.class );
 
 	/**
 	 * Is going to be injected.
 	 */
 	private final SessionFactory sessionFactory;
 	private final HibernateTemplate hibernateTemplate;
 
 	private Class<T> persistenceClass;
 
 	@SuppressWarnings( "unchecked" )
 	public GenericHibernateDAO( final HibernateTemplate hibernateTemplate )
 	{
 		if ( getClass().getGenericSuperclass() instanceof ParameterizedType )
 			this.persistenceClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		else if ( getClass().getSuperclass().getGenericSuperclass() instanceof ParameterizedType )
			this.persistenceClass = (Class<T>) ((ParameterizedType) getClass().getSuperclass().getGenericSuperclass()).getActualTypeArguments()[0];
 		else
 			this.persistenceClass = null;
 
 		this.hibernateTemplate = hibernateTemplate;
 		this.sessionFactory = hibernateTemplate.getSessionFactory();
 	}
 
 	/**
 	 * The hibernate template used by this class.
 	 * 
 	 * @return
 	 */
 	public HibernateTemplate getHibernateTemplate()
 	{
 		return this.hibernateTemplate;
 	}
 
 	/**
 	 * The current session. In most cases <i>null</i> will be returned, since
 	 * the session is maintained by hibernate.
 	 * 
 	 * @return
 	 */
 	protected Session getCurrentSession()
 	{
 		return this.sessionFactory.getCurrentSession();
 	}
 
 	/**
 	 * Returns the generic type parameter, with which this object has been
 	 * instantiated.
 	 * 
 	 * @return
 	 */
 	public Class<T> getPersistenceClass()
 	{
 		return this.persistenceClass;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.gesis.ddi.persistence.dataAccess.GenericDAO#getById(java.lang.String,
 	 * boolean)
 	 */
 	@Override
 	@Transactional
 	public T getById( final String id, final boolean lock )
 	{
 		T entity = getHibernateTemplate().get( getPersistenceClass(), id, lock ? LockMode.READ : LockMode.NONE );
 		return entity;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.gesis.ddi.persistence.dataAccess.GenericDAO#getByURN(java.lang.String)
 	 */
 	@Override
 	@Transactional
 	public <R> R getByURN( final Class<R> clazz, final String urn )
 	{
 		if ( StringUtils.isEmpty( urn ) )
 			return null;
 
 		@SuppressWarnings( "unchecked" )
 		List<R> list = getHibernateTemplate().find( "from " + clazz.getName() + " where urn=?", urn );
 
 		if ( list == null || list.size() == 0 )
 			return null;
 
 		return list.get( 0 );
 	}
 
 	@Override
 	public T getByPrefLabel( final Class<T> clazz, final LangString prefLabel )
 	{
 		if ( prefLabel == null )
 			return null;
 
 		if ( prefLabel.getDe() == null || prefLabel.getEn() == null || prefLabel.getFr() == null )
 			return null;
 
 		@SuppressWarnings( "unchecked" )
 		List<T> list = getHibernateTemplate().find( "FROM " + clazz.getName() + " c WHERE c.prefLabel.de = ? OR c.prefLabel.en = ? OR c.prefLabel.fr = ?", prefLabel.getDe(), prefLabel.getEn(), prefLabel.getFr() );
 
 		if ( list == null || list.size() == 0 )
 			return null;
 
 		return list.get( 0 );
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.gesis.ddi.persistence.dataAccess.GenericDAO#getAll()
 	 */
 	@Override
 	@Transactional
 	public List<T> getAll()
 	{
 		List<T> allObjects = getHibernateTemplate().loadAll( getPersistenceClass() );
 
 		if ( allObjects == null )
 			return Collections.emptyList();
 
 		return allObjects;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.gesis.ddi.persistence.dataAccess.GenericDAO#getByExample(java.lang
 	 * .Object, java.lang.String[])
 	 */
 	@Override
 	@Transactional
 	public List<T> getByExample( final T exampleInstance, final String... excludeProperty )
 	{
 		Example example = Example.create( exampleInstance );
 
 		if ( excludeProperty != null )
 			for ( String property : excludeProperty )
 				example.excludeProperty( property );
 
 		DetachedCriteria criteria = DetachedCriteria.forClass( getPersistenceClass() );
 		criteria.add( example );
 
 		@SuppressWarnings( "unchecked" )
 		List<T> list = getHibernateTemplate().findByCriteria( criteria );
 
 		if ( list == null )
 			return Collections.emptyList();
 
 		return list;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.gesis.ddi.persistence.dataAccess.GenericDAO#persist(java.lang.Object)
 	 */
 	@Override
 	@Transactional
 	public T persist( final T entity )
 	{
 		if ( entity instanceof PersistableResource )
 			log.debug( "Persisting entity with id:" + ((PersistableResource) entity).getId() + " urn:" + ((PersistableResource) entity).getURN() );
 
 		getHibernateTemplate().saveOrUpdate( entity );
 		return entity;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.gesis.ddi.persistence.dataAccess.GenericDAO#delete(java.lang.Object)
 	 */
 	@Override
 	@Transactional
 	public boolean delete( final T entity )
 	{
 		getHibernateTemplate().delete( entity );
 		return true;
 	}
 }
