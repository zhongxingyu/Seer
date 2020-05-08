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
 package com.buschmais.maexo.test.framework.commons.mbean;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 
 import org.osgi.framework.ServiceRegistration;
 
 import com.buschmais.maexo.framework.commons.mbean.dynamic.DynamicMBeanSupport;
 import com.buschmais.maexo.test.Constants;
 import com.buschmais.maexo.test.MaexoTests;
 import com.buschmais.maexo.test.common.mbeans.DynamicMBean;
 import com.buschmais.maexo.test.common.mbeans.DynamicMBeanImpl;
 import com.buschmais.maexo.test.common.mbeans.MaexoMBeanTests;
import com.buschmais.maexo.test.common.mbeans.OpenMBean;
import com.buschmais.maexo.test.common.mbeans.OpenMBeanImpl;
 
 /**
  * @see MaexoTests
  */
 public class DynamicMBeanSupportTest extends MaexoMBeanTests {
 
 	/**
 	 * The object name to use for publishing MBeans.
 	 */
 	private static final String OBJECTNAME_DYNAMICMBEAN = "com.buschmais.maexo:type=DynamicMBean";
 
	private static final String OBJECTNAME_OPENMBEAN = "com.buschmais.maexo:type=OpenMBean";

	private OpenMBean openMBean;
	private ServiceRegistration openMBeanRegistration;
 	private DynamicMBean dynamicMBean;
 	private ServiceRegistration dynamicMBeanRegistration;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected String[] getTestBundlesNames() {
 		return new String[] { Constants.ARTIFACT_COMMONS_MBEAN,
 				Constants.ARTIFACT_SWITCHBOARD,
 				Constants.ARTIFACT_PLATFORM_MBEAN_SERVER };
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void onSetUp() throws Exception {
 		super.onSetUp();
		this.openMBean = new OpenMBeanImpl();
		Dictionary<String, Object> openMBeanProperties = new Hashtable<String, Object>();
		openMBeanProperties.put(ObjectName.class.getName(), new ObjectName(
				OBJECTNAME_OPENMBEAN));
		this.openMBeanRegistration = super.bundleContext.registerService(
				javax.management.DynamicMBean.class.getName(), this.openMBean,
				openMBeanProperties);
 		this.dynamicMBean = new DynamicMBeanImpl();
 		Dictionary<String, Object> dynamicMBeanProperties = new Hashtable<String, Object>();
 		dynamicMBeanProperties.put(ObjectName.class.getName(), new ObjectName(
 				OBJECTNAME_DYNAMICMBEAN));
 		this.dynamicMBeanRegistration = super.bundleContext.registerService(
 				javax.management.DynamicMBean.class.getName(),
 				this.dynamicMBean, dynamicMBeanProperties);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void onTearDown() throws Exception {
 		this.dynamicMBeanRegistration.unregister();
		this.openMBeanRegistration.unregister();
 	}
 
 	/**
 	 * Tests read and write operations on MBean attributes
 	 *
 	 * @throws MalformedObjectNameException
 	 * @throws NullPointerException
 	 */
 	public void test_DynamicMBeanAttributes()
 			throws MalformedObjectNameException, NullPointerException {
 		DynamicMBean importedMBean = (DynamicMBean) super.getMBean(
 				new ObjectName(OBJECTNAME_DYNAMICMBEAN), DynamicMBean.class);
 		importedMBean.setIntAttribute(111);
 		assertEquals(111, this.dynamicMBean.getIntAttribute());
 		assertEquals(111, importedMBean.getIntAttribute());
 		importedMBean.setIntegerAttribute(Integer.valueOf(222));
 		assertEquals(Integer.valueOf(222), this.dynamicMBean
 				.getIntegerAttribute());
 		assertEquals(Integer.valueOf(222), importedMBean.getIntegerAttribute());
 		importedMBean.setStringAttribute("stringAttribute");
 		assertEquals("stringAttribute", this.dynamicMBean.getStringAttribute());
 		assertEquals("stringAttribute", importedMBean.getStringAttribute());
 	}
 
 	/**
 	 * Tests invocation of MBean operations
 	 *
 	 * @throws MalformedObjectNameException
 	 * @throws NullPointerException
 	 */
 	public void test_DynamicMBeanOperation()
 			throws MalformedObjectNameException, NullPointerException {
 		DynamicMBean importedMBean = (DynamicMBean) super.getMBean(
 				new ObjectName(OBJECTNAME_DYNAMICMBEAN), DynamicMBean.class);
 		importedMBean.operation("stringAttribute", 111, Integer.valueOf(222));
 		assertEquals(111, this.dynamicMBean.getIntAttribute());
 		assertEquals(Integer.valueOf(222), this.dynamicMBean
 				.getIntegerAttribute());
 		assertEquals("stringAttribute", this.dynamicMBean.getStringAttribute());
 	}
 }
