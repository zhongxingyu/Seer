 package com.aciertoteam.common.service.impl;
 
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
import com.aciertoteam.common.entity.AbstractEntity;
import com.aciertoteam.common.interfaces.IAbstractEntity;
import com.aciertoteam.common.repository.EntityRepository;
import com.aciertoteam.common.service.EntityService;
 
 /**
  * @author Bogdan Nechyporenko
  */
@Service(value = "entityService")
 @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
 public class DefaultEntityService implements EntityService {
 
     @Autowired
     private EntityRepository entityRepository;
 
     @Override
     public boolean isEmpty(Class clazz) {
         return entityRepository.count(clazz) == 0;
     }
 
     @Override
     public <T extends IAbstractEntity> T findById(Class<T> clazz, Long id) {
         return entityRepository.findById(clazz, id);
     }
 
     @Override
     public <T extends IAbstractEntity> List<T> findByIds(Class<T> clazz, List<Long> ids) {
         return entityRepository.findByIds(clazz, ids);
     }
 
     @Override
     public <T extends IAbstractEntity> List<T> findAll(Class<T> clazz) {
         return entityRepository.findAll(clazz);
     }
 
     @Override
     public <T extends IAbstractEntity> List<T> findAllOrderedByTimestamp(Class<T> clazz, boolean asc) {
         return entityRepository.findAllOrderedByTimestamp(clazz, asc);
     }
 
     @Override
     public <T extends IAbstractEntity> Set<T> findAllAsSet(Class<T> clazz) {
         return new HashSet<T>(findAll(clazz));
     }
 
     @Override
     public <T extends IAbstractEntity> void saveOrUpdate(T... list) {
         for (T t : list) {
             entityRepository.saveOrUpdate((AbstractEntity) t);
         }
     }
 
     @Override
     public <T extends IAbstractEntity> void saveOrUpdate(Collection<T> collection) {
         for (T t : collection) {
             entityRepository.saveOrUpdate((AbstractEntity) t);
         }
     }
 
     @Override
     public <T extends IAbstractEntity> void markAsDeleted(T t) {
         t.closeEndPeriod();
         entityRepository.saveOrUpdate((AbstractEntity) t);
     }
 
     @Override
     public <T> T findByField(Class clazz, String fieldName, Object value) {
         return entityRepository.findByField(clazz, fieldName, value);
     }
 
     @Override
     public void delete(List<AbstractEntity> entities) {
         entityRepository.delete(entities);
     }
 
     @Override
     public void saveAll(Collection<AbstractEntity> entities) {
         entityRepository.saveAll(entities);
     }
 
 }
