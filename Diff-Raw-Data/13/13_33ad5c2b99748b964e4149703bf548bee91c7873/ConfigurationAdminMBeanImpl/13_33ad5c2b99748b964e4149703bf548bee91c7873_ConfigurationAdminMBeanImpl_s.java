 /*
  * Copyright 2009 buschmais GbR
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
 package com.buschmais.maexo.mbeans.osgi.compendium.impl;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.management.MBeanInfo;
 import javax.management.MBeanNotificationInfo;
 import javax.management.ObjectName;
 import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
 import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
 import javax.management.openmbean.OpenMBeanInfoSupport;
 import javax.management.openmbean.OpenMBeanOperationInfoSupport;
 
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 
 import com.buschmais.maexo.framework.commons.mbean.dynamic.DynamicMBeanSupport;
 import com.buschmais.maexo.mbeans.osgi.compendium.ConfigurationAdminMBean;
 import com.buschmais.maexo.mbeans.osgi.compendium.ConfigurationAdminMBeanConstants;
 import com.buschmais.maexo.mbeans.osgi.compendium.impl.lifecycle.ConfigurationMBeanLifeCycle;
 
 /**
  * Implementation of a {@link ConfigurationAdminMBean}.
  */
 public class ConfigurationAdminMBeanImpl extends DynamicMBeanSupport implements
 		ConfigurationAdminMBean {
 
 	private ConfigurationAdmin configurationAdmin;
 
 	private ConfigurationMBeanLifeCycle configurationMBeanLifeCycle;
 
 	/**
 	 * Constructor.
	 *
 	 * @param configurationAdmin
 	 *            The {@link ConfigurationAdmin} service to manage.
 	 * @param configurationMBeanLifeCycle
 	 *            The instance of the {@link ConfigurationMBeanLifeCycle}.
 	 */
 	public ConfigurationAdminMBeanImpl(ConfigurationAdmin configurationAdmin,
 			ConfigurationMBeanLifeCycle configurationMBeanLifeCycle) {
 		this.configurationAdmin = configurationAdmin;
 		this.configurationMBeanLifeCycle = configurationMBeanLifeCycle;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public final MBeanInfo getMBeanInfo() {
 		String className = ConfigurationAdminMBeanImpl.class.getName();
 		// attributes
 		OpenMBeanAttributeInfoSupport[] mbeanAttributeInfos = new OpenMBeanAttributeInfoSupport[0];
 		// operations
 		OpenMBeanOperationInfoSupport[] mbeanOperationInfos = new OpenMBeanOperationInfoSupport[] {
 				ConfigurationAdminMBeanConstants.CREATEFACTORYCONFIGURATION_BY_FACTORYPID,
 				ConfigurationAdminMBeanConstants.CREATEFACTORYCONFIGURATION_BY_FACTORYPID_AND_LOCATION,
 				ConfigurationAdminMBeanConstants.GETCONFIGURATION_BY_PID,
 				ConfigurationAdminMBeanConstants.GETCONFIGURATION_BY_PID_AND_LOCATION,
 				ConfigurationAdminMBeanConstants.LISTCONFIGURATIONS };
 		// constructors
 		OpenMBeanConstructorInfoSupport[] mbeanConstructorInfos = new OpenMBeanConstructorInfoSupport[] {};
 		// notifications
 		MBeanNotificationInfo[] mbeanNotificationInfos = new MBeanNotificationInfo[] {};
 		// mbean info
 		OpenMBeanInfoSupport mbeanInfo = new OpenMBeanInfoSupport(className,
 				ConfigurationAdminMBeanConstants.MBEAN_DESCRIPTION,
 				mbeanAttributeInfos, mbeanConstructorInfos,
 				mbeanOperationInfos, mbeanNotificationInfos);
 		return mbeanInfo;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public final ObjectName createFactoryConfiguration(String factoryPid) {
 		try {
 			Configuration configuration = this.configurationAdmin
 					.createFactoryConfiguration(factoryPid);
 			return this.configurationMBeanLifeCycle.getConfigurationMBean(
 					configuration, configuration.getPid());
 		} catch (IOException e) {
 			throw new RuntimeException(e.toString());
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public final ObjectName createFactoryConfiguration(String factoryPid,
 			String location) {
 		try {
 			Configuration configuration = this.configurationAdmin
 					.createFactoryConfiguration(factoryPid, location);
 			return this.configurationMBeanLifeCycle.getConfigurationMBean(
 					configuration, configuration.getPid());
 		} catch (IOException e) {
 			throw new RuntimeException(e.toString());
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public final ObjectName getConfiguration(String pid) {
 		try {
 			Configuration configuration = this.configurationAdmin
 					.getConfiguration(pid);
 			return this.configurationMBeanLifeCycle.getConfigurationMBean(
 					configuration, configuration.getPid());
 		} catch (IOException e) {
 			throw new RuntimeException(e.toString());
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public final ObjectName getConfiguration(String pid, String location) {
 		try {
 			Configuration configuration = this.configurationAdmin
 					.getConfiguration(pid, location);
 			return this.configurationMBeanLifeCycle.getConfigurationMBean(
 					configuration, configuration.getPid());
 		} catch (IOException e) {
 			throw new RuntimeException(e.toString());
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public final ObjectName[] listConfigurations(String filter) {
 		Configuration[] configurations;
 		try {
 			configurations = this.configurationAdmin.listConfigurations(filter);
 		} catch (Exception e) {
 			throw new IllegalStateException(e.getMessage());
 		}
 		List<ObjectName> objectNames = new LinkedList<ObjectName>();
		for (Configuration configuration : configurations) {
			objectNames.add(this.configurationMBeanLifeCycle
					.getConfigurationMBean(configuration, configuration
							.getPid()));
 		}
 		return objectNames.toArray(new ObjectName[0]);
 	}
 
 }
