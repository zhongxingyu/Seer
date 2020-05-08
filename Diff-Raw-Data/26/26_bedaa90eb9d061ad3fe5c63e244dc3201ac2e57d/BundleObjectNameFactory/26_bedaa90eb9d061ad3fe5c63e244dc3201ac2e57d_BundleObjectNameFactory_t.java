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
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.Constants;
 
 import com.buschmais.osgi.maexo.framework.commons.mbean.objectname.ObjectNameFactory;
 import com.buschmais.osgi.maexo.framework.commons.mbean.objectname.ObjectNameHelper;
 import com.buschmais.osgi.maexo.mbeans.osgi.core.BundleConstants;
 
 /**
  * Object name factory implementation which supports bundles
  */
 public class BundleObjectNameFactory implements ObjectNameFactory {
 
 	private static final String DEFAULT_BUNDLE_SYMBOLICNAME = "unknown";
 
 	private static final String DEFAULT_BUNDLE_VERSION = "0.0.0";
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
	 * com.buschmais.osgi.maexo.framework.commons.mbean.objectname.ObjectNameFactory
	 * #getObjectName(java.lang.Object)
 	 */
 	public ObjectName getObjectName(Object resource) {
 		Bundle bundle = (Bundle) resource;
 		// create object name properties
 		Properties objectNameProperties = new Properties();
 		// type
 		objectNameProperties.setProperty(
 				BundleConstants.OBJECTNAME_TYPE_PROPERTY,
 				BundleConstants.OBJECTNAME_TYPE_VALUE);
 		// create name property: <symbolic name>
 		String symbolicName = bundle.getSymbolicName();
 		if (symbolicName == null) {
 			symbolicName = DEFAULT_BUNDLE_SYMBOLICNAME;
 		}
 		objectNameProperties.setProperty(
 				BundleConstants.OBJECTNAME_NAME_PROPERTY, symbolicName);
 		// create version property
 		String bundleVersion = (String) bundle.getHeaders().get(
 				Constants.BUNDLE_VERSION);
 		if (bundleVersion == null) {
 			bundleVersion = DEFAULT_BUNDLE_VERSION;
 		}
		objectNameProperties.setProperty(
				BundleConstants.OBJECTNAME_VERSION_PROPERTY, bundleVersion);
 		return ObjectNameHelper.getObjectName(objectNameProperties);
 	}
 }
