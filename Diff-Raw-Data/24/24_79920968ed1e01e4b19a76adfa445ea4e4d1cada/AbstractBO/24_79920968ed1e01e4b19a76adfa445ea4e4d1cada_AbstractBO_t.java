 /**
  * Without Copyright
  */
 package com.pingpong.core.bo.impl;
 
 import com.pingpong.core.bo.BO;
 import com.pingpong.core.dao.DAO;
 import com.pingpong.core.hibernate.HibernateManager;
 import com.pingpong.domain.Entity;
 import com.pingpong.shared.hibernate.ListResult;
 import com.pingpong.shared.hibernate.PatternSearchData;
 import com.pingpong.shared.hibernate.SearchData;
 import net.sf.oval.constraint.NotNull;
 import net.sf.oval.guard.Guarded;
 import org.apache.commons.beanutils.BeanUtils;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Projections;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * @author Artur Zhurat
  * @version 1.0
  * @since 20/02/2012
  */
 @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
 @Guarded
 public abstract class AbstractBO<ID extends Serializable, E extends Entity<ID>, Dao extends DAO<ID, E>> implements BO<ID, E> {
 
 	private Dao dao;
 
 	@Autowired
 	public void setDao(@NotNull final Dao dao) {
 		this.dao = dao;
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public ID insert(@NotNull E entity) {
 		return dao.insert(entity);
 	}
 
 	@Override
 	public E getById(@NotNull ID id) {
 		return dao.getById(id);
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void update(@NotNull E entity) {
 		dao.update(entity);
 	}
 
 	@Override
 	public List<E> findAll() {
 		return dao.list();
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void deleteById(@NotNull ID id) {
 		dao.deleteById(id);
 	}
 
 	@Override
 	public ListResult<E> list(@NotNull PatternSearchData<E> searchData) {
 		return getListResult(createCriteria(), searchData);
 	}
 
 	@NotNull
 	protected ListResult<E> toList(@NotNull Criteria criteria, @NotNull PatternSearchData<E> searchData) {
 		return getListResult(criteria, searchData);
 	}
 
 	protected HibernateManager getManager() {
 		return dao.getManager();
 	}
 
 	protected Session getCurrentSession() {
 		return getManager().getCurrentSession();
 	}
 
 	protected void lockEntity(E entity) {
 		getManager().lockEntity(entity);
 	}
 
 	@NotNull
 	protected Dao getDao() {
 		return dao;
 	}
 
 	protected Criteria createCriteria() {
 		return getCurrentSession().createCriteria(dao.getEntityType());
 	}
 
 	@SuppressWarnings("unchecked")
 	private ListResult<E> getListResult(Criteria criteria, SearchData searchData) {
 
 		try {
 			final Criteria countCriteria = getCurrentSession().createCriteria(dao.getEntityType());
 			BeanUtils.copyProperties(countCriteria, criteria);
 			countCriteria.setProjection(Projections.count("id"));
 
 			final Integer totalCount = ((Long)countCriteria.uniqueResult()).intValue();
 
 			if(searchData.getOrder() != null) {
 				criteria.addOrder(searchData.getOrder());
 			}
 
 			if(searchData.getLimit() != null) {
 				criteria.setMaxResults(searchData.getLimit());
 			}
 
 			if(searchData.getOffset() != null) {
 				criteria.setFirstResult(searchData.getOffset());
 			}
 
 			final List<E> items = criteria.list();
 
 			return new ListResult<E>(items, totalCount);
 		} catch(Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
