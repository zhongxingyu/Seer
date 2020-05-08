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
 package org.jboss.test.classloading.vfs.metadata.xml.test;
 
 import junit.framework.Test;
 import org.jboss.test.classloading.vfs.metadata.xml.AbstractJBossXBTest;
 import org.jboss.test.classloading.vfs.metadata.xml.support.NoopClassLoaderFactory;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultSchemaResolver;
 import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;
 import org.jboss.xb.binding.sunday.unmarshalling.SingletonSchemaResolverFactory;
import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory10;
 
 /**
  * NoopClassLoaderFactoryXMLUnitTestCase.
  *
  * @author <a href="ales.justin@jboss.com">Ales Justin</a>
  */
 public class NoopClassLoaderFactoryXMLUnitTestCase extends AbstractJBossXBTest
 {
    public static Test suite()
    {
       return suite(NoopClassLoaderFactoryXMLUnitTestCase.class);
    }
 
    public NoopClassLoaderFactoryXMLUnitTestCase(String name)
    {
       super(name);
    }
 
    protected void changeMetaDataClass(Class<?> clazz)
    {
       SingletonSchemaResolverFactory factory = SingletonSchemaResolverFactory.getInstance();
       SchemaBindingResolver resolver = factory.getSchemaBindingResolver();
      DefaultSchemaResolver dsr = assertInstanceOf(resolver, DefaultSchemaResolver.class);
      dsr.removeClassBinding("urn:jboss:classloader:1.0");
      dsr.addClassBinding("urn:jboss:classloader:1.0", clazz);
    }
 
    @Override
    protected void setUp() throws Exception
    {
       changeMetaDataClass(NoopClassLoaderFactory.class);
       try
       {
          super.setUp();
       }
       catch (Exception e)
       {
          changeMetaDataClass(VFSClassLoaderFactory10.class);
          throw e;
       }
    }
 
    @Override
    protected void tearDown() throws Exception
    {
       try
       {
          super.tearDown();
       }
       finally
       {
          changeMetaDataClass(VFSClassLoaderFactory10.class);
       }
    }
 
    public void testExportImportMixed() throws Exception
    {
       NoopClassLoaderFactory result = unmarshalObject(NoopClassLoaderFactory.class);
       assertNotNull(result);
    }
 }
