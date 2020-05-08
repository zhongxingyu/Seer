 /*
  * Copyright 2006-2007 Luca Garulli (luca.garulli--at--assetdata.it)
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
 
 package org.romaframework.core.flow;
 
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.aspect.session.SessionInfo;
 import org.romaframework.core.Roma;
 import org.romaframework.core.Utility;
 import org.romaframework.core.config.ApplicationConfiguration;
 import org.romaframework.core.config.ContextException;
 import org.romaframework.core.config.RomaApplicationContext;
 import org.romaframework.core.exception.ConfigurationNotFoundException;
 import org.romaframework.core.schema.SchemaObject;
 
 /**
  * Manager of forms, user objects and their relationships. Track all changes in objects. It's used to update changed properties of
  * objects after user's code execution.
  * 
  * @author Luca Garulli (luca.garulli--at--assetdata.it)
  */
 @SuppressWarnings("unchecked")
 public class ObjectContext {
 
 	private static final int					WAIT_TIME_MILLISECS	= 100;
 
 	private static ThreadLocalContext	localContext;
 
 	private static ObjectContext			instance						= new ObjectContext();
 	private static Log								log									= LogFactory.getLog(ObjectContext.class);
 
 	protected ObjectContext() {
 		localContext = new ThreadLocalContext();
 	}
 
 	/**
 	 * Get a component configured in the IoC system.
 	 * 
 	 * @param iClass
 	 *          Interface of component implementation
 	 * @return the component if any or null if not found
 	 */
 	public <T> T getComponent(Class<T> iClass) throws ContextException {
 		// FOR RETRO-COMPATIBILITY
 		if (Utility.getClassName(iClass).endsWith("Aspect"))
 			return Roma.aspect(iClass);
 
 		return RomaApplicationContext.getInstance().getComponentAspect().getComponent(iClass);
 	}
 
 	/**
 	 * Get a component configured in the IoC system.
 	 * 
 	 * @param iName
 	 *          Component name
 	 * @return the component if any or null if not found
 	 */
 	public <T> T getComponent(String iName) throws ContextException {
 		return (T) RomaApplicationContext.getInstance().getComponentAspect().getComponent(iName);
 	}
 
 	/**
 	 * Check if a component was configured in the IoC system.
 	 * 
 	 * @param iClass
 	 *          Interface of component implementation
 	 * @return true if was configured, otherwise null
 	 */
 	public boolean existComponent(Class<? extends Object> iClass) {
 		return existComponent(Utility.getClassName(iClass));
 	}
 
 	/**
 	 * Check if a component was configured in the IoC system.
 	 * 
 	 * @param iComponentName
 	 *          Name of component to search
 	 * @return true if was configured, otherwise null
 	 */
 	public boolean existComponent(String iComponentName) {
 		return RomaApplicationContext.getInstance().getComponentAspect().existComponent(iComponentName);
 	}
 
 	public boolean existContextComponent(Class<? extends Object> iClass) {
 		return existContextComponent(Utility.getClassName(iClass));
 	}
 
 	/**
 	 * Check if a component was configured in the current thread's context.
 	 * 
 	 * @param iComponentName
 	 *          Name of component to search
 	 * @return true if was configured, otherwise null
 	 */
 	public boolean existContextComponent(String iComponentName) {
 		return localContext.get().get(iComponentName) != null;
 	}
 
 	/**
 	 * Refresh a property feature and/or value.
 	 * 
 	 * @param iUserObject
 	 *          The User Object of changed property
 	 * @param iFieldNames
 	 *          Optional field names to signal the change
 	 */
 	@Deprecated
 	public void fieldChanged(Object iUserObject, String... iFieldNames) {
 		Roma.fieldChanged(null, iUserObject, iFieldNames);
 	}
 
 	/**
 	 * Refresh a property feature and/or value of iUserSession session.
 	 * 
 	 * @param iUserSession
 	 *          The User Session
 	 * @param iUserObject
 	 *          The User Object of changed property
 	 * @param iFieldNames
 	 *          Optional field names to signal the change
 	 */
 	@Deprecated
 	public void fieldChanged(SessionInfo iUserSession, Object iUserObject, String... iFieldNames) {
 		Roma.fieldChanged(iUserSession, iUserObject, iFieldNames);
 	}
 
 	/**
 	 * Refresh an object of iUserSession session.
 	 * 
 	 * @param iUserSession
 	 *          The User Session
 	 * @param iUserObject
 	 *          The User Object of changed property
 	 */
 	@Deprecated
 	public void objectChanged(SessionInfo iUserSession, Object iUserObject) {
 		Roma.objectChanged(iUserSession, iUserObject);
 	}
 
 	/**
 	 * Get the schema object associated to the current POJO.
 	 * 
 	 * @param iUserObject
 	 *          User POJO
 	 * @return SchemaObject instance
 	 * @throws ConfigurationNotFoundException
 	 */
 	@Deprecated
 	public SchemaObject getSchemaObject(Object iUserObject) throws ConfigurationNotFoundException {
 		return Roma.session().getSchemaObject(iUserObject);
 	}
 
 	/**
 	 * Update a class feature of a SchemaClassDefinition instance. The SchemaClassDefinition is taken by current session form.
 	 * 
 	 * @param iUserObject
 	 *          POJO to apply
 	 * @param iAspectName
 	 *          Name of the feature's aspect
 	 * @param iFeatureName
 	 *          Name of the feature to change
 	 * @param iFeatureValue
 	 *          New value of the feature
 	 * @return true if changed, otherwise false
 	 * @throws ConfigurationNotFoundException
 	 */
 	@Deprecated
 	public boolean setClassFeature(Object iUserObject, String iAspectName, String iFeatureName, Object iFeatureValue) throws ConfigurationNotFoundException {
 		return Roma.setClassFeature(iUserObject, iAspectName, iFeatureName, iFeatureValue);
 	}
 
 	/**
 	 * Update a field feature. The SchemaClassDefinition is taken by current session form.
 	 * 
 	 * @param iUserObject
 	 *          POJO to apply
 	 * @param iAspectName
 	 *          Name of the feature's aspect
 	 * @param iFieldName
 	 *          Name of field to apply
 	 * @param iFeatureName
 	 *          Name of feature to change
 	 * @param iFeatureValue
 	 *          New value of feature
 	 * @return true if changed, otherwise false
 	 * @throws ConfigurationNotFoundException
 	 */
 	@Deprecated
	public boolean setFieldFeature(Object iUserObject, String iAspectName, String iFieldName, String iFeatureName, Object iFeatureValue) throws ConfigurationNotFoundException {
		return Roma.setFieldFeature(iUserObject, iAspectName, iFieldName, iFeatureName, iFeatureValue);
 	}
 
 	/**
 	 * Update an action feature of a SchemaClassDefinition instance.
 	 * 
 	 * @param iUserObject
 	 *          POJO to apply
 	 * @param iAspectName
 	 *          Name of the feature's aspect
 	 * @param iActionName
 	 *          Name of the action to apply
 	 * @param iFeatureName
 	 *          Name of the feature to change
 	 * @param iFeatureValue
 	 *          New value of the feature
 	 * @return true if changed, otherwise false
 	 * @throws ConfigurationNotFoundException
 	 */
 	@Deprecated()
 	public boolean setActionFeature(Object iUserObject, String iAspectName, String iActionName, String iFeatureName, Object iFeatureValue) throws ConfigurationNotFoundException {
 		return Roma.setActionFeature(iUserObject, iAspectName, iActionName, iFeatureName, iFeatureValue);
 	}
 
 	/**
 	 * Get the component from the current thread's context.
 	 * 
 	 * @param iClass
 	 *          Class of the component to get.
 	 * @return Component if found, otherwise null
 	 */
 	public <T> T getContextComponent(Class<T> iClass) {
 		return (T) getContextComponent(Utility.getClassName(iClass));
 	}
 
 	/**
 	 * Get the component from the current thread's context.
 	 * 
 	 * @param iComponent
 	 *          Component name to get.
 	 * @return Component if found, otherwise null
 	 */
 	public <T> T getContextComponent(String iComponent) {
 		T localComponent = (T) localContext.get().get(iComponent);
 		T component = null;
 
 		List<ContextLifecycleListener> listeners = Controller.getInstance().getListeners(ContextLifecycleListener.class);
 		if (listeners != null)
 			for (ContextLifecycleListener listener : listeners) {
 				try {
 					component = (T) listener.onContextRead(iComponent, localComponent);
 					if (component != null)
 						return (T) component;
 				} catch (Throwable t) {
 					log.error("[ObjectContext.getContextComponent] listener: " + listener, t);
 				}
 			}
 
 		return localComponent;
 	}
 
 	/**
 	 * Get a component configured in the IoC system. If not yet available wait until is configured.
 	 * 
 	 * @param iClass
 	 *          Interface of component implementation
 	 * @return the component if any or null if not found
 	 */
 	public <T> T waitForComponent(Class<T> iClass) {
 		while (true) {
 			// REGISTER THE OWN CALENDAR
 			if (ObjectContext.getInstance().existComponent(iClass)) {
 				return Roma.component(iClass);
 			}
 			try {
 				Thread.sleep(WAIT_TIME_MILLISECS);
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 
 	/**
 	 * Get from Context a component configured in the IoC system. If not yet available wait until is configured.
 	 * 
 	 * @param iClass
 	 *          Interface of component implementation
 	 * @return the component if any or null if not found
 	 */
 	public <T> T waitForContextComponent(Class<T> iClass) {
 		while (true) {
 			// REGISTER THE OWN CALENDAR
 			if (ObjectContext.getInstance().existContextComponent(iClass)) {
 				return ObjectContext.getInstance().getContextComponent(iClass);
 			}
 			try {
 				Thread.sleep(WAIT_TIME_MILLISECS);
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 
 	/**
 	 * Set a component in the thread's context.
 	 * 
 	 * @param iClass
 	 *          Class of the component to set.
 	 * @param iValue
 	 *          Component instance
 	 */
 	public void setContextComponent(Class<? extends Object> iClass, Object iValue) {
 		setContextComponent(Utility.getClassName(iClass), iValue);
 	}
 
 	/**
 	 * Set a component in the thread's context.
 	 * 
 	 * @param iComponentName
 	 *          Component name to set.
 	 * @param iValue
 	 *          Component instance
 	 */
 	public void setContextComponent(String iComponentName, Object iValue) {
 		if (iValue == null)
 			localContext.get().remove(iComponentName);
 		else
 			localContext.get().put(iComponentName, iValue);
 	}
 
 	/*
 	 * Logout current user session.
 	 */
 	public void logout() {
 		// CALL CUSTOM APPLICATION CALLBACK
 		Roma.component(ApplicationConfiguration.class).destroyUserSession();
 	}
 
 	/**
 	 * Get the singleton instance.
 	 * 
 	 * @return The singleton instance.
 	 */
 	public static ObjectContext getInstance() {
 		return instance;
 	}
 }
