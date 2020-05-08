 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.services.authorizationservice.impl.cache;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.ebayopensource.turmeric.services.authorizationservice.impl.biz.InternalAuthorizationServiceImpl;
 
 import com.ebay.kernel.bean.configuration.BaseConfigBean;
 import com.ebay.kernel.bean.configuration.BeanConfigCategoryInfo;
 import com.ebay.kernel.bean.configuration.BeanPropertyInfo;
 import com.ebay.kernel.bean.configuration.ConfigCategoryCreateException;
 import com.ebay.kernel.logger.LogLevel;
 import com.ebay.kernel.logger.Logger;
 
 /**
  * This is the Configuration Management Bean to toggle the 
  * Authorization Service Cache ON or OFF.
  *  
  * @author mpoplacenel
  */
 public class AuthorizationServiceCacheToggleBean extends BaseConfigBean {
 
 	private static final long serialVersionUID = 0L;
 	
 	/**
 	 * Name of the "cache enabled" property.
 	 */
 	protected static final String CACHE_ON_PROPERTY_NAME = "m_cacheOn";
 	
 	/**
 	 * External name of the property name. 
 	 */
 	protected static final String CACHE_ON_PROPERTY_EXTERNAL_NAME = "cacheOn";
 	
 	/**
 	 * Bean name. 
 	 */
 	public static final String NAME = "org.ebayopensource.turmeric.services.authorizationservice.impl.cache.AuthorizationService.CacheToggle";
 	
 	private static final Logger LOGGER = Logger.getInstance(AuthorizationServiceCacheToggleBean.class);
 	
 	/**
 	 * CacheOn Property Descriptor - discovered automatically through reflection by the CM framework.
 	 */
 	private static final BeanPropertyInfo CACHE_ON_PROPERTY_INFO = 
 		createBeanPropertyInfo(CACHE_ON_PROPERTY_NAME, CACHE_ON_PROPERTY_EXTERNAL_NAME, true);
 	
 	/**
 	 * The only property exposed by this bean - cache on or off (off by default).
 	 */
 	private Boolean m_cacheOn = Boolean.TRUE; // true by default
 	
 	private static AuthorizationServiceCacheToggleBean s_instance = new AuthorizationServiceCacheToggleBean();
 	
 	/**
 	 * Singleton instance accessor. 
 	 * @return the singleton instance. 
 	 */
 	public static AuthorizationServiceCacheToggleBean getInstance() {
 		return s_instance;
 	}
 	
 	/**
 	 * Constructor. 
 	 */
 	public AuthorizationServiceCacheToggleBean() {
 
 		try {
 			BeanConfigCategoryInfo beanInfo =
 				BeanConfigCategoryInfo.createBeanConfigCategoryInfo(
 					NAME,
 					null, // alias
 					"org.ebayopensource.turmeric.services.authorizationservice.impl.cache", 
 					true, // persistent
 					true, //ops manageable
 					"AuthzCacheToggle", // persistent file
 					"Authorization Service Cache Toggle Bean - toggles cache ON and OFF", // description
 					true // return an existing one
 				);
 			
 			addPropertyChangeListener(new PropertyChangeListener() {
 				@Override
 				public void propertyChange(PropertyChangeEvent event) {
 					if (!CACHE_ON_PROPERTY_EXTERNAL_NAME.equals(event.getPropertyName())) {
 						if (LOGGER.isLogEnabled(LogLevel.WARN)) {
 							LOGGER.log(LogLevel.WARN, "Unrecognized property " + event.getPropertyName() 
 									+ " passed in");
 						}
 						return;
 					}
 					Object newValue = event.getNewValue();
 					if (newValue == null) {
 						if (LOGGER.isLogEnabled(LogLevel.WARN)) {
 							LOGGER.log(LogLevel.WARN, "Null new value being passed in");
 						}
 						return;
 					}
 					Object oldValue = event.getOldValue();
 					if (newValue.equals(oldValue)) {
 						if (LOGGER.isLogEnabled(LogLevel.WARN)) {
 							LOGGER.log(LogLevel.WARN, "Equal values being passed into the listener");
 						}
 						return;
 					}
 					if (!(newValue instanceof Boolean)) {
 						if (LOGGER.isLogEnabled(LogLevel.WARN)) {
 							LOGGER.log(LogLevel.WARN, "Unknown new value " + newValue
									+ ((newValue == null) ? "" : " of class " + newValue.getClass().getName())
 									+ " was passed in - ignoring..."); 
 						}
 					}
 					boolean newCacheOn = ((Boolean) newValue).booleanValue();
 					if (LOGGER.isLogEnabled(LogLevel.INFO)) {
 						LOGGER.log(LogLevel.INFO, "Setting cache to " + (newCacheOn ? "ON" : "OFF"));
 					}
 					InternalAuthorizationServiceImpl.getInstance().setCacheOn(newCacheOn);
 				}
 			});
 			init(beanInfo, true);
 		} catch (RuntimeException e) {
 			if (LOGGER.isLogEnabled(LogLevel.ERROR))
 				LOGGER.log(LogLevel.ERROR, "Unable to initialize bean " + NAME, e);
 			throw e;
 		} catch (ConfigCategoryCreateException e) {
 			if (LOGGER.isLogEnabled(LogLevel.ERROR))
 				LOGGER.log(LogLevel.ERROR, "Unable to initialize bean " + NAME, e);
 		} catch (Error e) {
 			if (LOGGER.isLogEnabled(LogLevel.FATAL))
 				LOGGER.log(LogLevel.FATAL, "Unable to initialize bean " + NAME, e);
 			throw e;
 		}
 		
 	}
 	
 	/**
 	 * Checks if the cache is ON. 
 	 * @return <code>true</code> if cache is ON, <code>false</code> if cache is OFF.
 	 */
 	public Boolean isCacheOn() {
 		return m_cacheOn;
 	}
 
 	/**
 	 * Turns the cache ON or OFF. 
 	 * @param cacheOn <code>true</code> to set cache ON, <code>false</code> to set cache OFF.
 	 */
 	public void setCacheOn(Boolean cacheOn) {
 		changeProperty(CACHE_ON_PROPERTY_INFO, m_cacheOn, cacheOn);
 	}
 	
 	
 
 }
