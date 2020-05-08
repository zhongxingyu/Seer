 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
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
 
 
 package com.paxxis.cornerstone.service;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  *
  * @author Robert Englander
  */
 public abstract class CornerstoneConfigurable implements IManagedBean {
 
 	/** the configuration object to use to populate property values */
     private CornerstoneConfiguration _configuration = null;
     
     /** contains a mapping of property names to configuration values */ 
     private HashMap<String, Object> _propertyMap = new HashMap<String, Object>();
     
     /** flag indicating if this instance should register for changes */
     private boolean registerForChanges = true;
     
     public CornerstoneConfigurable() {
     }
     
 	public void setConfigurationPropertyMap(Map<String, ?> localMap) {
         _propertyMap.putAll(localMap);
     }
 
     void onChange(String propName) {
     	// the prop name we want is the key mapped to this propName value.  we don't support this
     	// for non string mappings (yet).
     	Collection<String> props = new ArrayList<String>();
     	for (String key : _propertyMap.keySet()) {
     		Object obj = _propertyMap.get(key);
     		if (obj instanceof String) {
         		String value = (String)obj;
     			if (value.equals(propName)) {
         			props.add(key);
         		}
     		}
     	}
     	props.add(propName);
     	loadConfigurationPropertyValues(props);
     }
     
     /**
      * Load property values from the configuration.
      */
     public void loadConfigurationPropertyValues(Collection<String> props) {
         CornerstoneConfiguration config = getCornerstoneConfiguration();
         
         if (config != null) {
             Method[] methods = this.getClass().getMethods();
             
             for (String propName : props) {
             	Object configObject = _propertyMap.get(propName);
             	if (configObject != null) {
                 	Object value = null;
                 	if (configObject instanceof List<?>) {
                 		List<String> valueList = new ArrayList<String>();
                 		List<?> configList = (List<?>)configObject;
                 		for (Object o : configList) {
                 			String v = config.getStringValue(o.toString(), "");
                 			valueList.add(v);
                 		}
                 		
                 		value = valueList;
                 	} else {
                         value = config.getObjectValue(configObject.toString());
                 	}
 
                 	if (value != null) {
                         // get the setter
                         String firstLetter = propName.substring(0, 1).toUpperCase();
                         String setterName = "set" + firstLetter + propName.substring(1);
 
                         for (Method method : methods) {
                             if (method.getName().equals(setterName)) {
                                 Class<?>[] paramClasses = method.getParameterTypes();
                                 if (paramClasses.length == 1) {
                                     // this is the one we want, so convert the value to this type
                                     Object objValue = null;
                                     if (paramClasses[0].getName().equals("java.lang.String")) {
                                         objValue = String.valueOf(value);
                                     } else if (paramClasses[0].getName().equals("int")) {
                                         objValue = Integer.valueOf(value.toString());
                                     } else if (paramClasses[0].getName().equals("long")) {
                                         objValue = Long.valueOf(value.toString());
                                     } else if (paramClasses[0].getName().equals("float")) {
                                         objValue = Float.valueOf(value.toString());
                                     } else if (paramClasses[0].getName().equals("double")) {
                                         objValue = Double.valueOf(value.toString());
                                     } else if (paramClasses[0].getName().equals("boolean")) {
                                         objValue = Boolean.valueOf(value.toString());
                                     } else if (paramClasses[0].getName().equals("java.util.List")) {
                                     	objValue = value;                                    
                                     } else {
                                         //this covers any class (Enums most importantly) that has
                                         //a static valueOf(java.lang.String) method
                                         try {
                                             Method valueOf = paramClasses[0].getMethod(
                                                     "valueOf", 
                                                     String.class);
                                             objValue = valueOf.invoke(null, value);
                                         } catch (Exception e) {
                                             throw new RuntimeException(e);
                                         }
                                     }
                                     
                                     try {
                                         method.invoke(this, objValue);
                                     } catch (Exception e) {
                                         throw new RuntimeException(e);
                                     }
 
                                     break;
                                 }
                             }
                         }
                     }
             	}
             }
         }
     }
 
     
     /**
      * Set the cornerstoneConfiguration property.  Setting this property
      * causes the initialization process to use the configuration object
      * to retrieve property values immediately.
      *
      */
     public void setCornerstoneConfiguration(CornerstoneConfiguration configuration) {
         _configuration = configuration;
         Set<String> props = _propertyMap.keySet();
         loadConfigurationPropertyValues(props);
     }
     
     /**
      * Get the Configuration object
      *
      */
     public CornerstoneConfiguration getCornerstoneConfiguration() {
         return _configuration;
     }
     
     public void setRegisterForChanges(boolean register) {
     	this.registerForChanges = register;
     }
     
     /**
      * Initialize the object
      */
     public void initialize() {
    	if (registerForChanges) {
             _configuration.registerConfigurable(this);
     	}
     }
     
     /**
      * Tear down the object
      */
     public void destroy() {
     }
 }
