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
 package org.jboss.test.kernel.inject.test;
 
 import junit.framework.Test;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.test.kernel.inject.support.CallbackTestObject;
 import org.jboss.test.kernel.junit.ManualMicrocontainerTest;
 
 /**
  * Cardinality tests.
  *
  * @author <a href="mailto:ales.justin@gmail.com">Ales Justin</a>
  */
 public class CardinalityCallbackTestCase extends ManualMicrocontainerTest
 {
    public CardinalityCallbackTestCase(String name)
    {
       super(name);
    }
 
    public static Test suite()
    {
       return suite(CardinalityCallbackTestCase.class);
    }
 
    public void testCallback() throws Throwable
    {
       KernelControllerContext context = getControllerContext("testObject", ControllerState.NOT_INSTALLED);
       assertNull(context.getTarget());
       assertEquals(context.getState(), ControllerState.NOT_INSTALLED);
 
       change(context, ControllerState.CREATE);
       CallbackTestObject injectee = (CallbackTestObject)context.getTarget();
       assertNotNull(injectee);
       assertNull(injectee.getTesterInterfaces());
 
       KernelControllerContext tester1 = getControllerContext("tester1", ControllerState.NOT_INSTALLED);
       assertNotNull(tester1);
       change(tester1, ControllerState.INSTALLED);
 
       assertNull(injectee.getTesterInterfaces());
 
       KernelControllerContext tester2 = getControllerContext("tester2", ControllerState.NOT_INSTALLED);
       assertNotNull(tester2);
       change(tester2, ControllerState.INSTALLED);
 
       change(context, ControllerState.INSTALLED);
 
       assertNotNull(injectee.getTesterInterfaces());
       assertEquals(2, injectee.getTesterInterfaces().size());
 
       KernelControllerContext tester3 = getControllerContext("tester3", ControllerState.NOT_INSTALLED);
       assertNotNull(tester3);
       change(tester3, ControllerState.INSTALLED);
 
       assertNotNull(injectee.getTesterInterfaces());
       assertEquals(3, injectee.getTesterInterfaces().size());

      change(tester3, ControllerState.NOT_INSTALLED);
      assertEquals(2, injectee.getTesterInterfaces().size());
      assertEquals(context.getState(), ControllerState.INSTALLED);

      change(tester2, ControllerState.NOT_INSTALLED);
      assertEquals(context.getState(), ControllerState.CONFIGURED);
    }
 
 }
