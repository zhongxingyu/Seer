 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
 
 /**
  * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
  */
 public class JMSTopicDiscoveryComponent implements ResourceDiscoveryComponent
 {
 
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext resourceDiscoveryContext) throws InvalidPluginConfigurationException, Exception
    {
       Set<DiscoveredResourceDetails> set = new HashSet<DiscoveredResourceDetails>();
       JMSComponent context = (JMSComponent) resourceDiscoveryContext.getParentResourceComponent();
       ManagementView managementView = context.getProfileService();
       ManagedOperation operation = ManagementSupport.getOperation(managementView, JMSConstants.Topic.COMPONENT_NAME,
             "getJMSTopics", JMSConstants.Topic.COMPONENT_TYPE);
 
       ArrayValueSupport value = (ArrayValueSupport) operation.invoke();
 
       for (int i = 0; i < value.getLength(); i++)
       {
          SimpleValueSupport queue = (SimpleValueSupport) value.getValue(i);
          ResourceType resourceType = resourceDiscoveryContext.getResourceType();
          String queueName = "jms.topic." + queue.getValue();
          set.add(new DiscoveredResourceDetails(resourceType,
              queueName,
              queueName,
             null,
              "a JMS Topic",
              resourceDiscoveryContext.getDefaultPluginConfiguration(),
              null));
       }
       return set;
    }
}
