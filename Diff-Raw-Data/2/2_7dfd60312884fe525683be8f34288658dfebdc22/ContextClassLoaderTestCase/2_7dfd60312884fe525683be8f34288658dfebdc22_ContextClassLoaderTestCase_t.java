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
 package org.jboss.test.kernel.controller.test;
 
 
 import junit.framework.Test;
 
 import org.jboss.kernel.spi.deployment.KernelDeployment;
 import org.jboss.test.kernel.controller.support.TestClassLoaderBean;
 
 /**
  * ContextClassLoaderTestCase.
  * 
  * TODO test others, e.g. ControllerContextAware
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class ContextClassLoaderTestCase extends AbstractControllerTest
 {
    ClassLoader cl = null;
    
    public static Test suite()
    {
       return suite(ContextClassLoaderTestCase.class);
    }
 
    public ContextClassLoaderTestCase(String name) throws Throwable
    {
       super(name);
    }
 
    protected void setUp() throws Exception
    {
       super.setUp();
       SecurityManager sm = suspendSecurity();
       try
       {
          cl = Thread.currentThread().getContextClassLoader();
       }
       finally
       {
          resumeSecurity(sm);
       }
    }
 
    protected void tearDown() throws Exception
    {
       SecurityManager sm = suspendSecurity();
       try
       {
          assertEquals(cl, Thread.currentThread().getContextClassLoader());
       }
       finally
       {
          resumeSecurity(sm);
          super.tearDown();
       }
    }
 
    public ClassLoader assertClassLoader() throws Exception
    {
       ClassLoader classLoader = (ClassLoader) getBean("ClassLoader");
       checkClassLoader(classLoader);
       return classLoader;
    }
 
    public ClassLoader assertClassLoader(ClassLoader cl) throws Exception
    {
       ClassLoader classLoader = (ClassLoader) getBean("ClassLoader");
       checkClassLoader(cl);
       return classLoader;
    }
 
    public void checkClassLoader(ClassLoader classLoader) throws Exception
    {
       assertEquals(classLoader, TestClassLoaderBean.getAndResetClassLoader());
    }
    
    public void testFactory() throws Throwable
    {
       KernelDeployment deployment = deploy("ContextClassLoaderTestCase_Factory.xml");
       try
       {
          validate();
          assertClassLoader();
       }
       finally
       {
          undeploy(deployment);
       }
    }
    
    public void testConstructor() throws Throwable
    {
       KernelDeployment deployment = deploy("ContextClassLoaderTestCase_Constructor.xml");
       try
       {
          validate();
          assertClassLoader();
       }
       finally
       {
          undeploy(deployment);
       }
    }
    
    public void testConfigure() throws Throwable
    {
       KernelDeployment deployment = deploy("ContextClassLoaderTestCase_Configure.xml");
       ClassLoader cl = null;
       try
       {
          validate();
          cl = assertClassLoader();
       }
       finally
       {
          undeploy(deployment);
       }
       checkClassLoader(cl);
    }
    
    public void testCreate() throws Throwable
    {
       KernelDeployment deployment = deploy("ContextClassLoaderTestCase_Create.xml");
       try
       {
          assertClassLoader();
       }
       finally
       {
          undeploy(deployment);
       }
    }
    
    public void testStart() throws Throwable
    {
      KernelDeployment deployment = deploy("ContextClassLoaderTestCase_Start.xml");
       try
       {
          validate();
          assertClassLoader();
       }
       finally
       {
          undeploy(deployment);
       }
    }
    
    public void testStop() throws Throwable
    {
       KernelDeployment deployment = deploy("ContextClassLoaderTestCase_Stop.xml");
       ClassLoader cl = null;
       try
       {
          cl = assertClassLoader(null);
       }
       finally
       {
          undeploy(deployment);
       }
       checkClassLoader(cl);
    }
    
    public void testDestroy() throws Throwable
    {
       KernelDeployment deployment = deploy("ContextClassLoaderTestCase_Destroy.xml");
       ClassLoader cl = null;
       try
       {
          validate();
          cl = assertClassLoader(null);
       }
       finally
       {
          undeploy(deployment);
       }
       checkClassLoader(cl);
    }
    
    public void testInstall() throws Throwable
    {
       KernelDeployment deployment = deploy("ContextClassLoaderTestCase_Install.xml");
       try
       {
          validate();
          assertClassLoader();
       }
       finally
       {
          undeploy(deployment);
       }
    }
    
    public void testUninstall() throws Throwable
    {
       KernelDeployment deployment = deploy("ContextClassLoaderTestCase_Uninstall.xml");
       ClassLoader cl = null;
       try
       {
          validate();
          cl = assertClassLoader(null);
       }
       finally
       {
          undeploy(deployment);
       }
       checkClassLoader(cl);
    }
    
    public void testIncallbackSingle() throws Throwable
    {
       KernelDeployment repository = deploy("ContextClassLoaderTestCase_InCallBackSingle.xml");
       KernelDeployment deployment = deploy("ContextClassLoaderTestCase_Callback.xml");
       try
       {
          validate();
          assertClassLoader();
       }
       finally
       {
          undeploy(deployment);
          undeploy(repository);
       }
    }
    
    public void testIncallbackCollection() throws Throwable
    {
       KernelDeployment repository = deploy("ContextClassLoaderTestCase_InCallBackCollection.xml");
       KernelDeployment deployment = deploy("ContextClassLoaderTestCase_Callback.xml");
       try
       {
          validate();
          assertClassLoader();
       }
       finally
       {
          undeploy(deployment);
          undeploy(repository);
       }
    }
    
    public void testUncallbackSingle() throws Throwable
    {
       KernelDeployment repository = deploy("ContextClassLoaderTestCase_UnCallBackSingle.xml");
       ClassLoader cl = null;
       try
       {
          KernelDeployment deployment = deploy("ContextClassLoaderTestCase_Callback.xml");
          try
          {
             validate();
             cl = assertClassLoader(null);
          }
          finally
          {
             undeploy(deployment);
          }
          checkClassLoader(cl);
       }
       finally
       {
          undeploy(repository);
       }
    }
    
    public void testUncallbackCollection() throws Throwable
    {
       KernelDeployment repository = deploy("ContextClassLoaderTestCase_UnCallBackCollection.xml");
       ClassLoader cl = null;
       try
       {
          KernelDeployment deployment = deploy("ContextClassLoaderTestCase_Callback.xml");
          try
          {
             validate();
             cl = assertClassLoader(null);
          }
          finally
          {
             undeploy(deployment);
          }
          checkClassLoader(cl);
       }
       finally
       {
          undeploy(repository);
       }
    }
 }
