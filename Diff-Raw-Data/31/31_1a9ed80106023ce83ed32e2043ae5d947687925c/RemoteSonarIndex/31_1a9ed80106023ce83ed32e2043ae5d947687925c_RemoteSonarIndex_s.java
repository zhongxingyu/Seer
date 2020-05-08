 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2010 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.ide.wsclient;
 
 import org.sonar.ide.api.SourceCode;
 import org.sonar.ide.api.SourceCodeDiffEngine;
 import org.sonar.ide.api.SourceCodeSearchEngine;
 import org.sonar.ide.client.ExtendedHttpClient3Connector;
 import org.sonar.wsclient.Host;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.services.Metric;
 import org.sonar.wsclient.services.MetricQuery;
 import org.sonar.wsclient.services.Resource;
 import org.sonar.wsclient.services.ResourceQuery;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import java.util.*;
 
 /**
  * EXPERIMENTAL!!!
  * Layer between Sonar IDE and Sonar based on sonar-ws-client :
  * Sonar IDE -> RemoteSonarIndex -> sonar-ws-client -> Sonar
  * 
  * @author Evgeny Mandrikov
  * @since 0.2
  */
 class RemoteSonarIndex implements SourceCodeSearchEngine {
 
   private final Host host;
   private final Sonar sonar;
   private final SourceCodeDiffEngine diffEngine;
   private final Map<String, RemoteSourceCode> cache = new HashMap<String, RemoteSourceCode>();
 
   /**
   * Only for testing purposes.
   */
   protected RemoteSonarIndex(Host host) {
     this(host, null);
   }
 
   public RemoteSonarIndex(Host host, SourceCodeDiffEngine diffEngine) {
     this(host, new Sonar(new ExtendedHttpClient3Connector(host)), diffEngine);
   }
 
   private RemoteSonarIndex(Host host, Sonar sonar, SourceCodeDiffEngine diffEngine) {
     this.sonar = sonar;
     this.diffEngine = diffEngine;
     this.host = host;
   }
 
   /**
    * {@inheritDoc}
    */
   public SourceCode search(String key) {
     //TODO tominm: cache non-existent resources as well
     ResourceQuery query = new ResourceQuery();
     RemoteSourceCode result = cache.get(key);
     Resource resource = null;
     if (result != null && result.obsoleteCheckRequired()) {
       //check if the resource is obsolete
       resource = sonar.find(query.setResourceKeyOrId(key));
       if (result.isObsolete(resource.getDate())) {
         //it is - remove it
         result = null;
         cache.remove(key);
       }
     }
     if (result == null) {
       //resource is either obsolete or never existed
       //query resource if we haven't done it yet:
       if (resource == null) {
         resource = sonar.find(query.setResourceKeyOrId(key));
       }
       //create and cache data object (if it exists at all in Sonar):
       if (resource != null) {
         result = new RemoteSourceCode(key, resource.getDate()).setRemoteSonarIndex(this);
         cache.put(key, result);
       }
     }
     return result;
   }
 
   /**
    * {@inheritDoc}
    */
   public Collection<SourceCode> getProjects() {
     ArrayList<SourceCode> result = Lists.newArrayList();
     for (Resource resource : sonar.findAll(new ResourceQuery())) {
       result.add(new RemoteSourceCode(resource.getKey(), resource.getDate()).setRemoteSonarIndex(this));
     }
     return result;
   }
 
   protected Host getServer() {
     return host;
   }
 
   protected Sonar getSonar() {
     return sonar;
   }
 
   protected SourceCodeDiffEngine getDiffEngine() {
     return diffEngine;
   }
 
   public Map<String, Metric> getMetrics() {
     // TODO Godin: This is not optimal. Would be better to load metrics only once.
     List<Metric> metrics = getSonar().findAll(MetricQuery.all());
     return Maps.uniqueIndex(metrics, new Function<Metric, String>() {
       public String apply(Metric metric) {
         return metric.getKey();
       }
     });
   }
 
 }
