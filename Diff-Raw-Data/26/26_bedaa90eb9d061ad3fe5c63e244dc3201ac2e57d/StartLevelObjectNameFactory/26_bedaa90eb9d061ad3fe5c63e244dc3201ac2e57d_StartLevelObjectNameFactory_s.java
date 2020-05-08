 /**
  * Copyright 2008 buschmais GbR
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.
  * See the License for the specific language governing permissions and
  * limitations under the License
  */
 package com.buschmais.osgi.maexo.mbeans.osgi.core.impl.objectname;
 
 import java.util.Properties;
 
 import javax.management.ObjectName;
 
 import com.buschmais.osgi.maexo.framework.commons.mbean.objectname.ObjectNameFactory;
 import com.buschmais.osgi.maexo.framework.commons.mbean.objectname.ObjectNameHelper;
 import com.buschmais.osgi.maexo.mbeans.osgi.core.StartLevelConstants;
 
 /**
  * Object name factory implementation which supports the start level service
  */
 public class StartLevelObjectNameFactory implements ObjectNameFactory {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
	 * com.buschmais.maexo.core.registry.ObjectNameFactory#getObjectName(java
	 * .lang.Object)
 	 */
 	public ObjectName getObjectName(Object resource) {
 		// create object name properties
 		Properties objectNameProperties = new Properties();
 		// type
 		objectNameProperties.setProperty(
 				StartLevelConstants.OBJECTNAME_TYPE_PROPERTY,
 				StartLevelConstants.OBJECTNAME_TYPE_VALUE);
 		return ObjectNameHelper.getObjectName(objectNameProperties);
 	}
 }
