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
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import org.amplafi.flow.Flow;
 import org.amplafi.flow.FlowActivity;
 import org.amplafi.flow.FlowActivityImplementor;
 import org.amplafi.flow.FlowLifecycleState;
 import org.amplafi.flow.FlowManagement;
 import org.amplafi.flow.FlowPropertyDefinition;
 import org.amplafi.flow.FlowState;
 import org.amplafi.flow.FlowStepDirection;
 import org.amplafi.flow.FlowValidationResult;
 import org.amplafi.flow.FlowValueMapKey;
 import org.amplafi.flow.FlowValuesMap;
 import org.amplafi.flow.PropertyRequired;
 import org.amplafi.flow.PropertyUsage;
 import org.amplafi.flow.validation.FlowValidationException;
 import org.amplafi.flow.validation.ReportAllValidationResult;
 import org.apache.commons.collections.map.MultiKeyMap;
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 
 import com.sworddance.util.RandomKeyGenerator;
 import com.sworddance.util.perf.LapTimer;
 
 import static com.sworddance.util.CUtilities.*;
 import static org.amplafi.flow.FlowConstants.*;
 import static org.amplafi.flow.FlowLifecycleState.*;
 import static org.amplafi.flow.FlowUtils.*;
 import static org.apache.commons.lang.StringUtils.*;
 
 
 /**
  * Application State Object that tracks the current state of a flow. Holds any
  * state information related to a specific flow
  *
  * Each flow state has all the information to run the flow and re-enter it if
  * needed.
  *
  * defines an actively executing flow. Each FlowState has an attached Flow which
  * is the instantiated definition. This copy is made to avoid problems with flow
  * definitions changing while an instance of a flow is active.
  */
 public class FlowStateImpl implements FlowStateImplementor {
 
     private static final long serialVersionUID = -7694935572121566257L;
 
     /**
      * used when displaying the FlowEntryPoint.
      */
     private String activeFlowLabel;
 
     /**
      * unique flow id so that flows can be started, stopped, restarted easily.
      * Important! This key must be immutable as it is the key or part of the key for many for namespace lookups and maps.
      */
     private String lookupKey;
 
     /**
      * key, value pairs that will be used to hold the current state of the flow.
      * The values here should be fairly lightweight.
      */
     protected FlowValuesMap flowValuesMap;
 
     private String flowTypeName;
 
     protected transient Flow flow;
 
     /**
      * index into flow.activities.
      */
     private Integer currentActivityIndex;
 
     /**
      * to be used when advancing flow to a fixed step. Use case: changing
      * flowtype.
      */
     private String currentActivityByName;
 
     /**
      * flowManagement instance that this FlowState is attached to.
      */
     private transient FlowManagement flowManagement;
 
     /**
      * a map of values that the current components have placed here for the
      * FlowActivity and Flow definitions to use for processing this request.
      * This map only contains values up until the completion of the
      * selectActivity() call
      */
     private transient MultiKeyMap cachedValues;
 
     private FlowLifecycleState flowLifecycleState;
 
     public FlowStateImpl() {
     }
 
     public FlowStateImpl(String flowTypeName, FlowManagement sessionFlowManagement,
             Map<String, String> initialFlowState) {
         this(flowTypeName, sessionFlowManagement);
         this.setFlowValuesMap(new DefaultFlowValuesMap(initialFlowState));
     }
 
     public FlowStateImpl(String flowTypeName, FlowManagement sessionFlowManagement) {
         this();
         this.flowLifecycleState = created;
         this.flowTypeName = flowTypeName;
         this.flowManagement = sessionFlowManagement;
         this.lookupKey = createLookupKey();
     }
 
     // HACK : TODO lookupKey injection
     private String createLookupKey() {
         return this.flowTypeName +"_"+ new RandomKeyGenerator(8).nextKey().toString();
     }
     /**
      * for tests only
      */
     @Deprecated
     public void clearLookupKey() {
         this.lookupKey = null;
     }
 
     @Override
     public String begin() {
         // HACK feels hacky ... making assumptions about the value.. NOTE: we do have to reinitialize after a flow has been morphed to another flow. ( new activities/properties )
         if ( !this.isCompleted() && this.getFlowLifecycleState() != initialized ) {
             this.initializeFlow();
         }
         this.setFlowLifecycleState(starting);
         FlowLifecycleState nextFlowLifecycleState = started;
         try {
             // TODO ... should we just be using next()... seems better.
             selectActivity(0, true);
         } catch(RuntimeException e) {
             nextFlowLifecycleState = failed;
             throw e;
         } finally {
             // because may throw flow validation exception
             if ( this.getFlowLifecycleState() == starting) {
                 this.setFlowLifecycleState(nextFlowLifecycleState);
             }
         }
         return getCurrentPage();
     }
 
     /**
      * @see org.amplafi.flow.FlowState#resume()
      */
     @Override
     public String resume() {
         if (!isActive()) {
             return begin();
         } else {
             if (getCurrentActivity().activate(FlowStepDirection.inPlace)) {
                 selectActivity(nextIndex(), true);
             }
             return getCurrentPage();
         }
     }
 
     /**
      *
      * @see org.amplafi.flow.FlowState#initializeFlow()
      */
     public void initializeFlow() {
         this.setFlowLifecycleState(initializing);
         FlowLifecycleState nextFlowLifecycleState = initialized;
         try {
             Map<String, FlowPropertyDefinition> propertyDefinitions = this.getFlow().getPropertyDefinitions();
             if (propertyDefinitions != null) {
                 Collection<FlowPropertyDefinition> flowPropertyDefinitions = propertyDefinitions.values();
                 initializeFlowProperties(null, flowPropertyDefinitions);
             }
 
             int size = this.size();
             for (int i = 0; i < size; i++) {
                 FlowActivity activity = getActivity(i);
                 activity.initializeFlow();
             }
         } catch(RuntimeException e) {
             nextFlowLifecycleState = failed;
             throw e;
         } finally {
             // because may throw flow validation exception
             if ( this.getFlowLifecycleState() == initializing) {
                 this.setFlowLifecycleState(nextFlowLifecycleState);
             }
         }
     }
     public void initializeFlowProperties(FlowActivityImplementor flowActivity, Iterable<FlowPropertyDefinition> flowPropertyDefinitions) {
         for (FlowPropertyDefinition flowPropertyDefinition : flowPropertyDefinitions) {
             initializeFlowProperty(flowActivity, flowPropertyDefinition);
         }
     }
     /**
      * @param flowActivity
      * @param flowPropertyDefinition
      */
     public void initializeFlowProperty(FlowActivityImplementor flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
         // move values from alternateNames to the true name.
         // or just clear out the alternate names of their values.
         List<String> namespaces = flowPropertyDefinition.getNamespaceKeySearchList(this, flowActivity);
         String value = null;
         boolean valueSet = false;
         PropertyUsage propertyUsage = flowPropertyDefinition.getPropertyUsage();
         for(String namespace: namespaces) {
             for (String alternateName : flowPropertyDefinition.getAllNames()) {
                 if ( getFlowValuesMap().containsKey(namespace, alternateName)) {
                     if ( !valueSet ) {
                         value = getRawProperty(namespace, alternateName);
                         valueSet = true;
                     }
                     if ( propertyUsage.isCleanOnInitialization()) {
                         // if clearing then we need to clear all possible matches - so we continue with loop.
                         remove(namespace, alternateName);
                     } else {
                         break;
                     }
                 }
             }
         }
         if ( !valueSet || !propertyUsage.isExternallySettable()) {
             // if property is not set  OR
             // if the property is not allowed to be overridden then
             // initialize it.
             // TODO set valueSet if flowPropertyDefinition.isInitialSet() -- can't check for null because null may be initial value ( see note about PropertyUsage#initialize )
             value = flowPropertyDefinition.getInitial();
             // TODO: what about flowPropertyValueProviders -- but need to handle lazy initialization + and better handling of initializing to null.
            // TODO: should be able to pass FlowState to do a get property operation on a FlowState if there is no FlowActivity.
            if ( value == null && propertyUsage == PropertyUsage.initialize && flowPropertyDefinition.getFlowPropertyValueProvider() != null && flowActivity != null) {
                 value = flowPropertyDefinition.getFlowPropertyValueProvider().get(flowActivity, flowPropertyDefinition);
             }
         }
         String namespace = flowPropertyDefinition.getNamespaceKey(this, flowActivity);
         String currentValue = getRawProperty(namespace, flowPropertyDefinition.getName());
         if (!StringUtils.equals(value, currentValue)) {
             if (!propertyUsage.isExternallySettable() && currentValue != null) {
                 // property cannot be overridden.
                 getLog().info(
                     (flowActivity==null?getFlow().getFlowTypeName():flowActivity.getFullActivityName())
                                 + '.'
                                 + flowPropertyDefinition.getName()
                                 + " cannot be set to '"
                                 + currentValue
                                 + "' external to the flow. It is being force to the initial value of '"
                                 + value + "'");
             }
             setProperty(flowActivity, flowPropertyDefinition, value);
         }
     }
 
 
     /**
      * @see org.amplafi.flow.FlowState#getExportedValuesMap()
      */
     @Override
     public FlowValuesMap getExportedValuesMap() {
         FlowValuesMap valuesMap =exportProperties(false);
         return valuesMap;
     }
     protected FlowValuesMap createFlowValuesMapCopy() {
         return new DefaultFlowValuesMap(getFlowValuesMap());
     }
 
     /**
      * When exporting we start from all the values in the flowValuesMap. This is because the current flow may not be aware of/understand
      * all the properties. This flow is just passing the values on unaltered.
      * @param clearFrom
      * @return
      */
     @SuppressWarnings("unchecked")
     protected FlowValuesMap<FlowValueMapKey, CharSequence> exportProperties(boolean clearFrom) {
         FlowValuesMap exportValueMap = createFlowValuesMapCopy();
         Map<String, FlowPropertyDefinition> propertyDefinitions = this.getFlow().getPropertyDefinitions();
         if (propertyDefinitions != null) {
             Collection<FlowPropertyDefinition> flowPropertyDefinitions = propertyDefinitions.values();
             exportProperties(exportValueMap, flowPropertyDefinitions, null, clearFrom);
         }
 
         int size = this.size();
         for (int i = 0; i < size; i++) {
             FlowActivityImplementor activity = getActivity(i);
             exportProperties(exportValueMap, activity.getPropertyDefinitions().values(), activity, clearFrom);
         }
         // TODO should we clear all non-global namespace values? We have slight leak through when undefined properties are set on a flow.
         return exportValueMap;
     }
     public void exportProperties(FlowValuesMap exportValueMap, Iterable<FlowPropertyDefinition> flowPropertyDefinitions, FlowActivityImplementor flowActivity, boolean clearFrom) {
         for (FlowPropertyDefinition flowPropertyDefinition : flowPropertyDefinitions) {
             exportFlowProperty(exportValueMap, flowPropertyDefinition, flowActivity, clearFrom);
         }
     }
     /**
      * @param exportValueMap map to copy exported values to .
      * @param flowPropertyDefinition
      * @param flowActivity
      * @param flowCompletingExport The FlowState is completing so all clean up actions on the FlowState can be performed as part of this export.
      */
     public void exportFlowProperty(FlowValuesMap exportValueMap, FlowPropertyDefinition flowPropertyDefinition, FlowActivityImplementor flowActivity, boolean flowCompletingExport) {
         // move values from alternateNames to the true name.
         // or just clear out the alternate names of their values.
         List<String> namespaces = flowPropertyDefinition.getNamespaceKeySearchList(this, flowActivity);
         String value = null;
         boolean valueSet = false;
         for(String namespace: namespaces) {
             for (String key : flowPropertyDefinition.getAllNames()) {
                 if ( getFlowValuesMap().containsKey(namespace, key)) {
                     if ( !valueSet ) {
                         // preserve the value from the most precise namespace.
                         value = getRawProperty(namespace, key);
                         valueSet = true;
                     }
                     if ( namespace != null ) {
                         // exclude the global namespace as we clear because global values may not be altered by this property ( propertyUsage.isCopyBackOnFlowSuccess() may be false )
                         exportValueMap.remove(namespace, key);
                         if ( flowCompletingExport) {
                             remove(namespace, key);
                         }
                     }
                 }
             }
         }
         if ( valueSet ) {
             // TODO HANDLE case where we need a to copy from caller to callee. This situation suggests that if PropertyUsage != internalState then the property should be exposed.
             // but need to know the situation: copy to callee or back to caller?
             String namespace = null;
             if ( flowPropertyDefinition.getPropertyUsage().isCopyBackOnFlowSuccess()) {
                 put(namespace, flowPropertyDefinition.getName(), value);
                 exportValueMap.put(namespace, flowPropertyDefinition.getName(), value);
             }
         }
     }
 
     /**
      *
      * @see org.amplafi.flow.FlowState#morphFlow(java.lang.String, java.util.Map)
      */
     @Override
     public String morphFlow(String morphingToFlowTypeName, Map<String, String> initialFlowState) {
         if (isCompleted()) {
             return null;
         }
         if ( this.getFlowTypeName().equals(morphingToFlowTypeName)) {
             return this.getCurrentPage();
         }
         Flow nextFlow = getFlowManagement().getInstanceFromDefinition(morphingToFlowTypeName);
         List<FlowActivityImplementor> originalFAs = getActivities();
         List<FlowActivityImplementor> nextFAs = nextFlow.getActivities();
 
         // make sure FAs in both the flows are in order
         boolean inOrder = areFlowActivitiesInOrder(originalFAs, nextFAs);
         if (!inOrder) {
             throw new IllegalStateException("The FlowActivities in the original and the morphed flow are not in order"
                                             + "\nOriginal Flow FlowActivities : " + originalFAs
                                             + "\nNext Flow FlowActivities : " + nextFAs);
         }
 
         // complete the current FA in the current Flow
         FlowActivityImplementor currentFAInOriginalFlow = getCurrentFlowActivityImplementor();
         // So the current FlowActivity does not try to do validation.
         passivate(false, FlowStepDirection.inPlace);
 
         // morph and initialize to next flow
         setFlowTypeName(morphingToFlowTypeName);
         INSTANCE.copyMapToFlowState(this, initialFlowState);
         this.setCurrentActivityIndex(0);
         // new flow will have different flow activities (and properties ) that needs to be
         initializeFlow();
         begin();
 
         FlowActivityImplementor targetFAInNextFlow = getTargetFAInNextFlow(currentFAInOriginalFlow,
                                                                 originalFAs, nextFAs);
 
         // No common FAs, No need to run nextFlow at all, just return
         if (targetFAInNextFlow != null) {
 
             // move the second flow to appropriate FA
             while (hasNext() && !isEqualTo(getCurrentActivity(), targetFAInNextFlow)) {
                 next();
             }
         }
         return this.getCurrentPage();
     }
 
     private FlowActivityImplementor getTargetFAInNextFlow(FlowActivityImplementor currentFAInOriginalFlow,
             List<FlowActivityImplementor> originalFAs, List<FlowActivityImplementor> nextFAs) {
         FlowActivity flowActivity = this.getActivity(currentFAInOriginalFlow.getActivityName());
         if ( flowActivity != null ) {
             // cool .. exact match on the names.
             return (FlowActivityImplementor) flowActivity;
         }
         // find the first FlowActivity that is after all the flowActivities with the same names
         // as FlowActivities in the previous flow.to find the same approximate spot in the the new flow.
         int newCurrentIndex = this.getCurrentActivityIndex();
         for (int prevIndex = 0; prevIndex < originalFAs.size(); prevIndex++) {
             FlowActivity originalFA = originalFAs.get(prevIndex);
             if ( isEqualTo(originalFA, currentFAInOriginalFlow)) {
                 break;
             }
             for(int nextIndex = newCurrentIndex; nextIndex < nextFAs.size(); nextIndex++) {
                 FlowActivity nextFA = nextFAs.get(nextIndex);
                 if(isEqualTo(originalFA, nextFA)) {
                     newCurrentIndex = nextIndex+1;
                 }
             }
         }
         return (FlowActivityImplementor) this.getActivity(newCurrentIndex);
     }
 
     private boolean isEqualTo(FlowActivity fa1, FlowActivity fa2) {
         return fa1 != null && fa2 != null && fa1.getActivityName().equals(fa2.getActivityName());
     }
 
     private boolean areFlowActivitiesInOrder(List<FlowActivityImplementor> prevFAs, List<FlowActivityImplementor> nextFAs) {
         int lastPrevIndex = -1;
         int lastNextIndex = -1;
         for(int prevIndex = 0; prevIndex < prevFAs.size(); prevIndex++) {
             for(int nextIndex = 0; nextIndex < nextFAs.size(); nextIndex++) {
                 if ( isEqualTo(prevFAs.get(prevIndex), nextFAs.get(nextIndex))) {
                     if ( nextIndex > lastNextIndex && prevIndex > lastPrevIndex ) {
                         lastNextIndex = nextIndex;
                         lastPrevIndex = prevIndex;
                     } else {
                         return false;
                     }
                 }
             }
         }
         return true;
     }
     /**
      *
      * @param verifyValues if true check the flow to validate the {@link PropertyRequired#finish} properties.
      * @return the next flowState 'this' FlowActivities believe should be run.
      */
     protected FlowState finishFlowActivities(boolean verifyValues) {
         FlowValidationResult flowValidationResult = null;
         if (verifyValues) {
             flowValidationResult = getFullFlowValidationResult(PropertyRequired.finish, FlowStepDirection.forward);
         }
         if (flowValidationResult == null || flowValidationResult.isValid()) {
             FlowState currentNextFlowState = getFlowManagement().transitionToFlowState(this, FSFLOW_TRANSITIONS);
             int size = this.size();
             for (int i = 0; i < size; i++) {
                 FlowActivity activity = getActivity(i);
                 FlowState returned = activity.finishFlow(currentNextFlowState);
                 // avoids lose track of FlowState if another FA later in the Flow
                 // definition returns a null. ( this means that a FA cannot override a previous decision ).
                 if (returned != null && currentNextFlowState != returned) {
                     currentNextFlowState = returned;
                 }
             }
             return currentNextFlowState;
         } else {
             throw new FlowValidationException(flowValidationResult);
         }
     }
 
 
     /**
      * @see org.amplafi.flow.FlowState#selectActivity(int, boolean)
      */
     @SuppressWarnings("unchecked")
     @Override
     public <T extends FlowActivity> T selectActivity(int newActivity, boolean verifyValues) {
         if (isCompleted()) {
             return null;
         }
         // used to help determine if the flow is not altering which FlowActivity is current. ( refresh case )
         int originalIndex = getCurrentActivityIndex();
         int next = newActivity;
         FlowActivity currentActivity;
         // if true, currentActivity indicated that it has finished processing and the FlowState should immediately advanced. Used primarily for invisible FlowActivities.
         boolean lastFlowActivityActivateAutoFinished;
         FlowStepDirection flowStepDirection = FlowStepDirection.get(originalIndex, newActivity);
         // based on the flowStepDirection. if true, then there another FlowActivity in the same direction as the current flowActivity
         boolean canContinue;
         do {
             if(this.isActive()) {
                 FlowValidationResult flowValidationResult;
                 // call passivate even if just returning to the current
                 // activity. but not if we are going back to a previous step
                 flowValidationResult = this.passivate(verifyValues, flowStepDirection);
                 if ( !flowValidationResult.isValid()) {
                     getCurrentActivity().activate(flowStepDirection);
                     throw new FlowValidationException(getCurrentActivity(), flowValidationResult);
                 }
             }
             this.setCurrentActivityIndex(next);
 
             currentActivity = getCurrentActivity();
             // TODO should really already be so...
             currentActivity.setActivatable(true);
             switch(flowStepDirection) {
             case forward:
                 next = nextIndex();
                 canContinue = hasNext();
                 break;
             case backward:
                 next = previousIndex();
                 canContinue = hasPrevious();
                 break;
             default:
                 canContinue = false;
                 break;
             }
             lastFlowActivityActivateAutoFinished = currentActivity.activate(flowStepDirection);
         } while (lastFlowActivityActivateAutoFinished && canContinue);
         if (lastFlowActivityActivateAutoFinished && flowStepDirection == FlowStepDirection.forward && !canContinue) {
             // ran out .. time to complete...
             // if chaining FlowStates the actual page may be from another
             // flowState.
             finishFlow();
             currentActivity = null;
         }
         return (T) currentActivity;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#selectVisibleActivity(int)
      */
     @SuppressWarnings("unchecked")
     @Override
     public <T extends FlowActivity> T selectVisibleActivity(int visibleIndex) {
         int index = -1;
         int realIndex = -1;
         for (FlowActivity activity : getActivities()) {
             if (!activity.isInvisible()) {
                 index++;
             }
             realIndex++;
             if (index == visibleIndex) {
                 break;
             }
         }
         return (T) selectActivity(realIndex, false);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#saveChanges()
      */
     @Override
     public void saveChanges() {
         LapTimer.sLap(this.getActiveFlowLabel()," beginning saveChanges()");
         for (int i = 0; i < this.size(); i++) {
             FlowActivity activity = getActivity(i);
             activity.saveChanges();
             LapTimer.sLap(activity.getFullActivityName(), ".saveChanges() completed");
         }
         LapTimer.sLap(this.getActiveFlowLabel()," end saveChanges()");
     }
 
     /**
      * @see org.amplafi.flow.FlowState#finishFlow()
      */
     @Override
     public String finishFlow() {
         return completeFlow(FlowLifecycleState.successful);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#cancelFlow()
      */
     @Override
     public String cancelFlow() {
         return completeFlow(FlowLifecycleState.canceled);
     }
 
     protected String completeFlow(FlowLifecycleState nextFlowLifecycleState) {
         String pageName = null;
         if (!isCompleted()) {
             FlowState continueWithFlow = null;
             boolean verifyValues = nextFlowLifecycleState.isVerifyValues();
             FlowValidationResult flowValidationResult = passivate(verifyValues, FlowStepDirection.inPlace);
 
             if (verifyValues) {
                 if (flowValidationResult.isValid()) {
                     flowValidationResult = getFullFlowValidationResult(PropertyRequired.saveChanges, FlowStepDirection.forward);
                 }
                 if (flowValidationResult == null || flowValidationResult.isValid()) {
                     saveChanges();
                 } else {
                     throw new FlowValidationException(flowValidationResult);
                 }
             }
 
             this.setFlowLifecycleState(nextFlowLifecycleState);
 
             boolean success = false;
             try {
                 // getting continueWithFlow should use FlowLauncher more correctly.
                 continueWithFlow = finishFlowActivities(verifyValues);
                 success = true;
             } finally {
                 this.setCurrentActivityByName(null);
                 clearCache();
                 if (!success) {
                     getFlowManagement().dropFlowState(this);
                 }
             }
             // TODO: THIS block of code should be in the FlowManagement code.
             // OLD note but may still be valid:
             // if continueWithFlow is not null then we do not want start
             // any other flows except continueWithFlow. Autorun flows should
             // start only if we have no flow specified by the finishingActivity. This
             // caused bad UI behavior when we used TransitionFlowActivity to start new
             // flow.
             // make sure that don't get into trouble by a finishFlow that
             // returns the current FlowState.
             if (continueWithFlow == null || continueWithFlow == this) {
                 pageName = getFlowManagement().completeFlowState(this, false);
             } else {
                 // pass on the return flow to the continuation flow.
                 // need to set before starting continuation flow because continuation flow may run to completion.
                 // HACK : seems like the continueFlow should have picked this up automatically
                 String returnToFlow = this.getPropertyAsObject(FSRETURN_TO_FLOW);
                 if ( isNotBlank(returnToFlow)) {
                     continueWithFlow.setPropertyAsObject(FSRETURN_TO_FLOW, returnToFlow);
                 }
                 this.setRawProperty(FSRETURN_TO_FLOW, null);
                 pageName = getFlowManagement().completeFlowState(this, true);
                 if (!continueWithFlow.isActive()) {
                     pageName = continueWithFlow.begin();
                 } else if (!continueWithFlow.isCompleted()) {
                     pageName = continueWithFlow.resume();
                 }
                 String continueWithFlowLookup;
                 if (continueWithFlow.isCompleted()) {
                     // the flow that was continued with immediately finished.
                     // find out what the next continue flow is ... shouldn't this be in a while loop???
                     // or passed over to the FlowManagement code for handling??
                     continueWithFlowLookup = continueWithFlow.getPropertyAsObject(FSCONTINUE_WITH_FLOW);
                 } else {
                     continueWithFlowLookup = continueWithFlow.getLookupKey();
                 }
                 // save back to "this" so that if the current flowState is in turn part of a chain that the callers
                 // will find the correct continue flow state.
                 setRawProperty(FSCONTINUE_WITH_FLOW, continueWithFlowLookup);
 
             }
         }
         // if afterPage is already set then don't lose that information.
         if (pageName != null) {
             setAfterPage(pageName);
         }
         return pageName;
     }
 
     public FlowValidationResult getFullFlowValidationResult(PropertyRequired propertyRequired, FlowStepDirection flowStepDirection) {
         FlowValidationResult flowValidationResult = new ReportAllValidationResult();
         for(FlowActivity flowActivity: this.getActivities()) {
             FlowValidationResult flowActivityValidationResult  = flowActivity.getFlowValidationResult(propertyRequired, flowStepDirection);
             flowValidationResult.merge(flowActivityValidationResult);
         }
         return flowValidationResult;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFinishFlowValidationResult()
      */
     @Override
     public FlowValidationResult getFinishFlowValidationResult() {
         FlowValidationResult flowValidationResult = getCurrentActivityFlowValidationResult();
         if ( flowValidationResult == null || flowValidationResult.isValid()) {
             flowValidationResult = getFullFlowValidationResult(PropertyRequired.finish, FlowStepDirection.forward);
             if ( flowValidationResult == null || flowValidationResult.isValid()) {
                 flowValidationResult = getFullFlowValidationResult(PropertyRequired.saveChanges, FlowStepDirection.forward);
             }
         }
         return flowValidationResult;
     }
 
     /**
      * @param possibleReferencedState
      * @return true if this flowState references possibleReferencedState
      */
     public boolean isReferencing(FlowState possibleReferencedState) {
         if ( this == possibleReferencedState) {
             // can't reference self.
             return false;
         } else {
             String possibleReferencedLookupKey = possibleReferencedState.getLookupKey();
             return possibleReferencedLookupKey.equals(this.getPropertyAsObject(FSCONTINUE_WITH_FLOW))
                 || possibleReferencedLookupKey.equals(this.getPropertyAsObject(FSRETURN_TO_FLOW));
         }
     }
 
     @Override
     public FlowValidationResult passivate(boolean verifyValues, FlowStepDirection flowStepDirection) {
         FlowActivity currentActivity = getCurrentActivity();
         if ( currentActivity != null ) {
             currentActivity.refresh();
             return currentActivity.passivate(verifyValues, flowStepDirection);
         }
         return null;
     }
     /**
      * @see org.amplafi.flow.FlowState#getCurrentPage()
      */
     @Override
     public String getCurrentPage() {
         if (isCompleted()) {
             return this.getAfterPage();
         }
         String pageName = getPropertyAsObject(FSPAGE_NAME);
         if (isBlank(pageName)) {
             if (isActive()) {
                 FlowActivity flowActivity = getCurrentActivity();
                 pageName = flowActivity.getPageName();
             }
             if (isBlank(pageName)) {
                 pageName = this.getFlow().getPageName();
             }
         }
         return pageName;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#setActiveFlowLabel(java.lang.String)
      */
     @Override
     public void setActiveFlowLabel(String activeFlowLabel) {
         this.activeFlowLabel = activeFlowLabel;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getActiveFlowLabel()
      */
     @Override
     public String getActiveFlowLabel() {
         if (this.activeFlowLabel != null) {
             return activeFlowLabel;
         } else {
             return getFlow().getContinueFlowTitle();
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T extends FlowActivity> T getActivity(int activityIndex) {
         T flowActivity = (T) this.getFlow().getActivity(activityIndex);
         return resolveActivity(flowActivity);
     }
 
     /**
      * All accesses to a {@link FlowActivity} should occur through this method.
      * This allows {@link FlowState} implementations to a chance to add in any
      * objects needed to access other parts of the service (database transactions for example).
      * @param <T>
      * @see FlowManagement#resolveFlowActivity(FlowActivity)
      * @param flowActivity
      * @return flowActivity
      */
     public <T extends FlowActivity> T resolveActivity(T flowActivity) {
         getFlowManagement().resolveFlowActivity(flowActivity);
         return flowActivity;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getActivity(java.lang.String)
      */
     @SuppressWarnings("unchecked")
     @Override
     public <T extends FlowActivity> T  getActivity(String activityName) {
         // HACK we need to set up a map.
         if ( activityName != null ) {
             for (FlowActivity flowActivity : this.getFlow().getActivities()) {
                 if (flowActivity.isNamed(activityName)) {
                     return (T) resolveActivity(flowActivity);
                 }
             }
         }
         return null;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getLookupKey()
      */
     @Override
     public String getLookupKey() {
         return lookupKey;
     }
 
     @Override
     public boolean hasLookupKey(Object key) {
         if (key == null) {
             return false;
         } else {
             return getLookupKey().equals(key.toString());
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getCurrentActivity()
      */
     @SuppressWarnings("unchecked")
     @Override
     public  <T extends FlowActivity> T getCurrentActivity() {
         return (T) getActivity(this.getCurrentActivityIndex());
     }
     public FlowActivityImplementor getCurrentFlowActivityImplementor() {
         return (FlowActivityImplementor) getCurrentActivity();
     }
     /**
      * Use selectActivity to change the current activity.
      *
      * @param currentActivity The currentActivity to set.
      */
     private void setCurrentActivityIndex(int currentActivity) {
         if (currentActivity >= 0 && currentActivity < size()) {
             this.currentActivityIndex = currentActivity;
             this.currentActivityByName = getCurrentActivity().getActivityName();
         } else {
             // required to match the iterator definition.
             throw new NoSuchElementException(currentActivity + ": incorrect index for "
                                              + this.activeFlowLabel);
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#setCurrentActivityByName(java.lang.String)
      */
     @Override
     public void setCurrentActivityByName(String currentActivityByName) {
         this.currentActivityByName = currentActivityByName;
         this.currentActivityIndex = null;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getCurrentActivityByName()
      */
     @Override
     public String getCurrentActivityByName() {
         return this.currentActivityByName;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#size()
      */
     @Override
     public int size() {
         if ( !isEmpty(getActivities())) {
             return getActivities().size();
         } else {
             return 0;
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getCurrentActivityIndex()
      */
     @Override
     public int getCurrentActivityIndex() {
         if (currentActivityIndex == null) {
             currentActivityIndex = -1;
             if (isNotBlank(currentActivityByName)) {
                 int i = 0;
                 for (FlowActivity flowActivity : this.getFlow().getActivities()) {
                     if (currentActivityByName.equals(flowActivity.getActivityName())) {
                         currentActivityIndex = i;
                         break;
                     } else {
                         i++;
                     }
                 }
             }
         }
         return currentActivityIndex;
     }
 
     @Override
     public String getRawProperty(String key) {
         return getRawProperty((FlowActivity)null, key);
     }
 
     @Override
     public String getRawProperty(String namespace, String key) {
         return ObjectUtils.toString(getFlowValuesMap().get(namespace, key), null);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T> T getProperty(FlowActivity flowActivity, FlowPropertyDefinition propertyDefinition) {
         T result = (T) getCached(propertyDefinition, flowActivity);
         if ( result == null ) {
             String value = getRawProperty(flowActivity, propertyDefinition);
             result = (T) propertyDefinition.parse(value);
             if (result == null && propertyDefinition.isAutoCreate()) {
                 result =  (T) propertyDefinition.getDefaultObject(flowActivity);
             }
             setCached(propertyDefinition, flowActivity, result);
         }
         return result;
     }
 
     @Override
     public <T> void setProperty(FlowActivity flowActivity, FlowPropertyDefinition propertyDefinition, T value) {
         Object actual;
         String stringValue = null;
 
         if (value instanceof String && propertyDefinition.getDataClass() != String.class) {
             // handle case for when initializing from string values.
             // or some other raw format.
             stringValue = (String) value;
             actual = propertyDefinition.parse(stringValue);
         } else {
             actual = value;
         }
         boolean cacheValue = !(actual instanceof String);
         if (!propertyDefinition.isCacheOnly()) {
             if ( stringValue==null ) {
                 stringValue = propertyDefinition.serialize(actual);
             }
             cacheValue &= this.setRawProperty(flowActivity, propertyDefinition, stringValue);
         }
         if (cacheValue) {
             // HACK FPD can't currently parse AmpEntites to actual objects.
             this.setCached(propertyDefinition, flowActivity, actual);
         }
 
     }
 
     /**
      * @param flowActivity
      * @param flowPropertyDefinition
      * @param value
      * @return true if the value has changed.
      */
     protected boolean setRawProperty(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition, String value) {
         String namespace = flowPropertyDefinition.getNamespaceKey(this, flowActivity);
         return setRawProperty(flowActivity, namespace, flowPropertyDefinition.getName(), value);
     }
 
     protected boolean setRawProperty(FlowActivity flowActivity, String namespace, String key, String value) {
         String oldValue = getRawProperty(namespace, key);
         if (!equalsIgnoreCase(value, oldValue)) {
             FlowActivity activity = getActivity(namespace);
             if ( activity == null) {
                 activity = flowActivity!=null?flowActivity:getCurrentFlowActivityImplementor();
             }
             if ( activity != null) {
                 value = ((FlowActivityImplementor)activity).propertyChange(namespace, key, value, oldValue);
             }
             put(namespace, key, value);
             return true;
         } else {
             return false;
         }
     }
 
 
     @Deprecated
     @Override
     public boolean setRawProperty(String key, String value) {
         FlowPropertyDefinition propertyDefinition = getFlowPropertyDefinitionWithCreate(key, null, value);
         String oldValue = getRawProperty((FlowActivity)null, propertyDefinition);
         if (!equalsIgnoreCase(value, oldValue)) {
             String namespace = propertyDefinition.getNamespaceKey(this, null);
             if (this.isActive()) {
                 value = getCurrentFlowActivityImplementor().propertyChange(namespace, key, value, oldValue);
                 if (value == oldValue) {
                     return false;
                 }
             }
             put(namespace, key, value);
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * @param key
      * @param value
      * @param namespace
      */
     private void put(String namespace, String key, String value) {
         getFlowValuesMap().put(namespace, key, value);
         // in other way wrong cached value returns in next get request
         setCached(namespace, key, null);
     }
     protected void remove(String namespace, String key) {
         getFlowValuesMap().remove(namespace, key);
         // in other way wrong cached value returns in next get request
         setCached(namespace, key, null);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#hasProperty(java.lang.String)
      */
     @Override
     public boolean hasProperty(String key) {
         return getFlowValuesMap().containsKey(key);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getActivities()
      */
     @SuppressWarnings("unchecked")
     @Override
     public List<FlowActivityImplementor> getActivities() {
         return getFlow().getActivities();
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getVisibleActivities()
      */
     @Override
     public List<FlowActivity> getVisibleActivities() {
         return getFlow().getVisibleActivities();
     }
 
     /**
      * @see org.amplafi.flow.FlowState#isFinishable()
      */
     @Override
     public boolean isFinishable() {
         if ( !isCompleted()) {
             FlowActivity currentActivity = this.getCurrentActivity();
             // may not have been started
             if ((currentActivity != null && currentActivity.isFinishingActivity()) || !hasVisibleNext()) {
                 // explicitly able to finish.
                 // or last visible step, which must always be able to finish.
                 return true;
             } else {
                 // all remaining activities claim they have valid data.
                 // this enables a user to go back to a previous step and still finish the flow.
                 // FlowActivities that have content that is required to be viewed (Terms of Service )
                 // should have a state flag so that the flow can not be finished until the ToS is viewed.
                 return getFinishFlowValidationResult().isValid();
             }
         } else {
             // if it is already completed then the flow is not finishable (it already is finished)
             // but may need to indicate that this is not an error as well.
             return false;
         }
     }
 
     @Override
     public void clearCache() {
         if (this.cachedValues != null) {
             this.cachedValues.clear();
             this.cachedValues = null;
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFlowTitle()
      */
     @Override
     public String getFlowTitle() {
         String flowTitle = getPropertyAsObject(FSTITLE_TEXT);
         if (isBlank(flowTitle)) {
             flowTitle = this.getFlow().getFlowTitle();
         }
         if (isBlank(flowTitle)) {
             flowTitle = this.getFlow().getLinkTitle();
         }
         return flowTitle;
     }
 
     @Override
     public synchronized void setCached(String namespace, String key, Object value) {
         if (cachedValues == null) {
             if ( value == null) {
                 // nothing to cache and no cached values.
                 return;
             }
             cachedValues = new MultiKeyMap();
             flowManagement.registerForCacheClearing();
         }
         if ( value == null ) {
             cachedValues.remove(namespace, key);
         } else {
             cachedValues.put(namespace, key, value);
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T> T getCached(String namespace, String key) {
         if(cachedValues != null) {
             T value = (T) cachedValues.get(namespace, key);
             return value;
         } else {
             return null;
         }
     }
 
     public <T> T getCached(FlowPropertyDefinition flowPropertyDefinition, FlowActivity flowActivity) {
         String namespace = flowPropertyDefinition.getNamespaceKey(this, flowActivity);
         return (T) getCached(namespace, flowPropertyDefinition.getName());
     }
     public void setCached(FlowPropertyDefinition flowPropertyDefinition, FlowActivity flowActivity, Object value) {
         String namespace = flowPropertyDefinition.getNamespaceKey(this, flowActivity);
         setCached(namespace, flowPropertyDefinition.getName(), value);
     }
 
     @Override
     public void setFlowManagement(FlowManagement flowManagement) {
         this.flowManagement = flowManagement;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFlowManagement()
      */
     @Override
     public FlowManagement getFlowManagement() {
         return flowManagement;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#setFlowTypeName(java.lang.String)
      */
     @Override
     public synchronized void setFlowTypeName(String flowTypeName) {
         this.flowTypeName = flowTypeName;
         this.flow = null;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFlowTypeName()
      */
     @Override
     public String getFlowTypeName() {
         return flowTypeName;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFlow()
      */
     @Override
     public synchronized Flow getFlow() {
         if (this.flow == null && getFlowTypeName() != null) {
             this.flow = this.getFlowManagement().getInstanceFromDefinition(getFlowTypeName());
             if (this.flow == null) {
                 throw new IllegalArgumentException(getFlowTypeName() + ": no such flow definition");
             }
             this.flow.setFlowState(this);
         }
         return flow;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#iterator()
      */
     @Override
     public Iterator<FlowActivity> iterator() {
         return this;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#next()
      */
     @Override
     public FlowActivity next() {
         if (hasNext()) {
             return this.selectActivity(nextIndex(), true);
         } else {
             finishFlow();
             return null;
         }
 
     }
 
     /**
      * @see org.amplafi.flow.FlowState#previous()
      */
     @Override
     public FlowActivity previous() {
         return this.selectActivity(previousIndex(), false);
     }
 
     /**
      *
      * @see java.util.ListIterator#add(java.lang.Object)
      */
     @Override
     @SuppressWarnings("unused")
     public void add(FlowActivity e) {
         throw new UnsupportedOperationException("cannot add FlowActivities");
     }
 
     /**
      * @see org.amplafi.flow.FlowState#hasVisibleNext()
      */
     @Override
     public boolean hasVisibleNext() {
         if (!hasNext()) {
             return false;
         }
         int count = getActivities().size();
         for (int i = getCurrentActivityIndex() + 1; i < count; i++) {
             if (!getActivity(i).isInvisible()) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#hasVisiblePrevious()
      */
     @Override
     public boolean hasVisiblePrevious() {
         if (!hasPrevious()) {
             return false;
         }
         for (int i = getCurrentActivityIndex() - 1; i >= 0; i--) {
             if (!getActivity(i).isInvisible()) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#hasNext()
      */
     @Override
     public boolean hasNext() {
         if (isCompleted()) {
             return false;
         } else {
 //        } else if ( getCurrentActivity().getFlowValidationResult().isValid()) {
             int count = getActivities().size();
             return this.getCurrentActivityIndex() < count - 1;
 //        } else {
 //            // TODO -- this seems bad because hasNext() seems like it should be constant.
 //            return false;
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#hasPrevious()
      */
     @Override
     public boolean hasPrevious() {
         if (isCompleted()) {
             return false;
         } else {
             return this.getCurrentActivityIndex() > 0;
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#nextIndex()
      */
     @Override
     public int nextIndex() {
         return this.getCurrentActivityIndex() + 1;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#previousIndex()
      */
     @Override
     public int previousIndex() {
         return this.getCurrentActivityIndex() - 1;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#remove()
      */
     @Override
     public void remove() {
         throw new UnsupportedOperationException("TODO: Auto generated");
     }
 
     @Override
     @SuppressWarnings("unused")
     public void set(FlowActivity e) {
         throw new UnsupportedOperationException("TODO: Auto generated");
     }
 
     /**
      * @see org.amplafi.flow.FlowState#isTrue(java.lang.String)
      */
     @Override
     public boolean isTrue(String key) {
         Boolean b = getBoolean(key);
         return b != null && b;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getBoolean(java.lang.String)
      */
     @Override
     public Boolean getBoolean(String key) {
         Boolean value = getPropertyAsObject(key, Boolean.class);
         if (value == null) {
             return null;
         } else {
             return Boolean.valueOf(value);
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getLong(java.lang.String)
      */
     @Override
     @Deprecated
     public Long getLong(String key) {
         return getRawLong(null, key);
     }
     public FlowValidationResult getCurrentActivityFlowValidationResult(PropertyRequired propertyRequired, FlowStepDirection flowStepDirection) {
         FlowActivity currentActivity = this.getCurrentActivity();
         if (currentActivity == null) {
             return null;
         } else {
             if (PropertyRequired.advance == propertyRequired && flowStepDirection == FlowStepDirection.forward) {
                 // TODO temp hack
                 return currentActivity.getFlowValidationResult();
             } else {
                 return currentActivity.getFlowValidationResult(propertyRequired, flowStepDirection);
             }
         }
     }
     /**
      * @see org.amplafi.flow.FlowState#getCurrentActivityFlowValidationResult()
      */
     @Override
     public FlowValidationResult getCurrentActivityFlowValidationResult() {
         return this.getCurrentActivityFlowValidationResult(PropertyRequired.advance, FlowStepDirection.forward);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#isCurrentActivityCompletable()
      */
     @Override
     public boolean isCurrentActivityCompletable() {
         return getCurrentActivityFlowValidationResult().isValid();
     }
 
     @Override
     public Map<String, FlowValidationResult> getFlowValidationResults(PropertyRequired propertyRequired, FlowStepDirection flowStepDirection) {
         Map<String, FlowValidationResult> result = new LinkedHashMap<String, FlowValidationResult>();
         for (FlowActivity activity : this.getActivities()) {
             FlowValidationResult flowValidationResult = activity.getFlowValidationResult(propertyRequired, flowStepDirection);
             if (!flowValidationResult.isValid()) {
                 result.put(activity.getActivityName(), flowValidationResult);
             }
         }
         return result;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#setAfterPage(java.lang.String)
      */
     @Override
     public void setAfterPage(String afterPage) {
         this.setPropertyAsObject(FSAFTER_PAGE, afterPage);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getAfterPage()
      */
     @Override
     public String getAfterPage() {
         // if (this.flowLifecycleState == canceled) {
         // return null;
         // }
         String page = getPropertyAsObject(FSAFTER_PAGE, String.class);
         if (isNotBlank(page)) {
             return page;
         }
         page = getPropertyAsObject(FSDEFAULT_AFTER_PAGE, String.class);
         if (isNotBlank(page)) {
             return page;
         } else {
             return flow == null ? null : flow.getDefaultAfterPage();
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#isUpdatePossible()
      */
     @Override
     public boolean isUpdatePossible() {
         return StringUtils.isNotBlank(getUpdateText());
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getUpdateText()
      */
     @Override
     public String getUpdateText() {
         return this.getPropertyAsObject(FAUPDATE_TEXT, String.class);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#isCancelPossible()
      */
     @Override
     public boolean isCancelPossible() {
         Boolean b = this.getBoolean(FSNO_CANCEL);
         return b == null || !b;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getCancelText()
      */
     @Override
     public String getCancelText() {
         return this.getPropertyAsObject(FSCANCEL_TEXT, String.class);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#setCancelText(java.lang.String)
      */
     @Override
     public void setCancelText(String cancelText) {
         this.setPropertyAsObject(FSCANCEL_TEXT, cancelText);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFinishText()
      */
     @Override
     public String getFinishText() {
         return this.getPropertyAsObject(FSFINISH_TEXT, String.class);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#setFinishText(java.lang.String)
      */
     @Override
     public void setFinishText(String finishText) {
         this.setPropertyAsObject(FSFINISH_TEXT, finishText);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#setFinishType(java.lang.String)
      */
     @Override
     public void setFinishType(String type) {
         this.setPropertyAsObject(FSALT_FINISHED, type);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFinishType()
      */
     @Override
     public String getFinishType() {
         return this.getPropertyAsObject(FSALT_FINISHED, String.class);
     }
 
     @Override
     public void setFlowLifecycleState(FlowLifecycleState flowLifecycleState) {
         checkAllowed(this.flowLifecycleState, flowLifecycleState);
         this.flowLifecycleState = flowLifecycleState;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFlowLifecycleState()
      */
     @Override
     public FlowLifecycleState getFlowLifecycleState() {
         return this.flowLifecycleState;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#isCompleted()
      */
     @Override
     public boolean isCompleted() {
         return this.flowLifecycleState != null && this.flowLifecycleState.isTerminatorState();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T> T getPropertyAsObject(String key) {
         return (T) getPropertyAsObject(key, null);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T> T getPropertyAsObject(String key, Class<T> expected) {
         if (isActive()) {
             return (T) getCurrentActivity().getProperty(key);
         } else {
             FlowPropertyDefinition flowPropertyDefinition = getFlowPropertyDefinitionWithCreate(key, expected, null);
             return (T) getProperty(null, flowPropertyDefinition);
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowState#isActive()
      */
     @Override
     public boolean isActive() {
         // TODO | HACK Seems like we should be looking at FlowLifecycleState here not the index range.
         return this.getCurrentActivityIndex() >= 0 && getCurrentActivityIndex() < size();
     }
 
     @Override
     public <T> FlowPropertyDefinition getFlowPropertyDefinition(String key) {
         FlowPropertyDefinition flowPropertyDefinition = null;
         if ( this.getFlow() != null ) {
             flowPropertyDefinition = this.getFlow().getPropertyDefinition(key);
         }
         if (flowPropertyDefinition == null && this.getFlowManagement() != null) {
             // (may not be assigned to a flowManagement any more -- historical FlowState )
             flowPropertyDefinition = this.getFlowManagement().getFlowPropertyDefinition(key);
         }
         return flowPropertyDefinition;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#setPropertyAsObject(java.lang.String,
      *      java.lang.Object)
      */
     @SuppressWarnings("unchecked")
     @Override
     public <T> void setPropertyAsObject(String key, T value) {
         if (isActive()) {
             getCurrentActivity().setProperty(key, value);
         } else {
             Class<T> expected = (Class<T>) (value == null?null:value.getClass());
             FlowPropertyDefinition flowPropertyDefinition = getFlowPropertyDefinitionWithCreate(key, expected, value);
             setProperty(null, flowPropertyDefinition, value);
         }
     }
 
     /**
      * @param <T>
      * @param key
      * @param value
      * @return
      */
     private <T> FlowPropertyDefinition getFlowPropertyDefinitionWithCreate(String key, Class<T> expected, T value) {
         FlowPropertyDefinition flowPropertyDefinition = getFlowPropertyDefinition(key);
 
         if (flowPropertyDefinition == null) {
             flowPropertyDefinition = getFlowManagement().createFlowPropertyDefinition(getFlow(), key, expected, value);
         }
         return flowPropertyDefinition;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#setDefaultAfterPage(java.lang.String)
      */
     @Override
     public void setDefaultAfterPage(String pageName) {
         this.setPropertyAsObject(FSDEFAULT_AFTER_PAGE, pageName);
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getDefaultAfterPage()
      */
     @Override
     public String getDefaultAfterPage() {
         String property = this.getPropertyAsObject(FSDEFAULT_AFTER_PAGE);
         return property == null ? this.getFlow().getDefaultAfterPage() : property;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#isNotCurrentAllowed()
      */
     @Override
     public boolean isNotCurrentAllowed() {
         return this.getFlow().isNotCurrentAllowed();
     }
 
     /**
      * @see org.amplafi.flow.FlowState#getFlowValuesMap()
      */
     @Override
     public FlowValuesMap getFlowValuesMap() {
         if (this.flowValuesMap == null) {
             this.flowValuesMap = new DefaultFlowValuesMap();
         }
         return this.flowValuesMap;
     }
 
     /**
      * @see org.amplafi.flow.FlowState#setFlowValuesMap(org.amplafi.flow.FlowValuesMap)
      */
     @Override
     public void setFlowValuesMap(FlowValuesMap flowValuesMap) {
         this.flowValuesMap = flowValuesMap;
     }
 
     public Log getLog() {
         // TODO handle historical FlowStates ( no FlowManagement )
         if ( getFlowManagement() == null ) {
             return null;
         } else {
             return getFlowManagement().getLog();
         }
     }
 
     protected String getRawProperty(FlowActivity flowActivity, String key) {
         FlowPropertyDefinition propertyDefinition = getFlowPropertyDefinitionWithCreate(key, null, null);
         return getRawProperty(flowActivity, propertyDefinition);
     }
     @Override
     public String getRawProperty(FlowActivity flowActivity, FlowPropertyDefinition propertyDefinition) {
         String key = propertyDefinition.getName();
         String namespace = propertyDefinition.getNamespaceKey(this, flowActivity);
         String value = getRawProperty(namespace, key);
         return value;
     }
 
     /**
      * Get an object from the database.  If the object does not exist in the database a value of null will be returned.
      *
      * This uses a Hibernate get() call rather than a load() call to prevent us from getting errors if an entity can't be found
      * by the ID passed in.  This exception gets thrown whenever an object field is first accessed which can be well above the data
      * access code.
      * @param <T>
      * @param <K>
      *
      * @param clazz The class of the object to load
      * @param entityId The id of the object to load
      * @return The loaded object or null if it doesn't exist
      */
     public <T, K> T load(Class<? extends T> clazz, K entityId) {
         // We need to use get() to load entities as opposed to load() in case we try to get an entity via an id that doesn't pair
         // to a record in the database.  If we can figure out a way to validate the record actually exists then we can
         // switch this to use load() and not incur the up-front overhead.
         return getFlowManagement().getFlowTx().get(clazz, entityId, true);
     }
 
     /**
      *
      * @see org.amplafi.flow.impl.FlowStateImplementor#getRawLong(org.amplafi.flow.FlowActivity, java.lang.String)
      */
     @Override
     public Long getRawLong(FlowActivity flowActivity, String key) {
         String value = getRawProperty(flowActivity, key);
         if (isNotEmpty(value) && isNumeric(value)) {
             return Long.parseLong(value);
         } else {
             return null;
         }
 
     }
 
     public boolean isApiCall() {
         return isTrue(FSAPI_CALL);
     }
 
     protected void warn(String message) {
         Log log = getLog();
         if ( log != null ) {
             log.warn(message);
         }
     }
 
     @Override
     public String toString() {
         return this.lookupKey + " [type:" + this.flowTypeName + "] flowStateMap="+this.flowValuesMap;
     }
 
 }
