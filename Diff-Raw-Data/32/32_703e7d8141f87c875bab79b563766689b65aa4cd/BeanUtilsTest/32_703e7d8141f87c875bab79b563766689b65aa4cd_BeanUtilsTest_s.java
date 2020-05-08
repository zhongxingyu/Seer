 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.hobsoft.symmetry.support.bean;
 
 import java.beans.BeanDescriptor;
 
 import org.hobsoft.symmetry.support.bean.model.Bean;
 import org.hobsoft.symmetry.support.bean.model.BoundBean;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Tests {@code BeanUtils}.
  * 
  * @author Mark Hobson
  * @see BeanUtils
  */
 public class BeanUtilsTest
 {
 	// equals(BeanDescriptor, BeanDescriptor) tests ---------------------------
 	
 	@Test
 	public void equalsBeanDescriptorWhenEqual()
 	{
 		BeanDescriptor descriptor1 = new BeanDescriptor(Bean.class);
 		BeanDescriptor descriptor2 = new BeanDescriptor(Bean.class);
 		
 		assertTrue(BeanUtils.equals(descriptor1, descriptor2));
 	}
 	
 	@Test
 	public void equalsBeanDescriptorWhenBeanClassDifferent()
 	{
 		BeanDescriptor descriptor1 = new BeanDescriptor(Bean.class);
 		BeanDescriptor descriptor2 = new BeanDescriptor(BoundBean.class);
 		
 		assertFalse(BeanUtils.equals(descriptor1, descriptor2));
 	}
 
 	// TODO: test all other BeanDescriptor properties
 	
 	// toString(BeanDescriptor) tests -----------------------------------------
 	
 	@Test
 	public void toStringBeanDescriptor()
 	{
 		BeanDescriptor descriptor = new BeanDescriptor(Bean.class);
 		
		assertEquals("java.beans.BeanDescriptor[beanClass=org.hobsoft.symmetry.support.bean.model.Bean,customizerClass=null]",
			BeanUtils.toString(descriptor));
 	}
 	
 	@Test
 	public void toStringBeanDescriptorWithNull()
 	{
 		assertEquals("null", BeanUtils.toString((BeanDescriptor) null));
 	}
 }
