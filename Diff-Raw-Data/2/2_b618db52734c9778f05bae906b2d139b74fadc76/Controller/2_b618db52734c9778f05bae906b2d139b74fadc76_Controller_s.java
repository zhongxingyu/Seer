 /*
  * Controller.java
  *
  * Created on March 28, 2008, 2:00 PM
  *
  */
 package org.j2free.jpa;
 
 import java.io.Serializable;
 
 import java.util.Collection;
 import java.util.List;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 
 import javax.transaction.HeuristicMixedException;
 import javax.transaction.HeuristicRollbackException;
 import javax.transaction.NotSupportedException;
 import javax.transaction.RollbackException;
 import javax.transaction.Status;
 import javax.transaction.SystemException;
 import javax.transaction.UserTransaction;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.queryParser.MultiFieldQueryParser;
 import org.apache.lucene.queryParser.ParseException;
 
 import org.hibernate.CacheMode;
 import org.hibernate.Criteria;
 import org.hibernate.FlushMode;
 import org.hibernate.PropertyValueException;
 import org.hibernate.SQLQuery;
 import org.hibernate.ScrollMode;
 import org.hibernate.ScrollableResults;
 import org.hibernate.Session;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Example;
 import org.hibernate.criterion.NaturalIdentifier;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.ejb.EntityManagerImpl;
 import org.hibernate.exception.ConstraintViolationException;
 import org.hibernate.search.FullTextSession;
 import org.hibernate.search.jpa.FullTextEntityManager;
 import org.hibernate.search.jpa.Search;
 import org.hibernate.validator.InvalidStateException;
 import org.hibernate.validator.InvalidValue;
 
 import org.j2free.util.Pair;
 
 /**
  *
  * @author ryan
  */
 public class Controller {
 
     protected Log LOG = LogFactory.getLog(Controller.class);
 
     public static final String ATTRIBUTE_KEY = "controller";
 
     protected UserTransaction tx;
     protected EntityManager em;
     protected FullTextEntityManager fullTextEntityManager;
 
     private boolean markedForRollback;
 
     protected Throwable problem;
     protected InvalidValue[] errors;
 
     public Controller() throws NamingException {
         markedForRollback = false;
 
         InitialContext ctx = new InitialContext();
         tx = (UserTransaction) ctx.lookup("UserTransaction");
         em = (EntityManager) ctx.lookup("java:comp/env/" + getJndiName());
 
         problem = null;
 
     }
 
     protected String getJndiName() {
         return "persistence/EntityManager";
     }
 
     public UserTransaction getUserTransaction() {
         return tx;
     }
 
     public EntityManager getEntityManager() {
         return em;
     }
 
     public void setEntityManager(EntityManager em) {
         this.em = em;
     }
 
     public void setUserTransaction(UserTransaction tx) {
         this.tx = tx;
     }
 
     public void clear() {
         em.clear();
     }
 
     public void flush() {
         try {
             em.flush();
             problem = null;
         } catch (InvalidStateException ise) {
             this.errors = ise.getInvalidValues();
         }
     }
 
     public void flushAndClear() {
         try {
             em.flush();
             em.clear();
             problem = null;
         } catch (InvalidStateException ise) {
             this.errors = ise.getInvalidValues();
         }
     }
 
     public void markForRollback(boolean markForRollback) {
         this.markedForRollback = markForRollback;
     }
 
     public void setMarkedForRollback(boolean markedForRollback) {
         this.markedForRollback = markedForRollback;
     }
 
     public boolean isMarkedForRollback() {
         return markedForRollback;
     }
 
     public FullTextEntityManager getFullTextEntityManager() {
 
         if (fullTextEntityManager == null || !fullTextEntityManager.isOpen()) {
             fullTextEntityManager = Search.createFullTextEntityManager(getEntityManager());
         }
         return fullTextEntityManager;
     }
 
     public void startTransaction() throws NotSupportedException, SystemException {
         
         tx.begin();
 
         // make sure the entity manager knows the transaction has begun
         em.joinTransaction();
 
         // make sure that a transaction always starts clean
         markedForRollback = false; 
         problem           = null;
         errors            = null;
     }
 
     public void endTransaction() throws SystemException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
         try {
 
             if (markedForRollback) {
                 tx.rollback();
             } else {
                 tx.commit();
             }
 
             problem = null;
             errors = null;
         } catch (InvalidStateException ise) {
             problem = ise;
             this.errors = ise.getInvalidValues();
         }
     }
 
     public boolean isTransactionOpen() throws SystemException {
         return (tx.getStatus() == Status.STATUS_ACTIVE);
     }
 
     public Session getSession() {
         return ((EntityManagerImpl) em.getDelegate()).getSession();
     }
 
     public void setCacheMode(CacheMode mode) throws SystemException {
         if (!isTransactionOpen()) {
             return;
         }
 
         getSession().setCacheMode(mode);
     }
 
     public void evictCollection(String collectionName, Serializable primaryKey) {
         getSession().getSessionFactory().evictCollection(collectionName, primaryKey);
     }
 
     public void evict(Class clazz, Serializable primaryKey) {
         getSession().getSessionFactory().evict(clazz, primaryKey);
     }
 
     public void evictCollection(String collection) {
         getSession().getSessionFactory().evictCollection(collection);
     }
 
     public void evict(Class clazz) {
         getSession().getSessionFactory().evict(clazz);
     }
 
     public Query createQuery(String query) {
         return em.createQuery(query);
     }
 
     public Query createNativeQuery(String query) {
         return em.createNativeQuery(query);
     }
 
     public <T extends Object> T findPrimaryKey(Class<T> entityClass, Object entityId) {
         return (T) em.find(entityClass, entityId);
     }
 
     public <T extends Object> List<T> list(Class<T> entityClass) {
         return list(entityClass, -1, -1);
     }
 
     public <T extends Object> List<T> list(Class<T> entityClass, int start, int limit) {
         Query query = em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e");
 
         if (start > 0) {
             query.setFirstResult(start);
         }
         if (limit > 0) {
             query.setMaxResults(limit);
         }
 
         return (List<T>) query.getResultList();
     }
 
     public <T extends Object> int count(Class<T> entityClass) {
 
         Object o = null;
 
         try {
             Query query = em.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e");
 
             o = query.getSingleResult();
 
             return o == null ? -1 : ((Long) o).intValue();
 
         } catch (ClassCastException cce) {
             return o == null ? -1 : ((java.math.BigInteger) o).intValue();
         }
     }
 
     public <T extends Object> int count(Query query, Pair<String, ? extends Object>... parameters) {
         int count = -1;
         Object o = null;
         if (parameters != null) {
             for (Pair<String, ? extends Object> parameter : parameters) {
                 query.setParameter(parameter.getFirst(), parameter.getSecond());
             }
         }
         try {
             o = query.getSingleResult();
             count = ((Long) o).intValue();
         } catch (ClassCastException cce) {
             count = ((java.math.BigInteger) o).intValue();
         }
         return count;
     }
 
     public int count(String queryString, Pair<String, ? extends Object>... parameters) {
         return count(em.createQuery(queryString), parameters);
     }
 
     public <T extends Object> int namedCount(Class<T> entityClass, String namedQuery,
                                              Pair<String, ? extends Object>... parameters) {
         return count(em.createNamedQuery(entityClass.getSimpleName() + "." + namedQuery), parameters);
     }
 
     public <T extends Object> T proxy(Class<T> entityClass, Object entityId) {
         return (T) em.getReference(entityClass, entityId);
     }
 
     public <T extends Object> T merge(T entity) {
         entity = (T) em.merge(entity);
         return entity;
     }
 
     public <T extends Object> void refresh(T entity) {
         em.refresh(entity);
     }
 
     public <T extends Object> void remove(Class<T> entityClass, Object entityId) {
         T entity = (T) findPrimaryKey(entityClass, entityId);
 
         if (entity == null)
             throw new IllegalStateException("Error removing " + entityClass.getSimpleName() + " with id = " + entityId + ". Entity not found!");
 
         em.remove(entity);
     }
 
     public <T extends Object> void remove(T entity) {
 
         if (entity == null)
             throw new IllegalStateException("Cannot remove null entity!");
 
         em.remove(entity);
     }
 
     public <T extends Object> T persist(T entity) {
         return persist(entity, false);
     }
 
     public <T extends Object> T persist(T entity, boolean flush) {
         try {
             em.persist(entity);
 
             this.errors = null;
 
             if (flush) {
                 em.flush();
             }
 
         } catch (InvalidStateException ise) {
             this.problem = ise;
             this.errors  = ise.getInvalidValues();
 
             for (InvalidValue error : ise.getInvalidValues()) {
                 LOG.warn("Invalid Value: " + error.getBeanClass() + "." + error.getPropertyName() + " = " + error.
                         getValue() + " | " + error.getMessage());
             }
 
             markForRollback(true);
             LOG.error("InvalidStateException in Controller.persist", ise);
 
         } catch (ConstraintViolationException cve) {
             this.problem = cve;
             markForRollback(true);
             LOG.error("ConstraintViolationException in Controller.persist", cve);
         } catch (PropertyValueException pve) {
             this.problem = pve;
             markForRollback(true);
             LOG.error("PropertyValueException in Controller.persist", pve);
 
         }
         return entity;
     }
 
     public boolean hasErrors() {
         return this.errors != null || this.problem != null;
     }
 
     public InvalidValue[] getErrors() {
         return this.errors;
     }
 
     public void clearErrors() {
         this.errors = null;
     }
 
     public Throwable getLastException() {
         return problem;
     }
     
     public String getErrorsAsString(String delimiter, boolean debug) {
         StringBuilder errorStringBuilder = new StringBuilder();
         boolean first = true;
         for (InvalidValue error : this.errors) {
             if (debug) {
                 errorStringBuilder.append((first ? "" : delimiter) + "Invalid Value: " + error.getBeanClass() + "." + error.
                         getPropertyName() + " = " + error.getValue() + ", message = " + error.getMessage());
             } else {
                 errorStringBuilder.append((first ? "" : delimiter) + error.getMessage());
             }
 
             if (first) {
                 first = false;
             }
         }
         return errorStringBuilder.toString();
     }
 
     /**
      * Find all objects matching criteria
      * 
      * Sample criteria include:
      * Restrictions.eq("property", variable)
      * Restrictions.eq("subclass.property", variable)
      * Order.desc("property")
      * 
      * See for more examples:
      * http://www.hibernate.org/hib_docs/v3/api/org/hibernate/criterion/Restrictions.html
      * 
      **/
     public <T extends Object> List<T> listByCriterions(Class<T> entityClass, Object... criteria) {
         return listByCriterions(entityClass, -1, -1, criteria);
     }
 
     /**
      * Find numResults objects matching criteria
      * 
      * Sample criteria include:
      * Restrictions.eq("property", variable)
      * Restrictions.eq("subclass.property", variable)
      * Order.desc("property")
      * 
      * See for more examples:
      * http://www.hibernate.org/hib_docs/v3/api/org/hibernate/criterion/Restrictions.html
      * 
      **/
     public <T extends Object> List<T> listByCriterions(Class<T> entityClass, int numResults, Object... criteria) {
         return listByCriterions(entityClass, 0, numResults, criteria);
     }
 
     /**
      * Find numResults objects matching criteria starting at firstResult
      * 
      * Sample criteria include:
      * Restrictions.eq("property", variable)
      * Restrictions.eq("subclass.property", variable)
      * Order.desc("property")
      * 
      * See for more examples:
      * http://www.hibernate.org/hib_docs/v3/api/org/hibernate/criterion/Restrictions.html
      * 
      **/
     public <T extends Object> List<T> listByCriterions(Class<T> entityClass, int firstResult, int numResults,
                                                        Object... criteria) {
         try {
             Criteria search = getSession().createCriteria(entityClass);
             for (Object c : criteria) {
                 if (c instanceof Criterion || c.getClass() == Criterion.class) {
                     search.add((Criterion) c);
                 }
                 if (c instanceof Order || c.getClass() == Order.class) {
                     search.addOrder((Order) c);
                 }
             }
             if (firstResult > 0) {
                 search.setFirstResult(firstResult);
             }
             if (numResults > 0) {
                 search.setMaxResults(numResults);
             }
             return (List<T>) search.list();
         } catch (NoResultException e) {
             return null;
         }
     }
 
     /**
      * Find numResults objects matching criteria starting at firstResult
      * 
      * See for more examples:
      * http://www.hibernate.org/hib_docs/v3/api/org/hibernate/criterion/Restrictions.html
      * 
      **/
     public <T extends Object> List<T> listByCriterea(Class<T> entityClass, int firstResult, int numResults,
                                                      Criteria criteria) {
         if (firstResult > 0) {
             criteria.setFirstResult(firstResult);
         }
         if (numResults > 0) {
             criteria.setMaxResults(numResults);
         }
         return listByCriteria(entityClass, criteria);
     }
 
     /**
      * Find numResults objects matching criteria starting at firstResult
      * 
      * See for more examples:
      * http://www.hibernate.org/hib_docs/v3/api/org/hibernate/criterion/Restrictions.html
      * 
      **/
     public <T extends Object> List<T> listByCriteria(Class<T> entityClass, Criteria criteria) {
         try {
             return (List<T>) criteria.list();
         } catch (NoResultException e) {
             return null;
         }
     }
 
     /**
      * Find a single object matching criteria
      * 
      * Sample criteria include:
      * Restrictions.eq("property", variable)
      * Restrictions.eq("subclass.property", variable)
      * Order.desc("property")
      * 
      * See for more examples:
      * http://www.hibernate.org/hib_docs/v3/api/org/hibernate/criterion/Restrictions.html
      * 
      **/
     public <T extends Object> T findByCriterion(Class<T> entityClass, Object... criteria) {
         try {
             Criteria search = getSession().createCriteria(entityClass);
             for (Object c : criteria) {
                 if (c instanceof Criterion || c.getClass() == Criterion.class) {
                     search.add((Criterion) c);
                 }
                 if (c instanceof Order || c.getClass() == Order.class) {
                     search.addOrder((Order) c);
                 }
             }
             return (T) search.setMaxResults(1).uniqueResult();
         } catch (NoResultException e) {
             return null;
         }
     }
 
     /**
      * Find a single object matching criteria using a naturalId lookup with the queryByFormula cache
      * 
      * Sample criteria include:
      * Restrictions.naturalId().set("email",request.getRemoteUser());
      * 
      * See for more examples:
      * http://www.hibernate.org/hib_docs/reference/en/html/querycriteria.html#queryByFormula-criteria-naturalid
      */
     public <T extends Object> T findNaturalId(Class<T> entityClass, NaturalIdentifier naturalId) {
         try {
             return (T) (getSession().createCriteria(entityClass).add(naturalId).setCacheable(true).uniqueResult());
         } catch (NoResultException e) {
             return null;
         }
     }
 
     /**
      * Find the count of object matching the criteria
      * 
      * Sample criteria include:
      * Restrictions.eq("property", variable)
      * Restrictions.eq("subclass.property", variable)
      * Order.desc("property")
      * 
      * See for more examples:
      * http://www.hibernate.org/hib_docs/v3/api/org/hibernate/criterion/Restrictions.html
      * 
      **/
     public <T extends Object> int count(Class<T> entityClass, Object... criteria) {
         try {
             Criteria search = getSession().createCriteria(entityClass);
             for (Object c : criteria) {
                 if (c instanceof Criterion || c.getClass() == Criterion.class) {
                     search.add((Criterion) c);
                 }
             }
             search.setProjection(Projections.rowCount());
             return ((Integer) search.list().get(0)).intValue();
         } catch (NoResultException e) {
             return 0;
         }
     }
 
     public <T extends Object> List<T> listByExample(T exampleInstance, String... excludeProperties) {
         try {
             Example example = Example.create(exampleInstance);
             for (String prop : excludeProperties) {
                 example.excludeProperty(prop);
             }
 
             return (List<T>) getSession().createCriteria(exampleInstance.getClass()).add(example).list();
         } catch (NoResultException e) {
             return null;
         }
     }
 
     public <T extends Object> List<T> listByExample(Class<T> entityClass, Example example) {
         try {
             return (List<T>) getSession().createCriteria(entityClass).add(example).list();
         } catch (NoResultException e) {
             return null;
         }
     }
 
     public <T extends Object> T queryByFormula(QueryFormula formula) {
         return (T) query(em.createQuery(formula.getQuery()), formula.getParametersAsPairArray());
     }
 
     public <T extends Object> T query(String queryString, Pair<String, ? extends Object>... parameters) {
         Query query = em.createQuery(queryString);
         return (T) query(query, parameters);
     }
 
     public <T extends Object> T query(Query query, Pair<String, ? extends Object>... parameters) {
         if (parameters != null) {
             for (Pair<String, ? extends Object> parameter : parameters) {
                 query.setParameter(parameter.getFirst(), parameter.getSecond());
             }
         }
         try {
            return (T) query.getSingleResult();
         } catch (NoResultException nre) {
             return null;
         }
     }
 
     public <T extends Object> T query(Class<T> entityClass, String queryString,
                                       Pair<String, ? extends Object>... parameters) {
         try {
             return (T) query(em.createQuery(queryString), parameters);
         } catch (NoResultException e) {
             return null;
         }
     }
 
     public <T extends Object> T namedQuery(Class<T> entityClass, String namedQuery,
                                            Pair<String, ? extends Object>... parameters) {
         try {
             return (T) query(em.createNamedQuery(entityClass.getSimpleName() + "." + namedQuery), parameters);
         } catch (NoResultException e) {
             return null;
         }
     }
 
     public <T extends Object> T namedScaler(Class<T> returnClass, String namedQuery,
                                             Pair<String, ? extends Object>... parameters) {
         try {
             return (T) query(em.createNamedQuery(namedQuery), parameters);
         } catch (NoResultException e) {
             return null;
         }
     }
 
     public <T extends Object> List<T> list(String queryString, Pair<String, ? extends Object>... parameters) {
         return (List<T>) list(queryString, 0, -1, parameters);
     }
 
     public <T extends Object> List<T> list(String queryString, int start, int limit,
                                            Pair<String, ? extends Object>... parameters) {
         Query query = em.createQuery(queryString);
         return (List<T>) list(query, start, limit, parameters);
     }
 
     public <T extends Object> List<T> list(Query query, Pair<String, ? extends Object>... parameters) {
         return (List<T>) list(query, 0, -1, parameters);
     }
 
     public <T extends Object> List<T> list(Query query, int start, int limit,
                                            Pair<String, ? extends Object>... parameters) {
         if (parameters != null) {
             for (Pair<String, ? extends Object> parameter : parameters) {
                 query.setParameter(parameter.getFirst(), parameter.getSecond());
             }
         }
         if (start > 0) {
             query.setFirstResult(start);
         }
         if (limit > 0) {
             query.setMaxResults(limit);
         }
         try {
             return (List<T>) query.getResultList();
         } catch (NoResultException nre) {
             return null;
         }
     }
 
     public List<Object[]> namedList(String namedQuery, Pair<String, ? extends Object>... parameters) {
         return namedList(namedQuery, -1, -1, parameters);
     }
 
     public List<Object[]> namedList(String namedQuery, int start, int limit,
                                     Pair<String, ? extends Object>... parameters) {
         Query query = em.createNamedQuery(namedQuery);
         if (parameters != null) {
             for (Pair<String, ? extends Object> parameter : parameters) {
                 query.setParameter(parameter.getFirst(), parameter.getSecond());
             }
         }
         if (start > 0) {
             query.setFirstResult(start);
         }
         if (limit > 0) {
             query.setMaxResults(limit);
         }
         try {
             return (List<Object[]>) query.getResultList();
         } catch (NoResultException nre) {
             return null;
         }
     }
 
     public List<Object[]> namedNativeList(String namedQuery, Pair<String, ? extends Object>... parameters) {
         return namedNativeList(namedQuery, -1, -1, parameters);
     }
 
     public List<Object[]> namedNativeList(String namedQuery, int start, int limit,
                                           Pair<String, ? extends Object>... parameters) {
         Query query = em.createNativeQuery(namedQuery);
         if (parameters != null) {
             for (Pair<String, ? extends Object> parameter : parameters) {
                 query.setParameter(parameter.getFirst(), parameter.getSecond());
             }
         }
         if (start > 0) {
             query.setFirstResult(start);
         }
         if (limit > 0) {
             query.setMaxResults(limit);
         }
         try {
             return (List<Object[]>) query.getResultList();
         } catch (NoResultException nre) {
             return null;
         }
     }
 
     public <T extends Object> List<T> nativeList(Class<T> entityClass, String queryString,
                                                  Pair<String, ? extends Object>... parameters) {
 
         Session session = getSession();
 
         SQLQuery query = session.createSQLQuery(queryString).addEntity(entityClass);
 
         if (parameters != null) {
             for (Pair<String, ? extends Object> parameter : parameters) {
                 query.setParameter(parameter.getFirst(), parameter.getSecond());
             }
         }
 
         try {
             return (List<T>) query.list();
         } catch (NoResultException nre) {
             return null;
         }
     }
 
     public <T extends Object> List<T> nativeScalerList(Class<T> scalarType, String queryString,
                                                        Pair<String, ? extends Object>... parameters) {
         Query query = em.createNativeQuery(queryString);
         if (parameters != null) {
             for (Pair<String, ? extends Object> parameter : parameters) {
                 query.setParameter(parameter.getFirst(), parameter.getSecond());
             }
         }
         try {
             return (List<T>) query.getResultList();
         } catch (NoResultException nre) {
             return null;
         }
     }
 
     public List<Object[]> nativeList(String queryString, Pair<String, ? extends Object>... parameters) {
         Query query = em.createNativeQuery(queryString);
         if (parameters != null) {
             for (Pair<String, ? extends Object> parameter : parameters) {
                 query.setParameter(parameter.getFirst(), parameter.getSecond());
             }
         }
         try {
             return (List<Object[]>) query.getResultList();
         } catch (NoResultException nre) {
             return null;
         }
     }
 
     public Object nativeScalar(String queryString, Pair<String, ? extends Object>... parameters) {
         Query query = em.createNativeQuery(queryString);
         if (parameters != null) {
             for (Pair<String, ? extends Object> parameter : parameters) {
                 query.setParameter(parameter.getFirst(), parameter.getSecond());
             }
         }
         try {
             return query.getSingleResult();
         } catch (NoResultException nre) {
             return null;
         }
     }
 
     public int nativeCount(String queryString, Pair<String, ? extends Object>... parameters) {
         Query query = em.createNativeQuery(queryString);
 
         int count = 0;
         Object o = null;
         if (parameters != null) {
             for (Pair<String, ? extends Object> parameter : parameters) {
                 query.setParameter(parameter.getFirst(), parameter.getSecond());
             }
         }
         
         try {
             o = query.getSingleResult();
             count = ((Long) o).intValue();
         } catch (NoResultException nre) {
             count = 0;
         } catch (ClassCastException cce) {
             count = ((java.math.BigInteger) o).intValue();
         }
         
         return count;
     }
 
     public <T extends Object> List<T> list(Class<T> entityClass, String queryString,
                                            Pair<String, ? extends Object>... parameters) {
         return list(entityClass, queryString, -1, -1, parameters);
     }
 
     public <T extends Object> List<T> namedList(Class<T> entityClass, String namedQuery,
                                                 Pair<String, ? extends Object>... parameters) {
         return namedList(entityClass, namedQuery, -1, -1, parameters);
     }
 
     public <T extends Object> List<T> list(Class<T> entityClass, String queryString, int start, int limit,
                                            Pair<String, ? extends Object>... parameters) {
         try {
             return (List<T>) list(em.createQuery(queryString), start, limit, parameters);
         } catch (NoResultException e) {
             return null;
         }
     }
 
     public <T extends Object> List<T> namedList(Class<T> entityClass, String namedQuery, int start, int limit,
                                                 Pair<String, ? extends Object>... parameters) {
         try {
             return (List<T>) list(em.createNamedQuery(entityClass.getSimpleName() + "." + namedQuery), start, limit,
                                   parameters);
         } catch (NoResultException e) {
             return null;
         }
     }
 
     /**
      * @deprecated
      * will cause memory problems when mapping large numbers of entities, use the alternate version with batch sizes
      **/
     public <T extends Object> void hibernateSearchIndex(List<T> objects) {
         this.getFullTextEntityManager();
         for (T o : objects) {
             fullTextEntityManager.index(o);
         }
     }
 
     /**
      * It is critical that batchSize matches the hibernate.search.worker.batch_size you set
      **/
     public <T extends Object> void hibernateSearchIndex(Class<T> entityClass, int batchSize) {
         FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(getSession());
         fullTextSession.setFlushMode(FlushMode.MANUAL);
         fullTextSession.setCacheMode(CacheMode.IGNORE);
 
         ScrollableResults results = fullTextSession.createCriteria(entityClass).setFetchSize(batchSize).scroll(
                 ScrollMode.FORWARD_ONLY);
 
         try {
             int index = 0;
             while (results.next()) {
                 index++;
                 fullTextSession.index(results.get(0)); //index each element
 
                 //clear every batchSize since the queue is processed
                 if (index % batchSize == 0) {
                     fullTextSession.flushToIndexes();
                     fullTextSession.clear();
                 }
             }
         } finally {
             results.close();
         }
     }
 
     /**
      * It is critical that batchSize matches the hibernate.search.worker.batch_size you set
      **/
     public <T extends Object> void hibernateSearchClearAndIndex(Class<T> entityClass, int batchSize) {
         FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(getSession());
         fullTextSession.purgeAll(entityClass);
         hibernateSearchIndex(entityClass, batchSize);
         fullTextSession.getSearchFactory().optimize(entityClass);
     }
 
     public <T extends Object> void hibernateSearchRemove(Class<T> entityClass, int entityId) {
         FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(getSession());
         fullTextSession.purge(entityClass, entityId);
         fullTextSession.flush(); //index are written at commit time
     }
 
     public <T extends Object> List<T> hibernateSearchResults(Class<T> entityClass, String query, String[] fields) throws ParseException {
         return hibernateSearchResults(entityClass, query, -1, -1, fields);
     }
 
     public <T extends Object> List<T> hibernateSearchResults(Class<T> entityClass, String query, int limit,
                                                              String[] fields) throws ParseException {
         return hibernateSearchResults(entityClass, query, -1, limit, fields);
     }
 
     public <T extends Object> List<T> hibernateSearchResults(Class<T> entityClass, String query, int start, int limit,
                                                              String[] fields) throws ParseException {
         try {
             MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
 
             //org.apache.lucene.search.Query luceneQuery = parser.parse(query.trim().replaceAll(" ","~ ").replaceAll(" [Aa][Nn][Dd]~ "," AND ").replaceAll(" [Oo][Rr]~ "," OR ") + "~");
             //org.apache.lucene.search.Query luceneQuery = parser.parse(query.trim());
             org.apache.lucene.search.Query luceneQuery;
 
             query = filterLuceneQuery(query);
             luceneQuery = parser.parse(query);
 
             org.hibernate.search.jpa.FullTextQuery hibQuery = getFullTextEntityManager().createFullTextQuery(luceneQuery,
                                                                                                              entityClass);
 
             if (start >= 0 && limit > 0) {
                 return hibQuery.setFirstResult(start).setMaxResults(limit).getResultList();
             } else if (limit > 0) {
                 return hibQuery.setMaxResults(limit).getResultList();
             } else if (start >= 0) {
                 return hibQuery.setFirstResult(start).getResultList();
             } else {
                 return hibQuery.getResultList();
             }
 
         } catch (NoResultException e) {
             return null;
         }
     }
 
     public <T extends Object> int hibernateSearchCount(Class<T> entityClass, String query, String[] fields) throws ParseException {
         try {
             MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
 
             //org.apache.lucene.search.Query luceneQuery = parser.parse(query.trim().replaceAll(" ","~ ").replaceAll(" [Aa][Nn][Dd]~ "," AND ").replaceAll(" [Oo][Rr]~ "," OR ") + "~");
             //org.apache.lucene.search.Query luceneQuery = parser.parse(query.trim());
             org.apache.lucene.search.Query luceneQuery;
 
             query = filterLuceneQuery(query);
             luceneQuery = parser.parse(query);
 
             org.hibernate.search.jpa.FullTextQuery hibQuery = getFullTextEntityManager().createFullTextQuery(luceneQuery,
                                                                                                              entityClass);
             return hibQuery.getResultSize();
             
         } catch (NoResultException e) {
             return 0;
         }
     }
 
     private String filterLuceneQuery(String queryOrig) {
 
         String query = queryOrig;
 
         query = query.trim().replaceAll("\\s+", "* ");
 
         query += "*";
 
         query = query.replaceAll(" [Aa][Nn][Dd]\\* ", " AND ").replaceAll(" [Oo][Rr]\\* ", " OR ").replaceAll("[-]\\*",
                                                                                                               "-").
                 replaceAll("[)]\\*", ")").replaceAll("[(]\\*", "(").replaceAll("[!]\\*", "!").replaceAll("[?]\\*", "?").
                 replaceAll("[:]\\*", ":").replaceAll("[+]\\*", "+");
 
 
         query = query.replaceAll("[-]", "\\\\-").replaceAll("[)]", "\\\\)").replaceAll("[(]", "\\\\(").replaceAll("[!]",
                                                                                                                   "\\\\!").
                 replaceAll("[?]", "\\\\?").replaceAll("[:]", "\\\\:").replaceAll("[+]", "\\\\+");
 
         return query;
     }
 
     public <T extends Object> Criteria createCriteria(Class<T> entityClass) {
         return getSession().createCriteria(entityClass);
     }
 
     public int update(String queryString, Pair<String, Object>... parameters) {
         Query query = em.createQuery(queryString);
         return update(query, parameters);
     }
 
     public int nativeUpdate(String queryString, Pair<String, Object>... parameters) {
         Query query = em.createNativeQuery(queryString);
         return update(query, parameters);
     }
 
     public int update(Query query, Pair<String, Object>... parameters) {
         if (parameters != null) {
             for (Pair<String, Object> parameter : parameters) {
                 query.setParameter(parameter.getFirst(), parameter.getSecond());
             }
         }
         return query.executeUpdate();
     }
 
     public <T extends Object> T filterSingle(Collection collection, String filterString) {
         List<T> list = filter(collection, filterString, 0, 1);
         return list.size() > 0 ? list.get(0) : null;
     }
 
     public <T extends Object> T filterSingle(Collection collection, String filterString, Pair<String, Object>... params) {
         List<T> list = filter(collection, filterString, 0, 1, params);
         return list.size() > 0 ? list.get(0) : null;
     }
 
     public <T extends Object> List<T> filter(Collection collection, String filterString) {
         return filter(collection, filterString, 0, -1);
     }
 
     public <T extends Object> List<T> filter(Collection collection, String filterString, int start, int limit) {
         org.hibernate.Query query = getSession().createFilter(collection, filterString).setFirstResult(start);
         if (limit > 0) {
             query.setMaxResults(limit);
         }
 
         return (List<T>) query.list();
     }
 
     public <T extends Object> List<T> filter(Collection collection, String filterString, Pair<String, Object>... params) {
         return filter(collection, filterString, 0, -1, params);
     }
 
     public <T extends Object> List<T> filter(Collection collection, String filterString, int start, int limit,
                                              Pair<String, Object>... params) {
         org.hibernate.Query query = getSession().createFilter(collection, filterString).setFirstResult(start);
         if (limit > 0) {
             query.setMaxResults(limit);
         }
 
         for (Pair<String, Object> param : params) {
             query.setParameter(param.getFirst(), param.getSecond());
         }
 
         return (List<T>) query.list();
     }
 }
