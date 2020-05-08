 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 package org.jboss.test.classloading.dependency.test;
 
 import junit.framework.Test;
 import org.jboss.classloader.spi.ClassLoaderSystem;
 import org.jboss.classloading.spi.dependency.policy.mock.MockClassLoadingMetaData;
 import org.jboss.classloading.spi.metadata.ClassLoadingMetaDataFactory;
 import org.jboss.classloading.spi.metadata.ExportAll;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.test.classloading.dependency.support.a.A;
 import org.jboss.test.classloading.dependency.support.b.B;
 import org.jboss.test.classloading.dependency.support.c.C;
 
 /**
  * HierarchicalDomainUnitTestCase.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @author <a href="ales.justin@jboss.org">Ales Justin</a>
  * @version $Revision: 1.1 $
  */
 public class HierarchicalDomainUnitTestCase extends AbstractMockClassLoaderUnitTest
 {
    public static Test suite()
    {
       return suite(HierarchicalDomainUnitTestCase.class);
    }
 
    public HierarchicalDomainUnitTestCase(String name)
    {
       super(name);
    }
    
    public void testParentFirst() throws Exception
    {
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       MockClassLoadingMetaData aParent = new MockClassLoadingMetaData("aParent");
       aParent.setPathsAndPackageNames(A.class);
       KernelControllerContext contextParentA = install(aParent);
       try
       {
          ClassLoader clParentA = assertClassLoader(contextParentA);
          assertLoadClass(A.class, clParentA);
          assertLoadClassFail(B.class, clParentA);
 
          MockClassLoadingMetaData aChild = new MockClassLoadingMetaData("aChild");
          aChild.setDomain("ChildDomain");
          aChild.setJ2seClassLoadingCompliance(true);
          aChild.setPathsAndPackageNames(A.class);
          KernelControllerContext contextChildA = install(aChild);
          try
          {
             ClassLoader clChildA = assertClassLoader(contextChildA);
             assertLoadClass(A.class, clParentA);
             assertLoadClassFail(B.class, clChildA);
 
             MockClassLoadingMetaData b = new MockClassLoadingMetaData("b");
             b.setDomain("ChildDomain");
             b.setJ2seClassLoadingCompliance(true);
             b.setPathsAndPackageNames(B.class);
             b.getRequirements().addRequirement(factory.createRequirePackage(A.class.getPackage().getName()));
             KernelControllerContext contextB = install(b);
             try
             {
                ClassLoader clB = assertClassLoader(contextB);
                assertLoadClass(A.class, clParentA);
                assertLoadClass(B.class, clB);
             }
             finally
             {
                uninstall(contextB);
             }
             assertNoClassLoader(contextB);
          }
          finally
          {
             uninstall(contextChildA);
          }
          assertNoClassLoader(contextChildA);
       }
       finally
       {
          uninstall(contextParentA);
       }
       assertNoClassLoader(contextParentA);
    }
    
    public void testParentLast() throws Exception
    {
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       MockClassLoadingMetaData aParent = new MockClassLoadingMetaData("aParent");
       aParent.setPathsAndPackageNames(A.class);
       KernelControllerContext contextParentA = install(aParent);
       try
       {
          ClassLoader clParentA = assertClassLoader(contextParentA);
          assertLoadClass(A.class, clParentA);
          assertLoadClassFail(B.class, clParentA);
 
          MockClassLoadingMetaData aChild = new MockClassLoadingMetaData("aChild");
          aChild.setDomain("ChildDomain");
          aChild.setJ2seClassLoadingCompliance(false);
          aChild.setPathsAndPackageNames(A.class);
          KernelControllerContext contextChildA = install(aChild);
          try
          {
             ClassLoader clChildA = assertClassLoader(contextChildA);
             assertLoadClass(A.class, clChildA);
             assertLoadClassFail(B.class, clChildA);
 
             MockClassLoadingMetaData b = new MockClassLoadingMetaData("b");
             b.setDomain("ChildDomain");
             b.setJ2seClassLoadingCompliance(false);
             b.setPathsAndPackageNames(B.class);
             b.getRequirements().addRequirement(factory.createRequirePackage(A.class.getPackage().getName()));
             KernelControllerContext contextB = install(b);
             try
             {
                ClassLoader clB = assertClassLoader(contextB);
                assertLoadClass(A.class, clChildA);
                assertLoadClass(B.class, clB);
             }
             finally
             {
                uninstall(contextB);
             }
             assertNoClassLoader(contextB);
          }
          finally
          {
             uninstall(contextChildA);
          }
          assertNoClassLoader(contextChildA);
       }
       finally
       {
          uninstall(contextParentA);
       }
       assertNoClassLoader(contextParentA);
    }
    
    public void testParentLastNotInChild() throws Exception
    {
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       MockClassLoadingMetaData aParent = new MockClassLoadingMetaData("aParent");
       aParent.setPathsAndPackageNames(A.class);
       KernelControllerContext contextParentA = install(aParent);
       try
       {
          ClassLoader clParentA = assertClassLoader(contextParentA);
          assertLoadClass(A.class, clParentA);
          assertLoadClassFail(B.class, clParentA);
 
          MockClassLoadingMetaData b = new MockClassLoadingMetaData("b");
          b.setDomain("ChildDomain");
          b.setJ2seClassLoadingCompliance(false);
          b.setPathsAndPackageNames(B.class);
          b.getRequirements().addRequirement(factory.createRequirePackage(A.class.getPackage().getName()));
          KernelControllerContext contextB = install(b);
          try
          {
             ClassLoader clB = assertClassLoader(contextB);
             assertLoadClass(A.class, clParentA);
             assertLoadClass(B.class, clB);
          }
          finally
          {
             uninstall(contextB);
          }
          assertNoClassLoader(contextB);
       }
       finally
       {
          uninstall(contextParentA);
       }
       assertNoClassLoader(contextParentA);
    }
    
    public void testParentFirstWrongWayAround() throws Exception
    {
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       MockClassLoadingMetaData b = new MockClassLoadingMetaData("b");
       b.setDomain("ChildDomain");
       b.setJ2seClassLoadingCompliance(true);
       b.setPathsAndPackageNames(B.class);
       b.getRequirements().addRequirement(factory.createRequirePackage(A.class.getPackage().getName()));
       KernelControllerContext contextB = install(b);
       try
       {
          assertNoClassLoader(contextB);
 
          MockClassLoadingMetaData aParent = new MockClassLoadingMetaData("aParent");
          aParent.setPathsAndPackageNames(A.class);
          KernelControllerContext contextParentA = install(aParent);
          try
          {
             ClassLoader clParentA = assertClassLoader(contextParentA);
             assertLoadClass(A.class, clParentA);
             assertLoadClassFail(B.class, clParentA);
 
             ClassLoader clB = assertClassLoader(contextB);
             assertLoadClass(A.class, clParentA);
             assertLoadClass(B.class, clB);
          }
          finally
          {
             uninstall(contextParentA);
          }
          assertNoClassLoader(contextParentA);
       }
       finally
       {
          uninstall(contextB);
       }
       assertNoClassLoader(contextB);
    }
    
    public void testParentLastWrongWayAround() throws Exception
    {
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       MockClassLoadingMetaData b = new MockClassLoadingMetaData("b");
       b.setDomain("ChildDomain");
       b.setJ2seClassLoadingCompliance(false);
       b.setPathsAndPackageNames(B.class);
       b.getRequirements().addRequirement(factory.createRequirePackage(A.class.getPackage().getName()));
       KernelControllerContext contextB = install(b);
       try
       {
          assertNoClassLoader(contextB);
 
          MockClassLoadingMetaData aParent = new MockClassLoadingMetaData("aParent");
          aParent.setPathsAndPackageNames(A.class);
          KernelControllerContext contextParentA = install(aParent);
          try
          {
             ClassLoader clParentA = assertClassLoader(contextParentA);
             assertLoadClass(A.class, clParentA);
             assertLoadClassFail(B.class, clParentA);
 
             ClassLoader clB = assertClassLoader(contextB);
             assertLoadClass(A.class, clParentA);
             assertLoadClass(B.class, clB);
          }
          finally
          {
             uninstall(contextParentA);
          }
          assertNoClassLoader(contextParentA);
       }
       finally
       {
          uninstall(contextB);
       }
       assertNoClassLoader(contextB);
    }
    
    public void testParentRedeploy() throws Exception
    {
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       MockClassLoadingMetaData b = new MockClassLoadingMetaData("b");
       b.setDomain("ChildDomain");
       b.setJ2seClassLoadingCompliance(true);
       b.setPathsAndPackageNames(B.class);
       b.getRequirements().addRequirement(factory.createRequirePackage(A.class.getPackage().getName()));
       KernelControllerContext contextB = install(b);
       try
       {
          assertNoClassLoader(contextB);
 
          MockClassLoadingMetaData aParent = new MockClassLoadingMetaData("aParent");
          aParent.setPathsAndPackageNames(A.class);
          KernelControllerContext contextParentA = install(aParent);
          try
          {
             ClassLoader clParentA = assertClassLoader(contextParentA);
             assertLoadClass(A.class, clParentA);
             assertLoadClassFail(B.class, clParentA);
 
             ClassLoader clB = assertClassLoader(contextB);
             assertLoadClass(A.class, clParentA);
             assertLoadClass(B.class, clB);
          }
          finally
          {
             uninstall(contextParentA);
          }
          assertNoClassLoader(contextParentA);
 
          assertNoClassLoader(contextB);
 
          contextParentA = install(aParent);
          try
          {
             ClassLoader clParentA = assertClassLoader(contextParentA);
             assertLoadClass(A.class, clParentA);
             assertLoadClassFail(B.class, clParentA);
 
             ClassLoader clB = assertClassLoader(contextB);
             assertLoadClass(A.class, clParentA);
             assertLoadClass(B.class, clB);
          }
          finally
          {
             uninstall(contextParentA);
          }
          assertNoClassLoader(contextParentA);
       }
       finally
       {
          uninstall(contextB);
       }
       assertNoClassLoader(contextB);
    }
    
    public void testParentOtherDomain() throws Exception
    {
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       MockClassLoadingMetaData aParent = new MockClassLoadingMetaData("aParent");
       aParent.setPathsAndPackageNames(A.class);
       aParent.setDomain("ParentDomain");
       KernelControllerContext contextParentA = install(aParent);
       try
       {
          ClassLoader clParentA = assertClassLoader(contextParentA);
          assertLoadClass(A.class, clParentA);
          assertLoadClassFail(B.class, clParentA);
 
          MockClassLoadingMetaData b = new MockClassLoadingMetaData("b");
          b.setDomain("ChildDomain");
          b.setParentDomain("ParentDomain");
          b.setJ2seClassLoadingCompliance(true);
          b.setPathsAndPackageNames(B.class);
          b.getRequirements().addRequirement(factory.createRequirePackage(A.class.getPackage().getName()));
          KernelControllerContext contextB = install(b);
          try
          {
                ClassLoader clB = assertClassLoader(contextB);
                assertLoadClass(A.class, clParentA);
                assertLoadClass(B.class, clB);
          }
          finally
          {
             uninstall(contextB);
          }
          assertNoClassLoader(contextB);
       }
       finally
       {
          uninstall(contextParentA);
       }
       assertNoClassLoader(contextParentA);
    }
    
    public void testParentOtherDomainLazy() throws Exception
    {
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       MockClassLoadingMetaData b = new MockClassLoadingMetaData("b");
       b.setDomain("ChildDomain");
       b.setParentDomain("ParentDomain");
       b.setJ2seClassLoadingCompliance(true);
       b.setPathsAndPackageNames(B.class);
       b.getRequirements().addRequirement(factory.createRequirePackage(A.class.getPackage().getName()));
       KernelControllerContext contextB = install(b);
       try
       {
          assertNoClassLoader(contextB);
 
          MockClassLoadingMetaData aParent = new MockClassLoadingMetaData("aParent");
          aParent.setPathsAndPackageNames(A.class);
          aParent.setDomain("ParentDomain");
          KernelControllerContext contextParentA = install(aParent);
          try
          {
             ClassLoader clParentA = assertClassLoader(contextParentA);
             assertLoadClass(A.class, clParentA);
             assertLoadClassFail(B.class, clParentA);
 
             ClassLoader clB = assertClassLoader(contextB);
             assertLoadClass(A.class, clParentA);
             assertLoadClass(B.class, clB);
          }
          finally
          {
             uninstall(contextParentA);
          }
          assertNoClassLoader(contextParentA);
       }
       finally
       {
          uninstall(contextB);
       }
       assertNoClassLoader(contextB);
    }
 
    public void testParentRedeployOtherDomain() throws Exception
    {
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       MockClassLoadingMetaData b = new MockClassLoadingMetaData("b");
       b.setDomain("ChildDomain");
       b.setParentDomain("ParentDomain");
       b.setJ2seClassLoadingCompliance(true);
       b.setPathsAndPackageNames(B.class);
       b.getRequirements().addRequirement(factory.createRequirePackage(A.class.getPackage().getName()));
       KernelControllerContext contextB = install(b);
       try
       {
          assertNoClassLoader(contextB);
 
          MockClassLoadingMetaData aParent = new MockClassLoadingMetaData("aParent");
          aParent.setPathsAndPackageNames(A.class);
          aParent.setDomain("ParentDomain");
          KernelControllerContext contextParentA = install(aParent);
          try
          {
             ClassLoader clParentA = assertClassLoader(contextParentA);
             assertLoadClass(A.class, clParentA);
             assertLoadClassFail(B.class, clParentA);
 
             ClassLoader clB = assertClassLoader(contextB);
             assertLoadClass(A.class, clParentA);
             assertLoadClass(B.class, clB);
          }
          finally
          {
             uninstall(contextParentA);
          }
          assertNoClassLoader(contextParentA);
 
          assertNoClassLoader(contextB);
 
          contextParentA = install(aParent);
          try
          {
             ClassLoader clParentA = assertClassLoader(contextParentA);
             assertLoadClass(A.class, clParentA);
             assertLoadClassFail(B.class, clParentA);
 
             ClassLoader clB = assertClassLoader(contextB);
             assertLoadClass(A.class, clParentA);
             assertLoadClass(B.class, clB);
          }
          finally
          {
             uninstall(contextParentA);
          }
          assertNoClassLoader(contextParentA);
       }
       finally
       {
          uninstall(contextB);
       }
       assertNoClassLoader(contextB);
    }
 
    public void testExplicitRequirementsInDefaultDomain() throws Exception
    {
      testExplicitRequirementsInDomain(ClassLoaderSystem.DEFAULT_DOMAIN_NAME);
    }
 
    public void testExplicitRequirementsInNewDomain() throws Exception
    {
      // FIXME - testExplicitRequirementsInDomain("SomeNewDomain");
    }
 
   protected void testExplicitRequirementsInDomain(String domain) throws Exception
    {
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       MockClassLoadingMetaData c = new MockClassLoadingMetaData("c");
       c.setPathsAndPackageNames(C.class);
       c.setImportAll(true);
       c.setExportAll(ExportAll.NON_EMPTY);
       KernelControllerContext contextC = install(c);
       try
       {
          MockClassLoadingMetaData b = new MockClassLoadingMetaData("b");
          b.setDomain(domain);
          b.setPathsAndPackageNames(B.class);
          b.getRequirements().addRequirement(factory.createRequirePackage(A.class.getPackage().getName()));
          KernelControllerContext contextB = install(b);
          try
          {
             assertNoClassLoader(contextB);
 
             MockClassLoadingMetaData a = new MockClassLoadingMetaData("a");
             a.setDomain(domain);
             a.setPathsAndPackageNames(A.class);
             a.getCapabilities().addCapability(factory.createPackage(A.class.getPackage().getName()));
             KernelControllerContext contextA = install(a);
             try
             {
                ClassLoader clA = assertClassLoader(contextA);
                assertLoadClass(A.class, clA);
                assertLoadClassFail(B.class, clA);
 
                ClassLoader clB = assertClassLoader(contextB);
                assertLoadClass(A.class, clA);
                assertLoadClass(B.class, clB);
 
                ClassLoader clC = assertClassLoader(contextC);
                assertLoadClass(C.class, clC);               
                if (ClassLoaderSystem.DEFAULT_DOMAIN_NAME.equals(domain))
                {
                   assertLoadClass(A.class, clC, clA);
                   assertLoadClass(B.class, clC, clB);
                }
                else
                {
                   assertLoadClassFail(A.class, clC);
                   assertLoadClassFail(B.class, clC);
                }
 
               assertLoadClassFail(C.class.getName(), clB);
             }
             finally
             {
                uninstall(contextA);
             }
          }
          finally
          {
             uninstall(contextB);
          }
          assertNoClassLoader(contextB);
       }
       finally
       {
          uninstall(contextC);
       }
    }
 }
