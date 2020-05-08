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
 
 import static org.amplafi.flow.FlowConstants.*;
 import static org.amplafi.flow.PropertyUsage.*;
 import static org.apache.commons.lang.StringUtils.isBlank;
 import static org.apache.commons.lang.StringUtils.isNotBlank;
 import static org.apache.commons.lang.StringUtils.isNotEmpty;
 import static org.apache.commons.lang.StringUtils.isNumeric;
 
 import java.io.Serializable;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.amplafi.flow.flowproperty.*;
 import org.amplafi.flow.validation.FlowValidationException;
 import org.amplafi.flow.*;
 import org.amplafi.flow.validation.InconsistencyTracking;
 import org.amplafi.flow.validation.MissingRequiredTracking;
 import org.amplafi.flow.validation.ReportAllValidationResult;
 import org.amplafi.json.JSONObject;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
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
 public class FlowActivityImpl implements Serializable, FlowActivityImplementor {
 
     private static final long serialVersionUID = 5578715117421910908L;
 
     /**
      * The page name that this FlowActivity will activate.
      */
     private String pageName;
 
     /**
      * The component name that this FlowActivity will activate.
      */
     private String componentName;
 
     /**
      * This is the activity name (id) of this FlowActivity.
      */
     private String activityName;
 
     /**
      * The flow title that the appears in the flow picture.
      */
     private String activityTitle;
 
     /**
      * means that while there may be more possible steps, those steps are
      * optional and the user may exit gracefully out of the flow if this
      * activity is the current activity.
      */
     private boolean finishingActivity;
 
     /**
      * indicates that this flow activity is accessible. Generally, each previous
      * step must be completed for the activity to be available.
      */
     private boolean activatable;
 
     private boolean invisible;
 
     private boolean persistFlow;
 
     /**
      * if this is an instance, this is the {@link org.amplafi.flow.Flow} instance.
      */
     private Flow flow;
 
     /**
      * if this is an instance {@link org.amplafi.flow.FlowActivity}, then this is the definition
      * {@link org.amplafi.flow.FlowActivity}.
      */
     private FlowActivity definitionFlowActivity;
 
     /**
      * null if this is an instance.
      */
     private Map<String, FlowPropertyDefinition> propertyDefinitions;
 
     private static final Pattern compNamePattern = Pattern
             .compile("([\\w]+)\\.flows\\.([\\w]+)FlowActivity$");
 
     public FlowActivityImpl() {
         if (this.getClass() != FlowActivityImpl.class) {
             // set default activity name
             activityName = this.getClass().getSimpleName();
 
             String name = this.getClass().getName();
             Matcher m = compNamePattern.matcher(name);
             if (m.find()) {
                 componentName = m.group(1) + "/" + m.group(2);
             }
         }
     }
 
     /**
      * subclasses should override to add their standard definitions.
      */
     protected void addStandardFlowPropertyDefinitions() {
         // see #2179 #2192
         this.addPropertyDefinitions(
             new FlowPropertyDefinitionImpl(FATITLE_TEXT).initPropertyUsage(activityLocal),
             new FlowPropertyDefinitionImpl(FAUPDATE_TEXT).initPropertyUsage(activityLocal),
             new FlowPropertyDefinitionImpl(FANEXT_TEXT).initPropertyUsage(activityLocal),
             new FlowPropertyDefinitionImpl(FAPREV_TEXT).initPropertyUsage(activityLocal),
             new FlowPropertyDefinitionImpl(FAINVISIBLE, boolean.class).initPropertyUsage(activityLocal)
         );
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#initializeFlow()
      */
     public void initializeFlow() {
         Map<String, FlowPropertyDefinition> props = this.getPropertyDefinitions();
         if (props != null) {
             for (FlowPropertyDefinition propertyDefinition : props.values()) {
                 // move values from alternateNames to the true name.
                 // or just clear out the alternate names of their values.
                 for (String alternateName : propertyDefinition.getAlternates()) {
                     String altProperty = getRawProperty(alternateName);
                     setRawProperty(alternateName, null);
                     if (isNotBlank(altProperty)) {
                         initPropertyIfNull(propertyDefinition.getName(), altProperty);
                     }
                 }
                 // if the property is not allowed to be overridden then we force
                 // initialize it.
                 String initial = propertyDefinition.getInitial();
                 if (initial != null) {
                     if (!propertyDefinition.isInitialMode()) {
                         String currentValue = getRawProperty(propertyDefinition.getName());
                         if (!StringUtils.equals(initial, currentValue)) {
                             if (currentValue != null) {
                                 throw new IllegalArgumentException(
                                         this.getFullActivityName()
                                                 + '.'
                                                 + propertyDefinition.getName()
                                                 + " cannot be set to '"
                                                 + currentValue
                                                 + "' external to the flow. It is being force to the initial value of '"
                                                 + initial + "'");
                             }
                             setProperty(propertyDefinition.getName(), initial);
                         }
                     } else {
                         initPropertyIfNull(propertyDefinition.getName(), initial);
                     }
                 }
             }
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#activate(FlowStepDirection)
      */
     public boolean activate(FlowStepDirection flowStepDirection) {
         // Check for missing required parameters
         FlowValidationResult activationValidationResult = getFlowValidationResult(PropertyRequired.activate);
         if (!activationValidationResult.isValid()) {
             throw new FlowValidationException(activationValidationResult);
         }
 
         if (!isInvisible()) {
             boolean autoComplete = isTrue(FSAUTO_COMPLETE);
             if (autoComplete) {
                 FlowValidationResult flowValidationResult = getFlowValidationResult();
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
             FlowValidationResult validationResult = getFlowValidationResult();
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
                 if (!entry.getValue().isCacheOnly() && entry.getValue().isSaveBack()) {
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
      * @see org.amplafi.flow.FlowActivity#saveChanges()
      */
     public void saveChanges() {
 
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#finishFlow(org.amplafi.flow.FlowState)
      */
     public FlowState finishFlow(FlowState currentNextFlowState) {
         return currentNextFlowState;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getFlow()
      */
     public Flow getFlow() {
         return flow;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#setPageName(java.lang.String)
      */
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
 
     /**
      * @see org.amplafi.flow.FlowActivity#setComponentName(java.lang.String)
      */
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
     protected FlowState createNewFlow(String spawnFlowTypeName) {
         FlowManagement fm = getFlowManagement();
         FlowState createdFlowState = fm.createFlowState(spawnFlowTypeName, getFlowState().getClearFlowValuesMap(), false);
         return createdFlowState;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getFlowValidationResult()
      */
     public FlowValidationResult getFlowValidationResult() {
         return this.getFlowValidationResult(PropertyRequired.advance);
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getFlowValidationResult(org.amplafi.flow.PropertyRequired)
      */
     public FlowValidationResult getFlowValidationResult(PropertyRequired required) {
         FlowValidationResult result = new ReportAllValidationResult();
         Map<String, FlowPropertyDefinition> propDefs = getPropertyDefinitions();
         if (MapUtils.isNotEmpty(propDefs)) {
             for (FlowPropertyDefinition def : propDefs.values()) {
                 if ((required != null && def.getPropertyRequired() == required)
                         && isPropertyNotSet(def.getName())
                        && def.getDefaultObject(this) == null && def.getFlowPropertyValueProvider() == null) {
                     result.addTracking(new MissingRequiredTracking(def.getUiComponentParameterName()));
                 }
             }
         }
         return result;
     }
 
     /**
      * Helps describing 'missing value' problems.
      *
      * @param result keeps track of validation results
      * @param value if true then the property is *NOT set correctly and we need
      *        a {@link MissingRequiredTracking}.
      * @param property missing property's name
      */
     protected void appendRequiredTrackingIfTrue(FlowValidationResult result, boolean value,
             String property) {
         if (value) {
             result.addTracking(new MissingRequiredTracking(property));
         }
     }
 
     /**
      * Helps describing 'incorrect value' problems.
      *
      * @param result keeps track of validation results
      * @param value if true then we need to inform of an inconsistency (using
      *        {@link InconsistencyTracking}) described be the key and data
      *        parameters.
      * @param key The key that describes the inconsistency.
      * @param data Additional values to use for generating the message that
      *        describes the problem.
      */
     protected void appendInconsistencyTrackingIfTrue(FlowValidationResult result, boolean value,
             String key, String... data) {
         if (value) {
             result.addTracking(new InconsistencyTracking(key, data));
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#setActivityName(java.lang.String)
      */
     public void setActivityName(String activityName) {
         this.activityName = activityName;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getActivityName()
      */
     public String getActivityName() {
         return activityName;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#setActivityTitle(java.lang.String)
      */
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
             return "message:"+ "flow.activity." + FlowUtils.INSTANCE.toLowerCase(this.getActivityName() ) +".title";
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getFullActivityName()
      */
     public String getFullActivityName() {
         return this.getFlow().getFlowTypeName() + "." + getActivityName();
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#setActivatable(boolean)
      */
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
 
     /**
      * @see org.amplafi.flow.FlowActivity#setFinishingActivity(boolean)
      */
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
 
     /**
      * @see org.amplafi.flow.FlowActivity#getFlowManagement()
      */
     public FlowManagement getFlowManagement() {
         return this.getFlowState() == null ? null : this.getFlowState().getFlowManagement();
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getFlowState()
      */
     public FlowState getFlowState() {
         return flow == null? null:flow.getFlowState();
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
         instance.definitionFlowActivity = this;
         return instance;
     }
 
     protected <T extends FlowActivityImpl>void copyTo(T instance) {
         instance.activatable = activatable;
         instance.activityName = activityName;
         instance.activityTitle = activityTitle;
         instance.componentName = componentName;
         instance.pageName = pageName;
         instance.finishingActivity = finishingActivity;
         instance.invisible = invisible;
         instance.persistFlow = persistFlow;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivityImplementor#setPropertyDefinitions(java.util.Map)
      */
     public void setPropertyDefinitions(Map<String, FlowPropertyDefinition> properties) {
         propertyDefinitions = properties;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getPropertyDefinitions()
      */
     public Map<String, FlowPropertyDefinition> getPropertyDefinitions() {
         if (propertyDefinitions == null && isInstance()) {
             // as is usually the case for instance flow activities.
             return definitionFlowActivity.getPropertyDefinitions();
         }
         return propertyDefinitions;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#isInstance()
      */
     public boolean isInstance() {
         return definitionFlowActivity != null;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#initInvisible()
      */
     public FlowActivityImpl initInvisible() {
         setInvisible(true);
         return this;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#setInvisible(boolean)
      */
     public void setInvisible(boolean invisible) {
         // HACK -- larger problem this value is cached so the next FA looks invisible as well.
         // good test case complete the registration of a new user which transitions to the FinishSignUp.
         // the ChangePasswordFA acts as if it is invisible because of cached "faInvisible" value = true.
 //        this.setProperty(FAINVISIBLE, invisible);
         this.invisible = invisible;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#isInvisible()
      */
     public boolean isInvisible() {
         if (isPropertyNotSet(FAINVISIBLE)) {
             return invisible;
         } else {
             return isTrue(FAINVISIBLE);
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#setPersistFlow(boolean)
      */
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
      * @see org.amplafi.flow.FlowActivity#getPropertyDefinition(java.lang.String)
      */
     public FlowPropertyDefinition getPropertyDefinition(String key) {
         FlowPropertyDefinition propertyDefinition = getLocalPropertyDefinition(key);
         if (propertyDefinition == null) {
             propertyDefinition = getFlowPropertyDefinition(key);
         }
         return propertyDefinition;
     }
 
     private FlowPropertyDefinition getFlowPropertyDefinition(String key) {
         FlowPropertyDefinition flowPropertyDefinition = null;
         if ( this.getFlowState() != null) {
             flowPropertyDefinition = getFlowState().getFlowPropertyDefinition(key);
         }
         // should be else if
         if ( flowPropertyDefinition == null && this.getFlow() != null) {
             flowPropertyDefinition = this.getFlow().getPropertyDefinition(key);
         }
         return flowPropertyDefinition;
     }
 
     private FlowPropertyDefinition getLocalPropertyDefinition(String key) {
         Map<String, FlowPropertyDefinition> propDefs = this.getPropertyDefinitions();
         FlowPropertyDefinition def = null;
         if (propDefs != null) {
             def = propDefs.get(key);
         }
         return def;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivityImplementor#addPropertyDefinition(org.amplafi.flow.FlowPropertyDefinition)
      */
     public void addPropertyDefinition(FlowPropertyDefinition definition) {
         FlowPropertyDefinition currentLocal;
         if (propertyDefinitions == null) {
             propertyDefinitions = new LinkedHashMap<String, FlowPropertyDefinition>();
             currentLocal = null;
         } else {
             currentLocal = getLocalPropertyDefinition(definition.getName());
         }
         // check against the FlowPropertyDefinition
         if (!definition.merge(currentLocal)) {
             getLog().warn(this.getFlow().getFlowTypeName()
                                     + "."
                                     + this.getActivityName()
                                     + " has a FlowPropertyDefinition '"
                                     + definition.getName()
                                     + "' that conflicts with previous definition. Previous definition discarded.");
             propertyDefinitions.remove(definition.getName());
         }
         if (!definition.isLocal()) {
             // this property may be from the Flow definition itself.
             FlowPropertyDefinition current = this.getFlowPropertyDefinition(definition.getName());
             if (!definition.merge(current)) {
                 getLog().warn(this.getFlow().getFlowTypeName()
                                         + "."
                                         + this.getActivityName()
                                         + " has a FlowPropertyDefinition '"
                                         + definition.getName()
                                         + "' that conflicts with flow's definition. The FlowActivity's definition will be marked as local only.");
                 definition.setPropertyUsage(activityLocal);
             } else {
                 pushPropertyDefinitionToFlow(definition);
             }
         }
         propertyDefinitions.put(definition.getName(), definition);
     }
 
     protected void pushPropertyDefinitionToFlow(FlowPropertyDefinition definition) {
         if (getFlow() != null && !definition.isLocal()) {
             FlowPropertyDefinition flowProp = this.getFlow().getPropertyDefinition(
                     definition.getName());
             if (flowProp == null ) {
                 // push up to flow so that other can see it.
                 FlowPropertyDefinition cloned = definition.clone();
                 // a FPD may be pushed so for an earlier FA may not require the property be set.
                 cloned.setRequired(false);
                 this.getFlow().addPropertyDefinition(cloned);
             } else if ( flowProp.isMergeable(definition)) {
                 flowProp.merge(definition);
                 flowProp.setRequired(false);
             }
         }
     }
 
     protected void pushPropertyDefinitionsToFlow() {
         if (MapUtils.isNotEmpty(propertyDefinitions) && getFlow() != null) {
             for (FlowPropertyDefinition definition : propertyDefinitions.values()) {
                 pushPropertyDefinitionToFlow(definition);
             }
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivityImplementor#addPropertyDefinitions(org.amplafi.flow.FlowPropertyDefinition[])
      */
     public void addPropertyDefinitions(FlowPropertyDefinition... definitions) {
         for (FlowPropertyDefinition definition : definitions) {
             this.addPropertyDefinition(definition);
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivityImplementor#addPropertyDefinitions(java.lang.Iterable)
      */
     public void addPropertyDefinitions(Iterable<FlowPropertyDefinition> definitions) {
         for (FlowPropertyDefinition def : definitions) {
             addPropertyDefinition(def);
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivityImplementor#setFlow(org.amplafi.flow.Flow)
      */
     public void setFlow(Flow flow) {
         this.flow = flow;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getIndex()
      */
     public int getIndex() {
         return flow.indexOf(this);
     }
 
     public boolean isPropertyNotSet(String key) {
         return getRawProperty(key) == null;
     }
 
     public boolean isPropertySet(String key) {
         return getRawProperty(key) != null;
     }
 
     public boolean isPropertyNotBlank(String key) {
         String v = getRawProperty(key);
         return isNotBlank(v);
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
         return getFlowState().getRawProperty(this, key);
     }
 
     /**
      *
      * @see org.amplafi.flow.FlowActivityImplementor#setRawProperty(java.lang.String, java.lang.String)
      */
     public boolean setRawProperty(String key, String value) {
         String oldValue = getFlowState().getProperty(this.getActivityName(), key);
         if (oldValue == null) {
             return getFlowState().setProperty(key, value);
         } else {
             return getFlowState().setProperty(this.getActivityName(), key, value);
         }
     }
 
     public Boolean getRawBoolean(String key) {
         String value = getRawProperty(key);
         return Boolean.parseBoolean(value);
     }
 
     public Long getRawLong(String key) {
         return getFlowState().getRawLong(this, key);
     }
 
     public void setRawLong(String key, Long value) {
         this.setRawProperty(key, value != null ? value.toString() : null);
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
     public <T> T getProperty(String key) {
         T result = (T) getCached(key);
         if (result == null) {
             FlowPropertyDefinition flowPropertyDefinition = getPropertyDefinition(key);
             if (flowPropertyDefinition == null) {
                 flowPropertyDefinition = getFlowState().createFlowPropertyDefinition(key, null, null);
             }
             result = (T) getFlowState().getProperty(this, flowPropertyDefinition);
         }
         return result;
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#getProperty(java.lang.Class)
      */
     @SuppressWarnings("unchecked")
     public <T> T getProperty(Class<T> dataClass) {
         return (T) getProperty(FlowUtils.INSTANCE.toPropertyName(dataClass));
     }
 
     public String getString(String key) {
         return getProperty(key);
     }
 
     public Boolean getBoolean(String key) {
         return getProperty(key);
     }
 
     public boolean isTrue(String key) {
         Boolean b = getBoolean(key);
         return b != null && b;
     }
 
     public boolean isFalse(String key) {
         return !isTrue(key);
     }
 
     /**
      *
      * @see org.amplafi.flow.FlowActivity#setProperty(java.lang.String, java.lang.Object)
      */
     public <T> void setProperty(String key, T value) {
         Class<T> expected = (Class<T>) (value == null?null:value.getClass());
         if ( getFlowState() != null) {
             FlowPropertyDefinition propertyDefinition = getPropertyDefinition(key);
             if (propertyDefinition == null ) {
                 propertyDefinition = getFlowState().createFlowPropertyDefinition(key, expected, value);
             }
             getFlowState().setProperty(this, propertyDefinition, value);
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#setProperty(Object)
      */
     public <T> void setProperty(T value) {
         if (value == null) {
             throw new IllegalArgumentException("value must not be null");
         } else {
             setProperty(value.getClass(), value);
         }
     }
 
     /**
      * @see org.amplafi.flow.FlowActivity#setProperty(Class, Object)
      */
     public <T> void setProperty(Class<? extends T> dataClass, T value) {
         setProperty(FlowUtils.INSTANCE.toPropertyName(dataClass), value);
     }
 
 
     @SuppressWarnings("unused")
     public String propertyChange(String flowActivityName, String key, String value,
             String oldValue) {
         return value;
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
     protected <T> T cache(String key, T value) {
         getFlowState().setCached(key, value);
         return value;
     }
 
     @SuppressWarnings("unchecked")
     protected <T> T getCached(String key) {
         FlowState flowState = getFlowState();
         return flowState == null ? null : (T) flowState.getCached(key);
     }
 
     @Override
     public String toString() {
         Flow f = getFlow();
         if ( f != null ) {
             return f.getFlowTypeName()+"."+activityName+ " " +getClass()+" id="+super.toString();
         } else {
             return activityName+ " " +getClass()+" id="+super.toString();
         }
     }
 
     /**
      * If the property has no value stored in the flowState's keyvalueMap then
      * put the supplied value in it.
      *
      * @param key
      * @param value
      * @see #isPropertyNotSet(String)
      */
     public void initPropertyIfNull(String key, Object value) {
         if (isPropertyNotSet(key)) {
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
     public String resolve(String value) {
         if (value != null && value.startsWith(FLOW_PROPERTY_PREFIX)) {
             return getString(value.substring(FLOW_PROPERTY_PREFIX.length()));
         } else {
             return value;
         }
     }
 
     protected String getResolvedProperty(String key) {
         return resolve(getString(key));
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
 
     protected void redirectTo(String uri) {
         setProperty(FlowConstants.FSREDIRECT_URL, uri);
     }
 
     /**
      * HACK ... should only be called from  {@link #addStandardFlowPropertyDefinitions()} NOT {@link #initializeFlow()}
      * this is because adding to standard properties will not happen correctly ( the {@link org.amplafi.flow.FlowTranslatorResolver} is
      * visible.
      * other wise will affect the definitions.
      * see #2179 / #2192
      */
     protected void handleFlowPropertyValueProvider(String key, FlowPropertyValueProvider flowPropertyValueProvider) {
         FlowPropertyDefinition flowPropertyDefinition = this.getLocalPropertyDefinition(key);
         if ( flowPropertyDefinition != null) {
             if ( flowPropertyValueProvider instanceof ChainedFlowPropertyValueProvider) {
                 ((ChainedFlowPropertyValueProvider)flowPropertyValueProvider).setPrevious(flowPropertyDefinition.getFlowPropertyValueProvider());
             }
             flowPropertyDefinition.setFlowPropertyValueProvider(flowPropertyValueProvider);
         }
         flowPropertyDefinition = this.getFlowPropertyDefinition(key);
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
