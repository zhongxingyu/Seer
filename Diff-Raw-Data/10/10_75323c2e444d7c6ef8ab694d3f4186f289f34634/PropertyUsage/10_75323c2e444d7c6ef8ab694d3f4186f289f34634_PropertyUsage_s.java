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
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Describes how a property may be initialized and how visible the property changes are external to flow after the flow terminates.
  * While a flow is in progress no changes are visible to other flows ( except those invoked in a caller/callee relationship )
  *
  * @author patmoore
  */
 public enum PropertyUsage {
     /**
      * ignores outside namespaces. Does not initialize from them nor does is property copied back to the outside namespaces.
      *
      * These properties are considered invisible properties that should not be documented to external users.
      * TODO: should internalState be same as {@link #initialize} ? Maybe PropertyScope value should be used?
      *
      */
     internalState(false, false, false),
     /**
      * read if passed. not created.
      */
     use(false, false, true),
     /**
      * Similar to {@link #use} but property may (optionally) be initialized.
      * parameter is read if passed. No guarantee that the property will have a value upon flow completion.
      */
     io(false, true, true),
     /**
      * Like {@link #io} except the property will be assigned a value if there is none and the created value will be visible externally.
      *
      */
     creates(false, true, true),
     /**
      * Like {@link #use} except the value will not be visible to any later flows.
      * the property can be initialized externally ( using the global namespace - null ). The initialization value will be moved into the flow/activity namespace
      * Any changes the flow makes are not visible after flow completes.
      */
     consume(true, false, true),
 
     /**
      * Like {@link #creates} except that any previous value will be ignored.
      * If the property has a FlowPropertyValueProvider, the FlowPropertyValueProvider will be called on {@link FlowState#initializeFlow()}.
      */
     initialize(false, true, false);
 
     /**
      * clear from global namespaces when copied to local flow state's namespace. Namespaces in question are determined by PropertyScope??
      */
     private final boolean cleanOnInitialization;
     private final boolean copyBackOnFlowSuccess;
     private final boolean externallySettable;
     private List<PropertyUsage> canbeChangedTo;
     static {
         // roughly trying to enforce that can become more strict but not less strict.
         // notice that this some changes take away behavior ( for example, use -> consume removes the property )
         internalState.canbeChangedTo = Arrays.asList();
         use.canbeChangedTo = Arrays.asList(io, creates, consume, initialize);
         io.canbeChangedTo = Arrays.asList(creates, consume, initialize);
         creates.canbeChangedTo = Arrays.asList(initialize);
         consume.canbeChangedTo = Arrays.asList();
         initialize.canbeChangedTo = Arrays.asList();
     }
 
     private PropertyUsage(boolean cleanOnInitialization, boolean copyBackOnExit, boolean externallySettable) {
         this.cleanOnInitialization = cleanOnInitialization;
         this.copyBackOnFlowSuccess = copyBackOnExit;
         this.externallySettable = externallySettable;
     }
 
     public boolean isCleanOnInitialization() {
         return cleanOnInitialization;
     }
 
     /**
      * @return the copyBackOnExit
      */
     public boolean isCopyBackOnFlowSuccess() {
         return copyBackOnFlowSuccess;
     }
 
     /**
      * @return the externallySettable
      */
     public boolean isExternallySettable() {
         return externallySettable;
     }
 
     public boolean isChangeableTo(PropertyUsage other) {
         return other==this || (other != null && canbeChangedTo.contains(other));
     }
 
     public static PropertyUsage survivingPropertyUsage(PropertyUsage current, PropertyUsage other) {
         if ( other == null || other == current) {
             return current;
         } else if ( current == null ) {
             return other;
         } else if ( !other.isChangeableTo(current)) {
             if ( current.isChangeableTo(other)) {
                 return other;
             } else {
                 // no change
                 return current;
             }
         } else if (!current.isChangeableTo(other)) {
             // other can change to current but current can not change to other
             return current;
         } else {
             // both can change to each other - return most restrictive
             return other.canbeChangedTo.size() > current.canbeChangedTo.size()?current:other;
         }
     }
 
 }
