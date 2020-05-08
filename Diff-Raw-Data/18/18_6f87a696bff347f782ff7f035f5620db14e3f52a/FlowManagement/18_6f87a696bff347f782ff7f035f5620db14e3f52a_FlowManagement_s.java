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
 package org.amplafi.flow;
 
 import java.net.URI;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 
 
 /**
  * implementation that provides session level FlowManagement.
  * @author Patrick Moore
  *
  */
 public interface FlowManagement {
 
     public static final String USER_INFORMATION = "sessionFlows";
 
     /**
      *  a user may have multiple concurrently active
      * requests.
      *
      * @param lookupKey
      * @return the flow named with this flowId.
      */
     public FlowState getFlowState(String lookupKey);
 
     /**
      *  a user may have multiple concurrently active
      * requests.
      *
      * @param flowTypes
      * @return the first active {@link FlowState} having the given type.
      */
     public FlowState getFirstFlowStateByType(String... flowTypes);
     /**
      * synchronized because a user may have multiple concurrently active
      * requests.
      *
      * @param flowTypes
      * @return the first active {@link FlowState} having the given type.
      */
     public FlowState getFirstFlowStateByType(Collection<String> flowTypes);
     /**
      * a user may have multiple concurrently active
      * requests.
      *
      * @param flowType
      * @return the active {@link FlowState}s having the given type.
      */
     public List<FlowState> getActiveFlowStatesByType(String... flowType);
 
     public FlowState getCurrentFlowState();
 
     /**
      * creates a {@link FlowState}. The properties are set to the initialFlowState.
      *
      * @param flowTypeName
      * @param initialFlowState
      * @param makeNewStateCurrent
      * @return the newly-created FlowState
      */
     public FlowState createFlowState(String flowTypeName, Map<String, String> initialFlowState, boolean makeNewStateCurrent);
 
     public FlowState createFlowState(String flowTypeName, FlowState initialFlowState, boolean makeNewStateCurrent);
 
     /**
      * Starts a flow by name.
      * @param flowTypeName The name of the flow.
      * @param currentFlow Whether to make this the current active flow.
      * @param initialFlowState The initial state of the flow.
      * @param returnToFlow TODO
      * @return the newly-started FlowState
      */
     public FlowState startFlowState(String flowTypeName, boolean currentFlow,
             Map<String, String> initialFlowState, Object returnToFlow);
 
     /**
      * Starts a flow by name.
      * @param flowType
      * @param currentFlow the new flow should be set the current flow.
      * @param propertyRoot
      * @param initialValues used to define the initial values for flow. This is a
      * list of strings. Each string is 'key=value'. if value is the same name as a component
      * that has a 'value' attribute (like TextField components) then the initial value.
      * If value is a container's property then that value is used. Otherwise the value
      * provided is used as a literal.
      * @param returnToFlow
      * @return the newly-started FlowState
      */
     public FlowState startFlowState(String flowType, boolean currentFlow, Object propertyRoot,
             Iterable<String> initialValues, Object returnToFlow);
 
     /**
      * Continue the flow with the given lookup key.
      * @param lookupKey
      * @param currentFlow
      * @param propertyRoot
      * @param initialValues
      * @return the resulting FlowState may not be the same as the FlowState corresponding to the passed lookupKey. This
      * happens if the lookupKey'ed flow completes.
      */
     public FlowState continueFlowState(String lookupKey, boolean currentFlow, Object propertyRoot,
             Iterable<String> initialValues);
     /**
      * Continue the flow with the given lookup key.
      * @param lookupKey
      * @param currentFlow
      * @param initialFlowState
      * @return the resulting FlowState may not be the same as the FlowState corresponding to the passed lookupKey. This
      * happens if the lookupKey'ed flow completes.
      */
     public FlowState continueFlowState(String lookupKey, boolean currentFlow,
             Map<String, String> initialFlowState);
     /**
      * the flows that the current session has active.
      *
      * @return list of active {@link FlowState}s referenced by this FlowManagement object.
      */
     public List<FlowState> getFlowStates();
 
     /**
      * drop the indicated flow. This is not a canceling of the flow
      * as a flow is dropped after it has completed.
      * @param flow
      * @return the page name to land on.
      */
     public String dropFlowState(FlowState flow);
 
     public FlowState transitionToFlowState(FlowState flowState);
 
     /**
      * drop the indicated flow. This is not canceling the flow.
      * With "Cancel" a flow has a chance to do something in response.
      * A flow is dropped after it has completed, but can also be dropped if
      * the user is stuck.
      * @param lookupKey
      * @return the page name to land on.
      */
     public String dropFlowStateByLookupKey(String lookupKey);
 
     public FlowActivity getCurrentActivity();
 
     public Flow getInstanceFromDefinition(String flowTypeName);
 
     public void registerForCacheClearing();
 
     public Log getLog();
 
     /**
      * @param flowState
      * @param nextFlowState
      * @return true if the current flow changed ( nextFlowState was the current flow)
      */
     public boolean makeAfter(FlowState flowState, FlowState nextFlowState);
 
     /**
      * @param state the {@link FlowState} that should be made the current FlowState.
      * @return the {@link #getCurrentFlowState()}.{@link FlowState#getCurrentPage()}, null if no current flow.
      */
     public String makeCurrent(FlowState state);
 
     public Flow getFlowDefinition(String flowTypeName);
 
     /**
      * @return the current transaction.
      */
     public FlowTx getFlowTx();
 
     /**
      *
      * @return the {@link #getCurrentFlowState()}.{@link FlowState#getCurrentPage()}, null if no current flow.
      */
     public String getCurrentPage();
 
     /**
      * flow has exited. Called by {@link FlowState#finishFlow()}
      * @param flowState
      * @param newFlowActive
      * @return the page that should be rendered next.
      */
     public String completeFlowState(FlowState flowState, boolean newFlowActive);
 
     /**
      * @return the FlowTranslatorResolver
      */
     public FlowTranslatorResolver getFlowTranslatorResolver();
 
     /**
     * @param activity
      */
     public void resolveFlowActivity(FlowActivity activity);
 
     /**
      * look for a global {@link FlowPropertyDefinition} that is not specific to a {@link Flow} or {@link FlowActivity}.
      * @param key
      * @return the global {@link FlowPropertyDefinition}
      */
     public FlowPropertyDefinition getFlowPropertyDefinition(String key);
 
     /**
      * similar to {@link FlowDefinitionsManager#getDefaultHomePage()}. However, because {@link FlowManagement} is a session
      * object the value returned by this method may be customized to the session settings and may not be
      * {@link FlowDefinitionsManager#getDefaultHomePage()}.
      * @return the default home to use when a flow ends and there is no other place to return.
      */
     public URI getDefaultHomePage();
 }
