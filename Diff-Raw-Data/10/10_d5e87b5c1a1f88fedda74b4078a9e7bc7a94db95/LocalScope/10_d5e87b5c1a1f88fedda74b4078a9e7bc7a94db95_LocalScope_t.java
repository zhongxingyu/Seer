 /** (C) Copyright 2010 Hal Hildebrand, All Rights Reserved
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  *     
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 package com.hellblazer.slp.local;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentSkipListSet;
 import java.util.concurrent.Executor;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.uuid.NoArgGenerator;
 import com.hellblazer.slp.Filter;
 import com.hellblazer.slp.InvalidSyntaxException;
 import com.hellblazer.slp.ServiceEvent;
 import com.hellblazer.slp.ServiceEvent.EventType;
 import com.hellblazer.slp.ServiceListener;
 import com.hellblazer.slp.ServiceReference;
 import com.hellblazer.slp.ServiceScope;
 import com.hellblazer.slp.ServiceURL;
 
 /**
  * A local service discovery scope that provides only local, in process service
  * discovery. This implementation is mostly here as a reference for implementors
  * of truly distributed implmentations. However, it is not entirely useless as
  * it is useful for unit testing code that relies on the SLP apis and you need a
  * light weight, process based discovery without all the hassle of the
  * distributed implementations.
  * 
  * Also, too, in process, local discovery is still useful even in production.
  * 
  * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
  * 
  */
 public class LocalScope implements ServiceScope {
     private static class ListenerRegistration implements
             Comparable<ListenerRegistration> {
         final ServiceListener listener;
         final Filter          query;
 
         /**
          * @param listener
          * @param fileter
          */
         public ListenerRegistration(ServiceListener listener, Filter filter) {
             this.listener = listener;
             query = filter;
         }
 
         /* (non-Javadoc)
          * @see java.lang.Comparable#compareTo(java.lang.Object)
          */
         @Override
         public int compareTo(ListenerRegistration reg) {
             if (listener.equals(reg.listener)) {
                 return query.compareTo(reg.query);
             } else {
                 return new Integer(listener.hashCode()).compareTo(new Integer(
                                                                               listener.hashCode()));
             }
         }
 
         @Override
         public boolean equals(Object obj) {
             if (this == obj) {
                 return true;
             }
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             ListenerRegistration other = (ListenerRegistration) obj;
             if (listener == null) {
                 if (other.listener != null) {
                     return false;
                 }
             } else if (!listener.equals(other.listener)) {
                 return false;
             }
             if (query == null) {
                 if (other.query != null) {
                     return false;
                 }
             } else if (!query.equals(other.query)) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             final int prime = 31;
             int result = 1;
             result = prime * result
                      + (listener == null ? 0 : listener.hashCode());
             result = prime * result + (query == null ? 0 : query.hashCode());
             return result;
         }
     }
 
     private final static Logger                   log       = LoggerFactory.getLogger(LocalScope.class);
 
     private final Executor                        executor;
     private final Set<ListenerRegistration>       listeners = new ConcurrentSkipListSet<LocalScope.ListenerRegistration>();
     private final Map<UUID, ServiceReferenceImpl> services  = new ConcurrentHashMap<UUID, ServiceReferenceImpl>();
     private final NoArgGenerator                  uuidGenerator;
 
     public LocalScope(Executor execService, NoArgGenerator generator) {
         uuidGenerator = generator;
         executor = execService;
     }
 
     /* (non-Javadoc)
      * @see com.hellblazer.slp.ServiceScope#addServiceListener(com.hellblazer.slp.ServiceListener, java.lang.String)
      */
     @Override
     public void addServiceListener(final ServiceListener listener, String query)
                                                                                 throws InvalidSyntaxException {
         if (log.isTraceEnabled()) {
             log.trace("adding listener: " + listener + " on query: " + query);
         }
         List<ServiceReference> references;
         listeners.add(new ListenerRegistration(listener, new Filter(query)));
         references = getServiceReferences(null, query);
         for (ServiceReference reference : references) {
             final ServiceReference ref = reference;
             executor.execute(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         listener.serviceChanged(new ServiceEvent(
                                                                  EventType.REGISTERED,
                                                                  ref));
                     } catch (Throwable e) {
                         log.error("Error when notifying listener on reference "
                                   + EventType.REGISTERED, e);
                     }
                 }
             });
         }
 
     }
 
     /* (non-Javadoc)
      * @see com.hellblazer.slp.ServiceScope#getServiceReference(java.lang.String)
      */
     @Override
     public ServiceReference getServiceReference(String serviceType)
                                                                    throws InvalidSyntaxException {
         if (serviceType == null) {
             serviceType = "*";
         }
         Filter filter = new Filter("(" + SERVICE_TYPE + "=" + serviceType + ")");
         for (ServiceReference ref : services.values()) {
             if (filter.match(ref)) {
                 return ref;
             }
         }
         return null;
     }
 
     /* (non-Javadoc)
      * @see com.hellblazer.slp.ServiceScope#getServiceReferences(java.lang.String, java.lang.String)
      */
     @Override
     public List<ServiceReference> getServiceReferences(String serviceType,
                                                        String query)
                                                                     throws InvalidSyntaxException {
         if (serviceType == null) {
             serviceType = "*";
         }
        Filter filter;
        if (query == null) {
            filter = new Filter(String.format("(%s=%s)", SERVICE_TYPE,
                                              serviceType));
        } else {
            filter = new Filter(String.format("(&(%s=%s) %s)", SERVICE_TYPE,
                                              serviceType, query));
        }
         ArrayList<ServiceReference> references = new ArrayList<ServiceReference>();
         for (Map.Entry<UUID, ServiceReferenceImpl> entry : services.entrySet()) {
             if (filter.match(entry.getValue())) {
                 references.add(entry.getValue());
             }
         }
         return references;
     }
 
     /* (non-Javadoc)
      * @see com.hellblazer.slp.ServiceScope#register(com.hellblazer.slp.ServiceURL, java.util.Map)
      */
     @Override
     public UUID register(ServiceURL url, Map<String, String> properties) {
         if (url == null) {
             throw new IllegalArgumentException("Service URL cannot be null");
         }
         UUID registration = uuidGenerator.generate();
         if (properties == null) {
             properties = new HashMap<String, String>();
         }
         properties = new HashMap<String, String>(properties);
         properties.put(SERVICE_TYPE, url.getServiceType().toString());
         properties.put(SERVICE_REGISTRAION, registration.toString());
         ServiceReferenceImpl ref = new ServiceReferenceImpl(url, properties,
                                                             registration);
         services.put(registration, ref);
         serviceChanged(ref, EventType.REGISTERED);
         return registration;
 
     }
 
     /* (non-Javadoc)
      * @see com.hellblazer.slp.ServiceScope#removeServiceListener(com.hellblazer.slp.ServiceListener)
      */
     @Override
     public void removeServiceListener(ServiceListener listener) {
         List<ListenerRegistration> registrations = new ArrayList<LocalScope.ListenerRegistration>();
         for (ListenerRegistration reg : listeners) {
             if (reg.listener == listener) {
                 registrations.add(reg);
             }
         }
         listeners.removeAll(registrations);
     }
 
     /* (non-Javadoc)
      * @see com.hellblazer.slp.ServiceScope#removeServiceListener(com.hellblazer.slp.ServiceListener, java.lang.String)
      */
     @Override
     public void removeServiceListener(ServiceListener listener, String query)
                                                                              throws InvalidSyntaxException {
         listeners.remove(new ListenerRegistration(listener, new Filter(query)));
     }
 
     /* (non-Javadoc)
      * @see com.hellblazer.slp.ServiceScope#setProperties(java.util.UUID, java.util.Map)
      */
     @Override
     public void setProperties(UUID serviceRegistration,
                               Map<String, String> properties) {
         ServiceReferenceImpl ref = services.get(serviceRegistration);
         if (ref == null) {
             if (log.isTraceEnabled()) {
                 log.trace(String.format("No service registered for %s",
                                         serviceRegistration));
             }
             return;
         }
         properties = new HashMap<String, String>(properties);
         properties.put(SERVICE_TYPE, ref.currentProperties().get(SERVICE_TYPE));
         ref.setProperties(properties);
         serviceChanged(ref, EventType.MODIFIED);
     }
 
     /* (non-Javadoc)
      * @see com.hellblazer.slp.ServiceScope#unregister(java.util.UUID)
      */
     @Override
     public void unregister(UUID serviceRegistration) {
         ServiceReference ref = services.remove(serviceRegistration);
         if (ref != null) {
             serviceChanged(ref, EventType.UNREGISTERED);
         } else {
             if (log.isTraceEnabled()) {
                 log.trace(String.format("No service registered for %s",
                                         serviceRegistration));
             }
         }
     }
 
     protected void serviceChanged(final ServiceReference reference,
                                   final EventType type) {
         executor.execute(new Runnable() {
             @Override
             public void run() {
                 for (ListenerRegistration reg : listeners) {
                     if (reg.query.match(reference)) {
                         final ServiceListener listener = reg.listener;
                         executor.execute(new Runnable() {
                             @Override
                             public void run() {
                                 try {
                                     listener.serviceChanged(new ServiceEvent(
                                                                              type,
                                                                              reference));
                                 } catch (Throwable e) {
                                     log.error(String.format("Error when notifying listener %s on reference %s type %s",
                                                             listener,
                                                             reference, type), e);
                                 }
                             }
                         });
                     }
                 }
             }
         });
     }
 }
