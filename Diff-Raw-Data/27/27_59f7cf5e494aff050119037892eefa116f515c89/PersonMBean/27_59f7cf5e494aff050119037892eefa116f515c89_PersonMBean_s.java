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
 
 import java.util.LinkedList;
 import java.util.List;
 
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
 import com.buschmais.maexo.framework.commons.mbean.dynamic.OpenTypeFactory;
 import com.buschmais.maexo.framework.commons.mbean.objectname.ObjectNameFactoryHelper;
 
 /**
  * Implementation of an MBean which will be used to manage an instance of the
  * class {@link Person}.
  */
 public class PersonMBean extends DynamicMBeanSupport {
 
 	/**
 	 * The person instance.
 	 */
 	private Person person;
 
 	/**
 	 * The object name factory helper which is used to construct object names of
 	 * referenced MBeans.
 	 */
 	private ObjectNameFactoryHelper objectNameFactoryHelper;
 
 	/**
 	 * Constructs the MBean.
 	 *
 	 * @param person
 	 *            The person.
 	 * @param objectNameFactoryHelper
 	 *            The object name factory helper.
 	 */
 	public PersonMBean(Person person,
 			ObjectNameFactoryHelper objectNameFactoryHelper) {
 		this.person = person;
 		this.objectNameFactoryHelper = objectNameFactoryHelper;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public final MBeanInfo getMBeanInfo() {
 		String className = this.getClass().getName();
 		OpenMBeanAttributeInfo firstNameInfo = new OpenMBeanAttributeInfoSupport(
 				"firstName", "The first name.", SimpleType.STRING, true, false,
 				false);
 		OpenMBeanAttributeInfo lastNameInfo = new OpenMBeanAttributeInfoSupport(
 				"lastName", "The last name.", SimpleType.STRING, true, false,
 				false);
 		OpenMBeanAttributeInfo addressesInfo = new OpenMBeanAttributeInfoSupport(
 				"addresses", "The addresses of this person.", OpenTypeFactory
 						.createArrayType(1, SimpleType.OBJECTNAME), true,
 				false, false);
 		return new OpenMBeanInfoSupport(className,
 				"An OpenMBean which represents a person.",
 				new OpenMBeanAttributeInfo[] { firstNameInfo, lastNameInfo,
 						addressesInfo }, new OpenMBeanConstructorInfo[] {},
 				new OpenMBeanOperationInfoSupport[] {},
 				new MBeanNotificationInfo[] {});
 	}
 
 	/**
 	 * Returns the addresses as object name representation.
 	 *
 	 * @return The object names.
 	 */
 	public final ObjectName[] getAddresses() {
 		List<ObjectName> addresses = new LinkedList<ObjectName>();
 		for (Address address : this.person.getAdresses()) {
 			addresses.add(this.objectNameFactoryHelper.getObjectName(address,
 					Address.class));
 		}
 		return addresses.toArray(new ObjectName[0]);
 	}

 }
