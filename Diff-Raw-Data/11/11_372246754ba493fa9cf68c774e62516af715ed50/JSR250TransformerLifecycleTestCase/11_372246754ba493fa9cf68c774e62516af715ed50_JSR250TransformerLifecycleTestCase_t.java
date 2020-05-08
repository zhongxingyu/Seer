 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.ibeans;
 
 import org.mule.ibeans.test.AbstractIBeansTestCase;
 import org.mule.ibeans.api.application.Transformer;
 import org.mule.util.concurrent.Latch;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 
 import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
 
 /**
  * Tests that transformers annotated with JSR250 @PostConstruct and @PreDestroy are called
  */
 public class JSR250TransformerLifecycleTestCase extends AbstractIBeansTestCase
 {
     public static final long TIMEOUT = 2000;
     private Latch initLatch = new Latch();
     private Latch destroyLatch = new Latch();
 
     @Override
     protected void doSetUp() throws Exception
     {
         registerBeans(new DummyTransformers());
     }
 
     public void testLifecycleAnnotations() throws Exception
     {
 
         assertTrue(initLatch.await(TIMEOUT, TimeUnit.MILLISECONDS));
         assertFalse(destroyLatch.await(TIMEOUT, TimeUnit.MILLISECONDS));
         muleContext.dispose();
         assertTrue(destroyLatch.await(TIMEOUT, TimeUnit.MILLISECONDS));
     }
 
     public class DummyTransformers
     {
        public DummyTransformers()
        {
        }

         @Transformer
         public String stringBufferToString(StringBuffer buffer)
         {
             return buffer.toString();
         }
 
         @PostConstruct
         public void init()
         {
             initLatch.countDown();
 
         }
 
         @PreDestroy
         public void destroy()
         {
             destroyLatch.countDown();
         }
     }
 }
