 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 package org.jboss.test.classloader.resources.tests;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashSet;
 
 import junit.framework.Test;
 
 import org.jboss.classloader.spi.ClassLoaderDomain;
 import org.jboss.classloader.spi.ClassLoaderSystem;
 import org.jboss.classloader.spi.ParentPolicy;
 import org.jboss.classloader.spi.filter.FilteredDelegateLoader;
 import org.jboss.classloader.test.support.MockClassLoaderPolicy;
 import org.jboss.test.classloader.AbstractClassLoaderTestWithSecurity;
 
 /**
  * ClassLoaderSystemUnitTestCase.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class ResourceUnitTestCase extends AbstractClassLoaderTestWithSecurity
 {
    public static Test suite()
    {
       return suite(ResourceUnitTestCase.class);
    }
 
    public ResourceUnitTestCase(String name)
    {
       super(name);
    }
    
    public void testGetResource() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy);
       
       assertGetResource("a/", "com/acme/p1/testResource", classLoader);
    }
    
    public void testGetResourceFromDefaultPackage() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("");
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy);
       
       assertGetResource("a/", "testResource", classLoader);
    }
    
    public void testGetResourceFromDelegate() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
 
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setDelegates(Collections.singletonList(new FilteredDelegateLoader(policy)));
       
       system.registerClassLoaderPolicy(policy);
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy2);
       
       assertGetResource("a/", "com/acme/p1/testResource", classLoader);
    }
    
    public void testGetResourceFromDefaultPackageFromDelegate() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("");
 
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setDelegates(Collections.singletonList(new FilteredDelegateLoader(policy)));
       
       system.registerClassLoaderPolicy(policy);
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy2);
       
       assertGetResource("a/", "testResource", classLoader);
    }
    
    public void testGetResourceWithDotFromDelegate() throws Exception
    {
       enableTrace("org.jboss.classloader");
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       policy.setPackageNames(new String[] { "com.acme.p1" } );
 
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setDelegates(Collections.singletonList(new FilteredDelegateLoader(policy)));
       
       system.registerClassLoaderPolicy(policy);
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy2);
       
       assertGetResource("a/", "com/acme/p1/testResource.xml", classLoader);
    }
    
    public void testGetResourceWithDotFromDelegatNotFound() throws Exception
    {
       enableTrace("org.jboss.classloader");
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       policy.setPackageNames(new String[] { "com.acme.p1" } );
 
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setDelegates(Collections.singletonList(new FilteredDelegateLoader(policy)));
       
       system.registerClassLoaderPolicy(policy);
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy2);
       
       assertGetResourceFail("testResource.xml", classLoader);
    }
    
    public void testGetResourceUsingAllImports() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       policy.setPackageNames(new String[] { "com.acme.p1" } );
 
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setImportAll(true);
       
       system.registerClassLoaderPolicy(policy);
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy2);
       
       assertGetResource("a/", "com/acme/p1/testResource", classLoader);
    }
    
    public void testGetResourceFromDefaultPackageUsingAllImports() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("");
       policy.setPackageNames(new String[] { "" } );
 
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setImportAll(true);
       
       system.registerClassLoaderPolicy(policy);
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy2);
       
       assertGetResource("a/", "testResource", classLoader);
    }
    
    public void testGetResourceNotFound() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       policy.setPackageNames(new String[] { "com.acme.p1" } );
 
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       
       system.registerClassLoaderPolicy(policy);
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy2);
       
       assertGetResourceFail("com/acme/p1/testResource", classLoader);
    }
    
    public void testGetResourceFromParentBefore() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       ClassLoaderDomain parent = system.createAndRegisterDomain("parent");
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       policy.setPackageNames(new String[] { "com.acme.p1" } );
       system.registerClassLoaderPolicy(parent, policy);
 
       ClassLoaderDomain child = system.createAndRegisterDomain("child", ParentPolicy.BEFORE, parent);
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setPrefix("b/");
       policy2.setPath("com/acme/p1");
       ClassLoader classLoader = system.registerClassLoaderPolicy(child, policy2);
       
      assertGetResource("b/", "com/acme/p1/testResource", classLoader);
    }
    
    public void testGetResourceFromParentAfter() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       ClassLoaderDomain parent = system.createAndRegisterDomain("parent");
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       policy.setPackageNames(new String[] { "com.acme.p1" } );
       system.registerClassLoaderPolicy(parent, policy);
 
       ClassLoaderDomain child = system.createAndRegisterDomain("child", ParentPolicy.AFTER, parent);
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       ClassLoader classLoader = system.registerClassLoaderPolicy(child, policy2);
       
       assertGetResource("a/", "com/acme/p1/testResource", classLoader);
    }
    
    public void testGetResourceFromParentAfterNotReached() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       ClassLoaderDomain parent = system.createAndRegisterDomain("parent");
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       policy.setPackageNames(new String[] { "com.acme.p1" } );
       system.registerClassLoaderPolicy(parent, policy);
 
       ClassLoaderDomain child = system.createAndRegisterDomain("child", ParentPolicy.AFTER, parent);
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setPrefix("b/");
       policy2.setPath("com/acme/p1");
       ClassLoader classLoader = system.registerClassLoaderPolicy(child, policy2);
       
       assertGetResource("b/", "com/acme/p1/testResource", classLoader);
    }
    
    public void testGetResourceFromDefaultPackageFromParent() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       ClassLoaderDomain parent = system.createAndRegisterDomain("parent");
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("");
       policy.setPackageNames(new String[] { "" } );
       system.registerClassLoaderPolicy(parent, policy);
 
       ClassLoaderDomain child = system.createAndRegisterDomain("child", ParentPolicy.BEFORE, parent);
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setPrefix("b/");
       policy2.setPath("com/acme/p1");
       ClassLoader classLoader = system.registerClassLoaderPolicy(child, policy2);
       
       assertGetResource("a/", "testResource", classLoader);
    }
    
    public void testGetResources() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy);
       
       assertGetResources("com/acme/p1/testResource", classLoader, "a/");
    }
    
    public void testGetResourcesFromDefaultPackage() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("");
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy);
       
       assertGetResources("testResource", classLoader, "a/");
    }
    
    public void testGetResourcesMultiple() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setImportAll(true);
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setPrefix("b/");
       policy2.setPath("com/acme/p1");
       policy2.setPackageNames(new String[] { "com.acme.p1" });
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy);
       system.registerClassLoaderPolicy(policy2);
       
       assertGetResources("com/acme/p1/testResource", classLoader, "a/", "b/");
    }
    
    public void testGetResourcesFromDefaultPackageMultiple() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setImportAll(true);
       policy.setPrefix("a/");
       policy.setPath("");
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setPrefix("b/");
       policy2.setPath("");
       policy2.setPackageNames(new String[] { "" });
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy);
       system.registerClassLoaderPolicy(policy2);
       
       assertGetResources("testResource", classLoader, "a/", "b/");
    }
    
    public void testGetResourcesFromDelegate() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
 
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setDelegates(Collections.singletonList(new FilteredDelegateLoader(policy)));
       
       system.registerClassLoaderPolicy(policy);
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy2);
       
       assertGetResources("com/acme/p1/testResource", classLoader, "a/");
    }
    
    public void testGetResourcesFromDefaultPackageFromDelegate() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("");
 
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setDelegates(Collections.singletonList(new FilteredDelegateLoader(policy)));
       
       system.registerClassLoaderPolicy(policy);
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy2);
       
       assertGetResources("testResource", classLoader, "a/");
    }
    
    public void testGetResourcesWithDotFromDelegate() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       policy.setPackageNames(new String[] { "com.acme.p1" } );
 
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setDelegates(Collections.singletonList(new FilteredDelegateLoader(policy)));
       
       system.registerClassLoaderPolicy(policy);
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy2);
       
       assertGetResources("com/acme/p1/testResource.xml", classLoader, "a/");
    }
    
    public void testGetResourcesWithDotFromDelegateNotFound() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       policy.setPackageNames(new String[] { "com.acme.p1" } );
 
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setDelegates(Collections.singletonList(new FilteredDelegateLoader(policy)));
       
       system.registerClassLoaderPolicy(policy);
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy2);
       
       assertGetResourcesFail("testResource.xml", classLoader);
    }
    
    public void testGetResourcesUsingAllImports() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       policy.setPackageNames(new String[] { "com.acme.p1" } );
 
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setImportAll(true);
       
       system.registerClassLoaderPolicy(policy);
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy2);
       
       assertGetResources("com/acme/p1/testResource", classLoader, "a/");
    }
    
    public void testGetResourcesFromDefaultPackageUsingAllImports() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("");
       policy.setPackageNames(new String[] { "" } );
 
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setImportAll(true);
       
       system.registerClassLoaderPolicy(policy);
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy2);
       
       assertGetResources("testResource", classLoader, "a/");
    }
    
    public void testGetResourcesNotFound() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       policy.setPackageNames(new String[] { "com.acme.p1" } );
 
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       
       system.registerClassLoaderPolicy(policy);
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy2);
       
       assertGetResourcesFail("com/acme/p1/testResource", classLoader);
    }
    
    public void testGetResourcesFromParentBefore() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       ClassLoaderDomain parent = system.createAndRegisterDomain("parent");
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       policy.setPackageNames(new String[] { "com.acme.p1" } );
       system.registerClassLoaderPolicy(parent, policy);
 
       ClassLoaderDomain child = system.createAndRegisterDomain("child", ParentPolicy.BEFORE, parent);
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setPrefix("b/");
       policy2.setPath("com/acme/p1");
       ClassLoader classLoader = system.registerClassLoaderPolicy(child, policy2);
       
       assertGetResources("com/acme/p1/testResource", classLoader, "a/", "b/");
    }
    
    public void testGetResourcesFromParentAfter() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       ClassLoaderDomain parent = system.createAndRegisterDomain("parent");
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("com/acme/p1");
       policy.setPackageNames(new String[] { "com.acme.p1" } );
       system.registerClassLoaderPolicy(parent, policy);
 
       ClassLoaderDomain child = system.createAndRegisterDomain("child", ParentPolicy.AFTER, parent);
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setPrefix("b/");
       policy2.setPath("com/acme/p1");
       ClassLoader classLoader = system.registerClassLoaderPolicy(child, policy2);
       
       assertGetResources("com/acme/p1/testResource", classLoader, "a/", "b/");
    }
    
    public void testGetResourcesFromDefaultPackageFromParent() throws Exception
    {
       ClassLoaderSystem system = createClassLoaderSystem();
       ClassLoaderDomain parent = system.createAndRegisterDomain("parent");
       MockClassLoaderPolicy policy = createMockClassLoaderPolicy();
       policy.setPrefix("a/");
       policy.setPath("");
       policy.setPackageNames(new String[] { "" } );
       system.registerClassLoaderPolicy(parent, policy);
 
       ClassLoaderDomain child = system.createAndRegisterDomain("child", ParentPolicy.BEFORE, parent);
       MockClassLoaderPolicy policy2 = createMockClassLoaderPolicy();
       policy2.setPrefix("b/");
       policy2.setPath("");
       ClassLoader classLoader = system.registerClassLoaderPolicy(child, policy2);
       
       assertGetResources("testResource", classLoader, "a/", "b/");
    }
    
    protected URL assertGetResource(String prefix, String resourcePath, ClassLoader classLoader) throws Exception
    {
       URL url = classLoader.getResource(resourcePath);
       assertNotNull("Should have got resource prefix=" + prefix + " resourcePath " + resourcePath + " from " + classLoader, url);
       SecurityManager sm = suspendSecurity();
       try
       {
          InputStream is = url.openStream();
          try
          {
             InputStreamReader reader = new InputStreamReader(is);
             char[] chars = new char[1000];
             int count = 0;
             int read = reader.read(chars);
             while (read != -1)
             {
                count += read;
                read = reader.read(chars, read, 1000 - read);
             }
             String string = new String(chars, 0, count);
             assertEquals("Should have read the correct resource", prefix + resourcePath, string);
          }
          finally
          {
             is.close();
          }
       }
       finally
       {
          resumeSecurity(sm);
       }
       return url;
    }
    
    protected void assertGetResourceFail(String resourcePath, ClassLoader classLoader) throws Exception
    {
       URL url = classLoader.getResource(resourcePath);
       assertNull("Should NOT have got resource " + resourcePath + " from " + classLoader, url);
    }
    
    protected Enumeration<URL> assertGetResources(String resourcePath, ClassLoader classLoader, String... prefixes) throws Exception
    {
       Enumeration<URL> urls = classLoader.getResources(resourcePath);
       
       HashSet<String> foundResources = new HashSet<String>();
       SecurityManager sm = suspendSecurity();
       try
       {
          while (urls.hasMoreElements())
          {
             URL url = urls.nextElement();
             InputStream is = url.openStream();
             try
             {
                InputStreamReader reader = new InputStreamReader(is);
                char[] chars = new char[1000];
                int count = 0;
                int read = reader.read(chars);
                while (read != -1)
                {
                   count += read;
                   read = reader.read(chars, read, 1000 - read);
                }
                String string = new String(chars, 0, count);
                foundResources.add(string);
             }
             finally
             {
                is.close();
             }
          }
       }
       finally
       {
          resumeSecurity(sm);
       }
       
       HashSet<String> expectedResources = new HashSet<String>();
       for (String prefix : prefixes)
          expectedResources.add(prefix + resourcePath);
       
       assertEquals(expectedResources, foundResources);
       
       return urls;
    }
    
    protected void assertGetResourcesFail(String resourcePath, ClassLoader classLoader) throws Exception
    {
       Enumeration<URL> urls = classLoader.getResources(resourcePath);
       if (urls.hasMoreElements())
       {
          HashSet<URL> found = new HashSet<URL>();
          while (urls.hasMoreElements())
             found.add(urls.nextElement());
          fail("Should NOT have got resources " + resourcePath + " from " + classLoader + " found " + urls);
       }
    }
 }
