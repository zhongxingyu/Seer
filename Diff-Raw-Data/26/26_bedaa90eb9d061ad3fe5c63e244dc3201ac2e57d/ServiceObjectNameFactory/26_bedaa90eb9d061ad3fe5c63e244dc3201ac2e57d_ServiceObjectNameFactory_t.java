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
 
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 
 import com.buschmais.osgi.maexo.framework.commons.mbean.objectname.ObjectNameFactory;
 import com.buschmais.osgi.maexo.framework.commons.mbean.objectname.ObjectNameHelper;
 import com.buschmais.osgi.maexo.mbeans.osgi.core.ServiceConstants;
 
 /**
  * Object name factory implementation which supports service references
  */
 public class ServiceObjectNameFactory implements ObjectNameFactory {
 
 	/**
 	 * the separator token to use if several object classes are implemented by
 	 * the service
 	 */
 	private static final char SEPARATOR_OBJECTCLASS = '|';
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
	 * com.buschmais.osgi.maexo.framework.commons.mbean.objectname.ObjectNameFactory
	 * #getObjectName(java.lang.Object)
 	 */
 	public ObjectName getObjectName(Object resource) {
 		ServiceReference serviceReference = (ServiceReference) resource;
 		Properties objectNameProperties = new Properties();
 		// type
 		objectNameProperties.setProperty(
 				ServiceConstants.OBJECTNAME_TYPE_PROPERTY,
 				ServiceConstants.OBJECTNAME_TYPE_VALUE);
 		// id
 		Long id = (Long) serviceReference.getProperty(Constants.SERVICE_ID);
 		objectNameProperties.put(ServiceConstants.OBJECTNAME_ID_PROPERTY, id);
 		// object classes
 		String[] objectClasses = (String[]) serviceReference
 				.getProperty(Constants.OBJECTCLASS);
 		StringBuilder objectClassValue = new StringBuilder();
 		for (String objectClass : objectClasses) {
 			if (objectClassValue.length() > 0) {
 				objectClassValue.append(SEPARATOR_OBJECTCLASS);
 			}
 			objectClassValue.append(objectClass);
 		}
 		objectNameProperties.put(ServiceConstants.OBJECTNAME_NAME_PROPERTY,
 				objectClassValue);
 		// pid
 		String pid = (String) serviceReference
 				.getProperty(Constants.SERVICE_PID);
 		if (pid != null) {
 			objectNameProperties.put(ServiceConstants.OBJECTNAME_PID_PROPERTY,
 					pid);
 		}
 		return ObjectNameHelper.getObjectName(objectNameProperties);
 	}
 }
