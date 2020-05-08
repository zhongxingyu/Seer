 package com.maxifier.guice.mbean;
 
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.assertTrue;
 
 /**
  * Created by: Aleksey Didik
  * Date: 5/26/11
  * Time: 9:55 PM
  * <p/>
  * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
  * Code proprietary and confidential.
  * Use is subject to license terms.
  *
  * @author Aleksey Didik
  */
 public class CGLIBMBeanGeneratorTest {
 
     @Test
     public void testNameCompliance() throws Exception {
         CGLIBMBeanGenerator cglibmBeanGenerator = new CGLIBMBeanGenerator();
        Foo mbeanPretender = new Foo(false
        );
         Object mbean = cglibmBeanGenerator.makeMBean(mbeanPretender);
         MBeanManagerImpl.checkCompliantion(mbean);
     }
 
     @Test
     public void testMethodDelegation() throws Exception {
         CGLIBMBeanGenerator cglibmBeanGenerator = new CGLIBMBeanGenerator();
        Foo mbeanPretender = new Foo(false);
         Object mbean = cglibmBeanGenerator.makeMBean(mbeanPretender);
         mbean.getClass().getMethod("hello").invoke(mbean);
         assertTrue(mbeanPretender.called, "mbeanPretender method have to be called");
     }
 
     public static class Foo {
 
         boolean called;
 
        public Foo(boolean called) {
            this.called = called;
        }

         @MBeanMethod
         public void hello() {
             called = true;
         }
 
     }
 }
