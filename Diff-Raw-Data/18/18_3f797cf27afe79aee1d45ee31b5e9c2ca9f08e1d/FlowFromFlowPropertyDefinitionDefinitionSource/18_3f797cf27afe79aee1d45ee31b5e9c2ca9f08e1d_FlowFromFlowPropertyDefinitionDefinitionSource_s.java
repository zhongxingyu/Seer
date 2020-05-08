 package org.amplafi.flow.definitions;
 
import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.amplafi.flow.FlowPropertyDefinition;
 
 import static org.amplafi.flow.FlowConstants.FSSINGLE_PROPERTY_NAME;
 import org.amplafi.flow.FlowImplementor;
 import org.amplafi.flow.FlowPropertyExpectation;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionProvider;
 import org.amplafi.flow.flowproperty.PropertyUsage;
 import org.amplafi.flow.impl.FlowActivityImpl;
 import org.amplafi.flow.impl.FlowImpl;
 import org.apache.commons.lang.StringUtils;
 
 import com.sworddance.util.ApplicationIllegalArgumentException;
 import static com.sworddance.util.CUtilities.*;
 
 /**
  * Define flows only needed to provide a property that is used by the ui of another flow. but is not needed by the execution of the flow itself.
  *
  * TODO: looks like we should be able to refactor some of these methods that create the flow. Bothered by the lack of DRY.
  * @author patmoore
  *
  */
 public class FlowFromFlowPropertyDefinitionDefinitionSource implements DefinitionSource<FlowImplementor> {
 
     public static final String FLOW_PREFIX = "Flow";
     private final Map<String, FlowImplementor> flows = new ConcurrentHashMap<String, FlowImplementor>();
 
     public FlowFromFlowPropertyDefinitionDefinitionSource() {
 
     }
 
     public void add(String flowPropertyName, FlowPropertyDefinitionProvider flowPropertyDefinitionProvider, List<FlowPropertyExpectation>additionalConfigurationParameters) {
        List<FlowPropertyExpectation>configurationParameters;
        if ( isNotEmpty(additionalConfigurationParameters)) {
            configurationParameters = new ArrayList<>();
            configurationParameters.addAll(additionalConfigurationParameters);
            configurationParameters.addAll(FlowPropertyDefinitionBuilder.API_RETURN_VALUE);
        } else {
            configurationParameters = FlowPropertyDefinitionBuilder.API_RETURN_VALUE;
        }

         String capitalizedFlowPropertyName = StringUtils.capitalize(flowPropertyName);
         FlowImpl flow = new FlowImpl(capitalizedFlowPropertyName+FLOW_PREFIX);
        flow.addPropertyDefinition(new FlowPropertyDefinitionBuilder(FSSINGLE_PROPERTY_NAME).applyFlowPropertyExpectations(FlowPropertyDefinitionBuilder.INTERNAL_ONLY).initDefaultObject(flowPropertyName).toFlowPropertyDefinition());
 
         FlowActivityImpl flowActivity = new FlowActivityImpl("FA");
        flowPropertyDefinitionProvider.defineFlowPropertyDefinitions(flowActivity, configurationParameters);
         flow.addActivity(flowActivity);
         put(this.flows, flow.getFlowPropertyProviderFullName(), flow);
     }
     /**
      * Add {@link FlowPropertyDefinition}s from the flowPropertyDefinitionProviders as a flow for each property.
      *
      * Only the properties that have {@link FlowPropertyDefinition#getPropertyUsage()}.{@link PropertyUsage#isOutputedProperty()} != false get their own flow.
      * This makes it safe to list properties that are expected on input without having a flow created for those input only properties.
      *
      * @param flowPropertyDefinitionProviders
      */
     public void add(FlowPropertyDefinitionProvider... flowPropertyDefinitionProviders) {
         for(FlowPropertyDefinitionProvider flowPropertyDefinitionProvider :flowPropertyDefinitionProviders) {
             List<String> outputFlowPropertyDefinitionNames = flowPropertyDefinitionProvider.getOutputFlowPropertyDefinitionNames();
             ApplicationIllegalArgumentException.valid(isNotEmpty(outputFlowPropertyDefinitionNames), flowPropertyDefinitionProvider.getClass(), " has no output properties defined.");
             for(String flowPropertyName : outputFlowPropertyDefinitionNames) {
                 this.add(flowPropertyName, flowPropertyDefinitionProvider, null);
             }
         }
     }
 
     /**
      * @see org.amplafi.flow.definitions.DefinitionSource#getFlowDefinition(java.lang.String)
      */
     @Override
     public FlowImplementor getFlowDefinition(String flowTypeName) {
         return this.flows.get(flowTypeName);
     }
 
     /**
      * @see org.amplafi.flow.definitions.DefinitionSource#getFlowDefinitions()
      */
     @Override
     public Map<String, FlowImplementor> getFlowDefinitions() {
         return this.flows;
     }
 
     /**
      * @see org.amplafi.flow.definitions.DefinitionSource#isFlowDefined(java.lang.String)
      */
     @Override
     public boolean isFlowDefined(String flowTypeName) {
         return this.flows.containsKey(flowTypeName);
     }
 }
