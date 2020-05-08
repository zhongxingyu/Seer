 package org.iucn.sis.server.api.persistance.hibernate;
 
 import java.util.List;
 
 import org.hibernate.CacheMode;
 import org.hibernate.Criteria;
 import org.hibernate.FetchMode;
 import org.hibernate.FlushMode;
 import org.hibernate.HibernateException;
 import org.hibernate.LockMode;
 import org.hibernate.ScrollMode;
 import org.hibernate.ScrollableResults;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projection;
 import org.hibernate.transform.ResultTransformer;
 
 public abstract class AbstractORMCriteria implements Criteria {
 	private final Criteria _criteria;
 
 	public AbstractORMCriteria(Criteria criteria) {
 		this._criteria = criteria;
 	}
 
 	public Criteria add(Criterion aCriterion) {
 		return _criteria.add(aCriterion);
 	}
 
 	public Criteria addOrder(Order aOrder) {
 		return _criteria.addOrder(aOrder);
 	}
 
 	public Criteria createAlias(String associationPath, String alias, int aJoinType) throws HibernateException {
 		return _criteria.createAlias(associationPath, alias, aJoinType);
 	}
 
 	public Criteria createAlias(String associationPath, String alias) throws HibernateException {
 		return _criteria.createAlias(associationPath, alias);
 	}
 
 	public Criteria createCriteria(String associationPath, int aJoinType) throws HibernateException {
 		return _criteria.createCriteria(associationPath, aJoinType);
 	}
 
 	public Criteria createCriteria(String associationPath, String alias, int aJoinType) throws HibernateException {
 		return _criteria.createCriteria(associationPath, alias, aJoinType);
 	}
 
 	public Criteria createCriteria(String associationPath, String alias) throws HibernateException {
 		return _criteria.createCriteria(associationPath, alias);
 	}
 
 	public Criteria createCriteria(String associationPath) throws HibernateException {
 		return _criteria.createCriteria(associationPath);
 	}
 
 	public String getAlias() {
 		return _criteria.getAlias();
 	}
 
 	public List list() throws HibernateException {
 		return _criteria.list();
 	}
 
 	public ScrollableResults scroll() throws HibernateException {
 		return _criteria.scroll();
 	}
 
 	public ScrollableResults scroll(ScrollMode aScrollMode) throws HibernateException {
 		return _criteria.scroll(aScrollMode);
 	}
 
 	public Criteria setCacheable(boolean aCacheable) {
 		return _criteria.setCacheable(aCacheable);
 	}
 
 	public Criteria setCacheMode(CacheMode aCacheMode) {
 		return _criteria.setCacheMode(aCacheMode);
 	}
 
 	public Criteria setCacheRegion(String aCacheRegion) {
 		return _criteria.setCacheRegion(aCacheRegion);
 	}
 
 	public Criteria setComment(String aComment) {
 		return _criteria.setComment(aComment);
 	}
 
 	public Criteria setFetchMode(String associationPath, FetchMode aMode) throws HibernateException {
 		return _criteria.setFetchMode(associationPath, aMode);
 	}
 
 	public Criteria setFetchSize(int aFetchSize) {
 		return _criteria.setFetchSize(aFetchSize);
 	}
 
 	public Criteria setFirstResult(int aFirstResult) {
 		return _criteria.setFirstResult(aFirstResult);
 	}
 
 	public Criteria setFlushMode(FlushMode aFlushMode) {
 		return _criteria.setFlushMode(aFlushMode);
 	}
 
 	public Criteria setLockMode(LockMode aLockMode) {
 		return _criteria.setLockMode(aLockMode);
 	}
 
 	public Criteria setLockMode(String alias, LockMode aLockMode) {
 		return _criteria.setLockMode(alias, aLockMode);
 	}
 
 	public Criteria setMaxResults(int aMaxResults) {
 		return _criteria.setMaxResults(aMaxResults);
 	}
 
 	public Criteria setProjection(Projection aProjection) {
 		return _criteria.setProjection(aProjection);
 	}
 
 	public Criteria setResultTransformer(ResultTransformer aResultTransformer) {
 		return _criteria.setResultTransformer(aResultTransformer);
 	}
 
 	public Criteria setTimeout(int aTimeout) {
 		return _criteria.setTimeout(aTimeout);
 	}
 
 	public Object uniqueResult() throws HibernateException {
 		return _criteria.uniqueResult();
 	}
 }
