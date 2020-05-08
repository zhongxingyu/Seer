 /*
  * Copyright 2007-2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.shept.org.springframework.web.servlet.mvc.support;
 
 import java.lang.reflect.Method;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.shept.org.springframework.beans.support.CommandSupplier;
 import org.shept.org.springframework.beans.support.ModelSupplier;
 import org.shept.org.springframework.web.servlet.mvc.delegation.configuration.ChainConfiguration;
 import org.shept.persistence.ModelCreation;
 import org.shept.persistence.provider.DaoUtils;
 import org.springframework.beans.BeanUtils;
 import org.springframework.dao.support.DaoSupport;
 import org.springframework.util.Assert;
 import org.springframework.util.ReflectionUtils;
 import org.springframework.util.StringUtils;
 
 /** 
  * @version $$Id: ModelUtils.java 110 2011-02-21 09:16:15Z aha $$
  *
  * @author Andi
  *
  */
 public class ModelUtils {
 	
 	public static String initMethod = "initialize";
 
 	
 	/** Logger that is available to subclasses */
 	protected static Log logger = LogFactory.getLog(ModelUtils.class);
 
 	/**
 	 * Unwrap a command or model from its envelope.
 	 * You may have compound objects as a result of a database query and now we need
 	 * to obtain the base object entity.
 	 * 
 	 * If commandOrModel is an Array we take the first object from the array.
 	 * If commandOrModel implements ModelSupplier then we ask for the wrapped object.
 	 * Note that cascading will be done until we have reached the bare object.
 	 * Not that the ModelSupplier interface should never be implemented by Entity model objects
 	 * 
 	 * Current implementation doesn't check for recursion
 	 * 
 	 * @param commandOrModel
 	 * @return
 	 */
 	public static Object unwrapIfNecessary(Object commandOrModel) {
 		if (commandOrModel == null) {
 			return null;
 		}
 		if (commandOrModel.getClass().isArray() && ((Object[]) commandOrModel).length > 0) {
 			return unwrapIfNecessary(((Object[]) commandOrModel)[0]);
 		}
 		if (commandOrModel instanceof ModelSupplier) {
 			// TODO might be subject of recursion
 			return unwrapIfNecessary(((ModelSupplier)commandOrModel).getModel());
 		}
 		return commandOrModel;
 	}
 
 	/**
 	 * This is the counterpart of {@link #unwrapIfNecessary(Object)}
 	 * When showing model entity objects in a form we may want to decorate them
 	 * with additional information that should'nt be saved to the database,
 	 * e.g. repeat information fields (passwords other temporary or compound fields)
 	 * Wrapping provides a solution to present a command object instead of the real model entity.
 	 * 
 	 * @param model
 	 * @return
 	 */
 	public static Object wrapIfNecessary(Object model) {
 		if (model instanceof CommandSupplier) {
 			return wrapIfNecessary(((CommandSupplier) model).getCommand());
 		}
 		return model;
 	}
 	
 	/**
 	 * Trying to copy a model as generic as possible when no Dao layer is available
 	 * 1st we check if a #clone method is implemented. In this case we will use object.clone()
 	 * Then we will resort to a shallowCopy of the object.
 	 * 
 	 * @param model
 	 * @return
 	 */
 	public static Object copyModel(Object model) {
 		Object newModel = null;
 		if (model == null) {
 			return null;
 		}
 		newModel = cloneCopy(model);
 		if (newModel != null) {
 			return newModel;
 		}
 		return shallowCopy(model);
 	}
 
 	/**
 	 * Trying to copy a model as generic as possible when Dao Layer is available
 	 * 1st we check if a #clone method is implemented. In this case we will use object.clone()
 	 * 2nd we check if it is an entity supported by the dao. In this case we will use the entity-copy method provided by the dao
 	 * At last we will resort to a shallowCopy of the object.
 	 * 
 	 * @param model
 	 * @return
 	 */
 	public static Object copyModel(DaoSupport dao, Object model) {
 		Object newModel = null;
 		if (model == null) {
 			return null;
 		}
 		newModel = cloneCopy(model);
 		if (newModel != null) {
 			return newModel;
 		}
 		if (DaoUtils.isEntity(dao, model)) {
 			newModel = DaoUtils.deepCopyModel(dao, model);
 		} else {
 			newModel = shallowCopy(model);
 		}
 		return newModel;
 	}
 
 	/**
 	 * When the object to be copied provides a clone implementation
 	 * return a clone of the object.
 	 * 
 	 * @param model
 	 * @return
 	 */
 	private static Object cloneCopy(Object model) {
 		Object newModel = null;
 		Method mth = ReflectionUtils.findMethod(model.getClass(), "clone");
 		if (mth != null) {
 			try {
 				newModel = ReflectionUtils.invokeMethod(mth, model);
 			} catch (Exception ex) {
 				// ignore any exception during cloning
 			}
 		}
 		return newModel;
 	}
 	
 	/**
 	 * Answer a shallow copy of the object
 	 * @param model
 	 * @return
 	 */
 	private static Object shallowCopy(Object model) {
 		Object newModel = BeanUtils.instantiate(model.getClass());
 		BeanUtils.copyProperties(model, newModel);
 		return newModel;
 	}
 
 	/**
 	 * 
 	 * @return the newModelTemplate if any
 	 */
 	public static ModelCreation getNewModelTemplate(Class<?> clazz, Object model, String initString ) {
 		if (clazz == null) return null;
 		Object entity = BeanUtils.instantiate(clazz);
 		if (!(entity instanceof ModelCreation)) {
 			logger.warn("The entity model '" + entity.getClass() 
 					+ "' does not implement the interface 'ModelCreation' itself and /or "
 					+ " the return value of all of its '#initialize()' - methods. "
 					+ " No new row objects will be created.");
 			return null;
 		}
 		if (StringUtils.hasText(initString)) {
 			initialize(entity, initString);
 		}
 		if (model != null) {
 			initialize(entity, model);
 		}
 		return (ModelCreation) entity;
 	}
 	
 	/**
 	 * 
 	 * @return the newModelTemplate if any
 	 */
 	public static ModelCreation getNewModelTemplate(Class<?> clazz, String initString) {
 		return getNewModelTemplate(clazz, null, initString);
 	}
 
 	/**
 	 * 
 	 * @return the newModelTemplate if any
 	 */
 	public static ModelCreation getNewModelTemplate(ChainConfiguration cc, Object model, String initString ) {
 		return getNewModelTemplate(cc.getEntityClass(), model, initString);
 	}
 
 	/**
 	 * 
 	 * @return the initialize(sourceObject) a target object from a source
 	 */
 	public static void initialize(Object target, Object sourceModel) {
 		if (sourceModel == null) {
 			return;
 		}
 		Assert.notNull(target, "The object being initialized with '" + sourceModel + "' (of class '" + sourceModel.getClass() + "') may not be null");
 		Method mth = ReflectionUtils.findMethod(target.getClass(), initMethod, sourceModel.getClass());
 		if (mth == null) {
 			String message = "The object of class '" + target.getClass() 
 					+ "' cannot be initialized from model '" + sourceModel.getClass() 
 					+ "' Method '" + initMethod + "(" + sourceModel.getClass() + ")' is missing";
 			if (sourceModel instanceof String) {
 				logger.info(message);
 			} else {
 				logger.warn(message);
 			}
 		} else {
 			// void initialize(sourceObject)
 			ReflectionUtils.invokeMethod(mth, target, sourceModel);
 		}
 	}
 
 }
