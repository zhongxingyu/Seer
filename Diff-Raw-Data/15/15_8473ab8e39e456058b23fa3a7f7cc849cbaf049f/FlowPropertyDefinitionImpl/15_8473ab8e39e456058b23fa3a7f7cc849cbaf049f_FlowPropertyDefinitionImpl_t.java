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
 
 import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
 import static org.apache.commons.lang.StringUtils.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.amplafi.flow.*;
 import org.amplafi.json.JsonSelfRenderer;
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.builder.EqualsBuilder;
 
 import com.sworddance.util.ApplicationIllegalArgumentException;
 import com.sworddance.util.ApplicationNullPointerException;
 
 
 /**
  * Defines a property that will be assigned as part of a {@link Flow} or
  * {@link FlowActivity}. This allows the value to be available to the component
  * or page referenced by a {@link FlowActivity}.
  *
  * TODO:
  * * FPD have a flag that makes them read-only
  * * if the FPD is "changed" then then method that changes the FPD should returned a cloned FPD with the modification. (see the FPD.init* methods() )
  * * necessary to allow instances of Flows to modify the values on a per flow-instance basis.
  *
  * @author Patrick Moore
  */
 public class FlowPropertyDefinitionImpl implements FlowPropertyDefinitionImplementor, FlowPropertyDefinitionProvider {
 
     /**
      * Name of the property as used in the flow code.
      */
     private String name;
 
     /**
      * when a flow starts this should be the initial value set unless there is
      * already a value.
      */
     private String initial;
 
     /**
      * Used when there is no explicit flowPropertyValueProvider. Primary usecase is FlowProperties that have a default
      * way of determining their value. But wish to allow that default method to be changed. (for example, fsFinishText )
      */
     private FlowPropertyValueProvider<FlowPropertyProvider> factoryFlowPropertyValueProvider;
     private FlowPropertyValueProvider<FlowPropertyProvider> flowPropertyValueProvider;
     private FlowPropertyValuePersister<FlowPropertyProvider> flowPropertyValuePersister;
     private List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners = new CopyOnWriteArrayList<FlowPropertyValueChangeListener>();
     /**
      * Used if the UI component's parameter name is different from the FlowPropertyDefinition's name.
      * Useful when using a FlowActivity with components that cannot be changed or have not been changed.
      * For example, a standard tapestry or tacos component.
      * Or a component that is used in multiple places and changing the UI component itself could cause a ripple of
      * cascading problems and possible regressions.
      */
     private String uiComponentParameterName;
 
     /**
      * on {@link FlowActivity#passivate(boolean, FlowStepDirection)} the object should be saved
      * back. This allows complex object to have their string representation
      * saved. Necessary for cases when setProperty() is not used to save value
      * changes, because the object stored in the property is being modified not
      * the usual case of where a new object is saved.
      */
     private Boolean saveBack;
 
     /**
      * if the property does not exist in the cache then create a new instance.
      * As a result, the property will never return a null.
      */
     private Boolean autoCreate;
 
     private DataClassDefinitionImpl dataClassDefinition;
 
     private Set<String> alternates;
 
     /**
      * data that should not be outputted or saved. for example, passwords.
      */
     private PropertySecurity propertySecurity;
 
     /**
      * A string meaningful to the UI framework
      */
     private String validators;
     private FlowActivityPhase flowActivityPhase;
     private PropertyUsage propertyUsage;
     private PropertyScope propertyScope;
     /**
      * once set no further changes to this {@link FlowPropertyDefinitionImpl} are permitted.
      * calling init* methods will return a new {@link FlowPropertyDefinitionImpl} that can be modified.
      * calling set* will result in an exception.
      */
     private boolean templateFlowPropertyDefinition;
 
     /**
      * Creates an unnamed String property.
      */
     public FlowPropertyDefinitionImpl() {
         dataClassDefinition = new DataClassDefinitionImpl();
     }
 
     public FlowPropertyDefinitionImpl(FlowPropertyDefinitionImpl clone) {
         this.setName(clone.name);
         dataClassDefinition = new DataClassDefinitionImpl(clone.dataClassDefinition);
         if (isNotEmpty(clone.alternates)) {
             alternates = new HashSet<String>();
             alternates.addAll(clone.alternates);
         }
         autoCreate = clone.autoCreate;
         this.factoryFlowPropertyValueProvider = clone.factoryFlowPropertyValueProvider;
         this.flowPropertyValueProvider = clone.flowPropertyValueProvider;
         this.flowPropertyValuePersister = clone.flowPropertyValuePersister;
         this.setInitial(clone.initial);
         this.setUiComponentParameterName(clone.uiComponentParameterName);
         if (clone.propertySecurity != null) {
             this.setPropertySecurity(clone.propertySecurity);
         }
         validators = clone.validators;
         saveBack = clone.saveBack;
         this.flowActivityPhase = clone.flowActivityPhase;
         this.propertyUsage = clone.propertyUsage;
         this.propertyScope = clone.propertyScope;
     }
 
     /**
      * Creates an optional string property.
      *
      * @param name The name of the property.
      */
     public FlowPropertyDefinitionImpl(String name) {
         dataClassDefinition = new DataClassDefinitionImpl();
         this.setName(name);
     }
 
     /**
      * Creates an optional property of the given type.
      *
      * @param name
      * @param dataClass
      * @param collectionClasses
      */
     public FlowPropertyDefinitionImpl(String name, Class<?> dataClass, Class<?>...collectionClasses) {
         this(name, dataClass, FlowActivityPhase.optional, collectionClasses);
     }
 
     /**
      * Creates a string property of the given requirements.
      *
      * @param name
      * @param required
      */
     public FlowPropertyDefinitionImpl(String name, FlowActivityPhase required) {
         this(name, null, required);
     }
 
     /**
      * Creates a property of the given type and requirements.
      *
      * @param name
      * @param dataClass the underlying class that all the collection classes are wrapping.
      * @param required how required is this property?
      * @param collectionClasses
      */
     public FlowPropertyDefinitionImpl(String name, Class<? extends Object> dataClass, FlowActivityPhase required, Class<?>...collectionClasses) {
         this(name, required, new DataClassDefinitionImpl(dataClass, collectionClasses));
     }
     public FlowPropertyDefinitionImpl(String name, DataClassDefinitionImpl dataClassDefinition) {
         this(name, FlowActivityPhase.optional, dataClassDefinition);
     }
     public FlowPropertyDefinitionImpl(String name, FlowActivityPhase required, DataClassDefinitionImpl dataClassDefinition) {
         this.setName(name);
         this.flowActivityPhase = required;
         this.dataClassDefinition = dataClassDefinition;
     }
 
     @SuppressWarnings("unchecked")
     public void setDefaultObject(Object defaultObject) {
         if ( !(defaultObject instanceof FlowPropertyValueProvider<?>)) {
             FixedFlowPropertyValueProvider<FlowPropertyProvider> fixedFlowPropertyValueProvider = new FixedFlowPropertyValueProvider<FlowPropertyProvider>(defaultObject);
             fixedFlowPropertyValueProvider.convertable(this);
             if (getDataClassDefinition().isDataClassDefined()) {
                 if (!this.getDataClass().isPrimitive()) {
                     // really need to handle the autobox issue better.
                     this.getDataClass().cast(defaultObject);
                 }
             } else if (defaultObject.getClass() != String.class) {
                 setDataClass(defaultObject.getClass());
             }
             this.factoryFlowPropertyValueProvider = fixedFlowPropertyValueProvider;
         } else {
             this.factoryFlowPropertyValueProvider = (FlowPropertyValueProvider<FlowPropertyProvider>)defaultObject;
         }
     }
 
     /**
      *
      * @param flowPropertyProvider
      * @return defaultObject Should not save default object in
      *         {@link FlowPropertyDefinitionImpl} if it is mutable.
      */
     public Object getDefaultObject(FlowPropertyProvider flowPropertyProvider) {
         Object value = null;
         if ( flowPropertyProvider == null ) {
             throw new ApplicationNullPointerException("flowPropertyProvider cannot be null");
         }
         FlowPropertyValueProvider<FlowPropertyProvider> propertyValueProvider = getFlowPropertyValueProviderToUse();
         if ( propertyValueProvider != null) {
             Class<FlowPropertyProvider> expected = propertyValueProvider.getFlowPropertyProviderClass();
             if (  expected.isAssignableFrom(flowPropertyProvider.getClass())) {
                 value = propertyValueProvider.get(flowPropertyProvider, this);
             } else {
                 throw new ApplicationIllegalArgumentException("expected a ", expected, " but got a ", flowPropertyProvider.getClass());
             }
         } else {
             // TODO -- may still want to call this if flowPropertyValueProvider returns null.
             // for example the property type is a primitive.
             value = this.getDataClassDefinition().getFlowTranslator().getDefaultObject(flowPropertyProvider);
         }
         // TODO -- do we want to set the default object? or recalculate it each time?
         // might be important if the default object is to get modified or if a FPD is shared.
         return value;
     }
 
     public boolean isDefaultObjectAvailable(FlowPropertyProvider flowPropertyProvider) {
         FlowPropertyValueProvider<FlowPropertyProvider> propertyValueProvider = getFlowPropertyValueProviderToUse();
         if ( propertyValueProvider != null) {
             Class<FlowPropertyProvider> expected = propertyValueProvider.getFlowPropertyProviderClass();
             if (  expected.isAssignableFrom(flowPropertyProvider.getClass())) {
                 return true;
             } else {
                 return false;
             }
         } else {
             // slight HACK as we should really check first and not actually get the default object.
             return false;//this.getDataClassDefinition().getFlowTranslator().getDefaultObject(flowPropertyProvider) != null;
         }
     }
 
     /**
      * @return
      */
     private FlowPropertyValueProvider<FlowPropertyProvider> getFlowPropertyValueProviderToUse() {
         if ( this.flowPropertyValueProvider != null) {
             return this.flowPropertyValueProvider;
         } else {
             return this.factoryFlowPropertyValueProvider;
         }
     }
 
     public FlowPropertyDefinitionImpl initDefaultObject(Object defaultObject) {
         FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.factoryFlowPropertyValueProvider, defaultObject);
         flowPropertyDefinition.setDefaultObject(defaultObject);
         return flowPropertyDefinition;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public String getValidators() {
         return validators;
     }
 
     public void setValidators(String validators) {
         this.validators = validators;
     }
 
     // TODO fix with template check
     public FlowPropertyDefinitionImpl addValidator(String validator) {
         if (StringUtils.isBlank(validators)) {
             setValidators(validator);
         } else {
             validators += "," + validator;
         }
         return this;
     }
 
     public FlowPropertyDefinitionImpl validateWith(String... fields) {
         addValidator("flowField="+join(fields,"-"));
         return this;
     }
 
     /**
      * Sets validators for this definition. <p/>
      * Note that existing validators will be removed - if you
      * don't want that behavior, consider using {@link #addValidator(String)}.
      * @param validators
      * @return this
      */
     @SuppressWarnings("hiding")
     public FlowPropertyDefinitionImpl initValidators(String validators) {
         FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.validators, validators);
         flowPropertyDefinition.setValidators(validators);
         return flowPropertyDefinition;
     }
 
     @Override
     public FlowTranslator<?> getTranslator() {
         return this.getDataClassDefinition().getFlowTranslator();
     }
     public void setTranslator(FlowTranslator<?> flowTranslator) {
         this.getDataClassDefinition().setFlowTranslator(flowTranslator);
     }
 
     public FlowPropertyDefinitionImpl initTranslator(FlowTranslator<?> flowTranslator) {
         setTranslator(flowTranslator);
         return this;
     }
 
     /**
      * This is used to handle case of parameter name change and 'short' names
      * for uris and the like.
      *
      * @param alternateNames
      * @return this
      */
     public FlowPropertyDefinitionImpl addAlternateNames(String... alternateNames) {
         getAlternates().addAll(Arrays.asList(alternateNames));
         return this;
     }
 
     public void setInitial(String initial) {
         this.initial = initial;
         checkInitial(this.initial);
     }
 
     public String getInitial() {
         return initial;
     }
 
     public FlowPropertyDefinitionImpl initInitial(String initialValue) {
         FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.initial, initialValue);
         flowPropertyDefinition.setInitial(initialValue);
         return flowPropertyDefinition;
     }
 
     public boolean isLocal() {
         return this.getPropertyScope().isLocalToActivity();
     }
 
     public void setUiComponentParameterName(String parameterName) {
         this.uiComponentParameterName = parameterName;
     }
 
     public String getUiComponentParameterName() {
         if (uiComponentParameterName == null) {
             return getName();
         }
         return uiComponentParameterName;
     }
 
     public FlowPropertyDefinitionImpl initParameterName(String parameterName) {
         setUiComponentParameterName(parameterName);
         return this;
     }
 
     public void setAutoCreate(boolean autoCreate) {
         this.autoCreate = autoCreate;
     }
 
     public boolean isAutoCreate() {
         if (isDefaultAvailable()) {
             return true;
         }
         return isDefaultByClassAvailable();
     }
 
     /**
      * @return
      */
     private boolean isDefaultByClassAvailable() {
         if ( autoCreate != null) {
             return getBoolean(autoCreate);
         }
         if (!getDataClassDefinition().isCollection() ) {
             Class<?> dataClass = getDataClassDefinition().getDataClass();
             if (dataClass.isPrimitive() ) {
                 return true;
             } else if (dataClass.isInterface() || dataClass.isEnum()) {
                 return false;
             }
         }
         return false;
     }
     public boolean isDefaultAvailable() {
         return this.getFlowPropertyValueProviderToUse() != null;
     }
 
     @Override
     public String toString() {
         return toComponentDef() +"(scope="+getPropertyScope()+", usage="+getPropertyUsage()+") :"+this.dataClassDefinition;
     }
 
     public String toComponentDef() {
         return getUiComponentParameterName();
     }
 
     public static String toString(String paramName, String flowPropName) {
         return " " + paramName + "=\"fprop:" + flowPropName + "\" ";
     }
 
     public <T> String serialize(T object) {
         if ( this.getDataClassDefinition().getFlowTranslator() == null) {
             return null;
         } else {
             Object serialized = this.getDataClassDefinition().serialize(this, object);
             return ObjectUtils.toString(serialized, null);
         }
     }
 
     @SuppressWarnings("unchecked")
     public <V> V parse(String value) throws FlowException {
         return (V) this.getDataClassDefinition().deserialize(this, value);
     }
     /**
      * @param flowActivityPhase the propertyRequired to set
      * @return this
      */
     @SuppressWarnings("hiding")
     public FlowPropertyDefinitionImpl initPropertyRequired(FlowActivityPhase flowActivityPhase) {
         this.setPropertyRequired(flowActivityPhase);
         return this;
     }
     /**
      * @param flowActivityPhase the propertyRequired to set
      */
     public void setPropertyRequired(FlowActivityPhase flowActivityPhase) {
         this.flowActivityPhase = flowActivityPhase;
     }
 
     /**
      * @return the propertyRequired
      */
     public FlowActivityPhase getPropertyRequired() {
         return flowActivityPhase == null?FlowActivityPhase.optional:flowActivityPhase;
     }
 
     /**
      * @param propertyUsage the propertyUsage to set
      */
     public void setPropertyUsage(PropertyUsage propertyUsage) {
         if ( this.getPropertyScope().isAllowedPropertyUsage(propertyUsage)) {
             this.propertyUsage = setCheckTemplateState(this.propertyUsage, propertyUsage);
         } else {
             // throw exception?
         }
     }
 
     /**
      * @return the propertyUsage (default {@link PropertyUsage#consume}
      */
     public PropertyUsage getPropertyUsage() {
         if ( isPropertyUsageSet() ) {
             return propertyUsage;
         } else {
             return this.getPropertyScope().getDefaultPropertyUsage();
         }
     }
 
     /**
      * @return true if the propertyUsage is explicitly set.
      */
     public boolean isPropertyUsageSet() {
         return this.propertyUsage != null;
     }
     @SuppressWarnings("hiding")
     public FlowPropertyDefinitionImpl initPropertyUsage(PropertyUsage propertyUsage) {
         setPropertyUsage(propertyUsage);
         return this;
     }
 
     /**
      * @param propertyScope the propertyScope to set
      */
     public void setPropertyScope(PropertyScope propertyScope) {
         this.propertyScope = setCheckTemplateState(this.propertyScope, propertyScope);
     }
 
     /**
      * @return the propertyScope
      */
     public PropertyScope getPropertyScope() {
         return isPropertyScopeSet()?propertyScope:PropertyScope.flowLocal;
     }
     public boolean isPropertyScopeSet() {
         return propertyScope != null;
     }
     @SuppressWarnings("hiding")
     public FlowPropertyDefinitionImpl initPropertyScope(PropertyScope propertyScope) {
         setPropertyScope(propertyScope);
         return this;
     }
     @SuppressWarnings("hiding")
     public FlowPropertyDefinitionImpl initAccess(PropertyScope propertyScope, PropertyUsage propertyUsage) {
         return initPropertyScope(propertyScope).initPropertyUsage(propertyUsage);
     }
 
     /**
      * affects isEntity()
      *
      * @param dataClass
      */
     public void setDataClass(Class<? extends Object> dataClass) {
         if (dataClass == String.class) {
             dataClass = null;
         }
         this.getDataClassDefinition().setDataClass(dataClass);
     }
 
     // HACK -- to fix later...
     public FlowPropertyDefinition initialize() {
         checkInitial(this.getInitial());
         return this;
     }
 
     /**
      * make sure the initial value can be deserialized to the expected type.
      */
     private void checkInitial(String value) {
         if ( !this.getDataClassDefinition().isDeserializable(this, value)) {
             throw new IllegalStateException(this + " while checking initial value="+ value);
         }
     }
 
     /**
      * @param dataClassDefinition the dataClassDefinition to set
      */
     public void setDataClassDefinition(DataClassDefinitionImpl dataClassDefinition) {
         this.dataClassDefinition = dataClassDefinition;
     }
 
     /**
      * @return the dataClassDefinition
      */
     public DataClassDefinitionImpl getDataClassDefinition() {
         return dataClassDefinition;
     }
 
     public Class<? extends Object> getDataClass() {
         return getDataClassDefinition().getDataClass();
     }
 
     public Class<? extends Object> getCollectionClass() {
         return this.getDataClassDefinition().getCollection();
     }
 
     public Set<String> getAlternates() {
         if (alternates == null) {
             alternates = new HashSet<String>();
         }
         return alternates;
     }
     public Set<String> getAllNames() {
         Set<String> allNames = new LinkedHashSet<String>();
         allNames.add(this.getName());
         allNames.addAll(getAlternates());
         return allNames;
     }
 
     public void setPropertySecurity(PropertySecurity propertySecurity) {
         this.propertySecurity = propertySecurity;
         // TODO: rethink on handling this (commented out in order to access
         // values)
         /*
          * if ( sensitive) { this.cacheOnly = true; }
          */
     }
 
     public PropertySecurity getPropertySecurity() {
         return propertySecurity != null?propertySecurity:getPropertyScope().getDefaultPropertySecurity();
     }
 
     public FlowPropertyDefinitionImpl initSensitive() {
         setPropertySecurity(PropertySecurity.noAccess);
         return this;
     }
 
     public boolean isSensitive() {
         return !getPropertySecurity().isExternalReadAccessAllowed();
     }
     /**
      * not a reversible process.
      */
     public void setTemplateFlowPropertyDefinition() {
         this.templateFlowPropertyDefinition = true;
     }
 
     /**
      * @return the templateFlowPropertyDefinition
      */
     public boolean isTemplateFlowPropertyDefinition() {
         return templateFlowPropertyDefinition;
     }
     protected <T> T setCheckTemplateState(T oldObject, T newObject) {
         if ( templateFlowPropertyDefinition && !ObjectUtils.equals(oldObject, newObject)) {
             throw new IllegalStateException("Cannot change state of a Template FlowPropertyDefinition");
         }
         return newObject;
     }
     protected <T> FlowPropertyDefinitionImpl cloneIfTemplate(T oldObject, T newObject) {
         if ( !ObjectUtils.equals(oldObject, newObject) && templateFlowPropertyDefinition) {
             return this.clone();
         }
         return this;
     }
 
     public boolean isAssignableFrom(Class<?> clazz) {
         return this.getDataClassDefinition().isAssignableFrom(clazz);
     }
 
     public boolean isDataClassMergeable(FlowPropertyDefinition flowPropertyDefinition) {
         return getDataClassDefinition().isMergable(((FlowPropertyDefinitionImpl)flowPropertyDefinition).dataClassDefinition);
     }
 
     public boolean isMergeable(FlowPropertyDefinition property) {
         if (!(property instanceof FlowPropertyDefinitionImpl)) {
             return false;
         }
         FlowPropertyDefinitionImpl source = (FlowPropertyDefinitionImpl)property;
         boolean result = isDataClassMergeable(source);
         if ( result ) {
             result &=this.flowPropertyValueProvider == null || source.flowPropertyValueProvider == null || this.flowPropertyValueProvider.equals(source.flowPropertyValueProvider);
             if ( result ) {
                 result &= !isPropertyScopeSet() || !property.isPropertyScopeSet() || getPropertyScope() == property.getPropertyScope();
                 result &= !isPropertyUsageSet() || !property.isPropertyUsageSet()
                     || getPropertyUsage().isChangeableTo(property.getPropertyUsage()) || property.getPropertyUsage().isChangeableTo(getPropertyUsage());
                 if ( !result) {
 //                    System.out.println("scope clash");
                 }
             } else {
 //                System.out.println("provider clash");
             }
         } else {
 //            System.out.println("dataclass clash");
         }
         return result;
     }
 
     /**
      * For any fields that are not already set in this
      * {@link FlowPropertyDefinitionImpl}, this use previous to supply any missing
      * values.
      *
      * TODO: Need to check {@link PropertyScope}!! How to handle activityLocal/flowLocals???
      *
      * @param property
      * @return true if there is no conflict in the dataClass, true if
      *         this.dataClass cannot be assigned by previous.dataClass
      *         instances.
      */
     public boolean merge(FlowPropertyDefinition property) {
         if ( property == null) {
             return true;
         } else if ( !isDataClassMergeable(property)) {
             return false;
         }
         FlowPropertyDefinitionImpl source = (FlowPropertyDefinitionImpl)property;
         boolean noMergeConflict = isMergeable(source);
         if (autoCreate == null && source.autoCreate != null) {
             this.setAutoCreate(source.autoCreate);
         }
         this.getDataClassDefinition().merge(source.dataClassDefinition);
 
         if ( isNotEmpty(source.alternates)) {
             if (alternates == null ) {
                 alternates = new HashSet<String>(source.alternates);
             } else {
                 alternates.addAll(source.alternates);
             }
         }
 
         if ( flowPropertyValueProvider == null && source.flowPropertyValueProvider != null ) {
             this.setFlowPropertyValueProvider(source.flowPropertyValueProvider);
         }
         if ( flowPropertyValuePersister == null && source.flowPropertyValuePersister != null ) {
             this.setFlowPropertyValuePersister(source.flowPropertyValuePersister);
         }
         if ( factoryFlowPropertyValueProvider == null && source.factoryFlowPropertyValueProvider != null ) {
             this.setDefaultObject(source.factoryFlowPropertyValueProvider);
         }
         // TODO: merging should handling chained notification.
         if ( flowPropertyValueChangeListeners == null && source.flowPropertyValueChangeListeners != null ) {
             this.setFlowPropertyValueChangeListeners(source.flowPropertyValueChangeListeners);
         }
         if (initial == null && source.initial != null) {
             this.setInitial(source.initial);
         }
         if (saveBack == null && source.saveBack != null) {
             this.setSaveBack(source.saveBack);
         }
         if (uiComponentParameterName == null && source.uiComponentParameterName != null) {
             uiComponentParameterName = source.uiComponentParameterName;
         }
         if (propertySecurity == null && source.propertySecurity != null) {
             this.setPropertySecurity(source.propertySecurity);
         }
         if (validators == null && source.validators != null) {
             validators = source.validators;
         }
         if ( !isPropertyScopeSet() && source.isPropertyScopeSet()) {
             setPropertyScope(source.propertyScope);
         }
         if ( source.isPropertyUsageSet()) {
             setPropertyUsage(PropertyUsage.survivingPropertyUsage(propertyUsage, source.propertyUsage));
         }
 
         // TODO : determine how to handle propertyRequired / PropertyUsage/PropertyScope/PropertySecurity which vary between different FAs in the same Flow.
         return noMergeConflict;
     }
 
     @Override
     public FlowPropertyDefinitionImpl clone() {
         return new FlowPropertyDefinitionImpl(this);
     }
 
     public void setSaveBack(Boolean saveBack) {
         this.saveBack = saveBack;
     }
 
     public boolean isSaveBack() {
         if (saveBack != null) {
             return getBoolean(saveBack);
         } else {
             return getDataClassDefinition().isCollection() || JsonSelfRenderer.class.isAssignableFrom(getDataClassDefinition().getDataClass());
         }
     }
 
     private boolean getBoolean(Boolean b) {
         return b != null && b;
     }
 
     /**
      * @see #saveBack
      * @param saveBack
      * @return this
      */
     @SuppressWarnings("hiding")
     public FlowPropertyDefinitionImpl initSaveBack(Boolean saveBack) {
         setSaveBack(saveBack);
         return this;
     }
 
     public boolean isCacheOnly() {
         return getPropertyScope().isCacheOnly();
     }
 
     /**
      * Sets autoCreate to true - meaning that if the property does not exist in
      * the cache, a new instance is created. <p/> Uses
      * {@link #setAutoCreate(boolean)}
      *
      * @return this
      */
     public FlowPropertyDefinitionImpl initAutoCreate() {
         setAutoCreate(true);
         return this;
     }
 
     /**
      * @param <FA>
      * @param flowPropertyValueProvider the flowPropertyValueProvider to set
      */
     @SuppressWarnings("unchecked")
     public <FA extends FlowPropertyProvider>void setFlowPropertyValueProvider(FlowPropertyValueProvider<FA> flowPropertyValueProvider) {
         this.flowPropertyValueProvider = (FlowPropertyValueProvider<FlowPropertyProvider>) flowPropertyValueProvider;
     }
 
     /**
      * @param <FA>
      * @return the flowPropertyValueProvider
      */
     @SuppressWarnings("unchecked")
     public <FA extends FlowPropertyProvider> FlowPropertyValueProvider<FA> getFlowPropertyValueProvider() {
         return (FlowPropertyValueProvider<FA>)flowPropertyValueProvider;
     }
 
     /**
      * @param flowPropertyValueProvider
      * @return this
      */
     @SuppressWarnings("hiding")
     public FlowPropertyDefinitionImpl initFlowPropertyValueProvider(FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider) {
         setFlowPropertyValueProvider(flowPropertyValueProvider);
         return this;
     }
 
     /**
      * @return the flowPropertyValuePersister
      */
     public FlowPropertyValuePersister<FlowPropertyProvider> getFlowPropertyValuePersister() {
         return flowPropertyValuePersister;
     }
 
     /**
      * @param <FA>
      * @param flowPropertyValuePersister the flowPropertyValuePersister to set
      */
     @SuppressWarnings("unchecked")
     public <FA extends FlowPropertyProvider> void setFlowPropertyValuePersister(FlowPropertyValuePersister<FA> flowPropertyValuePersister) {
         this.flowPropertyValuePersister = (FlowPropertyValuePersister<FlowPropertyProvider>) flowPropertyValuePersister;
     }
 
     /**
      * @param flowPropertyValuePersister
      * @return this
      */
     @SuppressWarnings("hiding")
     public FlowPropertyDefinitionImpl initFlowPropertyValuePersister(FlowPropertyValuePersister<? extends FlowPropertyProvider> flowPropertyValuePersister) {
         setFlowPropertyValuePersister(flowPropertyValuePersister);
         return this;
     }
 
     /**
      * @return the flowPropertyValueChangeListener
      */
     public List<FlowPropertyValueChangeListener> getFlowPropertyValueChangeListeners() {
         return flowPropertyValueChangeListeners;
     }
 
     /**
      * @param flowPropertyValueChangeListeners the flowPropertyValueChangeListener to set
      */
     public void setFlowPropertyValueChangeListeners(List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners) {
         this.flowPropertyValueChangeListeners.clear();
         this.flowPropertyValueChangeListeners.addAll(flowPropertyValueChangeListeners);
     }
 
     public FlowPropertyDefinitionImpl initFlowPropertyValueChangeListener(FlowPropertyValueChangeListener flowPropertyValueChangeListener) {
         if ( flowPropertyValueChangeListener != null) {
             this.flowPropertyValueChangeListeners.add(flowPropertyValueChangeListener);
         }
         return this;
     }

    public boolean isDynamic() {
        return this.flowPropertyValueChangeListeners != null && !this.flowPropertyValueChangeListeners.isEmpty();
    }
     @Override
     public boolean equals(Object o) {
         if ( o == null || ! (o instanceof FlowPropertyDefinitionImpl)) {
             return false;
         } else if (o == this) {
             return true;
         }
         FlowPropertyDefinitionImpl flowPropertyDefinition = (FlowPropertyDefinitionImpl) o;
         EqualsBuilder equalsBuilder = new EqualsBuilder()
             .append(this.alternates, flowPropertyDefinition.alternates)
             .append(this.autoCreate, flowPropertyDefinition.autoCreate)
             .append(this.dataClassDefinition, flowPropertyDefinition.dataClassDefinition)
             .append(this.flowPropertyValueProvider, flowPropertyDefinition.flowPropertyValueProvider)
             .append(this.initial, flowPropertyDefinition.initial)
             .append(this.name, flowPropertyDefinition.name)
             .append(this.uiComponentParameterName, flowPropertyDefinition.uiComponentParameterName)
                 // use getter so that defaults can be calculated.
             .append(this.getPropertyRequired(), flowPropertyDefinition.getPropertyRequired())
             .append(this.getPropertyUsage(), flowPropertyDefinition.getPropertyUsage())
             .append(this.getPropertyScope(), flowPropertyDefinition.getPropertyScope())
             .append(this.saveBack, flowPropertyDefinition.saveBack)
             .append(this.propertySecurity, flowPropertyDefinition.propertySecurity)
             .append(this.validators, flowPropertyDefinition.validators);
         return equalsBuilder.isEquals();
     }
 
     /**
      * @see org.amplafi.flow.FlowPropertyDefinition#isNamed(java.lang.String)
      */
     @Override
     public boolean isNamed(String possiblePropertyName) {
         if ( isBlank(possiblePropertyName)) {
             return false;
         } else {
             return getName().equals(possiblePropertyName) ||
                 this.getAlternates().contains(possiblePropertyName);
         }
     }
 
     /**
      * Ideally we have a hierarchy of namespaces that can be checked for values. This works o.k. for searching for a value.
      * But problem is once the value is found, should the old value be replaced in whatever name space? should the found value be moved to the more exact namespace?
      *
      * Example, if a activityLocal can look at namespaces={flowstate.lookupKey+activity.activityName, flow.typename+activity.activityname, etc.} should any found value
      * be moved to flowstate.lookupKey+activity.activityName?
      *
      * What about morphing which would change the flowtype and thus the flowtype+activity.activityName namespace value. Morphing would result in loosing activity local values.
      *
      * right now flow morph does loose flowLocal values.
      *
      * Maybe when a flow is created the name space gets set for all flowLocal and activityLocal based on the flowstate lookup key?
      *
      * More thought definitely needed.
      *
      * Seems like the namespace list should vary depending on if we are initializing a flow from external parameters. An external setting would not like
      * to have to worry about using a flowState's lookupKey to set the namespace. Would prefer to just use the key with no namespace, or maybe a more generic
      * namespace like FlowTypeName or ActivityName.
      * @param flowActivity
      * @return list of namespaces to search in order
      */
     private List<String> getNamespaceKeySearchList(FlowActivity flowActivity) {
         //TODO have hierarchy of namespaces
         List<String> list = new ArrayList<String>();
         list.add(getNamespaceKey(flowActivity));
         if ( this.getPropertyUsage().isExternallySettable()) {
             switch(this.getPropertyScope()) {
             case activityLocal:
                 // TODO should really be ? but what about morphing??
     //            list.add(flowActivity.getFullActivityName());
                 list.add(flowActivity.getFlowPropertyProviderName());
             case flowLocal:
             case requestFlowLocal:
                 list.add(flowActivity.getFlow().getFlowPropertyProviderName());
                 list.add(null);
             case global:
                 break;
             default:
                 throw new IllegalStateException(this.getPropertyScope()+": don't know namespace.");
             }
         }
         return list;
     }
     private List<String> getNamespaceKeySearchList(FlowImplementor flow) {
         //TODO have hierarchy of namespaces
         List<String> list = new ArrayList<String>();
         list.add(getNamespaceKey(flow));
         if ( this.getPropertyUsage().isExternallySettable()) {
             switch(this.getPropertyScope()) {
             case flowLocal:
             case requestFlowLocal:
                 list.add(flow.getFlowPropertyProviderName());
                 list.add(null);
             case global:
                 break;
             default:
                 throw new IllegalStateException(this.getPropertyScope()+": don't know namespace.");
             }
         }
         return list;
     }
     private String getNamespaceKey(FlowActivity flowActivity) {
         switch ( this.getPropertyScope()) {
         case activityLocal:
             // TODO should really be ? but what about morphing??
 //            list.add(flowActivity.getFullActivityName());
             return flowActivity.getFullActivityInstanceNamespace();
         case flowLocal:
         case requestFlowLocal:
             return getNamespaceKey((FlowImplementor)flowActivity.getFlow());
         case global:
             return null;
         default:
             throw new IllegalStateException(flowActivity.getFlowPropertyProviderFullName()+":"+this+":"+this.getPropertyScope()+": don't know namespace.");
         }
     }
 
     private String getNamespaceKey(FlowImplementor flow) {
         switch ( this.getPropertyScope()) {
         case flowLocal:
         case requestFlowLocal:
             if ( flow.getFlowState() != null) {
                 return getNamespaceKey(flow.getFlowState());
             } else {
                 return flow.getFlowPropertyProviderName();
             }
         case global:
             return null;
         default:
             throw new IllegalStateException(flow.getFlowPropertyProviderFullName()+":"+this+":"+this.getPropertyScope()+": don't know namespace.");
         }
     }
 
     private String getNamespaceKey(FlowState flowState) {
         switch (this.getPropertyScope()) {
         case activityLocal:
             throw new IllegalStateException(this + ":" + this.getPropertyScope() + " cannot be used when supplying only a FlowState");
         case flowLocal:
         case requestFlowLocal:
             return flowState.getLookupKey();
         case global:
             return null;
         default:
             throw new IllegalStateException(this.getPropertyScope() + ": don't know namespace.");
         }
     }
     /**
      * see notes on {@link #getNamespaceKeySearchList(FlowActivity)}
      * @param flowState
      * @return list of namespace to search in order
      */
     private List<String> getNamespaceKeySearchList(FlowState flowState) {
         //TODO have hierarchy of namespaces
         List<String> list = new ArrayList<String>();
         list.add(getNamespaceKey(flowState));
         switch (this.getPropertyScope()) {
         case activityLocal:
             throw new IllegalStateException(this + ":" + this.getPropertyScope() + " cannot be used when supplying only a FlowState");
         case flowLocal:
         case requestFlowLocal:
             list.add(flowState.getFlow().getFlowPropertyProviderName());
             if ( getPropertyUsage().isExternallySettable()) {
                 list.add(null);
             }
         case global:
             break;
         default:
             throw new IllegalStateException(flowState.getLookupKey()+":"+this+":"+this.getPropertyScope()+": don't know namespace.");
         }
         return list;
     }
 
     public List<String> getNamespaceKeySearchList(FlowState flowState, FlowPropertyProvider flowPropertyProvider) {
         if( flowPropertyProvider == null) {
             return this.getNamespaceKeySearchList(flowState);
         } else if ( flowPropertyProvider instanceof FlowActivity) {
             return this.getNamespaceKeySearchList((FlowActivity)flowPropertyProvider);
         } else if ( flowPropertyProvider instanceof FlowState) {
             FlowImplementor flow = ((FlowState)flowPropertyProvider).getFlow();
             return this.getNamespaceKeySearchList(flow);
         } else {
             return this.getNamespaceKeySearchList((FlowImplementor)flowPropertyProvider);
         }
     }
     public String getNamespaceKey(FlowState flowState, FlowPropertyProvider flowPropertyProvider) {
         if( flowPropertyProvider == null) {
             return this.getNamespaceKey(flowState);
         } else if ( flowPropertyProvider instanceof FlowActivity) {
             return this.getNamespaceKey((FlowActivity)flowPropertyProvider);
         } else if ( flowPropertyProvider instanceof FlowState) {
             FlowImplementor flow = ((FlowState)flowPropertyProvider).getFlow();
             return this.getNamespaceKey(flow);
         } else {
             return this.getNamespaceKey((FlowImplementor)flowPropertyProvider);
         }
     }
 
     /**
      * @see org.amplafi.flow.flowproperty.FlowPropertyDefinitionProvider#defineFlowPropertyDefinitions(org.amplafi.flow.flowproperty.FlowPropertyProviderImplementor, org.amplafi.flow.FlowValuesMap)
      */
     @Override
     public void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider,
         FlowValuesMap<? extends FlowValueMapKey, ? extends CharSequence> additionalConfigurationParameters) {
         flowPropertyProvider.addPropertyDefinition(this);
     }
 
 }
