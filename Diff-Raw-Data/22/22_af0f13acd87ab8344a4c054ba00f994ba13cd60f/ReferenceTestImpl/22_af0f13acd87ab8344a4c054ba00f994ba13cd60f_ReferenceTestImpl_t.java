 package org.everit.osgi.servicereference.tests;
 
 /*
  * Copyright (c) 2011, Everit Kft.
  *
  * All rights reserved.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301  USA
  */
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.everit.osgi.servicereference.core.Reference;
 import org.everit.osgi.servicereference.core.ServiceUnavailableException;
 import org.everit.osgi.servicereference.core.ServiceUnavailableHandler;
 import org.everit.osgi.servicereference.core.WarmUpListener;
 import org.junit.Assert;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Filter;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceRegistration;
 
 /**
  * Implementation that contains tests for reference holder classes.
  */
 public class ReferenceTestImpl implements ReferenceTest {
 
     /**
      * The context of the test bundle.
      */
     private final BundleContext bundleContext;
 
     /**
      * Simple constructor that sets fields.
      * 
      * @param bundleContext
      *            The value of {@link #bundleContext} property.
      */
     ReferenceTestImpl(final BundleContext bundleContext) {
         this.bundleContext = bundleContext;
     }
 
     private Filter createTestFilter() {
         Filter filter = null;
         try {
             filter = bundleContext.createFilter("(testservice=true)");
         } catch (InvalidSyntaxException e) {
             Assert.fail(e.getMessage());
         }
         return filter;
     }
 
     @Override
     public void testCustomHandler() {
         final int testTimeout = 3;
         Reference reference = new Reference(bundleContext, new Class<?>[] { Comparable.class },
                 createTestFilter(), testTimeout);
 
         reference.setServiceUnavailableHander(new ServiceUnavailableHandler() {
 
             @Override
             public Object handle(final String serviceFilter, final Method method, final Object[] args,
                     final long timeout) {
                 return (int) (timeout * (-1));
             }
         });
         reference.open();
         Comparable<Integer> service = reference.getProxyInstance();
        Assert.assertEquals(testTimeout * (-1), service.compareTo(1));
 
         reference.setServiceUnavailableHander(new ServiceUnavailableHandler() {
 
             @Override
             public Object handle(final String serviceFilter, final Method method, final Object[] args,
                     final long timeout) {
                 throw new NullPointerException("test");
             }
         });
         try {
             service.compareTo(null);
             Assert.fail();
         } catch (NullPointerException e) {
             Assert.assertEquals("test", e.getMessage());
         }
         reference.close();
     }
 
     @Override
     public void testException() {
         Hashtable<String, Object> properties = new Hashtable<String, Object>();
         properties.put("testservice", "true");
         ServiceRegistration<Closeable> existingSR = bundleContext.registerService(Closeable.class,
                 new Closeable() {
 
                     @Override
                     public void close() throws IOException {
                         throw new IOException("Test exception");
                     }
                 }, properties);
         try {
 
             Reference reference = new Reference(bundleContext, new Class[] { Closeable.class }, createTestFilter(), 1);
             reference.open();
             Closeable proxyInstance = reference.getProxyInstance();
             try {
                 proxyInstance.close();
                 Assert.fail("An IOException should have been dropped");
             } catch (IOException e) {
                 // Good behavior
             }
             reference.close();
         } finally {
             existingSR.unregister();
         }
     }
 
     @Override
     public void testExistingReference() {
         Hashtable<String, Object> properties = new Hashtable<String, Object>();
         properties.put("testservice", "true");
 
         @SuppressWarnings("rawtypes")
         ServiceRegistration<List> existingSR = bundleContext.registerService(List.class,
                 new ArrayList<String>(Arrays.asList("Test")), properties);
 
         try {
             Filter filter = createTestFilter();
             Reference reference = new Reference(bundleContext, new Class<?>[] { List.class }, filter, 1);
 
             Assert.assertEquals(filter, reference.getFilter());
 
             reference.open();
 
             List<String> proxyInstance = reference.getProxyInstance();
 
             Assert.assertTrue(proxyInstance.contains("Test"));
 
             reference.close();
         } finally {
             existingSR.unregister();
         }
     }
 
     @Override
     public void testLaterAvailableService() {
         Reference reference = new Reference(bundleContext, new Class<?>[] { List.class },
                 createTestFilter(), 2000);
         reference.open();
         final List<Integer> proxyInstance = reference.getProxyInstance();
         final AtomicInteger result = new AtomicInteger(0);
         new Thread(new Runnable() {
 
             @Override
             public void run() {
                 try {
                     result.set(proxyInstance.get(0));
                 } catch (ServiceUnavailableException e) {
                     Assert.fail(e.getMessage());
                 }
             }
         }).start();
 
         try {
             // Wait a bit to be sure that the get method is called on the proxy instance before the service is
             // registered. Not the nicest solution but for a test it is ok.
             Thread.sleep(50);
         } catch (InterruptedException e1) {
             Assert.fail(e1.getMessage());
         }
         Hashtable<String, Object> properties = new Hashtable<String, Object>();
         properties.put("testservice", "true");
 
         @SuppressWarnings("rawtypes")
         ServiceRegistration<List> existingSR = bundleContext.registerService(List.class,
                 new ArrayList<Integer>(Arrays.asList(1)), properties);
 
         try {
             for (int i = 0; (i < 100) && (result.get() != 1); i++) {
                 try {
                     Thread.sleep(10);
                 } catch (InterruptedException e) {
                     Assert.fail(e.getMessage());
                 }
             }
             Assert.assertEquals(1, result.get());
             reference.close();
         } finally {
             existingSR.unregister();
         }
     }
 
     @Override
     public void testNoFilter() {
         try {
             new Reference(bundleContext, new Class[] { List.class }, null, 1);
             Assert.fail("In case no filter is provided for Reference Constructor an IllegalArgument should be thrown");
         } catch (IllegalArgumentException e) {
             // Good behavior
         }
     }
 
     @Override
     public void testNoInterfaceDefinition() {
         try {
             new Reference(bundleContext, new Class[0], createTestFilter(), 1);
             Assert.fail("In case of an empty interface array an exception should be thrown");
         } catch (IllegalArgumentException e) {
             // Right behavior
         }
 
         try {
             new Reference(bundleContext, null, createTestFilter(), 1);
             Assert.fail("In case null interface array an exception should be thrown");
         } catch (IllegalArgumentException e) {
             // Right behavior
         }
     }
 
     @Override
     public void testNotAllRequiredInterfaces() {
         Hashtable<String, Object> properties = new Hashtable<String, Object>();
         properties.put("testservice", "true");
 
         @SuppressWarnings("rawtypes")
         ServiceRegistration<List> existingSR = bundleContext.registerService(List.class,
                 new ArrayList<String>(Arrays.asList("Test")), properties);
 
         try {
             Reference reference = new Reference(bundleContext, new Class<?>[] { List.class, Comparable.class },
                     createTestFilter(), 1);
 
             reference.open();
 
             List<String> proxyInstance = reference.getProxyInstance();
             try {
                 proxyInstance.contains("Test");
                 Assert.fail("Should throw a ServiceUnavailable exception as there is no service registered which"
                         + " implements all the necessary interfaces");
             } catch (ServiceUnavailableException e) {
                 // Correct. An exception should be thrown as not all the required interfaces are implemented by the
                 // registered service object.
             }
             reference.close();
         } finally {
             existingSR.unregister();
         }
     }
 
     @Override
     public void testNotOpenedReference() {
         Reference reference = new Reference(bundleContext, new Class[] { List.class }, createTestFilter(), 1);
         List<Integer> proxyInstance = reference.getProxyInstance();
         try {
             proxyInstance.add(1);
             Assert.fail("IllegalStateException should have happened as reference is not opened");
         } catch (IllegalStateException e) {
             // Good behavior
         }
     }
 
     @Override
     public void testServiceModification() {
         // First: doing a normal service call
         Hashtable<String, Object> properties = new Hashtable<String, Object>();
         properties.put("testservice", "true");
 
         @SuppressWarnings("rawtypes")
         ServiceRegistration<Comparable> existingSR = bundleContext.registerService(Comparable.class,
                 Integer.valueOf(1), properties);
 
         try {
 
             Reference reference = new Reference(bundleContext, new Class<?>[] { Comparable.class },
                     createTestFilter(), 1);
 
             reference.open();
 
             Comparable<Integer> serviceProxy = reference.getProxyInstance();
             Assert.assertEquals(0, serviceProxy.compareTo(1));
             // Normal service call is done
 
             // Second: changing the properties so the service is unregistered. An exception should be thrown
             Hashtable<String, Object> emptyProperties = new Hashtable<String, Object>();
             existingSR.setProperties(emptyProperties);
             try {
                 serviceProxy.compareTo(1);
                 Assert.fail();
             } catch (ServiceUnavailableException e) {
                 // Good behavior
             }
 
             // Third: setting back the original properties so the normal service call works again
             existingSR.setProperties(properties);
             Assert.assertEquals(0, serviceProxy.compareTo(1));
 
             // Fourth: changing the properties but leaving the testservice as well to let modifiedService being called
             // in
             // the hidden ServiceTrackerCustomizer
             Hashtable<String, Object> addonProperties = new Hashtable<String, Object>();
             addonProperties.put("testservice", "true");
             addonProperties.put("testotherprop", "done");
             existingSR.setProperties(addonProperties);
             Assert.assertEquals(0, serviceProxy.compareTo(1));
 
             reference.close();
         } finally {
             existingSR.unregister();
         }
     }
 
     @Override
     public void testTimeout() {
         Filter testFilter = createTestFilter();
         Reference reference = new Reference(bundleContext, new Class<?>[] { List.class },
                 testFilter, 1);
         reference.open();
         List<String> proxyInstance = reference.getProxyInstance();
         try {
             proxyInstance.contains("Test");
             Assert.fail("Should throw a ServiceUnavailable exception");
         } catch (ServiceUnavailableException e) {
             Assert.assertEquals(testFilter.toString(), e.getServiceFilter());
             Assert.assertEquals(1, e.getTimeout());
             try {
                 Method method = List.class.getMethod("contains", new Class[] { Object.class });
                 Assert.assertEquals(method, e.getMethod());
             } catch (NoSuchMethodException e1) {
                 throw new RuntimeException(e1);
             } catch (SecurityException e1) {
                 throw new RuntimeException(e1);
             }
         }
         reference.close();
     }
 
     @Override
     public void testWaitForService() {
         Reference reference = new Reference(bundleContext, new Class<?>[] { Comparable.class },
                 createTestFilter(), 1);
         try {
             reference.waitForService(1);
             Assert.fail();
         } catch (InterruptedException e) {
             Assert.fail();
         } catch (IllegalStateException e) {
             // Good behavior
         }
         reference.open();
         try {
             Assert.assertFalse(reference.waitForService(1));
         } catch (InterruptedException e) {
             Assert.fail();
         }
 
         Hashtable<String, Object> properties = new Hashtable<String, Object>();
         properties.put("testservice", "true");
 
         @SuppressWarnings("rawtypes")
         ServiceRegistration<Comparable> existingSR = bundleContext.registerService(Comparable.class,
                 Integer.valueOf(1), properties);
 
         try {
             Assert.assertTrue(reference.waitForService(1));
         } catch (InterruptedException e) {
             Assert.fail();
         } finally {
             existingSR.unregister();
         }
 
         reference.close();
     }
 
     @Override
     public void testWarmUp() {
 
         final Reference reference = new Reference(bundleContext, new Class<?>[] { Comparable.class },
                 createTestFilter(), 1);
 
         final AtomicBoolean warmed = new AtomicBoolean(false);
 
         reference.setWarmUpListener(new WarmUpListener() {
 
             @Override
             public void warming() {
                 warmed.set(true);
             }
         });
 
         reference.open();
 
         Assert.assertFalse(warmed.get());
 
         Hashtable<String, Object> properties = new Hashtable<String, Object>();
         properties.put("testservice", "true");
 
         @SuppressWarnings("rawtypes")
         ServiceRegistration<Comparable> existingSR = bundleContext.registerService(Comparable.class,
                 Integer.valueOf(1), properties);
 
         try {
 
             Assert.assertTrue(warmed.get());
 
             try {
                 reference.setWarmUpListener(null);
                 Assert.fail();
             } catch (IllegalStateException e) {
                 // Good behavior
             }
 
             reference.close();
 
             warmed.set(false);
             reference.open();
             Assert.assertTrue(warmed.get());
             reference.close();
 
             reference.setWarmUpListener(null);
             warmed.set(false);
             reference.open();
             Assert.assertFalse(warmed.get());
             reference.close();
 
             // and finally testing if the reference can be used from within the warmup method and does not end up in
             // deadlock.
             warmed.set(false);
             final AtomicReference<Thread> warmingThread = new AtomicReference<Thread>();
             final Object mutex = new Object();
             reference.setWarmUpListener(new WarmUpListener() {
 
                 @Override
                 public void warming() {
                     warmingThread.set(Thread.currentThread());
                     @SuppressWarnings("unchecked")
                     Comparable<Integer> comparable = (Comparable<Integer>) reference.getProxyInstance();
                     comparable.compareTo(1);
                     warmed.set(true);
                     synchronized (mutex) {
                         mutex.notify();
                     }
                 }
             });
             // Opening the reference in a new thread as if there is a deadlock it should be possible to interrupt it.
             new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     reference.open();
                 }
             }).start();
             // Waiting till a timeout to be warmed.
             synchronized (mutex) {
                 if (!warmed.get()) {
                     try {
                         mutex.wait(100);
                     } catch (InterruptedException e) {
                         Assert.fail(e.getMessage());
                     }
                 }
             }
             // If it is not warmed until the timeout a possible deadlock is tested so we interrupt the warming thread.
             if (!warmed.get()) {
                 Thread warmingThreadInstance = warmingThread.get();
                 if (warmingThreadInstance != null) {
                     warmingThread.get().interrupt();
                 }
                 Assert.fail("No warming was done well when reference was called from the warmuplistener scope");
             }
             reference.close();
         } finally {
             existingSR.unregister();
         }
     }
 }
