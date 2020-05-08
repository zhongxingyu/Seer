 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
 package org.jboss.test.beaninfo.test;
 
 import java.security.AccessControlException;
 
 import junit.framework.Test;
 
 import org.jboss.beans.info.spi.BeanAccessMode;
 import org.jboss.beans.info.spi.BeanInfo;
 import org.jboss.beans.info.spi.PropertyInfo;
 import org.jboss.config.plugins.BasicConfiguration;
 import org.jboss.config.spi.Configuration;
 import org.jboss.test.AbstractTestCaseWithSetup;
 import org.jboss.test.AbstractTestDelegate;
 import org.jboss.test.beaninfo.support.FieldsClass;
 
 /**
  * Access restriction test.
  *
  * @author <a href="ales.justin@jboss.com">Ales Justin</a>
  */
 public class AccessRestrictionTestCase extends AbstractTestCaseWithSetup
 {
    /** The bean info factory */
    private Configuration configuration = new BasicConfiguration();
 
    /**
     * Create a new ContainerTest.
     *
     * @param name the test name
     */
    public AccessRestrictionTestCase(String name)
    {
       super(name);
    }
 
    public static Test suite()
    {
       return suite(AccessRestrictionTestCase.class);
    }
 
    /**
     * Default setup with security manager enabled
     *
     * @param clazz the class
     * @return the delegate
     * @throws Exception for any error
     */
    public static AbstractTestDelegate getDelegate(Class<?> clazz) throws Exception
    {
       AbstractTestDelegate delegate = new AbstractTestDelegate(clazz);
       delegate.enableSecurity = true;
       return delegate;
    }
 
    public void testBeanFieldAccess() throws Throwable
    {
       // First try to get the Bean info without the priviledge on the private field
       FieldsClass test = new FieldsClass();
       // This should work
       BeanInfo beanInfo = configuration.getBeanInfo(FieldsClass.class, BeanAccessMode.ALL);
       
       // We should be able to set the public field
       beanInfo.setProperty(test, "pubString", "public");
       assertEquals("public", test.pubString);
       
      // But we should be able to set the private field
       try
       {
          beanInfo.setProperty(test, "privString", "private");
          fail("should not be here");
       }
       catch (Throwable t)
       {
          checkThrowable(AccessControlException.class, t);
       }
       try
       {
          beanInfo.getProperty(test, "privString");
          fail("should not be here");
       }
       catch (Throwable t)
       {
          checkThrowable(AccessControlException.class, t);
       }
       assertNull(test.getPrivStringNotGetter());
 
       // Repeat for the properties
       PropertyInfo pubProp = beanInfo.getProperty("pubString");
       test = new FieldsClass();
       
       pubProp.set(test, "public");
       assertEquals("public", test.pubString);
       
       PropertyInfo privProp = beanInfo.getProperty("privString");
       try
       {
          privProp.set(test, "private");
          fail("should not be here");
       }
       catch (Throwable t)
       {
          checkThrowable(AccessControlException.class, t);
       }
       try
       {
          privProp.get(test);
          fail("should not be here");
       }
       catch (Throwable t)
       {
          checkThrowable(AccessControlException.class, t);
       }
       assertNull(test.getPrivStringNotGetter());
       
       // Now lets disable security and check we can do what we couldn't do before
       SecurityManager sm = suspendSecurity();
       try
       {
          test = new FieldsClass();
          beanInfo.setProperty(test, "privString", "private");
          assertEquals("private", beanInfo.getProperty(test, "privString"));
 
          test = new FieldsClass();
          privProp.set(test, "private");
          assertEquals("private", privProp.get(test));
       }
       finally
       {
          resumeSecurity(sm);
       }
    }
 }
