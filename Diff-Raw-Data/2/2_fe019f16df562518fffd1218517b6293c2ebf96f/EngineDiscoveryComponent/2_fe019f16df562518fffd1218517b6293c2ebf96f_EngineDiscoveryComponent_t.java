 /*
  * ModeShape (http://www.modeshape.org)
  * See the COPYRIGHT.txt file distributed with this work for information
  * regarding copyright ownership.  Some portions may be licensed
  * to Red Hat, Inc. under one or more contributor license agreements.
  * See the AUTHORS.txt file in the distribution for a full listing of 
  * individual contributors.
  *
  * ModeShape is free software. Unless otherwise indicated, all code in ModeShape
  * is licensed to you under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  * 
  * ModeShape is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.modeshape.rhq;
 
 import java.util.HashSet;
 import java.util.Set;
 import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
 import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
 import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
 import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
 import org.rhq.modules.plugins.jbossas7.ASConnection;
 import org.rhq.modules.plugins.jbossas7.BaseComponent;
 import org.rhq.modules.plugins.jbossas7.json.Address;
 import org.rhq.modules.plugins.jbossas7.json.Result;
 
 /**
  * Used to discover the ModeShape engine component.
  */
 public class EngineDiscoveryComponent implements ResourceDiscoveryComponent<EngineComponent> {
 
     /**
      * {@inheritDoc}
      * 
      * @see org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent#discoverResources(org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext)
      */
     @Override
     public Set<DiscoveredResourceDetails> discoverResources( final ResourceDiscoveryContext<EngineComponent> context )
         throws InvalidPluginConfigurationException, Exception {
         final Set<DiscoveredResourceDetails> discoveredResources = new HashSet<DiscoveredResourceDetails>();
         final BaseComponent<?> parentComponent = context.getParentResourceComponent();
         final ASConnection connection = parentComponent.getASConnection();
         final Address addr = ModeShapePlugin.createModeShapeAddress();
         final Result result = connection.execute(Operation.Util.createReadResourceOperation(addr, true));
 
         if (result.isSuccess()) {
 
             // TODO: Get version somehow?
             // String version = DmrUtil.stringValue(ModeShapeModuleView.executeManagedOperation(mc, "getVersion", new
             // MetaValue[]{null}));
 
             /**
              * A discovered resource must have a unique key, that must stay the same when the resource is discovered the next time
              */
             final DiscoveredResourceDetails detail = new DiscoveredResourceDetails(context.getResourceType(),
                                                                                    EngineComponent.TYPE,
                                                                                    EngineComponent.DISPLAY_NAME,
                                                                                    ModeShapePlugin.VERSION,
                                                                                    EngineComponent.DESCRIPTION,
                                                                                    context.getDefaultPluginConfiguration(), null);
 
             // Add to return values
             discoveredResources.add(detail);
             ModeShapePlugin.LOG.debug("Discovered ModeShape Engine");
         } else {
            ModeShapePlugin.LOG.debug(PluginI18n.engineNotDiscovered);
         }
 
         return discoveredResources;
     }
 
 }
