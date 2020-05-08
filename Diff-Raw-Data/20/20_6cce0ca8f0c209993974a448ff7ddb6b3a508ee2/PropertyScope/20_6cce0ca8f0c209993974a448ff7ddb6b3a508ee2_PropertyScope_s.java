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
 package org.amplafi.flow.flowproperty;
 
 import static org.amplafi.flow.flowproperty.PropertySecurity.*;
 import static org.amplafi.flow.flowproperty.PropertyUsage.*;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import static com.sworddance.util.CUtilities.*;
 
 /**
  *
  * EXPERIMENTAL -- I am exploring what/how scope should be used and is it really needed? Or does PropertyUsage do a good job?
  * Need more thinking and explicit explanation of how to use PropertyScope/ {@link PropertyUsage}.
  *
  * PropertyScope is really scope of visibility while the flow is active.
  *
  * So property scope should NOT(? what about activityLocal? be used to determine namespace of property while the flow is active. (while active should always be in
  * the most narrow namespace?)
  *
  * However, once the flow exists, PropertyScope is used to determine where changes to the value should be copied.
  *
  * Do we really need PropertyScope?
 * how to handle initializing flowproperies when they are labeled as 'flowLocal' or 'activityLocal'
    * how should 'flowLocal'/'activityLocal' be used?
    * reason for flowlocal/activity local is to allow a common set of properties names to be reused. this allows activities to be less aware of their flow.
    * an analogy might be parameter passing in Java?
  *
  * @author patmoore
  *
  */
 public enum PropertyScope {
     /**
      * flowLocal to flow.
      * If {@link PropertyUsage} is NOT {@link PropertyUsage#initialize},
      * initial value is copied from global namespace.
      *
      * Need to make sure that callee Flows get their initial property settings from the caller flow.
      * Do we want flowLocal with inheritance?
      *
      * Changes are copied back to global namespace if {@link PropertyUsage} is {@link PropertyUsage#io}
      *
      * TODO put in a flowState.lookupKey namespace
      */
     flowLocal(true, false, use, noRestrictions, false, PropertyUsage.values()),
     /**
      * If {@link PropertyUsage} is NOT {@link PropertyUsage#initialize},
      * Initial value is copied from flow namespace or no value in flow namespace then the global namespace.
      *
      * Changes are copied back to global namespace if {@link PropertyUsage} is {@link PropertyUsage#io}
      *
      * This allows a FA to have a private namespace so it can save info knowing
      * that it will not impact another FA.
      */
     activityLocal(true, true, use, noRestrictions, false, PropertyUsage.values()),
     /**
      * EXPERIMENTAL -- NOT IMPLEMENTED -- playing with this idea.
      *
      * can this be used instead of a separate cached-only?
      * request would be a {@link #flowLocal} that is cleared when current transaction completes.
      *
      * I am not a big fan of 'flash' persistence which clears a value on the completion of the next transaction. Typically used for telling user
      * that the requested action was completed.
      *
      * Useful for passing values between different FAs while doing things like saveChanges()
      *
      * Reasons I dislike 'flash':
      * <ul><li>If user clicks another action immediately, they see the message.</li>
      * <li>If user clicks another action, sees the 'flash' message pop-up. There is no 'go back' support for seeing the flash message.</li>
      * <li>A history tracker is better</li>
      * </ul>
      */
     requestFlowLocal(true, false, internalState, noAccess, true),
     /**
      * Global handles the case when a flow is having a property being set that it has no definition for. Since the flow does not understand the property,
      * it shouldn't try to manage it.
      *
      * TODO: Question: does this make since in the context of morphing? where the flow being morphed to / from may understand it .
      *
      * EXPERIMENTAL -- does global mean that any changes the flow makes are supposed to be 'universally visible'/instantly visible outside of flow?
      * How is this different than saveImmediate? Why not just have flowLocal with PropertyUsage be {@link PropertyUsage#io}?
      *
      * May be null namespace is just the 'transfer' namespace?
      * There should NEVER be a universal flow state because:
      * <ul><li>Security hole: user could change which BP is active. They start an admin flow within a BP that they are admin on.
      * <li>change their active BP to one in which they are much lower</li>
      * <li>with global flow states this could change the BP of the admin flow -- constant battle to make sure that no security hole opens.</li>
      * </ul>
      */
    global(false, false, io, noRestrictions, false, PropertyUsage.values())
     ;
     private final boolean localToFlow;
     private final boolean localToActivity;
     private final PropertyUsage defaultPropertyUsage;
     private final Set<PropertyUsage> allowPropertyUsages;
     private final PropertySecurity defaultPropertySecurity;
     /**
      * Property should not be persisted as string in the FlowState map. This is
      * useful caching values during this transaction. TODO maybe allow
      * non-entities that are Serializable to last beyond current transaction?
      *
      * modifications to this property are not persisted, but are cached.
      *
      * if its sensitive we have already forced this to cache only.
      * this prevents passwords from being saved into the flowstate db table.
      */
     private final boolean cacheOnly;
     private PropertyScope(boolean localToFlow, boolean localToActivity, PropertyUsage defaultPropertyUsage, PropertySecurity defaultPropertySecurity, boolean cacheOnly, PropertyUsage... allowedPropertyUsages) {
         this.localToFlow = localToFlow;
         this.localToActivity = localToActivity;
         this.defaultPropertyUsage = defaultPropertyUsage;
         this.defaultPropertySecurity = defaultPropertySecurity;
         this.cacheOnly = cacheOnly;
         this.allowPropertyUsages = new HashSet<PropertyUsage>();
         if (isNotEmpty(allowedPropertyUsages)) {
             this.getAllowPropertyUsages().addAll(Arrays.asList(allowedPropertyUsages));
         }
         if ( this.defaultPropertyUsage != null ) {
             this.getAllowPropertyUsages().add(defaultPropertyUsage);
         }
     }
     /**
      * @return the namespacedProperty
      */
     public boolean isLocalToFlow() {
         return localToFlow;
     }
     /**
      * @return the localToActivity
      */
     public boolean isLocalToActivity() {
         return localToActivity;
     }
     /**
      * @return the defaultPropertyUsage
      */
     public PropertyUsage getDefaultPropertyUsage() {
         return defaultPropertyUsage;
     }
     /**
      * @return the defaultPropertySecurity
      */
     public PropertySecurity getDefaultPropertySecurity() {
         return defaultPropertySecurity;
     }
     /**
      * @return the cacheOnly
      */
     public boolean isCacheOnly() {
         return cacheOnly;
     }
     /**
      * @return the allowPropertyUsages
      */
     public Set<PropertyUsage> getAllowPropertyUsages() {
         return allowPropertyUsages;
     }
     public boolean isAllowedPropertyUsage(PropertyUsage propertyUsage) {
         return allowPropertyUsages.contains(propertyUsage);
     }
 }
