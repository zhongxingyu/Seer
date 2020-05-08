 /*
  * Copyright (C) 2011 Jefferson Campos <foguinho.peruca@gmail.com>
  * This file is part of awknet-commons - http://awknet-commons.awknet.org
  *
  * Awknet-commons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Awknet-commons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with awknet-commons. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.awknet.commons.model.business;
 
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.awknet.commons.data.GenericDaoFactory;
 import org.awknet.commons.model.entity.BaseEntity;
 
 // TODO use singleton here...
 // TODO implement all tests
 public class RegisterBOImpl<T extends BaseEntity> implements Register<T> {
 
     private GenericDaoFactory daoFactory;
     private Class clazz;
     private static final Log LOG = LogFactory.getLog(RegisterBOImpl.class);
 
     // FIXME [GENERIC] One for money...
     public RegisterBOImpl(GenericDaoFactory _daoFactory, Class _clazz) {
 	this.clazz = _clazz;
 	daoFactory = _daoFactory;
 	LOG.debug("[clazz]: Class name is: " + clazz.getName());
     }
 
     // FIXME [GENERIC] Two for the show... must use reflection to get class name
     public RegisterBOImpl(GenericDaoFactory _daoFactory) {
 	daoFactory = _daoFactory;
	// FIXME too ugly! The master workarround! Only Chuck Norris can do!
	this.clazz = ((T) new Object()).getClass();
 	// FIXME [GENERIC] NOT working....
 	// Type type = getClass().getGenericSuperclass();
 	// clazz = (Class) getClass().getGenericSuperclass();
 	// LOG.info("-- Class name is: " + clazz.getName());
 	// ParameterizedType parameterizedType = (ParameterizedType) type;
 	// clazz = (Class) parameterizedType.getActualTypeArguments()[0];
 
     }
 
     @Override
     public T save(T _entity) {
 	daoFactory.beginTransaction();
 	daoFactory.getRegisterDao(this.clazz).save(_entity);
 	daoFactory.commit();
 	return _entity;
     }
 
     @Override
     public T update(T _entity) {
 	daoFactory.beginTransaction();
 	daoFactory.getRegisterDao(clazz).update(_entity);
 	daoFactory.commit();
 	return _entity;
     }
 
     @Override
     public void delete(T _entity) {
 	daoFactory.beginTransaction();
 	daoFactory.getRegisterDao(clazz).delete(_entity);
 	daoFactory.commit();
     }
 
     // FIXME type cast List<BaseEntity> to List<T>
     @SuppressWarnings("unchecked")
     @Override
     public List<T> listAll() {
 	return (List<T>) daoFactory.getRegisterDao(clazz).list();
     }
 
     // TODO implement security check for ID: http://code.google.com/p/cofoja/
     // FIXME type cast BaseEntity to T
     @SuppressWarnings("unchecked")
     @Override
     public T load(long id) {
 	return ((T) daoFactory.getRegisterDao(clazz).load(id));
     }
 
     // FIXME type cast BaseEntity to T
     @SuppressWarnings("unchecked")
     @Override
     public T loadByExemple(T _entity) {
 	return (T) daoFactory.getRegisterDao(clazz).loadByExample(_entity);
     }
 }
 
