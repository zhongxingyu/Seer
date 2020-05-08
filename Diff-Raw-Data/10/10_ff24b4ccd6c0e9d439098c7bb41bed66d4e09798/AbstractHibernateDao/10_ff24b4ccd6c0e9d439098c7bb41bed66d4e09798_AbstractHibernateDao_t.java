 package com.xone.model.hibernate.generic;
 
 
 import java.io.Serializable;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.beanutils.PropertyUtils;
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.LockMode;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.criterion.CriteriaSpecification;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Example.PropertySelector;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projection;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.impl.CriteriaImpl;
 import org.hibernate.metadata.ClassMetadata;
 import org.hibernate.stat.Statistics;
 import org.hibernate.transform.ResultTransformer;
 import org.hibernate.type.Type;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.orm.hibernate3.HibernateCallback;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import org.springframework.orm.ibatis.SqlMapClientTemplate;
 import org.springframework.util.Assert;
 import org.springframework.util.StringUtils;
 
 import com.xone.model.hibernate.support.Pagination;
 
 @SuppressWarnings("unchecked")
 public class AbstractHibernateDao<T extends Serializable> extends HibernateDaoSupport implements HibernateDao<T> {
 	
 	protected final Logger logger = LoggerFactory.getLogger(getClass());
 	protected Class<T> entityClass;
 	protected SqlMapClientTemplate sqlMapTemplate;
 	
 	public AbstractHibernateDao() {
 	}
 	
 
 
 
 	/**
 	 * 保存对象
 	 * 
 	 * @param entity
 	 *            实体对象
 	 * @return 实体对象
 	 */
 	public T save(T entity) {
 		Assert.notNull(entity);
 		getHibernateTemplate().save(setDateCreated(entity));
 		return entity;
 	}
 	
 	/**
 	 * 设置更新时间
 	 * @param entity
 	 */
 	protected T setLastUpdated(T entity) {
 		try {
 			Method setLastUpdated = entity.getClass().getMethod("setLastUpdated", Date.class);
 			setLastUpdated.invoke(entity, new Date());
 		} catch (Exception e) {
 			logger.debug(e.getMessage());
 		}
 		return entity;
 	}
 	
 	public T setDateCreated(T entity) {
 		try {
 			Date date = new Date();
 			Method setDateCreated = entity.getClass().getMethod("setDateCreated", Date.class);
 			setDateCreated.invoke(entity, date);
 			Method setLastUpdated = entity.getClass().getMethod("setLastUpdated", Date.class);
 			setLastUpdated.invoke(entity, date);
 		} catch (Exception e) {
 			logger.debug(e.getMessage());
 		}
 		return entity;
 	}
 	
 	/**
 	 * 
 	 * 批量保存对象。
 	 * 
 	 * @param entity
 	 * @return
 	 */
 	public List<T> save(List<T> entities) {
 		Assert.notNull(entities);
 		List<T> result = new ArrayList<T>(entities.size());
 		for (int i = 0; i < entities.size(); i++) {
 			result.add(this.save(entities.get(i)));
 			if (i % 20 == 0) {
 				getSession(false).flush();
 			}
 		}
 		return result;
 	}
 	/**
 	 * 更新对象
 	 * 
 	 * @param entity
 	 *            实体对象
 	 * @return 实体对象
 	 */
 	public T update(T entity) {
 		Assert.notNull(entity);
 		getHibernateTemplate().update(setLastUpdated(entity));
 		return entity;
 	}
 	/**
 	 * 保存或更新对象
 	 * 
 	 * @param entity
 	 *            实体对象
 	 * @return 实体对象
 	 */
 	public T saveOrUpdate(T entity) {
 		Assert.notNull(entity);
 		getHibernateTemplate().saveOrUpdate(setLastUpdated(entity));
 		return entity;
 	}
 	/**
 	 * 保存或更新对象拷贝
 	 * 
 	 * @param entity
 	 * @return 已更新的持久化对象
 	 */
 	public Object merge(Object entity) {
 		Assert.notNull(entity);
 		return getHibernateTemplate().merge(entity);
 	}
 	/**
 	 * 删除对象
 	 * 
 	 * @param entity
 	 *            实体对象
 	 */
 	public void delete(Object entity) {
 		Assert.notNull(entity);
 		getHibernateTemplate().delete(entity);
 	}
 	
 	/**
 	 * 根据ID删除记录
 	 * 
 	 * @param id
 	 *            记录ID
 	 */
 	public T deleteById(Serializable id) {
 		Assert.notNull(id);
 		T entity = load(id);
 		getHibernateTemplate().delete(entity);
 		return entity;
 	}
 	
 	/**
 	 * 根据ID删除记录
 	 * 
 	 * @param id
 	 *            记录ID
 	 */
 	public T findById(Serializable id) {
 		Assert.notNull(id);
 		return load(id);
 	}
 	
 	
 	/**
 	 * 
 	 * 根据ID批量删除。
 	 * 
 	 * @param ids
 	 * @return
 	 */
 	@SuppressWarnings("rawtypes")
 	public int deleteById(Serializable[] ids) {
 		Assert.notNull(ids);
 		Class clazz = this.getClass();
 		java.lang.reflect.Type genType = clazz.getGenericSuperclass();
 		java.lang.reflect.Type[] params = ((java.lang.reflect.ParameterizedType) genType)
 				.getActualTypeArguments();
 		Class classT = (Class) params[0];
 		String className = classT.getName();
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("delete from ");
 		buffer.append(className);
 		buffer.append(" dClass where dClass.id in (:id)");
 		Query query = getSession(false).createQuery(buffer.toString());
 		//query.setCacheable(true);
 		query.setParameterList("id", ids);
 		return query.executeUpdate();
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public int deleteByProperties(String propertyName, List<T> propertyList) {
 		Assert.notNull(propertyList);
 		Class clazz = this.getClass();
 		java.lang.reflect.Type genType = clazz.getGenericSuperclass();
 		java.lang.reflect.Type[] params = ((java.lang.reflect.ParameterizedType) genType)
 				.getActualTypeArguments();
 		Class classT = (Class) params[0];
 		String className = classT.getName();
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("delete from ");
 		buffer.append(className);
 		buffer.append(" dClass where dClass.");
 		buffer.append(propertyName);
 		buffer.append(" in (:property)");
 		Query query = getSession(false).createQuery(buffer.toString());
 		//query.setCacheable(true);
 		query.setParameterList("property", propertyList);
 		return query.executeUpdate();
 	}
 	
 	/**
 	 * 通过ID查找对象
 	 * 
 	 * @param id
 	 *            记录的ID
 	 * @param lock
 	 *            是否锁定对象
 	 * @return 实体对象
 	 */
 	public T load(Serializable id) {
 		Assert.notNull(id);
 		return load(id, false);
 	}
 
 	/**
 	 * 通过ID查找对象
 	 * @param id
 	 * 			记录的ID
 	 * @return 	实体对象
 	 */
 	public T get(Serializable id) {
 		Assert.notNull(id);
 		return (T) getHibernateTemplate().get(getPersistentClass(), id);
 	}
 	/**
 	 * 通过ID查找对象,不锁定对象
 	 * 
 	 * @param id
 	 *            记录的ID
 	 * @return 实体对象
 	 */
 	public T load(Serializable id, boolean lock) {
 		Assert.notNull(id);
 		T entity = null;
 		if (lock) {
 			entity = (T) getHibernateTemplate().load(getPersistentClass(), id,
 					LockMode.UPGRADE);
 		} else {
 			entity = (T) getHibernateTemplate().load(getPersistentClass(), id);
 		}
 		return entity;
 	}
 	/**
 	 * 查找所有对象
 	 * 
 	 * @return 对象列表
 	 */
 	public List<T> findAll() {
 		return findByCriteria();
 	}
 
 	/**
 	 * 根据提供的sql预编译语句和参数值查找对象
 	 * @param hql
 	 * 			预编译sql语句
 	 * @param values 
 	 * 			数量可变的参数
 	 * @return 实体对象列表
 	 */
 	@SuppressWarnings("rawtypes")
 	public List find(String hql, Object... values) {
 		return createQuery(hql, values).list();
 	}
 
 	/**
 	 * 根据提供的sql预编译语句和参数值查找对象
 	 * @param hql
 	 * 			预编译sql语句
 	 * @param values 
 	 * 			数量可变的参数
 	 * @return 实体对象
 	 */
 	public Object findUnique(String hql, Object... values) {
 		return createQuery(hql, values).uniqueResult();
 	}
 
 	/**
 	 * 按属性查找对象列表.
 	 * 
 	 * @param property
 	 * @param value
 	 * @return
 	 */
 	public List<T> findByProperty(String property, Object value) {
 		Assert.hasText(property);
 		return createCriteria(Restrictions.eq(property, value)).list();
 	}
 	
 	/**
 	 * 按属性查找对象列表.
 	 * 
 	 * @param property
 	 * @param value
 	 * @return
 	 */
 	public <K> List<T> findInProperty(String property, List<K> value) {
 		Assert.hasText(property);
 		return createCriteria(Restrictions.in(property, value)).list();
 	}
 
 	/**
 	 * 按属性查找唯一对象.
 	 * 
 	 * @param property
 	 * @param value
 	 * @return
 	 */
 	public T findUniqueByProperty(String property, Object value) {
 		Assert.hasText(property);
 		Assert.notNull(value);
 		return (T) createCriteria(Restrictions.eq(property, value))
 				.uniqueResult();
 	}
 	/**
 	 * 按属性查找对象的数量
 	 * 
 	 * @param property
 	 * @param value
 	 * @return
 	 */
 	public int countByProperty(String property, Object value) {
 		Assert.hasText(property);
 		Assert.notNull(value);
 		return ((Number) (createCriteria(Restrictions.eq(property, value))
 				.setProjection(Projections.rowCount()).uniqueResult()))
 				.intValue();
 	}
 
 	/**
 	 * 根据查询函数与参数列表创建Query对象,后续可进行更多处理,辅助函数.
 	 * @param queryString
 	 * @param values
 	 * @return
 	 */
 	public Query createQuery(String queryString, Object... values) {
 		Assert.hasText(queryString);
 		Query queryObject = getSession(false).createQuery(queryString);
 		//queryObject.setCacheable(true);
 		if (values != null) {
 			for (int i = 0; i < values.length; i++) {
 				queryObject.setParameter(i, values[i]);
 			}
 		}
 		return queryObject;
 	}
 
 	/**
 	 * 按Criterion查询对象列表.
 	 * 
 	 * @param criterion
 	 *            数量可变的Criterion.
 	 */
 	public List<T> findByCriteria(Criterion... criterion) {
 		return createCriteria(criterion).list();
 	}
 
 	/**
 	 * 获取分页对象
 	 * @param crit
 	 * @param pageNo
 	 * @param pageSize
 	 * @param projection
 	 * @param orders
 	 * @return
 	 */
 	public Pagination findByCriteria(Criteria crit, int pageNo,
 			int pageSize, Projection projection, Order... orders) {
 		int totalCount = ((Number) crit.setProjection(Projections.rowCount())
 				.uniqueResult()).intValue();
 		Pagination p = new Pagination(pageNo, pageSize, totalCount);
 		if (totalCount < 1) {
 			p.setList(Collections.emptyList());
 			return p;
 		}
 		crit.setProjection(projection);
 		if (projection == null) {
 			crit.setResultTransformer(Criteria.ROOT_ENTITY);
 		}
 		if (orders != null) {
 			for (Order order : orders) {
 				crit.addOrder(order);
 			}
 		}
 		crit.setFirstResult(p.getFirstResult());
 		crit.setMaxResults(p.getPageSize());
 		p.setList(crit.list());
 		return p;
 	}
 
 	/**
 	 * @param hql
 	 *            查询语句
 	 * @param countHql
 	 *            查询总数
 	 * @param pageSize
 	 *            每页大小
 	 * @param startIndex
 	 *            开始索引位置
 	 * @return
 	 */
 	public Pagination findPageByQuery(final String hql, final String countHql,
 			final int pageSize, final int startIndex) {
 		return (Pagination) getHibernateTemplate().execute(
 				new HibernateCallback() {
 					@SuppressWarnings("rawtypes")
 					public Object doInHibernate(Session session)
 							throws HibernateException, SQLException {
 						int totalCount = ((Number) session
 								.createQuery(countHql).iterate().next())
 								.intValue();
 						Query query = session.createQuery(hql);
 						query.setFirstResult(startIndex);
 						query.setMaxResults(pageSize);
 						List items = query.list();
 						Pagination ps = new Pagination(startIndex / pageSize
 								+ 1, pageSize, totalCount, items);
 						return ps;
 					}
 				});
 	}
 
 	/**
 	 * 查询对象
 	 * @param hql 查询语句
 	 * @param pageSize 页大小
 	 * @param startIndex 开始位置
 	 * @return
 	 */
 	@SuppressWarnings("rawtypes")
 	public List findByQuery(final String hql, final int pageSize,
 			final int startIndex) {
 		return (List) getHibernateTemplate().execute(new HibernateCallback() {
 			public Object doInHibernate(Session session)
 					throws HibernateException, SQLException {
 				Query query = session.createQuery(hql);
 				query.setFirstResult(startIndex);
 				query.setMaxResults(pageSize);
 				return query.list();
 			}
 		});
 	}
 
 	/**
 	 * 根据查询条件查询总数
 	 * @param condi
 	 * @return
 	 */
 	public int countByProperty(DetachedCriteria detachCriteria){
 		Criteria criteria=detachCriteria
 		.getExecutableCriteria(getSession(false));
 		criteria.setProjection(Projections.rowCount());
 		return ((Number)criteria.uniqueResult()).intValue();
 	}
 
 	/**
 	 * 刷新对象
 	 * 
 	 * @param entity
 	 */
 	public void refresh(Object entity) {
 		getSession(false).refresh(entity);
 	}
 
 	/** 
 	 * 根据Criterion条件创建Criteria,后续可进行更多处理,辅助函数.
 	 * @param criterions
 	 * @return
 	 */
 	public Criteria createCriteria(Criterion... criterions) {
 		Criteria criteria = getSession(false).createCriteria(
 				getPersistentClass());
 		for (Criterion c : criterions) {
 			criteria.add(c);
 		}
 		//criteria.setCacheable(true);
 		return criteria;
 	}
 
 	/**查询对象列表
 	 * @param criterion
 	 * 			游离态的动态查询对象
 	 * @return
 	 */
 
 	public List<T> findByDetachedCriteria(DetachedCriteria detachCriteria) {
 		return getHibernateTemplate().findByCriteria(detachCriteria);
 //		return detachCriteria.getExecutableCriteria(getSession(false)).list();
 	}
 	
 	public List<T> findListByDetachedCriteria(DetachedCriteria detachCriteria, int fetchSize, int maxResults) {
 		return getHibernateTemplate().findByCriteria(detachCriteria, fetchSize, maxResults);
 		//return detachCriteria.getExecutableCriteria(getSession(false)).setFetchSize(fetchSize).setMaxResults(maxResults).list();
 	}
 
 	/**
 	 * 查询分页对象
 	 * @param criterion
 	 * 			游离态的动态查询对象
 	 * @return
 	 */
 	@SuppressWarnings("rawtypes")
 	public Pagination findByDetachedCriteria(DetachedCriteria detachCriteria,
 			int pageSize, int startIndex) {
 		Session session = getSession(false);
 		Criteria criteria = detachCriteria.getExecutableCriteria(session);
 		CriteriaImpl impl = (CriteriaImpl) criteria;
 		Projection poj = impl.getProjection();
 		ResultTransformer transformer = impl.getResultTransformer();
 		int totalCount = (Integer) criteria.setProjection(
 				Projections.rowCount()).uniqueResult();
 		criteria.setProjection(poj);
 		if (poj == null)
 			criteria.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);
 		if (transformer != null) {
 			criteria.setResultTransformer(transformer);
 		}
 		List items = criteria.setFirstResult((startIndex - 1) * pageSize)
 				.setMaxResults(pageSize).list();
 		Pagination ps = new Pagination(startIndex / pageSize + 1, pageSize,
 				totalCount, items);
 		return ps;
 	}
 
 	/**
 	 * 根据传入sql和主键id批量操作
 	 */
 	public int batchUpdateDelete(String sql, Serializable[] ids) {
 		Session session=this.getSession(false);
 		Query query = session.createQuery(sql);
 		query.setParameterList("id", ids);
 		int dels = query.executeUpdate();
 		return dels;
 	}
 
 	@SuppressWarnings("rawtypes")
 	public void compSQL(String sql, Map proMap,Map listMap) {
 		Session session=this.getSession(false);
 		Query query = session.createQuery(sql);
 		Set set=proMap.keySet();
 		Iterator it=set.iterator();
 		while(it.hasNext()){
 			Object key=it.next();
 			query.setParameter((String)key, proMap.get(key));
 		}
 		set=listMap.keySet();
 		it=set.iterator();
 		while(it.hasNext()){
 			Object key=it.next();
 			query.setParameterList((String)key, (List)listMap.get(key));
 		}
 		query.executeUpdate();
 	}
 	
 	public Class<T> getPersistentClass() {
 		if (entityClass == null) {
 			entityClass = (Class<T>) ((ParameterizedType) getClass()
 					.getGenericSuperclass()).getActualTypeArguments()[0];
 		}
 		return entityClass;
 	}
 
 	public T createNewEntity() {
 		try {
 			return getPersistentClass().newInstance();
 		} catch (Exception e) {
 			throw new RuntimeException("不能创建实体对象："
 					+ getPersistentClass().getName());
 		}
 	}
 
 	@SuppressWarnings("rawtypes")
 	protected ClassMetadata getClassMetadata(Class clazz) {
 		return (ClassMetadata) getHibernateTemplate().getSessionFactory()
 				.getClassMetadata(clazz);
 	}
 
 	/**
 	 * 不为空的EXAMPLE属性选择方式
 	 * 
 	 */
 	static final class NotBlankPropertySelector implements PropertySelector {
 		private static final long serialVersionUID = 1L;
 
 		public boolean include(Object object, String property, Type type) {
 			return object != null
 					&& !(object instanceof String && StringUtils
 							.hasText((String) object));
 		}
 	}
 
 	/**
 	 * 获取SqlMapClientTemplate对象
 	 * @return
 	 */
 	public SqlMapClientTemplate getSqlMapTemplate() {
 		return sqlMapTemplate;
 	}
 
 	/**
 	 * 设置SqlMapClientTemplate对象
 	 * @param sqlMapTemplate
 	 */
 	public void setSqlMapTemplate(SqlMapClientTemplate sqlMapTemplate) {
 		this.sqlMapTemplate = sqlMapTemplate;
 	}
 
 	/**
 	 * 通过sqlmap的键值获取对象列表
 	 * @param args sqlmap配置的键值
 	 * @return
 	 */
 	public List<T> listBySqlMap(String id) {
 		return getSqlMapTemplate().queryForList(id);
 	}
 
 	/**
 	 * 通过传递sqlmap的键值和参数获取对象列表
 	 * @param arg0 sqlmap配置的 键值
 	 * @param arg1 参数1
 	 * @param arg2 参数2
 	 * @return
 	 */
 	public T getTBySqlMap(String id) {
 		return (T)getSqlMapTemplate().queryForObject(id);
 	}
 
 	
 	/**
 	 * 通过sqlmap的键值和传递的参数获得实体列表
 	 * @param id
 	 * @param args
 	 * @return
 	 */
 	public List<T> listBySqlMap(String id,Object args){
 		return getSqlMapTemplate().queryForList(id, args);
 	}
 	
 	/**
 	 * 通过sqlmap的键值和传递的参数列表获得实体列表
 	 * @param id
 	 * @param args
 	 * @return
 	 */
 	public List<T> listBySqlMap(String id,Object[] args){
 		return getSqlMapTemplate().queryForList(id, args);
 	}
 	
 	/**
 	 * 通过sqlmap的键值和传递的参数列表获得实体列表
 	 * @param id
 	 * @param list
 	 * @return
 	 */
 	public List<T> listBySqlMap(String id,List<Object> list){
 		return getSqlMapTemplate().queryForList(id, list);
 	}
 	/**
 	 * 通过传递sqlmap的键值和参数获取实体列表
 	 * @param id sqlmap配置的 键值
 	 * @param arg1 参数1
 	 * @param arg2 参数2
 	 * @return
 	 */
 
 	
 	/**
 	 * 通过传递sqlmap的键值和参数获取实体
 	 * @param id
 	 * @param args
 	 * @return
 	 */
 	public T getTBySqlMap(String id,Object args){
 		return (T)getSqlMapTemplate().queryForObject(id,args);
 	}
 	
 	/**
 	 * 通过传递sqlmap的键值和参数获取对象
 	 * @param id
 	 * @param args
 	 * @return
 	 */
 	public Object getObjectBySqlMap(String id,Object args){
 		return getSqlMapTemplate().queryForObject(id,args);
 	}
 	/**
 	 * 通过传递的sqlmap的键值和参数列表获取实体
 	 * @param id
 	 * @param args
 	 * @return
 	 */
 	public T getTBySqlMap(String id, Object[] args){
 		return (T)getSqlMapTemplate().queryForObject(id,args);
 	}
 	
 	
 	/**
 	 * 通过传递的sqlmap的键值和参数列表获取实体
 	 * @param id
 	 * @param list
 	 * @return
 	 */
 	public T getTBySqlMap(String id, List<Object> list){
 		return (T)getSqlMapTemplate().queryForObject(id,list);
 	}
 	
 	
 	/**
 	 * 通过传递的sqlmap键值获取对象
 	 * @param id1 查询列表键值
 	 * @param id2 查询总数键值
 	 * @return
 	 */
 	public Pagination getObjectBySqlMap(String id1,String id2){
 		int totalCount = Integer.parseInt(getSqlMapTemplate().queryForList(id2).get(0).toString());
 		Pagination p = new Pagination(1, 10, totalCount);
 		p.setList(getSqlMapTemplate().queryForList(id1));
 		return p;
 	}
 	/**
 	 * 通过传递的sqlmap键值和参数获取对象
 	 * @param id1 查询列表键值
 	 * @param id2 查询总数键值
 	 * @param args 参数
 	 * @return
 	 */
 	public Pagination getObjectBySqlMap(String id1, String id2,Object args){
 		int totalCount = Integer.parseInt(getSqlMapTemplate().queryForList(id2,args).get(0).toString());
 		Pagination p = new Pagination(1, 10, totalCount);
 		p.setList(getSqlMapTemplate().queryForList(id1,args));
 		return p;
 	}
 	
 	/**
 	 * 通过传递的sqlmap键值和参数数组获取对象
 	 * @param id1 查询列表键值
 	 * @param id2 查询总数键值
 	 * @param args 参数数组
 	 * @return
 	 */
 	public Pagination getObjectBySqlMap(String id1, String id2, Object[] args){
 		int totalCount = Integer.parseInt(getSqlMapTemplate().queryForList(id2,args).get(0).toString());
 		Pagination p = new Pagination(1, 10, totalCount);
 		p.setList(getSqlMapTemplate().queryForList(id1,args));
 		return p;
 	}
 	
 	/**
 	 * 通过传递的sqlmap键值和参数数组获取对象
 	 * @param id1 查询列表键值
 	 * @param id2 查询总数键值
 	 * @param list 参数列表
 	 * @return
 	 */
 	public Pagination getObjectBySqlMap(String id1, String id2, List<Object> list){
 		int totalCount = Integer.parseInt(getSqlMapTemplate().queryForList(id2,list).get(0).toString());
 		Pagination p = new Pagination(1, 10, totalCount);
 		p.setList(getSqlMapTemplate().queryForList(id1,list));
 		return p;
 	}
 	
 
 	/**
 	 * 根据属性删除
 	 * 
 	 * @param propertyName
 	 * @param value
 	 */
 	public int deleteByProperty(String propertyName, Object value) {
 		return deleteByProperty(propertyName, new Object[] { value });
 	}
 
 	/**
 	 * 通过属性删除实体
 	 * 
 	 * @param propertyName
 	 * @param ids
 	 * @return
 	 */
 	@SuppressWarnings("rawtypes")
 	public int deleteByProperty(String propertyName, Object[] ids) {
 		Assert.notNull(ids);
 		Class clazz = this.getClass();
 		java.lang.reflect.Type genType = clazz.getGenericSuperclass();
 		java.lang.reflect.Type[] params = ((java.lang.reflect.ParameterizedType) genType)
 				.getActualTypeArguments();
 		Class classT = (Class) params[0];
 		String className = classT.getName();
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("delete from ");
 		buffer.append(className);
 		if (ids.length == 1)
 			buffer.append(" dClass where dClass." + propertyName + " = :id");
 		else
 			buffer.append(" dClass where dClass." + propertyName + " in (:id)");
 		Query query = getSession(false).createQuery(buffer.toString());
 		//query.setCacheable(true);
 		if (ids.length == 1)
 			query.setParameter("id", ids[0]);
 		else
 			query.setParameterList("id", ids);
 		return query.executeUpdate();
 	}
 
 }
