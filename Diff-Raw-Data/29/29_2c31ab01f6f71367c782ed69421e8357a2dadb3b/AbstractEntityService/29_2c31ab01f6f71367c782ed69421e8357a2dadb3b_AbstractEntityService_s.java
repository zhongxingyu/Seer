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
 
 import javax.persistence.EntityManager;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 
 import de.cosmocode.collections.Procedure;
 import de.cosmocode.palava.jpa.Transactional;
 import de.cosmocode.palava.model.base.Copyable;
import de.cosmocode.palava.model.base.EntityBase;
 
 /**
  * Abstract base implementation of the {@link EntityService} interface.
  *
  * <p>
  *   <strong>Note</strong>: This implementation does not provide a meaningful
 *   {@link EntityService#delete(EntityBase)} method. Decisions about deleting
  *   or hiding entites are left to sub-classes. Create and update are marked
  *   with @Transactional to use or create transactions for database actions.
  * </p>
  *
  * @author Willi Schoenborn
  * @param <T> generic entity type
  */
 public abstract class AbstractEntityService<T> extends AbstractReadOnlyEntityService<T>
     implements EntityService<T> {
 
     private static final Logger LOG = LoggerFactory.getLogger(AbstractEntityService.class);
     
     @Transactional
     @Override
     public T create(T entity) {
         LOG.debug("Creating {} in database", entity);
         entityManager().persist(entity);
         return entity;
     }
 
     @Transactional
     @Override
     public T update(T entity) {
         return entityManager().merge(entity);
     }
 
     @Transactional
     @Override
     public void each(Procedure<? super T> procedure) {
         Preconditions.checkNotNull(procedure, "Procedure");
         for (T entity : iterate()) {
             procedure.apply(entity);
         }
     }
 
     @Transactional
     @Override
     public void each(Procedure<? super T> procedure, int batchSize) {
         each(procedure, batchSize, Batch.NOOP);
     }
 
     @Transactional
     @Override
     public void each(Procedure<? super T> procedure, int batchSize, Procedure<? super EntityManager> batchProcedure) {
         Preconditions.checkNotNull(procedure, "Procedure");
         Preconditions.checkNotNull(batchProcedure, "BatchProcedure");
         int i = 1;
         for (T entity : iterate(batchSize)) {
             procedure.apply(entity);
             // flush every 'batchSize' elements
             if (i++ % batchSize == 0) {
                 batchProcedure.apply(entityManager());
             }
         }
         
     }
 
     @Transactional
     @Override
     public <C extends Copyable<T>> T createCopy(C entity) {
         return create(entity.copy());
     }
 
 }
