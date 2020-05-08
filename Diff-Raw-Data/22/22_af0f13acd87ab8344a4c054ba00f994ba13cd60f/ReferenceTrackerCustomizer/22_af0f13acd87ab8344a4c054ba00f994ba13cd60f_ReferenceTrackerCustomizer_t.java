 package org.everit.osgi.servicereference.core.internal;
 
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
 
import java.util.Arrays;

 import org.everit.osgi.servicereference.core.WarmUpListener;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 
 /**
  * {@link ServiceTrackerCustomizer} that checks the provided interfaces when it is available and adds it to the tracker
  * if all the {@link #requiredInterfaces} are implemented by the service object.
  */
 public class ReferenceTrackerCustomizer implements ServiceTrackerCustomizer<Object, Object> {
 
     /**
      * The interfaces that have to be implemented by the service objects that are tracked.
      */
     private final Class<?>[] requiredInterfaces;
 
     /**
      * The context of the bundle that references the service.
      */
     private final BundleContext bundleContext;
 
     /**
      * The warm up listener that is called when the first service is added by this customizer.
      */
     private WarmUpListener warmUpListener;
 
     /**
      * A flag that shows whether warmup listener has been called or not.
      */
     private boolean warmedUp;
 
     /**
      * Simple object to handle thread safety.
      */
     private volatile Object mutex = new Object();
 
     /**
      * A thread local variable that holds the service object during the warmup listener call.
      */
     static final ThreadLocal<Object> WARM_UP_SERVICE_OBJECT = new ThreadLocal<Object>();
 
     /**
      * Simple constructor that sets the properties of this class.
      * 
      * @param bundleContext
      *            Value of {@link #bundleContext}.
      * @param requiredInterfaces
      *            Value of {@link #requiredInterfaces}.
      * 
      * @throws IllegalArgumentException
      *             if no interface is specified. At least one interface has to be specified as the tracked service will
      *             be proxied and the proxy object will implement the required interfaces.
      */
     public ReferenceTrackerCustomizer(final BundleContext bundleContext, final Class<?>[] requiredInterfaces) {
        this.requiredInterfaces = Arrays.copyOf(requiredInterfaces, requiredInterfaces.length);
         this.bundleContext = bundleContext;
     }
 
     /**
      * Adding a service to the tracker only if all the {@link #requiredInterfaces} are implemented by the service
      * object. <br>
      * <br>
      * {@inheritDoc}
      */
     @Override
     public Object addingService(final ServiceReference<Object> reference) {
         boolean implementsAll = true;
 
         Object service = bundleContext.getService(reference);
         Class<? extends Object> classOfService = service.getClass();
 
         for (int i = 0, n = requiredInterfaces.length; (i < n) && implementsAll; i++) {
             implementsAll = (requiredInterfaces[i].isAssignableFrom(classOfService));
         }
         if (implementsAll) {
             callWarmUpListenerIfNecessary(service);
             return service;
         } else {
             bundleContext.ungetService(reference);
             return null;
         }
     }
 
    private void callWarmUpListenerIfNecessary(final Object service) {
        if (!warmedUp && (warmUpListener != null)) {
            WarmUpListener tmp = warmUpListener;
             boolean callIt = false;
             synchronized (mutex) {
                if (!warmedUp && (tmp != null)) {
                     callIt = true;
                 }
             }
 
             if (callIt) {
                 WARM_UP_SERVICE_OBJECT.set(service);
                 tmp.warming();
                 WARM_UP_SERVICE_OBJECT.remove();
             }
         }
     }
 
     @Override
     public void modifiedService(final ServiceReference<Object> reference, final Object service) {
         // Do nothing
 
     }
 
     @Override
     public void removedService(final ServiceReference<Object> reference, final Object service) {
         bundleContext.ungetService(reference);
     }
 
     /**
      * Resetting this customizer and taking it to it's initial state.
      */
     public void reset() {
         warmedUp = false;
     }
 
     public void setWarmUpListener(final WarmUpListener warmUpListener) {
         this.warmUpListener = warmUpListener;
     }
 
 }
