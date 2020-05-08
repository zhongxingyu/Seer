 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.test.kernel.deployment.test;
 
 import junit.framework.Test;
 import org.jboss.test.kernel.deployment.support.container.Bean1Type;
 import org.jboss.test.kernel.deployment.support.container.Bean2Type;
 import org.jboss.test.kernel.deployment.support.container.BeanContainer;
 
 /**
 * Bean Container Test Case.
  * 
 * @author <a href="scott.stark@jboss.com">Scott Stark</a>
  */
 public class BeanContainerUsageTestCase extends AbstractDeploymentTest
 {
    public static Test suite()
    {
       return suite(BeanContainerUsageTestCase.class);
    }
 
    public BeanContainerUsageTestCase(String name) throws Throwable
    {
       super(name);
    }
 
   @SuppressWarnings("unchecked")
    public void testDependencyInjectionOfBean() throws Throwable
    {
       BeanContainer<Bean1Type> container1 = (BeanContainer<Bean1Type>) getBean("BeanContainer1Type");
       BeanContainer<Bean2Type> container2 = (BeanContainer<Bean2Type>) getBean("BeanContainer2Type");
       Bean2Type bean21 = container2.getBean();
       Bean1Type bean11 = bean21.getBean1();
       assertNotNull(bean11);
       // Create another Bean2Type instance
       Bean2Type bean22 = container2.getBean();
       assertTrue(bean22 != bean21);
       // The injected bean should not be the same as injected into bean21
       Bean1Type bean12 = bean22.getBean1();
       assertNotNull(bean12);
       assertTrue(bean12 != bean11);
    }
 
 }
