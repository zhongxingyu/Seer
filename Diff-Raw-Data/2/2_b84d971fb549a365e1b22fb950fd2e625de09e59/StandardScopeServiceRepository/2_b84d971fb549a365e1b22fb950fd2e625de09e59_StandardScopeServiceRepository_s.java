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
 
 package org.eclipse.virgo.kernel.install.artifact.internal;
 
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.osgi.framework.Constants;
 import org.osgi.framework.Filter;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.InvalidSyntaxException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.StringUtils;
 
 import org.eclipse.virgo.kernel.install.artifact.ScopeServiceRepository;
 import org.eclipse.virgo.util.math.Sets;
 
 /**
  * Tracks the service model for a given named scope. The service model of a named scope defines the set of services that
  * are statically known to be published by bundles in that named scope. Typically, this reflects the metadata derived
  * from Spring DM configuration files.
  * 
  * <p/>
  * 
  * The service model information is used to determine which service lookups are automatically application scoped.
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Threadsafe
  * 
  */
 final class StandardScopeServiceRepository implements ScopeServiceRepository {
 
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
     private final Map<String, List<Service>> scopeServices = new HashMap<String, List<Service>>();
 
     private final Object monitor = new Object();
 
     /** 
      * {@inheritDoc}
      */
     public void recordService(String scopeName, String[] types, Dictionary<String, Object> properties) {
         if (logger.isDebugEnabled()) {
             logger.debug("Adding service to scope '{}' with service types '{}' and properties '{}'", new Object[] { scopeName,
                 StringUtils.arrayToCommaDelimitedString(types), dictionaryToCommaSeparatedString(properties) });
         }
 
         synchronized (this.monitor) {
             if (properties == null) {
                 properties = new Hashtable<String, Object>();
             }
             setStandardProperties(types, properties);
             List<Service> servicesForScope = this.scopeServices.get(scopeName);
             if (servicesForScope == null) {
                 servicesForScope = new ArrayList<Service>();
                 this.scopeServices.put(scopeName, servicesForScope);
             }
             servicesForScope.add(new Service(types, properties));
         }
     }
 
     private static String dictionaryToCommaSeparatedString(Dictionary<String, Object> properties) {
         StringBuffer propsString = new StringBuffer();
         if (properties != null) {
             Enumeration<String> keys = properties.keys();
             for (int i = 0; keys.hasMoreElements(); i++) {
                 if (i > 0) {
                     propsString.append(", ");
                 }
                String key = keys.nextElement().toString();
                 propsString.append(key + "=" + properties.get(key).toString());
             }
         }
         return propsString.toString();
     }
 
     private void setStandardProperties(String[] types, Dictionary<String, Object> properties) {
         if (properties.get(Constants.OBJECTCLASS) == null) {
             properties.put(Constants.OBJECTCLASS, types);
         }
     }
 
     /** 
      * {@inheritDoc}
      */
     public boolean scopeHasMatchingService(String scopeName, String type, String filter) throws InvalidSyntaxException {
         synchronized (this.monitor) {
             List<Service> servicesForScope = this.scopeServices.get(scopeName);
             boolean matches = false;
             if (servicesForScope != null) {
                 Filter f = (filter == null ? null : FrameworkUtil.createFilter(filter));
                 for (Service service : servicesForScope) {
                     if (service.matches(type, f)) {
                         matches = true;
                         break;
                     }
                 }
             }
             return matches;
         }
     }
 
     /** 
      * {@inheritDoc}
      */
     public void clearScope(String scopeName) {
         synchronized (this.monitor) {
             this.scopeServices.remove(scopeName);
 
         }
     }
 
     /** 
      * {@inheritDoc}
      */
     public Set<String> knownScopes() {
         Set<String> scopes = new HashSet<String>();
         synchronized (this.monitor) {
             scopes.addAll(this.scopeServices.keySet());
         }
         return scopes;
     }
 
     private static final class Service {
 
         private final Set<String> types;
 
         private final Dictionary<String, Object> properties;
 
         public Service(String[] types, Dictionary<String, Object> properties) {
             this.types = Sets.asSet(types);
             this.properties = properties;
         }
 
         public boolean matches(String type, Filter filter) {
             if (type == null || this.types.contains(type)) {
                 return filter == null || filter.match(this.properties);
             } else {
                 return false;
             }
         }
     }
 
 }
