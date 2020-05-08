 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 package org.jboss.test.deployers.vfs.deployer.bean.test;
 
 import java.util.Map;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;

 import org.jboss.deployers.vfs.deployer.kernel.BeanDeployer;
 import org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataDeployer;
 import org.jboss.deployers.vfs.deployer.kernel.KernelDeploymentDeployer;
 import org.jboss.deployers.vfs.spi.client.VFSDeployment;
 import org.jboss.kernel.Kernel;
 import org.jboss.managed.api.ManagedObject;
 import org.jboss.managed.api.ManagedProperty;
 import org.jboss.managed.api.factory.ManagedObjectFactory;
 import org.jboss.metatype.api.values.CollectionValue;
 import org.jboss.metatype.api.values.GenericValue;
 
 /**
  * Tests of bean deployment ManagedObject/ManagedDeployment creation.
  * 
  * @author Scott.Stark@jboss.org
  * @version $Revision:$
  */
 public class BeanManagedDeploymentUnitTestCase extends AbstractDeployerUnitTestCase
 {
    public static Test suite()
    {
       return new TestSuite(BeanManagedDeploymentUnitTestCase.class);
    }
 
    public BeanManagedDeploymentUnitTestCase(String name) throws Throwable
    {
       super(name);
    }
 
    /**
     * Basic test of getting ManagedObject/ManagedProperty from a bean deployment.
     * TODO: the root ManagedObject for a KernelDeployment should be coming from a
     * custom {@linkplain org.jboss.managed.spi.factory.InstanceClassFactory}
     * @throws Exception
     */
    public void testBeanManagedObject()
       throws Exception
    {
       super.enableTrace("org.jboss.managed.plugins.factory");
       VFSDeployment context = createDeployment("/managed", "annotated-beans.xml");
       assertDeploy(context);
 
      ManagedObjectFactory.getInstance();
       Map<String, ManagedObject> mos = main.getManagedObjects(context.getName());
       log.info("annotated-beans.xml ManagedObjects: "+mos);
       assertEquals("annotated-beans.xml has 1 ManagedObject", 1, mos.size());
       ManagedObject kdMO = mos.values().iterator().next();
       log.info("KernelDeployment.ManagedProperties: "+kdMO.getProperties());
       ManagedProperty beanFactories = kdMO.getProperty("beanFactories");
       assertNotNull(beanFactories);
       CollectionValue beanFactoriesAV = CollectionValue.class.cast(beanFactories.getValue());
      beanFactoriesAV.getMetaType();
       assertEquals("BeanFactories size", 2, beanFactoriesAV.getSize());
       GenericValue beanFactoriesGV = (GenericValue) beanFactoriesAV.getElements()[0];
       log.info("BeanFactories[0].GV: "+beanFactoriesGV);
       ManagedObject beanFactoriesMO = (ManagedObject) beanFactoriesGV.getValue();
       assertNotNull(beanFactoriesMO);
       log.info("beanFactories.ManagedProperties: "+beanFactoriesMO.getProperties());
       ManagedProperty properties = beanFactoriesMO.getProperty("properties");
       assertNotNull(properties);
       CollectionValue propertiesArray = CollectionValue.class.cast(properties.getValue());
       assertNotNull(propertiesArray);
    }
 
    public void testBeanManagedDeployment()
       throws Exception
    {
       VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
       assertDeploy(context);
       assertNotNull(controller.getInstalledContext("Test"));
 
       assertUndeploy(context);
       assertNull(controller.getContext("Test", null));
    }
 
    protected void addDeployers(Kernel kernel)
    {
       BeanDeployer beanDeployer = new BeanDeployer();
       KernelDeploymentDeployer kernelDeploymentDeployer = new KernelDeploymentDeployer();
       BeanMetaDataDeployer beanMetaDataDeployer = new BeanMetaDataDeployer(kernel);
       addDeployer(main, beanDeployer);
       addDeployer(main, kernelDeploymentDeployer);
       addDeployer(main, beanMetaDataDeployer);
    }
 
 }
