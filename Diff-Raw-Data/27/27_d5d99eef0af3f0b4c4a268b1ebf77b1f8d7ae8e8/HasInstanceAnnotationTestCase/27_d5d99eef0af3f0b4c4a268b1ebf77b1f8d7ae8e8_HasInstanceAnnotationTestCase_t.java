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
 package org.jboss.test.microcontainer.test;
 
 import junit.framework.Test;

import org.jboss.test.AbstractTestDelegate;
 import org.jboss.test.aop.junit.AbstractTypeTest;
import org.jboss.test.aop.junit.AbstractTypeTestDelegate;
 
 /**
  * Test instance annotations, do we require aop proxy for them.
  *
  * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
  */
 public class HasInstanceAnnotationTestCase extends AbstractTypeTest
 {
    public HasInstanceAnnotationTestCase(String name)
    {
       super(name);
    }
 
    public static Test suite()
    {
       return suite(HasInstanceAnnotationTestCase.class);
    }
   
   public static AbstractTestDelegate getDelegate(Class<?> clazz) throws Exception
   {
      //Don't use security for this test
      AbstractTypeTestDelegate delegate = new AbstractTypeTestDelegate(clazz);
      return delegate;
   }
 
    public void testInstanceAnnotation() throws Throwable
    {
       SecurityManager sm = suspendSecurity();
       try
       {
          assertIsPojo("Bean1");
          assertIsPojo("Bean2");
    
          assertIsAspectized("Bean3");
          assertIsAspectized("Bean4");
    
          assertIsAspectized("Bean5");
          assertIsAspectized("Bean6");
    
          assertIsPojo("Bean7");
          assertIsPojo("Bean8");
          assertIsPojo("Bean9");
       }
       finally
       {
          resumeSecurity(sm);
       }
    }
 }
