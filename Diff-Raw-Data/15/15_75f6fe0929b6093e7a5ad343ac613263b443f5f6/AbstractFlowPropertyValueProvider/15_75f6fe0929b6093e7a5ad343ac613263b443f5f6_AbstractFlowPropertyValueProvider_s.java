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
 
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.amplafi.flow.FlowActivity;
 import org.amplafi.flow.FlowPropertyDefinition;
 import org.amplafi.flow.FlowPropertyValueProvider;
 import org.apache.commons.collections.CollectionUtils;
 
 import com.sworddance.util.ApplicationIllegalArgumentException;
 import com.sworddance.util.ApplicationNullPointerException;
 
 /**
  * @author patmoore
  * @param <FPP>
  *
  */
 public abstract class AbstractFlowPropertyValueProvider<FPP extends FlowPropertyProvider> implements FlowPropertyValueProvider<FPP> {
     private final Class<FPP> flowPropertyProviderClass;
     private Set<String> propertiesHandled;
     /**
      * TODO: in future should define the property requirements?
      * TODO: also if some propertiesHandled may have different requirements. - so should be a Map<String,Set<String/ FlowPropertyDefinition>>
      */
     private Set<String> requiredProperties;
     private Set<String> optionalProperties;
 
     /**
      * {@link #getFlowPropertyProviderClass()} will return {@link FlowPropertyProviderWithValues} if FPP extends that class. otherwise {@link FlowPropertyProvider}
      * @param propertiesHandled first property listed is property returned by
      */
     protected AbstractFlowPropertyValueProvider(String...propertiesHandled) {
         this.propertiesHandled = new LinkedHashSet<String>();
         this.requiredProperties = new LinkedHashSet<String>();
         this.optionalProperties = new LinkedHashSet<String>();
         CollectionUtils.addAll(this.propertiesHandled, propertiesHandled);
         Class<FPP> clazz;
         // TODO: is there a way to find out the class of FPP - last I checked there wasn't
         try {
             clazz = (Class<FPP>) FlowPropertyProviderWithValues.class;
         } catch (ClassCastException e) {
             clazz = (Class<FPP>) FlowPropertyProvider.class;
         }
         this.flowPropertyProviderClass = clazz;
     }
     /**
     *
     * @param propertiesHandled first property listed is property returned by
     */
    protected AbstractFlowPropertyValueProvider(Class<FPP>flowPropertyProviderClass, String...propertiesHandled) {
        this.flowPropertyProviderClass = flowPropertyProviderClass;
        this.propertiesHandled = new LinkedHashSet<String>();
        this.requiredProperties = new LinkedHashSet<String>();
        this.optionalProperties = new LinkedHashSet<String>();
        CollectionUtils.addAll(this.propertiesHandled, propertiesHandled);
    }
 
     protected void check(FlowPropertyDefinition flowPropertyDefinition) {
         if ( !isHandling(flowPropertyDefinition)) {
             throw new IllegalArgumentException(flowPropertyDefinition+": is not handled by "+this.getClass().getCanonicalName()+" only "+propertiesHandled);
         }
     }
     /**
      * avoids infinite loop by detecting when attempting to get the property that the FlowPropertyValueProvider is supposed to be supplying.
      * @param <T>
      * @param flowPropertyProvider -- should this be FPP?
      * @param flowPropertyDefinition
      * @param propertyName
      * @return null if {@link FlowPropertyDefinition#isNamed(String)} is true otherwise the property retrieved.
      */
     @SuppressWarnings("unchecked")
     protected <T> T getSafe(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, String propertyName) {
         if ( flowPropertyDefinition.isNamed(propertyName)) {
             return null; // TODO throw exception?
         } else {
             return (T) flowPropertyProvider.getProperty(propertyName);
         }
     }
     /**
      *
      * @param <T>
      * @param flowPropertyProvider -- should this be FPP?
      * @param flowPropertyDefinition
      * @param propertyName
      * @return will not be null.
      */
     @SuppressWarnings("unchecked")
     protected <T> T getRequired(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, String propertyName) {
         if ( flowPropertyDefinition.isNamed(propertyName)) {
             throw new ApplicationIllegalArgumentException(propertyName);
         } else {
             // HACK only temporary -- shift to flowPropertyProvider
             T result = (T) ((FlowActivity)flowPropertyProvider).getProperty(propertyName);
             if ( result == null ) {
                 throw new ApplicationNullPointerException(propertyName);
             }
             return result;
         }
     }
 
     public Collection<String> getPropertiesHandled() {
         return this.propertiesHandled;
     }
 
     /**
      *
      * @param flowPropertyDefinition
      * @return true if this {@link FlowPropertyDefinitionProvider} handles the {@link FlowPropertyDefinition}.
      */
     public boolean isHandling(FlowPropertyDefinition flowPropertyDefinition) {
         for(String propertyName: propertiesHandled) {
             if (flowPropertyDefinition.isNamed(propertyName)) {
                 return true;
             }
         }
         return false;
     }
     @SuppressWarnings("hiding")
     protected void addRequires(String...requiredProperties) {
         CollectionUtils.addAll(this.requiredProperties, requiredProperties);
     }
     public Collection<String> getRequiredProperties() {
         return this.requiredProperties;
     }
     @SuppressWarnings("hiding")
     protected void addOptional(String...optionalProperties) {
         CollectionUtils.addAll(this.optionalProperties, optionalProperties);
     }
     public Collection<String> getOptionalProperties() {
         return this.optionalProperties;
     }
     /**
      * adds in the initFlowPropertyValueProvider(this) since I keep forgetting.
      * @param flowPropertyProvider
      * @param flowPropertyDefinitions
      */
     @SuppressWarnings("unchecked")
     protected void addPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, FlowPropertyDefinitionImpl...flowPropertyDefinitions ) {
         for(FlowPropertyDefinitionImpl flowPropertyDefinition: flowPropertyDefinitions) {
             if ( !flowPropertyDefinition.isDefaultAvailable()) {
                 flowPropertyDefinition.initFlowPropertyValueProvider(this);
             }
             if ( flowPropertyDefinition.getFlowPropertyValuePersister() == null && flowPropertyProvider instanceof FlowPropertyValuePersister) {
                FlowPropertyValuePersister<? extends FlowActivity> flowPropertyValuePersister = (FlowPropertyValuePersister<? extends FlowActivity>) flowPropertyProvider;
                 flowPropertyDefinition.initFlowPropertyValuePersister(flowPropertyValuePersister);
             }
         }
         flowPropertyProvider.addPropertyDefinitions(flowPropertyDefinitions);
     }
 
     /**
      * @return the flowPropertyProviderClass
      */
     public Class<FPP> getFlowPropertyProviderClass() {
         return flowPropertyProviderClass;
     }
 }
