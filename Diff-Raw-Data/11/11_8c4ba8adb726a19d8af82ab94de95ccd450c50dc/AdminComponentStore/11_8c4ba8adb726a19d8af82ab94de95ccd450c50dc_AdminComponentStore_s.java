 /*
  * SCI-Flex: Flexible Integration of SOA and CEP
  * Copyright (C) 2008, 2009  http://sci-flex.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.sciflex.plugins.synapse.esper.mediators.helpers;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.axis2.AxisFault;
 import org.apache.axis2.engine.AxisConfiguration;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.synapse.MessageContext;
 import org.apache.synapse.registry.Registry;
 
 import org.sciflex.plugins.synapse.esper.core.admin.AdminComponentServiceConstants;
 import org.sciflex.plugins.synapse.esper.core.util.UniquelyIdentifiable;
 import org.sciflex.plugins.synapse.esper.core.util.UniquelyIdentifiableChild;
 import org.sciflex.plugins.synapse.esper.mediators.SynapseEsperMediator;
 import org.sciflex.plugins.synapse.esper.mediators.editors.CEPInstanceEditor;
 import org.sciflex.plugins.synapse.esper.mediators.editors.DynamicEPLEditor;
 import org.sciflex.plugins.synapse.esper.mediators.editors.RegistryKeyEditor;
 import org.sciflex.plugins.synapse.esper.mediators.editors.StaticEPLEditor;
 import org.sciflex.plugins.synapse.esper.mediators.helpers.EPLStatementHelper;
 import org.sciflex.plugins.synapse.esper.mediators.monitors.MediatorActivityMonitor;
 import org.sciflex.plugins.synapse.esper.mediators.monitors.MediatorStatisticsMonitor;
 import org.sciflex.plugins.synapse.esper.mediators.monitors.QueryActivityMonitor;
 
 /**
  * The Admin Component Store is capable of stroring a set of admin
  * components associated with the {@link SynapseEsperMediator}.
  */
 public class AdminComponentStore extends AdminComponentServiceConstants 
     implements InvokableHelper, ComponentStore {
 
     /**
      * Log associated with the Admin Component Store.
      */
     private static final Log log = LogFactory.getLog(AdminComponentStore.class);
 
     /**
      * Map of mediators
      */
     private static final Map ALL_MEDIATOR_MAP = new HashMap<String, Map>();
 
     /**
      * Map of mediator components
      */
     private final Map MEDIATOR_COMPONENT_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Map of Dynamic EPL Editors
      */
     private static final Map DYNAMIC_EPL_EDITOR_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Map of Static EPL Editors
      */
     private static final Map STATIC_EPL_EDITOR_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Map of CEP Instance Editors
      */
     private static final Map CEP_INSTANCE_EDITOR_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Map of Registry Key Editors
      */
     private static final Map REGISTRY_KEY_EDITOR_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Map of Query Activity Monitors
      */
     private static final Map QUERY_ACTIVITY_MONITOR_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Map of Mediator Activity Monitors
      */
     private static final Map MEDIATOR_ACTIVITY_MONITOR_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Map of Mediator Statistics Monitors
      */
     private static final Map MEDIATOR_STATISTICS_MONITOR_MAP = new HashMap<String, UniquelyIdentifiableChild>();
 
     /**
      * Indicates whether the one-time configuration has been done or not.
      */
     private boolean isConfigured = false;
 
     /**
      * Associated {@link EPLStatementHelper} instance
      */
     private EPLStatementHelper eplStatementHelper = null;
 
     /**
      * Associated {@link SynapseEsperMediator} instance
      */
     private SynapseEsperMediator mediator = null;
 
     /**
      * States whether a Dynamic EPL Editor is set.
      */
     private boolean isDynamicEPLEditorSet = false;
 
     /**
      * Dynamic EPL Editor
      */
     private UniquelyIdentifiableChild dee = null;
 
     /**
      * Static EPL Editor
      */
     private UniquelyIdentifiableChild see = null;
 
     /**
      * Registry Key Editor
      */
     private UniquelyIdentifiableChild rke = null;
 
     /**
      * Query Activity Monitors
      */
     private UniquelyIdentifiableChild qam = null;
 
     /**
      * Object used to control access to operation of {@link #configure}.
      * This ensures that only one thread can attempt to configure the 
      * store at a time.
      */
     private Object confLock = new Object();
 
     /**
      * Changes {@link EPLStatementHelper} instance
      *
      * @param eplStatementHelper new {@link EPLStatementHelper} instance
      */
     public synchronized void changeEPLStatementHelper(EPLStatementHelper eplStatementHelper) {
         if (eplStatementHelper == null || this.eplStatementHelper == eplStatementHelper) {
             return;
         }
         log.debug("Changing EPL Statement Helper");
         this.eplStatementHelper = eplStatementHelper;
         if (qam != null) {
             removeUniquelyIdentifiableChild(qam);
         }
         qam = eplStatementHelper.getQueryActivity();
         if (rke != null) {
             removeUniquelyIdentifiableChild(rke);
         }
         rke = new RegistryKeyEditor(eplStatementHelper);
         if (see != null) {
             removeUniquelyIdentifiableChild(see);
         }
         see = new StaticEPLEditor(eplStatementHelper);
         if (dee != null) {
             removeUniquelyIdentifiableChild(dee);
         }
         isDynamicEPLEditorSet = false;
         addUniquelyIdentifiableChild(qam);
         addUniquelyIdentifiableChild(rke);
         addUniquelyIdentifiableChild(see);
         log.info("EPL Statement Helper Changed");
     }
 
     /**
      * Constructor accepting {@link SynapseEsperMediator}.
      * 
      * @param mediator {@link SynapseEsperMediator} instance.
      */
     public AdminComponentStore(SynapseEsperMediator mediator) {
         this.mediator = mediator;
         if (mediator != null) {
             ALL_MEDIATOR_MAP.put(mediator.getUID(), MEDIATOR_COMPONENT_MAP);
         }
         log.debug("Created Admin Component Store");
     }
 
     /**
      * Destroy the references related to this instance.
      */
     public void destroy() {
         if (MEDIATOR_COMPONENT_MAP.values() != null) {
             Iterator components = MEDIATOR_COMPONENT_MAP.values().iterator();
            if (components != null) {
                while (components.hasNext()) {
                    removeUniquelyIdentifiableChild(
                        (UniquelyIdentifiableChild) components.next());
                }
             }
         }
         if (mediator != null) {
             ALL_MEDIATOR_MAP.remove(mediator.getUID());
         }
         log.debug("Destroyed Admin Component Store");
     }
 
     /**
      * Adds Uniquely Identifiable Component.
      *
      * @param component Uniquely Identifiable Component.
      */
     public synchronized void addUniquelyIdentifiableChild(UniquelyIdentifiableChild component) {
         if (component == null) {
             return;
         }
         Map map = null;
         if (component.getParentUID() != null) {
             map = (Map) ALL_MEDIATOR_MAP.get(component.getParentUID());
             if (map == null) {
                 return;
             }
         } else if (mediator == null) {
             return;
         } else {
             map = MEDIATOR_COMPONENT_MAP;
             component.setParentUID(mediator.getUID());
         }
         // For the benefit of testing.
         log.info("Adding Admin Component: " + component.getClass().getName());
         if (component instanceof DynamicEPLEditor) {
             DYNAMIC_EPL_EDITOR_MAP.put(component.getUID(), component);
             map.put(DYNAMIC_EPL_EDITOR_MAP_NAME, component);
         } else if (component instanceof StaticEPLEditor) {
             STATIC_EPL_EDITOR_MAP.put(component.getUID(), component);
             map.put(STATIC_EPL_EDITOR_MAP_NAME, component);
         } else if (component instanceof RegistryKeyEditor) {
             REGISTRY_KEY_EDITOR_MAP.put(component.getUID(), component);
             map.put(REGISTRY_KEY_EDITOR_MAP_NAME, component);
         } else if (component instanceof CEPInstanceEditor) {
             CEP_INSTANCE_EDITOR_MAP.put(component.getUID(), component);
             map.put(CEP_INSTANCE_EDITOR_MAP_NAME, component);
         } else if (component instanceof QueryActivityMonitor) {
             QUERY_ACTIVITY_MONITOR_MAP.put(component.getUID(), component);
             map.put(QUERY_ACTIVITY_MONITOR_MAP_NAME, component);
         } else if (component instanceof MediatorActivityMonitor) {
             MEDIATOR_ACTIVITY_MONITOR_MAP.put(component.getUID(), component);
             map.put(MEDIATOR_ACTIVITY_MONITOR_MAP_NAME, component);
         } else if (component instanceof MediatorStatisticsMonitor) {
             MEDIATOR_STATISTICS_MONITOR_MAP.put(component.getUID(), component);
             map.put(MEDIATOR_STATISTICS_MONITOR_MAP_NAME, component);
         }
     }
 
     /**
      * Removes Uniquely Identifiable Component.
      *
      * @param component Uniquely Identifiable Component.
      */
     public synchronized void removeUniquelyIdentifiableChild(UniquelyIdentifiableChild component) {
         if (component == null) {
             return;
         }
         Map map = null;
         if (component.getParentUID() != null) {
             map = (Map) ALL_MEDIATOR_MAP.get(component.getParentUID());
             if (map == null) {
                 return;
             }
         } else {
             // Simply, if the component is not on the map, there is no
             // point removing it. If someone by any chance added a component
             // without a parentUID, he'll never be able to remove it under
             // this scheme. However, as it makes no sense to have such components
             // we are not doing anything wrong here.
             return;
         }
         // For the benefit of testing.
         log.info("Removing Admin Component: " + component.getClass().getName());
         if (component instanceof DynamicEPLEditor) {
             DYNAMIC_EPL_EDITOR_MAP.remove(component.getUID());
             map.remove(DYNAMIC_EPL_EDITOR_MAP_NAME);
         } else if (component instanceof StaticEPLEditor) {
             STATIC_EPL_EDITOR_MAP.remove(component.getUID());
             map.remove(STATIC_EPL_EDITOR_MAP_NAME);
         } else if (component instanceof RegistryKeyEditor) {
             REGISTRY_KEY_EDITOR_MAP.remove(component.getUID());
             map.remove(REGISTRY_KEY_EDITOR_MAP_NAME);
         } else if (component instanceof CEPInstanceEditor) {
             CEP_INSTANCE_EDITOR_MAP.remove(component.getUID());
             map.remove(CEP_INSTANCE_EDITOR_MAP_NAME);
         } else if (component instanceof QueryActivityMonitor) {
             QUERY_ACTIVITY_MONITOR_MAP.remove(component.getUID());
             map.remove(QUERY_ACTIVITY_MONITOR_MAP_NAME);
         } else if (component instanceof MediatorActivityMonitor) {
             MEDIATOR_ACTIVITY_MONITOR_MAP.remove(component.getUID());
             map.remove(MEDIATOR_ACTIVITY_MONITOR_MAP_NAME);
         } else if (component instanceof MediatorStatisticsMonitor) {
             MEDIATOR_STATISTICS_MONITOR_MAP.remove(component.getUID());
             map.remove(MEDIATOR_STATISTICS_MONITOR_MAP_NAME);
         }
     }
 
     /**
      * Configure {@link AdminComponentStore} just once
      * @param mc Synapse Message Context
      */
     private void configure(MessageContext mc) {
         // this function will run only once
         if (isConfigured) {
             return;
         }
         synchronized(confLock) {
             // The second isConfigured check is required because if the store was not configured
             // and two threads attempted configuration with one able to acquire the confLock and
             // the other unable to, the second thread will go into this block and attempt to
             // re-configure the store, which is not harmful but wasteful. This can be a factor
             // that makes the system a slow-starter if we remove this second isConfigured check.
             if (!isConfigured) {
                 log.debug("Configuring Admin Component Store.");
                 isConfigured = true;
                 Map componentRoot = new HashMap<String, Map>();
                 componentRoot.put(ALL_MEDIATOR_MAP_NAME, ALL_MEDIATOR_MAP);
                 componentRoot.put(DYNAMIC_EPL_EDITOR_MAP_NAME, DYNAMIC_EPL_EDITOR_MAP);
                 componentRoot.put(STATIC_EPL_EDITOR_MAP_NAME, STATIC_EPL_EDITOR_MAP);
                 componentRoot.put(CEP_INSTANCE_EDITOR_MAP_NAME, CEP_INSTANCE_EDITOR_MAP);
                 componentRoot.put(REGISTRY_KEY_EDITOR_MAP_NAME, REGISTRY_KEY_EDITOR_MAP);
                 componentRoot.put(QUERY_ACTIVITY_MONITOR_MAP_NAME, QUERY_ACTIVITY_MONITOR_MAP);
                 componentRoot.put(MEDIATOR_ACTIVITY_MONITOR_MAP_NAME, MEDIATOR_ACTIVITY_MONITOR_MAP);
                 componentRoot.put(MEDIATOR_STATISTICS_MONITOR_MAP_NAME, MEDIATOR_STATISTICS_MONITOR_MAP);
                 doRegister(mc, componentRoot);
                 log.info("Admin Component Store Successfully Configured.");
             }
         }
     }
 
     /**
      * Register {@link AdminComponentStore} once per instance
      * @param mc            Synapse Message Context
      * @param componentRoot Root of Component Store.
      */
     private void doRegister(MessageContext mc, Map componentRoot) {
         log.debug("Registering Admin Component Store.");
         if (mc != null && mc.getConfiguration() != null) {
             AxisConfiguration conf = mc.getConfiguration().getAxisConfiguration();
             if (conf == null) {
                 return;
             }
             try {
                 if (conf.getParameterValue(COMPONENT_ROOT_MAP_NAME) == null) {
                     conf.addParameter(COMPONENT_ROOT_MAP_NAME, componentRoot);
                     log.debug("Admin Component Store Successfully Registered.");
                 } else {
                     log.error("Admin Component Store is already Registered.");
                 }
             } catch (AxisFault e) {
                 log.fatal("Unable to add Admin Components to Server Description: " +
                     e.getMessage());
             }
         }
     }
 
     /**
      * Sets DynamicEPLEditor
      * @param mc Synapse Message Context
      */
     private synchronized void setDynamicEPLEditor(MessageContext mc) {
         if (isDynamicEPLEditorSet) {
             return;
         } else if (mc != null && mc.getConfiguration() != null) {
             log.debug("Adding Dynamic EPL Editor Component");
             dee = new DynamicEPLEditor(eplStatementHelper,
                 mc.getConfiguration().getRegistry());
             addUniquelyIdentifiableChild(dee);
         }
         isDynamicEPLEditorSet = true;
     }
 
 
     /**
      * Invokes Admin Component Store before mediating the current message.
      * @param mc Message Context of the current message.
      */
     public void invoke(MessageContext mc) {
         if (!isConfigured) {
             configure(mc);
         } else if (isDynamicEPLEditorSet) {
             return;
         }
         setDynamicEPLEditor(mc);
     }
 }
