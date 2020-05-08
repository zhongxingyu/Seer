 /*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors. 
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
 package org.jboss.test.microcontainer.beans.test;
 
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.test.aop.junit.AOPMicrocontainerTest;
 import org.jboss.test.microcontainer.beans.Dependency;
 import org.jboss.test.microcontainer.beans.POJO;
 import org.jboss.test.microcontainer.beans.TestAspectWithDependency;
 
 /**
  * 
  * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
  * @version $Revision: 1.1 $
  */
 public abstract class AspectWithDependencyTest extends AOPMicrocontainerTest
 {
 
    public AspectWithDependencyTest(String name)
    {
       super(name);
    }
 
    protected abstract String getFile0();
    
    protected abstract String getFile1();
    
    public void testInterceptorWithDependencyCorrectOrder() throws Exception
    {
       deploy(getFile0());
       try
       {
          Dependency dependency = (Dependency) getBean("Dependency");
          assertNotNull(dependency);
          deploy(getFile1());
          try
          {
             validate();
             POJO pojo = (POJO) getBean("Intercepted");
             assertNotNull(pojo);
             pojo.method(2);
             assertNotNull(TestAspectWithDependency.invoked);
             assertTrue(dependency == TestAspectWithDependency.invoked);
          }
          finally
          {
             undeploy(getFile1());
          }
       }
       finally
       {
          undeploy(getFile0());
       }
    }
 
    public void testInterceptorWithDependencyWrongOrder() throws Exception
    {
       deploy(getFile1());
       try
       {
          POJO pojo;
          try
          {
             pojo = (POJO) getBean("Intercepted");
             fail("'Interceped' should not be installed yet");
          }
          catch (IllegalStateException expected)
          {
          }
          
          pojo = (POJO) getBean("Intercepted", ControllerState.DESCRIBED);
          assertNull("This should not be deployed until the interceptor is", pojo);
          
          deploy(getFile0());
          try
          {
             validate();
             Dependency dependency = (Dependency) getBean("Dependency");
             assertNotNull(dependency);
             pojo = (POJO) getBean("Intercepted");
             assertNotNull(pojo);
             pojo.method(2);
             assertTrue(dependency == TestAspectWithDependency.invoked);
          }
          finally
          {
             undeploy(getFile0());
          }
       }
       finally
       {
          undeploy(getFile1());
       }
    }
 
    public void testInterceptorWithDependencyRedeploy() throws Exception
    {
       deploy(getFile1());
       try
       {
          POJO pojo;
          try
          {
             pojo = (POJO) getBean("Intercepted");
             fail("Bean should not be installed until the dependency is");
          }
          catch (IllegalStateException expected)
          {
             KernelControllerContext context = getControllerContext("Intercepted", ControllerState.DESCRIBED);
             assertNotNull(context);
          }
    
          deploy(getFile0());
          try
          {
             validate();
             Dependency dependency = (Dependency) getBean("Dependency");
             assertNotNull(dependency);
             pojo = (POJO) getBean("Intercepted");
             assertNotNull(pojo);
             pojo.method(2);
             assertTrue(dependency == TestAspectWithDependency.invoked);
          }
          finally
          {
             undeploy(getFile0());
          }
          
          try
          {
             pojo = (POJO) getBean("Intercepted");
             fail("Bean should not be installed after the dependency is undeployed");
          }
          catch (IllegalStateException expected)
          {
             KernelControllerContext context = getControllerContext("Intercepted", ControllerState.DESCRIBED);
             assertNotNull(context);
          }
    
          
          deploy(getFile0());
          try
          {
             validate();
             Dependency dependency = (Dependency) getBean("Dependency");
             assertNotNull(dependency);
             pojo = (POJO) getBean("Intercepted");
             assertNotNull(pojo);
             int called = TestAspectWithDependency.called;
             pojo.method(2);
            assertEquals("Interceptor was not rebound", called + 1, TestAspectWithDependency.called);
             assertTrue("Should not be caching the interceptor/dependency across rebinding", dependency == TestAspectWithDependency.invoked);
          }
          finally
          {
             undeploy(getFile0());
          }
       }
       finally
       {
          undeploy(getFile1());
       }
    }
 
    public void testInterceptorWithDependencyRedeploy2() throws Exception
    {
       deploy(getFile0());
       try
       {
          Dependency dependency = (Dependency) getBean("Dependency");
          assertNotNull(dependency);
          deploy(getFile1());
          try
          {
             validate();
             POJO pojo = (POJO) getBean("Intercepted");
             assertNotNull(pojo);
             pojo.method(2);
             assertTrue(dependency == TestAspectWithDependency.invoked);
          }
          finally
          {
             undeploy(getFile1());
          }
    
          dependency = (Dependency) getBean("Dependency");
          assertNotNull(dependency);
          deploy(getFile1());
          try
          {
             POJO pojo = (POJO) getBean("Intercepted");
             assertNotNull(pojo);
             pojo.method(2);
             assertTrue(dependency == TestAspectWithDependency.invoked);
          }
          finally
          {
             undeploy(getFile1());
          }
       }
       finally
       {
          undeploy(getFile0());
       }
    }
 
 }
