 /**
  * Generic Hibernate Dao.
  *
  * @copyright	2010 Monits
  * @license 	Copyright (C) 2010. All rights reserved
  * @version 	Release: 1.0.0
  * @link 		http://www.monits.com/
  * @since 		1.0.0
  */
 package com.monits.commons.dao;
 
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
 
 import com.google.common.base.Preconditions;
 import com.monits.commons.PaginatedResult;
 import com.monits.commons.model.Builder;
 
 /**
  * Generic Hibernate Dao.
  *
  * @author 		lbritez <lbritez@monits.com>
  * @copyright 	2010 Monits
  * @license 	Copyright (C) 2010. All rights reserved
  * @version 	Release: 1.0.0
  * @link 		http://www.monits.com/
  * @since 		1.0.0
  */
 public abstract class GenericHibernateDao<E> implements GenericDao<E> {
 
 	protected SessionFactory sessionFactory;
 	
 	protected Class<? extends E> eClass;
 
 	/**
 	 * Returns the class of the object to be manipulated by the DAO
 	 *
 	 * @return dao class
 	 */
 	protected abstract Class<? extends E> getDaoClass();
 
 	/**
 	 * Creates a new instance of a GenericHibernateDao
 	 * @param sessionFactory The session factory to be used.
 	 */
 	public GenericHibernateDao(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 		this.eClass = getDaoClass();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public E get(Long id) {
 		Preconditions.checkNotNull(id);
 
 		// Warning are suppressed as we are getting an E element
 		return (E) sessionFactory.getCurrentSession().get(eClass, id);
 	}
 
 	@Override
 	public List<? extends E> getAll() {
 		// Warning are suppressed as we are getting an E list
 		@SuppressWarnings("unchecked")
 		List<? extends E> results = createCriteria().list();
 
 		return results;
 	}
 
 	/**
 	 * Creates a session of criteria.
 	 *
 	 * @return The session.
 	 */
 	protected Criteria createCriteria() {
 		return sessionFactory.getCurrentSession().createCriteria(eClass);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public PaginatedResult<E> getAll(int page, int amount) {
 
 		Preconditions.checkArgument(page >= 0, "Invalid page " + page);
 		Preconditions.checkArgument(amount > 0, "Invalid amount " + amount);
 
 		int totalElements = 
 			((Integer) createCriteria().setProjection(Projections.rowCount())
		            .uniqueResult());

		int totalPages = calculateTotalPages(totalElements, amount);
 		Criteria criteria = createCriteria();
 
 		criteria.setFirstResult(page * amount);
 		criteria.setMaxResults(amount);
 
		return new PaginatedResult<E>(page + 1, totalPages, criteria.list());
 	}
 
 	@Override
 	public void delete(E entity) {
 		Preconditions.checkNotNull(entity);
 
 		sessionFactory.getCurrentSession().delete(entity);
 	}
 
 	@Override
 	public E create(Builder<E> builder) {
 		E entity = builder.build();
 
 		sessionFactory.getCurrentSession().save(entity);
 
 		return entity;
 	}

    /**
     * Calculate the total amount of pages, using a amount of elements and the amount of elements per page
     *
     * @param totalElements amount of total elements
     * @param amountPage amount of element per page
     * @return the total amount of pages
     */
    private int calculateTotalPages(int totalElements, int amountPage) {

    	int totalPages;

    	totalPages = totalElements/amountPage;

 		if(totalElements%amountPage != 0) {
 			totalPages++;
 		}

 		return totalPages;
    }
    
 }
