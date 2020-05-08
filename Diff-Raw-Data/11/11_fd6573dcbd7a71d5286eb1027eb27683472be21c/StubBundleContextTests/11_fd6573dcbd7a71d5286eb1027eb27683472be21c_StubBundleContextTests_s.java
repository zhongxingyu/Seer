 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.teststubs.osgi.framework;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Test;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.BundleListener;
 import org.osgi.framework.FrameworkEvent;
 import org.osgi.framework.FrameworkListener;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.framework.Version;
 
 import org.eclipse.virgo.teststubs.osgi.support.AbstractFilter;
 
 public class StubBundleContextTests {
 
     private StubBundleContext bundleContext = new StubBundleContext();
 
     @Test
     public void initialState() {
         assertNotNull(this.bundleContext.getBundle());
         assertEquals(0, this.bundleContext.getBundleListeners().size());
         assertEquals(0, this.bundleContext.getFrameworkListeners().size());
         assertEquals(0, this.bundleContext.getServiceListeners().size());
         assertEquals(0, this.bundleContext.getBundles().length);
         assertNull(this.bundleContext.getDataFile("testFilename"));
         assertEquals(0, this.bundleContext.getServiceRegistrations().size());
         assertNull(this.bundleContext.getProperty("testKey"));
     }
 
     @Test
     public void bundleListeners() {
         TestBundleListener listener = new TestBundleListener();
         assertEquals(0, this.bundleContext.getBundleListeners().size());
         this.bundleContext.addBundleListener(listener);
         assertEquals(1, this.bundleContext.getBundleListeners().size());
         this.bundleContext.removeBundleListener(listener);
         assertEquals(0, this.bundleContext.getBundleListeners().size());
     }
 
     @Test
     public void frameworkListeners() {
         TestFrameworkListener listener = new TestFrameworkListener();
         assertEquals(0, this.bundleContext.getFrameworkListeners().size());
         this.bundleContext.addFrameworkListener(listener);
         assertEquals(1, this.bundleContext.getFrameworkListeners().size());
         this.bundleContext.removeFrameworkListener(listener);
         assertEquals(0, this.bundleContext.getFrameworkListeners().size());
     }
 
     @Test
     public void serviceListeners() {
         TestServiceListener listener = new TestServiceListener();
         assertEquals(0, this.bundleContext.getServiceListeners().size());
         this.bundleContext.addServiceListener(listener);
         assertEquals(1, this.bundleContext.getServiceListeners().size());
         this.bundleContext.removeServiceListener(listener);
         assertEquals(0, this.bundleContext.getServiceListeners().size());
     }
 
     @Test
     public void getBundle() {
         assertNotNull(this.bundleContext.getBundle());
     }
 
     @Test
     public void getBundleById() {
         StubBundle bundle = new StubBundle(25L, "testSymbolicName", new Version(1, 0, 0), "testLocation");
         this.bundleContext.addInstalledBundle(bundle);
         assertSame(bundle, this.bundleContext.getBundle(25L));
     }
 
     @Test
     public void getBundles() {
         this.bundleContext.addInstalledBundle(new StubBundle());
         assertEquals(1, this.bundleContext.getBundles().length);
     }
 
     @Test
     public void getDataFile() {
         assertNull(this.bundleContext.getDataFile("testFile"));
         File file = new File("/");
         this.bundleContext.addDataFile("testFile2", file);
         assertSame(file, this.bundleContext.getDataFile("testFile2"));
     }
 
     @Test
     public void installBundle() throws BundleException {
         TestBundleListener listener = new TestBundleListener();
         this.bundleContext.addBundleListener(listener);
         Bundle bundle = this.bundleContext.installBundle("/");
         assertNotNull(bundle);
         assertEquals(2, bundle.getBundleId());
         assertEquals("/", bundle.getSymbolicName());
         assertEquals("/", bundle.getLocation());
         assertEquals(Bundle.INSTALLED, bundle.getState());
         assertTrue(listener.getCalled());
         assertEquals(1, listener.getEvents().length);
         assertEquals(BundleEvent.INSTALLED, listener.getEvents()[0].getType());
     }
 
     @Test
     public void installBundleInputStream() throws BundleException {
         TestBundleListener listener = new TestBundleListener();
         this.bundleContext.addBundleListener(listener);
         Bundle bundle = this.bundleContext.installBundle("/", new TestInputStream());
         assertNotNull(bundle);
         assertEquals(2, bundle.getBundleId());
         assertEquals("/", bundle.getSymbolicName());
         assertEquals("/", bundle.getLocation());
         assertEquals(Bundle.INSTALLED, bundle.getState());
         assertTrue(listener.getCalled());
         assertEquals(1, listener.getEvents().length);
         assertEquals(BundleEvent.INSTALLED, listener.getEvents()[0].getType());
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void registerService() {
         Dictionary properties = new Hashtable();
         properties.put("testKey", "testValue");
         Object service = new Object();
         ServiceRegistration serviceRegistration = this.bundleContext.registerService(Object.class.getName(), service, properties);
         assertNotNull(serviceRegistration);
         assertNotNull(serviceRegistration.getReference());
         assertEquals("testValue", serviceRegistration.getReference().getProperty("testKey"));
         assertSame(service, this.bundleContext.getService(serviceRegistration.getReference()));
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void registerServiceTyped() {
         Dictionary properties = new Hashtable();
         properties.put("testKey", "testValue");
         Object service = new Object();
         ServiceRegistration<Object> serviceRegistration = this.bundleContext.registerService(Object.class, service, properties);
         assertNotNull(serviceRegistration);
         assertNotNull(serviceRegistration.getReference());
         assertEquals("testValue", serviceRegistration.getReference().getProperty("testKey"));
         assertSame(service, this.bundleContext.getService(serviceRegistration.getReference()));
     }
 
 
     @Test
     public void getService() {
         Object service = new Object();
         ServiceRegistration<Object> serviceRegistration = this.bundleContext.registerService(Object.class, service, null);
         assertSame(service, this.bundleContext.getService(serviceRegistration.getReference()));
     }
 
     @Test
     public void getServiceUnregistered() {
         Object service = new Object();
         ServiceRegistration<Object> serviceRegistration = this.bundleContext.registerService(Object.class, service, null);
         ServiceReference<Object> reference = serviceRegistration.getReference();
         serviceRegistration.unregister();
         assertNull(this.bundleContext.getService(reference));
     }
 
     @Test
     public void registerServiceArray() {
         Dictionary<String, String> properties = new Hashtable<String, String>();
         properties.put("testKey", "testValue");
         ServiceRegistration<?> serviceRegistration = this.bundleContext.registerService(
             new String[] { Object.class.getName(), Exception.class.getName() }, new Object(), properties);
         assertNotNull(serviceRegistration);
         assertNotNull(serviceRegistration.getReference());
         assertEquals("testValue", serviceRegistration.getReference().getProperty("testKey"));
     }
 
     @Test
     public void removeRegisteredService() {
         ServiceRegistration<Object> serviceRegistration = this.bundleContext.registerService(Object.class, new Object(), null);
         assertEquals(1, this.bundleContext.getServiceRegistrations().size());
         this.bundleContext.removeRegisteredService(serviceRegistration);
         assertEquals(0, this.bundleContext.getServiceRegistrations().size());
     }
 
     @Test
     public void getProperty() {
         assertNull(this.bundleContext.getProperty("testKey"));
         this.bundleContext.addProperty("testKey", "testValue");
         assertEquals("testValue", this.bundleContext.getProperty("testKey"));
     }
 
     @Test(expected = NullPointerException.class)
     public void createFilterNull() throws InvalidSyntaxException {
         this.bundleContext.createFilter(null);
     }
 
     @Test(expected = InvalidSyntaxException.class)
     public void createFilterMissing() throws InvalidSyntaxException {
         this.bundleContext.createFilter("testFilter");
     }
 
     @Test
     public void createFilter() throws InvalidSyntaxException {
         this.bundleContext.addFilter("testFilter", new TestFilter());
         assertNotNull(this.bundleContext.createFilter("testFilter"));
     }
 
     @Test
     public void ungetService() {
         StubServiceRegistration<Object> serviceRegistration = new StubServiceRegistration<Object>(this.bundleContext);
         StubServiceReference<Object> serviceReference = new StubServiceReference<Object>(serviceRegistration);
         assertTrue(this.bundleContext.ungetService(serviceReference));
         serviceRegistration.unregister();
         assertFalse(this.bundleContext.ungetService(serviceReference));
     }
 
     @Test
     public void getAllServiceReferences() throws InvalidSyntaxException {
         assertNull(this.bundleContext.getAllServiceReferences(null, null));
     }
 
     @Test
     public void getServiceReferencesNullReturn() throws InvalidSyntaxException {
         assertNull(this.bundleContext.getServiceReferences((String)null, null));
     }
 
     @Test
     public void getServiceReferencesNullClassNullFilter() throws InvalidSyntaxException {
         this.bundleContext.registerService(Object.class.getName(), new Object(), null);
         assertEquals(1, this.bundleContext.getServiceReferences((String)null, null).length);
     }
 
     @Test
     public void getServiceReferencesNullFilter() throws InvalidSyntaxException {
         this.bundleContext.registerService(Object.class.getName(), new Object(), null);
         assertEquals(1, this.bundleContext.getServiceReferences(Object.class.getName(), null).length);
     }
 
     @Test
     public void getServiceReferencesWrongClass() throws InvalidSyntaxException {
         this.bundleContext.registerService(Object.class.getName(), new Object(), null);
         assertNull(this.bundleContext.getServiceReferences(Exception.class.getName(), null));
     }
 
     @Test
     public void getServiceReferences() throws InvalidSyntaxException {
         this.bundleContext.addFilter(new TestFilter());
         this.bundleContext.registerService(Object.class.getName(), new Object(), null);
         assertEquals(1, this.bundleContext.getServiceReferences(Object.class.getName(), "testFilter").length);
     }
 
     @Test
     public void getServiceReferencesTyped() throws InvalidSyntaxException {
         this.bundleContext.addFilter(new TestFilter());
         this.bundleContext.registerService(Object.class, new Object(), null);
         assertEquals(1, this.bundleContext.getServiceReferences(Object.class, "testFilter").size());
     }
 
     @Test
     public void getServiceReferenceNoValues() throws InvalidSyntaxException {
         assertNull(this.bundleContext.getServiceReference((String)null));
     }
 
     @Test
     public void getServiceReferenceOneValue() throws InvalidSyntaxException {
         this.bundleContext.registerService(Object.class.getName(), new Object(), null);
         assertNotNull(this.bundleContext.getServiceReference((String)null));
     }
 
     @Test
     public void getServiceReferenceOneValueTyped() throws InvalidSyntaxException {
         this.bundleContext.registerService(Object.class.getName(), new Object(), null);
         assertNotNull(this.bundleContext.getServiceReference(Object.class));
     }
 
 
     @Test
     public void getServiceReferenceTwoValues() throws InvalidSyntaxException {
         this.bundleContext.registerService(Object.class.getName(), new Object(), null);
         this.bundleContext.registerService(Object.class.getName(), new Object(), null);
         assertNotNull(this.bundleContext.getServiceReference((String)null));
     }
 
     private static class TestBundleListener implements BundleListener {
 
         private boolean called = false;
 
         private List<BundleEvent> events = new ArrayList<BundleEvent>();
 
         public void bundleChanged(BundleEvent event) {
             this.called = true;
             this.events.add(event);
         }
 
         public boolean getCalled() {
             return called;
         }
 
         public BundleEvent[] getEvents() {
             return events.toArray(new BundleEvent[events.size()]);
         }
 
     }
 
     private static class TestFrameworkListener implements FrameworkListener {
 
         public void frameworkEvent(FrameworkEvent event) {
             throw new UnsupportedOperationException();
         }
 
     }
 
     private static class TestServiceListener implements ServiceListener {
 
         public void serviceChanged(ServiceEvent event) {
             throw new UnsupportedOperationException();
         }
 
     }
 
     private static class TestInputStream extends InputStream {
 
         @Override
         public int read() throws IOException {
             throw new UnsupportedOperationException();
         }
 
     }
 
     private static class TestFilter extends AbstractFilter {
 
         /**
          * {@inheritDoc}
          */
         public boolean match(ServiceReference<?> reference) {
             return true;
         }
 
         /**
          * {@inheritDoc}
          */
         public boolean match(Dictionary<String, ?> dictionary) {
             throw new UnsupportedOperationException();
         }
 
         /**
          * {@inheritDoc}
          */
         public boolean matchCase(Dictionary<String, ?> dictionary) {
             throw new UnsupportedOperationException();
         }
 
         /**
          * {@inheritDoc}
          */
         public String getFilterString() {
             return "testFilter";
         }
 
         /**
          * {@inheritDoc}
          */
         public boolean matches(Map<String, ?> map) {
             throw new UnsupportedOperationException();
         }
 
     }
 }
