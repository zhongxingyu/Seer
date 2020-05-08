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
 
 import org.amplafi.flow.impl.FlowStateImplementor;
 
 /**
  * Listener implementations will get notified as a FlowState moves through the {@link FlowStateLifecycle}.
  *
  * Listeners are good for monitoring / reporting visibility.
  * @author patmoore
  *
  */
 public interface FlowStateLifecycleListener {
     /**
      * Note that listeners are not given veto permission.
      * TODO: Maybe should be ( FlowValidation ? ) - maybe new interface for vetoing?
      *
      * @param flowState
     * @param previousFlowStateLifecycle may be null if not known or there was no previous state.
      */
    void lifecycleChange(FlowStateImplementor flowState, FlowStateLifecycle previousFlowStateLifecycle);
 }
