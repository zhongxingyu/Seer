 /*
  * Copyright 2009 Red Hat, Inc.
  * Red Hat licenses this file to you under the Apache License, version
  * 2.0 (the "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *    http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.  See the License for the specific language governing
  * permissions and limitations under the License.
  */
 package org.jboss.as.integration.hornetq.jopr;
 
 import org.jboss.as.integration.hornetq.jopr.util.ManagementSupport;
 import org.jboss.deployers.spi.management.ManagementView;
 import org.jboss.managed.api.ManagedOperation;
 import org.jboss.metatype.api.values.ArrayValueSupport;
 import org.jboss.metatype.api.values.SimpleValueSupport;
 import org.rhq.core.domain.resource.ResourceType;
 import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
 import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
 import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
 import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import static org.jboss.as.integration.hornetq.jopr.JMSConstants.ConnectionFactory.COMPONENT_NAME;
 import static org.jboss.as.integration.hornetq.jopr.JMSConstants.ConnectionFactory.COMPONENT_TYPE;
 
 /**
  * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
  *         Created Mar 19, 2010
  */
 public class JMSConnectionFactoryDiscoveryComponent implements ResourceDiscoveryComponent
 {
 
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext resourceDiscoveryContext) throws InvalidPluginConfigurationException, Exception
    {
       Set<DiscoveredResourceDetails> set = new HashSet<DiscoveredResourceDetails>();
       JMSComponent context = (JMSComponent) resourceDiscoveryContext.getParentResourceComponent();
       ManagementView managementView = context.getProfileService();
       ManagedOperation operation = ManagementSupport.getOperation(managementView, COMPONENT_NAME, "getJMSConnectionFactories", COMPONENT_TYPE);
 
       ArrayValueSupport value = (ArrayValueSupport) operation.invoke();
 
       for (int i = 0; i < value.getLength(); i++)
       {
          SimpleValueSupport queue = (SimpleValueSupport) value.getValue(i);
          ResourceType resourceType = resourceDiscoveryContext.getResourceType();
          String queueName = queue.getValue().toString();
          set.add(new DiscoveredResourceDetails(resourceType,
              queueName,
              queueName,
             null,
              "a JMS ConnectionFactory",
              resourceDiscoveryContext.getDefaultPluginConfiguration(),
              null));
       }
       return set;
    }
 }
