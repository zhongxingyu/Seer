 /*
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
 import java.util.Date;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.management.DynamicMBean;
 import javax.management.MBeanException;
 import javax.management.MBeanInfo;
 import javax.management.MBeanNotificationInfo;
 import javax.management.ObjectName;
 import javax.management.openmbean.CompositeDataSupport;
 import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
 import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
 import javax.management.openmbean.OpenMBeanInfoSupport;
 import javax.management.openmbean.OpenMBeanOperationInfoSupport;
 import javax.management.openmbean.TabularData;
 import javax.management.openmbean.TabularDataSupport;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.ServiceReference;
 
 import com.buschmais.osgi.maexo.framework.commons.mbean.dynamic.DynamicMBeanSupport;
 import com.buschmais.osgi.maexo.framework.commons.mbean.objectname.ObjectNameHelper;
 
 /**
  * Represents am OSGi bundle.
  */
 public class Bundle extends DynamicMBeanSupport implements DynamicMBean,
 		BundleMBean {
 
 	// translation map for bundle states
 	private static Map<Integer, String> bundleStates;
 
 	static {
 		bundleStates = new HashMap<Integer, String>();
 		bundleStates.put(Integer.valueOf(org.osgi.framework.Bundle.ACTIVE),
 				"ACTIVE");
 		bundleStates.put(Integer.valueOf(org.osgi.framework.Bundle.INSTALLED),
 				"INSTALLED");
 		bundleStates.put(Integer.valueOf(org.osgi.framework.Bundle.RESOLVED),
 				"RESOLVED");
 		bundleStates.put(Integer.valueOf(org.osgi.framework.Bundle.STARTING),
 				"STARTING");
 		bundleStates.put(Integer.valueOf(org.osgi.framework.Bundle.STOPPING),
 				"STOPPING");
 		bundleStates.put(
 				Integer.valueOf(org.osgi.framework.Bundle.UNINSTALLED),
 				"UNINSTALLED");
 	}
 
 	/**
 	 * The bundle to manage.
 	 */
 	private org.osgi.framework.Bundle bundle;
 
 	private ObjectNameHelper objectNameHelper;
 
 	/**
 	 * Constructs the managed bean.
 	 * 
 	 * @param bundle
 	 *            the bundle to manage
 	 */
 	public Bundle(BundleContext bundleContext, org.osgi.framework.Bundle bundle) {
 		this.bundle = bundle;
 		this.objectNameHelper = new ObjectNameHelper(bundleContext);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.management.DynamicMBean#getMBeanInfo()
 	 */
 	public MBeanInfo getMBeanInfo() {
 		String className = Bundle.class.getName();
 		// attributes
 		OpenMBeanAttributeInfoSupport[] mbeanAttributeInfos = new OpenMBeanAttributeInfoSupport[] {
 				BundleConstants.ID, BundleConstants.STATE,
 				BundleConstants.STATENAME, BundleConstants.HEADER,
 				BundleConstants.LASTMODIFIED,
 				BundleConstants.LASTMODIFIEDASDATE, BundleConstants.LOCATION,
 				BundleConstants.REGISTEREDSERVICES,
 				BundleConstants.SERVICESINUSE };
 
 		// operations
 		OpenMBeanOperationInfoSupport[] mbeanOperationInfos = new OpenMBeanOperationInfoSupport[] {
 				BundleConstants.START, BundleConstants.STOP,
 				BundleConstants.UPDATE, BundleConstants.UPDATEFROMURL };
 		// constructors
 		OpenMBeanConstructorInfoSupport[] mbeanConstructorInfos = new OpenMBeanConstructorInfoSupport[] {};
 		// notifications
 		MBeanNotificationInfo[] mbeanNotificationInfos = new MBeanNotificationInfo[] {};
 		// mbean info
 		OpenMBeanInfoSupport mbeanInfo = new OpenMBeanInfoSupport(className,
 				BundleConstants.MBEAN_DESCRIPTION, mbeanAttributeInfos,
 				mbeanConstructorInfos, mbeanOperationInfos,
 				mbeanNotificationInfos);
 		return mbeanInfo;
 	}
 
 	public Long getBundleId() {
 		return Long.valueOf(this.bundle.getBundleId());
 	}
 
 	public Integer getState() {
 		return Integer.valueOf(this.bundle.getState());
 	}
 
 	public String getStateAsName() {
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
 	 */
 	@SuppressWarnings("unchecked")
 	public TabularData getHeaders() throws MBeanException {
 		TabularDataSupport tabularHeaders = new TabularDataSupport(
				BundleConstants.HEADERS_TYPE);
 		Dictionary<String, String> headers = this.bundle.getHeaders();
 		Enumeration<String> keys = headers.keys();
 		while (keys.hasMoreElements()) {
 			String key = keys.nextElement();
 			String value = headers.get(key);
 			try {
 				CompositeDataSupport header = new CompositeDataSupport(
						BundleConstants.HEADER_TYPE,
						BundleConstants.ITEM_NAMES, new Object[] { key, value });
 				tabularHeaders.put(header);
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
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.buschmais.osgi.maexo.mbeans.osgi.core.BundleMBean#getLastModified()
 	 */
 	public Long getLastModified() {
 		return Long.valueOf(this.bundle.getLastModified());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.buschmais.osgi.maexo.mbeans.osgi.core.BundleMBean#getLastModifiedAsDate
 	 * ()
 	 */
 	public Date getLastModifiedAsDate() {
 		return new Date(this.bundle.getLastModified());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.buschmais.osgi.maexo.mbeans.osgi.core.BundleMBean#getLocation()
 	 */
 	public String getLocation() {
 		return this.bundle.getLocation();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.buschmais.osgi.maexo.mbeans.osgi.core.BundleMBean#getServicesInUse()
 	 */
 	public ObjectName[] getServicesInUse() {
 		ServiceReference[] servicesInUse = this.bundle.getServicesInUse();
 		if (servicesInUse == null) {
 			return null;
 		}
 		ObjectName[] objectNames = new ObjectName[servicesInUse.length];
 		for (int i = 0; i < servicesInUse.length; i++) {
 			objectNames[i] = this.objectNameHelper.getObjectName(
 					servicesInUse[i], ServiceReference.class);
 		}
 		return objectNames;
 	}
 }
