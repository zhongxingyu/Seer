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
 
 package de.cosmocode.palava.jpa.aspectj;
 
 import com.google.common.base.Preconditions;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import de.cosmocode.commons.Throwables;
 import de.cosmocode.palava.core.aop.PalavaAspect;
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.SuppressAjWarnings;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import javax.persistence.PersistenceException;
 
/**
 * Aspect for methods annotated with {@link de.cosmocode.palava.jpa.Transactional}.
 */
 @Aspect
 public final class EntityTransactionAspect extends PalavaAspect {
 
     private static final Logger LOG = LoggerFactory.getLogger(EntityTransactionAspect.class);
 
     private Provider<EntityManager> currentManager;
     
     @Inject
     public void setProvider(Provider<EntityManager> manager) {
         this.currentManager = Preconditions.checkNotNull(manager, "Manager");
     }
 
    // CHECKSTYLE:OFF
     @Around("execution(@de.cosmocode.palava.jpa.Transactional * *.*(..))")
    public Object transactional(ProceedingJoinPoint point) {
         checkInjected();
 
         final EntityManager manager = currentManager.get();
         LOG.trace("Retrieved entitymanager {}", manager);
         final EntityTransaction tx = manager.getTransaction();
         LOG.trace("Using transaction {}", tx);
         
         final boolean local = !tx.isActive();
         
         if (local) {
             LOG.debug("Beginning automatic transaction {}", tx);
             tx.begin();
         } else {
             LOG.trace("Transaction {} already active", tx);
         }
         
         final Object returnValue;
         
         try {
             returnValue = point.proceed();
         } catch (Throwable e) {
             try {
                 if (local && (tx.isActive() || tx.getRollbackOnly())) {
                     LOG.debug("Rolling back local/active transaction {}", tx);
                     tx.rollback();
                 } else if (!tx.getRollbackOnly()) {
                     LOG.debug("Setting transaction {} as rollback only", tx);
                     tx.setRollbackOnly();
                 }
             } catch (PersistenceException inner) {
                 LOG.error("Rollback failed", inner);
             } finally {
                 throw Throwables.sneakyThrow(e);
             }
         }
 
         if (local && tx.isActive()) {
             if (tx.getRollbackOnly()) {
                 LOG.debug("Rolling back marked transaction {}", tx);
                 tx.rollback();
             } else {
                 try {
                     tx.commit();
                     LOG.debug("Committed automatic transaction {}", tx);
                 } catch (PersistenceException e) {
                     if (tx.isActive()) {
                         try {
                             LOG.debug("Rolling back transaction {}", tx);
                             tx.rollback();
                         } catch (PersistenceException inner) {
                             LOG.error("Rollback failed", inner);
                         }
                     }
                     throw e;
                 }
             }
         } else {
             LOG.trace("Not committing non-local transaction {}", tx);
         }
         
         return returnValue;
     }
    // CHECKSTYLE:ON
 
 }
