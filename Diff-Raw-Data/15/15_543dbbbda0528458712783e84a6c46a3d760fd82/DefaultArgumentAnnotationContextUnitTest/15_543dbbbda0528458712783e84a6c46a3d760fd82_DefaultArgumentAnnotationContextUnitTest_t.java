 /*
  Copyright (c) 2011, The Staccato-Commons Team
 
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; version 3 of the License.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
  */
 package net.sf.staccatocommons.instrument.context.internal;
 
 import static org.junit.Assert.*;
 import javassist.CtMethod;
 import javassist.NotFoundException;
 import net.sf.staccatocommons.instrument.context.ArgumentAnnotationContext;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test for {@link DefaultArgumentAnnotationContext}
  * 
  * @author flbulgarelli
  * 
  */
 public class DefaultArgumentAnnotationContextUnitTest extends AbstractAnnotationContextUnitTest {
 
   private ArgumentAnnotationContext context;
   private CtMethod method;
 
   /**
    * Instatiates the context
    * 
    * @throws NotFoundException
    **/
   @Before
   public void createContext() throws NotFoundException {
     context = new DefaultArgumentAnnotationContext(getPool(), getLogger());
    method = getPool().getMethod("net.sf.staccatocommons.util.Range", "from");
     ((DefaultArgumentAnnotationContext) context).setBehavior(method);
     ((DefaultArgumentAnnotationContext) context).setParameterNumber(1);
   }
 
   /**
    * Test method for
    * {@link DefaultArgumentAnnotationContext#getArgumentBehavior()} .
    */
   @Test
   public void testGetArgumentBehavior() {
     assertSame(method, context.getArgumentBehavior());
   }
 
   /**
    * Test method for
    * {@link DefaultArgumentAnnotationContext#getArgumentNumber()} .
    */
   @Test
   public void testGetArgumentNumber() {
     assertEquals(1, context.getArgumentNumber());
   }
 
   /**
    * Test method for
    * {@link DefaultArgumentAnnotationContext#isConstructorArgument()} .
    */
   @Test
   public void testIsConstructorArgument() {
     assertFalse(context.isConstructorArgument());
   }
 
   /**
    * Test method for
    * {@link DefaultArgumentAnnotationContext#getArgumentIdentifier()} .
    */
   @Test
   public void testGetArgumentIdentifier() {
     assertEquals("$2", context.getArgumentIdentifier());
   }
 
 }
