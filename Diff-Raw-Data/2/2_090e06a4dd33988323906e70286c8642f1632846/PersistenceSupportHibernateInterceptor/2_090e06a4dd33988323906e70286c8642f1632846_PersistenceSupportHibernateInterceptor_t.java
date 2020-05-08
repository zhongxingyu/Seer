 package com.derbysoft.common.hibernate;
 
 import com.derbysoft.common.domain.BasePersistenceSupport;
 import com.derbysoft.common.domain.PersistenceSupport;
 import org.hibernate.EmptyInterceptor;
 import org.hibernate.type.Type;
 
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * @author zhupan
  * @version 1.0
  * @since 2009-3-19
  */
 public class PersistenceSupportHibernateInterceptor extends EmptyInterceptor {
 
     @Override
     public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
         if (BasePersistenceSupport.class.isInstance(entity)) {
             BasePersistenceSupport basePersistenceSupport = (BasePersistenceSupport) entity;
             boolean modified = false;
             for (int i = 0; i < propertyNames.length; i++) {
                 if ("createTime".equals(propertyNames[i])) {
                     Date createTime = getCurrentTime();
                     state[i] = createTime;
                     if (basePersistenceSupport.getCreateTime() == null) {
                         basePersistenceSupport.setCreateTime(createTime);
                     }
                     modified = true;
                } else if (PersistenceSupport.class.isInstance(entity) && "lastModifyTime".equals(propertyNames[i])) {
                     Date updateTime = getCurrentTime();
                     state[i] = updateTime;
                     PersistenceSupport persistenceSupport = (PersistenceSupport) basePersistenceSupport;
                     if (persistenceSupport.getLastModifyTime() == null) {
                         persistenceSupport.setLastModifyTime(updateTime);
                     }
                     modified = true;
                 }
             }
             return modified;
         }
         return false;
     }
 
     @Override
     public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
         if (PersistenceSupport.class.isInstance(entity)) {
             PersistenceSupport persistenceSupport = (PersistenceSupport) entity;
             boolean modified = false;
             for (int i = 0; i < propertyNames.length; i++) {
                 if ("lastModifyTime".equals(propertyNames[i])) {
                     Date updateTime = getCurrentTime();
                     currentState[i] = updateTime;
                     persistenceSupport.setLastModifyTime(updateTime);
                     modified = true;
                 }
             }
             return modified;
         }
         return false;
     }
 
     private Date getCurrentTime() {
         return new Date();
     }
 
 
 }
