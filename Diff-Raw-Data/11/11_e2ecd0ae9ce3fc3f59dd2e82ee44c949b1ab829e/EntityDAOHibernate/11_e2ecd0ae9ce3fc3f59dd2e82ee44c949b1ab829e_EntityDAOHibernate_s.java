 package de.taimos.dao.hibernate;
 
 /*
  * #%L
  * Hibernate DAO for Spring
  * %%
  * Copyright (C) 2013 Taimos GmbH
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 import org.hibernate.HibernateException;
 import org.hibernate.exception.ConstraintViolationException;
 import org.springframework.transaction.annotation.Transactional;
 
 import de.taimos.dao.EntityDAO;
 import de.taimos.dao.IEntity;
 import de.taimos.dao.exceptions.ConstraintException;
 import de.taimos.dao.exceptions.DAOException;
 import de.taimos.dao.exceptions.EntityNotFoundException;
 
 /**
  * @author hoegertn
  * 
  * @param <E>
  *            the entity type
  * @param <I>
  *            the id type
  */
 public abstract class EntityDAOHibernate<E extends IEntity<I>, I> implements EntityDAO<E, I> {
 
 	@PersistenceContext
 	protected EntityManager entityManager;
 
 	@Override
 	@Transactional
 	public E save(final E element) {
 		try {
 			return this.entityManager.merge(element);
 		} catch (final ConstraintViolationException e) {
 			throw new ConstraintException(e);
 		} catch (final HibernateException e) {
 			throw new DAOException(e);
 		}
 	}
 
 	@Override
 	@Transactional
 	public void delete(final E element) {
 		try {
 			this.entityManager.remove(this.entityManager.merge(element));
 		} catch (final HibernateException e) {
 			throw new DAOException(e);
 		}
 	}
 
 	@Override
 	public void deleteById(final I id) {
 		try {
 			final E element = this.findById(id);
 			if (element == null) {
 				throw new EntityNotFoundException();
 			}
 			this.entityManager.remove(element);
 		} catch (final HibernateException e) {
 			throw new DAOException(e);
 		}
 	}
 
 	@Override
 	public E findById(final I id) {
 		try {
 			return this.entityManager.find(this.getEntityClass(), id);
 		} catch (final HibernateException e) {
 			throw new DAOException(e);
 		}
 	}
 
 	@Override
 	public List<E> findList() {
 		try {
 			final TypedQuery<E> query = this.entityManager.createQuery(this.getFindListQuery(), this.getEntityClass());
 			return query.getResultList();
 		} catch (final HibernateException e) {
 			throw new DAOException(e);
 		}
 	}
 
 	/**
 	 * Override in sub-class if not "id"
 	 * 
 	 * @return the id field
 	 */
 	protected String getIdField() {
 		return "id";
 	}
 
 	/**
 	 * @param query
 	 * @param params
 	 * @return the entity or null if not found or more than one found
 	 */
 	protected E findByQuery(final String query, final Object... params) {
 		try {
 			final List<E> list = this.findListByQuery(query, params);
 			if (list.size() == 1) {
 				return list.get(0);
 			}
 			return null;
 		} catch (final HibernateException e) {
 			throw new DAOException(e);
 		}
 	}
 
 	protected List<E> findListByQuery(final String query, final Object... params) {
 		try {
 			final TypedQuery<E> tq = this.entityManager.createQuery(query, this.getEntityClass());
 			for (int i = 0; i < params.length; i++) {
 				tq.setParameter(i + 1, params[i]);
 			}
 			return tq.getResultList();
 		} catch (final HibernateException e) {
 			throw new DAOException(e);
 		}
 	}
 
 	/**
 	 * Override in sub-class if not "FROM 'EntityName'"
 	 * 
 	 * @return the select query
 	 */
 	protected String getFindListQuery() {
 		return "FROM " + this.getEntityClass().getSimpleName();
 	}
 
 }
