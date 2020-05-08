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
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.management.Attribute;
 import javax.management.AttributeList;
 import javax.management.AttributeNotFoundException;
 import javax.management.DynamicMBean;
 import javax.management.InvalidAttributeValueException;
 import javax.management.MBeanException;
 import javax.management.MBeanInfo;
 import javax.management.MBeanNotificationInfo;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 import javax.management.openmbean.ArrayType;
 import javax.management.openmbean.CompositeDataSupport;
 import javax.management.openmbean.CompositeType;
 import javax.management.openmbean.OpenDataException;
 import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
 import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
 import javax.management.openmbean.OpenMBeanInfoSupport;
 import javax.management.openmbean.OpenMBeanOperationInfoSupport;
 import javax.management.openmbean.OpenMBeanParameterInfoSupport;
 import javax.management.openmbean.OpenType;
 import javax.management.openmbean.SimpleType;
 import javax.management.openmbean.TabularData;
 import javax.management.openmbean.TabularDataSupport;
 import javax.management.openmbean.TabularType;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.ServiceReference;
 
 import com.buschmais.osgi.maexo.framework.commons.mbean.objectname.ObjectNameHelper;
 
 /**
  * Represents a bundle
  */
 public class BundleMBean implements DynamicMBean,
 		com.buschmais.osgi.maexo.mbeans.osgi.core.Bundle {
 
 	// translation map for bundle states
 	private static Map<Integer, String> bundleStates;
 
 	static {
 		bundleStates = new HashMap<Integer, String>();
 		bundleStates.put(Integer.valueOf(Bundle.ACTIVE), "ACTIVE");
 		bundleStates.put(Integer.valueOf(Bundle.INSTALLED), "INSTALLED");
 		bundleStates.put(Integer.valueOf(Bundle.RESOLVED), "RESOLVED");
 		bundleStates.put(Integer.valueOf(Bundle.STARTING), "STARTING");
 		bundleStates.put(Integer.valueOf(Bundle.STOPPING), "STOPPING");
 		bundleStates.put(Integer.valueOf(Bundle.UNINSTALLED), "UNINSTALLED");
 	}
 
 	/**
 	 * the bundle to manage
 	 */
 	private Bundle bundle;
 
 	private ObjectNameHelper objectNameHelper;
 
 	private CompositeType headersRowType;
 
 	private TabularType headersType;
 
 	/**
 	 * Constructs the managed bean
 	 * 
 	 * @param bundle
 	 *            the bundle to manage
 	 */
 	public BundleMBean(Bundle bundle, ObjectNameHelper objectNameHelper) {
 		this.bundle = bundle;
 		this.objectNameHelper = objectNameHelper;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
 	 */
 	public Object getAttribute(String attribute)
 			throws AttributeNotFoundException, MBeanException,
 			ReflectionException {
 		if (BundleConstants.ATTRIBUTE_ID_NAME.equals(attribute)) {
 			return this.getId();
 		} else if (BundleConstants.ATTRIBUTE_STATE_NAME.equals(attribute)) {
 			return this.getState();
 		} else if (BundleConstants.ATTRIBUTE_STATENAME_NAME.equals(attribute)) {
 			return this.getStateName();
 		} else if (BundleConstants.ATTRIBUTE_HEADERS_NAME.equals(attribute)) {
 			return this.getHeaders();
 		} else if (BundleConstants.ATTRIBUTE_REGISTEREDSERVICES_NAME
 				.equals(attribute)) {
 			return this.getRegisteredServices();
 		}
 		throw new AttributeNotFoundException("unknown attribute" + attribute);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
 	 */
 	@SuppressWarnings("unchecked")
 	public AttributeList getAttributes(String[] attributes) {
 		AttributeList attributeList = new AttributeList();
 		for (String attribute : attributes) {
 			try {
 				attributeList.add(this.getAttribute(attribute));
 			} catch (Exception e) {
 				// silently ignore
 			}
 		}
 		return attributeList;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.management.DynamicMBean#getMBeanInfo()
 	 */
 	public MBeanInfo getMBeanInfo() {
 		try {
 			String className = BundleMBean.class.getName();
 			// attributes
 			List<OpenMBeanAttributeInfoSupport> attributeList = new ArrayList<OpenMBeanAttributeInfoSupport>();
 			// id
 			attributeList.add(new OpenMBeanAttributeInfoSupport(
 					BundleConstants.ATTRIBUTE_ID_NAME,
 					BundleConstants.ATTRIBUTE_ID_DESCRIPTION,
 					SimpleType.INTEGER, true, false, false));
 			// state
 			attributeList.add(new OpenMBeanAttributeInfoSupport(
 					BundleConstants.ATTRIBUTE_STATE_NAME,
 					BundleConstants.ATTRIBUTE_STATE_DESCRIPTION,
 					SimpleType.INTEGER, true, false, false));
 			// state as name
 			attributeList.add(new OpenMBeanAttributeInfoSupport(
 					BundleConstants.ATTRIBUTE_STATENAME_NAME,
 					BundleConstants.ATTRIBUTE_STATENAME_DESCRIPTION,
 					SimpleType.STRING, true, false, false));
 			// header
 			this.headersRowType = new CompositeType(
 					BundleConstants.COMPOSITETYPE_HEADERS_ENTRY,
 					BundleConstants.COMPOSITETYPE_HEADERS_ENTRY_DESCRIPTION,
 					new String[] { BundleConstants.COMPOSITETYPE_HEADERS_NAME,
 							BundleConstants.COMPOSITETYPE_HEADERS_VALUE },
 					new String[] { BundleConstants.COMPOSITETYPE_HEADERS_NAME,
 							BundleConstants.COMPOSITETYPE_HEADERS_VALUE },
 					new OpenType[] { SimpleType.STRING, SimpleType.STRING });
 			this.headersType = new TabularType(
 					BundleConstants.TABULARTYPE_HEADERS_NAME,
 					BundleConstants.TABULARTYPE_HEADERS_DESCRIPTION,
 					this.headersRowType,
 					new String[] { BundleConstants.COMPOSITETYPE_HEADERS_NAME });
 			attributeList.add(new OpenMBeanAttributeInfoSupport(
 					BundleConstants.ATTRIBUTE_HEADERS_NAME,
 					BundleConstants.ATTRIBUTE_HEADERS_DESCRIPTION,
 					this.headersType, true, false, false));
 			// registered services
 			attributeList
 					.add(new OpenMBeanAttributeInfoSupport(
 							BundleConstants.ATTRIBUTE_REGISTEREDSERVICES_NAME,
 							BundleConstants.ATTRIBUTE_REGISTEREDSERVICES_DESCRIPTION,
 							new ArrayType(1, SimpleType.OBJECTNAME), true,
 							false, false));
 			OpenMBeanAttributeInfoSupport[] mbeanAttributeInfos = attributeList
 					.toArray(new OpenMBeanAttributeInfoSupport[attributeList
 							.size()]);
 
 			// operations
 			List<OpenMBeanOperationInfoSupport> operationList = new ArrayList<OpenMBeanOperationInfoSupport>();
 			// start
 			operationList.add(new OpenMBeanOperationInfoSupport(
 					BundleConstants.OPERATION_START_NAME,
 					BundleConstants.OPERATION_START_NAME,
 					new OpenMBeanParameterInfoSupport[] {}, SimpleType.VOID,
 					OpenMBeanOperationInfoSupport.ACTION_INFO));
 			// stop
 			operationList.add(new OpenMBeanOperationInfoSupport(
 					BundleConstants.OPERATION_STOP_NAME,
 					BundleConstants.OPERATION_STOP_NAME,
 					new OpenMBeanParameterInfoSupport[] {}, SimpleType.VOID,
 					OpenMBeanOperationInfoSupport.ACTION_INFO));
 			// update
 			operationList.add(new OpenMBeanOperationInfoSupport(
 					BundleConstants.OPERATION_UPDATE_NAME,
 					BundleConstants.OPERATION_UPDATE_NAME,
 					new OpenMBeanParameterInfoSupport[] {}, SimpleType.VOID,
 					OpenMBeanOperationInfoSupport.ACTION_INFO));
 			// update from url
 			operationList
 					.add(new OpenMBeanOperationInfoSupport(
 							BundleConstants.OPERATION_UPDATEFROMURL_NAME,
 							BundleConstants.OPERATION_UPDATEFROMURL_NAME,
 							BundleConstants.OPERATION_UPDATEFROMURL_PARAMETERS,
 							SimpleType.VOID,
 							OpenMBeanOperationInfoSupport.ACTION_INFO));
 			OpenMBeanOperationInfoSupport[] mbeanOperationInfos = operationList
 					.toArray(new OpenMBeanOperationInfoSupport[0]);
 			// constructors
 			OpenMBeanConstructorInfoSupport[] mbeanConstructorInfos = new OpenMBeanConstructorInfoSupport[] {};
 			// notifications
 			MBeanNotificationInfo[] mbeanNotificationInfos = new MBeanNotificationInfo[] {};
 			// mbean info
 			OpenMBeanInfoSupport mbeanInfo = new OpenMBeanInfoSupport(
 					className, BundleConstants.MBEAN_DESCRIPTION,
 					mbeanAttributeInfos, mbeanConstructorInfos,
 					mbeanOperationInfos, mbeanNotificationInfos);
 			return mbeanInfo;
 		} catch (OpenDataException e) {
 			throw new RuntimeException("cannot construct mbean info", e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.management.DynamicMBean#invoke(java.lang.String,
 	 * java.lang.Object[], java.lang.String[])
 	 */
 	public Object invoke(String actionName, Object[] params, String[] signature)
 			throws MBeanException, ReflectionException {
 		if (BundleConstants.OPERATION_START_NAME.equals(actionName)) {
 			this.start();
 			return null;
 		} else if (BundleConstants.OPERATION_STOP_NAME.equals(actionName)) {
 			this.stop();
 			return null;
 		} else if (BundleConstants.OPERATION_UPDATE_NAME.equals(actionName)) {
 			if (signature.length == 0) {
 				this.update();
 			} else if (signature.length == 1
 					&& BundleConstants.OPERATION_UPDATEFROMURL_PARAMETERS[0]
 							.getName().equals(signature[0])) {
 				this.update((String) params[0]);
 			}
 			return null;
 		} else if (BundleConstants.OPERATION_UNINSTALL_NAME.equals(actionName)) {
 			this.uninstall();
 			return null;
 		}
 		throw new ReflectionException(new NoSuchMethodException(actionName));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
 	 */
 	public void setAttribute(Attribute attribute)
 			throws AttributeNotFoundException, InvalidAttributeValueException,
 			MBeanException, ReflectionException {
 		throw new ReflectionException(new IllegalAccessException(attribute
 				.getName()));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * javax.management.DynamicMBean#setAttributes(javax.management.AttributeList
 	 * )
 	 */
 	public AttributeList setAttributes(AttributeList attributes) {
 		return new AttributeList();
 	}
 
 	public Long getId() {
		return Long.valueOf(this.bundle.getBundleId());
 	}
 
 	public Integer getState() {
 		return Integer.valueOf(this.bundle.getState());
 	}
 
 	public String getStateName() {
 		return bundleStates.get(Integer.valueOf(this.bundle.getState()));
 	}
 
 	/**
 	 * Returns an array of object names representing the services which were
 	 * registered by this bundle
 	 * 
 	 * @return the array of object names
 	 */
 	public ObjectName[] getRegisteredServices() {
 		List<ObjectName> registeredServicesList = new LinkedList<ObjectName>();
 		ServiceReference[] registeredServices = this.bundle
 				.getRegisteredServices();
 		if (registeredServices != null) {
 			for (ServiceReference registeredService : registeredServices) {
 				registeredServicesList.add(this.objectNameHelper.getObjectName(
 						registeredService, ServiceReference.class));
 			}
 		}
 		return registeredServicesList.toArray(new ObjectName[0]);
 	}
 
 	/**
 	 * Returns the bundle headers as tabular data
 	 * 
 	 * @return the bundle headers
 	 * @throws MBeanException
 	 * @throws OpenDataException
 	 */
 	@SuppressWarnings("unchecked")
 	public TabularData getHeaders() throws MBeanException {
 		TabularDataSupport tabularHeaders = new TabularDataSupport(
 				this.headersType);
 		Dictionary<String, String> headers = this.bundle.getHeaders();
 		Enumeration<String> keys = headers.keys();
 		while (keys.hasMoreElements()) {
 			String key = keys.nextElement();
 			String value = headers.get(key);
 			try {
 				CompositeDataSupport row = new CompositeDataSupport(
 						this.headersRowType, new String[] {
 								BundleConstants.COMPOSITETYPE_HEADERS_NAME,
 								BundleConstants.COMPOSITETYPE_HEADERS_VALUE },
 						new Object[] { key, value });
 				tabularHeaders.put(row);
 			} catch (Exception e) {
 				throw new MBeanException(e);
 			}
 
 		}
 		return tabularHeaders;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.buschmais.osgi.maexo.mbeans.osgi.core.Bundle#start()
 	 */
 	public void start() throws MBeanException {
 		try {
 			this.bundle.start();
 		} catch (BundleException e) {
 			throw new MBeanException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.buschmais.osgi.maexo.mbeans.osgi.core.Bundle#stop()
 	 */
 	public void stop() throws MBeanException {
 		try {
 			this.bundle.stop();
 		} catch (BundleException e) {
 			throw new MBeanException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.buschmais.osgi.maexo.mbeans.osgi.core.Bundle#uninstall()
 	 */
 	public void uninstall() throws MBeanException {
 		try {
 			this.bundle.uninstall();
 		} catch (BundleException e) {
 			throw new MBeanException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.buschmais.osgi.maexo.mbeans.osgi.core.Bundle#update()
 	 */
 	public void update() throws MBeanException {
 		try {
 			this.bundle.update();
 		} catch (BundleException e) {
 			throw new MBeanException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.buschmais.osgi.maexo.mbeans.osgi.core.Bundle#update(java.lang.String)
 	 */
 	public void update(String url) throws MBeanException {
 		try {
 			URL updateUrl = new URL(url);
 			this.bundle.update(updateUrl.openStream());
 		} catch (Exception e) {
 			throw new MBeanException(e);
 		}
 	}
 
 }
