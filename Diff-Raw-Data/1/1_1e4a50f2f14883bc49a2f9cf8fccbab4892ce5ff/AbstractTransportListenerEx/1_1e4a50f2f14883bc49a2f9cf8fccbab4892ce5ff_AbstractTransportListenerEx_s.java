 /*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
 package org.apache.axis2.transport.base;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.axis2.AxisFault;
 import org.apache.axis2.addressing.EndpointReference;
 import org.apache.axis2.context.ConfigurationContext;
 import org.apache.axis2.description.AxisService;
 import org.apache.axis2.description.TransportInDescription;
 
 /**
  * Partial implementation of {@link AbstractTransportListener} with a higher level
  * of abstraction. It maintains the mapping between services and protocol specific
  * endpoints.
  * <p>
  * Note: the intention is to eventually merge the code in this class into
  * {@link AbstractTransportListener}
  * 
  * @param <E> the type of protocol endpoint for this transport
  */
 public abstract class AbstractTransportListenerEx<E extends ProtocolEndpoint>
         extends AbstractTransportListener {
     
     /** A Map of service name to the protocol endpoints */
     private List<E> endpoints = new ArrayList<E>();
 
     @Override
     public void init(ConfigurationContext cfgCtx,
             TransportInDescription transportIn) throws AxisFault {
 
         super.init(cfgCtx, transportIn);
         
         // Create endpoint configured at transport level (if available)
         E endpoint = createEndpoint();
         if (endpoint.loadConfiguration(transportIn)) {
             startEndpoint(endpoint);
             endpoints.add(endpoint);
         }
     }
     
     @Override
     public void destroy() {
         // Explicitly stop all endpoints not predispatched to services. All other endpoints will
         // be stopped by stopListeningForService.
         List<E> endpointsToStop = new ArrayList<E>();
         for (E endpoint : endpoints) {
             if (endpoint.getService() == null) {
                 endpointsToStop.add(endpoint);
             }
         }
         for (E endpoint : endpointsToStop) {
             stopEndpoint(endpoint);
             endpoints.remove(endpoint);
         }
         
         super.destroy();
     }
 
     @Override
     public EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault {
         //Strip out the operation name
         if (serviceName.indexOf('/') != -1) {
             serviceName = serviceName.substring(0, serviceName.indexOf('/'));
         }
         // strip out the endpoint name if present
         if (serviceName.indexOf('.') != -1) {
             serviceName = serviceName.substring(0, serviceName.indexOf('.'));
         }
         for (E endpoint : endpoints) {
             AxisService service = endpoint.getService();
             if (service != null) {
                 if (service.getName().equals(serviceName)) {
                     return endpoint.getEndpointReferences(ip);
                 }
             }
         }
         return null;
     }
 
     public final Collection<E> getEndpoints() {
         return Collections.unmodifiableCollection(endpoints);
     }
 
     protected abstract E createEndpoint();
     
     @Override
     protected final void startListeningForService(AxisService service) throws AxisFault {
         E endpoint = createEndpoint();
         endpoint.init(this, service);
         if (endpoint.loadConfiguration(service)) {
             startEndpoint(endpoint);
             endpoints.add(endpoint);
         } else {
             throw new AxisFault("Service doesn't have configuration information for transport " +
                     getTransportName());
         }
     }
 
     protected abstract void startEndpoint(E endpoint) throws AxisFault;
 
     @Override
     protected final void stopListeningForService(AxisService service) {
         for (E endpoint : endpoints) {
             if (service == endpoint.getService()) {
                 stopEndpoint(endpoint);
                 endpoints.remove(endpoint);
                 return;
             }
         }
         log.error("Unable to stop service : " + service.getName() +
                 " - unable to find the corresponding protocol endpoint");
     }
     
     protected abstract void stopEndpoint(E endpoint);
 }
