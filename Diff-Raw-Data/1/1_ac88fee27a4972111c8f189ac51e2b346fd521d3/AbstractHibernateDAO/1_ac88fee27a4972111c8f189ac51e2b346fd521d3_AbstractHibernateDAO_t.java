 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.repository;
 
 import org.patientview.patientview.model.BaseModel;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 
 /**
  *  Abstract DAO class class with support for the common generic operations
  */
 public class AbstractHibernateDAO<T extends BaseModel> {
 
     private Class<T> clazz
             = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
 
     @PersistenceContext
     private EntityManager entityManager;
 
     public T get(final Long id) {
         return entityManager.find(clazz, id);
     }
 
     public List<T> getAll() {
         return entityManager.createQuery("from " + clazz.getName()).getResultList();
     }
 
     public void save(final T entity) {
 
         if (!entity.hasValidId()) {
 
             // apply any baseModel standards
             entityManager.persist(entity);
         } else {
             // apply any baseModel standards
             entityManager.merge(entity);
         }
     }
 
     public void delete(final T entity) {
         entityManager.remove(entity);
        entityManager.flush();
     }
 
     public void delete(final Long entityId) {
         final T entity = get(entityId);
         if (entity != null) {
             delete(entity);
         }
     }
 
     protected <T> void buildWhereClause(CriteriaQuery<T> criteria, List<Predicate> wherePredicates) {
         if (!wherePredicates.isEmpty()) {
             Predicate[] predicates = new Predicate[wherePredicates.size()];
 
             for (int x = 0; x < wherePredicates.size(); x++) {
                 predicates[x] = wherePredicates.get(x);
             }
 
             criteria.where(entityManager.getCriteriaBuilder().and(predicates));
         }
     }
 
     protected EntityManager getEntityManager() {
         return entityManager;
     }
 }
