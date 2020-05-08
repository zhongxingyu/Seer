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
 package com.buschmais.maexo.samples.commons.mbean.objectname;
 
 import javax.management.MBeanInfo;
 import javax.management.MBeanNotificationInfo;
 import javax.management.ObjectName;
 import javax.management.openmbean.OpenMBeanAttributeInfo;
 import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
 import javax.management.openmbean.OpenMBeanConstructorInfo;
 import javax.management.openmbean.OpenMBeanInfoSupport;
 import javax.management.openmbean.OpenMBeanOperationInfoSupport;
 import javax.management.openmbean.SimpleType;
 
 import com.buschmais.maexo.framework.commons.mbean.dynamic.DynamicMBeanSupport;
 import com.buschmais.maexo.framework.commons.mbean.objectname.ObjectNameFactoryHelper;
 
 /**
  * Implementation of an MBean which will be used to manage an instance of the
  * class {@link Address}.
  */
 public class AddressMBean extends DynamicMBeanSupport {
 
 	/**
 	 * The address instance.
 	 */
 	private Address address;
 
 	/**
 	 * The object name factory helper which is used to construct object names of
 	 * referenced MBeans.
 	 */
 	private ObjectNameFactoryHelper objectNameFactoryHelper;
 
 	/**
 	 * Constructs the MBean.
 	 *
 	 * @param address
 	 *            The address.
 	 * @param objectNameFactoryHelper
 	 *            The object name factory helper.
 	 */
 	public AddressMBean(Address address,
 			ObjectNameFactoryHelper objectNameFactoryHelper) {
 		this.address = address;
 		this.objectNameFactoryHelper = objectNameFactoryHelper;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public final MBeanInfo getMBeanInfo() {
 		String className = this.getClass().getName();
 		OpenMBeanAttributeInfo idInfo = new OpenMBeanAttributeInfoSupport(
				"person", "The id.", SimpleType.INTEGER, true, false, false);
 		OpenMBeanAttributeInfo personInfo = new OpenMBeanAttributeInfoSupport(
 				"person", "The person which owns this address.",
 				SimpleType.OBJECTNAME, true, false, false);
 		return new OpenMBeanInfoSupport(className,
 				"An OpenMBean which represents an address.",
 				new OpenMBeanAttributeInfo[] { idInfo, personInfo },
 				new OpenMBeanConstructorInfo[] {},
 				new OpenMBeanOperationInfoSupport[] {},
 				new MBeanNotificationInfo[] {});
 	}
 
 	/**
 	 * Returns the id of the address.
 	 *
 	 * @return The id of the address.
 	 */
 	public final Integer getId() {
 		return Integer.valueOf(this.address.getId());
 	}
 
 	/**
 	 * Returns the object name of the {@link PersonMBean} which represents the
 	 * person this address is assigned to.
 	 *
 	 * @return The object name of the {@link PersonMBean}.
 	 */
 	public final ObjectName getPerson() {
 		return this.objectNameFactoryHelper.getObjectName(this.address
 				.getPerson(), Person.class);
 	}
 }
