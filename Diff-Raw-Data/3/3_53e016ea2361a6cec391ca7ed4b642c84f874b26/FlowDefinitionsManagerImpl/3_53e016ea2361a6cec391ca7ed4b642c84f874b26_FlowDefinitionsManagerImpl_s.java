 /**
  * Copyright 2006-8 by Amplafi, Inc.
  */
 package org.amplafi.flow.impl;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.amplafi.flow.FlowTranslatorResolver;
 import org.amplafi.flow.validation.FlowValidationException;
 import org.amplafi.flow.validation.MissingRequiredTracking;
 import org.amplafi.flow.impl.BaseFlowManagement;
 import org.amplafi.flow.FlowDefinitionsManager;
 import org.amplafi.flow.Flow;
 import org.amplafi.flow.FlowManagement;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  *
  *
  */
 public class FlowDefinitionsManagerImpl implements FlowDefinitionsManager {
     private FlowTranslatorResolver flowTranslatorResolver;
     private boolean running;
     private ConcurrentMap<String, Flow> flowDefinitions;
     private Log log;
     public FlowDefinitionsManagerImpl() {
         flowDefinitions = new ConcurrentHashMap<String, Flow>();
     }
 
     /**
      *
      */
     public void initializeService() {
         initFlowDefinitions();
         running = true;
     }
 
     /**
      *
      */
     private void initFlowDefinitions() {
         for(Flow flow: flowDefinitions.values()) {
             getFlowTranslatorResolver().resolveFlow(flow);
         }
     }
     /**
      * @see org.amplafi.flow.FlowDefinitionsManager#addDefinitions(Flow...)
      */
     @Override
     public void addDefinitions(Flow... flows) {
         for(Flow flow: flows) {
             addDefinition(flow.getFlowTypeName(), flow);
         }
     }
     public void addDefinition(String key, Flow flow) {
         if (flow.isInstance()) {
             throw new IllegalStateException( flow+ " not a definition");
         }
         this.flowTranslatorResolver.resolveFlow(flow);
         getFlowDefinitions().put(key, flow);
     }
     /**
      * @see org.amplafi.flow.FlowDefinitionsManager#getFlowDefinition(java.lang.String)
      */
     @Override
     public Flow getFlowDefinition(String flowTypeName) {
         Flow flow = this.getFlowDefinitions().get(flowTypeName);
         if (flow==null) {
             throw new FlowValidationException("flow.definition-not-found", new MissingRequiredTracking(flowTypeName));
         }
         return flow;
     }
 
     /**
      * @see org.amplafi.flow.FlowDefinitionsManager#getFlowDefinitions()
      */
     @Override
     public Map<String, Flow> getFlowDefinitions() {
         return this.flowDefinitions;
     }
 
     public void setFlowDefinitions(Map<String, Flow> flowDefinitions) {
         this.flowDefinitions.clear();
         if ( MapUtils.isNotEmpty(flowDefinitions) ) {
             this.flowDefinitions.putAll(flowDefinitions);
         }
         if ( running) {
             initFlowDefinitions();
         }
     }
     /**
      * @see org.amplafi.flow.FlowDefinitionsManager#getInstanceFromDefinition(java.lang.String)
      */
     @Override
     public Flow getInstanceFromDefinition(String flowTypeName) {
         Flow definition = getFlowDefinition(flowTypeName);
         if (definition == null) {
             throw new IllegalArgumentException(flowTypeName + ": definition does not exist");
         }
         Flow inst = definition.createInstance();
         return inst;
     }
 
     public Log getLog() {
         if ( this.log == null ) {
             this.log = LogFactory.getLog(this.getClass());
         }
         return this.log;
     }
 
     /**
      * @see org.amplafi.flow.FlowDefinitionsManager#getSessionFlowManagement()
      */
     @Override
     public FlowManagement getSessionFlowManagement() {
         BaseFlowManagement baseFlowManagement = new BaseFlowManagement();
         baseFlowManagement.setFlowDefinitionsManager(this);
         baseFlowManagement.setFlowTranslatorResolver(getFlowTranslatorResolver());
         return baseFlowManagement;
     }
 
     /**
      * @see org.amplafi.flow.FlowDefinitionsManager#isFlowDefined(java.lang.String)
      */
     @Override
     public boolean isFlowDefined(String flowTypeName) {
         Flow flow = this.getFlowDefinitions().get(flowTypeName);
         return flow != null;
     }
     public void setFlowTranslatorResolver(FlowTranslatorResolver flowTranslatorResolver) {
         this.flowTranslatorResolver = flowTranslatorResolver;
     }
     public FlowTranslatorResolver getFlowTranslatorResolver() {
         return flowTranslatorResolver;
     }
 }
