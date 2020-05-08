 /**
  * Copyright 2010 CosmoCode GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cosmocode.palava.entity;
 
 import com.google.inject.Provider;
 import de.cosmocode.palava.jpa.Transactional;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import java.util.List;
 
 /**
  * Abstract skeleton implementation of the {@link ReadOnlyEntityService} interface.
  *
  * @author Willi Schoenborn
  * @param <T> the generic entity type
  */
 public abstract class AbstractReadOnlyEntityService<T> implements ReadOnlyEntityService<T> {
 
     /**
      * Provides an {@link EntityManager} this implementation uses to do it's
      * work. Implementations will ususally delegate to an injected {@link Provider}.
      * 
      * @return an {@link EntityManager}
      */
     protected abstract EntityManager entityManager();
     
     /**
      * Provides the class object of this entity type.
      * 
      * @return this entity type's class object
      */
     protected abstract Class<T> entityClass();
 
     @Transactional
     @Override
     public T get(Object identifier) {
         return entityManager().find(entityClass(), identifier);
     }
 
     @Transactional
     @Override
     public T read(Object identifier) {
         final T entity = get(identifier);
         if (entity == null) {
             throw new PersistenceException(String.format("No entity found for id #%s", identifier));
         } else {
             return entity;
         }
     }
 
     @Transactional
     @Override
     @SuppressWarnings("unchecked")
     public T read(Query query, Object... parameters) {
         return (T) prepare(query, parameters).getSingleResult();
     }
 
     @Override
     public T read(TypedQuery<T> query, Object... parameters) {
         return prepare(query, parameters).getSingleResult();
     }
 
     @Transactional
     @Override
     public T read(String queryName, Object... parameters) {
         return read(entityManager().createNamedQuery(queryName), parameters);
     }
 
     @Transactional
     @Override
     public T reference(long identifier) {
         return entityManager().getReference(entityClass(), identifier);
     }
 
     @Transactional
     @Override
     @SuppressWarnings("unchecked")
     public List<T> list(Query query, Object... parameters) {
         return prepare(query, parameters).getResultList();
     }
 
     @Transactional
     @Override
     public List<T> list(TypedQuery<T> query, Object... parameters) {
         return prepare(query, parameters).getResultList();
     }
 
     @Transactional
     @Override
     public List<T> list(String queryName, Object... parameters) {
         return list(entityManager().createNamedQuery(queryName), parameters);
     }
 
     /**
      * Creates a typed query using {@link #entityClass()}. This is like a <code>SELECT * FROM T</code>.
      *
      * @return a typed query without any restrictions
      */
     TypedQuery<T> getTypedQuery() {
         final CriteriaBuilder builder = entityManager().getCriteriaBuilder();
         final CriteriaQuery<T> criteria = builder.createQuery(entityClass());
        criteria.from(entityClass());
         return entityManager().createQuery(criteria);
     }
 
     @Transactional
     @Override
     public List<T> iterate() {
         return getTypedQuery().getResultList();
     }
 
     @Override
     public Iterable<T> iterate(int batchSize) {
         final TypedQuery<T> query = getTypedQuery();
         return iterate(query, batchSize);
     }
 
     @Override
     public Iterable<T> iterate(TypedQuery<T> query, int batchSize) {
         return new PreloadingIterable<T>(query, batchSize);
     }
 
     @Transactional
     @Override
     @SuppressWarnings("unchecked")
     public <P> P projection(Query query, Object... parameters) {
         return (P) prepare(query, parameters).getSingleResult();
     }
 
     @Override
     public <P> P projection(TypedQuery<P> query, Object... parameters) {
         return prepare(query, parameters).getSingleResult();
     }
 
     @Transactional
     @Override
     public <P> P projection(String queryName, Object... parameters) {
         return projection(entityManager().createNamedQuery(queryName), parameters);
     }
 
     @Transactional
     @Override
     @SuppressWarnings("unchecked")
     public <P> P[] projections(Query query, Object... parameters) {
         return (P[]) prepare(query, parameters).getSingleResult();
     }
 
     @Transactional
     @Override
     public <P> P[] projections(String queryName, Object... parameters) {
         return projections(entityManager().createNamedQuery(queryName), parameters);
     }
 
     @Transactional
     @Override
     @SuppressWarnings("unchecked")
     public <P> List<P> projectionList(Query query, Object... parameters) {
         return prepare(query, parameters).getResultList();
     }
 
     @Override
     public <P> List<P> projectionList(TypedQuery<P> query, Object... parameters) {
         return prepare(query, parameters).getResultList();
     }
 
     @Transactional
     @Override
     public <P> List<P> projectionList(String queryName, Object... parameters) {
         return projectionList(entityManager().createNamedQuery(queryName), parameters);
     }
 
     @Transactional
     @Override
     @SuppressWarnings("unchecked")
     public <P> List<P[]> projectionsList(Query query, Object... parameters) {
         return prepare(query, parameters).getResultList();
     }
 
     @Transactional
     @Override
     public <P> List<P[]> projectionsList(String queryName, Object... parameters) {
         return projectionsList(entityManager().createNamedQuery(queryName), parameters);
     }
 
     private void doPrepare(Query query, Object[] parameters) {
         for (int i = 0; i < parameters.length; i++) {
             query.setParameter(i + 1, parameters[i]);
         }
     }
 
     @Override
     public Query prepare(Query query, Object... parameters) {
         doPrepare(query, parameters);
         return query;
     }
 
     @Override
     public <X> TypedQuery<X> prepare(TypedQuery<X> query, Object... parameters) {
         doPrepare(query, parameters);
         return query;
     }
 
     @Override
     public TypedQuery<T> prepare(String queryName, Object... parameters) {
         return prepare(entityManager().createNamedQuery(queryName, entityClass()), parameters);
     }
     
 }
