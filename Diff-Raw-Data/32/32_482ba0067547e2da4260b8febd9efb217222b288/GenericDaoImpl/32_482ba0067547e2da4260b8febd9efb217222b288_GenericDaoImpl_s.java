 package org.imirsel.nema.dao.impl;
 
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.LockMode;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Example;
 
 import org.imirsel.nema.dao.GenericDao;
 
 import org.springframework.dao.DataAccessException;
 import org.springframework.orm.hibernate3.SessionFactoryUtils;
 
 import java.io.Serializable;
 
 import java.lang.reflect.ParameterizedType;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 
 /**
  * Implements the generic CRUD data access operations using Hibernate APIs.
  *
  * <p>To write a DAO, subclass and parameterize this class with your persistent
  * class. Of course, assuming that you have a traditional 1:1 appraoch for
  * Entity:DAO design.</p>
  *
  * <p>You have to inject a current Hibernate <tt>Session</tt> to use a DAO.
  * Otherwise, this generic implementation will use <tt>
  * HibernateUtil.getSessionFactory()</tt> to obtain the curren <tt>Session</tt>.
  * </p>
  *
  * @author Christian Bauer
  * @see HibernateDAOFactory
  */
 abstract public class GenericDaoImpl<T, ID extends Serializable>
       implements GenericDao<T, ID> {
 
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    
    private final Class<T> persistentClass;
 
    private SessionFactory sessionFactory;
    
    private Session managedSession;
 
    //~ Constructors ------------------------------------------------------------
 
    /**
     * TODO: Creates a new {@link GenericDaoImpl} object.
     */
    @SuppressWarnings("unchecked")
    public GenericDaoImpl() {
       this.persistentClass =
          (Class<T>)
          ((ParameterizedType) getClass().getGenericSuperclass())
          .getActualTypeArguments()[0];
    }
 
    //~ Methods -----------------------------------------------------------------
 
    public void setSessionFactory(SessionFactory s) { this.sessionFactory = s; }
    
    public SessionFactory getSessionFactory() { return sessionFactory;}
 
    protected Session getSession() { 
 	   if(managedSession!=null){
 		   return managedSession;
 	   }  else {
 		   return sessionFactory.getCurrentSession();
 	   }
    }
 
    public Class<T> getPersistentClass() { return persistentClass; }
 
    @SuppressWarnings("unchecked")
    public T findById(ID id, boolean lock) throws DataAccessException {
       T entity;
 
       try {
          if (lock) {
             entity =
                (T) getSession().load(
                   getPersistentClass(), id, LockMode.UPGRADE);
          } else {
             entity = (T) getSession().load(getPersistentClass(), id);
          }
       } catch (HibernateException e) {
          throw SessionFactoryUtils.convertHibernateAccessException(e);
       }
 
       return entity;
    }
 
    public List<T> findAll() throws DataAccessException {
       return findByCriteria();
    }
 
    @SuppressWarnings("unchecked")
    public List<T> findByExample(T exampleInstance, String... excludeProperty)
          throws DataAccessException {
       try {
          Criteria crit = getSession().createCriteria(getPersistentClass());
          Example example = Example.create(exampleInstance);
          for (String exclude : excludeProperty) {
             example.excludeProperty(exclude);
          }
          crit.add(example);
          return crit.list();
       } catch (HibernateException e) {
          throw SessionFactoryUtils.convertHibernateAccessException(e);
       }
    }
 
    public T makePersistent(T entity) throws DataAccessException {
       try {
          getSession().saveOrUpdate(entity);
       } catch (HibernateException e) {
          throw SessionFactoryUtils.convertHibernateAccessException(e);
       }
       return entity;
    }
 
    public void makeTransient(T entity) throws DataAccessException {
       getSession().delete(entity);
    }
 
    public void flush() { getSession().flush(); }
 
    public void clear() { getSession().clear(); }
 
    /**
     * Use this inside subclasses as a convenience method.
     *
     * @param criterion TODO: Description of parameter criterion.
     * @return use this inside subclasses as a convenience method.
     * @throws DataAccessException TODO: Description of exception {@link
     * DataAccessException}.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(Criterion... criterion)
          throws DataAccessException {
       try {
          Criteria crit = getSession().createCriteria(getPersistentClass());
          for (Criterion c : criterion) {
             crit.add(c);
          }
          return crit.list();
       } catch (HibernateException e) {
          throw SessionFactoryUtils.convertHibernateAccessException(e);
       }
    }
    
    public void startManagedSession(Session session) {
 	   this.managedSession = session;
    }
    public void endManagedSession() {
 	   managedSession=null;
    }
 }
