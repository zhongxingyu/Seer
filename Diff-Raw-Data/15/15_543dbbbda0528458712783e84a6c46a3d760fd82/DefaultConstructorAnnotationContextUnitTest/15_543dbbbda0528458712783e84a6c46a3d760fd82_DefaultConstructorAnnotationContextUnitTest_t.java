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
 import javassist.CtConstructor;
 
 import org.junit.Test;
 
 /**
  * @author flbulgarelli
  * 
  */
 public class DefaultConstructorAnnotationContextUnitTest extends AbstractAnnotationContextUnitTest {
 
   /**
    * Test method for
    * {@link DefaultConstructorAnnotationContext#getConstructor()}.
    * 
    * @throws Exception
    */
   @Test
   public void testGetConstructor() throws Exception {
    DefaultConstructorAnnotationContext context = new DefaultConstructorAnnotationContext(
      getPool(),
      getLogger());
    CtConstructor constructor = getPool()
      .get("net.sf.staccatocommons.util.Range")
      .getConstructors()[0];
     context.setConstructor(constructor);
     assertSame(constructor, context.getConstructor());
   }
 
 }
