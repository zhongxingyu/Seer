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
 package com.buschmais.osgi.maexo.mbeans.osgi.core;
 
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;
 
 public class BundleConstants {
 
 	/**
 	 * object name properties: type and name
 	 */
 	public static final String OBJECTNAME_PROPERTY_TYPE = "type";
 	public static final String OBJECTNAME_PROPERTY_NAME = "name";
 
 	/**
 	 * MBean description
 	 */
 	public static final String MBEAN_DESCRIPTION = "Bundle MBean";
 
 	/**
 	 * attribute: id
 	 */
 	public static final String ATTRIBUTE_ID_NAME = "id";
 	public static final String ATTRIBUTE_ID_DESCRIPTION = "The id of the bundle";
 
 	/**
 	 * attribute: state
 	 */
 	public static final String ATTRIBUTE_STATE_NAME = "state";
 	public static final String ATTRIBUTE_STATE_DESCRIPTION = "The current state of bundle the bundle (integer value as reported by the framework)";
 
 	/**
 	 * attribute: stateName
 	 */
 	public static final String ATTRIBUTE_STATENAME_NAME = "stateName";
 	public static final String ATTRIBUTE_STATENAME_DESCRIPTION = "The current state of bundle the bundle (human readable string value)";
 
 	/**
 	 * attribute: headers
 	 */
 	public static final String TABULARTYPE_HEADERS_NAME = "headers";
 	public static final String TABULARTYPE_HEADERS_DESCRIPTION = "bundle headers";
 	public static final String COMPOSITETYPE_HEADERS_ENTRY = "headerEntry";
 	public static final String COMPOSITETYPE_HEADERS_ENTRY_DESCRIPTION = "bundle header entry";
 	public static final String COMPOSITETYPE_HEADERS_NAME = "name";
 	public static final String COMPOSITETYPE_HEADERS_VALUE = "value";
 	public static final String ATTRIBUTE_HEADERS_NAME = "headers";
 	public static final String ATTRIBUTE_HEADERS_DESCRIPTION = "The bundle headers";
 
 	/**
 	 * attribute: registeredServices
 	 */
 	public static final String ATTRIBUTE_REGISTEREDSERVICES_NAME = "registeredServices";
 	public static final String ATTRIBUTE_REGISTEREDSERVICES_DESCRIPTION = "The services which were registered by this bundle";
 
 	/**
 	 * operation: start
 	 */
 	public static final String OPERATION_START_NAME = "start";
 	public static final String OPERATION_START_DESCRIPTION = "Start the bundle";
 
 	/**
 	 * operation: stop
 	 */
 	public static final String OPERATION_STOP_NAME = "stop";
 	public static final String OPERATION_STOP_DESCRIPTION = "Stop the bundle";
 
 	/**
 	 * operation: update
 	 */
 	public static final String OPERATION_UPDATE_NAME = "update";
 	public static final String OPERATION_UPDATE_DESCRIPTION = "Update the bundle";
 
 	/**
 	 * operation: update from url
 	 */
 	public static final String OPERATION_UPDATEFROMURL_NAME = "update";
 	public static final String OPERATION_UPDATEFROMURL_DESCRIPTION = "Update the bundle from the given url";
	public static final OpenMBeanParameterInfo[] OPERATION_UPDATEFROMURL_PARAMETERS = new OpenMBeanParameterInfoSupport[] { new OpenMBeanParameterInfoSupport(
			"url", "The url to update the bundle from", SimpleType.STRING) };
 
 	/**
 	 * operation: uninstall
 	 */
 	public static final String OPERATION_UNINSTALL_NAME = "uninstall";
 	public static final String OPERATION_UNINSTALL_DESCRIPTION = "Uninstall the bundle";
 }
