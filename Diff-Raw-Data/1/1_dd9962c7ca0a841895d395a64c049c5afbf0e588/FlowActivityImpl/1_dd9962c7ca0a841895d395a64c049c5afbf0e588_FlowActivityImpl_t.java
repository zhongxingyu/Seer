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
 
 import static com.sworddance.util.CUtilities.*;
 import static org.amplafi.flow.FlowConstants.FAINVISIBLE;
 import static org.amplafi.flow.FlowConstants.FANEXT_TEXT;
 import static org.amplafi.flow.FlowConstants.FATITLE_TEXT;
 import static org.amplafi.flow.FlowConstants.FAUPDATE_TEXT;
 import static org.amplafi.flow.FlowConstants.FLOW_PROPERTY_PREFIX;
 import static org.amplafi.flow.FlowConstants.FSAUTO_COMPLETE;
 import static org.amplafi.flow.FlowConstants.FSPAGE_NAME;
 import static org.amplafi.flow.flowproperty.ExternalPropertyAccessRestriction.noAccess;
 import static org.amplafi.flow.flowproperty.PropertyScope.activityLocal;
 import static org.amplafi.flow.flowproperty.PropertyUsage.consume;
 import static org.amplafi.flow.flowproperty.PropertyUsage.use;
 import static org.apache.commons.lang.StringUtils.isBlank;
 import static org.apache.commons.lang.StringUtils.isNotBlank;
 import static org.apache.commons.lang.StringUtils.isNumeric;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.amplafi.flow.Flow;
 import org.amplafi.flow.FlowActivity;
 import org.amplafi.flow.FlowActivityImplementor;
 import org.amplafi.flow.FlowActivityPhase;
 import org.amplafi.flow.FlowAppearance;
 import org.amplafi.flow.FlowConstants;
 import org.amplafi.flow.FlowImplementor;
 import org.amplafi.flow.FlowManagement;
 import org.amplafi.flow.FlowPropertyDefinition;
 import org.amplafi.flow.FlowPropertyValueProvider;
 import org.amplafi.flow.FlowState;
 import org.amplafi.flow.FlowStepDirection;
 import org.amplafi.flow.FlowTx;
 import org.amplafi.flow.FlowUtils;
 import org.amplafi.flow.flowproperty.ChainedFlowPropertyValueProvider;
 import org.amplafi.flow.flowproperty.FlowAppearanceFlowPropertyDefinitionProvider;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
 import org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues;
 import org.amplafi.flow.flowproperty.FlowPropertyValuePersister;
 import org.amplafi.flow.flowproperty.PropertyScope;
 import org.amplafi.flow.flowproperty.PropertyUsage;
 import org.amplafi.flow.validation.FlowValidationException;
 import org.amplafi.flow.validation.FlowValidationResult;
 import org.amplafi.flow.validation.FlowValidationResultProvider;
 import org.amplafi.flow.validation.ReportAllValidationResult;
 import org.amplafi.json.JSONObject;
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
 public class FlowActivityImpl extends BaseFlowPropertyProviderWithValues<FlowActivity> implements FlowActivityImplementor {
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
 
     /**
      * if this is an instance, this is the {@link org.amplafi.flow.Flow} instance.
      */
     private FlowImplementor flow;
 
     private String fullActivityInstanceNamespace;
 
     private static final Pattern compNamePattern = Pattern.compile("([\\w]+)\\.flows\\.([\\w]+)FlowActivity$");
 
     private static final List<PropertyScope> LOCAL_PROPERTY_SCOPES = Arrays.asList(PropertyScope.activityLocal);
 
     private List<FlowValidationResultProvider<FlowPropertyProviderWithValues>> flowValidationResultProviders;
     public FlowActivityImpl() {
         this.flowValidationResultProviders = new ArrayList<>();
         // TODO in future make this
         this.flowValidationResultProviders.add(FlowValidationResultProviderImpl.INSTANCE);
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
         // See note in FactoryFlowPropertyDefinitionProvider for what needs to be changed in order for these explicit property definitions to be removed.
         this.addPropertyDefinitions(
             new FlowPropertyDefinitionImpl(FATITLE_TEXT).initAccess(activityLocal, use, noAccess),
             new FlowPropertyDefinitionImpl(FAUPDATE_TEXT).initAccess(activityLocal, use, noAccess),
             new FlowPropertyDefinitionImpl(FANEXT_TEXT).initAccess(activityLocal, use, noAccess),
             //Having this property to be activityLocal we can allow each activity to manage redirects of its own..
             new FlowPropertyDefinitionImpl(FSPAGE_NAME).initAccess(activityLocal, use, noAccess),
             new FlowPropertyDefinitionImpl(FAINVISIBLE, boolean.class).initAccess(activityLocal, consume, noAccess)
         );
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#initializeFlow()
      */
     @Override
     public void initializeFlow() {
         //TODO: this needs to be the same as FlowImpl's initializeFlow() code
         Map<String, FlowPropertyDefinitionImplementor> props = this.getPropertyDefinitions();
         if (props != null) {
             getFlowStateImplementor().initializeFlowProperties(this, props.values());
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#activate(FlowStepDirection)
      */
     @Override
     public boolean activate(FlowStepDirection flowStepDirection) {
         // Check for missing required parameters
         FlowValidationResult activationValidationResult = getFlowValidationResult(FlowActivityPhase.activate, flowStepDirection);
         FlowValidationException.valid(getFlowState(), activationValidationResult);
 
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
     @Override
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
         if ( isNotEmpty(definitions)) {
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
     @Override
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
         if ( isNotEmpty(definitions)) {
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
     @Override
     public FlowState finishFlow(FlowState currentNextFlowState) {
         return currentNextFlowState;
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public FlowImplementor getFlow() {
         return flow;
     }
 
     @Override
     public void setPageName(String pageName) {
         setProperty(FSPAGE_NAME, pageName);
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getPageName()
      */
     @Override
     public String getPageName() {
         return getProperty(FlowConstants.FSPAGE_NAME);
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getComponentName()
      */
     @Override
     public String getComponentName() {
         return componentName;
     }
 
     @Override
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
     @Override
     public FlowValidationResult getFlowValidationResult() {
         return this.getFlowValidationResult(FlowActivityPhase.advance, FlowStepDirection.forward);
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getFlowValidationResult(org.amplafi.flow.FlowActivityPhase, FlowStepDirection)
      */
     @Override
     public FlowValidationResult getFlowValidationResult(FlowActivityPhase flowActivityPhase, FlowStepDirection flowStepDirection) {
         FlowValidationResult flowValidationResult = new ReportAllValidationResult();
         for(FlowValidationResultProvider<FlowPropertyProviderWithValues> flowValidationResultProvider : this.flowValidationResultProviders) {
             flowValidationResult = flowValidationResultProvider.getFlowValidationResult(flowValidationResult, this, flowActivityPhase, flowStepDirection);
         }
         return flowValidationResult;
     }
     public void addFlowValidationResultProvider(FlowValidationResultProvider flowValidationResultProvider) {
         addIfNotContains(this.flowValidationResultProviders, flowValidationResultProvider);
     }
     public void setFlowValidationResultProviders(List<? extends FlowValidationResultProvider<FlowPropertyProviderWithValues>> flowValidationResultProviders) {
         this.flowValidationResultProviders = new ArrayList<>();
         addAllNotNull((List)this.flowValidationResultProviders, (List)flowValidationResultProviders);
     }
     public List<? extends FlowValidationResultProvider<FlowPropertyProviderWithValues>> getFlowValidationResultProviders() {
         return this.flowValidationResultProviders;
     }
 
     @Override
     public void setFlowPropertyProviderName(String flowPropertyProviderName) {
         if ( !StringUtils.equalsIgnoreCase(this.flowPropertyProviderName, flowPropertyProviderName)) {
             ApplicationIllegalStateException.checkState(this.flowPropertyProviderName == null || this.flow == null,
                 this,": cannot change flowPropertyProviderName once it is part of a flow. Tried to change to =",flowPropertyProviderName);
             this.flowPropertyProviderName = flowPropertyProviderName;
         }
     }
 
     @Override
     public String getFlowPropertyProviderName() {
         if ( isFlowPropertyProviderNameSet() ) {
             return super.getFlowPropertyProviderName();
         } else {
             return this.getClass().getSimpleName()+"_"+getIndex();
         }
     }
 
     @Override
     public void setActivityTitle(String activityTitle) {
         this.activityTitle = activityTitle;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getActivityTitle()
      */
     @Override
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
 
     @Override
     public boolean isNamed(String possibleName) {
         if ( isNotBlank(possibleName) ) {
             return possibleName.equals(getFlowPropertyProviderName()) || possibleName.equals(getFullActivityInstanceNamespace()) || possibleName.equals(getFlowPropertyProviderFullName());
         } else {
             return false;
         }
     }
 
     @Override
     public void setActivatable(boolean activatable) {
         this.activatable = activatable;
     }
 
     /**
      * Used to control which FlowActivities a user may select. This is used to
      * prevent the user from jumping ahead in a flow.
      *
      * @return true if this FlowActivity can be activated by the user.
      */
     @Override
     public boolean isActivatable() {
         return activatable;
     }
 
     @Override
     public void setFinishingActivity(boolean finishedActivity) {
         finishingActivity = finishedActivity;
     }
 
     /**
      * @return true if the user can finish the flow when this activity is
      *         current.
      */
     @Override
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
 
     @Override
     public FlowActivityImpl createInstance() {
         FlowActivityImpl instance = dup();
         instance.setDefinition(this);
         return instance;
     }
 
     protected <T extends FlowActivityImpl>void copyTo(T instance) {
         instance.setActivatable(activatable);
         instance.setFlowPropertyProviderName(flowPropertyProviderName);
         instance.setActivityTitle(activityTitle);
         instance.setComponentName(componentName);
         instance.setPageName(getPageName());
         instance.setFinishingActivity(finishingActivity);
         instance.setInvisible(invisible);
         instance.setFlowValidationResultProviders(this.flowValidationResultProviders);
     }
 
     @Override
     @SuppressWarnings({ "unchecked" })
     public <T extends FlowActivityImplementor> T initInvisible(Boolean invisible) {
         this.invisible = invisible;
         return (T) this;
     }
 
     @Override
     public void setInvisible(Boolean invisible) {
         // HACK -- larger problem this value is cached so the next FA looks invisible as well.
         // good test case complete the registration of a new user which transitions to the FinishSignUp.
         // the ChangePasswordFA acts as if it is invisible because of cached "faInvisible" value = true.
         if ( isInstance() ) {
             this.setProperty(FAINVISIBLE, invisible);
         }
 
         this.invisible = invisible;
     }
 
     private boolean isApiCall() {
         if ( isPropertyValueSet(FlowAppearanceFlowPropertyDefinitionProvider.FLOW_APPEARANCE)) {
             FlowAppearance flowAppearance = this.getProperty(FlowAppearanceFlowPropertyDefinitionProvider.FLOW_APPEARANCE);
             return flowAppearance == FlowAppearance.apiCall;
         } else {
             return false;
         }
     }
     /**
      * @see org.amplafi.flow.FlowActivity#isInvisible()
      */
     @Override
     public boolean isInvisible() {
         if ( isApiCall() ) {
             return true;
         } else if (isPropertyValueSet(FAINVISIBLE)) {
             // forced no matter what
             return isTrue(FAINVISIBLE);
         } else if (invisible != null ) {
             return invisible;
         } else {
             // no component -- how could this be visible?
             return !isPossiblyVisible();
         }
     }
 
     @Override
     public boolean isPossiblyVisible() {
         return isNotBlank(getComponentName()) || isNotBlank(getPageName());
     }
 
     /**
      * TODO: pull up to BaseFlowPropertyProvider
      */
     @Override
     @SuppressWarnings("unchecked")
     protected <FPD extends FlowPropertyDefinition> FPD getFlowPropertyDefinition(String flowPropertyDefinitionName, boolean followChain) {
         FPD propertyDefinition = (FPD) super.getFlowPropertyDefinition(flowPropertyDefinitionName, false /*for now*/);
         if (propertyDefinition == null && followChain) {
             propertyDefinition = (FPD) getFlowPropertyDefinitionDefinedInFlow(flowPropertyDefinitionName);
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
 
     @Override
     public void addPropertyDefinition(FlowPropertyDefinitionImplementor definition) {
         FlowPropertyDefinition currentLocal = getFlowPropertyDefinition(definition.getName(), false);
         if ( currentLocal != null) {
             // check against the FlowPropertyDefinition
             if ( !definition.isDataClassMergeable(currentLocal)) {
                 getLog().warn(this.getFlowPropertyProviderFullName()+": has new (overriding) definition '"+definition+
                     "' with different data type than the previous definition '"+currentLocal+"'. The overriding definition will be used.");
             } else if (!definition.merge(currentLocal)) {
                 getLog().warn(this.getFlowPropertyProviderFullName()
                                         + " has a new FlowPropertyDefinition '"
                                         + definition
                                         + "'(overriding) with the same data type but different scope or initializations that conflicts with previous definition " + currentLocal +
                                                 ". Previous definition discarded.");
             }
             removeLocalPropertyDefinition(currentLocal.getName());
         }
         if (!isLocal(definition)) {
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
                     definition.setPropertyUsage(PropertyUsage.internalState);
                 }
                 definition.setPropertyScope(activityLocal);
             } else {
                 // no flow version of this property or no merge conflict with the flow version of the property.
                 // NOTE that this means the first FlowActivity to define this property sets the meaning for the whole flow.
                 // TODO we may want to see if we can merge up to the flow property as well. but this could cause problems with previous flowactivities that have already checked against the
                 // property ( the property might become more precise in a way that causes a conflict.)
                 // TODO: maybe in such cases if no FlowPropertyValueProvider the provider gets merged up?
                 pushPropertyDefinitionToFlow(definition);
             }
         }
         putLocalPropertyDefinition(definition);
     }
 
     protected void pushPropertyDefinitionToFlow(FlowPropertyDefinitionImplementor definition) {
         if (getFlow() != null && !isLocal(definition)) {
             definition.setTemplateFlowPropertyDefinition();
             FlowPropertyDefinitionImplementor flowProp = this.getFlow().getFlowPropertyDefinition( definition.getName());
             // the FAP requirement is removed because otherwise the FPD could prevent an earlier FA from advancing.
             FlowPropertyDefinitionImplementor cloned = definition.initPropertyRequired(null);
             if (flowProp == null ) {
                 // push up to flow so that other can see it.
                 // seems like flows should handle this issue with properties.
                 this.getFlow().addPropertyDefinitions(cloned);
             } else if ( flowProp.isMergeable(cloned)) {
                 flowProp.merge(cloned);
             }
         }
     }
 
     protected void pushPropertyDefinitionsToFlow() {
         if (isNotEmpty(getPropertyDefinitions()) && getFlow() != null) {
             Collection<FlowPropertyDefinition> values = getPropertyDefinitions().values();
             for (FlowPropertyDefinition definition : values) {
                 pushPropertyDefinitionToFlow((FlowPropertyDefinitionImplementor)definition);
             }
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivityImplementor#setFlow(FlowImplementor)
      */
     @Override
     public void setFlow(FlowImplementor flow) {
         this.flow = flow;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getIndex()
      */
     @Override
     public int getIndex() {
         return flow.indexOf(this);
     }
 
     // Duplicated in FlowStateImpl
     @Override
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
 
     @Override
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
     @Override
     public String getRawProperty(String key) {
         FlowPropertyDefinition flowPropertyDefinition = getFlowPropertyDefinitionWithCreate(key, null, null);
         if (flowPropertyDefinition == null) {
             //Looks like flow state wasn't initialized yet, nothing to return..
             return null;
         }
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
 
     @SuppressWarnings("unchecked")
     @Override
     public <T> T getProperty(String key, Class<? extends T> expected) {
         // doing the FlowPropertyDefinition here so that the flow doesn't create a global property.
         FlowPropertyDefinitionImplementor flowPropertyDefinition = getFlowPropertyDefinitionWithCreate(key, expected, null);
         FlowStateImplementor flowStateImplementor = getFlowStateImplementor();
         T result;
         if (flowStateImplementor != null) {
         	result = (T) flowStateImplementor.getPropertyWithDefinition(this, flowPropertyDefinition);
         } else {
         	//There is no flow state yet, i.e. we're in the middle of 'describe' request. Let's just return
         	//default object in the case.
         	result = (T) flowPropertyDefinition.getDefaultObject(this);
         }
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
             FlowImplementor flow = getFlow();
             FlowManagement flowManagement = getFlowManagement();
             if (flowManagement != null) {
                 flowPropertyDefinition = (FP)flowManagement.createFlowPropertyDefinition(flow, key, expected, sampleValue);
             }
         }
         if (flowPropertyDefinition != null &&!flowPropertyDefinition.isFlowTranslatorSet()) {
         	getFlowManagement().getFlowTranslatorResolver().resolve(null, flowPropertyDefinition);
         }
         return flowPropertyDefinition;
     }
 
     @Override
     public String getString(String key) {
         return getProperty(key, String.class);
     }
 
     public Boolean getBoolean(String key) {
         return getProperty(key, Boolean.class);
     }
 
     @Override
     public boolean isTrue(String key) {
         Boolean b = getBoolean(key);
         return b != null && b;
     }
 
 
     /**
      * @see org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues#clearCached(java.lang.String, java.lang.String)
      */
     @Override
     public void clearCached(String namespace, String key) {
         if ( this.getFlowState() != null ) {
             this.getFlowState().clearCached(namespace, key);
         }
     }
 
     /**
      *
      * @see org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues#setProperty(java.lang.String, java.lang.Object)
      */
     @Override
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
     @Override
     public <T> void setProperty(T value) {
         ApplicationNullPointerException.notNull(value, "value must not be null");
         setProperty(value.getClass(), value);
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#setProperty(Class, Object)
      */
     @Override
     public <T> void setProperty(Class<? extends T> dataClass, T value) {
         setProperty(FlowPropertyDefinitionBuilder.toPropertyName(dataClass), value);
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
         FlowPropertyDefinitionImplementor flowPropertyDefinition = getFlowPropertyDefinition(key);
         getFlowStateImplementor().setCached(flowPropertyDefinition, this, value);
         return value;
     }
 
     @Deprecated // should look at initCacheOnly()
     @SuppressWarnings("unchecked")
     protected <T> T getCached(String key) {
         FlowStateImplementor flowState = getFlowStateImplementor();
         FlowPropertyDefinitionImplementor flowPropertyDefinition = getFlowPropertyDefinition(key);
         return flowState == null ? null : (T) flowState.getCached(flowPropertyDefinition, this);
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
     @Override
     public void initPropertyIfNull(String key, Object value) {
         if (!isPropertyValueSet(key)) {
             this.setProperty(key, value);
         }
     }
 
     @Override
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
     @Override
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
     @Override
     public void refresh() {
         saveBack();
     }
 
     protected void redirectTo(URI uri) {
         setProperty(FlowConstants.FSREDIRECT_URL, uri);
     }
 
     /**
      * PAT : 14 jul 2010 : I forgot meaning of this comment :-P
      *
      * This handles the linking of {@link ChainedFlowPropertyValueProvider}.
      *
      * HACK ... should only be called from  {@link #addStandardFlowPropertyDefinitions()} NOT {@link #initializeFlow()}
      * this is because adding to standard properties will not happen correctly ( the {@link org.amplafi.flow.FlowTranslatorResolver} is
      * visible.
      * other wise will affect the definitions.
      * see #2179 / #2192
      *
      * TODO: make chaining more common
      */
     @SuppressWarnings("unchecked")
     protected void handleFlowPropertyValueProvider(String key, FlowPropertyValueProvider flowPropertyValueProvider) {
         FlowPropertyDefinitionImplementor flowPropertyDefinition = this.getFlowPropertyDefinition(key, false);
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
     @Override
     public void processDefinitions() {
         addStandardFlowPropertyDefinitions();
         pushPropertyDefinitionsToFlow();
     }
     @Override
     protected List<PropertyScope> getLocalPropertyScopes() {
         return LOCAL_PROPERTY_SCOPES;
     }
 }
