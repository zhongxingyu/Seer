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
 
 package de.cosmocode.palava.jpa;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import javax.persistence.FlushModeType;
 import javax.persistence.LockModeType;
 import javax.persistence.Query;
 
 import com.google.common.collect.ForwardingObject;
 
 import de.cosmocode.patterns.Decorator;
 
 /**
  * Abstract {@link Decorator} for {@link EntityManager}s.
  *
  * @author Willi Schoenborn
  */
 public abstract class ForwardingEntityManager extends ForwardingObject implements EntityManager {
 
     @Override
     protected abstract EntityManager delegate();
     
     @Override
     public void clear() {
         delegate().clear();
     }
 
     @Override
     public void close() {
         delegate().close();
     }
 
     @Override
     public boolean contains(Object entity) {
         return delegate().contains(entity);
     }
 
     @Override
     public Query createNamedQuery(String name) {
         return delegate().createNamedQuery(name);
     }
 
     @Override
    public Query createNativeQuery(String sqlString, @SuppressWarnings("rawtypes") Class resultClass) {
         return delegate().createNativeQuery(sqlString, resultClass);
     }
 
     @Override
     public Query createNativeQuery(String sqlString, String resultSetMapping) {
         return delegate().createNativeQuery(sqlString, resultSetMapping);
     }
 
     @Override
     public Query createNativeQuery(String sqlString) {
         return delegate().createNativeQuery(sqlString);
     }
 
     @Override
     public Query createQuery(String qlString) {
         return delegate().createQuery(qlString);
     }
 
     @Override
     public <T> T find(Class<T> entityClass, Object primaryKey) {
         return delegate().find(entityClass, primaryKey);
     }
 
     @Override
     public void flush() {
         delegate().flush();
     }
 
     @Override
     public Object getDelegate() {
         return delegate().getDelegate();
     }
 
     @Override
     public FlushModeType getFlushMode() {
         return delegate().getFlushMode();
     }
 
     @Override
     public <T> T getReference(Class<T> entityClass, Object primaryKey) {
         return delegate().getReference(entityClass, primaryKey);
     }
 
     @Override
     public EntityTransaction getTransaction() {
         return delegate().getTransaction();
     }
 
     @Override
     public boolean isOpen() {
         return delegate().isOpen();
     }
 
     @Override
     public void joinTransaction() {
         delegate().joinTransaction();
     }
 
     @Override
     public void lock(Object entity, LockModeType lockMode) {
         delegate().lock(entity, lockMode);
     }
 
     @Override
     public <T> T merge(T entity) {
         return delegate().merge(entity);
     }
 
     @Override
     public void persist(Object entity) {
         delegate().persist(entity);
     }
 
     @Override
     public void refresh(Object entity) {
         delegate().refresh(entity);
     }
 
     @Override
     public void remove(Object entity) {
         delegate().remove(entity);
     }
 
     @Override
     public void setFlushMode(FlushModeType flushMode) {
         delegate().setFlushMode(flushMode);
     }
     
 }
