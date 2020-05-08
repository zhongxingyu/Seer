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
 
 import java.io.Serializable;
 import java.net.URI;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static org.amplafi.flow.FlowConstants.*;
 import static org.amplafi.flow.flowproperty.PropertyScope.*;
 import static org.amplafi.flow.flowproperty.PropertyUsage.*;
 import static org.apache.commons.lang.StringUtils.*;
 
 import org.amplafi.flow.Flow;
 import org.amplafi.flow.FlowActivity;
 import org.amplafi.flow.FlowActivityImplementor;
 import org.amplafi.flow.FlowActivityPhase;
 import org.amplafi.flow.FlowConstants;
 import org.amplafi.flow.FlowImplementor;
 import org.amplafi.flow.FlowManagement;
 import org.amplafi.flow.FlowPropertyDefinition;
 import org.amplafi.flow.FlowPropertyValueProvider;
 import org.amplafi.flow.FlowState;
 import org.amplafi.flow.FlowStepDirection;
 import org.amplafi.flow.FlowTx;
 import org.amplafi.flow.FlowUtils;
 import org.amplafi.flow.FlowValidationResult;
 import org.amplafi.flow.flowproperty.ChainedFlowPropertyValueProvider;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
 import org.amplafi.flow.flowproperty.FlowPropertyValuePersister;
 import org.amplafi.flow.flowproperty.PropertyUsage;
 import org.amplafi.flow.validation.FlowValidationException;
 import org.amplafi.flow.validation.MissingRequiredTracking;
 import org.amplafi.flow.validation.ReportAllValidationResult;
 import org.amplafi.json.JSONObject;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.sworddance.util.ApplicationIllegalStateException;
 import com.sworddance.util.ApplicationNullPointerException;
 
 
 /**
  * defines one activity in the flow. This can be a definition of an activity or
  * the actual activity depending on the state of the Flow parent object.
  * FlowActivityImpl objects may be part of multiple definitions or multiple
  * instances (but not both instances and definitions).
  *
  * FlowActivities must be stateless. FlowActivityImpl instances can be reused
  * between different users and different {@link org.amplafi.flow.Flow}s.
  *
  * <p/> Lifecycle methods:
  * <ol>
  * <li>{@link #initializeFlow()} - used to initialize the FlowState with any
  * defaults for missing values. <b>No</b> modifications should occur in this
  * method.</li>
  * <li>{@link #activate(FlowStepDirection)} - called each time the FlowActivityImpl is made the
  * current FlowActivityImpl. Returns true if the Flow should immediately advance to
  * the next FlowActivityImpl. If this is the last FlowActivityImpl, then the Flow
  * completes. <b>No</b> modifications should occur in this method.</li>
  * <li>{@link #passivate(boolean, FlowStepDirection)} - called each time the FlowActivityImpl was the
  * current FlowActivityImpl and is now no longer the current FlowActivityImpl. Used to
  * validate input as needed. <b>No</b> modifications should occur in this
  * method.</li>
  * <li>{@link #saveChanges()} - called when the flow is completing. <i>Only
  * place where db modifications can be made.</i> This allows canceling the flow
  * to meaningfully revert all changes.</li>
  * <li>{@link #finishFlow(org.amplafi.flow.FlowState)} - called when the flow is finishing.</li>
  * </ol>
  * <p/> This structure is in place so that FlowActivities that create
  * relationships are not put into the position of having to be aware of the
  * surrounding Flow and previously created objects. Nor are they aware of the
  * state of the flow. <p/> By convention, FlowActivies are expected to be in a
  * 'flows' package and the FlowActivityImpl subclass' name ends with 'FlowActivityImpl'.
  * <p/> If a FlowActivityImpl is a visible step then the FlowActivityImpl needs a
  * component. The default component type is the grandparent package + the
  * FlowActivityImpl class name with 'FlowActivityImpl' stripped off. For example,
  * fuzzy.flows.FooBarFlowActivity would have a default component of
  * 'fuzzy/FooBar'. <p/> TODO handle return to previous flow issues.
  */
 public class FlowActivityImpl extends BaseFlowPropertyProvider<FlowActivity> implements Serializable, FlowActivityImplementor {
 
     private static final long serialVersionUID = 5578715117421910908L;
 
     /**
      * The page name that this FlowActivity will activate.
      */
     @Deprecated // use FlowPropertyDefinition
     private String pageName;
 
     /**
      * The component name that this FlowActivity will activate.
      */
     @Deprecated // use FlowPropertyDefinition
     private String componentName;
 
     /**
      * The flow title that the appears in the flow picture.
      */
     @Deprecated // use FlowPropertyDefinition
     private String activityTitle;
 
     /**
      * means that while there may be more possible steps, those steps are
      * optional and the user may exit gracefully out of the flow if this
      * activity is the current activity.
      */
     @Deprecated // use FlowPropertyDefinition
     private boolean finishingActivity;
 
     /**
      * indicates that this flow activity is accessible. Generally, each previous
      * step must be completed for the activity to be available.
      */
     @Deprecated // use FlowPropertyDefinition
     private boolean activatable;
 
     @Deprecated // use FlowPropertyDefinition
     private Boolean invisible;
 
     @Deprecated // use FlowPropertyDefinition
     private boolean persistFlow;
 
     /**
      * if this is an instance, this is the {@link org.amplafi.flow.Flow} instance.
      */
     private FlowImplementor flow;
 
     private String fullActivityInstanceNamespace;
 
     private static final Pattern compNamePattern = Pattern.compile("([\\w]+)\\.flows\\.([\\w]+)FlowActivity$");
 
     public FlowActivityImpl() {
         if (this.getClass() != FlowActivityImpl.class) {
             // a subclass -- therefore subclass name might be good for figuring out the ui component name.
             String name = this.getClass().getName();
             Matcher m = compNamePattern.matcher(name);
             if (m.find()) {
                 componentName = m.group(1) + "/" + m.group(2);
             }
         }
     }
 
     public FlowActivityImpl(String name) {
         this();
         setFlowPropertyProviderName(name);
     }
 
     /**
      * subclasses should override to add their standard definitions.
      */
     protected void addStandardFlowPropertyDefinitions() {
         // see #2179 #2192
         this.addPropertyDefinitions(
             new FlowPropertyDefinitionImpl(FATITLE_TEXT).initAccess(activityLocal, use),
             new FlowPropertyDefinitionImpl(FAUPDATE_TEXT).initAccess(activityLocal, use),
             new FlowPropertyDefinitionImpl(FANEXT_TEXT).initAccess(activityLocal, use),
             new FlowPropertyDefinitionImpl(FAPREV_TEXT).initAccess(activityLocal, use),
             new FlowPropertyDefinitionImpl(FAINVISIBLE, boolean.class).initAccess(activityLocal, consume)
         );
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#initializeFlow()
      */
     public void initializeFlow() {
         //TODO: this needs to be the same as FlowImpl's initializeFlow() code
         Map<String, FlowPropertyDefinition> props = this.getPropertyDefinitions();
         if (props != null) {
             getFlowStateImplementor().initializeFlowProperties(this, props.values());
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#activate(FlowStepDirection)
      */
     public boolean activate(FlowStepDirection flowStepDirection) {
         // Check for missing required parameters
         FlowValidationResult activationValidationResult = getFlowValidationResult(FlowActivityPhase.activate, flowStepDirection);
         FlowValidationException.valid(activationValidationResult);
 
         if ( flowStepDirection == FlowStepDirection.backward) {
             // skip back only through invisible steps.
             return isInvisible();
         } else if (!isInvisible() ) {
             // auto complete only happens when advancing (otherwise can never get back to such FAs)
             // additional work needed here -- FSAUTO_COMPLETE should be consumed.
             boolean autoComplete = isTrue(FSAUTO_COMPLETE);
             if (autoComplete) {
                 FlowValidationResult flowValidationResult = getFlowValidationResult(FlowActivityPhase.advance, flowStepDirection);
                 return flowValidationResult.isValid();
             }
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#passivate(boolean, FlowStepDirection)
      */
     public FlowValidationResult passivate(boolean verifyValues, FlowStepDirection flowStepDirection) {
         if (verifyValues) {
             // HACK that needs to be fixed. -- we only need to verify when we go back because:
             // 1)if the user is on a previous step and a later step has a validation problem we need to advance the flow
             // to that step.
             // 2) if we have a step that does an immediate save operation.
             FlowValidationResult validationResult = flowStepDirection == FlowStepDirection.backward?
                 getFlowValidationResult(FlowActivityPhase.advance, flowStepDirection):
                     getFlowValidationResult();
             return validationResult;
         }
         return new ReportAllValidationResult();
     }
 
     /**
      * Some properties do not have simple representation. for those objects
      * their internal state may have changed. This give opportunity for that
      * internal state change to be saved into flowState.
      */
     protected void saveBack() {
         Map<String, FlowPropertyDefinition> definitions = this.getPropertyDefinitions();
         if ( MapUtils.isNotEmpty(definitions)) {
             for (Map.Entry<String, FlowPropertyDefinition> entry : definitions.entrySet()) {
                 FlowPropertyDefinition flowPropertyDefinition = entry.getValue();
                 if (!flowPropertyDefinition.isCacheOnly() && flowPropertyDefinition.isSaveBack()) {
                     Object cached = getCached(entry.getKey());
                     if (cached != null) {
                         // this means that we miss case when the value has been
                         // discarded.
                         // but otherwise much worse case when the value simply was
                         // not accessed
                         // results in the property being cleared.
                         setProperty(entry.getKey(), cached);
                     }
                 }
             }
         }
     }
     /**
      * Saves properties using the FlowPropertyValuePersister.
      * @see org.amplafi.flow.FlowActivity#saveChanges()
      */
     @SuppressWarnings("unchecked")
     public void saveChanges() {
         // TODO: refactor so that FlowStateImpl can save the global flow Properties.
         // TODO: make sure that each property is only saved once.
         // TODO: have mechanism to determine if a property has been changed.
         // TODO: security mechanism on who is allowed to change the value.
         // TODO: watch out for case when a FlowActivity uses a property read-only where a later FA will do the actual modifications for saving.
         // For example, CreateEndPointFA accesses the ExternalServiceInstance. The ExternalUriFA, which occurs later in the Flow, is actually defining and creating the ESI.
         // Probably 1) we need to define access as readonly v. write. So this loop here would only save properties that are writeable. This would also be a good security feature.
         // so even if there is a bug that some how permits someone to change the data of a readonly object during the flow - that change is not persisted. ( watch out for things that
         // would allow user to upgrade their permissions.
         // 2) should persistence be sequenced?
         Map<String, FlowPropertyDefinition> definitions = this.getPropertyDefinitions();
         if ( MapUtils.isNotEmpty(definitions)) {
             for (Map.Entry<String, FlowPropertyDefinition> entry : definitions.entrySet()) {
                 FlowPropertyDefinition flowPropertyDefinition = entry.getValue();
                 FlowPropertyValuePersister flowPropertyValuePersister = flowPropertyDefinition.getFlowPropertyValuePersister();
                 if ( flowPropertyValuePersister != null) {
                    getFlowManagement().wireDependencies(flowPropertyValuePersister);
                     flowPropertyValuePersister.saveChanges(this, flowPropertyDefinition);
                 }
             }
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#finishFlow(org.amplafi.flow.FlowState)
      */
     public FlowState finishFlow(FlowState currentNextFlowState) {
         return currentNextFlowState;
     }
 
     @SuppressWarnings("unchecked")
     public FlowImplementor getFlow() {
         return flow;
     }
 
     public void setPageName(String pageName) {
         this.pageName = pageName;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getPageName()
      */
     public String getPageName() {
         return pageName;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getComponentName()
      */
     public String getComponentName() {
         return componentName;
     }
 
     public void setComponentName(String componentName) {
         this.componentName = componentName;
     }
 
     /**
      * creates a new Flow instance. The flow definition of the new Flow is
      * spawnFlowTypeName. This is an optional flow from this current flow.
      *
      * @param spawnFlowTypeName the flow definition to use to create the new
      *        flow instance.
      * @return created {@link FlowState}
      */
     @SuppressWarnings("unchecked")
     protected <FS extends FlowState> FS createNewFlow(String spawnFlowTypeName) {
         FlowManagement fm = getFlowManagement();
         FS createdFlowState = (FS) fm.createFlowState(spawnFlowTypeName, getFlowState().getExportedValuesMap(), false);
         return createdFlowState;
     }
 
     /**
      * HACK really need to make it easy for a FA to just be interested in inPlace, and PropertyRequired.saveChanges, advance or finish
      * @return {@link #getFlowValidationResult(FlowActivityPhase, FlowStepDirection)}(FlowActivityPhase.advance, FlowStepDirection.forward)
      */
     public FlowValidationResult getFlowValidationResult() {
         return this.getFlowValidationResult(FlowActivityPhase.advance, FlowStepDirection.forward);
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getFlowValidationResult(org.amplafi.flow.FlowActivityPhase, FlowStepDirection)
      */
     public FlowValidationResult getFlowValidationResult(FlowActivityPhase flowActivityPhase, FlowStepDirection flowStepDirection) {
         // TODO : Don't validate if user is going backwards.
         // Need to handle case where user enters invalid data, backs up and then tries to complete the flow
         FlowValidationResult result = new ReportAllValidationResult();
         Map<String, FlowPropertyDefinition> propDefs = getPropertyDefinitions();
         if (MapUtils.isNotEmpty(propDefs)) {
             for (FlowPropertyDefinition def : propDefs.values()) {
                 MissingRequiredTracking.appendRequiredTrackingIfTrue(result,
                     (flowActivityPhase != null && def.getPropertyRequired() == flowActivityPhase)
                         && !isPropertySet(def.getName())
                         && !def.isAutoCreate(),
                         def.getUiComponentParameterName());
             }
         }
         return result;
     }
 
     @Override
     public void setFlowPropertyProviderName(String flowPropertyProviderName) {
         if ( !StringUtils.equalsIgnoreCase(this.flowPropertyProviderName, flowPropertyProviderName)) {
             ApplicationIllegalStateException.valid(this.flowPropertyProviderName == null || this.flow == null,
                 this,": cannot change flowPropertyProviderName once it is part of a flow. Tried to change to =",flowPropertyProviderName);
             this.flowPropertyProviderName = flowPropertyProviderName;
         }
     }
 
     @Override
     public String getFlowPropertyProviderName() {
         return isFlowPropertyProviderNameSet()?super.getFlowPropertyProviderName():this.getClass().getSimpleName();
     }
 
     public void setActivityTitle(String activityTitle) {
         this.activityTitle = activityTitle;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getActivityTitle()
      */
     public String getActivityTitle() {
         String title = getString(FATITLE_TEXT);
         if (isNotBlank(title)) {
             return title;
         } else if (isNotBlank(activityTitle)){
             return activityTitle;
         } else {
             return "message:"+ "flow.activity." + FlowUtils.INSTANCE.toLowerCase(this.getFlowPropertyProviderName() ) +".title";
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getFlowPropertyProviderFullName()
      */
     @Override
     public String getFlowPropertyProviderFullName() {
         if ( this.getFlow() != null) {
             return this.getFlow().getFlowPropertyProviderName() + "." + getFlowPropertyProviderName();
         } else {
             return getFlowPropertyProviderName();
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getFullActivityInstanceNamespace()
      */
     @Override
     public String getFullActivityInstanceNamespace() {
         if ( this.fullActivityInstanceNamespace == null) {
             if ( this.getFlowState() != null ) {
                 this.fullActivityInstanceNamespace = getFlowState().getLookupKey()+"."+getFlowPropertyProviderName();
             } else {
                 // we don't want to save this value because then if this method is called before a flowState is attached, then this flowActivity will never see the
                 // namespace that should be used after the flow is active.
                 return getFlowPropertyProviderFullName();
             }
         }
         return this.fullActivityInstanceNamespace;
     }
 
     public boolean isNamed(String possibleName) {
         if ( isNotBlank(possibleName) ) {
             return possibleName.equals(getFlowPropertyProviderName()) || possibleName.equals(getFullActivityInstanceNamespace()) || possibleName.equals(getFlowPropertyProviderFullName());
         } else {
             return false;
         }
     }
 
     public void setActivatable(boolean activatable) {
         this.activatable = activatable;
     }
 
     /**
      * Used to control which FlowActivities a user may select. This is used to
      * prevent the user from jumping ahead in a flow.
      *
      * @return true if this FlowActivity can be activated by the user.
      */
     public boolean isActivatable() {
         return activatable;
     }
 
     public void setFinishingActivity(boolean finishedActivity) {
         finishingActivity = finishedActivity;
     }
 
     /**
      * @return true if the user can finish the flow when this activity is
      *         current.
      */
     public boolean isFinishingActivity() {
         return finishingActivity;
     }
 
     protected FlowManagement getFlowManagement() {
         return this.getFlowState() == null ? null : this.getFlowState().getFlowManagement();
     }
 
     /**
      *
      * @see org.amplafi.flow.flowproperty.FlowPropertyProviderImplementor#getFlowState()
      */
     @SuppressWarnings("unchecked")
     @Override
     public <FS extends FlowState> FS getFlowState() {
         return flow == null? null:(FS)flow.getFlowState();
     }
 
     @SuppressWarnings("unchecked")
     protected <FS extends FlowStateImplementor> FS getFlowStateImplementor() {
         return flow == null? null:(FS)flow.getFlowState();
     }
     @SuppressWarnings("unchecked")
     public <T> T dup() {
         FlowActivityImpl clone;
         try {
             clone = this.getClass().newInstance();
         } catch (Exception e) {
             throw new IllegalArgumentException(e);
         }
         copyTo(clone);
         return (T) clone;
     }
 
     public FlowActivityImpl createInstance() {
         FlowActivityImpl instance = dup();
         copyTo(instance);
         instance.setDefinition(this);
         return instance;
     }
 
     protected <T extends FlowActivityImpl>void copyTo(T instance) {
         instance.activatable = activatable;
         instance.flowPropertyProviderName = flowPropertyProviderName;
         instance.activityTitle = activityTitle;
         instance.componentName = componentName;
         instance.pageName = pageName;
         instance.finishingActivity = finishingActivity;
         instance.invisible = invisible;
         instance.persistFlow = persistFlow;
     }
 
     @SuppressWarnings({ "hiding", "unchecked" })
     public <T extends FlowActivityImplementor> T initInvisible(Boolean invisible) {
         this.invisible = invisible;
         return (T) this;
     }
 
     public void setInvisible(boolean invisible) {
         // HACK -- larger problem this value is cached so the next FA looks invisible as well.
         // good test case complete the registration of a new user which transitions to the FinishSignUp.
         // the ChangePasswordFA acts as if it is invisible because of cached "faInvisible" value = true.
         if ( isInstance() ) {
             this.setProperty(FAINVISIBLE, invisible);
         }
 
         this.invisible = invisible;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#isInvisible()
      */
     public boolean isInvisible() {
         if (isPropertyValueSet(FAINVISIBLE)) {
             // forced no matter what
             return isTrue(FAINVISIBLE);
         } else if (invisible != null ) {
             return invisible;
         } else {
             // no component -- how could this be visible?
             return !isPossiblyVisible();
         }
     }
 
     public boolean isPossiblyVisible() {
         return StringUtils.isNotBlank(getComponentName()) || StringUtils.isNotBlank(getPageName());
     }
 
     public void setPersistFlow(boolean persistFlow) {
         this.persistFlow = persistFlow;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#isPersistFlow()
      */
     public boolean isPersistFlow() {
         return persistFlow;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getFlowPropertyDefinition(java.lang.String)
      */
     @Override
     @SuppressWarnings("unchecked")
     public <T extends FlowPropertyDefinition> T getFlowPropertyDefinition(String key) {
         T propertyDefinition = (T) getLocalPropertyDefinition(key);
         if (propertyDefinition == null) {
             propertyDefinition = (T) getFlowPropertyDefinitionDefinedInFlow(key);
         }
         return propertyDefinition;
     }
 
     @SuppressWarnings("unchecked")
     private <T extends FlowPropertyDefinition> T getFlowPropertyDefinitionDefinedInFlow(String key) {
         T flowPropertyDefinition = null;
         if ( this.getFlowState() != null) {
             flowPropertyDefinition = (T) getFlowState().getFlowPropertyDefinition(key);
         }
         // should be else if
         if ( flowPropertyDefinition == null && this.getFlow() != null) {
             flowPropertyDefinition = (T) this.getFlow().getFlowPropertyDefinition(key);
         }
         return flowPropertyDefinition;
     }
 
     /**
      * Look for the {@link FlowPropertyDefinition} in just this FlowActivity's local property definitions.
      * @param key
      * @return
      */
     @SuppressWarnings("unchecked")
     private <T extends FlowPropertyDefinition> T getLocalPropertyDefinition(String key) {
         Map<String, FlowPropertyDefinition> propDefs = this.getPropertyDefinitions();
         T def = null;
         if (propDefs != null) {
             def = (T) propDefs.get(key);
         }
         return def;
     }
 
     @Override
     public void addPropertyDefinition(FlowPropertyDefinition definition) {
         FlowPropertyDefinition currentLocal;
         if (getPropertyDefinitions() == null) {
             setPropertyDefinitions(new LinkedHashMap<String, FlowPropertyDefinition>());
             currentLocal = null;
         } else if ( (currentLocal = getLocalPropertyDefinition(definition.getName())) != null) {
             // check against the FlowPropertyDefinition
             if ( !definition.isDataClassMergeable(currentLocal)) {
                 getLog().warn(this.getFlowPropertyProviderFullName()+": has new (overriding) definition '"+definition+
                     "' with different data type than the previous definition '"+currentLocal+"'. The overriding definition will be used.");
             } else if (!definition.merge(currentLocal)) {
                 getLog().debug(this.getFlowPropertyProviderFullName()
                                         + " has a new FlowPropertyDefinition '"
                                         + definition
                                         + "'(overriding) with the same data type but different scope or initializations that conflicts with previous definition " + currentLocal +
                                         		". Previous definition discarded.");
             }
             getPropertyDefinitions().remove(currentLocal.getName());
         }
         if (!definition.isLocal()) {
             // this property may be from the Flow definition itself.
             FlowPropertyDefinition current = this.getFlowPropertyDefinitionDefinedInFlow(definition.getName());
             if ( current != null && !definition.merge(current)) {
                 if ( definition.isDataClassMergeable(current)) {
                     getLog().debug(this.getFlowPropertyProviderFullName()
                                         + " has a FlowPropertyDefinition '"
                                         + definition.getName()
                                         + "' that conflicts with flow's property definition. The data classes are mergeable. So this probably is o.k. The FlowActivity's definition will be marked as 'activityLocal'.");
                 } else {
                     getLog().warn(this.getFlowPropertyProviderFullName()
                         + " has a FlowPropertyDefinition '"
                         + definition.getName()
                         + "' that conflicts with flow's property definition. The data classes are NOT mergeable. This might cause problems so the FlowActivity's definition will be marked as 'activityLocal' and 'internalState'.");
                     ((FlowPropertyDefinitionImplementor)definition).setPropertyUsage(PropertyUsage.internalState);
                 }
                 ((FlowPropertyDefinitionImplementor)definition).setPropertyScope(activityLocal);
             } else {
                 // no flow version of this property.
                 // NOTE that this means the first FlowActivity to define this property sets the meaning for the whole flow.
                 // TODO we may want to see if we can merge up to the flow property as well. but this could cause problems with previous flowactivities that have already checked against the
                 // property ( the property might become more precise in a way that causes a conflict.)
                 // TODO: maybe in such cases if no FlowPropertyValueProvider the provider gets merged up?
                 pushPropertyDefinitionToFlow(definition);
             }
         }
         getPropertyDefinitions().put(definition.getName(), definition);
     }
 
     protected void pushPropertyDefinitionToFlow(FlowPropertyDefinition definition) {
         if (getFlow() != null && !definition.isLocal()) {
             FlowPropertyDefinitionImplementor flowProp = this.getFlow().getFlowPropertyDefinition( definition.getName());
             if (flowProp == null ) {
                 // push up to flow so that other can see it.
                 // seems like flows should handle this issue with properties.
                 FlowPropertyDefinitionImplementor cloned = definition.clone();
                 // a FPD may be pushed so for an earlier FA may not require the property be set.
                 cloned.setPropertyRequired(FlowActivityPhase.optional);
                 this.getFlow().addPropertyDefinitions(cloned);
             } else if ( flowProp.isMergeable(definition)) {
                 flowProp.merge(definition);
                 flowProp.setPropertyRequired(FlowActivityPhase.optional);
             }
         }
     }
 
     protected void pushPropertyDefinitionsToFlow() {
         if (MapUtils.isNotEmpty(getPropertyDefinitions()) && getFlow() != null) {
             for (FlowPropertyDefinition definition : getPropertyDefinitions().values()) {
                 pushPropertyDefinitionToFlow(definition);
             }
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivityImplementor#setFlow(FlowImplementor)
      */
     public void setFlow(FlowImplementor flow) {
         this.flow = flow;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getIndex()
      */
     public int getIndex() {
         return flow.indexOf(this);
     }
 
     // Duplicated in FlowStateImpl
     public boolean isPropertySet(String key) {
         // HACK : too problematic need better way to ask if a FlowPropertyValueProvider can actually return a value.
 //        FlowPropertyDefinition flowPropertyDefinition = getFlowPropertyDefinitionWithCreate(key, null, null);
 //        if ( !flowPropertyDefinition.isDefaultObjectAvailable(this)) {
             return isPropertyValueSet(key);
 //        } else {
 //            return true;
 //        }
     }
 
     /**
      * @see org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues#isPropertyValueSet(java.lang.String)
      */
     @Override
     // Duplicated in FlowStateImpl
     public boolean isPropertyValueSet(String key) {
         return getRawProperty(key) != null;
     }
 
     public boolean isPropertyBlank(String key) {
         String v = getRawProperty(key);
         return isBlank(v);
     }
 
     public boolean isPropertyNumeric(String key) {
         String v = getRawProperty(key);
         return isNotEmpty(v) && isNumeric(v);
     }
 
     /**
      * Return the raw string value representation of the value indexed by key.
      * This should be used only rarely, as it bypasses all of the normal
      * property processing code. <p/> Permissible uses include cases are rare
      * and does not include retrieving a String property.
      *
      * @param key
      * @return raw string property.
      */
     public String getRawProperty(String key) {
         FlowPropertyDefinition flowPropertyDefinition = getFlowPropertyDefinitionWithCreate(key, null, null);
         return getRawProperty(flowPropertyDefinition);
     }
 
     /**
      * @param flowPropertyDefinition
      * @return the property as a string not converted to the object.
      */
     protected String getRawProperty(FlowPropertyDefinition flowPropertyDefinition) {
         if ( isInstance()) {
             return getFlowStateImplementor().getRawProperty(this, flowPropertyDefinition);
         } else {
             // TODO: initial not always set - for example, "invisible" -property.
             return flowPropertyDefinition.getInitial();
         }
     }
 
     public Boolean getRawBoolean(String key) {
         String value = getRawProperty(key);
         return Boolean.parseBoolean(value);
     }
 
     public Long getRawLong(String key) {
         return getFlowStateImplementor().getRawLong(this, key);
     }
 
     // TODO .. injected
     protected FlowTx getTx() {
         return getFlowManagement().getFlowTx();
     }
 
     public void delete(Object entity) {
         getTx().delete(entity);
     }
 
     public <T> boolean flushIfNeeded(T... entities) {
         return getTx().flushIfNeeded(entities);
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getProperty(java.lang.String)
      */
     @SuppressWarnings("unchecked")
     public final <T> T getProperty(String key) {
         return (T) this.getProperty(key, null);
     }
     @SuppressWarnings("unchecked")
     @Override
     public <T> T getProperty(String key, Class<? extends T> expected) {
         FlowPropertyDefinition flowPropertyDefinition = getFlowPropertyDefinitionWithCreate(key, expected, null);
         T result = (T) getFlowStateImplementor().getPropertyWithDefinition(this, flowPropertyDefinition);
         return result;
     }
     /**
      * @param key
      * @return a flow property definition, if none then the definition is created
      */
     @SuppressWarnings("unchecked")
     protected <T, FP extends FlowPropertyDefinition> FP getFlowPropertyDefinitionWithCreate(String key, Class<T> expected, T sampleValue) {
         FP flowPropertyDefinition = (FP)getFlowPropertyDefinition(key);
         if (flowPropertyDefinition == null) {
             flowPropertyDefinition = (FP)getFlowManagement().createFlowPropertyDefinition(getFlow(), key, expected, sampleValue);
         }
         return flowPropertyDefinition;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getProperty(java.lang.Class)
      */
     public <T> T getProperty(Class<? extends T> dataClass) {
         return getProperty(FlowUtils.INSTANCE.toPropertyName(dataClass), dataClass);
     }
 
     public String getString(String key) {
         return getProperty(key, String.class);
     }
 
     public Boolean getBoolean(String key) {
         return getProperty(key, Boolean.class);
     }
 
     public boolean isTrue(String key) {
         Boolean b = getBoolean(key);
         return b != null && b;
     }
 
     /**
      *
      * @see org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues#setProperty(java.lang.String, java.lang.Object)
      */
     public <T> void setProperty(String key, T value) {
         if ( getFlowState() != null) { // TODO: why are we ignoring (probably a test that should be fixed )
             FlowPropertyDefinitionImplementor propertyDefinition = getFlowPropertyDefinitionWithCreate(key, null, value);
             setProperty(propertyDefinition, value);
         }
     }
 
     /**
      * @param <T>
      * @param propertyDefinition
      * @param value
      */
     protected <T> void setProperty(FlowPropertyDefinitionImplementor propertyDefinition, T value) {
         getFlowStateImplementor().setPropertyWithDefinition(this, propertyDefinition, value);
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#setProperty(Object)
      */
     public <T> void setProperty(T value) {
         ApplicationNullPointerException.notNull(value, "value must not be null");
         setProperty(value.getClass(), value);
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#setProperty(Class, Object)
      */
     public <T> void setProperty(Class<? extends T> dataClass, T value) {
         setProperty(FlowUtils.INSTANCE.toPropertyName(dataClass), value);
     }
 
     /**
      * save an object in the cache. This cache is flushed when the current
      * transaction has been committed. This flushing is necessary because
      * otherwise there will be errors with accessing data outside a transaction.
      *
      * @param <T>
      * @param key
      * @param value
      */
     @Deprecated // should look at initCacheOnly()
     protected <T> T cache(String key, T value) {
         getFlowStateImplementor().setCached(getFlowPropertyDefinition(key), this, value);
         return value;
     }
 
     @Deprecated // should look at initCacheOnly()
     @SuppressWarnings("unchecked")
     protected <T> T getCached(String key) {
         FlowStateImplementor flowState = getFlowStateImplementor();
         return flowState == null ? null : (T) flowState.getCached(getFlowPropertyDefinition(key), this);
     }
 
     @Override
     public String toString() {
         Flow f = getFlow();
         if ( f != null ) {
             return f.getFlowPropertyProviderName()+"."+getFlowPropertyProviderName()+ " " +getClass()+" id="+super.toString();
         } else {
             return getFlowPropertyProviderName()+ " " +getClass()+" id="+super.toString();
         }
     }
 
     /**
      * If the property has no value stored in the flowState's keyvalueMap then
      * put the supplied value in it.
      *
      * @param key
      * @param value
      * @see #isPropertyValueSet(String)
      */
     public void initPropertyIfNull(String key, Object value) {
         if (!isPropertyValueSet(key)) {
             this.setProperty(key, value);
         }
     }
 
     public void initPropertyIfBlank(String key, Object value) {
         if (isPropertyBlank(key)) {
             this.setProperty(key, value);
         }
     }
 
     // TODO inject....
     protected Log getLog() {
         FlowManagement flowManagement = getFlowManagement();
         return flowManagement != null ? flowManagement.getLog() : LogFactory
                 .getLog(this.getClass());
     }
 
     /**
      * @param value may start with {@link FlowConstants#FLOW_PROPERTY_PREFIX}
      * @return value if does not start with
      *         {@link FlowConstants#FLOW_PROPERTY_PREFIX}, otherwise look up the
      *         property value referenced and return that value.
      */
     public String resolveIndirectReference(String value) {
         if (value != null && value.startsWith(FLOW_PROPERTY_PREFIX)) {
             return getString(value.substring(FLOW_PROPERTY_PREFIX.length()));
         } else {
             return value;
         }
     }
 
     protected String getResolvedIndirectReferenceProperty(String key) {
         return resolveIndirectReference(getString(key));
     }
 
     // TODO refactor to FlowState ?
     protected JSONObject getRawJsonObject(String key) {
         String rawProperty = getRawProperty(key);
         return JSONObject.toJsonObject(rawProperty);
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#refresh()
      */
     public void refresh() {
         saveBack();
     }
 
     protected void redirectTo(URI uri) {
         setProperty(FlowConstants.FSREDIRECT_URL, uri);
     }
 
     /**
      * HACK ... should only be called from  {@link #addStandardFlowPropertyDefinitions()} NOT {@link #initializeFlow()}
      * this is because adding to standard properties will not happen correctly ( the {@link org.amplafi.flow.FlowTranslatorResolver} is
      * visible.
      * other wise will affect the definitions.
      * see #2179 / #2192
      */
     @SuppressWarnings("unchecked")
     protected void handleFlowPropertyValueProvider(String key, FlowPropertyValueProvider flowPropertyValueProvider) {
         FlowPropertyDefinitionImplementor flowPropertyDefinition = this.getLocalPropertyDefinition(key);
         if ( flowPropertyDefinition != null) {
             if ( flowPropertyValueProvider instanceof ChainedFlowPropertyValueProvider) {
                 ((ChainedFlowPropertyValueProvider)flowPropertyValueProvider).setPrevious(flowPropertyDefinition.getFlowPropertyValueProvider());
             }
             flowPropertyDefinition.setFlowPropertyValueProvider(flowPropertyValueProvider);
         }
         flowPropertyDefinition = this.getFlowPropertyDefinitionDefinedInFlow(key);
         if ( flowPropertyDefinition != null) {
             if ( flowPropertyValueProvider instanceof ChainedFlowPropertyValueProvider) {
                 ((ChainedFlowPropertyValueProvider)flowPropertyValueProvider).setPrevious(flowPropertyDefinition.getFlowPropertyValueProvider());
             }
             flowPropertyDefinition.setFlowPropertyValueProvider(flowPropertyValueProvider);
         }
     }
 
     /**
      *
      */
     public void processDefinitions() {
         addStandardFlowPropertyDefinitions();
         pushPropertyDefinitionsToFlow();
     }
 }
