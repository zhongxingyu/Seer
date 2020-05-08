 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.entity;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 
 import com.google.inject.Provider;
 
 import de.cosmocode.palava.model.base.EntityBase;
 
 /**
  * Abstract skeleton implementation of the {@link ReadOnlyEntityService} interface.
  *
  * @author Willi Schoenborn
  * @param <T> the generic entity type
  */
 public abstract class AbstractReadOnlyEntityService<T extends EntityBase> implements ReadOnlyEntityService<T> {
 
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
 
     @Override
     public T get(long identifier) {
         return entityManager().find(entityClass(), identifier);
     }
     
     @Override
     public T read(long identifier) {
         final T entity = get(identifier);
         if (entity == null) {
             throw new PersistenceException(String.format("No entity found for id #%s", identifier));
         } else {
             return entity;
         }
     }
     
     @Override
     @SuppressWarnings("unchecked")
     public T read(Query query, Object... parameters) {
         return (T) prepare(query, parameters).getSingleResult();
     }
     
     @Override
     public T read(String queryName, Object... parameters) {
         return read(entityManager().createNamedQuery(queryName), parameters);
     }
     
     @Override
     public T reference(long identifier) {
         return entityManager().getReference(entityClass(), identifier);
     }
     
     @Override
     @SuppressWarnings("unchecked")
     public List<T> list(Query query, Object... parameters) {
         return prepare(query, parameters).getResultList();
     }
     
     @Override
     public List<T> list(String queryName, Object... parameters) {
         return list(entityManager().createNamedQuery(queryName), parameters);
     }
 
     @Override
     public List<T> all() {
         final String query = String.format("from %s", entityClass().getSimpleName());
         return list(entityManager().createQuery(query));
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <P> P projection(Query query, Object... parameters) {
         return (P) prepare(query, parameters).getSingleResult();
     }
 
     @Override
     public <P> P projection(String queryName, Object... parameters) {
         return this.<P>projection(entityManager().createNamedQuery(queryName), parameters);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <P> P[] projections(Query query, Object... parameters) {
         return (P[]) prepare(query, parameters).getSingleResult();
     }
     
     @Override
     public <P> P[] projections(String queryName, Object... parameters) {
         return this.<P>projections(entityManager().createNamedQuery(queryName), parameters);
     }
     
     @Override
     @SuppressWarnings("unchecked")
     public <P> List<P> projectionList(Query query, Object... parameters) {
         return prepare(query, parameters).getResultList();
     }
     
     @Override
     public <P> List<P> projectionList(String queryName, Object... parameters) {
         return projectionList(entityManager().createNamedQuery(queryName), parameters);
     }
     
     @Override
     @SuppressWarnings("unchecked")
     public <P> List<P[]> projectionsList(Query query, Object... parameters) {
         return prepare(query, parameters).getResultList();
     }
     
     @Override
     public <P> List<P[]> projectionsList(String queryName, Object... parameters) {
         return projectionsList(entityManager().createNamedQuery(queryName), parameters);
     }
     
     @Override
     public Query prepare(Query query, Object... parameters) {
         for (int i = 0; i < parameters.length; i++) {
            query.setParameter(i, parameters[i]);
         }
         return query;
     }
     
     @Override
     public Query prepare(String queryName, Object... parameters) {
         return prepare(entityManager().createNamedQuery(queryName), parameters);
     }
     
 }
