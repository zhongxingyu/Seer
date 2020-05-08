 /*
  * Copyright 2013 Pijin Limited
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
 
 package com.pijin.nicorobin.dao;
 
 import com.pijin.nicorobin.model.OwnedEntity;
 import com.pijin.nicorobin.model.User;
 import org.hibernate.Criteria;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
 
 import java.io.Serializable;
 import java.util.Collection;
 
 /**
  * @author John Pang
  */
 public class OwnedEntityDaoImpl<T extends OwnedEntity> implements OwnedEntityDao<T> {
 
     public OwnedEntityDaoImpl(SessionFactory sessionFactory, Dao<T> baseDao) {
         this.sessionFactory = sessionFactory;
         this.baseDao = baseDao;
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public Collection<T> findAllByUser(User user) {
         Criteria criteria = sessionFactory.getCurrentSession().createCriteria(baseDao.getEntityClass());
         criteria.add(Restrictions.eq("owner", user));
         criteria.setCacheable(true);
         return criteria.list();
     }
 
     @Override
     public Collection<T> findAllPublic() {
         Criteria criteria = sessionFactory.getCurrentSession().createCriteria(baseDao.getEntityClass());
         criteria.add(Restrictions.eq("isPublic", true));
         criteria.add(Restrictions.isNull("deletedDate"));
         criteria.setCacheable(true);
        return criteria.list();    }
 
     @Override
     public Collection<T> findAll() {
         return baseDao.findAll();
     }
 
     @Override
     public T findById(Serializable id) {
         return baseDao.findById(id);
     }
 
     @Override
     public void save(T entity) {
         baseDao.save(entity);
     }
 
     @Override
     public void update(T entity) {
         baseDao.update(entity);
     }
 
     @Override
     public void delete(T entity) {
         baseDao.delete(entity);
     }
 
     @Override
     public Class<T> getEntityClass() {
         return baseDao.getEntityClass();
     }
 
     private SessionFactory sessionFactory;
     private Dao<T> baseDao;
 }
