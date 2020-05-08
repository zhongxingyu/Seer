 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 package org.amplafi.flow.impl;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.amplafi.flow.Flow;
 import org.amplafi.flow.FlowActivity;
 import org.amplafi.flow.FlowActivityImplementor;
 import org.amplafi.flow.FlowActivityPhase;
 import org.amplafi.flow.FlowImplementor;
 import org.amplafi.flow.FlowManagement;
 import org.amplafi.flow.FlowManager;
 import org.amplafi.flow.FlowPropertyDefinition;
 import org.amplafi.flow.FlowState;
 import org.amplafi.flow.FlowStateLifecycle;
 import org.amplafi.flow.FlowStateListener;
 import org.amplafi.flow.FlowStepDirection;
 import org.amplafi.flow.FlowTransition;
 import org.amplafi.flow.FlowTranslatorResolver;
 import org.amplafi.flow.FlowTx;
 import org.amplafi.flow.FlowUtils;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
 import org.amplafi.flow.flowproperty.FlowPropertyProvider;
 import org.amplafi.flow.flowproperty.FlowPropertyProviderImplementor;
 import org.amplafi.flow.flowproperty.FlowPropertyValueChangeListener;
 import org.amplafi.flow.flowproperty.PropertyScope;
 import org.amplafi.flow.flowproperty.PropertyUsage;
 import org.amplafi.flow.launcher.ValueFromBindingProvider;
 import org.amplafi.flow.web.PageProvider;
 
 import com.sworddance.beans.ClassResolver;
 import com.sworddance.beans.DefaultClassResolver;
 import com.sworddance.util.ApplicationIllegalArgumentException;
 import com.sworddance.util.perf.LapTimer;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import static org.amplafi.flow.FlowConstants.*;
 import static org.apache.commons.lang.StringUtils.*;
import static com.sworddance.util.CUtilities.*;
 /**
  * A basic implementation of FlowManagement.
  *
  *
  */
 public class BaseFlowManagement implements FlowManagement {
 
     private static final long serialVersionUID = -6759548552816525625L;
 
     protected SessionFlows sessionFlows = new SessionFlows();
 
     private transient FlowManager flowManager;
     private transient FlowTx flowTx;
 
     private transient PageProvider pageProvider;
     private transient ValueFromBindingProvider valueFromBindingProvider;
 
     private transient FlowTranslatorResolver flowTranslatorResolver;
     private transient Set<FlowStateListener> flowStateListeners = Collections.synchronizedSet(new HashSet<FlowStateListener>());
 
     private transient ClassResolver classResolver;
 
     private URI defaultHomePage;
 
     public BaseFlowManagement() {
 
     }
     /**
      * @see org.amplafi.flow.FlowManagement#getFlowStates()
      */
     public List<FlowState> getFlowStates() {
         List<FlowState> collection= new ArrayList<FlowState>();
         CollectionUtils.addAll(collection, sessionFlows.iterator());
         return collection;
     }
 
     /**
      * @see org.amplafi.flow.FlowManagement#dropFlowState(org.amplafi.flow.FlowState)
      */
     public String dropFlowState(FlowState flow) {
         return this.dropFlowStateByLookupKey(flow.getLookupKey());
     }
 
     public String getCurrentPage() {
         FlowState flow = getCurrentFlowState();
         if (flow != null ) {
             return flow.getCurrentPage();
         } else {
             return null;
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowManagement#getCurrentFlowState()
      */
     @SuppressWarnings("unchecked")
     public <FS extends FlowState> FS getCurrentFlowState() {
         return (FS) sessionFlows.getFirst();
     }
 //    /**
 //     * @see org.amplafi.flow.FlowManagement#getCurrentActivity()
 //     */
 //    public FlowActivity getCurrentActivity() {
 //        FlowState current = getCurrentFlowState();
 //        if ( current != null ) {
 //            return current.getCurrentActivity();
 //        } else {
 //            return null;
 //        }
 //    }
 
     /**
      * @see org.amplafi.flow.FlowManagement#getActiveFlowStatesByType(java.lang.String...)
      */
     public synchronized List<FlowState> getActiveFlowStatesByType(String... flowTypes) {
         List<String> types=Arrays.asList(flowTypes);
         ArrayList<FlowState> result = new ArrayList<FlowState>();
         for (FlowState flowState: sessionFlows) {
             if (types.contains(flowState.getFlowTypeName())) {
                 result.add(flowState);
             }
         }
         return result;
     }
 
     /**
      * @see org.amplafi.flow.FlowManagement#getFirstFlowStateByType(java.lang.String...)
      */
     @SuppressWarnings("unchecked")
     public synchronized <FS extends FlowState> FS getFirstFlowStateByType(String... flowTypes) {
         if(flowTypes == null){
             return null;
         }
         List<String> types=Arrays.asList(flowTypes);
         return (FS) getFirstFlowStateByType(types);
     }
 
     @SuppressWarnings("unchecked")
     public <FS extends FlowState> FS getFirstFlowStateByType(Collection<String> types) {
         for (FlowState flowState: sessionFlows) {
             if (types.contains(flowState.getFlowTypeName())) {
                 return (FS)flowState;
             }
         }
         return null;
     }
     public Flow getFlowDefinition(String flowTypeName) {
         return getFlowManager().getFlowDefinition(flowTypeName);
     }
 
     /**
      * @see org.amplafi.flow.FlowManagement#getFlowState(java.lang.String)
      */
     @SuppressWarnings("unchecked")
     public <FS extends FlowState> FS getFlowState(String lookupKey) {
         FS flowState;
         if ( isNotBlank(lookupKey)) {
             flowState = (FS) sessionFlows.get(lookupKey);
         } else {
             flowState = null;
         }
         return flowState;
     }
     /**
      * Override this method to create a custom {@link FlowState} object.
      * @param flowTypeName
      * @param initialFlowState
      * @return the newly created FlowsState
      */
     @SuppressWarnings("unchecked")
     protected <FS extends FlowState> FS makeFlowState(String flowTypeName, Map<String, String> initialFlowState) {
         return (FS) new FlowStateImpl(flowTypeName, this, initialFlowState);
     }
     /**
      * @see org.amplafi.flow.FlowManagement#createFlowState(java.lang.String, java.util.Map, boolean)
      * TODO : sees like method should be renamed.
      */
     @SuppressWarnings("unchecked")
     public synchronized <FS extends FlowState> FS createFlowState(String flowTypeName, Map<String, String> initialFlowState, boolean makeNewStateCurrent) {
         FS flowState = (FS) makeFlowState(flowTypeName, initialFlowState);
         initializeFlowState(flowState);
         if (makeNewStateCurrent || this.sessionFlows.isEmpty()) {
             makeCurrent(flowState);
         } else {
             makeLast(flowState);
         }
         return flowState;
     }
     @SuppressWarnings({ "unchecked" })
     public <FS extends FlowState> FS createFlowState(String flowTypeName, FlowState initialFlowState, Map<String, String> initialValues, boolean makeNewStateCurrent) {
         FS flowState = (FS) createFlowState(flowTypeName, initialFlowState.getFlowValuesMap(), makeNewStateCurrent);
         return flowState;
     }
 
     protected <FS extends FlowState> void initializeFlowState(FS flowState) {
         flowState.initializeFlow();
     }
 
     /**
      * @see org.amplafi.flow.FlowManagement#transitionToFlowState(FlowState, String)
      */
     @SuppressWarnings("unchecked")
     @Override
     public FlowState transitionToFlowState(FlowState flowState, String key) {
         FlowState nextFlowState = null;
         Map<String, FlowTransition> transitions = flowState.getProperty(key, Map.class);
         String finishKey = flowState.getFinishKey();
         if ( isNotEmpty(transitions) && isNotBlank(finishKey)) {
             FlowTransition flowTransition = transitions.get(finishKey);
             if ( flowTransition != null ) {
                 FlowActivityImplementor currentActivity = flowState.getCurrentActivity();
                 String flowType = currentActivity.resolveIndirectReference(flowTransition.getNextFlowType());
                 if (isNotBlank(flowType)) {
                     nextFlowState = this.createFlowState(flowType, flowState.getExportedValuesMap(), false);
                     FlowUtils.INSTANCE.copyMapToFlowState(nextFlowState, flowTransition.getInitialValues());
                 }
             }
         }
         return nextFlowState;
     }
 
     /**
      * @see org.amplafi.flow.FlowManagement#startFlowState(java.lang.String, boolean, java.util.Map, Object)
      */
     @SuppressWarnings("unchecked")
     public <FS extends FlowState> FS startFlowState(String flowTypeName, boolean makeNewStateCurrent, Map<String, String> initialFlowState, Object returnToFlow) {
         initialFlowState = initReturnToFlow(initialFlowState, returnToFlow);
         FS flowState = (FS) createFlowState(flowTypeName, initialFlowState, makeNewStateCurrent);
         /* If you want tapestry stuff injected here...
         // set default page to go to after flow if flow is successful
         if (flowState.getDefaultAfterPage() == null ) {
             IPage currentCyclePage;
             try {
                 currentCyclePage = cycle.getPage();
             } catch(NullPointerException e) {
                 // because of the way cycle is injected - it is impossible to see if the cycle is null.
                 // (normal java checks are looking at the proxy object)
                 currentCyclePage = null;
             }
             if ( currentCyclePage != null) {
                 flowState.setDefaultAfterPage(currentCyclePage.getPageName());
             }
         }
         */
         return (FS) beginFlowState(flowState);
     }
 
     /**
      * @param initialFlowState
      * @param returnToFlow
      * @return initialFlowState if existed otherwise a new map if returnToFlow was legal.
      */
     protected Map<String, String> initReturnToFlow(Map<String, String> initialFlowState, Object returnToFlow) {
         if ( returnToFlow != null) {
             String returnToFlowLookupKey = null;
             if ( returnToFlow instanceof Boolean) {
                 if ( ((Boolean)returnToFlow).booleanValue()) {
                     FlowState currentFlowState = getCurrentFlowState();
                     if ( currentFlowState != null) {
                         returnToFlowLookupKey = currentFlowState.getLookupKey();
                     }
                 }
             } else if ( returnToFlow instanceof FlowState) {
                 returnToFlowLookupKey = ((FlowState)returnToFlow).getLookupKey();
             } else {
                 returnToFlowLookupKey = returnToFlow.toString();
             }
             if ( isNotBlank(returnToFlowLookupKey)) {
                 if ( initialFlowState == null) {
                     initialFlowState = new HashMap<String, String>();
                 }
                 initialFlowState.put(FSRETURN_TO_FLOW, returnToFlowLookupKey);
             }
         }
         return initialFlowState;
     }
 
     /**
      * @see org.amplafi.flow.FlowManagement#continueFlowState(java.lang.String, boolean, java.util.Map)
      */
     @SuppressWarnings("unchecked")
     public <FS extends FlowState> FS continueFlowState(String lookupKey, boolean makeStateCurrent, Map<String, String> initialFlowState) {
         FS flowState = (FS) getFlowState(lookupKey);
         ApplicationIllegalArgumentException.notNull(lookupKey,": no flow with this lookupKey found");
         if (isNotEmpty(initialFlowState)) {
             for(Map.Entry<String, String> entry: initialFlowState.entrySet()) {
                 // HACK this looks bad. At the very least shouldn't FlowUtils.copyState be used
                 // more likely PropertyUsage/PropertyScope
                 ((FlowStateImplementor)flowState).setRawProperty(entry.getKey(), entry.getValue());
             }
         }
         if (makeStateCurrent) {
             makeCurrent(flowState);
         }
         return flowState;
     }
 
     /**
      * call flowState's {@link FlowState#begin()}. If flowState is now completed then see if the flow has transitioned to a new flow.
      *
      * @param flowState
      * @return flowState if flowState has not completed, otherwise the continue flow or the return flow.
      */
     @SuppressWarnings("unchecked")
     protected <FS extends FlowState> FS beginFlowState(FlowState flowState) {
         boolean success = false;
         LapTimer.sLap(flowState, "beginning");
         try {
             flowState.begin();
             success = true;
             if ( flowState.isCompleted()) {
                 FS state = (FS) getNextFlowState(flowState);
                 if ( state != null) {
                     return state;
                 }
             }
             return (FS) flowState;
         } finally {
             if ( !success ) {
                 this.dropFlowState(flowState);
             } else {
                 LapTimer.sLap(flowState, "begun");
             }
         }
     }
 
     /**
      * @param flowState
      * @return
      */
     @SuppressWarnings("unchecked")
     private <FS extends FlowState> FS getNextFlowState(FlowState flowState) {
         String id = flowState.getProperty(FSCONTINUE_WITH_FLOW);
         FS next = (FS) this.getFlowState(id);
         if ( next == null ) {
             id = flowState.getProperty(FSRETURN_TO_FLOW);
             next = (FS) this.getFlowState(id);
         }
         return next;
     }
 
     @Override
     public void wireDependencies(Object object) {
         if (object instanceof FlowPropertyProvider) {
             getFlowTranslatorResolver().resolve((FlowPropertyProvider)object);
         }
         if ( object instanceof FlowPropertyDefinition) {
             // HACK : really should be handling the wiring issue without special casing.
             FlowPropertyDefinition flowPropertyDefinition = (FlowPropertyDefinition) object;
             for(FlowPropertyValueChangeListener flowPropertyValueChangeListener: flowPropertyDefinition.getFlowPropertyValueChangeListeners()) {
                 wireDependencies(flowPropertyValueChangeListener);
             }
             wireDependencies(flowPropertyDefinition.getTranslator());
             wireDependencies(flowPropertyDefinition.getFlowPropertyValuePersister());
             wireDependencies(flowPropertyDefinition.getFlowPropertyValueProvider());
         }
     }
     /**
      * @see org.amplafi.flow.FlowManagement#dropFlowStateByLookupKey(java.lang.String)
      */
     public synchronized String dropFlowStateByLookupKey(String lookupKey) {
         getLog().debug("Dropping flow "+lookupKey);
         boolean successful = false;
         try {
             if ( !sessionFlows.isEmpty()) {
                 FlowStateImplementor fs = sessionFlows.getFirst();
                 boolean first = fs.hasLookupKey(lookupKey);
                 fs = (FlowStateImplementor) sessionFlows.removeByLookupKey(lookupKey);
                 if ( fs != null ) {
                     successful = true;
                     if ( !fs.getFlowStateLifecycle().isTerminalState()) {
                         fs.setFlowLifecycleState(FlowStateLifecycle.canceled);
                     }
 
                     // look for redirect before clearing the flow state
                     // why before cache clearing?
                     URI redirect = fs.getProperty(FSREDIRECT_URL);
                     String returnToFlowId = fs.getProperty(FSRETURN_TO_FLOW);
                     FlowState returnToFlow = getFlowState(returnToFlowId);
                     fs.clearCache();
 
                     if ( !first ) {
                         // dropped flow was not the current flow
                         // so we return the current flow's page.
                         return sessionFlows.getFirst().getCurrentPage();
                     } else if (redirect!=null) {
                         return redirect.toString();
                     } else if ( returnToFlow != null) {
                         return makeCurrent(returnToFlow);
                     } else if ( returnToFlowId != null ) {
                         getLog().warn("FlowState ("+fs.getLookupKey()+ ") trying to return to a flowState ("+returnToFlowId+") that could not be found.");
                     }
                     if ( !sessionFlows.isEmpty() ) {
                         return makeCurrent(sessionFlows.getFirst());
                     } else {
                         // no other flows...
                         return fs.getAfterPage();
                     }
                 }
             }
             return null;
         } finally {
             if ( !successful) {
                 getLog().info("Did not find flow to drop. key="+lookupKey);
             }
         }
     }
 
     /**
      * Called by the {@link FlowState#finishFlow()}
      * @param newFlowActive pass false if you already have flow to run.
      */
     @Override
     public String completeFlowState(FlowState flowState, boolean newFlowActive, FlowStateLifecycle flowStateLifecycle) {
         return dropFlowState(flowState);
     }
     /**
      * @see org.amplafi.flow.FlowManagement#makeCurrent(org.amplafi.flow.FlowState)
      */
     @Override
     public synchronized String makeCurrent(FlowState state) {
         if ( !this.sessionFlows.isEmpty()) {
             FlowStateImplementor oldFirst = this.sessionFlows.getFirst();
             if ( oldFirst == state) {
                 // state is already the first state.
                 return state.getCurrentPage();
             } else {
                 sessionFlows.remove((FlowStateImplementor)state);
                 if ( !oldFirst.isNotCurrentAllowed()) {
                     // the formerly first state is only supposed to be active if it is the first state.
                     // see if it this state is referenced as a return state -- otherwise the oldFirst will need to be dropped.
                     boolean notReferenced = state.isReferencing(oldFirst);
                     if ( !notReferenced ) {
                         for (FlowState flowState: this.sessionFlows) {
                             if( flowState.isReferencing(oldFirst)) {
                                 notReferenced = false;
                                 break;
                             }
                         }
                     }
                     if ( !notReferenced ) {
                         dropFlowState(oldFirst);
                     }
                 } else {
                     oldFirst.clearCache();
                 }
             }
         }
         this.sessionFlows.makeFirst((FlowStateImplementor)state);
         return state.getCurrentPage();
     }
 
     protected void makeLast(FlowState flowState) {
         this.sessionFlows.addLast((FlowStateImplementor)flowState);
     }
     /**
      * @see org.amplafi.flow.FlowManagement#makeAfter(org.amplafi.flow.FlowState, org.amplafi.flow.FlowState)
      */
     @Override
     public boolean makeAfter(FlowState flowState, FlowState nextFlowState) {
         boolean wasFirst = this.sessionFlows.makeAfter((FlowStateImplementor)flowState, (FlowStateImplementor)nextFlowState) == 0;
         if ( wasFirst) {
             makeCurrent(this.sessionFlows.getFirst());
         }
         return wasFirst;
     }
 
     @Override
     public <T> FlowPropertyDefinitionImplementor createFlowPropertyDefinition(FlowPropertyProviderImplementor flowPropertyProvider, String key, Class<T> expected, T sampleValue) {
         Class<? extends T> expectedClass;
         if ( expected == null) {
             if ( this.getClassResolver() != null ) {
                 expectedClass = getClassResolver().getRealClass(sampleValue);
             } else {
                 expectedClass = DefaultClassResolver.INSTANCE.getRealClass(sampleValue);
             }
         } else {
             expectedClass = expected;
         }
         // something to be said for making it requestFlowLocal - because this would give flash persistence for free.
         // but using global allows a property to be set that is really for the next flow to be run.
         FlowPropertyDefinitionImpl propertyDefinition = new FlowPropertyDefinitionImpl(key).initAccess(PropertyScope.global, PropertyUsage.io);
         if (expectedClass != null && !CharSequence.class.isAssignableFrom(expectedClass) ) {
             // auto define property
             // TODO save the definition when the flowState is persisted.
             propertyDefinition.setDataClass(expectedClass);
             if ( sampleValue != null) {
                 // actually going to be setting this property
            	getLog().warn("FlowState: Creating a dynamic FlowDefinition for key="+key+"(expected class="+expected+") might want to check situation. FlowState="+flowPropertyProvider );
                 flowPropertyProvider.addPropertyDefinitions(propertyDefinition);
             }
         }
        // HACK : don't think this should be a 'toString()' maybe flowProvidername ?
         getFlowTranslatorResolver().resolve(flowPropertyProvider.toString(), propertyDefinition);
         return propertyDefinition;
     }
     /**
      * @see org.amplafi.flow.FlowManagement#getInstanceFromDefinition(java.lang.String)
      */
     public FlowImplementor getInstanceFromDefinition(String flowTypeName) {
         return getFlowManager().getInstanceFromDefinition(flowTypeName);
     }
     /**
      * @see org.amplafi.flow.FlowManagement#registerForCacheClearing()
      */
     public void registerForCacheClearing() {
 
     }
 
     public void setFlowTx(FlowTx flowTx) {
         this.flowTx = flowTx;
     }
 
     public FlowTx getFlowTx() {
         return flowTx;
     }
     public Log getLog() {
         return LogFactory.getLog(this.getClass());
     }
 
     public void setFlowManager(FlowManager flowManager) {
         this.flowManager = flowManager;
     }
 
     public FlowManager getFlowManager() {
         return flowManager;
     }
 
     /**
      * @param flowTranslatorResolver the flowTranslatorResolver to set
      */
     public void setFlowTranslatorResolver(FlowTranslatorResolver flowTranslatorResolver) {
         this.flowTranslatorResolver = flowTranslatorResolver;
     }
 
     /**
      * @return the flowTranslatorResolver
      */
     public FlowTranslatorResolver getFlowTranslatorResolver() {
         return flowTranslatorResolver;
     }
 
 
     /**
      * @see org.amplafi.flow.FlowManagement#getFlowPropertyDefinition(java.lang.String)
      */
     @SuppressWarnings("unchecked")
     @Override
     public <T extends FlowPropertyDefinition> T getFlowPropertyDefinition(String key) {
         return (T) this.getFlowTranslatorResolver().getFlowPropertyDefinition(key);
     }
 
 
     /**
      * @see org.amplafi.flow.FlowManagement#getDefaultHomePage()
      */
     @Override
     public URI getDefaultHomePage() {
         return this.defaultHomePage;
     }
     /**
      * @param defaultHomePage the defaultHomePage to set
      */
     public void setDefaultHomePage(URI defaultHomePage) {
         this.defaultHomePage = defaultHomePage;
     }
     /**
      * @see org.amplafi.flow.FlowManagement#addFlowStateListener(org.amplafi.flow.FlowStateListener)
      */
     @Override
     public void addFlowStateListener(FlowStateListener flowStateListener) {
         if ( flowStateListener != null) {
             this.getFlowStateListeners().add(flowStateListener);
         }
     }
 
     public void lifecycleChange(FlowStateImplementor flowState, FlowStateLifecycle previousFlowStateLifecycle) {
         //TODO synchronization issues if new listeners being added.
         // TODO: allow FlowState specific listeners ( for example ExternalServiceConfigurationFlowActivity.finishFlow() )
         // this would make the need to extend FA disappear even more.
         for(FlowStateListener flowStateListener: this.getFlowStateListeners()) {
             flowStateListener.lifecycleChange(flowState, previousFlowStateLifecycle);
         }
     }
 
     public void activityChange(FlowStateImplementor flowState, FlowActivity flowActivity, FlowStepDirection flowStepDirection, FlowActivityPhase flowActivityPhase) {
         //TODO synchronization issues if new listeners being added.
         for(FlowStateListener flowStateListener: this.getFlowStateListeners()) {
             flowStateListener.activityChange(flowState, flowActivity, flowStepDirection, flowActivityPhase);
         }
     }
 
     /**
      * @param pageProvider the pageProvider to set
      */
     public void setPageProvider(PageProvider pageProvider) {
         this.pageProvider = pageProvider;
     }
 
     /**
      * @return the pageProvider
      */
     public PageProvider getPageProvider() {
         return pageProvider;
     }
 
     /**
      * @param flowStateListeners the flowStateListeners to set
      */
     public void setFlowStateListeners(Set<FlowStateListener> flowStateListeners) {
         this.flowStateListeners.clear();
         if ( isNotEmpty(flowStateListeners)) {
             this.flowStateListeners.addAll(flowStateListeners);
         }
     }
 
     /**
      * @return the flowStateListeners
      */
     public Set<FlowStateListener> getFlowStateListeners() {
         return flowStateListeners;
     }
 
     /**
      * @param valueFromBindingProvider the valueFromBindingProvider to set
      */
     public void setValueFromBindingProvider(ValueFromBindingProvider valueFromBindingProvider) {
         this.valueFromBindingProvider = valueFromBindingProvider;
     }
 
     /**
      * @return the valueFromBindingProvider
      */
     public ValueFromBindingProvider getValueFromBindingProvider() {
         return valueFromBindingProvider;
     }
 
     /**
      * @param classResolver the classResolver to set
      */
     public void setClassResolver(ClassResolver classResolver) {
         this.classResolver = classResolver;
     }
 
     /**
      * @return the classResolver
      */
     public ClassResolver getClassResolver() {
         return classResolver;
     }
 
 }
