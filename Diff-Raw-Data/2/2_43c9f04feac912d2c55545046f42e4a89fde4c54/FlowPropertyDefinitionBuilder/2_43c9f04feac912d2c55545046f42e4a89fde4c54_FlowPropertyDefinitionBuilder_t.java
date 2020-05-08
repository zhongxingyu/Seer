 package org.amplafi.flow.flowproperty;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.amplafi.flow.FlowActivityPhase;
 import org.amplafi.flow.FlowPropertyDefinition;
 import org.amplafi.flow.FlowPropertyExpectation;
 import org.amplafi.flow.FlowPropertyValueProvider;
 import org.amplafi.flow.FlowTranslatorResolver;
 import org.amplafi.flow.translator.FlowTranslator;
 
 import com.sworddance.util.AbstractParameterizedCallableImpl;
 import com.sworddance.util.ApplicationIllegalArgumentException;
 import com.sworddance.util.NotNullIterator;
 import com.sworddance.util.map.ConcurrentInitializedMap;
 
 /**
  * Designed to handle the problems of extending standard definitions.
  *
  * Notes:
  * FlowPropertyDefinitionBuilder was introduced because of the problems of having a
  * FlowPropertyDefinition that can be endlessly modified, which then resulted in FPD deciding if
  * they were templates (immutable) or not. With a builder, all {@link FlowPropertyDefinition}s
  * become immutable.
  *
 * TODO: make {@link FlowPropertyDefinitionImpl} immutable.
  * Any changes to a FPD require a builder to construct a new FPD.
  *
  * TODO: ? add a way to make an attempt to read an unset property fail rather than return null? Useful for
  * {@link PropertyUsage#getAltersProperty()} == TRUE
  *
  * TODO: remove initDefaultObject() Handles some common use cases
  *
  * @author patmoore
  */
 public class FlowPropertyDefinitionBuilder {
 
     private FlowPropertyDefinitionImpl flowPropertyDefinition;
 
     private static final Map<Class<?>, String> propertyNameFromClassName = new ConcurrentInitializedMap<>(new AbstractParameterizedCallableImpl<String>() {
         @Override
         public String executeCall(Object... parameters) throws Exception {
             Class<?> clazz = getKeyFromParameters(parameters);
             String simpleName = clazz.getSimpleName();
             String propertyName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
             return propertyName;
         }
 
     });
 
     /**
      * To return a api value,
      * 1) the property must be initialized when the call completes,
      * 2) the property must local to at least flow
      */
     public static List<FlowPropertyExpectation> API_RETURN_VALUE = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(FlowActivityPhase.finish, PropertyScope.flowLocal, PropertyUsage.initialize, ExternalPropertyAccessRestriction.readonly));
     public static List<FlowPropertyExpectation> IO = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(null, null, PropertyUsage.io, null));
     public static List<FlowPropertyExpectation> REQUIRED_INPUT_CONSUMING = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(FlowActivityPhase.activate, null, PropertyUsage.consume, null));
     public static List<FlowPropertyExpectation> CONSUMING = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(null, null, PropertyUsage.consume, null));
     // not externally settable by user but can be set by previous flow.
     public static List<FlowPropertyExpectation> INTERNAL_ONLY = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(null, null, PropertyUsage.internalState, ExternalPropertyAccessRestriction.noAccess));
     public static List<FlowPropertyExpectation> GENERATED_KNOWN_FA = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(null, null, PropertyUsage.consume, ExternalPropertyAccessRestriction.noAccess));
     public FlowPropertyDefinitionBuilder() {
 
     }
     public FlowPropertyDefinitionBuilder(String name) {
         this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name);
     }
     public FlowPropertyDefinitionBuilder(String name, DataClassDefinitionImpl dataClassDefinition) {
         this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name,dataClassDefinition);
     }
 
     public FlowPropertyDefinitionBuilder createFromTemplate(FlowPropertyDefinitionImplementor flowPropertyDefinitionImplementor) {
         this.flowPropertyDefinition = (FlowPropertyDefinitionImpl) flowPropertyDefinitionImplementor.clone();
         return this;
     }
     public FlowPropertyDefinitionBuilder(String name, Class<? extends Object> dataClass, Class<?>... collectionClasses) {
         this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, collectionClasses);
     }
     public FlowPropertyDefinitionBuilder(Class<? extends Object> dataClass, Class<?>... collectionClasses) {
         this(toPropertyName(dataClass), dataClass, collectionClasses);
     }
 
     /**
      * A property that is not allowed to be altered. (no set is allowed) But the property is not
      * immutable because a FPVP could supply different values. Use case: User id Specifically:
      * PropertyScope.flowLocal, PropertyUsage.initialize, ExternalPropertyAccessRestriction.readonly
      * Expectation is that {@link FlowPropertyValueProvider} will be supplied later.
      *
      * @param name
      * @param dataClass
      * @param whenMustBeAvailable
      * @param collectionClasses
      * @return this
      */
     public FlowPropertyDefinitionBuilder createNonalterableFlowPropertyDefinition(String name, Class<? extends Object> dataClass,
         FlowActivityPhase whenMustBeAvailable, Class<?>... collectionClasses) {
         this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, whenMustBeAvailable, collectionClasses).initAccess(
             PropertyScope.flowLocal, PropertyUsage.initialize, ExternalPropertyAccessRestriction.readonly);
         return this;
     }
 
     /**
      * Immutable because a default value is provided that must be used.
      *
      * @param name
      * @param dataClass
      * @param immutableValue
      * @param collectionClasses
      * @return
      */
     public FlowPropertyDefinitionBuilder createImmutableFlowPropertyDefinition(String name, Class<? extends Object> dataClass, Object immutableValue,
         Class<?>... collectionClasses) {
         this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, null, collectionClasses).initAccess(PropertyScope.flowLocal,
             PropertyUsage.initialize, ExternalPropertyAccessRestriction.readonly).initDefaultObject(immutableValue);
         return this;
     }
 
     /**
      * A property that cannot be configured by the initialFlowState map, but is alterable by a
      * {@link FlowPropertyValueProvider} and can be changed by the user during the flow. This
      * property is guaranteed to have a value if the flow completes normally.
      *
      * @param name
      * @param dataClass
      * @param flowPropertyValueProvider
      * @param collectionClasses
      * @return this
      */
     public FlowPropertyDefinitionBuilder createNonconfigurableFlowPropertyDefinition(String name, Class<? extends Object> dataClass,
         FlowPropertyValueProvider flowPropertyValueProvider, Class<?>... collectionClasses) {
         this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, null, collectionClasses).initAccess(PropertyScope.flowLocal,
             PropertyUsage.initialize).initFlowPropertyValueProvider(flowPropertyValueProvider);
         return this;
     }
 
     /**
      * A security property Use case: a password
      *
      * @param name
      * @param dataClass
      * @param collectionClasses
      * @return this
      */
     public FlowPropertyDefinitionBuilder createPasswordFlowPropertyDefinition(String name, Class<? extends Object> dataClass,
         Class<?>... collectionClasses) {
         this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, FlowActivityPhase.activate, collectionClasses).initAccess(
             PropertyScope.flowLocal, PropertyUsage.consume, ExternalPropertyAccessRestriction.writeonly);
         return this;
     }
 
     /**
      * Used to create a value that must be available by the time the flow completes.
      *
      * TODO use {@link #API_RETURN_VALUE}
      * @param name
      * @param dataClass
      * @param collectionClasses
      * @return this
      */
     public FlowPropertyDefinitionBuilder createApiReturnValueFlowPropertyDefinition(String name, Class<? extends Object> dataClass,
         Class<?>... collectionClasses) {
         // HACK - TODO : using FlowActivityPhase.finish forces the property to be set even if not used.
         // May need to fix if properties used in both api flows and as part of another flow.
         this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, FlowActivityPhase.finish, collectionClasses)
             .initAccess(PropertyScope.flowLocal, PropertyUsage.initialize);
         return this;
     }
 
     public FlowPropertyDefinitionBuilder createApiReturnValueFlowPropertyDefinition(String name,
         DataClassDefinitionImpl dataClassDefinition) {
         this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name,dataClassDefinition).initPropertyRequired(FlowActivityPhase.finish)
                 .initAccess(PropertyScope.flowLocal, PropertyUsage.initialize);
         return this;
     }
 
     /**
      * create a {@link FlowPropertyDefinition} for a property whose value is recomputed for every
      * request. Use case: very dynamic properties for example, a status message.
      *
      * @param name
      * @param dataClass
      * @param collectionClasses
      * @return this
      */
     public FlowPropertyDefinitionBuilder createCurrentRequestOnlyFlowPropertyDefinition(String name, Class<? extends Object> dataClass,
         Class<?>... collectionClasses) {
         this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, FlowActivityPhase.optional, collectionClasses).initAccess(
             PropertyScope.requestFlowLocal, PropertyUsage.suppliesIfMissing);
         return this;
     }
 
     /**
      * A flow property for a property that will be created by the flow. Use case: create a new user.
      *
      * @param name
      * @param dataClass
      * @param collectionClasses
      * @return this
      */
     public FlowPropertyDefinitionBuilder createCreatingFlowPropertyDefinition(String name, Class<? extends Object> dataClass,
         FlowActivityPhase whenCreated, Class<?>... collectionClasses) {
         this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, whenCreated, collectionClasses).initAccess(
             PropertyScope.flowLocal, PropertyUsage.suppliesIfMissing);
         return this;
     }
 
     public FlowPropertyDefinitionBuilder createFlowPropertyDefinitionWithDefault(String name, Class<? extends Object> dataClass,
         Object defaultObject, Class<?>... collectionClasses) {
         this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, null, collectionClasses).initAccess(PropertyScope.flowLocal,
             PropertyUsage.io).initDefaultObject(defaultObject);
         return this;
     }
 
     public FlowPropertyDefinitionBuilder createInternalStateFlowPropertyDefinition(Class<? extends Object> dataClass,
         Class<?>... collectionClasses) {
         String name = toPropertyName(dataClass);
         return createInternalStateFlowPropertyDefinition(name, dataClass, PropertyScope.flowLocal, collectionClasses);
     }
 
     public FlowPropertyDefinitionBuilder createInternalStateFlowPropertyDefinition(String name, Class<? extends Object> dataClass,
         Class<?>... collectionClasses) {
         return createInternalStateFlowPropertyDefinition(name, dataClass, PropertyScope.flowLocal, collectionClasses);
     }
 
     public FlowPropertyDefinitionBuilder createInternalStateFlowPropertyDefinition(String name, Class<? extends Object> dataClass,
         PropertyScope propertyScope, Class<?>... collectionClasses) {
         ApplicationIllegalArgumentException.valid(propertyScope != PropertyScope.global, "internalState cannot be global");
         this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, null, collectionClasses).initAccess(propertyScope,
             PropertyUsage.internalState);
         return this;
     }
 
     @SuppressWarnings("unchecked")
     public <FE extends FlowPropertyExpectation> FE createFlowPropertyExpectation(FlowPropertyDefinition flowPropertyDefinition,
         FlowPropertyValueChangeListener flowPropertyValueChangeListener) {
         return (FE) new FlowPropertyExpectationImpl(flowPropertyDefinition.getName(), flowPropertyValueChangeListener);
     }
 
     /**
      * Use case : Create a new User Which should be used?
      * {@link #createCreatingFlowPropertyDefinition(String, Class, FlowActivityPhase, Class...)} ?
      *
      * @param <FE>
      * @param flowPropertyDefinition
      * @param flowPropertyValueProvider
      * @param flowPropertyValuePersister
      * @return
      */
     public <FE extends FlowPropertyExpectation> FE createFlowPropertyExpectationToCreateReadOnlyFlowPropertyDefinition(
         FlowPropertyDefinition flowPropertyDefinition, FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider,
         FlowPropertyValuePersister flowPropertyValuePersister) {
         return (FE) new FlowPropertyExpectationImpl(flowPropertyDefinition.getName(), null, null, null, null, flowPropertyValueProvider,
             flowPropertyValuePersister, null);
     }
 
     /**
      * scans through all the {@link FlowPropertyExpectation}s looking for expectations that
      * {@link FlowPropertyExpectation#isApplicable(FlowPropertyDefinitionImplementor)} those
      * expectations have their values applied to flowPropertyDefinition in the order they are
      * encountered.
      *
      * @param additionalConfigurationParameters a list because order matters.
      * @return this
      */
     public FlowPropertyDefinitionBuilder applyFlowPropertyExpectations(List<FlowPropertyExpectation>... additionalConfigurationParameters) {
         for (List<FlowPropertyExpectation> additionalConfigurationParameterList : NotNullIterator.<List<FlowPropertyExpectation>> newNotNullIterator(additionalConfigurationParameters)) {
             for (FlowPropertyExpectation flowPropertyExpectation : NotNullIterator.<FlowPropertyExpectation> newNotNullIterator(additionalConfigurationParameterList)) {
                 if (flowPropertyExpectation.isApplicable(this.flowPropertyDefinition)) {
                     // FlowPropertyExpectation expectation applies
 
                     // TODO: use init so that if the flowPropertyDefinition already has a different value then a new flowPD can be created.
                     this.flowPropertyDefinition.addFlowPropertyValueChangeListeners(flowPropertyExpectation.getFlowPropertyValueChangeListeners());
                     FlowPropertyValueProvider<FlowPropertyProvider> flowPropertyValueProvider = flowPropertyExpectation
                         .getFlowPropertyValueProvider();
                     if (flowPropertyValueProvider != null) {
                         // forces a new valueProvider
                         initFlowPropertyValueProvider(flowPropertyValueProvider);
                     }
 
                     // TODO: Need to do test and clone!!
                     FlowActivityPhase flowActivityPhase = flowPropertyExpectation.getPropertyRequired();
                     if (flowActivityPhase != null) {
                         this.flowPropertyDefinition = this.flowPropertyDefinition.initPropertyRequired(flowActivityPhase);
                     }
                     PropertyScope propertyScope = flowPropertyExpectation.getPropertyScope();
                     if (propertyScope != null) {
                         this.flowPropertyDefinition = this.flowPropertyDefinition.initPropertyScope(propertyScope);
                     }
                     PropertyUsage propertyUsage = flowPropertyExpectation.getPropertyUsage();
                     if (propertyUsage != null) {
                         this.flowPropertyDefinition = this.flowPropertyDefinition.initPropertyUsage(propertyUsage);
                     }
                     if (!this.flowPropertyDefinition.isReadOnly()) {
                         FlowPropertyValuePersister flowPropertyValuePersister = flowPropertyExpectation.getFlowPropertyValuePersister();
                         if (flowPropertyValuePersister != null) {
                             // forces a new flowPropertyValuePersister
                             initFlowPropertyValuePersister(flowPropertyValuePersister);
                         }
                     }
                 }
             }
         }
 
         return this;
     }
 
     public FlowPropertyDefinitionBuilder initFlowPropertyValuePersister(
         FlowPropertyValuePersister<? extends FlowPropertyProvider> flowPropertyValuePersister) {
         _initFlowPropertyValuePersister(flowPropertyValuePersister, true);
         return this;
     }
 
     private boolean _initFlowPropertyValuePersister(FlowPropertyValuePersister<? extends FlowPropertyProvider> flowPropertyValuePersister,
         boolean failOnNotHandling) {
         if (flowPropertyValuePersister.isHandling(this.flowPropertyDefinition)) {
             this.flowPropertyDefinition = this.flowPropertyDefinition.initFlowPropertyValuePersister(flowPropertyValuePersister);
             return true;
         } else {
             ApplicationIllegalArgumentException
                 .valid(!failOnNotHandling, flowPropertyValuePersister + " not handling " + this.flowPropertyDefinition);
             return false;
         }
     }
 
     public FlowPropertyDefinitionBuilder initFlowPropertyValueProvider(
         FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider) {
         return initFlowPropertyValueProvider(flowPropertyValueProvider, true);
     }
 
     public FlowPropertyDefinitionBuilder initFactoryFlowPropertyValueProvider(
         FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider) {
         if (flowPropertyValueProvider.isHandling(this.flowPropertyDefinition)) {
             this.flowPropertyDefinition = this.flowPropertyDefinition.initFactoryFlowPropertyValueProvider(flowPropertyValueProvider);
         } else {
             ApplicationIllegalArgumentException.valid(false, flowPropertyValueProvider + " not handling " + this.flowPropertyDefinition);
         }
         return this;
     }
 
     public FlowPropertyDefinitionBuilder initFlowPropertyValueProvider(
         FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider, boolean failOnNotHandling) {
         _initFlowPropertyValueProvider(flowPropertyValueProvider, failOnNotHandling);
         return this;
     }
 
     private boolean _initFlowPropertyValueProvider(FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider,
         boolean failOnNotHandling) {
         if (flowPropertyValueProvider.isHandling(this.flowPropertyDefinition)) {
             this.flowPropertyDefinition = this.flowPropertyDefinition.initFlowPropertyValueProvider(flowPropertyValueProvider);
             return true;
         } else {
             ApplicationIllegalArgumentException.valid(!failOnNotHandling, flowPropertyValueProvider + " not handling " + this.flowPropertyDefinition);
             return false;
         }
     }
 
     public FlowPropertyDefinitionBuilder initAccess(PropertyScope propertyScope, PropertyUsage propertyUsage) {
         this.flowPropertyDefinition = this.flowPropertyDefinition.initAccess(propertyScope, propertyUsage);
         return this;
     }
 
     public FlowPropertyDefinitionBuilder initSensitive() {
         this.flowPropertyDefinition = this.flowPropertyDefinition.initSensitive();
         return this;
     }
 
     public FlowPropertyDefinitionBuilder initAccess(PropertyScope propertyScope, PropertyUsage propertyUsage,
         ExternalPropertyAccessRestriction externalPropertyAccessRestriction) {
         this.flowPropertyDefinition = this.flowPropertyDefinition.initAccess(propertyScope, propertyUsage, externalPropertyAccessRestriction);
         return this;
     }
 
     public FlowPropertyDefinitionBuilder applyDefaultProviders(Object... defaultProviders) {
         boolean needPersister = this.flowPropertyDefinition.getFlowPropertyValuePersister() == null && !this.flowPropertyDefinition.isReadOnly();
         boolean needProvider = !this.flowPropertyDefinition.isDefaultAvailable();
         for (Object provider : NotNullIterator.<Object> newNotNullIterator(defaultProviders)) {
             if (needPersister && (provider instanceof FlowPropertyValuePersister)) {
                 needPersister = !_initFlowPropertyValuePersister((FlowPropertyValuePersister<FlowPropertyProvider>) provider, false);
             }
             if (needProvider && (provider instanceof FlowPropertyValueProvider)) {
                 FlowPropertyValueProvider flowPropertyValueProvider = (FlowPropertyValueProvider) provider;
                 if (flowPropertyValueProvider.isHandling(this.flowPropertyDefinition)) {
                     this.flowPropertyDefinition = this.flowPropertyDefinition.initFactoryFlowPropertyValueProvider(flowPropertyValueProvider);
                     needProvider = false;
                 }
                 //    			needProvider= !_initFlowPropertyValueProvider((FlowPropertyValueProvider<? extends FlowPropertyProvider>)provider, false);
             }
             if (provider instanceof FlowPropertyValueChangeListener) {
                 this.flowPropertyDefinition.addFlowPropertyValueChangeListeners(Arrays.asList((FlowPropertyValueChangeListener) provider));
             }
         }
         return this;
     }
 
     public FlowPropertyDefinitionBuilder initTranslator(FlowTranslator<?> flowTranslator) {
         this.flowPropertyDefinition = this.flowPropertyDefinition.initTranslator(flowTranslator);
         return this;
     }
     public FlowPropertyDefinitionBuilder initElementFlowTranslator(FlowTranslator<?> flowTranslator) {
         this.flowPropertyDefinition = this.flowPropertyDefinition.initElementFlowTranslator(flowTranslator);
         return this;
     }
 
 
     public FlowPropertyDefinitionBuilder initPropertyRequired(FlowActivityPhase flowActivityPhase) {
         this.flowPropertyDefinition = this.flowPropertyDefinition.initPropertyRequired(flowActivityPhase);
         return this;
     }
 
     public FlowPropertyDefinitionBuilder addPropertiesDependentOn(FlowPropertyExpectation... propertiesDependentOn) {
         this.flowPropertyDefinition.addPropertiesDependentOn(propertiesDependentOn);
         return this;
     }
 
     public FlowPropertyDefinitionBuilder initPropertyScope(PropertyScope propertyScope) {
         this.flowPropertyDefinition = this.flowPropertyDefinition.initPropertyScope(propertyScope);
         return this;
     }
 
     public FlowPropertyDefinitionBuilder initPropertyUsage(PropertyUsage propertyUsage) {
         this.flowPropertyDefinition = this.flowPropertyDefinition.initPropertyUsage(propertyUsage);
         return this;
     }
 
 	public FlowPropertyDefinitionBuilder initDefaultObject(Object defaultObject) {
 		this.flowPropertyDefinition = this.flowPropertyDefinition.initDefaultObject(defaultObject);
 		return this;
 	}
 
 	public <FPD extends FlowPropertyDefinitionImplementor> FPD toFlowPropertyDefinition(FlowTranslatorResolver flowTranslatorResolver) {
 		if (flowTranslatorResolver != null) {
 			flowTranslatorResolver.resolve(null, flowPropertyDefinition);
 		}
 		// additional cleanup
 		if ( this.flowPropertyDefinition.isReadOnly() && this.flowPropertyDefinition.getFlowPropertyValuePersister() != null) {
 		    this.flowPropertyDefinition = this.flowPropertyDefinition.initFlowPropertyValuePersister(null);
 		}
 		return (FPD) this.flowPropertyDefinition;
 	}
 
 	  /**
      * TODO: In future setTemplateFlowPropertyDefinition() will be called so that once the FPD is
      * emitted it is not changed. (but best solution is FPD being truly immutable ) prevent further
      * changes and return constructed {@link FlowPropertyDefinition}
      *
      * @return
      */
     public <FPD extends FlowPropertyDefinitionImplementor> FPD toFlowPropertyDefinition() {
         return toFlowPropertyDefinition(null);
     }
     public static String toPropertyName(Class<?> clazz) {
         ApplicationIllegalArgumentException.valid(!clazz.isPrimitive(), clazz, ": must supply property name if clazz is a primitive");
         String packageName = clazz.getPackage().getName();
         ApplicationIllegalArgumentException.valid(!packageName.startsWith("java.") && !packageName.startsWith("javax."), clazz, ": must supply property name if clazz is java[x] class");
         return propertyNameFromClassName.get(clazz);
     }
 
     public FlowPropertyDefinitionBuilder initAutoCreate() {
         this.flowPropertyDefinition = this.flowPropertyDefinition.initAutoCreate();
         return this;
     }
 
     public FlowPropertyDefinitionBuilder addNames(String... alternateNames) {
         this.flowPropertyDefinition.addAlternateNames(alternateNames);
         return this;
     }
     public FlowPropertyDefinitionBuilder list(Class<?> elementClass) {
         this.flowPropertyDefinition.setDataClassDefinition(new DataClassDefinitionImpl(elementClass, List.class));
         return this;
     }
     public FlowPropertyDefinitionBuilder set(Class<?> elementClass) {
         this.flowPropertyDefinition.setDataClassDefinition(new DataClassDefinitionImpl(elementClass, Set.class));
         return this;
     }
     /**
      * Handles case where the key class is a string.
      * @param elementClass
      * @return
      */
     public FlowPropertyDefinitionBuilder map(Class<?> elementClass) {
         return this.map(String.class, elementClass);
     }
     public FlowPropertyDefinitionBuilder map(Class<?> keyClass, Class<?> elementClass, Class<?>... elementCollectionClasses) {
         this.flowPropertyDefinition.setDataClassDefinition(DataClassDefinitionImpl.map(keyClass, elementClass, elementCollectionClasses));
         return this;
     }
     public FlowPropertyDefinitionBuilder initSaveBack(Boolean saveBack) {
         this.flowPropertyDefinition.initSaveBack(saveBack);
         return this;
     }
 }
